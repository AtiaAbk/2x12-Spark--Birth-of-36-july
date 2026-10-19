package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;

/**
 * Authoritative configuration parameters for the Dhaka University campus pond
 * (Curzon Hall pukur) water rendering and lily-pad buoyancy system.
 */
public final class WaterConfig {

    private WaterConfig() {}

    // ── Pond Spatial Boundaries (World Coordinates) ──────────────────────────
    /** Pond center coordinates in world space. */
    public static final float CENTER_X = 0.0f;
    public static final float CENTER_Z = 6.0f;

    /** Pond water surface dimensions (nested comfortably inside stone curbs). */
    public static final float WATER_WIDTH = 17.6f;   // X extent: [-8.8, 8.8]
    public static final float WATER_LENGTH = 23.6f;  // Z extent: [-5.8, 17.8]

    /** Equilibrium water surface height (Y level in world units). */
    public static final float BASE_WATER_HEIGHT = 0.12f;

    /** Pond silt bed level (Y level in world units). */
    public static final float POND_BED_HEIGHT = 0.01f;

    /** Shoreline dampening margin: within this distance of curbs, wave displacement smoothly fades to zero. */
    public static final float SHORE_MARGIN = 0.85f;

    // ── Multi-Layer Gerstner Wave Parameters ─────────────────────────────────
    // Wave 1: Primary gentle swell (gentle diagonal drift)
    public static final float WAVE1_AMP = 0.016f;
    public static final float WAVE1_LEN = 3.80f;
    public static final float WAVE1_SPEED = 0.35f;
    public static final float WAVE1_DIR_X = 0.80f;
    public static final float WAVE1_DIR_Z = 0.60f;
    public static final float WAVE1_STEEPNESS = 0.40f;

    // Wave 2: Secondary transverse swell (soft cross-wave)
    public static final float WAVE2_AMP = 0.009f;
    public static final float WAVE2_LEN = 2.20f;
    public static final float WAVE2_SPEED = 0.45f;
    public static final float WAVE2_DIR_X = -0.50f;
    public static final float WAVE2_DIR_Z = 0.866f;
    public static final float WAVE2_STEEPNESS = 0.35f;

    // Wave 3: Micro capillary ripple (gentle surface motion)
    public static final float WAVE3_AMP = 0.005f;
    public static final float WAVE3_LEN = 1.30f;
    public static final float WAVE3_SPEED = 0.55f;
    public static final float WAVE3_DIR_X = 0.30f;
    public static final float WAVE3_DIR_Z = -0.954f;
    public static final float WAVE3_STEEPNESS = 0.30f;

    /** Maximum theoretical wave displacement above/below base height (sum of amplitudes). */
    public static final float MAX_WAVE_DISPLACEMENT = WAVE1_AMP + WAVE2_AMP + WAVE3_AMP; // 0.030f

    // ── Optical & Shading Parameters ─────────────────────────────────────────
    /** Characteristic Dhaka University pond shallow water tint (clean sunlit teal). */
    public static final Color SHALLOW_COLOR = new Color(0.20f, 0.56f, 0.68f, 0.72f);

    /** Deep water body color (algae-tinted emerald teal). */
    public static final Color DEEP_COLOR = new Color(0.06f, 0.24f, 0.32f, 0.94f);

    /** Light extinction / depth absorption coefficient. */
    public static final float ABSORPTION_COEFF = 2.8f;

    /** Schlick Fresnel reflectance at normal incidence for water (IOR = 1.333). */
    public static final float FRESNEL_F0 = 0.020f;

    /** Sun specular highlight sharpness (shininess exponent). */
    public static final float SPECULAR_SHININESS = 256.0f;

    /** Specular highlight intensity multiplier. */
    public static final float SPECULAR_INTENSITY = 2.4f;

    // ── Lily-Pad Buoyancy Tuning ─────────────────────────────────────────────
    /** Vertical offset above calculated water surface to prevent z-fighting with water mesh. */
    public static final float LILY_PAD_VERTICAL_OFFSET = 0.010f;

    /** Maximum allowed tilt angle (degrees) for lily pads to prevent unnatural rolling. */
    public static final float LILY_PAD_MAX_TILT_DEG = 7.0f;

    /** Damping factor for smooth lily-pad bobbing and rotation (0 = instant, higher = snappier). */
    public static final float LILY_PAD_DAMPING = 8.5f;
}
