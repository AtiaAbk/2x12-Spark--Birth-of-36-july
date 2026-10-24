package bd.spark36.app;

import bd.spark36.audio.SoundManager;
import bd.spark36.render.RenderPipelineConfig;
import com.badlogic.gdx.Gdx;

/**
 * The running game's settings and story progress, loaded once from {@link SaveManager} and written
 * back on every change. Main menu, pause menu, settings screen and level flow talk to this class
 * instead of touching files or libGDX Preferences directly.
 */
public final class GameProfile {

    private static final String LEGACY_PREFS = "Spark36Preferences";
    private static final String LEGACY_KEY = "unlockedChapter";

    private static GameProfile instance;

    private final SaveManager manager;
    private final GameSettings settings;
    private final SaveData save;

    public GameProfile(SaveManager manager) {
        this.manager = manager;
        this.settings = manager.loadSettings();
        this.save = manager.loadSave();
    }

    public static synchronized GameProfile get() {
        if (instance == null) {
            SaveStorage.Directory storage = new SaveStorage.Directory(SaveStorage.defaultDirectory());
            instance = new GameProfile(new SaveManager(storage, GameProfile::readLegacyPreferences));
            log("Profile folder: " + storage.describe() + " -> " + instance.settings + ", " + instance.save);
            if (instance.manager.getLastError() != null) log(instance.manager.getLastError());
        }
        return instance;
    }

    /** Test hook: replace (or clear with null) the shared profile. */
    public static synchronized void install(GameProfile profile) {
        instance = profile;
    }

    public SaveManager getManager() {
        return manager;
    }

    /** Live settings object. Call {@link #settingsChanged()} after mutating it. */
    public GameSettings settings() {
        return settings;
    }

    public SaveData save() {
        return save;
    }

    /** Applies the loaded settings to every subsystem and keeps tone-map hotkeys in sync with the file. */
    public void applyStartupSettings() {
        SettingsApplier.apply(settings);
        RenderPipelineConfig.setToneMappingListener(this::onToneMappingChangedElsewhere);
    }

    /** Sanitises, applies live and persists. */
    public void settingsChanged() {
        settings.sanitize();
        SettingsApplier.apply(settings);
        if (!manager.saveSettings(settings)) log(manager.getLastError());
    }

    /** Restores every setting to its default, then applies and persists. */
    public void resetSettings() {
        GameSettings d = GameSettings.defaults();
        settings.mouseSensitivity = d.mouseSensitivity;
        settings.masterVolume = d.masterVolume;
        settings.sfxVolume = d.sfxVolume;
        settings.ambienceVolume = d.ambienceVolume;
        settings.subtitles = d.subtitles;
        settings.textSizeIndex = d.textSizeIndex;
        settings.reduceCameraMotion = d.reduceCameraMotion;
        settings.graphicsTier = null;
        settings.toneMapping = -1;
        settings.language = d.language;
        settingsChanged();
    }

    // ── Progress ──────────────────────────────────────────────────────────

    public boolean hasContinue() {
        return save.hasProgress();
    }

    public int continueLevel() {
        return Math.max(1, Math.min(save.unlockedLevel, save.lastPlayedLevel));
    }

    public int unlockedLevel() {
        return save.unlockedLevel;
    }

    public void unlockLevel(int level) {
        int next = Math.max(1, Math.min(SaveData.MAX_LEVEL, level));
        if (next > save.unlockedLevel) {
            save.unlockedLevel = next;
            persistSave();
        }
    }

    /**
     * Called whenever a level becomes active. Story levels become the Continue target; the
     * ambience bed follows the level in every mode.
     */
    public void onLevelActive(int level, boolean storyMode) {
        if (storyMode && level >= 1 && level <= save.unlockedLevel && save.lastPlayedLevel != level) {
            save.lastPlayedLevel = level;
            persistSave();
        }
        SoundManager sm = SoundManager.getIfCreated();
        if (sm == null && Gdx.app != null) sm = SoundManager.get();
        if (sm != null) sm.setAmbience(SoundManager.ambienceForLevel(level));
    }

    /** Back on the main menu: silence the level ambience. */
    public void onMainMenu() {
        SoundManager sm = SoundManager.getIfCreated();
        if (sm != null) sm.setAmbience(SoundManager.Ambience.NONE);
    }

    private void persistSave() {
        if (!manager.saveSave(save)) log(manager.getLastError());
    }

    private void onToneMappingChangedElsewhere() {
        if (settings.toneMapping != RenderPipelineConfig.toneMappingMode) {
            settings.toneMapping = RenderPipelineConfig.toneMappingMode;
            if (!manager.saveSettings(settings)) log(manager.getLastError());
        }
    }

    private static int readLegacyPreferences() {
        if (Gdx.app == null) return 0;
        try {
            return Gdx.app.getPreferences(LEGACY_PREFS).getInteger(LEGACY_KEY, 0);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static void log(String msg) {
        if (msg == null) return;
        if (Gdx.app != null) Gdx.app.log("GameProfile", msg);
        else System.out.println("[GameProfile] " + msg);
    }
}
