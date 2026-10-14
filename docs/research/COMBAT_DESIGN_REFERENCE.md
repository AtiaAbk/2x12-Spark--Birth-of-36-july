# COMBAT DESIGN & MARTIAL ARTS REFERENCE
**Project:** Spark: Birth of 36 July (Repository: `2x12`)  
**Design Inspiration:** *Where Winds Meet* (fluid wuxia redirection, stance flow), *Black Myth: Wukong* (rhythmic staff play, perfect dodges), *Sekiro* (deflection & posture meter)  
**Cultural Foundation:** Traditional Bengali **Lathi Khela** (লাঠি খেলা — indigenous Bengali staff martial art) + Defensive Student Resistance

---

## 1. DESIGN PHILOSOPHY & ETHICAL CONSTRAINTS

1. **Defensive & Principled:** The player is an unarmed or staff-wielding student defending innocent peers from hired aggressors. The player NEVER uses bladed weapons, guns, or lethal violence.
2. **Strict Non-Violence to Civilians:** Striking any unarmed student bystander on campus results in an immediate **Disqualification / Mission Failure** ("Disqualified: Violence against peaceful students violates the principles of the movement").
3. **Tactical Depth over Button Mashing:** Combat relies on recognizing enemy telegraphs, exploiting parry windows, managing stamina and posture, and switching stances to counter enemy archetypes.

---

## 2. WEAPON & STANCE SYSTEM

### Weapon Typology
- **Improvised Bamboo Staff (*Lathi* / বাঁশ):** 2.1m cylindrical cured bamboo pole reinforced with brass/iron ring ferrules (*tara*). Offers superior reach (2.4m), multi-target crowd control sweeps, vault attacks, and high posture damage.
- **Bare-Handed Martial Arts (*Khaali Haat* / খালি হাত):** Close-range boxing, knee strikes, sweeping low kicks, and joint locks. Fast frame startup (0.12s), high mobility, low stamina cost, but shorter range (1.3m).

### Three Combat Stances (Inspired by *Where Winds Meet* & *Wukong*)

```
               [STANCE SELECTION WHEEL / D-PAD]
                             |
         +-------------------+-------------------+
         |                                       |
    [THRUST STANCE]                       [SWEEP STANCE]
   (গতি - Goti Stance)                    (ঢাল - Dhal Stance)
• Long reach spear-pokes               • Low circular sweeps
• Fast linear interrupts               • Multi-target crowd control
• High single-target stagger           • Armor break against shields
         |                                       |
         +-------------------+-------------------+
                             |
                     [AGILE / EVASIVE]
                    (মুক্ত - Mukt Stance)
                  • Bare-handed redirection
                  • Extended i-frame dodges
                  • Flowing parry & counter
```

| Stance Name | Bengali Name | Primary Focus | Best Used Against | Signature Move |
|---|---|---|---|---|
| **Thrust Stance** | *Goti* (গতি) | Linear reach, precision pokes | Aggressive single enemies (Helmet Goon) | *Shongrami Teer* (Front Staff Thrust) |
| **Sweep Stance** | *Dhal* (ঢাল) | 360° sweeps, poise damage | Group encounters, flanking goons | *Bansh Chakkor* (Low 360° Dragon Sweep) |
| **Agile Stance** | *Mukt* (মুক্ত) | Flowing redirection, i-frame slips | Heavy overhead smash telegraphs | *Chhayabaji* (Ghost Slip & Counter) |

---

## 3. FRAME DATA & ATTACK CHAINS (AT 60 FPS)

