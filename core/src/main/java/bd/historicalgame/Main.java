package bd.historicalgame;

import bd.historicalgame.game.GameManager;
import bd.historicalgame.game.GameState;
import bd.historicalgame.ui.UIManager;
import bd.historicalgame.utils.ScreenshotManager;
import bd.historicalgame.world.World;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main entry point of 2x12.
 *
 * Rendering and interaction for every non-gameplay screen
 * (main menu, intro, pause, settings, exit confirmation) is
 * delegated to {@link UIManager}, which owns the Stage, the
 * cinematic dark/gold/cyan visual theme, and mouse click
 * handling. This class keeps ownership of the high-level
 * state machine and keyboard shortcuts, and routes both
 * mouse clicks (via UIManager.Action) and keyboard presses
 * through the same transition methods so the two input
 * paths can never disagree about game state.
 */
public class Main extends ApplicationAdapter {

    private UIManager uiManager;

    private GameManager gameManager;
    private World world;

    /*
     * Main menu options (keyboard navigation index).
     */
    private int selectedOption = 0;

    private final String[] menuOptions = {
        "PLAY",
        "SETTINGS",
        "EXIT"
    };

    /*
     * Pause menu options (keyboard navigation index).
     *
     * Order matches the buttons UIManager draws for the
     * pause screen: RESUME, SETTINGS, EXIT TO MAIN MENU,
     * EXIT GAME.
     */
    private int pauseOption = 0;

    private final String[] pauseOptions = {
        "RESUME",
        "SETTINGS",
        "EXIT TO MAIN MENU",
        "EXIT GAME"
    };

    /*
     * Exit confirmation.
     */
    private boolean exitConfirmation = false;

    /*
     * True when the open confirmation dialog was requested
     * from the pause menu's "EXIT TO MAIN MENU" option, so
     * confirming returns to the main menu instead of quitting.
     */
    private boolean exitToMainMenu = false;

    private int exitSelection = 0;

    private final String[] exitOptions = {
        "YES",
        "NO"
    };

    @Override
    public void create() {

        gameManager = new GameManager();

        uiManager = new UIManager();

        world = null;

        System.out.println("================================");
        System.out.println("              2x12");
        System.out.println("      HISTORICAL ADVENTURE");
        System.out.println("================================");
        System.out.println(
            "State: " +
            gameManager.getCurrentState()
        );
    }

    @Override
    public void render() {

        float delta =
            Gdx.graphics.getDeltaTime();

        handleInput();

        processUIActions();

        GameState state =
            gameManager.getCurrentState();

        /*
         * Keep the UIManager screen in sync with the current
         * game state every frame. showState() is a cheap no-op
         * once it is already showing the right screen, so this
         * is safe to call continuously. It is skipped while an
         * overlay (settings / exit confirmation) is open so the
         * overlay isn't clobbered mid-display.
         */
        if (!exitConfirmation &&
            !uiManager.isSettingsOpen()) {

            uiManager.showState(state);
        }

        /*
         * If exit confirmation is active,
         * show confirmation over the current screen.
         */
        if (exitConfirmation) {

            renderCurrentBackground(state);

            uiManager.renderBackdrop(true);
            uiManager.render(delta);

            return;
        }

        switch (state) {

            case MAIN_MENU:
            case LEVEL1_INTRO:

                renderCurrentBackground(state);

                uiManager.renderBackdrop(false);
                uiManager.render(delta);

                break;

            case PLAYING:

                if (world != null) {

                    world.update(delta);
                    world.render();
                }

                break;

            case PAUSED:

                if (world != null) {
                    world.render();
                }

                uiManager.render(delta);
                break;

            default:

                renderCurrentBackground(state);

                uiManager.renderBackdrop(false);
                uiManager.render(delta);

                break;
        }
    }

    // =========================================================
    // UI ACTIONS (MOUSE)
    // =========================================================

    /**
     * Consumes at most one pending click-driven action per
     * frame from the UIManager and routes it through the same
     * methods keyboard input uses, so mouse and keyboard can
     * never leave the game in inconsistent states.
     */
    private void processUIActions() {

        UIManager.Action action =
            uiManager.consumeAction();

        switch (action) {

            case START_LEVEL1:
                startLevel1();
                break;

            case START_PLAYING:

                gameManager.startPlaying();

                System.out.println(
                    "LEVEL 1 STARTED"
                );

                break;

            case RESUME:
                gameManager.resumeGame();
                break;

            case SETTINGS:
                uiManager.showSettings();
                break;

            case CLOSE_SETTINGS:

                uiManager.showState(
                    gameManager.getCurrentState()
                );

                break;

            case EXIT_TO_MAIN_MENU:
                requestExitToMainMenu();
                break;

            case EXIT_GAME:
                requestExit();
                break;

            case CONFIRM_EXIT:
                confirmExit();
                break;

            case CANCEL_EXIT:
                exitConfirmation = false;
                break;

            case NONE:
            default:
                break;
        }
    }

