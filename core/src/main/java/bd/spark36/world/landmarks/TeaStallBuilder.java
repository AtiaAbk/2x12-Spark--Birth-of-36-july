package bd.spark36.world.landmarks;

import static bd.spark36.world.landmarks.StreetPropKit.box;
import static bd.spark36.world.landmarks.StreetPropKit.post;
import static bd.spark36.world.landmarks.StreetPropKit.signQuad;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Roadside tea stall ("cha'er dokan"): a raised plank kiosk under a sloping tin roof with a blue
 * tarpaulin awning, glass biscuit jars and an aluminium kettle on a clay stove on the counter, a
 * hanging bunch of bananas, the painted signboard "ভাই ভাই চা ঘর", and a long wooden bench in front.
 * When closed (curfew), the front is boarded up, the jars and kettle are gone and the awning is down.
 *
 * <p>Local frame: origin on the ground at the kiosk centre, serving side toward +Z.
 * Footprint about 1.8 m x 1.3 m plus the bench 1.1 m in front.</p>
 */
public final class TeaStallBuilder {

    private TeaStallBuilder() {}

    public static Model build(ModelBuilder mb, StreetPropKit k, boolean open) {
        mb.begin();
        long a = StreetPropKit.ATTR;
        final float w = 1.8f, d = 1.3f;

        MeshPartBuilder wood = mb.part("wood", GL20.GL_TRIANGLES, a, k.wood);
        box(wood, 0f, 0.55f, 0f, w, 0.10f, d);                       // raised floor
        box(wood, 0f, 1.35f, -d * 0.5f + 0.03f, w, 1.50f, 0.06f);    // back wall
        for (int s = -1; s <= 1; s += 2) {
            box(wood, s * (w * 0.5f - 0.03f), 1.35f, 0f, 0.06f, 1.50f, d);   // side walls
            box(wood, s * (w * 0.5f - 0.05f), 0.25f, d * 0.5f - 0.05f, 0.08f, 0.50f, 0.08f); // stilts
            box(wood, s * (w * 0.5f - 0.05f), 0.25f, -d * 0.5f + 0.05f, 0.08f, 0.50f, 0.08f);
        }
        box(wood, 0f, 0.95f, d * 0.5f - 0.02f, w, 0.70f, 0.04f);     // counter front
        box(wood, 0f, 1.32f, d * 0.5f - 0.15f, w, 0.05f, 0.34f);     // counter top
        if (!open) {
            box(wood, 0f, 1.75f, d * 0.5f, w - 0.1f, 0.80f, 0.04f);  // boarded front
        }
        // Bench in front: plank on four legs
        box(wood, 0f, 0.44f, d * 0.5f + 1.05f, 1.70f, 0.05f, 0.30f);
        for (int s = -1; s <= 1; s += 2) {
            box(wood, s * 0.75f, 0.21f, d * 0.5f + 0.95f, 0.06f, 0.42f, 0.06f);
            box(wood, s * 0.75f, 0.21f, d * 0.5f + 1.15f, 0.06f, 0.42f, 0.06f);
        }

        MeshPartBuilder tin = mb.part("roof", GL20.GL_TRIANGLES, a, k.tinRoof);
        box(tin, 0f, 2.18f, 0.05f, w + 0.3f, 0.03f, d + 0.5f, 0f, 8f, 0f);

        MeshPartBuilder tarp = mb.part("tarp", GL20.GL_TRIANGLES, a, k.tarpBlue);
        if (open) {
            box(tarp, 0f, 2.05f, d * 0.5f + 0.70f, w + 0.2f, 0.02f, 1.10f, 0f, 14f, 0f);
        } else {
            box(tarp, 0f, 1.75f, d * 0.5f + 0.03f, w - 0.1f, 0.80f, 0.02f);   // tarp lashed over the boards
        }

        MeshPartBuilder signs = mb.part("sign", GL20.GL_TRIANGLES, a, k.signs);
        signQuad(signs, 0f, 2.40f, d * 0.5f + 0.14f, 1.90f, 0.30f,
            BengaliSignAtlas.vBottom(BengaliSignAtlas.Sign.TEA_STALL), BengaliSignAtlas.vTop(BengaliSignAtlas.Sign.TEA_STALL));
        MeshPartBuilder signBack = mb.part("signBoard", GL20.GL_TRIANGLES, a, k.steel);
        box(signBack, 0f, 2.40f, d * 0.5f + 0.11f, 1.94f, 0.34f, 0.03f); // front face at +0.125, sign sits 1.5 cm proud

        if (open) {
            MeshPartBuilder glass = mb.part("jars", GL20.GL_TRIANGLES, a, k.glass);
            for (int i = 0; i < 5; i++) post(glass, -0.70f + i * 0.20f, 1.345f, d * 0.5f - 0.15f, 0.15f, 0.24f, 8);
            MeshPartBuilder lids = mb.part("lids", GL20.GL_TRIANGLES, a, k.rickshawRed);
            for (int i = 0; i < 5; i++) post(lids, -0.70f + i * 0.20f, 1.585f, d * 0.5f - 0.15f, 0.12f, 0.04f, 8);
            MeshPartBuilder chrome = mb.part("kettle", GL20.GL_TRIANGLES, a, k.chrome);
            post(chrome, 0.55f, 1.43f, d * 0.5f - 0.15f, 0.22f, 0.22f, 10);
            box(chrome, 0.70f, 1.56f, d * 0.5f - 0.15f, 0.14f, 0.03f, 0.03f, 0f, 0f, 30f);
            MeshPartBuilder clay = mb.part("stove", GL20.GL_TRIANGLES, a, k.rickshawHood);
            post(clay, 0.55f, 1.345f, d * 0.5f - 0.15f, 0.28f, 0.09f, 10);
            MeshPartBuilder bananas = mb.part("bananas", GL20.GL_TRIANGLES, a, k.accentYellow);
            box(bananas, -0.55f, 1.80f, d * 0.5f - 0.05f, 0.20f, 0.36f, 0.18f);
            box(bananas, -0.30f, 1.84f, d * 0.5f - 0.05f, 0.18f, 0.30f, 0.16f);
        }
        return mb.end();
    }
}
