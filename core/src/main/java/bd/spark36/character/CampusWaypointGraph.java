package bd.spark36.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Validated waypoint navigation network for the University of Dhaka campus.
 * Provides lane-offset path clearance checking against solid campus colliders,
 * collision avoidance against buildings, trees, and water boundaries, and
 * Dijkstra shortest-path routing between campus landmarks.
 */
public class CampusWaypointGraph {

    // Named landmark node indices for AI goals
    public static final int NODE_MAIN_GATE = 0;
    public static final int NODE_PROMENADE = 1;
    public static final int NODE_CROSS_AVENUE = 2;
    public static final int NODE_POND_SOUTH = 3;
    public static final int NODE_WEST_POND_SOUTH = 4;
    public static final int NODE_WEST_POND_NORTH = 5;
    public static final int NODE_EAST_POND_SOUTH = 6;
    public static final int NODE_EAST_POND_NORTH = 7;
    public static final int NODE_POND_NORTH = 8;
    public static final int NODE_CURZON_STEPS = 9;
    public static final int NODE_CURZON_WEST_VERANDAH = 10;
    public static final int NODE_CURZON_EAST_VERANDAH = 11;
    public static final int NODE_ARTS_PLAZA = 12;
    public static final int NODE_CENTRAL_LIBRARY = 13;
    public static final int NODE_MADHUR_CANTEEN = 14;
    public static final int NODE_LIBRARY_SOUTH = 15;
    public static final int NODE_HAKIM_CHATTAR = 16;
    public static final int NODE_EAST_AVENUE = 17;
    public static final int NODE_RAJU_SCULPTURE = 18;
    public static final int NODE_TSC = 19;
    public static final int NODE_WEST_POND_DETOUR = 20;

    /** Path junctions {x, z}, on the pavings laid out in DhakaCampusWorld, clear of buildings. */
    public static final float[][] NODES = {
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
        {42f, -4f},     // 19 towards the sculpture garden / TSC
        {-6f, 20.8f},   // 20 detour south of the bench and bicycle on the west pond link
    };

    public static final int[][] EDGES = {
        {0, 1}, {1, 2}, {2, 3}, {3, 20}, {20, 4}, {3, 6}, {4, 5}, {6, 7}, {5, 8}, {7, 8}, {8, 9},
        {9, 10}, {9, 11}, {2, 12}, {12, 13}, {13, 14}, {13, 15}, {15, 16}, {2, 17}, {17, 18},
        {18, 19},
    };

    public static final float BODY_RADIUS = 0.35f;
    public static final float MAX_LANE = 1.4f;
    public static final float LANE_STEP = 0.35f;
    public static final float STEP_UP = 0.66f; // stairs, plinths, terraces

    private final float[][] colliders;
    private final float[][] edgeLanes = new float[EDGES.length][];
    private final int[][] nodeEdges;
    private final float[] edgeLengths = new float[EDGES.length];

    // Pre-allocated buffers for pathfinding (Zero per-frame allocation)
    private final float[] dist = new float[NODES.length];
    private final int[] prev = new int[NODES.length];
    private final boolean[] visited = new boolean[NODES.length];

    public CampusWaypointGraph(float[][] colliders) {
        this.colliders = colliders != null ? colliders : new float[0][0];
        calculateEdgeLengths();
        buildLanes();
        this.nodeEdges = buildNodeEdges();
    }

    private void calculateEdgeLengths() {
        for (int e = 0; e < EDGES.length; e++) {
            float[] a = NODES[EDGES[e][0]];
            float[] b = NODES[EDGES[e][1]];
            float dx = b[0] - a[0];
            float dz = b[1] - a[1];
            edgeLengths[e] = (float) Math.sqrt(dx * dx + dz * dz);
        }
    }

