package bd.spark36.render;

import com.badlogic.gdx.Gdx;

/**
 * Centralized configuration and tuning parameters for the Spark 36 July PBR, IBL, HDR,
 * and post-processing render pipeline.
 *
 * <p>Avoids hardcoded duplicate settings across engine classes and supports dynamic system
 * property overrides for test automation and performance scaling.</p>
 */
public final class RenderPipelineConfig {

    private RenderPipelineConfig() {}

    // ── Pipeline Feature Toggles ──────────────────────────────────────────
    /** Whether metallic-roughness PBR shaders are enabled. Toggle with -Dbd.spark36.pbr=true/false */
    public static boolean pbrEnabled = getBooleanProperty("bd.spark36.pbr", true);

    /** Whether Image-Based Lighting (irradiance + prefiltered radiance cubemaps) is enabled. */
    public static boolean iblEnabled = getBooleanProperty("bd.spark36.ibl", true);

    /** Whether floating-point HDR framebuffer (RGBA16F) is preferred. Toggle with -Dbd.spark36.hdr=true/false */
    public static boolean hdrEnabled = getBooleanProperty("bd.spark36.hdr", true);

    /** Whether FXAA edge-smoothing anti-aliasing is enabled. Toggle with -Dbd.spark36.fxaa=true/false */
    public static boolean fxaaEnabled = getBooleanProperty("bd.spark36.fxaa", true);

    /** Tone mapping mode: 1 = ACES Filmic, 2 = Extended Reinhard, 0 = Linear Clamp. Toggle with -Dbd.spark36.toneMapping=N */
    public static int toneMappingMode = getIntProperty("bd.spark36.toneMapping", 1);

    // ── Exposure & Color Grading ──────────────────────────────────────────
    /** Base photographic exposure multiplier (linear scale, 1.0 = baseline daylight). */
    public static float exposure = getFloatProperty("bd.spark36.exposure", 1.0f);

    /** Midpoint-anchored image contrast. */
    public static float contrast = getFloatProperty("bd.spark36.contrast", 1.04f);

    /** Color saturation multiplier (1.0 = neutral, 1.04 = subtle natural summer vibrance). */
    public static float saturation = getFloatProperty("bd.spark36.saturation", 1.04f);

    /** White balance temperature shift (-1.0 to 1.0). */
    public static float colorTemperature = getFloatProperty("bd.spark36.colorTemperature", 0.0f);

    /** White balance tint shift (-1.0 to 1.0). */
    public static float colorTint = getFloatProperty("bd.spark36.colorTint", 0.0f);

    /** Shadow detail recovery / lift (prevents crushed blacks in verandas & foliage). */
    public static float shadowLift = getFloatProperty("bd.spark36.shadowLift", 0.02f);

    // ── Bloom Configuration ───────────────────────────────────────────────
    /** Minimum luminance threshold to trigger HDR bloom extraction (values in linear space). */
    public static float bloomThreshold = getFloatProperty("bd.spark36.bloomThreshold", 0.80f);

    /** Additive bloom composite strength. */
    public static float bloomStrength = getFloatProperty("bd.spark36.bloomStrength", 0.20f);

    // ── Vignette & Film Optics ───────────────────────────────────────────
    public static float vignetteStrength = getFloatProperty("bd.spark36.vignetteStrength", 0.20f);
    public static float vignetteRadius = getFloatProperty("bd.spark36.vignetteRadius", 0.90f);
    public static float filmGrainStrength = getFloatProperty("bd.spark36.grain", 0.005f);

    // ── IBL Cubemap Dimensions ────────────────────────────────────────────
    /** Diffuse irradiance cubemap face size (32x32 target). */
    public static final int IRRADIANCE_MAP_SIZE = 32;

    /** Specular prefiltered environment map mip levels (equivalent to 256x256 base face). */
    public static final int SPECULAR_MAP_LEVELS = 8;

    /** BRDF 2D integration lookup table dimension (256x256 target). */
    public static final int BRDF_LUT_SIZE = 256;

    // ── Material PBR Defaults ─────────────────────────────────────────────
    /** Default dielectric metallic factor (non-conductors like brick, concrete, soil, foliage). */
    public static final float DEFAULT_DIELECTRIC_METALLIC = 0.0f;

    /** Default non-metal surface roughness (matte brick / stone). */
    public static final float DEFAULT_ROUGHNESS = 0.85f;

    /** Default metal conductor metallic factor (cast iron, brass). */
    public static final float DEFAULT_CONDUCTOR_METALLIC = 0.90f;

    /** Default smooth metal roughness. */
    public static final float DEFAULT_METAL_ROUGHNESS = 0.35f;

    public static String getToneMappingName() {
        if (toneMappingMode == 1) return "ACES Filmic (Cinematic)";
        if (toneMappingMode == 2) return "Extended Reinhard";
        return "Linear / Bypass";
    }

    public static String cycleToneMapping() {
        if (toneMappingMode == 1) {
            toneMappingMode = 2; // Reinhard
        } else if (toneMappingMode == 2) {
            toneMappingMode = 0; // Linear / Bypass
        } else {
            toneMappingMode = 1; // ACES Filmic
        }
        return getToneMappingName();
    }

    // ── Helper Accessors ──────────────────────────────────────────────────
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String val = System.getProperty(key);
        return val != null ? Boolean.parseBoolean(val.trim()) : defaultValue;
    }

    private static int getIntProperty(String key, int defaultValue) {
        String val = System.getProperty(key);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static float getFloatProperty(String key, float defaultValue) {
        String val = System.getProperty(key);
        if (val != null) {
            try {
                return Float.parseFloat(val.trim());
            } catch (NumberFormatException e) {
                Gdx.app.error("RenderPipelineConfig", "Invalid float for property " + key + ": " + val);
            }
        }
        return defaultValue;
    }
}
