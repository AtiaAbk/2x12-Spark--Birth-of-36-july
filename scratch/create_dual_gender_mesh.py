with open('core/src/main/java/bd/spark36/character/StudentMesh.java', 'r') as f:
    code = f.read()

# Check imports and package
assert 'package bd.spark36.character;' in code
print("Original StudentMesh read successfully.")
