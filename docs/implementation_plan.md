# SEVEN-DAY AUTONOMOUS REBUILD & LAUNCH IMPLEMENTATION PLAN
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Target Platform:** macOS Apple Silicon M4 / Universal Desktop (Java 17 + LibGDX 1.14.2)  
**Execution Mode:** Fully Autonomous, Action-First, One-Commit-Per-Feature with Screenshot Verification

---

## 1. PHASE STRUCTURE & SCHEDULE

### Day 1: Audit, Baseline Verification & Campaign Architecture
- [x] Deliver comprehensive 8-point repository audit.
- [x] Create historical research dossier (`docs/historical_research.md`).
- [x] Create scene reference matrix (`docs/scene_reference_matrix.md`).
- [ ] Create clean, modular `CampaignLevel` interface and `LevelManager`.
- [ ] Refactor `SparkGame.java` to delegate level lifecycle (init, update, render, dispose).
- [ ] Expand `LevelConfig.java` to support campaign level switching and persistence.

### Day 2: Level 1 Polish & Level 2 Implementation (Campus Confrontation)
- [ ] Ground Level 1 as the polished historical setup baseline (Curzon Hall, peaceful exploration).
- [ ] Implement Level 2: July 15 Student Confrontation at Raju Sculpture.
- [ ] Add barricaded campus gate geometry, aggressive counter-protester AI (`HelmetGoon` variants).
- [ ] Integrate martial arts self-defense mechanics, hit-reaction, and injured student rescue triage.
- [ ] Test and verify Level 2 completion flow and transition.

### Day 3: Level 3 Implementation (Police Escalation & Tear Gas Navigation)
- [ ] Implement Level 3: July 18 "Complete Shutdown" & Police Tear Gas confrontation.
- [ ] Build CS gas volumetric fog/particle effects with breath/exposure mechanics.
- [ ] Add riot police lines, tear gas canister trails, sound grenade acoustic rumble.
- [ ] Add civilian rescue/escort objectives (delivering water/bandages to disoriented peers).
- [ ] Test and capture verification screenshots.

### Day 4: Level 4 Implementation (Curfew, Military APCs & Covert Routes)
- [ ] Implement Level 4: July 20–23 Midnight Curfew & Total Internet Blackout.
- [ ] Create night-time lighting with sodium lamps, dynamic sweeping halogen searchlights.
- [ ] Add military BTR-80 APC vehicle geometry and sandbagged checkpoint barriers.
- [ ] Implement stealth crouching, vision cone avoidance, and physical dossier delivery to press contacts.
- [ ] Test and capture verification screenshots.

### Day 5: Level 5 Implementation (Mass Mobilization & Shaheed Minar Assembly)
- [ ] Implement Level 5: August 3–4 Central Shaheed Minar historic assembly.
- [ ] Build Central Shaheed Minar 3D landmark monument.
- [ ] Implement high-density optimized crowd impostors / billboard clusters.
- [ ] Implement mass flow navigation, forming defensive human chains, and frontline medical logistics.
- [ ] Test and capture verification screenshots.

### Day 6: Level 6 Implementation (36 July Victory, Turning Point & Epilogue)
- [ ] Implement Level 6: August 5 "36 July" Long March to Dhaka & Fall of Autocracy.
- [ ] Build triumphant procession to liberated TSC and Curzon Hall.
- [ ] Create interactive flower-laying tribute ceremony at martyr monuments.
- [ ] Implement cinematic epilogue sequence, historical victory banner, and complete documentary credits.
- [ ] Connect all six levels in unbroken campaign progression.

### Day 7: QA, Performance Profiling, Packaging & Launch Preparation
- [ ] Full regression test across all 6 levels (movement, camera, combat, transitions, modals).
- [ ] Profile frame pacing on Apple Silicon M4 (stable 60 FPS verification).
- [ ] Verify packaging tasks (`./gradlew :lwjgl3:dist`, `jarMac`).
- [ ] Finalize release documentation and launch report.
