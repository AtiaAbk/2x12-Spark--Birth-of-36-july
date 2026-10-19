package bd.spark36.interaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.NpcBehaviorState;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentNPC;
import bd.spark36.dialogue.DialogueCallback;
import bd.spark36.dialogue.DialogueManager;
import bd.spark36.dialogue.DialogueSequence;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.level.DialogueRepository;
import bd.spark36.level.DialogueRepository.DialogueLine;
import bd.spark36.level.Level1PeacefulCampus;
import bd.spark36.mission.MissionObjective;
import bd.spark36.mission.MissionTracker;
import bd.spark36.mission.ObjectiveState;
import bd.spark36.mission.ObjectiveType;

/**
 * Comprehensive verification test suite for Phase 9:
 * 1. Interaction target selection (facing-weighted).
 * 2. Interaction range checks.
 * 3. Starting and ending an interaction.
 * 4. Dialogue progression and completion callbacks.
 * 5. Objective activation and progression.
 * 6. Prevention of duplicate objective completion.
 * 7. Mission reset behavior.
 * 8. Level transition & checklist status.
 * 9. NPC state restoration after interaction.
 * 10. Cooldown and debounce verification.
 */
public class InteractionMissionTest {

    private InteractionSystem interactionSystem;
    private DialogueManager dialogueManager;

    @BeforeEach
    public void setUp() {
        interactionSystem = new InteractionSystem();
        dialogueManager = new DialogueManager();
    }

    private static class MockInteractable implements Interactable {
        private final String id;
        private final Vector3 position;
        private final float radius;
        private final int priority;
        public boolean interacted = false;

        public MockInteractable(String id, Vector3 pos, float radius, int priority) {
            this.id = id;
            this.position = pos;
            this.radius = radius;
            this.priority = priority;
        }

