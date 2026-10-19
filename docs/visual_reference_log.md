# VISUAL REFERENCE & RENDERING BENCHMARK LOG
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Visual Target:** Grounded Historical Realism, Cinematic Lighting, Physically Plausible Outdoor Shading  

---

## 1. VISUAL TARGET & PHOTOREALISTIC DESIGN PRINCIPLES

### 1.1 Geometry & Architectural Relief
- **3D Structural Openings:** All windows and doorways feature physical depth (recess of 0.22m) with modeled stone archivolts, extruded brick casings, and sills via `OpeningBuilder.java` to prevent flat-card artifacts.
- **Back-Face Culling:** Back-face culling (`GL_BACK`) is strictly enforced to prevent flipped/mirrored rendering artifacts.
- **Accurate UV Mapping:** Vertical quad builders (`createUprightQuad()`) map UV coordinates in strict alignment with LibGDX `Pixmap` top-left conventions ($U \in [0, 1], V \in [0, 1]$ upright).

### 1.2 Physically Plausible Lighting & Atmospheres
- **Level 1 (Curzon Hall):** Warm morning sun (Sun Altitude: 38°, Azimuth: 145°), soft warm ambient tone (`#665544`), directional sunlight (`#F8EAC8`), cast shadows with depth pass.
- **Level 2 (Raju Memorial):** Harsh afternoon daylight (Sun Altitude: 62°), intense street reflections, high contrast shadows.
- **Level 3 (Police Escalation / Shahbagh):** Hazy, diffused overcast sunlight obscured by dense white/grey CS tear gas fog; periodic volumetric shockwaves and camera shake.
- **Level 4 (Curfew Blackout):** Midnight urban darkness, deep blue/indigo ambient sky (`#0A0D18`), 35m sweeping halogen searchlight beam from BTR-80 APC, high-contrast chiaroscuro shadows.
- **Level 5 (Central Shaheed Minar):** Late afternoon golden hour (Sun Altitude: 22°), dramatic long shadows, deep crimson sunlight illuminating the Minar sun disk.
- **Level 6 (36 July Victory):** Bright mid-day sun, crisp blue atmospheric scattering, high visual clarity, triumphant illumination.

---

## 2. SHADING & POST-PROCESSING PIPELINE

### 2.1 Post-Processing Chain (`PostProcessor.java`)
- **FXAA Anti-Aliasing:** High-performance fast approximate anti-aliasing smoothing jagged polygon edges on buildings, railings, and vegetation.
- **Cinematic Bloom:** Thresholded luminance extraction ($L_{thresh} = 0.82$) with dual-pass Gaussian blur for subtle highlight glows on water surfaces, lampposts, and searchlights without overblowing the scene.
- **Color Grading:** Custom film curve mapping enhancing terracotta brick tones and foliage greens while maintaining shadow detail.
- **Subtle Vignette:** Optical lens darkening (radius 0.85, smoothness 0.45) directing player focus toward the central gameplay area.
- **Subtle Film Grain:** Micro-luminance temporal jitter (amplitude 0.025) simulating analog film texture and breaking digital gradient banding.

### 2.2 Water Simulation (`AtmosphereRenderer.java`)
- **Procedural Normal Ripples:** Dual sine-wave animated normal perturbance simulating gentle wind disturbances on Curzon pond.
- **Fresnel Approximation:** View-angle dependent reflectivity showing sky/building reflections at glancing angles and murky aquatic depth when viewed top-down.
- **Organic Props:** Dynamically placed lotus leaves (`Nymphaea nouchali`) floating at water level ($Y = 0.22\text{m}$).

---

## 3. VEGETATION & ENVIRONMENTAL SHADING

- **Flora Archetypes:**
  - *Debdaru (Polyalthia longifolia)*: Slender, conical evergreen trees along campus promenades.
  - *Neem (Azadirachta indica)*: Feathery foliage canopy filtering sunlight into dapple ground shadows.
  - *Banyan / Peepal (Ficus)*: Broad canopy providing environmental scale and framing historical courtyards.
- **Procedural Sway:** Vertex shader sinusoidal wind displacement based on world coordinates and elapsed time ($A = 0.04\text{m}, \omega = 1.4\text{ rad/s}$), ensuring natural non-synchronized foliage motion.
