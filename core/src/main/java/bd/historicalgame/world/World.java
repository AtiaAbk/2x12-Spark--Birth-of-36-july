package bd.historicalgame.world;

import bd.historicalgame.world.level1.Level1World;

import com.badlogic.gdx.utils.Disposable;

/**
 * World controller for 2x12.
 */
public class World implements Disposable {

    private Level1World level1World;

    public World() {
        level1World = new Level1World();
    }

    public void update(float delta) {
        level1World.update(delta);
    }

    public void render() {
        level1World.render();
    }

    public Level1World getLevel1World() {
        return level1World;
    }

    /**
     * Keeps the active level's camera aspect ratio matched to
     * the window whenever it is resized.
     */
    public void resize(int width, int height) {

        if (level1World != null) {
            level1World.resize(width, height);
        }
    }

    /**
     * Forwarded from {@link bd.historicalgame.Main} after a
     * screenshot is saved so the active level can flash a
     * brief HUD confirmation.
     */
    public void notifyScreenshotSaved() {

        if (level1World != null) {
            level1World.notifyScreenshotSaved();
        }
    }

    @Override
    public void dispose() {

        if (level1World != null) {
            level1World.dispose();
        }
    }
}
