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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IdentityMap;
import bd.spark36.world.TextureFactory;

/**
 * Procedural 3D university student (Level 1: an ordinary Dhaka University student at the
 * very start of the movement - no protest gear).
 * <ul>
 *   <li>Male (gender 0): neat short black hair, light button-up shirt with collar and sleeves
 *       rolled to the elbow, jeans, sneakers, backpack with two shoulder straps, and a DU ID card
 *       on a lanyard.</li>
 *   <li>Female (gender 1): shoulder-length black hair, kurti with an orna draped over the
 *       shoulders, light trousers, sandals, backpack and DU ID card on a lanyard.</li>
 * </ul>
 * Colours of skin / hair / shirt / trousers / shoes / bag / accent (orna) can be varied per
 * instance for NPC students via {@link #render(ModelBatch, Environment, Vector3, float, float,
 * boolean, boolean, boolean, boolean, int, float, boolean, float, int, int)} (preset variant
 * index) or the {@link Appearance} overload. Model geometry is shared; each variant only adds
 * ModelInstances with recoloured material copies.
 *
 * Facing: local +Z is forward. Animations: idle breathing, walk / sprint, crouch, 3-hit combo,
 * sitting on the ghat steps. Works with a depth-only ModelBatch and a null Environment.
 */
public class StudentMesh implements Disposable {

    // Material colour slots that an Appearance can override
    private static final int SLOT_FIXED = -1;
    private static final int SLOT_SKIN = 0;
    private static final int SLOT_HAIR = 1;
    private static final int SLOT_SHIRT = 2;
    private static final int SLOT_TROUSERS = 3;
    private static final int SLOT_SHOES = 4;
    private static final int SLOT_BAG = 5;
    private static final int SLOT_ACCENT = 6;

    // Optional accessory groups (can be left off per variant)
    private static final int GROUP_BODY = 0;
    private static final int GROUP_BAG = 1;
    private static final int GROUP_ID = 2;

    /** Number of pre-built colour variants (0 = protagonist, 1..VARIANT_COUNT-1 = NPC students). */
    public static final int VARIANT_COUNT = 8;
    private static final boolean[] VARIANT_BAG = {true, true, false, true, false, true, true, false};
    private static final boolean[] VARIANT_ID = {true, false, true, true, false, true, false, true};

    /** Per-instance colour set. Treat as immutable once passed to render(). */
    public static final class Appearance {
        public final Color skin = new Color();
        public final Color hair = new Color();
        public final Color shirt = new Color();
        public final Color trousers = new Color();
        public final Color shoes = new Color();
        public final Color bag = new Color();
        /** Female: orna colour. Male: unused. */
        public final Color accent = new Color();
        /** Whether this student carries the backpack / wears the ID card lanyard. */
        public boolean hasBag = true;
        public boolean hasIdCard = true;

        public Appearance() {
        }

        public Appearance(Color skin, Color hair, Color shirt, Color trousers,
                          Color shoes, Color bag, Color accent) {
            this.skin.set(skin);
            this.hair.set(hair);
            this.shirt.set(shirt);
            this.trousers.set(trousers);
            this.shoes.set(shoes);
            this.bag.set(bag);
            this.accent.set(accent);
        }

        Color slot(int s) {
            switch (s) {
                case SLOT_SKIN: return skin;
                case SLOT_HAIR: return hair;
                case SLOT_SHIRT: return shirt;
                case SLOT_TROUSERS: return trousers;
                case SLOT_SHOES: return shoes;
                case SLOT_BAG: return bag;
                default: return accent;
            }
        }

        private Appearance c(Color t, float[] v) {
            t.set(v[0], v[1], v[2], 1f);
            return this;
        }

        /** The protagonist's look for the given gender (0 = male, 1 = female). */
        public static Appearance defaultLook(int gender) {
            Appearance a = new Appearance();
            if (gender == 1) {
                a.skin.set(0.83f, 0.66f, 0.54f, 1f);
                a.hair.set(0.055f, 0.045f, 0.040f, 1f);
                a.shirt.set(0.16f, 0.45f, 0.48f, 1f);     // teal kurti
                a.trousers.set(0.90f, 0.87f, 0.80f, 1f);  // cream salwar
                a.shoes.set(0.55f, 0.35f, 0.22f, 1f);     // tan sandal straps
                a.bag.set(0.45f, 0.20f, 0.25f, 1f);       // maroon backpack
                a.accent.set(0.95f, 0.91f, 0.82f, 1f);    // off-white orna
            } else {
                a.skin.set(0.80f, 0.62f, 0.49f, 1f);
                a.hair.set(0.050f, 0.045f, 0.040f, 1f);
                a.shirt.set(0.72f, 0.81f, 0.91f, 1f);     // light sky-blue shirt
                a.trousers.set(0.18f, 0.25f, 0.40f, 1f);  // blue jeans
                a.shoes.set(0.16f, 0.16f, 0.18f, 1f);     // dark sneakers
                a.bag.set(0.16f, 0.20f, 0.28f, 1f);       // navy/charcoal backpack
                a.accent.set(0.92f, 0.92f, 0.90f, 1f);
            }
            return a;
        }

        private static final float[][] SKINS = {
            {0.80f, 0.62f, 0.49f}, {0.72f, 0.54f, 0.41f}, {0.86f, 0.69f, 0.56f},
            {0.64f, 0.47f, 0.35f}, {0.76f, 0.58f, 0.45f}, {0.68f, 0.50f, 0.38f}};
        private static final float[][] HAIRS = {
            {0.050f, 0.045f, 0.040f}, {0.08f, 0.06f, 0.05f}, {0.13f, 0.09f, 0.07f}};
        private static final float[][] MALE_SHIRTS = {
            {0.93f, 0.93f, 0.91f}, {0.72f, 0.81f, 0.91f}, {0.55f, 0.18f, 0.20f},
            {0.40f, 0.46f, 0.32f}, {0.55f, 0.57f, 0.60f}, {0.20f, 0.26f, 0.42f},
            {0.86f, 0.78f, 0.60f}, {0.30f, 0.52f, 0.40f}, {0.80f, 0.62f, 0.62f}};
        private static final float[][] FEMALE_KURTIS = {
            {0.72f, 0.22f, 0.35f}, {0.16f, 0.45f, 0.48f}, {0.90f, 0.72f, 0.30f},
            {0.40f, 0.30f, 0.60f}, {0.85f, 0.55f, 0.58f}, {0.25f, 0.35f, 0.60f},
            {0.95f, 0.95f, 0.93f}, {0.35f, 0.50f, 0.30f}};
        private static final float[][] MALE_TROUSERS = {
            {0.18f, 0.25f, 0.40f}, {0.12f, 0.12f, 0.14f}, {0.55f, 0.48f, 0.36f},
            {0.30f, 0.32f, 0.36f}, {0.28f, 0.36f, 0.52f}};
        private static final float[][] FEMALE_TROUSERS = {
            {0.90f, 0.87f, 0.80f}, {0.95f, 0.95f, 0.94f}, {0.12f, 0.12f, 0.14f},
            {0.18f, 0.25f, 0.40f}, {0.60f, 0.45f, 0.50f}};
        private static final float[][] SHOES = {
            {0.16f, 0.16f, 0.18f}, {0.35f, 0.25f, 0.18f}, {0.82f, 0.82f, 0.82f}, {0.20f, 0.25f, 0.40f}};
        private static final float[][] BAGS = {
            {0.16f, 0.20f, 0.28f}, {0.12f, 0.12f, 0.13f}, {0.45f, 0.20f, 0.25f},
            {0.25f, 0.35f, 0.25f}, {0.50f, 0.40f, 0.28f}, {0.20f, 0.40f, 0.60f}, {0.40f, 0.40f, 0.42f}};
        private static final float[][] ORNAS = {
            {0.95f, 0.91f, 0.82f}, {0.85f, 0.40f, 0.45f}, {0.95f, 0.80f, 0.40f},
            {0.35f, 0.55f, 0.60f}, {0.93f, 0.93f, 0.93f}, {0.55f, 0.30f, 0.55f}};

