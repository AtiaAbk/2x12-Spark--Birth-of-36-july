package bd.historicalgame.world;

/**
 * Controls the active world of 2x12.
 */
public class WorldManager {

    private World currentWorld;

    public void createWorld() {
        currentWorld = new World();
    }

    public void update(float delta) {
        if (currentWorld != null) {
            currentWorld.update(delta);
        }
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public boolean hasWorld() {
        return currentWorld != null;
    }
}
