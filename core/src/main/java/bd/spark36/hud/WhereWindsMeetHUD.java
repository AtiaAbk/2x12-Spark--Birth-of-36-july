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
import com.badlogic.gdx.utils.Array;
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

    // Theme Colors (Ultra-Transparent Glassmorphism matching concept board)
    private final Color glassBg = new Color(0.02f, 0.04f, 0.06f, 0.16f);
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
    private boolean isVictoryOpen = false;
    private boolean victoryShown = false;
    private float animTime = 0f;

    // Toast Notification Banner (triggered on memorial inspection)
    private float missionBannerTime = 0f;
    private String missionBannerTitle = "";
    private int missionBannerCount = 0;

    // Exit actions: 0=none, 1=return to main menu, 2=quit game, 3=replay level
    private int exitAction = 0;

    public WhereWindsMeetHUD(FontRenderer fontRenderer) {
        this.fonts = fontRenderer;
    }

    public void update(float delta, PlayerController player, JulyMemorials memorials) {
        animTime += delta;
        // Only count down toast banner when modal is NOT blocking view!
        if (missionBannerTime > 0f && activeModalEntry == null) {
            missionBannerTime -= delta;
        }

        // Handle Victory Screen input
        if (isVictoryOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                exitAction = 1; // Return to Main Menu
                isVictoryOpen = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                exitAction = 3; // Replay Level 1
                isVictoryOpen = false;
                victoryShown = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                exitAction = 2; // Quit Game
                isVictoryOpen = false;
            }
            return;
        }

        // Shortcut key [K] for instant mission completion / testing victory
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && !isVictoryOpen && activeModalEntry == null) {
            for (MemorialEntry e : memorials.getEntries()) {
                e.inspected = true;
            }
            missionBannerTime = 6.0f;
            missionBannerTitle = "ALL HISTORICAL ARCHIVES";
            missionBannerCount = 5;
            isVictoryOpen = true;
            victoryShown = true;
            return;
        }

        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());

        if (nearby != null && (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
            if (activeModalEntry == null) {
                activeModalEntry = nearby;
                nearby.inspected = true;
                missionBannerTitle = nearby.title;
                missionBannerCount = memorials.getInspectedCount();
            } else {
                activeModalEntry = null;
                missionBannerTime = 6.0f; // Start 6-second mission complete celebration!
                // If all memorials inspected and victory hasn't triggered yet, open Victory Screen!
                if (memorials.isAllInspected() && !victoryShown) {
                    isVictoryOpen = true;
                    victoryShown = true;
                }
            }
        }

        if (activeModalEntry != null && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            activeModalEntry = null;
            missionBannerTime = 6.0f;
            if (memorials.isAllInspected() && !victoryShown) {
                isVictoryOpen = true;
                victoryShown = true;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (activeModalEntry == null && !isVictoryOpen) {
                isMapOpen = !isMapOpen;
            }
        }

        if (activeModalEntry == null && !isMapOpen && !isVictoryOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPauseMenuOpen = !isPauseMenuOpen;
        }

        // Pause menu exit actions
        if (isPauseMenuOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                exitAction = 1; // Return to main menu
                isPauseMenuOpen = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                exitAction = 2; // Quit game
                isPauseMenuOpen = false;
            }
        }
    }

    /**
     * Returns and clears the pending exit action.
     * @return 0=none, 1=return to main menu, 2=quit game
     */
    public int consumeExitAction() {
        int action = exitAction;
        exitAction = 0;
        return action;
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw) {
        render(player, memorials, cameraYaw, null);
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw, com.badlogic.gdx.graphics.Camera camera) {
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

        // 0. Draw 3D In-World Waypoint Pin directly above next objective
        if (camera != null && nearest != null && !memorials.isAllInspected() && !isModalOpen()) {
            draw3DObjectivePin(nearest, player, camera, w, h);
        }

        // 1. Draw HUD Background Shapes & Ornate Geometry
        shapeRenderer.begin(ShapeType.Filled);
        drawMissionCardBg(memorials, w, h);
        drawMissionChecklistBg(player, memorials, w, h);
        drawAntiqueCompassRoseFilled(w, h, cameraYaw);
        drawKeycapsBg(w, h);

        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());
        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen && !isVictoryOpen) {
            drawSpeechBubblePromptBg(w, h);
        }
        shapeRenderer.end();

        // 2. Draw HUD Outlines, Accents & Filigree
        shapeRenderer.begin(ShapeType.Line);
        drawMissionCardBorders(w, h);
        drawMissionChecklistBorders(player, memorials, w, h);
        drawAntiqueCompassRoseLines(w, h, cameraYaw);
        drawKeycapsBorders(w, h);

        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen && !isVictoryOpen) {
            drawSpeechBubblePromptBorders(w, h);
        }
        shapeRenderer.end();

        // 3. Draw Typography & Glyphs
        spriteBatch.begin();
        drawMissionCardText(memorials, nearest, dstToNearest, w, h);
        drawMissionChecklistText(player, memorials, w, h);
        drawAntiqueCompassRoseText(w, h, cameraYaw);
        drawKeycapsText(w, h);

        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen && !isVictoryOpen) {
            drawSpeechBubblePromptText(nearby, w, h);
        }
        spriteBatch.end();

        // 4. Toast Notification Banner (renders on top of HUD when active)
        if (!isVictoryOpen && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            renderMissionCompleteBanner(w, h);
        }

        // 5. Overlays: Historical Archival Modal, Map, Pause Menu, or Victory Screen
        if (isVictoryOpen) {
            renderVictoryScreen(w, h);
        } else if (activeModalEntry != null) {
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
    private void drawMissionCardBg(JulyMemorials memorials, float w, float h) {
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

        // Glowing Golden Progress Bar (Dynamic: fills as archives are inspected)
        float barX = cardX + 16f;
        float barY = cardY + 48f;
        float barW = 300f;
        float barH = 5f;

        // Track
        shapeRenderer.setColor(0.18f, 0.16f, 0.12f, 0.9f);
        shapeRenderer.rect(barX, barY, barW, barH);

        // Gold/Green Fill with Gleam based on actual mission progress
        int inspected = memorials.getInspectedCount();
        int total = memorials.getTotalCount();
        float progress = total > 0 ? (float) inspected / (float) total : 0f;
        float fillW = barW * progress;

        if (fillW > 0f) {
            shapeRenderer.setColor(inspected >= total ? Color.GREEN : goldAccent);
            shapeRenderer.rect(barX, barY, fillW, barH);

            // Gleam particle
            float gleamPos = (MathUtils.sin(animTime * 2.5f) + 1f) * 0.5f * fillW;
            shapeRenderer.setColor(1.0f, 0.98f, 0.85f, 0.95f);
            shapeRenderer.circle(barX + gleamPos, barY + barH / 2f, 4f, 12);
        }

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

        int ins = memorials.getInspectedCount();
        int total = memorials.getTotalCount();
        boolean allDone = ins >= total;

        // 1. ACTIVE MISSION TAG
        fonts.smallFont.setColor(allDone ? Color.GREEN : goldAccent);
        String missionTag = allDone ?
            "ALL ARCHIVES UNLOCKED: 05 / 05" :
            String.format("ACTIVE MISSION: %02d / %02d", Math.min(ins + 1, total), total);
        fonts.smallFont.draw(spriteBatch, missionTag, cardX + 28f, cardY + 148f);

        // 2. MISSION TITLE
        fonts.titleFont.setColor(Color.WHITE);
        String missionTitle = allDone ? "LEVEL 1 ACCOMPLISHED" : (nearest != null ? nearest.title : "EXPLORE CAMPUS");
        fonts.titleFont.draw(spriteBatch, missionTitle, cardX + 16f, cardY + 124f);

        // 3. Subtitle description / location
        fonts.bodyFont.setColor(new Color(0.88f, 0.88f, 0.90f, 1f));
        String missionDesc = allDone ?
            "All 5 historical archives secured. Victory achieved!" :
            (nearest != null ? "Target: " + nearest.location : "Investigate Dhaka University archives");
        fonts.bodyFont.draw(spriteBatch, missionDesc, cardX + 16f, cardY + 92f);

        // 4. DISTANCE
        fonts.headerFont.setColor(goldAccent);
        String distStr = allDone ? "STATUS: COMPLETED" : String.format("DISTANCE: %.1f M", dst);
        fonts.headerFont.draw(spriteBatch, distStr, cardX + 16f, cardY + 30f);
    }

    // =======================================================
    // 2. TOP-RIGHT ANTIQUE 8-POINT COMPASS ROSE (Matching Mockup)
    // =======================================================
    private void drawAntiqueCompassRoseFilled(float w, float h, float yaw) {
        float cx = w - 105f;
        float cy = h - 105f;
        float r = 70f;

        // Circular transparent glass background
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.16f);
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
        fonts.smallFont.draw(spriteBatch, displayYaw + " deg", cx - 18f, cy - r - 8f);

        // Waypoint Ticker Text ("CURZON HALL   W")
        float pillW = 165f;
        float pillX = cx - r - pillW - 14f;
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "CURZON HALL   W", pillX + 28f, cy + 6f);
    }

    // ========================================================
    // 3. BOTTOM-LEFT LIVE MISSION PARAMETERS CHECKLIST & COUNTER
    // ========================================================
    private void drawMissionChecklistBg(PlayerController player, JulyMemorials memorials, float w, float h) {
        float bx = 36f;
        float by = 36f;
        float bw = 390f;
        float bh = 158f;

        // Transparent glass backing (zero dark black box!)
        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(bx, by, bw, bh);

        // Progress bar track at bottom of checklist
        float barX = bx + 16f;
        float barY = by + 12f;
        float barW = bw - 32f;
        float barH = 5f;

        shapeRenderer.setColor(0.12f, 0.14f, 0.16f, 0.40f);
        shapeRenderer.rect(barX, barY, barW, barH);

        int ins = memorials.getInspectedCount();
        int tot = memorials.getTotalCount();
        float fillRatio = tot > 0 ? (float) ins / (float) tot : 0f;
        float fillW = barW * fillRatio;

        if (fillW > 0f) {
            Color fillC = ins >= tot ? Color.GREEN : staminaGreen;
            shapeRenderer.setColor(fillC);
            shapeRenderer.rect(barX, barY, fillW, barH);
        }
    }

    private void drawMissionChecklistBorders(PlayerController player, JulyMemorials memorials, float w, float h) {
        float bx = 36f;
        float by = 36f;
        float bw = 390f;
        float bh = 158f;

        // Thin outer gold line
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(bx, by, bw, bh);

        // Ornate Corner Brackets
        shapeRenderer.setColor(goldAccent);
        float cLen = 14f;
        shapeRenderer.line(bx - 2, by + bh + 2, bx + cLen, by + bh + 2);
        shapeRenderer.line(bx - 2, by + bh + 2, bx - 2, by + bh - cLen);
        shapeRenderer.line(bx + bw + 2, by + bh + 2, bx + bw - cLen, by + bh + 2);
        shapeRenderer.line(bx + bw + 2, by + bh + 2, bx + bw + 2, by + bh - cLen);
        shapeRenderer.line(bx - 2, by - 2, bx + cLen, by - 2);
        shapeRenderer.line(bx - 2, by - 2, bx - 2, by + cLen);
        shapeRenderer.line(bx + bw + 2, by - 2, bx + bw - cLen, by - 2);
        shapeRenderer.line(bx + bw + 2, by - 2, bx + bw + 2, by + cLen);

        // Separator below title
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.line(bx + 14f, by + bh - 28f, bx + bw - 14f, by + bh - 28f);
    }

    private void drawMissionChecklistText(PlayerController player, JulyMemorials memorials, float w, float h) {
        float bx = 36f;
        float by = 36f;
        float bw = 390f;
        float bh = 158f;

        int ins = memorials.getInspectedCount();
        int tot = memorials.getTotalCount();

        // 1. Header: "* MISSION PARAMETERS: 01 / 05"
        String headerTitle = String.format("* MISSION PARAMETERS: %02d / %02d", ins, tot);
        fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.keyFont.draw(spriteBatch, headerTitle, bx + 17f, by + bh - 11f);
        fonts.keyFont.setColor(ins >= tot ? Color.GREEN : goldAccent);
        fonts.keyFont.draw(spriteBatch, headerTitle, bx + 16f, by + bh - 10f);

        // 2. Checklist of the 5 Memorial checkpoints
        String[] shortNames = {
            "1. Curzon Hall Central Arcade",
            "2. Language Monument Plaza",
            "3. TSC Raju Anti-Terrorism Sculpture",
            "4. Women's Hall Quadrangle",
            "5. 36 July Gateway"
        };

        Array<MemorialEntry> entries = memorials.getEntries();
        MemorialEntry nextObj = memorials.getNextObjective(player.getPosition());
        float startY = by + bh - 38f;
        float itemSpacing = 18f;

        for (int i = 0; i < entries.size && i < shortNames.length; i++) {
            MemorialEntry entry = entries.get(i);
            float y = startY - i * itemSpacing;
            if (entry.inspected) {
                // Completed: green [OK]
                fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
                fonts.smallFont.draw(spriteBatch, "[OK] " + shortNames[i], bx + 17f, y - 1f);
                fonts.smallFont.setColor(Color.GREEN);
                fonts.smallFont.draw(spriteBatch, "[OK] " + shortNames[i], bx + 16f, y);
            } else if (entry == nextObj) {
                // Active objective: pulsing gold with pointer arrow and distance!
                float pulse = 0.8f + 0.2f * MathUtils.sin(animTime * 5f);
                float dst = player.getPosition().dst(entry.position);
                String line = String.format("[>]  %s (%.0fm)", shortNames[i], dst);
                fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
                fonts.smallFont.draw(spriteBatch, line, bx + 17f, y - 1f);
                fonts.smallFont.setColor(1f, 0.85f * pulse, 0.35f, 1f);
                fonts.smallFont.draw(spriteBatch, line, bx + 16f, y);
            } else {
                // Upcoming
                fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
                fonts.smallFont.draw(spriteBatch, "[  ] " + shortNames[i], bx + 17f, y - 1f);
                fonts.smallFont.setColor(new Color(0.95f, 0.95f, 0.98f, 0.95f));
                fonts.smallFont.draw(spriteBatch, "[  ] " + shortNames[i], bx + 16f, y);
            }
        }

        // 3. Progress percentage
        int pct = tot > 0 ? (ins * 100 / tot) : 0;
        String pctStr = String.format("PROGRESS: %d%% (%d/5 COMPLETE)", pct, ins);
        fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.smallFont.draw(spriteBatch, pctStr, bx + bw - 194f, by + 29f);
        fonts.smallFont.setColor(ins >= tot ? Color.GREEN : goldAccent);
        fonts.smallFont.draw(spriteBatch, pctStr, bx + bw - 195f, by + 30f);
    }

    // In-world 3D Waypoint Pin floating above active objective
    private void draw3DObjectivePin(MemorialEntry target, PlayerController player, com.badlogic.gdx.graphics.Camera camera, float w, float h) {
        if (target == null || camera == null) return;

        Vector3 pinPos = new Vector3(target.position.x, 4.0f, target.position.z);
        Vector3 scr = camera.project(pinPos);

        if (scr.z > 0f && scr.z < 1.0f) {
            float px = scr.x * (w / Gdx.graphics.getBackBufferWidth());
            float py = scr.y * (h / Gdx.graphics.getBackBufferHeight());

            float bob = MathUtils.sin(animTime * 4f) * 4f;
            py += bob;

            float dst = player.getPosition().dst(target.position);

            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(goldAccent);
            float ds = 7f;
            shapeRenderer.triangle(px, py + ds, px + ds, py, px, py - ds);
            shapeRenderer.triangle(px, py + ds, px - ds, py, px, py - ds);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(px, py, ds + 3f, 16);
            shapeRenderer.end();

            spriteBatch.begin();
            String pinText = String.format("> %s (%.0fm)", target.title, dst);
            fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.keyFont.draw(spriteBatch, pinText, px - 78f, py + 24f);
            fonts.keyFont.setColor(goldAccent);
            fonts.keyFont.draw(spriteBatch, pinText, px - 80f, py + 25f);
            spriteBatch.end();
        }
    }

    // ========================================================
    // 4. CENTER SPEECH-BUBBLE INTERACTION PROMPT (Matching Mockup)
    // ========================================================
    private void drawSpeechBubblePromptBg(float w, float h) {
        // Zero solid background: completely transparent campus view!
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

        // Text with drop shadows
        fonts.headerFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.headerFont.draw(spriteBatch, "[E]  INSPECT HISTORICAL ARCHIVE", px + 23f, py + 41f);
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "[E]  INSPECT HISTORICAL ARCHIVE", px + 22f, py + 42f);

        fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.smallFont.draw(spriteBatch, "-- 36 JULY STUDENT MASS MOVEMENT", px + 63f, py + 19f);
        fonts.smallFont.setColor(new Color(0.95f, 0.92f, 0.80f, 1f));
        fonts.smallFont.draw(spriteBatch, "-- 36 JULY STUDENT MASS MOVEMENT", px + 62f, py + 20f);
    }

    // ========================================================
    // 5. BOTTOM-RIGHT SLEEK KEYCAPS (Matching Mockup)
    // ========================================================
    private void drawKeycapsBg(float w, float h) {
        float kBaseX = w - 240f;
        float kBaseY = 16f;

        // Keycap button boxes (sleek transparent)
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.20f);

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
        fonts.keyFont.draw(spriteBatch, "SPACE", kBaseX + 24f, kBaseY + 42f);
        fonts.keyFont.draw(spriteBatch, "E", kBaseX + 139f, kBaseY + 42f);
        fonts.keyFont.draw(spriteBatch, "M", kBaseX + 51f, kBaseY + 15f);
        fonts.keyFont.draw(spriteBatch, "ESC", kBaseX + 118f, kBaseY + 15f);

        // Action sub-labels matching updated keybinds:
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "MOVE", kBaseX - 6f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "SPRINT / JUMP", kBaseX + 194f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "JUMP / RUN", kBaseX + 74f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "INTERACT", kBaseX + 164f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "MAP", kBaseX + 75f, kBaseY + 15f);
        fonts.smallFont.draw(spriteBatch, "PAUSE", kBaseX + 156f, kBaseY + 15f);
    }

    // ==========================================
    // HISTORICAL MEMORIAL FLOATING OVERLAY (100% Transparent, No Black Box)
    // ==========================================
    private void renderMemorialModal(MemorialEntry entry, float w, float h) {
        // ZERO full-screen black overlay: Curzon Hall, flag, and 3D campus remain 100% visible!
        float mw = 780f;
        float mh = 225f;
        float mx = (w - mw) / 2f;
        float my = 48f; // Floating in lower area of screen

        // Thin golden top line
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(mx, my + mh - 2f, mw, 2f);
        shapeRenderer.end();

        // Golden Corner brackets
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        float cLen = 16f;
        shapeRenderer.line(mx, my + mh, mx + cLen, my + mh);
        shapeRenderer.line(mx, my + mh, mx, my + mh - cLen);
        shapeRenderer.line(mx + mw, my + mh, mx + mw - cLen, my + mh);
        shapeRenderer.line(mx + mw, my + mh, mx + mw, my + mh - cLen);
        shapeRenderer.line(mx, my, mx + cLen, my);
        shapeRenderer.line(mx, my, mx, my + cLen);
        shapeRenderer.line(mx + mw, my, mx + mw - cLen, my);
        shapeRenderer.line(mx + mw, my, mx + mw, my + cLen);
        shapeRenderer.end();

        // Essential Text with drop shadow for 100% readability directly against 3D world
        spriteBatch.begin();
        float textX = mx + 25f;

        // Header Title
        fonts.titleFont.setColor(0f, 0f, 0f, 0.92f);
        fonts.titleFont.draw(spriteBatch, entry.title, textX + 2f, my + mh - 16f);
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, entry.title, textX, my + mh - 14f);

        // Subtitle: Date & Location
        fonts.smallFont.setColor(0f, 0f, 0f, 0.92f);
        fonts.smallFont.draw(spriteBatch, entry.date + "   |   " + entry.location, textX + 1f, my + mh - 45f);
        fonts.smallFont.setColor(new Color(0.95f, 0.90f, 0.70f, 1f));
        fonts.smallFont.draw(spriteBatch, entry.date + "   |   " + entry.location, textX, my + mh - 44f);

        // Description narrative
        fonts.bodyFont.setColor(0f, 0f, 0f, 0.92f);
        fonts.bodyFont.draw(spriteBatch, entry.description, textX + 1.5f, my + mh - 73.5f, mw - 50f, 10, true);
        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch, entry.description, textX, my + mh - 72f, mw - 50f, 10, true);

        // Close prompt
        fonts.promptFont.setColor(0f, 0f, 0f, 0.92f);
        fonts.promptFont.draw(spriteBatch, "[E] / [ESC] Close Archive", mx + mw - 218f, my + 27f);
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[E] / [ESC] Close Archive", mx + mw - 220f, my + 28f);

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
        float ph = 360f;
        float px = (w - pw) / 2f;
        float py = (h - ph) / 2f;

        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(px, py, pw, ph);

        // Gold accent bar at top
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(px, py + ph - 4f, pw, 4f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(px, py, pw, ph);
        // Separator line below title
        shapeRenderer.line(px + 20f, py + ph - 62f, px + pw - 20f, py + ph - 62f);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "GAME PAUSED", px + 115f, py + ph - 22f);

        fonts.headerFont.setColor(Color.WHITE);
        fonts.headerFont.draw(spriteBatch, "[ESC]  Resume Game", px + 80f, py + 250f);
        fonts.headerFont.draw(spriteBatch, "[M]    Campus Map", px + 80f, py + 210f);

        fonts.headerFont.setColor(new Color(1f, 0.75f, 0.35f, 1f));
        fonts.headerFont.draw(spriteBatch, "[Q]    Exit to Main Menu", px + 80f, py + 160f);

        fonts.headerFont.setColor(new Color(0.95f, 0.35f, 0.30f, 1f));
        fonts.headerFont.draw(spriteBatch, "[X]    Quit Game", px + 80f, py + 120f);

        fonts.smallFont.setColor(goldMuted);
        fonts.smallFont.draw(spriteBatch, "2x12: Spark -- Birth of 36 July", px + 108f, py + 52f);
        spriteBatch.end();
    }

    // ==========================================
    // TOAST NOTIFICATION: MISSION COMPLETE BANNER
    // ==========================================
    // TOAST NOTIFICATION: MISSION COMPLETE BANNER
    // ==========================================
    private void renderMissionCompleteBanner(float w, float h) {
        if (missionBannerTime <= 0f) return;

        // Smooth alpha fade: 0.8s fade in, 0.8s fade out
        float alpha = 1f;
        if (missionBannerTime > 5.2f) {
            alpha = (6.0f - missionBannerTime) / 0.8f;
        } else if (missionBannerTime < 0.8f) {
            alpha = missionBannerTime / 0.8f;
        }
        alpha = MathUtils.clamp(alpha, 0f, 1f);

        float bw = 640f;
        float bh = 76f;
        float bx = (w - bw) / 2f;
        float by = h - 105f;

        // Transparent glass background (zero dark block!)
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.01f, 0.02f, 0.04f, 0.20f * alpha);
        shapeRenderer.rect(bx, by, bw, bh);

        // Gold top accent
        shapeRenderer.setColor(1.0f, 0.85f, 0.40f, 0.95f * alpha);
        shapeRenderer.rect(bx, by + bh - 3f, bw, 3f);

        // Gold diamond icon on left
        float dx = bx + 28f;
        float dy = by + bh / 2f;
        float ds = 8f;
        shapeRenderer.triangle(dx, dy + ds, dx + ds, dy, dx, dy - ds);
        shapeRenderer.triangle(dx, dy + ds, dx - ds, dy, dx, dy - ds);
        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(0.92f, 0.76f, 0.32f, 0.90f * alpha);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();

        // Text with drop shadows
        spriteBatch.begin();
        fonts.titleFont.setColor(0f, 0f, 0f, 0.85f * alpha);
        fonts.titleFont.draw(spriteBatch, "★ MISSION COMPLETE! ★", bx + 50f + 2f, by + bh - 16f);
        fonts.titleFont.setColor(1.0f, 0.88f, 0.40f, alpha);
        fonts.titleFont.draw(spriteBatch, "★ MISSION COMPLETE! ★", bx + 50f, by + bh - 14f);

        fonts.headerFont.setColor(0f, 0f, 0f, 0.85f * alpha);
        String sub = String.format("%s  •  [ %d / 5 Archives Secured ]", missionBannerTitle, missionBannerCount);
        fonts.headerFont.draw(spriteBatch, sub, bx + 50f + 1.5f, by + 26f);
        fonts.headerFont.setColor(1f, 1f, 1f, 0.95f * alpha);
        fonts.headerFont.draw(spriteBatch, sub, bx + 50f, by + 28f);
        spriteBatch.end();
    }

    // ==========================================
    // GRAND LEVEL 1 VICTORY / CONGRATULATIONS SCREEN
    // ==========================================
    private void renderVictoryScreen(float w, float h) {
        shapeRenderer.begin(ShapeType.Filled);
        // Dim screen background (translucent: 3D campus remains visible)
        shapeRenderer.setColor(0f, 0f, 0f, 0.45f);
        shapeRenderer.rect(0, 0, w, h);

        float vw = 760f;
        float vh = 520f;
        float vx = (w - vw) / 2f;
        float vy = (h - vh) / 2f;

        // Translucent glass panel
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.85f);
        shapeRenderer.rect(vx, vy, vw, vh);

        // Header band
        shapeRenderer.setColor(0.06f, 0.05f, 0.03f, 0.85f);
        shapeRenderer.rect(vx, vy + vh - 75f, vw, 75f);

        // Gold top accent
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(vx, vy + vh - 4f, vw, 4f);

        // Statistics sub-card
        float sx = vx + 40f;
        float sy = vy + 115f;
        float sw = vw - 80f;
        float sh = 105f;
        shapeRenderer.setColor(0.08f, 0.10f, 0.13f, 0.80f);
        shapeRenderer.rect(sx, sy, sw, sh);
        shapeRenderer.end();

        // Lines and Borders
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(vx, vy, vw, vh);
        shapeRenderer.line(vx, vy + vh - 75f, vx + vw, vy + vh - 75f);
        shapeRenderer.rect(sx, sy, sw, sh);

        // Ornate Corner Brackets (⌜ ⌝ ⌞ ⌟)
        float cLen = 22f;
        shapeRenderer.setColor(goldAccent);
        // Top-Left
        shapeRenderer.line(vx - 4f, vy + vh + 4f, vx + cLen, vy + vh + 4f);
        shapeRenderer.line(vx - 4f, vy + vh + 4f, vx - 4f, vy + vh - cLen);
        // Top-Right
        shapeRenderer.line(vx + vw + 4f, vy + vh + 4f, vx + vw - cLen, vy + vh + 4f);
        shapeRenderer.line(vx + vw + 4f, vy + vh + 4f, vx + vw + 4f, vy + vh - cLen);
        // Bottom-Left
        shapeRenderer.line(vx - 4f, vy - 4f, vx + cLen, vy - 4f);
        shapeRenderer.line(vx - 4f, vy - 4f, vx - 4f, vy + cLen);
        // Bottom-Right
        shapeRenderer.line(vx + vw + 4f, vy - 4f, vx + vw - cLen, vy - 4f);
        shapeRenderer.line(vx + vw + 4f, vy - 4f, vx + vw + 4f, vy + cLen);
        shapeRenderer.end();

        // Typography
        spriteBatch.begin();
        // Title
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "CONGRATULATIONS!", vx + 40f, vy + vh - 22f);

        fonts.headerFont.setColor(new Color(0.95f, 0.90f, 0.80f, 1f));
        fonts.headerFont.draw(spriteBatch, "LEVEL 1 COMPLETED — 36 JULY: THE SPARK OF FREEDOM", vx + 40f, vy + vh - 48f);

        // Historical Tribute Narrative
        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch,
            "You have successfully documented all 5 historical checkpoints of the July 2024 Student Mass Uprising across Dhaka University campus.",
            vx + 40f, vy + vh - 100f, vw - 80f, 10, true);

        fonts.bodyFont.setColor(new Color(0.88f, 0.88f, 0.90f, 1f));
        fonts.bodyFont.draw(spriteBatch,
            "From the initial solidarity at Curzon Hall to the climax of 36 July (August 5), students and citizens stood united for meritocracy, equality, and democratic rights. Authoritarian rule dissolved, opening a new dawn of freedom for Bangladesh.",
            vx + 40f, vy + vh - 145f, vw - 80f, 10, true);

        // Statistics Card
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "MISSION STATISTICS", sx + 20f, sy + sh - 15f);

        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Historical Archives Documented: 5 / 5 (100% Completed)", sx + 20f, sy + 52f);
        fonts.smallFont.draw(spriteBatch, "Campus Sector Explored: Curzon Hall & Central Avenue", sx + 20f, sy + 30f);

        fonts.smallFont.setColor(Color.GREEN);
        fonts.smallFont.draw(spriteBatch, "STATUS: VICTORY ACHIEVED", sx + sw - 210f, sy + 42f);

        // Interactive action prompts
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[ENTER] Return to Main Menu", vx + 50f, vy + 55f);

        fonts.promptFont.setColor(new Color(1f, 0.80f, 0.40f, 1f));
        fonts.promptFont.draw(spriteBatch, "[R] Replay Level 1", vx + 330f, vy + 55f);

        fonts.promptFont.setColor(new Color(0.95f, 0.40f, 0.35f, 1f));
        fonts.promptFont.draw(spriteBatch, "[X] Exit Game", vx + 540f, vy + 55f);

        spriteBatch.end();
    }

    /** Trigger victory screen directly (used for automated visual verification) */
    public void triggerVictoryForTest() {
        isVictoryOpen = true;
    }

    /** Open memorial modal directly (used for automated visual verification) */
    public void openMemorialModal(MemorialEntry entry) {
        this.activeModalEntry = entry;
    }

    public boolean isModalOpen() {
        return activeModalEntry != null || isMapOpen || isPauseMenuOpen || isVictoryOpen;
    }

    /** Reset HUD state (used when returning from main menu or replaying) */
    public void reset() {
        activeModalEntry = null;
        isMapOpen = false;
        isPauseMenuOpen = false;
        isVictoryOpen = false;
        victoryShown = false;
        missionBannerTime = 0f;
        exitAction = 0;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}


