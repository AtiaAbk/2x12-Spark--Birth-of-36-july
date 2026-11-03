package bd.spark36.hud.screens;

import bd.spark36.audio.SoundManager;
import bd.spark36.hud.FontRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

/**
 * High-tech Sci-Fi Loading Screen inspired by Dhaka University monuments and the July movement:
 * - Glowing Aparajeyo Bangla silhouette in electric cyan on left
 * - Glowing Central Shaheed Minar silhouette in crimson red on right
 * - Central animated rotating HUD radar rings & Dhaka skyline
 * - Prominent "2x12 SPARK" title and "BIRTH OF 36 JULY" subtitle
 * - Segmented gradient LED loading bar with percentage readout
 * - 5 Stages: Initializing System, Loading Assets, Preparing World, Syncing Data, Finalizing
 */
public class LoadingScreen {

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final FontRenderer fonts;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private float progress = 0.0f;
    private float animTime = 0.0f;
    private float totalDuration = 2.8f;
    private boolean finished = false;
    private float finishFade = 1.0f;
    private int lastStageSound = -1;

    // Cyberpunk Neon Color Palette
    private final Color bgDark = new Color(0.015f, 0.025f, 0.040f, 1f);
    private final Color cyanGlow = new Color(0.00f, 0.88f, 1.00f, 1f);
    private final Color cyanMuted = new Color(0.00f, 0.55f, 0.75f, 0.5f);
    private final Color greenGlow = new Color(0.00f, 0.95f, 0.45f, 1f);
    private final Color yellowGlow = new Color(1.00f, 0.85f, 0.20f, 1f);
    private final Color redGlow = new Color(1.00f, 0.18f, 0.25f, 1f);
    private final Color textWhite = new Color(0.95f, 0.98f, 1.00f, 1f);
    private final Color textDim = new Color(0.50f, 0.58f, 0.68f, 0.8f);

    private static final String[] STAGE_LABELS = {
        "INIT SYSTEM",
        "LOAD ASSETS",
        "PREPARE WORLD",
        "SYNC DATA",
        "FINALIZING"
    };

