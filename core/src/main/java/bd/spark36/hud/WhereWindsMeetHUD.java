package bd.spark36.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.character.PlayerController;
import bd.spark36.world.JulyMemorials;
import bd.spark36.world.JulyMemorials.MemorialEntry;

/**
 * 1:1 Recreation of the Where Winds Meet HUD matching game_exact_ui_design_1790197130736.jpg:
 * - Top-Left: Active mission card with glowing gold progress bar, distance meter, and waypoint compass.
 * - Top-Right: 8-point antique faceted 3D nautical compass star with CURZON HALL waypoint ticker.
 * - Bottom-Left: Fluid curved tapered health bar with gold filigree wing and stamina bar.
 * - Center: Speech-bubble prompt [E] INSPECT HISTORICAL ARCHIVE — 36 JULY MOVEMENT with pointer beak.
 * - Bottom-Right: Individual glassmorphic keycaps for WASD, Shift, Space, and E.
 */
public class WhereWindsMeetHUD implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // Theme Colors
    private final Color glassBg = new Color(0.05f, 0.07f, 0.10f, 0.82f);
    private final Color goldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color goldAccent = new Color(1.0f, 0.85f, 0.40f, 1f);
    private final Color goldDark = new Color(0.55f, 0.42f, 0.18f, 1f);
    private final Color goldMuted = new Color(0.68f, 0.58f, 0.32f, 0.70f);
    private final Color healthRed = new Color(0.85f, 0.16f, 0.18f, 1f);
    private final Color healthBg = new Color(0.25f, 0.06f, 0.06f, 0.85f);
    private final Color staminaGreen = new Color(0.18f, 0.85f, 0.62f, 1f); // Jade cyan
    private final Color staminaBg = new Color(0.06f, 0.22f, 0.16f, 0.85f);

    // States
    private MemorialEntry activeModalEntry = null;
    private boolean isMapOpen = false;
    private boolean isPauseMenuOpen = false;
    private float animTime = 0f;

    public WhereWindsMeetHUD(FontRenderer fontRenderer) {
        this.fonts = fontRenderer;
    }

    public void update(float delta, PlayerController player, JulyMemorials memorials) {
        animTime += delta;

        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());

        if (nearby != null && (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
            if (activeModalEntry == null) {
                activeModalEntry = nearby;
                nearby.inspected = true;
            } else {
                activeModalEntry = null;
            }
        }

        if (activeModalEntry != null && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            activeModalEntry = null;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (activeModalEntry == null) {
                isMapOpen = !isMapOpen;
            }
        }

        if (activeModalEntry == null && !isMapOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPauseMenuOpen = !isPauseMenuOpen;
        }
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw) {
        int backbufferW = Gdx.graphics.getBackBufferWidth();
        int backbufferH = Gdx.graphics.getBackBufferHeight();

        // Virtual coordinates scaling: 1600 baseline
        float w = 1600f;
        float h = 1600f * ((float) backbufferH / (float) backbufferW);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        MemorialEntry nearest = memorials.getNextObjective(player.getPosition());
        float dstToNearest = nearest != null ? player.getPosition().dst(nearest.position) : 65.4f;

        // 1. Draw HUD Background Shapes & Ornate Geometry
        shapeRenderer.begin(ShapeType.Filled);
        drawMissionCardBg(w, h);
        drawCurvedHealthBarBg(player, w, h);
        drawAntiqueCompassRoseFilled(w, h, cameraYaw);
        drawKeycapsBg(w, h);

        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());
        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            drawSpeechBubblePromptBg(w, h);
        }
        shapeRenderer.end();

        // 2. Draw HUD Outlines, Accents & Filigree
        shapeRenderer.begin(ShapeType.Line);
        drawMissionCardBorders(w, h);
        drawCurvedHealthBarBorders(player, w, h);
        drawAntiqueCompassRoseLines(w, h, cameraYaw);
        drawKeycapsBorders(w, h);

        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            drawSpeechBubblePromptBorders(w, h);
        }
        shapeRenderer.end();

        // 3. Draw Typography & Glyphs
        spriteBatch.begin();
        drawMissionCardText(memorials, nearest, dstToNearest, w, h);
        drawCurvedHealthBarText(player, w, h);
        drawAntiqueCompassRoseText(w, h, cameraYaw);
        drawKeycapsText(w, h);

        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            drawSpeechBubblePromptText(nearby, w, h);
        }
        spriteBatch.end();

        // 4. Overlays: Historical Archival Modal, Map, or Pause Menu
        if (activeModalEntry != null) {
            renderMemorialModal(activeModalEntry, w, h);
        } else if (isMapOpen) {
            renderCampusMap(player, memorials, cameraYaw, w, h);
        } else if (isPauseMenuOpen) {
            renderPauseMenu(w, h);
        }
    }

    // ==========================================
    // 1. TOP-LEFT MISSION CARD (Matching Mockup)
    // ==========================================
    private void drawMissionCardBg(float w, float h) {
        float cardX = 36f;
        float cardY = h - 195f;
        float cardW = 390f;
        float cardH = 160f;

        // Dark glass background
        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(cardX, cardY, cardW, cardH);

        // Gold Diamond Icon for "◆ ACTIVE MISSION"
        shapeRenderer.setColor(goldAccent);
        float dX = cardX + 18f;
        float dY = cardY + cardH - 18f;
        float dS = 5f;
        shapeRenderer.triangle(dX, dY + dS, dX + dS, dY, dX, dY - dS);
        shapeRenderer.triangle(dX, dY + dS, dX - dS, dY, dX, dY - dS);

        // Glowing Golden Progress Bar
        float barX = cardX + 16f;
        float barY = cardY + 48f;
        float barW = 300f;
        float barH = 5f;

        // Track
        shapeRenderer.setColor(0.18f, 0.16f, 0.12f, 0.9f);
        shapeRenderer.rect(barX, barY, barW, barH);

        // Gold Fill with Gleam
        shapeRenderer.setColor(goldAccent);
        float fillW = barW * 0.72f;
        shapeRenderer.rect(barX, barY, fillW, barH);

        // Gleam particle
        float gleamPos = (MathUtils.sin(animTime * 2.5f) + 1f) * 0.5f * fillW;
        shapeRenderer.setColor(1.0f, 0.98f, 0.85f, 0.95f);
        shapeRenderer.circle(barX + gleamPos, barY + barH / 2f, 4f, 12);

        // Waypoint Compass Needle Icon Circle (next to DISTANCE)
        float cX = cardX + 338f;
        float cY = cardY + 22f;
        shapeRenderer.setColor(0.08f, 0.18f, 0.22f, 0.9f);
        shapeRenderer.circle(cX, cY, 15f, 20);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cX, cY, 2.5f, 10);
    }

    private void drawMissionCardBorders(float w, float h) {
        float cardX = 36f;
        float cardY = h - 195f;
        float cardW = 390f;
        float cardH = 160f;

        // Subtle outer border
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(cardX, cardY, cardW, cardH);

        // Ornate Corner Brackets (⌜ ⌝ ⌞ ⌟)
        shapeRenderer.setColor(goldAccent);
        float cLen = 14f;

        // Top-Left ⌜
        shapeRenderer.line(cardX - 2, cardY + cardH + 2, cardX + cLen, cardY + cardH + 2);
        shapeRenderer.line(cardX - 2, cardY + cardH + 2, cardX - 2, cardY + cardH - cLen);

        // Top-Right ⌝
        shapeRenderer.line(cardX + cardW + 2, cardY + cardH + 2, cardX + cardW - cLen, cardY + cardH + 2);
        shapeRenderer.line(cardX + cardW + 2, cardY + cardH + 2, cardX + cardW + 2, cardY + cardH - cLen);

        // Bottom-Left ⌞
        shapeRenderer.line(cardX - 2, cardY - 2, cardX + cLen, cardY - 2);
        shapeRenderer.line(cardX - 2, cardY - 2, cardX - 2, cardY + cLen);

        // Bottom-Right ⌟
        shapeRenderer.line(cardX + cardW + 2, cardY - 2, cardX + cardW - cLen, cardY - 2);
        shapeRenderer.line(cardX + cardW + 2, cardY - 2, cardX + cardW + 2, cardY + cLen);

        // Waypoint Compass Needle Icon Ring
        float cX = cardX + 338f;
        float cY = cardY + 22f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cX, cY, 15f, 24);

        // Pointer needle (pointing towards Curzon Hall)
        float nAngle = (animTime * 1.5f) % 360f * MathUtils.degreesToRadians;
        shapeRenderer.line(cX, cY, cX + 11f * MathUtils.cos(nAngle), cY + 11f * MathUtils.sin(nAngle));
    }

    private void drawMissionCardText(JulyMemorials memorials, MemorialEntry nearest, float dst, float w, float h) {
        float cardX = 36f;
        float cardY = h - 195f;

        // 1. ACTIVE MISSION
        fonts.smallFont.setColor(goldAccent);
        int ins = memorials.getInspectedCount() + 1;
        String missionTag = String.format("ACTIVE MISSION: %02d / %02d", Math.min(ins, 5), 5);
        fonts.smallFont.draw(spriteBatch, missionTag, cardX + 28f, cardY + 148f);

        // 2. REACH CURZON HALL (Bold uppercase header)
        fonts.titleFont.setColor(Color.WHITE);
        fonts.titleFont.draw(spriteBatch, "REACH CURZON HALL", cardX + 16f, cardY + 124f);

        // 3. Subtitle description
        fonts.bodyFont.setColor(new Color(0.88f, 0.88f, 0.90f, 1f));
        fonts.bodyFont.draw(spriteBatch, "Investigate the historical science faculty forecourt", cardX + 16f, cardY + 92f);

        // 4. DISTANCE: 65.4 M
        fonts.headerFont.setColor(goldAccent);
        String distStr = String.format("DISTANCE: %.1f M", dst);
        fonts.headerFont.draw(spriteBatch, distStr, cardX + 16f, cardY + 30f);
    }

    // =======================================================
    // 2. TOP-RIGHT ANTIQUE 8-POINT COMPASS ROSE (Matching Mockup)
    // =======================================================
    private void drawAntiqueCompassRoseFilled(float w, float h, float yaw) {
        float cx = w - 105f;
        float cy = h - 105f;
        float r = 70f;

        // Circular dark background
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.85f);
        shapeRenderer.circle(cx, cy, r, 48);

        // Waypoint Ticker Pill to the left ("◆ CURZON HALL  W")
        float pillW = 165f;
        float pillH = 28f;
        float pillX = cx - r - pillW - 14f;
        float pillY = cy - pillH / 2f;
        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(pillX, pillY, pillW, pillH);

        // Diamond marker on ticker
        shapeRenderer.setColor(goldAccent);
        float dX = pillX + 14f;
        float dY = cy;
        float dS = 5.5f;
        shapeRenderer.triangle(dX, dY + dS, dX + dS, dY, dX, dY - dS);
        shapeRenderer.triangle(dX, dY + dS, dX - dS, dY, dX, dY - dS);

        // 8-Point Antique Shaded Compass Star!
        // 4 Primary Points (N, E, S, W)
        float len1 = r - 10f;
        float baseW1 = 12f;

        for (int i = 0; i < 4; i++) {
            float angleDeg = i * 90f - yaw + 90f;
            float tipRad = angleDeg * MathUtils.degreesToRadians;
            float leftRad = (angleDeg - 25f) * MathUtils.degreesToRadians;
            float rightRad = (angleDeg + 25f) * MathUtils.degreesToRadians;

            float tx = cx + len1 * MathUtils.cos(tipRad);
            float ty = cy + len1 * MathUtils.sin(tipRad);
            float lx = cx + baseW1 * MathUtils.cos(leftRad);
            float ly = cy + baseW1 * MathUtils.sin(leftRad);
            float rx = cx + baseW1 * MathUtils.cos(rightRad);
            float ry = cy + baseW1 * MathUtils.sin(rightRad);

            // Shaded light gold facet
            shapeRenderer.setColor(goldAccent);
            shapeRenderer.triangle(cx, cy, tx, ty, lx, ly);

            // Shaded dark bronze facet
            shapeRenderer.setColor(goldDark);
            shapeRenderer.triangle(cx, cy, tx, ty, rx, ry);
        }

        // 4 Secondary Points (NE, SE, SW, NW)
        float len2 = r * 0.58f;
        float baseW2 = 8f;

        for (int i = 0; i < 4; i++) {
            float angleDeg = i * 90f + 45f - yaw + 90f;
            float tipRad = angleDeg * MathUtils.degreesToRadians;
            float leftRad = (angleDeg - 25f) * MathUtils.degreesToRadians;
            float rightRad = (angleDeg + 25f) * MathUtils.degreesToRadians;

            float tx = cx + len2 * MathUtils.cos(tipRad);
            float ty = cy + len2 * MathUtils.sin(tipRad);
            float lx = cx + baseW2 * MathUtils.cos(leftRad);
            float ly = cy + baseW2 * MathUtils.sin(leftRad);
            float rx = cx + baseW2 * MathUtils.cos(rightRad);
            float ry = cy + baseW2 * MathUtils.sin(rightRad);

            shapeRenderer.setColor(1.0f, 0.88f, 0.45f, 0.85f);
            shapeRenderer.triangle(cx, cy, tx, ty, lx, ly);

            shapeRenderer.setColor(0.48f, 0.35f, 0.15f, 0.85f);
            shapeRenderer.triangle(cx, cy, tx, ty, rx, ry);
        }

        // Center Gold Jewel Boss
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cx, cy, 6f, 16);
    }

    private void drawAntiqueCompassRoseLines(float w, float h, float yaw) {
        float cx = w - 105f;
        float cy = h - 105f;
        float r = 70f;

        // Outer ornate gold rims
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.circle(cx, cy, r, 54);
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.circle(cx, cy, r - 6f, 48);

        // Degree tick marks
        for (int i = 0; i < 360; i += 15) {
            float tickRad = (i - yaw + 90f) * MathUtils.degreesToRadians;
            float inner = (i % 90 == 0) ? r - 12f : ((i % 45 == 0) ? r - 9f : r - 5f);
            shapeRenderer.setColor(i == 0 ? healthRed : goldBorder);
            shapeRenderer.line(
                cx + inner * MathUtils.cos(tickRad),
                cy + inner * MathUtils.sin(tickRad),
                cx + r * MathUtils.cos(tickRad),
                cy + r * MathUtils.sin(tickRad)
            );
        }

        // Waypoint Ticker Border
        float pillW = 165f;
        float pillH = 28f;
        float pillX = cx - r - pillW - 14f;
        float pillY = cy - pillH / 2f;
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(pillX, pillY, pillW, pillH);
    }

    private void drawAntiqueCompassRoseText(float w, float h, float yaw) {
        float cx = w - 105f;
        float cy = h - 105f;
        float r = 70f;

        // Cardinal Letters (N, E, S, W)
        String[] cardinals = {"N", "E", "S", "W"};
        Color[] cardColors = {healthRed, goldAccent, goldAccent, goldAccent};

        for (int i = 0; i < 4; i++) {
            float rad = (i * 90f - yaw + 90f) * MathUtils.degreesToRadians;
            float lx = cx + (r - 18f) * MathUtils.cos(rad) - 5f;
            float ly = cy + (r - 18f) * MathUtils.sin(rad) + 5f;

            fonts.smallFont.setColor(cardColors[i]);
            fonts.smallFont.draw(spriteBatch, cardinals[i], lx, ly);
        }

        // Degree readout beneath
        int displayYaw = (int) ((yaw % 360 + 360) % 360);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, displayYaw + "°", cx - 10f, cy - r - 8f);

        // Waypoint Ticker Text ("CURZON HALL   W")
        float pillW = 165f;
        float pillX = cx - r - pillW - 14f;
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "CURZON HALL   W", pillX + 28f, cy + 6f);
    }

    // ========================================================
    // 3. BOTTOM-LEFT FLUID CURVED HEALTH BAR (Matching Mockup)
    // ========================================================
    private void drawCurvedHealthBarBg(PlayerController player, float w, float h) {
        float bx = 38f;
        float by = 38f;
        float barW = 280f;
        float barH = 18f;

        // Ornate Left Filigree Wing (Tip)
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.triangle(bx - 18f, by + barH / 2f, bx + 2f, by + barH + 4f, bx + 2f, by - 4f);

        // Health Bar Track
        shapeRenderer.setColor(healthBg);
        shapeRenderer.rect(bx, by, barW, barH);

        // Fluid Gradient Fill (Gold into Crimson Red)
        float hpRatio = MathUtils.clamp(player.getHealth() / player.getMaxHealth(), 0f, 1f);
        float fillW = barW * hpRatio;
        shapeRenderer.rect(bx, by, fillW, barH, goldAccent, healthRed, healthRed, goldAccent);

        // Secondary Stamina Bar Underneath (in Jade Cyan)
        float stY = by - 8f;
        float stH = 4f;
        shapeRenderer.setColor(staminaBg);
        shapeRenderer.rect(bx + 15f, stY, barW - 15f, stH);

        float stRatio = MathUtils.clamp(player.getStamina() / player.getMaxStamina(), 0f, 1f);
        shapeRenderer.setColor(staminaGreen);
        shapeRenderer.rect(bx + 15f, stY, (barW - 15f) * stRatio, stH);
    }

    private void drawCurvedHealthBarBorders(PlayerController player, float w, float h) {
        float bx = 38f;
        float by = 38f;
        float barW = 280f;
        float barH = 18f;

        // Curved Gold Bar Outline
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(bx, by, barW, barH);

        // Top decorative accent line
        shapeRenderer.line(bx - 12f, by + barH + 2f, bx + barW + 10f, by + barH + 2f);

        // Secondary Stamina border
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(bx + 15f, by - 8f, barW - 15f, 4f);
    }

    private void drawCurvedHealthBarText(PlayerController player, float w, float h) {
        float bx = 38f;
        float by = 38f;
        float barW = 280f;

        // Numeric HP readout "100/100" in crisp gold/white
        fonts.keyFont.setColor(Color.WHITE);
        String hpStr = (int) player.getHealth() + "/100";
        fonts.keyFont.draw(spriteBatch, hpStr, bx + barW - 55f, by + 14f);
    }

    // ========================================================
    // 4. CENTER SPEECH-BUBBLE INTERACTION PROMPT (Matching Mockup)
    // ========================================================
    private void drawSpeechBubblePromptBg(float w, float h) {
        float promptW = 460f;
        float promptH = 58f;
        float px = (w - promptW) / 2f + 140f; // Offset slightly right, pointing towards central memorial
        float py = h * 0.44f;

        // Dark glass rounded box
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.88f);
        shapeRenderer.rect(px, py, promptW, promptH);

        // Left Pointer Beak (Speech bubble triangle pointing to Curzon archive)
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.88f);
        shapeRenderer.triangle(px, py + 34f, px, py + 18f, px - 16f, py + 26f);
    }

    private void drawSpeechBubblePromptBorders(float w, float h) {
        float promptW = 460f;
        float promptH = 58f;
        float px = (w - promptW) / 2f + 140f;
        float py = h * 0.44f;

        // Gold border with subtle pulse
        float pulse = 0.7f + 0.3f * MathUtils.sin(animTime * 4.5f);
        shapeRenderer.setColor(1.0f, 0.84f * pulse, 0.38f, 1f);

        shapeRenderer.rect(px, py, promptW, promptH);

        // Pointer Beak lines
        shapeRenderer.line(px, py + 34f, px - 16f, py + 26f);
        shapeRenderer.line(px - 16f, py + 26f, px, py + 18f);
    }

    private void drawSpeechBubblePromptText(MemorialEntry nearby, float w, float h) {
        float promptW = 460f;
        float px = (w - promptW) / 2f + 140f;
        float py = h * 0.44f;

        // Exact text from mockup!
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "[E]  INSPECT HISTORICAL ARCHIVE", px + 22f, py + 42f);

        fonts.smallFont.setColor(new Color(0.92f, 0.92f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch, "— 36 JULY STUDENT MASS MOVEMENT", px + 62f, py + 20f);
    }

    // ========================================================
    // 5. BOTTOM-RIGHT SLEEK KEYCAPS (Matching Mockup)
    // ========================================================
    private void drawKeycapsBg(float w, float h) {
        float kBaseX = w - 240f;
        float kBaseY = 16f;

        // Keycap button boxes
        shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.85f);

        // [W]
        shapeRenderer.rect(kBaseX + 68f, kBaseY + 84f, 28f, 26f);
        // [A] [S] [D]
        shapeRenderer.rect(kBaseX + 36f, kBaseY + 54f, 28f, 26f);
        shapeRenderer.rect(kBaseX + 68f, kBaseY + 54f, 28f, 26f);
        shapeRenderer.rect(kBaseX + 100f, kBaseY + 54f, 28f, 26f);

        // [SHIFT]
        shapeRenderer.rect(kBaseX + 140f, kBaseY + 54f, 48f, 26f);

        // [SPACE]
        shapeRenderer.rect(kBaseX + 16f, kBaseY + 26f, 62f, 22f);

        // [E]
        shapeRenderer.rect(kBaseX + 130f, kBaseY + 26f, 28f, 22f);

        // [M]
        shapeRenderer.rect(kBaseX + 44f, kBaseY, 26f, 20f);

        // [ESC]
        shapeRenderer.rect(kBaseX + 114f, kBaseY, 36f, 20f);
    }

    private void drawKeycapsBorders(float w, float h) {
        float kBaseX = w - 240f;
        float kBaseY = 16f;

        shapeRenderer.setColor(goldBorder);

        // [W]
        shapeRenderer.rect(kBaseX + 68f, kBaseY + 84f, 28f, 26f);
        // [A] [S] [D]
        shapeRenderer.rect(kBaseX + 36f, kBaseY + 54f, 28f, 26f);
        shapeRenderer.rect(kBaseX + 68f, kBaseY + 54f, 28f, 26f);
        shapeRenderer.rect(kBaseX + 100f, kBaseY + 54f, 28f, 26f);

        // [SHIFT]
        shapeRenderer.rect(kBaseX + 140f, kBaseY + 54f, 48f, 26f);

        // [SPACE]
        shapeRenderer.rect(kBaseX + 16f, kBaseY + 26f, 62f, 22f);

        // [E]
        shapeRenderer.rect(kBaseX + 130f, kBaseY + 26f, 28f, 22f);

        // [M]
        shapeRenderer.rect(kBaseX + 44f, kBaseY, 26f, 20f);

        // [ESC]
        shapeRenderer.rect(kBaseX + 114f, kBaseY, 36f, 20f);
    }

    private void drawKeycapsText(float w, float h) {
        float kBaseX = w - 240f;
        float kBaseY = 16f;

        // Button letter labels
        fonts.keyFont.setColor(goldAccent);
        fonts.keyFont.draw(spriteBatch, "W", kBaseX + 76f, kBaseY + 102f);
        fonts.keyFont.draw(spriteBatch, "A", kBaseX + 44f, kBaseY + 72f);
        fonts.keyFont.draw(spriteBatch, "S", kBaseX + 77f, kBaseY + 72f);
        fonts.keyFont.draw(spriteBatch, "D", kBaseX + 108f, kBaseY + 72f);

        fonts.keyFont.draw(spriteBatch, "SHIFT", kBaseX + 146f, kBaseY + 72f);
        fonts.keyFont.draw(spriteBatch, "SPACE", kBaseX + 26f, kBaseY + 42f);
        fonts.keyFont.draw(spriteBatch, "E", kBaseX + 139f, kBaseY + 42f);
        fonts.keyFont.draw(spriteBatch, "M", kBaseX + 51f, kBaseY + 15f);
        fonts.keyFont.draw(spriteBatch, "ESC", kBaseX + 118f, kBaseY + 15f);

        // Action sub-labels matching mockup
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "MOVE", kBaseX - 6f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "SPRINT", kBaseX + 194f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "JUMP", kBaseX + 84f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "INTERACT", kBaseX + 164f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "MAP", kBaseX + 75f, kBaseY + 15f);
        fonts.smallFont.draw(spriteBatch, "PAUSE", kBaseX + 156f, kBaseY + 15f);
    }

    // ==========================================
    // HISTORICAL MEMORIAL MODAL
    // ==========================================
    private void renderMemorialModal(MemorialEntry entry, float w, float h) {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(0, 0, w, h);

        float mw = 720f;
        float mh = 440f;
        float mx = (w - mw) / 2f;
        float my = (h - mh) / 2f;

        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(mx, my, mw, mh);

        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.94f);
        shapeRenderer.rect(mx, my + mh - 58f, mw, 58f);

        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(mx, my + mh - 4f, mw, 4f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mx, my, mw, mh);
        shapeRenderer.line(mx, my + mh - 58f, mx + mw, my + mh - 58f);
        shapeRenderer.line(mx + 30f, my + 60f, mx + mw - 30f, my + 60f);
        shapeRenderer.end();

        spriteBatch.begin();
        float textX = mx + 40f;
        float textTop = my + mh - 22f;

        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, entry.title, textX, textTop);

        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.90f, 1f));
        fonts.smallFont.draw(spriteBatch, entry.date + "   |   " + entry.location, textX, textTop - 50f);

        fonts.bodyFont.setColor(new Color(0.95f, 0.95f, 0.95f, 1f));
        fonts.bodyFont.draw(spriteBatch, entry.description, textX, textTop - 92f, mw - 80f, 10, true);

        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "HISTORICAL SIGNIFICANCE", textX, my + 135f);
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        fonts.smallFont.draw(spriteBatch,
            "Documented as part of the student-led democratic reform movement for meritocracy and justice in Bangladesh.",
            textX, my + 105f, mw - 80f, 10, true);

        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[E] or [ESC] Close & Continue Exploration", textX + 140f, my + 42f);
        spriteBatch.end();
    }

    // ==========================================
    // CAMPUS TACTICAL MAP OVERLAY
    // ==========================================
    private void renderCampusMap(PlayerController player, JulyMemorials memorials, float cameraYaw, float w, float h) {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.84f);
        shapeRenderer.rect(0, 0, w, h);

        float mapW = 640f;
        float mapH = 520f;
        float mx = (w - mapW) / 2f;
        float my = (h - mapH) / 2f;

        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(mx, my, mapW, mapH);

        shapeRenderer.setColor(new Color(0.68f, 0.22f, 0.16f, 0.85f));
        float centerMapX = mx + mapW / 2f;
        float centerMapY = my + mapH / 2f + 40f;
        float scale = 2.6f;

        shapeRenderer.rect(centerMapX - 45f * scale / 2f, centerMapY + 32f * scale - 14f * scale / 2f, 45f * scale, 14f * scale);

        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(centerMapX - 3.5f * scale / 2f, centerMapY - 50f * scale, 3.5f * scale, 82f * scale);
        shapeRenderer.rect(centerMapX - 60f * scale, centerMapY - 18f * scale - 2.5f * scale / 2f, 120f * scale, 2.5f * scale);

        for (MemorialEntry entry : memorials.getEntries()) {
            float pointX = centerMapX + entry.position.x * scale;
            float pointY = centerMapY - entry.position.z * scale;
            shapeRenderer.setColor(entry.inspected ? Color.GREEN : goldAccent);
            shapeRenderer.circle(pointX, pointY, 6.5f, 16);
        }

        Vector3 ppos = player.getPosition();
        float pMapX = centerMapX + ppos.x * scale;
        float pMapY = centerMapY - ppos.z * scale;
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(pMapX, pMapY, 5.5f, 16);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mx, my, mapW, mapH);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "DHAKA UNIVERSITY CAMPUS MAP", mx + 30f, my + mapH - 24f);

        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "[Green] Unlocked Archive    [Gold] Active Objective    [Cyan] Player", mx + 30f, my + 38f);
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[M] Close Map", mx + mapW - 140f, my + 38f);
        spriteBatch.end();
    }

    // ==========================================
    // PAUSE MENU
    // ==========================================
    private void renderPauseMenu(float w, float h) {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(0, 0, w, h);

        float pw = 420f;
        float ph = 290f;
        float px = (w - pw) / 2f;
        float py = (h - ph) / 2f;

        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "GAME PAUSED", px + 115f, py + ph - 40f);

        fonts.headerFont.setColor(Color.WHITE);
        fonts.headerFont.draw(spriteBatch, "Press [ESC] to Resume", px + 105f, py + 155f);
        fonts.headerFont.draw(spriteBatch, "Press [M] for Campus Map", px + 95f, py + 115f);
        fonts.smallFont.setColor(goldMuted);
        fonts.smallFont.draw(spriteBatch, "2x12: Spark — Birth of 36 July", px + 108f, py + 52f);
        spriteBatch.end();
    }

    public boolean isModalOpen() {
        return activeModalEntry != null || isMapOpen || isPauseMenuOpen;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
