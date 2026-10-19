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
    public final Texture lotusAtlas;
    public final Texture grassTuftAtlas;
    public final Texture colonialWindow;
    public final Texture teakDoorTexture;
    public final Texture curzonGrandPortal;
    public final Texture curzonPorticoTripleWindow;
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
    public final Texture debugOrientationTestCard;

    // ── Tangent-Space Normal Maps for PBR ──────────────────────────────────
    public final Texture brickPavementNormal;
    public final Texture curzonBrickNormal;
    public final Texture modernistConcreteNormal;
    public final Texture treeBarkNormal;
    public final Texture waterRippleNormal;
    public final Texture lawnGrassNormal;
    public final Texture soilLoam;
    public final Texture soilLoamNormal;
    public final Texture dirtPathNormal;
    public final Texture asphaltRoadNormal;

    public TextureFactory() {
        Texture[] brickPair = createBrickPavementWithNormal();
        brickPavement = brickPair[0];
        brickPavementNormal = brickPair[1];

        Texture[] curzonPair = createCurzonBrickWithNormal();
        curzonBrick = curzonPair[0];
        curzonBrickNormal = curzonPair[1];

        Texture[] grassPair = createLawnGrassWithNormal();
        lawnGrass = grassPair[0];
        lawnGrassNormal = grassPair[1];

        Texture[] soilPair = createSoilLoamWithNormal();
        soilLoam = soilPair[0];
        soilLoamNormal = soilPair[1];

        Texture[] barkPair = createTreeBarkWithNormal();
        treeBark = barkPair[0];
        treeBarkNormal = barkPair[1];

        foliage = createFoliage();
        krishnachuraBlossom = createKrishnachuraBlossom();
        leafClump = createLeafClump(false);
        blossomClump = createLeafClump(true);
        whiteBlossomClump = createWhiteBlossomClump();
        leafAtlas = createLeafAtlas();
        bambooAtlas = createBambooAtlas();
        whiteBlossomAtlas = createWhiteBlossomAtlas();
        kashfulAtlas = createKashfulAtlas();
        lotusAtlas = createLotusAtlas();
        grassTuftAtlas = createGrassTuftAtlas();
        colonialWindow = createColonialWindow();
        teakDoorTexture = createTeakDoorTexture();
        curzonGrandPortal = createCurzonGrandPortal();
        curzonPorticoTripleWindow = createCurzonPorticoTripleWindow();

        Texture[] dirtPair = createDirtPathWithNormal();
        dirtPath = dirtPair[0];
        dirtPathNormal = dirtPair[1];

        lightBeam = createLightBeam();
        rockSurface = createRockSurface();
        bambooCulm = createBambooCulm();
        bambooFoliage = createBambooFoliage();
        bushFoliage = createBushFoliage();
        weatheredStone = createWeatheredStone();
        curzonWater = createCurzonWater();
        waterRipple = createWaterRipple();
        waterRippleNormal = createWaterRippleNormal();
        backpackFabric = createBackpackFabric();
        studentJacket = createStudentJacket();
        movementBanner = createMovementBanner();
        bdFlag = createBdFlag();
        aparajeyoStone = createAparajeyoStone();

        Texture[] asphaltPair = createAsphaltRoadWithNormal();
        asphaltRoad = asphaltPair[0];
        asphaltRoadNormal = asphaltPair[1];

        Texture[] concretePair = createModernistConcreteWithNormal();
        modernistConcrete = concretePair[0];
        modernistConcreteNormal = concretePair[1];

        canteenTinRoof = createCanteenTinRoof();
        mapVisualTarget = createMapVisualTarget();
        debugOrientationTestCard = createDebugOrientationTestCard();

        textures.addAll(brickPavement, curzonBrick, lawnGrass, treeBark,
                        foliage, krishnachuraBlossom, leafClump, blossomClump, whiteBlossomClump,
                        leafAtlas, bambooAtlas, whiteBlossomAtlas, kashfulAtlas, lotusAtlas, grassTuftAtlas,
                        colonialWindow, teakDoorTexture, curzonGrandPortal, curzonPorticoTripleWindow,
                        dirtPath, lightBeam, rockSurface, bambooCulm, bambooFoliage,
                        bushFoliage, weatheredStone, curzonWater, waterRipple,
                        backpackFabric, studentJacket, movementBanner, bdFlag, aparajeyoStone,
                        asphaltRoad, modernistConcrete, canteenTinRoof, mapVisualTarget, debugOrientationTestCard,
                        brickPavementNormal, curzonBrickNormal, modernistConcreteNormal, treeBarkNormal,
                        waterRippleNormal, lawnGrassNormal, soilLoam, soilLoamNormal, dirtPathNormal, asphaltRoadNormal);
    }

    public static Texture createNormalMap(Pixmap src, float bumpScale) {
        int w = src.getWidth();
        int h = src.getHeight();
        Pixmap normPix = new Pixmap(w, h, Format.RGBA8888);

        for (int y = 0; y < h; y++) {
            int yPrev = (y - 1 + h) % h;
            int yNext = (y + 1) % h;
            for (int x = 0; x < w; x++) {
                int xPrev = (x - 1 + w) % w;
                int xNext = (x + 1) % w;

                float left  = getLuminance(src.getPixel(xPrev, y));
                float right = getLuminance(src.getPixel(xNext, y));
                float down  = getLuminance(src.getPixel(x, yPrev));
                float up    = getLuminance(src.getPixel(x, yNext));

                float dx = (right - left) * bumpScale;
                float dy = (up - down) * bumpScale;
                float dz = 1.0f;

                float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float nx = -dx / len;
                float ny = -dy / len;
                float nz = dz / len;

                normPix.setColor(nx * 0.5f + 0.5f, ny * 0.5f + 0.5f, nz * 0.5f + 0.5f, 1.0f);
                normPix.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(normPix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        normPix.dispose();
        return tex;
    }

    private static float getLuminance(int rgba) {
        float r = ((rgba >>> 24) & 0xff) / 255.0f;
        float g = ((rgba >>> 16) & 0xff) / 255.0f;
        float b = ((rgba >>> 8) & 0xff) / 255.0f;
        return 0.299f * r + 0.587f * g + 0.114f * b;
    }

    private Texture[] createBrickPavementWithNormal() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Mortar base color (weathered warm earthy joint)
        Color mortar = new Color(0.28f, 0.22f, 0.18f, 1f);
        pix.setColor(mortar);
        pix.fill();

        // High-density authentic clay brick pavement (32 rows, 16 bricks per row)
        int rows = 32;
        int rowHeight = size / rows;
        int brickWidth = size / 16;

        for (int r = 0; r < rows; r++) {
            int y = r * rowHeight;
            int xOffset = (r % 2 == 0) ? 0 : brickWidth / 2;

            for (int b = -1; b <= 17; b++) {
                int x = b * brickWidth + xOffset;

                // Authentic weathered terracotta clay brick with natural tone shifts
                float var = (MathUtils.sin(r * 4.3f + b * 6.1f) + 1f) * 0.5f;
                float wear = (MathUtils.sin(r * 11.7f - b * 5.9f) + 1f) * 0.5f;
                float red = 0.50f + var * 0.12f - wear * 0.06f;
                float green = 0.26f + var * 0.06f - wear * 0.03f;
                float blue = 0.19f + var * 0.05f - wear * 0.03f;

                pix.setColor(red, green, blue, 1f);
                pix.fillRectangle(x + 1, y + 1, brickWidth - 2, rowHeight - 2);

                // Subtle edge highlight and shadow bevel
                pix.setColor(red + 0.08f, green + 0.06f, blue + 0.04f, 0.55f);
                pix.drawLine(x + 1, y + 1, x + brickWidth - 2, y + 1);
                pix.drawLine(x + 1, y + 1, x + 1, y + rowHeight - 2);

                pix.setColor(0.18f, 0.10f, 0.08f, 0.45f);
                pix.drawLine(x + 1, y + rowHeight - 2, x + brickWidth - 2, y + rowHeight - 2);
                pix.drawLine(x + brickWidth - 2, y + 1, x + brickWidth - 2, y + rowHeight - 2);
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 2.5f);
        pix.dispose();
        return new Texture[]{tex, norm};
    }

    private Texture[] createCurzonBrickWithNormal() {
        int size = 1024;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Aged deep terracotta lime-sand mortar base (warm dark joint, eliminating stark white lines)
        pix.setColor(0.24f, 0.13f, 0.09f, 1f);
        pix.fill();

        // Authentic fine-scale Dhaka University Curzon Hall brick courses (96 rows x 48 columns)
        int rows = 96;
        int rowH = size / rows;
        int brickW = size / 48;

        for (int r = 0; r < rows; r++) {
            int y = r * rowH;
            int xOffset = (r % 2 == 0) ? 0 : brickW / 2;

            for (int b = -1; b <= 49; b++) {
                int x = b * brickW + xOffset;
                float v = (MathUtils.sin(r * 3.7f + b * 5.3f) + 1f) * 0.5f;
                float v2 = (MathUtils.cos(r * 7.1f - b * 4.1f) + 1f) * 0.5f;

                // Rich, deep authentic Mughal terracotta burnt-red variation
                float rCol = MathUtils.clamp(0.58f + v * 0.14f - v2 * 0.08f, 0.44f, 0.74f);
                float gCol = MathUtils.clamp(0.20f + v * 0.05f - v2 * 0.03f, 0.12f, 0.27f);
                float bCol = MathUtils.clamp(0.13f + v * 0.04f - v2 * 0.03f, 0.08f, 0.18f);

                pix.setColor(rCol, gCol, bCol, 1f);
                pix.fillRectangle(x + 1, y + 1, brickW - 1, rowH - 1);

                // Baked-clay micro-texture stipples
                for (int s = 0; s < 3; s++) {
                    int sx = x + 1 + MathUtils.random(Math.max(1, brickW - 3));
                    int sy = y + 1 + MathUtils.random(Math.max(1, rowH - 3));
                    float stipple = MathUtils.random(-0.04f, 0.04f);
                    pix.setColor(MathUtils.clamp(rCol + stipple, 0.40f, 0.78f),
                                 MathUtils.clamp(gCol + stipple * 0.5f, 0.10f, 0.30f),
                                 MathUtils.clamp(bCol + stipple * 0.3f, 0.07f, 0.20f), 1f);
                    pix.drawPixel(sx, sy);
                }
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 3.0f);
        pix.dispose();
        return new Texture[]{tex, norm};
    }

    private Texture[] createLawnGrassWithNormal() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Lush tropical monsoon lawn grass base (healthy Bangladeshi July summer turf)
        pix.setColor(0.18f, 0.44f, 0.14f, 1f);
        pix.fill();

        // Multi-frequency organic turf blades and warm alluvial soil undertones
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float n1 = (MathUtils.sin(x * 0.18f) * MathUtils.cos(y * 0.20f)
                          + MathUtils.sin((x + y) * 0.38f) * 0.5f);
                float n2 = (MathUtils.sin(x * 0.65f + y * 0.35f) * 0.25f);
                float speckle = MathUtils.random(-0.03f, 0.03f);

                // Natural lush tropical greens with rich chlorophyll and subtle warm undertones
                float r = MathUtils.clamp(0.18f + (n1 + n2) * 0.04f + speckle, 0.12f, 0.26f);
                float g = MathUtils.clamp(0.44f + (n1 + n2) * 0.08f + speckle * 1.2f, 0.32f, 0.58f);
                float b = MathUtils.clamp(0.14f + (n1 + n2) * 0.03f + speckle * 0.5f, 0.08f, 0.22f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Fine blade striations
        for (int b = 0; b < 1600; b++) {
            int bx = MathUtils.random(size - 1);
            int by = MathUtils.random(size - 12);
            int len = MathUtils.random(4, 11);
            float shade = MathUtils.random(-0.04f, 0.05f);
            pix.setColor(MathUtils.clamp(0.18f + shade, 0.11f, 0.27f),
                         MathUtils.clamp(0.48f + shade * 1.3f, 0.34f, 0.62f),
                         MathUtils.clamp(0.14f + shade * 0.8f, 0.08f, 0.23f), 1f);
            pix.drawLine(bx, by, bx + MathUtils.random(-1, 1), by + len);
        }

        // Scattered fallen white blossom petals matching reference image
        for (int p = 0; p < 90; p++) {
            int px = MathUtils.random(size - 6);
            int py = MathUtils.random(size - 6);
            int pr = MathUtils.random(2, 4);
            pix.setColor(0.94f, 0.94f, 0.92f, 0.85f);
            pix.fillCircle(px, py, pr);
            pix.setColor(0.98f, 0.88f, 0.35f, 0.70f);
            pix.drawPixel(px, py);
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 3.2f);
        pix.dispose();
        return new Texture[]{tex, norm};
    }

    private Texture[] createSoilLoamWithNormal() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Rich dark alluvial monsoon silt / humus base
        pix.setColor(0.18f, 0.13f, 0.09f, 1f);
        pix.fill();

        // Multi-frequency organic loam texture (earth grain, moist silt, micro-pebbles)
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float n1 = (MathUtils.sin(x * 0.12f) * MathUtils.cos(y * 0.15f)
                          + MathUtils.sin((x + y) * 0.28f) * 0.5f);
                float n2 = (MathUtils.sin(x * 0.45f - y * 0.38f) * 0.25f);
                float speckle = MathUtils.random(-0.03f, 0.03f);

                float r = MathUtils.clamp(0.20f + (n1 + n2) * 0.035f + speckle, 0.12f, 0.28f);
                float g = MathUtils.clamp(0.15f + (n1 + n2) * 0.028f + speckle, 0.09f, 0.22f);
                float b = MathUtils.clamp(0.10f + (n1 + n2) * 0.020f + speckle * 0.7f, 0.06f, 0.16f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Decomposed organic mulch specks and tiny river pebbles
        for (int i = 0; i < 600; i++) {
            int px = MathUtils.random(size - 2);
            int py = MathUtils.random(size - 2);
            float pebble = MathUtils.random(0.08f, 0.16f);
            pix.setColor(0.28f + pebble, 0.24f + pebble * 0.9f, 0.18f + pebble * 0.7f, 1f);
            pix.drawPixel(px, py);
            pix.drawPixel(px + 1, py);
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 2.5f);
        pix.dispose();
        return new Texture[]{tex, norm};
    }

    /**
     * Opaque, tileable canopy texture for the leaf-clump meshes: a dark shaded base overlaid with
     * hundreds of small leaves in varied greens, and (for the flame tree) scarlet blossoms.
     */
    private Texture createLeafClump(boolean blossoms) {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        pix.setColor(0.05f, 0.14f, 0.05f, 1f);
        pix.fill();

        for (int i = 0; i < 1300; i++) {
            int x = MathUtils.random(size - 1);
            int y = MathUtils.random(size - 1);
            float t = MathUtils.random();
            int r = MathUtils.random(3, 7);

            if (blossoms && MathUtils.random() < 0.65f) {
                // High-contrast, fiery scarlet-crimson Krishnachura blossoms
                float rCol = MathUtils.clamp(0.94f + 0.06f * t, 0.88f, 1.0f);
                float gCol = MathUtils.clamp(0.10f + 0.12f * t, 0.06f, 0.24f);
                float bCol = 0.04f;
                pix.setColor(rCol, gCol, bCol, 1f);
            } else {
                // Deep organic forest jade-green (zero neon/catceta)
                pix.setColor(0.06f + 0.05f * t, 0.16f + 0.09f * t, 0.05f + 0.03f * t, 1f);
            }
            for (int ox = -size; ox <= size; ox += size) {
                for (int oy = -size; oy <= size; oy += size) {
                    pix.fillCircle(x + ox, y + oy, r);
                    if (blossoms && MathUtils.random() < 0.20f) {
                        pix.setColor(0.98f, 0.88f, 0.26f, 1f); // golden stamen center
                        pix.drawPixel(x + ox, y + oy);
                    }
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
     * Soft, dense canopy texture for the Feathery Bipinnate Tree:
     * Deep chlorophyll forest-moss base with thousands of delicate micro-leaflets in vibrant
     * emerald, jade, and sunlit golden-amber tones matching the feathery "jira jira" cards.
     */
    private Texture createWhiteBlossomClump() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        // Deep organic forest moss green base
        pix.setColor(0.06f, 0.16f, 0.06f, 1f);
        pix.fill();

        for (int i = 0; i < 1600; i++) {
            int x = MathUtils.random(size - 1);
            int y = MathUtils.random(size - 1);
            float t = MathUtils.random();
            int r = MathUtils.random(3, 7);

            // Layered rich natural chlorophyll greens with warm sunlit foliage tips
            if (t > 0.65f) {
                // Sunlit tender golden-green tips
                pix.setColor(0.18f + 0.08f * t, 0.44f + 0.12f * t, 0.12f + 0.04f * t, 1f);
            } else if (t > 0.30f) {
                // Vibrant fresh emerald foliage
                pix.setColor(0.10f + 0.06f * t, 0.28f + 0.10f * t, 0.08f + 0.03f * t, 1f);
            } else {
                // Deep shaded jade interior
                pix.setColor(0.06f + 0.04f * t, 0.18f + 0.06f * t, 0.05f + 0.02f * t, 1f);
            }

            for (int ox = -size; ox <= size; ox += size) {
                for (int oy = -size; oy <= size; oy += size) {
                    pix.fillCircle(x + ox, y + oy, r);
                    if (MathUtils.random() < 0.12f) {
                        pix.setColor(0.24f, 0.52f, 0.15f, 0.90f);
                        pix.drawPixel(x + ox, y + oy);
                    }
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
        // Color bleed transparent background with matching foliage green to eliminate black halos
        pix.setColor(0.18f, 0.42f, 0.16f, 0f);
        pix.fill();
        final float up = -MathUtils.HALF_PI; // pixmap y points down, so "up" is -90 degrees

        // Cell 0: broad tropical leaf (Samanea saman rain tree / banyan leaf)
        drawLeaf(pix, 0, 0, 128, 244, 226, 74, up,
            0.12f, 0.34f, 0.14f, 0.28f, 0.60f, 0.20f);

        // Cell 1: fan of three spreading foliage leaves in lush summer greens
        drawLeaf(pix, cell, 0, 128, 246, 168, 48, up - 0.58f, 0.16f, 0.42f, 0.18f, 0.38f, 0.70f, 0.24f);
        drawLeaf(pix, cell, 0, 128, 246, 168, 48, up + 0.58f, 0.12f, 0.32f, 0.14f, 0.46f, 0.76f, 0.26f);
        drawLeaf(pix, cell, 0, 128, 246, 214, 56, up, 0.16f, 0.42f, 0.18f, 0.28f, 0.58f, 0.20f);

        // Cell 2: feathery compound frond with central rachis and pairs of leaflets
        int ox = 0, oy = cell;
        for (int i = 0; i < 15; i++) {
            float t = 0.10f + i * 0.058f;
            float ry = 246f - t * 232f;
            float rx = 128f + MathUtils.sin(t * 2.2f) * 10f;
            float len = 78f * (1f - 0.45f * t);
            float hw = 14f * (1f - 0.25f * t);
            drawLeaf(pix, ox, oy, rx, ry, len, hw, up - 1.05f, 0.14f, 0.36f, 0.15f, 0.34f, 0.66f, 0.22f);
            drawLeaf(pix, ox, oy, rx, ry, len, hw, up + 1.05f, 0.14f, 0.36f, 0.15f, 0.34f, 0.66f, 0.22f);
        }
        drawLeaf(pix, ox, oy, 128, 250, 236, 4, up, 0.18f, 0.34f, 0.12f, 0.28f, 0.48f, 0.16f);

        // Cell 3: blazing scarlet Krishnachura blossom clusters over feathery leaflets
        ox = cell;
        oy = cell;
        drawLeaf(pix, ox, oy, 128, 250, 160, 42, up - 0.55f, 0.14f, 0.36f, 0.15f, 0.32f, 0.64f, 0.19f);
        drawLeaf(pix, ox, oy, 128, 250, 160, 42, up + 0.55f, 0.14f, 0.36f, 0.15f, 0.32f, 0.64f, 0.19f);
        drawLeaf(pix, ox, oy, 128, 250, 190, 36, up, 0.16f, 0.42f, 0.18f, 0.38f, 0.70f, 0.22f);

        float[][] flowers = {
            {92, 96}, {168, 86}, {124, 140}, {70, 168}, {180, 158}, {132, 62},
            {65, 80}, {195, 110}, {145, 185}, {90, 205}, {165, 215}, {110, 105}
        };
        for (float[] f : flowers) {
            for (int p = 0; p < 5; p++) {
                float ang = p * MathUtils.PI2 / 5f + f[0] * 0.15f;
                drawLeaf(pix, ox, oy, f[0], f[1], 44f, 17f, ang, 0.92f, 0.12f, 0.08f, 0.99f, 0.28f, 0.10f);
            }
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
     * 2x2 atlas of hanging bamboo leaf sprays (each cell 256px, hung from the top-centre):
     * Strictly calibrated to the Bamboo reference photos (Ref 4 & 5):
     * Slender lanceolate leaf blades with clear pale green midrib, subtle micro-veins,
     * delicate drooping posture, and color bleeding background to eliminate dark borders.
     */
    private Texture createBambooAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        // Color bleed transparent background with matching bamboo green
        pix.setColor(0.24f, 0.52f, 0.18f, 0f);
        pix.fill();
        final float down = MathUtils.HALF_PI; // pixmap y points down

        // Palette RGB constants calibrated to Ref 4 & 5:
        final float c0R = 0.12f, c0G = 0.28f, c0B = 0.14f; // deep forest bamboo
        final float c1R = 0.18f, c1G = 0.38f, c1B = 0.16f; // natural bamboo green
        final float c2R = 0.26f, c2G = 0.50f, c2B = 0.20f; // vibrant mid green
        final float c3R = 0.38f, c3G = 0.62f, c3B = 0.24f; // fresh shoot green
        final float c4R = 0.52f, c4G = 0.74f, c4B = 0.28f; // sunlit golden-green tip

        // In each cell, draw delicate central branchlet twig and drooping lanceolate blades:
        // Cell 0: Full drooping bamboo spray with 9 lanceolate blades
        drawBambooTwig(pix, 0, 0, 128, 6, 128, 55);
        float[] fan0 = {-1.05f, -0.75f, -0.45f, -0.20f, 0f, 0.20f, 0.45f, 0.75f, 1.05f};
        float[] len0 = {155f, 180f, 210f, 225f, 235f, 225f, 210f, 180f, 155f};
        for (int i = 0; i < fan0.length; i++) {
            float sy = 12f + i * 4.5f;
            drawBambooLeaf(pix, 0, 0, 128, sy, len0[i], 9.5f, down + fan0[i],
                c1R, c1G, c1B, (i % 2 == 0) ? c4R : c3R, (i % 2 == 0) ? c4G : c3G, (i % 2 == 0) ? c4B : c3B);
        }

        // Cell 1: Airy graceful spray with 7 slender arching blades
        drawBambooTwig(pix, cell, 0, 128, 6, 128, 65);
        float[] fan1 = {-0.85f, -0.55f, -0.25f, 0.05f, 0.35f, 0.65f, 0.95f};
        float[] len1 = {175f, 205f, 225f, 235f, 220f, 195f, 165f};
        for (int i = 0; i < fan1.length; i++) {
            float sy = 14f + i * 5.5f;
            drawBambooLeaf(pix, cell, 0, 128, sy, len1[i], 8.5f, down + fan1[i],
                c1R, c1G, c1B, c3R, c3G, c3B);
        }

        // Cell 2: Young vibrant emerald bamboo sprig of 5 drooping blades
        drawBambooTwig(pix, 0, cell, 128, 6, 128, 48);
        float[] fan2 = {-0.60f, -0.30f, 0f, 0.30f, 0.60f};
        float[] len2 = {145f, 185f, 215f, 185f, 145f};
        for (int i = 0; i < fan2.length; i++) {
            float sy = 15f + i * 6f;
            drawBambooLeaf(pix, 0, cell, 128, sy, len2[i], 10.5f, down + fan2[i],
                c0R, c0G, c0B, c2R, c2G, c2B);
        }

        // Cell 3: Sun-dappled spray with 8 lanceolate blades and golden-green highlights
        drawBambooTwig(pix, cell, cell, 128, 6, 128, 58);
        float[] fan3 = {-0.95f, -0.68f, -0.40f, -0.12f, 0.15f, 0.42f, 0.70f, 0.98f};
        for (int i = 0; i < fan3.length; i++) {
            float sy = 12f + i * 5f;
            boolean highlight = (i % 3 == 1);
            drawBambooLeaf(pix, cell, cell, 128, sy, 200f - Math.abs(fan3[i]) * 45f, 9.0f, down + fan3[i],
                c1R, c1G, c1B, highlight ? c4R : c3R, highlight ? c4G : c3G, highlight ? c4B : c3B);
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    private void drawBambooTwig(Pixmap pix, int ox, int oy, int x0, int y0, int x1, int y1) {
        pix.setColor(0.38f, 0.32f, 0.16f, 1f); // delicate bamboo twig brown
        pix.drawLine(ox + x0, oy + y0, ox + x1, oy + y1);
        pix.drawLine(ox + x0 + 1, oy + y0, ox + x1 + 1, oy + y1);
    }

    private void drawBambooLeaf(Pixmap pix, int ox, int oy, float bx, float by, float length, float maxHalfWidth,
                                float angle, float baseR, float baseG, float baseB, float tipR, float tipG, float tipB) {
        float dx = MathUtils.cos(angle), dy = MathUtils.sin(angle);
        float px = -dy, py = dx;

        float reach = length + maxHalfWidth + 2f;
        int minX = Math.max(0, (int) (bx - reach)), maxX = Math.min(255, (int) (bx + reach));
        int minY = Math.max(0, (int) (by - reach)), maxY = Math.min(255, (int) (by + reach));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float rx = x + 0.5f - bx, ry = y + 0.5f - by;
                float along = rx * dx + ry * dy;
                float t = along / length;
                if (t < 0f || t > 1f) continue;
                float across = Math.abs(rx * px + ry * py);
                // Slender lanceolate profile: widest at t ~ 0.28, tapering gradually to needle tip at t = 1.0
                float profile = (float) Math.sin(t * Math.PI) * (1.15f - 0.40f * t);
                float w = maxHalfWidth * profile;
                if (across > w + 0.5f) continue;

                float alpha = MathUtils.clamp(w - across + 0.5f, 0f, 1f);
                float edge = w > 0.5f ? across / w : 1f;

                // Color gradient along blade length
                float r = MathUtils.lerp(baseR, tipR, t);
                float g = MathUtils.lerp(baseG, tipG, t);
                float b = MathUtils.lerp(baseB, tipB, t);

                // Pale bright green central midrib (pale yellowish-green, characteristic of bamboo in Ref 4 & 5)
                float midrib = (across < 1.3f) ? 0.08f : 0f;
                // Longitudinal fine parallel venation (parallel micro-striations)
                float fineVein = (MathUtils.sin(across * 3.8f) > 0.6f) ? 0.02f : 0f;

                pix.setColor(MathUtils.clamp(r + midrib + fineVein, 0f, 1f),
                             MathUtils.clamp(g + midrib * 1.3f + fineVein, 0f, 1f),
                             MathUtils.clamp(b + midrib * 0.4f, 0f, 1f), alpha);
                pix.drawPixel(ox + x, oy + y);
            }
        }
    }

    /**
     * 2x2 atlas of natural volumetric 3D grass tufts (each cell 256px):
     * Multi-blade organic clusters for planting around tree trunks, buttress roots, and lawn glades.
     * Calibrated to under-tree turf in reference image (Ref 2).
     */
    private Texture createGrassTuftAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        // Color bleed transparent background with lush monsoon grass green
        pix.setColor(0.20f, 0.45f, 0.18f, 0f);
        pix.fill();

        // 4 cells of diverse 3D grass tufts:
        // Cell 0: Dense lush upright and arching lawn grass tuft (matching Ref 2 under-tree turf)
        drawGrassTuft(pix, 0, 0, 16, 220f, 6.5f, 0.85f);

        // Cell 1: Spreading meadow grass cluster with gracefully drooping outer blades
        drawGrassTuft(pix, cell, 0, 18, 200f, 5.8f, 1.15f);

        // Cell 2: Clump of fresh young monsoon grass blades
        drawGrassTuft(pix, 0, cell, 14, 185f, 6.0f, 0.70f);

        // Cell 3: Tall wild grass tuft for buttress roots and grove undergrowth
        drawGrassTuft(pix, cell, cell, 20, 235f, 6.2f, 0.95f);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    private void drawGrassTuft(Pixmap pix, int ox, int oy, int bladeCount, float maxHeight, float maxHalfW, float spread) {
        float bx = 128f;
        float by = 250f; // base at bottom of cell

        for (int b = 0; b < bladeCount; b++) {
            float frac = (float) b / (float) (bladeCount - 1);
            float angleOffset = (frac - 0.5f) * spread;
            float angle = -MathUtils.HALF_PI + angleOffset;
            float h = maxHeight * (0.75f + 0.30f * MathUtils.sin(b * 2.3f));
            float hw = maxHalfW * (0.80f + 0.35f * MathUtils.cos(b * 1.7f));
            float curvature = angleOffset * 0.45f;

            drawCurvedGrassBlade(pix, ox, oy, bx + (frac - 0.5f) * 22f, by, h, hw, angle, curvature);
        }
    }

    private void drawCurvedGrassBlade(Pixmap pix, int ox, int oy, float bx, float by, float length, float maxHalfW, float startAngle, float curve) {
        int steps = (int) (length * 0.8f);
        float curX = bx;
        float curY = by;
        float angle = startAngle;

        for (int s = 0; s <= steps; s++) {
            float t = (float) s / (float) steps;
            angle += curve / (float) steps;
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);
            float perpX = -dirY;
            float perpY = dirX;

            float w = maxHalfW * (1.0f - t * 0.92f); // tapers to sharp tip at top
            float r = MathUtils.lerp(0.11f, 0.46f, t);
            float g = MathUtils.lerp(0.25f, 0.70f, t);
            float b = MathUtils.lerp(0.10f, 0.22f, t);

            int span = (int) Math.ceil(w);
            for (int d = -span; d <= span; d++) {
                if (Math.abs(d) <= w) {
                    int px = Math.round(curX + perpX * d);
                    int py = Math.round(curY + perpY * d);
                    if (px >= 0 && px < 256 && py >= 0 && py < 256) {
                        float alpha = MathUtils.clamp(w - Math.abs(d) + 0.5f, 0f, 1f);
                        float highlight = (Math.abs(d) < 1.0f) ? 0.05f : 0f;
                        pix.setColor(r + highlight, g + highlight * 1.2f, b, alpha);
                        pix.drawPixel(ox + px, oy + py);
                    }
                }
            }
            curX += dirX * (length / steps);
            curY += dirY * (length / steps);
        }
    }

    /**
     * 2x2 atlas of authentic "jira jira pata" cards (each cell 256px):
     * Pure feathery bipinnate compound foliage (like Krishnachura / Delonix regia) requested by user:
     * Cell 0: Main upright feathery bipinnate frond with 22 pairs of delicate micro-leaflets
     * Cell 1: Twin arching feathery fronds with spreading micro-leaflets and sunlit tips
     * Cell 2: Graceful sweeping lateral feathery frond with 24 pairs of micro-leaflets
     * Cell 3: Dense radiating feathery foliage spray with 3 layered bipinnate fronds
     */
    private Texture createWhiteBlossomAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        // Color bleed transparent background with foliage green
        pix.setColor(0.18f, 0.44f, 0.16f, 0f);
        pix.fill();

        // Cell 0: Main upright feathery bipinnate frond ("jira jira pata")
        drawFeatheryBipinnateBranch(pix, 0, 0, 0f, 0f, 22);

        // Cell 1: Twin fanning feathery fronds with delicate micro-leaflets
        drawFeatheryBipinnateBranch(pix, cell, 0, -0.26f, -0.5f, 19);
        drawFeatheryBipinnateBranch(pix, cell, 0, 0.26f, 0.5f, 19);

        // Cell 2: Arching feathery compound branch with 24 pairs of micro-leaflets
        drawFeatheryBipinnateBranch(pix, 0, cell, 0.10f, 0.9f, 24);

        // Cell 3: Dense feathery foliage spray with 3 radiating bipinnate fronds
        drawFeatheryBipinnateBranch(pix, cell, cell, -0.32f, -0.4f, 18);
        drawFeatheryBipinnateBranch(pix, cell, cell, 0.05f, 0.2f, 20);
        drawFeatheryBipinnateBranch(pix, cell, cell, 0.34f, 0.5f, 18);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * Draws authentic feathery bipinnate foliage ("jira jira pata"):
     * Central slender rachis stem flanked by numerous pairs of delicate micro-leaflets
     * in rich chlorophyll greens and warm sunlit tips, exactly matching the red Krishnachura tree.
     */
    private void drawFeatheryBipinnateBranch(Pixmap pix, int ox, int oy, float angle, float curve, int pairs) {
        final float up = -MathUtils.HALF_PI + angle;
        float baseR = 0.08f, baseG = 0.24f, baseB = 0.07f;
        float tipR = 0.18f, tipG = 0.42f, tipB = 0.11f;

        // Pairs of delicate bipinnate leaflets ("jira jira pata")
        for (int i = 0; i < pairs; i++) {
            float t = 0.06f + (float) i / (float) pairs * 0.90f;
            float ry = 248f - t * 234f;
            float rx = 128f + MathUtils.sin(t * 2.5f + curve) * 22f;
            float len = 78f * (1f - 0.44f * t);
            float hw = 13f * (1f - 0.22f * t);

            // Alternate slight color variety for rich organic foliage depth
            float var = (i % 2 == 0) ? 0f : 0.03f;
            drawLeaf(pix, ox, oy, rx, ry, len, hw, up - 1.05f,
                baseR + var, baseG + var, baseB, tipR + var * 1.5f, tipG + var * 1.5f, tipB);
            drawLeaf(pix, ox, oy, rx, ry, len, hw, up + 1.05f,
                baseR + var, baseG + var, baseB, tipR + var * 1.5f, tipG + var * 1.5f, tipB);

            // Secondary micro-leaflets near outer edges for lush fern-like feathery silhouette
            if (i > 3 && i < pairs - 4 && i % 2 == 0) {
                float subLen = len * 0.48f;
                float subHw = hw * 0.75f;
                float srxL = rx + MathUtils.cos(up - 1.05f) * (len * 0.5f);
                float sryL = ry + MathUtils.sin(up - 1.05f) * (len * 0.5f);
                drawLeaf(pix, ox, oy, srxL, sryL, subLen, subHw, up - 0.55f, baseR, baseG, baseB, tipR, tipG, tipB);

                float srxR = rx + MathUtils.cos(up + 1.05f) * (len * 0.5f);
                float sryR = ry + MathUtils.sin(up + 1.05f) * (len * 0.5f);
                drawLeaf(pix, ox, oy, srxR, sryR, subLen, subHw, up + 0.55f, baseR, baseG, baseB, tipR, tipG, tipB);
            }
        }

        // Main rachis stem (slender woody stalk)
        drawLeaf(pix, ox, oy, 128, 250, 240, 4, up, 0.16f, 0.22f, 0.08f, 0.22f, 0.34f, 0.11f);
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
     * 2x2 atlas of floating lotus pads (water lilies) and sacred lotus blossoms (Nelumbo nucifera)
     * matching reference image media_1791358203326.png:
     * Cell 0: Large round peltate lotus pad with radial veins, notched sinus, and water drops
     * Cell 1: Blooming white & pink sacred lotus blossom with golden receptacle
     * Cell 2: Open lotus pad with gentle cup curvature and central dewdrop
     * Cell 3: Water lily pad with pink blossom bud
     */
    private Texture createLotusAtlas() {
        int cell = 256;
        Pixmap pix = new Pixmap(cell * 2, cell * 2, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill();

        // Cell 0: Large round peltate lotus pad
        drawLotusPad(pix, 0, 0, 110, false);

        // Cell 1: Blooming sacred lotus flower (white petals with soft pink tips and golden core)
        drawLotusBlossom(pix, cell, 0);

        // Cell 2: Second lotus pad variant with prominent radial veins
        drawLotusPad(pix, 0, cell, 115, true);

        // Cell 3: Water lily pad with pink blossom bud
        drawLotusPad(pix, cell, cell, 95, false);
        drawLotusBud(pix, cell + 165, cell + 95);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    private void drawLotusPad(Pixmap pix, int ox, int oy, int rad, boolean extraVeins) {
        int cx = ox + 128;
        int cy = oy + 128;

        // Base peltate disk (deep forest jade-green, matching reference image)
        for (int r = rad; r >= 1; r--) {
            float t = (float) r / (float) rad;
            // Center is natural olive-jade, outer rim is rich deep forest jade
            float red = MathUtils.lerp(0.12f, 0.06f, t);
            float green = MathUtils.lerp(0.28f, 0.18f, t);
            float blue = MathUtils.lerp(0.10f, 0.06f, t);
            pix.setColor(red, green, blue, 1f);
            pix.fillCircle(cx, cy, r);
        }

        // Distinctive V-shaped leaf notch (sinus) from center to outer rim
        pix.setColor(0f, 0f, 0f, 0f);
        float notchAng = -0.45f;
        for (int d = 0; d <= rad + 2; d++) {
            float nx1 = cx + MathUtils.cos(notchAng) * d;
            float ny1 = cy + MathUtils.sin(notchAng) * d;
            float nx2 = cx + MathUtils.cos(notchAng + 0.18f) * d;
            float ny2 = cy + MathUtils.sin(notchAng + 0.18f) * d;
            pix.drawLine((int)nx1, (int)ny1, (int)nx2, (int)ny2);
        }

        // Radiating delicate leaf veins
        pix.setColor(0.14f, 0.32f, 0.11f, 0.70f);
        int veinCount = extraVeins ? 16 : 12;
        for (int v = 0; v < veinCount; v++) {
            float va = v * MathUtils.PI2 / (float) veinCount;
            if (Math.abs(va - notchAng) < 0.35f) continue; // skip notch
            float vx = cx + MathUtils.cos(va) * (rad - 4);
            float vy = cy + MathUtils.sin(va) * (rad - 4);
            pix.drawLine(cx, cy, (int)vx, (int)vy);
        }

        // Natural central petiole hub
        pix.setColor(0.22f, 0.38f, 0.15f, 1f);
        pix.fillCircle(cx, cy, 5);
        pix.setColor(0.30f, 0.48f, 0.20f, 1f);
        pix.fillCircle(cx, cy, 2);

        // Glistening water dewdrops
        int drops = extraVeins ? 4 : 3;
        for (int d = 0; d < drops; d++) {
            int dx = cx + (int)(MathUtils.cos(d * 1.9f + 0.5f) * (rad * 0.45f));
            int dy = cy + (int)(MathUtils.sin(d * 1.9f + 0.5f) * (rad * 0.45f));
            pix.setColor(0.70f, 0.88f, 0.95f, 0.75f);
            pix.fillCircle(dx, dy, 4);
            pix.setColor(1.0f, 1.0f, 1.0f, 0.95f);
            pix.drawPixel(dx - 1, dy - 1);
        }
    }

    private void drawLotusBlossom(Pixmap pix, int ox, int oy) {
        int cx = ox + 128;
        int cy = oy + 128;

        // Outer petals (pure ivory with soft magenta-pink tips, sacred lotus style)
        int petalCount = 14;
        for (int p = 0; p < petalCount; p++) {
            float ang = p * MathUtils.PI2 / (float) petalCount;
            float px = cx + MathUtils.cos(ang) * 52f;
            float py = cy + MathUtils.sin(ang) * 52f;

            // Base white
            pix.setColor(0.98f, 0.96f, 0.96f, 0.95f);
            pix.fillCircle((int)px, (int)py, 22);

            // Soft pink tip
            float tipX = cx + MathUtils.cos(ang) * 64f;
            float tipY = cy + MathUtils.sin(ang) * 64f;
            pix.setColor(0.95f, 0.65f, 0.78f, 0.85f);
            pix.fillCircle((int)tipX, (int)tipY, 12);
        }

        // Inner petal ring
        for (int p = 0; p < 10; p++) {
            float ang = p * MathUtils.PI2 / 10f + 0.25f;
            float px = cx + MathUtils.cos(ang) * 32f;
            float py = cy + MathUtils.sin(ang) * 32f;
            pix.setColor(1.0f, 0.98f, 0.98f, 0.98f);
            pix.fillCircle((int)px, (int)py, 16);
            pix.setColor(0.98f, 0.72f, 0.82f, 0.80f);
            pix.fillCircle((int)(cx + MathUtils.cos(ang) * 40f), (int)(cy + MathUtils.sin(ang) * 40f), 8);
        }

        // Golden center: flat-topped floral receptacle (torus)
        pix.setColor(1.0f, 0.85f, 0.18f, 1f);
        pix.fillCircle(cx, cy, 18);
        pix.setColor(0.92f, 0.72f, 0.12f, 1f);
        pix.drawCircle(cx, cy, 18);

        // Ring of golden yellow stamens
        for (int s = 0; s < 18; s++) {
            float sa = s * MathUtils.PI2 / 18f;
            int sx = cx + (int)(MathUtils.cos(sa) * 14f);
            int sy = cy + (int)(MathUtils.sin(sa) * 14f);
            pix.setColor(1.0f, 0.95f, 0.45f, 1f);
            pix.fillCircle(sx, sy, 2);
        }
    }

    private void drawLotusBud(Pixmap pix, int bx, int by) {
        // Deep pink pointed bud
        pix.setColor(0.92f, 0.55f, 0.72f, 1f);
        pix.fillCircle(bx, by, 14);
        pix.setColor(0.98f, 0.75f, 0.85f, 1f);
        pix.fillCircle(bx, by - 4, 8);
        // Green basal sepals
        pix.setColor(0.18f, 0.42f, 0.14f, 1f);
        pix.fillCircle(bx - 6, by + 8, 6);
        pix.fillCircle(bx + 6, by + 8, 6);
    }

    /**
     * Authentic British-Mughal colonial arched window texture (512x1024 with trilinear mipmaps):
     * Multi-pane glazed window with pointed arch stone surround, central keystone, radial fanlight,
     * deep reflective blue-grey glass with sky reflection, and carved stone sill.
     */
    /**
     * Authentic Curzon Hall Indo-Saracenic Pointed Cusped Arch Window (512x1024 with trilinear mipmaps):
     * - Multi-cusped pointed arch head with radiating fanlight tracery
     * - Carved sandstone voussoirs and keystone at arch apex
     * - Carved circular rosette medallions in terracotta spandrels
     * - 8-pane colonial glazed window with clean white timber mullions and reflective glass
     * - Projecting carved sandstone sill supported by twin corbel brackets
     */
    /**
     * Authentic Curzon Hall Indo-Saracenic Pointed Arch Colonial Window (512x1024 with trilinear mipmaps):
     * - Smooth 2-centered pointed Mughal arch (NO jagged sawtooth cusps)
     * - Transparent spandrels (alpha=0) allowing Curzon Hall's red brickwork to show through seamlessly
     * - Sandstone arch surround with radiating voussoirs and carved apex keystone
     * - Deep reflective blue-grey architectural glass with subtle sky reflection
     * - Graceful radiating fanlight tracery in arch head
     * - Classic 8-pane colonial sash window with crisp cream timber mullions
     * - Projecting carved sandstone sill with twin corbel brackets
     */
    private Texture createColonialWindow() {
        int w = 512, h = 1024;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // 100% transparent base

        Color stoneBase = new Color(0.80f, 0.74f, 0.65f, 1f);
        Color stoneHi = new Color(0.88f, 0.82f, 0.74f, 1f);
        Color stoneSh = new Color(0.55f, 0.48f, 0.40f, 1f);
        Color mullion = new Color(0.90f, 0.88f, 0.82f, 1f);
        Color mullionSh = new Color(0.48f, 0.45f, 0.40f, 1f);

        int cx = w / 2;
        int x0 = 54, x1 = w - 54;
        float hw = (x1 - x0) / 2.0f;
        int sy = 420;
        int ay = 95;
        float rise = sy - ay;
        float d = (rise * rise - hw * hw) / (2.0f * hw);
        float R = hw + d;
        int archTh = 30;
        int frameBot = 930;

        // 1. Lower Window (below springline): vertical stone jambs and deep reflective glass
        for (int y = sy; y < frameBot; y++) {
            float t = (float)(y - sy) / (float)(frameBot - sy);
            pix.setColor(MathUtils.lerp(0.12f, 0.06f, t), MathUtils.lerp(0.16f, 0.08f, t), MathUtils.lerp(0.24f, 0.12f, t), 1f);
            pix.fillRectangle(x0, y, x1 - x0 + 1, 1);

            // Left stone jamb
            pix.setColor(stoneBase);
            pix.fillRectangle(x0 - archTh, y, archTh, 1);
            pix.setColor(stoneHi);
            pix.drawPixel(x0 - archTh, y);
            pix.setColor(stoneSh);
            pix.drawPixel(x0 - 1, y);

            // Right stone jamb
            pix.setColor(stoneBase);
            pix.fillRectangle(x1 + 1, y, archTh, 1);
            pix.setColor(stoneSh);
            pix.drawPixel(x1 + archTh, y);
            pix.setColor(stoneHi);
            pix.drawPixel(x1 + 1, y);
        }

        // 2. Smooth Pointed Arch Head (NO jagged sawtooth cusps)
        for (int y = ay; y < sy; y++) {
            float dy = sy - y;
            float radSqIn = R * R - dy * dy;
            float radSqOut = (R + archTh) * (R + archTh) - dy * dy;
            if (radSqIn > 0f) {
                float rxIn = (float) Math.sqrt(radSqIn);
                int xlIn = (int)((cx + d) - rxIn);
                int xrIn = (int)((cx - d) + rxIn);

                // Deep reflective glass in arch head
                float tg = (float)(y - ay) / rise;
                pix.setColor(MathUtils.lerp(0.14f, 0.08f, tg), MathUtils.lerp(0.20f, 0.11f, tg), MathUtils.lerp(0.30f, 0.15f, tg), 1f);
                if (xrIn >= xlIn) {
                    pix.fillRectangle(xlIn, y, xrIn - xlIn + 1, 1);
                }

                if (radSqOut > 0f) {
                    float rxOut = (float) Math.sqrt(radSqOut);
                    int xlOut = (int)((cx + d) - rxOut);
                    int xrOut = (int)((cx - d) + rxOut);

                    // Stone arch voussoirs with subtle masonry radial joints
                    float angL = MathUtils.atan2(dy, Math.abs(xlIn - cx));
                    float jointL = MathUtils.sin(angL * 14f);
                    float varL = (jointL > 0.94f) ? -0.15f : 0.0f;
                    pix.setColor(MathUtils.clamp(stoneBase.r + varL, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.g + varL, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.b + varL, 0.5f, 1f), 1f);
                    if (xlIn > xlOut) pix.fillRectangle(xlOut, y, xlIn - xlOut, 1);

                    float angR = MathUtils.atan2(dy, Math.abs(xrIn - cx));
                    float jointR = MathUtils.sin(angR * 14f);
                    float varR = (jointR > 0.94f) ? -0.15f : 0.0f;
                    pix.setColor(MathUtils.clamp(stoneBase.r + varR, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.g + varR, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.b + varR, 0.5f, 1f), 1f);
                    if (xrOut > xrIn) pix.fillRectangle(xrIn + 1, y, xrOut - xrIn, 1);

                    // Inner and outer contour lines
                    pix.setColor(stoneSh);
                    pix.drawPixel(xlIn, y);
                    pix.drawPixel(xrIn, y);
                    pix.setColor(stoneHi);
                    pix.drawPixel(xlOut, y);
                    pix.drawPixel(xrOut, y);
                }
            }
        }

        // 3. Carved Sandstone Keystone at arch apex
        int kw = 44, kh = 48;
        pix.setColor(stoneBase);
        pix.fillRectangle(cx - kw / 2, ay - 24, kw, kh);
        pix.setColor(stoneHi);
        pix.drawRectangle(cx - kw / 2, ay - 24, kw, kh);

        // 4. Horizontal Transom Bar at springline
        pix.setColor(stoneBase);
        pix.fillRectangle(x0, sy - 6, x1 - x0 + 1, 12);
        pix.setColor(stoneHi);
        pix.drawLine(x0, sy - 6, x1, sy - 6);
        pix.setColor(stoneSh);
        pix.drawLine(x0, sy + 5, x1, sy + 5);

        // 5. Radiating Fanlight Tracery in arch head
        for (int spoke = 0; spoke < 7; spoke++) {
            float angle = MathUtils.PI * (0.15f + 0.70f * ((float) spoke / 6f));
            float cosA = MathUtils.cos(angle);
            float sinA = MathUtils.sin(angle);
            for (int rad = 14; rad < 240; rad++) {
                int fx = cx - (int)(cosA * rad);
                int fy = sy - (int)(sinA * rad);
                if (fy < ay || fy >= sy || fx < x0 || fx > x1) continue;
                float dyF = sy - fy;
                float radSqF = R * R - dyF * dyF;
                if (radSqF > 0f) {
                    float rxF = (float) Math.sqrt(radSqF);
                    int xlLim = (int)((cx + d) - rxF);
                    int xrLim = (int)((cx - d) + rxF);
                    if (fx > xlLim + 2 && fx < xrLim - 2) {
                        pix.setColor(mullion);
                        pix.drawPixel(fx, fy);
                    }
                }
            }
        }

        // Concentric inner fanlight arc
        float rInnerFan = R * 0.55f;
        for (int fx = x0; fx <= x1; fx++) {
            float dx = fx - cx;
            float fyF = sy - (float) Math.sqrt(Math.max(0f, rInnerFan * rInnerFan - dx * dx));
            int fy = (int) fyF;
            if (fy >= ay && fy < sy - 10) {
                pix.setColor(mullion);
                pix.drawPixel(fx, fy);
                pix.drawPixel(fx, fy + 1);
            }
        }

        // 6. Lower 8-Pane Colonial Window Sash Grid
        // Central vertical mullion
        pix.setColor(mullion);
        pix.fillRectangle(cx - 3, sy + 6, 6, frameBot - (sy + 6));
        pix.setColor(stoneHi);
        pix.drawLine(cx - 3, sy + 6, cx - 3, frameBot);
        pix.setColor(mullionSh);
        pix.drawLine(cx + 3, sy + 6, cx + 3, frameBot);

        // 3 horizontal transoms
        int lowerH = frameBot - (sy + 6);
        for (int row = 1; row <= 3; row++) {
            int hy = sy + 6 + (lowerH * row) / 4;
            pix.setColor(mullion);
            pix.fillRectangle(x0, hy - 3, x1 - x0 + 1, 6);
            pix.setColor(stoneHi);
            pix.drawLine(x0, hy - 3, x1, hy - 3);
            pix.setColor(mullionSh);
            pix.drawLine(x0, hy + 3, x1, hy + 3);
        }

        // 7. Projecting Sandstone Sill & Twin Corbel Brackets
        pix.setColor(stoneBase);
        pix.fillRectangle(x0 - archTh - 12, frameBot, (x1 - x0) + (archTh + 12) * 2, 22);
        pix.setColor(stoneHi);
        pix.drawLine(x0 - archTh - 12, frameBot, x1 + archTh + 12, frameBot);
        pix.setColor(stoneSh);
        pix.drawLine(x0 - archTh - 12, frameBot + 21, x1 + archTh + 12, frameBot + 21);

        int[] brackets = {x0 + 16, x1 - 44};
        for (int bx : brackets) {
            for (int by = frameBot + 22; by <= frameBot + 48; by++) {
                float prog = (float)(by - (frameBot + 22)) / 26f;
                int curW = (int)(28f * (1f - prog * 0.6f));
                pix.setColor(stoneBase);
                pix.fillRectangle(bx, by, curW, 1);
                pix.setColor(stoneSh);
                pix.drawPixel(bx + curW - 1, by);
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * Authentic Curzon Hall Ground Floor Pointed Arched Doorway (512x1024 with trilinear mipmaps):
     * - Transparent spandrels (alpha=0) seamlessly integrating with Curzon red brickwork
     * - Smooth 2-centered pointed Mughal arch surround with cream sandstone voussoirs
     * - Wrought-iron Arabesque lattice / jali fanlight in arch head with deep interior cavity
     * - Carved Burma teak double doors with rich teak grain, bevelled panels, and central rosette medallions
     * - Heavy iron strap hinges, burnished brass studs, and lion-head knockers
     * - Sandstone threshold at base
     */
    private Texture createTeakDoorTexture() {
        int w = 512, h = 1024;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // 100% transparent base

        Color stoneBase = new Color(0.90f, 0.86f, 0.78f, 1f);
        Color stoneHi = new Color(0.98f, 0.96f, 0.90f, 1f);
        Color stoneSh = new Color(0.60f, 0.55f, 0.48f, 1f);

        int cx = w / 2;
        int x0 = 54, x1 = w - 54;
        float hw = (x1 - x0) / 2.0f;
        int sy = 430;
        int ay = 90;
        float rise = sy - ay;
        float d = (rise * rise - hw * hw) / (2.0f * hw);
        float R = hw + d;
        int archTh = 36;
        int frameBot = 970;

        // 1. Lower section: Carved Burma Teak Doors
        for (int y = sy + 8; y < frameBot; y++) {
            // Teak base with rich organic grain
            for (int x = x0; x <= x1; x++) {
                float grain = MathUtils.sin(x * 0.45f) * 0.5f + MathUtils.sin(x * 1.8f) * 0.3f;
                float n = grain + MathUtils.sin(y * 0.08f + x * 0.05f) * 0.2f;
                float r = MathUtils.clamp(0.28f + n * 0.05f, 0.18f, 0.38f);
                float g = MathUtils.clamp(0.15f + n * 0.03f, 0.09f, 0.22f);
                float b = MathUtils.clamp(0.08f + n * 0.02f, 0.04f, 0.14f);
                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
            // Left stone jamb
            pix.setColor(stoneBase);
            pix.fillRectangle(x0 - archTh, y, archTh, 1);
            pix.setColor(stoneHi);
            pix.drawPixel(x0 - archTh, y);
            pix.setColor(stoneSh);
            pix.drawPixel(x0 - 1, y);

            // Right stone jamb
            pix.setColor(stoneBase);
            pix.fillRectangle(x1 + 1, y, archTh, 1);
            pix.setColor(stoneSh);
            pix.drawPixel(x1 + archTh, y);
            pix.setColor(stoneHi);
            pix.drawPixel(x1 + 1, y);
        }

        // Center meeting stile between double doors
        pix.setColor(0.16f, 0.08f, 0.04f, 1f);
        pix.fillRectangle(cx - 10, sy + 8, 20, frameBot - (sy + 8));
        pix.setColor(0.08f, 0.04f, 0.02f, 1f);
        pix.drawLine(cx, sy + 8, cx, frameBot);

        // Recessed carved wood panels (3 per door leaf = 6 tall panels)
        int doorPanelW = (cx - x0) - 24;
        int usableH = frameBot - sy - 48;
        int doorPanelH = usableH / 3 - 12;
        int[] doorPanelX = {x0 + 12, cx + 12};
        for (int px : doorPanelX) {
            for (int row = 0; row < 3; row++) {
                int py = sy + 24 + row * (doorPanelH + 16);
                drawCarvedDoorPanel(pix, px, py, doorPanelW, doorPanelH);
            }
        }

        // 2. Pointed Arch Head: Smooth curve & Wrought-Iron Arabesque Tracery Fanlight
        for (int y = ay; y < sy; y++) {
            float dy = sy - y;
            float radSqIn = R * R - dy * dy;
            float radSqOut = (R + archTh) * (R + archTh) - dy * dy;
            if (radSqIn > 0f) {
                float rxIn = (float) Math.sqrt(radSqIn);
                int xlIn = (int)((cx + d) - rxIn);
                int xrIn = (int)((cx - d) + rxIn);

                // Deep atmospheric interior vestibule cavity
                float tCavity = (float)(y - ay) / rise;
                pix.setColor(MathUtils.lerp(0.12f, 0.07f, tCavity), MathUtils.lerp(0.09f, 0.05f, tCavity), MathUtils.lerp(0.06f, 0.04f, tCavity), 1f);
                if (xrIn >= xlIn) {
                    pix.fillRectangle(xlIn, y, xrIn - xlIn + 1, 1);
                }

                if (radSqOut > 0f) {
                    float rxOut = (float) Math.sqrt(radSqOut);
                    int xlOut = (int)((cx + d) - rxOut);
                    int xrOut = (int)((cx - d) + rxOut);

                    // Stone arch voussoirs
                    float ang = MathUtils.atan2(dy, Math.abs(xlIn - cx));
                    float vLine = (MathUtils.sin(ang * 14f) > 0.94f) ? -0.14f : 0.0f;
                    pix.setColor(MathUtils.clamp(stoneBase.r + vLine, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.g + vLine, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.b + vLine, 0.5f, 1f), 1f);
                    if (xlIn > xlOut) pix.fillRectangle(xlOut, y, xlIn - xlOut, 1);
                    if (xrOut > xrIn) pix.fillRectangle(xrIn + 1, y, xrOut - xrIn, 1);

                    pix.setColor(stoneSh);
                    pix.drawPixel(xlIn, y);
                    pix.drawPixel(xrIn, y);
                    pix.setColor(stoneHi);
                    pix.drawPixel(xlOut, y);
                    pix.drawPixel(xrOut, y);
                }
            }
        }

        // Heavy carved transom bar separating door from fanlight
        pix.setColor(0.18f, 0.09f, 0.05f, 1f);
        pix.fillRectangle(x0, sy - 8, x1 - x0 + 1, 16);
        pix.setColor(stoneHi);
        pix.drawLine(x0, sy - 8, x1, sy - 8);
        pix.setColor(0.08f, 0.04f, 0.02f, 1f);
        pix.drawLine(x0, sy + 7, x1, sy + 7);

        // Wrought iron arabesque lattice spokes & concentric arches in fanlight
        for (int rRing = 45; rRing <= 210; rRing += 48) {
            for (float a = 0.15f; a <= 2.99f; a += 0.035f) {
                int fx = cx - (int)(MathUtils.cos(a) * rRing);
                int fy = sy - (int)(MathUtils.sin(a) * rRing);
                if (fy < ay || fy >= sy - 8 || fx < x0 || fx > x1) continue;
                float dyF = sy - fy;
                float radSqF = R * R - dyF * dyF;
                if (radSqF > 0f) {
                    float rxF = (float) Math.sqrt(radSqF);
                    if (fx > (cx + d) - rxF + 3 && fx < (cx - d) + rxF - 3) {
                        pix.setColor(0.32f, 0.28f, 0.24f, 1f);
                        pix.drawPixel(fx, fy);
                    }
                }
            }
        }

        // Carved Keystone at arch apex
        int kw = 48, kh = 52;
        pix.setColor(stoneBase);
        pix.fillRectangle(cx - kw / 2, ay - 24, kw, kh);
        pix.setColor(stoneHi);
        pix.drawRectangle(cx - kw / 2, ay - 24, kw, kh);

        // Brass studs along door stiles
        for (int y = sy + 30; y < frameBot - 20; y += 48) {
            drawBrassStud(pix, x0 + 10, y);
            drawBrassStud(pix, x1 - 10, y);
            drawBrassStud(pix, cx - 6, y);
            drawBrassStud(pix, cx + 6, y);
        }

        // Heavy wrought iron decorative strap hinges
        drawIronHinge(pix, x0, sy + 70, 75);
        drawIronHinge(pix, x0, frameBot - 120, 75);
        drawIronHinge(pix, x1 - 75, sy + 70, 75);
        drawIronHinge(pix, x1 - 75, frameBot - 120, 75);

        // Burnished brass door handles with lion-head knockers
        drawBrassDoorHandle(pix, cx - 28, sy + (usableH / 2) + 10);
        drawBrassDoorHandle(pix, cx + 28, sy + (usableH / 2) + 10);

        // Sandstone threshold at base
        pix.setColor(stoneBase);
        pix.fillRectangle(x0 - archTh - 14, frameBot, (x1 - x0) + (archTh + 14) * 2, 24);
        pix.setColor(stoneHi);
        pix.drawLine(x0 - archTh - 14, frameBot, x1 + archTh + 14, frameBot);
        pix.setColor(stoneSh);
        pix.drawLine(x0 - archTh - 14, frameBot + 23, x1 + archTh + 14, frameBot + 23);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * Monumental Pointed Mughal Iwan Portal for Curzon Hall Grand Entrance (1024x2048 with trilinear mipmaps):
     * - Transparent spandrels (alpha=0) allowing seamless integration with Curzon brick walls
     * - Smooth, stately 2-centered pointed Mughal arch (NO pitch-black void, NO jagged sawtooth cusps)
     * - Warm, illuminated architectural grand vestibule / foyer with soft ambient chandelier glow
     * - Intricate wrought-iron and burnished brass Arabesque grand fanlight screen
     * - Massive ceremonial Burma teak double gates with 8 deeply carved panels, brass studs, and lion knockers
     * - Refined sandstone pilaster jambs, impost mouldings, and carved floral keystone
     */
    private Texture createCurzonGrandPortal() {
        int w = 1024, h = 2048;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // 100% transparent base!

        Color stoneBase = new Color(0.80f, 0.74f, 0.65f, 1f);
        Color stoneHi = new Color(0.88f, 0.82f, 0.74f, 1f);
        Color stoneSh = new Color(0.55f, 0.48f, 0.40f, 1f);

        int cx = w / 2;
        int x0 = 140, x1 = w - 140;
        float hw = (x1 - x0) / 2.0f;
        int sy = 560;
        int ay = 160;
        float rise = sy - ay;
        float d = (rise * rise - hw * hw) / (2.0f * hw);
        float R = hw + d;
        int archTh = 38;
        int frameBot = 1960;

        // 1. Lower Ceremonial Burma Teak Double Gates (from sy + 12 to frameBot)
        for (int y = sy + 12; y < frameBot; y++) {
            // Teak base with warm rich woodgrain
            for (int x = x0; x <= x1; x++) {
                float grain = MathUtils.sin(x * 0.35f) * 0.5f + MathUtils.sin(x * 1.5f) * 0.3f;
                float n = grain + MathUtils.sin(y * 0.05f + x * 0.03f) * 0.2f;
                float r = MathUtils.clamp(0.28f + n * 0.04f, 0.18f, 0.38f);
                float g = MathUtils.clamp(0.15f + n * 0.03f, 0.09f, 0.22f);
                float b = MathUtils.clamp(0.08f + n * 0.02f, 0.05f, 0.14f);
                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
            // Left sandstone pilaster jamb
            pix.setColor(stoneBase);
            pix.fillRectangle(x0 - archTh, y, archTh, 1);
            pix.setColor(stoneHi);
            pix.drawPixel(x0 - archTh, y);
            pix.setColor(stoneSh);
            pix.drawPixel(x0 - 1, y);

            // Right sandstone pilaster jamb
            pix.setColor(stoneBase);
            pix.fillRectangle(x1 + 1, y, archTh, 1);
            pix.setColor(stoneSh);
            pix.drawPixel(x1 + archTh, y);
            pix.setColor(stoneHi);
            pix.drawPixel(x1 + 1, y);
        }

        // Center meeting stile
        pix.setColor(0.16f, 0.08f, 0.04f, 1f);
        pix.fillRectangle(cx - 16, sy + 12, 32, frameBot - (sy + 12));
        pix.setColor(0.08f, 0.04f, 0.02f, 1f);
        pix.drawLine(cx, sy + 12, cx, frameBot);

        // 8 Large deeply recessed carved panels (4 per door leaf)
        int gatePanelW = (cx - x0) - 40;
        int usableH = frameBot - sy - 50;
        int gatePanelH = usableH / 4 - 16;
        int[] gatePanelX = {x0 + 20, cx + 20};
        for (int px : gatePanelX) {
            for (int row = 0; row < 4; row++) {
                int py = sy + 24 + row * (gatePanelH + 20);
                drawCarvedDoorPanel(pix, px, py, gatePanelW, gatePanelH);
            }
        }

        // 2. Upper Arched Tympanum: Warm Architectural Grand Foyer Interior (NO BLACK HOLE!)
        for (int y = ay; y < sy; y++) {
            float dy = sy - y;
            float radSqIn = R * R - dy * dy;
            float radSqOut = (R + archTh) * (R + archTh) - dy * dy;
            if (radSqIn > 0f) {
                float rxIn = (float) Math.sqrt(radSqIn);
                int xlIn = (int)((cx + d) - rxIn);
                int xrIn = (int)((cx - d) + rxIn);

                // Warm illuminated foyer depth with soft amber ambient glow
                float tFoyer = (float)(y - ay) / rise;
                float rF = MathUtils.lerp(0.36f, 0.20f, tFoyer);
                float gF = MathUtils.lerp(0.24f, 0.12f, tFoyer);
                float bF = MathUtils.lerp(0.14f, 0.07f, tFoyer);
                pix.setColor(rF, gF, bF, 1f);
                if (xrIn >= xlIn) {
                    pix.fillRectangle(xlIn, y, xrIn - xlIn + 1, 1);
                }

                if (radSqOut > 0f) {
                    float rxOut = (float) Math.sqrt(radSqOut);
                    int xlOut = (int)((cx + d) - rxOut);
                    int xrOut = (int)((cx - d) + rxOut);

                    // Multi-tiered sandstone arch moldings with subtle joint lines
                    float ang = MathUtils.atan2(dy, Math.abs(xlIn - cx));
                    float joint = (MathUtils.sin(ang * 16f) > 0.95f) ? -0.12f : 0.0f;
                    pix.setColor(MathUtils.clamp(stoneBase.r + joint, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.g + joint, 0.5f, 1f),
                                 MathUtils.clamp(stoneBase.b + joint, 0.5f, 1f), 1f);
                    if (xlIn > xlOut) pix.fillRectangle(xlOut, y, xlIn - xlOut, 1);
                    if (xrOut > xrIn) pix.fillRectangle(xrIn + 1, y, xrOut - xrIn, 1);

                    pix.setColor(stoneSh);
                    pix.drawPixel(xlIn, y);
                    pix.drawPixel(xrIn, y);
                    pix.setColor(stoneHi);
                    pix.drawPixel(xlOut, y);
                    pix.drawPixel(xrOut, y);
                }
            }
        }

        // Heavy carved Burma teak transom beam separating gate from fanlight
        pix.setColor(0.18f, 0.09f, 0.05f, 1f);
        pix.fillRectangle(x0, sy - 12, x1 - x0 + 1, 24);
        pix.setColor(stoneHi);
        pix.drawLine(x0, sy - 12, x1, sy - 12);
        pix.setColor(0.08f, 0.04f, 0.02f, 1f);
        pix.drawLine(x0, sy + 11, x1, sy + 11);

        // Ornate wrought-iron and brass Arabesque grand fanlight tracery
        for (int rRing = 60; rRing <= 360; rRing += 60) {
            for (float a = 0.14f; a <= 3.00f; a += 0.02f) {
                int fx = cx - (int)(MathUtils.cos(a) * rRing);
                int fy = sy - (int)(MathUtils.sin(a) * rRing);
                if (fy < ay || fy >= sy - 12 || fx < x0 || fx > x1) continue;
                float dyF = sy - fy;
                float radSqF = R * R - dyF * dyF;
                if (radSqF > 0f) {
                    float rxF = (float) Math.sqrt(radSqF);
                    if (fx > (cx + d) - rxF + 3 && fx < (cx - d) + rxF - 3) {
                        pix.setColor(0.85f, 0.70f, 0.30f, 1f); // Brass accent
                        pix.drawPixel(fx, fy);
                    }
                }
            }
        }

        // Radial grand spokes in fanlight
        for (int spoke = 0; spoke < 11; spoke++) {
            float angle = MathUtils.PI * (0.12f + 0.76f * ((float) spoke / 10f));
            float cosA = MathUtils.cos(angle);
            float sinA = MathUtils.sin(angle);
            for (int rad = 20; rad < 380; rad++) {
                int fx = cx - (int)(cosA * rad);
                int fy = sy - (int)(sinA * rad);
                if (fy < ay || fy >= sy - 12 || fx < x0 || fx > x1) continue;
                float dyF = sy - fy;
                float radSqF = R * R - dyF * dyF;
                if (radSqF > 0f) {
                    float rxF = (float) Math.sqrt(radSqF);
                    if (fx > (cx + d) - rxF + 3 && fx < (cx - d) + rxF - 3) {
                        pix.setColor(0.32f, 0.28f, 0.24f, 1f);
                        pix.drawPixel(fx, fy);
                    }
                }
            }
        }

        // Hanging ceremonial brass lantern / chandelier glow in upper foyer
        int lanX = cx, lanY = ay + 170;
        pix.setColor(1.0f, 0.85f, 0.40f, 0.9f);
        pix.fillCircle(lanX, lanY, 14);
        pix.setColor(1.0f, 0.95f, 0.70f, 1f);
        pix.fillCircle(lanX, lanY, 7);
        pix.setColor(0.40f, 0.30f, 0.12f, 1f);
        pix.drawCircle(lanX, lanY, 15);
        pix.drawLine(lanX, ay + 30, lanX, lanY - 15);

        // Sandstone Carved Keystone at apex
        pix.setColor(stoneBase);
        pix.fillRectangle(cx - 40, ay - 28, 80, 60);
        pix.setColor(stoneHi);
        pix.drawRectangle(cx - 40, ay - 28, 80, 60);

        // Grand Brass Bosses (heavy ceremonial studs)
        for (int y = sy + 50; y < frameBot - 40; y += 75) {
            drawBrassStud(pix, x0 + 22, y);
            drawBrassStud(pix, x1 - 22, y);
            drawBrassStud(pix, cx - 10, y);
            drawBrassStud(pix, cx + 10, y);
        }

        // Heavy Brass Pull Handles with Lion Knocker Rings
        drawBrassDoorHandle(pix, cx - 50, sy + 420);
        drawBrassDoorHandle(pix, cx + 50, sy + 420);

        // Sandstone threshold at base
        pix.setColor(stoneBase);
        pix.fillRectangle(x0 - archTh - 16, frameBot, (x1 - x0) + (archTh + 16) * 2, 28);
        pix.setColor(stoneHi);
        pix.drawLine(x0 - archTh - 16, frameBot, x1 + archTh + 16, frameBot);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    /**
     * Authentic Curzon Hall Portico Upper Storey Triple Pointed Mughal Windows (1024x1024 with trilinear mipmaps):
     * - Transparent spandrels (alpha=0) seamlessly integrating with Curzon brick walls
     * - Central tall pointed arch flanked by two balanced side pointed arches
     * - Slender carved sandstone baluster colonnettes with molded capitals separating the arches
     * - Deep reflective blue-grey architectural glass and clean white colonial muntins
     * - Continuous carved sandstone sill at base
     */
    private Texture createCurzonPorticoTripleWindow() {
        int w = 1024, h = 1024;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill(); // 100% transparent base!

        Color stoneBase = new Color(0.90f, 0.86f, 0.78f, 1f);
        Color stoneHi = new Color(0.98f, 0.96f, 0.90f, 1f);
        Color stoneSh = new Color(0.60f, 0.55f, 0.48f, 1f);
        Color mullion = new Color(0.92f, 0.90f, 0.84f, 1f);

        // Three arch centers: Left=220, Center=512, Right=804
        int[] centers = {220, 512, 804};
        int[] halfWidths = {105, 135, 105};
        int[] apexY = {140, 70, 140};
        int sy = 460;
        int frameBot = 940;

        for (int i = 0; i < 3; i++) {
            int acx = centers[i];
            int ahw = halfWidths[i];
            int aay = apexY[i];
            float aRise = sy - aay;
            float aD = (aRise * aRise - (float)(ahw * ahw)) / (2.0f * (float)ahw);
            float aR = (float)ahw + aD;
            int aTh = 26;

            // Lower glass
            for (int y = sy; y < frameBot; y++) {
                float t = (float)(y - sy) / (float)(frameBot - sy);
                pix.setColor(MathUtils.lerp(0.14f, 0.07f, t), MathUtils.lerp(0.18f, 0.09f, t), MathUtils.lerp(0.26f, 0.13f, t), 1f);
                pix.fillRectangle(acx - ahw, y, ahw * 2 + 1, 1);
            }

            // Smooth Pointed arch head (NO jagged sawtooth cusps)
            for (int y = aay; y < sy; y++) {
                float dy = sy - y;
                float radSqIn = aR * aR - dy * dy;
                float radSqOut = (aR + aTh) * (aR + aTh) - dy * dy;
                if (radSqIn > 0f) {
                    float rxIn = (float) Math.sqrt(radSqIn);
                    int xlIn = (int)((acx + aD) - rxIn);
                    int xrIn = (int)((acx - aD) + rxIn);

                    // Deep reflective glass
                    float tg = (float)(y - aay) / aRise;
                    pix.setColor(MathUtils.lerp(0.16f, 0.08f, tg), MathUtils.lerp(0.22f, 0.11f, tg), MathUtils.lerp(0.32f, 0.15f, tg), 1f);
                    if (xrIn >= xlIn) pix.fillRectangle(xlIn, y, xrIn - xlIn + 1, 1);

                    if (radSqOut > 0f) {
                        float rxOut = (float) Math.sqrt(radSqOut);
                        int xlOut = (int)((acx + aD) - rxOut);
                        int xrOut = (int)((acx - aD) + rxOut);

                        pix.setColor(stoneBase);
                        if (xlIn > xlOut) pix.fillRectangle(xlOut, y, xlIn - xlOut, 1);
                        if (xrOut > xrIn) pix.fillRectangle(xrIn + 1, y, xrOut - xrIn, 1);

                        pix.setColor(stoneSh);
                        pix.drawPixel(xlIn, y);
                        pix.drawPixel(xrIn, y);
                        pix.setColor(stoneHi);
                        pix.drawPixel(xlOut, y);
                        pix.drawPixel(xrOut, y);
                    }
                }
            }

            // Lower sash mullions
            pix.setColor(mullion);
            pix.fillRectangle(acx - 3, sy, 6, frameBot - sy);
            int subH = frameBot - sy;
            for (int r = 1; r <= 3; r++) {
                pix.fillRectangle(acx - ahw, sy + (subH * r) / 4 - 3, ahw * 2 + 1, 6);
            }

            // Keystone
            pix.setColor(stoneBase);
            pix.fillRectangle(acx - 22, aay - 18, 44, 38);
            pix.setColor(stoneHi);
            pix.drawRectangle(acx - 22, aay - 18, 44, 38);
        }

        // Sandstone colonnettes separating the arches
        for (int px : new int[]{350, 674}) {
            pix.setColor(stoneBase);
            pix.fillRectangle(px - 14, 60, 28, frameBot - 60);
            pix.setColor(stoneHi);
            pix.drawLine(px - 14, 60, px - 14, frameBot);
            pix.setColor(stoneSh);
            pix.drawLine(px + 13, 60, px + 13, frameBot);

            // Molded capitals and bases
            pix.setColor(stoneHi);
            pix.fillRectangle(px - 18, 60, 36, 14);
            pix.fillRectangle(px - 18, frameBot - 14, 36, 14);
        }

        // Projecting continuous stone sill
        pix.setColor(stoneBase);
        pix.fillRectangle(50, frameBot, w - 100, 26);
        pix.setColor(stoneHi);
        pix.drawLine(50, frameBot, w - 50, frameBot);
        pix.setColor(stoneSh);
        pix.drawLine(50, frameBot + 25, w - 50, frameBot + 25);

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pix.dispose();
        return tex;
    }

    private void drawBrassStud(Pixmap pix, int x, int y) {
        pix.setColor(0.92f, 0.78f, 0.22f, 1f);
        pix.fillCircle(x, y, 4);
        pix.setColor(1.0f, 0.92f, 0.55f, 1f);
        pix.drawPixel(x - 1, y - 1);
        pix.setColor(0.35f, 0.25f, 0.08f, 1f);
        pix.drawPixel(x + 1, y + 1);
    }

    private void drawIronHinge(Pixmap pix, int x, int y, int len) {
        pix.setColor(0.12f, 0.11f, 0.10f, 1f);
        pix.fillRectangle(x, y - 6, len, 12);
        pix.setColor(0.24f, 0.22f, 0.20f, 1f);
        pix.drawLine(x, y - 6, x + len, y - 6);
        pix.setColor(0.06f, 0.05f, 0.05f, 1f);
        pix.drawLine(x, y + 6, x + len, y + 6);
    }

    private void drawCarvedDoorPanel(Pixmap pix, int x, int y, int pw, int ph) {
        // Deep sunken bevel shadow
        pix.setColor(0.10f, 0.05f, 0.03f, 1f);
        pix.fillRectangle(x, y, pw, ph);

        // Inner raised panel face with warm teak highlight
        pix.setColor(0.30f, 0.16f, 0.09f, 1f);
        pix.fillRectangle(x + 8, y + 8, pw - 16, ph - 16);

        // Top and left bevel highlight
        pix.setColor(0.44f, 0.25f, 0.14f, 1f);
        pix.drawLine(x + 4, y + 4, x + pw - 4, y + 4);
        pix.drawLine(x + 4, y + 4, x + 4, y + ph - 4);

        // Bottom and right bevel deep shadow
        pix.setColor(0.06f, 0.03f, 0.02f, 1f);
        pix.drawLine(x + 4, y + ph - 4, x + pw - 4, y + ph - 4);
        pix.drawLine(x + pw - 4, y + 4, x + pw - 4, y + ph - 4);

        // Traditional diamond/rosette central carving in each panel
        int cx = x + pw / 2;
        int cy = y + ph / 2;
        int r = Math.min(pw, ph) / 5;
        pix.setColor(0.18f, 0.09f, 0.05f, 1f);
        pix.fillCircle(cx, cy, r);
        pix.setColor(0.38f, 0.22f, 0.12f, 1f);
        pix.drawCircle(cx, cy, r);
    }

    private void drawBrassDoorHandle(Pixmap pix, int cx, int cy) {
        // Heavy brass decorative backplate
        pix.setColor(0.20f, 0.15f, 0.06f, 1f);
        pix.fillRectangle(cx - 14, cy - 28, 28, 56);

        // Burnished warm gold brass body
        pix.setColor(0.92f, 0.78f, 0.24f, 1f);
        pix.fillRectangle(cx - 11, cy - 25, 22, 50);
        pix.setColor(1.0f, 0.94f, 0.58f, 1f);
        pix.drawLine(cx - 6, cy - 22, cx - 6, cy + 22);

        // Ornate circular ring knocker
        pix.setColor(0.95f, 0.82f, 0.28f, 1f);
        pix.drawCircle(cx, cy + 8, 14);
        pix.drawCircle(cx, cy + 8, 13);
        pix.setColor(0.40f, 0.30f, 0.08f, 1f);
        pix.drawCircle(cx, cy + 8, 15);
    }

    /**
     * Worn dirt path, tileable along its length (V) and feathered to transparent at both sides
     * (U) so a strip of it blends into the grass instead of showing a hard edge.
     */
    private Texture[] createDirtPathWithNormal() {
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
        Texture norm = createNormalMap(pix, 2.2f);
        pix.dispose();
        return new Texture[]{tex, norm};
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

    /** Soft filmic gradient for volumetric light shafts: feathered across with smooth Gaussian curve, gently fading at both ends. */
    private Texture createLightBeam() {
        int w = 64, h = 256;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float v = y / (float) (h - 1);
            float along = MathUtils.sin(v * MathUtils.PI);
            along = along * along; // smooth bell curve along length
            for (int x = 0; x < w; x++) {
                float u = x / (float) (w - 1);
                float across = MathUtils.sin(u * MathUtils.PI);
                float a = across * across * across * across * along * 0.70f;
                pix.setColor(1.0f, 0.96f, 0.85f, a);
                pix.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
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

                // Deep, natural chlorophyll without artificial neon/catceta blow-out (Ghost of Tsushima & Where Winds Meet style)
                float gain = 1.0f;
                float r = MathUtils.lerp(br, tr, t) * gain;
                float g = MathUtils.lerp(bg, tg, t) * gain;
                float b = MathUtils.lerp(bb, tb, t) * gain;

                // Subtle natural midrib and lateral veins; NO darkening rim!
                float vein = 0f;
                if (across < 1.6f) vein = 0.035f;
                else {
                    float phase = (t * 9f - edge * 1.6f) % 1f;
                    if (phase < 0f) phase += 1f;
                    if (phase < 0.10f) vein = 0.02f;
                }

                pix.setColor(MathUtils.clamp(r + vein, 0f, 1f),
                             MathUtils.clamp(g + vein, 0f, 1f),
                             MathUtils.clamp(b + vein * 0.5f, 0f, 1f), alpha);
                pix.drawPixel(ox + x, oy + y);
            }
        }
    }

    private Texture[] createTreeBarkWithNormal() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);

        // Natural rough bark palette from reference specification:
        // #34372A (deep shadow), #4A4A32 (dark olive brown), #62563A (natural brown), #81734B (sunlit bark)
        pix.setColor(0.29f, 0.29f, 0.20f, 1f); // #4A4A32 base
        pix.fill();

        // Fine longitudinal grooves, small ridges, uneven color and subtle weathering
        for (int x = 0; x < size; x++) {
            float groove = MathUtils.sin(x * 0.42f) * 0.6f + MathUtils.sin(x * 1.15f) * 0.3f;
            for (int y = 0; y < size; y++) {
                float n = groove * (0.75f + 0.25f * MathUtils.sin(y * 0.08f));
                float speckle = MathUtils.random(-0.02f, 0.02f);
                float r = MathUtils.clamp(0.34f + n * 0.12f + speckle, 0.19f, 0.52f);
                float g = MathUtils.clamp(0.31f + n * 0.10f + speckle, 0.18f, 0.46f);
                float b = MathUtils.clamp(0.21f + n * 0.07f + speckle, 0.13f, 0.31f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 3.5f);
        pix.dispose();
        return new Texture[]{tex, norm};
    }

    private Texture createFoliage() {
        int size = 512;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        // Color bleed transparent background with foliage green to eliminate black halos
        pix.setColor(0.18f, 0.42f, 0.16f, 0f);
        pix.fill();

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
        // Color bleed transparent background with foliage green to eliminate black halos
        pix.setColor(0.18f, 0.42f, 0.16f, 0f);
        pix.fill();

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

        // Bamboo culm specification palette:
        // #183B20 (deep forest green), #42652B (natural bamboo green), #68713A (olive green),
        // #92733B (golden olive), #9A815D (weathered tan)
        for (int x = 0; x < width; x++) {
            float xNorm = (float) x / (float) width;
            float cylLight = 0.65f + 0.35f * MathUtils.sin(xNorm * MathUtils.PI);
            float fiber = MathUtils.sin(x * 1.2f) * 0.035f + MathUtils.sin(x * 3.4f) * 0.02f;

            // Longitudinal color bands giving natural culm age variation across the sheet
            float culmAge = (MathUtils.sin(xNorm * MathUtils.PI2 * 2f) + 1f) * 0.5f;

            for (int y = 0; y < height; y++) {
                // Base culm color blending from natural bamboo green #42652B to olive #68713A
                float rBase = MathUtils.lerp(0.259f, 0.408f, culmAge);
                float gBase = MathUtils.lerp(0.396f, 0.443f, culmAge);
                float bBase = MathUtils.lerp(0.169f, 0.227f, culmAge);

                // Vertical micro-fibers and subtle golden olive undertones #92733B
                float r = MathUtils.clamp((rBase + fiber) * cylLight, 0.094f, 0.604f);
                float g = MathUtils.clamp((gBase + fiber) * cylLight, 0.231f, 0.550f);
                float b = MathUtils.clamp((bBase + fiber * 0.5f) * cylLight, 0.125f, 0.365f);

                pix.setColor(r, g, b, 1f);
                pix.drawPixel(x, y);
            }
        }

        // Horizontal bamboo joints / raised nodal rings spaced along culm
        int nodeInterval = 64;
        for (int y = nodeInterval; y < height; y += nodeInterval) {
            // Upper swollen nodal highlight line (golden olive / pale ivory: #92733B)
            pix.setColor(0.573f, 0.481f, 0.251f, 0.95f);
            pix.drawLine(0, y - 2, width, y - 2);

            // Dark joint indentation ring (#183B20 deep forest green / woody boundary)
            pix.setColor(0.094f, 0.181f, 0.095f, 1f);
            pix.drawLine(0, y - 1, width, y - 1);
            pix.drawLine(0, y, width, y);

            // Lower joint ring shadow (#34372A dark olive)
            pix.setColor(0.204f, 0.216f, 0.165f, 0.85f);
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
        // Color bleed transparent background with bamboo green
        pix.setColor(0.24f, 0.52f, 0.18f, 0f);
        pix.fill();

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
        // Color bleed transparent background with foliage green
        pix.setColor(0.18f, 0.44f, 0.16f, 0f);
        pix.fill();

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

    /**
     * Seamless procedural water ripple normal map for dual-scrolling micro caustics on the pukur surface.
     */
    private Texture createWaterRippleNormal() {
        int size = 256;
        Pixmap pix = new Pixmap(size, size, Format.RGBA8888);
        float k = MathUtils.PI2 / size;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // Periodic multi-frequency height variation
                float a = MathUtils.sin(x * k * 3f + MathUtils.sin(y * k * 2f) * 1.2f);
                float b = MathUtils.sin(y * k * 4f + MathUtils.sin(x * k * 3f) * 1.1f);
                float c = MathUtils.sin((x + y) * k * 5f + MathUtils.cos((x - y) * k * 2f) * 0.8f);
                float h = MathUtils.clamp(((a + b + c) / 3f) * 0.5f + 0.5f, 0f, 1f);
                pix.setColor(h, h, h, 1f);
                pix.drawPixel(x, y);
            }
        }
        Texture normalMap = createNormalMap(pix, 2.5f);
        pix.dispose();
        return normalMap;
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
                            float edgeSoft = 1.0f + 0.04f * (Math.abs(d) / (w + 0.01f));
                            pix.setColor(MathUtils.clamp(bladeCol.r * edgeSoft, 0f, 1f),
                                         MathUtils.clamp(bladeCol.g * edgeSoft, 0f, 1f),
                                         MathUtils.clamp(bladeCol.b * edgeSoft, 0f, 1f), 1f);
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

    private Texture[] createAsphaltRoadWithNormal() {
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

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 2.8f);
        pix.dispose();
        return new Texture[]{tex, norm};
    }

    private Texture[] createModernistConcreteWithNormal() {
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

        Texture tex = new Texture(pix, Format.RGBA8888, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        Texture norm = createNormalMap(pix, 1.8f);
        pix.dispose();
        return new Texture[]{tex, norm};
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

    /**
     * Debug Orientation Test Card (Fix Pack 02)
     * Provides an unambiguous, asymmetric visual reference to test UV mapping,
     * winding order, and texture orientation on 3D meshes.
     * Features:
     *  - Corner markers: Top-Left (RED), Top-Right (GREEN), Bottom-Left (BLUE), Bottom-Right (YELLOW)
     *  - Large asymmetric letter 'F' in the center
     *  - Upward pointing arrow with "UP" indicator
     *  - Explicit "TOP", "BOT", "LEFT", "RIGHT" boundary text
     *  - Fine 32px alignment grid
     */
    private Texture createDebugOrientationTestCard() {
        int w = 512, h = 512;
        Pixmap pix = new Pixmap(w, h, Format.RGBA8888);

        // 1. Dark slate background
        pix.setColor(0.10f, 0.12f, 0.16f, 1f);
        pix.fill();

        // 2. Alignment grid
        pix.setColor(0.18f, 0.22f, 0.28f, 1f);
        for (int x = 0; x < w; x += 32) {
            pix.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 32) {
            pix.drawLine(0, y, w, y);
        }

        // Center crosshairs
        pix.setColor(0.35f, 0.40f, 0.50f, 1f);
        pix.drawLine(w / 2, 0, w / 2, h);
        pix.drawLine(0, h / 2, w, h / 2);

        // 3. Four Distinct Colored Corner Markers (80x80 px)
        // Top-Left: RED (0,0 in Pixmap)
        pix.setColor(0.95f, 0.15f, 0.15f, 1f);
        pix.fillRectangle(0, 0, 80, 80);

        // Top-Right: GREEN (w-80, 0)
        pix.setColor(0.15f, 0.90f, 0.25f, 1f);
        pix.fillRectangle(w - 80, 0, 80, 80);

        // Bottom-Left: BLUE (0, h-80)
        pix.setColor(0.15f, 0.40f, 0.95f, 1f);
        pix.fillRectangle(0, h - 80, 80, 80);

        // Bottom-Right: YELLOW (w-80, h-80)
        pix.setColor(0.95f, 0.85f, 0.15f, 1f);
        pix.fillRectangle(w - 80, h - 80, 80, 80);

        // White perimeter border (6px)
        pix.setColor(Color.WHITE);
        for (int b = 0; b < 6; b++) {
            pix.drawRectangle(b, b, w - 2 * b, h - 2 * b);
        }

        // 4. Large Asymmetric Center Letter 'F' (White with dark drop shadow)
        int cx = w / 2;
        int cy = h / 2;

        // Shadow
        pix.setColor(0f, 0f, 0f, 0.6f);
        pix.fillRectangle(cx - 96, cy - 116, 36, 240);
        pix.fillRectangle(cx - 96, cy - 116, 150, 40);
        pix.fillRectangle(cx - 96, cy - 16, 95, 36);

        // White letter 'F'
        pix.setColor(1f, 1f, 1f, 1f);
        pix.fillRectangle(cx - 100, cy - 120, 36, 240); // vertical stem
        pix.fillRectangle(cx - 100, cy - 120, 150, 40); // top bar
        pix.fillRectangle(cx - 100, cy - 20, 95, 36);   // middle bar

        // 5. Directional Arrow Pointing UP (towards y = 0, Pixmap Top)
        pix.setColor(1.0f, 0.82f, 0.15f, 1f);
        // Vertical shaft
        pix.fillRectangle(cx + 80, cy - 10, 24, 130);
        // Arrow head pointing towards top
        for (int y = cy - 70; y <= cy - 10; y++) {
            int halfSpan = (int) ((y - (cy - 70)) * 0.75f);
            pix.drawLine(cx + 92 - halfSpan, y, cx + 92 + halfSpan, y);
        }

        // 6. Explicit text labels
        drawPixelText(pix, "TOP", cx - 40, 24, 6, Color.WHITE);
        drawPixelText(pix, "BOT", cx - 40, h - 50, 6, Color.WHITE);
        drawPixelText(pix, "LEFT", 12, cy - 30, 5, Color.WHITE);
        drawPixelText(pix, "RIGHT", w - 70, cy - 30, 5, Color.WHITE);
        drawPixelText(pix, "UP", cx + 78, cy + 130, 4, new Color(1.0f, 0.82f, 0.15f, 1f));

        Texture tex = new Texture(pix);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private void drawPixelText(Pixmap pix, String text, int startX, int startY, int scale, Color color) {
        pix.setColor(color);
        int curX = startX;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            drawPixelChar(pix, c, curX, startY, scale);
            curX += 6 * scale;
        }
    }

    private void drawPixelChar(Pixmap pix, char c, int x, int y, int s) {
        int[] rows;
        switch (c) {
            case 'T': rows = new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100}; break;
            case 'O': rows = new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110}; break;
            case 'P': rows = new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000}; break;
            case 'B': rows = new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110}; break;
            case 'L': rows = new int[]{0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111}; break;
            case 'E': rows = new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111}; break;
            case 'F': rows = new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000}; break;
            case 'R': rows = new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001}; break;
            case 'I': rows = new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b11111}; break;
            case 'G': rows = new int[]{0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110}; break;
            case 'H': rows = new int[]{0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001}; break;
            case 'U': rows = new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110}; break;
            default:  rows = new int[]{0, 0, 0, 0, 0, 0, 0}; break;
        }
        for (int r = 0; r < 7; r++) {
            for (int col = 0; col < 5; col++) {
                if (((rows[r] >> (4 - col)) & 1) == 1) {
                    pix.fillRectangle(x + col * s, y + r * s, s, s);
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (Texture t : textures) {
            t.dispose();
        }
        textures.clear();
    }
}
