package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
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
import bd.spark36.world.landmarks.ShaheedMinarBuilder;

/**
 * LEVEL 5: "The Wall of People" (জনসমুদ্র)
 * Period: August 3 – 4, 2024 (Central Shaheed Minar historic mass assembly & One-Point Demand).
 * Environment: Central Shaheed Minar Plaza with crimson solar disk and massive gathered assemblies.
 * Focus: High-density crowd navigation, forming defensive human chains, frontline medical logistics.
 * Combat: Mass collective solidarity & frontline civilian protection.
 */
public class Level5MassMobilization implements CampaignLevel {

    private DhakaCampusWorld world;
    private CampusCrowd crowd;
    private Environment sunsetEnvironment;

    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();

    // Shaheed Minar monument position
    private final Vector3 minarPos = new Vector3(0f, 0f, -8f);

    // Objectives:
    // 1. Ascend Shaheed Minar central staircase to the main dais
    // 2. Link arms to form the defensive human chain (East flank)
    // 3. Deliver emergency medical supplies to volunteer clinic
    // 4. Stand with leaders on dais to declare the One-Point Demand
    private final boolean[] objectivesDone = new boolean[4];
    private final Vector3[] waypoints = {
        new Vector3(0f, 1.2f, 0.5f),   // Main Dais Staircase
        new Vector3(12f, 0f, 8f),       // Human Chain East Flank
        new Vector3(-16f, 0f, 12f),     // Medical Volunteer Clinic
        new Vector3(0f, 1.4f, -4f)      // Central Speakers' Podium
    };

    private static final String[] CHECKLIST = {
        "1. Ascend to Central Shaheed Minar Dais",
        "2. Form Defensive Human Chain on East Flank",
        "3. Deliver Medical Supplies to Volunteer Clinic",
        "4. Announce Historic One-Point Demand with Assembly"
    };

    @Override
    public int getLevelNumber() {
        return 5;
    }

    @Override
    public String getTitle() {
        return "THE WALL OF PEOPLE";
    }

    @Override
    public String getSubtitle() {
        return "জনসমুদ্র ও এক দফা";
    }

    @Override
    public String getHistoricalDate() {
        return "August 3 – 4, 2024";
    }

    @Override
    public String getLocationName() {
        return "Central Shaheed Minar — Dhaka";
    }

    @Override
    public String getMissionTag() {
        return String.format("ACTIVE MISSION: %02d / 04", Math.min(getCompletedObjectiveCount() + 1, 4));
    }

