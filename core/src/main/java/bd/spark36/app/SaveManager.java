package bd.spark36.app;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.function.IntSupplier;

/**
 * JSON persistence for settings and story progress (repo DECISION-004).
 *
 * <ul>
 *   <li>{@code spark36_settings.json}: sensitivity, volumes, subtitles, text size, camera-motion
 *       reduction, graphics tier, tone map, language.</li>
 *   <li>{@code spark36_save.json}: unlocked chapter and last played chapter.</li>
 * </ul>
 *
 * Missing or corrupt files fall back to defaults; individual bad fields fall back to their own
 * default while the rest of the file is kept. When there is no save file yet, the legacy libGDX
 * Preferences key {@code Spark36Preferences/unlockedChapter} is read through
 * {@code legacyUnlockedChapter} and migrated into the JSON file.
 *
 * <p>Uses only libGDX's pure-Java JSON classes and the injected {@link SaveStorage}, so it runs in
 * unit tests without a libGDX application.</p>
 */
public final class SaveManager {

    public static final String SETTINGS_FILE = "spark36_settings.json";
    public static final String SAVE_FILE = "spark36_save.json";
    static final int FORMAT_VERSION = 1;

    private final SaveStorage storage;
    private final IntSupplier legacyUnlockedChapter;
    private String lastError;

    /**
     * @param storage               where the JSON files live
     * @param legacyUnlockedChapter returns the old Preferences value, or 0 when there is none
     */
    public SaveManager(SaveStorage storage, IntSupplier legacyUnlockedChapter) {
        this.storage = storage;
        this.legacyUnlockedChapter = legacyUnlockedChapter != null ? legacyUnlockedChapter : () -> 0;
    }

    public SaveStorage getStorage() {
        return storage;
    }

    /** Last load/save problem, or null. For logs only. */
    public String getLastError() {
        return lastError;
    }

    // ── Settings ──────────────────────────────────────────────────────────

    public GameSettings loadSettings() {
        GameSettings s = GameSettings.defaults();
        JsonValue root = parse(storage.read(SETTINGS_FILE), SETTINGS_FILE);
        if (root == null) return s;
        s.mouseSensitivity = getFloat(root, "mouseSensitivity", s.mouseSensitivity);
        s.masterVolume = getFloat(root, "masterVolume", s.masterVolume);
        s.sfxVolume = getFloat(root, "sfxVolume", s.sfxVolume);
        s.ambienceVolume = getFloat(root, "ambienceVolume", s.ambienceVolume);
        s.subtitles = getBool(root, "subtitles", s.subtitles);
        s.textSizeIndex = getInt(root, "textSize", s.textSizeIndex);
        s.reduceCameraMotion = getBool(root, "reduceCameraMotion", s.reduceCameraMotion);
        s.graphicsTier = getString(root, "graphicsTier", s.graphicsTier);
        s.toneMapping = getInt(root, "toneMapping", s.toneMapping);
        String lang = getString(root, "language", s.language.name());
        try {
            s.language = GameSettings.Language.valueOf(lang.trim().toUpperCase());
        } catch (Exception e) {
            s.language = GameSettings.Language.EN;
        }
        return s.sanitize();
    }

    public boolean saveSettings(GameSettings settings) {
        GameSettings s = settings.copy().sanitize();
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("version", new JsonValue(FORMAT_VERSION));
        root.addChild("mouseSensitivity", num(s.mouseSensitivity));
        root.addChild("masterVolume", num(s.masterVolume));
        root.addChild("sfxVolume", num(s.sfxVolume));
        root.addChild("ambienceVolume", num(s.ambienceVolume));
        root.addChild("subtitles", new JsonValue(s.subtitles));
        root.addChild("textSize", new JsonValue(s.textSizeIndex));
        root.addChild("reduceCameraMotion", new JsonValue(s.reduceCameraMotion));
        root.addChild("graphicsTier", s.graphicsTier != null ? new JsonValue(s.graphicsTier) : new JsonValue(JsonValue.ValueType.nullValue));
        root.addChild("toneMapping", new JsonValue(s.toneMapping));
        root.addChild("language", new JsonValue(s.language.name()));
        return write(SETTINGS_FILE, root);
    }

