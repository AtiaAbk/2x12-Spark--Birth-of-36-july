with open('core/src/main/java/bd/spark36/SparkGame.java', 'r') as f:
    code = f.read()

# 1. Update shadow pass studentMesh.render
old_shadow_render = """        studentMesh.render(
            shadowBatch,
            null,
            player.getPosition(),
            player.getHeadingDegrees(),
            player.getWalkCycle(),
            player.isMoving(),
            player.isSprinting()
        );"""

new_shadow_render = """        studentMesh.render(
            shadowBatch,
            null,
            player.getPosition(),
            player.getHeadingDegrees(),
            player.getWalkCycle(),
            player.isMoving(),
            player.isSprinting(),
            player.isCrouching(),
            player.isAttacking(),
            player.getAttackCombo(),
            player.getAttackProgress(),
            player.isSittingWater(),
            player.getSitProgress()
        );"""

assert old_shadow_render in code, "old_shadow_render not found"
code = code.replace(old_shadow_render, new_shadow_render, 1)

# 2. Update 3D pass studentMesh.render
old_model_render = """        studentMesh.render(
            modelBatch,
            world.getEnvironment(),
            player.getPosition(),
            player.getHeadingDegrees(),
            player.getWalkCycle(),
            player.isMoving(),
            player.isSprinting()
        );"""

new_model_render = """        studentMesh.render(
            modelBatch,
            world.getEnvironment(),
            player.getPosition(),
            player.getHeadingDegrees(),
            player.getWalkCycle(),
            player.isMoving(),
            player.isSprinting(),
            player.isCrouching(),
            player.isAttacking(),
            player.getAttackCombo(),
            player.getAttackProgress(),
            player.isSittingWater(),
            player.getSitProgress()
        );"""

assert old_model_render in code, "old_model_render not found"
code = code.replace(old_model_render, new_model_render, 1)

# 3. Add pond ghat interaction in renderGameplay
old_update_logic = """        // 1. Update Game Logic
        boolean canMove = !hud.isModalOpen() && System.getProperty("bd.spark36.testHeroScreenshot") == null;
        player.update(delta, camera.getYaw(), canMove);"""

new_update_logic = """        // 1. Update Game Logic
        boolean canMove = !hud.isModalOpen() && System.getProperty("bd.spark36.testHeroScreenshot") == null;
        if (world.isNearPondGhat(player.getPosition()) && Gdx.input.isKeyJustPressed(Input.Keys.E) && !hud.isModalOpen()) {
            boolean nextSit = !player.isSittingWater();
            player.setSittingWater(nextSit);
            if (nextSit) {
                player.setPosition(0f, 0.18f, 17.2f);
            }
        }
        player.update(delta, camera.getYaw(), canMove);"""

assert old_update_logic in code, "old_update_logic not found"
code = code.replace(old_update_logic, new_update_logic, 1)

# 4. Update testSitScreenshot logic
old_sit_test = """        } else if (System.getProperty("bd.spark36.testSitScreenshot") != null) {
            hud.reset();
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_pond_sitting_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        }"""

new_sit_test = """        } else if (System.getProperty("bd.spark36.testSitScreenshot") != null) {
            hud.reset();
            player.setPosition(0f, 0.18f, 17.2f);
            player.setSittingWater(true);
            testTimer += delta;
            if (!autoScreenshotTaken && testTimer >= 2.0f) {
                takeScreenshot("spark36_pond_sitting_verified");
                autoScreenshotTaken = true;
                if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) {
                    Gdx.app.exit();
                }
            }
        }"""

assert old_sit_test in code, "old_sit_test not found"
code = code.replace(old_sit_test, new_sit_test, 1)

with open('core/src/main/java/bd/spark36/SparkGame.java', 'w') as f:
    f.write(code)

print("SparkGame.java updated successfully!")
