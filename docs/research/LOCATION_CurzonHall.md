# Location reference pack: Curzon Hall (কার্জন হল)

**Task:** T3 (reference §5.4). **Compiled:** 2026-10-24. **Status of the in-game model:** RECONSTRUCTION. Nothing in this pack changes geometry.

Labels used below: **VERIFIED** (seen in a cited source), **OBSCURED/UNCERTAIN** (not visible or sources disagree), **APPROXIMATE** (proportion read from photos or a community map, not a measurement).

---

## 1. Maps

| Source | Link | Loaded on 2026-10-24? | Notes |
|---|---|---|---|
| DU official map page | https://old.du.ac.bd/footer_widget/footer_widget_item/map | **Partly.** TLS certificate expired, so browsers/fetchers reject it. With verification disabled the HTML loads. It embeds a Google Maps iframe centred on 23.7315 N, 90.3925 E plus two map images. | Both embedded images return **HTTP 404**: `http://jobs.du.ac.bd/wp-content/uploads/2016/12/campus_map.jpg` and `http://jobs.du.ac.bd/wp-content/uploads/2017/08/DU_Map_2014-small.jpg`. The current site https://du.ac.bd/ loads but has no map link. **No official campus map image could be retrieved.** |
| OpenStreetMap, Curzon Hall building | https://www.openstreetmap.org/way/264272612 | Yes (HTTP 200; API v0.6 returned v16, edited 2026-02-14) | Tags: `building=university`, `building:levels=3`, `building:material=masonry`, `building:entrances=12`, `wikidata=Q5196138`. ODbL. |
| OpenStreetMap, neighbouring buildings | https://www.openstreetmap.org/way/264272613 (EEE dept., west), https://www.openstreetmap.org/way/297610977 (Chemistry, east), https://www.openstreetmap.org/way/67887007 (Shahidullah Hall pond) | Yes | Used for the "nearby structures" row. |

`assets/maps/campus_map.png` is an illustrated, AI-style map. It is **not** a survey and must not be treated as authoritative (it has blurred labels and invented sign text). It is useful only as the game's own layout key.

## 2. Reference photos (links only; nothing copied into the repo)

All checked through the Wikimedia Commons API on 2026-10-24. Viewpoints were described by looking at a 640 px preview.

| # | Commons page | Licence / author / date | Viewpoint |
|---|---|---|---|
| 1 | https://commons.wikimedia.org/wiki/File:Curzon_Hall_-_Northern_Facade_-_University_of_Dhaka_-_Dhaka_2015-05-31_1992.JPG | CC BY 3.0, Biswarup Ganguly, 2015-05-31 | Straight-on frontal view of the central block from the lawn, at eye level. Title says **northern façade**. Shows the central tower, two flanking turrets, both arcaded storeys, palms either side, rickshaw on the path. |
| 2 | https://commons.wikimedia.org/wiki/File:Curzon_Hall_-_DU.jpg | CC BY-SA 4.0, Hnmil, 2023-10-20 | Wide-angle frontal view from further back on the lawn; shows both wings receding, the bamboo rail fence, poster boards and signage. |
| 3 | https://commons.wikimedia.org/wiki/File:Curzon_Hall,_University_of_Dhaka_2023_wide.jpg | CC BY-SA 4.0, Aashaa, 2023 | Oblique view of a wing end and corner tower; shows the "Department of Fisheries" signboard, jali balustrades, the high-rise behind. |
| 4 | https://commons.wikimedia.org/wiki/File:Curzon_Hall,_Dhaka_University_01.jpg | CC BY-SA 4.0, Tarunsamanta, 2024-11-17 | Long lawn view along the Chemistry building's façade (part of the Curzon complex), with mature trees and path. |
| 5 | https://commons.wikimedia.org/wiki/File:Architecture_of_Curzon_Hall.jpg | CC BY-SA 4.0, Wasiul Bahar, 2022-12-16 | Close-up looking up at one bay: exposed brick piers, cusped bracketed opening, chhajja on brackets, white pierced parapet, lotus-bud finials. Good material reference. |
| 6 | https://commons.wikimedia.org/wiki/File:Curzon_Hall_-_Northern_Facade_-_University_of_Dhaka_-_Dhaka_2015-05-31_1985-1991.tif | CC BY 3.0, Biswarup Ganguly, 2015-05-31 | Stitched high-resolution frontal panorama (TIFF). Best source for counting bays. |

