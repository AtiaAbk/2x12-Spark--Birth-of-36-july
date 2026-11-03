// ei file ta rasta thik thaka ar pani clearance test kore
package bd.spark36.world;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoadClearanceAndWaterTest {

    @Test
    @DisplayName("Road Boundary Check: Identifies central promenade, cross avenues, pond ring, and glade paths")
    public void testRoadBoundaryDetection() {

        assertTrue(DhakaCampusWorld.isRoadOrWalkway(0f, 30f, 0f), "Center of promenade should be detected as road");
        assertTrue(DhakaCampusWorld.isRoadOrWalkway(2f, 50f, 0f), "Promenade walkway should be detected");
        assertFalse(DhakaCampusWorld.isRoadOrWalkway(25f, 50f, 0f), "Garden lawn should not be detected as road");

        assertTrue(DhakaCampusWorld.isRoadOrWalkway(30f, 30f, 0f), "East cross avenue should be detected as road");
        assertTrue(DhakaCampusWorld.isRoadOrWalkway(-30f, 30f, 0f), "West cross avenue should be detected as road");

        assertTrue(DhakaCampusWorld.isRoadOrWalkway(14f, 10f, 0f), "East pond ring path should be detected");
        assertTrue(DhakaCampusWorld.isRoadOrWalkway(-14f, 10f, 0f), "West pond ring path should be detected");

        assertTrue(DhakaCampusWorld.isRoadOrWalkway(18f, 40.5f, 0f), "Glade dirt path should be detected");
        assertTrue(DhakaCampusWorld.isRoadOrWalkway(-18f, 40.5f, 0f), "West glade dirt path should be detected");
    }

    @Test
    @DisplayName("Tree Clearance: Guarantees zero trees in TREE_LOCATIONS stand on any road or walkway")
    public void testTreeLocationsClearOfRoads() {
        for (float[] loc : DhakaCampusWorld.TREE_LOCATIONS) {
            float tx = loc[0];
            float tz = loc[1];
            assertFalse(DhakaCampusWorld.isRoadOrWalkway(tx, tz, 0.0f),
                "Tree at (" + tx + ", " + tz + ") stands on a campus road or walkway!");
        }
    }

    @Test
    @DisplayName("Bamboo Pondside Clumps: Bamboo groves are positioned safely in lawns away from pond ring path")
    public void testPondsideBambooGrovesClearOfPath() {

        float[][] pondsideGroves = {
            {21.5f, 14f},
            {-21.5f, 14f}
        };

        for (float[] grove : pondsideGroves) {
            assertFalse(DhakaCampusWorld.isRoadOrWalkway(grove[0], grove[1], 1.5f),
                "Pondside bamboo grove at (" + grove[0] + ", " + grove[1] + ") is too close to walkway!");

            float distFromRing = Math.abs(Math.abs(grove[0]) - 14.0f);
            assertTrue(distFromRing >= 5.0f,
                "Bamboo grove must have at least 5m clearance from pond ring path, got " + distFromRing);
        }
    }

    @Test
    @DisplayName("Realistic Water Configuration: Shallow teal & deep algae-emerald optical properties")
    public void testRealisticWaterConfig() {

        assertTrue(WaterConfig.SHALLOW_COLOR.r >= 0.05f && WaterConfig.SHALLOW_COLOR.r <= 0.25f);
        assertTrue(WaterConfig.SHALLOW_COLOR.g >= 0.40f && WaterConfig.SHALLOW_COLOR.g <= 0.70f);
        assertTrue(WaterConfig.SHALLOW_COLOR.b >= 0.45f && WaterConfig.SHALLOW_COLOR.b <= 0.75f);
        assertTrue(WaterConfig.SHALLOW_COLOR.a < 1.0f, "Shallow water must be translucent");

        assertTrue(WaterConfig.DEEP_COLOR.r <= 0.10f);
        assertTrue(WaterConfig.DEEP_COLOR.g >= 0.15f && WaterConfig.DEEP_COLOR.g <= 0.30f);
        assertTrue(WaterConfig.DEEP_COLOR.b >= 0.20f && WaterConfig.DEEP_COLOR.b <= 0.35f);
        assertTrue(WaterConfig.DEEP_COLOR.a >= 0.90f, "Deep water must have high opacity");

        assertTrue(WaterConfig.WAVE1_AMP > WaterConfig.WAVE2_AMP);
        assertTrue(WaterConfig.WAVE2_AMP > WaterConfig.WAVE3_AMP);
        assertTrue(WaterConfig.MAX_WAVE_DISPLACEMENT <= 0.05f, "Wave displacement must be subtle for campus pond");
    }
}
