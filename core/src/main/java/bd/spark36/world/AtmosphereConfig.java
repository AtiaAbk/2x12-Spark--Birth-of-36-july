package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/**
 * Atmospheric parameters and configuration presets for all weather states in 2x12: Spark.
 * Calibrated for Dhaka University campus (Curzon Hall red brick, lush foliage, pond reflections)
 * under July-August Bangladesh climatic conditions.
 */
public final class AtmosphereConfig {

    public static final class Preset {
        public final Vector3 sunDirection;
        public final Color sunColor;
        public final Color ambientColor;
        public final float fogNear;
        public final float fogFar;
        public final float fogExponent;
        public final Color fogColor;
        public final Color skyZenith;
        public final Color skyMidBlue;
        public final Color skyHorizon;
        public final Color skyLowHaze;
        public final Color sunDiskColor;
        public final Color sunCoronaColor;
        public final float lightShaftOpacity;
        public final float rainIntensity;
        public final float cloudAlphaFactor;
        public final float windMultiplier;
        public final float colorTemperature;
        public final float colorTint;

        public Preset(Vector3 sunDirection, Color sunColor, Color ambientColor,
                      float fogNear, float fogFar, float fogExponent, Color fogColor,
                      Color skyZenith, Color skyMidBlue, Color skyHorizon, Color skyLowHaze,
                      Color sunDiskColor, Color sunCoronaColor,
                      float lightShaftOpacity, float rainIntensity, float cloudAlphaFactor, float windMultiplier,
                      float colorTemperature, float colorTint) {
            this.sunDirection = new Vector3(sunDirection).nor();
            this.sunColor = new Color(sunColor);
            this.ambientColor = new Color(ambientColor);
            this.fogNear = fogNear;
            this.fogFar = fogFar;
            this.fogExponent = fogExponent;
            this.fogColor = new Color(fogColor);
            this.skyZenith = new Color(skyZenith);
            this.skyMidBlue = new Color(skyMidBlue);
            this.skyHorizon = new Color(skyHorizon);
            this.skyLowHaze = new Color(skyLowHaze);
            this.sunDiskColor = new Color(sunDiskColor);
            this.sunCoronaColor = new Color(sunCoronaColor);
            this.lightShaftOpacity = lightShaftOpacity;
            this.rainIntensity = rainIntensity;
            this.cloudAlphaFactor = cloudAlphaFactor;
            this.windMultiplier = windMultiplier;
            this.colorTemperature = colorTemperature;
            this.colorTint = colorTint;
        }

        public Preset(Vector3 sunDirection, Color sunColor, Color ambientColor,
                      float fogNear, float fogFar, float fogExponent, Color fogColor,
                      Color skyZenith, Color skyMidBlue, Color skyHorizon, Color skyLowHaze,
                      Color sunDiskColor, Color sunCoronaColor,
                      float lightShaftOpacity, float rainIntensity, float cloudAlphaFactor, float windMultiplier) {
            this(sunDirection, sunColor, ambientColor, fogNear, fogFar, fogExponent, fogColor,
                 skyZenith, skyMidBlue, skyHorizon, skyLowHaze, sunDiskColor, sunCoronaColor,
                 lightShaftOpacity, rainIntensity, cloudAlphaFactor, windMultiplier, 0.0f, 0.0f);
        }
    }

    public static final Preset CLEAR_SUNNY = new Preset(
        new Vector3(-0.45f, -0.75f, -0.45f),        // Summer midday sun
        new Color(1.08f, 1.05f, 0.98f, 1.0f),       // Crisp warm summer sunlight
        new Color(0.38f, 0.40f, 0.35f, 1.0f),       // Warm daylight ambient with lush green/terracotta bounce
        75.0f, 240.0f, 1.0f,                         // Natural summer atmospheric depth (no milky near blanching)
        new Color(0.74f, 0.78f, 0.80f, 1.0f),       // Warm summer atmospheric haze (soft humidity depth)
        new Color(0.24f, 0.42f, 0.66f, 1.0f),       // Filmic slate-azure zenith
        new Color(0.44f, 0.60f, 0.76f, 1.0f),       // Soft atmospheric summer mid sky
        new Color(0.68f, 0.78f, 0.86f, 1.0f),       // Pearlescent horizon
        new Color(0.78f, 0.82f, 0.86f, 1.0f),       // Lower haze
        new Color(1.0f, 0.98f, 0.90f, 0.95f),        // Intense white-gold solar disk
        new Color(1.0f, 0.94f, 0.78f, 0.28f),        // Warm solar corona
        0.0f,                                       // No artificial god rays / shafts
        0.0f,                                       // No rain
        1.0f,                                       // Standard fair weather clouds
        1.0f,                                       // Standard breeze
        0.0f,                                       // Base temperature handled in RenderPipelineConfig
        0.0f                                        // Base tint handled in RenderPipelineConfig
    );

