package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Disposable;

/**
 * Environmental particle system for 2x12: Spark.
 * Spawns falling leaves, dust motes, bokeh ambient, and pukur water ripples.
 * All particles are 2D screen-space (rendered after 3D pass, before HUD).
 */
public class ParticleSystem implements Disposable {

    // ─── Leaf Particle ────────────────────────────────────────────────
    private static final int MAX_LEAVES = 55;
    private final float[] leafX = new float[MAX_LEAVES];
    private final float[] leafY = new float[MAX_LEAVES];
    private final float[] leafVX = new float[MAX_LEAVES];
    private final float[] leafVY = new float[MAX_LEAVES];
    private final float[] leafAngle = new float[MAX_LEAVES];
    private final float[] leafAngVel = new float[MAX_LEAVES];
    private final float[] leafLife = new float[MAX_LEAVES];
    private final float[] leafMaxLife = new float[MAX_LEAVES];
    private final float[] leafSize = new float[MAX_LEAVES];
    private final float[] leafSway = new float[MAX_LEAVES];   // sway phase
    private final Color[] leafColor = new Color[MAX_LEAVES];

    // ─── Dust Mote ────────────────────────────────────────────────────
    private static final int MAX_DUST = 22;
    private final float[] dustX = new float[MAX_DUST];
    private final float[] dustY = new float[MAX_DUST];
    private final float[] dustVX = new float[MAX_DUST];
    private final float[] dustVY = new float[MAX_DUST];
    private final float[] dustLife = new float[MAX_DUST];
    private final float[] dustMaxLife = new float[MAX_DUST];
    private final float[] dustSize = new float[MAX_DUST];

    // ─── Water Ripples ────────────────────────────────────────────────
    private static final int MAX_RIPPLES = 8;
    private final float[] rippleX = new float[MAX_RIPPLES];
    private final float[] rippleY = new float[MAX_RIPPLES];
    private final float[] rippleRadius = new float[MAX_RIPPLES];
    private final float[] rippleLife = new float[MAX_RIPPLES];
    private final float[] rippleMaxLife = new float[MAX_RIPPLES];
    private float rippleTimer = 0f;
    private int rippleIdx = 0;

    // Pukur screen-space position (updated each frame from camera projection)
    private float pukurScreenX = -999f;
    private float pukurScreenY = -999f;
    private boolean pukurVisible = false;
    private final Vector3 pukurWorldPos = new Vector3(0f, 0.02f, 6f);
    private final Vector3 screenProj = new Vector3();

    private final ShapeRenderer sr;
    private float time = 0f;
    private int screenW, screenH;

    // Leaf color palette (autumn warm)
    private static final Color[] LEAF_COLORS = {
        new Color(0.72f, 0.42f, 0.12f, 1f), // amber
        new Color(0.82f, 0.56f, 0.08f, 1f), // gold
        new Color(0.55f, 0.32f, 0.10f, 1f), // brown
        new Color(0.25f, 0.52f, 0.18f, 1f), // dark green
        new Color(0.35f, 0.60f, 0.22f, 1f), // fresh green
        new Color(0.78f, 0.28f, 0.12f, 1f), // red-orange
    };

    public ParticleSystem() {
        sr = new ShapeRenderer();
        // Initialize all leaves as dead (life = 0)
        for (int i = 0; i < MAX_LEAVES; i++) {
            leafLife[i] = 0f;
            leafColor[i] = LEAF_COLORS[i % LEAF_COLORS.length].cpy();
        }
        for (int i = 0; i < MAX_DUST; i++) dustLife[i] = 0f;
        for (int i = 0; i < MAX_RIPPLES; i++) rippleLife[i] = 0f;
        screenW = Gdx.graphics.getBackBufferWidth();
        screenH = Gdx.graphics.getBackBufferHeight();
    }

    public void resize(int w, int h) {
        screenW = w;
        screenH = h;
    }

