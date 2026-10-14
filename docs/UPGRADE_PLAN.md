# PHASED UPGRADE PLAN & ROADMAP
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Architecture Target:** High-fidelity indie 3D action-adventure running at 60 FPS on Apple Silicon M4.  
**Execution Standard:** Action-first, one atomic Git commit per stage, zero regressions, build & test verified before claiming completion.

---

## STAGE 1: RENDERING FOUNDATION & VOID FIX
- **Goal:** Transform visual fidelity from flat procedural unshaded polygons to grounded, cinematic materials with tactile surface depth, and eliminate the raw skybox void drop-off.
- **Key Deliverables:**
  1. Lightweight PBR-lite shader supporting Tangent-Space Normal Maps, Diffuse Albedo, and Roughness/Metallic highlights.
  2. Analytical Exponential Height Fog to naturally haze the horizon and blend building bases into the atmosphere.
  3. Perimeter boundary wall (brick + wrought-iron railings) and dense perimeter background treeline to seal off world edges.
  4. Post-processing tuning (tone-mapping, subtle bloom threshold, filmic vignette).
- **Affected Files:**
  - `core/src/main/java/bd/spark36/render/PBRShader.java` (New or integrated)
  - `core/src/main/java/bd/spark36/render/PostProcessor.java`
  - `core/src/main/java/bd/spark36/world/DhakaCampusWorld.java`
  - `core/src/main/java/bd/spark36/world/PerimeterEnclosure.java`
- **Verification:** Screenshots of all 8 baseline viewpoints showing atmospheric grounding and zero void bleeding.
- **Rollback Plan:** Revert shader to default `DefaultShaderProvider` with diffuse lighting.

---

## STAGE 2: CURZON HALL ARCHITECTURAL RECONSTRUCTION
- **Goal:** Replace the flat rectangular box with an authentic Indo-Saracenic masterpiece matching measured surveys (93.9m × 47.6m × 18.5m).
- **Key Deliverables:**
  1. Modular architectural bay system: multi-foil cusped Mughal arches, horseshoe veranda openings, paired columns.
  2. Recessed 2-storey covered verandas with authentic depth and shadow casting.
  3. North and South projecting central iwans with octagonal corner turrets.
  4. Stone corbel brackets (*chhajjas*), pierced jali parapets, and white-cupola domed chhatris.
  5. 0.6m elevated stone plinth with grand entrance stairway.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/world/landmarks/CurzonHallReconstruction.java`
  - `core/src/main/java/bd/spark36/world/DhakaCampusWorld.java`
- **Verification:** Baseline viewpoints `curzon_front.png` and `curzon_side.png` comparison against historical photos.

---

## STAGE 3: CAMPUS ENVIRONMENT & REMAINING LANDMARKS
- **Goal:** Fully reconstruct all landmarks from `campus_map.png` to their verified historical scale and appearance.
- **Key Deliverables:**
  1. Curzon Hall Pukur: 18m × 24m masonry-curbed pool, stone ghat steps, aquatic lotus pads, and caustics.
  2. TSC (Teacher-Student Centre): Pyramidal roof pavilion, open-air courtyard, butterfly canopy on pilotis.
  3. Raju Memorial Sculpture: 8 figures with locked arms atop circular tiered island.
  4. Aparajeyo Bangla: 5.5m high sculpture on stepped concrete plinth at Arts Plaza.
  5. Madhur Canteen: Octagonal colonial pavilion with hipped timber roof.
  6. Central Library quadrangle and Hakim Chattar pavilion.
  7. Foliage overhaul: lush Bengali trees (Banyan, Rain Tree, Krishna Chura) with deep natural green foliage.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/world/landmarks/*`
  - `core/src/main/java/bd/spark36/world/DhakaCampusWorld.java`
- **Verification:** Full campus walkthrough; zero floating geometry; correct collision hulls.

---

## STAGE 4: CHARACTER VISUAL UPGRADE
- **Goal:** Replace blocky humanoid shapes with expressive, high-detail student and antagonist models.
- **Key Deliverables:**
  1. Student protagonist model: cloth folds on Bengali panjabi/kurta/t-shirt, rolled sleeves, denim trousers, chappals/sneakers.
  2. Diverse student NPC crowd models (male and female students, varied attire, backpacks).
  3. Helmet Goon antagonist: distinctive motorcycle helmet with reflective visor, heavy club/lathi, aggressive posture.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/character/StudentMesh.java`
  - `core/src/main/java/bd/spark36/character/HelmetGoonMesh.java`
- **Verification:** Baseline viewpoint `student_closeup.png` and `boss_fight.png`.

---

## STAGE 5: ANIMATION PIPELINE & LOCOMOTION FEEL
- **Goal:** Smooth, natural locomotion with tactile weight, inertia, and fluid state blending.
- **Key Deliverables:**
  1. Locomotion acceleration/deceleration curves (no instant ice-skating starts/stops).
  2. Inverse-kinematic foot planting on stairs, curbs, and slopes.
  3. Athletic parkour vaults over low walls, curbs, and fences.
  4. Dynamic swimming / water-wading transitions in the Curzon Pukur.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/character/PlayerController.java`
  - `core/src/main/java/bd/spark36/character/AnimationState.java`
