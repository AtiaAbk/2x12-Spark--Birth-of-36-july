package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.Disposable;

/**
 * Environmental particle system for 2x12: Spark.
 * Spawns falling leaves, dust motes, bokeh ambient, and pukur water ripples.
 * Leaves are simulated in 3D world space and projected through the camera; dust and ripples
 * are 2D screen-space. Everything is drawn after the 3D pass, before the HUD.
 */
public class ParticleSystem implements Disposable {

    // ─── Leaf Particle (world space) ──────────────────────────────────
    // Leaves live in 3D world coordinates (metres) and are projected through the game camera
    // every frame, so they stay put in the world as the camera moves instead of riding along
    // as a screen overlay.
    private static final int MAX_LEAVES = 80;
    private final float[] leafX = new float[MAX_LEAVES];
    private final float[] leafY = new float[MAX_LEAVES];
    private final float[] leafZ = new float[MAX_LEAVES];
    private final float[] leafVX = new float[MAX_LEAVES];      // horizontal velocity (relaxes toward wind)
    private final float[] leafVZ = new float[MAX_LEAVES];
    private final float[] leafFall = new float[MAX_LEAVES];    // base fall speed m/s
    private final float[] leafDrag = new float[MAX_LEAVES];    // how quickly the leaf follows the wind
    private final float[] leafAngle = new float[MAX_LEAVES];   // screen-plane spin (deg)
    private final float[] leafAngVel = new float[MAX_LEAVES];
    private final float[] leafTumble = new float[MAX_LEAVES];  // flip phase (rad)
    private final float[] leafTumbleVel = new float[MAX_LEAVES];
    private final float[] leafSway = new float[MAX_LEAVES];    // flutter phase
    private final float[] leafSwayFreq = new float[MAX_LEAVES];
    private final float[] leafSwayAmp = new float[MAX_LEAVES];
    private final float[] leafSwayDirX = new float[MAX_LEAVES];
    private final float[] leafSwayDirZ = new float[MAX_LEAVES];
    private final float[] leafAge = new float[MAX_LEAVES];     // seconds since spawn (fade-in)
    private final float[] leafFade = new float[MAX_LEAVES];    // >0 = fading out, seconds left
    private final float[] leafSize = new float[MAX_LEAVES];    // half-length in metres
    private final boolean[] leafAlive = new boolean[MAX_LEAVES];
    private final boolean[] leafLanded = new boolean[MAX_LEAVES];
    private final Color[] leafColor = new Color[MAX_LEAVES];
    private int leafActive = 0;
    private float leafSpawnAccum = 0f;
    private boolean leavesSeeded = false;

    private static final float LEAF_FADE_IN = 0.8f;
    private static final float LEAF_GROUND_FADE = 2.2f;
    private static final float LEAF_FAR_FADE = 0.9f;
    private static final float LEAF_SPAWN_RADIUS = 30f;
    private static final float LEAF_KILL_RADIUS = 40f;

    /**
     * Broadleaf canopy centres (x, z) — mirrors the rain tree / krishnachura entries of
     * DhakaCampusWorld's treeLocations table (palms excluded). DhakaCampusWorld exposes no
     * getter, so the list is duplicated here; keep in sync if trees are moved.
     */
    // Leaf-shedding trees (rain trees and krishnachura; palms do not drop broad leaves), as x,z pairs
    private static final float[] TREE_XZ = buildTreeXZ();

    private static float[] buildTreeXZ() {
        int n = 0;
        for (float[] t : DhakaCampusWorld.TREE_LOCATIONS) if (t[2] != 2) n++;
        float[] xz = new float[n * 2];
        int i = 0;
        for (float[] t : DhakaCampusWorld.TREE_LOCATIONS) {
            if (t[2] == 2) continue;
            xz[i++] = t[0];
            xz[i++] = t[1];
        }
        return xz;
    }
    private final int[] nearTrees = new int[TREE_XZ.length / 2];

    // ─── Global wind (changes smoothly; gusts & calms) ───────────────
    private float windX = 0.6f, windZ = 0.2f;          // current wind m/s
    private float windTargetX = 0.6f, windTargetZ = 0.2f;
    private float windAngle = MathUtils.random(MathUtils.PI2);
    private float windTimer = 0f;                        // time until next target
    private float gust = 0f, gustTarget = 0f, gustTimer = 4f;
    private float calm = 0f, calmTarget = 0f;
    private float windDensity = 0.5f;                    // 0..1 fraction of MAX_LEAVES wanted

