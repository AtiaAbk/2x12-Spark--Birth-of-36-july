# Fix Pack 02 Report: Curzon Hall Doors & Windows Orientation & 3D Openings

## Step 1: Reproduce, Experiment & Root Cause Analysis

### 1. Executive Summary
During in-game inspection from the promenade spawn viewpoint toward Curzon Hall (~66 m) and up close, four major defects were identified on the Curzon Hall façade openings:
1. **Centre Window Sideways Rotation**: The triple pointed arches of the central portico upper storey appear rotated 90° clockwise, displaying 3 pointed arches stacked horizontally on their side instead of standing side-by-side with apexes pointing up.
2. **Wing Doors & Windows Turned ("D"-shaped)**: All 14 ground-floor teak doors and 14 upper-floor arched windows across the East and West wings look like half-ovals / "D" shapes with their curved arches pointing to the right instead of having their apexes at the top.
3. **Paper-Thin Painted Cards**: The openings are flat 0.01 m thin boxes (`mb.createBox(...)`) with no reveal depth, no frame thickness, and no interior cavity, creating an artificial cardboard appearance when viewed at an angle.
4. **Back-Face Peeking & Bleed**: Materials configured with `IntAttribute.createCullFace(0)` render both $\pm Z$ faces without culling, resulting in mirrored images visible from inside and z-fighting on thin geometry.

---

### 2. Empirical Test Card Experiment

To mathematically and visually isolate the cause (whether texture drawing, UV mapping, mesh dimension swap, or rotation transform), an **Orientation Test Card** was implemented in `TextureFactory.createDebugOrientationTestCard()` and applied via a debug toggle (`-Dbd.spark36.testCard=true` or runtime key `F4` / `O`).

#### Test Card Specifications:
- **Central Glyph**: Large asymmetric white letter **'F'** (has no vertical or horizontal symmetry).
- **Directional Arrow**: Bright yellow arrow pointing strictly toward Pixmap $y=0$ (UP / TOP).
- **Quadrant Corner Markers**:
  - Top-Left: **RED** ($x \in [0, 80], y \in [0, 80]$)
  - Top-Right: **GREEN** ($x \in [w-80, w], y \in [0, 80]$)
  - Bottom-Left: **BLUE** ($x \in [0, 80], y \in [h-80, h]$)
  - Bottom-Right: **YELLOW** ($x \in [w-80, w], y \in [h-80, h]$)
- **Text Labels**: High-contrast block lettering: `"TOP"`, `"BOT"`, `"LEFT"`, `"RIGHT"`.

#### Experimental Observations:
1. **Centre Window (Front Face - `01_testcard_centre_window_front.png`)**:
   - The letter **'F'** is lying horizontally on its back.
   - The yellow arrow is pointing **90° clockwise to the RIGHT**.
   - `"TOP"` label is positioned vertically on the **RIGHT** margin.
   - `"LEFT"` label is positioned horizontally on the **TOP** margin.
   - Corner markers: **RED** is at top-right; **GREEN** is at bottom-right; **BLUE** is at top-left; **YELLOW** is at bottom-left.
   - **Conclusion**: The front face ($+Z$) has its UV coordinates transposed ($U \leftrightarrow V$) and rotated **90° clockwise**.
2. **Centre Window (Back Face - `01_testcard_centre_window_back.png`)**:
   - The arrow points to the **LEFT**.
   - The letter **'F'** is mirrored horizontally.
   - Both front and back faces are rendered because `IntAttribute.createCullFace(0)` disables back-face culling.
3. **Wing Bays (Front Face - `01_testcard_wing_bay_front.png`)**:
   - On both the ground-floor door (2.20 m $\times$ 4.10 m) and upper window (2.20 m $\times$ 4.10 m), the test card exhibits the exact same 90° clockwise rotation.
   - Because the box is portrait ($W < H$) while the texture's $U/V$ axes are swapped, the texture is stretched horizontally and squashed vertically, converting the vertical pointed arch into a sideways half-oval / "D" shape.
