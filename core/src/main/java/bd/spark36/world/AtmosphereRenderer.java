package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.hud.FontRenderer;

/**
 * Advanced atmospheric renderer for 2x12: Spark.
 * Dynamic gradient sky, multi-layer animated clouds, bird silhouettes,
 * horizon haze, and floating in-world landmark labels.
 */
public class AtmosphereRenderer implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // ── Sky Colors (Golden Morning) ────────────────────────────────────
    private final Color skyZenith    = new Color(0.28f, 0.50f, 0.78f, 1f); // deep morning blue
    private final Color skyMidBlue   = new Color(0.52f, 0.70f, 0.88f, 1f); // mid blue
    private final Color skyHorizon   = new Color(0.90f, 0.80f, 0.62f, 1f); // warm dawn gold
    private final Color skyLowHaze   = new Color(0.96f, 0.88f, 0.74f, 1f); // lower haze band

    // ── Cloud layers ──────────────────────────────────────────────────
    // 3 layers: high thin, mid fluffy, low haze strips
    // Each cloud: {x, y, width, height, speed, alpha}
    private static final int CLOUDS_PER_LAYER = 5;
    private final float[][] cloudLayer1 = new float[CLOUDS_PER_LAYER][6]; // High cirrus
    private final float[][] cloudLayer2 = new float[CLOUDS_PER_LAYER][6]; // Mid cumulus
    private final float[][] cloudLayer3 = new float[CLOUDS_PER_LAYER][6]; // Low haze strips

    // ── Birds ─────────────────────────────────────────────────────────
    private static final int BIRD_COUNT = 5;
    private final float[] birdX = new float[BIRD_COUNT];
    private final float[] birdY = new float[BIRD_COUNT];
    private final float[] birdVX = new float[BIRD_COUNT];
    private final float[] birdVY = new float[BIRD_COUNT];
    private final float[] birdPhase = new float[BIRD_COUNT]; // wing flap phase
    private final float[] birdSize = new float[BIRD_COUNT];

    // ── Floating Landmark Labels ───────────────────────────────────────
    private final Vector3 curzonWorldPos   = new Vector3(0f, 42.0f, -22.5f);
    private final Vector3 libraryWorldPos  = new Vector3(28f, 16.0f, 10f);
    private final Vector3 tscWorldPos      = new Vector3(-56f, 12.0f, 8f);
    private final Vector3 screenCoords     = new Vector3();

    private float time = 0f;
    private int screenW, screenH;

    public AtmosphereRenderer(FontRenderer fonts) {
        this.fonts = fonts;
        screenW = Gdx.graphics.getBackBufferWidth();
        screenH = Gdx.graphics.getBackBufferHeight();
        initClouds();
        initBirds();
    }

    private void initClouds() {
        // Layer 1: High thin cirrus (fast, high up, narrow)
        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer1[i][0] = MathUtils.random() * 1.8f - 0.4f;  // x (0..1 + overflow)
            cloudLayer1[i][1] = 0.72f + MathUtils.random() * 0.18f; // y (upper sky)
            cloudLayer1[i][2] = 0.15f + MathUtils.random() * 0.25f; // width
            cloudLayer1[i][3] = 0.025f + MathUtils.random() * 0.015f; // height
            cloudLayer1[i][4] = 0.012f + MathUtils.random() * 0.008f; // speed (screen-widths/sec)
            cloudLayer1[i][5] = 0.18f + MathUtils.random() * 0.18f;  // alpha
        }
        // Layer 2: Mid cumulus (medium speed, mid height, wider)
        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer2[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer2[i][1] = 0.48f + MathUtils.random() * 0.22f;
            cloudLayer2[i][2] = 0.18f + MathUtils.random() * 0.32f;
            cloudLayer2[i][3] = 0.055f + MathUtils.random() * 0.04f;
            cloudLayer2[i][4] = 0.006f + MathUtils.random() * 0.005f;
            cloudLayer2[i][5] = 0.28f + MathUtils.random() * 0.22f;
        }
        // Layer 3: Low horizon haze strips (slow, near horizon)
        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer3[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer3[i][1] = 0.28f + MathUtils.random() * 0.12f;
            cloudLayer3[i][2] = 0.35f + MathUtils.random() * 0.55f;
            cloudLayer3[i][3] = 0.018f + MathUtils.random() * 0.015f;
            cloudLayer3[i][4] = 0.002f + MathUtils.random() * 0.003f;
            cloudLayer3[i][5] = 0.14f + MathUtils.random() * 0.12f;
        }
    }

    private void initBirds() {
        for (int i = 0; i < BIRD_COUNT; i++) {
            birdX[i] = MathUtils.random();
            birdY[i] = 0.50f + MathUtils.random() * 0.35f;
            birdVX[i] = (0.018f + MathUtils.random() * 0.022f) * (MathUtils.randomBoolean() ? 1f : -1f);
            birdVY[i] = (MathUtils.random() - 0.5f) * 0.004f;
            birdPhase[i] = MathUtils.random() * MathUtils.PI2;
            birdSize[i] = 3.5f + MathUtils.random() * 3f;
        }
    }

    private void updateClouds(float delta) {
        for (float[] c : cloudLayer1) c[0] += c[4] * delta;
        for (float[] c : cloudLayer2) c[0] += c[4] * delta;
        for (float[] c : cloudLayer3) c[0] += c[4] * delta;
        // Wrap around when off-screen right
        for (float[] c : cloudLayer1) if (c[0] > 1.4f) c[0] = -c[2] - 0.1f;
        for (float[] c : cloudLayer2) if (c[0] > 1.4f) c[0] = -c[2] - 0.1f;
        for (float[] c : cloudLayer3) if (c[0] > 1.5f) c[0] = -c[2] - 0.1f;
    }

    private void updateBirds(float delta) {
        for (int i = 0; i < BIRD_COUNT; i++) {
            birdX[i] += birdVX[i] * delta;
            birdY[i] += birdVY[i] * delta + MathUtils.sin(time * 0.8f + birdPhase[i]) * 0.0003f;
            // Wrap
            if (birdX[i] > 1.15f) birdX[i] = -0.1f;
            if (birdX[i] < -0.15f) birdX[i] = 1.1f;
            birdY[i] = MathUtils.clamp(birdY[i], 0.42f, 0.88f);
        }
    }

    public void renderSkyBackground(int w, int h) {
        screenW = w;
        screenH = h;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeType.Filled);

        // ── 4-band gradient sky ──────────────────────────────────────────
        // Band 1: Lower haze (horizon-to-20%)
        shapeRenderer.rect(0, 0, w, h * 0.20f, skyLowHaze, skyLowHaze, skyHorizon, skyHorizon);
        // Band 2: Horizon (20%-45%)
        shapeRenderer.rect(0, h * 0.20f, w, h * 0.25f, skyHorizon, skyHorizon, skyMidBlue, skyMidBlue);
        // Band 3: Mid sky (45%-75%)
        shapeRenderer.rect(0, h * 0.45f, w, h * 0.30f, skyMidBlue, skyMidBlue, skyZenith, skyZenith);
        // Band 4: Zenith (75%-100%)
        shapeRenderer.rect(0, h * 0.75f, w, h * 0.25f, skyZenith, skyZenith, skyZenith, skyZenith);

        // ── Warm horizon sun-glow haze ────────────────────────────────
        float pulse = 0.06f + 0.018f * MathUtils.sin(time * 0.5f);
        shapeRenderer.setColor(0.98f, 0.88f, 0.62f, pulse);
        shapeRenderer.rect(0, h * 0.22f, w, h * 0.14f);

        // ── Cloud Layer 3: Low haze strips (rendered first = farthest) ──
        for (float[] c : cloudLayer3) {
            shapeRenderer.setColor(0.98f, 0.95f, 0.88f, c[5]);
            float cx = c[0] * w;
            float cy = c[1] * h;
            float cw = c[2] * w;
            float ch = c[3] * h;
            // Soft elongated ellipse approximation (multiple overlapping rects)
            for (int s = 0; s < 4; s++) {
                float sf = 1f - s * 0.18f;
                float ay = s * ch * 0.20f;
                shapeRenderer.setColor(0.98f, 0.95f, 0.88f, c[5] * sf);
                shapeRenderer.rect(cx - cw * sf / 2f, cy - ch / 2f + ay, cw * sf, ch * 0.6f);
            }
        }

        // ── Cloud Layer 2: Mid cumulus ────────────────────────────────
        for (float[] c : cloudLayer2) {
            float cx = c[0] * w;
            float cy = c[1] * h;
            float cw = c[2] * w;
            float ch = c[3] * h;
            // Fluffy puffball: stacked circles
            for (int s = 0; s < 5; s++) {
                float sf = 0.55f + s * 0.10f;
                float ox = (s - 2f) * cw * 0.20f;
                float oy = -s * ch * 0.15f;
                shapeRenderer.setColor(0.97f, 0.96f, 0.94f, c[5] * sf * 0.9f);
                shapeRenderer.circle(cx + ox, cy + oy, ch * (0.7f + s * 0.12f), 10);
            }
            // Base fill
            shapeRenderer.setColor(0.97f, 0.96f, 0.94f, c[5] * 0.70f);
            shapeRenderer.rect(cx - cw / 2f, cy - ch * 0.5f, cw, ch * 0.55f);
        }

        // ── Cloud Layer 1: High thin cirrus ───────────────────────────
        for (float[] c : cloudLayer1) {
            float cx = c[0] * w;
            float cy = c[1] * h;
            float cw = c[2] * w;
            float ch = c[3] * h;
            shapeRenderer.setColor(0.95f, 0.96f, 1.00f, c[5] * 0.7f);
            // Thin horizontal wisp
            shapeRenderer.rect(cx, cy - ch / 2f, cw, ch);
            shapeRenderer.setColor(0.95f, 0.96f, 1.00f, c[5] * 0.35f);
            shapeRenderer.rect(cx - cw * 0.2f, cy - ch * 0.8f, cw * 1.4f, ch * 0.4f);
        }

        // ── Bird silhouettes ─────────────────────────────────────────
        shapeRenderer.setColor(0.12f, 0.10f, 0.08f, 0.55f);
        for (int i = 0; i < BIRD_COUNT; i++) {
            float bx = birdX[i] * w;
            float by = birdY[i] * h;
            float bs = birdSize[i];
            // Wing flap: sine-based wing angle
            float flapAngle = MathUtils.sin(time * 5f + birdPhase[i]) * 0.5f;
            // Left wing
            float lwX = bx - bs * 2.2f;
            float lwY = by + MathUtils.sin(flapAngle) * bs * 0.8f;
            // Right wing
            float rwX = bx + bs * 2.2f;
            float rwY = by + MathUtils.sin(flapAngle) * bs * 0.8f;
            // Body center
            shapeRenderer.triangle(bx - bs * 0.4f, by, bx + bs * 0.4f, by, bx, by - bs * 0.3f);
            // Wings (two triangles)
            shapeRenderer.triangle(bx - bs * 0.4f, by, lwX, lwY, bx, by);
            shapeRenderer.triangle(bx + bs * 0.4f, by, rwX, rwY, bx, by);
        }

        shapeRenderer.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    public void renderAtmosphereOverlays(Camera camera, float delta, int w, int h) {
        screenW = w;
        screenH = h;
        time += delta;
        updateClouds(delta);
        updateBirds(delta);

        // Floating In-World Landmark Labels
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderFloatingLabel(camera, curzonWorldPos, "Curzon Hall", w, h);
        renderFloatingLabel(camera, libraryWorldPos, "Central Library", w, h);
        renderFloatingLabel(camera, tscWorldPos, "TSC", w, h);
    }

    private void renderFloatingLabel(Camera camera, Vector3 worldPos, String text, int w, int h) {
        screenCoords.set(worldPos);
        camera.project(screenCoords);
        if (screenCoords.z > 0 && screenCoords.z < 1.0f) {
            float lx = screenCoords.x;
            float ly = screenCoords.y;
            if (lx >= 50 && lx <= w - 50 && ly >= 50 && ly <= h - 50) {
                // Distance fade: closer = more visible
                float depthFade = (float) Math.pow(1f - screenCoords.z, 2.0);
                GlyphLayout layout = new GlyphLayout(fonts.smallFont, text);
                float pw = layout.width + 28f;
                float ph = 24f;

                spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
                spriteBatch.begin();
                // Backdrop pill
                // (drawn separately via shapeRenderer before spriteBatch)
                spriteBatch.end();

                shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.setColor(0.05f, 0.06f, 0.08f, 0.72f * depthFade);
                shapeRenderer.rect(lx - pw / 2f, ly - ph / 2f, pw, ph);
                shapeRenderer.end();

                spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
                spriteBatch.begin();
                fonts.smallFont.setColor(1f, 0.90f, 0.65f, depthFade * 0.90f);
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