| Move / Action | Startup (Frames) | Active Hitbox (Frames) | Recovery (Frames) | Total Duration | Cancel Windows | Range (Meters) | Poise Dmg | Health Dmg |
|---|---|---|---|---|---|---|---|---|
| **Light Jab (Fist)** | 6 (0.10s) | 4 (0.07s) | 8 (0.13s) | 18 (0.30s) | Dodge at F10 | 1.2m | 8 | 10 |
| **Heavy Cross (Fist)** | 10 (0.17s) | 5 (0.08s) | 12 (0.20s) | 27 (0.45s) | Dodge at F15 | 1.4m | 15 | 18 |
| **Front Push Kick** | 14 (0.23s) | 6 (0.10s) | 16 (0.27s) | 36 (0.60s) | Parry at F20 | 1.8m | 28 | 22 |
| **Staff Quick Thrust** | 8 (0.13s) | 5 (0.08s) | 10 (0.17s) | 23 (0.38s) | Dodge at F13 | 2.5m | 18 | 16 |
| **Staff Overhead Strike**| 18 (0.30s) | 6 (0.10s) | 20 (0.33s) | 44 (0.73s) | Cancel until F12 | 2.2m | 45 | 38 |
| **Staff 360° Sweep** | 16 (0.27s) | 12 (0.20s) | 22 (0.37s) | 50 (0.83s) | Non-cancelable | 2.4m (Arc) | 40 | 30 |
| **Defensive Parry** | 2 (0.03s) | 12 (0.20s) | 14 (0.23s) | 28 (0.47s) | Counter at F14 | Contact arc | 0 (Self) | 0 |
| **Tactical Dodge (Roll)**| 1 (0.02s) | 18 i-frames | 12 recovery | 31 (0.52s) | Attack at F20 | 3.5m travel | 0 | 0 |

---

## 4. DEFENSIVE MECHANICS & DEFLECTION

### 1. Perfect Parry (*Shothik Protirukha*)
- **Input Window:** Block key tapped within **0.18 seconds (11 frames)** of an incoming strike.
- **Visual Feedback:** Golden radial spark ring at the point of contact, 0.08s hit-stop (screen freeze), dynamic camera micro-zoom (5%).
- **Tactical Effect:**
  - Zero damage and zero stamina loss to player.
  - Enemy attack bounces backward into Stagger State.
  - Deals **+35 Poise Damage** directly to the attacker.
  - Unlocks an immediate Critical Counter (*Protighat*).

### 2. Standard Block
- Holding Block reduces incoming damage by 80%.
- Consumes Stamina proportional to attack force.
- If Stamina drops to zero during a block, player enters **Guard Break** (1.2s vulnerable lockout).

### 3. Perfect Dodge (*Nikhut Shoron*)
- Dodging within 0.12s of enemy impact creates a ghostly wind ripple afterimage (*Wukong* mechanic).
- Player immediately regains 25 Stamina and gains a 25% movement speed boost for 1.5 seconds.

---

## 5. POISE & POSTURE BAR SYSTEM (*SEKIRO / WUKONG*)

```
ENEMY: [=========================] 100 HP
POISE: [========|||||||||||||||||] 45/100 (Turns Red when Broken)
```

1. **Poise Mechanics:**
   - Both the player and boss possess a secondary **Poise Meter** (Yellow bar).
   - Successful hits and parries inflict Poise Damage.
   - Poise slowly regenerates after 3.0s without receiving hits.
2. **Guard Break & Finisher:**
   - When Poise reaches 100% (filled/broken), the enemy staggers helplessly for 3.5 seconds.
   - A glowing golden prompt (`[F] TAKEDOWN`) appears.
   - Triggering the finisher executes a cinematic non-lethal takedown (e.g., sweeping the legs with the staff, pinning the weapon to the ground, disarming the aggressor).

---

## 6. HIT-STOP, CAMERA SHAKE & AUDIO FEEL

- **Hit-Stop (Micro Freeze):**
  - Light hits: 35ms freeze.
  - Heavy strikes: 65ms freeze.
  - Perfect Parry: 85ms freeze with motion blur fade.
- **Directional Camera Impulse:**
  - Traumatic horizontal swings impart a 0.08m camera kick along the impact normal, settling over 0.2s via spring damping.
- **Audio Cues:**
  - Sharp wood-on-wood clatter for staff parries.
  - Deep low-end bass thud (60–90 Hz) for heavy blunt impacts.
  - High-frequency metallic sheen (2–4 kHz) for successful deflections.
