package bd.spark36.character;

import com.badlogic.gdx.math.MathUtils;

/**
 * Lightweight, allocation-free container for an articulated character pose.
 * Holds continuous joint rotation angles and translation offsets evaluated by
 * the animation state machine, supporting smooth Hermite/linear blending between states.
 */
public class CharacterPose {

    // Root and Pelvis
    public float bobOffset = 0f;
    public float pelvisHeight = 0.87f;
    public float pelvisPitch = 0f;
    public float pelvisTwist = 0f;
    public float pelvisRoll = 0f;

    // Torso and Spine
    public float torsoPitch = 0f;
    public float torsoTwist = 0f;
    public float torsoRoll = 0f;
    public float breathe = 0f;

    // Neck and Head
    public float neckPitch = 0f;
    public float neckYaw = 0f;
    public float headPitch = 0f;
    public float headYaw = 0f;
    public float headRoll = 0f;

    // Legs (0 = Left, 1 = Right)
    public final float[] thighPitch = new float[2];
    public final float[] thighRoll = new float[2];
    public final float[] thighYaw = new float[2];
    public final float[] kneePitch = new float[2];
    public final float[] footPitch = new float[2];
    public final float[] footRoll = new float[2];
    public final float[] footGroundOffset = new float[2]; // Ground adaptation (IK)

    // Arms (0 = Left, 1 = Right)
    public final float[] upperArmPitch = new float[2];
    public final float[] upperArmRoll = new float[2];
    public final float[] upperArmYaw = new float[2];
    public final float[] foreArmPitch = new float[2];
    public final float[] foreArmYaw = new float[2];

    public CharacterPose() {
        reset();
    }

    public void reset() {
        bobOffset = 0f;
        pelvisHeight = 0.87f;
        pelvisPitch = 0f;
        pelvisTwist = 0f;
        pelvisRoll = 0f;

        torsoPitch = 0f;
        torsoTwist = 0f;
        torsoRoll = 0f;
        breathe = 0f;

        neckPitch = 0f;
        neckYaw = 0f;
        headPitch = 0f;
        headYaw = 0f;
        headRoll = 0f;

        for (int i = 0; i < 2; i++) {
            thighPitch[i] = 0f;
            thighRoll[i] = 0f;
            thighYaw[i] = 0f;
            kneePitch[i] = 0f;
            footPitch[i] = 0f;
            footRoll[i] = 0f;
            footGroundOffset[i] = 0f;

            upperArmPitch[i] = 3f;
            upperArmRoll[i] = i == 0 ? 3.5f : -3.5f;
            upperArmYaw[i] = i == 0 ? -4.0f : 4.0f;
            foreArmPitch[i] = -8f;
            foreArmYaw[i] = i == 0 ? -8f : 8f;
        }
    }

    public void copyFrom(CharacterPose other) {
        set(other);
    }

    public static void lerp(CharacterPose a, CharacterPose b, float t, CharacterPose out) {
        if (out != null) {
            out.lerp(a, b, t);
        }
    }

    public void set(CharacterPose other) {
        if (other == null) return;
        this.bobOffset = other.bobOffset;
        this.pelvisHeight = other.pelvisHeight;
        this.pelvisPitch = other.pelvisPitch;
        this.pelvisTwist = other.pelvisTwist;
        this.pelvisRoll = other.pelvisRoll;

        this.torsoPitch = other.torsoPitch;
        this.torsoTwist = other.torsoTwist;
        this.torsoRoll = other.torsoRoll;
        this.breathe = other.breathe;

        this.neckPitch = other.neckPitch;
        this.neckYaw = other.neckYaw;
        this.headPitch = other.headPitch;
        this.headYaw = other.headYaw;
        this.headRoll = other.headRoll;

        for (int i = 0; i < 2; i++) {
            this.thighPitch[i] = other.thighPitch[i];
            this.thighRoll[i] = other.thighRoll[i];
            this.thighYaw[i] = other.thighYaw[i];
            this.kneePitch[i] = other.kneePitch[i];
            this.footPitch[i] = other.footPitch[i];
            this.footRoll[i] = other.footRoll[i];
            this.footGroundOffset[i] = other.footGroundOffset[i];

            this.upperArmPitch[i] = other.upperArmPitch[i];
            this.upperArmRoll[i] = other.upperArmRoll[i];
            this.upperArmYaw[i] = other.upperArmYaw[i];
            this.foreArmPitch[i] = other.foreArmPitch[i];
            this.foreArmYaw[i] = other.foreArmYaw[i];
        }
    }

