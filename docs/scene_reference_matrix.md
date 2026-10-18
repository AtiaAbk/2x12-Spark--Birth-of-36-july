# SCENE REFERENCE & ASSET REQUIREMENTS MATRIX
**Project:** Spark: Birth of 36 July (Repo: `2x12`)  
**Scope:** Six Playable Campaign Levels  
**Engine:** Java + LibGDX 1.14.2 (LWJGL 3.4.1)

---

## 1. COMPREHENSIVE SIX-LEVEL MATRIX

```
+====================================================================================================================+
| LEVEL 1: WHERE IT ALL BEGAN (CAMPUS EXPLORATION & ARCHIVE RECOVERY)                                                |
+--------------------------------------------------------------------------------------------------------------------+
| Date & Period:      | July 1 – 14, 2024 (Pre-crackdown peaceful student solidarity)                                |
| Historical Context: | Quota reform protests, student study circles, non-violent campus assembly                   |
| Location:           | Dhaka University Curzon Hall, Pukur (Pond), Central Library, Raju Sculpture, TSC          |
| Atmosphere/Light:   | Warm afternoon sunlight, dappled brick shadows, peaceful breeze, calm ambient birds & water  |
| Primary Objectives: | 1. Explore Curzon Hall verandas; 2. Visit 5 historical memorials; 3. Read archival records |
| Gameplay Systems:   | 3rd-person movement, sprint, jump, camera orbit, memorial inspection modal, water sitting    |
| Combat Status:      | Strictly Peaceful (LevelConfig.isCombatEnabled() == false). Campus Security greeting NPC.   |
| Key Assets Needed:  | Curzon Hall 3D facade with reveals, lotus pond, colonial trees, FreeType Bengali HUD, map    |
+====================================================================================================================+
| LEVEL 2: THE DAY THE STREETS CHANGED (STUDENT CONFRONTATION & MARTIAL ARTS DEFENSE)                                |
+--------------------------------------------------------------------------------------------------------------------+
| Date & Period:      | July 15, 2024 (Afternoon assault on peaceful students by helmeted counter-protesters)        |
| Historical Context: | Violent crackdown at DU VC Chottor, Raju Sculpture, and DMCH emergency ward                |
| Location:           | Dhaka University Raju Anti-Terrorism Sculpture & Arts Faculty Crossroads                   |
| Atmosphere/Light:   | Overcast stormy monsoon sky, harsh contrast, scattered banners, distant chanting & shouts    |
| Primary Objectives: | 1. Evade armed attackers; 2. Defend cornered students using non-lethal martial arts disarms; |
|                     | 3. Carry injured peers to emergency medical triage point at Arts Faculty gate              |
| Gameplay Systems:   | Melee martial arts combat (5-hit combo, sweep, kick, block, dodge, stamina), AI windup     |
| Combat Status:      | Active Self-Defense Combat against Helmeted Aggressors; hitting innocents = instant fail   |
| Key Assets Needed:  | Barricaded campus gates, damaged signs, iron rod/stick weapons, medical stretcher triage    |
+====================================================================================================================+
| LEVEL 3: A CITY ON EDGE (POLICE ESCALATION & TEAR GAS RESCUE)                                                      |
+--------------------------------------------------------------------------------------------------------------------+
| Date & Period:      | July 18, 2024 ("Complete Shutdown" declaration across Dhaka)                                 |
| Historical Context: | Nationwide student strike, police riot lines deploying CS gas, sound grenades, water cannon|
| Location:           | Rampura / Shahbagh Major Boulevard & Overpass approach                                     |
| Atmosphere/Light:   | Thick hazy white CS gas smoke, muted sunlight, flashing emergency beacon reflections        |
| Primary Objectives: | 1. Navigate low-visibility tear gas cloud without suffocating; 2. Distribute water/cloths; |
|                     | 3. Guide disoriented students past barricaded police cordon to safety                      |
| Gameplay Systems:   | Gas exposure timer / breath meter, smoke particle field, low-visibility post-fx, triage aid  |
| Combat Status:      | Tactical Evasion & Humanitarian Aid (Non-lethal diversion, rescue mechanics)                |
| Key Assets Needed:  | Smoke/fog particle volumes, riot shields, tire barricades, water bottles, megaphone audio   |
+====================================================================================================================+
| LEVEL 4: VOICES IN THE DARK (CURFEW, BLACKOUT & COVERT ROUTES)                                                     |
+--------------------------------------------------------------------------------------------------------------------+
| Date & Period:      | July 20 – 23, 2024 (Nationwide total communications blackout & military curfew)              |
| Historical Context: | Internet severed, shoot-on-sight curfew, armored military vehicle patrols, midnight sweeps  |
| Location:           | Dense Residential Alleyways & BTR-80 Armored Checkpoint Corridor (Shahbagh - Nilkhet)      |
| Atmosphere/Light:   | Deep night, solitary sodium streetlamps, sweeping military halogen searchlights, shadows    |
| Primary Objectives: | 1. Move covertly through back alleys; 2. Avoid searchlight cones and checkpoint patrols;    |
|                     | 3. Hand-deliver casualty documentation dossier to independent press contact                |
| Gameplay Systems:   | Stealth crouching, vision cone avoidance, searchlight evasion, dynamic shadow cover         |
| Combat Status:      | Pure Covert Stealth & Infiltration (Avoid detection; direct confrontation is fatal)         |
| Key Assets Needed:  | BTR-80 APC low-poly model/mockup, military checkpoint sandbags, razor wire, spotlight beams |
+====================================================================================================================+
| LEVEL 5: THE WALL OF PEOPLE (MASS MOBILIZATION & THE ONE-POINT DEMAND)                                             |
+--------------------------------------------------------------------------------------------------------------------+
| Date & Period:      | August 3 – 4, 2024 (Central Shaheed Minar historic assembly & Non-Cooperation Movement)     |
| Historical Context: | Massive sea of hundreds of thousands declaring the singular One-Point Demand for justice   |
| Location:           | Central Shaheed Minar Plaza & Surrounding Arterials                                         |
| Atmosphere/Light:   | Dynamic dusk into sunset, crimson-tinted skies, sea of red banners and green-red flags      |
| Primary Objectives: | 1. Push through dense crowd currents to reach the central dais; 2. Form human chains;      |
|                     | 3. Deliver vital medical supplies to frontline emergency volunteers                        |
| Gameplay Systems:   | High-density crowd flow navigation (instanced LOD crowd agents), collective push physics   |
| Combat Status:      | Mass Solidarity & Frontline Protection (Defensive shielding, crowd de-escalation)          |
| Key Assets Needed:  | Shaheed Minar monument geometry, high-density crowd impostors, fluttering national flags    |
+====================================================================================================================+
| LEVEL 6: 36 JULY (THE HISTORICAL TURNING POINT & EPILOGUE)                                                         |
+--------------------------------------------------------------------------------------------------------------------+
| Date & Period:      | August 5, 2024 ("36 July" — The Long March to Dhaka & Fall of the Autocratic Regime)         |
| Historical Context: | Military refuses to fire on citizens; autocrat flees; historic victory rally at TSC/Shahbagh|
| Location:           | Dhaka University TSC, Promenade to Central Curzon Hall & Monument Plaza                    |
| Atmosphere/Light:   | Radiant golden afternoon breaking through storm clouds, confetti, marigold flowers          |
| Primary Objectives: | 1. March with thousands through the liberated campus gates; 2. Place flowers at memorials; |
|                     | 3. Ring the bell of victory; 4. View the permanent historical epilogue and memorial credits|
| Gameplay Systems:   | Triumphant procession, interactive flower-offering ceremony, historical credits crawl       |
| Combat Status:      | Complete Peace & Celebration (Solemn commemoration + joyful public liberation)              |
| Key Assets Needed:  | Marigold flower garland models, open victory gates, celebratory banners, credits cinematic   |
+====================================================================================================================+
```

---

## 2. PERFORMANCE & THERMAL BUDGET ALLOCATION (M4 MACBOOK AIR)

To guarantee steady 60 FPS on Apple Silicon M4 without active fan cooling:
- **Draw Call Budget:** Max 250 draw calls per frame across all levels.
- **Poly Budget:** Max 120,000 active triangles in camera frustum.
- **Level Transitions:** Explicit disposal and garbage-collection of unused meshes and textures upon level unloads.
- **Crowd Scaling:** Max 16 fully articulated skeletal student meshes; distant crowds in Levels 5 & 6 use optimized billboard/impostor quad clusters with vertex animation.
- **Shadow Map:** 1024x1024 single-pass directional depth map rendered strictly within a 40m bounding sphere around the player.
