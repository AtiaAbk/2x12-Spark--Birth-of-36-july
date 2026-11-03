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
    @DisplayName("Level 1: striking an unarmed student returns a disqualification")
    public void testLevel1StrikeOnStudentDisqualifies() {
        Level1PeacefulCampus level = new Level1PeacefulCampus();
        assertTrue(level.isCombatEnabled(), "Level 1 combat must be enabled for self-defense and peace rule enforcement");
        level.setCrowdForTest(crowd);
        String v = level.checkCivilianStrike(PLAYER, 0f);
        assertNotNull(v, "hitting a student in Level 1 must disqualify");
        assertTrue(v.startsWith("VIOLATION"));
        assertNull(level.checkCivilianStrike(PLAYER, 180f), "swinging away from the student is not a violation");
    }

    @Test
    @DisplayName("Level 1: 5 campus archives must be explored before aggressors, and subduing them completes the mission")
    public void testLevel1ExplorationAndCombatProgression() {
        Level1PeacefulCampus level = new Level1PeacefulCampus();
        assertEquals(6, level.getTotalObjectiveCount());
        assertFalse(level.isCombatPhase(), "combat phase begins only after the 5 archives are explored");
        assertFalse(level.isLevelComplete(), "level is not complete at start");

        // Complete all objectives via test hook
        level.completeAllObjectivesForTest();
        assertTrue(level.isCombatPhase(), "combat phase must activate");
        assertTrue(level.allAggressorsSubdued(), "all aggressors must be knocked out");
        assertTrue(level.isLevelComplete(), "level must complete after subduing helmeted attackers");
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

    @Test
    @DisplayName("Promenade crowd seeding: walkers are initialized on main avenue in active walking state")
    public void testPromenadeCrowdSeedingAndMovement() {
        CampusCrowd promenadeCrowd = new CampusCrowd(12, COLLIDERS);
        StudentNPC[] students = promenadeCrowd.getStudents();
        assertTrue(students.length >= 6);

        // Check the first 6 seeded promenade students
        int walkingCount = 0;
        for (int i = 0; i < 6; i++) {
            StudentNPC s = students[i];
            assertTrue(s.getPosition().z >= 30f && s.getPosition().z <= 70f,
                "Student " + i + " must be positioned on the main promenade avenue");
            if (s.getState() == bd.spark36.character.NpcBehaviorState.WALKING) {
                walkingCount++;
            }
        }
        assertTrue(walkingCount >= 4, "Most promenade students must be actively walking on frame 1");
    }

    @Test
    @DisplayName("Level 1 combat encounter: aggressors spawn directly in front of the player, ready to attack")
    public void testCombatEncounterSpawnsInFrontOfPlayer() {
        Level1PeacefulCampus level = new Level1PeacefulCampus();
        bd.spark36.character.PlayerController player = new bd.spark36.character.PlayerController();
        player.setPosition(0f, 0f, 72f);
        player.setHeadingDegrees(180f); // Facing north (towards negative Z)

        level.triggerCombatEncounter(player, null);
        assertTrue(level.isCombatPhase(), "Combat phase must be active");

        com.badlogic.gdx.utils.Array<bd.spark36.character.HelmetGoon> aggressors = level.getAggressors();
        assertEquals(3, aggressors.size, "Must spawn 3 helmeted aggressors");

        for (int i = 0; i < aggressors.size; i++) {
            bd.spark36.character.HelmetGoon g = aggressors.get(i);
            assertTrue(g.getPosition().z < player.getPosition().z,
                "Aggressor " + i + " must spawn in front of player (z < 72)");
            assertEquals(bd.spark36.character.HelmetGoon.State.APPROACHING, g.getState(),
                "Aggressors must immediately advance on player in APPROACHING state");
        }
    }
}
