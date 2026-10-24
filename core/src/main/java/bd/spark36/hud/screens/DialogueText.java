package bd.spark36.hud.screens;

import bd.spark36.app.DialoguePrefs;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Temporarily scales the dialogue fonts by the player's text-size setting and restores them
 * afterwards, so the HUD's dialogue boxes need only a begin/end pair.
 */
public final class DialogueText {

    private static final int MAX = 8;
    private static final BitmapFont[] fonts = new BitmapFont[MAX];
    private static final float[] scaleX = new float[MAX];
    private static final float[] scaleY = new float[MAX];
    private static int count = 0;

    private DialogueText() {}

    /** Multiplies each distinct font's scale by the text-size setting. Pair with {@link #end()}. */
    public static void begin(BitmapFont... list) {
        end();
        float k = DialoguePrefs.textScale();
        for (BitmapFont f : list) {
            if (f == null || count >= MAX || contains(f)) continue;
            fonts[count] = f;
            scaleX[count] = f.getData().scaleX;
            scaleY[count] = f.getData().scaleY;
            if (k != 1f) f.getData().setScale(scaleX[count] * k, scaleY[count] * k);
            count++;
        }
    }

    /** Restores the scales saved by {@link #begin(BitmapFont...)}. Safe to call twice. */
    public static void end() {
        for (int i = 0; i < count; i++) {
            fonts[i].getData().setScale(scaleX[i], scaleY[i]);
            fonts[i] = null;
        }
        count = 0;
    }

    private static boolean contains(BitmapFont f) {
        for (int i = 0; i < count; i++) if (fonts[i] == f) return true;
        return false;
    }
}
