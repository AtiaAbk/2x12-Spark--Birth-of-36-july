# Spark: Birth of 36 July

A third-person 3D narrative action-adventure set during the July–August 2024 student-led uprising in Bangladesh. The player is a fictional Dhaka University student. The game is a dramatised work, not a historical record: real events are sourced, real people are never given invented words, and real martyrs are memorialised, never made interactive or scored.

Built with Java 17 and libGDX 1.14.2 (LWJGL3 desktop). Geometry, textures and audio are generated in code.

## Build and run

```bash
./gradlew.bat :core:build :lwjgl3:compileJava
```

```bash
./gradlew.bat :lwjgl3:run
```

On macOS or Linux use `./gradlew` instead of `./gradlew.bat`. Add `-Dbd.spark36.windowed=true` for a window instead of fullscreen. More switches are listed in `docs/BUILD_STATUS.md`.

## Controls (short)

| Key | Action |
|---|---|
| WASD / arrows | Move |
| Shift | Sprint |
| E | Interact |
| M | Campus map |
| Esc | Pause menu |
| F2 | Settings (in pause menu) |
| F10 | Photo mode |
| F11 | Screenshot gallery |
| F12 | Screenshot (saved to `Pictures/Spark36`) |
| Alt+Enter | Toggle fullscreen |

Full list: `CONTROLS.md`. Chapter guide: `LEVELS.md`.

## Where things are saved

- Settings and progress: `<user home>/.spark36/`
- Screenshots: `<user home>/Pictures/Spark36/`

## Historical evidence policy

Every historical claim the player can read is listed in `docs/RESEARCH_SOURCES.md` with its source and one of five labels: VERIFIED, CORROBORATED BUT INCOMPLETE, DISPUTED/UNCONFIRMED, FICTIONAL GAMEPLAY, RECONSTRUCTION. The in-game Credits & Sources screen shows the same list. Building dimensions and layouts are reconstructions; see `docs/research/LOCATION_*.md`.

## Project status

- What runs and how fast: `docs/BUILD_STATUS.md`
- Honest QA state: `docs/qa_checklist.md`
- Design decisions: `docs/DECISIONS.md`
- Handoff for the next developer: `docs/HANDOFF.md`
