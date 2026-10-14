# WORLD SCALE AND COORDINATE TRANSLATION DOSSIER
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Engine:** Java 17 + libGDX 1.14.2 (LWJGL3 Desktop)  
**Authoritative Reference:** `assets/maps/campus_map.png`  
**Metric Standard:** 1.0 unit = 1.0 meter (1:1 real-world physical scale)

---

## 1. ENGINE COORDINATE CONVENTIONS

In libGDX 3D (Right-Handed Cartesian coordinate system with Y-up):

| Axis | World Direction | Metric Range | Description |
|---|---|---|---|
| **+X** | **East** | `0.0m` to `+95.0m` | Towards TSC, Raju Roundabout, Swadhinata Sangram |
| **-X** | **West** | `0.0m` to `-95.0m` | Towards Central Library, Aparajeyo Bangla, Madhur Canteen |
| **+Y** | **Up** | `0.0m` to `+30.0m` | Elevation (Ground = `0.0m`, Plinths = `+0.6m`, Spire = `+20.0m`) |
| **-Y** | **Down** | `0.0m` to `-2.0m` | Water depth / pond bed (`-1.4m` to `-1.8m`) |
| **+Z** | **South** | `0.0m` to `+85.0m` | Towards South Perimeter Wall, Spawn Gate (`+76.0m`), City Road |
| **-Z** | **North** | `0.0m` to `-55.0m` | Towards Curzon Hall Façade (`-18.0m`), Rear Fuller Road (`-48.0m`) |

### Rotational & Heading Standards
- **Yaw = 0° / 360°:** Facing North (`-Z`).
- **Yaw = 90°:** Facing East (`+X`).
- **Yaw = 180°:** Facing South (`+Z`).
- **Yaw = 270°:** Facing West (`-X`).
- **Compass Rose UI Mapping:** `Compass Angle = (90° - Bearing + CameraYaw)` ensuring the compass needle correctly points North regardless of player orientation.

---

## 2. WORLD DIMENSIONS & AABB BOUNDARIES

The game world is an enclosed historic campus district covering approximately **190 meters East-West × 140 meters North-South** (approx. 2.66 hectares / 6.57 acres).

```
                  NORTH (-Z)
       [Fuller Road Corridor Perimeter Wall (Z = -50m)]
-95m                                                     +95m
+-----------------------------------------------------------+
| [Madhur Canteen]     [Curzon Hall]      [Swadhinata      |
|  X: -56m, Z: -16m    Central: X:0, Z:-26m  Sangram]       |
|                       Wings: X:±38m       X:+58m, Z:-18m  |
|                                                           |
| [Central Library]         [Pukur]            [TSC]        |
|  X: -68m, Z: +28m    X:0, Z: 0m to +24m    X:+68m, Z:+28m |
|                                                           |
| [Aparajeyo Bangla]                     [Raju Memorial]    |
|  X: -34m, Z: +30m                      X:+44m, Z:+42m     |
|                                                           |
| [Hakim Chattar]                                           |
|  X: -56m, Z: +54m                                         |
|                                                           |
|                        [MAIN GATE]                        |
|                     X: 0m, Z: +76m                        |
+-----------------------------------------------------------+
-95m              [South City Avenue Wall]               +95m
                  SOUTH (+Z)
```

### Perimeter AABB Box
- `Min (X, Y, Z) = (-95.0, -2.0, -55.0)`
- `Max (X, Y, Z) = (+95.0, 35.0, +85.0)`
- **Boundary Colliders:** Solid perimeter brick-and-wrought-iron boundary walls prevent camera and player clipping out into empty space. Dense perimeter mahogany, rain tree, and banyan foliage mask the horizon.

---

## 3. LANDMARK COORDINATE TRANSLATION TABLE

