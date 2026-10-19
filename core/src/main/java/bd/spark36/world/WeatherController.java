package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Real-time dynamic weather state machine and atmospheric parameter interpolator.
 * Handles smooth, delta-time independent blending between weather states (Clear Sunny,
 * Golden Hour Sunset, Overcast Monsoon, and Monsoon Rain) across sun angle, color,
 * native PBR distance fog, sky gradient bands, volumetric shafts, and precipitation.
 */
public class WeatherController {

    private WeatherState currentState = WeatherState.CLEAR_SUNNY;
    private WeatherState targetState = WeatherState.CLEAR_SUNNY;
    private float transitionProgress = 1.0f;
    private float transitionDuration = 4.0f;

    // Snapshot of source values when transition starts
    private final Vector3 srcSunDir = new Vector3();
    private final Color srcSunColor = new Color();
    private final Color srcAmbientColor = new Color();
    private float srcFogNear;
    private float srcFogFar;
    private float srcFogExponent;
    private final Color srcFogColor = new Color();
    private final Color srcSkyZenith = new Color();
    private final Color srcSkyMidBlue = new Color();
    private final Color srcSkyHorizon = new Color();
    private final Color srcSkyLowHaze = new Color();
    private final Color srcSunDiskColor = new Color();
    private final Color srcSunCoronaColor = new Color();
    private float srcLightShaftOpacity;
    private float srcRainIntensity;
    private float srcCloudAlphaFactor;
    private float srcWindMultiplier;
    private float srcTemperature;
    private float srcTint;

    // Target values
    private final Vector3 tgtSunDir = new Vector3();
    private final Color tgtSunColor = new Color();
    private final Color tgtAmbientColor = new Color();
    private float tgtFogNear;
    private float tgtFogFar;
    private float tgtFogExponent;
    private final Color tgtFogColor = new Color();
    private final Color tgtSkyZenith = new Color();
    private final Color tgtSkyMidBlue = new Color();
    private final Color tgtSkyHorizon = new Color();
    private final Color tgtSkyLowHaze = new Color();
    private final Color tgtSunDiskColor = new Color();
    private final Color tgtSunCoronaColor = new Color();
    private float tgtLightShaftOpacity;
    private float tgtRainIntensity;
    private float tgtCloudAlphaFactor;
    private float tgtWindMultiplier;
    private float tgtTemperature;
    private float tgtTint;

    // Active interpolated values
    private final Vector3 currentSunDir = new Vector3();
    private final Color currentSunColor = new Color();
    private final Color currentAmbientColor = new Color();
    private float currentFogNear;
    private float currentFogFar;
    private float currentFogExponent;
    private final Color currentFogColor = new Color();
    private final Color currentSkyZenith = new Color();
    private final Color currentSkyMidBlue = new Color();
    private final Color currentSkyHorizon = new Color();
    private final Color currentSkyLowHaze = new Color();
    private final Color currentSunDiskColor = new Color();
    private final Color currentSunCoronaColor = new Color();
    private float currentLightShaftOpacity;
    private float currentRainIntensity;
    private float currentCloudAlphaFactor;
    private float currentWindMultiplier;
    private float currentTemperature;
    private float currentTint;

    public WeatherController() {
        WeatherState initial = parseInitialWeatherProperty();
        setWeather(initial, 0f);
    }

    public WeatherController(WeatherState initialState) {
        setWeather(initialState != null ? initialState : WeatherState.CLEAR_SUNNY, 0f);
    }

    private static WeatherState parseInitialWeatherProperty() {
        String prop = System.getProperty("bd.spark36.weather");
        if (prop != null) {
            String p = prop.trim().toLowerCase();
            if (p.contains("gold") || p.contains("sunset")) return WeatherState.GOLDEN_HOUR;
            if (p.contains("overcast") || p.contains("cloud")) return WeatherState.OVERCAST_MONSOON;
            if (p.contains("rain") || p.contains("storm") || p.contains("monsoon")) return WeatherState.MONSOON_RAIN;
            if (p.contains("clear") || p.contains("sun")) return WeatherState.CLEAR_SUNNY;
        }
        return WeatherState.CLEAR_SUNNY;
    }

