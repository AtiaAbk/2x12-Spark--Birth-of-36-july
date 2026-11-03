// ei file ta keyboard ar mouse diye main player ke control kore
package bd.spark36.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import bd.spark36.input.GameInput;
import bd.spark36.input.GameInput.GameAction;
import bd.spark36.world.TerrainSurface;

public class PlayerController {

    private final Vector3 position = new Vector3(0f, 0f, 49.4f);
    private final Vector3 velocity = new Vector3();
    private float verticalVelocity = 0f;
    private boolean isGrounded = true;

    private float headingDegrees = 180f;
    private float targetHeadingDegrees = 180f;

    public static final float WALK_SPEED = 4.8f;
    public static final float SPRINT_SPEED = 9.2f;
    public static final float ACCELERATION = 14f;
    public static final float JUMP_VELOCITY = 13.5f;
    public static final float DOUBLE_JUMP_VELOCITY = 14.5f;
    public static final float GRAVITY = -20.0f;

    private int jumpCount = 0;
    private static final int MAX_JUMPS = 2;

    public static final float PLAYER_RADIUS = 0.38f;
    public static final float PLAYER_HEIGHT = 1.70f;
    public static final float STEP_UP_HEIGHT = 0.65f;

    private static final float POND_MIN_X = -8.7f;
    private static final float POND_MAX_X = 8.7f;
    private static final float POND_MIN_Z = -5.7f;
    private static final float POND_MAX_Z = 17.7f;
    public static final float POND_FLOOR_Y = -0.45f;
    private static final float WADE_SPEED_FACTOR = 0.55f;
    private static final float WADE_STEP_UP_HEIGHT = 1.0f;

    private static final float CLIMB_SPEED = 4.2f;
    private static final float MIN_CLIMB_WALL_WIDTH = 1.5f;
    private boolean isClimbing = false;
    private boolean wallBlocked = false;
    private float wallTopY = 0f;
    private float wallPushX = 0f;
    private float wallPushZ = 0f;

    private float health = 100f;
    private final float maxHealth = 100f;
    private float stamina = 100f;
    private final float maxStamina = 100f;
    private final float staminaDrainRate = 26f;
    private final float staminaRegenRate = 22f;
    private float staminaCooldown = 0f;
    private boolean staminaExhausted = false;

    private int gender = 0;

    public enum PlayerCombatState {
        IDLE,
        ATTACKING,
        ATTACK_RECOVERY
    }
    private PlayerCombatState combatState = PlayerCombatState.IDLE;
    private float attackRecoveryTimer = 0f;

    private final AnimationStateMachine animStateMachine = new AnimationStateMachine();
    private boolean isMoving = false;
    private boolean isSprinting = false;
    private float walkCycle = 0f;
    private boolean isCrouching = false;
    private boolean isBlocking = false;
    private float blockTimer = 0f;
    private boolean isAttacking = false;
    private int attackCombo = 0;
    private float attackProgress = 0f;
    private float comboWindowTimer = 0f;
    private boolean isSittingWater = false;
    private float sitProgress = 0f;

    private boolean isDodging = false;
    private float dodgeProgress = 0f;
    private float dodgeTimer = 0f;
    private float dodgeCooldown = 0f;
    private final Vector2 dodgeDir = new Vector2();
    private static final float DODGE_DURATION = 0.22f;
    private static final float DODGE_SPEED = 7.4f;

    private final Vector3 combatTarget = new Vector3();
    private boolean hasCombatTarget = false;

    private int bufferedAttack = 0;

    private float staggerTimer = 0f;
    private final Vector3 knockbackVel = new Vector3();

    private final Vector2 moveDir = new Vector2();
    private final Vector3 prevPosition = new Vector3();

