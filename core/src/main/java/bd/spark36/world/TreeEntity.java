package bd.spark36.world;

import bd.spark36.render.GraphicsTier;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * An individual tree entity in the campus environment.
 * Encapsulates the tree instance models (trunk, core canopy, leaf cards),
 * evaluates base-anchored wind sway (guaranteeing zero displacement at y <= 0),
 * and performs distance-based Level-of-Detail (LOD).
 */
public class TreeEntity {

    public final int species;
    public final Vector3 position = new Vector3();
    public final float scale;
    public final float seed;
    public final float height;

    private final ModelInstance trunkInstance;
    private final ModelInstance canopyCoreInstance;
    private final ModelInstance leafCardsInstance;

    public final BoundingBox bounds = new BoundingBox();
    private final Vector3 tempPitchRoll = new Vector3();
    private final Vector3 windDir = new Vector3();
    private final Vector3 windCross = new Vector3();

    private boolean showCards = true;
    private float lastDistSq = 0f;

    public TreeEntity(int species, float x, float z, float scale, float seed, float height,
                      ModelInstance trunk, ModelInstance canopyCore, ModelInstance leafCards) {
        this.species = species;
        this.position.set(x, 0f, z);
        this.scale = scale;
        this.seed = seed;
        this.height = height;
        this.trunkInstance = trunk;
        this.canopyCoreInstance = canopyCore;
        this.leafCardsInstance = leafCards;

        // Position models at world base
        if (trunkInstance != null) {
            trunkInstance.transform.setToTranslation(position);
        }
        if (canopyCoreInstance != null) {
            canopyCoreInstance.transform.setToTranslation(position);
        }
        if (leafCardsInstance != null) {
            leafCardsInstance.transform.setToTranslation(position);
        }

        // Bounding box in world coordinates for frustum culling
        float radius = 4.8f * scale;
        bounds.set(
            new Vector3(x - radius, -0.35f * scale, z - radius),
            new Vector3(x + radius, height + 2.0f * scale, z + radius)
        );
    }

    public ModelInstance getTrunkInstance() {
        return trunkInstance;
    }

    public ModelInstance getCanopyCoreInstance() {
        return canopyCoreInstance;
    }

    public ModelInstance getLeafCardsInstance() {
        return leafCardsInstance;
    }

    public boolean isShowingCards() {
        return showCards;
    }

    /**
     * Updates wind sway simulation and distance-based LOD.
     * Guaranteed: base (y <= 0) displacement is identically (0, 0, 0).
     */
    public void update(float time, WindModel wind, GraphicsTier tier, Camera camera) {
        // Distance calculation for Level of Detail
        if (camera != null) {
            float dx = camera.position.x - position.x;
            float dy = camera.position.y - (position.y + height * 0.5f);
            float dz = camera.position.z - position.z;
            lastDistSq = dx * dx + dy * dy + dz * dz;

            float lodCutoff = tier.vegetationLodDistance;
            // Hysteresis prevents rapid toggling at cutoff border
            float threshold = showCards ? (lodCutoff + VegetationConfig.LOD_HYSTERESIS) : lodCutoff;
            showCards = (lastDistSq <= threshold * threshold);
        } else {
            showCards = true;
        }

        if (!tier.enableLayeredWind || wind == null) {
            if (trunkInstance != null) trunkInstance.transform.setToTranslation(position);
            if (canopyCoreInstance != null) canopyCoreInstance.transform.setToTranslation(position);
            if (leafCardsInstance != null) leafCardsInstance.transform.setToTranslation(position);
            return;
        }

        // Evaluate wind tilt angles
        wind.getWindDirection(windDir);
        windCross.set(-windDir.z, 0f, windDir.x).nor();

        wind.getTrunkSwayAngles(position.x, position.z, time, tempPitchRoll);
        float pitchDeg = tempPitchRoll.x;
        float rollDeg = tempPitchRoll.z;

        // Base-anchored transforms with layered hierarchical sway:
        // Local origin (0, 0, 0) strictly guaranteed zero displacement in world space.
        // 1. Stiff trunk (0.45x sway)
        if (trunkInstance != null) {
            trunkInstance.transform.setToTranslation(position)
                .rotate(windCross, pitchDeg * 0.45f)
                .rotate(windDir, rollDeg * 0.45f);
        }

        // 2. Moderate canopy core (1.0x sway)
        if (canopyCoreInstance != null) {
            canopyCoreInstance.transform.setToTranslation(position)
                .rotate(windCross, pitchDeg * 1.0f)
                .rotate(windDir, rollDeg * 1.0f);
        }

        // 3. Dynamic leaf cards (1.35x sway + 3.4 Hz micro-flutter)
        if (leafCardsInstance != null) {
            float flutter = 0.35f * MathUtils.sin(time * 3.4f * 2.0f * MathUtils.PI + seed);
            leafCardsInstance.transform.setToTranslation(position)
                .rotate(windCross, pitchDeg * 1.35f + flutter)
                .rotate(windDir, rollDeg * 1.35f + flutter * 0.5f);
        }
    }

    public void render(ModelBatch batch, Environment env) {
        if (trunkInstance != null) {
            batch.render(trunkInstance, env);
        }
        if (canopyCoreInstance != null) {
            batch.render(canopyCoreInstance, env);
        }
        if (leafCardsInstance != null && showCards) {
            batch.render(leafCardsInstance, env);
        }
    }

    public void renderShadowCaster(ModelBatch depthBatch) {
        if (trunkInstance != null) {
            depthBatch.render(trunkInstance);
        }
        if (canopyCoreInstance != null) {
            depthBatch.render(canopyCoreInstance);
        }
        if (leafCardsInstance != null && showCards) {
            depthBatch.render(leafCardsInstance);
        }
    }
}
