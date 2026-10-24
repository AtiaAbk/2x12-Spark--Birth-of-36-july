package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.world.landmarks.BengaliSignAtlas;
import bd.spark36.world.landmarks.BengaliSignAtlas.Sign;
import bd.spark36.world.landmarks.BusBuilder;
import bd.spark36.world.landmarks.CngAutoRickshawBuilder;
import bd.spark36.world.landmarks.ElectricPoleCableBuilder;
import bd.spark36.world.landmarks.RickshawBuilder;
import bd.spark36.world.landmarks.ShopfrontBuilder;
import bd.spark36.world.landmarks.StreetPropKit;
import bd.spark36.world.landmarks.TeaStallBuilder;

import java.util.List;

/**
 * Street identity for Levels 3–6 (reference §6.3): the Doel Chattar road outside the main gate
 * (z 79–91) gets a row of shop buildings with Bengali signboards on its far side, a footpath,
 * a run of electric poles with Dhaka's tangled cables, a tea stall against the campus wall and
 * rickshaws, CNG auto-rickshaws and a bus, arranged per chapter:
 * <ul>
 *   <li>Level 3 (police escalation): traffic pulled over, half the shutters coming down.</li>
 *   <li>Level 4 (curfew): every shutter down, tea stall boarded, rickshaws abandoned on the road,
 *       one tipped over; no bus.</li>
 *   <li>Level 5 (mass mobilisation): rickshaws and CNGs parked along the kerb, a bus that brought
 *       people in, shops open.</li>
 *   <li>Level 6 (5 August): traffic back on the road, every shop open.</li>
 * </ul>
 * Shop names are generic and fictional; no political graffiti, no damage to real landmarks.
 *
 * <p>Everything is static and goes into one {@link ModelCache}, so the whole street costs about
 * one draw call per shared material (around 20) in the colour pass and again in the shadow pass.</p>
 */
public final class StreetDressing implements Disposable {

    private static final float SHOP_FRONT_Z = 93.6f;
    private static final float FOOTPATH_Z = 92.3f;
    private static final float KERB_PARK_Z = 89.6f;

    private final int level;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private BengaliSignAtlas atlas;
    private ModelCache cache;

    public StreetDressing(int level) {
        this.level = level;
    }

    /** Builds and places the street props for this level and appends their collision boxes. GL thread only. */
    public void build(List<float[]> colliders) {
        // A/B switch for performance measurements: -Dbd.spark36.streetDressing=false
        if ("false".equalsIgnoreCase(System.getProperty("bd.spark36.streetDressing"))) return;
        try {
            atlas = new BengaliSignAtlas();
            StreetPropKit kit = new StreetPropKit(atlas.getTexture());
            ModelBuilder mb = new ModelBuilder();
            placeShops(mb, kit, colliders);
            placePoles(mb, kit, colliders);
            placeTeaStall(mb, kit, colliders);
            placeVehicles(mb, kit, colliders);

            cache = new ModelCache();
            cache.begin();
            cache.add(instances);
            cache.end();
            logCost();
        } catch (Throwable t) {
            // Street dressing is decoration: never let it break a level
            Gdx.app.error("StreetDressing", "Street dressing for level " + level + " skipped: " + t.getMessage(), t);
            disposeCache();
        }
    }

    public void render(ModelBatch batch, Environment env) {
        if (cache != null) batch.render(cache, env);
    }

    public void renderShadow(ModelBatch shadowBatch) {
        if (cache != null) shadowBatch.render(cache);
    }

    /** Number of placed props (for tests and logs). */
    public int getPropCount() {
        return instances.size;
    }

    // ── Shop row ────────────────────────────────────────────────────────────

    private static final class Building {
        final int bays, floors, plaster; final Sign[] signs;
        Building(int bays, int floors, int plaster, Sign... signs) {
            this.bays = bays; this.floors = floors; this.plaster = plaster; this.signs = signs;
        }
    }

