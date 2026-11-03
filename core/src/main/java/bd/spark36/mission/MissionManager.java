// ei file ta 5-mission campaign system manage kore
package bd.spark36.mission;

import com.badlogic.gdx.Gdx;
import bd.spark36.app.GameProfile;
import bd.spark36.level.LevelConfig;

public class MissionManager {

    public static final int TOTAL_MISSIONS = 5;

    public static class MissionInfo {
        public final int number;
        public final String title;
        public final String subtitle;
        public final String defaultObjective;
        public final int targetCount;

        public MissionInfo(int number, String title, String subtitle, String defaultObjective, int targetCount) {
            this.number = number;
            this.title = title;
            this.subtitle = subtitle;
            this.defaultObjective = defaultObjective;
            this.targetCount = targetCount;
        }
    }

    private static final MissionInfo[] MISSIONS = {
        new MissionInfo(1, "CAMPUS EXPLORATION", "ক্যাম্পাস পরিচিতি", "Meet 5 campus members.", 5),
        new MissionInfo(2, "CAMPUS INTERACTION", "শিক্ষার্থী সংলাপ", "Complete dialogue with designated campus members.", 4),
        new MissionInfo(3, "HOSTILE ENCOUNTER", "প্রতিরোধ ও আত্মরক্ষা", "Defeat hostile student-group opponents (protect civilians).", 3),
        new MissionInfo(4, "COMBAT CHALLENGE", "কঠোর সংঘাত", "Defeat advanced hostile opponents using timing & defense.", 4),
        new MissionInfo(5, "FINAL MISSION", "৩৬ জুলাইয়ের বিজয়", "Secure the Grand Assembly dais & proclaim victory.", 4)
    };

    private static final MissionManager INSTANCE = new MissionManager();

    public static MissionManager get() {
        return INSTANCE;
    }

    private int activeMission = 1;
    private int progress = 0;
    private String customObjective = null;
    private boolean completed = false;
    private boolean aborted = false;
    private String abortReason = "You attacked a protected campus member.";

    public MissionManager() {
        resetToCurrentLevel();
    }

    public void resetToCurrentLevel() {
        int lvl = LevelConfig.getCurrentLevel();
        startMission(Math.max(1, Math.min(TOTAL_MISSIONS, lvl)));
    }

    public void startMission(int missionNumber) {
        this.activeMission = Math.max(1, Math.min(TOTAL_MISSIONS, missionNumber));
        this.progress = 0;
        this.customObjective = null;
        this.completed = false;
        this.aborted = false;
        this.abortReason = "You attacked a protected campus member.";
    }

    public int getActiveMissionNumber() {
        return activeMission;
    }

    public MissionInfo getActiveMissionInfo() {
        return MISSIONS[activeMission - 1];
    }

    public static MissionInfo getMissionInfo(int missionNumber) {
        int idx = Math.max(1, Math.min(TOTAL_MISSIONS, missionNumber)) - 1;
        return MISSIONS[idx];
    }

    public String getMissionTitle() {
        return getActiveMissionInfo().title;
    }

    public String getMissionSubtitle() {
        return getActiveMissionInfo().subtitle;
    }

    public String getCurrentObjective() {
        if (customObjective != null && !customObjective.isEmpty()) {
            return customObjective;
        }
        return getActiveMissionInfo().defaultObjective;
    }

    public void setCustomObjective(String objective) {
        this.customObjective = objective;
    }

    public int getProgress() {
        return progress;
    }

    public int getTargetCount() {
        return getActiveMissionInfo().targetCount;
    }

    public void setProgress(int count) {
        this.progress = Math.max(0, Math.min(getTargetCount(), count));
        if (this.progress >= getTargetCount() && !completed) {
            markComplete();
        }
    }

    public void incrementProgress() {
        setProgress(this.progress + 1);
    }

    public void advanceProgress(int delta) {
        setProgress(this.progress + delta);
    }

    public boolean isMissionComplete() {
        return completed;
    }

    public void completeMission() {
        markComplete();
    }

    public void markComplete() {
        if (completed) return;
        completed = true;
        this.progress = getTargetCount();
        LevelConfig.unlockNextLevel(activeMission);
        if (Gdx.app != null) {
            Gdx.app.log("MissionManager", String.format("Mission %d/5 (%s) COMPLETED!", activeMission, getMissionTitle()));
        }
    }

    public boolean isMissionAborted() {
        return aborted;
    }

    public String getAbortReason() {
        return abortReason;
    }

    public void abortMission(String reason) {
        this.aborted = true;
        if (reason != null && !reason.trim().isEmpty()) {
            this.abortReason = reason;
        }
        if (Gdx.app != null) {
            Gdx.app.log("MissionManager", String.format("Mission %d/5 ABORTED: %s", activeMission, this.abortReason));
        }
    }

    public void clearAbort() {
        this.aborted = false;
    }

    public int getUnlockedMission() {
        return Math.max(1, Math.min(TOTAL_MISSIONS, LevelConfig.getUnlockedLevel()));
    }

    public int getCompletedCampaignCount() {
        int unlocked = getUnlockedMission();
        if (completed && activeMission == TOTAL_MISSIONS) {
            return TOTAL_MISSIONS;
        }
        return Math.max(0, unlocked - 1);
    }

    public String getFormattedHudHeader() {
        return String.format("MISSION %d/%d — %s", activeMission, TOTAL_MISSIONS, getMissionTitle());
    }

    public String getFormattedProgress() {
        return String.format("Progress: %d/%d", progress, getTargetCount());
    }

    public String getFormattedCampaignProgress() {
        return String.format("Campaign: %d/%d Complete", getCompletedCampaignCount(), TOTAL_MISSIONS);
    }
}
