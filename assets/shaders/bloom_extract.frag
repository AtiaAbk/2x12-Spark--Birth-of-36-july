#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform float u_threshold; // linear luminance threshold (default ~0.80)

void main() {
    vec3 col = texture2D(u_texture, v_texCoord).rgb;
    // Linear Rec. 709 luminance
    float brightness = dot(col, vec3(0.2126, 0.7152, 0.0722));

    // Physically-motivated soft-knee threshold (Karis curve)
    float knee = max(u_threshold * 0.5, 0.01);
    float soft = brightness - u_threshold + knee;
    soft = clamp(soft, 0.0, 2.0 * knee);
    soft = soft * soft / (4.0 * knee + 1e-4);
    float contrib = max(soft, brightness - u_threshold) / max(brightness, 1e-4);

    gl_FragColor = vec4(col * contrib, 1.0);
}
