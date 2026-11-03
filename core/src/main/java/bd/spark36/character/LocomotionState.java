// ei file ta character er haata ar douranor movement state rakhe
package bd.spark36.character;

public enum LocomotionState {
    IDLE("Idle"),
    START_WALK("Starting Walk"),
    WALK("Walking"),
    START_SPRINT("Starting Sprint"),
    SPRINT("Sprinting"),
    STOPPING("Stopping & Settling"),
    TURN_IN_PLACE("Turning in Place"),
    AIRBORNE_JUMP("Airborne Ascent"),
    AIRBORNE_FALL("Airborne Fall"),
    LANDING("Landing Cushion"),
    CROUCH_IDLE("Crouch Idle"),
    CROUCH_WALK("Crouch Walking"),
    WADING("Wading in Pukur"),
    WALL_CLIMB("Scaling Wall"),
    SITTING_WATER("Sitting on Ghat Steps"),
    COMBAT_GUARD("Defensive Guard / Parry"),
    COMBAT_ATTACK("Martial Arts Strike"),
    COMBAT_DODGE("Tactical Slip Evasion"),
    COMBAT_STAGGER("Stagger / Hit Reaction");

    public final String displayName;

    LocomotionState(String displayName) {
        this.displayName = displayName;
    }

    public boolean isMovingLocomotion() {
        return this == WALK || this == SPRINT || this == START_WALK ||
               this == START_SPRINT || this == CROUCH_WALK || this == WADING;
    }

    public boolean isAirborne() {
        return this == AIRBORNE_JUMP || this == AIRBORNE_FALL;
    }
}
