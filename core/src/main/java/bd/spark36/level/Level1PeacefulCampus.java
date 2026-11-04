// ei file ta curzon hall er shantopurno campus exploration level
package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.HelmetGoon;
import bd.spark36.character.PlayerController;
import bd.spark36.character.StudentNPC;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.JulyMemorials;
import bd.spark36.world.JulyMemorials.MemorialEntry;
import bd.spark36.world.TextureFactory;

import bd.spark36.mission.MissionObjective;
import bd.spark36.mission.MissionTracker;
import bd.spark36.mission.ObjectiveType;

public class Level1PeacefulCampus implements CampaignLevel {

    private DhakaCampusWorld world;
    private JulyMemorials memorials;
    private CampusCrowd crowd;
    private HelmetGoon campusSecurity;
    private final Array<HelmetGoon> aggressors = new Array<>();
    private TextureFactory textures;
    private final MissionTracker missionTracker = new MissionTracker(false);
    private final bd.spark36.interaction.InteractionSystem interactionSystem = new bd.spark36.interaction.InteractionSystem();

    private boolean combatPhase = false;
    private boolean victoryTriggered = false;
    private float hitStopTimer = 0f;
    private boolean attackHitProcessed = false;
    private final Vector3 lastPlayerPos = new Vector3(0f, 0f, 72f);

    private static final String[] CHECKLIST = {
        "1. Curzon Hall Central Arcade",
        "2. Aparajeyo Bangla (Arts Plaza)",
        "3. TSC Raju Anti-Terrorism Sculpture",
        "4. Central Library & Hakim Chattar",
        "5. Teacher-Student Centre (TSC)",
        "6. Defend against helmeted attackers"
    };

    public Level1PeacefulCampus() {
        this.memorials = new JulyMemorials();
        initDefaultObjectives();
        registerInteractables();
    }

    private void initDefaultObjectives() {
        missionTracker.reset();
        missionTracker.addObjective(new MissionObjective("m1", "THE SPARK OF SOLIDARITY", "Target: Curzon Hall Central Arcade", CHECKLIST[0], ObjectiveType.INTERACT, new Vector3(3.6f, 0f, -16.8f), 3.8f, true));
        missionTracker.addObjective(new MissionObjective("m2", "THE CALL FOR JUSTICE", "Target: Aparajeyo Bangla (Arts Plaza)", CHECKLIST[1], ObjectiveType.INTERACT, new Vector3(-32f, 0f, 26f), 3.8f, true));
        missionTracker.addObjective(new MissionObjective("m3", "THE RESISTANCE BEGINS", "Target: TSC Raju Anti-Terrorism Sculpture", CHECKLIST[2], ObjectiveType.INTERACT, new Vector3(44f, 0f, 42f), 3.8f, true));
        missionTracker.addObjective(new MissionObjective("m4", "COMMUNAL AWAKENING", "Target: Central Library & Hakim Chattar", CHECKLIST[3], ObjectiveType.INTERACT, new Vector3(-58f, 0f, 36f), 3.8f, true));
        missionTracker.addObjective(new MissionObjective("m5", "THE UNBROKEN CHAIN", "Target: Teacher-Student Centre (TSC)", CHECKLIST[4], ObjectiveType.INTERACT, new Vector3(66f, 0f, 20f), 3.8f, true));
        missionTracker.addObjective(new MissionObjective("m6", "DEFEND CAMPUS SOLIDARITY", "Target: Subdue helmeted attackers armed with lathis", CHECKLIST[5], ObjectiveType.TRIGGER_EVENT, new Vector3(0f, 0f, 36f), 6.0f, false));
    }

    @Override
    public int getLevelNumber() {
        return 1;
    }

    @Override
    public String getTitle() {
        return "WHERE IT ALL BEGAN";
    }

    @Override
    public String getSubtitle() {
        return "যেখান থেকে সব শুরু";
    }

    @Override
    public String getHistoricalDate() {
        return "July 1 – 14, 2024";
    }

