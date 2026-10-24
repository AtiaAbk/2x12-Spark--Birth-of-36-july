package bd.spark36.capture;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhotoModeTest {

    @Test
    void orbitMatchesFollowCameraConvention() {
        Vector3 focus = new Vector3(1f, 2f, 3f);
        Vector3 behind = PhotoMode.orbitPosition(focus, 0f, 0f, 5f, new Vector3());
        assertEquals(1f, behind.x, 1e-4f);
        assertEquals(2f, behind.y, 1e-4f);
        assertEquals(8f, behind.z, 1e-4f);

        Vector3 side = PhotoMode.orbitPosition(focus, 90f, 0f, 5f, new Vector3());
        assertEquals(6f, side.x, 1e-3f);
        assertEquals(3f, side.z, 1e-3f);

        Vector3 above = PhotoMode.orbitPosition(focus, 0f, 90f, 5f, new Vector3());
        assertEquals(7f, above.y, 1e-3f);
    }

    @Test
    void orbitKeepsRequestedDistance() {
        Vector3 focus = new Vector3();
        Vector3 p = PhotoMode.orbitPosition(focus, 37f, 21f, 4.2f, new Vector3());
        assertEquals(4.2f, p.len(), 1e-3f);
    }
}
