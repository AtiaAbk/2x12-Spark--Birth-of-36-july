package bd.spark36.character;

import com.badlogic.gdx.Gdx;
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
 * High-fidelity 3D Student Protagonist for 2x12: Spark.
 * Faithfully reimagined from the official "Where Winds Meet Style" Concept Design Board (Section 11 Male Student):
 * - Handsome, expressive South Asian facial structure (tapered jaw, sculpted chin, expressive eyes with sclera & pupil, defined nose, natural lips)
 * - Layered wavy modern side-parted textured hairstyle
 * - Earthy khaki/brown utility jacket over a white/cream crewneck t-shirt
 * - Sculpted hands with knuckles and thumb joints for combat strikes
 * - Indigo denim jeans with leather belt and silver buckle
 * - Leather trail sneakers with rubber soles
 * - Canvas rucksack with dual shoulder straps and utility pouches
 * - Seamless procedural animation for Idle, Walk, Sprint, Crouch, Combat Attacks, and Sitting by the Water.
 *
 * Local orientation: +Z is forward, -Z is rear (backpack), +X is character right, -X is character left.
 */
public class StudentMesh implements Disposable {

    private static final int DIV = 16;

    public static final class Part {
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

    // Body segments
    private final Array<Part> pelvisParts = new Array<>();
    private final Array<Part> chestParts = new Array<>();
    private final Array<Part> neckParts = new Array<>();
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

    // ----- Primitive creation helpers -----

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

    private Model boxModel(float w, float h, float d, Material mat) {
        Model m = modelBuilder.createBox(w, h, d, mat, attr);
        models.add(m);
        return m;
    }

    private void put(Array<Part> list, Model m, float x, float y, float z) {
        list.add(new Part(new ModelInstance(m), x, y, z, 0f, 0f, 0f));
    }

    private void put(Array<Part> list, Model m, float x, float y, float z, float rx, float ry, float rz) {
        list.add(new Part(new ModelInstance(m), x, y, z, rx, ry, rz));
    }

    private void putPair(Array<Part>[] lists, Model m, float x, float y, float z) {
        put(lists[0], m, -x, y, z);
        put(lists[1], m, x, y, z);
    }

    private void putPair(Array<Part>[] lists, Model m, float x, float y, float z, float rx, float ry, float rz) {
        put(lists[0], m, -x, y, z, rx, -ry, -rz);
        put(lists[1], m, x, y, z, rx, ry, rz);
    }

    private static Material flat(float r, float g, float b) {
        return new Material(ColorAttribute.createDiffuse(new Color(r, g, b, 1f)));
    }

