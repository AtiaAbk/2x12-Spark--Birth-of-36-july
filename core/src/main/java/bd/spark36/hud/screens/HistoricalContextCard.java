package bd.spark36.hud.screens;

import bd.spark36.history.SourceCatalog;
import bd.spark36.hud.FontRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;

import java.util.List;

/**
 * Historical context card drawn directly above a memorial / archive modal: one row per mapped
 * register entry with its evidence label and a one-line source, taken from
 * {@code historical_sources.json} through {@link SourceCatalog}. Draws nothing for entries without
 * a mapping. Uses the caller's projection (the HUD's virtual space).
 */
public final class HistoricalContextCard {

    private static final float ROW_H = 26f;
    private static final float HEAD_H = 26f;
    private static final float CHIP_W = 272f;
    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private HistoricalContextCard() {}

    /** Colour of an evidence label, shared with the credits screen. */
    public static Color labelColor(String label) {
        if (SourceCatalog.VERIFIED.equals(label)) return new Color(0.45f, 0.85f, 0.50f, 1f);
        if (SourceCatalog.CORROBORATED.equals(label)) return new Color(0.95f, 0.78f, 0.35f, 1f);
        if (SourceCatalog.DISPUTED.equals(label)) return new Color(0.95f, 0.48f, 0.38f, 1f);
        if (SourceCatalog.FICTIONAL.equals(label)) return new Color(0.70f, 0.60f, 0.95f, 1f);
        if (SourceCatalog.RECONSTRUCTION.equals(label)) return new Color(0.55f, 0.75f, 0.90f, 1f);
        return new Color(0.75f, 0.75f, 0.75f, 1f);
    }

    /** Height the card needs for this memorial (0 when there is nothing to show). */
    public static float height(int memorialId) {
        int n = SourceCatalog.get().sourcesForMemorial(memorialId).size();
        return n == 0 ? 0f : HEAD_H + n * ROW_H + 10f;
    }

    /**
     * Draws the card with its bottom edge at {@code y}, spanning {@code x .. x + w}. Expects no
     * batch or shape pass to be active, and leaves none active.
     */
    public static void render(ShapeRenderer shapes, SpriteBatch batch, FontRenderer fonts,
                              int memorialId, float x, float y, float w) {
        List<SourceCatalog.Source> rows = SourceCatalog.get().sourcesForMemorial(memorialId);
        if (rows.isEmpty()) return;
        float h = HEAD_H + rows.size() * ROW_H + 10f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeType.Filled);
        shapes.setColor(0.02f, 0.04f, 0.06f, 0.88f);
        shapes.rect(x, y, w, h);
        for (int i = 0; i < rows.size(); i++) {
            Color c = labelColor(rows.get(i).label);
            float ry = y + h - HEAD_H - (i + 1) * ROW_H;
            shapes.setColor(c.r, c.g, c.b, 0.18f);
            shapes.rect(x + 24f, ry + 3f, CHIP_W, ROW_H - 6f);
            shapes.setColor(c);
            shapes.rect(x + 24f, ry + 3f, 3f, ROW_H - 6f);
        }
        shapes.end();
        shapes.begin(ShapeType.Line);
        shapes.setColor(0.92f, 0.76f, 0.32f, 0.55f);
        shapes.rect(x, y, w, h);
        shapes.end();

        batch.begin();
        fonts.smallFont.setColor(1.0f, 0.85f, 0.40f, 0.95f);
        fonts.smallFont.draw(batch, "HISTORICAL CONTEXT", x + 24f, y + h - 9f);
        fonts.smallFont.setColor(0.62f, 0.60f, 0.56f, 0.90f);
        String hint = "Full source list: Main Menu > Credits";
        LAYOUT.setText(fonts.smallFont, hint);
        fonts.smallFont.draw(batch, hint, x + w - 24f - LAYOUT.width, y + h - 9f);
        for (int i = 0; i < rows.size(); i++) {
            SourceCatalog.Source s = rows.get(i);
            float ry = y + h - HEAD_H - (i + 1) * ROW_H;
            float ty = ry + ROW_H / 2f + 6f;
            fonts.smallFont.setColor(labelColor(s.label));
            fonts.smallFont.draw(batch, s.label, x + 34f, ty);
            fonts.smallFont.setColor(0.92f, 0.90f, 0.86f, 1f);
            float sx = x + 24f + CHIP_W + 14f;
            fonts.smallFont.draw(batch, s.sourceLine(), sx, ty, 0, s.sourceLine().length(),
                x + w - 24f - sx, Align.left, false, "...");
        }
        batch.end();
    }
}
