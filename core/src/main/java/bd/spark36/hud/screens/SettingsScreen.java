package bd.spark36.hud.screens;

import bd.spark36.app.GameProfile;
import bd.spark36.app.GameSettings;
import bd.spark36.audio.SoundManager;
import bd.spark36.hud.FontRenderer;
import bd.spark36.render.GraphicsTier;
import bd.spark36.render.RenderPipelineConfig;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * Settings screen shared by the main menu and the pause menu (reference sections 8.5 and 11.3).
 *
 * <p>Keyboard: W/S or Up/Down select, A/D or Left/Right change, Enter/Space toggles or activates,
 * Esc/Backspace goes back. Mouse: hover selects, click the left or right half of a value to step
 * it down or up, click BACK to leave. Every change is applied live and written to
 * spark36_settings.json at once.</p>
 *
 * <p>The caller owns the render targets and passes virtual coordinates (origin bottom-left).</p>
 */
public final class SettingsScreen {

    public enum Row {
        SENSITIVITY("MOUSE SENSITIVITY", "How far the camera turns per mouse movement."),
        MASTER("MASTER VOLUME", "Scales every sound in the game."),
        SFX("EFFECTS VOLUME", "Footsteps, interface, combat and objective sounds."),
        AMBIENCE("AMBIENCE VOLUME", "Background bed: campus birds and wind, distant crowd in Chapters 5-6."),
        SUBTITLES("SUBTITLES", "Shows the translation line under dialogue. The chosen language is always shown."),
        TEXT_SIZE("DIALOGUE TEXT SIZE", "Size of dialogue and subtitle text."),
        REDUCE_MOTION("REDUCE CAMERA MOTION", "Removes head-bob, screen shake and the sprint zoom."),
        GRAPHICS("GRAPHICS QUALITY", "Shadows and terrain detail update on the next level load or restart."),
        TONEMAP("TONE MAPPING", "Colour response of the final image. Also on F8 in game."),
        LANGUAGE("DIALOGUE LANGUAGE", "Main language of dialogue lines. Menus stay in English for now."),
        RESET("RESET TO DEFAULTS", "Restores every setting above to its default."),
        BACK("BACK", "Return to the previous menu.");

        public final String label;
        public final String help;

        Row(String label, String help) {
            this.label = label;
            this.help = help;
        }
    }

    private static final Row[] ROWS = Row.values();

    // Palette shared with the main menu and pause menu
    private final Color glassBg = new Color(0.05f, 0.07f, 0.10f, 0.92f);
    private final Color gold = new Color(1.0f, 0.85f, 0.40f, 1f);
    private final Color goldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color goldMuted = new Color(0.68f, 0.58f, 0.32f, 0.70f);
    private final Color selectedBg = new Color(0.12f, 0.11f, 0.08f, 0.92f);
    private final Color textCol = new Color(0.90f, 0.88f, 0.85f, 1f);
    private final Color dimCol = new Color(0.62f, 0.60f, 0.56f, 0.90f);
    private final GlyphLayout layout = new GlyphLayout();

    private boolean open = false;
    private int selection = 0;
    private String fromLabel = "";

    // Layout (virtual units), recomputed each frame from the viewport
    private float px, py, pw, ph, rowX, rowW, valueX, valueW, firstRowY;
    private static final float ROW_H = 36f;
    private static final float ROW_STEP = 41f;

    public void open(String fromLabel) {
        this.open = true;
        this.selection = 0;
        this.fromLabel = fromLabel != null ? fromLabel : "";
    }

    public void close() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public int getSelection() {
        return selection;
    }

    /** Folder holding spark36_settings.json / spark36_save.json. */
    static String profileFolder() {
        return GameProfile.get().getManager().getStorage().describe();
    }

    /**
     * Screenshot folder as used by the capture code: {@code -Dbd.spark36.screenshotDir} if given,
     * otherwise {@code <home>/Pictures/Spark36}. Display only.
     */
    static String screenshotFolder() {
        String o = System.getProperty("bd.spark36.screenshotDir");
        if (o != null && !o.trim().isEmpty()) return new java.io.File(o.trim()).getAbsolutePath();
        return new java.io.File(new java.io.File(System.getProperty("user.home", "."), "Pictures"), "Spark36").getAbsolutePath();
    }

