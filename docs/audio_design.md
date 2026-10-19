# AUDIO ARCHITECTURE & SOUND DESIGN SPECIFICATION
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Package:** `bd.spark36.audio` (`SoundManager.java`)  
**Compliance Standard:** 100% Procedural Synthesis, Zero Unlicensed Audio Assets, Zero Copyright Liability  

---

## 1. DESIGN PHILOSOPHY: ZERO COPYRIGHT LIABILITY

Commercial and open-source game development often suffers from copyright takedowns, unverified third-party audio packs, and licensing ambiguity. 

For *Spark: Birth of 36 July*, 100% of sound effects are procedurally synthesized in Java using raw 16-bit PCM mathematical models at 44.1 kHz, converted to canonical RIFF/WAVE files, and cached in memory. This guarantees:
1. **Zero External Dependencies:** No external sound files or asset downloads required.
2. **Zero Copyright Liability:** All acoustic waveforms are generated from first-principles DSP equations (exponential envelopes, frequency modulation, white noise filtering, and harmonic resonance).
3. **Deterministic Performance:** Ultra-low memory footprint (< 2 MB for the entire soundbank), zero runtime allocation, and sub-millisecond playback response.

---

## 2. PROCEDURAL SOUNDBANK INVENTORY

| Sound Identifier | File Name | Synthesis Technique | Harmonic / Frequency Profile | In-Game Trigger |
|---|---|---|---|---|
| **UI Click** | `ui_click.wav` | Damped sine pulse with fast exponential decay | $f_0 = 1200\text{ Hz} \to 300\text{ Hz}$, duration $0.045\text{s}$ | Menu navigation, button hover/select, map toggle |
| **Attack Whoosh** | `whoosh.wav` | Low-pass filtered noise with frequency sweep | Bandpass noise $300\text{ Hz} \to 950\text{ Hz} \to 250\text{ Hz}$, duration $0.22\text{s}$ | Punch jab, roundhouse kick, tactical dodge initiation |
| **Punch Impact** | `punch.wav` | Low-frequency transient pop + resonant thump | $f_0 = 140\text{ Hz} \to 45\text{ Hz}$, duration $0.14\text{s}$, high initial kinetic punch | Unarmed martial arts strike connecting with aggressor |
| **Kick Impact** | `kick.wav` | Heavy sub-bass body blow with dual-stage decay | $f_0 = 90\text{ Hz} \to 32\text{ Hz}$, duration $0.24\text{s}$, resonant heavy impact | Heavy high kick or tripping leg sweep connecting |
| **Martial Block** | `block.wav` | High-frequency deflection clack with quick damping | $f_0 = 2400\text{ Hz} \to 600\text{ Hz}$, duration $0.09\text{s}$, sharp transient | Player holding guard or parrying incoming strikes |
| **Objective Chime** | `objective.wav` | Pentatonic major chord with rich exponential sustain | Fundamental 523.25 Hz (C5) + 659.25 Hz (E5) + 783.99 Hz (G5) + 1046.50 Hz (C6), duration $0.85\text{s}$ | Inspecting historical archive, completing mission waypoint |
| **Freedom Bell Toll**| `bell.wav` | Deep bronze carillon bell with inharmonic strike overtones | $f_0 = 220\text{ Hz}$, overtones at $1.5\times, 2.0\times, 2.76\times, 3.4\times$, long decay $3.2\text{s}$ | Level victory, campaign climax at Central Shaheed Minar |
| **Water Splash** | `splash.wav` | Bandpassed white noise with aquatic surface bubble flutter | Filtered noise $400\text{ Hz} - 1800\text{ Hz}$ modulated by 24 Hz sine bubble, duration $0.35\text{s}$ | Player sitting on Curzon pond ghat, stepping into shallow water |
| **Footstep** | `footstep.wav` | Dual-layer gravel/stone tap with subtle low-frequency heel | High-frequency stone friction $1400\text{ Hz}$ + low-frequency heel thump $75\text{ Hz}$, duration $0.08\text{s}$ | Walking (0.48s interval) and sprinting (0.32s interval) |
| **Tear Gas Hiss** | `gas_hiss.wav` | High-pressure gas jet turbulence with flutter modulation | Low-pass smoothed white noise with 12 Hz amplitude flutter, duration $1.2\text{s}$ | Tear gas canister deployment, shockwave detonations in Level 3 |

---

## 3. ENGINE INTEGRATION & AUDIO SAFETY

### 3.1 Headless & Safe Execution
`SoundManager` encapsulates all LibGDX OpenAL calls within safe exception boundaries:
- If audio hardware is unavailable (e.g., CI/CD headless test environment), `enabled` is cleanly set to `false`, preventing any application crashes.
- Master volume is dynamically clamped in $[0.0, 1.0]$.
- All sound handles are properly freed in `dispose()` upon application exit.

### 3.2 Dynamic Pitch Variation
To eliminate the machine-gun effect during repetitive actions (such as footsteps and combat exchanges), each playback call introduces a subtle pseudo-random pitch variation:
$$\text{pitch} \sim \mathcal{U}(0.92, 1.08)$$
This provides a lively, organic acoustic feel without requiring duplicate audio assets.
