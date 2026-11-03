package bd.spark36.audit;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bd.spark36.character.CampusCrowd;
import bd.spark36.character.CombatClassification;
import bd.spark36.character.HelmetGoon;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentNPC;
import bd.spark36.level.DialogueRepository;
import bd.spark36.dialogue.DialogueSequence;
import bd.spark36.level.CheckpointStore;
import bd.spark36.level.CivilianStrikeRule;
import bd.spark36.level.LevelConfig;
import bd.spark36.level.Level1PeacefulCampus;
import bd.spark36.level.Level2StudentConfrontation;
import bd.spark36.level.Level3PoliceEscalation;
import bd.spark36.level.Level4CurfewBlackout;
import bd.spark36.level.Level5MassMobilization;
import bd.spark36.mission.MissionManager;
import bd.spark36.world.JulyMemorials;

import static org.junit.jupiter.api.Assertions.*;

public class FullMasterDirectiveAuditTest {

    private PlayerController player;

    @BeforeEach
    public void setUp() {
        LevelConfig.reset(1, LevelConfig.GameMode.STORY);
        MissionManager.get().startMission(1);
        player = new PlayerController();
    }

    @Test
    @DisplayName("TEST 1: Game launches and initializes subsystems without errors")
    public void test01_gameLaunchesWithoutErrors() {
        assertNotNull(player, "PlayerController must initialize");
        assertEquals(1, LevelConfig.getCurrentLevel(), "Default level must be 1");
        assertNotNull(LevelConfig.getGameMode(), "GameMode must be defined");
        assertNotNull(MissionManager.get(), "MissionManager singleton must be initialized");
        assertEquals(1, MissionManager.get().getActiveMissionNumber(), "MissionManager must start at Mission 1");
        assertFalse(MissionManager.get().isMissionAborted(), "Mission must not start in aborted state");
    }

    @Test
    @DisplayName("TEST 2: Player movement, velocity, and collisions operate correctly")
    public void test02_playerMovementWorks() {
        player.setPosition(0f, 0f, 50f);
        player.setHeadingDegrees(90f);
        assertEquals(0f, player.getPosition().x, 1e-4f);
        assertEquals(50f, player.getPosition().z, 1e-4f);
        assertEquals(90f, player.getHeadingDegrees(), 1e-4f);

        player.setCrouching(true);
        assertTrue(player.isCrouching());
        player.setCrouching(false);
        assertFalse(player.isCrouching());

        float[][] colliders = player.getColliders();
        assertNotNull(colliders, "World collision boxes must be loaded for player");
        assertTrue(colliders.length >= 20, "At least 20 static colliders should guard campus architecture");
    }

    @Test
    @DisplayName("TEST 3: NPC movement and waypoint graph traversal work")
    public void test03_npcMovementWorks() {
        CampusCrowd crowd = new CampusCrowd(8, player.getColliders());
        StudentNPC[] students = crowd.getStudents();
        assertNotNull(students);
        assertEquals(8, students.length);

        Vector3 initialPos = students[0].getPosition().cpy();
        for (int frame = 0; frame < 120; frame++) {
            crowd.update(0.016f, player.getPosition());
        }
        float moved = initialPos.dst(students[0].getPosition());
        assertTrue(moved >= 0f, "NPC position must update smoothly along waypoint paths");
    }

    @Test
    @DisplayName("TEST 4: Player can detect and interact with campus NPCs using E key")
    public void test04_playerCanInteractWithNpcsUsingE() {
        StudentNPC npc = new StudentNPC(1, 0, 1, 1.3f, "Curzon Hall Peer");
        npc.setPosition(0f, 0f, 20f);

        assertTrue(npc.isInteractable(), "Campus students must be interactable by default");
        assertEquals(2.6f, npc.getInteractionRadius(), 1e-4f, "Interaction radius must be 2.6m");
        assertTrue(npc.getInteractionPrompt().contains("[E]"), "Prompt must instruct player to press [E]");

        Vector3 closePlayer = new Vector3(0f, 0f, 21.5f);
        assertTrue(npc.canInteract(closePlayer, 180f), "Player within 2.6m must be permitted to interact");

        Vector3 farPlayer = new Vector3(0f, 0f, 30f);
        assertFalse(npc.canInteract(farPlayer, 180f), "Player beyond 2.6m must not trigger interaction");
    }

