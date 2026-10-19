package bd.spark36.world;

import bd.spark36.render.GraphicsTier;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Set;

/**
 * High-fidelity, continuous campus terrain and surface blending system.
 * Replaces primitive stretched boxes with a structured, multi-zone terrain grid
 * featuring real-world UV texture scaling, PBR normal mapping, organic tree-root
 * soil collars, walkway dirt shoulders, and Curzon pond embankment blending.
 */
public class TerrainSurface implements Disposable {

    private final Array<Model> models = new Array<>();

    private static final VertexInfo V0 = new VertexInfo();
    private static final VertexInfo V1 = new VertexInfo();
    private static final VertexInfo V2 = new VertexInfo();
    private static final VertexInfo V3 = new VertexInfo();

    public TerrainSurface() {}

    /**
     * Builds the continuous, multi-quad terrain mesh for the University of Dhaka Curzon Hall precinct.
     * Generates real-world UV coordinates (1 repeat every 4m), hollows out the Curzon Pond basin,
     * and adds edge skirts to prevent camera underside peeking.
     */
    public ModelInstance buildTerrain(ModelBuilder mb, TextureFactory textures, GraphicsTier tier, long baseAttr) {
        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        Material grassMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.lawnGrass),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.92f, 0.95f, 0.90f, 1f))
        );
        if (textures.lawnGrassNormal != null) {
            grassMat.set(PBRTextureAttribute.createNormalTexture(textures.lawnGrassNormal));
        }
        setupPbr(grassMat, GroundConfig.GRASS_METALLIC, GroundConfig.GRASS_ROUGHNESS);

        mb.begin();
        MeshPartBuilder mpb = mb.part("campusTerrainGrid", GL20.GL_TRIANGLES, attr, grassMat);

        float minX = GroundConfig.CAMPUS_MIN_X;
        float maxX = GroundConfig.CAMPUS_MAX_X;
        float minZ = GroundConfig.CAMPUS_MIN_Z;
        float maxZ = GroundConfig.CAMPUS_MAX_Z;
        float step = tier.terrainGridResolution; // 4.0m to 5.0m grid cells

        float uvScale = GroundConfig.GRASS_UV_SCALE;

        // Structured quad grid with pond cutout
        for (float z = minZ; z < maxZ; z += step) {
            float zNext = Math.min(z + step, maxZ);
            for (float x = minX; x < maxX; x += step) {
                float xNext = Math.min(x + step, maxX);

                // Omit top quad if inside Curzon Pond basin
                if (isInsidePond(x, z, xNext, zNext)) {
                    continue;
                }

                float u0 = (x - minX) * uvScale;
                float v0 = (z - minZ) * uvScale;
                float u1 = (xNext - minX) * uvScale;
                float v1 = (zNext - minZ) * uvScale;

                // CCW winding convention matching LibGDX box top face:
                // (x, z) -> (x, zNext) -> (xNext, zNext) -> (xNext, z)
                V0.setPos(x,     0.0f, z).setNor(0f, 1f, 0f).setUV(u0, v0);
                V1.setPos(x,     0.0f, zNext).setNor(0f, 1f, 0f).setUV(u0, v1);
                V2.setPos(xNext, 0.0f, zNext).setNor(0f, 1f, 0f).setUV(u1, v1);
                V3.setPos(xNext, 0.0f, z).setNor(0f, 1f, 0f).setUV(u1, v0);

                mpb.rect(V0, V1, V2, V3);
            }
        }

        // Perimeter boundary vertical skirts down to Y = -0.30m
        float skirtY = -0.30f;
        // North skirt (Z = minZ)
        V0.setPos(minX, skirtY, minZ).setNor(0f, 0f, -1f).setUV(0f, 1f);
        V1.setPos(maxX, skirtY, minZ).setNor(0f, 0f, -1f).setUV(1f, 1f);
        V2.setPos(maxX, 0.0f,   minZ).setNor(0f, 0f, -1f).setUV(1f, 0f);
        V3.setPos(minX, 0.0f,   minZ).setNor(0f, 0f, -1f).setUV(0f, 0f);
        mpb.rect(V0, V1, V2, V3);

        // South skirt (Z = maxZ)
        V0.setPos(maxX, skirtY, maxZ).setNor(0f, 0f, 1f).setUV(0f, 1f);
        V1.setPos(minX, skirtY, maxZ).setNor(0f, 0f, 1f).setUV(1f, 1f);
        V2.setPos(minX, 0.0f,   maxZ).setNor(0f, 0f, 1f).setUV(1f, 0f);
        V3.setPos(maxX, 0.0f,   maxZ).setNor(0f, 0f, 1f).setUV(0f, 0f);
        mpb.rect(V0, V1, V2, V3);

        // West skirt (X = minX)
        V0.setPos(minX, skirtY, maxZ).setNor(-1f, 0f, 0f).setUV(0f, 1f);
        V1.setPos(minX, skirtY, minZ).setNor(-1f, 0f, 0f).setUV(1f, 1f);
        V2.setPos(minX, 0.0f,   minZ).setNor(-1f, 0f, 0f).setUV(1f, 0f);
        V3.setPos(minX, 0.0f,   maxZ).setNor(-1f, 0f, 0f).setUV(0f, 0f);
        mpb.rect(V0, V1, V2, V3);

        // East skirt (X = maxX)
        V0.setPos(maxX, skirtY, minZ).setNor(1f, 0f, 0f).setUV(0f, 1f);
        V1.setPos(maxX, skirtY, maxZ).setNor(1f, 0f, 0f).setUV(1f, 1f);
        V2.setPos(maxX, 0.0f,   maxZ).setNor(1f, 0f, 0f).setUV(1f, 0f);
        V3.setPos(maxX, 0.0f,   minZ).setNor(1f, 0f, 0f).setUV(0f, 0f);
        mpb.rect(V0, V1, V2, V3);

        Model terrainModel = mb.end();
        models.add(terrainModel);
        return new ModelInstance(terrainModel);
    }

    /**
     * Builds organic surface transitions:
     * 1. Tree root soil collars (alluvial humus discs under every campus tree).
     * 2. Promenade and walkway dirt shoulders.
     * 3. Pond verge moisture gradient along the Curzon Pukur stone curbs.
     * 4. Road shoulders along campus asphalt boundaries.
     */
    public void buildSurfaceTransitions(ModelBuilder mb, TextureFactory textures, float[][] treeLocations,
                                        GraphicsTier tier, Array<ModelInstance> outInstances,
                                        Set<ModelInstance> nonSolid, Set<ModelInstance> shadowExclusions) {
        if (!tier.enableTerrainTransitions) {
            return;
        }

        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates | Usage.ColorUnpacked;

        // Material 1: Alluvial Tree Soil / Humus Collar with feathered radial alpha
        Material soilCollarMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.soilLoam),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f))
        );
        if (textures.soilLoamNormal != null) {
            soilCollarMat.set(PBRTextureAttribute.createNormalTexture(textures.soilLoamNormal));
        }
        setupPbr(soilCollarMat, GroundConfig.SOIL_METALLIC, GroundConfig.SOIL_ROUGHNESS);

        // Material 2: Walkway & Road Compacted Dirt Shoulder
        Material dirtShoulderMat = new Material(
            PBRTextureAttribute.createBaseColorTexture(textures.dirtPath),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
            IntAttribute.createCullFace(0),
            ColorAttribute.createDiffuse(new Color(0.98f, 0.96f, 0.92f, 1f))
        );
        if (textures.dirtPathNormal != null) {
            dirtShoulderMat.set(PBRTextureAttribute.createNormalTexture(textures.dirtPathNormal));
        }
        setupPbr(dirtShoulderMat, GroundConfig.DIRT_METALLIC, GroundConfig.DIRT_ROUGHNESS);

        // A. TREE ROOT SOIL COLLARS
        mb.begin();
        MeshPartBuilder mpbSoil = mb.part("treeSoilCollars", GL20.GL_TRIANGLES, attr, soilCollarMat);

        float yOffset = GroundConfig.TRANSITION_Y_OFFSET;
        int segments = 16;
        float angleStep = MathUtils.PI2 / segments;
        Color centerColor = new Color(0.95f, 0.95f, 0.95f, 0.92f);
        Color outerColor = new Color(0.95f, 0.95f, 0.95f, 0.0f); // feathered to grass

        for (float[] loc : treeLocations) {
            float tx = loc[0];
            float tz = loc[1];
            int species = (int) loc[2];

            float radius = (species == 0) ? GroundConfig.TREE_SOIL_RADIUS_RAIN_TREE :
                           (species == 1) ? GroundConfig.TREE_SOIL_RADIUS_KRISHNA :
                           (species == 3) ? GroundConfig.TREE_SOIL_RADIUS_WHITE_BLOSSOM :
                                            GroundConfig.TREE_SOIL_RADIUS_PALM;

            float uCenter = tx * GroundConfig.SOIL_UV_SCALE;
            float vCenter = tz * GroundConfig.SOIL_UV_SCALE;

            V0.setPos(tx, yOffset, tz).setNor(0f, 1f, 0f).setUV(uCenter, vCenter).setCol(centerColor);

            for (int i = 0; i < segments; i++) {
                float a1 = i * angleStep;
                float a2 = (i + 1) * angleStep;

                // Subtle organic perturbation to radial boundary
                float r1 = radius * (0.92f + 0.16f * MathUtils.sin(i * 1.7f + tx));
                float r2 = radius * (0.92f + 0.16f * MathUtils.sin((i + 1) * 1.7f + tx));

                float x1 = tx + MathUtils.cos(a1) * r1;
                float z1 = tz + MathUtils.sin(a1) * r1;
                float x2 = tx + MathUtils.cos(a2) * r2;
                float z2 = tz + MathUtils.sin(a2) * r2;

                float u1 = x1 * GroundConfig.SOIL_UV_SCALE;
                float v1 = z1 * GroundConfig.SOIL_UV_SCALE;
                float u2 = x2 * GroundConfig.SOIL_UV_SCALE;
                float v2 = z2 * GroundConfig.SOIL_UV_SCALE;

                V1.setPos(x1, yOffset, z1).setNor(0f, 1f, 0f).setUV(u1, v1).setCol(outerColor);
                V2.setPos(x2, yOffset, z2).setNor(0f, 1f, 0f).setUV(u2, v2).setCol(outerColor);

                mpbSoil.triangle(V0, V2, V1);
            }
        }

        Model soilModel = mb.end();
        models.add(soilModel);
        ModelInstance soilInst = new ModelInstance(soilModel);
        outInstances.add(soilInst);
        nonSolid.add(soilInst);
        shadowExclusions.add(soilInst);

        // B. PROMENADE & WALKWAY DIRT SHOULDERS
        mb.begin();
        MeshPartBuilder mpbShoulder = mb.part("walkwayShoulders", GL20.GL_TRIANGLES, attr, dirtShoulderMat);
        Color curbEdgeCol = new Color(0.92f, 0.90f, 0.85f, 0.82f);
        Color turfEdgeCol = new Color(0.92f, 0.90f, 0.85f, 0.0f);

        // Central promenade shoulders (Z: -18 to 76m, excluding pond basin Z: -6 to 20m)
        float shoulderW = GroundConfig.PATH_SHOULDER_WIDTH;
        float halfWalk = 3.75f; // Stone curb outer edge
        float tileLen = 3.6f;

        for (float z = -18f; z <= 76f; z += tileLen) {
            if (z > -6f && z < 20f) continue; // Curzon pond area handled separately
            boolean atCrossing = z > 20f && z < 36f;
            if (atCrossing) continue;

            float zEnd = Math.min(z + tileLen, 76f);
            float v0 = z * GroundConfig.DIRT_UV_SCALE;
            float v1 = zEnd * GroundConfig.DIRT_UV_SCALE;

            // West shoulder (Left of curb at X = -halfWalk): +Z x -X = +Y (UP)
            V0.setPos(-halfWalk, yOffset, z).setNor(0f, 1f, 0f).setUV(0f, v0).setCol(curbEdgeCol);
            V1.setPos(-halfWalk, yOffset, zEnd).setNor(0f, 1f, 0f).setUV(0f, v1).setCol(curbEdgeCol);
            V2.setPos(-halfWalk - shoulderW, yOffset, zEnd).setNor(0f, 1f, 0f).setUV(1f, v1).setCol(turfEdgeCol);
            V3.setPos(-halfWalk - shoulderW, yOffset, z).setNor(0f, 1f, 0f).setUV(1f, v0).setCol(turfEdgeCol);
            mpbShoulder.rect(V0, V1, V2, V3);

            // East shoulder (Right of curb at X = +halfWalk): -Z x +X = +Y (UP)
            V0.setPos(halfWalk, yOffset, zEnd).setNor(0f, 1f, 0f).setUV(0f, v1).setCol(curbEdgeCol);
            V1.setPos(halfWalk, yOffset, z).setNor(0f, 1f, 0f).setUV(0f, v0).setCol(curbEdgeCol);
            V2.setPos(halfWalk + shoulderW, yOffset, z).setNor(0f, 1f, 0f).setUV(1f, v0).setCol(turfEdgeCol);
            V3.setPos(halfWalk + shoulderW, yOffset, zEnd).setNor(0f, 1f, 0f).setUV(1f, v1).setCol(turfEdgeCol);
            mpbShoulder.rect(V0, V1, V2, V3);
        }

        // C. CURZON POND VERGE EMBANKMENT
        // Around stone curbs (X in [-8.8, 8.8], Z in [-5.8, 17.8])
        float pMinX = GroundConfig.POND_MIN_X - 0.3f;
        float pMaxX = GroundConfig.POND_MAX_X + 0.3f;
        float pMinZ = GroundConfig.POND_MIN_Z - 0.3f;
        float pMaxZ = GroundConfig.POND_MAX_Z + 0.3f;
        float vergeW = GroundConfig.POND_VERGE_WIDTH;

        // North curb verge: -X x -Z = +Y (UP)
        V0.setPos(pMaxX, yOffset, pMinZ).setNor(0f, 1f, 0f).setUV(2f, 0f).setCol(curbEdgeCol);
        V1.setPos(pMinX, yOffset, pMinZ).setNor(0f, 1f, 0f).setUV(0f, 0f).setCol(curbEdgeCol);
        V2.setPos(pMinX, yOffset, pMinZ - vergeW).setNor(0f, 1f, 0f).setUV(0f, 1f).setCol(turfEdgeCol);
        V3.setPos(pMaxX, yOffset, pMinZ - vergeW).setNor(0f, 1f, 0f).setUV(2f, 1f).setCol(turfEdgeCol);
        mpbShoulder.rect(V0, V1, V2, V3);

        // South curb verge: +X x +Z = +Y (UP)
        V0.setPos(pMinX, yOffset, pMaxZ).setNor(0f, 1f, 0f).setUV(0f, 0f).setCol(curbEdgeCol);
        V1.setPos(pMaxX, yOffset, pMaxZ).setNor(0f, 1f, 0f).setUV(2f, 0f).setCol(curbEdgeCol);
        V2.setPos(pMaxX, yOffset, pMaxZ + vergeW).setNor(0f, 1f, 0f).setUV(2f, 1f).setCol(turfEdgeCol);
        V3.setPos(pMinX, yOffset, pMaxZ + vergeW).setNor(0f, 1f, 0f).setUV(0f, 1f).setCol(turfEdgeCol);
        mpbShoulder.rect(V0, V1, V2, V3);

        // West curb verge: +Z x -X = +Y (UP)
        V0.setPos(pMinX, yOffset, pMinZ).setNor(0f, 1f, 0f).setUV(0f, 0f).setCol(curbEdgeCol);
        V1.setPos(pMinX, yOffset, pMaxZ).setNor(0f, 1f, 0f).setUV(3f, 0f).setCol(curbEdgeCol);
        V2.setPos(pMinX - vergeW, yOffset, pMaxZ).setNor(0f, 1f, 0f).setUV(3f, 1f).setCol(turfEdgeCol);
        V3.setPos(pMinX - vergeW, yOffset, pMinZ).setNor(0f, 1f, 0f).setUV(0f, 1f).setCol(turfEdgeCol);
        mpbShoulder.rect(V0, V1, V2, V3);

        // East curb verge: -Z x +X = +Y (UP)
        V0.setPos(pMaxX, yOffset, pMaxZ).setNor(0f, 1f, 0f).setUV(3f, 0f).setCol(curbEdgeCol);
        V1.setPos(pMaxX, yOffset, pMinZ).setNor(0f, 1f, 0f).setUV(0f, 0f).setCol(curbEdgeCol);
        V2.setPos(pMaxX + vergeW, yOffset, pMinZ).setNor(0f, 1f, 0f).setUV(0f, 1f).setCol(turfEdgeCol);
        V3.setPos(pMaxX + vergeW, yOffset, pMaxZ).setNor(0f, 1f, 0f).setUV(3f, 1f).setCol(turfEdgeCol);
        mpbShoulder.rect(V0, V1, V2, V3);

        Model shoulderModel = mb.end();
        models.add(shoulderModel);
        ModelInstance shoulderInst = new ModelInstance(shoulderModel);
        outInstances.add(shoulderInst);
        nonSolid.add(shoulderInst);
        shadowExclusions.add(shoulderInst);
    }

    /**
     * Determines whether a grid cell falls completely within the Curzon Pond basin.
     */
    private static boolean isInsidePond(float x0, float z0, float x1, float z1) {
        return x0 >= GroundConfig.POND_MIN_X && x1 <= GroundConfig.POND_MAX_X &&
               z0 >= GroundConfig.POND_MIN_Z && z1 <= GroundConfig.POND_MAX_Z;
    }

    /**
     * Height query matching {@link bd.spark36.character.PlayerController} physics.
     * Guaranteed zero mismatch between rendered terrain and player movement collision.
     */
    public static float getGroundHeight(float x, float z) {
        boolean inPond = x > GroundConfig.POND_MIN_X && x < GroundConfig.POND_MAX_X &&
                         z > GroundConfig.POND_MIN_Z && z < GroundConfig.POND_MAX_Z;
        if (!inPond) return GroundConfig.CAMPUS_BASE_Y;
        // The ghat's submerged bottom step
        if (Math.abs(x) < 3.1f && z >= 16.3f) return 0.14f;
        return GroundConfig.POND_FLOOR_Y;
    }

    public static void setupPbr(Material mat, float metallic, float roughness) {
        mat.set(PBRFloatAttribute.createMetallic(metallic));
        mat.set(PBRFloatAttribute.createRoughness(roughness));
        ColorAttribute diff = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
        if (diff != null && !mat.has(PBRColorAttribute.BaseColorFactor)) {
            mat.set(PBRColorAttribute.createBaseColorFactor(diff.color));
        }
    }

    @Override
    public void dispose() {
        for (int i = 0; i < models.size; i++) {
            models.get(i).dispose();
        }
        models.clear();
    }
}