More: `Category:Curzon Hall` on Commons (https://commons.wikimedia.org/wiki/Category:Curzon_Hall) has ~25 "Architecture of Curzon Hall N" detail shots (Wasiul Bahar, 2022, CC BY-SA 4.0) and 2021 U.S. Embassy photos (public domain).

**2024-era:** the Tarunsamanta series is dated 2024-11-17, after the July–August 2024 events. No Commons photo of Curzon Hall dated July 2024 was found in this pass.

## 3. Feature checklist (reference §5.4 step 3)

| Feature | Real-world observation | Label |
|---|---|---|
| Frontage / orientation | Main entrance façade faces **north** onto an open lawn (photo 1 title; OSM way 264272612 has its single northernmost vertex at 23.7275945 N, 90.4018871 E, which is the middle of the long side, i.e. the projecting centre is on the north side). | VERIFIED (Commons photo 1 title + OSM geometry) |
| Footprint | OSM polygon bounding box is 93.9 m E–W × 47.6 m N–S (computed from the polygon's extreme coordinates). This is a community trace from imagery, not a survey, and the box includes projections and possibly courtyards. | APPROXIMATE (OSM-derived) |
| Floors | Two arcaded storeys on a low plinth, plus rooftop kiosks and a taller central tower (photos 1, 2). OSM says `building:levels=3`. | VERIFIED (2 storeys visible); tower storey count UNCERTAIN |
| Height | No published height found. | UNCERTAIN |
| Roofline | Flat roof hidden behind a white pierced (jali) parapet with lotus-bud finials; small domed chhatri kiosks on corner turrets; central square tower ends in an open arcaded stage with two small domed kiosks and pinnacles. **No large central dome.** | VERIFIED (photos 1, 2, 5) |
| Columns / piers | Engaged brick piers and round corner turrets; no free-standing classical columns on the front. | VERIFIED (photos 1, 5) |
| Arches | Horseshoe and cusped (multifoil) arches on both storeys; ground-floor bays partly use bracketed (corbelled) lintel openings. | VERIFIED (photos 1, 5) |
| Windows | Deep arcade verandahs: the "windows" seen from outside are open arches with white jali balustrades at sill level. Central first-floor bay is a large cusped window with white jali tracery. | VERIFIED (photos 1, 2) |
| Doors | Single deep central arched entrance at ground level with steps. Interior doors not documented. | VERIFIED (entrance); door material UNCERTAIN |
| Colour | Muted brick red / salmon-red overall (exposed brick), white or off-white jali, finials and some trim. Not a saturated pure red. | VERIFIED (photos 1–5); exact values APPROXIMATE |
| Materials | Exposed fired brick with stone/plaster detail. OSM: `building:material=masonry`. | VERIFIED (photo 5, OSM) |
| Signage | Department boards (e.g. "Department of Fisheries" on a wing, photo 3), poster and banner boards on the lawn (photo 2). | VERIFIED (2023 photos); 2024 state UNCERTAIN |
| Gates | Not visible in any of the photos used. | OBSCURED |
| Boundary walls | Not visible; a low bamboo rail fence on the lawn in photo 2. | OBSCURED/UNCERTAIN |
| Trees | Royal palms flank the central block; large shade trees on the lawn (photos 1, 2, 4). | VERIFIED |
| Paths | Paved path running parallel to the façade, lawn in front, low hedge bed in front of the entrance. | VERIFIED |
| Nearby structures | EEE department building to the west (OSM way 264272613) and Chemistry to the east (way 297610977). Shahidullah Hall pond is about 180 m south-south-east (way 67887007). **No pond in front of the north façade.** A modern high-rise is visible behind the hall (photos 3, 4). | VERIFIED (OSM) |
| Sightlines | From the north lawn the central tower is the focal point, framed by palms. | VERIFIED (photo 1) |

Dates (for context, not geometry): foundation stone 1904, completion 1908, per Wikipedia citing Banglapedia and Prothom Alo. Wikipedia gives **19 February 1904**; `ARCHITECTURE.md` said 14 February 1904. **Conflict, UNCERTAIN**; prefer Banglapedia if checked directly.

## 4. In-game representation

| Item | Where |
|---|---|
| Builder | `core/src/main/java/bd/spark36/world/DhakaCampusWorld.java`, section "3. Curzon Hall Indo-Saracenic Central Building Complex" (from about line 785), plus `world/landmarks/OpeningBuilder.java` for arches, doors and the grand portal, and "Curzon Hall Grand White Mughal Bulbous Dome" (about line 1101). |
| Coordinates (engine, 1 unit = 1 m, −Z = north) | Central block box 38 × 14 × 17 m at (0, 7, −32). Wings 28 × 11.5 × 15 m at (±32, 5.75, −31). Portico 15 × 15.6 × 3.8 m at (0, 7.8, −22.5). White dome centre (0, 18.55, −22.5). Overall width about 92–96 m. |
| Façade direction | Faces **south** (+Z) toward the player spawn, the "Curzon Hall Pukur" and the main gate. |
| Levels using it | Level 1 (and every level that loads `DhakaCampusWorld`, including 3, 5, 6). |

### Screenshots compared

- `docs/screenshots/t0_windows/spawn_level1.png` (baseline `spawn`, from the south avenue, about 89 m away) vs Commons photo 2 (frontal, from the lawn).
- `docs/screenshots/t3/curzon_front.png` (baseline `curzon_front`, at the portal) vs Commons photo 5 (bay close-up).
- Baseline `curzon_side` was also captured but only shows the gap between wings at point-blank range; not useful, not committed.

### Concrete differences

1. **Large white onion dome over the portico.** The game has a white bulbous "Mughal" dome as the main silhouette element. The real building has no large dome; the centre is a square brick tower ending in small domed kiosks. This is the biggest recognisability error.
2. **Orientation flipped.** Real main façade faces north onto a lawn; the game's faces south.
3. **Invented pond.** The game puts an 18 × 24 m "Curzon Hall Pukur" directly in front of the façade. OSM shows no pond there; the nearest is Shahidullah Hall pond, about 180 m away and not on the axis.
4. **Colour too saturated.** The game brick texture reads as bright pure red; the real brick is a duller salmon/brick red with white jali.
5. **Arcade depth missing.** Real storeys are open verandah arcades with white jali balustrades. The game uses flat boxes with recessed window and door openings, so the deep shadowed arcade rhythm is lost.
6. **Roofline.** Real roofline is a white pierced parapet with finials and kiosks; the game has a plain white cornice slab and a flat railing.
7. **Lawn and palms.** Real front is a lawn with royal palms; the game front is a herringbone brick plaza.
8. **Scale of central block.** Game central block is 14 m tall plus a 15.6 m portico; real height is unknown, so neither can be checked. Width (about 94 m) agrees with the OSM bounding box.

## 5. Uncertainty list

- Total and tower heights: no source.
- Whether the OSM polygon covers the whole Curzon Hall or only the original block (wings may be separate department buildings).
- Exact bay count per wing (count from photo 6 before modelling).
- Foundation date: 14 vs 19 February 1904.
- What the façade looked like in July 2024 (banners, graffiti, barriers): no dated photo found.
- Gate and boundary-wall details around the north lawn.

## 6. Custom assets needed

- Modular arcade bay (horseshoe and cusped arch variants) with jali balustrade, two storeys.
- Central square tower with open top stage and small domed kiosks; corner turrets with chhatris.
- Pierced parapet strip with lotus-bud finials; chhajja with brackets.
- Brick material tuned toward the muted salmon-red of the photos, plus an off-white jali material.
- Royal palm (Roystonea) model; lawn edge, low hedge.
- Department signboards in Bengali and English (original artwork, no copied logos).
