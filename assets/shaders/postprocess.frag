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
// Color Grading — Clean realistic daylight
// Pure neutral shadow definition and clean natural highlights
// ─────────────────────────────────────────────
vec3 colorGrade(vec3 col) {
    // Zero greenish/teal tint: completely clean neutral tones
    vec3 shadowLift = vec3(0.005, 0.005, 0.008);
    vec3 highlightTint = vec3(1.02, 1.01, 1.00);

    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 shadows = mix(col + shadowLift, col, smoothstep(0.0, 0.4, lum));
    vec3 highlights = mix(shadows, shadows * highlightTint, smoothstep(0.5, 1.0, lum));

    return highlights;
}

// ─────────────────────────────────────────────
// FXAA: smooths polygon and foliage edges (the scene renders into a non-multisampled FBO)
// ─────────────────────────────────────────────
vec3 fxaa(vec2 uv, vec2 inv) {
    const vec3 lumaW = vec3(0.299, 0.587, 0.114);
    const float REDUCE_MIN = 1.0 / 128.0;
    const float REDUCE_MUL = 1.0 / 8.0;
    const float SPAN_MAX = 8.0;
    vec3 rgbNW = texture2D(u_texture, uv + vec2(-1.0, -1.0) * inv).rgb;
    vec3 rgbNE = texture2D(u_texture, uv + vec2( 1.0, -1.0) * inv).rgb;
    vec3 rgbSW = texture2D(u_texture, uv + vec2(-1.0,  1.0) * inv).rgb;
    vec3 rgbSE = texture2D(u_texture, uv + vec2( 1.0,  1.0) * inv).rgb;
    vec3 rgbM  = texture2D(u_texture, uv).rgb;
    float lNW = dot(rgbNW, lumaW), lNE = dot(rgbNE, lumaW);
    float lSW = dot(rgbSW, lumaW), lSE = dot(rgbSE, lumaW);
    float lM  = dot(rgbM, lumaW);
    float lMin = min(lM, min(min(lNW, lNE), min(lSW, lSE)));
    float lMax = max(lM, max(max(lNW, lNE), max(lSW, lSE)));

    vec2 dir = vec2(-((lNW + lNE) - (lSW + lSE)), (lNW + lSW) - (lNE + lSE));
    float dirReduce = max((lNW + lNE + lSW + lSE) * (0.25 * REDUCE_MUL), REDUCE_MIN);
    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = clamp(dir * rcpDirMin, vec2(-SPAN_MAX), vec2(SPAN_MAX)) * inv;

    vec3 rgbA = 0.5 * (texture2D(u_texture, uv + dir * (1.0 / 3.0 - 0.5)).rgb +
                       texture2D(u_texture, uv + dir * (2.0 / 3.0 - 0.5)).rgb);
    vec3 rgbB = rgbA * 0.5 + 0.25 * (texture2D(u_texture, uv - dir * 0.5).rgb +
                                     texture2D(u_texture, uv + dir * 0.5).rgb);
    float lB = dot(rgbB, lumaW);
    return (lB < lMin || lB > lMax) ? rgbA : rgbB;
}

void main() {
    vec2 uv = v_texCoord;

    // ── 1. Scene colour ──────────────────────
    vec3 col = fxaa(uv, 1.0 / u_resolution);

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

    // ── 6b. Cinematic S-Curve Contrast & Detail Sharpening ──
    // Rich cinematic contrast curve (deep shadows and vibrant highlights)
    vec3 contrastS = col * col * (3.0 - 2.0 * col);
    col = mix(col, contrastS, 0.45);

    // Clean unsharp masking for razor-sharp foliage and brick texture details
    vec2 invR = 1.0 / u_resolution;
    vec3 n1 = aces(texture2D(u_texture, uv + vec2( invR.x, 0.0)).rgb * u_exposure);
    vec3 n2 = aces(texture2D(u_texture, uv + vec2(-invR.x, 0.0)).rgb * u_exposure);
    vec3 n3 = aces(texture2D(u_texture, uv + vec2(0.0,  invR.y)).rgb * u_exposure);
    vec3 n4 = aces(texture2D(u_texture, uv + vec2(0.0, -invR.y)).rgb * u_exposure);
    vec3 neighborAvg = (n1 + n2 + n3 + n4) * 0.25;
    vec3 sharpDetail = col - neighborAvg;
    col += clamp(sharpDetail * 0.50, -0.07, 0.07);

    // ── 7. Vignette ──────────────────────────
    vec2 center = uv - 0.5;
    float dist = length(center) / u_vignetteRadius;
    float vignette = 1.0 - smoothstep(0.65, 1.0, dist) * u_vignetteStrength;
    col *= vignette;

    // ── 8. Subtle Chromatic Aberration ───────
    float aberration = 0.0008;
    float r = texture2D(u_texture, uv + vec2( aberration,  0.0)).r;
    float b = texture2D(u_texture, uv + vec2(-aberration,  0.0)).b;
    col.r = mix(col.r, r, 0.15);   // light touch: raw samples would reintroduce aliasing
    col.b = mix(col.b, b, 0.15);

    // ── 9. Film Grain ─────────────────────────
    float grain = (rand(uv + vec2(u_time * 0.1, u_time * 0.07)) - 0.5) * 0.010;
    col += grain;

    gl_FragColor = vec4(col, 1.0);
}
