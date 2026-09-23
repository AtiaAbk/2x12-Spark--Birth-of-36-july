package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Enhanced 3D environment representing Dhaka University Curzon Hall campus.
 * Features authentic Indo-Saracenic terracotta red-brick architecture, double arches,
 * white ornamental cornices, domes, chhatris, central fountain plaza, national flag,
 * flowerbeds, rain trees, vintage lampposts, and morning golden-hour lighting.
 */
public class DhakaCampusWorld implements Disposable {

    private final Environment environment;
    private final DirectionalLight sunLight;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();

    public DhakaCampusWorld() {
        environment = new Environment();

        // Atmospheric lighting: Warm golden-hour morning sun from south-east
        sunLight = new DirectionalLight();
        sunLight.set(new Color(1.0f, 0.94f, 0.82f, 1f), new Vector3(-0.45f, -0.65f, -0.55f).nor());
        environment.add(sunLight);

        // Soft ambient daylight with warm bounce
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.50f, 0.48f, 0.52f, 1f));

        // Atmospheric depth fog matching horizon dawn
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.88f, 0.82f, 0.72f, 1f));

        buildCampus();
    }

    private void buildCampus() {
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;

        // --- Materials ---
        Material grassMat = new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.46f, 0.20f, 1f))); // Lush lawn
        Material pathMat = new Material(ColorAttribute.createDiffuse(new Color(0.74f, 0.40f, 0.28f, 1f)));  // Terracotta walkway
        Material curbMat = new Material(ColorAttribute.createDiffuse(new Color(0.85f, 0.82f, 0.78f, 1f)));  // Stone curb
        Material curzonBrick = new Material(ColorAttribute.createDiffuse(new Color(0.72f, 0.24f, 0.18f, 1f))); // Rich red brick
        Material curzonTrim = new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.92f, 0.86f, 1f)));  // White ornamental trim
        Material curzonDome = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.85f, 0.78f, 1f)));  // Indo-Saracenic dome
        Material archCavity = new Material(ColorAttribute.createDiffuse(new Color(0.20f, 0.10f, 0.08f, 1f)));  // Shaded verandah depth
        Material teakDoor = new Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.18f, 0.10f, 1f)));    // Teak portal
        Material treeTrunkMat = new Material(ColorAttribute.createDiffuse(new Color(0.30f, 0.20f, 0.14f, 1f))); // Bark
        Material canopyMat1 = new Material(ColorAttribute.createDiffuse(new Color(0.18f, 0.44f, 0.16f, 1f)));  // Rain tree canopy
        Material canopyMat2 = new Material(ColorAttribute.createDiffuse(new Color(0.24f, 0.52f, 0.22f, 1f)));  // Banyan canopy
        Material lampPostMat = new Material(ColorAttribute.createDiffuse(new Color(0.16f, 0.18f, 0.20f, 1f))); // Cast iron
        Material lanternGlow = new Material(ColorAttribute.createDiffuse(new Color(1f, 0.90f, 0.55f, 1f)));   // Golden lantern
        Material waterMat = new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.55f, 0.65f, 1f)));    // Fountain water
        Material flowerMat1 = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.18f, 0.24f, 1f))); // Red blooms
        Material flowerMat2 = new Material(ColorAttribute.createDiffuse(new Color(0.98f, 0.72f, 0.12f, 1f))); // Marigold blooms
        Material flagGreen = new Material(ColorAttribute.createDiffuse(new Color(0.02f, 0.42f, 0.24f, 1f)));  // BD Flag field
        Material flagRed = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.12f, 0.15f, 1f)));    // BD Flag circle

        // 1. Central Campus Lawn & Ground
        Model groundModel = mb.createBox(260f, 0.2f, 220f, grassMat, attr);
        models.add(groundModel);
        ModelInstance groundInst = new ModelInstance(groundModel);
        groundInst.transform.setTranslation(0f, -0.1f, 15f);
        instances.add(groundInst);

        // 2. Brick Paved Walkways with Stone Curbs
        Model mainPathModel = mb.createBox(6.8f, 0.06f, 130f, pathMat, attr);
        Model mainCurbModel = mb.createBox(0.35f, 0.12f, 130f, curbMat, attr);
        models.add(mainPathModel);
        models.add(mainCurbModel);

        ModelInstance mainPathInst = new ModelInstance(mainPathModel);
        mainPathInst.transform.setTranslation(0f, 0.03f, 22f);
        instances.add(mainPathInst);

        // Curbs along central path
        ModelInstance curbL = new ModelInstance(mainCurbModel);
        curbL.transform.setTranslation(-3.55f, 0.06f, 22f);
        instances.add(curbL);

        ModelInstance curbR = new ModelInstance(mainCurbModel);
        curbR.transform.setTranslation(3.55f, 0.06f, 22f);
        instances.add(curbR);

        // East-West Crosswalk
        Model crossPathModel = mb.createBox(150f, 0.06f, 5.2f, pathMat, attr);
        models.add(crossPathModel);
        ModelInstance crossPathInst = new ModelInstance(crossPathModel);
        crossPathInst.transform.setTranslation(0f, 0.03f, 18f);
        instances.add(crossPathInst);

        // Curzon Front Verandah Walkway
        Model frontWalkModel = mb.createBox(96f, 0.06f, 5.8f, pathMat, attr);
        models.add(frontWalkModel);
        ModelInstance frontWalkInst = new ModelInstance(frontWalkModel);
        frontWalkInst.transform.setTranslation(0f, 0.03f, -17f);
        instances.add(frontWalkInst);

        // 3. Central Fountain & Plaza Pool (at x=0, z=5)
        Model poolRim = mb.createCylinder(13f, 0.5f, 13f, 28, curbMat, attr);
        Model poolWater = mb.createCylinder(12f, 0.42f, 12f, 28, waterMat, attr);
        Model fountainPillar = mb.createCylinder(1.6f, 2.2f, 1.6f, 16, curbMat, attr);
        models.add(poolRim);
        models.add(poolWater);
        models.add(fountainPillar);

        ModelInstance rimInst = new ModelInstance(poolRim);
        rimInst.transform.setTranslation(0f, 0.25f, 2f);
        instances.add(rimInst);

        ModelInstance waterInst = new ModelInstance(poolWater);
        waterInst.transform.setTranslation(0f, 0.23f, 2f);
        instances.add(waterInst);

        ModelInstance pillarInst = new ModelInstance(fountainPillar);
        pillarInst.transform.setTranslation(0f, 1.1f, 2f);
        instances.add(pillarInst);

        // 4. Curzon Hall Indo-Saracenic Central Building Complex
        // Central Block
        Model curzonBlock = mb.createBox(38f, 13.5f, 17f, curzonBrick, attr);
        models.add(curzonBlock);
        ModelInstance mainBlockInst = new ModelInstance(curzonBlock);
        mainBlockInst.transform.setTranslation(0f, 6.75f, -32f);
        instances.add(mainBlockInst);

        // East & West Wings
        Model curzonWing = mb.createBox(28f, 11.2f, 15f, curzonBrick, attr);
        models.add(curzonWing);

        ModelInstance eastWingInst = new ModelInstance(curzonWing);
        eastWingInst.transform.setTranslation(32f, 5.6f, -31f);
        instances.add(eastWingInst);

        ModelInstance westWingInst = new ModelInstance(curzonWing);
        westWingInst.transform.setTranslation(-32f, 5.6f, -31f);
        instances.add(westWingInst);

        // Plinth & Grand Steps leading to Portico
        Model steps = mb.createBox(16f, 0.6f, 4.0f, curbMat, attr);
        models.add(steps);
        ModelInstance stepsInst = new ModelInstance(steps);
        stepsInst.transform.setTranslation(0f, 0.3f, -19.5f);
        instances.add(stepsInst);

        // White Ornamental Cornices & Parapets
        Model cornice = mb.createBox(96f, 0.7f, 18f, curzonTrim, attr);
        models.add(cornice);
        ModelInstance corniceInst = new ModelInstance(cornice);
        corniceInst.transform.setTranslation(0f, 11.8f, -31.5f);
        instances.add(corniceInst);

        // Central Facade Portico Projection
        Model portico = mb.createBox(15f, 15.2f, 3.8f, curzonBrick, attr);
        models.add(portico);
        ModelInstance porticoInst = new ModelInstance(portico);
        porticoInst.transform.setTranslation(0f, 7.6f, -22.5f);
        instances.add(porticoInst);

        // Verandah Double Arches
        Model archNiche = mb.createBox(2.8f, 5.8f, 0.4f, archCavity, attr);
        models.add(archNiche);
        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue;
            ModelInstance arch = new ModelInstance(archNiche);
            arch.transform.setTranslation(i * 4.4f, 3.6f, -23.8f);
            instances.add(arch);
        }

        // Grand Central Portal with Teak Door & White Trim
        Model mainArch = mb.createBox(4.6f, 7.2f, 0.6f, archCavity, attr);
        Model doorLeaf = mb.createBox(3.8f, 6.2f, 0.3f, teakDoor, attr);
        models.add(mainArch);
        models.add(doorLeaf);

        ModelInstance mainArchInst = new ModelInstance(mainArch);
        mainArchInst.transform.setTranslation(0f, 4.0f, -20.6f);
        instances.add(mainArchInst);

        ModelInstance doorInst = new ModelInstance(doorLeaf);
        doorInst.transform.setTranslation(0f, 3.6f, -20.4f);
        instances.add(doorInst);

        // Central Dome on Top of Portico
        Model domeBase = mb.createCylinder(8.0f, 2.4f, 8.0f, 20, curzonTrim, attr);
        Model domeSphere = mb.createSphere(7.6f, 5.6f, 7.6f, 20, 20, curzonDome, attr);
        models.add(domeBase);
        models.add(domeSphere);

        ModelInstance domeBaseInst = new ModelInstance(domeBase);
        domeBaseInst.transform.setTranslation(0f, 16.4f, -22.5f);
        instances.add(domeBaseInst);

        ModelInstance domeInst = new ModelInstance(domeSphere);
        domeInst.transform.setTranslation(0f, 19.4f, -22.5f);
        instances.add(domeInst);

        // Flagpole & Bangladesh National Flag flying on the Central Dome
        Model flagPole = mb.createCylinder(0.12f, 6.0f, 0.12f, 8, curbMat, attr);
        Model flagBanner = mb.createBox(2.8f, 1.6f, 0.05f, flagGreen, attr);
        Model flagDisc = mb.createCylinder(0.9f, 0.06f, 0.9f, 16, flagRed, attr);
        models.add(flagPole);
        models.add(flagBanner);
        models.add(flagDisc);

        ModelInstance poleInst = new ModelInstance(flagPole);
        poleInst.transform.setTranslation(0f, 24.5f, -22.5f);
        instances.add(poleInst);

        ModelInstance flagInst = new ModelInstance(flagBanner);
        flagInst.transform.setTranslation(1.4f, 25.5f, -22.5f);
        instances.add(flagInst);

        ModelInstance discInst = new ModelInstance(flagDisc);
        discInst.transform.setTranslation(1.2f, 25.5f, -22.45f);
        discInst.transform.rotate(Vector3.X, 90f);
        instances.add(discInst);

        // Corner Chhatris (4 authentic Indo-Saracenic domed kiosks)
        Model chhatriDome = mb.createSphere(3.2f, 2.5f, 3.2f, 16, 14, curzonDome, attr);
        Model chhatriPillar = mb.createBox(0.45f, 3.4f, 0.45f, curzonTrim, attr);
        models.add(chhatriDome);
        models.add(chhatriPillar);

        float[] chhatriX = {-18.5f, 18.5f, -44.5f, 44.5f};
        for (float cx : chhatriX) {
            float cz = -24.5f;
            ModelInstance cd = new ModelInstance(chhatriDome);
            cd.transform.setTranslation(cx, 15.6f, cz);
            instances.add(cd);

            float[] ox = {-1.0f, 1.0f};
            float[] oz = {-1.0f, 1.0f};
            for (float px : ox) {
                for (float pz : oz) {
                    ModelInstance cp = new ModelInstance(chhatriPillar);
                    cp.transform.setTranslation(cx + px, 12.8f, cz + pz);
                    instances.add(cp);
                }
            }
        }

        // 5. Flowerbeds bordering Walkways
        Model redBed = mb.createBox(1.2f, 0.25f, 24f, flowerMat1, attr);
        Model goldBed = mb.createBox(1.2f, 0.25f, 24f, flowerMat2, attr);
        models.add(redBed);
        models.add(goldBed);

        ModelInstance fb1 = new ModelInstance(redBed);
        fb1.transform.setTranslation(-4.5f, 0.12f, 22f);
        instances.add(fb1);

        ModelInstance fb2 = new ModelInstance(goldBed);
        fb2.transform.setTranslation(4.5f, 0.12f, 22f);
        instances.add(fb2);

        // 6. Lush Rain Trees and Banyan Trees
        Model trunk = mb.createCylinder(0.9f, 5.5f, 0.9f, 10, treeTrunkMat, attr);
        Model canopy1 = mb.createSphere(7.2f, 4.8f, 7.2f, 14, 12, canopyMat1, attr);
        Model canopy2 = mb.createSphere(8.6f, 5.6f, 8.6f, 14, 12, canopyMat2, attr);
        models.add(trunk);
        models.add(canopy1);
        models.add(canopy2);

        float[][] treeLocations = {
            {-10f, -6f}, {10f, -6f},
            {-12f, 12f}, {12f, 12f},
            {-12f, 32f}, {12f, 32f},
            {-14f, 52f}, {14f, 52f},
            {-28f, 14f}, {28f, 14f},
            {-48f, 5f}, {48f, 5f},
            {-62f, 28f}, {62f, 28f},
            {-32f, -12f}, {32f, -12f},
            {-55f, -8f}, {55f, -8f},
            {-22f, 68f}, {22f, 68f},
            {0f, 78f}
        };

        for (int i = 0; i < treeLocations.length; i++) {
            float tx = treeLocations[i][0];
            float tz = treeLocations[i][1];

            ModelInstance tInst = new ModelInstance(trunk);
            tInst.transform.setTranslation(tx, 2.75f, tz);
            instances.add(tInst);

            Model foliageModel = (i % 2 == 0) ? canopy1 : canopy2;
            ModelInstance cInst = new ModelInstance(foliageModel);
            cInst.transform.setTranslation(tx, 6.4f, tz);
            instances.add(cInst);
        }

        // 7. Vintage Cast-Iron Lampposts along Promenade
        Model postModel = mb.createCylinder(0.18f, 3.8f, 0.18f, 8, lampPostMat, attr);
        Model lanternModel = mb.createBox(0.48f, 0.52f, 0.48f, lanternGlow, attr);
        models.add(postModel);
        models.add(lanternModel);

        float[] lampZ = {-12f, 8f, 26f, 44f, 60f};
        for (float lz : lampZ) {
            for (float lx : new float[]{-5.8f, 5.8f}) {
                ModelInstance pInst = new ModelInstance(postModel);
                pInst.transform.setTranslation(lx, 1.9f, lz);
                instances.add(pInst);

                ModelInstance lInst = new ModelInstance(lanternModel);
                lInst.transform.setTranslation(lx, 3.8f, lz);
                instances.add(lInst);
            }
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
