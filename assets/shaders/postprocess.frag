#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;

uniform sampler2D u_texture;      // Linear HDR scene render
uniform sampler2D u_bloomTexture;  // Linear bloom bright pass (Gaussian blurred)
uniform vec2 u_resolution;

uniform float u_bloomStrength;     // Bloom contribution
uniform float u_vignetteStrength;  // Edge darkening intensity
uniform float u_vignetteRadius;    // Radial vignette falloff
uniform float u_exposure;          // Linear photographic exposure multiplier
uniform float u_saturation;        // Saturation adjustment in linear space
uniform float u_time;              // Subtle temporal film grain animation

// ─────────────────────────────────────────────
// Pseudo-random generator (for organic film grain)
// ─────────────────────────────────────────────
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

// ─────────────────────────────────────────────
// ACES Filmic Tone Mapping (Stephen Hill / Krzysztof Narkowicz fit)
// Produces natural highlight roll-off and rich shadow contrast without crushing blacks.
// ─────────────────────────────────────────────
vec3 acesToneMapping(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

// ─────────────────────────────────────────────
// Fast Approximate Anti-Aliasing (FXAA 3.11 quality pass)
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

// ─────────────────────────────────────────────
// Output Transfer Function: Linear to Display sRGB / Gamma
// Applied exactly once at the final output stage.
// ─────────────────────────────────────────────
vec3 linearToDisplayGamma(vec3 linColor) {
    // Standard IEC 61966-2-1 display gamma transfer
    vec3 bLess = step(vec3(0.0031308), linColor);
    return mix(linColor * 12.92, 1.055 * pow(linColor, vec3(1.0 / 2.4)) - 0.055, bLess);
}

void main() {
    vec2 uv = v_texCoord;

    // ── 1. Sample linear HDR scene colour (FXAA edge-smoothed) ──
    vec3 linearScene = fxaa(uv, 1.0 / u_resolution);

    // ── 2. Linear Bloom composite ──────────────────────────────
    vec3 linearBloom = texture2D(u_bloomTexture, uv).rgb;
    linearScene += linearBloom * u_bloomStrength;

    // ── 3. Photographic Exposure ───────────────────────────────
    linearScene *= max(u_exposure, 0.01);

    // ── 4. Saturation adjustment in linear space ───────────────
    float linearLum = dot(linearScene, vec3(0.2126, 0.7152, 0.0722));
    linearScene = mix(vec3(linearLum), linearScene, u_saturation);

    // ── 5. ACES Filmic Tone Mapping ────────────────────────────
    // Compresses [0, inf) dynamic range into smooth [0, 1] filmic rolloff
    vec3 toneMapped = acesToneMapping(linearScene);

    // ── 6. Vignette (optical lens edge attenuation) ────────────
    vec2 center = uv - 0.5;
    float dist = length(center) / max(u_vignetteRadius, 0.1);
    float vignette = 1.0 - smoothstep(0.65, 1.0, dist) * u_vignetteStrength;
    toneMapped *= vignette;

    // ── 7. Subtle film grain ───────────────────────────────────
    float grain = (rand(uv + vec2(u_time * 0.08, u_time * 0.05)) - 0.5) * 0.008;
    toneMapped = clamp(toneMapped + grain, 0.0, 1.0);

    // ── 8. Single Display Color Space Transfer (Linear -> sRGB) ─
    vec3 finalOutput = linearToDisplayGamma(toneMapped);

    gl_FragColor = vec4(finalOutput, 1.0);
}
