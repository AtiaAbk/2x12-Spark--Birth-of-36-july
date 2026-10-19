package bd.spark36.mission;

/**
 * Functional category of a gameplay mission objective.
 */
public enum ObjectiveType {
    /** Reach a specified 3D world landmark or location */
    REACH_LOCATION,

    /** Interact with an eligible world object, memorial, or shelter */
    INTERACT,

    /** Initiate and complete a dialogue sequence with an NPC or coordinator */
    COMPLETE_DIALOGUE,

    /** Trigger or resolve a scripted scene event (e.g. combat victory, barrier breach) */
    TRIGGER_EVENT,

    /** Visit a set of locations or markers in a designated sequence */
    VISIT_SEQUENCE
}
