package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.TextureFactory;
import bd.spark36.world.landmarks.BarricadeBuilder;
import bd.spark36.world.landmarks.MilitaryVehicleBuilder;

/**
 * LEVEL 4: "Voices in the Dark" (অন্ধকারে কণ্ঠস্বর)
 * Period: July 20 – 23, 2024 (Total communications blackout, army curfew & BTR-80 APC checkpoints).
 * Environment: Deep night urban corridor, solitary sodium lamps, sweeping military searchlights.
 * Focus: Stealth pathfinding, avoiding searchlight cones, hand-delivering casualty documentation to press.
 * Combat: Pure covert stealth & evasion.
 */
public class Level4CurfewBlackout implements CampaignLevel {

    private DhakaCampusWorld world;
    private Environment nightEnvironment;

    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();

    private ModelInstance apcInstance;
    private ModelInstance searchlightInstance;

    // Objectives:
    // 1. Infiltrate outer perimeter sandbag checkpoint
    // 2. Retrieve casualty documentation dossier from courier
    // 3. Bypass BTR-80 APC searchlight zone
    // 4. Reach independent press contact at Library gate
    private final boolean[] objectivesDone = new boolean[4];
    private final Vector3[] waypoints = {
        new Vector3(0f, 0f, 48f),    // Checkpoint 1
        new Vector3(14f, 0f, 22f),   // Courier hiding spot
        new Vector3(0f, 0f, 6f),     // APC Searchlight sector
        new Vector3(-42f, 0f, 18f)   // Press contact
    };

    private static final String[] CHECKLIST = {
        "1. Infiltrate Past Outer Checkpoint Sandbags",
        "2. Retrieve Secret Casualty Dossier from Courier",
        "3. Bypass BTR-80 Armored Vehicle Searchlight Zone",
        "4. Hand-Deliver Evidence to Press Contact"
    };

    private boolean carryingDossier = false;
    private float searchlightAngle = 0f;

    @Override
    public int getLevelNumber() {
        return 4;
    }

    @Override
    public String getTitle() {
        return "VOICES IN THE DARK";
    }

    @Override
    public String getSubtitle() {
        return "অন্ধকারে কণ্ঠস্বর";
    }

    @Override
    public String getHistoricalDate() {
        return "July 20 – 23, 2024";
    }

    @Override
    public String getLocationName() {
        return "Dhaka Curfew Zone — Blackout Corridor";
    }

    @Override
    public String getMissionTag() {
        return String.format("ACTIVE MISSION: %02d / 04", Math.min(getCompletedObjectiveCount() + 1, 4));
    }