    /**
     * Initiates a smooth transition toward target weather.
     * @param target Desired weather state.
     * @param durationSeconds Transition time in seconds (<=0 snaps immediately).
     */
    public void setWeather(WeatherState target, float durationSeconds) {
        if (target == null) target = WeatherState.CLEAR_SUNNY;

        if (durationSeconds <= 0f) {
            this.currentState = target;
            this.targetState = target;
            this.transitionProgress = 1.0f;
            this.transitionDuration = 1.0f;
            applyPresetInstant(AtmosphereConfig.getPreset(target));
            return;
        }

        // Store current interpolated values as new source
        srcSunDir.set(currentSunDir);
        srcSunColor.set(currentSunColor);
        srcAmbientColor.set(currentAmbientColor);
        srcFogNear = currentFogNear;
        srcFogFar = currentFogFar;
        srcFogExponent = currentFogExponent;
        srcFogColor.set(currentFogColor);
        srcSkyZenith.set(currentSkyZenith);
        srcSkyMidBlue.set(currentSkyMidBlue);
        srcSkyHorizon.set(currentSkyHorizon);
        srcSkyLowHaze.set(currentSkyLowHaze);
        srcSunDiskColor.set(currentSunDiskColor);
        srcSunCoronaColor.set(currentSunCoronaColor);
        srcLightShaftOpacity = currentLightShaftOpacity;
        srcRainIntensity = currentRainIntensity;
        srcCloudAlphaFactor = currentCloudAlphaFactor;
        srcWindMultiplier = currentWindMultiplier;
        srcTemperature = currentTemperature;
        srcTint = currentTint;

        // Load target preset
        AtmosphereConfig.Preset p = AtmosphereConfig.getPreset(target);
        tgtSunDir.set(p.sunDirection);
        tgtSunColor.set(p.sunColor);
        tgtAmbientColor.set(p.ambientColor);
        tgtFogNear = p.fogNear;
        tgtFogFar = p.fogFar;
        tgtFogExponent = p.fogExponent;
        tgtFogColor.set(p.fogColor);
        tgtSkyZenith.set(p.skyZenith);
        tgtSkyMidBlue.set(p.skyMidBlue);
        tgtSkyHorizon.set(p.skyHorizon);
        tgtSkyLowHaze.set(p.skyLowHaze);
        tgtSunDiskColor.set(p.sunDiskColor);
        tgtSunCoronaColor.set(p.sunCoronaColor);
        tgtLightShaftOpacity = p.lightShaftOpacity;
        tgtRainIntensity = p.rainIntensity;
        tgtCloudAlphaFactor = p.cloudAlphaFactor;
        tgtWindMultiplier = p.windMultiplier;
        tgtTemperature = p.colorTemperature;
        tgtTint = p.colorTint;

        this.targetState = target;
        this.transitionDuration = Math.max(0.05f, durationSeconds);
        this.transitionProgress = 0.0f;
    }

    public void setWeather(WeatherState target) {
        setWeather(target, 4.0f);
    }

    public WeatherState cycleNextWeather() {
        WeatherState next = targetState.next();
        setWeather(next, 3.5f);
        return next;
    }

    private void applyPresetInstant(AtmosphereConfig.Preset p) {
        srcSunDir.set(p.sunDirection);
        srcSunColor.set(p.sunColor);
        srcAmbientColor.set(p.ambientColor);
        srcFogNear = p.fogNear;
        srcFogFar = p.fogFar;
        srcFogExponent = p.fogExponent;
        srcFogColor.set(p.fogColor);
        srcSkyZenith.set(p.skyZenith);
        srcSkyMidBlue.set(p.skyMidBlue);
        srcSkyHorizon.set(p.skyHorizon);
        srcSkyLowHaze.set(p.skyLowHaze);
        srcSunDiskColor.set(p.sunDiskColor);
        srcSunCoronaColor.set(p.sunCoronaColor);
        srcLightShaftOpacity = p.lightShaftOpacity;
        srcRainIntensity = p.rainIntensity;
        srcCloudAlphaFactor = p.cloudAlphaFactor;
        srcWindMultiplier = p.windMultiplier;
        srcTemperature = p.colorTemperature;
        srcTint = p.colorTint;

        tgtSunDir.set(srcSunDir);
        tgtSunColor.set(srcSunColor);
        tgtAmbientColor.set(srcAmbientColor);
        tgtFogNear = srcFogNear;
        tgtFogFar = srcFogFar;
        tgtFogExponent = srcFogExponent;
        tgtFogColor.set(srcFogColor);
        tgtSkyZenith.set(srcSkyZenith);
        tgtSkyMidBlue.set(srcSkyMidBlue);
        tgtSkyHorizon.set(srcSkyHorizon);
        tgtSkyLowHaze.set(srcSkyLowHaze);
        tgtSunDiskColor.set(srcSunDiskColor);
        tgtSunCoronaColor.set(srcSunCoronaColor);
        tgtLightShaftOpacity = srcLightShaftOpacity;
        tgtRainIntensity = srcRainIntensity;
        tgtCloudAlphaFactor = srcCloudAlphaFactor;
        tgtWindMultiplier = srcWindMultiplier;
        tgtTemperature = srcTemperature;
        tgtTint = srcTint;

        currentSunDir.set(srcSunDir);
        currentSunColor.set(srcSunColor);
        currentAmbientColor.set(srcAmbientColor);
        currentFogNear = srcFogNear;
        currentFogFar = srcFogFar;
        currentFogExponent = srcFogExponent;
        currentFogColor.set(srcFogColor);
        currentSkyZenith.set(srcSkyZenith);
        currentSkyMidBlue.set(srcSkyMidBlue);
        currentSkyHorizon.set(srcSkyHorizon);
        currentSkyLowHaze.set(srcSkyLowHaze);
        currentSunDiskColor.set(srcSunDiskColor);
        currentSunCoronaColor.set(srcSunCoronaColor);
        currentLightShaftOpacity = srcLightShaftOpacity;
        currentRainIntensity = srcRainIntensity;
        currentCloudAlphaFactor = srcCloudAlphaFactor;
        currentWindMultiplier = srcWindMultiplier;
        currentTemperature = srcTemperature;
        currentTint = srcTint;
    }

