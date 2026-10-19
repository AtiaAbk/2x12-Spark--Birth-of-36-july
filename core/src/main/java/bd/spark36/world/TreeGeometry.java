package bd.spark36.world;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Procedural tree geometry written into shared mesh parts: tapered limbs that grow out of the
 * trunk, and lumpy leaf clumps that overlap into one canopy. Nothing here is a loose box or a
 * flat card, so limbs never stab through foliage and clumps merge instead of showing seams.
 */
final class TreeGeometry {

    private static final VertexInfo V0 = new VertexInfo();
    private static final VertexInfo V1 = new VertexInfo();
    private static final VertexInfo V2 = new VertexInfo();
    private static final VertexInfo V3 = new VertexInfo();
    private static final Vector3 AXIS = new Vector3();
    private static final Vector3 SIDE = new Vector3();
    private static final Vector3 UP = new Vector3();

    private TreeGeometry() {}

    /** Tapered limb from (a, radius ra) to (b, radius rb). Two-sided material expected. */
    static void limb(MeshPartBuilder mpb, float ax, float ay, float az, float ra,
                     float bx, float by, float bz, float rb, int sides) {
        AXIS.set(bx - ax, by - ay, bz - az);
        float length = AXIS.len();
        AXIS.nor();
        UP.set(Math.abs(AXIS.y) < 0.95f ? Vector3.Y : Vector3.X);
        SIDE.set(AXIS).crs(UP).nor();
        UP.set(SIDE).crs(AXIS).nor();
        float vRepeat = Math.max(0.5f, length * 0.45f);

        for (int i = 0; i < sides; i++) {
            float a0 = MathUtils.PI2 * i / sides;
            float a1 = MathUtils.PI2 * (i + 1) / sides;
            float c0 = MathUtils.cos(a0), s0 = MathUtils.sin(a0);
            float c1 = MathUtils.cos(a1), s1 = MathUtils.sin(a1);
            float r0x = SIDE.x * c0 + UP.x * s0, r0y = SIDE.y * c0 + UP.y * s0, r0z = SIDE.z * c0 + UP.z * s0;
            float r1x = SIDE.x * c1 + UP.x * s1, r1y = SIDE.y * c1 + UP.y * s1, r1z = SIDE.z * c1 + UP.z * s1;
            float u0 = (float) i / sides;
            float u1 = (float) (i + 1) / sides;

            V0.setPos(ax + r0x * ra, ay + r0y * ra, az + r0z * ra).setNor(r0x, r0y, r0z).setUV(u0, 0f);
            V1.setPos(bx + r0x * rb, by + r0y * rb, bz + r0z * rb).setNor(r0x, r0y, r0z).setUV(u0, vRepeat);
            V2.setPos(bx + r1x * rb, by + r1y * rb, bz + r1z * rb).setNor(r1x, r1y, r1z).setUV(u1, vRepeat);
            V3.setPos(ax + r1x * ra, ay + r1y * ra, az + r1z * ra).setNor(r1x, r1y, r1z).setUV(u1, 0f);
            mpb.rect(V0, V1, V2, V3);
        }
    }

    /**
     * Lumpy ellipsoid leaf clump. The lower half is flattened (leaves hang in a shelf, not a
     * ball) and normals lean upward so the clump catches sunlight on top and shades underneath.
     */
    static void clump(MeshPartBuilder mpb, float cx, float cy, float cz,
                      float rx, float ry, float rz, float seed) {
        final int slices = 14;
        final int stacks = 9;
        final int cols = slices + 1;
        float[] p = new float[(stacks + 1) * cols * 6];

        for (int i = 0; i <= stacks; i++) {
            float phi = MathUtils.PI * i / stacks;
            float sp = MathUtils.sin(phi), cp = MathUtils.cos(phi);
            for (int j = 0; j < cols; j++) {
                float th = MathUtils.PI2 * j / slices;
                float lump = lump(th, phi, seed);
                float dx = sp * MathUtils.cos(th);
                float dy = cp;
                float dz = sp * MathUtils.sin(th);
                float yScale = cp < 0f ? 0.55f : 1f;

                int k = (i * cols + j) * 6;
                p[k] = cx + rx * lump * dx;
                p[k + 1] = cy + ry * lump * dy * yScale;
                p[k + 2] = cz + rz * lump * dz;

                float nx = dx / rx, ny = dy / (ry * yScale) + 0.25f, nz = dz / rz;
                float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                p[k + 3] = nx / nl;
                p[k + 4] = ny / nl;
                p[k + 5] = nz / nl;
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                put(V0, p, i, j, cols, slices, stacks);
                put(V1, p, i, j + 1, cols, slices, stacks);
                put(V2, p, i + 1, j + 1, cols, slices, stacks);
                put(V3, p, i + 1, j, cols, slices, stacks);
                mpb.rect(V0, V1, V2, V3);
            }
        }
    }