    private void spawnLeaf(int i) {
        // Spawn across upper 30% of screen, biased toward center (promenade)
        leafX[i] = screenW * (0.25f + MathUtils.random() * 0.55f);
        leafY[i] = screenH * (0.85f + MathUtils.random() * 0.18f);
        leafVX[i] = (MathUtils.random() - 0.5f) * 28f;  // gentle sideways drift
        leafVY[i] = -(22f + MathUtils.random() * 18f);   // fall downward
        leafAngle[i] = MathUtils.random() * 360f;
        leafAngVel[i] = (MathUtils.random() - 0.5f) * 80f;
        leafMaxLife[i] = 4.5f + MathUtils.random() * 3.5f;
        leafLife[i] = leafMaxLife[i];
        leafSize[i] = 5f + MathUtils.random() * 5f;
        leafSway[i] = MathUtils.random() * MathUtils.PI2;
        leafColor[i] = LEAF_COLORS[MathUtils.random(LEAF_COLORS.length - 1)].cpy();
    }

    private void spawnDust(int i) {
        // Dust in upper 60% of screen around Curzon Hall area
        dustX[i] = screenW * (0.20f + MathUtils.random() * 0.60f);
        dustY[i] = screenH * (0.35f + MathUtils.random() * 0.55f);
        dustVX[i] = (MathUtils.random() - 0.5f) * 8f;
        dustVY[i] = (MathUtils.random() - 0.3f) * 6f;
        dustMaxLife[i] = 5f + MathUtils.random() * 5f;
        dustLife[i] = dustMaxLife[i];
        dustSize[i] = 1.2f + MathUtils.random() * 2.0f;
    }

    public void update(float delta, Camera camera) {
        time += delta;
        screenW = Gdx.graphics.getBackBufferWidth();
        screenH = Gdx.graphics.getBackBufferHeight();

        // Project pukur world pos to screen
        screenProj.set(pukurWorldPos);
        camera.project(screenProj);
        pukurVisible = screenProj.z > 0f && screenProj.z < 1.0f
            && screenProj.x > 50 && screenProj.x < screenW - 50
            && screenProj.y > 50 && screenProj.y < screenH - 50;
        if (pukurVisible) {
            pukurScreenX = screenProj.x;
            pukurScreenY = screenProj.y;
        }

        // ── Leaves ──────────────────────────────────────────────────
        int deadLeaves = 0;
        for (int i = 0; i < MAX_LEAVES; i++) {
            if (leafLife[i] <= 0f) { deadLeaves++; continue; }
            leafLife[i] -= delta;
            // Sine sway in X
            float swayForce = MathUtils.sin(time * 1.8f + leafSway[i]) * 18f;
            leafX[i] += (leafVX[i] + swayForce) * delta;
            leafY[i] += leafVY[i] * delta;
            leafAngle[i] += leafAngVel[i] * delta;
            // Kill if below screen
            if (leafY[i] < -30) leafLife[i] = 0f;
        }
        // Spawn replacements
        if (deadLeaves > 0) {
            for (int i = 0; i < MAX_LEAVES; i++) {
                if (leafLife[i] <= 0f && MathUtils.random() < delta * 8f) {
                    spawnLeaf(i);
                    break;
                }
            }
        }
        // Seed initial population
        for (int i = 0; i < MAX_LEAVES; i++) {
            if (leafLife[i] <= 0f && MathUtils.random() < 0.015f) spawnLeaf(i);
        }

        // ── Dust Motes ──────────────────────────────────────────────
        for (int i = 0; i < MAX_DUST; i++) {
            if (dustLife[i] <= 0f) {
                if (MathUtils.random() < delta * 4f) spawnDust(i);
                continue;
            }
            dustLife[i] -= delta;
            dustX[i] += dustVX[i] * delta;
            dustY[i] += dustVY[i] * delta;
        }

        // ── Water Ripples ────────────────────────────────────────────
        if (pukurVisible) {
            rippleTimer -= delta;
            if (rippleTimer <= 0f) {
                rippleTimer = 0.8f + MathUtils.random() * 1.2f;
                // Spawn ripple near pukur center with random offset
                rippleX[rippleIdx] = pukurScreenX + (MathUtils.random() - 0.5f) * 60f;
                rippleY[rippleIdx] = pukurScreenY + (MathUtils.random() - 0.5f) * 30f;
                rippleRadius[rippleIdx] = 0f;
                rippleMaxLife[rippleIdx] = 2.0f + MathUtils.random() * 1.0f;
                rippleLife[rippleIdx] = rippleMaxLife[rippleIdx];
                rippleIdx = (rippleIdx + 1) % MAX_RIPPLES;
            }
            for (int i = 0; i < MAX_RIPPLES; i++) {
                if (rippleLife[i] <= 0f) continue;
                rippleLife[i] -= delta;
                float progress = 1f - (rippleLife[i] / rippleMaxLife[i]);
                rippleRadius[i] = progress * 35f;
            }
        }
    }

