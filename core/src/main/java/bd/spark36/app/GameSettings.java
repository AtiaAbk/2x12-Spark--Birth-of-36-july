package bd.spark36.app;

/**
 * Player-facing settings persisted to {@code spark36_settings.json} (repo DECISION-004).
 *
 * <p>Plain data with no libGDX dependency so it can be built, clamped and compared in unit tests.
 * Every setter clamps to the legal range so a hand-edited or corrupt file can never push the game
 * into an unusable state.</p>
 */
public final class GameSettings {

    public static final float MIN_SENSITIVITY = 0.25f;
    public static final float MAX_SENSITIVITY = 3.0f;

    /** Dialogue text scale steps offered by the settings screen. */
    public static final float[] TEXT_SCALES = {0.85f, 1.0f, 1.2f, 1.4f};
    public static final String[] TEXT_SCALE_NAMES = {"SMALL", "NORMAL", "LARGE", "EXTRA LARGE"};

    public enum Language { EN, BN }

    /** Multiplier on the camera's base mouse sensitivity (1.0 = shipped feel). */
    public float mouseSensitivity = 1.0f;
    public float masterVolume = 0.85f;
    public float sfxVolume = 1.0f;
    public float ambienceVolume = 0.6f;
    /** Subtitles on by default (reference section 11.3). */
    public boolean subtitles = true;
    /** Index into {@link #TEXT_SCALES}. */
    public int textSizeIndex = 1;
    /** Removes camera head-bob, screen shake and the sprint FOV push. */
    public boolean reduceCameraMotion = false;
    /** LOW / MEDIUM / HIGH, or null meaning "use the assets/visuals.properties default". */
    public String graphicsTier = null;
    /** 1 = ACES, 2 = Reinhard, 0 = Linear; -1 meaning "use the visuals.properties default". */
    public int toneMapping = -1;
    public Language language = Language.EN;

    public static GameSettings defaults() {
        return new GameSettings();
    }

    public float textScale() {
        return TEXT_SCALES[textSizeIndex];
    }

    public String textSizeName() {
        return TEXT_SCALE_NAMES[textSizeIndex];
    }

    /** Clamps every field into its legal range; returns this for chaining. */
    public GameSettings sanitize() {
        mouseSensitivity = clamp(finite(mouseSensitivity, 1.0f), MIN_SENSITIVITY, MAX_SENSITIVITY);
        masterVolume = clamp(finite(masterVolume, 0.85f), 0f, 1f);
        sfxVolume = clamp(finite(sfxVolume, 1f), 0f, 1f);
        ambienceVolume = clamp(finite(ambienceVolume, 0.6f), 0f, 1f);
        if (textSizeIndex < 0 || textSizeIndex >= TEXT_SCALES.length) textSizeIndex = 1;
        if (graphicsTier != null) {
            String t = graphicsTier.trim().toUpperCase();
            graphicsTier = ("LOW".equals(t) || "MEDIUM".equals(t) || "HIGH".equals(t)) ? t : null;
        }
        if (toneMapping < -1 || toneMapping > 2) toneMapping = -1;
        if (language == null) language = Language.EN;
        return this;
    }

    public GameSettings copy() {
        GameSettings c = new GameSettings();
        c.mouseSensitivity = mouseSensitivity;
        c.masterVolume = masterVolume;
        c.sfxVolume = sfxVolume;
        c.ambienceVolume = ambienceVolume;
        c.subtitles = subtitles;
        c.textSizeIndex = textSizeIndex;
        c.reduceCameraMotion = reduceCameraMotion;
        c.graphicsTier = graphicsTier;
        c.toneMapping = toneMapping;
        c.language = language;
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameSettings)) return false;
        GameSettings s = (GameSettings) o;
        return Float.compare(mouseSensitivity, s.mouseSensitivity) == 0
            && Float.compare(masterVolume, s.masterVolume) == 0
            && Float.compare(sfxVolume, s.sfxVolume) == 0
            && Float.compare(ambienceVolume, s.ambienceVolume) == 0
            && subtitles == s.subtitles
            && textSizeIndex == s.textSizeIndex
            && reduceCameraMotion == s.reduceCameraMotion
            && java.util.Objects.equals(graphicsTier, s.graphicsTier)
            && toneMapping == s.toneMapping
            && language == s.language;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(mouseSensitivity, masterVolume, sfxVolume, ambienceVolume, subtitles,
            textSizeIndex, reduceCameraMotion, graphicsTier, toneMapping, language);
    }

    @Override
    public String toString() {
        return "GameSettings{sens=" + mouseSensitivity + ", master=" + masterVolume + ", sfx=" + sfxVolume
            + ", amb=" + ambienceVolume + ", subtitles=" + subtitles + ", textSize=" + textSizeIndex
            + ", reduceMotion=" + reduceCameraMotion + ", tier=" + graphicsTier + ", toneMap=" + toneMapping
            + ", lang=" + language + "}";
    }

    private static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static float finite(float v, float fallback) {
        return (Float.isNaN(v) || Float.isInfinite(v)) ? fallback : v;
    }
}
