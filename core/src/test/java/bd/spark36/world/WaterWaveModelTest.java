package bd.spark36.world;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WaterWaveModelTest {

    private WaterWaveModel waveModel;

    @BeforeEach
    public void setUp() {
        waveModel = new WaterWaveModel();
    }

    @Test
    @DisplayName("Point 1: Water height at pond center stays within physical bounds")
    public void testPondCenterHeightBounds() {
        float cx = WaterConfig.CENTER_X;
        float cz = WaterConfig.CENTER_Z;
        for (float t = 0f; t <= 10f; t += 0.25f) {
            float h = waveModel.getWaterHeight(cx, cz, t);
            assertTrue(h >= WaterConfig.BASE_WATER_HEIGHT - WaterConfig.MAX_WAVE_DISPLACEMENT - 1e-4f,
                "Height " + h + " below minimum bound at time " + t);
            assertTrue(h <= WaterConfig.BASE_WATER_HEIGHT + WaterConfig.MAX_WAVE_DISPLACEMENT + 1e-4f,
                "Height " + h + " above maximum bound at time " + t);
        }
    }

    @Test
    @DisplayName("Point 2: Maximum wave displacement never exceeds theoretical sum of amplitudes")
    public void testMaxDisplacementAcrossPond() {
        float maxDisp = WaterConfig.MAX_WAVE_DISPLACEMENT + 1e-4f;
        for (float x = -8f; x <= 8f; x += 1.0f) {
            for (float z = -4f; z <= 16f; z += 1.0f) {
                for (float t = 0f; t <= 5f; t += 0.5f) {
                    float h = waveModel.getWaterHeight(x, z, t);
                    float diff = Math.abs(h - WaterConfig.BASE_WATER_HEIGHT);
                    assertTrue(diff <= maxDisp,
                        "Displacement " + diff + " exceeded limit " + maxDisp + " at (" + x + ", " + z + ")");
                }
            }
        }
    }

    @Test
    @DisplayName("Point 3: Wave displacement dampens to exactly zero at and beyond pond stone curbs")
    public void testBoundaryDampeningAtCurbs() {
        // Pond boundaries are x in [-8.8, 8.8] and z in [-5.8, 17.8]
        float[] testEdgePoints = {
            -8.8f, 6.0f,
             8.8f, 6.0f,
             0.0f, -5.8f,
             0.0f, 17.8f,
            -9.5f, 6.0f, // outside
             0.0f, 20.0f // outside
        };

        for (int i = 0; i < testEdgePoints.length; i += 2) {
            float x = testEdgePoints[i];
            float z = testEdgePoints[i + 1];
            for (float t = 0f; t <= 3f; t += 0.5f) {
                float h = waveModel.getWaterHeight(x, z, t);
                assertEquals(WaterConfig.BASE_WATER_HEIGHT, h, 1e-5f,
                    "Water height at curb/exterior (" + x + ", " + z + ") must equal BASE_WATER_HEIGHT");
            }
        }
    }

    @Test
    @DisplayName("Point 4: Shoreline dampening factor transitions smoothly from 0.0 to 1.0")
    public void testSmoothShorelineTransition() {
        // Walking inward from curb towards center
        float prevDamp = -0.01f;
        for (float dist = 0.0f; dist <= WaterConfig.SHORE_MARGIN + 0.5f; dist += 0.05f) {
            float x = (WaterConfig.WATER_WIDTH * 0.5f) - dist; // distance from east curb
            float damp = waveModel.getShoreDampening(x, WaterConfig.CENTER_Z);
            assertTrue(damp >= 0.0f && damp <= 1.0f, "Dampening " + damp + " out of [0, 1] range");
            assertTrue(damp >= prevDamp - 1e-5f, "Dampening must be monotonic inward from shore");
            prevDamp = damp;
        }
        // At margin and beyond, dampening must be exactly 1.0
        float fullDamp = waveModel.getShoreDampening(WaterConfig.CENTER_X, WaterConfig.CENTER_Z);
        assertEquals(1.0f, fullDamp, 1e-5f);
    }

    @Test
    @DisplayName("Point 5: Analytical surface normal vector is unit length and predominantly upward")
    public void testNormalVectorValidity() {
        Vector3 norm = new Vector3();
        for (float x = -6f; x <= 6f; x += 2.0f) {
            for (float z = -2f; z <= 14f; z += 2.0f) {
                for (float t = 0f; t <= 4f; t += 1.0f) {
                    waveModel.getWaterNormal(x, z, t, norm);
                    float len = norm.len();
                    assertEquals(1.0f, len, 1e-4f, "Normal vector must be normalized to length 1.0");
                    assertTrue(norm.y > 0.95f, "Normal vector Y component must be predominantly upright (calm pond), got " + norm.y);
                }
            }
        }
    }

    @Test
    @DisplayName("Point 6: Wave height calculation is completely deterministic and reproducible")
    public void testDeterministicEvaluation() {
        float x = 2.45f, z = 7.12f, t = 3.87f;
        float h1 = waveModel.getWaterHeight(x, z, t);
        float h2 = waveModel.getWaterHeight(x, z, t);
        float h3 = waveModel.getWaterHeight(x, z, t);
        assertEquals(h1, h2, 0.0f);
        assertEquals(h2, h3, 0.0f);
    }

    @Test
    @DisplayName("Point 7: getWaterNormal modifies output vector in-place without returning a new instance")
    public void testInPlaceNormalModification() {
        Vector3 out = new Vector3(99f, 99f, 99f);
        waveModel.getWaterNormal(1.0f, 2.0f, 0.5f, out);
        assertNotEquals(99f, out.x);
        assertNotEquals(99f, out.y);
        assertNotEquals(99f, out.z);
    }

    @Test
    @DisplayName("Point 8: All 36 historical lotus pad placements lie strictly within pond water boundaries")
    public void testAllLilyPadsInsideWater() {
        float[][] lotusPlacements = {
            // North-West calm bay (world X in [-7.2, -3.6], world Z in [-3.8, -0.5])
            {-6.8f, -9.5f}, {-5.5f, -9.8f}, {-4.2f, -9.0f},
            {-6.2f, -8.2f}, {-5.0f, -7.8f}, {-3.6f, -8.5f},
            {-7.2f, -7.0f}, {-5.8f, -6.5f}, {-4.5f, -6.0f},

            // North-East calm bay (world X in [3.5, 7.0], world Z in [-3.8, -0.5])
            {4.0f, -9.5f}, {5.6f, -9.8f}, {6.8f, -9.2f},
            {3.5f, -8.2f}, {5.0f, -7.8f}, {6.4f, -8.0f},
            {4.2f, -6.5f}, {5.8f, -6.8f}, {7.0f, -7.0f},

            // West bank fringes (world X in [-7.5, -5.8], world Z in [1.5, 10.8])
            {-7.5f, -4.5f}, {-6.4f, -3.5f}, {-7.2f, -2.0f},
            {-6.0f, -0.8f}, {-7.4f,  0.8f}, {-6.2f,  2.2f},
            {-7.0f,  3.5f}, {-5.8f,  4.8f},

            // East bank fringes (world X in [6.0, 7.5], world Z in [1.5, 10.8])
            {7.2f, -4.5f}, {6.2f, -3.5f}, {7.4f, -2.0f},
            {6.0f, -0.8f}, {7.2f,  0.8f}, {6.4f,  2.2f},
            {7.5f,  3.5f}, {6.1f,  4.8f},

            // South-West corner (away from ghat, world Z in [11.8, 15.5])
            {-6.8f, 5.8f}, {-5.4f, 6.8f}, {-6.5f, 7.8f},
            {-5.0f, 8.8f}, {-6.2f, 9.5f},

            // South-East corner (away from ghat, world Z in [11.8, 15.5])
            {6.8f, 5.8f}, {5.5f, 6.8f}, {6.6f, 7.8f},
            {5.2f, 8.8f}, {6.4f, 9.5f}
        };

        assertEquals(44, lotusPlacements.length, "Must verify all 44 lotus pad placements");

        for (int i = 0; i < lotusPlacements.length; i++) {
            float worldX = WaterConfig.CENTER_X + lotusPlacements[i][0];
            float worldZ = WaterConfig.CENTER_Z + lotusPlacements[i][1];

            assertTrue(waveModel.isInsideWater(worldX, worldZ),
                "Lotus pad #" + i + " at (" + worldX + ", " + worldZ + ") is outside pond boundaries!");
            assertTrue(waveModel.getShoreDistance(worldX, worldZ) > 0.2f,
                "Lotus pad #" + i + " at (" + worldX + ", " + worldZ + ") is too close to or intersecting stone curbs!");
        }
    }
}
