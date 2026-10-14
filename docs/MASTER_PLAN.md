# MASTER EXECUTION PLAN — MILESTONES M0 TO M10
**Project:** 2x12 / Spark: Birth of 36 July  
**Scope:** Complete Game (All Levels 1–7, Modes, Systems, Visuals, UX, Audio, Packaging)  
**Target Hardware:** Apple Silicon MacBook Air M4 (16 GB, fanless, 60 FPS locked @ 1080p)

---

## 1. MILESTONE OVERVIEW & ROADMAP

```
[M0: Research & Audit] ---> [M1: Rendering Foundation] ---> [M2: Architecture Skeleton]
                                                                     |
[M5: Level 1 Complete] <--- [M4: Characters & Dialogue] <--- [M3: Curzon Core Slice]
        |
        v
[M6: TechFest Demo Build] ---> [M7: Arena & Combat Extraction] ---> [M8: Levels 2 to 7]
                                                                            |
[M10: Polish & TechFest Packaging] <---------------------------- [M9: Full Game Integration]
```

---

## 2. MILESTONE SPECIFICATIONS & ACCEPTANCE TESTS

### Milestone M0: Audit, Research & Master Plan (COMPLETED)
- **Deliverables:**
  - `backup/before-antigravity-full-game` tag created.
  - `docs/AUDIT.md`, `docs/research/*`, `docs/story/*`, `docs/DESIGN_BIBLE.md`, `docs/DECISIONS.md`, `docs/PROGRESS.md`, `docs/HANDOFF.md`.
  - Baseline screenshots in `assets/screenshots/00_baseline/`.
  - Git hygiene clean (no tracked `.class` files).
- **Acceptance Criteria:** Clean compilation with `./gradlew :core:build :lwjgl3:compileJava`.

---

### Milestone M1: Rendering Foundation & Quality Tiers (CURRENT ACTIVE)
- **Goal:** Real PBR texture pipeline, normal-mapped lighting, atmospheric height fog, and perimeter enclosure to eradicate the skybox void drop-off.
- **Tasks:**
  1. Lightweight PBR-lite shader supporting Tangent-Space Normal Maps, Diffuse Albedo, and Roughness/Metallic highlights.
  2. Analytical Exponential Height Fog to ground buildings and blend the horizon.
  3. Perimeter brick boundary wall and perimeter background treeline along all world edges.
  4. Post-processing tuning (ACES tone-mapping, subtle bloom threshold, filmic vignette).
  5. 3 graphics presets (Low, Medium, High).
- **Acceptance Criteria:**
  - Build passes cleanly.
  - Screenshots of all 8 baseline viewpoints showing atmospheric grounding and zero raw void drop-off.
  - Solid 60 FPS on Medium tier on M4 MacBook Air.

---

### Milestone M2: Architecture Refactor Skeleton
- **Goal:** Establish extensible game architecture without breaking Level 1 launchability.
- **Tasks:**
  1. Create `GameState` lifecycle (BOOT, MAIN_MENU, LOADING, PLAYING, DIALOGUE, PAUSED, LEVEL_COMPLETE, ARENA, ARCHIVE).
  2. Implement `LevelManager` with clean disposal and memory management.
  3. Implement `SaveManager` with JSON persistence (`spark36_settings.json` and `spark36_save.json`).
  4. Skeleton for data-driven `MissionManager`, `DialogueManager`, and Event Bus.
- **Acceptance Criteria:** Game boots into Main Menu, transitions seamlessly to Level 1, loads/saves settings cleanly.

---

### Milestone M3: Level 1 Vertical Slice (Curzon Core Per Map)
- **Goal:** Complete physical environment of Level 1 matching `campus_map.png` to 1:1 metric scale.
- **Tasks:**
  1. Reconstruct Curzon Hall with real Indo-Saracenic proportions (93.9m × 47.6m × 18.5m), 2-storey verandas, cusped Mughal arches, projecting iwans, stone brackets, and white chhatri cupolas.
  2. Curzon Pukur: 18m × 24m masonry-curbed pond with ghat steps and lotus pads.
  3. Reconstruct flanking science wings, Central Library quadrangle, Madhur Canteen, Arts Plaza & Aparajeyo Bangla, Raju Memorial roundabout, and TSC pavilion.
  4. Complete avenue paving, curbs, historical streetlamps, and lush Bengali trees (Rain Tree, Banyan, Krishna Chura).
- **Acceptance Criteria:** All landmarks from `campus_map.png` present and physically traversable with solid collision hulls.

---

