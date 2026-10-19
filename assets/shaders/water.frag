#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_worldPos;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec2 v_texCoord;
varying float v_shoreFactor;

uniform sampler2D u_normalTexture;
uniform samplerCube u_specularCubemap;
uniform int u_hasCubemap;

uniform vec3 u_sunDir;
uniform vec3 u_sunColor;
uniform vec4 u_shallowColor;
uniform vec4 u_deepColor;
uniform vec3 u_skyZenith;
uniform vec3 u_skyHorizon;
uniform float u_time;
uniform float u_waterDepth;

const float c_fresnelF0 = 0.020;
const float c_shininess = 256.0;

void main() {
    vec3 V = normalize(v_viewDir);

    // ── Dual-Scrolling Micro-Ripple Tangent Normal Mapping ──────────────────
    vec2 uv1 = v_texCoord * 4.0 + vec2(u_time * 0.015, u_time * 0.022);
    vec2 uv2 = v_texCoord * 6.5 + vec2(-u_time * 0.020, u_time * 0.014);

    vec3 nMap1 = texture2D(u_normalTexture, uv1).rgb * 2.0 - 1.0;
    vec3 nMap2 = texture2D(u_normalTexture, uv2).rgb * 2.0 - 1.0;
    vec3 microNormal = normalize(nMap1 + nMap2);

    // Blend analytical wave normal with micro ripples
    vec3 N = normalize(v_normal + vec3(microNormal.x * 0.22, 0.0, microNormal.y * 0.22));

    // ── Schlick Fresnel Reflection ──────────────────────────────────────────
    float NdotV = clamp(dot(N, V), 0.0, 1.0);
    float fresnel = c_fresnelF0 + (1.0 - c_fresnelF0) * pow(1.0 - NdotV, 5.0);

    // ── Environment Reflection (Cubemap or Analytical Daylight Sky) ─────────
    vec3 R = reflect(-V, N);
    vec3 reflectionColor;
    if (u_hasCubemap == 1) {
        reflectionColor = textureCube(u_specularCubemap, R).rgb * 1.15;
    } else {
        float skyT = clamp(R.y * 1.2 + 0.1, 0.0, 1.0);
        reflectionColor = mix(u_skyHorizon, u_skyZenith, skyT);
    }

    // ── Directional Sun Specular Highlight (Sparkle) ────────────────────────
    vec3 H = normalize(u_sunDir + V);
    float NdotH = clamp(dot(N, H), 0.0, 1.0);
    float spec = pow(NdotH, c_shininess) * 2.6;
    vec3 sunGlint = u_sunColor * spec;

    // ── Depth Absorption & Pond Body Coloration ─────────────────────────────
    // Beer-Lambert absorption through water depth
    float extinction = 1.0 - exp(-u_waterDepth * 3.2);
    vec3 waterBody = mix(u_shallowColor.rgb, u_deepColor.rgb, extinction);

    // ── Final Optical Composite ─────────────────────────────────────────────
    vec3 finalColor = mix(waterBody, reflectionColor, fresnel) + sunGlint;

    // Shoreline edge softening (subtle foam crest against stone curbs)
    if (v_shoreFactor < 0.12) {
        float foam = (1.0 - v_shoreFactor / 0.12) * 0.18;
        finalColor += vec3(foam);
    }

    float alpha = mix(u_shallowColor.a, 0.96, fresnel);
    gl_FragColor = vec4(finalColor, alpha);
}