        /**
         * Deterministic NPC look. Variant 0 (or negative) returns {@link #defaultLook(int)};
         * any other index gives a stable pseudo-random mix of plausible student colours.
         */
        public static Appearance preset(int variant, int gender) {
            if (variant <= 0) return defaultLook(gender);
            int s = variant * 7919 + 104729;
            Appearance a = new Appearance();
            s = next(s); a.c(a.skin, SKINS[idx(s, SKINS.length)]);
            s = next(s); a.c(a.hair, HAIRS[idx(s, HAIRS.length)]);
            s = next(s); a.c(a.shirt, gender == 1 ? FEMALE_KURTIS[idx(s, FEMALE_KURTIS.length)]
                                                   : MALE_SHIRTS[idx(s, MALE_SHIRTS.length)]);
            s = next(s); a.c(a.trousers, gender == 1 ? FEMALE_TROUSERS[idx(s, FEMALE_TROUSERS.length)]
                                                      : MALE_TROUSERS[idx(s, MALE_TROUSERS.length)]);
            s = next(s); a.c(a.shoes, SHOES[idx(s, SHOES.length)]);
            s = next(s); a.c(a.bag, BAGS[idx(s, BAGS.length)]);
            s = next(s); a.c(a.accent, ORNAS[idx(s, ORNAS.length)]);
            int k = variant % VARIANT_COUNT;
            a.hasBag = VARIANT_BAG[k];
            a.hasIdCard = VARIANT_ID[k];
            return a;
        }

        private static int next(int s) {
            return s * 1103515245 + 12345;
        }

        private static int idx(int s, int n) {
            return ((s >>> 16) & 0x7fff) % n;
        }
    }

    public static final class Part {
        final Model model;
        final ModelInstance inst;
        final Matrix4 local;
        final int slot;
        final float tr, tg, tb;
        final int group;

        Part(Model model, ModelInstance inst, Matrix4 local, int slot, float tr, float tg, float tb, int group) {
            this.group = group;
            this.model = model;
            this.inst = inst;
            this.local = local;
            this.slot = slot;
            this.tr = tr;
            this.tg = tg;
            this.tb = tb;
        }
    }

    /** One complete body (one gender, one colour set). */
    private static final class Rig {
        final Array<Part> pelvis = new Array<>();
        final Array<Part> chest = new Array<>();
        final Array<Part> neck = new Array<>();
        final Array<Part> head = new Array<>();
        final Array<Part>[] upperArm = pair();
        final Array<Part>[] foreArm = pair();
        final Array<Part>[] thigh = pair();
        final Array<Part>[] shin = pair();
        final Array<Part>[] foot = pair();

        @SuppressWarnings("unchecked")
        private static Array<Part>[] pair() {
            return new Array[]{new Array<Part>(), new Array<Part>()};
        }
    }

    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Array<Model> models = new Array<>();
    private final IdentityMap<Material, float[]> slotInfo = new IdentityMap<>();
    private final long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
    private Appearance cur; // look used while building materials
    private int curGroup = GROUP_BODY;

    private final Rig[] baseRigs = new Rig[]{new Rig(), new Rig()};
    private final Rig[][] variantRigs = new Rig[VARIANT_COUNT][];
    private final IdentityMap<Appearance, Rig[]> lookRigs = new IdentityMap<>();

    private final Matrix4 rootTransform = new Matrix4();
    private final Matrix4 segment = new Matrix4();
    private final Matrix4 tmp = new Matrix4();
    private final Vector3 tv = new Vector3();
    private final Quaternion tq = new Quaternion();

    private float idleTime = 0f;
    private long lastFrameId = -1L;

    public StudentMesh(TextureFactory textures) {
        cur = Appearance.defaultLook(0);
        buildMale(baseRigs[0]);
        cur = Appearance.defaultLook(1);
        buildFemale(baseRigs[1]);
        // Pre-build every NPC variant once: own ModelInstances + own Material copies, so any
        // number of students can be queued in one ModelBatch without sharing colours.
        variantRigs[0] = baseRigs;
        for (int v = 1; v < VARIANT_COUNT; v++) {
            variantRigs[v] = new Rig[]{
                recolor(baseRigs[0], Appearance.preset(v, 0)),
                recolor(baseRigs[1], Appearance.preset(v, 1))};
        }
    }

    /** Number of pre-built variants accepted by the variant render overload. */
    public static int getVariantCount() {
        return VARIANT_COUNT;
    }

    // =====================================================================
    // Primitive helpers
    // =====================================================================

    private static int divFor(float maxDim) {
        return maxDim > 0.12f ? 24 : (maxDim > 0.05f ? 16 : 10);
    }

    private Model ell(float w, float h, float d, Material mat) {
        int div = divFor(Math.max(w, Math.max(h, d)));
        Model m = modelBuilder.createSphere(w, h, d, div, Math.max(8, div * 3 / 4), mat, attr);
        models.add(m);
        return m;
    }

    private Model cap(float radius, float length, Material mat) {
        Model m = modelBuilder.createCapsule(radius, Math.max(length, radius * 2.001f),
            radius > 0.03f ? 20 : 12, mat, attr);
        models.add(m);
        return m;
    }

    private Model cyl(float w, float h, float d, Material mat) {
        Model m = modelBuilder.createCylinder(w, h, d, divFor(Math.max(w, d)) + 4, mat, attr);
        models.add(m);
        return m;
    }

    private Model box(float w, float h, float d, Material mat) {
        Model m = modelBuilder.createBox(w, h, d, mat, attr);
        models.add(m);
        return m;
    }

    /** Material bound to an Appearance slot, tinted by (tr,tg,tb) relative to the slot colour. */
    private Material m(int slot, float tr, float tg, float tb) {
        Color c = cur.slot(slot);
        Material mat = new Material(ColorAttribute.createDiffuse(new Color(
            MathUtils.clamp(c.r * tr, 0f, 1f), MathUtils.clamp(c.g * tg, 0f, 1f),
            MathUtils.clamp(c.b * tb, 0f, 1f), 1f)));
        slotInfo.put(mat, new float[]{slot, tr, tg, tb});
        return mat;
    }

