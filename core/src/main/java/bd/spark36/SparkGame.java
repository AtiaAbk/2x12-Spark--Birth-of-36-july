package bd.spark36;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentMesh;
import bd.spark36.hud.FontRenderer;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.AtmosphereRenderer;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.JulyMemorials;
import bd.spark36.world.TextureFactory;

/**
 * Main game class for 2x12: Spark — Birth of 36 July.
 * Manages game states (Main Menu / Playing), coordinates 3D Dhaka University campus
 * exploration, over-the-shoulder cinematic camera, July 2024 student protagonist,
 * historical archives, and Where Winds Meet style HUD.
 */
public class SparkGame extends ApplicationAdapter {

    // ==========================================
    // GAME STATE MACHINE
    // ==========================================
    private enum GameState { MAIN_MENU, PLAYING }
    private GameState gameState = GameState.MAIN_MENU;

    // Main Menu State
    private int menuSelection = 0; // 0 = PLAY, 1 = EXIT
    private float menuAnimTime = 0f;

    // Shared Renderers (initialized in create — lightweight, used by both states)
    private ShapeRenderer shapeRenderer;
    private SpriteBatch menuBatch;
    private FontRenderer fontRenderer;
    private GlyphLayout glyphLayout;

    // Menu Color Palette (Where Winds Meet aesthetic)
    private final Color menuGlassBg = new Color(0.05f, 0.07f, 0.10f, 0.88f);
    private final Color menuGoldAccent = new Color(1.0f, 0.85f, 0.40f, 1f);
    private final Color menuGoldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color menuGoldMuted = new Color(0.68f, 0.58f, 0.32f, 0.70f);
    private final Color menuSelectedBg = new Color(0.12f, 0.11f, 0.08f, 0.92f);

    // ==========================================
    // 3D GAMEPLAY SUBSYSTEMS (lazy init on first play)
    // ==========================================
    private ModelBatch modelBatch;
    private DhakaCampusWorld world;
    private JulyMemorials memorials;
    private StudentMesh studentMesh;
    private PlayerController player;
    private CinematicCamera camera;
    private TextureFactory textures;
    private AtmosphereRenderer atmosphere;
    private WhereWindsMeetHUD hud;
    private boolean gameplayInitialized = false;

    // Auto-screenshot support
    private float testTimer = 0f;
    private boolean autoScreenshotTaken = false;

    @Override
    public void create() {
        // Initialize lightweight shared renderers for both menu and gameplay
        shapeRenderer = new ShapeRenderer();
        menuBatch = new SpriteBatch();
        fontRenderer = new FontRenderer();
        glyphLayout = new GlyphLayout();
        gameState = GameState.MAIN_MENU;
    }

