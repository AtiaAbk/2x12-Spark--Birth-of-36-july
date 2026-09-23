package bd.historicalgame.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Camera-relative 3D player controller for Level 1.
 *
 * Features:
 * - Smooth acceleration/deceleration
 * - Camera-relative WASD movement
 * - Shift/Space jump
 * - Simple gravity
 * - Solid buildings: walking into a wall is blocked, exactly like
 *   a real building - the only way up onto a roof is to jump for
 *   it, and jumping onto a roof also blocks falling straight
 *   through it back into the building interior.
 */
public class Player {

    private static final float ACCELERATION = 18f;
    private static final float DECELERATION = 22f;
    private static final float VELOCITY_EPSILON = 0.01f;
    /*
     * Slightly smaller footprint keeps the character visually
     * proportional to the campus architecture while preserving
     * stable building collision.
     */
    private static final float COLLISION_RADIUS = 0.48f;

    /*
     * Keep these values synchronized with GameConfig.
     *
     * The playable campus is intentionally much larger than the
     * original compact prototype area.
     */
    private static final float MIN_X = -70f;
    private static final float MAX_X = 70f;
    private static final float MIN_Z = -52f;
    private static final float MAX_Z = 52f;

    private static final float GROUND_Y = 1.0f;
    private static final float GRAVITY = 24f;
    private static final float JUMP_SPEED = 19f;
    private static final float JUMP_COOLDOWN = 0.12f;

    // Small grace window makes keyboard jumping feel responsive.
    private static final float COYOTE_TIME = 0.10f;
    private static final float PLAYER_HALF_HEIGHT = 0.75f;

    private final Vector3 position;
    private final float speed;

    private float velocityX;
    private float velocityZ;
    private float verticalVelocity;
    private float jumpCooldown;
    private float coyoteTimer = COYOTE_TIME;

    private boolean moving;
    private boolean grounded = true;
    private boolean jumping;
    private boolean sprinting;

    // Where Winds Meet style Vitality & Stamina system
    private float stamina = 100f;
    private static final float MAX_STAMINA = 100f;
    private static final float STAMINA_DRAIN_RATE = 18f;
    private static final float STAMINA_RECOVERY_RATE = 22f;
    private static final float JUMP_STAMINA_COST = 10f;
    private float health = 100f;
    private static final float MAX_HEALTH = 100f;
    private static final float SPRINT_MULTIPLIER = 1.8f;

    private final Vector3 movementDirection =
        new Vector3(0f, 0f, -1f);

    private final Array<CollisionSurface> collisionObjects =
        new Array<>();

    public Player(
        float startX,
        float startY,
        float startZ,
        float speed
    ) {
        position = new Vector3(
            startX,
            startY,
            startZ
        );

        this.speed = speed;

        if (position.y < GROUND_Y) {
            position.y = GROUND_Y;
        }

        clampToWorld();
    }

    /**
     * Existing wall/ground collision API.
     * A normal collision surface has ground-level top height.
     */
    public void addCollision(
        float x,
        float z,
        float width,
        float depth
    ) {
        addCollision(
            x,
            z,
            width,
            depth,
            GROUND_Y
        );
    }

    /**
     * Adds a rectangular surface with a top height.
     * The surface blocks the player while grounded and becomes a
     * jumpable platform while airborne.
     */
    public void addCollision(
        float x,
        float z,
        float width,
        float depth,
        float topY
    ) {
        collisionObjects.add(
            new CollisionSurface(
                x,
                z,
                width,
                depth,
                topY
            )
        );
    }

    public void clearCollisions() {
        collisionObjects.clear();
    }

    /**
     * Camera-relative movement update.
     */
    public void update(
        float delta,
        Vector3 cameraForward,
        Vector3 cameraRight
    ) {
        delta = Math.min(
            Math.max(delta, 0f),
            0.05f
        );

        jumpCooldown =
            Math.max(
                0f,
                jumpCooldown - delta
            );

        if (grounded) {
            coyoteTimer = COYOTE_TIME;
        } else {
            coyoteTimer = Math.max(0f, coyoteTimer - delta);
        }

        float forwardInput = 0f;
        float rightInput = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            forwardInput += 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            forwardInput -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            rightInput += 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            rightInput -= 1f;
        }

