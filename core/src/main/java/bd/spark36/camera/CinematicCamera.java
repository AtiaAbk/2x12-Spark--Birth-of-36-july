// ei file ta third person ar cinematic camera movement control kore
package bd.spark36.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class CinematicCamera {

    private final PerspectiveCamera camera;

    private static final float CAMERA_MARGIN = 0.30f;
    private static final float MIN_ARM_FRACTION = 0.12f;
    private float[][] colliders;

    private float distance = 3.3f;
    private final float minDistance = 2.2f;
    private final float maxDistance = 6.0f;
    private float shoulderOffset = 0.28f;
    private float targetHeight = 1.32f;

    private float yaw = 0f;
    private float pitch = 5.0f;
    private final float minPitch = -15f;
    private final float maxPitch = 38f;

    private float mouseSensitivity = 0.22f;

    private static volatile float userSensitivity = 1.0f;
    private static volatile boolean reduceMotion = false;

    public static void setUserSensitivity(float multiplier) {
        if (Float.isNaN(multiplier)) multiplier = 1f;
        userSensitivity = MathUtils.clamp(multiplier, 0.25f, 3.0f);
    }

    public static float getUserSensitivity() { return userSensitivity; }

    public static void setReduceMotion(boolean on) { reduceMotion = on; }

    public static boolean isReduceMotion() { return reduceMotion; }

    public float getEffectiveSensitivity() { return mouseSensitivity * userSensitivity; }

    public float getCurrentBob() { return currentBob; }

    public Vector3 getShakeOffset() { return shakeOffset; }
    private boolean mouseLookActive = true;
    private int initFrameSkip = 2;

    private final Vector3 currentCameraPos = new Vector3();
    private final Vector3 desiredCameraPos = new Vector3();
    private final Vector3 targetFocus = new Vector3();
    private final Vector3 currentLookAt = new Vector3();

    private boolean fixedView = false;
    private final Vector3 fixedPos = new Vector3();
    private final Vector3 fixedLookAt = new Vector3();

    public void setFixedView(float posX, float posY, float posZ, float targetX, float targetY, float targetZ) {
        this.fixedView = true;
        this.fixedPos.set(posX, posY, posZ);
        this.fixedLookAt.set(targetX, targetY, targetZ);
        this.currentCameraPos.set(fixedPos);
        this.currentLookAt.set(fixedLookAt);
        this.camera.position.set(fixedPos);
        this.camera.up.set(Vector3.Y);
        this.camera.lookAt(fixedLookAt);
        this.camera.update();
    }

    public void clearFixedView() {
        this.fixedView = false;
    }

    public boolean isFixedView() {
        return fixedView;
    }

    private float bobTime = 0f;
    private float bobAmplitude = 0.045f;
    private float bobFrequency = 1.85f;
    private float currentBob = 0f;
    private float bobDecay = 0f;

    private float baseFov = 65f;
    private float sprintFov = 79f;
    private float currentFov = 65f;
    private float targetFov = 65f;

    private float lookAheadStrength = 0.30f;
    private final Vector3 lookAheadOffset = new Vector3();
    private float currentHeadingX = 0f;
    private float currentHeadingZ = 0f;

    private float shakeMagnitude = 0f;
    private float shakeDecay = 8.0f;
    private float shakeTime = 0f;
    private final Vector3 shakeOffset = new Vector3();

    public CinematicCamera(int viewportWidth, int viewportHeight) {

        yaw = Float.parseFloat(System.getProperty("bd.spark36.cameraYaw", "0"));
        distance = Float.parseFloat(System.getProperty("bd.spark36.cameraDistance", String.valueOf(distance)));
        pitch = Float.parseFloat(System.getProperty("bd.spark36.cameraPitch", String.valueOf(pitch)));
        shoulderOffset = Float.parseFloat(System.getProperty("bd.spark36.cameraShoulderOffset", String.valueOf(shoulderOffset)));
        targetHeight = Float.parseFloat(System.getProperty("bd.spark36.cameraTargetHeight", String.valueOf(targetHeight)));
        camera = new PerspectiveCamera(baseFov, viewportWidth, viewportHeight);
        camera.near = 0.2f;
        camera.far = 280f;
        camera.position.set(0.28f, 1.85f, 52.7f);
        camera.lookAt(0f, 1.32f, 49.4f);
        camera.update();

        currentCameraPos.set(camera.position);
        currentLookAt.set(0f, 1.32f, 49.4f);
    }

    public void update(float delta, Vector3 playerPos, boolean inputEnabled) {
        update(delta, playerPos, inputEnabled, false, false, 0f);
    }

    public void update(float delta, Vector3 playerPos, boolean inputEnabled,
                       boolean isMoving, boolean isSprinting, float headingDeg) {
        if (fixedView) {
            camera.position.set(fixedPos);
            camera.up.set(Vector3.Y);
            camera.lookAt(fixedLookAt);
            camera.update();
            return;
        }

        boolean testMode = System.getProperty("bd.spark36.testScreenshot") != null ||
                           System.getProperty("bd.spark36.testHeroScreenshot") != null ||
                           System.getProperty("bd.spark36.testSitScreenshot") != null ||
                           System.getProperty("bd.spark36.testCongratulations") != null ||
                           System.getProperty("bd.spark36.testCombatScreenshot") != null ||
                           System.getProperty("bd.spark36.testPondScreenshot") != null ||
                           System.getProperty("bd.spark36.testPauseScreenshot") != null ||
                           System.getProperty("bd.spark36.testPauseMenu") != null ||
                           System.getProperty("bd.spark36.testExitConfirm") != null ||
                           System.getProperty("bd.spark36.testSummerCurzon") != null ||
                           System.getProperty("bd.spark36.testBambooGrove") != null ||
                           System.getProperty("bd.spark36.testVictory") != null ||
                           System.getProperty("bd.spark36.testModalScreenshot") != null ||
                           System.getProperty("bd.spark36.testMapScreenshot") != null ||
                           System.getProperty("bd.spark36.baseline") != null ||
                           System.getProperty("bd.spark36.noMouseLook") != null;
        if (inputEnabled && mouseLookActive && !testMode) {
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
                    float sens = getEffectiveSensitivity();
                    yaw -= deltaX * sens;
                    boolean invertPitch = "true".equalsIgnoreCase(System.getProperty("bd.spark36.invertPitch", "false"));
                    float pitchSign = invertPitch ? -1f : 1f;
                    pitch += deltaY * sens * pitchSign;
                    yaw = (yaw % 360f + 360f) % 360f;
                    pitch = MathUtils.clamp(pitch, minPitch, maxPitch);
                }
            }
        }

        if (isMoving && inputEnabled && !reduceMotion) {
            float bobRate = isSprinting ? bobFrequency * 1.55f : bobFrequency;
            bobTime += delta * bobRate * MathUtils.PI2;
            float targetBob = MathUtils.sin(bobTime) * bobAmplitude * (isSprinting ? 1.5f : 1f);
            currentBob = currentBob + (targetBob - currentBob) * Math.min(1f, 18f * delta);
        } else {

            currentBob *= Math.max(0f, 1f - 8f * delta);
        }

        targetFov = isSprinting && isMoving && !reduceMotion ? sprintFov : baseFov;
        currentFov += (targetFov - currentFov) * Math.min(1f, 5f * delta);
        camera.fieldOfView = currentFov;

        float headRad = headingDeg * MathUtils.degreesToRadians;
        float targetLAX = -MathUtils.sin(headRad) * lookAheadStrength * (isMoving ? 1f : 0.3f);
        float targetLAZ = -MathUtils.cos(headRad) * lookAheadStrength * (isMoving ? 1f : 0.3f);
        currentHeadingX += (targetLAX - currentHeadingX) * Math.min(1f, 4f * delta);
        currentHeadingZ += (targetLAZ - currentHeadingZ) * Math.min(1f, 4f * delta);
        lookAheadOffset.set(currentHeadingX, 0f, currentHeadingZ);

        if (reduceMotion) shakeMagnitude = 0f;
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

        float pitchRad = pitch * MathUtils.degreesToRadians;
        float sinPitch = MathUtils.sin(pitchRad);
        float pitchElevation = -sinPitch * 0.85f;
        targetFocus.set(
            playerPos.x + lookAheadOffset.x,
            playerPos.y + targetHeight + currentBob + pitchElevation,
            playerPos.z + lookAheadOffset.z
        );

        float yawRad   = yaw * MathUtils.degreesToRadians;
        float cosPitch = MathUtils.cos(pitchRad);
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

        float followSpeed = 12f;
        currentCameraPos.lerp(desiredCameraPos, Math.min(1f, followSpeed * delta));
        currentLookAt.lerp(targetFocus, Math.min(1f, (followSpeed + 4f) * delta));

        camera.position.set(currentCameraPos);
        camera.up.set(Vector3.Y);
        camera.lookAt(currentLookAt);
        camera.update();
    }

    public void setColliders(float[][] boxes) {
        this.colliders = boxes;
    }

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

            if (hit && tEnter > 0f && tEnter < nearest) nearest = tEnter;
        }

        if (nearest < 1f) {
            float t = Math.max(nearest, MIN_ARM_FRACTION);
            camPos.set(focus.x + dx * t, focus.y + dy * t, focus.z + dz * t);
        }
    }

    public void addShake(float magnitude) {
        if (reduceMotion) return;
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
    public void setAngles(float yaw, float pitch) {
        this.yaw = (yaw % 360f + 360f) % 360f;
        this.pitch = MathUtils.clamp(pitch, minPitch, maxPitch);
    }
    public void setDistance(float distance) {
        this.distance = MathUtils.clamp(distance, 1.2f, 18.0f);
    }
    public float getCurrentFov() { return currentFov; }
    public void setMouseLookActive(boolean active) { mouseLookActive = active; }
    public boolean isMouseLookActive() { return mouseLookActive; }
}
