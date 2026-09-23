package bd.historicalgame.ui;

import bd.historicalgame.assets.FontManager;
import bd.historicalgame.game.GameState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Cinematic UI system for 2x12.
 *
 * Designed to provide:
 * - premium dark surfaces
 * - gold/cyan accent treatment
 * - keyboard and mouse interaction
 * - smooth transitions
 * - responsive desktop layout
 * - minimal visual obstruction
 */
public class UIManager implements Disposable {

    public enum Action {

        NONE,

        START_LEVEL1,
        START_PLAYING,

        RESUME,

        SETTINGS,
        CLOSE_SETTINGS,

        EXIT_TO_MAIN_MENU,
        EXIT_GAME,

        CONFIRM_EXIT,
        CANCEL_EXIT
    }

    /*
     * =========================================================
     * COLORS
     * =========================================================
     */

    private static final Color GOLD =
        Color.valueOf("EFC868");

    private static final Color GOLD_SOFT =
        Color.valueOf("B9923F");

    private static final Color CYAN =
        Color.valueOf("81EFE8");

    private static final Color WHITE =
        Color.valueOf("FBF8F1");

    /*
     * Brightened from the original E7E0D3 / A7A39B so body and
     * hint text stay easily readable now that it renders at its
     * true crisp size instead of a blurred, stretched bitmap font.
     */
    private static final Color TEXT =
        Color.valueOf("F1ECE1");

    private static final Color MUTED =
        Color.valueOf("BDB8AC");

    private static final Color DARK =
        Color.valueOf("090D12");

    private static final Color PANEL =
        Color.valueOf("10161D");

    private static final Color PANEL_SOFT =
        Color.valueOf("171E26");

    /*
     * =========================================================
     * TYPOGRAPHY
     * =========================================================
     */

    /**
     * Reference pixel size for a "scale" of 1.0. Callers keep
     * passing the same relative scale values as before; this class
     * turns that into a real font generated at that exact pixel
     * size (instead of stretching one tiny bitmap font), which is
     * what actually fixes the blurriness.
     */
    private static final float BASE_FONT_PX = 17f;

    private static final int MIN_FONT_PX = 12;
    private static final int MAX_FONT_PX = 92;

    private static final int BUTTON_FONT_PX = 19;

    /*
     * =========================================================
     * CORE
     * =========================================================
     */

    private final Stage stage;
    private final FontManager fontManager;
    private final ShapeRenderer shapes;

    private TextButton.TextButtonStyle buttonStyle;

    /*
     * =========================================================
     * TEXTURES
     * =========================================================
     */

    private Texture panelTexture;
    private Texture softPanelTexture;

    private Texture buttonTexture;
    private Texture hoverTexture;
    private Texture downTexture;

    private TextureRegionDrawable panelDrawable;
    private TextureRegionDrawable softPanelDrawable;

    private TextureRegionDrawable buttonDrawable;
    private TextureRegionDrawable hoverDrawable;
    private TextureRegionDrawable downDrawable;

    /*
     * =========================================================
     * STATE
     * =========================================================
     */

    private GameState activeState;

    private Action pendingAction =
        Action.NONE;

    private boolean settingsOpen;
    private boolean exitConfirmation;

    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public UIManager() {

        stage =
            new Stage(
                new ScreenViewport()
            );

        fontManager =
            new FontManager();

        shapes =
            new ShapeRenderer();

        createTextures();

        configureButtonStyle();
    }

    /*
     * =========================================================
     * TEXTURES
     * =========================================================
     */

    private void createTextures() {

        panelTexture =
            createTexture(
                new Color(
                    PANEL.r,
                    PANEL.g,
                    PANEL.b,
                    0.92f
                )
            );

        softPanelTexture =
            createTexture(
                new Color(
                    PANEL_SOFT.r,
                    PANEL_SOFT.g,
                    PANEL_SOFT.b,
                    0.88f
                )
            );

        buttonTexture =
            createTexture(
                new Color(
                    0.06f,
                    0.08f,
                    0.10f,
                    0.82f
                )
            );

        hoverTexture =
            createTexture(
                new Color(
                    CYAN.r,
                    CYAN.g,
                    CYAN.b,
                    0.12f
                )
            );

        downTexture =
            createTexture(
                new Color(
                    GOLD.r,
                    GOLD.g,
                    GOLD.b,
                    0.24f
                )
            );

        panelDrawable =
            drawable(panelTexture);

        softPanelDrawable =
            drawable(softPanelTexture);

        buttonDrawable =
            drawable(buttonTexture);

        hoverDrawable =
            drawable(hoverTexture);

        downDrawable =
            drawable(downTexture);
    }