    @Override
    public String getMissionTitle() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "INFILTRATE CURFEW CHECKPOINT";
            case 1: return "RENDEZVOUS WITH COURIER";
            case 2: return "BYPASS APC SEARCHLIGHT";
            case 3: return "DELIVER DOSSIER TO PRESS";
            default: return "BLACKOUT DOSSIER DELIVERED";
        }
    }

    @Override
    public String getMissionTarget() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "Target: Sandbag checkpoint breach point";
            case 1: return "Target: Student courier with casualty records";
            case 2: return "Target: Avoid sweeping BTR-80 searchlight";
            case 3: return "Target: Foreign correspondent at Library gate";
            default: return "Evidence secured for independent press";
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
        return 4;
    }

    @Override
    public String getContextPrompt(Vector3 playerPos) {
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 3.5f) {
            return "[E] SLIP PAST CHECKPOINT BREACH";
        }
        if (objectivesDone[0] && !objectivesDone[1] && playerPos.dst(waypoints[1]) <= 3.2f) {
            return "[E] COLLECT DOCUMENTATION DOSSIER FROM COURIER";
        }
        if (carryingDossier && !objectivesDone[2] && playerPos.dst(waypoints[2]) <= 4.0f) {
            return "[E] CROSS APC SECTOR IN SHADOW";
        }
        if (objectivesDone[2] && !objectivesDone[3] && playerPos.dst(waypoints[3]) <= 4.2f) {
            return "[E] HAND-DELIVER EVIDENCE DOSSIER TO JOURNALIST";
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 3.5f) {
            objectivesDone[0] = true;
            hud.triggerMissionToast("CHECKPOINT INFILTRATED", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[0] && !objectivesDone[1] && playerPos.dst(waypoints[1]) <= 3.2f) {
            objectivesDone[1] = true;
            carryingDossier = true;
            hud.triggerMissionToast("CASUALTY DOSSIER RETRIEVED", getCompletedObjectiveCount());
            return true;
        }
        if (carryingDossier && !objectivesDone[2] && playerPos.dst(waypoints[2]) <= 4.0f) {
            objectivesDone[2] = true;
            hud.triggerMissionToast("APC PATROL BYPASSED", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[2] && !objectivesDone[3] && playerPos.dst(waypoints[3]) <= 4.2f) {
            objectivesDone[3] = true;
            hud.triggerMissionToast("EVIDENCE SECURED WITH PRESS", getCompletedObjectiveCount());
            hud.triggerVictory();
            return true;
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
        return false; // Stealth & covert courier
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

        // Curfew night environment: deep blue-black ambient with solitary warm sodium streetlights
        nightEnvironment = new Environment();
        nightEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.14f, 0.18f, 1f));
        DirectionalLight moon = new DirectionalLight().set(0.18f, 0.22f, 0.28f, -0.3f, -0.9f, -0.3f);
        nightEnvironment.add(moon);

        // Build BTR-80 APC and Searchlight
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;
        Material apcArmor = new Material(ColorAttribute.createDiffuse(new Color(0.24f, 0.30f, 0.22f, 1f))); // Olive drab
        Material wheelRubber = new Material(ColorAttribute.createDiffuse(new Color(0.12f, 0.12f, 0.14f, 1f)));
        Material turretSteel = new Material(ColorAttribute.createDiffuse(new Color(0.20f, 0.26f, 0.18f, 1f)));
        Material searchlightGlass = new Material(
            ColorAttribute.createDiffuse(new Color(1f, 1f, 0.9f, 1f)),
            ColorAttribute.createEmissive(new Color(0.85f, 0.85f, 0.70f, 1f))
        );
        Material beamMat = new Material(
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.24f),
            ColorAttribute.createDiffuse(new Color(1f, 0.95f, 0.80f, 0.24f)),
            ColorAttribute.createEmissive(new Color(0.40f, 0.38f, 0.25f, 0.24f))
        );
        Material sandbagMat = new Material(ColorAttribute.createDiffuse(new Color(0.55f, 0.48f, 0.35f, 1f)));

        Model apcModel = MilitaryVehicleBuilder.buildAPC(mb, apcArmor, wheelRubber, turretSteel, searchlightGlass, attr);
        Model beamModel = MilitaryVehicleBuilder.buildSearchlightBeam(mb, beamMat, 22.0f, 4.5f, attr);
        Model sandbagModel = BarricadeBuilder.buildSandbagRevetment(mb, 5.5f, 1.2f, sandbagMat, attr);
        models.add(apcModel);
        models.add(beamModel);
        models.add(sandbagModel);

        // APC stationed at crossroads (0, 0, 10)
        apcInstance = new ModelInstance(apcModel);
        apcInstance.transform.setTranslation(0f, 0f, 10f);
        instances.add(apcInstance);
        colliders.add(new float[] { -1.8f, 0f, 6.5f, 1.8f, 3.0f, 13.5f });

        // Searchlight beam
        searchlightInstance = new ModelInstance(beamModel);
        instances.add(searchlightInstance);

        // Sandbag revetments blocking side streets
        Vector3[] sbp = {
            new Vector3(-6f, 0f, 48f),
            new Vector3(6f, 0f, 48f),
            new Vector3(-12f, 0f, 14f)
        };
        for (Vector3 p : sbp) {
            ModelInstance sbi = new ModelInstance(sandbagModel);
            sbi.transform.setTranslation(p);
            instances.add(sbi);
            colliders.add(new float[] { p.x - 2.8f, 0f, p.z - 0.4f, p.x + 2.8f, 1.3f, p.z + 0.4f });
        }

        for (float[] b : world.getColliders()) colliders.add(b);
        player.setExtraColliders(colliders);
        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(4.5f);
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        if (world != null) world.update(delta);

        // Sweeping searchlight cone animation
        searchlightAngle += delta * 0.8f;
        float sweepDeg = MathUtils.sin(searchlightAngle) * 45f;
        if (searchlightInstance != null) {
            searchlightInstance.transform.idt()
                .translate(0.45f, 2.5f, 10.8f)
                .rotate(Vector3.Y, sweepDeg)
                .rotate(Vector3.X, 15f);
        }
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        if (apcInstance != null) shadowBatch.render(apcInstance);
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment env) {
        if (world != null) world.render(modelBatch);
        for (ModelInstance mi : instances) modelBatch.render(mi, nightEnvironment);
    }

    @Override
    public void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH) {
    }

    @Override
    public java.util.List<float[]> getColliders() {
        return colliders;
    }

    @Override
    public Environment getEnvironment() {
        return nightEnvironment;
    }

    @Override
    public boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud) {
        return false;
    }

    @Override
    public DhakaCampusWorld getWorld() {
        return world;
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
    }
}
