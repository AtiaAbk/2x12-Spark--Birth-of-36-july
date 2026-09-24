package bd.spark36.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Professional-grade Player Controller for 2x12: Spark — Birth of 36 July.
 * Features:
 * - Dual scheme inputs: WASD + Arrow Keys (↑↓←→)
 * - Space bar = SPRINT (Run with stamina)
 * - Shift key = JUMP (Realistic physics arc, jump onto platforms/benches)
 * - Robust Axis-Separated AABB Collision (X, Y, Z resolved independently)
 * - Guaranteed Zero Penetration: CANNOT phase into buildings, trees, pillars, or monuments
 * - Smooth Wall Sliding: fluidly slides along walls/obstacles when running diagonally
 * - Platform Landing: jumping onto benches, steps, or monument bases lands on top
 * - Step-Up: low curbs and stairs (<= 0.65m) smoothly stepped onto without snagging
 * - Edge Detection: walking off platforms triggers realistic gravity fall
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
    public static final float JUMP_VELOCITY = 7.0f;
    public static final float GRAVITY = -22.0f;

    // Collision Dimensions
    public static final float PLAYER_RADIUS = 0.32f;
    public static final float PLAYER_HEIGHT = 1.70f;
    public static final float STEP_UP_HEIGHT = 0.65f; // Auto step-up for obstacles <= this height (stairs, curbs)

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
     * Complete AABB collision boxes covering ALL solid geometry in the campus.
     * Each box: {minX, minY, minZ, maxX, maxY, maxZ}.
     * Total: 50 boxes covering Curzon Hall, benches, bicycles, trees, lampposts, and memorials.
     */
    private static final float[][] COLLISION_BOXES = {

        // ======== CURZON HALL INDO-SARACENIC COMPLEX ========
        // Main Central Block: (0, 7, -32), size (38, 14, 17)
        {-19f, 0f, -40.5f, 19f, 14f, -23.5f},
        // East Wing: (32, 5.75, -31), size (28, 11.5, 15)
        {18f, 0f, -38.5f, 46f, 11.5f, -23.5f},
        // West Wing: (-32, 5.75, -31), size (28, 11.5, 15)
        {-46f, 0f, -38.5f, -18f, 11.5f, -23.5f},
        // Central Facade Portico Projection: (0, 7.8, -22.5), size (15, 15.6, 3.8)
        {-7.5f, 0f, -24.4f, 7.5f, 15.6f, -20.6f},
        // Grand Access Steps: (0, 0.3, -19.5), size (16, 0.6, 4.2)
        {-8f, 0f, -21.6f, 8f, 0.6f, -17.4f},

        // ======== BENCHES (3) — height 0.8m (JUMPABLE!) ========
        {-7.4f, 0f, 4.9f, -6.6f, 0.8f, 7.1f},
        {-7.4f, 0f, 22.9f, -6.6f, 0.8f, 25.1f},
        {-7.4f, 0f, 40.9f, -6.6f, 0.8f, 43.1f},

        // ======== BICYCLES (3) — height 1.0m (JUMPABLE!) ========
        {-8.8f, 0f, 6.6f, -7.6f, 1.0f, 7.8f},
        {-8.8f, 0f, 24.6f, -7.6f, 1.0f, 25.8f},
        {-8.8f, 0f, 42.6f, -7.6f, 1.0f, 43.8f},

        // ======== 19 RAIN TREE TRUNKS — solid trunks height 6.0m ========
        {-11.5f, 0f, -4.5f, -10.5f, 6f, -3.5f},
        {10.5f, 0f, -4.5f, 11.5f, 6f, -3.5f},
        {-12.5f, 0f, 13.5f, -11.5f, 6f, 14.5f},
        {11.5f, 0f, 13.5f, 12.5f, 6f, 14.5f},
        {-13.5f, 0f, 31.5f, -12.5f, 6f, 32.5f},
        {12.5f, 0f, 31.5f, 13.5f, 6f, 32.5f},
        {-14.5f, 0f, 49.5f, -13.5f, 6f, 50.5f},
        {13.5f, 0f, 49.5f, 14.5f, 6f, 50.5f},
        {-28.5f, 0f, 15.5f, -27.5f, 6f, 16.5f},
        {27.5f, 0f, 15.5f, 28.5f, 6f, 16.5f},
        {-48.5f, 0f, 5.5f, -47.5f, 6f, 6.5f},
        {47.5f, 0f, 5.5f, 48.5f, 6f, 6.5f},
        {-62.5f, 0f, 29.5f, -61.5f, 6f, 30.5f},
        {61.5f, 0f, 29.5f, 62.5f, 6f, 30.5f},
        {-32.5f, 0f, -12.5f, -31.5f, 6f, -11.5f},
        {31.5f, 0f, -12.5f, 32.5f, 6f, -11.5f},
        {-22.5f, 0f, 67.5f, -21.5f, 6f, 68.5f},
        {21.5f, 0f, 67.5f, 22.5f, 6f, 68.5f},
        {-0.5f, 0f, 77.5f, 0.5f, 6f, 78.5f},

        // ======== 10 CAST-IRON LAMPPOSTS — solid posts height 4.0m ========
        {-5.55f, 0f, -10.15f, -5.25f, 4.0f, -9.85f},
        {5.25f, 0f, -10.15f, 5.55f, 4.0f, -9.85f},
        {-5.55f, 0f, 7.85f, -5.25f, 4.0f, 8.15f},
        {5.25f, 0f, 7.85f, 5.55f, 4.0f, 8.15f},
        {-5.55f, 0f, 25.85f, -5.25f, 4.0f, 26.15f},
        {5.25f, 0f, 25.85f, 5.55f, 4.0f, 26.15f},
        {-5.55f, 0f, 43.85f, -5.25f, 4.0f, 44.15f},
        {5.25f, 0f, 43.85f, 5.55f, 4.0f, 44.15f},
        {-5.55f, 0f, 59.85f, -5.25f, 4.0f, 60.15f},
        {5.25f, 0f, 59.85f, 5.55f, 4.0f, 60.15f},

        // ======== 5 JULY HISTORICAL MEMORIALS (Bases + Solid Pillars) ========
        // Memorial 1 at (0, 0, -16) — Curzon Central Arcade
        {-1.1f, 0f, -17.1f, 1.1f, 0.4f, -14.9f},    // Base (jumpable! height 0.4m)
        {-0.5f, 0.4f, -16.5f, 0.5f, 3.0f, -15.5f},  // Central Pillar (solid! height 3.0m)

        // Memorial 2 at (-45, 0, 18) — Language Monument Plaza
        {-46.1f, 0f, 16.9f, -43.9f, 0.4f, 19.1f},
        {-45.5f, 0.4f, 17.5f, -44.5f, 3.0f, 18.5f},

        // Memorial 3 at (-6.5, 0, 48) — Raju Sculpture Intersection
        {-7.6f, 0f, 46.9f, -5.4f, 0.4f, 49.1f},
        {-7.0f, 0.4f, 47.5f, -6.0f, 3.0f, 48.5f},

        // Memorial 4 at (48, 0, 18) — Women's Hall Quadrangle
        {46.9f, 0f, 16.9f, 49.1f, 0.4f, 19.1f},
        {47.5f, 0.4f, 17.5f, 48.5f, 3.0f, 18.5f},

        // Memorial 5 at (28, 0, -10) — 36 July Gateway
        {26.9f, 0f, -11.1f, 29.1f, 0.4f, -8.9f},
        {27.5f, 0.4f, -10.5f, 28.5f, 3.0f, -9.5f},
    };

    public PlayerController() {
    }

    /**
     * Updates physics, stamina, input, jump arc, and collision resolution.
     *
     * @param delta time step in seconds
     * @param cameraYawDegrees horizontal yaw angle of the camera in degrees
     * @param inputEnabled whether player controls are active
     */
    public void update(float delta, float cameraYawDegrees, boolean inputEnabled) {
        if (delta > 0.1f) delta = 0.1f; // Cap delta spikes

        // 1. Read input: Dual scheme (WASD + Arrow Keys)
        moveDir.setZero();
        boolean sprintKey = false;
        boolean jumpKey = false;

        if (inputEnabled) {
            boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
            boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
            boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            // SPRINT / RUN: SHIFT or CONTROL, or holding SPACE while moving
            boolean spaceHeld = Gdx.input.isKeyPressed(Input.Keys.SPACE);
            boolean spaceJustPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

            sprintKey = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                        Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ||
                        Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                        (spaceHeld && !spaceJustPressed && isMoving);

            // JUMP / LONG JUMP: Pressing SPACE, C, V, or mouse right-click
            jumpKey = spaceJustPressed ||
                      Gdx.input.isKeyJustPressed(Input.Keys.C) ||
                      Gdx.input.isKeyJustPressed(Input.Keys.V) ||
                      Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);

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

        // 3. Movement direction relative to camera angle
        float currentSpeed = isSprinting ? SPRINT_SPEED : WALK_SPEED;
        float targetVx = 0f;
        float targetVz = 0f;

        if (isMoving) {
            float yawRad = cameraYawDegrees * MathUtils.degreesToRadians;
            float forwardX = -MathUtils.sin(yawRad);
            float forwardZ = -MathUtils.cos(yawRad);
            float rightX = MathUtils.cos(yawRad);
            float rightZ = -MathUtils.sin(yawRad);

            float worldMoveX = forwardX * moveDir.y + rightX * moveDir.x;
            float worldMoveZ = forwardZ * moveDir.y + rightZ * moveDir.x;

            targetVx = worldMoveX * currentSpeed;
            targetVz = worldMoveZ * currentSpeed;

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

        // 4. Jump & Athletic LONG JUMP Physics
        if (isGrounded && jumpKey && inputEnabled) {
            if ((isSprinting || sprintKey) && isMoving) {
                // Running / Sprinting LONG JUMP (high athletic forward leap!)
                verticalVelocity = 9.4f; // High upward leap
                velocity.x *= 1.45f;     // Forward trajectory boost to sail onto high obstacles
                velocity.z *= 1.45f;
            } else if (isMoving) {
                // Moving forward jump
                verticalVelocity = 8.0f;
                velocity.x *= 1.20f;
                velocity.z *= 1.20f;
            } else {
                // Standing vertical hop
                verticalVelocity = 7.0f;
            }
            isGrounded = false;
        }

        // 5. Physics and Collision Resolution
        prevPosition.set(position);
        resolveMovementAndCollisions(delta);

        // 6. Walk cycle animation progression
        if (isMoving) {
            float cycleSpeed = isSprinting ? 14f : 8.5f;
            walkCycle += cycleSpeed * delta;
        } else {
            walkCycle = 0f;
        }
    }

    /**
     * Professional Axis-Separated Physics & Collision Engine.
     * Resolves:
     * 1. Vertical Gravity & Platform/Ground Landing
     * 2. Independent X Axis Collision & Wall Sliding
     * 3. Independent Z Axis Collision & Wall Sliding
     * 4. Step-Up for stairs/curbs (<= STEP_UP_HEIGHT)
     * 5. Edge Detection for platform fall-off
     * 6. Campus Boundary Clamping
     */
    private void resolveMovementAndCollisions(float delta) {
        // --- A. Vertical Gravity & Landing ---
        if (!isGrounded) {
            verticalVelocity += GRAVITY * delta;
        }

        float proposedY = position.y + verticalVelocity * delta;

        // Find the highest solid ground surface directly beneath player's footprint
        float highestGround = 0f; // Default ground plane Y=0

        for (float[] box : COLLISION_BOXES) {
            float bMinX = box[0], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            // Does player horizontal footprint overlap this box?
            boolean overlapX = (position.x + PLAYER_RADIUS > bMinX) && (position.x - PLAYER_RADIUS < bMaxX);
            boolean overlapZ = (position.z + PLAYER_RADIUS > bMinZ) && (position.z - PLAYER_RADIUS < bMaxZ);

            if (overlapX && overlapZ) {
                // If player was at or above this surface (or within step margin), it's a valid landing surface
                if (prevPosition.y >= bMaxY - 0.20f) {
                    if (bMaxY > highestGround) {
                        highestGround = bMaxY;
                    }
                }
            }
        }

        // Apply landing or falling
        if (proposedY <= highestGround) {
            position.y = highestGround;
            verticalVelocity = 0f;
            isGrounded = true;
        } else {
            position.y = proposedY;
            isGrounded = false;
        }

        // --- B. Horizontal Movement: X Axis Resolution ---
        float moveX = velocity.x * delta;
        float proposedX = position.x + moveX;

        for (float[] box : COLLISION_BOXES) {
            float bMinX = box[0], bMinY = box[1], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            // If player's feet are completely on top of this box, ignore horizontal wall collision
            if (position.y >= bMaxY - 0.05f) continue;
            // If player is completely below this box, ignore
            if (position.y + PLAYER_HEIGHT <= bMinY) continue;

            // Check if current Z overlaps
            boolean overlapZ = (position.z + PLAYER_RADIUS > bMinZ) && (position.z - PLAYER_RADIUS < bMaxZ);
            if (!overlapZ) continue;

            // Check if proposed X enters the box
            boolean overlapX = (proposedX + PLAYER_RADIUS > bMinX) && (proposedX - PLAYER_RADIUS < bMaxX);
            if (overlapX) {
                // Low obstacle auto-step check: if box top is within step-up reach from current feet
                if (bMaxY <= position.y + STEP_UP_HEIGHT && (bMaxY - bMinY) <= STEP_UP_HEIGHT) {
                    // Allowed to enter; vertical step-up will elevate player
                } else {
                    // Firm wall collision! Push outside box along X
                    if (position.x <= (bMinX + bMaxX) / 2f) {
                        proposedX = bMinX - PLAYER_RADIUS;
                    } else {
                        proposedX = bMaxX + PLAYER_RADIUS;
                    }
                    velocity.x = 0f;
                }
            }
        }
        position.x = proposedX;

        // --- C. Horizontal Movement: Z Axis Resolution ---
        float moveZ = velocity.z * delta;
        float proposedZ = position.z + moveZ;

        for (float[] box : COLLISION_BOXES) {
            float bMinX = box[0], bMinY = box[1], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            // If player's feet are completely on top of this box, ignore horizontal wall collision
            if (position.y >= bMaxY - 0.05f) continue;
            // If player is completely below this box, ignore
            if (position.y + PLAYER_HEIGHT <= bMinY) continue;

            // Check if current X overlaps
            boolean overlapX = (position.x + PLAYER_RADIUS > bMinX) && (position.x - PLAYER_RADIUS < bMaxX);
            if (!overlapX) continue;

            // Check if proposed Z enters the box
            boolean overlapZ = (proposedZ + PLAYER_RADIUS > bMinZ) && (proposedZ - PLAYER_RADIUS < bMaxZ);
            if (overlapZ) {
                // Low obstacle auto-step check
                if (bMaxY <= position.y + STEP_UP_HEIGHT && (bMaxY - bMinY) <= STEP_UP_HEIGHT) {
                    // Allowed to enter; vertical step-up will elevate player
                } else {
                    // Firm wall collision! Push outside box along Z
                    if (position.z <= (bMinZ + bMaxZ) / 2f) {
                        proposedZ = bMinZ - PLAYER_RADIUS;
                    } else {
                        proposedZ = bMaxZ + PLAYER_RADIUS;
                    }
                    velocity.z = 0f;
                }
            }
        }
        position.z = proposedZ;

        // --- D. Post-Move Step-Up & Edge Detection ---
        float surfaceUnderFoot = 0f;
        for (float[] box : COLLISION_BOXES) {
            float bMinX = box[0], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            boolean overlapX = (position.x + PLAYER_RADIUS > bMinX) && (position.x - PLAYER_RADIUS < bMaxX);
            boolean overlapZ = (position.z + PLAYER_RADIUS > bMinZ) && (position.z - PLAYER_RADIUS < bMaxZ);

            if (overlapX && overlapZ) {
                // Can step up onto this box if it's within reach
                if (bMaxY >= position.y && bMaxY <= position.y + STEP_UP_HEIGHT) {
                    if (bMaxY > surfaceUnderFoot) {
                        surfaceUnderFoot = bMaxY;
                    }
                } else if (position.y >= bMaxY - 0.20f) {
                    if (bMaxY > surfaceUnderFoot) {
                        surfaceUnderFoot = bMaxY;
                    }
                }
            }
        }

        // Apply step up
        if (surfaceUnderFoot > position.y) {
            position.y = surfaceUnderFoot;
            verticalVelocity = 0f;
            isGrounded = true;
        }

        // Edge detection: if standing on an elevated surface and walked off
        if (isGrounded && position.y > 0.05f) {
            if (surfaceUnderFoot < position.y - 0.10f) {
                isGrounded = false; // Walked off the edge! Start falling
            }
        }

        // --- E. Campus Boundary Clamping ---
        position.x = MathUtils.clamp(position.x, -95f, 95f);
        position.z = MathUtils.clamp(position.z, -55f, 85f);
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
