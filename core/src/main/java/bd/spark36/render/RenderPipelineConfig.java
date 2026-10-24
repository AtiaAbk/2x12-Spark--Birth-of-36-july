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
    public static float exposure = getFloatProperty("bd.spark36.exposure", 1.08f);

    /** Midpoint-anchored image contrast. */
    public static float contrast = getFloatProperty("bd.spark36.contrast", 1.02f);

    /**
     * Color saturation multiplier (1.0 = neutral, 1.04 = mild vibrance). The per-channel ACES curve
     * already adds saturation in the darks, so values above ~1.10 crush blue out of foliage
     * (yellow-green) and push brick to pure red (T8 colour-cast fix).
     */
    public static float saturation = getFloatProperty("bd.spark36.saturation", 1.04f);

    /** White balance temperature shift (-1.0 to 1.0, 0.08 = slightly warm; the sun colour carries the warmth). */
    public static float colorTemperature = getFloatProperty("bd.spark36.colorTemperature", 0.08f);

    /** White balance tint shift (-1.0 to 1.0, positive = green; -0.01 = faint magenta offsetting lawn/canopy bounce). */
    public static float colorTint = getFloatProperty("bd.spark36.colorTint", -0.01f);

    /** Shadow detail recovery / lift (prevents crushed blacks in verandas & foliage without blanching). */
    public static float shadowLift = getFloatProperty("bd.spark36.shadowLift", 0.006f);

    // ── Bloom Configuration ───────────────────────────────────────────────
    /** Minimum luminance threshold to trigger HDR bloom extraction (values in linear space). */
    public static float bloomThreshold = getFloatProperty("bd.spark36.bloomThreshold", 0.85f);

    /** Additive bloom composite strength (natural soft bloom, avoids milky veil). */
    public static float bloomStrength = getFloatProperty("bd.spark36.bloomStrength", 0.16f);

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

    public static GraphicsTier activeGraphicsTier = GraphicsTier.fromSystemProperty();

    /** True when the tier / tone map was forced on the command line; those switches beat saved settings. */
    private static final boolean TIER_SWITCH_GIVEN = System.getProperty("bd.spark36.graphicsTier") != null;
    private static final boolean TONEMAP_SWITCH_GIVEN = System.getProperty("bd.spark36.toneMapping") != null;

    /** Values after the command line and assets/visuals.properties, before any saved user setting. */
    private static GraphicsTier defaultGraphicsTier;
    private static int defaultToneMappingMode;

    private static Runnable toneMappingListener;

    static {
        loadVisualsProperties();
        defaultGraphicsTier = activeGraphicsTier;
        defaultToneMappingMode = toneMappingMode;
    }

    public static GraphicsTier getDefaultGraphicsTier() {
        return defaultGraphicsTier;
    }

    public static int getDefaultToneMappingMode() {
        return defaultToneMappingMode;
    }

    /**
     * Makes {@code tier} the active tier. Most render systems read the tier through
     * {@link GraphicsTier#fromSystemProperty()}, so the property is updated too; before this, the
     * visuals.properties tier was shown in the pause menu but never reached those systems.
     * Per-frame consumers (vegetation, water, particles, post) follow at once; the shadow-map size
     * and terrain grid are fixed when a world is built, so they follow on the next level load / restart.
     */
    public static void setGraphicsTier(GraphicsTier tier) {
        if (tier == null) return;
        activeGraphicsTier = tier;
        System.setProperty("bd.spark36.graphicsTier", tier.name());
    }

    /**
     * Applies a saved user tier ("LOW"/"MEDIUM"/"HIGH"); null restores the default. A
     * {@code -Dbd.spark36.graphicsTier} switch given at launch always wins.
     */
    public static void applyUserGraphicsTier(String tierName) {
        if (TIER_SWITCH_GIVEN) return;
        GraphicsTier tier = defaultGraphicsTier;
        if (tierName != null) {
            try {
                tier = GraphicsTier.valueOf(tierName.trim().toUpperCase());
            } catch (Exception ignored) {}
        }
        setGraphicsTier(tier);
    }

    /** Applies a saved tone map (0..2); -1 restores the default. A launch switch always wins. */
    public static void applyUserToneMapping(int mode) {
        if (TONEMAP_SWITCH_GIVEN) return;
        toneMappingMode = (mode >= 0 && mode <= 2) ? mode : defaultToneMappingMode;
    }

    /** Called after {@link #cycleToneMapping()} so a hotkey change is persisted. */
    public static void setToneMappingListener(Runnable listener) {
        toneMappingListener = listener;
    }

    private static void loadVisualsProperties() {
        try {
            java.io.File file = new java.io.File("assets/visuals.properties");
            if (!file.exists()) {
                file = new java.io.File("visuals.properties");
            }
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    props.load(fis);
                }
                exposure = getFloat(props, "exposure", exposure);
                contrast = getFloat(props, "contrast", contrast);
                saturation = getFloat(props, "saturation", saturation);
                colorTemperature = getFloat(props, "colorTemperature", colorTemperature);
                colorTint = getFloat(props, "colorTint", colorTint);
                shadowLift = getFloat(props, "shadowLift", shadowLift);
                bloomStrength = getFloat(props, "bloomStrength", bloomStrength);
                bloomThreshold = getFloat(props, "bloomThreshold", bloomThreshold);
                vignetteStrength = getFloat(props, "vignetteStrength", vignetteStrength);
                vignetteRadius = getFloat(props, "vignetteRadius", vignetteRadius);
                filmGrainStrength = getFloat(props, "filmGrainStrength", filmGrainStrength);
                toneMappingMode = getInt(props, "toneMappingMode", toneMappingMode);
                pbrEnabled = getBool(props, "pbrEnabled", pbrEnabled);
                iblEnabled = getBool(props, "iblEnabled", iblEnabled);
                hdrEnabled = getBool(props, "hdrEnabled", hdrEnabled);
                fxaaEnabled = getBool(props, "fxaaEnabled", fxaaEnabled);

                // An explicit -Dbd.spark36.graphicsTier switch wins over the file default.
                String tierStr = props.getProperty("graphicsTier");
                if (tierStr != null && System.getProperty("bd.spark36.graphicsTier") == null) {
                    try {
                        activeGraphicsTier = GraphicsTier.valueOf(tierStr.trim().toUpperCase());
                    } catch (Exception ignored) {}
                }
            }
        } catch (Throwable t) {
            if (Gdx.app != null) {
                Gdx.app.error("RenderPipelineConfig", "Failed to load visuals.properties: " + t.getMessage());
            }
        }
    }

    private static float getFloat(java.util.Properties props, String key, float def) {
        String v = props.getProperty(key);
        if (v != null) {
            try { return Float.parseFloat(v.trim()); } catch (Exception ignored) {}
        }
        return def;
    }

    private static int getInt(java.util.Properties props, String key, int def) {
        String v = props.getProperty(key);
        if (v != null) {
            try { return Integer.parseInt(v.trim()); } catch (Exception ignored) {}
        }
        return def;
    }

    private static boolean getBool(java.util.Properties props, String key, boolean def) {
        String v = props.getProperty(key);
        if (v != null) {
            return Boolean.parseBoolean(v.trim());
        }
        return def;
    }

    public static String cycleGraphicsTier() {
        if (activeGraphicsTier == GraphicsTier.HIGH) {
            setGraphicsTier(GraphicsTier.LOW);
        } else if (activeGraphicsTier == GraphicsTier.LOW) {
            setGraphicsTier(GraphicsTier.MEDIUM);
        } else {
            setGraphicsTier(GraphicsTier.HIGH);
        }
        return activeGraphicsTier.displayName;
    }

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
        Runnable l = toneMappingListener;
        if (l != null) l.run();
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
                if (Gdx.app != null) {
                    Gdx.app.error("RenderPipelineConfig", "Invalid float for property " + key + ": " + val);
                }
            }
        }
        return defaultValue;
    }
}
