package bd.spark36.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Handles student character locomotion, dual input schemes (WASD + Arrow Keys),
 * sprint stamina management, jump physics, collision detection, and procedural movement animation.
 */
public class PlayerController {

    // Position and Physics
    private final Vector3 position = new Vector3(0f, 0f, 49.4f); // Start on central avenue facing Curzon Hall (65.4m from Curzon Arcade)
    private final Vector3 velocity = new Vector3();
    private float verticalVelocity = 0f;
    private boolean isGrounded = true;

    // Orientation
    private float headingDegrees = 180f; // Facing Curzon Hall (north: -Z, south: +Z)
    private float targetHeadingDegrees = 180f;

    // Movement Speeds (m/s)
    public static final float WALK_SPEED = 4.8f;
    public static final float SPRINT_SPEED = 9.2f;
    public static final float ACCELERATION = 14f;
    public static final float JUMP_VELOCITY = 6.8f;
    public static final float GRAVITY = -20f;

    // Stamina & Vitality
    private float health = 100f;
    private final float maxHealth = 100f;
    private float stamina = 100f;
    private final float maxStamina = 100f;
    private final float staminaDrainRate = 26f; // per second sprinting
    private final float staminaRegenRate = 22f; // per second recovering
    private float staminaCooldown = 0f;
    private boolean staminaExhausted = false;

    // Animation & State
    private boolean isMoving = false;
    private boolean isSprinting = false;
    private float walkCycle = 0f;

    // Temp vectors for math
    private final Vector2 moveDir = new Vector2();
    private final Vector3 prevPosition = new Vector3();

    public PlayerController() {
    }

    /**
     * Updates physics, stamina, and input relative to camera yaw.
     *
     * @param delta time step in seconds
     * @param cameraYawDegrees horizontal yaw angle of the camera in degrees
     */
    public void update(float delta, float cameraYawDegrees, boolean inputEnabled) {
        if (delta > 0.1f) delta = 0.1f; // Cap spike deltas

        // 1. Read input
        moveDir.setZero();
        boolean sprintKey = false;
        boolean jumpKey = false;

        if (inputEnabled) {
            // Dual control: WASD + Arrow Keys
            boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
            boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
            boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            sprintKey = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

            if (up) moveDir.y += 1f;
            if (down) moveDir.y -= 1f;
            if (left) moveDir.x -= 1f;
            if (right) moveDir.x += 1f;

            if (moveDir.len2() > 0.001f) {
                moveDir.nor();
            }
        }

        isMoving = moveDir.len2() > 0.001f;

        // 2. Sprint and Stamina logic
        if (staminaExhausted) {
            if (stamina > 20f) {
                staminaExhausted = false;
            }
        }

        if (isMoving && sprintKey && !staminaExhausted && stamina > 0f) {
            isSprinting = true;
            stamina -= staminaDrainRate * delta;
            staminaCooldown = 0.6f;
            if (stamina <= 0f) {
                stamina = 0f;
                staminaExhausted = true;
                isSprinting = false;
            }
        } else {
            isSprinting = false;
            if (staminaCooldown > 0f) {
                staminaCooldown -= delta;
            } else if (stamina < maxStamina) {
                stamina += staminaRegenRate * delta;
                if (stamina > maxStamina) stamina = maxStamina;
            }
        }

        // 3. Convert input direction relative to camera angle
        float currentSpeed = isSprinting ? SPRINT_SPEED : WALK_SPEED;
        float targetVx = 0f;
        float targetVz = 0f;

        if (isMoving) {
            // Camera yaw angle in radians (0 is looking along -Z)
            float yawRad = cameraYawDegrees * MathUtils.degreesToRadians;

            // Forward vector in world XZ
            float forwardX = -MathUtils.sin(yawRad);
            float forwardZ = -MathUtils.cos(yawRad);

            // Right vector in world XZ
            float rightX = MathUtils.cos(yawRad);
            float rightZ = -MathUtils.sin(yawRad);

            // Combine based on input
            float worldMoveX = forwardX * moveDir.y + rightX * moveDir.x;
            float worldMoveZ = forwardZ * moveDir.y + rightZ * moveDir.x;

            targetVx = worldMoveX * currentSpeed;
            targetVz = worldMoveZ * currentSpeed;

            // Calculate target character heading angle in degrees (0 = +Z, 90 = +X, 180 = -Z, 270 = -X)
            targetHeadingDegrees = MathUtils.atan2(worldMoveX, worldMoveZ) * MathUtils.radiansToDegrees;
        }

        // Smooth horizontal acceleration
        velocity.x = MathUtils.lerp(velocity.x, targetVx, ACCELERATION * delta);
        velocity.z = MathUtils.lerp(velocity.z, targetVz, ACCELERATION * delta);

        // Smooth character heading turn
        if (isMoving) {
            float diff = (targetHeadingDegrees - headingDegrees) % 360f;
            if (diff > 180f) diff -= 360f;
            if (diff < -180f) diff += 360f;
            headingDegrees += diff * Math.min(1f, 15f * delta);
        }

        // 4. Jump and Vertical Gravity
        if (isGrounded && jumpKey && inputEnabled) {
            verticalVelocity = JUMP_VELOCITY;
            isGrounded = false;
        }

        if (!isGrounded) {
            verticalVelocity += GRAVITY * delta;
        }

        // 5. Apply displacement & collision check
        prevPosition.set(position);
        position.x += velocity.x * delta;
        position.z += velocity.z * delta;
        position.y += verticalVelocity * delta;

        // Ground collision
        if (position.y <= 0f) {
            position.y = 0f;
            verticalVelocity = 0f;
            isGrounded = true;
        }

        // Campus boundaries and building collisions
        resolveCollisions();

        // 6. Walk cycle animation progression
        if (isMoving) {
            float cycleSpeed = isSprinting ? 14f : 8.5f;
            walkCycle += cycleSpeed * delta;
        } else {
            walkCycle = 0f;
        }
    }

    /**
     * Clamps player to the Dhaka University Curzon Hall campus bounds and prevents
     * walking through solid building walls.
     */
    private void resolveCollisions() {
        // Outer campus boundary
        position.x = MathUtils.clamp(position.x, -95f, 95f);
        position.z = MathUtils.clamp(position.z, -55f, 85f);

        // Curzon Hall Central Building AABB collision box:
        // Main block: X from -35 to +35, Z from -42 to -22
        float curzonMinX = -36f;
        float curzonMaxX = 36f;
        float curzonMinZ = -43f;
        float curzonMaxZ = -20f;

        if (position.x > curzonMinX && position.x < curzonMaxX &&
            position.z > curzonMinZ && position.z < curzonMaxZ) {
            // Push back to previous valid position
            position.x = prevPosition.x;
            position.z = prevPosition.z;
        }
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public float getHeadingDegrees() {
        return headingDegrees;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getStamina() {
        return stamina;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public float getWalkCycle() {
        return walkCycle;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }
}
