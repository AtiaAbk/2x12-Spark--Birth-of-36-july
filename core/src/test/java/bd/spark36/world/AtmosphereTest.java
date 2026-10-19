package bd.spark36.world;

import bd.spark36.render.GraphicsTier;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AtmosphereTest {

    private WeatherController weatherController;

    @BeforeEach
    public void setUp() {
        weatherController = new WeatherController(WeatherState.CLEAR_SUNNY);
    }

    @Test
    @DisplayName("Point 1: All Weather presets have normalized sun vectors, valid fog equation, and valid color bounds")
    public void testPresetsExistAndValid() {
        for (WeatherState state : WeatherState.values()) {
            AtmosphereConfig.Preset p = AtmosphereConfig.getPreset(state);
            assertNotNull(p, "Preset for state " + state + " must not be null");

            // Sun direction must be unit vector pointing downward from the sky
            float sunLen = p.sunDirection.len();
            assertEquals(1.0f, sunLen, 1e-4f, "Sun vector for " + state + " must be normalized");
            assertTrue(p.sunDirection.y < 0f, "Sun vector Y for " + state + " must point down toward terrain");

            // Sun & ambient color sanity
            assertTrue(p.sunColor.r > 0f && p.sunColor.g > 0f && p.sunColor.b > 0f);
            assertTrue(p.ambientColor.r > 0f && p.ambientColor.g > 0f && p.ambientColor.b > 0f);

            // Distance fog parameters
            assertTrue(p.fogNear > 0f, "Fog near for " + state + " must be positive");
            assertTrue(p.fogFar > p.fogNear, "Fog far must exceed fog near for " + state);
            assertTrue(p.fogExponent >= 1.0f, "Fog exponent must be >= 1.0 for " + state);
            assertTrue(p.fogColor.a >= 0.99f, "Fog alpha must be opaque");

            // Sky gradient colors
            assertTrue(p.skyZenith.r >= 0f && p.skyZenith.r <= 1f);
            assertTrue(p.skyMidBlue.r >= 0f && p.skyMidBlue.r <= 1f);
            assertTrue(p.skyHorizon.r >= 0f && p.skyHorizon.r <= 1f);
            assertTrue(p.skyLowHaze.r >= 0f && p.skyLowHaze.r <= 1f);

            // Rain intensity sanity
            if (state == WeatherState.MONSOON_RAIN) {
                assertEquals(1.0f, p.rainIntensity, 1e-4f, "Monsoon Rain must have full rain intensity");
            } else {
                assertEquals(0.0f, p.rainIntensity, 1e-4f, "Non-rain state " + state + " must have zero rain intensity");
            }
        }
    }

    @Test
    @DisplayName("Point 2: WeatherController state machine smoothly transitions between states")
    public void testWeatherStateTransitions() {
        assertEquals(WeatherState.CLEAR_SUNNY, weatherController.getCurrentState());
        assertFalse(weatherController.isTransitioning());

        // Initiate transition to Golden Hour over 4 seconds
        weatherController.setWeather(WeatherState.GOLDEN_HOUR, 4.0f);
        assertTrue(weatherController.isTransitioning());
        assertEquals(WeatherState.GOLDEN_HOUR, weatherController.getTargetState());

        // Update halfway (2 seconds)
        weatherController.update(2.0f);
        assertTrue(weatherController.isTransitioning());
        assertEquals(0.5f, weatherController.getTransitionProgress(), 1e-4f);

        // Update remaining 2 seconds
        weatherController.update(2.0f);
        assertFalse(weatherController.isTransitioning());
        assertEquals(1.0f, weatherController.getTransitionProgress(), 1e-4f);
        assertEquals(WeatherState.GOLDEN_HOUR, weatherController.getCurrentState());
    }

    @Test
    @DisplayName("Point 3: Sun vector remains normalized across all interpolation frames")
    public void testSunDirectionNormalizationAcrossTransition() {
        weatherController.setWeather(WeatherState.GOLDEN_HOUR, 2.0f);
        for (float t = 0f; t <= 2.0f; t += 0.05f) {
            weatherController.update(0.05f);
            Vector3 sunDir = weatherController.getCurrentSunDirection();
            assertEquals(1.0f, sunDir.len(), 1e-4f, "Sun vector must stay normalized during weather blend");
        }
    }

    @Test
    @DisplayName("Point 4: Monsoon fog is denser (shorter far distance) than clear summer day")
    public void testFogDensityComparison() {
        AtmosphereConfig.Preset clear = AtmosphereConfig.getPreset(WeatherState.CLEAR_SUNNY);
        AtmosphereConfig.Preset overcast = AtmosphereConfig.getPreset(WeatherState.OVERCAST_MONSOON);
        AtmosphereConfig.Preset rain = AtmosphereConfig.getPreset(WeatherState.MONSOON_RAIN);

        assertTrue(overcast.fogFar < clear.fogFar, "Overcast fog must be closer/denser than clear summer sky");
        assertTrue(rain.fogFar < overcast.fogFar, "Monsoon rain fog must be denser than overcast");
        assertTrue(rain.fogNear < clear.fogNear, "Monsoon rain fog near boundary must start closer to camera");
    }

    @Test
    @DisplayName("Point 5: Cycle weather visits all 4 states in defined order and wraps")
    public void testWeatherCycling() {
        WeatherState s1 = weatherController.cycleNextWeather();
        assertEquals(WeatherState.GOLDEN_HOUR, s1);

        WeatherState s2 = weatherController.cycleNextWeather();
        assertEquals(WeatherState.OVERCAST_MONSOON, s2);

        WeatherState s3 = weatherController.cycleNextWeather();
        assertEquals(WeatherState.MONSOON_RAIN, s3);

        WeatherState s4 = weatherController.cycleNextWeather();
        assertEquals(WeatherState.CLEAR_SUNNY, s4);
    }

    @Test
    @DisplayName("Point 6: GraphicsTier scalability for rain particles and volumetric shafts")
    public void testGraphicsTierScalability() {
        assertEquals(80, GraphicsTier.LOW.maxRainParticles);
        assertEquals(180, GraphicsTier.MEDIUM.maxRainParticles);
        assertEquals(300, GraphicsTier.HIGH.maxRainParticles);

        assertFalse(GraphicsTier.LOW.enableVolumetricShafts, "Low tier must disable volumetric light shafts for performance");
        assertTrue(GraphicsTier.MEDIUM.enableVolumetricShafts, "Medium tier enables volumetric light shafts");
        assertTrue(GraphicsTier.HIGH.enableVolumetricShafts, "High tier enables volumetric light shafts");
    }

    @Test
    @DisplayName("Point 7: Instant weather transition snaps immediately with zero duration")
    public void testInstantWeatherTransition() {
        weatherController.setWeather(WeatherState.MONSOON_RAIN, 0f);
        assertFalse(weatherController.isTransitioning());
        assertEquals(WeatherState.MONSOON_RAIN, weatherController.getCurrentState());
        assertEquals(1.0f, weatherController.getCurrentRainIntensity(), 1e-4f);
        assertEquals(AtmosphereConfig.MONSOON_RAIN.fogFar, weatherController.getCurrentFogFar(), 1e-4f);
    }
}
