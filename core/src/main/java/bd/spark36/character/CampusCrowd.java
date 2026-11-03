// ei file ta campus e student der path dhore shavabik chola fira koray
package bd.spark36.character;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.level.DialogueRepository;

public class CampusCrowd {

    public static final float[][] NODES = CampusWaypointGraph.NODES;
    public static final int[][] EDGES = CampusWaypointGraph.EDGES;

    private static final float PLAYER_SPACE = 1.2f;
    private static final float NPC_SPACE = 0.95f;

    private final CampusWaypointGraph graph;
    private final StudentNPC[] students;
    private int activeLevelNumber = 1;

    private final float[] pushTmp = new float[2];

    public CampusCrowd(int count, float[][] colliders) {
        this.graph = new CampusWaypointGraph(colliders);

        java.util.List<Integer> usable = new java.util.ArrayList<>();
        float[][] lanes = graph.getEdgeLanes();
        for (int e = 0; e < EDGES.length; e++) {
            if (lanes[e] != null && lanes[e].length > 0) usable.add(e);
        }

        students = new StudentNPC[count];
        for (int i = 0; i < count; i++) {
            int gender = MathUtils.randomBoolean(0.48f) ? 1 : 0;
            int variant = 1 + (i % (StudentMesh.getVariantCount() - 1));
            float speed = MathUtils.random(1.10f, 1.48f);
            String role = (gender == 1) ? "DU Student" : "DU Student";

            StudentNPC s = new StudentNPC(i, gender, variant, speed, role);

            if (!usable.isEmpty()) {
                int e = usable.get(MathUtils.random(usable.size() - 1));
                boolean flip = MathUtils.randomBoolean();
                int fromNode = flip ? EDGES[e][1] : EDGES[e][0];
                int toNode = flip ? EDGES[e][0] : EDGES[e][1];
                s.enterEdge(graph, e, fromNode);

                float t = MathUtils.random(0.12f, 0.88f);
                float[] a = NODES[fromNode], b = NODES[toNode];
                float dx = b[0] - a[0], dz = b[1] - a[1];
                float len = (float) Math.sqrt(dx * dx + dz * dz);
                if (len < 0.001f) len = 0.001f;
                float nx = -dz / len, nz = dx / len;

                float[] edgeLanes = lanes[e];
                float chosenLane = (edgeLanes != null && edgeLanes.length > 0) ?
                    edgeLanes[MathUtils.random(edgeLanes.length - 1)] : 0f;
                s.setLane(chosenLane);

                s.setPosition(
                    MathUtils.lerp(a[0], b[0], t) + nx * chosenLane,
                    graph.supportHeight(a[0], a[1]),
                    MathUtils.lerp(a[1], b[1], t) + nz * chosenLane
                );
                s.setHeading(MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees);
            }
            students[i] = s;
        }

        setLevelProfile(1);
        seedPromenadeWalkers();
    }

    public void seedPromenadeWalkers() {
        seedCampusWalkers();
    }

