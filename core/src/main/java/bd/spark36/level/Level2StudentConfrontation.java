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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.HelmetGoon;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.TextureFactory;
import bd.spark36.world.landmarks.BarricadeBuilder;
import bd.spark36.mission.MissionObjective;
import bd.spark36.mission.MissionTracker;
import bd.spark36.mission.ObjectiveType;

/**
 * LEVEL 2: "The Day the Streets Changed" (যেদিন পথ বদলে গেল)
 * Period: July 15, 2024 (Armed counter-protesters attack peaceful students at DU).
 * Environment: Barricaded Raju Anti-Terrorism Sculpture & Arts Faculty Crossroads.
 * Focus: Non-lethal martial arts self-defense, dodging attacks, rescuing wounded students to medical triage.
 * Combat: Active martial arts self-defense against Helmeted Aggressors. Hitting innocents = instant failure.
 */
public class Level2StudentConfrontation implements CampaignLevel {

    private DhakaCampusWorld world;
    private final Array<HelmetGoon> aggressors = new Array<>();
    private CampusCrowd crowd;
    private Environment environment;
    private final MissionTracker missionTracker = new MissionTracker(true);

    private final Array<Model> barricadeModels = new Array<>();
    private final Array<ModelInstance> barricadeInstances = new Array<>();
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();

    // Objectives:
    // 1. Disarm Aggressor 1 (Raju Sculpture East)
    // 2. Disarm Aggressor 2 (Raju Sculpture West)
    // 3. Disarm Aggressor 3 (Arts Roadway)
    // 4. Rescue Injured Student 1 (Near Raju Base)
    // 5. Rescue Injured Student 2 (Arts Faculty Gate)
    private final boolean[] objectivesDone = new boolean[5];
    private final Vector3[] objectiveWaypoints = {
        new Vector3(44f, 0f, 38f),  // Aggressor 1
        new Vector3(38f, 0f, 44f),  // Aggressor 2
        new Vector3(22f, 0f, 32f),  // Aggressor 3
        new Vector3(44f, 0f, 42f),  // Injured Student 1
        new Vector3(-20f, 0f, 28f)  // Injured Student 2
    };

    private static final String[] CHECKLIST = {
        "1. Disarm Aggressors at Raju Roundabout",
        "2. Neutralize Flanking Attacker",
        "3. Clear Arts Faculty Approach Road",
        "4. Triage Injured Student at Raju Sculpture",
        "5. Secure Emergency Medical Triage Station"
    };

    public Level2StudentConfrontation() {
        initDefaultObjectives();
    }

    private void initDefaultObjectives() {
        missionTracker.reset();
        missionTracker.addObjective(new MissionObjective("l2_1", "DEFEND RAJU ROUNDABOUT", "Target: Helmeted Aggressor near Raju Sculpture", CHECKLIST[0], ObjectiveType.TRIGGER_EVENT, objectiveWaypoints[0], 4.5f, true));
        missionTracker.addObjective(new MissionObjective("l2_2", "INTERCEPT FLANKING AGGRESSOR", "Target: Secondary Aggressor on west roadway", CHECKLIST[1], ObjectiveType.TRIGGER_EVENT, objectiveWaypoints[1], 4.5f, false));
        missionTracker.addObjective(new MissionObjective("l2_3", "SECURE ARTS FACULTY APPROACH", "Target: Roadway barricade enforcer", CHECKLIST[2], ObjectiveType.TRIGGER_EVENT, objectiveWaypoints[2], 4.5f, false));
        missionTracker.addObjective(new MissionObjective("l2_4", "TRIAGE INJURED STUDENT", "Target: Fallen student requiring first aid", CHECKLIST[3], ObjectiveType.INTERACT, objectiveWaypoints[3], 3.5f, false));
        missionTracker.addObjective(new MissionObjective("l2_5", "EVACUATE TO MEDICAL TENT", "Target: Emergency Medical Triage Tent", CHECKLIST[4], ObjectiveType.INTERACT, objectiveWaypoints[4], 4.0f, false));
    }

