package bd.spark36.world.landmarks;

import static bd.spark36.world.landmarks.StreetPropKit.box;
import static bd.spark36.world.landmarks.StreetPropKit.rod;
import static bd.spark36.world.landmarks.StreetPropKit.wheel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * Dhaka cycle rickshaw: single steered front wheel, rider's saddle and handlebar, a raised
 * two-seat passenger bench on a rear axle, painted back and side "rickshaw art" panels and a
 * folding black hood. About 2.5 m long, 1.1 m wide, hood top at 2.0 m.
 *
 * <p>Local frame: origin on the ground under the rear axle centre, front of the vehicle toward +Z.</p>
 */
public final class RickshawBuilder {

    private RickshawBuilder() {}

    /**
     * @param hoodUp true for the hood raised (in service), false for folded back (parked or abandoned)
     */
    public static Model build(ModelBuilder mb, StreetPropKit k, boolean hoodUp) {
        mb.begin();
        long a = StreetPropKit.ATTR;
        final float wheelR = 0.34f;

        // Tyres
        MeshPartBuilder tyres = mb.part("tyres", GL20.GL_TRIANGLES, a, k.tyre);
        wheel(tyres, -0.52f, wheelR, 0f, wheelR, 0.06f, 14);
        wheel(tyres, 0.52f, wheelR, 0f, wheelR, 0.06f, 14);
        wheel(tyres, 0f, wheelR, 1.72f, wheelR, 0.06f, 14);

        // Hubs, rims, handlebar, bell
        MeshPartBuilder chrome = mb.part("chrome", GL20.GL_TRIANGLES, a, k.chrome);
        wheel(chrome, -0.52f, wheelR, 0f, wheelR * 0.82f, 0.03f, 12);
        wheel(chrome, 0.52f, wheelR, 0f, wheelR * 0.82f, 0.03f, 12);
        wheel(chrome, 0f, wheelR, 1.72f, wheelR * 0.82f, 0.03f, 12);
        box(chrome, 0f, 1.10f, 1.62f, 0.62f, 0.03f, 0.03f);

        // Frame: rear axle, chain stay to the steering head, fork, seat post
        MeshPartBuilder steel = mb.part("frame", GL20.GL_TRIANGLES, a, k.steel);
        box(steel, 0f, wheelR, 0f, 1.10f, 0.04f, 0.04f);
        rod(steel, new Vector3(0f, 0.42f, 0.10f), new Vector3(0f, 0.62f, 1.10f), 0.05f);
        rod(steel, new Vector3(0f, 0.62f, 1.10f), new Vector3(0f, wheelR, 1.72f), 0.045f);
        rod(steel, new Vector3(0f, 0.62f, 1.10f), new Vector3(0f, 1.08f, 1.60f), 0.04f);
        rod(steel, new Vector3(0f, 0.62f, 1.10f), new Vector3(0f, 0.96f, 1.02f), 0.04f);
        // Pedal crank
        rod(steel, new Vector3(-0.12f, 0.36f, 1.06f), new Vector3(0.12f, 0.36f, 1.06f), 0.03f);
        // Hood bows
        if (hoodUp) {
            for (int s = -1; s <= 1; s += 2) {
                rod(steel, new Vector3(s * 0.50f, 0.98f, -0.32f), new Vector3(s * 0.50f, 1.95f, -0.42f), 0.025f);
                rod(steel, new Vector3(s * 0.50f, 1.95f, -0.42f), new Vector3(s * 0.50f, 1.86f, 0.30f), 0.025f);
            }
        }

        // Saddle and foot board (black)
        MeshPartBuilder hood = mb.part("hood", GL20.GL_TRIANGLES, a, k.rickshawHood);
        box(hood, 0f, 1.00f, 1.02f, 0.18f, 0.06f, 0.26f);
        box(hood, 0f, 0.84f, -0.08f, 0.94f, 0.12f, 0.46f);        // seat cushion
        box(hood, 0f, 1.10f, -0.30f, 0.94f, 0.36f, 0.08f, 0f, -12f, 0f); // backrest
        if (hoodUp) {
            // Raised folding hood: back, roof and two short sides
            box(hood, 0f, 1.50f, -0.44f, 1.06f, 0.92f, 0.04f, 0f, -8f, 0f);
            box(hood, 0f, 1.93f, -0.07f, 1.06f, 0.04f, 0.74f, 0f, 6f, 0f);
            for (int s = -1; s <= 1; s += 2) {
                box(hood, s * 0.53f, 1.68f, -0.20f, 0.03f, 0.50f, 0.42f);
            }
        } else {
            // Folded hood: a bundled roll behind the backrest
            box(hood, 0f, 1.32f, -0.48f, 1.06f, 0.26f, 0.30f, 0f, -20f, 0f);
        }

        // Painted body: seat box, back art panel, side skirts, mudguards, fork shield
        MeshPartBuilder red = mb.part("body", GL20.GL_TRIANGLES, a, k.rickshawRed);
        box(red, 0f, 0.62f, -0.08f, 0.98f, 0.32f, 0.52f);
        box(red, 0f, 0.66f, -0.40f, 1.00f, 0.42f, 0.04f);
        box(red, 0f, 0.48f, 0.38f, 0.60f, 0.04f, 0.44f);           // foot board
        for (int s = -1; s <= 1; s += 2) {
            box(red, s * 0.52f, 0.72f, 0f, 0.05f, 0.04f, 0.62f, 0f, 0f, 0f); // mudguard
        }
        box(red, 0f, 0.72f, 1.72f, 0.10f, 0.04f, 0.50f);

        // Art trim: yellow and blue bands on the back panel and seat edges
        MeshPartBuilder yellow = mb.part("trimY", GL20.GL_TRIANGLES, a, k.accentYellow);
        box(yellow, 0f, 0.76f, -0.425f, 0.92f, 0.06f, 0.02f);
        box(yellow, 0f, 0.52f, -0.425f, 0.92f, 0.04f, 0.02f);
        box(yellow, 0f, 0.79f, 0.19f, 0.98f, 0.04f, 0.04f);
        MeshPartBuilder blue = mb.part("trimB", GL20.GL_TRIANGLES, a, k.accentBlue);
        box(blue, -0.24f, 0.64f, -0.428f, 0.30f, 0.16f, 0.02f);
        box(blue, 0.24f, 0.64f, -0.428f, 0.30f, 0.16f, 0.02f);

        return mb.end();
    }
}
