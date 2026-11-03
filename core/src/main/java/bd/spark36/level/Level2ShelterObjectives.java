// ei file ta level 2 er mission objectives ar task track kore
package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;

public final class Level2ShelterObjectives {

    public static final int STABILIZE_FIRST = 0;
    public static final int ESCORT_FIRST = 1;
    public static final int STABILIZE_SECOND = 2;
    public static final int ESCORT_SECOND = 3;
    public static final int SECURE_SHELTER = 4;
    public static final int COUNT = 5;

    public static final float STUDENT_RADIUS = 3.5f;
    public static final float SHELTER_RADIUS = 4.0f;

    public static final Vector3 FIRST_STUDENT_POS = new Vector3(44f, 0f, 42f);

    public static final Vector3 SECOND_STUDENT_POS = new Vector3(22f, 0f, 32f);

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
        "Target: Injured student at Raju Sculpture",
        "Target: Medical shelter (evade or block attackers)",
        "Target: Injured student on Arts Faculty road",
        "Target: Medical shelter (knockouts optional)",
        "Target: Medical shelter, both students inside"
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

    public boolean isEscorting() {
        return (done[STABILIZE_FIRST] && !done[ESCORT_FIRST])
            || (done[STABILIZE_SECOND] && !done[ESCORT_SECOND]);
    }

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

    public int interact(Vector3 pos) {
        int idx = interactableIndex(pos);
        if (idx >= 0) done[idx] = true;
        return idx;
    }

    private int interactableIndex(Vector3 pos) {
        if (pos == null) return -1;
        int next = nextIndex();
        if (done[next]) return -1;
        Vector3 wp = WAYPOINTS[next];
        float radius = (next == STABILIZE_FIRST || next == STABILIZE_SECOND) ? STUDENT_RADIUS : SHELTER_RADIUS;
        if (next == SECURE_SHELTER && !(done[ESCORT_FIRST] && done[ESCORT_SECOND])) return -1;
        return pos.dst(wp) <= radius ? next : -1;
    }

    public void recordOptionalKnockout() {
        optionalKnockouts++;
    }

    public int getOptionalKnockouts() {
        return optionalKnockouts;
    }

    public boolean[] snapshot() {
        return done.clone();
    }

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