    private void createMeshes(TextureFactory textures) {
        attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        // ---------- Sophisticated Palette matching Concept Board (Section 11) ----------
        // Skin & Features
        Material skin = flat(0.74f, 0.54f, 0.40f);         // Warm golden South Asian tone
        Material skinShade = flat(0.62f, 0.44f, 0.32f);    // Jawline, nostrils, ear concha
        Material eyeWhite = flat(0.96f, 0.96f, 0.98f);     // Realistic sclera
        Material iris = flat(0.12f, 0.08f, 0.06f);         // Dark warm brown iris
        Material pupil = flat(0.02f, 0.02f, 0.02f);        // Deep pupil
        Material brow = flat(0.09f, 0.06f, 0.05f);         // Natural dark brow
        Material lips = flat(0.55f, 0.34f, 0.30f);         // Natural warm lips
        Material hair = flat(0.07f, 0.05f, 0.045f);        // Wavy espresso-black hair
        Material hairHighlight = flat(0.12f, 0.09f, 0.08f);

        // Attire
        Material innerShirt = flat(0.92f, 0.90f, 0.86f);   // Clean white/cream crewneck t-shirt
        Material jacketBody = flat(0.48f, 0.38f, 0.26f);   // Earthy khaki/brown utility jacket
        Material jacketTrim = flat(0.38f, 0.30f, 0.20f);   // Lapels, cuffs, shoulder flaps
        Material denim = flat(0.18f, 0.24f, 0.35f);        // Indigo washed denim
        Material beltLeather = flat(0.18f, 0.11f, 0.08f);  // Leather belt
        Material buckle = flat(0.85f, 0.85f, 0.90f);       // Metallic belt buckle
        Material sneakerLeather = flat(0.30f, 0.20f, 0.14f);// Brown leather trail shoe
        Material sole = flat(0.88f, 0.88f, 0.85f);         // Clean rubber sneaker sole
        Material backpackCanvas = flat(0.35f, 0.30f, 0.22f);
        Material backpackStrap = flat(0.22f, 0.18f, 0.14f);

        // ---------- 1. Pelvis & Waist (Origin: y = 0.87) ----------
        put(pelvisParts, ellipsoidModel(0.33f, 0.22f, 0.22f, denim), 0f, 0f, 0f);
        put(pelvisParts, cylinderModel(0.325f, 0.040f, 0.215f, beltLeather), 0f, 0.085f, 0f);
        // Belt buckle
        put(pelvisParts, boxModel(0.045f, 0.038f, 0.015f, buckle), 0f, 0.085f, 0.110f);

        // ---------- 2. Chest & Torso (Origin: y = 1.13) ----------
        // Inner white crewneck shirt (visible at open jacket front)
        put(chestParts, ellipsoidModel(0.38f, 0.56f, 0.24f, innerShirt), 0f, 0.02f, 0.01f);
        put(chestParts, ellipsoidModel(0.16f, 0.06f, 0.14f, innerShirt), 0f, 0.285f, 0.02f);

        // Open Jacket Side Panels (Khaki/Brown Utility Jacket)
        // Left & right jacket bodies draping open across chest
        Model jacketPanel = ellipsoidModel(0.17f, 0.58f, 0.25f, jacketBody);
        put(chestParts, jacketPanel, -0.145f, 0.01f, 0.012f, 0f, 8f, 0f);
        put(chestParts, jacketPanel, 0.145f, 0.01f, 0.012f, 0f, -8f, 0f);
        // Jacket back cover
        put(chestParts, ellipsoidModel(0.40f, 0.58f, 0.14f, jacketBody), 0f, 0.01f, -0.07f);
        // Jacket lapel collars
        Model lapel = boxModel(0.04f, 0.22f, 0.02f, jacketTrim);
        put(chestParts, lapel, -0.09f, 0.18f, 0.115f, -8f, 12f, 15f);
        put(chestParts, lapel, 0.09f, 0.18f, 0.115f, -8f, -12f, -15f);

        // Canvas Backpack & Utility Pouches
        put(chestParts, ellipsoidModel(0.32f, 0.42f, 0.21f, backpackCanvas), 0f, 0.03f, -0.20f);
        put(chestParts, ellipsoidModel(0.30f, 0.12f, 0.22f, backpackStrap), 0f, 0.19f, -0.21f); // Top flap
        put(chestParts, ellipsoidModel(0.22f, 0.17f, 0.09f, backpackCanvas), 0f, -0.09f, -0.30f); // Lower pocket
        Model sidePouch = ellipsoidModel(0.08f, 0.18f, 0.10f, backpackCanvas);
        put(chestParts, sidePouch, -0.175f, -0.08f, -0.19f);
        put(chestParts, sidePouch, 0.175f, -0.08f, -0.19f);
        // Ergonomic Shoulder Straps curving over front
        Model strapTop = ellipsoidModel(0.045f, 0.035f, 0.28f, backpackStrap);
        put(chestParts, strapTop, -0.11f, 0.26f, -0.02f);
        put(chestParts, strapTop, 0.11f, 0.26f, -0.02f);
        Model strapFront = ellipsoidModel(0.040f, 0.28f, 0.02f, backpackStrap);
        put(chestParts, strapFront, -0.115f, 0.08f, 0.115f);
        put(chestParts, strapFront, 0.115f, 0.08f, 0.115f);

        // ---------- 3. Neck (Origin: y = 1.42) ----------
        put(neckParts, cylinderModel(0.11f, 0.14f, 0.11f, skin), 0f, 0f, 0.015f);
        put(neckParts, ellipsoidModel(0.03f, 0.04f, 0.025f, skinShade), 0f, -0.01f, 0.075f); // Adam's apple

        // ---------- 4. Head & Face (Origin: head centre, y = 1.55) ----------
        // Cranium & Cheekbones
        put(headParts, ellipsoidModel(0.185f, 0.22f, 0.20f, skin), 0f, 0.01f, 0f);
        // Tapered South Asian Jawline & Sculpted Chin (defined, handsome jaw)
        put(headParts, ellipsoidModel(0.145f, 0.13f, 0.145f, skin), 0f, -0.075f, 0.035f);
        put(headParts, ellipsoidModel(0.060f, 0.050f, 0.060f, skin), 0f, -0.125f, 0.075f); // Defined chin

        // Sculpted Ears
        Model ear = ellipsoidModel(0.026f, 0.050f, 0.038f, skinShade);
        put(headParts, ear, -0.095f, -0.005f, 0f, 0f, -12f, 0f);
        put(headParts, ear, 0.095f, -0.005f, 0f, 0f, 12f, 0f);

        // Expressive Eyes: Sclera + Iris + Pupil (crisp, prominent, handsome)
        Model eyeWhiteMesh = ellipsoidModel(0.034f, 0.022f, 0.018f, eyeWhite);
        Model eyeIrisMesh = ellipsoidModel(0.022f, 0.022f, 0.014f, iris);
        Model eyePupilMesh = ellipsoidModel(0.010f, 0.010f, 0.010f, pupil);

        // Left Eye
        put(headParts, eyeWhiteMesh, -0.042f, 0.016f, 0.102f, 0f, -6f, 0f);
        put(headParts, eyeIrisMesh, -0.042f, 0.016f, 0.110f, 0f, -6f, 0f);
        put(headParts, eyePupilMesh, -0.042f, 0.016f, 0.115f, 0f, -6f, 0f);

        // Right Eye
        put(headParts, eyeWhiteMesh, 0.042f, 0.016f, 0.102f, 0f, 6f, 0f);
        put(headParts, eyeIrisMesh, 0.042f, 0.016f, 0.110f, 0f, 6f, 0f);
        put(headParts, eyePupilMesh, 0.042f, 0.016f, 0.115f, 0f, 6f, 0f);

        // Eyebrows (Arched & Determined, dark contrast)
        Model browL = boxModel(0.052f, 0.012f, 0.016f, brow);
        put(headParts, browL, -0.043f, 0.040f, 0.110f, -4f, 0f, 7f);
        put(headParts, browL, 0.043f, 0.040f, 0.110f, -4f, 0f, -7f);

        // Defined Nose Bridge and Tip
        put(headParts, capsuleModel(0.016f, 0.065f, skin), 0f, -0.010f, 0.112f, 15f, 0f, 0f); // Bridge
        put(headParts, ellipsoidModel(0.028f, 0.022f, 0.026f, skinShade), 0f, -0.038f, 0.125f); // Tip & nostrils

        // Sculpted Proportional Lips (warm natural tone)
        put(headParts, ellipsoidModel(0.046f, 0.014f, 0.016f, lips), 0f, -0.072f, 0.108f); // Upper
        put(headParts, ellipsoidModel(0.044f, 0.016f, 0.018f, lips), 0f, -0.086f, 0.106f); // Lower

        // Modern Layered Side-Parted Hairstyle (Where Winds Meet Style, Section 11)
        // Main hair volume over crown and back
        put(headParts, ellipsoidModel(0.210f, 0.180f, 0.220f, hair), 0f, 0.050f, -0.025f);
        // Styled Side-Sweep volume (parted on left, sweeping across right)
        put(headParts, ellipsoidModel(0.170f, 0.080f, 0.170f, hairHighlight), 0.025f, 0.115f, 0.010f, -10f, 15f, -8f);
        // Front fringe locks - neatly framing the forehead without covering eyes
        put(headParts, ellipsoidModel(0.130f, 0.050f, 0.070f, hair), 0.035f, 0.085f, 0.080f, -15f, 10f, -12f);
        put(headParts, ellipsoidModel(0.075f, 0.040f, 0.055f, hair), -0.055f, 0.080f, 0.075f, -10f, -15f, 8f);
        // Sideburns and temple tapers
        put(headParts, capsuleModel(0.014f, 0.070f, hair), -0.092f, 0.015f, 0.035f, 10f, 0f, 0f);
        put(headParts, capsuleModel(0.014f, 0.070f, hair), 0.092f, 0.015f, 0.035f, 10f, 0f, 0f);

        // ---------- 5. Arms & Hands ----------
        // Shoulder Joint
        Model shoulderBall = ellipsoidModel(0.135f, 0.135f, 0.135f, jacketBody);
        putPair(upperArmParts, shoulderBall, 0f, 0f, 0f);
        putPair(upperArmParts, capsuleModel(0.056f, 0.30f, jacketBody), 0f, -0.14f, 0f);

        // Forearm & Wrist Cuff
        putPair(foreArmParts, capsuleModel(0.048f, 0.29f, jacketBody), 0f, -0.13f, 0f);
        putPair(foreArmParts, cylinderModel(0.095f, 0.035f, 0.095f, jacketTrim), 0f, -0.225f, 0f);

        // Sculpted Hands (Palm + Thumb + Knuckles for combat punches)
        Model palm = ellipsoidModel(0.075f, 0.085f, 0.055f, skin);
        putPair(foreArmParts, palm, 0f, -0.285f, 0f);
        Model knuckles = boxModel(0.065f, 0.025f, 0.045f, skinShade);
        putPair(foreArmParts, knuckles, 0f, -0.320f, 0.010f);

        // ---------- 6. Legs & Feet ----------
        // Thigh (Indigo Denim)
        putPair(thighParts, capsuleModel(0.088f, 0.44f, denim), 0f, -0.20f, 0f);

        // Shin (Denim knee & calf)
        putPair(shinParts, ellipsoidModel(0.14f, 0.14f, 0.14f, denim), 0f, 0f, 0f);
        putPair(shinParts, capsuleModel(0.068f, 0.42f, denim), 0f, -0.20f, 0f);

        // Sturdy Trail Sneakers (Brown leather upper, white athletic rubber sole)
        putPair(footParts, ellipsoidModel(0.115f, 0.085f, 0.27f, sneakerLeather), 0f, -0.012f, 0.05f);
        putPair(footParts, ellipsoidModel(0.122f, 0.038f, 0.28f, sole), 0f, -0.046f, 0.05f);
    }