    private Material m(int slot, float shade) {
        return m(slot, shade, shade, shade);
    }

    private Material m(int slot) {
        return m(slot, 1f, 1f, 1f);
    }

    private static Material flat(float r, float g, float b) {
        return new Material(ColorAttribute.createDiffuse(new Color(r, g, b, 1f)));
    }

    private Part part(Model model, Matrix4 local) {
        float[] info = model.materials.size > 0 ? slotInfo.get(model.materials.first()) : null;
        if (info == null) {
            return new Part(model, new ModelInstance(model), local, SLOT_FIXED, 1f, 1f, 1f, curGroup);
        }
        return new Part(model, new ModelInstance(model), local, (int) info[0], info[1], info[2], info[3], curGroup);
    }

    private void put(Array<Part> list, Model m, float x, float y, float z) {
        list.add(part(m, new Matrix4().translate(x, y, z)));
    }

    private void put(Array<Part> list, Model m, float x, float y, float z, float rx, float ry, float rz) {
        Matrix4 l = new Matrix4().translate(x, y, z);
        if (rx != 0f) l.rotate(Vector3.X, rx);
        if (ry != 0f) l.rotate(Vector3.Y, ry);
        if (rz != 0f) l.rotate(Vector3.Z, rz);
        list.add(part(m, l));
    }

    /** Same model on both limbs; x is mirrored (list 0 = -x side). */
    private void putPair(Array<Part>[] lists, Model m, float x, float y, float z) {
        put(lists[0], m, -x, y, z);
        put(lists[1], m, x, y, z);
    }

    private void putPair(Array<Part>[] lists, Model m, float x, float y, float z, float rx, float ry, float rz) {
        put(lists[0], m, -x, y, z, rx, -ry, -rz);
        put(lists[1], m, x, y, z, rx, ry, rz);
    }

    /** Flat strap (w wide, t thick) from a to b. */
    private void strap(Array<Part> list, float ax, float ay, float az, float bx, float by, float bz,
                       float w, float t, Material mat) {
        tv.set(bx - ax, by - ay, bz - az);
        float len = tv.len();
        if (len < 1e-4f) return;
        tv.scl(1f / len);
        Model mdl = box(w, len + t, t, mat); // slight overlap hides the joints
        Matrix4 l = new Matrix4().translate((ax + bx) * 0.5f, (ay + by) * 0.5f, (az + bz) * 0.5f)
            .rotate(tq.setFromCross(Vector3.Y, tv));
        list.add(part(mdl, l));
    }

    /** Strap along a polyline of xyz triples; if mirror, also adds the -x copy. */
    private void strapPath(Array<Part> list, float[] p, float w, float t, Material mat, boolean mirror) {
        for (int s = 0; s < (mirror ? 2 : 1); s++) {
            float sx = s == 0 ? 1f : -1f;
            for (int i = 0; i + 5 < p.length; i += 3) {
                strap(list, sx * p[i], p[i + 1], p[i + 2], sx * p[i + 3], p[i + 4], p[i + 5], w, t, mat);
            }
        }
    }

    /** Rectangle with rounded corners in the XY plane, depth d along Z. */
    private void roundedBlock(Array<Part> list, float cx, float cy, float cz,
                              float w, float h, float d, float r, Material mat) {
        put(list, box(w - 2f * r, h, d, mat), cx, cy, cz);
        put(list, box(w, h - 2f * r, d, mat), cx, cy, cz);
        Model corner = cyl(2f * r, d, 2f * r, mat);
        float ox = w * 0.5f - r, oy = h * 0.5f - r;
        put(list, corner, cx - ox, cy - oy, cz, 90f, 0f, 0f);
        put(list, corner, cx + ox, cy - oy, cz, 90f, 0f, 0f);
        put(list, corner, cx - ox, cy + oy, cz, 90f, 0f, 0f);
        put(list, corner, cx + ox, cy + oy, cz, 90f, 0f, 0f);
    }

    // =====================================================================
    // Shared accessories
    // =====================================================================

    /**
     * DU ID card on a lanyard. path = one side of the lanyard (xyz triples, +x side, from the back
     * of the neck down to the clip); card hangs below (0, cardTop) at depth cardZ.
     */
    private void addIdCard(Array<Part> chest, float[] path, float cardTop, float cardZ) {
        Material lanyard = flat(0.10f, 0.15f, 0.42f);
        Material lanyardRed = flat(0.78f, 0.12f, 0.14f);
        Material metal = flat(0.78f, 0.79f, 0.82f);
        Material cardWhite = flat(0.95f, 0.97f, 1.00f);
        Material cardBlue = flat(0.12f, 0.30f, 0.66f);
        Material photo = flat(0.55f, 0.52f, 0.50f);
        Material photoFace = flat(0.74f, 0.58f, 0.46f);
        Material text = flat(0.45f, 0.47f, 0.52f);
        curGroup = GROUP_ID;

        strapPath(chest, path, 0.013f, 0.004f, lanyard, true);
        float ex = path[path.length - 3], ey = path[path.length - 2], ez = path[path.length - 1];
        // red tab joining both lanyard ends, then the metal clip
        put(chest, box(ex * 2f + 0.016f, 0.012f, 0.006f, lanyardRed), 0f, ey - 0.002f, ez);
        put(chest, box(0.010f, 0.014f, 0.006f, metal), 0f, cardTop + 0.006f, cardZ + 0.001f);

        float cw = 0.062f, ch = 0.086f, f = cardZ + 0.0025f;
        float cy = cardTop - ch * 0.5f;
        put(chest, box(cw, ch, 0.003f, cardWhite), 0f, cy, cardZ, -3f, 0f, 0f);
        put(chest, box(cw, 0.018f, 0.002f, cardBlue), 0f, cardTop - 0.011f, f, -3f, 0f, 0f);
        put(chest, box(cw, 0.004f, 0.002f, lanyardRed), 0f, cardTop - 0.022f, f, -3f, 0f, 0f);
        put(chest, box(0.019f, 0.024f, 0.002f, photo), -0.016f, cy - 0.006f, f, -3f, 0f, 0f);
        put(chest, box(0.010f, 0.012f, 0.002f, photoFace), -0.016f, cy - 0.003f, f + 0.0006f, -3f, 0f, 0f);
        Model line = box(0.024f, 0.0035f, 0.002f, text);
        for (int i = 0; i < 3; i++) {
            put(chest, line, 0.012f, cy + 0.002f - i * 0.008f, f, -3f, 0f, 0f);
        }
        put(chest, box(cw - 0.012f, 0.006f, 0.002f, cardBlue), 0f, cardTop - ch + 0.008f, f, -3f, 0f, 0f);
        curGroup = GROUP_BODY;
    }

