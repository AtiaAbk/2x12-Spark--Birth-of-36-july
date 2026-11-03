// ei file ta dhakar local bus er 3d model generate kore
package bd.spark36.world.landmarks;

import static bd.spark36.world.landmarks.StreetPropKit.box;
import static bd.spark36.world.landmarks.StreetPropKit.signQuad;
import static bd.spark36.world.landmarks.StreetPropKit.wheel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public final class BusBuilder {

    private BusBuilder() {}

    public static Model build(ModelBuilder mb, StreetPropKit k) {
        mb.begin();
        long a = StreetPropKit.ATTR;
        final float len = 9.8f, wid = 2.45f;
        final float wheelR = 0.50f;

        MeshPartBuilder body = mb.part("body", GL20.GL_TRIANGLES, a, k.busBody);
        box(body, 0f, 1.05f, 0f, wid, 1.10f, len);
        box(body, 0f, 2.90f, 0f, wid, 0.36f, len);
        for (int i = 0; i <= 6; i++) {
            float z = -len * 0.5f + 0.35f + i * 1.45f;
            for (int s = -1; s <= 1; s += 2) box(body, s * (wid * 0.5f - 0.03f), 2.16f, z, 0.06f, 1.12f, 0.16f);
        }
        box(body, 0f, 2.16f, -len * 0.5f + 0.03f, wid, 1.12f, 0.06f);

        MeshPartBuilder blue = mb.part("livery", GL20.GL_TRIANGLES, a, k.accentBlue);
        for (int s = -1; s <= 1; s += 2) box(blue, s * (wid * 0.5f + 0.005f), 1.30f, 0f, 0.02f, 0.26f, len - 0.2f);
        box(blue, 0f, 1.30f, len * 0.5f + 0.005f, wid - 0.2f, 0.26f, 0.02f);

        MeshPartBuilder glass = mb.part("glass", GL20.GL_TRIANGLES, a, k.glass);
        for (int s = -1; s <= 1; s += 2) box(glass, s * (wid * 0.5f - 0.04f), 2.16f, 0f, 0.04f, 1.08f, len - 0.4f);
        box(glass, 0f, 2.06f, len * 0.5f - 0.02f, wid - 0.2f, 1.16f, 0.04f, 0f, -6f, 0f);
        box(glass, 0f, 2.16f, -len * 0.5f - 0.02f, wid - 0.5f, 0.70f, 0.04f);

        box(glass, -wid * 0.5f - 0.005f, 1.25f, len * 0.5f - 1.25f, 0.03f, 2.0f, 0.95f);

        MeshPartBuilder tyres = mb.part("tyres", GL20.GL_TRIANGLES, a, k.tyre);
        for (int s = -1; s <= 1; s += 2) {
            wheel(tyres, s * (wid * 0.5f - 0.22f), wheelR, len * 0.5f - 2.0f, wheelR, 0.34f, 14);
            wheel(tyres, s * (wid * 0.5f - 0.22f), wheelR, -len * 0.5f + 2.3f, wheelR, 0.34f, 14);
        }
        box(tyres, 0f, 0.42f, len * 0.5f + 0.06f, wid, 0.26f, 0.14f);
        box(tyres, 0f, 0.42f, -len * 0.5f - 0.06f, wid, 0.26f, 0.14f);

        MeshPartBuilder chrome = mb.part("chrome", GL20.GL_TRIANGLES, a, k.chrome);
        for (int s = -1; s <= 1; s += 2) {
            box(chrome, s * 0.86f, 0.80f, len * 0.5f + 0.02f, 0.26f, 0.18f, 0.04f);
            box(chrome, s * (wid * 0.5f + 0.18f), 2.10f, len * 0.5f - 0.15f, 0.04f, 0.32f, 0.20f);
        }

        MeshPartBuilder signs = mb.part("route", GL20.GL_TRIANGLES, a, k.signs);
        signQuad(signs, 0f, 2.86f, len * 0.5f + 0.02f, 1.90f, 0.30f,
            BengaliSignAtlas.vBottom(BengaliSignAtlas.Sign.BUS_ROUTE), BengaliSignAtlas.vTop(BengaliSignAtlas.Sign.BUS_ROUTE));

        return mb.end();
    }
}
