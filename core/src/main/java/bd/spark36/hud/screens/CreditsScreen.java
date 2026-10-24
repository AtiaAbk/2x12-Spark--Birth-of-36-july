package bd.spark36.hud.screens;

import bd.spark36.audio.SoundManager;
import bd.spark36.history.SourceCatalog;
import bd.spark36.hud.FontRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;

import java.util.List;

/**
 * Credits &amp; Sources overlay, opened from the main menu (CREDITS) and by the last Level 6
 * objective. Content comes from {@link CreditsContent}; the source list is read at run time from
 * {@code assets/data/historical_sources.json}.
 *
 * <p>Keyboard: W/S or Up/Down scroll, Page Up/Down and Home/End jump, Esc/Backspace closes.
 * Mouse: wheel scrolls, click BACK closes. The caller passes virtual coordinates (origin
 * bottom-left), as with {@link SettingsScreen}.</p>
 */
public final class CreditsScreen {

    private static final float LINE_GAP = 6f;
    private static final float GAP_H = 14f;
    private static final float SCROLL_SPEED = 620f;   // virtual units per second while a key is held
    private static final float WHEEL_STEP = 70f;

    private final Color glassBg = new Color(0.05f, 0.07f, 0.10f, 0.98f);
    private final Color gold = new Color(1.0f, 0.85f, 0.40f, 1f);
    private final Color goldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color goldMuted = new Color(0.68f, 0.58f, 0.32f, 0.85f);
    private final Color textCol = new Color(0.90f, 0.88f, 0.85f, 1f);
    private final Color dimCol = new Color(0.66f, 0.63f, 0.58f, 0.95f);
    private final Color linkCol = new Color(0.55f, 0.75f, 0.95f, 0.95f);
    private final GlyphLayout layout = new GlyphLayout();

    private final Wheel wheel = new Wheel();
    private List<CreditsContent.Line> lines;
    private float[] heights;
    private float measuredWidth = -1f;
    private float contentH = 0f;

    private boolean open = false;
    private String fromLabel = "";
    private float scroll = 0f;
    private float viewH = 1f;

    // Layout (virtual units), recomputed each frame
    private float px, py, pw, ph, cx, cy, cw, ch, backX, backY, backW, backH;

    public void open(String fromLabel) {
        if (lines == null) lines = CreditsContent.build(SourceCatalog.get());
        open = true;
        scroll = 0f;
        measuredWidth = -1f;
        this.fromLabel = fromLabel != null ? fromLabel : "";
        wheel.install();
        testStep = 0;
        testTime = 0f;
    }

    public void close() {
        if (!open) return;
        open = false;
        wheel.uninstall();
    }

    public boolean isOpen() {
        return open;
    }

    public float getScroll() {
        return scroll;
    }

    private void computeLayout(float w, float h) {
        pw = Math.min(1180f, w - 80f);
        ph = h - 70f;
        px = (w - pw) / 2f;
        py = (h - ph) / 2f;
        cx = px + 44f;
        cw = pw - 88f;
        cy = py + 64f;                 // content bottom (above the hint bar)
        ch = ph - 64f - 92f;           // below the title bar
        backW = 150f;
        backH = 34f;
        backX = px + pw - 44f - backW;
        backY = py + 18f;
        viewH = ch;
    }

    private BitmapFont fontFor(CreditsContent.Line l, FontRenderer fonts) {
        if (containsBengali(l.text) && fonts.bengaliFont != null) return fonts.bengaliFont;
        switch (l.kind) {
            case TITLE: return fonts.titleFont;
            case HEADER:
            case LABEL: return fonts.headerFont;
            case TEXT: return fonts.bodyFont;
            default: return fonts.smallFont;
        }
    }