    // ── Story progress ────────────────────────────────────────────────────

    /** True when {@code spark36_save.json} exists (legacy Preferences are not counted). */
    public boolean hasSaveFile() {
        return storage.read(SAVE_FILE) != null;
    }

    public SaveData loadSave() {
        String text = storage.read(SAVE_FILE);
        if (text == null) {
            return migrateLegacy();
        }
        SaveData d = SaveData.empty();
        JsonValue root = parse(text, SAVE_FILE);
        if (root == null) {
            // Corrupt file: keep whatever the old Preferences still know rather than wiping progress.
            int legacy = safeLegacy();
            if (legacy > 1) d.unlockedLevel = legacy;
            return d.sanitize();
        }
        d.unlockedLevel = getInt(root, "unlockedLevel", d.unlockedLevel);
        d.lastPlayedLevel = getInt(root, "lastPlayedLevel", d.lastPlayedLevel);
        int legacy = safeLegacy();
        if (legacy > d.unlockedLevel) d.unlockedLevel = legacy; // never lose a chapter the old build unlocked
        return d.sanitize();
    }

    public boolean saveSave(SaveData data) {
        SaveData d = data.copy().sanitize();
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("version", new JsonValue(FORMAT_VERSION));
        root.addChild("unlockedLevel", new JsonValue(d.unlockedLevel));
        root.addChild("lastPlayedLevel", new JsonValue(d.lastPlayedLevel));
        return write(SAVE_FILE, root);
    }

    private SaveData migrateLegacy() {
        SaveData d = SaveData.empty();
        int legacy = safeLegacy();
        if (legacy >= 1) {
            d.unlockedLevel = legacy;
            // The old build only stored the unlock; resuming the furthest chapter is the closest match.
            d.lastPlayedLevel = legacy > 1 ? legacy : 0;
            d.sanitize();
            if (legacy > 1) saveSave(d);
        }
        return d.sanitize();
    }

    private int safeLegacy() {
        try {
            int v = legacyUnlockedChapter.getAsInt();
            return Math.max(0, Math.min(SaveData.MAX_LEVEL, v));
        } catch (Throwable t) {
            return 0;
        }
    }

    // ── JSON helpers ──────────────────────────────────────────────────────

    private JsonValue parse(String text, String name) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            JsonValue v = new JsonReader().parse(text);
            if (v == null || !v.isObject()) {
                lastError = name + ": not a JSON object, using defaults";
                return null;
            }
            return v;
        } catch (Exception e) {
            lastError = name + ": unreadable (" + e.getClass().getSimpleName() + "), using defaults";
            return null;
        }
    }

    private boolean write(String name, JsonValue root) {
        try {
            storage.write(name, root.prettyPrint(JsonWriter.OutputType.json, 0) + "\n");
            return true;
        } catch (Exception e) {
            lastError = name + ": could not be written (" + e.getMessage() + ")";
            return false;
        }
    }

    /** Rounds to 3 decimals so the file reads 0.7, not 0.699999988079071. */
    private static JsonValue num(float v) {
        return new JsonValue(Math.round(v * 1000.0) / 1000.0);
    }

    private static float getFloat(JsonValue root, String key, float def) {
        JsonValue v = root.get(key);
        if (v == null || !v.isNumber()) return def;
        return v.asFloat();
    }

    private static int getInt(JsonValue root, String key, int def) {
        JsonValue v = root.get(key);
        if (v == null || !v.isNumber()) return def;
        return v.asInt();
    }

    private static boolean getBool(JsonValue root, String key, boolean def) {
        JsonValue v = root.get(key);
        if (v == null || !v.isBoolean()) return def;
        return v.asBoolean();
    }

    private static String getString(JsonValue root, String key, String def) {
        JsonValue v = root.get(key);
        if (v == null || v.isNull() || !v.isString()) return def;
        return v.asString();
    }
}
