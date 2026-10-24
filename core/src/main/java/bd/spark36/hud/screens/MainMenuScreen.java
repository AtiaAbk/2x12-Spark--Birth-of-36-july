package bd.spark36.hud.screens;

import bd.spark36.app.GameProfile;
import bd.spark36.audio.SoundManager;
import bd.spark36.hud.FontRenderer;
import bd.spark36.level.LevelConfig;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Title screen (Where Winds Meet aesthetic), moved out of SparkGame so the menu can grow without
 * growing the frozen game class.
 *
 * <p>Entries: CONTINUE (only when a save exists), the chapter selector, the martial arts arena,
 * SETTINGS, CREDITS (credits and historical sources) and EXIT. W/S or arrows move, A/D or arrows pick the chapter, Enter/Space/J select;
 * the mouse hovers and clicks as before. SparkGame acts on the returned {@link Action}.</p>
 */
public final class MainMenuScreen {

    public enum Action { NONE, CONTINUE, START_CHAPTER, ARENA, EXIT }

    private enum Item { CONTINUE, CHAPTER, ARENA, SETTINGS, CREDITS, EXIT }

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final FontRenderer fonts;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final SettingsScreen settings = new SettingsScreen();
    private final CreditsScreen credits = new CreditsScreen();

    private final Color glassBg = new Color(0.05f, 0.07f, 0.10f, 0.88f);
    private final Color goldAccent = new Color(1.0f, 0.85f, 0.40f, 1f);
    private final Color goldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color goldMuted = new Color(0.68f, 0.58f, 0.32f, 0.70f);
    private final Color selectedBg = new Color(0.12f, 0.11f, 0.08f, 0.92f);

    private final List<Item> items = new ArrayList<>();
    private int selection = 0;
    private int selectedChapter = 1;
    private float animTime = 0f;
    private String notice = null;
    private boolean creditsTestStarted = false;
    private float noticeTimer = 0f;

    private static final float BTN_W = 480f;
    private static final float BTN_H = 46f;
    private static final float BTN_STEP = 58f;

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

