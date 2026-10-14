# RESEARCH DOSSIER INDEX & SUMMARY
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Scope:** Phase B Historical, Architectural, World Scale, License, and Combat Design Research  
**Engine:** Java 17 + libGDX 1.14.2 (LWJGL3 Desktop)

---

## 1. RESEARCH DOCUMENTS DIRECTORY

| Document | Purpose | Key Sources | Status |
|---|---|---|---|
| [**`ARCHITECTURE.md`**](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/docs/research/ARCHITECTURE.md) | Authoritative architectural survey of Curzon Hall, Pukur, TSC, Raju Memorial, Aparajeyo Bangla, Madhur Canteen, Central Library, etc. | OpenStreetMap (`way 264272612`), Banglapedia, DU Official Archives, Liberation War Museum | **COMPLETE** |
| [**`WORLD_SCALE_AND_COORDINATES.md`**](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/docs/research/WORLD_SCALE_AND_COORDINATES.md) | Translation table matching `assets/maps/campus_map.png` to in-engine 3D Cartesian coordinates, road widths, plinth heights, and collision boundaries. | `campus_map.png`, `DhakaCampusWorld.java` | **COMPLETE** |
| [**`ASSET_LICENSE_REGISTER.md`**](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/docs/research/ASSET_LICENSE_REGISTER.md) | 100% Permissive Open Source & CC0 Asset Register (OFL fonts, CC0 textures, audio archives, libGDX Apache 2.0). | Google Fonts, AmbientCG, Poly Haven, Freesound CC0 | **COMPLETE** |
| [**`COMBAT_DESIGN_REFERENCE.md`**](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/docs/research/COMBAT_DESIGN_REFERENCE.md) | Combat mechanics specification: Lathi Khela staff martial arts, stance trees (*Goti*, *Dhal*, *Mukt*), parry/dodge i-frames, poise bars, hit-stop, and civilian safety rules. | *Where Winds Meet*, *Black Myth: Wukong*, *Sekiro*, Traditional Lathi Khela documentation | **COMPLETE** |

---

## 2. SYNTHESIS OF ESSENTIAL FACTS

1. **Physical Scale Standard:**
   - 1 unit = 1.0 meter.
   - Curzon Hall: **93.9m wide × 47.6m deep × 18.5m high** (Mughal-Gothic / Indo-Saracenic).
   - Curzon Pukur: **18m wide × 24m long** masonry-curbed reflecting pool.
   - Aparajeyo Bangla: **5.5m total height** (1.8m base + 3.7m statues).
   - Raju Memorial: **4.8m height** across circular traffic island plinth.
2. **Campus Map Alignment:**
   - South Main Gate at `Z = +76m`.
   - Curzon Hall portico steps at `Z = -17m`.
   - Pukur center at `Z = +12m`.
   - West Wing connects to Arts Plaza, Central Library, and Madhur Canteen.
   - East Wing connects to Raju Roundabout, TSC, and Swadhinata Sangram.
3. **Gameplay Ethics & Tone:**
   - Respectful, authentic portrayal of the July 2024 student movement.
   - Player defends peers using non-lethal martial arts and traditional bamboo staff (*lathi*).
   - Striking innocent civilians triggers instant disqualification.
