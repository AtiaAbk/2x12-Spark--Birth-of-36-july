content = """# Walkthrough: Authentic 19yo Dhaka University Student, Grand Pukur Ghat & Campus Life

We have completed the comprehensive overhaul of **2x12: Spark — Birth of 36 July** to fulfill all user requirements:
1. **Authentic 19-Year-Old Dhaka University Student Protagonist**: Realistic young South Asian college student with textured "elomelo chul" (messy hair), official Dhaka University Student ID Card hanging on a royal-blue neck lanyard down his chest, casual summer button-down shirt with folded collar and placket, straight-cut indigo denim jeans with leather belt and steel buckle, slung campus daypack, athletic sneakers with white soles, and smartwatch.
2. **Pond Ghat Sitting with Dipped Feet & Swimming Fishes**: Grand reflection pond with stepped red-brick and stone ghats. Students can sit on the ghat steps with legs dangling into the water, kicking gently in the cool shimmer while colorful koi and rohu fishes swim gracefully underwater.
3. **Combat Combos & Tactical Ducking**: 3-hit protest defense combo (`[F]` / Left-Click) and low crouch (`[C]`) to duck under low structures and trellises.
4. **Mission Parameter Toggle**: Press **`[P]`** or **`[H]`** to hide or reveal the bottom-left Mission Parameters checklist box, allowing a completely unobstructed view of the campus and character.
5. **Vibrant July Summer Atmosphere**: Summer sun illumination, true sky sun rays, fluttery Krishnachura petals with gusty 3D aerodynamics, and accessible Curzon Hall campus grounds.

---

## 1. Visual Verification & Proof of Work

````carousel
![Hero Character Close-Up (Dhaka University Student ID, Messy Hair, Summer Attire)](/Users/atiaoishi/.gemini/antigravity/brain/55b2dc93-e576-4d64-90ef-96784350f1bf/spark36_hero_character_verified.png)
<!-- slide -->
![Student Sitting on Curzon Pond Ghat Steps with Feet Dipped in Water](/Users/atiaoishi/.gemini/antigravity/brain/55b2dc93-e576-4d64-90ef-96784350f1bf/spark36_pond_sitting_verified.png)
````

---

## 2. Character Model & Attire Breakdown ([StudentMesh.java](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/core/src/main/java/bd/spark36/character/StudentMesh.java))

- **Head & Expressive Face**:
  - Sculpted South Asian college youth facial structure: high cheekbones, masculine jawline, refined chin, and straight nose bridge with nostrils.
  - Expressive eyes with white sclera, dark pupils, and specular catchlight reflection.
  - Sleek youthful eyebrows with natural brow bone contours.
- **"Elomelo Chul" (Messy Youthful College Hair)**:
  - Volumetric dark hair with organic textured locks.
  - Tousled fringe falling naturally across the forehead in layered strands.
  - Tapered sideburns and nape contour.
- **Official Dhaka University Student ID Card & Lanyard**:
  - Royal blue lanyard ribbon draped around his neck across the shirt collar, converging at mid-chest.
  - Metallic swivel clip holding the laminated DU student smart ID badge with blue top header and student photo.
- **Summer Campus Wardrobe**:
  - Light sage half-sleeve casual summer button-down shirt with folded collar lapels, button placket, and chest pocket.
  - Classic straight-cut indigo denim jeans with leather belt and silver buckle.
  - Athletic street sneakers with white EVA midsoles, dark rubber outsoles, and laces.
  - Slung campus daypack/backpack over shoulders.
  - Sport smartwatch on left wrist and cord bracelet on right wrist.
  - Seamless anatomy: zero floating gaps or robotic ball joints.

---

## 3. Curzon Hall Pond Ghat & Aquatic Life ([DhakaCampusWorld.java](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/core/src/main/java/bd/spark36/world/DhakaCampusWorld.java))

- **Grand Curzon Pukur & Ghat Steps**:
  - Stepped tiers of Curzon red-brick and white sandstone curbs on the south bank (`Z: 17.0m - 18.5m`).
  - Flanking ornamental stone bollards at the landing.
- **Interactive Water Sitting**:
  - Walk up to the pond ghat steps and press **`[E]`** to sit down on the brick steps.
  - The student sits with legs dangling over the ledge and feet dipped directly into the cooling, shimmering water.
  - Press **`[WASD]`**, **`[SPACE]`**, or **`[E]`** anytime to stand back up.
- **Animated Swimming Fish**:
  - A school of colorful Bengali fishes (Golden Koi, Scarlet Carp, Shimmering Rohu) swimming underwater.
  - Continuous sinusoidal tail-wagging and smooth elliptical patrol swimming paths.

---

## 4. Controls & Gameplay Systems ([PlayerController.java](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/core/src/main/java/bd/spark36/character/PlayerController.java), [WhereWindsMeetHUD.java](file:///Users/atiaoishi/Downloads/2x12-Spark--Birth-of-36-july/core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java))

| Action | Key / Input | Details |
|---|---|---|
| **Move** | `W`, `A`, `S`, `D` / Arrow Keys | Responsive third-person traversal relative to camera |
| **Sprint** | `Shift` / `R` | Fast summer campus sprint with athletic camera push |
| **Jump** | `J` / `Space` / `V` / Right-Click | Jump & Double Jump for vertical agility |
| **Crouch / Duck** | `C` | Low athletic crouch; lowers collision height to duck under obstacles |
| **Combat Combo** | `F` / Left-Click | 3-Hit Protest Defense Combo (Jab -> Cross -> Sweeping Kick) |
| **Interact / Sit** | `E` | Inspect July Memorials / Sit on Curzon Pond Ghat |
| **Toggle Parameters** | `P` / `H` | **Hide or show the bottom-left Mission Parameters box** |
| **Tactical Map** | `M` | Fullscreen campus satellite overlay with player waypoint |
| **Pause / Menu** | `Esc` | Pause menu with mission objectives and audio controls |

---

## 5. Verification Commands

To reproduce the verified in-game screenshots at any time:

- **Hero Character Portrait**:
  ```bash
  java -Dbd.spark36.testHeroScreenshot=true -Dbd.spark36.cameraYaw=170 -Dbd.spark36.cameraDistance=2.0 -Dbd.spark36.cameraPitch=4.0 -Dbd.spark36.cameraTargetHeight=1.2 -Dbd.spark36.autoExit=true -cp "assets:$(cat full_classpath.txt):build/full_classes" bd.spark36.launcher.DesktopLauncher
  ```
- **Curzon Pond Ghat Sitting**:
  ```bash
  java -Dbd.spark36.testSitScreenshot=true -Dbd.spark36.cameraYaw=165 -Dbd.spark36.cameraDistance=2.5 -Dbd.spark36.cameraPitch=12.0 -Dbd.spark36.cameraTargetHeight=0.6 -Dbd.spark36.autoExit=true -cp "assets:$(cat full_classpath.txt):build/full_classes" bd.spark36.launcher.DesktopLauncher
  ```
"""

with open('/Users/atiaoishi/.gemini/antigravity/brain/55b2dc93-e576-4d64-90ef-96784350f1bf/walkthrough.md', 'w') as f:
    f.write(content)

print("walkthrough.md updated successfully!")
