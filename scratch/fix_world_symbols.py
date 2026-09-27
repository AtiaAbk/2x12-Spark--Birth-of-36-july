with open('core/src/main/java/bd/spark36/world/DhakaCampusWorld.java', 'r') as f:
    code = f.read()

# Replace terracottaBrick with curzonBrickMat
code = code.replace("Model ghatStepMid = mb.createBox(6.6f, 0.20f, 1.2f, terracottaBrick, attr);",
                    "Model ghatStepMid = mb.createBox(6.6f, 0.20f, 1.2f, curzonBrickMat, attr);")

# Update buildPondFishes call to pass attr
code = code.replace("buildPondFishes(mb, pukurX, pukurZ, pukurW, pukurL);",
                    "buildPondFishes(mb, pukurX, pukurZ, pukurW, pukurL, attr);")

# Update buildPondFishes signature
code = code.replace("private void buildPondFishes(ModelBuilder mb, float cx, float cz, float pw, float pl) {",
                    "private void buildPondFishes(ModelBuilder mb, float cx, float cz, float pw, float pl, long attr) {")

with open('core/src/main/java/bd/spark36/world/DhakaCampusWorld.java', 'w') as f:
    f.write(code)

print("DhakaCampusWorld.java symbols fixed!")
