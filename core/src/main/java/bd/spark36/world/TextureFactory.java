package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Procedurally generates high-resolution textures for Dhaka University campus,
 * Curzon Hall architecture, brick pavements, vegetation, and student attire.
 */
public class TextureFactory implements Disposable {

    private final Array<Texture> textures = new Array<>();

    public final Texture brickPavement;
    public final Texture curzonBrick;
    public final Texture lawnGrass;
    public final Texture treeBark;
    public final Texture foliage;
    public final Texture backpackFabric;
    public final Texture studentJacket;
    public final Texture movementBanner;

    public TextureFactory() {
        brickPavement = createBrickPavement();
        curzonBrick = createCurzonBrick();
        lawnGrass = createLawnGrass();
        treeBark = createTreeBark();
        foliage = createFoliage();
        backpackFabric = createBackpackFabric();
        studentJacket = createStudentJacket();
        movementBanner = createMovementBanner();

        textures.addAll(brickPavement, curzonBrick, lawnGrass, treeBark,
                        foliage, backpackFabric, studentJacket, movementBanner);
    }

    private Texture createBrickPavement() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Mortar base color (weathered cement)
        Color mortar = new Color(0.38f, 0.36f, 0.35f, 1f);
        pix.setColor(mortar);
        pix.fill();

        // Brick grid: 16 rows, 8 bricks per row (interlocked/stretcher bond)
        int rows = 16;
        int rowHeight = size / rows;
        int brickWidth = size / 8;

