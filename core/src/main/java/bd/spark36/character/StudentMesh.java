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
 * 3D student protagonist: a Bangladeshi university student during the July 2024 uprising.
 *
 * Built from rounded, deliberately overlapping parts (ellipsoids and capsules) grouped by body
 * segment, so limbs pivot at real joints: hip, knee, shoulder, elbow. Detail that tells the story:
 * a red protest headband with its knot tails hanging behind, a khaki jacket with collar and cuffs,
 * a canvas backpack with straps, flap and side pouches, jeans with a belt, and sneakers.
 *
 * Facing: local +Z is forward, so the backpack and headband tails sit on -Z.
 */
public class StudentMesh implements Disposable {

    private static final int DIV = 14;

    /** One rounded part, positioned (and optionally tilted) in its body segment's local frame. */
    private static final class Part {
        final ModelInstance inst;
        final float x, y, z, rx, ry, rz;

        Part(ModelInstance inst, float x, float y, float z, float rx, float ry, float rz) {
            this.inst = inst;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
        }
    }

    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Array<Model> models = new Array<>();
    private long attr;

    // Body segments; each part list is drawn relative to that segment's transform
    private final Array<Part> pelvisParts = new Array<>();
    private final Array<Part> chestParts = new Array<>();
    private final Array<Part> headParts = new Array<>();
    private final Array<Part>[] upperArmParts = newPartArrays();
    private final Array<Part>[] foreArmParts = newPartArrays();
    private final Array<Part>[] thighParts = newPartArrays();
    private final Array<Part>[] shinParts = newPartArrays();
    private final Array<Part>[] footParts = newPartArrays();

    private final Matrix4 rootTransform = new Matrix4();
    private final Matrix4 segment = new Matrix4();
    private final Matrix4 tmp = new Matrix4();

    private float idleTime = 0f;

    @SuppressWarnings("unchecked")
    private static Array<Part>[] newPartArrays() {
        return new Array[]{new Array<Part>(), new Array<Part>()};
    }

    public StudentMesh(TextureFactory textures) {
        createMeshes(textures);
    }

    // ----- building helpers -----

    private Model ellipsoidModel(float w, float h, float d, Material mat) {
        Model m = modelBuilder.createSphere(w, h, d, DIV, DIV - 2, mat, attr);
        models.add(m);
        return m;
    }

    private Model capsuleModel(float radius, float length, Material mat) {
        Model m = modelBuilder.createCapsule(radius, length, DIV, mat, attr);
        models.add(m);
        return m;
    }

    private Model cylinderModel(float w, float h, float d, Material mat) {
        Model m = modelBuilder.createCylinder(w, h, d, DIV, mat, attr);
        models.add(m);
        return m;
    }

    private void put(Array<Part> list, Model m, float x, float y, float z) {
        list.add(new Part(new ModelInstance(m), x, y, z, 0f, 0f, 0f));
    }

    private void put(Array<Part> list, Model m, float x, float y, float z, float rx, float ry, float rz) {
        list.add(new Part(new ModelInstance(m), x, y, z, rx, ry, rz));
    }

    /** Adds a part on both sides of the body: side 0 = left (-X), side 1 = right (+X). */
    private void putPair(Array<Part>[] lists, Model m, float x, float y, float z) {
        put(lists[0], m, -x, y, z);
        put(lists[1], m, x, y, z);
    }

    private static Material flat(float r, float g, float b) {
        return new Material(ColorAttribute.createDiffuse(new Color(r, g, b, 1f)));
    }