    /**
     * Test switch {@code -Dbd.spark36.testSettingsMutate=true}: performs the same changes a player
     * would make with the keys (sensitivity up, master volume down twice, subtitles off, text size
     * up, reduce motion on) through the same apply-and-save path, so a second launch can show
     * that they persisted.
     */
    public static void applyScriptedTestChangesIfRequested() {
        if (!"true".equalsIgnoreCase(System.getProperty("bd.spark36.testSettingsMutate"))) return;
        GameProfile p = GameProfile.get();
        GraphicsTier tier = RenderPipelineConfig.activeGraphicsTier;
        int tone = RenderPipelineConfig.toneMappingMode;
        adjust(p.settings(), Row.SENSITIVITY, 1, tier, tone);
        adjust(p.settings(), Row.MASTER, -1, tier, tone);
        adjust(p.settings(), Row.MASTER, -1, tier, tone);
        adjust(p.settings(), Row.SUBTITLES, 1, tier, tone);
        adjust(p.settings(), Row.TEXT_SIZE, 1, tier, tone);
        adjust(p.settings(), Row.REDUCE_MOTION, 1, tier, tone);
        p.settingsChanged();
    }

    /** True when a test run asked for an automatic settings-screen screenshot. */
    public static boolean screenshotTestRequested() {
        return System.getProperty("bd.spark36.testSettingsScreenshot") != null;
    }

    private void computeLayout(float w, float h) {
        pw = Math.min(980f, w - 80f);
        ph = Math.min(h - 60f, 150f + ROWS.length * ROW_STEP + 40f);
        px = (w - pw) / 2f;
        py = (h - ph) / 2f;
        rowX = px + 36f;
        rowW = pw - 72f;
        valueW = 330f;
        valueX = rowX + rowW - valueW;
        firstRowY = py + ph - 112f;
    }

    private float rowY(int i) {
        return firstRowY - i * ROW_STEP;
    }

    // ── Input ─────────────────────────────────────────────────────────────

