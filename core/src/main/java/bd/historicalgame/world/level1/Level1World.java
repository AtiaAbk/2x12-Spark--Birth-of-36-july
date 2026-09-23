package bd.historicalgame.world.level1;

import bd.historicalgame.assets.FontManager;
import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * LEVEL 1
 *
 * Dhaka University inspired historical campus environment.
 *
 * Design goals:
 * - Smooth third-person camera
 * - Cinematic environment
 * - Central campus road
 * - Curzon Hall inspired main building
 * - TSC inspired secondary location
 * - Library inspired location
 * - Central lawn
 * - Trees
 * - Lamps
 * - Benches
 * - Campus walls
 * - Mission markers
 * - Direction indicator
 * - Distance indicator
 * - Mini-map
 * - Atmospheric lighting
 *
 * NOTE:
 * The architecture is intentionally procedural so the game
 * can run without external 3D assets.
 */
public class Level1World implements Disposable {

    private boolean SCREENSHOT_TRIGGERED_FOR_LEVEL1 = false;


    // =========================================================
    // RENDERING
    // =========================================================

    private final ModelBatch modelBatch;
    private final Environment environment;

    private final Array<ModelInstance> instances;
    private final Array<Model> models;

    // =========================================================
    // HUD
    // =========================================================

    private final SpriteBatch spriteBatch;
    private final FontManager fontManager;
    private final ShapeRenderer hudShapes;

    /**
     * Reference pixel size for a HUD "scale" of 1.0, mirroring
     * UIManager's approach: every size actually used is rendered
     * by FreeType at its exact pixel size instead of being
     * stretched from one small bitmap font (which is what made the
     * HUD text blurry).
     */
    private static final float HUD_BASE_FONT_PX = 18f;
    private static final int HUD_MIN_FONT_PX = 12;
    private static final int HUD_MAX_FONT_PX = 96;

    /**
     * Returns a crisp font for the given HUD "scale" (same
     * convention the old {@code font.getData().setScale(scale)}
     * calls used), already sized for the current window height.
     */
    private BitmapFont hudFont(float scale) {

        int pixelSize =
            MathUtils.clamp(
                Math.round(HUD_BASE_FONT_PX * scale),
                HUD_MIN_FONT_PX,
                HUD_MAX_FONT_PX
            );

        return fontManager.get(pixelSize);
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private PerspectiveCamera camera;

    private float cameraYaw = 0f;
    private float cameraPitch = -12f;
    private static final float DEFAULT_CAMERA_YAW = 0f;
    private static final float DEFAULT_CAMERA_PITCH = -12f;
    private static final float MOUSE_SENSITIVITY = 0.18f;
    private static final float MIN_PITCH = -55f;
    private static final float MAX_PITCH = 35f;

    private final Vector3 desiredCameraPosition =
        new Vector3();

    private final Vector3 cameraLookAt =
        new Vector3();

    /*
     * Where Winds Meet cinematic third-person framing.
     * Places the camera over the shoulder with dynamic depth and smooth response.
     */
    private static final float CAMERA_HEIGHT = 2.4f;
    private static final float CAMERA_DISTANCE = 4.8f;
    private static final float CAMERA_SMOOTH = 7.5f;

    // =========================================================
    // PLAYER
    // =========================================================

    private Player player;

    private Model playerBodyModel;
    private Model playerHeadModel;

    private ModelInstance playerBody;
    private ModelInstance playerHead;

    // =========================================================
    // MISSION
    // =========================================================

    private int missionIndex = 0;

    private boolean levelCompleted = false;

    private float missionTimer = 0f;

    private float totalTime = 0f;

    // =========================================================
    // SCREENSHOT TOAST
    // =========================================================

    /**
     * Counts down from a small positive value whenever a
     * screenshot has just been saved, so the HUD can show a
     * brief "SCREENSHOT SAVED" confirmation.
     */
    private float screenshotToastTimer = 0f;

    // =========================================================
    // MAP (LOCATION TAGS)
    // =========================================================

    /**
     * When true, the mini-map is shown large and centered
     * on screen instead of the small corner radar.
     */
    private boolean mapExpanded = false;

    /**
     * World-space positions the player has manually tagged
     * on the map with T. Cleared only one tag at a time
     * (BACKSPACE), never automatically.
     */
    private final Array<Vector3> mapTags = new Array<>();

    private final Vector3[] missionTargets = {

        // Mission 1
        // Just in front of the entrance stairs - reachable on
        // foot now that Curzon Hall's walls actually block
        // walking through them.
        new Vector3(
            0f,
            1f,
            -6.0f
        ),

        // Mission 2
        new Vector3(
            0f,
            1f,
            5f
        ),

        // Mission 3
        new Vector3(
            18f,
            1f,
            -3f
        ),

        // Mission 4
        // Just south of the library entrance - reachable on foot
        // now that its walls actually block walking through them.
        new Vector3(
            -18f,
            1f,
            1.5f
        ),

        // Mission 5
        new Vector3(
            0f,
            1f,
            14f
        )
    };

    private final String[] missionTitles = {

        "CURZON HALL",

        "CENTRAL LAWN",

        "TSC",

        "DU LIBRARY",

        "RETURN TO MAIN GATE"
    };

    private final String[] missionDescriptions = {

        "Reach Curzon Hall and investigate the area.",

        "Cross the Central Lawn and investigate the campus.",

        "Visit the TSC area and look for clues.",

        "Investigate the university library area.",

        "Return to the Main Gate."
    };

    private static final float MISSION_DISTANCE = 3.2f;

    // TSC roof: the building body is 4.5f high and the roof slab
    // raises the walkable top to approximately 4.825f.
    private static final float TSC_ROOF_Y = 4.825f;
    private static final float TSC_PLAYER_CENTER_Y = TSC_ROOF_Y + 0.75f;
    private static final float TSC_ROOF_TOLERANCE = 0.30f;
/////hjgkjfy5dd
    // =========================================================
    // OBJECTIVE MARKER
    // =========================================================

    private Model objectiveModel;
    private ModelInstance objectiveMarker;

    // =========================================================
    // 36 JULY STUDENT MOVEMENT — HISTORICAL CLUES & ARCHIVES
    // =========================================================

    public static final class HistoricalClue {
        public final String title;
        public final String date;
        public final String location;
        public final String text;
        public final Vector3 position;
        public boolean discovered;

        public HistoricalClue(
            String title,
            String date,
            String location,
            String text,
            float x,
            float y,
            float z
        ) {
            this.title = title;
            this.date = date;
            this.location = location;
            this.text = text;
            this.position = new Vector3(x, y, z);
            this.discovered = false;
        }
    }

    private final Array<HistoricalClue> historicalClues = new Array<>();
    private HistoricalClue activeInspectingClue = null;
    private HistoricalClue nearbyClue = null;
    private final Array<ModelInstance> clueMarkerInstances = new Array<>();

    // =========================================================
    // WORLD COLORS
    // =========================================================

    private static final Color GRASS =
        Color.valueOf("3C5D35");

    private static final Color GRASS_DARK =
        Color.valueOf("29452A");

    private static final Color PATH =
        Color.valueOf("8A7657");

    private static final Color PATH_LIGHT =
        Color.valueOf("A58F6D");

    private static final Color BRICK =
        Color.valueOf("A85E45");

    private static final Color BRICK_DARK =
        Color.valueOf("703B31");

    private static final Color CREAM =
        Color.valueOf("D7C39A");

    private static final Color ROOF =
        Color.valueOf("653C32");

    private static final Color WOOD =
        Color.valueOf("5A3E2B");

    private static final Color METAL =
        Color.valueOf("343A35");

    private static final Color GOLD =
        Color.valueOf("E6B84A");

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    
    // COMMIT_H_CAMERA_ZOOM
    // Adjustable third-person camera zoom.
    private float cameraZoomDistance = 13.0f;

    private static final float CAMERA_ZOOM_MIN = 5.0f;
    private static final float CAMERA_ZOOM_MAX = 30.0f;
    private static final float CAMERA_ZOOM_STEP = 1.25f;

public Level1World() {

        modelBatch =
            new ModelBatch();

        environment =
            new Environment();

        instances =
            new Array<>();

        models =
            new Array<>();

        spriteBatch =
            new SpriteBatch();

        fontManager =
            new FontManager();

        hudShapes =
            new ShapeRenderer();

        createLighting();

        createCamera();

        createPlayer();

        createWorld();

        createObjectiveMarker();

        registerCollisions();
    }

    // =========================================================
    // LIGHTING
    // =========================================================

    private void createLighting() {

        // Where Winds Meet Golden Hour Atmosphere & Depth Fog
        environment.set(
            ColorAttribute.createAmbient(
                0.58f,
                0.56f,
                0.52f,
                1f
            )
        );

        // Soft atmospheric morning haze / depth fog
        environment.set(
            new ColorAttribute(
                ColorAttribute.Fog,
                0.75f,
                0.80f,
                0.88f,
                1f
            )
        );

        // Primary Golden Sunlight (long morning shadows through campus trees)
        environment.add(
            new DirectionalLight().set(
                1.0f,
                0.95f,
                0.82f,
                -0.55f,
                -0.85f,
                -0.45f
            )
        );

        // Secondary Soft Sky Fill Light
        environment.add(
            new DirectionalLight().set(
                0.35f,
                0.42f,
                0.50f,
                0.55f,
                0.60f,
                0.45f
            )
        );
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private void createCamera() {

        float width =
            Math.max(
                1f,
                Gdx.graphics.getWidth()
            );

        float height =
            Math.max(
                1f,
                Gdx.graphics.getHeight()
            );

        camera =
            new PerspectiveCamera(
                67f,
                width,
                height
            );

        camera.near = 0.1f;

        /*
         * Larger draw distance is required for the expanded
         * Level 1 environment and future distant scenery.
         */
        camera.far = 650f;
        resetCamera();
    }

    // =========================================================
    // PLAYER
    // =========================================================

    private void createPlayer() {

        player =
            new Player(
                0f,
                1f,
                14f,
                GameConfig.PLAYER_SPEED
            );

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        // -----------------------------------------------------
        // BODY
        // -----------------------------------------------------

        playerBodyModel =
            builder.createBox(
                0.9f,
                1.5f,
                0.65f,
                material(
                    Color.valueOf("33495C")
                ),
                attributes
            );

        models.add(
            playerBodyModel
        );

        playerBody =
            new ModelInstance(
                playerBodyModel
            );

        instances.add(
            playerBody
        );

        // -----------------------------------------------------
        // HEAD
        // -----------------------------------------------------

        playerHeadModel =
            builder.createSphere(
                0.72f,
                0.72f,
                0.72f,
                18,
                18,
                material(
                    Color.valueOf("B87850")
                ),
                attributes
            );

        models.add(
            playerHeadModel
        );

        playerHead =
            new ModelInstance(
                playerHeadModel
            );

        instances.add(
            playerHead
        );

        updatePlayerModel();
    }

    // =========================================================
    // PLAYER MODEL UPDATE
    // =========================================================

    private void updatePlayerModel() {

        if (player == null) {
            return;
        }

        float x =
            player.getX();

        float z =
            player.getZ();

        float bob =
            player.isMoving()
                ? MathUtils.sin(totalTime * 10f) * 0.035f
                : 0f;

        float y = player.getY();

        playerBody.transform.setToTranslation(
            x,
            y + bob,
            z
        );

        playerHead.transform.setToTranslation(
            x,
            y + 1.05f + bob,
            z
        );

        /*
         * Rotate player toward movement direction.
         */
        if (player.isMoving()) {

            Vector3 direction =
                player.getMovementDirection();

            float angle =
                MathUtils.atan2(
                    direction.x,
                    direction.z
                ) * MathUtils.radiansToDegrees;

            playerBody.transform.rotate(
                Vector3.Y,
                angle
            );
        }
    }

    // =========================================================
    // CAMERA FOLLOW
    // =========================================================

    private void updateCamera(float delta) {

        if (player == null) return;

        // R always restores the explicitly stored default camera state.
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetCamera();
            return;
        }

        // Mouse controls orientation only. Movement keys never touch yaw/pitch.
        if (!Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(true);
        }

        float mouseDX = Gdx.input.getDeltaX();
        float mouseDY = Gdx.input.getDeltaY();

        cameraYaw -= mouseDX * MOUSE_SENSITIVITY;
        cameraPitch += mouseDY * MOUSE_SENSITIVITY;
        cameraPitch = MathUtils.clamp(cameraPitch, MIN_PITCH, MAX_PITCH);
        cameraYaw = ((cameraYaw + 180f) % 360f + 360f) % 360f - 180f;

        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        Vector3 forward = new Vector3(
            -MathUtils.sin(yawRad),
            0f,
            -MathUtils.cos(yawRad)
        ).nor();
        Vector3 right = new Vector3(
            -forward.z,
            0f,
            forward.x
        ).nor();

        player.update(delta, forward, right);

        float pitchRad = cameraPitch * MathUtils.degreesToRadians;
        Vector3 lookDirection = new Vector3(
            forward.x * MathUtils.cos(pitchRad),
            MathUtils.sin(pitchRad),
            forward.z * MathUtils.cos(pitchRad)
        ).nor();

        Vector3 target = new Vector3(
            player.getX(),
            player.getY() + 1.25f,
            player.getZ()
        );

        float horizontalDistance = CAMERA_DISTANCE * MathUtils.cos(pitchRad);
        desiredCameraPosition.set(
            target.x - forward.x * horizontalDistance,
            target.y - lookDirection.y * CAMERA_DISTANCE,
            target.z - forward.z * horizontalDistance
        );

        float alpha = 1f - (float)Math.exp(-CAMERA_SMOOTH * delta);
        camera.position.lerp(desiredCameraPosition, alpha);
        camera.up.set(Vector3.Y);
        camera.lookAt(target);
        camera.update();
    }

    private void resetCamera() {
        cameraYaw = DEFAULT_CAMERA_YAW;
        cameraPitch = DEFAULT_CAMERA_PITCH;

        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        Vector3 forward = new Vector3(
            -MathUtils.sin(yawRad), 0f, -MathUtils.cos(yawRad)
        ).nor();

        Vector3 target = new Vector3(
            player == null ? 0f : player.getX(),
            player == null ? 1.25f : player.getY() + 1.25f,
            player == null ? 0f : player.getZ()
        );

        float pitchRad = cameraPitch * MathUtils.degreesToRadians;
        Vector3 lookDirection = new Vector3(
            forward.x * MathUtils.cos(pitchRad),
            MathUtils.sin(pitchRad),
            forward.z * MathUtils.cos(pitchRad)
        ).nor();

        camera.position.set(
            target.x - forward.x * CAMERA_DISTANCE * MathUtils.cos(pitchRad),
            target.y - lookDirection.y * CAMERA_DISTANCE,
            target.z - forward.z * CAMERA_DISTANCE * MathUtils.cos(pitchRad)
        );
        camera.up.set(Vector3.Y);
        camera.lookAt(target);
        camera.update();
    }

    // =========================================================
    // WORLD
    // =========================================================

    private void createWorld() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        createGround(
            builder,
            attributes
        );

        createCampusPaths(
            builder,
            attributes
        );

        createCentralLawn(
            builder,
            attributes
        );

        createHistoricalClues(
            builder,
            attributes
        );

        createCurzonHall(
            builder,
            attributes
        );

        createTSC(
            builder,
            attributes
        );

        createLibrary(
            builder,
            attributes
        );

        createAcademicBuildings(
            builder,
            attributes
        );

        createMainGate(
            builder,
            attributes
        );

        createTrees(
            builder,
            attributes
        );

        createBenches(
            builder,
            attributes
        );

        createLamps(
            builder,
            attributes
        );

        createDecorativeFlowers(
            builder,
            attributes
        );

        /*
         * Distant scenery forms a visual continuation beyond the
         * playable campus. The player cannot reach this scenery
         * because Player has an invisible gameplay boundary.
         */
        createDistantEnvironment(
            builder,
            attributes
        );
    }

    // =========================================================
    // MATERIAL HELPER
    // =========================================================

    private Material material(Color color) {

        return new Material(
            ColorAttribute.createDiffuse(
                color
            )
        );
    }

    // =========================================================
    // ADD BOX
    // =========================================================