    /**
     * Backpack worn on both shoulders. Body is a rounded block behind the torso with a padded back,
     * front pocket, zip and grab handle. strap = one shoulder strap path (+x side, xyz triples) from
     * the top of the bag, over the shoulder, down the chest and back round to the bag's side.
     */
    private void addBackpack(Array<Part> chest, float w, float h, float d, float cy, float cz, float[] strap) {
        Material bag = m(SLOT_BAG);
        Material bagPanel = m(SLOT_BAG, 0.84f);
        Material bagStrap = m(SLOT_BAG, 0.62f);
        Material zip = flat(0.08f, 0.08f, 0.09f);
        Material metal = flat(0.72f, 0.73f, 0.76f);
        curGroup = GROUP_BAG;

        float r = 0.05f;
        roundedBlock(chest, 0f, cy, cz, w, h, d, r, bag);
        float back = cz - d * 0.5f;
        // soft bulge on the outer face so it doesn't read as a crate
        put(chest, ell(w * 0.92f, h * 0.90f, 0.05f, bag), 0f, cy + 0.005f, back);
        // front pocket
        float ph = h * 0.42f, pw = w * 0.74f, pd = 0.045f;
        float py = cy - h * 0.5f + ph * 0.5f + 0.035f;
        float pz = back - 0.012f - pd * 0.5f;
        roundedBlock(chest, 0f, py, pz, pw, ph, pd, 0.035f, bagPanel);
        put(chest, box(pw - 0.03f, 0.006f, 0.006f, zip), 0f, py + ph * 0.5f - 0.014f, pz - pd * 0.5f - 0.002f);
        put(chest, box(0.010f, 0.022f, 0.006f, metal), pw * 0.30f, py + ph * 0.5f - 0.026f, pz - pd * 0.5f - 0.004f);
        // main zip along the top
        put(chest, box(w - 0.06f, 0.006f, 0.008f, zip), 0f, cy + h * 0.5f + 0.001f, back + 0.02f);
        // grab handle
        put(chest, cap(0.010f, 0.08f, bagStrap), 0f, cy + h * 0.5f + 0.012f, cz + 0.02f, 0f, 0f, 90f);
        // shoulder straps + buckles
        strapPath(chest, strap, 0.038f, 0.012f, bagStrap, true);
        Model buckle = box(0.044f, 0.020f, 0.016f, zip);
        int bi = 9; // 4th point = chest-strap buckle position
        put(chest, buckle, strap[bi], strap[bi + 1], strap[bi + 2] + 0.004f);
        put(chest, buckle, -strap[bi], strap[bi + 1], strap[bi + 2] + 0.004f);
        curGroup = GROUP_BODY;
    }

    // =====================================================================
    // MALE (gender 0)
    // =====================================================================

