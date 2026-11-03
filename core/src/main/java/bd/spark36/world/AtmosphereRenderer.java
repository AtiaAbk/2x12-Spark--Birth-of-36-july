// ei file ta akasher shundor daylight gradient ar megh render kore
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

public class AtmosphereRenderer implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    private final Color skyZenith    = new Color(0.085f, 0.172f, 0.504f, 1f);
    private final Color skyMidBlue   = new Color(0.141f, 0.254f, 0.652f, 1f);
    private final Color skyHorizon   = new Color(0.242f, 0.375f, 0.658f, 1f);
    private final Color skyLowHaze   = new Color(0.350f, 0.473f, 0.661f, 1f);

    private static final int CLOUDS_PER_LAYER = 5;
    private final float[][] cloudLayer1 = new float[CLOUDS_PER_LAYER][6];
    private final float[][] cloudLayer2 = new float[CLOUDS_PER_LAYER][6];
    private final float[][] cloudLayer3 = new float[CLOUDS_PER_LAYER][6];

    private static final int BIRD_COUNT = 7;
    private final float[] birdX = new float[BIRD_COUNT];
    private final float[] birdY = new float[BIRD_COUNT];
    private final float[] birdVX = new float[BIRD_COUNT];
    private final float[] birdVY = new float[BIRD_COUNT];
    private final float[] birdPhase = new float[BIRD_COUNT];
    private final float[] birdSize = new float[BIRD_COUNT];
    private final float[] birdFlapTimer = new float[BIRD_COUNT];
    private final float[] birdGlideTimer = new float[BIRD_COUNT];
    private final float[] birdThermalAngle = new float[BIRD_COUNT];

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

        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer1[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer1[i][1] = 0.72f + MathUtils.random() * 0.18f;
            cloudLayer1[i][2] = 0.15f + MathUtils.random() * 0.25f;
            cloudLayer1[i][3] = 0.025f + MathUtils.random() * 0.015f;
            cloudLayer1[i][4] = 0.012f + MathUtils.random() * 0.008f;
            cloudLayer1[i][5] = 0.18f + MathUtils.random() * 0.18f;
        }

        for (int i = 0; i < CLOUDS_PER_LAYER; i++) {
            cloudLayer2[i][0] = MathUtils.random() * 1.8f - 0.4f;
            cloudLayer2[i][1] = 0.48f + MathUtils.random() * 0.22f;
            cloudLayer2[i][2] = 0.18f + MathUtils.random() * 0.32f;
            cloudLayer2[i][3] = 0.055f + MathUtils.random() * 0.04f;
            cloudLayer2[i][4] = 0.006f + MathUtils.random() * 0.005f;
            cloudLayer2[i][5] = 0.28f + MathUtils.random() * 0.22f;
        }

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
            birdY[i] = 0.52f + MathUtils.random() * 0.32f;
            float baseSpeed = 0.012f + MathUtils.random() * 0.018f;
            birdVX[i] = baseSpeed * (MathUtils.randomBoolean() ? 1f : -1f);
            birdVY[i] = (MathUtils.random() - 0.5f) * 0.003f;
            birdPhase[i] = MathUtils.random() * MathUtils.PI2;
            birdSize[i] = 2.8f + MathUtils.random() * 3.2f;
            birdFlapTimer[i] = MathUtils.random(0.5f, 1.8f);
            birdGlideTimer[i] = MathUtils.random(3.0f, 6.5f);
            birdThermalAngle[i] = MathUtils.random(MathUtils.PI2);
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
            birdThermalAngle[i] += delta * 0.45f;
            float thermalLift = MathUtils.sin(birdThermalAngle[i]) * 0.0018f * delta;

            if (birdFlapTimer[i] > 0f) {
                birdFlapTimer[i] -= delta;
                if (birdFlapTimer[i] <= 0f) {
                    birdGlideTimer[i] = 3.5f + MathUtils.random() * 4.0f;
                }
            } else if (birdGlideTimer[i] > 0f) {
                birdGlideTimer[i] -= delta;
                if (birdGlideTimer[i] <= 0f) {
                    birdFlapTimer[i] = 1.0f + MathUtils.random() * 1.5f;
                }
            }

            birdX[i] += birdVX[i] * delta;
            birdY[i] += (birdVY[i] * delta) + thermalLift;

            if (birdVX[i] > 0 && birdX[i] > 1.15f) {
                birdX[i] = -0.1f;
                birdY[i] = 0.52f + MathUtils.random() * 0.32f;
            } else if (birdVX[i] < 0 && birdX[i] < -0.15f) {
                birdX[i] = 1.1f;
                birdY[i] = 0.52f + MathUtils.random() * 0.32f;
            }
            birdY[i] = MathUtils.clamp(birdY[i], 0.45f, 0.88f);
        }
    }

    private final Vector3 tmpSunProj = new Vector3();
    private final Vector3 tmpSunDir = new Vector3();

    public void renderSkyBackground(int w, int h) {
        renderSkyBackground(w, h, null, null);
    }

    public void renderSkyBackground(int w, int h, Camera camera) {
        renderSkyBackground(w, h, camera, null);
    }

    public void renderSkyBackground(int w, int h, Camera camera, WeatherController weather) {
        screenW = w;
        screenH = h;

        Color zColor = (weather != null) ? weather.getCurrentSkyZenith() : skyZenith;
        Color mColor = (weather != null) ? weather.getCurrentSkyMidBlue() : skyMidBlue;
        Color hColor = (weather != null) ? weather.getCurrentSkyHorizon() : skyHorizon;
        Color lColor = (weather != null) ? weather.getCurrentSkyLowHaze() : skyLowHaze;

        float cloudAlpha = (weather != null) ? weather.getCurrentCloudAlphaFactor() : 1.0f;
        float rainIntensity = (weather != null) ? weather.getCurrentRainIntensity() : 0.0f;

        float pitch = (camera != null) ? camera.direction.y : 0f;
        float pitchOffset = -pitch * h * 0.50f;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeType.Filled);

        float yBase = pitchOffset;
        float y0 = 0f;
        float y1 = MathUtils.clamp(yBase + h * 0.22f, 0f, h);
        float y2 = MathUtils.clamp(yBase + h * 0.48f, y1, h);
        float y3 = MathUtils.clamp(yBase + h * 0.78f, y2, h);
        float y4 = h;

        if (y1 > y0) {
            shapeRenderer.rect(0, y0, w, y1 - y0, lColor, lColor, hColor, hColor);
        }

        if (y2 > y1) {
            shapeRenderer.rect(0, y1, w, y2 - y1, hColor, hColor, mColor, mColor);
        }

        if (y3 > y2) {
            shapeRenderer.rect(0, y2, w, y3 - y2, mColor, mColor, zColor, zColor);
        }

        if (y4 > y3) {
            shapeRenderer.rect(0, y3, w, y4 - y3, zColor, zColor, zColor, zColor);
        }

        if (yBase > 0f) {
            shapeRenderer.rect(0, 0, w, Math.min(h, yBase), lColor, lColor, lColor, lColor);
        }

        float pulse = 0.04f + 0.012f * MathUtils.sin(time * 0.5f);
        float horizonGlowY = MathUtils.clamp(yBase + h * 0.22f, 0f, h - h * 0.14f);
        shapeRenderer.setColor(hColor.r, hColor.g, hColor.b, pulse);
        shapeRenderer.rect(0, horizonGlowY, w, h * 0.14f);

        for (float[] c : cloudLayer2) {
            float cx = c[0] * w;
            float cy = (0.52f + c[1] * 0.35f) * h + pitchOffset * 0.55f;
            float cw = c[2] * w * 1.5f;
            float ch = c[3] * h * 0.85f;
            float calpha = Math.min(1.0f, c[5] * 0.10f * cloudAlpha);
            if (calpha <= 0.005f) continue;
            for (int s = 0; s < 3; s++) {
                float ox = (s - 1.0f) * cw * 0.28f;
                float oy = MathUtils.sin(s * 1.4f) * ch * 0.20f;
                float ew = cw * (0.75f + s * 0.12f);
                float eh = ch * (0.65f + s * 0.10f);
                shapeRenderer.setColor(0.96f, 0.97f, 1.0f, calpha);
                shapeRenderer.ellipse(cx + ox - ew * 0.5f, cy + oy - eh * 0.5f, ew, eh, 32);
            }
        }

        for (float[] c : cloudLayer1) {
            float cx = c[0] * w;
            float cy = c[1] * h + pitchOffset * 0.70f;
            float cw = c[2] * w;
            float ch = c[3] * h * 1.6f;
            float calpha = Math.min(1.0f, c[5] * 0.16f * cloudAlpha);
            if (calpha <= 0.005f) continue;
            shapeRenderer.setColor(0.95f, 0.97f, 1.00f, calpha);
            shapeRenderer.ellipse(cx - cw * 0.5f, cy - ch * 0.5f, cw, ch, 28);
        }

        float birdAlpha = Math.max(0.0f, 1.0f - rainIntensity * 1.6f);
        if (birdAlpha > 0.05f) {
            shapeRenderer.setColor(0.14f, 0.12f, 0.10f, 0.65f * birdAlpha);
            for (int i = 0; i < BIRD_COUNT; i++) {
                float bx = birdX[i] * w;
                float by = birdY[i] * h + pitchOffset * 0.40f;
                if (by < 0 || by > h) continue;
                float bs = birdSize[i];
                float dir = birdVX[i] >= 0 ? 1f : -1f;

                boolean isFlapping = birdFlapTimer[i] > 0f;
                float flapAngle;
                if (isFlapping) {
                    flapAngle = MathUtils.sin(time * 8.5f + birdPhase[i]) * 0.55f;
                } else {
                    flapAngle = MathUtils.sin(birdThermalAngle[i]) * 0.12f;
                }

                float headX = bx + dir * bs * 0.85f;
                float tailX = bx - dir * bs * 0.95f;
                float bodyY = by;

                float elbowOffset = bs * 1.35f;
                float wingSpan = bs * 2.8f;
                float flapY = MathUtils.sin(flapAngle) * bs * 1.1f;

                float lElbowX = bx + dir * bs * 0.25f;
                float lElbowY = bodyY + elbowOffset * 0.5f + flapY * 0.45f;
                float lTipX = bx - dir * bs * 0.45f;
                float lTipY = bodyY + wingSpan * 0.5f + flapY;

                float rElbowX = bx + dir * bs * 0.25f;
                float rElbowY = bodyY - elbowOffset * 0.5f - flapY * 0.45f;
                float rTipX = bx - dir * bs * 0.45f;
                float rTipY = bodyY - wingSpan * 0.5f - flapY;

                shapeRenderer.triangle(headX, bodyY, tailX, bodyY + bs * 0.25f, tailX, bodyY - bs * 0.25f);

                shapeRenderer.triangle(bx, bodyY, lElbowX, lElbowY, lTipX, lTipY);

                shapeRenderer.triangle(bx, bodyY, rElbowX, rElbowY, rTipX, rTipY);
            }
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
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
