package bd.spark36.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.world.WaterConfig;

/**
 * Dedicated high-performance water renderer for the Dhaka University campus pond.
 * Renders a tessellated dynamic wave mesh with dual-scrolling ripple normals,
 * Fresnel reflections, IBL cubemap sampling, and Beer-Lambert depth absorption.
 */
public class WaterRenderer implements Disposable {

    private static final String TAG = "WaterRenderer";

    private Mesh waterMesh;
    private ShaderProgram shader;
    private boolean shaderValid = false;

    private final Matrix4 worldTransform = new Matrix4();
    private final Vector3 tmpSunDir = new Vector3();

    // Subdivision counts for smooth wave curvature (tuned for 60 FPS on Apple Silicon M4)
    private static final int GRID_COLS = 48; // along width X
    private static final int GRID_ROWS = 64; // along length Z

    public WaterRenderer() {
        initMesh();
        initShader();
    }

    private void initMesh() {
        int numVerts = (GRID_COLS + 1) * (GRID_ROWS + 1);
        int numIndices = GRID_COLS * GRID_ROWS * 6;

        float[] vertices = new float[numVerts * 8]; // pos(3) + nor(3) + uv(2)
        short[] indices = new short[numIndices];

        float halfW = WaterConfig.WATER_WIDTH * 0.5f;
        float halfL = WaterConfig.WATER_LENGTH * 0.5f;
        float dx = WaterConfig.WATER_WIDTH / (float) GRID_COLS;
        float dz = WaterConfig.WATER_LENGTH / (float) GRID_ROWS;

        int vertIdx = 0;
        for (int r = 0; r <= GRID_ROWS; r++) {
            float z = -halfL + r * dz;
            float v = (float) r / (float) GRID_ROWS;
            for (int c = 0; c <= GRID_COLS; c++) {
                float x = -halfW + c * dx;
                float u = (float) c / (float) GRID_COLS;

                // Position (X, Y, Z) - Y initialized to base height
                vertices[vertIdx++] = x;
                vertices[vertIdx++] = 0f;
                vertices[vertIdx++] = z;

                // Normal (Nx, Ny, Nz) - pointing upward
                vertices[vertIdx++] = 0f;
                vertices[vertIdx++] = 1f;
                vertices[vertIdx++] = 0f;

                // UV (U, V)
                vertices[vertIdx++] = u;
                vertices[vertIdx++] = v;
            }
        }

        int indexIdx = 0;
        int rowStride = GRID_COLS + 1;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                short v0 = (short) (r * rowStride + c);
                short v1 = (short) (v0 + 1);
                short v2 = (short) ((r + 1) * rowStride + c);
                short v3 = (short) (v2 + 1);

                // Triangle 1
                indices[indexIdx++] = v0;
                indices[indexIdx++] = v2;
                indices[indexIdx++] = v1;

                // Triangle 2
                indices[indexIdx++] = v1;
                indices[indexIdx++] = v2;
                indices[indexIdx++] = v3;
            }
        }

        waterMesh = new Mesh(true, numVerts, numIndices,
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
            new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );

        waterMesh.setVertices(vertices);
        waterMesh.setIndices(indices);

        worldTransform.setToTranslation(WaterConfig.CENTER_X, WaterConfig.BASE_WATER_HEIGHT, WaterConfig.CENTER_Z);
    }

    private void initShader() {
        try {
            com.badlogic.gdx.files.FileHandle vFile = Gdx.files.internal("shaders/water.vert");
            if (!vFile.exists()) vFile = Gdx.files.internal("assets/shaders/water.vert");
            com.badlogic.gdx.files.FileHandle fFile = Gdx.files.internal("shaders/water.frag");
            if (!fFile.exists()) fFile = Gdx.files.internal("assets/shaders/water.frag");

            String vertSrc = vFile.readString();
            String fragSrc = fFile.readString();

            shader = new ShaderProgram(vertSrc, fragSrc);
            if (!shader.isCompiled()) {
                Gdx.app.error(TAG, "Water shader compilation failed:\n" + shader.getLog());
                shaderValid = false;
            } else {
                shaderValid = true;
                Gdx.app.log(TAG, "Realistic water shader compiled and linked successfully.");
            }
        } catch (Exception e) {
            Gdx.app.error(TAG, "Failed to load water shader files", e);
            shaderValid = false;
        }
    }

    /**
     * Renders the water surface inside the active FBO or screen pass.
     */
    public void render(Camera camera, PbrEnvironment pbrEnv, DirectionalShadowLight sunLight,
                       Texture normalTexture, float animTime, GraphicsTier tier) {
        if (!shaderValid || waterMesh == null) return;

        // OpenGL state configuration for transparent water surface
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false); // Read depth to avoid covering underwater objects, do not write depth

        shader.bind();

        // Transforms & Camera
        shader.setUniformMatrix("u_projViewTrans", camera.combined);
        shader.setUniformMatrix("u_worldTrans", worldTransform);
        shader.setUniformf("u_time", animTime);
        shader.setUniformf("u_camPos", camera.position);

        // Water Colors & Depth Absorption
        Color shallow = WaterConfig.SHALLOW_COLOR;
        Color deep = WaterConfig.DEEP_COLOR;
        shader.setUniformf("u_shallowColor", shallow.r, shallow.g, shallow.b, shallow.a);
        shader.setUniformf("u_deepColor", deep.r, deep.g, deep.b, deep.a);
        float waterDepth = WaterConfig.BASE_WATER_HEIGHT - WaterConfig.POND_BED_HEIGHT;
        shader.setUniformf("u_waterDepth", waterDepth);

        // Sun Direction & Color
        if (sunLight != null) {
            tmpSunDir.set(sunLight.direction).scl(-1f).nor(); // direction toward sun
            shader.setUniformf("u_sunDir", tmpSunDir);
            shader.setUniformf("u_sunColor", sunLight.color.r, sunLight.color.g, sunLight.color.b);
        } else {
            shader.setUniformf("u_sunDir", 0.45f, 0.75f, 0.45f);
            shader.setUniformf("u_sunColor", 1.0f, 0.98f, 0.94f);
        }

        // Sky fallback colors
        shader.setUniformf("u_skyZenith", 0.52f, 0.72f, 0.94f);
        shader.setUniformf("u_skyHorizon", 0.80f, 0.86f, 0.92f);

        // Normal Map Texture (Texture unit 0)
        if (normalTexture != null) {
            normalTexture.bind(0);
            shader.setUniformi("u_normalTexture", 0);
        }

        // Specular Radiance Cubemap (Texture unit 1)
        boolean hasCubemap = (tier != GraphicsTier.LOW) && (pbrEnv != null) &&
                             (pbrEnv.getSpecularRadianceMap() != null);
        if (hasCubemap) {
            pbrEnv.getSpecularRadianceMap().bind(1);
            shader.setUniformi("u_specularCubemap", 1);
            shader.setUniformi("u_hasCubemap", 1);
        } else {
            shader.setUniformi("u_hasCubemap", 0);
        }

        // Draw tessellated water surface mesh
        waterMesh.render(shader, GL20.GL_TRIANGLES);

        // Restore OpenGL state
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    }

    public boolean isShaderValid() {
        return shaderValid;
    }

    @Override
    public void dispose() {
        if (waterMesh != null) {
            waterMesh.dispose();
            waterMesh = null;
        }
        if (shader != null) {
            shader.dispose();
            shader = null;
            shaderValid = false;
        }
    }
}