    private static final float[][] COLLISION_BOXES = {

        {-19f, 0f, -40.5f, 19f, 14f, -23.5f},

        {18f, 0f, -38.5f, 46f, 11.5f, -23.5f},

        {-46f, 0f, -38.5f, -18f, 11.5f, -23.5f},

        {-7.5f, 0f, -24.4f, 7.5f, 15.6f, -20.6f},

        {-8f, 0f, -21.6f, 8f, 0.6f, -17.4f},

        {-46f, 0f, -23.5f, -8f, 0.6f, -21.7f},
        {8f, 0f, -23.5f, 46f, 0.6f, -21.7f},

        {-17.95f, 0f, 4.9f, -17.25f, 0.8f, 7.1f},
        {-7.4f, 0f, 22.9f, -6.6f, 0.8f, 25.1f},
        {-7.4f, 0f, 40.9f, -6.6f, 0.8f, 43.1f},

        {-19.4f, 0f, 6.6f, -18.2f, 1.0f, 7.8f},
        {-8.8f, 0f, 24.6f, -7.6f, 1.0f, 25.8f},
        {-8.8f, 0f, 42.6f, -7.6f, 1.0f, 43.8f},

        {-20f, 0f, -2.5f, -19f, 6f, -1.5f},
        {19f, 0f, -2.5f, 20f, 6f, -1.5f},
        {-20f, 0f, 13.5f, -19f, 6f, 14.5f},
        {19f, 0f, 13.5f, 20f, 6f, 14.5f},
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
        {-12f, 0f, 70.5f, -11f, 6f, 71.5f},

        {-5.55f, 0f, -10.15f, -5.25f, 4.0f, -9.85f},
        {5.25f, 0f, -10.15f, 5.55f, 4.0f, -9.85f},
        {-16.65f, 0f, 7.85f, -16.35f, 4.0f, 8.15f},
        {16.35f, 0f, 7.85f, 16.65f, 4.0f, 8.15f},
        {-5.55f, 0f, 25.85f, -5.25f, 4.0f, 26.15f},
        {5.25f, 0f, 25.85f, 5.55f, 4.0f, 26.15f},
        {-5.55f, 0f, 43.85f, -5.25f, 4.0f, 44.15f},
        {5.25f, 0f, 43.85f, 5.55f, 4.0f, 44.15f},
        {-5.55f, 0f, 59.85f, -5.25f, 4.0f, 60.15f},
        {5.25f, 0f, 59.85f, 5.55f, 4.0f, 60.15f},

        {-1.1f, 0f, -17.1f, 1.1f, 0.4f, -14.9f},
        {-0.5f, 0.4f, -16.5f, 0.5f, 3.0f, -15.5f},

        {-46.1f, 0f, 16.9f, -43.9f, 0.4f, 19.1f},
        {-45.5f, 0.4f, 17.5f, -44.5f, 3.0f, 18.5f},

        {-7.6f, 0f, 46.9f, -5.4f, 0.4f, 49.1f},
        {-7.0f, 0.4f, 47.5f, -6.0f, 3.0f, 48.5f},

        {46.9f, 0f, 16.9f, 49.1f, 0.4f, 19.1f},
        {47.5f, 0.4f, 17.5f, 48.5f, 3.0f, 18.5f},

        {26.9f, 0f, -11.1f, 29.1f, 0.4f, -8.9f},
        {27.5f, 0.4f, -10.5f, 28.5f, 3.0f, -9.5f},

        {7.1f, 0f, 35.8f, 8.5f, 0.35f, 37.2f},
        {7.3f, 0f, 36.1f, 8.3f, 1.85f, 36.9f},
        {8.6f, 0f, 34.8f, 10.5f, 0.95f, 36.4f},
        {5.6f, 0f, 37.0f, 7.0f, 0.85f, 38.4f},
        {8.2f, 0f, 37.1f, 10.4f, 1.25f, 39.0f},
        {9.7f, 0f, 32.5f, 12.1f, 1.40f, 34.6f},

        {-9.3f, 0f, -6.3f, -8.7f, 0.45f, 18.3f},

        {8.7f, 0f, -6.3f, 9.3f, 0.45f, 18.3f},

        {-9.3f, 0f, -6.3f, 9.3f, 0.45f, -5.7f},

        {-9.3f, 0f, 17.7f, 9.3f, 0.45f, 18.3f},

        {-110f, 0f, 77.2f, -4.2f, 2.8f, 78.8f},
        {4.2f, 0f, 77.2f, 110f, 2.8f, 78.8f},
        {-4.4f, 0f, 76.6f, -2.2f, 7.2f, 79.4f},
        {2.2f, 0f, 76.6f, 4.4f, 7.2f, 79.4f},
        {7.0f, 0f, 74.0f, 10.0f, 3.2f, 77.0f},

        {-100f, 0f, -45.0f, 100f, 2.8f, -43.8f},

        {-86f, 0f, 29.8f, -50f, 11.5f, 38.2f},
        {-86f, 0f, 5.8f, -50f, 11.5f, 14.2f},
        {-86.2f, 0f, 14.0f, -77.8f, 11.5f, 30.0f},
        {-58.2f, 0f, 14.0f, -49.8f, 11.5f, 30.0f},
        {-52f, 0f, 17f, -46f, 0.6f, 27f},

        {-62.5f, 0f, 49.5f, -53.5f, 0.6f, 58.5f},
        {-58.8f, 0.6f, 53.2f, -57.2f, 3.2f, 54.8f},

        {-65f, 0f, -23f, -47f, 0.6f, -9f},
        {-64.2f, 0.6f, -22.2f, -49.8f, 4.2f, -11.8f},

        {-43.5f, 0f, 32.5f, -40.5f, 2.6f, 35.5f},
        {-43.5f, 0f, 36.5f, -40.5f, 2.6f, 39.5f},
        {-43.5f, 0f, 40.5f, -40.5f, 2.6f, 43.5f},

        {-35.1f, 0f, 22.9f, -28.9f, 0.4f, 29.1f},
        {-34.5f, 0f, 23.5f, -29.5f, 0.65f, 28.5f},
        {-33.9f, 0f, 24.1f, -30.1f, 1.35f, 27.9f},

        {-36.2f, 0f, 45.8f, -15.8f, 9.0f, 58.2f},

        {15.8f, 0f, 45.8f, 36.2f, 9.0f, 58.2f},

        {36.5f, 0f, 34.5f, 51.5f, 0.35f, 49.5f},
        {41.6f, 0f, 39.6f, 46.4f, 1.15f, 44.4f},
        {43.2f, 1.15f, 41.2f, 44.8f, 6.2f, 42.8f},

        {49.0f, 0f, 4.8f, 87.0f, 10.5f, 35.2f},
        {44.8f, 0f, 12.0f, 49.0f, 0.5f, 28.0f},

        {44.8f, 0f, -25.2f, 67.2f, 0.5f, -6.8f},
        {52.5f, 0f, -18.5f, 54.5f, 1.2f, -16.5f},
        {57.5f, 0f, -18.5f, 59.5f, 1.0f, -16.5f},
        {54.5f, 0f, -14.5f, 57.5f, 1.5f, -11.5f},
    };

