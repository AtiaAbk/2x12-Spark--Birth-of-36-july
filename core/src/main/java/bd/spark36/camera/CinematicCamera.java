package bd.spark36.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Cinematic 3rd-person over-the-shoulder camera inspired by Where Winds Meet.
 * Features smooth spring-damper following, mouse look orbital controls,
 * shoulder offset, and ground clipping prevention.
 */
public class CinematicCamera {

    private final PerspectiveCamera camera;

    // Follow target configuration
    private float distance = 4.4f;
    private final float minDistance = 2.8f;
    private final float maxDistance = 6.5f;
    private final float shoulderOffset = 0.35f; // Slight right-shoulder offset
    private final float targetHeight = 1.45f;   // Player chest/eye level

    // Angles (degrees)
    private float yaw = 0f;    // 0 is looking North (-Z) towards Curzon Hall
    private float pitch = 7.5f; // Gentle over-the-shoulder angle
    private final float minPitch = -15f;
    private final float maxPitch = 38f;

    // Mouse sensitivity & frame skipping
    private float mouseSensitivity = 0.18f;
    private boolean mouseLookActive = true;
    private int initFrameSkip = 5; // Skip OS window mouse jump on startup

    // Smooth interpolation
    private final Vector3 currentCameraPos = new Vector3();
    private final Vector3 desiredCameraPos = new Vector3();
    private final Vector3 targetFocus = new Vector3();
    private final Vector3 currentLookAt = new Vector3();

    public CinematicCamera(int viewportWidth, int viewportHeight) {
        camera = new PerspectiveCamera(65f, viewportWidth, viewportHeight);
        camera.near = 0.2f;
        camera.far = 350f;
        camera.position.set(0f, 2.1f, 29.5f);
        camera.lookAt(0f, 1.45f, 25f);
        camera.update();

        currentCameraPos.set(camera.position);
        currentLookAt.set(0f, 1.45f, 25f);
    }

    /**
     * Updates camera rotation from mouse input and interpolates position smoothly.
     */
    public void update(float delta, Vector3 playerPos, boolean inputEnabled) {
        // 1. Mouse rotation when active
        if (inputEnabled && mouseLookActive) {
            if (initFrameSkip > 0) {
                initFrameSkip--;
                Gdx.input.getDeltaX();
                Gdx.input.getDeltaY();
            } else {
                float deltaX = MathUtils.clamp(Gdx.input.getDeltaX(), -30f, 30f);
                float deltaY = MathUtils.clamp(Gdx.input.getDeltaY(), -30f, 30f);

                if (Math.abs(deltaX) > 0.001f || Math.abs(deltaY) > 0.001f) {
                    yaw -= deltaX * mouseSensitivity;
                    pitch -= deltaY * mouseSensitivity;

                    // Keep yaw in [0, 360)
                    yaw = (yaw % 360f + 360f) % 360f;
                    pitch = MathUtils.clamp(pitch, minPitch, maxPitch);
                }
            }
        }

        // 2. Calculate target look-at point (player chest/head)
        targetFocus.set(playerPos.x, playerPos.y + targetHeight, playerPos.z);

        // 3. Spherical coordinates to world camera position
        float yawRad = yaw * MathUtils.degreesToRadians;
        float pitchRad = pitch * MathUtils.degreesToRadians;

        float cosPitch = MathUtils.cos(pitchRad);
        float sinPitch = MathUtils.sin(pitchRad);
        float cosYaw = MathUtils.cos(yawRad);
        float sinYaw = MathUtils.sin(yawRad);

        // Orbit offset from focus point
        float ox = sinYaw * cosPitch * distance;
        float oy = sinPitch * distance;
        float oz = cosYaw * cosPitch * distance;

        // Shoulder offset (perpendicular to viewing vector in XZ plane)
        float rightX = cosYaw * shoulderOffset;
        float rightZ = -sinYaw * shoulderOffset;

        desiredCameraPos.set(
            targetFocus.x + ox + rightX,
            targetFocus.y + oy,
            targetFocus.z + oz + rightZ
        );

        // Ground collision avoidance (keep camera above ground)
        if (desiredCameraPos.y < 0.45f) {
            desiredCameraPos.y = 0.45f;
        }

        // 4. Smooth cinematic lerp
        float followSpeed = 12f;
        currentCameraPos.lerp(desiredCameraPos, Math.min(1f, followSpeed * delta));
        currentLookAt.lerp(targetFocus, Math.min(1f, (followSpeed + 4f) * delta));

        camera.position.set(currentCameraPos);
        camera.up.set(Vector3.Y);
        camera.lookAt(currentLookAt);
        camera.update();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setMouseLookActive(boolean active) {
        this.mouseLookActive = active;
    }

    public boolean isMouseLookActive() {
        return mouseLookActive;
    }
}
