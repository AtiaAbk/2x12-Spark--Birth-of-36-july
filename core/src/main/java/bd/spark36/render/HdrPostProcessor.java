package bd.spark36.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.world.WeatherController;

/**
 * AAA-grade HDR post-processing and tone-mapping pipeline for Spark: Birth of 36 July.
 *
 * <p>Pipeline execution order:
 * <ol>
 *   <li>Scene 3D pass renders directly into linear HDR floating-point FBO (RGBA16F preferred)</li>
 *   <li>Atmospheric sky background and god-ray overlays captured inside FBO</li>
 *   <li>Bloom bright-pass extraction at half-resolution</li>
 *   <li>Separable two-pass Gaussian blur (horizontal then vertical)</li>
 *   <li>Full-screen composite pass: Samples linear HDR scene, composites bloom in linear space,
 *       applies photographic exposure, ACES filmic tone mapping, vignette, and outputs
 *       gamma-corrected sRGB to the backbuffer exactly once</li>
 * </ol>
 *
 * <p>Robust fallback: If floating-point buffers are unsupported on the underlying driver,
 * seamlessly falls back to standard 32-bit RGBA8888 without crashing.</p>
 */
public class HdrPostProcessor implements Disposable {

    private static final String TAG = "HdrPostProcessor";

    // Frame buffers
    private FrameBuffer sceneFBO;    // Linear scene target (RGBA16F or fallback RGBA8888)
    private FrameBuffer bloomFBO;    // Bright-extract pass
    private FrameBuffer blurHFBO;   // Horizontal blur pass
    private FrameBuffer blurVFBO;   // Vertical blur pass (= final bloom)

    // Shaders
    private ShaderProgram bloomExtractShader;
    private ShaderProgram bloomBlurShader;
    private ShaderProgram compositeShader;

    // Full-screen quad mesh
    private Mesh quad;

    // Dimensions
    private int sceneW, sceneH;
    private int bloomW, bloomH;

    // Runtime state
    private boolean valid = false;
    private boolean isHdrFboActive = false;
    private float time = 0f;

    public HdrPostProcessor(int screenW, int screenH) {
        resize(screenW, screenH);
    }