    private Camera leafCamera;
    private final Vector3 leafProj = new Vector3();

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
        for (int i = 0; i < MAX_LEAVES; i++) {
            leafAlive[i] = false;
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

    /** Spawns leaf i in world space around (cx, cz), biased toward nearby tree canopies. */
    private void spawnLeaf(int i, float cx, float cz, boolean prewarm) {
        // Collect canopies within range (no allocation: reuse index buffer)
        int nTrees = 0;
        float r2 = (LEAF_SPAWN_RADIUS + 5f) * (LEAF_SPAWN_RADIUS + 5f);
        for (int t = 0; t < nearTrees.length; t++) {
            float dx = TREE_XZ[t * 2] - cx, dz = TREE_XZ[t * 2 + 1] - cz;
            if (dx * dx + dz * dz < r2) nearTrees[nTrees++] = t;
        }
        float x, z, y;
        if (nTrees > 0 && MathUtils.random() < 0.72f) {
            // Drop out of a canopy (rain trees spread ~5-7 m)
            int t = nearTrees[MathUtils.random(nTrees - 1)];
            float a = MathUtils.random(MathUtils.PI2);
            float r = (float) Math.sqrt(MathUtils.random()) * 6f;
            x = TREE_XZ[t * 2] + MathUtils.cos(a) * r;
            z = TREE_XZ[t * 2 + 1] + MathUtils.sin(a) * r;
            y = 4f + MathUtils.random() * 5f;
        } else {
            // Carried in on the air from somewhere upwind in the surrounding volume
            float a = MathUtils.random(MathUtils.PI2);
            float r = 4f + (float) Math.sqrt(MathUtils.random()) * (LEAF_SPAWN_RADIUS - 4f);
            x = cx + MathUtils.cos(a) * r - windX * 3f;
            z = cz + MathUtils.sin(a) * r - windZ * 3f;
            y = 3f + MathUtils.random() * 6f;
        }
        if (prewarm) y = 0.4f + MathUtils.random() * 8.5f; // start mid-flight so no "sheet" appears

        leafX[i] = x;
        leafY[i] = y;
        leafZ[i] = z;
        leafVX[i] = windX * 0.5f;
        leafVZ[i] = windZ * 0.5f;
        leafFall[i] = 0.6f + MathUtils.random() * 0.6f;
        leafDrag[i] = 0.7f + MathUtils.random() * 1.1f;
        leafAngle[i] = MathUtils.random() * 360f;
        leafAngVel[i] = (MathUtils.random() - 0.5f) * 160f;
        leafTumble[i] = MathUtils.random(MathUtils.PI2);
        leafTumbleVel[i] = 2f + MathUtils.random() * 5f;
        leafSway[i] = MathUtils.random(MathUtils.PI2);
        leafSwayFreq[i] = 1.2f + MathUtils.random() * 1.6f;
        leafSwayAmp[i] = 0.35f + MathUtils.random() * 0.55f;
        float sa = MathUtils.random(MathUtils.PI2);
        leafSwayDirX[i] = MathUtils.cos(sa);
        leafSwayDirZ[i] = MathUtils.sin(sa);
        leafAge[i] = prewarm ? LEAF_FADE_IN * MathUtils.random() : 0f;
        leafFade[i] = 0f;
        leafSize[i] = 0.045f + MathUtils.random() * 0.045f;
        leafLanded[i] = false;
        leafAlive[i] = true;
        leafColor[i].set(LEAF_COLORS[MathUtils.random(LEAF_COLORS.length - 1)]);
    }

    /** Evolves the global wind: smooth drift between random targets, gusts and calm spells. */
    private void updateWind(float delta) {
        windTimer -= delta;
        if (windTimer <= 0f) {
            windTimer = 3f + MathUtils.random() * 4f;
            windAngle += MathUtils.random(-1.2f, 1.2f);
            float speed = 0.3f + MathUtils.random() * 1.9f;
            windTargetX = MathUtils.cos(windAngle) * speed;
            windTargetZ = MathUtils.sin(windAngle) * speed;
            // Occasionally settle into a calm spell
            calmTarget = MathUtils.random() < 0.22f ? 1f : 0f;
        }
        gustTimer -= delta;
        if (gustTimer <= 0f) {
            if (gustTarget > 0f) {
                gustTarget = 0f;                                    // gust dies down
                gustTimer = 5f + MathUtils.random() * 9f;
            } else if (calmTarget < 0.5f && MathUtils.random() < 0.6f) {
                gustTarget = 0.6f + MathUtils.random() * 0.4f;      // a gust rolls through
                gustTimer = 1.8f + MathUtils.random() * 2.5f;
            } else {
                gustTimer = 3f + MathUtils.random() * 4f;
            }
        }
        gust += (gustTarget - gust) * Math.min(1f, delta * (gustTarget > gust ? 1.6f : 0.5f));
        calm += (calm < calmTarget ? 1f : -1f) * delta * 0.25f;
        calm = MathUtils.clamp(calm, 0f, 1f);

        float strength = (1f + gust * 1.8f) * (1f - calm * 0.8f);
        float k = Math.min(1f, delta * 0.45f);
        windX += (windTargetX * strength - windX) * k;
        windZ += (windTargetZ * strength - windZ) * k;

        windDensity = MathUtils.clamp(0.38f + gust * 0.62f - calm * 0.3f, 0.08f, 1f);
    }

    private void updateLeaves(float delta, Camera camera) {
        updateWind(delta);

        // Spawn centre: the player region a little ahead of the (third-person) camera
        float fx = camera.direction.x, fz = camera.direction.z;
        float fl = (float) Math.sqrt(fx * fx + fz * fz);
        if (fl > 1e-4f) { fx /= fl; fz /= fl; } else { fx = 0f; fz = 1f; }
        float cx = camera.position.x + fx * 10f;
        float cz = camera.position.z + fz * 10f;

        float kill2 = LEAF_KILL_RADIUS * LEAF_KILL_RADIUS;
        leafActive = 0;
        for (int i = 0; i < MAX_LEAVES; i++) {
            if (!leafAlive[i]) continue;
            leafAge[i] += delta;

            if (leafFade[i] > 0f) {
                leafFade[i] -= delta;
                if (leafFade[i] <= 0f) { leafAlive[i] = false; continue; }
            }

            if (!leafLanded[i]) {
                // Local turbulence on top of the global wind (cheap spatial/time noise)
                float turbX = MathUtils.sin(leafZ[i] * 0.31f + time * 1.3f + leafSway[i]) * 0.35f;
                float turbZ = MathUtils.sin(leafX[i] * 0.27f - time * 1.1f) * 0.35f;
                float k = Math.min(1f, delta * leafDrag[i]);
                leafVX[i] += (windX + turbX - leafVX[i]) * k;
                leafVZ[i] += (windZ + turbZ - leafVZ[i]) * k;

                // Flutter: pendulum side-sway; the leaf drops faster when edge-on
                leafSway[i] += leafSwayFreq[i] * delta;
                float swing = MathUtils.cos(leafSway[i]) * leafSwayAmp[i] * leafSwayFreq[i];
                float edgeOn = Math.abs(MathUtils.sin(leafSway[i]));
                float vy = -leafFall[i] * (0.55f + 0.9f * edgeOn)
                    + gust * 0.35f * MathUtils.sin(time * 2.3f + leafSway[i]);

                leafX[i] += (leafVX[i] + leafSwayDirX[i] * swing) * delta;
                leafZ[i] += (leafVZ[i] + leafSwayDirZ[i] * swing) * delta;
                leafY[i] += vy * delta;

                float spin = 1f + gust;
                leafAngle[i] += leafAngVel[i] * spin * delta;
                leafTumble[i] += leafTumbleVel[i] * spin * delta;

                if (leafY[i] <= 0.03f) {
                    leafY[i] = 0.03f;
                    leafLanded[i] = true;
                    if (leafFade[i] <= 0f) leafFade[i] = LEAF_GROUND_FADE;
                }
            }

            float dx = leafX[i] - cx, dz = leafZ[i] - cz;
            if (leafFade[i] <= 0f && dx * dx + dz * dz > kill2) leafFade[i] = LEAF_FAR_FADE;
            leafActive++;
        }

        // Keep population near the wind-driven density target
        int desired = (int) (MAX_LEAVES * windDensity);
        if (!leavesSeeded) {
            leavesSeeded = true;
            for (int i = 0; i < MAX_LEAVES && leafActive < desired; i++) {
                if (!leafAlive[i]) { spawnLeaf(i, cx, cz, true); leafActive++; }
            }
        }
        if (leafActive < desired) {
            leafSpawnAccum += delta * (4f + 18f * gust);
            for (int i = 0; i < MAX_LEAVES && leafSpawnAccum >= 1f && leafActive < desired; i++) {
                if (!leafAlive[i]) {
                    spawnLeaf(i, cx, cz, false);
                    leafActive++;
                    leafSpawnAccum -= 1f;
                }
            }
        } else {
            leafSpawnAccum = 0f;
        }
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

        // ── Leaves (world space) ────────────────────────────────────
        leafCamera = camera;
        updateLeaves(delta, camera);

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

        // ── Falling leaves (small rhombus shapes, projected from world space) ──
        if (leafCamera != null) {
            Camera cam = leafCamera;
            float fov = cam instanceof PerspectiveCamera ? ((PerspectiveCamera) cam).fieldOfView : 67f;
            float pxPerMetreAt1 = screenH / (2f * (float) Math.tan(fov * 0.5f * MathUtils.degreesToRadians));
            for (int i = 0; i < MAX_LEAVES; i++) {
                if (!leafAlive[i]) continue;
                // Depth along view direction; cull behind / too close to the lens
                float depth = (leafX[i] - cam.position.x) * cam.direction.x
                    + (leafY[i] - cam.position.y) * cam.direction.y
                    + (leafZ[i] - cam.position.z) * cam.direction.z;
                if (depth < 0.4f) continue;
                leafProj.set(leafX[i], leafY[i], leafZ[i]);
                cam.project(leafProj, 0f, 0f, screenW, screenH);
                if (leafProj.z <= 0f || leafProj.z >= 1f) continue;
                float s = leafSize[i] * pxPerMetreAt1 / depth;
                if (leafProj.x < -s - 4 || leafProj.x > screenW + s + 4
                    || leafProj.y < -s - 4 || leafProj.y > screenH + s + 4) continue;

                float alpha = Math.min(1f, leafAge[i] / LEAF_FADE_IN);
                if (leafFade[i] > 0f) {
                    float total = leafLanded[i] ? LEAF_GROUND_FADE : LEAF_FAR_FADE;
                    alpha *= Math.min(1f, leafFade[i] / total);
                }
                // Distance haze; sub-pixel leaves fade rather than shimmer
                alpha *= MathUtils.clamp(1.25f - depth / 36f, 0f, 1f);
                if (s < 1.4f) { alpha *= s / 1.4f; s = 1.4f; }
                if (s > 42f) s = 42f;
                if (alpha <= 0.01f) continue;

                Color lc = leafColor[i];
                sr.setColor(lc.r, lc.g, lc.b, alpha * 0.85f);

                float ang = leafAngle[i] * MathUtils.degreesToRadians;
                float cosA = MathUtils.cos(ang);
                float sinA = MathUtils.sin(ang);
                // Tumbling in 3D: width collapses when the leaf turns edge-on
                float w = leafLanded[i] ? s * 0.4f
                    : s * 0.4f * (0.2f + 0.8f * Math.abs(MathUtils.cos(leafTumble[i])));

                // Leaf shape: two triangles forming a small pointed oval
                float tx = leafProj.x, ty = leafProj.y;
                float tx1 = tx + cosA * s, ty1 = ty + sinA * s;
                float tx2 = tx - cosA * s, ty2 = ty - sinA * s;
                float tx3 = tx - sinA * w, ty3 = ty + cosA * w;
                float tx4 = tx + sinA * w, ty4 = ty - cosA * w;
                sr.triangle(tx1, ty1, tx3, ty3, tx2, ty2);
                sr.triangle(tx1, ty1, tx4, ty4, tx2, ty2);
            }
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
