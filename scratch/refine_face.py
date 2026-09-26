with open('core/src/main/java/bd/spark36/character/StudentMesh.java', 'r') as f:
    code = f.read()

old_brows_fringe = """        // Natural Groomed Eyebrows
        Model browInner = capsuleModel(0.0055f, 0.028f, brow);
        Model browOuter = capsuleModel(0.0045f, 0.026f, brow);
        put(headParts, browInner, -0.022f, 0.028f, 0.088f, 0f, 0f, 75f);
        put(headParts, browOuter, -0.046f, 0.030f, 0.082f, 0f, 0f, 108f);
        put(headParts, browInner, 0.022f, 0.028f, 0.088f, 0f, 0f, -75f);
        put(headParts, browOuter, 0.046f, 0.030f, 0.082f, 0f, 0f, -108f);"""

new_brows_fringe = """        // Natural Groomed Eyebrows (Sleek youthful arch)
        Model browInner = capsuleModel(0.0038f, 0.026f, brow);
        Model browOuter = capsuleModel(0.0030f, 0.024f, brow);
        put(headParts, browInner, -0.022f, 0.030f, 0.086f, 0f, 0f, 78f);
        put(headParts, browOuter, -0.045f, 0.033f, 0.080f, 0f, 0f, 105f);
        put(headParts, browInner, 0.022f, 0.030f, 0.086f, 0f, 0f, -78f);
        put(headParts, browOuter, 0.045f, 0.033f, 0.080f, 0f, 0f, -105f);"""

assert old_brows_fringe in code, 'old_brows_fringe not found'
code = code.replace(old_brows_fringe, new_brows_fringe, 1)

old_fringe = """        // Messy Forehead Fringe Locks (falling naturally across forehead in tousled layers)
        put(headParts, capsuleModel(0.011f, 0.062f, hair), 0.026f, 0.064f, 0.082f, -34f, 18f, -32f);
        put(headParts, capsuleModel(0.010f, 0.056f, hairStrand), 0.046f, 0.056f, 0.076f, -26f, 26f, -38f);
        put(headParts, capsuleModel(0.011f, 0.058f, hair), -0.016f, 0.066f, 0.080f, -28f, -10f, 24f);
        put(headParts, capsuleModel(0.010f, 0.052f, hairStrand), -0.040f, 0.060f, 0.074f, -20f, -18f, 28f);
        put(headParts, capsuleModel(0.009f, 0.044f, hair), 0.006f, 0.062f, 0.082f, -30f, 5f, -10f);"""

new_fringe = """        // Messy Forehead Fringe Locks (Youthful textured college bangs across upper forehead)
        put(headParts, capsuleModel(0.010f, 0.052f, hair), 0.028f, 0.072f, 0.080f, -30f, 16f, -36f);
        put(headParts, capsuleModel(0.009f, 0.048f, hairStrand), 0.048f, 0.066f, 0.075f, -22f, 24f, -42f);
        put(headParts, capsuleModel(0.010f, 0.050f, hair), -0.018f, 0.074f, 0.078f, -26f, -12f, 28f);
        put(headParts, capsuleModel(0.009f, 0.045f, hairStrand), -0.042f, 0.068f, 0.072f, -18f, -18f, 32f);
        put(headParts, capsuleModel(0.008f, 0.038f, hair), 0.005f, 0.072f, 0.080f, -28f, 6f, -14f);"""

assert old_fringe in code, 'old_fringe not found'
code = code.replace(old_fringe, new_fringe, 1)

with open('core/src/main/java/bd/spark36/character/StudentMesh.java', 'w') as f:
    f.write(code)

print('Face refined successfully!')
