#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;

uniform sampler2D u_texture;          // Linear HDR scene render
uniform sampler2D u_bloomTexture;      // Linear bloom bright pass (Gaussian blurred)
uniform vec2 u_resolution;

// Bloom & Optics
uniform float u_bloomStrength;         // Bloom contribution multiplier
uniform float u_vignetteStrength;      // Edge darkening intensity (subtle)
uniform float u_vignetteRadius;        // Radial vignette falloff
uniform float u_filmGrainStrength;     // Film grain dither intensity

// Color Grading & Exposure
uniform float u_exposure;              // Linear photographic exposure multiplier
uniform float u_contrast;              // Midpoint-anchored contrast
uniform float u_saturation;            // Saturation adjustment in linear space
uniform float u_colorTemperature;      // White balance temperature shift (-1 to 1)
uniform float u_colorTint;             // White balance tint shift (-1 to 1)
uniform float u_shadowLift;            // Shadow detail recovery

// Anti-aliasing & Tone Mapping Modes
uniform float u_enableFxaa;            // 1.0 = on, 0.0 = off
uniform int   u_toneMappingMode;       // 1 = ACES Filmic, 2 = Reinhard, 0 = Linear clamp
uniform float u_time;                  // Temporal grain seed

// ─────────────────────────────────────────────
// Pseudo-random generator (for organic film grain & dithering)
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
// Extended Reinhard Tone Mapping
// ─────────────────────────────────────────────
vec3 reinhardToneMapping(vec3 x) {
    const float whitePoint2 = 9.0; // 3.0 squared
    return clamp((x * (1.0 + x / whitePoint2)) / (1.0 + x), 0.0, 1.0);
}

