# Location reference pack: Anti-Terrorism Raju Memorial Sculpture (সন্ত্রাস বিরোধী রাজু স্মারক ভাস্কর্য)

**Task:** T3 (reference §5.4). **Compiled:** 2026-10-24. **Status of the in-game model:** RECONSTRUCTION (placeholder quality). Nothing in this pack changes geometry.

Labels: **VERIFIED** (seen in a cited source), **OBSCURED/UNCERTAIN**, **APPROXIMATE**.

This is a memorial to a real person (Moin Hossain Raju, killed on campus in 1992). Under reference §4 it is memorialised and must never become a combat prop or scored object. Level 2 currently stages a fight "near Raju Sculpture"; that is a T5 question, recorded here only as context.

---

## 1. Maps

| Source | Link | Loaded on 2026-10-24? | Notes |
|---|---|---|---|
| DU official map page | https://old.du.ac.bd/footer_widget/footer_widget_item/map | Partly: expired TLS certificate; map images 404. | See `LOCATION_CurzonHall.md` §1. |
| OSM node | https://www.openstreetmap.org/node/9150960977 | Yes | `historic=monument`, `tourism=artwork`, `wikidata=Q16346792`, at 23.732649 N, 90.395680 E. |
| OSM context | https://www.openstreetmap.org/way/655714604 (TSC main block), https://www.openstreetmap.org/way/296813434 (TSC Ground), https://www.openstreetmap.org/way/142214388 (Kazi Nazrul Islam Avenue) | Yes | Road and TSC context. |
| Wikipedia | https://en.wikipedia.org/wiki/Anti_Terrorism_Raju_Memorial_Sculpture | Yes | Coordinates 23.732528 N, 90.395667 E (uncited in the article). |

Real position: about 66 m north of the TSC main-block centroid and roughly on its east–west line (OSM, APPROXIMATE). About 864 m north-west of Curzon Hall.

## 2. Reference photos (links only)

| # | Commons page | Licence / author / date | Viewpoint |
|---|---|---|---|
| 1 | https://commons.wikimedia.org/wiki/File:Anti-Terrorism_Raju_Memorial_Sculpture_3.A.M.R.jpg | CC BY 2.5, A.M.R., 2006-08-07 | Frontal, slightly low view: five figures visible including a woman in a sari, two white inscription plaques on the dark tiled pedestal, hedge of red-leaved shrubs, trees and palms behind. |
| 2 | https://commons.wikimedia.org/wiki/File:Raju_memorial_sculpture_dhaka.jpg | CC BY-SA 3.0, Hopeoflight, 2013-09-02 | Three-quarter view from outside the railing: shows the vertical-bar steel fence, grass island, pedestal profile and a TSC concrete screen wall in the background left. |
| 3 | https://commons.wikimedia.org/wiki/File:Anti_Terrorism_Raju_Memorial_Sculpture_(2).jpg | CC BY-SA 4.0, Nurunnaby Chowdhury (Hasive), 2018-02-16 | Close-up of the figure group from the other side, with a TSC block (concrete frame, blue glazing) behind right. |
| 4 | https://commons.wikimedia.org/wiki/File:Raju_memorial_sculpture,_DU_-_Ashfaq.JPG | CC BY 3.0, Ashfaq Mahmud, 2008-10-23 | Low angle against sky: shows the pedestal as a dark, tapering, tiled mass with a rough top slab. |
| 5 | https://commons.wikimedia.org/wiki/File:Anti_Terrorism_Raju_Memorial_Sculpture_in_2022.01.jpg | CC BY-SA 4.0, "CAPTAIN RAJU", 2022-09-09 | Close-up of the figures' faces and linked arms; weathering and staining visible. |
| 6 | https://commons.wikimedia.org/wiki/File:গোধূলি_বেলায়_সন্ত্রাস_বিরোধী_রাজু_স্মারক_ভাস্কর্য_(Anti_Terrorism_Raju_Memorial_Sculpture).jpg | CC BY-SA 4.0, Syed Sajidul Islam, 2018-09-06 | Dusk view (not previewed in this pass; viewpoint not checked). |

No Commons photo of the sculpture dated July–August 2024 was found in this pass.

## 3. Feature checklist

