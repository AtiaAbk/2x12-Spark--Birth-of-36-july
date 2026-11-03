package scratch;

import bd.spark36.character.CampusCrowd;
import bd.spark36.character.CampusWaypointGraph;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentNPC;
import com.badlogic.gdx.math.Vector3;

public class CrowdDiagnostics {
    public static void main(String[] args) {
        PlayerController pc = new PlayerController();
        CampusCrowd crowd = new CampusCrowd(24, pc.getColliders());
        crowd.setLevelProfile(1);
        System.out.println("Usable edges: " + crowd.usableEdgeCount() + " / " + CampusWaypointGraph.EDGES.length);
        System.out.println("Blocked: " + crowd.blockedEdges());

        Vector3 playerPos = new Vector3(0, 0, 72);
        StudentNPC[] students = crowd.getStudents();
        float[] initialDist = new float[students.length];
        for (int i = 0; i < students.length; i++) {
            System.out.printf("S%02d: State=%s, Pos=(%.2f, %.2f), Speed=%.2f%n",
                i, students[i].getState(), students[i].getPosition().x, students[i].getPosition().z, students[i].getSpeed());
        }

        // Simulate 10 seconds of updates
        for (int step = 0; step < 600; step++) {
            crowd.update(0.016f, playerPos);
        }

        System.out.println("\n--- After 10s ---");
        for (int i = 0; i < students.length; i++) {
            System.out.printf("S%02d: State=%s, Pos=(%.2f, %.2f), Speed=%.2f%n",
                i, students[i].getState(), students[i].getPosition().x, students[i].getPosition().z, students[i].getSpeed());
        }
    }
}
