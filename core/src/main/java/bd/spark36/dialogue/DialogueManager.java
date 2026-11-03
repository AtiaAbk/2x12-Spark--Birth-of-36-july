// ei file ta screen e bangla dialogue text ar choices present kore
package bd.spark36.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import bd.spark36.level.DialogueRepository.DialogueLine;

public class DialogueManager {

    private DialogueSequence activeSequence = null;
    private int currentLineIndex = 0;
    private float lineDisplayTimer = 0f;
    private float inputCooldown = 0f;

    public static final float MIN_LINE_ADVANCE_COOLDOWN = 0.22f;

    public static final float AUTO_ADVANCE_TIMEOUT = 8.0f;

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

    public void completeDialogue() {
        if (activeSequence != null) {
            DialogueSequence seq = activeSequence;
            activeSequence = null;
            currentLineIndex = 0;
            lineDisplayTimer = 0f;
            inputCooldown = 0.3f;

            if (seq.getCallback() != null) {
                seq.getCallback().onDialogueComplete(seq.getId());
            }
        }
    }

    public void cancelDialogue() {
        activeSequence = null;
        currentLineIndex = 0;
        lineDisplayTimer = 0f;
        inputCooldown = 0.2f;
    }

    public void update(float delta) {
        if (inputCooldown > 0f) {
            inputCooldown -= delta;
            if (inputCooldown < 0f) inputCooldown = 0f;
        }

        if (activeSequence != null) {
            lineDisplayTimer += delta;

            if (lineDisplayTimer >= AUTO_ADVANCE_TIMEOUT) {
                advanceDialogue();
            }
        }
    }

    public boolean handleInput() {
        if (activeSequence == null) return false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            cancelDialogue();
            return true;
        }

        if (inputCooldown <= 0f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.E) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                advanceDialogue();
                return true;
            }
        }

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