    // ----- Drawing Helpers -----

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
     * Backward-compatible render call.
     */
    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               false, false, 0, 0f, false, 0f);
    }

    /**
     * Master procedural rendering method supporting:
     * - Idle breathing & alert stance
     * - Athletic Walk & Sprint cycles
     * - Tactical Crouch & Ducking under low clearance
     * - 3-Hit Combat Martial Arts combo (Jab, Cross Hook, Spin Kick)
     * - Contemplative Sitting by the Water on the grand pukur ghat with feet dipped
     */
    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress) {

        float delta = Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : 0.016f;
        idleTime += delta;

        // Base procedural animation channels
        float bobOffset = 0f;
        float swing = 0f;
        float pelvisHeight = 0.87f;
        float torsoPitch = 0f;
        float headPitch = 0f;
        float torsoTwist = 0f;

        if (isMoving && !isSittingWater) {
            float swingScale = isSprinting ? 38f : (isCrouching ? 16f : 24f);
            swing = MathUtils.sin(walkCycle) * swingScale;
            bobOffset = Math.abs(MathUtils.sin(walkCycle * 2f)) * (isSprinting ? 0.04f : 0.022f);
        }

        float breathe = (!isMoving && !isAttacking) ? MathUtils.sin(idleTime * 2.2f) * 0.007f : 0f;

        // ---------- Stance Adjustments ----------
        if (isCrouching) {
            // Low tactical crouch: drop pelvis, lean torso forward, tilt head up
            pelvisHeight = 0.46f;
            torsoPitch = 32f;
            headPitch = -20f;
        }

        if (isSittingWater) {
            // Seated on the stone curb / ghat steps
            pelvisHeight = 0.38f;
            torsoPitch = -8f; // Relaxed recline onto hands
        }

        // ---------- Combat Strike Overrides ----------
        float attackArmR = 0f, attackForeArmR = 0f;
        float attackArmL = 0f, attackForeArmL = 0f;
        float attackLegR = 0f, attackKneeR = 0f;

        if (isAttacking) {
            float t = MathUtils.clamp(attackProgress, 0f, 1f);
            float strikePower = MathUtils.sin(t * MathUtils.PI); // 0 -> 1 -> 0 peak at midway

            if (attackCombo == 1) {
                // Combo 1: Fast Right Straight Jab
                torsoTwist = -28f * strikePower;
                attackArmR = -85f * strikePower;
                attackForeArmR = -12f * strikePower;
                // Guard left hand
                attackArmL = -45f * strikePower;
                attackForeArmL = -85f * strikePower;
            } else if (attackCombo == 2) {
                // Combo 2: Powerful Left Cross Hook
                torsoTwist = 36f * strikePower;
                attackArmL = -75f * strikePower;
                attackForeArmL = -80f * strikePower;
                // Guard right hand
                attackArmR = -50f * strikePower;
                attackForeArmR = -75f * strikePower;
            } else if (attackCombo == 3) {
                // Combo 3: Athletic Spinning Roundhouse Kick
                torsoPitch = -15f * strikePower;
                torsoTwist = -45f * strikePower;
                attackLegR = -90f * strikePower;
                attackKneeR = 25f * strikePower;
            }
        }

        // ---------- Root Transformation ----------
        rootTransform.idt();
        rootTransform.translate(pos.x, pos.y + bobOffset, pos.z);
        rootTransform.rotate(Vector3.Y, headingDegrees);

        // Twist from movement swing or combat strike
        float twist = isAttacking ? torsoTwist : (swing * 0.12f);

        // Pelvis
        segment.set(rootTransform).translate(0f, pelvisHeight, 0f).rotate(Vector3.Y, twist);
        drawParts(batch, env, pelvisParts, segment);

        // Chest & Torso (Spine pivots from pelvis)
        float chestY = isCrouching ? 0.72f : (isSittingWater ? 0.65f : 1.13f);
        float chestZ = isCrouching ? 0.12f : 0f;
        segment.set(rootTransform).translate(0f, chestY + breathe, chestZ)
            .rotate(Vector3.Y, -twist)
            .rotate(Vector3.X, torsoPitch);
        drawParts(batch, env, chestParts, segment);

        // Neck
        float neckY = isCrouching ? 0.94f : (isSittingWater ? 0.88f : 1.42f);
        float neckZ = isCrouching ? 0.22f : 0f;
        segment.set(rootTransform).translate(0f, neckY + breathe, neckZ)
            .rotate(Vector3.Y, -twist * 0.5f)
            .rotate(Vector3.X, torsoPitch * 0.7f);
        drawParts(batch, env, neckParts, segment);

        // Head (Head look pitch & lean)
        float sprintHeadLean = isSprinting && isMoving ? 8f : 0f;
        float headY = isCrouching ? 1.05f : (isSittingWater ? 0.99f : 1.55f);
        float headZ = isCrouching ? 0.28f : 0f;
        segment.set(rootTransform).translate(0f, headY + breathe - bobOffset * 0.3f, headZ)
            .rotate(Vector3.X, headPitch + sprintHeadLean)
            .rotate(Vector3.Y, -twist * 0.3f);
        drawParts(batch, env, headParts, segment);

        // ---------- Limbs (Arms & Legs) ----------
        for (int side = 0; side < 2; side++) {
            float sx = side == 0 ? -1f : 1f;

            if (isSittingWater) {
                // ----- Seated Pose: Dangle and splash feet in water -----
                // Thighs extended forward over the curb
                float sitThighAngle = -85f;
                segment.set(rootTransform).translate(sx * 0.11f, pelvisHeight - 0.04f, 0.08f)
                    .rotate(Vector3.X, sitThighAngle);
                drawParts(batch, env, thighParts[side], segment);

                // Shins hanging down into the water
                float shinDangleAngle = 82f;
                // Alternate water splashing kick
                float kickPhase = idleTime * 2.8f + (side == 0 ? 0f : MathUtils.PI);
                float footKick = MathUtils.sin(kickPhase) * 12f;
                segment.translate(0f, -0.40f, 0f).rotate(Vector3.X, shinDangleAngle + footKick);
                drawParts(batch, env, shinParts[side], segment);

                // Feet dipped in water
                tmp.set(segment).translate(0f, -0.40f, 0f).rotate(Vector3.X, -10f);
                drawParts(batch, env, footParts[side], tmp);

                // Arms resting gently at sides on the curb
                segment.set(rootTransform).translate(sx * 0.23f, chestY + 0.10f, -0.06f)
                    .rotate(Vector3.X, 15f)
                    .rotate(Vector3.Z, sx * -12f);
                drawParts(batch, env, upperArmParts[side], segment);
                segment.translate(0f, -0.28f, 0f).rotate(Vector3.X, -25f);
                drawParts(batch, env, foreArmParts[side], segment);

            } else if (isCrouching) {
                // ----- Crouch Pose: Knees flexed deep, hands ready -----
                float crouchLegSwing = isMoving ? swing * 0.7f : 0f;
                float legAngle = (side == 0 ? crouchLegSwing : -crouchLegSwing) + 48f;
                float kneeFlex = -78f;

                segment.set(rootTransform).translate(sx * 0.12f, pelvisHeight - 0.03f, 0.04f)
                    .rotate(Vector3.X, legAngle);
                drawParts(batch, env, thighParts[side], segment);
                segment.translate(0f, -0.38f, 0f).rotate(Vector3.X, kneeFlex);
                drawParts(batch, env, shinParts[side], segment);
                tmp.set(segment).translate(0f, -0.38f, 0f).rotate(Vector3.X, -(legAngle + kneeFlex));
                drawParts(batch, env, footParts[side], tmp);

                // Low tactical arm carry
                float armAngle = (side == 0 ? -swing : swing) * 0.5f - 24f;
                segment.set(rootTransform).translate(sx * 0.22f, chestY + 0.12f, chestZ)
                    .rotate(Vector3.X, armAngle + torsoPitch * 0.5f);
                drawParts(batch, env, upperArmParts[side], segment);
                segment.translate(0f, -0.28f, 0f).rotate(Vector3.X, -45f);
                drawParts(batch, env, foreArmParts[side], segment);

            } else {
                // ----- Standard Locomotion & Combat Strikes -----
                float legAngle = side == 0 ? swing : -swing;
                float armAngle = side == 0 ? -swing : swing;

                if (isAttacking) {
                    if (side == 1 && attackLegR != 0f) {
                        legAngle = attackLegR;
                    }
                    if (side == 1 && attackArmR != 0f) {
                        armAngle = attackArmR;
                    } else if (side == 0 && attackArmL != 0f) {
                        armAngle = attackArmL;
                    }
                }

                // Leg swing & knee flexion
                float knee = isMoving ? (legAngle < 0f ? -legAngle * 1.4f : legAngle * 0.3f) + 4f : 0f;
                if (isAttacking && side == 1 && attackKneeR != 0f) {
                    knee = attackKneeR;
                }

                segment.set(rootTransform).translate(sx * 0.10f, 0.85f, 0f).rotate(Vector3.X, legAngle);
                drawParts(batch, env, thighParts[side], segment);
                segment.translate(0f, -0.40f, 0f).rotate(Vector3.X, knee);
                drawParts(batch, env, shinParts[side], segment);
                tmp.set(segment).translate(0f, -0.40f, 0f).rotate(Vector3.X, -(legAngle + knee) * 0.75f);
                drawParts(batch, env, footParts[side], tmp);

                // Arm swing and elbow bend
                float elbow = isMoving ? -(8f + Math.abs(armAngle) * 0.5f) : -4f;
                if (isAttacking) {
                    if (side == 1 && attackForeArmR != 0f) elbow = attackForeArmR;
                    else if (side == 0 && attackForeArmL != 0f) elbow = attackForeArmL;
                }

                segment.set(rootTransform).translate(sx * 0.235f, 1.33f + breathe, 0f).rotate(Vector3.X, armAngle);
                drawParts(batch, env, upperArmParts[side], segment);
                segment.translate(0f, -0.29f, 0f).rotate(Vector3.X, elbow);
                drawParts(batch, env, foreArmParts[side], segment);
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
