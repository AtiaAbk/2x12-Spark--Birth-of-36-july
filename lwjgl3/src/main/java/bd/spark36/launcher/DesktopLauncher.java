package bd.spark36.launcher;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import bd.spark36.SparkGame;

/**
 * Desktop (LWJGL3) Launcher for 2x12: Spark — Birth of 36 July.
 * Configured for crisp modern graphics on macOS Retina and PC displays.
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new SparkGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("2x12: Spark — Birth of 36 July");
        configuration.setWindowedMode(1440, 900);
        configuration.setResizable(true);

        // Anti-aliasing (4x MSAA) for clean polygon edges
        configuration.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        configuration.setInitialVisible(true);
        configuration.disableAudio(false);

        // Standard libGDX icons if available
        try {
            configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        } catch (Exception ignored) {
            // Icon fallback
        }

        return configuration;
    }
}
