package bd.spark36.dialogue;

/**
 * Callback invoked upon the successful completion of a dialogue sequence.
 */
public interface DialogueCallback {

    /**
     * Called when the player finishes all lines in a dialogue sequence.
     * @param sequenceId The unique identifier of the completed sequence.
     */
    void onDialogueComplete(String sequenceId);
}
