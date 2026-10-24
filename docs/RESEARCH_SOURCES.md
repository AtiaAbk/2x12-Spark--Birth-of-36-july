# RESEARCH SOURCES — Evidence Register for Player-Visible History
**Project:** Spark: Birth of 36 July
**Created:** 2026-10-24 (work order T2). Format follows reference §4 / Appendix A §3.
**Machine-readable mirror:** `assets/data/historical_sources.json`. Every claim `id` below appears there too, with the same label. Change both files together.
**Labels (DECISION-011):** VERIFIED · CORROBORATED BUT INCOMPLETE · DISPUTED / UNCONFIRMED · FICTIONAL GAMEPLAY · RECONSTRUCTION

This register is the project's own evidence file. It is **not** the lost Batch 105 / v12.97 chronology (see `docs/DECISIONS.md` §4).

Rules used in this pass:
- A claim is VERIFIED only if a source listed in §1 was actually loaded and read and states it.
- Anything that only came from a search-result summary is marked as such, and is never the sole basis for VERIFIED.
- No quotation is attributed to a real person unless the quoted words appear in a loaded source.

---

## 1. Source list (with load status, checked 2026-10-24)

| Src ID | Title | Publisher | Publication date | URL | Loaded? |
|---|---|---|---|---|---|
| SRC-OHCHR | *Human Rights Violations and Abuses related to the Protests of July and August 2024 in Bangladesh* (Fact-Finding Report) | Office of the UN High Commissioner for Human Rights (OHCHR) | 12 Feb 2025 | https://www.ohchr.org/sites/default/files/documents/countries/bangladesh/ohchr-fftb-hr-violations-bd.pdf | **Direct URL failed: HTTP 403** to both WebFetch and curl. Full text was read from the Internet Archive copy `https://web.archive.org/web/2025id_/https://www.ohchr.org/sites/default/files/documents/countries/bangladesh/ohchr-fftb-hr-violations-bd.pdf` (PDF, 3.0 MB, text extracted with pdftotext). Paragraph numbers below refer to that text. |
| SRC-UNBD | "Bangladesh: UN report finds brutal, systematic repression of protests, calls for justice for serious rights violations" | United Nations in Bangladesh | 12 Feb 2025 | https://bangladesh.un.org/en/289108-bangladesh-un-report-finds-brutal-systematic-repression-protests-calls-justice-serious | Yes |
| SRC-EUAA | "Annex 1: Timeline of the protests in July–August 2024", *COI Report – Bangladesh: Country Focus* | European Union Agency for Asylum (EUAA) | Aug 2025 (per page) | https://www.euaa.europa.eu/coi/bangladesh/2025/country-focus/coi-report-bangladesh-country-focus/annex-1-timeline-protests-july-august-2024 | Yes. This index page has **section headings only**. The sub-pages read were: `…/annex-1-timeline-protests-july-august-2024/14-17-july-2024-clashes-chhatra-league-and-law-enforcement`, `…/20-july-2024-shoot-sight-curfew`, `…/4-5-august-2024-last-days-protests-hasina-flees-country` (all on `https://euaa.europa.eu/coi/bangladesh/2025/country-focus/`). All three loaded. |
| SRC-DS-TL | "Timeline of student protests" | The Daily Star | 3 Aug 2024 (updated 4 Aug 2024) | https://www.thedailystar.net/news/bangladesh/news/timeline-student-protests-3668996 | Yes. Covers **15 Jul – 2 Aug only**. No 5 June–14 July entries; no mention of Abu Sayed, Mugdho, the one-point demand or the departure. |
| SRC-JA | "Timeline — July Archive" | July Archive ("A Dooplin project") | not shown on page | https://www.julyarchive.org/timeline | Yes (5 Jun – 5 Aug 2024 entries) |
| SRC-DS-MUGDHO | "A promising life cut short by bullet" (Mashfiq Mizan) | The Daily Star ("Lives we lost") | 2 Aug 2024 (updated 8 Sep 2024) | https://www.thedailystar.net/news/bangladesh/lives-we-lost/news/promising-life-cut-short-bullet-3668151 | Yes |
| SRC-DSB-MUGDHO | ‘পানি লাগবে কারো, পানি?’—বুলেটে নিঃশেষ যে জীবন | The Daily Star Bangla | 3 Aug 2024 | https://bangla.thedailystar.net/news/bangladesh/quota-protest/news-602186 | Yes |
| SRC-BSS-MUGDHO | "Mugdho will never ask anyone, anybody needs water brother ?" (Selina Sheuly) | Bangladesh Sangbad Sangstha (BSS) | 28 Oct 2024 | https://www.bssnews.net/stories-of-mass-upsurge/219171 | Yes |
| SRC-TBS-15JUL | "15 July 2024: The dawn of brutality" | The Business Standard | 15 Jul 2025 | https://www.tbsnews.net/node/1188326 | Yes |
| SRC-PA-1POINT | "Students Against Discrimination declares one-point demand for govt's resignation" | Prothom Alo (English) | 3 Aug 2024 | https://en.prothomalo.com/bangladesh/rd12isd6ni | Yes |
| SRC-KT-REUTERS | "Bangladesh PM Hasina resigns, flies off to India in military helicopter" (Reuters copy) | Khaleej Times / Reuters | 5 Aug 2024 | https://www.khaleejtimes.com/world/asia/bangladesh-pm-hasina-took-off-to-india-in-military-helicopter-reports | Yes |
| SRC-DS-RAJU | "A voice that still resonates" (Moyukh Mahtab) | The Daily Star | 12 Mar 2018 (updated 13 Mar 2018) | https://www.thedailystar.net/node/1547224 | Yes |
| SRC-DS-APARAJEYO | "Modeling for the Aporajeyo Bangla" (Fahmim Ferdous) | The Daily Star | 14 Dec 2017 | https://www.thedailystar.net/arts-entertainment/modeling-the-aporajeyo-bangla-1505080 | Yes |
| SRC-FE-36JULY | "July 36: when Bangladesh saw democracy restored after 18 years" (Yasir Wardad) | The Financial Express (Bangladesh) | 5 Aug 2026 | https://thefinancialexpress.com.bd/national/july-36-when-bangladesh-saw-democracy-restored-after-18-years | Yes |
| SRC-DU | University of Dhaka home page | University of Dhaka | n/a (live site) | https://www.du.ac.bd/ | Yes |