    private float[][] boxes = COLLISION_BOXES;

    public PlayerController() {
    }

    public void setExtraColliders(java.util.List<float[]> extra) {
        float[][] all = new float[COLLISION_BOXES.length + extra.size()][];
        System.arraycopy(COLLISION_BOXES, 0, all, 0, COLLISION_BOXES.length);
        for (int i = 0; i < extra.size(); i++) {
            all[COLLISION_BOXES.length + i] = extra.get(i);
        }
        boxes = all;
    }

    public float[][] getColliders() {
        return boxes;
    }

    public void update(float delta, float cameraYawDegrees, boolean inputEnabled) {
        if (delta > 0.1f) delta = 0.1f;

        boolean wasGrounded = isGrounded;
        float prevVerticalVelocity = verticalVelocity;

        if (dodgeCooldown > 0f) dodgeCooldown -= delta;
        if (staggerTimer > 0f) staggerTimer -= delta;

        moveDir.setZero();
        boolean sprintKey = false;
        boolean jumpKey = false;
        boolean dodgeKey = false;

        if (inputEnabled) {
            boolean up = GameInput.isPressed(GameAction.MOVE_FORWARD);
            boolean down = GameInput.isPressed(GameAction.MOVE_BACK);
            boolean left = GameInput.isPressed(GameAction.MOVE_LEFT);
            boolean right = GameInput.isPressed(GameAction.MOVE_RIGHT);

            sprintKey = GameInput.isPressed(GameAction.SPRINT) ||
                        (Gdx.input != null && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.R)));

            if (GameInput.isJustPressed(GameAction.CROUCH)) {
                isCrouching = !isCrouching;
            }
            if (Gdx.input != null && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                isCrouching = true;
            }

            if (bd.spark36.level.LevelConfig.isCombatEnabled()) {
                dodgeKey = GameInput.isJustPressed(GameAction.DODGE);

                boolean blockHeld = GameInput.isPressed(GameAction.DEFEND_BLOCK);
                if (blockHeld) {
                    if (!isBlocking) {
                        blockTimer = 0f;
                        bd.spark36.audio.SoundManager.get().playBlock();
                    } else {
                        blockTimer += delta;
                    }
                    isBlocking = true;
                } else {
                    isBlocking = false;
                    blockTimer = 0f;
                }
            } else {
                dodgeKey = false;
                isBlocking = false;
                blockTimer = 0f;
            }