    public LoadingScreen(ShapeRenderer shapeRenderer, SpriteBatch batch, FontRenderer fonts) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.fonts = fonts;
    }

    private float virtW() { return 1600f; }

    private float virtH() {
        return 1600f * ((float) Gdx.graphics.getBackBufferHeight() / (float) Math.max(1, Gdx.graphics.getBackBufferWidth()));
    }

    public void setProgress(float p) {
        this.progress = MathUtils.clamp(p, 0f, 1f);
    }

    public float getProgress() {
        return progress;
    }

    public boolean isFinished() {
        return finished && finishFade <= 0.05f;
    }

    public void update(float delta) {
        animTime += delta;

        // Skip to end if user presses SPACE, ENTER, or clicks
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            progress = Math.max(progress, 0.96f);
        }

        // Advance progress smoothly over totalDuration
        if (progress < 1.0f) {
            float step = delta / totalDuration;
            float speedMod = 1.0f;
            if (progress < 0.25f) speedMod = 1.2f;
            else if (progress < 0.65f) speedMod = 0.9f;
            else if (progress < 0.90f) speedMod = 1.15f;
            else speedMod = 0.8f;

            progress = Math.min(1.0f, progress + step * speedMod);

            int currentStage = (int) (progress * 5.0f);
            if (currentStage > lastStageSound && currentStage < 5) {
                lastStageSound = currentStage;
                SoundManager.get().playClick();
            }
        } else {
            if (!finished) {
                finished = true;
                SoundManager.get().playObjective();
            }
            finishFade = Math.max(0.0f, finishFade - delta * 3.0f);
        }
    }

    public void render() {
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();
        float w = virtW(), h = virtH();
        float cx = w / 2f, cy = h / 2f;

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        Gdx.gl.glClearColor(bgDark.r, bgDark.g, bgDark.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        // 1. BACKGROUND GRADIENTS & SMOOTH NEON MIST DIFFUSION
        shapeRenderer.begin(ShapeType.Filled);
        // Base dark space gradient
        shapeRenderer.rect(0, 0, w, h,
            new Color(0.010f, 0.020f, 0.035f, 1f), new Color(0.015f, 0.025f, 0.040f, 1f),
            new Color(0.025f, 0.010f, 0.020f, 1f), new Color(0.020f, 0.015f, 0.030f, 1f));

        // Smooth Multi-Layer Radial Glow (Left Cyan)
        float leftBase = 0.025f + 0.010f * MathUtils.sin(animTime * 1.4f);
        for (int step = 0; step < 8; step++) {
            float frac = (8 - step) / 8.0f;
            shapeRenderer.setColor(0.00f, 0.50f, 0.80f, leftBase * frac);
            shapeRenderer.circle(w * 0.18f, h * 0.50f, w * 0.35f * (step + 1) / 8f, 36);
        }

        // Smooth Multi-Layer Radial Glow (Right Crimson)
        float rightBase = 0.025f + 0.010f * MathUtils.cos(animTime * 1.4f);
        for (int step = 0; step < 8; step++) {
            float frac = (8 - step) / 8.0f;
            shapeRenderer.setColor(0.80f, 0.08f, 0.15f, rightBase * frac);
            shapeRenderer.circle(w * 0.82f, h * 0.50f, w * 0.35f * (step + 1) / 8f, 36);
        }

        // Center Subtle Radar Backplate
        shapeRenderer.setColor(0.03f, 0.06f, 0.10f, 0.75f);
        shapeRenderer.circle(cx, cy + 45f, 210f, 48);
        shapeRenderer.end();

        // 2. MONUMENT SILHOUETTES & SCI-FI ELEMENTS
        renderMonuments(shapeRenderer, w, h);

        // 3. CENTRAL RADAR & RETICLE RINGS
        renderCentralRadar(shapeRenderer, cx, cy + 45f);

        // 4. SCI-FI HUD FRAME & CORNER BRACKETS
        renderHudFrame(shapeRenderer, w, h);

        // 5. PROGRESS BAR & STAGES
        renderProgressBar(shapeRenderer, cx, h * 0.24f);

        // 6. TEXT & LABELS
        renderText(cx, cy + 45f, h);
    }

    private void renderMonuments(ShapeRenderer sr, float w, float h) {
        // --- Left: Aparajeyo Bangla & Curzon Arches (Cyan / Blue Neon) ---
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        sr.setColor(0.00f, 0.65f, 0.90f, 0.40f);

        float lx = w * 0.12f;
        float ly = h * 0.32f;
        for (int i = -1; i <= 1; i++) {
            float archX = lx + i * 75f;
            sr.line(archX - 25f, ly, archX - 25f, ly + 90f);
            sr.line(archX + 25f, ly, archX + 25f, ly + 90f);
            sr.arc(archX, ly + 90f, 25f, 0f, 180f, 16);
        }

        sr.setColor(0.00f, 0.85f, 1.00f, 0.75f);
        sr.line(lx - 55f, ly, lx + 55f, ly);
        sr.line(lx - 55f, ly, lx - 40f, ly + 40f);
        sr.line(lx + 55f, ly, lx + 40f, ly + 40f);
        sr.line(lx - 40f, ly + 40f, lx + 40f, ly + 40f);

        sr.line(lx, ly + 40f, lx, ly + 140f);
        sr.circle(lx, ly + 152f, 12f, 12);
        sr.line(lx - 12f, ly + 115f, lx + 12f, ly + 115f);

        sr.line(lx - 22f, ly + 40f, lx - 22f, ly + 145f);
        sr.circle(lx - 22f, ly + 157f, 12f, 12);
        sr.line(lx - 34f, ly + 120f, lx - 10f, ly + 120f);
        sr.line(lx - 30f, ly + 70f, lx - 30f, ly + 180f);

        sr.line(lx + 22f, ly + 40f, lx + 22f, ly + 148f);
        sr.circle(lx + 22f, ly + 160f, 12f, 12);
        sr.line(lx + 10f, ly + 122f, lx + 34f, ly + 122f);
        sr.end();

        // --- Right: Central Shaheed Minar (Crimson Red Neon) ---
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        float rx = w * 0.88f;
        float ry = h * 0.32f;

        sr.setColor(1.00f, 0.25f, 0.30f, 0.45f);
        for (int s = 0; s < 4; s++) {
            float stepW = 120f - s * 18f;
            float stepY = ry + s * 8f;
            sr.line(rx - stepW / 2f, stepY, rx + stepW / 2f, stepY);
        }

        sr.setColor(1.00f, 0.15f, 0.20f, 0.80f);
        sr.circle(rx, ry + 110f, 38f, 24);

        sr.setColor(1.00f, 0.35f, 0.40f, 0.90f);
        float minarBaseY = ry + 32f;
        sr.line(rx - 16f, minarBaseY, rx - 16f, minarBaseY + 130f);
        sr.line(rx + 16f, minarBaseY, rx + 16f, minarBaseY + 130f);
        sr.line(rx - 16f, minarBaseY + 130f, rx, minarBaseY + 155f);
        sr.line(rx + 16f, minarBaseY + 130f, rx, minarBaseY + 155f);
        sr.line(rx, minarBaseY, rx, minarBaseY + 140f);

        float[] flankX = {-34f, -48f, 34f, 48f};
        float[] flankH = {105f, 80f, 105f, 80f};
        for (int f = 0; f < flankX.length; f++) {
            float fx = rx + flankX[f];
            float fh = flankH[f];
            sr.line(fx - 4f, minarBaseY, fx - 4f, minarBaseY + fh);
            sr.line(fx + 4f, minarBaseY, fx + 4f, minarBaseY + fh);
            sr.line(fx - 4f, minarBaseY + fh, fx + 4f, minarBaseY + fh);
        }
        sr.end();
    }

    private void renderCentralRadar(ShapeRenderer sr, float cx, float cy) {
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(1.5f);

        float outerR = 210f;
        sr.setColor(cyanMuted);
        sr.circle(cx, cy, outerR, 64);

        float rot = animTime * 20f;
        for (int deg = 0; deg < 360; deg += 30) {
            float rad = (deg + rot) * MathUtils.degreesToRadians;
            float cos = MathUtils.cos(rad);
            float sin = MathUtils.sin(rad);
            sr.setColor(0.00f, 0.85f, 1.00f, 0.65f);
            sr.line(cx + cos * (outerR - 10f), cy + sin * (outerR - 10f),
                    cx + cos * (outerR + 5f), cy + sin * (outerR + 5f));
        }

        float midR = 175f;
        sr.setColor(0.00f, 0.95f, 0.45f, 0.70f);
        sr.circle(cx, cy, midR, 48);

        float inR = 140f;
        sr.setColor(1.00f, 0.20f, 0.30f, 0.60f);
        sr.circle(cx, cy, inR, 48);

        sr.setColor(0.00f, 0.80f, 0.95f, 0.35f);
        sr.line(cx - outerR - 25f, cy, cx - outerR + 15f, cy);
        sr.line(cx + outerR - 15f, cy, cx + outerR + 25f, cy);
        sr.line(cx, cy - outerR - 25f, cx, cy - outerR + 15f);
        sr.line(cx, cy + outerR - 15f, cx, cy + outerR + 25f);
        sr.end();

        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        sr.setColor(0.00f, 0.95f, 0.60f, 0.85f);
        float baseSkylineY = cy + 42f;

        float[] skyX = {-110f, -90f, -70f, -50f, -30f, -10f, 10f, 30f, 50f, 70f, 90f, 110f};
        float[] skyH = {  35f,  60f,  45f,  85f,  65f, 115f, 95f, 75f, 80f, 50f, 65f,  30f};

        for (int i = 0; i < skyX.length - 1; i++) {
            float x0 = cx + skyX[i];
            float x1 = cx + skyX[i + 1];
            float h0 = skyH[i];
            float h1 = skyH[i + 1];

            sr.line(x0, baseSkylineY, x0, baseSkylineY + h0);
            sr.line(x0, baseSkylineY + h0, x1, baseSkylineY + h0);
            sr.line(x1, baseSkylineY + h0, x1, baseSkylineY + h1);
        }
        sr.end();
    }

    private void renderHudFrame(ShapeRenderer sr, float w, float h) {
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        float pad = 24f;
        float arm = 55f;

        sr.setColor(cyanGlow);
        sr.line(pad, h - pad, pad + arm, h - pad);
        sr.line(pad, h - pad, pad, h - pad - arm);
        sr.circle(pad + arm + 6f, h - pad, 2.5f, 8);

        sr.line(pad, pad, pad + arm, pad);
        sr.line(pad, pad, pad, pad + arm);
        sr.circle(pad + arm + 6f, pad, 2.5f, 8);

        sr.setColor(redGlow);
        sr.line(w - pad, h - pad, w - pad - arm, h - pad);
        sr.line(w - pad, h - pad, w - pad, h - pad - arm);
        sr.circle(w - pad - arm - 6f, h - pad, 2.5f, 8);

        sr.line(w - pad, pad, w - pad - arm, pad);
        sr.line(w - pad, pad, w - pad, pad + arm);
        sr.circle(w - pad - arm - 6f, pad, 2.5f, 8);

        sr.setColor(0.12f, 0.22f, 0.35f, 0.50f);
        sr.line(pad + arm + 15f, h - pad, w - pad - arm - 15f, h - pad);
        sr.line(pad + arm + 15f, pad, w - pad - arm - 15f, pad);
        sr.line(pad, pad + arm + 15f, pad, h - pad - arm - 15f);
        sr.line(w - pad, pad + arm + 15f, w - pad, h - pad - arm - 15f);
        sr.end();
    }

    private void renderProgressBar(ShapeRenderer sr, float cx, float barY) {
        float barW = 760f;
        float barH = 22f;
        float startX = cx - barW / 2f;

        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(1.8f);
        sr.setColor(0.00f, 0.75f, 0.95f, 0.65f);
        sr.rect(startX - 6f, barY - 4f, barW + 12f, barH + 8f);
        sr.end();

        sr.begin(ShapeType.Filled);
        sr.setColor(0.04f, 0.08f, 0.12f, 0.90f);
        sr.rect(startX, barY, barW, barH);

        int totalSegments = 36;
        float gap = 3.5f;
        float segW = (barW - (totalSegments - 1) * gap) / totalSegments;
        int activeSegments = Math.round(progress * totalSegments);

        for (int i = 0; i < totalSegments; i++) {
            float segX = startX + i * (segW + gap);
            float t = (float) i / (float) totalSegments;

            Color segColor;
            if (t < 0.30f) {
                segColor = cyanGlow;
            } else if (t < 0.60f) {
                segColor = greenGlow;
            } else if (t < 0.82f) {
                segColor = yellowGlow;
            } else {
                segColor = redGlow;
            }

            if (i < activeSegments) {
                sr.setColor(segColor.r, segColor.g, segColor.b, 0.92f);
                sr.rect(segX, barY + 2f, segW, barH - 4f);
            } else {
                sr.setColor(segColor.r * 0.15f, segColor.g * 0.15f, segColor.b * 0.15f, 0.25f);
                sr.rect(segX, barY + 2f, segW, barH - 4f);
            }
        }
        sr.end();

        // 5 Pipeline Stage Badges
        float stageY = barY - 46f;
        float stageAreaW = 920f;
        float stageSpacing = stageAreaW / 5f;
        float stageStartX = cx - stageAreaW / 2f + stageSpacing / 2f;

        for (int s = 0; s < 5; s++) {
            float sx = stageStartX + s * stageSpacing;
            float stageThreshold = (s + 1) * 0.20f;
            boolean isCompleted = progress >= stageThreshold;
            boolean isActive = !isCompleted && (progress >= s * 0.20f);

            sr.begin(ShapeType.Filled);
            if (isCompleted) {
                sr.setColor(0.00f, 0.90f, 0.45f, 0.90f);
            } else if (isActive) {
                float pulse = 0.6f + 0.4f * MathUtils.sin(animTime * 6f);
                sr.setColor(0.00f, 0.85f, 1.00f, pulse);
            } else {
                sr.setColor(0.15f, 0.20f, 0.28f, 0.50f);
            }
            sr.circle(sx - 48f, stageY + 6f, 6.5f, 16);
            sr.end();

            sr.begin(ShapeType.Line);
            Gdx.gl.glLineWidth(1.2f);
            if (isActive) {
                sr.setColor(cyanGlow);
                sr.circle(sx - 48f, stageY + 6f, 9.5f, 16);
            }
            sr.end();
        }
    }

    private void renderText(float cx, float cy, float h) {
        batch.begin();

        // 1. GAME TITLE: "2x12 SPARK"
        fonts.titleFont.setColor(0.00f, 0.80f, 1.00f, 0.45f);
        drawCentered(fonts.titleFont, "2x12 SPARK", cx + 2f, cy - 18f);
        fonts.titleFont.setColor(1.00f, 0.20f, 0.30f, 0.45f);
        drawCentered(fonts.titleFont, "2x12 SPARK", cx - 2f, cy - 18f);

        fonts.titleFont.setColor(textWhite);
        drawCentered(fonts.titleFont, "2x12 SPARK", cx, cy - 20f);

        // 2. SUBTITLE: "BIRTH OF 36 JULY"
        fonts.headerFont.setColor(yellowGlow);
        drawCentered(fonts.headerFont, "B I R T H   O F   3 6   J U L Y", cx, cy - 54f);

        // 3. "LOADING..." & PERCENTAGE (Positioned with generous clearance above the bar)
        float barY = h * 0.24f;
        float pulseDot = 0.70f + 0.30f * MathUtils.sin(animTime * 4.5f);
        fonts.headerFont.setColor(0.00f, 0.90f, 0.70f, pulseDot);
        String dots = (animTime % 1.5f < 0.5f) ? "LOADING." : ((animTime % 1.5f < 1.0f) ? "LOADING.." : "LOADING...");
        drawCentered(fonts.headerFont, dots, cx, barY + 48f);

        int pct = Math.round(progress * 100f);
        fonts.headerFont.setColor(cyanGlow);
        fonts.headerFont.draw(batch, pct + "%", cx + 400f, barY + 18f);

        // 4. STAGE NAMES
        float stageY = barY - 46f;
        float stageAreaW = 920f;
        float stageSpacing = stageAreaW / 5f;
        float stageStartX = cx - stageAreaW / 2f + stageSpacing / 2f;

        for (int s = 0; s < 5; s++) {
            float sx = stageStartX + s * stageSpacing;
            float stageThreshold = (s + 1) * 0.20f;
            boolean isCompleted = progress >= stageThreshold;
            boolean isActive = !isCompleted && (progress >= s * 0.20f);

            if (isCompleted) {
                fonts.smallFont.setColor(0.00f, 0.95f, 0.50f, 0.95f);
            } else if (isActive) {
                fonts.smallFont.setColor(cyanGlow);
            } else {
                fonts.smallFont.setColor(textDim);
            }
            fonts.smallFont.draw(batch, STAGE_LABELS[s], sx - 35f, stageY + 12f);
        }

        // 5. FOOTER HINT
        fonts.smallFont.setColor(new Color(0.40f, 0.50f, 0.65f, 0.70f));
        drawCentered(fonts.smallFont, "PRESS [SPACE / ENTER] OR CLICK TO SKIP", cx, 36f);

        batch.end();
    }

    private void drawCentered(BitmapFont font, String text, float cx, float y) {
        glyphLayout.setText(font, text);
        font.draw(batch, text, cx - glyphLayout.width / 2f, y);
    }
}
