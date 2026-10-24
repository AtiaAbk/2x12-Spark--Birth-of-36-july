package bd.spark36.capture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Decides where screenshots go and writes them without ever overwriting a player's image.
 *
 * <p>Folder: {@code -Dbd.spark36.screenshotDir=<path>} if given (a relative path resolves against
 * the JVM working directory, which is {@code assets/} under {@code gradlew :lwjgl3:run}), otherwise
 * {@code <user home>/Pictures/Spark36}, or {@code <user home>/Spark36Screenshots} when there is no
 * Pictures folder.
 *
 * <p>Player captures (F12, photo mode) are named {@code Spark36_yyyyMMdd-HHmmss-SSS.png}; a name
 * that already exists gets {@code _2}, {@code _3}, ... The file is reserved atomically with
 * {@link File#createNewFile()} so two captures in the same millisecond cannot collide. Nothing is
 * ever deleted except the empty placeholder of a capture whose write failed.
 *
 * <p>Automated harness captures ({@code bd.spark36.baseline}, {@code bd.spark36.test*}) keep their
 * deterministic names and are written to {@code <folder>/<name>.png}, overwriting a previous run.
 *
 * <p>This class is plain Java (no libGDX calls) so it is unit-testable; pixels are written through
 * a {@link PngSink}.
 */
public final class ScreenshotService {

    public static final String DIR_PROPERTY = "bd.spark36.screenshotDir";
    /** Refuse to write when the target volume has less than this much usable space. */
    public static final long MIN_FREE_BYTES = 50L * 1024L * 1024L;
    static final String USER_PREFIX = "Spark36_";
    static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final int MAX_COLLISIONS = 9999;

    /** Writes the PNG bytes of one capture to the given (already created) file. */
    public interface PngSink {
        void write(File target) throws IOException;
    }

    /** Usable bytes on the volume holding {@code dir}; injectable so tests can fake a full disk. */
    public interface FreeSpaceProvider {
        long usableBytes(File dir);
    }

    /** Outcome of one capture, suitable for an on-screen toast. */
    public static final class Result {
        public final boolean ok;
        public final File file;
        public final String message;

        private Result(boolean ok, File file, String message) {
            this.ok = ok;
            this.file = file;
            this.message = message;
        }

        static Result success(File file) {
            return new Result(true, file, "Screenshot saved: " + file.getName());
        }

        static Result failure(String message) {
            return new Result(false, null, message);
        }
    }

    private static ScreenshotService instance;

    private final File directory;
    private final FreeSpaceProvider freeSpace;
    private final Clock clock;

    public ScreenshotService(File directory, FreeSpaceProvider freeSpace, Clock clock) {
        this.directory = directory;
        this.freeSpace = freeSpace;
        this.clock = clock;
    }

    /** Process-wide service resolved from system properties on first use. */
    public static synchronized ScreenshotService get() {
        if (instance == null) {
            File dir = resolveDirectory(System.getProperty(DIR_PROPERTY), System.getProperty("user.home"));
            instance = new ScreenshotService(dir, File::getUsableSpace, Clock.systemDefaultZone());
        }
        return instance;
    }

    /** See the class comment for the resolution order. */
    public static File resolveDirectory(String override, String userHome) {
        if (override != null && !override.trim().isEmpty()) {
            return new File(override.trim()).getAbsoluteFile();
        }
        File home = new File(userHome == null || userHome.isEmpty() ? "." : userHome);
        File pictures = new File(home, "Pictures");
        if (pictures.isDirectory()) {
            return new File(pictures, "Spark36").getAbsoluteFile();
        }
        return new File(home, "Spark36Screenshots").getAbsoluteFile();
    }

    public File getDirectory() {
        return directory;
    }

    /** Base name (without counter or extension) for a player capture taken at {@code time}. */
    static String timestampName(LocalDateTime time) {
        return USER_PREFIX + STAMP.format(time);
    }

    /**
     * Saves a player capture under a fresh unique name. Never overwrites, never throws.
     */
    public Result saveUserCapture(PngSink sink) {
        String problem = prepareDirectory(directory);
        if (problem != null) return Result.failure(problem);

        String base = timestampName(LocalDateTime.now(clock));
        File target;
        try {
            target = reserveUnique(directory, base);
        } catch (IOException e) {
            return Result.failure("Screenshot failed: cannot create file in " + directory.getPath());
        }
        if (target == null) {
            return Result.failure("Screenshot failed: too many files named " + base);
        }
        return writeInto(target, sink, true);
    }

    /**
     * Saves an automated-harness capture at {@code <dir>/<name>.png}. Overwriting is allowed here
     * only, because harness names are deterministic by design.
     */
    public Result saveHarnessCapture(String name, PngSink sink) {
        File target = harnessFile(name);
        if (target == null) return Result.failure("Screenshot failed: invalid name '" + name + "'");
        String problem = prepareDirectory(target.getParentFile());
        if (problem != null) return Result.failure(problem);
        return writeInto(target, sink, false);
    }

    /** Maps a harness name such as {@code 00_baseline/spawn} to a file inside the folder. */
    File harnessFile(String name) {
        if (name == null) return null;
        String clean = name.trim().replace('\\', '/');
        if (clean.toLowerCase().endsWith(".png")) clean = clean.substring(0, clean.length() - 4);
        // Legacy callers addressed the old in-repo folder directly.
        if (clean.startsWith("assets/screenshots/")) clean = clean.substring("assets/screenshots/".length());
        else if (clean.startsWith("assets/")) clean = clean.substring("assets/".length());
        while (clean.startsWith("/")) clean = clean.substring(1);
        if (clean.isEmpty()) return null;
        for (String part : clean.split("/")) {
            if (part.isEmpty() || part.equals(".") || part.equals("..") || part.contains(":")) return null;
        }
        return new File(directory, clean + ".png");
    }

    /** Returns null when the folder is usable, otherwise a short message for the player. */
    private String prepareDirectory(File dir) {
        if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory()) {
            return "Screenshot failed: cannot create folder " + dir.getPath();
        }
        long usable;
        try {
            usable = freeSpace.usableBytes(dir);
        } catch (RuntimeException e) {
            usable = Long.MAX_VALUE; // unknown: do not block the capture on a failed query
        }
        if (usable >= 0 && usable < MIN_FREE_BYTES) {
            return "Screenshot not saved: low disk space (" + (usable / (1024 * 1024)) + " MB free)";
        }
        return null;
    }

    /** Atomically creates {@code base.png}, or {@code base_2.png}, {@code base_3.png}, ... */
    static File reserveUnique(File dir, String base) throws IOException {
        for (int i = 1; i <= MAX_COLLISIONS; i++) {
            File candidate = new File(dir, i == 1 ? base + ".png" : base + "_" + i + ".png");
            if (candidate.createNewFile()) return candidate;
        }
        return null;
    }

    /**
     * Player captures are written to {@code <name>.png.part} and then moved over the reserved
     * zero-byte placeholder, so a failed write never leaves a half-written PNG in the gallery.
     */
    private Result writeInto(File target, PngSink sink, boolean reservedPlaceholder) {
        File part = reservedPlaceholder ? new File(target.getParentFile(), target.getName() + ".part") : null;
        try {
            if (part == null) {
                sink.write(target);
            } else {
                sink.write(part);
                Files.move(part.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return Result.success(target);
        } catch (IOException | RuntimeException e) {
            // Only our own temp file and zero-byte placeholder are removed; images are never touched.
            if (part != null) {
                //noinspection ResultOfMethodCallIgnored
                part.delete();
                if (target.length() == 0L) {
                    //noinspection ResultOfMethodCallIgnored
                    target.delete();
                }
            }
            String why = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return Result.failure("Screenshot failed: " + why);
        }
    }
}
