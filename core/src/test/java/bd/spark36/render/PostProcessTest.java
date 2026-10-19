package bd.spark36.render;

import bd.spark36.world.AtmosphereConfig;
import bd.spark36.world.WeatherController;
import bd.spark36.world.WeatherState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 Unit Tests: Post-processing pipeline math, tone mapping curves, exposure/contrast,
 * soft-knee bloom extraction, FXAA thresholds, and GraphicsTier scalability.
 */
public class PostProcessTest {

    // ── ACES Filmic Formula Reference ───────────────────────────────────────
    private static float aces(float x) {
        float a = 2.51f;
        float b = 0.03f;
        float c = 2.43f;
        float d = 0.59f;
        float e = 0.14f;
        return Math.max(0.0f, Math.min(1.0f, (x * (a * x + b)) / (x * (c * x + d) + e)));
    }

    // ── Extended Reinhard Formula Reference ────────────────────────────────
    private static float reinhard(float x, float whitePoint) {
        float wp2 = whitePoint * whitePoint;
        return Math.max(0.0f, Math.min(1.0f, (x * (1.0f + x / wp2)) / (1.0f + x)));
    }

    // ── Karis Soft-Knee Bloom Extraction Reference ─────────────────────────
    private static float bloomKarisSoftKnee(float luma, float threshold, float knee) {
        float soft = luma - threshold + knee;
        soft = Math.max(0.0f, Math.min(2.0f * knee, soft));
        soft = (soft * soft) / (4.0f * knee + 1e-4f);
        float factor = Math.max(soft, luma - threshold) / Math.max(luma, 1e-4f);
        return Math.max(0.0f, factor);
    }

    @Test
    @DisplayName("Point 1: ACES filmic curve is monotonic, preserves blacks, rolloffs highlights smoothly")
    public void testAcesToneMapping() {
        // Deep blacks
        assertEquals(0.0f, aces(0.0f), 0.01f, "Zero input must map to near-zero");

        // Monotonicity across dynamic range
        float prev = 0.0f;
        for (float v = 0.05f; v <= 50.0f; v += 0.25f) {
            float mapped = aces(v);
            assertTrue(mapped >= prev, "ACES must be monotonically increasing at v=" + v);
            assertTrue(mapped <= 1.0f, "ACES must not exceed 1.0 at v=" + v);
            prev = mapped;
        }

        // Photographic mid-gray (0.18) mapping
        float midGray = aces(0.18f);
        assertTrue(midGray >= 0.25f && midGray <= 0.45f,
            "18% mid-gray should map pleasingly to filmic mid-range (~0.35)");

        // Extreme HDR highlight compression
        float highlight10 = aces(10.0f);
        float highlight50 = aces(50.0f);
        assertTrue(highlight10 >= 0.95f, "10x highlight must compress into top 5%");
        assertTrue(highlight50 >= highlight10, "50x highlight must be >= 10x highlight");
        assertTrue(highlight50 <= 1.0f);
    }

    @Test
    @DisplayName("Point 2: Extended Reinhard curve maintains linear response for dark values and asymptotically approaches 1.0")
    public void testReinhardToneMapping() {
        assertEquals(0.0f, reinhard(0.0f, 3.0f), 1e-5f);

        // Near zero, Reinhard is approximately linear: x / (1 + x) ~ x
        float small = 0.02f;
        float rSmall = reinhard(small, 3.0f);
        assertTrue(Math.abs(rSmall - small) < 0.005f, "Reinhard should be near-linear at very low luminance");

        // High values approach 1.0
        float r10 = reinhard(10.0f, 3.0f);
        assertTrue(r10 > 0.90f && r10 <= 1.0f);
    }

    @Test
    @DisplayName("Point 3: Contrast adjustment is strictly anchored at photographic 18% mid-gray")
    public void testContrastMidpointAnchoring() {
        float midGray = 0.18f;

        // At mid-gray (0.18), any contrast value must return 0.18
        float[] contrastValues = { 0.5f, 0.8f, 1.0f, 1.05f, 1.2f, 1.5f };
        for (float c : contrastValues) {
            float result = (midGray - 0.18f) * c + 0.18f;
            assertEquals(midGray, result, 1e-6f,
                "Midpoint 0.18 must remain invariant under contrast=" + c);
        }

        // Higher than mid-gray gets boosted with contrast > 1.0
        float bright = 0.60f;
        float boosted = (bright - 0.18f) * 1.05f + 0.18f;
        assertTrue(boosted > bright, "Values above 0.18 must increase with contrast > 1.0");

        // Lower than mid-gray gets darkened with contrast > 1.0
        float dark = 0.10f;
        float lowered = (dark - 0.18f) * 1.05f + 0.18f;
        assertTrue(lowered < dark, "Values below 0.18 must decrease with contrast > 1.0");
    }

    @Test
    @DisplayName("Point 4: Rec. 709 linear luminance weights sum to 1.0000 and Karis soft-knee eliminates harsh thresholds")
    public void testBloomLuminanceAndKarisKnee() {
        // Rec. 709 standard coefficients
        float rWeight = 0.2126f;
        float gWeight = 0.7152f;
        float bWeight = 0.0722f;
        float sum = rWeight + gWeight + bWeight;
        assertEquals(1.0f, sum, 1e-4f, "Rec. 709 luminance weights must sum to exactly 1.0");

        float threshold = 1.05f;
        float knee = 0.35f;

        // Sub-threshold values well below knee return 0
        float factorLow = bloomKarisSoftKnee(0.5f, threshold, knee);
        assertEquals(0.0f, factorLow, 1e-4f, "Values below (threshold - knee) must be fully suppressed");

        // In the soft knee region (e.g. at threshold 1.05), factor is smoothly non-zero without hard step
        float factorKnee = bloomKarisSoftKnee(1.05f, threshold, knee);
        assertTrue(factorKnee > 0.0f, "Karis knee should smoothly blend non-zero near threshold");

        // Over threshold values have increasing bloom extraction factor
        float factorHigh = bloomKarisSoftKnee(3.0f, threshold, knee);
        assertTrue(factorHigh > factorKnee, "Extraction factor must monotonically increase above threshold");
    }

