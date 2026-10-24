package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;

/**
 * Pure, headless objective logic for Level 2 ("The Day the Streets Changed").
 *
 * Victory is defensive and non-lethal (reference section 8.3): the player stabilises
 * two injured students, escorts each to the medical shelter, then secures the shelter.
 * Helmeted aggressors are a hazard to evade, block or break away from. Knocking an
 * aggressor out is possible but optional: it is counted, never required, and never
 * completes an objective in story mode.
 *
 * Kept free of GL / HUD types so it can be unit tested without a libGDX backend.
 */
public final class Level2ShelterObjectives {

    public static final int STABILIZE_FIRST = 0;
    public static final int ESCORT_FIRST = 1;
    public static final int STABILIZE_SECOND = 2;
    public static final int ESCORT_SECOND = 3;
    public static final int SECURE_SHELTER = 4;
    public static final int COUNT = 5;

    public static final float STUDENT_RADIUS = 3.5f;
    public static final float SHELTER_RADIUS = 4.0f;

    /** Injured student near the Raju Sculpture base. */
    public static final Vector3 FIRST_STUDENT_POS = new Vector3(44f, 0f, 42f);
    /** Injured student on the Arts Faculty approach road. */
    public static final Vector3 SECOND_STUDENT_POS = new Vector3(22f, 0f, 32f);
    /** Medical shelter (triage tent) near the Arts Faculty gate. */
    public static final Vector3 SHELTER_POS = new Vector3(-20f, 0f, 28f);

    public static final String[] CHECKLIST = {
        "1. Stabilize Injured Student at Raju Sculpture",
        "2. Escort Student to Medical Shelter (Evade Aggressors)",
        "3. Reach Injured Student on Arts Faculty Road",
        "4. Escort Second Student to Medical Shelter",
        "5. Secure the Medical Shelter"
    };

    public static final String[] TITLES = {
        "STABILIZE INJURED STUDENT",
        "ESCORT TO MEDICAL SHELTER",
        "REACH SECOND INJURED STUDENT",
        "ESCORT SECOND STUDENT TO SHELTER",
        "SECURE THE MEDICAL SHELTER"
    };

    public static final String[] TARGETS = {
        "Target: Fallen student at Raju Sculpture (evade, block or break away from aggressors)",
        "Target: Medical shelter at Arts Faculty gate (knockouts optional)",
        "Target: Injured student on Arts Faculty road",
        "Target: Medical shelter at Arts Faculty gate (knockouts optional)",
        "Target: Medical shelter (both students safe inside)"
    };

    private static final Vector3[] WAYPOINTS = {
        FIRST_STUDENT_POS, SHELTER_POS, SECOND_STUDENT_POS, SHELTER_POS, SHELTER_POS
    };

    private final boolean[] done = new boolean[COUNT];
    private int optionalKnockouts = 0;

    public boolean isDone(int index) {
        return index >= 0 && index < COUNT && done[index];
    }

    public int getCompletedCount() {
        int c = 0;
        for (boolean b : done) if (b) c++;
        return c;
    }

    public boolean isComplete() {
        for (boolean b : done) if (!b) return false;
        return true;
    }

    /** Index of the next unfinished objective, or the last index once all are done. */
    public int nextIndex() {
        for (int i = 0; i < COUNT; i++) if (!done[i]) return i;
        return COUNT - 1;
    }

    public Vector3 getWaypoint(int index) {
        return WAYPOINTS[Math.max(0, Math.min(COUNT - 1, index))];
    }

    public Vector3 getActiveWaypoint() {
        return getWaypoint(nextIndex());
    }

    /** True while the player is guiding a stabilised student to the shelter. */
    public boolean isEscorting() {
        return (done[STABILIZE_FIRST] && !done[ESCORT_FIRST])
            || (done[STABILIZE_SECOND] && !done[ESCORT_SECOND]);
    }

    /** Context prompt for the objective reachable from {@code pos}, or null. */
    public String contextPrompt(Vector3 pos) {
        int idx = interactableIndex(pos);
        switch (idx) {
            case STABILIZE_FIRST:
            case STABILIZE_SECOND:
                return "[E] APPLY FIRST AID & HELP STUDENT TO THEIR FEET";
            case ESCORT_FIRST:
            case ESCORT_SECOND:
                return "[E] HAND STUDENT OVER TO SHELTER MEDICS";
            case SECURE_SHELTER:
                return "[E] SECURE THE MEDICAL SHELTER";
            default:
                return null;
        }
    }

    /**
     * Attempts the interaction available at {@code pos}.
     * @return the objective index completed, or -1 if nothing was in range.
     */
    public int interact(Vector3 pos) {
        int idx = interactableIndex(pos);
        if (idx >= 0) done[idx] = true;
        return idx;
    }

    private int interactableIndex(Vector3 pos) {
        if (pos == null) return -1;
        int next = nextIndex();
        if (done[next]) return -1; // all complete
        Vector3 wp = WAYPOINTS[next];
        float radius = (next == STABILIZE_FIRST || next == STABILIZE_SECOND) ? STUDENT_RADIUS : SHELTER_RADIUS;
        if (next == SECURE_SHELTER && !(done[ESCORT_FIRST] && done[ESCORT_SECOND])) return -1;
        return pos.dst(wp) <= radius ? next : -1;
    }

    /** Records an optional aggressor knockout. Never completes an objective. */
    public void recordOptionalKnockout() {
        optionalKnockouts++;
    }

    public int getOptionalKnockouts() {
        return optionalKnockouts;
    }

    public boolean[] snapshot() {
        return done.clone();
    }

    /** Restores objective flags (e.g. from a checkpoint). Missing entries are treated as not done. */
    public void restore(boolean[] flags) {
        for (int i = 0; i < COUNT; i++) {
            done[i] = flags != null && i < flags.length && flags[i];
        }
    }

    public void reset() {
        java.util.Arrays.fill(done, false);
        optionalKnockouts = 0;
    }

    public void completeAllForTest() {
        java.util.Arrays.fill(done, true);
    }
}
