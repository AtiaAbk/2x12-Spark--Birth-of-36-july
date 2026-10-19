package bd.spark36.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Input;
import bd.spark36.input.GameInput.GameAction;
import bd.spark36.level.LevelConfig;
import bd.spark36.render.GraphicsTier;
import bd.spark36.render.RenderPipelineConfig;

/**
 * Automated Test Suite for Gameplay Master Overhaul:
 * 1. Action-to-Key mapping fidelity in GameInput.
 * 2. Virtual mouse scaling robustness.
 * 3. Level Progression & Campaign Unlocking (Level 1 through 6).
 * 4. Graphics Tier cycling and Tone Mapping modes.
 */
public class GameInputAndLevelFlowTest {

    @BeforeEach
    public void setUp() {
        GameInput.resetToDefaults();
        LevelConfig.reset(1, LevelConfig.GameMode.STORY);
    }

    @Test
    @DisplayName("GameInput: Locomotion actions correctly mapped to dual scheme (WASD + Arrows)")
    public void testLocomotionBindings() {
        assertTrue(GameInput.matchesKey(GameAction.MOVE_FORWARD, Input.Keys.W));
        assertTrue(GameInput.matchesKey(GameAction.MOVE_FORWARD, Input.Keys.UP));

        assertTrue(GameInput.matchesKey(GameAction.MOVE_BACK, Input.Keys.S));
        assertTrue(GameInput.matchesKey(GameAction.MOVE_BACK, Input.Keys.DOWN));

        assertTrue(GameInput.matchesKey(GameAction.MOVE_LEFT, Input.Keys.A));
        assertTrue(GameInput.matchesKey(GameAction.MOVE_LEFT, Input.Keys.LEFT));

        assertTrue(GameInput.matchesKey(GameAction.MOVE_RIGHT, Input.Keys.D));
        assertTrue(GameInput.matchesKey(GameAction.MOVE_RIGHT, Input.Keys.RIGHT));
    }

    @Test
    @DisplayName("GameInput: Athletic controls mapped (Space for Jump, Shift for Sprint, C for Crouch)")
    public void testAthleticsBindings() {
        assertTrue(GameInput.matchesKey(GameAction.JUMP, Input.Keys.SPACE));
        assertTrue(GameInput.matchesKey(GameAction.JUMP, Input.Keys.J));

        assertTrue(GameInput.matchesKey(GameAction.SPRINT, Input.Keys.SHIFT_LEFT));
        assertTrue(GameInput.matchesKey(GameAction.SPRINT, Input.Keys.SHIFT_RIGHT));

        assertTrue(GameInput.matchesKey(GameAction.CROUCH, Input.Keys.C));
        assertTrue(GameInput.matchesKey(GameAction.CROUCH, Input.Keys.CONTROL_LEFT));
    }

    @Test
    @DisplayName("GameInput: Technical combat actions mapped (F/Left-Click Punch, Q/K Kick, B Guard, Z Dodge)")
    public void testCombatBindings() {
        assertTrue(GameInput.matchesKey(GameAction.ATTACK_PUNCH, Input.Keys.F));
        assertTrue(GameInput.matchesKey(GameAction.ATTACK_KICK, Input.Keys.Q));
        assertTrue(GameInput.matchesKey(GameAction.ATTACK_KICK, Input.Keys.K));
        assertTrue(GameInput.matchesKey(GameAction.DEFEND_BLOCK, Input.Keys.B));
        assertTrue(GameInput.matchesKey(GameAction.DODGE, Input.Keys.Z));
        assertTrue(GameInput.matchesKey(GameAction.DODGE, Input.Keys.ALT_LEFT));
    }

    @Test
    @DisplayName("GameInput: System and UI navigation bindings (E/Enter Interact, M Map, ESC Pause, H Codex)")
    public void testSystemBindings() {
        assertTrue(GameInput.matchesKey(GameAction.INTERACT, Input.Keys.E));
        assertTrue(GameInput.matchesKey(GameAction.INTERACT, Input.Keys.ENTER));

        assertTrue(GameInput.matchesKey(GameAction.MAP, Input.Keys.M));
        assertTrue(GameInput.matchesKey(GameAction.MAP, Input.Keys.TAB));

        assertTrue(GameInput.matchesKey(GameAction.PAUSE, Input.Keys.ESCAPE));
        assertTrue(GameInput.matchesKey(GameAction.HELP_CONTROLS, Input.Keys.H));
        assertTrue(GameInput.matchesKey(GameAction.HELP_CONTROLS, Input.Keys.F1));

        assertTrue(GameInput.matchesKey(GameAction.TOGGLE_GENDER, Input.Keys.G));
        assertTrue(GameInput.matchesKey(GameAction.TOGGLE_PARAMETERS, Input.Keys.P));
        assertTrue(GameInput.matchesKey(GameAction.CYCLE_TONEMAP, Input.Keys.T));
        assertTrue(GameInput.matchesKey(GameAction.CONFIRM, Input.Keys.ENTER));
        assertTrue(GameInput.matchesKey(GameAction.CONFIRM, Input.Keys.SPACE));
        assertTrue(GameInput.matchesKey(GameAction.CANCEL, Input.Keys.ESCAPE));
        assertTrue(GameInput.matchesKey(GameAction.RESTART, Input.Keys.R));
    }

