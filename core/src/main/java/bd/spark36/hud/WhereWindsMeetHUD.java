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
 * AAA-inspired dark glassmorphic HUD matching Where Winds Meet visual standard.
 * Features top-left quest card, top-right 360° rotating circular compass rose with objective beacon,
 * bottom-left health & fluid stamina bars, bottom-right PC gamer keycaps, center interaction badge,
 * full-screen archival modal, and tactical campus map overlay.
 * Uses resolution-independent virtual projection for flawless Retina and 4K display.
 */
public class WhereWindsMeetHUD implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // Theme Colors
    private final Color glassBg = new Color(0.04f, 0.06f, 0.09f, 0.82f);
    private final Color glassHeaderBg = new Color(0.02f, 0.04f, 0.06f, 0.94f);
    private final Color goldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color goldAccent = new Color(1.0f, 0.84f, 0.40f, 1f);
    private final Color goldMuted = new Color(0.68f, 0.58f, 0.32f, 0.70f);
    private final Color healthRed = new Color(0.85f, 0.16f, 0.18f, 1f);
    private final Color healthBg = new Color(0.28f, 0.08f, 0.08f, 0.85f);
    private final Color staminaGreen = new Color(0.18f, 0.85f, 0.62f, 1f); // Vibrant jade cyan
    private final Color staminaBg = new Color(0.06f, 0.24f, 0.18f, 0.85f);

    // States
    private MemorialEntry activeModalEntry = null;
    private boolean isMapOpen = false;
    private boolean isPauseMenuOpen = false;

    // Pulse animation timer
    private float animTime = 0f;

    public WhereWindsMeetHUD(FontRenderer fontRenderer) {
        this.fonts = fontRenderer;
    }

    public void update(float delta, PlayerController player, JulyMemorials memorials) {
        animTime += delta;

        // Check for interactive prompt toggle
        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());

        if (nearby != null && (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
            if (activeModalEntry == null) {
                activeModalEntry = nearby;
                nearby.inspected = true;
            } else {
                activeModalEntry = null; // Close
            }
        }

        // Close modal on Escape
        if (activeModalEntry != null && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            activeModalEntry = null;
        }

        // Toggle map on M
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (activeModalEntry == null) {
                isMapOpen = !isMapOpen;
            }
        }

        // Toggle pause menu on ESC if no modal or map is open
        if (activeModalEntry == null && !isMapOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPauseMenuOpen = !isPauseMenuOpen;
        }
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw) {
        int backbufferW = Gdx.graphics.getBackBufferWidth();
        int backbufferH = Gdx.graphics.getBackBufferHeight();

        // Virtual coordinates scaling: 1600 width baseline
        float w = 1600f;
        float h = 1600f * ((float) backbufferH / (float) backbufferW);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        MemorialEntry nearest = memorials.getNextObjective(player.getPosition());
        float dstToNearest = nearest != null ? player.getPosition().dst(nearest.position) : 0f;

        // 1. Draw HUD Background Shapes
        shapeRenderer.begin(ShapeType.Filled);
        drawTopLeftQuestCardBg(w, h);
        drawCompassBg(w, h);
        drawBottomLeftStatusBg(player, w, h);
        drawBottomRightControlsBg(w, h);

        // Center Interaction prompt background if near memorial
        MemorialEntry nearby = memorials.getNearbyMemorial(player.getPosition());
        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            drawInteractionPromptBg(w, h);
        }
        shapeRenderer.end();

        // 2. Draw HUD Border Outlines & Accents
        shapeRenderer.begin(ShapeType.Line);
        drawTopLeftQuestCardBorders(w, h);
        drawCompassDetails(w, h, cameraYaw, player.getPosition(), nearest);
        drawBottomLeftStatusBorders(w, h);
        drawBottomRightControlsBorders(w, h);

        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            drawInteractionPromptBorders(w, h);
        }
        shapeRenderer.end();

        // 3. Draw HUD Typography & Text Labels
        spriteBatch.begin();
        drawTopLeftQuestCardText(memorials, nearest, dstToNearest, w, h);
        drawCompassText(w, h, cameraYaw);
        drawBottomLeftStatusText(player, w, h);
        drawBottomRightControlsText(w, h);

        if (nearby != null && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            drawInteractionPromptText(nearby, w, h);
        }
        spriteBatch.end();

        // 4. Overlays: Historical Modal, Campus Map, or Pause Menu
        if (activeModalEntry != null) {
            renderMemorialModal(activeModalEntry, w, h);
        } else if (isMapOpen) {
            renderCampusMap(player, memorials, cameraYaw, w, h);
        } else if (isPauseMenuOpen) {
            renderPauseMenu(w, h);
        }
    }

    // ==========================================
    // TOP-LEFT QUEST CARD
    // ==========================================
    private void drawTopLeftQuestCardBg(float w, float h) {
        float cardX = 32f;
        float cardY = h - 170f;
        float cardW = 400f;
        float cardH = 140f;

        // Dark glass background
        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(cardX, cardY, cardW, cardH);

        // Header accent bar
        shapeRenderer.setColor(glassHeaderBg);
        shapeRenderer.rect(cardX, cardY + cardH - 28f, cardW, 28f);

        // Gold left vertical accent strip
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(cardX, cardY, 4.5f, cardH);
    }

    private void drawTopLeftQuestCardBorders(float w, float h) {
        float cardX = 32f;
        float cardY = h - 170f;
        float cardW = 400f;
        float cardH = 140f;

        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(cardX, cardY, cardW, cardH);
        shapeRenderer.line(cardX, cardY + cardH - 28f, cardX + cardW, cardY + cardH - 28f);
    }

    private void drawTopLeftQuestCardText(JulyMemorials memorials, MemorialEntry nearest, float dst, float w, float h) {
        float startX = 46f;
        float cardTop = h - 42f;

        // Chapter tag
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "LEVEL 1 : PROLOGUE", startX, cardTop);

        // Title
        fonts.headerFont.setColor(Color.WHITE);
        fonts.headerFont.draw(spriteBatch, "Dhaka University Campus", startX, cardTop - 25f);

        // Objective description
        fonts.bodyFont.setColor(new Color(0.88f, 0.88f, 0.92f, 1f));
        fonts.bodyFont.draw(spriteBatch, "Explore Curzon Hall & historic July movement archives", startX, cardTop - 50f);

        // Nearest target & distance meter
        String targetName = nearest != null ? nearest.title : "All Explored";
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "Waypoint: " + targetName + " (" + (int) dst + "m)", startX, cardTop - 76f);

        // Progress badge
        int inspected = memorials.getInspectedCount();
        int total = memorials.getTotalCount();
        fonts.smallFont.setColor(inspected == total ? Color.GREEN : new Color(0.9f, 0.9f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch, "July Archives Discovered: " + inspected + " / " + total, startX, cardTop - 98f);
    }

    // ==========================================
    // TOP-RIGHT 360° COMPASS ROSE
    // ==========================================
    private void drawCompassBg(float w, float h) {
        float cx = w - 85f;
        float cy = h - 85f;
        float r = 54f;

        shapeRenderer.setColor(glassBg);
        shapeRenderer.circle(cx, cy, r, 36);
    }

    private void drawCompassDetails(float w, float h, float yaw, Vector3 playerPos, MemorialEntry target) {
        float cx = w - 85f;
        float cy = h - 85f;
        float r = 54f;

        // Outer rim
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.circle(cx, cy, r, 40);

        // Inner decorative circle
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.circle(cx, cy, r - 6f, 36);

        // Center crosshair / player point
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cx, cy, 2.5f, 12);

        // Compass cardinal direction ticks
        for (int i = 0; i < 360; i += 30) {
            float relAngle = (i - yaw + 90f) * MathUtils.degreesToRadians;
            float cos = MathUtils.cos(relAngle);
            float sin = MathUtils.sin(relAngle);

            float inner = (i % 90 == 0) ? r - 12f : r - 7f;
            shapeRenderer.setColor(i == 0 ? healthRed : goldMuted);
            shapeRenderer.line(cx + inner * cos, cy + inner * sin, cx + r * cos, cy + r * sin);
        }

        // Objective indicator beacon on compass rim
        if (target != null) {
            float dx = target.position.x - playerPos.x;
            float dz = target.position.z - playerPos.z;
            float targetWorldAngle = MathUtils.atan2(-dz, dx) * MathUtils.radiansToDegrees;
            float relativeToCamera = (targetWorldAngle - (yaw + 90f)) * MathUtils.degreesToRadians;

            float bx = cx + (r - 3f) * MathUtils.cos(relativeToCamera);
            float by = cy + (r - 3f) * MathUtils.sin(relativeToCamera);

            float pulse = 0.5f + 0.5f * MathUtils.sin(animTime * 6f);
            shapeRenderer.setColor(1f, 0.85f * pulse, 0.2f, 1f);
            shapeRenderer.circle(bx, by, 5f, 16);
        }
    }

    private void drawCompassText(float w, float h, float yaw) {
        float cx = w - 85f;
        float cy = h - 85f;
        float r = 54f;

        // Draw North indicator
        float northAngle = (90f - yaw) * MathUtils.degreesToRadians;
        float nx = cx + (r - 19f) * MathUtils.cos(northAngle) - 5f;
        float ny = cy + (r - 19f) * MathUtils.sin(northAngle) + 5f;

        fonts.smallFont.setColor(healthRed);
        fonts.smallFont.draw(spriteBatch, "N", nx, ny);

        // Degree readout
        int displayYaw = (int) ((yaw % 360 + 360) % 360);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, displayYaw + "°", cx - 11f, cy - r - 8f);
    }

    // ==========================================
    // BOTTOM-LEFT STATUS & GAUGES
    // ==========================================
    private void drawBottomLeftStatusBg(PlayerController player, float w, float h) {
        float bx = 32f;
        float by = 30f;
        float barW = 240f;
        float barH = 14f;

        // Health Bar Background
        shapeRenderer.setColor(healthBg);
        shapeRenderer.rect(bx + 52f, by + 28f, barW, barH);

        // Health Bar Fill
        float hpRatio = MathUtils.clamp(player.getHealth() / player.getMaxHealth(), 0f, 1f);
        shapeRenderer.setColor(healthRed);
        shapeRenderer.rect(bx + 52f, by + 28f, barW * hpRatio, barH);

        // Stamina Bar Background
        shapeRenderer.setColor(staminaBg);
        shapeRenderer.rect(bx + 52f, by + 7f, barW, barH);

        // Stamina Bar Fill
        float stRatio = MathUtils.clamp(player.getStamina() / player.getMaxStamina(), 0f, 1f);
        shapeRenderer.setColor(staminaGreen);
        shapeRenderer.rect(bx + 52f, by + 7f, barW * stRatio, barH);

        // Avatar circle background
        shapeRenderer.setColor(glassBg);
        shapeRenderer.circle(bx + 20f, by + 25f, 24f, 28);
    }

    private void drawBottomLeftStatusBorders(float w, float h) {
        float bx = 32f;
        float by = 30f;
        float barW = 240f;
        float barH = 14f;

        // Bar Outlines
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(bx + 52f, by + 28f, barW, barH);
        shapeRenderer.rect(bx + 52f, by + 7f, barW, barH);

        // Avatar outer gold ring
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(bx + 20f, by + 25f, 24f, 28);
        shapeRenderer.circle(bx + 20f, by + 25f, 20f, 24);
    }

    private void drawBottomLeftStatusText(PlayerController player, float w, float h) {
        float bx = 32f;
        float by = 30f;

        // Character Icon "DU" in gold
        fonts.keyFont.setColor(goldAccent);
        fonts.keyFont.draw(spriteBatch, "DU", bx + 11f, by + 32f);

        // Bar labels
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "HP  " + (int) player.getHealth(), bx + 60f, by + 40f);
        fonts.smallFont.draw(spriteBatch, "STA " + (int) player.getStamina(), bx + 60f, by + 19f);

        // Subtitle tag
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "STUDENT EXPLORER  |  JULY 2024", bx + 52f, by + 60f);
    }

    // ==========================================
    // BOTTOM-RIGHT CONTROLS KEYCAPS
    // ==========================================
    private void drawBottomRightControlsBg(float w, float h) {
        float barW = 500f;
        float barH = 36f;
        float barX = w - barW - 32f;
        float barY = 28f;

        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(barX, barY, barW, barH);
    }

    private void drawBottomRightControlsBorders(float w, float h) {
        float barW = 500f;
        float barH = 36f;
        float barX = w - barW - 32f;
        float barY = 28f;

        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(barX, barY, barW, barH);
    }

    private void drawBottomRightControlsText(float w, float h) {
        float barW = 500f;
        float barX = w - barW - 32f;
        float barY = 51f;

        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "[WASD / ARROWS] Move   [SHIFT] Sprint   [SPACE] Jump   [E] Read   [M] Map", barX + 16f, barY);
    }

    // ==========================================
    // CENTER INTERACTION PROMPT
    // ==========================================
    private void drawInteractionPromptBg(float w, float h) {
        float promptW = 360f;
        float promptH = 46f;
        float px = (w - promptW) / 2f;
        float py = 100f;

        shapeRenderer.setColor(glassHeaderBg);
        shapeRenderer.rect(px, py, promptW, promptH);
    }

    private void drawInteractionPromptBorders(float w, float h) {
        float promptW = 360f;
        float promptH = 46f;
        float px = (w - promptW) / 2f;
        float py = 100f;

        float pulse = 0.6f + 0.4f * MathUtils.sin(animTime * 5f);
        shapeRenderer.setColor(1f, 0.82f * pulse, 0.35f, 1f);
        shapeRenderer.rect(px, py, promptW, promptH);
    }

    private void drawInteractionPromptText(MemorialEntry nearby, float w, float h) {
        float promptW = 360f;
        float px = (w - promptW) / 2f;
        float py = 129f;

        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[E]  INSPECT HISTORICAL ARCHIVE", px + 35f, py);
    }

    // ==========================================
    // HISTORICAL MEMORIAL MODAL
    // ==========================================
    private void renderMemorialModal(MemorialEntry entry, float w, float h) {
        // Dim background
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.74f);
        shapeRenderer.rect(0, 0, w, h);

        float mw = 720f;
        float mh = 440f;
        float mx = (w - mw) / 2f;
        float my = (h - mh) / 2f;

        // Modal main glass card
        shapeRenderer.setColor(glassBg);
        shapeRenderer.rect(mx, my, mw, mh);

        // Header band
        shapeRenderer.setColor(glassHeaderBg);
        shapeRenderer.rect(mx, my + mh - 58f, mw, 58f);

        // Top decorative accent
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(mx, my + mh - 4f, mw, 4f);
        shapeRenderer.end();

        // Borders
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mx, my, mw, mh);
        shapeRenderer.line(mx, my + mh - 58f, mx + mw, my + mh - 58f);
        shapeRenderer.line(mx + 30f, my + 60f, mx + mw - 30f, my + 60f);
        shapeRenderer.end();

        // Typography
        spriteBatch.begin();
        float textX = mx + 40f;
        float textTop = my + mh - 22f;

        // Title
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, entry.title, textX, textTop);

        // Metadata: Date & Location
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.90f, 1f));
        fonts.smallFont.draw(spriteBatch, entry.date + "   |   " + entry.location, textX, textTop - 50f);

        // Narrative Description
        fonts.bodyFont.setColor(new Color(0.95f, 0.95f, 0.95f, 1f));
        fonts.bodyFont.draw(spriteBatch, entry.description, textX, textTop - 92f, mw - 80f, 10, true);

        // Historical significance banner
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "HISTORICAL SIGNIFICANCE", textX, my + 135f);
        fonts.smallFont.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        fonts.smallFont.draw(spriteBatch,
            "Documented as part of the student-led democratic reform movement for meritocracy and justice in Bangladesh.",
            textX, my + 105f, mw - 80f, 10, true);

        // Bottom CTA
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

        // Draw Curzon Hall footprint on map
        shapeRenderer.setColor(new Color(0.68f, 0.22f, 0.16f, 0.85f));
        float centerMapX = mx + mapW / 2f;
        float centerMapY = my + mapH / 2f + 40f;
        float scale = 2.6f;

        // Curzon main building
        shapeRenderer.rect(centerMapX - 45f * scale / 2f, centerMapY + 32f * scale - 14f * scale / 2f, 45f * scale, 14f * scale);

        // Campus walkways lines
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.rect(centerMapX - 3.5f * scale / 2f, centerMapY - 50f * scale, 3.5f * scale, 82f * scale);
        shapeRenderer.rect(centerMapX - 60f * scale, centerMapY - 18f * scale - 2.5f * scale / 2f, 120f * scale, 2.5f * scale);

        // Draw Memorials on map
        for (MemorialEntry entry : memorials.getEntries()) {
            float pointX = centerMapX + entry.position.x * scale;
            float pointY = centerMapY - entry.position.z * scale;

            if (entry.inspected) {
                shapeRenderer.setColor(Color.GREEN);
            } else {
                shapeRenderer.setColor(goldAccent);
            }
            shapeRenderer.circle(pointX, pointY, 6.5f, 16);
        }

        // Draw Player position & heading on map
        Vector3 ppos = player.getPosition();
        float pMapX = centerMapX + ppos.x * scale;
        float pMapY = centerMapY - ppos.z * scale;
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(pMapX, pMapY, 5.5f, 16);
        shapeRenderer.end();

        // Map borders
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mx, my, mapW, mapH);
        shapeRenderer.end();

        // Map text
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
