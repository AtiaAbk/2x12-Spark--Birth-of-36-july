package bd.spark36.app;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SaveManagerTest {

    /** In-memory storage so tests never touch the player's real profile folder. */
    static final class MapStorage implements SaveStorage {
        final Map<String, String> files = new HashMap<>();

        @Override public String read(String name) { return files.get(name); }
        @Override public void write(String name, String content) { files.put(name, content); }
        @Override public String describe() { return "memory"; }
    }

    @Test
    void settingsRoundTripThroughRealFiles(@TempDir Path dir) {
        SaveManager m = new SaveManager(new SaveStorage.Directory(dir.toFile()), () -> 0);
        GameSettings s = GameSettings.defaults();
        s.mouseSensitivity = 1.75f;
        s.masterVolume = 0.4f;
        s.sfxVolume = 0.3f;
        s.ambienceVolume = 0.2f;
        s.subtitles = false;
        s.textSizeIndex = 3;
        s.reduceCameraMotion = true;
        s.graphicsTier = "LOW";
        s.toneMapping = 2;
        s.language = GameSettings.Language.BN;

        assertTrue(m.saveSettings(s));
        assertTrue(new File(dir.toFile(), SaveManager.SETTINGS_FILE).isFile());
        assertFalse(new File(dir.toFile(), SaveManager.SETTINGS_FILE + ".tmp").exists(), "temp file must be renamed away");

        GameSettings back = new SaveManager(new SaveStorage.Directory(dir.toFile()), () -> 0).loadSettings();
        assertEquals(s, back);
    }

    @Test
    void saveRoundTrip() {
        MapStorage st = new MapStorage();
        SaveManager m = new SaveManager(st, () -> 0);
        SaveData d = SaveData.empty();
        d.unlockedLevel = 4;
        d.lastPlayedLevel = 3;
        assertTrue(m.saveSave(d));
        assertTrue(m.hasSaveFile());
        assertEquals(d, new SaveManager(st, () -> 0).loadSave());
    }

    @Test
    void missingFilesGiveDefaultsAndNoContinue() {
        SaveManager m = new SaveManager(new MapStorage(), () -> 0);
        assertEquals(GameSettings.defaults(), m.loadSettings());
        SaveData d = m.loadSave();
        assertEquals(1, d.unlockedLevel);
        assertFalse(d.hasProgress());
        assertTrue(GameSettings.defaults().subtitles, "subtitles must default to on");
    }

    @Test
    void corruptFilesFallBackToDefaults() {
        MapStorage st = new MapStorage();
        st.files.put(SaveManager.SETTINGS_FILE, "{\"masterVolume\": 0.5, \"subtitles\": tr");
        st.files.put(SaveManager.SAVE_FILE, "\u0000\u0001 not json at all ]]]");
        SaveManager m = new SaveManager(st, () -> 0);
        assertEquals(GameSettings.defaults(), m.loadSettings());
        SaveData d = m.loadSave();
        assertEquals(1, d.unlockedLevel);
        assertEquals(0, d.lastPlayedLevel);
        assertNotNull(m.getLastError());
    }

    @Test
    void nonObjectJsonIsTreatedAsCorrupt() {
        MapStorage st = new MapStorage();
        st.files.put(SaveManager.SETTINGS_FILE, "[1,2,3]");
        assertEquals(GameSettings.defaults(), new SaveManager(st, () -> 0).loadSettings());
    }

    @Test
    void badFieldsAreClampedOrDefaultedIndividually() {
        MapStorage st = new MapStorage();
        st.files.put(SaveManager.SETTINGS_FILE,
            "{\"mouseSensitivity\": 99, \"masterVolume\": -3, \"sfxVolume\": \"loud\", \"textSize\": 12,"
                + " \"graphicsTier\": \"ULTRA\", \"toneMapping\": 7, \"language\": \"FR\", \"subtitles\": false}");
        st.files.put(SaveManager.SAVE_FILE, "{\"unlockedLevel\": 42, \"lastPlayedLevel\": -5}");
        SaveManager m = new SaveManager(st, () -> 0);
        GameSettings s = m.loadSettings();
        assertEquals(GameSettings.MAX_SENSITIVITY, s.mouseSensitivity);
        assertEquals(0f, s.masterVolume);
        assertEquals(GameSettings.defaults().sfxVolume, s.sfxVolume);
        assertEquals(1, s.textSizeIndex);
        assertNull(s.graphicsTier);
        assertEquals(-1, s.toneMapping);
        assertEquals(GameSettings.Language.EN, s.language);
        assertFalse(s.subtitles, "valid fields survive next to bad ones");
        SaveData d = m.loadSave();
        assertEquals(6, d.unlockedLevel);
        assertEquals(0, d.lastPlayedLevel);
    }

    @Test
    void legacyPreferencesAreMigratedWhenNoSaveFileExists() {
        MapStorage st = new MapStorage();
        SaveManager m = new SaveManager(st, () -> 4); // old build had unlocked Chapter 4
        SaveData d = m.loadSave();
        assertEquals(4, d.unlockedLevel);
        assertEquals(4, d.lastPlayedLevel, "Continue resumes the furthest migrated chapter");
        assertTrue(st.files.containsKey(SaveManager.SAVE_FILE), "migration writes the JSON save");

        // Once migrated, the JSON file is the source of truth
        SaveData again = new SaveManager(st, () -> 0).loadSave();
        assertEquals(d, again);
    }

    @Test
    void legacyChapterOneIsNotAProgressSave() {
        MapStorage st = new MapStorage();
        SaveData d = new SaveManager(st, () -> 1).loadSave();
        assertEquals(1, d.unlockedLevel);
        assertFalse(d.hasProgress());
        assertFalse(st.files.containsKey(SaveManager.SAVE_FILE));
    }

    @Test
    void legacyNeverLowersAndCanRaiseUnlock() {
        MapStorage st = new MapStorage();
        st.files.put(SaveManager.SAVE_FILE, "{\"unlockedLevel\": 3, \"lastPlayedLevel\": 2}");
        assertEquals(3, new SaveManager(st, () -> 2).loadSave().unlockedLevel);
        assertEquals(5, new SaveManager(st, () -> 5).loadSave().unlockedLevel);
    }

    @Test
    void corruptSaveKeepsLegacyUnlock() {
        MapStorage st = new MapStorage();
        st.files.put(SaveManager.SAVE_FILE, "{{{{");
        assertEquals(3, new SaveManager(st, () -> 3).loadSave().unlockedLevel);
    }

    @Test
    void throwingLegacySourceIsIgnored() {
        SaveData d = new SaveManager(new MapStorage(), () -> { throw new IllegalStateException("no prefs"); }).loadSave();
        assertEquals(1, d.unlockedLevel);
    }

    @Test
    void failedWriteIsReportedNotThrown() {
        SaveStorage broken = new SaveStorage() {
            @Override public String read(String name) { return null; }
            @Override public void write(String name, String content) throws IOException { throw new IOException("disk full"); }
            @Override public String describe() { return "broken"; }
        };
        SaveManager m = new SaveManager(broken, () -> 0);
        assertFalse(m.saveSettings(GameSettings.defaults()));
        assertTrue(m.getLastError().contains("disk full"));
    }

    @Test
    void writtenJsonIsReadable(@TempDir Path dir) throws IOException {
        SaveManager m = new SaveManager(new SaveStorage.Directory(dir.toFile()), () -> 0);
        m.saveSettings(GameSettings.defaults());
        String text = new String(Files.readAllBytes(dir.resolve(SaveManager.SETTINGS_FILE)), StandardCharsets.UTF_8);
        assertTrue(text.contains("\"masterVolume\""), text);
        assertTrue(text.contains("\"language\": \"EN\"") || text.contains("\"language\":\"EN\""), text);
    }

    @Test
    void defaultFolderIsPerUserNotWorkingDirectory() {
        String old = System.getProperty("bd.spark36.saveDir");
        try {
            System.clearProperty("bd.spark36.saveDir");
            File d = SaveStorage.defaultDirectory();
            assertEquals(new File(System.getProperty("user.home"), ".spark36"), d);
            System.setProperty("bd.spark36.saveDir", "custom_dir");
            assertEquals(new File("custom_dir"), SaveStorage.defaultDirectory());
        } finally {
            if (old == null) System.clearProperty("bd.spark36.saveDir");
            else System.setProperty("bd.spark36.saveDir", old);
        }
    }

    @Test
    void profileTracksContinueAndUnlock() {
        MapStorage st = new MapStorage();
        GameProfile p = new GameProfile(new SaveManager(st, () -> 0));
        assertFalse(p.hasContinue());
        p.onLevelActive(1, true);
        assertTrue(p.hasContinue());
        assertEquals(1, p.continueLevel());
        p.onLevelActive(3, true); // not unlocked yet: ignored
        assertEquals(1, p.continueLevel());
        p.unlockLevel(3);
        p.onLevelActive(3, true);
        assertEquals(3, p.continueLevel());
        p.onLevelActive(2, false); // arena does not move Continue
        assertEquals(3, p.continueLevel());

        GameProfile reloaded = new GameProfile(new SaveManager(st, () -> 0));
        assertEquals(3, reloaded.unlockedLevel());
        assertEquals(3, reloaded.continueLevel());
    }
}
