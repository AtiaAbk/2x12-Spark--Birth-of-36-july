package bd.spark36.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import bd.spark36.level.DialogueRepository.DialogueLine;

/**
 * Manages active multi-line dialogue sequences, line advancement via player input,
 * subtitle lifecycle, and execution of completion callbacks.
 * Enforces zero per-frame allocation and prevents input leak during conversations.
 */
public class DialogueManager {

    private DialogueSequence activeSequence = null;
    private int currentLineIndex = 0;
    private float lineDisplayTimer = 0f;
    private float inputCooldown = 0f;

    // Minimum display time before player can skip line (prevents accidental double-skips)
    public static final float MIN_LINE_ADVANCE_COOLDOWN = 0.22f;

    // Auto-advance duration if player does not manually click (e.g. 8.0s per line)
    public static final float AUTO_ADVANCE_TIMEOUT = 8.0f;

    /**
     * Starts playback of a dialogue sequence from line 0.
     */
    public void startDialogue(DialogueSequence sequence) {
        if (sequence == null || sequence.getLineCount() == 0) {
            cancelDialogue();
            return;
        }
        this.activeSequence = sequence;
        this.currentLineIndex = 0;
        this.lineDisplayTimer = 0f;
        this.inputCooldown = MIN_LINE_ADVANCE_COOLDOWN;
    }

    /**
     * Advances to the next line in the active sequence.
     * Completes dialogue and triggers callback if the last line was reached.
     */
    public boolean advanceDialogue() {
        if (activeSequence == null) return false;

        if (currentLineIndex < activeSequence.getLineCount() - 1) {
            currentLineIndex++;
            lineDisplayTimer = 0f;
            inputCooldown = MIN_LINE_ADVANCE_COOLDOWN;
            return true;
        } else {
            completeDialogue();
            return false;
        }
    }

    /**
     * Completes current dialogue sequence cleanly and invokes callback.
     */
    public void completeDialogue() {
        if (activeSequence != null) {
            DialogueSequence seq = activeSequence;
            activeSequence = null;
            currentLineIndex = 0;
            lineDisplayTimer = 0f;
            inputCooldown = 0.3f; // Brief post-dialogue cooldown

            if (seq.getCallback() != null) {
                seq.getCallback().onDialogueComplete(seq.getId());
            }
        }
    }

    /**
     * Cancels dialogue immediately without invoking completion callback.
     */
    public void cancelDialogue() {
        activeSequence = null;
        currentLineIndex = 0;
        lineDisplayTimer = 0f;
        inputCooldown = 0.2f;
    }

    /**
     * Updates display timers and input debounces.
     */
    public void update(float delta) {
        if (inputCooldown > 0f) {
            inputCooldown -= delta;
            if (inputCooldown < 0f) inputCooldown = 0f;
        }

        if (activeSequence != null) {
            lineDisplayTimer += delta;
            // Optional auto-advance on prolonged idle
            if (lineDisplayTimer >= AUTO_ADVANCE_TIMEOUT) {
                advanceDialogue();
            }
        }
    }

    /**
     * Checks player input for dialogue progression.
     * Returns true if input was consumed by the dialogue system,
     * signaling to caller that standard player actions should be suppressed.
     */
    public boolean handleInput() {
        if (activeSequence == null) return false;

        // Escape cancels dialogue
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            cancelDialogue();
            return true;
        }

        // Space, E, Enter, or Left Click advances dialogue
        if (inputCooldown <= 0f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.E) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                advanceDialogue();
                return true;
            }
        }

        // While dialogue is active, block other gameplay inputs from propagating
        return true;
    }

    public boolean isDialogueActive() {
        return activeSequence != null;
    }

    public DialogueSequence getActiveSequence() {
        return activeSequence;
    }

    public DialogueLine getCurrentLine() {
        if (activeSequence != null) {
            return activeSequence.getLine(currentLineIndex);
        }
        return null;
    }

    public int getCurrentLineIndex() {
        return currentLineIndex;
    }

    public int getTotalLines() {
        return (activeSequence != null) ? activeSequence.getLineCount() : 0;
    }

    public boolean isLastLine() {
        return activeSequence != null && currentLineIndex == activeSequence.getLineCount() - 1;
    }

    public float getLineDisplayTimer() {
        return lineDisplayTimer;
    }
}
