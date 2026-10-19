package bd.spark36.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Authoritative mathematical model for water wave displacement and surface normals.
 * Provides identical calculations for both CPU gameplay buoyancy and GPU vertex shader
 * displacement, ensuring lily pads and swimming fish remain strictly in sync with visual waves.
 */
public class WaterWaveModel {

    // Precomputed wave constants: wave numbers k = 2pi / lambda
    private final float k1 = MathUtils.PI2 / WaterConfig.WAVE1_LEN;
    private final float k2 = MathUtils.PI2 / WaterConfig.WAVE2_LEN;
    private final float k3 = MathUtils.PI2 / WaterConfig.WAVE3_LEN;

    // Angular frequencies omega = k * speed
    private final float w1 = k1 * WaterConfig.WAVE1_SPEED;
    private final float w2 = k2 * WaterConfig.WAVE2_SPEED;
    private final float w3 = k3 * WaterConfig.WAVE3_SPEED;

    // Direction components
    private final float d1x = WaterConfig.WAVE1_DIR_X, d1z = WaterConfig.WAVE1_DIR_Z;
    private final float d2x = WaterConfig.WAVE2_DIR_X, d2z = WaterConfig.WAVE2_DIR_Z;
    private final float d3x = WaterConfig.WAVE3_DIR_X, d3z = WaterConfig.WAVE3_DIR_Z;

    // Amplitudes
    private final float a1 = WaterConfig.WAVE1_AMP;
    private final float a2 = WaterConfig.WAVE2_AMP;
    private final float a3 = WaterConfig.WAVE3_AMP;

    /**
     * Checks if the given world coordinates lie within the pond perimeter.
     */
    public boolean isInsideWater(float worldX, float worldZ) {
        float halfW = WaterConfig.WATER_WIDTH * 0.5f;
        float halfL = WaterConfig.WATER_LENGTH * 0.5f;
        return (Math.abs(worldX - WaterConfig.CENTER_X) <= halfW) &&
               (Math.abs(worldZ - WaterConfig.CENTER_Z) <= halfL);
    }

    /**
     * Calculates the minimum distance from (worldX, worldZ) to the pond's perimeter curbs.
     * Returns 0.0 or negative if outside the pond boundaries.
     */
    public float getShoreDistance(float worldX, float worldZ) {
        float halfW = WaterConfig.WATER_WIDTH * 0.5f;
        float halfL = WaterConfig.WATER_LENGTH * 0.5f;
        float dx = halfW - Math.abs(worldX - WaterConfig.CENTER_X);
        float dz = halfL - Math.abs(worldZ - WaterConfig.CENTER_Z);
        return Math.min(dx, dz);
    }

    /**
     * Shoreline dampening factor: smoothly attenuates waves near the stone curbs from 1.0 down to 0.0.
     */
    public float getShoreDampening(float worldX, float worldZ) {
        float dist = getShoreDistance(worldX, worldZ);
        if (dist <= 0f) return 0f;
        if (dist >= WaterConfig.SHORE_MARGIN) return 1f;
        float t = dist / WaterConfig.SHORE_MARGIN;
        return t * t * (3f - 2f * t); // smoothstep
    }

    /**
     * Returns the authoritative water surface elevation Y at the specified coordinates and time.
     */
    public float getWaterHeight(float worldX, float worldZ, float time) {
        float shore = getShoreDampening(worldX, worldZ);
        if (shore <= 0f) return WaterConfig.BASE_WATER_HEIGHT;

        float phi1 = (d1x * worldX + d1z * worldZ) * k1 - w1 * time;
        float phi2 = (d2x * worldX + d2z * worldZ) * k2 - w2 * time;
        float phi3 = (d3x * worldX + d3z * worldZ) * k3 - w3 * time;

        float dy = a1 * MathUtils.sin(phi1) +
                   a2 * MathUtils.sin(phi2) +
                   a3 * MathUtils.sin(phi3);

        return WaterConfig.BASE_WATER_HEIGHT + dy * shore;
    }

    /**
     * Computes the exact analytical surface normal vector at (worldX, worldZ, time).
     * Modifies outNormal in-place without memory allocations.
     */
    public void getWaterNormal(float worldX, float worldZ, float time, Vector3 outNormal) {
        float shore = getShoreDampening(worldX, worldZ);
        if (shore <= 0f) {
            outNormal.set(0f, 1f, 0f);
            return;
        }

        float phi1 = (d1x * worldX + d1z * worldZ) * k1 - w1 * time;
        float phi2 = (d2x * worldX + d2z * worldZ) * k2 - w2 * time;
        float phi3 = (d3x * worldX + d3z * worldZ) * k3 - w3 * time;

        float cos1 = MathUtils.cos(phi1);
        float cos2 = MathUtils.cos(phi2);
        float cos3 = MathUtils.cos(phi3);

        // Partial derivatives dy/dx and dy/dz
        float dYdX = (a1 * k1 * d1x * cos1 +
                      a2 * k2 * d2x * cos2 +
                      a3 * k3 * d3x * cos3) * shore;

        float dYdZ = (a1 * k1 * d1z * cos1 +
                      a2 * k2 * d2z * cos2 +
                      a3 * k3 * d3z * cos3) * shore;

        outNormal.set(-dYdX, 1.0f, -dYdZ).nor();
    }
}