    @Override
    public String getLocationName() {
        return "Dhaka University — Curzon Hall";
    }

    @Override
    public String getMissionTag() {
        if (!combatPhase) {
            int ins = (memorials != null) ? memorials.getInspectedCount() : 0;
            return String.format("CAMPUS EXPLORATION: %02d / 05", Math.min(ins + 1, 5));
        } else {
            return "DEFEND CAMPUS / আত্মরক্ষা";
        }
    }

    @Override
    public String getMissionTitle() {
        if (!combatPhase) {
            if (memorials == null) return "THE SPARK OF SOLIDARITY";
            MemorialEntry next = memorials.getNextObjective(lastPlayerPos);
            return (next != null) ? next.title : "CAMPUS ARCHIVES UNLOCKED";
        } else {
            return "DEFEND AGAINST HELMETED ATTACKERS";
        }
    }

    @Override
    public String getMissionTarget() {
        if (!combatPhase) {
            if (memorials == null) return "Target: Curzon Hall Central Arcade";
            MemorialEntry next = memorials.getNextObjective(lastPlayerPos);
            return (next != null) ? "Target: " + next.location : "All 5 archives inspected";
        } else {
            int remaining = getRemainingAggressorsCount();
            return String.format("Target: Subdue helmeted attackers with lathi (%d remaining)", remaining);
        }
    }

    @Override
    public Vector3 getActiveWaypoint() {
        if (!combatPhase) {
            if (memorials == null) return new Vector3(3.6f, 0f, -16.8f);
            MemorialEntry next = memorials.getNextObjective(lastPlayerPos);
            return (next != null) ? next.position : new Vector3(0f, 0f, 0f);
        } else {
            HelmetGoon closest = getClosestActiveAggressor(lastPlayerPos);
            return (closest != null) ? closest.getPosition() : new Vector3(0f, 0f, 36f);
        }
    }

    @Override
    public float getWaypointDistance(Vector3 playerPos) {
        if (!combatPhase) {
            return playerPos.dst(getActiveWaypoint());
        } else {
            HelmetGoon closest = getClosestActiveAggressor(playerPos);
            return (closest != null) ? playerPos.dst(closest.getPosition()) : 0f;
        }
    }

    @Override
    public String[] getChecklistItems() {
        return CHECKLIST;
    }

    @Override
    public boolean isChecklistItemDone(int index) {
        if (index >= 0 && index < 5) {
            if (memorials == null) return false;
            Array<MemorialEntry> entries = memorials.getEntries();
            if (index < entries.size) {
                return entries.get(index).inspected;
            }
            return false;
        } else if (index == 5) {
            return combatPhase && allAggressorsSubdued();
        }
        return false;
    }

    @Override
    public int getCompletedObjectiveCount() {
        int count = (memorials != null) ? memorials.getInspectedCount() : 0;
        if (combatPhase && allAggressorsSubdued()) {
            count++;
        }
        return count;
    }

    @Override
    public int getTotalObjectiveCount() {
        return CHECKLIST.length;
    }

    public void registerInteractables() {
        interactionSystem.clear();
        if (memorials != null) {
            for (final MemorialEntry entry : memorials.getEntries()) {
                interactionSystem.register(new bd.spark36.interaction.Interactable() {
                    @Override public Vector3 getInteractionPosition() { return entry.position; }
                    @Override public float getInteractionRadius() { return 3.8f; }
                    @Override public String getInteractionPrompt() { return "[E] INSPECT HISTORICAL ARCHIVE — 36 JULY MOVEMENT"; }
                    @Override public boolean canInteract(Vector3 playerPos, float playerHeading) { return !combatPhase && !entry.inspected; }
                    @Override public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
                        entry.inspected = true;
                        missionTracker.completeObjective("m" + entry.id);
                        bd.spark36.mission.MissionManager.get().advanceProgress(1);
                        if (hud != null) {
                            hud.openMemorialModal(entry);
                            hud.triggerMissionToast(entry.title, getCompletedObjectiveCount());
                        }
                        if (allExplorationsComplete()) {
                            triggerCombatEncounter(player, hud);
                        }
                        return true;
                    }
                    @Override public int getInteractionPriority() { return 3; }
                    @Override public String getInteractableId() { return "MemorialArchive_" + entry.id; }
                });
            }
        }

