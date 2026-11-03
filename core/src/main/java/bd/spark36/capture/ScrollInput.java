// ei file ta gallery screen e mouse scroll input process kore
package bd.spark36.capture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

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
