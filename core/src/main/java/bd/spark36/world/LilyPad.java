// ei file ta pukurer panir upor shapla ar pad render kore
package bd.spark36.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class LilyPad {

    public final float baseX;
    public final float baseZ;
    public final float radius;
    public final float yaw;
    public final int atlasCol;
    public final int atlasRow;
    public final ModelInstance instance;

    private float currentY;
    private float currentPitch;
    private float currentRoll;

    private static final Vector3 tmpNormal = new Vector3();

    public LilyPad(float baseX, float baseZ, float radius, float yaw, int col, int row, ModelInstance instance) {
        this.baseX = baseX;
        this.baseZ = baseZ;
        this.radius = radius;
        this.yaw = yaw;
        this.atlasCol = col;
        this.atlasRow = row;
        this.instance = instance;
        this.currentY = WaterConfig.BASE_WATER_HEIGHT + WaterConfig.LILY_PAD_VERTICAL_OFFSET;
        this.currentPitch = 0f;
        this.currentRoll = 0f;
        updateTransform();
    }

    public void update(float delta, float animTime, WaterWaveModel waveModel) {

        float targetY = waveModel.getWaterHeight(baseX, baseZ, animTime) + WaterConfig.LILY_PAD_VERTICAL_OFFSET;
        waveModel.getWaterNormal(baseX, baseZ, animTime, tmpNormal);

        float targetPitchRad = -MathUtils.atan2(tmpNormal.z, tmpNormal.y);
        float targetRollRad = MathUtils.atan2(tmpNormal.x, tmpNormal.y);

        float maxTiltRad = WaterConfig.LILY_PAD_MAX_TILT_DEG * MathUtils.degreesToRadians;
        targetPitchRad = MathUtils.clamp(targetPitchRad, -maxTiltRad, maxTiltRad);
        targetRollRad = MathUtils.clamp(targetRollRad, -maxTiltRad, maxTiltRad);

        float targetPitchDeg = targetPitchRad * MathUtils.radiansToDegrees;
        float targetRollDeg = targetRollRad * MathUtils.radiansToDegrees;

        float alpha = MathUtils.clamp(delta * WaterConfig.LILY_PAD_DAMPING, 0f, 1f);
        currentY = MathUtils.lerp(currentY, targetY, alpha);
        currentPitch = MathUtils.lerp(currentPitch, targetPitchDeg, alpha);
        currentRoll = MathUtils.lerp(currentRoll, targetRollDeg, alpha);

        updateTransform();
    }

    private void updateTransform() {
        if (instance != null) {
            instance.transform.idt()
                .translate(baseX, currentY, baseZ)
                .rotate(Vector3.Y, yaw)
                .rotate(Vector3.X, currentPitch)
                .rotate(Vector3.Z, currentRoll)
                .scl(radius, 1f, radius);
        }
    }

    public void render(com.badlogic.gdx.graphics.g3d.ModelBatch batch, com.badlogic.gdx.graphics.g3d.Environment env) {
        if (instance != null) {
            batch.render(instance, env);
        }
    }

    public float getCurrentY() {
        return currentY;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }

    public float getCurrentRoll() {
        return currentRoll;
    }
}
