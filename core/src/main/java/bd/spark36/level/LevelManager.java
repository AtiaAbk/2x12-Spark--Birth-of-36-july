package bd.spark36.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.camera.CinematicCamera;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;
import bd.spark36.world.TextureFactory;

/**
 * Manages the lifecycle, transitions, and execution of the six-level campaign:
 * Level 1: "Where It All Began" (July 1–14, 2024: Peaceful Campus Exploration & Archive Recovery)
 * Level 2: "The Day the Streets Changed" (July 15, 2024: Student Confrontation & Martial Arts Defense)
 * Level 3: "A City on Edge" (July 18, 2024: Police Escalation, CS Tear Gas & Humanitarian Aid)
 * Level 4: "Voices in the Dark" (July 20–23, 2024: Blackout Curfew, Military Checkpoints & Covert Route)
 * Level 5: "The Wall of People" (August 3–4, 2024: Shaheed Minar Assembly & One-Point Demand)
 * Level 6: "36 July" (August 5, 2024: Long March Victory, Liberation Ceremony & Historic Epilogue)
 */
public class LevelManager implements Disposable {

    private CampaignLevel activeLevel;
    private int currentLevelIndex = 1;
    private TextureFactory textures;
    private PlayerController player;
    private CinematicCamera camera;
    private WhereWindsMeetHUD hud;

    public LevelManager(TextureFactory textures, PlayerController player, CinematicCamera camera, WhereWindsMeetHUD hud) {
        this.textures = textures;
        this.player = player;
        this.camera = camera;
        this.hud = hud;
    }

    /**
     * Loads and initializes the specified level index (1 to 6).
     * Disposes previous level geometry cleanly to avoid memory leaks.
     */
    public void loadLevel(int levelIndex) {
        if (activeLevel != null) {
            activeLevel.dispose();
            activeLevel = null;
        }

        this.currentLevelIndex = Math.max(1, Math.min(6, levelIndex));
        LevelConfig.setCurrentLevel(currentLevelIndex);

        switch (currentLevelIndex) {
            case 1:
                activeLevel = new Level1PeacefulCampus();
                break;
            case 2:
                activeLevel = new Level2StudentConfrontation();
                break;
            case 3:
                activeLevel = new Level3PoliceEscalation();
                break;
            case 4:
                activeLevel = new Level4CurfewBlackout();
                break;
            case 5:
                activeLevel = new Level5MassMobilization();
                break;
            case 6:
                activeLevel = new Level6VictoryEpilogue();
                break;
            default:
                activeLevel = new Level1PeacefulCampus();
                break;
        }

        activeLevel.init(textures, player, camera);
        LevelConfig.setCombatEnabled(activeLevel.isCombatEnabled());

        if (hud != null) {
            hud.setActiveLevel(activeLevel);
            hud.reset();
        }

        Gdx.app.log("LevelManager", String.format("Loaded Level %d: %s (%s) — Location: %s",
            activeLevel.getLevelNumber(), activeLevel.getTitle(), activeLevel.getHistoricalDate(), activeLevel.getLocationName()));
    }

    public void update(float delta, boolean canMove) {
        if (activeLevel != null) {
            activeLevel.update(delta, player, camera, canMove, hud);
        }
    }

    public void renderShadow(ModelBatch shadowBatch) {
        if (activeLevel != null) {
            activeLevel.renderShadow(shadowBatch);
        }
    }

    public void render3D(ModelBatch modelBatch) {
        if (activeLevel != null) {
            activeLevel.render3D(modelBatch, activeLevel.getEnvironment());
        }
    }

    public void renderOverlays(CinematicCamera camera, float delta, int screenW, int screenH) {
        if (activeLevel != null) {
            activeLevel.renderOverlays(camera, delta, screenW, screenH);
        }
    }

    public boolean handleInteraction(Vector3 playerPos) {
        if (activeLevel != null) {
            return activeLevel.onInteract(playerPos, player, hud);
        }
        return false;
    }

    public boolean handlePlayerAttack() {
        if (activeLevel != null && activeLevel.isCombatEnabled()) {
            return activeLevel.onPlayerAttack(player, camera, hud);
        }
        return false;
    }

    public void nextLevel() {
        if (currentLevelIndex < 6) {
            loadLevel(currentLevelIndex + 1);
        } else {
            // Replay from beginning
            loadLevel(1);
        }
    }

    public void restartCurrentLevel() {
        loadLevel(currentLevelIndex);
    }

    public CampaignLevel getActiveLevel() {
        return activeLevel;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    @Override
    public void dispose() {
        if (activeLevel != null) {
            activeLevel.dispose();
            activeLevel = null;
        }
    }
}