    /** West to east along the far side of the road; the 5-bay block faces the main gate. */
    private static final Building[] ROW = {
        new Building(3, 2, 0, Sign.BOOKS, Sign.PHOTOCOPY, null),
        new Building(2, 1, 1, Sign.HOTEL, null),
        new Building(3, 3, 2, Sign.PHARMACY, null, Sign.GENERAL_STORE),
        new Building(2, 2, 0, Sign.TELECOM, Sign.PHOTOCOPY),
        new Building(5, 2, 1, Sign.GENERAL_STORE, null, Sign.HOTEL, Sign.BOOKS, null),
        new Building(3, 2, 2, Sign.PHOTOCOPY, Sign.TELECOM, null),
        new Building(2, 3, 0, Sign.HOTEL, null),
        new Building(3, 2, 2, Sign.PHARMACY, Sign.BOOKS, Sign.GENERAL_STORE),
        new Building(2, 1, 1, null, Sign.TELECOM),
    };
    private static final float GAP = 0.6f;

    private boolean shopOpen(int building, int bay) {
        switch (level) {
            case 4: return false;                                        // curfew: all shutters down
            case 3: return ((building + bay) % 2) == 0;                 // shutters coming down
            case 5: return ((building * 3 + bay) % 5) != 0;             // most open
            default: return true;                                        // level 6: everything open
        }
    }

    private void placeShops(ModelBuilder mb, StreetPropKit kit, List<float[]> colliders) {
        Material[] plasters = { kit.plasterCream, kit.plasterGreen, kit.plasterRose };
        float total = 0f;
        for (Building b : ROW) total += ShopfrontBuilder.width(b.bays) + GAP;
        total -= GAP;
        // Centre the 5-bay block on the gate (x = 0)
        float centreOffset = 0f;
        for (int i = 0; i < 4; i++) centreOffset += ShopfrontBuilder.width(ROW[i].bays) + GAP;
        final float rowStart = -centreOffset - ShopfrontBuilder.width(ROW[4].bays) * 0.5f;
        float x = rowStart;

        for (int i = 0; i < ROW.length; i++) {
            Building b = ROW[i];
            float w = ShopfrontBuilder.width(b.bays);
            boolean[] open = new boolean[b.bays];
            for (int bay = 0; bay < b.bays; bay++) open[bay] = shopOpen(i, bay);
            Model m = ShopfrontBuilder.build(mb, kit, b.bays, b.floors, plasters[b.plaster], b.signs, open);
            float cx = x + w * 0.5f;
            add(m, cx, 0f, SHOP_FRONT_Z, 180f);
            colliders.add(new float[] { cx - w * 0.5f, 0f, SHOP_FRONT_Z - 0.1f, cx + w * 0.5f, 3.4f + b.floors * 3f,
                SHOP_FRONT_Z + ShopfrontBuilder.DEPTH });
            x += w + GAP;
        }
        Model footpath = ShopfrontBuilder.buildFootpath(mb, kit, total + 4f, 2.6f);
        add(footpath, rowStart + total * 0.5f, 0f, FOOTPATH_Z, 180f);
    }

    // ── Electric poles ──────────────────────────────────────────────────────

    private void placePoles(ModelBuilder mb, StreetPropKit kit, List<float[]> colliders) {
        float[][] poles = { {-46f, 91.6f}, {-30f, 91.6f}, {-14f, 91.6f}, {6f, 91.6f}, {22f, 91.6f}, {38f, 91.6f}, {54f, 91.6f} };
        boolean[] transformer = { false, true, false, false, true, false, false };
        Model run = ElectricPoleCableBuilder.buildRun(mb, kit, poles, transformer, SHOP_FRONT_Z);
        add(run, 0f, 0f, 0f, 0f);
        for (float[] p : poles) colliders.add(new float[] { p[0] - 0.2f, 0f, p[1] - 0.2f, p[0] + 0.2f, 9f, p[1] + 0.2f });
    }

    // ── Tea stall against the campus wall, west of the gate ─────────────────

    private void placeTeaStall(ModelBuilder mb, StreetPropKit kit, List<float[]> colliders) {
        Model stall = TeaStallBuilder.build(mb, kit, level != 4);
        float sx = -14f, sz = 79.05f;
        add(stall, sx, 0f, sz, 0f);
        colliders.add(new float[] { sx - 1.05f, 0f, sz - 0.65f, sx + 1.05f, 2.5f, sz + 0.75f });
        colliders.add(new float[] { sx - 0.9f, 0f, sz + 1.5f, sx + 0.9f, 0.5f, sz + 1.95f });
    }

