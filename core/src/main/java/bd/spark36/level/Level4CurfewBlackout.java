// ei file ta curfew ar internet blackout er moddhe level
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

public class Level4CurfewBlackout implements CampaignLevel {

    private DhakaCampusWorld world;
    private final StreetDressing street = new StreetDressing(4);
    private Environment nightEnvironment;

    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final java.util.List<float[]> colliders = new java.util.ArrayList<>();

    private ModelInstance apcInstance;
    private ModelInstance searchlightInstance;

    private final boolean[] objectivesDone = new boolean[4];
    private final Vector3[] waypoints = {
        new Vector3(0f, 0f, 48f),
        new Vector3(14f, 0f, 22f),
        new Vector3(0f, 0f, 6f),
        new Vector3(-42f, 0f, 18f)
    };

    private static final String[] CHECKLIST = {
        "1. Infiltrate Past Outer Checkpoint Sandbags",
        "2. Retrieve Secret Casualty Dossier from Courier",
        "3. Bypass Armoured Vehicle Searchlight Zone",
        "4. Hand-Deliver Evidence to Press Contact"
    };

    private final Array<bd.spark36.character.HelmetGoon> aggressors = new Array<>();
    private boolean attackHitProcessed = false;
    private float hitStopTimer = 0f;
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
            case 2: return "Target: Avoid sweeping armoured-vehicle searchlight";
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

    public boolean allAggressorsSubdued() {
        if (aggressors.size == 0) return false;
        for (bd.spark36.character.HelmetGoon g : aggressors) {
            if (!g.isKnockedOut()) return false;
        }
        return true;
    }

    @Override
    public boolean isLevelComplete() {
        if (allAggressorsSubdued()) return true;
        for (boolean b : objectivesDone) if (!b) return false;
        return true;
    }

