# Location reference pack: Central Shaheed Minar (কেন্দ্রীয় শহীদ মিনার)

**Task:** T3 (reference §5.4). **Compiled:** 2026-10-24. **Status of the in-game model:** RECONSTRUCTION. Nothing in this pack changes geometry.

Labels: **VERIFIED** (seen in a cited source), **OBSCURED/UNCERTAIN**, **APPROXIMATE**.

A national memorial to the 1952 Language Movement martyrs. Under reference §4 it is memorialised, not a combat space.

---

## 1. Maps

| Source | Link | Loaded on 2026-10-24? | Notes |
|---|---|---|---|
| DU official map page | https://old.du.ac.bd/footer_widget/footer_widget_item/map | Partly: expired TLS certificate; map images 404. | See `LOCATION_CurzonHall.md` §1. |
| OSM node | https://www.openstreetmap.org/node/401826555 | Yes | `historic=monument`, `wikidata=Q2425991`, at 23.727211 N, 90.396637 E. No building outline for the plinth was found in this pass. |
| Wikipedia | https://en.wikipedia.org/wiki/Shaheed_Minar,_Dhaka | Yes | Coordinates 23.72722 N, 90.39667 E; near Dhaka Medical College Hospital. Designers Hamidur Rahman and Novera Ahmed; completed and inaugurated 21 February 1963; damaged 1971; rebuilt from 1972; marble lining 1983; premises enlarged mid-1980s. Main sources cited: Banglapedia ("Shaheed Minar", Ahmed Rafique, 2012). |

Real position: about 539 m **west** of Curzon Hall and roughly level with it north–south (OSM, APPROXIMATE).

## 2. Reference photos (links only)

| # | Commons page | Licence / author / date | Viewpoint |
|---|---|---|---|
| 1 | https://commons.wikimedia.org/wiki/File:Central_Shaheed_Minar_-_Dhaka_Medical_College_Campus_-_Dhaka_2015-05-31_2577.JPG | CC BY 3.0, Biswarup Ganguly, 2015-05-31 | Frontal, from the plaza: all five pylons, the red-tread step bank with white edging, a thin red circle visible behind the central pylon, trees behind. Best elevation reference. |
| 2 | https://commons.wikimedia.org/wiki/File:Central_Shaheed_Minar_-_Dhaka_Medical_College_Campus_01.jpg | CC BY-SA 4.0, Tarunsamanta, 2024-11-17 | Frontal from the plaza edge, closer and lower: shows the bent top of the central pylon, the open frame construction with thin bars, the patterned plaza (alpana-style floor painting). **Post-July-2024.** |
| 3 | https://commons.wikimedia.org/wiki/File:Central_Shaheed_Minar_Dhaka_(5).jpg | CC BY-SA 3.0, Salim Khandoker, Nov 2018 | Long axial view across the lower plaza and a second step bank, with yellow bollards; shows the two-level terrace layout and scale against trees. |
| 4 | https://commons.wikimedia.org/wiki/File:Central_Shaheed_Minar,_Dhaka,_Bangladesh.jpg | CC BY-SA 4.0, Tanvir Rahat, 2023-12-16 | Night view from the lower steps with visitors; floodlit white pylons. |
| 5 | https://commons.wikimedia.org/wiki/File:Central_Shaheed_Minar_in_Dhaka_01.jpg | CC BY-SA 4.0, MdsShakil, 2024-09-13 | Not previewed in this pass (viewpoint not checked); dated two months after August 2024. |

## 3. Feature checklist

