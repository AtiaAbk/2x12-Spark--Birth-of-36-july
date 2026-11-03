// ei file ta story campaign er shob level er base class
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

public interface CampaignLevel extends Disposable {

    int getLevelNumber();

    String getTitle();

    String getSubtitle();

    String getHistoricalDate();

    String getLocationName();

    String getMissionTag();

    String getMissionTitle();

    String getMissionTarget();

    Vector3 getActiveWaypoint();

    float getWaypointDistance(Vector3 playerPos);

    String[] getChecklistItems();

    boolean isChecklistItemDone(int index);

    int getCompletedObjectiveCount();

    int getTotalObjectiveCount();

    String getContextPrompt(Vector3 playerPos);

    boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud);

    boolean isLevelComplete();

    boolean isCombatEnabled();

    Vector3 getSpawnPosition();

    float getSpawnHeading();

    void init(TextureFactory textures, PlayerController player, CinematicCamera camera);

    void update(float delta, PlayerController player, CinematicCamera camera, boolean canMove, WhereWindsMeetHUD hud);

    void renderShadow(ModelBatch shadowBatch);

    void render3D(ModelBatch modelBatch, Environment environment);

    void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH);

    java.util.List<float[]> getColliders();

    Environment getEnvironment();

    boolean onPlayerAttack(PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud);

    default bd.spark36.world.DhakaCampusWorld getWorld() { return null; }
    default bd.spark36.world.JulyMemorials getMemorials() { return null; }
    default bd.spark36.character.CampusCrowd getCrowd() { return null; }
    default bd.spark36.mission.MissionTracker getMissionTracker() { return null; }
    default bd.spark36.interaction.InteractionSystem getInteractionSystem() { return null; }
    default bd.spark36.dialogue.DialogueManager getDialogueManager() { return null; }

    default void completeAllObjectivesForTest() {}

    default boolean[] getObjectiveFlags() {
        boolean[] flags = new boolean[Math.max(0, getTotalObjectiveCount())];
        for (int i = 0; i < flags.length; i++) flags[i] = isChecklistItemDone(i);
        return flags;
    }

    default void restoreObjectiveFlags(boolean[] flags) {}
}