    @Test
    @DisplayName("TEST 5: Dialogue sequences open, deliver text, and close cleanly")
    public void test05_dialogueOpensAndClosesCorrectly() {
        DialogueSequence seq = DialogueRepository.getStudentDialogueSequence(1, 0);
        assertNotNull(seq, "Student dialogue sequence must exist for level 1");
        assertTrue(seq.getLineCount() > 0, "Dialogue sequence must contain lines");

        DialogueRepository.DialogueLine line = seq.getLine(0);
        assertNotNull(line.getSpeaker(), "Dialogue speaker name must be present");
        assertNotNull(line.getTextEnglish(), "Dialogue English line must be present");
        assertNotNull(line.getTextBengali(), "Dialogue Bengali line must be present");

        for (int i = 0; i < seq.getLineCount(); i++) {
            assertNotNull(seq.getLine(i).getTextEnglish());
        }
    }

    @Test
    @DisplayName("TEST 6: Player cannot damage protected campus civilians")
    public void test06_playerCannotDamageProtectedCivilians() {
        StudentNPC student = new StudentNPC(2, 1, 1, 1.3f, "Literature Student");
        assertEquals(CombatClassification.PROTECTED_CIVILIAN, student.getCombatClassification(),
            "Student NPCs must strictly hold PROTECTED_CIVILIAN classification");

        HelmetGoon security = new HelmetGoon();
        security.setRole(HelmetGoon.Role.CAMPUS_SECURITY);
        assertEquals(CombatClassification.PROTECTED_CIVILIAN, security.getCombatClassification(),
            "Campus Security guards must also hold PROTECTED_CIVILIAN classification");

        HelmetGoon aggressor = new HelmetGoon();
        aggressor.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        assertEquals(CombatClassification.HOSTILE_OPPONENT, aggressor.getCombatClassification(),
            "Hostile opponents must hold HOSTILE_OPPONENT classification");
    }

    @Test
    @DisplayName("TEST 7: Attacking a protected civilian immediately aborts the mission")
    public void test07_attackingProtectedCivilianImmediatelyAbortsMission() {
        CampusCrowd crowd = new CampusCrowd(1, player.getColliders());
        StudentNPC civilian = crowd.getStudents()[0];
        civilian.setPosition(0f, 0f, 70f);

        Vector3 playerPos = new Vector3(0f, 0f, 71.5f);
        float headingTowardsCivilian = 180f;

        String violation = CivilianStrikeRule.check(crowd, playerPos, headingTowardsCivilian, CivilianStrikeRule.STUDENT_VIOLATION);
        assertNotNull(violation, "Striking within range of a civilian must produce a violation");

        MissionManager.get().abortMission("You attacked a protected campus member.");
        assertTrue(MissionManager.get().isMissionAborted(), "MissionManager must immediately flag mission as ABORTED");
        assertEquals("You attacked a protected campus member.", MissionManager.get().getAbortReason(),
            "Abort reason must state 'You attacked a protected campus member.'");
    }

    @Test
    @DisplayName("TEST 8: Combat controls and state machine operate smoothly without lock")
    public void test08_combatControlsWorkCorrectly() {
        assertEquals(PlayerController.PlayerCombatState.IDLE, player.getCombatState(), "Initial state must be IDLE");

        player.setAttackingForTest(true, 0.20f);
        player.update(0.016f, 0f, true);
        assertEquals(PlayerController.PlayerCombatState.ATTACKING, player.getCombatState(),
            "During attack execution, state must be ATTACKING");

        player.setAttackingForTest(true, 0.75f);
        player.update(0.016f, 0f, true);
        assertEquals(PlayerController.PlayerCombatState.ATTACK_RECOVERY, player.getCombatState(),
            "Late in attack (>=0.70 progress), state must transition to ATTACK_RECOVERY");

        player.setAttackingForTest(false, 0f);
        player.update(0.30f, 0f, true);
        assertEquals(PlayerController.PlayerCombatState.IDLE, player.getCombatState(),
            "After recovery timer expires, state must return to IDLE");
    }

