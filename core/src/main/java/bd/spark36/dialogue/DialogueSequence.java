package bd.spark36.dialogue;

import com.badlogic.gdx.utils.Array;
import bd.spark36.level.DialogueRepository.DialogueLine;

/**
 * Encapsulates a multi-line conversation sequence with speaker tags,
 * bilingual subtitles, and an optional completion event callback.
 */
public class DialogueSequence {

    private final String id;
    private final String title;
    private final Array<DialogueLine> lines;
    private DialogueCallback callback;

    public DialogueSequence(String id, DialogueLine... linesArray) {
        this(id, null, null, linesArray);
    }

    public DialogueSequence(String id, String title, DialogueLine... linesArray) {
        this(id, title, null, linesArray);
    }

    public DialogueSequence(String id, String title, DialogueCallback callback, DialogueLine... linesArray) {
        this.id = (id != null) ? id : "sequence_default";
        this.title = (title != null) ? title : "";
        this.callback = callback;
        this.lines = new Array<DialogueLine>(false, linesArray.length);
        for (DialogueLine line : linesArray) {
            if (line != null) this.lines.add(line);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getLineCount() {
        return lines.size;
    }

    public DialogueLine getLine(int index) {
        if (index >= 0 && index < lines.size) {
            return lines.get(index);
        }
        return null;
    }

    public Array<DialogueLine> getLines() {
        return lines;
    }

    public DialogueCallback getCallback() {
        return callback;
    }

    public DialogueSequence setCallback(DialogueCallback callback) {
        this.callback = callback;
        return this;
    }
}
