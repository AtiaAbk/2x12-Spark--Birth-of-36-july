// ei file ta keyboard ar mouse er shob input event capture kore
package bd.spark36.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.EnumMap;
import java.util.Map;

public final class GameInput {

    public enum GameAction {
        MOVE_FORWARD,
        MOVE_BACK,
        MOVE_LEFT,
        MOVE_RIGHT,
        JUMP,
        SPRINT,
        CROUCH,
        ATTACK_PUNCH,
        ATTACK_KICK,
        DEFEND_BLOCK,
        DODGE,
        INTERACT,
        MAP,
        PAUSE,
        HELP_CONTROLS,
        TOGGLE_GENDER,
        TOGGLE_PARAMETERS,
        CYCLE_TONEMAP,
        CONFIRM,
        CANCEL,
        RESTART
    }

    public static class KeyBinding {
        public int primaryKey;
        public int secondaryKey;
        public int mouseButton = -1;

        public KeyBinding(int primaryKey, int secondaryKey) {
            this.primaryKey = primaryKey;
            this.secondaryKey = secondaryKey;
        }

        public KeyBinding(int primaryKey, int secondaryKey, int mouseButton) {
            this.primaryKey = primaryKey;
            this.secondaryKey = secondaryKey;
            this.mouseButton = mouseButton;
        }
    }

    private static final Map<GameAction, KeyBinding> BINDINGS = new EnumMap<>(GameAction.class);

    static {
        resetToDefaults();
    }

    private GameInput() {}

    public static void resetToDefaults() {
        BINDINGS.put(GameAction.MOVE_FORWARD, new KeyBinding(Input.Keys.W, Input.Keys.UP));
        BINDINGS.put(GameAction.MOVE_BACK, new KeyBinding(Input.Keys.S, Input.Keys.DOWN));
        BINDINGS.put(GameAction.MOVE_LEFT, new KeyBinding(Input.Keys.A, Input.Keys.LEFT));
        BINDINGS.put(GameAction.MOVE_RIGHT, new KeyBinding(Input.Keys.D, Input.Keys.RIGHT));

        BINDINGS.put(GameAction.JUMP, new KeyBinding(Input.Keys.SPACE, Input.Keys.J));

        BINDINGS.put(GameAction.SPRINT, new KeyBinding(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT));

        BINDINGS.put(GameAction.CROUCH, new KeyBinding(Input.Keys.C, Input.Keys.CONTROL_LEFT));

        BINDINGS.put(GameAction.ATTACK_PUNCH, new KeyBinding(Input.Keys.F, -1, Input.Buttons.LEFT));

        BINDINGS.put(GameAction.ATTACK_KICK, new KeyBinding(Input.Keys.Q, Input.Keys.K));

        BINDINGS.put(GameAction.DEFEND_BLOCK, new KeyBinding(Input.Keys.B, -1, Input.Buttons.RIGHT));

        BINDINGS.put(GameAction.DODGE, new KeyBinding(Input.Keys.Z, Input.Keys.ALT_LEFT));

        BINDINGS.put(GameAction.INTERACT, new KeyBinding(Input.Keys.E, Input.Keys.ENTER));

        BINDINGS.put(GameAction.MAP, new KeyBinding(Input.Keys.M, Input.Keys.TAB));

        BINDINGS.put(GameAction.PAUSE, new KeyBinding(Input.Keys.ESCAPE, -1));

        BINDINGS.put(GameAction.HELP_CONTROLS, new KeyBinding(Input.Keys.H, Input.Keys.F1));

        BINDINGS.put(GameAction.TOGGLE_GENDER, new KeyBinding(Input.Keys.G, -1));

        BINDINGS.put(GameAction.TOGGLE_PARAMETERS, new KeyBinding(Input.Keys.P, -1));

        BINDINGS.put(GameAction.CYCLE_TONEMAP, new KeyBinding(Input.Keys.T, -1));

        BINDINGS.put(GameAction.CONFIRM, new KeyBinding(Input.Keys.ENTER, Input.Keys.SPACE));

        BINDINGS.put(GameAction.CANCEL, new KeyBinding(Input.Keys.ESCAPE, Input.Keys.BACKSPACE));

        BINDINGS.put(GameAction.RESTART, new KeyBinding(Input.Keys.R, -1));
    }

    public static boolean isPressed(GameAction action) {
        if (Gdx.input == null) return false;
        KeyBinding binding = BINDINGS.get(action);
        if (binding == null) return false;

        if (binding.primaryKey >= 0 && Gdx.input.isKeyPressed(binding.primaryKey)) return true;
        if (binding.secondaryKey >= 0 && Gdx.input.isKeyPressed(binding.secondaryKey)) return true;
        if (binding.mouseButton >= 0 && Gdx.input.isButtonPressed(binding.mouseButton)) return true;
        return false;
    }

    public static boolean isJustPressed(GameAction action) {
        if (Gdx.input == null) return false;
        KeyBinding binding = BINDINGS.get(action);
        if (binding == null) return false;

        if (binding.primaryKey >= 0 && Gdx.input.isKeyJustPressed(binding.primaryKey)) return true;
        if (binding.secondaryKey >= 0 && Gdx.input.isKeyJustPressed(binding.secondaryKey)) return true;
        if (binding.mouseButton >= 0 && Gdx.input.isButtonJustPressed(binding.mouseButton)) return true;
        return false;
    }

    public static boolean matchesKey(GameAction action, int keyCode) {
        KeyBinding binding = BINDINGS.get(action);
        if (binding == null) return false;
        return binding.primaryKey == keyCode || binding.secondaryKey == keyCode;
    }

    public static String getKeyLabel(GameAction action) {
        KeyBinding b = BINDINGS.get(action);
        if (b == null) return "";
        if (action == GameAction.ATTACK_PUNCH) return "LEFT-CLICK / F";
        if (action == GameAction.ATTACK_KICK) return "Q / K";
        if (action == GameAction.DEFEND_BLOCK) return "B / RIGHT-CLICK";
        if (action == GameAction.JUMP) return "SPACE";
        if (action == GameAction.SPRINT) return "SHIFT";
        if (action == GameAction.MOVE_FORWARD) return "W / UP";
        if (action == GameAction.MOVE_BACK) return "S / DOWN";
        if (action == GameAction.MOVE_LEFT) return "A / LEFT";
        if (action == GameAction.MOVE_RIGHT) return "D / RIGHT";
        if (b.primaryKey >= 0) return Input.Keys.toString(b.primaryKey);
        return "";
    }

    public static float[] getVirtualMousePos(float virtW, float virtH) {
        if (Gdx.input == null || Gdx.graphics == null) return new float[]{0f, 0f};
        float winW = (float) Gdx.graphics.getWidth();
        float winH = (float) Gdx.graphics.getHeight();
        if (winW <= 0f) winW = 1f;
        if (winH <= 0f) winH = 1f;

        float rawX = (float) Gdx.input.getX();
        float rawY = (float) Gdx.input.getY();

        float vx = (rawX / winW) * virtW;
        float vy = (1.0f - (rawY / winH)) * virtH;
        return new float[]{vx, vy};
    }
}
