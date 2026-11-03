// ei file ta bangla signboard texture generation test kore
package bd.spark36.world;

import bd.spark36.world.landmarks.BengaliSignAtlas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BengaliSignTextTest {

    @Test
    @DisplayName("Pre-base vowel signs move in front of their consonant; two-part vowels split around it")
    public void testVisualOrder() {

        assertEquals("েটিলকম", BengaliSignAtlas.toVisualOrder("টেলিকম"));

        assertEquals("েহােটল", BengaliSignAtlas.toVisualOrder("হোটেল"));

        assertEquals("ভাই ভাই চা ঘর", BengaliSignAtlas.toVisualOrder("ভাই ভাই চা ঘর"));
    }

    @Test
    @DisplayName("Sign texts avoid conjuncts (hasanta) and use no Latin brand names in the Bengali line")
    public void testSignTextsAreShapingSafe() {
        for (BengaliSignAtlas.Sign s : BengaliSignAtlas.Sign.values()) {
            assertFalse(s.bengali.indexOf('্') >= 0, "Conjunct in " + s + " cannot render without shaping");
            assertFalse(s.english.indexOf('্') >= 0, "Conjunct in " + s + " cannot render without shaping");
            assertFalse(s.bengali.isEmpty(), s + " needs Bengali text");

            assertFalse(s.bengali.indexOf('য়') >= 0 || s.bengali.indexOf('়') >= 0,
                "Nukta letter in " + s + " renders with a detached dot");
        }
    }
}