// ─────────────────────────────────────────────
// Fast Approximate Anti-Aliasing (FXAA 3.11 with early-exit contrast optimization)
// ─────────────────────────────────────────────
vec3 fxaa(vec2 uv, vec2 inv) {
    const vec3 lumaW = vec3(0.2126, 0.7152, 0.0722);
    const float REDUCE_MIN = 1.0 / 128.0;
    const float REDUCE_MUL = 1.0 / 8.0;
    const float SPAN_MAX = 8.0;

    vec3 rgbM  = texture2D(u_texture, uv).rgb;
    vec3 rgbNW = texture2D(u_texture, uv + vec2(-1.0, -1.0) * inv).rgb;
    vec3 rgbNE = texture2D(u_texture, uv + vec2( 1.0, -1.0) * inv).rgb;
    vec3 rgbSW = texture2D(u_texture, uv + vec2(-1.0,  1.0) * inv).rgb;
    vec3 rgbSE = texture2D(u_texture, uv + vec2( 1.0,  1.0) * inv).rgb;

    float lM  = dot(rgbM, lumaW);
    float lNW = dot(rgbNW, lumaW);
    float lNE = dot(rgbNE, lumaW);
    float lSW = dot(rgbSW, lumaW);
    float lSE = dot(rgbSE, lumaW);

    float lMin = min(lM, min(min(lNW, lNE), min(lSW, lSE)));
    float lMax = max(lM, max(max(lNW, lNE), max(lSW, lSE)));
    float range = lMax - lMin;

    // Early exit on flat/low-contrast regions: preserves 100% texture sharpness on bricks & foliage
    if (range < max(1.0 / 16.0, lMax * 0.125)) {
        return rgbM;
    }

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
// White Balance: Temperature & Tint adjustment
// ─────────────────────────────────────────────
vec3 adjustTemperatureAndTint(vec3 col, float temp, float tint) {
    vec3 filter = vec3(1.0 + temp * 0.15, 1.0 + tint * 0.06, 1.0 - temp * 0.15);
    return max(vec3(0.0), col * filter);
}

// ─────────────────────────────────────────────
// Shadow recovery (lifts deep darks without blowing out highlights or blanching colors)
// ─────────────────────────────────────────────
vec3 liftShadows(vec3 col, float lift) {
    if (lift <= 0.0001) return col;
    float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
    float factor = 1.0 - smoothstep(0.0, 0.35, lum);
    vec3 tint = mix(col / max(lum, 0.02), vec3(1.0), 0.35);
    return max(vec3(0.0), col + tint * (lift * factor));
}

// ─────────────────────────────────────────────
// Contrast adjustment around linear midpoint (0.18)
// ─────────────────────────────────────────────
vec3 adjustContrast(vec3 col, float contrast) {
    return max(vec3(0.0), (col - vec3(0.18)) * contrast + vec3(0.18));
}

// ─────────────────────────────────────────────
// Output Transfer Function: Linear to Display sRGB / Gamma
// Applied exactly once at the final output stage.
// ─────────────────────────────────────────────
vec3 linearToDisplayGamma(vec3 linColor) {
    vec3 bLess = step(vec3(0.0031308), linColor);
    return mix(linColor * 12.92, 1.055 * pow(linColor, vec3(1.0 / 2.4)) - 0.055, bLess);
}

void main() {
    vec2 uv = v_texCoord;

    // ── 1. Sample linear HDR scene colour (FXAA edge-smoothed or point) ──
    vec3 linearScene;
    if (u_enableFxaa > 0.5) {
        linearScene = fxaa(uv, 1.0 / u_resolution);
    } else {
        linearScene = texture2D(u_texture, uv).rgb;
    }

    // ── 2. Linear Bloom composite ──────────────────────────────
    if (u_bloomStrength > 0.001) {
        vec3 linearBloom = texture2D(u_bloomTexture, uv).rgb;
        linearScene += linearBloom * u_bloomStrength;
    }

    // ── 3. Photographic Exposure ───────────────────────────────
    linearScene *= max(u_exposure, 0.01);

    // ── 4. White Balance (Temperature & Tint) ──────────────────
    linearScene = adjustTemperatureAndTint(linearScene, u_colorTemperature, u_colorTint);

    // ── 5. Shadow Detail Recovery (Lift) ───────────────────────
    linearScene = liftShadows(linearScene, u_shadowLift);

    // ── 6. Contrast adjustment ─────────────────────────────────
    linearScene = adjustContrast(linearScene, u_contrast);

    // ── 7. Saturation adjustment in linear space ───────────────
    float linearLum = dot(linearScene, vec3(0.2126, 0.7152, 0.0722));
    linearScene = max(vec3(0.0), mix(vec3(linearLum), linearScene, u_saturation));

    // ── 8. Filmic Tone Mapping ─────────────────────────────────
    vec3 toneMapped;
    if (u_toneMappingMode == 1) {
        toneMapped = acesToneMapping(linearScene);
    } else if (u_toneMappingMode == 2) {
        toneMapped = reinhardToneMapping(linearScene);
    } else {
        toneMapped = clamp(linearScene, 0.0, 1.0);
    }

    // ── 9. Vignette (optical lens edge attenuation) ────────────
    if (u_vignetteStrength > 0.001) {
        vec2 center = uv - 0.5;
        float dist = length(center) / max(u_vignetteRadius, 0.1);
        float vignette = 1.0 - smoothstep(0.60, 1.0, dist) * u_vignetteStrength;
        toneMapped *= vignette;
    }

    // ── 10. Subtle organic film grain / dither ─────────────────
    if (u_filmGrainStrength > 0.0001) {
        float grain = (rand(uv + vec2(u_time * 0.08, u_time * 0.05)) - 0.5) * u_filmGrainStrength;
        toneMapped = clamp(toneMapped + grain, 0.0, 1.0);
    }

    // ── 11. Single Display Color Space Transfer (Linear -> sRGB) ─
    vec3 finalOutput = linearToDisplayGamma(toneMapped);

    gl_FragColor = vec4(finalOutput, 1.0);
}
