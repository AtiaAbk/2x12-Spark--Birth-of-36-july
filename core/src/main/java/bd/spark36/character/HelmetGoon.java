package bd.spark36.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.world.TextureFactory;

/**
 * The Antagonist Boss for Level 1: A helmet-wearing, lathi-wielding student thug/goon
 * ("helmet pora lathi hate ekjon gunda type student") sent to suppress the student movement.
 * Features:
 * - 3D Riot/Motorcycle Helmet on head with dark visor and danger stripe.
 * - Heavy bamboo/hardwood Lathi (stick/baton) in right hand with brass ferrules.
 * - Full AI state machine: STANDOFF, CHALLENGING, APPROACHING, HIGH SWING, OVERHEAD SLAM, THRUST, STAGGERED, KNOCKED_OUT.
 * - Real martial arts simulation: High swings can be evaded by crouching/ducking (Bosepora),
 *   overhead slams can be blocked, and bare-handed punches and kicks damage and stagger him.
 */
public class HelmetGoon implements Disposable {

    public enum State {
        STANDOFF,       // Waiting at post, challenges when player nears
        CHALLENGING,    // Warning dialogue before combat
        APPROACHING,    // Advancing on player with lathi raised
        HIGH_SWING,     // Horizontal lathi swing (dodgeable with duck/crouch)
        OVERHEAD_SLAM,  // Heavy vertical strike (blockable)
        THRUST,         // Quick poke attack
        STAGGERED,      // Reeling from player hit
        KNOCKED_OUT     // Defeated / collapsed on ground
    }

    private State state = State.STANDOFF;
    private final Vector3 position = new Vector3(0f, 0f, 6.0f); // Central avenue facing the player
    private float heading = 0f; // degrees
    private float health = 100f;
    private final float maxHealth = 100f;

    // Animation & Combat Timers
    private float animTime = 0f;
    private float stateTimer = 0f;
    private float attackCooldown = 0f;
    private float walkCycle = 0f;
    private boolean isAttacking = false;
    private float attackProgress = 0f;
    private int currentAttackType = 0; // 0=none, 1=high swing, 2=overhead slam, 3=thrust
    private boolean hitDealtThisAttack = false;

    // Stagger & Hit FX
    private float staggerTimer = 0f;
    private float hitFlashTimer = 0f;
    private String combatPopupText = "";
    private float combatPopupTimer = 0f;
    private Color combatPopupColor = Color.WHITE;

    // 3D Models and Render Hierarchy
    private final ModelBuilder mb = new ModelBuilder();
    private final Array<Model> models = new Array<>();
    private final long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

    // Body parts
    private Model torsoModel;
    private Model headModel;
    private Model upperArmModel;
    private Model foreArmModel;
    private Model legModel;
    private Model bootModel;

    // Weapon & Armor
    private Model helmetShellModel;
    private Model helmetVisorModel;
    private Model helmetStripeModel;
    private Model lathiStickModel;
    private Model lathiTipModel;

    // ModelInstances
    private ModelInstance torsoInst;
    private ModelInstance headInst;
    private ModelInstance armUpperLInst, armUpperRInst;
    private ModelInstance armForeLInst, armForeRInst;
    private ModelInstance legLInst, legRInst;
    private ModelInstance bootLInst, bootRInst;
    private ModelInstance helmetShellInst, helmetVisorInst, helmetStripeInst;
    private ModelInstance lathiStickInst, lathiTipTopInst, lathiTipBottomInst;

    // Transform matrices for skeletal hierarchy
    private final Matrix4 rootTransform = new Matrix4();
    private final Matrix4 torsoTransform = new Matrix4();
    private final Matrix4 headTransform = new Matrix4();
    private final Matrix4 rightUpperArmTransform = new Matrix4();
    private final Matrix4 rightForeArmTransform = new Matrix4();
    private final Matrix4 leftUpperArmTransform = new Matrix4();
    private final Matrix4 leftForeArmTransform = new Matrix4();
    private final Matrix4 lathiTransform = new Matrix4();
    private final Matrix4 legLTransform = new Matrix4();
    private final Matrix4 legRTransform = new Matrix4();

    public HelmetGoon(TextureFactory textures) {
        buildModels(textures);
    }

