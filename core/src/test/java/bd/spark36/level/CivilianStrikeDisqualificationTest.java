package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.StudentNPC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QA: striking an unarmed student / civilian disqualifies the player in Levels 2 and 3.
 * Uses a headless CampusCrowd with one student placed directly in front of the player.
 */
public class CivilianStrikeDisqualificationTest {

    private static final float[][] COLLIDERS = { {-19f, 0f, -40.5f, 19f, 14f, -23.5f} };
    private static final Vector3 PLAYER = new Vector3(0f, 0f, 60f);

    private CampusCrowd crowd;

    @BeforeEach
    public void setUp() {
        LevelConfig.reset(2, LevelConfig.GameMode.STORY);
        crowd = new CampusCrowd(3, COLLIDERS);
        StudentNPC[] students = crowd.getStudents();
        for (int i = 0; i < students.length; i++) {
            students[i].getPosition().set(200f + i * 10f, 0f, 200f); // far away
        }
        // One unarmed student 1.5 m in front of the player (heading 0 faces +Z)
        students[0].getPosition().set(PLAYER.x, 0f, PLAYER.z + 1.5f);
    }

    @Test
    @DisplayName("Level 2: striking an unarmed student returns a disqualification")
    public void testLevel2StrikeOnStudentDisqualifies() {
        Level2StudentConfrontation level = new Level2StudentConfrontation();
        level.setCrowdForTest(crowd);
        String v = level.checkCivilianStrike(PLAYER, 0f);
        assertNotNull(v, "hitting a student must disqualify");
        assertTrue(v.startsWith("VIOLATION"));
        assertNull(level.checkCivilianStrike(PLAYER, 180f), "swinging away from the student is not a violation");
    }

    @Test
    @DisplayName("Level 3: striking an unarmed civilian returns a disqualification; combat stays disabled")
    public void testLevel3StrikeOnCivilianDisqualifies() {
        Level3PoliceEscalation level = new Level3PoliceEscalation();
        assertFalse(level.isCombatEnabled(), "Level 3 is humanitarian aid and evasion only");
        level.setCrowdForTest(crowd);
        String v = level.checkCivilianStrike(PLAYER, 0f);
        assertNotNull(v, "hitting a civilian must disqualify");
        assertTrue(v.startsWith("VIOLATION"));
        assertNull(level.checkCivilianStrike(PLAYER, 180f));
    }

    @Test
    @DisplayName("Strike rule: out-of-range or empty crowd is never a violation")
    public void testRuleEdgeCases() {
        assertNull(CivilianStrikeRule.check(null, PLAYER, 0f, CivilianStrikeRule.STUDENT_VIOLATION));
        Vector3 far = new Vector3(PLAYER.x, 0f, PLAYER.z - 10f);
        assertNull(CivilianStrikeRule.check(crowd, far, 0f, CivilianStrikeRule.STUDENT_VIOLATION));
    }
}
