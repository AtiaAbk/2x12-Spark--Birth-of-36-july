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

---

## 5. Other machines (not re-verified)

Earlier sessions reported "60 FPS locked @ 1080p" on an Apple Silicon MacBook Air M4 (`docs/PROGRESS.md`, `docs/qa_checklist.md`, `CHANGES.md`). There was no FPS measurement code in the repo before T0, so these numbers have no recorded method. Treat them as **unverified** until re-measured with `bd.spark36.benchmarkSeconds`.

---

## 6. Defects found in T0, not yet fixed

| Defect | Evidence | Planned task |
|---|---|---|
| Runtime files are written relative to the working directory. `gradlew run` uses `assets/` as working directory, so screenshots go to `assets/assets/screenshots/` and synthesised sounds to `assets/assets/audio/`. Fixed screenshot names overwrite earlier captures. **Screenshots fixed in T7** (user folder, unique names; see "Where screenshots go"); the audio half is a separate task. | `SparkGame.takeScreenshot`, run logs | T7 (screenshots done) |
| The asset-list task scanned that nested folder and `*.log` crash dumps, so they would have been packed into the game JAR. **Mitigated in T0** by excluding `assets/**` and `*.log` from `generateAssetList`. | `build.gradle` | done (mitigation) |
| `-Dbd.spark36.graphicsTier` was ignored because `assets/visuals.properties` overrode it. **Fixed in T0**: the explicit switch now wins. | `RenderPipelineConfig` | done |
| Shipped default tier is HIGH, while repo DECISION-007 names MEDIUM as the target default. Left unchanged: the measurements show no tier gains anything here. | `assets/visuals.properties` | T6 settings |
| Pause menu lists `[G] GRAPHICS` while its footer lists `[G] Switch Gender`. | `pause_menu.png` | T6 |
| Level 1 frame has a strong green-yellow cast, including the sky, against the reference's natural-colour rule. | `spawn_level1.png` | T8 |
| Main menu has no Continue, Settings or Credits. | `main_menu.png` | T4, T6 |

---

## 7. Useful run switches

| Switch | Effect |
|---|---|
| `-Dbd.spark36.windowed=true` | 1440×900 window instead of fullscreen |
| `-Dbd.spark36.width=W -Dbd.spark36.height=H` | Custom window size |
| `-Dbd.spark36.msaa=N` | MSAA samples (default 8) |
| `-Dbd.spark36.graphicsTier=LOW\|MEDIUM\|HIGH` | Force tier (now honoured) |
| `-Dbd.spark36.baseline=<viewpoint> -Dbd.spark36.autoExit=true` | Capture a baseline screenshot and exit |
| `-Dbd.spark36.benchmarkSeconds=N` | Measure N seconds after 3 s warm-up, log `[FrameStats]`, exit |
| `-Dbd.spark36.testMenuScreenshot=true` / `testPauseMenu=true` | Capture menu / pause screenshots |
| `-Dbd.spark36.screenshotDir=<absolute path>` | Screenshot folder (default `<home>/Pictures/Spark36`) |
| `-Dbd.spark36.autoStartGame=true -Dbd.spark36.testPhotoMode=true -Dbd.spark36.testGallery=true -Dbd.spark36.autoExit=true` | Enter photo mode, capture its overlay (`t7/photo_mode_overlay`) and a real F12-path capture, leave photo mode, open the gallery, capture grid and viewer (`t7/gallery_grid`, `t7/gallery_viewer`), exit |
