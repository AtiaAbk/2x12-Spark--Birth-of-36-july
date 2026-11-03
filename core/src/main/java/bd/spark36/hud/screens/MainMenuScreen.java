package bd.spark36.hud.screens;

import bd.spark36.app.GameProfile;
import bd.spark36.audio.SoundManager;
import bd.spark36.hud.FontRenderer;
import bd.spark36.level.LevelConfig;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Redesigned High-Tech Cyberpunk Main Menu Screen matching Image 1:
 * - Title: "2x12 SPARK" (replacing SparkVision)
 * - Subtitle: "BIRTH OF 36 JULY"
 * - Dhaka skyline silhouette above the title
 * - Left: Aparajeyo Bangla & Curzon Hall in electric cyan/blue neon
 * - Right: Central Shaheed Minar & red sun in vibrant crimson neon
 * - 3 Primary Menu Buttons: START GAME, SETTINGS, EXIT
 * - Interactive, fully user-friendly with hover animations, sounds, keyboard & mouse controls
 */
public final class MainMenuScreen {

    public enum Action { NONE, CONTINUE, START_CHAPTER, ARENA, EXIT }

    private enum Item { START, SETTINGS, EXIT }

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final FontRenderer fonts;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final SettingsScreen settings = new SettingsScreen();
    private final CreditsScreen credits = new CreditsScreen();

    // Vibrant Sci-Fi Cyberpunk Color Palette
    private final Color bgDark = new Color(0.015f, 0.025f, 0.040f, 1f);
    private final Color cyanGlow = new Color(0.00f, 0.88f, 1.00f, 1f);
    private final Color cyanBorder = new Color(0.00f, 0.75f, 0.95f, 0.85f);
    private final Color greenGlow = new Color(0.00f, 0.95f, 0.50f, 1f);
    private final Color greenBorder = new Color(0.00f, 0.80f, 0.40f, 0.85f);
    private final Color redGlow = new Color(1.00f, 0.22f, 0.30f, 1f);
    private final Color redBorder = new Color(0.90f, 0.15f, 0.25f, 0.85f);
    private final Color textWhite = new Color(0.95f, 0.98f, 1.00f, 1f);
    private final Color btnGlassBg = new Color(0.035f, 0.065f, 0.105f, 0.85f);
    private final Color btnSelectedBg = new Color(0.060f, 0.120f, 0.180f, 0.95f);

    private final List<Item> items = new ArrayList<>();
    private int selection = 0;
    private int selectedChapter = 1;
    private float animTime = 0f;
    private String notice = null;
    private float noticeTimer = 0f;
    private boolean creditsTestStarted = false;

    // Button geometry: wide hexagonal/chamfered pills with spacious breathing room
    private static final float BTN_W = 560f;
    private static final float BTN_H = 56f;
    private static final float BTN_STEP = 92f;

