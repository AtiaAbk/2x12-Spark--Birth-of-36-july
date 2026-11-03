// ei file ta nirdosh student der marle fail howar rule check kore
package bd.spark36.level;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.CampusCrowd;

public final class CivilianStrikeRule {

    public static final float STRIKE_RANGE = 2.2f;

    public static final String STUDENT_VIOLATION =
        "VIOLATION: You struck an innocent campus student! Unprovoked violence against unarmed peers is strictly forbidden.";
    public static final String CIVILIAN_VIOLATION =
        "VIOLATION: Striking civilians is strictly forbidden!";

    private CivilianStrikeRule() {}

    public static String check(CampusCrowd crowd, Vector3 pos, float headingDeg, String violationMessage) {
        if (crowd == null || pos == null) return null;
        return crowd.checkHitByPlayer(pos, headingDeg, STRIKE_RANGE) ? violationMessage : null;
    }
}
