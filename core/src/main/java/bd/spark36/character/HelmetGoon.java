// ei file ta helmet pora attacker goon der ai ar movement control kore
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
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;

public class HelmetGoon implements Disposable {

    public enum Role {
        CAMPUS_SECURITY,
        HELMETED_AGGRESSOR
    }

    public enum State {
        STANDOFF,
        CHALLENGING,
        APPROACHING,
        HIGH_SWING,
        OVERHEAD_SLAM,
        THRUST,
        STAGGERED,
        KNOCKED_OUT
    }

    private Role role = Role.CAMPUS_SECURITY;
    private State state = State.STANDOFF;
    private final Vector3 position = new Vector3(0f, 0f, 36.0f);
    private float heading = 0f;
    private float health = 100f;
    private final float maxHealth = 100f;
    private float poise = 100f;
    private final float maxPoise = 100f;
    private float poiseRegenCooldown = 0f;
    private boolean isGuardBroken = false;
    private float guardBreakTimer = 0f;

    private final Vector3 patrolTarget = new Vector3(0f, 0f, 40.0f);
    private float patrolPauseTimer = 0f;
    private int patrolWaypointIndex = 0;
    private static final float[][] SECURITY_PATROL_WAYPOINTS = {
        {0f, 62f},     // Promenade North
        {0f, 45f},     // Promenade Mid
        {0f, 30f},     // Cross Avenue Center
        {0f, 22f},     // South Pond Link
        {-10f, 24f},   // West Pond Walk
        {0f, -10f},    // Curzon Forecourt
        {3.6f, -15f},  // Curzon Arcade Entry
        {10f, 24f},    // East Pond Walk
        {0f, 40f}      // Promenade Return
    };

    private float animTime = 0f;
    private float stateTimer = 0f;
    private float attackCooldown = 0f;
    private float walkCycle = 0f;
    private boolean isAttacking = false;
    private float attackProgress = 0f;
    private int currentAttackType = 0;
    private boolean hitDealtThisAttack = false;

    private float staggerTimer = 0f;
    private float hitFlashTimer = 0f;
    private final Vector3 knockbackVel = new Vector3();
    private String combatPopupText = "";
    private float combatPopupTimer = 0f;
    private Color combatPopupColor = Color.WHITE;

    private final ModelBuilder mb = new ModelBuilder();
    private final Array<Model> models = new Array<>();
    private final long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

    private Model torsoModel;
    private Model headModel;
    private Model upperArmModel;
    private Model foreArmModel;
    private Model legModel;
    private Model bootModel;

    private Model helmetShellModel;
    private Model helmetVisorModel;
    private Model helmetStripeModel;
    private Model lathiStickModel;
    private Model lathiTipModel;

    private Model securityCapModel;
    private Model securityBadgeModel;

    private ModelInstance torsoInst;
    private ModelInstance headInst;
    private ModelInstance armUpperLInst, armUpperRInst;
    private ModelInstance armForeLInst, armForeRInst;
    private ModelInstance legLInst, legRInst;
    private ModelInstance bootLInst, bootRInst;
    private ModelInstance helmetShellInst, helmetVisorInst, helmetStripeInst;
    private ModelInstance lathiStickInst, lathiTipTopInst, lathiTipBottomInst;
    private ModelInstance securityCapInst, securityBadgeInst;

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

    public HelmetGoon() {

    }

    public HelmetGoon(TextureFactory textures) {
        buildModels(textures);
    }

    private static void setupPbr(Material mat, float metallic, float roughness) {
        mat.set(PBRFloatAttribute.createMetallic(metallic));
        mat.set(PBRFloatAttribute.createRoughness(roughness));
        ColorAttribute diff = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
        if (diff != null && !mat.has(PBRColorAttribute.BaseColorFactor)) {
            mat.set(PBRColorAttribute.createBaseColorFactor(diff.color));
        }
    }

