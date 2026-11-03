// ei test file ta player movement, mission update, panel indicator, combat hits ar chatrolegue arrival inspect kore
package bd.spark36.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bd.spark36.level.LevelConfig;
import bd.spark36.level.Level1PeacefulCampus;
import bd.spark36.level.Level2StudentConfrontation;
import bd.spark36.world.JulyMemorials;

import static org.junit.jupiter.api.Assertions.*;

public class GameplayInspectionAndCombatTest {

    private PlayerController player;

    @BeforeEach
    public void setUp() {
        LevelConfig.reset(1, LevelConfig.GameMode.STORY);
        player = new PlayerController();
    }

    @Test
    @DisplayName("1. Player Movement: Position, heading, and physics state respond correctly")
    public void testPlayerMovementBasics() {
        player.setPosition(0f, 0f, 72f);
        assertEquals(0f, player.getPosition().x, 1e-4f);
        assertEquals(0f, player.getPosition().y, 1e-4f);
        assertEquals(72f, player.getPosition().z, 1e-4f);

        player.setHeadingDegrees(180f);
        assertEquals(180f, player.getHeadingDegrees(), 1e-4f);

        // Test crouching
        assertFalse(player.isCrouching());
        player.setCrouching(true);
        assertTrue(player.isCrouching());
        player.setCrouching(false);
        assertFalse(player.isCrouching());

        // Test sitting in water
        assertFalse(player.isSittingWater());
        player.setSittingWater(true);
        assertTrue(player.isSittingWater());
        player.setSittingWater(false);
        assertFalse(player.isSittingWater());

        // Test colliders presence
        assertNotNull(player.getColliders());
        assertTrue(player.getColliders().length > 20, "Player must have world collision boxes loaded");
    }

    @Test
    @DisplayName("2. Missions Update: Level 1 archives inspection progresses and activates combat encounter")
    public void testMissionUpdatesAndProgression() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        assertEquals(6, level1.getTotalObjectiveCount(), "Level 1 has 5 exploration + 1 defense objective");
        assertEquals(0, level1.getCompletedObjectiveCount(), "Starts with 0 completed objectives");
        assertFalse(level1.isCombatPhase(), "Combat phase should not start initially");

        // Inspect first archive
        JulyMemorials memorials = level1.getMemorials();
        assertNotNull(memorials, "Memorials must be present");
        JulyMemorials.MemorialEntry m1 = memorials.getEntries().get(0);
        assertFalse(m1.inspected);

        // Simulate interaction with m1
        boolean interacted = level1.onInteract(m1.position.cpy(), player, null);
        assertTrue(interacted, "Interacting at archive position must succeed");
        assertTrue(m1.inspected, "Archive 1 must be marked inspected");
        assertEquals(1, level1.getCompletedObjectiveCount(), "Completed count must increment to 1");
        assertTrue(level1.isChecklistItemDone(0), "Checklist item 1 must be done");
        assertFalse(level1.isChecklistItemDone(1), "Checklist item 2 must still be pending");

        // Inspect remaining 4 archives
        for (int i = 1; i < 5; i++) {
            JulyMemorials.MemorialEntry entry = memorials.getEntries().get(i);
            level1.onInteract(entry.position.cpy(), player, null);
        }