    /** Back from gameplay: refresh Continue, highlight the first entry. */
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
        if (GameProfile.get().hasContinue()) items.add(Item.CONTINUE);
        items.add(Item.CHAPTER);
        items.add(Item.ARENA);
        items.add(Item.SETTINGS);
        items.add(Item.CREDITS);
        items.add(Item.EXIT);
        if (selection >= items.size()) selection = 0;
    }

    /** Chapter Continue resumes. */
    public int continueChapter() {
        return GameProfile.get().continueLevel();
    }

    private float virtW() { return 1600f; }

    private float virtH() {
        return 1600f * ((float) Gdx.graphics.getBackBufferHeight() / (float) Math.max(1, Gdx.graphics.getBackBufferWidth()));
    }

    private float btnY(int i, float h) {
        // Seven entries (with CONTINUE) start a little higher and pack closer so the list clears the footer.
        if (items.size() > 6) return h * 0.47f - i * (BTN_STEP - 6f);
        return h * 0.44f - i * BTN_STEP;
    }

    /** Handles input for one frame and returns what SparkGame should do. */
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
        if (mouseX >= btnX && mouseX <= btnX + BTN_W) {
            for (int i = 0; i < items.size(); i++) {
                float by = btnY(i, h);
                if (mouseY >= by && mouseY <= by + BTN_H) {
                    if (selection != i) {
                        selection = i;
                        click();
                    }
                    if (clicked) return activate(items.get(i));
                    break;
                }
            }
        }

        int n = items.size();
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selection = (selection - 1 + n) % n;
            click();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selection = (selection + 1) % n;
            click();
        }
        if (items.get(selection) == Item.CHAPTER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedChapter = (selectedChapter - 2 + 6) % 6 + 1;
                click();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedChapter = (selectedChapter % 6) + 1;
                click();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            return activate(items.get(selection));
        }
        return Action.NONE;
    }

    private Action activate(Item item) {
        switch (item) {
            case CONTINUE:
                click();
                return Action.CONTINUE;
            case CHAPTER:
                if (!LevelConfig.isLevelUnlocked(selectedChapter)) {
                    SoundManager.get().playBlock();
                    notice = "CHAPTER " + selectedChapter + " IS LOCKED! Complete Chapter " + (selectedChapter - 1) + " to unlock.";
                    noticeTimer = 3.5f;
                    return Action.NONE;
                }
                click();
                return Action.START_CHAPTER;
            case ARENA:
                click();
                return Action.ARENA;
            case SETTINGS:
                click();
                openSettings();
                return Action.NONE;
            case CREDITS:
                click();
                openCredits();
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

    private static String chapterName(int chapter) {
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

    private static String shortChapterName(int chapter) {
        String n = chapterName(chapter);
        int colon = n.indexOf(':');
        int paren = n.indexOf(" (");
        return (colon > 0 && paren > colon) ? n.substring(colon + 2, paren) : n;
    }

    public void render() {
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();
        float w = virtW(), h = virtH();
        float centerX = w / 2f;
        float btnX = centerX - BTN_W / 2f;

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        Gdx.gl.glClearColor(0.02f, 0.04f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        // Background: atmospheric dark gradient, mist band, warm horizon glow
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.rect(0, 0, w, h,
            new Color(0.02f, 0.03f, 0.06f, 1f), new Color(0.03f, 0.04f, 0.07f, 1f),
            new Color(0.08f, 0.06f, 0.12f, 1f), new Color(0.06f, 0.05f, 0.10f, 1f));
        shapeRenderer.setColor(0.10f, 0.08f, 0.06f, 0.12f);
        shapeRenderer.rect(0, h * 0.15f, w, h * 0.15f);
        float glowPulse = 0.04f + 0.015f * MathUtils.sin(animTime * 0.8f);
        shapeRenderer.setColor(0.25f, 0.18f, 0.08f, glowPulse);
        shapeRenderer.rect(0, h * 0.35f, w, h * 0.12f);
        shapeRenderer.end();

        float titleBlockY = h * 0.62f;
        boolean menuActive = !settings.isOpen() && !credits.isOpen();

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(goldAccent);
        float d = 8f;
        shapeRenderer.triangle(centerX, titleBlockY + 70f + d, centerX - d, titleBlockY + 70f, centerX + d, titleBlockY + 70f);
        shapeRenderer.triangle(centerX, titleBlockY + 70f - d, centerX - d, titleBlockY + 70f, centerX + d, titleBlockY + 70f);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(centerX - 120f, titleBlockY + 55f, 240f, 1.5f);

        for (int i = 0; i < items.size(); i++) {
            float by = btnY(i, h);
            if (i == selection && menuActive) {
                shapeRenderer.setColor(selectedBg);
                shapeRenderer.rect(btnX - 4f, by - 4f, BTN_W + 8f, BTN_H + 8f);
                shapeRenderer.setColor(goldAccent.r, goldAccent.g, goldAccent.b, 0.08f);
                shapeRenderer.rect(btnX, by, BTN_W, BTN_H);
            } else {
                shapeRenderer.setColor(glassBg);
                shapeRenderer.rect(btnX, by, BTN_W, BTN_H);
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        for (int i = 0; i < items.size(); i++) {
            shapeRenderer.setColor(i == selection && menuActive ? goldAccent : goldBorder);
            shapeRenderer.rect(btnX, btnY(i, h), BTN_W, BTN_H);
        }
        if (menuActive) {
            float selY = btnY(selection, h);
            shapeRenderer.setColor(goldAccent);
            float cb = 14f;
            shapeRenderer.line(btnX - 6f, selY + BTN_H + 6f, btnX - 6f, selY + BTN_H + 6f - cb);
            shapeRenderer.line(btnX - 6f, selY + BTN_H + 6f, btnX - 6f + cb, selY + BTN_H + 6f);
            shapeRenderer.line(btnX + BTN_W + 6f, selY + BTN_H + 6f, btnX + BTN_W + 6f, selY + BTN_H + 6f - cb);
            shapeRenderer.line(btnX + BTN_W + 6f, selY + BTN_H + 6f, btnX + BTN_W + 6f - cb, selY + BTN_H + 6f);
            shapeRenderer.line(btnX - 6f, selY - 6f, btnX - 6f, selY - 6f + cb);
            shapeRenderer.line(btnX - 6f, selY - 6f, btnX - 6f + cb, selY - 6f);
            shapeRenderer.line(btnX + BTN_W + 6f, selY - 6f, btnX + BTN_W + 6f, selY - 6f + cb);
            shapeRenderer.line(btnX + BTN_W + 6f, selY - 6f, btnX + BTN_W + 6f - cb, selY - 6f);
        }
        shapeRenderer.end();

        batch.begin();
        fonts.titleFont.setColor(goldAccent);
        drawCentered(fonts.titleFont, "2x12: SPARK", centerX, titleBlockY + 45f);
        fonts.headerFont.setColor(new Color(0.92f, 0.88f, 0.78f, 1f));
        drawCentered(fonts.headerFont, "Birth of 36 July", centerX, titleBlockY + 14f);
        fonts.smallFont.setColor(goldMuted);
        drawCentered(fonts.smallFont, "July 2024  •  Dhaka University Campus", centerX, titleBlockY - 14f);

        Color normal = new Color(0.85f, 0.82f, 0.75f, 1f);
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean sel = i == selection && menuActive;
            float textY = btnY(i, h) + BTN_H / 2f + 8f;
            String label;
            Color col;
            switch (item) {
                case CONTINUE:
                    label = "CONTINUE  •  CH " + continueChapter() + ": " + shortChapterName(continueChapter());
                    col = sel ? goldAccent : normal;
                    break;
                case CHAPTER: {
                    boolean unlocked = LevelConfig.isLevelUnlocked(selectedChapter);
                    String name = chapterName(selectedChapter) + (unlocked ? "" : " [LOCKED]");
                    label = sel ? "<  " + name + "  >" : name;
                    if (sel) col = unlocked ? goldAccent : new Color(0.85f, 0.45f, 0.45f, 0.95f);
                    else col = unlocked ? normal : new Color(0.55f, 0.50f, 0.50f, 0.7f);
                    break;
                }
                case ARENA:
                    label = "MARTIAL ARTS ARENA (COMBAT)";
                    col = sel ? goldAccent : normal;
                    break;
                case SETTINGS:
                    label = "SETTINGS";
                    col = sel ? goldAccent : normal;
                    break;
                case CREDITS:
                    label = "CREDITS & SOURCES";
                    col = sel ? goldAccent : normal;
                    break;
                case EXIT:
                default:
                    label = "EXIT GAME";
                    col = sel ? new Color(0.95f, 0.40f, 0.35f, 1f) : new Color(0.70f, 0.65f, 0.60f, 1f);
                    break;
            }
            fonts.headerFont.setColor(col);
            drawCentered(fonts.headerFont, label, centerX, textY);
        }

        if (noticeTimer > 0f && notice != null) {
            fonts.smallFont.setColor(new Color(1f, 0.40f, 0.35f, Math.min(1f, noticeTimer)));
            drawCentered(fonts.smallFont, notice, centerX, btnY(0, h) + BTN_H + 26f);
        }

        float lastY = btnY(items.size() - 1, h);
        fonts.smallFont.setColor(new Color(0.55f, 0.52f, 0.48f, 0.8f));
        String navHint = (menuActive && items.get(selection) == Item.CHAPTER)
            ? "W/S: Navigate  •  A/D or Arrows: Chapter  •  ENTER: Start"
            : "W/S or Arrow Keys: Navigate    •    ENTER or SPACE: Select";
        drawCentered(fonts.smallFont, navHint, centerX, lastY - 26f);

        fonts.smallFont.setColor(new Color(0.42f, 0.40f, 0.38f, 0.7f));
        drawCentered(fonts.smallFont, "Atia Sanjida  •  ICE, BAUET  •  Student Code: 2x12", centerX, 45f);
        batch.end();

        if (settings.isOpen()) {
            settings.render(shapeRenderer, batch, fonts, w, h);
        }
        if (credits.isOpen()) {
            credits.render(shapeRenderer, batch, fonts, w, h);
        }
    }

    private void drawCentered(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float cx, float y) {
        glyphLayout.setText(font, text);
        font.draw(batch, text, cx - glyphLayout.width / 2f, y);
    }
}
