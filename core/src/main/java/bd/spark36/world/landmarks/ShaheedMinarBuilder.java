package bd.spark36.world.landmarks;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Procedural 3D builder for the iconic Central Shaheed Minar (কেন্দ্রীয় শহীদ মিনার).
 * Historically accurate proportions:
 * - Central mother pylon (14m tall) tilted forward with arched canopy
 * - Four flanking son pylons (two on each side: 10m and 7.5m)
 * - Deep crimson solar disk behind the central canopy
 * - Multi-tiered polished marble stepped plinth and floral offering altar (শহীদ বেদী)
 */
public class ShaheedMinarBuilder {

    public static Model buildShaheedMinar(ModelBuilder mb, Material marbleMat, Material sunMat,
                                          Material redBrickMat, Material goldMat, long attr) {
        mb.begin();

        // 1. Plinth: Stepped elevated marble plaza (18m x 14m x 1.2m total height)
        MeshPartBuilder pPlaza = mb.part("plaza", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, marbleMat);
        pPlaza.box(0, 0.25f, 0, 22.0f, 0.5f, 16.0f); // Base platform
        pPlaza.box(0, 0.65f, 0, 19.0f, 0.4f, 13.0f); // Middle tier
        pPlaza.box(0, 1.05f, 0, 16.0f, 0.4f, 10.0f); // Upper altar plinth

        // Front ceremonial staircase
        for (int step = 0; step < 4; step++) {
            float sy = 0.15f + step * 0.25f;
            float sz = 8.5f + (3 - step) * 0.45f;
            pPlaza.box(0, sy, sz, 12.0f, 0.25f, 0.5f);
        }

        // 2. Central Mother Pylon: 14m tall, vertical with tilted top canopy
        MeshPartBuilder pPylon = mb.part("pylons", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, marbleMat);
        float pylonThick = 0.55f;

        // Central pylon vertical columns & cross grille
        float cW = 3.6f;
        float cH = 12.5f;
        pPylon.box(-cW * 0.5f, 1.2f + cH * 0.5f, -1.2f, pylonThick, cH, pylonThick);
        pPylon.box(cW * 0.5f, 1.2f + cH * 0.5f, -1.2f, pylonThick, cH, pylonThick);
        // Horizontal cross bars
        for (int bar = 1; bar <= 6; bar++) {
            pPylon.box(0, 1.2f + bar * 1.8f, -1.2f, cW, 0.18f, pylonThick * 0.8f);
        }
        // Top forward-bending canopy arch
        float topY = 1.2f + cH;
        pPylon.box(0, topY + 0.9f, -0.6f, cW + 0.4f, 0.5f, 1.6f);

        // 3. Flanking Side Pylons:
        // Inner flanking pair (height 9.8m)
        float flankInnerX = 3.2f;
        float flankInnerH = 9.8f;
        pPylon.box(-flankInnerX, 1.2f + flankInnerH * 0.5f, -1.4f, 1.4f, flankInnerH, pylonThick);
        pPylon.box(flankInnerX, 1.2f + flankInnerH * 0.5f, -1.4f, 1.4f, flankInnerH, pylonThick);

        // Outer flanking pair (height 7.2m)
        float flankOuterX = 5.2f;
        float flankOuterH = 7.2f;
        pPylon.box(-flankOuterX, 1.2f + flankOuterH * 0.5f, -1.6f, 1.2f, flankOuterH, pylonThick);
        pPylon.box(flankOuterX, 1.2f + flankOuterH * 0.5f, -1.6f, 1.2f, flankOuterH, pylonThick);

        // 4. Brilliant Crimson Solar Disk (represents rising sun of freedom & language/uprising martyrs)
        MeshPartBuilder pSun = mb.part("sun", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, sunMat);
        pSun.box(0, 7.8f, -2.4f, 5.2f, 5.2f, 0.22f);
        pSun.box(0, 7.8f, -2.4f, 3.8f, 5.6f, 0.24f);
        pSun.box(0, 7.8f, -2.4f, 5.6f, 3.8f, 0.24f);

        // 5. Floral offering altar & inscription plaques (শহীদ বেদী)
        MeshPartBuilder pAltar = mb.part("altar", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, redBrickMat);
        pAltar.box(0, 1.4f, 1.5f, 8.0f, 0.35f, 2.2f);

        MeshPartBuilder pPlaque = mb.part("plaque", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, goldMat);
        pPlaque.box(0, 1.45f, 2.65f, 4.2f, 0.20f, 0.08f);

        return mb.end();
    }
}
