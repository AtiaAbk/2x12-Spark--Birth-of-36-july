# ASSET LICENSE AND ATTRIBUTION REGISTER

**Project:** Spark: Birth of 36 July (Repository: `2x12`)
**Last audited:** 2026-10-24 (work order task T4), against `assets/assets.txt`, `lwjgl3/src/main/resources/`, the Gradle build files and the code that loads each file.
**Rule:** this register lists what actually ships, not what is planned. Every file in `assets/` and every bundled library has a row. If provenance is not documented it says **unknown**; nothing here is guessed. Reference §11.1 / work order §5: no ripped, unlicensed or "fair use" audio or models.

The in-game **Credits & Sources** screen (main menu > CREDITS & SOURCES, and the Level 6 epilogue objective) shows the fonts, libraries and procedural-content statement below. Keep the two in step (`core/src/main/java/bd/spark36/hud/screens/CreditsContent.java`).

---

## 1. Fonts (`assets/fonts/`)

| File | Upstream | Licence | Licence text in repo | Used by |
|---|---|---|---|---|
| `NotoSansBengali-Variable.ttf` | The Noto Project Authors, https://github.com/notofonts/bengali (Copyright 2022) | SIL Open Font License 1.1 | `assets/fonts/LICENSE-NotoSansBengali.txt` | `hud/FontRenderer` (Bengali dialogue, subtitles, Bengali titles) |
| `LiberationSans-Regular.ttf`, `LiberationSans-Bold.ttf` | Liberation Fonts, https://github.com/liberationfonts (Copyright (c) 2012 Red Hat, Inc.; digitized data (c) 2010 Google Corporation) | SIL Open Font License 1.1 | `assets/fonts/LICENSE-LiberationSans.txt` | `hud/FontRenderer` (all Latin UI text) |

No other fonts ship. (Earlier versions of this file listed Cinzel Decorative / EB Garamond; they are **not in the repository** and have been removed from this register.)

---

## 2. Images

| File | Size | Provenance | Licence | Used by |
|---|---|---|---|---|
| `assets/maps/campus_map.png` | 1264 × 795 | **Unknown.** First committed in `f0fba93` (2026-09-24) with no source note. It is an illustrated map with blurred labels (see `LOCATION_CurzonHall.md`); who made it and how is not documented. | **Unknown** | `WhereWindsMeetHUD` tactical map overlay (M key) |
| `assets/cinematic_finish_bg.png` | 1024 × 558 | **Unknown.** No source note in the commit history. | **Unknown** | `WhereWindsMeetHUD` cinematic finish / victory background |
| `assets/libgdx.png` | 378 × 63 | libGDX logo from the project template (gdx-liftoff). | libGDX project material | **Not used at run time** (template leftover; kept in repo) |
| `lwjgl3/src/main/resources/libgdx16/32/64/128.png` | 16–128 px | libGDX logo icons from the project template (gdx-liftoff). | libGDX project material | `DesktopLauncher` window icon |

**Action needed (owner):** confirm who created `campus_map.png` and `cinematic_finish_bg.png` and under what terms. Until then they must not be described as licensed or CC0 anywhere.

---

## 3. Shaders and configuration (project-original)

| File | Origin | Used by |
|---|---|---|
| `assets/shaders/postprocess.vert`, `postprocess.frag`, `bloom_extract.frag`, `bloom_blur.frag` | Written for this project | `render/HdrPostProcessor` |
| `assets/shaders/water.vert`, `water.frag` | Written for this project | `render/WaterRenderer` |
| `assets/visuals.properties` | Project configuration | `render/RenderPipelineConfig` |
| `assets/data/historical_sources.json` | Project research data (mirror of `docs/RESEARCH_SOURCES.md`) | `history/SourceCatalog` (credits screen, memorial context cards) |

---

## 4. Audio

All sound the player hears is **synthesised in memory at run time** by `audio/SoundManager` and `audio/AmbienceSynth` (effects, bell, footsteps, tear-gas hiss, campus and crowd ambience beds). No recordings are used.

| File | Status | Origin |
|---|---|---|
| `assets/audio/bell.wav`, `block.wav`, `footstep.wav`, `gas_hiss.wav`, `kick.wav`, `objective.wav`, `punch.wav`, `splash.wav`, `ui_click.wav`, `whoosh.wav` (10 files) | **Unused at run time, kept in repo.** `SoundManager` now builds the same sounds in memory and never reads these files. | Output of earlier builds of this project's own synthesiser (which wrote .wav files to disk, see the `SoundManager` class comment). Not third-party material. |

Removed from this register because they **were never in the repository**: the CC0 audio packs (Sonniss GDC, Freesound, "Bangladesh Wildlife Sound Archive") and the row **"Distant Mass Chants — July 2024 archival recordings (Fair Use)" — NOT USED**. No archival or protest recording ships, and none may be added unless it is legally cleared and registered here (reference §11.1).

---

## 5. Textures and 3D geometry (procedural)

All textures are generated as pixmaps in code (`world/TextureFactory`), and all meshes are built in code with libGDX `ModelBuilder` (buildings, trees, characters, props). No texture image or 3D model file ships.

Removed from this register because they **were never in the repository**: the AmbientCG / Poly Haven CC0 PBR texture rows (brick, plaster, flagstone, grass, teak, water normal, iron railing) — **NOT USED**.

---

## 6. Software libraries (from `build.gradle`, `core/build.gradle`, `lwjgl3/build.gradle`, `gradle.properties`)

| Library | Version | Group / artifact | Licence | Purpose |
|---|---|---|---|---|
| libGDX (`gdx`, `gdx-backend-lwjgl3`, `gdx-platform` natives) | 1.14.2 | `com.badlogicgames.gdx` | Apache License 2.0 | Engine core, desktop backend |
| gdx-freetype (+ `gdx-freetype-platform` natives) | 1.14.2 | `com.badlogicgames.gdx` | Apache License 2.0; bundled FreeType library under the FreeType License (FTL), credit shown in game | Runtime TTF rasterisation |
| gdx-lwjgl3-angle | 1.14.2 | `com.badlogicgames.gdx` | Apache License 2.0 (wraps ANGLE, BSD-3-Clause) | Optional ANGLE backend (`-Dbd.spark36.angle=true`) |
| LWJGL 3 (`lwjgl`, `-glfw`, `-jemalloc`, `-openal`, `-opengl`, `-stb`) | 3.4.1 | `org.lwjgl` | BSD-3-Clause | Windowing, OpenGL, OpenAL |
| gdx-gltf | 2.2.1 | `com.github.mgsx-dev.gdx-gltf:gltf` | Apache License 2.0 | PBR shaders and material attributes |
| JUnit Jupiter | 5.10.0 | `org.junit.jupiter` | EPL 2.0 | Tests only, not shipped |

---

## 7. Audit rules

1. Every binary file added to `assets/` or `lwjgl3/src/main/resources/` needs a row here with upstream URL and licence before it is committed. Unknown provenance is written as **unknown**, never guessed.
2. No "fair use" or unlicensed media. No recordings of real protests or real people.
3. When a file stops being used, mark it "unused, kept in repo" or delete it; do not leave it described as in use.
4. Re-check this file against `assets/assets.txt` whenever assets change.
