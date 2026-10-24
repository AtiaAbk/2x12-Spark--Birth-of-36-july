package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.CampusCrowd;

/**
 * Campus Peace Rule: striking an unarmed student or civilian disqualifies the player.
 * Shared by the campaign levels so the rule is identical everywhere and testable headlessly.
 */
public final class CivilianStrikeRule {

    /** Reach of a player strike used for the civilian check (metres). */
    public static final float STRIKE_RANGE = 2.2f;

    public static final String STUDENT_VIOLATION =
        "VIOLATION: You struck an innocent campus student! Unprovoked violence against unarmed peers is strictly forbidden.";
    public static final String CIVILIAN_VIOLATION =
        "VIOLATION: Striking civilians is strictly forbidden!";

    private CivilianStrikeRule() {}

    /**
     * @return the disqualification message if a strike from {@code pos} facing {@code headingDeg}
     *         would hit an unarmed crowd member, otherwise null.
     */
    public static String check(CampusCrowd crowd, Vector3 pos, float headingDeg, String violationMessage) {
        if (crowd == null || pos == null) return null;
        return crowd.checkHitByPlayer(pos, headingDeg, STRIKE_RANGE) ? violationMessage : null;
    }
}
