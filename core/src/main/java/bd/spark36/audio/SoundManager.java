package bd.spark36.audio;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Procedural Audio Synthesizer and Sound Manager for Spark: Birth of 36 July.
 * Synthesizes 100% legally compliant, royalty-free 16-bit PCM sound effects
 * for footsteps, UI chimes, martial arts combat impacts, tear gas hiss,
 * water splashes, and the historic Freedom Bell toll, plus looping ambience beds
 * ({@link AmbienceSynth}).
 *
 * <p>All audio is generated in memory and handed to OpenAL through an in-memory file handle;
 * nothing is written to disk (earlier builds wrote .wav files relative to the working directory,
 * which put them in assets/assets/audio/).</p>
 *
 * <p>Mixer: every effect plays at {@code base * master * sfx}; the ambience bed at
 * {@code base * master * ambience}.</p>
 */
public class SoundManager implements Disposable {

    /** Looping background beds. */
    public enum Ambience { NONE, CAMPUS, CROWD }

    private static SoundManager instance;

    // Volumes set before the manager exists (startup settings) are picked up by the constructor.
    private static float initialMaster = 0.85f;
    private static float initialSfx = 1.0f;
    private static float initialAmbience = 0.6f;

    private static final float AMBIENCE_BASE_GAIN = 0.55f;

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

    private Sound ambCampus;
    private Sound ambCrowd;
    private Ambience currentAmbience = Ambience.NONE;
    private Sound ambSound;
    private long ambId = -1;

    private boolean enabled = true;
    private float masterVolume = 0.85f;
    private float sfxVolume = 1.0f;
    private float ambienceVolume = 0.6f;
    private float footstepTimer = 0f;

    public static SoundManager get() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /** The manager if something already created it, else null (never starts audio). */
    public static SoundManager getIfCreated() {
        return instance;
    }

    /** Volumes to use when the manager is created later. */
    public static void setInitialVolumes(float master, float sfx, float ambience) {
        initialMaster = clamp01(master);
        initialSfx = clamp01(sfx);
        initialAmbience = clamp01(ambience);
    }

    /** Which bed a level uses: campus birds/wind for 1-2, distant crowd for 5-6, none otherwise. */
    public static Ambience ambienceForLevel(int level) {
        if (level == 1 || level == 2) return Ambience.CAMPUS;
        if (level == 5 || level == 6) return Ambience.CROWD;
        return Ambience.NONE;
    }

    /** Effective playback gain for a sound with the given base level. */
    public static float mix(float base, float master, float channel) {
        return clamp01(base) * clamp01(master) * clamp01(channel);
    }

    public SoundManager() {
        masterVolume = initialMaster;
        sfxVolume = initialSfx;
        ambienceVolume = initialAmbience;
        init();
    }

    private void init() {
        try {
            sndClick = fromMemory("ui_click.wav", generateClick());
            sndWhoosh = fromMemory("whoosh.wav", generateWhoosh());
            sndPunch = fromMemory("punch.wav", generatePunch());
            sndKick = fromMemory("kick.wav", generateKick());
            sndBlock = fromMemory("block.wav", generateBlock());
            sndObjective = fromMemory("objective.wav", generateObjective());
            sndBell = fromMemory("bell.wav", generateBell());
            sndSplash = fromMemory("splash.wav", generateSplash());
            sndFootstep = fromMemory("footstep.wav", generateFootstep());
            sndGasHiss = fromMemory("gas_hiss.wav", generateGasHiss());

            log("Procedural Audio Synthesizer initialized in memory (10 sound effects, ambience on demand).");
        } catch (Throwable t) {
            enabled = false;
            log("Audio system disabled: " + t.getMessage());
        }
    }

    private Sound fromMemory(String name, byte[] wavData) {
        try {
            return Gdx.audio.newSound(new MemoryFileHandle(name, wavData));
        } catch (Throwable t) {
            log("Could not load sound " + name + ": " + t.getMessage());
            return null;
        }
    }

    private void sfx(Sound s, float base, float pitch) {
        if (enabled && s != null) {
            s.play(mix(base, masterVolume, sfxVolume), pitch, 0f);
        }
    }

    public void playClick() {
        sfx(sndClick, 0.75f, MathUtils.random(0.96f, 1.04f));
    }

