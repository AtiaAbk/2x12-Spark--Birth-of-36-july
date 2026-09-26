package bd.spark36.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Disposable;

/**
 * AAA-grade post-processing pipeline for 2x12: Spark.
 * Render pipeline: Scene → FBO → Bloom Extract → Blur H → Blur V → Composite (vignette + color grade + grain + CA)
 */
public class PostProcessor implements Disposable {

    // Frame buffers
    private FrameBuffer sceneFBO;    // Full scene render target
    private FrameBuffer bloomFBO;    // Bright-extract pass
    private FrameBuffer blurHFBO;   // Horizontal blur pass
    private FrameBuffer blurVFBO;   // Vertical blur pass (= final bloom)

    // Shaders
    private ShaderProgram bloomExtractShader;
    private ShaderProgram bloomBlurShader;
    private ShaderProgram compositeShader;

    // Full-screen quad mesh
    private Mesh quad;

    // Dimensions (half-res for bloom)
    private int sceneW, sceneH;
    private int bloomW, bloomH;

    // Parameters
    public float bloomStrength   = 0.30f;
    public float bloomThreshold  = 0.70f;
    public float vignetteStrength = 0.50f;
    public float vignetteRadius  = 0.75f;
    public float exposure        = 1.05f;
    public float saturation      = 1.00f;
    private float time = 0f;

    private boolean valid = false;

    public PostProcessor(int screenW, int screenH) {
        resize(screenW, screenH);
    }

    public void resize(int screenW, int screenH) {
        dispose();
        sceneW = screenW;
        sceneH = screenH;
        bloomW = screenW / 2;
        bloomH = screenH / 2;
        if (bloomW < 1) bloomW = 1;
        if (bloomH < 1) bloomH = 1;

        try {
            sceneFBO  = new FrameBuffer(Format.RGBA8888, sceneW, sceneH, true);
            bloomFBO  = new FrameBuffer(Format.RGBA8888, bloomW, bloomH, false);
            blurHFBO  = new FrameBuffer(Format.RGBA8888, bloomW, bloomH, false);
            blurVFBO  = new FrameBuffer(Format.RGBA8888, bloomW, bloomH, false);
        } catch (Exception e) {
            Gdx.app.error("PostProcessor", "FBO creation failed: " + e.getMessage());
            valid = false;
            return;
        }

        bloomExtractShader = loadShader("shaders/postprocess.vert", "shaders/bloom_extract.frag");
        bloomBlurShader    = loadShader("shaders/postprocess.vert", "shaders/bloom_blur.frag");
        compositeShader    = loadShader("shaders/postprocess.vert", "shaders/postprocess.frag");

        if (bloomExtractShader == null || bloomBlurShader == null || compositeShader == null) {
            valid = false;
            return;
        }

        // Full-screen quad (NDC -1 to +1, UV 0 to 1)
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
        String vert = Gdx.files.internal(vertPath).readString();
        String frag = Gdx.files.internal(fragPath).readString();
        ShaderProgram.pedantic = false;
        ShaderProgram shader = new ShaderProgram(vert, frag);
        if (!shader.isCompiled()) {
            Gdx.app.error("PostProcessor", "Shader compile error [" + fragPath + "]: " + shader.getLog());
            shader.dispose();
            return null;
        }
        return shader;
    }

    /** Begin capturing scene to FBO. Call before any 3D rendering. */
    public void beginCapture() {
        if (!valid) return;
        sceneFBO.begin();
        Gdx.gl.glClearColor(0.40f, 0.60f, 0.82f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    /** End FBO capture. Call after all 3D rendering but BEFORE HUD. */
    public void endCapture() {
        if (!valid) return;
        sceneFBO.end();
    }

    /** Apply post-processing chain and blit to screen. Then caller renders HUD on top. */
    public void render(float delta) {
        if (!valid) return;
        time += delta;

        Texture sceneTexture = sceneFBO.getColorBufferTexture();
        sceneTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Pass 1: Bloom bright-extract ─────────────────
        bloomFBO.begin();
        Gdx.gl.glViewport(0, 0, bloomW, bloomH);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        bloomExtractShader.bind();
        sceneTexture.bind(0);
        bloomExtractShader.setUniformi("u_texture", 0);
        bloomExtractShader.setUniformf("u_threshold", bloomThreshold);
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

        // ── Pass 4: Composite to screen ───────────────────
        Gdx.gl.glViewport(0, 0, sceneW, sceneH);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Texture bloomFinal = blurVFBO.getColorBufferTexture();
        bloomFinal.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        compositeShader.bind();
        sceneTexture.bind(0);
        compositeShader.setUniformi("u_texture", 0);
        bloomFinal.bind(1);
        compositeShader.setUniformi("u_bloomTexture", 1);
        compositeShader.setUniformf("u_resolution", sceneW, sceneH);
        compositeShader.setUniformf("u_bloomStrength", bloomStrength);
        compositeShader.setUniformf("u_vignetteStrength", vignetteStrength);
        compositeShader.setUniformf("u_vignetteRadius", vignetteRadius);
        compositeShader.setUniformf("u_exposure", exposure);
        compositeShader.setUniformf("u_saturation", saturation);
        compositeShader.setUniformf("u_time", time);
        quad.render(compositeShader, GL20.GL_TRIANGLES);

        // Leave unit 0 active: SpriteBatch (particles, HUD text) binds to whatever unit is current
        // but samples unit 0, so a leftover unit 1 made every glyph draw the scene texture.
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    /** Returns whether post-processing is active (shaders compiled successfully). */
    public boolean isValid() { return valid; }

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
    }
}
