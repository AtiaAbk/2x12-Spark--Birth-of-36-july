// ei file ta curzon hall er daroan ar gate er structure banay
package bd.spark36.world.landmarks;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class OpeningBuilder {

    private static final int ARCH_SEGMENTS = 16;

    public static Model build3DWindowOpening(ModelBuilder mb, float width, float height,
                                             float revealDepth, float architraveWidth,
                                             Material wallMat, Material trimMat, Material glassMat, long attr) {
        mb.begin();

        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float yBot = -hh;
        float yTop = hh;

        float hwIn = 0.87f;
        float yBotIn = yBot + 0.28f;
        float ySpring = yBot + 2.42f;
        float yApex = yBot + 3.72f;
        float rise = yApex - ySpring;

        float d = (rise * rise - hwIn * hwIn) / (2.0f * hwIn);
        float r = hwIn + d;
        float aw = architraveWidth;

        Vector3[] archPoints = new Vector3[ARCH_SEGMENTS + 1];
        Vector3[] archOutVectors = new Vector3[ARCH_SEGMENTS + 1];
        for (int i = 0; i <= ARCH_SEGMENTS; i++) {
            float t = (float) i / (float) ARCH_SEGMENTS;
            float x, y, ux, uy;
            if (t <= 0.5f) {
                float st = t / 0.5f;
                x = MathUtils.lerp(-hwIn, 0f, st);
                float dySq = r * r - (x - d) * (x - d);
                y = ySpring + (dySq > 0 ? (float) Math.sqrt(dySq) : rise);
                ux = (x - d) / r;
                uy = (y - ySpring) / r;
            } else {
                float st = (t - 0.5f) / 0.5f;
                x = MathUtils.lerp(0f, hwIn, st);
                float dySq = r * r - (x + d) * (x + d);
                y = ySpring + (dySq > 0 ? (float) Math.sqrt(dySq) : rise);
                ux = (x + d) / r;
                uy = (y - ySpring) / r;
            }
            archPoints[i] = new Vector3(x, y, 0f);
            archOutVectors[i] = new Vector3(ux, uy, 0f);
        }

        MeshPartBuilder mpbWall = mb.part("brick", GL20.GL_TRIANGLES, attr, wallMat);

        buildQuad(mpbWall,
            -hw, yBot, 0f,
            -hw, yBot, revealDepth,
            -hw, yTop, revealDepth,
            -hw, yTop, 0f,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            hw, yBot, revealDepth,
            hw, yBot, 0f,
            hw, yTop, 0f,
            hw, yTop, revealDepth,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            -hw, yTop, revealDepth,
            hw, yTop, revealDepth,
            hw, yTop, 0f,
            -hw, yTop, 0f,
            0f, 1f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            -hw, yBot, 0f,
            hw, yBot, 0f,
            hw, yBot, revealDepth,
            -hw, yBot, revealDepth,
            0f, -1f, 0f, 0f, 1f, 0f, 1f);

        float outerXLeft = -hwIn - aw;
        float outerXRight = hwIn + aw;

        if (-hw < outerXLeft) {
            buildQuad(mpbWall,
                -hw, yBot, revealDepth,
                outerXLeft, yBot, revealDepth,
                outerXLeft, yTop, revealDepth,
                -hw, yTop, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        if (outerXRight < hw) {
            buildQuad(mpbWall,
                outerXRight, yBot, revealDepth,
                hw, yBot, revealDepth,
                hw, yTop, revealDepth,
                outerXRight, yTop, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        for (int i = 0; i < ARCH_SEGMENTS / 2; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            float op0x = p0.x + u0.x * aw;
            float op0y = p0.y + u0.y * aw;
            float op1x = (i + 1 == ARCH_SEGMENTS / 2) ? 0f : (p1.x + u1.x * aw);
            float op1y = (i + 1 == ARCH_SEGMENTS / 2) ? (p1.y + aw) : (p1.y + u1.y * aw);

            buildQuad(mpbWall,
                outerXLeft, op0y, revealDepth,
                op0x, op0y, revealDepth,
                op1x, op1y, revealDepth,
                outerXLeft, op1y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        for (int i = ARCH_SEGMENTS / 2; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            float op0x = (i == ARCH_SEGMENTS / 2) ? 0f : (p0.x + u0.x * aw);
            float op0y = (i == ARCH_SEGMENTS / 2) ? (p0.y + aw) : (p0.y + u0.y * aw);
            float op1x = p1.x + u1.x * aw;
            float op1y = p1.y + u1.y * aw;

            buildQuad(mpbWall,
                op0x, op0y, revealDepth,
                outerXRight, op0y, revealDepth,
                outerXRight, op1y, revealDepth,
                op1x, op1y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        buildQuad(mpbWall,
            outerXLeft, yApex + aw, revealDepth,
            outerXRight, yApex + aw, revealDepth,
            outerXRight, yTop, revealDepth,
            outerXLeft, yTop, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            outerXLeft, yBot, revealDepth,
            outerXRight, yBot, revealDepth,
            outerXRight, yBotIn, revealDepth,
            outerXLeft, yBotIn, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        MeshPartBuilder mpbTrim = mb.part("trim", GL20.GL_TRIANGLES, attr, trimMat);

        buildQuad(mpbTrim,
            -hwIn, yBotIn, revealDepth,
            -hwIn, yBotIn, 0f,
            -hwIn, ySpring, 0f,
            -hwIn, ySpring, revealDepth,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbTrim,
            hwIn, yBotIn, 0f,
            hwIn, yBotIn, revealDepth,
            hwIn, ySpring, revealDepth,
            hwIn, ySpring, 0f,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        for (int i = 0; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            Vector3 inN = new Vector3(-(u0.x + u1.x) * 0.5f, -(u0.y + u1.y) * 0.5f, 0f).nor();

            buildQuad(mpbTrim,
                p0.x, p0.y, 0f,
                p1.x, p1.y, 0f,
                p1.x, p1.y, revealDepth,
                p0.x, p0.y, revealDepth,
                inN.x, inN.y, inN.z,
                (float) i / ARCH_SEGMENTS, (float) (i + 1) / ARCH_SEGMENTS, 0f, 1f);
        }

        buildQuad(mpbTrim,
            -hwIn - aw, yBotIn, revealDepth,
            -hwIn, yBotIn, revealDepth,
            -hwIn, ySpring, revealDepth,
            -hwIn - aw, ySpring, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        buildQuad(mpbTrim,
            hwIn, yBotIn, revealDepth,
            hwIn + aw, yBotIn, revealDepth,
            hwIn + aw, ySpring, revealDepth,
            hwIn, ySpring, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        for (int i = 0; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];

            float op0x = (i == ARCH_SEGMENTS / 2) ? 0f : (p0.x + u0.x * aw);
            float op0y = (i == ARCH_SEGMENTS / 2) ? (p0.y + aw) : (p0.y + u0.y * aw);
            float op1x = (i + 1 == ARCH_SEGMENTS / 2) ? 0f : (p1.x + u1.x * aw);
            float op1y = (i + 1 == ARCH_SEGMENTS / 2) ? (p1.y + aw) : (p1.y + u1.y * aw);

            buildQuad(mpbTrim,
                p0.x, p0.y, revealDepth,
                p1.x, p1.y, revealDepth,
                op1x, op1y, revealDepth,
                op0x, op0y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        float kw = 0.22f, kh = 0.26f, kd = 0.04f;
        buildBox(mpbTrim, 0f, yApex + aw * 0.5f, revealDepth + kd * 0.5f, kw, kh, kd);

        float sillW = 2.36f;
        float sillH = 0.16f;
        float sillD = revealDepth + 0.14f;
        buildBox(mpbTrim, 0f, yBotIn - sillH * 0.5f, sillD * 0.5f - 0.02f, sillW, sillH, sillD);

        float cbW = 0.18f, cbH = 0.26f, cbD = revealDepth;
        float cbX = 0.70f;
        buildBox(mpbTrim, -cbX, yBotIn - sillH - cbH * 0.5f, cbD * 0.5f, cbW, cbH, cbD);
        buildBox(mpbTrim,  cbX, yBotIn - sillH - cbH * 0.5f, cbD * 0.5f, cbW, cbH, cbD);

        MeshPartBuilder mpbGlass = mb.part("glazing", GL20.GL_TRIANGLES, attr, glassMat);
        buildUprightQuad(mpbGlass, width, height, 0.01f);

        return mb.end();
    }

    public static Model build3DDoorOpening(ModelBuilder mb, float width, float height,
                                           float revealDepth, float architraveWidth,
                                           Material wallMat, Material trimMat, Material doorMat, long attr) {
        mb.begin();

        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float yBot = -hh;
        float yTop = hh;

        float hwIn = 0.87f;
        float yBotIn = yBot;
        float ySpring = yBot + 2.38f;
        float yApex = yBot + 3.74f;
        float rise = yApex - ySpring;

        float d = (rise * rise - hwIn * hwIn) / (2.0f * hwIn);
        float r = hwIn + d;
        float aw = architraveWidth;

        Vector3[] archPoints = new Vector3[ARCH_SEGMENTS + 1];
        Vector3[] archOutVectors = new Vector3[ARCH_SEGMENTS + 1];
        for (int i = 0; i <= ARCH_SEGMENTS; i++) {
            float t = (float) i / (float) ARCH_SEGMENTS;
            float x, y, ux, uy;
            if (t <= 0.5f) {
                float st = t / 0.5f;
                x = MathUtils.lerp(-hwIn, 0f, st);
                float dySq = r * r - (x - d) * (x - d);
                y = ySpring + (dySq > 0 ? (float) Math.sqrt(dySq) : rise);
                ux = (x - d) / r;
                uy = (y - ySpring) / r;
            } else {
                float st = (t - 0.5f) / 0.5f;
                x = MathUtils.lerp(0f, hwIn, st);
                float dySq = r * r - (x + d) * (x + d);
                y = ySpring + (dySq > 0 ? (float) Math.sqrt(dySq) : rise);
                ux = (x + d) / r;
                uy = (y - ySpring) / r;
            }
            archPoints[i] = new Vector3(x, y, 0f);
            archOutVectors[i] = new Vector3(ux, uy, 0f);
        }

        MeshPartBuilder mpbWall = mb.part("brick", GL20.GL_TRIANGLES, attr, wallMat);

        buildQuad(mpbWall,
            -hw, yBot, 0f,
            -hw, yBot, revealDepth,
            -hw, yTop, revealDepth,
            -hw, yTop, 0f,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            hw, yBot, revealDepth,
            hw, yBot, 0f,
            hw, yTop, 0f,
            hw, yTop, revealDepth,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            -hw, yTop, revealDepth,
            hw, yTop, revealDepth,
            hw, yTop, 0f,
            -hw, yTop, 0f,
            0f, 1f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbWall,
            -hw, yBot, 0f,
            hw, yBot, 0f,
            hw, yBot, revealDepth,
            -hw, yBot, revealDepth,
            0f, -1f, 0f, 0f, 1f, 0f, 1f);

        float outerXLeft = -hwIn - aw;
        float outerXRight = hwIn + aw;

        if (-hw < outerXLeft) {
            buildQuad(mpbWall,
                -hw, yBot, revealDepth,
                outerXLeft, yBot, revealDepth,
                outerXLeft, yTop, revealDepth,
                -hw, yTop, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }
        if (outerXRight < hw) {
            buildQuad(mpbWall,
                outerXRight, yBot, revealDepth,
                hw, yBot, revealDepth,
                hw, yTop, revealDepth,
                outerXRight, yTop, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        for (int i = 0; i < ARCH_SEGMENTS / 2; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            float op0x = p0.x + u0.x * aw;
            float op0y = p0.y + u0.y * aw;
            float op1x = (i + 1 == ARCH_SEGMENTS / 2) ? 0f : (p1.x + u1.x * aw);
            float op1y = (i + 1 == ARCH_SEGMENTS / 2) ? (p1.y + aw) : (p1.y + u1.y * aw);

            buildQuad(mpbWall,
                outerXLeft, op0y, revealDepth,
                op0x, op0y, revealDepth,
                op1x, op1y, revealDepth,
                outerXLeft, op1y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        for (int i = ARCH_SEGMENTS / 2; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            float op0x = (i == ARCH_SEGMENTS / 2) ? 0f : (p0.x + u0.x * aw);
            float op0y = (i == ARCH_SEGMENTS / 2) ? (p0.y + aw) : (p0.y + u0.y * aw);
            float op1x = p1.x + u1.x * aw;
            float op1y = p1.y + u1.y * aw;

            buildQuad(mpbWall,
                op0x, op0y, revealDepth,
                outerXRight, op0y, revealDepth,
                outerXRight, op1y, revealDepth,
                op1x, op1y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        buildQuad(mpbWall,
            outerXLeft, yApex + aw, revealDepth,
            outerXRight, yApex + aw, revealDepth,
            outerXRight, yTop, revealDepth,
            outerXLeft, yTop, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        MeshPartBuilder mpbTrim = mb.part("trim", GL20.GL_TRIANGLES, attr, trimMat);

        buildQuad(mpbTrim,
            -hwIn, yBotIn, revealDepth,
            -hwIn, yBotIn, 0f,
            -hwIn, ySpring, 0f,
            -hwIn, ySpring, revealDepth,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbTrim,
            hwIn, yBotIn, 0f,
            hwIn, yBotIn, revealDepth,
            hwIn, ySpring, revealDepth,
            hwIn, ySpring, 0f,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        for (int i = 0; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            Vector3 inN = new Vector3(-(u0.x + u1.x) * 0.5f, -(u0.y + u1.y) * 0.5f, 0f).nor();

            buildQuad(mpbTrim,
                p0.x, p0.y, 0f,
                p1.x, p1.y, 0f,
                p1.x, p1.y, revealDepth,
                p0.x, p0.y, revealDepth,
                inN.x, inN.y, inN.z,
                (float) i / ARCH_SEGMENTS, (float) (i + 1) / ARCH_SEGMENTS, 0f, 1f);
        }

        buildQuad(mpbTrim,
            -hwIn - aw, yBotIn, revealDepth,
            -hwIn, yBotIn, revealDepth,
            -hwIn, ySpring, revealDepth,
            -hwIn - aw, ySpring, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        buildQuad(mpbTrim,
            hwIn, yBotIn, revealDepth,
            hwIn + aw, yBotIn, revealDepth,
            hwIn + aw, ySpring, revealDepth,
            hwIn, ySpring, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        for (int i = 0; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];

            float op0x = (i == ARCH_SEGMENTS / 2) ? 0f : (p0.x + u0.x * aw);
            float op0y = (i == ARCH_SEGMENTS / 2) ? (p0.y + aw) : (p0.y + u0.y * aw);
            float op1x = (i + 1 == ARCH_SEGMENTS / 2) ? 0f : (p1.x + u1.x * aw);
            float op1y = (i + 1 == ARCH_SEGMENTS / 2) ? (p1.y + aw) : (p1.y + u1.y * aw);

            buildQuad(mpbTrim,
                p0.x, p0.y, revealDepth,
                p1.x, p1.y, revealDepth,
                op1x, op1y, revealDepth,
                op0x, op0y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        float kw = 0.24f, kh = 0.28f, kd = 0.04f;
        buildBox(mpbTrim, 0f, yApex + aw * 0.5f, revealDepth + kd * 0.5f, kw, kh, kd);

        float threshW = width + 0.16f;
        float threshH = 0.10f;
        float threshD = revealDepth + 0.08f;
        buildBox(mpbTrim, 0f, yBot + threshH * 0.5f, threshD * 0.5f - 0.02f, threshW, threshH, threshD);

        MeshPartBuilder mpbDoor = mb.part("door", GL20.GL_TRIANGLES, attr, doorMat);
        buildUprightQuad(mpbDoor, width, height, 0.01f);

        return mb.end();
    }

    public static Model build3DGrandPortal(ModelBuilder mb, float width, float height,
                                           float revealDepth, float architraveWidth,
                                           Material frameMat, Material portalMat, long attr) {
        mb.begin();

        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float yBot = -hh;
        float yTop = hh;
        float hwIn = hw * 0.85f;
        float ySpring = yBot + height * 0.65f;
        float yApex = yTop;
        float rise = yApex - ySpring;

        float d = (rise * rise - hwIn * hwIn) / (2.0f * hwIn);
        float r = hwIn + d;
        float aw = architraveWidth;

        MeshPartBuilder mpbFrame = mb.part("frame", GL20.GL_TRIANGLES, attr, frameMat);

        buildQuad(mpbFrame,
            -hwIn, yBot, revealDepth,
            -hwIn, yBot, 0f,
            -hwIn, ySpring, 0f,
            -hwIn, ySpring, revealDepth,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbFrame,
            hwIn, yBot, 0f,
            hwIn, yBot, revealDepth,
            hwIn, ySpring, revealDepth,
            hwIn, ySpring, 0f,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        Vector3[] archPoints = new Vector3[ARCH_SEGMENTS + 1];
        Vector3[] archOutVectors = new Vector3[ARCH_SEGMENTS + 1];
        for (int i = 0; i <= ARCH_SEGMENTS; i++) {
            float t = (float) i / (float) ARCH_SEGMENTS;
            float x, y, ux, uy;
            if (t <= 0.5f) {
                float st = t / 0.5f;
                x = MathUtils.lerp(-hwIn, 0f, st);
                float dySq = r * r - (x - d) * (x - d);
                y = ySpring + (dySq > 0 ? (float) Math.sqrt(dySq) : rise);
                ux = (x - d) / r;
                uy = (y - ySpring) / r;
            } else {
                float st = (t - 0.5f) / 0.5f;
                x = MathUtils.lerp(0f, hwIn, st);
                float dySq = r * r - (x + d) * (x + d);
                y = ySpring + (dySq > 0 ? (float) Math.sqrt(dySq) : rise);
                ux = (x + d) / r;
                uy = (y - ySpring) / r;
            }
            archPoints[i] = new Vector3(x, y, 0f);
            archOutVectors[i] = new Vector3(ux, uy, 0f);
        }

        for (int i = 0; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];
            Vector3 inN = new Vector3(-(u0.x + u1.x) * 0.5f, -(u0.y + u1.y) * 0.5f, 0f).nor();

            buildQuad(mpbFrame,
                p0.x, p0.y, 0f,
                p1.x, p1.y, 0f,
                p1.x, p1.y, revealDepth,
                p0.x, p0.y, revealDepth,
                inN.x, inN.y, inN.z, 0f, 1f, 0f, 1f);
        }

        buildQuad(mpbFrame,
            -hwIn - aw, yBot, revealDepth,
            -hwIn, yBot, revealDepth,
            -hwIn, ySpring, revealDepth,
            -hwIn - aw, ySpring, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        buildQuad(mpbFrame,
            hwIn, yBot, revealDepth,
            hwIn + aw, yBot, revealDepth,
            hwIn + aw, ySpring, revealDepth,
            hwIn, ySpring, revealDepth,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        for (int i = 0; i < ARCH_SEGMENTS; i++) {
            Vector3 p0 = archPoints[i];
            Vector3 p1 = archPoints[i + 1];
            Vector3 u0 = archOutVectors[i];
            Vector3 u1 = archOutVectors[i + 1];

            float op0x = (i == ARCH_SEGMENTS / 2) ? 0f : (p0.x + u0.x * aw);
            float op0y = (i == ARCH_SEGMENTS / 2) ? (p0.y + aw) : (p0.y + u0.y * aw);
            float op1x = (i + 1 == ARCH_SEGMENTS / 2) ? 0f : (p1.x + u1.x * aw);
            float op1y = (i + 1 == ARCH_SEGMENTS / 2) ? (p1.y + aw) : (p1.y + u1.y * aw);

            buildQuad(mpbFrame,
                p0.x, p0.y, revealDepth,
                p1.x, p1.y, revealDepth,
                op1x, op1y, revealDepth,
                op0x, op0y, revealDepth,
                0f, 0f, 1f, 0f, 1f, 0f, 1f);
        }

        MeshPartBuilder mpbPortal = mb.part("portal", GL20.GL_TRIANGLES, attr, portalMat);
        buildUprightQuad(mpbPortal, width, height, 0.01f);

        return mb.end();
    }

    public static Model build3DPorticoTripleWindow(ModelBuilder mb, float totalWidth, float height,
                                                   float revealDepth, float architraveWidth,
                                                   Material frameMat, Material tripleWinMat, long attr) {
        mb.begin();

        float hw = totalWidth * 0.5f;
        float hh = height * 0.5f;
        float yBot = -hh;
        float yTop = hh;
        float aw = architraveWidth;

        MeshPartBuilder mpbFrame = mb.part("frame", GL20.GL_TRIANGLES, attr, frameMat);

        buildQuad(mpbFrame,
            -hw, yBot, revealDepth,
            -hw, yBot, 0f,
            -hw, yTop, 0f,
            -hw, yTop, revealDepth,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbFrame,
            hw, yBot, 0f,
            hw, yBot, revealDepth,
            hw, yTop, revealDepth,
            hw, yTop, 0f,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpbFrame,
            -hw, yTop, revealDepth,
            hw, yTop, revealDepth,
            hw, yTop, 0f,
            -hw, yTop, 0f,
            0f, -1f, 0f, 0f, 1f, 0f, 1f);

        float pierWidth = 0.20f;
        float pier1X = -hw + totalWidth * 0.333f;
        float pier2X = -hw + totalWidth * 0.666f;
        buildBox(mpbFrame, pier1X, 0f, revealDepth * 0.5f, pierWidth, height, revealDepth);
        buildBox(mpbFrame, pier2X, 0f, revealDepth * 0.5f, pierWidth, height, revealDepth);

        MeshPartBuilder mpbWin = mb.part("window", GL20.GL_TRIANGLES, attr, tripleWinMat);
        buildUprightQuad(mpbWin, totalWidth, height, 0.01f);

        return mb.end();
    }

    private static void buildUprightQuad(MeshPartBuilder mpb, float width, float height, float z) {
        float hw = width * 0.5f;
        float hh = height * 0.5f;

        VertexInfo v00 = new VertexInfo().setPos(-hw, -hh, z).setNor(0f, 0f, 1f).setUV(0f, 1f);
        VertexInfo v10 = new VertexInfo().setPos( hw, -hh, z).setNor(0f, 0f, 1f).setUV(1f, 1f);
        VertexInfo v11 = new VertexInfo().setPos( hw,  hh, z).setNor(0f, 0f, 1f).setUV(1f, 0f);
        VertexInfo v01 = new VertexInfo().setPos(-hw,  hh, z).setNor(0f, 0f, 1f).setUV(0f, 0f);

        mpb.rect(v00, v10, v11, v01);
    }

    private static void buildQuad(MeshPartBuilder mpb,
                                  float x00, float y00, float z00,
                                  float x10, float y10, float z10,
                                  float x11, float y11, float z11,
                                  float x01, float y01, float z01,
                                  float nx, float ny, float nz,
                                  float u0, float u1, float v0, float v1) {
        VertexInfo v00 = new VertexInfo().setPos(x00, y00, z00).setNor(nx, ny, nz).setUV(u0, v1);
        VertexInfo v10 = new VertexInfo().setPos(x10, y10, z10).setNor(nx, ny, nz).setUV(u1, v1);
        VertexInfo v11 = new VertexInfo().setPos(x11, y11, z11).setNor(nx, ny, nz).setUV(u1, v0);
        VertexInfo v01 = new VertexInfo().setPos(x01, y01, z01).setNor(nx, ny, nz).setUV(u0, v0);

        mpb.rect(v00, v10, v11, v01);
    }

    private static void buildBox(MeshPartBuilder mpb, float cx, float cy, float cz,
                                 float w, float h, float d) {
        float hw = w * 0.5f;
        float hh = h * 0.5f;
        float hd = d * 0.5f;

        buildQuad(mpb,
            cx - hw, cy - hh, cz + hd,
            cx + hw, cy - hh, cz + hd,
            cx + hw, cy + hh, cz + hd,
            cx - hw, cy + hh, cz + hd,
            0f, 0f, 1f, 0f, 1f, 0f, 1f);

        buildQuad(mpb,
            cx + hw, cy - hh, cz - hd,
            cx - hw, cy - hh, cz - hd,
            cx - hw, cy + hh, cz - hd,
            cx + hw, cy + hh, cz - hd,
            0f, 0f, -1f, 0f, 1f, 0f, 1f);

        buildQuad(mpb,
            cx - hw, cy - hh, cz - hd,
            cx - hw, cy - hh, cz + hd,
            cx - hw, cy + hh, cz + hd,
            cx - hw, cy + hh, cz - hd,
            -1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpb,
            cx + hw, cy - hh, cz + hd,
            cx + hw, cy - hh, cz - hd,
            cx + hw, cy + hh, cz - hd,
            cx + hw, cy + hh, cz + hd,
            1f, 0f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpb,
            cx - hw, cy + hh, cz + hd,
            cx + hw, cy + hh, cz + hd,
            cx + hw, cy + hh, cz - hd,
            cx - hw, cy + hh, cz - hd,
            0f, 1f, 0f, 0f, 1f, 0f, 1f);

        buildQuad(mpb,
            cx - hw, cy - hh, cz - hd,
            cx + hw, cy - hh, cz - hd,
            cx + hw, cy - hh, cz + hd,
            cx - hw, cy - hh, cz + hd,
            0f, -1f, 0f, 0f, 1f, 0f, 1f);
    }
}
