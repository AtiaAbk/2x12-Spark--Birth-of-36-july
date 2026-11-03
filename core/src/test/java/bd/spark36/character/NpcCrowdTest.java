// ei file ta student npc crowd movement test kore
package bd.spark36.character;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import bd.spark36.level.DialogueRepository;

import static org.junit.jupiter.api.Assertions.*;

public class NpcCrowdTest {

    private CampusWaypointGraph graph;
    private CampusCrowd crowd;
    private static final float[][] SAMPLE_COLLIDERS = {

        {-19f, 0f, -40.5f, 19f, 14f, -23.5f},

        {-7.5f, 0f, -24.4f, 7.5f, 15.6f, -20.6f}
    };

    @BeforeEach
    public void setUp() {
        graph = new CampusWaypointGraph(SAMPLE_COLLIDERS);
        crowd = new CampusCrowd(16, SAMPLE_COLLIDERS);
    }

    @Test
    @DisplayName("CampusWaypointGraph: all 21 landmark nodes exist within campus boundaries")
    public void testWaypointNodesBounds() {
        assertEquals(21, graph.getNodeCount());
        for (int i = 0; i < graph.getNodeCount(); i++) {
            float x = CampusWaypointGraph.NODES[i][0];
            float z = CampusWaypointGraph.NODES[i][1];
            assertTrue(x >= -70f && x <= 70f, "Node " + i + " X coordinate " + x + " out of campus bounds");
            assertTrue(z >= -60f && z <= 90f, "Node " + i + " Z coordinate " + z + " out of campus bounds");
        }
    }

    @Test
    @DisplayName("CampusWaypointGraph: Dijkstra pathfinding finds shortest route from Main Gate to Curzon Steps")
    public void testDijkstraPathfinding() {
        int[] path = new int[CampusWaypointGraph.NODES.length];
        int count = graph.findPath(CampusWaypointGraph.NODE_MAIN_GATE, CampusWaypointGraph.NODE_CURZON_STEPS, path);

        assertTrue(count > 0, "Dijkstra must find a valid path between Main Gate and Curzon Steps");
        assertEquals(CampusWaypointGraph.NODE_CURZON_STEPS, path[count - 1], "Last node in path must be the destination");

        int prev = CampusWaypointGraph.NODE_MAIN_GATE;
        for (int i = 0; i < count; i++) {
            int curr = path[i];
            boolean edgeFound = false;
            for (int[] e : CampusWaypointGraph.EDGES) {
                if ((e[0] == prev && e[1] == curr) || (e[0] == curr && e[1] == prev)) {
                    edgeFound = true;
                    break;
                }
            }
            assertTrue(edgeFound, "Consecutive path nodes " + prev + " -> " + curr + " must be connected by an edge");
            prev = curr;
        }
    }

    @Test
    @DisplayName("CampusWaypointGraph: Nearest node query maps coordinates to correct landmark")
    public void testNearestNodeQuery() {

        int nearest = graph.getNearestNode(41.5f, 30.8f);
        assertEquals(CampusWaypointGraph.NODE_RAJU_SCULPTURE, nearest);

        nearest = graph.getNearestNode(0.5f, 71.5f);
        assertEquals(CampusWaypointGraph.NODE_MAIN_GATE, nearest);
    }

    @Test
    @DisplayName("StudentNPC: AI State transitions and movement velocity update correctly")
    public void testStudentNpcStateTransitions() {
        StudentNPC student = new StudentNPC(101, 0, 1, 1.35f, "DU Student");

        assertEquals(NpcBehaviorState.IDLE, student.getState());
        assertTrue(student.isInteractable());

        student.transitionTo(NpcBehaviorState.WALKING, 10f);
        assertEquals(NpcBehaviorState.WALKING, student.getState());
        assertTrue(student.getState().isMoving());

        student.enterEdge(graph, 0, 0);
        student.update(0.05f, graph, new Vector3(50f, 0f, 50f), 0.05f);

        assertTrue(student.getSpeed() > 0f, "Student speed must accelerate while walking");
        assertNotNull(student.getPose());
        assertFalse(Float.isNaN(student.getPose().pelvisHeight));
    }

    @Test
    @DisplayName("StudentNPC: Player interaction engages conversation, faces player, and assigns dialogue")
    public void testStudentPlayerInteraction() {
        StudentNPC student = new StudentNPC(42, 1, 2, 1.25f, "Law Student");
        student.setPosition(10f, 0f, 20f);
        student.setHeading(0f);

        Vector3 playerPos = new Vector3(10f, 0f, 22f);

        student.startPlayerInteraction(1);

        assertEquals(NpcBehaviorState.INTERACTING_PLAYER, student.getState());
        assertNotNull(student.getActiveDialogue(), "Interacting student must have an active dialogue line");
        assertNotNull(student.getActiveDialogue().getTextBengali(), "Student dialogue must contain authentic Bengali text");
        assertNotNull(student.getActiveDialogue().getTextEnglish(), "Student dialogue must contain English subtitle");

        student.update(0.1f, graph, playerPos, 0.1f);
        assertEquals(0f, student.getHeading(), 2.0f, "Student must turn to face player during interaction");

        student.endInteraction();
        assertNull(student.getActiveDialogue());
        assertNotEquals(NpcBehaviorState.INTERACTING_PLAYER, student.getState());
    }

