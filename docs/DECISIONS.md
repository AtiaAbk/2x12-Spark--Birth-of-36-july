# ARCHITECTURAL & DESIGN DECISIONS REGISTER
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Standard:** Every major design choice, conflict resolution, alternative considered, rationale, and rollback procedure recorded here.

---

## 1. CONFLICT RESOLUTIONS (FROM MASTER BRIEF §2)

### DECISION-001: Level 1 Combat & "Helmet Goon" Relocation
- **Context & Conflict:** The master plan mandates that Level 1 ("Where It All Began") is entirely peaceful (exploration, social participation, gathering, trust building, zero weapons, zero enemies, zero player death). However, the existing prototype contained a combat encounter with `HelmetGoon` placed directly in front of Curzon Hall.
- **Chosen Decision:** 
  1. Keep 100% of the combat code, animation timings, poise system, and `HelmetGoon` implementation intact without regression.
  2. Implement a global `GameMode` and per-level capability flag (`levelConfig.combatEnabled = false` for Level 1 Story Mode).
  3. When `combatEnabled` is false: player punches/kicks are disabled, `HelmetGoon` is not spawned in the campus plaza, boss health bar is hidden, and player death is impossible.
  4. Relocate `HelmetGoon` and the martial arts combat suite into the dedicated **Arena / Lathi Khela Training Mode** (and later justified story scenes).
- **Alternatives Considered:**
  - *Delete HelmetGoon entirely:* Rejected — violates the rule to preserve working systems, destroys combat gameplay developed for the project.
  - *Leave boss fight in Level 1:* Rejected — completely contradicts the historical truth of the peaceful June–July 14 period.
- **How to Reverse:** Set `combatEnabled = true` in Level 1 configuration or launch via `-Dbd.spark36.forceCombat=true`.

---

### DECISION-002: Level 1 Spatial Scope & Map Alignment
- **Context & Conflict:** The user's planning notes listed extensive science faculty buildings (Applied Physics, Geology, Fisheries, Applied Chemistry, Zoology, Botany, Chemistry, Biochemistry, Shahid Minar), whereas `assets/maps/campus_map.png` defines a beautifully illustrated layout focused on the Curzon Hall core campus zone (Curzon Hall, Pukur, Arts Plaza, Aparajeyo Bangla, Central Library, Madhur Canteen, Hakim Chattar, TSC, Raju Roundabout, Swadhinata Sangram).
- **Chosen Decision:**
  1. Adopt `campus_map.png` as the primary spatial authority for the Milestone 3 Vertical Slice.
  2. Build the Curzon core zone first to 1:1 metric scale (`docs/research/WORLD_SCALE_AND_COORDINATES.md`).
  3. Reconstruct surrounding departments (Chemistry, Physics, Botany) as architectural wings and perimeter structures flanking Curzon Hall and the Pukur in the extended Level 1 perimeter.
- **Alternatives Considered:**
  - *Sprawl into a massive 4 km² Dhaka map immediately:* Rejected — would destroy the frame budget on MacBook Air M4, dilute visual polish, and leave all buildings looking like low-detail boxes.
- **How to Reverse:** Add sub-sectors to `DhakaCampusWorld` or instantiate separate levels in `LevelManager`.

---

### DECISION-003: Monolith Decomposition & Modular Package Structure
- **Context & Conflict:** Existing classes were growing dangerously large (`DhakaCampusWorld.java` ~2,300 lines; `WhereWindsMeetHUD.java` ~2,700 lines; `StudentMesh.java` ~1,200 lines).
- **Chosen Decision:**
  1. Freeze monolithic classes from further expansion.
  2. Incrementally extract modular builders and systems into dedicated sub-packages:
     - `bd.spark36.world.landmarks.*` (individual builders for Curzon Hall, Pukur, TSC, Raju, etc.)
     - `bd.spark36.world.vegetation.*` (tree and plant procedural generation)
     - `bd.spark36.combat.*` (stances, weapons, hitboxes, poise)
     - `bd.spark36.missions.*` (data-driven mission state machine)
     - `bd.spark36.dialogue.*` (Unicode dialogue trees and subtitle renderer)
     - `bd.spark36.render.*` (PBR shaders, atmosphere, height fog)
  3. Maintain backward-compatible wrappers so the game never breaks compilation or launch during refactoring.
- **Alternatives Considered:**
  - *Single massive rewrite:* Rejected — violates hard rule #1 and creates high regression risk.
- **How to Reverse:** Revert extracted delegate calls back to monolithic internal routines.

---

## 2. PROFESSIONAL ARCHITECTURE GAPS FILLED (FROM MASTER BRIEF §12)

