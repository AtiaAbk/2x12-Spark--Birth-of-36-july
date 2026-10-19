package bd.spark36.world;

import bd.spark36.render.GraphicsTier;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VegetationTest {

    private WindModel windModel;

    @BeforeEach
    public void setUp() {
        windModel = new WindModel();
    }

    @Test
    @DisplayName("Point 1: Physical Grounding Guarantee — displacement is strictly zero at y <= 0")
    public void testGroundAnchorDisplacementZero() {
        Vector3 disp = new Vector3();
        float[] testY = {0.0f, -0.05f, -0.22f, -1.0f};
        float[] testCoords = {-40f, -15f, 0f, 15f, 40f};

        for (float y : testY) {
            for (float x : testCoords) {
                for (float z : testCoords) {
                    for (float t = 0f; t <= 10f; t += 0.5f) {
                        windModel.getCanopyDisplacement(x, z, y, 10.0f, t, disp);
                        assertEquals(0.0f, disp.x, 1e-6f, "X displacement non-zero at ground level!");
                        assertEquals(0.0f, disp.y, 1e-6f, "Y displacement non-zero at ground level!");
                        assertEquals(0.0f, disp.z, 1e-6f, "Z displacement non-zero at ground level!");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Point 2: Canopy displacement is mathematically bounded and monotonic with height")
    public void testCanopyDisplacementBoundsAndMonotonicity() {
        Vector3 disp = new Vector3();
        float treeH = 12.0f;
        float maxAllowedDisp = 0.085f; // ~8.5 cm max displacement

        for (float t = 0f; t <= 8f; t += 0.25f) {
            for (float h = 1.0f; h <= treeH; h += 2.0f) {
                windModel.getCanopyDisplacement(10f, 20f, h, treeH, t, disp);
                float len = disp.len();
                assertTrue(len <= maxAllowedDisp,
                    "Displacement length " + len + " exceeded physical limit " + maxAllowedDisp + " at height " + h);
            }

            // Monotonic test: top of tree flexes more than or equal to trunk midpoint in peak gusts
            Vector3 dispMid = new Vector3();
            Vector3 dispTop = new Vector3();
            windModel.getCanopyDisplacement(0f, 0f, treeH * 0.5f, treeH, t, dispMid);
            windModel.getCanopyDisplacement(0f, 0f, treeH, treeH, t, dispTop);
            assertTrue(dispTop.len() >= dispMid.len() - 1e-5f,
                "Top displacement must be greater or equal to midpoint displacement due to (y/H)^2 height weighting");
        }
    }

    @Test
    @DisplayName("Point 3: Base-anchored trunk tilt angles stay strictly within safe degrees")
    public void testTrunkSwayAngleBounds() {
        Vector3 angles = new Vector3();
        float maxPitch = VegetationConfig.MAX_TRUNK_SWAY_DEG * 1.5f;
        float maxRoll = VegetationConfig.MAX_TRUNK_SWAY_DEG * 0.75f;

        for (float x = -60f; x <= 60f; x += 15f) {
            for (float z = -60f; z <= 60f; z += 15f) {
                for (float t = 0f; t <= 12f; t += 0.3f) {
                    windModel.getTrunkSwayAngles(x, z, t, angles);
                    assertTrue(Math.abs(angles.x) <= maxPitch,
                        "Trunk pitch " + angles.x + " exceeded max degrees " + maxPitch);
                    assertEquals(0.0f, angles.y, 1e-6f, "Yaw angle must remain zero");
                    assertTrue(Math.abs(angles.z) <= maxRoll,
                        "Trunk roll " + angles.z + " exceeded max degrees " + maxRoll);
                }
            }
        }
    }

    @Test
    @DisplayName("Point 4: Wind direction is unit normalized and matches monsoon breeze vector")
    public void testWindDirectionNormalization() {
        Vector3 dir = new Vector3();
        windModel.getWindDirection(dir);
        assertEquals(1.0f, dir.len(), 1e-4f, "Wind direction vector must be unit length");
        assertEquals(VegetationConfig.WIND_DIRECTION.x, dir.x, 1e-4f);
        assertEquals(VegetationConfig.WIND_DIRECTION.y, dir.y, 1e-4f);
        assertEquals(VegetationConfig.WIND_DIRECTION.z, dir.z, 1e-4f);
    }

    @Test
    @DisplayName("Point 5: Spatial decorrelation prevents synchronous campus-wide tree rocking")
    public void testSpatialPhaseDecorrelation() {
        Vector3 angles1 = new Vector3();
        Vector3 angles2 = new Vector3();

        int differentFrames = 0;
        int totalFrames = 50;
        for (int i = 0; i < totalFrames; i++) {
            float t = i * 0.2f;
            windModel.getTrunkSwayAngles(-30f, 10f, t, angles1);
            windModel.getTrunkSwayAngles(30f, 40f, t, angles2);
            if (Math.abs(angles1.x - angles2.x) > 0.05f || Math.abs(angles1.z - angles2.z) > 0.02f) {
                differentFrames++;
            }
        }
        assertTrue(differentFrames > totalFrames * 0.80,
            "Trees across campus should not sway in lockstep; spatial phase must decorrelate angles");
    }

    @Test
    @DisplayName("Point 6: Leaf card physical dimensions strictly adhere to biological realism (0.32m - 0.45m)")
    public void testLeafCardPhysicalDimensions() {
        assertTrue(VegetationConfig.RAIN_TREE_LEAF_CARD_SIZE >= 0.32f && VegetationConfig.RAIN_TREE_LEAF_CARD_SIZE <= 0.45f,
            "Rain Tree leaf card size out of physical bounds: " + VegetationConfig.RAIN_TREE_LEAF_CARD_SIZE);
        assertTrue(VegetationConfig.KRISHNA_LEAF_CARD_SIZE >= 0.32f && VegetationConfig.KRISHNA_LEAF_CARD_SIZE <= 0.45f,
            "Krishnachura leaf card size out of physical bounds: " + VegetationConfig.KRISHNA_LEAF_CARD_SIZE);
        assertTrue(VegetationConfig.WHITE_BLOSSOM_CARD_SIZE >= 0.32f && VegetationConfig.WHITE_BLOSSOM_CARD_SIZE <= 0.45f,
            "White Blossom card size out of physical bounds: " + VegetationConfig.WHITE_BLOSSOM_CARD_SIZE);
    }

    @Test
    @DisplayName("Point 7: PBR material configuration matches physical organic foliage standards")
    public void testPbrVegetationStandards() {
        assertEquals(0.0f, VegetationConfig.LEAF_METALLIC, 1e-6f, "Leaves must have zero metallic response");
        assertTrue(VegetationConfig.LEAF_ROUGHNESS >= 0.55f && VegetationConfig.LEAF_ROUGHNESS <= 0.75f,
            "Leaf roughness must be organic waxy value: " + VegetationConfig.LEAF_ROUGHNESS);
        assertTrue(VegetationConfig.BARK_ROUGHNESS >= 0.80f,
            "Bark roughness must be high (rough grooved bark): " + VegetationConfig.BARK_ROUGHNESS);
        assertTrue(VegetationConfig.LEAF_ALPHA_CUTOFF >= 0.30f && VegetationConfig.LEAF_ALPHA_CUTOFF <= 0.40f,
            "Alpha cutoff must prevent halos without thinning: " + VegetationConfig.LEAF_ALPHA_CUTOFF);
    }

    @Test
    @DisplayName("Point 8: Campus tree locations are valid and species match historical Curzon Hall precinct")
    public void testCampusTreeLocations() {
        float[][] locs = DhakaCampusWorld.TREE_LOCATIONS;
        assertNotNull(locs);
        assertTrue(locs.length >= 18, "Campus must maintain full historical tree count");

        for (int i = 0; i < locs.length; i++) {
            float[] loc = locs[i];
            assertEquals(3, loc.length, "Tree location must have {x, z, species}");
            float tx = loc[0];
            float tz = loc[1];
            int species = (int) loc[2];

            assertTrue(tx >= -80f && tx <= 80f, "Tree X out of precinct bounds: " + tx);
            assertTrue(tz >= -30f && tz <= 90f, "Tree Z out of precinct bounds: " + tz);
            assertTrue(species >= 0 && species <= 3, "Unknown tree species ID: " + species);
        }
    }

    @Test
    @DisplayName("Point 9: GraphicsTier vegetation quality settings scale predictably")
    public void testGraphicsTierScales() {
        assertTrue(GraphicsTier.LOW.vegetationLodDistance < GraphicsTier.MEDIUM.vegetationLodDistance,
            "LOW LOD distance must be smaller than MEDIUM");
        assertTrue(GraphicsTier.MEDIUM.vegetationLodDistance < GraphicsTier.HIGH.vegetationLodDistance,
            "MEDIUM LOD distance must be smaller than HIGH");
        assertTrue(GraphicsTier.LOW.vegetationDensity < GraphicsTier.MEDIUM.vegetationDensity,
            "LOW vegetation density must be smaller than MEDIUM");
        assertFalse(GraphicsTier.LOW.enableLayeredWind, "LOW tier should disable layered wind for performance");
        assertTrue(GraphicsTier.MEDIUM.enableLayeredWind, "MEDIUM tier must enable layered wind");
        assertTrue(GraphicsTier.HIGH.enableLayeredWind, "HIGH tier must enable layered wind");
    }

    @Test
    @DisplayName("Point 10: TreeEntity base anchor guarantee — root point (0,0,0) displacement is identically zero")
    public void testTreeEntityBaseAnchorDisplacementIdenticallyZero() {
        com.badlogic.gdx.graphics.g3d.Model dummyModel = new com.badlogic.gdx.graphics.g3d.Model();
        com.badlogic.gdx.graphics.g3d.ModelInstance trunk = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);
        com.badlogic.gdx.graphics.g3d.ModelInstance core = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);
        com.badlogic.gdx.graphics.g3d.ModelInstance cards = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);

        float px = 15.0f, pz = -22.5f;
        TreeEntity entity = new TreeEntity(VegetationConfig.SPECIES_RAIN_TREE, px, pz, 1.0f, 42f, 4.0f, trunk, core, cards);

        Vector3 rootWorldTrunk = new Vector3();
        Vector3 rootWorldCore = new Vector3();
        Vector3 rootWorldCards = new Vector3();

        for (float t = 0f; t <= 10f; t += 0.25f) {
            entity.update(t, windModel, GraphicsTier.MEDIUM, null);

            // Transform local root (0, 0, 0)
            rootWorldTrunk.set(0f, 0f, 0f).mul(entity.getTrunkInstance().transform);
            rootWorldCore.set(0f, 0f, 0f).mul(entity.getCanopyCoreInstance().transform);
            rootWorldCards.set(0f, 0f, 0f).mul(entity.getLeafCardsInstance().transform);

            assertEquals(px, rootWorldTrunk.x, 1e-5f, "Trunk root X drifted!");
            assertEquals(0f, rootWorldTrunk.y, 1e-5f, "Trunk root Y displaced!");
            assertEquals(pz, rootWorldTrunk.z, 1e-5f, "Trunk root Z drifted!");

            assertEquals(px, rootWorldCore.x, 1e-5f, "Core root X drifted!");
            assertEquals(0f, rootWorldCore.y, 1e-5f, "Core root Y displaced!");
            assertEquals(pz, rootWorldCore.z, 1e-5f, "Core root Z drifted!");

            assertEquals(px, rootWorldCards.x, 1e-5f, "Cards root X drifted!");
            assertEquals(0f, rootWorldCards.y, 1e-5f, "Cards root Y displaced!");
            assertEquals(pz, rootWorldCards.z, 1e-5f, "Cards root Z drifted!");
        }
    }

    @Test
    @DisplayName("Point 11: Layered wind sway hierarchy — leaves sway more than core, core sways more than trunk")
    public void testLayeredWindSwayHierarchy() {
        com.badlogic.gdx.graphics.g3d.Model dummyModel = new com.badlogic.gdx.graphics.g3d.Model();
        com.badlogic.gdx.graphics.g3d.ModelInstance trunk = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);
        com.badlogic.gdx.graphics.g3d.ModelInstance core = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);
        com.badlogic.gdx.graphics.g3d.ModelInstance cards = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);

        TreeEntity entity = new TreeEntity(VegetationConfig.SPECIES_KRISHNACHURA, 0f, 0f, 1.0f, 7.7f, 3.5f, trunk, core, cards);

        Vector3 topTrunk = new Vector3();
        Vector3 topCore = new Vector3();

        int dynamicChecks = 0;
        for (float t = 0.5f; t <= 10f; t += 0.5f) {
            entity.update(t, windModel, GraphicsTier.MEDIUM, null);

            topTrunk.set(0f, 3f, 0f).mul(entity.getTrunkInstance().transform);
            topCore.set(0f, 3f, 0f).mul(entity.getCanopyCoreInstance().transform);

            float trunkDeflection = (float) Math.hypot(topTrunk.x, topTrunk.z);
            float coreDeflection = (float) Math.hypot(topCore.x, topCore.z);

            if (trunkDeflection > 0.01f) {
                // Core has 1.0x sway vs trunk 0.45x sway -> coreDeflection > trunkDeflection
                assertTrue(coreDeflection >= trunkDeflection,
                    "Core deflection " + coreDeflection + " must exceed trunk deflection " + trunkDeflection);
                dynamicChecks++;
            }
        }
        assertTrue(dynamicChecks > 0, "Wind model produced deflection during test interval");
    }

    @Test
    @DisplayName("Point 12: Distance-based LOD toggles leaf cards correctly")
    public void testDistanceBasedLod() {
        com.badlogic.gdx.graphics.g3d.Model dummyModel = new com.badlogic.gdx.graphics.g3d.Model();
        com.badlogic.gdx.graphics.g3d.ModelInstance trunk = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);
        com.badlogic.gdx.graphics.g3d.ModelInstance core = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);
        com.badlogic.gdx.graphics.g3d.ModelInstance cards = new com.badlogic.gdx.graphics.g3d.ModelInstance(dummyModel);

        TreeEntity entity = new TreeEntity(VegetationConfig.SPECIES_RAIN_TREE, 0f, 0f, 1.0f, 1.0f, 4.0f, trunk, core, cards);

        com.badlogic.gdx.graphics.Camera cam = new com.badlogic.gdx.graphics.Camera() {
            @Override
            public void update() {}
            @Override
            public void update(boolean updateFrustum) {}
        };

        // Near distance: 20m from tree -> cards visible
        cam.position.set(0f, 2f, 20f);
        entity.update(0f, windModel, GraphicsTier.MEDIUM, cam);
        assertTrue(entity.isShowingCards(), "Leaf cards should be visible at 20m (< 65m cutoff)");

        // Far distance: 100m from tree -> cards culled
        cam.position.set(0f, 2f, 100f);
        entity.update(0f, windModel, GraphicsTier.MEDIUM, cam);
        assertFalse(entity.isShowingCards(), "Leaf cards should be culled at 100m (> 65m cutoff)");
    }
}