    private void buildMale(Rig r) {
        Material skin = m(SLOT_SKIN);
        Material skinShade = m(SLOT_SKIN, 0.92f);
        Material lips = m(SLOT_SKIN, 0.90f, 0.70f, 0.68f);
        Material hair = m(SLOT_HAIR);
        Material hairSheen = m(SLOT_HAIR, 1.7f);
        Material brow = m(SLOT_HAIR, 1.1f);
        Material shirt = m(SLOT_SHIRT);
        Material shirtShade = m(SLOT_SHIRT, 0.90f);
        Material shirtSeam = m(SLOT_SHIRT, 0.80f);
        Material jeans = m(SLOT_TROUSERS);
        Material jeansShade = m(SLOT_TROUSERS, 0.82f);
        Material shoe = m(SLOT_SHOES);
        Material shoeShade = m(SLOT_SHOES, 0.80f);

        Material eyeWhite = flat(0.94f, 0.93f, 0.92f);
        Material iris = flat(0.16f, 0.10f, 0.07f);
        Material lash = flat(0.06f, 0.05f, 0.05f);
        Material button = flat(0.92f, 0.92f, 0.90f);
        Material sole = flat(0.93f, 0.93f, 0.92f);
        Material watchBand = flat(0.12f, 0.12f, 0.13f);
        Material watchFace = flat(0.70f, 0.72f, 0.75f);

        // ---- Pelvis (frame y = 0.87): jeans seat; shirt hem hangs over the waist ----
        put(r.pelvis, cyl(0.29f, 0.12f, 0.20f, jeans), 0f, 0.02f, 0f);
        put(r.pelvis, ell(0.30f, 0.19f, 0.21f, jeans), 0f, -0.015f, 0f);
        put(r.pelvis, box(0.012f, 0.07f, 0.004f, jeansShade), 0f, -0.045f, 0.101f);

        // ---- Chest (frame y = 1.13): casual button-up shirt, worn untucked ----
        put(r.chest, cyl(0.30f, 0.40f, 0.195f, shirt), 0f, 0f, -0.005f);
        put(r.chest, ell(0.315f, 0.20f, 0.205f, shirt), 0f, 0.12f, 0.005f);
        put(r.chest, ell(0.27f, 0.14f, 0.17f, shirt), 0f, 0.20f, -0.010f);
        Model shoulder = ell(0.11f, 0.09f, 0.125f, shirt);
        put(r.chest, shoulder, -0.165f, 0.19f, 0f, 0f, 0f, -12f);
        put(r.chest, shoulder, 0.165f, 0.19f, 0f, 0f, 0f, 12f);
        put(r.chest, cyl(0.312f, 0.06f, 0.208f, shirtShade), 0f, -0.20f, -0.005f);   // hem
        put(r.chest, box(0.020f, 0.24f, 0.006f, shirtShade), 0f, -0.085f, 0.094f);  // placket
        Model btn = ell(0.009f, 0.009f, 0.005f, button);
        put(r.chest, btn, 0f, 0.165f, 0.101f);
        put(r.chest, btn, 0f, 0.085f, 0.103f);
        put(r.chest, btn, 0f, -0.140f, 0.098f);
        put(r.chest, btn, 0f, -0.200f, 0.1045f);
        // collar band round the neck + two pointed collar wings
        put(r.chest, cyl(0.118f, 0.036f, 0.112f, shirtShade), 0f, 0.232f, 0.004f);
        strap(r.chest, 0.016f, 0.236f, 0.058f, 0.060f, 0.188f, 0.078f, 0.036f, 0.010f, shirtSeam);
        strap(r.chest, -0.016f, 0.236f, 0.058f, -0.060f, 0.188f, 0.078f, 0.036f, 0.010f, shirtSeam);

        addIdCard(r.chest, new float[]{
            0.050f, 0.245f, 0.045f,
            0.032f, 0.170f, 0.104f,
            0.018f, 0.090f, 0.111f,
            0.006f, 0.012f, 0.1015f}, 0.004f, 0.1005f);

        addBackpack(r.chest, 0.28f, 0.36f, 0.13f, 0.035f, -0.178f, new float[]{
            0.085f, 0.190f, -0.112f,
            0.085f, 0.262f, -0.030f,
            0.085f, 0.245f, 0.045f,
            0.085f, 0.150f, 0.092f,
            0.085f, -0.060f, 0.084f,
            0.158f, -0.090f, 0.020f,
            0.135f, -0.110f, -0.115f});

        // ---- Neck (frame y = 1.34) ----
        put(r.neck, cyl(0.095f, 0.10f, 0.092f, skin), 0f, -0.005f, 0.004f);

        // ---- Head (frame y = 1.45) ----
        put(r.head, ell(0.158f, 0.190f, 0.172f, skin), 0f, 0.022f, -0.008f);   // cranium
        put(r.head, ell(0.132f, 0.100f, 0.125f, skin), 0f, -0.030f, 0.012f);   // jaw & cheeks
        put(r.head, ell(0.050f, 0.034f, 0.040f, skin), 0f, -0.068f, 0.048f);   // chin
        put(r.head, ell(0.020f, 0.042f, 0.030f, skinShade), -0.080f, 0.005f, -0.008f, 0f, -12f, 5f);
        put(r.head, ell(0.020f, 0.042f, 0.030f, skinShade), 0.080f, 0.005f, -0.008f, 0f, 12f, -5f);
        // eyes
        Model white = ell(0.024f, 0.013f, 0.010f, eyeWhite);
        Model irisM = ell(0.012f, 0.012f, 0.006f, iris);
        Model lid = cap(0.0030f, 0.026f, lash);
        Model browM = cap(0.0038f, 0.030f, brow);
        for (int s = -1; s <= 1; s += 2) {
            put(r.head, white, s * 0.032f, 0.018f, 0.072f, 0f, s * 4f, 0f);
            put(r.head, irisM, s * 0.032f, 0.018f, 0.0765f, 0f, s * 4f, 0f);
            put(r.head, lid, s * 0.032f, 0.0245f, 0.0755f, 0f, 0f, 90f);
            put(r.head, browM, s * 0.033f, 0.038f, 0.077f, 0f, s * 6f, 90f - s * 6f);
        }
        // nose & mouth
        put(r.head, cap(0.0075f, 0.034f, skin), 0f, 0.002f, 0.080f, 16f, 0f, 0f);
        put(r.head, ell(0.018f, 0.014f, 0.015f, skin), 0f, -0.013f, 0.085f);
        put(r.head, ell(0.028f, 0.008f, 0.010f, lips), 0f, -0.036f, 0.072f);
        put(r.head, ell(0.025f, 0.010f, 0.010f, lips), 0f, -0.043f, 0.070f);

        // neat short black hair with a side part
        put(r.head, ell(0.172f, 0.140f, 0.185f, hair), 0f, 0.068f, -0.014f);    // cap
        put(r.head, ell(0.164f, 0.150f, 0.152f, hair), 0f, 0.008f, -0.042f);    // back & nape
        Model side = ell(0.030f, 0.064f, 0.090f, hair);
        put(r.head, side, -0.074f, 0.045f, -0.006f);
        put(r.head, side, 0.074f, 0.045f, -0.006f);
        put(r.head, ell(0.140f, 0.048f, 0.074f, hair), 0.006f, 0.104f, 0.046f, -14f, 0f, -5f);   // fringe
        put(r.head, ell(0.070f, 0.030f, 0.060f, hairSheen), -0.020f, 0.128f, 0.030f, -10f, 0f, 8f);
        put(r.head, box(0.004f, 0.006f, 0.070f, skinShade), -0.030f, 0.137f, 0.010f, -8f, 0f, 10f); // part line
        Model burn = ell(0.012f, 0.032f, 0.018f, hair);
        put(r.head, burn, -0.077f, 0.004f, 0.030f);
        put(r.head, burn, 0.077f, 0.004f, 0.030f);

        // ---- Arms (shoulder frame; forearm frame at elbow) ----
        putPair(r.upperArm, ell(0.100f, 0.100f, 0.105f, shirt), 0f, -0.010f, 0f);
        putPair(r.upperArm, cap(0.050f, 0.25f, shirt), 0f, -0.120f, 0f);
        putPair(r.upperArm, cyl(0.106f, 0.040f, 0.106f, shirtShade), 0f, -0.225f, 0f);   // rolled cuff

        putPair(r.foreArm, ell(0.072f, 0.070f, 0.072f, skin), 0f, 0f, 0f);             // elbow
        putPair(r.foreArm, cap(0.036f, 0.24f, skin), 0f, -0.110f, 0f);
        put(r.foreArm[0], cyl(0.076f, 0.020f, 0.076f, watchBand), 0f, -0.195f, 0f);
        put(r.foreArm[0], cyl(0.030f, 0.008f, 0.030f, watchFace), -0.037f, -0.195f, 0f, 0f, 0f, 90f);
        addHands(r, skin, 0.056f, 0.080f, 0.032f, -0.255f);

        // ---- Legs ----
        putPair(r.thigh, ell(0.125f, 0.140f, 0.130f, jeans), 0f, -0.020f, 0f);
        putPair(r.thigh, cap(0.057f, 0.36f, jeans), 0f, -0.190f, 0f);
        putPair(r.shin, ell(0.100f, 0.100f, 0.106f, jeans), 0f, 0f, 0.002f);           // knee
        putPair(r.shin, cap(0.050f, 0.35f, jeans), 0f, -0.175f, -0.004f);
        putPair(r.shin, cyl(0.104f, 0.035f, 0.108f, jeansShade), 0f, -0.335f, -0.004f); // hem

        // sneakers
        putPair(r.foot, cyl(0.088f, 0.045f, 0.095f, shoeShade), 0f, 0f, 0f);           // collar
        putPair(r.foot, ell(0.095f, 0.085f, 0.245f, shoe), 0f, -0.022f, 0.045f);
        putPair(r.foot, box(0.100f, 0.024f, 0.250f, sole), 0f, -0.058f, 0.045f);
        putPair(r.foot, ell(0.100f, 0.030f, 0.080f, sole), 0f, -0.056f, 0.130f);        // round toe sole
        putPair(r.foot, box(0.040f, 0.008f, 0.070f, sole), 0f, 0.020f, 0.075f, 14f, 0f, 0f); // laces
    }

    /** Palm, curled fingers and a thumb pointing forward, at forearm-frame height y. */
    private void addHands(Rig r, Material skin, float w, float h, float d, float y) {
        putPair(r.foreArm, ell(w, h, d, skin), 0f, y, 0f);
        putPair(r.foreArm, ell(w * 0.92f, h * 0.60f, d * 0.95f, skin), 0f, y - h * 0.48f, 0.004f, 10f, 0f, 0f);
        Model thumb = cap(d * 0.30f, h * 0.50f, skin);
        put(r.foreArm[0], thumb, w * 0.18f, y + h * 0.10f, d * 0.55f, 25f, 0f, -20f);
        put(r.foreArm[1], thumb, -w * 0.18f, y + h * 0.10f, d * 0.55f, 25f, 0f, 20f);
    }

    // =====================================================================
    // FEMALE (gender 1)
    // =====================================================================

