package bd.spark36.app;

import static org.junit.jupiter.api.Assertions.*;

import bd.spark36.audio.AmbienceSynth;
import bd.spark36.audio.SoundManager;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.hud.screens.SettingsScreen;
import bd.spark36.render.GraphicsTier;
import bd.spark36.render.RenderPipelineConfig;
import com.badlogic.gdx.math.Vector3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SettingsApplyTest {

    private final String tierPropBefore = System.getProperty("bd.spark36.graphicsTier");
    private final int toneBefore = RenderPipelineConfig.toneMappingMode;
    private final GraphicsTier tierBefore = RenderPipelineConfig.activeGraphicsTier;

    @BeforeAll
    static void loadNatives() {
        com.badlogic.gdx.utils.GdxNativesLoader.load(); // PerspectiveCamera.update uses native Matrix4.prj
    }

    @AfterEach
    void restoreGlobals() {
        SettingsApplier.applyNonAudio(GameSettings.defaults());
        RenderPipelineConfig.activeGraphicsTier = tierBefore;
        RenderPipelineConfig.toneMappingMode = toneBefore;
        if (tierPropBefore == null) System.clearProperty("bd.spark36.graphicsTier");
        else System.setProperty("bd.spark36.graphicsTier", tierPropBefore);
    }

    @Test
    void cameraSensitivityAndMotionReductionApply() {
        GameSettings s = GameSettings.defaults();
        s.mouseSensitivity = 2.0f;
        s.reduceCameraMotion = true;
        SettingsApplier.applyNonAudio(s);
        assertEquals(2.0f, CinematicCamera.getUserSensitivity());
        assertTrue(CinematicCamera.isReduceMotion());

        CinematicCamera cam = new CinematicCamera(800, 600);
        cam.setMouseLookActive(false);
        assertEquals(0.44f, cam.getEffectiveSensitivity(), 1e-5f);

        cam.addShake(0.3f);
        Vector3 pos = new Vector3(0f, 0f, 40f);
        for (int i = 0; i < 30; i++) cam.update(1f / 60f, pos, true, true, true, 0f);
        assertEquals(0f, cam.getCurrentBob(), 1e-6f, "no head-bob when reduced");
        assertEquals(0f, cam.getShakeOffset().len(), 1e-6f, "no shake when reduced");
        assertEquals(65f, cam.getCurrentFov(), 0.01f, "no sprint FOV push when reduced");
    }

    @Test
    void cameraMovesNormallyWithoutReduction() {
        SettingsApplier.applyNonAudio(GameSettings.defaults());
        CinematicCamera cam = new CinematicCamera(800, 600);
        cam.setMouseLookActive(false);
        Vector3 pos = new Vector3(0f, 0f, 40f);
        float maxBob = 0f;
        for (int i = 0; i < 60; i++) {
            cam.update(1f / 60f, pos, true, true, true, 0f);
            maxBob = Math.max(maxBob, Math.abs(cam.getCurrentBob()));
        }
        assertTrue(maxBob > 0.005f, "head-bob present by default");
        assertTrue(cam.getCurrentFov() > 70f, "sprint FOV push present by default");
    }

    @Test
    void sensitivityIsClamped() {
        CinematicCamera.setUserSensitivity(50f);
        assertEquals(3.0f, CinematicCamera.getUserSensitivity());
        CinematicCamera.setUserSensitivity(Float.NaN);
        assertEquals(1.0f, CinematicCamera.getUserSensitivity());
    }

    @Test
    void graphicsTierAndToneMapReachRenderPipeline() {
        GameSettings s = GameSettings.defaults();
        s.graphicsTier = "LOW";
        s.toneMapping = 0;
        SettingsApplier.applyNonAudio(s);
        boolean switchGiven = tierPropBefore != null;
        if (!switchGiven) {
            assertEquals(GraphicsTier.LOW, RenderPipelineConfig.activeGraphicsTier);
            assertEquals(GraphicsTier.LOW, GraphicsTier.fromSystemProperty(), "systems that read the tier see it");
        }
        assertEquals(0, RenderPipelineConfig.toneMappingMode);

        s.graphicsTier = null;
        s.toneMapping = -1;
        SettingsApplier.applyNonAudio(s);
        if (!switchGiven) assertEquals(RenderPipelineConfig.getDefaultGraphicsTier(), RenderPipelineConfig.activeGraphicsTier);
        assertEquals(RenderPipelineConfig.getDefaultToneMappingMode(), RenderPipelineConfig.toneMappingMode);
    }

    @Test
    void toneMapHotkeyNotifiesListener() {
        int[] calls = {0};
        RenderPipelineConfig.setToneMappingListener(() -> calls[0]++);
        try {
            RenderPipelineConfig.cycleToneMapping();
            assertEquals(1, calls[0]);
        } finally {
            RenderPipelineConfig.setToneMappingListener(null);
        }
    }

    @Test
    void subtitleAndLanguageChooseDialogueLines() {
        GameSettings s = GameSettings.defaults();
        SettingsApplier.applyNonAudio(s);
        assertTrue(DialoguePrefs.showEnglishLine());
        assertTrue(DialoguePrefs.showBengaliLine());

        s.subtitles = false;
        SettingsApplier.applyNonAudio(s);
        assertTrue(DialoguePrefs.showEnglishLine());
        assertFalse(DialoguePrefs.showBengaliLine());

        s.language = GameSettings.Language.BN;
        SettingsApplier.applyNonAudio(s);
        assertFalse(DialoguePrefs.showEnglishLine());
        assertTrue(DialoguePrefs.showBengaliLine());

        s.textSizeIndex = 3;
        SettingsApplier.applyNonAudio(s);
        assertEquals(1.4f, DialoguePrefs.textScale(), 1e-6f);
    }

    @Test
    void settingsScreenStepsAndClamps() {
        GameSettings s = GameSettings.defaults();
        for (int i = 0; i < 20; i++) SettingsScreen.adjust(s, SettingsScreen.Row.MASTER, 1, GraphicsTier.MEDIUM, 1);
        assertEquals(1f, s.masterVolume, 1e-6f);
        for (int i = 0; i < 20; i++) SettingsScreen.adjust(s, SettingsScreen.Row.SFX, -1, GraphicsTier.MEDIUM, 1);
        assertEquals(0f, s.sfxVolume, 1e-6f);
        SettingsScreen.adjust(s, SettingsScreen.Row.SENSITIVITY, 1, GraphicsTier.MEDIUM, 1);
        assertEquals(1.25f, s.mouseSensitivity, 1e-6f);
        SettingsScreen.adjust(s, SettingsScreen.Row.SUBTITLES, 1, GraphicsTier.MEDIUM, 1);
        assertFalse(s.subtitles);
        SettingsScreen.adjust(s, SettingsScreen.Row.GRAPHICS, 1, GraphicsTier.HIGH, 1);
        assertEquals("HIGH", s.graphicsTier, "already at the top tier");
        SettingsScreen.adjust(s, SettingsScreen.Row.GRAPHICS, -1, GraphicsTier.HIGH, 1);
        assertEquals("MEDIUM", s.graphicsTier);
        SettingsScreen.adjust(s, SettingsScreen.Row.TONEMAP, 1, GraphicsTier.MEDIUM, 1);
        assertEquals(2, s.toneMapping);
        SettingsScreen.adjust(s, SettingsScreen.Row.TONEMAP, 1, GraphicsTier.MEDIUM, 0);
        assertEquals(1, s.toneMapping, "wraps LINEAR -> ACES");
        SettingsScreen.adjust(s, SettingsScreen.Row.LANGUAGE, 1, GraphicsTier.MEDIUM, 1);
        assertEquals(GameSettings.Language.BN, s.language);
        assertEquals("BANGLA", SettingsScreen.valueText(s, SettingsScreen.Row.LANGUAGE, GraphicsTier.MEDIUM, 1));
    }

    @Test
    void mixerMultipliesChannels() {
        assertEquals(0.5f * 0.8f * 0.25f, SoundManager.mix(0.5f, 0.8f, 0.25f), 1e-6f);
        assertEquals(0f, SoundManager.mix(0.5f, 0f, 1f));
        assertEquals(1f, SoundManager.mix(2f, 3f, 4f), "clamped to unity");
        assertEquals(SoundManager.Ambience.CAMPUS, SoundManager.ambienceForLevel(1));
        assertEquals(SoundManager.Ambience.CAMPUS, SoundManager.ambienceForLevel(2));
        assertEquals(SoundManager.Ambience.NONE, SoundManager.ambienceForLevel(4));
        assertEquals(SoundManager.Ambience.CROWD, SoundManager.ambienceForLevel(5));
        assertEquals(SoundManager.Ambience.CROWD, SoundManager.ambienceForLevel(6));
    }

    @Test
    void ambienceBedsLoopSeamlesslyWithoutClipping() {
        for (short[] pcm : new short[][]{AmbienceSynth.campusBirdsAndWind(1921L), AmbienceSynth.distantCrowdMurmur(2024L)}) {
            assertEquals((int) (AmbienceSynth.SAMPLE_RATE * AmbienceSynth.LOOP_SECONDS), pcm.length);
            int peak = 0;
            long sumStep = 0;
            for (int i = 0; i < pcm.length; i++) {
                peak = Math.max(peak, Math.abs(pcm[i]));
                if (i > 0) sumStep += Math.abs(pcm[i] - pcm[i - 1]);
            }
            assertTrue(peak < 32767, "no clipping");
            assertTrue(peak > 8000, "audible");
            double meanStep = (double) sumStep / (pcm.length - 1);
            int seam = Math.abs(pcm[0] - pcm[pcm.length - 1]);
            assertTrue(seam < Math.max(200, meanStep * 8), "loop seam " + seam + " vs mean step " + meanStep);
        }
        assertArrayEquals(AmbienceSynth.campusBirdsAndWind(7L), AmbienceSynth.campusBirdsAndWind(7L), "deterministic");
    }
}
