package bd.spark36.character;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Ordinary students strolling around campus. Each walker follows the paved path network as a
 * small waypoint graph, in a lane that was checked against the collision boxes so nobody walks
 * through a wall, tree or bench. Walkers keep personal space from each other and from the player,
 * vary their pace, ease in and out of stops, and sometimes stop to chat when they meet.
 */
public class CampusCrowd {

    /** Path junctions {x, z}, on the pavings laid out in DhakaCampusWorld, clear of buildings. */
    private static final float[][] NODES = {
        {0f, 72f},      // 0  inside the main gate
        {0f, 50f},      // 1  promenade
        {0f, 29.5f},    // 2  cross avenue junction
        {0f, 24f},      // 3  pond-ring link, south
        {-14f, 24f},    // 4  west pond ring, south
        {-14f, -10.8f}, // 5  west pond ring, north
        {14f, 24f},     // 6  east pond ring, south
        {14f, -10.8f},  // 7  east pond ring, north
        {0f, -12f},     // 8  pond-ring link, north
        {0f, -19f},     // 9  Curzon Hall steps, centre (behind the statue)
        {-30f, -16.5f}, // 10 verandah walk, west
        {30f, -16.5f},  // 11 verandah walk, east
        {-30f, 31f},    // 12 Arts Plaza (Aparajeyo Bangla)
        {-46f, 31f},    // 13 west avenue, east of the Central Library
        {-45f, -8f},    // 14 by Madhur Canteen
        {-46f, 45f},    // 15 south of the library
        {-52f, 47.5f},  // 16 Hakim Chattar
        {30f, 31f},     // 17 east avenue
        {42f, 31f},     // 18 between Raju roundabout and TSC
        {42f, -4f},     // 19 towards the sculpture garden
        {-6f, 20.8f},   // 20 detour south of the bench and bicycle on the west pond link
    };

    private static final int[][] EDGES = {
        {0, 1}, {1, 2}, {2, 3}, {3, 20}, {20, 4}, {3, 6}, {4, 5}, {6, 7}, {5, 8}, {7, 8}, {8, 9},
        {9, 10}, {9, 11}, {2, 12}, {12, 13}, {13, 14}, {13, 15}, {15, 16}, {2, 17}, {17, 18},
        {18, 19},
    };

    private static final float BODY_RADIUS = 0.35f;
    private static final float MAX_LANE = 1.4f;
    private static final float LANE_STEP = 0.35f;
    private static final float STEP_UP = 0.66f;          // same as the player: plinths, steps, terraces
    private static final float PLAYER_SPACE = 1.1f;
    private static final float NPC_SPACE = 0.9f;

    private final float[][] colliders;
    /** Per edge: sideways lane offsets whose whole run is clear of solids (empty = unusable). */
    private final float[][] edgeLanes = new float[EDGES.length][];
    /** Per node: indices of usable edges touching it. */
    private final int[][] nodeEdges;

    private static final class Walker {
        final Vector3 pos = new Vector3();
        int edge, from, to;
        float lane;
        float baseSpeed, speed, speedPhase;
        float heading;
        float walkCycle;
        float pause;         // seconds left standing still
        float chatCooldown;
        float stuckTime;
        final Vector3 lastPos = new Vector3();
        Walker chatPartner;
        int gender;
        int variant;
        boolean moving;
    }

    private final Walker[] walkers;