    public MainMenuScreen(ShapeRenderer shapeRenderer, SpriteBatch batch, FontRenderer fonts) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.fonts = fonts;
        rebuildItems();
    }

    public int getSelectedChapter() {
        return selectedChapter;
    }

    public void setSelectedChapter(int chapter) {
        selectedChapter = Math.max(1, Math.min(6, chapter));
    }

    public void onShow() {
        rebuildItems();
        selection = 0;
        settings.close();
        credits.close();
    }

    public void openSettings() {
        settings.open("Main Menu");
    }

    public boolean isSettingsOpen() {
        return settings.isOpen();
    }

    public void openCredits() {
        credits.open("Main Menu");
    }

    public boolean isCreditsOpen() {
        return credits.isOpen();
    }

    private void rebuildItems() {
        items.clear();
        // The 3 user-requested primary options
        items.add(Item.START);
        items.add(Item.SETTINGS);
        items.add(Item.EXIT);
        if (selection >= items.size()) selection = 0;
    }

    public int continueChapter() {
        return GameProfile.get().continueLevel();
    }

    private float virtW() { return 1600f; }

    private float virtH() {
        return 1600f * ((float) Gdx.graphics.getBackBufferHeight() / (float) Math.max(1, Gdx.graphics.getBackBufferWidth()));
    }

    private float btnY(int i, float h) {
        return h * 0.44f - i * BTN_STEP;
    }

    public Action update(float delta) {
        animTime += delta;
        if (noticeTimer > 0f) noticeTimer -= delta;

        float w = virtW(), h = virtH();
        float mouseX = ((float) Gdx.input.getX() / (float) Gdx.graphics.getWidth()) * w;
        float mouseY = (1.0f - (float) Gdx.input.getY() / (float) Gdx.graphics.getHeight()) * h;

        if (settings.isOpen()) {
            settings.update(w, h, mouseX, mouseY);
            if (!settings.isOpen()) rebuildItems();
            return Action.NONE;
        }
        if (CreditsScreen.screenshotTestRequested() && !creditsTestStarted) {
            creditsTestStarted = true;
            openCredits();
        }
        if (credits.isOpen()) {
            credits.update(w, h, mouseX, mouseY, delta);
            return Action.NONE;
        }

        float btnX = w / 2f - BTN_W / 2f;
        boolean clicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        // Mouse hover and selection
        if (mouseX >= btnX - 20f && mouseX <= btnX + BTN_W + 20f) {
            for (int i = 0; i < items.size(); i++) {
                float by = btnY(i, h);
                if (mouseY >= by && mouseY <= by + BTN_H) {
                    if (selection != i) {
                        selection = i;
                        click();
                    }
                    if (clicked) {
                        // Check if clicking left/right arrow on chapter toggle for START button
                        if (items.get(i) == Item.START) {
                            if (mouseX < btnX + 90f) {
                                selectedChapter = (selectedChapter - 2 + 6) % 6 + 1;
                                click();
                                return Action.NONE;
                            } else if (mouseX > btnX + BTN_W - 90f) {
                                selectedChapter = (selectedChapter % 6) + 1;
                                click();
                                return Action.NONE;
                            }
                        }
                        return activate(items.get(i));
                    }
                    break;
                }
            }
        }

        // Keyboard navigation
        int n = items.size();
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selection = (selection - 1 + n) % n;
            click();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selection = (selection + 1) % n;
            click();
        }

        // Left/Right or A/D to cycle chapters when START is selected
        if (items.get(selection) == Item.START) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedChapter = (selectedChapter - 2 + 6) % 6 + 1;
                click();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedChapter = (selectedChapter % 6) + 1;
                click();
            }
        }

        // Enter, Space, or J to activate
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            return activate(items.get(selection));
        }

        // ESC to exit
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return Action.EXIT;
        }

        // C shortcut for Credits & Sources
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            openCredits();
            click();
            return Action.NONE;
        }

        return Action.NONE;
    }

    private Action activate(Item item) {
        switch (item) {
            case START:
                click();
                if (GameProfile.get().hasContinue() && selectedChapter == continueChapter()) {
                    return Action.CONTINUE;
                }
                if (!LevelConfig.isLevelUnlocked(selectedChapter)) {
                    SoundManager.get().playBlock();
                    notice = "CHAPTER " + selectedChapter + " IS LOCKED! Complete Chapter " + (selectedChapter - 1) + " to unlock.";
                    noticeTimer = 3.5f;
                    return Action.NONE;
                }
                return Action.START_CHAPTER;
            case SETTINGS:
                click();
                openSettings();
                return Action.NONE;
            case EXIT:
            default:
                click();
                return Action.EXIT;
        }
    }

    private static void click() {
        SoundManager.get().playClick();
    }

    public static String chapterName(int chapter) {
        switch (chapter) {
            case 1: return "CH 1: CURZON SOLIDARITY (JUL 1-14)";
            case 2: return "CH 2: CONFRONTATION (JUL 15-16)";
            case 3: return "CH 3: POLICE ESCALATION (JUL 17-18)";
            case 4: return "CH 4: CURFEW BLACKOUT (JUL 19-22)";
            case 5: return "CH 5: MASS MOBILIZATION (AUG 3-4)";
            case 6: return "CH 6: VICTORY & 36 JULY (AUG 5)";
            default: return "START CAMPAIGN";
        }
    }

    public void render() {
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();
        float w = virtW(), h = virtH();
        float cx = w / 2f;
        float btnX = cx - BTN_W / 2f;

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        Gdx.gl.glClearColor(bgDark.r, bgDark.g, bgDark.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        // 1. BACKGROUND GRADIENTS & SMOOTH NEON MIST DIFFUSION
        shapeRenderer.begin(ShapeType.Filled);
        // Base dark blue-black gradient
        shapeRenderer.rect(0, 0, w, h,
            new Color(0.010f, 0.020f, 0.035f, 1f), new Color(0.015f, 0.025f, 0.040f, 1f),
            new Color(0.035f, 0.015f, 0.025f, 1f), new Color(0.025f, 0.010f, 0.020f, 1f));

        // Smooth Multi-Layer Radial Glow (Left Cyan)
        float leftBase = 0.025f + 0.010f * MathUtils.sin(animTime * 1.2f);
        for (int step = 0; step < 8; step++) {
            float frac = (8 - step) / 8.0f;
            shapeRenderer.setColor(0.00f, 0.55f, 0.85f, leftBase * frac);
            shapeRenderer.circle(w * 0.16f, h * 0.46f, w * 0.35f * (step + 1) / 8f, 36);
        }

        // Smooth Multi-Layer Radial Glow (Right Crimson)
        float rightBase = 0.025f + 0.010f * MathUtils.cos(animTime * 1.2f);
        for (int step = 0; step < 8; step++) {
            float frac = (8 - step) / 8.0f;
            shapeRenderer.setColor(0.85f, 0.10f, 0.18f, rightBase * frac);
            shapeRenderer.circle(w * 0.84f, h * 0.46f, w * 0.35f * (step + 1) / 8f, 36);
        }

        // Wet Ground Surface Reflection Band (Bottom 26%)
        shapeRenderer.setColor(0.00f, 0.35f, 0.60f, 0.08f);
        shapeRenderer.rect(0, 0, w * 0.5f, h * 0.26f);
        shapeRenderer.setColor(0.60f, 0.05f, 0.12f, 0.08f);
        shapeRenderer.rect(w * 0.5f, 0, w * 0.5f, h * 0.26f);
        shapeRenderer.end();

        // 2. MONUMENT LANDMARKS (Aparajeyo Bangla & Shaheed Minar)
        renderMonuments(shapeRenderer, w, h);

        // 3. TOP SKYLINE SILHOUETTE
        renderSkyline(shapeRenderer, cx, h * 0.72f);

        // 4. OUTER CYBERPUNK HUD FRAME & CORNER ACCENTS
        renderHudFrame(shapeRenderer, w, h);

        // 5. BUTTONS (Hexagonal / Chamfered Sci-Fi Glowing Boxes)
        boolean menuActive = !settings.isOpen() && !credits.isOpen();
        renderButtons(shapeRenderer, btnX, h, menuActive);

        // 6. TEXT & ICONS
        renderText(cx, h, menuActive);

        // 7. SUB-SCREENS (Settings / Credits)
        if (settings.isOpen()) {
            settings.render(shapeRenderer, batch, fonts, w, h);
        }
        if (credits.isOpen()) {
            credits.render(shapeRenderer, batch, fonts, w, h);
        }
    }

    private void renderMonuments(ShapeRenderer sr, float w, float h) {
        // --- Left: Aparajeyo Bangla (Electric Cyan/Blue Neon) ---
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.2f);
        float lx = w * 0.14f;
        float ly = h * 0.30f;

        // Curzon Hall brick arches in mist
        sr.setColor(0.00f, 0.60f, 0.90f, 0.35f);
        for (int i = -1; i <= 1; i++) {
            float archX = lx + i * 85f;
            sr.line(archX - 30f, ly, archX - 30f, ly + 110f);
            sr.line(archX + 30f, ly, archX + 30f, ly + 110f);
            sr.arc(archX, ly + 110f, 30f, 0f, 180f, 16);
        }

        // Aparajeyo Bangla Statue Pedestal
        sr.setColor(cyanGlow);
        sr.line(lx - 65f, ly, lx + 65f, ly);
        sr.line(lx - 65f, ly, lx - 48f, ly + 50f);
        sr.line(lx + 65f, ly, lx + 48f, ly + 50f);
        sr.line(lx - 48f, ly + 50f, lx + 48f, ly + 50f);

        // 3 Freedom Fighters
        // Center (Female student)
        sr.line(lx, ly + 50f, lx, ly + 165f);
        sr.circle(lx, ly + 178f, 13f, 14);
        sr.line(lx - 14f, ly + 130f, lx + 14f, ly + 130f);

        // Left figure (Male student holding rifle/banner)
        sr.line(lx - 26f, ly + 50f, lx - 26f, ly + 172f);
        sr.circle(lx - 26f, ly + 185f, 13f, 14);
        sr.line(lx - 40f, ly + 135f, lx - 12f, ly + 135f);
        sr.line(lx - 35f, ly + 80f, lx - 35f, ly + 215f); // Flag / Rifle pole

        // Right figure (Male student leader)
        sr.line(lx + 26f, ly + 50f, lx + 26f, ly + 175f);
        sr.circle(lx + 26f, ly + 188f, 13f, 14);
        sr.line(lx + 12f, ly + 138f, lx + 40f, ly + 138f);
        sr.end();

        // --- Right: Central Shaheed Minar (Crimson Red Neon) ---
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.2f);
        float rx = w * 0.86f;
        float ry = h * 0.30f;

        // Base platform steps
        sr.setColor(1.00f, 0.20f, 0.28f, 0.40f);
        for (int s = 0; s < 5; s++) {
            float stepW = 145f - s * 22f;
            float stepY = ry + s * 9f;
            sr.line(rx - stepW / 2f, stepY, rx + stepW / 2f, stepY);
        }

        // Circular Red Sun in background
        sr.setColor(1.00f, 0.12f, 0.18f, 0.85f);
        sr.circle(rx, ry + 135f, 44f, 28);

        // Central Tall Bent Pillar
        sr.setColor(redGlow);
        float minarBaseY = ry + 42f;
        sr.line(rx - 18f, minarBaseY, rx - 18f, minarBaseY + 155f);
        sr.line(rx + 18f, minarBaseY, rx + 18f, minarBaseY + 155f);
        sr.line(rx - 18f, minarBaseY + 155f, rx, minarBaseY + 182f);
        sr.line(rx + 18f, minarBaseY + 155f, rx, minarBaseY + 182f);
        sr.line(rx, minarBaseY, rx, minarBaseY + 168f);

        // 4 Flanking Columns
        float[] flankX = {-40f, -56f, 40f, 56f};
        float[] flankH = {125f, 95f, 125f, 95f};
        for (int f = 0; f < flankX.length; f++) {
            float fx = rx + flankX[f];
            float fh = flankH[f];
            sr.line(fx - 4.5f, minarBaseY, fx - 4.5f, minarBaseY + fh);
            sr.line(fx + 4.5f, minarBaseY, fx + 4.5f, minarBaseY + fh);
            sr.line(fx - 4.5f, minarBaseY + fh, fx + 4.5f, minarBaseY + fh);
        }
        sr.end();
    }

    private void renderSkyline(ShapeRenderer sr, float cx, float baseSkyY) {
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        sr.setColor(greenGlow);

        float[] skyX = {-160f, -130f, -100f, -70f, -40f, -15f, 15f, 40f, 70f, 100f, 130f, 160f};
        float[] skyH = {  25f,   55f,   38f,  75f,  60f, 105f, 85f, 65f, 70f,  45f,  60f,  25f};

        for (int i = 0; i < skyX.length - 1; i++) {
            float x0 = cx + skyX[i];
            float x1 = cx + skyX[i + 1];
            float h0 = skyH[i];
            float h1 = skyH[i + 1];

            sr.line(x0, baseSkyY, x0, baseSkyY + h0);
            sr.line(x0, baseSkyY + h0, x1, baseSkyY + h0);
            sr.line(x1, baseSkyY + h0, x1, baseSkyY + h1);
        }
        sr.end();
    }

    private void renderHudFrame(ShapeRenderer sr, float w, float h) {
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        float pad = 24f;
        float arm = 60f;

        // Top-Left (Cyan)
        sr.setColor(cyanGlow);
        sr.line(pad, h - pad, pad + arm, h - pad);
        sr.line(pad, h - pad, pad, h - pad - arm);

        // Bottom-Left (Cyan)
        sr.line(pad, pad, pad + arm, pad);
        sr.line(pad, pad, pad, pad + arm);

        // Top-Right (Red)
        sr.setColor(redGlow);
        sr.line(w - pad, h - pad, w - pad - arm, h - pad);
        sr.line(w - pad, h - pad, w - pad, h - pad - arm);

        // Bottom-Right (Red)
        sr.line(w - pad, pad, w - pad - arm, pad);
        sr.line(w - pad, pad, w - pad, pad + arm);

        // Outer border
        sr.setColor(0.15f, 0.25f, 0.38f, 0.45f);
        sr.line(pad + arm + 15f, h - pad, w - pad - arm - 15f, h - pad);
        sr.line(pad + arm + 15f, pad, w - pad - arm - 15f, pad);
        sr.line(pad, pad + arm + 15f, pad, h - pad - arm - 15f);
        sr.line(w - pad, pad + arm + 15f, w - pad, h - pad - arm - 15f);
        sr.end();
    }

    private void renderButtons(ShapeRenderer sr, float btnX, float h, boolean menuActive) {
        float chamfer = 14f;

        // Button background plates
        sr.begin(ShapeType.Filled);
        for (int i = 0; i < items.size(); i++) {
            float by = btnY(i, h);
            boolean sel = i == selection && menuActive;

            if (sel) {
                sr.setColor(btnSelectedBg);
            } else {
                sr.setColor(btnGlassBg);
            }
            // Draw chamfered button body
            sr.rect(btnX + chamfer, by, BTN_W - chamfer * 2f, BTN_H);
            sr.rect(btnX, by + chamfer, BTN_W, BTN_H - chamfer * 2f);
            sr.triangle(btnX, by + chamfer, btnX + chamfer, by, btnX + chamfer, by + chamfer);
            sr.triangle(btnX + BTN_W - chamfer, by, btnX + BTN_W, by + chamfer, btnX + BTN_W - chamfer, by + chamfer);
            sr.triangle(btnX, by + BTN_H - chamfer, btnX + chamfer, by + BTN_H, btnX + chamfer, by + BTN_H - chamfer);
            sr.triangle(btnX + BTN_W - chamfer, by + BTN_H, btnX + BTN_W, by + BTN_H - chamfer, btnX + BTN_W - chamfer, by + BTN_H - chamfer);

            // Pulsing highlight when selected
            if (sel) {
                float pulse = 0.08f + 0.04f * MathUtils.sin(animTime * 5.0f);
                Color c = (i == 0) ? cyanGlow : ((i == 1) ? greenGlow : redGlow);
                sr.setColor(c.r, c.g, c.b, pulse);
                sr.rect(btnX + chamfer, by, BTN_W - chamfer * 2f, BTN_H);
            }
        }
        sr.end();

        // Glowing Chamfered Borders & Chevrons
        sr.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2.2f);
        for (int i = 0; i < items.size(); i++) {
            float by = btnY(i, h);
            boolean sel = i == selection && menuActive;

            Color borderCol;
            if (i == 0) {
                borderCol = sel ? cyanGlow : cyanBorder;
            } else if (i == 1) {
                borderCol = sel ? greenGlow : greenBorder;
            } else {
                borderCol = sel ? redGlow : redBorder;
            }

            sr.setColor(borderCol);

            // Hexagonal chamfered perimeter
            sr.line(btnX + chamfer, by, btnX + BTN_W - chamfer, by);
            sr.line(btnX + BTN_W - chamfer, by, btnX + BTN_W, by + chamfer);
            sr.line(btnX + BTN_W, by + chamfer, btnX + BTN_W, by + BTN_H - chamfer);
            sr.line(btnX + BTN_W, by + BTN_H - chamfer, btnX + BTN_W - chamfer, by + BTN_H);
            sr.line(btnX + BTN_W - chamfer, by + BTN_H, btnX + chamfer, by + BTN_H);
            sr.line(btnX + chamfer, by + BTN_H, btnX, by + BTN_H - chamfer);
            sr.line(btnX, by + BTN_H - chamfer, btnX, by + chamfer);
            sr.line(btnX, by + chamfer, btnX + chamfer, by);

            // Side sci-fi tech chevrons « »
            if (sel) {
                float chvX0 = btnX - 18f;
                float chvX1 = btnX + BTN_W + 18f;
                float midY = by + BTN_H / 2f;
                sr.line(chvX0, midY, chvX0 + 8f, midY + 12f);
                sr.line(chvX0, midY, chvX0 + 8f, midY - 12f);
                sr.line(chvX1, midY, chvX1 - 8f, midY + 12f);
                sr.line(chvX1, midY, chvX1 - 8f, midY - 12f);
            }
        }
        sr.end();
    }

    private void renderText(float cx, float h, boolean menuActive) {
        batch.begin();

        // 1. GAME TITLE: "2x12 SPARK" (Prominent, replacing Spark Vision)
        float titleY = h * 0.70f;
        fonts.titleFont.setColor(0.00f, 0.85f, 1.00f, 0.40f);
        drawCentered(fonts.titleFont, "2x12 SPARK", cx + 3f, titleY + 2f);
        fonts.titleFont.setColor(1.00f, 0.20f, 0.30f, 0.40f);
        drawCentered(fonts.titleFont, "2x12 SPARK", cx - 3f, titleY + 2f);

        fonts.titleFont.setColor(textWhite);
        drawCentered(fonts.titleFont, "2x12 SPARK", cx, titleY);

        // Subtitle: "BIRTH OF 36 JULY"
        fonts.headerFont.setColor(new Color(1.00f, 0.85f, 0.25f, 0.95f));
        drawCentered(fonts.headerFont, "B I R T H   O F   3 6   J U L Y", cx, titleY - 34f);

        // Campus context badge
        fonts.smallFont.setColor(new Color(0.50f, 0.65f, 0.80f, 0.85f));
        drawCentered(fonts.smallFont, "July 2024  •  Dhaka University Campus  •  Student Movement", cx, titleY - 60f);

        // 2. MENU BUTTON LABELS & ICONS
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean sel = i == selection && menuActive;
            float by = btnY(i, h);
            float textY = by + BTN_H / 2f + 8f;

            String label;
            Color col;
            switch (item) {
                case START:
                    label = (GameProfile.get().hasContinue() && selectedChapter == continueChapter())
                        ? "CONTINUE GAME"
                        : "START GAME";
                    col = sel ? cyanGlow : textWhite;
                    break;
                case SETTINGS:
                    label = "SETTINGS";
                    col = sel ? greenGlow : textWhite;
                    break;
                case EXIT:
                default:
                    label = "EXIT";
                    col = sel ? redGlow : new Color(0.85f, 0.75f, 0.75f, 0.95f);
                    break;
            }

            fonts.headerFont.setColor(col);
            drawCentered(fonts.headerFont, label, cx, textY);

            // Sub-detail on START button: selected chapter navigation
            if (item == Item.START) {
                boolean unlocked = LevelConfig.isLevelUnlocked(selectedChapter);
                String chName = chapterName(selectedChapter) + (unlocked ? "" : " [LOCKED]");
                fonts.smallFont.setColor(sel ? (unlocked ? cyanGlow : new Color(1f, 0.4f, 0.4f, 1f)) : new Color(0.6f, 0.7f, 0.8f, 0.75f));
                String sub = sel ? "<  " + chName + "  >" : chName;
                drawCentered(fonts.smallFont, sub, cx, by - 16f);
            }
        }

        // 3. NOTICE BANNER (if chapter is locked)
        if (noticeTimer > 0f && notice != null) {
            fonts.smallFont.setColor(new Color(1f, 0.35f, 0.35f, Math.min(1f, noticeTimer)));
            drawCentered(fonts.smallFont, notice, cx, btnY(0, h) + BTN_H + 28f);
        }

        // 4. BOTTOM CONTROLS FOOTER
        float footerY = 42f;
        fonts.smallFont.setColor(new Color(0.45f, 0.55f, 0.68f, 0.85f));
        drawCentered(fonts.smallFont,
            "[MOUSE] SELECT    •    [UP / DOWN] NAVIGATE    •    [ENTER / SPACE] CONFIRM    •    [ESC] EXIT",
            cx, footerY);

        fonts.smallFont.setColor(new Color(0.35f, 0.45f, 0.55f, 0.65f));
        drawCentered(fonts.smallFont,
            "2x12 SPARK  •  Atia Sanjida  •  ICE, BAUET  •  Student Code: 2x12",
            cx, footerY - 20f);

        batch.end();
    }

    private void drawCentered(BitmapFont font, String text, float cx, float y) {
        glyphLayout.setText(font, text);
        font.draw(batch, text, cx - glyphLayout.width / 2f, y);
    }
}
