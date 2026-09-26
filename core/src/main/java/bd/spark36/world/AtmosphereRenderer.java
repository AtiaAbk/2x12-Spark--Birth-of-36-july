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
 * Features:
 * - Full Vibrant Tropical Summer sky (radiant azure zenith, crisp cyan mid-sky, bright airy horizon)
 * - Billowing summer cumulus clouds with multi-layer parallax
 * - Physically authentic Sun Crepuscular Rays radiating strictly from the true sky sun position
 * - Bird flock silhouettes soaring across the sky
 * - In-world floating landmark pins for Curzon Hall, Library, and TSC.
 */
public class AtmosphereRenderer implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // ── Sky Colors (Vibrant Tropical Summer July Sky) ─────────────────────────
    private final Color skyZenith    = new Color(0.12f, 0.48f, 0.88f, 1f); // Radiant deep summer azure
    private final Color skyMidBlue   = new Color(0.38f, 0.70f, 0.94f, 1f); // Bright tropical cyan
    private final Color skyHorizon   = new Color(0.82f, 0.90f, 0.98f, 1f); // Clean airy summer horizon
    private final Color skyLowHaze   = new Color(0.90f, 0.94f, 1.00f, 1f); // Crisp daylight haze

    // ── Cloud layers (Summer Cumulus & Cirrus) ────────────────────────────────
    private static final int CLOUDS_PER_LAYER = 6;
    private final float[][] cloudLayer1 = new float[CLOUDS_PER_LAYER][6]; // High thin cirrus
    private final float[][] cloudLayer2 = new float[CLOUDS_PER_LAYER][6]; // Billowing cumulus puffballs
    private final float[][] cloudLayer3 = new float[CLOUDS_PER_LAYER][6]; // Low horizon haze banks

    // ── Birds ────────────────────────────────────────────────────────────────
    private static final int BIRD_COUNT = 6;
    private final float[] birdX = new float[BIRD_COUNT];
    private final float[] birdY = new float[BIRD_COUNT];
    private final float[] birdVX = new float[BIRD_COUNT];
    private final float[] birdVY = new float[BIRD_COUNT];
    private final float[] birdPhase = new float[BIRD_COUNT];
    private final float[] birdSize = new float[BIRD_COUNT];

    // ── Sun Crepuscular Rays Physics ──────────────────────────────────────────
    // Sun light direction in DhakaCampusWorld is (-0.55, -0.60, -0.58).
    // Therefore the Sun body sits in the sky at (+0.55, +0.60, +0.58).
    private final Vector3 sunSkyDir = new Vector3(0.55f, 0.60f, 0.58f).nor();
    private final Vector3 sunWorldPos = new Vector3(sunSkyDir).scl(160f);
    private final Vector3 sunScreenPos = new Vector3();

    // ── Floating Landmark Labels ─────────────────────────────────────────────
    private final Vector3 curzonWorldPos   = new Vector3(0f, 42.0f, -22.5f);
    private final Vector3 libraryWorldPos  = new Vector3(28f, 16.0f, 10f);
    private final Vector3 tscWorldPos      = new Vector3(-56f, 12.0f, 8f);
    private final Vector3 screenCoords     = new Vector3();

    private float time = 0f;
    private int screenW, screenH;

    public AtmosphereRenderer(FontRenderer fonts) {
        this.fonts = fonts;
        initClouds();
        initBirds();
    }

    private void initClouds() {
        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer1[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer1[i][1] = 0.72f + MathUtils.random() * 0.20f;
            cloudLayer1[i][2] = 0.18f + MathUtils.random() * 0.28f;
            cloudLayer1[i][3] = 0.025f + MathUtils.random() * 0.015f;
            cloudLayer1[i][4] = 0.010f + MathUtils.random() * 0.008f;
            cloudLayer1[i][5] = 0.22f + MathUtils.random() * 0.18f;
        }
        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer2[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer2[i][1] = 0.46f + MathUtils.random() * 0.24f;
            cloudLayer2[i][2] = 0.22f + MathUtils.random() * 0.35f;
            cloudLayer2[i][3] = 0.065f + MathUtils.random() * 0.045f;
            cloudLayer2[i][4] = 0.007f + MathUtils.random() * 0.005f;
            cloudLayer2[i][5] = 0.35f + MathUtils.random() * 0.25f; // bright white cumulus
        }
        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer3[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer3[i][1] = 0.26f + MathUtils.random() * 0.14f;
            cloudLayer3[i][2] = 0.35f + MathUtils.random() * 0.55f;
            cloudLayer3[i][3] = 0.020f + MathUtils.random() * 0.015f;
            cloudLayer3[i][4] = 0.003f + MathUtils.random() * 0.003f;
            cloudLayer3[i][5] = 0.16f + MathUtils.random() * 0.12f;
        }
    }

    private void initBirds() {
        for (int i = 0; i < BIRD_COUNT; i++) {
            birdX[i] = MathUtils.random();
            birdY[i] = 0.52f + MathUtils.random() * 0.35f;
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
        for (float[] c : cloudLayer1) if (c[0] > 1.4f) c[0] = -c[2] - 0.1f;
        for (float[] c : cloudLayer2) if (c[0] > 1.4f) c[0] = -c[2] - 0.1f;
        for (float[] c : cloudLayer3) if (c[0] > 1.5f) c[0] = -c[2] - 0.1f;
    }

    private void updateBirds(float delta) {
        for (int i = 0; i < BIRD_COUNT; i++) {
            birdX[i] += birdVX[i] * delta;
            birdY[i] += birdVY[i] * delta + MathUtils.sin(time * 0.8f + birdPhase[i]) * 0.0003f;
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

        // ── 4-Band Vibrant Tropical Summer Sky ────────────────────────────────
        // Band 1: Lower clean haze
        shapeRenderer.rect(0, 0, w, h * 0.18f, skyLowHaze, skyLowHaze, skyHorizon, skyHorizon);
        // Band 2: Bright summer horizon
        shapeRenderer.rect(0, h * 0.18f, w, h * 0.27f, skyHorizon, skyHorizon, skyMidBlue, skyMidBlue);
        // Band 3: Vibrant mid-sky
        shapeRenderer.rect(0, h * 0.45f, w, h * 0.32f, skyMidBlue, skyMidBlue, skyZenith, skyZenith);
        // Band 4: Deep radiant zenith
        shapeRenderer.rect(0, h * 0.77f, w, h * 0.23f, skyZenith, skyZenith, skyZenith, skyZenith);

        // ── Warm summer horizon glow ─────────────────────────────────────────
        float pulse = 0.04f + 0.015f * MathUtils.sin(time * 0.5f);
        shapeRenderer.setColor(1.0f, 0.92f, 0.75f, pulse);
        shapeRenderer.rect(0, h * 0.20f, w, h * 0.14f);

        // ── Cloud Layer 3: Low haze strips ────────────────────────────────────
        for (float[] c : cloudLayer3) {
            shapeRenderer.setColor(0.98f, 0.98f, 0.96f, c[5]);
            float cx = c[0] * w, cy = c[1] * h, cw = c[2] * w, ch = c[3] * h;
            for (int s = 0; s < 5; s++) {
                float sf = 1f - s * 0.16f;
                shapeRenderer.setColor(0.98f, 0.98f, 0.96f, c[5] * 0.35f);
                shapeRenderer.ellipse(cx - cw * sf / 2f, cy - ch * sf / 2f, cw * sf, ch * sf, 24);
            }
        }

        // ── Cloud Layer 2: Billowing Summer Cumulus ───────────────────────────
        for (float[] c : cloudLayer2) {
            float cx = c[0] * w, cy = c[1] * h, cw = c[2] * w, ch = c[3] * h;
            for (int s = 0; s < 5; s++) {
                float sf = 0.55f + s * 0.10f;
                float ox = (s - 2f) * cw * 0.20f;
                float oy = -s * ch * 0.15f;
                shapeRenderer.setColor(1.0f, 1.0f, 0.98f, c[5] * sf * 0.95f);
                shapeRenderer.circle(cx + ox, cy + oy, ch * (0.7f + s * 0.12f), 36);
            }
            shapeRenderer.setColor(0.98f, 0.98f, 0.96f, c[5] * 0.75f);
            shapeRenderer.rect(cx - cw / 2f, cy - ch * 0.5f, cw, ch * 0.55f);
        }

        // ── Cloud Layer 1: High thin cirrus ───────────────────────────────────
        for (float[] c : cloudLayer1) {
            float cx = c[0] * w, cy = c[1] * h, cw = c[2] * w, ch = c[3] * h;
            shapeRenderer.setColor(0.96f, 0.98f, 1.00f, c[5] * 0.30f);
            shapeRenderer.ellipse(cx - cw * 0.1f, cy - ch * 0.9f, cw * 1.2f, ch * 1.8f, 24);
            shapeRenderer.setColor(0.96f, 0.98f, 1.00f, c[5] * 0.55f);
            shapeRenderer.ellipse(cx, cy - ch / 2f, cw, ch, 24);
        }

        // ── Bird silhouettes soaring in sky ───────────────────────────────────
        shapeRenderer.setColor(0.12f, 0.10f, 0.08f, 0.50f);
        for (int i = 0; i < BIRD_COUNT; i++) {
            float bx = birdX[i] * w, by = birdY[i] * h, bs = birdSize[i];
            float flapAngle = MathUtils.sin(time * 5f + birdPhase[i]) * 0.5f;
            float lwX = bx - bs * 2.2f, lwY = by + MathUtils.sin(flapAngle) * bs * 0.8f;
            float rwX = bx + bs * 2.2f, rwY = by + MathUtils.sin(flapAngle) * bs * 0.8f;
            shapeRenderer.triangle(bx - bs * 0.4f, by, bx + bs * 0.4f, by, bx, by - bs * 0.3f);
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

        // 1. Physically Authentic Sun Crepuscular Rays
        renderSkySunRays(camera, w, h);

        // 2. Floating In-World Landmark Labels
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderFloatingLabel(camera, curzonWorldPos, "Curzon Hall", w, h);
        renderFloatingLabel(camera, libraryWorldPos, "Central Library", w, h);
        renderFloatingLabel(camera, tscWorldPos, "TSC", w, h);
    }

    /**
     * Renders physically authentic crepuscular rays radiating strictly from
     * the projected sun position in the sky, completely eliminating unrealistic
     * rays shooting out of building walls or ground.
     */
    private void renderSkySunRays(Camera camera, int w, int h) {
        // Project physical sun location into camera screen coordinates
        sunScreenPos.set(sunWorldPos);
        camera.project(sunScreenPos);

        // Check if sun is in front of camera
        float viewDot = camera.direction.dot(sunSkyDir);
        if (viewDot <= 0.15f || sunScreenPos.z <= 0f || sunScreenPos.z >= 1.0f) {
            return; // Looking away from the sun; no godrays
        }

        float sunX = sunScreenPos.x;
        float sunY = sunScreenPos.y;

        // Fade intensity based on how centrally the camera looks towards the sun
        float sunAlignment = MathUtils.clamp((viewDot - 0.15f) / 0.70f, 0f, 1f);
        float baseOpacity = 0.16f * sunAlignment;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // Additive bloom blending

        shapeRenderer.begin(ShapeType.Filled);

        // Sun Disk & Golden Corona Glow
        float coronaPulse = 1.0f + 0.08f * MathUtils.sin(time * 1.5f);
        shapeRenderer.setColor(1.0f, 0.95f, 0.80f, 0.22f * sunAlignment);
        shapeRenderer.circle(sunX, sunY, 70f * coronaPulse, 32);
        shapeRenderer.setColor(1.0f, 0.98f, 0.90f, 0.45f * sunAlignment);
        shapeRenderer.circle(sunX, sunY, 32f, 24);

        // Crepuscular Rays radiating OUTWARD from Sun Disk across the sky & canopy
        int rayCount = 10;
        float maxRayLen = Math.max(w, h) * 1.2f;

        for (int i = 0; i < rayCount; i++) {
            float rayAngle = (float) i / rayCount * MathUtils.PI2 + MathUtils.sin(time * 0.2f + i) * 0.08f;
            // Downward and lateral spread towards campus
            float raySweep = 0.08f + 0.04f * MathUtils.sin(time * 0.5f + i * 2f);

            float cosA = MathUtils.cos(rayAngle);
            float sinA = MathUtils.sin(rayAngle);
            float cosB = MathUtils.cos(rayAngle + raySweep);
            float sinB = MathUtils.sin(rayAngle + raySweep);

            float pulseRay = baseOpacity * (0.6f + 0.4f * MathUtils.sin(time * 1.2f + i * 1.7f));
            Color rayCol = new Color(1.0f, 0.94f, 0.82f, pulseRay);
            Color fadeCol = new Color(1.0f, 0.90f, 0.75f, 0f);

            // Tapered volumetric ray trapezoid: origin at sun, fanning wide outwards
            float nearR = 25f;
            float farR = maxRayLen;

            shapeRenderer.triangle(
                sunX + cosA * nearR, sunY + sinA * nearR,
                sunX + cosA * farR, sunY + sinA * farR,
                sunX + cosB * farR, sunY + sinB * farR,
                rayCol, fadeCol, fadeCol
            );
        }

        shapeRenderer.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private void renderFloatingLabel(Camera camera, Vector3 worldPos, String text, int w, int h) {
        screenCoords.set(worldPos);
        camera.project(screenCoords);
        if (screenCoords.z > 0 && screenCoords.z < 1.0f) {
            float lx = screenCoords.x;
            float ly = screenCoords.y;
            if (lx >= 50 && lx <= w - 50 && ly >= 50 && ly <= h - 50) {
                float depthFade = (float) Math.pow(1f - screenCoords.z, 2.0);
                GlyphLayout layout = new GlyphLayout(fonts.smallFont, text);
                float pw = layout.width + 28f;
                float ph = 24f;

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