    public static final Preset GOLDEN_HOUR = new Preset(
        new Vector3(-0.75f, -0.32f, -0.58f),        // Low late afternoon / sunset angle
        new Color(1.24f, 0.92f, 0.65f, 1.0f),       // Rich golden amber sunlight
        new Color(0.44f, 0.38f, 0.42f, 1.0f),       // Violet-warm fill bounce
        65.0f, 200.0f, 1.1f,                         // Warm atmospheric distance scattering
        new Color(0.92f, 0.78f, 0.65f, 1.0f),       // Warm amber horizon mist
        new Color(0.20f, 0.28f, 0.52f, 1.0f),       // Deep twilight azure zenith
        new Color(0.65f, 0.45f, 0.50f, 1.0f),       // Dusky rose-purple mid sky
        new Color(0.95f, 0.65f, 0.40f, 1.0f),       // Brilliant amber-gold horizon
        new Color(0.92f, 0.78f, 0.65f, 1.0f),       // Golden low haze
        new Color(1.0f, 0.88f, 0.58f, 0.98f),        // Blazing amber sun disk
        new Color(1.0f, 0.65f, 0.30f, 0.50f),        // Broad radiant sunset corona
        1.35f,                                      // Pronounced dramatic shafts
        0.0f,                                       // No rain
        1.2f,                                       // Golden illuminated clouds
        0.85f,                                      // Calmer evening breeze
        0.22f,                                      // Warm golden color temperature
        0.04f                                       // Subtle warm magenta tint
    );

    public static final Preset OVERCAST_MONSOON = new Preset(
        new Vector3(-0.35f, -0.85f, -0.38f),        // Diffuse filtered overhead light
        new Color(0.70f, 0.74f, 0.78f, 1.0f),       // Cool diffuse overcast daylight
        new Color(0.48f, 0.52f, 0.56f, 1.0f),       // High ambient fill, soft shadows
        16.0f, 95.0f, 1.2f,                          // Thick monsoon humidity fog
        new Color(0.68f, 0.72f, 0.76f, 1.0f),       // Slate monsoon mist
        new Color(0.38f, 0.42f, 0.46f, 1.0f),       // Slate gray zenith
        new Color(0.48f, 0.52f, 0.56f, 1.0f),       // Cool dense cloud mid band
        new Color(0.58f, 0.62f, 0.66f, 1.0f),       // Overcast horizon
        new Color(0.68f, 0.72f, 0.76f, 1.0f),       // Fogged ground horizon
        new Color(0.85f, 0.88f, 0.90f, 0.25f),      // Filtered cloud-occluded sun disk
        new Color(0.80f, 0.84f, 0.88f, 0.10f),      // Dim diffuse corona
        0.20f,                                      // Faint diffused light shafts
        0.0f,                                       // Heavy clouds before rain
        2.2f,                                       // Dense cloud cover
        1.45f,                                      // Gusty pre-storm wind
        -0.10f,                                     // Cool overcast temperature
        -0.02f                                      // Faint overcast cool tint
    );

    public static final Preset MONSOON_RAIN = new Preset(
        new Vector3(-0.30f, -0.88f, -0.35f),        // Overhead storm light
        new Color(0.52f, 0.58f, 0.62f, 1.0f),       // Dim rainy illumination
        new Color(0.42f, 0.46f, 0.50f, 1.0f),       // Cool rainy ambient
        10.0f, 75.0f, 1.3f,                          // Dense downpour fog & precipitation haze
        new Color(0.55f, 0.60f, 0.65f, 1.0f),       // Dark rainy mist
        new Color(0.30f, 0.35f, 0.40f, 1.0f),       // Storm cloud zenith
        new Color(0.40f, 0.45f, 0.50f, 1.0f),       // Heavy rain cloud layer
        new Color(0.48f, 0.54f, 0.58f, 1.0f),       // Rainy horizon
        new Color(0.55f, 0.60f, 0.65f, 1.0f),       // Ground splash mist
        new Color(0.70f, 0.75f, 0.80f, 0.12f),      // Barely visible occluded sun
        new Color(0.65f, 0.70f, 0.75f, 0.05f),      // Subtle wet sky glow
        0.05f,                                      // Minimal direct rays
        1.0f,                                       // Active monsoon downpour
        2.6f,                                       // Overcast sky filled with storm clouds
        1.85f,                                      // Strong monsoon wind gusting
        -0.14f,                                     // Cool rainy temperature
        -0.03f                                      // Moody storm tint
    );

    public static Preset getPreset(WeatherState state) {
        if (state == null) return CLEAR_SUNNY;
        switch (state) {
            case GOLDEN_HOUR: return GOLDEN_HOUR;
            case OVERCAST_MONSOON: return OVERCAST_MONSOON;
            case MONSOON_RAIN: return MONSOON_RAIN;
            case CLEAR_SUNNY:
            default:
                return CLEAR_SUNNY;
        }
    }

    private AtmosphereConfig() {}
}