        Vector3 move = new Vector3();
        move.mulAdd(cameraForward, forwardInput);
        move.mulAdd(cameraRight, rightInput);
        move.y = 0f;

        if (move.len2() > 0f) {
            move.nor();
        }

        moving = move.len2() > 0f;

        // Sprint detection & stamina handling
        boolean wantsSprint = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        if (wantsSprint && moving && stamina > 5f) {
            sprinting = true;
            stamina = Math.max(0f, stamina - STAMINA_DRAIN_RATE * delta);
        } else {
            sprinting = false;
            stamina = Math.min(MAX_STAMINA, stamina + STAMINA_RECOVERY_RATE * delta);
        }

        float effectiveSpeed = sprinting ? (speed * SPRINT_MULTIPLIER) : speed;
        float targetX = move.x * effectiveSpeed;
        float targetZ = move.z * effectiveSpeed;

        if (moving) {
            velocityX = moveTowards(
                velocityX,
                targetX,
                ACCELERATION * (sprinting ? 1.4f : 1f) * delta
            );

            velocityZ = moveTowards(
                velocityZ,
                targetZ,
                ACCELERATION * (sprinting ? 1.4f : 1f) * delta
            );

            movementDirection
                .set(move)
                .nor();
        } else {
            velocityX = moveTowards(
                velocityX,
                0f,
                DECELERATION * delta
            );

            velocityZ = moveTowards(
                velocityZ,
                0f,
                DECELERATION * delta
            );
        }

        // -----------------------------------------------------
        // JUMP
        // -----------------------------------------------------

        if (
            (grounded || coyoteTimer > 0f) &&
            jumpCooldown <= 0f &&
            stamina >= JUMP_STAMINA_COST &&
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
        ) {
            stamina = Math.max(0f, stamina - JUMP_STAMINA_COST);
            jump();
        }

        tryMoveX(
            velocityX * delta
        );

        tryMoveZ(
            velocityZ * delta
        );

        updateVertical(delta);

        clampToWorld();

        if (Math.abs(velocityX) < VELOCITY_EPSILON) {
            velocityX = 0f;
        }