    private void buildModels(TextureFactory textures) {
        // Material: Thug dark leather/tactical jacket (charcoal with slight gloss)
        Material jacketMat = new Material(ColorAttribute.createDiffuse(new Color(0.18f, 0.20f, 0.24f, 1f)));
        Material skinMat = new Material(ColorAttribute.createDiffuse(new Color(0.74f, 0.54f, 0.42f, 1f)));
        Material pantsMat = new Material(ColorAttribute.createDiffuse(new Color(0.14f, 0.16f, 0.20f, 1f)));
        Material bootMat = new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.10f, 0.11f, 1f)));

        // Helmet Materials: Protective riot helmet with danger yellow stripe and dark smoked visor
        Material helmetMat = new Material(ColorAttribute.createDiffuse(new Color(0.12f, 0.14f, 0.18f, 1f)));
        Material visorMat = new Material(ColorAttribute.createDiffuse(new Color(0.04f, 0.05f, 0.06f, 1f)));
        Material stripeMat = new Material(ColorAttribute.createDiffuse(new Color(0.96f, 0.78f, 0.10f, 1f)));

        // Lathi Materials: Seasoned hardwood/bamboo stick with polished brass ferrule caps
        Material lathiWoodMat = new Material(ColorAttribute.createDiffuse(new Color(0.48f, 0.32f, 0.16f, 1f)));
        Material brassMat = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.72f, 0.22f, 1f)));

        // Build Body Primitives
        torsoModel = mb.createBox(0.48f, 0.62f, 0.30f, jacketMat, attr);
        headModel = mb.createSphere(0.24f, 0.28f, 0.26f, 14, 12, skinMat, attr);
        upperArmModel = mb.createCapsule(0.08f, 0.36f, 10, jacketMat, attr);
        foreArmModel = mb.createCapsule(0.075f, 0.34f, 10, skinMat, attr);
        legModel = mb.createCapsule(0.10f, 0.74f, 10, pantsMat, attr);
        bootModel = mb.createBox(0.15f, 0.14f, 0.28f, bootMat, attr);

        // Build Helmet Primitives
        helmetShellModel = mb.createSphere(0.38f, 0.36f, 0.40f, 16, 14, helmetMat, attr);
        helmetVisorModel = mb.createBox(0.34f, 0.14f, 0.18f, visorMat, attr);
        helmetStripeModel = mb.createBox(0.06f, 0.38f, 0.41f, stripeMat, attr);

        // Build Lathi (Bamboo Stick) Primitives (1.65m total length, sturdy diameter)
        lathiStickModel = mb.createCylinder(0.052f, 1.60f, 0.052f, 12, lathiWoodMat, attr);
        lathiTipModel = mb.createCylinder(0.058f, 0.08f, 0.058f, 12, brassMat, attr);

        models.addAll(torsoModel, headModel, upperArmModel, foreArmModel, legModel, bootModel,
                      helmetShellModel, helmetVisorModel, helmetStripeModel, lathiStickModel, lathiTipModel);

        // Instantiate
        torsoInst = new ModelInstance(torsoModel);
        headInst = new ModelInstance(headModel);
        armUpperLInst = new ModelInstance(upperArmModel);
        armUpperRInst = new ModelInstance(upperArmModel);
        armForeLInst = new ModelInstance(foreArmModel);
        armForeRInst = new ModelInstance(foreArmModel);
        legLInst = new ModelInstance(legModel);
        legRInst = new ModelInstance(legModel);
        bootLInst = new ModelInstance(bootModel);
        bootRInst = new ModelInstance(bootModel);

        helmetShellInst = new ModelInstance(helmetShellModel);
        helmetVisorInst = new ModelInstance(helmetVisorModel);
        helmetStripeInst = new ModelInstance(helmetStripeModel);

        lathiStickInst = new ModelInstance(lathiStickModel);
        lathiTipTopInst = new ModelInstance(lathiTipModel);
        lathiTipBottomInst = new ModelInstance(lathiTipModel);
    }

    /**
     * AI update and combat simulation loop.
     */
    public void update(float delta, PlayerController player) {
        animTime += delta;
        stateTimer += delta;
        if (attackCooldown > 0f) attackCooldown -= delta;
        if (staggerTimer > 0f) staggerTimer -= delta;
        if (hitFlashTimer > 0f) hitFlashTimer -= delta;
        if (combatPopupTimer > 0f) combatPopupTimer -= delta;

        if (state == State.KNOCKED_OUT) {
            return;
        }

        Vector3 pPos = player.getPosition();
        float distToPlayer = position.dst(pPos);

        // Face player
        float dx = pPos.x - position.x;
        float dz = pPos.z - position.z;
        float targetHeading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
        if (state != State.KNOCKED_OUT) {
            float diff = (targetHeading - heading) % 360f;
            if (diff > 180f) diff -= 360f;
            if (diff < -180f) diff += 360f;
            heading += diff * Math.min(1f, delta * 6f);
        }

        // State Machine
        switch (state) {
            case STANDOFF:
                // If player approaches within 26m, initiate challenge
                if (distToPlayer <= 26f) {
                    state = State.CHALLENGING;
                    stateTimer = 0f;
                }
                break;

            case CHALLENGING:
                // Stood ground, raising lathi to intimidate
                if (stateTimer >= 2.5f || distToPlayer <= 10f || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F)) {
                    state = State.APPROACHING;
                    stateTimer = 0f;
                }
                break;

            case APPROACHING:
                if (distToPlayer > 2.2f) {
                    // Walk toward player
                    float speed = 2.4f;
                    walkCycle += delta * 7f;
                    position.x += MathUtils.sinDeg(heading) * speed * delta;
                    position.z += MathUtils.cosDeg(heading) * speed * delta;
                } else {
                    // In striking range: choose an attack!
                    if (attackCooldown <= 0f) {
                        chooseAttack();
                    }
                }
                break;

            case HIGH_SWING:
            case OVERHEAD_SLAM:
            case THRUST:
                updateAttack(delta, player, distToPlayer);
                break;

            case STAGGERED:
                if (staggerTimer <= 0f) {
                    state = State.APPROACHING;
                    attackCooldown = 0.4f;
                }
                break;
        }
    }

    private void chooseAttack() {
        hitDealtThisAttack = false;
        attackProgress = 0f;
        isAttacking = true;
        // Alternating attack patterns: 50% high horizontal swing, 35% overhead slam, 15% thrust
        float r = MathUtils.random();
        if (r < 0.50f) {
            state = State.HIGH_SWING;
            currentAttackType = 1;
        } else if (r < 0.85f) {
            state = State.OVERHEAD_SLAM;
            currentAttackType = 2;
        } else {
            state = State.THRUST;
            currentAttackType = 3;
        }
        stateTimer = 0f;
    }

    private void updateAttack(float delta, PlayerController player, float distToPlayer) {
        float attackSpeed = currentAttackType == 1 ? 2.0f : (currentAttackType == 2 ? 1.6f : 2.5f);
        attackProgress += delta * attackSpeed;

        // Strike impact moment occurs midway through swing (0.45 .. 0.65)
        if (!hitDealtThisAttack && attackProgress >= 0.48f && attackProgress <= 0.68f) {
            if (distToPlayer <= 2.6f) {
                // Determine whether player avoided or blocked the strike!
                if (currentAttackType == 1 && player.isCrouching()) {
                    // Player ducked under the horizontal swing!
                    showPopup("DODGED! / ফাঁকি দেওয়া হয়েছে!", Color.CYAN);
                    hitDealtThisAttack = true;
                } else if (player.isBlocking()) {
                    // Player blocked the strike!
                    float blockedDmg = (currentAttackType == 2) ? 6f : 3f;
                    player.takeDamage(blockedDmg);
                    showPopup("BLOCKED! / প্রতিহত!", Color.YELLOW);
                    hitDealtThisAttack = true;
                } else {
                    // Direct hit on player!
                    float dmg = (currentAttackType == 2) ? 24f : 16f;
                    player.takeDamage(dmg);
                    showPopup("HIT! -" + (int)dmg + " HP", Color.RED);
                    hitDealtThisAttack = true;
                }
            }
        }

        if (attackProgress >= 1.0f) {
            isAttacking = false;
            currentAttackType = 0;
            attackProgress = 0f;
            state = State.APPROACHING;
            attackCooldown = MathUtils.random(0.6f, 1.2f);
        }
    }

    /**
     * Called when the player successfully strikes the Goon with punches or kicks.
     */
    public boolean takeHitFromPlayer(float damage, boolean isKick) {
        if (state == State.KNOCKED_OUT) return false;

        health -= damage;
        hitFlashTimer = 0.25f;
        staggerTimer = isKick ? 0.65f : 0.40f;
        state = State.STAGGERED;
        isAttacking = false;
        currentAttackType = 0;

        // Push goon backward slightly from impact
        position.x -= MathUtils.sinDeg(heading) * (isKick ? 0.65f : 0.35f);
        position.z -= MathUtils.cosDeg(heading) * (isKick ? 0.65f : 0.35f);

        if (health <= 0f) {
            health = 0f;
            state = State.KNOCKED_OUT;
            showPopup("GUNDA DEFEATED / গুন্ডা পরাজিত!", Color.GREEN);
            return true; // Knockout!
        } else {
            String txt = isKick ? "HEAVY KICK! -" + (int)damage : "MARTIAL PUNCH! -" + (int)damage;
            showPopup(txt, isKick ? Color.ORANGE : Color.YELLOW);
            return false;
        }
    }

    private void showPopup(String text, Color color) {
        this.combatPopupText = text;
        this.combatPopupColor = color;
        this.combatPopupTimer = 1.4f;
    }

    /**
     * Renders the 3D Goon, Helmet, and Lathi into the ModelBatch.
     */
    public void render(ModelBatch batch, Environment env) {
        rootTransform.idt();
        rootTransform.translate(position.x, position.y, position.z);
        rootTransform.rotate(Vector3.Y, heading);

        if (state == State.KNOCKED_OUT) {
            // Defeated on ground: rotate root transform 90 deg back onto ground
            rootTransform.translate(0f, 0.22f, 0f).rotate(Vector3.X, -88f);
        }

        // Hit flash color effect
        Color flashTint = (hitFlashTimer > 0f) ? Color.RED : null;

        // 1. Torso
        torsoTransform.set(rootTransform).translate(0f, 1.15f, 0f);
        if (state == State.STAGGERED) {
            torsoTransform.rotate(Vector3.X, -18f); // Reeling backward
        }
        torsoInst.transform.set(torsoTransform);
        renderWithTint(batch, env, torsoInst, flashTint);

        // 2. Head & Helmet (He wears a protective helmet with visor & danger stripe)
        headTransform.set(torsoTransform).translate(0f, 0.42f, 0f);
        headInst.transform.set(headTransform);
        renderWithTint(batch, env, headInst, flashTint);

        helmetShellInst.transform.set(headTransform).translate(0f, 0.04f, -0.01f);
        helmetVisorInst.transform.set(headTransform).translate(0f, 0.03f, 0.16f);
        helmetStripeInst.transform.set(headTransform).translate(0f, 0.05f, 0f);
        renderWithTint(batch, env, helmetShellInst, flashTint);
        renderWithTint(batch, env, helmetVisorInst, null);
        renderWithTint(batch, env, helmetStripeInst, null);

        // 3. Legs
        float legSwing = (state == State.APPROACHING) ? MathUtils.sin(walkCycle) * 32f : 0f;
        legLTransform.set(rootTransform).translate(-0.14f, 0.45f, 0f).rotate(Vector3.X, legSwing);
        legRTransform.set(rootTransform).translate(0.14f, 0.45f, 0f).rotate(Vector3.X, -legSwing);
        legLInst.transform.set(legLTransform);
        legRInst.transform.set(legRTransform);
        renderWithTint(batch, env, legLInst, flashTint);
        renderWithTint(batch, env, legRInst, flashTint);

        bootLInst.transform.set(legLTransform).translate(0f, -0.40f, 0.06f);
        bootRInst.transform.set(legRTransform).translate(0f, -0.40f, 0.06f);
        renderWithTint(batch, env, bootLInst, flashTint);
        renderWithTint(batch, env, bootRInst, flashTint);

        // 4. Arms & Lathi Weapon
        // Left arm: aggressive guard/posture
        leftUpperArmTransform.set(torsoTransform).translate(-0.30f, 0.18f, 0.04f).rotate(Vector3.X, -20f).rotate(Vector3.Z, 15f);
        leftForeArmTransform.set(leftUpperArmTransform).translate(0f, -0.28f, 0.08f).rotate(Vector3.X, -65f);
        armUpperLInst.transform.set(leftUpperArmTransform);
        armForeLInst.transform.set(leftForeArmTransform);
        renderWithTint(batch, env, armUpperLInst, flashTint);
        renderWithTint(batch, env, armForeLInst, flashTint);

        // Right arm: holds the lathi!
        float armPitch = -15f;
        float armYaw = 0f;
        float armRoll = 0f;

        if (state == State.HIGH_SWING) {
            float t = MathUtils.clamp(attackProgress, 0f, 1f);
            float swingAngle = MathUtils.lerp(65f, -95f, t); // Powerful horizontal sweep
            armPitch = -35f;
            armYaw = swingAngle;
        } else if (state == State.OVERHEAD_SLAM) {
            float t = MathUtils.clamp(attackProgress, 0f, 1f);
            float slamAngle = (t < 0.4f) ? MathUtils.lerp(-20f, -120f, t / 0.4f) : MathUtils.lerp(-120f, 40f, (t - 0.4f) / 0.6f);
            armPitch = slamAngle;
        } else if (state == State.CHALLENGING) {
            // Raising lathi high above head to intimidate
            armPitch = -115f + MathUtils.sin(animTime * 3f) * 12f;
        } else if (state == State.STAGGERED) {
            armPitch = 20f;
            armRoll = 35f;
        }

        rightUpperArmTransform.set(torsoTransform).translate(0.30f, 0.18f, 0.04f)
            .rotate(Vector3.X, armPitch).rotate(Vector3.Y, armYaw).rotate(Vector3.Z, armRoll);
        rightForeArmTransform.set(rightUpperArmTransform).translate(0f, -0.28f, 0f).rotate(Vector3.X, -30f);

        armUpperRInst.transform.set(rightUpperArmTransform);
        armForeRInst.transform.set(rightForeArmTransform);
        renderWithTint(batch, env, armUpperRInst, flashTint);
        renderWithTint(batch, env, armForeRInst, flashTint);

        // Lathi in right hand (held securely at lower grip)
        lathiTransform.set(rightForeArmTransform).translate(0f, -0.16f, 0.38f).rotate(Vector3.X, 90f);
        lathiStickInst.transform.set(lathiTransform);
        lathiTipTopInst.transform.set(lathiTransform).translate(0f, 0.78f, 0f);
        lathiTipBottomInst.transform.set(lathiTransform).translate(0f, -0.78f, 0f);

        renderWithTint(batch, env, lathiStickInst, null);
        renderWithTint(batch, env, lathiTipTopInst, null);
        renderWithTint(batch, env, lathiTipBottomInst, null);
    }

    private void renderWithTint(ModelBatch batch, Environment env, ModelInstance inst, Color tint) {
        if (tint != null) {
            ColorAttribute ca = (ColorAttribute) inst.materials.get(0).get(ColorAttribute.Diffuse);
            if (ca != null) {
                Color old = ca.color.cpy();
                ca.color.set(tint);
                batch.render(inst, env);
                ca.color.set(old);
                return;
            }
        }
        batch.render(inst, env);
    }

    // Getters and helper status methods
    public Vector3 getPosition() { return position; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public State getState() { return state; }
    public boolean isKnockedOut() { return state == State.KNOCKED_OUT; }
    public boolean isChallenging() { return state == State.CHALLENGING; }
    public String getCombatPopupText() { return combatPopupTimer > 0f ? combatPopupText : null; }
    public Color getCombatPopupColor() { return combatPopupColor; }

    public void startFightDirectly() {
        if (state == State.STANDOFF || state == State.CHALLENGING) {
            state = State.APPROACHING;
            stateTimer = 0f;
        }
    }

    public void reset(float x, float z) {
        position.set(x, 0f, z);
        health = maxHealth;
        state = State.STANDOFF;
        stateTimer = 0f;
        attackCooldown = 0f;
        isAttacking = false;
        currentAttackType = 0;
        combatPopupTimer = 0f;
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
    }
}