    static boolean containsBengali(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'ঀ' && c <= '৿') return true;
        }
        return false;
    }

    private void measure(FontRenderer fonts) {
        if (measuredWidth == cw && heights != null) return;
        heights = new float[lines.size()];
        float total = 0f;
        for (int i = 0; i < lines.size(); i++) {
            CreditsContent.Line l = lines.get(i);
            float lh;
            if (l.kind == CreditsContent.Kind.GAP) lh = GAP_H;
            else {
                layout.setText(fontFor(l, fonts), l.text, Color.WHITE, cw, Align.left, true);
                lh = layout.height + LINE_GAP + (l.kind == CreditsContent.Kind.HEADER ? 10f : 0f)
                    + (l.kind == CreditsContent.Kind.LINK ? 6f : 0f); // breathing room after each source
            }
            heights[i] = lh;
            total += lh;
        }
        contentH = total;
        measuredWidth = cw;
    }

    private float maxScroll() {
        return Math.max(0f, contentH - viewH + 20f);
    }

    /** Scroll offset at which the first line with this text is at the top of the view. */
    public float offsetOf(String text) {
        if (heights == null) return 0f;
        float y = 0f;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).text.startsWith(text)) return Math.min(maxScroll(), y);
            y += heights[i];
        }
        return 0f;
    }

    public void scrollTo(float s) {
        scroll = Math.max(0f, Math.min(maxScroll(), s));
    }

    // ── Input ─────────────────────────────────────────────────────────────

    public void update(float w, float h, float mouseX, float mouseY, float delta) {
        if (!open) return;
        computeLayout(w, h);
        float d = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) d += SCROLL_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) d -= SCROLL_SPEED * delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) d += viewH * 0.85f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) d -= viewH * 0.85f;
        d += wheel.consume() * WHEEL_STEP;
        if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) scroll = 0f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.END)) scroll = maxScroll();
        if (heights != null) scrollTo(scroll + d);

        boolean overBack = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;
        if ((overBack && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            close();
            SoundManager sm = SoundManager.getIfCreated();
            if (sm != null) sm.playClick();
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────

    public void render(ShapeRenderer shapes, SpriteBatch batch, FontRenderer fonts, float w, float h) {
        if (!open) return;
        computeLayout(w, h);
        measure(fonts);
        scrollTo(scroll);

        shapes.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.80f);
        shapes.rect(0, 0, w, h);
        shapes.setColor(glassBg);
        shapes.rect(px, py, pw, ph);
        shapes.setColor(gold);
        shapes.rect(px, py + ph - 4f, pw, 4f);
        shapes.setColor(goldBorder.r, goldBorder.g, goldBorder.b, 0.45f);
        shapes.rect(px + 36f, cy + ch + 8f, pw - 72f, 1.5f);
        shapes.rect(px + 36f, cy - 8f, pw - 72f, 1.5f);
        // Scroll bar
        float max = maxScroll();
        if (max > 0f) {
            float trackX = px + pw - 22f, trackH = ch;
            shapes.setColor(0.20f, 0.20f, 0.22f, 0.9f);
            shapes.rect(trackX, cy, 4f, trackH);
            float thumbH = Math.max(30f, trackH * viewH / (contentH + 20f));
            float t = scroll / max;
            shapes.setColor(gold);
            shapes.rect(trackX, cy + (trackH - thumbH) * (1f - t), 4f, thumbH);
        }
        // Label chips
        float y = cy + ch + scroll;
        for (int i = 0; i < lines.size(); i++) {
            CreditsContent.Line l = lines.get(i);
            if (l.kind == CreditsContent.Kind.LABEL && y <= cy + ch && y - heights[i] >= cy) {
                Color c = HistoricalContextCard.labelColor(l.label);
                shapes.setColor(c.r, c.g, c.b, 0.85f);
                shapes.rect(cx - 16f, y - heights[i] + LINE_GAP, 5f, heights[i] - LINE_GAP);
            }
            y -= heights[i];
        }
        shapes.end();

        shapes.begin(ShapeType.Line);
        shapes.setColor(goldBorder);
        shapes.rect(px, py, pw, ph);
        shapes.rect(backX, backY, backW, backH);
        shapes.end();

        batch.begin();
        fonts.titleFont.setColor(gold);
        fonts.titleFont.draw(batch, "CREDITS & SOURCES", px + 44f, py + ph - 26f);
        fonts.smallFont.setColor(goldMuted);
        String sub = "Dramatised work  •  fictional protagonist" + (fromLabel.isEmpty() ? "" : "  •  " + fromLabel);
        layout.setText(fonts.smallFont, sub);
        fonts.smallFont.draw(batch, sub, px + pw - 44f - layout.width, py + ph - 38f);
        fonts.smallFont.setColor(dimCol);
        fonts.smallFont.draw(batch, "W/S or Arrows / Mouse Wheel: Scroll   •   PgUp/PgDn, Home/End   •   ESC: Back",
            px + 44f, py + 40f);
        fonts.headerFont.setColor(gold);
        layout.setText(fonts.headerFont, "BACK");
        fonts.headerFont.draw(batch, "BACK", backX + (backW - layout.width) / 2f, backY + backH / 2f + 9f);
        batch.flush();

        // Clip the scrolling text to the content box
        int bbW = Gdx.graphics.getBackBufferWidth(), bbH = Gdx.graphics.getBackBufferHeight();
        float sx = bbW / w, sy = bbH / h;
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(Math.round((px + 8f) * sx), Math.round(cy * sy), Math.round((pw - 40f) * sx), Math.round(ch * sy));
        y = cy + ch + scroll;
        for (int i = 0; i < lines.size(); i++) {
            CreditsContent.Line l = lines.get(i);
            float top = y;
            y -= heights[i];
            if (l.kind == CreditsContent.Kind.GAP || top - heights[i] > cy + ch || top < cy) continue;
            BitmapFont f = fontFor(l, fonts);
            f.setColor(colorFor(l));
            f.draw(batch, l.text, cx, top, cw, Align.left, true);
        }
        batch.flush();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        batch.end();

        tickTest();
    }

    private Color colorFor(CreditsContent.Line l) {
        switch (l.kind) {
            case TITLE:
            case HEADER: return gold;
            case LABEL: return HistoricalContextCard.labelColor(l.label);
            case TEXT: return textCol;
            case LINK: return linkCol;
            default: return dimCol;
        }
    }

    // ── Screenshot harness ───────────────────────────────────────────────

    private int testStep = 0;
    private float testTime = 0f;

    /** {@code -Dbd.spark36.testCreditsScreenshot}: open credits from the main menu and capture. */
    public static boolean screenshotTestRequested() {
        return System.getProperty("bd.spark36.testCreditsScreenshot") != null;
    }

    /** {@code -Dbd.spark36.testEpilogueCredits}: Level 6 completes its last objective and captures. */
    public static boolean epilogueTestRequested() {
        return System.getProperty("bd.spark36.testEpilogueCredits") != null;
    }

    /**
     * Captures t4/credits_top, then t4/credits_sources (scrolled to the source list) and
     * t4/credits_sources_more (further down), then exits when {@code bd.spark36.autoExit=true}.
     * Runs only under one of the two test switches; called after the frame is drawn.
     */
    private void tickTest() {
        if (!screenshotTestRequested() && !epilogueTestRequested()) return;
        testTime += Math.min(0.1f, Gdx.graphics.getDeltaTime());
        String prefix = epilogueTestRequested() ? "t4/epilogue_" : "t4/";
        if (testStep == 0 && testTime >= 1.0f) {
            bd.spark36.capture.CaptureController.captureHarness(prefix + "credits_top");
            scrollTo(offsetOf(CreditsContent.SOURCES_HEADER));
            testStep = 1;
        } else if (testStep == 1 && testTime >= 1.6f) {
            bd.spark36.capture.CaptureController.captureHarness(prefix + "credits_sources");
            scrollTo(offsetOf(SourceCatalog.DISPUTED));
            testStep = 2;
        } else if (testStep == 2 && testTime >= 2.2f) {
            bd.spark36.capture.CaptureController.captureHarness(prefix + "credits_sources_more");
            testStep = 3;
            if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) Gdx.app.exit();
        }
    }

    /** Mouse-wheel collector, installed in front of the game's input processor while open. */
    private static final class Wheel extends InputAdapter {
        private InputProcessor previous, installed;
        private float pending;

        void install() {
            if (installed != null || Gdx.input == null) return;
            previous = Gdx.input.getInputProcessor();
            installed = previous == null ? this : new InputMultiplexer(this, previous);
            Gdx.input.setInputProcessor(installed);
            pending = 0f;
        }

        void uninstall() {
            if (installed == null || Gdx.input == null) return;
            if (Gdx.input.getInputProcessor() == installed) Gdx.input.setInputProcessor(previous);
            installed = null;
            previous = null;
            pending = 0f;
        }

        float consume() {
            float v = pending;
            pending = 0f;
            return v;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            pending += amountY;
            return false;
        }
    }
}
