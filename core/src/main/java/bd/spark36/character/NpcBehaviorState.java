package bd.spark36.character;

/**
 * AI behavioral states for student NPCs and campus crowds across the July–August 2024 movement.
 * Governs locomotion, social clustering, ambient gestures, protest actions, and reactions to events.
 */
public enum NpcBehaviorState {

    /** Standing naturally, breathing, subtle weight shift, observing surroundings. */
    IDLE("Idle"),

    /** Moving along the campus waypoint path network toward a destination. */
    WALKING("Walking"),

    /** Paused at a landmark, notice board, or pond, looking around with head yaw. */
    LOOKING_AROUND("Observing Campus"),

    /** Standing in a pair or small circle, facing conversation partners, gesturing while conversing. */
    SOCIALIZING("Socializing"),

    /** Moving toward a designated gathering point (e.g. Arts Plaza, Curzon portal, Raju Sculpture). */
    GROUPING("Forming Group"),

    /** Standing in an assembly, facing a student organizer or speaker, listening attentively. */
    LISTENING("Listening to Assembly"),

    /** Rhythmic chanting in solidarity with the mass student movement, arm raised. */
    CHANTING("Chanting Slogans"),

    /** Fleeing away from confrontations, tear gas, or police cordons toward campus perimeter. */
    FLEEING("Dispersing / Seeking Safety"),

    /** Paused and turned to face the protagonist student during an active dialogue interaction. */
    INTERACTING_PLAYER("Conversing with Player");

    public final String displayName;

    NpcBehaviorState(String displayName) {
        this.displayName = displayName;
    }

    /** Whether this state involves physical forward movement across the world. */
    public boolean isMoving() {
        return this == WALKING || this == GROUPING || this == FLEEING;
    }

    /** Whether the student is available to be engaged in conversation by the player. */
    public boolean isInteractable() {
        return this == IDLE || this == WALKING || this == LOOKING_AROUND || this == SOCIALIZING || this == LISTENING;
    }
}