### DECISION-004: Save System & State Persistence
- **Decision:** Implement JSON-based profile serialization in `bd.spark36.app.SaveManager`. Store game settings (audio sliders, graphics tiers, camera sensitivity, keybindings) in `spark36_settings.json`, and story progress (active level, completed missions, unlocked journal entries, trust metrics) in `spark36_save.json`.
- **Why:** Essential for user-friendliness and game progression across multiple sessions.
- **How to Reverse:** SaveManager falls back to default in-memory configuration if JSON files are absent or corrupted.

### DECISION-005: Bilingual Localisation & Font Pipeline
- **Decision:** Integrate Google Fonts **Noto Sans Bengali** (SIL Open Font License 1.1) alongside Liberation Sans. Use libGDX's `FreeTypeFontGenerator` to generate font atlases supporting complete Bengali Unicode ranges, conjuncts, and punctuation.
- **Why:** The game represents Bangladesh's history; authentic Bengali typography is mandatory.
- **How to Reverse:** Toggle `language = "EN"` to use Latin Liberation Sans.

### DECISION-006: Asset Missing Fallbacks & Error Tolerance
- **Decision:** Implement safe fallback patterns across all texture and audio loaders. If an external texture or audio clip fails to load, `TextureFactory` and `AudioManager` return a neutral procedural texture or a silent audio stub with a console warning instead of crashing the game loop.
- **Why:** Guarantees zero crashes during gameplay on incomplete or missing optional assets.
- **How to Reverse:** Enable strict validation mode via `-Dbd.spark36.strictAssets=true`.

### DECISION-007: Performance & Graphics Tiers
- **Decision:** Provide three graphics presets (Low, Medium, High) with configurable toggles:
  - *High:* 4096 shadow map, full post-processing (ACES tonemap, bloom, FXAA, chromatic aberration), max foliage.
  - *Medium (M4 Target):* 2048 shadow map, ACES tonemap, bloom, FXAA, optimized foliage. Rock-solid 60 FPS locked.
  - *Low:* 1024 shadow map, FXAA only, reduced foliage density.
- **Why:** Ensures cool thermals on fanless Apple Silicon MacBook Air M4 while offering scalability.
- **How to Reverse:** User can change preset at any time in the settings menu.

---

## 3. REFERENCE-DOCUMENT RECONCILIATION (2026-10-24)

Source of these entries: `docs/OPUS_WORK_ORDER_CONSOLIDATED_REFERENCE_GAPS.md` (work order, task T1), which compares the shipped game against the user-supplied `Project_July_2024_Consolidated_Prompt_Reference (1).md` ("the reference"). The IDs below continue this register's own numbering. They are **not** IDs from the reference's lost Batch 105 / v12.97 register (see §4). None of these entries is marked "approved by the user": the user has not yet confirmed them, and each one says so.

