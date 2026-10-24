package bd.spark36.world.landmarks;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

/**
 * Shared material palette and shape helpers for the Dhaka street-dressing builders
 * (rickshaws, CNG auto-rickshaws, buses, tea stalls, shopfronts, electric poles).
 *
 * <p>Every street prop draws from this one small set of materials, so a {@code ModelCache}
 * holding all of them merges into about one draw call per material, however many props
 * are placed. Colours are written as sRGB (what you would pick from a photo) and stored
 * as linear PBR base colours, because the scene renders in linear space.</p>
 */
public final class StreetPropKit {

    /** One vertex layout for all street props, so the cache can merge across prop types. */
    public static final long ATTR = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

    // Vehicles
    public final Material rickshawRed;     // painted rickshaw body and art panels
    public final Material rickshawHood;    // black vinyl hood
    public final Material accentYellow;    // trim, art panel highlights, bananas
    public final Material accentBlue;      // trim, plastic chairs, bus livery band
    public final Material cngGreen;        // CNG auto-rickshaw body (CNG three-wheelers are green)
    public final Material busBody;         // faded cream bus body
    public final Material steel;           // frames, grilles, cages
    public final Material chrome;          // hubs, lamps, kettle
    public final Material tyre;            // tyres, rubber
    public final Material glass;           // dark glazing

    // Built fabric
    public final Material plasterCream;
    public final Material plasterGreen;
    public final Material plasterRose;
    public final Material concrete;        // footpath, poles, slab edges
    public final Material shutter;         // rolling steel shutters
    public final Material interior;        // dark shop interiors
    public final Material wood;            // stall boards, benches
    public final Material tinRoof;         // corrugated tin
    public final Material tarpBlue;        // plastic tarpaulin awnings
    public final Material waterTank;       // black rooftop plastic water tanks
    public final Material cable;           // overhead cables
    public final Material signs;           // Bengali signboard atlas

    public StreetPropKit(Texture signAtlas) {
        rickshawRed  = mat("street_rickshawRed",  0.72f, 0.16f, 0.12f, 0.0f, 0.55f);
        rickshawHood = mat("street_rickshawHood", 0.10f, 0.10f, 0.11f, 0.0f, 0.45f);
        accentYellow = mat("street_accentYellow", 0.90f, 0.70f, 0.16f, 0.0f, 0.55f);
        accentBlue   = mat("street_accentBlue",   0.16f, 0.36f, 0.66f, 0.0f, 0.55f);
        cngGreen     = mat("street_cngGreen",     0.16f, 0.48f, 0.26f, 0.0f, 0.45f);
        busBody      = mat("street_busBody",      0.82f, 0.78f, 0.66f, 0.0f, 0.60f);
        steel        = mat("street_steel",        0.22f, 0.23f, 0.24f, 0.6f, 0.55f);
        chrome       = mat("street_chrome",       0.72f, 0.72f, 0.70f, 0.9f, 0.30f);
        tyre         = mat("street_tyre",         0.07f, 0.07f, 0.07f, 0.0f, 0.90f);
        glass        = mat("street_glass",        0.10f, 0.13f, 0.15f, 0.0f, 0.15f);

        plasterCream = mat("street_plasterCream", 0.80f, 0.74f, 0.60f, 0.0f, 0.90f);
        plasterGreen = mat("street_plasterGreen", 0.62f, 0.70f, 0.58f, 0.0f, 0.90f);
        plasterRose  = mat("street_plasterRose",  0.78f, 0.62f, 0.56f, 0.0f, 0.90f);
        concrete     = mat("street_concrete",     0.58f, 0.57f, 0.54f, 0.0f, 0.92f);
        shutter      = mat("street_shutter",      0.47f, 0.49f, 0.50f, 0.5f, 0.60f);
        interior     = mat("street_interior",     0.13f, 0.11f, 0.10f, 0.0f, 0.95f);
        wood         = mat("street_wood",         0.45f, 0.31f, 0.19f, 0.0f, 0.80f);
        tinRoof      = mat("street_tinRoof",      0.56f, 0.52f, 0.47f, 0.7f, 0.65f);
        tarpBlue     = mat("street_tarpBlue",     0.15f, 0.40f, 0.72f, 0.0f, 0.70f);
        waterTank    = mat("street_waterTank",    0.09f, 0.09f, 0.10f, 0.0f, 0.60f);
        cable        = mat("street_cable",        0.05f, 0.05f, 0.05f, 0.0f, 0.70f);

        signs = new Material("street_signs");
        signs.set(PBRColorAttribute.createBaseColorFactor(new com.badlogic.gdx.graphics.Color(1f, 1f, 1f, 1f)));
        if (signAtlas != null) signs.set(PBRTextureAttribute.createBaseColorTexture(signAtlas));
        signs.set(PBRFloatAttribute.createMetallic(0f));
        signs.set(PBRFloatAttribute.createRoughness(0.7f));
    }

