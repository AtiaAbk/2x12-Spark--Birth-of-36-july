package bd.historicalgame.game;

/**
 * Controls the high-level state of 2x12.
 */
public class GameManager {

    private GameState currentState;

    public GameManager() {
        currentState = GameState.MAIN_MENU;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setState(GameState state) {

        if (state == null) {
            throw new IllegalArgumentException(
                "Game state cannot be null."
            );
        }

        currentState = state;
    }

    public void startLevel1() {
        currentState = GameState.LEVEL1_INTRO;
    }

    public void startPlaying() {
        currentState = GameState.PLAYING;
    }

    public void pauseGame() {

        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
        }
    }

    public void resumeGame() {

        if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
        }
    }

    public void openDialogue() {
        currentState = GameState.DIALOGUE;
    }

    public void startCinematic() {
        currentState = GameState.CINEMATIC;
    }

    public void completeLevel() {
        currentState = GameState.LEVEL_COMPLETE;
    }

    public void gameOver() {
        currentState = GameState.GAME_OVER;
    }

    public boolean isMainMenu() {
        return currentState == GameState.MAIN_MENU;
    }

    public boolean isLevel1Intro() {
        return currentState == GameState.LEVEL1_INTRO;
    }

    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }

    public boolean isPaused() {
        return currentState == GameState.PAUSED;
    }

    public boolean isDialogue() {
        return currentState == GameState.DIALOGUE;
    }

    public boolean isCinematic() {
        return currentState == GameState.CINEMATIC;
    }
}
