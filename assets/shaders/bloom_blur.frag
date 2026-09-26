#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform vec2 u_blurDir;   // (1/w, 0) or (0, 1/h) for ping-pong
uniform float u_radius;    // default 2.0

void main() {
    vec3 result = vec3(0.0);
    // 9-tap symmetric Gaussian weights
    result += texture2D(u_texture, v_texCoord - u_blurDir * (4.0 * u_radius)).rgb * 0.05;
    result += texture2D(u_texture, v_texCoord - u_blurDir * (3.0 * u_radius)).rgb * 0.09;
    result += texture2D(u_texture, v_texCoord - u_blurDir * (2.0 * u_radius)).rgb * 0.12;
    result += texture2D(u_texture, v_texCoord - u_blurDir * (1.0 * u_radius)).rgb * 0.15;
    result += texture2D(u_texture, v_texCoord).rgb * 0.18;
    result += texture2D(u_texture, v_texCoord + u_blurDir * (1.0 * u_radius)).rgb * 0.15;
    result += texture2D(u_texture, v_texCoord + u_blurDir * (2.0 * u_radius)).rgb * 0.12;
    result += texture2D(u_texture, v_texCoord + u_blurDir * (3.0 * u_radius)).rgb * 0.09;
    result += texture2D(u_texture, v_texCoord + u_blurDir * (4.0 * u_radius)).rgb * 0.05;

    gl_FragColor = vec4(result, 1.0);
}
