# Spark: Birth of 36 July — Complete Campaign Level Walkthrough & Design Guide

The campaign spans six historically grounded chapters documenting the 2024 Mass Uprising in Bangladesh, from initial campus discussions to the historic victory on August 5, 2024 ("36 July").

---

## Campaign Chapter Overview

```
Chapter 1: "Where It All Began" (July 1–14, 2024)
       │
       ▼ [Automatic Transition on All 5 Archives Secured]
Chapter 2: "The Day the Streets Changed" (July 15, 2024)
       │
       ▼ [Confront Aggressors & Administer First Aid]
Chapter 3: "A City on Edge" (July 18, 2024)
       │
       ▼ [Survive CS Gas & Rescue Civilians]
Chapter 4: "Voices in the Dark" (July 20–23, 2024)
       │
       ▼ [Evade Curfew Patrols & Establish Covert Relay]
Chapter 5: "The Wall of People" (August 3–4, 2024)
       │
       ▼ [Mobilize Assembly & Deliver One-Point Demand]
Chapter 6: "36 July" (August 5, 2024)
       │
       ▼ [Historic Long March to Dhaka & Liberation Epilogue]
Victory Credits & Epilogue
```

---

## Checkpoints, Failure and Combat Framing (all chapters)

- **Mid-level checkpoints**: every time an objective completes, progress is saved (in memory and in libGDX Preferences, key `checkpoint.L<n>`). From the Pause Menu choose **`[C] RESTART CHECKPOINT`** to reload the chapter with completed objectives restored, or **`[R] RESTART LEVEL`** for a full restart. The checkpoint is cleared when the chapter is completed.
- **No chapter requires defeating people.** Chapters 3 to 6 are aid, evasion, courier and assembly objectives with combat disabled; Chapter 2 is won by sheltering the injured.
- **Disqualification**: striking an unarmed student or civilian ends the attempt (tested for Chapters 2 and 3 in `CivilianStrikeDisqualificationTest`).

---

## Chapter 1: "Where It All Began"
- **Historical Timeframe**: July 1 – 14, 2024
- **Location**: Dhaka University — Curzon Hall Heritage Precinct
- **Atmosphere & Visuals**: Summer natural daylight, warm sun (color temp 0.14), lush green mango trees, rain trees, bamboo groves, and Curzon Hall pond reflections.
- **Combat Status**: **Disabled** (Strictly peaceful student demonstration).
- **Core Mission**: Recover 5 Historical Archives and inspect the 1921 Boundary Stone:
  1. *Curzon Hall Central Arcade* (Position: `[3.6, 0.0, -16.8]`) — Historical founding and architectural archives.
  2. *Aparajeyo Bangla (Arts Plaza)* (Position: `[-36.0, 0.0, 26.0]`) — 1952 Language Movement & 1971 legacy.
  3. *TSC Raju Anti-Terrorism Sculpture* (Position: `[44.0, 0.0, 42.0]`) — Anti-repression student unity.
  4. *Central Library & Hakim Chattar* (Position: `[-58.0, 0.0, 36.0]`) — Student reform assembly archives.
  5. *Teacher-Student Centre (TSC)* (Position: `[68.0, 0.0, 20.0]`) — Solidarity declaration.
- **Interactive Features**:
  - Talk to student NPCs in the courtyard.
  - Sit on the ghat steps of Curzon pond (`[E] SIT / RELAX`).
  - View the 1921 Boundary Stone on the eastern perimeter.
- **Victory Condition**: Inspect all 5 archives. Automatically displays victory screen and unlocks Chapter 2.

---

## Chapter 2: "The Day the Streets Changed"
- **Historical Timeframe**: July 15, 2024
- **Location**: TSC Intersection & Raju Sculpture Perimeter
- **Atmosphere**: Afternoon tension, hazy atmosphere, barricades, scattered student flyers.
- **Combat Status**: **Enabled**, defensive self-protection only (**FICTIONAL GAMEPLAY**). Helmeted aggressors are a hazard to evade, block or break away from. Knocking one out is possible but optional and never required.
- **Objectives** (checklist shown in game):
  1. Stabilize Injured Student at Raju Sculpture.
  2. Escort Student to Medical Shelter (Evade Aggressors).
  3. Reach Injured Student on Arts Faculty Road.
  4. Escort Second Student to Medical Shelter.
  5. Secure the Medical Shelter (Arts Faculty gate).
- **Mechanics**:
  - Dodge slips (`Z` / `Alt`) and crouch (`C`) to evade swings; defensive guard and flash parry (`B` / `Right-Click`) to block and break away.
  - Bare-handed strikes (`Left-Click` / `F`, `Q` / `K`) remain available for self-protection.
  - Striking an unarmed student causes instant disqualification.
- **Victory Condition**: Both injured students reach the medical shelter and the shelter is secured. Completable with zero knockouts (`Level2NonLethalCompletionTest`).
- **Arena Mode note**: Arena reuses this map as a separate fictional folk-sport sparring mode; there, subduing all three sparring aggressors also ends the round. This does not apply to the campaign.

---

## Chapter 3: "A City on Edge"
- **Historical Timeframe**: July 18, 2024
- **Location**: Doyel Chattar & High Court Intersection
- **Atmosphere**: Heavy CS tear gas haze, dynamic volumetric fog, evening dusk lighting, police barricades.
- **Objectives**:
  1. Navigate around tear gas hotspots using water sprayers and wet towels.
  2. Rescue trapped students near Doyel Chattar.
  3. Escort injured demonstrators to safe humanitarian corridors.
- **Victory Condition**: Evacuate all stranded demonstrators through the safe corridor.

---

## Chapter 4: "Voices in the Dark"
- **Historical Timeframe**: July 20 – 23, 2024
- **Location**: Dhaka Curfew Zone — Blackout Corridor
- **Atmosphere**: Midnight darkness, total nationwide internet blackout, security searchlights, drone sweeps.
- **Objectives**:
  1. Stealth traversal: Stay in shadows and use crouch (`C`) to avoid searchlight beams.
  2. Deliver offline communication flash drives across 4 neighborhood relay points.
  3. Evade mobile security checkpoints without getting detected.
- **Mechanics**:
  - Light meter detection and stealth crouch walk.
  - Distraction sound mechanics.
- **Victory Condition**: Successfully deliver all 4 offline archives and reach the safehouse.

---

## Chapter 5: "The Wall of People"
- **Historical Timeframe**: August 3 – 4, 2024
- **Location**: Central Shaheed Minar Assembly Ground
- **Atmosphere**: Massive collective gathering, red headbands, flags, overcast sky, united crowd chanting.
- **Objectives**:
  1. Coordinate with student leaders from different universities and schools.
  2. Form human chains protecting women and younger students.
  3. Deliver and proclaim the historic "One-Point Demand".
- **Victory Condition**: Unify the assembly perimeter and ratify the One-Point Demand.

---

## Chapter 6: "36 July"
- **Historical Timeframe**: August 5, 2024 ("The 36th of July")
- **Location**: Long March Route to Gono Bhaban & Shahbagh Square
- **Atmosphere**: Triumphant golden morning daylight, massive historic celebrations, green and red banners, flower petals.
- **Objectives**:
  1. Join the victorious Long March to Dhaka with thousands of citizens.
  2. Prevent vandalism of public heritage sites and maintain civic order.
  3. Raise the national flag at Shahbagh in honour of the martyrs of 36 July.
- **Victory Condition**: Complete the Long March and reach the victory podium to trigger the cinematic epilogue and credits.