    public CampusCrowd(int count, float[][] colliders) {
        this.colliders = colliders;
        buildLanes();
        nodeEdges = buildNodeEdges();

        java.util.List<Integer> usable = new java.util.ArrayList<>();
        for (int e = 0; e < EDGES.length; e++) if (edgeLanes[e].length > 0) usable.add(e);

        walkers = new Walker[count];
        for (int i = 0; i < count; i++) {
            Walker w = new Walker();
            int e = usable.get(MathUtils.random(usable.size() - 1));
            boolean flip = MathUtils.randomBoolean();
            enterEdge(w, e, flip ? EDGES[e][1] : EDGES[e][0]);
            float t = MathUtils.random(0.1f, 0.9f);
            float[] a = NODES[w.from], b = NODES[w.to];
            float nx = -(b[1] - a[1]), nz = b[0] - a[0];
            float nl = (float) Math.sqrt(nx * nx + nz * nz);
            w.pos.set(MathUtils.lerp(a[0], b[0], t) + nx / nl * w.lane, 0f,
                      MathUtils.lerp(a[1], b[1], t) + nz / nl * w.lane);
            w.pos.y = supportHeight(w.pos.x, w.pos.z);
            w.heading = MathUtils.atan2(b[0] - a[0], b[1] - a[1]) * MathUtils.radiansToDegrees;
            w.baseSpeed = MathUtils.random(1.0f, 1.5f);
            w.speed = w.baseSpeed;
            w.speedPhase = MathUtils.random(MathUtils.PI2);
            w.gender = MathUtils.randomBoolean(0.45f) ? 1 : 0;
            w.variant = 1 + i % (StudentMesh.getVariantCount() - 1);   // 0 is the protagonist
            w.walkCycle = MathUtils.random(10f);
            w.chatCooldown = MathUtils.random(5f, 20f);
            walkers[i] = w;
        }
    }

    // ── Path validation ──────────────────────────────────────────────

    private void buildLanes() {
        float[] candidate = new float[(int) (2 * MAX_LANE / LANE_STEP) + 1];
        for (int e = 0; e < EDGES.length; e++) {
            float[] a = NODES[EDGES[e][0]], b = NODES[EDGES[e][1]];
            float dx = b[0] - a[0], dz = b[1] - a[1];
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            float nx = -dz / len, nz = dx / len;
            int n = 0;
            for (float lane = -MAX_LANE; lane <= MAX_LANE + 0.001f; lane += LANE_STEP) {
                if (segmentClear(a[0] + nx * lane, a[1] + nz * lane, b[0] + nx * lane, b[1] + nz * lane)) {
                    candidate[n++] = lane;
                }
            }
            edgeLanes[e] = java.util.Arrays.copyOf(candidate, n);
        }
    }

