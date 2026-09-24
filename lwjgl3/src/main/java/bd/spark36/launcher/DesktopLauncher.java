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

        // Ensure macOS brings this game window to the absolute front and gives it full keyboard focus
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            try {
                String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
                String pid = jvmName.split("@")[0];
                Runtime.getRuntime().exec(new String[]{
                    "osascript", "-e",
                    "tell application \"System Events\" to set frontmost of first process whose unix id is " + pid + " to true"
                });
            } catch (Throwable ignored) {}
        }

        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new SparkGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("2x12: Spark — Birth of 36 July");

        // Native Retina Display 1:1 Pixel HDPI Mode for razor-sharp rendering on macOS
        configuration.setHdpiMode(com.badlogic.gdx.graphics.glutils.HdpiMode.Pixels);

        // Launch directly in Fullscreen mode as requested by user
        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.windowed"))) {
            configuration.setWindowedMode(1440, 900);
        } else {
            try {
                configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
            } catch (Throwable t) {
                configuration.setWindowedMode(1440, 900);
            }
        }
        configuration.setResizable(true);

        // Anti-aliasing (8x MSAA) and 24-bit depth buffer for crisp clean polygon edges
        configuration.setBackBufferConfig(8, 8, 8, 8, 24, 0, 8);
        configuration.useVsync(true);
        int refreshRate = 60;
        try {
            refreshRate = Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate;
        } catch (Throwable ignored) {
            refreshRate = 60;
        }
        configuration.setForegroundFPS(refreshRate);
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
