package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.TextureFactory;
import bd.spark36.world.landmarks.BarricadeBuilder;

/**
 * LEVEL 3: "A City on Edge" (উত্তাল শহর)
 * Period: July 18, 2024 (The "Complete Shutdown" — CS tear gas, sound grenades, and humanitarian aid).
 * Environment: Fog-shrouded Shahbagh / Campus perimeter road with police riot line and tire barricades.
 * Focus: Low-visibility gas navigation, delivering water/wet cloths ("পানি লাগবে, পানি?"), crowd evacuation.
 * Combat: Non-combat humanitarian aid and evasion.
 */
public class Level3PoliceEscalation implements CampaignLevel {

    private DhakaCampusWorld world;
    private CampusCrowd crowd;
    private Environment environment;
    private ShapeRenderer overlayRenderer;

    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();

    // Objectives:
    // 1. Collect water bottles from supply table
    // 2. Distribute water to choking student group 1 (North Avenue)
    // 3. Distribute water to choking student group 2 (Curzon Gate)
    // 4. Distribute water to choking student group 3 (West Road)
    // 5. Guide evacuated crowd to safe corridor
    private final boolean[] objectivesDone = new boolean[5];
    private final Vector3[] waypoints = {
        new Vector3(0f, 0f, 64f),    // Water Supply Station
        new Vector3(18f, 0f, 52f),   // Choking Group 1
        new Vector3(-14f, 0f, 48f),  // Choking Group 2
        new Vector3(-32f, 0f, 32f),  // Choking Group 3
        new Vector3(0f, 0f, 76f)     // Safe Evacuation Exit
    };

    private static final String[] CHECKLIST = {
        "1. Collect Fresh Water & Cloth Bundles",
        "2. Aid Choking Students on Promenade",
        "3. Provide Water to Gate Crowd",
        "4. Relieve Tear Gas Victims on West Road",
        "5. Lead Evacuated Group to Safety Corridor"
    };

    private boolean carryingWater = false;
    private float gasShakeTimer = 0f;
    private float smokeAnimTime = 0f;

    @Override
    public int getLevelNumber() {
        return 3;
    }

    @Override
    public String getTitle() {
        return "A CITY ON EDGE";
    }

    @Override
    public String getSubtitle() {
        return "উত্তাল শহর";
    }

    @Override
    public String getHistoricalDate() {
        return "July 18, 2024";
    }

    @Override
    public String getLocationName() {
        return "Dhaka University Perimeter — Shahbagh Approach";
    }

    @Override
    public String getMissionTag() {
        return String.format("ACTIVE MISSION: %02d / 05", Math.min(getCompletedObjectiveCount() + 1, 5));
    }

