package bd.spark36.interaction;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;

/**
 * Common interface for entities and world objects that the player can interact with
 * via the [E] key (Student NPCs, Historical Memorials, Boundary Stones, Medical Shelters, etc.).
 */
public interface Interactable {

    /**
     * Returns the 3D world position of this interactable entity.
     */
    Vector3 getInteractionPosition();

    /**
     * Max radius (in meters) within which the player can trigger interaction.
     */
    float getInteractionRadius();

    /**
     * UI Context Prompt displayed on HUD when in range and focused (e.g. "[E] TALK TO DU STUDENT").
     */
    String getInteractionPrompt();

    /**
     * Whether interaction is currently permissible given player state and target readiness.
     */
    boolean canInteract(Vector3 playerPos, float playerHeading);

    /**
     * Executes interaction logic. Returns true if the interaction was successfully initiated or consumed.
     */
    boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud);

    /**
     * Priority value for selection tie-breaking when multiple targets are within range.
     * Higher value = higher priority (e.g. Mission Objectives > NPCs > Ambient Sitting).
     */
    default int getInteractionPriority() {
        return 0;
    }

    /**
     * Unique identifier or label for this interactable (used for logging and mission binding).
     */
    default String getInteractableId() {
        return getClass().getSimpleName();
    }
}
