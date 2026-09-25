package bd.spark36.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Cinematic 3rd-person over-the-shoulder camera for 2x12: Spark.
 * AAA-level enhancements: camera bob, FOV push on sprint, heading look-ahead,
 * impulse-based screen shake system.
 */
public class CinematicCamera {

    private final PerspectiveCamera camera;

    // Follow target config
    private float distance = 3.3f;
    private final float minDistance = 2.2f;
    private final float maxDistance = 6.0f;
    private final float shoulderOffset = 0.28f;
    private final float targetHeight = 1.32f;

    // Angles (degrees)
    private float yaw = 0f;
    private float pitch = 5.0f;
    private final float minPitch = -15f;
    private final float maxPitch = 38f;

    // Mouse & Trackpad
    private float mouseSensitivity = 0.22f;
    private boolean mouseLookActive = true;
    private int initFrameSkip = 2;

    // Smooth interpolation
    private final Vector3 currentCameraPos = new Vector3();
    private final Vector3 desiredCameraPos = new Vector3();
    private final Vector3 targetFocus = new Vector3();
    private final Vector3 currentLookAt = new Vector3();

    // ─── Camera Bob ───────────────────────────────────────────────────
    private float bobTime = 0f;
    private float bobAmplitude = 0.045f;  // 4.5cm vertical bob
    private float bobFrequency = 1.85f;   // ~2 steps per second
    private float currentBob = 0f;
    private float bobDecay = 0f;           // 0 = walking, 1 = still

    // ─── FOV System ───────────────────────────────────────────────────
    private float baseFov = 65f;
    private float sprintFov = 79f;
    private float currentFov = 65f;
    private float targetFov = 65f;

    // ─── Look-Ahead ───────────────────────────────────────────────────
    private float lookAheadStrength = 0.30f;
    private final Vector3 lookAheadOffset = new Vector3();
    private float currentHeadingX = 0f;
    private float currentHeadingZ = 0f;

    // ─── Screen Shake ─────────────────────────────────────────────────
    private float shakeMagnitude = 0f;
    private float shakeDecay = 8.0f;       // how fast shake fades
    private float shakeTime = 0f;
    private final Vector3 shakeOffset = new Vector3();

    public CinematicCamera(int viewportWidth, int viewportHeight) {
        camera = new PerspectiveCamera(baseFov, viewportWidth, viewportHeight);
        camera.near = 0.2f;
        camera.far = 350f;
        camera.position.set(0.28f, 1.85f, 52.7f);
        camera.lookAt(0f, 1.32f, 49.4f);
        camera.update();

        currentCameraPos.set(camera.position);
        currentLookAt.set(0f, 1.32f, 49.4f);
    }

    /**
     * Updates camera from input and player state.
     * @param isMoving   whether player is currently walking
     * @param isSprinting whether player is sprinting
     * @param headingDeg  player facing direction in degrees
     */
    public void update(float delta, Vector3 playerPos, boolean inputEnabled) {
        update(delta, playerPos, inputEnabled, false, false, 0f);
    }

