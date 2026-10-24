# BUILD STATUS

**Project:** Spark: Birth of 36 July
**Last verified:** 2026-10-24, task T0 of `docs/OPUS_WORK_ORDER_CONSOLIDATED_REFERENCE_GAPS.md`
**Rule:** this file records only what was actually run on the machine named below. Numbers from other machines are listed separately and marked unverified.

---

## 1. Verified machine

| Item | Value |
|---|---|
| OS | Windows 11 Enterprise 10.0.26100 |
| CPU / GPU | AMD Ryzen 7 5700G with integrated Radeon Graphics |
| GPU driver | 31.0.12027.9001 (2023-03-30); OpenGL 4.6 Compatibility Profile 22.20.27.09.230330 |
| RAM | 15.4 GB usable |
| Display | 1920 × 1080 @ 60 Hz |
| JDK | Microsoft OpenJDK 17.0.18 |
| libGDX / LWJGL | 1.14.2 / 3.4.1 |

---

## 2. Build and tests

```bash
./gradlew.bat :core:build :lwjgl3:compileJava
```

| Check | Result |
|---|---|
| `:core:build` + `:lwjgl3:compileJava` | **PASS** (deprecation notes only) |
| Unit tests at start of T0 (HEAD `75ee36a`) | **87 / 87 pass**, 10 suites, 0 skipped |
| Unit tests after T0 | **91 / 91 pass** (adds `FrameStatsTest`, 4 tests) |
| Unit tests after T1–T8 integration (2026-10-24) | **163 / 163 pass**, 0 failures, 0 errors |

The 87 matches the figure in `CHANGES.md`.

---

## 3. Launch

```bash
./gradlew.bat :lwjgl3:run
```

| Before T0 | After T0 |
|---|---|
| **Crash on every launch** inside the AMD OpenGL driver (`atio6axx.dll`, EXCEPTION_ACCESS_VIOLATION) during `glfwCreateWindow`, before any game code ran. Reproduced with MSAA 8, 4 and 0, fullscreen and windowed. The game had never launched on this machine. | **Launches** fullscreen and windowed; Level 1 renders; screenshots captured. |

**Root cause:** `lwjgl3/build.gradle` copied *all* of Gradle's own JVM system properties into the game JVM (`systemProperties = System.properties`). A standalone GLFW probe opened GL 2.1, 3.2 core, 4.3 core and 4.6 compatibility contexts without error, and the unchanged game launched fine with plain `java`, isolating the cause to the Gradle run task. Fix: forward only `bd.spark36.*` switches.

**ANGLE fallback (`-Dbd.spark36.angle=true`) does not work:** it opens a window, then the gdx-gltf PBR shader fails to compile because it emits desktop GLSL 1.30, which ANGLE's GLES 2.0 mode rejects. Not needed on this machine now; left as is.

### Screenshots from this machine
- `docs/screenshots/t0_windows/main_menu.png`: main menu with chapter selector, Arena, Exit.
- `docs/screenshots/t0_windows/spawn_level1.png`: Level 1 spawn view toward Curzon Hall, HUD and mission tracker.
- `docs/screenshots/t0_windows/pause_menu.png`: 7-item pause menu with controls codex.

`assets/screenshots/` is gitignored, so evidence images live under `docs/screenshots/`.

### Where screenshots go (T7)
- Folder: `-Dbd.spark36.screenshotDir=<path>` if given, else `<user home>/Pictures/Spark36`, else `<user home>/Spark36Screenshots` when there is no Pictures folder. A relative `screenshotDir` resolves against the JVM working directory, which is `assets/` under `gradlew :lwjgl3:run`, so pass an absolute path. Nothing is written under `assets/` any more.
- Player captures (F12, also inside F10 photo mode): `Spark36_yyyyMMdd-HHmmss-SSS.png`, `_2`, `_3`... on collision. HUD-free, never overwritten, never auto-deleted. Under 50 MB free space, or on an IO error, the game shows a toast and writes nothing.
- Harness captures (`bd.spark36.baseline`, `bd.spark36.test*`): `<folder>/<name>.png` with their fixed names, e.g. `<folder>/00_baseline/spawn.png`; these overwrite the previous run. For repo evidence, run with `-Dbd.spark36.screenshotDir=<scratch dir>` and copy the images into `docs/screenshots/`.
- Evidence: `docs/screenshots/t7/photo_mode_f12_capture_*.png` (F12 capture in photo mode, no HUD), `photo_mode_overlay.png` (photo mode with its hint line, harness capture), `gallery_grid.png`, `gallery_viewer.png`.

---

## 4. Measured performance (this machine)

