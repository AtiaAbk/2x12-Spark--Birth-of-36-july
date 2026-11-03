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
}
