package bd.spark36.world;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

/**
 * Procedural forest-floor geometry: segmented bamboo canes that arch and carry hanging leaf
 * sprays, low leafy ground plants, and mossy boulders. Like {@link TreeGeometry} it writes into
 * shared mesh parts and never produces loose boxes or interpenetrating slabs.
 */
final class GladeGeometry {

    private static final VertexInfo V0 = new VertexInfo();
    private static final VertexInfo V1 = new VertexInfo();
    private static final VertexInfo V2 = new VertexInfo();
    private static final VertexInfo V3 = new VertexInfo();
    private static final Random RNG = new Random();

    private GladeGeometry() {}

    /**
     * One bamboo cane: a tapering, slightly arching stem built from segments with a raised ring at
     * every node, carrying pairs of thin branches that end in hanging leaf sprays over its upper
     * half. {@code culm} takes the stem meshes (bamboo bark texture), {@code leaves} the sprays
     * (bamboo leaf atlas).
     */
    static void bambooCane(MeshPartBuilder culm, MeshPartBuilder leaves, float x, float z,
                           float height, float lean, float leanAngle, float radius, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        int nodes = Math.max(6, Math.round(height));
        float lx = MathUtils.cos(leanAngle) * lean;
        float lz = MathUtils.sin(leanAngle) * lean;

        float[] px = new float[nodes + 1], py = new float[nodes + 1], pz = new float[nodes + 1], pr = new float[nodes + 1];
        for (int i = 0; i <= nodes; i++) {
            float t = (float) i / nodes;
            px[i] = x + lx * t * t;          // arches more toward the top
            py[i] = height * t;
            pz[i] = z + lz * t * t;
            pr[i] = radius * (1f - 0.55f * t);
        }

        for (int i = 0; i < nodes; i++) {
            TreeGeometry.limb(culm, px[i], py[i], pz[i], pr[i], px[i + 1], py[i + 1], pz[i + 1], pr[i + 1], 6);
            // Node ring: a short, slightly fatter band where the internodes meet
            TreeGeometry.limb(culm, px[i], py[i] - 0.03f, pz[i], pr[i] * 1.3f,
                px[i], py[i] + 0.03f, pz[i], pr[i] * 1.3f, 6);
        }

        int firstBranchNode = (int) (nodes * 0.42f);
        for (int i = firstBranchNode; i <= nodes; i++) {
            float t = (float) i / nodes;
            int branches = i == nodes ? 1 : 3;
            for (int b = 0; b < branches; b++) {
                float yaw = RNG.nextFloat() * MathUtils.PI2;
                float reach = i == nodes ? 0.15f : 0.45f + 0.75f * t * RNG.nextFloat();
                float ex = px[i] + MathUtils.cos(yaw) * reach;
                float ey = py[i] - 0.05f - 0.25f * RNG.nextFloat();
                float ez = pz[i] + MathUtils.sin(yaw) * reach;
                if (i < nodes) {
                    TreeGeometry.limb(culm, px[i], py[i], pz[i], pr[i] * 0.4f, ex, ey, ez, 0.010f, 4);
                }
                spray(leaves, ex, ey, ez, yaw, 0.85f + 0.45f * RNG.nextFloat());
            }
        }
    }

    /** A hanging leaf spray: two crossed vertical cards pivoting at their top centre. */
    private static void spray(MeshPartBuilder mpb, float x, float y, float z, float yaw, float size) {
        int cell = RNG.nextInt(4);
        float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
        float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;
        float half = size * 0.5f;
        float top = y + 0.06f * size, bottom = y - 0.94f * size;

        for (int k = 0; k < 2; k++) {
            float a = yaw + k * MathUtils.HALF_PI;
            float tx = MathUtils.cos(a) * half, tz = MathUtils.sin(a) * half;
            V0.setPos(x - tx, bottom, z - tz).setNor(0f, 1f, 0f).setUV(u0, v1);
            V1.setPos(x + tx, bottom, z + tz).setNor(0f, 1f, 0f).setUV(u1, v1);
            V2.setPos(x + tx, top, z + tz).setNor(0f, 1f, 0f).setUV(u1, v0);
            V3.setPos(x - tx, top, z - tz).setNor(0f, 1f, 0f).setUV(u0, v0);
            mpb.rect(V0, V1, V2, V3);
        }
    }

