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
        // Curzon Hall Main Central Block
        {-19f, 0f, -40.5f, 19f, 14f, -23.5f},
        // Curzon Portico
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

        // Verify that consecutive nodes in the path share a valid edge
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
        // Coordinate right near Raju Memorial Sculpture (42, 31)
        int nearest = graph.getNearestNode(41.5f, 30.8f);
        assertEquals(CampusWaypointGraph.NODE_RAJU_SCULPTURE, nearest);

        // Coordinate near Main Gate (0, 72)
        nearest = graph.getNearestNode(0.5f, 71.5f);
        assertEquals(CampusWaypointGraph.NODE_MAIN_GATE, nearest);
    }

    @Test
    @DisplayName("StudentNPC: AI State transitions and movement velocity update correctly")
    public void testStudentNpcStateTransitions() {
        StudentNPC student = new StudentNPC(101, 0, 1, 1.35f, "DU Student");

        assertEquals(NpcBehaviorState.IDLE, student.getState());
        assertTrue(student.isInteractable());

        // Transition to walking
        student.transitionTo(NpcBehaviorState.WALKING, 10f);
        assertEquals(NpcBehaviorState.WALKING, student.getState());
        assertTrue(student.getState().isMoving());

        // Update movement
        student.enterEdge(graph, 0, 0); // Edge 0: {0, 1}
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

        Vector3 playerPos = new Vector3(10f, 0f, 22f); // Player is directly north (+Z)

        student.startPlayerInteraction(1);

        assertEquals(NpcBehaviorState.INTERACTING_PLAYER, student.getState());
        assertNotNull(student.getActiveDialogue(), "Interacting student must have an active dialogue line");
        assertNotNull(student.getActiveDialogue().getTextBengali(), "Student dialogue must contain authentic Bengali text");
        assertNotNull(student.getActiveDialogue().getTextEnglish(), "Student dialogue must contain English subtitle");

        // Update interaction frame: student turns toward player (+Z -> heading 0)
        student.update(0.1f, graph, playerPos, 0.1f);
        assertEquals(0f, student.getHeading(), 2.0f, "Student must turn to face player during interaction");

        // End interaction
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

            // Must not be inside solid buildings
            float[] hit = graph.insideSolid(pos.x, pos.z, CampusWaypointGraph.BODY_RADIUS);
            assertNull(hit, "Student spawned inside solid collider at (" + pos.x + ", " + pos.z + ")");
        }
    }

    @Test
    @DisplayName("CampusCrowd: Level profiles configure crowd behavior appropriately")
    public void testLevelProfiles() {
        // Level 1: Peaceful Academic Life
        crowd.setLevelProfile(1);
        int peacefulCount = 0;
        for (StudentNPC s : crowd.getStudents()) {
            if (s.getState() == NpcBehaviorState.WALKING || s.getState() == NpcBehaviorState.IDLE ||
                s.getState() == NpcBehaviorState.LOOKING_AROUND) {
                peacefulCount++;
            }
        }
        assertTrue(peacefulCount > 12, "Level 1 must feature overwhelmingly peaceful strolling & observing students");

        // Level 5: Mass Mobilization
        crowd.setLevelProfile(5);
        int mobilizationCount = 0;
        for (StudentNPC s : crowd.getStudents()) {
            if (s.getState() == NpcBehaviorState.CHANTING || s.getState() == NpcBehaviorState.GROUPING) {
                mobilizationCount++;
            }
        }
        assertTrue(mobilizationCount >= 6, "Level 5 must feature chanting and grouping protest students");

        // Level 3: Police Escalation
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

        Vector3 playerPos = new Vector3(0f, 0f, 11f); // 1 meter away from student 0

        StudentNPC nearest = crowd.getClosestInteractable(playerPos, 2.5f);
        assertNotNull(nearest);
        assertEquals(students[0].getId(), nearest.getId());

        // Out of range query
        Vector3 farPos = new Vector3(100f, 0f, 100f);
        assertNull(crowd.getClosestInteractable(farPos, 2.5f));
    }

    @Test
    @DisplayName("CampusCrowd: Local avoidance applies separation repulsion between nearby agents")
    public void testCrowdAvoidanceSeparation() {
        StudentNPC[] students = crowd.getStudents();
        // Place two students very close together (0.4m apart)
        students[0].setPosition(0f, 0f, 0f);
        students[1].setPosition(0.3f, 0f, 0f);

        float initialDist = students[0].getPosition().dst(students[1].getPosition());

        // Run crowd update
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
        students[0].setPosition(0f, 0f, 2.0f); // In front of player facing north

        Vector3 playerPos = new Vector3(0f, 0f, 0f);
        float headingNorth = 0f; // facing +Z

        boolean hit = crowd.checkHitByPlayer(playerPos, headingNorth, 2.5f);
        assertTrue(hit, "Attack facing toward student within range must register hit");

        boolean hitOpposite = crowd.checkHitByPlayer(playerPos, 180f, 2.5f); // facing away (-Z)
        assertFalse(hitOpposite, "Attack facing away from student must not register hit");
    }
}