    @Override
    public boolean isCombatEnabled() {
        return true;
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

        nightEnvironment = new Environment();
        nightEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.14f, 0.18f, 1f));
        DirectionalLight moon = new DirectionalLight().set(0.18f, 0.22f, 0.28f, -0.3f, -0.9f, -0.3f);
        nightEnvironment.add(moon);

        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;
        Material apcArmor = new Material(ColorAttribute.createDiffuse(new Color(0.24f, 0.30f, 0.22f, 1f)));
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

        apcInstance = new ModelInstance(apcModel);
        apcInstance.transform.setTranslation(0f, 0f, 10f);
        instances.add(apcInstance);
        colliders.add(new float[] { -1.8f, 0f, 6.5f, 1.8f, 3.0f, 13.5f });

        searchlightInstance = new ModelInstance(beamModel);
        instances.add(searchlightInstance);

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

        street.build(colliders);
        for (float[] b : world.getColliders()) colliders.add(b);
        player.setExtraColliders(colliders);
        LevelPbrMaterials.adopt(instances);
        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        aggressors.clear();
        for (int i = 0; i < 4; i++) {
            bd.spark36.character.HelmetGoon g = (textures != null) ? new bd.spark36.character.HelmetGoon(textures) : new bd.spark36.character.HelmetGoon();
            g.setRole(bd.spark36.character.HelmetGoon.Role.HELMETED_AGGRESSOR);
            g.setCombatStats(120f, 130f, 3.0f, 0.4f, 0.8f);
            float sx = (i % 2 == 0 ? -1 : 1) * (4f + i * 2f);
            float sz = 40f + (i * 3.5f);
            g.reset(sx, sz);
            g.startFightDirectly();
            aggressors.add(g);
        }

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

        searchlightAngle += delta * 0.8f;
        float sweepDeg = MathUtils.sin(searchlightAngle) * 45f;
        if (searchlightInstance != null) {
            searchlightInstance.transform.idt()
                .translate(0.45f, 2.5f, 10.8f)
                .rotate(Vector3.Y, sweepDeg)
                .rotate(Vector3.X, 15f);
        }

        for (bd.spark36.character.HelmetGoon g : aggressors) {
            g.update(delta, player);
        }

        bd.spark36.character.HelmetGoon closestAggressor = null;
        float closestDst = Float.MAX_VALUE;
        for (bd.spark36.character.HelmetGoon g : aggressors) {
            if (!g.isKnockedOut()) {
                float d = player.getPosition().dst(g.getPosition());
                if (d < closestDst) {
                    closestDst = d;
                    closestAggressor = g;
                }
            }
        }

        if (closestAggressor != null && closestDst <= 18f && hud != null) {
            hud.setHelmetGoon(closestAggressor);
            player.setCombatTarget(closestAggressor.getPosition());
        } else if (hud != null) {
            hud.setHelmetGoon(null);
            player.clearCombatTarget();
        }

        if (player.isKnockedOut() && hud != null && !hud.isDefeated()) {
            hud.setDefeated(true);
        }
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        street.renderShadow(shadowBatch);
        if (apcInstance != null) shadowBatch.render(apcInstance);
        for (bd.spark36.character.HelmetGoon g : aggressors) {
            g.render(shadowBatch, null);
        }
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment env) {
        if (world != null) world.render(modelBatch);
        if (world != null) street.render(modelBatch, world.getEnvironment());
        for (ModelInstance mi : instances) modelBatch.render(mi, getEnvironment());
        for (bd.spark36.character.HelmetGoon g : aggressors) {
            g.render(modelBatch, getEnvironment());
        }
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

        return world != null ? world.getEnvironment() : nightEnvironment;
    }

    @Override
    public boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud) {
        if (player == null || !player.isAttacking()) {
            attackHitProcessed = false;
            return false;
        }

        float prog = player.getAttackProgress();
        if (prog >= 0.40f && prog <= 0.68f && !attackHitProcessed) {
            attackHitProcessed = true;

            for (int i = 0; i < aggressors.size; i++) {
                bd.spark36.character.HelmetGoon g = aggressors.get(i);
                if (g.isKnockedOut()) continue;

                float dst = player.getPosition().dst(g.getPosition());
                if (dst <= 2.8f) {
                    boolean isSweep = (player.getAttackCombo() == 5);
                    boolean isKick = (player.getAttackCombo() == 3 || player.getAttackCombo() == 4);
                    float dmg = isSweep ? 34f : (isKick ? 38f : 24f);
                    hitStopTimer = isSweep ? 0.065f : (isKick ? 0.052f : 0.038f);
                    if (camera != null) camera.addShake(isSweep ? 0.10f : (isKick ? 0.07f : 0.04f));

                    if (isKick || isSweep) {
                        bd.spark36.audio.SoundManager.get().playKick();
                    } else {
                        bd.spark36.audio.SoundManager.get().playPunch();
                    }

                    boolean ko = g.takeHitFromPlayer(dmg, isKick, isSweep, player.getHeadingDegrees());
                    if (ko) {
                        if (camera != null) camera.addShake(0.24f);
                        bd.spark36.mission.MissionManager.get().advanceProgress(1);
                        if (allAggressorsSubdued()) {
                            bd.spark36.mission.MissionManager.get().completeMission();
                            if (hud != null) hud.triggerVictory();
                        }
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
    public void restoreObjectiveFlags(boolean[] flags) {
        for (int i = 0; i < objectivesDone.length; i++) {
            objectivesDone[i] = flags != null && i < flags.length && flags[i];
        }
        carryingDossier = objectivesDone[1];
    }

    @Override
    public void completeAllObjectivesForTest() {
        java.util.Arrays.fill(objectivesDone, true);
    }

    @Override
    public void dispose() {
        for (bd.spark36.character.HelmetGoon g : aggressors) g.dispose();
        aggressors.clear();
        street.dispose();
        if (world != null) { world.dispose(); world = null; }
        for (Model m : models) m.dispose();
        models.clear();
    }
}