        for (int r = 0; r < rows; r++) {
            int y = r * rowHeight;
            int xOffset = (r % 2 == 0) ? 0 : brickWidth / 2;

            for (int b = -1; b <= 9; b++) {
                int x = b * brickWidth + xOffset;

                // Subtle brick color variation (warm terracotta red to brownish amber)
                float var = (MathUtils.sin(r * 3.7f + b * 5.3f) + 1f) * 0.5f;
                float red = 0.68f + var * 0.12f;
                float green = 0.28f + var * 0.08f;
                float blue = 0.20f + var * 0.06f;

                pix.setColor(red, green, blue, 1f);
                pix.fillRectangle(x + 2, y + 2, brickWidth - 4, rowHeight - 4);

                // Highlight upper/left bevel and shadow bottom/right
                pix.setColor(red + 0.10f, green + 0.08f, blue + 0.05f, 0.6f);
                pix.drawLine(x + 2, y + 2, x + brickWidth - 3, y + 2);
                pix.drawLine(x + 2, y + 2, x + 2, y + rowHeight - 3);

                pix.setColor(0.18f, 0.10f, 0.08f, 0.5f);
                pix.drawLine(x + 2, y + rowHeight - 3, x + brickWidth - 3, y + rowHeight - 3);
                pix.drawLine(x + brickWidth - 3, y + 2, x + brickWidth - 3, y + rowHeight - 3);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createCurzonBrick() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Mortar base
        pix.setColor(0.42f, 0.38f, 0.35f, 1f);
        pix.fill();

        // Authentic terracotta red brick courses
        int rows = 32;
        int rowH = size / rows;
        int brickW = size / 16;

        for (int r = 0; r < rows; r++) {
            // Every 8th row is a white ornamental stone cornice line (Indo-Saracenic banding)
            if (r % 8 == 0) {
                pix.setColor(0.92f, 0.90f, 0.86f, 1f);
                pix.fillRectangle(0, r * rowH, size, rowH);
                continue;
            }

            int y = r * rowH;
            int xOffset = (r % 2 == 0) ? 0 : brickW / 2;

            for (int b = -1; b <= 17; b++) {
                int x = b * brickW + xOffset;
                float v = (MathUtils.sin(r * 4.1f + b * 7.9f) + 1f) * 0.5f;

                float rCol = 0.64f + v * 0.14f;
                float gCol = 0.18f + v * 0.08f;
                float bCol = 0.14f + v * 0.05f;

                pix.setColor(rCol, gCol, bCol, 1f);
                pix.fillRectangle(x + 1, y + 1, brickW - 2, rowH - 2);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createLawnGrass() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Base emerald green
        pix.setColor(0.22f, 0.44f, 0.18f, 1f);
        pix.fill();

        // Multi-frequency organic turf variation
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float n = (MathUtils.sin(x * 0.22f) * MathUtils.cos(y * 0.25f)
                         + MathUtils.sin((x + y) * 0.45f) * 0.5f);
                float r = MathUtils.clamp(0.20f + n * 0.06f, 0.14f, 0.28f);
                float g = MathUtils.clamp(0.44f + n * 0.12f, 0.32f, 0.56f);
                float b = MathUtils.clamp(0.18f + n * 0.05f, 0.12f, 0.24f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createTreeBark() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        pix.setColor(0.28f, 0.18f, 0.12f, 1f);
        pix.fill();

        // Vertical bark grooves
        for (int x = 0; x < size; x++) {
            float groove = MathUtils.sin(x * 0.35f);
            for (int y = 0; y < size; y++) {
                float n = groove * (0.8f + 0.2f * MathUtils.sin(y * 0.1f));
                float r = MathUtils.clamp(0.28f + n * 0.08f, 0.16f, 0.38f);
                float g = MathUtils.clamp(0.18f + n * 0.06f, 0.10f, 0.26f);
                float b = MathUtils.clamp(0.12f + n * 0.04f, 0.06f, 0.18f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createFoliage() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        pix.setColor(0.16f, 0.40f, 0.15f, 1f);
        pix.fill();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float leaf = MathUtils.sin(x * 0.4f) * MathUtils.sin(y * 0.4f)
                           + MathUtils.cos((x - y) * 0.3f) * 0.5f;
                float r = MathUtils.clamp(0.16f + leaf * 0.08f, 0.10f, 0.26f);
                float g = MathUtils.clamp(0.42f + leaf * 0.14f, 0.28f, 0.58f);
                float b = MathUtils.clamp(0.15f + leaf * 0.06f, 0.08f, 0.22f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createBackpackFabric() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Canvas brown/khaki backpack matching mockup
        Color canvas = new Color(0.42f, 0.35f, 0.26f, 1f);
        pix.setColor(canvas);
        pix.fill();

        // Cross-hatch fabric weave
        for (int x = 0; x < size; x += 4) {
            pix.setColor(0.38f, 0.31f, 0.22f, 0.3f);
            pix.drawLine(x, 0, x, size);
        }
        for (int y = 0; y < size; y += 4) {
            pix.setColor(0.46f, 0.39f, 0.29f, 0.3f);
            pix.drawLine(0, y, size, y);
        }

        // Pocket seam stitching lines
        pix.setColor(0.24f, 0.18f, 0.12f, 1f);
        pix.fillRectangle(20, 110, 216, 6); // Pocket zipper seam
        pix.fillRectangle(40, 180, 176, 4);

        // Circular enamel badges: "DU" and "36 July" pins on backpack!
        pix.setColor(0.92f, 0.82f, 0.25f, 1f); // Gold pin
        pix.fillCircle(75, 75, 18);
        pix.setColor(0.12f, 0.14f, 0.35f, 1f);
        pix.fillCircle(75, 75, 14);

        pix.setColor(0.85f, 0.15f, 0.18f, 1f); // July red pin
        pix.fillCircle(125, 82, 16);
        pix.setColor(1f, 1f, 1f, 1f);
        pix.fillCircle(125, 82, 12);

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createStudentJacket() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Earthy khaki/tan jacket color matching mockup character
        pix.setColor(0.62f, 0.56f, 0.46f, 1f);
        pix.fill();

        // Fabric fold shading
        for (int y = 0; y < size; y++) {
            float fold = MathUtils.sin(y * 0.12f) * 0.08f;
            pix.setColor(0.62f + fold, 0.56f + fold, 0.46f + fold, 1f);
            pix.drawLine(0, y, size, y);
        }

        // Collar & seam lines
        pix.setColor(0.42f, 0.36f, 0.28f, 1f);
        pix.fillRectangle(124, 0, 8, size);

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createMovementBanner() {
        int w = 256;
        int h = 512;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);

        // Cream banner canvas
        pix.setColor(0.94f, 0.92f, 0.88f, 1f);
        pix.fill();

        // Red top and bottom header bands
        pix.setColor(0.85f, 0.12f, 0.15f, 1f);
        pix.fillRectangle(0, 0, w, 40);
        pix.fillRectangle(0, h - 30, w, 30);

        // Gold border lines
        pix.setColor(0.92f, 0.78f, 0.28f, 1f);
        pix.drawRectangle(8, 8, w - 16, h - 16);
        pix.drawRectangle(10, 10, w - 20, h - 20);

        // Black typography representation (protest slogans)
        pix.setColor(0.12f, 0.12f, 0.15f, 1f);
        pix.fillRectangle(30, 80, 196, 24);  // "36 JULY 2024"
        pix.fillRectangle(40, 130, 176, 16); // "DHAKA UNIVERSITY"
        pix.fillRectangle(30, 180, 196, 14); // "MERIT OVER PRIVILEGE"

        // Red circular movement emblem in center
        pix.setColor(0.85f, 0.12f, 0.15f, 1f);
        pix.fillCircle(128, 280, 48);
        pix.setColor(0.95f, 0.92f, 0.86f, 1f);
        pix.fillCircle(128, 280, 42);
        pix.setColor(0.85f, 0.12f, 0.15f, 1f);
        pix.fillCircle(128, 280, 20);

        // Bottom text bar
        pix.setColor(0.12f, 0.12f, 0.15f, 1f);
        pix.fillRectangle(40, 380, 176, 14);
        pix.fillRectangle(50, 410, 156, 12);

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    @Override
    public void dispose() {
        for (Texture t : textures) {
            t.dispose();
        }
        textures.clear();
    }
}
