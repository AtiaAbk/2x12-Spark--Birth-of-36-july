// ei file ta opengl er draw call, triangle ar texture memory track kore
package bd.spark36.diagnostics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

public final class GlStats {

    public enum Phase { UPDATE, SHADOW, SKY, WORLD3D, OVERLAYS, POST, PARTICLES, HUD, OTHER }

    public static final boolean ENABLED = "true".equalsIgnoreCase(System.getProperty("bd.spark36.glProfile"));
    private static final boolean SYNC = "true".equalsIgnoreCase(System.getProperty("bd.spark36.glProfileSync"));

    private static final int PHASES = Phase.values().length;
    private static final Phase[] PHASE_VALUES = Phase.values();
    private static final long[] phaseNanos = new long[PHASES];
    private static final int[] phaseDraws = new int[PHASES];
    private static GLProfiler profiler;
    private static long phaseStart;
    private static int drawsAtPhaseStart;
    private static long frameStart;
    private static long frameNanos;
    private static long windowStart;
    private static int frames;
    private static long sumDraws, sumBinds, sumShaders, sumCalls;
    private static double sumVerts;
    private static final StringBuilder sb = new StringBuilder(512);

    private GlStats() {}

    public static void beginFrame() {
        if (!ENABLED) return;
        if (profiler == null) {
            profiler = new GLProfiler(Gdx.graphics);
            profiler.enable();
            windowStart = System.nanoTime();
        }
        profiler.reset();
        frameStart = System.nanoTime();
        phaseStart = frameStart;
        drawsAtPhaseStart = 0;
    }

    public static void mark(Phase phase) {
        if (!ENABLED || profiler == null) return;
        if (SYNC) Gdx.gl.glFinish();
        long now = System.nanoTime();
        phaseNanos[phase.ordinal()] += now - phaseStart;
        int draws = profiler.getDrawCalls();
        phaseDraws[phase.ordinal()] += draws - drawsAtPhaseStart;
        drawsAtPhaseStart = draws;
        phaseStart = now;
    }

    public static void endFrame() {
        if (!ENABLED || profiler == null) return;
        long now = System.nanoTime();
        frameNanos += now - frameStart;
        frames++;
        sumDraws += profiler.getDrawCalls();
        sumBinds += profiler.getTextureBindings();
        sumShaders += profiler.getShaderSwitches();
        sumCalls += profiler.getCalls();
        sumVerts += profiler.getVertexCount().total;
        if (now - windowStart >= 1_000_000_000L) {
            log();
            windowStart = now;
        }
    }

    private static void log() {
        float n = Math.max(1, frames);
        sb.setLength(0);
        sb.append("frames=").append(frames)
          .append(" draws=").append(Math.round(sumDraws / n))
          .append(" texBinds=").append(Math.round(sumBinds / n))
          .append(" shaderSw=").append(Math.round(sumShaders / n))
          .append(" glCalls=").append(Math.round(sumCalls / n))
          .append(" verts=").append(Math.round(sumVerts / n))
          .append(" cpuMs=").append(fmt(frameNanos / n / 1e6f));
        for (int i = 0; i < PHASES; i++) {
            sb.append(' ').append(PHASE_VALUES[i].name().toLowerCase()).append('=')
              .append(fmt(phaseNanos[i] / n / 1e6f)).append("ms/")
              .append(Math.round(phaseDraws[i] / n)).append('d');
            phaseNanos[i] = 0;
            phaseDraws[i] = 0;
        }
        Gdx.app.log("GlStats", sb.toString());
        frames = 0;
        frameNanos = 0;
        sumDraws = sumBinds = sumShaders = sumCalls = 0;
        sumVerts = 0;
    }

    private static String fmt(float v) {
        return String.valueOf(Math.round(v * 100f) / 100f);
    }
}
