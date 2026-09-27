import re

mesh_code = '''package bd.spark36.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
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
 * Ultra-polished 3D student protagonist supporting both Male and Female character models,
 * faithfully aligned with the reference photograph:
 * - Male Student: Dark Navy striped polo shirt, classic blue denim jeans, athletic sneakers,
 *                 textured youthful college hair, and Dhaka University ID card lanyard.
 * - Female Student: Crisp light sky-blue Oxford shirt, dark indigo slim jeans, summer sandals,
 *                   flowing long hair, gold pendant necklace, and DU ID card lanyard.
 *
 * Full procedural kinetic rigging:
 * - Idle breathing & alert posture
 * - Dynamic Walk & Sprint cycles
 * - Low tactical crouch/ducking
 * - 3-Hit Protest Defense Martial Combo (Jab -> Cross Hook -> Flying Spin Kick)
 * - Contemplative Sitting by the Water on the Curzon Hall grand ghat steps with submerged feet
 */
public class StudentMesh implements Disposable {

    private static final int DIV = 24; // High polygon smoothness

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

    // Body segments indexed by gender: [0] = Male, [1] = Female
    @SuppressWarnings("unchecked")
    private final Array<Part>[] pelvisParts = new Array[]{new Array<Part>(), new Array<Part>()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[] chestParts = new Array[]{new Array<Part>(), new Array<Part>()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[] neckParts = new Array[]{new Array<Part>(), new Array<Part>()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[] headParts = new Array[]{new Array<Part>(), new Array<Part>()};

    @SuppressWarnings("unchecked")
    private final Array<Part>[][] upperArmParts = new Array[][]{newPartArrays(), newPartArrays()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[][] foreArmParts = new Array[][]{newPartArrays(), newPartArrays()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[][] thighParts = new Array[][]{newPartArrays(), newPartArrays()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[][] shinParts = new Array[][]{newPartArrays(), newPartArrays()};
    @SuppressWarnings("unchecked")
    private final Array<Part>[][] footParts = new Array[][]{newPartArrays(), newPartArrays()};

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

        // Shared realistic materials
        Material eyeWhite = flat(0.96f, 0.96f, 0.98f);
        Material iris = flat(0.22f, 0.14f, 0.09f);
        Material pupil = flat(0.02f, 0.02f, 0.02f);
        Material catchlight = flat(0.99f, 0.99f, 1.0f);
        Material upperLid = flat(0.14f, 0.09f, 0.07f);
        Material browMale = flat(0.08f, 0.06f, 0.05f);
        Material browFemale = flat(0.28f, 0.16f, 0.10f);
        Material lipsMale = flat(0.70f, 0.46f, 0.42f);
        Material lipsFemale = flat(0.80f, 0.46f, 0.44f);
        Material hairMale = flat(0.07f, 0.06f, 0.05f);
        Material hairMaleHighlight = flat(0.12f, 0.10f, 0.08f);
        Material hairFemale = flat(0.42f, 0.22f, 0.12f);
        Material hairFemaleHighlight = flat(0.55f, 0.30f, 0.18f);

        // Authentic Dhaka University ID Card & Lanyard
        Material lanyardBlue = flat(0.10f, 0.35f, 0.85f);
        Material idCardWhite = flat(0.98f, 0.98f, 0.99f);
        Material idCardHeader = flat(0.10f, 0.35f, 0.85f);
        Material idPhoto = flat(0.62f, 0.48f, 0.38f);
        Material idClip = flat(0.85f, 0.85f, 0.90f);

        // Male Wardrobe (Reference: Navy striped polo, blue jeans, athletic sneakers)
        Material skinMale = flat(0.82f, 0.66f, 0.54f);
        Material skinMaleHighlight = flat(0.86f, 0.70f, 0.58f);
        Material skinMaleShade = flat(0.72f, 0.56f, 0.44f);
        Material poloNavy = flat(0.11f, 0.14f, 0.24f);
        Material poloNavyTrim = flat(0.08f, 0.10f, 0.18f);
        Material stripeWhite = flat(0.95f, 0.95f, 0.98f);
        Material pearlButton = flat(0.92f, 0.92f, 0.90f);
        Material denimBlue = flat(0.20f, 0.28f, 0.44f);
        Material denimBlueShade = flat(0.14f, 0.20f, 0.32f);
        Material beltLeather = flat(0.15f, 0.10f, 0.07f);
        Material buckleSteel = flat(0.85f, 0.86f, 0.88f);
        Material sneakerDark = flat(0.12f, 0.12f, 0.14f);
        Material sneakerWhite = flat(0.92f, 0.92f, 0.92f);
        Material backpackCanvas = flat(0.24f, 0.22f, 0.20f);
        Material backpackStrap = flat(0.14f, 0.13f, 0.12f);
        Material watchBand = flat(0.12f, 0.12f, 0.12f);
        Material watchFace = flat(0.04f, 0.04f, 0.06f);

        // Female Wardrobe (Reference: Sky-blue Oxford shirt, dark jeans, yellow sandals, gold necklace)
        Material skinFemale = flat(0.85f, 0.70f, 0.58f);
        Material skinFemaleHighlight = flat(0.89f, 0.74f, 0.62f);
        Material skinFemaleShade = flat(0.75f, 0.60f, 0.48f);
        Material shirtSkyBlue = flat(0.66f, 0.78f, 0.92f);
        Material shirtSkyBlueTrim = flat(0.58f, 0.70f, 0.85f);
        Material denimDark = flat(0.13f, 0.17f, 0.28f);
        Material denimDarkShade = flat(0.09f, 0.12f, 0.20f);
        Material necklaceGold = flat(0.94f, 0.80f, 0.32f);
        Material sandalYellow = flat(0.94f, 0.76f, 0.22f);
        Material sandalSole = flat(0.78f, 0.62f, 0.44f);

        // =========================================================================
        // BUILD MALE STUDENT (Gender = 0)
        // =========================================================================
        // 1. Pelvis & Waist (Origin: y = 0.87)
        put(pelvisParts[0], cylinderModel(0.29f, 0.16f, 0.20f, denimBlue), 0f, 0f, 0f);
        put(pelvisParts[0], ellipsoidModel(0.30f, 0.16f, 0.21f, denimBlue), 0f, -0.01f, 0f);
        put(pelvisParts[0], cylinderModel(0.298f, 0.042f, 0.202f, denimBlue), 0f, 0.075f, 0f);
        put(pelvisParts[0], cylinderModel(0.302f, 0.030f, 0.205f, beltLeather), 0f, 0.075f, 0f);
        put(pelvisParts[0], boxModel(0.040f, 0.030f, 0.012f, buckleSteel), 0f, 0.075f, 0.106f);

        // 2. Chest & Torso: Navy Striped Polo (Origin: y = 1.13)
        put(chestParts[0], cylinderModel(0.315f, 0.43f, 0.205f, poloNavy), 0f, 0.02f, -0.005f);
        put(chestParts[0], ellipsoidModel(0.325f, 0.18f, 0.210f, poloNavy), 0f, 0.13f, 0.005f);
        put(chestParts[0], ellipsoidModel(0.26f, 0.14f, 0.17f, poloNavy), 0f, 0.20f, -0.010f);

        // Fine horizontal white pinstripes across chest
        for (int s = 0; s < 4; s++) {
            float sy = 0.16f - s * 0.075f;
            put(chestParts[0], cylinderModel(0.317f, 0.009f, 0.207f, stripeWhite), 0f, sy, -0.005f);
        }

        // Rounded natural deltoid shoulders
        Model poloShoulderM = ellipsoidModel(0.110f, 0.085f, 0.130f, poloNavy);
        put(chestParts[0], poloShoulderM, -0.170f, 0.185f, 0.005f, 0f, 0f, -12f);
        put(chestParts[0], poloShoulderM, 0.170f, 0.185f, 0.005f, 0f, 0f, 12f);

        // Ribbed polo collar & button placket
        Model poloCollar = boxModel(0.038f, 0.12f, 0.016f, poloNavyTrim);
        put(chestParts[0], poloCollar, -0.058f, 0.205f, 0.092f, -12f, 18f, 22f);
        put(chestParts[0], poloCollar, 0.058f, 0.205f, 0.092f, -12f, -18f, -22f);
        put(chestParts[0], boxModel(0.022f, 0.15f, 0.010f, poloNavyTrim), 0f, 0.12f, 0.105f);
        put(chestParts[0], ellipsoidModel(0.008f, 0.008f, 0.006f, pearlButton), 0f, 0.15f, 0.112f);
        put(chestParts[0], ellipsoidModel(0.008f, 0.008f, 0.006f, pearlButton), 0f, 0.09f, 0.112f);

        // DU ID Card & Lanyard on Male Chest
        Model ribTopM = boxModel(0.015f, 0.12f, 0.004f, lanyardBlue);
        put(chestParts[0], ribTopM, -0.052f, 0.21f, 0.078f, 20f, 0f, 10f);
        put(chestParts[0], ribTopM, 0.052f, 0.21f, 0.078f, 20f, 0f, -10f);
        Model ribChestLM = boxModel(0.015f, 0.24f, 0.004f, lanyardBlue);
        Model ribChestRM = boxModel(0.015f, 0.24f, 0.004f, lanyardBlue);
        put(chestParts[0], ribChestLM, -0.030f, 0.08f, 0.112f, 0f, 0f, 12f);
        put(chestParts[0], ribChestRM, 0.030f, 0.08f, 0.112f, 0f, 0f, -12f);
        put(chestParts[0], boxModel(0.014f, 0.020f, 0.008f, idClip), 0f, -0.022f, 0.118f);
        put(chestParts[0], boxModel(0.082f, 0.110f, 0.004f, idCardWhite), 0f, -0.082f, 0.120f);
        put(chestParts[0], boxModel(0.082f, 0.024f, 0.005f, idCardHeader), 0f, -0.036f, 0.121f);
        put(chestParts[0], boxModel(0.032f, 0.038f, 0.006f, idPhoto), -0.018f, -0.072f, 0.122f);
        put(chestParts[0], boxModel(0.024f, 0.005f, 0.006f, idCardHeader), 0.018f, -0.062f, 0.122f);
        put(chestParts[0], boxModel(0.024f, 0.004f, 0.006f, denimBlue), 0.018f, -0.072f, 0.122f);
        put(chestParts[0], boxModel(0.024f, 0.004f, 0.006f, denimBlue), 0.018f, -0.082f, 0.122f);

        // Backpack on back
        put(chestParts[0], ellipsoidModel(0.27f, 0.36f, 0.15f, backpackCanvas), 0f, 0.04f, -0.165f);
        put(chestParts[0], capsuleModel(0.018f, 0.20f, backpackStrap), -0.105f, 0.22f, -0.035f, 30f, 0f, 0f);
        put(chestParts[0], capsuleModel(0.018f, 0.20f, backpackStrap), 0.105f, 0.22f, -0.035f, 30f, 0f, 0f);
        put(chestParts[0], capsuleModel(0.016f, 0.24f, backpackStrap), -0.105f, 0.08f, 0.105f, -6f, 0f, 0f);
        put(chestParts[0], capsuleModel(0.016f, 0.24f, backpackStrap), 0.105f, 0.08f, 0.105f, -6f, 0f, 0f);

        // 3. Male Neck
        put(neckParts[0], cylinderModel(0.120f, 0.100f, 0.114f, skinMale), 0f, -0.005f, 0.005f);
        put(neckParts[0], ellipsoidModel(0.020f, 0.024f, 0.016f, skinMale), 0f, 0.005f, 0.058f);

        // 4. Male Head & Textured Hair
        put(headParts[0], ellipsoidModel(0.165f, 0.200f, 0.165f, skinMale), 0f, 0.015f, -0.005f);
        put(headParts[0], ellipsoidModel(0.145f, 0.090f, 0.070f, skinMaleHighlight), 0f, 0.040f, 0.048f);
        put(headParts[0], ellipsoidModel(0.138f, 0.028f, 0.040f, skinMale), 0f, 0.026f, 0.068f);
        put(headParts[0], ellipsoidModel(0.042f, 0.036f, 0.044f, skinMaleHighlight), -0.052f, 0f, 0.062f);
        put(headParts[0], ellipsoidModel(0.042f, 0.036f, 0.044f, skinMaleHighlight), 0.052f, 0f, 0.062f);
        put(headParts[0], ellipsoidModel(0.135f, 0.105f, 0.125f, skinMale), 0f, -0.045f, 0.012f);
        put(headParts[0], ellipsoidModel(0.044f, 0.034f, 0.038f, skinMaleHighlight), 0f, -0.086f, 0.056f);

        // Male Ears
        put(headParts[0], ellipsoidModel(0.020f, 0.046f, 0.030f, skinMale), -0.084f, -0.005f, -0.008f, -4f, -12f, 5f);
        put(headParts[0], ellipsoidModel(0.020f, 0.046f, 0.030f, skinMale), 0.084f, -0.005f, -0.008f, -4f, 12f, -5f);

        // Eyes & Brows
        put(headParts[0], ellipsoidModel(0.028f, 0.016f, 0.012f, eyeWhite), -0.036f, 0.012f, 0.082f, 0f, -5f, 0f);
        put(headParts[0], ellipsoidModel(0.016f, 0.016f, 0.008f, iris), -0.036f, 0.012f, 0.086f, 0f, -5f, 0f);
        put(headParts[0], ellipsoidModel(0.008f, 0.008f, 0.005f, pupil), -0.036f, 0.012f, 0.089f, 0f, -5f, 0f);
        put(headParts[0], ellipsoidModel(0.004f, 0.004f, 0.004f, catchlight), -0.034f, 0.014f, 0.092f);
        put(headParts[0], capsuleModel(0.004f, 0.028f, upperLid), -0.036f, 0.019f, 0.087f, 0f, 0f, 85f);

        put(headParts[0], ellipsoidModel(0.028f, 0.016f, 0.012f, eyeWhite), 0.036f, 0.012f, 0.082f, 0f, 5f, 0f);
        put(headParts[0], ellipsoidModel(0.016f, 0.016f, 0.008f, iris), 0.036f, 0.012f, 0.086f, 0f, 5f, 0f);
        put(headParts[0], ellipsoidModel(0.008f, 0.008f, 0.005f, pupil), 0.036f, 0.012f, 0.089f, 0f, 5f, 0f);
        put(headParts[0], ellipsoidModel(0.004f, 0.004f, 0.004f, catchlight), 0.038f, 0.014f, 0.092f);
        put(headParts[0], capsuleModel(0.004f, 0.028f, upperLid), 0.036f, 0.019f, 0.087f, 0f, 0f, -85f);

        put(headParts[0], capsuleModel(0.0038f, 0.026f, browMale), -0.022f, 0.030f, 0.086f, 0f, 0f, 78f);
        put(headParts[0], capsuleModel(0.0030f, 0.024f, browMale), -0.045f, 0.033f, 0.080f, 0f, 0f, 105f);
        put(headParts[0], capsuleModel(0.0038f, 0.026f, browMale), 0.022f, 0.030f, 0.086f, 0f, 0f, -78f);
        put(headParts[0], capsuleModel(0.0030f, 0.024f, browMale), 0.045f, 0.033f, 0.080f, 0f, 0f, -105f);

        // Nose & Lips
        put(headParts[0], capsuleModel(0.0085f, 0.042f, skinMaleHighlight), 0f, -0.006f, 0.088f, 18f, 0f, 0f);
        put(headParts[0], ellipsoidModel(0.016f, 0.013f, 0.015f, skinMale), 0f, -0.024f, 0.098f);
        put(headParts[0], ellipsoidModel(0.032f, 0.009f, 0.010f, lipsMale), 0f, -0.044f, 0.082f);
        put(headParts[0], ellipsoidModel(0.030f, 0.011f, 0.011f, lipsMale), 0f, -0.053f, 0.080f);

        // Male Textured College Hair (matching reference image)
        put(headParts[0], ellipsoidModel(0.182f, 0.142f, 0.182f, hairMale), 0f, 0.055f, -0.015f);
        put(headParts[0], ellipsoidModel(0.165f, 0.130f, 0.140f, hairMale), 0f, -0.020f, -0.040f);
        put(headParts[0], ellipsoidModel(0.052f, 0.085f, 0.078f, hairMale), -0.076f, 0.020f, -0.005f);
        put(headParts[0], ellipsoidModel(0.052f, 0.085f, 0.078f, hairMale), 0.076f, 0.020f, -0.005f);
        put(headParts[0], ellipsoidModel(0.165f, 0.060f, 0.155f, hairMale), 0.010f, 0.100f, 0.005f, -8f, 10f, -6f);
        put(headParts[0], ellipsoidModel(0.130f, 0.052f, 0.130f, hairMaleHighlight), -0.015f, 0.106f, 0.010f, -5f, -8f, 4f);
        put(headParts[0], capsuleModel(0.010f, 0.052f, hairMale), 0.028f, 0.072f, 0.080f, -30f, 16f, -36f);
        put(headParts[0], capsuleModel(0.009f, 0.048f, hairMaleHighlight), 0.048f, 0.066f, 0.075f, -22f, 24f, -42f);
        put(headParts[0], capsuleModel(0.010f, 0.050f, hairMale), -0.018f, 0.074f, 0.078f, -26f, -12f, 28f);
        put(headParts[0], capsuleModel(0.008f, 0.050f, hairMale), -0.076f, 0.002f, 0.022f, 8f, 0f, 0f);
        put(headParts[0], capsuleModel(0.008f, 0.050f, hairMale), 0.076f, 0.002f, 0.022f, 8f, 0f, 0f);

        // 5. Male Arms & Hands (Polo Sleeves + White Stripe)
        Model poloSleeveM = capsuleModel(0.048f, 0.16f, poloNavy);
        putPair(upperArmParts[0], poloSleeveM, 0f, -0.060f, 0f);
        Model sleeveStripeM = cylinderModel(0.098f, 0.012f, 0.098f, stripeWhite);
        putPair(upperArmParts[0], sleeveStripeM, 0f, -0.120f, 0f);
        Model armSkinM = capsuleModel(0.040f, 0.14f, skinMale);
        putPair(upperArmParts[0], armSkinM, 0f, -0.200f, 0f);

        putPair(foreArmParts[0], capsuleModel(0.038f, 0.22f, skinMale), 0f, -0.100f, 0f);
        put(foreArmParts[0][0], boxModel(0.036f, 0.026f, 0.010f, watchFace), 0f, -0.195f, 0.038f);
        put(foreArmParts[0][0], cylinderModel(0.072f, 0.018f, 0.072f, watchBand), 0f, -0.195f, 0f);
        putPair(foreArmParts[0], boxModel(0.058f, 0.065f, 0.028f, skinMale), 0f, -0.250f, 0f);
        putPair(foreArmParts[0], capsuleModel(0.009f, 0.034f, skinMale), 0.028f, -0.250f, 0.009f, 25f, 0f, 35f);

        // 6. Male Legs & Athletic Sneakers (Blue Denim Jeans)
        Model hipSeatM = ellipsoidModel(0.120f, 0.135f, 0.130f, denimBlue);
        putPair(thighParts[0], hipSeatM, 0f, -0.015f, 0f);
        putPair(thighParts[0], capsuleModel(0.056f, 0.32f, denimBlue), 0f, -0.165f, 0f);
        putPair(thighParts[0], capsuleModel(0.050f, 0.15f, denimBlue), 0f, -0.305f, 0f);
        putPair(shinParts[0], cylinderModel(0.098f, 0.052f, 0.104f, denimBlue), 0f, 0f, 0f);
        putPair(shinParts[0], capsuleModel(0.048f, 0.32f, denimBlue), 0f, -0.165f, -0.004f);
        putPair(shinParts[0], cylinderModel(0.095f, 0.032f, 0.100f, denimBlueShade), 0f, -0.335f, 0f);

        // Athletic street sneakers
        Model shoeUpperM = boxModel(0.092f, 0.060f, 0.215f, sneakerDark);
        putPair(footParts[0], shoeUpperM, 0f, -0.015f, 0.038f);
        Model toeCapM = ellipsoidModel(0.090f, 0.050f, 0.085f, sneakerDark);
        putPair(footParts[0], toeCapM, 0f, -0.020f, 0.125f);
        Model midsoleM = boxModel(0.098f, 0.020f, 0.230f, sneakerWhite);
        putPair(footParts[0], midsoleM, 0f, -0.048f, 0.038f);
        Model lacesM = boxModel(0.042f, 0.012f, 0.075f, sneakerWhite);
        putPair(footParts[0], lacesM, 0f, 0.012f, 0.062f);

        // =========================================================================
        // BUILD FEMALE STUDENT (Gender = 1)
        // (Reference: Sky-blue Oxford shirt, dark slim jeans, yellow sandals, long hair)
        // =========================================================================
        // 1. Pelvis & Waist (Origin: y = 0.87)
        put(pelvisParts[1], cylinderModel(0.27f, 0.15f, 0.19f, denimDark), 0f, 0f, 0f);
        put(pelvisParts[1], ellipsoidModel(0.28f, 0.15f, 0.20f, denimDark), 0f, -0.01f, 0f);
        put(pelvisParts[1], cylinderModel(0.276f, 0.030f, 0.192f, beltLeather), 0f, 0.070f, 0f);
        put(pelvisParts[1], boxModel(0.036f, 0.026f, 0.010f, buckleSteel), 0f, 0.070f, 0.100f);

        // 2. Chest & Torso: Sky-Blue Oxford Shirt (Origin: y = 1.13)
        put(chestParts[1], cylinderModel(0.285f, 0.41f, 0.190f, shirtSkyBlue), 0f, 0.02f, -0.005f);
        put(chestParts[1], ellipsoidModel(0.295f, 0.17f, 0.195f, shirtSkyBlue), 0f, 0.13f, 0.005f);
        put(chestParts[1], ellipsoidModel(0.240f, 0.13f, 0.160f, shirtSkyBlue), 0f, 0.19f, -0.008f);

        // Feminine natural shoulders
        Model shirtShoulderF = ellipsoidModel(0.095f, 0.075f, 0.115f, shirtSkyBlue);
        put(chestParts[1], shirtShoulderF, -0.155f, 0.180f, 0.005f, 0f, 0f, -12f);
        put(chestParts[1], shirtShoulderF, 0.155f, 0.180f, 0.005f, 0f, 0f, 12f);

        // Folded shirt collar & button placket
        Model collarLapelF = boxModel(0.034f, 0.11f, 0.014f, shirtSkyBlueTrim);
        put(chestParts[1], collarLapelF, -0.052f, 0.200f, 0.088f, -12f, 18f, 22f);
        put(chestParts[1], collarLapelF, 0.052f, 0.200f, 0.088f, -12f, -18f, -22f);
        put(chestParts[1], boxModel(0.018f, 0.36f, 0.008f, shirtSkyBlueTrim), 0f, 0.03f, 0.098f);
        for (int b = 0; b < 4; b++) {
            float by = 0.14f - b * 0.08f;
            put(chestParts[1], ellipsoidModel(0.007f, 0.007f, 0.005f, pearlButton), 0f, by, 0.104f);
        }

        // Gold pendant necklace draped from collar (like reference photo!)
        Model necklaceChain = capsuleModel(0.004f, 0.18f, necklaceGold);
        put(chestParts[1], necklaceChain, 0f, 0.10f, 0.102f, 0f, 0f, 0f);
        put(chestParts[1], ellipsoidModel(0.014f, 0.018f, 0.008f, necklaceGold), 0f, 0.015f, 0.104f);

        // DU ID Card & Lanyard on Female Chest
        Model ribChestLF = boxModel(0.013f, 0.22f, 0.003f, lanyardBlue);
        Model ribChestRF = boxModel(0.013f, 0.22f, 0.003f, lanyardBlue);
        put(chestParts[1], ribChestLF, -0.028f, 0.07f, 0.106f, 0f, 0f, 12f);
        put(chestParts[1], ribChestRF, 0.028f, 0.07f, 0.106f, 0f, 0f, -12f);
        put(chestParts[1], boxModel(0.012f, 0.018f, 0.006f, idClip), 0f, -0.025f, 0.110f);
        put(chestParts[1], boxModel(0.075f, 0.100f, 0.004f, idCardWhite), 0f, -0.080f, 0.112f);
        put(chestParts[1], boxModel(0.075f, 0.022f, 0.005f, idCardHeader), 0f, -0.038f, 0.113f);
        put(chestParts[1], boxModel(0.028f, 0.034f, 0.005f, idPhoto), -0.016f, -0.070f, 0.114f);

        // 3. Female Neck
        put(neckParts[1], cylinderModel(0.108f, 0.095f, 0.104f, skinFemale), 0f, -0.005f, 0.005f);

        // 4. Female Head & Flowing Hair (Reference: Long auburn/chestnut locks past shoulders)
        put(headParts[1], ellipsoidModel(0.155f, 0.190f, 0.155f, skinFemale), 0f, 0.015f, -0.005f);
        put(headParts[1], ellipsoidModel(0.135f, 0.085f, 0.065f, skinFemaleHighlight), 0f, 0.038f, 0.046f);
        put(headParts[1], ellipsoidModel(0.038f, 0.032f, 0.040f, skinFemaleHighlight), -0.048f, 0f, 0.058f);
        put(headParts[1], ellipsoidModel(0.038f, 0.032f, 0.040f, skinFemaleHighlight), 0.048f, 0f, 0.058f);
        put(headParts[1], ellipsoidModel(0.125f, 0.095f, 0.115f, skinFemale), 0f, -0.042f, 0.012f);
        put(headParts[1], ellipsoidModel(0.038f, 0.030f, 0.034f, skinFemaleHighlight), 0f, -0.082f, 0.052f);

        // Eyes & Brows (Feminine gentle contours)
        put(headParts[1], ellipsoidModel(0.026f, 0.015f, 0.011f, eyeWhite), -0.034f, 0.012f, 0.078f, 0f, -5f, 0f);
        put(headParts[1], ellipsoidModel(0.015f, 0.015f, 0.007f, iris), -0.034f, 0.012f, 0.082f, 0f, -5f, 0f);
        put(headParts[1], ellipsoidModel(0.007f, 0.007f, 0.004f, pupil), -0.034f, 0.012f, 0.085f, 0f, -5f, 0f);
        put(headParts[1], ellipsoidModel(0.004f, 0.004f, 0.004f, catchlight), -0.032f, 0.014f, 0.088f);
        put(headParts[1], capsuleModel(0.0035f, 0.026f, upperLid), -0.034f, 0.018f, 0.083f, 0f, 0f, 85f);

        put(headParts[1], ellipsoidModel(0.026f, 0.015f, 0.011f, eyeWhite), 0.034f, 0.012f, 0.078f, 0f, 5f, 0f);
        put(headParts[1], ellipsoidModel(0.015f, 0.015f, 0.007f, iris), 0.034f, 0.012f, 0.082f, 0f, 5f, 0f);
        put(headParts[1], ellipsoidModel(0.007f, 0.007f, 0.004f, pupil), 0.034f, 0.012f, 0.085f, 0f, 5f, 0f);
        put(headParts[1], ellipsoidModel(0.004f, 0.004f, 0.004f, catchlight), 0.036f, 0.014f, 0.088f);
        put(headParts[1], capsuleModel(0.0035f, 0.026f, upperLid), 0.034f, 0.018f, 0.083f, 0f, 0f, -85f);

        put(headParts[1], capsuleModel(0.0030f, 0.024f, browFemale), -0.022f, 0.028f, 0.082f, 0f, 0f, 78f);
        put(headParts[1], capsuleModel(0.0026f, 0.022f, browFemale), -0.042f, 0.031f, 0.076f, 0f, 0f, 105f);
        put(headParts[1], capsuleModel(0.0030f, 0.024f, browFemale), 0.022f, 0.028f, 0.082f, 0f, 0f, -78f);
        put(headParts[1], capsuleModel(0.0026f, 0.022f, browFemale), 0.042f, 0.031f, 0.076f, 0f, 0f, -105f);

        put(headParts[1], capsuleModel(0.0075f, 0.038f, skinFemaleHighlight), 0f, -0.006f, 0.084f, 18f, 0f, 0f);
        put(headParts[1], ellipsoidModel(0.014f, 0.011f, 0.013f, skinFemale), 0f, -0.022f, 0.092f);
        put(headParts[1], ellipsoidModel(0.028f, 0.009f, 0.009f, lipsFemale), 0f, -0.042f, 0.078f);
        put(headParts[1], ellipsoidModel(0.026f, 0.010f, 0.010f, lipsFemale), 0f, -0.050f, 0.076f);

        // Long Flowing Auburn Hair (Cascading naturally past shoulders)
        put(headParts[1], ellipsoidModel(0.175f, 0.140f, 0.175f, hairFemale), 0f, 0.055f, -0.015f);
        put(headParts[1], ellipsoidModel(0.160f, 0.180f, 0.130f, hairFemale), 0f, -0.060f, -0.045f); // Back mane
        put(headParts[1], capsuleModel(0.026f, 0.28f, hairFemale), -0.082f, -0.06f, 0.010f, 8f, 0f, 5f);  // Left cascade
        put(headParts[1], capsuleModel(0.026f, 0.28f, hairFemale), 0.082f, -0.06f, 0.010f, 8f, 0f, -5f);  // Right cascade
        put(headParts[1], capsuleModel(0.022f, 0.24f, hairFemaleHighlight), -0.075f, -0.08f, 0.030f, 10f, 0f, 8f);
        put(headParts[1], capsuleModel(0.022f, 0.24f, hairFemaleHighlight), 0.075f, -0.08f, 0.030f, 10f, 0f, -8f);
        put(headParts[1], capsuleModel(0.012f, 0.065f, hairFemale), 0.024f, 0.068f, 0.078f, -28f, 14f, -32f);
        put(headParts[1], capsuleModel(0.012f, 0.065f, hairFemale), -0.024f, 0.068f, 0.078f, -28f, -14f, 32f);

        // 5. Female Arms & Rolled Sleeves
        Model sleeveF = capsuleModel(0.042f, 0.15f, shirtSkyBlue);
        putPair(upperArmParts[1], sleeveF, 0f, -0.055f, 0f);
        Model cuffF = cylinderModel(0.086f, 0.022f, 0.086f, shirtSkyBlueTrim);
        putPair(upperArmParts[1], cuffF, 0f, -0.130f, 0f);
        putPair(upperArmParts[1], capsuleModel(0.036f, 0.14f, skinFemale), 0f, -0.195f, 0f);

        putPair(foreArmParts[1], capsuleModel(0.034f, 0.21f, skinFemale), 0f, -0.095f, 0f);
        put(foreArmParts[1][0], cylinderModel(0.062f, 0.010f, 0.062f, necklaceGold), 0f, -0.190f, 0f); // Gold bracelet
        putPair(foreArmParts[1], boxModel(0.052f, 0.058f, 0.024f, skinFemale), 0f, -0.240f, 0f);
        putPair(foreArmParts[1], capsuleModel(0.008f, 0.030f, skinFemale), 0.024f, -0.240f, 0.008f, 25f, 0f, 35f);

        // 6. Female Legs & Yellow Summer Flat Sandals (Dark Indigo Slim Jeans)
        Model hipSeatF = ellipsoidModel(0.115f, 0.130f, 0.125f, denimDark);
        putPair(thighParts[1], hipSeatF, 0f, -0.015f, 0f);
        putPair(thighParts[1], capsuleModel(0.050f, 0.32f, denimDark), 0f, -0.165f, 0f);
        putPair(thighParts[1], capsuleModel(0.045f, 0.15f, denimDark), 0f, -0.305f, 0f);
        putPair(shinParts[1], cylinderModel(0.088f, 0.050f, 0.092f, denimDark), 0f, 0f, 0f);
        putPair(shinParts[1], capsuleModel(0.042f, 0.32f, denimDark), 0f, -0.165f, -0.004f);
        putPair(shinParts[1], cylinderModel(0.085f, 0.030f, 0.088f, denimDarkShade), 0f, -0.335f, 0f);

        // Summer yellow flat sandals (matching reference photo!)
        Model footSkinF = boxModel(0.072f, 0.024f, 0.190f, skinFemale);
        putPair(footParts[1], footSkinF, 0f, -0.010f, 0.035f);
        Model sandalSoleM = boxModel(0.080f, 0.012f, 0.210f, sandalSole);
        putPair(footParts[1], sandalSoleM, 0f, -0.024f, 0.038f);
        Model sandalStrapFront = boxModel(0.078f, 0.014f, 0.036f, sandalYellow);
        putPair(footParts[1], sandalStrapFront, 0f, -0.002f, 0.095f);
        Model sandalStrapAnkle = cylinderModel(0.078f, 0.010f, 0.078f, sandalYellow);
        putPair(footParts[1], sandalStrapAnkle, 0f, 0.015f, 0.005f);
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

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               false, false, 0, 0f, false, 0f, 0);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               isCrouching, isAttacking, attackCombo, attackProgress,
               isSittingWater, sitProgress, 0);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender) {

        int g = (gender == 1) ? 1 : 0;
        float delta = Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : 0.016f;
        idleTime += delta;

        float bobOffset = 0f;
        float swing = 0f;
        float pelvisHeight = 0.87f;
        float torsoPitch = 0f;
        float headPitch = 0f;
        float torsoTwist = 0f;

        if (isMoving && !isSittingWater) {
            float cycleSpeed = isSprinting ? 2.3f : 1.4f;
            float phase = walkCycle * cycleSpeed;
            float bobAmp = isSprinting ? 0.038f : 0.018f;
            bobOffset = Math.abs(MathUtils.sin(phase)) * bobAmp;
            swing = MathUtils.sin(phase);
            torsoPitch = isSprinting ? 12f : 3f;
        }

        float breathe = (!isMoving && !isAttacking) ? MathUtils.sin(idleTime * 2.2f) * 0.006f : 0f;

        if (isCrouching) {
            pelvisHeight = 0.46f;
            torsoPitch = 32f;
            headPitch = -20f;
        }

        if (isSittingWater) {
            pelvisHeight = 0.38f;
            torsoPitch = -8f;
        }

        float attackArmR = 0f, attackForeArmR = 0f;
        float attackArmL = 0f, attackForeArmL = 0f;
        float attackLegR = 0f, attackKneeR = 0f;

        if (isAttacking) {
            float t = MathUtils.clamp(attackProgress, 0f, 1f);
            float strikePower = MathUtils.sin(t * MathUtils.PI);

            if (attackCombo == 1) {
                torsoTwist = -28f * strikePower;
                attackArmR = -85f * strikePower;
                attackForeArmR = -12f * strikePower;
                attackArmL = -45f * strikePower;
                attackForeArmL = -85f * strikePower;
            } else if (attackCombo == 2) {
                torsoTwist = 36f * strikePower;
                attackArmL = -75f * strikePower;
                attackForeArmL = -80f * strikePower;
                attackArmR = -50f * strikePower;
                attackForeArmR = -75f * strikePower;
            } else if (attackCombo == 3) {
                torsoPitch = -15f * strikePower;
                torsoTwist = -45f * strikePower;
                attackLegR = -90f * strikePower;
                attackKneeR = 25f * strikePower;
            }
        }

        rootTransform.idt();
        rootTransform.translate(pos.x, pos.y + bobOffset, pos.z);
        rootTransform.rotate(Vector3.Y, headingDegrees);

        float twist = isAttacking ? torsoTwist : (swing * 0.12f);

        // Pelvis
        segment.set(rootTransform).translate(0f, pelvisHeight, 0f).rotate(Vector3.Y, twist);
        drawParts(batch, env, pelvisParts[g], segment);

        // Chest & Torso
        float chestY = isCrouching ? 0.72f : (isSittingWater ? 0.65f : 1.13f);
        float chestZ = isCrouching ? 0.12f : 0f;
        segment.set(rootTransform).translate(0f, chestY + breathe, chestZ)
            .rotate(Vector3.Y, -twist)
            .rotate(Vector3.X, torsoPitch);
        drawParts(batch, env, chestParts[g], segment);

        // Neck
        float neckY = isCrouching ? 0.94f : (isSittingWater ? 0.88f : 1.42f);
        float neckZ = isCrouching ? 0.22f : 0f;
        segment.set(rootTransform).translate(0f, neckY + breathe, neckZ)
            .rotate(Vector3.Y, -twist * 0.5f)
            .rotate(Vector3.X, torsoPitch * 0.7f);
        drawParts(batch, env, neckParts[g], segment);

        // Head
        float sprintHeadLean = isSprinting && isMoving ? 8f : 0f;
        float headY = isCrouching ? 1.05f : (isSittingWater ? 0.99f : 1.55f);
        float headZ = isCrouching ? 0.28f : 0f;
        segment.set(rootTransform).translate(0f, headY + breathe - bobOffset * 0.3f, headZ)
            .rotate(Vector3.X, headPitch + sprintHeadLean)
            .rotate(Vector3.Y, -twist * 0.3f);
        drawParts(batch, env, headParts[g], segment);

        // Limbs (Arms & Legs)
        for (int side = 0; side < 2; side++) {
            float sx = side == 0 ? -1f : 1f;

            if (isSittingWater) {
                float sitThighAngle = -85f;
                segment.set(rootTransform).translate(sx * 0.105f, pelvisHeight - 0.04f, 0.08f)
                    .rotate(Vector3.X, sitThighAngle);
                drawParts(batch, env, thighParts[g][side], segment);

                float shinDangleAngle = 82f;
                float kickPhase = idleTime * 2.8f + (side == 0 ? 0f : MathUtils.PI);
                float footKick = MathUtils.sin(kickPhase) * 12f;
                segment.translate(0f, -0.38f, 0f).rotate(Vector3.X, shinDangleAngle + footKick);
                drawParts(batch, env, shinParts[g][side], segment);

                tmp.set(segment).translate(0f, -0.37f, 0f).rotate(Vector3.X, -10f);
                drawParts(batch, env, footParts[g][side], tmp);

                segment.set(rootTransform).translate(sx * 0.185f, chestY + 0.10f, -0.06f)
                    .rotate(Vector3.X, 15f)
                    .rotate(Vector3.Z, sx * -8f);
                drawParts(batch, env, upperArmParts[g][side], segment);

                segment.translate(0f, -0.27f, 0f).rotate(Vector3.X, 35f);
                drawParts(batch, env, foreArmParts[g][side], segment);
            } else {
                float legAngle = isMoving ? swing * (side == 0 ? 32f : -32f) : 0f;
                float kneeAngle = 0f;

                if (isMoving) {
                    float cycle = side == 0 ? swing : -swing;
                    if (cycle < 0f) {
                        kneeAngle = -cycle * (isSprinting ? 58f : 38f);
                    }
                }

                if (isCrouching) {
                    legAngle = -48f;
                    kneeAngle = 78f;
                }

                if (isAttacking && side == 1 && attackLegR != 0f) {
                    legAngle += attackLegR;
                }

                segment.set(rootTransform).translate(sx * 0.105f, pelvisHeight - 0.04f, 0f)
                    .rotate(Vector3.X, legAngle);
                drawParts(batch, env, thighParts[g][side], segment);

                segment.translate(0f, -0.38f, 0f);
                if (isAttacking && side == 1 && attackKneeR != 0f) {
                    kneeAngle += attackKneeR;
                }
                segment.rotate(Vector3.X, kneeAngle);
                drawParts(batch, env, shinParts[g][side], segment);

                tmp.set(segment).translate(0f, -0.37f, 0f);
                drawParts(batch, env, footParts[g][side], tmp);

                // Upper Arm
                float armSwing = isMoving ? -swing * (side == 0 ? 28f : -28f) * (isSprinting ? 1.6f : 1f) : 0f;
                float armBaseAngle = 4f;

                if (isAttacking) {
                    if (side == 0 && attackArmL != 0f) armSwing = attackArmL;
                    if (side == 1 && attackArmR != 0f) armSwing = attackArmR;
                }

                segment.set(rootTransform).translate(sx * 0.185f, chestY + 0.18f + breathe, chestZ)
                    .rotate(Vector3.X, armBaseAngle + armSwing)
                    .rotate(Vector3.Z, sx * -5f);
                drawParts(batch, env, upperArmParts[g][side], segment);

                // Forearm
                float elbowBend = isMoving ? (isSprinting ? 52f : 24f) : 10f;
                if (isAttacking) {
                    if (side == 0 && attackForeArmL != 0f) elbowBend = Math.abs(attackForeArmL);
                    if (side == 1 && attackForeArmR != 0f) elbowBend = Math.abs(attackForeArmR);
                }

                segment.translate(0f, -0.27f, 0f).rotate(Vector3.X, -elbowBend);
                drawParts(batch, env, foreArmParts[g][side], segment);
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
'''

with open('core/src/main/java/bd/spark36/character/StudentMesh.java', 'w') as f:
    f.write(mesh_code)

print("StudentMesh.java generated with dual-gender support and clean reference attire!")
