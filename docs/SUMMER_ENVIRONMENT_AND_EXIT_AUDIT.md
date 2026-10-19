# SPARK: BIRTH OF 36 JULY — SUMMER ENVIRONMENT & EXIT SYSTEM AUDIT

**Project**: *Spark: Birth of 36 July* (৩৬ জুলাই: স্ফুলিঙ্গ)  
**Setting**: University of Dhaka Campus, July–August 2024 Mass Student Movement  
**Target Hardware**: MacBook Air M4 (Apple Silicon), 60 FPS  
**Date**: October 20, 2026  
**Auditor**: Senior Graphics Engineer, Environment Artist & QA Engineer  

---

## 1. Problem Statement & Audit Objectives

The project currently exhibits two critical defects affecting player experience and historical authenticity:

1. **Pale, Washed-Out, Cold/Winter-Like Environment**:
   - Rather than depicting a hot, humid, sun-drenched Bangladeshi summer in July 2024, the game environment appears pale, desaturated, greyish-cyan, and cold.
   - Terracotta brickwork on Curzon Hall appears chalky, foliage appears desaturated, and shadows suffer from a milky grey veil.
2. **Missing/Non-Functional Pause & Exit System**:
   - There is no obvious, functional pause menu that allows safe navigation, resuming, restarting, or quitting.
   - Pressing `X` in the HUD abruptly triggers `Gdx.app.exit()` without an exit confirmation modal.
   - Gameplay simulation, NPC movement, footstep sounds, and mission timers continue running in the background even when the pause overlay is open.
   - The Main Menu does not respond to mouse clicks on the `EXIT GAME` button.
3. **Vegetation Visual Realism**:
   - Broadleaf trees need to match the botanical reference specification (`Broadleaf_Green_Tree_Reference`): 7 major spreading limbs, asymmetrical multi-lobed canopy, rich layered greens, and textured bark.
   - A dedicated dense bamboo grove (`Dense_Realistic_Bamboo_Clump`) must be created according to the 5×5m footprint, 24–42 segmented culms, raised horizontal node rings, and narrow lance-shaped foliage sprays.
   - Coconut palms (`tropical_palm_tree`) must feature curved ringed trunks, full pinnate compound fronds, and natural age variation (fresh green crowns to drooping tan lower fronds).

---

## 2. Root Cause Analysis: Rendering & Color Pipeline

A line-by-line inspection of the rendering pipeline revealed five compounding causes for the washed-out, winter-like appearance:

### A. Distance Fog Equation Distance & Color (`AtmosphereConfig.java`)
- **Current Parameters**:
  - `fogNear = 35.0f`, `fogFar = 140.0f` in `CLEAR_SUNNY`.
  - `fogColor = Color(0.84f, 0.88f, 0.92f)` (high-luminance cold pale cyan-white).
- **Diagnosis**:
  - The distance from the spawn promenade ($Z = 72\text{m}$) to Curzon Hall ($Z = -20\text{m}$) is **92 meters**.
  - At 92m, the linear fog factor evaluates to:
    $$\text{fog} = \frac{92 - 35}{140 - 35} = \frac{57}{105} \approx \mathbf{54.3\%}$$
  - More than half of Curzon Hall's red brick facade and background trees are blended with a bright pale cold-white color (`#D6E0EB`), creating an intense milky blanket that destroys contrast and saturation.
- **Correction**:
  - Push `fogNear` out to **$75.0\text{m} - 80.0\text{m}$** and `fogFar` to **$240.0\text{m} - 260.0\text{m}$** for clear summer daylight.
  - Shift `fogColor` to warm summer atmospheric haze (`Color(0.74f, 0.78f, 0.80f)`), allowing the humid atmosphere to provide depth without blanching architecture and foliage.

### B. Ambient & Fill Light Color Cast (`DhakaCampusWorld.java`, `AtmosphereConfig.java`)
- **Current Parameters**:
  - `ambientColor = Color(0.32f, 0.35f, 0.40f)` (cold blue dominant over red/green).
  - `fillLight = Color(0.20f, 0.22f, 0.25f)` (cold blue-grey directional fill).
- **Diagnosis**:
  - In shaded verandas and under tree canopies, this cold blue ambient light turns warm terracotta bricks into slate-purple and rich green leaves into dull olive-grey.
- **Correction**:
  - Recalibrate ambient lighting to warm daylight bounce with natural chlorophyll reflection: `Color(0.38f, 0.40f, 0.35f)`.

