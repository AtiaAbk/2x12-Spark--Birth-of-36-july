# Location reference pack: Dhaka University Central Library (ঢাকা বিশ্ববিদ্যালয় গ্রন্থাগার)

**Task:** T3 (reference §5.4). **Compiled:** 2026-10-24. **Status of the in-game model:** RECONSTRUCTION. Nothing in this pack changes geometry.

Labels: **VERIFIED** (seen in a cited source), **OBSCURED/UNCERTAIN**, **APPROXIMATE**.

---

## 1. Maps

| Source | Link | Loaded on 2026-10-24? | Notes |
|---|---|---|---|
| DU official map page | https://old.du.ac.bd/footer_widget/footer_widget_item/map | Partly: expired TLS certificate; HTML only loads with verification off; both embedded map images return HTTP 404. | No official map image available. See `LOCATION_CurzonHall.md` §1. |
| OSM, Central Library Building I | https://www.openstreetmap.org/way/295394032 | Yes | `amenity=library`, `building:levels=3`, centroid about 23.73383 N, 90.39490 E. |
| OSM, Central Library Building II | https://www.openstreetmap.org/way/1152628065 | Yes | `building=university`, `building:levels=3`, just south of Building I. |
| OSM, "Library Bhaban" and library food court | https://www.openstreetmap.org/way/295391114, https://www.openstreetmap.org/node/11665637350 | Yes | Neighbouring library-related buildings to the north. |

Real position relative to Curzon Hall (computed from OSM centroids, flat-earth approximation): about **716 m west and 715 m north**, roughly 1.0 km away. APPROXIMATE.

## 2. Reference photos (links only)

| # | Commons page | Licence / author / date | Viewpoint |
|---|---|---|---|
| 1 | https://commons.wikimedia.org/wiki/File:Dhaka_University_Central_Library.jpg | CC BY-SA 4.0, Salim Khandoker, 2 Sep 2020 (Exif) | Frontal view of the long main façade from the forecourt road, under trees. Shows the full three-storey grid and the central entrance. |
| 2 | https://commons.wikimedia.org/wiki/File:Central_Library,_Dhaka_University.jpg | CC BY-SA 4.0, Yahya, 2024-09-13 | Close frontal view of the entrance: timber-clad entrance block with collapsible steel gate, Bengali/English name board, white concrete frame, perforated concrete screen panels. **Post-July-2024.** |
| 3 | https://commons.wikimedia.org/wiki/File:Central_Library,_University_of_Dhaka.jpg | CC BY-SA 4.0, MD. Fazla Rabbe Sarder, 6 Aug 2020 (Exif) | 360° equirectangular panorama from the forecourt: building at centre, tree-lined approach, hedges. |
| 4 | https://commons.wikimedia.org/wiki/File:DU_central_Library.jpg | CC BY-SA 4.0, Wasiul Bahar, 2022-11-12 | The street gate: steel double gate between brick piers under a black "ঢাকা বিশ্ববিদ্যালয় গ্রন্থাগার" sign board with the DU crest; posters on the piers. |
| 5 | https://commons.wikimedia.org/wiki/File:DU_Library_Gate_Area_-_Nilkhet_Road_-_Dhaka_2015-05-31_1939.JPG | CC BY 3.0, Biswarup Ganguly, 2015-05-31 | Older gate sign seen from Nilkhet Road with rickshaws and a CNG; street-level context. |
| 6 | https://commons.wikimedia.org/wiki/File:Dhaka_University_Library_(5610671611).jpg | CC BY-SA 2.0, Naquib Hossain, 2011-03-13 | Interior (black and white): concrete ramp, steel-framed glazed walls. Useful for the modernist character, not the exterior. |

## 3. Feature checklist