Measured with the new opt-in recorder: `-Dbd.spark36.benchmarkSeconds=30` (3 s warm-up discarded, then 30 s measured, game exits and logs a `[FrameStats]` line). Vsync on. Camera static at a baseline viewpoint, Level 1, no input.

```bash
./gradlew.bat :lwjgl3:run -Dbd.spark36.baseline=spawn -Dbd.spark36.benchmarkSeconds=30
```

| Viewpoint | Resolution | Tier | Avg FPS | Median ms | 1%-low FPS | Max ms |
|---|---|---|---|---|---|---|
| spawn | 1920×1080 | HIGH (shipped default) | **20.5** | 47.6 | 14.3 | 87.7 |
| curzon_front | 1920×1080 | HIGH | **20.1** | 49.2 | 12.1 | 98.4 |
| spawn | 1920×1080 | MEDIUM | 17.9 | 53.7 | 11.9 | 107.8 |
| spawn | 1920×1080 | LOW | 18.8 | 51.9 | 10.6 | 110.2 |
| spawn | 960×540 | HIGH | 24.7 | 38.1 | 13.6 | 95.1 |

**Reading:**
- The documented "60 FPS locked" is **not reached on this machine**: about 20 FPS at 1080p.
- Graphics tiers change nothing measurable. Either the tiers do not touch the expensive work or the bottleneck is elsewhere.
- Quartering the pixel count raises FPS only from about 20 to 25. The limit is mostly **CPU-side or draw-call work**, not GPU fill-rate. Optimisation should start with profiling draw calls and per-frame CPU work, not with lowering visual quality (reference rule: profile first).
- Not measured yet: moving camera, crowds in Levels 2–6, long-run thermal behaviour.

### 4.1 T8 profile and optimisations (spawn, 1080p, HIGH)

New opt-in profiler: `-Dbd.spark36.glProfile=true` (class `bd.spark36.diagnostics.GlStats`) logs once per second the per-frame averages of draw calls, texture binds, shader switches, GL calls, vertices, and CPU ms plus draw calls per frame phase. Add `-Dbd.spark36.glProfileSync=true` to call `glFinish` at every phase end, so each phase time includes its GPU work.

**Profile before T8** (commit 3f07545):

| Metric per frame | Value |
|---|---|
| Draw calls | **6293** (shadow pass 3046, main 3D pass 3226, post 4, HUD 14) |
| Shader switches / GL calls | 520 / 140 700 |
| Vertices submitted | 11.8 M (about 5.9 M per pass) |
| CPU time, update / shadow / world3d / post / HUD | 0.5 / 6.8 / 14–16 / 0.1 / 0.7 ms (about 23 ms of a 43 ms frame) |

Where the draws came from: about 18 characters × about 110 primitives per character, in each pass (about 2000 draws per pass), plus about 1000 un-culled static campus instances in each pass. Characters were also about half the vertices: 145 k indices per body.

| Step (commit) | Draws/frame | Vertices | Avg FPS (best run) | Notes |
|---|---|---|---|---|
| Before (3f07545) | 6293 | 11.8 M | 25.0 (10 s) | CPU 23–25 ms per frame |
| Merge same-material character parts per segment (e92eea7) | 3777 | 11.9 M | 27.5 (10 s) | CPU 24.6 → 11.0 ms; GPU-bound after this |
| Bake static instances into culled per-material chunks (5b8e25c) | 2313 | 10.3 M | 30.9 (best of 2 × 20 s) | 903 instances → 222 chunks; shader switches 519 → 273 |
| Frustum-cull characters per pass (3361370) | 2142 | 9.5 M | 31.6, 1%-low 26.2 (best of 2 × 20 s) | |
| Coarse character rigs in the shadow pass and beyond 20 m (876dfd2) | not measured | not measured | 24.6 vs. 17.4 for the base build in the same contended window | single run pair, see caveat |

After the first fix, `glProfileSync` attribution (GPU-inclusive, contended run): shadow pass about 13 ms, main 3D pass about 19 ms, post-processing about 1.6 ms, HUD about 1.7 ms. The frame is now **GPU vertex-bound** (shadow and main pass), not draw-call-bound.

**Caveat on numbers:** the machine was shared with two other agents that ran the game at the same time. Single runs of the same build ranged from 16 to 31 FPS. The "best run" column is the least-disturbed run. Draw-call and vertex counts are deterministic and are the reliable signal. Game launches were then stopped on the user's request. As a result, there are no clean 30 s runs of the final build at spawn or curzon_front, and Level 5 was not measured. Re-measure on an idle machine:

```bash
./gradlew.bat :lwjgl3:run -Dbd.spark36.baseline=spawn -Dbd.spark36.benchmarkSeconds=30
./gradlew.bat :lwjgl3:run -Dbd.spark36.baseline=curzon_front -Dbd.spark36.benchmarkSeconds=30
./gradlew.bat :lwjgl3:run -Dbd.spark36.level=5 -Dbd.spark36.baseline=curzon_front -Dbd.spark36.benchmarkSeconds=30
```