    public void update(float delta, Vector3 playerPos, boolean inputEnabled,
                       boolean isMoving, boolean isSprinting, float headingDeg) {
        // 1. Mouse/Trackpad rotation
        if (inputEnabled && mouseLookActive) {
            if (!Gdx.input.isCursorCatched()) Gdx.input.setCursorCatched(true);

            if (initFrameSkip > 0) {
                initFrameSkip--;
                Gdx.input.getDeltaX();
                Gdx.input.getDeltaY();
            } else {
                float rawDeltaX = Gdx.input.getDeltaX();
                float rawDeltaY = Gdx.input.getDeltaY();
                float deltaX = MathUtils.clamp(rawDeltaX, -100f, 100f);
                float deltaY = MathUtils.clamp(rawDeltaY, -100f, 100f);
                if (Math.abs(deltaX) > 0.001f || Math.abs(deltaY) > 0.001f) {
                    yaw -= deltaX * mouseSensitivity;
                    pitch -= deltaY * mouseSensitivity;
                    yaw = (yaw % 360f + 360f) % 360f;
                    pitch = MathUtils.clamp(pitch, minPitch, maxPitch);
                }
            }
        }

        // 2. Camera bob — only when moving, fades out when still
        if (isMoving && inputEnabled) {
            float bobRate = isSprinting ? bobFrequency * 1.55f : bobFrequency;
            bobTime += delta * bobRate * MathUtils.PI2;
            float targetBob = MathUtils.sin(bobTime) * bobAmplitude * (isSprinting ? 1.5f : 1f);
            currentBob = currentBob + (targetBob - currentBob) * Math.min(1f, 18f * delta);
        } else {
            // Decay bob smoothly when stopping
            currentBob *= Math.max(0f, 1f - 8f * delta);
        }

        // 3. FOV push
        targetFov = isSprinting && isMoving ? sprintFov : baseFov;
        currentFov += (targetFov - currentFov) * Math.min(1f, 5f * delta);
        camera.fieldOfView = currentFov;

        // 4. Look-ahead offset (camera leads toward player facing direction)
        float headRad = headingDeg * MathUtils.degreesToRadians;
        float targetLAX = -MathUtils.sin(headRad) * lookAheadStrength * (isMoving ? 1f : 0.3f);
        float targetLAZ = -MathUtils.cos(headRad) * lookAheadStrength * (isMoving ? 1f : 0.3f);
        currentHeadingX += (targetLAX - currentHeadingX) * Math.min(1f, 4f * delta);
        currentHeadingZ += (targetLAZ - currentHeadingZ) * Math.min(1f, 4f * delta);
        lookAheadOffset.set(currentHeadingX, 0f, currentHeadingZ);

        // 5. Screen shake decay
        if (shakeMagnitude > 0.0001f) {
            shakeMagnitude -= shakeDecay * delta * shakeMagnitude;
            shakeTime += delta * 28f;
            shakeOffset.set(
                MathUtils.sin(shakeTime * 2.1f) * shakeMagnitude,
                MathUtils.cos(shakeTime * 3.7f) * shakeMagnitude,
                0f
            );
        } else {
            shakeMagnitude = 0f;
            shakeOffset.setZero();
        }

        // 6. Target focus = player chest + look-ahead
        targetFocus.set(
            playerPos.x + lookAheadOffset.x,
            playerPos.y + targetHeight + currentBob,
            playerPos.z + lookAheadOffset.z
        );

        // 7. Spherical orbit
        float yawRad   = yaw * MathUtils.degreesToRadians;
        float pitchRad = pitch * MathUtils.degreesToRadians;
        float cosPitch = MathUtils.cos(pitchRad);
        float sinPitch = MathUtils.sin(pitchRad);
        float cosYaw   = MathUtils.cos(yawRad);
        float sinYaw   = MathUtils.sin(yawRad);

        float ox = sinYaw * cosPitch * distance;
        float oy = sinPitch * distance;
        float oz = cosYaw * cosPitch * distance;

        float rightX = cosYaw * shoulderOffset;
        float rightZ = -sinYaw * shoulderOffset;

        desiredCameraPos.set(
            targetFocus.x + ox + rightX + shakeOffset.x,
            targetFocus.y + oy + shakeOffset.y,
            targetFocus.z + oz + rightZ
        );

        if (desiredCameraPos.y < 0.45f) desiredCameraPos.y = 0.45f;

        // 8. Smooth cinematic lerp
        float followSpeed = 12f;
        currentCameraPos.lerp(desiredCameraPos, Math.min(1f, followSpeed * delta));
        currentLookAt.lerp(targetFocus, Math.min(1f, (followSpeed + 4f) * delta));

        camera.position.set(currentCameraPos);
        camera.up.set(Vector3.Y);
        camera.lookAt(currentLookAt);
        camera.update();
    }

    /** Trigger a screen shake impulse. magnitude in world units (0.05 = subtle, 0.2 = strong). */
    public void addShake(float magnitude) {
        shakeMagnitude = Math.min(shakeMagnitude + magnitude, 0.35f);
        shakeTime = 0f;
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public PerspectiveCamera getCamera() { return camera; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getCurrentFov() { return currentFov; }
    public void setMouseLookActive(boolean active) { mouseLookActive = active; }
    public boolean isMouseLookActive() { return mouseLookActive; }
}
