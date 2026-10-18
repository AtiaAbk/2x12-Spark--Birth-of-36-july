# KNOWN LIMITATIONS & ARCHITECTURAL BOUNDARIES
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Status:** Living Engineering Record

---

## 1. HARDWARE & THERMAL CONSTRAINTS (APPLE SILICON M4 FANLESS)
- **Constraint:** The development target is an Apple Silicon MacBook Air M4 (16 GB unified RAM) featuring passive (fanless) cooling.
- **Architectural Mitigation:**
  - Avoid brute-force multi-pass rendering or unbounded shadow cascades.
  - Directional shadow pass uses an optimized orthographic frustum focused strictly within 40 meters of the player.
  - Distant crowds in mass mobilization scenes (Levels 5 & 6) utilize instanced LOD billboard impostors rather than high-poly skinned meshes.
  - Particle limits are capped at 500 active particles per emitter.

## 2. PROCEDURAL 3D GEOMETRY VS EXTERNAL ASSET BUNDLES
- **Constraint:** To ensure zero proprietary asset contamination, zero copyright risks, and lightweight binary distribution, 3D structures (Curzon Hall, Raju Sculpture, Shaheed Minar, APC vehicles, barricades) are constructed procedurally using custom LibGDX `ModelBuilder` / `MeshPartBuilder` pipelines (`OpeningBuilder`, `TreeGeometry`, etc.).
- **Architectural Mitigation:**
  - Procedural geometries are generated once during level load and cached.
  - Textures are procedurally synthesized at startup via `TextureFactory` with deterministic seeds and mipmapped filtering, keeping download footprint under 20 MB.

## 3. HISTORICAL NARRATIVE VS ENTERTAINMENT GAMEPLAY
- **Constraint:** The July 2024 uprising resulted in profound human loss and national trauma. It must not be gamified into an arcade bloodbath or sensationalized combat spectacle.
- **Architectural Mitigation:**
  - In Level 1, combat is disabled by default.
  - In Level 2, combat represents strictly non-lethal self-defense against armed helmeted aggressors, with heavy emphasis on evasion, disarming, and rescuing wounded peers.
  - Striking any unarmed student or civilian immediately triggers disqualification and mission failure.
  - Real historic figures and martyrs are memorialized through documentary plaques and solemn tributes rather than interactive avatars.