Other URLs checked:
- `https://old.du.ac.bd/footer_widget/footer_widget_item/map` (reference starting source 7): **failed**. curl got no response within 25 s.
- `https://www.du.ac.bd/index.php/officeContact/TSC` and `https://development.juniv.edu/`: HTTP 200, but the content was **not reviewed** in this pass (location work is task T3).
- Wikipedia articles ("Mir Mugdho", "Razakar slogans") were read **only to find primary sources**. They are not used as the basis of any label.

Seen in search results but **not fetched**, so used for nothing beyond open questions:
- Prothom Alo report of a helicopter stop at Agartala on 5 Aug.
- Deccan Herald / ANI / AFP accounts of a C-130J flight to Hindon.
- Al Jazeera "Who were the Razakars…" (22 Jul 2024).

---

## 2. Claim register (one entry per player-visible historical claim, scene or memorial text)

Index:

| id | Claim (short) | Label | In-game location |
|---|---|---|---|
| L1-01 | 5 June 2024 High Court verdict reinstated the 30% freedom-fighter quota | VERIFIED | Memorial 1 text; `historical_*` docs |
| L1-02 | Dhaka University students resumed peaceful quota protests in early July (memorial at Curzon Hall) | CORROBORATED BUT INCOMPLETE | `JulyMemorials` entry 1 |
| L1-03 | Bangla Blockade, early July; Dhaka at a standstill on 7 July (memorial at Aparajeyo Bangla) | CORROBORATED BUT INCOMPLETE | `JulyMemorials` entry 2 |
| L1-04 | Aparajeyo Bangla depicts three youths of the 1971 war, in front of the Arts Faculty | VERIFIED | `JulyMemorials` entry 2 |
| L1-05 | Chant "Amar Shonar Bangla, Boishammo Mukto Bangla" and quote "Merit over privilege…" | DISPUTED / UNCONFIRMED (removed from game) | formerly `JulyMemorials` 1 & 2 |
| L1-06 | Raju Sculpture honours Moin Hossain Raju, shot near TSC in 1992 | VERIFIED | `JulyMemorials` entry 3 |
| L1-07 | 14 July "razakar" remark and the chant "Who are you? Who am I? Razakar…" | VERIFIED | docs; dialogue context |
| L1-08 | 15 July Chhatra League attacks at DU and at Dhaka Medical College Hospital | VERIFIED | `JulyMemorials` 3; Level 2; HUD victory ch. 2 |
| L1-09 | Nationwide internet shutdown from the evening of 18 July to 23 July | VERIFIED | `JulyMemorials` 4; Level 4; HUD victory ch. 4 |
| L1-10 | Students at Hakim Chattar organising relief during the blackout | FICTIONAL GAMEPLAY | `JulyMemorials` 4 |
| L1-11 | Dhaka University opened on 1 July 1921 | VERIFIED | HUD boundary-stone entry |
| L1-12 | The "1921 boundary stone" object itself | FICTIONAL GAMEPLAY | HUD boundary-stone entry |
| L1-13 | Campus geometry (Curzon Hall, Aparajeyo Bangla, Raju, Library, TSC) | RECONSTRUCTION | whole Level 1/2/6 map |
| L2-01 | Level 2 (15 July) fights, triage, medical tent | FICTIONAL GAMEPLAY | `Level2StudentConfrontation` |
| L3-01 | Abu Sayed killed by police in Rangpur, 16 July, arms outstretched | VERIFIED | HUD victory ch. 3; L6 memorial |
| L3-02 | Abu Sayed's age (23 vs 25) and "point-blank" range | DISPUTED / UNCONFIRMED (not shown in game) | docs only |
| L3-03 | At least six killed on 16 July | VERIFIED | docs |
| L3-04 | Mir Mugdho killed in Uttara on 18 July while handing out water | VERIFIED | L6 memorial target; docs |
| L3-05 | Mugdho's call "পানি লাগবে কারো, পানি, পানি?" | VERIFIED (wording); **removed from gameplay prompts** | formerly `Level3PoliceEscalation` |
| L3-06 | Tear gas against protesters in Dhaka on 18 July (Level 3 setting at DU/Shahbagh) | CORROBORATED BUT INCOMPLETE | `Level3PoliceEscalation` |
| L4-01 | Curfew from midnight on 19 July; 27,000 soldiers deployed | VERIFIED | `Level4CurfewBlackout` date & setting |
| L4-02 | "Shoot on sight" orders | VERIFIED (as a public statement) | docs only; not in game strings |
| L4-03 | Armoured vehicles and checkpoints during the curfew (vehicle model) | RECONSTRUCTION | `Level4CurfewBlackout` |
| L4-04 | Courier, casualty dossier, foreign correspondent | FICTIONAL GAMEPLAY | `Level4CurfewBlackout` |
| L4-05 | Arrest and death-toll numbers (">10,000 arrested", ">200 killed in blackout") | CORROBORATED BUT INCOMPLETE | docs only |
| L4-06 | Helicopter gunfire on 19–21 July | DISPUTED / UNCONFIRMED (kept out of game) | none |
| L5-01 | One-point demand announced at Central Shaheed Minar, 3 Aug 2024 | VERIFIED | `Level5MassMobilization`; HUD ch. 5 |
| L5-02 | Crowd size at Shaheed Minar on 3 Aug | CORROBORATED BUT INCOMPLETE | HUD ch. 5 (softened) |
| L5-03 | 4 Aug non-cooperation; about 93–97 killed; March to Dhaka called | VERIFIED | HUD ch. 5; docs |
| L5-04 | Player announcing the demand; human chain holding off agitators | FICTIONAL GAMEPLAY | `Level5MassMobilization` |
| L6-01 | 5 Aug: hundreds of thousands marched towards central Dhaka | VERIFIED | HUD ch. 6 |
| L6-02 | Army "refused to fire" / stood by on 5 Aug | CORROBORATED BUT INCOMPLETE | docs only |
| L6-03 | Sheikh Hasina resigned and left the country on 5 Aug | VERIFIED | HUD ch. 6 |
| L6-04 | Exact departure point and route on 5 Aug | DISPUTED / UNCONFIRMED (not shown in game) | none |
| L6-05 | "36 July" as the name for 5 August | VERIFIED | `JulyMemorials` 5; level titles |
| L6-06 | Up to 1,400 killed, 15 July – 5 August | VERIFIED (as an OHCHR estimate) | HUD ch. 6 |
| L6-07 | Victory march, marigold wreath, "Bell of Freedom" at TSC | FICTIONAL GAMEPLAY | `Level6VictoryEpilogue` |
| D-01 | All NPC and protagonist dialogue (Rafi, Amina, Tanvir, Mofiz Chacha, generic students) | FICTIONAL GAMEPLAY | `DialogueRepository`, `STORY_BIBLE.md` |

