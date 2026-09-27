with open('core/src/main/java/bd/spark36/character/StudentMesh.java', 'r') as f:
    code = f.read()

old_sec = """        // Skin & Face (Radiant warm golden South Asian tone)
        Material skin = flat(0.82f, 0.64f, 0.48f);
        Material skinHighlight = flat(0.90f, 0.74f, 0.58f);
        Material skinShade = flat(0.68f, 0.50f, 0.36f);
        Material eyeWhite = flat(0.96f, 0.96f, 0.98f);
        Material iris = flat(0.18f, 0.11f, 0.07f);
        Material pupil = flat(0.02f, 0.02f, 0.02f);
        Material catchlight = flat(0.99f, 0.99f, 1.0f);
        Material upperLid = flat(0.14f, 0.09f, 0.07f);
        Material brow = flat(0.08f, 0.05f, 0.04f);
        Material lips = flat(0.68f, 0.42f, 0.38f);
        Material hair = flat(0.08f, 0.06f, 0.05f);
        Material hairHighlight = flat(0.18f, 0.14f, 0.11f);

        // Authentic Dhaka University ID Card & Lanyard
        Material lanyardBlue = flat(0.12f, 0.38f, 0.88f);
        Material idCardWhite = flat(0.98f, 0.98f, 0.99f);
        Material idCardHeader = flat(0.12f, 0.38f, 0.88f);
        Material idPhoto = flat(0.62f, 0.48f, 0.38f);
        Material idClip = flat(0.85f, 0.85f, 0.90f);

        // Casual University Summer Clothing
        Material shirtBody = flat(0.48f, 0.58f, 0.52f);      // Light sage summer casual shirt
        Material shirtTrim = flat(0.38f, 0.48f, 0.42f);      // Collar, cuffs, placket
        Material shirtButton = flat(0.92f, 0.92f, 0.90f);    // Crisp buttons
        Material innerShirt = flat(0.93f, 0.92f, 0.90f);     // White crewneck undershirt at open collar
        Material denim = flat(0.15f, 0.21f, 0.33f);          // Classic indigo denim jeans
        Material denimShade = flat(0.11f, 0.15f, 0.25f);     // Jeans hem / cuff
        Material beltLeather = flat(0.16f, 0.10f, 0.07f);    // Leather belt
        Material buckle = flat(0.82f, 0.84f, 0.88f);         // Steel buckle"""

new_sec = """        // Skin & Face (Radiant warm golden South Asian tone)
        Material skin = flat(0.81f, 0.63f, 0.49f);
        Material skinHighlight = flat(0.85f, 0.68f, 0.54f);
        Material skinShade = flat(0.72f, 0.55f, 0.42f);
        Material eyeWhite = flat(0.96f, 0.96f, 0.98f);
        Material iris = flat(0.18f, 0.11f, 0.07f);
        Material pupil = flat(0.02f, 0.02f, 0.02f);
        Material catchlight = flat(0.99f, 0.99f, 1.0f);
        Material upperLid = flat(0.14f, 0.09f, 0.07f);
        Material brow = flat(0.07f, 0.05f, 0.04f);
        Material lips = flat(0.66f, 0.42f, 0.38f);
        Material hair = flat(0.06f, 0.05f, 0.05f);
        Material hairHighlight = flat(0.09f, 0.07f, 0.06f);
        Material hairStrand = flat(0.11f, 0.09f, 0.08f);

        // Authentic Dhaka University ID Card & Lanyard
        Material lanyardBlue = flat(0.10f, 0.35f, 0.85f);
        Material idCardWhite = flat(0.98f, 0.98f, 0.99f);
        Material idCardHeader = flat(0.10f, 0.35f, 0.85f);
        Material idPhoto = flat(0.62f, 0.48f, 0.38f);
        Material idClip = flat(0.85f, 0.85f, 0.90f);

        // Casual University Summer Clothing
        Material shirtBody = flat(0.46f, 0.56f, 0.50f);      // Light sage summer casual shirt
        Material shirtTrim = flat(0.36f, 0.46f, 0.40f);      // Collar, cuffs, placket
        Material shirtButton = flat(0.92f, 0.92f, 0.90f);    // Crisp buttons
        Material innerShirt = flat(0.88f, 0.88f, 0.87f);     // Subtle undershirt at open collar
        Material denim = flat(0.15f, 0.21f, 0.33f);          // Classic indigo denim jeans
        Material denimShade = flat(0.11f, 0.15f, 0.25f);     // Jeans hem / cuff
        Material beltLeather = flat(0.16f, 0.10f, 0.07f);    // Leather belt
        Material buckle = flat(0.82f, 0.84f, 0.88f);         // Steel buckle"""

