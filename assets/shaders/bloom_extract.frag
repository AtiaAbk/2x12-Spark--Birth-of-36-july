#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform float u_threshold; // default 0.60

void main() {
    vec3 col = texture2D(u_texture, v_texCoord).rgb;
    float brightness = dot(col, vec3(0.299, 0.587, 0.114));
    // Smooth threshold extraction
    float extract = smoothstep(u_threshold, u_threshold + 0.2, brightness);
    gl_FragColor = vec4(col * extract, 1.0);
}
