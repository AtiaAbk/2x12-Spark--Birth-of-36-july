# DESIGN BIBLE — SPARK: BIRTH OF 36 JULY
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Engine:** Java 17 + libGDX 1.14.2 (LWJGL3 Desktop)  
**Target Platform:** macOS (Apple Silicon M4 / Metal OpenGL), Windows, Linux  
**Target Performance Budget:** Rock-solid 60 FPS @ 1080p on fanless MacBook Air M4 (Thermals strictly controlled)

---

## 1. VISION & PILLARS

### High Concept
A cinematic, historically grounded, 3D action-adventure honoring the courage of Bangladeshi students during the historic July 2024 Student Mass Uprising. Set within the iconic red-brick grounds of the University of Dhaka, players navigate peaceful campus solidarity, protect innocent peers from violent aggression using defensive martial arts (*Lathi Khela*), and unearth the real historical events that ignited the birth of "36 July".

### Core Design Pillars
1. **Historical Gravitas & Cultural Authenticity:**
   - Real architecture (Curzon Hall, TSC, Aparajeyo Bangla, Raju Memorial) reconstructed faithfully from measured survey data.
   - Authentic bilingual presentation (Bengali typography via Noto Sans Bengali, atmospheric ambient soundscapes of Dhaka).
2. **Defensive, Principled Combat (*Where Winds Meet / Wukong* caliber):**
   - High-skill, rhythmic combat featuring traditional bamboo staff (*lathi*) and unarmed martial defense.
   - Non-lethal disarms and deflections. Absolute prohibition against harming peaceful students.
3. **Cinematic Indie Visual Polish:**
   - Deep terracotta brick, weathered white chunam plaster, lush monsoon greenery, dynamic water reflections, and atmospheric depth fog.
   - Cohesive lighting with warm golden sunlight cutting through monsoon morning mist.
4. **Thermal & Frame Budget Discipline:**
   - 60 FPS lock on fanless Apple Silicon hardware.
   - Draw calls < 250, triangle count < 400,000, texture VRAM < 400 MB. Zero garbage collection spikes during gameplay loops.

---

## 2. ART DIRECTION & VISUAL DESIGN

### Color Palette & Visual Tone
- **Terracotta Red (Hero Architecture):** `#8E2819` to `#A73A27` — Aged colonial fired clay brick with natural color variation and subtle lime mortar efflorescence.
- **Lime Plaster / Chunam (Trims & Cornices):** `#E4DEC9` — Warm off-white lime wash with rain staining and subtle wear.
- **Monsoon Tropical Green (Vegetation):** `#1E3F20` (Deep shade foliage), `#336329` (Lush canopy), `#4E8732` (Sunlit leaves). No oversaturated neon greens; rich, deep, cinematic Bengali flora.
- **Historic Water (Curzon Pukur):** `#2A4B46` (Murky natural jade green with soft sky highlights and lotus pads).
- **Sunlight & Atmosphere:** Morning monsoon light (`Color(1.0f, 0.96f, 0.88f)`) with directional amber tint casting long, dramatic 4096-resolution shadows.

### Materials & Shading Pipeline
- Custom lightweight PBR-lite shader featuring:
  - Diffuse Albedo with color variation per instance.
  - Tangent-space Normal mapping for tactile brick depth and carved relief.
  - Roughness / Specular mask for wet pavement sheen and sun highlights.
  - Analytical Exponential Height Fog to ground buildings and eliminate skybox void drop-offs.

---

## 3. LEVEL DESIGN & NAVIGATION

### Campus Layout (Strictly Aligned with `campus_map.png`)
```
                          [FULLER ROAD BOUNDARY]
   [Madhur Canteen]        [CURZON HALL COMPLEX]       [Swadhinata Sangram]
   (Historic Pavilion)       (Indo-Saracenic Hero)      (Sculpture Court)
           |                          |                         |
   [Central Library]       [CURZON HALL PUKUR]               [TSC]
   (Colonnaded Quad)      (Masonry Reflecting Pool)    (Pyramidal Auditorium)
           |                          |                         |
   [Aparajeyo Bangla]      [CENTRAL PROMENADE]           [Raju Memorial]
     (Arts Plaza)                     |                   (Roundabout)
           |                          |                         |
   [Hakim Chattar]                    |                         |
           \___________________[MAIN GATE]_____________________/
                               (Spawn Point)
```

