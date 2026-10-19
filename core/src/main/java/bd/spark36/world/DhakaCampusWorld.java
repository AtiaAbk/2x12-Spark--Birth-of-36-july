package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.render.PbrEnvironment;
import bd.spark36.render.WaterRenderer;
import bd.spark36.render.GraphicsTier;
import bd.spark36.world.landmarks.OpeningBuilder;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

/**
 * 3D environment representing Dhaka University Curzon Hall campus with high-fidelity
 * procedural textures, Indo-Saracenic terracotta architecture, tree canopies, vintage
 * lampposts with student movement banners, parked bicycles, and golden-hour lighting.
 */
public class DhakaCampusWorld implements Disposable {

    /** Tree placements {x, z, kind}: 0 = rain tree, 1 = krishnachura, 2 = royal palm, 3 = feathery jira-jira compound tree. */
    public static final float[][] TREE_LOCATIONS = {
        // Prominent heritage trees matching user reference image:
        // Iconic vibrant Red Krishnachura tree prominently situated in the front-left background!
        {-19.5f, 14f, 1},  // Iconic Red Krishnachura tree (matches uploaded image background!)
        {-19.5f, -2f, 1},  // Flame tree flank
        {19.5f, -2f, 1},   // Flame tree flank
        {19.5f, 14f, 3},   // Feathery "jira-jira" bipinnate tree
        {-13.5f, 32f, 3},  // Majestic feathery "jira-jira" compound tree
        {13.5f, 32f, 1},   // Heritage Red Krishnachura tree
        {-14.5f, 50f, 3},  // Feathery "jira-jira" compound tree
        {14.5f, 50f, 1},   // Red Krishnachura tree
        {-28.0f, 16f, 0}, {28.0f, 16f, 1}, // Rain tree, Krishnachura
        {-48.0f, 6f, 1},  {48.0f, 6f, 3},
        {-62.0f, 30f, 0}, {62.0f, 30f, 0},
        {-32.0f, -12f, 1}, {32.0f, -12f, 3},
        {-22.0f, 68f, 3}, {22.0f, 68f, 1},
        {-11.5f, 71f, 0}, // beside (not inside) the main gate

        // Royal Palms along perimeter
        {-18f, 2f, 2}, {18f, 2f, 2},
        {-20f, 40f, 2}, {20f, 40f, 2}
    };

    public static final Vector3 BOUNDARY_STONE_POS = new Vector3(7.8f, 0f, 36.5f);
    private static final int SHADOW_MAP_SIZE = bd.spark36.render.GraphicsTier.fromSystemProperty().shadowMapSize;

    private final Environment environment;
    private final PbrEnvironment pbrEnvironment;
    private final DirectionalShadowLight sunLight;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final Array<ModelInstance> boundarySparkles = new Array<>();
    private float animTime = 0f;

    public static class PondFish {
        public float centerX, centerZ;
        public float radiusX, radiusZ;
        public float speed;
        public float angle;
        public float swimPhase;
        public float depthY;
        public ModelInstance body;
        public ModelInstance tail;
    }
    private final Array<PondFish> pondFishes = new Array<>();

    // Solid geometry for player/camera collision: {minX, minY, minZ, maxX, maxY, maxZ}.
    // Trunks and bamboo are added explicitly while building; everything else is derived from the
    // instances' real bounds so no solid object can be forgotten.
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();
    // Instances that must never block: ground, water, sparkles and the merged foliage meshes
    private final java.util.Set<ModelInstance> nonSolid = new java.util.HashSet<>();
    // Architectural facade panels excluded from shadow pass to eliminate self-shadow acne and jagged cuts
    private final java.util.Set<ModelInstance> shadowExclusions = new java.util.HashSet<>();
    private final Array<ModelInstance> foliageInstances = new Array<>();

    // Translucent effects (light shafts): drawn after the scene, never cast shadows or collide
    private final Array<ModelInstance> effects = new Array<>();
    private BlendingAttribute beamBlend;
    private static final float BEAM_BASE_OPACITY = 0.10f;

    // Animated water shimmer (pukur)
    private ModelInstance waterSurface;      // main water plane (animated tint)
    private ModelInstance waterShimmer;      // scrolling caustic ripple layer
    private TextureAttribute waterRippleTex;
    private final com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute waterDiffuse =
        ColorAttribute.createDiffuse(new com.badlogic.gdx.graphics.Color(0.20f, 0.55f, 0.72f, 1f));
    private final com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute shimmerDiffuse =
        ColorAttribute.createDiffuse(new com.badlogic.gdx.graphics.Color(0.55f, 0.82f, 0.95f, 0f));

    // Realistic Water & Lily-Pad Buoyancy System (Phase 2)
    private final WaterWaveModel waveModel = new WaterWaveModel();
    private final WaterRenderer waterRenderer;
    private final Array<LilyPad> lilyPads = new Array<>();
    private final Array<Model> padModels = new Array<>();

    // Realistic Vegetation & Wind Simulation System (Phase 3)
    private final VegetationManager vegetationManager = new VegetationManager();
    private com.badlogic.gdx.graphics.Camera activeCam = null;

    // Realistic Terrain, Ground Materials & Surface Blending System (Phase 4)
    private final TerrainSurface terrainSurface = new TerrainSurface();

    // Dynamic Weather & Atmospheric Lighting System (Phase 5)
    private final WeatherController weatherController = new WeatherController();
    private ColorAttribute ambientAttr;
    private ColorAttribute fogColorAttr;
    private FogAttribute fogEquationAttr;

    // Facade opening materials & orientation debug test card
    private final TextureFactory texturesRef;
    public Material colonialWindowMat;
    public Material teakDoorMat;
    public Material grandPortalMat;
    public Material porticoTripleWinMat;
    public final Array<ModelInstance> wingDoors = new Array<>();
    public final Array<ModelInstance> wingWindows = new Array<>();
    public ModelInstance grandPortalInst;
    public ModelInstance porticoTripleWinInst;
    private boolean testCardActive = false;
    public static boolean debugDrawNormals = "true".equalsIgnoreCase(System.getProperty("bd.spark36.showNormals"));

    public DhakaCampusWorld(TextureFactory textures) {
        this.texturesRef = textures;
        this.waterRenderer = new WaterRenderer();
        environment = new Environment();

        // Dynamic sun with rich, cinematic shadow contrast calibrated to active weather state
        sunLight = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 120f, 120f, 1f, 140f);
        sunLight.set(weatherController.getCurrentSunColor(), weatherController.getCurrentSunDirection());
        environment.add(sunLight);
        environment.shadowMap = sunLight;

        // Rich daylight ambient with natural blue sky bounce
        ambientAttr = new ColorAttribute(ColorAttribute.AmbientLight, weatherController.getCurrentAmbientColor());
        environment.set(ambientAttr);

        // Soft front fill light for natural character and facade illumination
        DirectionalLight fillLight = new DirectionalLight();
        fillLight.set(new Color(0.24f, 0.23f, 0.20f, 1f), new Vector3(0.20f, -0.30f, 0.92f).nor());
        environment.add(fillLight);

        // Atmospheric depth haze & distance fog equation (gdx-gltf native fogFlag + fogEquationFlag)
        fogColorAttr = new ColorAttribute(ColorAttribute.Fog, weatherController.getCurrentFogColor());
        environment.set(fogColorAttr);
        fogEquationAttr = FogAttribute.createFog(
            weatherController.getCurrentFogNear(),
            weatherController.getCurrentFogFar(),
            weatherController.getCurrentFogExponent()
        );
        environment.set(fogEquationAttr);

        pbrEnvironment = new PbrEnvironment();
        pbrEnvironment.setupOutdoorIBL(sunLight);
        pbrEnvironment.applyTo(environment);

