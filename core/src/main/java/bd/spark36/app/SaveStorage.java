package bd.spark36.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Where {@link SaveManager} keeps its JSON files. An interface so tests can use a temp folder or
 * an in-memory map instead of the player's home directory.
 */
public interface SaveStorage {

    /** Returns the file's text, or null when it does not exist or cannot be read. */
    String read(String name);

    /** Writes the file; throws when the data could not be stored. */
    void write(String name, String content) throws IOException;

    /** Human-readable location for logs and docs. */
    String describe();

    /**
     * Default per-user folder: {@code -Dbd.spark36.saveDir=<dir>} if given, otherwise
     * {@code <user.home>/.spark36}. Never the working directory, so nothing lands in assets/.
     */
    static File defaultDirectory() {
        String override = System.getProperty("bd.spark36.saveDir");
        if (override != null && !override.trim().isEmpty()) return new File(override.trim());
        return new File(System.getProperty("user.home", "."), ".spark36");
    }

    /** Plain java.io storage in one directory, with write-to-temp-then-rename so a crash cannot truncate a file. */
    final class Directory implements SaveStorage {
        private final File dir;

        public Directory(File dir) {
            this.dir = dir;
        }

        public File getDirectory() {
            return dir;
        }

        @Override
        public String read(String name) {
            File f = new File(dir, name);
            if (!f.isFile()) return null;
            try {
                return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public void write(String name, String content) throws IOException {
            if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory()) {
                throw new IOException("Cannot create save folder " + dir);
            }
            File target = new File(dir, name);
            File tmp = new File(dir, name + ".tmp");
            Files.write(tmp.toPath(), content.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        @Override
        public String describe() {
            return dir.getAbsolutePath();
        }
    }
}
