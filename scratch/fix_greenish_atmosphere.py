# 1. Update DhakaCampusWorld.java lighting and fog
with open('core/src/main/java/bd/spark36/world/DhakaCampusWorld.java', 'r') as f:
    dw = f.read()

old_lights = """        sunLight = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 120f, 120f, 1f, 140f);
        sunLight.set(new Color(1.05f, 0.97f, 0.84f, 1f), new Vector3(-0.55f, -0.60f, -0.58f).nor());
        environment.add(sunLight);
        environment.shadowMap = sunLight;

        // Cool sky-fill ambient so shadowed areas read as blue-ish shade, not flat grey
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.52f, 0.52f, 0.56f, 1f));

        // Soft front/south fill light so character faces and details are clear and radiant
        DirectionalLight fillLight = new DirectionalLight();
        fillLight.set(new Color(0.38f, 0.36f, 0.34f, 1f), new Vector3(0.20f, -0.30f, 0.92f).nor());
        environment.add(fillLight);

        // Atmospheric depth fog (warm golden dawn mist)
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.92f, 0.86f, 0.74f, 1f));"""

new_lights = """        sunLight = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 120f, 120f, 1f, 140f);
        sunLight.set(new Color(1.18f, 1.14f, 1.06f, 1f), new Vector3(-0.55f, -0.65f, -0.55f).nor());
        environment.add(sunLight);
        environment.shadowMap = sunLight;

        // Neutral daylight ambient without sickly yellow/green tint
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.44f, 0.46f, 0.50f, 1f));

        // Soft front fill light for natural facial illumination
        DirectionalLight fillLight = new DirectionalLight();
        fillLight.set(new Color(0.34f, 0.34f, 0.36f, 1f), new Vector3(0.20f, -0.30f, 0.92f).nor());
        environment.add(fillLight);

        // Clean atmospheric sky-blue depth haze (zero muddy greenish-yellow fog!)
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.78f, 0.86f, 0.96f, 1f));"""

assert old_lights in dw, "old_lights not found"
dw = dw.replace(old_lights, new_lights, 1)

# Update hedgeMat to deep rich natural forest green
old_hedge = "Material hedgeMat = new Material(ColorAttribute.createDiffuse(new Color(0.20f, 0.33f, 0.16f, 1f)));"
new_hedge = "Material hedgeMat = new Material(ColorAttribute.createDiffuse(new Color(0.11f, 0.20f, 0.10f, 1f)));"
assert old_hedge in dw, "old_hedge not found"
dw = dw.replace(old_hedge, new_hedge, 1)

with open('core/src/main/java/bd/spark36/world/DhakaCampusWorld.java', 'w') as f:
    f.write(dw)

# 2. Update AtmosphereRenderer.java sky colors
with open('core/src/main/java/bd/spark36/world/AtmosphereRenderer.java', 'r') as f:
    ar = f.read()

old_sky = """    // ── Sky Colors (Golden Morning) ────────────────────────────────────
    private final Color skyZenith    = new Color(0.28f, 0.50f, 0.78f, 1f); // deep morning blue
    private final Color skyMidBlue   = new Color(0.52f, 0.70f, 0.88f, 1f); // mid blue
    private final Color skyHorizon   = new Color(0.90f, 0.80f, 0.62f, 1f); // warm dawn gold
    private final Color skyLowHaze   = new Color(0.96f, 0.88f, 0.74f, 1f); // lower haze band"""

new_sky = """    // ── Sky Colors (Clean Crisp Summer Daylight - No Greenish/Yellow Cast) ────
    private final Color skyZenith    = new Color(0.18f, 0.44f, 0.84f, 1f); // Deep radiant azure
    private final Color skyMidBlue   = new Color(0.36f, 0.62f, 0.90f, 1f); // Mid summer sky
    private final Color skyHorizon   = new Color(0.68f, 0.82f, 0.96f, 1f); // Light clear sky horizon
    private final Color skyLowHaze   = new Color(0.82f, 0.90f, 0.98f, 1f); // Clean atmospheric horizon mist"""

assert old_sky in ar, "old_sky not found"
ar = ar.replace(old_sky, new_sky, 1)

# Remove the yellow horizon haze rectangle
old_haze_rect = """        // ── Warm horizon sun-glow haze ────────────────────────────────
        float pulse = 0.06f + 0.018f * MathUtils.sin(time * 0.5f);
        shapeRenderer.setColor(0.98f, 0.88f, 0.62f, pulse);
        shapeRenderer.rect(0, h * 0.22f, w, h * 0.14f);"""

new_haze_rect = """        // ── Clean solar horizon glow ────────────────────────────────
        float pulse = 0.04f + 0.012f * MathUtils.sin(time * 0.5f);
        shapeRenderer.setColor(0.90f, 0.94f, 1.0f, pulse);
        shapeRenderer.rect(0, h * 0.22f, w, h * 0.14f);"""

assert old_haze_rect in ar, "old_haze_rect not found"
ar = ar.replace(old_haze_rect, new_haze_rect, 1)

# Lower cloud layer 2 opacity and adjust height so it doesn't look like white discs on roofline
old_cumulus = """        // ── Cloud Layer 2: Mid cumulus ────────────────────────────────
        for (float[] c : cloudLayer2) {
            float cx = c[0] * w;
            float cy = c[1] * h;
            float cw = c[2] * w;
            float ch = c[3] * h;
            // Fluffy puffball: stacked circles
            for (int s = 0; s < 5; s++) {
                float sf = 0.55f + s * 0.10f;
                float ox = (s - 2f) * cw * 0.20f;
                float oy = -s * ch * 0.15f;
                shapeRenderer.setColor(0.97f, 0.96f, 0.94f, c[5] * sf * 0.9f);
                shapeRenderer.circle(cx + ox, cy + oy, ch * (0.7f + s * 0.12f), 36);
            }
            // Base fill
            shapeRenderer.setColor(0.97f, 0.96f, 0.94f, c[5] * 0.70f);
            shapeRenderer.rect(cx - cw / 2f, cy - ch * 0.5f, cw, ch * 0.55f);
        }"""

new_cumulus = """        // ── Cloud Layer 2: High soft cumulus (feathered) ────────────────
        for (float[] c : cloudLayer2) {
            float cx = c[0] * w;
            float cy = (0.60f + c[1] * 0.35f) * h;
            float cw = c[2] * w;
            float ch = c[3] * h * 0.8f;
            for (int s = 0; s < 5; s++) {
                float sf = 0.40f + s * 0.08f;
                float ox = (s - 2f) * cw * 0.18f;
                float oy = -s * ch * 0.10f;
                shapeRenderer.setColor(0.96f, 0.97f, 1.0f, c[5] * sf * 0.35f);
                shapeRenderer.ellipse(cx + ox - ch * 0.5f, cy + oy - ch * 0.3f, ch * (1.2f + s * 0.15f), ch * (0.7f + s * 0.10f), 24);
            }
        }"""

assert old_cumulus in ar, "old_cumulus not found"
ar = ar.replace(old_cumulus, new_cumulus, 1)

with open('core/src/main/java/bd/spark36/world/AtmosphereRenderer.java', 'w') as f:
    f.write(ar)

print("Greenish atmosphere successfully eliminated!")
