// ei file ta kache thaka object ba npc er sathe interaction handle kore
package bd.spark36.interaction;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import bd.spark36.character.PlayerController;
import bd.spark36.hud.WhereWindsMeetHUD;

public class InteractionSystem {

    private final Array<Interactable> registeredInteractables = new Array<Interactable>(false, 32);

    private float cooldownTimer = 0f;
    public static final float DEFAULT_DEBOUNCE_TIME = 0.35f;

    private Interactable focusedInteractable = null;

    public void register(Interactable interactable) {
        if (interactable != null && !registeredInteractables.contains(interactable, true)) {
            registeredInteractables.add(interactable);
        }
    }

    public void unregister(Interactable interactable) {
        if (interactable != null) {
            registeredInteractables.removeValue(interactable, true);
            if (focusedInteractable == interactable) {
                focusedInteractable = null;
            }
        }
    }

    public void clear() {
        registeredInteractables.clear();
        focusedInteractable = null;
        cooldownTimer = 0f;
    }

    public void update(float delta, Vector3 playerPos, float playerHeadingDegrees) {
        if (cooldownTimer > 0f) {
            cooldownTimer -= delta;
            if (cooldownTimer < 0f) cooldownTimer = 0f;
        }

        focusedInteractable = findBestInteractable(playerPos, playerHeadingDegrees, registeredInteractables);
    }

    public Interactable findBestInteractable(Vector3 playerPos, float playerHeadingDegrees, Iterable<? extends Interactable> candidates) {
        if (playerPos == null || candidates == null) return null;

        float fx = MathUtils.sinDeg(playerHeadingDegrees);
        float fz = MathUtils.cosDeg(playerHeadingDegrees);

        Interactable bestTarget = null;
        float bestScore = -Float.MAX_VALUE;

        for (Interactable candidate : candidates) {
            if (candidate == null) continue;

            Vector3 targetPos = candidate.getInteractionPosition();
            if (targetPos == null) continue;

            float maxRadius = candidate.getInteractionRadius();
            float dx = targetPos.x - playerPos.x;
            float dz = targetPos.z - playerPos.z;
            float distSq = dx * dx + dz * dz;

            if (distSq > maxRadius * maxRadius) continue;

            if (!candidate.canInteract(playerPos, playerHeadingDegrees)) continue;

            float dist = (float) Math.sqrt(distSq);
            float dot = 0f;
            if (dist > 0.001f) {
                dot = (dx * fx + dz * fz) / dist;
            } else {
                dot = 1f;
            }

            if (dot < -0.25f && dist > 0.75f) {
                continue;
            }

            float proximityScore = (1f - (dist / maxRadius)) * 4.0f;
            float facingScore = Math.max(0f, dot) * 3.0f;
            float totalScore = (candidate.getInteractionPriority() * 10.0f) + proximityScore + facingScore;

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestTarget = candidate;
            }
        }

        return bestTarget;
    }

    public Interactable findBestInteractable(Vector3 playerPos, float playerHeadingDegrees) {
        return findBestInteractable(playerPos, playerHeadingDegrees, registeredInteractables);
    }

    public boolean triggerInteraction(Vector3 playerPos, float playerHeadingDegrees, PlayerController player, WhereWindsMeetHUD hud) {
        if (cooldownTimer > 0f) {
            return false;
        }

        Interactable target = findBestInteractable(playerPos, playerHeadingDegrees, registeredInteractables);
        if (target != null) {
            boolean consumed = target.onInteract(playerPos, player, hud);
            if (consumed) {
                cooldownTimer = DEFAULT_DEBOUNCE_TIME;
                return true;
            }
        }
        return false;
    }

    public boolean triggerInteractionDirect(Interactable target, Vector3 playerPos, PlayerController player, WhereWindsMeetHUD hud) {
        if (cooldownTimer > 0f || target == null) {
            return false;
        }

        boolean consumed = target.onInteract(playerPos, player, hud);
        if (consumed) {
            cooldownTimer = DEFAULT_DEBOUNCE_TIME;
            return true;
        }
        return false;
    }

    public Interactable getFocusedInteractable() {
        return focusedInteractable;
    }

    public String getContextPrompt() {
        return (focusedInteractable != null) ? focusedInteractable.getInteractionPrompt() : null;
    }

    public boolean isCooldownActive() {
        return cooldownTimer > 0f;
    }

    public void setCooldown(float seconds) {
        this.cooldownTimer = Math.max(0f, seconds);
    }
}
