with open('core/src/main/java/bd/spark36/world/DhakaCampusWorld.java', 'r') as f:
    code = f.read()

# 1. Add PondFish class definition and pondFishes field
old_fields = """    private final Array<ModelInstance> boundarySparkles = new Array<>();
    private float animTime = 0f;"""

new_fields = """    private final Array<ModelInstance> boundarySparkles = new Array<>();
    private float animTime = 0f;

    public static class PondFish {
        public float centerX, centerZ;
        public float radiusX, radiusZ;
        public float speed;
        public float angle;
        public float swimPhase;
        public float depthY;
        public ModelInstance body;
        public ModelInstance tail;
    }
    private final Array<PondFish> pondFishes = new Array<>();"""

assert old_fields in code, "old_fields not found"
code = code.replace(old_fields, new_fields, 1)

# 2. Add ghat steps and fish creation after pukurShimmer
old_pukur = """        waterShimmer = new ModelInstance(pukurShimmer);
        waterShimmer.transform.setTranslation(pukurX - 2f, 0.14f, pukurZ - 1.5f);
        instances.add(waterShimmer);"""

new_pukur = """        waterShimmer = new ModelInstance(pukurShimmer);
        waterShimmer.transform.setTranslation(pukurX - 2f, 0.14f, pukurZ - 1.5f);
        instances.add(waterShimmer);

        // Curzon Hall Pukur Grand Ghat Steps (South Bank Entrance)
        float ghatZ = pukurZ + pukurL * 0.5f; // 18.0f
        Model ghatStepUpper = mb.createBox(7.2f, 0.32f, 1.4f, stoneCurb, attr);
        Model ghatStepMid = mb.createBox(6.6f, 0.20f, 1.2f, terracottaBrick, attr);
        Model ghatStepWater = mb.createBox(6.2f, 0.12f, 1.0f, stoneCurb, attr);
        models.add(ghatStepUpper);
        models.add(ghatStepMid);
        models.add(ghatStepWater);

        ModelInstance gsu = new ModelInstance(ghatStepUpper);
        gsu.transform.setTranslation(pukurX, 0.28f, ghatZ + 0.5f);
        instances.add(gsu);

        ModelInstance gsm = new ModelInstance(ghatStepMid);
        gsm.transform.setTranslation(pukurX, 0.18f, ghatZ - 0.4f);
        instances.add(gsm);

        ModelInstance gsw = new ModelInstance(ghatStepWater);
        gsw.transform.setTranslation(pukurX, 0.08f, ghatZ - 1.2f);
        instances.add(gsw);

        // Flanking ornamental stone bollards at the ghat
        Model ghatBollard = mb.createCylinder(0.36f, 0.70f, 0.36f, 12, stoneCurb, attr);
        models.add(ghatBollard);
        ModelInstance gbL = new ModelInstance(ghatBollard);
        gbL.transform.setTranslation(pukurX - 3.8f, 0.45f, ghatZ + 0.5f);
        instances.add(gbL);
        ModelInstance gbR = new ModelInstance(ghatBollard);
        gbR.transform.setTranslation(pukurX + 3.8f, 0.45f, ghatZ + 0.5f);
        instances.add(gbR);

        // Curzon Pond Swimming Fishes (Colorful Bengali Rohu, Koi & Golden Carp)
        buildPondFishes(mb, pukurX, pukurZ, pukurW, pukurL);"""

assert old_pukur in code, "old_pukur not found"
code = code.replace(old_pukur, new_pukur, 1)

