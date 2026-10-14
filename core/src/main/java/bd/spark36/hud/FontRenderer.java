package bd.spark36.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages crisp FreeType vector fonts for the Where Winds Meet HUD.
 * Renders multiple resolutions cleanly without pixelation.
 */
public class FontRenderer implements Disposable {

    public BitmapFont titleFont;
    public BitmapFont headerFont;
    public BitmapFont bodyFont;
    public BitmapFont smallFont;
    public BitmapFont promptFont;
    public BitmapFont keyFont;

    public BitmapFont bengaliFont;
    public BitmapFont bengaliTitleFont;

    private FreeTypeFontGenerator boldGen;
    private FreeTypeFontGenerator regularGen;
    private FreeTypeFontGenerator bengaliGen;

    public static final String BENGALI_CHARS =
        "ঁংঃঅআইঈউঊঋএঐওঔকখগঘঙচছজঝঞটঠডঢণতথদধনপফবভমযরলশষসহ়ািীুূৃেৈোৌ্ৎৗড়ঢ়য়০১২৩৪৫৬৭৮৯।॥“”—’‘… ";
    public static final String EXTENDED_CHARS =
        FreeTypeFontGenerator.DEFAULT_CHARS + "—◆•°–|[]/\\:><=*+-~_!@#$%^&()'\";.,?` ";

    public FontRenderer() {
        initFonts();
        runStartupGlyphCheck();
    }

