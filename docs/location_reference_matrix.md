# LOCATION REFERENCE MATRIX
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Scope:** Geographical, Architectural & Environmental Mapping across Campaign Levels (Levels 1–6)  
**Historical Grounding:** Public Geographical Surveys, Photographs, News Footage, and Architectural Records  

---

## 0. LOCATION REFERENCE PACKS (T3, 2026-10-24)

Each signature location now has a reference pack following reference §5.4: map links with load status, at least four Commons photo links with licence and viewpoint, the feature checklist labelled VERIFIED / OBSCURED-UNCERTAIN / APPROXIMATE, the Java builder and coordinates, a comparison with an in-game screenshot, an uncertainty list and the assets still needed.

| Landmark | Pack | Real position (OSM) | In-game builder and position | Main gaps found |
|---|---|---|---|---|
| Curzon Hall | [LOCATION_CurzonHall.md](research/LOCATION_CurzonHall.md) | way 264272612, centroid 23.72738 N, 90.40193 E | `DhakaCampusWorld` §3, central block (0, 7, −32) | Invented white dome; façade faces south in game but north in reality; invented pond in front |
| Central Library | [LOCATION_CentralLibrary.md](research/LOCATION_CentralLibrary.md) | way 295394032, 23.73383 N, 90.39490 E | `DhakaCampusWorld` §8, (−68, 0, 22) | Red brick box instead of white concrete grid with glazing and screens; no windows; wrong place |
| Raju Memorial Sculpture | [LOCATION_RajuSculpture.md](research/LOCATION_RajuSculpture.md) | node 9150960977, 23.73265 N, 90.39568 E | `DhakaCampusWorld` §14, (44, 0, 42) | 4 boxes around an invented spire vs 8 linked figures on a tiled pedestal; no viewpoint |
| TSC | [LOCATION_TSC.md](research/LOCATION_TSC.md) | way 655714604 (main), way 447734507 (auditorium), 23.73205 N, 90.39578 E | `DhakaCampusWorld` §13, (68, 0, 20) | Gable roof on top of block vs separate barrel-vault auditorium; no lawn court |
| Central Shaheed Minar | [LOCATION_ShaheedMinar.md](research/LOCATION_ShaheedMinar.md) | node 401826555, 23.72721 N, 90.39664 E | `ShaheedMinarBuilder`, placed by Level 5 at (0, 0, −8) | Solid side pylons vs open frames; white plinth vs red steps; placed in front of Curzon Hall |
| Shahbagh intersection | [LOCATION_Shahbagh.md](research/LOCATION_Shahbagh.md) | Kazi Nazrul Islam Ave × Shahbag Rd, about 23.738 N, 90.396 E | **Not modelled**; Level 3 uses the campus south gate (0, 0, 76) as "Shahbagh Approach" | Whole junction missing (metro viaduct, footbridges, hospitals, traffic) |

**Scale note:** in reality these six places spread over roughly 1.4 km (Shahbagh to Curzon Hall) and all except Shaheed Minar lie north-west of Curzon Hall. The game packs them into a 190 × 140 m composite campus with Library to the west and TSC/Raju to the east. This is a deliberate COMPOSITE and must be labelled RECONSTRUCTION wherever the game implies real geography.

**Coordinates in the table below** were not sourced; the OSM positions above supersede them. In-game screenshots used for comparison are under `docs/screenshots/t3/`.

---

## 1. GEOGRAPHICAL MAPPING MATRIX

| Level | Real-World Location | GPS Coordinates | Historical Date Range | Key Architectural & Environmental Features | In-Game Implementation Details |
|---|---|---|---|---|---|
| **Level 1** | **Curzon Hall, Dhaka University** | `23.7277° N, 90.4014° E` | July 1 – 14, 2024 | Indo-Saracenic red-brick architecture, ornate pointed arches, horseshoe pavilions, tranquil pukur (pond) with lotus pads, white plaster central dome, cast-iron colonial lampposts, manicured lawns, botanical trees (Debdaru, Chhatim, Neem). | `Level1PeacefulCampus.java`, `DhakaCampusWorld.java`, `OpeningBuilder.java` (recessed arches), dynamic lotus pads, water shader, 5 historical archive memorial monuments. |
| **Level 2** | **Raju Memorial Sculpture & TSC Roundabout** | `23.7317° N, 90.3958° E` | July 15 – 16, 2024 | Raju Memorial anti-terrorism sculpture (sculptors Shaymol Chowdhury and Gopal Paul, per Wikipedia citing Prothom Alo; the earlier "Mahmuduzzaman Babu" attribution had no source), central circular island, asphalt roadway, concrete street curbs, perimeter boundary fences, student activity centre (TSC) pavilion, brick perimeter walls. | `Level2StudentConfrontation.java`, `BarricadeBuilder.java` (street barricades, sandbags), non-lethal martial arts self-defense against helmeted aggressors, medical triage station. |
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
