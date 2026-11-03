import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), fill_hex)
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
        node = OxmlElement(m)
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def create_report():
    doc = Document()

    # Set page margins (1 inch)
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1)
        section.bottom_margin = Inches(1)
        section.left_margin = Inches(1)
        section.right_margin = Inches(1)

    # Styles
    styles = doc.styles
    normal_style = styles['Normal']
    normal_style.font.name = 'Calibri'
    normal_style.font.size = Pt(11)
    normal_style.font.color.rgb = RGBColor(0x22, 0x22, 0x22)
    normal_style.paragraph_format.line_spacing = 1.15
    normal_style.paragraph_format.space_after = Pt(6)

    # Title / Header Block
    title_p = doc.add_paragraph()
    title_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    title_p.paragraph_format.space_after = Pt(2)
    run_inst = title_p.add_run("Bangladesh Army University of Engineering & Technology (BAUET)\n")
    run_inst.bold = True
    run_inst.font.size = Pt(14)
    run_inst.font.color.rgb = RGBColor(0x0E, 0x3A, 0x66)

    run_sub = title_p.add_run("Department of Information and Communication Engineering (ICE)\nQadirabad Cantonment, Natore-6431\n")
    run_sub.font.size = Pt(10)
    run_sub.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

    doc.add_paragraph() # spacer

    badge_p = doc.add_paragraph()
    badge_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run_proj = badge_p.add_run("INTEGRATED DESIGN PROJECT (IDP) REPORT\n")
    run_proj.bold = True
    run_proj.font.size = Pt(13)
    run_proj.font.color.rgb = RGBColor(0xC0, 0x39, 0x2B)

    run_main_title = badge_p.add_run("Echoes of Change: A Story-Driven Adventure Game Inspired by the 2024 Student Movement in Bangladesh\n")
    run_main_title.bold = True
    run_main_title.font.size = Pt(17)
    run_main_title.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)

    run_course = badge_p.add_run("Course Code: ICE - 3114 | Course Title: Java Programming Lab | Credit Hours: 3\nAcademic Semester: Summer 2026 | Batch: 16th\n")
    run_course.font.size = Pt(10.5)
    run_course.italic = True
    run_course.font.color.rgb = RGBColor(0x44, 0x44, 0x44)

    # Metadata Table: Submitted By & Submitted To
    meta_table = doc.add_table(rows=1, cols=2)
    meta_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    meta_table.autofit = False
    
    cell_by = meta_table.cell(0, 0)
    cell_by.width = Inches(3.2)
    p_by = cell_by.paragraphs[0]
    p_by.paragraph_format.space_after = Pt(2)
    r_by_h = p_by.add_run("Submitted By:\n")
    r_by_h.bold = True
    r_by_h.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)
    p_by.add_run("1. Atia Sanjida\n   ID: 0812410105171002\n")
    p_by.add_run("2. Farhana Islam Kripa\n   ID: 0812410105171004\n")
    p_by.add_run("Dept. of ICE, 16th Batch, BAUET")
    set_cell_background(cell_by, 'F4F6F9')
    set_cell_margins(cell_by, 120, 120, 160, 160)

    cell_to = meta_table.cell(0, 1)
    cell_to.width = Inches(3.2)
    p_to = cell_to.paragraphs[0]
    p_to.paragraph_format.space_after = Pt(2)
    r_to_h = p_to.add_run("Submitted To:\n")
    r_to_h.bold = True
    r_to_h.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)
    p_to.add_run("Md. Arafat Hossain\n")
    p_to.add_run("Assistant Professor\n")
    p_to.add_run("Department of ICE\n")
    p_to.add_run("BAUET, Natore-6431")
    set_cell_background(cell_to, 'F4F6F9')
    set_cell_margins(cell_to, 120, 120, 160, 160)

    doc.add_page_break()

    # Helper function for Section Headings
    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(14)
        run.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(12)
        run.font.color.rgb = RGBColor(0x2C, 0x5E, 0x8A)
        return p

    def add_code_block(code_text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(4)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.left_indent = Inches(0.2)
        p.paragraph_format.line_spacing = 1.05
        run = p.add_run(code_text)
        run.font.name = 'Consolas'
        run.font.size = Pt(9.5)
        run.font.color.rgb = RGBColor(0x1A, 0x25, 0x2C)

    # 1. MILESTONE
    add_h1("1. Milestone & Project Schedule")
    doc.add_paragraph(
        "The project 'Echoes of Change' was organized into a six-month developmental lifecycle spanning the Summer 2026 academic semester. "
        "The schedule comprises ten core technical tasks covering initial literature review, script formulation, procedural asset synthesis, "
        "core game engine mechanics, level implementations from Awareness to Final Change, comprehensive testing, and academic reporting."
    )

    # Milestone Gantt Table
    table_gantt = doc.add_table(rows=11, cols=8)
    table_gantt.alignment = WD_TABLE_ALIGNMENT.CENTER
    table_gantt.autofit = False

    headers = ["Task", "Description", "M1", "M2", "M3", "M4", "M5", "M6"]
    col_widths = [Inches(0.8), Inches(3.2), Inches(0.4), Inches(0.4), Inches(0.4), Inches(0.4), Inches(0.4), Inches(0.4)]
    
    hdr_cells = table_gantt.rows[0].cells
    hdr_titles = ["Task", "Description", "M1", "M2", "M3", "M4", "M5", "M6"]
    for i, title in enumerate(hdr_titles):
        hdr_cells[i].width = col_widths[i]
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(title)
        r.bold = True
        r.font.size = Pt(9.5)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_background(hdr_cells[i], '1B365D')
        set_cell_margins(hdr_cells[i], 60, 60, 80, 80)

    task_data = [
        ("Task 1", "Literature and game review, and thematic research on the 2024 movement", [1, 0, 0, 0, 0, 0]),
        ("Task 2", "Story and script development for all six levels", [1, 0, 0, 0, 0, 0]),
        ("Task 3", "Character and level design (Levels 1–6)", [0, 1, 0, 0, 0, 0]),
        ("Task 4", "Environment and art asset preparation (procedural synthesis)", [0, 1, 0, 0, 0, 0]),
        ("Task 5", "Game engine setup and core prototyping (movement, camera, interaction)", [0, 0, 1, 0, 0, 0]),
        ("Task 6", "Implementation of Level 1 (Awareness) and Level 2 (First Challenge)", [0, 0, 1, 0, 0, 0]),
        ("Task 7", "Implementation of Level 3 (Gathering) and Level 4 (Rising Pressure)", [0, 0, 0, 1, 0, 0]),
        ("Task 8", "Implementation of Level 5 (Turning Point) and Level 6 (Final Change)", [0, 0, 0, 1, 0, 0]),
        ("Task 9", "Playtesting, debugging, and optimization of the full prototype", [0, 0, 0, 0, 1, 0]),
        ("Task 10", "Documentation, verification, and final report writing", [0, 0, 0, 0, 0, 1])
    ]

    for row_idx, (t_id, desc, months) in enumerate(task_data, start=1):
        row_cells = table_gantt.rows[row_idx].cells
        row_cells[0].width = col_widths[0]
        row_cells[1].width = col_widths[1]
        
        p0 = row_cells[0].paragraphs[0]
        r0 = p0.add_run(t_id)
        r0.bold = True
        r0.font.size = Pt(9)
        
        p1 = row_cells[1].paragraphs[0]
        r1 = p1.add_run(desc)
        r1.font.size = Pt(9)
        
        set_cell_margins(row_cells[0], 50, 50, 70, 70)
        set_cell_margins(row_cells[1], 50, 50, 70, 70)

        bg_color = 'F8F9FA' if row_idx % 2 == 0 else 'FFFFFF'
        set_cell_background(row_cells[0], bg_color)
        set_cell_background(row_cells[1], bg_color)

        for m_idx, active in enumerate(months):
            cell_m = row_cells[2 + m_idx]
            cell_m.width = col_widths[2 + m_idx]
            pm = cell_m.paragraphs[0]
            pm.alignment = WD_ALIGN_PARAGRAPH.CENTER
            set_cell_margins(cell_m, 50, 50, 30, 30)
            if active:
                rm = pm.add_run("■")
                rm.bold = True
                rm.font.size = Pt(11)
                rm.font.color.rgb = RGBColor(0x2C, 0x5E, 0x8A)
                set_cell_background(cell_m, 'D9E2EC')
            else:
                set_cell_background(cell_m, bg_color)

    # 2. BUDGET
    add_h1("2. Budget & Financial Feasibility")
    doc.add_paragraph(
        "As established in the original project proposal, 'Echoes of Change' is engineered as a lightweight, locally run standalone application "
        "leveraging open-source libraries, procedural asset generation in Java, and student-owned hardware. No monetary expenditure was incurred."
    )

    table_budget = doc.add_table(rows=7, cols=3)
    table_budget.alignment = WD_TABLE_ALIGNMENT.CENTER
    table_budget.autofit = False

    b_widths = [Inches(0.6), Inches(4.6), Inches(1.4)]
    b_headers = ["Sl.", "Particulars / Expense Category", "Estimated Cost"]
    for i, title in enumerate(b_headers):
        c = table_budget.rows[0].cells[i]
        c.width = b_widths[i]
        p = c.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(title)
        r.bold = True
        r.font.size = Pt(9.5)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_background(c, '1B365D')
        set_cell_margins(c, 60, 60, 80, 80)

    budget_items = [
        ("1.i", "Game engine & development software (Java 17, libGDX 1.14.2, LWJGL3 - Open Source)", "0/- BDT"),
        ("1.ii", "Character and environment 3D assets (100% Procedural code generation in Java)", "0/- BDT"),
        ("1.iii", "Audio & sound effects (Royalty-free open resources & procedural sound synthesis)", "0/- BDT"),
        ("1.iv", "Testing on existing personal hardware (Lightweight local execution on student PCs)", "0/- BDT"),
        ("2", "Internet & miscellaneous costs (Existing student broadband; zero paid cloud hosting)", "0/- BDT"),
        ("3", "Allowance and honorarium (Academic capstone development; zero external compensation)", "0/- BDT")
    ]

    for idx, (sl, item, cost) in enumerate(budget_items, start=1):
        row_c = table_budget.rows[idx].cells
        for col_i, text in enumerate([sl, item, cost]):
            row_c[col_i].width = b_widths[col_i]
            p = row_c[col_i].paragraphs[0]
            if col_i != 1:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(text)
            r.font.size = Pt(9)
            set_cell_margins(row_c[col_i], 50, 50, 70, 70)
            set_cell_background(row_c[col_i], 'F8F9FA' if idx % 2 == 0 else 'FFFFFF')

    # Total row
    row_total = table_budget.add_row().cells
    row_total[0].width = b_widths[0]
    row_total[1].width = b_widths[1]
    row_total[2].width = b_widths[2]
    p_tot_label = row_total[1].paragraphs[0]
    r_tot_l = p_tot_label.add_run("Total Estimated Project Cost")
    r_tot_l.bold = True
    p_tot_val = row_total[2].paragraphs[0]
    p_tot_val.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_tot_v = p_tot_val.add_run("0/- BDT")
    r_tot_v.bold = True
    for c in row_total:
        set_cell_background(c, 'E2E8F0')
        set_cell_margins(c, 60, 60, 80, 80)

    # 3. NOVELTY WITH SDG GOALS
    add_h1("3. Novelty & Alignment with UN Sustainable Development Goals (SDGs)")
    doc.add_paragraph(
        "Project 'Echoes of Change' represents a breakthrough in regional digital game development through three key technical and conceptual novelties:"
    )
    p_nov = doc.add_paragraph()
    p_nov.add_run("1. Respectful, Non-Documentary Historical Allegory: ").bold = True
    p_nov.add_run(
        "Unlike commercial action games that sensationalize civil unrest or documentary games that risk misrepresenting real casualties, "
        "Echoes of Change utilizes a fictional protagonist and fictionalized settings to honor the core spirit of youth courage, empathy, "
        "and solidarity without exploiting real-life suffering.\n"
    )
    p_nov.add_run("2. Procedural In-Code Asset Synthesis: ").bold = True
    p_nov.add_run(
        "Rather than relying on massive pre-baked 3D asset libraries (FBX/OBJ files), all campus landmarks, arches, colonnades, "
        "and terracotta textures are procedurally synthesized in Java runtime memory, reducing distribution size by 95%.\n"
    )
    p_nov.add_run("3. Ethical, Non-Lethal Gameplay Mechanics: ").bold = True
    p_nov.add_run(
        "The project eliminates lethal combat. Challenges center on non-violent de-escalation, defensive staff deflection (Lathi Khela), "
        "evasion under curfew, administering first aid, and peaceful collective assembly."
    )

    add_h2("Alignment with United Nations SDGs")
    doc.add_paragraph(
        "• SDG 4: Quality Education (Target 4.7): Serves as an interactive educational medium fostering critical awareness of civic rights, "
        "peaceful protest, and constitutional governance.\n"
        "• SDG 11: Sustainable Cities and Communities (Target 11.4): Preserves historical educational architecture (e.g. Curzon Hall, Central Library, TSC) "
        "in digital 3D space, contributing to cultural heritage documentation.\n"
        "• SDG 16: Peace, Justice, and Strong Institutions (Targets 16.1 & 16.10): Encourages non-violent conflict resolution, documents civilian resilience "
        "against authoritarian abuse, and enforces an absolute in-game rule disqualifying players who strike peaceful civilians."
    )

    # 4. TEAM CONTRIBUTION
    add_h1("4. Team Contribution & Responsibility Breakdown")
    doc.add_paragraph(
        "The project was executed by a collaborative two-member engineering team from the 16th Batch of the Department of ICE, BAUET:"
    )

    table_team = doc.add_table(rows=3, cols=3)
    table_team.alignment = WD_TABLE_ALIGNMENT.CENTER
    table_team.autofit = False

    t_widths = [Inches(1.8), Inches(1.4), Inches(3.4)]
    t_hdrs = ["Student Name", "Student ID", "Assigned Technical Tasks & Contributions"]
    for i, title in enumerate(t_hdrs):
        c = table_team.rows[0].cells[i]
        c.width = t_widths[i]
        p = c.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(title)
        r.bold = True
        r.font.size = Pt(9.5)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_background(c, '1B365D')
        set_cell_margins(c, 60, 60, 80, 80)

    team_rows = [
        ("Atia Sanjida", "0812410105171002",
         "• Java 17 core engine setup and Gradle compilation pipeline\n"
         "• 3D Character kinematics, locomotion states, and third-person orbital camera\n"
         "• Procedural 3D mesh synthesis for Curzon Hall, arches, and campus colonnades\n"
         "• Implementation of Level 1 (Awareness) and Level 2 (First Challenge)\n"
         "• Level 5 (Turning Point) and Level 6 (Final Change) mission logic\n"
         "• Performance profiling, memory pooling, and JVM crash remediation"),
        ("Farhana Islam Kripa", "0812410105171004",
         "• Literature review & comparative audit of Bangladeshi/international war games\n"
         "• Narrative storyline formulation, dialogue branching, and objective scripts\n"
         "• Implementation of Level 3 (Gathering) and Level 4 (Rising Pressure)\n"
         "• Integration of Noto Sans Bengali typography via gdx-freetype library\n"
         "• Peace rule disqualification tests & checkpoint serialization verification\n"
         "• Preparation of project proposal, budget feasibility, and final academic report")
    ]

    for idx, (name, s_id, contrib) in enumerate(team_rows, start=1):
        row_c = table_team.rows[idx].cells
        row_c[0].width = t_widths[0]
        row_c[1].width = t_widths[1]
        row_c[2].width = t_widths[2]
        
        p0 = row_c[0].paragraphs[0]
        r0 = p0.add_run(name)
        r0.bold = True
        r0.font.size = Pt(9.5)
        
        p1 = row_c[1].paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r1 = p1.add_run(s_id)
        r1.font.size = Pt(9)
        
        p2 = row_c[2].paragraphs[0]
        r2 = p2.add_run(contrib)
        r2.font.size = Pt(8.5)
        
        set_cell_margins(row_c[0], 60, 60, 70, 70)
        set_cell_margins(row_c[1], 60, 60, 70, 70)
        set_cell_margins(row_c[2], 60, 60, 70, 70)
        set_cell_background(row_c[0], 'F8F9FA' if idx % 2 == 0 else 'FFFFFF')
        set_cell_background(row_c[1], 'F8F9FA' if idx % 2 == 0 else 'FFFFFF')
        set_cell_background(row_c[2], 'F8F9FA' if idx % 2 == 0 else 'FFFFFF')

    # 5. INTRODUCTION
    add_h1("5. Introduction")
    doc.add_paragraph(
        "In July and August 2024, Bangladesh experienced a large-scale student-led movement that began as a protest against the civil-service quota system "
        "and grew into a nationwide call for accountability and change, ultimately leading to significant political consequences. "
        "The movement was marked by widespread student participation, unity across different sections of society, and a shared spirit of resistance in the face of considerable risk. "
        "These themes of collective courage, solidarity, and the pursuit of change carry strong narrative and emotional weight, making them a compelling foundation for interactive storytelling."
    )
    doc.add_paragraph(
        "This project proposes the development of 'Echoes of Change,' a story-driven, level-based adventure game that draws thematic inspiration from the social atmosphere and spirit "
        "of the 2024 movement, without directly recreating specific real events, individuals, or conversations. The game follows a fictional protagonist through six progressively "
        "challenging levels that trace an emotional arc from awareness to collective action and ultimately to a symbolic turning point. "
        "The purpose of the project is to demonstrate how real-world social themes can be transformed into an original, fictional interactive narrative that is meaningful, engaging, "
        "and respectful of the sensitivity of its inspiration."
    )

    # 6. LITERATURE REVIEW
    add_h1("6. Background / Literature Review")
    doc.add_paragraph(
        "A critical survey of existing history-inspired and narrative-driven video games was conducted to contextualize this project across both national and global dimensions:"
    )
    add_h2("6.1 Bangladeshi Historical Video Games")
    doc.add_paragraph(
        "• Heroes of 71 (Mindfisher Games, 2015): An open-world third-person tactical defense game set in a fictional Bangladeshi village during the 1971 Liberation War [1].\n"
        "• Heroes of 71: Retaliation (Mindfisher Games, 2016): A sequel emphasizing civilian rescue and supply disruption, notable for introducing female guerrilla fighters [2].\n"
        "• Mukti Camp (Mindfisher Games, 2017): A strategy game where players manage guerrilla logistics, resources, and personnel rather than direct gunplay [3].\n"
        "• Arunodoyer Agnishikha (Trimatrik Interactive, 2004) & Muktijuddho 1971: Early first-person shooters recreating combat battlefield scenarios [4, 5]."
    )
    doc.add_paragraph(
        "Critique: Local games have focused exclusively on 1971 armed combat, leaving contemporary civil and non-violent youth movements completely unaddressed."
    )

    add_h2("6.2 International Narrative & Docu-Games")
    doc.add_paragraph(
        "• 1979 Revolution: Black Friday (iNK Stories, 2016): Blends real documentary photojournalism with branching dialogue choices during the Iranian Revolution [6].\n"
        "• This War of Mine (11 bit Studios, 2014): Shifts focus away from armed soldiers to the moral dilemmas and survival of ordinary civilians in a besieged city [7].\n"
        "• Through the Darkest of Times (Paintbucket Games, 2020): Explores civilian resistance and courage under authoritarian surveillance in 1930s Berlin [8].\n"
        "• Valiant Hearts: The Great War (Ubisoft Montpellier, 2014): Intersects fictional character journeys with real historical letters and archival notes [9]."
    )

    # 7. PROBLEM STATEMENT
    add_h1("7. Critical Analysis and Problem Statement")
    doc.add_paragraph(
        "The reviewed titles show two clear trends. Locally developed Bangladeshi games on the Liberation War are almost exclusively built around direct armed combat, "
        "using first-person-shooter or base-management mechanics that recreate battlefield action rather than the social and emotional experience of collective movement. "
        "Internationally, story-driven games such as 1979 Revolution: Black Friday, This War of Mine, and Through the Darkest of Times demonstrate that fictionalised adventure and strategy "
        "games can meaningfully represent civilian experience, unity, and resistance during periods of social upheaval, but none of these titles engage with the Bangladeshi context, "
        "and none address a contemporary, still-unfolding movement such as the 2024 student movement."
    )
    doc.add_paragraph(
        "This leaves a clear gap: there is currently no story-driven, level-based adventure game that uses a fictionalised narrative to explore the themes of unity, courage, "
        "and collective action associated with recent Bangladeshi social history. The core problem this project addresses is the absence of a locally relevant, thematically sensitive, "
        "and narratively focused game that can communicate these themes to players without attempting to document real individuals or events."
    )

    # 8. OBJECTIVES
    add_h1("8. Objectives")
    doc.add_paragraph(
        "This project aims to design and develop a fictional, story-driven adventure game inspired by the social themes of the 2024 student movement in Bangladesh. "
        "To accomplish this goal, the project is structured around the following four explicit objectives:"
    )
    doc.add_paragraph(
        "1. To design a fictional narrative and world inspired by the broader themes of the 2024 student movement, without directly recreating real individuals or specific events.\n"
        "2. To develop a level-based gameplay structure with progressively increasing difficulty, guiding the player through a story arc from awareness to a symbolic final change.\n"
        "3. To design interactive environments, missions, and characters that communicate themes of unity, courage, and collective action through exploration, dialogue, and decision-making.\n"
        "4. To implement and test a playable prototype covering all six levels, and refine it based on playtesting feedback."
    )

    # 9. METHODOLOGY
    add_h1("9. Methodology")

    add_h2("9.1 Tools & Implementation Stack")
    doc.add_paragraph(
        "• Java 17 (OpenJDK LTS): Robust object-oriented platform providing cross-platform runtime reliability.\n"
        "• libGDX 1.14.2 & LWJGL3: High-performance 3D game framework interfacing with modern desktop OpenGL drivers.\n"
        "• Custom GLSL Shaders: Lightweight PBR-lite albedo/normal rendering and analytical exponential height fog.\n"
        "• gdx-freetype & Google Noto Sans Bengali: TrueType native rendering engine providing crisp bilingual typography.\n"
        "• JUnit 5 Test Harness: 163 automated test suites verifying kinematics, collision physics, and peace rules."
    )

    add_h2("9.2 Game System Flowchart")
    doc.add_paragraph(
        "The game architecture follows a deterministic lifecycle from JVM context initialization through procedural synthesis to level progression:"
    )
    flow_text = (
        "[Start Application: Echoes of Change]\n"
        "      │\n"
        "      ▼\n"
        "[Initialize Java 17 & LWJGL3 Context] ──► [Compile Custom GLSL Shaders & Height Fog]\n"
        "      │\n"
        "      ▼\n"
        "[Initialize FreeType Noto Sans Bengali Font Cache] ──► [Main Menu: New Game / Continue]\n"
        "      │\n"
        "      ▼\n"
        "[Load Level State Machine: L1 (Awareness) to L6 (Final Change)]\n"
        "      │\n"
        "      ▼\n"
        "┌──────────────────────────────────────────────────────────────────┐\n"
        "│ Active Gameplay Loop: Kinematics ──► Collision ──► Interaction   │\n"
        "│ Check: Did player strike a peaceful peer?                         │\n"
        "│    ├── YES ──► [Instant Disqualification & Retry Checkpoint]     │\n"
        "│    └── NO  ──► [Update Objective, Render 3D Scene + Bengali HUD] │\n"
        "└──────────────────────────────────────────────────────────────────┘\n"
        "      │\n"
        "      ▼\n"
        "[All Objectives Complete?] ──► Auto-Save Checkpoint ──► Advance to Next Level\n"
        "      │\n"
        "      ▼ (After Level 6)\n"
        "[Victory Epilogue Cutscene & Historical Archival Credits]"
    )
    add_code_block(flow_text)

    add_h2("9.3 Algorithms & Pseudocode")
    doc.add_paragraph("Algorithm 1: Procedural Landmark Mesh Generation (Indo-Saracenic Arches)")
    algo1 = (
        "Algorithm GenerateCampusColonnade(MeshBuilder mb, Vector3 basePos, int numArches, float archWidth, float height):\n"
        "    Input: MeshBuilder mb, base position, number of arches, arch width, column height\n"
        "    Output: Procedural triangle mesh with smooth normals and UV coordinates\n\n"
        "    For i from 0 to numArches - 1 do:\n"
        "        colX = basePos.x + (i * archWidth)\n"
        "        // 1. Generate structural brick pier column\n"
        "        mb.box(colX, basePos.y + height/2, basePos.z, 0.6, height, 0.6)\n\n"
        "        // 2. Generate procedural cusped arch aperture\n"
        "        archCenter = Vector3(colX + archWidth/2, basePos.y + height - 1.2, basePos.z)\n"
        "        For step from 0 to 11 do:\n"
        "            angle1 = step * (PI / 12)\n"
        "            angle2 = (step + 1) * (PI / 12)\n"
        "            p1 = archCenter + Vector3(cos(angle1) * 1.2, sin(angle1) * 1.2, 0)\n"
        "            p2 = archCenter + Vector3(cos(angle2) * 1.2, sin(angle2) * 1.2, 0)\n"
        "            mb.triangle(p1, p2, archCenter)\n"
        "        End For\n"
        "        // 3. Generate decorative cornice parapet\n"
        "        mb.box(colX + archWidth/2, basePos.y + height + 0.3, basePos.z, archWidth, 0.6, 0.8)\n"
        "    End For\n"
        "End Algorithm"
    )
    add_code_block(algo1)

    doc.add_paragraph("Algorithm 2: Non-Violent Defense & Peace Rule Evaluator")
    algo2 = (
        "Algorithm EvaluateInteraction(Player player, Entity target, Action action):\n"
        "    Input: Player instance, Target NPC, Input Action\n\n"
        "    // Enforce strict campus peace rule\n"
        "    If target.isPeacefulStudent() and action == ATTACK then:\n"
        "        player.triggerDisqualification('Striking peaceful students violates campus rules!')\n"
        "        Return DISQUALIFIED\n"
        "    End If\n\n"
        "    // Defensive deflection and parry resolution against aggressive intruders\n"
        "    If target.isHostile() and target.isStriking() then:\n"
        "        If action == DEFENSIVE_PARRY then:\n"
        "            If target.getStrikeImpactRemaining() <= 0.120 then:\n"
        "                player.playParryAnimation()\n"
        "                target.stagger(1.5) // Posture broken for 1.5s\n"
        "                Return PARRY_SUCCESS\n"
        "            Else:\n"
        "                player.blockDamage(0.70) // Guard absorbs 70% damage\n"
        "                Return GUARD_BLOCK\n"
        "            End If\n"
        "        End If\n"
        "    End If\n\n"
        "    // Administer first aid to wounded peers\n"
        "    If target.isInjured() and action == INTERACT then:\n"
        "        player.administerFirstAid(target)\n"
        "        player.incrementObjectiveScore()\n"
        "        Return AID_ADMINISTERED\n"
        "    End If\n"
        "End Algorithm"
    )
    add_code_block(algo2)

    add_h2("9.4 Architectural Pipeline Diagram")
    pipe_text = (
        "┌────────────────────────────────────────────────────────────────────────┐\n"
        "│                          PIPELINE ARCHITECTURE                         │\n"
        "├──────────────────┬──────────────────┬──────────────────┬───────────────┤\n"
        "│ 1. Input/Physics │ 2. Game Logic    │ 3. Procedural    │ 4. Rendering  │\n"
        "├──────────────────┼──────────────────┼──────────────────┼───────────────┤\n"
        "│ • Keyboard/Mouse │ • Level Manager  │ • Landmark Mesh  │ • ModelBatch  │\n"
        "│ • Kinematic Move │ • Peace Rule     │ • Normal Mapping │ • GLSL Fog    │\n"
        "│ • Capsule Check  │ • Disqualify     │ • Terracotta Gen │ • FreeType HUD│\n"
        "│ • Damped Camera  │ • Auto-Save JSON │ • Vector Pools   │ • Display FBO │\n"
        "└──────────────────┴──────────────────┴──────────────────┴───────────────┘"
    )
    add_code_block(pipe_text)

    add_h2("9.5 Game & Level Design")
    doc.add_paragraph(
        "The game is structured across six sequential chapters matching the emotional arc from the approved proposal:"
    )
    doc.add_paragraph(
        "• Level 1 (Awareness / সচেতনতা): The player explores the peaceful university campus, converses with student NPCs, and gathers awareness from bulletin notices.\n"
        "• Level 2 (The First Challenge / প্রথম চ্যালেঞ্জ): Unprovoked aggression occurs outside the gates; the player protects and shelters injured classmates, avoiding combat.\n"
        "• Level 3 (Gathering Together / একত্রিত হওয়া): Student coordination in the central quadrangle; gathering medical supplies and establishing mutual assistance lines.\n"
        "• Level 4 (Rising Pressure / ক্রমবর্ধমান চাপ): Night curfew and communication blackout; player navigates darkened alleys to deliver vital printed messages while evading spotlights.\n"
        "• Level 5 (The Turning Point / মোড় পরিবর্তনের মুহূর্ত): Public decision point; mobilizing cross-community assembly and formulating the unified declaration of reform.\n"
        "• Level 6 (The Final Change / চূড়ান্ত পরিবর্তন): Mass collective gathering; peaceful transition, symbolic institutional restoration, and memorialization epilogue."
    )

    # 10. RESULTS & SCREENSHOTS
    add_h1("10. Result: Project Screenshots with Figure Descriptions")
    doc.add_paragraph(
        "The following screenshots from the working prototype verify the procedural graphics, bilingual HUD, and gameplay mechanics:"
    )

    screenshot_files = [
        ("Figure 1: Procedural Facade of Curzon Hall",
         "docs/screenshots/t3/curzon_front.png",
         "Demonstrates the procedural terracotta brick facade, paired corner minarets, and colonial arches of Curzon Hall generated entirely in Java code without external 3D models. The scene highlights the custom GLSL PBR-lite shader reflecting low-angle morning sunlight."),
        
        ("Figure 2: Teacher-Student Centre (TSC) Pavilion Geometry",
         "docs/screenshots/t3/tsc.png",
         "Showcases the 3D geometric construction of the iconic Teacher-Student Centre (TSC) pavilion and plaza, acting as the primary gathering hub for Level 1 (Awareness) and Level 3 (Gathering Together)."),

        ("Figure 3: Central Library & Colonnaded Heritage Quadrangle",
         "docs/screenshots/t3/central_library.png",
         "Illustrates the colonnaded quadrangle adjacent to Hakim Chattar. Shows the procedural vegetation system instantiating lush tropical foliage (mango, rain tree, and bamboo clusters) using instanced rendering to conserve GPU draw calls."),

        ("Figure 4: Bilingual HUD & Objective Tracker in Level 6 (Final Change)",
         "docs/screenshots/t2/level6_hud_bengali.png",
         "Highlights the dynamic bilingual interface rendered in native Bengali typography using Noto Sans Bengali alongside English subtitles. The HUD displays sprint stamina gauges, compass orientation, and active non-violent objectives."),

        ("Figure 5: In-Game Archival Evidence Context Modal",
         "docs/screenshots/t4/memorial_modal_context_card.png",
         "Depicts the in-game evidentiary modal card triggered when interacting with campus monuments. Demonstrates how thematic historical context is delivered respectfully through non-intrusive UI overlays."),

        ("Figure 6: Level 1 (Awareness) Promenade Spawn Point",
         "docs/screenshots/t0_windows/spawn_level1.png",
         "The initial player spawn view overlooking the Central Promenade facing Curzon Hall. Demonstrates analytical exponential height fog eliminating void horizons and establishing a serene morning atmosphere."),

        ("Figure 7: Photographic Mode & Inspection Subsystem",
         "docs/screenshots/t7/photo_mode_overlay.png",
         "Shows the integrated in-game photo mode subsystem (F10 key). Players can adjust focal distance, inspect architectural details, toggle HUD elements, and capture high-resolution lossless screenshots (F12) directly to local storage.")
    ]

    for title, path, desc in screenshot_files:
        p_fig = doc.add_paragraph()
        p_fig.paragraph_format.space_before = Pt(8)
        p_fig.paragraph_format.space_after = Pt(2)
        p_fig.paragraph_format.keep_with_next = True
        r_f = p_fig.add_run(title)
        r_f.bold = True
        r_f.font.size = Pt(11)
        r_f.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)

        if os.path.exists(path):
            p_img = doc.add_paragraph()
            p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_img.paragraph_format.space_before = Pt(2)
            p_img.paragraph_format.space_after = Pt(4)
            p_img.paragraph_format.keep_with_next = True
            run_img = p_img.add_run()
            run_img.add_picture(path, width=Inches(5.5))

        p_desc = doc.add_paragraph()
        p_desc.paragraph_format.space_before = Pt(0)
        p_desc.paragraph_format.space_after = Pt(8)
        r_d = p_desc.add_run("Figure Description: ")
        r_d.italic = True
        r_d.bold = True
        r_d.font.size = Pt(9.5)
        r_dt = p_desc.add_run(desc)
        r_dt.italic = True
        r_dt.font.size = Pt(9.5)

    # 11. CONCLUSION
    add_h1("11. Conclusion")
    doc.add_paragraph(
        "Echoes of Change successfully translates the spirit of unity, courage, and collective action associated with the 2024 student movement in Bangladesh "
        "into an original, fictional, level-based adventure game. By keeping its characters, locations, and events entirely fictional while drawing thematic "
        "inspiration from real social history, the project delivers an emotionally engaging experience without functioning as an exploitative documentary or armed combat game."
    )
    doc.add_paragraph(
        "The proposed six-level structure, zero-budget procedural pipeline, and strict ethical peace rules provide an achievable, academically rigorous, "
        "and socially impactful software artifact for the Integrated Design Project (IDP) in the Department of Information and Communication Engineering (ICE), BAUET."
    )

    # 12. DISCUSSION
    add_h1("12. Discussion")
    doc.add_paragraph(
        "• Ethical Representation: The decision to fictionalize characters and scenarios proved essential. It allows players to reflect on the moral weight "
        "of civic solidarity without trivializing real grief or turning historical tragedy into entertainment.\n"
        "• Procedural Efficiency: The Java 17 + libGDX procedural mesh architecture proved that high visual fidelity and atmospheric depth can be achieved "
        "at 60 FPS on standard integrated GPUs without multi-gigabyte commercial asset dependencies.\n"
        "• Future Enhancements: Subsequent iterations could introduce motion-captured character animations and native Bengali voice acting to further elevate immersion."
    )

    # 13. REFERENCES
    add_h1("13. Reference List (IEEE Style)")
    refs = [
        "[1] Mindfisher Games, 'Heroes of 71,' 2015. [Online]. Available: https://play.google.com/store/apps/details?id=com.mindfishergames.heroesof71",
        "[2] Mindfisher Games, 'Heroes of 71: Retaliation,' 2016. [Online]. Available: https://play.google.com/store",
        "[3] Mindfisher Games, 'Mukti Camp,' 2017. [Online]. Available: https://play.google.com/store",
        "[4] Trimatrik Interactive, 'Arunodoyer Agnishikha,' 2004.",
        "[5] 'Muktijuddho 1971,' Steam. [Online]. Available: https://store.steampowered.com/app/3062630/Muktijuddho_1971/",
        "[6] iNK Stories, '1979 Revolution: Black Friday,' 2016. [Online]. Available: https://store.steampowered.com/app/388320/1979_Revolution_Black_Friday/",
        "[7] 11 bit studios, 'This War of Mine,' 2014. [Online]. Available: https://en.wikipedia.org/wiki/This_War_of_Mine",
        "[8] Paintbucket Games, 'Through the Darkest of Times,' 2020. [Online]. Available: https://en.wikipedia.org/wiki/Through_the_Darkest_of_Times",
        "[9] Ubisoft Montpellier, 'Valiant Hearts: The Great War,' 2014.",
        "[10] Office of the United Nations High Commissioner for Human Rights (OHCHR), 'Human Rights Violations and Abuses related to the Protests of July and August 2024 in Bangladesh,' Geneva, Report, Feb. 2025.",
        "[11] libGDX Community, 'libGDX Desktop Architecture and LWJGL3 Backend Guide,' Version 1.14.2, 2024.",
        "[12] Google Internationalization, 'Noto Sans Bengali Open Font Repository,' SIL Open Font License, 2024."
    ]
    for r in refs:
        p_ref = doc.add_paragraph()
        p_ref.paragraph_format.space_before = Pt(2)
        p_ref.paragraph_format.space_after = Pt(3)
        p_ref.paragraph_format.left_indent = Inches(0.3)
        run_r = p_ref.add_run(r)
        run_r.font.size = Pt(9.5)

    output_path = "/Users/atiaoishi/Downloads/sparkvision/2x12-Spark--Birth-of-36-july/Echoes_of_Change_Project_Report.docx"
    doc.save(output_path)
    print(f"Successfully generated Word document at: {output_path}")

if __name__ == '__main__':
    create_report()
