attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform float u_time;
uniform vec3 u_camPos;

// Wave parameters matching WaterConfig & WaterWaveModel
// vec4(amplitude, wavelength, speed, steepness)
const vec4 c_wave1 = vec4(0.016, 3.80, 0.35, 0.40);
const vec2 c_dir1 = vec2(0.80, 0.60);

const vec4 c_wave2 = vec4(0.009, 2.20, 0.45, 0.35);
const vec2 c_dir2 = vec2(-0.50, 0.866);

const vec4 c_wave3 = vec4(0.005, 1.30, 0.55, 0.30);
const vec2 c_dir3 = vec2(0.30, -0.954);

const float c_pi2 = 6.2831853;
const float c_shoreMargin = 0.85;
const vec4 c_pondBounds = vec4(-8.8, -5.8, 8.8, 17.8); // minX, minZ, maxX, maxZ

varying vec3 v_worldPos;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec2 v_texCoord;
varying float v_shoreFactor;

void main() {
    vec4 worldPos = u_worldTrans * a_position;
    v_texCoord = a_texCoord0;

    // Calculate distance to stone curbs for shoreline wave dampening
    float distW = worldPos.x - c_pondBounds.x;
    float distE = c_pondBounds.z - worldPos.x;
    float distN = worldPos.z - c_pondBounds.y;
    float distS = c_pondBounds.w - worldPos.z;
    float shoreDist = min(min(distW, distE), min(distN, distS));

    float shoreDamp = clamp(shoreDist / c_shoreMargin, 0.0, 1.0);
    shoreDamp = shoreDamp * shoreDamp * (3.0 - 2.0 * shoreDamp); // smoothstep
    v_shoreFactor = shoreDamp;

    // Wave 1
    float k1 = c_pi2 / c_wave1.y;
    float w1 = k1 * c_wave1.z;
    float phi1 = dot(c_dir1, worldPos.xz) * k1 - w1 * u_time;
    float sin1 = sin(phi1);
    float cos1 = cos(phi1);

    // Wave 2
    float k2 = c_pi2 / c_wave2.y;
    float w2 = k2 * c_wave2.z;
    float phi2 = dot(c_dir2, worldPos.xz) * k2 - w2 * u_time;
    float sin2 = sin(phi2);
    float cos2 = cos(phi2);

    // Wave 3
    float k3 = c_pi2 / c_wave3.y;
    float w3 = k3 * c_wave3.z;
    float phi3 = dot(c_dir3, worldPos.xz) * k3 - w3 * u_time;
    float sin3 = sin(phi3);
    float cos3 = cos(phi3);

    // Vertical wave displacement
    float dy = (c_wave1.x * sin1 + c_wave2.x * sin2 + c_wave3.x * sin3) * shoreDamp;

    // Trochoidal horizontal displacement (Gerstner)
    float dx = -(c_wave1.w * c_wave1.x * c_dir1.x * cos1 +
                 c_wave2.w * c_wave2.x * c_dir2.x * cos2 +
                 c_wave3.w * c_wave3.x * c_dir3.x * cos3) * shoreDamp;

    float dz = -(c_wave1.w * c_wave1.x * c_dir1.y * cos1 +
                 c_wave2.w * c_wave2.x * c_dir2.y * cos2 +
                 c_wave3.w * c_wave3.x * c_dir3.y * cos3) * shoreDamp;

    worldPos.x += dx;
    worldPos.y += dy;
    worldPos.z += dz;
    v_worldPos = worldPos.xyz;

    // Analytical partial derivatives for accurate wave normals
    float dYdX = (c_wave1.x * k1 * c_dir1.x * cos1 +
                  c_wave2.x * k2 * c_dir2.x * cos2 +
                  c_wave3.x * k3 * c_dir3.x * cos3) * shoreDamp;

    float dYdZ = (c_wave1.x * k1 * c_dir1.y * cos1 +
                  c_wave2.x * k2 * c_dir2.y * cos2 +
                  c_wave3.x * k3 * c_dir3.y * cos3) * shoreDamp;

    v_normal = normalize(vec3(-dYdX, 1.0, -dYdZ));
    v_viewDir = u_camPos - worldPos.xyz;

    gl_Position = u_projViewTrans * worldPos;
}
