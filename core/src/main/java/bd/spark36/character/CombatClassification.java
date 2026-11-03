// ei file ta hostile opponent ar protected civilian er classification set kore
package bd.spark36.character;

public enum CombatClassification {
    PLAYER,
    PROTECTED_CIVILIAN,
    HOSTILE_OPPONENT;

    public boolean isCivilianProtected() {
        return this == PROTECTED_CIVILIAN;
    }

    public boolean isHostile() {
        return this == HOSTILE_OPPONENT;
    }
}