    public void update(float delta) {
        if (transitionProgress >= 1.0f) {
            currentState = targetState;
            return;
        }

        transitionProgress += delta / transitionDuration;
        if (transitionProgress >= 1.0f) {
            transitionProgress = 1.0f;
            currentState = targetState;
        }

        // Hermite smoothstep for natural perceptual easing
        float t = transitionProgress * transitionProgress * (3.0f - 2.0f * transitionProgress);

        currentSunDir.set(srcSunDir).lerp(tgtSunDir, t).nor();
        currentSunColor.set(srcSunColor).lerp(tgtSunColor, t);
        currentAmbientColor.set(srcAmbientColor).lerp(tgtAmbientColor, t);

        currentFogNear = MathUtils.lerp(srcFogNear, tgtFogNear, t);
        currentFogFar = MathUtils.lerp(srcFogFar, tgtFogFar, t);
        currentFogExponent = MathUtils.lerp(srcFogExponent, tgtFogExponent, t);
        currentFogColor.set(srcFogColor).lerp(tgtFogColor, t);

        currentSkyZenith.set(srcSkyZenith).lerp(tgtSkyZenith, t);
        currentSkyMidBlue.set(srcSkyMidBlue).lerp(tgtSkyMidBlue, t);
        currentSkyHorizon.set(srcSkyHorizon).lerp(tgtSkyHorizon, t);
        currentSkyLowHaze.set(srcSkyLowHaze).lerp(tgtSkyLowHaze, t);

        currentSunDiskColor.set(srcSunDiskColor).lerp(tgtSunDiskColor, t);
        currentSunCoronaColor.set(srcSunCoronaColor).lerp(tgtSunCoronaColor, t);

        currentLightShaftOpacity = MathUtils.lerp(srcLightShaftOpacity, tgtLightShaftOpacity, t);
        currentRainIntensity = MathUtils.lerp(srcRainIntensity, tgtRainIntensity, t);
        currentCloudAlphaFactor = MathUtils.lerp(srcCloudAlphaFactor, tgtCloudAlphaFactor, t);
        currentWindMultiplier = MathUtils.lerp(srcWindMultiplier, tgtWindMultiplier, t);
        currentTemperature = MathUtils.lerp(srcTemperature, tgtTemperature, t);
        currentTint = MathUtils.lerp(srcTint, tgtTint, t);
    }

    public WeatherState getCurrentState() { return currentState; }
    public WeatherState getTargetState() { return targetState; }
    public boolean isTransitioning() { return transitionProgress < 1.0f; }
    public float getTransitionProgress() { return transitionProgress; }

    public Vector3 getCurrentSunDirection() { return currentSunDir; }
    public Color getCurrentSunColor() { return currentSunColor; }
    public Color getCurrentAmbientColor() { return currentAmbientColor; }

    public float getCurrentFogNear() { return currentFogNear; }
    public float getCurrentFogFar() { return currentFogFar; }
    public float getCurrentFogExponent() { return currentFogExponent; }
    public Color getCurrentFogColor() { return currentFogColor; }

    public Color getCurrentSkyZenith() { return currentSkyZenith; }
    public Color getCurrentSkyMidBlue() { return currentSkyMidBlue; }
    public Color getCurrentSkyHorizon() { return currentSkyHorizon; }
    public Color getCurrentSkyLowHaze() { return currentSkyLowHaze; }

    public Color getCurrentSunDiskColor() { return currentSunDiskColor; }
    public Color getCurrentSunCoronaColor() { return currentSunCoronaColor; }

    public float getCurrentLightShaftOpacity() { return currentLightShaftOpacity; }
    public float getCurrentRainIntensity() { return currentRainIntensity; }
    public float getCurrentCloudAlphaFactor() { return currentCloudAlphaFactor; }
    public float getCurrentWindMultiplier() { return currentWindMultiplier; }
    public float getCurrentTemperature() { return currentTemperature; }
    public float getCurrentTint() { return currentTint; }
}