| Landmark | Map Position | Engine Coordinates (X, Y, Z) | Footprint / Dimensions (W × H × D) | Elevation Plinth |
|---|---|---|---|---|
| **Main Gate (South Entrance)** | Bottom Center | `(0.0, 0.0, +76.0)` | `14.0m × 8.5m × 3.5m` | `0.0m` (Road Level) |
| **Player Spawn Point** | Inside Main Gate | `(0.0, 0.0, +71.0)` | Player Height `1.75m` | `0.0m` (Facing North) |
| **Curzon Hall — Central Hall** | Center Top | `(0.0, 0.6, -26.0)` | `38.0m × 18.5m × 18.0m` | `+0.60m` Stepped Plinth |
| **Curzon Hall — West Wing** | Center-Left Top | `(-38.0, 0.6, -26.0)` | `32.0m × 12.0m × 15.0m` | `+0.60m` Plinth |
| **Curzon Hall — East Wing** | Center-Right Top | `(+38.0, 0.6, -26.0)` | `32.0m × 12.0m × 15.0m` | `+0.60m` Plinth |
| **Curzon Hall Grand Portico** | Central Façade | `(0.0, 0.6, -17.0)` | `16.0m × 16.0m × 4.2m` | Stepped Access from `0.0m` |
| **Curzon Hall Pukur (Pond)** | Central Spine | `(0.0, -0.35, +12.0)` | `18.0m (W) × 24.0m (L)` | Water: `-0.35m`, Bed: `-1.5m` |
| **Arts Plaza (Aparajeyo Bangla)** | Mid-West | `(-34.0, 0.0, +30.0)` | `22.0m × 18.0m Plaza` | Sculpture: `+1.8m` Plinth |
| **Central Library** | Far-West | `(-68.0, 0.0, +28.0)` | `42.0m × 14.0m × 38.0m` | `+0.30m` Curb |
| **Madhur Canteen** | North-West | `(-56.0, 0.0, -16.0)` | `24.0m × 6.5m × 20.0m` | Octagonal Pavilion Plinth |
| **Hakim Chattar** | South-West | `(-56.0, 0.0, +54.0)` | `16.0m × 4.5m × 16.0m` | Pavilion Platform |
| **Book Stalls Corridor** | West Promenade | `(-18.0, 0.0, +12.0)` | `12.0m × 3.2m × 3.5m` | Stall Counters `+0.9m` |
| **Shabash Bangladesh Memorial** | East Promenade | `(+18.0, 0.0, +12.0)` | `8.0m × 5.2m × 6.0m` | Tiered Plinth `+1.2m` |
| **Raju Memorial Roundabout** | Mid-East | `(+44.0, 0.0, +42.0)` | `Radius = 11.0m, H = 4.8m`| Tiered Traffic Island |
| **Teacher-Student Centre (TSC)** | Far-East | `(+68.0, 0.0, +28.0)` | `46.0m × 12.0m × 40.0m` | Modernist Pilotis Grid |
| **Swadhinata Sangram Garden** | North-East | `(+58.0, 0.0, -18.0)` | `32.0m × 18.0m × 28.0m` | Multi-figure sculpture court |
| **Boss Duel Arena (Helmet Goon)** | Central Plaza / Steps | `(0.0, 0.0, -11.0)` | `20.0m × 20.0m Circle` | Open Stone Flagstones |

---

## 4. PEDESTRIAN PATHWAYS & THOROUGHFARES

1. **Central Promenade:**
   - Width: `7.0 meters`.
   - Extent: Runs from Main Gate (`Z = +76.0m`) to Curzon Hall Entrance Steps (`Z = -17.0m`).
   - Split at Pond: At `Z = +26.0m`, the promenade splits into symmetrical East and West waterside esplanades (`X = ±13.0m`, width `4.0m`) flanking the Curzon Pukur and rejoins at `Z = -2.0m`.
2. **West Avenue:**
   - Width: `5.0 meters`.
   - Path: Branches at `Z = +30.0m` heading West (`X = -4.0m` to `X = -72.0m`) linking Arts Plaza, Central Library, and Hakim Chattar.
3. **East Avenue:**
   - Width: `5.0 meters`.
   - Path: Branches at `Z = +30.0m` heading East (`X = +4.0m` to `X = +72.0m`) leading to Raju Memorial Roundabout and TSC.
4. **North Perimeter Service Path (Fuller Road Corridor):**
   - Width: `4.0 meters`.
   - Extent: Runs behind Curzon Hall and the science wings from `X = -70m` to `X = +70m` at `Z = -44m`.

---

## 5. ELEVATION, WATER & TERRAIN SPECIFICATION

- **Macro Grade:** Generally flat alluvial plain typical of Dhaka (`Y = 0.00m`).
- **Surface Elevation Steps:**
  - Lawn Grass: `Y = 0.00m`
  - Stone / Paved Avenues: `Y = +0.06m` (to prevent Z-fighting with ground grass mesh)
  - Curbs: Height `0.15m`, width `0.18m`
  - Pukur Curb: `Y = +0.45m`
  - Pukur Water Surface: `Y = -0.35m` (dynamic sinusoidal wave displacement ±0.04m)
  - Pukur Masonry Bed: `Y = -1.50m` (translucent water allows sight of bottom silts and aquatic vegetation)
  - Curzon Hall Ground Plinth: `Y = +0.60m`
  - Curzon Hall 1st Storey Level: `Y = +5.40m`
  - Curzon Hall Parapet / Roof Terrace: `Y = +10.20m`
  - Curzon Main Dome Apex: `Y = +18.50m`