    /**
     * Lazily initializes all 3D gameplay subsystems on first play.
     * Keeps them alive for re-entry from pause menu.
     */
    private void initGameplay() {
        if (gameplayInitialized) return;

        modelBatch = new ModelBatch();
        textures = new TextureFactory();
        world = new DhakaCampusWorld(textures);
        memorials = new JulyMemorials();
        studentMesh = new StudentMesh(textures);
        player = new PlayerController();
        camera = new CinematicCamera(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        atmosphere = new AtmosphereRenderer(fontRenderer);
        hud = new WhereWindsMeetHUD(fontRenderer);

        gameplayInitialized = true;
    }

    /** Transitions from Main Menu to Gameplay */
    public void startGameplay() {
        initGameplay();
        hud.reset();
        gameState = GameState.PLAYING;
        Gdx.input.setCursorCatched(true);
    }

    /** Transitions from Gameplay back to Main Menu */
    public void returnToMainMenu() {
        gameState = GameState.MAIN_MENU;
        menuSelection = 0;
        Gdx.input.setCursorCatched(false);
    }

    // ==========================================
    // MAIN RENDER DISPATCH
    // ==========================================
    @Override
    public void render() {
        if (gameState == GameState.MAIN_MENU) {
            renderMainMenu();
        } else {
            renderGameplay();
        }
    }

    // ==========================================
    // MAIN MENU SCREEN (Where Winds Meet aesthetic)
    // ==========================================
    private void renderMainMenu() {
        float delta = Gdx.graphics.getDeltaTime();
        if (delta > 0.1f) delta = 0.1f;
        menuAnimTime += delta;

        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        // Automated testing support: auto start gameplay if system property set
        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoStartGame"))) {
            startGameplay();
            return;
        }

        // Handle menu navigation (WASD + Arrow Keys + ENTER/SPACE)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            menuSelection = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            menuSelection = 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (menuSelection == 0) {
                startGameplay();
                return;
            } else {
                Gdx.app.exit();
                return;
            }
        }

        // Virtual coordinates scaling (1600 baseline)
        float w = 1600f;
        float h = 1600f * ((float) screenH / (float) screenW);

        // Clear screen with deep dark atmospheric color
        Gdx.gl.glViewport(0, 0, screenW, screenH);
        Gdx.gl.glClearColor(0.02f, 0.04f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        menuBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        // --- Background: Atmospheric dark gradient ---
        shapeRenderer.begin(ShapeType.Filled);
        Color bottomL = new Color(0.02f, 0.03f, 0.06f, 1f);
        Color bottomR = new Color(0.03f, 0.04f, 0.07f, 1f);
        Color topR = new Color(0.08f, 0.06f, 0.12f, 1f);
        Color topL = new Color(0.06f, 0.05f, 0.10f, 1f);
        shapeRenderer.rect(0, 0, w, h, bottomL, bottomR, topR, topL);

        // Subtle atmospheric mist band at lower third
        shapeRenderer.setColor(0.10f, 0.08f, 0.06f, 0.12f);
        shapeRenderer.rect(0, h * 0.15f, w, h * 0.15f);

        // Subtle warm horizon glow
        float glowPulse = 0.04f + 0.015f * MathUtils.sin(menuAnimTime * 0.8f);
        shapeRenderer.setColor(0.25f, 0.18f, 0.08f, glowPulse);
        shapeRenderer.rect(0, h * 0.35f, w, h * 0.12f);
        shapeRenderer.end();

        // --- Decorative Gold Elements ---
        shapeRenderer.begin(ShapeType.Filled);
        float centerX = w / 2f;
        float titleBlockY = h * 0.62f;

        // Gold diamond ornament above title
        shapeRenderer.setColor(menuGoldAccent);
        float diamondSize = 8f;
        shapeRenderer.triangle(
            centerX, titleBlockY + 70f + diamondSize,
            centerX - diamondSize, titleBlockY + 70f,
            centerX + diamondSize, titleBlockY + 70f
        );
        shapeRenderer.triangle(
            centerX, titleBlockY + 70f - diamondSize,
            centerX - diamondSize, titleBlockY + 70f,
            centerX + diamondSize, titleBlockY + 70f
        );

        // Horizontal gold line below diamond
        shapeRenderer.setColor(menuGoldBorder);
        shapeRenderer.rect(centerX - 120f, titleBlockY + 55f, 240f, 1.5f);

        // --- Menu Buttons ---
        float btnW = 340f;
        float btnH = 52f;
        float btnX = centerX - btnW / 2f;
        float playBtnY = h * 0.38f;
        float exitBtnY = playBtnY - btnH - 18f;

        // PLAY button background
        if (menuSelection == 0) {
            shapeRenderer.setColor(menuSelectedBg);
            shapeRenderer.rect(btnX - 4f, playBtnY - 4f, btnW + 8f, btnH + 8f);
            shapeRenderer.setColor(menuGoldAccent.r, menuGoldAccent.g, menuGoldAccent.b, 0.06f);
            shapeRenderer.rect(btnX, playBtnY, btnW, btnH);
        } else {
            shapeRenderer.setColor(menuGlassBg);
            shapeRenderer.rect(btnX, playBtnY, btnW, btnH);
        }

        // EXIT button background
        if (menuSelection == 1) {
            shapeRenderer.setColor(menuSelectedBg);
            shapeRenderer.rect(btnX - 4f, exitBtnY - 4f, btnW + 8f, btnH + 8f);
            shapeRenderer.setColor(menuGoldAccent.r, menuGoldAccent.g, menuGoldAccent.b, 0.06f);
            shapeRenderer.rect(btnX, exitBtnY, btnW, btnH);
        } else {
            shapeRenderer.setColor(menuGlassBg);
            shapeRenderer.rect(btnX, exitBtnY, btnW, btnH);
        }
        shapeRenderer.end();

        // --- Button Borders ---
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(menuSelection == 0 ? menuGoldAccent : menuGoldBorder);
        shapeRenderer.rect(btnX, playBtnY, btnW, btnH);

        shapeRenderer.setColor(menuSelection == 1 ? menuGoldAccent : menuGoldBorder);
        shapeRenderer.rect(btnX, exitBtnY, btnW, btnH);

        // Decorative corner brackets on selected button
        float selY = menuSelection == 0 ? playBtnY : exitBtnY;
        shapeRenderer.setColor(menuGoldAccent);
        float cbLen = 14f;
        // Top-left corner bracket
        shapeRenderer.line(btnX - 6f, selY + btnH + 6f, btnX - 6f, selY + btnH + 6f - cbLen);
        shapeRenderer.line(btnX - 6f, selY + btnH + 6f, btnX - 6f + cbLen, selY + btnH + 6f);
        // Top-right corner bracket
        shapeRenderer.line(btnX + btnW + 6f, selY + btnH + 6f, btnX + btnW + 6f, selY + btnH + 6f - cbLen);
        shapeRenderer.line(btnX + btnW + 6f, selY + btnH + 6f, btnX + btnW + 6f - cbLen, selY + btnH + 6f);
        // Bottom-left corner bracket
        shapeRenderer.line(btnX - 6f, selY - 6f, btnX - 6f, selY - 6f + cbLen);
        shapeRenderer.line(btnX - 6f, selY - 6f, btnX - 6f + cbLen, selY - 6f);
        // Bottom-right corner bracket
        shapeRenderer.line(btnX + btnW + 6f, selY - 6f, btnX + btnW + 6f, selY - 6f + cbLen);
        shapeRenderer.line(btnX + btnW + 6f, selY - 6f, btnX + btnW + 6f - cbLen, selY - 6f);
        shapeRenderer.end();

        // --- Text Typography ---
        menuBatch.begin();

        // Title: "2x12: SPARK"
        fontRenderer.titleFont.setColor(menuGoldAccent);
        glyphLayout.setText(fontRenderer.titleFont, "2x12: SPARK");
        fontRenderer.titleFont.draw(menuBatch, "2x12: SPARK",
            centerX - glyphLayout.width / 2f, titleBlockY + 45f);

        // Subtitle: "Birth of 36 July"
        fontRenderer.headerFont.setColor(new Color(0.92f, 0.88f, 0.78f, 1f));
        glyphLayout.setText(fontRenderer.headerFont, "Birth of 36 July");
        fontRenderer.headerFont.draw(menuBatch, "Birth of 36 July",
            centerX - glyphLayout.width / 2f, titleBlockY + 14f);

        // Tagline
        fontRenderer.smallFont.setColor(menuGoldMuted);
        glyphLayout.setText(fontRenderer.smallFont, "July 2024  •  Dhaka University Campus");
        fontRenderer.smallFont.draw(menuBatch, "July 2024  •  Dhaka University Campus",
            centerX - glyphLayout.width / 2f, titleBlockY - 14f);

        // PLAY button text
        fontRenderer.headerFont.setColor(menuSelection == 0 ? menuGoldAccent : new Color(0.85f, 0.82f, 0.75f, 1f));
        glyphLayout.setText(fontRenderer.headerFont, "START JOURNEY");
        fontRenderer.headerFont.draw(menuBatch, "START JOURNEY",
            centerX - glyphLayout.width / 2f, playBtnY + btnH / 2f + 8f);

        // EXIT button text
        fontRenderer.headerFont.setColor(menuSelection == 1 ? new Color(0.95f, 0.40f, 0.35f, 1f) : new Color(0.70f, 0.65f, 0.60f, 1f));
        glyphLayout.setText(fontRenderer.headerFont, "EXIT GAME");
        fontRenderer.headerFont.draw(menuBatch, "EXIT GAME",
            centerX - glyphLayout.width / 2f, exitBtnY + btnH / 2f + 8f);

        // Navigation hint
        fontRenderer.smallFont.setColor(new Color(0.55f, 0.52f, 0.48f, 0.8f));
        String navHint = "W/S or Arrow Keys Navigate    ENTER or J Select";
        glyphLayout.setText(fontRenderer.smallFont, navHint);
        fontRenderer.smallFont.draw(menuBatch, navHint,
            centerX - glyphLayout.width / 2f, exitBtnY - 30f);

        // Credits at bottom
        fontRenderer.smallFont.setColor(new Color(0.42f, 0.40f, 0.38f, 0.7f));
        String credits = "Atia Sanjida  •  ICE, BAUET  •  Student Code: 2x12";
        glyphLayout.setText(fontRenderer.smallFont, credits);
        fontRenderer.smallFont.draw(menuBatch, credits,
            centerX - glyphLayout.width / 2f, 45f);

        menuBatch.end();

        // Automated visual verification for Main Menu
        if (System.getProperty("bd.spark36.testMenuScreenshot") != null) {
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 1.0f) {
                takeScreenshot("spark36_menu_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        }
    }

    // ==========================================
    // GAMEPLAY RENDERING (all existing logic)
    // ==========================================
    private void renderGameplay() {
        float delta = Gdx.graphics.getDeltaTime();
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        handleGameplayInputs(delta);

        // Check for exit actions from pause menu or victory screen
        int exitAction = hud.consumeExitAction();
        if (exitAction == 1) {
            returnToMainMenu();
            return;
        } else if (exitAction == 2) {
            Gdx.app.exit();
            return;
        } else if (exitAction == 3) {
            // Replay Level 1
            player.setPosition(0f, 0f, 49.4f);
            memorials.reset();
            hud.reset();
            return;
        }

        // 1. Update Game Logic
        boolean canMove = !hud.isModalOpen();
        player.update(delta, camera.getYaw(), canMove);
        camera.update(delta, player.getPosition(), canMove);
        memorials.update(delta);
        hud.update(delta, player, memorials);

        // 2. Render Atmospheric Dawn Sky Background (Golden Morning)
        atmosphere.renderSkyBackground(screenW, screenH);

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

        // 5. Render Atmospheric Crepuscular God Rays & In-World 3D Labels
        atmosphere.renderAtmosphereOverlays(camera.getCamera(), delta, screenW, screenH);

        // 6. Render 2D Where Winds Meet HUD Pass with 3D Waypoint Pin projection
        hud.render(player, memorials, camera.getYaw(), camera.getCamera());
    }

    private void handleGameplayInputs(float delta) {
        // Toggle fullscreen with F11
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1440, 900);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        // Automatic cursor capture management:
        // In active gameplay (no modal/map/pause open), ALWAYS keep cursor captured
        // so trackpad/mouse immediately controls camera without needing a click.
        if (!hud.isModalOpen()) {
            if (!Gdx.input.isCursorCatched()) {
                Gdx.input.setCursorCatched(true);
            }
        } else {
            if (Gdx.input.isCursorCatched()) {
                Gdx.input.setCursorCatched(false);
            }
        }

        // F12 Screenshot key
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            takeScreenshot("spark36_" + System.currentTimeMillis());
        }

        // Automated visual verification support
        if (System.getProperty("bd.spark36.testVictory") != null) {
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isModalOpen()) {
                for (bd.spark36.world.JulyMemorials.MemorialEntry e : memorials.getEntries()) {
                    e.inspected = true;
                }
                hud.triggerVictoryForTest();
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_victory_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testModalScreenshot") != null) {
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isModalOpen()) {
                hud.openMemorialModal(memorials.getNextObjective(player.getPosition()));
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_modal_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testScreenshot") != null) {
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
        if (gameplayInitialized && camera != null) {
            camera.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (menuBatch != null) menuBatch.dispose();
        if (modelBatch != null) modelBatch.dispose();
        if (world != null) world.dispose();
        if (memorials != null) memorials.dispose();
        if (studentMesh != null) studentMesh.dispose();
        if (hud != null) hud.dispose();
        if (fontRenderer != null) fontRenderer.dispose();
        if (atmosphere != null) atmosphere.dispose();
        if (textures != null) textures.dispose();
    }
}