    @Test
    @DisplayName("GameInput: Virtual mouse position handles null headless state safely")
    public void testVirtualMouseSafeHandling() {
        float[] pos = GameInput.getVirtualMousePos(1600f, 900f);
        assertNotNull(pos);
        assertEquals(2, pos.length);
    }

    @Test
    @DisplayName("LevelConfig: Campaign progression seamlessly unlocks Levels 1 through 6")
    public void testCampaignLevelProgression() {
        assertEquals(1, LevelConfig.getUnlockedLevel());
        assertTrue(LevelConfig.isLevelUnlocked(1));
        assertFalse(LevelConfig.isLevelUnlocked(2));
        assertFalse(LevelConfig.isLevelUnlocked(3));

        // Complete Level 1 -> Unlocks Level 2
        LevelConfig.unlockNextLevel(1);
        assertEquals(2, LevelConfig.getUnlockedLevel());
        assertTrue(LevelConfig.isLevelUnlocked(2));
        assertFalse(LevelConfig.isLevelUnlocked(3));

        // Complete Level 2 -> Unlocks Level 3
        LevelConfig.unlockNextLevel(2);
        assertEquals(3, LevelConfig.getUnlockedLevel());
        assertTrue(LevelConfig.isLevelUnlocked(3));

        // Complete Level 3 -> Unlocks Level 4
        LevelConfig.unlockNextLevel(3);
        assertEquals(4, LevelConfig.getUnlockedLevel());
        assertTrue(LevelConfig.isLevelUnlocked(4));

        // Complete Level 4 -> Unlocks Level 5
        LevelConfig.unlockNextLevel(4);
        assertEquals(5, LevelConfig.getUnlockedLevel());
        assertTrue(LevelConfig.isLevelUnlocked(5));

        // Complete Level 5 -> Unlocks Level 6
        LevelConfig.unlockNextLevel(5);
        assertEquals(6, LevelConfig.getUnlockedLevel());
        assertTrue(LevelConfig.isLevelUnlocked(6));
    }

    @Test
    @DisplayName("RenderPipelineConfig: Graphics tier cycles correctly (HIGH -> LOW -> MEDIUM -> HIGH)")
    public void testGraphicsTierCycling() {
        RenderPipelineConfig.activeGraphicsTier = GraphicsTier.HIGH;
        String nextTier = RenderPipelineConfig.cycleGraphicsTier();
        assertEquals(GraphicsTier.LOW.displayName, nextTier);
        assertEquals(GraphicsTier.LOW, RenderPipelineConfig.activeGraphicsTier);

        nextTier = RenderPipelineConfig.cycleGraphicsTier();
        assertEquals(GraphicsTier.MEDIUM.displayName, nextTier);
        assertEquals(GraphicsTier.MEDIUM, RenderPipelineConfig.activeGraphicsTier);

        nextTier = RenderPipelineConfig.cycleGraphicsTier();
        assertEquals(GraphicsTier.HIGH.displayName, nextTier);
        assertEquals(GraphicsTier.HIGH, RenderPipelineConfig.activeGraphicsTier);
    }

    @Test
    @DisplayName("RenderPipelineConfig: Tone mapping modes return valid human-readable names")
    public void testToneMappingNames() {
        RenderPipelineConfig.toneMappingMode = 1;
        assertEquals("ACES Filmic (Cinematic)", RenderPipelineConfig.getToneMappingName());

        RenderPipelineConfig.toneMappingMode = 2;
        assertEquals("Extended Reinhard", RenderPipelineConfig.getToneMappingName());

        RenderPipelineConfig.toneMappingMode = 0;
        assertEquals("Linear / Bypass", RenderPipelineConfig.getToneMappingName());
    }
}
