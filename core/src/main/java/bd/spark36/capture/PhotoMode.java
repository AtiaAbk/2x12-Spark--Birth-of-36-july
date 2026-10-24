package bd.spark36.capture;

import bd.spark36.camera.CinematicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * F10 photo mode: a free orbit camera around the player while the simulation is frozen.
 *
 * <p>It drives the {@link PerspectiveCamera} owned by {@link CinematicCamera} directly and never
 * calls {@code CinematicCamera.update}, so the follow camera's own state (yaw, pitch, distance,
 * smoothing, fixed views) is untouched; on exit the camera's position, direction, up vector and
 * field of view are restored, and the next normal frame continues exactly where it left off.
 *
 * <p>Controls: mouse or WASD/arrows orbit, wheel or +/- (PgUp/PgDn) zoom, Q/E roll, R level the
 * horizon, H hide the hint line.
 */
public final class PhotoMode {

    static final float MIN_DISTANCE = 0.8f;
    static final float MAX_DISTANCE = 14f;
    static final float MIN_PITCH = -80f;
    static final float MAX_PITCH = 85f;
    static final float MAX_ROLL = 45f;
    private static final float FOCUS_HEIGHT = 1.15f;
    private static final float MOUSE_SENSITIVITY = 0.22f;

    private boolean active;
    private float yaw, pitch, distance, roll;
    private boolean hintVisible = true;
    private final Vector3 focus = new Vector3();

    // Saved camera state for an exact restore
    private final Vector3 savedPosition = new Vector3();
    private final Vector3 savedDirection = new Vector3();
    private final Vector3 savedUp = new Vector3();
    private float savedFov;
    private boolean savedCursorCatched;
    private PerspectiveCamera cam;

    public boolean isActive() {
        return active;
    }

    public boolean isHintVisible() {
        return hintVisible;
    }

    public void enter(CinematicCamera cinematic, Vector3 playerPos) {
        if (active) return;
        cam = cinematic.getCamera();
        savedPosition.set(cam.position);
        savedDirection.set(cam.direction);
        savedUp.set(cam.up);
        savedFov = cam.fieldOfView;
        savedCursorCatched = Gdx.input.isCursorCatched();

        // Start from where the camera is now, expressed as an orbit around the player.
        focus.set(playerPos.x, playerPos.y + FOCUS_HEIGHT, playerPos.z);
        float dx = cam.position.x - focus.x, dy = cam.position.y - focus.y, dz = cam.position.z - focus.z;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-3f) { dz = 1f; len = 1f; }
        distance = MathUtils.clamp(len, MIN_DISTANCE, MAX_DISTANCE);
        pitch = MathUtils.clamp((float) Math.toDegrees(Math.asin(MathUtils.clamp(dy / len, -1f, 1f))), MIN_PITCH, MAX_PITCH);
        yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        roll = 0f;

        Gdx.input.setCursorCatched(true);
        Gdx.input.getDeltaX(); // drop the jump accumulated before entering
        Gdx.input.getDeltaY();
        active = true;
        apply();
    }

    public void exit() {
        if (!active) return;
        active = false;
        cam.position.set(savedPosition);
        cam.direction.set(savedDirection);
        cam.up.set(savedUp);
        cam.fieldOfView = savedFov;
        cam.update();
        Gdx.input.setCursorCatched(savedCursorCatched);
    }

    /** Orbit/zoom/roll input; call once per frame while active, before the scene renders. */
    void update(float delta, float scrollY, boolean mouseLook) {
        if (!active) return;
        if (mouseLook) {
            float mdx = MathUtils.clamp(Gdx.input.getDeltaX(), -100f, 100f);
            float mdy = MathUtils.clamp(Gdx.input.getDeltaY(), -100f, 100f);
            boolean invert = "true".equalsIgnoreCase(System.getProperty("bd.spark36.invertPitch", "false"));
            yaw -= mdx * MOUSE_SENSITIVITY;
            pitch += mdy * MOUSE_SENSITIVITY * (invert ? -1f : 1f);
        }
        float orbitSpeed = 90f * delta, pitchSpeed = 60f * delta;
        if (key(Input.Keys.A) || key(Input.Keys.LEFT)) yaw -= orbitSpeed;
        if (key(Input.Keys.D) || key(Input.Keys.RIGHT)) yaw += orbitSpeed;
        if (key(Input.Keys.W) || key(Input.Keys.UP)) pitch += pitchSpeed;
        if (key(Input.Keys.S) || key(Input.Keys.DOWN)) pitch -= pitchSpeed;

        float zoom = scrollY * 0.12f;
        if (key(Input.Keys.EQUALS) || key(Input.Keys.PLUS) || key(Input.Keys.NUMPAD_ADD) || key(Input.Keys.PAGE_UP)) zoom -= 1.2f * delta;
        if (key(Input.Keys.MINUS) || key(Input.Keys.NUMPAD_SUBTRACT) || key(Input.Keys.PAGE_DOWN)) zoom += 1.2f * delta;
        distance *= (1f + zoom);

        if (key(Input.Keys.Q)) roll -= 45f * delta;
        if (key(Input.Keys.E)) roll += 45f * delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) roll = 0f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) hintVisible = !hintVisible;

        apply();
    }

    /** Sets the orbit directly (used by the automated capture test). */
    void setOrbit(float yaw, float pitch, float distance, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.distance = distance;
        this.roll = roll;
        if (active) apply();
    }

    private void apply() {
        yaw = (yaw % 360f + 360f) % 360f;
        pitch = MathUtils.clamp(pitch, MIN_PITCH, MAX_PITCH);
        distance = MathUtils.clamp(distance, MIN_DISTANCE, MAX_DISTANCE);
        roll = MathUtils.clamp(roll, -MAX_ROLL, MAX_ROLL);
        orbitPosition(focus, yaw, pitch, distance, cam.position);
        if (cam.position.y < 0.15f) cam.position.y = 0.15f;
        cam.up.set(Vector3.Y);
        cam.lookAt(focus);
        if (roll != 0f) cam.up.rotate(cam.direction, roll);
        cam.update();
    }

    /** Point on the orbit sphere: same convention as CinematicCamera (yaw 0 = behind on +Z). */
    static Vector3 orbitPosition(Vector3 focus, float yawDeg, float pitchDeg, float dist, Vector3 out) {
        float yr = yawDeg * MathUtils.degreesToRadians, pr = pitchDeg * MathUtils.degreesToRadians;
        float cp = MathUtils.cos(pr);
        return out.set(focus.x + MathUtils.sin(yr) * cp * dist,
                       focus.y + MathUtils.sin(pr) * dist,
                       focus.z + MathUtils.cos(yr) * cp * dist);
    }

    private static boolean key(int k) {
        return Gdx.input.isKeyPressed(k);
    }
}
