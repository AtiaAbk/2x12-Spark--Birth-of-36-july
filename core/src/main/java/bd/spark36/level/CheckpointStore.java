package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Mid-level checkpoints (reference section 11.3: "checkpoints before difficult sequences",
 * "no forced long replay after failure").
 *
 * Persists, per level, which objectives are done. Stored in memory and, when a libGDX
 * backend is running, in {@link Preferences} under keys {@code checkpoint.L<n>} as a
 * string of '1' / '0' flags (e.g. {@code "11000"}). Tolerates {@code Gdx.app == null}
 * so it can be unit tested headlessly.
 *
 * Flow:
 * - {@link #recordProgress} is polled by {@link LevelManager}; it saves whenever a new
 *   objective completes and clears the checkpoint when the level is complete.
 * - {@link #requestRestore} + {@link LevelManager#restartCurrentLevel()} reloads the level
 *   and re-applies the saved flags ("Restart from checkpoint").
 */
public final class CheckpointStore {

    public static final String PREF_NAME = "Spark36Preferences";
    public static final String KEY_PREFIX = "checkpoint.L";

    private static final Map<Integer, boolean[]> memory = new HashMap<>();
    private static boolean restoreRequested = false;

    private CheckpointStore() {}

    public static String key(int level) {
        return KEY_PREFIX + level;
    }

    // ------------------------------------------------------------------
    // Storage
    // ------------------------------------------------------------------

    /** Saves the objective flags for {@code level} (memory + Preferences when available). */
    public static void save(int level, boolean[] flags) {
        if (flags == null) return;
        memory.put(level, flags.clone());
        Preferences prefs = prefs();
        if (prefs != null) {
            try {
                prefs.putString(key(level), encode(flags));
                prefs.flush();
            } catch (Exception ignored) {}
        }
    }

    /** @return the saved flags for {@code level}, or null when there is no checkpoint. */
    public static boolean[] load(int level) {
        boolean[] mem = memory.get(level);
        if (mem != null) return mem.clone();
        Preferences prefs = prefs();
        if (prefs != null) {
            try {
                String s = prefs.getString(key(level), null);
                boolean[] decoded = decode(s);
                if (decoded != null) {
                    memory.put(level, decoded.clone());
                    return decoded;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    /** True when {@code level} has a checkpoint with at least one completed objective. */
    public static boolean has(int level) {
        boolean[] f = load(level);
        if (f == null) return false;
        for (boolean b : f) if (b) return true;
        return false;
    }

    /** Removes the checkpoint for {@code level} (called when the level is completed). */
    public static void clear(int level) {
        memory.remove(level);
        Preferences prefs = prefs();
        if (prefs != null) {
            try {
                prefs.remove(key(level));
                prefs.flush();
            } catch (Exception ignored) {}
        }
    }

    /** Clears in-memory state only (tests). Preferences are untouched. */
    public static void resetMemoryForTest() {
        memory.clear();
        restoreRequested = false;
    }

    // ------------------------------------------------------------------
    // Level integration
    // ------------------------------------------------------------------

    /**
     * Compares the level's current flags against {@code runFlags} (the flags already seen in
     * this run, updated in place). Saves when an objective newly completed; clears when the
     * level is complete.
     * @return true if a checkpoint was written.
     */
    public static boolean recordProgress(CampaignLevel level, boolean[] runFlags) {
        if (level == null) return false;
        int n = level.getLevelNumber();
        if (level.isLevelComplete()) {
            clear(n); // callers stop polling once a level is complete, so this runs once
            return false;
        }
        boolean[] now = level.getObjectiveFlags();
        boolean newlyDone = false;
        for (int i = 0; i < now.length; i++) {
            boolean before = runFlags != null && i < runFlags.length && runFlags[i];
            if (now[i] && !before) newlyDone = true;
            if (runFlags != null && i < runFlags.length) runFlags[i] = now[i];
        }
        if (newlyDone) {
            save(n, now);
            if (Gdx.app != null) Gdx.app.log("CheckpointStore", "Checkpoint saved for Level " + n + ": " + encode(now));
            return true;
        }
        return false;
    }

    /**
     * Applies the saved checkpoint to a freshly initialised level.
     * @return the flags applied, or null if there was no checkpoint.
     */
    public static boolean[] restore(CampaignLevel level) {
        if (level == null) return null;
        boolean[] flags = load(level.getLevelNumber());
        if (flags == null) return null;
        level.restoreObjectiveFlags(flags.clone());
        return flags;
    }

    /** Marks that the next {@link LevelManager#restartCurrentLevel()} should restore the checkpoint. */
    public static void requestRestore() {
        restoreRequested = true;
    }

    /** Returns and clears the pending restore request. */
    public static boolean consumeRestoreRequest() {
        boolean r = restoreRequested;
        restoreRequested = false;
        return r;
    }

    // ------------------------------------------------------------------
    // Encoding
    // ------------------------------------------------------------------

    static String encode(boolean[] flags) {
        StringBuilder sb = new StringBuilder(flags.length);
        for (boolean b : flags) sb.append(b ? '1' : '0');
        return sb.toString();
    }

    static boolean[] decode(String s) {
        if (s == null || s.isEmpty()) return null;
        boolean[] f = new boolean[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '0' && c != '1') return null;
            f[i] = (c == '1');
        }
        return f;
    }

    private static Preferences prefs() {
        if (Gdx.app == null) return null;
        try {
            return Gdx.app.getPreferences(PREF_NAME);
        } catch (Exception e) {
            return null;
        }
    }
}
