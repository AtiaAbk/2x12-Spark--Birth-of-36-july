package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.hud.FontRenderer;

/**
 * Atmospheric background and in-world landmark labels.
 * Clean, serene dawn sky gradient without harsh sun disc or blinding god rays.
 */
public class AtmosphereRenderer implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // Atmospheric Dawn Sky Colors
    private final Color skyZenith = new Color(0.40f, 0.60f, 0.82f, 1f); // Morning blue
    private final Color skyHorizon = new Color(0.96f, 0.88f, 0.72f, 1f); // Warm dawn gold

    // Floating landmarks
    private final Vector3 curzonWorldPos = new Vector3(0f, 42.0f, -22.5f);
    private final Vector3 libraryWorldPos = new Vector3(28f, 16.0f, 10f);
    private final Vector3 screenCoords = new Vector3();

    public AtmosphereRenderer(FontRenderer fonts) {
        this.fonts = fonts;
    }

    /**
     * Renders the clear dawn sky gradient background before the 3D pass.
     * Sunlight disc/corona removed for clean, glare-free visibility.
     */
    public void renderSkyBackground(int screenW, int screenH) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Clean Vertical Dawn Gradient
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.rect(0, 0, screenW, screenH, skyHorizon, skyHorizon, skyZenith, skyZenith);
        shapeRenderer.end();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    /**
     * Renders 3D floating landmark labels ("Curzon Hall", "Library") after 3D world pass.
     * God ray overlays removed for clear, unobstructed campus view.
     */
    public void renderAtmosphereOverlays(Camera camera, float delta, int screenW, int screenH) {
        // Floating In-World Landmark Labels
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderFloatingLabel(camera, curzonWorldPos, "Curzon Hall", screenW, screenH);
        renderFloatingLabel(camera, libraryWorldPos, "Library", screenW, screenH);
    }

    private void renderFloatingLabel(Camera camera, Vector3 worldPos, String text, int screenW, int screenH) {
        screenCoords.set(worldPos);
        camera.project(screenCoords);

        // Only draw if in front of camera
        if (screenCoords.z > 0 && screenCoords.z < 1.0f) {
            float lx = screenCoords.x;
            float ly = screenCoords.y;

            if (lx >= 50 && lx <= screenW - 50 && ly >= 50 && ly <= screenH - 50) {
                GlyphLayout layout = new GlyphLayout(fonts.smallFont, text);
                float pw = layout.width + 28f;
                float ph = 26f;

                // Dark pill background
                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.78f);
                shapeRenderer.rect(lx - pw / 2f, ly - ph / 2f, pw, ph);
                shapeRenderer.end();

                // Gold border
                shapeRenderer.begin(ShapeType.Line);
                shapeRenderer.setColor(0.92f, 0.78f, 0.35f, 0.85f);
                shapeRenderer.rect(lx - pw / 2f, ly - ph / 2f, pw, ph);
                shapeRenderer.end();

                // Text
                spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);
                spriteBatch.begin();
                fonts.smallFont.setColor(new Color(1f, 0.88f, 0.45f, 1f));
                fonts.smallFont.draw(spriteBatch, text, lx - layout.width / 2f, ly + layout.height / 2f);
                spriteBatch.end();
            }
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