            if (GameInput.isJustPressed(GameAction.TOGGLE_GENDER)) {
                gender = (gender == 0) ? 1 : 0;
            }

            boolean punchPressed = GameInput.isJustPressed(GameAction.ATTACK_PUNCH) && !isSittingWater;
            boolean kickPressed = GameInput.isJustPressed(GameAction.ATTACK_KICK);

            if (up) moveDir.y += 1f;
            if (down) moveDir.y -= 1f;
            if (left) moveDir.x -= 1f;
            if (right) moveDir.x += 1f;

            if (moveDir.len2() > 0.001f) {
                moveDir.nor();
            }

            if (dodgeKey && dodgeCooldown <= 0f && !isSittingWater && isGrounded) {
                isDodging = true;
                dodgeTimer = DODGE_DURATION;
                dodgeProgress = 0f;
                dodgeCooldown = 0.38f;
                isAttacking = false;
                bufferedAttack = 0;
                attackProgress = 0f;
                bd.spark36.audio.SoundManager.get().playWhoosh();

                float yawRad = cameraYawDegrees * MathUtils.degreesToRadians;
                float forwardX = -MathUtils.sin(yawRad);
                float forwardZ = -MathUtils.cos(yawRad);
                float rightX = MathUtils.cos(yawRad);
                float rightZ = -MathUtils.sin(yawRad);

                if (moveDir.len2() > 0.001f) {
                    dodgeDir.set(forwardX * moveDir.y + rightX * moveDir.x, forwardZ * moveDir.y + rightZ * moveDir.x).nor();
                } else {

                    dodgeDir.set(-forwardX, -forwardZ).nor();
                }
            }

            if (punchPressed && !isSittingWater && !isDodging) {
                if (!isAttacking) {
                    isAttacking = true;
                    attackProgress = 0f;
                    bufferedAttack = 0;
                    if (isCrouching) {
                        attackCombo = 5;
                    } else if (comboWindowTimer > 0f) {
                        attackCombo = (attackCombo == 1) ? 2 : 1;
                    } else {
                        attackCombo = 1;
                    }
                    applyCombatFacing();
                    bd.spark36.audio.SoundManager.get().playWhoosh();
                } else if (attackProgress >= 0.45f) {
                    bufferedAttack = 1;
                }
            } else if (kickPressed && !isSittingWater && !isDodging) {
                if (!isAttacking) {
                    isAttacking = true;
                    attackProgress = 0f;
                    bufferedAttack = 0;
                    if (isCrouching) {
                        attackCombo = 5;
                    } else if (comboWindowTimer > 0f && (attackCombo == 3 || attackCombo == 4)) {
                        attackCombo = (attackCombo == 3) ? 4 : 3;
                    } else {
                        attackCombo = 3;
                    }
                    applyCombatFacing();
                    bd.spark36.audio.SoundManager.get().playWhoosh();
                } else if (attackProgress >= 0.45f) {
                    bufferedAttack = 2;
                }
            }

