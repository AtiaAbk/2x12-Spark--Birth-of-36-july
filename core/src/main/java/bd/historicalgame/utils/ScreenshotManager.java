package bd.historicalgame.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles in-game screenshot capture.
 *
 * Screenshots are written as PNG files into a "screenshots"
 * folder created next to the game itself (resolved with
 * {@link Gdx#files}'s local file handles, i.e. relative to the
 * game's own working directory) rather than the OS Pictures
 * folder, so every capture ends up in one predictable place
 * inside the game.
 */
public final class ScreenshotManager {

    private static final String SCREENSHOT_FOLDER = "screenshots";

    private static final SimpleDateFormat FILE_TIMESTAMP =
        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private ScreenshotManager() {
        // Utility class.
    }

    /**
     * Captures the current frame buffer and saves it as a PNG
     * inside {@code screenshots/} next to the game.
     *
     * @return the saved file's path, or {@code null} if the
     *         capture failed.
     */
    public static String capture() {

        Pixmap pixmap = null;

        try {

            int width =
                Gdx.graphics.getBackBufferWidth();

            int height =
                Gdx.graphics.getBackBufferHeight();

            /*
             * getFrameBufferPixmap already returns the image the
             * right way up (it internally flips the raw GL read,
             * which comes back bottom-to-top).
             */
            pixmap =
                ScreenUtils.getFrameBufferPixmap(
                    0,
                    0,
                    width,
                    height
                );

            String fileName =
                "2x12_" +
                FILE_TIMESTAMP.format(new Date()) +
                ".png";

            FileHandle file =
                Gdx.files.local(
                    SCREENSHOT_FOLDER + "/" + fileName
                );

            PixmapIO.writePNG(file, pixmap);

            System.out.println(
                "Screenshot saved: " +
                file.file().getAbsolutePath()
            );

            return file.path();

        } catch (Exception e) {

            System.out.println(
                "Screenshot failed: " + e.getMessage()
            );

            return null;

        } finally {

            if (pixmap != null) {
                pixmap.dispose();
            }
        }
    }
}
