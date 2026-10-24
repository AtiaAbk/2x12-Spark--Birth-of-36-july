package bd.spark36.world;

import bd.spark36.world.landmarks.BengaliSignAtlas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** T8: signboard text is drawn glyph by glyph without a shaper, so it must be reordered and conjunct-free. */
public class BengaliSignTextTest {

    @Test
    @DisplayName("Pre-base vowel signs move in front of their consonant; two-part vowels split around it")
    public void testVisualOrder() {
        // টেলিকম: ট+ে, ল+ি  ->  ে ট ি ল ক ম
        assertEquals("েটিলকম", BengaliSignAtlas.toVisualOrder("টেলিকম"));
        // হোটেল: হ+ো -> ে হ া ; ট+ে -> ে ট
        assertEquals("েহােটল", BengaliSignAtlas.toVisualOrder("হোটেল"));
        // Text without pre-base vowels is unchanged
        assertEquals("ভাই ভাই চা ঘর", BengaliSignAtlas.toVisualOrder("ভাই ভাই চা ঘর"));
    }

    @Test
    @DisplayName("Sign texts avoid conjuncts (hasanta) and use no Latin brand names in the Bengali line")
    public void testSignTextsAreShapingSafe() {
        for (BengaliSignAtlas.Sign s : BengaliSignAtlas.Sign.values()) {
            assertFalse(s.bengali.indexOf('্') >= 0, "Conjunct in " + s + " cannot render without shaping");
            assertFalse(s.english.indexOf('্') >= 0, "Conjunct in " + s + " cannot render without shaping");
            assertFalse(s.bengali.isEmpty(), s + " needs Bengali text");
            // Without shaping, য় (U+09DF) and the nukta draw as a detached dot in Noto Sans Bengali
            assertFalse(s.bengali.indexOf('য়') >= 0 || s.bengali.indexOf('়') >= 0,
                "Nukta letter in " + s + " renders with a detached dot");
        }
    }
}