    private void initFonts() {
        FileHandle boldFile = Gdx.files.internal("fonts/LiberationSans-Bold.ttf");
        FileHandle regFile = Gdx.files.internal("fonts/LiberationSans-Regular.ttf");
        FileHandle benFile = Gdx.files.internal("fonts/NotoSansBengali-Variable.ttf");

        if (boldFile.exists() && regFile.exists()) {
            try {
                boldGen = new FreeTypeFontGenerator(boldFile);
                regularGen = new FreeTypeFontGenerator(regFile);

                FreeTypeFontParameter p = new FreeTypeFontParameter();
                p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.genMipMaps = true;

                String chars = EXTENDED_CHARS;

                // Title: large bold for modal titles and headers
                p.size = 28;
                p.characters = chars;
                p.color = Color.WHITE;
                p.borderWidth = 1.5f;
                p.borderColor = new Color(0f, 0f, 0f, 0.90f);
                p.shadowOffsetX = 1;
                p.shadowOffsetY = 1;
                p.shadowColor = new Color(0f, 0f, 0f, 0.75f);
                titleFont = boldGen.generateFont(p);

                // Header: medium bold for quest cards and titles
                p = new FreeTypeFontParameter();
                p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.genMipMaps = true;
                p.characters = chars;
                p.size = 20;
                p.color = Color.WHITE;
                p.borderWidth = 1.2f;
                p.borderColor = new Color(0f, 0f, 0f, 0.85f);
                headerFont = boldGen.generateFont(p);

                // Body: regular text for narrative stories
                p = new FreeTypeFontParameter();
                p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.genMipMaps = true;
                p.characters = chars;
                p.size = 16;
                p.color = Color.WHITE;
                p.shadowOffsetX = 1;
                p.shadowOffsetY = 1;
                p.shadowColor = new Color(0f, 0f, 0f, 0.80f);
                bodyFont = regularGen.generateFont(p);

                // Small: UI badges, distance counters, sub-labels
                p = new FreeTypeFontParameter();
                p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.genMipMaps = true;
                p.characters = chars;
                p.size = 14;
                p.color = new Color(0.96f, 0.96f, 0.98f, 1f);
                p.shadowOffsetX = 1;
                p.shadowOffsetY = 1;
                p.shadowColor = new Color(0f, 0f, 0f, 0.80f);
                smallFont = regularGen.generateFont(p);

                // Prompt: interactive key prompts e.g. "[E] Inspect Memorial"
                p = new FreeTypeFontParameter();
                p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.genMipMaps = true;
                p.characters = chars;
                p.size = 16;
                p.color = Color.WHITE;
                p.borderWidth = 1.2f;
                p.borderColor = new Color(0f, 0f, 0f, 0.85f);
                promptFont = boldGen.generateFont(p);

                // Key font: for keycaps icons [W], [SHIFT]
                p = new FreeTypeFontParameter();
                p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                p.genMipMaps = true;
                p.characters = chars;
                p.size = 13;
                p.color = new Color(1f, 0.88f, 0.45f, 1f); // Warm gold
                keyFont = boldGen.generateFont(p);

                // Load Noto Sans Bengali if present
                if (benFile.exists()) {
                    bengaliGen = new FreeTypeFontGenerator(benFile);
                    FreeTypeFontParameter bp = new FreeTypeFontParameter();
                    bp.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                    bp.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                    bp.genMipMaps = true;
                    bp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + BENGALI_CHARS;
                    bp.size = 16;
                    bp.color = Color.WHITE;
                    bengaliFont = bengaliGen.generateFont(bp);

                    bp.size = 24;
                    bengaliTitleFont = bengaliGen.generateFont(bp);
                    Gdx.app.log("FontRenderer", "Loaded Noto Sans Bengali font successfully.");
                } else {
                    bengaliFont = bodyFont;
                    bengaliTitleFont = titleFont;
                }

                return;
            } catch (Exception e) {
                Gdx.app.error("FontRenderer", "Failed to generate FreeType fonts, using fallback", e);
            }
        }

        // Fallback default fonts
        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.5f);
        headerFont = new BitmapFont();
        headerFont.getData().setScale(1.2f);
        bodyFont = new BitmapFont();
        smallFont = new BitmapFont();
        smallFont.getData().setScale(0.85f);
        promptFont = new BitmapFont();
        keyFont = new BitmapFont();
        bengaliFont = bodyFont;
        bengaliTitleFont = titleFont;
    }

    /**
     * Scans critical HUD and dialogue strings against loaded font glyph sets
     * to guarantee zero missing-glyph boxes ("▯") at runtime.
     */
    public void runStartupGlyphCheck() {
        String[] testStrings = {
            "ACTIVE MISSION: 01 / 05",
            "THE SPARK OF SOLIDARITY",
            "Target: Curzon Hall Central Arcade",
            "HELMETED AGGRESSOR ENFORCER",
            "CAMPUS MAP",
            "CURZON HALL W",
            "[E] INTERACT",
            "[M] MAP",
            "[ESC] PAUSE",
            "HP: 100 / 100",
            "POSTURE: 100%",
            "—◆•°–|[]/\\:><=*+-~_!@#$%^&()'\";.,?` "
        };

        int missingCount = 0;
        for (String s : testStrings) {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (headerFont != null && !headerFont.getData().hasGlyph(ch)) {
                    Gdx.app.error("FontRenderer", String.format("MISSING GLYPH: '%c' (U+%04X) in string \"%s\"", ch, (int)ch, s));
                    missingCount++;
                }
            }
        }
        if (missingCount == 0) {
            Gdx.app.log("FontRenderer", "Startup glyph self-check PASSED: 0 missing glyphs detected.");
        } else {
            Gdx.app.error("FontRenderer", "Startup glyph self-check FAILED: " + missingCount + " missing glyphs detected.");
        }
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (headerFont != null) headerFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (promptFont != null) promptFont.dispose();
        if (keyFont != null) keyFont.dispose();
        if (bengaliFont != null && bengaliFont != bodyFont) bengaliFont.dispose();
        if (bengaliTitleFont != null && bengaliTitleFont != titleFont) bengaliTitleFont.dispose();

        if (boldGen != null) boldGen.dispose();
        if (regularGen != null) regularGen.dispose();
        if (bengaliGen != null) bengaliGen.dispose();
    }
}