    private Texture createTexture(
        Color color
    ) {

        Pixmap pixmap =
            new Pixmap(
                2,
                2,
                Pixmap.Format.RGBA8888
            );

        pixmap.setColor(color);

        pixmap.fill();

        Texture texture =
            new Texture(pixmap);

        texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        pixmap.dispose();

        return texture;
    }

    private TextureRegionDrawable drawable(
        Texture texture
    ) {

        return new TextureRegionDrawable(
            new TextureRegion(texture)
        );
    }

    /*
     * =========================================================
     * SKIN
     * =========================================================
     */

    private void configureButtonStyle() {

        BitmapFont buttonFont =
            fontManager.getBold(BUTTON_FONT_PX);

        TextButton.TextButtonStyle style =
            new TextButton.TextButtonStyle();

        style.font = buttonFont;

        style.fontColor =
            TEXT;

        style.overFontColor =
            CYAN;

        style.downFontColor =
            WHITE;

        style.up =
            buttonDrawable;

        style.over =
            hoverDrawable;

        style.down =
            downDrawable;

        buttonStyle = style;
    }

    /*
     * =========================================================
     * STATE UI
     * =========================================================
     */

    public void showState(
        GameState state
    ) {

        if (
            state == activeState &&
            !settingsOpen &&
            !exitConfirmation
        ) {

            return;
        }

        activeState =
            state;

        stage.clear();

        settingsOpen = false;
        exitConfirmation = false;

        if (
            state ==
            GameState.MAIN_MENU
        ) {

            buildMainMenu();

        } else if (
            state ==
            GameState.LEVEL1_INTRO
        ) {

            buildIntro();

        } else if (
            state ==
            GameState.PAUSED
        ) {

            buildPauseMenu();
        }

        setInteractive(
            state != GameState.PLAYING
        );
    }

    /*
     * =========================================================
     * MAIN MENU
     * =========================================================
     */

    private void buildMainMenu() {

        Table root =
            new Table();

        root.setFillParent(true);

        root.top().left();

        Table content =
            new Table();

        content.top().left();

        content.padTop(95f);
        content.padLeft(85f);

        content.add(
            label(
                "DHAKA UNIVERSITY CAMPUS",
                0.72f,
                GOLD
            )
        )
        .left()
        .row();

        content.add(
            label(
                "2x12",
                4.4f,
                WHITE
            )
        )
        .left()
        .padTop(4f)
        .row();

        content.add(
            label(
                "WHERE WINDS MEET STYLE",
                1.12f,
                TEXT
            )
        )
        .left()
        .padTop(2f)
        .row();

        content.add(
            label(
                "A HISTORICAL ADVENTURE",
                0.70f,
                MUTED
            )
        )
        .left()
        .padTop(8f)
        .row();

        content.add()
            .height(58f)
            .row();

        content.add(
            menuButton(
                "BEGIN JOURNEY",
                Action.START_LEVEL1
            )
        )
        .width(330f)
        .height(56f)
        .left()
        .row();

        content.add(
            menuButton(
                "SETTINGS",
                Action.SETTINGS
            )
        )
        .width(330f)
        .height(50f)
        .left()
        .padTop(9f)
        .row();

        content.add(
            menuButton(
                "EXIT",
                Action.EXIT_GAME
            )
        )
        .width(330f)
        .height(50f)
        .left()
        .padTop(9f)
        .row();

        content.add(
            label(
                "↑ ↓  NAVIGATE     ENTER / SPACE  SELECT     ESC  BACK",
                0.61f,
                MUTED
            )
        )
        .left()
        .padTop(34f)
        .row();

        root.add(content)
            .expand()
            .fill();

        addAnimatedActor(root);
    }

