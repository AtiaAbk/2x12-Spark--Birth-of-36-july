package bd.spark36.mission;

/**
 * Lifecycle state of an individual mission objective.
 */
public enum ObjectiveState {
    /** Objective has not yet been unlocked or activated */
    INACTIVE,

    /** Objective is currently active and can be progressed by player */
    ACTIVE,

    /** Objective has been successfully accomplished */
    COMPLETED,

    /** Objective was failed (e.g. stealth detected or VIP compromised) */
    FAILED
}
