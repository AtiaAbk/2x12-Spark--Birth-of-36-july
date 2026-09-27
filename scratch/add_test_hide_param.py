with open('core/src/main/java/bd/spark36/SparkGame.java', 'r') as f:
    code = f.read()

old_block = """        // Automated visual verification support
        if (System.getProperty("bd.spark36.testHeroScreenshot") != null) {"""

new_block = """        // Automated visual verification support
        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.testHideParameters"))) {
            hud.setParametersVisible(false);
        }

        if (System.getProperty("bd.spark36.testHeroScreenshot") != null) {"""

assert old_block in code, "old_block not found"
code = code.replace(old_block, new_block, 1)

with open('core/src/main/java/bd/spark36/SparkGame.java', 'w') as f:
    f.write(code)

print("testHideParameters support added to SparkGame.java!")
