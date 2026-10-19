package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.CampusCrowd;
import bd.spark36.character.HelmetGoon;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.DhakaCampusWorld;
import bd.spark36.world.JulyMemorials;
import bd.spark36.world.JulyMemorials.MemorialEntry;
import bd.spark36.world.TextureFactory;

/**
 * LEVEL 1: "Where It All Began" (যেখান থেকে সব শুরু)
 * Period: July 1 – 14, 2024 (Pre-crackdown peaceful student solidarity).
 * Environment: Dhaka University Curzon Hall, Pukur (Lotus Pond), verandas, and gardens.
 * Focus: Movement, camera, historical setup, and archival discovery at 5 commemorative monuments.
 * Combat: Strictly disabled (peaceful assembly).
 */
public class Level1PeacefulCampus implements CampaignLevel {

    private DhakaCampusWorld world;
    private JulyMemorials memorials;
    private CampusCrowd crowd;
    private HelmetGoon campusSecurity;
    private TextureFactory textures;

    private static final String[] CHECKLIST = {
        "1. Curzon Hall Central Arcade",
        "2. Aparajeyo Bangla (Arts Plaza)",
        "3. TSC Raju Anti-Terrorism Sculpture",
        "4. Central Library & Hakim Chattar",
        "5. Teacher-Student Centre (TSC)"
    };

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
        if (memorials == null) return "ACTIVE MISSION: 01 / 05";
        int ins = memorials.getInspectedCount();
        int tot = memorials.getTotalCount();
        return String.format("ACTIVE MISSION: %02d / %02d", Math.min(ins + 1, tot), tot);
    }

    @Override
    public String getMissionTitle() {
        if (memorials == null) return "THE SPARK OF SOLIDARITY";
        MemorialEntry next = memorials.getNextObjective(Vector3.Zero);
        return (next != null) ? next.title : "CAMPUS ARCHIVES UNLOCKED";
    }

    @Override
    public String getMissionTarget() {
        if (memorials == null) return "Target: Curzon Hall Central Arcade";
        MemorialEntry next = memorials.getNextObjective(Vector3.Zero);
        return (next != null) ? "Target: " + next.location : "All 5 archives inspected";
    }

    @Override
    public Vector3 getActiveWaypoint() {
        if (memorials == null) return new Vector3(3.6f, 0f, -16.8f);
        MemorialEntry next = memorials.getNextObjective(Vector3.Zero);
        return (next != null) ? next.position : new Vector3(0f, 0f, 0f);
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
        if (memorials == null) return false;
        Array<MemorialEntry> entries = memorials.getEntries();
        if (index >= 0 && index < entries.size) {
            return entries.get(index).inspected;
        }
        return false;
    }

    @Override
    public int getCompletedObjectiveCount() {
        return (memorials != null) ? memorials.getInspectedCount() : 0;
    }

    @Override
    public int getTotalObjectiveCount() {
        return (memorials != null) ? memorials.getTotalCount() : 5;
    }

    @Override
    public String getContextPrompt(Vector3 playerPos) {
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
        if (memorials == null) return false;

        // 1. Inspect Memorial
        for (MemorialEntry entry : memorials.getEntries()) {
            if (!entry.inspected && playerPos.dst(entry.position) <= 3.8f) {
                entry.inspected = true;
                hud.openMemorialModal(entry);
                hud.triggerMissionToast(entry.title, memorials.getInspectedCount());
                if (memorials.isAllInspected()) {
                    hud.triggerVictory();
                }
                return true;
            }
        }

        // 2. Inspect 1921 Boundary Stone
        if (playerPos.dst(DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
            hud.openMemorialModal(hud.getBoundaryStoneEntry());
            return true;
        }

        // 3. Water sitting at Pond Ghat
        if (world != null && world.isNearPondGhat(playerPos)) {
            boolean nextSit = !player.isSittingWater();
            player.setSittingWater(nextSit);
            if (nextSit) {
                player.setPosition(0f, 0.18f, 17.2f);
            }
            return true;
        }

        // 4. Talk to nearby DU Student in crowd
        if (crowd != null && crowd.interactWithClosest(playerPos, 2.6f, getLevelNumber(), hud)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isLevelComplete() {
        return (memorials != null) && memorials.isAllInspected();
    }

    @Override
    public boolean isCombatEnabled() {
        return false;
    }

    @Override
    public Vector3 getSpawnPosition() {
        return new Vector3(0f, 0f, 72.0f); // Main promenade gate
    }

    @Override
    public float getSpawnHeading() {
        return 180f; // Facing north toward Curzon Hall
    }

    @Override
    public void init(TextureFactory textures, PlayerController player, CinematicCamera camera) {
        this.textures = textures;
        this.world = new DhakaCampusWorld(textures);
        this.memorials = new JulyMemorials();

        player.setExtraColliders(world.getColliders());
        this.crowd = new CampusCrowd(16, player.getColliders());
        this.crowd.setLevelProfile(1);

        this.campusSecurity = new HelmetGoon(textures);
        this.campusSecurity.setRole(HelmetGoon.Role.CAMPUS_SECURITY);
        this.campusSecurity.reset(0f, 36.0f);

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
        if (campusSecurity != null && canMove) {
            campusSecurity.update(delta, player);
        }
    }

    @Override
    public void renderShadow(ModelBatch shadowBatch) {
        if (world != null) world.renderShadowCasters(shadowBatch);
        if (campusSecurity != null) campusSecurity.render(shadowBatch, null);
    }

    @Override
    public void render3D(ModelBatch modelBatch, Environment environment) {
        if (world != null) world.render(modelBatch);
        if (memorials != null) memorials.render(modelBatch, environment);
        if (campusSecurity != null) campusSecurity.render(modelBatch, environment);
    }

    @Override
    public void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH) {
        // Atmospheric daylight god rays and campus markers handled by atmosphere renderer
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
        // Peaceful Level 1: hitting innocent students triggers peace violation
        if (crowd != null && crowd.checkHitByPlayer(player.getPosition(), player.getHeadingDegrees(), 2.2f)) {
            hud.setDisqualified(true, "VIOLATION: You struck an innocent campus student! Unprovoked violence against unarmed peers is strictly forbidden.");
            return true;
        }
        return false;
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
        return campusSecurity;
    }

    @Override
    public void completeAllObjectivesForTest() {
        if (memorials != null && memorials.getEntries() != null) {
            for (JulyMemorials.MemorialEntry e : memorials.getEntries()) e.inspected = true;
        }
    }

    @Override
    public void dispose() {
        if (world != null) { world.dispose(); world = null; }
        if (memorials != null) { memorials.dispose(); memorials = null; }
        if (campusSecurity != null) { campusSecurity.dispose(); campusSecurity = null; }
    }
}
