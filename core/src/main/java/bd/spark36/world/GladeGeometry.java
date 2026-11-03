// ei file ta bash jhar, pather pathor ar gash er jhar toiri kore
package bd.spark36.world;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

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

    static void denseBambooClump(PartSupplier culmSupplier, PartSupplier leafSupplier, float cx, float cz, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        int culmCount = 32;
        for (int c = 0; c < culmCount; c++) {

            float r = (c < 20) ? (0.20f + 1.15f * (float) Math.sqrt(RNG.nextFloat()))
                               : (1.35f + 1.00f * (float) Math.sqrt(RNG.nextFloat()));
            float theta = RNG.nextFloat() * MathUtils.PI2;
            float x = cx + MathUtils.cos(theta) * r;
            float z = cz + MathUtils.sin(theta) * r;
            if (DhakaCampusWorld.isRoadOrWalkway(x, z, 1.2f)) {
                x = cx - MathUtils.cos(theta) * (r * 0.5f);
                z = cz - MathUtils.sin(theta) * (r * 0.5f);
                if (DhakaCampusWorld.isRoadOrWalkway(x, z, 1.0f)) continue;
            }

            float distNorm = r / 2.35f;
            float baseH = 6.4f + (1.0f - distNorm) * 1.8f;
            float height = MathUtils.clamp(baseH + (RNG.nextFloat() - 0.45f) * 2.4f, 4.8f, 9.6f);

            float radius = MathUtils.clamp(0.020f + (height / 10.0f) * 0.024f + (RNG.nextFloat() - 0.5f) * 0.006f, 0.018f, 0.048f);

            float leanAngle = theta + (RNG.nextFloat() - 0.5f) * 0.4f;
            float lean = MathUtils.lerp(0.30f, 1.45f, distNorm) + RNG.nextFloat() * 0.30f;
            float tipX = x + MathUtils.cos(leanAngle) * lean;
            float tipZ = z + MathUtils.sin(leanAngle) * lean;
            if (DhakaCampusWorld.isRoadOrWalkway(tipX, tipZ, 0.8f)) {
                leanAngle += MathUtils.PI;
            }

            MeshPartBuilder culm = (culmSupplier != null) ? culmSupplier.getPart(1600) : null;
            MeshPartBuilder leaves = (leafSupplier != null) ? leafSupplier.getPart(600) : null;
            bambooCane(culm, leaves, x, z, height, lean, leanAngle, radius, seed + c * 37L);
        }
    }

    static void denseBambooClump(MeshPartBuilder culm, MeshPartBuilder leaves, float cx, float cz, long seed) {
        denseBambooClump(needed -> culm, needed -> leaves, cx, cz, seed);
    }

    static void bambooCane(MeshPartBuilder culm, MeshPartBuilder leaves, float x, float z,
                           float height, float lean, float leanAngle, float radius, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));

        int nodes = Math.max(12, Math.round(height / 0.32f));
        float lx = MathUtils.cos(leanAngle) * lean;
        float lz = MathUtils.sin(leanAngle) * lean;

        float[] px = new float[nodes + 1], py = new float[nodes + 1], pz = new float[nodes + 1], pr = new float[nodes + 1];
        for (int i = 0; i <= nodes; i++) {
            float t = (float) i / nodes;
            px[i] = x + lx * (t * t * 0.75f + t * 0.25f);
            py[i] = height * t;
            pz[i] = z + lz * (t * t * 0.75f + t * 0.25f);
            pr[i] = radius * (1f - 0.55f * t);
        }

        for (int i = 0; i < nodes; i++) {
            TreeGeometry.limb(culm, px[i], py[i], pz[i], pr[i], px[i + 1], py[i + 1], pz[i + 1], pr[i + 1], 6);

            TreeGeometry.limb(culm, px[i], py[i] - 0.025f, pz[i], pr[i] * 1.35f,
                px[i], py[i] + 0.025f, pz[i], pr[i] * 1.35f, 6);
        }

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

    private static void spray(MeshPartBuilder mpb, float x, float y, float z, float yaw, float size) {
        int cell = RNG.nextInt(4);
        float u0 = (cell % 2) * 0.5f, u1 = u0 + 0.5f;
        float v0 = (cell / 2) * 0.5f, v1 = v0 + 0.5f;

        float reach = size * 0.95f;
        float halfW = size * 0.45f;
        float droop = -size * 0.38f;

        float dx = MathUtils.cos(yaw), dz = MathUtils.sin(yaw);
        float px = -dz, pz = dx;

        float nx = -dx * 0.15f, ny = 0.96f, nz = -dz * 0.15f;

        float tipX = x + dx * reach, tipY = y + droop, tipZ = z + dz * reach;
        V0.setPos(tipX - px * halfW, tipY, tipZ - pz * halfW).setNor(nx, ny, nz).setUV(u0, v1);
        V1.setPos(tipX + px * halfW, tipY, tipZ + pz * halfW).setNor(nx, ny, nz).setUV(u1, v1);
        V2.setPos(x + px * halfW * 0.35f, y + 0.05f * size, z + pz * halfW * 0.35f).setNor(nx, ny, nz).setUV(u1, v0);
        V3.setPos(x - px * halfW * 0.35f, y + 0.05f * size, z - pz * halfW * 0.35f).setNor(nx, ny, nz).setUV(u0, v0);
        mpb.rect(V0, V1, V2, V3);

        int cell2 = (cell + 1) % 4;
        float u0b = (cell2 % 2) * 0.5f, u1b = u0b + 0.5f;
        float v0b = (cell2 / 2) * 0.5f, v1b = v0b + 0.5f;

        float ang2 = yaw + 1.05f;
        float dx2 = MathUtils.cos(ang2), dz2 = MathUtils.sin(ang2);
        float px2 = -dz2, pz2 = dx2;
        float tipX2 = x + dx2 * reach * 0.88f, tipY2 = y + droop * 0.92f, tipZ2 = z + dz2 * reach * 0.88f;

        V0.setPos(tipX2 - px2 * halfW, tipY2, tipZ2 - pz2 * halfW).setNor(nx, ny, nz).setUV(u0b, v1b);
        V1.setPos(tipX2 + px2 * halfW, tipY2, tipZ2 + pz2 * halfW).setNor(nx, ny, nz).setUV(u1b, v1b);
        V2.setPos(x + px2 * halfW * 0.3f, y + 0.04f * size, z + pz2 * halfW * 0.3f).setNor(nx, ny, nz).setUV(u1b, v0b);
        V3.setPos(x - px2 * halfW * 0.3f, y + 0.04f * size, z - px2 * halfW * 0.3f).setNor(nx, ny, nz).setUV(u0b, v0b);
        mpb.rect(V0, V1, V2, V3);

        float ang3 = yaw - 0.95f;
        float dx3 = MathUtils.cos(ang3), dz3 = MathUtils.sin(ang3);
        float px3 = -dz3, pz3 = dx3;
        float tipX3 = x + dx3 * reach * 0.82f, tipY3 = y + droop * 0.85f, tipZ3 = z + dz3 * reach * 0.82f;

        V0.setPos(tipX3 - px3 * halfW * 0.9f, tipY3, tipZ3 - pz3 * halfW * 0.9f).setNor(nx, ny, nz).setUV(u0, v1);
        V1.setPos(tipX3 + px3 * halfW * 0.9f, tipY3, tipZ3 + pz3 * halfW * 0.9f).setNor(nx, ny, nz).setUV(u1, v1);
        V2.setPos(x + px3 * halfW * 0.25f, y + 0.03f * size, z + pz3 * halfW * 0.25f).setNor(nx, ny, nz).setUV(u1, v0);
        V3.setPos(x - px3 * halfW * 0.25f, y + 0.03f * size, z - px3 * halfW * 0.25f).setNor(nx, ny, nz).setUV(u0, v0);
        mpb.rect(V0, V1, V2, V3);
    }

    static void plant(MeshPartBuilder mpb, float x, float z, float size, int leafCount, long seed) {
        RNG.setSeed(TreeGeometry.mix(seed));
        for (int j = 0; j < leafCount; j++) {
            float yaw = (j + RNG.nextFloat() * 0.8f) * MathUtils.PI2 / leafCount;
            float tilt = 0.30f + 0.55f * RNG.nextFloat();
            float len = size * (0.75f + 0.55f * RNG.nextFloat());
            float hw = len * 0.42f;

            float ax = MathUtils.cos(yaw) * MathUtils.cos(tilt);
            float ay = MathUtils.sin(tilt);
            float az = MathUtils.sin(yaw) * MathUtils.cos(tilt);
            float wx = -MathUtils.sin(yaw), wz = MathUtils.cos(yaw);

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

    static void rock(MeshPartBuilder body, MeshPartBuilder moss, float x, float z,
                     float rx, float ry, float rz, float seed) {
        float cy = ry * 0.42f;
        TreeGeometry.clump(body, x, cy, z, rx, ry, rz, seed);
        TreeGeometry.clump(moss, x, cy + ry * 0.62f, z, rx * 0.70f, ry * 0.34f, rz * 0.70f, seed + 2f);
    }

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