| Feature | Real-world observation | Label |
|---|---|---|
| Sculptors / date | Shaymol (Shyamal) Chowdhury and Gopal Paul; inaugurated 17 September 1997 (Wikipedia, citing Prothom Alo; Prothom Alo English editorial https://en.prothomalo.com/opinion/editorial/s1rs4zc215). | VERIFIED (secondary sources) |
| Figure count | Eight figures (Wikipedia, citing Prothom Alo in Bengali). Not all can be seen from one side. | CORROBORATED BUT INCOMPLETE |
| Figure genders | At least one woman in a sari (photo 1). `ARCHITECTURE.md` says "7 men, 1 woman"; not confirmed. | Woman VERIFIED; exact split UNCERTAIN |
| Composition | Figures stand shoulder to shoulder in a tight cluster, arms linked, striding forward together; no central spire. | VERIFIED (photos 1–5) |
| Height | No published height found. Figures look larger than life relative to people in photo 1. | UNCERTAIN |
| Pedestal | Tall, dark, tapering polygonal/conical base clad in square tiles, with a rough light top slab under the figures; two white inscription plaques on the front. | VERIFIED (photos 1, 4) |
| Colour / material | Figures off-white to grey with heavy weathering stains; pedestal dark grey-brown tiles. Material not documented. | Colour VERIFIED; material UNCERTAIN |
| Signage | White marble-like plaques with Bengali text and a "বাংলাদেশ ছাত্র ইউনিয়ন" line (photo 1, 2006). | VERIFIED (2006); current state UNCERTAIN |
| Boundary | Vertical-bar steel railing around a grass island; low hedge of red-leaved shrubs. | VERIFIED (photo 2) |
| Gates | None seen. | OBSCURED |
| Trees | Large shade trees and palms around the island (photos 1, 2). | VERIFIED |
| Paths / roads | Island sits beside the road in front of TSC; street lights visible. Whether it is a true roundabout or a road-side island is not clear from these photos. | UNCERTAIN |
| Nearby structures | TSC concrete-screen blocks in the background (photos 2, 3); "Bird of Peace" artwork node 3006309126 about 45 m south (OSM). | VERIFIED (OSM, photos) |
| Sightlines | Visible from the road and the TSC frontage. | VERIFIED |

## 4. In-game representation

| Item | Where |
|---|---|
| Builder | `DhakaCampusWorld.java`, section "14. EAST HUB: RAJU MEMORIAL SCULPTURE (TSC ROUNDABOUT)" (about line 2157). |
| Coordinates | Centre (44, 0, 42). Round stone island r = 16 m, 0.35 m high. Two square pedestals (4.8 m and 3.4 m). A 0.85 × 4.8 m square **spire** at (44, 4.4, 42). Four box "figures" 0.55 × 1.9 × 0.45 m on a 1.1 m radius at 90° steps. |
| Missions | Level 1 objective m2 at (44, 0, 42); Level 2 "DEFEND RAJU ROUNDABOUT" waypoints. |

### Screenshot

There is **no baseline viewpoint** for the Raju sculpture in `SparkGame.java`. The only capture is incidental: in `docs/screenshots/t3/tsc.png` the grey block in the lower right corner is the top of the game's Raju spire. A proper comparison needs a new viewpoint (code change, out of scope for T3).

### Concrete differences

1. **Composition wrong.** Real: eight arm-linked, life-like figures in one tight group. Game: four separate boxes around a tall square spire. The spire does not exist in reality.
2. **Figure count and detail.** 4 vs 8, boxes vs sculpted people; no woman figure.
3. **Pedestal shape.** Real: dark, tiled, tapering polygonal base with inscription plaques. Game: two stacked light-coloured square blocks.
4. **Setting.** Real: fenced grass island with hedges and steel railings. Game: a 32 m wide stone disc with no fence or planting.
5. **Placement.** Real: about 66 m north of TSC, in front of it on the road side. Game: 24 m west and 22 m south of the game's TSC centre, and the whole pair is about 70 m from Curzon Hall instead of about 860 m.

## 5. Uncertainty list

- Total height, pedestal height and diameter: no source. The 4.8 m figure in `ARCHITECTURE.md` and `WORLD_SCALE_AND_COORDINATES.md` is unsourced.
- Gender split of the eight figures.
- Figure material.
- Exact road layout around the island in 2024.
- `docs/location_reference_matrix.md` previously named "Mahmuduzzaman Babu" as sculptor; no source supports that. Corrected there to Shaymol Chowdhury and Gopal Paul.

## 6. Custom assets needed

- Original sculpted figure group (eight figures, one woman in a sari), modelled from permitted photos as reference only, never photogrammetry from copyrighted images.
- Tapering tiled pedestal with plaque recesses; plaque text must be original or paraphrased, not copied inscriptions, unless the wording is confirmed and permitted.
- Grass island, steel vertical-bar railing, red-leaved hedge shrubs.
