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
 * Focus: Get injured students to the medical shelter. Aggressors are a hazard to evade, block or break away from.
 * Combat: Defensive self-protection only (FICTIONAL GAMEPLAY). Knockouts are optional and never required.
 * Hitting unarmed students = instant disqualification.
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

    // Objectives (defensive, non-lethal; reference section 8.3):
    // 1. Stabilize injured student at Raju Sculpture
    // 2. Escort that student to the medical shelter
    // 3. Reach the second injured student on the Arts Faculty road
    // 4. Escort the second student to the medical shelter
    // 5. Secure the medical shelter
    // Aggressors are a hazard to evade / block / break away from. Knockouts are optional
    // and never complete an objective in story mode.
    private final Level2ShelterObjectives objectives = new Level2ShelterObjectives();

    /** Aggressor spawn points (hazards around the injured students and the approach road). */
    private final Vector3[] aggressorSpawns = {
        new Vector3(44f, 0f, 38f),  // Raju Sculpture east
        new Vector3(38f, 0f, 44f),  // Raju Sculpture west
        new Vector3(22f, 0f, 36f)   // Arts Faculty approach road
    };
    private final boolean[] aggressorKnockoutCounted = new boolean[3];

    private static final String[] CHECKLIST = Level2ShelterObjectives.CHECKLIST;

    public Level2StudentConfrontation() {
        initDefaultObjectives();
    }

    private void initDefaultObjectives() {
        missionTracker.reset();
        for (int i = 0; i < Level2ShelterObjectives.COUNT; i++) {
            boolean shelter = (i == Level2ShelterObjectives.ESCORT_FIRST || i == Level2ShelterObjectives.ESCORT_SECOND
                || i == Level2ShelterObjectives.SECURE_SHELTER);
            missionTracker.addObjective(new MissionObjective("l2_" + (i + 1), Level2ShelterObjectives.TITLES[i],
                Level2ShelterObjectives.TARGETS[i], CHECKLIST[i], ObjectiveType.INTERACT, objectives.getWaypoint(i),
                shelter ? Level2ShelterObjectives.SHELTER_RADIUS : Level2ShelterObjectives.STUDENT_RADIUS, i == 0));
        }
    }

    /** Pure objective logic (package-private for headless tests). */
    Level2ShelterObjectives getShelterObjectives() {
        return objectives;
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
        if (objectives.isComplete()) return "STUDENTS SHELTERED";
        return Level2ShelterObjectives.TITLES[objectives.nextIndex()];
    }

    @Override
    public String getMissionTarget() {
        if (objectives.isComplete()) return "Injured students safe in the medical shelter";
        return Level2ShelterObjectives.TARGETS[objectives.nextIndex()];
    }

    @Override
    public Vector3 getActiveWaypoint() {
        return objectives.getActiveWaypoint();
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
        return objectives.isDone(index);
    }

    @Override
    public int getCompletedObjectiveCount() {
        return objectives.getCompletedCount();
    }

    @Override
    public int getTotalObjectiveCount() {
        return 5;
    }

    @Override
    public String getContextPrompt(Vector3 playerPos) {
        String prompt = objectives.contextPrompt(playerPos);
        if (prompt != null) return prompt;
        if (crowd != null && crowd.getClosestInteractable(playerPos, 2.6f) != null) {
            return "[E] TALK TO DU STUDENT";
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        int completed = objectives.interact(playerPos);
        if (completed >= 0) {
            missionTracker.completeObjective(completed);
            if (hud != null) {
                String toast;
                switch (completed) {
                    case Level2ShelterObjectives.STABILIZE_FIRST:
                    case Level2ShelterObjectives.STABILIZE_SECOND:
                        toast = "FIRST AID GIVEN — ESCORT TO SHELTER"; break;
                    case Level2ShelterObjectives.ESCORT_FIRST:
                    case Level2ShelterObjectives.ESCORT_SECOND:
                        toast = "STUDENT SAFE IN MEDICAL SHELTER"; break;
                    default:
                        toast = "MEDICAL SHELTER SECURED"; break;
                }
                hud.triggerMissionToast(toast, getCompletedObjectiveCount());
            }
            checkCompletion(hud);
            return true;
        }
        // Student crowd interaction
        if (crowd != null && hud != null && crowd.interactWithClosest(playerPos, 2.6f, getLevelNumber(), hud)) {
            return true;
        }
        return false;
    }

    private void checkCompletion(WhereWindsMeetHUD hud) {
        if (isLevelComplete() && hud != null) {
            hud.triggerVictory();
        }
    }

    /**
     * Story mode: complete only when the injured students are sheltered (zero knockouts allowed).
     * Arena mode (fictional folk-sport sparring, separate from the campaign) additionally
     * accepts subduing all three sparring aggressors, preserving the original Arena loop.
     */
    @Override
    public boolean isLevelComplete() {
        if (objectives.isComplete()) return true;
        return LevelConfig.getGameMode() == LevelConfig.GameMode.ARENA && allAggressorsSubdued();
    }

    private boolean allAggressorsSubdued() {
        if (aggressors.size == 0) return false;
        for (HelmetGoon g : aggressors) if (!g.isKnockedOut()) return false;
        return true;
    }

    /** Disqualification check for striking unarmed students (package-private for tests). */
    String checkCivilianStrike(Vector3 pos, float headingDeg) {
        return CivilianStrikeRule.check(crowd, pos, headingDeg, CivilianStrikeRule.STUDENT_VIOLATION);
    }

    /** Test hook: inject a headless crowd without building the 3D world. */
    void setCrowdForTest(CampusCrowd crowd) {
        this.crowd = crowd;
    }

    @Override
    public void restoreObjectiveFlags(boolean[] flags) {
        objectives.restore(flags);
        missionTracker.restoreCompleted(objectives.snapshot());
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
        g1.reset(aggressorSpawns[0].x, aggressorSpawns[0].z);
        aggressors.add(g1);

        HelmetGoon g2 = new HelmetGoon(textures);
        g2.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        g2.reset(aggressorSpawns[1].x, aggressorSpawns[1].z);
        aggressors.add(g2);

        HelmetGoon g3 = new HelmetGoon(textures);
        g3.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        g3.reset(aggressorSpawns[2].x, aggressorSpawns[2].z);
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
        ti.transform.setTranslation(Level2ShelterObjectives.SHELTER_POS);
        barricadeInstances.add(ti);
        colliders.add(new float[] { Level2ShelterObjectives.SHELTER_POS.x - 2.5f, 0f, Level2ShelterObjectives.SHELTER_POS.z - 2.5f,
                                    Level2ShelterObjectives.SHELTER_POS.x + 2.5f, 3.5f, Level2ShelterObjectives.SHELTER_POS.z + 2.5f });

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
            } else {
                recordOptionalKnockout(i, hud);
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
            String violation = checkCivilianStrike(player.getPosition(), player.getHeadingDegrees());
            if (violation != null) {
                hud.setDisqualified(true, violation);
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
                        recordOptionalKnockout(i, hud);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * An aggressor was knocked out in self-defence. Optional: it is counted once, shown as a
     * neutral notice, and never completes a story objective (no reward for knockouts).
     */
    private void recordOptionalKnockout(int i, WhereWindsMeetHUD hud) {
        if (i < 0 || i >= aggressorKnockoutCounted.length || aggressorKnockoutCounted[i]) return;
        aggressorKnockoutCounted[i] = true;
        objectives.recordOptionalKnockout();
        if (hud != null) {
            hud.triggerMissionToast("ATTACKER DOWN — GET THE STUDENTS TO SAFETY", getCompletedObjectiveCount());
        }
        checkCompletion(hud);
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
        objectives.completeAllForTest();
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
