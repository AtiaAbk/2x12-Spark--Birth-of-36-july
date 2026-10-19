# Spark: Birth of 36 July — Master Fix & Overhaul Changelog
**Project**: 2x12-Spark--Birth-of-36-july  
**Date**: October 20, 2026  
**Safety Backup Branch**: `backup/before-gameplay-master-overhaul` (Commit: `1256654`)  
**Test Suite Status**: 87/87 Unit Tests Passed (100% Green)

---

## 1. Executive Summary

This overhaul resolves five critical gameplay and visual regressions that occurred following a recent graphics and render pipeline upgrade:
1. **Unresponsive Buttons & Controls**: `[Fight]`, `[Exit]`, `[Map]` (M key), Pause menu, and HUD buttons failed to trigger or behaved erratically.
2. **Character Movement Freeze**: The protagonist became permanently immobilized upon interacting with any object or opening any modal.
3. **Broken Level Progression**: Levels were disconnected; winning or finishing a level failed to transition to subsequent chapters or returned to Chapter 1.
4. **Color Grading & Tone Mapping Imbalance**: Post-processing was hardcoded and prone to looking cold, pale, and washed-out rather than capturing a natural Bangladeshi summer in July.
5. **Lack of User-Friendly UX & Feedback**: Missing on-screen controls codex, no feedback when combat is peacefully disabled in Chapter 1, and no intuitive mouse/keyboard navigation.

---

## 2. Root Cause Analysis & Technical Solutions

### Issue 1: Buttons Unresponsive (`Fight`, `Exit`, `[M]` Map, HUD Click Targets)
- **Root Cause A (Early Return in HUD Update Loop)**:
  In `core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java` (lines 678–687), when `activeLevel != null` (which was true in all gameplay levels), the update loop executed:
  ```java
  if (activeLevel != null) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { ... }
      return; // <--- DEADLY PREMATURE RETURN!
  }
  ```
  This single premature `return;` caused the HUD update routine to exit immediately every frame. It completely skipped:
  - `[M]` and `[TAB]` for opening the Dhaka University campus map.
  - `[ESCAPE]` for opening the pause menu and dismissing active modals.
  - `[P]` for toggling objective tracking and parameters.
  - `[H]` for the controls guide.
  - All pause menu arrow/WASD navigation and shortcut execution (`[R]` restart, `[T]` tone map, `[Q]` main menu, `[X]` exit).
- **Root Cause B (High-DPI / Retina Mouse Coordinate Distortion)**:
  In `WhereWindsMeetHUD.java`, mouse virtual positions were computed using:
  ```java
  float mouseX = ((float) Gdx.input.getX() / backbufferW) * virtW;
  ```
  On macOS Retina / high-DPI displays, `Gdx.input.getX()` is reported in logical window points (e.g. 1280px), whereas `getBackBufferWidth()` is the physical resolution (2560px). Dividing by `backbufferW` halved the mouse coordinate, placing mouse hitboxes 50% away from the visual button bounding boxes.
- **Root Cause C (Continuous Cursor Trapping During Menus)**:
  In `core/src/main/java/bd/spark36/SparkGame.java`, the game loop enforced `Gdx.input.setCursorCatched(true)` whenever `!hud.isModalOpen()`. Because `isModalOpen()` originally omitted pause menus, maps, and victory screens, the OS cursor was trapped and invisible, preventing the player from clicking HUD buttons.
- **Fix**:
  1. Removed the premature `return;` in `WhereWindsMeetHUD.java`. Wrapped level interaction cleanly so the update loop continues.
  2. Created `bd.spark36.input.GameInput` with `getVirtualMousePos(virtW, virtH)` dividing logical `Gdx.input.getX()` by `Gdx.graphics.getWidth()`.
  3. Updated `WhereWindsMeetHUD.isModalOpen()` to return `true` whenever any modal, map, pause menu, exit dialog, or controls codex is open. `SparkGame.java` automatically frees the cursor when menus are active.

---

### Issue 2: Characters Do Not Move (Movement Freeze)
- **Root Cause**:
  In `SparkGame.java`, player movement is guarded by:
  ```java
  boolean canMove = !hud.isModalOpen() && !hud.isNpcDialogueActive();
  ```
  When the player inspected an archive, monument, or boundary stone, `activeModalEntry` became non-null (`hud.isModalOpen() == true`). Because the premature `return;` in `WhereWindsMeetHUD.java` prevented `ESCAPE` or `E` from clearing `activeModalEntry`, `activeModalEntry` was stuck open indefinitely. As a result, `canMove` remained `false` forever.
- **Fix**:
  1. Connected modal dismissal to `GameAction.CANCEL` (`ESCAPE`) and `GameAction.INTERACT` (`E` / `ENTER`).
  2. Centralized all character input in `PlayerController.java` to use `GameInput.isPressed(...)` and `GameInput.isJustPressed(...)`, supporting dual WASD + Arrow keys, Space/J for Jump, Shift/R for Sprint, and C/Ctrl for Crouch.

---

