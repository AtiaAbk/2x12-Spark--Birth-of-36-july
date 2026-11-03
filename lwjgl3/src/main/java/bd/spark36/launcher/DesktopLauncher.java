// ei file ta desktop e game launch korar main entry point
package bd.spark36.launcher;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import bd.spark36.SparkGame;

public class DesktopLauncher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }

        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            Thread focusThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
                        String pid = jvmName.split("@")[0];
                        int[] delays = { 400, 800, 1500, 2500 };
                        for (int delay : delays) {
                            Thread.sleep(delay);
                            Runtime.getRuntime().exec(new String[] {
                                    "osascript", "-e",
                                    "tell application \"System Events\" to set frontmost of first process whose unix id is "
                                            + pid + " to true"
                            });
                            Runtime.getRuntime().exec(new String[] {
                                    "osascript", "-e",
                                    "tell application \"System Events\" to set frontmost of (every process whose name contains \"java\") to true"
                            });
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
            focusThread.setDaemon(true);
            focusThread.start();
        }

        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new SparkGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("2x12: Spark — Birth of 36 July");

        configuration.setHdpiMode(com.badlogic.gdx.graphics.glutils.HdpiMode.Pixels);

        int customW = Integer.getInteger("bd.spark36.width", 0);
        int customH = Integer.getInteger("bd.spark36.height", 0);
        if (customW > 0 && customH > 0) {
            configuration.setWindowedMode(customW, customH);
        } else if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.windowed"))) {
            configuration.setWindowedMode(1440, 900);
        } else {
            try {
                configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
            } catch (Throwable t) {
                configuration.setWindowedMode(1440, 900);
            }
        }
        configuration.setResizable(true);

        configuration.setWindowListener(new com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter() {
            @Override
            public void created(com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window window) {
                try {
                    org.lwjgl.glfw.GLFW.glfwFocusWindow(window.getWindowHandle());
                    org.lwjgl.glfw.GLFW.glfwRequestWindowAttention(window.getWindowHandle());
                } catch (Throwable ignored) {
                }
            }
        });

        int msaaSamples = Integer.getInteger("bd.spark36.msaa", 8);
        configuration.setBackBufferConfig(8, 8, 8, 8, 24, 0, msaaSamples);

        if (Boolean.getBoolean("bd.spark36.angle")) {
            configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
        }
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

        try {
            configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        } catch (Exception ignored) {

        }

        return configuration;
    }
}
