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
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.input.GameInput;
import bd.spark36.render.GraphicsTier;
import bd.spark36.render.RenderPipelineConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentMesh;
import bd.spark36.hud.FontRenderer;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.AtmosphereRenderer;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.JulyMemorials;
import bd.spark36.world.ParticleSystem;
import bd.spark36.world.PostProcessor;
import bd.spark36.world.TextureFactory;
import bd.spark36.world.WeatherController;
import bd.spark36.world.WeatherState;


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
    private ModelBatch shadowBatch;
    private final Vector3 shadowForward = new Vector3();
    private DhakaCampusWorld world;
    private JulyMemorials memorials;
    private StudentMesh studentMesh;
    private CampusCrowd crowd;
    private PlayerController player;
    private CinematicCamera camera;
    private TextureFactory textures;
    private AtmosphereRenderer atmosphere;
    private WhereWindsMeetHUD hud;
    private PostProcessor postProcessor;
    private ParticleSystem particleSystem;
    private bd.spark36.character.HelmetGoon helmetGoon;
    private bd.spark36.level.LevelManager levelManager;
    private boolean attackHitProcessed = false;
    private float hitStopTimer = 0f;
    private boolean gameplayInitialized = false;
    private int selectedChapter = 1;
    private String menuNotice = null;
    private float menuNoticeTimer = 0f;

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

        String levelProp = System.getProperty("bd.spark36.level");
        if (levelProp != null) {
            try {
                selectedChapter = Integer.parseInt(levelProp.trim());
            } catch (Exception ignored) {}
        } else if (System.getProperty("bd.spark36.baseline") != null) {
            selectedChapter = 1;
        } else {
            selectedChapter = bd.spark36.level.LevelConfig.getUnlockedLevel();
        }

        if (System.getProperty("bd.spark36.testHeroScreenshot") != null ||
            System.getProperty("bd.spark36.testSitScreenshot") != null ||
            System.getProperty("bd.spark36.testScreenshot") != null) {
            System.clearProperty("bd.spark36.testVictory");
            System.clearProperty("bd.spark36.testModalScreenshot");
            System.clearProperty("bd.spark36.testMapScreenshot");
        }
        if (System.getProperty("bd.spark36.testMenuScreenshot") == null &&
            (System.getProperty("bd.spark36.testScreenshot") != null ||
             System.getProperty("bd.spark36.testHeroScreenshot") != null ||
             System.getProperty("bd.spark36.testSitScreenshot") != null ||
             System.getProperty("bd.spark36.testMapScreenshot") != null ||
             System.getProperty("bd.spark36.testModalScreenshot") != null ||
             System.getProperty("bd.spark36.testCombatScreenshot") != null ||
             System.getProperty("bd.spark36.testPondScreenshot") != null ||
             System.getProperty("bd.spark36.testPauseScreenshot") != null ||
             System.getProperty("bd.spark36.testPauseMenu") != null ||
             System.getProperty("bd.spark36.testExitConfirm") != null ||
             System.getProperty("bd.spark36.testSummerCurzon") != null ||
             System.getProperty("bd.spark36.testBambooGrove") != null ||
             System.getProperty("bd.spark36.testCongratulations") != null ||
             System.getProperty("bd.spark36.testVictory") != null ||
             System.getProperty("bd.spark36.testNpcDialogue") != null ||
             System.getProperty("bd.spark36.baseline") != null ||
             System.getProperty("bd.spark36.testCard") != null)) {
            startGameplay();
        }
    }

    /**
     * Lazily initializes all 3D gameplay subsystems on first play.
     * Keeps them alive for re-entry from pause menu.
     */
    private void initGameplay() {
        if (gameplayInitialized) return;

        if (RenderPipelineConfig.pbrEnabled) {
            PBRShaderConfig pbrConfig = PBRShaderProvider.createDefaultConfig();
            pbrConfig.numDirectionalLights = 2;
            pbrConfig.numBones = 16;
            pbrConfig.manualGammaCorrection = false;
            pbrConfig.manualSRGB = PBRShaderConfig.SRGB.FAST;
            modelBatch = new ModelBatch(PBRShaderProvider.createDefault(pbrConfig));
            shadowBatch = new ModelBatch(PBRShaderProvider.createDefaultDepth(16));
        } else {
            modelBatch = new ModelBatch();
            shadowBatch = new ModelBatch(new DepthShaderProvider());
        }
        textures = new TextureFactory();
        studentMesh = new StudentMesh(textures);
        player = new PlayerController();
        camera = new CinematicCamera(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        atmosphere = new AtmosphereRenderer(fontRenderer);
        hud = new WhereWindsMeetHUD(fontRenderer);
        hud.setTextures(textures);

        levelManager = new bd.spark36.level.LevelManager(textures, player, camera, hud);
        levelManager.loadLevel(selectedChapter);
        syncLevelSubsystems();

        // Test hook: start somewhere other than the gate, e.g. -Dbd.spark36.startPos=4,41
        String startPos = System.getProperty("bd.spark36.startPos");
        if (startPos != null) {
            String[] xz = startPos.split(",");
            player.setPosition(Float.parseFloat(xz[0].trim()), 0f, Float.parseFloat(xz[1].trim()));
        }

        // AAA post-processing pipeline (bloom + color grade + vignette + grain)
        postProcessor = new PostProcessor(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        // Environmental particle system (leaves, dust, water ripples)
        particleSystem = new ParticleSystem();

        gameplayInitialized = true;
    }

    private void syncLevelSubsystems() {
        if (levelManager != null) {
            bd.spark36.level.CampaignLevel active = levelManager.getActiveLevel();
            if (active != null) {
                if (active.getWorld() != null) world = active.getWorld();
                if (active.getMemorials() != null) memorials = active.getMemorials();
                if (active.getCrowd() != null) crowd = active.getCrowd();
                if (active instanceof bd.spark36.level.Level1PeacefulCampus) {
                    helmetGoon = ((bd.spark36.level.Level1PeacefulCampus) active).getCampusSecurity();
                    hud.setHelmetGoon(helmetGoon);
                } else {
                    hud.setHelmetGoon(null);
                }
            }
        }
        if (world == null) world = new DhakaCampusWorld(textures);
        if (memorials == null) memorials = new JulyMemorials();
        if (crowd == null) crowd = new CampusCrowd(16, player.getColliders());
    }

    /** Transitions from Main Menu to Peaceful Exploration Campaign */
    public void startGameplay() {
        initGameplay();
        if (levelManager != null && levelManager.getActiveLevel() != null && levelManager.getActiveLevel().getLevelNumber() != selectedChapter) {
            levelManager.loadLevel(selectedChapter);
        }
        syncLevelSubsystems();
        if (helmetGoon != null) {
            helmetGoon.setRole(bd.spark36.level.LevelConfig.isCombatEnabled() ?
                bd.spark36.character.HelmetGoon.Role.HELMETED_AGGRESSOR :
                bd.spark36.character.HelmetGoon.Role.CAMPUS_SECURITY);
        }
        hud.reset();
        gameState = GameState.PLAYING;
        Gdx.input.setCursorCatched(true);
    }

    /** Transitions from Main Menu to Arena Combat Mode */
    public void startArenaMode() {
        bd.spark36.level.LevelConfig.reset(2, bd.spark36.level.LevelConfig.GameMode.ARENA);
        startGameplay();
        if (levelManager != null) {
            levelManager.loadLevel(2);
            syncLevelSubsystems();
        }
    }

    /** Transitions from Gameplay back to Main Menu */
    public void returnToMainMenu() {
        gameState = GameState.MAIN_MENU;
        menuSelection = 0;
        selectedChapter = bd.spark36.level.LevelConfig.getUnlockedLevel();
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

        // Virtual coordinates scaling (1600 baseline)
        float w = 1600f;
        float h = 1600f * ((float) screenH / (float) screenW);
        float centerX = w / 2f;

        float btnW = 480f;
        float btnH = 50f;
        float btnX = centerX - btnW / 2f;
        float playBtnY = h * 0.40f;
        float arenaBtnY = playBtnY - btnH - 16f;
        float exitBtnY = arenaBtnY - btnH - 16f;

        // Automated testing support: auto start gameplay if system property set
        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoStartGame"))) {
            startGameplay();
            return;
        }

        // Handle Mouse Navigation (Hover & Click)
        float mouseX = ((float) Gdx.input.getX() / (float) Gdx.graphics.getWidth()) * w;
        float mouseY = (1.0f - (float) Gdx.input.getY() / (float) Gdx.graphics.getHeight()) * h;
        boolean mouseClicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (mouseX >= btnX && mouseX <= btnX + btnW) {
            if (mouseY >= playBtnY && mouseY <= playBtnY + btnH) {
                if (menuSelection != 0) {
                    menuSelection = 0;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (mouseClicked) {
                    if (!bd.spark36.level.LevelConfig.isLevelUnlocked(selectedChapter)) {
                        bd.spark36.audio.SoundManager.get().playBlock();
                        menuNotice = "CHAPTER " + selectedChapter + " IS LOCKED! Complete Chapter " + (selectedChapter - 1) + " to unlock.";
                        menuNoticeTimer = 3.5f;
                    } else {
                        bd.spark36.audio.SoundManager.get().playClick();
                        bd.spark36.level.LevelConfig.reset(selectedChapter, bd.spark36.level.LevelConfig.GameMode.STORY);
                        startGameplay();
                        return;
                    }
                }
            } else if (mouseY >= arenaBtnY && mouseY <= arenaBtnY + btnH) {
                if (menuSelection != 1) {
                    menuSelection = 1;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (mouseClicked) {
                    bd.spark36.audio.SoundManager.get().playClick();
                    startArenaMode();
                    return;
                }
            } else if (mouseY >= exitBtnY && mouseY <= exitBtnY + btnH) {
                if (menuSelection != 2) {
                    menuSelection = 2;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (mouseClicked) {
                    bd.spark36.audio.SoundManager.get().playClick();
                    Gdx.app.exit();
                    return;
                }
            }
        }

        // Handle menu navigation (WASD + Arrow Keys + ENTER/SPACE)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            menuSelection = (menuSelection - 1 + 3) % 3;
            bd.spark36.audio.SoundManager.get().playClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            menuSelection = (menuSelection + 1) % 3;
            bd.spark36.audio.SoundManager.get().playClick();
        }
        if (menuSelection == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedChapter = (selectedChapter - 2 + 6) % 6 + 1; // 1 to 6
                bd.spark36.audio.SoundManager.get().playClick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedChapter = (selectedChapter % 6) + 1; // 1 to 6
                bd.spark36.audio.SoundManager.get().playClick();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (menuSelection == 0) {
                if (!bd.spark36.level.LevelConfig.isLevelUnlocked(selectedChapter)) {
                    bd.spark36.audio.SoundManager.get().playBlock();
                    menuNotice = "CHAPTER " + selectedChapter + " IS LOCKED! Complete Chapter " + (selectedChapter - 1) + " to unlock.";
                    menuNoticeTimer = 3.5f;
                    return;
                }
                bd.spark36.audio.SoundManager.get().playClick();
                bd.spark36.level.LevelConfig.reset(selectedChapter, bd.spark36.level.LevelConfig.GameMode.STORY);
                startGameplay();
                return;
            } else if (menuSelection == 1) {
                bd.spark36.audio.SoundManager.get().playClick();
                startArenaMode();
                return;
            } else {
                bd.spark36.audio.SoundManager.get().playClick();
                Gdx.app.exit();
                return;
            }
        }

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
        // 1. PLAY CAMPAIGN (Peaceful)
        if (menuSelection == 0) {
            shapeRenderer.setColor(menuSelectedBg);
            shapeRenderer.rect(btnX - 4f, playBtnY - 4f, btnW + 8f, btnH + 8f);
            shapeRenderer.setColor(menuGoldAccent.r, menuGoldAccent.g, menuGoldAccent.b, 0.08f);
            shapeRenderer.rect(btnX, playBtnY, btnW, btnH);
        } else {
            shapeRenderer.setColor(menuGlassBg);
            shapeRenderer.rect(btnX, playBtnY, btnW, btnH);
        }

        // 2. ARENA COMBAT MODE
        if (menuSelection == 1) {
            shapeRenderer.setColor(menuSelectedBg);
            shapeRenderer.rect(btnX - 4f, arenaBtnY - 4f, btnW + 8f, btnH + 8f);
            shapeRenderer.setColor(menuGoldAccent.r, menuGoldAccent.g, menuGoldAccent.b, 0.08f);
            shapeRenderer.rect(btnX, arenaBtnY, btnW, btnH);
        } else {
            shapeRenderer.setColor(menuGlassBg);
            shapeRenderer.rect(btnX, arenaBtnY, btnW, btnH);
        }

        // 3. EXIT button
        if (menuSelection == 2) {
            shapeRenderer.setColor(menuSelectedBg);
            shapeRenderer.rect(btnX - 4f, exitBtnY - 4f, btnW + 8f, btnH + 8f);
            shapeRenderer.setColor(menuGoldAccent.r, menuGoldAccent.g, menuGoldAccent.b, 0.08f);
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
        shapeRenderer.rect(btnX, arenaBtnY, btnW, btnH);

        shapeRenderer.setColor(menuSelection == 2 ? menuGoldAccent : menuGoldBorder);
        shapeRenderer.rect(btnX, exitBtnY, btnW, btnH);

        // Decorative corner brackets on selected button
        float selY = (menuSelection == 0) ? playBtnY : ((menuSelection == 1) ? arenaBtnY : exitBtnY);
        shapeRenderer.setColor(menuGoldAccent);
        float cbLen = 14f;
        shapeRenderer.line(btnX - 6f, selY + btnH + 6f, btnX - 6f, selY + btnH + 6f - cbLen);
        shapeRenderer.line(btnX - 6f, selY + btnH + 6f, btnX - 6f + cbLen, selY + btnH + 6f);
        shapeRenderer.line(btnX + btnW + 6f, selY + btnH + 6f, btnX + btnW + 6f, selY + btnH + 6f - cbLen);
        shapeRenderer.line(btnX + btnW + 6f, selY + btnH + 6f, btnX + btnW + 6f - cbLen, selY + btnH + 6f);
        shapeRenderer.line(btnX - 6f, selY - 6f, btnX - 6f, selY - 6f + cbLen);
        shapeRenderer.line(btnX - 6f, selY - 6f, btnX - 6f + cbLen, selY - 6f);
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

        // Button 1: Campaign
        boolean isUnlocked = bd.spark36.level.LevelConfig.isLevelUnlocked(selectedChapter);
        String chapterName;
        switch (selectedChapter) {
            case 1: chapterName = "CH 1: CURZON SOLIDARITY (JUL 1-14)"; break;
            case 2: chapterName = "CH 2: CONFRONTATION (JUL 15-16)"; break;
            case 3: chapterName = "CH 3: POLICE ESCALATION (JUL 17-18)"; break;
            case 4: chapterName = "CH 4: CURFEW BLACKOUT (JUL 19-22)"; break;
            case 5: chapterName = "CH 5: MASS MOBILIZATION (AUG 3-4)"; break;
            case 6: chapterName = "CH 6: VICTORY & 36 JULY (AUG 5)"; break;
            default: chapterName = "START CAMPAIGN"; break;
        }
        if (!isUnlocked) {
            chapterName += " [LOCKED]";
        }
        String playLabel = (menuSelection == 0) ? "<  " + chapterName + "  >" : chapterName;
        glyphLayout.setText(fontRenderer.headerFont, playLabel);
        if (menuSelection == 0) {
            fontRenderer.headerFont.setColor(isUnlocked ? menuGoldAccent : new Color(0.85f, 0.45f, 0.45f, 0.95f));
        } else {
            fontRenderer.headerFont.setColor(isUnlocked ? new Color(0.85f, 0.82f, 0.75f, 1f) : new Color(0.55f, 0.50f, 0.50f, 0.7f));
        }
        fontRenderer.headerFont.draw(menuBatch, playLabel,
            centerX - glyphLayout.width / 2f, playBtnY + btnH / 2f + 8f);

        if (menuNoticeTimer > 0f && menuNotice != null) {
            menuNoticeTimer -= delta;
            fontRenderer.smallFont.setColor(new Color(1f, 0.40f, 0.35f, Math.min(1f, menuNoticeTimer)));
            glyphLayout.setText(fontRenderer.smallFont, menuNotice);
            fontRenderer.smallFont.draw(menuBatch, menuNotice,
                centerX - glyphLayout.width / 2f, playBtnY - 14f);
        }

        // Button 2: Arena
        fontRenderer.headerFont.setColor(menuSelection == 1 ? menuGoldAccent : new Color(0.85f, 0.82f, 0.75f, 1f));
        glyphLayout.setText(fontRenderer.headerFont, "MARTIAL ARTS ARENA (COMBAT)");
        fontRenderer.headerFont.draw(menuBatch, "MARTIAL ARTS ARENA (COMBAT)",
            centerX - glyphLayout.width / 2f, arenaBtnY + btnH / 2f + 8f);

        // Button 3: Exit
        fontRenderer.headerFont.setColor(menuSelection == 2 ? new Color(0.95f, 0.40f, 0.35f, 1f) : new Color(0.70f, 0.65f, 0.60f, 1f));
        glyphLayout.setText(fontRenderer.headerFont, "EXIT GAME");
        fontRenderer.headerFont.draw(menuBatch, "EXIT GAME",
            centerX - glyphLayout.width / 2f, exitBtnY + btnH / 2f + 8f);

        // Navigation hint
        fontRenderer.smallFont.setColor(new Color(0.55f, 0.52f, 0.48f, 0.8f));
        String navHint = (menuSelection == 0) ?
            "W/S: Navigate  •  A/D or Arrows: Chapter  •  ENTER: Start" :
            "W/S or Arrow Keys: Navigate    •    ENTER or SPACE: Select";
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
        if (delta > 0.1f) delta = 0.1f;
        if (hitStopTimer > 0f) {
            hitStopTimer -= delta;
            delta *= 0.12f; // Impact micro-freeze pause for kinetic weight
        }
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        // Check for exit actions from pause menu or victory screen
        int exitAction = hud.consumeExitAction();
        if (exitAction == 1) {
            returnToMainMenu();
            return;
        } else if (exitAction == 2) {
            Gdx.app.exit();
            return;
        } else if (exitAction == 3) {
            if (levelManager != null) {
                levelManager.restartCurrentLevel();
                syncLevelSubsystems();
            } else {
                player.setPosition(0f, 0f, 49.4f);
                player.resetCombat();
                if (helmetGoon != null) helmetGoon.reset(0f, 36.0f);
                memorials.reset();
                hud.reset();
            }
            return;
        } else if (exitAction == 4) {
            if (levelManager != null) {
                int currentLvl = (levelManager.getActiveLevel() != null) ? levelManager.getActiveLevel().getLevelNumber() : 1;
                bd.spark36.level.LevelConfig.unlockNextLevel(currentLvl);
                selectedChapter = Math.min(6, currentLvl + 1);
                levelManager.nextLevel();
                syncLevelSubsystems();
            }
            return;
        }

        bd.spark36.level.CampaignLevel currentLevel = (levelManager != null) ? levelManager.getActiveLevel() : null;
        DhakaCampusWorld activeWorld = (currentLevel != null && currentLevel.getWorld() != null) ? currentLevel.getWorld() : world;
        JulyMemorials activeMemorials = (currentLevel != null && currentLevel.getMemorials() != null) ? currentLevel.getMemorials() : memorials;
        CampusCrowd activeCrowd = (currentLevel != null && currentLevel.getCrowd() != null) ? currentLevel.getCrowd() : crowd;

        // Pass active boss position to player for smooth martial arts combat facing (Arena mode only)
        if (bd.spark36.level.LevelConfig.isCombatEnabled() && helmetGoon != null && !helmetGoon.isKnockedOut()) {
            player.setCombatTarget(helmetGoon.getPosition());
        } else if (currentLevel == null || !currentLevel.isCombatEnabled()) {
            player.clearCombatTarget();
        }

        boolean isSimulationFrozen = (hud != null && (hud.isPauseMenuOpen() || hud.isExitConfirmOpen()));
        float simDelta = isSimulationFrozen ? 0f : delta;

        // 1. Update Game Logic
        boolean canMove = !hud.isModalOpen() && !hud.isNpcDialogueActive() && System.getProperty("bd.spark36.testHeroScreenshot") == null;
        if (!isSimulationFrozen && !hud.isModalOpen()) {
            if (GameInput.isJustPressed(GameInput.GameAction.INTERACT)) {
                if (levelManager != null) {
                    boolean consumed = levelManager.handleInteraction(player.getPosition());
                    if (consumed) {
                        bd.spark36.audio.SoundManager.get().playObjective();
                    }
                } else if (activeWorld != null && activeWorld.isNearPondGhat(player.getPosition())) {
                    boolean nextSit = !player.isSittingWater();
                    player.setSittingWater(nextSit);
                    if (nextSit) {
                        player.setPosition(0f, 0.18f, 17.2f);
                        bd.spark36.audio.SoundManager.get().playSplash();
                    }
                }
            } else if (!bd.spark36.level.LevelConfig.isCombatEnabled()) {
                if (GameInput.isJustPressed(GameInput.GameAction.ATTACK_PUNCH) || GameInput.isJustPressed(GameInput.GameAction.ATTACK_KICK)) {
                    hud.triggerMissionToast("PEACEFUL MARCH: Combat unlocks in Chapter 2 & Arena [Press H for Codex]", 0);
                }
            }
        }
        player.update(simDelta, camera.getYaw(), canMove && !isSimulationFrozen);

        // Procedural footstep audio
        if (!isSimulationFrozen && canMove && !player.isSittingWater() && !player.isDodging()) {
            bd.spark36.audio.SoundManager.get().updateFootsteps(simDelta, player.isMoving(), player.isSprinting());
        }

        if (levelManager != null) {
            levelManager.update(simDelta, canMove && !isSimulationFrozen);
        } else {
            if (activeCrowd != null) activeCrowd.update(simDelta, player.getPosition());
            if (helmetGoon != null && canMove && !isSimulationFrozen) helmetGoon.update(simDelta, player);
            if (activeWorld != null) activeWorld.update(simDelta);
            if (activeMemorials != null) activeMemorials.update(simDelta);
        }

        // Combat attack resolution
        if (!isSimulationFrozen && currentLevel != null && currentLevel.isCombatEnabled()) {
            levelManager.handlePlayerAttack();
        }

        // Enhanced camera: pass movement state for bob + FOV push + shake
        camera.update(simDelta, player.getPosition(), canMove && !isSimulationFrozen,
            player.isMoving() && !isSimulationFrozen, player.isSprinting() && !isSimulationFrozen, player.getHeadingDegrees());

        hud.update(delta, player, activeMemorials);

        // Update particles (incorporating weather rain intensity & GraphicsTier)
        if (particleSystem != null) {
            WeatherController wc = (activeWorld != null) ? activeWorld.getWeatherController() : null;
            float rainInt = (wc != null) ? wc.getCurrentRainIntensity() : 0f;
            particleSystem.update(delta, camera.getCamera(), rainInt, GraphicsTier.fromSystemProperty());
        }

        // 1b. SUN SHADOW PASS: depth-only render from the sun, centred a little ahead of the player
        if (activeWorld != null) {
            shadowForward.set(camera.getCamera().direction.x, 0f, camera.getCamera().direction.z).nor();
            activeWorld.beginShadowPass(player.getPosition(), shadowForward);
            shadowBatch.begin(activeWorld.getShadowCamera());
            if (currentLevel != null) {
                currentLevel.renderShadow(shadowBatch);
            } else {
                activeWorld.renderShadowCasters(shadowBatch);
                if (helmetGoon != null) helmetGoon.render(shadowBatch, null);
            }
            studentMesh.render(
                shadowBatch,
                null,
                player.getPosition(),
                player.getHeadingDegrees(),
                player.getPose(),
                player.isCrouching(),
                player.isSittingWater(),
                player.getGender()
            );
            if (activeCrowd != null) {
                activeCrowd.render(shadowBatch, null, studentMesh);
            }
            shadowBatch.end();
            activeWorld.endShadowPass();
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        }

        // 2. POST-PROCESSING: Begin FBO capture (replaces direct sky render)
        boolean ppActive = postProcessor != null && postProcessor.isValid();
        WeatherController wc = (activeWorld != null) ? activeWorld.getWeatherController() : null;
        if (ppActive) {
            postProcessor.beginCapture();
            // Sky renders INSIDE the FBO with camera pitch/yaw coupling and dynamic weather
            atmosphere.renderSkyBackground(screenW, screenH, camera.getCamera(), wc);
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        } else {
            // Fallback: render sky directly to screen
            atmosphere.renderSkyBackground(screenW, screenH, camera.getCamera(), wc);
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        }

        // 3. Render 3D World Pass (inside FBO if post-processing active)
        camera.resize(screenW, screenH);
        modelBatch.begin(camera.getCamera());
        com.badlogic.gdx.graphics.g3d.Environment activeEnv = (currentLevel != null && currentLevel.getEnvironment() != null) ?
            currentLevel.getEnvironment() : ((activeWorld != null) ? activeWorld.getEnvironment() : null);

        if (currentLevel != null) {
            currentLevel.render3D(modelBatch, activeEnv);
        } else if (activeWorld != null) {
            activeWorld.render(modelBatch);
            if (activeMemorials != null) activeMemorials.render(modelBatch, activeEnv);
            if (helmetGoon != null) helmetGoon.render(modelBatch, activeEnv);
        }

        studentMesh.render(
            modelBatch,
            activeEnv,
            player.getPosition(),
            player.getHeadingDegrees(),
            player.getPose(),
            player.isCrouching(),
            player.isSittingWater(),
            player.getGender()
        );
        if (activeCrowd != null) {
            activeCrowd.render(modelBatch, activeEnv, studentMesh);
        }
        modelBatch.end();
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        if (activeWorld != null) {
            activeWorld.renderDebugOpeningNormals(shapeRenderer, camera.getCamera());
        }

        // 4. Atmospheric overlays (labels, god rays) — inside FBO for bloom
        atmosphere.renderAtmosphereOverlays(camera.getCamera(), delta, screenW, screenH);
        if (currentLevel != null) {
            currentLevel.renderOverlays(camera, delta, screenW, screenH);
        }

        // 5. End FBO capture and apply post-processing chain to screen
        if (ppActive) {
            postProcessor.endCapture();
            postProcessor.render(delta, wc);
        }

        // 6. Particle System (leaves, dust, water ripples) — AFTER post-processing, BEFORE HUD
        if (particleSystem != null) {
            particleSystem.render();
        }

        // 7. 2D HUD (always crisp, rendered AFTER post-processing)
        hud.render(player, activeMemorials, camera.getYaw(), camera.getCamera());

        // Process inputs and automated test screenshot triggers AFTER the frame is fully drawn!
        handleGameplayInputs(delta);
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

        // F4 or O: Toggle Orientation Test Card on facade elements
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4) || Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            world.setTestCardActive(!world.isTestCardActive());
        }

        // F3: Toggle Opening Outlines and Normal Vectors
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            DhakaCampusWorld.debugDrawNormals = !DhakaCampusWorld.debugDrawNormals;
            Gdx.app.log("SparkGame", "Debug Draw Normals toggled: " + DhakaCampusWorld.debugDrawNormals);
        }

        // F5: Restart Current Level / F6: Next Level
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            if (levelManager != null) {
                levelManager.restartCurrentLevel();
                syncLevelSubsystems();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            if (levelManager != null) {
                levelManager.nextLevel();
                syncLevelSubsystems();
            }
        }

        // F7: Cycle Dynamic Dhaka Summer Weather
        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
            DhakaCampusWorld activeW = (levelManager != null && levelManager.getActiveLevel() != null && levelManager.getActiveLevel().getWorld() != null) ?
                levelManager.getActiveLevel().getWorld() : world;
            if (activeW != null && activeW.getWeatherController() != null) {
                bd.spark36.world.WeatherState nextW = activeW.getWeatherController().cycleNextWeather();
                hud.triggerMissionToast("WEATHER: " + nextW.displayName, nextW.ordinal() + 1);
            }
        }

        // F8: Cycle Tone Mapping Curve (ACES Filmic -> Reinhard Extended -> Linear Clamp)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            String modeName = RenderPipelineConfig.cycleToneMapping();
            hud.triggerMissionToast("TONE MAPPING: " + modeName, RenderPipelineConfig.toneMappingMode + 1);
        }

        // Automated visual verification support
        if (System.getProperty("bd.spark36.testLevelScreenshot") != null) {
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                int lvlNum = (levelManager != null && levelManager.getActiveLevel() != null) ?
                    levelManager.getActiveLevel().getLevelNumber() : 1;
                takeScreenshot("level_" + lvlNum + "_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        }

        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.testHideParameters"))) {
            hud.setParametersVisible(false);
        }

        String testGender = System.getProperty("bd.spark36.testGender");
        if ("female".equalsIgnoreCase(testGender) || "1".equals(testGender)) {
            player.setGender(1);
        }

        if (System.getProperty("bd.spark36.testHeroScreenshot") != null) {
            hud.reset();
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                String shotName = System.getProperty("bd.spark36.testHeroName", "spark36_hero_character_verified");
                takeScreenshot(shotName);
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testSitScreenshot") != null) {
            hud.reset();
            player.setPosition(0f, 0.18f, 17.2f);
            player.setSittingWater(true);
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                String shotName = System.getProperty("bd.spark36.testHeroName", "spark36_pond_sitting_verified");
                takeScreenshot(shotName);
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testScreenshot") != null) {
            hud.reset();
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testCombatScreenshot") != null) {
            hud.reset();
            player.setPosition(0f, 0f, 39.5f);
            player.setHeadingDegrees(180f); // Face north directly towards Helmet Goon
            if (helmetGoon != null) {
                helmetGoon.reset(0f, 36.0f);
                helmetGoon.startFightDirectly();
            }
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("helmet_goon_duel");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testPondScreenshot") != null) {
            hud.reset();
            player.setPosition(0f, 0.28f, 18.0f);
            player.setHeadingDegrees(180f); // face north into the pond towards lotus pads and Curzon Hall
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("curzon_pond_lotus_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testCongratulations") != null) {
            testTimer += delta;
            if (testTimer >= 0.8f && !hud.isCongratulationsPopupActive()) {
                hud.triggerCongratulationsToastForTest();
            }
            if (!autoScreenshotTaken && testTimer >= 1.8f) {
                takeScreenshot("spark36_congratulations_toast_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testVictory") != null) {
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isModalOpen()) {
                if (levelManager != null && levelManager.getActiveLevel() != null) {
                    levelManager.getActiveLevel().completeAllObjectivesForTest();
                } else if (memorials != null && memorials.getEntries() != null) {
                    for (bd.spark36.world.JulyMemorials.MemorialEntry e : memorials.getEntries()) {
                        e.inspected = true;
                    }
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
        } else if (System.getProperty("bd.spark36.testMapScreenshot") != null) {
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isMapOpen()) {
                hud.openMapForTest();
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_map_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testPauseScreenshot") != null || System.getProperty("bd.spark36.testPauseMenu") != null) {
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isPauseMenuOpen()) {
                hud.openPauseMenuForTest();
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("pause_menu_interactive_verified");
                takeScreenshot("spark36_pause_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testHelpControls") != null) {
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isHelpControlsOpen()) {
                hud.setHelpControlsOpen(true);
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("controls_codex_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testExitConfirm") != null) {
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            testTimer += delta;
            if (testTimer >= 1.0f && !hud.isExitConfirmOpen()) {
                hud.openExitConfirmForTest();
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("exit_confirmation_dialog_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testSummerCurzon") != null) {
            hud.reset();
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            player.setPosition(0f, 0f, 72f);
            player.setHeadingDegrees(180f);
            camera.setAngles(0f, -2.5f);
            camera.setDistance(2.8f);
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("summer_natural_daylight_curzon");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testBambooGrove") != null) {
            hud.reset();
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            player.setPosition(13.0f, 0f, 21.0f);
            player.setHeadingDegrees(355f);
            camera.setAngles(15f, 6.0f);
            camera.setDistance(3.5f);
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("dense_bamboo_clump_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testPhase9Dialogue") != null) {
            hud.reset();
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            player.setPosition(0f, 0f, 70f);
            player.setHeadingDegrees(180f);
            camera.setAngles(0f, -2f);
            camera.setDistance(2.2f);
            testTimer += delta;
            if (testTimer >= 0.8f && !hud.getDialogueManager().isDialogueActive()) {
                hud.startDialogueSequence(bd.spark36.level.DialogueRepository.getStudentDialogueSequence(1, 0));
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("phase9_multiline_dialogue");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testPhase9Prompt") != null) {
            hud.reset();
            if (levelManager.getActiveLevel() == null || levelManager.getActiveLevel().getLevelNumber() != 1) {
                levelManager.loadLevel(1);
                syncLevelSubsystems();
            }
            player.setPosition(3.6f, 0f, -14.2f);
            player.setHeadingDegrees(180f);
            camera.setAngles(0f, -2f);
            camera.setDistance(2.8f);
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 1.5f) {
                takeScreenshot("phase9_interaction_prompt");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.testNpcDialogue") != null) {
            hud.reset();
            player.setPosition(0f, 0f, 70f);
            player.setHeadingDegrees(180f);
            camera.setAngles(0f, -2f);
            camera.setDistance(2.2f);
            testTimer += delta;
            if (testTimer >= 0.8f && !hud.isNpcDialogueActive()) {
                hud.showNpcDialogue(bd.spark36.level.DialogueRepository.getStudentDialogue(1, 0), 5.0f);
            }
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("phase8_crowd_dialogue_interaction");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        } else if (System.getProperty("bd.spark36.baseline") != null) {
            String viewpoint = System.getProperty("bd.spark36.baseline");
            hud.reset();
            if ("spawn".equalsIgnoreCase(viewpoint)) {
                bd.spark36.level.LevelConfig.reset(1, bd.spark36.level.LevelConfig.GameMode.STORY);
                player.setPosition(0f, 0f, 72f);
                player.setHeadingDegrees(180f);
                camera.setAngles(0f, -4f);
                camera.setDistance(4.5f);
                if (helmetGoon != null) {
                    helmetGoon.setRole(bd.spark36.character.HelmetGoon.Role.CAMPUS_SECURITY);
                    helmetGoon.reset(0f, 36.0f);
                }
            } else if ("curzon_front".equalsIgnoreCase(viewpoint)) {
                player.setPosition(0f, 0f, -12f);
                player.setHeadingDegrees(180f);
                camera.setAngles(0f, -2f);
                camera.setDistance(5.5f);
            } else if ("curzon_side".equalsIgnoreCase(viewpoint)) {
                player.setPosition(32f, 0f, -32f);
                player.setHeadingDegrees(270f);
                camera.setAngles(90f, -2f);
                camera.setDistance(6.0f);
            } else if ("pond_ghat".equalsIgnoreCase(viewpoint)) {
                player.setPosition(0f, 0.28f, 18.0f);
                player.setHeadingDegrees(0f);
                camera.setAngles(180f, -12f);
                camera.setDistance(4.5f);
            } else if ("pond_water".equalsIgnoreCase(viewpoint)) {
                player.setPosition(0f, 0.28f, 17.5f);
                player.setHeadingDegrees(0f);
                camera.setFixedView(0f, 3.2f, 18.5f, 0f, 0.12f, 6.0f);
            } else if ("tsc".equalsIgnoreCase(viewpoint)) {
                player.setPosition(48f, 0f, 20f);
                camera.setFixedView(36f, 9.5f, 42f, 68f, 7.5f, 20f);
            } else if ("central_library".equalsIgnoreCase(viewpoint)) {
                player.setPosition(-48f, 0f, 22f);
                camera.setFixedView(-36f, 9.5f, 42f, -68f, 7.5f, 22f);
            } else if ("student_closeup".equalsIgnoreCase(viewpoint)) {
                player.setPosition(0f, 0f, 70f);
                player.setHeadingDegrees(180f);
                camera.setAngles(0f, -2f);
                camera.setDistance(1.6f);
            } else if ("boss_fight".equalsIgnoreCase(viewpoint)) {
                bd.spark36.level.LevelConfig.reset(1, bd.spark36.level.LevelConfig.GameMode.ARENA);
                player.setPosition(0f, 0f, 39.5f);
                player.setHeadingDegrees(180f);
                camera.setAngles(0f, -6f);
                camera.setDistance(4.2f);
                if (helmetGoon != null) {
                    helmetGoon.reset(0f, 36.0f);
                    helmetGoon.startFightDirectly();
                }
            } else if ("world_edge".equalsIgnoreCase(viewpoint)) {
                player.setPosition(-100f, 0f, 72f);
                player.setHeadingDegrees(45f);
                camera.setAngles(225f, -4f);
                camera.setDistance(5.0f);
            } else if ("curzon_centre_window".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(0f, 11.40f, -14.0f, 0f, 11.40f, -20.59f);
            } else if ("curzon_centre_window_back".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(0f, 11.40f, -24.0f, 0f, 11.40f, -20.59f);
            } else if ("curzon_wing_bay".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(-15.6f, 5.87f, -17.5f, -15.6f, 5.87f, -23.49f);
            } else if ("curzon_wing_bay_back".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(-15.6f, 5.87f, -26.5f, -15.6f, 5.87f, -23.49f);
            } else if ("curzon_side_angle".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(-19.5f, 4.20f, -19.0f, -15.6f, 4.20f, -23.49f);
            } else if ("curzon_grand_portal".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(0f, 3.20f, -15.0f, 0f, 3.20f, -20.59f);
            } else if ("curzon_grand_portal_back".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(0f, 3.20f, -23.5f, 0f, 3.20f, -20.59f);
            } else if ("curzon_dome".equalsIgnoreCase(viewpoint)) {
                camera.setFixedView(0f, 16.0f, -8.0f, 0f, 19.0f, -22.5f);
            }
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                String shotName = System.getProperty("bd.spark36.screenshotName");
                if (shotName == null || shotName.isEmpty()) {
                    shotName = "00_baseline/" + viewpoint;
                }
                takeScreenshot(shotName);
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
            String cleanName = name.endsWith(".png") ? name.substring(0, name.length() - 4) : name;
            com.badlogic.gdx.files.FileHandle file;
            if (cleanName.startsWith("assets/")) {
                file = Gdx.files.local(cleanName + ".png");
            } else {
                file = Gdx.files.local("assets/screenshots/" + cleanName + ".png");
            }
            if (!file.parent().exists()) {
                file.parent().mkdirs();
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
        if (postProcessor != null) {
            postProcessor.resize(width, height);
        }
        if (particleSystem != null) {
            particleSystem.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (menuBatch != null) menuBatch.dispose();
        if (modelBatch != null) modelBatch.dispose();
        if (shadowBatch != null) shadowBatch.dispose();
        if (world != null) world.dispose();
        if (memorials != null) memorials.dispose();
        if (studentMesh != null) studentMesh.dispose();
        if (hud != null) hud.dispose();
        if (fontRenderer != null) fontRenderer.dispose();
        if (atmosphere != null) atmosphere.dispose();
        if (textures != null) textures.dispose();
        if (helmetGoon != null) helmetGoon.dispose();
        if (levelManager != null) levelManager.dispose();
        if (postProcessor != null) postProcessor.dispose();
        if (particleSystem != null) particleSystem.dispose();
        bd.spark36.audio.SoundManager.get().dispose();
    }
}

