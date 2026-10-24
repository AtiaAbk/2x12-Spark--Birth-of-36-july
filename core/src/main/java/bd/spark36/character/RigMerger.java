package bd.spark36.character;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

/**
 * Bakes the rigid parts of one body segment into as few meshes as possible.
 *
 * <p>A {@link StudentMesh} body segment (head, chest, forearm ...) is made of dozens of small
 * primitives that all move with the same segment transform. Drawing each as its own
 * {@link ModelInstance} cost one draw call per primitive per pass (about 110 per character, twice:
 * shadow and main). Parts whose materials are identical in every attribute value are merged here
 * into one mesh with their local transform baked into the vertices, so the shader, material and
 * geometry are unchanged and the image is the same; only the number of draw calls drops.
 *
 * <p>Runs once at rig build time; nothing here is called per frame.
 */
final class RigMerger {

    private static final int MAX_VERTS = 65535; // short indices

    private RigMerger() {}

    /** Returns merged parts (identity local transform) for {@code parts}; new models go to {@code owned}. */
    static Array<StudentMesh.Part> merge(Array<StudentMesh.Part> parts, Array<Model> owned) {
        Array<StudentMesh.Part> out = new Array<>();
        boolean[] used = new boolean[parts.size];
        Array<StudentMesh.Part> group = new Array<>();
        for (int i = 0; i < parts.size; i++) {
            if (used[i]) continue;
            StudentMesh.Part first = parts.get(i);
            Material mat = first.inst.materials.first();
            group.clear();
            int verts = 0;
            for (int j = i; j < parts.size; j++) {
                if (used[j]) continue;
                StudentMesh.Part p = parts.get(j);
                if (!mergeable(p)) {
                    if (j == i) break;
                    continue;
                }
                if (j != i && !p.inst.materials.first().same(mat, true)) continue;
                if (j != i && !sameLayout(first, p)) continue;
                int v = meshOf(p).getNumVertices();
                if (verts + v > MAX_VERTS) continue;
                verts += v;
                group.add(p);
                used[j] = true;
            }
            if (group.size <= 1) {
                used[i] = true;
                out.add(first);
            } else {
                out.add(bake(group, mat, owned));
            }
        }
        return out;
    }

    private static boolean mergeable(StudentMesh.Part p) {
        Model m = p.model;
        if (m.nodes.size != 1 || m.meshes.size != 1) return false;
        Node n = m.nodes.first();
        if (n.parts.size != 1 || n.hasChildren()) return false;
        if (!isIdentity(n.globalTransform)) return false;
        MeshPart mp = n.parts.first().meshPart;
        return mp.primitiveType == GL20.GL_TRIANGLES && mp.mesh.getNumIndices() > 0
            && p.inst.materials.size == 1;
    }

    private static final Matrix4 IDENTITY = new Matrix4();

    private static boolean isIdentity(Matrix4 m) {
        for (int k = 0; k < 16; k++) if (Math.abs(m.val[k] - IDENTITY.val[k]) > 1e-6f) return false;
        return true;
    }

    private static Mesh meshOf(StudentMesh.Part p) {
        return p.model.nodes.first().parts.first().meshPart.mesh;
    }

    private static boolean sameLayout(StudentMesh.Part a, StudentMesh.Part b) {
        return meshOf(a).getVertexAttributes().equals(meshOf(b).getVertexAttributes());
    }

    private static StudentMesh.Part bake(Array<StudentMesh.Part> group, Material mat, Array<Model> owned) {
        StudentMesh.Part first = group.first();
        VertexAttributes attrs = meshOf(first).getVertexAttributes();
        int stride = attrs.vertexSize / 4;
        VertexAttribute pos = attrs.findByUsage(VertexAttributes.Usage.Position);
        VertexAttribute nor = attrs.findByUsage(VertexAttributes.Usage.Normal);
        int posOff = pos != null ? pos.offset / 4 : -1;
        int norOff = nor != null ? nor.offset / 4 : -1;

        int totalV = 0, totalI = 0;
        for (StudentMesh.Part p : group) {
            NodePart np = p.model.nodes.first().parts.first();
            totalV += np.meshPart.mesh.getNumVertices();
            totalI += np.meshPart.size;
        }
        float[] verts = new float[totalV * stride];
        short[] idx = new short[totalI];
        int vBase = 0, iBase = 0;
        Matrix3 normalMat = new Matrix3();
        for (StudentMesh.Part p : group) {
            MeshPart mp = p.model.nodes.first().parts.first().meshPart;
            Mesh src = mp.mesh;
            int nv = src.getNumVertices();
            float[] sv = new float[nv * stride];
            src.getVertices(sv);
            short[] si = new short[src.getNumIndices()];
            src.getIndices(si);
            float[] m = p.local.val;
            normalMat.set(p.local).inv().transpose();
            float[] nm = normalMat.val;
            for (int v = 0; v < nv; v++) {
                int s = v * stride;
                int d = (vBase + v) * stride;
                System.arraycopy(sv, s, verts, d, stride);
                if (posOff >= 0) {
                    float x = sv[s + posOff], y = sv[s + posOff + 1], z = sv[s + posOff + 2];
                    verts[d + posOff] = m[Matrix4.M00] * x + m[Matrix4.M01] * y + m[Matrix4.M02] * z + m[Matrix4.M03];
                    verts[d + posOff + 1] = m[Matrix4.M10] * x + m[Matrix4.M11] * y + m[Matrix4.M12] * z + m[Matrix4.M13];
                    verts[d + posOff + 2] = m[Matrix4.M20] * x + m[Matrix4.M21] * y + m[Matrix4.M22] * z + m[Matrix4.M23];
                }
                if (norOff >= 0) {
                    float x = sv[s + norOff], y = sv[s + norOff + 1], z = sv[s + norOff + 2];
                    float nx = nm[Matrix3.M00] * x + nm[Matrix3.M01] * y + nm[Matrix3.M02] * z;
                    float ny = nm[Matrix3.M10] * x + nm[Matrix3.M11] * y + nm[Matrix3.M12] * z;
                    float nz = nm[Matrix3.M20] * x + nm[Matrix3.M21] * y + nm[Matrix3.M22] * z;
                    float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (len > 1e-8f) { nx /= len; ny /= len; nz /= len; }
                    verts[d + norOff] = nx;
                    verts[d + norOff + 1] = ny;
                    verts[d + norOff + 2] = nz;
                }
            }
            for (int k = 0; k < mp.size; k++) {
                idx[iBase + k] = (short) (vBase + (si[mp.offset + k] & 0xFFFF));
            }
            vBase += nv;
            iBase += mp.size;
        }

        Mesh mesh = new Mesh(true, totalV, totalI, attrs);
        mesh.setVertices(verts);
        mesh.setIndices(idx);
        Model model = new Model();
        Material copy = mat.copy();
        MeshPart meshPart = new MeshPart("merged", mesh, 0, totalI, GL20.GL_TRIANGLES);
        Node node = new Node();
        node.id = "merged";
        node.parts.add(new NodePart(meshPart, copy));
        model.meshes.add(mesh);
        model.materials.add(copy);
        model.meshParts.add(meshPart);
        model.nodes.add(node);
        model.manageDisposable(mesh);
        model.calculateTransforms();
        owned.add(model);
        return new StudentMesh.Part(model, new ModelInstance(model), new Matrix4(),
            first.slot, first.tr, first.tg, first.tb, first.group);
    }
}