    private void addBox(
        ModelBuilder builder,
        long attributes,
        float width,
        float height,
        float depth,
        Color color,
        float x,
        float y,
        float z
    ) {

        Model model =
            builder.createBox(
                width,
                height,
                depth,
                material(color),
                attributes
            );

        models.add(model);

        ModelInstance instance =
            new ModelInstance(model);

        instance.transform.setToTranslation(
            x,
            y,
            z
        );

        instances.add(instance);
    }

    // =========================================================
    // GROUND
    // =========================================================

    private void createGround(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Large campus terrain.
         *
         * The actual playable campus occupies the central region,
         * while the extra terrain provides visual breathing room
         * before the hidden world boundary.
         */
        /*
         * Very large continuous terrain.
         *
         * The player is restricted to the central playable
         * campus, but the visible ground continues far beyond
         * that area. This prevents a small rectangular map edge
         * from appearing in normal gameplay.
         */
        addBox(
            builder,
            attributes,
            360f,
            0.35f,
            300f,
            GRASS,
            0f,
            -0.2f,
            0f
        );

        /*
         * Large dark-green base layer.
         *
         * Extends beyond the primary terrain so there is no
         * abrupt dark/empty border near the playable area.
         */
        addBox(
            builder,
            attributes,
            365f,
            0.08f,
            305f,
            GRASS_DARK,
            0f,
            0.01f,
            0f
        );
    }

    // =========================================================
    // CAMPUS PATHS
    // =========================================================

    private void createCampusPaths(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Main north-south pedestrian avenue.
         *
         * The base path is deliberately wider than the original
         * prototype path to make the campus feel more walkable.
         */
        addBox(
            builder,
            attributes,
            13.5f,
            0.16f,
            42f,
            PATH,
            0f,
            0.10f,
            0f
        );

        /*
         * Subtle light stone shoulders.
         */
        addBox(
            builder,
            attributes,
            0.65f,
            0.08f,
            42f,
            PATH_LIGHT,
            -6.75f,
            0.20f,
            0f
        );

        addBox(
            builder,
            attributes,
            0.65f,
            0.08f,
            42f,
            PATH_LIGHT,
            6.75f,
            0.20f,
            0f
        );

        /*
         * Main east-west avenue.
         */
        addBox(
            builder,
            attributes,
            58f,
            0.16f,
            7.5f,
            PATH_LIGHT,
            0f,
            0.11f,
            5f
        );

        /*
         * Darker central road surface.
         */
        addBox(
            builder,
            attributes,
            58f,
            0.05f,
            5.9f,
            PATH,
            0f,
            0.20f,
            5f
        );

        /*
         * East-west path shoulders.
         */
        addBox(
            builder,
            attributes,
            58f,
            0.06f,
            0.55f,
            PATH_LIGHT,
            0f,
            0.20f,
            1.35f
        );

        addBox(
            builder,
            attributes,
            58f,
            0.06f,
            0.55f,
            PATH_LIGHT,
            0f,
            0.20f,
            8.65f
        );

        /*
         * Curzon Hall approach.
         */
        addBox(
            builder,
            attributes,
            17f,
            0.17f,
            10f,
            PATH_LIGHT,
            0f,
            0.14f,
            -8f
        );

        /*
         * Curzon approach center surface.
         */
        addBox(
            builder,
            attributes,
            13.5f,
            0.05f,
            8.8f,
            PATH,
            0f,
            0.23f,
            -8f
        );

        /*
         * Library path.
         */
        addBox(
            builder,
            attributes,
            11f,
            0.16f,
            19f,
            PATH,
            -17f,
            0.12f,
            0f
        );

        /*
         * Library path stone edge.
         */
        addBox(
            builder,
            attributes,
            0.55f,
            0.07f,
            19f,
            PATH_LIGHT,
            -22.45f,
            0.20f,
            0f
        );

        addBox(
            builder,
            attributes,
            0.55f,
            0.07f,
            19f,
            PATH_LIGHT,
            -11.55f,
            0.20f,
            0f
        );

        /*
         * TSC path.
         */
        addBox(
            builder,
            attributes,
            11f,
            0.16f,
            19f,
            PATH,
            17f,
            0.12f,
            0f
        );

        /*
         * TSC path stone edge.
         */
        addBox(
            builder,
            attributes,
            0.55f,
            0.07f,
            19f,
            PATH_LIGHT,
            11.55f,
            0.20f,
            0f
        );

        addBox(
            builder,
            attributes,
            0.55f,
            0.07f,
            19f,
            PATH_LIGHT,
            22.45f,
            0.20f,
            0f
        );

        /*
         * ----------------------------------------------------
         * WALKWAY TILE BREAKS
         * ----------------------------------------------------
         *
         * Small transverse strips break the long flat surfaces
         * into visually readable paving sections.
         */

        Color TILE =
            Color.valueOf("76644D");

        for (int z = -18; z <= 18; z += 4) {

            addBox(
                builder,
                attributes,
                12.2f,
                0.025f,
                0.10f,
                TILE,
                0f,
                0.235f,
                z
            );
        }

        for (int x = -27; x <= 27; x += 4) {

            addBox(
                builder,
                attributes,
                0.10f,
                0.025f,
                6.0f,
                TILE,
                x,
                0.235f,
                5f
            );
        }
    }

    // =========================================================
    // CENTRAL LAWN
    // =========================================================

    private void createCentralLawn(
        ModelBuilder builder,
        long attributes
    ) {

        Color LAWN =
            Color.valueOf("587C46");

        Color LAWN_EDGE =
            Color.valueOf("466638");

        Color LAWN_CENTER =
            Color.valueOf("638B50");

        /*
         * Main lawn area.
         *
         * The lawn sits beneath the central pedestrian network,
         * giving the campus a large green heart instead of a
         * single flat green rectangle.
         */
        addBox(
            builder,
            attributes,
            28f,
            0.10f,
            18f,
            LAWN,
            0f,
            0.055f,
            5f
        );

        /*
         * Darker lawn border.
         */
        addBox(
            builder,
            attributes,
            29f,
            0.045f,
            0.45f,
            LAWN_EDGE,
            0f,
            0.14f,
            -4f
        );

        addBox(
            builder,
            attributes,
            29f,
            0.045f,
            0.45f,
            LAWN_EDGE,
            0f,
            0.14f,
            14f
        );

        addBox(
            builder,
            attributes,
            0.45f,
            0.045f,
            17.5f,
            LAWN_EDGE,
            -14.25f,
            0.14f,
            5f
        );

        addBox(
            builder,
            attributes,
            0.45f,
            0.045f,
            17.5f,
            LAWN_EDGE,
            14.25f,
            0.14f,
            5f
        );

        /*
         * Decorative central oval/circle.
         */
        Model circle =
            builder.createCylinder(
                5.8f,
                0.10f,
                5.8f,
                32,
                material(LAWN_CENTER),
                attributes
            );

        models.add(circle);

        ModelInstance circleInstance =
            new ModelInstance(circle);

        circleInstance.transform.setToTranslation(
            0f,
            0.16f,
            5f
        );

        instances.add(circleInstance);

        /*
         * Smaller central feature.
         *
         * This creates visual layering without introducing a
         * gameplay obstacle.
         */
        Model innerCircle =
            builder.createCylinder(
                3.8f,
                0.055f,
                3.8f,
                32,
                material(
                    Color.valueOf("6E955A")
                ),
                attributes
            );

        models.add(innerCircle);

        ModelInstance innerCircleInstance =
            new ModelInstance(innerCircle);

        innerCircleInstance.transform.setToTranslation(
            0f,
            0.22f,
            5f
        );

        instances.add(innerCircleInstance);

        /*
         * Small decorative lawn islands.
         */
        addBox(
            builder,
            attributes,
            3.0f,
            0.06f,
            1.4f,
            LAWN_CENTER,
            -10f,
            0.13f,
            10.5f
        );

        addBox(
            builder,
            attributes,
            3.0f,
            0.06f,
            1.4f,
            LAWN_CENTER,
            10f,
            0.13f,
            10.5f
        );

        addBox(
            builder,
            attributes,
            3.0f,
            0.06f,
            1.4f,
            LAWN_CENTER,
            -10f,
            0.13f,
            -0.5f
        );

        addBox(
            builder,
            attributes,
            3.0f,
            0.06f,
            1.4f,
            LAWN_CENTER,
            10f,
            0.13f,
            -0.5f
        );
    }