assert old_sec in code, 'old_sec not found'
code = code.replace(old_sec, new_sec, 1)

old_body = """        // =========================================================================
        // 2. Chest & Torso (Origin: y = 1.13)
        // =========================================================================
        put(chestParts, ellipsoidModel(0.12f, 0.08f, 0.12f, innerShirt), 0f, 0.21f, 0.035f);
        put(chestParts, cylinderModel(0.335f, 0.43f, 0.210f, shirtBody), 0f, 0.02f, -0.005f);
        put(chestParts, ellipsoidModel(0.345f, 0.18f, 0.215f, shirtBody), 0f, 0.13f, 0.005f);

        // Trapezius muscle slope connecting neck naturally to broad athletic shoulders
        put(chestParts, ellipsoidModel(0.28f, 0.14f, 0.18f, shirtBody), 0f, 0.20f, -0.010f);

        // Natural Shoulder Drapery (tapered box drape, seamless arm seam)
        Model shirtShoulder = boxModel(0.095f, 0.080f, 0.140f, shirtBody);
        put(chestParts, shirtShoulder, -0.175f, 0.190f, 0.005f, 0f, 0f, -10f);
        put(chestParts, shirtShoulder, 0.175f, 0.190f, 0.005f, 0f, 0f, 10f);

        // Folded shirt collar
        Model collarLapel = boxModel(0.042f, 0.13f, 0.018f, shirtTrim);
        put(chestParts, collarLapel, -0.062f, 0.205f, 0.098f, -12f, 18f, 22f);
        put(chestParts, collarLapel, 0.062f, 0.205f, 0.098f, -12f, -18f, -22f);

        // Front button placket with buttons
        put(chestParts, boxModel(0.024f, 0.38f, 0.012f, shirtTrim), 0f, 0.03f, 0.106f);
        for (int b = 0; b < 4; b++) {
            float by = 0.15f - b * 0.08f;
            put(chestParts, ellipsoidModel(0.008f, 0.008f, 0.006f, shirtButton), 0f, by, 0.114f);
        }

        // Chest pocket
        put(chestParts, boxModel(0.062f, 0.070f, 0.008f, shirtTrim), -0.082f, 0.09f, 0.106f);

        // -------------------------------------------------------------------------
        // Dhaka University Student ID Card & Lanyard Ribbons
        // Draped naturally around collar and down across chest
        // -------------------------------------------------------------------------
        // Upper collar ribbons
        Model ribbonTop = boxModel(0.016f, 0.12f, 0.004f, lanyardBlue);
        put(chestParts, ribbonTop, -0.055f, 0.21f, 0.080f, 20f, 0f, 10f);
        put(chestParts, ribbonTop, 0.055f, 0.21f, 0.080f, 20f, 0f, -10f);
        // Chest ribbons converging to ID card
        Model ribbonChestL = boxModel(0.016f, 0.24f, 0.004f, lanyardBlue);
        Model ribbonChestR = boxModel(0.016f, 0.24f, 0.004f, lanyardBlue);
        put(chestParts, ribbonChestL, -0.032f, 0.08f, 0.114f, 0f, 0f, 12f);
        put(chestParts, ribbonChestR, 0.032f, 0.08f, 0.114f, 0f, 0f, -12f);

        // Metal Swivel Clip
        put(chestParts, boxModel(0.014f, 0.020f, 0.008f, idClip), 0f, -0.022f, 0.120f);

        // Laminated Dhaka University Student ID Card
        put(chestParts, boxModel(0.082f, 0.110f, 0.004f, idCardWhite), 0f, -0.082f, 0.122f);
        put(chestParts, boxModel(0.082f, 0.024f, 0.005f, idCardHeader), 0f, -0.036f, 0.123f);
        put(chestParts, boxModel(0.032f, 0.038f, 0.006f, idPhoto), -0.018f, -0.072f, 0.124f);
        put(chestParts, boxModel(0.024f, 0.005f, 0.006f, idCardHeader), 0.018f, -0.062f, 0.124f);
        put(chestParts, boxModel(0.024f, 0.004f, 0.006f, denim), 0.018f, -0.072f, 0.124f);
        put(chestParts, boxModel(0.024f, 0.004f, 0.006f, denim), 0.018f, -0.082f, 0.124f);
        put(chestParts, boxModel(0.072f, 0.006f, 0.006f, denim), 0f, -0.104f, 0.124f);

        // Backpack on back
        put(chestParts, ellipsoidModel(0.27f, 0.36f, 0.15f, backpackCanvas), 0f, 0.04f, -0.165f);
        put(chestParts, ellipsoidModel(0.21f, 0.13f, 0.08f, backpackCanvas), 0f, -0.07f, -0.235f);
        Model strapTop = capsuleModel(0.018f, 0.20f, backpackStrap);
        put(chestParts, strapTop, -0.105f, 0.22f, -0.035f, 30f, 0f, 0f);
        put(chestParts, strapTop, 0.105f, 0.22f, -0.035f, 30f, 0f, 0f);
        Model strapFront = capsuleModel(0.016f, 0.24f, backpackStrap);
        put(chestParts, strapFront, -0.105f, 0.08f, 0.105f, -6f, 0f, 0f);
        put(chestParts, strapFront, 0.105f, 0.08f, 0.105f, -6f, 0f, 0f);

        // =========================================================================
        // 3. Neck (Origin: y = 1.42) - Proportional, sturdy athletic neck
        // =========================================================================
        put(neckParts, cylinderModel(0.124f, 0.090f, 0.118f, skin), 0f, -0.010f, 0.005f);
        put(neckParts, ellipsoidModel(0.022f, 0.026f, 0.018f, skinHighlight), 0f, 0.005f, 0.060f); // Adam's apple"""

