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
            + 0.17f * MathUtils.sin(th * 3f + seed) * MathUtils.sin(phi * 2f + seed * 0.7f)
            + 0.09f * MathUtils.sin(th * 5f - seed * 1.3f + phi * 3f);
    }

    private static final java.util.Random RNG = new java.util.Random();
    private static final int MAX_LEAVES_PER_CANOPY = 900;

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
     * A leafy canopy: a slightly smaller dark clump as the shaded core, wrapped in individual
     * leaf cards scattered over the same lumpy surface. {@code cardCells} is the atlas cells the
     * cards may use (0 leaf, 1 leaf fan, 2 feathery leaf, 3 blossoms) with {@code blossomChance}
     * the odds a card is the blossom cell.
     */
    static void foliage(MeshPartBuilder core, MeshPartBuilder cards, float cx, float cy, float cz,
                        float rx, float ry, float rz, float seed, float leafSize,
                        int[] cardCells, float blossomChance) {
        clump(core, cx, cy, cz, rx * 0.90f, ry * 0.90f, rz * 0.90f, seed);
        leafCards(cards, cx, cy, cz, rx, ry, rz, seed, leafSize, cardCells, blossomChance);
    }

    /** Scatters leaf cards over the canopy surface, facing outward with random tilt and spin. */
    private static void leafCards(MeshPartBuilder mpb, float cx, float cy, float cz,
                                  float rx, float ry, float rz, float seed, float leafSize,
                                  int[] cardCells, float blossomChance) {
        RNG.setSeed(mix((long) (seed * 1000f) * 31L + 17L));

        // Ellipsoid surface area (Knud Thomsen approximation) sets how many cards we need
        final double p = 1.6;
        double area = 4.0 * Math.PI * Math.pow(
            (Math.pow(rx * ry, p) + Math.pow(rx * rz, p) + Math.pow(ry * rz, p)) / 3.0, 1.0 / p);
        int count = Math.min(MAX_LEAVES_PER_CANOPY, (int) (2.6 * area / (leafSize * leafSize * 0.55f)));

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

            // Outward normal, leaned up (leaves face the sky) and jittered so no two match
            float nx = dx / rx + (RNG.nextFloat() - 0.5f) * 0.5f;
            float ny = dy / (ry * yScale) + 0.25f + (RNG.nextFloat() - 0.5f) * 0.4f;
            float nz = dz / rz + (RNG.nextFloat() - 0.5f) * 0.5f;
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

            int cell = RNG.nextFloat() < blossomChance ? 3 : cardCells[RNG.nextInt(cardCells.length)];
            float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
            float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;

            V0.setPos(ox - tx * half - bx * half, oy - ty * half - by * half, oz - tz * half - bz * half)
                .setNor(nx, ny, nz).setUV(u0, v1);
            V1.setPos(ox + tx * half - bx * half, oy + ty * half - by * half, oz + tz * half - bz * half)
                .setNor(nx, ny, nz).setUV(u1, v1);
            V2.setPos(ox + tx * half + bx * half, oy + ty * half + by * half, oz + tz * half + bz * half)
                .setNor(nx, ny, nz).setUV(u1, v0);
            V3.setPos(ox - tx * half + bx * half, oy - ty * half + by * half, oz - tz * half + bz * half)
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
     * Rain tree: a flared trunk, five limbs that fork, and a broad shelf-like canopy.
     * {@code s} scales the whole tree, {@code seed} varies limb angles and clump shapes.
     */
    static void rainTree(MeshPartBuilder trunk, MeshPartBuilder leaves, MeshPartBuilder cards,
                         float x, float z, float s, float seed) {
        float lean = 0.25f * s * MathUtils.sin(seed);
        float leanZ = 0.25f * s * MathUtils.cos(seed * 1.3f);
        float topX = x + lean, topZ = z + leanZ, topY = 3.6f * s;

        // Flared root collar, then the trunk proper
        limb(trunk, x, -0.15f, z, 0.95f * s, x, 0.7f * s, z, 0.62f * s, 10);
        limb(trunk, x, 0.7f * s, z, 0.62f * s, topX, topY, topZ, 0.36f * s, 10);

        for (int k = 0; k < 5; k++) {
            float ang = seed + k * MathUtils.PI2 / 5f + 0.35f * MathUtils.sin(seed * 2.1f + k);
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float h0 = (2.7f + 0.22f * k) * s;
            float t = h0 / topY;
            float sx = x + lean * t, sz = z + leanZ * t;

            float reach = (3.3f + 0.4f * MathUtils.sin(seed + k * 1.7f)) * s;
            float rise = (1.5f + 0.35f * MathUtils.sin(k * 2.1f + seed)) * s;
            float ex = sx + ca * reach, ey = h0 + rise, ez = sz + sa * reach;
            limb(trunk, sx, h0, sz, 0.32f * s, ex, ey, ez, 0.09f * s, 7);

            // Fork off the outer half of each limb
            float fa = ang + (k % 2 == 0 ? 0.7f : -0.7f);
            float mx = sx + ca * reach * 0.6f, my = h0 + rise * 0.6f, mz = sz + sa * reach * 0.6f;
            float fx = mx + MathUtils.cos(fa) * 1.8f * s, fy = my + 1.1f * s, fz = mz + MathUtils.sin(fa) * 1.8f * s;
            limb(trunk, mx, my, mz, 0.15f * s, fx, fy, fz, 0.06f * s, 6);

            foliage(leaves, cards, ex, ey + 0.9f * s, ez, 2.7f * s, 1.9f * s, 2.7f * s, seed + k,
                RAIN_LEAF * s, RAIN_CELLS, 0f);
            foliage(leaves, cards, fx, fy + 0.7f * s, fz, 2.0f * s, 1.5f * s, 2.0f * s, seed + k * 2.3f,
                RAIN_LEAF * s, RAIN_CELLS, 0f);
        }

        // Crown and inner fill so the trunk top and limb roots are never visible from below
        foliage(leaves, cards, topX, topY + 2.2f * s, topZ, 3.5f * s, 2.3f * s, 3.5f * s, seed + 9f,
            RAIN_LEAF * s, RAIN_CELLS, 0f);
        foliage(leaves, cards, topX, topY + 0.7f * s, topZ, 2.7f * s, 1.5f * s, 2.7f * s, seed + 4f,
            RAIN_LEAF * s, RAIN_CELLS, 0f);
    }

    private static final float RAIN_LEAF = 0.95f;
    private static final float KRISHNA_LEAF = 0.80f;
    private static final int[] RAIN_CELLS = {0, 0, 1, 1, 1};     // broad leaves and fans
    private static final int[] KRISHNA_CELLS = {2, 2, 2, 1};     // mostly feathery compound leaves

    /**
     * Krishnachura (flame tree): short trunk, three heavy limbs and a wide, flat umbrella of
     * blossom-covered leaves.
     */
    static void krishnaTree(MeshPartBuilder trunk, MeshPartBuilder blossom, MeshPartBuilder cards,
                            float x, float z, float s, float seed) {
        float topY = 3.0f * s;
        limb(trunk, x, -0.15f, z, 0.78f * s, x, 0.6f * s, z, 0.52f * s, 9);
        limb(trunk, x, 0.6f * s, z, 0.52f * s, x, topY, z, 0.34f * s, 9);

        for (int k = 0; k < 3; k++) {
            float ang = seed + k * MathUtils.PI2 / 3f;
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float ex = x + ca * 3.6f * s, ey = topY + 1.1f * s, ez = z + sa * 3.6f * s;
            limb(trunk, x, topY - 0.3f * s, z, 0.28f * s, ex, ey, ez, 0.10f * s, 7);
            foliage(blossom, cards, ex, ey + 0.5f * s, ez, 3.0f * s, 1.3f * s, 3.0f * s, seed + k * 1.9f,
                KRISHNA_LEAF * s, KRISHNA_CELLS, 0.30f);
            foliage(blossom, cards, x + ca * 1.9f * s, topY + 1.3f * s, z + sa * 1.9f * s,
                2.6f * s, 1.2f * s, 2.6f * s, seed + k * 0.8f + 3f, KRISHNA_LEAF * s, KRISHNA_CELLS, 0.30f);
        }
        foliage(blossom, cards, x, topY + 1.5f * s, z, 3.8f * s, 1.5f * s, 3.8f * s, seed + 6f,
            KRISHNA_LEAF * s, KRISHNA_CELLS, 0.30f);
    }
}