    @Override
    public String getMissionTitle() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "COLLECT CLEAN WATER";
            case 1: return "RELIEVE TEAR GAS VICTIMS (GROUP 1)";
            case 2: return "DISTRIBUTE WATER AT MAIN PROMENADE";
            case 3: return "AID STRANDED CIVILIANS (WEST ROAD)";
            case 4: return "GUIDE CROWD TO SAFE CORRIDOR";
            default: return "CROWD EVACUATED TO SAFETY";
        }
    }

    @Override
    public String getMissionTarget() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "Target: Water Supply Station ('Pani lagbe, pani?')";
            case 1: return "Target: Choking students caught in CS gas";
            case 2: return "Target: Tear-gassed demonstrators at gate";
            case 3: return "Target: Injured peers on west road";
            case 4: return "Target: Liberated safe evacuation exit";
            default: return "All civilians safely evacuated";
        }
    }

    private int getNextObjectiveIndex() {
        for (int i = 0; i < objectivesDone.length; i++) {
            if (!objectivesDone[i]) return i;
        }
        return objectivesDone.length - 1;
    }

    @Override
    public Vector3 getActiveWaypoint() {
        return waypoints[getNextObjectiveIndex()];
    }

    @Override
    public float getWaypointDistance(Vector3 playerPos) {
        return playerPos.dst(getActiveWaypoint());
    }

    @Override
    public String[] getChecklistItems() {
        return CHECKLIST;
    }

    @Override
    public boolean isChecklistItemDone(int index) {
        if (index >= 0 && index < objectivesDone.length) {
            return objectivesDone[index];
        }
        return false;
    }

    @Override
    public int getCompletedObjectiveCount() {
        int c = 0;
        for (boolean b : objectivesDone) if (b) c++;
        return c;
    }

    @Override
    public int getTotalObjectiveCount() {
        return 5;
    }

    @Override
    public String getContextPrompt(Vector3 playerPos) {
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 3.2f) {
            return "[E] COLLECT WATER BOTTLES & SALINE CLOTHS";
        }
        if (carryingWater) {
            if (!objectivesDone[1] && playerPos.dst(waypoints[1]) <= 3.8f) {
                return "[E] DISTRIBUTE WATER — 'পানি লাগবে, পানি?'";
            }
            if (!objectivesDone[2] && playerPos.dst(waypoints[2]) <= 3.8f) {
                return "[E] DISTRIBUTE WATER TO CHOKING STUDENTS";
            }
            if (!objectivesDone[3] && playerPos.dst(waypoints[3]) <= 3.8f) {
                return "[E] DISTRIBUTE SALINE & WASH EYES";
            }
        }
        if (objectivesDone[1] && objectivesDone[2] && objectivesDone[3] && !objectivesDone[4]) {
            if (playerPos.dst(waypoints[4]) <= 4.5f) {
                return "[E] OPEN EVACUATION GATE TO SAFETY";
            }
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 3.2f) {
            objectivesDone[0] = true;
            carryingWater = true;
            hud.triggerMissionToast("WATER BOTTLES COLLECTED", getCompletedObjectiveCount());
            return true;
        }
        if (carryingWater) {
            if (!objectivesDone[1] && playerPos.dst(waypoints[1]) <= 3.8f) {
                objectivesDone[1] = true;
                hud.triggerMissionToast("AID DELIVERED ('PANI LAGBE, PANI?')", getCompletedObjectiveCount());
                return true;
            }
            if (!objectivesDone[2] && playerPos.dst(waypoints[2]) <= 3.8f) {
                objectivesDone[2] = true;
                hud.triggerMissionToast("AID DELIVERED (PROMENADE)", getCompletedObjectiveCount());
                return true;
            }
            if (!objectivesDone[3] && playerPos.dst(waypoints[3]) <= 3.8f) {
                objectivesDone[3] = true;
                hud.triggerMissionToast("AID DELIVERED (WEST ROAD)", getCompletedObjectiveCount());
                return true;
            }
        }
        if (objectivesDone[1] && objectivesDone[2] && objectivesDone[3] && !objectivesDone[4]) {
            if (playerPos.dst(waypoints[4]) <= 4.5f) {
                objectivesDone[4] = true;
                hud.triggerMissionToast("SAFE EVACUATION ACCOMPLISHED", getCompletedObjectiveCount());
                hud.triggerVictory();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLevelComplete() {
        for (boolean b : objectivesDone) if (!b) return false;
        return true;
    }

    @Override
    public boolean isCombatEnabled() {
        return false; // Level 3 is focused on humanitarian survival and evasion
    }

    @Override
    public Vector3 getSpawnPosition() {
        return new Vector3(0f, 0f, 68f);
    }

    @Override
    public float getSpawnHeading() {
        return 180f;
    }

    @Override
    public void init(TextureFactory textures, PlayerController player, CinematicCamera camera) {
        this.world = new DhakaCampusWorld(textures);
        this.overlayRenderer = new ShapeRenderer();

        // Thick hazy atmosphere (white CS gas haze)
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.58f, 0.60f, 0.62f, 1f));
        DirectionalLight diffusedSun = new DirectionalLight().set(0.65f, 0.65f, 0.68f, -0.3f, -0.8f, -0.3f);
        environment.add(diffusedSun);

        // Build water station & barricades
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;
        Material tableMat = new Material(ColorAttribute.createDiffuse(new Color(0.42f, 0.30f, 0.18f, 1f)));
        Material bottleMat = new Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.70f, 0.95f, 1f)));
        Material barricadeWood = new Material(ColorAttribute.createDiffuse(new Color(0.38f, 0.28f, 0.18f, 1f)));
        Material barricadeMetal = new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.24f, 0.28f, 1f)));

        Model waterTable = BarricadeBuilder.buildWaterSupplyStation(mb, tableMat, bottleMat, attr);
        Model barricade = BarricadeBuilder.buildBarricade(mb, 4.0f, 1.3f, barricadeWood, barricadeMetal, attr);
        models.add(waterTable);
        models.add(barricade);

        // Water station instance
        ModelInstance wti = new ModelInstance(waterTable);
        wti.transform.setTranslation(waypoints[0]);
        instances.add(wti);

        // Street barricades blocking Shahbagh arterial road
        Vector3[] bp = {
            new Vector3(-8f, 0f, 80f),
            new Vector3(0f, 0f, 82f),
            new Vector3(8f, 0f, 80f)
        };
        for (Vector3 p : bp) {
            ModelInstance bi = new ModelInstance(barricade);
            bi.transform.setTranslation(p);
            instances.add(bi);
            colliders.add(new float[] { p.x - 2f, 0f, p.z - 0.4f, p.x + 2f, 1.4f, p.z + 0.4f });
        }

        for (float[] b : world.getColliders()) colliders.add(b);
        player.setExtraColliders(colliders);
        this.crowd = new CampusCrowd(16, player.getColliders());
        this.crowd.setLevelProfile(3);
        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(4.5f);
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        smokeAnimTime += delta;
        gasShakeTimer += delta;

        // Periodic distant sound grenade detonations shaking camera
        if (gasShakeTimer >= 6.5f) {
            gasShakeTimer = 0f;
            camera.addShake(0.12f);
            bd.spark36.audio.SoundManager.get().playGasHiss();
        }

        if (world != null) world.update(delta);
        if (crowd != null) crowd.update(delta, player.getPosition());
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment env) {
        if (world != null) world.render(modelBatch);
        for (ModelInstance mi : instances) modelBatch.render(mi, environment);
    }

    @Override
    public void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH) {
        // Volumetric white/grey CS tear gas fog overlay
        if (overlayRenderer != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            overlayRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);
            overlayRenderer.begin(ShapeType.Filled);

            float pulse = 0.12f + 0.05f * MathUtils.sin(smokeAnimTime * 1.5f);
            overlayRenderer.setColor(0.92f, 0.94f, 0.96f, pulse);
            overlayRenderer.rect(0, 0, screenW, screenH);

            // Ground-level thicker gas bank
            float groundGasH = screenH * 0.35f;
            Color gasBottom = new Color(0.95f, 0.96f, 0.98f, pulse * 1.8f);
            Color gasClear = new Color(0.95f, 0.96f, 0.98f, 0f);
            overlayRenderer.rect(0, 0, screenW, groundGasH, gasBottom, gasBottom, gasClear, gasClear);

            overlayRenderer.end();
        }
    }

    @Override
    public java.util.List<float[]> getColliders() {
        return colliders;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud) {
        if (crowd != null && crowd.checkHitByPlayer(player.getPosition(), player.getHeadingDegrees(), 2.2f)) {
            hud.setDisqualified(true, "VIOLATION: Striking civilians is strictly forbidden!");
            return true;
        }
        return false;
    }

    @Override
    public DhakaCampusWorld getWorld() {
        return world;
    }

    @Override
    public CampusCrowd getCrowd() {
        return crowd;
    }

    @Override
    public void completeAllObjectivesForTest() {
        java.util.Arrays.fill(objectivesDone, true);
    }

    @Override
    public void dispose() {
        if (world != null) { world.dispose(); world = null; }
        for (Model m : models) m.dispose();
        models.clear();
        if (overlayRenderer != null) { overlayRenderer.dispose(); overlayRenderer = null; }
    }
}
