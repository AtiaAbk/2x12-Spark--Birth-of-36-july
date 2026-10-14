# GAME DESIGN REFERENCE & AAA BEST PRACTICES
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Scope:** Research Dossier B7 (Systems, UX, Locomotion, Camera, and Combat Architecture)  
**Industry References:** *Where Winds Meet*, *Black Myth: Wukong*, *Sekiro: Shadows Die Twice*, *The Last of Us Part I/II*, *Ghost of Tsushima*

---

## 1. THIRD-PERSON CAMERA & LOCOMOTION ARCHITECTURE

### Spherical Orbit & Spring Arm Mechanics
1. **Dynamic Pitch Elevation:** As the camera pitches downward (looking down at the player), the orbit arm raises slightly higher to maintain clear sightlines over shoulders; as it pitches upward, the arm lowers to frame majestic architecture (e.g., Curzon Hall domes).
2. **Predictive Lookahead:** Soft yaw offset in the direction of player movement velocity (10% velocity bias), allowing players to see where they are running without constant manual mouse correction.
3. **Collision Spring Arm:** Raycast sphere sweep from player head target to desired camera position. If geometry intercepts the sphere, camera distance clamps to `hitDistance - 0.2m` with instantaneous contraction and smooth damped recovery (0.35s).
4. **Cinematic Cutscene Rails:** Ability to interpolate between player-orbit camera and scripted spline curves for narrative moments with seamless blending.

---

## 2. INTERACTION & DIALOGUE PRESENTATION

### Contextual Interaction (E Key / Gamepad X)
1. **Screen-Space Radial Prompt:** Proximity detection (radius 2.5m, facing angle < 75°). UI renders an elegant gold-trimmed diamond indicator over interactive targets (NPCs, leaflets, memorial beacons).
2. **Hold vs. Tap:**
   - Instant tap: Pick up leaflet, talk to peer, inspect memorial.
   - Hold (0.8s): Critical transitions (e.g., leaving a zone, beginning a major assembly).

### Dialogue UI (*Where Winds Meet* Style)
- Non-modal, immersive presentation: The camera tracks conversationalists while maintaining world rendering and ambient audio.
- Bengali dialogue rendered in clean high-contrast white with dark charcoal shadow.
- English subtitles positioned inside a subtle smoked-glass lower banner.
- Branching choice pills: Left/Right or Up/Down directional choice selection with clear tone indicators (Inquire, Encourage, Reflect).

---

## 3. CROWD & NPC AI SYSTEMS

### Scalable Campus Crowd Architecture
1. **Bounded Density Budget:** Maximum 60 active student agents on screen simultaneously to protect MacBook Air M4 frame budget.
2. **Tiered Distance LOD:**
   - **LOD 0 (< 12m):** Full skeletal hierarchy, expressive head tracking towards player, dynamic cloth physics, individual pathfinding.
   - **LOD 1 (12m – 35m):** Simplified mesh, reduced animation tick rate (30 Hz instead of 60 Hz), flocking/waypoint locomotion.
   - **LOD 2 (> 35m):** Static or low-poly silhouette clusters with collective murmur audio.
3. **Atmospheric Behaviors:** Small conversational clusters (2–4 students), students studying on veranda benches, students reading placards, distributing leaflets, walking between departments.

---

## 4. ESCORT, EVASION & RESCUE MECHANICS (FOR HISTORICAL SCENES)

1. **Non-Combat Tension:** Creating high stakes without glorifying combat:
   - Line-of-sight stealth: Staying behind colonial pillars and veranda arches to avoid hostile squads.
   - Escorting panic-stricken peers: Companion AI matches player movement speed, avoids open courtyards, and takes cover when player crouches.
2. **Medical Triage & Aid:** First-aid bandage application mini-interaction (timed button hold), delivering drinking water bottles to dehydrated marchers.
3. **Evidence Preservation:** Finding and collecting discarded placards, journals, and photographic negatives for the historical archive.

---

## 5. COMBAT SYSTEM SPECIFICATION (ARENA & JUSTIFIED SCENES)

### Rhythmic Combat Loop
```
[Telegraph / Windup] ---> [Active Hitbox / Attack] ---> [Reaction / Impact]
        |                               |                         |
  Player reads                     Player parries            Hit-stop freeze
  visual cue                      or dodges i-frame         + Poise damage
```

### Precise Frame Timings (60 FPS Standard)
- **Perfect Parry Window:** Frames 2 to 11 (0.15s window). Deflects blow, plays metallic/wood clack, triggers 80ms hit-stop, and deals 35 Poise damage to attacker.
- **Dodge Invulnerability (i-frames):** Frames 1 to 18 (0.30s). Clean evasion allowing slip-through under sweeping attacks.
- **Attack Recovery Cancels:** Dodge cancel window opens immediately on frame 1 after active hitbox concludes.
- **Telegraph Warning:** Heavy attacks display a subtle directional glint (white for parryable, golden-red for unblockable sweep).

---

## 6. ACCESSIBILITY & USER CONFIGURATION

1. **Configurable Camera Motion:** Toggles for Camera Shake (0% to 100%), Head Bobbing (On/Off), and FOV slider (65° to 95°).
2. **Subtitle Readability:** Font size adjustment (Small, Medium, Large), high-contrast black backdrop box toggle.
3. **Input Remapping & Gamepad:** Full rebindable keyboard and controller mappings with real-time conflict detection.
4. **Assist Options:** Optional extended parry window (+50% timing grace) for players who want to experience the story without punishing difficulty.
