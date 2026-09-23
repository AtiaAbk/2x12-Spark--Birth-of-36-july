package bd.spark36.character;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
 * Procedural 3D stylized student model representing a Bangladeshi university student
 * during the July 2024 Student Mass Uprising.
 * Includes articulated limbs with walking/sprinting animation, red protest headband,
 * campus shirt, and backpack.
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
    private final Vector3 tempVec = new Vector3();

    public StudentMesh() {
        createMeshes();
    }

    private void createMeshes() {
        long attr = Usage.Position | Usage.Normal;

        // Materials
        Material skinMat = new Material(ColorAttribute.createDiffuse(new Color(0.85f, 0.70f, 0.58f, 1f)));
        Material hairMat = new Material(ColorAttribute.createDiffuse(new Color(0.12f, 0.10f, 0.10f, 1f)));
        Material headbandMat = new Material(ColorAttribute.createDiffuse(new Color(0.90f, 0.12f, 0.15f, 1f))); // July Red
        Material shirtMat = new Material(ColorAttribute.createDiffuse(new Color(0.16f, 0.28f, 0.44f, 1f))); // Navy Polo
        Material pantsMat = new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.24f, 0.30f, 1f))); // Dark slate
        Material backpackMat = new Material(ColorAttribute.createDiffuse(new Color(0.18f, 0.18f, 0.20f, 1f))); // Charcoal

        // Torso: center around y=0 inside its local box, height 0.60
        Model torsoModel = modelBuilder.createBox(0.46f, 0.58f, 0.28f, shirtMat, attr);
        models.add(torsoModel);
        torsoInstance = new ModelInstance(torsoModel);

        // Head: box height 0.24, width 0.24
        Model headModel = modelBuilder.createBox(0.24f, 0.24f, 0.24f, skinMat, attr);
        models.add(headModel);
        headInstance = new ModelInstance(headModel);

        // Hair: styled dark hair cap on head
        Model hairModel = modelBuilder.createBox(0.26f, 0.12f, 0.26f, hairMat, attr);
        models.add(hairModel);
        hairInstance = new ModelInstance(hairModel);

        // Red Protest Headband: wraps around forehead
        Model headbandModel = modelBuilder.createBox(0.255f, 0.06f, 0.255f, headbandMat, attr);
        models.add(headbandModel);
        headbandInstance = new ModelInstance(headbandModel);

        // Backpack: student bag on back
        Model backpackModel = modelBuilder.createBox(0.36f, 0.44f, 0.18f, backpackMat, attr);
        models.add(backpackModel);
        backpackInstance = new ModelInstance(backpackModel);

        // Arms: pivot near top of arm
        Model armModel = modelBuilder.createBox(0.12f, 0.52f, 0.12f, shirtMat, attr);
        models.add(armModel);
        leftArmInstance = new ModelInstance(armModel);
        rightArmInstance = new ModelInstance(armModel);

        // Legs: pivot near hip
        Model legModel = modelBuilder.createBox(0.14f, 0.66f, 0.14f, pantsMat, attr);
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
            float swingScale = isSprinting ? 40f : 25f;
            armSwing = MathUtils.sin(walkCycle) * swingScale;
            legSwing = MathUtils.sin(walkCycle) * swingScale;
            bobOffset = Math.abs(MathUtils.sin(walkCycle * 2f)) * (isSprinting ? 0.05f : 0.03f);
        }

        // Base transform at player world position & orientation
        rootTransform.idt();
        rootTransform.translate(pos.x, pos.y + bobOffset, pos.z);
        rootTransform.rotate(Vector3.Y, headingDegrees);

        // 1. Torso: local center at y = 0.98m
        torsoInstance.transform.set(rootTransform);
        torsoInstance.transform.translate(0f, 0.98f, 0f);
        batch.render(torsoInstance, env);

        // 2. Head: local center at y = 1.42m
        headInstance.transform.set(rootTransform);
        headInstance.transform.translate(0f, 1.42f, 0f);
        batch.render(headInstance, env);

        // Hair: styled dark hair on head
        hairInstance.transform.set(rootTransform);
        hairInstance.transform.translate(0f, 1.50f, -0.02f);
        batch.render(hairInstance, env);

        // 3. Headband: across forehead at y = 1.44m, slightly forward
        headbandInstance.transform.set(rootTransform);
        headbandInstance.transform.translate(0f, 1.44f, 0.01f);
        batch.render(headbandInstance, env);

        // 4. Backpack: attached to back of torso (z = -0.21m)
        backpackInstance.transform.set(rootTransform);
        backpackInstance.transform.translate(0f, 0.96f, -0.21f);
        batch.render(backpackInstance, env);

        // 5. Left Arm: pivot at shoulder (x = -0.30m, y = 1.20m)
        tempMat.set(rootTransform);
        tempMat.translate(-0.30f, 1.20f, 0f);
        tempMat.rotate(Vector3.X, -armSwing); // Swing opposite to left leg
        tempMat.translate(0f, -0.24f, 0f);
        leftArmInstance.transform.set(tempMat);
        batch.render(leftArmInstance, env);

        // 6. Right Arm: pivot at shoulder (x = +0.30m, y = 1.20m)
        tempMat.set(rootTransform);
        tempMat.translate(0.30f, 1.20f, 0f);
        tempMat.rotate(Vector3.X, armSwing);
        tempMat.translate(0f, -0.24f, 0f);
        rightArmInstance.transform.set(tempMat);
        batch.render(rightArmInstance, env);

        // 7. Left Leg: pivot at hip (x = -0.13m, y = 0.68m)
        tempMat.set(rootTransform);
        tempMat.translate(-0.13f, 0.68f, 0f);
        tempMat.rotate(Vector3.X, legSwing);
        tempMat.translate(0f, -0.32f, 0f);
        leftLegInstance.transform.set(tempMat);
        batch.render(leftLegInstance, env);

        // 8. Right Leg: pivot at hip (x = +0.13m, y = 0.68m)
        tempMat.set(rootTransform);
        tempMat.translate(0.13f, 0.68f, 0f);
        tempMat.rotate(Vector3.X, -legSwing);
        tempMat.translate(0f, -0.32f, 0f);
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