    /**
     * Reallocates framebuffers and recompiles shaders on window resize.
     * Guaranteed leak-free.
     */
    public void resize(int screenW, int screenH) {
        dispose();

        sceneW = Math.max(1, screenW);
        sceneH = Math.max(1, screenH);
        bloomW = Math.max(1, sceneW / 2);
        bloomH = Math.max(1, sceneH / 2);

        GraphicsTier tier = GraphicsTier.fromSystemProperty();

        // 1. Allocate Scene FrameBuffer (try RGBA16F floating-point first)
        boolean requestedHdr = RenderPipelineConfig.hdrEnabled && tier.enableHdr && Gdx.gl30 != null;
        if (requestedHdr) {
            try {
                GLFrameBuffer.FrameBufferBuilder fbb = new GLFrameBuffer.FrameBufferBuilder(sceneW, sceneH);
                // Attach 16-bit half-float RGBA texture
                fbb.addFloatAttachment(GL30.GL_RGBA16F, GL20.GL_RGBA, GL30.GL_HALF_FLOAT, false);
                fbb.addBasicDepthRenderBuffer();
                sceneFBO = fbb.build();
                isHdrFboActive = true;
                Gdx.app.log(TAG, "Floating-point HDR FrameBuffer (RGBA16F) created successfully: " + sceneW + "x" + sceneH);
            } catch (Throwable t) {
                Gdx.app.error(TAG, "RGBA16F HDR FrameBuffer unsupported on this driver, falling back to RGBA8888: " + t.getMessage());
                if (sceneFBO != null) { sceneFBO.dispose(); sceneFBO = null; }
                isHdrFboActive = false;
            }
        }

        // Fallback allocation if HDR FBO failed or wasn't requested
        if (sceneFBO == null) {
            try {
                GLFrameBuffer.FrameBufferBuilder fallbackFbb = new GLFrameBuffer.FrameBufferBuilder(sceneW, sceneH);
                fallbackFbb.addBasicColorTextureAttachment(Format.RGBA8888);
                fallbackFbb.addBasicDepthRenderBuffer();
                sceneFBO = fallbackFbb.build();
                isHdrFboActive = false;
                Gdx.app.log(TAG, "Standard FrameBuffer (RGBA8888) created: " + sceneW + "x" + sceneH);
            } catch (Exception e) {
                Gdx.app.error(TAG, "CRITICAL: Fallback FrameBuffer creation failed: " + e.getMessage());
                valid = false;
                return;
            }
        }

        // 2. Allocate Bloom pass framebuffers (half resolution)
        try {
            bloomFBO = new FrameBuffer(Format.RGBA8888, bloomW, bloomH, false);
            blurHFBO = new FrameBuffer(Format.RGBA8888, bloomW, bloomH, false);
            blurVFBO = new FrameBuffer(Format.RGBA8888, bloomW, bloomH, false);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Bloom FrameBuffer allocation failed: " + e.getMessage());
            valid = false;
            return;
        }

        // 3. Load Post-Processing Shaders
        bloomExtractShader = loadShader("shaders/postprocess.vert", "shaders/bloom_extract.frag");
        bloomBlurShader    = loadShader("shaders/postprocess.vert", "shaders/bloom_blur.frag");
        compositeShader    = loadShader("shaders/postprocess.vert", "shaders/postprocess.frag");

        if (bloomExtractShader == null || bloomBlurShader == null || compositeShader == null) {
            valid = false;
            return;
        }

        // 4. Full-screen quad mesh (NDC -1 to +1, UV 0 to 1)
        quad = new Mesh(true, 4, 6,
            new VertexAttribute(Usage.Position, 2, "a_position"),
            new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
        quad.setVertices(new float[]{
            -1f, -1f, 0f, 0f,
             1f, -1f, 1f, 0f,
             1f,  1f, 1f, 1f,
            -1f,  1f, 0f, 1f
        });
        quad.setIndices(new short[]{0, 1, 2, 2, 3, 0});

        valid = true;
    }

    private ShaderProgram loadShader(String vertPath, String fragPath) {
        com.badlogic.gdx.files.FileHandle vFile = Gdx.files.internal(vertPath);
        if (!vFile.exists()) vFile = Gdx.files.internal("assets/" + vertPath);
        com.badlogic.gdx.files.FileHandle fFile = Gdx.files.internal(fragPath);
        if (!fFile.exists()) fFile = Gdx.files.internal("assets/" + fragPath);

        if (!vFile.exists() || !fFile.exists()) {
            Gdx.app.error(TAG, "Shader files not found: " + vertPath + " / " + fragPath);
            return null;
        }

        String vert = vFile.readString();
        String frag = fFile.readString();
        ShaderProgram.pedantic = false;
        ShaderProgram shader = new ShaderProgram(vert, frag);
        if (!shader.isCompiled()) {
            Gdx.app.error(TAG, "Shader compilation failed [" + fragPath + "]: " + shader.getLog());
            shader.dispose();
            return null;
        }
        return shader;
    }

    /**
     * Begins capturing the 3D scene into the linear HDR FrameBuffer.
     * Call before rendering any 3D world geometry or sky background.
     */
    public void beginCapture() {
        if (!valid) return;
        sceneFBO.begin();
        // Clear to filmic slate-azure sky base
        Gdx.gl.glClearColor(0.24f, 0.42f, 0.66f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Ends capturing into the scene FrameBuffer.
     * Call after 3D world and atmospheric overlays are rendered, but before HUD.
     */
    public void endCapture() {
        if (!valid) return;
        sceneFBO.end();
    }

    /**
     * Executes the post-processing chain with default weather coupling.
     */
    public void render(float delta) {
        render(delta, null);
    }

    /**
     * Executes the full post-processing chain:
     * - Bloom bright extraction & multi-pass Gaussian blur (scaled per GraphicsTier, 0-cost when disabled)
     * - Full-screen composite pass: FXAA 3.11 with early-exit optimization, linear bloom composite,
     *   photographic exposure, dynamic white balance (weather-coupled temperature & tint), shadow lift,
     *   contrast & saturation, ACES / Reinhard / Linear tone mapping, subtle vignette, film grain dither,
     *   and single linear-to-sRGB gamma transfer.
     */
    public void render(float delta, WeatherController weather) {
        if (!valid) return;
        time += delta;

        GraphicsTier tier = GraphicsTier.fromSystemProperty();
        boolean bloomActive = tier.enableBloom && tier.bloomPasses > 0 && RenderPipelineConfig.bloomStrength > 0.001f;

        Texture sceneTexture = sceneFBO.getColorBufferTexture();
        sceneTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        Texture bloomFinal = null;

        if (bloomActive) {
            // ── Pass 1: Bloom bright-extract ─────────────────
            bloomFBO.begin();
            Gdx.gl.glViewport(0, 0, bloomW, bloomH);
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            bloomExtractShader.bind();
            sceneTexture.bind(0);
            bloomExtractShader.setUniformi("u_texture", 0);
            bloomExtractShader.setUniformf("u_threshold", RenderPipelineConfig.bloomThreshold);
            quad.render(bloomExtractShader, GL20.GL_TRIANGLES);
            bloomFBO.end();

            Texture bloomExtracted = bloomFBO.getColorBufferTexture();
            bloomExtracted.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            // ── Pass 2: Horizontal blur ───────────────────────
            blurHFBO.begin();
            Gdx.gl.glViewport(0, 0, bloomW, bloomH);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            bloomBlurShader.bind();
            bloomExtracted.bind(0);
            bloomBlurShader.setUniformi("u_texture", 0);
            bloomBlurShader.setUniformf("u_blurDir", 1.5f / bloomW, 0f);
            bloomBlurShader.setUniformf("u_radius", 2.0f);
            quad.render(bloomBlurShader, GL20.GL_TRIANGLES);
            blurHFBO.end();

            // ── Pass 3: Vertical blur ─────────────────────────
            blurVFBO.begin();
            Gdx.gl.glViewport(0, 0, bloomW, bloomH);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            Texture blurHResult = blurHFBO.getColorBufferTexture();
            blurHResult.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bloomBlurShader.bind();
            blurHResult.bind(0);
            bloomBlurShader.setUniformi("u_texture", 0);
            bloomBlurShader.setUniformf("u_blurDir", 0f, 1.5f / bloomH);
            bloomBlurShader.setUniformf("u_radius", 2.0f);
            quad.render(bloomBlurShader, GL20.GL_TRIANGLES);
            blurVFBO.end();

            // High tier: Second ping-pong blur pass for buttery wide halo
            if (tier.bloomPasses >= 2) {
                blurHFBO.begin();
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                Texture vResult = blurVFBO.getColorBufferTexture();
                vResult.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                bloomBlurShader.bind();
                vResult.bind(0);
                bloomBlurShader.setUniformi("u_texture", 0);
                bloomBlurShader.setUniformf("u_blurDir", 2.5f / bloomW, 0f);
                bloomBlurShader.setUniformf("u_radius", 2.5f);
                quad.render(bloomBlurShader, GL20.GL_TRIANGLES);
                blurHFBO.end();

                blurVFBO.begin();
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                Texture hResult2 = blurHFBO.getColorBufferTexture();
                hResult2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                bloomBlurShader.bind();
                hResult2.bind(0);
                bloomBlurShader.setUniformi("u_texture", 0);
                bloomBlurShader.setUniformf("u_blurDir", 0f, 2.5f / bloomH);
                bloomBlurShader.setUniformf("u_radius", 2.5f);
                quad.render(bloomBlurShader, GL20.GL_TRIANGLES);
                blurVFBO.end();
            }

            bloomFinal = blurVFBO.getColorBufferTexture();
            bloomFinal.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // ── Pass 4: Composite to screen with Tone Mapping & Grading ─────────
        Gdx.gl.glViewport(0, 0, sceneW, sceneH);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float temp = RenderPipelineConfig.colorTemperature;
        float tint = RenderPipelineConfig.colorTint;
        if (weather != null) {
            temp += weather.getCurrentTemperature();
            tint += weather.getCurrentTint();
        }

        compositeShader.bind();
        sceneTexture.bind(0);
        compositeShader.setUniformi("u_texture", 0);

        if (bloomActive && bloomFinal != null) {
            bloomFinal.bind(1);
            compositeShader.setUniformi("u_bloomTexture", 1);
            compositeShader.setUniformf("u_bloomStrength", RenderPipelineConfig.bloomStrength);
        } else {
            // When bloom is disabled, bind fallback and set strength to 0.0 (zero math overhead)
            bloomFBO.getColorBufferTexture().bind(1);
            compositeShader.setUniformi("u_bloomTexture", 1);
            compositeShader.setUniformf("u_bloomStrength", 0.0f);
        }

        compositeShader.setUniformf("u_resolution", sceneW, sceneH);
        compositeShader.setUniformf("u_vignetteStrength", RenderPipelineConfig.vignetteStrength);
        compositeShader.setUniformf("u_vignetteRadius", RenderPipelineConfig.vignetteRadius);
        compositeShader.setUniformf("u_filmGrainStrength", tier.filmGrain > 0f ? tier.filmGrain : RenderPipelineConfig.filmGrainStrength);
        compositeShader.setUniformf("u_exposure", RenderPipelineConfig.exposure);
        compositeShader.setUniformf("u_contrast", RenderPipelineConfig.contrast);
        compositeShader.setUniformf("u_saturation", RenderPipelineConfig.saturation);
        compositeShader.setUniformf("u_colorTemperature", temp);
        compositeShader.setUniformf("u_colorTint", tint);
        compositeShader.setUniformf("u_shadowLift", RenderPipelineConfig.shadowLift);
        compositeShader.setUniformf("u_enableFxaa", (RenderPipelineConfig.fxaaEnabled && tier.enableFxaa) ? 1.0f : 0.0f);
        compositeShader.setUniformi("u_toneMappingMode", RenderPipelineConfig.toneMappingMode);
        compositeShader.setUniformf("u_time", time);
        quad.render(compositeShader, GL20.GL_TRIANGLES);

        // Crucial: Restore active texture to unit 0 for subsequent 2D batch / HUD rendering
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isHdrActive() {
        return isHdrFboActive;
    }

    @Override
    public void dispose() {
        if (sceneFBO != null) { sceneFBO.dispose(); sceneFBO = null; }
        if (bloomFBO != null) { bloomFBO.dispose(); bloomFBO = null; }
        if (blurHFBO != null) { blurHFBO.dispose(); blurHFBO = null; }
        if (blurVFBO != null) { blurVFBO.dispose(); blurVFBO = null; }
        if (bloomExtractShader != null) { bloomExtractShader.dispose(); bloomExtractShader = null; }
        if (bloomBlurShader != null) { bloomBlurShader.dispose(); bloomBlurShader = null; }
        if (compositeShader != null) { compositeShader.dispose(); compositeShader = null; }
        if (quad != null) { quad.dispose(); quad = null; }
        valid = false;
        isHdrFboActive = false;
    }
}
