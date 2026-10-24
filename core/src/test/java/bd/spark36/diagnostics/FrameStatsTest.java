package bd.spark36.diagnostics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FrameStatsTest {

    @Test
    void warmupFramesAreDiscarded() {
        FrameStats s = new FrameStats(1f);
        for (int i = 0; i < 180; i++) assertFalse(s.record(1f / 60f)); // 3 s warmup
        assertEquals(0, s.frameCount());
    }

    @Test
    void completesOnceAfterMeasureWindowAndReportsSteadyRate() {
        FrameStats s = new FrameStats(2f);
        int completions = 0;
        for (int i = 0; i < 1000; i++) if (s.record(1f / 60f)) completions++;
        assertEquals(1, completions);
        assertEquals(60f, s.averageFps(), 0.5f);
        assertEquals(16.67f, s.percentileMs(50f), 0.05f);
    }

    @Test
    void percentileCapturesSlowFrames() {
        FrameStats s = new FrameStats(100f);
        s.record(3.5f); // consume warmup in one frame
        for (int i = 0; i < 99; i++) s.record(0.010f);
        s.record(0.050f);
        assertEquals(50f, s.maxMs(), 0.01f);
        assertEquals(10f, s.percentileMs(99f), 0.01f);
        assertEquals(50f, s.percentileMs(100f), 0.01f);
    }

    @Test
    void disabledWithoutSwitch() {
        System.clearProperty("bd.spark36.benchmarkSeconds");
        assertNull(FrameStats.fromSystemProperties());
        System.setProperty("bd.spark36.benchmarkSeconds", "abc");
        assertNull(FrameStats.fromSystemProperties());
        System.setProperty("bd.spark36.benchmarkSeconds", "5");
        assertNotNull(FrameStats.fromSystemProperties());
        System.clearProperty("bd.spark36.benchmarkSeconds");
    }
}
