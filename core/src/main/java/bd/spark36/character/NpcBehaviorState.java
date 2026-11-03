// ei file ta student npc der idle, walk ar talking state control kore
package bd.spark36.character;

public enum NpcBehaviorState {

    IDLE("Idle"),

    WALKING("Walking"),

    LOOKING_AROUND("Observing Campus"),

    SOCIALIZING("Socializing"),

    GROUPING("Forming Group"),

    LISTENING("Listening to Assembly"),

    CHANTING("Chanting Slogans"),

    FLEEING("Dispersing / Seeking Safety"),

    INTERACTING_PLAYER("Conversing with Player");

    public final String displayName;

    NpcBehaviorState(String displayName) {
        this.displayName = displayName;
    }

    public boolean isMoving() {
        return this == WALKING || this == GROUPING || this == FLEEING;
    }

    public boolean isInteractable() {
        return this == IDLE || this == WALKING || this == LOOKING_AROUND || this == SOCIALIZING || this == LISTENING;
    }
}