**Visual check:** none of the optimisations lowers quality. Screenshot diffs at spawn between builds stay within run-to-run noise: a mean of about 2 grey levels, caused by wind, NPC motion and grain. Static geometry and the player are pixel-identical. See `docs/screenshots/t8_perf/spawn_before.png` and `spawn_after.png`. The coarse rigs are used only where they are below the shadow-map texel size (about 3 cm) or below one pixel (bodies more than 20 m away).

**Remaining bottleneck and next step:** GPU vertex throughput on the integrated Radeon, at about 9.5 M vertices per frame before the LOD change. The largest remaining items are:
- the static campus and foliage chunks in the 4096² sun shadow pass;
- the main pass.

Next recommended fixes, in order:
1. Cache the sun shadow map. The sun barely moves, so re-render static casters only when the shadow camera moves by more than one texel, and draw only dynamic casters on top.
2. Add distance LOD or impostors for tree canopies and glade foliage.
3. Re-measure Level 5 crowds (24 NPCs).

---

## 5. Other machines (not re-verified)

Earlier sessions reported "60 FPS locked @ 1080p" on an Apple Silicon MacBook Air M4 (`docs/PROGRESS.md`, `docs/qa_checklist.md`, `CHANGES.md`). There was no FPS measurement code in the repo before T0, so these numbers have no recorded method. Treat them as **unverified** until re-measured with `bd.spark36.benchmarkSeconds`.

---

## 6. Defects found in T0, not yet fixed

| Defect | Evidence | Planned task |
|---|---|---|
| Runtime files are written relative to the working directory. `gradlew run` uses `assets/` as working directory, so screenshots go to `assets/assets/screenshots/` and synthesised sounds to `assets/assets/audio/`. Fixed screenshot names overwrite earlier captures. **Screenshots fixed in T7** (user folder, unique names; see "Where screenshots go"). **Audio fixed in T6**: `SoundManager` now synthesises in memory and writes nothing. | `SparkGame.takeScreenshot`, run logs | done |
| The asset-list task scanned that nested folder and `*.log` crash dumps, so they would have been packed into the game JAR. **Mitigated in T0** by excluding `assets/**` and `*.log` from `generateAssetList`. | `build.gradle` | done (mitigation) |
| `-Dbd.spark36.graphicsTier` was ignored because `assets/visuals.properties` overrode it. **Fixed in T0**: the explicit switch now wins. | `RenderPipelineConfig` | done |
| Shipped default tier was HIGH in the file while every render system actually read MEDIUM. **Resolved in T6**: the saved tier now really applies, and the file default is MEDIUM (DECISION-007, DECISION-T6). | `assets/visuals.properties` | done |
| Pause menu listed `[G] GRAPHICS` while its footer listed `[G] Switch Gender`. **Fixed in T6**: Settings is `[F2]`; G only switches gender. | `pause_menu.png` | done |
| Level 1 frame has a strong green-yellow cast, including the sky, against the reference's natural-colour rule. **Fixed in T8**: sky/fog colours were display-space values drawn into the linear buffer (gamma-encoded twice: pale cyan sky, white fog), plus saturation 1.18 / temperature 0.14 / green tint, an olive ambient and a cyan IBL sky. See section 8. | `spawn_level1.png`, `t8_visual/` | done |
| Levels 3-6 rendered the player, crowd and level props white or flat grey (legacy per-level Environment without IBL; Diffuse-only prop materials ignored by the PBR shader). **Fixed in T8** (world PBR environment, `LevelPbrMaterials`). Per-chapter night/sunset mood is still not implemented. | `t8_visual/before_level5_start.png` | T8 (mood: follow-up) |
| Main menu had no Continue, Settings or Credits. **Fixed in T4 and T6.** | `t6/main_menu_with_continue.png`, `t4/main_menu_credits_item.png` | done |

---

## 9. Final integrated build (branch `work`, 2026-10-24)

All eight task branches (T1+T2, T3, T4, T5, T6, T7, T8 performance, T8 visual) are merged. Build passes and **163 / 163 tests pass**.

**Verified on screen in the merged build:** main menu (Settings shown, Continue correctly hidden without a save), pause menu with 8 items (`[F2] SETTINGS`, `[C] RESTART CHECKPOINT`, defensive-play framing).

**Verified only on each task's own branch, not in the fully merged build:** credits screen, context cards, photo mode and gallery, settings screen, Level 2 shelter objectives, street dressing, colour grade, performance changes. The game was not launched after the last two merges (T8 visual, T8 performance) because the user was working on this PC.