new_body = """        // =========================================================================
        // 2. Chest & Torso (Origin: y = 1.13)
        // =========================================================================
        put(chestParts, ellipsoidModel(0.12f, 0.08f, 0.12f, innerShirt), 0f, 0.21f, 0.035f);
        put(chestParts, cylinderModel(0.315f, 0.43f, 0.205f, shirtBody), 0f, 0.02f, -0.005f);
        put(chestParts, ellipsoidModel(0.325f, 0.18f, 0.210f, shirtBody), 0f, 0.13f, 0.005f);

        // Trapezius muscle slope connecting neck naturally to athletic shoulders
        put(chestParts, ellipsoidModel(0.26f, 0.14f, 0.17f, shirtBody), 0f, 0.20f, -0.010f);

        // Natural Shoulder Drapery (rounded anatomical deltoid curves, NO sharp boxy corners!)
        Model shirtShoulder = ellipsoidModel(0.110f, 0.085f, 0.130f, shirtBody);
        put(chestParts, shirtShoulder, -0.170f, 0.185f, 0.005f, 0f, 0f, -12f);
        put(chestParts, shirtShoulder, 0.170f, 0.185f, 0.005f, 0f, 0f, 12f);

        // Folded shirt collar around base of neck
        Model collarLapel = boxModel(0.038f, 0.12f, 0.016f, shirtTrim);
        put(chestParts, collarLapel, -0.058f, 0.205f, 0.092f, -12f, 18f, 22f);
        put(chestParts, collarLapel, 0.058f, 0.205f, 0.092f, -12f, -18f, -22f);

        // Front button placket with buttons
        put(chestParts, boxModel(0.022f, 0.38f, 0.010f, shirtTrim), 0f, 0.03f, 0.104f);
        for (int b = 0; b < 4; b++) {
            float by = 0.15f - b * 0.08f;
            put(chestParts, ellipsoidModel(0.008f, 0.008f, 0.006f, shirtButton), 0f, by, 0.111f);
        }

        // Chest pocket
        put(chestParts, boxModel(0.060f, 0.068f, 0.007f, shirtTrim), -0.080f, 0.09f, 0.104f);

        // -------------------------------------------------------------------------
        // Dhaka University Student ID Card & Lanyard Ribbons
        // Draped naturally around collar and down across chest
        // -------------------------------------------------------------------------
        // Upper collar ribbons
        Model ribbonTop = boxModel(0.015f, 0.12f, 0.004f, lanyardBlue);
        put(chestParts, ribbonTop, -0.052f, 0.21f, 0.078f, 20f, 0f, 10f);
        put(chestParts, ribbonTop, 0.052f, 0.21f, 0.078f, 20f, 0f, -10f);
        // Chest ribbons converging to ID card
        Model ribbonChestL = boxModel(0.015f, 0.24f, 0.004f, lanyardBlue);
        Model ribbonChestR = boxModel(0.015f, 0.24f, 0.004f, lanyardBlue);
        put(chestParts, ribbonChestL, -0.030f, 0.08f, 0.112f, 0f, 0f, 12f);
        put(chestParts, ribbonChestR, 0.030f, 0.08f, 0.112f, 0f, 0f, -12f);

        // Metal Swivel Clip
        put(chestParts, boxModel(0.014f, 0.020f, 0.008f, idClip), 0f, -0.022f, 0.118f);

        // Laminated Dhaka University Student ID Card
        put(chestParts, boxModel(0.082f, 0.110f, 0.004f, idCardWhite), 0f, -0.082f, 0.120f);
        put(chestParts, boxModel(0.082f, 0.024f, 0.005f, idCardHeader), 0f, -0.036f, 0.121f);
        put(chestParts, boxModel(0.032f, 0.038f, 0.006f, idPhoto), -0.018f, -0.072f, 0.122f);
        put(chestParts, boxModel(0.024f, 0.005f, 0.006f, idCardHeader), 0.018f, -0.062f, 0.122f);
        put(chestParts, boxModel(0.024f, 0.004f, 0.006f, denim), 0.018f, -0.072f, 0.122f);
        put(chestParts, boxModel(0.024f, 0.004f, 0.006f, denim), 0.018f, -0.082f, 0.122f);
        put(chestParts, boxModel(0.072f, 0.006f, 0.006f, denim), 0f, -0.104f, 0.122f);

        // Backpack on back
        put(chestParts, ellipsoidModel(0.27f, 0.36f, 0.15f, backpackCanvas), 0f, 0.04f, -0.165f);
        put(chestParts, ellipsoidModel(0.21f, 0.13f, 0.08f, backpackCanvas), 0f, -0.07f, -0.235f);
        Model strapTop = capsuleModel(0.018f, 0.20f, backpackStrap);
        put(chestParts, strapTop, -0.105f, 0.22f, -0.035f, 30f, 0f, 0f);
        put(chestParts, strapTop, 0.105f, 0.22f, -0.035f, 30f, 0f, 0f);
        Model strapFront = capsuleModel(0.016f, 0.24f, backpackStrap);
        put(chestParts, strapFront, -0.105f, 0.08f, 0.105f, -6f, 0f, 0f);
        put(chestParts, strapFront, 0.105f, 0.08f, 0.105f, -6f, 0f, 0f);

        // =========================================================================
        // 3. Neck (Origin: y = 1.42) - Proportional, sturdy athletic neck
        // =========================================================================
        put(neckParts, cylinderModel(0.120f, 0.100f, 0.114f, skin), 0f, -0.005f, 0.005f);
        put(neckParts, ellipsoidModel(0.020f, 0.024f, 0.016f, skin), 0f, 0.005f, 0.058f); // Adam's apple"""

