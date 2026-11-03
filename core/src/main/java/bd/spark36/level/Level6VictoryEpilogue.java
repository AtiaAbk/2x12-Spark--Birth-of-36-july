// ei file ta movement er bijoy ar epilogue er shesh level
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
import bd.spark36.world.landmarks.ShaheedMinarBuilder;

public class Level6VictoryEpilogue implements CampaignLevel {

    private DhakaCampusWorld world;
    private final StreetDressing street = new StreetDressing(6);
    private CampusCrowd crowd;
    private Environment goldenEnvironment;

    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();

    private final boolean[] objectivesDone = new boolean[4];
    private final Vector3[] waypoints = {
        new Vector3(0f, 0f, 48f),
        new Vector3(3.6f, 0f, -16.8f),
        new Vector3(66f, 0f, 20f),
        new Vector3(0f, 0f, -22f)
    };

    private static final String[] CHECKLIST = {
        "1. Lead Victory March Through Liberated Gates",
        "2. Lay Marigold Floral Wreath for Martyrs",
        "3. Ring the Bell of Freedom at TSC",
        "4. Conclude the 36 July Epilogue & Documentary Credits"
    };

    @Override
    public int getLevelNumber() {
        return 6;
    }

    @Override
    public String getTitle() {
        return "36 JULY: THE TURNING POINT";
    }

    @Override
    public String getSubtitle() {
        return "৩৬ জুলাই ও ঐতিহাসিক বিজয়";
    }

    @Override
    public String getHistoricalDate() {
        return "August 5, 2024 (36 July)";
    }

    @Override
    public String getLocationName() {
        return "Dhaka University — Liberated Campus";
    }

    @Override
    public String getMissionTag() {
        return String.format("ACTIVE MISSION: %02d / 04", Math.min(getCompletedObjectiveCount() + 1, 4));
    }

