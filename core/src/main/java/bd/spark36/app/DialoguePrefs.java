// ei file ta dialogue settings ar preferences save kore
package bd.spark36.app;

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

    public static boolean showBengaliLine() {
        return language == GameSettings.Language.BN || subtitles;
    }

    public static boolean showEnglishLine() {
        return language == GameSettings.Language.EN || subtitles;
    }
}
