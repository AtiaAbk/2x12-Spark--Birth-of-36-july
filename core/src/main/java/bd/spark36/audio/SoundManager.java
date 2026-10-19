package bd.spark36.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Procedural Audio Synthesizer and Sound Manager for Spark: Birth of 36 July.
 * Synthesizes 100% legally compliant, royalty-free 16-bit PCM sound effects
 * for footsteps, UI chimes, martial arts combat impacts, tear gas hiss,
 * water splashes, and the historic Freedom Bell toll.
 */
public class SoundManager implements Disposable {

    private static SoundManager instance;

    private Sound sndClick;
    private Sound sndWhoosh;
    private Sound sndPunch;
    private Sound sndKick;
    private Sound sndBlock;
    private Sound sndObjective;
    private Sound sndBell;
    private Sound sndSplash;
    private Sound sndFootstep;
    private Sound sndGasHiss;

    private boolean enabled = true;
    private float masterVolume = 0.85f;
    private float footstepTimer = 0f;

    public static SoundManager get() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public SoundManager() {
        init();
    }

    private void init() {
        try {
            FileHandle audioDir = Gdx.files.local("assets/audio/");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }

            sndClick = loadOrGenerate(audioDir, "ui_click.wav", generateClick());
            sndWhoosh = loadOrGenerate(audioDir, "whoosh.wav", generateWhoosh());
            sndPunch = loadOrGenerate(audioDir, "punch.wav", generatePunch());
            sndKick = loadOrGenerate(audioDir, "kick.wav", generateKick());
            sndBlock = loadOrGenerate(audioDir, "block.wav", generateBlock());
            sndObjective = loadOrGenerate(audioDir, "objective.wav", generateObjective());
            sndBell = loadOrGenerate(audioDir, "bell.wav", generateBell());
            sndSplash = loadOrGenerate(audioDir, "splash.wav", generateSplash());
            sndFootstep = loadOrGenerate(audioDir, "footstep.wav", generateFootstep());
            sndGasHiss = loadOrGenerate(audioDir, "gas_hiss.wav", generateGasHiss());

            Gdx.app.log("SoundManager", "Procedural Audio Synthesizer initialized successfully (10 sound effects loaded).");
        } catch (Throwable t) {
            enabled = false;
            Gdx.app.log("SoundManager", "Audio system disabled: " + t.getMessage());
        }
    }

    private Sound loadOrGenerate(FileHandle dir, String name, byte[] wavData) {
        try {
            FileHandle file = dir.child(name);
            if (!file.exists() || file.length() < 100) {
                file.writeBytes(wavData, false);
            }
            return Gdx.audio.newSound(file);
        } catch (Throwable t) {
            Gdx.app.log("SoundManager", "Could not load sound " + name + ": " + t.getMessage());
            return null;
        }
    }

    public void playClick() {
        if (enabled && sndClick != null) {
            sndClick.play(masterVolume * 0.75f, MathUtils.random(0.96f, 1.04f), 0f);
        }
    }

    public void playWhoosh() {
        if (enabled && sndWhoosh != null) {
            sndWhoosh.play(masterVolume * 0.85f, MathUtils.random(0.92f, 1.08f), 0f);
        }
    }

    public void playPunch() {
        if (enabled && sndPunch != null) {
            sndPunch.play(masterVolume * 0.95f, MathUtils.random(0.94f, 1.06f), 0f);
        }
    }

    public void playKick() {
        if (enabled && sndKick != null) {
            sndKick.play(masterVolume * 1.0f, MathUtils.random(0.90f, 1.04f), 0f);
        }
    }

    public void playBlock() {
        if (enabled && sndBlock != null) {
            sndBlock.play(masterVolume * 0.90f, MathUtils.random(0.95f, 1.05f), 0f);
        }
    }

    public void playObjective() {
        if (enabled && sndObjective != null) {
            sndObjective.play(masterVolume * 0.90f, 1.0f, 0f);
        }
    }

    public void playBell() {
        if (enabled && sndBell != null) {
            sndBell.play(masterVolume * 1.0f, 1.0f, 0f);
        }
    }

    public void playSplash() {
        if (enabled && sndSplash != null) {
            sndSplash.play(masterVolume * 0.80f, MathUtils.random(0.95f, 1.10f), 0f);
        }
    }

    public void playGasHiss() {
        if (enabled && sndGasHiss != null) {
            sndGasHiss.play(masterVolume * 0.65f, MathUtils.random(0.95f, 1.05f), 0f);
        }
    }

    public void updateFootsteps(float delta, boolean moving, boolean sprinting) {
        if (!enabled || !moving) {
            footstepTimer = 0f;
            return;
        }

        footstepTimer += delta;
        float interval = sprinting ? 0.32f : 0.48f;
        if (footstepTimer >= interval) {
            footstepTimer = 0f;
            if (sndFootstep != null) {
                float vol = (sprinting ? 0.45f : 0.30f) * masterVolume;
                float pitch = MathUtils.random(0.92f, 1.08f);
                sndFootstep.play(vol, pitch, 0f);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = Math.max(0f, Math.min(1f, masterVolume));
    }

    // ==========================================
    // PROCEDURAL 16-BIT PCM WAV SYNTHESIS
    // ==========================================

    private static byte[] buildWav(short[] samples, int sampleRate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            int subChunk2Size = samples.length * 2;
            int chunkSize = 36 + subChunk2Size;

            dos.writeBytes("RIFF");
            dos.writeInt(Integer.reverseBytes(chunkSize));
            dos.writeBytes("WAVE");
            dos.writeBytes("fmt ");
            dos.writeInt(Integer.reverseBytes(16)); // Subchunk1Size
            dos.writeShort(Short.reverseBytes((short) 1)); // AudioFormat (PCM)
            dos.writeShort(Short.reverseBytes((short) 1)); // NumChannels (Mono)
            dos.writeInt(Integer.reverseBytes(sampleRate)); // SampleRate
            dos.writeInt(Integer.reverseBytes(sampleRate * 2)); // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
            dos.writeShort(Short.reverseBytes((short) 2)); // BlockAlign
            dos.writeShort(Short.reverseBytes((short) 16)); // BitsPerSample
            dos.writeBytes("data");
            dos.writeInt(Integer.reverseBytes(subChunk2Size));

            for (short s : samples) {
                dos.writeShort(Short.reverseBytes(s));
            }
            dos.flush();
        } catch (IOException e) {
            // Unreachable in byte array streams
        }
        return baos.toByteArray();
    }

    private static byte[] generateClick() {
        int rate = 44100;
        int len = (int) (rate * 0.045f);
        short[] s = new short[len];
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 110.0);
            float osc = MathUtils.sin(t * MathUtils.PI2 * 2400f);
            s[i] = (short) (osc * env * 22000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateWhoosh() {
        int rate = 44100;
        int len = (int) (rate * 0.22f);
        short[] s = new short[len];
        float lastNoise = 0f;
        for (int i = 0; i < len; i++) {
            float t = (float) i / len;
            float env = MathUtils.sin(t * MathUtils.PI);
            float noise = MathUtils.random(-1f, 1f);
            lastNoise = lastNoise * 0.65f + noise * 0.35f;
            float pitch = MathUtils.sin(t * MathUtils.PI) * 800f + 250f;
            float osc = MathUtils.sin(i * MathUtils.PI2 * pitch / rate);
            s[i] = (short) ((lastNoise * 0.6f + osc * 0.4f) * env * 24000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generatePunch() {
        int rate = 44100;
        int len = (int) (rate * 0.16f);
        short[] s = new short[len];
        float pitch = 175f;
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 32.0);
            pitch = Math.max(38f, pitch - 1.1f);
            float osc = MathUtils.sin(i * MathUtils.PI2 * pitch / rate);
            float noise = MathUtils.random(-0.3f, 0.3f) * (float) Math.exp(-t * 70.0);
            s[i] = (short) ((osc * 0.75f + noise * 0.25f) * env * 29000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateKick() {
        int rate = 44100;
        int len = (int) (rate * 0.24f);
        short[] s = new short[len];
        float pitch = 145f;
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 22.0);
            pitch = Math.max(30f, pitch - 0.75f);
            float osc = MathUtils.sin(i * MathUtils.PI2 * pitch / rate);
            float punchClick = (i < 400) ? MathUtils.random(-0.5f, 0.5f) : 0f;
            s[i] = (short) ((osc * 0.85f + punchClick * 0.15f) * env * 31000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateBlock() {
        int rate = 44100;
        int len = (int) (rate * 0.22f);
        short[] s = new short[len];
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 38.0);
            float m1 = MathUtils.sin(t * MathUtils.PI2 * 880f);
            float m2 = MathUtils.sin(t * MathUtils.PI2 * 1420f);
            float m3 = MathUtils.sin(t * MathUtils.PI2 * 2350f);
            float noise = (i < 800) ? MathUtils.random(-0.4f, 0.4f) : 0f;
            float val = (m1 * 0.45f + m2 * 0.3f + m3 * 0.15f + noise * 0.1f) * env;
            s[i] = (short) (val * 27000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateObjective() {
        int rate = 44100;
        int len = (int) (rate * 0.75f);
        short[] s = new short[len];
        int noteSplit = (int) (rate * 0.18f);
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float f = (i < noteSplit) ? 587.33f : 880.0f; // D5 to A5
            float noteT = (i < noteSplit) ? t : (t - 0.18f);
            float env = (float) Math.exp(-noteT * 6.5);
            float osc = MathUtils.sin(t * MathUtils.PI2 * f) * 0.7f +
                        MathUtils.sin(t * MathUtils.PI2 * f * 2f) * 0.3f;
            s[i] = (short) (osc * env * 24000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateBell() {
        int rate = 44100;
        int len = (int) (rate * 2.4f);
        short[] s = new short[len];
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 1.8);
            float b1 = MathUtils.sin(t * MathUtils.PI2 * 440f) * 0.50f;
            float b2 = MathUtils.sin(t * MathUtils.PI2 * 880f) * 0.25f;
            float b3 = MathUtils.sin(t * MathUtils.PI2 * 1220f) * 0.15f;
            float b4 = MathUtils.sin(t * MathUtils.PI2 * 1760f) * 0.10f;
            float val = (b1 + b2 + b3 + b4) * env;
            s[i] = (short) (val * 28000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateSplash() {
        int rate = 44100;
        int len = (int) (rate * 0.35f);
        short[] s = new short[len];
        float lastVal = 0f;
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 16.0);
            float noise = MathUtils.random(-1f, 1f);
            lastVal = lastVal * 0.7f + noise * 0.3f;
            float bubble = MathUtils.sin(t * MathUtils.PI2 * (140f - t * 180f));
            s[i] = (short) ((lastVal * 0.65f + bubble * 0.35f) * env * 23000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateFootstep() {
        int rate = 44100;
        int len = (int) (rate * 0.08f);
        short[] s = new short[len];
        float filtered = 0f;
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (float) Math.exp(-t * 65.0);
            float noise = MathUtils.random(-1f, 1f);
            filtered = filtered * 0.75f + noise * 0.25f;
            float low = MathUtils.sin(t * MathUtils.PI2 * 115f);
            s[i] = (short) ((filtered * 0.6f + low * 0.4f) * env * 18000f);
        }
        return buildWav(s, rate);
    }

    private static byte[] generateGasHiss() {
        int rate = 44100;
        int len = (int) (rate * 1.2f);
        short[] s = new short[len];
        float smoothNoise = 0f;
        for (int i = 0; i < len; i++) {
            float t = (float) i / rate;
            float env = (t < 0.2f) ? (t / 0.2f) : (1.0f - (t - 0.2f) / 1.0f);
            float noise = MathUtils.random(-1f, 1f);
            smoothNoise = smoothNoise * 0.55f + noise * 0.45f;
            float flutter = 1.0f + 0.15f * MathUtils.sin(t * MathUtils.PI2 * 12f);
            s[i] = (short) (smoothNoise * env * flutter * 16000f);
        }
        return buildWav(s, rate);
    }

    @Override
    public void dispose() {
        if (sndClick != null) sndClick.dispose();
        if (sndWhoosh != null) sndWhoosh.dispose();
        if (sndPunch != null) sndPunch.dispose();
        if (sndKick != null) sndKick.dispose();
        if (sndBlock != null) sndBlock.dispose();
        if (sndObjective != null) sndObjective.dispose();
        if (sndBell != null) sndBell.dispose();
        if (sndSplash != null) sndSplash.dispose();
        if (sndFootstep != null) sndFootstep.dispose();
        if (sndGasHiss != null) sndGasHiss.dispose();
    }
}
