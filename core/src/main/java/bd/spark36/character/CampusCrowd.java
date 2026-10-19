package bd.spark36.character;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.level.DialogueRepository;

/**
 * Realistic Campus Student Crowd & Population Management System for "Spark: Birth of 36 July".
 * Features:
 * - Autonomous individual StudentNPC agents with AI state machines.
 * - Level-aware behavioral profiles matching historical dates (July 1 to August 5, 2024).
 * - Navigation along the verified campus waypoint network clear of solid obstacles and water.
 * - Phase 7 anatomical CharacterPose kinematics (hip sway, breathing, look-around, conversation gesturing, chanting).
 * - Local neighborhood obstacle and student-student / student-player avoidance.
 * - Interactive student dialogue integrated with the HUD.
 */
public class CampusCrowd {

    // Re-expose waypoint geometry for backward compatibility
    public static final float[][] NODES = CampusWaypointGraph.NODES;
    public static final int[][] EDGES = CampusWaypointGraph.EDGES;

    private static final float PLAYER_SPACE = 1.2f;
    private static final float NPC_SPACE = 0.95f;

    private final CampusWaypointGraph graph;
    private final StudentNPC[] students;
    private int activeLevelNumber = 1;

    // Local temporary math buffers (Zero heap allocation during runtime)
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

            // Spawn onto a validated clear edge lane
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

                // Pick an initial lane offset
                float chosenLane = (i % 2 == 0 ? 1f : -1f) * MathUtils.random(0.3f, 1.2f);
                float spawnX = MathUtils.lerp(a[0], b[0], t) + nx * chosenLane;
                float spawnZ = MathUtils.lerp(a[1], b[1], t) + nz * chosenLane;
                // Keep the spawn area at the main gate (Z > 56m, |X| < 8m) completely clear of standing NPCs
                if (Math.abs(spawnX) < 8f && spawnZ > 56f) {
                    spawnZ = 52f - (float) i * 2.2f;
                }

                s.setPosition(
                    spawnX,
                    graph.supportHeight(spawnX, spawnZ),
                    spawnZ
                );
                s.setHeading(MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees);
            }
            students[i] = s;
        }

        // Apply initial level profile (Level 1 peaceful by default)
        setLevelProfile(1);
    }

    /**
     * Configures crowd behavior profile to reflect the active historical level.
     */
    public void setLevelProfile(int levelNumber) {
        this.activeLevelNumber = levelNumber;

        for (int i = 0; i < students.length; i++) {
            StudentNPC s = students[i];
            s.setLevelNumber(levelNumber);
            switch (levelNumber) {
                case 2: // July 15–16: Student Confrontation & Resistance
                    if (i % 3 == 0) {
                        s.transitionTo(NpcBehaviorState.GROUPING, MathUtils.random(10f, 25f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_ARTS_PLAZA);
                    } else if (i % 3 == 1) {
                        s.transitionTo(NpcBehaviorState.SOCIALIZING, MathUtils.random(8f, 20f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(6f, 15f));
                    }
                    break;

                case 3: // July 18: Police Escalation & Tear Gas
                    if (i % 4 == 0) {
                        s.transitionTo(NpcBehaviorState.FLEEING, MathUtils.random(12f, 25f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_MAIN_GATE);
                    } else if (i % 4 == 1) {
                        s.transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(4f, 10f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(8f, 18f));
                    }
                    break;

                case 4: // July 19–20: Curfew & Internet Blackout
                    if (i % 2 == 0) {
                        s.transitionTo(NpcBehaviorState.IDLE, MathUtils.random(6f, 14f));
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(10f, 22f));
                    }
                    break;

                case 5: // August 3–4: Mass Mobilization — 1-Point Demand
                    if (i % 3 == 0) {
                        s.transitionTo(NpcBehaviorState.CHANTING, MathUtils.random(15f, 35f));
                    } else if (i % 3 == 1) {
                        s.transitionTo(NpcBehaviorState.GROUPING, MathUtils.random(12f, 28f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_RAJU_SCULPTURE);
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(6f, 16f));
                    }
                    break;

                case 6: // August 5: Victory Epilogue — 36 July
                    if (i % 2 == 0) {
                        s.transitionTo(NpcBehaviorState.CHANTING, MathUtils.random(20f, 40f));
                    } else {
                        s.transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(8f, 20f));
                    }
                    break;

                case 1: // July 1–14: Peaceful Academic Life & Early Quota Awareness
                default:
                    if (i % 3 == 0) {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(14f, 28f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_ARTS_PLAZA);
                    } else if (i % 3 == 1) {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(12f, 24f));
                        s.routeToNode(graph, CampusWaypointGraph.NODE_CURZON_STEPS);
                    } else {
                        s.transitionTo(NpcBehaviorState.WALKING, MathUtils.random(10f, 20f));
                    }
                    break;
            }
        }
    }

    /**
     * Updates simulation, state machine transitions, crowd avoidance, and kinematics.
     */
    public void update(float delta, Vector3 playerPos) {
        if (delta > 0.1f) delta = 0.1f;

        for (int i = 0; i < students.length; i++) {
            StudentNPC s = students[i];
            s.update(delta, graph, playerPos, delta);

            // 1. Local avoidance against player
            if (playerPos != null) {
                float[] push = avoid(s.getPosition().x, s.getPosition().z, playerPos.x, playerPos.z, PLAYER_SPACE, 1.8f);
                if (push[0] != 0f || push[1] != 0f) {
                    s.applyAvoidanceForce(push[0], push[1], delta);
                }
            }

            // 2. Local avoidance against peer students + organic conversation triggering
            for (int j = i + 1; j < students.length; j++) {
                StudentNPC o = students[j];
                float dx = s.getPosition().x - o.getPosition().x;
                float dz = s.getPosition().z - o.getPosition().z;
                float d2 = dx * dx + dz * dz;

                if (d2 < (2f * NPC_SPACE) * (2f * NPC_SPACE) && d2 > 0.0001f) {
                    float[] push = avoid(s.getPosition().x, s.getPosition().z, o.getPosition().x, o.getPosition().z, NPC_SPACE, 0.95f);
                    s.applyAvoidanceForce(push[0], push[1], delta);
                    o.applyAvoidanceForce(-push[0], -push[1], delta);

                    // Organic peer conversation initiation
                    if (s.getState() == NpcBehaviorState.WALKING && o.getState() == NpcBehaviorState.WALKING &&
                        s.getChatCooldown() <= 0f && o.getChatCooldown() <= 0f && d2 < 2.0f * 2.0f) {
                        float chatDuration = MathUtils.random(4.0f, 8.5f);
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

    /**
     * Finds the nearest student within interaction distance who is available for conversation.
     */
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

    /**
     * Starts player conversation with the closest interactable student and displays dialogue in HUD.
     * Returns true if interaction was initiated.
     */
    public boolean interactWithClosest(Vector3 playerPos, float maxDistance, int levelNumber, WhereWindsMeetHUD hud) {
        StudentNPC npc = getClosestInteractable(playerPos, maxDistance);
        if (npc != null) {
            npc.setLevelNumber(levelNumber);
            return npc.onInteract(playerPos, null, hud);
        }
        return false;
    }

    /**
     * Draws every student using evaluated CharacterPose and PBR materials.
     */
    public void render(ModelBatch batch, Environment env, StudentMesh mesh) {
        for (StudentNPC s : students) {
            s.render(batch, env, mesh);
        }
    }

    /**
     * Detects if an attack hits any student in the crowd (campus peace violation check).
     */
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
