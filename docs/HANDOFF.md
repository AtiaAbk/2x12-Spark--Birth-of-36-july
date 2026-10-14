# AGENT & DEVELOPER RESUMPTION HANDOFF
**Project:** 2x12 / Spark: Birth of 36 July  
**Repository:** `/Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july`  
**Engine:** Java 17 + libGDX 1.14.2 (LWJGL3 Desktop)  
**Primary Branch:** `main` (Backup tag: `backup/before-antigravity-full-game`)  
**Hardware Profile:** Apple Silicon M4, 16 GB Unified Memory (Fanless, thermal budget strict)

---

## 1. INSTANT HOW-TO: BUILD, RUN & TEST

### Clean Build Check
```bash
./gradlew :core:build :lwjgl3:compileJava
```

### Launch Desktop Game
```bash
./gradlew :lwjgl3:run
```

### Automated Baseline Visual Capture (Headless / Automated Screenshot Harness)
Any agent can trigger automated screenshot capture from 8 fixed viewpoints without user intervention:
```bash
./gradlew :lwjgl3:run -Dbd.spark36.baseline=spawn -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=curzon_front -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=curzon_side -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=pond_ghat -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=tsc -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=student_closeup -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=boss_fight -Dbd.spark36.autoExit=true
./gradlew :lwjgl3:run -Dbd.spark36.baseline=world_edge -Dbd.spark36.autoExit=true
```
Screenshots will be output to `assets/screenshots/00_baseline/<viewpoint>.png`.

---

## 2. CODE ARCHITECTURE & DIRECTORY MAP

- `core/src/main/java/bd/spark36/`
  - `SparkGame.java`: Main application adapter; game state lifecycle (BOOT, MAIN_MENU, PLAYING).
  - `camera/CinematicCamera.java`: 3rd-person spherical camera, collision zoom, smoothing, baseline test overrides.
  - `character/`:
    - `PlayerController.java`: Locomotion, stamina, martial arts combat combos, parry, dodge.
    - `HelmetGoon.java`: Boss AI state machine, attack telegraphs, stagger, knockout.
    - `StudentMesh.java` & `CampusCrowd.java`: Procedural student characters and ambient crowd.
  - `hud/`:
    - `WhereWindsMeetHUD.java`: Smoked-glass HUD, compass rose, objective pin, mission cards, victory screen.
    - `FontRenderer.java`: Bitmap font rendering.
  - `world/`:
    - `DhakaCampusWorld.java`: Campus environment, buildings, colliders, Pukur pond, lighting, shadow pass.
    - `TextureFactory.java`: Procedural texture generator (bricks, stone, tiles, plaster, water).
    - `AtmosphereRenderer.java` & `PostProcessor.java`: Tone-mapping, bloom, FXAA, vignetting.
    - `JulyMemorials.java`: 5 authentic commemorative monuments with glowing beacons and archival logs.
- `assets/`
  - `maps/campus_map.png`: **Authoritative spatial layout for the Curzon core zone.**
  - `screenshots/00_baseline/`: 8 reference viewpoints for before/after visual audits.
- `docs/`
  - `AUDIT.md`: Hostile review and initial code audit.
  - `DECISIONS.md`: Log of all architectural decisions and conflict resolutions.
  - `PROGRESS.md`: Live status matrix per system and per level.
  - `HANDOFF.md`: This file.
  - `DESIGN_BIBLE.md` & `UPGRADE_PLAN.md`: Visual, combat, and milestone plans.
  - `story/TIMELINE.md` & `story/STORY_BIBLE.md`: Researched historical timeline and character bible.
  - `research/ARCHITECTURE.md`: Verified measurements for Curzon Hall, Pukur, TSC, Raju, etc.
  - `research/WORLD_SCALE_AND_COORDINATES.md`: 1 unit = 1m coordinate mapping table.
  - `research/ASSET_LICENSE_REGISTER.md`: 100% CC0 / SIL OFL legal asset register.
  - `research/COMBAT_DESIGN_REFERENCE.md`: Lathi Khela martial arts & stance frame data.

---

## 3. ENGINE & CODE CONVENTIONS

1. **Physical Scale:** 1.0 unit = 1.0 meter. Player height ≈ 1.75m. Door height ≈ 2.1m.
2. **Coordinate Orientation:**
   - `+X` = East (towards TSC & Raju Roundabout)
   - `-X` = West (towards Central Library & Arts Plaza)
   - `+Y` = Up (Ground = 0.0m)
   - `-Z` = North (towards Curzon Hall facade at Z = -18m)
   - `+Z` = South (towards Main Gate spawn at Z = +76m)
3. **Compass Formula:** Compass Angle = `(90° - Bearing + CameraYaw)` ensuring needle points North.
4. **Git Discipline:**
   - Exactly one coherent change per commit with descriptive message.
   - Run `./gradlew :core:build` before every commit.
   - Never force push (`git push --force`). Never delete user files without backup.
5. **Memory & Garbage Collection:**
   - Zero object allocations (`new Vector3()`, `new Color()`) in `render()` or `update()` loops.
   - Use pooled temporary vectors (`MathUtils`, scratch variables).

---

## 4. WHAT TO WORK ON NEXT

The project has completed **M0 (Audit, Research, Story Bible, Design Bible, Master Plan)**.  
The next active task is **Milestone M1 (Rendering Foundation + Quality Tiers)**:
1. Implement a custom PBR-lite shader supporting Tangent-Space Normal Maps, Diffuse Albedo, and Roughness/Metallic highlights.
2. Implement Analytical Exponential Height Fog to haze distant geometry and eliminate the skybox void drop-off.
3. Add perimeter boundary walls and dense perimeter treeline to seal off world edges.
4. Capture screenshots of all 8 baseline viewpoints to verify the visual grounding and commit M1.
