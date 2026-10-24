package bd.spark36.diagnostics;

import java.util.Arrays;

/**
 * Opt-in frame-time recorder used to replace claimed FPS numbers with measured ones.
 *
 * <p>Enabled only when {@code -Dbd.spark36.benchmarkSeconds=N} is set. Frames from the first
 * {@link #WARMUP_SECONDS} are discarded (shader compilation, IBL generation, first uploads).
 * After N measured seconds {@link #record(float)} returns {@code true} so the caller can log
 * {@link #summary()} and exit. The class has no libGDX dependency so it is unit-testable.
 */
public final class FrameStats {

    public static final float WARMUP_SECONDS = 3f;
    private static final int MAX_FRAMES = 60 * 600;

    private final float measureSeconds;
    private final float[] frameMs = new float[MAX_FRAMES];
    private int count;
    private float warmupElapsed;
    private float measuredElapsed;
    private boolean finished;

    public FrameStats(float measureSeconds) {
        this.measureSeconds = measureSeconds;
    }

    /** Returns a recorder if the benchmark switch is set, otherwise {@code null}. */
    public static FrameStats fromSystemProperties() {
        String v = System.getProperty("bd.spark36.benchmarkSeconds");
        if (v == null) return null;
        try {
            float s = Float.parseFloat(v.trim());
            return s > 0f ? new FrameStats(s) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Records one frame.
     *
     * @param deltaSeconds raw (unclamped) frame delta
     * @return true exactly once, when the measurement window has just completed
     */
    public boolean record(float deltaSeconds) {
        if (finished || deltaSeconds <= 0f) return false;
        if (warmupElapsed < WARMUP_SECONDS) {
            warmupElapsed += deltaSeconds;
            return false;
        }
        if (count < MAX_FRAMES) frameMs[count++] = deltaSeconds * 1000f;
        measuredElapsed += deltaSeconds;
        if (measuredElapsed >= measureSeconds) {
            finished = true;
            return true;
        }
        return false;
    }

    public int frameCount() {
        return count;
    }

    public float averageFps() {
        return measuredElapsed > 0f ? count / measuredElapsed : 0f;
    }

    /** Frame time (ms) at the given percentile, e.g. 99 for the slowest 1% boundary. */
    public float percentileMs(float percentile) {
        if (count == 0) return 0f;
        float[] sorted = Arrays.copyOf(frameMs, count);
        Arrays.sort(sorted);
        int idx = (int) Math.ceil(percentile / 100f * count) - 1;
        if (idx < 0) idx = 0;
        if (idx >= count) idx = count - 1;
        return sorted[idx];
    }

    public float maxMs() {
        float m = 0f;
        for (int i = 0; i < count; i++) m = Math.max(m, frameMs[i]);
        return m;
    }

    public String summary() {
        float p99 = percentileMs(99f);
        return String.format(java.util.Locale.ROOT,
            "frames=%d seconds=%.1f avgFps=%.1f medianMs=%.2f p99Ms=%.2f (1%%-low %.1f fps) maxMs=%.2f",
            count, measuredElapsed, averageFps(), percentileMs(50f), p99,
            p99 > 0f ? 1000f / p99 : 0f, maxMs());
    }
}
