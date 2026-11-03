// ei file ta shob active objective track kore ar update kore
package bd.spark36.mission;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class MissionTracker {

    public interface MissionListener {
        void onObjectiveActivated(MissionObjective objective, int index);
        void onObjectiveCompleted(MissionObjective objective, int index);
        void onMissionCompleted();
    }

    private final Array<MissionObjective> objectives = new Array<MissionObjective>(false, 8);
    private boolean isSequential = true;
    private MissionListener listener = null;

    private String[] checklistCache = null;

    public MissionTracker(boolean sequential) {
        this.isSequential = sequential;
    }

    public void setListener(MissionListener listener) {
        this.listener = listener;
    }

    public MissionTracker addObjective(MissionObjective obj) {
        if (obj != null) {
            objectives.add(obj);
            checklistCache = null;
        }
        return this;
    }

    public boolean update(float delta, Vector3 playerPos) {
        boolean anyCompleted = false;

        for (int i = 0; i < objectives.size; i++) {
            MissionObjective obj = objectives.get(i);
            if (obj.isActive()) {
                if (obj.checkLocationProximity(playerPos)) {
                    anyCompleted = true;
                    handleObjectiveCompleted(i, obj);
                    if (isSequential) break;
                }
            }
        }

        return anyCompleted;
    }

    public boolean completeObjective(String id) {
        if (id == null) return false;
        for (int i = 0; i < objectives.size; i++) {
            MissionObjective obj = objectives.get(i);
            if (id.equals(obj.getId()) && obj.isActive()) {
                if (obj.complete()) {
                    handleObjectiveCompleted(i, obj);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean completeObjective(int index) {
        if (index >= 0 && index < objectives.size) {
            MissionObjective obj = objectives.get(index);
            if (obj.isActive() && obj.complete()) {
                handleObjectiveCompleted(index, obj);
                return true;
            }
        }
        return false;
    }

    private void handleObjectiveCompleted(int index, MissionObjective completedObj) {
        if (listener != null) {
            listener.onObjectiveCompleted(completedObj, index);
        }

        if (isSequential) {

            int nextIndex = index + 1;
            if (nextIndex < objectives.size) {
                MissionObjective nextObj = objectives.get(nextIndex);
                if (nextObj.activate() && listener != null) {
                    listener.onObjectiveActivated(nextObj, nextIndex);
                }
            }
        }

        if (isAllCompleted() && listener != null) {
            listener.onMissionCompleted();
        }
    }

    public MissionObjective getActiveObjective() {
        for (int i = 0; i < objectives.size; i++) {
            MissionObjective obj = objectives.get(i);
            if (obj.isActive()) return obj;
        }

        if (objectives.size > 0 && isAllCompleted()) {
            return objectives.get(objectives.size - 1);
        }
        return (objectives.size > 0) ? objectives.get(0) : null;
    }

    public Vector3 getActiveWaypoint() {
        MissionObjective active = getActiveObjective();
        return (active != null) ? active.getTargetPosition() : null;
    }

    public boolean isAllCompleted() {
        if (objectives.size == 0) return false;
        for (int i = 0; i < objectives.size; i++) {
            if (!objectives.get(i).isComplete()) return false;
        }
        return true;
    }

    public int getCompletedCount() {
        int count = 0;
        for (int i = 0; i < objectives.size; i++) {
            if (objectives.get(i).isComplete()) count++;
        }
        return count;
    }

    public int getTotalCount() {
        return objectives.size;
    }

    public MissionObjective getObjective(int index) {
        if (index >= 0 && index < objectives.size) {
            return objectives.get(index);
        }
        return null;
    }

    public MissionObjective getObjectiveById(String id) {
        if (id == null) return null;
        for (int i = 0; i < objectives.size; i++) {
            MissionObjective obj = objectives.get(i);
            if (id.equals(obj.getId())) return obj;
        }
        return null;
    }

    public String[] getChecklistItems() {
        if (checklistCache == null || checklistCache.length != objectives.size) {
            checklistCache = new String[objectives.size];
            for (int i = 0; i < objectives.size; i++) {
                checklistCache[i] = objectives.get(i).getChecklistLabel();
            }
        }
        return checklistCache;
    }

    public boolean isChecklistItemDone(int index) {
        if (index >= 0 && index < objectives.size) {
            return objectives.get(index).isComplete();
        }
        return false;
    }

    public void reset() {
        for (int i = 0; i < objectives.size; i++) {
            objectives.get(i).reset();
        }
        if (isSequential && objectives.size > 0) {

            objectives.get(0).activate();
        }
    }

    public void completeAllForTest() {
        for (int i = 0; i < objectives.size; i++) {
            objectives.get(i).activate();
            objectives.get(i).complete();
        }
    }

    public void restoreCompleted(boolean[] flags) {
        reset();
        if (flags == null) return;
        MissionListener saved = listener;
        listener = null;
        try {
            for (int i = 0; i < objectives.size && i < flags.length; i++) {
                if (!flags[i]) continue;
                MissionObjective obj = objectives.get(i);
                obj.activate();
                completeObjective(i);
            }
        } finally {
            listener = saved;
        }
    }
}
