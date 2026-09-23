package bd.historicalgame.missions;

/**
 * Represents a single mission/objective in 2x12.
 *
 * A mission contains:
 * - Mission ID
 * - Title
 * - Description
 * - Target position
 * - Completion status
 */
public class Mission {

    // =========================================================
    // MISSION DATA
    // =========================================================

    private final int id;

    private final String title;

    private final String description;

    private final float targetX;

    private final float targetZ;

    // =========================================================
    // STATE
    // =========================================================

    private boolean completed;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Mission(
        int id,
        String title,
        String description,
        float targetX,
        float targetZ
    ) {

        this.id = id;

        this.title = title;

        this.description = description;

        this.targetX = targetX;

        this.targetZ = targetZ;

        this.completed = false;
    }


    // =========================================================
    // GETTERS
    // =========================================================

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetZ() {
        return targetZ;
    }


    // =========================================================
    // COMPLETION
    // =========================================================

    /**
     * Returns true if this mission has been completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks this mission as completed.
     */
    public void complete() {
        completed = true;
    }

    /**
     * Resets this mission.
     *
     * Useful later for save/load or restarting a level.
     */
    public void reset() {
        completed = false;
    }


    // =========================================================
    // DISTANCE
    // =========================================================

    /**
     * Calculates the 2D distance from the player
     * to this mission's target.
     *
     * Only X and Z are considered because Level 1
     * movement happens on the ground.
     */
    public float getDistanceTo(
        float playerX,
        float playerZ
    ) {

        float dx =
            targetX - playerX;

        float dz =
            targetZ - playerZ;

        return (float) Math.sqrt(
            dx * dx +
            dz * dz
        );
    }


    // =========================================================
    // STATUS
    // =========================================================

    @Override
    public String toString() {

        return "Mission{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", completed=" + completed +
            ", targetX=" + targetX +
            ", targetZ=" + targetZ +
            '}';
    }
}
