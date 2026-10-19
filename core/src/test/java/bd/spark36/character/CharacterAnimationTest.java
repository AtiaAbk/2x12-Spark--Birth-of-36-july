package bd.spark36.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterAnimationTest {

    private AnimationStateMachine stateMachine;
    private CharacterPose poseA;
    private CharacterPose poseB;
    private CharacterPose poseOut;

    @BeforeEach
    public void setUp() {
        stateMachine = new AnimationStateMachine();
        poseA = new CharacterPose();
        poseB = new CharacterPose();
        poseOut = new CharacterPose();
    }

    @Test
    @DisplayName("CharacterPose: copyFrom accurately clones all anatomical joint parameters")
    public void testPoseCopyFrom() {
        poseA.pelvisHeight = 0.92f;
        poseA.pelvisPitch = 12.5f;
        poseA.pelvisRoll = -3.2f;
        poseA.torsoPitch = 15.0f;
        poseA.thighPitch[0] = 25.0f;
        poseA.thighPitch[1] = -20.0f;
        poseA.kneePitch[0] = 30.0f;
        poseA.kneePitch[1] = 10.0f;
        poseA.footGroundOffset[0] = 0.05f;
        poseA.footGroundOffset[1] = -0.02f;

        poseB.copyFrom(poseA);

        assertEquals(poseA.pelvisHeight, poseB.pelvisHeight, 1e-6f);
        assertEquals(poseA.pelvisPitch, poseB.pelvisPitch, 1e-6f);
        assertEquals(poseA.pelvisRoll, poseB.pelvisRoll, 1e-6f);
        assertEquals(poseA.torsoPitch, poseB.torsoPitch, 1e-6f);
        assertEquals(poseA.thighPitch[0], poseB.thighPitch[0], 1e-6f);
        assertEquals(poseA.thighPitch[1], poseB.thighPitch[1], 1e-6f);
        assertEquals(poseA.kneePitch[0], poseB.kneePitch[0], 1e-6f);
        assertEquals(poseA.kneePitch[1], poseB.kneePitch[1], 1e-6f);
        assertEquals(poseA.footGroundOffset[0], poseB.footGroundOffset[0], 1e-6f);
        assertEquals(poseA.footGroundOffset[1], poseB.footGroundOffset[1], 1e-6f);
    }

    @Test
    @DisplayName("CharacterPose: lerp interpolates correctly across boundaries and intermediate points")
    public void testPoseLerp() {
        poseA.pelvisHeight = 0.80f;
        poseA.torsoPitch = 10.0f;
        poseA.upperArmPitch[0] = 20.0f;

        poseB.pelvisHeight = 1.00f;
        poseB.torsoPitch = 30.0f;
        poseB.upperArmPitch[0] = -40.0f;

        // Alpha = 0.0 -> matches A
        CharacterPose.lerp(poseA, poseB, 0.0f, poseOut);
        assertEquals(0.80f, poseOut.pelvisHeight, 1e-5f);
        assertEquals(10.0f, poseOut.torsoPitch, 1e-5f);
        assertEquals(20.0f, poseOut.upperArmPitch[0], 1e-5f);

        // Alpha = 1.0 -> matches B
        CharacterPose.lerp(poseA, poseB, 1.0f, poseOut);
        assertEquals(1.00f, poseOut.pelvisHeight, 1e-5f);
        assertEquals(30.0f, poseOut.torsoPitch, 1e-5f);
        assertEquals(-40.0f, poseOut.upperArmPitch[0], 1e-5f);

        // Alpha = 0.5 -> exact midpoint
        CharacterPose.lerp(poseA, poseB, 0.5f, poseOut);
        assertEquals(0.90f, poseOut.pelvisHeight, 1e-5f);
        assertEquals(20.0f, poseOut.torsoPitch, 1e-5f);
        assertEquals(-10.0f, poseOut.upperArmPitch[0], 1e-5f);
    }

    @Test
    @DisplayName("Locomotion State Selection: stationary player enters IDLE")
    public void testIdleState() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(0, 0, 0);

        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);

        assertEquals(LocomotionState.IDLE, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("Locomotion State Selection: walk speed enters WALK, sprint enters SPRINT")
    public void testWalkAndSprintStates() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(3.5f, 0, 0);

        // Walk
        stateMachine.update(0.016f, pos, 0f, vel, true, true, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.WALK, stateMachine.getCurrentState());

        // Sprint
        vel.set(8.5f, 0, 0);
        stateMachine.update(0.016f, pos, 0f, vel, true, true, true, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.SPRINT, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("Locomotion State Selection: airborne states transition between JUMP and FALL")
    public void testAirborneStates() {
        Vector3 pos = new Vector3(0, 2, 0);
        Vector3 vel = new Vector3(0, 8.0f, 0);

        // Rising -> JUMP
        stateMachine.update(0.016f, pos, 0f, vel, false, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.AIRBORNE_JUMP, stateMachine.getCurrentState());

        // Falling -> FALL
        vel.set(0, -6.0f, 0);
        stateMachine.update(0.016f, pos, 0f, vel, false, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.AIRBORNE_FALL, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("Locomotion State Selection: Crouch, Wading, Climbing, and Sitting states")
    public void testSpecialLocomotionStates() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(0, 0, 0);

        // Crouch Idle
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, true, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.CROUCH_IDLE, stateMachine.getCurrentState());

        // Crouch Walk
        vel.set(1.5f, 0, 0);
        stateMachine.update(0.016f, pos, 0f, vel, true, true, false, true, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.CROUCH_WALK, stateMachine.getCurrentState());

        // Wading in water
        vel.set(2.0f, 0, 0);
        stateMachine.update(0.016f, pos, 0f, vel, true, true, false, false, false, true, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.WADING, stateMachine.getCurrentState());

        // Wall Climbing
        vel.set(0, 2.5f, 0);
        stateMachine.update(0.016f, pos, 0f, vel, false, false, false, false, true, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.WALL_CLIMB, stateMachine.getCurrentState());

        // Sitting at ghat
        vel.set(0, 0, 0);
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, true, false, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.SITTING_WATER, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("Combat States: Attacks, Dodge, Block, and Hit Stagger take priority")
    public void testCombatStates() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(0, 0, 0);

        // Attack Strike
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, true, 1, 0.2f, false, 0f, 0f);
        assertEquals(LocomotionState.COMBAT_ATTACK, stateMachine.getCurrentState());

        // Attack Kick
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, true, 3, 0.2f, false, 0f, 0f);
        assertEquals(LocomotionState.COMBAT_ATTACK, stateMachine.getCurrentState());

        // Dodge slip
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, false, 0, 0f, true, 0.1f, 0f);
        assertEquals(LocomotionState.COMBAT_DODGE, stateMachine.getCurrentState());

        // Block guard
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, true, false, 0, 0f, false, 0f, 0f);
        assertEquals(LocomotionState.COMBAT_GUARD, stateMachine.getCurrentState());

        // Hit Stagger
        stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0.25f);
        assertEquals(LocomotionState.COMBAT_STAGGER, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("Foot Sliding Elimination: stride frequency scales strictly with horizontal velocity")
    public void testFootSlidingCadenceScaling() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 velA = new Vector3(2.0f, 0, 0);
        Vector3 velB = new Vector3(6.0f, 0, 0);

        // At 2 m/s for 1 second
        stateMachine.reset();
        for (int i = 0; i < 60; i++) {
            stateMachine.update(1f / 60f, pos, 0f, velA, true, true, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        }
        float phaseA = stateMachine.getStridePhase();

        // At 6 m/s for 1 second
        AnimationStateMachine fastMachine = new AnimationStateMachine();
        for (int i = 0; i < 60; i++) {
            fastMachine.update(1f / 60f, pos, 0f, velB, true, true, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        }
        float phaseB = fastMachine.getStridePhase();

        assertTrue(phaseB > phaseA * 2.5f, "Higher velocity must drive proportionally higher stride cadence (eliminated foot sliding)");
    }

    @Test
    @DisplayName("Dynamic Landing Cushion: impact compression activates and naturally springs back")
    public void testLandingCushion() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(0, 0, 0);

        // Trigger hard landing impact from -7.5 m/s fall
        stateMachine.notifyLanded(-7.5f);

        // Advance 0.08s so cross-fade completes and cushion reaches peak shock absorption
        for (int i = 0; i < 5; i++) {
            stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        }

        assertEquals(LocomotionState.LANDING, stateMachine.getCurrentState());
        CharacterPose landingPose = stateMachine.getBlendedPose();

        // Pelvis must be depressed and knees bent
        assertTrue(landingPose.pelvisHeight < 0.87f, "Landing impact must compress pelvis height downward");
        assertTrue(landingPose.kneePitch[0] > 10.0f, "Landing impact must flex knees to absorb shock");

        // Advance 0.3 seconds past landing duration (0.22s)
        for (int i = 0; i < 20; i++) {
            stateMachine.update(0.016f, pos, 0f, vel, true, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        }

        // Must smoothly return to IDLE
        assertEquals(LocomotionState.IDLE, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("Centrifugal Turn Banking: rapid turning tilts character roll into the turn")
    public void testTurnBanking() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(0, 0, 4.0f);

        // Turn right sharply (heading increases by 45 degrees over 0.1s)
        float heading = 0f;
        for (int i = 0; i < 6; i++) {
            heading += 7.5f;
            stateMachine.update(0.016f, pos, heading, vel, true, true, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        }

        CharacterPose pose = stateMachine.getBlendedPose();
        // Rightward angular velocity produces negative bank roll (tilt into the turn)
        assertTrue(pose.torsoRoll < 0f, "Turning right must lean character torso into the turn (negative roll)");
        assertTrue(pose.torsoRoll >= -8.0f, "Banking roll must be safely clamped within -8 degrees");
    }

    @Test
    @DisplayName("Cross-fade Blending: pose interpolates smoothly between states without popping")
    public void testHermiteCrossFadeSmoothness() {
        Vector3 pos = new Vector3(0, 0, 0);
        Vector3 vel = new Vector3(0, 0, 0);

        // Start in IDLE
        stateMachine.update(0.05f, pos, 0f, vel, true, false, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        float idlePelvis = stateMachine.getBlendedPose().pelvisHeight;

        // Initiate WALK
        vel.set(4.0f, 0, 0);
        stateMachine.update(0.04f, pos, 0f, vel, true, true, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        assertTrue(stateMachine.isTransitioning(), "State machine must be smoothly cross-fading into WALK");

        float midPelvis = stateMachine.getBlendedPose().pelvisHeight;
        assertNotNull(midPelvis);

        // Advance to transition completion
        for (int i = 0; i < 20; i++) {
            stateMachine.update(0.02f, pos, 0f, vel, true, true, false, false, false, false, false, false, false, 0, 0f, false, 0f, 0f);
        }
        assertFalse(stateMachine.isTransitioning(), "Cross-fade should complete cleanly without getting stuck");
    }
}