- **Verification:** Smooth 60 FPS motion capture; zero jitter on uneven steps.

---

## STAGE 6: COMBAT SYSTEM UPGRADE (*WHERE WINDS MEET / WUKONG*)
- **Goal:** Transform combat into a rhythmic, deeply satisfying martial arts duel rooted in Bengali *Lathi Khela*.
- **Key Deliverables:**
  1. Traditional bamboo staff (*lathi*) weapon with 3 dynamic stances (*Goti* Thrust, *Dhal* Sweep, *Mukt* Agile).
  2. Perfect Parry window (0.18s) with golden deflection sparks, hit-stop, and poise damage.
  3. Perfect Dodge with illusory wind afterimage and stamina regain.
  4. Dual-bar HUD: Health + Poise / Posture meter on enemy and player.
  5. Cinematic non-lethal Takedown Finisher upon breaking enemy guard.
  6. Strict civilian protection rule: hitting peaceful students triggers instant disqualification.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/combat/CombatSystem.java`
  - `core/src/main/java/bd/spark36/character/PlayerController.java`
  - `core/src/main/java/bd/spark36/character/HelmetGoon.java`
- **Verification:** Combat duel testing; verify parry windows, i-frames, and failure trigger.

---

## STAGE 7: CAMERA, UI & BENGALI TYPOGRAPHY
- **Goal:** Implement AAA-grade HUD, menus, and bilingual typography inspired by *Where Winds Meet*.
- **Key Deliverables:**
  1. Noto Sans Bengali font integration for authentic Bengali subtitles, memorial logs, and dialogue.
  2. Smoked-glass HUD panels with delicate gold filigree, dynamic health/stamina/poise rings.
  3. Cinematic victory / mission completion screen with transparent blur backdrop and bilingual laurels.
  4. Full settings menu with audio volume sliders, graphics presets, mouse sensitivity, and key remapping.
  5. Persistent configuration saved to JSON/Preferences.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/ui/WhereWindsMeetHUD.java`
  - `core/src/main/java/bd/spark36/ui/FontRenderer.java`
  - `core/src/main/java/bd/spark36/config/GameSettings.java`
- **Verification:** Native Bengali glyph rendering verified; settings survive app restart.

---

## STAGE 8: ENEMY AI & ENCOUNTER DESIGN
- **Goal:** Create dynamic, tense, multi-phase boss encounters with tactical escalation.
- **Key Deliverables:**
  1. Helmet Goon multi-phase behavior: Phase 1 (Cocky taunts, heavy sweeps), Phase 2 (Desperate strikes, rapid swings).
  2. Surrounding minion squad / lookouts coordinating perimeter tension.
  3. Dynamic duel arena boundary ring (crowd students forming an encouraging circle around the duel).
- **Affected Files:**
  - `core/src/main/java/bd/spark36/character/HelmetGoon.java`
  - `core/src/main/java/bd/spark36/character/GoonSquadAI.java`
- **Verification:** Complete play-through of duel encounter from challenge dialogue to knockout.

---

## STAGE 9: AUDIO ENGINE & BANGLADESH SOUNDSCAPE
- **Goal:** Immerse the player in the authentic acoustic environment of Dhaka University and high-impact combat foley.
- **Key Deliverables:**
  1. Multi-surface footstep audio (stone pavers, lawn grass, water wading, mud).
  2. High-impact combat foley: bamboo staff whooshes, resonant wood-on-wood clacks, deflection pings, heavy body impacts.
  3. Atmospheric ambient sound bed: Doel birdsong, gentle breeze through banyans, distant city murmur.
  4. Dynamic combat music transitions between peaceful exploration and tense standoff.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/audio/AudioManager.java`
- **Verification:** Full acoustic pass; audio balance checked with zero clipping or pops.

---

## STAGE 10: PERFORMANCE OPTIMIZATION & THERMAL TUNING
- **Goal:** Guarantee rock-solid 60 FPS on fanless MacBook Air M4 with cool thermals and zero stutter.
- **Key Deliverables:**
  1. Frustum culling and spatial octree for campus landmarks and foliage.
  2. Mesh instancing / batching for repeated architectural pillars, windows, balustrades, and trees.
  3. Zero allocation loop audit to ensure GC pauses never exceed 1ms.
  4. Benchmark harness recording 99th-percentile frame times across all 8 baseline viewpoints.
- **Affected Files:**
  - `core/src/main/java/bd/spark36/render/SpatialCuller.java`
  - `core/src/main/java/bd/spark36/benchmark/PerformanceMonitor.java`
- **Verification:** Benchmark report proving 60 FPS locked, < 200 draw calls, < 250k triangles.
