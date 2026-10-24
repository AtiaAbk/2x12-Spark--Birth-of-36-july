package bd.spark36.app;

import bd.spark36.audio.SoundManager;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.render.RenderPipelineConfig;

/**
 * Pushes a {@link GameSettings} into the running subsystems. Every target is a static switch so
 * the settings take effect live, before or after the gameplay objects exist.
 */
public final class SettingsApplier {

    private SettingsApplier() {}

    /** Applies everything except audio (no OpenAL needed). Used by tests and before audio starts. */
    public static void applyNonAudio(GameSettings s) {
        CinematicCamera.setUserSensitivity(s.mouseSensitivity);
        CinematicCamera.setReduceMotion(s.reduceCameraMotion);
        DialoguePrefs.set(s.subtitles, s.textScale(), s.language);
        RenderPipelineConfig.applyUserGraphicsTier(s.graphicsTier);
        RenderPipelineConfig.applyUserToneMapping(s.toneMapping);
    }

    /** Full apply, including the audio mixer. */
    public static void apply(GameSettings s) {
        applyNonAudio(s);
        SoundManager sm = SoundManager.getIfCreated();
        if (sm != null) {
            sm.setMasterVolume(s.masterVolume);
            sm.setSfxVolume(s.sfxVolume);
            sm.setAmbienceVolume(s.ambienceVolume);
        } else {
            SoundManager.setInitialVolumes(s.masterVolume, s.sfxVolume, s.ambienceVolume);
        }
    }
}