    // ── Vehicles ────────────────────────────────────────────────────────────

    private void placeVehicles(ModelBuilder mb, StreetPropKit kit, List<float[]> colliders) {
        Model rickshawUp = RickshawBuilder.build(mb, kit, true);
        Model rickshawDown = RickshawBuilder.build(mb, kit, false);
        Model cng = CngAutoRickshawBuilder.build(mb, kit);
        Model bus = (level == 4) ? null : BusBuilder.build(mb, kit);

        // {x, z, yaw}; rickshaw/CNG origin is the rear axle, front toward +Z before yaw
        switch (level) {
            case 3:
                rickshaw(rickshawUp, -22f, KERB_PARK_Z, 90f, colliders);
                rickshaw(rickshawUp, -25.2f, KERB_PARK_Z + 0.1f, 90f, colliders);
                rickshaw(rickshawDown, -30f, KERB_PARK_Z - 0.1f, 95f, colliders);
                rickshaw(rickshawUp, 17f, KERB_PARK_Z, -90f, colliders);
                rickshaw(rickshawUp, 20.4f, KERB_PARK_Z + 0.2f, -88f, colliders);
                vehicle(cng, 27f, KERB_PARK_Z - 0.2f, -90f, 0.7f, 2.1f, 1.8f, colliders);
                vehicle(bus, -44f, 86.4f, 90f, 1.3f, 5.0f, 3.1f, colliders);
                break;
            case 4:
                rickshaw(rickshawDown, -11f, 83.5f, 35f, colliders);
                rickshaw(rickshawDown, 6.8f, 87.6f, -110f, colliders);
                rickshaw(rickshawDown, 21f, 84.2f, 160f, colliders);
                rickshaw(rickshawUp, -27f, KERB_PARK_Z - 0.6f, 80f, colliders);
                rickshaw(rickshawDown, -7.6f, 73.4f, 12f, colliders);   // inside the gate, left behind
                tipped(rickshawDown, 13.5f, 81.8f, 70f, colliders);
                vehicle(cng, 31f, 86f, 70f, 0.7f, 2.1f, 1.8f, colliders);
                break;
            case 5:
                rickshaw(rickshawUp, -20f, KERB_PARK_Z, 90f, colliders);
                rickshaw(rickshawUp, -23.2f, KERB_PARK_Z + 0.1f, 92f, colliders);
                rickshaw(rickshawDown, -26.4f, KERB_PARK_Z, 88f, colliders);
                rickshaw(rickshawUp, 16f, KERB_PARK_Z, -90f, colliders);
                rickshaw(rickshawUp, 19.2f, KERB_PARK_Z + 0.1f, -92f, colliders);
                rickshaw(rickshawDown, 22.4f, KERB_PARK_Z, -90f, colliders);
                vehicle(cng, 30f, KERB_PARK_Z - 0.2f, -90f, 0.7f, 2.1f, 1.8f, colliders);
                vehicle(cng, -34f, KERB_PARK_Z - 0.2f, 90f, 0.7f, 2.1f, 1.8f, colliders);
                vehicle(bus, -50f, 86.4f, 90f, 1.3f, 5.0f, 3.1f, colliders);
                break;
            default: // 6
                rickshaw(rickshawUp, -18f, 82.8f, 90f, colliders);
                rickshaw(rickshawUp, 12f, 87.0f, -90f, colliders);
                rickshaw(rickshawUp, 24f, 82.6f, 90f, colliders);
                rickshaw(rickshawUp, -30f, KERB_PARK_Z, 90f, colliders);
                rickshaw(rickshawDown, -33.2f, KERB_PARK_Z + 0.1f, 92f, colliders);
                vehicle(cng, 35f, 86.8f, -90f, 0.7f, 2.1f, 1.8f, colliders);
                vehicle(cng, -40f, 83.0f, 90f, 0.7f, 2.1f, 1.8f, colliders);
                vehicle(bus, 52f, 86.4f, -90f, 1.3f, 5.0f, 3.1f, colliders);
                break;
        }
        models.add(rickshawUp);
        models.add(rickshawDown);
        models.add(cng);
        if (bus != null) models.add(bus);
    }

