package bd.spark36.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import bd.spark36.world.TerrainSurface;

/**
 * High-performance, allocation-free Animation State Machine and Cross-Fade Blending Engine
 * for 2x12: Spark — Birth of 36 July.
 *
 * <p>Key capabilities:
 * <ul>
 *   <li>Continuous pose generation with Hermite smoothstep cross-fading between locomotion states</li>
 *   <li>Foot sliding elimination via speed-synchronized stride phase scaling</li>
 *   <li>Dynamic directional banking and centrifugal torso lean during turns</li>
 *   <li>Full athletic jump arc with tuck ascent, extended fall, and landing impact cushion</li>
 *   <li>Lightweight terrain adaptation / foot placement for stairs, curbs, and slopes</li>
 *   <li>Stationary idle breathing with natural weight shift and organic fidgets</li>
 * </ul>
 */
public class AnimationStateMachine {

    // States and Cross-fading
    private LocomotionState currentState = LocomotionState.IDLE;
    private LocomotionState previousState = LocomotionState.IDLE;
    private float stateTime = 0f;
    private float transitionTime = 0f;
    private float transitionDuration = 0.18f;
    private boolean isTransitioning = false;

    // Stride and Locomotion timing
    private float stridePhase = 0f;
    private float prevHeading = Float.NaN;
    private float angularVelocity = 0f; // degrees/sec
    private float currentBankingRoll = 0f;
    private float landingTimer = 0f;
    private float landingImpactVelocity = 0f;
    private static final float LANDING_DURATION = 0.22f;

    // Idle fidgets
    private float idleDuration = 0f;
    private int currentFidget = 0; // 0 = none, 1 = glance left/right, 2 = strap adjust, 3 = weight shift

    // Pre-allocated pose buffers (Zero garbage collection during runtime)
    private final CharacterPose sourcePose = new CharacterPose();
    private final CharacterPose targetPose = new CharacterPose();
    private final CharacterPose finalBlendedPose = new CharacterPose();

    // Reusable math vectors
    private final Vector3 tempFootL = new Vector3();
    private final Vector3 tempFootR = new Vector3();

    public AnimationStateMachine() {
        reset();
    }

    public void reset() {
        currentState = LocomotionState.IDLE;
        previousState = LocomotionState.IDLE;
        stateTime = 0f;
        transitionTime = 0f;
        isTransitioning = false;
        stridePhase = 0f;
        prevHeading = Float.NaN;
        angularVelocity = 0f;
        currentBankingRoll = 0f;
        landingTimer = 0f;
        idleDuration = 0f;
        currentFidget = 0;
        sourcePose.reset();
        targetPose.reset();
        finalBlendedPose.reset();
    }