    private void buildLanes() {
        float[] candidate = new float[(int) (2 * MAX_LANE / LANE_STEP) + 1];
        for (int e = 0; e < EDGES.length; e++) {
            float[] a = NODES[EDGES[e][0]], b = NODES[EDGES[e][1]];
            float dx = b[0] - a[0], dz = b[1] - a[1];
            float len = edgeLengths[e];
            float nx = -dz / len, nz = dx / len;
            int n = 0;
            for (float lane = -MAX_LANE; lane <= MAX_LANE + 0.001f; lane += LANE_STEP) {
                if (segmentClear(a[0] + nx * lane, a[1] + nz * lane, b[0] + nx * lane, b[1] + nz * lane)) {
                    candidate[n++] = lane;
                }
            }
            edgeLanes[e] = Arrays.copyOf(candidate, n);
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

    /** Returns the first solid collider box a circle of radius r at (x, z) overlaps, or null. */
    public float[] insideSolid(float x, float z, float r) {
        for (float[] b : colliders) {
            if (b[4] <= STEP_UP || b[1] > 1.6f) continue; // low enough to step onto, or overhead
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
            out[node] = Arrays.copyOf(tmp, n);
        }
        return out;
    }

    /**
     * Finds the shortest path between two nodes using Dijkstra's algorithm.
     * Writes node indices into {@code outPath} in forward order (starting from startNode's neighbor to goalNode).
     * Returns the number of path nodes written, or 0 if unreachable or start==goal.
     */
    public synchronized int findPath(int startNode, int goalNode, int[] outPath) {
        if (startNode < 0 || startNode >= NODES.length || goalNode < 0 || goalNode >= NODES.length) {
            return 0;
        }
        if (startNode == goalNode) {
            return 0;
        }

        Arrays.fill(dist, Float.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        Arrays.fill(visited, false);

        dist[startNode] = 0f;

        for (int iter = 0; iter < NODES.length; iter++) {
            int u = -1;
            float minDist = Float.POSITIVE_INFINITY;
            for (int i = 0; i < NODES.length; i++) {
                if (!visited[i] && dist[i] < minDist) {
                    minDist = dist[i];
                    u = i;
                }
            }

            if (u == -1 || u == goalNode) break;
            visited[u] = true;

            int[] connectedEdges = nodeEdges[u];
            for (int e : connectedEdges) {
                int v = (EDGES[e][0] == u) ? EDGES[e][1] : EDGES[e][0];
                if (!visited[v]) {
                    float alt = dist[u] + edgeLengths[e];
                    if (alt < dist[v]) {
                        dist[v] = alt;
                        prev[v] = u;
                    }
                }
            }
        }

        if (dist[goalNode] == Float.POSITIVE_INFINITY) {
            return 0; // No valid path
        }

        // Reconstruct path backward
        int count = 0;
        int curr = goalNode;
        while (curr != -1 && curr != startNode) {
            count++;
            curr = prev[curr];
        }

        if (outPath == null || outPath.length < count) {
            return count;
        }

        curr = goalNode;
        for (int i = count - 1; i >= 0; i--) {
            outPath[i] = curr;
            curr = prev[curr];
        }

        return count;
    }

    /** Returns the nearest node index to a 3D world coordinate. */
    public int getNearestNode(float x, float z) {
        int best = 0;
        float bestDistSq = Float.MAX_VALUE;
        for (int i = 0; i < NODES.length; i++) {
            float dx = NODES[i][0] - x;
            float dz = NODES[i][1] - z;
            float d2 = dx * dx + dz * dz;
            if (d2 < bestDistSq) {
                bestDistSq = d2;
                best = i;
            }
        }
        return best;
    }

    public int[][] getNodeEdges() {
        return nodeEdges;
    }

    public float[][] getEdgeLanes() {
        return edgeLanes;
    }

    public float getEdgeLength(int edgeIndex) {
        return edgeLengths[edgeIndex];
    }

    /** Height of the highest step-up surface (plinth, stair, terrace) under (x, z). */
    public float supportHeight(float x, float z) {
        float h = 0f;
        for (float[] b : colliders) {
            if (b[4] > STEP_UP || b[4] <= h) continue;
            if (x > b[0] && x < b[3] && z > b[2] && z < b[5]) h = b[4];
        }
        return h;
    }

    public int getNodeCount() {
        return NODES.length;
    }

    public int getEdgeCount() {
        return EDGES.length;
    }
}
