package bd.spark36.capture;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/** Opens the screenshot folder in the platform file manager. Never throws. */
public final class FolderOpener {

    private FolderOpener() {}

    /** Command that opens {@code dir} for the given {@code os.name}, or null if unsupported. */
    static List<String> command(String osName, File dir) {
        String os = osName == null ? "" : osName.toLowerCase();
        String path = dir.getAbsolutePath();
        if (os.contains("win")) return Arrays.asList("explorer.exe", path);
        if (os.contains("mac") || os.contains("darwin")) return Arrays.asList("open", path);
        if (os.contains("nux") || os.contains("nix") || os.contains("bsd")) return Arrays.asList("xdg-open", path);
        return null;
    }

    /** @return null on success, otherwise a short message for the player. */
    public static String open(File dir) {
        try {
            if (!dir.isDirectory() && !dir.mkdirs()) return "Cannot open folder: " + dir.getPath();
            List<String> cmd = command(System.getProperty("os.name"), dir);
            if (cmd == null) return "Opening folders is not supported here: " + dir.getPath();
            // explorer.exe reports exit code 1 even on success, so do not wait for or judge it.
            new ProcessBuilder(cmd).start();
            return null;
        } catch (Exception e) {
            return "Cannot open folder: " + dir.getPath();
        }
    }
}
