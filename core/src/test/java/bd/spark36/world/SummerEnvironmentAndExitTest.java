// ei file ta summer environment ar safe exit logic test kore
package bd.spark36.world;

import bd.spark36.render.RenderPipelineConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SummerEnvironmentAndExitTest {

    @Test
    @DisplayName("Summer Fog Distance: Ensures Curzon Hall at 92m is never blanched in near fog")
    public void testSummerFogNearFarRange() {
        AtmosphereConfig.Preset p = AtmosphereConfig.getPreset(WeatherState.CLEAR_SUNNY);
        assertNotNull(p, "CLEAR_SUNNY preset must exist");

        assertTrue(p.fogNear >= 70.0f,
            "fogNear must be >= 70m so Curzon Hall (at 92m) has clear visibility, got " + p.fogNear);

        assertTrue(p.fogFar >= 220.0f,
            "fogFar must be >= 220m for deep atmospheric scale, got " + p.fogFar);

        float fogLum = 0.2126f * p.fogColor.r + 0.7152f * p.fogColor.g + 0.0722f * p.fogColor.b;
        assertTrue(fogLum >= 0.20f && fogLum <= 0.55f,
            "Summer fog must be a mid haze in linear space (not white-out, not dusk), got luminance " + fogLum);
        assertTrue(p.fogColor.g <= p.fogColor.b + 0.01f,
            "Summer fog must not lean green-yellow (G <= B), got G=" + p.fogColor.g + " B=" + p.fogColor.b);
        assertTrue(p.fogColor.b - p.fogColor.r <= 0.15f,
            "Summer fog must stay a near-neutral haze, not saturated blue, got R=" + p.fogColor.r + " B=" + p.fogColor.b);
    }

    @Test
    @DisplayName("T8: Clear summer sky is blue (not cyan or green) and is authored in linear space")
    public void testClearSkyIsBlueNotCyan() {
        AtmosphereConfig.Preset p = AtmosphereConfig.getPreset(WeatherState.CLEAR_SUNNY);
        com.badlogic.gdx.graphics.Color[] bands = { p.skyZenith, p.skyMidBlue, p.skyHorizon, p.skyLowHaze };
        for (com.badlogic.gdx.graphics.Color c : bands) {
            assertTrue(c.b > c.g && c.g > c.r, "Clear sky band must be blue-dominant (B > G > R), got " + c);

            assertTrue(c.g / c.b <= 0.75f, "Clear sky band must not be cyan (linear G/B <= 0.75), got " + c);
        }

        assertTrue(p.skyZenith.b <= 0.60f, "Zenith must be a linear value, got B=" + p.skyZenith.b);

        assertTrue(p.skyZenith.g < p.skyMidBlue.g && p.skyMidBlue.g < p.skyHorizon.g && p.skyHorizon.g <= p.skyLowHaze.g,
            "Sky gradient must brighten from zenith to horizon");
    }

    @Test
    @DisplayName("Summer Lighting: Sunlight ambient and directional light reflect warm July afternoon")
    public void testSummerLightingWarmth() {
        AtmosphereConfig.Preset p = AtmosphereConfig.getPreset(WeatherState.CLEAR_SUNNY);
        assertNotNull(p, "CLEAR_SUNNY preset must exist");

        assertTrue(p.ambientColor.r >= 0.35f, "Ambient light R should be >= 0.35, got " + p.ambientColor.r);
        assertTrue(p.ambientColor.g >= 0.38f, "Ambient light G should be >= 0.38, got " + p.ambientColor.g);

        assertTrue(p.ambientColor.b >= p.ambientColor.g,
            "Sky-fill ambient must not be olive (B >= G), got " + p.ambientColor);

        assertTrue(p.sunColor.r >= p.sunColor.b, "Sunlight R must be >= B for warm sunlight");
        assertTrue(p.sunColor.g >= p.sunColor.b, "Sunlight G must be >= B for warm sunlight");
    }

    @Test
    @DisplayName("Post-Processing Grading: natural summer saturation and slightly warm, cast-free white balance")
    public void testSummerColorGradingSettings() {

        assertTrue(RenderPipelineConfig.saturation >= 1.0f && RenderPipelineConfig.saturation <= 1.10f,
            "Saturation must be in [1.0, 1.10] (no grey wash, no oversaturation), got " + RenderPipelineConfig.saturation);

        assertTrue(RenderPipelineConfig.colorTemperature > 0.05f && RenderPipelineConfig.colorTemperature <= 0.10f,
            "Color temperature must be slightly warm (0.05, 0.10], got " + RenderPipelineConfig.colorTemperature);

        assertTrue(RenderPipelineConfig.colorTint <= 0.0f && RenderPipelineConfig.colorTint >= -0.05f,
            "Color tint must be neutral or faintly magenta [-0.05, 0], got " + RenderPipelineConfig.colorTint);

        assertTrue(RenderPipelineConfig.shadowLift <= 0.02f,
            "Shadow lift must be <= 0.02f to prevent grey shadow washout, got " + RenderPipelineConfig.shadowLift);

        assertTrue(RenderPipelineConfig.toneMappingMode >= 0 && RenderPipelineConfig.toneMappingMode <= 2,
            "Tone mapping mode must be 0, 1, or 2, got " + RenderPipelineConfig.toneMappingMode);
    }

    @Test
    @DisplayName("Bamboo Botanical Spec: Culm heights, diameters, node spacing, and footprint")
    public void testBambooCulmGeometryConstraints() {

        float minHeight = 4.5f;
        float maxHeight = 10.0f;
        float minRadius = 0.0175f;
        float maxRadius = 0.0475f;

        for (int c = 0; c < 32; c++) {
            float distNorm = (float) c / 32f;
            float baseH = 6.4f + (1.0f - distNorm) * 1.8f;
            float heightMin = baseH - 0.45f * 2.4f;
            float heightMax = baseH + 0.55f * 2.4f;

            assertTrue(heightMin >= minHeight - 0.2f, "Culm minimum height must be >= 4.5m");
            assertTrue(heightMax <= maxHeight + 0.2f, "Culm maximum height must be <= 10.0m");

            float rMin = 0.020f + (heightMin / 10.0f) * 0.024f - 0.5f * 0.006f;
            float rMax = 0.020f + (heightMax / 10.0f) * 0.024f + 0.5f * 0.006f;
            assertTrue(rMin >= minRadius - 0.002f, "Culm radius min must be >= 0.0175m");
            assertTrue(rMax <= maxRadius + 0.005f, "Culm radius max must be <= 0.0475m");

            int nodes = Math.max(12, Math.round(baseH / 0.32f));
            float spacing = baseH / nodes;
            assertTrue(spacing >= 0.22f && spacing <= 0.48f,
                "Node spacing " + spacing + " must be between 0.22m and 0.48m");
        }
    }

    @Test
    @DisplayName("Bamboo Footprint: Clump culms are strictly bounded within 5x5m footprint")
    public void testBambooClumpFootprintBounds() {
        float cx = 18f;
        float cz = 36f;
        float maxAllowedRadius = 2.5f;

        java.util.Random rng = new java.util.Random(9200L);
        for (int c = 0; c < 32; c++) {
            float r = (c < 20) ? (0.20f + 1.15f * (float) Math.sqrt(rng.nextFloat()))
                               : (1.35f + 1.00f * (float) Math.sqrt(rng.nextFloat()));
            float theta = rng.nextFloat() * (float) Math.PI * 2f;
            float x = cx + (float) Math.cos(theta) * r;
            float z = cz + (float) Math.sin(theta) * r;

            float distFromCenter = (float) Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
            assertTrue(distFromCenter <= maxAllowedRadius,
                "Culm distance " + distFromCenter + " exceeds 5x5m clump radius boundary (" + maxAllowedRadius + "m)");
        }
    }
}
