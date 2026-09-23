package bd.historicalgame.game;

/**
 * Global configuration for 2x12.
 *
 * Central configuration used by:
 * - Main menu
 * - Level 1
 * - Player
 * - Mission system
 * - Camera
 * - Presentation build
 */
public final class GameConfig {

    // =========================================================
    // GAME
    // =========================================================

    public static final String GAME_NAME = "2x12";
    public static final String VERSION = "0.1.0";

    // =========================================================
    // WINDOW / SCREEN
    // =========================================================

    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;

    // =========================================================
    // WORLD
    // =========================================================

    public static final int WORLD_WIDTH = 3200;
    public static final int WORLD_HEIGHT = 1800;

    // =========================================================
    // PLAYER
    // =========================================================

    /**
     * Player movement speed in world units per second.
     */
    public static final float PLAYER_SPEED = 8f;

    /**
     * Player collision radius.
     */
    public static final float PLAYER_COLLISION_RADIUS = 0.6f;

    // =========================================================
    // CAMERA
    // =========================================================

    /**
     * Third-person camera field of view.
     */
    public static final float CAMERA_FOV = 67f;

    /**
     * Camera height above player.
     */
    public static final float CAMERA_HEIGHT = 8f;

    /**
     * Camera distance behind player.
     */
    public static final float CAMERA_DISTANCE = 22f;

    /**
     * Camera near clipping plane.
     */
    public static final float CAMERA_NEAR = 0.1f;

    /**
     * Camera far clipping plane.
     */
    public static final float CAMERA_FAR = 500f;

    // =========================================================
    // MISSION
    // =========================================================

    /**
     * Distance required to reach an objective.
     */
    public static final float MISSION_COMPLETE_DISTANCE = 2.5f;

    // =========================================================
    // LEVEL 1
    // =========================================================

    /**
     * Level 1 playable boundaries.
     */
    /*
     * Level 1 uses a large exploration area.
     *
     * The playable area is intentionally much larger than the
     * current campus core so the player does not immediately
     * encounter the world boundary.
     */
    public static final float LEVEL1_MIN_X = -70f;
    public static final float LEVEL1_MAX_X = 70f;

    public static final float LEVEL1_MIN_Z = -52f;
    public static final float LEVEL1_MAX_Z = 52f;

    // =========================================================
    // LEVEL 1 OBJECTIVES
    // =========================================================

    public static final String LEVEL1_NAME =
        "WHERE IT ALL BEGAN";

    public static final String LEVEL1_PERIOD =
        "MID-JUNE 2024";

    // =========================================================
    // UI
    // =========================================================

    public static final String CONTROL_MOVE =
        "W A S D  MOVE";

    public static final String CONTROL_PAUSE =
        "ESC  PAUSE";

    public static final String CONTROL_CONFIRM =
        "ENTER  CONFIRM";

    // =========================================================
    // PERFORMANCE
    // =========================================================

    /**
     * Maximum delta time used by gameplay updates.
     *
     * Prevents large movement jumps when the game
     * temporarily loses focus or the frame rate drops.
     */
    public static final float MAX_DELTA = 0.05f;

    // =========================================================
    // PRIVATE CONSTRUCTOR
    // =========================================================

    private GameConfig() {
        // Utility class.
    }
}
