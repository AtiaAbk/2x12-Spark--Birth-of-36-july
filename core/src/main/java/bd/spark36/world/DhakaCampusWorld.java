package bd.spark36.world;

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
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * 3D environment representing Dhaka University Curzon Hall campus with high-fidelity
 * procedural textures, Indo-Saracenic terracotta architecture, tree canopies, vintage
 * lampposts with student movement banners, parked bicycles, and golden-hour lighting.
 */
public class DhakaCampusWorld implements Disposable {

    public static final Vector3 BOUNDARY_STONE_POS = new Vector3(7.8f, 0f, 36.5f);

    private final Environment environment;
    private final DirectionalLight sunLight;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final Array<ModelInstance> boundarySparkles = new Array<>();
    private float animTime = 0f;

    public DhakaCampusWorld(TextureFactory textures) {
        environment = new Environment();

        // Warm golden-hour sun matching mockup lighting (upper-right sun streaming down)
        sunLight = new DirectionalLight();
        sunLight.set(new Color(1.0f, 0.95f, 0.82f, 1f), new Vector3(-0.55f, -0.60f, -0.58f).nor());
        environment.add(sunLight);

        // Soft ambient daylight
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.52f, 0.50f, 0.54f, 1f));

        // Atmospheric depth fog (warm golden dawn mist)
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.92f, 0.86f, 0.74f, 1f));

        buildCampus(textures);
    }

    private void buildCampus(TextureFactory textures) {
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        // --- Materials with High-Res Procedural Textures ---
        Material grassMat = new Material(
            TextureAttribute.createDiffuse(textures.lawnGrass),
            ColorAttribute.createDiffuse(new Color(0.92f, 0.95f, 0.90f, 1f))
        );

        Material brickPavementMat = new Material(
            TextureAttribute.createDiffuse(textures.brickPavement),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.94f, 0.90f, 1f))
        );

        Material curzonBrickMat = new Material(
            TextureAttribute.createDiffuse(textures.curzonBrick),
            ColorAttribute.createDiffuse(new Color(0.98f, 0.95f, 0.92f, 1f))
        );

        Material treeTrunkMat = new Material(
            TextureAttribute.createDiffuse(textures.treeBark),
            ColorAttribute.createDiffuse(new Color(0.90f, 0.88f, 0.85f, 1f))
        );

        Material foliageMat = new Material(
            TextureAttribute.createDiffuse(textures.foliage),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.98f, 0.94f, 1f))
        );

        Material krishnachuraMat = new Material(
            TextureAttribute.createDiffuse(textures.krishnachuraBlossom),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 0.96f, 0.94f, 1f))
        );

        Material bambooCulmMat = new Material(
            TextureAttribute.createDiffuse(textures.bambooCulm),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.98f, 0.92f, 1f))
        );

        Material bambooLeafMat = new Material(
            TextureAttribute.createDiffuse(textures.bambooFoliage),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.98f, 0.92f, 1f))
        );

        Material bushMat = new Material(
            TextureAttribute.createDiffuse(textures.bushFoliage),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            FloatAttribute.createAlphaTest(0.20f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.98f, 0.92f, 1f))
        );

        Material weatheredStoneMat = new Material(
            TextureAttribute.createDiffuse(textures.weatheredStone),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.92f, 1f))
        );

        Material curzonPukurWaterMat = new Material(
            TextureAttribute.createDiffuse(textures.curzonWater),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.90f),
            ColorAttribute.createDiffuse(new Color(0.92f, 0.96f, 1.0f, 1f))
        );

        Material sparkleMat = new Material(ColorAttribute.createDiffuse(new Color(1f, 1f, 0.85f, 1f)));

        Material flagMat = new Material(
            TextureAttribute.createDiffuse(textures.bdFlag)
        );

        Material aparajeyoMat = new Material(
            TextureAttribute.createDiffuse(textures.aparajeyoStone),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.94f, 0.92f, 1f))
        );

        Material bannerMat = new Material(
            TextureAttribute.createDiffuse(textures.movementBanner)
        );

        Material asphaltMat = new Material(
            TextureAttribute.createDiffuse(textures.asphaltRoad),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.95f, 1f))
        );

        Material concreteMat = new Material(
            TextureAttribute.createDiffuse(textures.modernistConcrete),
            ColorAttribute.createDiffuse(new Color(0.96f, 0.95f, 0.92f, 1f))
        );

        Material canteenRoofMat = new Material(
            TextureAttribute.createDiffuse(textures.canteenTinRoof),
            ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.90f, 1f))
        );

        Material stoneCurb = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.85f, 0.80f, 1f)));
        Material curzonDomeMat = new Material(ColorAttribute.createDiffuse(new Color(0.93f, 0.92f, 0.88f, 1f)));
        Material curzonTrimMat = new Material(ColorAttribute.createDiffuse(new Color(0.96f, 0.94f, 0.90f, 1f)));
        Material goldFinial = new Material(ColorAttribute.createDiffuse(new Color(1.0f, 0.85f, 0.35f, 1f)));
        Material archCavity = new Material(ColorAttribute.createDiffuse(new Color(0.18f, 0.10f, 0.08f, 1f)));
        Material teakDoor = new Material(ColorAttribute.createDiffuse(new Color(0.32f, 0.16f, 0.10f, 1f)));
        Material castIron = new Material(ColorAttribute.createDiffuse(new Color(0.14f, 0.16f, 0.18f, 1f)));
        Material lanternGlow = new Material(ColorAttribute.createDiffuse(new Color(1.0f, 0.92f, 0.60f, 1f)));
        Material waterMat = new Material(ColorAttribute.createDiffuse(new Color(0.24f, 0.52f, 0.62f, 1f)));
        Material woodBench = new Material(ColorAttribute.createDiffuse(new Color(0.42f, 0.28f, 0.16f, 1f)));
        Material bicycleMetal = new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.24f, 0.28f, 1f)));
        Material hedgeMat = new Material(ColorAttribute.createDiffuse(new Color(0.14f, 0.38f, 0.12f, 1f)));
        Material flowerRed = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.16f, 0.18f, 1f)));

        // 1. Central Campus Lawn (Spanning entire precinct)
        Model groundModel = mb.createBox(280f, 0.2f, 260f, grassMat, attr);
        models.add(groundModel);
        ModelInstance groundInst = new ModelInstance(groundModel);
        groundInst.transform.setTranslation(0f, -0.1f, 20f);
        instances.add(groundInst);

        // 2. Grand Herringbone Brick Avenue with Stone Curbs & Landscaped Flowerbeds
        Model avenueTile = mb.createBox(7.2f, 0.06f, 7.2f, brickPavementMat, attr);
        Model curbTile = mb.createBox(0.35f, 0.14f, 7.2f, stoneCurb, attr);
        Model hedgeTile = mb.createBox(0.9f, 0.65f, 7.2f, hedgeMat, attr);
        Model flowerTile = mb.createBox(0.75f, 0.35f, 7.2f, flowerRed, attr);
        models.add(avenueTile);
        models.add(curbTile);
        models.add(hedgeTile);
        models.add(flowerTile);

        // Central Promenade (from South Gate at 76m to Curzon steps at -18m)
        for (float z = -18f; z <= 76f; z += 7.2f) {
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
            if (z > -10f && z < 70f) {
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
        Model crossWalkEastWest = mb.createBox(5.4f, 0.06f, 4.8f, brickPavementMat, attr);
        Model crossWalkNorthSouth = mb.createBox(4.8f, 0.06f, 5.4f, brickPavementMat, attr);
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

        // Arched Verandah Openings with White Cusped Arch Frames
        Model archNiche = mb.createBox(2.8f, 6.0f, 0.4f, archCavity, attr);
        Model archFrame = mb.createBox(3.2f, 6.4f, 0.1f, curzonTrimMat, attr);
        models.add(archNiche);
        models.add(archFrame);

        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue;
            float ax = i * 4.4f;
            ModelInstance frame = new ModelInstance(archFrame);
            frame.transform.setTranslation(ax, 3.8f, -23.7f);
            instances.add(frame);

            ModelInstance arch = new ModelInstance(archNiche);
            arch.transform.setTranslation(ax, 3.8f, -23.8f);
            instances.add(arch);
        }

        // Grand Central Portal with Cusped Archway & Heavy Teak Doors
        Model portalFrame = mb.createBox(5.6f, 8.2f, 0.2f, curzonTrimMat, attr);
        Model mainArch = mb.createBox(4.8f, 7.4f, 0.6f, archCavity, attr);
        Model doorLeaf = mb.createBox(4.0f, 6.4f, 0.3f, teakDoor, attr);
        models.add(portalFrame);
        models.add(mainArch);
        models.add(doorLeaf);

        ModelInstance portalFrameInst = new ModelInstance(portalFrame);
        portalFrameInst.transform.setTranslation(0f, 4.3f, -20.5f);
        instances.add(portalFrameInst);

        ModelInstance mainArchInst = new ModelInstance(mainArch);
        mainArchInst.transform.setTranslation(0f, 4.2f, -20.6f);
        instances.add(mainArchInst);

        ModelInstance doorInst = new ModelInstance(doorLeaf);
        doorInst.transform.setTranslation(0f, 3.8f, -20.4f);
        instances.add(doorInst);

        // Central High Drum & Majestic White Bulbous Mughal Dome
        Model domeBase = mb.createCylinder(8.6f, 3.2f, 8.6f, 24, curzonTrimMat, attr);
        Model domeSphere = mb.createSphere(8.2f, 6.4f, 8.2f, 24, 20, curzonDomeMat, attr);
        Model finialBase = mb.createCone(0.8f, 2.6f, 0.8f, 12, goldFinial, attr);
        Model finialSpire = mb.createCylinder(0.12f, 3.4f, 0.12f, 8, goldFinial, attr);
        models.add(domeBase);
        models.add(domeSphere);
        models.add(finialBase);
        models.add(finialSpire);

        ModelInstance domeBaseInst = new ModelInstance(domeBase);
        domeBaseInst.transform.setTranslation(0f, 17.2f, -22.5f);
        instances.add(domeBaseInst);

        ModelInstance domeInst = new ModelInstance(domeSphere);
        domeInst.transform.setTranslation(0f, 20.8f, -22.5f);
        instances.add(domeInst);

        ModelInstance finialBaseInst = new ModelInstance(finialBase);
        finialBaseInst.transform.setTranslation(0f, 24.8f, -22.5f);
        instances.add(finialBaseInst);

        ModelInstance finialSpireInst = new ModelInstance(finialSpire);
        finialSpireInst.transform.setTranslation(0f, 26.5f, -22.5f);
        instances.add(finialSpireInst);

        // Bangladesh National Flag Flying High on Central Roof Flagpole
        Model flagPole = mb.createCylinder(0.12f, 7.5f, 0.12f, 8, castIron, attr);
        Model flagModel = mb.createBox(3.4f, 2.04f, 0.04f, flagMat, attr);
        models.add(flagPole);
        models.add(flagModel);

        ModelInstance poleInst = new ModelInstance(flagPole);
        poleInst.transform.setTranslation(0f, 25.5f, -26.0f);
        instances.add(poleInst);

        ModelInstance flagInst = new ModelInstance(flagModel);
        flagInst.transform.setTranslation(1.7f, 27.5f, -26.0f);
        instances.add(flagInst);

        // Corner Chhatris (4 authentic Indo-Saracenic domed kiosks with white pillars)
        Model chhatriDome = mb.createSphere(3.4f, 2.6f, 3.4f, 16, 14, curzonDomeMat, attr);
        Model chhatriPillar = mb.createBox(0.45f, 3.4f, 0.45f, curzonTrimMat, attr);
        models.add(chhatriDome);
        models.add(chhatriPillar);

        float[] chhatriX = {-18.5f, 18.5f, -44.5f, 44.5f};
        for (float cx : chhatriX) {
            float cz = -24.5f;
            ModelInstance cd = new ModelInstance(chhatriDome);
            cd.transform.setTranslation(cx, 16.0f, cz);
            instances.add(cd);

            float[] ox = {-1.0f, 1.0f};
            float[] oz = {-1.0f, 1.0f};
            for (float px : ox) {
                for (float pz : oz) {
                    ModelInstance cp = new ModelInstance(chhatriPillar);
                    cp.transform.setTranslation(cx + px, 13.2f, cz + pz);
                    instances.add(cp);
                }
            }
        }

        // 4. REALISTIC DHAKA UNIVERSITY CAMPUS VEGETATION & LANDMARKS
        // Rain Tree & Krishnachura Trunks & Branches
        Model rainTrunk = mb.createCylinder(1.10f, 4.4f, 1.10f, 12, treeTrunkMat, attr);
        Model rainBranch = mb.createBox(0.42f, 3.2f, 0.42f, treeTrunkMat, attr);
        Model krishnaTrunk = mb.createCylinder(0.82f, 4.0f, 0.82f, 10, treeTrunkMat, attr);
        Model krishnaBranch = mb.createBox(0.35f, 2.8f, 0.35f, treeTrunkMat, attr);
        Model palmTrunk = mb.createCylinder(0.38f, 9.6f, 0.38f, 10, treeTrunkMat, attr);
        models.add(rainTrunk);
        models.add(rainBranch);
        models.add(krishnaTrunk);
        models.add(krishnaBranch);
        models.add(palmTrunk);

        // Bamboo Culm Models (slender segmented canes: 8.5m, 10.5m, 12m)
        Model culm8 = mb.createCylinder(0.14f, 8.5f, 0.14f, 8, bambooCulmMat, attr);
        Model culm10 = mb.createCylinder(0.16f, 10.5f, 0.16f, 8, bambooCulmMat, attr);
        Model culm12 = mb.createCylinder(0.16f, 12.0f, 0.16f, 8, bambooCulmMat, attr);
        Model bambooShoot = mb.createCylinder(0.08f, 1.4f, 0.08f, 6, bambooCulmMat, attr);
        models.add(culm8);
        models.add(culm10);
        models.add(culm12);
        models.add(bambooShoot);

        // Combined Foliage Meshes (Batch-rendered for 60fps performance!)
        ModelBuilder mbFoliage = new ModelBuilder();
        mbFoliage.begin();
        MeshPartBuilder mpbRain = mbFoliage.part("rainLeaves", GL20.GL_TRIANGLES, attr, foliageMat);
        MeshPartBuilder mpbKrishna = mbFoliage.part("krishnaLeaves", GL20.GL_TRIANGLES, attr, krishnachuraMat);
        MeshPartBuilder mpbBamboo = mbFoliage.part("bambooLeaves", GL20.GL_TRIANGLES, attr, bambooLeafMat);
        MeshPartBuilder mpbBushes = mbFoliage.part("bushes", GL20.GL_TRIANGLES, attr, bushMat);

        float[][] treeLocations = {
            // Avenue flanks: Alternating Rain Trees & Krishnachura
            {-11.5f, -4f, 0}, {11.5f, -4f, 1},
            {-12.5f, 14f, 1}, {12.5f, 14f, 0},
            {-13.5f, 32f, 0}, {13.5f, 32f, 1},
            {-14.5f, 50f, 1}, {14.5f, 50f, 0},
            {-28.0f, 16f, 0}, {28.0f, 16f, 1},
            {-48.0f, 6f, 1},  {48.0f, 6f, 0},
            {-62.0f, 30f, 0}, {62.0f, 30f, 1},
            {-32.0f, -12f, 1}, {32.0f, -12f, 0},
            {-22.0f, 68f, 0}, {22.0f, 68f, 1},
            {0.0f, 78f, 0},

            // Royal Palms along perimeter
            {-18f, 2f, 2}, {18f, 2f, 2},
            {-20f, 40f, 2}, {20f, 40f, 2}
        };

        for (float[] loc : treeLocations) {
            float tx = loc[0];
            float tz = loc[1];
            int type = (int) loc[2];

            if (type == 0) {
                // Rain Tree (Majestic spreading umbrella with real branches & cutout leaf cards)
                ModelInstance tInst = new ModelInstance(rainTrunk);
                tInst.transform.setTranslation(tx, 2.2f, tz);
                instances.add(tInst);

                // 4 Spreading gnarled branches reaching outwards
                float[][] bOffsets = {
                    {1.5f, 3.8f, 1.5f, 35f, 35f},
                    {-1.5f, 3.8f, 1.5f, 35f, -35f},
                    {1.5f, 3.8f, -1.5f, -35f, 35f},
                    {-1.5f, 3.8f, -1.5f, -35f, -35f}
                };

                for (float[] bo : bOffsets) {
                    ModelInstance b = new ModelInstance(rainBranch);
                    b.transform.setTranslation(tx + bo[0], bo[1], tz + bo[2]);
                    b.transform.rotate(Vector3.X, bo[3]).rotate(Vector3.Z, bo[4]);
                    instances.add(b);

                    // Multi-layer foliage cards at branch tip
                    addCrossedQuads(mpbRain, tx + bo[0] * 1.6f, 4.4f, tz + bo[2] * 1.6f, 5.2f, 3.6f, 3, 0f);
                    addHorizontalQuad(mpbRain, tx + bo[0] * 1.6f, 6.2f, tz + bo[2] * 1.6f, 4.8f);
                }

                // Central lower and high canopy crown (fully enveloping trunk and branches)
                addCrossedQuads(mpbRain, tx, 3.4f, tz, 7.2f, 3.8f, 3, 0f);
                addHorizontalQuad(mpbRain, tx, 4.8f, tz, 6.4f);
                addCrossedQuads(mpbRain, tx, 4.6f, tz, 7.8f, 4.4f, 4, 0f);
                addHorizontalQuad(mpbRain, tx, 7.0f, tz, 6.8f);

                // Ground bush ring around tree trunk
                addCrossedQuads(mpbBushes, tx + 1.2f, 0f, tz + 0.8f, 1.8f, 1.3f, 3, 0f);
                addCrossedQuads(mpbBushes, tx - 1.0f, 0f, tz - 1.1f, 1.6f, 1.1f, 3, 0f);
            } else if (type == 1) {
                // Krishnachura Tree (Vibrant scarlet-red blossoms & feathery fronds)
                ModelInstance tInst = new ModelInstance(krishnaTrunk);
                tInst.transform.setTranslation(tx, 2.0f, tz);
                instances.add(tInst);

                float[][] bOffsets = {
                    {1.3f, 3.5f, 1.1f, 30f, 30f},
                    {-1.3f, 3.5f, 1.1f, 30f, -30f},
                    {0.0f, 3.6f, -1.4f, -35f, 0f}
                };

                for (float[] bo : bOffsets) {
                    ModelInstance b = new ModelInstance(krishnaBranch);
                    b.transform.setTranslation(tx + bo[0], bo[1], tz + bo[2]);
                    b.transform.rotate(Vector3.X, bo[3]).rotate(Vector3.Z, bo[4]);
                    instances.add(b);

                    addCrossedQuads(mpbKrishna, tx + bo[0] * 1.5f, 4.2f, tz + bo[2] * 1.5f, 4.8f, 3.4f, 3, 0f);
                    addHorizontalQuad(mpbKrishna, tx + bo[0] * 1.5f, 5.8f, tz + bo[2] * 1.5f, 4.4f);
                }

                // Central lower and high blossom crown
                addCrossedQuads(mpbKrishna, tx, 3.2f, tz, 6.5f, 3.6f, 3, 0f);
                addHorizontalQuad(mpbKrishna, tx, 4.5f, tz, 5.8f);
                addCrossedQuads(mpbKrishna, tx, 4.4f, tz, 7.0f, 4.0f, 4, 0f);
                addHorizontalQuad(mpbKrishna, tx, 6.6f, tz, 6.2f);

                // Wildflower shrub around base
                addCrossedQuads(mpbBushes, tx + 0.9f, 0f, tz + 0.9f, 1.7f, 1.2f, 3, 0f);
            } else {
                // Royal Palm Tree with radiating fronds
                ModelInstance pInst = new ModelInstance(palmTrunk);
                pInst.transform.setTranslation(tx, 4.8f, tz);
                instances.add(pInst);

                addCrossedQuads(mpbRain, tx, 8.8f, tz, 5.8f, 2.4f, 4, 0f);
                addHorizontalQuad(mpbRain, tx, 9.6f, tz, 5.0f);
            }
        }

        // ==========================================
        // DENSE BAMBOO GROVES (বাঁশঝাড় — Authentic Asian Bamboo Stands)
        // ==========================================
        float[][] bambooGroves = {
            // Front walkway flank (Right next to spawn & Boundary Stone!)
            {12.5f, 38f},
            // Opposite walkway flank
            {-13.5f, 38f},
            // Western garden stands
            {-24.0f, 26f},
            // Eastern garden stands
            {24.0f, 26f},
            // Curzon Hall North-West & North-East Flanks
            {-22.0f, -8f},
            {22.0f, -8f}
        };

        for (float[] grove : bambooGroves) {
            float gx = grove[0], gz = grove[1];

            // 11-13 slender bamboo culms clustered naturally in each grove
            float[][] culmOffsets = {
                {0f, 0f, 12f}, {0.8f, 0.5f, 10.5f}, {-0.7f, 0.6f, 12f},
                {1.2f, -0.6f, 10.5f}, {-1.1f, -0.4f, 8.5f}, {0.4f, 1.2f, 10.5f},
                {-0.5f, 1.3f, 8.5f}, {1.5f, 0.8f, 12f}, {-1.4f, 0.9f, 10.5f},
                {0.2f, -1.2f, 8.5f}, {-0.8f, -1.1f, 10.5f}, {1.0f, -1.3f, 8.5f}
            };

            for (int c = 0; c < culmOffsets.length; c++) {
                float[] co = culmOffsets[c];
                float cx = gx + co[0];
                float cz = gz + co[1];
                float h = co[2];

                Model culmModel = (h > 11f) ? culm12 : ((h > 9.5f) ? culm10 : culm8);
                ModelInstance cInst = new ModelInstance(culmModel);
                cInst.transform.setTranslation(cx, h * 0.5f, cz);

                // Slight natural wind tilt (1 to 4 degrees)
                float tiltAngle = (c * 37f) % 4.5f - 2.2f;
                cInst.transform.rotate(Vector3.X, tiltAngle).rotate(Vector3.Z, -tiltAngle);
                instances.add(cInst);

                // Multi-tiered bamboo leaf sprays along the upper half of the culm
                float leafBaseY = h * 0.52f;
                float leafTopY = h * 0.96f;
                float leafH = (leafTopY - leafBaseY) * 0.65f;

                // Crossed bamboo leaf quads radiating in all directions
                addCrossedQuads(mpbBamboo, cx, leafBaseY, cz, 3.2f, leafH, 3, 0f);
                addCrossedQuads(mpbBamboo, cx, leafBaseY + leafH * 0.4f, cz, 2.6f, leafH * 0.75f, 3, 0f);
                addHorizontalQuad(mpbBamboo, cx, leafTopY, cz, 2.8f);
            }

            // Young bamboo shoot sprouts around grove perimeter
            float[][] shootOffsets = {{-1.6f, 0.2f}, {1.7f, -0.4f}, {0.3f, 1.8f}, {-0.2f, -1.7f}};
            for (float[] so : shootOffsets) {
                ModelInstance sInst = new ModelInstance(bambooShoot);
                sInst.transform.setTranslation(gx + so[0], 0.7f, gz + so[1]);
                sInst.transform.rotate(Vector3.Z, so[0] * 5f);
                instances.add(sInst);
            }

            // Low undergrowth bushes and fallen leaves at bamboo grove base
            addCrossedQuads(mpbBushes, gx + 0.6f, 0f, gz - 0.4f, 2.2f, 1.4f, 3, 0f);
            addCrossedQuads(mpbBushes, gx - 0.8f, 0f, gz + 0.6f, 2.0f, 1.2f, 3, 0f);
        }

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

        // Chiseled standing Boundary Stone monolith
        Model stoneMonolith = mb.createBox(0.95f, 1.40f, 0.65f, weatheredStoneMat, attr);
        Model stoneTop = mb.createCone(0.85f, 0.55f, 0.55f, 8, weatheredStoneMat, attr);
        Model stonePlaque = mb.createBox(0.70f, 0.90f, 0.04f, aparajeyoMat, attr);
        models.add(stoneMonolith);
        models.add(stoneTop);
        models.add(stonePlaque);

        ModelInstance monolithInst = new ModelInstance(stoneMonolith);
        monolithInst.transform.setTranslation(bsX, 1.02f, bsZ);
        monolithInst.transform.rotate(Vector3.Y, 20f);
        instances.add(monolithInst);

        ModelInstance topInst = new ModelInstance(stoneTop);
        topInst.transform.setTranslation(bsX, 1.95f, bsZ);
        topInst.transform.rotate(Vector3.Y, 20f);
        instances.add(topInst);

        ModelInstance plaqueInst = new ModelInstance(stonePlaque);
        plaqueInst.transform.setTranslation(bsX - 0.08f, 1.05f, bsZ + 0.32f);
        plaqueInst.transform.rotate(Vector3.Y, 20f);
        instances.add(plaqueInst);

        // Surrounding weathered granite rock boulders
        Model boulderLg = mb.createSphere(2.2f, 1.3f, 1.8f, 10, 8, weatheredStoneMat, attr);
        Model boulderMd = mb.createSphere(1.5f, 0.95f, 1.3f, 8, 6, weatheredStoneMat, attr);
        models.add(boulderLg);
        models.add(boulderMd);

        float[][] boulders = {
            {bsX + 1.8f, 0.55f, bsZ - 0.8f, 0},
            {bsX - 1.5f, 0.40f, bsZ + 1.1f, 1},
            {bsX + 1.4f, 0.65f, bsZ + 1.6f, 0},
            {bsX + 3.0f, 0.70f, bsZ - 2.8f, 0}
        };

        for (float[] b : boulders) {
            ModelInstance bInst = new ModelInstance(b[3] == 0 ? boulderLg : boulderMd);
            bInst.transform.setTranslation(b[0], b[1], b[2]);
            instances.add(bInst);
            // Shrub nestled beside boulder
            addCrossedQuads(mpbBushes, b[0] + 0.5f, 0f, b[2] + 0.5f, 1.6f, 1.1f, 3, 0f);
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
        float pukurX = 18.5f, pukurZ = 10.0f;
        float pukurW = 16.0f, pukurL = 24.0f;

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

        // Water surface plane with lilies
        Model pukurWater = mb.createBox(pukurW - 0.2f, 0.04f, pukurL - 0.2f, curzonPukurWaterMat, attr);
        models.add(pukurWater);
        ModelInstance pwInst = new ModelInstance(pukurWater);
        pwInst.transform.setTranslation(pukurX, 0.12f, pukurZ);
        instances.add(pwInst);

        // End Foliage batch generation & register models
        Model combinedFoliage = mbFoliage.end();
        models.add(combinedFoliage);
        instances.add(new ModelInstance(combinedFoliage));

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
        Model gateTrimFrame = mb.createBox(4.8f, 5.6f, 0.15f, curzonTrimMat, attr);
        Model sidePedArch = mb.createBox(2.4f, 4.4f, 1.6f, curzonBrickMat, attr);
        Model guardKiosk = mb.createBox(2.8f, 3.2f, 2.8f, curzonBrickMat, attr);
        Model guardRoof = mb.createBox(3.4f, 0.4f, 3.4f, curzonTrimMat, attr);
        models.add(gateArchPillar);
        models.add(gateArchBeam);
        models.add(gateArchCrest);
        models.add(gateTrimFrame);
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

        ModelInstance gtInst = new ModelInstance(gateTrimFrame);
        gtInst.transform.setTranslation(0f, 3.6f, 78f);
        instances.add(gtInst);

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
        Model asphaltRoadSouth = mb.createBox(240f, 0.04f, 12f, asphaltMat, attr);
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
        Model northRoad = mb.createBox(220f, 0.04f, 10f, asphaltMat, attr);
        models.add(northWall);
        models.add(northRoad);

        ModelInstance nwInst = new ModelInstance(northWall);
        nwInst.transform.setTranslation(0f, 1.4f, -44.5f);
        instances.add(nwInst);

        ModelInstance nrInst = new ModelInstance(northRoad);
        nrInst.transform.setTranslation(0f, 0.02f, -50f);
        instances.add(nrInst);

        // ==========================================
        // 8. WEST HUB: CENTRAL LIBRARY BUILDING (WITH INNER COURTYARD)
        // ==========================================
        // 4 wings forming rectangular building (36m x 32m x 11.5m) with central open-air atrium (16m x 16m)
        float clX = -68.0f, clZ = 22.0f;
        Model clWingEW = mb.createBox(36f, 11.5f, 8f, curzonBrickMat, attr);
        Model clWingNS = mb.createBox(8f, 11.5f, 16f, curzonBrickMat, attr);
        Model clPortico = mb.createBox(8f, 11.5f, 16f, concreteMat, attr);
        Model clSteps = mb.createBox(5f, 0.6f, 12f, stoneCurb, attr);
        Model clLouver = mb.createBox(0.3f, 8.5f, 0.6f, concreteMat, attr);
        Model clCornice = mb.createBox(38f, 0.8f, 34f, concreteMat, attr);
        models.add(clWingEW);
        models.add(clWingNS);
        models.add(clPortico);
        models.add(clSteps);
        models.add(clLouver);
        models.add(clCornice);

        // South Wing
        ModelInstance clSouth = new ModelInstance(clWingEW);
        clSouth.transform.setTranslation(clX, 5.75f, clZ + 12f);
        instances.add(clSouth);

        // North Wing
        ModelInstance clNorth = new ModelInstance(clWingEW);
        clNorth.transform.setTranslation(clX, 5.75f, clZ - 12f);
        instances.add(clNorth);

        // West Wing
        ModelInstance clWest = new ModelInstance(clWingNS);
        clWest.transform.setTranslation(clX - 14f, 5.75f, clZ);
        instances.add(clWest);

        // East Wing (Facade with concrete portico entrance)
        ModelInstance clEast = new ModelInstance(clPortico);
        clEast.transform.setTranslation(clX + 14f, 5.75f, clZ);
        instances.add(clEast);

        // Concrete roof cornice capping the building
        ModelInstance clcInst = new ModelInstance(clCornice);
        clcInst.transform.setTranslation(clX, 11.8f, clZ);
        instances.add(clcInst);

        // Grand Entrance Steps facing the walkway
        ModelInstance clsInst = new ModelInstance(clSteps);
        clsInst.transform.setTranslation(clX + 19.5f, 0.3f, clZ);
        instances.add(clsInst);

        // Modernist vertical sun louvers on east facade
        for (int l = -5; l <= 5; l++) {
            ModelInstance luv = new ModelInstance(clLouver);
            luv.transform.setTranslation(clX + 18.2f, 5.5f, clZ + l * 2.2f);
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
        Model tscMain = mb.createBox(38f, 9.5f, 30f, concreteMat, attr);
        Model tscAuditorium = mb.createBox(18f, 4.5f, 18f, concreteMat, attr);
        Model tscPyramidRoof = mb.createCone(18f, 3.2f, 18f, 4, canteenRoofMat, attr);
        Model tscTerrace = mb.createBox(12f, 0.5f, 24f, stoneCurb, attr);
        Model tscFins = mb.createBox(0.25f, 7.5f, 0.8f, concreteMat, attr);
        models.add(tscMain);
        models.add(tscAuditorium);
        models.add(tscPyramidRoof);
        models.add(tscTerrace);
        models.add(tscFins);

        // Main modernist building volume
        ModelInstance tmInst = new ModelInstance(tscMain);
        tmInst.transform.setTranslation(tscX, 4.75f, tscZ);
        instances.add(tmInst);

        // Central auditorium rising above roof
        ModelInstance taInst = new ModelInstance(tscAuditorium);
        taInst.transform.setTranslation(tscX, 11.5f, tscZ);
        instances.add(taInst);

        // Pyramidal clerestory roof
        ModelInstance tprInst = new ModelInstance(tscPyramidRoof);
        tprInst.transform.setTranslation(tscX, 15.0f, tscZ);
        instances.add(tprInst);

        // Covered cafeteria entrance terrace facing west towards campus
        ModelInstance ttInst = new ModelInstance(tscTerrace);
        ttInst.transform.setTranslation(tscX - 22f, 0.25f, tscZ);
        instances.add(ttInst);

        // Vertical concrete sun-breaker fins on west facade
        for (int f = -6; f <= 6; f++) {
            ModelInstance fin = new ModelInstance(tscFins);
            fin.transform.setTranslation(tscX - 19.3f, 4.8f, tscZ + f * 2.1f);
            instances.add(fin);
        }

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

        // 7. Vintage Cast-Iron Lampposts with Student Movement Banners
        Model postModel = mb.createCylinder(0.18f, 4.2f, 0.18f, 8, castIron, attr);
        Model lanternModel = mb.createBox(0.48f, 0.52f, 0.48f, lanternGlow, attr);
        Model bannerModel = mb.createBox(0.75f, 1.5f, 0.04f, bannerMat, attr);
        models.add(postModel);
        models.add(lanternModel);
        models.add(bannerModel);

        float[] lampZ = {-10f, 8f, 26f, 44f, 60f};
        for (int i = 0; i < lampZ.length; i++) {
            float lz = lampZ[i];
            for (float lx : new float[]{-5.4f, 5.4f}) {
                ModelInstance pInst = new ModelInstance(postModel);
                pInst.transform.setTranslation(lx, 2.1f, lz);
                instances.add(pInst);

                ModelInstance lInst = new ModelInstance(lanternModel);
                lInst.transform.setTranslation(lx, 4.2f, lz);
                instances.add(lInst);

                // Hanging July 2024 movement banner
                if (i % 2 == 0) {
                    ModelInstance bInst = new ModelInstance(bannerModel);
                    bInst.transform.setTranslation(lx + (lx < 0 ? -0.55f : 0.55f), 2.9f, lz);
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
            ModelInstance benchL = new ModelInstance(benchModel);
            benchL.transform.setTranslation(-7.0f, 0.4f, bz);
            benchL.transform.rotate(Vector3.Y, 90f);
            instances.add(benchL);

            // Bicycle leaning against bench
            ModelInstance bike = new ModelInstance(bikeFrame);
            bike.transform.setTranslation(-8.2f, 0.5f, bz + 1.2f);
            bike.transform.rotate(Vector3.Y, 75f);
            instances.add(bike);
        }
    }

    public void update(float delta) {
        animTime += delta;
        // Gently orbit and hover boundary stone sparkles
        for (int i = 0; i < boundarySparkles.size; i++) {
            ModelInstance sp = boundarySparkles.get(i);
            float angle = animTime * 1.8f + i * (MathUtils.PI2 / boundarySparkles.size);
            float rad = 0.95f + MathUtils.sin(animTime * 2f + i) * 0.15f;
            float px = BOUNDARY_STONE_POS.x + MathUtils.cos(angle) * rad;
            float pz = BOUNDARY_STONE_POS.z + MathUtils.sin(angle) * rad;
            float py = 1.1f + MathUtils.sin(animTime * 2.8f + i * 1.5f) * 0.45f;
            sp.transform.setTranslation(px, py, pz);
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
        for (ModelInstance inst : instances) {
            batch.render(inst, environment);
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
        instances.clear();
        boundarySparkles.clear();
    }
}