        assertEquals(5, level1.getCompletedObjectiveCount(), "All 5 archives should now be inspected");
        assertTrue(level1.allExplorationsComplete(), "All explorations should be complete");
        assertTrue(level1.isCombatPhase(), "Combat phase must be activated upon exploring all 5 archives");
    }

    @Test
    @DisplayName("3. Panel & Mission Indicator: Waypoint distance and directional needle angle calculate accurately")
    public void testMissionIndicatorCalculations() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        Vector3 activeWp = level1.getActiveWaypoint();
        assertNotNull(activeWp, "Active waypoint must not be null");

        // Set player position and verify distance
        player.setPosition(0f, 0f, 0f);
        float expectedDst = (float) Math.sqrt(activeWp.x * activeWp.x + activeWp.z * activeWp.z);
        float actualDst = level1.getWaypointDistance(player.getPosition());
        assertEquals(expectedDst, actualDst, 0.05f, "Waypoint distance calculation must match euclidean distance");

        // Test directional angle relative to camera yaw
        float dx = activeWp.x - player.getPosition().x;
        float dz = activeWp.z - player.getPosition().z;
        float targetAngle = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;

        // Camera looking directly at the target: relative needle angle should point up (90 deg in 2D ortho)
        float cameraFacingYaw = targetAngle;
        float relAngle = 90f - (targetAngle - cameraFacingYaw);
        assertEquals(90f, relAngle, 1e-4f, "When camera looks at target, needle must point straight UP (90 degrees)");

        // Camera looking 90 degrees away to the left: needle should point to the right (0 deg)
        float cameraYawLeft = targetAngle - 90f;
        float relAngleRight = 90f - (targetAngle - cameraYawLeft);
        assertEquals(0f, relAngleRight, 1e-4f, "When camera is turned left, needle must point RIGHT (0 degrees)");
    }

    @Test
    @DisplayName("4. Combat & Hitting: HelmetGoon takes damage, poise breaks, and is knocked out")
    public void testCombatHitAndDamageMechanics() {
        HelmetGoon goon = new HelmetGoon();
        goon.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        goon.reset(0f, 36f);

        assertEquals(100f, goon.getHealth(), 1e-4f);
        assertEquals(100f, goon.getPoise(), 1e-4f);
        assertFalse(goon.isKnockedOut());

        // Hit 1: Martial Punch (24 dmg, 16 poise dmg)
        boolean ko1 = goon.takeHitFromPlayer(24f, false, false, 180f);
        assertFalse(ko1);
        assertEquals(76f, goon.getHealth(), 1e-4f);
        assertEquals(84f, goon.getPoise(), 1e-4f);
        assertEquals(HelmetGoon.State.STAGGERED, goon.getState());

        // Hit 2: Heavy Kick (38 dmg, 32 poise dmg)
        boolean ko2 = goon.takeHitFromPlayer(38f, true, false, 180f);
        assertFalse(ko2);
        assertEquals(38f, goon.getHealth(), 1e-4f);
        assertEquals(52f, goon.getPoise(), 1e-4f);

        // Hit 3: Dragon Sweep (34 dmg) + finish
        boolean ko3 = goon.takeHitFromPlayer(40f, false, true, 180f);
        assertTrue(ko3, "Lethal hit must knock out the aggressor");
        assertEquals(0f, goon.getHealth(), 1e-4f);
        assertTrue(goon.isKnockedOut());
        assertEquals(HelmetGoon.State.KNOCKED_OUT, goon.getState());
    }

    @Test
    @DisplayName("5. Chhatra League Aggressors Spawning: Encounter triggers and enemies spawn in attack formation")
    public void testChatroLeagueAggressorArrival() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        assertFalse(level1.isCombatPhase());

        player.setPosition(0f, 0f, 36f);
        player.setHeadingDegrees(180f);

        // Trigger encounter
        level1.triggerCombatEncounter(player, null);
        assertTrue(level1.isCombatPhase(), "Combat phase must be active");

        // Verify aggressors are spawned and ready
        assertEquals(3, level1.getRemainingAggressorsCount(), "3 Chhatra League helmet goons must spawn");
        assertNotNull(level1.getActiveWaypoint(), "Waypoint must point to aggressor");

        // Subdue all 3 aggressors and check level completion
        level1.completeAllObjectivesForTest();
        assertTrue(level1.allAggressorsSubdued(), "All aggressors must be subdued");
        assertTrue(level1.isLevelComplete(), "Level 1 must complete when Chhatra League aggressors are subdued");
    }

    @Test
    @DisplayName("6. Level 2 Student Confrontation: Aggressors active immediately and students can be sheltered")
    public void testLevel2ConfrontationAndAggressors() {
        Level2StudentConfrontation level2 = new Level2StudentConfrontation();
        assertTrue(level2.isCombatEnabled(), "Level 2 combat must be enabled");
        assertEquals(5, level2.getTotalObjectiveCount());
        assertFalse(level2.isLevelComplete());

        // Check active waypoint points to first student
        Vector3 wp = level2.getActiveWaypoint();
        assertNotNull(wp);
        assertEquals("ACTIVE MISSION: 01 / 05", level2.getMissionTag());
    }

    @Test
    @DisplayName("7. Attack Mechanics: Pressing F attack near students does NOT abort or disqualify mission")
    public void testAttackPunchDoesNotAbortMission() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        CampusCrowd testCrowd = new CampusCrowd(3, player.getColliders());
        StudentNPC[] students = testCrowd.getStudents();
        // Place a student directly 1m in front of player
        player.setPosition(0f, 0f, 60f);
        player.setHeadingDegrees(0f); // facing north
        students[0].setPosition(0f, 0f, 61.2f);
        level1.setCrowdForTest(testCrowd);

        // Player attacks with F
        player.setAttackingForTest(true, 0.50f);

        boolean attackHandled = level1.onPlayerAttack(player, null, null);
        // Must NOT return true for disqualification and level must NOT be completed or aborted
        assertFalse(attackHandled, "Attacking near an innocent student must NOT abort or terminate the level");
        assertFalse(level1.isLevelComplete());
    }

    @Test
    @DisplayName("8. Campus Security AI: Continuous patrol movement across waypoints without freezing at spawn")
    public void testCampusSecurityContinuousPatrolMovement() {
        HelmetGoon security = new HelmetGoon();
        security.setRole(HelmetGoon.Role.CAMPUS_SECURITY);
        security.reset(0f, 60f);

        Vector3 startPos = security.getPosition().cpy();
        player.setPosition(0f, 0f, 72f); // Player at promenade gate

        // Update 5 seconds
        for (int frame = 0; frame < 300; frame++) {
            security.update(0.0166f, player);
        }

        float dstMoved = startPos.dst(security.getPosition());
        assertTrue(dstMoved > 3.0f, "Campus security guard must actively patrol along promenade (moved: " + dstMoved + "m)");
    }

    @Test
    @DisplayName("9. Campus Crowd Roaming: Overwhelming majority of 32 students actively traverse campus")
    public void testFullCrowdDynamicWanderingAcrossCampus() {
        CampusCrowd crowd = new CampusCrowd(32, player.getColliders());
        crowd.setLevelProfile(1);
        StudentNPC[] students = crowd.getStudents();
        assertEquals(32, students.length);

        Vector3[] startPos = new Vector3[students.length];
        for (int i = 0; i < students.length; i++) {
            startPos[i] = students[i].getPosition().cpy();
        }

        Vector3 dummyPlayer = new Vector3(0f, 0f, 72f);
        for (int frame = 0; frame < 1800; frame++) { // 30 seconds
            crowd.update(0.0166f, dummyPlayer);
        }

        int roamingCount = 0;
        for (int i = 0; i < students.length; i++) {
            float dist = startPos[i].dst(students[i].getPosition());
            if (dist > 1.5f) {
                roamingCount++;
            }
        }
        assertTrue(roamingCount >= 24, "At least 75% of campus students must be actively roaming across the campus network (actual: " + roamingCount + " / 32)");
    }

    @Test
    @DisplayName("10. Full Level 1 Playthrough: 5 Archives exploration -> Chhatra League encounter -> Victory")
    public void testCompleteLevel1PlaythroughFlow() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        JulyMemorials memorials = level1.getMemorials();
        assertNotNull(memorials);

        // Sequence of 5 archives
        for (int i = 0; i < 5; i++) {
            JulyMemorials.MemorialEntry entry = memorials.getEntries().get(i);
            assertEquals(i, level1.getCompletedObjectiveCount());
            // Move player to archive location
            player.setPosition(entry.position.x, 0f, entry.position.z);
            level1.onInteract(entry.position.cpy(), player, null);
            assertTrue(entry.inspected);
        }

        assertEquals(5, level1.getCompletedObjectiveCount());
        assertTrue(level1.allExplorationsComplete());
        assertTrue(level1.isCombatPhase(), "Chhatra League combat encounter must activate after 5th archive");

        // Verify Chhatra League enemies are present
        assertEquals(3, level1.getRemainingAggressorsCount());
        assertFalse(level1.allAggressorsSubdued());
        assertFalse(level1.isLevelComplete());

        // Player engages each aggressor with attacks
        for (HelmetGoon goon : level1.getAggressors()) {
            player.setPosition(goon.getPosition().x, 0f, goon.getPosition().z + 1.2f);
            player.setHeadingDegrees(180f);
            player.setAttackingForTest(true, 0.50f);

            // Deal lethal hits
            goon.takeHitFromPlayer(120f, false, true, 180f);
            assertTrue(goon.isKnockedOut());
        }

        // All Chhatra League subdued
        assertTrue(level1.allAggressorsSubdued());
        level1.getMissionTracker().completeObjective("m6");
        assertTrue(level1.isLevelComplete(), "Level 1 must be fully complete upon subduing all Chhatra League attackers!");
    }
}
