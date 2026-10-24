package bd.spark36.world.landmarks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Renders the street signboards (Bengali name + optional English line) into one texture atlas,
 * using the bundled Noto Sans Bengali font. One material covers every sign in the street dressing.
 *
 * <p>libGDX's FreeType path draws glyphs one by one without OpenType shaping, so the shop names
 * avoid conjuncts and reph, and {@link #toVisualOrder(String)} moves the pre-base vowel signs
 * (ি, ে, ৈ and the split vowels ো, ৌ) in front of their consonant, as the shaper would.</p>
 *
 * <p>All names are generic and fictional (no real brands or businesses).</p>
 */
public final class BengaliSignAtlas implements Disposable {

    /** One atlas row per sign. Row 0 is at the bottom of the framebuffer texture. */
    public enum Sign {
        // logical Bengali text, English line, background sRGB, text sRGB
        TEA_STALL("ভাই ভাই চা ঘর", "চা  কলা  পান", 0xB32821, 0xF7D23E),
        TELECOM("সাথী টেলিকম", "SATHI TELECOM  MOBILE RECHARGE", 0x1F4E9A, 0xFFFFFF),
        PHARMACY("সেবা ঔষধ ঘর", "SEBA PHARMACY", 0x1E7A45, 0xFFFFFF),
        GENERAL_STORE("মদিনা", "GENERAL STORE", 0xF2C230, 0xB01E1E),
        HOTEL("আপন হোটেল", "HOTEL & RESTAURANT", 0xF4F1E6, 0xC0261F),
        BOOKS("বই ঘর", "BOOKS & STATIONERY", 0x2F5E3A, 0xFFF4D0),
        BUS_ROUTE("মিরপুর - মতিঝিল", "", 0x141414, 0xFFA21F),
        PHOTOCOPY("ফটোকপি", "PHOTOCOPY  PRINT  LAMINATING", 0xE7792B, 0x1A1A1A);

        public final String bengali;
        public final String english;
        public final int background;
        public final int ink;

        Sign(String bengali, String english, int background, int ink) {
            this.bengali = bengali;
            this.english = english;
            this.background = background;
            this.ink = ink;
        }
    }

    public static final int SIZE = 1024;
    public static final int ROW_H = SIZE / 8;

    private FrameBuffer fbo;
    private Texture texture;

    /** Builds the atlas; must run on the GL thread. Falls back to plain-colour boards without the font. */
    public BengaliSignAtlas() {
        build();
    }

    public Texture getTexture() {
        return texture;
    }

    /** Lower texture v of a sign's row (its bottom edge). */
    public static float vBottom(Sign s) {
        return (s.ordinal() * ROW_H + 4f) / SIZE;
    }

    /** Upper texture v of a sign's row (its top edge). */
    public static float vTop(Sign s) {
        return ((s.ordinal() + 1) * ROW_H - 4f) / SIZE;
    }

    /**
     * Converts logical-order Bengali to the visual glyph order an OpenType shaper would produce,
     * for text without conjuncts: pre-base vowel signs move before their consonant (with an
     * optional nukta), and the two-part vowels split around it.
     */
    public static String toVisualOrder(String logical) {
        StringBuilder out = new StringBuilder(logical.length() + 4);
        for (int i = 0; i < logical.length(); i++) {
            char c = logical.charAt(i);
            char next = (i + 1 < logical.length()) ? logical.charAt(i + 1) : 0;
            char next2 = (i + 2 < logical.length()) ? logical.charAt(i + 2) : 0;
            boolean consonant = isConsonant(c);
            boolean hasNukta = consonant && next == '়';
            char vowel = hasNukta ? next2 : next;
            if (consonant && (vowel == 'ি' || vowel == 'ে' || vowel == 'ৈ')) {
                out.append(vowel).append(c);
                if (hasNukta) out.append('়');
                i += hasNukta ? 2 : 1;
            } else if (consonant && (vowel == 'ো' || vowel == 'ৌ')) {
                out.append('ে').append(c);
                if (hasNukta) out.append('়');
                out.append(vowel == 'ো' ? 'া' : 'ৗ');
                i += hasNukta ? 2 : 1;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean isConsonant(char c) {
        return (c >= 'ক' && c <= 'হ') || c == 'ৎ' || (c >= 'ড়' && c <= 'য়');
    }

    private void build() {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, SIZE, SIZE, false);
        FileHandle benFile = Gdx.files.internal("fonts/NotoSansBengali-Variable.ttf");
        FileHandle latFile = Gdx.files.internal("fonts/LiberationSans-Bold.ttf");

        BitmapFont bengali = null;
        BitmapFont latin = null;
        FreeTypeFontGenerator benGen = null;
        FreeTypeFontGenerator latGen = null;
        try {
            StringBuilder ben = new StringBuilder(" -");
            StringBuilder lat = new StringBuilder(" ");
            for (Sign s : Sign.values()) {
                ben.append(s.bengali);
                for (char ch : s.english.toCharArray()) {
                    if (ch < 0x0980) lat.append(ch); else ben.append(ch);
                }
            }
            if (benFile.exists()) {
                benGen = new FreeTypeFontGenerator(benFile);
                FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
                p.size = 66;
                p.characters = unique(ben.toString());
                p.minFilter = Texture.TextureFilter.Linear;
                p.magFilter = Texture.TextureFilter.Linear;
                bengali = benGen.generateFont(p);
            }
            if (latFile.exists()) {
                latGen = new FreeTypeFontGenerator(latFile);
                FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
                p.size = 28;
                p.characters = unique(lat.toString());
                p.minFilter = Texture.TextureFilter.Linear;
                p.magFilter = Texture.TextureFilter.Linear;
                latin = latGen.generateFont(p);
            }
        } catch (Throwable t) {
            Gdx.app.error("BengaliSignAtlas", "Font generation failed, signs will be plain: " + t.getMessage());
        }

        ShapeRenderer shapes = new ShapeRenderer();
        SpriteBatch batch = new SpriteBatch();
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, SIZE, SIZE);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, SIZE, SIZE);
        GlyphLayout layout = new GlyphLayout();
        Color tmp = new Color();

        fbo.begin();
        Gdx.gl.glViewport(0, 0, SIZE, SIZE);
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Sign s : Sign.values()) {
            float y0 = s.ordinal() * ROW_H;
            Color.rgb888ToColor(tmp, s.background);
            tmp.a = 1f; // rgb888ToColor leaves alpha untouched
            // Faded flex print: a slightly darker border band and a lighter upper wash
            shapes.setColor(tmp.r * 0.72f, tmp.g * 0.72f, tmp.b * 0.72f, 1f);
            shapes.rect(0, y0, SIZE, ROW_H);
            shapes.setColor(tmp);
            shapes.rect(10, y0 + 10, SIZE - 20, ROW_H - 20);
            shapes.setColor(Math.min(1f, tmp.r * 1.06f + 0.03f), Math.min(1f, tmp.g * 1.06f + 0.03f),
                            Math.min(1f, tmp.b * 1.06f + 0.03f), 1f);
            shapes.rect(10, y0 + ROW_H * 0.55f, SIZE - 20, ROW_H * 0.45f - 10);
        }
        shapes.end();

        batch.begin();
        for (Sign s : Sign.values()) {
            float y0 = s.ordinal() * ROW_H;
            Color.rgb888ToColor(tmp, s.ink);
            tmp.a = 1f;
            boolean twoLines = !s.english.isEmpty();
            if (bengali != null) {
                drawCentered(batch, bengali, layout, toVisualOrder(s.bengali), tmp,
                    y0 + (twoLines ? ROW_H * 0.70f : ROW_H * 0.56f), SIZE - 60);
            }
            if (twoLines) {
                boolean latinLine = s.english.charAt(0) < 0x0980;
                BitmapFont f = latinLine ? latin : bengali;
                if (f != null) {
                    String text = latinLine ? s.english : toVisualOrder(s.english);
                    float scale = latinLine ? 1f : 0.42f;
                    f.getData().setScale(scale);
                    drawCentered(batch, f, layout, text, tmp, y0 + ROW_H * 0.27f, SIZE - 80);
                    f.getData().setScale(1f);
                }
            }
        }
        batch.end();
        String dump = System.getProperty("bd.spark36.dumpSignAtlas");
        if (dump != null) {
            // Developer aid: write the atlas as rendered (bottom row first, as in GL)
            Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, SIZE, SIZE);
            com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.absolute(dump), pm, -1, true);
            pm.dispose();
        }
        fbo.end();

        texture = fbo.getColorBufferTexture();
        texture.bind();
        Gdx.gl.glGenerateMipmap(GL20.GL_TEXTURE_2D);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);

        batch.dispose();
        shapes.dispose();
        if (bengali != null) bengali.dispose();
        if (latin != null) latin.dispose();
        if (benGen != null) benGen.dispose();
        if (latGen != null) latGen.dispose();
    }

    private static void drawCentered(SpriteBatch batch, BitmapFont font, GlyphLayout layout, String text,
                                     Color color, float centerY, float maxWidth) {
        float oldScaleX = font.getData().scaleX;
        font.setColor(color); // GlyphLayout captures the font colour at setText time
        layout.setText(font, text);
        if (layout.width > maxWidth) {
            font.getData().setScale(oldScaleX * maxWidth / layout.width);
            layout.setText(font, text);
        }
        font.draw(batch, layout, (SIZE - layout.width) * 0.5f, centerY + layout.height * 0.5f);
        font.getData().setScale(oldScaleX);
    }

    private static String unique(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (out.indexOf(String.valueOf(c)) < 0) out.append(c);
        }
        return out.toString();
    }

    @Override
    public void dispose() {
        if (fbo != null) {
            fbo.dispose();
            fbo = null;
            texture = null;
        }
    }
}
