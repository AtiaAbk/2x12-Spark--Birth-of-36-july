package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Procedural School of Fish for the Curzon Hall Grand Pukur.
 * Simulates vibrant Japanese Koi, Golden Carp, and Scarlet Oranda swimming
 * in natural flowing paths with realistic sinusoidal spine and caudal fin wiggling.
 */
public class PondFishSystem implements Disposable {

    public static final int FISH_COUNT = 14;

    private static class Fish {
        Vector3 pos = new Vector3();
        Vector3 target = new Vector3();
        float heading = 0f;
        float speed = 1.2f;
        float bodyLength = 0.65f;
        float swimPhase = 0f;
        float tailWagSpeed = 8f;
        int colorType; // 0=Gold, 1=Scarlet, 2=Calico/White-Orange, 3=Deep Amber
    }

    private final Fish[] fish = new Fish[FISH_COUNT];
    private final ModelBuilder mb = new ModelBuilder();
    private final Array<Model> models = new Array<>();

    // Fish body part models
    private Model[] bodyModels;
    private Model[] tailModels;
    private Model finModel;

    private final Matrix4 fishTransform = new Matrix4();
    private final Matrix4 tailTransform = new Matrix4();

    // Pond swimming boundaries (Curzon Hall Grand Pukur)
    private float minX = -11.5f, maxX = 11.5f;
    private float minZ = -10.0f, maxZ = 22.0f;
    private float waterY = 0.06f; // Just below water surface (0.12f)

    public PondFishSystem() {
        createFishModels();
        initFish();
    }

    public void setPondBounds(float minX, float maxX, float minZ, float maxZ, float waterSurfaceY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.waterY = waterSurfaceY - 0.05f;
    }