    /**
     * Updates state selection, transition timers, stride progression, and pose blending.
     *
     * @param delta time step in seconds
     * @param rootPos world position of character
     * @param headingDegrees character heading in degrees
     * @param velocity character velocity vector
     * @param isGrounded whether player is in contact with the ground
     * @param isMoving whether movement input is active
     * @param isSprinting whether sprint modifier is held
     * @param isCrouching whether crouching
     * @param isClimbing whether climbing a wall
     * @param isWading whether wading in the pond
     * @param isSittingWater whether sitting on ghat steps
     * @param isBlocking whether in combat guard
     * @param isAttacking whether performing attack
     * @param attackCombo attack combo stage (1..5)
     * @param attackProgress attack animation progress (0..1)
     * @param isDodging whether dodging
     * @param dodgeProgress dodge progress (0..1)
     * @param staggerTimer hit stagger timer
     */
    public void update(float delta, Vector3 rootPos, float headingDegrees, Vector3 velocity,
                       boolean isGrounded, boolean isMoving, boolean isSprinting,
                       boolean isCrouching, boolean isClimbing, boolean isWading,
                       boolean isSittingWater, boolean isBlocking, boolean isAttacking,
                       int attackCombo, float attackProgress, boolean isDodging,
                       float dodgeProgress, float staggerTimer) {

        float safeDelta = Math.min(delta, 0.1f);
        stateTime += safeDelta;

        // 1. Calculate turn rate and centrifugal banking
        if (Float.isNaN(prevHeading)) {
            prevHeading = headingDegrees;
        }
        float headingDiff = (headingDegrees - prevHeading) % 360f;
        if (headingDiff > 180f) headingDiff -= 360f;
        if (headingDiff < -180f) headingDiff += 360f;
        float instantaneousTurnRate = safeDelta > 0.0001f ? (headingDiff / safeDelta) : 0f;
        angularVelocity = MathUtils.lerp(angularVelocity, instantaneousTurnRate, 14f * safeDelta);
        prevHeading = headingDegrees;

        float targetRoll = MathUtils.clamp(-angularVelocity * 0.045f, -8.0f, 8.0f);
        currentBankingRoll = MathUtils.lerp(currentBankingRoll, targetRoll, 10f * safeDelta);

        // 2. Compute horizontal speed & synchronized stride progression
        float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float strideLength = isSprinting ? 2.35f : (isCrouching ? 1.15f : 1.45f);

        if (isGrounded && isMoving && !isSittingWater && !isClimbing) {
            float cadence = (horizontalSpeed / strideLength) * MathUtils.PI2;
            stridePhase += cadence * safeDelta;
            if (stridePhase > MathUtils.PI2 * 1000f) stridePhase %= MathUtils.PI2;
            idleDuration = 0f;
            currentFidget = 0;
        } else {
            idleDuration += safeDelta;
            if (idleDuration > 4.5f) {
                int cycleIdx = ((int) ((idleDuration - 4.5f) / 3.8f)) % 3;
                currentFidget = cycleIdx + 1;
            } else {
                currentFidget = 0;
            }
        }

        // 3. Landing detection and impact compression
        if (landingTimer > 0f) {
            landingTimer -= safeDelta;
        }

        // 4. Determine desired target LocomotionState based on game physics & actions
        LocomotionState desiredState = selectTargetState(
            velocity, horizontalSpeed, isGrounded, isMoving, isSprinting,
            isCrouching, isClimbing, isWading, isSittingWater, isBlocking,
            isAttacking, isDodging, staggerTimer
        );

        // 5. Trigger smooth cross-fade transition if state changed
        if (desiredState != currentState) {
            transitionTo(desiredState, calculateTransitionDuration(currentState, desiredState));
        }

        // 6. Advance transition progress
        float t = 1.0f;
        if (isTransitioning) {
            transitionTime += safeDelta;
            float rawAlpha = Math.min(1.0f, transitionTime / Math.max(0.001f, transitionDuration));
            // Hermite smoothstep for natural organic blending
            t = rawAlpha * rawAlpha * (3.0f - 2.0f * rawAlpha);
            if (rawAlpha >= 1.0f) {
                isTransitioning = false;
                previousState = currentState;
            }
        }

        // 7. Evaluate poses for both states and blend
        evaluatePose(
            previousState, sourcePose, stateTime, stridePhase, horizontalSpeed,
            rootPos, headingDegrees, isSprinting, isCrouching, isAttacking, attackCombo,
            attackProgress, isDodging, dodgeProgress, staggerTimer
        );

        evaluatePose(
            currentState, targetPose, stateTime, stridePhase, horizontalSpeed,
            rootPos, headingDegrees, isSprinting, isCrouching, isAttacking, attackCombo,
            attackProgress, isDodging, dodgeProgress, staggerTimer
        );

        finalBlendedPose.lerp(sourcePose, targetPose, t);

        // 8. Apply Lightweight Foot Placement / Ground Adaptation (IK)
        applyTerrainFootPlacement(rootPos, headingDegrees, finalBlendedPose);
    }

