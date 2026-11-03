package bd.spark36.character;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CrowdWorldCollidersTest {

    @Test
    public void testCrowdWithSampleWorldColliders() {
        // Let's check Level1PeacefulCampus colliders
        bd.spark36.level.Level1PeacefulCampus lvl = new bd.spark36.level.Level1PeacefulCampus();
        PlayerController pc = new PlayerController();
        // Without GL, world cannot be built directly, but let's check what colliders exist
        System.out.println("Default player colliders: " + pc.getColliders().length);
    }
}