### C. Turf & Ground Material Albedo (`TextureFactory.java`)
- **Current Parameters**:
  - `createLawnGrassWithNormal()` used base color `(0.05f, 0.14f, 0.05f)`.
- **Diagnosis**:
  - An sRGB green value of `0.14` translates to only $\approx 0.013$ albedo in linear space.
  - The grass absorbed over 98% of incident light, appearing nearly black before the pale fog washed over it.
- **Correction**:
  - Calibrate turf albedo to healthy monsoon summer grass (Bermuda / Durba grass): sRGB green `0.45 - 0.58`, with multi-frequency blade striations and warm alluvial soil tones.

### D. Post-Processing Grading & Shadow Lift (`postprocess.frag`, `RenderPipelineConfig.java`)
- **Current Parameters**:
  - `shadowLift = 0.02f` added scalar white `col + vec3(lift * factor)` directly to shadows, washing out shadow contrast into grey milk.
  - `saturation = 1.04f` (under-saturated for a tropical summer).
  - `colorTemperature = 0.0f` (sterile neutral white balance).
- **Correction**:
  - Set `colorTemperature = 0.14f` for warm tropical daylight.
  - Set `saturation = 1.18f` to restore rich natural chlorophyll greens and terracotta reds.
  - Preserve shadow contrast without artificial scalar whitening.

---

## 3. Pause Menu & Exit Lifecycle Audit

### A. Current Implementation Gaps
1. **Simulation Leakage**: In `SparkGame.java`, `canMove` only checked `!hud.isModalOpen()`. When `isPauseMenuOpen == true`, `delta` was still passed to `player.update()`, `levelManager.update()`, `helmetGoon.update()`, `particleSystem.update()`, and `SoundManager.updateFootsteps()`.
2. **Abrupt Exit without Confirmation**: Pressing `X` immediately executed `exitAction = 2`, which called `Gdx.app.exit()` directly with no safety confirmation.
3. **No Interactive Menu Selection**: The pause overlay was purely a static text display of keyboard shortcuts. It had no cursor navigation (`W/S` or `Up/Down`), no `ENTER/SPACE` selection, and no mouse click detection.
4. **Main Menu Mouse Click**: `SparkGame.java` only listened to keyboard keys (`ENTER`, `SPACE`, `J`) for menu options; clicking the `EXIT GAME` button with the mouse did not trigger exit.

### B. Planned Architecture
- Introduce explicit menu state machine: `PAUSE_NAVIGATION` and `EXIT_CONFIRMATION`.
- Freeze delta-time / simulation when paused (`canMove = false`, zero movement, paused timers).
- Provide interactive buttons with keyboard focus navigation and mouse hover/click:
  1. **Resume Game**
  2. **Restart Level**
  3. **Visual & Audio Settings** (cycle tone mapping, toggle HUD parameters)
  4. **Return to Main Menu**
  5. **Exit Game**
- On selecting "Exit Game", display **Exit Confirmation Dialog**:
  - Message: *"Exit Game? Are you sure you want to quit? Unsaved progress will be lost."*
  - Interactive Buttons: `[CANCEL]` and `[EXIT TO DESKTOP]`.
- Main Menu: add full mouse click hit-testing for all buttons.

---

## 4. Vegetation Specification Mapping

| Asset Type | Reference Specification | Implementation Details |
| :--- | :--- | :--- |
| **Broadleaf Tree** | `Broadleaf_Green_Tree_Reference` | 10.0m height, 11.5m crown width, 7 spreading scaffold limbs, asymmetrical multi-lobed canopy, textured longitudinal bark grooves (`#34372A`, `#4A4A32`, `#62563A`, `#81734B`), layered leaf clusters with 5 calibrated greens (`#17451E` to `#8BCB45`). |
| **Bamboo Grove** | `Dense_Realistic_Bamboo_Clump` | 5×5m footprint, 32 culms (heights 4.5–10m, diameters 0.035–0.095m), raised node rings every 0.22–0.48m, natural lean (-5 to +5 deg), culm palette (`#183B20`, `#42652B`, `#68713A`, `#92733B`, `#9A815D`), narrow lance-shaped leaves in sprays (`#123D23` to `#79A957`). |
| **Coconut Palm** | `tropical_palm_tree` | Curved slender trunks with horizontal ring scars, full pinnate compound fronds with thin individual leaflet quads, young upright fresh green fronds, arching medium-green fronds, drooping dry tan/brown fronds. |