    /*
     * =========================================================
     * INTRO
     * =========================================================
     */

    private void buildIntro() {

        Table root =
            new Table();

        root.setFillParent(true);

        root.bottom().left();

        Table panel =
            new Table();

        panel.setBackground(
            panelDrawable
        );

        panel.pad(
            30f,
            42f,
            28f,
            42f
        );

        panel.add(
            label(
                "LEVEL 01  /  WHERE IT ALL BEGAN",
                0.72f,
                GOLD
            )
        )
        .left()
        .row();

        panel.add(
            label(
                "MID-JUNE 2024",
                2.05f,
                WHITE
            )
        )
        .left()
        .padTop(6f)
        .row();

        panel.add(
            label(
                "A normal day is about to change.",
                0.98f,
                TEXT
            )
        )
        .left()
        .padTop(10f)
        .row();

        panel.add(
            label(
                "Explore the campus. Follow the clues. Discover what is happening.",
                0.76f,
                MUTED
            )
        )
        .left()
        .padTop(6f)
        .row();

        panel.add(
            menuButton(
                "ENTER CAMPUS",
                Action.START_PLAYING
            )
        )
        .left()
        .width(255f)
        .height(50f)
        .padTop(20f)
        .row();

        panel.add(
            label(
                "ENTER / SPACE  BEGIN       Q / ESC  EXIT",
                0.62f,
                MUTED
            )
        )
        .left()
        .padTop(11f)
        .row();

        root.add(panel)
            .width(590f)
            .padLeft(38f)
            .padBottom(38f)
            .left();

        addAnimatedActor(root);
    }

    /*
     * =========================================================
     * PAUSE
     * =========================================================
     */

    private void buildPauseMenu() {

        Table root =
            new Table();

        root.setFillParent(true);

        root.center();

        Table panel =
            new Table();

        panel.setBackground(
            panelDrawable
        );

        panel.pad(
            34f,
            46f,
            30f,
            46f
        );

        panel.add(
            label(
                "PAUSED",
                2.4f,
                WHITE
            )
        )
        .center()
        .row();

        panel.add(
            label(
                "2x12  •  DHAKA UNIVERSITY CAMPUS",
                0.66f,
                GOLD
            )
        )
        .center()
        .padTop(5f)
        .row();

        panel.add()
            .height(25f)
            .row();

        panel.add(
            menuButton(
                "RESUME",
                Action.RESUME
            )
        )
        .width(310f)
        .height(52f)
        .row();

        panel.add(
            menuButton(
                "SETTINGS",
                Action.SETTINGS
            )
        )
        .width(310f)
        .height(48f)
        .padTop(8f)
        .row();

        panel.add(
            menuButton(
                "EXIT TO MAIN MENU",
                Action.EXIT_TO_MAIN_MENU
            )
        )
        .width(310f)
        .height(48f)
        .padTop(8f)
        .row();

        panel.add(
            menuButton(
                "EXIT GAME",
                Action.EXIT_GAME
            )
        )
        .width(310f)
        .height(48f)
        .padTop(8f)
        .row();

        panel.add(
            label(
                "ESC  RESUME",
                0.61f,
                MUTED
            )
        )
        .center()
        .padTop(18f)
        .row();

        root.add(panel)
            .width(430f);

        addAnimatedActor(root);
    }

    /*
     * =========================================================
     * BUTTON
     * =========================================================
     */

    private TextButton menuButton(
        String text,
        final Action action
    ) {

        TextButton button =
            new TextButton(
                text,
                buttonStyle
            );

        button.addListener(
            new ClickListener() {

                @Override
                public void clicked(
                    InputEvent event,
                    float x,
                    float y
                ) {

                    pendingAction =
                        action;
                }
            }
        );

        return button;
    }

    /*
     * =========================================================
     * LABEL
     * =========================================================
     */

    private Label label(
        String text,
        float scale,
        Color color
    ) {

        int pixelSize =
            MathUtils.clamp(
                Math.round(BASE_FONT_PX * scale),
                MIN_FONT_PX,
                MAX_FONT_PX
            );

        BitmapFont crispFont =
            fontManager.get(pixelSize);

        Label.LabelStyle style =
            new Label.LabelStyle(
                crispFont,
                color
            );

        Label label =
            new Label(
                text,
                style
            );

        label.setColor(color);

        return label;
    }

