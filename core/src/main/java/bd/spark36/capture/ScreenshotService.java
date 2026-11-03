// ei file ta screenshot file disk e png hishebe save kore
package bd.spark36.capture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotService {

    public static final String DIR_PROPERTY = "bd.spark36.screenshotDir";

    public static final long MIN_FREE_BYTES = 50L * 1024L * 1024L;
    static final String USER_PREFIX = "Spark36_";
    static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final int MAX_COLLISIONS = 9999;

    public interface PngSink {
        void write(File target) throws IOException;
    }

    public interface FreeSpaceProvider {
        long usableBytes(File dir);
    }

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

    public static synchronized ScreenshotService get() {
        if (instance == null) {
            File dir = resolveDirectory(System.getProperty(DIR_PROPERTY), System.getProperty("user.home"));
            instance = new ScreenshotService(dir, File::getUsableSpace, Clock.systemDefaultZone());
        }
        return instance;
    }

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

    static String timestampName(LocalDateTime time) {
        return USER_PREFIX + STAMP.format(time);
    }

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

    public Result saveHarnessCapture(String name, PngSink sink) {
        File target = harnessFile(name);
        if (target == null) return Result.failure("Screenshot failed: invalid name '" + name + "'");
        String problem = prepareDirectory(target.getParentFile());
        if (problem != null) return Result.failure(problem);
        return writeInto(target, sink, false);
    }

    File harnessFile(String name) {
        if (name == null) return null;
        String clean = name.trim().replace('\\', '/');
        if (clean.toLowerCase().endsWith(".png")) clean = clean.substring(0, clean.length() - 4);

        if (clean.startsWith("assets/screenshots/")) clean = clean.substring("assets/screenshots/".length());
        else if (clean.startsWith("assets/")) clean = clean.substring("assets/".length());
        while (clean.startsWith("/")) clean = clean.substring(1);
        if (clean.isEmpty()) return null;
        for (String part : clean.split("/")) {
            if (part.isEmpty() || part.equals(".") || part.equals("..") || part.contains(":")) return null;
        }
        return new File(directory, clean + ".png");
    }

    private String prepareDirectory(File dir) {
        if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory()) {
            return "Screenshot failed: cannot create folder " + dir.getPath();
        }
        long usable;
        try {
            usable = freeSpace.usableBytes(dir);
        } catch (RuntimeException e) {
            usable = Long.MAX_VALUE;
        }
        if (usable >= 0 && usable < MIN_FREE_BYTES) {
            return "Screenshot not saved: low disk space (" + (usable / (1024 * 1024)) + " MB free)";
        }
        return null;
    }

    static File reserveUnique(File dir, String base) throws IOException {
        for (int i = 1; i <= MAX_COLLISIONS; i++) {
            File candidate = new File(dir, i == 1 ? base + ".png" : base + "_" + i + ".png");
            if (candidate.createNewFile()) return candidate;
        }
        return null;
    }

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

            if (part != null) {

                part.delete();
                if (target.length() == 0L) {

                    target.delete();
                }
            }
            String why = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return Result.failure("Screenshot failed: " + why);
        }
    }
}