    private void buildFemale(Rig r) {
        Material skin = m(SLOT_SKIN);
        Material skinShade = m(SLOT_SKIN, 0.92f);
        Material lips = m(SLOT_SKIN, 0.92f, 0.66f, 0.66f);
        Material hair = m(SLOT_HAIR);
        Material hairSheen = m(SLOT_HAIR, 1.7f);
        Material brow = m(SLOT_HAIR, 1.1f);
        Material kurti = m(SLOT_SHIRT);
        Material kurtiTrim = m(SLOT_SHIRT, 0.72f);
        Material pants = m(SLOT_TROUSERS);
        Material pantsShade = m(SLOT_TROUSERS, 0.88f);
        Material strapMat = m(SLOT_SHOES);
        Material orna = m(SLOT_ACCENT);
        Material ornaShade = m(SLOT_ACCENT, 0.90f);

        Material eyeWhite = flat(0.94f, 0.93f, 0.92f);
        Material iris = flat(0.14f, 0.09f, 0.06f);
        Material lash = flat(0.05f, 0.04f, 0.04f);
        Material sole = flat(0.62f, 0.48f, 0.34f);
        Material gold = flat(0.90f, 0.74f, 0.32f);

        // ---- Pelvis ----
        put(r.pelvis, cyl(0.27f, 0.12f, 0.19f, pants), 0f, 0.02f, 0f);
        put(r.pelvis, ell(0.28f, 0.17f, 0.20f, pants), 0f, -0.015f, 0f);

        // ---- Chest: kurti to the hip ----
        put(r.chest, cyl(0.28f, 0.40f, 0.185f, kurti), 0f, 0.01f, -0.005f);
        put(r.chest, ell(0.29f, 0.17f, 0.19f, kurti), 0f, 0.13f, 0.006f);
        put(r.chest, ell(0.235f, 0.13f, 0.155f, kurti), 0f, 0.19f, -0.008f);
        Model shoulder = ell(0.095f, 0.075f, 0.112f, kurti);
        put(r.chest, shoulder, -0.152f, 0.18f, 0f, 0f, 0f, -12f);
        put(r.chest, shoulder, 0.152f, 0.18f, 0f, 0f, 0f, 12f);
        put(r.chest, cyl(0.305f, 0.08f, 0.210f, kurti), 0f, -0.18f, -0.005f);
        put(r.chest, cyl(0.330f, 0.10f, 0.232f, kurti), 0f, -0.25f, -0.005f);           // flared hem
        put(r.chest, cyl(0.334f, 0.018f, 0.236f, kurtiTrim), 0f, -0.292f, -0.005f);      // hem border
        put(r.chest, cyl(0.106f, 0.020f, 0.100f, kurtiTrim), 0f, 0.232f, 0.002f);        // neckline

        // Orna: V over the chest, over both shoulders, ends falling behind
        float[] ornaPath = {
            0.125f, 0.235f, -0.015f,
            0.070f, 0.190f, 0.070f,
            0.035f, 0.130f, 0.108f,
            0.000f, 0.065f, 0.100f};
        strapPath(r.chest, ornaPath, 0.075f, 0.014f, orna, true);
        put(r.chest, ell(0.060f, 0.034f, 0.024f, ornaShade), 0f, 0.062f, 0.100f);
        strapPath(r.chest, new float[]{0.125f, 0.235f, -0.015f, 0.112f, 0.195f, -0.088f},
            0.075f, 0.012f, ornaShade, true);
        put(r.chest, cap(0.004f, 0.10f, gold), 0f, 0.175f, 0.082f, -30f, 0f, 0f);        // necklace hint

        addIdCard(r.chest, new float[]{
            0.045f, 0.235f, 0.040f,
            0.030f, 0.160f, 0.100f,
            0.017f, 0.100f, 0.103f,
            0.006f, 0.000f, 0.093f}, -0.008f, 0.0925f);

        addBackpack(r.chest, 0.26f, 0.34f, 0.12f, 0.030f, -0.165f, new float[]{
            0.080f, 0.185f, -0.104f,
            0.080f, 0.250f, -0.020f,
            0.080f, 0.240f, 0.048f,
            0.080f, 0.150f, 0.097f,
            0.080f, -0.060f, 0.082f,
            0.150f, -0.090f, 0.015f,
            0.130f, -0.110f, -0.105f});

        // ---- Neck ----
        put(r.neck, cyl(0.080f, 0.10f, 0.078f, skin), 0f, 0f, 0.004f);

        // ---- Head ----
        put(r.head, ell(0.145f, 0.178f, 0.158f, skin), 0f, 0.018f, -0.006f);
        put(r.head, ell(0.118f, 0.088f, 0.110f, skin), 0f, -0.030f, 0.012f);
        put(r.head, ell(0.040f, 0.028f, 0.032f, skin), 0f, -0.064f, 0.044f);
        put(r.head, ell(0.017f, 0.036f, 0.026f, skinShade), -0.073f, 0.004f, -0.008f, 0f, -12f, 5f);
        put(r.head, ell(0.017f, 0.036f, 0.026f, skinShade), 0.073f, 0.004f, -0.008f, 0f, 12f, -5f);
        Model stud = ell(0.008f, 0.008f, 0.008f, gold);
        put(r.head, stud, -0.075f, -0.016f, 0.000f);
        put(r.head, stud, 0.075f, -0.016f, 0.000f);
        Model white = ell(0.022f, 0.013f, 0.009f, eyeWhite);
        Model irisM = ell(0.012f, 0.012f, 0.006f, iris);
        Model lid = cap(0.0030f, 0.025f, lash);
        Model browM = cap(0.0028f, 0.026f, brow);
        for (int s = -1; s <= 1; s += 2) {
            put(r.head, white, s * 0.029f, 0.015f, 0.066f, 0f, s * 4f, 0f);
            put(r.head, irisM, s * 0.029f, 0.015f, 0.0705f, 0f, s * 4f, 0f);
            put(r.head, lid, s * 0.029f, 0.0215f, 0.0695f, 0f, 0f, 90f);
            put(r.head, browM, s * 0.030f, 0.033f, 0.071f, 0f, s * 6f, 90f - s * 8f);
        }
        put(r.head, cap(0.0060f, 0.030f, skin), 0f, -0.001f, 0.073f, 16f, 0f, 0f);
        put(r.head, ell(0.014f, 0.011f, 0.012f, skin), 0f, -0.014f, 0.078f);
        put(r.head, ell(0.024f, 0.007f, 0.008f, lips), 0f, -0.033f, 0.067f);
        put(r.head, ell(0.022f, 0.009f, 0.009f, lips), 0f, -0.040f, 0.065f);

        // shoulder-length black hair, side-swept fringe
        put(r.head, ell(0.158f, 0.132f, 0.170f, hair), 0f, 0.056f, -0.012f);            // crown
        put(r.head, ell(0.152f, 0.240f, 0.112f, hair), 0f, -0.060f, -0.046f);           // back fall
        Model sideHair = ell(0.036f, 0.150f, 0.092f, hair);
        put(r.head, sideHair, -0.068f, -0.010f, -0.006f, 0f, 0f, -4f);
        put(r.head, sideHair, 0.068f, -0.010f, -0.006f, 0f, 0f, 4f);
        Model lock = cap(0.017f, 0.17f, hair);
        put(r.head, lock, -0.066f, -0.050f, 0.012f, 6f, 0f, 3f);
        put(r.head, lock, 0.066f, -0.050f, 0.012f, 6f, 0f, -3f);
        put(r.head, ell(0.128f, 0.046f, 0.064f, hair), 0.010f, 0.084f, 0.050f, -12f, 0f, -8f);   // fringe
        put(r.head, ell(0.064f, 0.028f, 0.056f, hairSheen), -0.018f, 0.112f, 0.026f, -10f, 0f, 8f);

        // ---- Arms: 3/4 kurti sleeves ----
        putPair(r.upperArm, ell(0.090f, 0.090f, 0.095f, kurti), 0f, -0.010f, 0f);
        putPair(r.upperArm, cap(0.043f, 0.25f, kurti), 0f, -0.120f, 0f);
        putPair(r.foreArm, ell(0.080f, 0.072f, 0.082f, kurti), 0f, 0f, 0f);
        putPair(r.foreArm, cap(0.039f, 0.12f, kurti), 0f, -0.050f, 0f);
        putPair(r.foreArm, cyl(0.082f, 0.020f, 0.082f, kurtiTrim), 0f, -0.105f, 0f);
        putPair(r.foreArm, cap(0.032f, 0.20f, skin), 0f, -0.135f, 0f);
        put(r.foreArm[0], cyl(0.066f, 0.010f, 0.066f, gold), 0f, -0.195f, 0f);           // bangle
        addHands(r, skin, 0.050f, 0.070f, 0.028f, -0.245f);

        // ---- Legs: salwar / churidar ----
        putPair(r.thigh, ell(0.115f, 0.130f, 0.125f, pants), 0f, -0.015f, 0f);
        putPair(r.thigh, cap(0.054f, 0.36f, pants), 0f, -0.190f, 0f);
        putPair(r.shin, ell(0.094f, 0.092f, 0.098f, pants), 0f, 0f, 0f);
        putPair(r.shin, cap(0.046f, 0.30f, pants), 0f, -0.150f, -0.004f);
        putPair(r.shin, cap(0.037f, 0.12f, pantsShade), 0f, -0.300f, -0.004f);

        // sandals
        putPair(r.foot, cap(0.030f, 0.07f, skin), 0f, 0.012f, 0f);                        // ankle
        putPair(r.foot, ell(0.070f, 0.045f, 0.190f, skin), 0f, -0.030f, 0.040f);
        putPair(r.foot, box(0.080f, 0.014f, 0.210f, sole), 0f, -0.055f, 0.040f);
        putPair(r.foot, box(0.078f, 0.014f, 0.036f, strapMat), 0f, -0.012f, 0.090f, 12f, 0f, 0f);
        putPair(r.foot, cyl(0.070f, 0.010f, 0.070f, strapMat), 0f, -0.004f, 0f);
    }

