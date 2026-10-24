package bd.spark36.world.landmarks;

import static bd.spark36.world.landmarks.StreetPropKit.box;
import static bd.spark36.world.landmarks.StreetPropKit.post;
import static bd.spark36.world.landmarks.StreetPropKit.signQuad;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Non-signature Dhaka mixed-use building with ground-floor shops, after reference §6.3:
 * painted plaster, 3 m shop bays with rolling steel shutters (rolled up or pulled down),
 * flex signboards in Bengali over each bay, grilled upper-floor windows under concrete
 * sunshades (chajja), one small balcony, a parapet and a black plastic rooftop water tank.
 *
 * <p>Local frame: origin on the ground at the centre of the front face, front toward +Z,
 * building extending 7 m toward -Z.</p>
 */
public final class ShopfrontBuilder {

    public static final float BAY_W = 3.0f;
    public static final float DEPTH = 7.0f;
    private static final float GROUND_H = 3.4f;
    private static final float STOREY_H = 3.0f;

    private ShopfrontBuilder() {}

    /** Overall width of a building with the given number of bays. */
    public static float width(int bays) {
        return bays * BAY_W + 0.4f;
    }

    /**
     * @param signs one entry per bay; null leaves that bay without a board
     * @param open  one entry per bay; true = shutter rolled up and the shop lit inside
     */
    public static Model build(ModelBuilder mb, StreetPropKit k, int bays, int upperFloors, Material plaster,
                              BengaliSignAtlas.Sign[] signs, boolean[] open) {
        mb.begin();
        long a = StreetPropKit.ATTR;
        final float w = width(bays);
        final float h = GROUND_H + upperFloors * STOREY_H;
        final float x0 = -w * 0.5f + 0.2f + BAY_W * 0.5f; // centre of bay 0

        MeshPartBuilder wall = mb.part("plaster", GL20.GL_TRIANGLES, a, plaster);
        box(wall, 0f, h * 0.5f, -DEPTH * 0.5f - 0.3f, w, h, DEPTH - 0.6f);           // main block
        box(wall, 0f, GROUND_H + upperFloors * STOREY_H * 0.5f, -0.3f, w, upperFloors * STOREY_H, 0.6f); // upper front
        box(wall, 0f, GROUND_H - 0.25f, -0.25f, w, 0.5f, 0.5f);                       // shop beam
        for (int i = 0; i <= bays; i++) {
            box(wall, -w * 0.5f + 0.2f + i * BAY_W, GROUND_H * 0.5f, -0.3f, 0.4f, GROUND_H, 0.6f); // piers
        }
        box(wall, 0f, h + 0.45f, -0.1f, w, 0.9f, 0.2f);                                // front parapet
        box(wall, -w * 0.5f + 1.6f, h + 1.2f, -DEPTH + 1.6f, 2.4f, 2.4f, 2.2f);       // stair room

        // NOTE: parts of the same vertex layout share one MeshBuilder, so each part's geometry
        // must be complete before the next part() call.
        MeshPartBuilder interior = mb.part("interior", GL20.GL_TRIANGLES, a, k.interior);
        for (int i = 0; i < bays; i++) {
            if (open[i]) box(interior, x0 + i * BAY_W, (GROUND_H - 0.5f) * 0.5f, -0.58f, BAY_W - 0.4f, GROUND_H - 0.5f, 0.04f);
        }

        MeshPartBuilder shutter = mb.part("shutter", GL20.GL_TRIANGLES, a, k.shutter);
        for (int i = 0; i < bays; i++) {
            float x = x0 + i * BAY_W;
            if (open[i]) {
                box(shutter, x, GROUND_H - 0.62f, -0.12f, BAY_W - 0.4f, 0.22f, 0.22f);   // rolled shutter drum
            } else {
                box(shutter, x, (GROUND_H - 0.5f) * 0.5f, -0.08f, BAY_W - 0.4f, GROUND_H - 0.5f, 0.04f);
                for (int r = 0; r < 9; r++) {
                    box(shutter, x, 0.25f + r * 0.32f, -0.05f, BAY_W - 0.42f, 0.03f, 0.03f); // ribs
                }
            }
        }

        // Goods in open shops: a counter, a wall shelf and stock in mixed colours
        MeshPartBuilder wood = mb.part("wood", GL20.GL_TRIANGLES, a, k.wood);
        for (int i = 0; i < bays; i++) {
            if (!open[i]) continue;
            float x = x0 + i * BAY_W;
            box(wood, x, 0.45f, -0.20f, BAY_W - 0.9f, 0.90f, 0.45f);
            box(wood, x, 1.70f, -0.48f, BAY_W - 0.6f, 0.04f, 0.18f);
        }
        MeshPartBuilder stockY = mb.part("stockY", GL20.GL_TRIANGLES, a, k.accentYellow);
        for (int i = 0; i < bays; i++) {
            if (open[i]) box(stockY, x0 + i * BAY_W - 0.6f, 1.86f, -0.48f, 0.5f, 0.28f, 0.14f);
        }
        MeshPartBuilder stockB = mb.part("stockB", GL20.GL_TRIANGLES, a, k.accentBlue);
        for (int i = 0; i < bays; i++) {
            if (!open[i]) continue;
            box(stockB, x0 + i * BAY_W + 0.1f, 1.84f, -0.48f, 0.6f, 0.24f, 0.14f);
            box(stockB, x0 + i * BAY_W - 0.4f, 1.05f, -0.20f, 0.35f, 0.30f, 0.30f);
        }
        MeshPartBuilder stockR = mb.part("stockR", GL20.GL_TRIANGLES, a, k.rickshawRed);
        for (int i = 0; i < bays; i++) {
            if (open[i]) box(stockR, x0 + i * BAY_W + 0.75f, 1.88f, -0.48f, 0.3f, 0.32f, 0.14f);
        }

        // Steel: sign backing, padlocks, window grilles, balcony rail, water-tank stand
        MeshPartBuilder steel = mb.part("steel", GL20.GL_TRIANGLES, a, k.steel);
        for (int i = 0; i < bays; i++) {
            float x = x0 + i * BAY_W;
            if (signs[i] != null) box(steel, x, GROUND_H - 0.10f, 0.03f, BAY_W - 0.15f, 0.62f, 0.06f);
            if (!open[i]) box(steel, x, 0.12f, -0.03f, 0.30f, 0.06f, 0.06f);                    // padlock hasp
        }
        for (int f = 0; f < upperFloors; f++) {
            float yc = GROUND_H + f * STOREY_H + 1.45f;
            for (int i = 0; i < bays; i++) {
                float x = x0 + i * BAY_W;
                for (int g = 0; g < 6; g++) box(steel, x - 0.65f + g * 0.26f, yc, 0.08f, 0.025f, 1.34f, 0.025f);
                box(steel, x, yc + 0.66f, 0.08f, 1.56f, 0.03f, 0.03f);
                box(steel, x, yc - 0.66f, 0.08f, 1.56f, 0.03f, 0.03f);
                box(steel, x, yc, 0.08f, 1.56f, 0.025f, 0.025f);
            }
        }
        boolean balcony = upperFloors > 1 && bays > 1;
        // On the top floor, clear of the shop signs below
        final float bx = x0 + (bays - 1) * BAY_W;
        final float by = GROUND_H + (upperFloors - 1) * STOREY_H + 0.05f;
        if (balcony) {
            box(steel, bx, by + 0.55f, 1.08f, BAY_W - 0.3f, 0.04f, 0.04f);                   // rail
            for (int r = 0; r < 9; r++) box(steel, bx - 1.25f + r * 0.31f, by + 0.30f, 1.08f, 0.025f, 0.5f, 0.025f);
        }
        final float tx = w * 0.5f - 1.6f;
        for (int s = 0; s < 4; s++) box(steel, tx - 0.5f + (s % 2), h + 0.5f, -DEPTH + 2.2f + (s / 2), 0.06f, 1.0f, 0.06f);
        box(steel, tx, h + 1.02f, -DEPTH + 2.7f, 1.2f, 0.06f, 1.2f);

        // Signboards over each bay
        MeshPartBuilder signsPart = mb.part("signs", GL20.GL_TRIANGLES, a, k.signs);
        for (int i = 0; i < bays; i++) {
            BengaliSignAtlas.Sign s = signs[i];
            if (s == null) continue;
            signQuad(signsPart, x0 + i * BAY_W, GROUND_H - 0.10f, 0.065f, BAY_W - 0.25f, 0.54f,
                BengaliSignAtlas.vBottom(s), BengaliSignAtlas.vTop(s));
        }

        // Upper floors: glazing, then concrete sunshades (chajja), balcony slab and roof edge
        MeshPartBuilder glass = mb.part("glass", GL20.GL_TRIANGLES, a, k.glass);
        for (int f = 0; f < upperFloors; f++) {
            float yc = GROUND_H + f * STOREY_H + 1.45f;
            for (int i = 0; i < bays; i++) box(glass, x0 + i * BAY_W, yc, 0.01f, 1.5f, 1.3f, 0.04f);
        }
        MeshPartBuilder conc = mb.part("concrete", GL20.GL_TRIANGLES, a, k.concrete);
        for (int f = 0; f < upperFloors; f++) {
            float yc = GROUND_H + f * STOREY_H + 1.45f;
            for (int i = 0; i < bays; i++) box(conc, x0 + i * BAY_W, yc + 0.86f, 0.25f, 1.9f, 0.08f, 0.5f);
        }
        if (balcony) box(conc, bx, by, 0.55f, BAY_W - 0.3f, 0.12f, 1.1f);
        box(conc, 0f, h + 0.02f, -DEPTH * 0.5f, w + 0.1f, 0.06f, DEPTH);

        MeshPartBuilder tank = mb.part("tank", GL20.GL_TRIANGLES, a, k.waterTank);
        post(tank, tx, h + 1.05f, -DEPTH + 2.7f, 1.10f, 1.25f, 12);

        return mb.end();
    }

    /** Concrete footpath slab with a kerb, {@code length} along X, {@code depth} along Z, front edge at z = depth/2. */
    public static Model buildFootpath(ModelBuilder mb, StreetPropKit k, float length, float depth) {
        mb.begin();
        MeshPartBuilder conc = mb.part("footpath", GL20.GL_TRIANGLES, StreetPropKit.ATTR, k.concrete);
        box(conc, 0f, 0.09f, 0f, length, 0.18f, depth);
        box(conc, 0f, 0.12f, depth * 0.5f - 0.12f, length, 0.24f, 0.24f);
        return mb.end();
    }
}