    // =========================================================
    // CURZON HALL
    // =========================================================
    private void createCurzonHall(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * =====================================================
         * CURZON HALL - COMMIT A
         * CORRECT OVERALL ARCHITECTURAL SCALE / MASSING
         *
         * 1 unit ~= 1 metre
         *
         * Target proportions:
         * Overall width       ~= 88 m
         * Main body depth     ~= 22 m
         * Wing height         ~= 11.5 m
         * Central tower       ~= 20.5 m
         *
         * This commit intentionally focuses ONLY on massing.
         * Windows, arches, kiosks, eaves and ornament come later.
         * =====================================================
         */

        final float MAIN_Z = -20.5f;

        /*
         * -----------------------------------------------------
         * 1. LONG MAIN BODY
         *
         * 88m wide x 22m deep x 11.5m high
         *
         * The large horizontal mass establishes the actual
         * Curzon Hall scale relative to the player.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            88f,
            11.5f,
            22f,
            BRICK,
            0f,
            5.75f,
            MAIN_Z
        );

        /*
         * -----------------------------------------------------
         * 2. SLIGHTLY PROJECTING FRONT WING BANDS
         *
         * These create the layered front elevation instead of
         * leaving the building as one completely flat box.
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            30f,
            10.5f,
            2.5f,
            BRICK_DARK,
            -29f,
            5.25f,
            -9.25f
        );

        addBox(
            builder,
            attributes,
            30f,
            10.5f,
            2.5f,
            BRICK_DARK,
            29f,
            5.25f,
            -9.25f
        );

        /*
         * -----------------------------------------------------
         * 3. CENTRAL HALL BASE
         *
         * Wider and taller than ordinary wing bays.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            18f,
            13f,
            9f,
            BRICK,
            0f,
            6.5f,
            -14.5f
        );

        /*
         * -----------------------------------------------------
         * 4. CENTRAL PROJECTING TOWER
         *
         * Approx. 15m wide x 20.5m total height.
         *
         * This is deliberately a large architectural mass.
         * Decorative crown/kiosks will be added later.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            15f,
            20.5f,
            8f,
            BRICK,
            0f,
            10.25f,
            -16.0f
        );

        /*
         * -----------------------------------------------------
         * 5. CENTRAL FRONT PROJECTION
         *
         * Creates the strong projecting entrance-tower feeling
         * visible in the reference.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            10f,
            10f,
            3.5f,
            BRICK_DARK,
            0f,
            5f,
            -10.25f
        );

        /*
         * -----------------------------------------------------
         * 6. LEFT TERMINAL MASS
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            8f,
            13f,
            16f,
            BRICK_DARK,
            -40f,
            6.5f,
            MAIN_Z
        );

        /*
         * -----------------------------------------------------
         * 7. RIGHT TERMINAL MASS
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            8f,
            13f,
            16f,
            BRICK_DARK,
            40f,
            6.5f,
            MAIN_Z
        );

        /*
         * -----------------------------------------------------
         * 8. CENTRAL ENTRANCE / PLINTH MASS
         *
         * Wide base for the future large staircase and recessed
         * horseshoe entrance.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            8f,
            2.5f,
            5f,
            CREAM,
            0f,
            1.25f,
            -7.75f
        );

        /*
         * -----------------------------------------------------
         * 9. BROAD CENTRAL STAIRCASE BASE
         *
         * Kept intentionally simple in this commit.
         * Detailed steps/railings will be a later commit.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            10f,
            0.6f,
            6f,
            PATH_LIGHT,
            0f,
            0.3f,
            -5.0f
        );

        addBox(
            builder,
            attributes,
            8.5f,
            0.6f,
            5f,
            PATH_LIGHT,
            0f,
            0.9f,
            -7.0f
        );

        addBox(
            builder,
            attributes,
            7f,
            0.6f,
            4f,
            PATH_LIGHT,
            0f,
            1.5f,
            -8.5f
        );

        System.out.println(
            "CURZON HALL: Correct architectural scale/massing created."
        );
    
        
        // COMMIT_B_CENTRAL_THREE_WINDOWS
        /*
         * -----------------------------------------------------
         * CENTRAL TOWER — THREE TALL UPPER WINDOWS
         *
         * Reference target:
         *   - three vertically elongated openings
         *   - center opening slightly dominant
         *   - dark recessed interior
         *   - aged cream masonry framing
         *
         * This commit intentionally does not add the final
         * horseshoe/cusped arch geometry yet.
         * -----------------------------------------------------
         */

        // Left window recess
        addBox(
            builder,
            attributes,
            2.4f,
            5.2f,
            0.18f,
            Color.valueOf("30221E"),
            -3.8f,
            13.6f,
            -20.08f
        );

        // Center window recess
        addBox(
            builder,
            attributes,
            2.8f,
            5.8f,
            0.18f,
            Color.valueOf("241916"),
            0f,
            13.8f,
            -20.08f
        );

        // Right window recess
        addBox(
            builder,
            attributes,
            2.4f,
            5.2f,
            0.18f,
            Color.valueOf("30221E"),
            3.8f,
            13.6f,
            -20.08f
        );

        // Cream vertical surrounds
        addBox(
            builder,
            attributes,
            0.32f,
            5.8f,
            0.24f,
            CREAM,
            -5.15f,
            13.6f,
            -20.18f
        );

        addBox(
            builder,
            attributes,
            0.32f,
            5.8f,
            0.24f,
            CREAM,
            -2.45f,
            13.6f,
            -20.18f
        );

        addBox(
            builder,
            attributes,
            0.34f,
            6.2f,
            0.24f,
            CREAM,
            -1.55f,
            13.8f,
            -20.18f
        );

        addBox(
            builder,
            attributes,
            0.34f,
            6.2f,
            0.24f,
            CREAM,
            1.55f,
            13.8f,
            -20.18f
        );

        addBox(
            builder,
            attributes,
            0.32f,
            5.8f,
            0.24f,
            CREAM,
            2.45f,
            13.6f,
            -20.18f
        );

        addBox(
            builder,
            attributes,
            0.32f,
            5.8f,
            0.24f,
            CREAM,
            5.15f,
            13.6f,
            -20.18f
        );

        // Lower sill band
        addBox(
            builder,
            attributes,
            11.0f,
            0.35f,
            0.25f,
            CREAM,
            0f,
            10.75f,
            -20.18f
        );

        // Upper horizontal frame
        addBox(
            builder,
            attributes,
            11.0f,
            0.35f,
            0.25f,
            CREAM,
            0f,
            16.8f,
            -20.18f
        );

        System.out.println(
            "CURZON HALL: Central three upper windows added."
        );

        
        // COMMIT_C_MAIN_ENTRANCE
        /*
         * =====================================================
         * CENTRAL MAIN ENTRANCE
         *
         * Target:
         *   Width  ~= 4.2m
         *   Height ~= 7.0m
         *
         * Visual structure:
         *   - deep dark recess
         *   - heavy vertical masonry frame
         *   - curved horseshoe-style arch
         *   - raised threshold
         * =====================================================
         */

        final Color ENTRANCE_DARK =
            Color.valueOf("211714");

        final Color ARCH_CREAM =
            Color.valueOf("C9B28A");

        /*
         * -----------------------------------------------------
         * 1. DEEP DARK ENTRANCE CAVITY
         *
         * Slightly in front of the tower facade so it reads as
         * a real recessed opening rather than a flat texture.
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            4.4f,
            6.8f,
            0.35f,
            ENTRANCE_DARK,
            0f,
            3.65f,
            -20.28f
        );

        /*
         * -----------------------------------------------------
         * 2. HEAVY SIDE PIERS
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            0.65f,
            7.2f,
            0.45f,
            ARCH_CREAM,
            -2.55f,
            3.6f,
            -20.48f
        );

        addBox(
            builder,
            attributes,
            0.65f,
            7.2f,
            0.45f,
            ARCH_CREAM,
            2.55f,
            3.6f,
            -20.48f
        );

        /*
         * -----------------------------------------------------
         * 3. ARCH SPRING BAND
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            5.6f,
            0.38f,
            0.48f,
            ARCH_CREAM,
            0f,
            6.95f,
            -20.50f
        );

        /*
         * -----------------------------------------------------
         * 4. CURVED ARCH VOUSSOIRS
         *
         * Individual masonry blocks are placed along a
         * semicircular path. Each block is rotated so the
         * entrance reads as a real curved masonry arch.
         * -----------------------------------------------------
         */

        float archCenterY = 6.95f;
        float radius = 2.15f;

        float[] angles = {
            20f, 40f, 60f, 80f,
            100f, 120f, 140f, 160f
        };

        for (float angle : angles) {

            float radians =
                angle * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            float x =
                radius * com.badlogic.gdx.math.MathUtils.cos(radians);

            float y =
                archCenterY +
                radius * com.badlogic.gdx.math.MathUtils.sin(radians);

            Model archBlock =
                builder.createBox(
                    0.72f,
                    0.42f,
                    0.48f,
                    material(ARCH_CREAM),
                    attributes
                );

            models.add(archBlock);

            ModelInstance archInstance =
                new ModelInstance(archBlock);

            archInstance.transform.setToTranslation(
                x,
                y,
                -20.52f
            );

            /*
             * Tangential rotation makes the individual blocks
             * follow the curved arch.
             */
            archInstance.transform.rotate(
                Vector3.Z,
                angle - 90f
            );

            instances.add(archInstance);
        }

        /*
         * -----------------------------------------------------
         * 5. CENTRAL KEYSTONE
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            0.85f,
            0.65f,
            0.55f,
            ARCH_CREAM,
            0f,
            9.12f,
            -20.54f
        );

        /*
         * -----------------------------------------------------
         * 6. ENTRANCE THRESHOLD
         * -----------------------------------------------------
         */
        addBox(
            builder,
            attributes,
            5.8f,
            0.30f,
            1.0f,
            ARCH_CREAM,
            0f,
            0.18f,
            -19.95f
        );

        System.out.println(
            "CURZON HALL: Main recessed entrance and arch added."
        );

        
        // COMMIT_D_WING_ARCADES


// COMMIT_F_ROOF_EAVES
// Curzon Hall roof silhouette: stepped cornices + broad projecting eaves.
// No giant central dome — skyline remains historically restrained.

// ===============================
// MAIN WING ROOF / PARAPET
// ===============================

// Low roof mass behind the parapet
addBox(builder, attributes,
        86f, 0.75f, 18.0f,
        new Color(0.42f, 0.18f, 0.10f, 1f),
        0f, 11.85f, -20.5f);

// Broad projecting eave
addBox(builder, attributes,
        91f, 0.55f, 23.5f,
        Color.LIGHT_GRAY,
        0f, 11.35f, -20.5f);

// Upper stepped cornice
addBox(builder, attributes,
        89f, 0.45f, 22.5f,
        Color.LIGHT_GRAY,
        0f, 12.25f, -20.5f);

// Narrow brick parapet
addBox(builder, attributes,
        87f, 0.95f, 20.0f,
        new Color(0.50f, 0.22f, 0.12f, 1f),
        0f, 12.75f, -20.5f);

// Front shadow-catching cornice
addBox(builder, attributes,
        89f, 0.35f, 1.15f,
        Color.LIGHT_GRAY,
        0f, 10.95f, -9.05f);

// ===============================
// CENTRAL TOWER ROOF STACK
// ===============================

// Tower roof base
addBox(builder, attributes,
        16.0f, 0.55f, 9.0f,
        new Color(0.45f, 0.19f, 0.11f, 1f),
        0f, 20.15f, -16.0f);

// First projecting cornice
addBox(builder, attributes,
        17.0f, 0.42f, 9.8f,
        Color.LIGHT_GRAY,
        0f, 20.55f, -16.0f);

// Second stepped cornice
addBox(builder, attributes,
        16.4f, 0.38f, 9.3f,
        new Color(0.52f, 0.24f, 0.13f, 1f),
        0f, 20.95f, -16.0f);

// Deep tower eave
addBox(builder, attributes,
        18.0f, 0.50f, 10.2f,
        Color.LIGHT_GRAY,
        0f, 21.35f, -16.0f);

// Tower parapet
addBox(builder, attributes,
        15.6f, 0.90f, 8.7f,
        new Color(0.50f, 0.22f, 0.12f, 1f),
        0f, 21.85f, -16.0f);

// Small top platform
addBox(builder, attributes,
        13.8f, 0.38f, 7.4f,
        Color.LIGHT_GRAY,
        0f, 22.30f, -16.0f);

// ===============================
// CENTRAL FRONT EAVE / CORNICE
// ===============================

addBox(builder, attributes,
        17.5f, 0.42f, 1.20f,
        Color.LIGHT_GRAY,
        0f, 19.45f, -20.45f);

addBox(builder, attributes,
        16.8f, 0.34f, 0.95f,
        new Color(0.52f, 0.24f, 0.13f, 1f),
        0f, 19.85f, -20.50f);

// Decorative stepped horizontal band
addBox(builder, attributes,
        14.8f, 0.32f, 0.75f,
        Color.LIGHT_GRAY,
        0f, 20.35f, -20.55f);

// COMMIT_E_CENTRAL_WINDOW_NICHE
// Large cream masonry arch surrounding the three central upper windows.

// Niche side piers
addBox(builder, attributes,
        0.65f, 4.4f, 0.55f,
        Color.LIGHT_GRAY,
        -6.15f, 12.45f, -20.32f);

addBox(builder, attributes,
        0.65f, 4.4f, 0.55f,
        Color.LIGHT_GRAY,
         6.15f, 12.45f, -20.32f);

// Lower horizontal niche sill / belt
addBox(builder, attributes,
        12.3f, 0.55f, 0.55f,
        Color.LIGHT_GRAY,
        0f, 10.25f, -20.32f);

// Large curved arch ring
float nicheRadius = 6.15f;
float nicheCenterY = 10.55f;
int nicheSegments = 16;

for (int i = 0; i <= nicheSegments; i++) {
    float angle = 180f - (180f * i / nicheSegments);
    float radians = angle * MathUtils.degreesToRadians;

    float x = MathUtils.cos(radians) * nicheRadius;
    float y = nicheCenterY + MathUtils.sin(radians) * nicheRadius;

    Model nicheBlock = builder.createBox(
            0.85f, 0.42f, 0.58f,
            material(Color.LIGHT_GRAY),
            attributes
    );

    ModelInstance nicheInstance = new ModelInstance(nicheBlock);
    nicheInstance.transform.setToTranslation(x, y, -20.34f);

    // Tangential rotation follows the curvature of the arch.
    nicheInstance.transform.rotate(
            Vector3.Z,
            angle - 90f
    );

    instances.add(nicheInstance);
    models.add(nicheBlock);
}

// Second smaller stepped arch accent
float accentRadius = 5.55f;
float accentCenterY = 10.55f;
int accentSegments = 14;

for (int i = 0; i <= accentSegments; i++) {
    float angle = 180f - (180f * i / accentSegments);
    float radians = angle * MathUtils.degreesToRadians;

    float x = MathUtils.cos(radians) * accentRadius;
    float y = accentCenterY + MathUtils.sin(radians) * accentRadius;

    Model accentBlock = builder.createBox(
            0.48f, 0.26f, 0.42f,
            material(Color.LIGHT_GRAY),
            attributes
    );

    ModelInstance accentInstance = new ModelInstance(accentBlock);
    accentInstance.transform.setToTranslation(x, y, -20.39f);

    accentInstance.transform.rotate(
            Vector3.Z,
            angle - 90f
    );

    instances.add(accentInstance);
    models.add(accentBlock);
}

// Central top keystone
addBox(builder, attributes,
        1.05f, 0.72f, 0.65f,
        Color.LIGHT_GRAY,
        0f, 16.75f, -20.40f);


        /*
         * =====================================================
         * CURZON HALL — SYMMETRICAL WING ARCADES
         *
         * Long repeated arcade modules on both sides of the
         * central tower.
         *
         * Left/right sides are generated from the same geometry
         * so the main facade remains symmetrical.
         *
         * This commit focuses on:
         *   - repeated piers
         *   - dark recessed openings
         *   - curved masonry arch blocks
         *
         * Kiosks / roof / decorative details come later.
         * =====================================================
         */

        final Color WING_RECESS =
            Color.valueOf("34231E");

        final Color WING_ARCH =
            Color.valueOf("B88965");

        /*
         * Each side contains 8 visible bays.
         *
         * Bay centers are kept away from the central tower.
         */
        float[] wingBayX = {
            10.0f, 13.4f, 16.8f, 20.2f,
            23.6f, 27.0f, 30.4f, 33.8f
        };

        for (int side = -1; side <= 1; side += 2) {

            for (float baseX : wingBayX) {

                float wingX = baseX * side;

                /*
                 * -------------------------------------------------
                 * DARK RECESSED BAY
                 * -------------------------------------------------
                 */
                addBox(
                    builder,
                    attributes,
                    2.65f,
                    7.8f,
                    0.30f,
                    WING_RECESS,
                    wingX,
                    4.65f,
                    -20.30f
                );

                /*
                 * -------------------------------------------------
                 * LEFT / RIGHT VERTICAL PIERS OF BAY
                 * -------------------------------------------------
                 */
                float pierOffset = 1.55f;

                addBox(
                    builder,
                    attributes,
                    0.62f,
                    9.6f,
                    1.15f,
                    BRICK_DARK,
                    wingX - pierOffset,
                    4.8f,
                    -20.48f
                );

                addBox(
                    builder,
                    attributes,
                    0.62f,
                    9.6f,
                    1.15f,
                    BRICK_DARK,
                    wingX + pierOffset,
                    4.8f,
                    -20.48f
                );

                /*
                 * -------------------------------------------------
                 * ARCH BLOCKS
                 *
                 * Individual blocks follow a semicircular curve
                 * in the X/Y plane.
                 * -------------------------------------------------
                 */
                float wingArchRadius = 1.48f;
                float wingArchCenterY = 7.05f;

                float[] wingAngles = {
                    15f, 30f, 45f, 60f,
                    75f, 90f, 105f, 120f,
                    135f, 150f, 165f
                };

                for (float wingAngle : wingAngles) {

                    float wingRadians =
                        wingAngle * MathUtils.degreesToRadians;

                    float localX =
                        wingArchRadius *
                        MathUtils.cos(wingRadians);

                    float localY =
                        wingArchRadius *
                        MathUtils.sin(wingRadians);

                    /*
                     * Mirror the arch geometry together with
                     * the wing.
                     */
                    float finalX =
                        wingX + (localX * side);

                    float finalY =
                        wingArchCenterY + localY;

                    Model wingArchBlock =
                        builder.createBox(
                            0.52f,
                            0.34f,
                            0.42f,
                            material(WING_ARCH),
                            attributes
                        );

                    models.add(wingArchBlock);

                    ModelInstance wingArchInstance =
                        new ModelInstance(wingArchBlock);

                    wingArchInstance.transform.setToTranslation(
                        finalX,
                        finalY,
                        -20.55f
                    );

                    /*
                     * Tangential orientation.
                     */
                    float rotation =
                        wingAngle - 90f;

                    if (side < 0) {
                        rotation = -rotation;
                    }

                    wingArchInstance.transform.rotate(
                        Vector3.Z,
                        rotation
                    );

                    instances.add(wingArchInstance);
                }

                /*
                 * -------------------------------------------------
                 * SMALL ARCH BASE / SPRING BANDS
                 * -------------------------------------------------
                 */
                addBox(
                    builder,
                    attributes,
                    3.4f,
                    0.32f,
                    0.45f,
                    WING_ARCH,
                    wingX,
                    5.95f,
                    -20.55f
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * UPPER LEVEL VISUAL BAND
         *
         * A continuous horizontal architectural line makes the
         * wings read as two-storey historical masonry instead of
         * one giant box.
         * ---------------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            64f,
            0.42f,
            0.55f,
            WING_ARCH,
            0f,
            9.85f,
            -20.60f
        );

        /*
         * Central interruption restores the tower hierarchy.
         */
        addBox(
            builder,
            attributes,
            14f,
            0.55f,
            0.65f,
            BRICK_DARK,
            0f,
            10.0f,
            -20.62f
        );

        System.out.println(
            "CURZON HALL: Symmetrical wing arcades added."
        );

        // =====================================================
        // COMMIT_K_DETAILED_FACADE_WINDOWS
        // =====================================================
        /*
         * DETAILED UPPER-LEVEL FACADE WINDOWS
         *
         * The long wings receive a repeated symmetrical rhythm
         * of recessed dark windows with cream masonry frames.
         *
         * Central tower remains visually dominant.
         */

        /*
         * LEFT WING
         *
         * Six windows between the terminal mass and tower.
         */
        createDetailedCurzonWindows(
            builder,
            attributes,
            -34.5f,
            -9.5f
        );

        /*
         * RIGHT WING
         *
         * Mirrored six-window composition.
         */
        createDetailedCurzonWindows(
            builder,
            attributes,
            10.5f,
            -9.5f
        );

        System.out.println(
            "CURZON HALL: Detailed symmetrical facade windows added."
        );

        // =====================================================
        // COMMIT_L_JALI_BALUSTRADE
        // =====================================================
        /*
         * VERANDAH BALUSTRADE / JALI DETAILS
         *
         * Small repeated masonry elements are placed along the
         * upper edge of the front verandah. They are intentionally
         * low-profile so that the main arches and tower remain
         * visually dominant.
         */

        final Color JALI_MASONRY =
            Color.valueOf("C8B58B");

        final Color JALI_DARK =
            Color.valueOf("6A3A32");

        /*
         * -----------------------------------------------------
         * LONG FRONT BALUSTRADE BANDS
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            27.0f,
            0.22f,
            0.38f,
            JALI_MASONRY,
            -25.0f,
            10.25f,
            -8.05f
        );

        addBox(
            builder,
            attributes,
            27.0f,
            0.22f,
            0.38f,
            JALI_MASONRY,
            25.0f,
            10.25f,
            -8.05f
        );

        /*
         * -----------------------------------------------------
         * REPEATED BALUSTERS
         * -----------------------------------------------------
         */

        float[] jaliX = {
            -37.0f, -34.0f, -31.0f, -28.0f,
            -25.0f, -22.0f, -19.0f, -16.0f,
            16.0f,  19.0f,  22.0f,  25.0f,
            28.0f,  31.0f,  34.0f,  37.0f
        };

        for (float x : jaliX) {

            /*
             * Vertical baluster.
             */
            addBox(
                builder,
                attributes,
                0.18f,
                0.85f,
                0.18f,
                JALI_MASONRY,
                x,
                9.75f,
                -8.05f
            );

            /*
             * Small cap.
             */
            addBox(
                builder,
                attributes,
                0.30f,
                0.12f,
                0.26f,
                JALI_MASONRY,
                x,
                10.25f,
                -8.05f
            );
        }

        /*
         * -----------------------------------------------------
         * MIRRORED LOWER ACCENT
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            27.0f,
            0.20f,
            0.30f,
            JALI_DARK,
            -25.0f,
            9.20f,
            -8.02f
        );

        addBox(
            builder,
            attributes,
            27.0f,
            0.20f,
            0.30f,
            JALI_DARK,
            25.0f,
            9.20f,
            -8.02f
        );

        /*
         * -----------------------------------------------------
         * CENTRAL VERANDAH ACCENT
         * -----------------------------------------------------
         *
         * Kept short so it does not cover the main entrance.
         */

        addBox(
            builder,
            attributes,
            9.0f,
            0.20f,
            0.34f,
            JALI_MASONRY,
            0f,
            9.85f,
            -8.00f
        );

        System.out.println(
            "CURZON HALL: Verandah jali and balustrade details added."
        );

        // =====================================================
        // COMMIT_M_VERANDAH_COLUMNS
        // =====================================================
        /*
         * CONTINUOUS SHADED VERANDAH COLUMN RHYTHM
         *
         * Slender masonry columns reinforce the long verandah
         * while keeping the central entrance unobstructed.
         */

        final Color VERANDAH_COLUMN =
            Color.valueOf("C8B58B");

        final Color VERANDAH_BASE =
            Color.valueOf("8A5140");

        /*
         * -----------------------------------------------------
         * FRONT VERANDAH COLUMNS
         * -----------------------------------------------------
         */

        float[] verandahX = {
            -38.0f, -34.0f, -30.0f, -26.0f,
            -22.0f, -18.0f, -14.0f, -10.0f,
             10.0f,  14.0f,  18.0f,  22.0f,
             26.0f,  30.0f,  34.0f,  38.0f
        };

        for (float x : verandahX) {

            /*
             * Main slender shaft.
             */
            Model column =
                builder.createCylinder(
                    0.42f,
                    7.0f,
                    0.42f,
                    10,
                    material(VERANDAH_COLUMN),
                    attributes
                );

            models.add(column);

            ModelInstance columnInstance =
                new ModelInstance(column);

            columnInstance.transform.setToTranslation(
                x,
                3.9f,
                -8.15f
            );

            instances.add(columnInstance);

            /*
             * Wider square base.
             */
            addBox(
                builder,
                attributes,
                1.05f,
                0.45f,
                1.05f,
                VERANDAH_BASE,
                x,
                0.45f,
                -8.15f
            );

            /*
             * Capital block.
             */
            addBox(
                builder,
                attributes,
                1.15f,
                0.32f,
                1.15f,
                VERANDAH_COLUMN,
                x,
                7.42f,
                -8.15f
            );

            /*
             * Small upper capital.
             */
            addBox(
                builder,
                attributes,
                0.82f,
                0.22f,
                0.82f,
                VERANDAH_BASE,
                x,
                7.68f,
                -8.15f
            );
        }

        /*
         * -----------------------------------------------------
         * INNER VERANDAH SHADOW LINE
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            76.0f,
            0.28f,
            0.32f,
            VERANDAH_BASE,
            0f,
            7.82f,
            -8.08f
        );

        /*
         * -----------------------------------------------------
         * CENTRAL ENTRANCE COLUMNS
         *
         * Slightly heavier than the wing columns to emphasize
         * the main entrance.
         * -----------------------------------------------------
         */

        for (float x : new float[]{-5.15f, 5.15f}) {

            Model entranceColumn =
                builder.createCylinder(
                    0.52f,
                    7.4f,
                    0.52f,
                    10,
                    material(VERANDAH_COLUMN),
                    attributes
                );

            models.add(entranceColumn);

            ModelInstance entranceColumnInstance =
                new ModelInstance(entranceColumn);

            entranceColumnInstance.transform.setToTranslation(
                x,
                4.1f,
                -10.15f
            );

            instances.add(entranceColumnInstance);

            addBox(
                builder,
                attributes,
                1.25f,
                0.48f,
                1.25f,
                VERANDAH_BASE,
                x,
                0.48f,
                -10.15f
            );

            addBox(
                builder,
                attributes,
                1.40f,
                0.35f,
                1.40f,
                VERANDAH_COLUMN,
                x,
                7.92f,
                -10.15f
            );
        }

        System.out.println(
            "CURZON HALL: Verandah columns and capitals added."
        );

        // =====================================================
        // COMMIT_I_CHHATRI_KIOSKS
        // =====================================================
        /*
         * HISTORICAL ROOFTOP CHHATRIS / KIOSKS
         *
         * The reference architecture uses small domed terrace
         * pavilions rather than one giant central dome.
         *
         * Main tower:
         *   - four corner kiosks
         *
         * Wings:
         *   - smaller intermittent kiosks
         *
         * All placements are mirrored around X = 0.
         */

        final Color CHHATRI_COLUMN =
            Color.valueOf("D2BF99");

        final Color CHHATRI_CAP =
            Color.valueOf("784238");

        final Color CHHATRI_DARK =
            Color.valueOf("5E332C");

        /*
         * -----------------------------------------------------
         * 1. FOUR PRINCIPAL CENTRAL-TOWER CHHATRIS
         *
         * The tower roof currently reaches approximately Y=22.5.
         * These pavilions sit above that roofline without creating
         * a giant dome.
         * -----------------------------------------------------
         */

        float[] kioskX = {
            -6.1f, 6.1f
        };

        float[] kioskZ = {
            -19.0f, -13.0f
        };

        for (float kioskXPos : kioskX) {

            for (float kioskZPos : kioskZ) {

                createChhatriKiosk(
                    builder,
                    attributes,
                    kioskXPos,
                    22.75f,
                    kioskZPos,
                    1.45f,
                    2.35f
                );
            }
        }

        /*
         * -----------------------------------------------------
         * 2. SMALLER WING KIOSKS
         *
         * Intermittent roof pavilions break the long horizontal
         * silhouette and make the wings feel more historically
         * articulated.
         *
         * They are deliberately smaller than the tower kiosks.
         * -----------------------------------------------------
         */

        float[] wingKioskX = {
            -28.0f, -18.0f, 18.0f, 28.0f
        };

        for (float x : wingKioskX) {

            createChhatriKiosk(
                builder,
                attributes,
                x,
                13.75f,
                -20.5f,
                1.05f,
                1.75f
            );
        }

        /*
         * Smaller terminal accents.
         */
        createChhatriKiosk(
            builder,
            attributes,
            -37.5f,
            14.0f,
            -20.5f,
            0.90f,
            1.55f
        );

        createChhatriKiosk(
            builder,
            attributes,
            37.5f,
            14.0f,
            -20.5f,
            0.90f,
            1.55f
        );

        System.out.println(
            "CURZON HALL: Historical chhatri/kiosk roof pavilions added."
        );

        // =====================================================
        // COMMIT_J_DEEP_EAVE_BRACKETS
        // =====================================================
        /*
         * DEEP PROJECTING EAVES + REPEATED BRACKETS
         *
         * The historical roofline is articulated with strong
         * horizontal eaves and repeated supporting brackets.
         *
         * These are deliberately shallow decorative elements:
         * they enrich the silhouette without changing the main
         * architectural massing.
         */

        final Color EAVE_CREAM =
            Color.valueOf("C9B88F");

        final Color EAVE_DARK =
            Color.valueOf("754338");

        /*
         * -----------------------------------------------------
         * CENTRAL TOWER EAVE
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            15.8f,
            0.55f,
            9.15f,
            EAVE_CREAM,
            0f,
            21.55f,
            -16f
        );

        addBox(
            builder,
            attributes,
            14.9f,
            0.38f,
            8.65f,
            EAVE_DARK,
            0f,
            22.02f,
            -16f
        );

        /*
         * -----------------------------------------------------
         * CENTRAL TOWER REPEATED BRACKETS
         * -----------------------------------------------------
         */

        float[] towerBracketX = {
            -6.0f, -4.0f, -2.0f,
             2.0f,  4.0f,  6.0f
        };

        for (float x : towerBracketX) {

            addBox(
                builder,
                attributes,
                0.55f,
                1.05f,
                0.65f,
                EAVE_DARK,
                x,
                21.0f,
                -11.75f
            );

            addBox(
                builder,
                attributes,
                0.55f,
                1.05f,
                0.65f,
                EAVE_DARK,
                x,
                21.0f,
                -20.25f
            );
        }

        /*
         * -----------------------------------------------------
         * LONG WING DEEP EAVES
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            89.5f,
            0.48f,
            1.35f,
            EAVE_CREAM,
            0f,
            12.72f,
            -9.45f
        );

        addBox(
            builder,
            attributes,
            89.5f,
            0.48f,
            1.35f,
            EAVE_CREAM,
            0f,
            12.72f,
            -31.55f
        );

        /*
         * -----------------------------------------------------
         * WING BRACKET RHYTHM
         * -----------------------------------------------------
         */

        float[] bracketX = {
            -39.0f, -35.5f, -32.0f, -28.5f,
            -25.0f, -21.5f, -18.0f, -14.5f,
            -11.0f, -7.5f, -4.0f,
             4.0f,   7.5f,  11.0f,  14.5f,
             18.0f,  21.5f, 25.0f, 28.5f,
             32.0f,  35.5f, 39.0f
        };

        for (float x : bracketX) {

            addBox(
                builder,
                attributes,
                0.52f,
                0.95f,
                0.75f,
                EAVE_DARK,
                x,
                12.05f,
                -9.05f
            );

            addBox(
                builder,
                attributes,
                0.52f,
                0.95f,
                0.75f,
                EAVE_DARK,
                x,
                12.05f,
                -31.95f
            );
        }

        /*
         * -----------------------------------------------------
         * LOWER VERANDAH EAVE ACCENTS
         * -----------------------------------------------------
         */

        addBox(
            builder,
            attributes,
            89.0f,
            0.30f,
            0.75f,
            EAVE_CREAM,
            0f,
            8.55f,
            -8.55f
        );

        addBox(
            builder,
            attributes,
            89.0f,
            0.30f,
            0.75f,
            EAVE_CREAM,
            0f,
            8.55f,
            -32.45f
        );

        System.out.println(
            "CURZON HALL: Deep projecting eaves and repeated brackets added."
        );
}

    // =========================================================
    // CHHATRI / KIOSK HELPER
    // =========================================================

    private void createChhatriKiosk(
        ModelBuilder builder,
        long attributes,
        float x,
        float baseY,
        float z,
        float radius,
        float totalHeight
    ) {

        // Materials are local to this helper so the kiosk
        // can be created independently of createCurzonHall().
        final Color CHHATRI_COLUMN =
            Color.valueOf("D2BF99");

        final Color CHHATRI_CAP =
            Color.valueOf("784238");

        final Color CHHATRI_DARK =
            Color.valueOf("5E332C");

        /*
         * -----------------------------------------------------
         * SLENDER SUPPORT COLUMNS
         * -----------------------------------------------------
         */

        float columnHeight =
            totalHeight * 0.58f;

        float columnRadius =
            radius * 0.13f;

        float columnOffset =
            radius * 0.58f;

        float columnY =
            baseY + (columnHeight * 0.5f);

        float[] offsets = {
            -columnOffset,
            columnOffset
        };

        for (float dx : offsets) {

            for (float dz : offsets) {

                Model column =
                    builder.createCylinder(
                        columnRadius * 2f,
                        columnHeight,
                        columnRadius * 2f,
                        10,
                        material(CHHATRI_COLUMN),
                        attributes
                    );

                models.add(column);

                ModelInstance columnInstance =
                    new ModelInstance(column);

                columnInstance.transform.setToTranslation(
                    x + dx,
                    columnY,
                    z + dz
                );

                instances.add(columnInstance);
            }
        }

        /*
         * -----------------------------------------------------
         * SMALL PLATFORM / CAPITAL BAND
         * -----------------------------------------------------
         */

        float platformY =
            baseY + columnHeight;

        addBox(
            builder,
            attributes,
            radius * 1.75f,
            radius * 0.16f,
            radius * 1.75f,
            CHHATRI_COLUMN,
            x,
            platformY,
            z
        );

        /*
         * -----------------------------------------------------
         * DOMED CAP BASE
         * -----------------------------------------------------
         */

        float domeBaseY =
            platformY + radius * 0.10f;

        Model dome =
            builder.createSphere(
                radius * 1.55f,
                radius * 0.95f,
                radius * 1.55f,
                12,
                8,
                material(CHHATRI_CAP),
                attributes
            );

        models.add(dome);

        ModelInstance domeInstance =
            new ModelInstance(dome);

        domeInstance.transform.setToTranslation(
            x,
            domeBaseY + radius * 0.42f,
            z
        );

        instances.add(domeInstance);

        /*
         * -----------------------------------------------------
         * SMALL ONION-LIKE TOP
         * -----------------------------------------------------
         */

        Model cap =
            builder.createCone(
                radius * 0.72f,
                radius * 0.72f,
                radius * 0.72f,
                12,
                material(CHHATRI_DARK),
                attributes
            );

        models.add(cap);

        ModelInstance capInstance =
            new ModelInstance(cap);

        capInstance.transform.setToTranslation(
            x,
            baseY + totalHeight - (radius * 0.28f),
            z
        );

        instances.add(capInstance);

        /*
         * -----------------------------------------------------
         * SMALL FINIAL
         * -----------------------------------------------------
         */

        Model finial =
            builder.createSphere(
                radius * 0.34f,
                radius * 0.34f,
                radius * 0.34f,
                10,
                8,
                material(CHHATRI_COLUMN),
                attributes
            );

        models.add(finial);

        ModelInstance finialInstance =
            new ModelInstance(finial);

        finialInstance.transform.setToTranslation(
            x,
            baseY + totalHeight + radius * 0.02f,
            z
        );

        instances.add(finialInstance);
    }


    // =========================================================
    // CURZON COLUMN
    // =========================================================

    private void createCurzonColumn(
        ModelBuilder builder,
        long attributes,
        float x,
        float y,
        float z
    ) {

        Model column = builder.createCylinder(
            0.72f,
            5.6f,
            0.72f,
            16,
            material(CREAM),
            attributes
        );

        models.add(column);

        ModelInstance instance =
            new ModelInstance(column);

        instance.transform.setToTranslation(
            x,
            y,
            z
        );

        instances.add(instance);
    }


    // =========================================================
    // DETAILED CURZON WINDOWS
    // =========================================================

    private void createDetailedCurzonWindows(
        ModelBuilder builder,
        long attributes,
        float startX,
        float z
    ) {

        /*
         * Six upper-level windows per wing.
         *
         * startX differs for left/right wing so the final
         * composition remains mirrored around the tower.
         */

        for (int i = 0; i < 6; i++) {

            float x = startX + (i * 4.0f);

            /*
             * Recessed upper window.
             *
             * The window sits on the front face of the main
             * 22m-deep building body.
             */
            createCurzonWindow(
                builder,
                attributes,
                x,
                8.15f,
                z,
                2.0f,
                2.45f
            );

            /*
             * Decorative sill.
             */
            addBox(
                builder,
                attributes,
                2.45f,
                0.18f,
                0.34f,
                CREAM,
                x,
                6.82f,
                z - 0.10f
            );

            /*
             * Small upper lintel band.
             */
            addBox(
                builder,
                attributes,
                2.35f,
                0.16f,
                0.30f,
                CREAM,
                x,
                9.48f,
                z - 0.10f
            );

            /*
             * Subtle vertical masonry accents.
             */
            addBox(
                builder,
                attributes,
                0.12f,
                2.75f,
                0.30f,
                CREAM,
                x - 1.12f,
                8.15f,
                z - 0.10f
            );

            addBox(
                builder,
                attributes,
                0.12f,
                2.75f,
                0.30f,
                CREAM,
                x + 1.12f,
                8.15f,
                z - 0.10f
            );
        }
    }

    // =========================================================
    // CURZON WINDOW
    // =========================================================

    private void createCurzonWindow(
        ModelBuilder builder,
        long attributes,
        float x,
        float y,
        float z,
        float width,
        float height
    ) {

        Color frame = CREAM;
        Color glass = Color.valueOf("24343A");

        // Glass
        addBox(builder, attributes,
            width, height, 0.16f,
            glass,
            x, y, z);

        // Left frame
        addBox(builder, attributes,
            0.16f, height + 0.35f, 0.24f,
            frame,
            x - width * 0.48f,
            y,
            z - 0.04f);

        // Right frame
        addBox(builder, attributes,
            0.16f, height + 0.35f, 0.24f,
            frame,
            x + width * 0.48f,
            y,
            z - 0.04f);

        // Top frame
        addBox(builder, attributes,
            width + 0.35f, 0.18f, 0.24f,
            frame,
            x,
            y + height * 0.50f,
            z - 0.04f);

        // Bottom frame
        addBox(builder, attributes,
            width + 0.35f, 0.18f, 0.24f,
            frame,
            x,
            y - height * 0.50f,
            z - 0.04f);

        // Vertical mullion
        addBox(builder, attributes,
            0.10f, height, 0.28f,
            frame,
            x,
            y,
            z - 0.07f);

        // Horizontal mullion
        addBox(builder, attributes,
            width, 0.10f, 0.28f,
            frame,
            x,
            y,
            z - 0.07f);

        /*
         * Upper decorative arch band.
         *
         * A shallow stepped profile keeps the window historical
         * without turning every opening into a plain rectangle.
         */
        addBox(builder, attributes,
            width + 0.22f, 0.16f, 0.28f,
            frame,
            x,
            y + height * 0.57f,
            z - 0.08f);

        addBox(builder, attributes,
            width * 0.72f, 0.12f, 0.30f,
            frame,
            x,
            y + height * 0.65f,
            z - 0.08f);
    }

    // =========================================================
    // CURZON COLUMN
    // =========================================================



    private void createTower(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        addBox(
            builder,
            attributes,
            4f,
            9f,
            4f,
            BRICK_DARK,
            x,
            4.5f,
            z
        );

        Model roof =
            builder.createCone(
                3.2f,
                3.5f,
                3.2f,
                4,
                material(ROOF),
                attributes
            );

        models.add(roof);

        ModelInstance instance =
            new ModelInstance(roof);

        instance.transform.setToTranslation(
            x,
            10f,
            z
        );

        instances.add(instance);
    }

    // =========================================================
    // WINDOWS
    // =========================================================

    private void createWindowRow(
        ModelBuilder builder,
        long attributes,
        float startX,
        float z
    ) {

        for (int i = 0; i < 4; i++) {

            float x =
                startX
                    + (i * 4f);

            addBox(
                builder,
                attributes,
                1.4f,
                2f,
                0.18f,
                Color.valueOf("2C3D43"),
                x,
                3.2f,
                z
            );
        }
    }

    // =========================================================
    // TSC
    // =========================================================

    private void createTSC(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            12f,
            4.5f,
            7f,
            CREAM,
            18f,
            2.25f,
            -3f
        );

        addBox(
            builder,
            attributes,
            13f,
            0.35f,
            8f,
            ROOF,
            18f,
            4.65f,
            -3f
        );

        createWindowRow(
            builder,
            attributes,
            14f,
            -6.55f
        );
    }

    // =========================================================
    // LIBRARY
    // =========================================================

    private void createLibrary(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            12f,
            4.5f,
            7f,
            CREAM,
            -18f,
            2.25f,
            -3f
        );

        addBox(
            builder,
            attributes,
            13f,
            0.35f,
            8f,
            ROOF,
            -18f,
            4.65f,
            -3f
        );

        createWindowRow(
            builder,
            attributes,
            -24f,
            -6.55f
        );
    }