assert old_body in code, 'old_body not found'
code = code.replace(old_body, new_body, 1)

old_hair = """        // -------------------------------------------------------------------------
        // \"Elomelo Chul\" - Messy, Natural, Textured Youthful College Hairstyle
        // -------------------------------------------------------------------------
        // Crown & Back Volume
        put(headParts, ellipsoidModel(0.178f, 0.140f, 0.185f, hair), 0f, 0.055f, -0.015f);
        put(headParts, ellipsoidModel(0.160f, 0.130f, 0.135f, hair), 0f, -0.020f, -0.040f); // Tapered nape

        // Side hair layers blending seamlessly with temples
        Model sideHair = ellipsoidModel(0.050f, 0.085f, 0.075f, hair);
        put(headParts, sideHair, -0.076f, 0.020f, -0.005f);
        put(headParts, sideHair, 0.076f, 0.020f, -0.005f);

        // Tousled, Messy Top Volume
        put(headParts, ellipsoidModel(0.140f, 0.065f, 0.140f, hairHighlight), 0.012f, 0.105f, 0.008f, -6f, 12f, -8f);

        // Layered Messy Strands & Cowlicks
        put(headParts, capsuleModel(0.013f, 0.075f, hair), 0.026f, 0.095f, 0.036f, -22f, 20f, -28f);
        put(headParts, capsuleModel(0.012f, 0.070f, hairHighlight), 0.048f, 0.090f, 0.042f, -18f, 14f, -18f);
        put(headParts, capsuleModel(0.011f, 0.065f, hair), -0.030f, 0.092f, 0.030f, -20f, -16f, 24f);

        // Messy Forehead Fringe Locks (falling naturally across forehead)
        put(headParts, capsuleModel(0.012f, 0.065f, hair), 0.024f, 0.062f, 0.082f, -32f, 16f, -30f);
        put(headParts, capsuleModel(0.011f, 0.055f, hair), -0.016f, 0.064f, 0.078f, -26f, -12f, 22f);
        put(headParts, capsuleModel(0.010f, 0.048f, hairHighlight), -0.042f, 0.058f, 0.072f, -18f, -16f, 28f);
        put(headParts, capsuleModel(0.009f, 0.042f, hair), 0.050f, 0.054f, 0.074f, -22f, 24f, -38f);
        put(headParts, capsuleModel(0.008f, 0.035f, hairHighlight), 0.004f, 0.060f, 0.080f, -28f, 4f, -8f);

        Model sideburn = capsuleModel(0.008f, 0.050f, hair);
        put(headParts, sideburn, -0.076f, 0.002f, 0.022f, 8f, 0f, 0f);
        put(headParts, sideburn, 0.076f, 0.002f, 0.022f, 8f, 0f, 0f);"""