4. **Grand Portal (Front Face - `01_testcard_grand_portal_front.png`)**:
   - Exactly identical 90° clockwise rotation observed on the central portal arch card.

---

### 3. Source Code Root Cause Analysis

An audit of libGDX 1.14.2's `BoxShapeBuilder.java` reveals the exact mathematical origin:

In `com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder.java`:
```java
// Line 73-76: Front (+Z) face creation
Vector3 nor = tmpV1.set(corner000).lerp(corner110, 0.5f).sub(tmpV2.set(corner001).lerp(corner111, 0.5f)).nor();
builder.rect(corner011, corner001, corner101, corner111, nor.scl(-1));
```
In `com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.java`:
```java
public void rect (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal) {
    rect(vertTmp1.set(corner00, normal, null, null).setUV(0f, 1f),
         vertTmp2.set(corner10, normal, null, null).setUV(1f, 1f),
         vertTmp3.set(corner11, normal, null, null).setUV(1f, 0f),
         vertTmp4.set(corner01, normal, null, null).setUV(0f, 0f));
}
```

#### The UV Transposition:
1. `BoxShapeBuilder` passes `corner011` (top-left in world space: $x_0, y_1, z_1$) as `corner00`, which receives $UV = (0, 1)$.
2. It passes `corner001` (bottom-left in world space: $x_0, y_0, z_1$) as `corner10`, which receives $UV = (1, 1)$.
3. It passes `corner101` (bottom-right in world space: $x_1, y_0, z_1$) as `corner11`, which receives $UV = (1, 0)$.
4. It passes `corner111` (top-right in world space: $x_1, y_1, z_1$) as `corner01`, which receives $UV = (0, 0)$.

As a result:
- Along the vertical axis ($Y$ going from $y_0$ to $y_1$): $U$ varies from $1.0$ down to $0.0$.
- Along the horizontal axis ($X$ going from $x_0$ to $x_1$): $V$ varies from $1.0$ down to $0.0$.
- **$U$ and $V$ are swapped and inverted on the front face of every box built with `mb.createBox()` in libGDX.**
- Furthermore, `IntAttribute.createCullFace(0)` forces OpenGL to render the mirrored $-Z$ face as well.

---

### 4. Step 1 Deliverables & Visual Verification Log

All required screenshots captured to `assets/screenshots/fix02/`:
- `00_before_spawn.png`: Spawn view looking north along the promenade toward Curzon Hall (~66 m away).
- `00_before_centre_window.png`: Head-on inspection of portico upper storey showing sideways pointed arches.
- `00_before_wing_bay.png`: Head-on inspection of wing bay showing "D"-shaped door and window.
- `00_before_side_angle.png`: 45-degree angle showing paper-thin card appearance without reveal depth.
- `01_testcard_centre_window_front.png`: Test card showing 90° clockwise rotation on portico triple window.
- `01_testcard_centre_window_back.png`: Test card showing mirrored $-Z$ face rendered from inside.
- `01_testcard_wing_bay_front.png`: Test card showing 90° clockwise rotation on wing doors and windows.
- `01_testcard_wing_bay_back.png`: Test card showing mirrored $-Z$ face on wing bay.
- `01_testcard_grand_portal_front.png`: Test card showing 90° clockwise rotation on central portal.
- `01_testcard_grand_portal_back.png`: Test card showing mirrored $-Z$ face on central portal.

#### Permanent Debug Tools Added:
- **`F4` / `O` key**: Toggles the Orientation Test Card on all façade openings at runtime.
- **`F3` key**: Toggles 3D Opening Outlines (cyan/yellow bounding boxes) and surface normal vectors (red vectors).
- **System properties**: `-Dbd.spark36.testCard=true` and `-Dbd.spark36.showNormals=true`.