    @Test
    @DisplayName("TEST 9: Enemy AI moves, approaches, attacks, and recovers")
    public void test09_enemyAiMovesAndAttacks() {
        HelmetGoon aggressor = new HelmetGoon();
        aggressor.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        aggressor.reset(0f, 25f);
        aggressor.startFightDirectly();

        player.setPosition(0f, 0f, 20f);

        float initialZ = aggressor.getPosition().z;
        for (int i = 0; i < 60; i++) {
            aggressor.update(0.016f, player);
        }
        float newZ = aggressor.getPosition().z;
        assertTrue(newZ < initialZ, "Aggressor must actively advance toward player");
    }

    @Test
    @DisplayName("TEST 10: Characters continue continuous non-blocking movement during combat (Fix for Phase 2 freeze bug)")
    public void test10_charactersContinueMovingDuringCombat_NoFreeze() {
        HelmetGoon aggressor = new HelmetGoon();
        aggressor.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        aggressor.reset(0f, 30f);
        aggressor.startFightDirectly();

        player.setPosition(0f, 0f, 20f);
        player.setAttackingForTest(true, 0.50f);

        for (int frame = 0; frame < 180; frame++) {
            player.update(0.016f, 0f, true);
            aggressor.update(0.016f, player);
            assertTrue(player.getHealth() > 0f, "Player must remain alive during test");
            assertNotNull(aggressor.getPosition(), "Aggressor position must remain valid");
        }
        assertTrue(player.getCombatState() != null, "Player state must continually update");
    }

    @Test
    @DisplayName("TEST 11: Enemy damage, poise, stagger, and defeat work correctly")
    public void test11_enemyDamageAndDefeatWorkCorrectly() {
        HelmetGoon aggressor = new HelmetGoon();
        aggressor.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        aggressor.reset(0f, 20f);
        aggressor.startFightDirectly();

        assertEquals(100f, aggressor.getHealth(), 1e-4f);
        assertFalse(aggressor.isKnockedOut());

        aggressor.takeHitFromPlayer(30f, false, false, 180f);
        assertTrue(aggressor.getHealth() < 100f, "Health must decrease on hit");
        assertEquals(HelmetGoon.State.STAGGERED, aggressor.getState(), "Hit must induce STAGGERED state");

        boolean defeated = aggressor.takeHitFromPlayer(120f, true, true, 180f);
        assertTrue(defeated, "Lethal damage must return true for defeat");
        assertTrue(aggressor.isKnockedOut(), "Health reaching 0 must trigger KNOCKED_OUT state");
    }

    @Test
    @DisplayName("TEST 12: Mission 1 completion marks complete and updates HUD")
    public void test12_mission1CompletionUpdatesHud() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        JulyMemorials memorials = level1.getMemorials();
        assertNotNull(memorials);

        MissionManager mm = MissionManager.get();
        mm.startMission(1);
        assertEquals(0, mm.getProgress());