| Feature | Real-world observation | Label |
|---|---|---|
| Pylon count | Five: a tall central pylon flanked by two shorter pylons on each side, stepping down outward. | VERIFIED (photos 1–3) |
| Pylon construction | Each pylon is an **open rectangular frame** (hollow, see-through), filled with thin vertical iron bars. The central one has three uprights (two bays) and its top section **bends forward** toward the viewer. | VERIFIED (photos 1, 2) |
| Symbolism | Mother with fallen sons (Wikipedia). | VERIFIED (secondary) |
| Height | 14 m (46 ft) per Wikipedia; the article flags the height/proportions passage as needing a citation. | CORROBORATED BUT INCOMPLETE |
| Red sun | Wikipedia mentions "the red sun shining behind". In photo 1 a thin red circle shows behind the central pylon; whether it is a ring or a disc was not established. | VERIFIED (exists); form UNCERTAIN |
| Colour | Pylons white (some photos show a pinkish cast from light); stair treads dark red/maroon with white nosing lines; plaza red-brown tiles, often painted with floor art. | VERIFIED |
| Materials | Marble lining added 1983 (Wikipedia). Visible surfaces: white-finished pylons, tiled plaza. | CORROBORATED BUT INCOMPLETE |
| Plinth / terraces | Raised dais reached by a wide bank of steps, with a second step bank down to a lower plaza (photo 3). Wikipedia: premises changed from triangular to square in the mid-1980s. | VERIFIED |
| Signage | Small black information boards on stands on the dais (photo 3). | VERIFIED |
| Gates / boundary | Not visible in the photos used; Wikipedia mentions a fence painted with poem lines in iron letters. | UNCERTAIN |
| Trees | Dense mature trees behind and to the sides. | VERIFIED |
| Paths | Open tiled plaza; yellow bollards along the dais edge (2018). | VERIFIED |
| Nearby structures | Dhaka Medical College Hospital nearby; DU Mathematics Department next to it (Wikipedia). | CORROBORATED BUT INCOMPLETE |
| Sightlines | Axial view from the plaza to the central pylon is the iconic view. | VERIFIED |

## 4. In-game representation

| Item | Where |
|---|---|
| Builder | `core/src/main/java/bd/spark36/world/landmarks/ShaheedMinarBuilder.java`, `buildShaheedMinar(...)`. Placed by `level/Level5MassMobilization.java` (`minarPos = (0, 0, -8)`); also imported by `Level6VictoryEpilogue.java`. |
| Geometry | White "marble" three-tier plinth 22 × 16 m base, top at about 1.25 m. Front stair of 4 steps. Central "mother" pylon: two 0.55 m posts 3.6 m apart, 12.5 m tall from y = 1.2, six horizontal cross bars, a forward-projecting top block (top about 14.6 m). Four flanking pylons are **solid slabs** (1.4 m and 1.2 m wide; 9.8 m and 7.2 m tall). Sun: three overlapping square boxes (5.2–5.6 m) forming an octagon-like red emissive plate behind the pylons at y = 7.8. Red altar slab and gold plaque in front. |
| Setting | Level 5 loads it **inside the Dhaka University campus world**, 8 m north of the pond, directly in front of Curzon Hall's façade (Curzon central block at z = −32). |

### Screenshot compared

`docs/screenshots/t3/l5_student_closeup.png` (Level 5, baseline `student_closeup`, from the south avenue about 70 m away). The pylons appear as thin white uprights in front of Curzon Hall, partly hidden by the player. There is **no dedicated Shaheed Minar viewpoint** in `SparkGame.java`; `pond_ghat` in Level 5 looks south, away from the monument. A frontal capture needs a new viewpoint (code change, out of scope).

### Concrete differences

1. **Flanking pylons are solid.** Real side pylons are hollow frames with bars; the game uses solid slabs, which changes the silhouette and loses the see-through effect.
2. **Central pylon.** Real: three uprights, two bays, top section bent forward as part of the frame. Game: two posts with ladder-like cross bars and a separate block on top.
3. **Sun shape.** Real: a circle (thin red ring or disc). Game: an octagon-like cross of three square boxes, emissive.
4. **Plinth colour.** Real step treads are dark red with white nosing and the plaza is red-brown tile; the game plinth is all white.
5. **Wrong setting.** Real monument stands about 540 m west of Curzon Hall near Dhaka Medical College, surrounded by trees and a two-level plaza. The game puts it on the Curzon Hall forecourt, so Curzon Hall appears behind it, which is never true in reality.
6. **Height.** Game central pylon top about 14.6 m vs Wikipedia's 14 m: close, but the 14 m is itself under-cited. APPROXIMATE.

## 5. Uncertainty list

- Pylon heights and widths other than the overall 14 m.
- Red sun: ring vs disc, diameter, material.
- Exact plaza layout and step counts (premises re-shaped in the mid-1980s).
- Appearance on 3 August 2024 (the one-point-demand rally): no dated Commons photo found in this pass.
- Fence and inscriptions.

## 6. Custom assets needed

- Five open-frame pylon meshes with bar infill; central pylon with bent top.
- Red ring/disc element.
- Two-level stepped dais and plaza with red treads, white nosing, tile material, and floor-art decals (original designs).
- Surrounding tree mass and a non-campus background (DMCH side) instead of Curzon Hall.