    /*
     * =========================================================
     * ANIMATION
     * =========================================================
     */

    private void addAnimatedActor(
        Actor actor
    ) {

        actor.getColor().a = 0f;

        stage.addActor(actor);

        actor.addAction(
            Actions.fadeIn(
                0.30f,
                Interpolation.fade
            )
        );
    }

    /*
     * =========================================================
     * SETTINGS
     * =========================================================
     */

    public void showSettings() {

        settingsOpen = true;

        stage.clear();

        Table root =
            new Table();

        root.setFillParent(true);

        root.center();

        Table panel =
            new Table();

        panel.setBackground(
            softPanelDrawable
        );

        panel.pad(
            35f,
            45f,
            32f,
            45f
        );

        panel.add(
            label(
                "SETTINGS",
                2.05f,
                WHITE
            )
        )
        .center()
        .row();

        panel.add(
            label(
                "PRESENTATION",
                0.68f,
                GOLD
            )
        )
        .left()
        .padTop(20f)
        .row();

        panel.add(
            label(
                "Display       Desktop / Fullscreen",
                0.80f,
                TEXT
            )
        )
        .left()
        .padTop(9f)
        .row();

        panel.add(
            label(
                "VSync         Enabled",
                0.80f,
                TEXT
            )
        )
        .left()
        .padTop(6f)
        .row();

        panel.add(
            label(
                "Camera        Mouse controlled",
                0.80f,
                TEXT
            )
        )
        .left()
        .padTop(6f)
        .row();

        panel.add(
            label(
                "Audio          Project audio",
                0.80f,
                TEXT
            )
        )
        .left()
        .padTop(6f)
        .row();

        panel.add(
            label(
                "Controls      WASD / SHIFT / ESC",
                0.80f,
                CYAN
            )
        )
        .left()
        .padTop(6f)
        .row();

        panel.add(
            menuButton(
                "BACK",
                Action.CLOSE_SETTINGS
            )
        )
        .width(220f)
        .height(48f)
        .padTop(24f)
        .row();

        panel.add(
            label(
                "ESC  BACK",
                0.61f,
                MUTED
            )
        )
        .center()
        .padTop(11f)
        .row();

        root.add(panel)
            .width(455f);

        addAnimatedActor(root);

        setInteractive(true);
    }

    /*
     * =========================================================
     * EXIT CONFIRMATION
     * =========================================================
     */

    public void showExitConfirmation(
        boolean fromMainMenu
    ) {

        exitConfirmation = true;

        stage.clear();

        Table root =
            new Table();

        root.setFillParent(true);

        root.center();

        Table panel =
            new Table();

        panel.setBackground(
            panelDrawable
        );

        panel.pad(
            34f,
            44f,
            30f,
            44f
        );

        panel.add(
            label(
                fromMainMenu
                    ? "EXIT 2x12?"
                    : "LEAVE GAME?",
                1.95f,
                WHITE
            )
        )
        .center()
        .row();

        panel.add(
            label(
                "Are you sure you want to leave this session?",
                0.78f,
                TEXT
            )
        )
        .center()
        .padTop(11f)
        .row();

        panel.add(
            menuButton(
                "YES, EXIT",
                Action.CONFIRM_EXIT
            )
        )
        .width(270f)
        .height(50f)
        .padTop(21f)
        .row();

        panel.add(
            menuButton(
                "NO, GO BACK",
                Action.CANCEL_EXIT
            )
        )
        .width(270f)
        .height(50f)
        .padTop(8f)
        .row();

        panel.add(
            label(
                "ENTER  CONFIRM       ESC  CANCEL",
                0.60f,
                MUTED
            )
        )
        .center()
        .padTop(15f)
        .row();

        root.add(panel)
            .width(430f);

        addAnimatedActor(root);

        setInteractive(true);
    }

    /*
     * =========================================================
     * CINEMATIC BACKDROP
     * =========================================================
     */

