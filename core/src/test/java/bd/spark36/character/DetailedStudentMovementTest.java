package bd.spark36.character;

import org.junit.jupiter.api.Test;
import com.badlogic.gdx.math.Vector3;
import static org.junit.jupiter.api.Assertions.*;

public class DetailedStudentMovementTest {

    @Test
    public void testStudentCrowdMovementSimulation() {
        PlayerController player = new PlayerController();
        CampusCrowd crowd = new CampusCrowd(24, player.getColliders());
        crowd.setLevelProfile(1);

        StudentNPC[] students = crowd.getStudents();
        assertEquals(24, students.length);

        Vector3 playerPos = new Vector3(0f, 0f, 72f);
        Vector3[] startPositions = new Vector3[students.length];
        for (int i = 0; i < students.length; i++) {
            startPositions[i] = students[i].getPosition().cpy();
            System.out.printf("Start S%02d: State=%s, Pos=(%.2f, %.2f), Speed=%.2f%n",
                i, students[i].getState(), students[i].getPosition().x, students[i].getPosition().z,
                students[i].getSpeed());
        }

        // Simulate 60 seconds of game loop at 60 FPS
        float delta = 0.016666f;
        int zeroMovementCount = 0;
        float[] distances = new float[students.length];

        for (int frame = 0; frame < 3600; frame++) {
            crowd.update(delta, playerPos);
        }

        System.out.println("\n--- Positions after 60 seconds ---");
        for (int i = 0; i < students.length; i++) {
            float dst = startPositions[i].dst(students[i].getPosition());
            distances[i] = dst;
            System.out.printf("After S%02d: State=%s, Pos=(%.2f, %.2f), Speed=%.2f, DistMoved=%.2f%n",
                i, students[i].getState(), students[i].getPosition().x, students[i].getPosition().z,
                students[i].getSpeed(), dst);
            if (dst < 0.5f) {
                zeroMovementCount++;
            }
        }
        System.out.println("Zero movement count (<0.5m after 60s): " + zeroMovementCount + " / 24");
    }
}
