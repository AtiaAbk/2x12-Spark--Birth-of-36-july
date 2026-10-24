package bd.spark36.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScreenshotServiceTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-07-16T10:20:30.456Z"), ZoneOffset.UTC);
    private static final ScreenshotService.FreeSpaceProvider PLENTY = dir -> Long.MAX_VALUE;

    private static ScreenshotService.PngSink writing(String content) {
        return target -> Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    void timestampNameUsesMillisecondPattern() {
        assertEquals("Spark36_20260716-102030-456",
            ScreenshotService.timestampName(LocalDateTime.of(2026, 7, 16, 10, 20, 30, 456_000_000)));
    }

    @Test
    void userCaptureGetsTimestampedPngName(@TempDir Path tmp) throws IOException {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), PLENTY, FIXED);
        ScreenshotService.Result r = svc.saveUserCapture(writing("a"));
        assertTrue(r.ok, r.message);
        assertEquals("Spark36_20260716-102030-456.png", r.file.getName());
        assertEquals("a", read(r.file));
        assertFalse(new File(tmp.toFile(), r.file.getName() + ".part").exists(), "temp file must be gone");
    }

    @Test
    void collisionsGetCounterAndNeverOverwrite(@TempDir Path tmp) throws IOException {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), PLENTY, FIXED);
        Set<String> names = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            ScreenshotService.Result r = svc.saveUserCapture(writing("shot" + i));
            assertTrue(r.ok, r.message);
            assertTrue(names.add(r.file.getName()), "names must be unique");
        }
        assertTrue(names.contains("Spark36_20260716-102030-456.png"));
        assertTrue(names.contains("Spark36_20260716-102030-456_2.png"));
        assertTrue(names.contains("Spark36_20260716-102030-456_3.png"));
        assertTrue(names.contains("Spark36_20260716-102030-456_4.png"));
        assertEquals("shot0", read(new File(tmp.toFile(), "Spark36_20260716-102030-456.png")));
        assertEquals("shot3", read(new File(tmp.toFile(), "Spark36_20260716-102030-456_4.png")));
    }

    @Test
    void preexistingFileIsNotOverwritten(@TempDir Path tmp) throws IOException {
        File existing = new File(tmp.toFile(), "Spark36_20260716-102030-456.png");
        Files.write(existing.toPath(), "player's image".getBytes(StandardCharsets.UTF_8));
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), PLENTY, FIXED);
        ScreenshotService.Result r = svc.saveUserCapture(writing("new"));
        assertTrue(r.ok);
        assertEquals("Spark36_20260716-102030-456_2.png", r.file.getName());
        assertEquals("player's image", read(existing));
    }

    @Test
    void lowDiskSpaceRefusesWithoutWriting(@TempDir Path tmp) {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(),
            dir -> ScreenshotService.MIN_FREE_BYTES - 1, FIXED);
        boolean[] called = {false};
        ScreenshotService.Result r = svc.saveUserCapture(target -> called[0] = true);
        assertFalse(r.ok);
        assertTrue(r.message.toLowerCase().contains("low disk space"), r.message);
        assertFalse(called[0], "pixels must not be read or written");
        assertEquals(0, tmp.toFile().listFiles().length);

        ScreenshotService.Result h = svc.saveHarnessCapture("x", target -> called[0] = true);
        assertFalse(h.ok);
        assertFalse(called[0]);
    }

    @Test
    void enoughSpaceAtThresholdIsAccepted(@TempDir Path tmp) {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), dir -> ScreenshotService.MIN_FREE_BYTES, FIXED);
        assertTrue(svc.saveUserCapture(writing("ok")).ok);
    }

    @Test
    void writeFailureIsReportedAndLeavesNoFile(@TempDir Path tmp) {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), PLENTY, FIXED);
        ScreenshotService.Result r = svc.saveUserCapture(target -> { throw new IOException("disk full"); });
        assertFalse(r.ok);
        assertTrue(r.message.contains("disk full"), r.message);
        assertEquals(0, tmp.toFile().listFiles().length, "placeholder and temp file must be cleaned up");
    }

    @Test
    void unwritableFolderIsReportedNotThrown(@TempDir Path tmp) throws IOException {
        File blocker = new File(tmp.toFile(), "blocker");
        Files.write(blocker.toPath(), new byte[]{1});
        ScreenshotService svc = new ScreenshotService(new File(blocker, "sub"), PLENTY, FIXED);
        ScreenshotService.Result r = svc.saveUserCapture(writing("x"));
        assertFalse(r.ok);
        assertTrue(r.message.contains("cannot create folder"), r.message);
    }

    @Test
    void harnessCaptureKeepsDeterministicNameAndMayOverwrite(@TempDir Path tmp) throws IOException {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), PLENTY, FIXED);
        ScreenshotService.Result a = svc.saveHarnessCapture("00_baseline/spawn", writing("first"));
        ScreenshotService.Result b = svc.saveHarnessCapture("00_baseline/spawn", writing("second"));
        assertTrue(a.ok && b.ok);
        File expected = new File(new File(tmp.toFile(), "00_baseline"), "spawn.png");
        assertEquals(expected.getAbsolutePath(), b.file.getAbsolutePath());
        assertEquals("second", read(expected));
    }

    @Test
    void harnessNamesAreNormalisedAndConfinedToFolder(@TempDir Path tmp) {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), PLENTY, FIXED);
        assertEquals(new File(tmp.toFile(), "menu.png"), svc.harnessFile("menu.png"));
        assertEquals(new File(tmp.toFile(), "menu.png"), svc.harnessFile("assets/screenshots/menu"));
        assertEquals(new File(tmp.toFile(), "a/b.png"), svc.harnessFile("a\\b"));
        assertNull(svc.harnessFile("../escape"));
        assertNull(svc.harnessFile("a/../../escape"));
        assertNull(svc.harnessFile("C:/x"));
        assertNull(svc.harnessFile(""));
        assertNull(svc.harnessFile(null));
    }

    @Test
    void folderResolution(@TempDir Path tmp) throws IOException {
        File home = tmp.toFile();
        File override = new File(home, "custom");
        assertEquals(override.getAbsoluteFile(), ScreenshotService.resolveDirectory(override.getPath(), home.getPath()));

        // No Pictures folder: fall back to <home>/Spark36Screenshots
        assertEquals(new File(home, "Spark36Screenshots").getAbsoluteFile(),
            ScreenshotService.resolveDirectory(null, home.getPath()));
        assertEquals(new File(home, "Spark36Screenshots").getAbsoluteFile(),
            ScreenshotService.resolveDirectory("   ", home.getPath()));

        Files.createDirectories(new File(home, "Pictures").toPath());
        assertEquals(new File(new File(home, "Pictures"), "Spark36").getAbsoluteFile(),
            ScreenshotService.resolveDirectory(null, home.getPath()));
    }

    @Test
    void failingFreeSpaceQueryDoesNotBlockCapture(@TempDir Path tmp) {
        ScreenshotService svc = new ScreenshotService(tmp.toFile(), dir -> { throw new SecurityException("denied"); }, FIXED);
        assertTrue(svc.saveUserCapture(writing("x")).ok);
    }
}
