package bd.spark36.world.landmarks;

import static bd.spark36.world.landmarks.StreetPropKit.box;
import static bd.spark36.world.landmarks.StreetPropKit.post;
import static bd.spark36.world.landmarks.StreetPropKit.rod;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * A run of tapered concrete distribution poles with a steel cross-arm and insulators, three
 * sagging power conductors between poles, the lower bundle of loosely strung telecom/TV cables
 * that Dhaka streets are known for, coiled spare cable, pole-mounted transformer drums on
 * chosen poles, and service drops to the buildings behind the run.
 *
 * <p>Built directly in world coordinates (place the instance at the origin), so the whole run is
 * one static model for the street's ModelCache.</p>
 */
public final class ElectricPoleCableBuilder {

    private static final float POLE_H = 9.0f;
    private static final int SAG_SEGMENTS = 6;

    private ElectricPoleCableBuilder() {}

    /**
     * @param poles       pole base positions {x, z} in order along the run
     * @param transformer per pole: mount a transformer drum
     * @param dropZ       z of the building faces that receive service drops, or NaN for none
     */
    public static Model buildRun(ModelBuilder mb, StreetPropKit k, float[][] poles, boolean[] transformer, float dropZ) {
        mb.begin();
        long a = StreetPropKit.ATTR;
        // Parts of one vertex layout share a MeshBuilder: finish each material before the next part().
        int n = poles.length;
        Vector3[] dirs = new Vector3[n];
        Vector3[] sides = new Vector3[n];
        float[] yaws = new float[n];
        for (int i = 0; i < n; i++) {
            int j = (i + 1 < n) ? i + 1 : i - 1;
            dirs[i] = new Vector3(poles[j][0] - poles[i][0], 0f, poles[j][1] - poles[i][1]).nor();
            if (j < i) dirs[i].scl(-1f);
            sides[i] = new Vector3(-dirs[i].z, 0f, dirs[i].x);
            yaws[i] = (float) Math.toDegrees(Math.atan2(sides[i].x, sides[i].z));
        }

        // Tapered concrete poles: three stacked sections
        MeshPartBuilder conc = mb.part("poles", GL20.GL_TRIANGLES, a, k.concrete);
        for (float[] p : poles) {
            box(conc, p[0], 1.5f, p[1], 0.30f, 3.0f, 0.30f);
            box(conc, p[0], 4.5f, p[1], 0.25f, 3.0f, 0.25f);
            box(conc, p[0], 7.5f, p[1], 0.20f, 3.0f, 0.20f);
        }

        // Cross-arms, insulators, junction boxes, transformer brackets
        MeshPartBuilder steel = mb.part("hardware", GL20.GL_TRIANGLES, a, k.steel);
        for (int i = 0; i < n; i++) {
            float x = poles[i][0], z = poles[i][1];
            Vector3 side = sides[i], dir = dirs[i];
            box(steel, x, POLE_H - 0.6f, z, 0.08f, 0.08f, 1.8f, yaws[i], 0f, 0f);
            for (int c = -1; c <= 1; c++) {
                post(steel, x + side.x * c * 0.8f, POLE_H - 0.56f, z + side.z * c * 0.8f, 0.07f, 0.16f, 6);
            }
            box(steel, x - side.x * 0.20f, 4.2f, z - side.z * 0.20f, 0.16f, 0.30f, 0.22f, yaws[i], 0f, 0f);
            if (transformer != null && transformer[i]) {
                box(steel, x + dir.x * 0.25f, 6.35f, z + dir.z * 0.25f, 0.06f, 0.06f, 0.5f, yaws[i] + 90f, 0f, 0f);
            }
        }

        // Transformer drums
        MeshPartBuilder tank = mb.part("transformers", GL20.GL_TRIANGLES, a, k.shutter);
        for (int i = 0; i < n; i++) {
            if (transformer == null || !transformer[i]) continue;
            post(tank, poles[i][0] + dirs[i].x * 0.45f, 6.4f, poles[i][1] + dirs[i].z * 0.45f, 0.55f, 0.85f, 10);
        }

        // Cables: coils, service drops, power conductors and the lower telecom/TV bundle
        MeshPartBuilder cable = mb.part("cables", GL20.GL_TRIANGLES, a, k.cable);
        Vector3 pa = new Vector3(), pb = new Vector3();
        for (int i = 0; i < n; i++) {
            float x = poles[i][0], z = poles[i][1];
            Vector3 side = sides[i];
            box(cable, x + side.x * 0.22f, 5.6f, z + side.z * 0.22f, 0.16f, 0.45f, 0.45f, yaws[i], 0f, 0f);
            if (!Float.isNaN(dropZ)) {
                for (int d = -1; d <= 1; d += 2) {
                    pa.set(x, 6.1f + d * 0.15f, z);
                    pb.set(x + d * 2.4f, 5.6f, dropZ);
                    sag(cable, pa, pb, 0.35f, 0.022f);
                }
            }
            if (i + 1 >= n) continue;
            float nx = poles[i + 1][0], nz = poles[i + 1][1];
            for (int c = -1; c <= 1; c++) {
                pa.set(x + side.x * c * 0.8f, POLE_H - 0.45f, z + side.z * c * 0.8f);
                pb.set(nx + side.x * c * 0.8f, POLE_H - 0.45f, nz + side.z * c * 0.8f);
                sag(cable, pa, pb, 0.45f, 0.025f);
            }
            for (int c = 0; c < 4; c++) {
                float h0 = 5.8f + c * 0.22f;
                float off = (c - 1.5f) * 0.07f;
                pa.set(x + side.x * off, h0, z + side.z * off);
                pb.set(nx + side.x * off, h0 + ((c % 2 == 0) ? 0.12f : -0.10f), nz + side.z * off);
                sag(cable, pa, pb, 0.65f + c * 0.18f, 0.03f);
            }
        }
        return mb.end();
    }

    /** Parabolic sag approximation of a catenary from a to b, built from short rods. */
    private static void sag(MeshPartBuilder p, Vector3 a, Vector3 b, float sag, float thickness) {
        Vector3 prev = new Vector3(a);
        Vector3 cur = new Vector3();
        for (int s = 1; s <= SAG_SEGMENTS; s++) {
            float t = s / (float) SAG_SEGMENTS;
            cur.set(a).lerp(b, t);
            cur.y -= 4f * sag * t * (1f - t);
            rod(p, prev, cur, thickness);
            prev.set(cur);
        }
    }
}