    @Test
    @DisplayName("CampusCrowd: Population size and initial spawn positions are strictly valid")
    public void testCrowdPopulationAndSpawns() {
        assertEquals(16, crowd.getStudentCount());
        StudentNPC[] students = crowd.getStudents();
        assertNotNull(students);

        for (StudentNPC s : students) {
            Vector3 pos = s.getPosition();
            assertFalse(Float.isNaN(pos.x));
            assertFalse(Float.isNaN(pos.y));
            assertFalse(Float.isNaN(pos.z));

            float[] hit = graph.insideSolid(pos.x, pos.z, CampusWaypointGraph.BODY_RADIUS);
            assertNull(hit, "Student spawned inside solid collider at (" + pos.x + ", " + pos.z + ")");
        }
    }

    @Test
    @DisplayName("CampusCrowd: Level profiles configure crowd behavior appropriately")
    public void testLevelProfiles() {

        crowd.setLevelProfile(1);
        int peacefulCount = 0;
        for (StudentNPC s : crowd.getStudents()) {
            if (s.getState() == NpcBehaviorState.WALKING || s.getState() == NpcBehaviorState.IDLE ||
                s.getState() == NpcBehaviorState.LOOKING_AROUND) {
                peacefulCount++;
            }
        }
        assertTrue(peacefulCount > 12, "Level 1 must feature overwhelmingly peaceful strolling & observing students");

        crowd.setLevelProfile(5);
        int mobilizationCount = 0;
        for (StudentNPC s : crowd.getStudents()) {
            if (s.getState() == NpcBehaviorState.CHANTING || s.getState() == NpcBehaviorState.GROUPING) {
                mobilizationCount++;
            }
        }
        assertTrue(mobilizationCount >= 6, "Level 5 must feature chanting and grouping protest students");

        crowd.setLevelProfile(3);
        int alertCount = 0;
        for (StudentNPC s : crowd.getStudents()) {
            if (s.getState() == NpcBehaviorState.FLEEING || s.getState() == NpcBehaviorState.LOOKING_AROUND) {
                alertCount++;
            }
        }
        assertTrue(alertCount >= 4, "Level 3 must feature students reacting with caution or seeking safety");
    }

    @Test
    @DisplayName("CampusCrowd: Closest interactable query finds nearest student within range")
    public void testClosestInteractableQuery() {
        StudentNPC[] students = crowd.getStudents();
        students[0].setPosition(0f, 0f, 10f);
        students[0].transitionTo(NpcBehaviorState.IDLE, 5f);

        students[1].setPosition(20f, 0f, 20f);
        students[1].transitionTo(NpcBehaviorState.IDLE, 5f);

        Vector3 playerPos = new Vector3(0f, 0f, 11f);

        StudentNPC nearest = crowd.getClosestInteractable(playerPos, 2.5f);
        assertNotNull(nearest);
        assertEquals(students[0].getId(), nearest.getId());

        Vector3 farPos = new Vector3(100f, 0f, 100f);
        assertNull(crowd.getClosestInteractable(farPos, 2.5f));
    }

    @Test
    @DisplayName("CampusCrowd: Local avoidance applies separation repulsion between nearby agents")
    public void testCrowdAvoidanceSeparation() {
        StudentNPC[] students = crowd.getStudents();

        students[0].setPosition(0f, 0f, 0f);
        students[1].setPosition(0.3f, 0f, 0f);

        float initialDist = students[0].getPosition().dst(students[1].getPosition());

        for (int i = 0; i < 10; i++) {
            crowd.update(0.016f, new Vector3(100f, 0f, 100f));
        }

        float afterDist = students[0].getPosition().dst(students[1].getPosition());
        assertTrue(afterDist > initialDist, "Local avoidance must push overlapping students apart (initial: " +
            initialDist + ", after: " + afterDist + ")");
    }

    @Test
    @DisplayName("CampusCrowd: CheckHitByPlayer detects attacks against students")
    public void testCheckHitByPlayer() {
        StudentNPC[] students = crowd.getStudents();
        students[0].setPosition(0f, 0f, 2.0f);

        Vector3 playerPos = new Vector3(0f, 0f, 0f);
        float headingNorth = 0f;

        boolean hit = crowd.checkHitByPlayer(playerPos, headingNorth, 2.5f);
        assertTrue(hit, "Attack facing toward student within range must register hit");

        boolean hitOpposite = crowd.checkHitByPlayer(playerPos, 180f, 2.5f);
        assertFalse(hitOpposite, "Attack facing away from student must not register hit");
    }

    @Test
    @DisplayName("CampusCrowd: Full waypoint graph connectivity with real environment colliders")
    public void testCrowdWithRealPlayerColliders() {
        PlayerController pc = new PlayerController();
        CampusCrowd realCrowd = new CampusCrowd(16, pc.getColliders());
        assertEquals(CampusWaypointGraph.EDGES.length, realCrowd.usableEdgeCount(),
            "All campus waypoint network edges must be completely traversable and clear of obstacles");
        assertEquals("", realCrowd.blockedEdges(), "No waypoint edges should be blocked by colliders");
    }

    @Test
    @DisplayName("CampusCrowd: Continuous natural roaming and displacement over 30 seconds")
    public void testStudentLongTermMovement() {
        StudentNPC[] students = crowd.getStudents();
        Vector3[] initialPositions = new Vector3[students.length];
        for (int i = 0; i < students.length; i++) {
            initialPositions[i] = new Vector3(students[i].getPosition());
        }

        Vector3 dummyPlayer = new Vector3(0, 0, 0);
        float delta = 0.016f;
        for (int frame = 0; frame < 1800; frame++) {
            crowd.update(delta, dummyPlayer);
        }

        int movingCount = 0;
        for (int i = 0; i < students.length; i++) {
            float dst = initialPositions[i].dst(students[i].getPosition());
            if (dst > 2.0f) {
                movingCount++;
            }
        }
        assertTrue(movingCount >= 12, "Overwhelming majority of students must actively traverse campus (moved: " + movingCount + " / " + students.length + ")");
    }
}
