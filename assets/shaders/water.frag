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

void main() {
    vec3 V = normalize(v_viewDir);

    // ── Multi-Harmonic Micro-Ripple Tangent Normal Mapping ──────────────────
    // 3 distinct octaves with differing scales, angles and scrolling speeds
    vec2 uv1 = v_texCoord * 4.5 + vec2(u_time * 0.016, u_time * 0.022);
    vec2 uv2 = v_texCoord * 8.0 + vec2(-u_time * 0.024, u_time * 0.017);
    vec2 uv3 = v_texCoord * 15.0 + vec2(u_time * 0.038, -u_time * 0.030);

    vec3 nMap1 = texture2D(u_normalTexture, uv1).rgb * 2.0 - 1.0;
    vec3 nMap2 = texture2D(u_normalTexture, uv2).rgb * 2.0 - 1.0;
    vec3 nMap3 = texture2D(u_normalTexture, uv3).rgb * 2.0 - 1.0;
    vec3 microNormal = normalize(nMap1 * 0.50 + nMap2 * 0.35 + nMap3 * 0.20);

    // Blend analytical wave normal with multi-scale micro ripples
    vec3 N = normalize(v_normal + vec3(microNormal.x * 0.25, 0.0, microNormal.y * 0.25));

    // ── Schlick Fresnel Reflection (IOR = 1.333 for water) ───────────────────
    float NdotV = clamp(dot(N, V), 0.0, 1.0);
    float fresnel = c_fresnelF0 + (1.0 - c_fresnelF0) * pow(1.0 - NdotV, 5.0);

    // ── Environment Reflection (Cubemap or Analytical Daylight Sky) ─────────
    vec3 R = reflect(-V, N);
    vec3 reflectionColor;
    if (u_hasCubemap == 1) {
        reflectionColor = textureCube(u_specularCubemap, R).rgb * 1.15;
    } else {
        float skyT = clamp(R.y * 1.25 + 0.12, 0.0, 1.0);
        reflectionColor = mix(u_skyHorizon, u_skyZenith, skyT);
    }

    // ── Directional Dual-Lobe Sun Specular Glints (Sparkle) ─────────────────
    vec3 H = normalize(u_sunDir + V);
    float NdotH = clamp(dot(N, H), 0.0, 1.0);
    // Pinpoint micro sun glints
    float specSharp = pow(NdotH, 384.0) * 3.2;
    // Focused sun surface sheen
    float specBroad = pow(NdotH, 128.0) * 0.40;
    vec3 sunGlint = u_sunColor * (specSharp + specBroad);

    // ── Subsurface Scattering (Sunlight penetrating wave crests) ────────────
    float sssFactor = pow(clamp(dot(V, -u_sunDir), 0.0, 1.0), 3.5) * (1.0 - NdotV) * 0.32;
    vec3 sssColor = vec3(0.12, 0.55, 0.48) * u_sunColor * sssFactor;

    // ── Underwater Caustic Sunlight Shimmer ─────────────────────────────────
    float c1 = sin((v_worldPos.x + v_worldPos.z) * 3.2 + u_time * 1.4);
    float c2 = cos((v_worldPos.x - v_worldPos.z) * 2.8 - u_time * 1.1);
    float c3 = sin(v_worldPos.x * 5.5 + u_time * 2.0);
    float caustics = pow(clamp(c1 * 0.45 + c2 * 0.45 + c3 * 0.20 + 0.30, 0.0, 1.0), 3.0) * 0.24;

    // ── Depth Absorption & Pond Body Coloration (Beer-Lambert Law) ──────────
    float extinction = 1.0 - exp(-u_waterDepth * 3.5);
    vec3 waterBody = mix(u_shallowColor.rgb, u_deepColor.rgb, extinction);
    waterBody += vec3(caustics * 0.8, caustics, caustics * 0.9);

    // ── Final Optical Composite ─────────────────────────────────────────────
    vec3 finalColor = mix(waterBody, reflectionColor, fresnel) + sunGlint + sssColor;

    // Shoreline edge softening (subtle foam crest against stone curbs)
    if (v_shoreFactor < 0.14) {
        float foam = (1.0 - v_shoreFactor / 0.14);
        float foamLine = pow(foam, 2.0) * 0.22;
        finalColor += vec3(foamLine * 0.95, foamLine * 1.0, foamLine * 0.98);
    }

    float alpha = mix(u_shallowColor.a, 0.96, fresnel);
    gl_FragColor = vec4(finalColor, alpha);
}
