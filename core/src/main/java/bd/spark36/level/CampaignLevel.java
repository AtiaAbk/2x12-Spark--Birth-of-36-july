package bd.spark36.level;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.TextureFactory;

/**
 * Interface representing a playable campaign level in "Spark: Birth of 36 July".
 * Encapsulates the environment, mission objectives, historical archival entries,
 * NPC state machines, combat rules, and visual atmosphere for a specific historical date.
 */
public interface CampaignLevel extends Disposable {

    /** 1-based level index (1 to 6) */
    int getLevelNumber();

    /** Main level title (e.g. "Where It All Began", "The Day the Streets Changed") */
    String getTitle();

    /** Bengali or secondary subtitle */
    String getSubtitle();

    /** Documented historical date range (e.g. "July 1 – 14, 2024") */
    String getHistoricalDate();

    /** Documented geographic location (e.g. "Dhaka University — Curzon Hall") */
    String getLocationName();

    /** Live mission tag for HUD card (e.g. "ACTIVE MISSION: 01 / 05") */
    String getMissionTag();

    /** Active mission objective title (e.g. "THE SPARK OF SOLIDARITY") */
    String getMissionTitle();

    /** Active target description for HUD card */
    String getMissionTarget();

    /** 3D world coordinate of the active primary objective / waypoint */
    Vector3 getActiveWaypoint();

    /** Distance from player position to active waypoint */
    float getWaypointDistance(Vector3 playerPos);

    /** Array of mission parameters/checklist entries shown in bottom-left HUD */
    String[] getChecklistItems();

    /** Whether the checklist item at given index is accomplished */
    boolean isChecklistItemDone(int index);

    /** Number of completed objectives in this level */
    int getCompletedObjectiveCount();

    /** Total number of primary objectives in this level */
    int getTotalObjectiveCount();

    /** Contextual interaction prompt for player (e.g. "[E] INSPECT ARCHIVE", "[E] PROVIDE FIRST AID") or null */
    String getContextPrompt(Vector3 playerPos);

    /** Handles player pressing [E] key. Returns true if interaction was consumed */
    boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud);

    /** Whether all required level objectives have been satisfied */
    boolean isLevelComplete();

    /** Whether martial arts combat is active in this level */
    boolean isCombatEnabled();

    /** Default initial player spawn coordinates in 3D world */
    Vector3 getSpawnPosition();

    /** Default initial player heading angle in degrees */
    float getSpawnHeading();

    /** Initializes 3D level geometry, props, lighting, and NPCs */
    void init(TextureFactory textures, PlayerController player, CinematicCamera camera);

    /** Updates level simulation, NPCs, objective tracking, and timers */
    void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud);

    /** Renders shadow casters for depth pass */
    void renderShadow(ModelBatch shadowBatch);

    /** Renders 3D world geometry, landmarks, props, and NPCs */
    void render3D(ModelBatch modelBatch, Environment environment);

    /** Renders level-specific volumetric or screen overlays (gas, smoke, night searchlights) */
    void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH);

    /** Returns collision boxes as list of [minX, minY, minZ, maxX, maxY, maxZ] for this level's environment */
    java.util.List<float[]> getColliders();

    /** Returns directional and ambient lighting environment */
    Environment getEnvironment();

    /**
     * Resolves player combat attack strike against enemies in this level.
     * Returns true if a combat hit was connected.
     */
    boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud);

    default bd.spark36.world.DhakaCampusWorld getWorld() { return null; }
    default bd.spark36.world.JulyMemorials getMemorials() { return null; }
    default bd.spark36.character.CampusCrowd getCrowd() { return null; }
    default bd.spark36.mission.MissionTracker getMissionTracker() { return null; }
    default bd.spark36.interaction.InteractionSystem getInteractionSystem() { return null; }
    default bd.spark36.dialogue.DialogueManager getDialogueManager() { return null; }

    /** Marks all level objectives complete (used for automated visual verification) */
    default void completeAllObjectivesForTest() {}
}