    private void rickshaw(Model m, float x, float z, float yaw, List<float[]> colliders) {
        vehicle(m, x, z, yaw, 0.6f, 2.1f, 2.0f, colliders);
    }

    /** Places a vehicle and adds an axis-aligned box around its rotated footprint. */
    private void vehicle(Model m, float x, float z, float yaw, float halfW, float front, float h, List<float[]> colliders) {
        if (m == null) return;
        ModelInstance mi = new ModelInstance(m);
        mi.transform.setToTranslation(x, 0f, z).rotate(Vector3.Y, yaw);
        instances.add(mi);
        // The local footprint runs from z = -back to z = front, x = ±halfW
        float back = (front > 4f) ? front : 0.7f;
        float c = (float) Math.cos(Math.toRadians(yaw)), s = (float) Math.sin(Math.toRadians(yaw));
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        float[][] corners = { {-halfW, -back}, {halfW, -back}, {halfW, front}, {-halfW, front} };
        for (float[] p : corners) {
            float wx = x + p[0] * c + p[1] * s;
            float wz = z - p[0] * s + p[1] * c;
            minX = Math.min(minX, wx); maxX = Math.max(maxX, wx);
            minZ = Math.min(minZ, wz); maxZ = Math.max(maxZ, wz);
        }
        colliders.add(new float[] { minX, 0f, minZ, maxX, h, maxZ });
    }

    /** A rickshaw knocked onto its side during the curfew. */
    private void tipped(Model m, float x, float z, float yaw, List<float[]> colliders) {
        ModelInstance mi = new ModelInstance(m);
        mi.transform.setToTranslation(x, 0.58f, z).rotate(Vector3.Y, yaw).rotate(Vector3.Z, 78f);
        instances.add(mi);
        colliders.add(new float[] { x - 1.3f, 0f, z - 1.3f, x + 1.3f, 1.2f, z + 1.3f });
    }

    private void add(Model m, float x, float y, float z, float yaw) {
        models.add(m);
        ModelInstance mi = new ModelInstance(m);
        mi.transform.setToTranslation(x, y, z).rotate(Vector3.Y, yaw);
        instances.add(mi);
    }

    /** Logs what the street costs per pass: merged renderables (draw calls) and vertices. */
    private void logCost() {
        Array<com.badlogic.gdx.graphics.g3d.Renderable> out = new Array<>();
        com.badlogic.gdx.utils.Pool<com.badlogic.gdx.graphics.g3d.Renderable> pool =
            new com.badlogic.gdx.utils.Pool<com.badlogic.gdx.graphics.g3d.Renderable>() {
                @Override protected com.badlogic.gdx.graphics.g3d.Renderable newObject() {
                    return new com.badlogic.gdx.graphics.g3d.Renderable();
                }
            };
        cache.getRenderables(out, pool);
        int verts = 0, tris = 0;
        for (com.badlogic.gdx.graphics.g3d.Renderable r : out) tris += r.meshPart.size / 3;
        java.util.IdentityHashMap<com.badlogic.gdx.graphics.Mesh, Boolean> meshes = new java.util.IdentityHashMap<>();
        for (com.badlogic.gdx.graphics.g3d.Renderable r : out) {
            if (meshes.put(r.meshPart.mesh, Boolean.TRUE) == null) verts += r.meshPart.mesh.getNumVertices();
        }
        Gdx.app.log("StreetDressing", "Level " + level + ": " + instances.size + " props -> " + out.size
            + " draw calls per pass, " + tris + " triangles, " + verts + " vertices");
    }

    private void disposeCache() {
        if (cache != null) { cache.dispose(); cache = null; }
    }

    @Override
    public void dispose() {
        disposeCache();
        for (Model m : models) m.dispose();
        models.clear();
        instances.clear();
        if (atlas != null) { atlas.dispose(); atlas = null; }
    }
}
