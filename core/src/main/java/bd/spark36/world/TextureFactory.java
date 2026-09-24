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
    public final Texture bambooCulm;
    public final Texture bambooFoliage;
    public final Texture bushFoliage;
    public final Texture weatheredStone;
    public final Texture curzonWater;
    public final Texture backpackFabric;
    public final Texture studentJacket;
    public final Texture movementBanner;
    public final Texture bdFlag;
    public final Texture aparajeyoStone;

    public TextureFactory() {
        brickPavement = createBrickPavement();
        curzonBrick = createCurzonBrick();
        lawnGrass = createLawnGrass();
        treeBark = createTreeBark();
        foliage = createFoliage();
        krishnachuraBlossom = createKrishnachuraBlossom();
        bambooCulm = createBambooCulm();
        bambooFoliage = createBambooFoliage();
        bushFoliage = createBushFoliage();
        weatheredStone = createWeatheredStone();
        curzonWater = createCurzonWater();
        backpackFabric = createBackpackFabric();
        studentJacket = createStudentJacket();
        movementBanner = createMovementBanner();
        bdFlag = createBdFlag();
        aparajeyoStone = createAparajeyoStone();

        textures.addAll(brickPavement, curzonBrick, lawnGrass, treeBark,
                        foliage, krishnachuraBlossom, bambooCulm, bambooFoliage,
                        bushFoliage, weatheredStone, curzonWater,
                        backpackFabric, studentJacket, movementBanner, bdFlag, aparajeyoStone);
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
            int leafCount = (int)(rad * 1.3f);
            for (int i = 0; i < leafCount; i++) {
                float angle = MathUtils.random(0f, 360f);
                float dist = MathUtils.random(0f, rad);
                float lx = cx + MathUtils.cosDeg(angle) * dist;
                float ly = cy + MathUtils.sinDeg(angle) * dist;
                float leafLen = MathUtils.random(22f, 42f);
                float leafAngle = angle + MathUtils.random(-35f, 35f);

                float var = MathUtils.random(0f, 1f);
                Color blade = new Color(
                    0.14f + var * 0.14f,
                    0.42f + var * 0.26f,
                    0.12f + var * 0.10f,
                    1f
                );
                Color vein = new Color(0.48f, 0.78f, 0.28f, 1f);
                drawBambooBlade(pix, lx, ly, leafLen, leafAngle, 9f, blade, vein);
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
        for (int b = 0; b < 24; b++) {
            float fx = MathUtils.random(80, 432);
            float fy = MathUtils.random(60, 380);
            float fLen = MathUtils.random(60, 110);
            float fAngle = MathUtils.random(-80, 80);
            Color fernCol = new Color(0.16f, 0.52f, 0.14f, 1f);
            Color fernVein = new Color(0.40f, 0.72f, 0.22f, 1f);
            drawBambooBlade(pix, fx, fy, fLen, fAngle, 7f, fernCol, fernVein);
        }

        // Iconic blazing scarlet-orange Krishnachura 5-petaled flowers
        int flowerCount = 75;
        for (int i = 0; i < flowerCount; i++) {
            float fx = MathUtils.random(60, 452);
            float fy = MathUtils.random(50, 400);
            int petLen = MathUtils.random(10, 20);

            // 5 fiery petals radiating outward
            for (int p = 0; p < 5; p++) {
                float pAngle = p * 72f + MathUtils.random(-12f, 12f);
                float px = fx + MathUtils.cosDeg(pAngle) * petLen;
                float py = fy + MathUtils.sinDeg(pAngle) * petLen;

                // Scarlet red petal with orange flame highlight
                float shade = MathUtils.random(0.85f, 1.0f);
                pix.setColor(0.96f * shade, 0.18f + MathUtils.random(-0.04f, 0.08f), 0.08f, 1f);
                pix.fillCircle((int)px, (int)py, Math.max(3, petLen / 3));
            }

            // Golden-yellow central stamens
            pix.setColor(1.0f, 0.88f, 0.24f, 1f);
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

    @Override
    public void dispose() {
        for (Texture t : textures) {
            t.dispose();
        }
        textures.clear();
    }
}