    /**
     * Blends pose {@code a} towards pose {@code b} with weight {@code t} in [0, 1].
     * Stores result directly into {@code this} with zero allocation.
     */
    public void lerp(CharacterPose a, CharacterPose b, float t) {
        float alpha = MathUtils.clamp(t, 0f, 1f);

        this.bobOffset = MathUtils.lerp(a.bobOffset, b.bobOffset, alpha);
        this.pelvisHeight = MathUtils.lerp(a.pelvisHeight, b.pelvisHeight, alpha);
        this.pelvisPitch = MathUtils.lerp(a.pelvisPitch, b.pelvisPitch, alpha);
        this.pelvisTwist = MathUtils.lerp(a.pelvisTwist, b.pelvisTwist, alpha);
        this.pelvisRoll = MathUtils.lerp(a.pelvisRoll, b.pelvisRoll, alpha);

        this.torsoPitch = MathUtils.lerp(a.torsoPitch, b.torsoPitch, alpha);
        this.torsoTwist = MathUtils.lerp(a.torsoTwist, b.torsoTwist, alpha);
        this.torsoRoll = MathUtils.lerp(a.torsoRoll, b.torsoRoll, alpha);
        this.breathe = MathUtils.lerp(a.breathe, b.breathe, alpha);

        this.neckPitch = MathUtils.lerp(a.neckPitch, b.neckPitch, alpha);
        this.neckYaw = MathUtils.lerp(a.neckYaw, b.neckYaw, alpha);
        this.headPitch = MathUtils.lerp(a.headPitch, b.headPitch, alpha);
        this.headYaw = MathUtils.lerp(a.headYaw, b.headYaw, alpha);
        this.headRoll = MathUtils.lerp(a.headRoll, b.headRoll, alpha);

        for (int i = 0; i < 2; i++) {
            this.thighPitch[i] = MathUtils.lerp(a.thighPitch[i], b.thighPitch[i], alpha);
            this.thighRoll[i] = MathUtils.lerp(a.thighRoll[i], b.thighRoll[i], alpha);
            this.thighYaw[i] = MathUtils.lerp(a.thighYaw[i], b.thighYaw[i], alpha);
            this.kneePitch[i] = MathUtils.lerp(a.kneePitch[i], b.kneePitch[i], alpha);
            this.footPitch[i] = MathUtils.lerp(a.footPitch[i], b.footPitch[i], alpha);
            this.footRoll[i] = MathUtils.lerp(a.footRoll[i], b.footRoll[i], alpha);
            this.footGroundOffset[i] = MathUtils.lerp(a.footGroundOffset[i], b.footGroundOffset[i], alpha);

            this.upperArmPitch[i] = MathUtils.lerp(a.upperArmPitch[i], b.upperArmPitch[i], alpha);
            this.upperArmRoll[i] = MathUtils.lerp(a.upperArmRoll[i], b.upperArmRoll[i], alpha);
            this.upperArmYaw[i] = MathUtils.lerp(a.upperArmYaw[i], b.upperArmYaw[i], alpha);
            this.foreArmPitch[i] = MathUtils.lerp(a.foreArmPitch[i], b.foreArmPitch[i], alpha);
            this.foreArmYaw[i] = MathUtils.lerp(a.foreArmYaw[i], b.foreArmYaw[i], alpha);
        }
    }
}
