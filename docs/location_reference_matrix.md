# LOCATION REFERENCE MATRIX
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Scope:** Geographical, Architectural & Environmental Mapping across Campaign Levels (Levels 1–6)  
**Historical Grounding:** Public Geographical Surveys, Photographs, News Footage, and Architectural Records  

---

## 1. GEOGRAPHICAL MAPPING MATRIX

| Level | Real-World Location | GPS Coordinates | Historical Date Range | Key Architectural & Environmental Features | In-Game Implementation Details |
|---|---|---|---|---|---|
| **Level 1** | **Curzon Hall, Dhaka University** | `23.7277° N, 90.4014° E` | July 1 – 14, 2024 | Indo-Saracenic red-brick architecture, ornate pointed arches, horseshoe pavilions, tranquil pukur (pond) with lotus pads, white plaster central dome, cast-iron colonial lampposts, manicured lawns, botanical trees (Debdaru, Chhatim, Neem). | `Level1PeacefulCampus.java`, `DhakaCampusWorld.java`, `OpeningBuilder.java` (recessed arches), dynamic lotus pads, water shader, 5 historical archive memorial monuments. |
| **Level 2** | **Raju Memorial Sculpture & TSC Roundabout** | `23.7317° N, 90.3958° E` | July 15 – 16, 2024 | Raju Memorial anti-terrorism sculpture (sculptor Mahmuduzzaman Babu), central circular island, asphalt roadway, concrete street curbs, perimeter boundary fences, student activity centre (TSC) pavilion, brick perimeter walls. | `Level2StudentConfrontation.java`, `BarricadeBuilder.java` (street barricades, sandbags), non-lethal martial arts self-defense against helmeted aggressors, medical triage station. |
| **Level 3** | **Shahbagh Road Intersection & Rampura Thoroughfare** | `23.7383° N, 90.3955° E` | July 17 – 18, 2024 | Broad multi-lane asphalt boulevard, street barricades constructed from corrugated iron, overturned rickshaw frames and wooden benches, makeshift triage station with water bottles and eyewash dispensers. | `Level3PoliceEscalation.java`, volumetric CS tear gas overlay, periodic sound grenade shockwaves, humanitarian water delivery station, medical aid distribution. |
| **Level 4** | **Dhaka Urban Checkpoint Corridor (Shahbagh / Nilkhet)** | `23.7335° N, 90.3900° E` | July 19 – 23, 2024 | Heavy nocturnal atmosphere, streetlight blackout, military sandbag emplacements, coiled razor wire barricades, BTR-80 Armored Personnel Carrier (APC) with sweeping 35-meter halogen spotlight beam, narrow pedestrian alleyways. | `Level4CurfewBlackout.java`, `MilitaryVehicleBuilder.java` (procedural BTR-80 APC, volumetric searchlight cone), stealth route-finding, clandestine courier delivery of photo evidence. |
| **Level 5** | **Central Shaheed Minar (Plaza & Surrounds)** | `23.7272° N, 90.3976° E` | August 3 – 4, 2024 | Central Shaheed Minar monument designed by Hamidur Rahman, towering stepped black marble plinth, central angled mother-and-child steel/marble columns, monumental crimson solar disk, wide ceremonial stone plaza. | `Level5MassMobilization.java`, `ShaheedMinarBuilder.java` (procedural stepped plinth, altar pillars, crimson sun disk), mass assembly flag bearers, 1-Point Demand rally checkpoint. |
| **Level 6** | **Shahbagh to Parliament (Manik Mia Ave) / DU TSC Corridor** | `23.7533° N, 90.3789° E` | August 5, 2024 ("36 July") | Wide open boulevards, triumphantly breached security checkpoints, victorious crowds bearing red-green national flags, red headbands, flower offerings, national assembly vista, historical commemorative pavilion. | `Level6VictoryEpilogue.java`, triumphant daylight atmosphere, four historic celebration waypoints, memorial bell toll ceremony, epilogue tribute to martyrs and civil society. |

---

## 2. ARCHITECTURAL FIDELITY & REFERENCE VERIFICATION

### 2.1 Curzon Hall (Levels 1 & 2 baseline)
- **Architectural Style:** Indo-Saracenic Revival (blend of Mughal architecture and European classicism), inaugurated 1904.
- **Color Palette:** Burnt crimson terracotta brick (`#8A2B1A`), sandstone trim and cornices (`#D2B48C`), off-white lime plaster dome (`#EFECE1`), deep teak doors (`#4A2B13`).
- **Openings:** Recessed 0.22m into masonry walls, framed by 3D chamfered brick surrounds and limestone archivolts (`OpeningBuilder.java`), avoiding the flat card look of initial prototypes.
- **Pond & Environment:** 18m × 24m ornamental rectangular pond with four-sided stepped stone ghats and floating lotus pads (`Nymphaea nouchali`).

### 2.2 Central Shaheed Minar (Level 5)
- **Symbolism:** Commemorating martyrs of the 1952 Bengali Language Movement, serving as the historical epicenter for all democratic movements.
- **Structure:** Central bent vertical column representing the protecting mother, flanked by four descending columns representing her fallen sons.
- **Sun Disk:** Circular crimson plate positioned behind the central columns, symbolizing the rising sun of independence and the blood of martyrs.

### 2.3 Military Vehicle & Urban Checkpoints (Level 4)
- **Vehicle Type:** BTR-80 8×8 amphibious armored personnel carrier, standard inventory for urban internal security deployment in Dhaka during July 2024.
- **Lighting:** Directional halogen turret spotlight sweeping in a 40-degree sinusoidal arc, triggering detection when unshielded within the light cone.
- **Barricades:** Modular timber barricades, coiled wire, sandbag revetments (`BarricadeBuilder.java`), providing realistic tactical cover for stealth gameplay.