    public void playWhoosh() {
        sfx(sndWhoosh, 0.85f, MathUtils.random(0.92f, 1.08f));
    }

    public void playPunch() {
        sfx(sndPunch, 0.95f, MathUtils.random(0.94f, 1.06f));
    }

    public void playKick() {
        sfx(sndKick, 1.0f, MathUtils.random(0.90f, 1.04f));
    }

    public void playBlock() {
        sfx(sndBlock, 0.90f, MathUtils.random(0.95f, 1.05f));
    }

    public void playObjective() {
        sfx(sndObjective, 0.90f, 1.0f);
    }

    public void playBell() {
        sfx(sndBell, 1.0f, 1.0f);
    }

    public void playSplash() {
        sfx(sndSplash, 0.80f, MathUtils.random(0.95f, 1.10f));
    }

    public void playGasHiss() {
        sfx(sndGasHiss, 0.65f, MathUtils.random(0.95f, 1.05f));
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
            sfx(sndFootstep, sprinting ? 0.45f : 0.30f, MathUtils.random(0.92f, 1.08f));
        }
    }

    // ==========================================
    // AMBIENCE BEDS
    // ==========================================

    /** Switches to the given looping bed (no-op when it is already playing). */
    public void setAmbience(Ambience which) {
        if (which == null) which = Ambience.NONE;
        if (which == currentAmbience && (which == Ambience.NONE || ambId != -1)) return;
        stopAmbience();
        currentAmbience = which;
        if (!enabled || which == Ambience.NONE) return;
        Sound s = bedFor(which);
        if (s == null) return;
        ambSound = s;
        ambId = s.loop(mix(AMBIENCE_BASE_GAIN, masterVolume, ambienceVolume));
        log("Ambience: " + which + (ambId == -1 ? " (no free voice)" : ""));
    }

    public Ambience getAmbience() {
        return currentAmbience;
    }

    private void stopAmbience() {
        if (ambSound != null && ambId != -1) {
            ambSound.stop(ambId);
        }
        ambSound = null;
        ambId = -1;
    }

    private Sound bedFor(Ambience which) {
        if (which == Ambience.CAMPUS) {
            if (ambCampus == null) ambCampus = fromMemory("amb_campus.wav",
                buildWav(AmbienceSynth.campusBirdsAndWind(1921L), AmbienceSynth.SAMPLE_RATE));
            return ambCampus;
        }
        if (which == Ambience.CROWD) {
            if (ambCrowd == null) ambCrowd = fromMemory("amb_crowd.wav",
                buildWav(AmbienceSynth.distantCrowdMurmur(2024L), AmbienceSynth.SAMPLE_RATE));
            return ambCrowd;
        }
        return null;
    }

    private void refreshAmbienceVolume() {
        if (ambSound != null && ambId != -1) {
            ambSound.setVolume(ambId, mix(AMBIENCE_BASE_GAIN, masterVolume, ambienceVolume));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) stopAmbience();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = clamp01(masterVolume);
        refreshAmbienceVolume();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = clamp01(sfxVolume);
    }

    public float getAmbienceVolume() {
        return ambienceVolume;
    }

    public void setAmbienceVolume(float ambienceVolume) {
        this.ambienceVolume = clamp01(ambienceVolume);
        refreshAmbienceVolume();
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v)) return 0f;
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static void log(String msg) {
        if (Gdx.app != null) Gdx.app.log("SoundManager", msg);
    }

    /**
     * A read-only file handle over a byte array, so OpenAL can decode generated WAV data without
     * a file on disk.
     */
    static final class MemoryFileHandle extends FileHandle {
        private final byte[] data;

        MemoryFileHandle(String name, byte[] data) {
            super(new File(name), Files.FileType.Classpath);
            this.data = data;
        }

        @Override
        public InputStream read() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public byte[] readBytes() {
            return data.clone();
        }

        @Override
        public long length() {
            return data.length;
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    // ==========================================
    // PROCEDURAL 16-BIT PCM WAV SYNTHESIS
    // ==========================================

    static byte[] buildWav(short[] samples, int sampleRate) {
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
        stopAmbience();
        if (ambCampus != null) ambCampus.dispose();
        if (ambCrowd != null) ambCrowd.dispose();
    }
}
