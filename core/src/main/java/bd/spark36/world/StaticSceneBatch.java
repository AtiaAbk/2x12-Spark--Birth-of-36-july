package bd.spark36.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * Static world geometry baked into few large meshes, drawn with per-chunk frustum culling.
 *
 * <p>The campus is built from ~1000 small {@link ModelInstance}s (plinths, railings, lamp posts,
 * window frames ...). Drawing them one by one costs a draw call each, twice per frame (sun shadow
 * pass and main pass), and nothing was culled. Here every renderable of the static instances is
 * transformed into world space and appended to a chunk keyed by (identical material, 32 m grid
 * cell). Each chunk is one mesh, one draw call and one bounding box tested against the camera of
 * whichever batch draws it (the player camera or the sun's shadow camera).
 *
 * <p>Same triangles, same material objects (the first of each identical group), same shader, so
 * the image is unchanged. Instances that are animated or have materials changed at runtime must
 * be left out by the caller and drawn as before. Transparent (blended) parts are never merged so
 * the back-to-front sort still sees them individually.
 */
public final class StaticSceneBatch implements RenderableProvider, Disposable {

    private static final float CELL = 32f;
    private static final int MAX_VERTS = 65535; // 16-bit indices

    private static final class Chunk {
        final Material material;
        final VertexAttributes attrs;
        final int cellX, cellZ;
        final boolean caster;
        final Array<Renderable> sources = new Array<>();
        int vertCount;
        MeshPart meshPart;
        final BoundingBox bounds = new BoundingBox();

        Chunk(Material material, VertexAttributes attrs, int cellX, int cellZ, boolean caster) {
            this.caster = caster;
            this.material = material;
            this.attrs = attrs;
            this.cellX = cellX;
            this.cellZ = cellZ;
        }
    }

    private final Array<Chunk> chunks = new Array<>();
    private final Array<Mesh> meshes = new Array<>();
    /** Renderables that could not be merged (blended, non-triangle, odd layout); drawn as-is. */
    private final Array<Renderable> passthrough = new Array<>();
    private final com.badlogic.gdx.utils.BooleanArray passthroughCaster = new com.badlogic.gdx.utils.BooleanArray();
    private boolean shadowPass;
    private final Matrix4 identity = new Matrix4();
    private Environment environment;
    private Camera cullCamera;
    private int lastVisible;

    /**
     * @param sources    static instances to bake (their transforms and materials must not change later)
     * @param nonCasters instances among {@code sources} that are skipped in the shadow pass
     */
    public StaticSceneBatch(Array<ModelInstance> sources, java.util.Set<ModelInstance> nonCasters) {
        Array<Renderable> rs = new Array<>();
        Pool<Renderable> pool = new Pool<Renderable>() {
            @Override
            protected Renderable newObject() {
                return new Renderable();
            }
        };
        for (int s = 0; s < sources.size; s++) {
            ModelInstance inst = sources.get(s);
            boolean caster = !nonCasters.contains(inst);
            rs.clear();
            inst.getRenderables(rs, pool);
            for (int i = 0; i < rs.size; i++) {
                Renderable r = rs.get(i);
                if (!mergeable(r)) {
                    passthrough.add(r);
                    passthroughCaster.add(caster);
                    continue;
                }
                addToChunk(r, caster);
            }
        }
        for (int i = 0; i < chunks.size; i++) {
            build(chunks.get(i));
        }
        // Single-source chunks stay merged too: they are already world-space and cullable.
    }

    /**
     * Splits {@code all} into a baked batch and the instances that must stay live: every
     * {@code dynamic} argument (a ModelInstance or an Array of them, nulls ignored) goes to
     * {@code dynamicOut} in original order; the rest is baked.
     */
    public static StaticSceneBatch split(Array<ModelInstance> all, Array<ModelInstance> dynamicOut,
                                         java.util.Set<ModelInstance> nonCasters, Object... dynamic) {
        java.util.Set<ModelInstance> live = new java.util.HashSet<>();
        for (Object o : dynamic) {
            if (o instanceof ModelInstance) live.add((ModelInstance) o);
            else if (o instanceof Array) for (Object e : (Array<?>) o) live.add((ModelInstance) e);
        }
        Array<ModelInstance> fixed = new Array<>();
        for (int i = 0; i < all.size; i++) (live.contains(all.get(i)) ? dynamicOut : fixed).add(all.get(i));
        StaticSceneBatch batch = new StaticSceneBatch(fixed, nonCasters);
        com.badlogic.gdx.Gdx.app.log("StaticSceneBatch", fixed.size + " static instances -> " + batch.chunks.size
            + " chunks + " + batch.passthrough.size + " unmerged parts; " + dynamicOut.size + " dynamic");
        return batch;
    }

    private static boolean mergeable(Renderable r) {
        MeshPart mp = r.meshPart;
        if (mp.primitiveType != GL20.GL_TRIANGLES) return false;
        if (mp.mesh.getNumIndices() <= 0) return false;
        if (r.bones != null) return false;
        if (r.material == null) return false;
        BlendingAttribute b = (BlendingAttribute) r.material.get(BlendingAttribute.Type);
        if (b != null && b.blended) return false;
        VertexAttributes a = mp.mesh.getVertexAttributes();
        for (int i = 0; i < a.size(); i++) {
            int u = a.get(i).usage;
            if (u != VertexAttributes.Usage.Position && u != VertexAttributes.Usage.Normal
                && u != VertexAttributes.Usage.TextureCoordinates && u != VertexAttributes.Usage.ColorUnpacked
                && u != VertexAttributes.Usage.ColorPacked && u != VertexAttributes.Usage.Tangent) {
                return false;
            }
        }
        return a.findByUsage(VertexAttributes.Usage.Position) != null
            && a.findByUsage(VertexAttributes.Usage.Position).numComponents == 3;
    }

    private final BoundingBox tmpBox = new BoundingBox();

    private void addToChunk(Renderable r, boolean caster) {
        r.meshPart.mesh.calculateBoundingBox(tmpBox, r.meshPart.offset, r.meshPart.size);
        tmpBox.mul(r.worldTransform);
        int cx = (int) Math.floor(tmpBox.getCenterX() / CELL);
        int cz = (int) Math.floor(tmpBox.getCenterZ() / CELL);
        VertexAttributes attrs = r.meshPart.mesh.getVertexAttributes();
        int nv = countUsedVertices(r.meshPart);
        for (int i = 0; i < chunks.size; i++) {
            Chunk c = chunks.get(i);
            if (c.cellX == cx && c.cellZ == cz && c.caster == caster && c.attrs.equals(attrs)
                && c.vertCount + nv <= MAX_VERTS
                && (c.material == r.material || c.material.same(r.material, true))) {
                c.sources.add(r);
                c.vertCount += nv;
                return;
            }
        }
        if (nv > MAX_VERTS) {
            passthrough.add(r);
            passthroughCaster.add(caster);
            return;
        }
        Chunk c = new Chunk(r.material, attrs, cx, cz, caster);
        c.sources.add(r);
        c.vertCount = nv;
        chunks.add(c);
    }

    private static int countUsedVertices(MeshPart mp) {
        short[] idx = new short[mp.size];
        mp.mesh.getIndices(mp.offset, mp.size, idx, 0);
        int max = mp.mesh.getNumVertices();
        boolean[] seen = new boolean[max];
        int n = 0;
        for (short s : idx) {
            int v = s & 0xFFFF;
            if (!seen[v]) {
                seen[v] = true;
                n++;
            }
        }
        return n;
    }

    private void build(Chunk c) {
        VertexAttributes attrs = c.attrs;
        int stride = attrs.vertexSize / 4;
        VertexAttribute pos = attrs.findByUsage(VertexAttributes.Usage.Position);
        VertexAttribute nor = attrs.findByUsage(VertexAttributes.Usage.Normal);
        VertexAttribute tan = attrs.findByUsage(VertexAttributes.Usage.Tangent);
        int posOff = pos.offset / 4;
        int norOff = nor != null ? nor.offset / 4 : -1;
        int tanOff = tan != null ? tan.offset / 4 : -1;

        int totalI = 0;
        for (Renderable r : c.sources) totalI += r.meshPart.size;
        float[] verts = new float[c.vertCount * stride];
        short[] out = new short[totalI];
        int vBase = 0, iBase = 0;
        Matrix3 nmat = new Matrix3();
        for (Renderable r : c.sources) {
            Mesh src = r.meshPart.mesh;
            int nvSrc = src.getNumVertices();
            float[] sv = new float[nvSrc * stride];
            src.getVertices(sv);
            short[] si = new short[r.meshPart.size];
            src.getIndices(r.meshPart.offset, r.meshPart.size, si, 0);
            int[] remap = new int[nvSrc];
            java.util.Arrays.fill(remap, -1);
            float[] m = r.worldTransform.val;
            nmat.set(r.worldTransform).inv().transpose();
            float[] nm = nmat.val;
            int next = vBase;
            for (int k = 0; k < si.length; k++) {
                int v = si[k] & 0xFFFF;
                int nvIdx = remap[v];
                if (nvIdx < 0) {
                    nvIdx = next++;
                    remap[v] = nvIdx;
                    int s = v * stride, d = nvIdx * stride;
                    System.arraycopy(sv, s, verts, d, stride);
                    float x = sv[s + posOff], y = sv[s + posOff + 1], z = sv[s + posOff + 2];
                    verts[d + posOff] = m[Matrix4.M00] * x + m[Matrix4.M01] * y + m[Matrix4.M02] * z + m[Matrix4.M03];
                    verts[d + posOff + 1] = m[Matrix4.M10] * x + m[Matrix4.M11] * y + m[Matrix4.M12] * z + m[Matrix4.M13];
                    verts[d + posOff + 2] = m[Matrix4.M20] * x + m[Matrix4.M21] * y + m[Matrix4.M22] * z + m[Matrix4.M23];
                    if (norOff >= 0) transformDir(nm, verts, d + norOff, true);
                    if (tanOff >= 0) transformTangent(m, verts, d + tanOff);
                }
                out[iBase + k] = (short) nvIdx;
            }
            vBase = next;
            iBase += si.length;
        }
        Mesh mesh = new Mesh(true, vBase, totalI, attrs);
        mesh.setVertices(verts, 0, vBase * stride);
        mesh.setIndices(out);
        meshes.add(mesh);
        c.meshPart = new MeshPart("static", mesh, 0, totalI, GL20.GL_TRIANGLES);
        c.meshPart.update();
        mesh.calculateBoundingBox(c.bounds);
        c.sources.clear();
    }

    private static void transformDir(float[] nm, float[] v, int o, boolean normalize) {
        float x = v[o], y = v[o + 1], z = v[o + 2];
        float nx = nm[Matrix3.M00] * x + nm[Matrix3.M01] * y + nm[Matrix3.M02] * z;
        float ny = nm[Matrix3.M10] * x + nm[Matrix3.M11] * y + nm[Matrix3.M12] * z;
        float nz = nm[Matrix3.M20] * x + nm[Matrix3.M21] * y + nm[Matrix3.M22] * z;
        if (normalize) {
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 1e-8f) {
                nx /= len;
                ny /= len;
                nz /= len;
            }
        }
        v[o] = nx;
        v[o + 1] = ny;
        v[o + 2] = nz;
    }

    /** Tangents follow the model matrix (not the normal matrix); w (handedness) is kept. */
    private static void transformTangent(float[] m, float[] v, int o) {
        float x = v[o], y = v[o + 1], z = v[o + 2];
        float tx = m[Matrix4.M00] * x + m[Matrix4.M01] * y + m[Matrix4.M02] * z;
        float ty = m[Matrix4.M10] * x + m[Matrix4.M11] * y + m[Matrix4.M12] * z;
        float tz = m[Matrix4.M20] * x + m[Matrix4.M21] * y + m[Matrix4.M22] * z;
        float len = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
        if (len > 1e-8f) {
            tx /= len;
            ty /= len;
            tz /= len;
        }
        v[o] = tx;
        v[o + 1] = ty;
        v[o + 2] = tz;
    }

    /** Queues visible chunks (culled against the batch camera) plus the pass-through parts. */
    public void render(ModelBatch batch, Environment env) {
        render(batch, env, false);
    }

    /** Shadow pass: only shadow casters, culled against the batch (sun) camera. */
    public void renderShadowCasters(ModelBatch depthBatch) {
        render(depthBatch, null, true);
    }

    private void render(ModelBatch batch, Environment env, boolean shadow) {
        this.shadowPass = shadow;
        this.environment = env;
        this.cullCamera = batch.getCamera();
        batch.render(this);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        int visible = 0;
        for (int i = 0; i < chunks.size; i++) {
            Chunk c = chunks.get(i);
            if (shadowPass && !c.caster) continue;
            if (cullCamera != null && !cullCamera.frustum.boundsInFrustum(c.bounds)) continue;
            Renderable r = pool.obtain();
            r.environment = environment;
            r.material = c.material;
            r.meshPart.set(c.meshPart);
            r.worldTransform.set(identity);
            r.bones = null;
            r.shader = null;
            r.userData = null;
            renderables.add(r);
            visible++;
        }
        for (int i = 0; i < passthrough.size; i++) {
            if (shadowPass && !passthroughCaster.get(i)) continue;
            Renderable src = passthrough.get(i);
            Renderable r = pool.obtain();
            r.set(src);
            r.environment = environment;
            r.shader = null;
            renderables.add(r);
        }
        lastVisible = visible;
    }

    public int getChunkCount() {
        return chunks.size;
    }

    public int getPassthroughCount() {
        return passthrough.size;
    }

    public int getLastVisibleChunks() {
        return lastVisible;
    }

    @Override
    public void dispose() {
        for (int i = 0; i < meshes.size; i++) meshes.get(i).dispose();
        meshes.clear();
        chunks.clear();
        passthrough.clear();
    }
}
