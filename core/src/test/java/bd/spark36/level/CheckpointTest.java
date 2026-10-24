package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mid-level checkpoints (work order T5.5). Headless: Gdx.app is null, so only the
 * in-memory store is exercised; levels are constructed but never init()-ed.
 */
public class CheckpointTest {

    @BeforeEach
    public void setUp() {
        LevelConfig.reset(2, LevelConfig.GameMode.STORY);
        CheckpointStore.resetMemoryForTest();
        for (int i = 1; i <= 6; i++) CheckpointStore.clear(i);
    }

    @Test
    @DisplayName("Checkpoint: save/load round-trip, key prefix and encoding")
    public void testRoundTrip() {
        boolean[] flags = {true, false, true, false, false};
        CheckpointStore.save(3, flags);
        flags[1] = true; // mutate caller copy: store must keep its own copy
        assertArrayEquals(new boolean[] {true, false, true, false, false}, CheckpointStore.load(3));
        assertTrue(CheckpointStore.has(3));
        assertFalse(CheckpointStore.has(4));
        assertNull(CheckpointStore.load(4));
        assertEquals("checkpoint.L3", CheckpointStore.key(3));
        assertEquals("10100", CheckpointStore.encode(new boolean[] {true, false, true, false, false}));
        assertArrayEquals(new boolean[] {false, true, true}, CheckpointStore.decode("011"));
        assertNull(CheckpointStore.decode("01x"));
        assertNull(CheckpointStore.decode(""));
    }

    @Test
    @DisplayName("Checkpoint: saved when an objective completes, cleared when the level completes")
    public void testSaveOnObjectiveAndClearOnCompletion() {
        Level2StudentConfrontation level = new Level2StudentConfrontation();
        boolean[] run = level.getObjectiveFlags();
        assertFalse(CheckpointStore.recordProgress(level, run), "nothing new yet");
        assertFalse(CheckpointStore.has(2));

        level.onInteract(Level2ShelterObjectives.FIRST_STUDENT_POS.cpy(), null, null);
        assertTrue(CheckpointStore.recordProgress(level, run), "first objective saves a checkpoint");
        assertArrayEquals(new boolean[] {true, false, false, false, false}, CheckpointStore.load(2));
        assertFalse(CheckpointStore.recordProgress(level, run), "no duplicate save without new progress");

        level.onInteract(Level2ShelterObjectives.SHELTER_POS.cpy(), null, null);
        level.onInteract(Level2ShelterObjectives.SECOND_STUDENT_POS.cpy(), null, null);
        CheckpointStore.recordProgress(level, run);
        assertArrayEquals(new boolean[] {true, true, true, false, false}, CheckpointStore.load(2));

        level.onInteract(Level2ShelterObjectives.SHELTER_POS.cpy(), null, null);
        level.onInteract(Level2ShelterObjectives.SHELTER_POS.cpy(), null, null);
        assertTrue(level.isLevelComplete());
        CheckpointStore.recordProgress(level, run);
        assertFalse(CheckpointStore.has(2), "checkpoint is cleared on level completion");
        assertNull(CheckpointStore.load(2));
    }

    @Test
    @DisplayName("Checkpoint: restore re-applies flags and derived state to a fresh Level 2")
    public void testRestoreLevel2() {
        CheckpointStore.save(2, new boolean[] {true, true, true, false, false});
        Level2StudentConfrontation fresh = new Level2StudentConfrontation();
        assertNotNull(CheckpointStore.restore(fresh));

        assertEquals(3, fresh.getCompletedObjectiveCount());
        assertTrue(fresh.getShelterObjectives().isEscorting(), "second student is mid-escort");
        assertEquals(Level2ShelterObjectives.SHELTER_POS, fresh.getActiveWaypoint());
        assertEquals(3, fresh.getMissionTracker().getCompletedCount(), "mission tracker restored too");
        assertFalse(fresh.isLevelComplete());

        // Play continues from the checkpoint
        assertTrue(fresh.onInteract(Level2ShelterObjectives.SHELTER_POS.cpy(), null, null));
        assertTrue(fresh.onInteract(Level2ShelterObjectives.SHELTER_POS.cpy(), null, null));
        assertTrue(fresh.isLevelComplete());
    }

    @Test
    @DisplayName("Checkpoint: restore re-derives carried items (Level 3 water) and copies flags (Levels 4-6)")
    public void testRestoreOtherLevels() {
        CheckpointStore.save(3, new boolean[] {true, false, false, false, false});
        Level3PoliceEscalation l3 = new Level3PoliceEscalation();
        CheckpointStore.restore(l3);
        assertEquals(1, l3.getCompletedObjectiveCount());
        assertNotNull(l3.getContextPrompt(new Vector3(18f, 0f, 52f)), "player is carrying water again after restore");

        CheckpointStore.save(4, new boolean[] {true, true, false, false});
        Level4CurfewBlackout l4 = new Level4CurfewBlackout();
        CheckpointStore.restore(l4);
        assertEquals(2, l4.getCompletedObjectiveCount());

        CheckpointStore.save(5, new boolean[] {true, false, false, false});
        Level5MassMobilization l5 = new Level5MassMobilization();
        CheckpointStore.restore(l5);
        assertArrayEquals(new boolean[] {true, false, false, false}, l5.getObjectiveFlags());

        CheckpointStore.save(6, new boolean[] {true, true, true, false});
        Level6VictoryEpilogue l6 = new Level6VictoryEpilogue();
        CheckpointStore.restore(l6);
        assertEquals(3, l6.getCompletedObjectiveCount());
        assertFalse(l6.isLevelComplete());
    }

    @Test
    @DisplayName("Checkpoint: no checkpoint means restore is a no-op; restore request is one-shot")
    public void testNoCheckpointAndRequestFlag() {
        Level5MassMobilization l5 = new Level5MassMobilization();
        assertNull(CheckpointStore.restore(l5));
        assertEquals(0, l5.getCompletedObjectiveCount());

        assertFalse(CheckpointStore.consumeRestoreRequest());
        CheckpointStore.requestRestore();
        assertTrue(CheckpointStore.consumeRestoreRequest());
        assertFalse(CheckpointStore.consumeRestoreRequest());
    }
}
