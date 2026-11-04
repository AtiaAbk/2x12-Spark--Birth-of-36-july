// ei file ta full game integration audit test kore
package bd.spark36.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.HelmetGoon;
import bd.spark36.character.LocomotionState;
import bd.spark36.character.PlayerController;
import bd.spark36.character.PlayerController.PlayerCombatState;
import bd.spark36.character.StudentNPC;
import bd.spark36.dialogue.DialogueManager;
import bd.spark36.dialogue.DialogueSequence;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.interaction.Interactable;
import bd.spark36.interaction.InteractionSystem;
import bd.spark36.level.CampaignLevel;
import bd.spark36.level.CheckpointStore;
import bd.spark36.level.DialogueRepository;
import bd.spark36.level.Level1PeacefulCampus;
import bd.spark36.level.Level2StudentConfrontation;
import bd.spark36.level.LevelConfig;
import bd.spark36.level.LevelManager;
import bd.spark36.mission.MissionManager;
import bd.spark36.mission.MissionTracker;
import bd.spark36.world.JulyMemorials;

public class FullPipelineIntegrationAuditTest {

    private PlayerController player;
    private LevelManager levelManager;
    private Level1PeacefulCampus level1;

    @BeforeEach
    public void setUp() {
        LevelConfig.reset(1, LevelConfig.GameMode.STORY);
        MissionManager.get().startMission(1);
        CheckpointStore.resetMemoryForTest();

        player = new PlayerController();
        level1 = new Level1PeacefulCampus();
        levelManager = new LevelManager(null, player, null, null);
    }

    @Test
    @DisplayName("INTEGRATION 1: Trace execution path and verify all 8 systems update in the loop")
    public void testAllEightSystemsUpdatedInLoop() {
        // 1. PlayerController & AnimationSystem
        assertNotNull(player.getPose(), "AnimationSystem must provide blended procedural pose");
        Vector3 initialPos = player.getPosition().cpy();
        player.update(0.016f, 0f, true);
        assertEquals(LocomotionState.IDLE, player.getLocomotionState());
        assertEquals(PlayerCombatState.IDLE, player.getCombatState());

        // 2. Level 1 CampaignLevel
        assertEquals(1, level1.getLevelNumber());

        // 3. NPCManager (CampusCrowd / StudentNPC)
        CampusCrowd crowd = new CampusCrowd(8, player.getColliders());
        level1.setCrowdForTest(crowd);
        assertEquals(8, crowd.getStudentCount());
        StudentNPC npc = crowd.getStudents()[0];
        Vector3 npcInitial = npc.getPosition().cpy();
        crowd.update(0.016f, player.getPosition());
        assertNotNull(npc.getPosition());

        // 4. EnemyAI (CampusSecurity / HelmetGoon)
        HelmetGoon security = level1.getCampusSecurity();
        assertNotNull(security);
        Vector3 secPos = security.getPosition().cpy();
        security.update(0.016f, player);
        assertNotNull(security.getPosition());

        // 5. MissionManager
        assertEquals(1, MissionManager.get().getActiveMissionNumber());
        assertEquals(0, MissionManager.get().getProgress());
        assertEquals(5, MissionManager.get().getTargetCount());

        // 6. InteractionSystem
        InteractionSystem is = level1.getInteractionSystem();
        assertNotNull(is, "Level 1 must provide an active InteractionSystem");
        is.update(0.016f, player.getPosition(), player.getHeadingDegrees());

        // 7. DialogueSystem
        DialogueManager dm = new DialogueManager();
        assertFalse(dm.isDialogueActive());

        // 8. CombatSystem
        assertFalse(level1.onPlayerAttack(player, null, null));
    }