    private void buildModels(TextureFactory textures) {

        Material jacketMat = new Material(ColorAttribute.createDiffuse(new Color(0.18f, 0.20f, 0.24f, 1f)));
        Material skinMat = new Material(ColorAttribute.createDiffuse(new Color(0.74f, 0.54f, 0.42f, 1f)));
        Material pantsMat = new Material(ColorAttribute.createDiffuse(new Color(0.14f, 0.16f, 0.20f, 1f)));
        Material bootMat = new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.10f, 0.11f, 1f)));

        Material helmetMat = new Material(ColorAttribute.createDiffuse(new Color(0.12f, 0.14f, 0.18f, 1f)));
        Material visorMat = new Material(ColorAttribute.createDiffuse(new Color(0.04f, 0.05f, 0.06f, 1f)));
        Material stripeMat = new Material(ColorAttribute.createDiffuse(new Color(0.96f, 0.78f, 0.10f, 1f)));

        Material lathiWoodMat = new Material(ColorAttribute.createDiffuse(new Color(0.48f, 0.32f, 0.16f, 1f)));
        Material brassMat = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.72f, 0.22f, 1f)));

        Material capMat = new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.14f, 0.22f, 1f)));
        Material badgeMat = new Material(ColorAttribute.createDiffuse(new Color(0.88f, 0.75f, 0.25f, 1f)));

        setupPbr(jacketMat, 0.0f, 0.65f);
        setupPbr(skinMat, 0.0f, 0.60f);
        setupPbr(pantsMat, 0.0f, 0.85f);
        setupPbr(bootMat, 0.0f, 0.70f);
        setupPbr(helmetMat, 0.20f, 0.35f);
        setupPbr(visorMat, 0.10f, 0.15f);
        setupPbr(stripeMat, 0.0f, 0.40f);
        setupPbr(lathiWoodMat, 0.0f, 0.75f);
        setupPbr(brassMat, 0.85f, 0.28f);
        setupPbr(capMat, 0.0f, 0.85f);
        setupPbr(badgeMat, 0.85f, 0.25f);

        torsoModel = mb.createBox(0.48f, 0.62f, 0.30f, jacketMat, attr);
        headModel = mb.createSphere(0.24f, 0.28f, 0.26f, 14, 12, skinMat, attr);
        upperArmModel = mb.createCapsule(0.08f, 0.36f, 10, jacketMat, attr);
        foreArmModel = mb.createCapsule(0.075f, 0.34f, 10, skinMat, attr);
        legModel = mb.createCapsule(0.10f, 0.74f, 10, pantsMat, attr);
        bootModel = mb.createBox(0.15f, 0.14f, 0.28f, bootMat, attr);

        helmetShellModel = mb.createSphere(0.38f, 0.36f, 0.40f, 16, 14, helmetMat, attr);
        helmetVisorModel = mb.createBox(0.34f, 0.14f, 0.18f, visorMat, attr);
        helmetStripeModel = mb.createBox(0.06f, 0.38f, 0.41f, stripeMat, attr);

        lathiStickModel = mb.createCylinder(0.052f, 1.60f, 0.052f, 12, lathiWoodMat, attr);
        lathiTipModel = mb.createCylinder(0.058f, 0.08f, 0.058f, 12, brassMat, attr);

        securityCapModel = mb.createCylinder(0.30f, 0.09f, 0.30f, 16, capMat, attr);
        securityBadgeModel = mb.createBox(0.06f, 0.06f, 0.03f, badgeMat, attr);

        models.addAll(torsoModel, headModel, upperArmModel, foreArmModel, legModel, bootModel,
                      helmetShellModel, helmetVisorModel, helmetStripeModel, lathiStickModel, lathiTipModel,
                      securityCapModel, securityBadgeModel);

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

        securityCapInst = new ModelInstance(securityCapModel);
        securityBadgeInst = new ModelInstance(securityBadgeModel);
    }

    public void update(float delta, PlayerController player) {
        animTime += delta;
        if (role == Role.CAMPUS_SECURITY) {
            animTime += delta;
            state = State.STANDOFF;
            isAttacking = false;

            if (patrolPauseTimer > 0f) {
                patrolPauseTimer -= delta;
                return;
            }

            float dx = patrolTarget.x - position.x;
            float dz = patrolTarget.z - position.z;
            float dist = (float) Math.sqrt(dx * dx + dz * dz);

            if (dist < 1.0f) {
                patrolPauseTimer = MathUtils.random(0.6f, 1.4f);
                patrolWaypointIndex = (patrolWaypointIndex + 1) % SECURITY_PATROL_WAYPOINTS.length;
                patrolTarget.set(SECURITY_PATROL_WAYPOINTS[patrolWaypointIndex][0], 0f, SECURITY_PATROL_WAYPOINTS[patrolWaypointIndex][1]);
                return;
            }

            float speed = 1.35f;
            float vx = (dx / dist) * speed;
            float vz = (dz / dist) * speed;
            position.x += vx * delta;
            position.z += vz * delta;

            float targetHeading = MathUtils.atan2(vx, vz) * MathUtils.radiansToDegrees;
            float diff = (targetHeading - heading) % 360f;
            if (diff > 180f) diff -= 360f;
            if (diff < -180f) diff += 360f;
            heading += diff * Math.min(1f, delta * 5.0f);

            walkCycle += delta * 4.2f;
            return;
        }

        stateTimer += delta;
        if (attackCooldown > 0f) attackCooldown -= delta;
        if (staggerTimer > 0f) staggerTimer -= delta;
        if (hitFlashTimer > 0f) hitFlashTimer -= delta;
        if (combatPopupTimer > 0f) combatPopupTimer -= delta;

        if (knockbackVel.len2() > 0.001f) {
            position.x += knockbackVel.x * delta;
            position.z += knockbackVel.z * delta;
            knockbackVel.scl(Math.max(0f, 1f - 11f * delta));
        }

        if (state == State.KNOCKED_OUT) {
            return;
        }

        if (isGuardBroken) {
            guardBreakTimer -= delta;
            if (guardBreakTimer <= 0f) {
                isGuardBroken = false;
                poise = maxPoise;
                state = State.APPROACHING;
                attackCooldown = 0.5f;
            }
        } else {
            if (poiseRegenCooldown > 0f) {
                poiseRegenCooldown -= delta;
            } else if (poise < maxPoise) {
                poise = Math.min(maxPoise, poise + delta * 18f);
            }
        }

        Vector3 pPos = player.getPosition();
        float distToPlayer = position.dst(pPos);

        float dx = pPos.x - position.x;
        float dz = pPos.z - position.z;
        float targetHeading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
        if (state != State.KNOCKED_OUT) {
            float diff = (targetHeading - heading) % 360f;
            if (diff > 180f) diff -= 360f;
            if (diff < -180f) diff += 360f;
            heading += diff * Math.min(1f, delta * 6f);
        }

        float minCombatDist = 1.35f;
        if (distToPlayer < minCombatDist && distToPlayer > 0.001f && state != State.KNOCKED_OUT) {
            float overlap = minCombatDist - distToPlayer;
            float pushX = (dx / distToPlayer) * overlap;
            float pushZ = (dz / distToPlayer) * overlap;
            position.x -= pushX * 0.75f;
            position.z -= pushZ * 0.75f;
            distToPlayer = position.dst(pPos);
        }

        switch (state) {
            case STANDOFF:

                if (distToPlayer <= 26f) {
                    state = State.CHALLENGING;
                    stateTimer = 0f;
                }
                break;

            case CHALLENGING:

                if (stateTimer >= 2.5f || distToPlayer <= 10f || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F)) {
                    state = State.APPROACHING;
                    stateTimer = 0f;
                }
                break;

            case APPROACHING:
                if (distToPlayer > 2.05f) {

                    float speed = 2.4f;
                    walkCycle += delta * 7f;
                    position.x += MathUtils.sinDeg(heading) * speed * delta;
                    position.z += MathUtils.cosDeg(heading) * speed * delta;
                } else if (distToPlayer < 1.40f) {

                    float backSpeed = 1.5f;
                    walkCycle -= delta * 5f;
                    position.x -= MathUtils.sinDeg(heading) * backSpeed * delta;
                    position.z -= MathUtils.cosDeg(heading) * backSpeed * delta;
                } else {

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
                if (staggerTimer <= 0f && !isGuardBroken) {
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

        if (!hitDealtThisAttack && attackProgress >= 0.48f && attackProgress <= 0.68f) {
            if (distToPlayer <= 2.6f) {

                if (player.isDodging()) {

                    showPopup("⚡ EVADED! / চাতুর্যের সাথে এড়ানো হয়েছে! ⚡", Color.CYAN);
                    hitDealtThisAttack = true;
                } else if (currentAttackType == 1 && player.isCrouching()) {

                    showPopup("DODGED! / ফাঁকি দেওয়া হয়েছে!", Color.CYAN);
                    hitDealtThisAttack = true;
                } else if (player.isBlocking()) {

                    if (player.getBlockTimer() <= 0.25f) {
                        hitDealtThisAttack = true;
                        isAttacking = false;
                        currentAttackType = 0;
                        staggerTimer = 0.95f;
                        state = State.STAGGERED;
                        damagePoise(40f);
                        showPopup("⚡ PERFECT FLASH PARRY! / নিখুঁত প্রতিহত! ⚡", Color.GOLD);
                    } else {

                        float blockedDmg = (currentAttackType == 2) ? 6f : 3f;
                        float kbX = MathUtils.sinDeg(heading);
                        float kbZ = MathUtils.cosDeg(heading);
                        player.takeDamage(blockedDmg, kbX, kbZ, 2.8f);
                        damagePoise(10f);
                        showPopup("BLOCKED! / প্রতিহত!", Color.YELLOW);
                        hitDealtThisAttack = true;
                    }
                } else {

                    float dmg = (currentAttackType == 2) ? 24f : 16f;
                    float kbX = MathUtils.sinDeg(heading);
                    float kbZ = MathUtils.cosDeg(heading);
                    float kbStrength = (currentAttackType == 2) ? 6.5f : 4.2f;
                    player.takeDamage(dmg, kbX, kbZ, kbStrength);
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

    public boolean takeHitFromPlayer(float damage, boolean isKick, boolean isSweep, float attackHeading) {
        if (role != Role.HELMETED_AGGRESSOR || state == State.KNOCKED_OUT) return false;

        boolean wasWindingUp = isWindingUp();
        float finalDmg = damage;
        if (isGuardBroken) {
            finalDmg *= 2.2f;
        } else if (wasWindingUp) {
            finalDmg *= 1.5f;
        }

        health -= finalDmg;
        hitFlashTimer = 0.25f;
        staggerTimer = isSweep ? 0.90f : (isKick ? 0.65f : 0.40f);
        state = State.STAGGERED;
        isAttacking = false;
        currentAttackType = 0;

        float poiseDmg = isSweep ? 50f : (isKick ? 32f : 16f);
        if (wasWindingUp) poiseDmg += 20f;
        damagePoise(poiseDmg);

        float knockbackForce = isSweep ? 6.2f : (isKick ? 4.8f : 3.2f);
        if (isGuardBroken) knockbackForce *= 1.4f;
        knockbackVel.set(MathUtils.sinDeg(attackHeading) * knockbackForce, 0f, MathUtils.cosDeg(attackHeading) * knockbackForce);

        if (health <= 0f) {
            health = 0f;
            state = State.KNOCKED_OUT;
            showPopup("AGGRESSOR DEFEATED / আক্রমণকারী পরাজিত!", Color.GREEN);
            return true;
        } else {
            if (isGuardBroken) {
                showPopup("💥 CRITICAL OPENING! -" + (int)finalDmg, Color.RED);
            } else if (wasWindingUp) {
                showPopup("⚡ COUNTER-HIT INTERRUPT! / পাল্টা আঘাত! -" + (int)finalDmg, Color.GOLD);
            } else if (isSweep) {
                showPopup("DRAGON LEG SWEEP! / ড্রাগন সুইপ! -" + (int)finalDmg, Color.CYAN);
            } else if (isKick) {
                showPopup("HEAVY KICK! -" + (int)finalDmg, Color.ORANGE);
            } else {
                showPopup("MARTIAL PUNCH! -" + (int)finalDmg, Color.YELLOW);
            }
            return false;
        }
    }

    public boolean takeHitFromPlayer(float damage, boolean isKick, boolean isSweep) {
        return takeHitFromPlayer(damage, isKick, isSweep, heading + 180f);
    }

    public boolean takeHitFromPlayer(float damage, boolean isKick) {
        return takeHitFromPlayer(damage, isKick, false);
    }

    public void damagePoise(float amount) {
        poiseRegenCooldown = 3.0f;
        poise = Math.max(0f, poise - amount);
        if (poise <= 0f && !isGuardBroken && state != State.KNOCKED_OUT) {
            isGuardBroken = true;
            guardBreakTimer = 2.4f;
            staggerTimer = 2.4f;
            state = State.STAGGERED;
            showPopup("💥 GUARD BROKEN! / ভঙ্গি ভেঙে গেছে! 💥", Color.RED);
        }
    }

    private void showPopup(String text, Color color) {
        this.combatPopupText = text;
        this.combatPopupColor = color;
        this.combatPopupTimer = 1.4f;
    }

    public void render(ModelBatch batch, Environment env) {
        if (torsoInst == null) return;
        rootTransform.idt();
        rootTransform.translate(position.x, position.y, position.z);
        rootTransform.rotate(Vector3.Y, heading);

        if (state == State.KNOCKED_OUT) {

            rootTransform.translate(0f, 0.22f, 0f).rotate(Vector3.X, -88f);
        }

        Color flashTint = (hitFlashTimer > 0f) ? Color.RED : null;

        torsoTransform.set(rootTransform).translate(0f, 1.15f, 0f);
        if (state == State.STAGGERED) {
            torsoTransform.rotate(Vector3.X, -18f);
        }
        torsoInst.transform.set(torsoTransform);
        renderWithTint(batch, env, torsoInst, flashTint);

        headTransform.set(torsoTransform).translate(0f, 0.42f, 0f);
        headInst.transform.set(headTransform);
        renderWithTint(batch, env, headInst, flashTint);

        if (role == Role.CAMPUS_SECURITY) {

            securityCapInst.transform.set(headTransform).translate(0f, 0.12f, -0.01f);
            securityBadgeInst.transform.set(headTransform).translate(0f, 0.12f, 0.15f);
            renderWithTint(batch, env, securityCapInst, null);
            renderWithTint(batch, env, securityBadgeInst, null);
        } else {

            helmetShellInst.transform.set(headTransform).translate(0f, 0.04f, -0.01f);
            helmetVisorInst.transform.set(headTransform).translate(0f, 0.03f, 0.16f);
            helmetStripeInst.transform.set(headTransform).translate(0f, 0.05f, 0f);
            renderWithTint(batch, env, helmetShellInst, flashTint);
            renderWithTint(batch, env, helmetVisorInst, null);
            renderWithTint(batch, env, helmetStripeInst, null);
        }

        boolean isWalking = (state == State.APPROACHING) ||
            (role == Role.CAMPUS_SECURITY && patrolPauseTimer <= 0f);
        float legSwing = isWalking ? MathUtils.sin(walkCycle) * 30f : 0f;
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

        if (role == Role.CAMPUS_SECURITY) {

            float armSwing = (patrolPauseTimer <= 0f) ? MathUtils.sin(walkCycle) * 20f : 0f;
            float breath = MathUtils.sin(animTime * 1.8f) * 2f;
            leftUpperArmTransform.set(torsoTransform).translate(-0.30f, 0.18f, 0.04f).rotate(Vector3.X, 8f + breath - armSwing).rotate(Vector3.Z, 6f);
            leftForeArmTransform.set(leftUpperArmTransform).translate(0f, -0.28f, 0.04f).rotate(Vector3.X, -10f);
            armUpperLInst.transform.set(leftUpperArmTransform);
            armForeLInst.transform.set(leftForeArmTransform);
            renderWithTint(batch, env, armUpperLInst, flashTint);
            renderWithTint(batch, env, armForeLInst, flashTint);

            rightUpperArmTransform.set(torsoTransform).translate(0.30f, 0.18f, 0.04f).rotate(Vector3.X, 8f + breath + armSwing).rotate(Vector3.Z, -6f);
            rightForeArmTransform.set(rightUpperArmTransform).translate(0f, -0.28f, 0.04f).rotate(Vector3.X, -10f);
            armUpperRInst.transform.set(rightUpperArmTransform);
            armForeRInst.transform.set(rightForeArmTransform);
            renderWithTint(batch, env, armUpperRInst, flashTint);
            renderWithTint(batch, env, armForeRInst, flashTint);

        } else {

            leftUpperArmTransform.set(torsoTransform).translate(-0.30f, 0.18f, 0.04f).rotate(Vector3.X, -20f).rotate(Vector3.Z, 15f);
            leftForeArmTransform.set(leftUpperArmTransform).translate(0f, -0.28f, 0.08f).rotate(Vector3.X, -65f);
            armUpperLInst.transform.set(leftUpperArmTransform);
            armForeLInst.transform.set(leftForeArmTransform);
            renderWithTint(batch, env, armUpperLInst, flashTint);
            renderWithTint(batch, env, armForeLInst, flashTint);

            float armPitch = -15f;
            float armYaw = 0f;
            float armRoll = 0f;

            if (state == State.HIGH_SWING) {
                float t = MathUtils.clamp(attackProgress, 0f, 1f);
                float swingAngle = MathUtils.lerp(65f, -95f, t);
                armPitch = -35f;
                armYaw = swingAngle;
            } else if (state == State.OVERHEAD_SLAM) {
                float t = MathUtils.clamp(attackProgress, 0f, 1f);
                float slamAngle = (t < 0.4f) ? MathUtils.lerp(-20f, -120f, t / 0.4f) : MathUtils.lerp(-120f, 40f, (t - 0.4f) / 0.6f);
                armPitch = slamAngle;
            } else if (state == State.CHALLENGING) {

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

            lathiTransform.set(rightForeArmTransform).translate(0f, -0.16f, 0.38f).rotate(Vector3.X, 90f);
            lathiStickInst.transform.set(lathiTransform);
            lathiTipTopInst.transform.set(lathiTransform).translate(0f, 0.78f, 0f);
            lathiTipBottomInst.transform.set(lathiTransform).translate(0f, -0.78f, 0f);

            renderWithTint(batch, env, lathiStickInst, null);
            renderWithTint(batch, env, lathiTipTopInst, null);
            renderWithTint(batch, env, lathiTipBottomInst, null);
        }
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

    public Vector3 getPosition() { return position; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getPoise() { return poise; }
    public float getMaxPoise() { return maxPoise; }
    public Role getRole() { return role; }
    public void setRole(Role role) {
        this.role = role;
        if (role == Role.CAMPUS_SECURITY) {
            this.state = State.STANDOFF;
            this.isAttacking = false;
        }
    }
    public boolean isGuardBroken() { return role == Role.HELMETED_AGGRESSOR && isGuardBroken; }
    public State getState() { return state; }
    public boolean isKnockedOut() { return role == Role.HELMETED_AGGRESSOR && state == State.KNOCKED_OUT; }
    public boolean isChallenging() { return role == Role.HELMETED_AGGRESSOR && state == State.CHALLENGING; }
    public boolean isWindingUp() { return role == Role.HELMETED_AGGRESSOR && isAttacking && attackProgress < 0.48f; }
    public String getCombatPopupText() { return (role == Role.HELMETED_AGGRESSOR && combatPopupTimer > 0f) ? combatPopupText : null; }
    public Color getCombatPopupColor() { return combatPopupColor; }

    public float getHeading() { return heading; }
    public void setHeading(float headingDeg) { this.heading = headingDeg; }

    public void startFightDirectly() {
        this.role = Role.HELMETED_AGGRESSOR;
        state = State.APPROACHING;
        stateTimer = 0f;
        attackCooldown = 0.4f;
    }

    public void reset(float x, float z) {
        position.set(x, 0f, z);
        knockbackVel.setZero();
        health = maxHealth;
        poise = maxPoise;
        isGuardBroken = false;
        guardBreakTimer = 0f;
        poiseRegenCooldown = 0f;
        state = State.STANDOFF;
        stateTimer = 0f;
        attackCooldown = 0f;
        isAttacking = false;
        currentAttackType = 0;
        combatPopupTimer = 0f;
        patrolPauseTimer = 0f;
        patrolTarget.set(x, 0f, z + 14f);
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
    }
}