        if (Math.abs(velocityZ) < VELOCITY_EPSILON) {
            velocityZ = 0f;
        }
    }

    /**
     * Backward-compatible overload.
     */
    public void update(float delta) {
        update(
            delta,
            new Vector3(0f, 0f, -1f),
            new Vector3(1f, 0f, 0f)
        );
    }

    private void jump() {
        grounded = false;
        jumping = true;
        coyoteTimer = 0f;
        verticalVelocity = JUMP_SPEED;
        jumpCooldown = JUMP_COOLDOWN;
    }

    private void updateVertical(float delta) {

        // If the player is standing on a raised platform and walks
        // away from it, start falling naturally.
        if (grounded) {
            float support = getSupportHeight();

            if (support >= 0f) {
                position.y = support;
                return;
            }

            grounded = false;
            jumping = false;
            verticalVelocity = 0f;
        }

        float oldY = position.y;

        verticalVelocity -= GRAVITY * delta;

        float newY =
            position.y + verticalVelocity * delta;

        float landingY =
            findLandingHeight(
                oldY,
                newY
            );

        if (landingY >= 0f && verticalVelocity <= 0f) {
            position.y = landingY;
            verticalVelocity = 0f;
            grounded = true;
            jumping = false;
            coyoteTimer = COYOTE_TIME;
            return;
        }

        if (newY <= GROUND_Y) {
            position.y = GROUND_Y;
            verticalVelocity = 0f;
            grounded = true;
            jumping = false;
            coyoteTimer = COYOTE_TIME;
            return;
        }

        position.y = newY;
        grounded = false;
    }

    private float findLandingHeight(
        float oldY,
        float newY
    ) {
        float best = -1f;

        for (CollisionSurface surface : collisionObjects) {
            if (!overlapsFootprint(surface)) {
                continue;
            }

            float top = surface.topY;
            float landingCenterY = top + PLAYER_HALF_HEIGHT;

            if (
                oldY >= landingCenterY &&
                newY <= landingCenterY &&
                top > GROUND_Y + 0.05f
            ) {
                if (landingCenterY > best) {
                    best = landingCenterY;
                }
            }
        }

        return best;
    }

    private float getSupportHeight() {
        float best = -1f;

        for (CollisionSurface surface : collisionObjects) {
            if (!overlapsFootprint(surface)) {
                continue;
            }

            float supportCenterY =
                surface.topY +
                PLAYER_HALF_HEIGHT;

            if (
                Math.abs(position.y - supportCenterY) <= 0.12f &&
                supportCenterY > best
            ) {
                best = supportCenterY;
            }
        }

        return best;
    }

    private float moveTowards(
        float current,
        float target,
        float maxDelta
    ) {
        if (
            Math.abs(target - current) <=
            maxDelta
        ) {
            return target;
        }

        return current < target
            ? current + maxDelta
            : current - maxDelta;
    }

    private void tryMoveX(float amount) {
        if (amount == 0f) return;

        float newX =
            position.x + amount;

        if (collidesAtGroundLevel(newX, position.z)) {
            velocityX = 0f;
        } else {
            position.x = newX;
        }
    }

    private void tryMoveZ(float amount) {
        if (amount == 0f) return;

        float newZ =
            position.z + amount;

        if (collidesAtGroundLevel(position.x, newZ)) {
            velocityZ = 0f;
        } else {
            position.z = newZ;
        }
    }

    /**
     * True if moving to (x, z) would walk the player into a solid
     * building wall.
     *
     * A surface only blocks the player while they are below its
     * rooftop: this is what stops "phasing through" building walls
     * while walking (in real life you cannot walk through a wall),
     * but still lets the player walk freely once they have jumped
     * up onto that same surface's roof.
     */
    private boolean collidesAtGroundLevel(
        float x,
        float z
    ) {
        Rectangle bounds = new Rectangle(
            x - COLLISION_RADIUS,
            z - COLLISION_RADIUS,
            COLLISION_RADIUS * 2f,
            COLLISION_RADIUS * 2f
        );

        for (CollisionSurface surface : collisionObjects) {

            if (!bounds.overlaps(surface.bounds)) {
                continue;
            }

            // Already standing at/above this surface's roof means
            // it was reached by jumping - treat it as walkable
            // ground, not a wall.
            if (position.y + 0.05f >= surface.topY) {
                continue;
            }

            return true;
        }

        return false;
    }

    private boolean overlapsFootprint(
        CollisionSurface surface
    ) {
        Rectangle bounds = new Rectangle(
            position.x - COLLISION_RADIUS,
            position.z - COLLISION_RADIUS,
            COLLISION_RADIUS * 2f,
            COLLISION_RADIUS * 2f
        );

        return bounds.overlaps(
            surface.bounds
        );
    }

    private void clampToWorld() {
        if (position.x < MIN_X) {
            position.x = MIN_X;
            velocityX = 0f;
        }

        if (position.x > MAX_X) {
            position.x = MAX_X;
            velocityX = 0f;
        }

        if (position.z < MIN_Z) {
            position.z = MIN_Z;
            velocityZ = 0f;
        }

        if (position.z > MAX_Z) {
            position.z = MAX_Z;
            velocityZ = 0f;
        }
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public Vector3 getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getZ() {
        return position.z;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isJumping() {
        return jumping;
    }

    public float getVerticalVelocity() {
        return verticalVelocity;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityZ() {
        return velocityZ;
    }

    public Vector3 getMovementDirection() {
        return movementDirection;
    }

    public float getCurrentSpeed() {
        return (float) Math.sqrt(
            velocityX * velocityX +
            velocityZ * velocityZ
        );
    }

    public float getStamina() {
        return stamina;
    }

    public float getMaxStamina() {
        return MAX_STAMINA;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return MAX_HEALTH;
    }

    // =========================================================
    // COLLISION SURFACE
    // =========================================================

    private static final class CollisionSurface {

        final Rectangle bounds;
        final float topY;

        CollisionSurface(
            float x,
            float z,
            float width,
            float depth,
            float topY
        ) {
            bounds = new Rectangle(
                x,
                z,
                width,
                depth
            );

            this.topY = topY;
        }
    }
}
