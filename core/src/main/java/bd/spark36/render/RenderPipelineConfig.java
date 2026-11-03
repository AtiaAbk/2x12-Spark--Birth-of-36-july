// ei file ta 3d render pipeline er settings ar buffer manage kore
package bd.spark36.render;

import com.badlogic.gdx.Gdx;

public final class RenderPipelineConfig {

    private RenderPipelineConfig() {}

    public static boolean pbrEnabled = getBooleanProperty("bd.spark36.pbr", true);

    public static boolean iblEnabled = getBooleanProperty("bd.spark36.ibl", true);

    public static boolean hdrEnabled = getBooleanProperty("bd.spark36.hdr", true);

    public static boolean fxaaEnabled = getBooleanProperty("bd.spark36.fxaa", true);

    public static int toneMappingMode = getIntProperty("bd.spark36.toneMapping", 1);

    public static float exposure = getFloatProperty("bd.spark36.exposure", 1.08f);

    public static float contrast = getFloatProperty("bd.spark36.contrast", 1.02f);

    public static float saturation = getFloatProperty("bd.spark36.saturation", 1.04f);

    public static float colorTemperature = getFloatProperty("bd.spark36.colorTemperature", 0.08f);

    public static float colorTint = getFloatProperty("bd.spark36.colorTint", -0.01f);

    public static float shadowLift = getFloatProperty("bd.spark36.shadowLift", 0.006f);

    public static float bloomThreshold = getFloatProperty("bd.spark36.bloomThreshold", 0.85f);

    public static float bloomStrength = getFloatProperty("bd.spark36.bloomStrength", 0.16f);

    public static float vignetteStrength = getFloatProperty("bd.spark36.vignetteStrength", 0.20f);
    public static float vignetteRadius = getFloatProperty("bd.spark36.vignetteRadius", 0.90f);
    public static float filmGrainStrength = getFloatProperty("bd.spark36.grain", 0.005f);

    public static final int IRRADIANCE_MAP_SIZE = 32;

    public static final int SPECULAR_MAP_LEVELS = 8;

    public static final int BRDF_LUT_SIZE = 256;

    public static final float DEFAULT_DIELECTRIC_METALLIC = 0.0f;

    public static final float DEFAULT_ROUGHNESS = 0.85f;

    public static final float DEFAULT_CONDUCTOR_METALLIC = 0.90f;

    public static final float DEFAULT_METAL_ROUGHNESS = 0.35f;

    public static GraphicsTier activeGraphicsTier = GraphicsTier.fromSystemProperty();

    private static final boolean TIER_SWITCH_GIVEN = System.getProperty("bd.spark36.graphicsTier") != null;
    private static final boolean TONEMAP_SWITCH_GIVEN = System.getProperty("bd.spark36.toneMapping") != null;

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

    public static void setGraphicsTier(GraphicsTier tier) {
        if (tier == null) return;
        activeGraphicsTier = tier;
        System.setProperty("bd.spark36.graphicsTier", tier.name());
    }

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

    public static void applyUserToneMapping(int mode) {
        if (TONEMAP_SWITCH_GIVEN) return;
        toneMappingMode = (mode >= 0 && mode <= 2) ? mode : defaultToneMappingMode;
    }

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
            toneMappingMode = 2;
        } else if (toneMappingMode == 2) {
            toneMappingMode = 0;
        } else {
            toneMappingMode = 1;
        }
        Runnable l = toneMappingListener;
        if (l != null) l.run();
        return getToneMappingName();
    }

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