---

### L1-01 — High Court reinstates the quota (5 June 2024)
- **Date / location:** 5 June 2024, High Court Division, Dhaka.
- **Sources:**
  - SRC-OHCHR para. 19: "On 5 June 2024, the High Court Division … reinstated a quota system that reserved 30 percent of all public sector jobs for the descendants of the fighters". The same paragraph says the 2018 abolition was ruled unconstitutional.
  - SRC-JA, 5 Jun entry.
- **Supports:** the date, the court, the 30% freedom-fighter quota, and that the 2018 circular was struck down.
- **Does not establish:** the "56% total quota" figure in the old docs. Neither source read here states it, so it was removed from the docs.
- **Label:** VERIFIED.
- **Game portrayal:** background in memorial 1 ("the High Court's 5 June verdict").
- **Open question:** source for the total reserved share before 2018 if it is ever shown.

### L1-02 — Early-July protests at Dhaka University (memorial 1, Curzon Hall)
- **Date / location:** 1–7 July 2024, Dhaka University and Shahbagh.
- **Sources:**
  - SRC-JA, 1 Jul: "Protests resume".
  - SRC-JA, 2–3 Jul: Shahbagh blockades.
  - SRC-OHCHR para. 33 (context of July) and Image 1: "Student protesters on 7 July 2024 in Dhaka".