### Issue 3: Disconnected Levels and Broken Campaign Flow
- **Root Cause A (Conflicting Duplicate Victory Blocks)**:
  In `WhereWindsMeetHUD.java`, an initial victory block set `exitAction = 3` or `4`, but a duplicate block further down unconditionally forced:
  ```java
  if (isVictoryOpen) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          exitAction = 1; // Returned to Main Menu instead of Next Level!
      }
  }
  ```
- **Root Cause B (Ambiguous Exit Action Codes)**:
  `exitAction == 3` was overloaded for both "Restart Level" and "Next Level". If the level's completion boolean was cleared during victory transition, `SparkGame.java` fell back to restarting the current level.
- **Root Cause C (Clamped Chapter Selection on Return to Menu)**:
  In `SparkGame.java` `returnToMainMenu()`, `selectedChapter` was calculated as `Math.min(unlockedLevel, activeLevel.getLevelNumber())`. Even after completing Level 1 and unlocking Level 2, the menu forced Chapter 1 selection.
- **Fix**:
  1. Separated exit actions into explicit, non-overlapping codes:
     - `1`: Return to Main Menu
     - `2`: Quit Game (`Gdx.app.exit()`)
     - `3`: Restart Current Level (`levelManager.restartCurrentLevel()`)
     - `4`: Advance to Next Level (`levelManager.nextLevel()`)
  2. Fixed Victory Screen inputs: `[ENTER]` and `[SPACE]` trigger `exitAction = 4` to seamlessly transition to the next chapter.
  3. Updated `returnToMainMenu()` to set `selectedChapter = LevelConfig.getUnlockedLevel()`, letting players immediately launch their newly unlocked chapter.

---

### Issue 4: Washed-Out Post-Processing & Color Grading
- **Root Cause**:
  Hardcoded shader uniforms in `RenderPipelineConfig.java` lacked a dedicated summer color profile and had no external configuration mechanism.
- **Fix**:
  1. Created `assets/visuals.properties` with tuned parameters for a Bangladeshi July summer:
     - `exposure=1.02`, `contrast=1.05`, `saturation=1.18`, `colorTemperature=0.14` (warm sunlight), `colorTint=0.02`, `shadowLift=0.005`.
  2. Added dynamic properties loader in `RenderPipelineConfig.java`.
  3. Implemented in-game graphics quality cycling (`LOW`, `MEDIUM`, `HIGH`) via `[G]` and tone mapping cycling via `[T]` in the pause menu.

---

### Issue 5: Unfinished Feel & UX Polish
- **Fix**:
  1. Built an on-screen **Controls & Historical Field Codex** (`[H]` / `[F1]`) with frosted glass styling and complete key/mouse breakdowns.
  2. Added an in-game **Peaceful Exploration Toast**: In Chapter 1, pressing combat buttons (`Left-Click`, `F`, `Q`) presents a friendly toast explaining that Chapter 1 depicts the historic peaceful student movement (July 1–14, 2024), and combat unlocks in Chapter 2 ("The Day the Streets Changed") and Arena Mode.
  3. Redesigned the Pause Menu with 7 aligned options: Resume, Controls Codex, Graphics Quality, Tone Map, Restart Level, Main Menu, and Quit Game.

---

## 3. Files Created and Modified

| File | Change Type | Summary of Changes |
|---|---|---|
| `core/src/main/java/bd/spark36/input/GameInput.java` | Created | Central action-to-key mapper, dual-scheme keyboard/mouse bindings, and high-DPI virtual mouse scaling. |
| `assets/visuals.properties` | Created | External config for photographic exposure, warmth, saturation, bloom, and graphics tiers. |
| `assets/assets.txt` | Modified | Registered `visuals.properties` in the asset manifest. |
| `core/src/main/java/bd/spark36/render/RenderPipelineConfig.java` | Modified | Added `visuals.properties` loader, graphics tier cycling, and safe null checks for headless runs. |
| `core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java` | Modified | Removed blocking `return;`, wired `GameInput`, added Controls Codex overlay, fixed Retina mouse hitboxes, unified victory flow. |
| `core/src/main/java/bd/spark36/character/PlayerController.java` | Modified | Refactored movement, jump, sprint, crouch, dodge, and combat strikes to use `GameInput`. |
| `core/src/main/java/bd/spark36/SparkGame.java` | Modified | Updated cursor capture management, separated `exitAction` 3 and 4, added peaceful combat toast, preserved unlocked chapters in menu. |
| `core/src/test/java/bd/spark36/input/GameInputAndLevelFlowTest.java` | Created | Automated JUnit 5 test suite covering input bindings, mouse coords, level progression, and pipeline cycling. |

---

## 4. Verification & Testing Evidence

1. **Automated Unit Tests**:
   - Ran `./gradlew test --no-daemon`
   - **Result**: `87 tests completed, 0 failed` (100% Success).
2. **Runtime Verification**:
   - `assets/screenshots/pause_menu_interactive_verified.png`: Verified 7-item pause menu with proper layout, borders, and shortcuts.
   - `assets/screenshots/controls_codex_verified.png`: Verified dual-column Controls Codex overlay.
   - `assets/screenshots/spark36_menu_verified.png`: Verified chapter select and navigation.
