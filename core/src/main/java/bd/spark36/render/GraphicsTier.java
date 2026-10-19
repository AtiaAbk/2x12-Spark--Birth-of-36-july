package bd.spark36.render;

/**
 * Graphics quality presets tuned for hardware profiles (Low, Medium, High).
 * Medium is the target default for fanless Apple Silicon MacBook Air M4 (60 FPS lock).
 */
public enum GraphicsTier {
    LOW(1024, 0.0f, false, false, false, true, 0.5f, 40.0f, false, false, 10.0f, 80, false, true, 0, 0.0f, "Low (Maximum Performance)"),
    MEDIUM(2048, 0.22f, true, true, true, true, 1.0f, 65.0f, true, true, 5.0f, 180, true, true, 1, 0.0035f, "Medium (Recommended for M4 Air)"),
    HIGH(4096, 0.28f, true, true, true, true, 1.25f, 85.0f, true, true, 4.0f, 300, true, true, 2, 0.0055f, "High (Cinematic Quality)");

    public final int shadowMapSize;
    public final float bloomStrength;
    public final boolean enableBloom;
    public final boolean enableChromaticAberration;
    public final boolean enableHdr;
    public final boolean enablePbr;
    public final float vegetationDensity;
    public final float vegetationLodDistance;
    public final boolean enableLayeredWind;
    public final boolean enableTerrainTransitions;
    public final float terrainGridResolution;
    public final int maxRainParticles;
    public final boolean enableVolumetricShafts;
    public final boolean enableFxaa;
    public final int bloomPasses;
    public final float filmGrain;
    public final String displayName;

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration,
                 boolean enableHdr, boolean enablePbr, float vegetationDensity, float vegetationLodDistance,
                 boolean enableLayeredWind, boolean enableTerrainTransitions, float terrainGridResolution,
                 int maxRainParticles, boolean enableVolumetricShafts,
                 boolean enableFxaa, int bloomPasses, float filmGrain, String displayName) {
        this.shadowMapSize = shadowMapSize;
        this.bloomStrength = bloomStrength;
        this.enableBloom = enableBloom;
        this.enableChromaticAberration = enableChromaticAberration;
        this.enableHdr = enableHdr;
        this.enablePbr = enablePbr;
        this.vegetationDensity = vegetationDensity;
        this.vegetationLodDistance = vegetationLodDistance;
        this.enableLayeredWind = enableLayeredWind;
        this.enableTerrainTransitions = enableTerrainTransitions;
        this.terrainGridResolution = terrainGridResolution;
        this.maxRainParticles = maxRainParticles;
        this.enableVolumetricShafts = enableVolumetricShafts;
        this.enableFxaa = enableFxaa;
        this.bloomPasses = bloomPasses;
        this.filmGrain = filmGrain;
        this.displayName = displayName;
    }

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration,
                 boolean enableHdr, boolean enablePbr, float vegetationDensity, float vegetationLodDistance,
                 boolean enableLayeredWind, boolean enableTerrainTransitions, float terrainGridResolution,
                 int maxRainParticles, boolean enableVolumetricShafts, String displayName) {
        this(shadowMapSize, bloomStrength, enableBloom, enableChromaticAberration, enableHdr, enablePbr,
             vegetationDensity, vegetationLodDistance, enableLayeredWind, enableTerrainTransitions, terrainGridResolution,
             maxRainParticles, enableVolumetricShafts, true, enableBloom ? 1 : 0, 0.0035f, displayName);
    }

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration,
                 boolean enableHdr, boolean enablePbr, float vegetationDensity, float vegetationLodDistance,
                 boolean enableLayeredWind, boolean enableTerrainTransitions, float terrainGridResolution, String displayName) {
        this(shadowMapSize, bloomStrength, enableBloom, enableChromaticAberration, enableHdr, enablePbr,
             vegetationDensity, vegetationLodDistance, enableLayeredWind, enableTerrainTransitions, terrainGridResolution,
             180, true, displayName);
    }

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration,
                 boolean enableHdr, boolean enablePbr, float vegetationDensity, float vegetationLodDistance,
                 boolean enableLayeredWind, String displayName) {
        this(shadowMapSize, bloomStrength, enableBloom, enableChromaticAberration, enableHdr, enablePbr,
             vegetationDensity, vegetationLodDistance, enableLayeredWind, true, 5.0f, displayName);
    }

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration,
                 boolean enableHdr, boolean enablePbr, String displayName) {
        this(shadowMapSize, bloomStrength, enableBloom, enableChromaticAberration, enableHdr, enablePbr, 1.0f, 65.0f, true, true, 5.0f, displayName);
    }

    GraphicsTier(int shadowMapSize, float bloomStrength, boolean enableBloom, boolean enableChromaticAberration, String displayName) {
        this(shadowMapSize, bloomStrength, enableBloom, enableChromaticAberration, true, true, 1.0f, 65.0f, true, true, 5.0f, displayName);
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
