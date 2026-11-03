// ei file ta loading screen ar menu screen logic test kore
package bd.spark36.hud.screens;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoadingAndMenuScreenTest {

    @Test
    @DisplayName("Loading Screen Progress: Clamping and Completion State")
    public void testLoadingScreenProgress() {
        LoadingScreen screen = new LoadingScreen(null, null, null);

        assertEquals(0.0f, screen.getProgress(), 1e-4f);
        assertFalse(screen.isFinished());

        screen.setProgress(0.45f);
        assertEquals(0.45f, screen.getProgress(), 1e-4f);
        assertFalse(screen.isFinished());

        screen.setProgress(1.20f);
        assertEquals(1.0f, screen.getProgress(), 1e-4f, "Progress must clamp to 1.0f");

        screen.setProgress(-0.50f);
        assertEquals(0.0f, screen.getProgress(), 1e-4f, "Progress must clamp to 0.0f");
    }

    @Test
    @DisplayName("Main Menu Chapters: Correct Chapter Titles and Formats")
    public void testMainMenuChapterTitles() {
        assertEquals("CH 1: CURZON SOLIDARITY (JUL 1-14)", MainMenuScreen.chapterName(1));
        assertEquals("CH 2: CONFRONTATION (JUL 15-16)", MainMenuScreen.chapterName(2));
        assertEquals("CH 3: POLICE ESCALATION (JUL 17-18)", MainMenuScreen.chapterName(3));
        assertEquals("CH 4: CURFEW BLACKOUT (JUL 19-22)", MainMenuScreen.chapterName(4));
        assertEquals("CH 5: MASS MOBILIZATION (AUG 3-4)", MainMenuScreen.chapterName(5));
        assertEquals("CH 6: VICTORY & 36 JULY (AUG 5)", MainMenuScreen.chapterName(6));
    }
}
