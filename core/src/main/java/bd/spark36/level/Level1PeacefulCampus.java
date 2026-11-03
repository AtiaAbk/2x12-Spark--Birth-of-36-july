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

/**
 * LEVEL 1: "Where It All Began" (যেখান থেকে সব শুরু)
 * Period: July 1 – 14, 2024.
 * Environment: Dhaka University Curzon Hall, Pukur (Lotus Pond), verandas, and gardens.
 * Structure:
 * 1. Exploration Phase: Main character explores 5 accessible campus locations to uncover the 36 July movement archives.
 *    PEACE RULE: Striking any innocent campus student (সাধারণ শিক্ষার্থী) is strictly forbidden and results in INSTANT DISQUALIFICATION.
 * 2. Confrontation Phase: Upon completing the 5 exploration missions, helmet-wearing attackers armed with bamboo/hardwood lathis
 *    arrive to attack the students. The player engages them in technical martial arts self-defense.
 *    - If player's health reaches 0 (knocked out) -> Mission Failed with Retry.
 *    - If player subdues all helmeted attackers -> Mission Success / Victory!
 */
public class Level1PeacefulCampus implements CampaignLevel {

    private DhakaCampusWorld world;
    private JulyMemorials memorials;
    private CampusCrowd crowd;
    private HelmetGoon campusSecurity;
    private final Array<HelmetGoon> aggressors = new Array<>();
    private TextureFactory textures;
    private final MissionTracker missionTracker = new MissionTracker(false);

    private boolean combatPhase = false;
    private boolean victoryTriggered = false;
    private float hitStopTimer = 0f;
    private boolean attackHitProcessed = false;

    private static final String[] CHECKLIST = {
        "1. Curzon Hall Central Arcade",
        "2. Aparajeyo Bangla (Arts Plaza)",
        "3. TSC Raju Anti-Terrorism Sculpture",
        "4. Central Library & Hakim Chattar",
        "5. Teacher-Student Centre (TSC)",
        "6. Defend against helmeted attackers"
    };