        interactionSystem.register(new bd.spark36.interaction.Interactable() {
            @Override public Vector3 getInteractionPosition() { return DhakaCampusWorld.BOUNDARY_STONE_POS; }
            @Override public float getInteractionRadius() { return 3.2f; }
            @Override public String getInteractionPrompt() { return "[E] INSPECT 1921 HERITAGE BOUNDARY MARKER"; }
            @Override public boolean canInteract(Vector3 playerPos, float playerHeading) { return !combatPhase; }
            @Override public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
                if (hud != null) hud.openMemorialModal(hud.getBoundaryStoneEntry());
                return true;
            }
            @Override public int getInteractionPriority() { return 1; }
            @Override public String getInteractableId() { return "BoundaryMarker_1921"; }
        });

        interactionSystem.register(new bd.spark36.interaction.Interactable() {
            @Override public Vector3 getInteractionPosition() { return new Vector3(0f, 0.18f, 17.2f); }
            @Override public float getInteractionRadius() { return 4.0f; }
            @Override public String getInteractionPrompt() { return "[E] SIT BY CURZON PUKUR GHAT"; }
            @Override public boolean canInteract(Vector3 playerPos, float playerHeading) { return !combatPhase && world != null && world.isNearPondGhat(playerPos); }
            @Override public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
                if (player != null) {
                    boolean nextSit = !player.isSittingWater();
                    player.setSittingWater(nextSit);
                    if (nextSit) {
                        player.setPosition(0f, 0.18f, 17.2f);
                        bd.spark36.audio.SoundManager.get().playSplash();
                    }
                    return true;
                }
                return false;
            }
            @Override public int getInteractionPriority() { return 1; }
            @Override public String getInteractableId() { return "CurzonPukurGhat"; }
        });

        if (crowd != null && crowd.getStudents() != null) {
            for (StudentNPC student : crowd.getStudents()) {
                interactionSystem.register(student);
            }
        }
    }

    @Override
    public String getContextPrompt(Vector3 playerPos) {
        if (combatPhase) return null;
        String prompt = interactionSystem.getContextPrompt();
        if (prompt != null) return prompt;

        if (memorials != null) {
            for (MemorialEntry entry : memorials.getEntries()) {
                if (!entry.inspected && playerPos.dst(entry.position) <= 3.8f) {
                    return "[E] INSPECT HISTORICAL ARCHIVE — 36 JULY MOVEMENT";
                }
            }
        }
        if (playerPos.dst(DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            return "[E] INSPECT 1921 HERITAGE BOUNDARY MARKER";
        }
        if (world != null && world.isNearPondGhat(playerPos)) {
            return "[E] SIT BY CURZON PUKUR GHAT";
        }
        if (crowd != null && crowd.getClosestInteractable(playerPos, 2.6f) != null) {
            return "[E] TALK TO DU STUDENT";
        }
        return null;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        if (combatPhase) return false;
        float heading = (player != null) ? player.getHeadingDegrees() : 180f;
        boolean consumed = interactionSystem.triggerInteraction(playerPos, heading, player, hud);
        if (consumed) {
            bd.spark36.interaction.Interactable focused = interactionSystem.getFocusedInteractable();
            if (focused instanceof StudentNPC) {
                bd.spark36.mission.MissionManager.get().advanceProgress(1);
            }
            return true;
        }

        if (memorials != null) {
            for (MemorialEntry entry : memorials.getEntries()) {
                if (!entry.inspected && playerPos.dst(entry.position) <= 3.8f) {
                    entry.inspected = true;
                    missionTracker.completeObjective("m" + entry.id);
                    bd.spark36.mission.MissionManager.get().advanceProgress(1);
                    if (hud != null) {
                        hud.openMemorialModal(entry);
                        hud.triggerMissionToast(entry.title, getCompletedObjectiveCount());
                    }
                    if (allExplorationsComplete()) {
                        triggerCombatEncounter(player, hud);
                    }
                    return true;
                }
            }
        }

        if (playerPos.dst(DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            if (hud != null) hud.openMemorialModal(hud.getBoundaryStoneEntry());
            return true;
        }

        if (world != null && world.isNearPondGhat(playerPos) && player != null) {
            boolean nextSit = !player.isSittingWater();
            player.setSittingWater(nextSit);
            if (nextSit) {
                player.setPosition(0f, 0.18f, 17.2f);
                bd.spark36.audio.SoundManager.get().playSplash();
            }
            return true;
        }

        if (crowd != null && player != null && crowd.interactWithClosest(playerPos, 2.6f, getLevelNumber(), hud)) {
            bd.spark36.mission.MissionManager.get().advanceProgress(1);
            return true;
        }

        return false;
    }

    public boolean allExplorationsComplete() {
        if (memorials == null) return false;
        return memorials.isAllInspected();
    }

    public void triggerCombatEncounter(PlayerController player, WhereWindsMeetHUD hud) {
        if (combatPhase) return;
        combatPhase = true;

        Vector3 pPos = (player != null) ? player.getPosition() : new Vector3(0f, 0f, 36f);
        float hDeg = (player != null) ? player.getHeadingDegrees() : 180f;

        while (aggressors.size < 3) {
            HelmetGoon g = (textures != null) ? new HelmetGoon(textures) : new HelmetGoon();
            g.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
            aggressors.add(g);
        }

        float[] angles = { 0f, -30f, 30f };
        float[] dists = { 12f, 14f, 14f };
        for (int i = 0; i < aggressors.size; i++) {
            float forwardAngle = hDeg + angles[i];
            float forwardX = MathUtils.sinDeg(forwardAngle);
            float forwardZ = MathUtils.cosDeg(forwardAngle);
            float spawnX = MathUtils.clamp(pPos.x + forwardX * dists[i], -65f, 65f);
            float spawnZ = MathUtils.clamp(pPos.z + forwardZ * dists[i], -40f, 74f);

            HelmetGoon g = aggressors.get(i);
            g.reset(spawnX, spawnZ);
            g.startFightDirectly();
            g.setHeading(hDeg + 180f);
        }

        MissionObjective m6 = missionTracker.getObjectiveById("m6");
        if (m6 != null) {
            m6.activate();
        }

        if (crowd != null) {
            crowd.setLevelProfile(3);
        }

        if (hud != null) {
            hud.triggerMissionToast("ALERT: CHHATRA LEAGUE HELMET FORCE ATTACKING! / ছাত্রলীগ হেলমেট বাহিনী আক্রমণ করতে আসছে!", 5);
            bd.spark36.audio.SoundManager.get().playObjective();
        }
    }

    @Override
    public boolean isLevelComplete() {
        if (missionTracker.getTotalCount() > 0 && missionTracker.isAllCompleted()) {
            return true;
        }
        return (memorials != null && memorials.isAllInspected() && combatPhase && allAggressorsSubdued());
    }

    @Override
    public boolean isCombatEnabled() {
        return true;
    }

    @Override
    public Vector3 getSpawnPosition() {
        return new Vector3(0f, 0f, 72.0f);
    }

    @Override
    public float getSpawnHeading() {
        return 180.0f;
    }

    @Override
    public void init(TextureFactory textures, PlayerController player, CinematicCamera camera) {
        this.textures = textures;
        this.world = new DhakaCampusWorld(textures);
        this.memorials = new JulyMemorials();
        this.combatPhase = false;
        this.victoryTriggered = false;

        if (this.missionTracker.getTotalCount() == 0) {
            initDefaultObjectives();
        } else {
            this.missionTracker.reset();
        }

        player.setExtraColliders(world.getColliders());
        this.crowd = new CampusCrowd(32, player.getColliders());
        this.crowd.setLevelProfile(1);

        // Exploration phase: No villains or aggressors on campus. Campus is completely peaceful.
        this.campusSecurity = null;

        aggressors.clear();
        for (int i = 0; i < 3; i++) {
            HelmetGoon g = new HelmetGoon(textures);
            g.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
            aggressors.add(g);
        }

        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());
        lastPlayerPos.set(getSpawnPosition());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(4.5f);
        registerInteractables();
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        if (hitStopTimer > 0f) {
            hitStopTimer -= delta;
            delta *= 0.15f;
        }

        if (player != null) {
            lastPlayerPos.set(player.getPosition());
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.F6) || Gdx.input.isKeyJustPressed(Input.Keys.X)) && !combatPhase) {
            if (memorials != null) {
                for (MemorialEntry entry : memorials.getEntries()) {
                    entry.inspected = true;
                    missionTracker.completeObjective("m" + entry.id);
                }
            }
            triggerCombatEncounter(player, hud);
            return;
        }

        if (world != null) world.update(delta);
        if (memorials != null) memorials.update(delta);
        if (crowd != null) crowd.update(delta, player.getPosition());

        if (player != null) {
            interactionSystem.update(delta, player.getPosition(), player.getHeadingDegrees());
        }

        if (!combatPhase) {
            if (hud != null) {
                hud.setHelmetGoon(null);
            }
            if (player != null) {
                player.clearCombatTarget();
            }
        } else {
            HelmetGoon closestAggressor = null;
            float closestDst = Float.MAX_VALUE;
            for (HelmetGoon g : aggressors) {
                g.update(delta, player);
                if (!g.isKnockedOut()) {
                    float dst = player.getPosition().dst(g.getPosition());
                    if (dst < closestDst) {
                        closestDst = dst;
                        closestAggressor = g;
                    }
                }
            }

            if (closestAggressor != null && closestDst <= 20f && hud != null) {
                hud.setHelmetGoon(closestAggressor);
                player.setCombatTarget(closestAggressor.getPosition());
            } else if (hud != null) {
                hud.setHelmetGoon(null);
                player.clearCombatTarget();
            }

            if (player.isKnockedOut() && hud != null && !hud.isDefeated()) {
                hud.setDefeated(true);
            }

            if (allAggressorsSubdued() && !victoryTriggered) {
                victoryTriggered = true;
                missionTracker.completeObjective("m6");
                if (hud != null) {
                    hud.triggerVictory();
                }
            }
        }
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        if (combatPhase) {
            for (HelmetGoon g : aggressors) {
                g.render(shadowBatch, null);
            }
        }
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment environment) {
        if (world != null) world.render(modelBatch);
        if (memorials != null) memorials.render(modelBatch, environment);
        if (combatPhase) {
            for (HelmetGoon g : aggressors) {
                g.render(modelBatch, environment);
            }
        }
    }

    @Override
    public void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH) {
    }

    @Override
    public java.util.List<float[]> getColliders() {
        return (world != null) ? world.getColliders() : new java.util.ArrayList<float[]>();
    }

    @Override
    public Environment getEnvironment() {
        return (world != null) ? world.getEnvironment() : null;
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

            // Combat Phase: Prioritize subduing armed Chhatra League aggressors
            if (combatPhase) {
                for (int i = 0; i < aggressors.size; i++) {
                    HelmetGoon g = aggressors.get(i);
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
                            if (allAggressorsSubdued() && !victoryTriggered) {
                                victoryTriggered = true;
                                missionTracker.completeObjective("m6");
                                bd.spark36.mission.MissionManager.get().completeMission();
                                if (hud != null) {
                                    hud.triggerVictory();
                                }
                            }
                        }
                        return true;
                    }
                }
            }

            // Protected civilian proximity: strictly enforced prohibition
            String violation = checkCivilianStrike(player.getPosition(), player.getHeadingDegrees());
            if (violation != null) {
                if (hud != null) {
                    hud.disqualify("You attacked a protected campus member.");
                }
                bd.spark36.mission.MissionManager.get().abortMission("You attacked a protected campus member.");
                return false;
            }
        }
        return false;
    }

    public String checkCivilianStrike(Vector3 pos, float headingDeg) {
        return CivilianStrikeRule.check(crowd, pos, headingDeg, CivilianStrikeRule.STUDENT_VIOLATION);
    }

    public void setCrowdForTest(CampusCrowd crowd) {
        this.crowd = crowd;
        registerInteractables();
    }

    public int getRemainingAggressorsCount() {
        int count = 0;
        for (HelmetGoon g : aggressors) {
            if (!g.isKnockedOut()) count++;
        }
        return count;
    }

    public HelmetGoon getClosestActiveAggressor(Vector3 pos) {
        HelmetGoon closest = null;
        float minDst = Float.MAX_VALUE;
        for (HelmetGoon g : aggressors) {
            if (!g.isKnockedOut()) {
                float dst = pos.dst(g.getPosition());
                if (dst < minDst) {
                    minDst = dst;
                    closest = g;
                }
            }
        }
        return closest;
    }

    public boolean allAggressorsSubdued() {
        if (aggressors.size == 0) return true;
        for (HelmetGoon g : aggressors) {
            if (!g.isKnockedOut()) return false;
        }
        return true;
    }

    public DhakaCampusWorld getWorld() {
        return world;
    }

    public JulyMemorials getMemorials() {
        return memorials;
    }

    public CampusCrowd getCrowd() {
        return crowd;
    }

    public HelmetGoon getCampusSecurity() {
        if (campusSecurity == null) {
            campusSecurity = (textures != null) ? new HelmetGoon(textures) : new HelmetGoon();
            campusSecurity.setRole(HelmetGoon.Role.CAMPUS_SECURITY);
            campusSecurity.reset(0f, 60.0f);
        }
        return (!combatPhase || aggressors.size == 0) ? campusSecurity : aggressors.get(0);
    }

    public Array<HelmetGoon> getAggressors() {
        return aggressors;
    }

    public boolean isCombatPhase() {
        return combatPhase;
    }

    @Override
    public MissionTracker getMissionTracker() {
        return missionTracker;
    }

    @Override
    public bd.spark36.interaction.InteractionSystem getInteractionSystem() {
        return interactionSystem;
    }

    @Override
    public void restoreObjectiveFlags(boolean[] flags) {
        missionTracker.restoreCompleted(flags);
        if (memorials != null && memorials.getEntries() != null) {
            for (JulyMemorials.MemorialEntry e : memorials.getEntries()) {
                e.inspected = flags != null && e.id >= 0 && e.id < flags.length && flags[e.id];
            }
        }
        if (allExplorationsComplete()) {
            combatPhase = true;
            if (flags != null && flags.length > 5 && flags[5]) {
                for (HelmetGoon g : aggressors) {
                    g.takeHitFromPlayer(999f, false, false, 0f);
                }
            }
        }
    }

    @Override
    public void completeAllObjectivesForTest() {
        if (memorials != null && memorials.getEntries() != null) {
            for (JulyMemorials.MemorialEntry e : memorials.getEntries()) e.inspected = true;
        }
        combatPhase = true;
        for (HelmetGoon g : aggressors) {
            g.takeHitFromPlayer(999f, false, false, 0f);
        }
        if (missionTracker != null) {
            missionTracker.completeAllForTest();
        }
    }

    @Override
    public void dispose() {
        if (world != null) { world.dispose(); world = null; }
        if (memorials != null) { memorials.dispose(); memorials = null; }
        if (campusSecurity != null) { campusSecurity.dispose(); campusSecurity = null; }
        for (HelmetGoon g : aggressors) {
            g.dispose();
        }
        aggressors.clear();
    }
}
