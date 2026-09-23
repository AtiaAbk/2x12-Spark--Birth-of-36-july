package bd.historicalgame.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;


public final class FontManager implements Disposable {

    private static final String REGULAR_PATH = "fonts/LiberationSans-Regular.ttf";
    private static final String BOLD_PATH = "fonts/LiberationSans-Bold.ttf";


    private static final String EXTRA_CHARS = "\u2191\u2193\u2192\u2190\u25C6\u2022";

    private final FreeTypeFontGenerator regularGenerator;
    private final FreeTypeFontGenerator boldGenerator;

    private final IntMap<BitmapFont> regularCache = new IntMap<>();
    private final IntMap<BitmapFont> boldCache = new IntMap<>();

    public FontManager() {
        regularGenerator = new FreeTypeFontGenerator(
            Gdx.files.internal(REGULAR_PATH)
        );

        boldGenerator = new FreeTypeFontGenerator(
            Gdx.files.internal(BOLD_PATH)
        );
    }

    /**
     * Returns a crisp regular-weight font rendered at exactly
     * {@code pixelSize}, generating and caching it on first use.
     */
    public BitmapFont get(int pixelSize) {
        return get(pixelSize, false);
    }

    /**
     * Returns a crisp bold-weight font rendered at exactly
     * {@code pixelSize}, generating and caching it on first use.
     */
    public BitmapFont getBold(int pixelSize) {
        return get(pixelSize, true);
    }

    public BitmapFont get(int pixelSize, boolean bold) {

        IntMap<BitmapFont> cache =
            bold ? boldCache : regularCache;

        BitmapFont cached = cache.get(pixelSize);

        if (cached != null) {
            return cached;
        }

        FreeTypeFontParameter parameter =
            new FreeTypeFontParameter();

        parameter.size = pixelSize;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.genMipMaps = false;
        parameter.characters =
            FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS;

        // Faint dark outline keeps small HUD text legible when it
        // sits directly over the busy 3D scene instead of a panel.
        if (pixelSize <= 20) {
            parameter.borderWidth = 1f;
            parameter.borderColor = new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.55f);
            parameter.borderStraight = false;
        }

        FreeTypeFontGenerator generator =
            bold ? boldGenerator : regularGenerator;

        BitmapFont font = generator.generateFont(parameter);
        font.setUseIntegerPositions(false);

        cache.put(pixelSize, font);

        return font;
    }

    @Override
    public void dispose() {

        for (BitmapFont font : regularCache.values()) {
            font.dispose();
        }

        for (BitmapFont font : boldCache.values()) {
            font.dispose();
        }

        regularCache.clear();
        boldCache.clear();

        regularGenerator.dispose();
        boldGenerator.dispose();
    }
}
