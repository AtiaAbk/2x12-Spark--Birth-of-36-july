# Location reference pack: Teacher-Student Centre, TSC (টিএসসি)

**Task:** T3 (reference §5.4). **Compiled:** 2026-10-24. **Status of the in-game model:** RECONSTRUCTION. Nothing in this pack changes geometry.

Labels: **VERIFIED** (seen in a cited source), **OBSCURED/UNCERTAIN**, **APPROXIMATE**.

---

## 1. Maps

| Source | Link | Loaded on 2026-10-24? | Notes |
|---|---|---|---|
| DU official map page | https://old.du.ac.bd/footer_widget/footer_widget_item/map | Partly: expired TLS certificate; map images 404. | See `LOCATION_CurzonHall.md` §1. |
| OSM, TSC main block | https://www.openstreetmap.org/way/655714604 | Yes | `building=university`, `building:levels=5`, `height=15.0` (community tag, no source given). Centroid 23.732045 N, 90.395777 E. |
| OSM, TSC Auditorium | https://www.openstreetmap.org/way/447734507 | Yes | `amenity=conference_centre`. Centroid 23.731732 N, 90.395602 E. |
| OSM, TSC Ground (central lawn) | https://www.openstreetmap.org/way/296813434 | Yes | Courtyard lawn. |
| OSM, Greek Memorial | https://www.openstreetmap.org/way/932286147 | Yes | `historic=memorial`, `memorial=stele`, `height=4`, `wikidata=Q5601799`. |
| OSM, TSC Cafeteria Wing | https://www.openstreetmap.org/way/1560050747 | Yes | One storey. |
| Wikipedia | https://en.wikipedia.org/wiki/Teacher-Student_Centre,_University_of_Dhaka | Yes (redirect from "Teacher-Student_Centre") | Architect C. A. Doxiadis; completed 1961; "double roof (butterfly canopy) extending over the front of the main block"; Greek Mausoleum 1915. Article is flagged for missing citations. |

Real position: about 627 m west and 517 m north of Curzon Hall (OSM, APPROXIMATE), i.e. north-west, not east.

## 2. Reference photos (links only)

| # | Commons page | Licence / author / date | Viewpoint |
|---|---|---|---|
| 1 | https://commons.wikimedia.org/wiki/File:Auditorium_-_Teacher-Student_Center_-_University_of_Dhaka_-_Dhaka_2015-05-31_2428.JPG | CC BY 3.0, Biswarup Ganguly, 2015-05-31 | From the central lawn looking at the auditorium end wall and the long main block on the right. Clearest view of the vault and the block's cantilevered roof. |
| 2 | https://commons.wikimedia.org/wiki/File:TSC_Field_and_Auditorium_wide_angle_view.jpg | CC BY-SA 4.0, Shikhon Biswas, 2019-06-23 | Wide panorama of the courtyard lawn: covered walkways with pyramidal-roofed pavilions on the far side, auditorium vault at right, high-rises beyond. |
| 3 | https://commons.wikimedia.org/wiki/File:TSC_University_of_Dhaka_2021.jpg | CC BY-SA 4.0, Dead.rabbit, 2021-12-25 | Similar lawn view with crowd and plastic chairs; shows the auditorium vault's side profile and the main block. |
| 4 | https://commons.wikimedia.org/wiki/File:TSC,_Dhaka_University.jpg | CC BY-SA 4.0, Yahya, 2024-09-13 | Street side of the main block: four storeys of concrete frame, jali screens, blue spandrel tiles, many organisation signboards, murals on the ground-floor wall. **Post-July-2024.** |
| 5 | https://commons.wikimedia.org/wiki/File:Graffiti_painted_on_Dhaka_University's_TSC_building.jpg | CC BY-SA 4.0, Yahya, 2024-09-13 | Graffiti "in remembrance of the July Movement" on the TSC wall (not previewed; description from file metadata). |
| 6 | https://commons.wikimedia.org/wiki/File:TSC_,_Dhaka_University_1960.jpg | Public domain (per Commons), Roger Gwynn, c. 1960 | Black-and-white road view of an open roundabout area with an obelisk-like structure in the distance. Which exact spot this is was not established. |

## 3. Feature checklist