    // =========================================================
    // ACADEMIC BUILDINGS
    // =========================================================

    private void createAcademicBuildings(
        ModelBuilder builder,
        long attributes
    ) {

        addBuilding(
            builder,
            attributes,
            -20f,
            11f
        );

        addBuilding(
            builder,
            attributes,
            20f,
            11f
        );

        addBuilding(
            builder,
            attributes,
            -26f,
            -13f
        );

        addBuilding(
            builder,
            attributes,
            26f,
            -13f
        );
    }

    private void addBuilding(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        addBox(
            builder,
            attributes,
            10f,
            4f,
            6f,
            CREAM,
            x,
            2f,
            z
        );

        addBox(
            builder,
            attributes,
            11f,
            0.3f,
            7f,
            ROOF,
            x,
            4.15f,
            z
        );
    }

    // =========================================================
    // MAIN GATE
    // =========================================================

    private void createMainGate(
        ModelBuilder builder,
        long attributes
    ) {

        createGatePillar(
            builder,
            attributes,
            -10f,
            16f
        );

        createGatePillar(
            builder,
            attributes,
            10f,
            16f
        );

        /*
         * Gate beam.
         */
        addBox(
            builder,
            attributes,
            22f,
            1.3f,
            1.3f,
            CREAM,
            0f,
            7f,
            16f
        );

        /*
         * Gate pathway.
         */
        addBox(
            builder,
            attributes,
            12f,
            0.15f,
            8f,
            PATH_LIGHT,
            0f,
            0.12f,
            15f
        );
    }