    private float hitStopTimer = 0f;
    private boolean attackHitProcessed = false;

    @Override
    public int getLevelNumber() {
        return 2;
    }

    @Override
    public String getTitle() {
        return "THE DAY THE STREETS CHANGED";
    }

    @Override
    public String getSubtitle() {
        return "যেদিন পথ বদলে গেল";
    }

    @Override
    public String getHistoricalDate() {
        return "July 15, 2024";
    }

    @Override
    public String getLocationName() {
        return "Raju Memorial Sculpture — TSC Roundabout";
    }

    @Override
    public String getMissionTag() {
        int done = getCompletedObjectiveCount();
        return String.format("ACTIVE MISSION: %02d / 05", Math.min(done + 1, 5));
    }

    @Override
    public String getMissionTitle() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "DEFEND RAJU ROUNDABOUT";
            case 1: return "INTERCEPT FLANKING AGGRESSOR";
            case 2: return "SECURE ARTS FACULTY APPROACH";
            case 3: return "TRIAGE INJURED STUDENT";
            case 4: return "EVACUATE TO MEDICAL TENT";
            default: return "CAMPUS DEFENDED";
        }
    }

    @Override
    public String getMissionTarget() {
        int next = getNextObjectiveIndex();
        switch (next) {
            case 0: return "Target: Helmeted Aggressor near Raju Sculpture";
            case 1: return "Target: Secondary Aggressor on west roadway";
            case 2: return "Target: Roadway barricade enforcer";
            case 3: return "Target: Fallen student requiring first aid";
            case 4: return "Target: Emergency Medical Triage Tent";
            default: return "All objectives secured";
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
        int idx = getNextObjectiveIndex();
        return objectiveWaypoints[idx];
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
        int count = 0;
        for (boolean b : objectivesDone) if (b) count++;
        return count;
    }

    @Override
    public int getTotalObjectiveCount() {
        return 5;
    }

    @Override
    public String getContextPrompt(Vector3 playerPos) {
        // Injured student 1 near Raju base
        if (!objectivesDone[3] && playerPos.dst(objectiveWaypoints[3]) <= 3.5f) {
            return "[E] APPLY FIRST AID & STABILIZE INJURED STUDENT";
        }
        // Medical tent near Arts Gate
        if (!objectivesDone[4] && playerPos.dst(objectiveWaypoints[4]) <= 4.0f) {
            return "[E] COORDINATE EMERGENCY MEDICAL SHELTER";
        }
        if (crowd != null && crowd.getClosestInteractable(playerPos, 2.6f) != null) {
            return "[E] TALK TO DU STUDENT";
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        // Injured student 1
        if (!objectivesDone[3] && playerPos.dst(objectiveWaypoints[3]) <= 3.5f) {
            objectivesDone[3] = true;
            missionTracker.completeObjective(3);
            hud.triggerMissionToast("FIRST AID ADMINISTERED", getCompletedObjectiveCount());
            checkCompletion(hud);
            return true;
        }
        // Medical tent
        if (!objectivesDone[4] && playerPos.dst(objectiveWaypoints[4]) <= 4.0f) {
            objectivesDone[4] = true;
            missionTracker.completeObjective(4);
            hud.triggerMissionToast("MEDICAL SHELTER SECURED", getCompletedObjectiveCount());
            checkCompletion(hud);
            return true;
        }
        // Student crowd interaction
        if (crowd != null && crowd.interactWithClosest(playerPos, 2.6f, getLevelNumber(), hud)) {
            return true;
        }
        return false;
    }

    private void checkCompletion(WhereWindsMeetHUD hud) {
        if (isLevelComplete()) {
            hud.triggerVictory();
        }
    }

    @Override
    public boolean isLevelComplete() {
        for (boolean b : objectivesDone) if (!b) return false;
        return true;
    }

    @Override
    public boolean isCombatEnabled() {
        return true;
    }

    @Override
    public Vector3 getSpawnPosition() {
        return new Vector3(44f, 0f, 26f); // Road approach to Raju Sculpture
    }

    @Override
    public float getSpawnHeading() {
        return 180f; // Facing Raju Sculpture
    }

    @Override
    public void init(TextureFactory textures, PlayerController player, CinematicCamera camera) {
        this.world = new DhakaCampusWorld(textures);

        if (missionTracker.getTotalCount() == 0) {
            initDefaultObjectives();
        } else {
            missionTracker.reset();
        }

        // Overcast, stormy monsoon environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.42f, 0.46f, 1f));
        DirectionalLight stormSun = new DirectionalLight().set(0.68f, 0.65f, 0.72f, -0.4f, -0.8f, -0.4f);
        environment.add(stormSun);

        // Spawn 3 Helmeted Aggressors
        HelmetGoon g1 = new HelmetGoon(textures);
        g1.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        g1.reset(objectiveWaypoints[0].x, objectiveWaypoints[0].z);
        aggressors.add(g1);

        HelmetGoon g2 = new HelmetGoon(textures);
        g2.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        g2.reset(objectiveWaypoints[1].x, objectiveWaypoints[1].z);
        aggressors.add(g2);

        HelmetGoon g3 = new HelmetGoon(textures);
        g3.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        g3.reset(objectiveWaypoints[2].x, objectiveWaypoints[2].z);
        aggressors.add(g3);

        // Build Barricades and Medical Triage Tent
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;
        Material woodMat = new Material(ColorAttribute.createDiffuse(new Color(0.48f, 0.32f, 0.18f, 1f)));
        Material metalMat = new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.28f, 0.32f, 1f)));
        Material canvasMat = new Material(ColorAttribute.createDiffuse(new Color(0.92f, 0.90f, 0.85f, 1f)));
        Material redCrossMat = new Material(ColorAttribute.createDiffuse(new Color(0.85f, 0.12f, 0.14f, 1f)));

        Model barricadeModel = BarricadeBuilder.buildBarricade(mb, 4.2f, 1.35f, woodMat, metalMat, attr);
        Model tentModel = BarricadeBuilder.buildTriageTent(mb, 4.5f, canvasMat, metalMat, redCrossMat, attr);
        barricadeModels.add(barricadeModel);
        barricadeModels.add(tentModel);

        // Place barricades blocking road junctions around Raju roundabout
        Vector3[] barPositions = {
            new Vector3(44f, 0f, 48f),
            new Vector3(36f, 0f, 36f),
            new Vector3(52f, 0f, 36f)
        };
        for (Vector3 bp : barPositions) {
            ModelInstance bi = new ModelInstance(barricadeModel);
            bi.transform.setTranslation(bp);
            barricadeInstances.add(bi);
            colliders.add(new float[] { bp.x - 2.1f, 0f, bp.z - 0.5f, bp.x + 2.1f, 1.4f, bp.z + 0.5f });
        }

        // Place Medical Triage Tent near Arts Gate
        ModelInstance ti = new ModelInstance(tentModel);
        ti.transform.setTranslation(objectiveWaypoints[4]);
        barricadeInstances.add(ti);
        colliders.add(new float[] { objectiveWaypoints[4].x - 2.5f, 0f, objectiveWaypoints[4].z - 2.5f,
                                    objectiveWaypoints[4].x + 2.5f, 3.5f, objectiveWaypoints[4].z + 2.5f });

        // Combine colliders
        for (float[] b : world.getColliders()) {
            colliders.add(b);
        }
        player.setExtraColliders(colliders);
        this.crowd = new CampusCrowd(12, player.getColliders());
        this.crowd.setLevelProfile(2);
        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(4.5f);
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        if (hitStopTimer > 0f) {
            hitStopTimer -= delta;
            delta *= 0.15f;
        }

        if (world != null) world.update(delta);
        if (crowd != null) crowd.update(delta, player.getPosition());

        // Update aggressors
        HelmetGoon closestAggressor = null;
        float closestDst = Float.MAX_VALUE;

        for (int i = 0; i < aggressors.size; i++) {
            HelmetGoon g = aggressors.get(i);
            if (canMove) {
                g.update(delta, player);
            }
            if (!g.isKnockedOut()) {
                float d = player.getPosition().dst(g.getPosition());
                if (d < closestDst) {
                    closestDst = d;
                    closestAggressor = g;
                }
            } else if (!objectivesDone[i]) {
                objectivesDone[i] = true;
                hud.triggerMissionToast("AGGRESSOR NEUTRALIZED", getCompletedObjectiveCount());
                checkCompletion(hud);
            }
        }

        if (closestAggressor != null && closestDst <= 18f) {
            hud.setHelmetGoon(closestAggressor);
            player.setCombatTarget(closestAggressor.getPosition());
        } else {
            hud.setHelmetGoon(null);
            player.clearCombatTarget();
        }

        // Knockout check for player
        if (player.isKnockedOut() && !hud.isDefeated()) {
            hud.setDefeated(true);
        }
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        for (HelmetGoon g : aggressors) g.render(shadowBatch, null);
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment env) {
        if (world != null) world.render(modelBatch);
        for (ModelInstance mi : barricadeInstances) modelBatch.render(mi, environment);
        for (HelmetGoon g : aggressors) g.render(modelBatch, environment);
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
        return (environment != null) ? environment : world.getEnvironment();
    }

    @Override
    public boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud) {
        if (!player.isAttacking()) {
            attackHitProcessed = false;
            return false;
        }

        float prog = player.getAttackProgress();
        if (prog >= 0.40f && prog <= 0.68f && !attackHitProcessed) {
            attackHitProcessed = true;

            // Campus Peace Rule: Hitting unarmed peers is strictly forbidden
            if (crowd != null && crowd.checkHitByPlayer(player.getPosition(), player.getHeadingDegrees(), 2.2f)) {
                hud.setDisqualified(true, "VIOLATION: You struck an innocent campus student! Unprovoked violence against unarmed peers is strictly forbidden.");
                return true;
            }

            // Martial arts strike against active aggressor
            for (int i = 0; i < aggressors.size; i++) {
                HelmetGoon g = aggressors.get(i);
                if (g.isKnockedOut()) continue;

                float dst = player.getPosition().dst(g.getPosition());
                if (dst <= 2.8f) {
                    boolean isSweep = (player.getAttackCombo() == 5);
                    boolean isKick = (player.getAttackCombo() == 3 || player.getAttackCombo() == 4);
                    float dmg = isSweep ? 34f : (isKick ? 38f : 24f);
                    hitStopTimer = isSweep ? 0.065f : (isKick ? 0.052f : 0.038f);
                    camera.addShake(isSweep ? 0.10f : (isKick ? 0.07f : 0.04f));

                    if (isKick || isSweep) {
                        bd.spark36.audio.SoundManager.get().playKick();
                    } else {
                        bd.spark36.audio.SoundManager.get().playPunch();
                    }

                    boolean ko = g.takeHitFromPlayer(dmg, isKick, isSweep, player.getHeadingDegrees());
                    if (ko) {
                        camera.addShake(0.24f);
                        objectivesDone[i] = true;
                        missionTracker.completeObjective(i);
                        hud.triggerMissionToast("AGGRESSOR DISARMED", getCompletedObjectiveCount());
                        checkCompletion(hud);
                    }
                    return true;
                }
            }
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
    public MissionTracker getMissionTracker() {
        return missionTracker;
    }

    @Override
    public void completeAllObjectivesForTest() {
        java.util.Arrays.fill(objectivesDone, true);
        if (missionTracker != null) missionTracker.completeAllForTest();
    }

    @Override
    public void dispose() {
        if (world != null) { world.dispose(); world = null; }
        for (HelmetGoon g : aggressors) g.dispose();
        aggressors.clear();
        for (Model m : barricadeModels) m.dispose();
        barricadeModels.clear();
    }
}
