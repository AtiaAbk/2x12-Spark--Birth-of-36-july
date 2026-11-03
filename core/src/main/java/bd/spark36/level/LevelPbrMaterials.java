// ei file ta level er pbr material ar texture bind kore
package bd.spark36.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.Array;
import bd.spark36.world.landmarks.StreetPropKit;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;

public final class LevelPbrMaterials {

    private LevelPbrMaterials() {}

    public static void adopt(Array<ModelInstance> instances) {
        for (ModelInstance mi : instances) {
            for (Material m : mi.materials) adopt(m);
        }
    }

    public static void adopt(Material m) {
        if (m.has(PBRColorAttribute.BaseColorFactor)) return;
        ColorAttribute diff = (ColorAttribute) m.get(ColorAttribute.Diffuse);
        if (diff == null) return;
        Color c = diff.color;
        m.set(PBRColorAttribute.createBaseColorFactor(new Color(
            StreetPropKit.lin(c.r), StreetPropKit.lin(c.g), StreetPropKit.lin(c.b), c.a)));
        if (!m.has(PBRFloatAttribute.Metallic)) m.set(PBRFloatAttribute.createMetallic(0f));
        if (!m.has(PBRFloatAttribute.Roughness)) m.set(PBRFloatAttribute.createRoughness(0.8f));
    }
}