    private void createGatePillar(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        addBox(
            builder,
            attributes,
            2.2f,
            7f,
            2.2f,
            CREAM,
            x,
            3.5f,
            z
        );

        Model cap =
            builder.createCone(
                1.6f,
                1.5f,
                1.6f,
                4,
                material(ROOF),
                attributes
            );

        models.add(cap);

        ModelInstance instance =
            new ModelInstance(cap);

        instance.transform.setToTranslation(
            x,
            7.8f,
            z
        );

        instances.add(instance);
    }

    // =========================================================
    // TREES
    // =========================================================

    /**
     * Level 1 foliage system.
     *
     * Important design goals:
     *
     * 1. Natural campus tree distribution.
     * 2. Larger canopy than the original prototype.
     * 3. Shared models instead of creating new models for every
     *    individual tree.
     * 4. Multiple tree sizes to avoid clone-like repetition.
     * 5. Palm trees for visual variation.
     *
     * The main walkway is intentionally framed by trees so the
     * player gets the same tree-lined campus feeling as the
     * visual reference.
     */
    private void createTrees(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * ----------------------------------------------------
         * SHARED MATERIALS
         * ----------------------------------------------------
         */

        Color TRUNK_COLOR =
            Color.valueOf("5A402B");

        Color LEAF_DARK =
            Color.valueOf("294B2B");

        Color LEAF_MID =
            Color.valueOf("37633A");

        Color LEAF_LIGHT =
            Color.valueOf("4B7842");

        Color PALM_TRUNK =
            Color.valueOf("6B4A2E");

        Color PALM_LEAF =
            Color.valueOf("3E6E39");

        /*
         * ----------------------------------------------------
         * SHARED MODELS
         * ----------------------------------------------------
         */

        Model trunkModel =
            builder.createCylinder(
                0.62f,
                4.4f,
                0.62f,
                12,
                material(TRUNK_COLOR),
                attributes
            );

        models.add(trunkModel);

        Model leafLargeModel =
            builder.createSphere(
                4.8f,
                4.1f,
                4.8f,
                14,
                12,
                material(LEAF_DARK),
                attributes
            );

        models.add(leafLargeModel);

        Model leafMediumModel =
            builder.createSphere(
                3.8f,
                3.5f,
                3.8f,
                14,
                12,
                material(LEAF_MID),
                attributes
            );

        models.add(leafMediumModel);

        Model leafSmallModel =
            builder.createSphere(
                2.8f,
                2.7f,
                2.8f,
                12,
                10,
                material(LEAF_LIGHT),
                attributes
            );

        models.add(leafSmallModel);

        /*
         * ----------------------------------------------------
         * MAIN CAMPUS TREE POSITIONS
         * ----------------------------------------------------
         *
         * These trees intentionally frame paths and open lawn
         * areas instead of randomly filling the playable space.
         */

        float[][] positions = {

            // Main avenue - west side
            {-11f, 18f},
            {-11.5f, 12f},
            {-11f, 6f},
            {-11.5f, 0f},
            {-11f, -6f},
            {-11.5f, -12f},
            {-11f, -18f},

            // Main avenue - east side
            {11f, 18f},
            {11.5f, 12f},
            {11f, 6f},
            {11.5f, 0f},
            {11f, -6f},
            {11.5f, -12f},
            {11f, -18f},

            // Central lawn background
            {-21f, 17f},
            {-15f, 19f},
            {-7f, 18f},
            {7f, 18f},
            {15f, 19f},
            {21f, 17f},

            // Central lawn sides
            {-23f, 11f},
            {-24f, 4f},
            {-23f, -4f},
            {-24f, -11f},

            {23f, 11f},
            {24f, 4f},
            {23f, -4f},
            {24f, -11f},

            // Outer campus transition
            {-31f, 20f},
            {-34f, 13f},
            {-32f, 5f},
            {-34f, -5f},
            {-31f, -14f},

            {31f, 20f},
            {34f, 13f},
            {32f, 5f},
            {34f, -5f},
            {31f, -14f},

            // Curzon approach framing
            {-16f, -18f},
            {16f, -18f},
            {-20f, -22f},
            {20f, -22f}
        };

        /*
         * Create trees with deterministic visual variation.
         */
        for (int i = 0; i < positions.length; i++) {

            float scale =
                0.82f +
                ((i % 5) * 0.09f);

            float x =
                positions[i][0];

            float z =
                positions[i][1];

            createDetailedTree(
                trunkModel,
                leafLargeModel,
                leafMediumModel,
                leafSmallModel,
                x,
                z,
                scale,
                i
            );
        }

        /*
         * ----------------------------------------------------
         * PALM TREES
         * ----------------------------------------------------
         *
         * A few palms break the visual repetition and give the
         * campus environment more Dhaka-specific character.
         */

        Model palmTrunkModel =
            builder.createCylinder(
                0.42f,
                5.8f,
                0.55f,
                10,
                material(PALM_TRUNK),
                attributes
            );

        models.add(palmTrunkModel);

        Model palmLeafModel =
            builder.createSphere(
                4.2f,
                1.2f,
                4.2f,
                12,
                6,
                material(PALM_LEAF),
                attributes
            );

        models.add(palmLeafModel);

        float[][] palms = {

            {-28f, 15f},
            {28f, 15f},
            {-29f, -9f},
            {29f, -9f},
            {-20f, 24f},
            {20f, 24f}
        };

        for (int i = 0; i < palms.length; i++) {

            createPalmTree(
                palmTrunkModel,
                palmLeafModel,
                palms[i][0],
                palms[i][1],
                0.90f + ((i % 3) * 0.08f)
            );
        }
    }

    /**
     * Creates one tree using shared models.
     *
     * The foliage is intentionally composed of multiple clusters
     * rather than one perfect sphere. This produces a more organic
     * silhouette while keeping the geometry inexpensive.
     */
    private void createDetailedTree(
        Model trunkModel,
        Model leafLargeModel,
        Model leafMediumModel,
        Model leafSmallModel,
        float x,
        float z,
        float scale,
        int variation
    ) {

        /*
         * ----------------------------------------------------
         * TRUNK
         * ----------------------------------------------------
         */

        ModelInstance trunk =
            new ModelInstance(
                trunkModel
            );

        trunk.transform
            .setToTranslation(
                x,
                2.2f * scale,
                z
            );

        trunk.transform.scale(
            scale,
            scale,
            scale
        );

        instances.add(trunk);

        /*
         * ----------------------------------------------------
         * MAIN LOWER CANOPY
         * ----------------------------------------------------
         *
         * The tree previously also placed two bare "branch"
         * cylinders here (unrotated, straight up from the
         * trunk). Because they were taller than the gap
         * between the trunk top and the leaf canopy, their
         * lower half always poked out beneath the leaves,
         * reading as a stray branch dangling out of the
         * foliage. The canopy clusters below already read as
         * a full tree without them, so they've been removed
         * rather than re-angled.
         * ----------------------------------------------------
         */

        ModelInstance lower =
            new ModelInstance(
                leafLargeModel
            );

        lower.transform
            .setToTranslation(
                x,
                5.5f * scale,
                z
            );

        lower.transform.scale(
            scale,
            scale * 0.90f,
            scale
        );

        instances.add(lower);

        /*
         * ----------------------------------------------------
         * LEFT CANOPY
         * ----------------------------------------------------
         */

        ModelInstance left =
            new ModelInstance(
                leafMediumModel
            );

        left.transform
            .setToTranslation(
                x - 2.7f * scale,
                6.1f * scale,
                z + 0.25f
            );

        left.transform.scale(
            scale * 0.82f,
            scale * 0.85f,
            scale * 0.82f
        );

        instances.add(left);

        /*
         * ----------------------------------------------------
         * RIGHT CANOPY
         * ----------------------------------------------------
         */

        ModelInstance right =
            new ModelInstance(
                leafMediumModel
            );

        right.transform
            .setToTranslation(
                x + 2.7f * scale,
                6.0f * scale,
                z - 0.20f
            );

        right.transform.scale(
            scale * 0.82f,
            scale * 0.86f,
            scale * 0.82f
        );

        instances.add(right);

        /*
         * ----------------------------------------------------
         * TOP CANOPY
         * ----------------------------------------------------
         */

        ModelInstance top =
            new ModelInstance(
                leafSmallModel
            );

        top.transform
            .setToTranslation(
                x + ((variation % 3) - 1) * 0.7f,
                8.0f * scale,
                z
            );

        top.transform.scale(
            scale * 0.90f,
            scale * 0.90f,
            scale * 0.90f
        );

        instances.add(top);

        /*
         * Extra small foliage cluster on alternating trees.
         */
        if (variation % 2 == 0) {

            ModelInstance accent =
                new ModelInstance(
                    leafSmallModel
                );

            accent.transform
                .setToTranslation(
                    x - 1.5f * scale,
                    7.3f * scale,
                    z + 1.5f * scale
                );

            accent.transform.scale(
                scale * 0.65f,
                scale * 0.70f,
                scale * 0.65f
            );

            instances.add(accent);
        }
    }

    /**
     * Creates a simplified palm silhouette.
     */
    private void createPalmTree(
        Model palmTrunkModel,
        Model palmLeafModel,
        float x,
        float z,
        float scale
    ) {

        ModelInstance trunk =
            new ModelInstance(
                palmTrunkModel
            );

        trunk.transform
            .setToTranslation(
                x,
                2.9f * scale,
                z
            );

        trunk.transform.scale(
            scale,
            scale,
            scale
        );

        instances.add(trunk);

        /*
         * Central palm crown.
         */
        ModelInstance crown =
            new ModelInstance(
                palmLeafModel
            );

        crown.transform
            .setToTranslation(
                x,
                6.0f * scale,
                z
            );

        crown.transform.scale(
            scale,
            scale,
            scale
        );

        instances.add(crown);

        /*
         * Side foliage clusters.
         */
        ModelInstance left =
            new ModelInstance(
                palmLeafModel
            );

        left.transform
            .setToTranslation(
                x - 1.9f * scale,
                6.1f * scale,
                z
            );

        left.transform.scale(
            scale * 0.65f,
            scale * 0.70f,
            scale * 0.65f
        );

        instances.add(left);

        ModelInstance right =
            new ModelInstance(
                palmLeafModel
            );

        right.transform
            .setToTranslation(
                x + 1.9f * scale,
                6.0f * scale,
                z
            );

        right.transform.scale(
            scale * 0.65f,
            scale * 0.70f,
            scale * 0.65f
        );

        instances.add(right);
    }

    // =========================================================
    // BENCHES
    // =========================================================

    private void createBenches(
        ModelBuilder builder,
        long attributes
    ) {

        float[][] positions = {

            {-8f, 5f},
            {8f, 5f},
            {-8f, 1f},
            {8f, 1f},
            {-7f, 9f},
            {7f, 9f}
        };

        for (float[] p : positions) {

            createBench(
                builder,
                attributes,
                p[0],
                p[1]
            );
        }
    }

    private void createBench(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        /*
         * Seat.
         */
        addBox(
            builder,
            attributes,
            3.5f,
            0.3f,
            0.7f,
            WOOD,
            x,
            0.9f,
            z
        );

        /*
         * Back.
         */
        addBox(
            builder,
            attributes,
            3.5f,
            1.2f,
            0.25f,
            WOOD,
            x,
            1.45f,
            z + 0.25f
        );

        /*
         * Legs.
         */
        addBox(
            builder,
            attributes,
            0.3f,
            0.9f,
            0.3f,
            METAL,
            x - 1.2f,
            0.45f,
            z
        );

        addBox(
            builder,
            attributes,
            0.3f,
            0.9f,
            0.3f,
            METAL,
            x + 1.2f,
            0.45f,
            z
        );
    }

    // =========================================================
    // LAMPS
    // =========================================================

    private void createLamps(
        ModelBuilder builder,
        long attributes
    ) {

        float[][] positions = {

            {-8f, 13f},
            {8f, 13f},
            {-8f, 7f},
            {8f, 7f},
            {-8f, 0f},
            {8f, 0f},
            {-8f, -7f},
            {8f, -7f},
            {-8f, -14f},
            {8f, -14f}
        };

        for (float[] p : positions) {

            createLamp(
                builder,
                attributes,
                p[0],
                p[1]
            );
        }
    }

