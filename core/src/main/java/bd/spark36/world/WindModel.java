package bd.spark36.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Authoritative mathematical model for vegetation wind simulation.
 * Combines slow, height-weighted trunk flexing, secondary branch sway, and high-frequency
 * leaf flutter. All methods modify vectors in-place with zero heap allocations.
 *
 * Physical Guarantee:
 * At ground level (y <= 0), displacement is strictly zero. Trunks stay 100% anchored to the earth.
 */
public class WindModel {

    private final Vector3 windDir = new Vector3(VegetationConfig.WIND_DIRECTION);
    private final Vector3 windCross = new Vector3();

    public WindModel() {
        // Cross vector for lateral sway perpendicular to prevailing wind
        windCross.set(-windDir.z, 0f, windDir.x).nor();
    }

    /**
     * Copies prevailing wind direction into out vector.
     */
    public void getWindDirection(Vector3 out) {
        out.set(windDir);
    }

    /**
     * Calculates base-anchored trunk tilt angles (pitch along wind direction, roll along cross direction).
     * Modifies outPitchRoll (x = pitch deg, z = roll deg) in-place without memory allocations.
     */
    public void getTrunkSwayAngles(float worldX, float worldZ, float time, Vector3 outPitchRoll) {
        // Spatial phase decorrelation: wave travels across the campus
        float spatialPhase = (worldX * windDir.x + worldZ * windDir.z) * 0.08f;
        float crossPhase   = (worldX * windCross.x + worldZ * windCross.z) * 0.12f;

        // Low-frequency gust envelope (breathing wind intensity)
        float gust = 0.75f + 0.25f * MathUtils.sin(time * 0.45f + spatialPhase * 0.5f);

        // Primary sway along wind direction
        float primary = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_PRIMARY + spatialPhase);
        // Secondary harmonic
        float secondary = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_SECONDARY + spatialPhase * 1.6f) * 0.35f;

        float pitchDeg = (primary + secondary) * VegetationConfig.MAX_TRUNK_SWAY_DEG * gust;

        // Subtle lateral roll oscillation
        float roll = MathUtils.cos(time * (VegetationConfig.WIND_SWAY_FREQ_PRIMARY * 0.85f) + crossPhase);
        float rollDeg = roll * (VegetationConfig.MAX_TRUNK_SWAY_DEG * 0.35f) * gust;

        outPitchRoll.set(pitchDeg, 0f, rollDeg);
    }

    /**
     * Calculates 3D canopy displacement at a given height above ground.
     * Guaranteed: when localY <= 0, displacement is identically (0, 0, 0).
     */
    public void getCanopyDisplacement(float worldX, float worldZ, float localY, float treeHeight,
                                     float time, Vector3 outOffset) {
        if (localY <= 0.0f || treeHeight <= 0.01f) {
            outOffset.set(0f, 0f, 0f);
            return;
        }

        // Quadratic height weighting (roots stationary, top flexible)
        float hNorm = MathUtils.clamp(localY / treeHeight, 0.0f, 1.0f);
        float heightWeight = hNorm * hNorm; // (y/H)^2

        float spatialPhase = (worldX * windDir.x + worldZ * windDir.z) * 0.08f;
        float gust = 0.75f + 0.25f * MathUtils.sin(time * 0.45f + spatialPhase * 0.5f);

        float wave1 = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_PRIMARY + spatialPhase);
        float wave2 = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_SECONDARY + spatialPhase * 1.5f) * 0.35f;

        float maxCanopyDisp = 0.055f; // ~5.5 cm displacement at top
        float disp = (wave1 + wave2) * maxCanopyDisp * heightWeight * gust;

        // Lateral sway component
        float latWave = MathUtils.cos(time * 1.8f + spatialPhase) * 0.018f * heightWeight * gust;

        outOffset.set(
            windDir.x * disp + windCross.x * latWave,
            -Math.abs(disp) * 0.08f, // slight natural dip under flex
            windDir.z * disp + windCross.z * latWave
        );
    }

    /**
     * High-frequency micro-flutter for leaf cards and small twigs.
     */
    public void getLeafFlutter(float worldX, float worldY, float worldZ, float time, Vector3 outOffset) {
        float leafPhase = (worldX * 1.7f + worldZ * 2.3f + worldY * 3.1f);
        float flutter = MathUtils.sin(time * VegetationConfig.WIND_FLUTTER_FREQ + leafPhase);
        float flutter2 = MathUtils.cos(time * (VegetationConfig.WIND_FLUTTER_FREQ * 1.4f) + leafPhase * 0.7f);

        float amp = VegetationConfig.LEAF_FLUTTER_AMPLITUDE;
        outOffset.set(
            (flutter * windDir.x + flutter2 * windCross.x) * amp,
            flutter * 0.4f * amp,
            (flutter * windDir.z + flutter2 * windCross.z) * amp
        );
    }
}