- **Supports:** students resumed peaceful protest at the start of July, with Shahbagh as a focal point.
- **Does not establish:** any specific gathering at the **Curzon Hall arcade**. It also does not support the quotation "Merit over privilege. Equality for all citizens." (see L1-05).
- **Label:** CORROBORATED BUT INCOMPLETE. The event is supported; the Curzon Hall placement is a RECONSTRUCTION.
- **Game portrayal (new text):** students "returned to peaceful protest". The memorial says outright that the Curzon Hall setting is a game reconstruction.
- **Open question:** a dated photo or report of science-faculty students marching from Curzon Hall in early July.

### L1-03 — The "Bangla Blockade" (memorial 2, dated 7 July)
- **Date / location:** 6–7 July 2024 onward; Shahbagh and other Dhaka junctions, plus highways.
- **Sources:**
  - SRC-JA, 6 Jul: "'Bangla Blockade' begins".
  - SRC-JA, 7 Jul: "Dhaka largely at a standstill".
  - SRC-EUAA section heading "7–10 July: The 'Bangla Blockade', and temporary suspension of the quota".
- **Supports:** the name, the dates, and road blockades in Dhaka.
- **Does not establish:**
  - a "mass oath of non-violent resistance" at Aparajeyo Bangla, which was removed;
  - "thousands gathered" at that sculpture on 7 July, which was removed;
  - "zero property damage", "emergency lanes preserved" and similar wording from the old docs.
- **Label:** CORROBORATED BUT INCOMPLETE.
- **Game portrayal (new text):** students called a "Bangla Blockade", sat in at Shahbagh and other junctions, and Dhaka was largely at a standstill on 7 July. The gathering at the sculpture is stated to be a reconstruction.
- **Open question:** whether Aparajeyo Bangla was itself a rally point in this week.

### L1-04 — Aparajeyo Bangla
- **Location:** in front of the Faculty of Arts, Dhaka University.
- **Source:** SRC-DS-APARAJEYO. It describes three youths, "representing people from all walks of life", who stood against the Pakistan army in 1971. One is a woman with a first-aid kit. Sculptor: the late Syed Abdullah Khalid.
- **Supports:** the description "three figures of the 1971 Liberation War" and the location.
- **Does not establish:** height or completion year. Search summaries conflict (1978 vs 1979; 12 ft vs 5.5 m), so neither is used.
- **Label:** VERIFIED.
- **Game portrayal:** named as a sculpture of three youths of 1971. Geometry is a RECONSTRUCTION (L1-13).

### L1-05 — Chant "Amar Shonar Bangla, Boishammo Mukto Bangla" and the quote "Merit over privilege…" (REMOVED)
- **Where it was:** `JulyMemorials` entries 1 and 2; `historical_research.md` Phase 1.
- **Search done:** web search for the slogan in English transliteration and Bangla, 2026-10-24. No loaded source contains either phrase.
- **Supports:** nothing found.
- **Label:** DISPUTED / UNCONFIRMED.
- **Game portrayal:** both phrases have been **removed** from player-visible text. They are not to be reinstated without a dated source.
- **Open question:** if the user has a source for either slogan, add it here first.

### L1-06 — Anti-Terrorism Raju Memorial Sculpture
- **Location:** near TSC, Dhaka University.
- **Source:** SRC-DS-RAJU. Moin Hossain Raju, a Bangladesh Chatra Union activist, was shot near TSC in 1992 while leading a procession against campus violence. The sculpture, by Shaymol Chowdhury, shows students with linked arms.
- **Does not establish:** the exact day. The article implies 12 March 1992; a search summary gives 13 March 1992. The game does not show the day.
- **Label:** VERIFIED.
- **Game portrayal:** memorial 3 names Raju and the 1992 shooting near TSC without a day, and describes the sculpture.
- **Open question:** 12 vs 13 March 1992.

### L1-07 — 14 July "razakar" remark and the chant
- **Date / location:** 14 July 2024. Press conference in the afternoon or evening; campus protests that night, including at the Raju Sculpture.
- **Sources:**
  - SRC-OHCHR para. 33 quotes the PM's remark at the 14 July press conference.
  - SRC-OHCHR para. 34: students chanted "Who are you? Who am I? Razakar, razakar." This was "later expanded with the clarifier, 'Says who? Says who? The dictator, the dictator!'". Hundreds of women at Dhaka University broke the night locks of their halls to join.
  - SRC-TBS-15JUL: students from several halls gathered at the Raju Sculpture.
  - SRC-EUAA 14–17 July page.
