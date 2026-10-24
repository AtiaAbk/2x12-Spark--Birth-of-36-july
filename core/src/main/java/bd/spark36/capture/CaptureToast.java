package bd.spark36.capture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Short notification line for capture results; drawn after the HUD so it never appears in a capture. */
final class CaptureToast {

    private static final float DURATION = 3.0f;
    private String message;
    private boolean error;
    private float timer;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color oldColor = new Color();

    void show(String message, boolean error) {
        this.message = message;
        this.error = error;
        this.timer = DURATION;
        if (Gdx.app != null) Gdx.app.log("Capture", message);
    }

    void update(float delta) {
        if (timer > 0f) timer -= delta;
    }

    void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont font, float w, float h, float scale) {
        if (timer <= 0f || message == null) return;
        float alpha = Math.min(1f, timer / 0.4f);
        float oldX = font.getData().scaleX, oldY = font.getData().scaleY;
        oldColor.set(font.getColor());
        font.getData().setScale(oldX * scale, oldY * scale);
        font.setColor(1f, 1f, 1f, alpha); // GlyphLayout takes the colour at setText time
        layout.setText(font, message);
        float padX = 18f * scale, padY = 10f * scale;
        float bw = layout.width + padX * 2f, bh = layout.height + padY * 2f;
        float x = (w - bw) / 2f, y = h - bh - 28f * scale;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.05f, 0.07f, 0.10f, 0.85f * alpha);
        shapes.rect(x, y, bw, bh);
        if (error) shapes.setColor(0.90f, 0.35f, 0.30f, alpha);
        else shapes.setColor(0.92f, 0.76f, 0.32f, alpha);
        shapes.rect(x, y, 4f * scale, bh);
        shapes.end();

        batch.begin();
        font.draw(batch, layout, x + padX, y + padY + layout.height);
        batch.end();
        font.setColor(oldColor);
        font.getData().setScale(oldX, oldY);
    }
}
