// ei file ta active level, difficulty ar game mode config kore
package bd.spark36.level;

import com.badlogic.gdx.Gdx;

public class LevelConfig {

    public enum GameMode {
        STORY,
        ARENA,
        EXPLORATION
    }

    private static GameMode currentMode = GameMode.STORY;
    private static int currentLevel = 1;
    private static boolean combatEnabled = false;

    static {
        init();
    }

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
                combatEnabled = true;
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
        combatEnabled = true;
    }

    public static int getCurrentLevel() {
        return currentLevel;
    }

    public static void setCurrentLevel(int level) {
        currentLevel = level;
    }

    public static boolean isPeacefulLevel() {
        return !combatEnabled;
    }

    public static void reset(int level, GameMode mode) {
        currentLevel = level;
        currentMode = mode;
        combatEnabled = true;
        unlockedLevel = 1;
    }

    private static int unlockedLevel = 1;

    public static int getUnlockedLevel() {
        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.unlockAll"))) {
            return 6;
        }
        if (Gdx.app != null) {
            try {
                int saved = bd.spark36.app.GameProfile.get().unlockedLevel();
                if (saved > unlockedLevel) unlockedLevel = saved;
            } catch (Exception ignored) {}
        }
        return Math.max(1, Math.min(6, unlockedLevel));
    }

    public static void unlockNextLevel(int completedLevel) {
        int next = Math.min(6, completedLevel + 1);
        if (next > unlockedLevel) {
            unlockedLevel = next;
            if (Gdx.app != null) {
                try {
                    bd.spark36.app.GameProfile.get().unlockLevel(unlockedLevel);
                    Gdx.app.log("LevelConfig", "Progress saved! Unlocked Chapter: " + unlockedLevel);
                } catch (Exception ignored) {}
            }
        }
    }

    public static boolean isLevelUnlocked(int level) {
        return level <= getUnlockedLevel();
    }
}
