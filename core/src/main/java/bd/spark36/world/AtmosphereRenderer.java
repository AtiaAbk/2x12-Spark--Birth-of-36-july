package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.hud.FontRenderer;

/**
 * Renders cinematic atmospheric lighting effects matching Where Winds Meet:
 * - Golden-hour dawn sky with radiant sun glow
 * - Volumetric crepuscular light shafts (God Rays) streaming through trees
 * - 3D world floating landmark labels ("Curzon Hall", "Library")
 */
public class AtmosphereRenderer implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // Atmospheric Colors
    private final Color skyZenith = new Color(0.40f, 0.60f, 0.82f, 1f); // Morning blue
    private final Color skyHorizon = new Color(0.96f, 0.88f, 0.72f, 1f); // Warm dawn gold
    private final Color sunGlow = new Color(1.0f, 0.94f, 0.80f, 0.45f);

    // Floating landmarks
    private final Vector3 curzonWorldPos = new Vector3(0f, 42.0f, -22.5f);
    private final Vector3 libraryWorldPos = new Vector3(28f, 16.0f, 10f);
    private final Vector3 screenCoords = new Vector3();

    private float animTime = 0f;

    public AtmosphereRenderer(FontRenderer fonts) {
        this.fonts = fonts;
    }

    /**
     * Renders the dawn sky background and radiant sun disc before the 3D pass.
     */
    public void renderSkyBackground(int screenW, int screenH) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 1. Vertical Dawn Gradient
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.rect(0, 0, screenW, screenH, skyHorizon, skyHorizon, skyZenith, skyZenith);

        // 2. Radiant Morning Sun Disc & Corona (upper right, matching mockup lighting)
        float sunX = screenW * 0.72f;
        float sunY = screenH * 0.82f;

        // Outer soft glow
        shapeRenderer.setColor(1.0f, 0.90f, 0.65f, 0.22f);
        shapeRenderer.circle(sunX, sunY, screenH * 0.35f, 48);

        // Mid glow
        shapeRenderer.setColor(1.0f, 0.93f, 0.75f, 0.40f);
        shapeRenderer.circle(sunX, sunY, screenH * 0.18f, 40);

        // Core sun disc
        shapeRenderer.setColor(1.0f, 0.98f, 0.90f, 0.85f);
        shapeRenderer.circle(sunX, sunY, screenH * 0.065f, 32);

        shapeRenderer.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    /**
     * Renders crepuscular god rays and 3D floating landmark labels after 3D world pass.
     */
    public void renderAtmosphereOverlays(Camera camera, float delta, int screenW, int screenH) {
        animTime += delta;

        // 1. Additive God Rays (Crepuscular light shafts streaming through tree branches)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // Additive blend for radiant light

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);
        shapeRenderer.begin(ShapeType.Filled);

        float sunX = screenW * 0.72f;
        float sunY = screenH * 0.95f;

        // Multiple diagonal fan rays
        float[] rayAngles = {-55f, -48f, -40f, -32f, -24f, -16f};
        float[] rayWidths = {65f, 95f, 130f, 110f, 85f, 60f};

        for (int i = 0; i < rayAngles.length; i++) {
            float shimmer = 0.85f + 0.15f * MathUtils.sin(animTime * 0.8f + i * 1.3f);
            float alpha = (0.045f + 0.015f * (i % 3)) * shimmer;

            shapeRenderer.setColor(1.0f, 0.92f, 0.70f, alpha);

            float rad = rayAngles[i] * MathUtils.degreesToRadians;
            float cos = MathUtils.cos(rad);
            float sin = MathUtils.sin(rad);

            float rayLen = screenH * 1.6f;
            float w0 = 15f;
            float w1 = rayWidths[i] * 2.2f;

            // Quad light shaft from sun down-left across campus
            float x1 = sunX + w0;
            float y1 = sunY;
            float x2 = sunX - w0;
            float y2 = sunY;
            float x3 = sunX + rayLen * cos - w1;
            float y3 = sunY + rayLen * sin;
            float x4 = sunX + rayLen * cos + w1;
            float y4 = sunY + rayLen * sin;

            shapeRenderer.triangle(x1, y1, x2, y2, x3, y3);
            shapeRenderer.triangle(x1, y1, x3, y3, x4, y4);
        }

        shapeRenderer.end();

        // 2. Floating In-World Landmark Labels ("Curzon Hall", "Library")
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
                com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(fonts.smallFont, text);
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