    /** sRGB channel value (0..1) to linear. */
    public static float lin(float c) {
        return c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }

    /** Opaque PBR material from an sRGB colour. */
    public static Material mat(String id, float r, float g, float b, float metallic, float roughness) {
        Material m = new Material(id);
        m.set(PBRColorAttribute.createBaseColorFactor(new com.badlogic.gdx.graphics.Color(lin(r), lin(g), lin(b), 1f)));
        m.set(PBRFloatAttribute.createMetallic(metallic));
        m.set(PBRFloatAttribute.createRoughness(roughness));
        return m;
    }

    // ── Shape helpers (all use the part's vertex transform, then reset it) ─────────

    private static final Matrix4 TMP = new Matrix4();
    private static final Quaternion Q = new Quaternion();
    private static final Vector3 DIR = new Vector3();
    private static final Vector3 MID = new Vector3();

    /** Axis-aligned box centred at (x, y, z). */
    public static void box(MeshPartBuilder p, float x, float y, float z, float w, float h, float d) {
        p.setVertexTransformationEnabled(false);
        p.box(x, y, z, w, h, d);
    }

    /** Box centred at (x, y, z), rotated by yaw (about Y), then pitch (about X), then roll (about Z), in degrees. */
    public static void box(MeshPartBuilder p, float x, float y, float z, float w, float h, float d,
                           float yawDeg, float pitchDeg, float rollDeg) {
        TMP.idt().translate(x, y, z).rotate(Vector3.Y, yawDeg).rotate(Vector3.X, pitchDeg).rotate(Vector3.Z, rollDeg);
        p.setVertexTransform(TMP);
        p.box(w, h, d);
        p.setVertexTransformationEnabled(false);
    }

    /** Wheel: a cylinder of the given radius and width whose axle runs along X, centred at (x, y, z). */
    public static void wheel(MeshPartBuilder p, float x, float y, float z, float radius, float width, int divisions) {
        TMP.idt().translate(x, y, z).rotate(Vector3.Z, 90f);
        p.setVertexTransform(TMP);
        p.cylinder(radius * 2f, width, radius * 2f, divisions);
        p.setVertexTransformationEnabled(false);
    }

    /** Upright cylinder (axis along Y) with its base at (x, y, z). */
    public static void post(MeshPartBuilder p, float x, float y, float z, float diameter, float height, int divisions) {
        TMP.idt().translate(x, y + height * 0.5f, z);
        p.setVertexTransform(TMP);
        p.cylinder(diameter, height, diameter, divisions);
        p.setVertexTransformationEnabled(false);
    }

    /** Thin square-section rod from a to b (cables, struts, tubes). */
    public static void rod(MeshPartBuilder p, Vector3 a, Vector3 b, float thickness) {
        DIR.set(b).sub(a);
        float len = DIR.len();
        if (len < 1e-4f) return;
        DIR.scl(1f / len);
        MID.set(a).add(b).scl(0.5f);
        Q.setFromCross(Vector3.Z, DIR);
        TMP.set(MID, Q);
        p.setVertexTransform(TMP);
        p.box(thickness, thickness, len);
        p.setVertexTransformationEnabled(false);
    }

    /**
     * Flat, single-sided textured quad (signboards) facing +Z in the builder's local frame:
     * centred at (x, y, z), w wide and h tall, showing atlas rows [vBottom, vTop].
     */
    public static void signQuad(MeshPartBuilder p, float x, float y, float z, float w, float h,
                                float vBottom, float vTop) {
        p.setVertexTransformationEnabled(false);
        // MeshBuilder.rect maps corner00 to v2 and corner01 to v1, so pass (top, bottom).
        p.setUVRange(0f, vTop, 1f, vBottom);
        float hw = w * 0.5f, hh = h * 0.5f;
        p.rect(x - hw, y - hh, z,
               x + hw, y - hh, z,
               x + hw, y + hh, z,
               x - hw, y + hh, z,
               0f, 0f, 1f);
        p.setUVRange(0f, 0f, 1f, 1f);
    }
}