### Milestone M4: Characters, Animation & Bengali Localisation
- **Goal:** Expressive student models, crowd system, and full Bengali typography.
- **Tasks:**
  1. Student protagonist model with panjabi/kurta/t-shirt cloth folds and messenger bag.
  2. FreeType Noto Sans Bengali font integration in `FontRenderer`.
  3. NPC crowd states (idle, walk, converse, read placard, distribute leaflet).
  4. Contextual interaction prompt (`[E] Talk / Inspect`).
  5. Bilingual subtitle renderer with smoked-glass UI banner.
- **Acceptance Criteria:** Native Bengali Unicode text renders with zero missing glyph boxes; NPCs populate campus verandas.

---

### Milestone M5: Level 1 Complete ("Where It All Began")
- **Goal:** Complete playable narrative arc for Level 1 (mid-June to July 14, 2024).
- **Tasks:**
  1. Implement missions `L1_M01` through `L1_M06` and scenes `SC01` through `SC10`, `SC19`, `SC20`.
  2. Peaceful activities: campus exploration, student dialogue, leaflet distribution, poster gathering.
  3. July 14 midnight assembly cinematic at Raju Memorial.
  4. Climax declaration: *"আমিও থাকব।"* ("I will be there too.").
  5. Level 1 completion card and seamless transition to Level 2 (July 15).
  6. Combat disabled by flag (`combatEnabled = false`).
- **Acceptance Criteria:** Full playthrough from spawn to 14 July climax with zero combat and zero soft-locks.

---

### Milestone M6: TechFest Demo Build (Shippable Vertical Slice)
- **Goal:** Packaged, standalone shippable demo featuring polished Level 1 + Free Roam + Arena Mode teaser.
- **Tasks:**
  1. Polished Main Menu, Settings, Chapter Select, and Credits.
  2. In-game pause menu, rebindable controls, and gamepad support.
  3. Standalone distribution package (macOS application bundle / JAR launcher).
  4. TechFest demo script and presentation screenshots.
- **Acceptance Criteria:** One-click launch on macOS without terminal; zero crashes during 30-minute play test.

---

### Milestone M7: Combat Module Extraction & Arena Mode
- **Goal:** Dedicated martial arts training grounds (*Lathi Khela*) featuring full combat suite.
- **Tasks:**
  1. Relocate `HelmetGoon` and combat mechanics to dedicated `bd.spark36.combat` module.
  2. Traditional bamboo staff (*lathi*) weapon with 3 dynamic stances (*Goti*, *Dhal*, *Mukt*).
  3. Deflection sparks, 80ms hit-stop, posture bars, and non-lethal takedown finishers.
  4. Combat trials against varied goon archetypes.
- **Acceptance Criteria:** Combat trials playable from Main Menu with fluid parries and 60 FPS responsiveness.

---

### Milestone M8: Levels 2 to 7 (Chronological Campaign Progression)
- **Goal:** Author and build Levels 2 through 7 matching historical events with strict ethical restraint.
- **Levels:**
  - Level 2: *The Day the Streets Changed* (July 15, 2024) — Campus attack evasion and medical rescue.
  - Level 3: *A City on Edge* (July 16–17, 2024) — Nationwide expansion, dormitory evacuations.
  - Level 4: *Voices Across the City* (July 18–19, 2024) — Internet blackout, physical courier network.
  - Level 5: *The Cost of Courage* (July 20–25, 2024) — Curfew, humanitarian medical supply runs.
  - Level 6: *A Turning Point* (August 3–4, 2024) — Central Shaheed Minar mass assembly.
  - Level 7: *After the Storm* (August 5 & Beyond) — 36 July reflection, memorial walkthrough.
- **Acceptance Criteria:** Each level accompanied by its Level Design Sheet (`docs/levels/Lx.md`) and verified against historical sources.

---

### Milestone M9: Full Game Integration, Audio & Educational Archive
- **Goal:** Continuous playthrough from Level 1 to Level 7 with comprehensive audio and educational archive.
- **Tasks:**
  1. Multi-surface footsteps (stone, grass, water, mud).
  2. Campus ambient soundscapes (Doel birdsong, wind in banyans, distant crowds).
  3. Interactive Educational Archive with sourced documents, photographs, and glossary.
  4. Final credits with complete historical references and citations.
- **Acceptance Criteria:** Seamless playthrough from start to finish without memory leaks or soft-locks.

---

### Milestone M10: Optimization, Final QA & TechFest Packaging
- **Goal:** Rock-solid stability, zero memory leaks, and final production builds.
- **Tasks:**
  1. Frustum culling and spatial octree optimization.
  2. GC pause audit (zero allocations in hot loops).
  3. Packaging and distribution builds (macOS Apple Silicon bundle, Windows executable).
  4. Final comprehensive test report.
- **Acceptance Criteria:** 60 FPS locked on M4 Air; final release ready for exhibition.
