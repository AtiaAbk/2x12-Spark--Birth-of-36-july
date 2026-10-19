# Phase 9: Gameplay Interactions, Dialogue, Mission Objectives & Git Architecture Plan
**Project:** 2x12: Spark — Birth of 36 July  
**Author:** Senior Gameplay Systems Engineer  
**Date:** October 19, 2026  
**Initial Commit:** `5334d6d` (*Phase 8: Realistic student crowd AI, navigation graph, and interactive dialogue*)  
**Initial Git Status:** Clean working tree on branch `main`

---

## 1. Initial Repository & Architecture Audit

### 1.1 Existing Systems & Assets
- **Engine & Core:** Java 8 compatibility in `core/`, Java 17 runtime, LWJGL3 backend, LibGDX 1.14.2.
- **Rendering & Environment (Phases 1–6):** PBR/HDR forward lighting pipeline, realistic water simulation with Gerstner waves and buoyancy, seasonal Dhaka vegetation with wind shaders, custom terrain heightmaps, dynamic day/night atmosphere, cinematic post-processing (ACES/Reinhard tone mapping, bloom, color grade).
- **Character Animation & NPCs (Phases 7–8):** Procedural articulated kinematics (`StudentMesh`, `CharacterPose`), 21-node Dijkstra campus navigation waypoint graph (`CampusWaypointGraph`), 9-state behavior machine (`NpcBehaviorState`), level-aware crowd profiles (`CampusCrowd`).
- **Levels & Campaign:** 6 distinct historical campaign levels managed by `LevelManager` and implementing `CampaignLevel`.
- **User Interface:** `WhereWindsMeetHUD` providing top-left objective cards, compass, parameter checklist, and memorial inspection modals.
- **Dialogue Assets:** `DialogueRepository` with authentic Bengali and English lines for all levels.

### 1.2 Audited Deficiencies & Gaps in Current Gameplay Architecture
1. **Lack of Formal Interaction Interface:**
   - Interactions are handled ad-hoc via scattered `dst()` distance checks in individual level classes and `SparkGame.java`.
   - No uniform `Interactable` interface with position, radius, context prompt, facing orientation test, and priority.
2. **Missing Facing-Aware Target Selection:**
   - When multiple interactables (e.g. an NPC and a memorial or bench) are nearby, selection is either arbitrary or first-found rather than prioritizing what the player is actively looking at.
3. **Absence of Interaction Debounce / Cooldown:**
   - Repeated key presses or held keys can re-trigger interactions or dismiss modals immediately.
4. **Hardcoded Level 1 Bypass in HUD:**
   - In `WhereWindsMeetHUD.java` (line 583), `if (activeLevel.getLevelNumber() > 1)` bypassed `activeLevel.onInteract` completely for Level 1, preventing Level 1 crowd dialogue and pond sitting from firing through standard HUD flow.
5. **No Multi-Line Dialogue Sequences with Player Advancement:**
   - Current dialogue display only supports single lines with fixed duration timers.
   - Players cannot advance conversations with `[SPACE]`, `[E]`, or `[ENTER]`, and cannot review multi-step narrative exchanges.
   - No dialogue completion callbacks to trigger scripted scene events or objective progression.
6. **Hardcoded Objective State in Level Classes:**
   - Objectives are tracked via loose boolean arrays (`objectivesDone[]`) without formal lifecycle states (`INACTIVE`, `ACTIVE`, `COMPLETED`, `FAILED`), objective types (`REACH_LOCATION`, `INTERACT`, `COMPLETE_DIALOGUE`, etc.), or decoupled progression tracking.
7. **Input Locking During Conversation:**
   - Movement and combat are not cleanly blocked when interacting or participating in dialogue sequences.

---

## 2. Implementation Plan & Logical Commit Sequence

| Step | Action & Scope | Expected Commit |
| :--- | :--- | :--- |
| **Step 1** | Comprehensive repository audit, architecture gap analysis, and implementation plan. | `docs: record phase 9 interaction and mission system audit and plan` |
| **Step 2** | `Interactable` interface and `InteractionSystem` with facing-weighted target selection, range checks, and debounce. | `feat: implement gameplay interaction target selection and interface` |
| **Step 3** | `DialogueSequence`, `DialogueManager`, multi-line playback, input advance (`[SPACE]`), and completion callbacks. | `feat: add multi-line dialogue playback and state management` |
| **Step 4** | `MissionObjective`, `ObjectiveType`, `ObjectiveState`, and `MissionTracker` state machine with clean reset. | `feat: implement mission objective tracking and state machine` |
| **Step 5** | NPC and Level integration: wire `StudentNPC`, `CampusCrowd`, and `CampaignLevel` (Levels 1–6) to interaction and mission tracker. | `feat: connect NPC interactions to mission events and level progression` |
| **Step 6** | UI integration: render interaction prompts, multi-line dialogue cards with speaker tags, and objective feedback in `WhereWindsMeetHUD`. | `feat: integrate interaction prompts and multi-line dialogue into HUD` |
| **Step 7** | Automated test suite (`InteractionMissionTest.java`) covering all 10 required verification criteria. | `test: add comprehensive gameplay interaction and mission objective test suite` |
| **Step 8** | Final documentation artifact, visual verification screenshots, and phase completion report. | `docs: complete phase 9 gameplay interaction report and documentation` |

---

## 3. Git Discipline & Safety Constraints
- Every logical step must compile cleanly (`./gradlew compileJava`) and be verified prior to commit.
- Only files belonging to each step will be staged (`git add <files>`).
- Conventional commit messages will be used.
- Commits are verified with `git log -1 --oneline` and recorded in the audit log.
- No destructive Git commands will be used.