    /**
     * A low leafy plant: a rosette of leaf cards that rise from one point at random tilts, like the
     * ground cover in a woodland. Uses cells 0 (single leaf) and 1 (leaf fan) of the leaf atlas.
     */
    static void plant(MeshPartBuilder mpb, float x, float z, float size, int leafCount, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        for (int j = 0; j < leafCount; j++) {
            float yaw = (j + RNG.nextFloat() * 0.8f) * MathUtils.PI2 / leafCount;
            float tilt = 0.30f + 0.55f * RNG.nextFloat();      // radians above the ground
            float len = size * (0.75f + 0.55f * RNG.nextFloat());
            float hw = len * 0.42f;

            float ax = MathUtils.cos(yaw) * MathUtils.cos(tilt);
            float ay = MathUtils.sin(tilt);
            float az = MathUtils.sin(yaw) * MathUtils.cos(tilt);
            float wx = -MathUtils.sin(yaw), wz = MathUtils.cos(yaw);

            // Slight upward-biased normal so the plant catches the sky, whatever its facing
            float nx = -ax * 0.2f, ny = 1f, nz = -az * 0.2f;
            int cell = RNG.nextFloat() < 0.7f ? 0 : 1;
            float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
            float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;

            float by = 0.03f;
            V0.setPos(x - wx * hw * 0.25f, by, z - wz * hw * 0.25f).setNor(nx, ny, nz).setUV(u0, v1);
            V1.setPos(x + wx * hw * 0.25f, by, z + wz * hw * 0.25f).setNor(nx, ny, nz).setUV(u1, v1);
            V2.setPos(x + ax * len + wx * hw, by + ay * len, z + az * len + wz * hw).setNor(nx, ny, nz).setUV(u1, v0);
            V3.setPos(x + ax * len - wx * hw, by + ay * len, z + az * len - wz * hw).setNor(nx, ny, nz).setUV(u0, v0);
            mpb.rect(V0, V1, V2, V3);
        }
    }

    /**
     * A boulder with a moss cap: a lumpy ellipsoid sunk into the ground plus a smaller dark-green
     * clump on top, so the rock reads as weathered and lived-in rather than a bare sphere.
     */
    static void rock(MeshPartBuilder body, MeshPartBuilder moss, float x, float z,
                     float rx, float ry, float rz, float seed) {
        float cy = ry * 0.42f; // the flattened lower half sinks below the ground line
        TreeGeometry.clump(body, x, cy, z, rx, ry, rz, seed);
        TreeGeometry.clump(moss, x, cy + ry * 0.62f, z, rx * 0.70f, ry * 0.34f, rz * 0.70f, seed + 2f);
    }

    /**
     * Flat ribbon along a polyline of (x, z) points, {@code halfWidth} to each side, for a path.
     * U runs across the ribbon (0..1); V repeats every {@code tileLength} metres along it.
     */
    static void ribbon(MeshPartBuilder mpb, float[][] points, float halfWidth, float y, float tileLength) {
        float run = 0f;
        for (int i = 0; i < points.length - 1; i++) {
            float[] a = points[i], b = points[i + 1];
            float dx = b[0] - a[0], dz = b[1] - a[1];
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            if (len < 1e-4f) continue;
            float nx = -dz / len * halfWidth, nz = dx / len * halfWidth;
            float va = run / tileLength, vb = (run + len) / tileLength;

            V0.setPos(a[0] - nx, y, a[1] - nz).setNor(0f, 1f, 0f).setUV(0f, va);
            V1.setPos(a[0] + nx, y, a[1] + nz).setNor(0f, 1f, 0f).setUV(1f, va);
            V2.setPos(b[0] + nx, y, b[1] + nz).setNor(0f, 1f, 0f).setUV(1f, vb);
            V3.setPos(b[0] - nx, y, b[1] - nz).setNor(0f, 1f, 0f).setUV(0f, vb);
            mpb.rect(V0, V1, V2, V3);
            run += len;
        }
    }
}
