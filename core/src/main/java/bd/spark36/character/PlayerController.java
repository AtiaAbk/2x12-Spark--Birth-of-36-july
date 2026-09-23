package bd.spark36.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Handles student character locomotion, dual input schemes (WASD + Arrow Keys),
 * sprint stamina management, jump physics, AABB collision detection with world
 * geometry, and procedural movement animation.
 */
public class PlayerController {

    // Position and Physics
    private final Vector3 position = new Vector3(0f, 0f, 49.4f); // Start on central avenue facing Curzon Hall
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

    // Collision Constants
    private static final float PLAYER_RADIUS = 0.3f;
    private static final float PLAYER_HEIGHT = 1.7f;
    private static final float STEP_UP_HEIGHT = 0.65f; // Auto-step up for obstacles <= this height

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

    /**
     * AABB collision boxes for ALL solid world geometry.
     * Each entry: {minX, minY, minZ, maxX, maxY, maxZ}
     * Derived from DhakaCampusWorld.java geometry positions and sizes.
     * Total: 40 collision boxes covering every solid object in the campus.
     */
    private static final float[][] COLLISION_BOXES = {

        // ======== CURZON HALL BUILDING COMPLEX ========
        // Main Block: createBox(38,14,17) at (0,7,-32)
        {-19f, 0f, -40.5f, 19f, 14f, -23.5f},
        // East Wing: createBox(28,11.5,15) at (32,5.75,-31)
        {18f, 0f, -38.5f, 46f, 11.5f, -23.5f},
        // West Wing: createBox(28,11.5,15) at (-32,5.75,-31)
        {-46f, 0f, -38.5f, -18f, 11.5f, -23.5f},
        // Portico (front projection): createBox(15,15.6,3.8) at (0,7.8,-22.5)
        {-7.5f, 0f, -24.4f, 7.5f, 15.6f, -20.6f},
        // Grand Steps: createBox(16,0.6,4.2) at (0,0.3,-19.5)
        {-8f, 0f, -21.6f, 8f, 0.6f, -17.4f},

        // ======== BENCHES (3) — rotated 90° ========
        // Bench at Z=6: createBox(2.2,0.8,0.7) at (-7,0.4,6)
        {-8.1f, 0f, 4.9f, -5.9f, 0.8f, 7.1f},
        // Bench at Z=24
        {-8.1f, 0f, 22.9f, -5.9f, 0.8f, 25.1f},
        // Bench at Z=42
        {-8.1f, 0f, 40.9f, -5.9f, 0.8f, 43.1f},

        // ======== BICYCLES (3) ========
        {-9.0f, 0f, 6.7f, -7.4f, 1.0f, 7.7f},
        {-9.0f, 0f, 24.7f, -7.4f, 1.0f, 25.7f},
        {-9.0f, 0f, 42.7f, -7.4f, 1.0f, 43.7f},

        // ======== TREE TRUNKS (19) — cylinder 0.95m diameter → 0.5m radius collision ========
        // Trees are solid trunks from ground to canopy (Y=0 to Y=6)
        {-11.5f, 0f, -4.5f, -10.5f, 6f, -3.5f},   // tree at (-11, -4)
        {10.5f, 0f, -4.5f, 11.5f, 6f, -3.5f},      // tree at (11, -4)
        {-12.5f, 0f, 13.5f, -11.5f, 6f, 14.5f},    // tree at (-12, 14)
        {11.5f, 0f, 13.5f, 12.5f, 6f, 14.5f},      // tree at (12, 14)
        {-13.5f, 0f, 31.5f, -12.5f, 6f, 32.5f},    // tree at (-13, 32)
        {12.5f, 0f, 31.5f, 13.5f, 6f, 32.5f},      // tree at (13, 32)
        {-14.5f, 0f, 49.5f, -13.5f, 6f, 50.5f},    // tree at (-14, 50)
        {13.5f, 0f, 49.5f, 14.5f, 6f, 50.5f},      // tree at (14, 50)
        {-28.5f, 0f, 15.5f, -27.5f, 6f, 16.5f},    // tree at (-28, 16)
        {27.5f, 0f, 15.5f, 28.5f, 6f, 16.5f},      // tree at (28, 16)
        {-48.5f, 0f, 5.5f, -47.5f, 6f, 6.5f},      // tree at (-48, 6)
        {47.5f, 0f, 5.5f, 48.5f, 6f, 6.5f},        // tree at (48, 6)
        {-62.5f, 0f, 29.5f, -61.5f, 6f, 30.5f},    // tree at (-62, 30)
        {61.5f, 0f, 29.5f, 62.5f, 6f, 30.5f},      // tree at (62, 30)
        {-32.5f, 0f, -12.5f, -31.5f, 6f, -11.5f},  // tree at (-32, -12)
        {31.5f, 0f, -12.5f, 32.5f, 6f, -11.5f},    // tree at (32, -12)
        {-22.5f, 0f, 67.5f, -21.5f, 6f, 68.5f},    // tree at (-22, 68)
        {21.5f, 0f, 67.5f, 22.5f, 6f, 68.5f},      // tree at (22, 68)
        {-0.5f, 0f, 77.5f, 0.5f, 6f, 78.5f},       // tree at (0, 78)

        // ======== LAMPPOSTS (10) — cylinder 0.18m → 0.15m radius collision ========
        // Solid cast-iron posts from ground to lantern (Y=0 to Y=4)
        {-5.55f, 0f, -10.15f, -5.25f, 4.2f, -9.85f},   // lamppost at (-5.4, -10)
        {5.25f, 0f, -10.15f, 5.55f, 4.2f, -9.85f},     // lamppost at (5.4, -10)
        {-5.55f, 0f, 7.85f, -5.25f, 4.2f, 8.15f},      // lamppost at (-5.4, 8)
        {5.25f, 0f, 7.85f, 5.55f, 4.2f, 8.15f},        // lamppost at (5.4, 8)
        {-5.55f, 0f, 25.85f, -5.25f, 4.2f, 26.15f},    // lamppost at (-5.4, 26)
        {5.25f, 0f, 25.85f, 5.55f, 4.2f, 26.15f},      // lamppost at (5.4, 26)
        {-5.55f, 0f, 43.85f, -5.25f, 4.2f, 44.15f},    // lamppost at (-5.4, 44)
        {5.25f, 0f, 43.85f, 5.55f, 4.2f, 44.15f},      // lamppost at (5.4, 44)
        {-5.55f, 0f, 59.85f, -5.25f, 4.2f, 60.15f},    // lamppost at (-5.4, 60)
        {5.25f, 0f, 59.85f, 5.55f, 4.2f, 60.15f},      // lamppost at (5.4, 60)
    };