    public void renderBackdrop(
        boolean dark
    ) {

        float width =
            Gdx.graphics.getWidth();

        float height =
            Gdx.graphics.getHeight();

        shapes.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            new Color(
                DARK.r,
                DARK.g,
                DARK.b,
                1f
            )
        );

        shapes.rect(
            0f,
            0f,
            width,
            height
        );

        /*
         * Soft atmospheric layer.
         */
        shapes.setColor(
            new Color(
                0.06f,
                0.09f,
                0.11f,
                0.42f
            )
        );

        shapes.rect(
            0f,
            height * 0.52f,
            width,
            height * 0.48f
        );

        /*
         * Gold cinematic accent.
         */
        shapes.setColor(
            new Color(
                GOLD_SOFT.r,
                GOLD_SOFT.g,
                GOLD_SOFT.b,
                0.55f
            )
        );

        shapes.rect(
            70f,
            75f,
            2f,
            height - 150f
        );

        shapes.rect(
            70f,
            height - 80f,
            Math.min(
                570f,
                width * 0.42f
            ),
            2f
        );

        /*
         * Cyan subtle accent.
         */
        shapes.setColor(
            new Color(
                CYAN.r,
                CYAN.g,
                CYAN.b,
                0.28f
            )
        );

        shapes.rect(
            width - 120f,
            75f,
            1f,
            height - 150f
        );

        shapes.end();
    }

    /*
     * =========================================================
     * RENDER
     * =========================================================
     */

    public void render(
        float delta
    ) {

        stage.act(
            Math.min(
                delta,
                0.05f
            )
        );

        stage.draw();
    }

    /*
     * Gameplay overlay extension point.
     */
    public void renderGameplayOverlay(
        float delta
    ) {
        /*
         * Existing Level1World HUD remains responsible
         * for gameplay values already implemented there.
         */
    }

    /*
     * =========================================================
     * INPUT MODE
     * =========================================================
     */

    public void setInteractive(
        boolean interactive
    ) {

        if (interactive) {

            Gdx.input.setCursorCatched(false);

            Gdx.input.setInputProcessor(
                stage
            );

        } else {

            Gdx.input.setInputProcessor(null);

            Gdx.input.setCursorCatched(true);
        }
    }

    /*
     * =========================================================
     * ACTION QUEUE
     * =========================================================
     */

    public Action consumeAction() {

        Action action =
            pendingAction;

        pendingAction =
            Action.NONE;

        return action;
    }

    /*
     * =========================================================
     * FLAGS
     * =========================================================
     */

    public boolean isSettingsOpen() {

        return settingsOpen;
    }

    public boolean isExitConfirmation() {

        return exitConfirmation;
    }

    public void closeSettings() {

        settingsOpen = false;

        pendingAction =
            Action.CLOSE_SETTINGS;
    }

    public void cancelExitConfirmation() {

        exitConfirmation = false;

        pendingAction =
            Action.CANCEL_EXIT;
    }

    public void clearOverlayFlags() {

        settingsOpen = false;

        exitConfirmation = false;

        activeState = null;
    }

    /*
     * =========================================================
     * STAGE
     * =========================================================
     */

    /**
     * Keeps the UI stage's viewport matched to the actual
     * window size whenever the player resizes the game window,
     * maximizes it, or toggles fullscreen. Without this, mouse
     * click coordinates and layout drift out of sync with the
     * window as soon as it stops matching the size the stage
     * was created with.
     */
    public void resize(int width, int height) {

        stage.getViewport().update(
            width,
            height,
            true
        );
    }

    public Stage getStage() {

        return stage;
    }

    /*
     * =========================================================
     * DISPOSE
     * =========================================================
     */

    @Override
    public void dispose() {

        stage.dispose();

        fontManager.dispose();

        shapes.dispose();

        if (panelTexture != null) {
            panelTexture.dispose();
        }

        if (softPanelTexture != null) {
            softPanelTexture.dispose();
        }

        if (buttonTexture != null) {
            buttonTexture.dispose();
        }

        if (hoverTexture != null) {
            hoverTexture.dispose();
        }

        if (downTexture != null) {
            downTexture.dispose();
        }
    }
}