    private void createLamp(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        Model pole =
            builder.createCylinder(
                0.14f,
                3.5f,
                0.14f,
                10,
                material(METAL),
                attributes
            );

        models.add(pole);

        ModelInstance poleInstance =
            new ModelInstance(pole);

        poleInstance.transform.setToTranslation(
            x,
            1.75f,
            z
        );

        instances.add(poleInstance);

        Model light =
            builder.createSphere(
                0.55f,
                0.55f,
                0.55f,
                12,
                12,
                material(GOLD),
                attributes
            );

        models.add(light);

        ModelInstance lightInstance =
            new ModelInstance(light);

        lightInstance.transform.setToTranslation(
            x,
            3.55f,
            z
        );

        instances.add(lightInstance);
    }

    // =========================================================
    // CAMPUS WALLS
    // =========================================================

    private void createCampusWalls(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * The campus boundary is now far away from the core
         * exploration area.
         *
         * These walls are temporary structural boundaries. In the
         * next environment pass they will be visually blended with
         * trees, vegetation and atmospheric depth so the player
         * cannot easily discover the world edge.
         */
        addBox(
            builder,
            attributes,
            160f,
            1.8f,
            0.5f,
            BRICK_DARK,
            0f,
            0.9f,
            -58f
        );

        addBox(
            builder,
            attributes,
            160f,
            1.8f,
            0.5f,
            BRICK_DARK,
            0f,
            0.9f,
            58f
        );

        addBox(
            builder,
            attributes,
            0.5f,
            1.8f,
            116f,
            BRICK_DARK,
            -78f,
            0.9f,
            0f
        );

        addBox(
            builder,
            attributes,
            0.5f,
            1.8f,
            116f,
            BRICK_DARK,
            78f,
            0.9f,
            0f
        );
    }


    // =========================================================
    // DISTANT ENVIRONMENT / EDGE MASKING
    // =========================================================

    /**
     * Creates a low-detail vegetation belt outside the main
     * playable campus.
     *
     * Purpose:
     * - Hide the finite playable boundary.
     * - Prevent a visible rectangular map edge.
     * - Make the campus feel larger than the actual gameplay area.
     * - Create natural visual depth without expensive terrain
     *   streaming or an infinite-world system.
     *
     * The player cannot reach these objects because Player uses
     * an invisible gameplay boundary at approximately +/-70 X
     * and +/-52 Z.
     */
    private void createDistantEnvironment(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Shared low-poly distant tree models.
         *
         * Reusing the same Model objects keeps memory and model
         * creation cost much lower than calling createTree() for
         * every distant tree.
         */

        Material distantTrunkMaterial =
            material(
                Color.valueOf("4A3829")
            );

        Material distantLeafMaterial =
            material(
                Color.valueOf("2F5130")
            );

        Model distantTrunk =
            builder.createCylinder(
                0.42f,
                4.0f,
                0.42f,
                8,
                distantTrunkMaterial,
                attributes
            );

        Model distantLeaves =
            builder.createSphere(
                4.8f,
                4.8f,
                4.8f,
                10,
                10,
                distantLeafMaterial,
                attributes
            );

        models.add(distantTrunk);
        models.add(distantLeaves);

        /*
         * Outer vegetation belt.
         *
         * Irregular spacing is intentional so the edge does not
         * look like a mathematically perfect fence.
         */

        float[][] positions = {

            // North
            {-145f, -92f},
            {-125f, -88f},
            {-104f, -94f},
            {-82f,  -90f},
            {-58f,  -96f},
            {-34f,  -91f},
            {-8f,   -95f},
            {18f,   -92f},
            {44f,   -96f},
            {70f,   -90f},
            {96f,   -94f},
            {122f,  -88f},
            {145f,  -93f},

            // South
            {-145f, 92f},
            {-120f, 96f},
            {-96f,  90f},
            {-70f,  95f},
            {-45f,  91f},
            {-18f,  96f},
            {8f,    92f},
            {34f,   97f},
            {60f,   91f},
            {86f,   95f},
            {112f,  90f},
            {140f,  96f},

            // West
            {-145f, -68f},
            {-138f, -45f},
            {-148f, -20f},
            {-140f,   5f},
            {-146f,  31f},
            {-138f,  58f},
            {-148f,  80f},

            // East
            {145f, -72f},
            {140f, -48f},
            {148f, -24f},
            {142f,   2f},
            {147f,  28f},
            {139f,  55f},
            {148f,  80f}
        };

        for (int i = 0; i < positions.length; i++) {

            float x = positions[i][0];
            float z = positions[i][1];

            /*
             * Slight deterministic variation.
             *
             * This keeps the vegetation from looking cloned while
             * remaining completely reproducible between runs.
             */
            float scale =
                0.85f +
                ((i % 5) * 0.08f);

            float y =
                2.0f;

            ModelInstance trunkInstance =
                new ModelInstance(
                    distantTrunk
                );

            trunkInstance.transform
                .setToTranslation(
                    x,
                    y,
                    z
                );

            trunkInstance.transform
                .scale(
                    scale,
                    scale,
                    scale
                );

            instances.add(
                trunkInstance
            );

            ModelInstance leavesInstance =
                new ModelInstance(
                    distantLeaves
                );

            leavesInstance.transform
                .setToTranslation(
                    x,
                    y + 3.8f * scale,
                    z
                );

            leavesInstance.transform
                .scale(
                    scale,
                    scale,
                    scale
                );

            instances.add(
                leavesInstance
            );
        }

        /*
         * Secondary vegetation layer.
         *
         * These smaller clusters fill visual gaps between the
         * main distant trees without creating a dense forest.
         */
        Model shrub =
            builder.createSphere(
                2.2f,
                1.8f,
                2.2f,
                8,
                8,
                material(
                    Color.valueOf("385C36")
                ),
                attributes
            );

        models.add(shrub);

        float[][] shrubPositions = {

            {-118f, -72f},
            {-92f, -78f},
            {-66f, -74f},
            {-40f, -82f},
            {-14f, -76f},
            {12f, -80f},
            {38f, -74f},
            {64f, -82f},
            {90f, -76f},
            {116f, -72f},

            {-116f, 74f},
            {-90f, 80f},
            {-62f, 76f},
            {-36f, 82f},
            {-10f, 76f},
            {16f, 82f},
            {42f, 76f},
            {68f, 82f},
            {94f, 76f},
            {120f, 80f},

            {-118f, -46f},
            {-124f, -18f},
            {-120f, 12f},
            {-124f, 40f},

            {118f, -50f},
            {124f, -20f},
            {120f, 10f},
            {124f, 42f}
        };

        for (int i = 0; i < shrubPositions.length; i++) {

            float scale =
                0.75f +
                ((i % 4) * 0.10f);

            ModelInstance shrubInstance =
                new ModelInstance(
                    shrub
                );

            shrubInstance.transform
                .setToTranslation(
                    shrubPositions[i][0],
                    0.9f,
                    shrubPositions[i][1]
                );

            shrubInstance.transform
                .scale(
                    scale,
                    scale,
                    scale
                );

            instances.add(
                shrubInstance
            );
        }
    }

    // =========================================================
    // FLOWERS / SMALL DECORATION
    // =========================================================

    private void createDecorativeFlowers(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Keep flowers mostly around lawn/path edges rather than
         * randomly placing them over the main walking surfaces.
         */
        float[][] flowerPositions = {

            {-11f, 10.5f},
            {-9.5f, 11.0f},
            {-8f, 10.4f},
            {-6.5f, 11.1f},

            {11f, 10.5f},
            {9.5f, 11.0f},
            {8f, 10.4f},
            {6.5f, 11.1f},

            {-11f, -0.5f},
            {-9.5f, -0.1f},
            {-8f, -0.6f},
            {-6.5f, -0.1f},

            {11f, -0.5f},
            {9.5f, -0.1f},
            {8f, -0.6f},
            {6.5f, -0.1f},

            {-24f, 10f},
            {-24.5f, 7f},
            {-24f, 4f},
            {-24.5f, 1f},

            {24f, 10f},
            {24.5f, 7f},
            {24f, 4f},
            {24.5f, 1f},

            {-5f, 15f},
            {0f, 15.5f},
            {5f, 15f},
            {-5f, -5f},
            {0f, -5.5f},
            {5f, -5f}
        };

        for (int i = 0; i < flowerPositions.length; i++) {

            float x = flowerPositions[i][0];
            float z = flowerPositions[i][1];

            Model flower =
                builder.createSphere(
                    0.16f,
                    0.16f,
                    0.16f,
                    8,
                    8,
                    material(
                        Color.valueOf(
                            i % 2 == 0
                                ? "D7A7A0"
                                : "D9C16C"
                        )
                    ),
                    attributes
                );

            models.add(flower);

            ModelInstance flowerInstance =
                new ModelInstance(flower);

            flowerInstance.transform.setToTranslation(
                x,
                0.3f,
                z
            );

            instances.add(
                flowerInstance
            );
        }
    }

    // =========================================================
    // OBJECTIVE MARKER
    // =========================================================

    private void createObjectiveMarker() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        objectiveModel =
            builder.createCylinder(
                0.8f,
                3.2f,
                0.8f,
                20,
                material(GOLD),
                attributes
            );

        models.add(
            objectiveModel
        );

        objectiveMarker =
            new ModelInstance(
                objectiveModel
            );

        instances.add(
            objectiveMarker
        );