    // =====================================================================
    // Variants
    // =====================================================================

    private Rig recolor(Rig src, Appearance look) {
        Rig r = new Rig();
        copyParts(src.pelvis, r.pelvis, look);
        copyParts(src.chest, r.chest, look);
        copyParts(src.neck, r.neck, look);
        copyParts(src.head, r.head, look);
        for (int s = 0; s < 2; s++) {
            copyParts(src.upperArm[s], r.upperArm[s], look);
            copyParts(src.foreArm[s], r.foreArm[s], look);
            copyParts(src.thigh[s], r.thigh[s], look);
            copyParts(src.shin[s], r.shin[s], look);
            copyParts(src.foot[s], r.foot[s], look);
        }
        return r;
    }

    private void copyParts(Array<Part> from, Array<Part> to, Appearance look) {
        for (int i = 0; i < from.size; i++) {
            Part p = from.get(i);
            if (p.group == GROUP_BAG && !look.hasBag) continue;
            if (p.group == GROUP_ID && !look.hasIdCard) continue;
            ModelInstance inst = new ModelInstance(p.model); // copies materials
            if (p.slot != SLOT_FIXED) {
                Color c = look.slot(p.slot);
                ColorAttribute ca = (ColorAttribute) inst.materials.first().get(ColorAttribute.Diffuse);
                if (ca != null) {
                    ca.color.set(MathUtils.clamp(c.r * p.tr, 0f, 1f), MathUtils.clamp(c.g * p.tg, 0f, 1f),
                        MathUtils.clamp(c.b * p.tb, 0f, 1f), 1f);
                }
            }
            to.add(new Part(p.model, inst, p.local, p.slot, p.tr, p.tg, p.tb, p.group));
        }
    }

    private Rig rigFor(int g, int variant) {
        if (variant <= 0) return baseRigs[g];
        return variantRigs[variant % VARIANT_COUNT][g];
    }

    private Rig rigFor(int g, Appearance look) {
        Rig[] arr = lookRigs.get(look);
        if (arr == null) {
            arr = new Rig[2];
            lookRigs.put(look, arr);
        }
        if (arr[g] == null) arr[g] = recolor(baseRigs[g], look);
        return arr[g];
    }

    // =====================================================================
    // Rendering
    // =====================================================================

    private void drawParts(ModelBatch batch, Environment env, Array<Part> parts, Matrix4 base) {
        for (int i = 0; i < parts.size; i++) {
            Part p = parts.get(i);
            p.inst.transform.set(base).mul(p.local);
            batch.render(p.inst, env);
        }
    }

