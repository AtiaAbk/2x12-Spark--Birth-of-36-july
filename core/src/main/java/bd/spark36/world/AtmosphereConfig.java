// ei file ta akasher rong, fog ar daylight parameters configure kore
package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

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
        new Vector3(-0.45f, -0.75f, -0.45f),
        new Color(1.08f, 1.05f, 0.98f, 1.0f),
        new Color(0.37f, 0.39f, 0.41f, 1.0f),
        75.0f, 240.0f, 1.0f,

        new Color(0.303f, 0.338f, 0.378f, 1.0f),
        new Color(0.085f, 0.172f, 0.504f, 1.0f),
        new Color(0.141f, 0.254f, 0.652f, 1.0f),
        new Color(0.242f, 0.375f, 0.658f, 1.0f),
        new Color(0.350f, 0.473f, 0.661f, 1.0f),
        new Color(1.0f, 0.98f, 0.90f, 0.95f),
        new Color(1.0f, 0.94f, 0.78f, 0.28f),
        1.0f,
        0.0f,
        1.0f,
        1.0f,
        0.0f,
        0.0f
    );

    public static final Preset GOLDEN_HOUR = new Preset(
        new Vector3(-0.75f, -0.32f, -0.58f),
        new Color(1.24f, 0.92f, 0.65f, 1.0f),
        new Color(0.44f, 0.38f, 0.42f, 1.0f),
        65.0f, 200.0f, 1.1f,

        new Color(0.960f, 0.400f, 0.255f, 1.0f),
        new Color(0.036f, 0.055f, 0.141f, 1.0f),
        new Color(0.216f, 0.114f, 0.142f, 1.0f),
        new Color(1.000f, 0.242f, 0.116f, 1.0f),
        new Color(0.960f, 0.400f, 0.255f, 1.0f),
        new Color(1.0f, 0.88f, 0.58f, 0.98f),
        new Color(1.0f, 0.65f, 0.30f, 0.50f),
        1.35f,
        0.0f,
        1.2f,
        0.85f,
        0.22f,
        0.04f
    );

    public static final Preset OVERCAST_MONSOON = new Preset(
        new Vector3(-0.35f, -0.85f, -0.38f),
        new Color(0.70f, 0.74f, 0.78f, 1.0f),
        new Color(0.48f, 0.52f, 0.56f, 1.0f),
        16.0f, 95.0f, 1.2f,

        new Color(0.261f, 0.306f, 0.357f, 1.0f),
        new Color(0.087f, 0.100f, 0.113f, 1.0f),
        new Color(0.126f, 0.145f, 0.164f, 1.0f),
        new Color(0.182f, 0.207f, 0.235f, 1.0f),
        new Color(0.261f, 0.306f, 0.357f, 1.0f),
        new Color(0.85f, 0.88f, 0.90f, 0.25f),
        new Color(0.80f, 0.84f, 0.88f, 0.10f),
        0.20f,
        0.0f,
        2.2f,
        1.45f,
        -0.10f,
        -0.02f
    );

    public static final Preset MONSOON_RAIN = new Preset(
        new Vector3(-0.30f, -0.88f, -0.35f),
        new Color(0.52f, 0.58f, 0.62f, 1.0f),
        new Color(0.42f, 0.46f, 0.50f, 1.0f),
        10.0f, 75.0f, 1.3f,

        new Color(0.164f, 0.193f, 0.227f, 1.0f),
        new Color(0.062f, 0.075f, 0.090f, 1.0f),
        new Color(0.095f, 0.112f, 0.132f, 1.0f),
        new Color(0.128f, 0.156f, 0.175f, 1.0f),
        new Color(0.164f, 0.193f, 0.227f, 1.0f),
        new Color(0.70f, 0.75f, 0.80f, 0.12f),
        new Color(0.65f, 0.70f, 0.75f, 0.05f),
        0.05f,
        1.0f,
        2.6f,
        1.85f,
        -0.14f,
        -0.03f
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
