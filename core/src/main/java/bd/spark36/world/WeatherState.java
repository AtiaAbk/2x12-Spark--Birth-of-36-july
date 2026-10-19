package bd.spark36.world;

/**
 * Historical atmospheric and weather conditions for Spark: Birth of 36 July.
 * Recreates authentic Dhaka summer weather during the July 2024 student movement:
 * scorching clear summer sun, golden afternoon hours, heavy pre-monsoon cloud cover,
 * and seasonal monsoon downpours.
 */
public enum WeatherState {
    CLEAR_SUNNY("Clear Summer Daylight", "পরিষ্কার রৌদ্রোজ্জ্বল দিন"),
    GOLDEN_HOUR("Golden Hour Sunset", "গোধূলি বেলা"),
    OVERCAST_MONSOON("Overcast Monsoon Sky", "মেঘলা বর্ষার আকাশ"),
    MONSOON_RAIN("Monsoon Downpour", "শ্রাবণের ধারা / প্রবল বর্ষণ");

    public final String displayName;
    public final String banglaName;

    WeatherState(String displayName, String banglaName) {
        this.displayName = displayName;
        this.banglaName = banglaName;
    }

    public WeatherState next() {
        WeatherState[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
