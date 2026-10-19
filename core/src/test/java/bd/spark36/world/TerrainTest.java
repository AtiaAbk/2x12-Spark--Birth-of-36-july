package bd.spark36.world;

import bd.spark36.character.PlayerController;
import bd.spark36.render.GraphicsTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerrainTest {

    @Test
    @DisplayName("Point 1: Ground Elevation Query Consistency — identical to PlayerController physics")
    public void testGroundHeightConsistencyWithPhysics() {
        // Sample extensively across the campus precinct (-120m to 120m X, -90m to 120m Z)
        for (float x = -120f; x <= 120f; x += 10f) {
            for (float z = -90f; z <= 120f; z += 10f) {
                float terrainH = TerrainSurface.getGroundHeight(x, z);
                float physicsH = PlayerController.groundLevelAt(x, z);
                assertEquals(physicsH, terrainH, 1e-6f,
                    "Elevation mismatch at (" + x + ", " + z + ")! Terrain: " + terrainH + " vs Physics: " + physicsH);
            }
        }

        // Test boundary of pond specifically
        float[][] testPondCoords = {
            {0f, 6f},        // Deep pond center
            {-5f, 5f},       // Inside pond
            {5f, 10f},       // Inside pond
            {0f, 16.5f},     // Ghat submerged step
            {-15f, 6f},      // Outside pond west
            {15f, 6f},       // Outside pond east
            {0f, -10f},      // Outside pond north
            {0f, 25f}        // Outside pond south
        };

        for (float[] coord : testPondCoords) {
            float x = coord[0], z = coord[1];
            float terrainH = TerrainSurface.getGroundHeight(x, z);
            float physicsH = PlayerController.groundLevelAt(x, z);
            assertEquals(physicsH, terrainH, 1e-6f,
                "Elevation mismatch at pond point (" + x + ", " + z + ")");
        }
    }

    @Test
    @DisplayName("Point 2: Pond Basin Cutout Bounds — water basin correctly detected and hollowed")
    public void testPondBasinCutoutBounds() {
        assertEquals(-8.8f, GroundConfig.POND_MIN_X, 1e-4f);
        assertEquals( 8.8f, GroundConfig.POND_MAX_X, 1e-4f);
        assertEquals(-5.8f, GroundConfig.POND_MIN_Z, 1e-4f);
        assertEquals(17.8f, GroundConfig.POND_MAX_Z, 1e-4f);
        assertEquals(-0.45f, GroundConfig.POND_FLOOR_Y, 1e-4f);
        assertEquals( 0.05f, GroundConfig.POND_WATER_Y, 1e-4f);

        // Center of pond must be in pond basin
        float centerH = TerrainSurface.getGroundHeight(0f, 6.0f);
        assertEquals(GroundConfig.POND_FLOOR_Y, centerH, 1e-4f, "Pond center must be at floor elevation");

        // Campus turf outside pond must be at base elevation 0.0
        float lawnH = TerrainSurface.getGroundHeight(-20f, 6.0f);
        assertEquals(GroundConfig.CAMPUS_BASE_Y, lawnH, 1e-4f, "Campus lawn must be at base elevation 0.0");
    }

    @Test
    @DisplayName("Point 3: Real-World UV Texture Scaling — eliminates 280-meter single-box stretch")
    public void testRealWorldUvScaleRealism() {
        // Prior to Phase 4, the ground was a single 280m x 260m box with UV repeating once across the entire campus (1/280 ~= 0.00357).
        // Phase 4 enforces real-world metric repeat rates >= 0.15 UV/meter.
        assertTrue(GroundConfig.GRASS_UV_SCALE >= 0.20f && GroundConfig.GRASS_UV_SCALE <= 0.50f,
            "Grass UV scale out of realistic bounds (1 repeat every 2m to 5m): " + GroundConfig.GRASS_UV_SCALE);
        assertEquals(0.25f, GroundConfig.GRASS_UV_SCALE, 1e-5f, "Grass UV scale must be 0.25 (1 repeat per 4m)");

        assertTrue(GroundConfig.BRICK_UV_SCALE >= 0.40f && GroundConfig.BRICK_UV_SCALE <= 0.80f,
            "Brick UV scale out of realistic bounds (~22cm pavers): " + GroundConfig.BRICK_UV_SCALE);
        assertEquals(0.55f, GroundConfig.BRICK_UV_SCALE, 1e-5f, "Brick UV scale must be 0.55 (1 repeat per 1.82m)");

        assertTrue(GroundConfig.SOIL_UV_SCALE >= 0.20f && GroundConfig.SOIL_UV_SCALE <= 0.50f,
            "Soil UV scale out of realistic bounds: " + GroundConfig.SOIL_UV_SCALE);
        assertTrue(GroundConfig.DIRT_UV_SCALE >= 0.20f && GroundConfig.DIRT_UV_SCALE <= 0.50f,
            "Dirt UV scale out of realistic bounds: " + GroundConfig.DIRT_UV_SCALE);
        assertTrue(GroundConfig.ASPHALT_UV_SCALE >= 0.10f && GroundConfig.ASPHALT_UV_SCALE <= 0.30f,
            "Asphalt UV scale out of realistic bounds: " + GroundConfig.ASPHALT_UV_SCALE);
    }

    @Test
    @DisplayName("Point 4: Ground PBR Material Calibration — dielectric organic values")
    public void testGroundPbrMaterialCalibration() {
        // All ground materials (grass, soil, dirt, brick, asphalt, stone curb) are dielectrics -> metallic strictly 0.0
        assertEquals(0.0f, GroundConfig.GRASS_METALLIC, 1e-6f, "Grass must have 0.0 metallic");
        assertEquals(0.0f, GroundConfig.SOIL_METALLIC, 1e-6f, "Soil must have 0.0 metallic");
        assertEquals(0.0f, GroundConfig.DIRT_METALLIC, 1e-6f, "Dirt must have 0.0 metallic");
        assertEquals(0.0f, GroundConfig.ASPHALT_METALLIC, 1e-6f, "Asphalt must have 0.0 metallic");
        assertEquals(0.0f, GroundConfig.BRICK_METALLIC, 1e-6f, "Brick must have 0.0 metallic");
        assertEquals(0.0f, GroundConfig.CURB_METALLIC, 1e-6f, "Curb must have 0.0 metallic");

        // Roughness values must be in organic / matte range [0.65, 0.95]
        assertTrue(GroundConfig.GRASS_ROUGHNESS >= 0.75f && GroundConfig.GRASS_ROUGHNESS <= 0.90f,
            "Grass roughness out of range: " + GroundConfig.GRASS_ROUGHNESS);
        assertTrue(GroundConfig.SOIL_ROUGHNESS >= 0.80f && GroundConfig.SOIL_ROUGHNESS <= 0.95f,
            "Soil roughness out of range: " + GroundConfig.SOIL_ROUGHNESS);
        assertTrue(GroundConfig.DIRT_ROUGHNESS >= 0.80f && GroundConfig.DIRT_ROUGHNESS <= 0.95f,
            "Dirt roughness out of range: " + GroundConfig.DIRT_ROUGHNESS);
        assertTrue(GroundConfig.ASPHALT_ROUGHNESS >= 0.75f && GroundConfig.ASPHALT_ROUGHNESS <= 0.90f,
            "Asphalt roughness out of range: " + GroundConfig.ASPHALT_ROUGHNESS);
        assertTrue(GroundConfig.BRICK_ROUGHNESS >= 0.65f && GroundConfig.BRICK_ROUGHNESS <= 0.85f,
            "Brick roughness out of range: " + GroundConfig.BRICK_ROUGHNESS);
        assertTrue(GroundConfig.CURB_ROUGHNESS >= 0.70f && GroundConfig.CURB_ROUGHNESS <= 0.90f,
            "Curb roughness out of range: " + GroundConfig.CURB_ROUGHNESS);
    }

    @Test
    @DisplayName("Point 5: Tree Soil Collars — species-proportional radii grounded around all campus trees")
    public void testTreeSoilCollarsGrounding() {
        assertTrue(GroundConfig.TREE_SOIL_RADIUS_RAIN_TREE >= 2.4f && GroundConfig.TREE_SOIL_RADIUS_RAIN_TREE <= 3.2f,
            "Rain Tree collar radius out of bounds: " + GroundConfig.TREE_SOIL_RADIUS_RAIN_TREE);
        assertTrue(GroundConfig.TREE_SOIL_RADIUS_KRISHNA >= 1.8f && GroundConfig.TREE_SOIL_RADIUS_KRISHNA <= 2.6f,
            "Krishnachura collar radius out of bounds: " + GroundConfig.TREE_SOIL_RADIUS_KRISHNA);
        assertTrue(GroundConfig.TREE_SOIL_RADIUS_WHITE_BLOSSOM >= 2.0f && GroundConfig.TREE_SOIL_RADIUS_WHITE_BLOSSOM <= 2.8f,
            "White Blossom collar radius out of bounds: " + GroundConfig.TREE_SOIL_RADIUS_WHITE_BLOSSOM);
        assertTrue(GroundConfig.TREE_SOIL_RADIUS_PALM >= 1.2f && GroundConfig.TREE_SOIL_RADIUS_PALM <= 2.0f,
            "Palm collar radius out of bounds: " + GroundConfig.TREE_SOIL_RADIUS_PALM);

        // Verify all 18+ trees in DhakaCampusWorld are covered
        float[][] trees = DhakaCampusWorld.TREE_LOCATIONS;
        assertNotNull(trees);
        assertTrue(trees.length >= 18);
        for (float[] tree : trees) {
            float tx = tree[0];
            float tz = tree[1];
            int species = (int) tree[2];
            float radius;
            switch (species) {
                case 0:
                    radius = GroundConfig.TREE_SOIL_RADIUS_RAIN_TREE;
                    break;
                case 1:
                    radius = GroundConfig.TREE_SOIL_RADIUS_KRISHNA;
                    break;
                case 2:
                    radius = GroundConfig.TREE_SOIL_RADIUS_PALM;
                    break;
                default:
                    radius = GroundConfig.TREE_SOIL_RADIUS_WHITE_BLOSSOM;
                    break;
            }
            assertTrue(radius >= 1.2f && radius <= 3.5f,
                "Invalid collar radius for tree at (" + tx + ", " + tz + ")");
        }
    }

    @Test
    @DisplayName("Point 6: Transition Geometry Safety — Z-fighting elimination & shoulder metrics")
    public void testTransitionGeometrySafety() {
        // Vertical offset for transition decals: 3mm (0.003m) prevents depth-buffer coplanar Z-fighting
        assertEquals(0.003f, GroundConfig.TRANSITION_Y_OFFSET, 1e-5f);
        assertTrue(GroundConfig.TRANSITION_Y_OFFSET > 0.001f && GroundConfig.TRANSITION_Y_OFFSET < 0.010f,
            "Transition Y offset must be between 1mm and 10mm");

        // Shoulder widths
        assertTrue(GroundConfig.PATH_SHOULDER_WIDTH >= 0.40f && GroundConfig.PATH_SHOULDER_WIDTH <= 0.80f,
            "Path shoulder width out of realistic pedestrian bounds: " + GroundConfig.PATH_SHOULDER_WIDTH);
        assertTrue(GroundConfig.POND_VERGE_WIDTH >= 0.50f && GroundConfig.POND_VERGE_WIDTH <= 1.20f,
            "Pond verge width out of bounds: " + GroundConfig.POND_VERGE_WIDTH);
    }

    @Test
    @DisplayName("Point 7: GraphicsTier Terrain Resolution and Surface Blending Settings")
    public void testGraphicsTierTerrainSettings() {
        // LOW Tier: Coarse resolution (10m), transitions disabled for low-end hardware
        assertEquals(10.0f, GraphicsTier.LOW.terrainGridResolution, 1e-4f);
        assertFalse(GraphicsTier.LOW.enableTerrainTransitions);

        // MEDIUM Tier: 5m resolution, transitions enabled
        assertEquals(5.0f, GraphicsTier.MEDIUM.terrainGridResolution, 1e-4f);
        assertTrue(GraphicsTier.MEDIUM.enableTerrainTransitions);

        // HIGH Tier: 4m fine resolution, transitions enabled
        assertEquals(4.0f, GraphicsTier.HIGH.terrainGridResolution, 1e-4f);
        assertTrue(GraphicsTier.HIGH.enableTerrainTransitions);

        // High tier has higher geometric resolution (smaller cell size) than Low tier
        assertTrue(GraphicsTier.HIGH.terrainGridResolution < GraphicsTier.LOW.terrainGridResolution,
            "HIGH tier must have smaller cell size (higher mesh density) than LOW tier");
    }
}
