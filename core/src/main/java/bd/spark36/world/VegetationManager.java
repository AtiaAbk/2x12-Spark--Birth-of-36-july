// ei file ta campus er shob gach manage ar update kore
package bd.spark36.world;

import bd.spark36.render.GraphicsTier;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class VegetationManager implements Disposable {

    private final WindModel windModel = new WindModel();
    private final Array<TreeEntity> trees = new Array<>();
    private final Array<Model> treeModels = new Array<>();

    public VegetationManager() {}

    public WindModel getWindModel() {
        return windModel;
    }

    public Array<TreeEntity> getTrees() {
        return trees;
    }

    public void buildCampusTrees(float[][] treeLocations,
                                 Material barkTwoSidedMat,
                                 Material leafClumpMat,
                                 Material leafCardMat,
                                 Material blossomClumpMat,
                                 Material whiteBlossomClumpMat,
                                 Material whiteBlossomCardMat,
                                 Material foliageMat,
                                 java.util.List<float[]> colliders) {
        buildCampusTrees(treeLocations, barkTwoSidedMat, leafClumpMat, leafCardMat,
                         blossomClumpMat, whiteBlossomClumpMat, whiteBlossomCardMat,
                         foliageMat, colliders, GraphicsTier.fromSystemProperty());
    }

    public void buildCampusTrees(float[][] treeLocations,
                                 Material barkTwoSidedMat,
                                 Material leafClumpMat,
                                 Material leafCardMat,
                                 Material blossomClumpMat,
                                 Material whiteBlossomClumpMat,
                                 Material whiteBlossomCardMat,
                                 Material foliageMat,
                                 java.util.List<float[]> colliders,
                                 GraphicsTier tier) {
        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        int treeIndex = 0;
        for (float[] loc : treeLocations) {
            float tx = loc[0];
            float tz = loc[1];
            int type = (int) loc[2];

            float seed = treeIndex * 2.399f;
            float scale = 0.9f + 0.28f * (0.5f + 0.5f * MathUtils.sin(treeIndex * 1.7f));
            treeIndex++;

            float baseDensity = (treeIndex % 3 == 1) ? 0.55f : 1f;
            TreeGeometry.leafDensity = baseDensity * (tier != null ? tier.vegetationDensity : 1.0f);

            float trunkHalf = (type == 2) ? 0.45f : 0.75f * scale;
            if (colliders != null) {
                colliders.add(new float[]{tx - trunkHalf, 0f, tz - trunkHalf, tx + trunkHalf, 6f, tz + trunkHalf});
            }

            if (type == 3) {

                ModelBuilder limbsB = new ModelBuilder();
                ModelBuilder coreB = new ModelBuilder();
                ModelBuilder cardsB = new ModelBuilder();
                limbsB.begin(); coreB.begin(); cardsB.begin();

                TreeGeometry.whiteBlossomTree(
                    limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkTwoSidedMat),
                    coreB.part("blossomCore", GL20.GL_TRIANGLES, attr, whiteBlossomClumpMat),
                    cardsB.part("blossomCards", GL20.GL_TRIANGLES, attr, whiteBlossomCardMat),
                    0f, 0f, scale, seed
                );

                Model limbsM = limbsB.end();
                Model coreM = coreB.end();
                Model cardsM = cardsB.end();
                treeModels.add(limbsM);
                treeModels.add(coreM);
                treeModels.add(cardsM);

                TreeEntity tree = new TreeEntity(
                    VegetationConfig.SPECIES_WHITE_BLOSSOM,
                    tx, tz, scale, seed, 3.8f * scale,
                    new ModelInstance(limbsM),
                    new ModelInstance(coreM),
                    new ModelInstance(cardsM)
                );
                trees.add(tree);

            } else if (type == 1) {

                ModelBuilder limbsB = new ModelBuilder();
                ModelBuilder coreB = new ModelBuilder();
                ModelBuilder cardsB = new ModelBuilder();
                limbsB.begin(); coreB.begin(); cardsB.begin();

                TreeGeometry.krishnaTree(
                    limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkTwoSidedMat),
                    coreB.part("core", GL20.GL_TRIANGLES, attr, blossomClumpMat),
                    cardsB.part("leaves", GL20.GL_TRIANGLES, attr, leafCardMat),
                    0f, 0f, scale, seed
                );

                Model limbsM = limbsB.end();
                Model coreM = coreB.end();
                Model cardsM = cardsB.end();
                treeModels.add(limbsM);
                treeModels.add(coreM);
                treeModels.add(cardsM);

                TreeEntity tree = new TreeEntity(
                    VegetationConfig.SPECIES_KRISHNACHURA,
                    tx, tz, scale, seed, 3.2f * scale,
                    new ModelInstance(limbsM),
                    new ModelInstance(coreM),
                    new ModelInstance(cardsM)
                );
                trees.add(tree);

            } else if (type == 0) {

                ModelBuilder limbsB = new ModelBuilder();
                ModelBuilder coreB = new ModelBuilder();
                ModelBuilder cardsB = new ModelBuilder();
                limbsB.begin(); coreB.begin(); cardsB.begin();

                TreeGeometry.rainTree(
                    limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkTwoSidedMat),
                    coreB.part("core", GL20.GL_TRIANGLES, attr, leafClumpMat),
                    cardsB.part("leaves", GL20.GL_TRIANGLES, attr, leafCardMat),
                    0f, 0f, scale, seed
                );

                Model limbsM = limbsB.end();
                Model coreM = coreB.end();
                Model cardsM = cardsB.end();
                treeModels.add(limbsM);
                treeModels.add(coreM);
                treeModels.add(cardsM);

                TreeEntity tree = new TreeEntity(
                    VegetationConfig.SPECIES_RAIN_TREE,
                    tx, tz, scale, seed, 3.8f * scale,
                    new ModelInstance(limbsM),
                    new ModelInstance(coreM),
                    new ModelInstance(cardsM)
                );
                trees.add(tree);

            } else {

                TreeGeometry.leafDensity = 1f * (tier != null ? tier.vegetationDensity : 1.0f);
                ModelBuilder trunkB = new ModelBuilder();
                ModelBuilder frondsB = new ModelBuilder();
                trunkB.begin(); frondsB.begin();

                TreeGeometry.palmTree(
                    trunkB.part("trunk", GL20.GL_TRIANGLES, attr, barkTwoSidedMat),
                    null,
                    frondsB.part("fronds", GL20.GL_TRIANGLES, attr, foliageMat),
                    0f, 0f, scale, seed
                );

                Model trunkM = trunkB.end();
                Model frondsM = frondsB.end();
                treeModels.add(trunkM);
                treeModels.add(frondsM);

                TreeEntity tree = new TreeEntity(
                    VegetationConfig.SPECIES_ROYAL_PALM,
                    tx, tz, scale, seed, 9.2f * scale,
                    new ModelInstance(trunkM),
                    new ModelInstance(frondsM),
                    null
                );
                trees.add(tree);
            }
        }
        TreeGeometry.leafDensity = 1f;
    }

    public void update(float animTime, GraphicsTier tier, Camera camera) {
        for (int i = 0; i < trees.size; i++) {
            trees.get(i).update(animTime, windModel, tier, camera);
        }
    }

    public void render(ModelBatch batch, Environment env, Camera camera) {
        for (int i = 0; i < trees.size; i++) {
            TreeEntity tree = trees.get(i);
            if (camera != null && !camera.frustum.boundsInFrustum(tree.bounds)) {
                continue;
            }
            tree.render(batch, env);
        }
    }

    public void renderShadowCasters(ModelBatch depthBatch) {
        for (int i = 0; i < trees.size; i++) {
            trees.get(i).renderShadowCaster(depthBatch);
        }
    }

    @Override
    public void dispose() {
        for (int i = 0; i < treeModels.size; i++) {
            treeModels.get(i).dispose();
        }
        treeModels.clear();
        trees.clear();
    }
}
