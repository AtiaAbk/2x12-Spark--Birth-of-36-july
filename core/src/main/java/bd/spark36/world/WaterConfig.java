// ei file ta panir dheu, shosshota ar rong configure kore
package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;

public final class WaterConfig {

    private WaterConfig() {}

    public static final float CENTER_X = 0.0f;
    public static final float CENTER_Z = 6.0f;

    public static final float WATER_WIDTH = 17.6f;
    public static final float WATER_LENGTH = 23.6f;

    public static final float BASE_WATER_HEIGHT = 0.12f;

    public static final float POND_BED_HEIGHT = 0.01f;

    public static final float SHORE_MARGIN = 0.85f;

    public static final float WAVE1_AMP = 0.016f;
    public static final float WAVE1_LEN = 3.80f;
    public static final float WAVE1_SPEED = 0.35f;
    public static final float WAVE1_DIR_X = 0.80f;
    public static final float WAVE1_DIR_Z = 0.60f;
    public static final float WAVE1_STEEPNESS = 0.40f;

    public static final float WAVE2_AMP = 0.009f;
    public static final float WAVE2_LEN = 2.20f;
    public static final float WAVE2_SPEED = 0.45f;
    public static final float WAVE2_DIR_X = -0.50f;
    public static final float WAVE2_DIR_Z = 0.866f;
    public static final float WAVE2_STEEPNESS = 0.35f;

    public static final float WAVE3_AMP = 0.005f;
    public static final float WAVE3_LEN = 1.30f;
    public static final float WAVE3_SPEED = 0.55f;
    public static final float WAVE3_DIR_X = 0.30f;
    public static final float WAVE3_DIR_Z = -0.954f;
    public static final float WAVE3_STEEPNESS = 0.30f;

    public static final float MAX_WAVE_DISPLACEMENT = WAVE1_AMP + WAVE2_AMP + WAVE3_AMP;

    public static final Color SHALLOW_COLOR = new Color(0.08f, 0.42f, 0.48f, 0.86f);

    public static final Color DEEP_COLOR = new Color(0.02f, 0.15f, 0.20f, 0.98f);

    public static final float ABSORPTION_COEFF = 2.8f;

    public static final float FRESNEL_F0 = 0.020f;

    public static final float SPECULAR_SHININESS = 256.0f;

    public static final float SPECULAR_INTENSITY = 2.4f;

    public static final float LILY_PAD_VERTICAL_OFFSET = 0.010f;

    public static final float LILY_PAD_MAX_TILT_DEG = 7.0f;

    public static final float LILY_PAD_DAMPING = 8.5f;
}
