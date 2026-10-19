# QUALITY ASSURANCE (QA) CHECKLIST
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Target:** macOS Apple Silicon M4 / Universal Desktop  
**Status:** Active QA Matrix

---

## 1. CORE ENGINE & GRAPHICS
- [x] Clean compilation: `./gradlew :core:build` passes with 0 errors.
- [x] Clean desktop compilation: `./gradlew :lwjgl3:build` passes with 0 errors.
- [x] Application launches without crash on macOS Apple Silicon M4.
- [x] Post-processing FBO pipeline initializes cleanly without OpenGL errors.
- [x] Directional shadow map pass renders without backface artifacts or shadow leaks.
- [x] FreeType font generator initializes `NotoSansBengali-Variable.ttf` with 0 missing glyphs.
- [x] Aspect ratio resizing handles 1280x720, 1440x900, 1920x1080, and 2560x1600.

## 2. CAMPAIGN & LEVEL PROGRESSION
- [x] Level 1: "Where It All Began" (Peaceful Curzon Hall exploration, 5 memorials, 100% completion verified).
- [x] Level 2: "The Day the Streets Changed" (July 15 campus confrontation, martial arts self-defense, medical rescue verified).
- [x] Level 3: "A City on Edge" (July 18 tear gas navigation, sound grenades, water aid verified).
- [x] Level 4: "Curfew & Covert Routes" (July 20 curfew, military BTR-80 APC searchlight, covert courier verified).
- [x] Level 5: "The Wall of People" (August 3–4 Shaheed Minar One-Point Demand mass assembly verified).
- [x] Level 6: "36 July: Epilogue" (August 5 victory march, monument flower tribute, epilogue credits verified).
- [x] Level transitions cleanly unload previous level geometry and advance via `[SPACE] Next Level (N)`.
- [x] Main Menu interactive Chapter Selector allows direct launch into any Chapter (1..6) via `A`/`D` or `Left`/`Right`.
- [x] Persistent chapter unlock state saved across game restarts via LibGDX Preferences (`Spark36Preferences`).
- [x] Automatic victory progression: 5-second countdown timer on victory screen automatically transitions player to next unlocked level.
- [x] Main Menu chapter selection protection: locked chapters display `[LOCKED]` badge and cannot be started until previous chapter is completed.

## 3. AUDIO SYSTEM & SOUND SYNTHESIS
- [x] Procedural 16-bit PCM sound synthesizer (`SoundManager.java`) with zero unlicensed audio dependencies.
- [x] Real-time sound effects: footsteps, punches, kicks, blocks, whooshes, objective chimes, Freedom Bell toll, water splashes, and CS tear gas hisses.
- [x] Safe headless execution support preventing OpenAL crashes on non-audio environments.
- [x] Clean resource disposal freeing OpenAL sound instances on game exit.

## 4. GAMEPLAY & CONTROLS
- [x] Player movement: WASD movement, Shift sprint, C crouch/sit.
- [x] Third-person camera: 360-degree orbit, collision avoidance against buildings/walls.
- [x] Interaction: E key inspects monuments and triggers contextual actions.
- [x] Combat controls: J attack combo (1..5), K block, Space dodge, stamina drain/recovery.
- [x] Campus peace rule: Striking innocent unarmed students triggers instant disqualification toast.
- [x] Pause menu: ESC key opens pause menu with resume, options, and exit buttons.
- [x] Mini-map: M key toggles Dhaka University campus map.

## 5. PERFORMANCE & THERMALS
- [x] Stable 60 FPS target maintained during active campus rendering on fanless M4.
- [x] Max triangle count in frustum stays within 120,000 budget.
- [x] Asset disposal properly frees OpenGL textures and mesh buffers on exit.
