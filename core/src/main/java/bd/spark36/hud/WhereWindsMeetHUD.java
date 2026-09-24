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
import bd.spark36.world.TextureFactory;

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

    // Boundary Stone (1921) Heritage Marker
    private final MemorialEntry boundaryStoneEntry = new MemorialEntry(
        99,
        "HISTORIC DU BOUNDARY STONE (1921)",
        "Founding Era — July 1, 1921",
        "Curzon Hall Heritage Precinct",
        "This weathered granite boundary marker demarcates the historic eastern perimeter of Dhaka University, established in 1921. For over a century, these grounds have witnessed generations of courageous students defending truth, freedom, and equal rights—culminating in the July 2024 Mass Uprising and the birth of 36 July.",
        bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS
    );

    private TextureFactory textures = null;

    public WhereWindsMeetHUD(FontRenderer fontRenderer) {
        this.fonts = fontRenderer;
    }

    public void setTextures(TextureFactory textures) {
        this.textures = textures;
    }

    public boolean isMapOpen() {
        return isMapOpen;
    }

    public void openMapForTest() {
        this.isMapOpen = true;
    }

    public void update(float delta, PlayerController player, JulyMemorials memorials) {
        animTime += delta;
        // Only count down toast banner when modal is NOT blocking view!
        if (missionBannerTime > 0f && activeModalEntry == null) {
            missionBannerTime -= delta;
        }

        // Mouse click navigation for Tactical Map button and Close buttons
        float backbufferW = (float) Gdx.graphics.getBackBufferWidth();
        float backbufferH = (float) Gdx.graphics.getBackBufferHeight();
        float virtW = 1600f;
        float virtH = 1600f * (backbufferH / backbufferW);
        float mouseX = ((float) Gdx.input.getX() / backbufferW) * virtW;
        float mouseY = (1.0f - (float) Gdx.input.getY() / backbufferH) * virtH;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (isMapOpen) {
                // Click close button on map
                float cbX = virtW - 200f;
                float cbY = virtH - 65f;
                if (mouseX >= cbX && mouseX <= cbX + 170f && mouseY >= cbY && mouseY <= cbY + 45f) {
                    isMapOpen = false;
                }
            } else if (activeModalEntry == null && !isPauseMenuOpen && !isVictoryOpen) {
                // Click tactical map HUD button
                float mbX = virtW - 215f;
                float mbY = virtH - 230f;
                if (mouseX >= mbX && mouseX <= mbX + 195f && mouseY >= mbY && mouseY <= mbY + 38f) {
                    isMapOpen = true;
                }
            }
        }

        // Handle Victory Screen input
        if (isVictoryOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.J)) {
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
        if (nearby == null && player.getPosition().dst(bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            nearby = boundaryStoneEntry;
        }

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

        if (isMapOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isMapOpen = false;
        } else if (activeModalEntry == null && !isMapOpen && !isVictoryOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
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
        if (!isMapOpen && !isPauseMenuOpen && !isVictoryOpen && activeModalEntry == null) {
            drawTacticalMapButtonBg(w, h);
        }

        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());
        if (nearby == null && player.getPosition().dst(bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            nearby = boundaryStoneEntry;
        }
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
        if (!isMapOpen && !isPauseMenuOpen && !isVictoryOpen && activeModalEntry == null) {
            drawTacticalMapButtonBorder(w, h);
        }

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
        if (!isMapOpen && !isPauseMenuOpen && !isVictoryOpen && activeModalEntry == null) {
            drawTacticalMapButtonText(w, h);
        }

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
            "2. Aparajeyo Bangla (Arts Plaza)",
            "3. TSC Raju Anti-Terrorism Sculpture",
            "4. Central Library & Hakim Chattar",
            "5. Teacher-Student Centre (TSC)"
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

        if (nearby != null && nearby.id == 99) {
            // Boundary Stone Prompt (Matching reference image [RT] Boundary Stone)
            fonts.headerFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.headerFont.draw(spriteBatch, "[E]  INSPECT BOUNDARY STONE (1921)", px + 18f, py + 41f);
            fonts.headerFont.setColor(goldAccent);
            fonts.headerFont.draw(spriteBatch, "[E]  INSPECT BOUNDARY STONE (1921)", px + 17f, py + 42f);

            fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.smallFont.draw(spriteBatch, "-- HISTORIC DHAKA UNIVERSITY HERITAGE", px + 48f, py + 19f);
            fonts.smallFont.setColor(new Color(0.95f, 0.92f, 0.80f, 1f));
            fonts.smallFont.draw(spriteBatch, "-- HISTORIC DHAKA UNIVERSITY HERITAGE", px + 47f, py + 20f);
        } else {
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

        // [J] (Jump / Double Jump)
        shapeRenderer.rect(kBaseX + 16f, kBaseY + 26f, 26f, 22f);

        // [E]
        shapeRenderer.rect(kBaseX + 140f, kBaseY + 26f, 26f, 22f);

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

        // [J] (Jump / Double Jump)
        shapeRenderer.rect(kBaseX + 16f, kBaseY + 26f, 26f, 22f);

        // [E]
        shapeRenderer.rect(kBaseX + 140f, kBaseY + 26f, 26f, 22f);

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
        fonts.keyFont.draw(spriteBatch, "J", kBaseX + 25f, kBaseY + 42f);
        fonts.keyFont.draw(spriteBatch, "E", kBaseX + 149f, kBaseY + 42f);
        fonts.keyFont.draw(spriteBatch, "M", kBaseX + 51f, kBaseY + 15f);
        fonts.keyFont.draw(spriteBatch, "ESC", kBaseX + 118f, kBaseY + 15f);

        // Action sub-labels matching updated keybinds:
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "MOVE", kBaseX - 6f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "SPRINT", kBaseX + 194f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "JUMP (2X)", kBaseX + 48f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "INTERACT", kBaseX + 172f, kBaseY + 42f);
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

    // ========================================================
    // TACTICAL MAP HUD BUTTON (Top-Right under Compass)
    // ========================================================
    private void drawTacticalMapButtonBg(float w, float h) {
        float mbX = w - 215f;
        float mbY = h - 230f;
        float mbW = 195f;
        float mbH = 34f;
        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(mbX, mbY, mbW, mbH);
    }

    private void drawTacticalMapButtonBorder(float w, float h) {
        float mbX = w - 215f;
        float mbY = h - 230f;
        float mbW = 195f;
        float mbH = 34f;
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mbX, mbY, mbW, mbH);

        float cLen = 7f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(mbX - 2, mbY + mbH + 2, mbX + cLen, mbY + mbH + 2);
        shapeRenderer.line(mbX - 2, mbY + mbH + 2, mbX - 2, mbY + mbH - cLen);
        shapeRenderer.line(mbX + mbW + 2, mbY + mbH + 2, mbX + mbW - cLen, mbY + mbH + 2);
        shapeRenderer.line(mbX + mbW + 2, mbY + mbH + 2, mbX + mbW + 2, mbY + mbH - cLen);
        shapeRenderer.line(mbX - 2, mbY - 2, mbX + cLen, mbY - 2);
        shapeRenderer.line(mbX - 2, mbY - 2, mbX - 2, mbY + cLen);
        shapeRenderer.line(mbX + mbW + 2, mbY - 2, mbX + mbW - cLen, mbY - 2);
        shapeRenderer.line(mbX + mbW + 2, mbY - 2, mbX + mbW + 2, mbY + cLen);
    }

    private void drawTacticalMapButtonText(float w, float h) {
        float mbX = w - 215f;
        float mbY = h - 230f;
        fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.keyFont.draw(spriteBatch, "[M]  CAMPUS MAP", mbX + 21f, mbY + 23f);
        fonts.keyFont.setColor(goldAccent);
        fonts.keyFont.draw(spriteBatch, "[M]  CAMPUS MAP", mbX + 20f, mbY + 24f);
    }

    // ==========================================
    // CAMPUS TACTICAL MAP OVERLAY (MATCHING MEDIA_1790270627121)
    // ==========================================
    private void renderCampusMap(PlayerController player, JulyMemorials memorials, float cameraYaw, float w, float h) {
        float mapW = 1520f;
        float mapH = h - 50f;
        float mx = (w - mapW) / 2f;
        float my = 25f;

        float centerMapX = mx + mapW / 2f;
        float centerMapY = my + mapH / 2f + 16f;
        float scale = 4.8f; // 4.8 pixels per meter
        // screenX = centerMapX + worldX * scale
        // screenY = centerMapY - (worldZ - 18) * scale  [north=negZ=higher Y]

        // ============================================================
        // FILLED PASS
        // ============================================================
        shapeRenderer.begin(ShapeType.Filled);

        // Full dark backdrop
        shapeRenderer.setColor(0f, 0f, 0f, 0.92f);
        shapeRenderer.rect(0, 0, w, h);

        // Map canvas
        shapeRenderer.setColor(0.07f, 0.09f, 0.11f, 0.97f);
        shapeRenderer.rect(mx, my, mapW, mapH);

        // Campus lawn (reference: rich green base)
        shapeRenderer.setColor(0.13f, 0.20f, 0.12f, 0.95f);
        shapeRenderer.rect(centerMapX - 100f * scale, centerMapY - 62f * scale, 200f * scale, 124f * scale);

        // Fuller Road (north, top of map)
        shapeRenderer.setColor(0.16f, 0.16f, 0.18f, 0.96f);
        shapeRenderer.rect(centerMapX - 110f * scale, centerMapY + 62f * scale, 220f * scale, 10f * scale);

        // Doel Chattar Road (south, bottom of map)
        shapeRenderer.rect(centerMapX - 110f * scale, centerMapY - 72f * scale, 220f * scale, 14f * scale);

        // South boundary wall (red brick, gap at Main Gate)
        shapeRenderer.setColor(0.52f, 0.18f, 0.14f, 0.96f);
        shapeRenderer.rect(centerMapX - 110f * scale, centerMapY - 62f * scale, 105f * scale, 2.0f * scale);
        shapeRenderer.rect(centerMapX + 5.5f * scale, centerMapY - 62f * scale, 104.5f * scale, 2.0f * scale);

        // ============================================================
        // WALKWAY NETWORK
        // ============================================================
        shapeRenderer.setColor(0.70f, 0.63f, 0.50f, 0.92f);

        // Central Promenade: X=0, Z=+78 (gate) to Z=-40 (behind Curzon Hall)
        shapeRenderer.rect(centerMapX - 3.6f * scale, centerMapY - 62f * scale, 7.2f * scale, 118f * scale);

        // East branch path (runs along RIGHT side of pukur): X=+22m, Z=-16 to Z=+14
        shapeRenderer.rect(centerMapX + 19.5f * scale, centerMapY - (14f - 18f) * scale - 4.8f * scale, 5.0f * scale, 30f * scale);

        // North cross-path (Z=-16, X=0 to X=+22) connecting promenade to east branch
        shapeRenderer.rect(centerMapX, centerMapY - (-16f - 18f) * scale - 2.4f * scale, 22f * scale, 4.8f * scale);

        // South cross-path (Z=+14, X=0 to X=+22) connecting promenade to east branch
        shapeRenderer.rect(centerMapX, centerMapY - (14f - 18f) * scale - 2.4f * scale, 22f * scale, 4.8f * scale);

        // West Cross-Avenue: Z=+26, X=0 to X=-74
        shapeRenderer.rect(centerMapX - 74f * scale, centerMapY - (26f - 18f) * scale - 2.4f * scale, 74f * scale, 4.8f * scale);

        // West North Branch to Madhur Canteen: X=-56, Z=+26 to Z=-20
        shapeRenderer.rect(centerMapX - 58.4f * scale, centerMapY - (26f - 18f) * scale, 4.8f * scale, 46f * scale);

        // West South Branch to Hakim Chattar: X=-58, Z=+26 to Z=+54
        shapeRenderer.rect(centerMapX - 60.4f * scale, centerMapY - (54f - 18f) * scale, 4.8f * scale, 28f * scale);

        // East Cross-Avenue: Z=+26, X=0 to X=+74
        shapeRenderer.rect(centerMapX, centerMapY - (26f - 18f) * scale - 2.4f * scale, 74f * scale, 4.8f * scale);

        // East North Branch to Swadhinata: X=+56, Z=+26 to Z=-18
        shapeRenderer.rect(centerMapX + 53.6f * scale, centerMapY - (26f - 18f) * scale, 4.8f * scale, 44f * scale);

        // East South Branch to Raju: X=+44, Z=+26 to Z=+42
        shapeRenderer.rect(centerMapX + 41.6f * scale, centerMapY - (42f - 18f) * scale, 4.8f * scale, 16f * scale);

        // Curzon Hall front verandah path: Z=-17, X=-48 to X=+48
        shapeRenderer.rect(centerMapX - 48f * scale, centerMapY - (-17f - 18f) * scale - 3f * scale, 96f * scale, 6f * scale);

        // ============================================================
        // CURZON HALL PUKUR
        // Reference map: pond is EAST of central promenade (~+12m east)
        // The central promenade passes to the LEFT (west) of the pond
        // ============================================================
        float pukurWorldX = 12f;
        float pukurWorldZ = -2f;
        float pukurWm     = 20f;
        float pukurHm     = 16f;

        float pX = centerMapX + pukurWorldX * scale;
        float pY = centerMapY - (pukurWorldZ - 18f) * scale;
        float pW = pukurWm * scale;
        float pH = pukurHm * scale;

        // Stone curb border
        shapeRenderer.setColor(0.82f, 0.79f, 0.74f, 1f);
        shapeRenderer.rect(pX - pW / 2f - 4f, pY - pH / 2f - 4f, pW + 8f, pH + 8f);
        // Water
        shapeRenderer.setColor(0.20f, 0.55f, 0.64f, 0.96f);
        shapeRenderer.rect(pX - pW / 2f, pY - pH / 2f, pW, pH);
        // Shimmer
        shapeRenderer.setColor(0.35f, 0.72f, 0.80f, 0.50f);
        shapeRenderer.rect(pX - pW / 2f + 4f, pY - pH / 2f + 3f, pW * 0.6f, pH * 0.25f);

        // ============================================================
        // BUILDINGS
        // ============================================================

        // 1. CURZON HALL (north/top-center)
        float chX = centerMapX;
        float chY = centerMapY - (-32f - 18f) * scale;
        shapeRenderer.setColor(0.66f, 0.21f, 0.15f, 0.97f);
        shapeRenderer.rect(chX - 19f * scale, chY - 8.5f * scale, 38f * scale, 17f * scale);
        shapeRenderer.rect(chX - 46f * scale, chY - 7.5f * scale, 27f * scale, 15f * scale);
        shapeRenderer.rect(chX + 19f * scale, chY - 7.5f * scale, 27f * scale, 15f * scale);
        shapeRenderer.rect(chX - 7.5f * scale, chY - 12.5f * scale, 15f * scale, 4f * scale);
        shapeRenderer.setColor(0.94f, 0.92f, 0.88f, 1f);
        shapeRenderer.circle(chX, chY, 5.2f * scale, 24);
        shapeRenderer.circle(chX - 18.5f * scale, chY - 2.5f * scale, 2.2f * scale, 16);
        shapeRenderer.circle(chX + 18.5f * scale, chY - 2.5f * scale, 2.2f * scale, 16);
        shapeRenderer.circle(chX - 44.5f * scale, chY - 2.5f * scale, 2.1f * scale, 16);
        shapeRenderer.circle(chX + 44.5f * scale, chY - 2.5f * scale, 2.1f * scale, 16);

        // 2. CENTRAL LIBRARY BUILDING (far west)
        float clMapX = centerMapX - 68f * scale;
        float clMapY = centerMapY - (22f - 18f) * scale;
        shapeRenderer.setColor(0.63f, 0.23f, 0.17f, 0.97f);
        shapeRenderer.rect(clMapX - 18f * scale, clMapY - 16f * scale, 36f * scale, 32f * scale);
        shapeRenderer.setColor(0.13f, 0.20f, 0.12f, 0.96f);
        shapeRenderer.rect(clMapX - 8f * scale, clMapY - 8f * scale, 16f * scale, 16f * scale);
        shapeRenderer.setColor(0.75f, 0.73f, 0.70f, 0.96f);
        shapeRenderer.rect(clMapX + 14f * scale, clMapY - 8f * scale, 4f * scale, 16f * scale);

        // 3. HAKIM CHATTAR (bottom-left, octagonal)
        float hkMapX = centerMapX - 58f * scale;
        float hkMapY = centerMapY - (54f - 18f) * scale;
        shapeRenderer.setColor(0.70f, 0.30f, 0.20f, 0.96f);
        shapeRenderer.circle(hkMapX, hkMapY, 5.2f * scale, 8);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(hkMapX, hkMapY, 1.3f * scale, 8);

        // 4. MADHUR CANTEEN (top-left)
        float mcMapX = centerMapX - 56f * scale;
        float mcMapY = centerMapY - (-16f - 18f) * scale;
        shapeRenderer.setColor(0.56f, 0.22f, 0.18f, 0.97f);
        shapeRenderer.rect(mcMapX - 9f * scale, mcMapY - 7f * scale, 18f * scale, 14f * scale);
        shapeRenderer.setColor(0.82f, 0.76f, 0.68f, 0.96f);
        shapeRenderer.rect(mcMapX - 8f * scale, mcMapY - 7f * scale, 16f * scale, 3.5f * scale);

        // 5. BOOK STALLS (west of promenade, north of Arts Plaza)
        shapeRenderer.setColor(0.36f, 0.26f, 0.16f, 0.96f);
        for (float bsz : new float[]{6f, 12f, 18f}) {
            float bsMapY = centerMapY - (bsz - 18f) * scale;
            shapeRenderer.rect(centerMapX - 44f * scale, bsMapY - 1.4f * scale, 2.8f * scale, 2.8f * scale);
        }

        // 6. ARTS PLAZA + APARAJEYO BANGLA (west of promenade)
        float abMapX = centerMapX - 36f * scale;
        float abMapY = centerMapY - (26f - 18f) * scale;
        shapeRenderer.setColor(0.78f, 0.76f, 0.72f, 0.96f);
        shapeRenderer.rect(abMapX - 4f * scale, abMapY - 4f * scale, 8f * scale, 8f * scale);
        shapeRenderer.setColor(0.24f, 0.27f, 0.30f, 0.97f);
        shapeRenderer.circle(abMapX, abMapY, 2.0f * scale, 12);

        // 7. FLANK BUILDINGS around pukur (small red-brick blocks in reference)
        shapeRenderer.setColor(0.66f, 0.22f, 0.16f, 0.90f);
        // West of pukur, north
        shapeRenderer.rect(centerMapX + 4f * scale, centerMapY - (-16f - 18f) * scale - 5f * scale, 6f * scale, 10f * scale);
        // East of pukur, north
        shapeRenderer.rect(centerMapX + 24f * scale, centerMapY - (-16f - 18f) * scale - 5f * scale, 6f * scale, 10f * scale);
        // South-east block
        shapeRenderer.rect(centerMapX + 22f * scale, centerMapY - (10f - 18f) * scale - 4f * scale, 5f * scale, 8f * scale);

        // 8. TEACHER-STUDENT CENTRE (far right, modernist)
        float tscMapX = centerMapX + 68f * scale;
        float tscMapY = centerMapY - (20f - 18f) * scale;
        shapeRenderer.setColor(0.74f, 0.73f, 0.70f, 0.97f);
        shapeRenderer.rect(tscMapX - 19f * scale, tscMapY - 15f * scale, 38f * scale, 30f * scale);
        shapeRenderer.setColor(0.60f, 0.26f, 0.20f, 0.97f);
        shapeRenderer.rect(tscMapX - 9f * scale, tscMapY - 9f * scale, 18f * scale, 18f * scale);
        shapeRenderer.setColor(0.82f, 0.80f, 0.76f, 0.70f);
        for (int f = -4; f <= 4; f++) {
            shapeRenderer.rect(tscMapX - 19.5f * scale, tscMapY + f * 1.8f * scale - 0.4f * scale, 1.5f * scale, 0.8f * scale);
        }

        // 9. RAJU MEMORIAL ROUNDABOUT (south-east)
        float rjMapX = centerMapX + 44f * scale;
        float rjMapY = centerMapY - (42f - 18f) * scale;
        shapeRenderer.setColor(0.16f, 0.16f, 0.18f, 0.96f);
        shapeRenderer.circle(rjMapX, rjMapY, 9.5f * scale, 28);
        shapeRenderer.setColor(0.17f, 0.30f, 0.15f, 0.96f);
        shapeRenderer.circle(rjMapX, rjMapY, 7.5f * scale, 28);
        shapeRenderer.setColor(0.90f, 0.88f, 0.84f, 0.97f);
        shapeRenderer.rect(rjMapX - 2.4f * scale, rjMapY - 2.4f * scale, 4.8f * scale, 4.8f * scale);

        // 10. SWADHINATA SANGRAM SCULPTURE GARDEN (top-right)
        float ssMapX = centerMapX + 56f * scale;
        float ssMapY = centerMapY - (-16f - 18f) * scale;
        shapeRenderer.setColor(0.68f, 0.66f, 0.62f, 0.96f);
        shapeRenderer.rect(ssMapX - 11f * scale, ssMapY - 9f * scale, 22f * scale, 18f * scale);
        shapeRenderer.setColor(0.24f, 0.27f, 0.30f, 0.96f);
        shapeRenderer.circle(ssMapX - 4.5f * scale, ssMapY + 3f * scale, 1.2f * scale, 8);
        shapeRenderer.circle(ssMapX + 4.5f * scale, ssMapY + 3f * scale, 1.2f * scale, 8);
        shapeRenderer.circle(ssMapX, ssMapY - 3f * scale, 1.2f * scale, 8);

        // 11. MAIN GATE (south/bottom-center)
        float mgMapY = centerMapY - (78f - 18f) * scale;
        shapeRenderer.setColor(0.70f, 0.22f, 0.16f, 0.97f);
        shapeRenderer.rect(centerMapX - 4.8f * scale, mgMapY - 1.5f * scale, 9.6f * scale, 3.5f * scale);
        shapeRenderer.setColor(0.82f, 0.76f, 0.68f, 1f);
        shapeRenderer.rect(centerMapX - 5.2f * scale, mgMapY - 2.5f * scale, 2.0f * scale, 5.5f * scale);
        shapeRenderer.rect(centerMapX + 3.2f * scale, mgMapY - 2.5f * scale, 2.0f * scale, 5.5f * scale);
        shapeRenderer.setColor(0.16f, 0.84f, 0.38f, 1f);
        shapeRenderer.triangle(centerMapX, mgMapY + 5f * scale,
            centerMapX - 2.2f * scale, mgMapY + 1.5f * scale,
            centerMapX + 2.2f * scale, mgMapY + 1.5f * scale);

        // 12. TREE CANOPIES
        float[][] treeMapLocs = {
            {-12f, -4f}, {12f, -4f},
            {-50f, -28f}, {-62f, -28f}, {50f, -28f}, {62f, -28f},
            {-14f, 12f}, {14f, 12f},
            {-28f, 14f}, {-48f, 4f}, {-62f, 28f},
            {28f, 14f}, {48f, 4f}, {62f, 28f},
            {-14f, 48f}, {14f, 48f},
            {-22f, 64f}, {22f, 64f},
            {-28f, 70f}, {28f, 70f},
            {-85f, -36f}, {85f, -36f}, {-85f, 60f}, {85f, 60f},
        };
        shapeRenderer.setColor(0.16f, 0.38f, 0.14f, 0.86f);
        for (float[] t : treeMapLocs) {
            shapeRenderer.circle(centerMapX + t[0] * scale, centerMapY - (t[1] - 18f) * scale, 4.0f * scale, 14);
        }
        shapeRenderer.setColor(0.11f, 0.28f, 0.10f, 0.70f);
        for (float[] t : treeMapLocs) {
            shapeRenderer.circle(centerMapX + t[0] * scale, centerMapY - (t[1] - 18f) * scale, 2.0f * scale, 10);
        }

        // 13. OBJECTIVE & MEMORIAL PINS
        MemorialEntry nextObj = memorials.getNextObjective(player.getPosition());
        for (MemorialEntry entry : memorials.getEntries()) {
            float pointX = centerMapX + entry.position.x * scale;
            float pointY = centerMapY - (entry.position.z - 18f) * scale;
            if (entry.inspected) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.circle(pointX, pointY, 8f, 16);
            } else if (entry == nextObj) {
                float pulse = 0.82f + 0.28f * MathUtils.sin(animTime * 6f);
                shapeRenderer.setColor(1.0f, 0.82f * pulse, 0.25f, 1f);
                float ds = 10f * pulse;
                shapeRenderer.triangle(pointX, pointY + ds, pointX + ds, pointY, pointX, pointY - ds);
                shapeRenderer.triangle(pointX, pointY + ds, pointX - ds, pointY, pointX, pointY - ds);
            } else {
                shapeRenderer.setColor(goldAccent);
                shapeRenderer.circle(pointX, pointY, 7f, 16);
            }
        }

        // 14. LIVE PLAYER MARKER
        Vector3 ppos = player.getPosition();
        float pMapX = centerMapX + ppos.x * scale;
        float pMapY = centerMapY - (ppos.z - 18f) * scale;
        shapeRenderer.setColor(0.2f, 0.9f, 1.0f, 0.30f + 0.18f * MathUtils.sin(animTime * 4f));
        shapeRenderer.circle(pMapX, pMapY, 13f, 20);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(pMapX, pMapY, 5.5f, 16);
        float pHeadRad = (player.getHeadingDegrees() - 90f) * MathUtils.degreesToRadians;
        float tipX = pMapX - 16f * MathUtils.cos(pHeadRad);
        float tipY = pMapY + 16f * MathUtils.sin(pHeadRad);
        float baseLeftX = pMapX - 6f * MathUtils.cos(pHeadRad + 2.2f);
        float baseLeftY = pMapY + 6f * MathUtils.sin(pHeadRad + 2.2f);
        float baseRightX = pMapX - 6f * MathUtils.cos(pHeadRad - 2.2f);
        float baseRightY = pMapY + 6f * MathUtils.sin(pHeadRad - 2.2f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.triangle(tipX, tipY, baseLeftX, baseLeftY, baseRightX, baseRightY);

        // 15. UI PANEL BACKGROUNDS
        float vtX = mx + 20f, vtY = my + 20f, vtW = 270f, vtH = 185f;
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.93f);
        shapeRenderer.rect(vtX, vtY, vtW, vtH);

        float legX = mx + mapW - 260f, legY = my + 20f, legW = 240f, legH = 230f;
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.93f);
        shapeRenderer.rect(legX, legY, legW, legH);

        float cbX = mx + mapW - 175f, cbY = my + mapH - 48f, cbW = 155f, cbH = 34f;
        shapeRenderer.setColor(0.10f, 0.14f, 0.20f, 0.93f);
        shapeRenderer.rect(cbX, cbY, cbW, cbH);

        // LEGEND ICONS
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(legX + 16f, legY + legH - 48f, 11f, 11f);
        shapeRenderer.setColor(1.0f, 0.85f, 0.25f, 1f);
        float ldy = legY + legH - 72f;
        shapeRenderer.triangle(legX + 21f, ldy + 7f, legX + 28f, ldy, legX + 14f, ldy);
        shapeRenderer.triangle(legX + 21f, ldy - 7f, legX + 28f, ldy, legX + 14f, ldy);
        shapeRenderer.setColor(0.35f, 0.82f, 1.0f, 1f);
        float lpy = legY + legH - 96f;
        shapeRenderer.triangle(legX + 21f, lpy + 6f, legX + 27f, lpy, legX + 15f, lpy);
        shapeRenderer.triangle(legX + 21f, lpy - 6f, legX + 27f, lpy, legX + 15f, lpy);
        shapeRenderer.setColor(1.0f, 0.55f, 0.20f, 1f);
        shapeRenderer.circle(legX + 21f, legY + legH - 118f, 5.5f, 12);
        shapeRenderer.triangle(legX + 21f, legY + legH - 128f, legX + 18f, legY + legH - 118f, legX + 24f, legY + legH - 118f);
        shapeRenderer.setColor(0.30f, 0.55f, 1.0f, 1f);
        shapeRenderer.circle(legX + 21f, legY + legH - 148f, 5.5f, 12);
        shapeRenderer.setColor(0.90f, 0.20f, 0.20f, 1f);
        shapeRenderer.circle(legX + 21f, legY + legH - 168f, 5.5f, 12);
        shapeRenderer.setColor(0.2f, 0.9f, 1.0f, 0.50f);
        shapeRenderer.circle(legX + 21f, legY + legH - 190f, 7f, 12);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(legX + 21f, legY + legH - 190f, 3.5f, 12);

        shapeRenderer.end();

        // ============================================================
        // LINE BORDERS PASS
        // ============================================================
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mx, my, mapW, mapH);
        shapeRenderer.rect(mx + 3f, my + 3f, mapW - 6f, mapH - 6f);
        float cLen = 30f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(mx, my + mapH - cLen, mx, my + mapH);
        shapeRenderer.line(mx, my + mapH, mx + cLen, my + mapH);
        shapeRenderer.line(mx + mapW - cLen, my + mapH, mx + mapW, my + mapH);
        shapeRenderer.line(mx + mapW, my + mapH, mx + mapW, my + mapH - cLen);
        shapeRenderer.line(mx, my, mx + cLen, my);
        shapeRenderer.line(mx, my, mx, my + cLen);
        shapeRenderer.line(mx + mapW - cLen, my, mx + mapW, my);
        shapeRenderer.line(mx + mapW, my, mx + mapW, my + cLen);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(vtX, vtY, vtW, vtH);
        shapeRenderer.rect(legX, legY, legW, legH);
        shapeRenderer.setColor(new Color(0.30f, 0.42f, 0.56f, 0.90f));
        shapeRenderer.rect(cbX, cbY, cbW, cbH);

        // Compass Rose
        float crX = mx + 90f, crY = my + mapH - 90f, crR = 52f;
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.circle(crX, crY, crR, 36);
        shapeRenderer.circle(crX, crY, crR - 5f, 32);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(crX, crY - crR, crX, crY + crR);
        shapeRenderer.line(crX - crR, crY, crX + crR, crY);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.line(crX - crR * 0.7f, crY - crR * 0.7f, crX + crR * 0.7f, crY + crR * 0.7f);
        shapeRenderer.line(crX + crR * 0.7f, crY - crR * 0.7f, crX - crR * 0.7f, crY + crR * 0.7f);
        shapeRenderer.end();

        // ============================================================
        // TEXT PASS
        // ============================================================
        spriteBatch.begin();

        // Title
        fonts.titleFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.titleFont.draw(spriteBatch, "LEVEL 1: DHAKA UNIVERSITY", centerMapX - 218f + 2f, my + mapH - 18f);
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "LEVEL 1: DHAKA UNIVERSITY", centerMapX - 218f, my + mapH - 16f);
        fonts.smallFont.setColor(new Color(0.88f, 0.84f, 0.68f, 1f));
        fonts.smallFont.draw(spriteBatch, "Mall Chattar Corridor  \u2022  Curzon Hall Precinct  \u2022  Est. 1921", centerMapX - 240f, my + mapH - 42f);

        // Compass
        fonts.headerFont.setColor(healthRed);
        fonts.headerFont.draw(spriteBatch, "N", crX - 6f, crY + crR + 22f);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "S", crX - 4f, crY - crR - 5f);
        fonts.smallFont.draw(spriteBatch, "W", crX - crR - 18f, crY + 5f);
        fonts.smallFont.draw(spriteBatch, "E", crX + crR + 6f, crY + 5f);

        // Close button
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[M] / [ESC]  Close  X", cbX + 12f, cbY + 23f);

        // Landmark labels
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Fuller Road Corridor", centerMapX - 60f, centerMapY + 64f * scale);
        fonts.smallFont.draw(spriteBatch, "Curzon Hall", chX - 32f, chY + 2.5f * scale);
        fonts.smallFont.setColor(new Color(0.88f, 0.96f, 1.0f, 1f));
        fonts.smallFont.draw(spriteBatch, "Curzon Hall", pX - 36f, pY + 4f);
        fonts.smallFont.draw(spriteBatch, "Pukur", pX - 18f, pY - 8f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Central Library Building", clMapX - 56f, clMapY + 20f * scale);
        fonts.smallFont.draw(spriteBatch, "Hakim Chattar", hkMapX - 42f, hkMapY - 9f * scale);
        fonts.smallFont.draw(spriteBatch, "Madhur Canteen", mcMapX - 44f, mcMapY + 11f * scale);
        fonts.smallFont.draw(spriteBatch, "Book Stalls", centerMapX - 50f * scale, centerMapY - (12f - 18f) * scale + 6f);
        fonts.smallFont.draw(spriteBatch, "Arts Plaza", abMapX - 28f, abMapY + 5f * scale);
        fonts.smallFont.draw(spriteBatch, "Aparajeyo Bangla", abMapX - 48f, abMapY - 7f * scale);
        fonts.smallFont.draw(spriteBatch, "Teacher-Student Centre (TSC)", tscMapX - 72f, tscMapY + 18f * scale);
        fonts.smallFont.draw(spriteBatch, "Raju Memorial Sculpture", rjMapX - 60f, rjMapY - 12f * scale);
        fonts.smallFont.draw(spriteBatch, "Swadhinata Sangram", ssMapX - 56f, ssMapY + 12f * scale);
        fonts.smallFont.draw(spriteBatch, "Sculpture Garden", ssMapX - 46f, ssMapY + 10f * scale - 14f);
        fonts.smallFont.draw(spriteBatch, "Main Gate", centerMapX - 28f, mgMapY - 10f);

        // Visual Target Card
        if (textures != null && textures.mapVisualTarget != null) {
            spriteBatch.draw(textures.mapVisualTarget, vtX + 12f, vtY + 38f, vtW - 24f, 110f);
        }
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "LEVEL 1 VISUAL TARGET", vtX + 14f, vtY + vtH - 12f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Curzon Hall (Science Faculty)", vtX + 14f, vtY + 24f);

        // Map Legend Card
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "MAP LEGEND", legX + 16f, legY + legH - 14f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Buildings", legX + 36f, legY + legH - 39f);
        fonts.smallFont.draw(spriteBatch, "POI objective", legX + 36f, legY + legH - 63f);
        fonts.smallFont.draw(spriteBatch, "Teacher-Student Centre", legX + 36f, legY + legH - 87f);
        fonts.smallFont.draw(spriteBatch, "Arts Plaza", legX + 36f, legY + legH - 109f);
        fonts.smallFont.draw(spriteBatch, "Quest Pin", legX + 36f, legY + legH - 139f);
        fonts.smallFont.draw(spriteBatch, "Quest Pins", legX + 36f, legY + legH - 159f);
        fonts.smallFont.draw(spriteBatch, "High objective Markers", legX + 36f, legY + legH - 181f);
        fonts.smallFont.setColor(new Color(0.70f, 0.82f, 0.90f, 1f));
        fonts.smallFont.draw(spriteBatch, String.format("%.1fE  %.1fN", ppos.x, -ppos.z), legX + 16f, legY + 24f);

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
        fonts.titleFont.draw(spriteBatch, "* MISSION COMPLETE! *", bx + 50f + 2f, by + bh - 16f);
        fonts.titleFont.setColor(1.0f, 0.88f, 0.40f, alpha);
        fonts.titleFont.draw(spriteBatch, "* MISSION COMPLETE! *", bx + 50f, by + bh - 14f);

        fonts.headerFont.setColor(0f, 0f, 0f, 0.85f * alpha);
        String sub = String.format("%s  |  [ %d / 5 Archives Secured ]", missionBannerTitle, missionBannerCount);
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
        fonts.headerFont.draw(spriteBatch, "LEVEL 1 COMPLETED -- 36 JULY: THE SPARK OF FREEDOM", vx + 40f, vy + vh - 48f);

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