    private boolean segmentClear(float ax, float az, float bx, float bz) {
        float len = (float) Math.sqrt((bx - ax) * (bx - ax) + (bz - az) * (bz - az));
        int steps = Math.max(1, (int) (len / 0.4f));
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            if (insideSolid(MathUtils.lerp(ax, bx, t), MathUtils.lerp(az, bz, t), BODY_RADIUS + 0.1f) != null) {
                return false;
            }
        }
        return true;
    }

    /** The first solid box a circle of radius r at (x, z) overlaps, or null. */
    private float[] insideSolid(float x, float z, float r) {
        for (float[] b : colliders) {
            if (b[4] <= STEP_UP || b[1] > 1.6f) continue;   // low enough to step onto, or overhead
            if (x + r > b[0] && x - r < b[3] && z + r > b[2] && z - r < b[5]) return b;
        }
        return null;
    }

    private int[][] buildNodeEdges() {
        int[][] out = new int[NODES.length][];
        for (int node = 0; node < NODES.length; node++) {
            int n = 0;
            int[] tmp = new int[EDGES.length];
            for (int e = 0; e < EDGES.length; e++) {
                if (edgeLanes[e].length == 0) continue;
                if (EDGES[e][0] == node || EDGES[e][1] == node) tmp[n++] = e;
            }
            out[node] = java.util.Arrays.copyOf(tmp, n);
        }
        return out;
    }

    private void enterEdge(Walker w, int e, int fromNode) {
        w.edge = e;
        w.from = fromNode;
        w.to = EDGES[e][0] == fromNode ? EDGES[e][1] : EDGES[e][0];
        float[] lanes = edgeLanes[e];
        // Lanes are measured from the edge's own direction; walking it backwards mirrors them
        float lane = lanes[MathUtils.random(lanes.length - 1)];
        w.lane = (EDGES[e][0] == fromNode) ? lane : -lane;
    }

    // ── Simulation ───────────────────────────────────────────────────

    public void update(float delta, Vector3 playerPos) {
        if (delta > 0.1f) delta = 0.1f;
        for (Walker w : walkers) {
            w.chatCooldown -= delta;

            if (w.pause > 0f) {
                w.pause -= delta;
                w.speed = Math.max(0f, w.speed - 3f * delta);       // ease to a stop
                if (w.chatPartner != null) turnTowards(w, w.chatPartner.pos.x - w.pos.x,
                                                           w.chatPartner.pos.z - w.pos.z, delta, 4f);
                if (w.pause <= 0f) w.chatPartner = null;
                w.moving = w.speed > 0.05f;
                if (!w.moving) continue;
            } else {
                // Pace drifts gently, and picks back up smoothly after a stop
                float target = w.baseSpeed * (1f + 0.08f * MathUtils.sin(w.speedPhase += delta * 0.4f));
                w.speed += (target - w.speed) * Math.min(1f, 1.5f * delta);
            }

            float[] a = NODES[w.from], b = NODES[w.to];
            float sx = b[0] - a[0], sz = b[1] - a[1];
            float len = (float) Math.sqrt(sx * sx + sz * sz);
            float nx = -sz / len, nz = sx / len;
            float tx = b[0] + nx * w.lane, tz = b[1] + nz * w.lane;

            float dx = tx - w.pos.x, dz = tz - w.pos.z;
            float dist = (float) Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.5f && w.pause <= 0f) {
                pickNextEdge(w);
                continue;
            }

            float vx = dx / dist * w.speed, vz = dz / dist * w.speed;

            // Keep personal space: the player, then the other walkers
            float[] push = avoid(w.pos.x, w.pos.z, playerPos.x, playerPos.z, PLAYER_SPACE, 1.6f);
            vx += push[0] * w.baseSpeed;
            vz += push[1] * w.baseSpeed;
            for (Walker o : walkers) {
                if (o == w) continue;
                push = avoid(w.pos.x, w.pos.z, o.pos.x, o.pos.z, NPC_SPACE, 0.9f);
                vx += push[0] * w.baseSpeed;
                vz += push[1] * w.baseSpeed;
                maybeStartChat(w, o);
            }

            w.lastPos.set(w.pos);
            moveWithCollision(w, vx * delta, vz * delta);
            // Snagged on a corner: after a while, give up and head back the way we came
            if (w.pos.dst2(w.lastPos) < (w.speed * delta * 0.2f) * (w.speed * delta * 0.2f)) {
                w.stuckTime += delta;
                if (w.stuckTime > 2.5f) {
                    w.stuckTime = 0f;
                    enterEdge(w, w.edge, w.to);
                }
            } else {
                w.stuckTime = 0f;
            }
            float ground = supportHeight(w.pos.x, w.pos.z);
            w.pos.y += (ground - w.pos.y) * Math.min(1f, 12f * delta);
            w.moving = true;
            w.walkCycle += 8.5f * (w.speed / 1.45f) * delta;
            turnTowards(w, vx, vz, delta, 5f);
        }
    }

    private final float[] pushTmp = new float[2];

    /** Sideways push away from (ox, oz) when closer than 2 * space, stronger the closer. */
    private float[] avoid(float x, float z, float ox, float oz, float space, float strength) {
        float px = x - ox, pz = z - oz;
        float d2 = px * px + pz * pz;
        pushTmp[0] = pushTmp[1] = 0f;
        float reach = 2f * space;
        if (d2 < reach * reach && d2 > 1e-6f) {
            float d = (float) Math.sqrt(d2);
            float k = (reach - d) / space * strength;
            pushTmp[0] = px / d * k;
            pushTmp[1] = pz / d * k;
        }
        return pushTmp;
    }

    private void maybeStartChat(Walker w, Walker o) {
        if (w.chatCooldown > 0f || o.chatCooldown > 0f || o.pause > 0f) return;
        float dx = o.pos.x - w.pos.x, dz = o.pos.z - w.pos.z;
        if (dx * dx + dz * dz > 2.2f * 2.2f) return;
        float chat = MathUtils.random(3f, 7f);
        w.pause = o.pause = chat;
        w.chatPartner = o;
        o.chatPartner = w;
        w.chatCooldown = o.chatCooldown = chat + MathUtils.random(20f, 45f);
    }

    /** Moves per axis and refuses any step that would put the walker inside a solid. */
    private void moveWithCollision(Walker w, float mx, float mz) {
        if (insideSolid(w.pos.x + mx, w.pos.z, BODY_RADIUS) == null) w.pos.x += mx;
        if (insideSolid(w.pos.x, w.pos.z + mz, BODY_RADIUS) == null) w.pos.z += mz;
        // Already overlapping (e.g. shoved by the player): push out along the shallowest axis
        float[] b = insideSolid(w.pos.x, w.pos.z, BODY_RADIUS);
        if (b != null) {
            float left = w.pos.x + BODY_RADIUS - b[0], right = b[3] - (w.pos.x - BODY_RADIUS);
            float back = w.pos.z + BODY_RADIUS - b[2], front = b[5] - (w.pos.z - BODY_RADIUS);
            float m = Math.min(Math.min(left, right), Math.min(back, front));
            if (m == left) w.pos.x -= left;
            else if (m == right) w.pos.x += right;
            else if (m == back) w.pos.z -= back;
            else w.pos.z += front;
        }
    }

    /** Height of the highest step-up surface (plinth, stair, terrace) under the walker. */
    private float supportHeight(float x, float z) {
        float h = 0f;
        for (float[] b : colliders) {
            if (b[4] > STEP_UP || b[4] <= h) continue;
            if (x > b[0] && x < b[3] && z > b[2] && z < b[5]) h = b[4];
        }
        return h;
    }

    private void turnTowards(Walker w, float dx, float dz, float delta, float rate) {
        if (dx * dx + dz * dz < 1e-6f) return;
        float target = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
        float diff = (target - w.heading) % 360f;
        if (diff > 180f) diff -= 360f;
        if (diff < -180f) diff += 360f;
        w.heading += diff * Math.min(1f, rate * delta);
    }

    private void pickNextEdge(Walker w) {
        int[] options = nodeEdges[w.to];
        int next = w.edge;
        if (options.length > 1) {
            // Don't turn straight back unless it's a dead end
            do {
                next = options[MathUtils.random(options.length - 1)];
            } while (next == w.edge);
        }
        enterEdge(w, next, w.to);
        if (MathUtils.randomBoolean(0.2f)) {
            w.pause = MathUtils.random(1.5f, 4.5f);   // look around, check the phone
        }
    }

    /** Draws every walker; pass a null environment for the depth (shadow) pass. */
    public void render(ModelBatch batch, Environment env, StudentMesh mesh) {
        for (Walker w : walkers) {
            mesh.render(batch, env, w.pos, w.heading, w.moving ? w.walkCycle : 0f, w.moving,
                false, false, false, 0, 0f, false, 0f, w.gender, w.variant);
        }
    }

    /** Path segments rejected because every lane crosses a solid, for the startup log. */
    public String blockedEdges() {
        StringBuilder sb = new StringBuilder();
        for (int e = 0; e < EDGES.length; e++) {
            if (edgeLanes[e].length == 0) {
                sb.append(EDGES[e][0]).append('-').append(EDGES[e][1]).append(" by");
                float[] na = NODES[EDGES[e][0]], nb = NODES[EDGES[e][1]];
                java.util.Set<float[]> hits = new java.util.LinkedHashSet<>();
                for (int i = 0; i <= 40; i++) {
                    float t = i / 40f;
                    float[] hit = insideSolid(MathUtils.lerp(na[0], nb[0], t), MathUtils.lerp(na[1], nb[1], t), BODY_RADIUS + 0.1f);
                    if (hit != null) hits.add(hit);
                }
                for (float[] h : hits) sb.append(java.util.Arrays.toString(h));
                sb.append(" | ");
            }
        }
        return sb.toString().trim();
    }

    /** Number of path segments that survived validation, for the startup log. */
    public int usableEdgeCount() {
        int n = 0;
        for (float[] lanes : edgeLanes) if (lanes.length > 0) n++;
        return n;
    }
}
