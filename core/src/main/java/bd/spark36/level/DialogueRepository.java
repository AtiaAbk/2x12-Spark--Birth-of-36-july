package bd.spark36.level;

/**
 * Data-driven dialogue repository containing authentic Bengali and English subtitles.
 * Replaces hardcoded strings and phonetic Banglish with verified cultural phrasing.
 */
public class DialogueRepository {

    public static class DialogueLine {
        private final String speaker;
        private final String speakerBengali;
        private final String textBengali;
        private final String textEnglish;

        public DialogueLine(String speaker, String speakerBengali, String textBengali, String textEnglish) {
            this.speaker = speaker;
            this.speakerBengali = speakerBengali;
            this.textBengali = textBengali;
            this.textEnglish = textEnglish;
        }

        public String getSpeaker() {
            return speaker;
        }

        public String getSpeakerBengali() {
            return speakerBengali;
        }

        public String getTextBengali() {
            return textBengali;
        }

        public String getTextEnglish() {
            return textEnglish;
        }
    }

    /**
     * Arena Combat Encounter Dialogue: Helmeted Aggressor's challenge.
     * Bengali: "ক্যাম্পাস খালি করো! এখনই এখান থেকে চলে যাও!"
     * English: "Leave this campus immediately, or face the consequences!"
     */
    public static final DialogueLine AGGRESSOR_CHALLENGE = new DialogueLine(
        "HELMETED AGGRESSOR",
        "হেলমেট পরা আক্রমণকারী",
        "ক্যাম্পাস খালি করো! এখনই এখান থেকে চলে যাও!",
        "Leave this campus immediately, or face the consequences!"
    );

    /**
     * Peaceful Level 1 Encounter Dialogue: Campus Security / Warden's friendly greeting.
     * Bengali: "কার্জন হলে স্বাগতম। ক্যাম্পাসের শান্তিশৃঙ্খলা বজায় রাখুন।"
     * English: "Welcome to Curzon Hall. Please maintain peace on campus."
     */
    public static final DialogueLine CAMPUS_SECURITY_PEACEFUL = new DialogueLine(
        "CAMPUS SECURITY",
        "ক্যাম্পাস নিরাপত্তা কর্মী",
        "কার্জন হলে স্বাগতম। ক্যাম্পাসের শান্তিশৃঙ্খলা বজায় রাখুন।",
        "Welcome to Curzon Hall. Please maintain peace on campus."
    );
}
