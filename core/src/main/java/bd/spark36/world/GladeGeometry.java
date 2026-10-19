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

    public interface PartSupplier {
        MeshPartBuilder getPart(int needed);
    }

    /**
     * Dense Realistic Bamboo Clump (Dense_Realistic_Bamboo_Clump specification):
     * - 5m x 5m footprint base.
     * - 32 culms (range 24-42) clustered naturally (dense core, spreading outward).
     * - Heights 4.5m to 10.0m (mostly 6.0m to 8.5m).
     * - Diameters 0.035m to 0.095m (radii 0.0175m to 0.0475m) tapering toward top.
     * - Raised nodal rings spaced every 0.22m to 0.48m.
     * - Outward lean on perimeter culms, erect central culms.
     * - Slender branch sprays with lanceolate bamboo leaf cards.
     */
    static void denseBambooClump(PartSupplier culmSupplier, PartSupplier leafSupplier, float cx, float cz, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        int culmCount = 32; // Specification: 24 to 42 culms
        for (int c = 0; c < culmCount; c++) {
            // Clustered distribution within 5x5m footprint:
            // Core culms (60%) within 1.35m radius, perimeter culms (40%) out to 2.35m
            float r = (c < 20) ? (0.20f + 1.15f * (float) Math.sqrt(RNG.nextFloat()))
                               : (1.35f + 1.00f * (float) Math.sqrt(RNG.nextFloat()));
            float theta = RNG.nextFloat() * MathUtils.PI2;
            float x = cx + MathUtils.cos(theta) * r;
            float z = cz + MathUtils.sin(theta) * r;

            // Heights: mostly 6.0m - 8.5m, range 4.5m - 10.0m
            float distNorm = r / 2.35f;
            float baseH = 6.4f + (1.0f - distNorm) * 1.8f; // taller in core
            float height = MathUtils.clamp(baseH + (RNG.nextFloat() - 0.45f) * 2.4f, 4.8f, 9.6f);

            // Diameters: 0.035m - 0.095m (radii: 0.0175m - 0.0475m)
            float radius = MathUtils.clamp(0.020f + (height / 10.0f) * 0.024f + (RNG.nextFloat() - 0.5f) * 0.006f, 0.018f, 0.048f);

            // Natural lean: inner culms erect (lean 0.2 - 0.5m), outer culms lean outward (lean 0.8 - 1.6m)
            float leanAngle = theta + (RNG.nextFloat() - 0.5f) * 0.4f; // radiating outward
            float lean = MathUtils.lerp(0.30f, 1.45f, distNorm) + RNG.nextFloat() * 0.30f;

            MeshPartBuilder culm = (culmSupplier != null) ? culmSupplier.getPart(1600) : null;
            MeshPartBuilder leaves = (leafSupplier != null) ? leafSupplier.getPart(600) : null;
            bambooCane(culm, leaves, x, z, height, lean, leanAngle, radius, seed + c * 37L);
        }
    }

    static void denseBambooClump(MeshPartBuilder culm, MeshPartBuilder leaves, float cx, float cz, long seed) {
        denseBambooClump(needed -> culm, needed -> leaves, cx, cz, seed);
    }

    /**
     * One bamboo cane: a tapering, slightly arching stem built from segments with a raised ring at
     * every node (spaced 0.22m to 0.48m), carrying slender branches that end in hanging leaf sprays over its upper
     * half. {@code culm} takes the stem meshes (bamboo bark texture), {@code leaves} the sprays
     * (bamboo leaf atlas).
     */
    static void bambooCane(MeshPartBuilder culm, MeshPartBuilder leaves, float x, float z,
                           float height, float lean, float leanAngle, float radius, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        // Node spacing between 0.22m and 0.48m (target ~0.32m)
        int nodes = Math.max(12, Math.round(height / 0.32f));
        float lx = MathUtils.cos(leanAngle) * lean;
        float lz = MathUtils.sin(leanAngle) * lean;

        float[] px = new float[nodes + 1], py = new float[nodes + 1], pz = new float[nodes + 1], pr = new float[nodes + 1];
        for (int i = 0; i <= nodes; i++) {
            float t = (float) i / nodes;
            px[i] = x + lx * (t * t * 0.75f + t * 0.25f); // graceful upper arching
            py[i] = height * t;
            pz[i] = z + lz * (t * t * 0.75f + t * 0.25f);
            pr[i] = radius * (1f - 0.55f * t);
        }

        for (int i = 0; i < nodes; i++) {
            TreeGeometry.limb(culm, px[i], py[i], pz[i], pr[i], px[i + 1], py[i + 1], pz[i + 1], pr[i + 1], 6);
            // Raised nodal ring: swollen band at each internode joint
            TreeGeometry.limb(culm, px[i], py[i] - 0.025f, pz[i], pr[i] * 1.35f,
                px[i], py[i] + 0.025f, pz[i], pr[i] * 1.35f, 6);
        }

        // Branching begins around 38% height (lower culm remains clean cane)
        int firstBranchNode = (int) (nodes * 0.38f);
        for (int i = firstBranchNode; i <= nodes; i++) {
            float t = (float) i / nodes;
            int branches = (i == nodes) ? 2 : (t > 0.75f ? 3 : 2);
            for (int b = 0; b < branches; b++) {
                float yaw = RNG.nextFloat() * MathUtils.PI2;
                float reach = (i == nodes) ? 0.20f : 0.45f + 0.65f * t * RNG.nextFloat();
                float ex = px[i] + MathUtils.cos(yaw) * reach;
                float ey = py[i] - 0.04f - 0.20f * RNG.nextFloat();
                float ez = pz[i] + MathUtils.sin(yaw) * reach;
                if (i < nodes) {
                    TreeGeometry.limb(culm, px[i], py[i], pz[i], pr[i] * 0.40f, ex, ey, ez, 0.009f, 4);
                }
                spray(leaves, ex, ey, ez, yaw, 0.85f + 0.45f * RNG.nextFloat());
            }
        }
    }

    /**
     * A hanging bamboo leaf spray: 4 gracefully drooping cross-cards fanning out and downward
     * from branch nodes, with outward-facing normals matching real bamboo foliage (Ref 4 & 5).
     */
    private static void spray(MeshPartBuilder mpb, float x, float y, float z, float yaw, float size) {
        int cell = RNG.nextInt(4);
        float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
        float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;
        float half = size * 0.55f;
        float top = y + 0.08f * size;
        float bottom = y - 0.92f * size;

        // 4 gracefully angled cross-cards at 45 degree intervals for full, natural 3D volume
        for (int k = 0; k < 4; k++) {
            float a = yaw + k * (MathUtils.PI / 4f);
            float ca = MathUtils.cos(a), sa = MathUtils.sin(a);
            float txTop = ca * half * 0.70f, tzTop = sa * half * 0.70f;
            float txBot = ca * half * 1.15f, tzBot = sa * half * 1.15f;

            // Outward-facing volumetric normal for soft, natural wrap lighting
            float nx = ca * 0.55f;
            float ny = 0.75f;
            float nz = sa * 0.55f;
            float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= nl; ny /= nl; nz /= nl;

            V0.setPos(x - txBot, bottom, z - tzBot).setNor(nx, ny, nz).setUV(u0, v1);
            V1.setPos(x + txBot, bottom, z + tzBot).setNor(nx, ny, nz).setUV(u1, v1);
            V2.setPos(x + txTop, top, z + tzTop).setNor(nx, ny, nz).setUV(u1, v0);
            V3.setPos(x - txTop, top, z - tzTop).setNor(nx, ny, nz).setUV(u0, v0);
            mpb.rect(V0, V1, V2, V3);
        }
    }

    /**
     * A 3D volumetric grass tuft: 3 intersecting cross-cards anchored into the turf,
     * utilizing the grassTuftAtlas with natural blade splay (matching Ref 2 under-tree turf).
     */
    static void grassTuft(MeshPartBuilder mpb, float x, float y, float z, float width, float height, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        int cell = RNG.nextInt(4);
        float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
        float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;
        float halfW = width * 0.5f;
        float baseY = y - 0.04f;
        float topY = y + height;

        float baseAngle = RNG.nextFloat() * MathUtils.PI;
        for (int k = 0; k < 3; k++) {
            float ang = baseAngle + k * (MathUtils.PI / 3f);
            float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
            float tx = ca * halfW, tz = sa * halfW;
            float nx = -sa * 0.35f, ny = 0.90f, nz = ca * 0.35f;

            V0.setPos(x - tx, baseY, z - tz).setNor(nx, ny, nz).setUV(u0, v1);
            V1.setPos(x + tx, baseY, z + tz).setNor(nx, ny, nz).setUV(u1, v1);
            V2.setPos(x + tx * 1.20f, topY, z + tz * 1.20f).setNor(nx, ny, nz).setUV(u1, v0);
            V3.setPos(x - tx * 1.20f, topY, z - tz * 1.20f).setNor(nx, ny, nz).setUV(u0, v0);
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