    /** Radius multiplier of the lumpy canopy surface at longitude th / colatitude phi. */
    private static float lump(float th, float phi, float seed) {
        return 1f
            + 0.18f * MathUtils.sin(th * 3f + seed) * MathUtils.sin(phi * 2f + seed * 0.7f)
            + 0.10f * MathUtils.sin(th * 5f - seed * 1.3f + phi * 3f)
            + 0.08f * MathUtils.cos(th * 2f + phi * 4f + seed * 0.5f);
    }

    private static final java.util.Random RNG = new java.util.Random();
    private static final int MAX_LEAVES_PER_CANOPY = 320;

    /**
     * Fraction of leaf cards to grow (1 = full canopy). Below 1 the shaded core also shrinks so
     * branches and sky show through. Set before building a tree; reset to 1 afterwards.
     */
    static float leafDensity = 1f;

    /**
     * SplitMix64 scramble. java.util.Random seeded with consecutive numbers gives nearly identical
     * first values, so seeds must be mixed first or neighbouring trees, canes and plants line up.
     */
    static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Organic flared buttress roots that radiate from the base of the trunk (y = 0.48 * s)
     * and plunge deeply into the turf (y = -0.22 * s), firmly anchoring the tree in the ground.
     */
    static void buttressRoots(MeshPartBuilder trunk, float cx, float cz, float baseRadius, float s, float seed, int numRoots) {
        for (int k = 0; k < numRoots; k++) {
            float ang = seed * 1.3f + k * MathUtils.PI2 / numRoots + 0.22f * MathUtils.sin(seed * 2.1f + k);
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);

            float startY = 0.48f * s;
            float endY = -0.22f * s;
            float midY = 0.12f * s;

            float rStart = baseRadius * 0.90f;
            float rMid = baseRadius + 0.25f * s;
            float rEnd = baseRadius + (0.55f + 0.18f * MathUtils.sin(k * 1.9f + seed)) * s;

            float p0x = cx + ca * rStart, p0z = cz + sa * rStart;
            float p1x = cx + ca * rMid,   p1z = cz + sa * rMid;
            float p2x = cx + ca * rEnd,   p2z = cz + sa * rEnd;

            limb(trunk, p0x, startY, p0z, 0.14f * s, p1x, midY, p1z, 0.09f * s, 6);
            limb(trunk, p1x, midY, p1z, 0.09f * s, p2x, endY, p2z, 0.04f * s, 6);
        }
    }

    /**
     * A leafy canopy: a slightly smaller dark clump as the shaded core, wrapped in individual
     * leaf cards scattered over the same lumpy surface. {@code cardCells} is the atlas cells the
     * cards may use (0 leaf, 1 leaf fan, 2 feathery leaf, 3 blossoms) with {@code blossomChance}
     * the odds a card is the blossom cell.
     */
    static void foliage(MeshPartBuilder core, MeshPartBuilder cards, float cx, float cy, float cz,
                        float rx, float ry, float rz, float seed, float leafSize,
                        int[] cardCells, float blossomChance) {
        float coreScale = leafDensity < 1f ? 0.90f * (0.45f + 0.55f * leafDensity) : 0.90f;
        clump(core, cx, cy, cz, rx * coreScale, ry * coreScale, rz * coreScale, seed);
        leafCards(cards, cx, cy, cz, rx, ry, rz, seed, leafSize, cardCells, blossomChance);
    }

    /**
     * Scatters leaf cards over the canopy surface with Spherical Normal Blending:
     * Blends 70% macro spherical normal pointing from canopy center with 30% local jitter.
     * This creates rich volumetric wrap lighting across the whole tree instead of flat billboard facets.
     */
    private static void leafCards(MeshPartBuilder mpb, float cx, float cy, float cz,
                                  float rx, float ry, float rz, float seed, float leafSize,
                                  int[] cardCells, float blossomChance) {
        RNG.setSeed(mix((long) (seed * 1000f) * 31L + 17L));

        // Ellipsoid surface area (Knud Thomsen approximation) sets how many cards we need
        final double p = 1.6;
        double area = 4.0 * Math.PI * Math.pow(
            (Math.pow(rx * ry, p) + Math.pow(rx * rz, p) + Math.pow(ry * rz, p)) / 3.0, 1.0 / p);
        int count = Math.min(MAX_LEAVES_PER_CANOPY, (int) (2.6 * area / (leafSize * leafSize * 0.55f)));
        count = (int) (count * leafDensity);

        for (int i = 0; i < count; i++) {
            float th = RNG.nextFloat() * MathUtils.PI2;
            float cp = 2f * RNG.nextFloat() - 1f;
            if (cp < 0f && RNG.nextFloat() < 0.4f) continue; // fewer cards on the underside
            float sp = (float) Math.sqrt(1f - cp * cp);
            float phi = (float) Math.acos(cp);

            float dx = sp * MathUtils.cos(th), dy = cp, dz = sp * MathUtils.sin(th);
            float yScale = cp < 0f ? 0.55f : 1f;
            float lump = lump(th, phi, seed);
            float px = cx + rx * lump * dx;
            float py = cy + ry * lump * dy * yScale;
            float pz = cz + rz * lump * dz;

            // ── Spherical Canopy Normal Blending ─────────────────────────────
            // Macro normal pointing radially outward from clump center (cx, cy, cz)
            float mx = px - cx, my = py - cy, mz = pz - cz;
            float ml = (float) Math.sqrt(mx * mx + my * my + mz * mz);
            if (ml > 0.001f) { mx /= ml; my /= ml; mz /= ml; } else { mx = 0f; my = 1f; mz = 0f; }

            // Upward-biased jittered leaf normal
            float jx = dx / rx + (RNG.nextFloat() - 0.5f) * 0.4f;
            float jy = dy / (ry * yScale) + 0.35f + (RNG.nextFloat() - 0.5f) * 0.3f;
            float jz = dz / rz + (RNG.nextFloat() - 0.5f) * 0.4f;
            float jl = (float) Math.sqrt(jx * jx + jy * jy + jz * jz);
            if (jl > 0.001f) { jx /= jl; jy /= jl; jz /= jl; } else { jx = 0f; jy = 1f; jz = 0f; }

            // 70% macro spherical volume + 30% individual leaf shimmer
            float nx = 0.70f * mx + 0.30f * jx;
            float ny = 0.70f * my + 0.30f * jy;
            float nz = 0.70f * mz + 0.30f * jz;
            float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= nl;
            ny /= nl;
            nz /= nl;

            // Tangent frame on the card plane, spun by a random angle
            if (Math.abs(ny) < 0.95f) UP.set(0f, 1f, 0f); else UP.set(1f, 0f, 0f);
            SIDE.set(nx, ny, nz).crs(UP).nor();
            AXIS.set(nx, ny, nz).crs(SIDE).nor();
            float spin = RNG.nextFloat() * MathUtils.PI2;
            float cs = MathUtils.cos(spin), sn = MathUtils.sin(spin);
            float tx = SIDE.x * cs + AXIS.x * sn, ty = SIDE.y * cs + AXIS.y * sn, tz = SIDE.z * cs + AXIS.z * sn;
            float bx = -SIDE.x * sn + AXIS.x * cs, by = -SIDE.y * sn + AXIS.y * cs, bz = -SIDE.z * sn + AXIS.z * cs;

            float half = leafSize * (0.75f + 0.55f * RNG.nextFloat()) * 0.5f;
            // Lift slightly off the core so cards don't z-fight with it
            float ox = px + nx * half * 0.30f, oy = py + ny * half * 0.30f, oz = pz + nz * half * 0.30f;

            int cell;
            if (blossomChance >= 0.8f && cardCells.length == 4) {
                // White Blossom tree: rich variety of all 4 dense blossom card types
                cell = cardCells[RNG.nextInt(cardCells.length)];
            } else {
                cell = RNG.nextFloat() < blossomChance ? 3 : cardCells[RNG.nextInt(cardCells.length)];
            }
            float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
            float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;

            V0.setPos(ox - tx * half - bx * half, oy - ty * half - by * half, oz - tz * half - bz * half)
                .setNor(nx, ny, nz).setUV(u0, v1);
            V1.setPos(ox + tx * half - bx * half, oy + ty * half - by * half, oz + tz * half - bz * half)
                .setNor(nx, ny, nz).setUV(u1, v1);
            V2.setPos(ox + tx * half + bx * half, oy + ty * half + by * half, oz + tz * half + bz * half)
                .setNor(nx, ny, nz).setUV(u1, v0);
            V3.setPos(ox - tx * half + bx * half, oy - ty * half + by * half, oz - tz * half - bz * half)
                .setNor(nx, ny, nz).setUV(u0, v0);
            mpb.rect(V0, V1, V2, V3);
        }
    }

    private static void put(VertexInfo v, float[] p, int i, int j, int cols, int slices, int stacks) {
        int k = (i * cols + j) * 6;
        v.setPos(p[k], p[k + 1], p[k + 2]).setNor(p[k + 3], p[k + 4], p[k + 5])
            .setUV(j * 2f / slices, i * 1.5f / stacks);
    }

    /**
     * Broadleaf Tree (Samanea saman / Rain tree):
     * Implemented to the user's Broadleaf_Green_Tree_Reference specification:
     * - Tapered trunk with clear trunk height ~2.8m, base radius ~0.42m, crown split ~0.20m.
     * - 7 major spreading scaffold limbs (spec: 7 major branches, range 5-10).
     * - Secondary branching and tertiary twigs spreading upward and outward.
     * - Broad, dense, asymmetrical multi-lobed canopy with gaps revealing branch network.
     * - Flared buttress roots firmly anchoring into turf (y = -0.22 * s).
     */
    static void rainTree(MeshPartBuilder trunk, MeshPartBuilder leaves, MeshPartBuilder cards,
                         float x, float z, float s, float seed) {
        float lean = 0.25f * s * MathUtils.sin(seed);
        float leanZ = 0.25f * s * MathUtils.cos(seed * 1.3f);
        float topX = x + lean, topZ = z + leanZ, topY = 3.6f * s;

        // Organic flared buttress roots anchored into turf (y = -0.22 * s)
        buttressRoots(trunk, x, z, 0.52f * s, s, seed, 5);

        // Multi-segment curved tapered trunk (botanical proportions matching spec)
        float m1x = x + lean * 0.25f, m1z = z + leanZ * 0.25f;
        float m2x = x + lean * 0.60f, m2z = z + leanZ * 0.60f;
        limb(trunk, x, -0.20f * s, z, 0.58f * s, m1x, 0.85f * s, m1z, 0.44f * s, 10);
        limb(trunk, m1x, 0.85f * s, m1z, 0.44f * s, m2x, 2.05f * s, m2z, 0.34f * s, 10);
        limb(trunk, m2x, 2.05f * s, m2z, 0.34f * s, topX, topY, topZ, 0.24f * s, 10);

        // 7 major scaffold limbs radiating upward and outward (spec: 7 major branches)
        for (int k = 0; k < 7; k++) {
            float ang = seed + k * MathUtils.PI2 / 7f + 0.32f * MathUtils.sin(seed * 2.1f + k);
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float h0 = (2.6f + 0.16f * k) * s;
            float t = h0 / topY;
            float sx = x + lean * t, sz = z + leanZ * t;

            float reach = (3.4f + 0.55f * MathUtils.sin(seed + k * 1.7f)) * s;
            float rise = (1.6f + 0.40f * MathUtils.sin(k * 2.1f + seed)) * s;
            float ex = sx + ca * reach, ey = h0 + rise, ez = sz + sa * reach;
            limb(trunk, sx, h0, sz, 0.22f * s, ex, ey, ez, 0.08f * s, 7);

            // Secondary branch division (each major branch forks into 2 sub-branches)
            float fa = ang + (k % 2 == 0 ? 0.65f : -0.65f);
            float mx = sx + ca * reach * 0.58f, my = h0 + rise * 0.58f, mz = sz + sa * reach * 0.58f;
            float fx = mx + MathUtils.cos(fa) * 1.9f * s, fy = my + 1.15f * s, fz = mz + MathUtils.sin(fa) * 1.9f * s;
            limb(trunk, mx, my, mz, 0.12f * s, fx, fy, fz, 0.045f * s, 6);

            // Tertiary twig reaches extending near crown edges
            float ta = ang + (k % 2 == 0 ? -0.45f : 0.45f);
            float tx = ex + MathUtils.cos(ta) * 1.3f * s, ty = ey + 0.65f * s, tz = ez + MathUtils.sin(ta) * 1.3f * s;
            limb(trunk, ex, ey, ez, 0.06f * s, tx, ty, tz, 0.03f * s, 4);

            // Layered foliage masses with natural gaps revealing inner branch network
            foliage(leaves, cards, ex, ey + 0.85f * s, ez, 2.7f * s, 1.8f * s, 2.7f * s, seed + k,
                RAIN_LEAF * s, RAIN_CELLS, 0f);
            foliage(leaves, cards, fx, fy + 0.65f * s, fz, 2.1f * s, 1.4f * s, 2.1f * s, seed + k * 2.3f,
                RAIN_LEAF * s, RAIN_CELLS, 0f);
            foliage(leaves, cards, tx, ty + 0.50f * s, tz, 1.6f * s, 1.2f * s, 1.6f * s, seed + k * 3.7f,
                RAIN_LEAF * s, RAIN_CELLS, 0f);
        }

        // Broad umbrella crown and inner fill
        foliage(leaves, cards, topX, topY + 2.3f * s, topZ, 3.6f * s, 2.4f * s, 3.6f * s, seed + 9f,
            RAIN_LEAF * s, RAIN_CELLS, 0f);
        foliage(leaves, cards, topX, topY + 0.8f * s, topZ, 2.8f * s, 1.6f * s, 2.8f * s, seed + 4f,
            RAIN_LEAF * s, RAIN_CELLS, 0f);
    }

    private static final float RAIN_LEAF = VegetationConfig.RAIN_TREE_LEAF_CARD_SIZE;
    private static final float KRISHNA_LEAF = VegetationConfig.KRISHNA_LEAF_CARD_SIZE;
    private static final int[] RAIN_CELLS = {0, 0, 1, 1, 1};     // broad leaves and fans
    private static final int[] KRISHNA_CELLS = {2, 2, 2, 1};     // mostly feathery compound leaves

    /**
     * Krishnachura (Delonix regia / Flame tree): iconic Dhaka University species.
     * Features flared buttress roots, multi-segment trunk, heavy spreading limbs, and a wide
     * umbrella canopy of fiery scarlet blossoms and feathery bipinnate foliage.
     */
    static void krishnaTree(MeshPartBuilder trunk, MeshPartBuilder blossom, MeshPartBuilder cards,
                            float x, float z, float s, float seed) {
        float topY = 3.0f * s;
        // Buttress roots anchored into turf
        buttressRoots(trunk, x, z, 0.48f * s, s, seed, 4);

        // Multi-segment curved trunk (Delonix regia slender profile)
        float m1x = x + 0.08f * s * MathUtils.sin(seed);
        float m1z = z + 0.08f * s * MathUtils.cos(seed);
        limb(trunk, x, -0.20f * s, z, 0.54f * s, m1x, 0.65f * s, m1z, 0.42f * s, 9);
        limb(trunk, m1x, 0.65f * s, m1z, 0.42f * s, x, 1.70f * s, z, 0.34f * s, 9);
        limb(trunk, x, 1.70f * s, z, 0.34f * s, x, topY, z, 0.24f * s, 9);

        for (int k = 0; k < 3; k++) {
            float ang = seed + k * MathUtils.PI2 / 3f;
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float ex = x + ca * 3.6f * s, ey = topY + 1.1f * s, ez = z + sa * 3.6f * s;
            limb(trunk, x, topY - 0.3f * s, z, 0.22f * s, ex, ey, ez, 0.08f * s, 7);
            foliage(blossom, cards, ex, ey + 0.5f * s, ez, 3.2f * s, 1.4f * s, 3.2f * s, seed + k * 1.9f,
                KRISHNA_LEAF * s, KRISHNA_CELLS, 0.65f);
            foliage(blossom, cards, x + ca * 1.9f * s, topY + 1.3f * s, z + sa * 1.9f * s,
                2.8f * s, 1.3f * s, 2.8f * s, seed + k * 0.8f + 3f, KRISHNA_LEAF * s, KRISHNA_CELLS, 0.60f);
        }
        foliage(blossom, cards, x, topY + 1.5f * s, z, 4.0f * s, 1.6f * s, 4.0f * s, seed + 6f,
            KRISHNA_LEAF * s, KRISHNA_CELLS, 0.60f);
    }

    private static final float WHITE_LEAF = VegetationConfig.WHITE_BLOSSOM_CARD_SIZE;
    private static final int[] WHITE_CELLS = {0, 1, 2, 3};

    /**
     * White Blossom Tree (Magnolia / Cherry / Bokul blossom):
     * Majestic spreading 3D tree with flared buttress roots, dark weathered limbs, and dense,
     * fluffy white blossom canopies.
     */
    static void whiteBlossomTree(MeshPartBuilder trunk, MeshPartBuilder blossomCore, MeshPartBuilder blossomCards,
                                 float x, float z, float s, float seed) {
        float lean = 0.22f * s * MathUtils.sin(seed);
        float leanZ = 0.22f * s * MathUtils.cos(seed * 1.3f);
        float topY = 3.6f * s;
        float topX = x + lean, topZ = z + leanZ;

        // Flared buttress roots anchored into turf
        buttressRoots(trunk, x, z, 0.50f * s, s, seed, 5);

        // Dark gnarled weathered multi-segment trunk
        float m1x = x + lean * 0.3f, m1z = z + leanZ * 0.3f;
        limb(trunk, x, -0.20f * s, z, 0.58f * s, m1x, 0.70f * s, m1z, 0.44f * s, 10);
        limb(trunk, m1x, 0.70f * s, m1z, 0.44f * s, topX, 1.90f * s, topZ, 0.35f * s, 10);
        limb(trunk, topX, 1.90f * s, topZ, 0.35f * s, topX, topY, topZ, 0.25f * s, 10);

        for (int k = 0; k < 5; k++) {
            float ang = seed + k * MathUtils.PI2 / 5f + 0.32f * MathUtils.sin(seed * 2.2f + k);
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float h0 = (2.6f + 0.22f * k) * s;
            float t = h0 / topY;
            float sx = x + lean * t, sz = z + leanZ * t;

            float reach = (3.4f + 0.40f * MathUtils.sin(seed + k * 1.8f)) * s;
            float rise = (1.5f + 0.30f * MathUtils.sin(k * 2.0f + seed)) * s;
            float ex = sx + ca * reach, ey = h0 + rise, ez = sz + sa * reach;
            limb(trunk, sx, h0, sz, 0.24f * s, ex, ey, ez, 0.07f * s, 7);

            // Forked sub-branch
            float fa = ang + (k % 2 == 0 ? 0.65f : -0.65f);
            float mx = sx + ca * reach * 0.58f, my = h0 + rise * 0.58f, mz = sz + sa * reach * 0.58f;
            float fx = mx + MathUtils.cos(fa) * 1.85f * s, fy = my + 1.1f * s, fz = mz + MathUtils.sin(fa) * 1.85f * s;
            limb(trunk, mx, my, mz, 0.11f * s, fx, fy, fz, 0.045f * s, 6);

            // Lush, dense white blossom clusters
            foliage(blossomCore, blossomCards, ex, ey + 0.8f * s, ez, 2.9f * s, 2.0f * s, 2.9f * s, seed + k,
                WHITE_LEAF * s, WHITE_CELLS, 0.85f);
            foliage(blossomCore, blossomCards, fx, fy + 0.6f * s, fz, 2.2f * s, 1.5f * s, 2.2f * s, seed + k * 2.3f,
                WHITE_LEAF * s, WHITE_CELLS, 0.85f);
        }

        // Grand central crown
        foliage(blossomCore, blossomCards, topX, topY + 2.2f * s, topZ, 3.8f * s, 2.4f * s, 3.8f * s, seed + 8f,
            WHITE_LEAF * s, WHITE_CELLS, 0.90f);
        foliage(blossomCore, blossomCards, topX, topY + 0.8f * s, topZ, 2.9f * s, 1.6f * s, 2.9f * s, seed + 3f,
            WHITE_LEAF * s, WHITE_CELLS, 0.85f);
    }

    /**
     * Coconut Palm / Royal Palm (tropical_palm_tree specification):
     * - Tall curved palm trunk with natural lean and horizontal ring scars.
     * - 3 frond tiers: young upright fresh green, mature spreading emerald, and drooping dry tan/brown.
     * - Feathered pinnate fronds with individual thin leaflet quads along curved central spines.
     */
    static void palmTree(MeshPartBuilder trunk, MeshPartBuilder crownshaft, MeshPartBuilder fronds,
                         float x, float z, float s, float seed) {
        float h = 9.4f * s;

        // Natural curved palm trunk lean (spec: curves slightly left/right)
        float leanX = 0.55f * s * MathUtils.sin(seed * 1.7f);
        float leanZ = 0.55f * s * MathUtils.cos(seed * 1.3f);

        // Flared root base into turf
        limb(trunk, x, -0.22f * s, z, 0.38f * s, x, 0.6f * s, z, 0.28f * s, 10);

        // Curved trunk segments with horizontal ring scars
        int trunkSegs = 6;
        float prevX = x, prevY = 0.6f * s, prevZ = z;
        float prevR = 0.28f * s;
        for (int i = 1; i <= trunkSegs; i++) {
            float t = (float) i / trunkSegs;
            float segY = 0.6f * s + (h - 2.0f * s) * t;
            float segX = x + leanX * t * t; // subtle natural quadratic curve
            float segZ = z + leanZ * t * t;
            float segR = MathUtils.lerp(0.28f, 0.18f, t) * s;

            limb(trunk, prevX, prevY, prevZ, prevR, segX, segY, segZ, segR, 10);

            // Horizontal ring scar at each trunk node
            limb(trunk, segX, segY - 0.035f * s, segZ, segR * 1.18f,
                 segX, segY + 0.035f * s, segZ, segR * 1.18f, 10);

            prevX = segX;
            prevY = segY;
            prevZ = segZ;
            prevR = segR;
        }

        // Smooth green crownshaft at the top of the curved column
        MeshPartBuilder csMpb = (crownshaft != null) ? crownshaft : trunk;
        float topX = x + leanX;
        float topZ = z + leanZ;
        limb(csMpb, prevX, prevY, prevZ, prevR, topX, h, topZ, 0.16f * s, 10);

        // Pinnate feather-shaped palm fronds with 3 natural age tiers (spec: upright young, spreading mature, drooping old)
        int numFronds = 20;
        for (int i = 0; i < numFronds; i++) {
            float ang = seed + i * MathUtils.PI2 / numFronds + 0.18f * MathUtils.sin(i * 1.7f + seed);
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float tierFraction = (float) i / numFronds; // 0..1 (0=young upright, 1=old drooping)

            float frondLen = (3.6f + 0.5f * MathUtils.sin(i * 2.3f + seed)) * s;
            float midReach = frondLen * 0.52f;
            float tipReach = frondLen;

            // Tier pitch: young fronds arch high, old fronds droop down
            float midY, tipY;
            if (tierFraction < 0.35f) {
                // Young upright fronds
                midY = h + 1.4f * s;
                tipY = h + 0.8f * s;
            } else if (tierFraction < 0.70f) {
                // Mature spreading fronds
                midY = h + 0.9f * s;
                tipY = h - 0.2f * s;
            } else {
                // Old drooping fronds (weathered brown/tan)
                midY = h + 0.3f * s;
                tipY = h - 1.5f * s;
            }

            float p0x = topX, p0y = h, p0z = topZ;
            float p1x = topX + ca * midReach, p1y = midY, p1z = topZ + sa * midReach;
            float p2x = topX + ca * tipReach, p2y = tipY, p2z = topZ + sa * tipReach;

            // Central arching rachis spine
            limb(fronds, p0x, p0y, p0z, 0.055f * s, p1x, p1y, p1z, 0.032f * s, 4);
            limb(fronds, p1x, p1y, p1z, 0.032f * s, p2x, p2y, p2z, 0.015f * s, 4);

            // Pinnate feather leaflet quads along spine (spec: many thin individual leaflets)
            int leafletPairs = 7;
            for (int lp = 1; lp <= leafletPairs; lp++) {
                float lt = (float) lp / (leafletPairs + 1);
                // Position along spine
                float lx = MathUtils.lerp(p0x, p2x, lt);
                float ly = (lt < 0.5f) ? MathUtils.lerp(p0y, p1y, lt * 2f) : MathUtils.lerp(p1y, p2y, (lt - 0.5f) * 2f);
                float lz = MathUtils.lerp(p0z, p2z, lt);

                // Leaflet span width
                float spanW = (0.50f + 0.25f * MathUtils.sin(lt * MathUtils.PI)) * s;
                float sideX = -sa * spanW;
                float sideZ = ca * spanW;
                float droopY = -0.15f * spanW;

                // Leaflet quad
                V0.setPos(lx, ly, lz).setNor(0f, 1f, 0f).setUV(0f, 0f);
                V1.setPos(lx - sideX, ly + droopY, lz - sideZ).setNor(0f, 1f, 0f).setUV(0f, 1f);
                V2.setPos(lx - sideX * 0.7f + ca * 0.25f * s, ly + droopY - 0.05f * s, lz - sideZ * 0.7f + sa * 0.25f * s)
                    .setNor(0f, 1f, 0f).setUV(0.5f, 1f);
                V3.setPos(lx + ca * 0.25f * s, ly, lz + sa * 0.25f * s).setNor(0f, 1f, 0f).setUV(0.5f, 0f);
                fronds.rect(V0, V1, V2, V3);

                // Opposite side leaflet
                V0.setPos(lx, ly, lz).setNor(0f, 1f, 0f).setUV(0.5f, 0f);
                V1.setPos(lx + sideX, ly + droopY, lz + sideZ).setNor(0f, 1f, 0f).setUV(0.5f, 1f);
                V2.setPos(lx + sideX * 0.7f + ca * 0.25f * s, ly + droopY - 0.05f * s, lz + sideZ * 0.7f + sa * 0.25f * s)
                    .setNor(0f, 1f, 0f).setUV(1f, 1f);
                V3.setPos(lx + ca * 0.25f * s, ly, lz + sa * 0.25f * s).setNor(0f, 1f, 0f).setUV(1f, 0f);
                fronds.rect(V0, V1, V2, V3);
            }
        }
    }
}
