# ARCHITECTURAL & DESIGN DECISIONS REGISTER
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Standard:** Every major design choice, conflict resolution, alternative considered, rationale, and rollback procedure recorded here.

---

## 1. CONFLICT RESOLUTIONS (FROM MASTER BRIEF §2)

### DECISION-001: Level 1 Combat & "Helmet Goon" Relocation
- **Context & Conflict:** The master plan mandates that Level 1 ("Where It All Began") is entirely peaceful (exploration, social participation, gathering, trust building, zero weapons, zero enemies, zero player death). However, the existing prototype contained a combat encounter with `HelmetGoon` placed directly in front of Curzon Hall.
- **Chosen Decision:** 
  1. Keep 100% of the combat code, animation timings, poise system, and `HelmetGoon` implementation intact without regression.
  2. Implement a global `GameMode` and per-level capability flag (`levelConfig.combatEnabled = false` for Level 1 Story Mode).
  3. When `combatEnabled` is false: player punches/kicks are disabled, `HelmetGoon` is not spawned in the campus plaza, boss health bar is hidden, and player death is impossible.
  4. Relocate `HelmetGoon` and the martial arts combat suite into the dedicated **Arena / Lathi Khela Training Mode** (and later justified story scenes).
- **Alternatives Considered:**
  - *Delete HelmetGoon entirely:* Rejected — violates the rule to preserve working systems, destroys combat gameplay developed for the project.
  - *Leave boss fight in Level 1:* Rejected — completely contradicts the historical truth of the peaceful June–July 14 period.
- **How to Reverse:** Set `combatEnabled = true` in Level 1 configuration or launch via `-Dbd.spark36.forceCombat=true`.

---

### DECISION-002: Level 1 Spatial Scope & Map Alignment
- **Context & Conflict:** The user's planning notes listed extensive science faculty buildings (Applied Physics, Geology, Fisheries, Applied Chemistry, Zoology, Botany, Chemistry, Biochemistry, Shahid Minar), whereas `assets/maps/campus_map.png` defines a beautifully illustrated layout focused on the Curzon Hall core campus zone (Curzon Hall, Pukur, Arts Plaza, Aparajeyo Bangla, Central Library, Madhur Canteen, Hakim Chattar, TSC, Raju Roundabout, Swadhinata Sangram).
- **Chosen Decision:**
  1. Adopt `campus_map.png` as the primary spatial authority for the Milestone 3 Vertical Slice.
  2. Build the Curzon core zone first to 1:1 metric scale (`docs/research/WORLD_SCALE_AND_COORDINATES.md`).
  3. Reconstruct surrounding departments (Chemistry, Physics, Botany) as architectural wings and perimeter structures flanking Curzon Hall and the Pukur in the extended Level 1 perimeter.
- **Alternatives Considered:**
  - *Sprawl into a massive 4 km² Dhaka map immediately:* Rejected — would destroy the frame budget on MacBook Air M4, dilute visual polish, and leave all buildings looking like low-detail boxes.
- **How to Reverse:** Add sub-sectors to `DhakaCampusWorld` or instantiate separate levels in `LevelManager`.

---

### DECISION-003: Monolith Decomposition & Modular Package Structure
- **Context & Conflict:** Existing classes were growing dangerously large (`DhakaCampusWorld.java` ~2,300 lines; `WhereWindsMeetHUD.java` ~2,700 lines; `StudentMesh.java` ~1,200 lines).
- **Chosen Decision:**
  1. Freeze monolithic classes from further expansion.
  2. Incrementally extract modular builders and systems into dedicated sub-packages:
     - `bd.spark36.world.landmarks.*` (individual builders for Curzon Hall, Pukur, TSC, Raju, etc.)
     - `bd.spark36.world.vegetation.*` (tree and plant procedural generation)
     - `bd.spark36.combat.*` (stances, weapons, hitboxes, poise)
     - `bd.spark36.missions.*` (data-driven mission state machine)
     - `bd.spark36.dialogue.*` (Unicode dialogue trees and subtitle renderer)
     - `bd.spark36.render.*` (PBR shaders, atmosphere, height fog)
  3. Maintain backward-compatible wrappers so the game never breaks compilation or launch during refactoring.
- **Alternatives Considered:**
  - *Single massive rewrite:* Rejected — violates hard rule #1 and creates high regression risk.
