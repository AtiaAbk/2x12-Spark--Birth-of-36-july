package bd.spark36.world.landmarks;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * Procedural 3D builder for historical uprising environmental props:
 * - Student street barricades (wooden planks, steel frames, crossbeams)
 * - Military sandbag revetments and checkpoint bunkers
 * - Emergency volunteer medical triage tent with red crescent emblem
 * - First-aid water bottle supply tables ("Pani lagbe, pani?")
 */
public class BarricadeBuilder {

    /**
     * Builds a student street barricade with horizontal planks, diagonal bracing, and metal support legs.
     */
    public static Model buildBarricade(ModelBuilder mb, float width, float height,
                                       Material woodMat, Material metalMat, long attr) {
        mb.begin();

        // 1. Horizontal planks
        MeshPartBuilder pWood = mb.part("planks", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, woodMat);
        float plankH = 0.22f;
        float plankD = 0.06f;
        int numPlanks = (int) (height / (plankH * 1.6f));
        for (int i = 0; i < numPlanks; i++) {
            float y = 0.25f + i * (plankH * 1.4f);
            pWood.box(0, y, 0, width, plankH, plankD);
        }

        // 2. Diagonal crossbeams
        pWood.box(0, height * 0.5f, plankD * 0.5f, width * 0.95f, plankH * 0.75f, plankD * 0.8f);

        // 3. Heavy upright steel posts and A-frame legs
        MeshPartBuilder pMetal = mb.part("metal", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, metalMat);
        float postW = 0.08f;
        pMetal.box(-width * 0.45f, height * 0.5f, 0, postW, height, postW);
        pMetal.box(width * 0.45f, height * 0.5f, 0, postW, height, postW);

        // A-frame base feet
        float footLen = 0.85f;
        pMetal.box(-width * 0.45f, 0.05f, 0, postW, 0.08f, footLen);
        pMetal.box(width * 0.45f, 0.05f, 0, postW, 0.08f, footLen);

        return mb.end();
    }

    /**
     * Builds a staggered sandbag checkpoint revetment.
     */
    public static Model buildSandbagRevetment(ModelBuilder mb, float length, float height,
                                              Material sandbagMat, long attr) {
        mb.begin();
        MeshPartBuilder p = mb.part("sandbags", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, sandbagMat);

        float bagL = 0.65f;
        float bagH = 0.22f;
        float bagW = 0.38f;

        int layers = (int) (height / bagH);
        int bagsPerLayer = (int) (length / bagL);

        for (int lay = 0; lay < layers; lay++) {
            float y = lay * bagH + bagH * 0.5f;
            float xOffset = (lay % 2 == 0) ? 0f : (bagL * 0.5f);
            for (int b = -bagsPerLayer / 2; b <= bagsPerLayer / 2; b++) {
                float x = b * bagL + xOffset;
                p.box(x, y, 0, bagL * 0.94f, bagH * 0.92f, bagW);
            }
        }

        return mb.end();
    }

    /**
     * Builds an emergency student medical aid triage tent.
     */
    public static Model buildTriageTent(ModelBuilder mb, float size,
                                        Material canvasMat, Material frameMat, Material redCrossMat, long attr) {
        mb.begin();

        // 1. Pyramid Canopy roof
        MeshPartBuilder pCanopy = mb.part("canopy", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, canvasMat);
        float hWall = 2.4f;
        float hRoof = 1.3f;
        float hs = size * 0.5f;

        // Roof slope quads
        Vector3 apex = new Vector3(0, hWall + hRoof, 0);
        Vector3 p0 = new Vector3(-hs, hWall, -hs);
        Vector3 p1 = new Vector3(hs, hWall, -hs);
        Vector3 p2 = new Vector3(hs, hWall, hs);
        Vector3 p3 = new Vector3(-hs, hWall, hs);

        pCanopy.triangle(p0, p1, apex);
        pCanopy.triangle(p1, p2, apex);
        pCanopy.triangle(p2, p3, apex);
        pCanopy.triangle(p3, p0, apex);

        // Three enclosed walls (back, left, right), front open for stretcher access
        pCanopy.box(0, hWall * 0.5f, -hs, size, hWall, 0.05f);
        pCanopy.box(-hs, hWall * 0.5f, 0, 0.05f, hWall, size);
        pCanopy.box(hs, hWall * 0.5f, 0, 0.05f, hWall, size);

        // 2. Corner steel poles
        MeshPartBuilder pFrame = mb.part("frame", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, frameMat);
        float poleW = 0.08f;
        pFrame.box(-hs, hWall * 0.5f, -hs, poleW, hWall, poleW);
        pFrame.box(hs, hWall * 0.5f, -hs, poleW, hWall, poleW);
        pFrame.box(hs, hWall * 0.5f, hs, poleW, hWall, poleW);
        pFrame.box(-hs, hWall * 0.5f, hs, poleW, hWall, poleW);

        // 3. Medical Red Crescent / Cross Emblem on canopy
        MeshPartBuilder pSign = mb.part("sign", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, redCrossMat);
        float signY = hWall + hRoof * 0.45f;
        float signW = 0.55f;
        float signT = 0.16f;
        // Horizontal bar
        pSign.box(0, signY, hs * 0.45f, signW, signT, 0.04f);
        // Vertical bar
        pSign.box(0, signY, hs * 0.45f, signT, signW, 0.04f);

        return mb.end();
    }

    /**
     * Builds a humanitarian water and first-aid supply station.
     */
    public static Model buildWaterSupplyStation(ModelBuilder mb, Material tableMat, Material bottleMat, long attr) {
        mb.begin();

        // Table
        MeshPartBuilder pTable = mb.part("table", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, tableMat);
        pTable.box(0, 0.72f, 0, 1.8f, 0.06f, 0.9f); // table top
        pTable.box(-0.8f, 0.36f, -0.38f, 0.06f, 0.72f, 0.06f); // 4 legs
        pTable.box(0.8f, 0.36f, -0.38f, 0.06f, 0.72f, 0.06f);
        pTable.box(0.8f, 0.36f, 0.38f, 0.06f, 0.72f, 0.06f);
        pTable.box(-0.8f, 0.36f, 0.38f, 0.06f, 0.72f, 0.06f);

        // Water bottles & saline packs stacked on top
        MeshPartBuilder pBottles = mb.part("bottles", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, bottleMat);
        for (int r = -2; r <= 2; r++) {
            for (int c = -1; c <= 1; c++) {
                pBottles.box(r * 0.28f, 0.92f, c * 0.24f, 0.12f, 0.32f, 0.12f);
            }
        }

        return mb.end();
    }
}