            jumpKey = GameInput.isJustPressed(GameAction.JUMP) ||
                      (Gdx.input != null && Gdx.input.isKeyJustPressed(Input.Keys.V));
        } else {
            isBlocking = false;
        }

        isMoving = moveDir.len2() > 0.001f;

        stamina = maxStamina;
        staminaExhausted = false;
        isSprinting = isMoving && sprintKey;

        if (isDodging) {
            dodgeTimer -= delta;
            dodgeProgress = 1.0f - Math.max(0f, dodgeTimer / DODGE_DURATION);
            if (dodgeTimer <= 0f) {
                isDodging = false;
                dodgeProgress = 1.0f;
            }
        }

        if (isAttacking) {
            attackProgress += delta * 3.4f;
            if (attackProgress >= 1.0f) {
                if (bufferedAttack > 0) {
                    int next = bufferedAttack;
                    bufferedAttack = 0;
                    attackProgress = 0f;
                    combatState = PlayerCombatState.ATTACKING;
                    if (next == 1) {
                        if (isCrouching) {
                            attackCombo = 5;
                        } else {
                            attackCombo = (attackCombo == 1) ? 2 : 1;
                        }
                    } else {
                        if (isCrouching) {
                            attackCombo = 5;
                        } else {
                            attackCombo = (attackCombo == 3) ? 4 : 3;
                        }
                    }
                    applyCombatFacing();
                    bd.spark36.audio.SoundManager.get().playWhoosh();
                } else {
                    isAttacking = false;
                    attackProgress = 1.0f;
                    comboWindowTimer = 0.65f;
                    combatState = PlayerCombatState.ATTACK_RECOVERY;
                    attackRecoveryTimer = 0.15f;
                }
            } else if (attackProgress >= 0.70f) {
                combatState = PlayerCombatState.ATTACK_RECOVERY;
            } else {
                combatState = PlayerCombatState.ATTACKING;
            }
        } else if (attackRecoveryTimer > 0f) {
            attackRecoveryTimer -= delta;
            if (attackRecoveryTimer <= 0f) {
                combatState = PlayerCombatState.IDLE;
            } else {
                combatState = PlayerCombatState.ATTACK_RECOVERY;
            }
        } else {
            combatState = PlayerCombatState.IDLE;
        }

        if (comboWindowTimer > 0f) {
            comboWindowTimer -= delta;
            if (comboWindowTimer <= 0f) {
                attackCombo = 0;
            }
        }

        if (isSittingWater) {
            sitProgress = Math.min(1.0f, sitProgress + delta * 3.5f);
            velocity.set(0f, 0f, 0f);
            boolean breakSit = (inputEnabled && (((Gdx.input != null && (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.S) ||
                                                 Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)))) ||
                                                 jumpKey || isAttacking || isDodging));
            if (breakSit) {
                isSittingWater = false;
            }
        } else {
            sitProgress = Math.max(0f, sitProgress - delta * 4.0f);
        }

        float currentSpeed = isCrouching ? (isSprinting ? 2.6f : 1.8f) : (isSprinting ? SPRINT_SPEED : WALK_SPEED);
        if (isWading()) currentSpeed *= WADE_SPEED_FACTOR;
        if (isSittingWater) currentSpeed = 0f;
        float targetVx = 0f;
        float targetVz = 0f;

        if (isDodging) {
            targetVx = dodgeDir.x * DODGE_SPEED;
            targetVz = dodgeDir.y * DODGE_SPEED;
        } else {
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

            if (isAttacking && isGrounded) {
                float baseLungeSpeed = (attackCombo == 1) ? 2.4f :
                                       (attackCombo == 2) ? 3.6f :
                                       (attackCombo == 3) ? 3.2f :
                                       (attackCombo == 4) ? 3.8f : 4.4f;
                float stepFactor = Math.max(0f, 1.0f - (attackProgress / 0.58f));
                float lungeSpeed = baseLungeSpeed * stepFactor;

                float lungeVx = MathUtils.sinDeg(headingDegrees) * lungeSpeed;
                float lungeVz = MathUtils.cosDeg(headingDegrees) * lungeSpeed;

                if (isMoving) {
                    targetVx = targetVx * 0.40f + lungeVx * 0.70f;
                    targetVz = targetVz * 0.40f + lungeVz * 0.70f;
                } else {
                    targetVx = lungeVx;
                    targetVz = lungeVz;
                }
            }
        }

        if (knockbackVel.len2() > 0.001f) {
            velocity.x += knockbackVel.x;
            velocity.z += knockbackVel.z;
            knockbackVel.scl(Math.max(0f, 1f - 14f * delta));
        }

        if (isGrounded) {
            velocity.x = MathUtils.lerp(velocity.x, targetVx, ACCELERATION * delta);
            velocity.z = MathUtils.lerp(velocity.z, targetVz, ACCELERATION * delta);
        } else {

            if (isMoving) {
                velocity.x = MathUtils.lerp(velocity.x, targetVx * 1.25f, (ACCELERATION * 0.6f) * delta);
                velocity.z = MathUtils.lerp(velocity.z, targetVz * 1.25f, (ACCELERATION * 0.6f) * delta);
            }
        }

        if (isMoving || (isAttacking && attackProgress < 0.35f)) {
            float diff = (targetHeadingDegrees - headingDegrees) % 360f;
            if (diff > 180f) diff -= 360f;
            if (diff < -180f) diff += 360f;
            float turnSpeed = isAttacking ? 22f : 15f;
            headingDegrees += diff * Math.min(1f, turnSpeed * delta);
        }

        if (jumpKey && inputEnabled) {
            if (isGrounded) {

                jumpCount = 1;
                isGrounded = false;
                verticalVelocity = JUMP_VELOCITY;

                if (isSprinting && isMoving) {

                    velocity.x *= 1.45f;
                    velocity.z *= 1.45f;
                } else if (isMoving) {

                    velocity.x *= 1.30f;
                    velocity.z *= 1.30f;
                }
            } else if (jumpCount < MAX_JUMPS) {

                jumpCount = 2;
                verticalVelocity = DOUBLE_JUMP_VELOCITY;

                if (isMoving) {

                    float yawRad = cameraYawDegrees * MathUtils.degreesToRadians;
                    float forwardX = -MathUtils.sin(yawRad);
                    float forwardZ = -MathUtils.cos(yawRad);
                    float rightX = MathUtils.cos(yawRad);
                    float rightZ = -MathUtils.sin(yawRad);

                    float worldMoveX = forwardX * moveDir.y + rightX * moveDir.x;
                    float worldMoveZ = forwardZ * moveDir.y + rightZ * moveDir.x;

                    velocity.x = worldMoveX * SPRINT_SPEED * 1.35f;
                    velocity.z = worldMoveZ * SPRINT_SPEED * 1.35f;
                }
            }
        }

        boolean climbHeld = inputEnabled && isMoving && !isSittingWater && Gdx.input != null &&
            (Gdx.input.isKeyPressed(Input.Keys.J) || Gdx.input.isKeyPressed(Input.Keys.SPACE));
        if (climbHeld && wallBlocked && wallTopY > position.y + STEP_UP_HEIGHT) {
            isClimbing = true;
            isGrounded = false;
            float remaining = wallTopY - position.y;
            if (remaining < 1.1f) {

                verticalVelocity = (float) Math.sqrt(2f * -GRAVITY * (remaining + 0.35f));
                velocity.x = wallPushX * WALK_SPEED;
                velocity.z = wallPushZ * WALK_SPEED;
            } else {
                verticalVelocity = CLIMB_SPEED;
            }
        } else if (isClimbing && (isGrounded || !climbHeld)) {
            isClimbing = false;
        }

        prevPosition.set(position);
        boolean airborneBeforeResolution = !isGrounded;
        float fallingVelocityBeforeResolution = verticalVelocity;
        resolveMovementAndCollisions(delta);

        if (airborneBeforeResolution && isGrounded && fallingVelocityBeforeResolution < -1.8f) {
            animStateMachine.notifyLanded(fallingVelocityBeforeResolution);
        }

        animStateMachine.update(
            delta, position, headingDegrees, velocity, isGrounded, isMoving,
            isSprinting, isCrouching, isClimbing, isWading(), isSittingWater,
            isBlocking, isAttacking, attackCombo, attackProgress, isDodging,
            dodgeProgress, staggerTimer
        );
        walkCycle = animStateMachine.getStridePhase();
    }

    private void resolveMovementAndCollisions(float delta) {

        if (!isGrounded) {
            verticalVelocity += GRAVITY * delta;
        }

        float proposedY = position.y + verticalVelocity * delta;

        float baseGround = groundLevelAt(position.x, position.z);
        float highestGround = baseGround;
        float stepUp = isWading() ? WADE_STEP_UP_HEIGHT : STEP_UP_HEIGHT;
        wallBlocked = false;

        for (float[] box : boxes) {
            float bMinX = box[0], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            boolean overlapX = (position.x + PLAYER_RADIUS > bMinX) && (position.x - PLAYER_RADIUS < bMaxX);
            boolean overlapZ = (position.z + PLAYER_RADIUS > bMinZ) && (position.z - PLAYER_RADIUS < bMaxZ);

            if (overlapX && overlapZ) {

                if (position.y >= bMaxY - 0.40f || prevPosition.y >= bMaxY - 0.40f || (proposedY <= bMaxY + 0.15f && position.y >= bMaxY - 0.50f)) {
                    if (bMaxY > highestGround) {
                        highestGround = bMaxY;
                    }
                }
            }
        }

        if (proposedY <= highestGround) {
            position.y = highestGround;
            verticalVelocity = 0f;
            isGrounded = true;
            jumpCount = 0;
        } else {
            position.y = proposedY;
            isGrounded = false;
        }

        float moveX = velocity.x * delta;
        float proposedX = position.x + moveX;

        for (float[] box : boxes) {
            float bMinX = box[0], bMinY = box[1], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            if (position.y >= bMaxY - 0.05f) continue;

            if (position.y + PLAYER_HEIGHT <= bMinY) continue;

            boolean overlapZ = (position.z + PLAYER_RADIUS > bMinZ) && (position.z - PLAYER_RADIUS < bMaxZ);
            if (!overlapZ) continue;

            boolean overlapX = (proposedX + PLAYER_RADIUS > bMinX) && (proposedX - PLAYER_RADIUS < bMaxX);
            if (overlapX) {

                if (bMaxY <= position.y + stepUp && (bMaxY - bMinY) <= STEP_UP_HEIGHT) {

                } else if (!isGrounded && (position.y + 0.65f >= bMaxY || (verticalVelocity > 0f && position.y + (verticalVelocity * verticalVelocity / (2 * -GRAVITY)) >= bMaxY))) {

                } else {

                    if (position.x <= (bMinX + bMaxX) / 2f) {
                        proposedX = bMinX - PLAYER_RADIUS;
                        recordWall(box, 1f, 0f);
                    } else {
                        proposedX = bMaxX + PLAYER_RADIUS;
                        recordWall(box, -1f, 0f);
                    }
                    velocity.x = 0f;
                }
            }
        }
        position.x = proposedX;

        float moveZ = velocity.z * delta;
        float proposedZ = position.z + moveZ;

        for (float[] box : boxes) {
            float bMinX = box[0], bMinY = box[1], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            if (position.y >= bMaxY - 0.05f) continue;

            if (position.y + PLAYER_HEIGHT <= bMinY) continue;

            boolean overlapX = (position.x + PLAYER_RADIUS > bMinX) && (position.x - PLAYER_RADIUS < bMaxX);
            if (!overlapX) continue;

            boolean overlapZ = (proposedZ + PLAYER_RADIUS > bMinZ) && (proposedZ - PLAYER_RADIUS < bMaxZ);
            if (overlapZ) {

                if (bMaxY <= position.y + stepUp && (bMaxY - bMinY) <= STEP_UP_HEIGHT) {

                } else if (!isGrounded && (position.y + 0.65f >= bMaxY || (verticalVelocity > 0f && position.y + (verticalVelocity * verticalVelocity / (2 * -GRAVITY)) >= bMaxY))) {

                } else {

                    if (position.z <= (bMinZ + bMaxZ) / 2f) {
                        proposedZ = bMinZ - PLAYER_RADIUS;
                        recordWall(box, 0f, 1f);
                    } else {
                        proposedZ = bMaxZ + PLAYER_RADIUS;
                        recordWall(box, 0f, -1f);
                    }
                    velocity.z = 0f;
                }
            }
        }
        position.z = proposedZ;

        float surfaceUnderFoot = groundLevelAt(position.x, position.z);
        for (float[] box : boxes) {
            float bMinX = box[0], bMinZ = box[2];
            float bMaxX = box[3], bMaxY = box[4], bMaxZ = box[5];

            boolean overlapX = (position.x + PLAYER_RADIUS > bMinX) && (position.x - PLAYER_RADIUS < bMaxX);
            boolean overlapZ = (position.z + PLAYER_RADIUS > bMinZ) && (position.z - PLAYER_RADIUS < bMaxZ);

            if (overlapX && overlapZ) {

                if (bMaxY >= position.y && bMaxY <= position.y + stepUp) {
                    if (bMaxY > surfaceUnderFoot) {
                        surfaceUnderFoot = bMaxY;
                    }
                } else if (position.y >= bMaxY - 0.40f) {
                    if (bMaxY > surfaceUnderFoot) {
                        surfaceUnderFoot = bMaxY;
                    }
                }
            }
        }

        if (surfaceUnderFoot > position.y) {
            position.y = surfaceUnderFoot;
            verticalVelocity = 0f;
            isGrounded = true;
            jumpCount = 0;
        }

        if (isGrounded && position.y > surfaceUnderFoot + 0.05f) {
            if (surfaceUnderFoot < position.y - 0.10f) {
                isGrounded = false;
            }
        }

        position.x = MathUtils.clamp(position.x, -95f, 95f);
        position.z = MathUtils.clamp(position.z, -55f, 85f);
    }

    public static float groundLevelAt(float x, float z) {
        return TerrainSurface.getGroundHeight(x, z);
    }

    private void recordWall(float[] box, float pushX, float pushZ) {
        float width = Math.max(box[3] - box[0], box[5] - box[2]);
        if (width < MIN_CLIMB_WALL_WIDTH) return;
        wallBlocked = true;
        wallTopY = box[4];
        wallPushX = pushX;
        wallPushZ = pushZ;
    }

    public boolean isWading() {
        return groundLevelAt(position.x, position.z) < 0f && position.y < 0.2f;
    }

    public boolean isClimbing() {
        return isClimbing;
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

    public void setHeadingDegrees(float deg) {
        this.headingDegrees = deg;
        this.targetHeadingDegrees = deg;
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
    public boolean isCrouching() {
        return isCrouching;
    }

    public void setCrouching(boolean crouching) {
        this.isCrouching = crouching;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public int getAttackCombo() {
        return attackCombo;
    }

    public float getAttackProgress() {
        return attackProgress;
    }

    public void setAttackingForTest(boolean attacking, float progress) {
        this.isAttacking = attacking;
        this.attackProgress = progress;
        this.attackCombo = 1;
    }

    public boolean isSittingWater() {
        return isSittingWater;
    }

    public float getSitProgress() {
        return sitProgress;
    }

    public void setSittingWater(boolean sitting) {
        this.isSittingWater = sitting;
        if (sitting) {
            this.sitProgress = 1f;
            this.velocity.set(0f, 0f, 0f);
        }
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public float getBlockTimer() {
        return blockTimer;
    }

    public void setBlocking(boolean blocking) {
        this.isBlocking = blocking;
    }

    public boolean isDodging() {
        return isDodging;
    }

    public float getDodgeProgress() {
        return dodgeProgress;
    }

    public float getStaggerTimer() {
        return staggerTimer;
    }

    public void setCombatTarget(Vector3 target) {
        if (target != null) {
            combatTarget.set(target);
            hasCombatTarget = true;
        } else {
            hasCombatTarget = false;
        }
    }

    public void clearCombatTarget() {
        hasCombatTarget = false;
    }

    public void applyCombatFacing() {
        if (hasCombatTarget && isGrounded) {
            float dst = position.dst(combatTarget);
            if (dst <= 4.2f && dst > 0.3f) {
                float dx = combatTarget.x - position.x;
                float dz = combatTarget.z - position.z;
                float enemyAngle = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
                float diff = (enemyAngle - headingDegrees) % 360f;
                if (diff > 180f) diff -= 360f;
                if (diff < -180f) diff += 360f;
                if (Math.abs(diff) < 130f) {
                    targetHeadingDegrees = enemyAngle;
                    headingDegrees = MathUtils.lerpAngleDeg(headingDegrees, enemyAngle, 0.65f);
                }
            }
        }
    }

    public void takeDamage(float amount) {
        takeDamage(amount, 0f, 0f, 0f);
    }

    public void takeDamage(float amount, float knockbackDirX, float knockbackDirZ, float knockbackStrength) {
        if (!bd.spark36.level.LevelConfig.isCombatEnabled()) {
            return;
        }
        this.health = Math.max(0f, this.health - amount);
        this.staggerTimer = isBlocking ? 0.08f : 0.28f;
        float factor = isBlocking ? 0.30f : 1.0f;
        if (isBlocking) {
            bd.spark36.audio.SoundManager.get().playBlock();
        } else {
            bd.spark36.audio.SoundManager.get().playPunch();
        }
        if (knockbackStrength > 0f) {
            this.knockbackVel.set(knockbackDirX * knockbackStrength * factor, 0f, knockbackDirZ * knockbackStrength * factor);
        }
    }

    public boolean isKnockedOut() {
        return health <= 0f;
    }

    public void resetCombat() {
        this.health = maxHealth;
        this.isAttacking = false;
        this.isBlocking = false;
        this.isDodging = false;
        this.dodgeTimer = 0f;
        this.dodgeCooldown = 0f;
        this.staggerTimer = 0f;
        this.knockbackVel.setZero();
        this.bufferedAttack = 0;
        this.attackCombo = 0;
        this.attackProgress = 0f;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = (gender == 1) ? 1 : 0;
    }

    public AnimationStateMachine getAnimStateMachine() {
        return animStateMachine;
    }

    public CharacterPose getPose() {
        return animStateMachine.getBlendedPose();
    }

    public LocomotionState getLocomotionState() {
        return animStateMachine.getCurrentState();
    }

    public PlayerCombatState getCombatState() {
        return combatState;
    }

    public void resetAnimation() {
        animStateMachine.reset();
    }
}
