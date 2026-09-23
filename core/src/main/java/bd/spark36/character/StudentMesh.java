package bd.spark36.character;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.world.TextureFactory;

/**
 * Realistic 3D student protagonist model representing a Bangladeshi university student
 * during the July 2024 Student Mass Uprising.
 * Features textured khaki canvas attire, detailed student backpack with movement pins,
 * natural dark hair, iconic red protest headband, and articulated walking/running locomotion.
 */
public class StudentMesh implements Disposable {

    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Array<Model> models = new Array<>();

    // Model parts
    private ModelInstance torsoInstance;
    private ModelInstance headInstance;
    private ModelInstance hairInstance;
    private ModelInstance headbandInstance;
    private ModelInstance backpackInstance;
    private ModelInstance leftArmInstance;
    private ModelInstance rightArmInstance;
    private ModelInstance leftLegInstance;
    private ModelInstance rightLegInstance;

    // Temporary matrices for articulated animation hierarchy
    private final Matrix4 rootTransform = new Matrix4();
    private final Matrix4 tempMat = new Matrix4();

    public StudentMesh(TextureFactory textures) {
        createMeshes(textures);
    }

    private void createMeshes(TextureFactory textures) {
        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        // Materials with procedural textures
        Material jacketMat = new Material(
            TextureAttribute.createDiffuse(textures.studentJacket),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.88f, 1f))
        );

        Material backpackMat = new Material(
            TextureAttribute.createDiffuse(textures.backpackFabric),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.88f, 1f))
        );

        Material skinMat = new Material(ColorAttribute.createDiffuse(new Color(0.82f, 0.68f, 0.54f, 1f)));
        Material hairMat = new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.08f, 0.08f, 1f)));
        Material headbandMat = new Material(ColorAttribute.createDiffuse(new Color(0.92f, 0.12f, 0.15f, 1f))); // July Red
        Material pantsMat = new Material(ColorAttribute.createDiffuse(new Color(0.24f, 0.28f, 0.35f, 1f)));  // Denim jeans

        // 1. Torso: human proportions (width 0.44m, height 0.62m, depth 0.26m)
        Model torsoModel = modelBuilder.createBox(0.44f, 0.62f, 0.26f, jacketMat, attr);
        models.add(torsoModel);
        torsoInstance = new ModelInstance(torsoModel);

        // 2. Head & Neck
        Model headModel = modelBuilder.createBox(0.22f, 0.24f, 0.22f, skinMat, attr);
        models.add(headModel);
        headInstance = new ModelInstance(headModel);

        // 3. Hair: styled dark hair on head
        Model hairModel = modelBuilder.createBox(0.24f, 0.14f, 0.24f, hairMat, attr);
        models.add(hairModel);
        hairInstance = new ModelInstance(hairModel);

        // 4. Red Protest Headband
        Model headbandModel = modelBuilder.createBox(0.235f, 0.055f, 0.235f, headbandMat, attr);
        models.add(headbandModel);
        headbandInstance = new ModelInstance(headbandModel);

        // 5. Backpack: canvas student bag with front pocket and enamel pins
        Model backpackModel = modelBuilder.createBox(0.36f, 0.46f, 0.20f, backpackMat, attr);
        models.add(backpackModel);
        backpackInstance = new ModelInstance(backpackModel);

        // 6. Arms: sleeves with khaki jacket material
        Model armModel = modelBuilder.createBox(0.12f, 0.54f, 0.12f, jacketMat, attr);
        models.add(armModel);
        leftArmInstance = new ModelInstance(armModel);
        rightArmInstance = new ModelInstance(armModel);

        // 7. Legs: denim trousers
        Model legModel = modelBuilder.createBox(0.14f, 0.68f, 0.14f, pantsMat, attr);
        models.add(legModel);
        leftLegInstance = new ModelInstance(legModel);
        rightLegInstance = new ModelInstance(legModel);
    }

    /**
     * Updates and renders the student model with smooth animated walking/running locomotion.
     */
    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting) {

        float bobOffset = 0f;
        float armSwing = 0f;
        float legSwing = 0f;

        if (isMoving) {
            float swingScale = isSprinting ? 38f : 24f;
            armSwing = MathUtils.sin(walkCycle) * swingScale;
            legSwing = MathUtils.sin(walkCycle) * swingScale;
            bobOffset = Math.abs(MathUtils.sin(walkCycle * 2f)) * (isSprinting ? 0.04f : 0.025f);
        }

        // Base transform at player world position & orientation
        rootTransform.idt();
        rootTransform.translate(pos.x, pos.y + bobOffset, pos.z);
        rootTransform.rotate(Vector3.Y, headingDegrees);

        // 1. Torso: local center at y = 1.02m
        torsoInstance.transform.set(rootTransform);
        torsoInstance.transform.translate(0f, 1.02f, 0f);
        batch.render(torsoInstance, env);

        // 2. Head: local center at y = 1.45m
        headInstance.transform.set(rootTransform);
        headInstance.transform.translate(0f, 1.45f, 0f);
        batch.render(headInstance, env);

        // 3. Hair: styled dark hair on head
        hairInstance.transform.set(rootTransform);
        hairInstance.transform.translate(0f, 1.53f, -0.015f);
        batch.render(hairInstance, env);

        // 4. Headband: across forehead at y = 1.46m
        headbandInstance.transform.set(rootTransform);
        headbandInstance.transform.translate(0f, 1.46f, 0.01f);
        batch.render(headbandInstance, env);

        // 5. Backpack: attached to back of torso (z = -0.22m)
        backpackInstance.transform.set(rootTransform);
        backpackInstance.transform.translate(0f, 1.00f, -0.22f);
        batch.render(backpackInstance, env);

        // 6. Left Arm: pivot at shoulder (x = -0.28m, y = 1.24m)
        tempMat.set(rootTransform);
        tempMat.translate(-0.28f, 1.24f, 0f);
        tempMat.rotate(Vector3.X, -armSwing);
        tempMat.translate(0f, -0.25f, 0f);
        leftArmInstance.transform.set(tempMat);
        batch.render(leftArmInstance, env);

        // 7. Right Arm: pivot at shoulder (x = +0.28m, y = 1.24m)
        tempMat.set(rootTransform);
        tempMat.translate(0.28f, 1.24f, 0f);
        tempMat.rotate(Vector3.X, armSwing);
        tempMat.translate(0f, -0.25f, 0f);
        rightArmInstance.transform.set(tempMat);
        batch.render(rightArmInstance, env);

        // 8. Left Leg: pivot at hip (x = -0.12m, y = 0.70f)
        tempMat.set(rootTransform);
        tempMat.translate(-0.12f, 0.70f, 0f);
        tempMat.rotate(Vector3.X, legSwing);
        tempMat.translate(0f, -0.34f, 0f);
        leftLegInstance.transform.set(tempMat);
        batch.render(leftLegInstance, env);

        // 9. Right Leg: pivot at hip (x = +0.12m, y = 0.70f)
        tempMat.set(rootTransform);
        tempMat.translate(0.12f, 0.70f, 0f);
        tempMat.rotate(Vector3.X, -legSwing);
        tempMat.translate(0f, -0.34f, 0f);
        rightLegInstance.transform.set(tempMat);
        batch.render(rightLegInstance, env);
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
    }
}