| Feature | Real-world observation | Label |
|---|---|---|
| Architect / date | C. A. Doxiadis; completed 1961 (Wikipedia infobox). `ARCHITECTURE.md` says 1961–1964. | Architect CORROBORATED BUT INCOMPLETE; dates UNCERTAIN |
| Layout | Buildings arranged around a large open lawn court with covered walkways (photos 1–3). | VERIFIED |
| Auditorium roof | A long **segmental barrel vault** (curved concrete shell with a thick edge rib) whose open end shows a red infill grid in a white concrete frame. Not a pyramid, not a butterfly. | VERIFIED (photos 1–3) |
| Main block | Long slab, four storeys above a ground floor on the lawn side (photo 1), with a deep cantilevered roof on exposed brackets. OSM: `building:levels=5`, `height=15.0` (unsourced). | Form VERIFIED; storey count APPROXIMATE |
| "Butterfly canopy" | Wikipedia describes a double roof / butterfly canopy over the front of the main block. Not identified in the photos used here. | UNCERTAIN |
| Columns | Slim concrete columns on the covered walkways and ground floor; regular concrete frame on the block. | VERIFIED |
| Arches | None, apart from the vault profile. | VERIFIED |
| Windows | Horizontal bands of glazing alternating with red (lawn side) or blue-tiled (street side) spandrels, plus white perforated concrete jali panels. | VERIFIED (photos 1, 4) |
| Doors | Ground-floor openings behind murals and signboards on the street side; not detailed. | OBSCURED |
| Colour | Off-white concrete frame; red spandrels and red auditorium infill; blue tiles on street side; grey weathered vault. | VERIFIED |
| Materials | Exposed concrete, brick or red-painted infill, concrete screen blocks, glass. | VERIFIED (visual); infill material UNCERTAIN |
| Signage | Many organisation signboards on the street façade, Bengali and English (photo 4). | VERIFIED (2024) |
| Murals / graffiti | Large painted murals on the ground-floor wall; July-movement graffiti (photos 4, 5). | VERIFIED (Sept 2024) |
| Gates / boundary | Not checked. | OBSCURED |
| Trees | Large trees and palms around the lawn. | VERIFIED |
| Paths | Open lawn with a paved edge; covered walkway along the court. | VERIFIED |
| Nearby structures | Raju sculpture about 66 m north; Greek Memorial (stele, 4 m per OSM tag) in the grounds; cafeteria wing. | VERIFIED (OSM) |
| Sightlines | From the lawn, the auditorium vault is the landmark silhouette. | VERIFIED |
| Greek memorial date | `ARCHITECTURE.md` says "18th-century"; Wikipedia says built 1915. | Conflict, UNCERTAIN |

## 4. In-game representation

| Item | Where |
|---|---|
| Builder | `DhakaCampusWorld.java`, section "13. EAST HUB: TEACHER-STUDENT CENTRE (TSC)" (about line 2061). |
| Coordinates | Centre (68, 0, 20). Concrete box 40 × 9.2 × 32 m with brick infill and two dark window bands at y = 3.2 and 6.8. "Auditorium" box 20 × 4.8 × 20 m on top at y = 11.2. Two 11.2 × 0.45 × 21.6 m slabs tilted ±11° at y = 13.9 form the roof. West terrace, canopy on two pillars, 13 vertical fins on the west face. |
| Missions | Level 1 objective m4 at (66, 0, 20); Level 6 "TSC Bell of Freedom" waypoint at (66, 0, 20). |

### Screenshot compared

`docs/screenshots/t3/tsc.png` (baseline `tsc`, raised camera from the south-west) vs Commons photos 1 and 4. A Level 2 capture with the same camera was identical apart from the HUD and was not committed.

### Concrete differences

1. **Roof form inverted.** The two tilted slabs meet as a ridge, so the game roof reads as a **gable**. The real auditorium is a curved barrel vault; the code comment calls it a "butterfly", which would be a V (low in the middle). Neither matches the vault.
2. **Auditorium placement.** The game stacks the auditorium on top of the main block. In reality it is a separate long vaulted hall beside the main block, facing the lawn.
3. **No courtyard.** The real TSC is defined by its open lawn court with covered walkways and small pavilions; the game has a single solid box.
4. **Façade.** Real block: four storeys of concrete frame, jali panels, red or blue spandrels, deep bracketed roof overhang. Game: two storeys of plain bands with a token fin screen.
5. **Street life missing.** Real 2024 street façade is covered in signboards, murals and July graffiti; none in game.
6. **Location.** Real TSC is about 800 m north-west of Curzon Hall; the game places it 68 m east of the Curzon axis, south of the hall.

## 5. Uncertainty list

- Real dimensions: none published found (OSM `height=15.0` is unsourced).
- Exact storey count and whether "5 levels" in OSM includes the ground floor.
- Where the Wikipedia "butterfly canopy" is.
- Completion year (1961 vs 1961–1964) and Greek memorial date.
- What the "Bell of Freedom" in Level 6 refers to: no real bell at TSC was found in sources; treat as FICTIONAL GAMEPLAY unless a source appears.

## 6. Custom assets needed

- Barrel-vault auditorium shell with edge rib and red grid infill end wall.
- Modular slab-block bay: concrete frame, jali panel, red/blue spandrel, bracketed cantilever roof.
- Covered walkway and small pyramidal-roofed pavilion modules around a lawn court.
- Signboard and mural decal set (original artwork; no copied organisation logos).
- Greek memorial stele (small, 4 m class per OSM tag, APPROXIMATE).
