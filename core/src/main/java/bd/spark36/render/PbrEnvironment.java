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

/**
 * Manages Image-Based Lighting (IBL) environment resources for the Spark 36 July PBR pipeline.
 *
 * <p>Builds and owns:
 * <ul>
 *   <li>Diffuse irradiance cubemap (32x32 target) for natural diffuse ambient lighting</li>
 *   <li>Prefiltered specular radiance cubemap (8 mip levels / 256x256 target) for rough-to-smooth reflections</li>
 *   <li>BRDF 2D integration lookup table (split-sum approximation)</li>
 * </ul>
 *
 * <p>Calibrated specifically for outdoor tropical daylight across Dhaka University campus,
 * capturing the azure sky zenith, hazy horizon, and warm terracotta/lawn ground bounce.</p>
 */
public class PbrEnvironment implements Disposable {

    private static final String TAG = "PbrEnvironment";

    private Cubemap diffuseIrradianceMap;
    private Cubemap specularRadianceMap;
    private Texture brdfLut;
    private boolean initialized = false;

    public PbrEnvironment() {}

    /**
     * Initializes IBL cubemaps and BRDF lookup table based on the outdoor sun lighting.
     * Must be called on the GL render thread.
     *
     * @param sunLight Main directional sunlight driving the ambient hemisphere
     */
    public void setupOutdoorIBL(DirectionalLight sunLight) {
        if (initialized) return;

        if (!RenderPipelineConfig.iblEnabled) {
            Gdx.app.log(TAG, "IBL disabled via RenderPipelineConfig.");
            return;
        }

        try {
            Gdx.app.log(TAG, "Generating outdoor IBL environment maps...");
            IBLBuilder ibl = IBLBuilder.createOutdoor(sunLight);

            // Calibrated summer Dhaka campus atmosphere
            // Sky zenith: filmic slate-azure
            ibl.nearSkyColor.set(0.24f, 0.42f, 0.66f, 1f);
            // Sky horizon: soft pearlescent mist
            ibl.farSkyColor.set(0.68f, 0.78f, 0.86f, 1f);
            // Ground bounce: warm terracotta brick / lush monsoon grass reflection
            ibl.nearGroundColor.set(0.38f, 0.32f, 0.25f, 1f);
            ibl.farGroundColor.set(0.44f, 0.40f, 0.35f, 1f);

            // Build 32x32 diffuse irradiance cubemap
            diffuseIrradianceMap = ibl.buildIrradianceMap(RenderPipelineConfig.IRRADIANCE_MAP_SIZE);

            // Build prefiltered specular radiance cubemap (8 levels)
            specularRadianceMap = ibl.buildRadianceMap(RenderPipelineConfig.SPECULAR_MAP_LEVELS);

            // Load precomputed BRDF integration LUT from gdx-gltf classpath
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

    /**
     * Applies IBL attributes to the target LibGDX Environment.
     *
     * @param env Active 3D environment for the scene
     */
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
