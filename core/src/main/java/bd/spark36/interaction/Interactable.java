// ei file ta player jader sathe kotha bolte ba interact korte pare tader interface
package bd.spark36.interaction;

import com.badlogic.gdx.math.Vector3;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;

public interface Interactable {

    Vector3 getInteractionPosition();

    float getInteractionRadius();

    String getInteractionPrompt();

    boolean canInteract(Vector3 playerPos, float playerHeading);

    boolean onInteract(Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud);

    default int getInteractionPriority() {
        return 0;
    }

    default String getInteractableId() {
        return getClass().getSimpleName();
    }
}
