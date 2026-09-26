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
// ACES filmic tone mapping
vec3 aces(vec3 x) {
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

// ─────────────────────────────────────────────
// Photographic Realistic Color Grading
// - Neutral white point: whites stay crisp & pristine (Curzon cornices, domes, student attire)
// - True blacks and clean neutral shadows (no muddy green or teal cast)
// - Chlorophyll & Foliage Balancing: tames fluorescent chartreuse/yellow-greens into natural lush botanical greens
// - Subtle natural contrast rolloff
// ─────────────────────────────────────────────
vec3 realisticColorGrade(vec3 col) {
    // 1. Natural Rec.709 Luminance
    float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));

    // 2. Foliage & Green Cast Neutralization
    // In CGI, green textures often spike purely in the green channel without the natural
    // red/earthy absorption of real leaves. When G significantly exceeds R, it reads as neon plastic.
    // Here we gently rebalance excess green dominance into natural, rich botanical tones.
    if (col.g > col.r && col.g > col.b) {
        float excessGreen = col.g - max(col.r, col.b);
        col.g -= excessGreen * 0.28; // soften electric green spike
        col.r += excessGreen * 0.09; // add warm earthy chlorophyll warmth
        col.b += excessGreen * 0.04; // slight cool balancing
    }

    // 3. Crisp Neutral Highlights & Clean White Balance
    // Ensure highlights do NOT skew green or yellow:
    vec3 neutralHighlight = vec3(lum);
    col = mix(col, mix(col, neutralHighlight, 0.06), smoothstep(0.70, 1.0, lum));

    // 4. Clean Shadow Contrast
    col = pow(col, vec3(1.04));

    return clamp(col, 0.0, 1.0);
}

void main() {
    vec2 uv = v_texCoord;

    // ── 1. Symmetrical Chromatic Aberration at sample level ──
    vec2 caDir = uv - 0.5;
    float distSq = dot(caDir, caDir);
    vec2 caOffset = caDir * (0.0016 * distSq);

    float r = texture2D(u_texture, uv - caOffset).r;
    float g = texture2D(u_texture, uv).g;
    float b = texture2D(u_texture, uv + caOffset).b;
    vec3 col = vec3(r, g, b);

    // ── 2. Natural Bloom composite ──────────────────────────
    vec3 bloom = texture2D(u_bloomTexture, uv).rgb;
    col += bloom * u_bloomStrength;

    // ── 3. Exposure ──────────────────────────────────────────
    col *= u_exposure;

    // ── 4. Natural Saturation ────────────────────────────────
    float gray = dot(col, vec3(0.2126, 0.7152, 0.0722));
    col = mix(vec3(gray), col, u_saturation);

    // ── 5. ACES Filmic Tone Mapping ──────────────────────────
    col = aces(col);

    // ── 6. Photographic Realistic Color Grade ────────────────
    col = realisticColorGrade(col);

    // ── 7. Soft Natural Vignette ─────────────────────────────
    float vDist = length(caDir) / u_vignetteRadius;
    float vignette = 1.0 - smoothstep(0.60, 1.0, vDist) * u_vignetteStrength;
    col *= vignette;

    // ── 8. Subtle Organic Film Grain ─────────────────────────
    float grain = (rand(uv + vec2(u_time * 0.05, u_time * 0.03)) - 0.5) * 0.012;
    col += grain;

    gl_FragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
