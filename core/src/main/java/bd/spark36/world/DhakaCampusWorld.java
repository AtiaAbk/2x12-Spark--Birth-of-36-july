package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * 3D environment representing Dhaka University Curzon Hall campus with high-fidelity
 * procedural textures, Indo-Saracenic terracotta architecture, tree canopies, vintage
 * lampposts with student movement banners, parked bicycles, and golden-hour lighting.
 */
public class DhakaCampusWorld implements Disposable {

    private final Environment environment;
    private final DirectionalLight sunLight;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();

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
            ColorAttribute.createDiffuse(new Color(0.92f, 0.96f, 0.90f, 1f))
        );

        Material krishnachuraMat = new Material(
            TextureAttribute.createDiffuse(textures.krishnachuraBlossom),
            ColorAttribute.createDiffuse(new Color(0.98f, 0.95f, 0.92f, 1f))
        );

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

        // 1. Central Campus Lawn
        Model groundModel = mb.createBox(280f, 0.2f, 240f, grassMat, attr);
        models.add(groundModel);
        ModelInstance groundInst = new ModelInstance(groundModel);
        groundInst.transform.setTranslation(0f, -0.1f, 15f);
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

        for (float z = -20f; z <= 85f; z += 7.2f) {
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

        // 4. Clustered Rain Trees with Overhanging Canopies
        // 4. Clustered Rain Trees with Overhanging Foliage
        Model trunk = mb.createCylinder(0.95f, 6.0f, 0.95f, 10, treeTrunkMat, attr);
        Model canopyMain = mb.createSphere(7.5f, 5.2f, 7.5f, 14, 12, foliageMat, attr);
        Model canopySide = mb.createSphere(5.6f, 4.2f, 5.6f, 12, 10, foliageMat, attr);
        models.add(trunk);
        models.add(canopyMain);
        models.add(canopySide);

        float[][] treeLocations = {
            {-11f, -4f}, {11f, -4f},
            {-12f, 14f}, {12f, 14f},
            {-13f, 32f}, {13f, 32f},
            {-14f, 50f}, {14f, 50f},
            {-28f, 16f}, {28f, 16f},
            {-48f, 6f}, {48f, 6f},
            {-62f, 30f}, {62f, 30f},
            {-32f, -12f}, {32f, -12f},
            {-22f, 68f}, {22f, 68f},
            {0f, 78f}
        };

        for (float[] loc : treeLocations) {
            float tx = loc[0];
            float tz = loc[1];

            ModelInstance tInst = new ModelInstance(trunk);
            tInst.transform.setTranslation(tx, 3.0f, tz);
            instances.add(tInst);

            // Main center foliage
            ModelInstance cMain = new ModelInstance(canopyMain);
            cMain.transform.setTranslation(tx, 6.6f, tz);
            instances.add(cMain);

            // Asymmetric side clusters for organic feel
            ModelInstance cL = new ModelInstance(canopySide);
            cL.transform.setTranslation(tx - 2.0f, 6.0f, tz + 1.2f);
            instances.add(cL);

            ModelInstance cR = new ModelInstance(canopySide);
            cR.transform.setTranslation(tx + 2.0f, 6.2f, tz - 1.2f);
            instances.add(cR);
        }

        // 5. Vintage Cast-Iron Lampposts with Student Movement Banners
        Model postModel = mb.createCylinder(0.18f, 4.0f, 0.18f, 8, castIron, attr);
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
                pInst.transform.setTranslation(lx, 2.0f, lz);
                instances.add(pInst);

                ModelInstance lInst = new ModelInstance(lanternModel);
                lInst.transform.setTranslation(lx, 4.0f, lz);
                instances.add(lInst);

                // Hanging July 2024 movement banner
                if (i % 2 == 0) {
                    ModelInstance bInst = new ModelInstance(bannerModel);
                    bInst.transform.setTranslation(lx + (lx < 0 ? -0.55f : 0.55f), 2.8f, lz);
                    instances.add(bInst);
                }
            }
        }

        // 6. Park Benches & Student Bicycles
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
    }
}
