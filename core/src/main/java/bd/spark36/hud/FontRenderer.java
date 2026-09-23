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

    private FreeTypeFontGenerator boldGen;
    private FreeTypeFontGenerator regularGen;

    public FontRenderer() {
        initFonts();
    }

    private void initFonts() {
        FileHandle boldFile = Gdx.files.internal("fonts/LiberationSans-Bold.ttf");
        FileHandle regFile = Gdx.files.internal("fonts/LiberationSans-Regular.ttf");

        if (boldFile.exists() && regFile.exists()) {
            try {
                boldGen = new FreeTypeFontGenerator(boldFile);
                regularGen = new FreeTypeFontGenerator(regFile);

                FreeTypeFontParameter p = new FreeTypeFontParameter();

                // Title: large bold for modal titles and headers
                p.size = 24;
                p.color = Color.WHITE;
                p.borderWidth = 1.2f;
                p.borderColor = new Color(0f, 0f, 0f, 0.7f);
                p.shadowOffsetX = 1;
                p.shadowOffsetY = 1;
                p.shadowColor = new Color(0f, 0f, 0f, 0.5f);
                titleFont = boldGen.generateFont(p);

                // Header: medium bold for quest cards and titles
                p = new FreeTypeFontParameter();
                p.size = 17;
                p.color = Color.WHITE;
                p.borderWidth = 1.0f;
                p.borderColor = new Color(0f, 0f, 0f, 0.6f);
                headerFont = boldGen.generateFont(p);

                // Body: regular text for narrative stories
                p = new FreeTypeFontParameter();
                p.size = 14;
                p.color = new Color(0.92f, 0.92f, 0.95f, 1f);
                p.shadowOffsetX = 1;
                p.shadowOffsetY = 1;
                p.shadowColor = new Color(0f, 0f, 0f, 0.4f);
                bodyFont = regularGen.generateFont(p);

                // Small: UI badges, distance counters, sub-labels
                p = new FreeTypeFontParameter();
                p.size = 11;
                p.color = new Color(0.85f, 0.85f, 0.88f, 0.9f);
                smallFont = regularGen.generateFont(p);

                // Prompt: interactive key prompts e.g. "[E] Inspect Memorial"
                p = new FreeTypeFontParameter();
                p.size = 15;
                p.color = Color.WHITE;
                p.borderWidth = 1.0f;
                p.borderColor = new Color(0f, 0f, 0f, 0.8f);
                promptFont = boldGen.generateFont(p);

                // Key font: for keycaps icons [W], [SHIFT]
                p = new FreeTypeFontParameter();
                p.size = 12;
                p.color = new Color(1f, 0.88f, 0.45f, 1f); // Warm gold
                keyFont = boldGen.generateFont(p);

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
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (headerFont != null) headerFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (promptFont != null) promptFont.dispose();
        if (keyFont != null) keyFont.dispose();

        if (boldGen != null) boldGen.dispose();
        if (regularGen != null) regularGen.dispose();
    }
}
