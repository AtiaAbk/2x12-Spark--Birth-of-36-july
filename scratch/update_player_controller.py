with open('core/src/main/java/bd/spark36/character/PlayerController.java', 'r') as f:
    code = f.read()

# 1. Add fields for crouch, combat combo, and sitting water
old_anim_fields = """    // Animation & State
    private boolean isMoving = false;
    private boolean isSprinting = false;
    private float walkCycle = 0f;"""

new_anim_fields = """    // Animation & State
    private boolean isMoving = false;
    private boolean isSprinting = false;
    private float walkCycle = 0f;
    private boolean isCrouching = false;
    private boolean isAttacking = false;
    private int attackCombo = 0;
    private float attackProgress = 0f;
    private float comboWindowTimer = 0f;
    private boolean isSittingWater = false;
    private float sitProgress = 0f;"""

assert old_anim_fields in code, "old_anim_fields not found"
code = code.replace(old_anim_fields, new_anim_fields, 1)

# 2. Update input handling
old_input = """            // JUMP / DOUBLE JUMP: 'J' (Primary key, replacing SPACE completely as requested!)
            // Also supports C, V, or Right-Click as alternative gamer inputs
            jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.J) ||
                      Gdx.input.isKeyJustPressed(Input.Keys.C) ||
                      Gdx.input.isKeyJustPressed(Input.Keys.V) ||
                      Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);"""

new_input = """            // CROUCH / DUCK: 'C' key toggles crouch
            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                isCrouching = !isCrouching;
            }

            // COMBAT STRIKES: 'F' key or Left-Click (3-hit student protest defense combo!)
            boolean attackPressed = Gdx.input.isKeyJustPressed(Input.Keys.F) ||
                                    (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !isSittingWater);
            if (attackPressed && !isAttacking && !isSittingWater) {
                isAttacking = true;
                attackProgress = 0f;
                if (comboWindowTimer > 0f) {
                    attackCombo = (attackCombo % 3) + 1;
                } else {
                    attackCombo = 1;
                }
            }

            // JUMP / DOUBLE JUMP: 'J' or 'SPACE' or 'V' or Right-Click
            jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.J) ||
                      Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                      Gdx.input.isKeyJustPressed(Input.Keys.V) ||
                      Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);"""

assert old_input in code, "old_input not found"
code = code.replace(old_input, new_input, 1)

# 3. Update speed calculation and sitting / attacking progression
old_speed = """        // 3. Movement direction relative to camera angle
        float currentSpeed = isSprinting ? SPRINT_SPEED : WALK_SPEED;"""

new_speed = """        // Handle combat attack timing
        if (isAttacking) {
            attackProgress += delta * 3.2f;
            if (attackProgress >= 1.0f) {
                isAttacking = false;
                attackProgress = 1.0f;
                comboWindowTimer = 0.65f;
            }
        } else if (comboWindowTimer > 0f) {
            comboWindowTimer -= delta;
            if (comboWindowTimer <= 0f) {
                attackCombo = 0;
            }
        }

        // Handle water sitting state
        if (isSittingWater) {
            sitProgress = Math.min(1.0f, sitProgress + delta * 3.5f);
            velocity.set(0f, 0f, 0f);
            boolean breakSit = (inputEnabled && (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.S) ||
                                                 Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D) ||
                                                 jumpKey || isAttacking));
            if (breakSit) {
                isSittingWater = false;
            }
        } else {
            sitProgress = Math.max(0f, sitProgress - delta * 4.0f);
        }

        // 3. Movement direction relative to camera angle
        float currentSpeed = isCrouching ? (isSprinting ? 2.6f : 1.8f) : (isSprinting ? SPRINT_SPEED : WALK_SPEED);
        if (isSittingWater) currentSpeed = 0f;"""

assert old_speed in code, "old_speed not found"
code = code.replace(old_speed, new_speed, 1)

# 4. Add getters and setters at end of class
getters = """    public boolean isCrouching() {
        return isCrouching;
    }

    public void setCrouching(boolean crouching) {
        this.isCrouching = crouching;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public int getAttackCombo() {
        return attackCombo;
    }

    public float getAttackProgress() {
        return attackProgress;
    }

    public boolean isSittingWater() {
        return isSittingWater;
    }

    public float getSitProgress() {
        return sitProgress;
    }

    public void setSittingWater(boolean sitting) {
        this.isSittingWater = sitting;
        if (sitting) {
            this.sitProgress = 1f;
            this.velocity.set(0f, 0f, 0f);
        }
    }
}
"""

last_brace = code.rfind('}')
code = code[:last_brace] + getters

with open('core/src/main/java/bd/spark36/character/PlayerController.java', 'w') as f:
    f.write(code)

print("PlayerController.java updated successfully!")
