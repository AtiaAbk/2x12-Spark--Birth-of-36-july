package bd.spark36.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Constructs the complete perimeter enclosure for the Dhaka University campus:
 * - East and West colonial red-brick heritage boundary walls with stone coping and piers
 * - Extended North Fuller Road perimeter wall
 * - Multi-layer perimeter backdrop treelines (Rain Trees, Banyans, and Mahogany) sealing off world edges
 * - Solid boundary collision hulls preventing player and camera clipping into the void.
 */
public class PerimeterEnclosure {

    public static void build(ModelBuilder mb,
                             long attr,
                             Material curzonBrickMat,
                             Material stoneCurb,
                             Material barkMat,
                             Material leafClumpMat,
                             Material leafCardMat,
                             Material krishnaClumpMat,
                             Array<Model> models,
                             Array<ModelInstance> instances,
                             java.util.List<float[]> colliders,
                             java.util.function.Consumer<ModelBuilder> foliageRegistrar) {

        // ==========================================
        // 1. EAST & WEST HERITAGE BOUNDARY WALLS
        // ==========================================
        // Wall spans Z = -45m (North Wall) to Z = +78m (South Wall) = Length 123m
        float wallLength = 124.0f;
        float wallCenterZ = 16.5f; // (-45 + 78) / 2 = 16.5
        float wallHeight = 2.8f;
        float wallThickness = 0.6f;

        Model boundWallNS = mb.createBox(wallThickness, wallHeight, wallLength, curzonBrickMat, attr);
        Model wallCopingNS = mb.createBox(wallThickness + 0.22f, 0.20f, wallLength, stoneCurb, attr);
        Model wallPier = mb.createBox(0.95f, 3.2f, 0.95f, curzonBrickMat, attr);
        Model pierCap = mb.createBox(1.15f, 0.25f, 1.15f, stoneCurb, attr);

        models.add(boundWallNS);
        models.add(wallCopingNS);
        models.add(wallPier);
        models.add(pierCap);

        // West Boundary Wall (X = -108.0m)
        float westX = -108.0f;
        ModelInstance westWallInst = new ModelInstance(boundWallNS);
        westWallInst.transform.setTranslation(westX, wallHeight * 0.5f, wallCenterZ);
        instances.add(westWallInst);

        ModelInstance westCopingInst = new ModelInstance(wallCopingNS);
        westCopingInst.transform.setTranslation(westX, wallHeight + 0.10f, wallCenterZ);
        instances.add(westCopingInst);

        // East Boundary Wall (X = +108.0m)
        float eastX = 108.0f;
        ModelInstance eastWallInst = new ModelInstance(boundWallNS);
        eastWallInst.transform.setTranslation(eastX, wallHeight * 0.5f, wallCenterZ);
        instances.add(eastWallInst);

        ModelInstance eastCopingInst = new ModelInstance(wallCopingNS);
        eastCopingInst.transform.setTranslation(eastX, wallHeight + 0.10f, wallCenterZ);
        instances.add(eastCopingInst);

        // Brick wall piers every 12 meters along East and West walls
        for (float pz = -44f; pz <= 78f; pz += 12f) {
            // West pier
            ModelInstance wp = new ModelInstance(wallPier);
            wp.transform.setTranslation(westX, 1.6f, pz);
            instances.add(wp);
            ModelInstance wpc = new ModelInstance(pierCap);
            wpc.transform.setTranslation(westX, 3.32f, pz);
            instances.add(wpc);

            // East pier
            ModelInstance ep = new ModelInstance(wallPier);
            ep.transform.setTranslation(eastX, 1.6f, pz);
            instances.add(ep);
            ModelInstance epc = new ModelInstance(pierCap);
            epc.transform.setTranslation(eastX, 3.32f, pz);
            instances.add(epc);
        }

        // ==========================================
        // 2. PERIMETER COLLIDERS (SOLID WORLD EDGES)
        // ==========================================
        // Format: {minX, minY, minZ, maxX, maxY, maxZ}
        colliders.add(new float[]{westX - 2.0f, 0f, -55.0f, westX + 0.5f, 12.0f, 85.0f}); // West
        colliders.add(new float[]{eastX - 0.5f, 0f, -55.0f, eastX + 2.0f, 12.0f, 85.0f}); // East
        colliders.add(new float[]{-115.0f, 0f, -52.0f, 115.0f, 12.0f, -44.0f});            // North
        colliders.add(new float[]{-115.0f, 0f, 77.0f, -5.0f, 12.0f, 85.0f});              // South West
        colliders.add(new float[]{5.0f, 0f, 77.0f, 115.0f, 12.0f, 85.0f});               // South East

        // ==========================================
        // 3. DENSE PERIMETER BACKDROP TREELINE
        // ==========================================
        // We generate tall, overlapping heritage trees (Rain Trees and Krishnachura)
        // along North, East, and West perimeter bands to naturally mask the horizon.

        // A. North Perimeter Band (Behind North Wall, Z = -48m to -54m)
        for (float nx = -95f; nx <= 95f; nx += 14f) {
            float nz = -48.0f - MathUtils.random(1.0f, 5.0f);
            float scale = 1.15f + MathUtils.random(0.1f, 0.35f);
            float seed = nx * 3.1415f;
            boolean isKrishna = MathUtils.randomBoolean(0.35f);

            ModelBuilder limbsB = new ModelBuilder();
            ModelBuilder coreB = new ModelBuilder();
            ModelBuilder cardsB = new ModelBuilder();
            limbsB.begin();
            coreB.begin();
            cardsB.begin();

            if (isKrishna) {
                TreeGeometry.krishnaTree(
                    limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkMat),
                    coreB.part("core", GL20.GL_TRIANGLES, attr, krishnaClumpMat),
                    cardsB.part("leaves", GL20.GL_TRIANGLES, attr, leafCardMat),
                    nx, nz, scale, seed);
            } else {
                TreeGeometry.rainTree(
                    limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkMat),
                    coreB.part("core", GL20.GL_TRIANGLES, attr, leafClumpMat),
                    cardsB.part("leaves", GL20.GL_TRIANGLES, attr, leafCardMat),
                    nx, nz, scale, seed);
            }
            foliageRegistrar.accept(limbsB);
            foliageRegistrar.accept(coreB);
            foliageRegistrar.accept(cardsB);
        }

        // B. East Perimeter Band (Behind East Wall, X = +102m to +112m)
        for (float ez = -38f; ez <= 70f; ez += 15f) {
            float ex = eastX - 2.0f + MathUtils.random(0.5f, 3.5f);
            float scale = 1.10f + MathUtils.random(0.1f, 0.3f);
            float seed = ez * 2.7182f;

            ModelBuilder limbsB = new ModelBuilder();
            ModelBuilder coreB = new ModelBuilder();
            ModelBuilder cardsB = new ModelBuilder();
            limbsB.begin();
            coreB.begin();
            cardsB.begin();

            TreeGeometry.rainTree(
                limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkMat),
                coreB.part("core", GL20.GL_TRIANGLES, attr, leafClumpMat),
                cardsB.part("leaves", GL20.GL_TRIANGLES, attr, leafCardMat),
                ex, ez, scale, seed);

            foliageRegistrar.accept(limbsB);
            foliageRegistrar.accept(coreB);
            foliageRegistrar.accept(cardsB);
        }

        // C. West Perimeter Band (Behind West Wall, X = -102m to -112m)
        for (float wz = -38f; wz <= 70f; wz += 15f) {
            float wx = westX + 2.0f - MathUtils.random(0.5f, 3.5f);
            float scale = 1.10f + MathUtils.random(0.1f, 0.3f);
            float seed = wz * 1.618f;

            ModelBuilder limbsB = new ModelBuilder();
            ModelBuilder coreB = new ModelBuilder();
            ModelBuilder cardsB = new ModelBuilder();
            limbsB.begin();
            coreB.begin();
            cardsB.begin();

            TreeGeometry.rainTree(
                limbsB.part("limbs", GL20.GL_TRIANGLES, attr, barkMat),
                coreB.part("core", GL20.GL_TRIANGLES, attr, leafClumpMat),
                cardsB.part("leaves", GL20.GL_TRIANGLES, attr, leafCardMat),
                wx, wz, scale, seed);

            foliageRegistrar.accept(limbsB);
            foliageRegistrar.accept(coreB);
            foliageRegistrar.accept(cardsB);
        }
    }
}
