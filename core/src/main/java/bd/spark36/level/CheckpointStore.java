// ei file ta level er checkpoint save ar restore kore
package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

public final class CheckpointStore {

    public static final String PREF_NAME = "Spark36Preferences";
    public static final String KEY_PREFIX = "checkpoint.L";

    private static final Map<Integer, boolean[]> memory = new HashMap<>();
    private static boolean restoreRequested = false;

    private CheckpointStore() {}

    public static String key(int level) {
        return KEY_PREFIX + level;
    }

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

    public static boolean has(int level) {
        boolean[] f = load(level);
        if (f == null) return false;
        for (boolean b : f) if (b) return true;
        return false;
    }

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

    public static void resetMemoryForTest() {
        memory.clear();
        restoreRequested = false;
    }

    public static boolean recordProgress(CampaignLevel level, boolean[] runFlags) {
        if (level == null) return false;
        int n = level.getLevelNumber();
        if (level.isLevelComplete()) {
            clear(n);
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

    public static boolean[] restore(CampaignLevel level) {
        if (level == null) return null;
        boolean[] flags = load(level.getLevelNumber());
        if (flags == null) return null;
        level.restoreObjectiveFlags(flags.clone());
        return flags;
    }

    public static void requestRestore() {
        restoreRequested = true;
    }

    public static boolean consumeRestoreRequest() {
        boolean r = restoreRequested;
        restoreRequested = false;
        return r;
    }

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
