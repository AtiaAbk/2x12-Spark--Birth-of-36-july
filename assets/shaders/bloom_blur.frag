#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform vec2 u_blurDir;   // (1/w, 0) or (0, 1/h) for ping-pong
uniform float u_radius;    // default 2.0

// 9-tap Gaussian weights
const float weights[9] = float[9](
    0.0625, 0.0938, 0.1250, 0.1563, 0.1250,
    0.1563, 0.1250, 0.0938, 0.0625
);

void main() {
    vec3 result = vec3(0.0);
    for (int i = 0; i < 9; i++) {
        float offset = float(i - 4) * u_radius;
        result += texture2D(u_texture, v_texCoord + u_blurDir * offset).rgb * weights[i];
    }
    gl_FragColor = vec4(result, 1.0);
}
