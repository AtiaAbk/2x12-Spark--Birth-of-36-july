# ASSET LICENSE AND ATTRIBUTION REGISTER
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Strict Policy:** 100% Free, Open-Source, Permissive, Commercial-Ready (CC0, SIL OFL 1.1, Apache 2.0, MIT, BSD). Zero pirated or proprietary copyrighted assets.

---

## 1. TYPOGRAPHY & FONTS

| Asset Name | Source / Provider | License | Intended Usage | Integration Method |
|---|---|---|---|---|
| **Noto Sans Bengali** | Google Fonts | **SIL Open Font License 1.1 (OFL-1.1)** | Bengali UI typography, dialogue, subtitles, memorial inscriptions, placards | TTF loaded via `FreeTypeFontGenerator` |
| **Liberation Sans** | Red Hat / Fedora Project | **SIL Open Font License 1.1 (OFL-1.1)** | English Latin UI, numeric readouts, debug overlay, HUD labels | TTF loaded via `FreeTypeFontGenerator` |
| **Cinzel Decorative / EB Garamond** | Google Fonts | **SIL Open Font License 1.1 (OFL-1.1)** | Stylized historical chapter headers, "Where Winds Meet" victory card | TTF loaded via `FreeTypeFontGenerator` |

---

## 2. PBR MATERIAL TEXTURES

| Material | Resolution | Source / Provider | License | Maps Included |
|---|---|---|---|---|
| **Historic Red Terracotta Brick** | 1024×1024 / 2048×2048 | AmbientCG / Poly Haven (`brick_wall_001`) | **Creative Commons Zero (CC0)** | Albedo, Normal (GL), Roughness, Ambient Occlusion |
| **Colonial Lime Plaster / Chunam** | 1024×1024 | AmbientCG (`plaster_rough_001`) | **Creative Commons Zero (CC0)** | Albedo, Normal (GL), Roughness |
| **Weathered Flagstone Paving** | 1024×1024 | AmbientCG (`cobblestone_large_01`) | **Creative Commons Zero (CC0)** | Albedo, Normal (GL), Roughness, AO |
| **Lush Tropical Grass / Lawn** | 1024×1024 | Poly Haven (`mossy_ground`) | **Creative Commons Zero (CC0)** | Albedo, Normal (GL), Roughness |
| **Dark Teak Architectural Wood** | 1024×1024 | AmbientCG (`wood_floor_002`) | **Creative Commons Zero (CC0)** | Albedo, Normal (GL), Roughness |
| **Curzon Pond Water Surface** | 512×512 | Procedural / CC0 Normal Waves | **Creative Commons Zero (CC0)** | Animated DuDv Flow Normal Map |
| **Wrought Iron Heritage Railing** | 512×512 | Procedural Alpha-Mask | **Creative Commons Zero (CC0)** | Albedo with Alpha cutout, Roughness |

---

## 3. AUDIO & SOUND EFFECTS (STAGE 9 TARGET)

| Sound Category | Description | Source / Collection | License |
|---|---|---|---|
| **Footsteps (Lawn Grass, Stone Pavers, Mud, Water)** | Physical walking & running steps | Sonniss GDC Community Archive / Freesound CC0 | **Creative Commons Zero (CC0)** |
| **Lathi / Bamboo Staff Swishes & Impact** | Aerodynamic whoosh, blunt wood clack, body thuds | Sonniss GDC Foley Archive / Freesound CC0 | **Creative Commons Zero (CC0)** |
| **Defensive Parry & Guard Deflection** | Resonant wood-on-wood and wood-on-metal clang | Freesound.org CC0 | **Creative Commons Zero (CC0)** |
| **Pukur Water Ambience & Splash** | Gentle pond ripples, wading slosh, lotus splashes | Freesound.org CC0 | **Creative Commons Zero (CC0)** |
| **Campus Birdsong & Wind Ambience** | Doel (Magpie Robin), Asian koel, soft breeze in banyan trees | Bangladesh Wildlife Sound Archive / CC0 Field Rec | **Creative Commons Zero (CC0)** |
| **Distant Mass Chants ("We Want Justice")** | Ambient reverbed crowd murmur (subtle background) | July 2024 Archival recordings (Fair Use / Creative Commons) | **Attribution / Cultural Archive** |

---

## 4. SOFTWARE LIBRARIES & ENGINE DEPENDENCIES

| Library | Version | Repository / Group | License | Description |
|---|---|---|---|---|
| **libGDX** | 1.14.2 | `com.badlogicgames.gdx` | **Apache License 2.0** | Core 3D engine, math, scene graph, input, sound |
| **LWJGL 3** | 3.3.3+ | `org.lwjgl` | **BSD-3-Clause** | Desktop cross-platform OpenGL, windowing, OpenAL |
| **gdx-freetype** | 1.14.2 | `com.badlogicgames.gdx` | **Apache License 2.0** | FreeType font rasterization for dynamic unicode rendering |
| **gdx-gltf** (Optional) | 2.2.1 | `net.mgsx.gltf` | **Apache License 2.0** | PBR shader pipeline, GLTF/GLB runtime mesh loading |

---

## 5. AUDIT AND COMPLIANCE RULES

1. **Clean Room Verification:** Every binary asset (texture image, sound WAV/OGG, font TTF) added to `assets/` must have an entry in this register with its upstream URL and license text.
2. **No Generative AI Watermarks or Artifacts:** Textures must be tileable seamless power-of-two (256, 512, 1024, 2048) without burned-in watermarks.
3. **No Commercial Royalties or NDAs:** All components are permissible for public distribution on GitHub, itch.io, or Steam.
