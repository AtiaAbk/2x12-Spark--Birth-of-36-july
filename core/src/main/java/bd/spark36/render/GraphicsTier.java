package bd.spark36.render;

/**
 * Graphics quality presets tuned for hardware profiles (Low, Medium, High).
 * Medium is the target default for fanless Apple Silicon MacBook Air M4 (60 FPS lock).
 */
public enum GraphicsTier {
    LOW(1024, 0.15f, false, false, "Low (Maximum Performance)"),
    MEDIUM(2048, 0.24f, true, true, "Medium (Recommended for M4 Air)"),
    HIGH(4096, 0.32f, true, true, "High (Cinematic Quality)");

    public final int shadowMapSize;
    public final float bloomStrength;
    public final boolean enableBloom;
    public final boolean enableChromaticAberration;
    public final String displayName;

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration, String displayName) {
        this.shadowMapSize = shadowMapSize;
        this.bloomStrength = bloomStrength;
        this.enableBloom = enableBloom;
        this.enableChromaticAberration = enableChromaticAberration;
        this.displayName = displayName;
    }

    public static GraphicsTier fromSystemProperty() {
        String prop = System.getProperty("bd.spark36.graphicsTier");
        if (prop != null) {
            for (GraphicsTier tier : values()) {
                if (tier.name().equalsIgnoreCase(prop.trim())) {
                    return tier;
                }
            }
        }
        return MEDIUM; // Default tier
    }
}
