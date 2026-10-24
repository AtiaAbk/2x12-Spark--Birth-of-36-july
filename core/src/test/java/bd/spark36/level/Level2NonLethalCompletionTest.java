package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 2 must be completable with zero knockouts (reference section 8.3, work order T5).
 * Runs headlessly: the level is constructed but never init()-ed, and the HUD is null.
 */
public class Level2NonLethalCompletionTest {

    private Level2StudentConfrontation level;

    @BeforeEach
    public void setUp() {
        LevelConfig.reset(2, LevelConfig.GameMode.STORY);
        level = new Level2StudentConfrontation();
    }

    private boolean interactAt(Vector3 pos) {
        return level.onInteract(pos.cpy(), null, null);
    }

    @Test
    @DisplayName("Level 2: completes by sheltering both injured students with zero knockouts")
    public void testCompletesWithZeroKnockouts() {
        assertFalse(level.isLevelComplete());

        assertTrue(interactAt(Level2ShelterObjectives.FIRST_STUDENT_POS), "stabilize first student");
        assertTrue(level.getShelterObjectives().isEscorting());
        assertTrue(interactAt(Level2ShelterObjectives.SHELTER_POS), "hand first student to shelter medics");
        assertFalse(level.getShelterObjectives().isEscorting());
        assertTrue(interactAt(Level2ShelterObjectives.SECOND_STUDENT_POS), "stabilize second student");
        assertTrue(interactAt(Level2ShelterObjectives.SHELTER_POS), "hand second student to shelter medics");
        assertFalse(level.isLevelComplete(), "shelter must still be secured");
        assertTrue(interactAt(Level2ShelterObjectives.SHELTER_POS), "secure the shelter");

        assertTrue(level.isLevelComplete());
        assertEquals(5, level.getCompletedObjectiveCount());
        assertEquals(0, level.getShelterObjectives().getOptionalKnockouts());
        assertTrue(level.getMissionTracker().isAllCompleted(), "mission tracker mirrors the shelter objectives");
    }

    @Test
    @DisplayName("Level 2: checklist no longer asks the player to neutralize or disarm anyone")
    public void testChecklistIsNonLethal() {
        String[] items = level.getChecklistItems();
        assertEquals(level.getTotalObjectiveCount(), items.length);
        for (String item : items) {
            String lower = item.toLowerCase();
            assertFalse(lower.contains("neutraliz"), item);
            assertFalse(lower.contains("disarm"), item);
            assertFalse(lower.contains("defeat"), item);
        }
        assertTrue(items[items.length - 1].toLowerCase().contains("shelter"));
    }

    @Test
    @DisplayName("Level 2: knockouts are optional and never complete an objective")
    public void testKnockoutsNeverCompleteObjectives() {
        Level2ShelterObjectives obj = level.getShelterObjectives();
        for (int i = 0; i < 3; i++) obj.recordOptionalKnockout();
        assertEquals(3, obj.getOptionalKnockouts());
        assertEquals(0, level.getCompletedObjectiveCount());
        assertFalse(level.isLevelComplete());
    }

    @Test
    @DisplayName("Level 2: objectives are ordered; the shelter cannot be secured before both students arrive")
    public void testOrderingAndRanges() {
        assertFalse(interactAt(Level2ShelterObjectives.SHELTER_POS), "nothing to do at the shelter yet");
        assertFalse(interactAt(Level2ShelterObjectives.SECOND_STUDENT_POS), "second student comes after the first");
        assertFalse(interactAt(new Vector3(0f, 0f, 0f)), "out of range");
        assertNull(level.getContextPrompt(new Vector3(0f, 0f, 0f)));
        assertNotNull(level.getContextPrompt(Level2ShelterObjectives.FIRST_STUDENT_POS.cpy()));
        assertEquals(Level2ShelterObjectives.FIRST_STUDENT_POS, level.getActiveWaypoint());
    }
}