        buildCampus(textures);
        buildColliders();
    }

    /**
     * A mesh being filled with one material, that quietly rolls over to a fresh mesh before it hits
     * the ~32k-vertex limit (16-bit indices), so callers can just keep adding geometry.
     */
    private final class FoliageBatch implements GladeGeometry.PartSupplier {
        private static final int MAX_VERTICES = 30000;
        private final String id;
        private final Material material;
        private final long attributes;
        private ModelBuilder builder;
        private MeshPartBuilder part;

        FoliageBatch(String id, Material material, long attributes) {
            this.id = id;
            this.material = material;
            this.attributes = attributes;
            open();
        }

        private void open() {
            builder = new ModelBuilder();
            builder.begin();
            part = builder.part(id, GL20.GL_TRIANGLES, attributes, material);
        }

        /** The part to write into, first starting a new mesh if {@code needed} more vertices won't fit. */
        MeshPartBuilder part(int needed) {
            if (((MeshBuilder) part).getNumVertices() + needed > MAX_VERTICES) {
                close();
                open();
            }
            return part;
        }

        @Override
        public MeshPartBuilder getPart(int needed) {
            return part(needed);
        }

        void close() {
            registerFoliage(builder);
        }
    }

    /**
     * Two crossed translucent quads along the sunlight direction, from the ground up toward the
     * sun: a soft volumetric shaft that reads from any viewing angle.
     */
    private void addLightShaft(MeshPartBuilder mpb, float baseX, float baseZ, float width, float length, Vector3 towardSun) {
        Vector3 w1 = new Vector3(towardSun).crs(Vector3.Y).nor().scl(width * 0.5f);
        Vector3 w2 = new Vector3(towardSun).crs(w1).nor().scl(width * 0.5f);
        Vector3 base = new Vector3(baseX, -0.4f, baseZ);
        Vector3 top = new Vector3(base).mulAdd(towardSun, length);

        for (Vector3 w : new Vector3[]{w1, w2}) {
            VertexInfo a = new VertexInfo().setPos(base.x - w.x, base.y - w.y, base.z - w.z).setNor(0f, 1f, 0f).setUV(0f, 0f);
            VertexInfo b = new VertexInfo().setPos(base.x + w.x, base.y + w.y, base.z + w.z).setNor(0f, 1f, 0f).setUV(1f, 0f);
            VertexInfo c = new VertexInfo().setPos(top.x + w.x, top.y + w.y, top.z + w.z).setNor(0f, 1f, 0f).setUV(1f, 1f);
            VertexInfo d = new VertexInfo().setPos(top.x - w.x, top.y - w.y, top.z - w.z).setNor(0f, 1f, 0f).setUV(0f, 1f);
            mpb.rect(a, b, c, d);
        }
    }

    /**
     * Finishes a foliage batch and adds it to the scene. Foliage (limbs, canopies, fronds, bushes)
     * is never derived into a collision box: tree trunks and bamboo stands get explicit ones.
     */
    private void registerFoliage(ModelBuilder builder) {
        Model foliageModel = builder.end();
        models.add(foliageModel);
        ModelInstance foliageInstance = new ModelInstance(foliageModel);
        nonSolid.add(foliageInstance);
        foliageInstances.add(foliageInstance);
        instances.add(foliageInstance);
    }

    /** Solid boxes for the player and camera to collide with. */
    public java.util.List<float[]> getColliders() {
        return colliders;
    }

    /**
     * Derives a collision box from every solid instance's world-space bounds. Skips things that
     * are too low to matter (paving, roads), too high to reach (cornices, domes, lanterns) and the
     * non-solid set. Hedge-height boxes are nudged just over the step-up limit so they read as
     * walls to jump over instead of stairs to walk up.
     */
    private void buildColliders() {
        nonSolid.add(instances.get(0)); // ground plane
        if (waterSurface != null) nonSolid.add(waterSurface);
        if (waterShimmer != null) nonSolid.add(waterShimmer);
        for (ModelInstance sp : boundarySparkles) nonSolid.add(sp);

        BoundingBox bb = new BoundingBox();
        for (ModelInstance inst : instances) {
            if (nonSolid.contains(inst)) continue;
            inst.calculateBoundingBox(bb);
            // ModelInstance bounds are local to the model. Apply the instance placement
            // before handing them to the player's world-space collision checks.
            bb.mul(inst.transform);
            float top = bb.max.y;
            if (top < 0.30f || bb.min.y > 2.0f) continue;
            if (top > 0.60f && top <= 0.68f) top = 0.72f;
            colliders.add(new float[]{bb.min.x, Math.max(bb.min.y, 0f), bb.min.z, bb.max.x, top, bb.max.z});
        }
    }

    /**
     * Creates an upright rectangular quad mesh facing +Z (normal: 0, 0, 1) with standard upright UV mapping:
     * (0,0) Top-Left, (1,0) Top-Right, (1,1) Bottom-Right, (0,1) Bottom-Left.
     * Replaces mb.createBox(...) which transposed UV coordinates (rotating 90 deg clockwise) on +Z face.
     */
    public static Model createUprightQuad(ModelBuilder mb, float width, float height, Material material, long attributes) {
        mb.begin();
        MeshPartBuilder mpb = mb.part("quad", GL20.GL_TRIANGLES, attributes, material);
        float hw = width * 0.5f;
        float hh = height * 0.5f;

        VertexInfo v00 = new VertexInfo().setPos(-hw, -hh, 0f).setNor(0f, 0f, 1f).setUV(0f, 1f); // Bottom-Left
        VertexInfo v10 = new VertexInfo().setPos( hw, -hh, 0f).setNor(0f, 0f, 1f).setUV(1f, 1f); // Bottom-Right
        VertexInfo v11 = new VertexInfo().setPos( hw,  hh, 0f).setNor(0f, 0f, 1f).setUV(1f, 0f); // Top-Right
        VertexInfo v01 = new VertexInfo().setPos(-hw,  hh, 0f).setNor(0f, 0f, 1f).setUV(0f, 0f); // Top-Left

        mpb.rect(v00, v10, v11, v01);
        return mb.end();
    }

    /**
     * Creates a horizontal paved ground slab with explicit real-world UV scaling
     * to eliminate extreme texture stretching on walkways and roads.
     */
    public static Model createPavedSlab(ModelBuilder mb, float width, float height, float length,
                                        float uScale, float vScale, Material material, long attributes) {
        mb.begin();
        MeshPartBuilder mpb = mb.part("pavedSlab", GL20.GL_TRIANGLES, attributes, material);
        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float hl = length * 0.5f;

        float uMax = width * uScale;
        float vMax = length * vScale;

        // Top surface (+Y) - LibGDX CCW winding convention
        VertexInfo v00 = new VertexInfo().setPos(-hw, hh, -hl).setNor(0f, 1f, 0f).setUV(0f, 0f);
        VertexInfo v01 = new VertexInfo().setPos(-hw, hh,  hl).setNor(0f, 1f, 0f).setUV(0f, vMax);
        VertexInfo v11 = new VertexInfo().setPos( hw, hh,  hl).setNor(0f, 1f, 0f).setUV(uMax, vMax);
        VertexInfo v10 = new VertexInfo().setPos( hw, hh, -hl).setNor(0f, 1f, 0f).setUV(uMax, 0f);
        mpb.rect(v00, v01, v11, v10);

        // Sides
        // North (-Z)
        mpb.rect(
            new VertexInfo().setPos( hw, -hh, -hl).setNor(0f, 0f, -1f).setUV(uMax, 1f),
            new VertexInfo().setPos(-hw, -hh, -hl).setNor(0f, 0f, -1f).setUV(0f, 1f),
            new VertexInfo().setPos(-hw,  hh, -hl).setNor(0f, 0f, -1f).setUV(0f, 0f),
            new VertexInfo().setPos( hw,  hh, -hl).setNor(0f, 0f, -1f).setUV(uMax, 0f)
        );
        // South (+Z)
        mpb.rect(
            new VertexInfo().setPos(-hw, -hh,  hl).setNor(0f, 0f, 1f).setUV(0f, 1f),
            new VertexInfo().setPos( hw, -hh,  hl).setNor(0f, 0f, 1f).setUV(uMax, 1f),
            new VertexInfo().setPos( hw,  hh,  hl).setNor(0f, 0f, 1f).setUV(uMax, 0f),
            new VertexInfo().setPos(-hw,  hh,  hl).setNor(0f, 0f, 1f).setUV(0f, 0f)
        );
        // West (-X)
        mpb.rect(
            new VertexInfo().setPos(-hw, -hh, -hl).setNor(-1f, 0f, 0f).setUV(0f, 1f),
            new VertexInfo().setPos(-hw, -hh,  hl).setNor(-1f, 0f, 0f).setUV(vMax, 1f),
            new VertexInfo().setPos(-hw,  hh,  hl).setNor(-1f, 0f, 0f).setUV(vMax, 0f),
            new VertexInfo().setPos(-hw,  hh, -hl).setNor(-1f, 0f, 0f).setUV(0f, 0f)
        );
        // East (+X)
        mpb.rect(
            new VertexInfo().setPos( hw, -hh,  hl).setNor(1f, 0f, 0f).setUV(0f, 1f),
            new VertexInfo().setPos( hw, -hh, -hl).setNor(1f, 0f, 0f).setUV(vMax, 1f),
            new VertexInfo().setPos( hw,  hh, -hl).setNor(1f, 0f, 0f).setUV(vMax, 0f),
            new VertexInfo().setPos( hw,  hh,  hl).setNor(1f, 0f, 0f).setUV(0f, 0f)
        );

        return mb.end();
    }

    private void buildCampus(TextureFactory textures) {
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        // --- Materials with High-Res Procedural Textures ---
        Material grassMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.lawnGrass),
            ColorAttribute.createDiffuse(new Color(0.92f, 0.95f, 0.90f, 1f))
        );

        Material brickPavementMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.brickPavement),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.94f, 0.90f, 1f))
        );

        Material curzonBrickMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.curzonBrick),
            ColorAttribute.createDiffuse(new Color(0.98f, 0.95f, 0.92f, 1f))
        );

        Material treeTrunkMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.treeBark),
            ColorAttribute.createDiffuse(new Color(0.90f, 0.88f, 0.85f, 1f))
        );

        Material foliageMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.foliage),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.98f, 0.94f, 1f))
        );

        Material krishnachuraMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.krishnachuraBlossom),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 0.96f, 0.94f, 1f))
        );

        // Limbs are built as open tubes, so draw both sides rather than depend on winding
        Material barkTwoSidedMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.treeBark),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.90f, 0.88f, 0.85f, 1f))
        );

        Material leafClumpMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.leafClump),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.98f, 0.92f, 1f)),
            ColorAttribute.createEmissive(new Color(0.015f, 0.035f, 0.01f, 1f))
        );

        Material blossomClumpMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.blossomClump),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 0.97f, 0.94f, 1f)),
            ColorAttribute.createEmissive(new Color(0.03f, 0.02f, 0.01f, 1f))
        );

        Material whiteBlossomClumpMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.whiteBlossomClump),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f)),
            ColorAttribute.createEmissive(new Color(0.03f, 0.03f, 0.025f, 1f))
        );

        Material whiteBlossomCardMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.whiteBlossomAtlas),
            FloatAttribute.createAlphaTest(VegetationConfig.LEAF_ALPHA_CUTOFF),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f)),
            ColorAttribute.createEmissive(new Color(0.02f, 0.04f, 0.015f, 1f))
        );

        Material kashfulCardMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.kashfulAtlas),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.35f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f)),
            ColorAttribute.createEmissive(new Color(0.03f, 0.03f, 0.025f, 1f))
        );

        colonialWindowMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.colonialWindow),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.35f),
            IntAttribute.createCullFace(GL20.GL_BACK),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f))
        );

        teakDoorMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.teakDoorTexture),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.35f),
            IntAttribute.createCullFace(GL20.GL_BACK),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f))
        );

        grandPortalMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.curzonGrandPortal),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.35f),
            IntAttribute.createCullFace(GL20.GL_BACK),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f))
        );

        porticoTripleWinMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.curzonPorticoTripleWindow),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.35f),
            IntAttribute.createCullFace(GL20.GL_BACK),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f))
        );

        // Individual leaf cards: cut out by alpha (opaque queue, writes depth, no sorting artefacts)
        // Seen from both sides. The cutout also shapes the shadows into dappled leaf patterns.
        Material leafCardMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.leafAtlas),
            FloatAttribute.createAlphaTest(VegetationConfig.LEAF_ALPHA_CUTOFF),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f)),
            // Subtle daylight transmission glow
            ColorAttribute.createEmissive(new Color(0.02f, 0.04f, 0.015f, 1f))
        );

        // Bamboo canes are built as open tubes, so draw both sides
        Material bambooBarkMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.bambooCulm),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.98f, 0.90f, 1f))
        );

        Material bambooLeafCardMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.bambooAtlas),
            FloatAttribute.createAlphaTest(VegetationConfig.LEAF_ALPHA_CUTOFF),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f)),
            ColorAttribute.createEmissive(new Color(0.02f, 0.04f, 0.015f, 1f))
        );

        Material grassTuftMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.grassTuftAtlas),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.98f, 0.92f, 1f)),
            ColorAttribute.createEmissive(new Color(0.015f, 0.035f, 0.012f, 1f))
        );

        Material rockMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.rockSurface),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.92f, 1f))
        );

        // Worn dirt path: feathered edges, so it blends into the grass
        Material dirtMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.dirtPath),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.93f, 0.90f, 1f))
        );

        Material bambooCulmMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.bambooCulm),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.98f, 0.92f, 1f))
        );

        Material bambooLeafMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.bambooFoliage),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.98f, 0.92f, 1f))
        );

        Material bushMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.bushFoliage),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.98f, 0.92f, 1f))
        );

        Material weatheredStoneMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.weatheredStone),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.92f, 1f))
        );

        Material curzonPukurWaterMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.curzonWater),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.90f),
            ColorAttribute.createDiffuse(new Color(0.92f, 0.96f, 1.0f, 1f))
        );

        Material sparkleMat = new Material(ColorAttribute.createDiffuse(new Color(1f, 1f, 0.85f, 1f)));

        Material flagMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.bdFlag)
        );

        Material aparajeyoMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.aparajeyoStone),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.94f, 0.92f, 1f))
        );

        Material bannerMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.movementBanner)
        );

        Material asphaltMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.asphaltRoad),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.95f, 1f))
        );

        Material concreteMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.modernistConcrete),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.95f, 0.92f, 1f))
        );

        Material canteenRoofMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.canteenTinRoof),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.90f, 1f))
        );

        Material stoneCurb = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.85f, 0.80f, 1f)));
        Material curzonTrimMat = new Material(ColorAttribute.createDiffuse(new Color(0.85f, 0.80f, 0.72f, 1f)));
        Material goldFinial = new Material(ColorAttribute.createDiffuse(new Color(1.0f, 0.85f, 0.35f, 1f)));
        Material archCavity = new Material(ColorAttribute.createDiffuse(new Color(0.18f, 0.10f, 0.08f, 1f)));
        Material teakDoor = new Material(ColorAttribute.createDiffuse(new Color(0.32f, 0.16f, 0.10f, 1f)));
        Material castIron = new Material(ColorAttribute.createDiffuse(new Color(0.14f, 0.16f, 0.18f, 1f)));
        Material lanternGlow = new Material(ColorAttribute.createDiffuse(new Color(1.0f, 0.92f, 0.60f, 1f)));
        Material waterMat = new Material(ColorAttribute.createDiffuse(new Color(0.24f, 0.52f, 0.62f, 1f)));
        Material woodBench = new Material(ColorAttribute.createDiffuse(new Color(0.42f, 0.28f, 0.16f, 1f)));
        Material bicycleMetal = new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.24f, 0.28f, 1f)));
        Material hedgeMat = new Material(ColorAttribute.createDiffuse(new Color(0.11f, 0.20f, 0.10f, 1f)));
        Material flowerRed = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.16f, 0.18f, 1f)));

        // PBR Metallic-Roughness & Normal Map calibration for Dhaka University campus
        setupPbr(grassMat, GroundConfig.GRASS_METALLIC, GroundConfig.GRASS_ROUGHNESS);
        if (textures.lawnGrassNormal != null) grassMat.set(PBRTextureAttribute.createNormalTexture(textures.lawnGrassNormal));
        setupPbr(brickPavementMat, GroundConfig.BRICK_METALLIC, GroundConfig.BRICK_ROUGHNESS);
        if (textures.brickPavementNormal != null) brickPavementMat.set(PBRTextureAttribute.createNormalTexture(textures.brickPavementNormal));
        setupPbr(curzonBrickMat, 0.0f, 0.75f);
        if (textures.curzonBrickNormal != null) curzonBrickMat.set(PBRTextureAttribute.createNormalTexture(textures.curzonBrickNormal));
        setupPbr(treeTrunkMat, 0.0f, 0.88f);
        if (textures.treeBarkNormal != null) treeTrunkMat.set(PBRTextureAttribute.createNormalTexture(textures.treeBarkNormal));
        setupPbr(foliageMat, 0.0f, 0.70f);
        setupPbr(krishnachuraMat, 0.0f, 0.65f);
        setupPbr(barkTwoSidedMat, 0.0f, 0.88f);
        if (textures.treeBarkNormal != null) barkTwoSidedMat.set(PBRTextureAttribute.createNormalTexture(textures.treeBarkNormal));
        setupPbr(leafClumpMat, 0.0f, 0.68f);
        setupPbr(blossomClumpMat, 0.0f, 0.65f);
        setupPbr(whiteBlossomClumpMat, 0.0f, 0.65f);
        setupPbr(whiteBlossomCardMat, 0.0f, 0.65f);
        setupPbr(kashfulCardMat, 0.0f, 0.65f);
        setupPbr(colonialWindowMat, 0.10f, 0.35f);
        setupPbr(teakDoorMat, 0.0f, 0.60f);
        setupPbr(grandPortalMat, 0.0f, 0.65f);
        setupPbr(porticoTripleWinMat, 0.10f, 0.35f);
        setupPbr(leafCardMat, 0.0f, 0.68f);
        setupPbr(bambooBarkMat, 0.0f, 0.55f);
        setupPbr(bambooLeafCardMat, 0.0f, 0.70f);
        setupPbr(grassTuftMat, 0.0f, 0.65f);
        setupPbr(rockMat, 0.0f, 0.85f);
        setupPbr(dirtMat, GroundConfig.DIRT_METALLIC, GroundConfig.DIRT_ROUGHNESS);
        if (textures.dirtPathNormal != null) dirtMat.set(PBRTextureAttribute.createNormalTexture(textures.dirtPathNormal));
        setupPbr(bambooCulmMat, 0.0f, 0.55f);
        setupPbr(bambooLeafMat, 0.0f, 0.70f);
        setupPbr(bushMat, 0.0f, 0.80f);
        setupPbr(weatheredStoneMat, 0.0f, 0.82f);
        setupPbr(curzonPukurWaterMat, 0.05f, 0.15f);
        setupPbr(sparkleMat, 0.0f, 0.50f);
        setupPbr(flagMat, 0.0f, 0.75f);
        setupPbr(aparajeyoMat, 0.0f, 0.78f);
        setupPbr(bannerMat, 0.0f, 0.75f);
        setupPbr(asphaltMat, GroundConfig.ASPHALT_METALLIC, GroundConfig.ASPHALT_ROUGHNESS);
        if (textures.asphaltRoadNormal != null) asphaltMat.set(PBRTextureAttribute.createNormalTexture(textures.asphaltRoadNormal));
        setupPbr(concreteMat, 0.0f, 0.80f);
        if (textures.modernistConcreteNormal != null) concreteMat.set(PBRTextureAttribute.createNormalTexture(textures.modernistConcreteNormal));
        setupPbr(canteenRoofMat, 0.75f, 0.42f);
        setupPbr(stoneCurb, 0.0f, 0.85f);
        setupPbr(curzonTrimMat, 0.0f, 0.75f);
        setupPbr(goldFinial, 0.90f, 0.25f);
        setupPbr(archCavity, 0.0f, 0.95f);
        setupPbr(teakDoor, 0.0f, 0.60f);
        setupPbr(castIron, 0.85f, 0.35f);
        setupPbr(lanternGlow, 0.0f, 0.50f);
        setupPbr(waterMat, 0.05f, 0.15f);
        setupPbr(woodBench, 0.0f, 0.65f);
        setupPbr(bicycleMetal, 0.85f, 0.30f);
        setupPbr(hedgeMat, 0.0f, 0.82f);
        setupPbr(flowerRed, 0.0f, 0.60f);

        // 1. Central Campus Lawn (Continuous structured terrain grid with real-world UV scale and pond cutout)
        ModelInstance groundInst = terrainSurface.buildTerrain(mb, textures, GraphicsTier.fromSystemProperty(), attr);
        instances.add(groundInst);

        // 2. Grand Herringbone Brick Avenue with Stone Curbs & Landscaped Flowerbeds
        Model avenueTile = createPavedSlab(mb, 7.2f, 0.06f, 7.2f, GroundConfig.BRICK_UV_SCALE, GroundConfig.BRICK_UV_SCALE, brickPavementMat, attr);
        Model curbTile = mb.createBox(0.35f, 0.14f, 7.2f, stoneCurb, attr);
        Model hedgeTile = mb.createBox(0.9f, 0.65f, 7.2f, hedgeMat, attr);
        Model flowerTile = mb.createBox(0.75f, 0.35f, 7.2f, flowerRed, attr);
        models.add(avenueTile);
        models.add(curbTile);
        models.add(hedgeTile);
        models.add(flowerTile);

        // Central Promenade (from South Gate at 76m to Curzon steps at -18m)
        // The Curzon Hall Pukur sits in the middle of the spine (per the campus map), so the
        // promenade splits around it and rejoins via the ring paths below.
        for (float z = -18f; z <= 76f; z += 7.2f) {
            if (z > -6f && z < 20f) continue;
            ModelInstance aInst = new ModelInstance(avenueTile);
            aInst.transform.setTranslation(0f, 0.03f, z);
            instances.add(aInst);

            // Left & Right Stone Curbs
            ModelInstance curbL = new ModelInstance(curbTile);
            curbL.transform.setTranslation(-3.75f, 0.07f, z);
            instances.add(curbL);

            ModelInstance curbR = new ModelInstance(curbTile);
            curbR.transform.setTranslation(3.75f, 0.07f, z);
            instances.add(curbR);

            // Flanking Garden Hedges & Flowerbeds
            // No hedge where the cross avenues (Z=30) and pond-ring links (Z=24) leave the promenade
            boolean atCrossing = z > 20f && z < 36f;
            if (z > -10f && z < 70f && !atCrossing) {
                ModelInstance hL = new ModelInstance(hedgeTile);
                hL.transform.setTranslation(-4.85f, 0.32f, z);
                instances.add(hL);

                ModelInstance hR = new ModelInstance(hedgeTile);
                hR.transform.setTranslation(4.85f, 0.32f, z);
                instances.add(hR);

                ModelInstance fL = new ModelInstance(flowerTile);
                fL.transform.setTranslation(-5.85f, 0.18f, z);
                instances.add(fL);

                ModelInstance fR = new ModelInstance(flowerTile);
                fR.transform.setTranslation(5.85f, 0.18f, z);
                instances.add(fR);
            }
        }

        // Cross-Campus Promenade Network (Connecting West Hub & East Hub)
        Model crossWalkEastWest = createPavedSlab(mb, 5.4f, 0.06f, 4.8f, GroundConfig.BRICK_UV_SCALE, GroundConfig.BRICK_UV_SCALE, brickPavementMat, attr);
        Model crossWalkNorthSouth = createPavedSlab(mb, 4.8f, 0.06f, 5.4f, GroundConfig.BRICK_UV_SCALE, GroundConfig.BRICK_UV_SCALE, brickPavementMat, attr);
        models.add(crossWalkEastWest);
        models.add(crossWalkNorthSouth);

        // West Avenue: from Central Promenade towards Arts Plaza & Central Library (Z=30m, X: -4m to -74m)
        for (float x = -6f; x >= -74f; x -= 5.4f) {
            ModelInstance cwInst = new ModelInstance(crossWalkEastWest);
            cwInst.transform.setTranslation(x, 0.03f, 30f);
            instances.add(cwInst);
        }

        // West North Branch: to Madhur Canteen (X=-56m, Z: 30m to -16m)
        for (float z = 28f; z >= -16f; z -= 5.4f) {
            ModelInstance cwInst = new ModelInstance(crossWalkNorthSouth);
            cwInst.transform.setTranslation(-56f, 0.03f, z);
            instances.add(cwInst);
        }

        // West South Branch: to Hakim Chattar (X=-58m, Z: 30m to 54m)
        for (float z = 32f; z <= 54f; z += 5.4f) {
            ModelInstance cwInst = new ModelInstance(crossWalkNorthSouth);
            cwInst.transform.setTranslation(-58f, 0.03f, z);
            instances.add(cwInst);
        }

        // East Avenue: from Central Promenade towards Raju Roundabout & TSC (Z=30m, X: +4m to +72m)
        for (float x = 6f; x <= 72f; x += 5.4f) {
            ModelInstance cwInst = new ModelInstance(crossWalkEastWest);
            cwInst.transform.setTranslation(x, 0.03f, 30f);
            instances.add(cwInst);
        }

        // East North Branch: to Swadhinata Sangram Sculpture Garden (X=+56m, Z: 30m to -16m)
        for (float z = 28f; z >= -16f; z -= 5.4f) {
            ModelInstance cwInst = new ModelInstance(crossWalkNorthSouth);
            cwInst.transform.setTranslation(56f, 0.03f, z);
            instances.add(cwInst);
        }

        // East South Branch: to Raju Memorial Roundabout (X=+44m, Z: 30m to 42m)
        for (float z = 32f; z <= 42f; z += 5.4f) {
            ModelInstance cwInst = new ModelInstance(crossWalkNorthSouth);
            cwInst.transform.setTranslation(44f, 0.03f, z);
            instances.add(cwInst);
        }

        // Pukur ring paths: run either side of the pond (X=+-14m) and link to the promenade
        // at the north (Z=-10.8m) and south (Z=24m) ends.
        for (float sx : new float[]{-14f, 14f}) {
            for (float z = -10.8f; z <= 24f; z += 5.4f) {
                ModelInstance ring = new ModelInstance(crossWalkNorthSouth);
                ring.transform.setTranslation(sx, 0.03f, z);
                instances.add(ring);
            }
            for (float z : new float[]{-10.8f, 24f}) {
                float dir = Math.signum(sx);
                for (float x = 6.0f; x <= 11.5f; x += 5.4f) {
                    ModelInstance link = new ModelInstance(crossWalkEastWest);
                    link.transform.setTranslation(dir * x, 0.03f, z);
                    instances.add(link);
                }
            }
        }

        // Front Verandah Walkway
        Model frontWalkModel = mb.createBox(96f, 0.06f, 6.2f, brickPavementMat, attr);
        models.add(frontWalkModel);
        ModelInstance frontWalkInst = new ModelInstance(frontWalkModel);
        frontWalkInst.transform.setTranslation(0f, 0.03f, -17f);
        instances.add(frontWalkInst);

        // 3. Curzon Hall Indo-Saracenic Central Building Complex
        // Central Block
        Model curzonBlock = mb.createBox(38f, 14f, 17f, curzonBrickMat, attr);
        models.add(curzonBlock);
        ModelInstance mainBlockInst = new ModelInstance(curzonBlock);
        mainBlockInst.transform.setTranslation(0f, 7.0f, -32f);
        instances.add(mainBlockInst);

        // East & West Wings
        Model curzonWing = mb.createBox(28f, 11.5f, 15f, curzonBrickMat, attr);
        models.add(curzonWing);

        ModelInstance eastWingInst = new ModelInstance(curzonWing);
        eastWingInst.transform.setTranslation(32f, 5.75f, -31f);
        instances.add(eastWingInst);

        ModelInstance westWingInst = new ModelInstance(curzonWing);
        westWingInst.transform.setTranslation(-32f, 5.75f, -31f);
        instances.add(westWingInst);

        // White Ornamental Cornices & Parapets
        Model cornice = mb.createBox(96f, 0.8f, 18f, curzonTrimMat, attr);
        models.add(cornice);
        ModelInstance corniceInst = new ModelInstance(cornice);
        corniceInst.transform.setTranslation(0f, 12.2f, -31.5f);
        instances.add(corniceInst);

        // Rooftop White Perforated Jali / Lattice Balustrade Railing
        Model roofRailing = mb.createBox(96f, 0.9f, 0.35f, curzonTrimMat, attr);
        models.add(roofRailing);
        ModelInstance roofRailingInst = new ModelInstance(roofRailing);
        roofRailingInst.transform.setTranslation(0f, 13.05f, -22.6f);
        instances.add(roofRailingInst);

        // Central Facade Portico Projection
        Model portico = mb.createBox(15f, 15.6f, 3.8f, curzonBrickMat, attr);
        models.add(portico);
        ModelInstance porticoInst = new ModelInstance(portico);
        porticoInst.transform.setTranslation(0f, 7.8f, -22.5f);
        instances.add(porticoInst);

        // Grand Access Steps leading up to Portico
        Model steps = mb.createBox(16f, 0.6f, 4.2f, stoneCurb, attr);
        Model stepBalustrade = mb.createBox(0.4f, 0.9f, 4.2f, curzonTrimMat, attr);
        models.add(steps);
        models.add(stepBalustrade);

        ModelInstance stepsInst = new ModelInstance(steps);
        stepsInst.transform.setTranslation(0f, 0.3f, -19.5f);
        instances.add(stepsInst);

        ModelInstance stepRailL = new ModelInstance(stepBalustrade);
        stepRailL.transform.setTranslation(-8.2f, 0.75f, -19.5f);
        instances.add(stepRailL);

        ModelInstance stepRailR = new ModelInstance(stepBalustrade);
        stepRailR.transform.setTranslation(8.2f, 0.75f, -19.5f);
        instances.add(stepRailR);

        // Authentic Continuous Raised Plinth Base & Stone Coping (Mughal-Gothic Podium)
        // Elevates Curzon Hall 0.60m above ground level across the entire north elevation (96m)
        Model plinthWingBase = mb.createBox(38.0f, 0.52f, 1.8f, curzonBrickMat, attr);
        Model plinthWingCoping = mb.createBox(38.2f, 0.10f, 2.0f, stoneCurb, attr);
        Model plinthReturnBase = mb.createBox(1.8f, 0.52f, 15.0f, curzonBrickMat, attr);
        Model plinthReturnCoping = mb.createBox(2.0f, 0.10f, 15.2f, stoneCurb, attr);
        models.add(plinthWingBase);
        models.add(plinthWingCoping);
        models.add(plinthReturnBase);
        models.add(plinthReturnCoping);

        // West Wing Plinth & Coping
        ModelInstance plinthWest = new ModelInstance(plinthWingBase);
        plinthWest.transform.setTranslation(-27.0f, 0.26f, -22.6f);
        instances.add(plinthWest);
        ModelInstance copeWest = new ModelInstance(plinthWingCoping);
        copeWest.transform.setTranslation(-27.0f, 0.55f, -22.5f);
        instances.add(copeWest);

        // East Wing Plinth & Coping
        ModelInstance plinthEast = new ModelInstance(plinthWingBase);
        plinthEast.transform.setTranslation(27.0f, 0.26f, -22.6f);
        instances.add(plinthEast);
        ModelInstance copeEast = new ModelInstance(plinthWingCoping);
        copeEast.transform.setTranslation(27.0f, 0.55f, -22.5f);
        instances.add(copeEast);

        // Outer Wing Returns at X = ±46m
        ModelInstance plinthRetW = new ModelInstance(plinthReturnBase);
        plinthRetW.transform.setTranslation(-46.0f, 0.26f, -31.0f);
        instances.add(plinthRetW);
        ModelInstance copeRetW = new ModelInstance(plinthReturnCoping);
        copeRetW.transform.setTranslation(-46.0f, 0.55f, -31.0f);
        instances.add(copeRetW);

        ModelInstance plinthRetE = new ModelInstance(plinthReturnBase);
        plinthRetE.transform.setTranslation(46.0f, 0.26f, -31.0f);
        instances.add(plinthRetE);
        ModelInstance copeRetE = new ModelInstance(plinthReturnCoping);
        copeRetE.transform.setTranslation(46.0f, 0.55f, -31.0f);
        instances.add(copeRetE);

        // Two-storey arcades of pointed Indo-Saracenic arches across the main block and both wings.
        // Clean authentic architecture: ground floor deep arched verandah openings with teak doors,
        // and upper floor stately colonial arched windows with cream stone surrounds.
        Material glassMat = new Material(ColorAttribute.createDiffuse(new Color(0.12f, 0.16f, 0.22f, 1f)));
        Material redCarpetMat = new Material(ColorAttribute.createDiffuse(new Color(0.85f, 0.12f, 0.15f, 1f)));
        Material curzonWhiteDome = new Material(ColorAttribute.createDiffuse(new Color(0.96f, 0.95f, 0.91f, 1f)));
        Material curzonRedDome = new Material(ColorAttribute.createDiffuse(new Color(0.68f, 0.20f, 0.16f, 1f)));
        setupPbr(glassMat, 0.10f, 0.15f);
        setupPbr(redCarpetMat, 0.0f, 0.85f);
        setupPbr(curzonWhiteDome, 0.0f, 0.55f);
        setupPbr(curzonRedDome, 0.0f, 0.60f);

        // Window & Door components for the two-storey Curzon facade (Authentic 3D Mughal-Gothic with deep reveals)
        Model archUpperWindow = OpeningBuilder.build3DWindowOpening(mb, 2.20f, 4.10f, 0.22f, 0.12f, curzonBrickMat, curzonTrimMat, colonialWindowMat, attr);
        Model archGroundDoor = OpeningBuilder.build3DDoorOpening(mb, 2.20f, 4.10f, 0.22f, 0.14f, curzonBrickMat, curzonTrimMat, teakDoorMat, attr);
        Model floorBand = mb.createBox(92f, 0.18f, 0.20f, curzonTrimMat, attr);

        // Projecting Mughal Chhajja Sunshade Eaves and Brackets for Upper Windows
        Model winChhajjaSlab = mb.createBox(3.10f, 0.14f, 0.82f, curzonTrimMat, attr);
        Model winChhajjaBracket = mb.createBox(0.20f, 0.48f, 0.65f, curzonTrimMat, attr);

        models.add(archUpperWindow);
        models.add(archGroundDoor);
        models.add(floorBand);
        models.add(winChhajjaSlab);
        models.add(winChhajjaBracket);

        // Perfectly symmetric 14 arcade bays (7 West Wing, 7 East Wing, each 5.2m spacing)
        float[] facadeBays = {
            -41.6f, -36.4f, -31.2f, -26.0f, -20.8f, -15.6f, -10.4f,
             10.4f,  15.6f,  20.8f,  26.0f,  31.2f,  36.4f,  41.6f
        };

        for (float ax : facadeBays) {
            float fz = -23.46f;

            // Ground floor: Authentic 3D carved Burma teak wooden double door with brass knockers & deep reveals
            ModelInstance door = new ModelInstance(archGroundDoor);
            door.transform.setTranslation(ax, 3.25f, fz);
            instances.add(door);
            shadowExclusions.add(door);
            wingDoors.add(door);

            // Upper floor: Stately 3D colonial glazed window with pointed cusped arch, stone sill & corbels
            ModelInstance win = new ModelInstance(archUpperWindow);
            win.transform.setTranslation(ax, 8.50f, fz);
            instances.add(win);
            shadowExclusions.add(win);
            wingWindows.add(win);

            // Projecting Sandstone Chhajja Sunshade Eaves Overhang (Authentic Mughal Climate Architecture)
            ModelInstance chhajja = new ModelInstance(winChhajjaSlab);
            chhajja.transform.setTranslation(ax, 10.75f, -23.09f);
            instances.add(chhajja);
            nonSolid.add(chhajja);

            // Twin carved corbel brackets supporting the window chhajja
            ModelInstance wbrkL = new ModelInstance(winChhajjaBracket);
            wbrkL.transform.setTranslation(ax - 1.05f, 10.45f, -23.18f);
            instances.add(wbrkL);
            nonSolid.add(wbrkL);

            ModelInstance wbrkR = new ModelInstance(winChhajjaBracket);
            wbrkR.transform.setTranslation(ax + 1.05f, 10.45f, -23.18f);
            instances.add(wbrkR);
            nonSolid.add(wbrkR);
        }

        // 3D Engaged Brick Pilasters (Vertical Buttress Piers) with Molded Bases, Belts, and Capitals
        // Breaks flat facade into richly articulated Indo-Saracenic bays with real depth & shadows
        Model pilasterShaftWing = mb.createBox(0.65f, 11.2f, 0.24f, curzonBrickMat, attr);
        Model pilasterShaftCenter = mb.createBox(0.65f, 13.2f, 0.24f, curzonBrickMat, attr);
        Model pilasterBase = mb.createBox(0.78f, 0.48f, 0.32f, curzonTrimMat, attr);
        Model pilasterBelt = mb.createBox(0.74f, 0.22f, 0.30f, curzonTrimMat, attr);
        Model pilasterCapital = mb.createBox(0.82f, 0.42f, 0.36f, curzonTrimMat, attr);

        models.add(pilasterShaftWing);
        models.add(pilasterShaftCenter);
        models.add(pilasterBase);
        models.add(pilasterBelt);
        models.add(pilasterCapital);

        float[] pilasterPositions = {
            -44.2f, -39.0f, -33.8f, -28.6f, -23.4f, -18.2f, -13.0f,
             13.0f,  18.2f,  23.4f,  28.6f,  33.8f,  39.0f,  44.2f
        };

        for (float px : pilasterPositions) {
            boolean isCenter = (Math.abs(px) <= 15.0f);
            float shaftH = isCenter ? 13.2f : 11.2f;
            float shaftY = isCenter ? 7.40f : 6.40f;
            float capY = isCenter ? 14.0f : 12.0f;
            float pz = -23.36f;

            // Brick Pilaster Shaft
            ModelInstance pShaft = new ModelInstance(isCenter ? pilasterShaftCenter : pilasterShaftWing);
            pShaft.transform.setTranslation(px, shaftY, pz);
            instances.add(pShaft);
            nonSolid.add(pShaft);

            // Molded Stone Base resting upon Plinth
            ModelInstance pBase = new ModelInstance(pilasterBase);
            pBase.transform.setTranslation(px, 0.84f, pz + 0.04f);
            instances.add(pBase);
            nonSolid.add(pBase);

            // Stone Mid-Floor Belt Block
            ModelInstance pBelt = new ModelInstance(pilasterBelt);
            pBelt.transform.setTranslation(px, 5.80f, pz + 0.03f);
            instances.add(pBelt);
            nonSolid.add(pBelt);

            // Carved Sandstone Corbel Capital meeting Roof Cornice
            ModelInstance pCap = new ModelInstance(pilasterCapital);
            pCap.transform.setTranslation(px, capY, pz + 0.06f);
            instances.add(pCap);
            nonSolid.add(pCap);
        }

        // Stepped Parapet Battlements (Mughal-Gothic Kanguras) along Roofline
        Model kanguraMerlon = mb.createBox(0.68f, 0.40f, 0.22f, curzonTrimMat, attr);
        models.add(kanguraMerlon);

        for (float kx = -44.2f; kx <= 44.21f; kx += 2.6f) {
            if (Math.abs(kx) < 8.6f) continue; // portico center
            boolean isCenter = (Math.abs(kx) <= 18.5f);
            float ky = isCenter ? 14.85f : 13.50f;
            ModelInstance km = new ModelInstance(kanguraMerlon);
            km.transform.setTranslation(kx, ky, -22.60f);
            instances.add(km);
            nonSolid.add(km);
        }

        // Refined sandstone string course between the floors
        ModelInstance bandInst = new ModelInstance(floorBand);
        bandInst.transform.setTranslation(0f, 5.8f, -23.38f);
        instances.add(bandInst);

        // Grand Central Portal with Monumental Pointed Iwan Archway & Ceremonial Teak Gates
        Model grandPortalModel = OpeningBuilder.build3DGrandPortal(mb, 3.80f, 5.20f, 0.50f, 0.18f, curzonTrimMat, grandPortalMat, attr);
        models.add(grandPortalModel);
        grandPortalInst = new ModelInstance(grandPortalModel);
        grandPortalInst.transform.setTranslation(0f, 3.20f, -20.56f);
        instances.add(grandPortalInst);
        shadowExclusions.add(grandPortalInst);

        // Ceremonial Red Carpet on Central Steps leading up to Portal
        Model redCarpet = mb.createBox(3.8f, 0.04f, 4.6f, redCarpetMat, attr);
        models.add(redCarpet);
        ModelInstance carpetInst = new ModelInstance(redCarpet);
        carpetInst.transform.setTranslation(0f, 0.62f, -19.5f);
        instances.add(carpetInst);

        // Grand Central Portico First-Floor Balcony & Triple Mughal Windows
        Model balconySlab = mb.createBox(8.4f, 0.35f, 1.4f, curzonTrimMat, attr);
        Model balconyRail = mb.createBox(8.4f, 0.85f, 0.15f, curzonTrimMat, attr);
        Model balconySideRail = mb.createBox(0.15f, 0.85f, 1.4f, curzonTrimMat, attr);
        models.add(balconySlab);
        models.add(balconyRail);
        models.add(balconySideRail);

        ModelInstance bSlabInst = new ModelInstance(balconySlab);
        bSlabInst.transform.setTranslation(0f, 9.15f, -19.95f);
        instances.add(bSlabInst);
        ModelInstance bRailInst = new ModelInstance(balconyRail);
        bRailInst.transform.setTranslation(0f, 9.75f, -19.28f);
        instances.add(bRailInst);
        ModelInstance bRailL = new ModelInstance(balconySideRail);
        bRailL.transform.setTranslation(-4.15f, 9.75f, -19.95f);
        instances.add(bRailL);
        ModelInstance bRailR = new ModelInstance(balconySideRail);
        bRailR.transform.setTranslation(4.15f, 9.75f, -19.95f);
        instances.add(bRailR);

        // Triple Pointed Mughal Windows on Portico Upper Storey with colonnette piers & deep reveals
        Model porticoTripleWinModel = OpeningBuilder.build3DPorticoTripleWindow(mb, 7.20f, 3.80f, 0.40f, 0.16f, curzonTrimMat, porticoTripleWinMat, attr);
        models.add(porticoTripleWinModel);
        porticoTripleWinInst = new ModelInstance(porticoTripleWinModel);
        porticoTripleWinInst.transform.setTranslation(0f, 11.40f, -20.56f);
        instances.add(porticoTripleWinInst);
        shadowExclusions.add(porticoTripleWinInst);

        // Projecting carved stone Chhajja (sunshade eaves overhang) above Portico windows
        Model porticoChhajja = mb.createBox(9.6f, 0.22f, 1.2f, curzonTrimMat, attr);
        Model chhajjaBracket = mb.createBox(0.22f, 0.60f, 0.85f, curzonTrimMat, attr);
        models.add(porticoChhajja);
        models.add(chhajjaBracket);

        ModelInstance chhajjaInst = new ModelInstance(porticoChhajja);
        chhajjaInst.transform.setTranslation(0f, 14.1f, -19.95f);
        instances.add(chhajjaInst);

        float[] bracketX = {-3.6f, -1.2f, 1.2f, 3.6f};
        for (float bx : bracketX) {
            ModelInstance brk = new ModelInstance(chhajjaBracket);
            brk.transform.setTranslation(bx, 13.7f, -20.15f);
            instances.add(brk);
        }

        // Flanking vintage carriage lanterns at the Grand Entrance
        Model lanternPost = mb.createBox(0.16f, 0.35f, 0.30f, castIron, attr);
        Model lanternGlass = mb.createBox(0.24f, 0.38f, 0.24f, lanternGlow, attr);
        models.add(lanternPost);
        models.add(lanternGlass);

        float[] lanternX = {-3.3f, 3.3f};
        for (float lx : lanternX) {
            ModelInstance lp = new ModelInstance(lanternPost);
            lp.transform.setTranslation(lx, 5.0f, -20.35f);
            instances.add(lp);
            ModelInstance lg = new ModelInstance(lanternGlass);
            lg.transform.setTranslation(lx, 5.25f, -20.25f);
            instances.add(lg);
        }

        // Curzon Hall Grand White Mughal Bulbous Dome
        Model finialBase = mb.createCone(0.85f, 3.2f, 0.85f, 16, goldFinial, attr);
        Model smallFinial = mb.createCone(0.32f, 1.1f, 0.32f, 12, goldFinial, attr);
        Model portDrum = mb.createCylinder(5.4f, 2.4f, 5.4f, 24, curzonBrickMat, attr);
        Model drumBand = mb.createCylinder(5.8f, 0.36f, 5.8f, 24, curzonTrimMat, attr);
        Model domeCollar = mb.createCylinder(6.0f, 0.28f, 6.0f, 24, curzonTrimMat, attr);
        Model domeBaseRing = mb.createCylinder(5.72f, 0.22f, 5.72f, 24, curzonTrimMat, attr);
        Model portDome = mb.createSphere(5.6f, 4.8f, 5.6f, 24, 20, curzonWhiteDome, attr);
        models.add(finialBase);
        models.add(smallFinial);
        models.add(portDrum);
        models.add(drumBand);
        models.add(domeCollar);
        models.add(domeBaseRing);
        models.add(portDome);

        ModelInstance drumInst = new ModelInstance(portDrum);
        drumInst.transform.setTranslation(0f, 16.7f, -22.5f);
        instances.add(drumInst);
        ModelInstance drumBandInst = new ModelInstance(drumBand);
        drumBandInst.transform.setTranslation(0f, 17.85f, -22.5f);
        instances.add(drumBandInst);
        ModelInstance collarInst = new ModelInstance(domeCollar);
        collarInst.transform.setTranslation(0f, 18.12f, -22.5f);
        instances.add(collarInst);
        ModelInstance baseRingInst = new ModelInstance(domeBaseRing);
        baseRingInst.transform.setTranslation(0f, 18.34f, -22.5f);
        instances.add(baseRingInst);
        ModelInstance portDomeInst = new ModelInstance(portDome);
        portDomeInst.transform.setTranslation(0f, 18.55f, -22.5f);
        instances.add(portDomeInst);
        ModelInstance portFinial = new ModelInstance(finialBase);
        portFinial.transform.setTranslation(0f, 22.15f, -22.5f);
        instances.add(portFinial);

        // Flanking octagonal turrets with cream bands and little red domes
        Model turretShaft = mb.createCylinder(1.5f, 18.0f, 1.5f, 8, curzonBrickMat, attr);
        Model turretBand = mb.createCylinder(1.75f, 0.3f, 1.75f, 8, curzonTrimMat, attr);
        Model turretDome = mb.createSphere(1.9f, 1.7f, 1.9f, 14, 10, curzonRedDome, attr);
        models.add(turretShaft);
        models.add(turretBand);
        models.add(turretDome);
        for (float side = -1f; side <= 1f; side += 2f) {
            float tx = side * 7.9f, tz = -20.8f;
            ModelInstance shaft = new ModelInstance(turretShaft);
            shaft.transform.setTranslation(tx, 9.0f, tz);
            instances.add(shaft);
            for (float by : new float[]{6.1f, 12.2f, 15.8f, 18.0f}) {
                ModelInstance band = new ModelInstance(turretBand);
                band.transform.setTranslation(tx, by, tz);
                instances.add(band);
            }
            ModelInstance td = new ModelInstance(turretDome);
            td.transform.setTranslation(tx, 18.7f, tz);
            instances.add(td);
            ModelInstance tf = new ModelInstance(smallFinial);
            tf.transform.setTranslation(tx, 20.0f, tz);
            instances.add(tf);
        }

        // Bangladesh National Flag on a pole rising from the portico roof behind the dome
        Model flagPole = mb.createCylinder(0.12f, 7.5f, 0.12f, 8, castIron, attr);
        Model flagModel = mb.createBox(3.4f, 2.04f, 0.04f, flagMat, attr);
        models.add(flagPole);
        models.add(flagModel);

        ModelInstance poleInst = new ModelInstance(flagPole);
        poleInst.transform.setTranslation(0f, 17.35f, -26.5f);
        instances.add(poleInst);

        ModelInstance flagInst = new ModelInstance(flagModel);
        flagInst.transform.setTranslation(1.7f, 20.0f, -26.5f);
        instances.add(flagInst);

        // Authentic Mughal Chhatris along the parapet: 4 pillars, molded capitals, flared chhajja eaves, octagonal drums and bulbous red domes
        Model chhatriDome = mb.createSphere(2.30f, 1.85f, 2.30f, 18, 14, curzonRedDome, attr);
        Model chhatriPillar = mb.createBox(0.28f, 2.2f, 0.28f, curzonTrimMat, attr);
        Model chhatriCapital = mb.createBox(0.40f, 0.16f, 0.40f, curzonTrimMat, attr);
        Model chhatriChhajja = mb.createBox(2.85f, 0.14f, 2.85f, curzonTrimMat, attr);
        Model chhatriDrum = mb.createCylinder(2.20f, 0.24f, 2.20f, 8, curzonTrimMat, attr);
        models.add(chhatriDome);
        models.add(chhatriPillar);
        models.add(chhatriCapital);
        models.add(chhatriChhajja);
        models.add(chhatriDrum);

        float[][] chhatriPos = {                       // {x, parapet top y}
            {-18.5f, 14f}, {18.5f, 14f},               // corners of the taller central block
            {-12.5f, 14f}, {12.5f, 14f},
            {-26f, 11.5f}, {26f, 11.5f}, {-36f, 11.5f}, {36f, 11.5f},
            {-45f, 11.5f}, {45f, 11.5f}                // wing ends
        };
        for (float[] c : chhatriPos) {
            float cx = c[0], baseY = c[1], cz = -24.2f;
            for (float px = -0.8f; px <= 0.8f; px += 1.6f) {
                for (float pz = -0.8f; pz <= 0.8f; pz += 1.6f) {
                    ModelInstance cp = new ModelInstance(chhatriPillar);
                    cp.transform.setTranslation(cx + px, baseY + 1.1f, cz + pz);
                    instances.add(cp);
                    nonSolid.add(cp);

                    ModelInstance ccap = new ModelInstance(chhatriCapital);
                    ccap.transform.setTranslation(cx + px, baseY + 2.28f, cz + pz);
                    instances.add(ccap);
                    nonSolid.add(ccap);
                }
            }
            // Flared projecting chhajja sunshade eaves slab
            ModelInstance chhatriChhajjaInst = new ModelInstance(chhatriChhajja);
            chhatriChhajjaInst.transform.setTranslation(cx, baseY + 2.38f, cz);
            instances.add(chhatriChhajjaInst);
            nonSolid.add(chhatriChhajjaInst);

            // Octagonal masonry drum beneath cupola
            ModelInstance chhatriDrumInst = new ModelInstance(chhatriDrum);
            chhatriDrumInst.transform.setTranslation(cx, baseY + 2.55f, cz);
            instances.add(chhatriDrumInst);
            nonSolid.add(chhatriDrumInst);

            // Mughal bulbous cupola dome
            ModelInstance cd = new ModelInstance(chhatriDome);
            cd.transform.setTranslation(cx, baseY + 3.10f, cz);
            instances.add(cd);
            nonSolid.add(cd);

            // Golden lotus finial atop cupola
            ModelInstance cf = new ModelInstance(smallFinial);
            cf.transform.setTranslation(cx, baseY + 4.25f, cz);
            instances.add(cf);
            nonSolid.add(cf);
        }

        // 4. REALISTIC DHAKA UNIVERSITY CAMPUS VEGETATION & LANDMARKS
        ModelBuilder mbBush = new ModelBuilder();
        mbBush.begin();
        MeshPartBuilder mpbBushes = mbBush.part("bushes", GL20.GL_TRIANGLES, attr, bushMat);
        ModelBuilder[] foliageBuilders = {mbBush};

        // Authentic procedural trees (Rain Tree, Krishnachura, White Blossom, Royal Palm)
        // built in local space with flared buttress roots into turf (y <= 0), spherical normal blending, and distance LOD
        vegetationManager.buildCampusTrees(
            TREE_LOCATIONS,
            barkTwoSidedMat,
            leafClumpMat,
            leafCardMat,
            blossomClumpMat,
            whiteBlossomClumpMat,
            whiteBlossomCardMat,
            foliageMat,
            colliders
        );

        // ==========================================
        // GLADES & UNDER-TREE GROUND COVER:
        // Dense bamboo, volumetric 3D grass tufts, leafy ground cover and worn dirt paths
        // ==========================================
        FoliageBatch caneBatch = new FoliageBatch("bambooCanes", bambooBarkMat, attr);
        FoliageBatch sprayBatch = new FoliageBatch("bambooSprays", bambooLeafCardMat, attr);
        FoliageBatch plantBatch = new FoliageBatch("groundPlants", leafCardMat, attr);
        FoliageBatch rockBatch = new FoliageBatch("rocks", rockMat, attr);
        FoliageBatch mossBatch = new FoliageBatch("moss", leafClumpMat, attr);
        FoliageBatch pathBatch = new FoliageBatch("dirtPaths", dirtMat, attr);
        FoliageBatch grassTuftBatch = new FoliageBatch("campusGrassTufts", grassTuftMat, attr);
        FoliageBatch[] gladeBatches = {caneBatch, sprayBatch, plantBatch, rockBatch, mossBatch, pathBatch, grassTuftBatch};

        // Volumetric 3D grass tufts around campus tree roots (matching Ref 2 under-tree turf)
        for (float[] loc : TREE_LOCATIONS) {
            float tx = loc[0], tz = loc[1];
            float ts = (loc.length > 3) ? loc[3] : 1.0f;
            long seed = (long) (Math.abs(tx * 31.0f + tz * 17.0f) * 100.0f);
            java.util.Random r = new java.util.Random(seed);
            int tufts = 16;
            for (int i = 0; i < tufts; i++) {
                float ang = r.nextFloat() * MathUtils.PI2;
                float dist = (0.55f + 1.65f * r.nextFloat()) * ts;
                float gx = tx + MathUtils.cos(ang) * dist;
                float gz = tz + MathUtils.sin(ang) * dist;
                float gw = (0.45f + 0.35f * r.nextFloat()) * ts;
                float gh = (0.35f + 0.30f * r.nextFloat()) * ts;
                GladeGeometry.grassTuft(grassTuftBatch.part(100), gx, 0f, gz, gw, gh, seed + i * 23L);
            }
        }
        TreeGeometry.leafDensity = 1f;

        // Surface transitions (Phase 4): organic tree root soil collars, walkway dirt shoulders, pond verge
        terrainSurface.buildSurfaceTransitions(
            mb,
            textures,
            TREE_LOCATIONS,
            GraphicsTier.fromSystemProperty(),
            instances,
            nonSolid,
            shadowExclusions
        );

        java.util.Random gladeRng = new java.util.Random(4242L);

        // Bamboo stands and Dense_Realistic_Bamboo_Clump groves:
        // {minX, maxX, minZ, maxZ}
        float[][] bambooBands = {
            {13f, 32f, 33.5f, 38.5f}, {13f, 32f, 42.5f, 45.5f},
            {-32f, -13f, 33.5f, 38.5f}, {-32f, -13f, 42.5f, 45.5f}
        };
        long caneSeed = 9000L;
        for (float[] band : bambooBands) {
            float bandArea = (band[1] - band[0]) * (band[3] - band[2]);
            int canes = Math.round(bandArea * 0.38f);
            for (int c = 0; c < canes; c++) {
                float cx = band[0] + gladeRng.nextFloat() * (band[1] - band[0]);
                float cz = band[2] + gladeRng.nextFloat() * (band[3] - band[2]);
                // Botanical spec: 4.5m to 10.0m (mostly 6.0m to 8.5m), radii 0.018m to 0.045m
                float height = 6.2f + gladeRng.nextFloat() * 2.8f;
                float lean = 0.35f + gladeRng.nextFloat() * 1.2f;
                float leanAngle = gladeRng.nextFloat() * MathUtils.PI2;
                float radius = 0.020f + gladeRng.nextFloat() * 0.022f;
                GladeGeometry.bambooCane(caneBatch.part(1800), sprayBatch.part(1800),
                    cx, cz, height, lean, leanAngle, radius, caneSeed++);
            }
            colliders.add(new float[]{band[0], 0f, band[2], band[1], 12f, band[3]});
        }

        // Dedicated Dense_Realistic_Bamboo_Clump groves (5x5m footprint, 32 culms each, spec-compliant)
        float[][] clumpCenters = {
            {18f, 36f}, {26f, 36f}, {18f, 44f}, {26f, 44f},
            {-18f, 36f}, {-26f, 36f}, {-18f, 44f}, {-26f, 44f},
            {13.5f, 15f}, {-13.5f, 15f} // Curzon pondside garden groves
        };
        long clumpSeed = 9200L;
        for (float[] cc : clumpCenters) {
            GladeGeometry.denseBambooClump(caneBatch, sprayBatch,
                cc[0], cc[1], clumpSeed += 120L);
            colliders.add(new float[]{cc[0] - 1.6f, 0f, cc[1] - 1.6f, cc[0] + 1.6f, 10f, cc[1] + 1.6f});
        }

        // Ground cover: lush low plants either side of the paths (kept off the path itself)
        for (float side : new float[]{1f, -1f}) {
            for (int p = 0; p < 1500; p++) {
                float px = side * (7.4f + gladeRng.nextFloat() * 26.6f);
                float pz = 33.2f + gladeRng.nextFloat() * 12.4f;
                float pathZ = 40.5f + 0.7f * MathUtils.sin(Math.abs(px) * 0.35f);
                if (Math.abs(pz - pathZ) < 1.3f) continue;
                float size = 0.17f + gladeRng.nextFloat() * 0.15f;
                GladeGeometry.plant(plantBatch.part(90), px, pz, size, 6 + gladeRng.nextInt(4),
                    500L + p * 7L + (side > 0f ? 0L : 3000L));
            }
        }

        // Worn dirt paths: one each way from the promenade, plus a short spur to the Boundary Stone
        for (float side : new float[]{1f, -1f}) {
            float[][] pts = new float[14][2];
            for (int i = 0; i < pts.length; i++) {
                float ax = 6.8f + i * 2.1f;
                pts[i][0] = side * ax;
                pts[i][1] = 40.5f + 0.7f * MathUtils.sin(ax * 0.35f);
            }
            GladeGeometry.ribbon(pathBatch.part(200), pts, 0.95f, 0.02f, 2.6f);
        }
        GladeGeometry.ribbon(pathBatch.part(200),
            new float[][]{{6.9f, 40.3f}, {8.2f, 39.2f}, {9.2f, 38.0f}}, 0.7f, 0.021f, 2.6f);

        // Artificial light shafts removed: campus glade lit by natural daylight without fake beams
        // ==========================================
        // HISTORICAL BOUNDARY STONE (1921) & MOSSY BOULDERS
        // (Directly echoing [RT] Boundary Stone from reference image)
        // ==========================================
        float bsX = BOUNDARY_STONE_POS.x, bsZ = BOUNDARY_STONE_POS.z;

        // Flat granite plinth foundation
        Model stonePlinth = mb.createBox(1.5f, 0.32f, 1.2f, weatheredStoneMat, attr);
        models.add(stonePlinth);
        ModelInstance plinthInst = new ModelInstance(stonePlinth);
        plinthInst.transform.setTranslation(bsX, 0.16f, bsZ);
        instances.add(plinthInst);

        // The Boundary Stone itself: a tall, weathered standing stone with a moss cap, in place of
        // the old box-and-cone monolith (its collision box is hand-authored in PlayerController).
        GladeGeometry.rock(rockBatch.part(500), mossBatch.part(500), bsX, bsZ, 0.55f, 1.15f, 0.42f, 8f);

        // Surrounding weathered, moss-capped boulders, plus more rocks along the glade paths
        float[][] boulders = {
            {bsX + 1.8f, bsZ - 0.8f, 1.15f, 0.80f, 0.95f},
            {bsX - 1.5f, bsZ + 1.1f, 0.80f, 0.55f, 0.70f},
            {bsX + 1.4f, bsZ + 1.6f, 1.05f, 0.75f, 0.85f},
            {bsX + 3.0f, bsZ - 2.8f, 1.15f, 0.80f, 0.95f},
            {16.5f, 39.4f, 0.70f, 0.45f, 0.55f}, {25.5f, 41.6f, 0.90f, 0.60f, 0.70f},
            {31.0f, 39.0f, 0.60f, 0.40f, 0.50f}, {-16.5f, 41.4f, 0.75f, 0.50f, 0.60f},
            {-24.5f, 39.2f, 0.95f, 0.65f, 0.75f}, {-30.5f, 41.5f, 0.60f, 0.40f, 0.50f}
        };
        float rockSeed = 1f;
        for (int r = 0; r < boulders.length; r++) {
            float[] b = boulders[r];
            GladeGeometry.rock(rockBatch.part(500), mossBatch.part(500), b[0], b[1], b[2], b[3], b[4], rockSeed += 1.7f);
            if (r >= 4) {
                // The first four have hand-authored collision boxes; the path rocks get theirs here
                colliders.add(new float[]{b[0] - b[2] * 0.85f, 0f, b[1] - b[4] * 0.85f,
                    b[0] + b[2] * 0.85f, b[3] * 1.1f, b[1] + b[4] * 0.85f});
            }
            // Shrub nestled beside the boulder
            addCrossedQuads(mpbBushes, b[0] + 0.5f, 0f, b[1] + 0.5f, 1.6f, 1.1f, 3, 0f);
        }

        // Sparkling aura particles orbiting Boundary Stone (matching reference image)
        Model sparkleParticle = mb.createSphere(0.09f, 0.09f, 0.09f, 6, 6, sparkleMat, attr);
        models.add(sparkleParticle);
        for (int i = 0; i < 6; i++) {
            ModelInstance sp = new ModelInstance(sparkleParticle);
            sp.transform.setTranslation(bsX, 1.4f, bsZ);
            boundarySparkles.add(sp);
            instances.add(sp);
        }

        // ==========================================
        // CURZON HALL PUKUR (Historic Campus Reflection Pond)
        // ==========================================
        float pukurX = 0f, pukurZ = 6f;
        float pukurW = 18.0f, pukurL = 24.0f;

        // Perimeter stone curb border
        Model pukurCurbX = mb.createBox(pukurW + 0.8f, 0.45f, 0.6f, stoneCurb, attr);
        Model pukurCurbZ = mb.createBox(0.6f, 0.45f, pukurL + 0.8f, stoneCurb, attr);
        models.add(pukurCurbX);
        models.add(pukurCurbZ);

        ModelInstance pcN = new ModelInstance(pukurCurbX);
        pcN.transform.setTranslation(pukurX, 0.22f, pukurZ - pukurL * 0.5f);
        instances.add(pcN);

        ModelInstance pcS = new ModelInstance(pukurCurbX);
        pcS.transform.setTranslation(pukurX, 0.22f, pukurZ + pukurL * 0.5f);
        instances.add(pcS);

        ModelInstance pcW = new ModelInstance(pukurCurbZ);
        pcW.transform.setTranslation(pukurX - pukurW * 0.5f, 0.22f, pukurZ);
        instances.add(pcW);

        ModelInstance pcE = new ModelInstance(pukurCurbZ);
        pcE.transform.setTranslation(pukurX + pukurW * 0.5f, 0.22f, pukurZ);
        instances.add(pcE);

        // Dark silty pond bed just above the ground plane, so fish read against it through the water
        Material pondBedMat = new Material(ColorAttribute.createDiffuse(new Color(0.07f, 0.16f, 0.17f, 1f)));
        setupPbr(pondBedMat, 0.0f, 0.95f);
        Model pondBed = mb.createBox(pukurW - 0.2f, 0.02f, pukurL - 0.2f, pondBedMat, attr);
        models.add(pondBed);
        ModelInstance pondBedInst = new ModelInstance(pondBed);
        pondBedInst.transform.setTranslation(pukurX, 0.01f, pukurZ);
        instances.add(pondBedInst);

        // Realistic Water Surface & Lily-Pad Buoyancy (Phase 2)
        // Note: The water surface plane is rendered via dedicated WaterRenderer with Gerstner wave displacement
        // and dual-scrolling ripple normals.

        // Floating Lotus Pads (Water Lilies) & Sacred Lotus Blossoms matching media_1791358203326.png
        Material lotusMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.lotusAtlas),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f),
            FloatAttribute.createAlphaTest(0.35f),
            new DepthTestAttribute(GL20.GL_LEQUAL, false),
            new IntAttribute(IntAttribute.CullFace, 0)
        );
        setupPbr(lotusMat, 0.0f, 0.65f);

        // Build 4 unit-sized (radius 1.0) reusable lotus models for atlas cells
        Model[] padVariants = new Model[4];
        for (int v = 0; v < 4; v++) {
            int col = (v == 1 || v == 2) ? 1 : 0;
            int row = (v >= 2) ? 1 : 0;
            float texU0 = col * 0.5f, texV0 = row * 0.5f;
            float texU1 = texU0 + 0.5f, texV1 = texV0 + 0.5f;

            mb.begin();
            MeshPartBuilder pb = mb.part("pad_" + v, GL20.GL_TRIANGLES, attr, lotusMat);
            // Unit quad at origin [-1, 1] in X and Z
            VertexInfo vert0 = new VertexInfo().setPos(-1f, 0f, -1f).setNor(0f, 1f, 0f).setUV(texU0, texV1);
            VertexInfo vert1 = new VertexInfo().setPos( 1f, 0f, -1f).setNor(0f, 1f, 0f).setUV(texU1, texV1);
            VertexInfo vert2 = new VertexInfo().setPos( 1f, 0f,  1f).setNor(0f, 1f, 0f).setUV(texU1, texV0);
            VertexInfo vert3 = new VertexInfo().setPos(-1f, 0f,  1f).setNor(0f, 1f, 0f).setUV(texU0, texV0);
            pb.rect(vert0, vert1, vert2, vert3);

            // Variant 3 is blooming sacred lotus (col=0, row=1): add upright petal cross-quads
            if (col == 0 && row == 1) {
                float fRad = 0.85f;
                float fH = 0.38f;
                VertexInfo fv0 = new VertexInfo().setPos(-fRad, 0f, 0f).setNor(0f, 1f, 0f).setUV(texU0, texV1);
                VertexInfo fv1 = new VertexInfo().setPos( fRad, 0f, 0f).setNor(0f, 1f, 0f).setUV(texU1, texV1);
                VertexInfo fv2 = new VertexInfo().setPos( fRad, fH, 0f).setNor(0f, 1f, 0f).setUV(texU1, texV0);
                VertexInfo fv3 = new VertexInfo().setPos(-fRad, fH, 0f).setNor(0f, 1f, 0f).setUV(texU0, texV0);
                pb.rect(fv0, fv1, fv2, fv3);

                VertexInfo fz0 = new VertexInfo().setPos(0f, 0f, -fRad).setNor(0f, 1f, 0f).setUV(texU0, texV1);
                VertexInfo fz1 = new VertexInfo().setPos(0f, 0f,  fRad).setNor(0f, 1f, 0f).setUV(texU1, texV1);
                VertexInfo fz2 = new VertexInfo().setPos(0f, fH,  fRad).setNor(0f, 1f, 0f).setUV(texU1, texV0);
                VertexInfo fz3 = new VertexInfo().setPos(0f, fH, -fRad).setNor(0f, 1f, 0f).setUV(texU0, texV0);
                pb.rect(fz0, fz1, fz2, fz3);
            }

            padVariants[v] = mb.end();
            padModels.add(padVariants[v]);
            models.add(padVariants[v]);
        }

        float[][] lotusPlacements = {
            // North-West calm bay (world X in [-7.2, -3.6], world Z in [-3.8, -0.5])
            {-6.8f, -9.5f, 0.95f, 0, 0}, {-5.5f, -9.8f, 1.15f, 0, 1}, {-4.2f, -9.0f, 0.85f, 1, 0},
            {-6.2f, -8.2f, 1.30f, 1, 1}, {-5.0f, -7.8f, 0.90f, 0, 0}, {-3.6f, -8.5f, 1.05f, 0, 1},
            {-7.2f, -7.0f, 1.10f, 1, 0}, {-5.8f, -6.5f, 0.85f, 1, 1}, {-4.5f, -6.0f, 1.20f, 0, 0},

            // North-East calm bay (world X in [3.5, 7.0], world Z in [-3.8, -0.5])
            {4.0f, -9.5f, 0.90f, 1, 0}, {5.6f, -9.8f, 1.10f, 0, 1}, {6.8f, -9.2f, 1.25f, 0, 0},
            {3.5f, -8.2f, 1.05f, 1, 1}, {5.0f, -7.8f, 1.20f, 0, 0}, {6.4f, -8.0f, 0.95f, 0, 1},
            {4.2f, -6.5f, 0.85f, 0, 0}, {5.8f, -6.8f, 1.15f, 1, 0}, {7.0f, -7.0f, 1.00f, 1, 1},

            // West bank fringes (world X in [-7.5, -5.8], world Z in [1.5, 10.8])
            {-7.5f, -4.5f, 1.20f, 0, 0}, {-6.4f, -3.5f, 0.90f, 0, 1}, {-7.2f, -2.0f, 1.10f, 1, 0},
            {-6.0f, -0.8f, 1.30f, 1, 1}, {-7.4f,  0.8f, 0.95f, 0, 0}, {-6.2f,  2.2f, 1.15f, 0, 1},
            {-7.0f,  3.5f, 1.05f, 1, 0}, {-5.8f,  4.8f, 1.25f, 1, 1},

            // East bank fringes (world X in [6.0, 7.5], world Z in [1.5, 10.8])
            {7.2f, -4.5f, 1.15f, 1, 0}, {6.2f, -3.5f, 0.85f, 0, 1}, {7.4f, -2.0f, 1.25f, 0, 0},
            {6.0f, -0.8f, 1.00f, 1, 1}, {7.2f,  0.8f, 1.20f, 0, 0}, {6.4f,  2.2f, 0.90f, 0, 1},
            {7.5f,  3.5f, 1.10f, 1, 0}, {6.1f,  4.8f, 1.25f, 1, 1},

            // South-West corner (away from ghat, world Z in [11.8, 15.5])
            {-6.8f, 5.8f, 1.20f, 0, 0}, {-5.4f, 6.8f, 0.95f, 0, 1}, {-6.5f, 7.8f, 1.15f, 1, 0},
            {-5.0f, 8.8f, 1.05f, 1, 1}, {-6.2f, 9.5f, 0.85f, 0, 0},

            // South-East corner (away from ghat, world Z in [11.8, 15.5])
            {6.8f, 5.8f, 1.10f, 0, 0}, {5.5f, 6.8f, 1.25f, 0, 1}, {6.6f, 7.8f, 0.90f, 1, 0},
            {5.2f, 8.8f, 1.15f, 1, 1}, {6.4f, 9.5f, 1.00f, 0, 0}
        };

        for (int i = 0; i < lotusPlacements.length; i++) {
            float[] lp = lotusPlacements[i];
            float lx = pukurX + lp[0];
            float lz = pukurZ + lp[1];
            float rad = lp[2] * 0.65f;
            int col = (int) lp[3];
            int row = (int) lp[4];
            int varIdx = (row == 1 && col == 0) ? 3 : (col == 1 && row == 1 ? 2 : (col == 1 ? 1 : 0));

            ModelInstance inst = new ModelInstance(padVariants[varIdx]);
            float yaw = (i * 1.37f + 0.5f) * MathUtils.PI;
            LilyPad pad = new LilyPad(lx, lz, rad, yaw, col, row, inst);
            lilyPads.add(pad);
        }

        // Curzon Hall Pukur Grand Ghat Steps (South Bank Entrance)
        float ghatZ = pukurZ + pukurL * 0.5f; // 18.0f
        Model ghatStepUpper = mb.createBox(7.2f, 0.32f, 1.4f, stoneCurb, attr);
        Model ghatStepMid = mb.createBox(6.6f, 0.20f, 1.2f, curzonBrickMat, attr);
        Model ghatStepWater = mb.createBox(6.2f, 0.12f, 1.0f, stoneCurb, attr);
        models.add(ghatStepUpper);
        models.add(ghatStepMid);
        models.add(ghatStepWater);

        ModelInstance gsu = new ModelInstance(ghatStepUpper);
        gsu.transform.setTranslation(pukurX, 0.28f, ghatZ + 0.5f);
        instances.add(gsu);

        ModelInstance gsm = new ModelInstance(ghatStepMid);
        gsm.transform.setTranslation(pukurX, 0.18f, ghatZ - 0.4f);
        instances.add(gsm);

        ModelInstance gsw = new ModelInstance(ghatStepWater);
        gsw.transform.setTranslation(pukurX, 0.08f, ghatZ - 1.2f);
        instances.add(gsw);

        // Flanking ornamental stone bollards at the ghat
        Model ghatBollard = mb.createCylinder(0.36f, 0.70f, 0.36f, 12, stoneCurb, attr);
        models.add(ghatBollard);
        ModelInstance gbL = new ModelInstance(ghatBollard);
        gbL.transform.setTranslation(pukurX - 3.8f, 0.45f, ghatZ + 0.5f);
        instances.add(gbL);
        ModelInstance gbR = new ModelInstance(ghatBollard);
        gbR.transform.setTranslation(pukurX + 3.8f, 0.45f, ghatZ + 0.5f);
        instances.add(gbR);

        // Curzon Pond Swimming Fishes (Colorful Bengali Rohu, Koi & Golden Carp)
        buildPondFishes(mb, pukurX, pukurZ, pukurW, pukurL, attr);


        // End foliage batches & register models
        for (ModelBuilder builder : foliageBuilders) {
            registerFoliage(builder);
        }
        for (FoliageBatch batch : gladeBatches) {
            batch.close();
        }

        // 5. APARAJEYO BANGLA (অপরাজেয় বাংলা — Iconic 3-Student Sculpture)
        // Positioned on south-west campus lawn at (-36, 0, 26)
        float aX = -36.0f, aZ = 26.0f;
        Model apStep1 = mb.createBox(6.2f, 0.4f, 6.2f, stoneCurb, attr);
        Model apStep2 = mb.createBox(5.0f, 0.5f, 5.0f, stoneCurb, attr);
        Model apPedestal = mb.createBox(3.8f, 0.9f, 3.8f, aparajeyoMat, attr);
        models.add(apStep1);
        models.add(apStep2);
        models.add(apPedestal);

        ModelInstance as1 = new ModelInstance(apStep1);
        as1.transform.setTranslation(aX, 0.2f, aZ);
        instances.add(as1);

        ModelInstance as2 = new ModelInstance(apStep2);
        as2.transform.setTranslation(aX, 0.65f, aZ);
        instances.add(as2);

        ModelInstance apP = new ModelInstance(apPedestal);
        apP.transform.setTranslation(aX, 1.35f, aZ);
        instances.add(apP);

        // Three Heroic Student Statues
        Model statTorso = mb.createBox(0.68f, 2.2f, 0.50f, aparajeyoMat, attr);
        Model statHead = mb.createSphere(0.40f, 0.48f, 0.40f, 10, 8, aparajeyoMat, attr);
        Model statRifle = mb.createBox(0.12f, 1.9f, 0.12f, castIron, attr);
        models.add(statTorso);
        models.add(statHead);
        models.add(statRifle);

        // Central Student Freedom Fighter
        ModelInstance scTorso = new ModelInstance(statTorso);
        scTorso.transform.setTranslation(aX, 2.9f, aZ);
        instances.add(scTorso);

        ModelInstance scHead = new ModelInstance(statHead);
        scHead.transform.setTranslation(aX, 4.2f, aZ);
        instances.add(scHead);

        ModelInstance scRifle = new ModelInstance(statRifle);
        scRifle.transform.setTranslation(aX + 0.38f, 3.3f, aZ - 0.2f);
        scRifle.transform.rotate(Vector3.Z, -15f);
        instances.add(scRifle);

        // Left Student (Female Medic Volunteer)
        ModelInstance slTorso = new ModelInstance(statTorso);
        slTorso.transform.setTranslation(aX - 0.9f, 2.75f, aZ - 0.1f);
        instances.add(slTorso);

        ModelInstance slHead = new ModelInstance(statHead);
        slHead.transform.setTranslation(aX - 0.9f, 4.05f, aZ - 0.1f);
        instances.add(slHead);

        // Right Student (Youth Movement Activist)
        ModelInstance srTorso = new ModelInstance(statTorso);
        srTorso.transform.setTranslation(aX + 0.9f, 2.85f, aZ + 0.1f);
        instances.add(srTorso);

        ModelInstance srHead = new ModelInstance(statHead);
        srHead.transform.setTranslation(aX + 0.9f, 4.15f, aZ + 0.1f);
        instances.add(srHead);

        // ==========================================
        // 6. SOUTH BOUNDARY WALL & MAIN GATE (DOEL CHATTAR ENTRANCE)
        // ==========================================
        // West Boundary Wall (from X=-110 to X=-4.5 at Z=78)
        Model boundWallW = mb.createBox(105f, 2.6f, 0.6f, curzonBrickMat, attr);
        Model boundWallE = mb.createBox(105f, 2.6f, 0.6f, curzonBrickMat, attr);
        Model boundCopingW = mb.createBox(105f, 0.25f, 0.85f, stoneCurb, attr);
        Model boundCopingE = mb.createBox(105f, 0.25f, 0.85f, stoneCurb, attr);
        models.add(boundWallW);
        models.add(boundWallE);
        models.add(boundCopingW);
        models.add(boundCopingE);

        ModelInstance bwwInst = new ModelInstance(boundWallW);
        bwwInst.transform.setTranslation(-57f, 1.3f, 78f);
        instances.add(bwwInst);

        ModelInstance bweInst = new ModelInstance(boundWallE);
        bweInst.transform.setTranslation(57f, 1.3f, 78f);
        instances.add(bweInst);

        ModelInstance bcwInst = new ModelInstance(boundCopingW);
        bcwInst.transform.setTranslation(-57f, 2.7f, 78f);
        instances.add(bcwInst);

        ModelInstance bceInst = new ModelInstance(boundCopingE);
        bceInst.transform.setTranslation(57f, 2.7f, 78f);
        instances.add(bceInst);

        // Brick wall piers with stone caps every 14m along south wall
        Model wallPier = mb.createBox(0.9f, 3.0f, 0.9f, curzonBrickMat, attr);
        Model pierCap = mb.createBox(1.1f, 0.25f, 1.1f, stoneCurb, attr);
        models.add(wallPier);
        models.add(pierCap);

        for (float px = -105f; px <= 105f; px += 14f) {
            if (Math.abs(px) < 6f) continue;
            ModelInstance wp = new ModelInstance(wallPier);
            wp.transform.setTranslation(px, 1.5f, 78f);
            instances.add(wp);

            ModelInstance cap = new ModelInstance(pierCap);
            cap.transform.setTranslation(px, 3.1f, 78f);
            instances.add(cap);
        }

        // MAIN GATE (Doel Chattar Grand Mughal Gateway at X=0, Z=78)
        Model gateArchPillar = mb.createBox(1.8f, 7.2f, 1.8f, curzonBrickMat, attr);
        Model gateArchBeam = mb.createBox(8.4f, 1.6f, 2.0f, curzonBrickMat, attr);
        Model gateArchCrest = mb.createBox(6.4f, 1.2f, 0.4f, curzonTrimMat, attr);
        Model gateTrimFrame = mb.createBox(4.8f, 0.3f, 2.05f, curzonTrimMat, attr);
        Model gateTrimJamb = mb.createBox(0.16f, 5.6f, 2.05f, curzonTrimMat, attr);
        Model gateLeafRail = mb.createBox(0.07f, 0.08f, 2.30f, castIron, attr);
        Model gateLeafBar = mb.createCylinder(0.045f, 3.2f, 0.045f, 6, castIron, attr);
        Model sidePedArch = mb.createBox(2.4f, 4.4f, 1.6f, curzonBrickMat, attr);
        Model guardKiosk = mb.createBox(2.8f, 3.2f, 2.8f, curzonBrickMat, attr);
        Model guardRoof = mb.createBox(3.4f, 0.4f, 3.4f, curzonTrimMat, attr);
        models.add(gateArchPillar);
        models.add(gateArchBeam);
        models.add(gateArchCrest);
        models.add(gateTrimFrame);
        models.add(gateTrimJamb);
        models.add(gateLeafRail);
        models.add(gateLeafBar);
        models.add(sidePedArch);
        models.add(guardKiosk);
        models.add(guardRoof);

        // Main gate left and right pillars
        ModelInstance gpL = new ModelInstance(gateArchPillar);
        gpL.transform.setTranslation(-3.3f, 3.6f, 78f);
        instances.add(gpL);

        ModelInstance gpR = new ModelInstance(gateArchPillar);
        gpR.transform.setTranslation(3.3f, 3.6f, 78f);
        instances.add(gpR);

        // Central arch beam spanning across
        ModelInstance gbInst = new ModelInstance(gateArchBeam);
        gbInst.transform.setTranslation(0f, 6.4f, 78f);
        instances.add(gbInst);

        ModelInstance gcInst = new ModelInstance(gateArchCrest);
        gcInst.transform.setTranslation(0f, 7.8f, 78f);
        instances.add(gcInst);

        // Open archway: trim only frames the opening (jambs + lintel) so the gateway stays clear
        ModelInstance gtInst = new ModelInstance(gateTrimFrame);
        gtInst.transform.setTranslation(0f, 5.45f, 78f);
        instances.add(gtInst);
        for (float side = -1f; side <= 1f; side += 2f) {
            ModelInstance jamb = new ModelInstance(gateTrimJamb);
            jamb.transform.setTranslation(side * 2.28f, 2.8f, 78f);
            instances.add(jamb);
        }

        // Wrought-iron gate leaves swung fully open, folded back along the inside of each jamb
        for (float side = -1f; side <= 1f; side += 2f) {
            float lx = side * 2.30f;
            for (int r = 0; r < 3; r++) {
                ModelInstance rail = new ModelInstance(gateLeafRail);
                rail.transform.setTranslation(lx, 0.25f + r * 1.45f, 78f - 0.9f - 1.15f);
                instances.add(rail);
            }
            for (int b = 0; b < 9; b++) {
                ModelInstance bar = new ModelInstance(gateLeafBar);
                bar.transform.setTranslation(lx, 1.6f, 78f - 0.95f - b * 0.27f);
                instances.add(bar);
            }
        }

        // Finials atop gate pillars
        ModelInstance gfL = new ModelInstance(finialBase);
        gfL.transform.setTranslation(-3.3f, 7.6f, 78f);
        instances.add(gfL);

        ModelInstance gfR = new ModelInstance(finialBase);
        gfR.transform.setTranslation(3.3f, 7.6f, 78f);
        instances.add(gfR);

        // Flanking pedestrian gates (left and right)
        ModelInstance spL = new ModelInstance(sidePedArch);
        spL.transform.setTranslation(-5.4f, 2.2f, 78f);
        instances.add(spL);

        ModelInstance spR = new ModelInstance(sidePedArch);
        spR.transform.setTranslation(5.4f, 2.2f, 78f);
        instances.add(spR);

        // Security Guard Kiosk at East flank of gate
        ModelInstance gkInst = new ModelInstance(guardKiosk);
        gkInst.transform.setTranslation(8.5f, 1.6f, 75.5f);
        instances.add(gkInst);

        ModelInstance grInst = new ModelInstance(guardRoof);
        grInst.transform.setTranslation(8.5f, 3.4f, 75.5f);
        instances.add(grInst);

        // Doel Chattar Road Corridor (Asphalt East-West Avenue outside the gate)
        Model asphaltRoadSouth = createPavedSlab(mb, 240f, 0.04f, 12f, 1.0f, GroundConfig.ASPHALT_UV_SCALE, asphaltMat, attr);
        Model roadCurbTile = mb.createBox(240f, 0.25f, 0.45f, stoneCurb, attr);
        models.add(asphaltRoadSouth);
        models.add(roadCurbTile);

        ModelInstance arsInst = new ModelInstance(asphaltRoadSouth);
        arsInst.transform.setTranslation(0f, 0.02f, 85f);
        instances.add(arsInst);

        ModelInstance rcInst = new ModelInstance(roadCurbTile);
        rcInst.transform.setTranslation(0f, 0.12f, 78.8f);
        instances.add(rcInst);

        // ==========================================
        // 7. NORTH BOUNDARY WALL & FULLER ROAD CORRIDOR
        // ==========================================
        Model northWall = mb.createBox(220f, 2.8f, 0.6f, curzonBrickMat, attr);
        Model northRoad = createPavedSlab(mb, 220f, 0.04f, 10f, 1.0f, GroundConfig.ASPHALT_UV_SCALE, asphaltMat, attr);
        models.add(northWall);
        models.add(northRoad);

        ModelInstance nwInst = new ModelInstance(northWall);
        nwInst.transform.setTranslation(0f, 1.4f, -44.5f);
        instances.add(nwInst);

        ModelInstance nrInst = new ModelInstance(northRoad);
        nrInst.transform.setTranslation(0f, 0.02f, -50f);
        instances.add(nrInst);

        // Build complete East and West perimeter walls and dense backdrop treelines
        PerimeterEnclosure.build(mb, attr, curzonBrickMat, stoneCurb, barkTwoSidedMat, leafClumpMat, leafCardMat, blossomClumpMat, models, instances, colliders, this::registerFoliage);

        // ==========================================
        // 8. WEST HUB: CENTRAL LIBRARY BUILDING (MUZHARUL ISLAM 1953 MODERNIST LANDMARK)
        // ==========================================
        // Elevated floating upper volumes on ground-floor pilotis colonnade, terracotta brick jali screens,
        // and deep horizontal concrete bands allowing natural cross-ventilation.
        float clX = -68.0f, clZ = 22.0f;
        // Upper floor reading hall volumes (floating mass)
        Model clUpperMain = mb.createBox(38f, 7.2f, 32f, curzonBrickMat, attr);
        Model clUpperCore = mb.createBox(20f, 7.8f, 18f, concreteMat, attr);
        Model clJaliScreenEW = mb.createBox(38.4f, 5.2f, 0.25f, curzonTrimMat, attr);
        Model clJaliScreenNS = mb.createBox(0.25f, 5.2f, 32.4f, curzonTrimMat, attr);
        // Ground floor open pilotis (concrete stilts) colonnade
        Model clPilotiCol = mb.createCylinder(0.65f, 4.4f, 0.65f, 16, concreteMat, attr);
        Model clGroundSlab = mb.createBox(42f, 0.35f, 36f, stoneCurb, attr);
        Model clFirstFloorSlab = mb.createBox(40f, 0.55f, 34f, concreteMat, attr);
        Model clRoofCornice = mb.createBox(40.6f, 0.65f, 34.6f, concreteMat, attr);
        // Wide entrance approach steps & portico canopy
        Model clSteps = mb.createBox(6f, 0.45f, 16f, stoneCurb, attr);
        Model clCanopy = mb.createBox(7f, 0.30f, 14f, concreteMat, attr);
        Model clLouver = mb.createBox(0.25f, 6.2f, 0.85f, concreteMat, attr);

        models.add(clUpperMain);
        models.add(clUpperCore);
        models.add(clJaliScreenEW);
        models.add(clJaliScreenNS);
        models.add(clPilotiCol);
        models.add(clGroundSlab);
        models.add(clFirstFloorSlab);
        models.add(clRoofCornice);
        models.add(clSteps);
        models.add(clCanopy);
        models.add(clLouver);

        // Ground podium slab
        ModelInstance clgsInst = new ModelInstance(clGroundSlab);
        clgsInst.transform.setTranslation(clX, 0.18f, clZ);
        instances.add(clgsInst);

        // Ground-floor open pilotis colonnade (Muzharul Islam signature: free movement under building)
        for (float px = -16f; px <= 16.1f; px += 8.0f) {
            for (float pz = -13f; pz <= 13.1f; pz += 6.5f) {
                ModelInstance col = new ModelInstance(clPilotiCol);
                col.transform.setTranslation(clX + px, 2.35f, clZ + pz);
                instances.add(col);
            }
        }

        // Heavy concrete transfer slab separating pilotis from upper brick volumes
        ModelInstance clffsInst = new ModelInstance(clFirstFloorSlab);
        clffsInst.transform.setTranslation(clX, 4.65f, clZ);
        instances.add(clffsInst);

        // Floating upper brick reading halls
        ModelInstance clumInst = new ModelInstance(clUpperMain);
        clumInst.transform.setTranslation(clX, 8.45f, clZ);
        instances.add(clumInst);

        // Exposed concrete central service and circulation core
        ModelInstance clucInst = new ModelInstance(clUpperCore);
        clucInst.transform.setTranslation(clX, 8.75f, clZ);
        instances.add(clucInst);

        // Terracotta perforated jali sunscreens on east and west facades (diffused tropical daylighting)
        ModelInstance jaliEast = new ModelInstance(clJaliScreenEW);
        jaliEast.transform.setTranslation(clX, 8.45f, clZ + 16.15f);
        instances.add(jaliEast);
        ModelInstance jaliWest = new ModelInstance(clJaliScreenEW);
        jaliWest.transform.setTranslation(clX, 8.45f, clZ - 16.15f);
        instances.add(jaliWest);
        ModelInstance jaliSouth = new ModelInstance(clJaliScreenNS);
        jaliSouth.transform.setTranslation(clX + 19.15f, 8.45f, clZ);
        instances.add(jaliSouth);

        // Deep cantilevered roof cornice capping the composition
        ModelInstance clrcInst = new ModelInstance(clRoofCornice);
        clrcInst.transform.setTranslation(clX, 12.25f, clZ);
        instances.add(clrcInst);

        // Wide entrance approach steps leading from the promenade
        ModelInstance clsInst = new ModelInstance(clSteps);
        clsInst.transform.setTranslation(clX + 21.5f, 0.25f, clZ);
        instances.add(clsInst);

        // Entrance canopy projection
        ModelInstance clcpInst = new ModelInstance(clCanopy);
        clcpInst.transform.setTranslation(clX + 21.0f, 4.65f, clZ);
        instances.add(clcpInst);

        // Modernist vertical sun louvers on east facade
        for (int l = -5; l <= 5; l++) {
            ModelInstance luv = new ModelInstance(clLouver);
            luv.transform.setTranslation(clX + 19.8f, 8.45f, clZ + l * 2.5f);
            instances.add(luv);
        }

        // ==========================================
        // 9. WEST HUB: HAKIM CHATTAR (OCTAGONAL STUDENT PAVILION)
        // ==========================================
        float hkX = -58.0f, hkZ = 54.0f;
        Model hkPlinth = mb.createCylinder(9.0f, 0.6f, 9.0f, 16, concreteMat, attr);
        Model hkPillar = mb.createCylinder(0.32f, 3.2f, 0.32f, 8, curzonTrimMat, attr);
        Model hkRoof = mb.createCone(10.5f, 2.2f, 10.5f, 16, canteenRoofMat, attr);
        Model hkFinial = mb.createCone(0.4f, 1.2f, 0.4f, 8, goldFinial, attr);
        Model hkTable = mb.createCylinder(2.4f, 0.75f, 2.4f, 12, woodBench, attr);
        models.add(hkPlinth);
        models.add(hkPillar);
        models.add(hkRoof);
        models.add(hkFinial);
        models.add(hkTable);

        ModelInstance hkpInst = new ModelInstance(hkPlinth);
        hkpInst.transform.setTranslation(hkX, 0.3f, hkZ);
        instances.add(hkpInst);

        // 8 Perimeter columns supporting the pavilion roof
        for (int i = 0; i < 8; i++) {
            float ang = i * 45f * MathUtils.degreesToRadians;
            float px = hkX + 3.8f * MathUtils.cos(ang);
            float pz = hkZ + 3.8f * MathUtils.sin(ang);
            ModelInstance col = new ModelInstance(hkPillar);
            col.transform.setTranslation(px, 2.2f, pz);
            instances.add(col);
        }

        ModelInstance hkrInst = new ModelInstance(hkRoof);
        hkrInst.transform.setTranslation(hkX, 4.8f, hkZ);
        instances.add(hkrInst);

        ModelInstance hkfInst = new ModelInstance(hkFinial);
        hkfInst.transform.setTranslation(hkX, 6.4f, hkZ);
        instances.add(hkfInst);

        ModelInstance hktInst = new ModelInstance(hkTable);
        hktInst.transform.setTranslation(hkX, 0.95f, hkZ);
        instances.add(hktInst);

        // ==========================================
        // 10. WEST HUB: MADHUR CANTEEN (HISTORIC CANTEEN PAVILION)
        // ==========================================
        float mcX = -56.0f, mcZ = -16.0f;
        Model mcPlinth = mb.createBox(18f, 0.6f, 14f, stoneCurb, attr);
        Model mcBuilding = mb.createBox(14f, 3.8f, 10f, curzonBrickMat, attr);
        Model mcRoof = mb.createBox(18.5f, 1.8f, 14.5f, canteenRoofMat, attr);
        Model mcSign = mb.createBox(6.5f, 0.8f, 0.1f, curzonTrimMat, attr);
        Model mcPillar = mb.createBox(0.28f, 3.6f, 0.28f, woodBench, attr);
        models.add(mcPlinth);
        models.add(mcBuilding);
        models.add(mcRoof);
        models.add(mcSign);
        models.add(mcPillar);

        ModelInstance mcpInst = new ModelInstance(mcPlinth);
        mcpInst.transform.setTranslation(mcX, 0.3f, mcZ);
        instances.add(mcpInst);

        ModelInstance mcbInst = new ModelInstance(mcBuilding);
        mcbInst.transform.setTranslation(mcX, 2.5f, mcZ - 1.5f);
        instances.add(mcbInst);

        ModelInstance mcrInst = new ModelInstance(mcRoof);
        mcrInst.transform.setTranslation(mcX, 5.0f, mcZ);
        instances.add(mcrInst);

        // Verandah pillars along front (South facade)
        for (float vx = -6.5f; vx <= 6.5f; vx += 3.25f) {
            ModelInstance col = new ModelInstance(mcPillar);
            col.transform.setTranslation(mcX + vx, 2.4f, mcZ + 5.2f);
            instances.add(col);
        }

        // Historic Signboard above verandah entrance
        ModelInstance mcsInst = new ModelInstance(mcSign);
        mcsInst.transform.setTranslation(mcX, 4.0f, mcZ + 5.5f);
        instances.add(mcsInst);

        // ==========================================
        // 11. WEST HUB: BOOK STALLS (বইয়ের দোকান)
        // ==========================================
        Model stallBox = mb.createBox(2.4f, 2.6f, 1.6f, woodBench, attr);
        Model stallAwning = mb.createBox(2.8f, 0.3f, 2.2f, flowerRed, attr);
        models.add(stallBox);
        models.add(stallAwning);

        float[] stallZ = {34f, 38f, 42f};
        for (float sz : stallZ) {
            ModelInstance sb = new ModelInstance(stallBox);
            sb.transform.setTranslation(-42f, 1.3f, sz);
            instances.add(sb);

            ModelInstance sa = new ModelInstance(stallAwning);
            sa.transform.setTranslation(-42f, 2.7f, sz);
            sa.transform.rotate(Vector3.Z, 12f);
            instances.add(sa);
        }

        // ==========================================
        // 12. SYMMETRICAL ACADEMIC BUILDINGS (WEST & EAST WINGS)
        // ==========================================
        Model acadBuilding = mb.createBox(20f, 8.8f, 12f, curzonBrickMat, attr);
        Model acadRoof = mb.createBox(21f, 1.2f, 13f, canteenRoofMat, attr);
        models.add(acadBuilding);
        models.add(acadRoof);

        // West Academic Building at X=-26m, Z=52m
        ModelInstance wabInst = new ModelInstance(acadBuilding);
        wabInst.transform.setTranslation(-26f, 4.4f, 52f);
        instances.add(wabInst);

        ModelInstance warInst = new ModelInstance(acadRoof);
        warInst.transform.setTranslation(-26f, 9.4f, 52f);
        instances.add(warInst);

        // East Academic Building at X=+26m, Z=52m
        ModelInstance eabInst = new ModelInstance(acadBuilding);
        eabInst.transform.setTranslation(26f, 4.4f, 52f);
        instances.add(eabInst);

        ModelInstance earInst = new ModelInstance(acadRoof);
        earInst.transform.setTranslation(26f, 9.4f, 52f);
        instances.add(earInst);

        // ==========================================
        // 13. EAST HUB: TEACHER-STUDENT CENTRE (TSC) — MODERNIST DOXIADIS COMPLEX
        // ==========================================
        float tscX = 68.0f, tscZ = 20.0f;
        // Doxiadis Tropical Modernist Complex:
        // Main block: concrete structural grid with red brick infills and deep horizontal band windows
        Model tscMain = mb.createBox(40f, 9.2f, 32f, concreteMat, attr);
        Model tscBrickInfill = mb.createBox(38.4f, 7.8f, 30.4f, curzonBrickMat, attr);
        Model tscWindowBand = mb.createBox(40.2f, 1.4f, 32.2f, archCavity, attr);
        // Central Auditorium with signature folded-plate butterfly roof profile
        Model tscAuditorium = mb.createBox(20f, 4.8f, 20f, concreteMat, attr);
        Model tscFoldedRoofEast = mb.createBox(11.2f, 0.45f, 21.6f, concreteMat, attr);
        Model tscFoldedRoofWest = mb.createBox(11.2f, 0.45f, 21.6f, concreteMat, attr);
        // West Entrance Terrace, cantilevered canopy, and cafeteria colonnade
        Model tscTerrace = mb.createBox(14f, 0.45f, 28f, stoneCurb, attr);
        Model tscEntranceCanopy = mb.createBox(8.5f, 0.35f, 16f, concreteMat, attr);
        Model tscCanopyPillar = mb.createCylinder(0.40f, 4.2f, 0.40f, 12, concreteMat, attr);
        // Vertical and horizontal concrete brise-soleil sun louvers
        Model tscVertFin = mb.createBox(0.24f, 7.4f, 0.95f, concreteMat, attr);
        Model tscHorizLouver = mb.createBox(0.85f, 0.18f, 26f, concreteMat, attr);
        models.add(tscMain);
        models.add(tscBrickInfill);
        models.add(tscWindowBand);
        models.add(tscAuditorium);
        models.add(tscFoldedRoofEast);
        models.add(tscFoldedRoofWest);
        models.add(tscTerrace);
        models.add(tscEntranceCanopy);
        models.add(tscCanopyPillar);
        models.add(tscVertFin);
        models.add(tscHorizLouver);

        // Main modernist building volume
        ModelInstance tmInst = new ModelInstance(tscMain);
        tmInst.transform.setTranslation(tscX, 4.6f, tscZ);
        instances.add(tmInst);

        // Warm red brick infill panels contrasting with exposed concrete frame
        ModelInstance tbiInst = new ModelInstance(tscBrickInfill);
        tbiInst.transform.setTranslation(tscX, 4.6f, tscZ);
        instances.add(tbiInst);

        // Continuous ribbon windows on upper and lower floors
        ModelInstance twin1 = new ModelInstance(tscWindowBand);
        twin1.transform.setTranslation(tscX, 3.2f, tscZ);
        instances.add(twin1);
        ModelInstance twin2 = new ModelInstance(tscWindowBand);
        twin2.transform.setTranslation(tscX, 6.8f, tscZ);
        instances.add(twin2);

        // Central auditorium rising above main roofline
        ModelInstance taInst = new ModelInstance(tscAuditorium);
        taInst.transform.setTranslation(tscX, 11.2f, tscZ);
        instances.add(taInst);

        // Signature butterfly / inverted folded-plate roof (Doxiadis architectural hallmark)
        ModelInstance tfrE = new ModelInstance(tscFoldedRoofEast);
        tfrE.transform.setTranslation(tscX + 5.2f, 13.9f, tscZ);
        tfrE.transform.rotate(Vector3.Z, -11f);
        instances.add(tfrE);

        ModelInstance tfrW = new ModelInstance(tscFoldedRoofWest);
        tfrW.transform.setTranslation(tscX - 5.2f, 13.9f, tscZ);
        tfrW.transform.rotate(Vector3.Z, 11f);
        instances.add(tfrW);

        // Covered cafeteria entrance terrace facing west towards campus
        ModelInstance ttInst = new ModelInstance(tscTerrace);
        ttInst.transform.setTranslation(tscX - 22f, 0.22f, tscZ);
        instances.add(ttInst);

        // Cantilevered concrete entrance portico
        ModelInstance tcInst = new ModelInstance(tscEntranceCanopy);
        tcInst.transform.setTranslation(tscX - 23.5f, 4.3f, tscZ);
        instances.add(tcInst);

        // Support columns for entrance canopy
        ModelInstance tcp1 = new ModelInstance(tscCanopyPillar);
        tcp1.transform.setTranslation(tscX - 27.2f, 2.2f, tscZ - 6.5f);
        instances.add(tcp1);
        ModelInstance tcp2 = new ModelInstance(tscCanopyPillar);
        tcp2.transform.setTranslation(tscX - 27.2f, 2.2f, tscZ + 6.5f);
        instances.add(tcp2);

        // Deep vertical concrete sun-breaker fins on west facade
        for (int f = -6; f <= 6; f++) {
            ModelInstance fin = new ModelInstance(tscVertFin);
            fin.transform.setTranslation(tscX - 20.3f, 4.8f, tscZ + f * 2.1f);
            instances.add(fin);
        }

        // Horizontal sun-shading brows across south and north facades
        ModelInstance hlSouth = new ModelInstance(tscHorizLouver);
        hlSouth.transform.setTranslation(tscX - 20.3f, 7.5f, tscZ);
        instances.add(hlSouth);

        // ==========================================
        // 14. EAST HUB: RAJU MEMORIAL SCULPTURE (TSC ROUNDABOUT)
        // ==========================================
        float rmX = 44.0f, rmZ = 42.0f;
        Model rmRoundabout = mb.createCylinder(16.0f, 0.35f, 16.0f, 24, stoneCurb, attr);
        Model rmPedestal1 = mb.createBox(4.8f, 1.15f, 4.8f, stoneCurb, attr);
        Model rmPedestal2 = mb.createBox(3.4f, 0.60f, 3.4f, aparajeyoMat, attr);
        Model rmSpire = mb.createBox(0.85f, 4.8f, 0.85f, aparajeyoMat, attr);
        Model rmFigure = mb.createBox(0.55f, 1.9f, 0.45f, aparajeyoMat, attr);
        models.add(rmRoundabout);
        models.add(rmPedestal1);
        models.add(rmPedestal2);
        models.add(rmSpire);
        models.add(rmFigure);

        ModelInstance rmrInst = new ModelInstance(rmRoundabout);
        rmrInst.transform.setTranslation(rmX, 0.17f, rmZ);
        instances.add(rmrInst);

        ModelInstance rmp1 = new ModelInstance(rmPedestal1);
        rmp1.transform.setTranslation(rmX, 0.92f, rmZ);
        instances.add(rmp1);

        ModelInstance rmp2 = new ModelInstance(rmPedestal2);
        rmp2.transform.setTranslation(rmX, 1.8f, rmZ);
        instances.add(rmp2);

        ModelInstance rmsInst = new ModelInstance(rmSpire);
        rmsInst.transform.setTranslation(rmX, 4.4f, rmZ);
        instances.add(rmsInst);

        // Figures locked arm-in-arm protesting around the central spire
        for (int i = 0; i < 4; i++) {
            float ang = i * 90f * MathUtils.degreesToRadians;
            float fx = rmX + 1.1f * MathUtils.cos(ang);
            float fz = rmZ + 1.1f * MathUtils.sin(ang);
            ModelInstance fig = new ModelInstance(rmFigure);
            fig.transform.setTranslation(fx, 3.05f, fz);
            instances.add(fig);
        }

        // ==========================================
        // 15. EAST HUB: SWADHINATA SANGRAM SCULPTURE GARDEN
        // ==========================================
        float ssX = 56.0f, ssZ = -16.0f;
        Model ssPlaza = mb.createBox(22f, 0.08f, 18f, aparajeyoMat, attr);
        Model ssWall = mb.createBox(22.6f, 0.5f, 0.4f, stoneCurb, attr);
        Model ssPedLg = mb.createBox(2.2f, 1.5f, 2.2f, weatheredStoneMat, attr);
        Model ssPedSm = mb.createBox(1.6f, 1.0f, 1.6f, weatheredStoneMat, attr);
        Model ssStatue = mb.createBox(0.6f, 1.8f, 0.5f, aparajeyoMat, attr);
        models.add(ssPlaza);
        models.add(ssWall);
        models.add(ssPedLg);
        models.add(ssPedSm);
        models.add(ssStatue);

        ModelInstance ssPlInst = new ModelInstance(ssPlaza);
        ssPlInst.transform.setTranslation(ssX, 0.04f, ssZ);
        instances.add(ssPlInst);

        // Low perimeter garden boundary
        ModelInstance sswN = new ModelInstance(ssWall);
        sswN.transform.setTranslation(ssX, 0.25f, ssZ - 9f);
        instances.add(sswN);

        ModelInstance sswS = new ModelInstance(ssWall);
        sswS.transform.setTranslation(ssX, 0.25f, ssZ + 9f);
        instances.add(sswS);

        // 3 Historical Sculpture Pedestals & Statues
        float[][] ssPedPositions = {
            {ssX - 4.5f, 0.75f, ssZ - 3f, 0},
            {ssX + 4.5f, 0.50f, ssZ - 3f, 1},
            {ssX, 0.75f, ssZ + 3f, 0}
        };
        for (float[] spp : ssPedPositions) {
            ModelInstance ped = new ModelInstance(spp[3] == 0 ? ssPedLg : ssPedSm);
            ped.transform.setTranslation(spp[0], spp[1], spp[2]);
            instances.add(ped);

            ModelInstance stat = new ModelInstance(ssStatue);
            stat.transform.setTranslation(spp[0], spp[1] * 2f + 0.9f, spp[2]);
            instances.add(stat);
        }

        // 7. Vintage Cast-Iron Lampposts with Student Movement Banners (Colonial Double-Lantern Posts)
        Model postPedestal = mb.createCylinder(0.48f, 0.65f, 0.48f, 8, castIron, attr);
        Model postShaft = mb.createCylinder(0.18f, 4.4f, 0.18f, 8, castIron, attr);
        Model postCrossArm = mb.createBox(1.5f, 0.12f, 0.14f, castIron, attr);
        Model lanternModel = mb.createBox(0.42f, 0.55f, 0.42f, lanternGlow, attr);
        Model lanternCap = mb.createCone(0.52f, 0.28f, 0.52f, 8, castIron, attr);
        Model bannerModel = mb.createBox(0.75f, 1.5f, 0.04f, bannerMat, attr);
        models.add(postPedestal);
        models.add(postShaft);
        models.add(postCrossArm);
        models.add(lanternModel);
        models.add(lanternCap);
        models.add(bannerModel);

        float[] lampZ = {-10f, 8f, 26f, 44f, 60f};
        for (int i = 0; i < lampZ.length; i++) {
            float lz = lampZ[i];
            // Lamp row at Z=8 would stand in the pukur, so it moves out to the ring paths
            float[] lampX = (lz == 8f) ? new float[]{-16.5f, 16.5f} : new float[]{-5.4f, 5.4f};
            for (float lx : lampX) {
                // Stepped ornamental base
                ModelInstance pedInst = new ModelInstance(postPedestal);
                pedInst.transform.setTranslation(lx, 0.32f, lz);
                instances.add(pedInst);

                // Fluted vertical post
                ModelInstance pInst = new ModelInstance(postShaft);
                pInst.transform.setTranslation(lx, 2.5f, lz);
                instances.add(pInst);

                // Decorative double-lantern cross arm
                ModelInstance armInst = new ModelInstance(postCrossArm);
                armInst.transform.setTranslation(lx, 4.5f, lz);
                instances.add(armInst);

                // Twin colonial carriage lanterns
                for (float so : new float[]{-0.62f, 0.62f}) {
                    ModelInstance lInst = new ModelInstance(lanternModel);
                    lInst.transform.setTranslation(lx + so, 4.25f, lz);
                    instances.add(lInst);

                    ModelInstance capInst = new ModelInstance(lanternCap);
                    capInst.transform.setTranslation(lx + so, 4.65f, lz);
                    instances.add(capInst);
                }

                // Hanging July 2024 student movement banner
                if (i % 2 == 0) {
                    ModelInstance bInst = new ModelInstance(bannerModel);
                    bInst.transform.setTranslation(lx + (lx < 0 ? -0.75f : 0.75f), 2.9f, lz);
                    instances.add(bInst);
                }
            }
        }

        // 8. Park Benches & Student Bicycles
        Model benchModel = mb.createBox(2.2f, 0.8f, 0.7f, woodBench, attr);
        Model bikeFrame = mb.createBox(1.6f, 1.0f, 0.35f, bicycleMetal, attr);
        models.add(benchModel);
        models.add(bikeFrame);

        float[] benchZ = {6f, 24f, 42f};
        for (float bz : benchZ) {
            // The Z=6 bench would be in the pukur, so it moves to the west ring path
            boolean atPukur = bz == 6f;
            ModelInstance benchL = new ModelInstance(benchModel);
            benchL.transform.setTranslation(atPukur ? -17.6f : -7.0f, 0.4f, bz);
            benchL.transform.rotate(Vector3.Y, 90f);
            instances.add(benchL);

            // Bicycle leaning against bench
            ModelInstance bike = new ModelInstance(bikeFrame);
            bike.transform.setTranslation(atPukur ? -18.8f : -8.2f, 0.5f, bz + 1.2f);
            bike.transform.rotate(Vector3.Y, 75f);
            instances.add(bike);
        }

        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.testCard"))) {
            setTestCardActive(true);
        }
    }

    public void setTestCardActive(boolean active) {
        this.testCardActive = active;
        Texture winTex = active ? texturesRef.debugOrientationTestCard : texturesRef.colonialWindow;
        Texture doorTex = active ? texturesRef.debugOrientationTestCard : texturesRef.teakDoorTexture;
        Texture portalTex = active ? texturesRef.debugOrientationTestCard : texturesRef.curzonGrandPortal;
        Texture tripleTex = active ? texturesRef.debugOrientationTestCard : texturesRef.curzonPorticoTripleWindow;

        for (ModelInstance d : wingDoors) {
            int matIdx = d.materials.size - 1;
            d.materials.get(matIdx).set(PBRTextureAttribute.createBaseColorTexture(doorTex));
        }
        for (ModelInstance w : wingWindows) {
            int matIdx = w.materials.size - 1;
            w.materials.get(matIdx).set(PBRTextureAttribute.createBaseColorTexture(winTex));
        }
        if (grandPortalInst != null) {
            int matIdx = grandPortalInst.materials.size - 1;
            grandPortalInst.materials.get(matIdx).set(PBRTextureAttribute.createBaseColorTexture(portalTex));
        }
        if (porticoTripleWinInst != null) {
            int matIdx = porticoTripleWinInst.materials.size - 1;
            porticoTripleWinInst.materials.get(matIdx).set(PBRTextureAttribute.createBaseColorTexture(tripleTex));
        }
        Gdx.app.log("DhakaCampusWorld", "Debug orientation test card active=" + active + " (applied to " + wingDoors.size + " doors, " + wingWindows.size + " windows)");
    }

    public boolean isTestCardActive() {
        return testCardActive;
    }

    public void renderDebugOpeningNormals(ShapeRenderer sr, com.badlogic.gdx.graphics.Camera cam) {
        if (!debugDrawNormals) return;
        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);

        // Wing bays (door at y=3.25, win at y=8.50, z=-23.46)
        float[] facadeBays = {
            -41.6f, -36.4f, -31.2f, -26.0f, -20.8f, -15.6f, -10.4f,
             10.4f,  15.6f,  20.8f,  26.0f,  31.2f,  36.4f,  41.6f
        };
        for (float bx : facadeBays) {
            // Door outline (cyan)
            sr.setColor(Color.CYAN);
            sr.box(bx - 1.10f, 3.25f - 2.05f, -23.46f + 0.005f, 2.20f, 4.10f, 0.01f);
            // Door normal (red line pointing +Z)
            sr.setColor(Color.RED);
            sr.line(bx, 3.25f, -23.46f, bx, 3.25f, -22.46f);

            // Upper Window outline (yellow)
            sr.setColor(Color.YELLOW);
            sr.box(bx - 1.10f, 8.50f - 2.05f, -23.46f + 0.005f, 2.20f, 4.10f, 0.01f);
            // Window normal (red line)
            sr.setColor(Color.RED);
            sr.line(bx, 8.50f, -23.46f, bx, 8.50f, -22.46f);
        }

        // Grand portal (x=0, y=3.20, z=-20.56)
        sr.setColor(Color.CYAN);
        sr.box(-1.90f, 3.20f - 2.60f, -20.56f + 0.005f, 3.80f, 5.20f, 0.01f);
        sr.setColor(Color.RED);
        sr.line(0f, 3.20f, -20.56f, 0f, 3.20f, -19.56f);

        // Portico triple window (x=0, y=11.40, z=-20.56)
        sr.setColor(Color.YELLOW);
        sr.box(-3.60f, 11.40f - 1.90f, -20.56f + 0.005f, 7.20f, 3.80f, 0.01f);
        sr.setColor(Color.RED);
        sr.line(0f, 11.40f, -20.56f, 0f, 11.40f, -19.56f);

        sr.end();
    }

    public void update(float delta) {
        animTime += delta;

        // Dynamic Weather state machine update & atmospheric lighting sync
        weatherController.update(delta);
        sunLight.direction.set(weatherController.getCurrentSunDirection());
        sunLight.color.set(weatherController.getCurrentSunColor());
        if (ambientAttr != null) {
            ambientAttr.color.set(weatherController.getCurrentAmbientColor());
        }
        if (fogColorAttr != null) {
            fogColorAttr.color.set(weatherController.getCurrentFogColor());
        }
        if (fogEquationAttr != null) {
            fogEquationAttr.set(
                weatherController.getCurrentFogNear(),
                weatherController.getCurrentFogFar(),
                weatherController.getCurrentFogExponent()
            );
        }

        // Light shafts breathe slowly and modulate with weather cloud cover / GraphicsTier
        if (beamBlend != null) {
            GraphicsTier tier = GraphicsTier.fromSystemProperty();
            if (!tier.enableVolumetricShafts) {
                beamBlend.opacity = 0f;
            } else {
                float shaftMult = weatherController.getCurrentLightShaftOpacity();
                beamBlend.opacity = BEAM_BASE_OPACITY * shaftMult * (0.80f + 0.20f * MathUtils.sin(animTime * 0.45f));
            }
        }

        // ── Dynamic Multi-Frequency Wind Simulation across Campus Trees ──
        vegetationManager.update(animTime, GraphicsTier.fromSystemProperty(), activeCam);

        // ── Buoyant Lily Pads Bobbing & Tilting on Gerstner Waves ─────────────
        for (int i = 0; i < lilyPads.size; i++) {
            lilyPads.get(i).update(delta, animTime, waveModel);
        }

        for (int f = 0; f < pondFishes.size; f++) {
            PondFish fish = pondFishes.get(f);
            fish.swimPhase += delta * (1.8f + fish.speed * 1.5f);
            fish.angle += delta * (fish.speed / Math.max(fish.radiusX, 1f));
            float fx = fish.centerX + MathUtils.sin(fish.angle) * fish.radiusX;
            float fz = fish.centerZ + MathUtils.cos(fish.angle) * fish.radiusZ;
            float dx = MathUtils.cos(fish.angle) * fish.radiusX;
            float dz = -MathUtils.sin(fish.angle) * fish.radiusZ;
            float heading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
            float tailWag = MathUtils.sin(fish.swimPhase * 6.5f) * 25.0f;

            float waterSurfaceY = waveModel.getWaterHeight(fx, fz, animTime);
            float currentFishY = waterSurfaceY - fish.depthY;

            fish.body.transform.idt().translate(fx, currentFishY, fz).rotate(Vector3.Y, heading);
            fish.tail.transform.idt().translate(fx, currentFishY, fz).rotate(Vector3.Y, heading)
                .translate(0f, 0f, -0.22f).rotate(Vector3.Y, tailWag);
        }

        // Fireflies: each wanders its own slow, irregular path (sums of unrelated sines) around
        // the stone and blinks on its own rhythm, instead of orbiting together in a ring
        for (int i = 0; i < boundarySparkles.size; i++) {
            ModelInstance sp = boundarySparkles.get(i);
            float seed = i * 2.39f;
            float t = animTime * (0.55f + (i % 3) * 0.12f);
            float px = BOUNDARY_STONE_POS.x + MathUtils.sin(t * 0.73f + seed) * 1.6f
                + MathUtils.sin(t * 1.91f + seed * 1.7f) * 0.45f;
            float pz = BOUNDARY_STONE_POS.z + MathUtils.cos(t * 0.61f + seed * 1.3f) * 1.6f
                + MathUtils.sin(t * 1.53f + seed * 0.6f) * 0.45f;
            float py = 1.0f + (i % 3) * 0.35f + MathUtils.sin(t * 1.17f + seed) * 0.35f
                + MathUtils.sin(t * 2.9f + seed * 2.1f) * 0.08f;
            float blink = MathUtils.sin(animTime * (1.3f + (i % 4) * 0.37f) + seed * 3f);
            float glow = blink > 0.2f ? 1f : Math.max(0.25f, 0.6f + blink);
            sp.transform.setToTranslationAndScaling(px, py, pz, glow, glow, glow);
        }
    }

    private void addCrossedQuads(MeshPartBuilder mpb, float cx, float cy, float cz,
                                 float width, float height, int planes, float yOffset) {
        float halfW = width * 0.5f;
        float angleStep = 180f / planes;

        VertexInfo v00 = new VertexInfo();
        VertexInfo v10 = new VertexInfo();
        VertexInfo v11 = new VertexInfo();
        VertexInfo v01 = new VertexInfo();

        for (int p = 0; p < planes; p++) {
            float angle = p * angleStep;
            float cos = MathUtils.cosDeg(angle);
            float sin = MathUtils.sinDeg(angle);

            float x0 = cx - halfW * cos;
            float z0 = cz - halfW * sin;
            float x1 = cx + halfW * cos;
            float z1 = cz + halfW * sin;
            float y0 = cy + yOffset;
            float y1 = cy + yOffset + height;

            v00.setPos(x0, y0, z0).setUV(0f, 1f).setNor(0f, 1f, 0f);
            v10.setPos(x1, y0, z1).setUV(1f, 1f).setNor(0f, 1f, 0f);
            v11.setPos(x1, y1, z1).setUV(1f, 0f).setNor(0f, 1f, 0f);
            v01.setPos(x0, y1, z0).setUV(0f, 0f).setNor(0f, 1f, 0f);

            mpb.rect(v00, v10, v11, v01);
        }
    }

    private void addHorizontalQuad(MeshPartBuilder mpb, float cx, float cy, float cz, float size) {
        float half = size * 0.5f;
        VertexInfo v00 = new VertexInfo();
        VertexInfo v10 = new VertexInfo();
        VertexInfo v11 = new VertexInfo();
        VertexInfo v01 = new VertexInfo();

        v00.setPos(cx - half, cy, cz - half).setUV(0f, 1f).setNor(0f, 1f, 0f);
        v10.setPos(cx + half, cy, cz - half).setUV(1f, 1f).setNor(0f, 1f, 0f);
        v11.setPos(cx + half, cy, cz + half).setUV(1f, 0f).setNor(0f, 1f, 0f);
        v01.setPos(cx - half, cy, cz + half).setUV(0f, 0f).setNor(0f, 1f, 0f);

        mpb.rect(v00, v10, v11, v01);
    }

    public void render(ModelBatch batch) {
        com.badlogic.gdx.graphics.Camera cam = batch.getCamera();
        if (cam != null) {
            activeCam = cam;
        }

        for (ModelInstance inst : instances) {
            batch.render(inst, environment);
        }
        for (ModelInstance fx : effects) {
            batch.render(fx, environment);
        }
        for (int i = 0; i < pondFishes.size; i++) {
            PondFish fish = pondFishes.get(i);
            batch.render(fish.body, environment);
            batch.render(fish.tail, environment);
        }

        // Render campus trees with frustum culling & distance LOD
        vegetationManager.render(batch, environment, cam);

        // Flush opaque scene & underwater geometry so pond bed and fish are depth-tested
        batch.flush();

        if (waterRenderer != null) {
            if (cam != null) {
                waterRenderer.render(cam, pbrEnvironment, sunLight, texturesRef.waterRippleNormal, animTime, GraphicsTier.fromSystemProperty());
            }
        }

        // Render buoyant lily pads on top of water surface
        for (int i = 0; i < lilyPads.size; i++) {
            lilyPads.get(i).render(batch, environment);
        }
    }

    /** Starts the sun's depth pass, centred on the player. Pair with {@link #endShadowPass()}. */
    public void beginShadowPass(Vector3 center, Vector3 forward) {
        sunLight.begin(center, forward);
    }

    public void endShadowPass() {
        sunLight.end();
    }

    public com.badlogic.gdx.graphics.Camera getShadowCamera() {
        return sunLight.getCamera();
    }

    /** Draws everything that should cast a shadow (all but the ground plane and the water). */
    public void renderShadowCasters(ModelBatch depthBatch) {
        // instances[0] is the ground plane: it only receives shadows
        for (int i = 1; i < instances.size; i++) {
            ModelInstance inst = instances.get(i);
            if (inst == waterSurface || inst == waterShimmer) continue;
            if (shadowExclusions.contains(inst)) continue;
            depthBatch.render(inst);
        }
        vegetationManager.renderShadowCasters(depthBatch);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public PbrEnvironment getPbrEnvironment() {
        return pbrEnvironment;
    }

    public WaterWaveModel getWaveModel() {
        return waveModel;
    }

    public WaterRenderer getWaterRenderer() {
        return waterRenderer;
    }

    public Array<LilyPad> getLilyPads() {
        return lilyPads;
    }

    public VegetationManager getVegetationManager() {
        return vegetationManager;
    }

    public TerrainSurface getTerrainSurface() {
        return terrainSurface;
    }

    public WeatherController getWeatherController() {
        return weatherController;
    }

    public DirectionalShadowLight getSunLight() {
        return sunLight;
    }

    private static void setupPbr(Material mat, float metallic, float roughness) {
        mat.set(PBRFloatAttribute.createMetallic(metallic));
        mat.set(PBRFloatAttribute.createRoughness(roughness));
        ColorAttribute diff = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
        if (diff != null && !mat.has(PBRColorAttribute.BaseColorFactor)) {
            mat.set(PBRColorAttribute.createBaseColorFactor(diff.color));
        }
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
        instances.clear();
        boundarySparkles.clear();
        sunLight.dispose();
        if (pbrEnvironment != null) {
            pbrEnvironment.dispose();
        }
        if (waterRenderer != null) {
            waterRenderer.dispose();
        }
        vegetationManager.dispose();
        terrainSurface.dispose();
        lilyPads.clear();
    }

    private void buildPondFishes(ModelBuilder mb, float cx, float cz, float pw, float pl, long attr) {
        Color[] fishColors = {
            new Color(1.0f, 0.55f, 0.08f, 1f), // Golden Koi
            new Color(0.92f, 0.20f, 0.14f, 1f), // Scarlet Carp
            new Color(0.65f, 0.82f, 0.96f, 1f), // Shimmering Rohu
            new Color(0.96f, 0.94f, 0.88f, 1f), // Pearl White Koi
            new Color(0.90f, 0.48f, 0.12f, 1f), // Deep Amber Carp
            new Color(0.85f, 0.25f, 0.20f, 1f), // Crimson Carp
            new Color(0.72f, 0.88f, 0.95f, 1f), // Silver Rohu
            new Color(0.98f, 0.65f, 0.15f, 1f)  // Sunburst Koi
        };

        for (int i = 0; i < 14; i++) {
            Color c = fishColors[i % fishColors.length];
            Material fishMat = new Material(
                ColorAttribute.createDiffuse(c),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.92f)
            );
            setupPbr(fishMat, 0.1f, 0.35f);
            Model bodyModel = mb.createSphere(0.13f, 0.055f, 0.36f, 12, 8, fishMat, attr);
            Model tailModel = mb.createBox(0.025f, 0.05f, 0.15f, fishMat, attr);
            models.add(bodyModel);
            models.add(tailModel);

            PondFish fish = new PondFish();
            fish.centerX = cx;
            fish.centerZ = cz;
            fish.radiusX = 2.6f + (i % 5) * 1.15f;
            fish.radiusZ = 3.8f + (i % 5) * 1.6f;
            fish.speed = 0.7f + (i % 7) * 0.14f;
            fish.angle = i * (MathUtils.PI2 / 14f);
            fish.swimPhase = i * 1.3f;
            fish.depthY = 0.055f + (i % 3) * 0.012f;
            fish.body = new ModelInstance(bodyModel);
            fish.tail = new ModelInstance(tailModel);
            pondFishes.add(fish);
        }
    }

    public boolean isNearPondGhat(Vector3 pos) {
        return Math.abs(pos.x) < 4.2f && Math.abs(pos.z - 18.0f) < 2.5f;
    }

}