    private LocomotionState selectTargetState(Vector3 velocity, float speed, boolean isGrounded,
                                              boolean isMoving, boolean isSprinting, boolean isCrouching,
                                              boolean isClimbing, boolean isWading, boolean isSittingWater,
                                              boolean isBlocking, boolean isAttacking, boolean isDodging,
                                              float staggerTimer) {

        if (staggerTimer > 0f) return LocomotionState.COMBAT_STAGGER;
        if (isDodging) return LocomotionState.COMBAT_DODGE;
        if (isAttacking) return LocomotionState.COMBAT_ATTACK;
        if (isBlocking) return LocomotionState.COMBAT_GUARD;
        if (isSittingWater) return LocomotionState.SITTING_WATER;
        if (isClimbing) return LocomotionState.WALL_CLIMB;

        // Airborne states
        if (!isGrounded) {
            if (velocity.y > 1.5f) {
                return LocomotionState.AIRBORNE_JUMP;
            } else {
                return LocomotionState.AIRBORNE_FALL;
            }
        }

        // Landing cushion state
        if (landingTimer > 0f) {
            return LocomotionState.LANDING;
        }

        // Crouching locomotion
        if (isCrouching) {
            return isMoving ? LocomotionState.CROUCH_WALK : LocomotionState.CROUCH_IDLE;
        }

        // Water wading
        if (isWading && isMoving) {
            return LocomotionState.WADING;
        }

        // Turning on the spot
        if (!isMoving && Math.abs(angularVelocity) > 28.0f) {
            return LocomotionState.TURN_IN_PLACE;
        }

        // Ground locomotion
        if (isMoving) {
            if (isSprinting && speed > 5.5f) {
                return LocomotionState.SPRINT;
            } else if (isSprinting) {
                return LocomotionState.START_SPRINT;
            } else if (speed < 1.8f && stateTime < 0.22f) {
                return LocomotionState.START_WALK;
            } else {
                return LocomotionState.WALK;
            }
        }

        // Settling from fast movement
        if (previousState == LocomotionState.SPRINT && stateTime < 0.24f) {
            return LocomotionState.STOPPING;
        }

        return LocomotionState.IDLE;
    }

    private float calculateTransitionDuration(LocomotionState from, LocomotionState to) {
        if (to == LocomotionState.COMBAT_DODGE || to == LocomotionState.COMBAT_STAGGER) return 0.06f;
        if (to == LocomotionState.COMBAT_ATTACK) return 0.08f;
        if (to == LocomotionState.AIRBORNE_JUMP) return 0.10f;
        if (to == LocomotionState.LANDING) return 0.08f;
        if (from == LocomotionState.WALK && to == LocomotionState.SPRINT) return 0.16f;
        if (from == LocomotionState.SPRINT && to == LocomotionState.WALK) return 0.20f;
        if (to == LocomotionState.IDLE) return 0.22f;
        return 0.18f;
    }

    public void transitionTo(LocomotionState newState, float duration) {
        if (newState == currentState) return;

        // Snapshot current blended pose as source of new transition
        sourcePose.set(finalBlendedPose);
        previousState = currentState;
        currentState = newState;
        transitionDuration = Math.max(0.01f, duration);
        transitionTime = 0f;
        isTransitioning = true;
        stateTime = 0f;
    }