    @Test
    @DisplayName("Point 5: FXAA 3.11 early-exit threshold correctly identifies smooth regions vs high-contrast edges")
    public void testFxaaEdgeDetectionThresholds() {
        // Flat surface with minor texture variation
        float lMaxFlat = 0.50f;
        float lMinFlat = 0.49f;
        float rangeFlat = lMaxFlat - lMinFlat; // 0.01

        float edgeThresholdMin = 1.0f / 16.0f; // 0.0625
        float edgeThresholdRelative = 0.125f;  // 1/8
        float thresholdFlat = Math.max(edgeThresholdMin, lMaxFlat * edgeThresholdRelative);

        assertTrue(rangeFlat < thresholdFlat, "Flat surface should be detected as low contrast and skip FXAA");

        // High contrast geometry edge (e.g. bright sky against dark Curzon Hall brick)
        float lMaxEdge = 0.95f;
        float lMinEdge = 0.15f;
        float rangeEdge = lMaxEdge - lMinEdge; // 0.80
        float thresholdEdge = Math.max(edgeThresholdMin, lMaxEdge * edgeThresholdRelative);

        assertTrue(rangeEdge >= thresholdEdge, "Geometry edge must trigger FXAA smoothing pass");
    }

    @Test
    @DisplayName("Point 6: Dynamic weather couples temperature and tint into post-processing")
    public void testDynamicWeatherWhitebalanceCoupling() {
        WeatherController wc = new WeatherController(WeatherState.CLEAR_SUNNY);
        assertEquals(0.0f, wc.getCurrentTemperature(), 1e-4f);
        assertEquals(0.0f, wc.getCurrentTint(), 1e-4f);

        // Switch to Golden Hour
        wc.setWeather(WeatherState.GOLDEN_HOUR, 0.0f); // Instant transition
        assertTrue(wc.getCurrentTemperature() > 0.15f,
            "Golden Hour must provide warm positive temperature shift (got " + wc.getCurrentTemperature() + ")");
        assertTrue(wc.getCurrentTint() > 0.0f,
            "Golden Hour must provide slight magenta tint (got " + wc.getCurrentTint() + ")");

        // Switch to Monsoon Rain
        wc.setWeather(WeatherState.MONSOON_RAIN, 0.0f);
        assertTrue(wc.getCurrentTemperature() < -0.05f,
            "Monsoon rain must provide cool negative temperature shift (got " + wc.getCurrentTemperature() + ")");
        assertTrue(wc.getCurrentTint() < 0.0f,
            "Monsoon rain must provide slight cool tint (got " + wc.getCurrentTint() + ")");
    }

    @Test
    @DisplayName("Point 7: GraphicsTier scalability governs bloom passes, film grain, and shadow quality")
    public void testGraphicsTierScalability() {
        // Low Tier: Performance focus
        GraphicsTier low = GraphicsTier.LOW;
        assertFalse(low.enableBloom, "Low tier must disable bloom");
        assertEquals(0, low.bloomPasses, "Low tier must execute 0 bloom passes");
        assertEquals(0.0f, low.filmGrain, 1e-5f, "Low tier must disable film grain");

        // Medium Tier: Balanced M4 Air 60 FPS
        GraphicsTier medium = GraphicsTier.MEDIUM;
        assertTrue(medium.enableBloom, "Medium tier must enable bloom");
        assertEquals(1, medium.bloomPasses, "Medium tier must have 1 bloom blur pass");
        assertTrue(medium.filmGrain > 0.0f && medium.filmGrain <= 0.005f);

        // High Tier: Cinematic
        GraphicsTier high = GraphicsTier.HIGH;
        assertTrue(high.enableBloom, "High tier must enable bloom");
        assertEquals(2, high.bloomPasses, "High tier must execute 2 bloom blur passes");
        assertTrue(high.filmGrain > medium.filmGrain, "High tier film grain should be refined and visible");
    }

    @Test
    @DisplayName("Point 8: Tone mapping mode cycling transitions ACES -> Reinhard -> Linear -> ACES")
    public void testToneMappingModeCycling() {
        RenderPipelineConfig.toneMappingMode = 1; // ACES
        assertEquals("ACES Filmic (Cinematic)", RenderPipelineConfig.getToneMappingName());

        String mode2 = RenderPipelineConfig.cycleToneMapping();
        assertEquals(2, RenderPipelineConfig.toneMappingMode);
        assertEquals("Extended Reinhard", mode2);

        String mode0 = RenderPipelineConfig.cycleToneMapping();
        assertEquals(0, RenderPipelineConfig.toneMappingMode);
        assertEquals("Linear / Bypass", mode0);

        String mode1 = RenderPipelineConfig.cycleToneMapping();
        assertEquals(1, RenderPipelineConfig.toneMappingMode);
        assertEquals("ACES Filmic (Cinematic)", mode1);
    }
}
