with open('core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java', 'r') as f:
    code = f.read()

# 1. Add parametersVisible field
old_field = """    private boolean isMapOpen = false;"""

new_field = """    private boolean isMapOpen = false;
    private boolean parametersVisible = true;

    public void toggleParameters() {
        parametersVisible = !parametersVisible;
    }

    public boolean areParametersVisible() {
        return parametersVisible;
    }"""

assert old_field in code, "old_field not found"
code = code.replace(old_field, new_field, 1)

# 2. Add keypress check near Keys.M
old_render_start = """        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {"""

new_render_start = """        if ((Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.H)) && !isVictoryOpen && !isPauseMenuOpen && activeModalEntry == null) {
            parametersVisible = !parametersVisible;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {"""

assert old_render_start in code, "old_render_start not found"
code = code.replace(old_render_start, new_render_start, 1)

# 3. Check parametersVisible before drawing checklist
old_draw_bg = """        drawMissionCardBg(memorials, w, h);
        drawMissionChecklistBg(player, memorials, w, h);"""

new_draw_bg = """        drawMissionCardBg(memorials, w, h);
        if (parametersVisible) {
            drawMissionChecklistBg(player, memorials, w, h);
        }"""

assert old_draw_bg in code, "old_draw_bg not found"
code = code.replace(old_draw_bg, new_draw_bg, 1)

old_draw_borders = """        drawMissionCardBorders(w, h);
        drawMissionChecklistBorders(player, memorials, w, h);"""

new_draw_borders = """        drawMissionCardBorders(w, h);
        if (parametersVisible) {
            drawMissionChecklistBorders(player, memorials, w, h);
        }"""

assert old_draw_borders in code, "old_draw_borders not found"
code = code.replace(old_draw_borders, new_draw_borders, 1)

old_draw_text = """        drawMissionCardText(memorials, nearest, dstToNearest, w, h);
        drawMissionChecklistText(player, memorials, w, h);"""

new_draw_text = """        drawMissionCardText(memorials, nearest, dstToNearest, w, h);
        if (parametersVisible) {
            drawMissionChecklistText(player, memorials, w, h);
        } else {
            drawParametersHiddenPrompt(w, h);
        }"""

assert old_draw_text in code, "old_draw_text not found"
code = code.replace(old_draw_text, new_draw_text, 1)

# 4. Add prompt on checklist header
old_chk_header = """        // 1. Header: "* MISSION PARAMETERS: 01 / 05"
        String headerTitle = String.format("* MISSION PARAMETERS: %02d / %02d", ins, tot);
        fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.keyFont.draw(spriteBatch, headerTitle, bx + 17f, by + bh - 11f);
        fonts.keyFont.setColor(ins >= tot ? Color.GREEN : goldAccent);
        fonts.keyFont.draw(spriteBatch, headerTitle, bx + 16f, by + bh - 10f);"""

new_chk_header = """        // 1. Header: "* MISSION PARAMETERS: 01 / 05" + [P] HIDE hint
        String headerTitle = String.format("* MISSION PARAMETERS: %02d / %02d", ins, tot);
        fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.keyFont.draw(spriteBatch, headerTitle, bx + 17f, by + bh - 11f);
        fonts.keyFont.setColor(ins >= tot ? Color.GREEN : goldAccent);
        fonts.keyFont.draw(spriteBatch, headerTitle, bx + 16f, by + bh - 10f);

        fonts.smallFont.setColor(0f, 0f, 0f, 0.85f);
        fonts.smallFont.draw(spriteBatch, "[P] HIDE", bx + bw - 74f, by + bh - 11f);
        fonts.smallFont.setColor(goldMuted);
        fonts.smallFont.draw(spriteBatch, "[P] HIDE", bx + bw - 75f, by + bh - 10f);"""

assert old_chk_header in code, "old_chk_header not found"
code = code.replace(old_chk_header, new_chk_header, 1)

# Add drawParametersHiddenPrompt method and extra controls hints
extra_helpers = """
    private void drawParametersHiddenPrompt(float w, float h) {
        float bx = 36f;
        float by = 36f;
        fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.keyFont.draw(spriteBatch, "[P] SHOW MISSION PARAMETERS", bx + 1f, by + 19f);
        fonts.keyFont.setColor(goldAccent);
        fonts.keyFont.draw(spriteBatch, "[P] SHOW MISSION PARAMETERS", bx, by + 20f);
    }
"""

old_keycaps_text = """        fonts.smallFont.draw(spriteBatch, "MAP", kBaseX + 75f, kBaseY + 15f);
        fonts.smallFont.draw(spriteBatch, "PAUSE", kBaseX + 156f, kBaseY + 15f);
    }"""

new_keycaps_text = """        fonts.smallFont.draw(spriteBatch, "MAP", kBaseX + 75f, kBaseY + 15f);
        fonts.smallFont.draw(spriteBatch, "PAUSE", kBaseX + 156f, kBaseY + 15f);

        // Action Hints: Crouch, Attack, and Hide Parameters
        fonts.smallFont.setColor(0f, 0f, 0f, 0.85f);
        fonts.smallFont.draw(spriteBatch, "[C] CROUCH  |  [F] ATTACK  |  [P] PARAMETERS", kBaseX - 119f, kBaseY - 3f);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "[C] CROUCH  |  [F] ATTACK  |  [P] PARAMETERS", kBaseX - 120f, kBaseY - 2f);
    }"""

assert old_keycaps_text in code, "old_keycaps_text not found"
code = code.replace(old_keycaps_text, new_keycaps_text, 1)

last_brace = code.rfind('}')
code = code[:last_brace] + extra_helpers + "\n}\n"

with open('core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java', 'w') as f:
    f.write(code)

print("WhereWindsMeetHUD.java updated successfully!")
