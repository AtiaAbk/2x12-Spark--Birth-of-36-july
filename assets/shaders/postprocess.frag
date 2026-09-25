#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;

uniform sampler2D u_texture;      // Scene render
uniform sampler2D u_bloomTexture;  // Bloom bright pass (blurred)
uniform vec2 u_resolution;

uniform float u_bloomStrength;     // 0.55 default
uniform float u_vignetteStrength;  // 0.60 default
uniform float u_vignetteRadius;    // 0.72 default
uniform float u_exposure;          // 1.15 default
uniform float u_saturation;        // 1.18 default
uniform float u_time;              // for grain animation

// ─────────────────────────────────────────────
// Pseudo-random (for film grain)
// ─────────────────────────────────────────────
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

// ─────────────────────────────────────────────
// ACES filmic tone mapping
// ─────────────────────────────────────────────
vec3 aces(vec3 x) {
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

// ─────────────────────────────────────────────
// Color Grading — warm golden-hour LUT simulation
// shadows → blue-teal lift, highlights → warm amber
// ─────────────────────────────────────────────
vec3 colorGrade(vec3 col) {
    // Shadows: lift toward cool blue-teal (cinematic look)
    vec3 shadowLift = vec3(0.01, 0.02, 0.04);
    // Highlights: push toward warm amber
    vec3 highlightTint = vec3(1.06, 0.98, 0.88);

    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 shadows = mix(col + shadowLift, col, smoothstep(0.0, 0.4, lum));
    vec3 highlights = mix(shadows, shadows * highlightTint, smoothstep(0.5, 1.0, lum));

    return highlights;
}

void main() {
    vec2 uv = v_texCoord;

    // ── 1. Scene colour ──────────────────────
    vec4 scene = texture2D(u_texture, uv);
    vec3 col = scene.rgb;

    // ── 2. Bloom additive composite ──────────
    vec3 bloom = texture2D(u_bloomTexture, uv).rgb;
    col += bloom * u_bloomStrength;

    // ── 3. Exposure ──────────────────────────
    col *= u_exposure;

    // ── 4. Saturation ────────────────────────
    float gray = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(gray), col, u_saturation);

    // ── 5. Color Grade ───────────────────────
    col = colorGrade(col);

    // ── 6. ACES Tone Mapping ─────────────────
    col = aces(col);

    // ── 7. Vignette ──────────────────────────
    vec2 center = uv - 0.5;
    float dist = length(center) / u_vignetteRadius;
    float vignette = 1.0 - smoothstep(0.65, 1.0, dist) * u_vignetteStrength;
    col *= vignette;

    // ── 8. Subtle Chromatic Aberration ───────
    float aberration = 0.0008;
    float r = texture2D(u_texture, uv + vec2( aberration,  0.0)).r;
    float b = texture2D(u_texture, uv + vec2(-aberration,  0.0)).b;
    col.r = mix(col.r, r, 0.45);
    col.b = mix(col.b, b, 0.45);

    // ── 9. Film Grain ─────────────────────────
    float grain = (rand(uv + vec2(u_time * 0.1, u_time * 0.07)) - 0.5) * 0.022;
    col += grain;

    gl_FragColor = vec4(col, 1.0);
}
