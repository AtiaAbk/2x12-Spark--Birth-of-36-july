package bd.historicalgame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import bd.historicalgame.Main;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {

        /*
         * Handles macOS support and restarts the JVM when required.
         */
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }

        createApplication();
    }

    private static Lwjgl3Application createApplication() {

        return new Lwjgl3Application(
            new Main(),
            getDefaultConfiguration()
        );
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {

        Lwjgl3ApplicationConfiguration configuration =
            new Lwjgl3ApplicationConfiguration();

        // =====================================================
        // GAME WINDOW
        // =====================================================

        configuration.setTitle("2x12");

        /*
         * Start in native fullscreen, same as before — this is
         * the mode that was already known to work reliably
         * (receiving input focus immediately, etc.) on this
         * project. It's still resizable, and the player can drop
         * to a normal, resizable window at any time with F11
         * (handled in Main), or resize that window freely once
         * they're in it. Starting windowed by default caused a
         * fresh window to sometimes not receive OS input focus on
         * some systems, which made the whole game look frozen
         * (no clicks, no keys) — starting fullscreen avoids that.
         */
        configuration.setResizable(true);

        configuration.setFullscreenMode(
            Lwjgl3ApplicationConfiguration.getDisplayMode()
        );

        // =====================================================
        // VISUAL QUALITY
        // =====================================================

        /*
         * 4x MSAA smooths jagged/soft-looking edges on the 3D
         * geometry (building corners, character silhouette) that
         * otherwise contribute to the whole scene looking fuzzy.
         */
        configuration.setBackBufferConfig(
            8, 8, 8, 8,
            16, 0,
            4
        );

        // =====================================================
        // PERFORMANCE
        // =====================================================

        /*
         * Enable VSync to prevent screen tearing.
         */
        configuration.useVsync(true);

        /*
         * Match the current monitor refresh rate.
         */
        configuration.setForegroundFPS(
            Lwjgl3ApplicationConfiguration
                .getDisplayMode()
                .refreshRate
        );

        // =====================================================
        // WINDOW / INPUT
        // =====================================================

        /*
         * Allow the application to receive keyboard input
         * normally in fullscreen mode.
         */
        configuration.setInitialVisible(true);

        /*
         * Enable audio.
         */
        configuration.disableAudio(false);

        // =====================================================
        // ICON
        // =====================================================

        /*
         * These files are normally located in:
         *
         * lwjgl3/src/main/resources/
         *
         * If your project already contains these icons,
         * they will be used automatically.
         */
        configuration.setWindowIcon(
            "libgdx128.png",
            "libgdx64.png",
            "libgdx32.png",
            "libgdx16.png"
        );

        return configuration;
    }
}