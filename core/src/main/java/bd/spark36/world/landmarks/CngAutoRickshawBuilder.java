package bd.spark36.world.landmarks;

import static bd.spark36.world.landmarks.StreetPropKit.box;
import static bd.spark36.world.landmarks.StreetPropKit.wheel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Dhaka CNG auto-rickshaw: green three-wheeler with a hard roof, front windscreen, the steel
 * mesh/grille cage on the passenger doors that Dhaka CNGs carry, one front and two rear wheels.
 * About 2.65 m long, 1.30 m wide, 1.72 m tall.
 *
 * <p>Local frame: origin on the ground under the rear axle centre, front toward +Z.</p>
 */
public final class CngAutoRickshawBuilder {

    private CngAutoRickshawBuilder() {}

    public static Model build(ModelBuilder mb, StreetPropKit k) {
        mb.begin();
        long a = StreetPropKit.ATTR;
        final float wheelR = 0.24f;

        MeshPartBuilder tyres = mb.part("tyres", GL20.GL_TRIANGLES, a, k.tyre);
        wheel(tyres, -0.60f, wheelR, 0f, wheelR, 0.14f, 12);
        wheel(tyres, 0.60f, wheelR, 0f, wheelR, 0.14f, 12);
        wheel(tyres, 0f, wheelR, 1.78f, wheelR, 0.14f, 12);
        box(tyres, 0f, 0.86f, -0.66f, 1.22f, 0.10f, 0.06f);   // rear bumper rubber

        MeshPartBuilder green = mb.part("body", GL20.GL_TRIANGLES, a, k.cngGreen);
        box(green, 0f, 0.58f, 0.20f, 1.30f, 0.62f, 1.70f);     // tub
        box(green, 0f, 0.62f, 1.40f, 0.88f, 0.56f, 0.74f);     // driver's nose
        box(green, 0f, 0.46f, 1.86f, 0.40f, 0.30f, 0.24f);     // front fork cowl
        box(green, 0f, 1.68f, 0.24f, 1.32f, 0.08f, 1.84f);     // roof
        box(green, 0f, 1.25f, -0.62f, 1.30f, 0.80f, 0.06f);    // rear panel
        for (int s = -1; s <= 1; s += 2) {
            box(green, s * 0.64f, 1.25f, 1.02f, 0.06f, 0.80f, 0.08f); // A-pillar
            box(green, s * 0.64f, 1.25f, -0.58f, 0.06f, 0.80f, 0.08f); // rear pillar
        }

        MeshPartBuilder glass = mb.part("glass", GL20.GL_TRIANGLES, a, k.glass);
        box(glass, 0f, 1.28f, 1.12f, 1.16f, 0.66f, 0.04f, 0f, 14f, 0f);
        box(glass, 0f, 1.32f, -0.66f, 0.70f, 0.30f, 0.02f);

        // Door cages: vertical bars and two rails on each side
        MeshPartBuilder steel = mb.part("cage", GL20.GL_TRIANGLES, a, k.steel);
        for (int s = -1; s <= 1; s += 2) {
            float x = s * 0.655f;
            box(steel, x, 1.22f, 0.22f, 0.03f, 0.03f, 1.56f);
            box(steel, x, 1.58f, 0.22f, 0.03f, 0.03f, 1.56f);
            for (int i = 0; i < 9; i++) {
                box(steel, x, 1.22f, -0.50f + i * 0.18f, 0.025f, 0.74f, 0.025f);
            }
        }
        box(steel, 0f, 1.76f, 0.24f, 1.10f, 0.04f, 1.20f);      // roof carrier

        MeshPartBuilder chrome = mb.part("chrome", GL20.GL_TRIANGLES, a, k.chrome);
        box(chrome, 0f, 0.74f, 1.99f, 0.17f, 0.17f, 0.05f);     // headlamp
        wheel(chrome, -0.60f, wheelR, 0f, wheelR * 0.6f, 0.15f, 10);
        wheel(chrome, 0.60f, wheelR, 0f, wheelR * 0.6f, 0.15f, 10);
        box(chrome, 0f, 1.02f, 1.20f, 0.56f, 0.03f, 0.03f);     // handlebar

        MeshPartBuilder yellow = mb.part("trim", GL20.GL_TRIANGLES, a, k.accentYellow);
        box(yellow, 0f, 0.30f, 0.20f, 1.32f, 0.06f, 1.70f);     // sill stripe

        return mb.end();
    }
}