    /** Advances the idle clock once per frame, however many times render() is called. */
    private void tick() {
        if (Gdx.graphics == null) {
            idleTime += 0.016f;
            return;
        }
        long frame = Gdx.graphics.getFrameId();
        if (frame != lastFrameId) {
            lastFrameId = frame;
            idleTime += Gdx.graphics.getDeltaTime();
        }
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               false, false, 0, 0f, false, 0f, 0, 0);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               isCrouching, isAttacking, attackCombo, attackProgress,
               isSittingWater, sitProgress, 0, 0);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               isCrouching, false, isAttacking, attackCombo, attackProgress,
               isSittingWater, sitProgress, gender, 0);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isBlocking, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               isCrouching, isBlocking, isAttacking, attackCombo, attackProgress,
               isSittingWater, sitProgress, gender, 0);
    }

    /**
     * Full render with a colour variant: 0 = protagonist, 1..{@link #VARIANT_COUNT}-1 = NPC students
     * (values wrap modulo VARIANT_COUNT). All variants are pre-built in the constructor with their own
     * instances/materials, so many students can share one ModelBatch begin/end; no per-call allocation.
     */
    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender, int variant) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               isCrouching, false, isAttacking, attackCombo, attackProgress,
               isSittingWater, sitProgress, gender, variant);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isBlocking, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender, int variant) {
        tick();
        int g = (gender == 1) ? 1 : 0;
        draw(batch, env, rigFor(g, variant), g, pos, headingDegrees, walkCycle, isMoving, isSprinting,
             isCrouching, isBlocking, isAttacking, attackCombo, attackProgress, isSittingWater, sitProgress,
             variant * 1.37f);
    }

    /**
     * Full render with a custom {@link Appearance}. The rig is cached per Appearance object
     * (identity) and built on first use, so reuse the same instance each frame and don't mutate it
     * afterwards. Prefer the int-variant overload for crowds.
     */
    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender, Appearance look) {
        render(batch, env, pos, headingDegrees, walkCycle, isMoving, isSprinting,
               isCrouching, false, isAttacking, attackCombo, attackProgress,
               isSittingWater, sitProgress, gender, look);
    }

    public void render(ModelBatch batch, Environment env, Vector3 pos, float headingDegrees,
                       float walkCycle, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isBlocking, boolean isAttacking, int attackCombo, float attackProgress,
                       boolean isSittingWater, float sitProgress, int gender, Appearance look) {
        tick();
        int g = (gender == 1) ? 1 : 0;
        Rig rig = look == null ? baseRigs[g] : rigFor(g, look);
        float phase = look == null ? 0f : (System.identityHashCode(look) % 97) * 0.13f;
        draw(batch, env, rig, g, pos, headingDegrees, walkCycle, isMoving, isSprinting,
             isCrouching, isBlocking, isAttacking, attackCombo, attackProgress, isSittingWater, sitProgress, phase);
    }

    private void draw(ModelBatch batch, Environment env, Rig rig, int g, Vector3 pos, float headingDegrees,
                      float walkCycle, boolean isMoving, boolean isSprinting,
                      boolean isCrouching, boolean isBlocking, boolean isAttacking, int attackCombo, float attackProgress,
                      boolean isSittingWater, float sitProgress, float idlePhase) {

        float idle = idleTime + idlePhase;

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

        float breathe = (!isMoving && !isAttacking) ? MathUtils.sin(idle * 2.2f) * 0.006f : 0f;

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

        if (isBlocking) {
            // Defensive martial arts guard / parry: both arms raised in front of face
            attackArmL = -82f;
            attackForeArmL = -95f;
            attackArmR = -82f;
            attackForeArmR = -95f;
            torsoPitch += 8f;
        } else if (isAttacking) {
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
            } else if (attackCombo == 4) {
                // Low sweeping martial arts kick
                torsoPitch = 22f * strikePower;
                torsoTwist = 55f * strikePower;
                attackLegR = -75f * strikePower;
                attackKneeR = 48f * strikePower;
            }
        }

        rootTransform.idt();
        rootTransform.translate(pos.x, pos.y + bobOffset, pos.z);
        rootTransform.rotate(Vector3.Y, headingDegrees);

        float twist = isAttacking ? torsoTwist : (swing * 0.12f);

        // Pelvis
        segment.set(rootTransform).translate(0f, pelvisHeight, 0f).rotate(Vector3.Y, twist);
        drawParts(batch, env, rig.pelvis, segment);

        // Chest & torso (incl. backpack, lanyard, orna)
        float chestY = isCrouching ? 0.72f : (isSittingWater ? 0.65f : 1.13f);
        float chestZ = isCrouching ? 0.12f : 0f;
        segment.set(rootTransform).translate(0f, chestY + breathe, chestZ)
            .rotate(Vector3.Y, -twist)
            .rotate(Vector3.X, torsoPitch);
        drawParts(batch, env, rig.chest, segment);

        // Neck
        float neckY = isCrouching ? 0.90f : (isSittingWater ? 0.84f : 1.34f);
        float neckZ = isCrouching ? 0.20f : 0f;
        segment.set(rootTransform).translate(0f, neckY + breathe, neckZ)
            .rotate(Vector3.Y, -twist * 0.5f)
            .rotate(Vector3.X, torsoPitch * 0.7f);
        drawParts(batch, env, rig.neck, segment);

        // Head
        float sprintHeadLean = isSprinting && isMoving ? 8f : 0f;
        float headY = isCrouching ? 0.99f : (isSittingWater ? 0.93f : 1.45f);
        float headZ = isCrouching ? 0.25f : 0f;
        segment.set(rootTransform).translate(0f, headY + breathe - bobOffset * 0.3f, headZ)
            .rotate(Vector3.X, headPitch + sprintHeadLean)
            .rotate(Vector3.Y, -twist * 0.3f);
        drawParts(batch, env, rig.head, segment);

        // Limbs
        for (int side = 0; side < 2; side++) {
            float sx = side == 0 ? -1f : 1f;

            if (isSittingWater) {
                float sitThighAngle = -85f;
                segment.set(rootTransform).translate(sx * 0.105f, pelvisHeight - 0.04f, 0.08f)
                    .rotate(Vector3.X, sitThighAngle);
                drawParts(batch, env, rig.thigh[side], segment);

                float shinDangleAngle = 82f;
                float kickPhase = idle * 2.8f + (side == 0 ? 0f : MathUtils.PI);
                float footKick = MathUtils.sin(kickPhase) * 12f;
                segment.translate(0f, -0.38f, 0f).rotate(Vector3.X, shinDangleAngle + footKick);
                drawParts(batch, env, rig.shin[side], segment);

                tmp.set(segment).translate(0f, -0.37f, 0f).rotate(Vector3.X, -10f);
                drawParts(batch, env, rig.foot[side], tmp);

                segment.set(rootTransform).translate(sx * 0.185f, chestY + 0.10f, -0.06f)
                    .rotate(Vector3.X, 15f)
                    .rotate(Vector3.Z, sx * -8f);
                drawParts(batch, env, rig.upperArm[side], segment);

                segment.translate(0f, -0.27f, 0f).rotate(Vector3.X, 35f);
                drawParts(batch, env, rig.foreArm[side], segment);
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
                drawParts(batch, env, rig.thigh[side], segment);

                segment.translate(0f, -0.38f, 0f);
                if (isAttacking && side == 1 && attackKneeR != 0f) {
                    kneeAngle += attackKneeR;
                }
                segment.rotate(Vector3.X, kneeAngle);
                drawParts(batch, env, rig.shin[side], segment);

                tmp.set(segment).translate(0f, -0.37f, 0f);
                drawParts(batch, env, rig.foot[side], tmp);

                float armSwing = isMoving ? -swing * (side == 0 ? 28f : -28f) * (isSprinting ? 1.6f : 1f) : 0f;
                float armBaseAngle = 3f;

                if (isAttacking) {
                    if (side == 0 && attackArmL != 0f) armSwing = attackArmL;
                    if (side == 1 && attackArmR != 0f) armSwing = attackArmR;
                }

                segment.set(rootTransform).translate(sx * (g == 1 ? 0.165f : 0.180f), chestY + 0.18f + breathe, chestZ)
                    .rotate(Vector3.X, armBaseAngle + armSwing)
                    .rotate(Vector3.Z, sx * -3.5f)
                    .rotate(Vector3.Y, sx * 4.0f);
                drawParts(batch, env, rig.upperArm[side], segment);

                float elbowBend = isMoving ? (isSprinting ? 52f : 24f) : 8f;
                if (isAttacking) {
                    if (side == 0 && attackForeArmL != 0f) elbowBend = Math.abs(attackForeArmL);
                    if (side == 1 && attackForeArmR != 0f) elbowBend = Math.abs(attackForeArmR);
                }

                segment.translate(0f, -0.27f, 0f).rotate(Vector3.X, -elbowBend).rotate(Vector3.Y, sx * 8f);
                drawParts(batch, env, rig.foreArm[side], segment);
            }
        }
    }

    @Override
    public void dispose() {
        for (Model mdl : models) {
            mdl.dispose();
        }
        models.clear();
        lookRigs.clear();
    }
}
