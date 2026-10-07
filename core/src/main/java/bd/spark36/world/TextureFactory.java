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
    public final Texture krishnachuraBlossom;
    public final Texture leafClump;
    public final Texture blossomClump;
    public final Texture whiteBlossomClump;
    public final Texture leafAtlas;
    public final Texture bambooAtlas;
    public final Texture whiteBlossomAtlas;
    public final Texture kashfulAtlas;
    public final Texture colonialWindow;
    public final Texture teakDoorTexture;
    public final Texture dirtPath;
    public final Texture lightBeam;
    public final Texture rockSurface;
    public final Texture bambooCulm;
    public final Texture bambooFoliage;
    public final Texture bushFoliage;
    public final Texture weatheredStone;
    public final Texture curzonWater;
    public final Texture waterRipple;
    public final Texture backpackFabric;
    public final Texture studentJacket;
    public final Texture movementBanner;
    public final Texture bdFlag;
    public final Texture aparajeyoStone;
    public final Texture asphaltRoad;
    public final Texture modernistConcrete;
    public final Texture canteenTinRoof;
    public final Texture mapVisualTarget;

    public TextureFactory() {
        brickPavement = createBrickPavement();
        curzonBrick = createCurzonBrick();
        lawnGrass = createLawnGrass();
        treeBark = createTreeBark();
        foliage = createFoliage();
        krishnachuraBlossom = createKrishnachuraBlossom();
        leafClump = createLeafClump(false);
        blossomClump = createLeafClump(true);
        whiteBlossomClump = createWhiteBlossomClump();
        leafAtlas = createLeafAtlas();
        bambooAtlas = createBambooAtlas();
        whiteBlossomAtlas = createWhiteBlossomAtlas();
        kashfulAtlas = createKashfulAtlas();
        colonialWindow = createColonialWindow();
        teakDoorTexture = createTeakDoorTexture();
        dirtPath = createDirtPath();
        lightBeam = createLightBeam();
        rockSurface = createRockSurface();
        bambooCulm = createBambooCulm();
        bambooFoliage = createBambooFoliage();
        bushFoliage = createBushFoliage();
        weatheredStone = createWeatheredStone();
        curzonWater = createCurzonWater();
        waterRipple = createWaterRipple();
        backpackFabric = createBackpackFabric();
        studentJacket = createStudentJacket();
        movementBanner = createMovementBanner();
        bdFlag = createBdFlag();
        aparajeyoStone = createAparajeyoStone();
        asphaltRoad = createAsphaltRoad();
        modernistConcrete = createModernistConcrete();
        canteenTinRoof = createCanteenTinRoof();
        mapVisualTarget = createMapVisualTarget();

        textures.addAll(brickPavement, curzonBrick, lawnGrass, treeBark,
                        foliage, krishnachuraBlossom, leafClump, blossomClump, whiteBlossomClump,
                        leafAtlas, bambooAtlas, whiteBlossomAtlas, kashfulAtlas, colonialWindow, teakDoorTexture,
                        dirtPath, lightBeam, rockSurface, bambooCulm, bambooFoliage,
                        bushFoliage, weatheredStone, curzonWater, waterRipple,
                        backpackFabric, studentJacket, movementBanner, bdFlag, aparajeyoStone,
                        asphaltRoad, modernistConcrete, canteenTinRoof, mapVisualTarget);
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
                // Weathered clay brick: muted terracotta with per-brick tone shifts
                float var = (MathUtils.sin(r * 3.7f + b * 5.3f) + 1f) * 0.5f;
                float wear = (MathUtils.sin(r * 12.9f + b * 7.1f) + 1f) * 0.5f;
                float red = 0.52f + var * 0.12f - wear * 0.05f;
                float green = 0.29f + var * 0.07f + wear * 0.02f;
                float blue = 0.23f + var * 0.05f + wear * 0.02f;

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

        // Aged lime-sand mortar base
        pix.setColor(0.35f, 0.31f, 0.28f, 1f);
        pix.fill();

        // Authentic Dhaka University terracotta red brick courses (stretcher bond)
        int rows = 32;
        int rowH = size / rows;
        int brickW = size / 16;

        for (int r = 0; r < rows; r++) {
            int y = r * rowH;
            int xOffset = (r % 2 == 0) ? 0 : brickW / 2;

            for (int b = -1; b <= 17; b++) {
                int x = b * brickW + xOffset;
                float v = (MathUtils.sin(r * 3.7f + b * 5.3f) + 1f) * 0.5f;
                float v2 = (MathUtils.cos(r * 7.1f - b * 4.1f) + 1f) * 0.5f;

                // Rich, deep Mughal terracotta burnt-red variation
                float rCol = MathUtils.clamp(0.62f + v * 0.16f - v2 * 0.08f, 0.48f, 0.82f);
                float gCol = MathUtils.clamp(0.18f + v * 0.05f - v2 * 0.03f, 0.11f, 0.26f);
                float bCol = MathUtils.clamp(0.12f + v * 0.04f - v2 * 0.03f, 0.08f, 0.18f);

                pix.setColor(rCol, gCol, bCol, 1f);
                pix.fillRectangle(x + 1, y + 1, brickW - 2, rowH - 2);

                // Subtle baked-clay organic stipple
                for (int s = 0; s < 5; s++) {
                    int sx = x + 2 + MathUtils.random(Math.max(1, brickW - 6));
                    int sy = y + 2 + MathUtils.random(Math.max(1, rowH - 6));
                    float stipple = MathUtils.random(-0.05f, 0.05f);
                    pix.setColor(MathUtils.clamp(rCol + stipple, 0.42f, 0.85f),
                                 MathUtils.clamp(gCol + stipple * 0.5f, 0.09f, 0.30f),
                                 MathUtils.clamp(bCol + stipple * 0.3f, 0.06f, 0.22f), 1f);
                    pix.drawPixel(sx, sy);
                }
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

        // Base vibrant emerald green
        pix.setColor(0.16f, 0.46f, 0.14f, 1f);
        pix.fill();

        // Multi-frequency organic turf variation matching reference image
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float n = (MathUtils.sin(x * 0.22f) * MathUtils.cos(y * 0.25f)
                         + MathUtils.sin((x + y) * 0.45f) * 0.5f);
                float speckle = MathUtils.random(-0.04f, 0.04f);
                float r = MathUtils.clamp(0.20f + n * 0.06f + speckle, 0.12f, 0.32f);
                float g = MathUtils.clamp(0.48f + n * 0.12f + speckle, 0.34f, 0.64f);
                float b = MathUtils.clamp(0.14f + n * 0.04f + speckle * 0.4f, 0.08f, 0.22f);

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

    /**
     * Opaque, tileable canopy texture for the leaf-clump meshes: a dark shaded base overlaid with
     * hundreds of small leaves in varied greens, and (for the flame tree) scarlet blossoms.
     */
    private Texture createLeafClump(boolean blossoms) {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0.09f, 0.20f, 0.06f, 1f);
        pix.fill();

        for (int i = 0; i < 1100; i++) {
            int x = MathUtils.random(size - 1);
            int y = MathUtils.random(size - 1);
            float t = MathUtils.random();
            int r = MathUtils.random(3, 6);

            if (blossoms && MathUtils.random() < 0.34f) {
                // Saturated high-contrast fiery scarlet-crimson flowers
                pix.setColor(0.94f + 0.05f * t, 0.11f + 0.08f * t, 0.06f, 1f);
            } else {
                pix.setColor(0.12f + 0.10f * t, 0.32f + 0.18f * t, 0.07f + 0.06f * t, 1f);
            }
            for (int ox = -size; ox <= size; ox += size) {
                for (int oy = -size; oy <= size; oy += size) {
                    pix.fillCircle(x + ox, y + oy, r);
                }
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    /**
     * Soft, dense canopy texture for the White Blossom Tree matching user reference image:
     * Dark branch-shaded base layered with thousands of soft ivory and pure snow-white petals.
     */
    private Texture createWhiteBlossomClump() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0.10f, 0.16f, 0.09f, 1f);
        pix.fill();

        for (int i = 0; i < 1300; i++) {
            int x = MathUtils.random(size - 1);
            int y = MathUtils.random(size - 1);
            float t = MathUtils.random();
            int r = MathUtils.random(3, 7);

            if (MathUtils.random() < 0.68f) {
                // Silvery white and ivory floral blossoms
                float shade = 0.94f + 0.05f * t;
                pix.setColor(shade, shade, shade * 0.94f, 1f);
            } else {
                // Understory green leaves
                pix.setColor(0.12f + 0.10f * t, 0.32f + 0.15f * t, 0.09f + 0.05f * t, 1f);
            }

            for (int ox = -size; ox <= size; ox += size) {
                for (int oy = -size; oy <= size; oy += size) {
                    pix.fillCircle(x + ox, y + oy, r);
                }
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    /**
     * 2x2 atlas of alpha-cut foliage cards (each cell 256px): [0] one broad leaf, [1] a fan of
     * three leaves, [2] a feathery compound leaf (flame tree), [3] a cluster of scarlet blossoms.
     * Cells are addressed by TreeGeometry via UV quarters; the row order is top-down.
     */
    private Texture createLeafAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill();
        final float up = -MathUtils.HALF_PI; // pixmap y points down, so "up" is -90 degrees

        // Cell 0: one broad ovate leaf
        drawLeaf(pix, 0, 0, 128, 244, 226, 74, up,
            0.09f, 0.27f, 0.07f, 0.34f, 0.58f, 0.16f);

        // Cell 1: fan of three leaves of different lengths and greens
        drawLeaf(pix, cell, 0, 128, 246, 168, 46, up - 0.62f, 0.10f, 0.30f, 0.08f, 0.30f, 0.54f, 0.14f);
        drawLeaf(pix, cell, 0, 128, 246, 168, 46, up + 0.62f, 0.12f, 0.32f, 0.09f, 0.36f, 0.58f, 0.16f);
        drawLeaf(pix, cell, 0, 128, 246, 214, 54, up, 0.08f, 0.26f, 0.06f, 0.28f, 0.52f, 0.13f);

        // Cell 2: feathery compound leaf: a central rachis with pairs of small leaflets
        int ox = 0, oy = cell;
        for (int i = 0; i < 15; i++) {
            float t = 0.10f + i * 0.058f;
            float ry = 246f - t * 232f;
            float rx = 128f + MathUtils.sin(t * 2.2f) * 10f;
            float len = 78f * (1f - 0.45f * t);
            float hw = 13f * (1f - 0.25f * t);
            drawLeaf(pix, ox, oy, rx, ry, len, hw, up - 1.05f, 0.14f, 0.36f, 0.08f, 0.40f, 0.62f, 0.14f);
            drawLeaf(pix, ox, oy, rx, ry, len, hw, up + 1.05f, 0.14f, 0.36f, 0.08f, 0.40f, 0.62f, 0.14f);
        }
        drawLeaf(pix, ox, oy, 128, 250, 236, 4, up, 0.20f, 0.30f, 0.09f, 0.26f, 0.38f, 0.10f);

        // Cell 3: blazing scarlet Krishnachura blossom clusters over feathery leaflets
        ox = cell;
        oy = cell;
        drawLeaf(pix, ox, oy, 128, 250, 160, 42, up - 0.55f, 0.10f, 0.34f, 0.08f, 0.32f, 0.58f, 0.14f);
        drawLeaf(pix, ox, oy, 128, 250, 160, 42, up + 0.55f, 0.10f, 0.34f, 0.08f, 0.32f, 0.58f, 0.14f);
        drawLeaf(pix, ox, oy, 128, 250, 190, 36, up, 0.12f, 0.38f, 0.09f, 0.36f, 0.62f, 0.16f);

        float[][] flowers = {
            {92, 96}, {168, 86}, {124, 140}, {70, 168}, {180, 158}, {132, 62},
            {65, 80}, {195, 110}, {145, 185}, {90, 205}, {165, 215}, {110, 105}
        };
        for (float[] f : flowers) {
            for (int p = 0; p < 5; p++) {
                float ang = p * MathUtils.PI2 / 5f + f[0] * 0.15f;
                // Saturated high-contrast scarlet-red petals
                drawLeaf(pix, ox, oy, f[0], f[1], 44f, 17f, ang, 0.90f, 0.08f, 0.05f, 0.99f, 0.28f, 0.08f);
            }
            // Glowing golden central stamens
            pix.setColor(1.0f, 0.90f, 0.20f, 1f);
            pix.fillCircle(ox + (int) f[0], oy + (int) f[1], 5);
            pix.setColor(1.0f, 0.98f, 0.60f, 1f);
            pix.fillCircle(ox + (int) f[0], oy + (int) f[1], 2);
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * 2x2 atlas of hanging bamboo leaf sprays (each cell 256px, hung from the top-centre): long
     * narrow lanceolate leaves fanning downward in a few densities and greens.
     */
    private Texture createBambooAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill();
        final float down = MathUtils.HALF_PI; // pixmap y points down

        // Cell 0: full spray, nine leaves fanning from the pivot
        float[] fan = {-1.15f, -0.85f, -0.55f, -0.25f, 0f, 0.25f, 0.55f, 0.85f, 1.15f};
        float[] fanLen = {150f, 175f, 200f, 215f, 225f, 215f, 200f, 175f, 150f};
        for (int i = 0; i < fan.length; i++) {
            float shade = (i % 2 == 0) ? 0f : 0.03f;
            drawLeaf(pix, 0, 0, 128, 10, fanLen[i], 11f, down + fan[i],
                0.20f + shade, 0.38f + shade, 0.09f, 0.42f + shade, 0.64f, 0.18f);
        }

        // Cell 1: airy spray of seven long thin leaves
        for (int i = 0; i < 7; i++) {
            float a = -0.9f + i * 0.3f;
            drawLeaf(pix, cell, 0, 128, 10, 205f + 20f * (1f - Math.abs(a)), 8f, down + a,
                0.16f, 0.34f, 0.08f, 0.36f, 0.58f, 0.15f);
        }

        // Cell 2: short bright sprig of five leaves
        for (int i = 0; i < 5; i++) {
            float a = -0.6f + i * 0.3f;
            drawLeaf(pix, 0, cell, 128, 10, 120f + 25f * (1f - Math.abs(a)), 10f, down + a,
                0.28f, 0.48f, 0.11f, 0.52f, 0.72f, 0.22f);
        }

        // Cell 3: spray with a few dry, yellowing leaves mixed in
        for (int i = 0; i < 8; i++) {
            float a = -1.0f + i * 0.285f;
            boolean dry = i % 3 == 1;
            if (dry) {
                drawLeaf(pix, cell, cell, 128, 10, 185f, 10f, down + a, 0.42f, 0.36f, 0.12f, 0.62f, 0.55f, 0.20f);
            } else {
                drawLeaf(pix, cell, cell, 128, 10, 200f, 10f, down + a, 0.20f, 0.38f, 0.09f, 0.42f, 0.64f, 0.18f);
            }
        }

        // No mipmaps: sprays are mostly empty space, and averaging that space into smaller mip
        // levels pushes alpha over the cutout threshold, turning distant sprays into solid blocks.
        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * 2x2 atlas of white blossom cards (each cell 256px) matching user reference image:
     * Dense clusters of five-petaled snow-white & ivory flowers with golden stamens and delicate green calyxes.
     */
    private Texture createWhiteBlossomAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill();

        // Cell 0: Dense bouquet of white petals with golden stamens
        drawWhiteBlossomCluster(pix, 0, 0, 16);
        // Cell 1: Fan of white blossoms with small green leaf calyxes
        drawWhiteBlossomFan(pix, cell, 0);
        // Cell 2: Spray of pure white cherry/magnolia florets
        drawWhiteBlossomSpray(pix, 0, cell);
        // Cell 3: Delicate white floral rosettes with golden centers
        drawWhiteBlossomCluster(pix, cell, cell, 20);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    private void drawWhiteBlossomCluster(Pixmap pix, int ox, int oy, int flowerCount) {
        final float up = -MathUtils.HALF_PI;
        // Background leafy calyxes
        drawLeaf(pix, ox, oy, 128, 250, 140, 36, up - 0.45f, 0.12f, 0.35f, 0.08f, 0.30f, 0.55f, 0.14f);
        drawLeaf(pix, ox, oy, 128, 250, 140, 36, up + 0.45f, 0.12f, 0.35f, 0.08f, 0.30f, 0.55f, 0.14f);

        for (int i = 0; i < flowerCount; i++) {
            float fx = 45f + MathUtils.random(166f);
            float fy = 45f + MathUtils.random(175f);
            float rad = MathUtils.random(18f, 32f);

            // 5 snow-white petals with subtle ivory base
            for (int p = 0; p < 5; p++) {
                float ang = p * MathUtils.PI2 / 5f + MathUtils.random(-0.15f, 0.15f);
                float px = fx + MathUtils.cos(ang) * (rad * 0.45f);
                float py = fy + MathUtils.sin(ang) * (rad * 0.45f);
                float pr = rad * 0.55f;

                pix.setColor(0.98f, 0.98f, 0.96f, 1f);
                pix.fillCircle(ox + (int)px, oy + (int)py, (int)pr);
                pix.setColor(1.0f, 1.0f, 1.0f, 1f);
                pix.fillCircle(ox + (int)px, oy + (int)py, Math.max(1, (int)(pr * 0.6f)));
            }

            // Warm golden amber pistils / central stamens
            pix.setColor(1.0f, 0.85f, 0.25f, 1f);
            pix.fillCircle(ox + (int)fx, oy + (int)fy, 4);
            pix.setColor(1.0f, 0.96f, 0.65f, 1f);
            pix.fillCircle(ox + (int)fx, oy + (int)fy, 2);
        }
    }

    private void drawWhiteBlossomFan(Pixmap pix, int ox, int oy) {
        final float up = -MathUtils.HALF_PI;
        drawLeaf(pix, ox, oy, 128, 246, 175, 42, up - 0.65f, 0.12f, 0.36f, 0.09f, 0.34f, 0.58f, 0.15f);
        drawLeaf(pix, ox, oy, 128, 246, 175, 42, up + 0.65f, 0.12f, 0.36f, 0.09f, 0.34f, 0.58f, 0.15f);
        drawLeaf(pix, ox, oy, 128, 246, 210, 48, up, 0.10f, 0.32f, 0.08f, 0.30f, 0.54f, 0.13f);

        for (int i = 0; i < 12; i++) {
            float fx = 55f + MathUtils.random(146f);
            float fy = 50f + MathUtils.random(155f);
            float rad = MathUtils.random(16f, 28f);

            for (int p = 0; p < 5; p++) {
                float ang = p * MathUtils.PI2 / 5f;
                float px = fx + MathUtils.cos(ang) * (rad * 0.42f);
                float py = fy + MathUtils.sin(ang) * (rad * 0.42f);
                pix.setColor(0.97f, 0.97f, 0.95f, 1f);
                pix.fillCircle(ox + (int)px, oy + (int)py, (int)(rad * 0.5f));
            }
            pix.setColor(1.0f, 0.88f, 0.22f, 1f);
            pix.fillCircle(ox + (int)fx, oy + (int)fy, 4);
        }
    }

    private void drawWhiteBlossomSpray(Pixmap pix, int ox, int oy) {
        final float up = -MathUtils.HALF_PI;
        drawLeaf(pix, ox, oy, 128, 250, 230, 4, up, 0.28f, 0.18f, 0.10f, 0.35f, 0.22f, 0.12f);

        for (int i = 0; i < 18; i++) {
            float t = (float) i / 18f;
            float fy = 230f - t * 190f;
            float fx = 128f + MathUtils.sin(t * 5.2f) * 45f;
            float rad = MathUtils.random(14f, 24f);

            for (int p = 0; p < 5; p++) {
                float ang = p * MathUtils.PI2 / 5f + i * 0.2f;
                float px = fx + MathUtils.cos(ang) * (rad * 0.45f);
                float py = fy + MathUtils.sin(ang) * (rad * 0.45f);
                pix.setColor(0.99f, 0.99f, 0.97f, 1f);
                pix.fillCircle(ox + (int)px, oy + (int)py, (int)(rad * 0.5f));
            }
            pix.setColor(1.0f, 0.90f, 0.25f, 1f);
            pix.fillCircle(ox + (int)fx, oy + (int)fy, 3);
        }
    }

    /**
     * White flowering reed plumes (Kashful / Saccharum spontaneum) along the water edges.
     */
    private Texture createKashfulAtlas() {
        int w = 256, h = 512;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill();

        float[] stemX = {65f, 128f, 190f};
        float[] plumeH = {300f, 340f, 280f};

        for (int s = 0; s < 3; s++) {
            float sx = stemX[s];
            float len = plumeH[s];

            // Slender green stem
            pix.setColor(0.22f, 0.46f, 0.15f, 1f);
            for (int y = (int)(h - 1); y >= (int)(h - len); y--) {
                float t = (float)(h - y) / len;
                float curve = MathUtils.sin(t * 1.6f) * 14f * (s % 2 == 0 ? 1f : -1f);
                pix.fillCircle((int)(sx + curve), y, 2);
            }

            // Silky fluffy white plume
            int plumeStart = (int)(h - len);
            int plumeEnd = (int)(h - len * 0.35f);
            for (int y = plumeStart; y < plumeEnd; y++) {
                float pt = (float)(y - plumeStart) / (plumeEnd - plumeStart);
                float width = MathUtils.sin(pt * MathUtils.PI) * 18f;
                float curve = MathUtils.sin(((float)(h - y) / len) * 1.6f) * 14f * (s % 2 == 0 ? 1f : -1f);
                float cx = sx + curve;

                int wisps = (int)(width * 2.2f);
                for (int wisp = 0; wisp < wisps; wisp++) {
                    float wx = cx + MathUtils.random(-width, width);
                    float wy = y + MathUtils.random(-3f, 3f);
                    float brightness = MathUtils.random(0.92f, 1.0f);
                    float alpha = MathUtils.random(0.70f, 0.98f);
                    pix.setColor(brightness, brightness, brightness * 0.96f, alpha);
                    pix.drawPixel((int)wx, (int)wy);
                }
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * Authentic colonial arched window texture: multi-pane glazed window with white stone surround,
     * transom fanlight, and stone sill.
     */
    private Texture createColonialWindow() {
        int w = 256, h = 512;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);

        // Dark deep reflective colonial glass base with sky sheen
        pix.setColor(0.08f, 0.12f, 0.16f, 1f);
        pix.fill();

        for (int y = 0; y < h / 2; y++) {
            float t = (float) y / (h / 2f);
            float r = MathUtils.lerp(0.18f, 0.08f, t);
            float g = MathUtils.lerp(0.24f, 0.12f, t);
            float b = MathUtils.lerp(0.32f, 0.16f, t);
            pix.setColor(r, g, b, 1f);
            pix.drawLine(0, y, w, y);
        }

        // Cream stone pointed arch surround and frame
        Color stoneTrim = new Color(0.95f, 0.93f, 0.89f, 1f);
        pix.setColor(stoneTrim);
        int border = 14;
        pix.fillRectangle(0, 0, w, border);
        pix.fillRectangle(0, h - border * 2, w, border * 2);
        pix.fillRectangle(0, 0, border, h);
        pix.fillRectangle(w - border, 0, border, h);

        // Protruding stone sill highlight
        pix.setColor(0.98f, 0.96f, 0.92f, 1f);
        pix.fillRectangle(4, h - border * 2 - 2, w - 8, 4);

        // White colonial glazing mullions and transoms
        Color mullion = new Color(0.92f, 0.90f, 0.86f, 1f);
        pix.setColor(mullion);
        pix.fillRectangle(w / 2 - 4, border, 8, h - border * 3);
        int glassTop = border;
        int glassH = h - border * 3;
        for (int i = 1; i <= 3; i++) {
            int ty = glassTop + (glassH * i) / 4;
            pix.fillRectangle(border, ty - 3, w - border * 2, 6);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    /**
     * Authentic carved teak wooden door texture: double door leaves with recessed panels,
     * wood grain, and burnished brass ring handles.
     */
    private Texture createTeakDoorTexture() {
        int w = 256, h = 512;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);

        // Burma teak wood grain base
        pix.setColor(0.28f, 0.15f, 0.09f, 1f);
        pix.fill();

        for (int x = 0; x < w; x++) {
            float grain = MathUtils.sin(x * 0.45f) * 0.6f + MathUtils.sin(x * 1.8f) * 0.4f;
            for (int y = 0; y < h; y++) {
                float n = grain + MathUtils.sin(y * 0.08f + x * 0.05f) * 0.25f;
                float r = MathUtils.clamp(0.28f + n * 0.06f, 0.18f, 0.38f);
                float g = MathUtils.clamp(0.15f + n * 0.04f, 0.09f, 0.22f);
                float b = MathUtils.clamp(0.09f + n * 0.03f, 0.05f, 0.14f);
                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Heavy outer teak frame
        int border = 16;
        pix.setColor(0.20f, 0.10f, 0.06f, 1f);
        pix.fillRectangle(0, 0, w, border);
        pix.fillRectangle(0, h - border, w, border);
        pix.fillRectangle(0, 0, border, h);
        pix.fillRectangle(w - border, 0, border, h);

        // Center meeting stile
        pix.fillRectangle(w / 2 - 6, 0, 12, h);
        pix.setColor(0.08f, 0.04f, 0.02f, 1f);
        pix.drawLine(w / 2, 0, w / 2, h);

        // Recessed carved wood panels
        int panelW = (w / 2) - border - 12;
        int upperPanelH = 180;
        int lowerPanelH = 180;
        int[] panelX = {border + 6, w / 2 + 6};

        for (int px : panelX) {
            drawCarvedDoorPanel(pix, px, border + 20, panelW, upperPanelH);
            drawCarvedDoorPanel(pix, px, h / 2 + 20, panelW, lowerPanelH);
        }

        // Burnished brass door handles
        drawBrassDoorHandle(pix, w / 2 - 24, h / 2 + 10);
        drawBrassDoorHandle(pix, w / 2 + 24, h / 2 + 10);

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private void drawCarvedDoorPanel(Pixmap pix, int x, int y, int pw, int ph) {
        // Outer beveled frame shadow
        pix.setColor(0.14f, 0.07f, 0.04f, 1f);
        pix.fillRectangle(x, y, pw, ph);

        // Inner panel face
        pix.setColor(0.26f, 0.14f, 0.08f, 1f);
        pix.fillRectangle(x + 4, y + 4, pw - 8, ph - 8);

        // Top and left bevel highlight
        pix.setColor(0.38f, 0.22f, 0.13f, 1f);
        pix.drawLine(x + 2, y + 2, x + pw - 2, y + 2);
        pix.drawLine(x + 2, y + 2, x + 2, y + ph - 2);

        // Bottom and right bevel shadow
        pix.setColor(0.10f, 0.05f, 0.03f, 1f);
        pix.drawLine(x + 2, y + ph - 2, x + pw - 2, y + ph - 2);
        pix.drawLine(x + pw - 2, y + 2, x + pw - 2, y + ph - 2);
    }

    private void drawBrassDoorHandle(Pixmap pix, int cx, int cy) {
        // Brass backplate
        pix.setColor(0.18f, 0.14f, 0.06f, 1f);
        pix.fillRectangle(cx - 8, cy - 16, 16, 32);

        // Burnished brass finish
        pix.setColor(0.92f, 0.78f, 0.25f, 1f);
        pix.fillRectangle(cx - 6, cy - 14, 12, 28);
        pix.setColor(1.0f, 0.92f, 0.55f, 1f);
        pix.drawLine(cx - 3, cy - 12, cx - 3, cy + 12);

        // Brass ring handle
        pix.setColor(0.95f, 0.82f, 0.28f, 1f);
        pix.drawCircle(cx, cy + 4, 8);
        pix.drawCircle(cx, cy + 4, 7);
    }

    /**
     * Worn dirt path, tileable along its length (V) and feathered to transparent at both sides
     * (U) so a strip of it blends into the grass instead of showing a hard edge.
     */
    private Texture createDirtPath() {
        int size = 128;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float u = x / (float) (size - 1);
                float side = Math.abs(u * 2f - 1f);
                float wobble = 0.10f * MathUtils.sin(y * 0.21f + 1.3f) + 0.06f * MathUtils.sin(y * 0.57f);
                float alpha = 1f - MathUtils.clamp((side + wobble - 0.62f) / 0.34f, 0f, 1f);

                float n = MathUtils.random(-0.05f, 0.05f)
                    + 0.05f * MathUtils.sin(x * 0.4f) * MathUtils.cos(y * 0.33f);
                float r = 0.52f + n, g = 0.40f + n * 0.9f, b = 0.27f + n * 0.7f;
                if (MathUtils.random() < 0.02f) { r += 0.14f; g += 0.13f; b += 0.11f; } // pebbles
                pix.setColor(MathUtils.clamp(r, 0f, 1f), MathUtils.clamp(g, 0f, 1f),
                    MathUtils.clamp(b, 0f, 1f), alpha);
                pix.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    /**
     * Smooth, low-frequency weathered rock: broad tonal clouds with a few darker streaks and pale
     * speckle. Kept soft on purpose; the tiling stone texture read as a wire mesh on round boulders.
     */
    private Texture createRockSurface() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float fx = x / (float) size * MathUtils.PI2, fy = y / (float) size * MathUtils.PI2;
                // Periodic in both axes so the tile has no seam
                float n = 0.5f * MathUtils.sin(fx * 2f + MathUtils.sin(fy * 3f) * 1.2f)
                    + 0.3f * MathUtils.sin(fy * 3f + MathUtils.cos(fx * 2f) * 1.5f)
                    + 0.2f * MathUtils.sin((fx + fy) * 5f);
                float streak = 0.5f + 0.5f * MathUtils.sin(fx * 7f + MathUtils.sin(fy * 2f) * 3f);
                float v = 0.50f + n * 0.07f - streak * 0.05f + MathUtils.random(-0.02f, 0.02f);
                if (MathUtils.random() < 0.01f) v += 0.12f;
                pix.setColor(v * 1.00f, v * 0.98f, v * 0.94f, 1f);
                pix.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    /** Soft white gradient for volumetric light shafts: feathered across, fading at both ends. */
    private Texture createLightBeam() {
        int w = 64, h = 256;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float v = y / (float) (h - 1);
            float along = MathUtils.clamp(v / 0.15f, 0f, 1f) * (1f - MathUtils.clamp((v - 0.70f) / 0.30f, 0f, 1f));
            for (int x = 0; x < w; x++) {
                float u = x / (float) (w - 1);
                float across = MathUtils.sin(u * MathUtils.PI);
                float a = across * across * along;
                pix.setColor(1f, 0.95f, 0.80f, a);
                pix.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * Rasterises one pointed leaf into the atlas cell at (ox, oy). The leaf starts at the base
     * point (bx, by) and runs {@code length} pixels toward {@code angle}; colour grades from the
     * base colour to the tip colour, with a lighter midrib, faint side veins and a darker rim.
     */
    private void drawLeaf(Pixmap pix, int ox, int oy, float bx, float by, float length, float halfWidth,
                          float angle, float br, float bg, float bb, float tr, float tg, float tb) {
        float dx = MathUtils.cos(angle), dy = MathUtils.sin(angle);
        float px = -dy, py = dx; // perpendicular

        float reach = length + halfWidth + 2f;
        // Clip to this leaf's own 256px atlas cell so it can't bleed into its neighbours
        int minX = Math.max(0, (int) (bx - reach)), maxX = Math.min(255, (int) (bx + reach));
        int minY = Math.max(0, (int) (by - reach)), maxY = Math.min(255, (int) (by + reach));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float rx = x + 0.5f - bx, ry = y + 0.5f - by;
                float along = rx * dx + ry * dy;
                float t = along / length;
                if (t < 0f || t > 1f) continue;
                float across = Math.abs(rx * px + ry * py);
                float w = halfWidth * (float) Math.pow(Math.sin(Math.PI * Math.pow(t, 0.72)), 0.8);
                if (across > w + 0.5f) continue;

                float alpha = MathUtils.clamp(w - across + 0.5f, 0f, 1f);
                float edge = w > 0.5f ? across / w : 1f;

                // Greens are lifted so sunlit foliage reads fresh; the red blossoms are left as drawn
                float gain = br > 0.5f ? 1.05f : 1.45f;
                float r = MathUtils.lerp(br, tr, t) * gain;
                float g = MathUtils.lerp(bg, tg, t) * gain;
                float b = MathUtils.lerp(bb, tb, t) * gain;

                // Midrib and lateral veins lighten; the rim darkens
                float vein = 0f;
                if (across < 1.6f) vein = 0.10f;
                else {
                    float phase = (t * 9f - edge * 1.6f) % 1f;
                    if (phase < 0f) phase += 1f;
                    if (phase < 0.10f) vein = 0.05f;
                }
                float rim = 1f - 0.30f * MathUtils.clamp((edge - 0.72f) / 0.28f, 0f, 1f);

                pix.setColor(MathUtils.clamp((r + vein) * rim, 0f, 1f),
                             MathUtils.clamp((g + vein) * rim, 0f, 1f),
                             MathUtils.clamp((b + vein * 0.5f) * rim, 0f, 1f), alpha);
                pix.drawPixel(ox + x, oy + y);
            }
        }
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
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // Fully transparent background for crisp alpha cutout

        // Central woody branch network
        pix.setColor(0.28f, 0.18f, 0.12f, 1f);
        drawBranchLine(pix, 256, 512, 256, 320, 8);
        drawBranchLine(pix, 256, 320, 160, 200, 5);
        drawBranchLine(pix, 256, 320, 350, 190, 5);
        drawBranchLine(pix, 160, 200, 90, 120, 3);
        drawBranchLine(pix, 160, 200, 220, 110, 3);
        drawBranchLine(pix, 350, 190, 420, 100, 3);
        drawBranchLine(pix, 350, 190, 290, 90, 3);

        // Clustered dense tropical leaf sprays with natural light gaps
        float[][] clusters = {
            {256, 120, 95}, {160, 180, 80}, {350, 170, 80},
            {100, 120, 65}, {410, 100, 65}, {220, 90, 70},
            {290, 80, 70}, {180, 250, 75}, {330, 240, 75},
            {256, 260, 85}
        };

        for (float[] cl : clusters) {
            float cx = cl[0], cy = cl[1], rad = cl[2];
            int leafCount = (int)(rad * 1.35f);
            for (int i = 0; i < leafCount; i++) {
                float angle = MathUtils.random(0f, 360f);
                float dist = MathUtils.random(0f, rad);
                float lx = cx + MathUtils.cosDeg(angle) * dist;
                float ly = cy + MathUtils.sinDeg(angle) * dist;
                float leafLen = MathUtils.random(24f, 46f);
                float leafAngle = angle + MathUtils.random(-35f, 35f);

                float var = MathUtils.random(0f, 1f);
                Color blade = new Color(
                    0.16f + var * 0.16f,
                    0.52f + var * 0.28f,
                    0.12f + var * 0.10f,
                    1f
                );
                Color vein = new Color(0.55f, 0.88f, 0.32f, 1f);
                drawBambooBlade(pix, lx, ly, leafLen, leafAngle, 9.5f, blade, vein);
            }
        }

        // Distinct vibrant white and scarlet floral accents matching reference image
        int flowerCount = 65;
        for (int i = 0; i < flowerCount; i++) {
            float fx = MathUtils.random(80, 432);
            float fy = MathUtils.random(70, 390);
            boolean isWhite = (i % 2 == 0);

            if (isWhite) {
                // Crisp bright white blossom with ivory highlight
                pix.setColor(0.99f, 0.99f, 0.96f, 1f);
                pix.fillCircle((int)fx, (int)fy, MathUtils.random(3, 6));
                pix.setColor(1.0f, 0.88f, 0.25f, 1f);
                pix.fillCircle((int)fx, (int)fy, 2);
            } else {
                // Saturated high-contrast scarlet-red floral cluster
                pix.setColor(0.98f, 0.14f, 0.18f, 1f);
                pix.fillCircle((int)fx, (int)fy, MathUtils.random(4, 7));
                pix.setColor(1.0f, 0.90f, 0.30f, 1f);
                pix.fillCircle((int)fx, (int)fy, 2);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createKrishnachuraBlossom() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // Fully transparent background

        // Delicate woody branchlets
        pix.setColor(0.30f, 0.20f, 0.12f, 1f);
        drawBranchLine(pix, 256, 512, 256, 300, 7);
        drawBranchLine(pix, 256, 300, 140, 180, 4);
        drawBranchLine(pix, 256, 300, 370, 170, 4);

        // Feathery bipinnate green fronds
        for (int b = 0; b < 28; b++) {
            float fx = MathUtils.random(70, 442);
            float fy = MathUtils.random(50, 390);
            float fLen = MathUtils.random(65, 120);
            float fAngle = MathUtils.random(-80, 80);
            Color fernCol = new Color(0.12f, 0.48f, 0.10f, 1f);
            Color fernVein = new Color(0.45f, 0.82f, 0.24f, 1f);
            drawBambooBlade(pix, fx, fy, fLen, fAngle, 7.5f, fernCol, fernVein);
        }

        // Iconic blazing high-contrast scarlet-orange Krishnachura flowers
        int flowerCount = 95;
        for (int i = 0; i < flowerCount; i++) {
            float fx = MathUtils.random(50, 462);
            float fy = MathUtils.random(40, 410);
            int petLen = MathUtils.random(11, 22);

            // 5 fiery petals radiating outward with intense contrast against deep green
            for (int p = 0; p < 5; p++) {
                float pAngle = p * 72f + MathUtils.random(-12f, 12f);
                float px = fx + MathUtils.cosDeg(pAngle) * petLen;
                float py = fy + MathUtils.sinDeg(pAngle) * petLen;

                // Glowing scarlet red petal with fiery vermilion flame highlight
                float shade = MathUtils.random(0.90f, 1.0f);
                pix.setColor(0.99f * shade, 0.12f + MathUtils.random(-0.02f, 0.08f), 0.06f, 1f);
                pix.fillCircle((int)px, (int)py, Math.max(4, petLen / 3));
            }

            // High-contrast luminous gold central stamens
            pix.setColor(1.0f, 0.92f, 0.20f, 1f);
            pix.fillCircle((int)fx, (int)fy, 4);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createBambooCulm() {
        int width = 256;
        int height = 512;
        Pixmap pix = new Pixmap(width, height, Format.RGBA8888);

        // Authentic bamboo cane base color (olive-yellowish green)
        for (int x = 0; x < width; x++) {
            float xNorm = (float) x / (float) width;
            // Cylindrical lighting shading across cane width
            float cylLight = MathUtils.sin(xNorm * MathUtils.PI);
            float vertFiber = MathUtils.sin(x * 0.85f) * 0.04f;

            for (int y = 0; y < height; y++) {
                float r = MathUtils.clamp((0.44f + vertFiber) * (0.6f + 0.4f * cylLight), 0.22f, 0.65f);
                float g = MathUtils.clamp((0.68f + vertFiber) * (0.6f + 0.4f * cylLight), 0.38f, 0.88f);
                float b = MathUtils.clamp((0.24f + vertFiber * 0.5f) * (0.6f + 0.4f * cylLight), 0.12f, 0.40f);
                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Horizontal bamboo joints / nodal rings every 64 pixels
        int nodeInterval = 64;
        for (int y = nodeInterval; y < height; y += nodeInterval) {
            // Upper swollen highlight line (pale ivory-gold)
            pix.setColor(0.78f, 0.84f, 0.45f, 0.95f);
            pix.drawLine(0, y - 2, width, y - 2);

            // Dark joint ring indentation (woody brownish olive)
            pix.setColor(0.20f, 0.24f, 0.10f, 1f);
            pix.drawLine(0, y - 1, width, y - 1);
            pix.drawLine(0, y, width, y);

            // Subtle lower shadow line
            pix.setColor(0.28f, 0.34f, 0.14f, 0.75f);
            pix.drawLine(0, y + 1, width, y + 1);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createBambooFoliage() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // 100% transparent background

        // Slender bamboo twigs
        pix.setColor(0.38f, 0.46f, 0.18f, 1f);
        drawBranchLine(pix, 256, 512, 256, 320, 5);
        drawBranchLine(pix, 256, 320, 160, 210, 3);
        drawBranchLine(pix, 256, 320, 360, 200, 3);
        drawBranchLine(pix, 160, 210, 80, 140, 2);
        drawBranchLine(pix, 160, 210, 230, 130, 2);
        drawBranchLine(pix, 360, 200, 440, 120, 2);
        drawBranchLine(pix, 360, 200, 290, 110, 2);

        // Nodes from which bamboo leaf sprays radiate
        float[][] sprayNodes = {
            {256, 320}, {160, 210}, {360, 200},
            {80, 140}, {230, 130}, {440, 120}, {290, 110},
            {120, 90}, {200, 70}, {320, 70}, {400, 80},
            {256, 160}
        };

        for (float[] node : sprayNodes) {
            float nx = node[0], ny = node[1];
            int leavesInSpray = MathUtils.random(5, 8);
            for (int l = 0; l < leavesInSpray; l++) {
                float len = MathUtils.random(65f, 135f);
                float angle = MathUtils.random(-80f, 80f);
                float halfW = MathUtils.random(6.5f, 9.5f);

                float tint = MathUtils.random(0f, 1f);
                Color bladeCol = new Color(
                    0.20f + tint * 0.16f,
                    0.60f + tint * 0.22f,
                    0.15f + tint * 0.10f,
                    1f
                );
                Color veinCol = new Color(0.60f, 0.88f, 0.32f, 1f);
                drawBambooBlade(pix, nx, ny, len, angle, halfW, bladeCol, veinCol);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createBushFoliage() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // Transparent background

        // Ground-hugging shrub dome with radiating rosettes of serrated leaves
        float[][] bushCenters = {
            {256, 320, 130}, {160, 360, 100}, {350, 350, 100},
            {200, 220, 95}, {310, 210, 95}, {256, 140, 80}
        };

        for (float[] bc : bushCenters) {
            float cx = bc[0], cy = bc[1], rad = bc[2];
            int leafCount = (int)(rad * 0.9f);
            for (int i = 0; i < leafCount; i++) {
                float angle = MathUtils.random(0f, 360f);
                float dist = MathUtils.random(0f, rad);
                float lx = cx + MathUtils.cosDeg(angle) * dist;
                float ly = cy + MathUtils.sinDeg(angle) * dist;
                float len = MathUtils.random(24f, 48f);
                float w = MathUtils.random(8f, 13f);

                float tint = MathUtils.random(0f, 1f);
                Color blade = new Color(
                    0.16f + tint * 0.14f,
                    0.46f + tint * 0.28f,
                    0.14f + tint * 0.10f,
                    1f
                );
                Color vein = new Color(0.52f, 0.82f, 0.28f, 1f);
                drawBambooBlade(pix, lx, ly, len, angle, w, blade, vein);
            }
        }

        // Tiny white and scarlet wildflowers matching reference image
        int flowerCount = 38;
        for (int i = 0; i < flowerCount; i++) {
            float fx = MathUtils.random(90, 420);
            float fy = MathUtils.random(110, 420);
            boolean isWhite = (i % 2 == 0);

            if (isWhite) {
                pix.setColor(0.96f, 0.96f, 0.94f, 1f);
            } else {
                pix.setColor(0.92f, 0.18f, 0.22f, 1f);
            }
            pix.fillCircle((int)fx, (int)fy, MathUtils.random(3, 6));

            // Gold floral center
            pix.setColor(1.0f, 0.88f, 0.20f, 1f);
            pix.fillCircle((int)fx, (int)fy, 2);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createWeatheredStone() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Granite base with mineral flecks
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float n = MathUtils.sin(x * 0.15f) * MathUtils.cos(y * 0.18f)
                        + MathUtils.sin((x + y) * 0.32f) * 0.3f;
                float noise = (MathUtils.random(-0.06f, 0.06f));
                float stone = MathUtils.clamp(0.50f + n * 0.08f + noise, 0.35f, 0.68f);

                // Natural cool slate grey
                pix.setColor(stone, stone * 0.98f, stone * 0.95f, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Natural dark stone fissures and chisel cracks
        pix.setColor(0.22f, 0.20f, 0.18f, 0.85f);
        int[] cracksY = {90, 180, 275, 380};
        for (int cy : cracksY) {
            int curX = 0;
            int curY = cy;
            while (curX < size) {
                int nextX = curX + MathUtils.random(12, 28);
                int nextY = curY + MathUtils.random(-6, 6);
                pix.drawLine(curX, curY, nextX, nextY);
                // Subtle bright highlight edge underneath crack
                pix.setColor(0.72f, 0.70f, 0.68f, 0.5f);
                pix.drawLine(curX, curY + 1, nextX, nextY + 1);
                pix.setColor(0.22f, 0.20f, 0.18f, 0.85f);
                curX = nextX;
                curY = nextY;
            }
        }

        // Velvety green moss and lichen creeping along crevices and base
        for (int x = 0; x < size; x++) {
            for (int y = 280; y < size; y++) {
                float mossProb = (float)(y - 280) / (float)(size - 280);
                float patch = (MathUtils.sin(x * 0.08f) + MathUtils.cos(y * 0.08f)) * 0.5f;
                if (mossProb + patch * 0.4f > 0.65f) {
                    float mg = MathUtils.random(0.36f, 0.52f);
                    float mr = mg * 0.62f;
                    float mb = mg * 0.35f;
                    pix.setColor(mr, mg, mb, 0.85f);
                    pix.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createCurzonWater() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Deep serene turquoise-blue pool water
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float wave = (MathUtils.sin(x * 0.14f + y * 0.12f) + MathUtils.cos((x - y) * 0.18f)) * 0.5f;
                float r = MathUtils.clamp(0.12f + wave * 0.05f, 0.08f, 0.22f);
                float g = MathUtils.clamp(0.38f + wave * 0.10f, 0.28f, 0.52f);
                float b = MathUtils.clamp(0.48f + wave * 0.12f, 0.36f, 0.65f);
                pix.setColor(r, g, b, 0.92f);
                pix.drawPixel(x, y);
            }
        }

        // Floating green water lily pads
        pix.setColor(0.18f, 0.48f, 0.16f, 0.95f);
        int[][] pads = {{45, 60, 14}, {180, 95, 18}, {90, 190, 16}, {210, 180, 15}, {130, 140, 12}};
        for (int[] p : pads) {
            pix.fillCircle(p[0], p[1], p[2]);
            // Lily notch
            pix.setColor(0.14f, 0.36f, 0.46f, 1f);
            pix.fillTriangle(p[0], p[1], p[0] + p[2], p[1] - 4, p[0] + p[2], p[1] + 4);
            // Delicate pink lotus bloom
            pix.setColor(0.96f, 0.62f, 0.78f, 1f);
            pix.fillCircle(p[0], p[1], 4);
            pix.setColor(0.18f, 0.48f, 0.16f, 0.95f);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    /** Tileable caustic highlight lines on transparent black, scrolled over the pukur surface. */
    private Texture createWaterRipple() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        float k = MathUtils.PI2 / size;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // Integer frequencies keep every term periodic over the tile, so it wraps seamlessly
                float a = MathUtils.sin(x * k * 3f + MathUtils.sin(y * k * 2f) * 1.6f);
                float b = MathUtils.sin(y * k * 4f + MathUtils.sin(x * k * 3f) * 1.3f);
                float c = MathUtils.sin((x + y) * k * 2f + MathUtils.cos(x * k * 5f) * 0.9f);
                float lines = 1f - Math.abs((a + b + c) / 3f);
                float glint = (float) Math.pow(lines, 7.0);
                pix.setColor(0.85f, 0.95f, 1f, MathUtils.clamp(glint * 0.9f, 0f, 1f));
                pix.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private void drawBranchLine(Pixmap pix, int x0, int y0, int x1, int y1, int thickness) {
        int half = thickness / 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                pix.drawLine(x0 + dx, y0 + dy, x1 + dx, y1 + dy);
            }
        }
    }

    private void drawBambooBlade(Pixmap pix, float startX, float startY, float length, float angleDeg,
                                float maxHalfWidth, Color bladeCol, Color veinCol) {
        float rad = angleDeg * MathUtils.degreesToRadians;
        float dirX = MathUtils.cos(rad);
        float dirY = MathUtils.sin(rad);
        float normX = -dirY;
        float normY = dirX;

        int steps = (int)(length * 1.5f);
        for (int s = 0; s <= steps; s++) {
            float t = (float) s / (float) steps;
            float px = startX + dirX * length * t;
            float py = startY + dirY * length * t;

            // Bamboo leaf profile: slender, widest at t ~ 0.30, tapers to acute tip at t = 1.0
            float w = maxHalfWidth * MathUtils.sin(t * MathUtils.PI) * (1.1f - 0.35f * t);

            int span = (int) Math.ceil(w);
            for (int d = -span; d <= span; d++) {
                if (Math.abs(d) <= w) {
                    int ix = Math.round(px + normX * d);
                    int iy = Math.round(py + normY * d);
                    if (ix >= 0 && ix < pix.getWidth() && iy >= 0 && iy < pix.getHeight()) {
                        if (Math.abs(d) < 1.0f) {
                            pix.setColor(veinCol);
                        } else {
                            float edgeDarken = 1.0f - 0.22f * (Math.abs(d) / (w + 0.01f));
                            pix.setColor(bladeCol.r * edgeDarken, bladeCol.g * edgeDarken, bladeCol.b * edgeDarken, 1f);
                        }
                        pix.drawPixel(ix, iy);
                    }
                }
            }
        }
    }

    private Texture createBdFlag() {
        int w = 500;
        int h = 300;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);

        // Bottle green field (#006A4E)
        pix.setColor(0.0f, 0.416f, 0.306f, 1f);
        pix.fill();

        // Crimson red circle offset slightly to left (#F42A41)
        pix.setColor(0.957f, 0.165f, 0.255f, 1f);
        pix.fillCircle(225, 150, 100);

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createAparajeyoStone() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Chiseled white/grey sculpture stone
        pix.setColor(0.86f, 0.85f, 0.82f, 1f);
        pix.fill();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float n = (MathUtils.sin(x * 0.3f) * MathUtils.cos(y * 0.3f)) * 0.04f;
                float c = MathUtils.clamp(0.86f + n + MathUtils.random(-0.02f, 0.02f), 0.78f, 0.94f);
                pix.setColor(c, c - 0.01f, c - 0.03f, 1f);
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

    private Texture createAsphaltRoad() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Bitumen dark charcoal base
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float noise = (MathUtils.sin(x * 12.3f + y * 7.9f) + MathUtils.cos(x * 5.1f - y * 14.2f)) * 0.5f;
                float base = 0.18f + noise * 0.04f;
                pix.setColor(base, base * 1.02f, base * 1.05f, 1f);
                pix.drawPixel(x, y);
            }
        }

        // White dashed center line down the middle
        int stripeW = 12;
        int stripeH = 64;
        int gapH = 48;
        int centerX = size / 2 - stripeW / 2;

        pix.setColor(0.92f, 0.90f, 0.85f, 0.92f);
        for (int y = 0; y < size; y += stripeH + gapH) {
            pix.fillRectangle(centerX, y, stripeW, stripeH);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createModernistConcrete() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Warm light-grey architectural concrete (Doxiadis modernist style)
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float n = (MathUtils.sin(x * 18.2f + y * 9.1f) + MathUtils.cos(x * 7.5f - y * 19.3f)) * 0.5f;
                float c = 0.78f + n * 0.035f;
                pix.setColor(c * 1.02f, c, c * 0.96f, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Shuttering panel seams every 128 pixels
        pix.setColor(0.62f, 0.60f, 0.58f, 0.7f);
        for (int p = 0; p < size; p += 128) {
            pix.drawLine(0, p, size - 1, p);
            pix.drawLine(p, 0, p, size - 1);
            // Formwork tie-rod holes
            pix.setColor(0.45f, 0.42f, 0.40f, 0.9f);
            pix.fillCircle(p + 16, p + 16, 3);
            pix.fillCircle(p + 112, p + 16, 3);
            pix.fillCircle(p + 16, p + 112, 3);
            pix.fillCircle(p + 112, p + 112, 3);
            pix.setColor(0.62f, 0.60f, 0.58f, 0.7f);
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createCanteenTinRoof() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Weathered reddish terracotta-tin roof base
        pix.setColor(0.62f, 0.28f, 0.22f, 1f);
        pix.fill();

        // Corrugated vertical ridges
        int ridgeWidth = 16;
        for (int x = 0; x < size; x += ridgeWidth) {
            // Highlight left edge
            pix.setColor(0.74f, 0.36f, 0.28f, 0.85f);
            pix.fillRectangle(x, 0, ridgeWidth / 2, size);
            // Shadow right edge
            pix.setColor(0.48f, 0.18f, 0.14f, 0.85f);
            pix.fillRectangle(x + ridgeWidth / 2, 0, ridgeWidth / 2, size);
            // Subtle horizontal weathering streaks
            for (int y = 0; y < size; y += 32) {
                pix.setColor(0.40f, 0.20f, 0.16f, 0.3f);
                pix.drawLine(x, y, x + ridgeWidth, y);
            }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pix.dispose();
        return tex;
    }

    private Texture createMapVisualTarget() {
        int w = 512;
        int h = 256;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);

        // Sky background gradient (warm golden morning)
        for (int y = 0; y < h; y++) {
            float t = (float) y / h;
            pix.setColor(0.92f - t * 0.12f, 0.88f - t * 0.15f, 0.78f - t * 0.25f, 1f);
            pix.drawLine(0, y, w, y);
        }

        // Lush green trees backdrop
        pix.setColor(0.18f, 0.42f, 0.16f, 1f);
        for (int x = 20; x < w - 20; x += 40) {
            pix.fillCircle(x, 140, 45);
        }

        // Curzon Hall Facade Red Terracotta Block
        int bY = 110;
        int bH = 95;
        pix.setColor(0.74f, 0.24f, 0.18f, 1f);
        pix.fillRectangle(60, bY, w - 120, bH);

        // White domes & finials
        pix.setColor(0.95f, 0.94f, 0.90f, 1f);
        pix.fillCircle(w / 2, bY - 15, 38);
        pix.setColor(1.0f, 0.84f, 0.30f, 1f);
        pix.fillRectangle(w / 2 - 2, bY - 65, 4, 30); // Finial spire

        // Side domes
        pix.setColor(0.95f, 0.94f, 0.90f, 1f);
        pix.fillCircle(120, bY, 18);
        pix.fillCircle(w - 120, bY, 18);

        // Arched windows and white cusped portico
        pix.setColor(0.96f, 0.94f, 0.90f, 1f);
        pix.fillRectangle(w / 2 - 35, bY + 15, 70, bH - 15);
        pix.setColor(0.20f, 0.12f, 0.08f, 1f);
        pix.fillRectangle(w / 2 - 22, bY + 30, 44, bH - 30);

        // Flanking arched windows
        for (int i = 0; i < 4; i++) {
            int wxL = 80 + i * 28;
            int wxR = w - 80 - i * 28;
            pix.setColor(0.96f, 0.94f, 0.90f, 1f);
            pix.fillRectangle(wxL - 2, bY + 35, 18, 42);
            pix.fillRectangle(wxR - 16, bY + 35, 18, 42);
            pix.setColor(0.22f, 0.12f, 0.08f, 1f);
            pix.fillRectangle(wxL, bY + 38, 14, 38);
            pix.fillRectangle(wxR - 14, bY + 38, 14, 38);
        }

        // Green lawn in front
        pix.setColor(0.24f, 0.55f, 0.20f, 1f);
        pix.fillRectangle(0, bY + bH, w, h - (bY + bH));

        // Central brick walkway
        pix.setColor(0.72f, 0.32f, 0.22f, 1f);
        pix.fillTriangle(w / 2 - 30, bY + bH, w / 2 + 30, bY + bH, w / 2 + 75, h);
        pix.fillTriangle(w / 2 - 30, bY + bH, w / 2 - 75, h, w / 2 + 75, h);

        // Gold border framing
        pix.setColor(0.85f, 0.70f, 0.30f, 1f);
        pix.drawRectangle(0, 0, w, h);
        pix.drawRectangle(1, 1, w - 2, h - 2);

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
