with open('core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java', 'r') as f:
    code = f.read()

old_meth = """    public boolean areParametersVisible() {
        return parametersVisible;
    }"""

new_meth = """    public boolean areParametersVisible() {
        return parametersVisible;
    }

    public void setParametersVisible(boolean visible) {
        this.parametersVisible = visible;
    }"""

assert old_meth in code, "old_meth not found"
code = code.replace(old_meth, new_meth, 1)

with open('core/src/main/java/bd/spark36/hud/WhereWindsMeetHUD.java', 'w') as f:
    f.write(code)

print("setParametersVisible added!")