new_hair = """        // -------------------------------------------------------------------------
        // \"Elomelo Chul\" - Messy, Natural, Textured Youthful College Hairstyle
        // -------------------------------------------------------------------------
        // Crown & Back Base Volume
        put(headParts, ellipsoidModel(0.182f, 0.142f, 0.182f, hair), 0f, 0.055f, -0.015f);
        put(headParts, ellipsoidModel(0.165f, 0.130f, 0.140f, hair), 0f, -0.020f, -0.040f); // Tapered nape

        // Side hair layers blending seamlessly with temples
        Model sideHair = ellipsoidModel(0.052f, 0.085f, 0.078f, hair);
        put(headParts, sideHair, -0.076f, 0.020f, -0.005f);
        put(headParts, sideHair, 0.076f, 0.020f, -0.005f);

        // Messy crown volume with directional flow (textured youthful cut)
        put(headParts, ellipsoidModel(0.165f, 0.060f, 0.155f, hair), 0.010f, 0.100f, 0.005f, -8f, 10f, -6f);
        put(headParts, ellipsoidModel(0.130f, 0.052f, 0.130f, hairHighlight), -0.015f, 0.106f, 0.010f, -5f, -8f, 4f);

        // Organic messy tufts / cowlicks breaking the smooth silhouette
        put(headParts, capsuleModel(0.014f, 0.075f, hair), 0.035f, 0.102f, 0.030f, -24f, 22f, -32f);
        put(headParts, capsuleModel(0.013f, 0.072f, hairStrand), 0.055f, 0.095f, 0.015f, -16f, 28f, -24f);
        put(headParts, capsuleModel(0.014f, 0.070f, hair), -0.035f, 0.098f, 0.025f, -22f, -18f, 26f);
        put(headParts, capsuleModel(0.012f, 0.065f, hairStrand), -0.055f, 0.092f, 0.010f, -14f, -25f, 20f);
        put(headParts, capsuleModel(0.013f, 0.068f, hair), 0.005f, 0.108f, -0.020f, 15f, 8f, -10f); // Back crown flick

        // Messy Forehead Fringe Locks (falling naturally across forehead in tousled layers)
        put(headParts, capsuleModel(0.011f, 0.062f, hair), 0.026f, 0.064f, 0.082f, -34f, 18f, -32f);
        put(headParts, capsuleModel(0.010f, 0.056f, hairStrand), 0.046f, 0.056f, 0.076f, -26f, 26f, -38f);
        put(headParts, capsuleModel(0.011f, 0.058f, hair), -0.016f, 0.066f, 0.080f, -28f, -10f, 24f);
        put(headParts, capsuleModel(0.010f, 0.052f, hairStrand), -0.040f, 0.060f, 0.074f, -20f, -18f, 28f);
        put(headParts, capsuleModel(0.009f, 0.044f, hair), 0.006f, 0.062f, 0.082f, -30f, 5f, -10f);

        Model sideburn = capsuleModel(0.008f, 0.050f, hair);
        put(headParts, sideburn, -0.076f, 0.002f, 0.022f, 8f, 0f, 0f);
        put(headParts, sideburn, 0.076f, 0.002f, 0.022f, 8f, 0f, 0f);"""

assert old_hair in code, 'old_hair not found'
code = code.replace(old_hair, new_hair, 1)

with open('core/src/main/java/bd/spark36/character/StudentMesh.java', 'w') as f:
    f.write(code)

print('StudentMesh.java updated successfully!')