    private void createMeshes(TextureFactory textures) {
        attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        Material jacketMat = new Material(
            TextureAttribute.createDiffuse(textures.studentJacket),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.88f, 1f)));
        Material backpackMat = new Material(
            TextureAttribute.createDiffuse(textures.backpackFabric),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.88f, 1f)));

        Material skin = flat(0.66f, 0.48f, 0.35f);
        Material skinShade = flat(0.58f, 0.41f, 0.29f);   // ears, nose: a touch deeper
        Material hair = flat(0.06f, 0.045f, 0.04f);
        Material headband = flat(0.86f, 0.09f, 0.12f);    // July red
        Material sleeve = flat(0.60f, 0.56f, 0.40f);
        Material cuff = flat(0.50f, 0.46f, 0.32f);
        Material collarMat = flat(0.56f, 0.52f, 0.37f);
        Material denim = flat(0.19f, 0.25f, 0.36f);
        Material belt = flat(0.20f, 0.13f, 0.09f);
        Material strap = flat(0.32f, 0.30f, 0.24f);
        Material pouch = flat(0.24f, 0.26f, 0.20f);
        Material sneaker = flat(0.86f, 0.85f, 0.82f);
        Material sole = flat(0.12f, 0.11f, 0.11f);
        Material eye = flat(0.04f, 0.03f, 0.03f);
        Material mouth = flat(0.40f, 0.20f, 0.18f);

        // ---------- Pelvis (segment origin: hips, y = 0.87) ----------
        put(pelvisParts, ellipsoidModel(0.34f, 0.22f, 0.22f, denim), 0f, 0f, 0f);
        put(pelvisParts, cylinderModel(0.325f, 0.035f, 0.215f, belt), 0f, 0.085f, 0f);

        // ---------- Chest (segment origin: torso centre, y = 1.13) ----------
        put(chestParts, ellipsoidModel(0.44f, 0.60f, 0.27f, jacketMat), 0f, 0f, 0f);
        put(chestParts, ellipsoidModel(0.17f, 0.06f, 0.15f, collarMat), 0f, 0.295f, 0.012f);
        // Backpack: body, top flap, front pocket, side pouches
        put(chestParts, ellipsoidModel(0.34f, 0.44f, 0.22f, backpackMat), 0f, 0.03f, -0.20f);
        put(chestParts, ellipsoidModel(0.32f, 0.12f, 0.24f, pouch), 0f, 0.20f, -0.215f);
        put(chestParts, ellipsoidModel(0.24f, 0.18f, 0.10f, backpackMat), 0f, -0.10f, -0.31f);
        Model sidePouch = ellipsoidModel(0.09f, 0.20f, 0.11f, pouch);
        put(chestParts, sidePouch, -0.185f, -0.09f, -0.20f);
        put(chestParts, sidePouch, 0.185f, -0.09f, -0.20f);
        // Shoulder straps: over the shoulder, then down the chest front
        Model strapTop = ellipsoidModel(0.05f, 0.035f, 0.30f, strap);
        put(chestParts, strapTop, -0.115f, 0.262f, -0.02f);
        put(chestParts, strapTop, 0.115f, 0.262f, -0.02f);
        Model strapFront = ellipsoidModel(0.045f, 0.30f, 0.02f, strap);
        put(chestParts, strapFront, -0.12f, 0.08f, 0.118f);
        put(chestParts, strapFront, 0.12f, 0.08f, 0.118f);

        // ---------- Head (segment origin: head centre, y = 1.55) ----------
        put(headParts, ellipsoidModel(0.21f, 0.25f, 0.23f, skin), 0f, 0f, 0f);
        Model ear = ellipsoidModel(0.03f, 0.055f, 0.04f, skinShade);
        put(headParts, ear, -0.104f, -0.005f, 0f);
        put(headParts, ear, 0.104f, -0.005f, 0f);
        put(headParts, ellipsoidModel(0.032f, 0.05f, 0.045f, skinShade), 0f, -0.022f, 0.114f);   // nose
        Model eyeModel = ellipsoidModel(0.024f, 0.017f, 0.014f, eye);
        put(headParts, eyeModel, -0.04f, 0.018f, 0.099f);
        put(headParts, eyeModel, 0.04f, 0.018f, 0.099f);
        Model brow = ellipsoidModel(0.052f, 0.010f, 0.014f, hair);
        put(headParts, brow, -0.041f, 0.044f, 0.103f, 0f, 0f, 6f);
        put(headParts, brow, 0.041f, 0.044f, 0.103f, 0f, 0f, -6f);
        put(headParts, ellipsoidModel(0.05f, 0.011f, 0.012f, mouth), 0f, -0.062f, 0.104f);
        // Hair: cap over the crown and back, plus a fringe above the headband
        put(headParts, ellipsoidModel(0.235f, 0.22f, 0.255f, hair), 0f, 0.028f, -0.02f);
        put(headParts, ellipsoidModel(0.20f, 0.06f, 0.10f, hair), 0f, 0.083f, 0.06f);
        // Red headband with its knot tails hanging behind
        put(headParts, cylinderModel(0.226f, 0.038f, 0.238f, headband), 0f, 0.038f, 0.003f);
        put(headParts, ellipsoidModel(0.05f, 0.05f, 0.05f, headband), 0f, 0.030f, -0.128f);
        Model tail = capsuleModel(0.013f, 0.17f, headband);
        put(headParts, tail, -0.022f, -0.055f, -0.142f, -10f, 0f, -8f);
        put(headParts, tail, 0.022f, -0.05f, -0.146f, -14f, 0f, 9f);

        // ---------- Arms ----------
        // Upper arm segment origin: shoulder joint
        Model shoulderBall = ellipsoidModel(0.14f, 0.14f, 0.14f, sleeve);
        putPair(upperArmParts, shoulderBall, 0f, 0f, 0f);
        putPair(upperArmParts, capsuleModel(0.058f, 0.32f, sleeve), 0f, -0.15f, 0f);
        // Forearm segment origin: elbow
        putPair(foreArmParts, capsuleModel(0.050f, 0.31f, sleeve), 0f, -0.14f, 0f);
        putPair(foreArmParts, cylinderModel(0.10f, 0.035f, 0.10f, cuff), 0f, -0.235f, 0f);
        putPair(foreArmParts, ellipsoidModel(0.10f, 0.115f, 0.10f, skin), 0f, -0.30f, 0f);

        // ---------- Legs ----------
        // Thigh segment origin: hip joint
        putPair(thighParts, capsuleModel(0.090f, 0.44f, denim), 0f, -0.20f, 0f);
        // Shin segment origin: knee
        putPair(shinParts, ellipsoidModel(0.15f, 0.15f, 0.15f, denim), 0f, 0f, 0f);
        putPair(shinParts, capsuleModel(0.070f, 0.43f, denim), 0f, -0.20f, 0f);
        // Foot segment origin: ankle
        putPair(footParts, ellipsoidModel(0.12f, 0.09f, 0.28f, sneaker), 0f, -0.012f, 0.05f);
        putPair(footParts, ellipsoidModel(0.125f, 0.04f, 0.29f, sole), 0f, -0.048f, 0.05f);
    }

    // ----- drawing -----

    private void drawParts(ModelBatch batch, Environment env, Array<Part> parts, Matrix4 base) {
        for (int i = 0; i < parts.size; i++) {
            Part p = parts.get(i);
            p.inst.transform.set(base).translate(p.x, p.y, p.z);
            if (p.rx != 0f) p.inst.transform.rotate(Vector3.X, p.rx);
            if (p.ry != 0f) p.inst.transform.rotate(Vector3.Y, p.ry);
            if (p.rz != 0f) p.inst.transform.rotate(Vector3.Z, p.rz);
            batch.render(p.inst, env);
        }
    }

    /**
     * Updates and renders the student with animated locomotion: legs swing at the hip and bend at
     * the knee (foot stays level-ish), arms swing at the shoulder and bend at the elbow, the torso
     * counter-twists, and the head bobs slightly. When idle the chest gently breathes.
     */
    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting) {

        float bobOffset = 0f;
        float swing = 0f;
        idleTime += com.badlogic.gdx.Gdx.graphics != null ? com.badlogic.gdx.Gdx.graphics.getDeltaTime() : 0f;
        if (isMoving) {
            float swingScale = isSprinting ? 38f : 24f;
            swing = MathUtils.sin(walkCycle) * swingScale;
            bobOffset = Math.abs(MathUtils.sin(walkCycle * 2f)) * (isSprinting ? 0.04f : 0.025f);
        }
        float breathe = isMoving ? 0f : MathUtils.sin(idleTime * 2.2f) * 0.006f;

        rootTransform.idt();
        rootTransform.translate(pos.x, pos.y + bobOffset, pos.z);
        rootTransform.rotate(Vector3.Y, headingDegrees);

        float twist = swing * 0.12f;

        segment.set(rootTransform).translate(0f, 0.87f, 0f).rotate(Vector3.Y, twist);
        drawParts(batch, env, pelvisParts, segment);

        segment.set(rootTransform).translate(0f, 1.13f + breathe, 0f).rotate(Vector3.Y, -twist);
        drawParts(batch, env, chestParts, segment);

        // Head: slight forward lean when sprinting, tiny counter-bob so it doesn't move rigidly
        float headLean = isSprinting && isMoving ? 6f : 0f;
        segment.set(rootTransform).translate(0f, 1.55f + breathe - bobOffset * 0.3f, 0.005f)
            .rotate(Vector3.X, headLean).rotate(Vector3.Y, -twist * 0.5f);
        drawParts(batch, env, headParts, segment);

        for (int side = 0; side < 2; side++) {
            float sx = side == 0 ? -1f : 1f;
            float legAngle = side == 0 ? swing : -swing;
            float armAngle = side == 0 ? -swing : swing;

            // Leg: hip swing, knee flexes the shin backward as the thigh comes forward
            float knee = isMoving ? (legAngle < 0f ? -legAngle * 1.4f : legAngle * 0.3f) + 4f : 0f;
            segment.set(rootTransform).translate(sx * 0.10f, 0.85f, 0f).rotate(Vector3.X, legAngle);
            drawParts(batch, env, thighParts[side], segment);
            segment.translate(0f, -0.40f, 0f).rotate(Vector3.X, knee);
            drawParts(batch, env, shinParts[side], segment);
            // Ankle: keep the sneaker close to flat against the ground as the leg swings
            tmp.set(segment).translate(0f, -0.40f, 0f).rotate(Vector3.X, -(legAngle + knee) * 0.75f);
            drawParts(batch, env, footParts[side], tmp);

            // Arm: shoulder swing, elbow bends forward while moving
            float elbow = isMoving ? -(8f + Math.abs(armAngle) * 0.5f) : -4f;
            segment.set(rootTransform).translate(sx * 0.235f, 1.33f + breathe, 0f).rotate(Vector3.X, armAngle);
            drawParts(batch, env, upperArmParts[side], segment);
            segment.translate(0f, -0.29f, 0f).rotate(Vector3.X, elbow);
            drawParts(batch, env, foreArmParts[side], segment);
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
