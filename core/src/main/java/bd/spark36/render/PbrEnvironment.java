// ei file ta realistic sunlight, sky ibl ar ambient lighting banay
package bd.spark36.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class PbrEnvironment implements Disposable {

    private static final String TAG = "PbrEnvironment";

    private Cubemap diffuseIrradianceMap;
    private Cubemap specularRadianceMap;
    private Texture brdfLut;
    private boolean initialized = false;

    public PbrEnvironment() {}

    public void setupOutdoorIBL(DirectionalLight sunLight) {
        if (initialized) return;

        if (!RenderPipelineConfig.iblEnabled) {
            Gdx.app.log(TAG, "IBL disabled via RenderPipelineConfig.");
            return;
        }

        try {
            Gdx.app.log(TAG, "Generating outdoor IBL environment maps...");
            IBLBuilder ibl = IBLBuilder.createOutdoor(sunLight);

            ibl.nearSkyColor.set(0.20f, 0.36f, 0.78f, 1f);

            ibl.farSkyColor.set(0.56f, 0.66f, 0.84f, 1f);

            ibl.nearGroundColor.set(0.34f, 0.30f, 0.25f, 1f);
            ibl.farGroundColor.set(0.42f, 0.40f, 0.36f, 1f);

            diffuseIrradianceMap = ibl.buildIrradianceMap(RenderPipelineConfig.IRRADIANCE_MAP_SIZE);

            specularRadianceMap = ibl.buildRadianceMap(RenderPipelineConfig.SPECULAR_MAP_LEVELS);

            brdfLut = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

            ibl.dispose();
            initialized = true;
            Gdx.app.log(TAG, "Outdoor IBL generation complete: Irradiance 32x32, Specular 8 mips, BRDF LUT 256x256.");
        } catch (Throwable t) {
            Gdx.app.error(TAG, "Failed to initialize IBL cubemaps: " + t.getMessage(), t);
            disposeResources();
            initialized = false;
        }
    }

    public void applyTo(Environment env) {
        if (!initialized || env == null) return;

        if (diffuseIrradianceMap != null) {
            env.set(PBRCubemapAttribute.createDiffuseEnv(diffuseIrradianceMap));
        }
        if (specularRadianceMap != null) {
            env.set(PBRCubemapAttribute.createSpecularEnv(specularRadianceMap));
        }
        if (brdfLut != null) {
            env.set(PBRTextureAttribute.createBRDFLookupTexture(brdfLut));
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Cubemap getDiffuseIrradianceMap() {
        return diffuseIrradianceMap;
    }

    public Cubemap getSpecularRadianceMap() {
        return specularRadianceMap;
    }

    public Texture getBrdfLut() {
        return brdfLut;
    }

    private void disposeResources() {
        if (diffuseIrradianceMap != null) {
            diffuseIrradianceMap.dispose();
            diffuseIrradianceMap = null;
        }
        if (specularRadianceMap != null) {
            specularRadianceMap.dispose();
            specularRadianceMap = null;
        }
        if (brdfLut != null) {
            brdfLut.dispose();
            brdfLut = null;
        }
        initialized = false;
    }

    @Override
    public void dispose() {
        disposeResources();
    }
}