- **How to Reverse:** Revert extracted delegate calls back to monolithic internal routines.

---

## 2. PROFESSIONAL ARCHITECTURE GAPS FILLED (FROM MASTER BRIEF §12)

### DECISION-004: Save System & State Persistence
- **Decision:** Implement JSON-based profile serialization in `bd.spark36.app.SaveManager`. Store game settings (audio sliders, graphics tiers, camera sensitivity, keybindings) in `spark36_settings.json`, and story progress (active level, completed missions, unlocked journal entries, trust metrics) in `spark36_save.json`.
- **Why:** Essential for user-friendliness and game progression across multiple sessions.
- **How to Reverse:** SaveManager falls back to default in-memory configuration if JSON files are absent or corrupted.

### DECISION-005: Bilingual Localisation & Font Pipeline
- **Decision:** Integrate Google Fonts **Noto Sans Bengali** (SIL Open Font License 1.1) alongside Liberation Sans. Use libGDX's `FreeTypeFontGenerator` to generate font atlases supporting complete Bengali Unicode ranges, conjuncts, and punctuation.
- **Why:** The game represents Bangladesh's history; authentic Bengali typography is mandatory.
- **How to Reverse:** Toggle `language = "EN"` to use Latin Liberation Sans.

### DECISION-006: Asset Missing Fallbacks & Error Tolerance
- **Decision:** Implement safe fallback patterns across all texture and audio loaders. If an external texture or audio clip fails to load, `TextureFactory` and `AudioManager` return a neutral procedural texture or a silent audio stub with a console warning instead of crashing the game loop.
- **Why:** Guarantees zero crashes during gameplay on incomplete or missing optional assets.
- **How to Reverse:** Enable strict validation mode via `-Dbd.spark36.strictAssets=true`.

### DECISION-007: Performance & Graphics Tiers
- **Decision:** Provide three graphics presets (Low, Medium, High) with configurable toggles:
  - *High:* 4096 shadow map, full post-processing (ACES tonemap, bloom, FXAA, chromatic aberration), max foliage.
  - *Medium (M4 Target):* 2048 shadow map, ACES tonemap, bloom, FXAA, optimized foliage. Rock-solid 60 FPS locked.
  - *Low:* 1024 shadow map, FXAA only, reduced foliage density.
- **Why:** Ensures cool thermals on fanless Apple Silicon MacBook Air M4 while offering scalability.
- **How to Reverse:** User can change preset at any time in the settings menu.

### DECISION-T6: Settings precedence, default graphics tier, and key rebinding deferred
- **Decision:** `bd.spark36.app.SaveManager` (DECISION-004) writes `spark36_settings.json` and `spark36_save.json` to `<user.home>/.spark36/` (override `-Dbd.spark36.saveDir`), never to the working directory. Precedence for graphics tier and tone map: launch switch (`-Dbd.spark36.graphicsTier`, `-Dbd.spark36.toneMapping`) > saved setting > `assets/visuals.properties`. The saved tier is now actually applied to the systems that read `GraphicsTier.fromSystemProperty()`; before T6 the pause menu displayed the file's HIGH while those systems always rendered MEDIUM. `visuals.properties` now says `graphicsTier=MEDIUM`, so the shipped look is unchanged and matches DECISION-007. Measured on the T0 Windows machine, spawn, 1920x1080, 20 s: HIGH applied 14.0 avg fps, MEDIUM 17.1 avg fps.
- **Key rebinding: deferred.** `GameInput` holds a clean action-to-key map, but many controls bypass it with hard-coded `Gdx.input.isKeyJustPressed(...)` checks (pause menu Q/X/M/F2, `PlayerController` V, `SparkGame` F-keys and O, dialogue and victory screens). Letting players rebind only the mapped actions would create silent conflicts with those keys. Prerequisite: move those call sites onto `GameInput` actions (they sit in the frozen `SparkGame`/`WhereWindsMeetHUD`), then add a capture UI with conflict detection and persist the map in `spark36_settings.json`.
- **Why:** Reference sections 8.5 and 11.3 (settings, save, accessibility); "do not lower visual quality by default" rules out a silent quality change in either direction.
- **How to Reverse:** Delete `<user.home>/.spark36/` to return to defaults; set `graphicsTier=HIGH` in `assets/visuals.properties` to make HIGH the default for players without a saved setting.
