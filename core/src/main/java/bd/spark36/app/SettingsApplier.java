// ei file ta settings onujayi game er volume ar display update kore
package bd.spark36.app;

import bd.spark36.audio.SoundManager;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.render.RenderPipelineConfig;

public final class SettingsApplier {

    private SettingsApplier() {}

    public static void applyNonAudio(GameSettings s) {
        CinematicCamera.setUserSensitivity(s.mouseSensitivity);
        CinematicCamera.setReduceMotion(s.reduceCameraMotion);
        DialoguePrefs.set(s.subtitles, s.textScale(), s.language);
        RenderPipelineConfig.applyUserGraphicsTier(s.graphicsTier);
        RenderPipelineConfig.applyUserToneMapping(s.toneMapping);
    }

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