    public void seedCampusWalkers() {
        if (students == null || students.length == 0) return;
        float[][] campusSpawns = {
            // Main Promenade avenue
            {  1.8f, 66.0f, 0, 1, 180f },
            { -1.8f, 61.0f, 1, 0,   0f },
            {  2.0f, 54.0f, 1, 2, 180f },
            { -2.2f, 47.0f, 2, 1,   0f },
            {  1.6f, 40.0f, 1, 2, 180f },
            { -1.5f, 34.0f, 2, 1,   0f },
            // Curzon Hall Courtyard & Verandahs
            {  0.0f, -13.0f, 8, 9, 180f },
            {  3.6f, -16.0f, 9, 11, 90f },
            { -15.0f, -16.5f, 9, 10, -90f },
            {  15.0f, -16.5f, 9, 11, 90f },
            // Lotus Pond Perimeter Walkways
            { -14.0f,  10.0f, 4, 5, 180f },
            {  14.0f,  10.0f, 6, 7, 180f },
            { -14.0f,  -2.0f, 5, 4,   0f },
            {  14.0f,  -2.0f, 7, 6,   0f },
            {  -7.0f,  22.5f, 3, 20, -90f },
            {   7.0f,  24.0f, 3, 6,  90f },
            // Arts Plaza & Central Library
            { -20.0f,  31.0f, 2, 12, -90f },
            { -32.0f,  28.0f, 12, 13, -90f },
            { -46.0f,  35.0f, 13, 15,   0f },
            { -50.0f,  46.0f, 15, 16, -90f },
            // Raju Sculpture & TSC
            {  20.0f,  31.0f, 2, 17, 90f },
            {  35.0f,  31.0f, 17, 18, 90f },
            {  42.0f,  20.0f, 18, 19, 180f },
            {  42.0f,   5.0f, 19, 18,   0f }
        };
        int count = Math.min(campusSpawns.length, students.length);
        for (int i = 0; i < count; i++) {
            float[] sp = campusSpawns[i];
            StudentNPC s = students[i];
            int fromNode = (int) sp[2];
            int toNode = (int) sp[3];
            int edge = -1;
            for (int e = 0; e < EDGES.length; e++) {
                if ((EDGES[e][0] == fromNode && EDGES[e][1] == toNode) ||
                    (EDGES[e][0] == toNode && EDGES[e][1] == fromNode)) {
                    edge = e;
                    break;
                }
            }
            if (edge == -1) edge = 0;
            s.enterEdge(graph, edge, fromNode);
            s.setPosition(sp[0], graph.supportHeight(sp[0], sp[1]), sp[1]);
            s.setHeading(sp[4]);
            s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(25f, 55f));
        }
    }

    public void setLevelProfile(int levelNumber) {
        this.activeLevelNumber = levelNumber;

        for (int i = 0; i < students.length; i++) {
            StudentNPC s = students[i];
            s.setLevelNumber(levelNumber);
            switch (levelNumber) {
                case 2:
                    if (i % 4 == 0) {
                        s.transitionTo(NpcBehaviorState.GROUPING, MathUtils.random(10f, 20f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_ARTS_PLAZA);
                    } else if (i % 4 == 1) {
                        s.transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(2f, 4f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(8f, 20f));
                    }
                    break;

                case 3:
                    if (i % 4 == 0) {
                        s.transitionTo(NpcBehaviorState.FLEEING, MathUtils.random(10f, 20f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_MAIN_GATE);
                    } else if (i % 4 == 1) {
                        s.transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(2f, 4f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(8f, 20f));
                    }
                    break;

                case 4:
                    if (i % 2 == 0) {
                        s.transitionTo(NpcBehaviorState.IDLE, MathUtils.random(3f, 6f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(10f, 22f));
                    }
                    break;

                case 5:
                    if (i % 3 == 0) {
                        s.transitionTo(NpcBehaviorState.CHANTING, MathUtils.random(8f, 16f));
                    } else if (i % 3 == 1) {
                        s.transitionTo(NpcBehaviorState.GROUPING, MathUtils.random(10f, 20f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_RAJU_SCULPTURE);
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(8f, 20f));
                    }
                    break;

                case 6:
                    if (i % 4 == 0) {
                        s.transitionTo(NpcBehaviorState.CHANTING, MathUtils.random(6f, 12f));
                    } else if (i % 4 == 1) {
                        s.transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(2f, 4f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(12f, 28f));
                    }
                    break;

                case 1:
                default:
                    if (i % 8 == 0) {
                        s.transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(1.2f, 2.4f));
                    } else if (i % 8 == 1) {
                        s.transitionTo(NpcBehaviorState.IDLE, MathUtils.random(1.0f, 2.0f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(20f, 45f));
                    }
                    break;
            }
        }
        if (levelNumber == 1) {
            seedCampusWalkers();
        }
    }

    public void update(float delta, Vector3 playerPos) {
        if (delta > 0.1f) delta = 0.1f;

        for (int i = 0; i < students.length; i++) {
            StudentNPC s = students[i];
            s.update(delta, graph, playerPos, delta);

            if (playerPos != null) {
                float[] push = avoid(s.getPosition().x, s.getPosition().z, playerPos.x, playerPos.z, PLAYER_SPACE, 1.8f);
                if (push[0] != 0f || push[1] != 0f) {
                    s.applyAvoidanceForce(push[0], push[1], delta, graph);
                }
            }

            for (int j = i + 1; j < students.length; j++) {
                StudentNPC o = students[j];
                float dx = s.getPosition().x - o.getPosition().x;
                float dz = s.getPosition().z - o.getPosition().z;
                float d2 = dx * dx + dz * dz;

                if (d2 < (2f * NPC_SPACE) * (2f * NPC_SPACE) && d2 > 0.0001f) {
                    float[] push = avoid(s.getPosition().x, s.getPosition().z, o.getPosition().x, o.getPosition().z, NPC_SPACE, 0.95f);
                    s.applyAvoidanceForce(push[0], push[1], delta, graph);
                    o.applyAvoidanceForce(-push[0], -push[1], delta, graph);

                    if (s.getState() == NpcBehaviorState.WALKING && o.getState() == NpcBehaviorState.WALKING &&
                        s.getChatCooldown() <= 0f && o.getChatCooldown() <= 0f && d2 < 1.15f * 1.15f && MathUtils.randomBoolean(0.30f)) {
                        float chatDuration = MathUtils.random(1.8f, 3.0f);
                        s.startSocializing(o, chatDuration);
                        o.startSocializing(s, chatDuration);
                    }
                }
            }
        }
    }

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

    public StudentNPC getClosestInteractable(Vector3 playerPos, float maxDistance) {
        if (playerPos == null || students == null) return null;
        StudentNPC best = null;
        float bestDistSq = maxDistance * maxDistance;

        for (StudentNPC s : students) {
            if (!s.isInteractable() && s.getState() != NpcBehaviorState.INTERACTING_PLAYER) continue;
            float dx = s.getPosition().x - playerPos.x;
            float dz = s.getPosition().z - playerPos.z;
            float d2 = dx * dx + dz * dz;
            if (d2 < bestDistSq) {
                bestDistSq = d2;
                best = s;
            }
        }
        return best;
    }

    public boolean interactWithClosest(Vector3 playerPos, float maxDistance, int levelNumber, WhereWindsMeetHUD hud) {
        StudentNPC npc = getClosestInteractable(playerPos, maxDistance);
        if (npc != null) {
            npc.setLevelNumber(levelNumber);
            return npc.onInteract(playerPos, null, hud);
        }
        return false;
    }

    public void render(ModelBatch batch, Environment env, StudentMesh mesh) {
        for (StudentNPC s : students) {
            s.render(batch, env, mesh);
        }
    }

    public boolean checkHitByPlayer(Vector3 playerPos, float heading, float attackRange) {
        if (students == null) return false;
        for (StudentNPC s : students) {
            float dx = s.getPosition().x - playerPos.x;
            float dz = s.getPosition().z - playerPos.z;
            float distSq = dx * dx + dz * dz;
            if (distSq <= attackRange * attackRange) {
                float angleToTarget = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
                float diff = Math.abs((angleToTarget - heading + 540f) % 360f - 180f);
                if (diff < 80f) {
                    return true;
                }
            }
        }
        return false;
    }

    public StudentNPC[] getStudents() {
        return students;
    }

    public int getStudentCount() {
        return students != null ? students.length : 0;
    }

    public CampusWaypointGraph getGraph() {
        return graph;
    }

    public String blockedEdges() {
        StringBuilder sb = new StringBuilder();
        float[][] lanes = graph.getEdgeLanes();
        for (int e = 0; e < EDGES.length; e++) {
            if (lanes[e].length == 0) {
                sb.append(EDGES[e][0]).append('-').append(EDGES[e][1]).append(" | ");
            }
        }
        return sb.toString().trim();
    }

    public int usableEdgeCount() {
        int n = 0;
        for (float[] l : graph.getEdgeLanes()) if (l.length > 0) n++;
        return n;
    }
}
