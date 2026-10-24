package bd.spark36.app;

/**
 * Read-only view of the dialogue display settings for the HUD. Kept as plain statics so the HUD
 * hook stays one line and tests can set them without a libGDX application.
 */
public final class DialoguePrefs {

    private static volatile boolean subtitles = true;
    private static volatile float textScale = 1.0f;
    private static volatile GameSettings.Language language = GameSettings.Language.EN;

    private DialoguePrefs() {}

    static void set(boolean subtitlesOn, float scale, GameSettings.Language lang) {
        subtitles = subtitlesOn;
        textScale = scale;
        language = lang != null ? lang : GameSettings.Language.EN;
    }

    public static boolean subtitlesOn() {
        return subtitles;
    }

    public static float textScale() {
        return textScale;
    }

    public static GameSettings.Language language() {
        return language;
    }

    /**
     * Whether the Bengali line of a dialogue box is drawn. The chosen language is always shown;
     * the other language is the subtitle/translation line and follows the Subtitles switch.
     */
    public static boolean showBengaliLine() {
        return language == GameSettings.Language.BN || subtitles;
    }

    /** Whether the English line of a dialogue box is drawn (see {@link #showBengaliLine()}). */
    public static boolean showEnglishLine() {
        return language == GameSettings.Language.EN || subtitles;
    }
}