### DECISION-008: Reference-Document Operating Mode
- **Context & Conflict:** The reference was written for a planning-only workflow. It says "Planning / prompt preparation only" and forbids code, engine selection and Git changes without separate explicit permission (reference §2.2; Appendix A "MODE A — PLANNING ONLY (active by default)"). This repository is already a working libGDX 1.14.2 / Java 17 game with six playable levels and a JUnit suite.
- **Chosen Decision:** Implementation mode (the reference's "MODE B") is in force for this repository. The user has asked for the game to be finished, and the work order gives implementation authority. The reference is used as the **requirements and ethics specification**, not as a mode switch. These sections still bind: §4 (historical evidence), §5.4 (location workflow), §6 (art bible), §8.3 (action boundaries), §8.5 (required systems), §11.3 (accessibility), §11.4 (QA), §12 (asset/licence register) and §14 (screenshots/photo/gallery, which the reference itself labels "summary-derived").
- **Alternatives Considered:**
  - *Return to planning only and re-run engine selection:* Rejected. It would discard a working build and contradict the user's current request.
- **Status:** Working direction taken from the work order. Not separately confirmed by the user in the reference.
- **How to Reverse:** If the user restores planning-only mode, stop code changes and limit work to `docs/`.

### DECISION-009: Protagonist Name (Rafi kept; reference names Arif)
- **Context & Conflict:** Reference §7.2 names the protagonist **Arif**, a second-year Political Science student, with friends Rafi, Tanvir and Nusrat. The reference labels these "working character notes" and says the original register was not recovered. The shipped game uses **Rafi**, a second-year Physics student, with Amina, Tanvir and Mofiz Chacha (`docs/story/STORY_BIBLE.md`). "Arif" appears nowhere in code or docs.
- **Chosen Decision:** Keep **Rafi** as implemented. Arif is not adopted, because no approval evidence for it exists and all dialogue and HUD text already use Rafi.
- **Status:** **Pending user confirmation.** Open question for the user: "Keep Rafi, or rename the protagonist to Arif?" Default if no answer: keep Rafi.
- **How to Reverse:** Rename the protagonist in `docs/story/STORY_BIBLE.md` and in every string that names him. Photo-mode text in reference §14 says "orbit around Arif"; read it as "orbit around the protagonist".

### DECISION-010: Jahangirnagar University and Ganabhaban Levels Deferred (M8-ext)
- **Context & Conflict:** The reference's location plan has 7 chapters, including **Jahangirnagar University** (Chapter 2) and a **Ganabhaban** final location (reference §5.2, Appendix A §7). The game ships 6 levels set in and around Dhaka University, Shahbagh and the Central Shaheed Minar. JU appears only in code comments.
- **Chosen Decision:** Do not build JU or Ganabhaban levels now. Both go to backlog item **M8-ext**, which is blocked on research:
  - JU needs a reference pack under the §5.4 workflow (official campus map, photos from several angles, an uncertainty list) and verified sources for the 14–17 July events on that campus.
  - Ganabhaban needs independent research on the chronology of 5 August. Reference §4 forbids implying that Sheikh Hasina's departure happened at Ganabhaban unless evidence supports it. OHCHR (12 Feb 2025, para. 53) says only that at around 2 pm she "was flown out of Dhaka by Armed Forces helicopter and then out of the country", and that protesters stormed her official residence shortly after. It does not name a take-off point (see `docs/RESEARCH_SOURCES.md`, row S-L6-03).
  - Level 6 already covers the 5 August turning point without either location.
- **Status:** Pending user confirmation of scope (work order §4, question 2). Default: 6 levels.
- **How to Reverse:** Once the reference packs exist, add `Level7…` classes and register them in `LevelManager`.

### DECISION-011: Evidence Label Scheme (5 labels adopted)
- **Context & Conflict:** The repository used `VERIFIED / DRAMATIZED / COMPOSITE` (`docs/historical_research.md`, `docs/story/STORY_BIBLE.md`) and `ESTIMATED` (`docs/story/TIMELINE.md`). The reference (§4; Appendix A §3) requires five labels.
- **Chosen Decision:** The reference's 5-label scheme is now canonical for all research notes, story docs, `docs/RESEARCH_SOURCES.md` and `assets/data/historical_sources.json`:
  - **VERIFIED:** supported by reliable sources (OHCHR report, credible reporting, official records, or several corroborating sources).
  - **CORROBORATED BUT INCOMPLETE:** the broad event is supported, but exact details, timing or location are uncertain.
  - **DISPUTED / UNCONFIRMED:** accounts conflict, or the evidence does not settle the claim.
  - **FICTIONAL GAMEPLAY:** an invented character, combined scene, mission objective or dialogue. It must never be presented as a real quotation or a documented event.
  - **RECONSTRUCTION:** a visual or spatial recreation from photos, videos and maps. It is not a surveyed or officially measured model.
- **Mapping from the old labels:**

  | Old label | New label | Rule |
  |---|---|---|
  | `VERIFIED` | `VERIFIED`, **only if** a row in `RESEARCH_SOURCES.md` backs it. Otherwise it is downgraded to `CORROBORATED BUT INCOMPLETE` or `DISPUTED / UNCONFIRMED`. | The relabelling pass on 2026-10-24 downgraded several old "VERIFIED" items, e.g. "point-blank range" for Abu Sayed, "military aircraft/helicopter" wording, and the "Amar Shonar Bangla, Boishammo Mukto Bangla" chant. |
  | `DRAMATIZED` | `RECONSTRUCTION` when it recreates a documented place or condition (tear gas on a street, curfew checkpoint). `FICTIONAL GAMEPLAY` when it is an invented mission action. | Decide case by case. |
  | `COMPOSITE` | `FICTIONAL GAMEPLAY` | Rafi, Amina, Tanvir, Mofiz Chacha and all generic NPC dialogue. |
  | `ESTIMATED` | `CORROBORATED BUT INCOMPLETE` | Used for reconstructed timings. |
- **Status:** Required by the reference (source-backed). Applied in this pass.
- **How to Reverse:** Not intended to be reversed. Add new labels only by appending a decision.

### DECISION-012: Combat Kept but Re-framed Defensively
- **Context & Conflict:** Reference §8.3 / Appendix A §8 prefers dodge, block, evade and escort, says "do not build a shooter", and requires any combat to be restrained and to have a narrative purpose. The repo has a full bare-handed martial-arts suite: combos, kicks, parry and counter-strike, a "neutralize all hostiles" victory condition in Chapter 2, and a planned Arena / Lathi Khela mode. DECISION-001 forbids deleting working combat code.
- **Chosen Decision:**
  - Keep all combat code.
  - Re-frame presentation and objectives defensively: Chapter 2 is won by getting the injured to safety, knockouts are optional, and codex text states the defensive purpose and the FICTIONAL GAMEPLAY label.
  - Keep disqualification for striking unarmed NPCs.
  - Arena / Lathi Khela stays a separate menu mode, labelled fictional folk sport and not advertised on campaign victory screens.
  - Real, identifiable people are never combat targets, and real victims are never score rewards.
  - Implementation is work-order task T5, owned by another agent. This entry records only the decision.
- **Status:** Working direction from the work order. Not separately confirmed by the user.
- **How to Reverse:** Restore the old objective text and victory conditions from git history.

---

## 4. RECOVERY GAP LIST — `Batch 105` / `Project Brief v12.97` (NOT RECOVERED)

The reference identifies **Project Brief v12.97, Batch 105** as the original planning baseline, and states that the full text of both was **not recovered**. This register therefore does **not** contain the original Batch 1–105 history or the original decision IDs. DECISION-001 … DECISION-012 above are this repository's own numbering and must never be treated as, or renamed to, the lost register's IDs.

Copied verbatim from reference §15, "Recovery gap list":

1. Exact complete `Project Brief v12.97` source text.
2. Complete `Batch 1–105` history, with exact batch titles/changes.
3. Complete decision register, original IDs, exact user choices, status, and timestamps.
4. Original wording/approval for Decisions 01–04 and any later option questions.
5. Complete exact contents of decisions 05–107.
6. Full text of decisions 108–257, beyond the limited screenshot/photo-mode summary.
7. Final approved story bible and full historical chronology with evidence citations.
8. Final location-name resolution for “ঘরোয়া ভবন”, “PSC”, and “IAL Building”.
9. Exact confirmation of any technical decision changes after earlier Java/jMonkeyEngine/libGDX planning.

Rules carried from reference §15 (binding on every future agent):
- Do not rename an earlier draft "v12.97", and do not mark any unconfirmed proposal "approved" just to fill the gap.
- Do not invent Batch 105 content, decision IDs, wording or approval status.
- If the original Batch 105 / v12.97 file or the full decision transcript becomes available, merge it while keeping its original IDs, wording, status and order. Then add a decision here that cross-references the two numbering schemes.
- Item 7 is partly addressed by `docs/RESEARCH_SOURCES.md` (2026-10-24). That file is this project's own evidence register, **not** the lost original.

### DECISION-T6: Settings precedence, default graphics tier, and key rebinding deferred
- **Decision:** `bd.spark36.app.SaveManager` (DECISION-004) writes `spark36_settings.json` and `spark36_save.json` to `<user.home>/.spark36/` (override `-Dbd.spark36.saveDir`), never to the working directory. Precedence for graphics tier and tone map: launch switch (`-Dbd.spark36.graphicsTier`, `-Dbd.spark36.toneMapping`) > saved setting > `assets/visuals.properties`. The saved tier is now actually applied to the systems that read `GraphicsTier.fromSystemProperty()`; before T6 the pause menu displayed the file's HIGH while those systems always rendered MEDIUM. `visuals.properties` now says `graphicsTier=MEDIUM`, so the shipped look is unchanged and matches DECISION-007. Measured on the T0 Windows machine, spawn, 1920x1080, 20 s: HIGH applied 14.0 avg fps, MEDIUM 17.1 avg fps.
- **Key rebinding: deferred.** `GameInput` holds a clean action-to-key map, but many controls bypass it with hard-coded `Gdx.input.isKeyJustPressed(...)` checks (pause menu Q/X/M/F2, `PlayerController` V, `SparkGame` F-keys and O, dialogue and victory screens). Letting players rebind only the mapped actions would create silent conflicts with those keys. Prerequisite: move those call sites onto `GameInput` actions (they sit in the frozen `SparkGame`/`WhereWindsMeetHUD`), then add a capture UI with conflict detection and persist the map in `spark36_settings.json`.
- **Why:** Reference sections 8.5 and 11.3 (settings, save, accessibility); "do not lower visual quality by default" rules out a silent quality change in either direction.
- **How to Reverse:** Delete `<user.home>/.spark36/` to return to defaults; set `graphicsTier=HIGH` in `assets/visuals.properties` to make HIGH the default for players without a saved setting.
