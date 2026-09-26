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

    // Camera-vs-world collision
    private static final float CAMERA_MARGIN = 0.30f;
    private static final float MIN_ARM_FRACTION = 0.12f;
    private float[][] colliders;

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
        // Debug/test hooks so automated screenshots can inspect the player from any side and range
        yaw = Float.parseFloat(System.getProperty("bd.spark36.cameraYaw", "0"));
        distance = Float.parseFloat(System.getProperty("bd.spark36.cameraDistance", String.valueOf(distance)));
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

        pullInFromSolids(targetFocus, desiredCameraPos);

        // 8. Smooth cinematic lerp
        float followSpeed = 12f;
        currentCameraPos.lerp(desiredCameraPos, Math.min(1f, followSpeed * delta));
        currentLookAt.lerp(targetFocus, Math.min(1f, (followSpeed + 4f) * delta));

        camera.position.set(currentCameraPos);
        camera.up.set(Vector3.Y);
        camera.lookAt(currentLookAt);
        camera.update();
    }

    /** Solid boxes ({minX, minY, minZ, maxX, maxY, maxZ}) the camera must not end up inside. */
    public void setColliders(float[][] boxes) {
        this.colliders = boxes;
    }

    /**
     * Shortens the focus-to-camera arm so the camera stops in front of the first solid it would
     * pass through, instead of poking through walls and showing the inside of buildings.
     * Boxes are grown by CAMERA_MARGIN so the near plane never clips a wall face.
     */
    private void pullInFromSolids(Vector3 focus, Vector3 camPos) {
        if (colliders == null) return;

        float dx = camPos.x - focus.x, dy = camPos.y - focus.y, dz = camPos.z - focus.z;
        float nearest = 1f;

        for (float[] b : colliders) {
            float tEnter = 0f, tExit = 1f;
            boolean hit = true;
            for (int axis = 0; axis < 3 && hit; axis++) {
                float o = axis == 0 ? focus.x : (axis == 1 ? focus.y : focus.z);
                float d = axis == 0 ? dx : (axis == 1 ? dy : dz);
                float lo = b[axis] - CAMERA_MARGIN;
                float hi = b[axis + 3] + CAMERA_MARGIN;
                if (Math.abs(d) < 1e-6f) {
                    if (o < lo || o > hi) hit = false;
                } else {
                    float t1 = (lo - o) / d, t2 = (hi - o) / d;
                    if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
                    tEnter = Math.max(tEnter, t1);
                    tExit = Math.min(tExit, t2);
                    if (tEnter > tExit) hit = false;
                }
            }
            // tEnter == 0 means the focus is already inside the grown box (hugging a wall);
            // leave the arm alone rather than shove the camera into the player's head.
            if (hit && tEnter > 0f && tEnter < nearest) nearest = tEnter;
        }

        if (nearest < 1f) {
            float t = Math.max(nearest, MIN_ARM_FRACTION);
            camPos.set(focus.x + dx * t, focus.y + dy * t, focus.z + dz * t);
        }
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
