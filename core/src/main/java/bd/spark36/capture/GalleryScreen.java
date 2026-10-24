package bd.spark36.capture;

import bd.spark36.hud.FontRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Minimal in-game gallery: newest-first thumbnail grid, keyboard and mouse navigation, a large
 * viewer with zoom and pan, and an "Open folder" action. Thumbnails live in a session-only cache
 * bounded by count and bytes; every texture is disposed on eviction, on close (viewer) and on
 * {@link #dispose()}. Search and date grouping are intentionally out of scope.
 */
final class GalleryScreen {

    static final int THUMB_MAX_WIDTH = 320;
    static final int CACHE_MAX_ENTRIES = 60;
    static final long CACHE_MAX_BYTES = 32L * 1024L * 1024L;

    private final FontRenderer fonts;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color tmpColor = new Color();

    private final BoundedLruCache<String, Texture> thumbs = new BoundedLruCache<>(
        CACHE_MAX_ENTRIES, CACHE_MAX_BYTES,
        new BoundedLruCache.Weigher<Texture>() {
            @Override public long weigh(Texture t) { return (long) t.getWidth() * t.getHeight() * 4L; }
        },
        new BoundedLruCache.EvictionListener<Texture>() {
            @Override public void evicted(Texture t) { t.dispose(); }
        });
    private final Set<String> unreadable = new HashSet<>();

    private boolean open;
    private File directory;
    private List<File> files = new ArrayList<>();
    private int selected;
    private int firstRow;
    private String notice;
    private float noticeTimer;

    // Viewer
    private boolean viewerOpen;
    private Texture viewerTexture;
    private int viewerIndex = -1;
    private boolean viewerFailed;
    private float zoom = 1f, panX, panY;

    // Layout of the last frame (pixels), used for mouse hit tests
    private float folderBtnX, folderBtnY, folderBtnW, folderBtnH;
    private float gridLeft, gridTop, cellW, cellH, gap;
    private int cols = 4, visibleRows = 3;

    GalleryScreen(FontRenderer fonts) {
        this.fonts = fonts;
    }

    boolean isOpen() { return open; }
    boolean isViewerOpen() { return viewerOpen; }
    List<File> getFiles() { return files; }

    void open(File dir) {
        directory = dir;
        files = GalleryIndex.list(dir);
        selected = 0;
        firstRow = 0;
        closeViewer();
        open = true;
        Gdx.input.setCursorCatched(false);
    }

    void close() {
        closeViewer();
        open = false;
    }

    void openViewer() {
        if (files.isEmpty()) return;
        viewerOpen = true;
        zoom = 1f;
        panX = panY = 0f;
        loadViewer(selected);
    }

    private void closeViewer() {
        viewerOpen = false;
        if (viewerTexture != null) {
            viewerTexture.dispose();
            viewerTexture = null;
        }
        viewerIndex = -1;
    }

    private void loadViewer(int index) {
        if (viewerIndex == index && (viewerTexture != null || viewerFailed)) return;
        if (viewerTexture != null) viewerTexture.dispose();
        viewerTexture = null;
        viewerFailed = false;
        viewerIndex = index;
        try {
            viewerTexture = new Texture(new FileHandle(files.get(index)));
            viewerTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            viewerFailed = true;
        }
    }

    /** @return true when the gallery asked to close this frame. */
    boolean render(float delta, float scrollY) {
        if (batch == null) {
            batch = new SpriteBatch();
            shapes = new ShapeRenderer();
        }
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        float s = Math.max(0.6f, h / 900f);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        if (noticeTimer > 0f) noticeTimer -= delta;

        float mx = Gdx.input.getX() * (float) w / Math.max(1, Gdx.graphics.getWidth());
        float my = h - Gdx.input.getY() * (float) h / Math.max(1, Gdx.graphics.getHeight());
        boolean click = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        Gdx.gl.glClearColor(0.035f, 0.045f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        boolean closeRequested;
        if (viewerOpen) {
            closeRequested = false;
            if (handleViewerInput(delta, scrollY, w, h)) closeViewer();
            if (viewerOpen) drawViewer(w, h, s);
        } else {
            layoutGrid(w, h, s);
            closeRequested = handleGridInput(scrollY, mx, my, click);
            if (open && !viewerOpen) {
                loadOneMissingThumbnail();
                drawGrid(w, h, s, mx, my);
            }
        }
        return closeRequested;
    }

    // ── Grid ─────────────────────────────────────────────────────────

    private void layoutGrid(float w, float h, float s) {
        float margin = 48f * s;
        gap = 18f * s;
        cols = Math.max(2, (int) ((w - 2f * margin + gap) / (300f * s + gap)));
        cellW = (w - 2f * margin - (cols - 1) * gap) / cols;
        cellH = cellW * 9f / 16f + 26f * s;
        gridLeft = margin;
        gridTop = h - 130f * s;
        float gridBottom = 64f * s;
        visibleRows = Math.max(1, (int) ((gridTop - gridBottom + gap) / (cellH + gap)));
        folderBtnW = 230f * s;
        folderBtnH = 40f * s;
        folderBtnX = w - margin - folderBtnW;
        folderBtnY = h - 40f * s - folderBtnH;
    }

    private boolean handleGridInput(float scrollY, float mx, float my, boolean click) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.F11) || Gdx.input.isKeyJustPressed(Input.Keys.G)
            || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            return true;
        }
        boolean overFolder = mx >= folderBtnX && mx <= folderBtnX + folderBtnW && my >= folderBtnY && my <= folderBtnY + folderBtnH;
        if (Gdx.input.isKeyJustPressed(Input.Keys.O) || (click && overFolder)) {
            openFolder();
            return false;
        }
        if (files.isEmpty()) return false;
        int n = files.size();
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) selected = Math.min(n - 1, selected + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) selected = Math.max(0, selected - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) selected = Math.min(n - 1, selected + cols);
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) selected = Math.max(0, selected - cols);
        if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) selected = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.END)) selected = n - 1;
        int totalRows = (n + cols - 1) / cols;
        if (scrollY != 0f) {
            firstRow = MathUtils.clamp(firstRow + (int) Math.signum(scrollY), 0, Math.max(0, totalRows - visibleRows));
        }
        int row = selected / cols;
        if (scrollY == 0f) {
            if (row < firstRow) firstRow = row;
            if (row >= firstRow + visibleRows) firstRow = row - visibleRows + 1;
        }

        int hovered = cellAt(mx, my);
        if (hovered >= 0 && (Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0 || click)) selected = hovered;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || (click && hovered >= 0)) {
            openViewer();
        }
        return false;
    }

    private int cellAt(float mx, float my) {
        for (int r = 0; r < visibleRows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = (firstRow + r) * cols + c;
                if (idx >= files.size()) return -1;
                float x = gridLeft + c * (cellW + gap);
                float y = gridTop - (r + 1) * cellH - r * gap;
                if (mx >= x && mx <= x + cellW && my >= y && my <= y + cellH) return idx;
            }
        }
        return -1;
    }

    private void openFolder() {
        String err = FolderOpener.open(directory);
        showNotice(err == null ? "Opened " + directory.getPath() : err);
    }

    private void showNotice(String text) {
        notice = text;
        noticeTimer = 3f;
        Gdx.app.log("Gallery", text);
    }

    private static String key(File f) {
        return f.getAbsolutePath() + "|" + f.lastModified();
    }

    /** Decodes at most one thumbnail per frame, visible cells first, to keep the gallery responsive. */
    private void loadOneMissingThumbnail() {
        int start = firstRow * cols, end = Math.min(files.size(), start + visibleRows * cols);
        for (int i = start; i < end; i++) {
            File f = files.get(i);
            String k = key(f);
            if (thumbs.containsKey(k) || unreadable.contains(k)) continue;
            Texture t = makeThumbnail(f);
            if (t == null) unreadable.add(k); else thumbs.put(k, t);
            return;
        }
    }

    private static Texture makeThumbnail(File f) {
        Pixmap full = null, small = null;
        try {
            full = new Pixmap(new FileHandle(f));
            int tw = Math.min(THUMB_MAX_WIDTH, full.getWidth());
            int th = Math.max(1, Math.round(full.getHeight() * (tw / (float) full.getWidth())));
            small = new Pixmap(tw, th, Pixmap.Format.RGBA8888);
            small.setBlending(Pixmap.Blending.None);
            small.setFilter(Pixmap.Filter.BiLinear);
            small.drawPixmap(full, 0, 0, full.getWidth(), full.getHeight(), 0, 0, tw, th);
            Texture t = new Texture(small);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        } catch (Exception e) {
            return null;
        } finally {
            if (full != null) full.dispose();
            if (small != null) small.dispose();
        }
    }

    private void drawGrid(float w, float h, float s, float mx, float my) {
        boolean overFolder = mx >= folderBtnX && mx <= folderBtnX + folderBtnW && my >= folderBtnY && my <= folderBtnY + folderBtnH;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.10f, 0.13f, 1f);
        shapes.rect(0, h - 110f * s, w, 110f * s);
        shapes.setColor(0.92f, 0.76f, 0.32f, 0.9f);
        shapes.rect(0, h - 112f * s, w, 2f * s);
        shapes.setColor(overFolder ? 0.20f : 0.12f, overFolder ? 0.18f : 0.11f, 0.08f, 1f);
        shapes.rect(folderBtnX, folderBtnY, folderBtnW, folderBtnH);
        for (int r = 0; r < visibleRows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = (firstRow + r) * cols + c;
                if (idx >= files.size()) break;
                float x = gridLeft + c * (cellW + gap);
                float y = gridTop - (r + 1) * cellH - r * gap;
                if (idx == selected) {
                    shapes.setColor(0.92f, 0.76f, 0.32f, 1f);
                    shapes.rect(x - 3f * s, y - 3f * s, cellW + 6f * s, cellH + 6f * s);
                }
                shapes.setColor(0.10f, 0.12f, 0.15f, 1f);
                shapes.rect(x, y, cellW, cellH);
            }
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.92f, 0.76f, 0.32f, 0.9f);
        shapes.rect(folderBtnX, folderBtnY, folderBtnW, folderBtnH);
        shapes.end();

        batch.begin();
        text(fonts.titleFont, "GALLERY", 48f * s, h - 34f * s, s, 1f, 0.85f, 0.40f);
        String sub = files.size() + (files.size() == 1 ? " screenshot" : " screenshots") + "  ·  " + directory.getPath();
        text(fonts.smallFont, sub, 48f * s, h - 78f * s, s, 0.78f, 0.80f, 0.84f);
        textCentered(fonts.promptFont, "OPEN FOLDER  [O]", folderBtnX + folderBtnW / 2f, folderBtnY + folderBtnH / 2f, s, 1f, 0.9f, 0.6f);

        float labelH = 26f * s;
        for (int r = 0; r < visibleRows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = (firstRow + r) * cols + c;
                if (idx >= files.size()) break;
                File f = files.get(idx);
                float x = gridLeft + c * (cellW + gap);
                float y = gridTop - (r + 1) * cellH - r * gap;
                float imgH = cellH - labelH;
                Texture t = thumbs.get(key(f));
                if (t != null) {
                    float sc = Math.min(cellW / t.getWidth(), imgH / t.getHeight());
                    float dw = t.getWidth() * sc, dh = t.getHeight() * sc;
                    batch.setColor(Color.WHITE);
                    batch.draw(t, x + (cellW - dw) / 2f, y + labelH + (imgH - dh) / 2f, dw, dh);
                } else {
                    boolean bad = unreadable.contains(key(f));
                    textCentered(fonts.smallFont, bad ? "unreadable" : "loading...", x + cellW / 2f, y + labelH + imgH / 2f, s, 0.6f, 0.6f, 0.6f);
                }
                text(fonts.smallFont, ellipsize(fonts.smallFont, f.getName(), cellW - 12f * s, s), x + 6f * s, y + labelH - 7f * s, s,
                    idx == selected ? 1f : 0.80f, idx == selected ? 0.9f : 0.82f, idx == selected ? 0.6f : 0.86f);
            }
        }
        if (files.isEmpty()) {
            textCentered(fonts.headerFont, "No screenshots yet", w / 2f, h / 2f + 20f * s, s, 1f, 0.9f, 0.6f);
            textCentered(fonts.bodyFont, "Press F12 in game, or F10 for photo mode", w / 2f, h / 2f - 16f * s, s, 0.8f, 0.8f, 0.8f);
        }
        int totalRows = (files.size() + cols - 1) / Math.max(1, cols);
        String footer = "Arrows / mouse  select   ·   Enter  view   ·   O  open folder   ·   Esc  close";
        if (totalRows > visibleRows) footer += "   ·   rows " + (firstRow + 1) + "-" + Math.min(totalRows, firstRow + visibleRows) + " of " + totalRows;
        textCentered(fonts.smallFont, footer, w / 2f, 32f * s, s, 0.75f, 0.77f, 0.80f);
        if (noticeTimer > 0f && notice != null) {
            textCentered(fonts.bodyFont, notice, w / 2f, 62f * s, s, 1f, 0.9f, 0.6f);
        }
        batch.end();
    }

    // ── Viewer ───────────────────────────────────────────────────────

    /** @return true to go back to the grid. */
    private boolean handleViewerInput(float delta, float scrollY, float w, float h) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) openFolder();
        int n = files.size();
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && selected < n - 1) { selected++; zoom = 1f; panX = panY = 0f; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && selected > 0) { selected--; zoom = 1f; panX = panY = 0f; }
        loadViewer(selected);

        float z = -scrollY * 0.15f;
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS) || Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_ADD)) z += 1.5f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_SUBTRACT)) z -= 1.5f * delta;
        zoom = MathUtils.clamp(zoom * (1f + z), 1f, 6f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0)) { zoom = 1f; panX = panY = 0f; }

        float pan = 900f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) panX += pan;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) panX -= pan;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) panY -= pan;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) panY += pan;
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            panX += Gdx.input.getDeltaX() * w / Math.max(1, Gdx.graphics.getWidth());
            panY -= Gdx.input.getDeltaY() * h / Math.max(1, Gdx.graphics.getHeight());
        }
        return false;
    }

    private void drawViewer(float w, float h, float s) {
        float top = 70f * s, bottom = 50f * s;
        float availW = w - 40f * s, availH = h - top - bottom;
        batch.begin();
        if (viewerTexture != null) {
            float fit = Math.min(availW / viewerTexture.getWidth(), availH / viewerTexture.getHeight());
            float dw = viewerTexture.getWidth() * fit * zoom, dh = viewerTexture.getHeight() * fit * zoom;
            float maxPanX = Math.max(0f, (dw - availW) / 2f), maxPanY = Math.max(0f, (dh - availH) / 2f);
            panX = MathUtils.clamp(panX, -maxPanX, maxPanX);
            panY = MathUtils.clamp(panY, -maxPanY, maxPanY);
            float cx = w / 2f + panX, cy = bottom + availH / 2f + panY;
            batch.setColor(Color.WHITE);
            batch.draw(viewerTexture, cx - dw / 2f, cy - dh / 2f, dw, dh);
        } else {
            textCentered(fonts.headerFont, viewerFailed ? "This image cannot be opened" : "Loading...", w / 2f, h / 2f, s, 1f, 0.6f, 0.5f);
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.05f, 0.06f, 0.08f, 0.92f);
        shapes.rect(0, h - top, w, top);
        shapes.rect(0, 0, w, bottom);
        shapes.end();

        batch.begin();
        File f = files.get(selected);
        text(fonts.headerFont, f.getName(), 24f * s, h - 24f * s, s, 1f, 0.85f, 0.40f);
        String info = (selected + 1) + " / " + files.size() + "   ·   zoom " + Math.round(zoom * 100f) + "%";
        if (viewerTexture != null) info += "   ·   " + viewerTexture.getWidth() + "×" + viewerTexture.getHeight();
        text(fonts.smallFont, info, 24f * s, h - 52f * s, s, 0.78f, 0.80f, 0.84f);
        textCentered(fonts.smallFont,
            "Left / Right  previous / next   ·   wheel or + / -  zoom   ·   drag or WASD  pan   ·   0  reset   ·   O  open folder   ·   Esc  back",
            w / 2f, bottom / 2f, s, 0.75f, 0.77f, 0.80f);
        if (noticeTimer > 0f && notice != null) {
            textCentered(fonts.bodyFont, notice, w / 2f, bottom + 24f * s, s, 1f, 0.9f, 0.6f);
        }
        batch.end();
    }

    // ── Text helpers (fonts are shared with the HUD, so scale and colour are restored) ──

    private void text(BitmapFont font, String str, float x, float y, float s, float r, float g, float b) {
        float ox = font.getData().scaleX, oy = font.getData().scaleY;
        tmpColor.set(font.getColor());
        font.getData().setScale(ox * s, oy * s);
        font.setColor(r, g, b, 1f);
        font.draw(batch, str, x, y);
        font.setColor(tmpColor);
        font.getData().setScale(ox, oy);
    }

    private void textCentered(BitmapFont font, String str, float cx, float cy, float s, float r, float g, float b) {
        float ox = font.getData().scaleX, oy = font.getData().scaleY;
        font.getData().setScale(ox * s, oy * s);
        layout.setText(font, str);
        float lw = layout.width, lh = layout.height;
        font.getData().setScale(ox, oy);
        text(font, str, cx - lw / 2f, cy + lh / 2f, s, r, g, b);
    }

    private String ellipsize(BitmapFont font, String str, float maxW, float s) {
        float ox = font.getData().scaleX, oy = font.getData().scaleY;
        font.getData().setScale(ox * s, oy * s);
        try {
            layout.setText(font, str);
            if (layout.width <= maxW) return str;
            String cut = str;
            while (cut.length() > 4) {
                cut = cut.substring(0, cut.length() - 1);
                layout.setText(font, cut + "...");
                if (layout.width <= maxW) return cut + "...";
            }
            return cut;
        } finally {
            font.getData().setScale(ox, oy);
        }
    }

    void dispose() {
        closeViewer();
        thumbs.clear();
        unreadable.clear();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        batch = null;
        shapes = null;
    }
}