**Not verified at all, or open:**
- Levels 3–6 characters: T8 visual reports the white-character bug fixed, but its own `t8_visual/street_level5_gate_view.png` still shows white students. Needs one look in game.
- Restart-from-checkpoint and the H controls overlay: unit-tested, never exercised in the running game.
- Real mouse/keyboard input in the settings screen.
- Final FPS on an idle machine. Best measured run after T8 performance: about 31.6 FPS avg, 1%-low 26 at spawn (was 20–25). Draw calls 6293 → 2142 per frame (exact). Commit `876dfd2` (coarse shadow twins) rests on one noisy run pair.
- Bengali text shaping: libGDX FreeType does no complex-script shaping, so Bengali vowel signs are misplaced everywhere (visible in `t4/credits_sources.png`).

**Re-check commands (opens the game window):**
```bash
./gradlew.bat :lwjgl3:run -Dbd.spark36.baseline=spawn -Dbd.spark36.benchmarkSeconds=30
./gradlew.bat :lwjgl3:run -Dbd.spark36.level=5 -Dbd.spark36.baseline=gate_street -Dbd.spark36.autoExit=true
```

---

## 8. T8 visual pass (colour and street dressing)

Grade now: exposure 1.08, contrast 1.02, saturation 1.04, temperature 0.08, tint -0.01, shadow lift 0.006 (`visuals.properties` and `RenderPipelineConfig` defaults). Sky, fog and post-process clear colours are linear values (`AtmosphereConfig`, `AtmosphereRenderer`, `HdrPostProcessor`); IBL sky bluer and ground bounce less yellow (`PbrEnvironment`); Curzon brick texture less saturated (`TextureFactory`). Evidence: `docs/screenshots/t8_visual/` (before/after spawn, curzon_front, Level 5 start; street views for Levels 4, 5, 6).

Street dressing (Levels 3-6, `level/StreetDressing` + `world/landmarks/*Builder`): about 20 props per level in one `ModelCache`, logged at load as about **22 draw calls and 23k triangles per pass** (colour and shadow). Measured FPS, fullscreen 1080p, HIGH tier, 30 s, **while other agents ran the game on the same machine** (run-to-run noise about +-3 FPS, some runs interrupted by user input):

| View | Without street (0f68242) | With street |
|---|---|---|
| Level 4 `gate_street` | 21.6 | 18.8, 20.0, 19.7, 17.4 |
| Level 6 `street_road` | 20.8 | 17.5, 21.3 |
| Same-session A/B with `-Dbd.spark36.streetDressing=false` (L6) | 18.9, 16.9 | 17.5, 21.3 |

The difference is inside the noise; re-measure on an idle machine. Not measured on an idle machine.

## 7. Useful run switches

| Switch | Effect |
|---|---|
| `-Dbd.spark36.windowed=true` | 1440×900 window instead of fullscreen |
| `-Dbd.spark36.width=W -Dbd.spark36.height=H` | Custom window size |
| `-Dbd.spark36.msaa=N` | MSAA samples (default 8) |
| `-Dbd.spark36.graphicsTier=LOW\|MEDIUM\|HIGH` | Force tier (now honoured) |
| `-Dbd.spark36.baseline=<viewpoint> -Dbd.spark36.autoExit=true` | Capture a baseline screenshot and exit |
| `-Dbd.spark36.level=N -Dbd.spark36.baseline=gate_street` or `street_road` | T8 views of the Doel Chattar road outside the main gate (Levels 3-6 street dressing) |
| `-Dbd.spark36.devKeys=true` | Enable developer shortcuts: F3 debug normals, F4/O facade test card, F6 skip to next chapter (off for players) |
| `-Dbd.spark36.streetDressing=false` | Turn the Level 3-6 street dressing off (A/B measurements) |
| `-Dbd.spark36.dumpSignAtlas=<absolute .png path>` | Write the Bengali signboard atlas as rendered |
| `-Dbd.spark36.benchmarkSeconds=N` | Measure N seconds after 3 s warm-up, log `[FrameStats]`, exit |
| `-Dbd.spark36.testMenuScreenshot=true` / `testPauseMenu=true` | Capture menu / pause screenshots |
| `-Dbd.spark36.screenshotDir=<absolute path>` | Screenshot folder (default `<home>/Pictures/Spark36`) |
| `-Dbd.spark36.autoStartGame=true -Dbd.spark36.testPhotoMode=true -Dbd.spark36.testGallery=true -Dbd.spark36.autoExit=true` | Enter photo mode, capture its overlay (`t7/photo_mode_overlay`) and a real F12-path capture, leave photo mode, open the gallery, capture grid and viewer (`t7/gallery_grid`, `t7/gallery_viewer`), exit |