    // =========================================================
    // INPUT (KEYBOARD)
    // =========================================================

    private void handleInput() {

        // =====================================================
        // GLOBAL: SCREENSHOT
        // =====================================================

        /*
         * Works in every state (menus, gameplay, paused) so the
         * player never has to be in a specific screen to capture
         * one.
         */
        if (Gdx.input.isKeyJustPressed(
            Input.Keys.F12
        )) {

            takeScreenshot();
        }

        // =====================================================
        // GLOBAL: TOGGLE FULLSCREEN / WINDOWED
        // =====================================================

        if (Gdx.input.isKeyJustPressed(
            Input.Keys.F11
        )) {

            toggleFullscreen();
        }

        GameState state =
            gameManager.getCurrentState();

        // =====================================================
        // EXIT CONFIRMATION
        // =====================================================

        if (exitConfirmation) {

            handleExitConfirmation();

            return;
        }

        // =====================================================
        // SETTINGS OVERLAY
        // =====================================================

        if (uiManager.isSettingsOpen()) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                uiManager.showState(state);
            }

            return;
        }

        // =====================================================
        // MAIN MENU
        // =====================================================

        if (state == GameState.MAIN_MENU) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            )) {

                selectedOption--;

                if (selectedOption < 0) {

                    selectedOption =
                        menuOptions.length - 1;
                }
            }

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.DOWN
            )) {

                selectedOption++;

                if (selectedOption >=
                    menuOptions.length) {

                    selectedOption = 0;
                }
            }

            if (isConfirmPressed()) {

                if (selectedOption == 0) {

                    startLevel1();
                }

                else if (selectedOption == 1) {

                    uiManager.showSettings();
                }

                else if (selectedOption == 2) {

                    requestExit();
                }
            }

            /*
             * Q or ESC from main menu
             * asks for exit confirmation.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            ) ||
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ESCAPE
                )) {

                requestExit();
            }
        }

        // =====================================================
        // LEVEL 1 INTRO
        // =====================================================

        else if (state ==
                 GameState.LEVEL1_INTRO) {

            if (isConfirmPressed()) {

                gameManager.startPlaying();

                System.out.println(
                    "LEVEL 1 STARTED"
                );
            }

            /*
             * Q immediately requests exit.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )) {

                requestExit();
            }
        }

        // =====================================================
        // PLAYING
        // =====================================================

        else if (state ==
                 GameState.PLAYING) {

            /*
             * ESC opens pause menu.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                pauseOption = 0;

                gameManager.pauseGame();

                return;
            }

            /*
             * Q asks to exit.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )) {

                requestExit();
            }
        }

        // =====================================================
        // PAUSED
        // =====================================================

        else if (state ==
                 GameState.PAUSED) {

            /*
             * ESC resumes the game.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                gameManager.resumeGame();

                return;
            }

            /*
             * UP.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            )) {

                pauseOption--;

                if (pauseOption < 0) {

                    pauseOption =
                        pauseOptions.length - 1;
                }
            }

            /*
             * DOWN.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.DOWN
            )) {

                pauseOption++;

                if (pauseOption >=
                    pauseOptions.length) {

                    pauseOption = 0;
                }
            }

            /*
             * ENTER.
             */
            if (isConfirmPressed()) {

                handlePauseSelection();
            }

            /*
             * Q asks for exit.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )) {

                requestExit();
            }
        }
    }

    // =========================================================
    // PAUSE MENU SELECTION
    // =========================================================

    private void handlePauseSelection() {

        /*
         * RESUME
         */
        if (pauseOption == 0) {

            gameManager.resumeGame();
        }

        /*
         * SETTINGS
         */
        else if (pauseOption == 1) {

            uiManager.showSettings();
        }

        /*
         * EXIT TO MAIN MENU
         */
        else if (pauseOption == 2) {

            requestExitToMainMenu();
        }

        /*
         * EXIT GAME
         */
        else if (pauseOption == 3) {

            requestExit();
        }
    }

    // =========================================================
    // EXIT SYSTEM
    // =========================================================

    private void requestExit() {

        exitToMainMenu = false;

        exitConfirmation = true;
        exitSelection = 1;

        uiManager.showExitConfirmation(false);

        System.out.println(
            "Exit confirmation opened."
        );
    }

    private void requestExitToMainMenu() {

        exitToMainMenu = true;

        exitConfirmation = true;
        exitSelection = 1;

        uiManager.showExitConfirmation(true);

        System.out.println(
            "Main menu confirmation opened."
        );
    }

    private void handleExitConfirmation() {

        /*
         * LEFT / RIGHT
         */
        if (Gdx.input.isKeyJustPressed(
            Input.Keys.LEFT
        )) {

            exitSelection--;

            if (exitSelection < 0) {
                exitSelection =
                    exitOptions.length - 1;
            }
        }

        if (Gdx.input.isKeyJustPressed(
            Input.Keys.RIGHT
        )) {

            exitSelection++;

            if (exitSelection >=
                exitOptions.length) {

                exitSelection = 0;
            }
        }

        /*
         * ENTER / SPACE
         */
        if (isConfirmPressed()) {

            /*
             * YES
             */
            if (exitSelection == 0) {

                confirmExit();
            }

            /*
             * NO
             */
            else {

                exitConfirmation = false;
            }
        }

        /*
         * ESC cancels confirmation.
         */
        if (Gdx.input.isKeyJustPressed(
            Input.Keys.ESCAPE
        )) {

            exitConfirmation = false;
        }
    }

    /**
     * Confirms whichever exit flow is currently open: returns
     * to the main menu if the dialog came from "EXIT TO MAIN
     * MENU", otherwise quits the application.
     */
    private void confirmExit() {

        exitConfirmation = false;

        if (exitToMainMenu) {

            returnToMainMenu();

        } else {

            exitGame();
        }
    }

    private void returnToMainMenu() {

        System.out.println(
            "Returning to main menu..."
        );

        if (world != null) {

            world.dispose();
            world = null;
        }

        gameManager.setState(
            GameState.MAIN_MENU
        );

        selectedOption = 0;
    }

    private void exitGame() {

        System.out.println(
            "Exiting 2x12..."
        );

        if (world != null) {

            world.dispose();
            world = null;
        }

        Gdx.app.exit();
    }

    // =========================================================
    // START LEVEL 1
    // =========================================================

    private void startLevel1() {

        System.out.println(
            "Loading Level 1..."
        );

        if (world != null) {

            world.dispose();
        }

        world = new World();

        gameManager.startLevel1();
    }

    // =========================================================
    // SCREENSHOT
    // =========================================================

    /**
     * Default windowed size the game returns to when the
     * player leaves fullscreen with F11.
     */
    private static final int WINDOWED_WIDTH = 1280;
    private static final int WINDOWED_HEIGHT = 720;

    private void takeScreenshot() {

        String savedPath =
            ScreenshotManager.capture();

        /*
         * Show a brief on-screen confirmation while in a level.
         * On menu screens the console log from capture() is
         * confirmation enough.
         */
        if (savedPath != null && world != null) {

            world.notifyScreenshotSaved();
        }
    }

    // =========================================================
    // FULLSCREEN / WINDOWED TOGGLE
    // =========================================================

    private void toggleFullscreen() {

        if (Gdx.graphics.isFullscreen()) {

            Gdx.graphics.setWindowedMode(
                WINDOWED_WIDTH,
                WINDOWED_HEIGHT
            );

        } else {

            Graphics.DisplayMode currentMode =
                Gdx.graphics.getDisplayMode();

            Gdx.graphics.setFullscreenMode(
                currentMode
            );
        }
    }

    // =========================================================
    // RESIZE
    // =========================================================

    /**
     * Keeps the UI stage and the 3D camera's aspect ratio in
     * sync whenever the player resizes the window, maximizes
     * it, or toggles fullscreen.
     */
    @Override
    public void resize(int width, int height) {

        if (uiManager != null) {
            uiManager.resize(width, height);
        }

        if (world != null) {
            world.resize(width, height);
        }
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    /**
     * Renders whatever should sit behind the current UIManager
     * screen: the live 3D world for PLAYING / PAUSED, or a
     * plain cleared color for every menu-style state (the
     * UIManager backdrop is painted on top of this).
     */
    private void renderCurrentBackground(
        GameState state
    ) {

        if (state == GameState.PLAYING ||
            state == GameState.PAUSED) {

            if (world != null) {

                world.render();
            }

            return;
        }

        ScreenUtils.clear(
            0.02f,
            0.02f,
            0.03f,
            1f
        );
    }

    // =========================================================
    // KEYBOARD CONFIRMATION
    // =========================================================

    /**
     * ENTER and SPACE both work as the universal confirm/select key.
     */
    private boolean isConfirmPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        if (world != null) {

            world.dispose();
            world = null;
        }

        if (uiManager != null) {
            uiManager.dispose();
        }
    }
}