    /**
     * Informs state machine that the character hit the ground with downward velocity.
     * Triggers dynamic landing impact compression.
     */
    public void notifyLanded(float verticalVel) {
        landingImpactVelocity = Math.abs(verticalVel);
        landingTimer = LANDING_DURATION;
        transitionTo(LocomotionState.LANDING, 0.06f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Articulated Joint Kinematics Evaluator
    // ─────────────────────────────────────────────────────────────────────────
    private void evaluatePose(LocomotionState state, CharacterPose out, float time,
                              float phase, float speed, Vector3 rootPos, float headingDegrees,
                              boolean isSprinting, boolean isCrouching, boolean isAttacking,
                              int attackCombo, float attackProgress, boolean isDodging,
                              float dodgeProgress, float staggerTimer) {

        out.reset();
        float swing = MathUtils.sin(phase);
        float cosSwing = MathUtils.cos(phase);

        switch (state) {
            case IDLE: {
                // Natural breathing oscillation
                float breatheOsc = MathUtils.sin(time * 2.2f);
                out.breathe = breatheOsc * 0.007f;
                out.torsoPitch = 1.8f + breatheOsc * 0.4f;

                // Subtle organic weight shift between left and right foot (0.45 Hz)
                float weightShift = MathUtils.sin(time * 0.9f);
                out.pelvisRoll = weightShift * 1.5f;
                out.pelvisTwist = weightShift * 2.0f;
                out.thighRoll[0] = -weightShift * 1.2f;
                out.thighRoll[1] = -weightShift * 1.2f;

                // Idle fidgets
                if (currentFidget == 1) {
                    // Glance across promenade (looking left then right)
                    float glance = MathUtils.sin(time * 1.4f);
                    out.headYaw = glance * 24f;
                    out.neckYaw = glance * 12f;
                } else if (currentFidget == 2) {
                    // Adjust backpack shoulder strap with right hand
                    float strapT = (float) Math.sin((time % 3.8f) / 3.8f * MathUtils.PI);
                    out.upperArmPitch[1] = -45f * strapT;
                    out.upperArmRoll[1] = -18f * strapT;
                    out.foreArmPitch[1] = -75f * strapT;
                }
                break;
            }

            case START_WALK: {
                out.torsoPitch = 5.5f;
                out.pelvisHeight = 0.86f;
                float t = Math.min(1.0f, stateTime / 0.22f);
                out.thighPitch[0] = MathUtils.lerp(0f, -22f, t);
                out.kneePitch[0] = MathUtils.lerp(0f, 25f, t);
                out.upperArmPitch[0] = MathUtils.lerp(3f, 18f, t);
                out.upperArmPitch[1] = MathUtils.lerp(3f, -18f, t);
                break;
            }

            case WALK: {
                // Pelvis bounce (2x step frequency)
                out.bobOffset = Math.abs(swing) * 0.022f;
                out.pelvisHeight = 0.87f;

                // Pelvic sway & spinal twist
                out.pelvisRoll = swing * 2.5f + currentBankingRoll * 0.5f;
                out.pelvisTwist = swing * 3.8f;
                out.torsoTwist = -swing * 4.2f;
                out.torsoPitch = 4.2f;
                out.torsoRoll = currentBankingRoll;

                // Head look-ahead into turn
                out.headYaw = MathUtils.clamp(-angularVelocity * 0.08f, -15f, 15f);

                // Legs: hip flexion, knee swing, foot flex
                for (int side = 0; side < 2; side++) {
                    float s = (side == 0) ? swing : -swing;
                    out.thighPitch[side] = s * 32f;
                    out.thighRoll[side] = (side == 0 ? -1f : 1f) * 1.5f;

                    // Knee bends only during rearward swing phase
                    if (s < 0f) {
                        out.kneePitch[side] = -s * 42f;
                        out.footPitch[side] = -s * 15f; // Toe push-off
                    } else {
                        out.kneePitch[side] = 4f;
                        out.footPitch[side] = -s * 12f; // Heel strike dorsiflexion
                    }

                    // Arm swing with elbow lag
                    float armDir = (side == 0) ? -swing : swing;
                    out.upperArmPitch[side] = 3f + armDir * 28f;
                    out.upperArmRoll[side] = (side == 0 ? 3.5f : -3.5f);
                    out.foreArmPitch[side] = -18f - Math.max(0f, armDir) * 16f;
                }
                break;
            }

            case START_SPRINT:
            case SPRINT: {
                // High athletic bounce
                out.bobOffset = Math.abs(swing) * 0.038f;
                out.pelvisHeight = 0.85f;

                // Strong forward torso drive and counter-twist
                out.torsoPitch = 14.5f;
                out.torsoTwist = -swing * 8.5f;
                out.pelvisTwist = swing * 6.5f;
                out.pelvisRoll = swing * 3.5f + currentBankingRoll * 0.7f;
                out.torsoRoll = currentBankingRoll;
                out.headPitch = -4.0f; // Head stays level focused forward
                out.headYaw = MathUtils.clamp(-angularVelocity * 0.12f, -18f, 18f);

                // High knee lift and athletic stride
                for (int side = 0; side < 2; side++) {
                    float s = (side == 0) ? swing : -swing;
                    out.thighPitch[side] = s * 45f;
                    out.thighRoll[side] = (side == 0 ? -1f : 1f) * 2.2f;

                    if (s < 0f) {
                        out.kneePitch[side] = -s * 65f; // High knee lift
                        out.footPitch[side] = -s * 22f; // Strong toe push-off
                    } else {
                        out.kneePitch[side] = 8f;
                        out.footPitch[side] = -s * 16f;
                    }

                    // Pronounced arm pump
                    float armDir = (side == 0) ? -swing : swing;
                    out.upperArmPitch[side] = 6f + armDir * 52f;
                    out.upperArmRoll[side] = (side == 0 ? 5.5f : -5.5f);
                    out.foreArmPitch[side] = -48f - Math.max(0f, armDir) * 32f;
                }
                break;
            }

            case STOPPING: {
                // Inertial settle: back torso recovery
                float settleT = Math.min(1.0f, stateTime / 0.24f);
                float brakePitch = MathUtils.lerp(-4f, 1.8f, settleT);
                out.torsoPitch = brakePitch;
                out.pelvisHeight = MathUtils.lerp(0.84f, 0.87f, settleT);
                out.thighPitch[0] = MathUtils.lerp(-12f, 0f, settleT);
                out.thighPitch[1] = MathUtils.lerp(12f, 0f, settleT);
                out.kneePitch[1] = MathUtils.lerp(18f, 0f, settleT);
                break;
            }

            case TURN_IN_PLACE: {
                // Turning on the spot
                float turnSwing = MathUtils.sin(stateTime * 10f);
                out.pelvisHeight = 0.86f;
                out.torsoTwist = MathUtils.clamp(angularVelocity * 0.15f, -25f, 25f);
                out.headYaw = MathUtils.clamp(angularVelocity * 0.22f, -32f, 32f);
                out.thighPitch[0] = turnSwing * 12f;
                out.thighPitch[1] = -turnSwing * 12f;
                out.kneePitch[0] = Math.max(0f, -turnSwing * 18f);
                out.kneePitch[1] = Math.max(0f, turnSwing * 18f);
                break;
            }

            case AIRBORNE_JUMP: {
                // Ascent: tuck legs, arms out for flight balance
                out.pelvisHeight = 0.83f;
                out.torsoPitch = 8.0f;
                out.thighPitch[0] = -24f;
                out.thighPitch[1] = -20f;
                out.kneePitch[0] = 48f;
                out.kneePitch[1] = 42f;
                out.footPitch[0] = -15f;
                out.footPitch[1] = -15f;

                // Arms raised for aerodynamic balance
                out.upperArmPitch[0] = -38f;
                out.upperArmRoll[0] = 16f;
                out.foreArmPitch[0] = -42f;
                out.upperArmPitch[1] = -38f;
                out.upperArmRoll[1] = -16f;
                out.foreArmPitch[1] = -42f;
                break;
            }

            case AIRBORNE_FALL: {
                // Descent: legs extend downward ready to absorb contact
                out.pelvisHeight = 0.86f;
                out.torsoPitch = 3.5f;
                out.thighPitch[0] = -10f;
                out.thighPitch[1] = -8f;
                out.kneePitch[0] = 18f;
                out.kneePitch[1] = 16f;
                out.footPitch[0] = 12f;
                out.footPitch[1] = 12f;

                out.upperArmPitch[0] = -22f;
                out.upperArmRoll[0] = 14f;
                out.foreArmPitch[0] = -28f;
                out.upperArmPitch[1] = -22f;
                out.upperArmRoll[1] = -14f;
                out.foreArmPitch[1] = -28f;
                break;
            }

            case LANDING: {
                // Dynamic ground impact cushion
                float landT = MathUtils.clamp(1.0f - (landingTimer / LANDING_DURATION), 0f, 1f);
                float cushionCurve = (float) Math.sin(landT * MathUtils.PI);
                float maxDip = Math.min(0.18f, 0.06f + landingImpactVelocity * 0.012f);

                out.pelvisHeight = 0.87f - cushionCurve * maxDip;
                out.torsoPitch = 4f + cushionCurve * 14f;
                out.thighPitch[0] = -cushionCurve * 22f;
                out.thighPitch[1] = -cushionCurve * 22f;
                out.kneePitch[0] = cushionCurve * 38f;
                out.kneePitch[1] = cushionCurve * 38f;

                // Arms lower to cushion momentum
                out.upperArmPitch[0] = 12f + cushionCurve * 18f;
                out.upperArmPitch[1] = 12f + cushionCurve * 18f;
                out.foreArmPitch[0] = -25f - cushionCurve * 30f;
                out.foreArmPitch[1] = -25f - cushionCurve * 30f;
                break;
            }

            case CROUCH_IDLE: {
                out.pelvisHeight = 0.46f;
                out.torsoPitch = 32f;
                out.headPitch = -20f;
                out.thighPitch[0] = -48f;
                out.thighPitch[1] = -48f;
                out.kneePitch[0] = 78f;
                out.kneePitch[1] = 78f;
                out.upperArmPitch[0] = 15f;
                out.upperArmPitch[1] = 15f;
                out.foreArmPitch[0] = -45f;
                out.foreArmPitch[1] = -45f;
                break;
            }

            case CROUCH_WALK: {
                out.pelvisHeight = 0.46f;
                out.torsoPitch = 34f;
                out.headPitch = -18f;
                out.bobOffset = Math.abs(swing) * 0.018f;

                for (int side = 0; side < 2; side++) {
                    float s = (side == 0) ? swing : -swing;
                    out.thighPitch[side] = -48f + s * 18f;
                    out.kneePitch[side] = 78f - Math.max(0f, s) * 20f;
                }
                break;
            }

            case WADING: {
                // High leg lift through pond water
                out.bobOffset = Math.abs(swing) * 0.028f;
                out.pelvisHeight = 0.86f;
                out.torsoPitch = 8.5f;

                for (int side = 0; side < 2; side++) {
                    float s = (side == 0) ? swing : -swing;
                    out.thighPitch[side] = s * 38f;
                    if (s < 0f) {
                        out.kneePitch[side] = -s * 55f; // High knee lift above water
                    } else {
                        out.kneePitch[side] = 8f;
                    }
                    float armDir = (side == 0) ? -swing : swing;
                    out.upperArmPitch[side] = armDir * 32f;
                    out.upperArmRoll[side] = (side == 0 ? 8f : -8f); // Spread arms for balance
                    out.foreArmPitch[side] = -24f;
                }
                break;
            }

            case WALL_CLIMB: {
                // Scaling campus walls
                float climbPhase = time * 7.5f;
                float cSwing = MathUtils.sin(climbPhase);
                out.pelvisHeight = 0.85f;
                out.torsoPitch = 12f;

                out.upperArmPitch[0] = -120f + cSwing * 28f;
                out.upperArmPitch[1] = -120f - cSwing * 28f;
                out.foreArmPitch[0] = -65f;
                out.foreArmPitch[1] = -65f;

                out.thighPitch[0] = -35f - cSwing * 22f;
                out.thighPitch[1] = -35f + cSwing * 22f;
                out.kneePitch[0] = 55f + cSwing * 25f;
                out.kneePitch[1] = 55f - cSwing * 25f;
                break;
            }

            case SITTING_WATER: {
                // Relaxed posture on ghat steps
                out.pelvisHeight = 0.38f;
                out.torsoPitch = -8f;
                out.thighPitch[0] = -85f;
                out.thighPitch[1] = -85f;

                // Dangle shins into water with gentle kicking
                float kick0 = MathUtils.sin(time * 2.8f) * 12f;
                float kick1 = MathUtils.sin(time * 2.8f + MathUtils.PI) * 12f;
                out.kneePitch[0] = 82f + kick0;
                out.kneePitch[1] = 82f + kick1;
                out.footPitch[0] = -10f;
                out.footPitch[1] = -10f;

                out.upperArmPitch[0] = 15f;
                out.upperArmRoll[0] = 8f;
                out.foreArmPitch[0] = 35f;
                out.upperArmPitch[1] = 15f;
                out.upperArmRoll[1] = -8f;
                out.foreArmPitch[1] = 35f;
                break;
            }

            case COMBAT_GUARD: {
                out.pelvisHeight = 0.84f;
                out.torsoPitch = 8f;
                out.thighPitch[0] = -14f;
                out.thighPitch[1] = 12f;
                out.kneePitch[0] = 20f;
                out.kneePitch[1] = 18f;

                // Guard parry hands protecting jaw
                out.upperArmPitch[0] = -82f;
                out.foreArmPitch[0] = -95f;
                out.upperArmPitch[1] = -82f;
                out.foreArmPitch[1] = -95f;
                break;
            }

            case COMBAT_DODGE: {
                float slipCurve = MathUtils.sin(dodgeProgress * MathUtils.PI);
                out.pelvisHeight = MathUtils.lerp(0.87f, 0.54f, slipCurve);
                out.torsoPitch = 24f * slipCurve;
                out.headPitch = -15f * slipCurve;
                out.torsoTwist = 20f * MathUtils.sin(dodgeProgress * MathUtils.PI * 2f);
                out.upperArmPitch[0] = -68f * slipCurve;
                out.foreArmPitch[0] = -92f * slipCurve;
                out.upperArmPitch[1] = -68f * slipCurve;
                out.foreArmPitch[1] = -92f * slipCurve;
                break;
            }

            case COMBAT_STAGGER: {
                float stRatio = Math.min(1.0f, staggerTimer / 0.28f);
                out.torsoPitch = -24f * stRatio;
                out.headPitch = -22f * stRatio;
                out.upperArmPitch[0] = -35f * stRatio;
                out.upperArmPitch[1] = -35f * stRatio;
                break;
            }

            case COMBAT_ATTACK: {
                float t = MathUtils.clamp(attackProgress, 0f, 1f);
                float strikePower = MathUtils.sin(t * MathUtils.PI);

                if (attackCombo == 1) {
                    float jabPower = (float) Math.sin(Math.pow(t, 0.75) * MathUtils.PI);
                    out.torsoTwist = 24f * jabPower;
                    out.torsoPitch = 8f * jabPower;
                    out.upperArmPitch[0] = -96f * jabPower;
                    out.foreArmPitch[0] = -8f * jabPower;
                    out.upperArmPitch[1] = -68f;
                    out.foreArmPitch[1] = -86f;
                } else if (attackCombo == 2) {
                    float crossPower = (float) Math.sin(Math.pow(t, 0.8) * MathUtils.PI);
                    out.torsoTwist = -40f * crossPower;
                    out.torsoPitch = 14f * crossPower;
                    out.upperArmPitch[1] = -98f * crossPower;
                    out.foreArmPitch[1] = -6f * crossPower;
                    out.upperArmPitch[0] = -70f;
                    out.foreArmPitch[0] = -88f;
                } else if (attackCombo == 3) {
                    out.torsoPitch = -20f * strikePower;
                    out.torsoTwist = -35f * strikePower;
                    out.thighPitch[1] = -105f * strikePower;
                    out.kneePitch[1] = 15f * strikePower;
                    out.upperArmPitch[0] = -55f * strikePower;
                    out.upperArmPitch[1] = -48f * strikePower;
                } else if (attackCombo == 4) {
                    out.torsoPitch = -10f * strikePower;
                    out.torsoTwist = 72f * strikePower;
                    out.thighPitch[1] = -88f * strikePower;
                    out.kneePitch[1] = 48f * strikePower;
                    out.upperArmPitch[0] = -62f * strikePower;
                    out.upperArmPitch[1] = -38f * strikePower;
                } else if (attackCombo == 5) {
                    out.pelvisHeight = 0.32f;
                    out.torsoPitch = 38f * strikePower;
                    out.torsoTwist = -75f * strikePower;
                    out.thighPitch[1] = -78f * strikePower;
                    out.kneePitch[1] = 20f * strikePower;
                    out.upperArmPitch[0] = 28f * strikePower;
                    out.foreArmPitch[0] = -45f * strikePower;
                    out.upperArmPitch[1] = -40f * strikePower;
                    out.foreArmPitch[1] = -70f * strikePower;
                }
                break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lightweight Foot IK & Terrain Slope Adaptation
    // ─────────────────────────────────────────────────────────────────────────
    private void applyTerrainFootPlacement(Vector3 rootPos, float headingDegrees, CharacterPose pose) {
        if (currentState == LocomotionState.AIRBORNE_JUMP ||
            currentState == LocomotionState.AIRBORNE_FALL ||
            currentState == LocomotionState.SITTING_WATER ||
            currentState == LocomotionState.WALL_CLIMB) {
            pose.footGroundOffset[0] = 0f;
            pose.footGroundOffset[1] = 0f;
            return;
        }

        float headingRad = headingDegrees * MathUtils.degreesToRadians;
        float rightX = MathUtils.cos(headingRad);
        float rightZ = -MathUtils.sin(headingRad);
        float legSpread = 0.12f;

        // Sample ground elevation under left foot and right foot
        tempFootL.set(rootPos.x - rightX * legSpread, 0f, rootPos.z - rightZ * legSpread);
        tempFootR.set(rootPos.x + rightX * legSpread, 0f, rootPos.z + rightZ * legSpread);

        float groundCenter = TerrainSurface.getGroundHeight(rootPos.x, rootPos.z);
        float groundL = TerrainSurface.getGroundHeight(tempFootL.x, tempFootL.z);
        float groundR = TerrainSurface.getGroundHeight(tempFootR.x, tempFootR.z);

        float diffL = MathUtils.clamp(groundL - groundCenter, -0.25f, 0.25f);
        float diffR = MathUtils.clamp(groundR - groundCenter, -0.25f, 0.25f);

        pose.footGroundOffset[0] = diffL;
        pose.footGroundOffset[1] = diffR;

        // Adapt knee bend and pelvis roll to uneven slope
        if (Math.abs(diffL - diffR) > 0.02f) {
            float slopeAngle = MathUtils.atan2(diffR - diffL, legSpread * 2.0f) * MathUtils.radiansToDegrees;
            pose.pelvisRoll += MathUtils.clamp(slopeAngle * 0.5f, -6f, 6f);

            if (diffL > diffR) {
                pose.kneePitch[0] += diffL * 35f;
            } else {
                pose.kneePitch[1] += diffR * 35f;
            }
        }
    }

    public LocomotionState getCurrentState() {
        return currentState;
    }

    public LocomotionState getPreviousState() {
        return previousState;
    }

    public boolean isTransitioning() {
        return isTransitioning;
    }

    public float getTransitionProgress() {
        return isTransitioning ? Math.min(1.0f, transitionTime / transitionDuration) : 1.0f;
    }

    public CharacterPose getBlendedPose() {
        return finalBlendedPose;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public float getStridePhase() {
        return stridePhase;
    }
}