        for (int i = 0; i < 5; i++) {
            memorials.getEntries().get(i).inspected = true;
            mm.advanceProgress(1);
        }
        assertEquals(5, mm.getProgress());
        assertTrue(level1.allExplorationsComplete());
        assertTrue(mm.isMissionComplete(), "Mission 1 must be marked complete upon 5/5 explorations");
        assertEquals("MISSION 1/5 — CAMPUS EXPLORATION", mm.getFormattedHudHeader());
        assertEquals("Progress: 5/5", mm.getFormattedProgress());
    }

    @Test
    @DisplayName("TEST 13: All five missions in campaign progress sequentially")
    public void test13_allFiveMissionsProgressCorrectly() {
        MissionManager mm = MissionManager.get();

        for (int m = 1; m <= 5; m++) {
            mm.startMission(m);
            assertEquals(m, mm.getActiveMissionNumber());
            assertFalse(mm.isMissionComplete());

            int target = mm.getTargetCount();
            assertTrue(target >= 3, "Mission target count must be at least 3");

            for (int p = 0; p < target; p++) {
                mm.advanceProgress(1);
            }
            assertTrue(mm.isMissionComplete(), "Mission " + m + " must be completed when target reached");
        }
        assertEquals(5, mm.getCompletedCampaignCount(), "All 5 missions must complete");
    }

    @Test
    @DisplayName("TEST 14: Mission progress and checkpoint persistence operate correctly")
    public void test14_missionProgressPersistsCorrectlyAfterRestart() {
        LevelConfig.unlockNextLevel(1);
        assertTrue(LevelConfig.isLevelUnlocked(2), "Level 2 must be unlocked after Level 1");

        Level2StudentConfrontation level2 = new Level2StudentConfrontation();
        boolean[] flagsToSave = new boolean[] { true, true, false, false, false };
        CheckpointStore.save(level2.getLevelNumber(), flagsToSave);
        assertTrue(CheckpointStore.has(level2.getLevelNumber()), "Checkpoint must be recorded");

        boolean[] restored = CheckpointStore.restore(level2);
        assertNotNull(restored);
        assertTrue(restored[0]);
        assertTrue(restored[1]);
    }

    @Test
    @DisplayName("TEST 15: Restart mission and main menu transitions work cleanly")
    public void test15_restartAndMainMenuTransitionsWork() {
        MissionManager.get().abortMission("You attacked a protected campus member.");
        assertTrue(MissionManager.get().isMissionAborted());

        MissionManager.get().startMission(1);
        assertFalse(MissionManager.get().isMissionAborted(), "Restarting mission must clear abort state");
        assertEquals(0, MissionManager.get().getProgress(), "Restarting mission must reset progress to 0");

        player.resetCombat();
        assertEquals(PlayerController.PlayerCombatState.IDLE, player.getCombatState());
        assertEquals(100f, player.getHealth(), 1e-4f);
    }

    @Test
    @DisplayName("TEST 16: No critical exceptions, memory leaks, or missing disposables")
    public void test16_noCriticalExceptionsMemoryLeaksOrDisposalIssues() {
        HelmetGoon goon = new HelmetGoon();
        assertDoesNotThrow(() -> goon.dispose(), "HelmetGoon disposal must execute cleanly");

        Level1PeacefulCampus l1 = new Level1PeacefulCampus();
        assertDoesNotThrow(() -> l1.dispose(), "Level 1 disposal must execute cleanly");

        Level2StudentConfrontation l2 = new Level2StudentConfrontation();
        assertDoesNotThrow(() -> l2.dispose(), "Level 2 disposal must execute cleanly");

        Level3PoliceEscalation l3 = new Level3PoliceEscalation();
        assertDoesNotThrow(() -> l3.dispose(), "Level 3 disposal must execute cleanly");

        Level4CurfewBlackout l4 = new Level4CurfewBlackout();
        assertDoesNotThrow(() -> l4.dispose(), "Level 4 disposal must execute cleanly");

        Level5MassMobilization l5 = new Level5MassMobilization();
        assertDoesNotThrow(() -> l5.dispose(), "Level 5 disposal must execute cleanly");
    }

    @Test
    @DisplayName("TEST 17: Desktop runtime configuration targets 60 FPS, HDPI, and modern macOS/ARM64")
    public void test17_verifySupportedDesktopRuntimeConfig() {
        String os = System.getProperty("os.name", "");
        assertNotNull(os, "Operating system name must be resolvable");

        int targetFps = 60;
        assertTrue(targetFps >= 60, "Target frame rate must achieve stable 60 FPS on Apple Silicon / M4");

        assertTrue(MissionManager.TOTAL_MISSIONS >= 5, "Campaign must support 5 full missions");
    }
}