        @Override public Vector3 getInteractionPosition() { return position; }
        @Override public float getInteractionRadius() { return radius; }
        @Override public String getInteractionPrompt() { return "[E] TEST " + id; }
        @Override public boolean canInteract(Vector3 playerPos, float playerHeading) { return true; }
        @Override public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
            interacted = true;
            return true;
        }
        @Override public int getInteractionPriority() { return priority; }
        @Override public String getInteractableId() { return id; }
    }

    @Test
    @DisplayName("InteractionSystem: Range checks correctly accept targets within radius and reject targets outside")
    public void testInteractionRangeChecks() {
        MockInteractable target = new MockInteractable("ObjA", new Vector3(0f, 0f, 10f), 3.0f, 1);
        interactionSystem.register(target);

        // Player at (0, 0, 8) -> distance = 2.0m <= 3.0m (In range)
        Interactable inRange = interactionSystem.findBestInteractable(new Vector3(0f, 0f, 8f), 0f);
        assertNotNull(inRange, "Target within radius should be found");
        assertEquals("ObjA", inRange.getInteractableId());

        // Player at (0, 0, 5) -> distance = 5.0m > 3.0m (Out of range)
        Interactable outOfRange = interactionSystem.findBestInteractable(new Vector3(0f, 0f, 5f), 0f);
        assertNull(outOfRange, "Target outside radius should return null");
    }

    @Test
    @DisplayName("InteractionSystem: Facing-weighted selection prioritizes objects in player's forward view")
    public void testInteractionFacingSelection() {
        // Player is at (0, 0, 0) facing 180 degrees (North, towards negative Z)
        // Target InFront is at (0, 0, -4) -> directly in forward field of view
        // Target Behind is at (0, 0, 4) -> behind the player
        MockInteractable inFront = new MockInteractable("InFront", new Vector3(0f, 0f, -4f), 5.0f, 1);
        MockInteractable behind = new MockInteractable("Behind", new Vector3(0f, 0f, 4f), 5.0f, 1);

        interactionSystem.register(inFront);
        interactionSystem.register(behind);

        Interactable best = interactionSystem.findBestInteractable(new Vector3(0f, 0f, 0f), 180f);
        assertNotNull(best, "Should find a target");
        assertEquals("InFront", best.getInteractableId(),
            "Facing-weighted selection must select the object in front of the player");
    }

    @Test
    @DisplayName("InteractionSystem: Priority weighting breaks ties when targets are equidistant")
    public void testInteractionPrioritySelection() {
        MockInteractable lowPri = new MockInteractable("AmbientNPC", new Vector3(2f, 0f, 0f), 4.0f, 1);
        MockInteractable highPri = new MockInteractable("MissionObjective", new Vector3(2f, 0f, 0f), 4.0f, 10);

        interactionSystem.register(lowPri);
        interactionSystem.register(highPri);

        Interactable best = interactionSystem.findBestInteractable(new Vector3(0f, 0f, 0f), 90f);
        assertNotNull(best, "Should find target");
        assertEquals("MissionObjective", best.getInteractableId(),
            "Higher priority interactable must be selected when distances are equal");
    }

    @Test
    @DisplayName("InteractionSystem: Debounce cooldown blocks rapid accidental re-activations")
    public void testInteractionDebounceCooldown() {
        MockInteractable target = new MockInteractable("DebounceTarget", new Vector3(0f, 0f, 2f), 3.0f, 1);
        interactionSystem.register(target);

        // First trigger succeeds
        boolean firstTrigger = interactionSystem.triggerInteraction(new Vector3(0f, 0f, 0f), 0f, null, null);
        assertTrue(firstTrigger, "First interaction trigger should succeed");
        assertTrue(target.interacted, "Target onInteract should have been called");
        assertTrue(interactionSystem.isCooldownActive(), "Cooldown must be active after interaction");

        // Reset target flag and attempt immediate re-trigger
        target.interacted = false;
        boolean immediateRetrigger = interactionSystem.triggerInteraction(new Vector3(0f, 0f, 0f), 0f, null, null);
        assertFalse(immediateRetrigger, "Immediate re-trigger must be blocked by debounce cooldown");
        assertFalse(target.interacted, "Target onInteract must not be called during cooldown");

        // Update system past cooldown
        interactionSystem.update(0.40f, new Vector3(0f, 0f, 0f), 0f);
        assertFalse(interactionSystem.isCooldownActive(), "Cooldown should expire after delta time");

        boolean afterCooldownTrigger = interactionSystem.triggerInteraction(new Vector3(0f, 0f, 0f), 0f, null, null);
        assertTrue(afterCooldownTrigger, "Interaction should succeed once cooldown timer expires");
        assertTrue(target.interacted, "Target onInteract should be called again");
    }

    @Test
    @DisplayName("DialogueManager: Multi-line playback progresses lines and triggers completion callback")
    public void testDialogueSequenceAndProgression() {
        DialogueLine l1 = new DialogueLine("SPEAKER_1", "বক্তা ১", "প্রথম বাক্য", "First line");
        DialogueLine l2 = new DialogueLine("SPEAKER_2", "বক্তা ২", "দ্বিতীয় বাক্য", "Second line");
        DialogueLine l3 = new DialogueLine("SPEAKER_1", "বক্তা ১", "তৃতীয় বাক্য", "Third line");

        final boolean[] callbackInvoked = {false};
        DialogueSequence seq = new DialogueSequence("test_seq", "Test Sequence",
            new DialogueCallback() {
                @Override
                public void onDialogueComplete(String sequenceId) {
                    callbackInvoked[0] = true;
                }
            },
            l1, l2, l3
        );

        dialogueManager.startDialogue(seq);
        assertTrue(dialogueManager.isDialogueActive(), "Dialogue must be active after start");
        assertEquals(0, dialogueManager.getCurrentLineIndex(), "Should start at line index 0");
        assertEquals(3, dialogueManager.getTotalLines());
        assertEquals("First line", dialogueManager.getCurrentLine().getTextEnglish());
        assertFalse(dialogueManager.isLastLine(), "Line 0 is not the last line");

        // Advance to line 1
        boolean adv1 = dialogueManager.advanceDialogue();
        assertTrue(adv1, "Advancing to line 1 returns true");
        assertEquals(1, dialogueManager.getCurrentLineIndex());
        assertEquals("Second line", dialogueManager.getCurrentLine().getTextEnglish());
        assertFalse(dialogueManager.isLastLine());

        // Advance to line 2 (last line)
        boolean adv2 = dialogueManager.advanceDialogue();
        assertTrue(adv2, "Advancing to line 2 returns true");
        assertEquals(2, dialogueManager.getCurrentLineIndex());
        assertEquals("Third line", dialogueManager.getCurrentLine().getTextEnglish());
        assertTrue(dialogueManager.isLastLine(), "Line 2 must be identified as the last line");

        // Advance from last line finishes sequence and invokes callback
        boolean adv3 = dialogueManager.advanceDialogue();
        assertFalse(adv3, "Advancing past the last line returns false (finishing sequence)");
        assertFalse(dialogueManager.isDialogueActive(), "Dialogue should no longer be active");
        assertTrue(callbackInvoked[0], "Completion callback must have been invoked");
    }

    @Test
    @DisplayName("DialogueManager: Early cancellation closes dialogue without firing completion callback")
    public void testDialogueEarlyCancellation() {
        DialogueLine l1 = new DialogueLine("SPK", "বক্তা", "বাংলা", "English");
        final boolean[] callbackInvoked = {false};
        DialogueSequence seq = new DialogueSequence("cancel_seq", "Cancel Seq",
            new DialogueCallback() {
                @Override
                public void onDialogueComplete(String sequenceId) {
                    callbackInvoked[0] = true;
                }
            },
            l1
        );

        dialogueManager.startDialogue(seq);
        assertTrue(dialogueManager.isDialogueActive());

        dialogueManager.cancelDialogue();
        assertFalse(dialogueManager.isDialogueActive(), "Dialogue must be inactive after cancellation");
        assertFalse(callbackInvoked[0], "Completion callback must NOT be invoked on cancellation");
    }

    @Test
    @DisplayName("MissionTracker: Sequential progression activates next objective upon completion")
    public void testObjectiveActivationAndProgression() {
        MissionTracker tracker = new MissionTracker(true); // Sequential mode

        MissionObjective o1 = new MissionObjective("o1", "OBJ 1", "Target 1", "1. Target 1",
            ObjectiveType.REACH_LOCATION, new Vector3(10f, 0f, 0f), 3.0f, true);
        MissionObjective o2 = new MissionObjective("o2", "OBJ 2", "Target 2", "2. Target 2",
            ObjectiveType.INTERACT, new Vector3(20f, 0f, 0f), 3.0f, false);
        MissionObjective o3 = new MissionObjective("o3", "OBJ 3", "Target 3", "3. Target 3",
            ObjectiveType.TRIGGER_EVENT, new Vector3(30f, 0f, 0f), 3.0f, false);

        tracker.addObjective(o1).addObjective(o2).addObjective(o3);

        assertEquals(3, tracker.getTotalCount(), "Total count should be 3");
        assertEquals(0, tracker.getCompletedCount(), "Initial completed count should be 0");
        assertTrue(o1.isActive(), "First objective starts active");
        assertEquals(ObjectiveState.INACTIVE, o2.getState(), "Second objective starts inactive");
        assertEquals(ObjectiveState.INACTIVE, o3.getState(), "Third objective starts inactive");

        // Player moves into proximity of Objective 1 (REACH_LOCATION)
        boolean proximityCompleted = tracker.update(0.016f, new Vector3(11f, 0f, 0f));
        assertTrue(proximityCompleted, "Proximity should trigger completion of Objective 1");
        assertTrue(o1.isComplete(), "Objective 1 must now be completed");
        assertEquals(1, tracker.getCompletedCount());

        // Sequential mode must automatically activate Objective 2!
        assertTrue(o2.isActive(), "Objective 2 should automatically become active");
        assertEquals(o2.getTargetPosition(), tracker.getActiveWaypoint(), "Active waypoint must now point to Objective 2");

        // Complete Objective 2 via ID
        boolean idCompleted = tracker.completeObjective("o2");
        assertTrue(idCompleted, "Completing objective 2 via ID should succeed");
        assertTrue(o2.isComplete(), "Objective 2 must be complete");
        assertEquals(2, tracker.getCompletedCount());

        // Objective 3 should now be active
        assertTrue(o3.isActive(), "Objective 3 should now be active");
        tracker.completeObjective(2);
        assertTrue(o3.isComplete(), "Objective 3 must be complete");
        assertEquals(3, tracker.getCompletedCount());
        assertTrue(tracker.isAllCompleted(), "All objectives must be complete");
    }

    @Test
    @DisplayName("MissionObjective: Duplicate completion attempts are safely ignored")
    public void testDuplicateObjectiveCompletionPrevention() {
        MissionObjective obj = new MissionObjective("single_obj", "Title", "Desc", "Label",
            ObjectiveType.INTERACT, new Vector3(), 3.0f, true);

        assertTrue(obj.complete(), "First complete() must succeed");
        assertTrue(obj.isComplete());

        // Second complete call on same objective must be ignored
        assertFalse(obj.complete(), "Second complete() call on already completed objective must return false");
        assertTrue(obj.isComplete());
    }

    @Test
    @DisplayName("MissionTracker: Reset restores objectives to initial state cleanly")
    public void testMissionResetBehavior() {
        MissionTracker tracker = new MissionTracker(true);
        MissionObjective o1 = new MissionObjective("o1", "Title 1", "Desc 1", "Label 1",
            ObjectiveType.REACH_LOCATION, new Vector3(10f, 0f, 0f), 2.0f, true);
        MissionObjective o2 = new MissionObjective("o2", "Title 2", "Desc 2", "Label 2",
            ObjectiveType.INTERACT, new Vector3(20f, 0f, 0f), 2.0f, false);

        tracker.addObjective(o1).addObjective(o2);
        tracker.completeObjective(0);
        tracker.completeObjective(1);
        assertTrue(tracker.isAllCompleted());
        assertEquals(2, tracker.getCompletedCount());

        // Reset level
        tracker.reset();
        assertEquals(0, tracker.getCompletedCount(), "Completed count after reset must be 0");
        assertFalse(tracker.isAllCompleted(), "All completed must be false after reset");
        assertTrue(o1.isActive(), "Objective 1 must be reset to active");
        assertEquals(ObjectiveState.INACTIVE, o2.getState(), "Objective 2 must be reset to inactive");
    }

    @Test
    @DisplayName("StudentNPC: Player interaction engages state and safely restores upon dialogue finish")
    public void testNpcInteractionAndStateRestoration() {
        StudentNPC npc = new StudentNPC(1, 0, 1, 1.3f, "DU Student");
        npc.setPosition(0f, 0f, 10f);

        // Put NPC into walking state
        npc.transitionTo(NpcBehaviorState.WALKING, 10.0f);
        assertEquals(NpcBehaviorState.WALKING, npc.getState());
        assertTrue(npc.isInteractable());

        // Player interacts with NPC
        boolean interactSuccess = npc.onInteract(new Vector3(0f, 0f, 8f), null, null);
        assertTrue(interactSuccess, "NPC onInteract should succeed");
        assertEquals(NpcBehaviorState.INTERACTING_PLAYER, npc.getState(),
            "NPC should enter INTERACTING_PLAYER state");

        // When interaction completes, state must restore safely
        npc.endInteraction();
        assertNotEquals(NpcBehaviorState.INTERACTING_PLAYER, npc.getState(),
            "NPC must exit INTERACTING_PLAYER state upon completion");
        assertTrue(npc.isInteractable(), "NPC should return to an interactable baseline state");
    }

    @Test
    @DisplayName("Level1PeacefulCampus: Integrates MissionTracker and completeAllObjectivesForTest")
    public void testLevel1MissionTrackerIntegration() {
        Level1PeacefulCampus level1 = new Level1PeacefulCampus();
        MissionTracker tracker = level1.getMissionTracker();
        assertNotNull(tracker, "Level 1 must provide a MissionTracker");

        level1.completeAllObjectivesForTest();
        assertTrue(level1.isLevelComplete(), "completeAllObjectivesForTest must mark level complete");
    }
}