- **Supports:** the remark (in OHCHR's English translation), the English chant wording, and that the "dictator" clarifier was a **later** addition.
- **Does not establish:**
  - the exact Bangla/romanised chant spelling. The romanised form in the old docs, "…Shwairachar", was only seen on Wikipedia, which gives স্বৈরাচার.
  - that both halves were chanted together on the first night.
  - an exact "10:30 PM" start time, which was in the old docs and is now removed.
- **Label:** VERIFIED for the English wording per OHCHR. The romanised Bangla spelling is CORROBORATED BUT INCOMPLETE.
- **Game portrayal:** not quoted in game strings. The docs quote the OHCHR English wording.

### L1-08 — 15 July attacks at Dhaka University and DMCH
- **Date / location:** 15 July 2024. Dhaka University (Bijoy Ekattor Hall, campus exits); Dhaka Medical College Hospital in the early evening.
- **Sources:**
  - SRC-OHCHR para. 85: Chhatra League supporters were "armed with machetes, axes, sticks, motorbike helmets for protection and some pistols"; they "indiscriminately attacked student protesters" shortly after 15:00, and police made "no meaningful effort" to stop it.
  - SRC-OHCHR para. 86: the storming of DMCH emergency units.
  - SRC-OHCHR para. 79: at about 03:00 on 15 July, the Chhatra League president addressed supporters at the Raju Memorial Statue.
  - SRC-DS-TL, 15 Jul: "more than 300 injured on both sides".
  - SRC-EUAA 14–17 July page.
- **Supports:** attackers in helmets with sticks, rods and bladed weapons; female students beaten; DMCH attacked; hundreds injured.
- **Does not establish:**
  - "over 300 **students** hospitalised". Daily Star says more than 300 injured **on both sides**.
  - that protesters were all unarmed. OHCHR says some armed themselves with sticks once they expected an attack.
- **Label:** VERIFIED.
- **Game portrayal:** memorial 3 and the chapter 2 victory text describe armed attackers striking student protesters, including at the hospital. No numbers are given in game.

### L1-09 — Internet shutdown
- **Date:** mobile internet was cut around DU and other campuses from just after midnight on 14–15 July. Nationwide 4G was cut from midnight on 17–18 July, broadband on the evening of 18 July, and the shutdown lasted until 23 July.
- **Sources:**
  - SRC-OHCHR para. 44: "imposed a complete Internet shutdown on the country that would last until 23 July".
  - SRC-OHCHR para. 193.
  - SRC-DS-TL, 18 and 23 Jul.
  - SRC-JA, 18, 23 and 28 Jul.
- **Does not establish:** "170 million people in total isolation" (old docs; removed) or "international phone circuits severed".
- **Label:** VERIFIED.
- **Game portrayal:** memorial 4; Level 4 setting ("Blackout Corridor"); chapter 4 victory text.

### L1-10 — Students at Hakim Chattar organising relief during the blackout
- **Sources:** none found for this place. SRC-OHCHR para. 161 supports only the general point that the curfew and the internet shutdown hindered media and NGO documentation.
- **Label:** FICTIONAL GAMEPLAY.
- **Game portrayal:** memorial 4 now says this scene is a game reconstruction, not a documented event at that spot.

### L1-11 — Dhaka University opened on 1 July 1921
- **Source:** SRC-DU: "On the first day of July 1921".
- **Label:** VERIFIED.
- **Game portrayal:** HUD boundary-stone entry, "Founding Era — July 1, 1921".

### L1-12 — The "1921 boundary stone"
- **Sources:** none. No source was found for a granite boundary marker on the eastern perimeter.
- **Label:** FICTIONAL GAMEPLAY (a game prop).
- **Game portrayal:** the entry text now calls it a game reconstruction, not a documented artefact.

### L1-13 — Campus geometry
- **Label:** RECONSTRUCTION. Dimensions are not surveyed. Location reference packs are work-order task T3 and are not part of this register.

### L2-01 — Level 2 gameplay (15 July)
- **Historical anchor:** L1-08.
- **Label:** FICTIONAL GAMEPLAY. The player defends, gives first aid and escorts people to a medical tent; named aggressors are invented.
- **Note:** objectives in this file are being re-tuned by another agent (task T5). This pass changed no Level 2 strings. The only Level 2 text that is historical is the date "July 15, 2024" and the setting "Raju Memorial Sculpture — TSC Roundabout", both backed by L1-08.

### L3-01 — Abu Sayed (16 July 2024, Rangpur)
- **Location:** Begum Rokeya University, Rangpur.
- **Sources:**
  - SRC-OHCHR Case 1: "When police started shooting at the crowd, Abu Sayed raised his hands"; officers were "approximately 14-15 meters away"; he was shot with shotguns loaded with metal pellets, at least twice; OHCHR calls it "a deliberate extrajudicial killing by the police".
  - SRC-UNBD: same finding, "about 14 metres".
  - SRC-JA, 16 Jul.
  - SRC-EUAA 14–17 July page, which cites Amnesty International: arms open, "posing no apparent physical threat".
- **Supports:** the date, the place, his arms raised or spread, police shotgun fire, and that footage of his killing mobilised more protesters (OHCHR para. 37).
- **Does not establish:** "point-blank range" (old docs). OHCHR puts the distance at 14–15 m, so that wording is removed.
- **Label:** VERIFIED.
- **Game portrayal:** chapter 3 victory text says he stood with his arms spread wide before the police and was killed by police fire. Nothing graphic. He is never interactive and never a score item. He is also honoured at the Level 6 floral memorial.

### L3-02 — Abu Sayed's age; "point-blank"
- **Sources:** SRC-OHCHR Case 1 says "Twenty-three-year-old". SRC-JA says 25.
- **Label:** DISPUTED / UNCONFIRMED.
- **Game portrayal:** age not shown. Docs give both figures with their sources.

### L3-03 — At least six killed on 16 July
- **Sources:** SRC-OHCHR para. 37; SRC-DS-TL, 16 Jul.
- **Label:** VERIFIED. Docs only.

### L3-04 — Mir Mahfuzur Rahman Mugdho (18 July 2024, Uttara)
- **Date / location:** 18 July 2024, around 17:50. Rabindra Sarani, Uttara (OHCHR). Azampur, Uttara (Daily Star, BSS).
- **Sources:**
  - SRC-OHCHR Case 3: "Mir Mughdo, a 25-year-old student who had been distributing water to the protesters", shot "in the head with lethal ammunition from a rifle", with a video authenticated by OHCHR.
  - SRC-DS-MUGDHO: he was distributing water after tear gas, and law enforcers opened fire.
  - SRC-BSS-MUGDHO: names the police.
- **Supports:** the date, the area, what he was doing, and that he was killed by gunfire from security forces.
- **Does not establish:** which individual or unit fired. OHCHR describes police and RAB firing in the area.
- **Label:** VERIFIED. The wound location is documented in the sources but is **deliberately not used** in game or in summary docs, to keep the treatment non-graphic.
- **Game portrayal:** named at the Level 6 floral memorial only. There is **no** playable Mugdho scene. Level 3 is set at DU/Shahbagh, not Uttara, so it must not be presented as his scene (reference §4 "Mugdho scene caution").

### L3-05 — "পানি লাগবে কারো, পানি, পানি?"
- **Sources:**
  - SRC-DSB-MUGDHO: body text quotes the Bangla, and the headline is ‘পানি লাগবে কারো, পানি?’.
  - SRC-DS-MUGDHO: caption in English, "Water, water, who needs water?"
  - SRC-BSS-MUGDHO: English, "Water, water, anybody needs water, brother?"
- **Supports:** the wording of his call, from a video recorded shortly before his death.
- **Does not establish:** the short form "Pani lagbe, pani?" used in the old game strings. That exact form is not in any loaded source.
- **Label:** VERIFIED (for the Daily Star Bangla wording).
- **Game portrayal:** **removed** from the Level 3 objective target, interaction prompt and completion toast. Two reasons:
  - It made a real victim's last words into a button prompt and a reward toast (reference §4: do not convert a real death into a reward mechanic).
  - Level 3's location is not where he was killed.
- **Open question:** a respectful, sourced memorial card quoting SRC-DSB-MUGDHO could be added on the Credits & Sources screen (task T4).

### L3-06 — Tear gas in Dhaka on 18 July (Level 3 setting)
- **Sources:** SRC-OHCHR paras. 43 and 139 (tear gas, including from helicopters, in Mirpur, Mohakhali and Dhanmondi on 18 July; Shahbagh on 19 July); SRC-DS-TL, 18 Jul.
- **Does not establish:** a specific tear-gas event on the DU perimeter at the Shahbagh approach on 18 July, or the use of "CS" gas in particular.
- **Label:** CORROBORATED BUT INCOMPLETE. The location is a RECONSTRUCTION.
- **Game portrayal:** water and first-aid objectives, which are FICTIONAL GAMEPLAY. "CS gas" was changed to "tear gas".

### L4-01 — Curfew and army deployment
- **Sources:**
  - SRC-OHCHR para. 44: on 19 July "the Prime Minister ordered a national curfew, effective from midnight, and deployed 27,000 Army soldiers".
  - SRC-EUAA 20 July page: patrols and checkpoints.
  - SRC-DS-TL, 19–20 Jul.
  - SRC-JA, 19 Jul.
- **Label:** VERIFIED.
- **Game portrayal:** Level 4 date "July 20 – 23, 2024" and the curfew setting.

### L4-02 — "Shoot on sight"
- **Sources:**
  - SRC-OHCHR para. 117: Awami League Secretary-General and minister Obaidul Quader told reporters on 19 July that security forces had been given orders to "shoot on sight".
  - SRC-EUAA 20 July page title ("A 'shoot-on-sight' curfew").
  - SRC-EUAA 4–5 Aug page: an indefinite "shoot-on-sight" curfew was declared on 4 Aug, citing Prothom Alo.
- **Does not establish:** the old docs' "military curfew imposed with shoot-on-sight orders" as a formal written order. What is documented is a public statement and later reporting.
- **Label:** VERIFIED as a public statement attributed by OHCHR.
- **Game portrayal:** not in any game string.

### L4-03 — Armoured vehicles and checkpoints
- **Sources:** SRC-EUAA 20 July page (army patrols and checkpoints); SRC-OHCHR Case 3 (a police armoured vehicle in Uttara).
- **Does not establish:** the vehicle model. "BTR-80" in the HUD text has no source.
- **Label:** RECONSTRUCTION.
- **Game portrayal:** strings now say "armoured vehicle". The 3D model stays a generic 8×8 APC.

### L4-04 — Courier, casualty dossier, foreign correspondent
- **Label:** FICTIONAL GAMEPLAY. It is motivated by SRC-OHCHR para. 161 (the curfew and internet shutdown hindered documentation), but it is not a documented event.

### L4-05 — Arrest and death numbers in the old docs
- **Sources:**
  - SRC-OHCHR para. 160: police arrested 10,525 men and 25 women (63 of them children) from 1 July to 4 August, and RAB made a further 1,152 arrests.
  - SRC-OHCHR para. 118: about 100 deaths on 18 July and close to 300 on 19 July (OHCHR estimate).
  - SRC-DS-TL: an official toll of 147 on 28 July.
- **Label:** CORROBORATED BUT INCOMPLETE. The docs now cite these figures instead of ">10,000 in midnight sweeps" and "over 200 killed during the blackout".
- **Game portrayal:** no numbers in game strings.

### L4-06 — Helicopter gunfire (19–21 July)
- **Source:** SRC-OHCHR paras. 138–144.
  - Tear gas and sound grenades from RAB and police helicopters are documented.
  - Witnesses describe rifles or shotguns fired from helicopters.
  - RAB denies firing firearms from helicopters.
  - OHCHR "cannot confirm or exclude the shooting of rifles or shotguns from helicopters" (para. 144) and notes that tear-gas launchers "can look like rifles or shotguns from a distance" (para. 143).
- **Label:** DISPUTED / UNCONFIRMED. Tear gas from helicopters is VERIFIED.
- **Game portrayal:** no helicopter appears in any level. It must not be conflated with the 5 August departure (L6-03).

### L5-01 — One-point demand, 3 August 2024
- **Location / time:** Central Shaheed Minar, Dhaka University campus, about 5:30 pm.
- **Sources:**
  - SRC-PA-1POINT: announced by coordinator Nahid Islam through a megaphone. The demand was "the downfall of Prime Minister Sheikh Hasina and the incumbent government".
  - SRC-OHCHR para. 50: the movement "coalesced around a new, single demand".
- **Label:** VERIFIED.
- **Game portrayal:** Level 5 setting and chapter 5 victory text. The real announcement was made by Nahid Islam. The player does not make it (L5-04).

### L5-02 — Crowd size on 3 August
- **Source:** SRC-PA-1POINT: "thousands of students and demonstrators", with the crowd spreading into adjacent areas.
- **Does not establish:** "hundreds of thousands" or "the largest peaceful rally in modern Bangladesh history" (old docs).
- **Label:** CORROBORATED BUT INCOMPLETE.
- **Game portrayal:** changed to "a vast crowd" / "the assembled crowd".

### L5-03 — 4 August
- **Sources:**
  - SRC-EUAA 4–5 Aug page: 93–97 killed per Daily Star / Prothom Alo; indefinite curfew.
  - SRC-JA, 4 Aug: "Deadliest day … (93+ killed); 'March to Dhaka' called".
  - SRC-PA-1POINT: non-cooperation was to start the next day.
- **Label:** VERIFIED.
- **Game portrayal:** chapter 5 victory text says non-cooperation began and a March to Dhaka was called.

### L5-04 — Player-led human chain and announcement
- **Label:** FICTIONAL GAMEPLAY. Strings were changed so that the player stands **with** the assembly as the demand is announced, rather than proclaiming it.

### L6-01 — March on Dhaka, 5 August
- **Source:** SRC-OHCHR para. 52: "hundreds of thousands of protesters marched towards central Dhaka. Police and armed Awami League supporters fired at protesters in many locations." SRC-EUAA 4–5 Aug page cites a Daily Star estimate of at least a million.
- **Does not establish:** "Millions marched into Shahbagh and the National Parliament" as a single fact (old chapter 6 text, now replaced).
- **Label:** VERIFIED for "hundreds of thousands".
- **Game portrayal:** chapter 6 victory text.

### L6-02 — Army on 5 August
- **Sources:**
  - SRC-OHCHR para. 51: on 3 Aug, junior officers told the Army Chief they did not wish to shoot.
  - SRC-OHCHR para. 52: Army and BGB "largely stood by", though soldiers shot at Jamuna Future Park.
  - SRC-EUAA 4–5 Aug page, citing Reuters.
- **Label:** CORROBORATED BUT INCOMPLETE.
- **Game portrayal:** not depicted. Reference §4 forbids inventing a scene of soldiers refusing an order.

### L6-03 — Sheikh Hasina resigned and left the country
- **Sources:**
  - SRC-OHCHR para. 53: "At around 2pm, Sheikh Hasina was flown out of Dhaka by Armed Forces helicopter and then out of the country. Shortly after, protesters stormed her official residence." Para. 54: the Army Chief announced her departure.
  - SRC-KT-REUTERS: she landed at Hindon military airfield near Delhi.
  - SRC-JA, 5 Aug.
- **Label:** VERIFIED.
- **Game portrayal:** chapter 6 victory text says she resigned and left the country. It names **no place and no vehicle**. Level 6 is set on the DU campus, and nothing in its text suggests the departure happened there.

### L6-04 — Departure point and route
- **Sources:**
  - SRC-OHCHR names no take-off point.
  - SRC-KT-REUTERS says she and her sister were taken to a "safe shelter" away from her official residence, and gives no origin for the helicopter.
  - Search-only, not fetched: a Prothom Alo report of a helicopter stop at Agartala, and other reports of a C-130 flight to Hindon.
- **Label:** DISPUTED / UNCONFIRMED.
- **Game portrayal:** not shown. The old docs' "flees by military helicopter" / "departs by military aircraft" are replaced with the OHCHR wording.

### L6-05 — "36 July"
- **Source:** SRC-FE-36JULY: the name reflected the belief that the movement begun in July had not ended with the calendar month. Search results (not fetched) describe 1 August being counted as "32 July".
- **Label:** VERIFIED for the meaning of the name.
- **Game portrayal:** memorial 5 and the Level 6 titles.

### L6-06 — Death estimate
- **Sources:** SRC-OHCHR para. 58: "as many as 1,400 protest-related deaths … for the period of 15 July to 5 August". SRC-UNBD.
- **Label:** VERIFIED as an OHCHR estimate. It must always be phrased as "as many as" / "may have been".
- **Game portrayal:** chapter 6 victory text: "As many as 1,400 people may have been killed (UN estimate)."

### L6-07 — Level 6 actions
- **Label:** FICTIONAL GAMEPLAY. This covers the victory march through gates, the marigold wreath and the "Bell of Freedom" at TSC. No source was found for a historic bell at TSC, so "Historic" was removed from the bell strings.

### D-01 — Dialogue
- **Label:** FICTIONAL GAMEPLAY.
  - No line in `DialogueRepository` is spoken by, or attributed to, a real person.
  - Slogan-like lines ("এক দফা এক দাবি", "শহীদ মিনার থেকে গণভবন") are spoken by fictional students. They are not presented as documented chants.
  - The Ganabhaban mention is a marching destination only. Per SRC-EUAA, crowds approached the official residence on 5 Aug. The line does not refer to the departure.

---

## 3. Summary of this pass

- 38 rows in total:
  - 19 VERIFIED;
  - 6 CORROBORATED BUT INCOMPLETE, with the uncertain part stated;
  - 4 DISPUTED / UNCONFIRMED, none of them shown in game as fact;
  - 7 FICTIONAL GAMEPLAY;
  - 2 RECONSTRUCTION.
- Player-visible strings softened or removed: memorials 1–4 (wording); memorial 5 (wording); boundary stone; Level 3 Mugdho phrase (×3 strings) and "CS gas"; Level 4 "BTR-80" (×2); Level 5 announcement and crowd size (×4); Level 6 "Historic Bell" (×2); HUD victory text for chapters 2, 3, 5 and 6.
- Left open: L1-02 (Curzon Hall placement), L1-03 (Aparajeyo Bangla as a rally point), L1-06 (day of Raju's death), L3-02 (Abu Sayed's age), L4-06 (helicopter gunfire), L6-04 (departure route), L1-05 (slogans the user may have a source for).
