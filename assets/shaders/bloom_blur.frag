#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform vec2 u_blurDir;   // (1/w, 0) or (0, 1/h) for ping-pong
uniform float u_radius;    // default 2.0

void main() {
    float weights[9];
    weights[0] = 0.0625;
    weights[1] = 0.0938;
    weights[2] = 0.1250;
    weights[3] = 0.1563;
    weights[4] = 0.1250;
    weights[5] = 0.1563;
    weights[6] = 0.1250;
    weights[7] = 0.0938;
    weights[8] = 0.0625;

    vec3 result = vec3(0.0);
    for (int i = 0; i < 9; i++) {
        float offset = float(i - 4) * u_radius;
        result += texture2D(u_texture, v_texCoord + u_blurDir * offset).rgb * weights[i];
    }
    gl_FragColor = vec4(result, 1.0);
}