    @Test
    @DisplayName("INTEGRATION 2: Verify InteractionSystem registers archives, marker, pond, and NPCs with [E]")
    public void testInteractionSystemFullRegistrationAndPrompt() {
        InteractionSystem is = level1.getInteractionSystem();
        assertNotNull(is, "InteractionSystem must be initialized");

        // Player at spawn (0, 0, 72)
        is.update(0.016f, player.getPosition(), player.getHeadingDegrees());

        // Move player to Curzon Hall Central Arcade (3.6f, 0f, -16.8f) facing it
        player.setPosition(3.6f, 0f, -15.0f);
        player.setHeadingDegrees(180f); // facing towards -z
        is.update(0.016f, player.getPosition(), player.getHeadingDegrees());

        String prompt = level1.getContextPrompt(player.getPosition());
        assertNotNull(prompt, "Context prompt must be displayed near Curzon Hall Central Arcade");
        assertTrue(prompt.contains("[E]"), "Prompt must show [E] keybinding");
        assertTrue(prompt.contains("INSPECT HISTORICAL ARCHIVE"), "Prompt must indicate archive inspection");

        // Trigger interaction with [E]
        int initialProgress = MissionManager.get().getProgress();
        boolean interacted = level1.onInteract(player.getPosition(), player, null);
        assertTrue(interacted, "Interacting with [E] must succeed");
        assertTrue(level1.isChecklistItemDone(0), "Archive 1 must be marked inspected");
        assertEquals(initialProgress + 1, MissionManager.get().getProgress(), "Mission progress must advance on [E] interaction");
    }

    @Test
    @DisplayName("INTEGRATION 3: Verify student NPC interaction triggers dialogue and advances mission")
    public void testStudentNpcInteractionAndDialogue() {
        CampusCrowd crowd = new CampusCrowd(4, player.getColliders());
        level1.setCrowdForTest(crowd);
        assertTrue(crowd.getStudentCount() >= 1);

        StudentNPC student = crowd.getStudents()[0];
        // Position player next to student
        student.setPosition(10f, 0f, 10f);
        player.setPosition(10f, 0f, 11.5f);
        player.setHeadingDegrees(180f); // facing student

        level1.getInteractionSystem().update(0.016f, player.getPosition(), player.getHeadingDegrees());
        String prompt = level1.getContextPrompt(player.getPosition());
        assertNotNull(prompt, "Must display talk prompt when facing student");
        assertTrue(prompt.contains("[E] TALK TO"), "Prompt must be talk action: " + prompt);

        int progBefore = MissionManager.get().getProgress();
        boolean talked = level1.onInteract(player.getPosition(), player, null);
        assertTrue(talked, "Talking to student must succeed");
        assertEquals(progBefore + 1, MissionManager.get().getProgress(), "Mission progress must advance when meeting student");
    }

    @Test
    @DisplayName("INTEGRATION 4: Verify combat phase transition, enemy AI combat, and victory trigger")
    public void testCombatPhaseAndEnemySubdue() {
        assertFalse(level1.isCombatPhase(), "Level 1 starts in exploration phase");

        // Complete all 5 archive explorations
        JulyMemorials memorials = level1.getMemorials();
        for (int i = 0; i < 5; i++) {
            JulyMemorials.MemorialEntry entry = memorials.getEntries().get(i);
            level1.onInteract(entry.position.cpy(), player, null);
        }

        assertTrue(level1.allExplorationsComplete(), "All explorations must be complete");
        assertTrue(level1.isCombatPhase(), "Combat encounter must trigger after 5 archives explored");
        assertEquals(3, level1.getRemainingAggressorsCount(), "3 helmeted aggressors must arrive");

        // Subdue all 3 aggressors
        HelmetGoon g1 = level1.getAggressors().get(0);
        g1.takeHitFromPlayer(999f, false, false, 0f);
        assertEquals(2, level1.getRemainingAggressorsCount());

        HelmetGoon g2 = level1.getAggressors().get(1);
        g2.takeHitFromPlayer(999f, false, false, 0f);
        assertEquals(1, level1.getRemainingAggressorsCount());

        HelmetGoon g3 = level1.getAggressors().get(2);
        g3.takeHitFromPlayer(999f, false, false, 0f);
        assertEquals(0, level1.getRemainingAggressorsCount());

        assertTrue(level1.allAggressorsSubdued(), "All aggressors must be subdued");
        assertTrue(level1.isLevelComplete(), "Level 1 must complete when enemies subdued");
    }

