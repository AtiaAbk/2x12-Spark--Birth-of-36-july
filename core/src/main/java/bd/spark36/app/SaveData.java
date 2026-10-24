package bd.spark36.app;

/**
 * Story progress persisted to {@code spark36_save.json}.
 *
 * <p>{@code lastPlayedLevel == 0} means "no story level has been played yet", which hides the
 * main-menu Continue entry.</p>
 */
public final class SaveData {

    public static final int MAX_LEVEL = 6;

    /** Highest chapter the player may start (1..6). */
    public int unlockedLevel = 1;
    /** Chapter Continue resumes (0 = none yet, else 1..6). */
    public int lastPlayedLevel = 0;

    public static SaveData empty() {
        return new SaveData();
    }

    public boolean hasProgress() {
        return lastPlayedLevel >= 1;
    }

    public SaveData sanitize() {
        if (unlockedLevel < 1) unlockedLevel = 1;
        if (unlockedLevel > MAX_LEVEL) unlockedLevel = MAX_LEVEL;
        if (lastPlayedLevel < 0) lastPlayedLevel = 0;
        if (lastPlayedLevel > unlockedLevel) lastPlayedLevel = unlockedLevel;
        return this;
    }

    public SaveData copy() {
        SaveData c = new SaveData();
        c.unlockedLevel = unlockedLevel;
        c.lastPlayedLevel = lastPlayedLevel;
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SaveData)) return false;
        SaveData s = (SaveData) o;
        return unlockedLevel == s.unlockedLevel && lastPlayedLevel == s.lastPlayedLevel;
    }

    @Override
    public int hashCode() {
        return 31 * unlockedLevel + lastPlayedLevel;
    }

    @Override
    public String toString() {
        return "SaveData{unlocked=" + unlockedLevel + ", lastPlayed=" + lastPlayedLevel + "}";
    }
}
