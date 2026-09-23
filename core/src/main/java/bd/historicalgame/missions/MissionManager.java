package bd.historicalgame.missions;

import bd.historicalgame.game.GameConfig;
import com.badlogic.gdx.utils.Array;

/**
 * Controls the mission sequence for 2x12.
 *
 * Level 1 mission flow:
 *
 * Mission 1
 *     ↓
 * Investigate Main Building
 *
 * Mission 2
 *     ↓
 * Explore Central Courtyard
 *
 * Mission 3
 *     ↓
 * Find the Historical Clue
 *
 * Mission 4
 *     ↓
 * Return to Main Gate
 *
 * After Mission 4:
 * LEVEL 1 COMPLETE
 */
public class MissionManager {

    // =========================================================
    // MISSION LIST
    // =========================================================

    private final Array<Mission> missions;

    /*
     * Index of the currently active mission.
     */
    private int currentMissionIndex;


    // =========================================================
    // STATE
    // =========================================================

    private boolean levelComplete;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MissionManager() {

        missions = new Array<>();

        currentMissionIndex = 0;

        levelComplete = false;

        createLevel1Missions();
    }


    // =========================================================
    // LEVEL 1 MISSIONS
    // =========================================================

    private void createLevel1Missions() {

        /*
         * Mission 1
         *
         * Main Building:
         * X = 0
         * Z = -10
         */
        missions.add(
            new Mission(
                1,
                "Investigate the Main Building",
                "Go to the main building and investigate what is happening.",
                0f,
                -10f
            )
        );


        /*
         * Mission 2
         *
         * Central Courtyard:
         * X = 0
         * Z = 5
         */
        missions.add(
            new Mission(
                2,
                "Explore the Central Courtyard",
                "Explore the courtyard and look for something unusual.",
                0f,
                5f
            )
        );


        /*
         * Mission 3
         *
         * Historical clue location.
         *
         * We place this slightly away from the
         * center so the player has to explore.
         */
        missions.add(
            new Mission(
                3,
                "Find the Historical Clue",
                "Search the eastern side of the campus for an important clue.",
                10f,
                2f
            )
        );


        /*
         * Mission 4
         *
         * Return to the main gate.
         */
        missions.add(
            new Mission(
                4,
                "Return to the Main Gate",
                "Return to the main gate to complete the investigation.",
                0f,
                15f
            )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates the active mission using the player's position.
     *
     * @param playerX player's X coordinate
     * @param playerZ player's Z coordinate
     */
    public void update(
        float playerX,
        float playerZ
    ) {

        if (levelComplete) {
            return;
        }

        Mission current =
            getCurrentMission();

        if (current == null) {
            levelComplete = true;
            return;
        }

        /*
         * Calculate distance from player
         * to current objective.
         */
        float distance =
            current.getDistanceTo(
                playerX,
                playerZ
            );

        /*
         * Automatically complete the mission
         * when the player reaches the target.
         */
        if (distance <=
            GameConfig.MISSION_COMPLETE_DISTANCE) {

            completeCurrentMission();
        }
    }


    // =========================================================
    // COMPLETE CURRENT MISSION
    // =========================================================

    /**
     * Completes the currently active mission
     * and moves to the next mission.
     */
    public void completeCurrentMission() {

        if (levelComplete) {
            return;
        }

        Mission current =
            getCurrentMission();

        if (current == null) {
            levelComplete = true;
            return;
        }

        /*
         * Prevent completing the same mission twice.
         */
        if (!current.isCompleted()) {
            current.complete();
        }

        /*
         * Move to next mission.
         */
        currentMissionIndex++;

        /*
         * Check whether all Level 1 missions
         * have been completed.
         */
        if (currentMissionIndex >=
            missions.size) {

            levelComplete = true;
        }
    }


    // =========================================================
    // CURRENT MISSION
    // =========================================================

    /**
     * Returns the currently active mission.
     *
     * Returns null when Level 1 is complete.
     */
    public Mission getCurrentMission() {

        if (levelComplete) {
            return null;
        }

        if (currentMissionIndex < 0 ||
            currentMissionIndex >= missions.size) {

            return null;
        }

        return missions.get(
            currentMissionIndex
        );
    }


    // =========================================================
    // MISSION INFORMATION
    // =========================================================

    /**
     * Returns all Level 1 missions.
     */
    public Array<Mission> getMissions() {
        return missions;
    }


    /**
     * Returns the current mission number.
     *
     * Example:
     *
     * Mission 1 → returns 1
     * Mission 2 → returns 2
     */
    public int getCurrentMissionNumber() {

        if (levelComplete) {
            return missions.size;
        }

        return currentMissionIndex + 1;
    }


    /**
     * Returns total number of missions.
     */
    public int getTotalMissions() {
        return missions.size;
    }


    /**
     * Returns the distance from the player
     * to the current objective.
     */
    public float getCurrentMissionDistance(
        float playerX,
        float playerZ
    ) {

        Mission current =
            getCurrentMission();

        if (current == null) {
            return 0f;
        }

        return current.getDistanceTo(
            playerX,
            playerZ
        );
    }


    // =========================================================
    // LEVEL STATUS
    // =========================================================

    /**
     * Returns true when all Level 1 missions
     * have been completed.
     */
    public boolean isLevelComplete() {
        return levelComplete;
    }


    /**
     * Resets the entire mission sequence.
     *
     * Useful for restarting Level 1.
     */
    public void reset() {

        for (Mission mission :
             missions) {

            mission.reset();
        }

        currentMissionIndex = 0;

        levelComplete = false;
    }
}