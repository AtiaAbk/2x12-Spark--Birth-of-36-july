// ei file ta gachpala noranor jonno batasher physics model banay
package bd.spark36.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class WindModel {

    private final Vector3 windDir = new Vector3(VegetationConfig.WIND_DIRECTION);
    private final Vector3 windCross = new Vector3();

    public WindModel() {

        windCross.set(-windDir.z, 0f, windDir.x).nor();
    }

    public void getWindDirection(Vector3 out) {
        out.set(windDir);
    }

    public void getTrunkSwayAngles(float worldX, float worldZ, float time, Vector3 outPitchRoll) {

        float spatialPhase = (worldX * windDir.x + worldZ * windDir.z) * 0.08f;
        float crossPhase   = (worldX * windCross.x + worldZ * windCross.z) * 0.12f;

        float gust = 0.75f + 0.25f * MathUtils.sin(time * 0.45f + spatialPhase * 0.5f);

        float primary = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_PRIMARY + spatialPhase);

        float secondary = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_SECONDARY + spatialPhase * 1.6f) * 0.35f;

        float pitchDeg = (primary + secondary) * VegetationConfig.MAX_TRUNK_SWAY_DEG * gust;

        float roll = MathUtils.cos(time * (VegetationConfig.WIND_SWAY_FREQ_PRIMARY * 0.85f) + crossPhase);
        float rollDeg = roll * (VegetationConfig.MAX_TRUNK_SWAY_DEG * 0.35f) * gust;

        outPitchRoll.set(pitchDeg, 0f, rollDeg);
    }

    public void getCanopyDisplacement(float worldX, float worldZ, float localY, float treeHeight,
                                     float time, Vector3 outOffset) {
        if (localY <= 0.0f || treeHeight <= 0.01f) {
            outOffset.set(0f, 0f, 0f);
            return;
        }

        float hNorm = MathUtils.clamp(localY / treeHeight, 0.0f, 1.0f);
        float heightWeight = hNorm * hNorm;

        float spatialPhase = (worldX * windDir.x + worldZ * windDir.z) * 0.08f;
        float gust = 0.75f + 0.25f * MathUtils.sin(time * 0.45f + spatialPhase * 0.5f);

        float wave1 = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_PRIMARY + spatialPhase);
        float wave2 = MathUtils.sin(time * VegetationConfig.WIND_SWAY_FREQ_SECONDARY + spatialPhase * 1.5f) * 0.35f;

        float maxCanopyDisp = 0.055f;
        float disp = (wave1 + wave2) * maxCanopyDisp * heightWeight * gust;

        float latWave = MathUtils.cos(time * 1.8f + spatialPhase) * 0.018f * heightWeight * gust;

        outOffset.set(
            windDir.x * disp + windCross.x * latWave,
            -Math.abs(disp) * 0.08f,
            windDir.z * disp + windCross.z * latWave
        );
    }

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