# 3. Add buildPondFishes helper method and isNearPondGhat method
build_fish_method = """
    private void buildPondFishes(ModelBuilder mb, float cx, float cz, float pw, float pl) {
        Color[] fishColors = {
            new Color(1.0f, 0.55f, 0.08f, 1f), // Golden Koi
            new Color(0.92f, 0.20f, 0.14f, 1f), // Scarlet Carp
            new Color(0.65f, 0.82f, 0.96f, 1f), // Shimmering Rohu
            new Color(0.96f, 0.94f, 0.88f, 1f), // Pearl White Koi
            new Color(0.90f, 0.48f, 0.12f, 1f), // Deep Amber Carp
            new Color(0.85f, 0.25f, 0.20f, 1f), // Crimson Carp
            new Color(0.72f, 0.88f, 0.95f, 1f), // Silver Rohu
            new Color(0.98f, 0.65f, 0.15f, 1f)  // Sunburst Koi
        };

        for (int i = 0; i < 8; i++) {
            Color c = fishColors[i % fishColors.length];
            Material fishMat = new Material(
                ColorAttribute.createDiffuse(c),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.92f)
            );
            Model bodyModel = mb.createSphere(0.18f, 0.12f, 0.52f, 10, 8, fishMat, attr);
            Model tailModel = mb.createBox(0.035f, 0.16f, 0.22f, fishMat, attr);
            models.add(bodyModel);
            models.add(tailModel);

            PondFish fish = new PondFish();
            fish.centerX = cx;
            fish.centerZ = cz;
            fish.radiusX = 3.5f + (i % 4) * 1.3f;
            fish.radiusZ = 5.0f + (i % 4) * 1.8f;
            fish.speed = 0.8f + (i * 0.15f);
            fish.angle = i * (MathUtils.PI2 / 8f);
            fish.swimPhase = i * 1.3f;
            fish.depthY = 0.04f + (i % 3) * 0.02f;
            fish.body = new ModelInstance(bodyModel);
            fish.tail = new ModelInstance(tailModel);
            pondFishes.add(fish);
        }
    }

    public boolean isNearPondGhat(Vector3 pos) {
        return Math.abs(pos.x) < 4.2f && Math.abs(pos.z - 18.0f) < 2.5f;
    }
"""

old_render_block = """    public void render(ModelBatch batch) {
        for (ModelInstance inst : instances) {
            batch.render(inst, environment);
        }
        for (ModelInstance fx : effects) {
            batch.render(fx, environment);
        }
    }"""

new_render_block = """    public void render(ModelBatch batch) {
        for (ModelInstance inst : instances) {
            batch.render(inst, environment);
        }
        for (ModelInstance fx : effects) {
            batch.render(fx, environment);
        }
        for (int i = 0; i < pondFishes.size; i++) {
            PondFish fish = pondFishes.get(i);
            batch.render(fish.body, environment);
            batch.render(fish.tail, environment);
        }
    }"""

assert old_render_block in code, "old_render_block not found"
code = code.replace(old_render_block, new_render_block, 1)

# 4. Add fish update in update method
old_update_sparkles = """        for (int i = 0; i < boundarySparkles.size; i++) {
            ModelInstance sp = boundarySparkles.get(i);"""

new_update_sparkles = """        for (int f = 0; f < pondFishes.size; f++) {
            PondFish fish = pondFishes.get(f);
            fish.swimPhase += delta * (1.8f + fish.speed * 1.5f);
            fish.angle += delta * (fish.speed / Math.max(fish.radiusX, 1f));
            float fx = fish.centerX + MathUtils.sin(fish.angle) * fish.radiusX;
            float fz = fish.centerZ + MathUtils.cos(fish.angle) * fish.radiusZ;
            float dx = MathUtils.cos(fish.angle) * fish.radiusX;
            float dz = -MathUtils.sin(fish.angle) * fish.radiusZ;
            float heading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
            float tailWag = MathUtils.sin(fish.swimPhase * 6.5f) * 25.0f;

            fish.body.transform.idt().translate(fx, fish.depthY, fz).rotate(Vector3.Y, heading);
            fish.tail.transform.idt().translate(fx, fish.depthY, fz).rotate(Vector3.Y, heading)
                .translate(0f, 0f, -0.28f).rotate(Vector3.Y, tailWag);
        }

        for (int i = 0; i < boundarySparkles.size; i++) {
            ModelInstance sp = boundarySparkles.get(i);"""

assert old_update_sparkles in code, "old_update_sparkles not found"
code = code.replace(old_update_sparkles, new_update_sparkles, 1)

# Append build_fish_method right before the last closing brace
last_brace = code.rfind('}')
code = code[:last_brace] + build_fish_method + "\n}\n"

with open('core/src/main/java/bd/spark36/world/DhakaCampusWorld.java', 'w') as f:
    f.write(code)

print("DhakaCampusWorld.java updated successfully!")