    public PlayerController() {
    }

    /**
     * Updates physics, stamina, and input relative to camera yaw.
     *
     * @param delta time step in seconds
     * @param cameraYawDegrees horizontal yaw angle of the camera in degrees
     * @param inputEnabled whether player input is active (false when modal/menu is open)
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

            // Calculate target character heading angle
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

        // 5. Apply displacement
        prevPosition.set(position);
        position.x += velocity.x * delta;
        position.z += velocity.z * delta;
        position.y += verticalVelocity * delta;

        // 6. Full AABB collision resolution (ground plane + world objects)
        resolveCollisions();

        // 7. Walk cycle animation progression
        if (isMoving) {
            float cycleSpeed = isSprinting ? 14f : 8.5f;
            walkCycle += cycleSpeed * delta;
        } else {
            walkCycle = 0f;
        }
    }

    /**
     * Full AABB collision resolution against all world geometry.
     * Handles:
     * - Outer campus boundary clamping
     * - Horizontal wall collision (push back to previous position)
     * - Vertical platform landing (land on top of objects when falling)
     * - Step-up for low obstacles (auto-step onto stairs, curbs)
     * - Edge detection (start falling when walking off a platform)
     * - Ground plane collision at Y=0
     */
    private void resolveCollisions() {
        // Outer campus boundary
        position.x = MathUtils.clamp(position.x, -95f, 95f);
        position.z = MathUtils.clamp(position.z, -55f, 85f);

        float landingSurface = 0f; // Default ground plane

        for (float[] box : COLLISION_BOXES) {
            float bMinX = box[0], bMinY = box[1], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            // Check horizontal overlap (player has collision radius)
            boolean overlapX = position.x + PLAYER_RADIUS > bMinX && position.x - PLAYER_RADIUS < bMaxX;
            boolean overlapZ = position.z + PLAYER_RADIUS > bMinZ && position.z - PLAYER_RADIUS < bMaxZ;

            if (!overlapX || !overlapZ) continue;

            // Player horizontally overlaps this box
            float boxHeight = bMaxY - bMinY;

            // Was player above this box on the previous frame?
            boolean wasAbove = prevPosition.y >= bMaxY - 0.1f;

            if (wasAbove) {
                // Player was above — this box is a potential landing platform
                landingSurface = Math.max(landingSurface, bMaxY);

            } else if (boxHeight <= STEP_UP_HEIGHT && bMaxY - position.y <= STEP_UP_HEIGHT) {
                // Low obstacle (steps, curbs) — auto-step up
                landingSurface = Math.max(landingSurface, bMaxY);

            } else if (position.y < bMaxY && position.y + PLAYER_HEIGHT > bMinY) {
                // Tall obstacle — wall collision: push back horizontally
                position.x = prevPosition.x;
                position.z = prevPosition.z;
                velocity.x = 0f;
                velocity.z = 0f;
            }
        }

        // Apply landing on the highest available surface
        if (position.y <= landingSurface) {
            position.y = landingSurface;
            verticalVelocity = 0f;
            isGrounded = true;
        }

        // Edge detection: if grounded above ground level, check if still over a surface
        if (isGrounded && position.y > 0.01f) {
            boolean onSurface = false;
            for (float[] box : COLLISION_BOXES) {
                boolean overX = position.x + PLAYER_RADIUS > box[0] && position.x - PLAYER_RADIUS < box[3];
                boolean overZ = position.z + PLAYER_RADIUS > box[2] && position.z - PLAYER_RADIUS < box[5];
                if (overX && overZ && Math.abs(position.y - box[4]) < 0.15f) {
                    onSurface = true;
                    break;
                }
            }
            if (!onSurface) {
                isGrounded = false; // Walk off edge — start falling
            }
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