### Visual Signposting & Flow
- **Primary Landmark:** The 18.5m-tall domes and arches of Curzon Hall dominate the northern horizon, serving as a constant geographic compass.
- **Primary Spine:** The 7m-wide Central Promenade leads directly from Spawn at Main Gate (`Z = +76m`) toward Curzon Hall (`Z = -18m`), naturally bifurcating around the shimmering waters of Curzon Pukur.
- **Breadcrumbs:** 5 glowing historical memorial beacons with subtle particle wisps attract the player to explore key monuments.

---

## 4. COMBAT & GAMEPLAY SYSTEMS

### The Three Stances (Fluid Stance Flow)
1. **Thrust Stance (*Goti* - গতি):**
   - Quick linear pokes with the bamboo staff.
   - Longest reach (2.5m). Ideal for interrupting charging attackers and maintaining distance.
2. **Sweep Stance (*Dhal* - ঢাল):**
   - Wide 360° rotational swings.
   - High posture damage. Breaks through enemy guards and controls surrounding space.
3. **Agile Stance (*Mukt* - মুক্ত):**
   - Bare-handed evasion, quick jabs, and flowing deflections.
   - Extended dodge i-frames and instant counter-attacks.

### Defensive Mastery
- **Perfect Parry (0.18s window):** Deflects enemy strike, inflicts +35 Poise damage to attacker, sparks golden flash, and triggers 0.08s hit-stop.
- **Perfect Dodge:** Slipping an incoming strike right before impact spawns a transient wind ripple afterimage, immediately restoring 25 stamina.
- **Takedown Finisher:** Depleting enemy poise breaks their posture, opening a cinematic disarming takedown (`[F] TAKEDOWN`).

### Movement & Evasion
- **Locomotion:** Smooth transition between Walk (3.2 m/s), Sprint (6.8 m/s with stamina limit), and Crouch (1.8 m/s).
- **Agility:** Jump, double jump vaulting, and ledge climbing.
- **Water Interaction:** Dynamic wading slowdown with physical water ripple rings in the Curzon Pukur.

---

## 5. HISTORICAL NARRATIVE & RESPECTFUL TREATMENT

- **Setting:** July 2024 student-led protest movement for justice and meritocracy in Bangladesh.
- **Tone:** Solemn, heroic, peaceful, inspiring.
- **Rules of Engagement:**
  - Aggression comes exclusively from hired goons (`HelmetGoon`).
  - Player acts purely in self-defense and defense of other students.
  - Hitting peaceful student NPCs results in immediate disqualification.
- **5 Commemorative Archives:**
  1. *The Spark of Solidarity* (Curzon Hall Arcade)
  2. *Reflecting on Truth* (Curzon Pukur Ghat)
  3. *Unbreakable Resolve* (Aparajeyo Bangla)
  4. *Voice of Unity* (TSC Pavilion)
  5. *Memorial of the Unforgotten* (Raju Memorial)

---

## 6. TECHNICAL SPECIFICATION & PERFORMANCE BUDGETS

| Metric | Target Budget | Safety Ceiling | Current Prototype Status |
|---|---|---|---|
| **Frame Rate** | **60 FPS locked** | Min 55 FPS | 60 FPS verified on M4 |
| **Draw Calls** | **< 200** | Max 300 | ~180 calls |
| **Triangles / Polycount** | **< 250,000** | Max 450,000 | ~120,000 polys |
| **Texture Memory (VRAM)** | **< 256 MB** | Max 512 MB | ~110 MB |
| **Garbage Collection (GC)** | **0 allocations / frame** in render loop | < 1 KB / frame | Audited & pooled |
| **Shadow Map** | **4096×4096 single pass** | Cascaded 2-split | 4096 single pass active |