    @Override
    public String getMissionTitle() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "ASCEND TO CENTRAL DAIS";
            case 1: return "REINFORCE DEFENSIVE HUMAN CHAIN";
            case 2: return "DELIVER VOLUNTEER MEDICAL AID";
            case 3: return "DECLARE THE ONE-POINT DEMAND";
            default: return "ONE-POINT DEMAND PROCLAIMED";
        }
    }

    @Override
    public String getMissionTarget() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "Target: Stepped marble plinth of Shaheed Minar";
            case 1: return "Target: Defensive perimeter line holding off agitators";
            case 2: return "Target: Volunteer field medical booth";
            case 3: return "Target: Central podium facing hundreds of thousands";
            default: return "Historic assembly concluded in unity";
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
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 3.8f) {
            return "[E] ASCEND CEREMONIAL STAIRS TO DAIS";
        }
        if (objectivesDone[0] && !objectivesDone[1] && playerPos.dst(waypoints[1]) <= 4.0f) {
            return "[E] LINK ARMS TO HOLD THE HUMAN CHAIN";
        }
        if (objectivesDone[1] && !objectivesDone[2] && playerPos.dst(waypoints[2]) <= 4.2f) {
            return "[E] DELIVER FIRST-AID SUPPLIES TO CLINIC";
        }
        if (objectivesDone[2] && !objectivesDone[3] && playerPos.dst(waypoints[3]) <= 3.8f) {
            return "[E] STAND TOGETHER — PROCLAIM ONE-POINT DEMAND";
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 3.8f) {
            objectivesDone[0] = true;
            hud.triggerMissionToast("SHAHEED MINAR DAIS REACHED", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[0] && !objectivesDone[1] && playerPos.dst(waypoints[1]) <= 4.0f) {
            objectivesDone[1] = true;
            hud.triggerMissionToast("HUMAN CHAIN REINFORCED", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[1] && !objectivesDone[2] && playerPos.dst(waypoints[2]) <= 4.2f) {
            objectivesDone[2] = true;
            hud.triggerMissionToast("MEDICAL AID DELIVERED", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[2] && !objectivesDone[3] && playerPos.dst(waypoints[3]) <= 3.8f) {
            objectivesDone[3] = true;
            hud.triggerMissionToast("ONE-POINT DEMAND PROCLAIMED!", getCompletedObjectiveCount());
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
        return false;
    }

    @Override
    public Vector3 getSpawnPosition() {
        return new Vector3(0f, 0f, 32f); // Approaching Shaheed Minar from south
    }

    @Override
    public float getSpawnHeading() {
        return 180f; // Facing north toward Shaheed Minar
    }

    @Override
    public void init(TextureFactory textures, PlayerController player, CinematicCamera camera) {
        this.world = new DhakaCampusWorld(textures);

        // Majestic dusk/sunset lighting: warm golden-orange and crimson tones
        sunsetEnvironment = new Environment();
        sunsetEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.48f, 0.38f, 0.42f, 1f));
        DirectionalLight sunsetSun = new DirectionalLight().set(0.98f, 0.62f, 0.38f, -0.4f, -0.6f, -0.4f);
        sunsetEnvironment.add(sunsetSun);

        // Build Central Shaheed Minar Monument
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;
        Material marbleMat = new Material(ColorAttribute.createDiffuse(new Color(0.96f, 0.96f, 0.94f, 1f)));
        Material sunMat = new Material(
            ColorAttribute.createDiffuse(new Color(0.88f, 0.08f, 0.12f, 1f)),
            ColorAttribute.createEmissive(new Color(0.45f, 0.04f, 0.06f, 1f))
        );
        Material redBrickMat = new Material(ColorAttribute.createDiffuse(new Color(0.68f, 0.28f, 0.22f, 1f)));
        Material goldMat = new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.78f, 0.30f, 1f)));

        Model minarModel = ShaheedMinarBuilder.buildShaheedMinar(mb, marbleMat, sunMat, redBrickMat, goldMat, attr);
        models.add(minarModel);

        ModelInstance mi = new ModelInstance(minarModel);
        mi.transform.setTranslation(minarPos);
        instances.add(mi);

        // Add medical clinic tent
        Material canvasMat = new Material(ColorAttribute.createDiffuse(new Color(0.92f, 0.90f, 0.85f, 1f)));
        Material frameMat = new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.28f, 0.32f, 1f)));
        Material redCrossMat = new Material(ColorAttribute.createDiffuse(new Color(0.85f, 0.12f, 0.14f, 1f)));
        Model tentModel = BarricadeBuilder.buildTriageTent(mb, 4.2f, canvasMat, frameMat, redCrossMat, attr);
        models.add(tentModel);

        ModelInstance ti = new ModelInstance(tentModel);
        ti.transform.setTranslation(waypoints[2]);
        instances.add(ti);

        // Colliders for Shaheed Minar base plinth
        colliders.add(new float[] { minarPos.x - 11f, 0f, minarPos.z - 8f,
                                    minarPos.x + 11f, 1.5f, minarPos.z + 8f });

        for (float[] b : world.getColliders()) colliders.add(b);
        player.setExtraColliders(colliders);
        this.crowd = new CampusCrowd(24, player.getColliders());
        this.crowd.setLevelProfile(5);
        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(5.0f);
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        if (world != null) world.update(delta);
        if (crowd != null) crowd.update(delta, player.getPosition());
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        for (ModelInstance mi : instances) shadowBatch.render(mi);
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment env) {
        if (world != null) world.render(modelBatch);
        for (ModelInstance mi : instances) modelBatch.render(mi, sunsetEnvironment);
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
        return sunsetEnvironment;
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
    }
}
