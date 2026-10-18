package bd.spark36.world.landmarks;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Procedural 3D builder for military curfew vehicles and checkpoint searchlights:
 * - BTR-80 style 8x8 Armored Personnel Carrier (APC)
 * - Sweeping military searchlight volumetric beam cone
 */
public class MilitaryVehicleBuilder {

    public static Model buildAPC(ModelBuilder mb, Material hullMat, Material wheelMat,
                                 Material turretMat, Material lightMat, long attr) {
        mb.begin();

        // 1. Lower chassis hull
        MeshPartBuilder pHull = mb.part("hull", GL20.GL_TRIANGLES, attr, hullMat);
        float hullL = 6.8f;
        float hullW = 2.8f;
        float hullH = 1.35f;
        float groundClear = 0.55f;

        // Main body box
        pHull.box(0, groundClear + hullH * 0.5f, 0, hullW, hullH, hullL);

        // Angled front glacis armor nose
        pHull.box(0, groundClear + hullH * 0.45f, hullL * 0.5f + 0.45f, hullW * 0.92f, hullH * 0.75f, 0.9f);

        // 2. Eight Heavy Military Wheels (4 on each side)
        MeshPartBuilder pWheels = mb.part("wheels", GL20.GL_TRIANGLES, attr, wheelMat);
        float wheelD = 0.92f;
        float wheelW = 0.38f;
        float wheelSpacing = 1.45f;
        float startZ = -hullL * 0.35f;

        for (int i = 0; i < 4; i++) {
            float z = startZ + i * wheelSpacing;
            // Left wheel
            pWheels.box(-hullW * 0.5f - wheelW * 0.35f, groundClear + wheelD * 0.4f, z, wheelW, wheelD, wheelD);
            // Right wheel
            pWheels.box(hullW * 0.5f + wheelW * 0.35f, groundClear + wheelD * 0.4f, z, wheelW, wheelD, wheelD);
        }

        // 3. Low-profile conical turret on roof
        MeshPartBuilder pTurret = mb.part("turret", GL20.GL_TRIANGLES, attr, turretMat);
        float turretY = groundClear + hullH + 0.32f;
        pTurret.box(0, turretY, 0.4f, 1.4f, 0.65f, 1.4f);
        // Barrel
        pTurret.box(0, turretY + 0.15f, 1.4f, 0.14f, 0.14f, 1.8f);

        // 4. Halogen Searchlight mount
        MeshPartBuilder pLight = mb.part("light", GL20.GL_TRIANGLES, attr, lightMat);
        pLight.box(0.45f, turretY + 0.35f, 0.8f, 0.35f, 0.35f, 0.28f);

        return mb.end();
    }

    /**
     * Builds a semi-transparent sweeping spotlight volumetric beam cone.
     */
    public static Model buildSearchlightBeam(ModelBuilder mb, Material beamMat, float length, float endRadius, long attr) {
        mb.begin();
        MeshPartBuilder p = mb.part("beam", GL20.GL_TRIANGLES, attr, beamMat);
        p.cone(endRadius * 2f, length, endRadius * 2f, 16);
        return mb.end();
    }
}
