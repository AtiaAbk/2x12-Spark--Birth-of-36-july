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

    // =========================================================================
    // LEVEL-AWARE STUDENT CROWD DIALOGUE (Historical Bengali & English Phrasing)
    // =========================================================================

    // Level 1: July 1 – 14 (Academic Life & Early Quota Reform Awareness)
    public static final DialogueLine[] LEVEL_1_STUDENT_LINES = {
        new DialogueLine("DU PHYSICS STUDENT", "পদার্থবিজ্ঞান বিভাগের শিক্ষার্থী",
            "দাবি আমাদের একটাই — মেধার যথাযথ মূল্যায়ন চাই, বৈষম্যহীন ভবিষ্যৎ চাই।",
            "Our demand is simple: merit-based evaluation and an equitable future."),
        new DialogueLine("LIBRARY SCHOLAR", "কেন্দ্রীয় লাইব্রেরির গবেষক",
            "লাইব্রেরিতে আজ খুব ভিড়। সাধারণ শিক্ষার্থীরা সবাই ন্যায্য অধিকার নিয়ে সচেতন হচ্ছে।",
            "The library is crowded today. General students are waking up to their rights."),
        new DialogueLine("CURZON RESIDENT", "কার্জন হল আবাসিক শিক্ষার্থী",
            "কার্জন হলের লাল ইটের প্রাঙ্গণে শতাব্দীর ঐতিহ্য। আমরা শান্তিপূর্ণ শিক্ষাঙ্গন চাই।",
            "Centuries of heritage live in Curzon's red bricks. We stand for a peaceful campus.")
    };

    // Level 2: July 15 – 16 (Student Resistance & Solidarity)
    public static final DialogueLine[] LEVEL_2_STUDENT_LINES = {
        new DialogueLine("STUDENT ACTIVIST", "আন্দোলনরত সাধারণ শিক্ষার্থী",
            "হলগুলো থেকে সব সাধারণ শিক্ষার্থীরা রাজপথে নেমে আসছে! কোনো হুমকি আমাদের থামাতে পারবে না।",
            "Students from every residential hall are pouring into the streets! No threats will stop us."),
        new DialogueLine("ARTS FACULTY STUDENT", "কলা অনুষদের শিক্ষার্থী",
            "রাজু ভাস্কর্যের সামনে চলো! সেখানে সবাই হাতে হাত রেখে মানবপ্রাচীর গড়ে তুলছে।",
            "Head to the Raju Memorial Sculpture! Everyone is forming a united human chain there.")
    };

    // Level 3: July 18 (Escalation, Tear Gas & Mutual Aid)
    public static final DialogueLine[] LEVEL_3_STUDENT_LINES = {
        new DialogueLine("FIRST AID VOLUNTEER", "প্রাথমিক চিকিৎসা দলের স্বেচ্ছাসেবক",
            "টিয়ারগ্যাসের ধোঁয়ায় চোখ জ্বলছে! বোতলের পানি নাও, মুখ ধুয়ে পাশে নিরাপদ ছায়ায় চলো।",
            "Tear gas in the air! Take fresh water, rinse your eyes, and move to safe cover."),
        new DialogueLine("FRONT-LINE STUDENT", "সংগ্রামী শিক্ষার্থী",
            "আমাদের ভ্রাতৃত্ব কেউ ভাঙতে পারবে না। আহত ভাই-বোনদের নিরাপদে সরিয়ে নিতে সাহায্য করো!",
            "Our brotherhood cannot be broken. Help move the injured to safety!")
    };

    // Level 4: July 19 – 20 (Internet Blackout & Curfew Vigilance)
    public static final DialogueLine[] LEVEL_4_STUDENT_LINES = {
        new DialogueLine("CAMPUS VIGIL STUDENT", "নিশাচর পাহারাদার শিক্ষার্থী",
            "ইন্টারনেট বন্ধ করে আমাদের স্তব্ধ করতে পারবে না। প্রতিটি গলিতে আমরা ঐক্যবদ্ধ আছি।",
            "They cut the internet but cannot silence the truth. We remain united across every corridor."),
        new DialogueLine("RESISTANCE COURIER", "যোগাযোগ সমন্বয়ক শিক্ষার্থী",
            "সাবধানে ছায়ায় ছায়ায় চলো। সার্চলাইটের আলো থেকে দূরে থেকে সহপাঠীদের খবর নাও।",
            "Stay in the shadows. Keep clear of the searchlights and check on your peers.")
    };

    // Level 5: August 3 – 4 (Mass Mobilization — 1-Point Demand)
    public static final DialogueLine[] LEVEL_5_STUDENT_LINES = {
        new DialogueLine("STUDENT CO-ORDINATOR", "কেন্দ্রীয় সমন্বয়ক শিক্ষার্থী",
            "এক দফা এক দাবি! পুরো জাতি আজ ঢাকার রাজপথে আমাদের পাশে দাঁড়িয়েছে!",
            "One point, one demand! The entire nation now marches alongside us on Dhaka's streets!"),
        new DialogueLine("MARCHING STUDENT", "মিছিলের সম্মুখ সারির শিক্ষার্থী",
            "শহীদ মিনার থেকে গণভবন — জনতার এই বাঁধভাঙা জোয়ার কেউ রুখতে পারবে না!",
            "From Shaheed Minar onward — nothing can hold back this unstoppable wave of the people!")
    };

    // Level 6: August 5 (Victory & Historic Epilogue — 36 July)
    public static final DialogueLine[] LEVEL_6_STUDENT_LINES = {
        new DialogueLine("CELEBRATING STUDENT", "বিজয়ী শিক্ষার্থী",
            "৩৬ জুলাইয়ের নতুন সূর্য উদিত হয়েছে! এবার দেশকে নতুন করে ন্যায় আর সাম্যের ভিত্তিতে গড়তে হবে।",
            "The new sun of 36 July has risen! Now we rebuild this nation upon justice and equity."),
        new DialogueLine("MEMORIAL GUARDIAN", "শহীদ স্মৃতি সংরক্ষক",
            "আমাদের শহীদ ভাই-বোনদের রক্ত আমরা বৃথা যেতে দেব না। তাদের আত্মত্যাগ চিরকাল বেঁচে থাকবে।",
            "We will never let our fallen peers' sacrifices be forgotten. Their memory lives forever.")
    };

    /**
     * Retrieves an authentic student dialogue line matching the active level and seed.
     */
    public static DialogueLine getStudentDialogue(int levelNumber, int seed) {
        DialogueLine[] pool;
        switch (levelNumber) {
            case 2:  pool = LEVEL_2_STUDENT_LINES; break;
            case 3:  pool = LEVEL_3_STUDENT_LINES; break;
            case 4:  pool = LEVEL_4_STUDENT_LINES; break;
            case 5:  pool = LEVEL_5_STUDENT_LINES; break;
            case 6:  pool = LEVEL_6_STUDENT_LINES; break;
            case 1:
            default: pool = LEVEL_1_STUDENT_LINES; break;
        }
        int index = Math.abs(seed) % pool.length;
        return pool[index];
    }
}