    /** Handles one frame of input. Mouse coordinates are in the same virtual space as render(). */
    public void update(float w, float h, float mouseX, float mouseY) {
        if (!open) return;
        computeLayout(w, h);

        // Mouse hover + click
        for (int i = 0; i < ROWS.length; i++) {
            float by = rowY(i);
            if (mouseX >= rowX && mouseX <= rowX + rowW && mouseY >= by && mouseY <= by + ROW_H) {
                // Hover selects only while the mouse moves, so a resting cursor never fights the keyboard.
                boolean mouseMoved = Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0;
                if (selection != i && mouseMoved) {
                    selection = i;
                }
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    selection = i;
                    int dir = 1;
                    if (mouseX >= valueX && mouseX < valueX + valueW / 2f) dir = -1;
                    activate(ROWS[i], dir);
                }
                break;
            }
        }
        if (!open) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selection = (selection - 1 + ROWS.length) % ROWS.length;
            click();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selection = (selection + 1) % ROWS.length;
            click();
        }
        Row row = ROWS[selection];
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            if (row != Row.RESET && row != Row.BACK) activate(row, -1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (row != Row.RESET && row != Row.BACK) activate(row, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            activate(row, 1);
        }
        if (open && (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))) {
            close();
            click();
        }
    }

    private void activate(Row row, int dir) {
        GameProfile profile = GameProfile.get();
        if (row == Row.BACK) {
            close();
        } else if (row == Row.RESET) {
            profile.resetSettings();
        } else {
            adjust(profile.settings(), row, dir, RenderPipelineConfig.activeGraphicsTier, RenderPipelineConfig.toneMappingMode);
            profile.settingsChanged();
        }
        click(); // after the change, so the click is heard at the new volume
    }

    private static void click() {
        SoundManager sm = SoundManager.getIfCreated();
        if (sm != null) sm.playClick();
    }

    /**
     * Steps one setting up ({@code dir > 0}) or down. Pure logic, used by the screen and tests.
     *
     * @param effectiveTier tier currently in use (shown when the saved value is "default")
     * @param effectiveTone tone map currently in use
     */
    public static void adjust(GameSettings s, Row row, int dir, GraphicsTier effectiveTier, int effectiveTone) {
        int d = dir >= 0 ? 1 : -1;
        switch (row) {
            case SENSITIVITY:
                s.mouseSensitivity = Math.round((s.mouseSensitivity + 0.25f * d) * 4f) / 4f;
                break;
            case MASTER:
                s.masterVolume = step10(s.masterVolume, d);
                break;
            case SFX:
                s.sfxVolume = step10(s.sfxVolume, d);
                break;
            case AMBIENCE:
                s.ambienceVolume = step10(s.ambienceVolume, d);
                break;
            case SUBTITLES:
                s.subtitles = !s.subtitles;
                break;
            case TEXT_SIZE: {
                int n = GameSettings.TEXT_SCALES.length;
                s.textSizeIndex = Math.max(0, Math.min(n - 1, s.textSizeIndex + d));
                break;
            }
            case REDUCE_MOTION:
                s.reduceCameraMotion = !s.reduceCameraMotion;
                break;
            case GRAPHICS: {
                GraphicsTier[] tiers = GraphicsTier.values(); // LOW, MEDIUM, HIGH
                GraphicsTier cur = effectiveTier != null ? effectiveTier : GraphicsTier.MEDIUM;
                int idx = Math.max(0, Math.min(tiers.length - 1, cur.ordinal() + d));
                s.graphicsTier = tiers[idx].name();
                break;
            }
            case TONEMAP: {
                // Display order ACES (1) -> REINHARD (2) -> LINEAR (0)
                int[] order = {1, 2, 0};
                int pos = 0;
                for (int i = 0; i < order.length; i++) if (order[i] == effectiveTone) pos = i;
                pos = (pos + d + order.length) % order.length;
                s.toneMapping = order[pos];
                break;
            }
            case LANGUAGE:
                s.language = s.language == GameSettings.Language.EN ? GameSettings.Language.BN : GameSettings.Language.EN;
                break;
            default:
                break;
        }
        s.sanitize();
    }

    private static float step10(float v, int d) {
        int pct = Math.round(v * 10f) + d;
        return Math.max(0, Math.min(10, pct)) / 10f;
    }

    /** Text shown in the value column. */
    public static String valueText(GameSettings s, Row row, GraphicsTier effectiveTier, int effectiveTone) {
        switch (row) {
            case SENSITIVITY: return String.format(java.util.Locale.ROOT, "%.2fx", s.mouseSensitivity);
            case MASTER: return Math.round(s.masterVolume * 100f) + "%";
            case SFX: return Math.round(s.sfxVolume * 100f) + "%";
            case AMBIENCE: return Math.round(s.ambienceVolume * 100f) + "%";
            case SUBTITLES: return s.subtitles ? "ON" : "OFF";
            case TEXT_SIZE: return s.textSizeName();
            case REDUCE_MOTION: return s.reduceCameraMotion ? "ON" : "OFF";
            case GRAPHICS: return effectiveTier != null ? effectiveTier.name() : "MEDIUM";
            case TONEMAP: return effectiveTone == 1 ? "ACES FILMIC" : (effectiveTone == 2 ? "REINHARD" : "LINEAR");
            case LANGUAGE: return s.language == GameSettings.Language.BN ? "BANGLA" : "ENGLISH";
            default: return "";
        }
    }

    private static boolean isSlider(Row row) {
        return row == Row.SENSITIVITY || row == Row.MASTER || row == Row.SFX || row == Row.AMBIENCE;
    }

    private static float sliderFraction(GameSettings s, Row row) {
        switch (row) {
            case SENSITIVITY:
                return (s.mouseSensitivity - GameSettings.MIN_SENSITIVITY) / (GameSettings.MAX_SENSITIVITY - GameSettings.MIN_SENSITIVITY);
            case MASTER: return s.masterVolume;
            case SFX: return s.sfxVolume;
            case AMBIENCE: return s.ambienceVolume;
            default: return 0f;
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────

    public void render(ShapeRenderer shapes, SpriteBatch batch, FontRenderer fonts, float w, float h) {
        if (!open) return;
        computeLayout(w, h);
        GameSettings s = GameProfile.get().settings();
        GraphicsTier tier = RenderPipelineConfig.activeGraphicsTier;
        int tone = RenderPipelineConfig.toneMappingMode;

        shapes.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0, 0, w, h);
        shapes.setColor(glassBg);
        shapes.rect(px, py, pw, ph);
        shapes.setColor(gold);
        shapes.rect(px, py + ph - 4f, pw, 4f);
        for (int i = 0; i < ROWS.length; i++) {
            float by = rowY(i);
            boolean sel = i == selection;
            shapes.setColor(sel ? selectedBg : new Color(0.06f, 0.08f, 0.10f, 0.60f));
            shapes.rect(rowX, by, rowW, ROW_H);
            if (sel) {
                shapes.setColor(gold.r, gold.g, gold.b, 0.10f);
                shapes.rect(rowX, by, rowW, ROW_H);
            }
            if (isSlider(ROWS[i])) {
                float barX = valueX + 40f, barW = valueW - 80f, barY = by + 6f;
                shapes.setColor(0.20f, 0.20f, 0.22f, 0.9f);
                shapes.rect(barX, barY, barW, 3f);
                shapes.setColor(sel ? gold : goldMuted);
                shapes.rect(barX, barY, barW * Math.max(0f, Math.min(1f, sliderFraction(s, ROWS[i]))), 3f);
            }
        }
        shapes.end();

        shapes.begin(ShapeType.Line);
        shapes.setColor(goldBorder);
        shapes.rect(px, py, pw, ph);
        for (int i = 0; i < ROWS.length; i++) {
            float by = rowY(i);
            boolean sel = i == selection;
            shapes.setColor(sel ? gold : new Color(goldBorder.r, goldBorder.g, goldBorder.b, 0.35f));
            shapes.rect(rowX, by, rowW, ROW_H);
            if (sel) {
                float cb = 10f;
                shapes.line(rowX - 4f, by + ROW_H + 4f, rowX - 4f + cb, by + ROW_H + 4f);
                shapes.line(rowX - 4f, by + ROW_H + 4f, rowX - 4f, by + ROW_H + 4f - cb);
                shapes.line(rowX + rowW + 4f, by - 4f, rowX + rowW + 4f - cb, by - 4f);
                shapes.line(rowX + rowW + 4f, by - 4f, rowX + rowW + 4f, by - 4f + cb);
            }
        }
        shapes.end();

        batch.begin();
        BitmapFont title = fonts.titleFont;
        title.setColor(gold);
        title.draw(batch, "SETTINGS", px + 36f, py + ph - 22f);
        fonts.smallFont.setColor(goldMuted);
        String sub = "Saved automatically" + (fromLabel.isEmpty() ? "" : "  •  " + fromLabel);
        layout.setText(fonts.smallFont, sub);
        fonts.smallFont.draw(batch, sub, px + pw - 36f - layout.width, py + ph - 34f);

        BitmapFont rowFont = fonts.headerFont;
        for (int i = 0; i < ROWS.length; i++) {
            Row row = ROWS[i];
            float by = rowY(i);
            boolean sel = i == selection;
            Color c = sel ? gold : textCol;
            if (row == Row.BACK && !sel) c = new Color(1f, 0.85f, 0.45f, 0.9f);
            rowFont.setColor(c);
            rowFont.draw(batch, row.label, rowX + 16f, by + ROW_H / 2f + 9f);

            if (row != Row.RESET && row != Row.BACK) {
                String value = valueText(s, row, tier, tone);
                String shown = sel ? "<   " + value + "   >" : value;
                layout.setText(rowFont, shown);
                float vy = isSlider(row) ? by + ROW_H / 2f + 13f : by + ROW_H / 2f + 9f;
                rowFont.setColor(sel ? gold : textCol);
                rowFont.draw(batch, shown, valueX + (valueW - layout.width) / 2f, vy);
            }
        }

        // Read-only locations: where settings and screenshots are stored
        fonts.smallFont.setColor(new Color(0.55f, 0.52f, 0.48f, 0.85f));
        fonts.smallFont.draw(batch, "Settings & save:  " + profileFolder(), rowX, py + 102f);
        fonts.smallFont.draw(batch, "Screenshots:  " + screenshotFolder(), rowX, py + 84f);

        // Help line for the selected row, then key hints
        fonts.bodyFont.setColor(dimCol);
        fonts.bodyFont.draw(batch, ROWS[selection].help, rowX, py + 58f);
        fonts.smallFont.setColor(new Color(0.55f, 0.52f, 0.48f, 0.85f));
        fonts.smallFont.draw(batch, "W/S: Select   •   A/D or Click Left/Right: Change   •   ENTER: Toggle   •   ESC: Back",
            rowX, py + 30f);
        batch.end();
    }
}
