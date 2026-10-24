package bd.spark36.audio;

import java.util.Random;

/**
 * Synthesises seamless looping ambience beds as 16-bit mono PCM. Pure Java (no libGDX), seeded so
 * every run produces the same audio and tests can check it.
 *
 * <p>All content is generated here; nothing is sampled or recorded, so there is no third-party
 * licence and no claim that it is a real recording of any place or date (reference section 11.1).</p>
 *
 * <p>Seamless looping: each bed is rendered {@code CROSSFADE} seconds longer than the loop and the
 * overhang is equal-power crossfaded into the head, so the last sample flows into the first.</p>
 */
public final class AmbienceSynth {

    public static final int SAMPLE_RATE = 22050;
    public static final float LOOP_SECONDS = 16f;
    static final float CROSSFADE = 1.0f;

    private AmbienceSynth() {}

    /** Open-air campus: soft wind in the trees with occasional distant bird calls (Levels 1-2). */
    public static short[] campusBirdsAndWind(long seed) {
        Random rnd = new Random(seed);
        int loop = (int) (SAMPLE_RATE * LOOP_SECONDS);
        int total = loop + (int) (SAMPLE_RATE * CROSSFADE);
        float[] buf = new float[total];

        // Wind: white noise through two one-pole low-passes, swelling on loop-periodic LFOs.
        float lp1 = 0f, lp2 = 0f;
        for (int i = 0; i < total; i++) {
            float t = (float) i / SAMPLE_RATE;
            float white = rnd.nextFloat() * 2f - 1f;
            lp1 += 0.035f * (white - lp1);
            lp2 += 0.020f * (lp1 - lp2);
            float swell = 0.55f + 0.30f * (float) Math.sin(2 * Math.PI * 2 * t / LOOP_SECONDS)
                                + 0.15f * (float) Math.sin(2 * Math.PI * 5 * t / LOOP_SECONDS + 1.3);
            float leaves = (lp1 - lp2) * 0.6f; // a touch of high rustle on gusts
            buf[i] = lp2 * 4.2f * swell + leaves * swell * swell;
        }

        // Birds: short phrases of 2-5 chirps (pitch sweeps with vibrato), quiet and spaced out.
        float tCursor = 0.4f + rnd.nextFloat();
        while (tCursor < LOOP_SECONDS - 0.8f) {
            int chirps = 2 + rnd.nextInt(4);
            float base = 2600f + rnd.nextFloat() * 2200f;
            float sweep = (rnd.nextBoolean() ? 1f : -1f) * (400f + rnd.nextFloat() * 1200f);
            float amp = 0.10f + rnd.nextFloat() * 0.12f;
            float pan = tCursor;
            for (int c = 0; c < chirps; c++) {
                float dur = 0.05f + rnd.nextFloat() * 0.09f;
                addChirp(buf, pan, dur, base * (0.92f + rnd.nextFloat() * 0.16f), sweep, amp);
                pan += dur + 0.04f + rnd.nextFloat() * 0.08f;
            }
            tCursor = pan + 0.9f + rnd.nextFloat() * 2.6f;
        }
        return finish(buf, loop, 0.55f);
    }

    /**
     * Distant crowd murmur (Levels 5-6): many unintelligible voices made of formant-filtered noise
     * with syllable-rate envelopes, low-passed for distance. No words, chants or slogans.
     */
    public static short[] distantCrowdMurmur(long seed) {
        Random rnd = new Random(seed);
        int loop = (int) (SAMPLE_RATE * LOOP_SECONDS);
        int total = loop + (int) (SAMPLE_RATE * CROSSFADE);
        float[] buf = new float[total];

        int voices = 22;
        for (int v = 0; v < voices; v++) {
            float gain = 0.5f + rnd.nextFloat() * 0.5f;
            // Two-pole resonator state
            float y1 = 0f, y2 = 0f;
            int i = 0;
            while (i < total) {
                // One syllable: 110-260 ms with a formant between 280 and 950 Hz
                int len = (int) (SAMPLE_RATE * (0.11f + rnd.nextFloat() * 0.15f));
                float f = 280f + rnd.nextFloat() * 670f;
                float r = 0.985f;
                float a1 = (float) (2 * r * Math.cos(2 * Math.PI * f / SAMPLE_RATE));
                float a2 = -r * r;
                boolean pause = rnd.nextFloat() < 0.22f; // speakers breathe
                float sylAmp = pause ? 0f : gain * (0.6f + rnd.nextFloat() * 0.4f);
                for (int k = 0; k < len && i < total; k++, i++) {
                    float env = (float) Math.sin(Math.PI * k / len);
                    float x = (rnd.nextFloat() * 2f - 1f) * env * sylAmp;
                    float y = x + a1 * y1 + a2 * y2;
                    y2 = y1;
                    y1 = y;
                    buf[i] += y * 0.02f;
                }
            }
        }

        // Distance: low-pass, plus a soft loop-periodic swell and a low rumble bed.
        float lp = 0f, rumble = 0f;
        for (int i = 0; i < total; i++) {
            float t = (float) i / SAMPLE_RATE;
            lp += 0.22f * (buf[i] - lp);
            rumble += 0.01f * ((rnd.nextFloat() * 2f - 1f) - rumble);
            float swell = 0.8f + 0.2f * (float) Math.sin(2 * Math.PI * 3 * t / LOOP_SECONDS);
            buf[i] = lp * swell + rumble * 1.5f;
        }
        return finish(buf, loop, 0.5f);
    }

    private static void addChirp(float[] buf, float startSec, float dur, float f0, float sweep, float amp) {
        int start = (int) (startSec * SAMPLE_RATE);
        int len = (int) (dur * SAMPLE_RATE);
        double phase = 0;
        for (int k = 0; k < len && start + k < buf.length; k++) {
            float u = (float) k / len;
            float env = (float) Math.sin(Math.PI * u);
            env *= env;
            float f = f0 + sweep * u + 180f * (float) Math.sin(2 * Math.PI * 38 * k / SAMPLE_RATE);
            phase += 2 * Math.PI * f / SAMPLE_RATE;
            buf[start + k] += (float) Math.sin(phase) * env * amp;
        }
    }

    /** Crossfades the overhang into the head, normalises to {@code peak} and converts to PCM16. */
    private static short[] finish(float[] buf, int loop, float peak) {
        int fade = buf.length - loop;
        float[] out = new float[loop];
        for (int i = 0; i < loop; i++) out[i] = buf[i];
        for (int i = 0; i < fade; i++) {
            double u = (double) i / fade;
            float in = (float) Math.sin(u * Math.PI / 2);
            float outGain = (float) Math.cos(u * Math.PI / 2);
            out[i] = buf[i] * in + buf[loop + i] * outGain;
        }
        float max = 1e-6f;
        for (float v : out) max = Math.max(max, Math.abs(v));
        float scale = peak / max;
        short[] pcm = new short[loop];
        for (int i = 0; i < loop; i++) {
            float v = out[i] * scale;
            if (v > 1f) v = 1f;
            if (v < -1f) v = -1f;
            pcm[i] = (short) Math.round(v * 32767f);
        }
        return pcm;
    }
}