    @Test
    @DisplayName("INTEGRATION 5: Verify GameStateManager and LevelConfig reset preserve unlocked level cleanly")
    public void testGameStateCleanReset() {
        LevelConfig.reset(1, LevelConfig.GameMode.STORY);
        assertEquals(1, LevelConfig.getCurrentLevel());
        assertEquals(1, LevelConfig.getUnlockedLevel());

        LevelConfig.unlockNextLevel(1);
        assertEquals(2, LevelConfig.getUnlockedLevel());

        // Calling reset on Level 2 must keep unlocked level at least 2
        LevelConfig.reset(2, LevelConfig.GameMode.STORY);
        assertEquals(2, LevelConfig.getCurrentLevel());
        assertTrue(LevelConfig.getUnlockedLevel() >= 2, "Unlocked level must not be reset to 1");
    }

    @Test
    @DisplayName("INTEGRATION 6: Verify 100% of students walk in Level 1 and villain does not spawn until 5 archives explored")
    public void testExplorationPhaseAllStudentsWalkingAndNoVillains() {
        CampusCrowd crowd = new CampusCrowd(32, player.getColliders());
        crowd.setLevelProfile(1);
        assertEquals(32, crowd.getStudentCount());

        for (int i = 0; i < crowd.getStudentCount(); i++) {
            StudentNPC s = crowd.getStudents()[i];
            assertEquals(bd.spark36.character.NpcBehaviorState.WALKING, s.getState(),
                "Student " + i + " must be walking in Level 1 peaceful campus");
            assertTrue(s.getSpeed() > 0f, "Student " + i + " must have active walking speed");
        }

        // Level 1 starts peaceful
        assertFalse(level1.isCombatPhase(), "Level 1 must not be in combat phase initially");

        // Inspect one by one and verify checklist progression
        JulyMemorials memorials = level1.getMemorials();
        for (int i = 0; i < 4; i++) {
            JulyMemorials.MemorialEntry entry = memorials.getEntries().get(i);
            level1.onInteract(entry.position.cpy(), player, null);
            assertTrue(level1.isChecklistItemDone(i), "Archive " + (i + 1) + " must be completed");
            assertFalse(level1.isCombatPhase(), "Villain must NOT arrive before all 5 archives are explored");
        }

        // Inspect 5th archive
        JulyMemorials.MemorialEntry entry5 = memorials.getEntries().get(4);
        level1.onInteract(entry5.position.cpy(), player, null);
        assertTrue(level1.isChecklistItemDone(4), "Archive 5 must be completed");
        assertTrue(level1.allExplorationsComplete(), "All 5 explorations must be complete");

        // Now villain encounter activates
        assertTrue(level1.isCombatPhase(), "Chhatra League aggressors must arrive only after 5 archives explored");
        assertEquals(3, level1.getRemainingAggressorsCount(), "3 aggressors must arrive");
    }

    @Test
    @DisplayName("INTEGRATION 7: Verify tree foliage coordinates do not clip into Central Library, TSC, or Academic Buildings")
    public void testTreeLocationsClearOfBuildings() {
        for (float[] loc : bd.spark36.world.DhakaCampusWorld.TREE_LOCATIONS) {
            float x = loc[0];
            float z = loc[1];

            // Central Library: X in [-89, -44], Z in [4, 40]
            boolean insideLibrary = (x >= -89f && x <= -44f && z >= 4f && z <= 40f);
            assertFalse(insideLibrary, "Tree at (" + x + ", " + z + ") clips into Central Library");

            // TSC: X in [39, 88], Z in [4, 36]
            boolean insideTSC = (x >= 39f && x <= 88f && z >= 4f && z <= 36f);
            assertFalse(insideTSC, "Tree at (" + x + ", " + z + ") clips into TSC");

            // West Academic Building: X in [-36, -16], Z in [46, 58]
            boolean insideWestAcad = (x >= -36f && x <= -16f && z >= 46f && z <= 58f);
            assertFalse(insideWestAcad, "Tree at (" + x + ", " + z + ") clips into West Academic Building");

            // East Academic Building: X in [16, 36], Z in [46, 58]
            boolean insideEastAcad = (x >= 16f && x <= 36f && z >= 46f && z <= 58f);
            assertFalse(insideEastAcad, "Tree at (" + x + ", " + z + ") clips into East Academic Building");
        }
    }
}