        updateObjectiveMarker();
    }

    // =========================================================
    // 36 JULY STUDENT MOVEMENT — HISTORICAL CLUES CREATION
    // =========================================================

    private void createHistoricalClues(ModelBuilder builder, long attributes) {
        historicalClues.clear();

        // 1. DU Main Gate Entryway
        historicalClues.add(new HistoricalClue(
            "তোরণ সমাবেশ ও বৈষম্যবিরোধী শপথ",
            "জুলাই ২০২৪",
            "ঢাকা বিশ্ববিদ্যালয় তোরণ ও এন্ট্রি প্রাঙ্গণ",
            "বৈষম্যবিরোধী ছাত্র আন্দোলনের সূচনালগ্নে বিশ্ববিদ্যালয়ের প্রধান তোরণে সমবেত হন লাখো শিক্ষার্থী। এখান থেকেই অধিকার আদায়ের অগ্নিঝরা স্লোগানে মুখরিত হয়েছিল রাজপথ।",
            0f, 1.0f, 13.5f
        ));

        // 2. Central Lawn Promenade
        historicalClues.add(new HistoricalClue(
            "রক্তাক্ত জুলাই ও অমর প্রতিরোধ",
            "১৮ জুলাই ২০২৪",
            "সেন্ট্রাল লন ও রাজু ভাস্কর্য করিডোর",
            "রংপুরের বীর আবু সাঈদ ও ঢাকার মুগ্ধর আত্মত্যাগ সমগ্র জাতির বিবেককে নাড়িয়ে দেয়। সেন্ট্রাল লনের প্রতিটি দেওয়ালে তরুণরা আঁকেন মুক্তির রঙিন ক্যানভাস ও প্রতিরোধ চিত্র।",
            0f, 1.0f, 5.0f
        ));

        // 3. Curzon Hall Forecourt (Main Goal)
        historicalClues.add(new HistoricalClue(
            "৩৬ জুলাই: মুক্তির মহাসমাবেশ",
            "৫ আগস্ট (৩৬ জুলাই) ২০২৪",
            "কার্জন হল বিজ্ঞান অনুষদ চত্বর",
            "শতবর্ষের ঐতিহাসিক কার্জন হলের লাল ইটের প্রাঙ্গণে ছাত্র-জনতা উদযাপন করে বিজয়ের প্রথম প্রহর। ক্যালেন্ডারের পাতায় ৫ আগস্টকে চিরভাস্বর করে নামকরণ করা হয় '৩৬ জুলাই'—এক নবপ্রজন্মের জাগরণ।",
            0f, 1.0f, -6.0f
        ));

        // 4. TSC Pavilion
        historicalClues.add(new HistoricalClue(
            "টিএসসি সমন্বয় ও মানবিক কেন্দ্র",
            "২৪ জুলাই ২০২৪",
            "টিএসসি প্রাঙ্গণ",
            "ছাত্র-শিক্ষক কেন্দ্র (টিএসসি) পরিণত হয়েছিল আন্দোলনের সমন্বয় ও জরুরি সেবা কেন্দ্রে। ক্যাম্পাসের প্রতিটি কোণ থেকে ওষুধ ও সংহতি বার্তা পৌঁছে দেওয়া হতো এখানে।",
            18f, 1.0f, -3.0f
        ));

        // 5. DU Central Library
        historicalClues.add(new HistoricalClue(
            "জ্ঞান, দ্রোহ ও লাল রিবন",
            "৩১ জুলাই ২০২৪",
            "ঢাকা বিশ্ববিদ্যালয় লাইব্রেরি চত্বর",
            "চোখে-মুখে লাল কাপড় বেঁধে শিক্ষক ও শিক্ষার্থীদের মৌন পদযাত্রা। লাইব্রেরির সিঁড়িতে বসে তৈরি হয়েছিল একতাবদ্ধ দাবিনামা ও নতুন বাংলাদেশের প্রত্যয়।",
            -18f, 1.0f, 1.5f
        ));

        // Build 3D visual clue pedestals with glowing golden crests
        Model plinthModel = builder.createBox(
            0.8f, 1.4f, 0.8f,
            material(Color.valueOf("482C24")),
            attributes
        );
        models.add(plinthModel);

        Model crestModel = builder.createBox(
            0.5f, 0.6f, 0.15f,
            material(GOLD),
            attributes
        );
        models.add(crestModel);

        for (HistoricalClue clue : historicalClues) {
            ModelInstance plinthInstance = new ModelInstance(plinthModel);
            plinthInstance.transform.setToTranslation(clue.position.x, 0.7f, clue.position.z);
            instances.add(plinthInstance);

            ModelInstance crestInstance = new ModelInstance(crestModel);
            crestInstance.transform.setToTranslation(clue.position.x, 1.6f, clue.position.z);
            instances.add(crestInstance);
            clueMarkerInstances.add(crestInstance);

            player.addCollision(clue.position.x - 0.4f, clue.position.z - 0.4f, 0.8f, 0.8f, 1.4f);
        }
    }

    private void updateObjectiveMarker() {

        if (
            levelCompleted ||
            objectiveMarker == null
        ) {
            return;
        }

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float baseY =
            missionIndex == 2
                ? 6.35f
                : 3.8f;

        float y =
            baseY +
            MathUtils.sin(
                totalTime * 3f
            ) * 0.5f;

        objectiveMarker.transform
            .setToTranslation(
                target.x,
                y,
                target.z
            );

        objectiveMarker.transform.rotate(
            Vector3.Y,
            totalTime * 60f
        );
    }

    // =========================================================
    // COLLISIONS
    // =========================================================

    private void registerCollisions() {

        /*
         * Curzon Hall - main body.
         * Rendered as a 22x7 box centered at (0, -13).
         */
        player.addCollision(
            -11f,
            -16.5f,
            22f,
            7f,
            6.0f
        );

        /*
         * Curzon Hall - entrance block.
         * Rendered as a 7x4 box centered at (0, -8.8), taller
         * than the main body, protruding toward the courtyard.
         */
        player.addCollision(
            -3.5f,
            -10.8f,
            7f,
            4f,
            7.5f
        );

        /*
         * TSC. The roof is a real jumpable platform.
         * Rendered as a 12x7 box centered at (18, -3).
         */
        player.addCollision(
            12f,
            -6.5f,
            12f,
            7f,
            TSC_ROOF_Y
        );

        /*
         * Library.
         * Rendered as a 12x7 box centered at (-18, -3).
         */
        player.addCollision(
            -24f,
            -6.5f,
            12f,
            7f,
            4.5f
        );

        /*
         * The four academic buildings (createAcademicBuildings()),
         * each a 10x6 box. These previously had NO collision
         * registered at all, which is why the player could walk
         * straight through them.
         */
        registerAcademicBuilding(-20f, 11f);
        registerAcademicBuilding(20f, 11f);
        registerAcademicBuilding(-26f, -13f);
        registerAcademicBuilding(26f, -13f);

        /*
         * Main gate pillars.
         * Rendered as 2.2x2.2 columns at (-10, 16) and (10, 16).
         */
        registerGatePillar(-10f, 16f);
        registerGatePillar(10f, 16f);
    }

    /**
     * Registers a collision box for one academic building, given
     * its center (matching the x/z passed to addBuilding() in
     * createAcademicBuildings()). The building itself is a 10x6
     * box with its roof topping out around y=4.3.
     */
    private void registerAcademicBuilding(
        float centerX,
        float centerZ
    ) {
        player.addCollision(
            centerX - 5f,
            centerZ - 3f,
            10f,
            6f,
            4.0f
        );
    }

    /**
     * Registers a small collision box for one gate pillar, given
     * its center (matching the x/z passed to createGatePillar()).
     */
    private void registerGatePillar(
        float centerX,
        float centerZ
    ) {
        player.addCollision(
            centerX - 1.1f,
            centerZ - 1.1f,
            2.2f,
            2.2f,
            7.0f
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(float delta) {

        /*
         * Avoid huge frame jumps.
         */
        delta =
            Math.min(
                delta,
                0.05f
            );

        totalTime += delta;

        if (screenshotToastTimer > 0f) {
            screenshotToastTimer -= delta;
        }

        handleMapInput();

        // -----------------------------------------------------
        // HISTORICAL CLUES PROXIMITY & ANIMATION
        // -----------------------------------------------------
        nearbyClue = null;
        if (player != null && historicalClues.size > 0) {
            float px = player.getX();
            float pz = player.getZ();
            float closestDist = 3.2f;

            for (HistoricalClue clue : historicalClues) {
                float dx = clue.position.x - px;
                float dz = clue.position.z - pz;
                float dist = (float) Math.sqrt(dx * dx + dz * dz);
                if (dist < closestDist) {
                    closestDist = dist;
                    nearbyClue = clue;
                }
            }
        }

        // Animate golden crests (gentle hover and slow rotation)
        for (int i = 0; i < clueMarkerInstances.size; i++) {
            ModelInstance mi = clueMarkerInstances.get(i);
            HistoricalClue clue = historicalClues.get(i);
            float bob = MathUtils.sin(totalTime * 3.5f + i) * 0.08f;
            mi.transform.setToTranslation(clue.position.x, 1.6f + bob, clue.position.z);
            mi.transform.rotate(Vector3.Y, (totalTime * 40f) % 360f);
        }

        // Handle Inspect Input
        if (activeInspectingClue != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                activeInspectingClue = null;
            }
        } else if (nearbyClue != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                activeInspectingClue = nearbyClue;
                nearbyClue.discovered = true;
            }
        }

        // Update player, camera, and missions only when not reading archive
        if (!levelCompleted && activeInspectingClue == null) {

            updateCamera(delta);

            updatePlayerModel();

            updateObjectiveMarker();

            updateMission(delta);
        }
    }

    // =========================================================
    // SCREENSHOT
    // =========================================================

    /**
     * Called by {@link bd.historicalgame.world.World} after a
     * screenshot has been written to disk, so the HUD can
     * flash a brief confirmation.
     */
    public void notifyScreenshotSaved() {
        screenshotToastTimer = 2.0f;
    }

    // =========================================================
    // MAP INPUT (TOGGLE / TAGGING)
    // =========================================================

    /**
     * M minimizes/expands the map, T drops a tag at the
     * player's current position, and BACKSPACE removes the
     * most recently placed tag. Available whenever the level
     * is active, including after completion, so the player can
     * still review the campus map.
     */
    private void handleMapInput() {

        if (player == null) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            mapExpanded = !mapExpanded;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {

            mapTags.add(
                new Vector3(
                    player.getX(),
                    player.getY(),
                    player.getZ()
                )
            );
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)
            && mapTags.size > 0) {

            mapTags.removeIndex(mapTags.size - 1);
        }
    }

    // =========================================================
    // MISSION UPDATE
    // =========================================================

    private void updateMission(float delta) {

        missionTimer += delta;

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            player.getX() - target.x;

        float dz =
            player.getZ() - target.z;

        float distance =
            (float)Math.sqrt(
                dx * dx +
                dz * dz
            );

        boolean insideObjectiveRadius =
            distance <= MISSION_DISTANCE;

        boolean tscRoofReached =
            missionIndex != 2
            || (
                player.isGrounded()
                &&
                player.getY() >=
                    TSC_PLAYER_CENTER_Y - TSC_ROOF_TOLERANCE
            );

        if (insideObjectiveRadius && tscRoofReached) {
            completeCurrentMission();
        }
    }

    private void completeCurrentMission() {

        missionTimer = 0f;

        if (
            missionIndex <
            missionTargets.length - 1
        ) {

            missionIndex++;

        } else {

            levelCompleted = true;
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    private void saveGameplayScreenshot() {
        try {
            FileHandle dir = com.badlogic.gdx.Gdx.files.local("screenshots");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "level1_" +
                    new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date()) +
                    ".png";

            int width = com.badlogic.gdx.Gdx.graphics.getBackBufferWidth();
            int height = com.badlogic.gdx.Gdx.graphics.getBackBufferHeight();

            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
            PixmapIO.writePNG(dir.child(filename), pixmap);
            pixmap.dispose();

            System.out.println("SCREENSHOT SAVED: " + dir.child(filename).path());
        } catch (Exception e) {
            System.err.println("SCREENSHOT FAILED: " + e.getMessage());
        }
    }

    public void render() {
        if (!SCREENSHOT_TRIGGERED_FOR_LEVEL1) {
            SCREENSHOT_TRIGGERED_FOR_LEVEL1 = true;
            saveGameplayScreenshot();
        }



        int width =
            Gdx.graphics.getWidth();

        int height =
            Gdx.graphics.getHeight();

        if (height <= 0) {
            height = 1;
        }

        Gdx.gl.glViewport(
            0,
            0,
            width,
            height
        );

        Gdx.gl.glEnable(
            GL20.GL_DEPTH_TEST
        );

        /*
         * Where Winds Meet atmospheric morning sky matching depth fog.
         */
        Gdx.gl.glClearColor(
            0.65f,
            0.76f,
            0.88f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
            GL20.GL_DEPTH_BUFFER_BIT
        );

        // -----------------------------------------------------
        // 3D
        // -----------------------------------------------------

        modelBatch.begin(camera);

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();

        // -----------------------------------------------------
        // HUD
        // -----------------------------------------------------

        renderHUD();
    }

    private void renderHUD() {

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float scale = Math.max(1f, h / 1080f);

        // -----------------------------------------------------
        // 1. TOP-LEFT MISSION PANEL (Where Winds Meet Glassmorphism)
        // -----------------------------------------------------
        float panelW = 560f * scale;
        float panelH = 148f * scale;
        float panelX = 28f * scale;
        float panelY = h - panelH - 28f * scale;

        hudShapes.setProjectionMatrix(spriteBatch.getProjectionMatrix());
        hudShapes.begin(ShapeRenderer.ShapeType.Filled);

        // Dark glass translucent backdrop
        hudShapes.setColor(0.035f, 0.055f, 0.085f, 0.86f);
        hudShapes.rect(panelX, panelY, panelW, panelH);

        // Gold left vertical accent stripe
        hudShapes.setColor(0.92f, 0.74f, 0.28f, 0.98f);
        hudShapes.rect(panelX, panelY, 5f * scale, panelH);

        // Subtle top gold hairline
        hudShapes.setColor(0.92f, 0.74f, 0.28f, 0.40f);
        hudShapes.rect(panelX, panelY + panelH - 2f * scale, panelW, 2f * scale);

        // -----------------------------------------------------
        // 2. BOTTOM-LEFT VITALITY & STAMINA GAUGES
        // -----------------------------------------------------
        float barX = 28f * scale;
        float barY = 24f * scale;
        float barW = 230f * scale;
        float barH = 9f * scale;

        // Vitality / HP Bar Background & Fill
        hudShapes.setColor(0.12f, 0.04f, 0.04f, 0.85f);
        hudShapes.rect(barX, barY + 30f * scale, barW, barH);
        hudShapes.setColor(0.88f, 0.26f, 0.22f, 0.95f);
        float hpRatio = player != null ? (player.getHealth() / player.getMaxHealth()) : 1f;
        hudShapes.rect(barX, barY + 30f * scale, barW * hpRatio, barH);

        // Stamina Bar Background & Fill
        hudShapes.setColor(0.04f, 0.10f, 0.08f, 0.85f);
        hudShapes.rect(barX, barY + 6f * scale, barW, barH);
        float stamRatio = player != null ? (player.getStamina() / player.getMaxStamina()) : 1f;
        boolean isSprinting = player != null && player.isSprinting();
        hudShapes.setColor(isSprinting ? 0.96f : 0.28f, 0.85f, 0.72f, 0.95f);
        hudShapes.rect(barX, barY + 6f * scale, barW * stamRatio, barH);

        // -----------------------------------------------------
        // 3. CENTER SCREEN PROMPT (When near historical clue)
        // -----------------------------------------------------
        if (nearbyClue != null && activeInspectingClue == null) {
            float promptW = 480f * scale;
            float promptH = 46f * scale;
            float promptX = w / 2f - promptW / 2f;
            float promptY = 160f * scale;

            hudShapes.setColor(0.03f, 0.05f, 0.08f, 0.92f);
            hudShapes.rect(promptX, promptY, promptW, promptH);
            hudShapes.setColor(0.92f, 0.74f, 0.28f, 0.95f);
            hudShapes.rect(promptX, promptY, promptW, 2f * scale);
            hudShapes.rect(promptX, promptY + promptH - 2f * scale, promptW, 2f * scale);
        }

        // -----------------------------------------------------
        // 4. CINEMATIC MODAL (When inspecting historical archive)
        // -----------------------------------------------------
        if (activeInspectingClue != null) {
            // Top and Bottom cinematic letterbox bars
            hudShapes.setColor(0f, 0f, 0f, 0.88f);
            hudShapes.rect(0f, 0f, w, h * 0.12f);
            hudShapes.rect(0f, h * 0.88f, w, h * 0.12f);

            // Modal card backdrop
            float modalW = Math.min(w * 0.80f, 860f * scale);
            float modalH = Math.min(h * 0.68f, 460f * scale);
            float modalX = w / 2f - modalW / 2f;
            float modalY = h / 2f - modalH / 2f;

            hudShapes.setColor(0.035f, 0.05f, 0.07f, 0.96f);
            hudShapes.rect(modalX, modalY, modalW, modalH);

            // Gold framing
            hudShapes.setColor(0.92f, 0.74f, 0.28f, 0.95f);
            hudShapes.rect(modalX, modalY, modalW, 3f * scale);
            hudShapes.rect(modalX, modalY + modalH - 3f * scale, modalW, 3f * scale);
            hudShapes.rect(modalX, modalY, 3f * scale, modalH);
            hudShapes.rect(modalX + modalW - 3f * scale, modalY, 3f * scale, modalH);
        }

        hudShapes.end();

        // -----------------------------------------------------
        // TEXT PASS (FreeType Vector Fonts)
        // -----------------------------------------------------
        spriteBatch.begin();

        if (screenshotToastTimer > 0f) {
            BitmapFont toastFont = hudFont(0.85f * scale);
            toastFont.setColor(1f, 1f, 1f, Math.min(1f, screenshotToastTimer));
            String toastText = "SCREENSHOT SAVED";
            GlyphLayout toastLayout = new GlyphLayout(toastFont, toastText);
            toastFont.draw(spriteBatch, toastText, w / 2f - toastLayout.width / 2f, h - 24f * scale);
            toastFont.setColor(Color.WHITE);
        }

        // Mission Badge
        BitmapFont tagFont = hudFont(0.72f * scale);
        tagFont.setColor(0.92f, 0.74f, 0.28f, 1f);
        tagFont.draw(
            spriteBatch,
            "◆ ACTIVE MISSION  0" + (missionIndex + 1) + " / 05  •  DHAKA UNIVERSITY",
            panelX + 20f * scale,
            panelY + panelH - 18f * scale
        );

        // Mission Title
        BitmapFont titleFont = hudFont(1.22f * scale);
        titleFont.setColor(Color.WHITE);
        String title = levelCompleted ? "LEVEL 1 COMPLETE" : missionTitles[missionIndex];
        titleFont.draw(spriteBatch, title, panelX + 20f * scale, panelY + panelH - 46f * scale);

        // Mission Description
        BitmapFont descFont = hudFont(0.84f * scale);
        descFont.setColor(0.86f, 0.84f, 0.80f, 1f);

        if (!levelCompleted) {
            descFont.draw(spriteBatch, missionDescriptions[missionIndex], panelX + 20f * scale, panelY + panelH - 78f * scale);

            Vector3 target = missionTargets[missionIndex];
            float dx = target.x - player.getX();
            float dz = target.z - player.getZ();
            float distance = (float) Math.sqrt(dx * dx + dz * dz);

            BitmapFont distFont = hudFont(0.90f * scale);
            distFont.setColor(distance < 8f ? Color.valueOf("75F09E") : Color.valueOf("EFC868"));
            distFont.draw(
                spriteBatch,
                String.format("OBJECTIVE DISTANCE:  %.1f M   [ %s ]", distance, getDirection(dx, dz)),
                panelX + 20f * scale,
                panelY + panelH - 110f * scale
            );

            if (missionIndex == 2) {
                BitmapFont rooftopFont = hudFont(0.74f * scale);
                rooftopFont.setColor(Color.valueOf("6FE7E0"));
                rooftopFont.draw(
                    spriteBatch,
                    "ROOFTOP OBJECTIVE  •  SPACE TO JUMP",
                    panelX + 20f * scale,
                    panelY + panelH - 132f * scale
                );
            }
        } else {
            descFont.draw(spriteBatch, "Campus exploration completed. Press ESC for pause menu.", panelX + 20f * scale, panelY + panelH - 85f * scale);
        }

        // Vitality / Stamina Labels
        BitmapFont gaugeFont = hudFont(0.68f * scale);
        gaugeFont.setColor(0.88f, 0.88f, 0.88f, 1f);
        gaugeFont.draw(spriteBatch, "VITALITY", barX, barY + 52f * scale);
        gaugeFont.draw(spriteBatch, isSprinting ? "STAMINA  [SPRINTING]" : "STAMINA", barX, barY + 28f * scale);

        // Keycap Controls (Bottom Right)
        BitmapFont controlsFont = hudFont(0.70f * scale);
        controlsFont.setColor(0.82f, 0.80f, 0.74f, 1f);
        String controlsText = "[W][A][S][D] MOVE    [SHIFT] SPRINT    [SPACE] JUMP    [E] INTERACT    [M] MAP    [ESC] PAUSE";
        GlyphLayout controlsLayout = new GlyphLayout(controlsFont, controlsText);
        controlsFont.draw(spriteBatch, controlsText, w - controlsLayout.width - 28f * scale, 34f * scale);

        // Center Clue Prompt
        if (nearbyClue != null && activeInspectingClue == null) {
            BitmapFont promptFont = hudFont(0.88f * scale);
            promptFont.setColor(0.96f, 0.88f, 0.45f, 1f);
            String promptText = "◆ [E] READ ARCHIVE: " + nearbyClue.title;
            GlyphLayout pLayout = new GlyphLayout(promptFont, promptText);
            promptFont.draw(spriteBatch, promptText, w / 2f - pLayout.width / 2f, 190f * scale);
        }

        // Modal Content
        if (activeInspectingClue != null) {
            float modalW = Math.min(w * 0.80f, 860f * scale);
            float modalH = Math.min(h * 0.68f, 460f * scale);
            float modalX = w / 2f - modalW / 2f;
            float modalY = h / 2f - modalH / 2f;

            BitmapFont mTag = hudFont(0.75f * scale);
            mTag.setColor(0.92f, 0.74f, 0.28f, 1f);
            mTag.draw(spriteBatch, "◆ HISTORICAL ARCHIVE — 36 JULY STUDENT MOVEMENT", modalX + 36f * scale, modalY + modalH - 32f * scale);

            BitmapFont mHeader = hudFont(1.30f * scale);
            mHeader.setColor(Color.WHITE);
            mHeader.draw(spriteBatch, activeInspectingClue.title, modalX + 36f * scale, modalY + modalH - 68f * scale);

            BitmapFont mSub = hudFont(0.82f * scale);
            mSub.setColor(0.60f, 0.85f, 0.85f, 1f);
            mSub.draw(spriteBatch, activeInspectingClue.date + "  •  " + activeInspectingClue.location, modalX + 36f * scale, modalY + modalH - 104f * scale);

            BitmapFont mBody = hudFont(0.90f * scale);
            mBody.setColor(0.92f, 0.90f, 0.85f, 1f);
            mBody.draw(
                spriteBatch,
                activeInspectingClue.text,
                modalX + 36f * scale,
                modalY + modalH - 145f * scale,
                modalW - 72f * scale,
                com.badlogic.gdx.utils.Align.left,
                true
            );

            BitmapFont mClose = hudFont(0.80f * scale);
            mClose.setColor(0.92f, 0.74f, 0.28f, 1f);
            String closeText = "PRESS  [E]  OR  [SPACE]  TO RESUME EXPLORATION";
            GlyphLayout clLayout = new GlyphLayout(mClose, closeText);
            mClose.draw(spriteBatch, closeText, w / 2f - clLayout.width / 2f, modalY + 36f * scale);
        }

        if (levelCompleted) {
            BitmapFont completeFont = hudFont(2.4f * scale);
            completeFont.setColor(Color.WHITE);
            String complete = "LEVEL 1 COMPLETE";
            GlyphLayout completeLayout = new GlyphLayout(completeFont, complete);
            completeFont.draw(spriteBatch, complete, w / 2f - completeLayout.width / 2f, h / 2f + 30f * scale);

            BitmapFont completeBodyFont = hudFont(1.0f * scale);
            completeBodyFont.setColor(Color.WHITE);
            GlyphLayout completeBodyLayout = new GlyphLayout(completeBodyFont, "Campus exploration completed.");
            completeBodyFont.draw(
                spriteBatch,
                "Campus exploration completed.",
                w / 2f - completeBodyLayout.width / 2f,
                h / 2f - 20f * scale
            );
        }

        spriteBatch.end();

        /*
         * Self-contained: manages its own ShapeRenderer and
         * SpriteBatch begin/end pairs, so it must run after
         * the main HUD batch above has been closed.
         */
        renderMiniMap();
    }

    private String getCameraRelativeArrow(Vector3 target) {
        float dx = target.x - player.getX();
        float dz = target.z - player.getZ();
        if (Math.abs(dx) < 1.5f && Math.abs(dz) < 1.5f) return "◆";

        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        Vector3 forward = new Vector3(-MathUtils.sin(yawRad), 0f, -MathUtils.cos(yawRad)).nor();
        Vector3 right = new Vector3(-forward.z, 0f, forward.x).nor();
        Vector3 toTarget = new Vector3(dx, 0f, dz).nor();

        float forwardDot = forward.dot(toTarget);
        float rightDot = right.dot(toTarget);
        if (forwardDot > 0.70f) return "↑";
        if (forwardDot < -0.70f) return "↓";
        if (rightDot > 0f) return "→";
        return "←";
    }

    // =========================================================
    // DIRECTION
    // =========================================================

    private String getDirection(
        float dx,
        float dz
    ) {

        if (
            Math.abs(dx) <
                1.5f &&
            Math.abs(dz) <
                1.5f
        ) {

            return "HERE";
        }

        if (
            Math.abs(dx) >
            Math.abs(dz)
        ) {

            return dx > 0
                ? "EAST  →"
                : "WEST  ←";
        }

        return dz > 0
            ? "SOUTH  ↓"
            : "NORTH  ↑";
    }

    // =========================================================
    // MINI MAP (RADAR) / EXPANDABLE MAP
    // =========================================================

    private static final float MINIMAP_RADIUS = 76f;
    private static final float MINIMAP_RANGE = 32f;

    /**
     * Range shown once the map is expanded (M). Wider than the
     * corner radar so most of the campus fits on screen at once.
     */
    private static final float MAP_EXPANDED_RANGE = 95f;

    /**
     * Chooses between the small corner radar and the large
     * centered map, then delegates to the shared renderer.
     */
    private void renderMiniMap() {

        renderMap(mapExpanded);
    }

    /**
     * Circular radar/map. Rotates with the camera so the
     * player's current facing direction always points to the
     * top of the disc (same convention as the on-screen
     * directional arrow), with a dot per mission target, a
     * highlighted dot for the current objective, and a marker
     * per player-placed location tag.
     *
     * In its default (minimized) state this draws as a small
     * disc tucked into the top-right corner. Pressing M expands
     * it into a large, centered map with room for tag labels;
     * pressing M again shrinks it back down to the corner.
     */
    private void renderMap(boolean expanded) {

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float scale = Math.max(1f, h / 1080f);

        float radius =
            expanded
                ? Math.min(w, h) * 0.36f
                : MINIMAP_RADIUS * scale;

        float range =
            expanded
                ? MAP_EXPANDED_RANGE
                : MINIMAP_RANGE;

        float centerX;
        float centerY;

        if (expanded) {

            centerX = w / 2f;
            centerY = h / 2f;

        } else {

            float margin = 26f * scale;

            centerX = w - margin - radius;
            centerY = h - margin - radius;
        }

        /*
         * Same forward/right convention as
         * getCameraRelativeArrow(), so the map and the
         * on-screen compass arrow always agree.
         */
        float yawRad =
            cameraYaw * MathUtils.degreesToRadians;

        float forwardX = -MathUtils.sin(yawRad);
        float forwardZ = -MathUtils.cos(yawRad);

        float rightX = -forwardZ;
        float rightZ = forwardX;

        hudShapes.setProjectionMatrix(
            spriteBatch.getProjectionMatrix()
        );

        hudShapes.begin(ShapeRenderer.ShapeType.Filled);

        /*
         * Dim the whole screen behind the expanded map so the
         * 3D scene doesn't compete for attention with the map.
         */
        if (expanded) {

            hudShapes.setColor(0f, 0f, 0f, 0.55f);
            hudShapes.rect(0f, 0f, w, h);
        }

        /*
         * Ring border: a slightly larger gold disc behind a
         * smaller dark disc, leaving a thin gold rim visible.
         */
        hudShapes.setColor(0.90f, 0.72f, 0.25f, 0.55f);
        hudShapes.circle(centerX, centerY, radius + 3f * scale, 64);

        hudShapes.setColor(0.03f, 0.045f, 0.055f, 0.90f);
        hudShapes.circle(centerX, centerY, radius, 64);

        /*
         * Mission markers.
         */
        for (int i = 0; i < missionTargets.length; i++) {

            Vector3 target = missionTargets[i];

            float dx = target.x - player.getX();
            float dz = target.z - player.getZ();

            float worldDist =
                (float) Math.sqrt(dx * dx + dz * dz);

            float mapX;
            float mapY;

            if (worldDist < 0.001f) {

                mapX = 0f;
                mapY = 0f;

            } else {

                float clampedDist =
                    Math.min(worldDist, range);

                float mapDist =
                    (clampedDist / range)
                        * (radius - 8f * scale);

                float mapRight =
                    (dx * rightX + dz * rightZ) / worldDist;

                float mapUp =
                    (dx * forwardX + dz * forwardZ) / worldDist;

                mapX = mapRight * mapDist;
                mapY = mapUp * mapDist;
            }

            boolean isCurrent =
                i == missionIndex && !levelCompleted;

            boolean isPast =
                i < missionIndex;

            if (isCurrent) {
                hudShapes.setColor(0.90f, 0.72f, 0.25f, 1f);
            } else if (isPast) {
                hudShapes.setColor(0.6f, 0.6f, 0.6f, 0.55f);
            } else {
                hudShapes.setColor(0.6f, 0.6f, 0.6f, 0.35f);
            }

            hudShapes.circle(
                centerX + mapX,
                centerY + mapY,
                (isCurrent ? 5f : 3.5f) * scale,
                16
            );
        }

        /*
         * Player-placed location tags. Screen positions are
         * cached so the label pass below (a separate
         * spriteBatch block) doesn't need to redo the
         * world-to-map projection.
         */
        float[] tagScreenX = new float[mapTags.size];
        float[] tagScreenY = new float[mapTags.size];

        for (int i = 0; i < mapTags.size; i++) {

            Vector3 tag = mapTags.get(i);

            float dx = tag.x - player.getX();
            float dz = tag.z - player.getZ();

            float worldDist =
                (float) Math.sqrt(dx * dx + dz * dz);

            float mapX;
            float mapY;

            if (worldDist < 0.001f) {

                mapX = 0f;
                mapY = 0f;

            } else {

                float clampedDist =
                    Math.min(worldDist, range);

                float mapDist =
                    (clampedDist / range)
                        * (radius - 8f * scale);

                float mapRight =
                    (dx * rightX + dz * rightZ) / worldDist;

                float mapUp =
                    (dx * forwardX + dz * forwardZ) / worldDist;

                mapX = mapRight * mapDist;
                mapY = mapUp * mapDist;
            }

            tagScreenX[i] = centerX + mapX;
            tagScreenY[i] = centerY + mapY;

            hudShapes.setColor(0.36f, 0.85f, 0.90f, 1f);

            float tagSize = (expanded ? 5.5f : 3.5f) * scale;

            hudShapes.rect(
                tagScreenX[i] - tagSize / 2f,
                tagScreenY[i] - tagSize / 2f,
                tagSize,
                tagSize
            );
        }

        /*
         * Player marker, fixed at the map's center. Since the
         * map rotates with the camera, this always points up.
         */
        hudShapes.setColor(0.94f, 0.96f, 0.98f, 1f);

        float triSize = (expanded ? 9f : 7f) * scale;

        hudShapes.triangle(
            centerX, centerY + triSize,
            centerX - triSize * 0.65f, centerY - triSize * 0.55f,
            centerX + triSize * 0.65f, centerY - triSize * 0.55f
        );

        hudShapes.end();

        /*
         * Crisp thin ring outline over the filled discs.
         */
        hudShapes.begin(ShapeRenderer.ShapeType.Line);
        hudShapes.setColor(0.90f, 0.72f, 0.25f, 0.9f);
        hudShapes.circle(centerX, centerY, radius, 64);
        hudShapes.end();

        String label =
            levelCompleted
                ? "CAMPUS MAP"
                : missionTitles[
                    Math.min(missionIndex, missionTitles.length - 1)
                ];

        BitmapFont labelFont =
            hudFont((expanded ? 0.9f : 0.62f) * scale);

        labelFont.setColor(Color.valueOf("E8BC55"));

        GlyphLayout labelLayout =
            new GlyphLayout(labelFont, label);

        spriteBatch.begin();

        // 360-degree rotating Cardinal markers on compass rim
        BitmapFont cardFont = hudFont((expanded ? 0.70f : 0.54f) * scale);
        float cardInset = radius - 12f * scale;

        // North (pointing in direction (0, -1) in world space)
        float nX = centerX + (0f * rightX - 1f * rightZ) * cardInset;
        float nY = centerY + (0f * forwardX - 1f * forwardZ) * cardInset;
        cardFont.setColor(Color.valueOf("F5C542"));
        cardFont.draw(spriteBatch, "N", nX - 4f * scale, nY + 5f * scale);

        // South
        float sX = centerX + (0f * rightX + 1f * rightZ) * cardInset;
        float sY = centerY + (0f * forwardX + 1f * forwardZ) * cardInset;
        cardFont.setColor(Color.valueOf("B4ACA0"));
        cardFont.draw(spriteBatch, "S", sX - 4f * scale, sY + 5f * scale);

        // East
        float eX = centerX + (1f * rightX + 0f * rightZ) * cardInset;
        float eY = centerY + (1f * forwardX + 0f * forwardZ) * cardInset;
        cardFont.setColor(Color.valueOf("B4ACA0"));
        cardFont.draw(spriteBatch, "E", eX - 4f * scale, eY + 5f * scale);

        // West
        float wX = centerX + (-1f * rightX + 0f * rightZ) * cardInset;
        float wY = centerY + (-1f * forwardX + 0f * forwardZ) * cardInset;
        cardFont.setColor(Color.valueOf("B4ACA0"));
        cardFont.draw(spriteBatch, "W", wX - 5f * scale, wY + 5f * scale);

        labelFont.draw(
            spriteBatch,
            label,
            centerX - labelLayout.width / 2f,
            centerY - radius - 10f * scale
        );

        /*
         * Numbered labels above each tag marker. Only drawn
         * when expanded; the minimized radar is too small for
         * legible text next to every dot.
         */
        if (expanded) {

            BitmapFont tagFont = hudFont(0.55f * scale);
            tagFont.setColor(Color.valueOf("5DD9E0"));

            for (int i = 0; i < mapTags.size; i++) {

                String tagLabel = "TAG " + (i + 1);

                GlyphLayout tagLayout =
                    new GlyphLayout(tagFont, tagLabel);

                tagFont.draw(
                    spriteBatch,
                    tagLabel,
                    tagScreenX[i] - tagLayout.width / 2f,
                    tagScreenY[i] + 14f * scale
                );
            }
        }

        /*
         * Control hint: a short reminder under the corner radar,
         * a fuller one under the expanded map.
         */
        BitmapFont hintFont =
            hudFont((expanded ? 0.62f : 0.5f) * scale);

        hintFont.setColor(1f, 1f, 1f, expanded ? 0.85f : 0.55f);

        String hint =
            expanded
                ? "T  TAG LOCATION     BACKSPACE  REMOVE LAST TAG     M  MINIMIZE MAP"
                : "M  MAP";

        GlyphLayout hintLayout =
            new GlyphLayout(hintFont, hint);

        float hintY =
            expanded
                ? centerY - radius - 28f * scale
                : centerY - radius - 22f * scale;

        hintFont.draw(
            spriteBatch,
            hint,
            centerX - hintLayout.width / 2f,
            hintY
        );

        spriteBatch.end();
    }

    // =========================================================
    // RESIZE
    // =========================================================

    /**
     * Keeps the 3D camera's aspect ratio matched to the actual
     * window size whenever the player resizes the game window
     * or toggles fullscreen. The HUD needs no equivalent call:
     * it already reads {@code Gdx.graphics.getWidth()/getHeight()}
     * fresh every frame.
     */
    public void resize(int width, int height) {

        if (camera == null) {
            return;
        }

        camera.viewportWidth = Math.max(1, width);
        camera.viewportHeight = Math.max(1, height);

        camera.update();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public PerspectiveCamera getCamera() {

        return camera;
    }

    public Player getPlayer() {

        return player;
    }

    public boolean isLevelCompleted() {

        return levelCompleted;
    }

    public int getMissionIndex() {

        return missionIndex;
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        if (modelBatch != null) {
            modelBatch.dispose();
        }

        if (spriteBatch != null) {
            spriteBatch.dispose();
        }

        if (fontManager != null) {
            fontManager.dispose();
        }

        if (hudShapes != null) {
            hudShapes.dispose();
        }

        for (Model model : models) {

            if (model != null) {
                model.dispose();
            }
        }

        models.clear();

        instances.clear();

        player = null;
        playerBody = null;
        playerHead = null;
        objectiveMarker = null;
    }

    // COMMIT_H_CAMERA_ZOOM
    // Mouse wheel + macOS trackpad scroll/pinch-compatible zoom.
    private void handleCameraZoom(float amount) {
        cameraZoomDistance += amount * CAMERA_ZOOM_STEP;

        if (cameraZoomDistance < CAMERA_ZOOM_MIN) {
            cameraZoomDistance = CAMERA_ZOOM_MIN;
        }

        if (cameraZoomDistance > CAMERA_ZOOM_MAX) {
            cameraZoomDistance = CAMERA_ZOOM_MAX;
        }

        updateCameraZoomDistance();
    }

    private void updateCameraZoomDistance() {
        // Update the camera position using the current zoom distance.
        // Existing camera target/rotation remains unchanged.
        if (camera != null && player != null) {
            Vector3 direction = new Vector3(
                    camera.position.x - player.getPosition().x,
                    0f,
                    camera.position.z - player.getPosition().z
            );

            if (direction.len2() > 0.001f) {
                direction.nor();

                camera.position.x =
                        player.getPosition().x +
                        direction.x * cameraZoomDistance;

                camera.position.z =
                        player.getPosition().z +
                        direction.z * cameraZoomDistance;

                camera.lookAt(
                        player.getPosition().x,
                        player.getPosition().y + 1.0f,
                        player.getPosition().z
                );

                camera.up.set(Vector3.Y);
                camera.update();
            }
        }
    }

}