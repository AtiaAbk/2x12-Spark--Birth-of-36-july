package bd.spark36;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentMesh;
import bd.spark36.hud.FontRenderer;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.JulyMemorials;

/**
 * Main game class for 2x12: Spark — Birth of 36 July.
 * Coordinates 3D Dhaka University campus exploration, over-the-shoulder cinematic camera,
 * July 2024 student protagonist, historical archives, and Where Winds Meet style HUD.
 */
public class SparkGame extends ApplicationAdapter {

    // 3D Rendering Subsystems
    private ModelBatch modelBatch;
    private DhakaCampusWorld world;
    private JulyMemorials memorials;
    private StudentMesh studentMesh;
    private PlayerController player;
    private CinematicCamera camera;

    // HUD and UI Subsystems
    private FontRenderer fontRenderer;
    private WhereWindsMeetHUD hud;

    // Sky Renderer for Dawn Gradient
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer skyRenderer;
    private final Color horizonColor = new Color(0.92f, 0.84f, 0.72f, 1f); // Warm dawn golden haze
    private final Color zenithColor = new Color(0.38f, 0.60f, 0.86f, 1f);  // Morning cerulean blue

    @Override
    public void create() {
        // Initialize 3D Batch & Sky Renderer
        modelBatch = new ModelBatch();
        skyRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        // Initialize Campus World & Historical Memorials
        world = new DhakaCampusWorld();
        memorials = new JulyMemorials();

        // Initialize Student Character & Controller
        studentMesh = new StudentMesh();
        player = new PlayerController();

        // Initialize Over-the-shoulder Cinematic Camera
        camera = new CinematicCamera(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        // Initialize Crisp FreeType Fonts & Glassmorphic HUD
        fontRenderer = new FontRenderer();
        hud = new WhereWindsMeetHUD(fontRenderer);

        // Catch cursor for seamless PC mouse-look
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        handleGlobalInputs(delta);

        // 1. Update Game Logic
        boolean canMove = !hud.isModalOpen();
        player.update(delta, camera.getYaw(), canMove);
        camera.update(delta, player.getPosition(), canMove);
        memorials.update(delta);
        hud.update(delta, player, memorials);

        // 2. Render Atmospheric Dawn Sky Gradient (Where Winds Meet Golden Morning)
        Gdx.gl.glViewport(0, 0, screenW, screenH);
        skyRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);
        skyRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        skyRenderer.rect(0, 0, screenW, screenH, horizonColor, horizonColor, zenithColor, zenithColor);
        skyRenderer.end();

        // 3. Clear Depth Buffer for 3D Scene
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        // 4. Render 3D World Pass
        camera.resize(screenW, screenH);
        modelBatch.begin(camera.getCamera());
        world.render(modelBatch);
        memorials.render(modelBatch, world.getEnvironment());
        studentMesh.render(
            modelBatch,
            world.getEnvironment(),
            player.getPosition(),
            player.getHeadingDegrees(),
            player.getWalkCycle(),
            player.isMoving(),
            player.isSprinting()
        );
        modelBatch.end();

        // 5. Render 2D Where Winds Meet HUD Pass
        hud.render(player, memorials, camera.getYaw());
    }

    private void handleGlobalInputs(float delta) {
        // Toggle fullscreen with F11
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1440, 900);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        // Toggle cursor capture on mouse click
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (!hud.isModalOpen() && !Gdx.input.isCursorCatched()) {
                Gdx.input.setCursorCatched(true);
            }
        }

        // Release cursor when modal or map is active
        if (hud.isModalOpen() && Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(false);
        }

        // F12 Screenshot key
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            takeScreenshot("spark36_" + System.currentTimeMillis());
        }

        // Automated visual verification support
        if (System.getProperty("bd.spark36.testScreenshot") != null) {
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        }
    }

    private float testTimer = 0f;
    private boolean autoScreenshotTaken = false;

    public static void takeScreenshot(String name) {
        try {
            int w = Gdx.graphics.getBackBufferWidth();
            int h = Gdx.graphics.getBackBufferHeight();
            com.badlogic.gdx.graphics.Pixmap pixmap = com.badlogic.gdx.graphics.Pixmap.createFromFrameBuffer(0, 0, w, h);
            com.badlogic.gdx.graphics.Pixmap flipped = new com.badlogic.gdx.graphics.Pixmap(w, h, pixmap.getFormat());
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    flipped.drawPixel(x, y, pixmap.getPixel(x, h - y - 1));
                }
            }
            com.badlogic.gdx.files.FileHandle file = Gdx.files.local("screenshots/" + name + ".png");
            if (!file.parent().exists()) {
                file = Gdx.files.local("assets/screenshots/" + name + ".png");
            }
            com.badlogic.gdx.graphics.PixmapIO.writePNG(file, flipped);
            pixmap.dispose();
            flipped.dispose();
            Gdx.app.log("SparkGame", "Screenshot saved: " + file.path());
        } catch (Exception e) {
            Gdx.app.error("SparkGame", "Failed to take screenshot", e);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        camera.resize(width, height);
    }

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (world != null) world.dispose();
        if (memorials != null) memorials.dispose();
        if (studentMesh != null) studentMesh.dispose();
        if (hud != null) hud.dispose();
        if (fontRenderer != null) fontRenderer.dispose();
        if (skyRenderer != null) skyRenderer.dispose();
    }
}
