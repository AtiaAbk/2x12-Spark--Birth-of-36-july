# QUALITY ASSURANCE (QA) CHECKLIST
**Project:** Spark: Birth of 36 July
**Last updated:** 2026-10-24, after merging tasks T0–T8 of `docs/OPUS_WORK_ORDER_CONSOLIDATED_REFERENCE_GAPS.md`
**Test machine:** Windows 11, AMD Ryzen 7 5700G with integrated Radeon graphics, 1920×1080 (see `docs/BUILD_STATUS.md`)

**Rule:** a box is ticked only when the item was run and seen on the test machine, or covered by a passing unit test (marked *unit*). Earlier ticks that were never checked here were removed. Macbook M4 results from earlier sessions are not re-verified.

Legend: `[x]` verified · `[~]` verified on a task branch only, or partially · `[ ]` not verified

---

## 1. Build and engine
- [x] `./gradlew.bat :core:build :lwjgl3:compileJava` passes on the merged `work` branch.
- [x] 163 / 163 unit tests pass.
- [x] Game launches through `gradlew run` (launch crash fixed in T0).
- [x] Noto Sans Bengali loads with 0 missing glyphs (startup self-check log).
- [ ] Bengali text is shaped correctly. **Fails:** vowel signs are misplaced because libGDX FreeType does not shape complex scripts.
- [ ] Packaged JAR (`:lwjgl3:dist`) runs on Windows. Not tried.

## 2. Menus, settings and saving
- [x] Main menu shows chapter selector, Arena, Settings, Exit (merged build screenshot, taken before the T4 merge).
- [~] Main menu shows Credits & Sources (T4 branch screenshot only).
- [~] Continue appears only when a save exists (T6 branch screenshot; correctly hidden in the merged build without a save).
- [x] Pause menu has 8 items, Settings on F2, Restart Checkpoint on C, no G-key conflict (merged build screenshot).
- [~] Settings screen opens from main menu and pause menu (T6 branch screenshots).
- [~] Settings persist across a restart (T6 branch: two runs, JSON committed in `docs/screenshots/t6/`).
- [x] Save files live in `<user home>/.spark36/`, never in `assets/` (*unit*).
- [ ] Real mouse and keyboard input in the settings screen. Not exercised.
- [ ] Key rebinding. Deferred (DECISION-T6).

## 3. Campaign and levels
- [x] Level 1 loads and renders (merged build).
- [~] Level 2 is completable with zero knockouts (*unit*; checklist screenshot on T5 branch).
- [x] Striking unarmed students/civilians disqualifies in Levels 2 and 3 (*unit*).
- [x] Checkpoints save, clear on completion and restore (*unit*).
- [ ] Restart from checkpoint exercised in the running game.
- [~] Levels 3–6 street dressing renders (T8 visual branch screenshots).
- [ ] Levels 3–6 characters are lit correctly. **Open:** the fix is reported, but `t8_visual/street_level5_gate_view.png` still shows white students.
- [ ] Full playthrough Level 1 → Level 6 with no soft-lock. Not done on this machine.
- [ ] Arena victory: still allows "subdue all 3 attackers" as a win (kept on purpose in T5; needs a decision).

## 4. History, credits and context
- [x] Every player-visible historical claim maps to a row in `docs/RESEARCH_SOURCES.md` (T2 pass, 38 claims).
- [x] Disputed helicopter gunfire is not shown as fact; Level 6 names no departure location.
- [~] Credits & Sources screen lists the 38 sources grouped by evidence label (T4 branch screenshots).
- [~] Memorial and archive modals show an evidence label and source line (T4 branch screenshot).
- [x] Source catalogue loads and every entry has a label and title (*unit*).

## 5. Screenshots, photo mode, gallery
- [x] Screenshots go to `<home>/Pictures/Spark36`, unique names, never overwritten (*unit* + T7 branch runs).
- [~] F10 photo mode hides the HUD and restores the camera on exit (T7 branch).
- [~] F11 gallery lists captures newest first (T7 branch screenshot).
- [ ] Low-disk toast and "Open folder" button in the running game. Unit-tested only.

## 6. Audio
- [x] Sounds are synthesised in memory; nothing is written to disk (T6).
- [~] Ambience beds for Levels 1–2 and 5–6 with volume control (*unit* for loop seams; not listened to on this machine).

## 7. Performance
- [x] Frame time measured with `-Dbd.spark36.benchmarkSeconds` (method exists since T0).
- [ ] 60 FPS at 1080p. **Fails on this machine:** about 20 FPS before T8, best run about 31.6 FPS after T8 (machine was shared during measurement).
- [x] Draw calls at spawn reduced from 6293 to 2142 per frame (exact counter, T8).
- [ ] Clean benchmark on an idle machine, Level 5 crowd benchmark, long-run thermal test.
