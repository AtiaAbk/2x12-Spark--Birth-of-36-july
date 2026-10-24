package bd.spark36.capture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

/**
 * Collects mouse-wheel movement while photo mode or the gallery is open. libGDX only reports the
 * wheel through an InputProcessor, so this one is installed in front of whatever processor the
 * game had (none today) and the original is put back on {@link #uninstall()}.
 */
final class ScrollInput extends InputAdapter {

    private InputProcessor previous;
    private InputProcessor installed;
    private float pendingY;

    void install() {
        if (installed != null || Gdx.input == null) return;
        previous = Gdx.input.getInputProcessor();
        installed = previous == null ? this : new InputMultiplexer(this, previous);
        Gdx.input.setInputProcessor(installed);
        pendingY = 0f;
    }

    void uninstall() {
        if (installed == null || Gdx.input == null) return;
        if (Gdx.input.getInputProcessor() == installed) Gdx.input.setInputProcessor(previous);
        installed = null;
        previous = null;
        pendingY = 0f;
    }

    /** Wheel notches since the last call; positive means scrolled down / towards the player. */
    float consumeScrollY() {
        float v = pendingY;
        pendingY = 0f;
        return v;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        pendingY += amountY;
        return false;
    }
}