    private void createFishModels() {
        long attr = Usage.Position | Usage.Normal;

        Color[] bodyColors = {
            new Color(0.96f, 0.62f, 0.12f, 1f), // Golden Carp
            new Color(0.92f, 0.22f, 0.14f, 1f), // Scarlet Koi
            new Color(0.95f, 0.92f, 0.88f, 1f), // Platinum White
            new Color(0.85f, 0.40f, 0.10f, 1f), // Deep Amber
        };

        Color[] tailColors = {
            new Color(0.98f, 0.75f, 0.28f, 0.85f),
            new Color(0.95f, 0.35f, 0.22f, 0.85f),
            new Color(0.98f, 0.95f, 0.92f, 0.85f),
            new Color(0.90f, 0.55f, 0.25f, 0.85f),
        };

        bodyModels = new Model[bodyColors.length];
        tailModels = new Model[tailColors.length];

        for (int i = 0; i < bodyColors.length; i++) {
            Material bodyMat = new Material(ColorAttribute.createDiffuse(bodyColors[i]));
            // Streamlined tapered torpedo fish body (width, height, length)
            bodyModels[i] = mb.createSphere(0.18f, 0.14f, 0.46f, 12, 10, bodyMat, attr);
            models.add(bodyModels[i]);

            Material tailMat = new Material(ColorAttribute.createDiffuse(tailColors[i]));
            // Fan-shaped tail fin
            tailModels[i] = mb.createCone(0.14f, 0.24f, 0.03f, 8, tailMat, attr);
            models.add(tailModels[i]);
        }

        // Translucent pectoral fin
        Material finMat = new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.85f, 0.70f, 0.75f)));
        finModel = mb.createBox(0.10f, 0.015f, 0.06f, finMat, attr);
        models.add(finModel);
    }

    private void initFish() {
        for (int i = 0; i < FISH_COUNT; i++) {
            Fish f = new Fish();
            f.colorType = i % bodyModels.length;
            f.bodyLength = 0.55f + MathUtils.random() * 0.25f;
            f.speed = 0.8f + MathUtils.random() * 1.0f;
            f.tailWagSpeed = 6f + f.speed * 3f;
            f.swimPhase = MathUtils.random() * MathUtils.PI2;

            // Spread out initially in the pond
            f.pos.set(
                MathUtils.random(minX + 2f, maxX - 2f),
                waterY + MathUtils.random(-0.02f, 0.02f),
                MathUtils.random(minZ + 3f, maxZ - 3f)
            );
            pickNewTarget(f);
            f.heading = MathUtils.random(0f, 360f);
            fish[i] = f;
        }
    }

    private void pickNewTarget(Fish f) {
        f.target.set(
            MathUtils.random(minX + 2.5f, maxX - 2.5f),
            waterY + MathUtils.random(-0.02f, 0.02f),
            MathUtils.random(minZ + 2.5f, maxZ - 2.5f)
        );
        f.speed = 0.7f + MathUtils.random() * 0.9f;
        f.tailWagSpeed = 5f + f.speed * 3.5f;
    }

    public void update(float delta) {
        for (int i = 0; i < FISH_COUNT; i++) {
            Fish f = fish[i];
            f.swimPhase += delta * f.tailWagSpeed;

            // Distance to target
            float dx = f.target.x - f.pos.x;
            float dz = f.target.z - f.pos.z;
            float distSq = dx * dx + dz * dz;

            if (distSq < 1.5f || MathUtils.random() < delta * 0.08f) {
                pickNewTarget(f);
            }

            // Target heading (degrees, local +Z is forward)
            float targetHeading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;

            // Smooth turn towards target
            float diff = (targetHeading - f.heading) % 360f;
            if (diff > 180f) diff -= 360f;
            if (diff < -180f) diff += 360f;
            f.heading += diff * Math.min(1f, 3.5f * delta);

            // Move forward along heading
            float rad = f.heading * MathUtils.degreesToRadians;
            float moveStep = f.speed * delta;
            f.pos.x += MathUtils.sin(rad) * moveStep;
            f.pos.z += MathUtils.cos(rad) * moveStep;

            // Keep within pond
            f.pos.x = MathUtils.clamp(f.pos.x, minX + 0.8f, maxX - 0.8f);
            f.pos.z = MathUtils.clamp(f.pos.z, minZ + 0.8f, maxZ - 0.8f);
        }
    }

    public void render(ModelBatch batch, Environment env) {
        for (int i = 0; i < FISH_COUNT; i++) {
            Fish f = fish[i];
            int ct = f.colorType;

            // Animated subtle body sway
            float bodySway = MathUtils.sin(f.swimPhase) * 4f;

            fishTransform.idt();
            fishTransform.translate(f.pos.x, f.pos.y, f.pos.z);
            fishTransform.rotate(Vector3.Y, f.heading + bodySway);
            fishTransform.scl(f.bodyLength);

            // Render main body
            ModelInstance bodyInst = new ModelInstance(bodyModels[ct]);
            bodyInst.transform.set(fishTransform);
            batch.render(bodyInst, env);

            // Animated tail fin wiggling
            float tailWag = MathUtils.sin(f.swimPhase - 1.2f) * 24f;
            tailTransform.set(fishTransform);
            tailTransform.translate(0f, 0f, -0.25f); // Attach to rear of body
            tailTransform.rotate(Vector3.Y, tailWag);
            tailTransform.rotate(Vector3.X, 90f); // Orient cone as fin

            ModelInstance tailInst = new ModelInstance(tailModels[ct]);
            tailInst.transform.set(tailTransform);
            batch.render(tailInst, env);

            // Left and Right pectoral fins
            float finFlap = MathUtils.sin(f.swimPhase * 0.8f) * 15f;
            for (int side = -1; side <= 1; side += 2) {
                Matrix4 finT = new Matrix4(fishTransform);
                finT.translate(side * 0.11f, -0.02f, 0.08f);
                finT.rotate(Vector3.Y, side * (25f + finFlap));
                ModelInstance finInst = new ModelInstance(finModel);
                finInst.transform.set(finT);
                batch.render(finInst, env);
            }
        }
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
    }
}
