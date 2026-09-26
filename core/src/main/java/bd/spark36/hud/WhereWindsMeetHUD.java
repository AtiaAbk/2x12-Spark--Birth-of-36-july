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
    private boolean showParameterHud = true;
    private float animTime = 0f;

    // Victory Screen Celebratory Sparkles
    private final float[][] victorySparks = new float[36][4]; // x, y, speed, alpha
    private boolean victorySparksInit = false;

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
    private com.badlogic.gdx.graphics.Texture campusMapTex = null;
    private boolean campusMapLoadTried = false;

    // ── AAA Visual FX Fields ──────────────────────────────────────────────────
    // Checkpoint flash: brief full-screen gold flash when a memorial is inspected
    private float checkpointFlashAlpha = 0f;
    private float checkpointFlashDecay = 3.5f;    // alpha/sec decay rate

    // Sprint side-trail: subtle screen-edge blur/darkening while sprinting
    private float sprintTrailIntensity = 0f;

    // Objective ring spin angle
    private float objectiveRingAngle = 0f;

    // Crosshair breath pulse
    private float breathPhase = 0f;


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

        // ── VFX Animations ─────────────────────────────────────────────────
        // Checkpoint flash decay
        if (checkpointFlashAlpha > 0f) {
            checkpointFlashAlpha -= checkpointFlashDecay * delta;
            if (checkpointFlashAlpha < 0f) checkpointFlashAlpha = 0f;
        }
        // Sprint trail: build up when sprinting, fade when not
        float sprintTarget = player.isSprinting() && player.isMoving() ? 1f : 0f;
        sprintTrailIntensity += (sprintTarget - sprintTrailIntensity) * Math.min(1f, 5f * delta);
        // Objective ring spin
        objectiveRingAngle = (objectiveRingAngle + 35f * delta) % 360f;
        // Breath pulse
        breathPhase += delta * 1.8f;


        if (missionBannerTime > 0f && activeModalEntry == null) {
            missionBannerTime -= delta;
        }

        // Toggle Parameter HUD on F1 or TAB
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            showParameterHud = !showParameterHud;
        }

        // Mouse click navigation for Tactical Map button, Victory buttons, and Close buttons
        float backbufferW = (float) Gdx.graphics.getBackBufferWidth();
        float backbufferH = (float) Gdx.graphics.getBackBufferHeight();
        float virtW = 1600f;
        float virtH = 1600f * (backbufferH / backbufferW);
        float mouseX = ((float) Gdx.input.getX() / backbufferW) * virtW;
        float mouseY = (1.0f - (float) Gdx.input.getY() / backbufferH) * virtH;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (isVictoryOpen) {
                float vw = 820f;
                float vh = 560f;
                float vx = (virtW - vw) / 2f;
                float vy = (virtH - vh) / 2f;
                float sw = vw - 80f;
                float cw = (sw - 20f) / 3f;
                float btn1X = vx + 40f;
                float btn2X = btn1X + cw + 10f;
                float btn3X = btn2X + cw + 10f;
                float btnY = vy + 34f;
                float btnH = 46f;

                if (mouseX >= btn1X && mouseX <= btn1X + cw && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 1; // Return to Main Menu
                    isVictoryOpen = false;
                    return;
                }
                if (mouseX >= btn2X && mouseX <= btn2X + cw && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 3; // Replay Level 1
                    isVictoryOpen = false;
                    victoryShown = false;
                    return;
                }
                if (mouseX >= btn3X && mouseX <= btn3X + cw && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 2; // Quit Game
                    isVictoryOpen = false;
                    return;
                }
            } else if (isMapOpen) {
                float margin = 34f;
                float fit = Math.min((virtW - 2f * margin) / MAP_IMG_W, (virtH - 2f * margin - 16f) / MAP_IMG_H);
                float dw = MAP_IMG_W * fit;
                float dh = MAP_IMG_H * fit;
                float dx = (virtW - dw) / 2f;
                float dy = (virtH - dh) / 2f + 8f;

                // Dedicated close button box in top-right
                float cbW = 160f;
                float cbH = 40f;
                float cbX = dx + dw - cbW - 12f;
                float cbY = dy + dh - cbH - 10f;

                boolean clickedCloseBtn = (mouseX >= cbX && mouseX <= cbX + cbW && mouseY >= cbY && mouseY <= cbY + cbH);
                boolean clickedBottomClose = (mouseX >= dx + dw - 220f && mouseX <= dx + dw && mouseY >= dy - 30f && mouseY <= dy + 15f);
                boolean clickedOutside = (mouseX < dx || mouseX > dx + dw || mouseY < dy || mouseY > dy + dh);

                if (clickedCloseBtn || clickedBottomClose || clickedOutside) {
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

        // Handle Victory Screen keyboard input
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
            if (Gdx.input.isKeyJustPressed(Input.Keys.X) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
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
                missionBannerTime = 6.0f;
                // Trigger checkpoint gold flash!
                checkpointFlashAlpha = 1.0f;

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
        render(player, memorials, cameraYaw, null, 5.0f, 65.0f);
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw, com.badlogic.gdx.graphics.Camera camera) {
        render(player, memorials, cameraYaw, camera, 5.0f, 65.0f);
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw, com.badlogic.gdx.graphics.Camera camera, float cameraPitch, float cameraFov) {
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

        // ── AAA VFX: Sprint Motion Trail + Checkpoint Flash ──────────────────
        shapeRenderer.begin(ShapeType.Filled);

        // Sprint vignette: dark edges when sprinting for speed blur effect
        if (sprintTrailIntensity > 0.01f) {
            float trailAlpha = sprintTrailIntensity * 0.36f;
            // Left edge
            shapeRenderer.setColor(0.02f, 0.02f, 0.04f, trailAlpha);
            shapeRenderer.rect(0, 0, w * 0.12f, h);
            // Right edge
            shapeRenderer.rect(w * 0.88f, 0, w * 0.12f, h);
            // Bottom edge
            shapeRenderer.setColor(0.02f, 0.02f, 0.04f, trailAlpha * 0.6f);
            shapeRenderer.rect(0, 0, w, h * 0.08f);
            // Top edge
            shapeRenderer.rect(0, h * 0.92f, w, h * 0.08f);
        }

        // Checkpoint gold flash: brief full-screen warm golden pulse
        if (checkpointFlashAlpha > 0.01f) {
            float alpha = checkpointFlashAlpha * 0.48f;
            shapeRenderer.setColor(1.0f, 0.82f, 0.25f, alpha);
            shapeRenderer.rect(0, 0, w, h);
        }

        shapeRenderer.end();
        // ── End VFX ───────────────────────────────────────────────────────────

        // 1. Draw HUD Background Shapes & Ornate Geometry
        boolean hudActive = (activeModalEntry == null && !isMapOpen && !isPauseMenuOpen && !isVictoryOpen);

        shapeRenderer.begin(ShapeType.Filled);
        if (hudActive) {
            drawMissionCardBg(memorials, w, h);
            drawParameterHudBg(player, memorials, w, h);
            drawAntiqueCompassRoseFilled(w, h, cameraYaw);
            drawKeycapsBg(w, h);
            drawTacticalMapButtonBg(w, h);
        }

        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());
        if (nearby == null && player.getPosition().dst(bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            nearby = boundaryStoneEntry;
        }
        if (nearby != null && hudActive) {
            drawSpeechBubblePromptBg(w, h);
        }
        shapeRenderer.end();

        // 2. Draw HUD Outlines, Accents & Filigree
        shapeRenderer.begin(ShapeType.Line);
        if (hudActive) {
            drawMissionCardBorders(player, memorials, cameraYaw, w, h);
            drawParameterHudBorders(player, memorials, w, h);
            drawAntiqueCompassRoseLines(w, h, cameraYaw);
            drawKeycapsBorders(w, h);
            drawTacticalMapButtonBorder(w, h);
        }

        if (nearby != null && hudActive) {
            drawSpeechBubblePromptBorders(w, h);
        }
        shapeRenderer.end();

        // 3. Draw Typography & Glyphs
        spriteBatch.begin();
        if (hudActive) {
            drawMissionCardText(memorials, nearest, dstToNearest, w, h);
            drawParameterHudText(player, memorials, cameraYaw, cameraPitch, cameraFov, w, h);
            drawAntiqueCompassRoseText(player, memorials, nearest, dstToNearest, w, h, cameraYaw);
            drawKeycapsText(w, h);
            drawTacticalMapButtonText(w, h);
        }

        if (nearby != null && hudActive) {
            drawSpeechBubblePromptText(nearby, w, h);
        }
        spriteBatch.end();

        // 4. Toast Notification Banner (renders on top of HUD when active)
        if (hudActive) {
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

    private void drawMissionCardBorders(PlayerController player, JulyMemorials memorials, float cameraYaw, float w, float h) {
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

        // Pointer needle (pointing towards active objective relative to cameraYaw)
        MemorialEntry nextObj = memorials.getNextObjective(player.getPosition());
        float needleAngle;
        if (nextObj != null) {
            float dx = nextObj.position.x - player.getPosition().x;
            float dz = nextObj.position.z - player.getPosition().z;
            float worldAngleDeg = MathUtils.atan2(-dz, dx) * MathUtils.radiansToDegrees;
            needleAngle = (worldAngleDeg - cameraYaw) * MathUtils.degreesToRadians;
        } else {
            needleAngle = 90f * MathUtils.degreesToRadians;
        }
        shapeRenderer.setColor(healthRed);
        shapeRenderer.line(cX, cY, cX + 11f * MathUtils.cos(needleAngle), cY + 11f * MathUtils.sin(needleAngle));
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(cX, cY, cX - 6f * MathUtils.cos(needleAngle), cY - 6f * MathUtils.sin(needleAngle));
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
    // 2. TOP-RIGHT ANTIQUE 8-POINT COMPASS ROSE (True Geographic Bearing)
    // =======================================================
    private void drawAntiqueCompassRoseFilled(float w, float h, float yaw) {
        float cx = w - 105f;
        float cy = h - 105f;
        float r = 70f;

        // Circular transparent glass background
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.16f);
        shapeRenderer.circle(cx, cy, r, 48);

        // Waypoint Ticker Pill to the left
        float pillW = 180f;
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
        // 4 Primary Points: 0=N (90° UP), 1=E (0° RIGHT), 2=S (270° DOWN), 3=W (180° LEFT)
        float len1 = r - 10f;
        float baseW1 = 12f;

        for (int i = 0; i < 4; i++) {
            float angleDeg = (90f - i * 90f) - yaw;
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

        // 4 Secondary Points: 0=NE (45°), 1=SE (315°), 2=SW (225°), 3=NW (135°)
        float len2 = r * 0.58f;
        float baseW2 = 8f;

        for (int i = 0; i < 4; i++) {
            float angleDeg = (45f - i * 90f) - yaw;
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
            float tickRad = (90f - i - yaw) * MathUtils.degreesToRadians;
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
        float pillW = 180f;
        float pillH = 28f;
        float pillX = cx - r - pillW - 14f;
        float pillY = cy - pillH / 2f;
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(pillX, pillY, pillW, pillH);
    }

    private void drawAntiqueCompassRoseText(PlayerController player, JulyMemorials memorials, MemorialEntry nearest, float dstToNearest, float w, float h, float yaw) {
        float cx = w - 105f;
        float cy = h - 105f;
        float r = 70f;

        // Cardinal Letters (N, E, S, W): East is clockwise from North
        String[] cardinals = {"N", "E", "S", "W"};
        Color[] cardColors = {healthRed, goldAccent, goldAccent, goldAccent};

        for (int i = 0; i < 4; i++) {
            float rad = (90f - i * 90f - yaw) * MathUtils.degreesToRadians;
            float lx = cx + (r - 18f) * MathUtils.cos(rad) - 5f;
            float ly = cy + (r - 18f) * MathUtils.sin(rad) + 5f;

            fonts.smallFont.setColor(cardColors[i]);
            fonts.smallFont.draw(spriteBatch, cardinals[i], lx, ly);
        }

        // Degree readout beneath
        int displayYaw = (int) ((yaw % 360 + 360) % 360);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, displayYaw + " deg", cx - 18f, cy - r - 8f);

        // Dynamic Waypoint Ticker Text
        float pillW = 180f;
        float pillX = cx - r - pillW - 14f;
        boolean allDone = memorials.isAllInspected();
        if (allDone) {
            fonts.smallFont.setColor(Color.GREEN);
            fonts.smallFont.draw(spriteBatch, "ALL ARCHIVES SECURED", pillX + 20f, cy + 6f);
        } else if (nearest != null) {
            String cardTag = getDirectionTag(player.getPosition(), nearest.position);
            String shortName = getShortLandmarkName(nearest);
            String tickerText = String.format("%s • %s • %.0fm", shortName, cardTag, dstToNearest);
            fonts.smallFont.setColor(goldAccent);
            fonts.smallFont.draw(spriteBatch, tickerText, pillX + 22f, cy + 6f);
        } else {
            fonts.smallFont.setColor(goldAccent);
            fonts.smallFont.draw(spriteBatch, "DHAKA UNIVERSITY", pillX + 24f, cy + 6f);
        }
    }

    // ========================================================
    // 3. BOTTOM-LEFT LIVE HUD PARAMETERS & TELEMETRY PANEL
    // ========================================================
    private void drawParameterHudBg(PlayerController player, JulyMemorials memorials, float w, float h) {
        if (!showParameterHud) {
            // When parameter HUD is toggled OFF, show subtle minimal toggle prompt box
            float bx = 36f;
            float by = 36f;
            float bw = 175f;
            float bh = 28f;
            shapeRenderer.setColor(glassBg);
            shapeRenderer.rect(bx, by, bw, bh);
            return;
        }

        float bx = 36f;
        float by = 36f;
        float bw = 430f;
        float bh = 236f;

        // Dark glass backing
        shapeRenderer.setColor(0.02f, 0.04f, 0.07f, 0.78f);
        shapeRenderer.rect(bx, by, bw, bh);

        // Header strip
        shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.88f);
        shapeRenderer.rect(bx, by + bh - 26f, bw, 26f);

        // Section 3: Mission progress bar track
        float barX = bx + 16f;
        float barY = by + 68f;
        float barW = bw - 32f;
        float barH = 5f;

        shapeRenderer.setColor(0.12f, 0.14f, 0.18f, 0.85f);
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

    private void drawParameterHudBorders(PlayerController player, JulyMemorials memorials, float w, float h) {
        if (!showParameterHud) {
            float bx = 36f;
            float by = 36f;
            float bw = 175f;
            float bh = 28f;
            shapeRenderer.setColor(goldMuted);
            shapeRenderer.rect(bx, by, bw, bh);
            return;
        }

        float bx = 36f;
        float by = 36f;
        float bw = 430f;
        float bh = 236f;

        // Outer border
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(bx, by, bw, bh);

        // Ornate Corner Brackets (⌜ ⌝ ⌞ ⌟)
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

        // Separators between sections
        shapeRenderer.setColor(goldMuted);
        // Below Header
        shapeRenderer.line(bx + 12f, by + bh - 26f, bx + bw - 12f, by + bh - 26f);
        // Between Section 1 (Location) and Section 2 (World)
        shapeRenderer.line(bx + 16f, by + bh - 78f, bx + bw - 16f, by + bh - 78f);
        // Between Section 2 (World) and Section 3 (Mission)
        shapeRenderer.line(bx + 16f, by + bh - 128f, bx + bw - 16f, by + bh - 128f);
        // Between Section 3 (Mission) and Section 4 (System)
        shapeRenderer.line(bx + 16f, by + bh - 182f, bx + bw - 16f, by + bh - 182f);
    }

    private void drawParameterHudText(PlayerController player, JulyMemorials memorials, float cameraYaw, float cameraPitch, float cameraFov, float w, float h) {
        if (!showParameterHud) {
            float bx = 36f;
            float by = 36f;
            fonts.smallFont.setColor(goldAccent);
            fonts.smallFont.draw(spriteBatch, "[F1 / TAB]  SHOW HUD", bx + 12f, by + 19f);
            return;
        }

        float bx = 36f;
        float by = 36f;
        float bw = 430f;
        float bh = 236f;

        Vector3 pos = player.getPosition();
        MemorialEntry nextObj = memorials.getNextObjective(pos);
        int ins = memorials.getInspectedCount();
        int tot = memorials.getTotalCount();

        // Header
        fonts.keyFont.setColor(goldAccent);
        fonts.keyFont.draw(spriteBatch, "CAMPUS TELEMETRY", bx + 16f, by + bh - 8f);
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.88f, 0.9f));
        fonts.smallFont.draw(spriteBatch, "[F1/TAB] HIDE", bx + bw - 98f, by + bh - 9f);

        // ── SECTION 1: LOCATION ──
        float s1Y = by + bh - 32f;
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "[LOC] LOCATION & ORIENTATION", bx + 16f, s1Y - 2f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Zone: " + getCurrentZone(pos), bx + 16f, s1Y - 17f);
        String coordStr = String.format("Pos: %.1fE, %.1fN, %.1fY  |  %s", pos.x, -pos.z, pos.y, getHeadingString(player.getHeadingDegrees()));
        fonts.smallFont.setColor(new Color(0.82f, 0.88f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch, coordStr, bx + 16f, s1Y - 32f);

        // ── SECTION 2: WORLD ──
        float s2Y = by + bh - 84f;
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "[WLD] WORLD METRICS", bx + 16f, s2Y - 2f);
        fonts.smallFont.setColor(new Color(0.92f, 0.92f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch, "Campus Area: 190m × 140m Heritage Grid", bx + 16f, s2Y - 17f);
        fonts.smallFont.draw(spriteBatch, "Heritage Landmarks: 5 Historical Records (1921-2024)", bx + 16f, s2Y - 31f);

        // ── SECTION 3: MISSION ──
        float s3Y = by + bh - 134f;
        fonts.smallFont.setColor(ins >= tot ? Color.GREEN : goldAccent);
        fonts.smallFont.draw(spriteBatch, "[MSN] ACTIVE MISSION CHECKPOINT", bx + 16f, s3Y - 2f);
        fonts.smallFont.setColor(Color.WHITE);
        String targetTitle = nextObj != null ? nextObj.title : "All Checkpoints Secured";
        fonts.smallFont.draw(spriteBatch, "Target: " + targetTitle, bx + 16f, s3Y - 16f);
        float dst = nextObj != null ? pos.dst(nextObj.position) : 0f;
        int pct = tot > 0 ? (ins * 100 / tot) : 0;
        String statusStr = ins >= tot ?
            "Status: 5/5 COMPLETE (100%) - VICTORY SECURED" :
            String.format("Range: %.1fm  |  Progress: %d/%d (%d%%)", dst, ins, tot, pct);
        fonts.smallFont.setColor(ins >= tot ? Color.GREEN : staminaGreen);
        fonts.smallFont.draw(spriteBatch, statusStr, bx + 16f, s3Y - 29f);

        // ── SECTION 4: SYSTEM ──
        float s4Y = by + bh - 188f;
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "[SYS] SYSTEM & RENDER TELEMETRY", bx + 16f, s4Y - 2f);
        int fps = Gdx.graphics.getFramesPerSecond();
        int bufW = Gdx.graphics.getBackBufferWidth();
        int bufH = Gdx.graphics.getBackBufferHeight();
        fonts.smallFont.setColor(new Color(0.85f, 0.90f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch, String.format("Performance: %d FPS  |  Buffer: %d×%d", fps, bufW, bufH), bx + 16f, s4Y - 16f);
        fonts.smallFont.draw(spriteBatch, String.format("Camera: Pitch %.1f°  |  FOV %.0f°  |  Yaw %.0f°", cameraPitch, cameraFov, ((cameraYaw % 360f + 360f) % 360f)), bx + 16f, s4Y - 30f);
    }

    private String getCurrentZone(Vector3 pos) {
        if (pos.dst(0f, 0f, -16f) < 22f || pos.dst(0f, 0f, -32f) < 22f) return "Curzon Hall Arcade";
        if (pos.dst(0f, 0f, 6f) < 16f) return "Curzon Lotus Pond (Pukur)";
        if (pos.dst(-32f, 0f, 26f) < 18f) return "Aparajeyo Bangla (Arts Plaza)";
        if (pos.dst(-58f, 0f, 40f) < 22f) return "Central Library & Hakim Chattar";
        if (pos.dst(-56f, 0f, -16f) < 20f) return "Madhur Canteen Sector";
        if (pos.dst(44f, 0f, 42f) < 18f) return "TSC Raju Sculpture Plaza";
        if (pos.dst(68f, 0f, 20f) < 22f) return "Teacher-Student Centre (TSC)";
        if (pos.dst(56f, 0f, -16f) < 20f) return "Swadhinata Sangram Garden";
        if (pos.dst(0f, 0f, 50f) < 28f) return "Fuller Road Central Promenade";
        return "Dhaka University Campus";
    }

    private String getHeadingString(float headingDeg) {
        float deg = (headingDeg % 360f + 360f) % 360f;
        String[] dirs = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                         "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int idx = Math.round(deg / 22.5f) % 16;
        return String.format("%d° %s", (int) deg, dirs[idx]);
    }

    private String getDirectionTag(Vector3 from, Vector3 to) {
        float dx = to.x - from.x;
        float dz = to.z - from.z;
        float angle = MathUtils.atan2(-dz, dx) * MathUtils.radiansToDegrees;
        float deg = (90f - angle + 360f) % 360f;
        String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int idx = Math.round(deg / 45f) % 8;
        return dirs[idx];
    }

    private String getShortLandmarkName(MemorialEntry entry) {
        if (entry == null) return "CAMPUS";
        switch (entry.id) {
            case 1: return "CURZON HALL";
            case 2: return "APARAJEYO";
            case 3: return "RAJU PLAZA";
            case 4: return "LIBRARY";
            case 5: return "TSC MEMORIAL";
            default: return "MEMORIAL";
        }
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

        // [F1]
        shapeRenderer.rect(kBaseX + 6f, kBaseY, 26f, 20f);

        // [M]
        shapeRenderer.rect(kBaseX + 70f, kBaseY, 26f, 20f);

        // [ESC]
        shapeRenderer.rect(kBaseX + 132f, kBaseY, 36f, 20f);
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

        // [F1]
        shapeRenderer.rect(kBaseX + 6f, kBaseY, 26f, 20f);

        // [M]
        shapeRenderer.rect(kBaseX + 70f, kBaseY, 26f, 20f);

        // [ESC]
        shapeRenderer.rect(kBaseX + 132f, kBaseY, 36f, 20f);
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
        fonts.keyFont.draw(spriteBatch, "F1", kBaseX + 11f, kBaseY + 15f);
        fonts.keyFont.draw(spriteBatch, "M", kBaseX + 77f, kBaseY + 15f);
        fonts.keyFont.draw(spriteBatch, "ESC", kBaseX + 136f, kBaseY + 15f);

        // Action sub-labels matching updated keybinds:
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "MOVE", kBaseX - 6f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "SPRINT", kBaseX + 194f, kBaseY + 72f);
        fonts.smallFont.draw(spriteBatch, "JUMP (2X)", kBaseX + 48f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "INTERACT", kBaseX + 172f, kBaseY + 42f);
        fonts.smallFont.draw(spriteBatch, "HUD", kBaseX + 36f, kBaseY + 15f);
        fonts.smallFont.draw(spriteBatch, "MAP", kBaseX + 100f, kBaseY + 15f);
        fonts.smallFont.draw(spriteBatch, "PAUSE", kBaseX + 172f, kBaseY + 15f);
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
        if (campusMapTex == null && !campusMapLoadTried) loadCampusMapTexture();
        if (campusMapTex != null) {
            renderCampusMapImage(player, memorials, w, h);
            return;
        }

        // Fallback: shape-drawn map, used only if the map image is missing from the assets
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

        // Pukur ring paths: X=+-14m, Z=-10.8 to +24
        float ringBottomY = centerMapY - (24f - 18f) * scale;
        shapeRenderer.rect(centerMapX - 16.4f * scale, ringBottomY, 4.8f * scale, 34.8f * scale);
        shapeRenderer.rect(centerMapX + 11.6f * scale, ringBottomY, 4.8f * scale, 34.8f * scale);

        // North link (Z=-10.8) and south link (Z=+24) joining the promenade to the ring paths
        shapeRenderer.rect(centerMapX - 14f * scale, centerMapY - (-10.8f - 18f) * scale - 2.4f * scale, 28f * scale, 4.8f * scale);
        shapeRenderer.rect(centerMapX - 14f * scale, centerMapY - (24f - 18f) * scale - 2.4f * scale, 28f * scale, 4.8f * scale);

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
        // Pond sits in the centre of the spine; the promenade splits around it
        // ============================================================
        float pukurWorldX = 0f;
        float pukurWorldZ = 6f;
        float pukurWm     = 18f;
        float pukurHm     = 24f;

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
            {-19.5f, -2f}, {19.5f, -2f},
            {-50f, -28f}, {-62f, -28f}, {50f, -28f}, {62f, -28f},
            {-19.5f, 14f}, {19.5f, 14f},
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
        fonts.smallFont.draw(spriteBatch, "Curzon Hall Pukur", pX - 46f, pY + 4f);
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
    // CAMPUS MAP (illustrated map image + live markers)
    // ==========================================
    // The map art was cleaned of its baked-in UI (title bar, menu button, legend, photo panels,
    // quest pins). The top 48px title bar was cropped off, so anchors below stay in the original
    // 843px-tall picture's coordinates and MAP_TOP_CROP is subtracted when converting.
    private static final float MAP_IMG_W = 1264f;
    private static final float MAP_IMG_H = 795f;
    private static final float MAP_TOP_CROP = 48f;

    /**
     * Landmarks whose in-game position is pinned to a spot on the map illustration:
     * {worldX, worldZ, imageX, imageY} with the image origin at the top-left.
     * The illustration is hand-drawn, not to scale, so a plain linear mapping drifts; these
     * anchors pull the markers onto the right buildings (see {@link #worldToMapImage}).
     */
    private static final float[][] MAP_ANCHORS = {
        {0f, 78f, 632f, 745f},      // Main Gate
        {0f, -32f, 630f, 170f},     // Curzon Hall
        {0f, -16f, 630f, 262f},     // Curzon Hall central arcade
        {0f, 6f, 630f, 405f},       // Curzon Hall Pukur
        {-68f, 22f, 150f, 385f},    // Central Library
        {68f, 20f, 1105f, 400f},    // Teacher-Student Centre
        {66f, 20f, 1085f, 410f},    // TSC memorial
        {-56f, -16f, 255f, 130f},   // Madhur Canteen
        {-58f, 54f, 190f, 640f},    // Hakim Chattar
        {-58f, 36f, 200f, 545f},    // Library steps / Hakim memorial
        {-36f, 26f, 440f, 430f},    // Aparajeyo Bangla statue / Arts Plaza
        {-42f, 38f, 345f, 375f},    // Book stalls
        {44f, 42f, 930f, 410f},     // Raju memorial
        {56f, -16f, 1030f, 150f},   // Swadhinata Sangram garden
        {-26f, 52f, 400f, 590f},    // West academic building
        {26f, 52f, 870f, 590f},     // East academic building
    };

    private final com.badlogic.gdx.graphics.g2d.GlyphLayout glyphs = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    private void loadCampusMapTexture() {
        campusMapLoadTried = true;
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("maps/campus_map.png");
            if (file.exists()) {
                campusMapTex = new com.badlogic.gdx.graphics.Texture(file, true);
                campusMapTex.setFilter(
                    com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapLinearLinear,
                    com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            }
        } catch (Exception e) {
            Gdx.app.error("WhereWindsMeetHUD", "Could not load campus map image, using fallback map", e);
        }
    }

    /**
     * Converts a world position to a pixel on the map image. Starts from a linear estimate
     * (gate at the bottom, north up) and bends it toward the landmark anchors: each anchor's
     * error is blended in by inverse-distance weight, so a landmark lands exactly on its
     * building and positions in between are smoothly interpolated.
     */
    private static void worldToMapImage(float wx, float wz, float[] out) {
        float baseX = 632f + 7.0f * wx;
        float baseY = 745f - 4.85f * (78f - wz);

        float corrX = 0f, corrY = 0f, weightSum = 0f;
        for (float[] a : MAP_ANCHORS) {
            float dx = wx - a[0], dz = wz - a[1];
            float d2 = dx * dx + dz * dz;
            if (d2 < 0.01f) {
                out[0] = a[2];
                out[1] = a[3] - MAP_TOP_CROP;
                return;
            }
            float weight = 1f / (d2 * (float) Math.sqrt(d2)); // 1 / d^3: landmarks dominate nearby
            corrX += weight * (a[2] - (632f + 7.0f * a[0]));
            corrY += weight * (a[3] - (745f - 4.85f * (78f - a[1])));
            weightSum += weight;
        }
        out[0] = MathUtils.clamp(baseX + corrX / weightSum, 0f, MAP_IMG_W);
        out[1] = MathUtils.clamp(baseY + corrY / weightSum - MAP_TOP_CROP, 0f, MAP_IMG_H);
    }

    private void renderCampusMapImage(PlayerController player, JulyMemorials memorials, float w, float h) {
        float margin = 34f;
        float fit = Math.min((w - 2f * margin) / MAP_IMG_W, (h - 2f * margin - 16f) / MAP_IMG_H);
        float dw = MAP_IMG_W * fit;
        float dh = MAP_IMG_H * fit;
        float dx = (w - dw) / 2f;
        float dy = (h - dh) / 2f + 8f;

        // Dim the game behind the map
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.94f);
        shapeRenderer.rect(0, 0, w, h);
        shapeRenderer.end();

        spriteBatch.begin();
        spriteBatch.setColor(1f, 1f, 1f, 1f);
        spriteBatch.draw(campusMapTex, dx, dy, dw, dh);
        spriteBatch.end();

        float[] pt = new float[2];
        MemorialEntry nextObj = memorials.getNextObjective(player.getPosition());

        shapeRenderer.begin(ShapeType.Filled);

        // Header bar backing plate
        float cbW = 160f;
        float cbH = 40f;
        float cbX = dx + dw - cbW - 12f;
        float cbY = dy + dh - cbH - 10f;

        shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.88f);
        shapeRenderer.rect(dx + 12f, dy + dh - 50f, dw - cbW - 32f, 40f);

        // Close button box backing plate
        shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.90f);
        shapeRenderer.rect(cbX, cbY, cbW, cbH);

        // Memorial pins: secured = green, current objective = pulsing gold diamond, others = gold
        for (MemorialEntry entry : memorials.getEntries()) {
            worldToMapImage(entry.position.x, entry.position.z, pt);
            float px = dx + pt[0] * fit;
            float py = dy + dh - pt[1] * fit;
            shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
            shapeRenderer.circle(px, py, 11f, 20);
            if (entry == nextObj) {
                // Backing plate so the objective label stays readable over light paths
                glyphs.setText(fonts.smallFont, "OBJECTIVE: " + entry.title);
                shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.82f);
                shapeRenderer.rect(px - 86f, py + 17f, glyphs.width + 12f, 24f);
            }
            if (entry.inspected) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.circle(px, py, 8f, 16);
            } else if (entry == nextObj) {
                float pulse = 0.85f + 0.25f * MathUtils.sin(animTime * 6f);
                shapeRenderer.setColor(1.0f, 0.82f * pulse, 0.25f, 1f);
                float ds = 13f * pulse;
                shapeRenderer.triangle(px, py + ds, px + ds, py, px, py - ds);
                shapeRenderer.triangle(px, py + ds, px - ds, py, px, py - ds);
            } else {
                shapeRenderer.setColor(goldAccent);
                shapeRenderer.circle(px, py, 7f, 16);
            }
        }

        // Live player marker
        Vector3 ppos = player.getPosition();
        worldToMapImage(ppos.x, ppos.z, pt);
        float pMapX = dx + pt[0] * fit;
        float pMapY = dy + dh - pt[1] * fit;
        shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.82f);
        shapeRenderer.rect(pMapX + 15f, pMapY - 9f, 42f, 22f);
        shapeRenderer.setColor(0.2f, 0.9f, 1.0f, 0.30f + 0.18f * MathUtils.sin(animTime * 4f));
        shapeRenderer.circle(pMapX, pMapY, 15f, 24);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(pMapX, pMapY, 7.5f, 18);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(pMapX, pMapY, 5.5f, 18);
        float headRad = (player.getHeadingDegrees() - 90f) * MathUtils.degreesToRadians;
        float tipX = pMapX - 22f * MathUtils.cos(headRad);
        float tipY = pMapY + 22f * MathUtils.sin(headRad);
        float leftX = pMapX - 8f * MathUtils.cos(headRad + 2.2f);
        float leftY = pMapY + 8f * MathUtils.sin(headRad + 2.2f);
        float rightX = pMapX - 8f * MathUtils.cos(headRad - 2.2f);
        float rightY = pMapY + 8f * MathUtils.sin(headRad - 2.2f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(dx, dy, dw, dh);
        // Header & close button outlines
        shapeRenderer.rect(dx + 12f, dy + dh - 50f, dw - cbW - 32f, 40f);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(cbX, cbY, cbW, cbH);
        // Corner brackets on close button
        float cLen = 6f;
        shapeRenderer.line(cbX - 2f, cbY + cbH + 2f, cbX + cLen, cbY + cbH + 2f);
        shapeRenderer.line(cbX - 2f, cbY + cbH + 2f, cbX - 2f, cbY + cbH - cLen);
        shapeRenderer.line(cbX + cbW + 2f, cbY + cbH + 2f, cbX + cbW - cLen, cbY + cbH + 2f);
        shapeRenderer.line(cbX + cbW + 2f, cbY + cbH + 2f, cbX + cbW + 2f, cbY + cbH - cLen);
        shapeRenderer.line(cbX - 2f, cbY - 2f, cbX + cLen, cbY - 2f);
        shapeRenderer.line(cbX - 2f, cbY - 2f, cbX - 2f, cbY + cLen);
        shapeRenderer.line(cbX + cbW + 2f, cbY - 2f, cbX + cbW - cLen, cbY - 2f);
        shapeRenderer.line(cbX + cbW + 2f, cbY - 2f, cbX + cbW + 2f, cbY + cLen);
        shapeRenderer.end();

        // Labels
        spriteBatch.begin();
        // Header Text
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "DHAKA UNIVERSITY TACTICAL MAP", dx + 24f, dy + dh - 16f);
        fonts.smallFont.setColor(new Color(0.85f, 0.90f, 0.95f, 0.95f));
        fonts.smallFont.draw(spriteBatch, "JULY 2024 MASS MOVEMENT HISTORICAL PRECINCT", dx + 24f, dy + dh - 34f);

        // Close button text
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[X] CLOSE MAP", cbX + 18f, cbY + 27f);

        if (nextObj != null) {
            worldToMapImage(nextObj.position.x, nextObj.position.z, pt);
            float px = dx + pt[0] * fit;
            float py = dy + dh - pt[1] * fit;
            String label = "OBJECTIVE: " + nextObj.title;
            fonts.smallFont.setColor(1f, 0.88f, 0.40f, 1f);
            fonts.smallFont.draw(spriteBatch, label, px - 80f, py + 35f);
        }
        fonts.smallFont.setColor(0.55f, 1f, 1f, 1f);
        fonts.smallFont.draw(spriteBatch, "YOU", pMapX + 24f, pMapY + 7f);

        // Bottom Telemetry Bar
        fonts.smallFont.setColor(new Color(0.75f, 0.85f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch, String.format("COORDS: %.1fE, %.1fN  |  ORIENTATION: %s", ppos.x, -ppos.z, getHeadingString(player.getHeadingDegrees())), dx + 12f, dy - 8f);

        if (nextObj != null) {
            float dst = ppos.dst(nextObj.position);
            String objTelemetry = String.format("ACTIVE OBJECTIVE: %s (%.0fm %s)", nextObj.title, dst, getDirectionTag(ppos, nextObj.position));
            fonts.smallFont.setColor(goldAccent);
            fonts.smallFont.draw(spriteBatch, objTelemetry, dx + 450f, dy - 8f);
        }

        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[M] / [ESC]  Close map", dx + dw - 190f, dy - 6f);
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
    // GRAND LEVEL 1 VICTORY / CONGRATULATIONS SCREEN (AAA REDESIGN)
    // ==========================================
    private void renderVictoryScreen(float w, float h) {
        // Initialize celebratory particles
        if (!victorySparksInit) {
            for (int i = 0; i < victorySparks.length; i++) {
                victorySparks[i][0] = MathUtils.random(w * 0.10f, w * 0.90f);
                victorySparks[i][1] = MathUtils.random(h * 0.10f, h * 0.90f);
                victorySparks[i][2] = MathUtils.random(25f, 65f); // upward speed
                victorySparks[i][3] = MathUtils.random(0.35f, 0.95f); // brightness
            }
            victorySparksInit = true;
        }

        // Animate celebratory particles
        float dt = Gdx.graphics.getDeltaTime();
        for (float[] spark : victorySparks) {
            spark[1] += spark[2] * dt;
            if (spark[1] > h * 0.92f) {
                spark[1] = h * 0.08f;
                spark[0] = MathUtils.random(w * 0.10f, w * 0.90f);
            }
        }

        float vw = 820f;
        float vh = 560f;
        float vx = (w - vw) / 2f;
        float vy = (h - vh) / 2f;

        // Mouse hover checks for interactive action buttons
        float backbufferW = (float) Gdx.graphics.getBackBufferWidth();
        float backbufferH = (float) Gdx.graphics.getBackBufferHeight();
        float mouseX = ((float) Gdx.input.getX() / backbufferW) * w;
        float mouseY = (1.0f - (float) Gdx.input.getY() / backbufferH) * h;

        float sw = vw - 80f;
        float cw = (sw - 20f) / 3f;
        float btn1X = vx + 40f;
        float btn2X = btn1X + cw + 10f;
        float btn3X = btn2X + cw + 10f;
        float btnY = vy + 34f;
        float btnH = 46f;

        boolean hoverBtn1 = (mouseX >= btn1X && mouseX <= btn1X + cw && mouseY >= btnY && mouseY <= btnY + btnH);
        boolean hoverBtn2 = (mouseX >= btn2X && mouseX <= btn2X + cw && mouseY >= btnY && mouseY <= btnY + btnH);
        boolean hoverBtn3 = (mouseX >= btn3X && mouseX <= btn3X + cw && mouseY >= btnY && mouseY <= btnY + btnH);

        shapeRenderer.begin(ShapeType.Filled);
        // Dim screen background (translucent: 3D campus remains visible)
        shapeRenderer.setColor(0.01f, 0.02f, 0.04f, 0.76f);
        shapeRenderer.rect(0, 0, w, h);

        // Celebratory Golden Sparkles
        for (float[] spark : victorySparks) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(animTime * 4f + spark[0]);
            shapeRenderer.setColor(1.0f, 0.85f, 0.40f, spark[3] * pulse * 0.8f);
            shapeRenderer.circle(spark[0], spark[1], 2.5f, 10);
        }

        // Translucent glass panel
        shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.90f);
        shapeRenderer.rect(vx, vy, vw, vh);

        // Header band with warm celebratory gold ribbon
        float bannerH = 82f;
        shapeRenderer.setColor(0.08f, 0.07f, 0.04f, 0.95f);
        shapeRenderer.rect(vx, vy + vh - bannerH, vw, bannerH);

        // Pulsing top and bottom gold accent bars
        float pulse = 0.85f + 0.15f * MathUtils.sin(animTime * 3.5f);
        shapeRenderer.setColor(1.0f, 0.84f * pulse, 0.38f, 1f);
        shapeRenderer.rect(vx, vy + vh - 4f, vw, 4f);
        shapeRenderer.rect(vx, vy + vh - bannerH, vw, 2f);

        // 3-Column Statistics Grid
        float statY = vy + 104f;
        float statH = 92f;
        shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.85f);
        shapeRenderer.rect(btn1X, statY, cw, statH);
        shapeRenderer.rect(btn2X, statY, cw, statH);
        shapeRenderer.rect(btn3X, statY, cw, statH);

        // 3 Interactive Action Buttons
        shapeRenderer.setColor(hoverBtn1 ? new Color(0.24f, 0.18f, 0.06f, 0.90f) : new Color(0.06f, 0.08f, 0.12f, 0.75f));
        shapeRenderer.rect(btn1X, btnY, cw, btnH);

        shapeRenderer.setColor(hoverBtn2 ? new Color(0.24f, 0.18f, 0.06f, 0.90f) : new Color(0.06f, 0.08f, 0.12f, 0.75f));
        shapeRenderer.rect(btn2X, btnY, cw, btnH);

        shapeRenderer.setColor(hoverBtn3 ? new Color(0.24f, 0.08f, 0.08f, 0.90f) : new Color(0.06f, 0.08f, 0.12f, 0.75f));
        shapeRenderer.rect(btn3X, btnY, cw, btnH);
        shapeRenderer.end();

        // Lines and Borders
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(vx, vy, vw, vh);

        // Statistics cards borders
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(btn1X, statY, cw, statH);
        shapeRenderer.rect(btn2X, statY, cw, statH);
        shapeRenderer.rect(btn3X, statY, cw, statH);

        // Action button borders (highlighted if hovered)
        shapeRenderer.setColor(hoverBtn1 ? goldAccent : goldBorder);
        shapeRenderer.rect(btn1X, btnY, cw, btnH);
        shapeRenderer.setColor(hoverBtn2 ? goldAccent : goldBorder);
        shapeRenderer.rect(btn2X, btnY, cw, btnH);
        shapeRenderer.setColor(hoverBtn3 ? new Color(1.0f, 0.45f, 0.45f, 1f) : goldBorder);
        shapeRenderer.rect(btn3X, btnY, cw, btnH);

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
        // Header Title
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "MISSION ACCOMPLISHED", vx + 40f, vy + vh - 22f);

        fonts.headerFont.setColor(new Color(0.96f, 0.92f, 0.82f, 1f));
        fonts.headerFont.draw(spriteBatch, "LEVEL 1 COMPLETE — 36 JULY: THE SPARK OF FREEDOM", vx + 40f, vy + vh - 52f);

        // Historical Tribute Narrative
        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch,
            "You have successfully documented all 5 historical checkpoints of the July 2024 Student Mass Uprising across Dhaka University campus.",
            vx + 40f, vy + vh - 105f, vw - 80f, 10, true);

        fonts.bodyFont.setColor(new Color(0.88f, 0.88f, 0.92f, 1f));
        fonts.bodyFont.draw(spriteBatch,
            "From the initial solidarity on the verandas of Curzon Hall to the historic triumph of 36 July (5 August), student courage and citizen solidarity dismantled discrimination. A new dawn of justice, equality, and democratic freedom has emerged for Bangladesh.",
            vx + 40f, vy + vh - 152f, vw - 80f, 10, true);

        // 3 Statistics Cards Content
        // Card 1: Archives
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "HISTORICAL ARCHIVES", btn1X + 16f, statY + statH - 14f);
        fonts.headerFont.setColor(Color.GREEN);
        fonts.headerFont.draw(spriteBatch, "5 / 5 (100%)", btn1X + 16f, statY + statH - 38f);
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.90f, 0.9f));
        fonts.smallFont.draw(spriteBatch, "All Checkpoints Secured", btn1X + 16f, statY + 24f);

        // Card 2: Sector
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "SECTOR EXPLORED", btn2X + 16f, statY + statH - 14f);
        fonts.headerFont.setColor(Color.WHITE);
        fonts.headerFont.draw(spriteBatch, "Dhaka University", btn2X + 16f, statY + statH - 38f);
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.90f, 0.9f));
        fonts.smallFont.draw(spriteBatch, "Curzon Hall to Arts Plaza", btn2X + 16f, statY + 24f);

        // Card 3: Rating
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "MISSION RATING", btn3X + 16f, statY + statH - 14f);
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "GOLD TIER", btn3X + 16f, statY + statH - 38f);
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.90f, 0.9f));
        fonts.smallFont.draw(spriteBatch, "HONOR ROLL • PERFECT", btn3X + 16f, statY + 24f);

        // Action Buttons Text
        fonts.promptFont.setColor(hoverBtn1 ? Color.WHITE : goldAccent);
        fonts.promptFont.draw(spriteBatch, "[ENTER] Main Menu", btn1X + 28f, btnY + 30f);

        fonts.promptFont.setColor(hoverBtn2 ? Color.WHITE : new Color(1f, 0.85f, 0.45f, 1f));
        fonts.promptFont.draw(spriteBatch, "[R] Replay Level", btn2X + 32f, btnY + 30f);

        fonts.promptFont.setColor(hoverBtn3 ? Color.WHITE : new Color(1.0f, 0.50f, 0.45f, 1f));
        fonts.promptFont.draw(spriteBatch, "[X] Exit Game", btn3X + 44f, btnY + 30f);

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
        if (campusMapTex != null) campusMapTex.dispose();
    }
}


