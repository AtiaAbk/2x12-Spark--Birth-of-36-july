package bd.spark36.level;

import com.badlogic.gdx.Gdx;

/**
 * Global and per-level gameplay rules and configuration.
 * Governs combat state, game mode (Story, Arena, Exploration), and level progression.
 *
 * Project Rule (Fix Pack 01 Item 3):
 * Level 1 "Where It All Began" is peaceful: no combat, weapons, enemies or player death.
 * The combat system remains fully preserved in the codebase but is gated by this
 * configuration, being active in Arena Mode, Level 2+, or via command-line flags.
 */
public class LevelConfig {

    public enum GameMode {
        STORY,          // Peaceful campus exploration & archive recovery (Level 1)
        ARENA,          // Technical martial arts combat arena & boss encounters
        EXPLORATION     // Free campus tour without combat or mission constraints
    }

    private static GameMode currentMode = GameMode.STORY;
    private static int currentLevel = 1;
    private static boolean combatEnabled = false;

    static {
        init();
    }

    /**
     * Initializes configuration from system properties and environment defaults.
     * Supports:
     *   -Dbd.spark36.mode=arena / story / exploration
     *   -Dbd.spark36.combat=true / false
     *   -Dbd.spark36.baseline=boss_fight
     */
    public static void init() {
        String modeProp = System.getProperty("bd.spark36.mode");
        if (modeProp != null && !modeProp.trim().isEmpty()) {
            try {
                currentMode = GameMode.valueOf(modeProp.trim().toUpperCase());
            } catch (Exception ignored) {
            }
        }

        String combatProp = System.getProperty("bd.spark36.combat");
        if (combatProp != null && !combatProp.trim().isEmpty()) {
            combatEnabled = Boolean.parseBoolean(combatProp.trim());
        } else {
            String baselineProp = System.getProperty("bd.spark36.baseline");
            if ("boss_fight".equalsIgnoreCase(baselineProp) || "combat".equalsIgnoreCase(baselineProp)) {
                currentMode = GameMode.ARENA;
                combatEnabled = true;
            } else if (currentMode == GameMode.ARENA) {
                combatEnabled = true;
            } else {
                // Story Mode Level 1 is strictly peaceful by project rule
                combatEnabled = (currentLevel > 1);
            }
        }

        if (Gdx.app != null) {
            Gdx.app.log("LevelConfig", String.format("Initialized LevelConfig: Mode=%s, Level=%d, CombatEnabled=%b",
                currentMode, currentLevel, combatEnabled));
        }
    }

    public static boolean isCombatEnabled() {
        return combatEnabled;
    }

    public static void setCombatEnabled(boolean enabled) {
        combatEnabled = enabled;
    }

    public static GameMode getGameMode() {
        return currentMode;
    }

    public static void setGameMode(GameMode mode) {
        currentMode = mode;
        if (mode == GameMode.ARENA) {
            combatEnabled = true;
        } else if (mode == GameMode.STORY && currentLevel == 1) {
            combatEnabled = false;
        }
    }

    public static int getCurrentLevel() {
        return currentLevel;
    }

    public static void setCurrentLevel(int level) {
        currentLevel = level;
        if (currentMode == GameMode.STORY && currentLevel == 1) {
            combatEnabled = false;
        }
    }

    public static boolean isPeacefulLevel() {
        return !combatEnabled;
    }

    public static void reset(int level, GameMode mode) {
        currentLevel = level;
        currentMode = mode;
        if (mode == GameMode.ARENA) {
            combatEnabled = true;
        } else if (mode == GameMode.STORY && level == 1) {
            combatEnabled = false;
        } else {
            combatEnabled = true;
        }
    }
}
