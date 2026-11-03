// ei file ta operating system er file manager e screenshot folder khole
package bd.spark36.capture;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class FolderOpener {

    private FolderOpener() {}

    static List<String> command(String osName, File dir) {
        String os = osName == null ? "" : osName.toLowerCase();
        String path = dir.getAbsolutePath();
        if (os.contains("win")) return Arrays.asList("explorer.exe", path);
        if (os.contains("mac") || os.contains("darwin")) return Arrays.asList("open", path);
        if (os.contains("nux") || os.contains("nix") || os.contains("bsd")) return Arrays.asList("xdg-open", path);
        return null;
    }

    public static String open(File dir) {
        try {
            if (!dir.isDirectory() && !dir.mkdirs()) return "Cannot open folder: " + dir.getPath();
            List<String> cmd = command(System.getProperty("os.name"), dir);
            if (cmd == null) return "Opening folders is not supported here: " + dir.getPath();

            new ProcessBuilder(cmd).start();
            return null;
        } catch (Exception e) {
            return "Cannot open folder: " + dir.getPath();
        }
    }
}
