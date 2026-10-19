package bd.spark36.mission;

import com.badlogic.gdx.math.Vector3;

/**
 * Encapsulates an individual, verifiable mission objective with explicit state,
 * target coordinates, proximity detection, and duplicate-completion prevention.
 */
public class MissionObjective {

    private final String id;
    private final String title;
    private final String targetDescription;
    private final String checklistLabel;
    private final ObjectiveType type;

    private ObjectiveState state;
    private final ObjectiveState initialState;

    private final Vector3 targetPosition;
    private final float targetRadius;

    // Optional countable progress (e.g. 3 of 5 archives collected)
    private int currentCount = 0;
    private int requiredCount = 1;

    // Bound dialogue or event identifier
    private String boundActionId = null;

    public MissionObjective(String id, String title, String targetDescription, String checklistLabel,
                            ObjectiveType type, Vector3 targetPosition, float targetRadius, boolean startsActive) {
        this.id = id;
        this.title = title;
        this.targetDescription = targetDescription;
        this.checklistLabel = (checklistLabel != null) ? checklistLabel : title;
        this.type = type;
        this.targetPosition = (targetPosition != null) ? new Vector3(targetPosition) : new Vector3();
        this.targetRadius = targetRadius;
        this.initialState = startsActive ? ObjectiveState.ACTIVE : ObjectiveState.INACTIVE;
        this.state = this.initialState;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public String getChecklistLabel() {
        return checklistLabel;
    }

    public ObjectiveType getType() {
        return type;
    }

    public ObjectiveState getState() {
        return state;
    }

    public Vector3 getTargetPosition() {
        return targetPosition;
    }

    public float getTargetRadius() {
        return targetRadius;
    }

    public boolean isActive() {
        return state == ObjectiveState.ACTIVE;
    }

    public boolean isComplete() {
        return state == ObjectiveState.COMPLETED;
    }

    public boolean isFailed() {
        return state == ObjectiveState.FAILED;
    }

    public boolean activate() {
        if (state == ObjectiveState.INACTIVE) {
            state = ObjectiveState.ACTIVE;
            return true;
        }
        return false;
    }

    /**
     * Marks objective complete. Guaranteed to execute state transition only once,
     * preventing duplicate triggers or double-credit.
     * @return true if state transitioned to COMPLETED, false if already completed/inactive.
     */
    public boolean complete() {
        if (state == ObjectiveState.ACTIVE) {
            currentCount = requiredCount;
            state = ObjectiveState.COMPLETED;
            return true;
        }
        return false;
    }

    public boolean fail() {
        if (state == ObjectiveState.ACTIVE) {
            state = ObjectiveState.FAILED;
            return true;
        }
        return false;
    }

    public void reset() {
        this.state = this.initialState;
        this.currentCount = 0;
    }

    public MissionObjective setCountable(int requiredCount) {
        this.requiredCount = Math.max(1, requiredCount);
        this.currentCount = 0;
        return this;
    }

    public boolean incrementCount() {
        if (state != ObjectiveState.ACTIVE) return false;
        currentCount++;
        if (currentCount >= requiredCount) {
            return complete();
        }
        return false;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public MissionObjective setBoundActionId(String actionId) {
        this.boundActionId = actionId;
        return this;
    }

    public String getBoundActionId() {
        return boundActionId;
    }

    /**
     * Proximity check for REACH_LOCATION objectives.
     * Automatically completes objective if the player is within targetRadius.
     */
    public boolean checkLocationProximity(Vector3 playerPos) {
        if (state == ObjectiveState.ACTIVE && type == ObjectiveType.REACH_LOCATION && playerPos != null) {
            float distSq = playerPos.dst2(targetPosition);
            if (distSq <= targetRadius * targetRadius) {
                return complete();
            }
        }
        return false;
    }
}