| Feature | Real-world observation | Label |
|---|---|---|
| Architect / date | Wikipedia ("Muzharul Islam", Early life) says Muzharul Islam began his career with two buildings in the Shahbag area in 1955, the Dhaka University Library and the College of Arts and Crafts; its cited source is S. A. Gandhi, "Poetry in Architecture", Bangladeshi Architects in Australia. The code comment says "1953". | Attribution CORROBORATED BUT INCOMPLETE (secondary source); year 1953 vs 1955 UNCERTAIN |
| Frontage | Long, symmetrical rectangular block facing a tree-lined forecourt (photos 1, 3). Which compass direction it faces was not checked. | VERIFIED (form); orientation UNCERTAIN |
| Floors | Three storeys visible (photo 1); OSM `building:levels=3`. Ground floor is set back behind the upper floors. | VERIFIED |
| Roofline | Flat roof with a thin projecting slab edge; no parapet ornament. | VERIFIED (photo 1) |
| Columns | Exposed white concrete frame: slim vertical mullion-columns at regular bays on the upper floors. Not free-standing round pilotis. | VERIFIED (photos 1, 2) |
| Arches | None. | VERIFIED |
| Windows | Each bay holds steel-framed glazing above a band of perforated concrete screen (jali) panels. | VERIFIED (photo 2) |
| Doors | Central entrance in a timber-boarded block with a collapsible steel gate; a second steel grille gate to the side. | VERIFIED (photo 2, 2024) |
| Colour | Off-white / light grey concrete; warm brown timber at the entrance; some reddish tone visible behind glazing. **No exposed red brick on the façade.** | VERIFIED |
| Materials | Concrete frame, concrete screen blocks, glass, timber cladding at entrance. | VERIFIED (visual) |
| Signage | Name board in Bengali and English above the entrance with DU crest (photo 2); black sign over the street gate (photo 4). | VERIFIED |
| Gates | Steel double gate between brick piers at the street, with posters (photos 4, 5). | VERIFIED |
| Boundary walls | Low wall plus metal railings at the street (photo 5); full perimeter not seen. | UNCERTAIN |
| Trees | Dense mature canopy over the forecourt; palms near the gate. | VERIFIED |
| Paths | Asphalt/concrete forecourt and driveway; hedges along the front. | VERIFIED (photos 1, 3) |
| Nearby structures | Library Building II to the south, "Library Bhaban" to the north (OSM). Nilkhet Road at the gate (photo 5). | VERIFIED (OSM) |
| Sightlines | From the forecourt the whole façade is visible but partly screened by trees. | VERIFIED |
| Internal courtyard | `ARCHITECTURE.md` claims an open-air internal quadrangle. Not visible in any photo used; not checked on imagery. | UNCERTAIN |

## 4. In-game representation

| Item | Where |
|---|---|
| Builder | `DhakaCampusWorld.java`, section "8. WEST HUB: CENTRAL LIBRARY BUILDING" (about line 1841). Collider in `character/PlayerController.java` (about line 240). |
| Coordinates | Centre (−68, 0, 22). Ground slab 42 × 0.35 × 36 m. 20 round pilotis (r 0.65 m, 4.4 m tall) in a 5 × 5 grid. Upper volume 38 × 7.2 × 32 m (red brick) from about 4.9 to 12 m. Roof cornice at 12.25 m. Entrance steps and canopy on the **east** side (x = −46.5). Louvers on the east face. |
| Mission | Level 1 objective m3 "Central Library & Hakim Chattar" at (−58, 0, 36). |

### Screenshot compared

`docs/screenshots/t3/central_library.png` (baseline `central_library`, raised camera from the south-east) vs Commons photos 1 and 2.

### Concrete differences

1. **Material and colour wrong.** The game's upper floors are a solid red-brick box with white bands. The real façade is a white/grey concrete frame filled with glazing and perforated concrete screens; no red brick shows.
2. **Window grid missing.** The real building reads as a regular grid of glazed bays over all three floors. The game shows a blank wall with a few big white fins and no windows.
3. **Storeys.** Real: three readable storeys with a recessed ground floor. Game: one tall pilotis level plus one undivided upper mass, so it reads as two storeys.
4. **Pilotis.** The game uses round free-standing stilts with an open undercroft. Photos show a recessed ground floor with gated openings, not an open stilt hall. Degree of openness UNCERTAIN.
5. **Entrance.** Real entrance is a timber-boarded block with steel grille gates and a name board; the game has a plain concrete canopy and steps, no signage.
6. **Location.** In reality the library is about 1 km north-west of Curzon Hall; the game puts it 68 m west of the Curzon axis, south of the hall.
7. **Street gate.** The signed steel gate between brick piers on the road side is absent.

## 5. Uncertainty list

- Design year (1953 in code comment vs 1955 in Wikipedia's source).
- Which façade is the main front and its compass orientation.
- Whether there is an internal courtyard.
- Real dimensions: none published found. OSM polygon could give an approximate footprint later.
- Condition and signage in July 2024 (photo 2 is September 2024).

## 6. Custom assets needed

- Modular concrete-frame bay: glazing over perforated concrete screen panel, three storeys.
- Recessed ground floor with steel grille gates; timber-boarded entrance block.
- Bilingual name board with an original (not copied) crest placeholder.
- Street gate with brick piers, steel gate, overhead sign, poster decals (original artwork).
- Forecourt trees, palm, hedges.