    @Override
    public String getMissionTitle() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "LEAD THE LONG MARCH";
            case 1: return "HONOR THE FALLEN MARTYRS";
            case 2: return "RING THE BELL OF FREEDOM";
            case 3: return "VIEW HISTORICAL EPILOGUE";
            default: return "36 JULY TRIUMPH COMPLETE";
        }
    }

    @Override
    public String getMissionTarget() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "Target: Liberated campus entrance on promenade";
            case 1: return "Target: Floral memorial for Abu Sayed, Mir Mugdho & heroes";
            case 2: return "Target: Historic Teacher-Student Centre (TSC)";
            case 3: return "Target: Permanent historical credits and archive";
            default: return "The people have reclaimed their sovereignty";
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
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 4.0f) {
            return "[E] MARCH THROUGH LIBERATED GATES";
        }
        if (objectivesDone[0] && !objectivesDone[1] && playerPos.dst(waypoints[1]) <= 3.8f) {
            return "[E] LAY MARIGOLD FLORAL WREATH AT MEMORIAL";
        }
        if (objectivesDone[1] && !objectivesDone[2] && playerPos.dst(waypoints[2]) <= 4.5f) {
            return "[E] RING THE BELL OF FREEDOM";
        }
        if (objectivesDone[2] && !objectivesDone[3] && playerPos.dst(waypoints[3]) <= 4.0f) {
            return "[E] VIEW 36 JULY DOCUMENTARY EPILOGUE & CREDITS";
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        if (!objectivesDone[0] && playerPos.dst(waypoints[0]) <= 4.0f) {
            objectivesDone[0] = true;
            hud.triggerMissionToast("LIBERATED GATES BREACHED", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[0] && !objectivesDone[1] && playerPos.dst(waypoints[1]) <= 3.8f) {
            objectivesDone[1] = true;
            hud.triggerMissionToast("MARTYRS HONORED WITH FLOWERS", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[1] && !objectivesDone[2] && playerPos.dst(waypoints[2]) <= 4.5f) {
            objectivesDone[2] = true;
            hud.triggerMissionToast("BELL OF FREEDOM RUNG", getCompletedObjectiveCount());
            return true;
        }
        if (objectivesDone[2] && !objectivesDone[3] && playerPos.dst(waypoints[3]) <= 4.0f) {
            objectivesDone[3] = true;
            hud.triggerMissionToast("36 JULY VICTORY ACCOMPLISHED!", getCompletedObjectiveCount());
            hud.triggerVictory();
            hud.openCredits("36 July Epilogue");
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
        return new Vector3(0f, 0f, 72f);
    }

    @Override
    public float getSpawnHeading() {
        return 180f;
    }

    @Override
    public void init(TextureFactory textures, PlayerController player, CinematicCamera camera) {
        this.world = new DhakaCampusWorld(textures);

        goldenEnvironment = new Environment();
        goldenEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.55f, 0.52f, 0.45f, 1f));
        DirectionalLight goldSun = new DirectionalLight().set(1.0f, 0.92f, 0.75f, -0.3f, -0.85f, -0.3f);
        goldenEnvironment.add(goldSun);

        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;
        Material marigoldMat = new Material(ColorAttribute.createDiffuse(new Color(0.98f, 0.65f, 0.08f, 1f)));
        Material roseMat = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.12f, 0.18f, 1f)));
        Material whiteMarble = new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.92f, 1f)));
        Material bellBronze = new Material(ColorAttribute.createDiffuse(new Color(0.82f, 0.68f, 0.32f, 1f)));

        mb.begin();
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder pAltar = mb.part("altar", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, whiteMarble);
        pAltar.box(0, 0.3f, 0, 3.2f, 0.6f, 2.2f);

        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder pFlowers = mb.part("flowers", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, marigoldMat);
        for (int i = 0; i < 16; i++) {
            float ang = i * 22.5f * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            pFlowers.box(com.badlogic.gdx.math.MathUtils.cos(ang) * 0.85f, 0.65f, com.badlogic.gdx.math.MathUtils.sin(ang) * 0.85f, 0.24f, 0.12f, 0.24f);
        }
        Model altarModel = mb.end();
        models.add(altarModel);

        ModelInstance ai = new ModelInstance(altarModel);
        ai.transform.setTranslation(waypoints[1]);
        instances.add(ai);

        mb.begin();
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder pBellFrame = mb.part("frame", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, whiteMarble);
        pBellFrame.box(0, 2.2f, 0, 0.25f, 4.4f, 0.25f);
        pBellFrame.box(1.8f, 2.2f, 0, 0.25f, 4.4f, 0.25f);
        pBellFrame.box(0.9f, 4.2f, 0, 2.0f, 0.25f, 0.25f);

        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder pBell = mb.part("bell", com.badlogic.gdx.graphics.GL20.GL_TRIANGLES, attr, bellBronze);
        pBell.box(0.9f, 3.4f, 0, 0.75f, 0.95f, 0.75f);
        Model bellModel = mb.end();
        models.add(bellModel);

        ModelInstance bi = new ModelInstance(bellModel);
        bi.transform.setTranslation(waypoints[2]);
        instances.add(bi);

        street.build(colliders);
        for (float[] b : world.getColliders()) colliders.add(b);
        player.setExtraColliders(colliders);
        LevelPbrMaterials.adopt(instances);
        this.crowd = new CampusCrowd(20, player.getColliders());
        this.crowd.setLevelProfile(6);
        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(4.5f);
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        if (world != null) world.update(delta);
        if (crowd != null) crowd.update(delta, player.getPosition());

        if (!epilogueTestDone && bd.spark36.hud.screens.CreditsScreen.epilogueTestRequested()) {
            epilogueTestTime += delta;
            if (epilogueTestTime >= 1.5f) {
                epilogueTestDone = true;
                objectivesDone[0] = objectivesDone[1] = objectivesDone[2] = true;
                onInteract(waypoints[3], player, hud);
            }
        }
    }

    private boolean epilogueTestDone = false;
    private float epilogueTestTime = 0f;

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        street.renderShadow(shadowBatch);
        for (ModelInstance mi : instances) shadowBatch.render(mi);
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment env) {
        if (world != null) world.render(modelBatch);
        if (world != null) street.render(modelBatch, world.getEnvironment());
        for (ModelInstance mi : instances) modelBatch.render(mi, getEnvironment());
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

        return world != null ? world.getEnvironment() : goldenEnvironment;
    }

    @Override
    public boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud) {
        if (player == null || !player.isAttacking()) return false;
        String violation = CivilianStrikeRule.check(crowd, player.getPosition(), player.getHeadingDegrees(), CivilianStrikeRule.CIVILIAN_VIOLATION);
        if (violation != null) {
            if (hud != null) {
                hud.disqualify("You attacked a protected campus member.");
            }
            bd.spark36.mission.MissionManager.get().abortMission("You attacked a protected campus member.");
            return false;
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
    public void restoreObjectiveFlags(boolean[] flags) {
        for (int i = 0; i < objectivesDone.length; i++) {
            objectivesDone[i] = flags != null && i < flags.length && flags[i];
        }
    }

    @Override
    public void completeAllObjectivesForTest() {
        java.util.Arrays.fill(objectivesDone, true);
    }

    @Override
    public void dispose() {
        street.dispose();
        if (world != null) { world.dispose(); world = null; }
        for (Model m : models) m.dispose();
        models.clear();
    }
}