    public void render() {
        sr.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        sr.begin(ShapeType.Filled);

        // ── Dust motes (tiny bright spots) ──────────────────────────
        for (int i = 0; i < MAX_DUST; i++) {
            if (dustLife[i] <= 0f) continue;
            float t = dustLife[i] / dustMaxLife[i];
            float alpha = t < 0.2f ? t / 0.2f : (t > 0.8f ? (1f - t) / 0.2f : 1f);
            sr.setColor(0.98f, 0.94f, 0.80f, alpha * 0.45f);
            sr.circle(dustX[i], dustY[i], dustSize[i], 8);
        }

        // ── Falling leaves (small rhombus shapes) ───────────────────
        for (int i = 0; i < MAX_LEAVES; i++) {
            if (leafLife[i] <= 0f) continue;
            float t = leafLife[i] / leafMaxLife[i];
            float alpha = t < 0.15f ? t / 0.15f : (t > 0.85f ? (1f - t) / 0.15f : 1f);
            Color lc = leafColor[i];
            sr.setColor(lc.r, lc.g, lc.b, alpha * 0.80f);

            float ang = leafAngle[i] * MathUtils.degreesToRadians;
            float s = leafSize[i];
            float cosA = MathUtils.cos(ang);
            float sinA = MathUtils.sin(ang);

            // Leaf shape: two triangles forming a small pointed oval
            float tx = leafX[i], ty = leafY[i];
            // Tip points
            float tx1 = tx + cosA * s,        ty1 = ty + sinA * s;
            float tx2 = tx - cosA * s,        ty2 = ty - sinA * s;
            float tx3 = tx - sinA * s * 0.4f, ty3 = ty + cosA * s * 0.4f;
            float tx4 = tx + sinA * s * 0.4f, ty4 = ty - cosA * s * 0.4f;
            sr.triangle(tx1, ty1, tx3, ty3, tx2, ty2);
            sr.triangle(tx1, ty1, tx4, ty4, tx2, ty2);
        }

        sr.end();

        // ── Water Ripples (rings) ─────────────────────────────────────
        sr.begin(ShapeType.Line);
        for (int i = 0; i < MAX_RIPPLES; i++) {
            if (rippleLife[i] <= 0f || !pukurVisible) continue;
            float t = rippleLife[i] / rippleMaxLife[i];
            float alpha = t * 0.55f;
            sr.setColor(0.55f, 0.82f, 0.92f, alpha);
            // Elliptical ripple (wider than tall, perspective-flattened)
            float rx = rippleRadius[i];
            float ry = rx * 0.35f; // flatten for top-down perspective feel
            int segments = 18;
            for (int s = 0; s < segments; s++) {
                float a1 = (float)s / segments * MathUtils.PI2;
                float a2 = (float)(s+1) / segments * MathUtils.PI2;
                sr.line(
                    rippleX[i] + MathUtils.cos(a1) * rx,
                    rippleY[i] + MathUtils.sin(a1) * ry,
                    rippleX[i] + MathUtils.cos(a2) * rx,
                    rippleY[i] + MathUtils.sin(a2) * ry
                );
            }
        }
        sr.end();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void dispose() {
        sr.dispose();
    }
}