    public Level1PeacefulCampus() {
        initDefaultObjectives();
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
            MemorialEntry next = memorials.getNextObjective(Vector3.Zero);
            return (next != null) ? next.title : "CAMPUS ARCHIVES UNLOCKED";
        } else {
            return "DEFEND AGAINST HELMETED ATTACKERS";
        }
    }

    @Override
    public String getMissionTarget() {
        if (!combatPhase) {
            if (memorials == null) return "Target: Curzon Hall Central Arcade";
            MemorialEntry next = memorials.getNextObjective(Vector3.Zero);
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
            MemorialEntry next = memorials.getNextObjective(Vector3.Zero);
            return (next != null) ? next.position : new Vector3(0f, 0f, 0f);
        } else {
            HelmetGoon closest = getClosestActiveAggressor(Vector3.Zero);
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

    @Override
    public String getContextPrompt(Vector3 playerPos) {
        if (combatPhase) return null; // Combat engaged: focus on self-defense
        if (memorials == null) return null;
        for (MemorialEntry entry : memorials.getEntries()) {
            if (!entry.inspected && playerPos.dst(entry.position) <= 3.8f) {
                return "[E] INSPECT HISTORICAL ARCHIVE — 36 JULY MOVEMENT";
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
        if (memorials == null) return false;

        // 1. Inspect Memorial
        for (MemorialEntry entry : memorials.getEntries()) {
            if (!entry.inspected && playerPos.dst(entry.position) <= 3.8f) {
                entry.inspected = true;
                missionTracker.completeObjective("m" + entry.id);
                if (hud != null) {
                    hud.openMemorialModal(entry);
                    hud.triggerMissionToast(entry.title, getCompletedObjectiveCount());
                }
                // Check if all 5 exploration archives are completed
                if (allExplorationsComplete()) {
                    triggerCombatEncounter(player, hud);
                }
                return true;
            }
        }

        // 2. Inspect 1921 Boundary Stone
        if (playerPos.dst(DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            if (hud != null) hud.openMemorialModal(hud.getBoundaryStoneEntry());
            return true;
        }

        // 3. Water sitting at Pond Ghat
        if (world != null && world.isNearPondGhat(playerPos) && player != null) {
            boolean nextSit = !player.isSittingWater();
            player.setSittingWater(nextSit);
            if (nextSit) {
                player.setPosition(0f, 0.18f, 17.2f);
            }
            return true;
        }

        // 4. Talk to nearby DU Student in crowd
        if (crowd != null && player != null && crowd.interactWithClosest(playerPos, 2.6f, getLevelNumber(), hud)) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether all 5 initial campus exploration missions have been inspected.
     */
    public boolean allExplorationsComplete() {
        if (memorials == null) return false;
        return memorials.isAllInspected();
    }

    /**
     * Spawns the helmeted attackers armed with lathis after the 5 archives are explored.
     */
    public void triggerCombatEncounter(PlayerController player, WhereWindsMeetHUD hud) {
        if (combatPhase) return;
        combatPhase = true;

        Vector3 pPos = (player != null) ? player.getPosition() : new Vector3(0f, 0f, 36f);
        float hDeg = (player != null) ? player.getHeadingDegrees() : 0f;

        // Spawn 3 helmeted attackers in front of the player
        float[] angles = { 0f, -35f, 35f };
        float[] dists = { 13f, 15f, 15f };
        for (int i = 0; i < aggressors.size; i++) {
            float angle = (hDeg + angles[i]) * MathUtils.degreesToRadians;
            float dist = dists[i];
            float spawnX = pPos.x - MathUtils.sin(angle) * dist;
            float spawnZ = pPos.z - MathUtils.cos(angle) * dist;
            HelmetGoon g = aggressors.get(i);
            g.reset(spawnX, spawnZ);
            g.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
        }

        MissionObjective m5 = missionTracker.getObjectiveById("m5");
        if (m5 != null) {
            m5.activate();
        }

        if (hud != null) {
            hud.triggerMissionToast("ALERT: HELMETED ATTACKERS WITH LATHI ADVANCING! / সতর্কবার্তা: লাঠিধারী হেলমেট বাহিনী আক্রমণ করতে আসছে!", 5);
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
        return new Vector3(0f, 0f, 72.0f); // Main promenade gate
    }

    @Override
    public float getSpawnHeading() {
        return 180.0f; // Facing north toward Curzon Hall
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
        this.crowd = new CampusCrowd(24, player.getColliders());
        this.crowd.setLevelProfile(1);

        this.campusSecurity = new HelmetGoon(textures);
        this.campusSecurity.setRole(HelmetGoon.Role.CAMPUS_SECURITY);
        this.campusSecurity.reset(0f, 36.0f);

        // Pre-create the 3 Helmeted Aggressors
        aggressors.clear();
        for (int i = 0; i < 3; i++) {
            HelmetGoon g = new HelmetGoon(textures);
            g.setRole(HelmetGoon.Role.HELMETED_AGGRESSOR);
            aggressors.add(g);
        }

        player.setPosition(getSpawnPosition().x, getSpawnPosition().y, getSpawnPosition().z);
        player.setHeadingDegrees(getSpawnHeading());

        camera.setColliders(player.getColliders());
        camera.setAngles(0f, -4f);
        camera.setDistance(4.5f);
    }

    @Override
    public void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud) {
        if (world != null) world.update(delta);
        if (memorials != null) memorials.update(delta);
        if (crowd != null) crowd.update(delta, player.getPosition());

        if (campusSecurity != null && !combatPhase && canMove) {
            campusSecurity.update(delta, player);
        }

        if (combatPhase && canMove) {
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

            // Knockout check for player (Failure / Retry)
            if (player.isKnockedOut() && hud != null && !hud.isDefeated()) {
                hud.setDefeated(true);
            }

            // Check if all aggressors are subdued (Victory)
            if (allAggressorsSubdued() && !victoryTriggered) {
                victoryTriggered = true;
                missionTracker.completeObjective("m5");
                if (hud != null) {
                    hud.triggerVictory();
                }
            }
        }
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        if (!combatPhase && campusSecurity != null) {
            campusSecurity.render(shadowBatch, null);
        }
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
        if (!combatPhase && campusSecurity != null) {
            campusSecurity.render(modelBatch, environment);
        }
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

            // STRICT RULE: Striking innocent campus students (সাধারণ শিক্ষার্থী) is strictly forbidden!
            String violation = checkCivilianStrike(player.getPosition(), player.getHeadingDegrees());
            if (violation != null) {
                if (hud != null) {
                    hud.setDisqualified(true, violation);
                }
                return true;
            }

            // In combat phase, resolve strikes on active helmeted goons
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
                                missionTracker.completeObjective("m5");
                                if (hud != null) {
                                    hud.triggerVictory();
                                }
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Disqualification check for striking unarmed students (package-private for tests). */
    public String checkCivilianStrike(Vector3 pos, float headingDeg) {
        return CivilianStrikeRule.check(crowd, pos, headingDeg, CivilianStrikeRule.STUDENT_VIOLATION);
    }

    /** Test hook: inject a headless crowd without building the 3D world. */
    public void setCrowdForTest(CampusCrowd crowd) {
        this.crowd = crowd;
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
