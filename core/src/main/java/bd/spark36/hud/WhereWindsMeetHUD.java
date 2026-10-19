package bd.spark36.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import bd.spark36.character.PlayerController;
import bd.spark36.input.GameInput;
import bd.spark36.render.RenderPipelineConfig;
import bd.spark36.world.JulyMemorials;
import bd.spark36.world.JulyMemorials.MemorialEntry;
import bd.spark36.world.TextureFactory;

/**
 * 1:1 Recreation of the Where Winds Meet HUD matching game_exact_ui_design_1790197130736.jpg:
 * - Top-Left: Active mission card with glowing gold progress bar, distance meter, and waypoint compass.
 * - Top-Right: 8-point antique faceted 3D nautical compass star with CURZON HALL waypoint ticker.
 * - Bottom-Left: Fluid curved tapered health bar with gold filigree wing and stamina bar.
 * - Center: Speech-bubble prompt [E] INSPECT HISTORICAL ARCHIVE — 36 JULY MOVEMENT with pointer beak.
 * - Bottom-Right: Individual glassmorphic keycaps for WASD, Shift, Space, and E.
 */
public class WhereWindsMeetHUD implements Disposable {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final FontRenderer fonts;

    // Theme Colors (Ultra-Transparent Glassmorphism matching concept board)
    private final Color glassBg = new Color(0.02f, 0.04f, 0.06f, 0.16f);
    private final Color goldBorder = new Color(0.92f, 0.76f, 0.32f, 0.88f);
    private final Color goldAccent = new Color(1.0f, 0.85f, 0.40f, 1f);
    private final Color goldDark = new Color(0.55f, 0.42f, 0.18f, 1f);
    private final Color goldMuted = new Color(0.68f, 0.58f, 0.32f, 0.70f);
    private final Color healthRed = new Color(0.85f, 0.16f, 0.18f, 1f);
    private final Color healthBg = new Color(0.25f, 0.06f, 0.06f, 0.85f);
    private final Color staminaGreen = new Color(0.18f, 0.85f, 0.62f, 1f); // Jade cyan
    private final Color staminaBg = new Color(0.06f, 0.22f, 0.16f, 0.85f);

    // ── Glass HUD panel theme (mission card, checklist, compass tag, map button, keycaps) ──
    // Luxurious smoked obsidian glass: vertical gradient ~0.78 at top to ~0.65 at bottom
    private final Color panelGlassTop = new Color(0.04f, 0.06f, 0.09f, 0.78f);
    private final Color panelGlassBottom = new Color(0.02f, 0.03f, 0.05f, 0.65f);
    private final Color panelSheen = new Color(1.0f, 0.86f, 0.45f, 0.05f);      // faint warm top sheen
    private final Color panelSheenClear = new Color(1.0f, 0.86f, 0.45f, 0f);
    private final Color panelGlow = new Color(1.0f, 0.80f, 0.35f, 0.07f);       // soft outer gold halo
    private final Color panelBorder = new Color(0.98f, 0.80f, 0.36f, 0.90f);    // crisp thin gold line
    private final Color textShadow = new Color(0f, 0f, 0f, 0.92f);
    private final Color textOutline = new Color(0f, 0f, 0f, 0.45f);
    private final Color activeRowGold = new Color(1.0f, 0.86f, 0.38f, 1f);
    private final Color doneRowColor = new Color(0.55f, 0.85f, 0.58f, 0.70f);
    private final Color upcomingRowColor = new Color(1f, 1f, 1f, 1f);
    private final Color tmpColor = new Color();
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout glyphLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    private static final String[] CHECKLIST_NAMES = {
        "1. Curzon Hall Central Arcade",
        "2. Aparajeyo Bangla (Arts Plaza)",
        "3. TSC Raju Anti-Terrorism Sculpture",
        "4. Central Library & Hakim Chattar",
        "5. Teacher-Student Centre (TSC)"
    };

    // States
    private MemorialEntry activeModalEntry = null;
    private boolean isMapOpen = false;
    private boolean parametersVisible = true;

    public void toggleParameters() {
        parametersVisible = !parametersVisible;
    }

    public boolean areParametersVisible() {
        return parametersVisible;
    }

    public void setParametersVisible(boolean visible) {
        this.parametersVisible = visible;
    }
    private boolean isPauseMenuOpen = false;
    private boolean isHelpControlsOpen = false;
    private boolean isExitConfirmOpen = false;
    private int pauseMenuSelection = 0; // 0=Resume, 1=Codex, 2=Graphics, 3=ToneMap, 4=Restart, 5=MainMenu, 6=Quit
    private int exitConfirmSelection = 0; // 0=Cancel, 1=Quit Game
    private boolean isVictoryOpen = false;
    private boolean victoryShown = false;
    private boolean congratulationsPopupActive = false;
    private float congratulationsPopupTimer = 0f;
    private float victoryTransitionTimer = 0f;
    private float victoryAutoAdvanceTimer = 0f;
    private float animTime = 0f;

    // Toast Notification Banner (triggered on memorial inspection)
    private float missionBannerTime = 0f;
    private String missionBannerTitle = "";
    private int missionBannerCount = 0;

    // Exit actions: 0=none, 1=return to main menu, 2=quit game, 3=replay level
    private int exitAction = 0;

    // Boundary Stone (1921) Heritage Marker
    private final MemorialEntry boundaryStoneEntry = new MemorialEntry(
        99,
        "HISTORIC DU BOUNDARY STONE (1921)",
        "Founding Era — July 1, 1921",
        "Curzon Hall Heritage Precinct",
        "This weathered granite boundary marker demarcates the historic eastern perimeter of Dhaka University, established in 1921. For over a century, these grounds have witnessed generations of courageous students defending truth, freedom, and equal rights—culminating in the July 2024 Mass Uprising and the birth of 36 July.",
        bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS
    );

    private TextureFactory textures = null;
    private com.badlogic.gdx.graphics.Texture campusMapTex = null;
    private boolean campusMapLoadTried = false;

    // Antagonist Boss and Combat status
    private bd.spark36.character.HelmetGoon helmetGoon = null;
    private boolean isDisqualified = false;
    private String disqualifyReason = "VIOLATION: You assaulted an innocent campus student!";
    private boolean isDefeated = false;

    // Active campaign level integration
    private bd.spark36.level.CampaignLevel activeLevel = null;

    public void setActiveLevel(bd.spark36.level.CampaignLevel level) {
        this.activeLevel = level;
    }

    public bd.spark36.level.CampaignLevel getActiveLevel() {
        return activeLevel;
    }

    public MemorialEntry getBoundaryStoneEntry() {
        return boundaryStoneEntry;
    }

    // ── HUD Scaled Viewport & Anchor Layout (Fix Pack 01 Item 2) ──
    public static class HUDLayout {
        public float virtW;
        public float virtH;
        public float safeMarginX;
        public float safeMarginY;

        // Top-Left: Mission Card
        public float cardX, cardY, cardW, cardH;

        // Top-Center: Boss / Event Health Bar
        public float bossX, bossY, bossW, bossH;

        // Top-Right: Compass Rose
        public float compassCx, compassCy, compassR;
        // Top-Right: Waypoint Pill
        public float pillX, pillY, pillW, pillH;
        // Top-Right: Tactical Map HUD Button
        public float mapBtnX, mapBtnY, mapBtnW, mapBtnH;

        // Bottom-Left: Mission Parameters Checklist
        public float checkX, checkY, checkW, checkH;

        // Bottom-Center: Dialogue / Challenge Subtitle Box
        public float dialX, dialY, dialW, dialH;

        // Bottom-Right: Keycaps Cluster
        public float keyBaseX, keyBaseY, keyClusterW, keyClusterH;
        public float hintLineY;

        public static HUDLayout compute(float backbufferW, float backbufferH, float scale) {
            HUDLayout l = new HUDLayout();
            float s = Math.max(0.5f, scale);
            l.virtW = 1920f / s;
            l.virtH = (1920f * (backbufferH / Math.max(1f, backbufferW))) / s;

            // Safe margins: at least 3.5% of virtual width & height
            l.safeMarginX = Math.max(50f, l.virtW * 0.035f);
            l.safeMarginY = Math.max(38f, l.virtH * 0.035f);

            // 1. Top-Left Mission Card
            l.cardW = 520f;
            l.cardH = 158f;
            l.cardX = l.safeMarginX;
            l.cardY = l.virtH - l.safeMarginY - l.cardH;

            // 2. Top-Center Boss Bar
            l.bossW = 540f;
            l.bossH = 46f;
            l.bossX = (l.virtW - l.bossW) / 2f;
            l.bossY = l.virtH - l.safeMarginY - l.bossH;

            // 3. Top-Right Compass Rose & Controls
            l.compassR = 64f;
            l.compassCx = l.virtW - l.safeMarginX - l.compassR;
            l.compassCy = l.virtH - l.safeMarginY - l.compassR - 4f;

            // Waypoint Ticker Pill (left of compass, aligned on compass equator)
            l.pillW = 168f;
            l.pillH = 28f;
            l.pillX = l.compassCx - l.compassR - l.pillW - 14f;
            l.pillY = l.compassCy - l.pillH / 2f;

            // Tactical Map HUD Button (stacked vertically below compass rose with generous margin)
            l.mapBtnW = 180f;
            l.mapBtnH = 34f;
            l.mapBtnX = l.compassCx + l.compassR - l.mapBtnW;
            l.mapBtnY = l.compassCy - l.compassR - 48f;

            // 4. Bottom-Left Mission Parameters Checklist
            l.checkW = 400f;
            l.checkH = 162f;
            l.checkX = l.safeMarginX;
            l.checkY = l.safeMarginY;

            // 5. Bottom-Center Cinematic Subtitle / Challenge Dialogue
            l.dialW = 860f;
            l.dialH = 82f;
            l.dialX = (l.virtW - l.dialW) / 2f;
            l.dialY = l.safeMarginY + 20f;

            // 6. Bottom-Right Keycaps Cluster
            l.keyClusterW = 285f;
            l.keyClusterH = 118f;
            l.keyBaseX = l.virtW - l.safeMarginX - l.keyClusterW;
            l.keyBaseY = l.safeMarginY + 28f;
            l.hintLineY = l.safeMarginY + 6f;

            return l;
        }
    }

    private float uiScale = 1.0f;
    private boolean debugOverlayEnabled = false;
    private HUDLayout currentLayout = HUDLayout.compute(1920f, 1080f, 1.0f);

    public void setUiScale(float scale) {
        this.uiScale = MathUtils.clamp(scale, 0.75f, 1.5f);
    }

    public float getUiScale() {
        return uiScale;
    }

    public void toggleDebugOverlay() {
        debugOverlayEnabled = !debugOverlayEnabled;
    }

    public boolean isDebugOverlayEnabled() {
        return debugOverlayEnabled;
    }

    public void setDebugOverlayEnabled(boolean enabled) {
        this.debugOverlayEnabled = enabled;
    }

    // Cinematic finish screen background texture
    private com.badlogic.gdx.graphics.Texture cinematicFinishTex = null;
    private boolean cinematicFinishLoadTried = false;

    public void setHelmetGoon(bd.spark36.character.HelmetGoon goon) {
        this.helmetGoon = goon;
    }

    public bd.spark36.character.HelmetGoon getHelmetGoon() {
        return helmetGoon;
    }

    public void setDisqualified(boolean disq, String reason) {
        this.isDisqualified = disq;
        if (reason != null && !reason.isEmpty()) {
            this.disqualifyReason = reason;
        }
    }

    public boolean isDisqualified() {
        return isDisqualified;
    }

    public void setDefeated(boolean def) {
        this.isDefeated = def;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    private bd.spark36.level.DialogueRepository.DialogueLine activeNpcDialogue = null;
    private float npcDialogueTimer = 0f;
    private final bd.spark36.dialogue.DialogueManager dialogueManager = new bd.spark36.dialogue.DialogueManager();

    public bd.spark36.dialogue.DialogueManager getDialogueManager() {
        return dialogueManager;
    }

    public void startDialogueSequence(bd.spark36.dialogue.DialogueSequence sequence) {
        clearNpcDialogue();
        dialogueManager.startDialogue(sequence);
    }

    public void showNpcDialogue(bd.spark36.level.DialogueRepository.DialogueLine line, float duration) {
        dialogueManager.cancelDialogue();
        this.activeNpcDialogue = line;
        this.npcDialogueTimer = duration;
    }

    public void clearNpcDialogue() {
        this.activeNpcDialogue = null;
        this.npcDialogueTimer = 0f;
    }

    public boolean isNpcDialogueActive() {
        return (activeNpcDialogue != null && npcDialogueTimer > 0f) || (dialogueManager != null && dialogueManager.isDialogueActive());
    }

    public void triggerMissionToast(String title, int count) {
        this.missionBannerTitle = title;
        this.missionBannerCount = count;
        this.missionBannerTime = 6.0f;
        this.checkpointFlashAlpha = 1.0f;
        bd.spark36.audio.SoundManager.get().playObjective();
    }

    public void triggerVictory() {
        this.congratulationsPopupActive = true;
        this.congratulationsPopupTimer = 3.2f;
        this.victoryTransitionTimer = 2.4f;
        this.victoryShown = true;
        this.isVictoryOpen = false;
        this.victoryAutoAdvanceTimer = 5.0f;
        int currentLvl = (activeLevel != null) ? activeLevel.getLevelNumber() : 1;
        bd.spark36.level.LevelConfig.unlockNextLevel(currentLvl);
        bd.spark36.audio.SoundManager.get().playBell();
    }

    public boolean isCongratulationsPopupActive() {
        return congratulationsPopupActive;
    }

    private com.badlogic.gdx.graphics.Texture getCinematicFinishTexture() {
        if (!cinematicFinishLoadTried) {
            cinematicFinishLoadTried = true;
            try {
                if (Gdx.files.internal("cinematic_finish_bg.png").exists()) {
                    cinematicFinishTex = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("cinematic_finish_bg.png"));
                } else if (Gdx.files.internal("assets/cinematic_finish_bg.png").exists()) {
                    cinematicFinishTex = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("assets/cinematic_finish_bg.png"));
                }
            } catch (Exception e) {
                Gdx.app.log("HUD", "Could not load cinematic finish background: " + e.getMessage());
            }
        }
        return cinematicFinishTex;
    }

    // ── AAA Visual FX Fields ──────────────────────────────────────────────────
    // Checkpoint flash: brief full-screen gold flash when a memorial is inspected
    private float checkpointFlashAlpha = 0f;
    private float checkpointFlashDecay = 3.5f;    // alpha/sec decay rate

    // Sprint side-trail: subtle screen-edge blur/darkening while sprinting
    private float sprintTrailIntensity = 0f;

    // Objective ring spin angle
    private float objectiveRingAngle = 0f;

    // Crosshair breath pulse
    private float breathPhase = 0f;


    public WhereWindsMeetHUD(FontRenderer fontRenderer) {
        this.fonts = fontRenderer;
    }

    public void setTextures(TextureFactory textures) {
        this.textures = textures;
    }

    public boolean isMapOpen() {
        return isMapOpen;
    }

    public void openMapForTest() {
        this.isMapOpen = true;
    }

    public void update(float delta, PlayerController player, JulyMemorials memorials) {
        animTime += delta;

        if (npcDialogueTimer > 0f) {
            npcDialogueTimer -= delta;
            if (npcDialogueTimer <= 0f) {
                activeNpcDialogue = null;
            }
        }

        // ── VFX Animations ─────────────────────────────────────────────────
        // Checkpoint flash decay
        if (checkpointFlashAlpha > 0f) {
            checkpointFlashAlpha -= checkpointFlashDecay * delta;
            if (checkpointFlashAlpha < 0f) checkpointFlashAlpha = 0f;
        }
        // Sprint trail: build up when sprinting, fade when not
        float sprintTarget = player.isSprinting() && player.isMoving() ? 1f : 0f;
        sprintTrailIntensity += (sprintTarget - sprintTrailIntensity) * Math.min(1f, 5f * delta);
        // Objective ring spin
        objectiveRingAngle = (objectiveRingAngle + 35f * delta) % 360f;
        // Breath pulse
        breathPhase += delta * 1.8f;


        if (missionBannerTime > 0f && activeModalEntry == null) {
            missionBannerTime -= delta;
        }

        // Celebratory Congratulations Toast & Victory Screen transition
        if (congratulationsPopupActive) {
            congratulationsPopupTimer -= delta;
            if (victoryTransitionTimer > 0f) {
                victoryTransitionTimer -= delta;
                if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) && victoryTransitionTimer < 2.0f) {
                    victoryTransitionTimer = 0f;
                }
                if (victoryTransitionTimer <= 0f) {
                    isVictoryOpen = true;
                    victoryAutoAdvanceTimer = 5.0f;
                }
            }
            if (congratulationsPopupTimer <= 0f) {
                congratulationsPopupActive = false;
            }
        }

        // Debug overlay toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) || Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) {
            toggleDebugOverlay();
        }

        // Mouse click navigation for Tactical Map button and Close buttons
        float backbufferW = (float) Gdx.graphics.getBackBufferWidth();
        float backbufferH = (float) Gdx.graphics.getBackBufferHeight();
        currentLayout = HUDLayout.compute(backbufferW, backbufferH, uiScale);
        float virtW = currentLayout.virtW;
        float virtH = currentLayout.virtH;
        float[] vMouse = GameInput.getVirtualMousePos(virtW, virtH);
        float mouseX = vMouse[0];
        float mouseY = vMouse[1];

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (isVictoryOpen) {
                float vw = 860f;
                float vh = 580f;
                float vx = (virtW - vw) / 2f;
                float vy = (virtH - vh) / 2f;
                float btnY = vy + 38f;
                float btnH = 46f;
                float b1X = vx + 45f, b1W = 230f;
                float b2X = vx + 315f, b2W = 230f;
                float b3X = vx + 585f, b3W = 230f;
                if (mouseX >= b1X && mouseX <= b1X + b1W && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 1; // Return to Main Menu
                    isVictoryOpen = false;
                    victoryShown = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                } else if (mouseX >= b2X && mouseX <= b2X + b2W && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 4; // Advance to next level
                    isVictoryOpen = false;
                    victoryShown = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                } else if (mouseX >= b3X && mouseX <= b3X + b3W && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 2; // Quit Game
                    isVictoryOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            } else if (isDisqualified || isDefeated) {
                float dw = 780f;
                float dh = 420f;
                float dx = (virtW - dw) / 2f;
                float dy = (virtH - dh) / 2f;
                float btnY = dy + 35f;
                float btnH = 46f;
                if (mouseX >= dx + 90f && mouseX <= dx + 330f && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 3; // Retry
                    isDisqualified = false;
                    isDefeated = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                } else if (mouseX >= dx + 450f && mouseX <= dx + 690f && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 1; // Menu
                    isDisqualified = false;
                    isDefeated = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            } else if (isExitConfirmOpen) {
                // Click Cancel or Quit to desktop in Exit Confirmation modal
                float cw = 640f, ch = 280f;
                float cx = (virtW - cw) / 2f, cy = (virtH - ch) / 2f;
                float btnY = cy + 34f, btnH = 48f;
                float b0X = cx + 45f, b0W = 250f;
                float b1X = cx + cw - 295f, b1W = 250f;
                if (mouseX >= b0X && mouseX <= b0X + b0W && mouseY >= btnY && mouseY <= btnY + btnH) {
                    isExitConfirmOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                } else if (mouseX >= b1X && mouseX <= b1X + b1W && mouseY >= btnY && mouseY <= btnY + btnH) {
                    exitAction = 2; // Quit Game
                    isPauseMenuOpen = false;
                    isExitConfirmOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            } else if (isHelpControlsOpen) {
                float hw = 1000f, hh = 560f;
                float hx = (virtW - hw) / 2f, hy = (virtH - hh) / 2f;
                float closeBtnX = hx + hw - 160f, closeBtnY = hy + 20f, closeBtnW = 130f, closeBtnH = 40f;
                if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + closeBtnH) {
                    isHelpControlsOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            } else if (isPauseMenuOpen) {
                float pw = 980f, ph = 520f;
                float px = (virtW - pw) / 2f, py = (virtH - ph) / 2f;
                float dividerX = px + 390f;
                float btnX = px + 28f, btnW = dividerX - px - 46f, btnH = 38f;
                float startY = py + ph - 105f;
                float spacingY = 46f;
                for (int i = 0; i < 7; i++) {
                    float by = startY - i * spacingY;
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= by && mouseY <= by + btnH) {
                        pauseMenuSelection = i;
                        bd.spark36.audio.SoundManager.get().playClick();
                        if (i == 0) {
                            isPauseMenuOpen = false;
                        } else if (i == 1) {
                            isPauseMenuOpen = false;
                            isHelpControlsOpen = true;
                        } else if (i == 2) {
                            RenderPipelineConfig.cycleGraphicsTier();
                        } else if (i == 3) {
                            RenderPipelineConfig.toneMappingMode = (RenderPipelineConfig.toneMappingMode + 1) % 3;
                        } else if (i == 4) {
                            exitAction = 3; // Restart level
                            isPauseMenuOpen = false;
                        } else if (i == 5) {
                            exitAction = 1; // Return to main menu
                            isPauseMenuOpen = false;
                        } else if (i == 6) {
                            isExitConfirmOpen = true;
                            exitConfirmSelection = 0;
                        }
                        break;
                    }
                }
            } else if (isMapOpen) {
                // Click close button on map
                float cbX = virtW - 200f;
                float cbY = virtH - 65f;
                if (mouseX >= cbX && mouseX <= cbX + 170f && mouseY >= cbY && mouseY <= cbY + 45f) {
                    isMapOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            } else if (activeModalEntry == null && !isPauseMenuOpen && !isVictoryOpen && !isDisqualified && !isDefeated && !isHelpControlsOpen) {
                // Click tactical map HUD button
                if (mouseX >= currentLayout.mapBtnX && mouseX <= currentLayout.mapBtnX + currentLayout.mapBtnW &&
                    mouseY >= currentLayout.mapBtnY && mouseY <= currentLayout.mapBtnY + currentLayout.mapBtnH) {
                    isMapOpen = true;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            }
        }

        // Mouse hover tracking for pause menu & exit confirmation
        if (isExitConfirmOpen) {
            float cw = 640f, ch = 280f;
            float cx = (virtW - cw) / 2f, cy = (virtH - ch) / 2f;
            float btnY = cy + 34f, btnH = 48f;
            if (mouseY >= btnY && mouseY <= btnY + btnH) {
                if (mouseX >= cx + 45f && mouseX <= cx + 295f) {
                    exitConfirmSelection = 0;
                } else if (mouseX >= cx + cw - 295f && mouseX <= cx + cw - 45f) {
                    exitConfirmSelection = 1;
                }
            }
        } else if (isPauseMenuOpen) {
            float pw = 980f, ph = 520f;
            float px = (virtW - pw) / 2f, py = (virtH - ph) / 2f;
            float dividerX = px + 360f;
            float btnX = px + 28f, btnW = dividerX - px - 46f, btnH = 44f;
            float startY = py + ph - 110f;
            float spacingY = 52f;
            for (int i = 0; i < 5; i++) {
                float by = startY - i * spacingY;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= by && mouseY <= by + btnH) {
                    pauseMenuSelection = i;
                    break;
                }
            }
        }

        // Handle Disqualification Screen input
        if (isDisqualified) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                exitAction = 3; // Replay Level
                isDisqualified = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                exitAction = 1; // Return to Main Menu
                isDisqualified = false;
            }
            return;
        }

        // Handle Defeat Screen input
        if (isDefeated) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                exitAction = 3; // Retry combat
                isDefeated = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                exitAction = 1; // Return to Main Menu
                isDefeated = false;
            }
            return;
        }

        // Handle Victory Screen input & auto-advance timer
        if (isVictoryOpen) {
            victoryAutoAdvanceTimer -= delta;
            int lvlNum = (activeLevel != null) ? activeLevel.getLevelNumber() : 1;

            // Auto-advance when countdown expires
            if (victoryAutoAdvanceTimer <= 0f) {
                exitAction = (lvlNum < 6) ? 4 : 1;
                isVictoryOpen = false;
                victoryShown = false;
                bd.spark36.audio.SoundManager.get().playClick();
                return;
            }

            // [ENTER], [SPACE], or [N] immediately advances to next level / finishes campaign
            if (GameInput.isJustPressed(GameInput.GameAction.CONFIRM) || Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                exitAction = (lvlNum < 6) ? 4 : 1;
                isVictoryOpen = false;
                victoryShown = false;
                bd.spark36.audio.SoundManager.get().playClick();
                return;
            }
            // [M] or [ESCAPE] returns to Main Menu
            if (Gdx.input.isKeyJustPressed(Input.Keys.M) || GameInput.isJustPressed(GameInput.GameAction.CANCEL) || GameInput.isJustPressed(GameInput.GameAction.PAUSE)) {
                exitAction = 1;
                isVictoryOpen = false;
                victoryShown = false;
                bd.spark36.audio.SoundManager.get().playClick();
                return;
            }
            // [R] replays current chapter
            if (GameInput.isJustPressed(GameInput.GameAction.RESTART)) {
                exitAction = 3;
                isVictoryOpen = false;
                victoryShown = false;
                bd.spark36.audio.SoundManager.get().playClick();
                return;
            }
            // [X] exits game
            if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                exitAction = 2;
                isVictoryOpen = false;
                bd.spark36.audio.SoundManager.get().playClick();
                return;
            }
            return;
        }

        // Shortcut key [K] for instant mission completion / testing victory
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && !isVictoryOpen && activeModalEntry == null) {
            for (MemorialEntry e : memorials.getEntries()) {
                e.inspected = true;
            }
            missionBannerTime = 6.0f;
            missionBannerTitle = "ALL HISTORICAL ARCHIVES";
            missionBannerCount = 5;
            isVictoryOpen = true;
            victoryShown = true;
            victoryAutoAdvanceTimer = 5.0f;
            int currentLvl = (activeLevel != null) ? activeLevel.getLevelNumber() : 1;
            bd.spark36.level.LevelConfig.unlockNextLevel(currentLvl);
            return;
        }

        if (dialogueManager.isDialogueActive()) {
            dialogueManager.update(delta);
            if (dialogueManager.handleInput()) {
                return;
            }
        }

        if (activeLevel != null) {
            if (GameInput.isJustPressed(GameInput.GameAction.INTERACT)) {
                if (activeModalEntry != null) {
                    activeModalEntry = null;
                } else {
                    activeLevel.onInteract(player.getPosition(), player, this);
                }
            }
        } else {
            MemorialEntry nearby = (memorials != null) ? memorials.getNearbyMemorial(player.getPosition()) : null;
            if (nearby == null && player.getPosition().dst(bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
                nearby = boundaryStoneEntry;
            }

            if (nearby != null && GameInput.isJustPressed(GameInput.GameAction.INTERACT)) {
                if (activeModalEntry == null) {
                    activeModalEntry = nearby;
                    nearby.inspected = true;
                    missionBannerTitle = nearby.title;
                    missionBannerCount = memorials.getInspectedCount();
                } else {
                    activeModalEntry = null;
                    missionBannerTime = 6.0f;
                    checkpointFlashAlpha = 1.0f;

                    if (memorials.isAllInspected()) {
                        if (bd.spark36.level.LevelConfig.isCombatEnabled()) {
                            boolean goonKnockedOut = (helmetGoon != null && helmetGoon.getState() == bd.spark36.character.HelmetGoon.State.KNOCKED_OUT);
                            if (goonKnockedOut && !victoryShown) {
                                triggerVictory();
                            } else if (!victoryShown) {
                                missionBannerTitle = "CLIMAX: CONFRONT THE AGGRESSOR";
                                missionBannerCount = 5;
                            }
                        } else {
                            if (!victoryShown) {
                                triggerVictory();
                                missionBannerTitle = "CAMPUS RECONNAISSANCE COMPLETE";
                                missionBannerCount = 5;
                            }
                        }
                    }
                }
            }
        }

        if (activeModalEntry != null && (GameInput.isJustPressed(GameInput.GameAction.CANCEL) || GameInput.isJustPressed(GameInput.GameAction.PAUSE))) {
            activeModalEntry = null;
            missionBannerTime = 4.0f;
            bd.spark36.audio.SoundManager.get().playClick();
        }

        if (GameInput.isJustPressed(GameInput.GameAction.TOGGLE_PARAMETERS) && !isVictoryOpen && !isPauseMenuOpen && !isHelpControlsOpen && activeModalEntry == null) {
            parametersVisible = !parametersVisible;
            bd.spark36.audio.SoundManager.get().playClick();
        }

        if (GameInput.isJustPressed(GameInput.GameAction.HELP_CONTROLS) && !isVictoryOpen && !isPauseMenuOpen && activeModalEntry == null) {
            isHelpControlsOpen = !isHelpControlsOpen;
            if (isHelpControlsOpen) isMapOpen = false;
            bd.spark36.audio.SoundManager.get().playClick();
        } else if (isHelpControlsOpen && (GameInput.isJustPressed(GameInput.GameAction.CANCEL) || GameInput.isJustPressed(GameInput.GameAction.PAUSE))) {
            isHelpControlsOpen = false;
            bd.spark36.audio.SoundManager.get().playClick();
        }

        if (GameInput.isJustPressed(GameInput.GameAction.MAP) && activeModalEntry == null && !isVictoryOpen && !isHelpControlsOpen && !isDisqualified && !isDefeated) {
            isMapOpen = !isMapOpen;
            if (isMapOpen) isPauseMenuOpen = false;
            bd.spark36.audio.SoundManager.get().playClick();
        }

        if (isMapOpen && (GameInput.isJustPressed(GameInput.GameAction.CANCEL) || GameInput.isJustPressed(GameInput.GameAction.PAUSE))) {
            isMapOpen = false;
            bd.spark36.audio.SoundManager.get().playClick();
        } else if (activeModalEntry == null && !isMapOpen && !isHelpControlsOpen && !isVictoryOpen && !isDisqualified && !isDefeated && GameInput.isJustPressed(GameInput.GameAction.PAUSE)) {
            if (isExitConfirmOpen) {
                isExitConfirmOpen = false;
                bd.spark36.audio.SoundManager.get().playClick();
            } else {
                isPauseMenuOpen = !isPauseMenuOpen;
                if (isPauseMenuOpen) {
                    pauseMenuSelection = 0;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            }
        }

        if (isDisqualified || isDefeated) {
            if (GameInput.isJustPressed(GameInput.GameAction.RESTART) || GameInput.isJustPressed(GameInput.GameAction.CONFIRM)) {
                exitAction = 3; // Retry / Reset
            }
            if (GameInput.isJustPressed(GameInput.GameAction.CANCEL) || GameInput.isJustPressed(GameInput.GameAction.PAUSE)) {
                exitAction = 1; // Return to main menu
            }
        }

        // Pause menu actions & shortcuts
        if (isPauseMenuOpen) {
            if (isExitConfirmOpen) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)
                    || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                    exitConfirmSelection = 1 - exitConfirmSelection;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (GameInput.isJustPressed(GameInput.GameAction.CONFIRM)) {
                    if (exitConfirmSelection == 0) {
                        isExitConfirmOpen = false;
                        bd.spark36.audio.SoundManager.get().playClick();
                    } else {
                        exitAction = 2; // Quit Game -> Gdx.app.exit()
                        isPauseMenuOpen = false;
                        isExitConfirmOpen = false;
                        bd.spark36.audio.SoundManager.get().playClick();
                    }
                }
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    pauseMenuSelection = (pauseMenuSelection - 1 + 7) % 7;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    pauseMenuSelection = (pauseMenuSelection + 1) % 7;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (GameInput.isJustPressed(GameInput.GameAction.CONFIRM)) {
                    bd.spark36.audio.SoundManager.get().playClick();
                    if (pauseMenuSelection == 0) {
                        isPauseMenuOpen = false;
                    } else if (pauseMenuSelection == 1) {
                        isPauseMenuOpen = false;
                        isHelpControlsOpen = true;
                    } else if (pauseMenuSelection == 2) {
                        RenderPipelineConfig.cycleGraphicsTier();
                    } else if (pauseMenuSelection == 3) {
                        RenderPipelineConfig.toneMappingMode = (RenderPipelineConfig.toneMappingMode + 1) % 3;
                    } else if (pauseMenuSelection == 4) {
                        exitAction = 3; // Restart level
                        isPauseMenuOpen = false;
                    } else if (pauseMenuSelection == 5) {
                        exitAction = 1; // Return to main menu
                        isPauseMenuOpen = false;
                    } else if (pauseMenuSelection == 6) {
                        isExitConfirmOpen = true;
                        exitConfirmSelection = 0;
                    }
                }

                if (GameInput.isJustPressed(GameInput.GameAction.MAP)) {
                    isMapOpen = true;
                    isPauseMenuOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (GameInput.isJustPressed(GameInput.GameAction.HELP_CONTROLS)) {
                    isHelpControlsOpen = true;
                    isPauseMenuOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                    RenderPipelineConfig.cycleGraphicsTier();
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (GameInput.isJustPressed(GameInput.GameAction.RESTART)) {
                    exitAction = 3; // Restart Level
                    isPauseMenuOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (GameInput.isJustPressed(GameInput.GameAction.CYCLE_TONEMAP)) {
                    RenderPipelineConfig.toneMappingMode = (RenderPipelineConfig.toneMappingMode + 1) % 3;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                    exitAction = 1; // Return to main menu
                    isPauseMenuOpen = false;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                    isExitConfirmOpen = true;
                    exitConfirmSelection = 1;
                    bd.spark36.audio.SoundManager.get().playClick();
                }
            }
        }
    }

    /**
     * Returns and clears the pending exit action.
     * @return 0=none, 1=return to main menu, 2=quit game
     */
    public int consumeExitAction() {
        int action = exitAction;
        exitAction = 0;
        return action;
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw) {
        render(player, memorials, cameraYaw, null);
    }

    public void render(PlayerController player, JulyMemorials memorials, float cameraYaw, com.badlogic.gdx.graphics.Camera camera) {
        int backbufferW = Gdx.graphics.getBackBufferWidth();
        int backbufferH = Gdx.graphics.getBackBufferHeight();

        currentLayout = HUDLayout.compute((float) backbufferW, (float) backbufferH, uiScale);
        float w = currentLayout.virtW;
        float h = currentLayout.virtH;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        MemorialEntry nearest = memorials.getNextObjective(player.getPosition());
        float dstToNearest = nearest != null ? player.getPosition().dst(nearest.position) : 65.4f;

        // 0. Draw 3D In-World Waypoint Pin directly above next objective
        if (camera != null && !isModalOpen()) {
            if (activeLevel != null && activeLevel.getLevelNumber() > 1 && !activeLevel.isLevelComplete()) {
                Vector3 wp = activeLevel.getActiveWaypoint();
                String title = activeLevel.getMissionTitle();
                float dst = activeLevel.getWaypointDistance(player.getPosition());
                draw3DObjectivePinDirect(wp, title, dst, camera, w, h);
            } else if (nearest != null && memorials != null && !memorials.isAllInspected()) {
                draw3DObjectivePin(nearest, player, camera, w, h);
            }
        }

        // ── AAA VFX: Sprint Motion Trail + Checkpoint Flash ──────────────────
        beginShapes(ShapeType.Filled);

        // Sprint vignette: dark edges when sprinting for speed blur effect
        if (sprintTrailIntensity > 0.01f) {
            float trailAlpha = sprintTrailIntensity * 0.36f;
            // Left edge
            shapeRenderer.setColor(0.02f, 0.02f, 0.04f, trailAlpha);
            shapeRenderer.rect(0, 0, w * 0.12f, h);
            // Right edge
            shapeRenderer.rect(w * 0.88f, 0, w * 0.12f, h);
            // Bottom edge
            shapeRenderer.setColor(0.02f, 0.02f, 0.04f, trailAlpha * 0.6f);
            shapeRenderer.rect(0, 0, w, h * 0.08f);
            // Top edge
            shapeRenderer.rect(0, h * 0.92f, w, h * 0.08f);
        }

        // Checkpoint gold flash: brief full-screen warm golden pulse
        if (checkpointFlashAlpha > 0.01f) {
            float alpha = checkpointFlashAlpha * 0.48f;
            shapeRenderer.setColor(1.0f, 0.82f, 0.25f, alpha);
            shapeRenderer.rect(0, 0, w, h);
        }

        shapeRenderer.end();
        // ── End VFX ───────────────────────────────────────────────────────────

        // 1. Draw HUD Background Shapes & Ornate Geometry (only during normal gameplay)
        if (!isVictoryOpen && !isDisqualified && !isDefeated) {
            MemorialEntry nearby = (memorials != null) ? memorials.getNearbyMemorial(player.getPosition()) : null;
            if (nearby == null && player.getPosition().dst(bd.spark36.world.DhakaCampusWorld.BOUNDARY_STONE_POS) <= 3.2f) {
                nearby = boundaryStoneEntry;
            }
            boolean hasPrompt = (activeLevel != null) ? (activeLevel.getContextPrompt(player.getPosition()) != null) : (nearby != null);

            beginShapes(ShapeType.Filled);

            drawMissionCardBg(memorials, w, h);
            if (parametersVisible) {
                drawMissionChecklistBg(player, memorials, w, h);
            }
            drawAntiqueCompassRoseFilled(w, h, cameraYaw);
            drawKeycapsBg(w, h);
            if (!isMapOpen && !isPauseMenuOpen && activeModalEntry == null) {
                drawTacticalMapButtonBg(w, h);
            }

            if (hasPrompt && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
                drawSpeechBubblePromptBg(w, h);
            }
            shapeRenderer.end();

            // 2. Draw HUD Outlines, Accents & Filigree
            beginShapes(ShapeType.Line);
            drawMissionCardBorders(w, h);
            if (parametersVisible) {
                drawMissionChecklistBorders(player, memorials, w, h);
            }
            drawAntiqueCompassRoseLines(w, h, cameraYaw);
            drawKeycapsBorders(w, h);
            if (!isMapOpen && !isPauseMenuOpen && activeModalEntry == null) {
                drawTacticalMapButtonBorder(w, h);
            }

            if (hasPrompt && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
                drawSpeechBubblePromptBorders(w, h);
            }
            shapeRenderer.end();

            // 3. Draw Typography & Glyphs
            spriteBatch.begin();
            drawMissionCardText(player, memorials, nearest, dstToNearest, w, h);
            if (parametersVisible) {
                drawMissionChecklistText(player, memorials, w, h);
            } else {
                drawParametersHiddenPrompt(w, h);
            }
            drawAntiqueCompassRoseText(w, h, cameraYaw);
            drawKeycapsText(w, h);
            if (!isMapOpen && !isPauseMenuOpen && activeModalEntry == null) {
                drawTacticalMapButtonText(w, h);
            }

            if (hasPrompt && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
                drawSpeechBubblePromptText(player, nearby, w, h);
            }
            spriteBatch.end();
        }

        // 4. Toast Notification Banner (renders on top of HUD when active)
        if (!isVictoryOpen && !isDisqualified && !isDefeated && activeModalEntry == null && !isMapOpen && !isPauseMenuOpen) {
            if (congratulationsPopupActive) {
                renderCongratulationsPopup(w, h);
            } else {
                renderMissionCompleteBanner(w, h);
            }
        }

        // 4b. Boss Health Bar, Dialogue Prompt, and Combat Popups
        if (bd.spark36.level.LevelConfig.isCombatEnabled() && helmetGoon != null && !isVictoryOpen && !isDisqualified && !isDefeated && !isPauseMenuOpen && !isMapOpen && activeModalEntry == null
            && !congratulationsPopupActive && !helmetGoon.isKnockedOut()) {
            float dstGoon = player.getPosition().dst(helmetGoon.getPosition());
            if (helmetGoon.getState() != bd.spark36.character.HelmetGoon.State.STANDOFF || dstGoon <= 28f) {
                renderBossHealthBar(helmetGoon, w, h);
            }
            if (helmetGoon.isChallenging()) {
                renderBossChallengeDialogue(helmetGoon, w, h);
            }
            String combatText = helmetGoon.getCombatPopupText();
            if (combatText != null) {
                renderCombatFloatingPopup(combatText, helmetGoon.getCombatPopupColor(), w, h);
            }
        }

        // 4c. Interactive Student NPC Dialogue Subtitle Box
        bd.spark36.level.DialogueRepository.DialogueLine activeLine = (dialogueManager.isDialogueActive()) ?
            dialogueManager.getCurrentLine() : activeNpcDialogue;
        if (activeLine != null && !isVictoryOpen && !isDisqualified && !isDefeated && !isPauseMenuOpen && !isMapOpen && activeModalEntry == null) {
            renderStudentNpcDialogue(activeLine, w, h);
        }

        // 5. Overlays: Historical Archival Modal, Map, Pause Menu, Victory, Disqualification, or Defeat Screen
        if (isVictoryOpen) {
            renderVictoryScreen(w, h);
        } else if (isDisqualified) {
            renderDisqualificationScreen(w, h);
        } else if (isDefeated) {
            renderDefeatScreen(w, h);
        } else if (isHelpControlsOpen) {
            renderHelpControlsScreen(w, h);
        } else if (activeModalEntry != null) {
            renderMemorialModal(activeModalEntry, w, h);
        } else if (isMapOpen) {
            renderCampusMap(player, memorials, cameraYaw, w, h);
        } else if (isPauseMenuOpen) {
            renderPauseMenu(w, h);
        }

        // 6. Debug Bounds & Overlap Check Overlay (Toggle via F3 / Grave `)
        if (debugOverlayEnabled) {
            drawDebugOverlay(currentLayout, w, h);
        }
    }

    // ==========================================
    // GLASS PANEL HELPERS
    // ==========================================

    /**
     * Begins a ShapeRenderer pass with alpha blending forced on. SpriteBatch.end() disables GL_BLEND,
     * so blending must be re-enabled before every shape pass or translucent colors render opaque (ANGLE/GLES2).
     */
    private void beginShapes(ShapeType type) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(type);
    }

    /** Smooth 0..1..0 pulse: cosine wave passed through smoothstep so peaks/troughs ease in and out (no jitter). */
    private float easedPulse(float speed) {
        float t = 0.5f - 0.5f * MathUtils.cos(animTime * speed);
        return t * t * (3f - 2f * t);
    }

    /** Filled pass: thin soft outer glow + gradient smoked-glass body + faint warm top sheen. */
    private void fillGlassPanel(float x, float y, float pw, float ph) {
        // Outer glow: one thin feathered frame just outside the border
        fillFrame(x - 2.5f, y - 2.5f, pw + 5f, ph + 5f, 2f, panelGlow.r, panelGlow.g, panelGlow.b, panelGlow.a);

        // Gradient glass body (bottom-left, bottom-right, top-right, top-left)
        shapeRenderer.rect(x, y, pw, ph, panelGlassBottom, panelGlassBottom, panelGlassTop, panelGlassTop);

        // Top sheen strip fading downward
        float sheenH = Math.min(18f, ph * 0.35f);
        shapeRenderer.rect(x, y + ph - sheenH, pw, sheenH, panelSheenClear, panelSheenClear, panelSheen, panelSheen);
    }

    /** Filled rectangular frame of thickness t. */
    private void fillFrame(float x, float y, float fw, float fh, float t, float r, float g, float b, float a) {
        shapeRenderer.setColor(r, g, b, a);
        shapeRenderer.rect(x, y, fw, t);
        shapeRenderer.rect(x, y + fh - t, fw, t);
        shapeRenderer.rect(x, y + t, t, fh - 2f * t);
        shapeRenderer.rect(x + fw - t, y + t, t, fh - 2f * t);
    }

    /** Line pass: crisp 1px gold border with a gentle eased breathing brightness. */
    private void strokeGlassBorder(float x, float y, float bw, float bh) {
        float breathe = easedPulse(1.6f);
        shapeRenderer.setColor(panelBorder.r, panelBorder.g, panelBorder.b, 0.75f + 0.20f * breathe);
        shapeRenderer.rect(x, y, bw, bh);
    }

    /**
     * Readable text over the 3D world. Large fonts (title/header) get a soft dark outline + drop shadow;
     * small fonts get only a crisp 1px drop shadow (an outline smears small glyphs).
     */
    private void drawOutlined(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float x, float y, Color color) {
        drawOutlined(font, text, x, y, color.r, color.g, color.b, color.a);
    }

    private void drawOutlined(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float x, float y,
                              float r, float g, float b, float a) {
        x = Math.round(x);
        y = Math.round(y);
        boolean hasBuiltInOutline = font == fonts.titleFont || font == fonts.headerFont;
        if (!hasBuiltInOutline) {
            // Clean 1px drop shadow for fonts without built-in FreeType border
            font.setColor(0f, 0f, 0f, 0.85f * a);
            font.draw(spriteBatch, text, x + 1f, y - 1f);
        }
        font.setColor(r, g, b, a);
        font.draw(spriteBatch, text, x, y);
    }

    // ==========================================
    // 1. TOP-LEFT MISSION CARD (Matching Mockup)
    // ==========================================
    private void drawMissionCardBg(JulyMemorials memorials, float w, float h) {
        float cardX = currentLayout.cardX;
        float cardY = currentLayout.cardY;
        float cardW = currentLayout.cardW;
        float cardH = currentLayout.cardH;

        // See-through smoked glass background with soft gold halo
        fillGlassPanel(cardX, cardY, cardW, cardH);

        // Gold Diamond Icon for "ACTIVE MISSION"
        shapeRenderer.setColor(goldAccent);
        float dX = cardX + 18f;
        float dY = cardY + cardH - 18f;
        float dS = 5f;
        shapeRenderer.triangle(dX, dY + dS, dX + dS, dY, dX, dY - dS);
        shapeRenderer.triangle(dX, dY + dS, dX - dS, dY, dX, dY - dS);

        // Glowing Golden Progress Bar (Dynamic: fills as archives are inspected)
        float barX = cardX + 18f;
        float barY = cardY + 48f;
        float barW = cardW - 36f;
        float barH = 5f;

        // Track
        shapeRenderer.setColor(0.18f, 0.16f, 0.12f, 0.45f);
        shapeRenderer.rect(barX, barY, barW, barH);

        // Gold/Green Fill with Gleam based on actual mission progress
        int inspected = memorials.getInspectedCount();
        int total = memorials.getTotalCount();
        float progress = total > 0 ? (float) inspected / (float) total : 0f;
        float fillW = barW * progress;

        if (fillW > 0f) {
            shapeRenderer.setColor(inspected >= total ? Color.GREEN : goldAccent);
            shapeRenderer.rect(barX, barY, fillW, barH);
            // Soft glow above/below the fill
            shapeRenderer.setColor(1.0f, 0.85f, 0.40f, 0.14f);
            shapeRenderer.rect(barX, barY - 2f, fillW, barH + 4f);

            // Gleam particle (eased sweep, slows smoothly at both ends)
            float gleamPos = easedPulse(1.25f) * fillW;
            shapeRenderer.setColor(1.0f, 0.98f, 0.85f, 0.95f);
            shapeRenderer.circle(barX + gleamPos, barY + barH / 2f, 4f, 12);
        }

        // Waypoint Compass Needle Icon Circle (next to DISTANCE)
        float cX = cardX + cardW - 36f;
        float cY = cardY + 24f;
        shapeRenderer.setColor(0.08f, 0.18f, 0.22f, 0.40f);
        shapeRenderer.circle(cX, cY, 15f, 20);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cX, cY, 2.5f, 10);
    }

    private void drawMissionCardBorders(float w, float h) {
        float cardX = currentLayout.cardX;
        float cardY = currentLayout.cardY;
        float cardW = currentLayout.cardW;
        float cardH = currentLayout.cardH;

        // Crisp thin gold border with gentle breathing glow
        strokeGlassBorder(cardX, cardY, cardW, cardH);

        // Ornate Corner Brackets (⌜ ⌝ ⌞ ⌟)
        shapeRenderer.setColor(goldAccent);
        float cLen = 14f;

        // Top-Left ⌜
        shapeRenderer.line(cardX - 2, cardY + cardH + 2, cardX + cLen, cardY + cardH + 2);
        shapeRenderer.line(cardX - 2, cardY + cardH + 2, cardX - 2, cardY + cardH - cLen);

        // Top-Right ⌝
        shapeRenderer.line(cardX + cardW + 2, cardY + cardH + 2, cardX + cardW - cLen, cardY + cardH + 2);
        shapeRenderer.line(cardX + cardW + 2, cardY + cardH + 2, cardX + cardW + 2, cardY + cardH - cLen);

        // Bottom-Left ⌞
        shapeRenderer.line(cardX - 2, cardY - 2, cardX + cLen, cardY - 2);
        shapeRenderer.line(cardX - 2, cardY - 2, cardX - 2, cardY + cLen);

        // Bottom-Right ⌟
        shapeRenderer.line(cardX + cardW + 2, cardY - 2, cardX + cardW - cLen, cardY - 2);
        shapeRenderer.line(cardX + cardW + 2, cardY - 2, cardX + cardW + 2, cardY + cLen);

        // Waypoint Compass Needle Icon Ring
        float cX = cardX + cardW - 36f;
        float cY = cardY + 24f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cX, cY, 15f, 24);

        // Pointer needle (pointing towards Curzon Hall)
        float nAngle = (animTime * 1.5f) % 360f * MathUtils.degreesToRadians;
        shapeRenderer.line(cX, cY, cX + 11f * MathUtils.cos(nAngle), cY + 11f * MathUtils.sin(nAngle));
    }

    private void drawMissionCardText(PlayerController player, JulyMemorials memorials, MemorialEntry nearest, float dst, float w, float h) {
        float cardX = currentLayout.cardX;
        float cardY = currentLayout.cardY;
        float cardW = currentLayout.cardW;
        float cardH = currentLayout.cardH;

        if (activeLevel != null && activeLevel.getLevelNumber() > 1) {
            String missionTag = activeLevel.getMissionTag();
            drawOutlined(fonts.smallFont, missionTag, cardX + 18f, cardY + cardH - 18f, new Color(1.0f, 0.75f, 0.30f, 1f));

            String missionTitle = activeLevel.getMissionTitle();
            drawOutlined(fonts.headerFont, missionTitle, cardX + 18f, cardY + cardH - 42f, goldAccent);

            String missionDesc = activeLevel.getMissionTarget();
            drawOutlined(fonts.bodyFont, missionDesc, cardX + 18f, cardY + cardH - 74f, Color.WHITE);

            float activeDst = activeLevel.getWaypointDistance(player.getPosition());
            String distStr = String.format("DISTANCE: %.1f M", activeDst);
            drawOutlined(fonts.headerFont, distStr, cardX + 18f, cardY + 24f, goldAccent);
            return;
        }

        int ins = (memorials != null) ? memorials.getInspectedCount() : 0;
        int total = (memorials != null) ? memorials.getTotalCount() : 5;
        boolean allDone = ins >= total;
        boolean goonKnockedOut = (helmetGoon != null && helmetGoon.getState() == bd.spark36.character.HelmetGoon.State.KNOCKED_OUT);

        if (allDone && !goonKnockedOut) {
            if (bd.spark36.level.LevelConfig.isCombatEnabled()) {
                // Climax Combat Confrontation (Arena Mode)
                drawOutlined(fonts.smallFont, "CLIMAX OBJECTIVE: CONFRONTATION", cardX + 18f, cardY + cardH - 18f, new Color(1.0f, 0.45f, 0.25f, 1f));
                String bossTitle = "CONFRONT THE AGGRESSOR";
                drawOutlined(fonts.headerFont, bossTitle, cardX + 18f, cardY + cardH - 42f, goldAccent);
                drawOutlined(fonts.bodyFont, "Target: Curzon Hall Plaza — Disarm the Helmeted Attacker", cardX + 18f, cardY + cardH - 74f, Color.WHITE);
                float goonDst = (helmetGoon != null && helmetGoon.getPosition() != null) ?
                    (player != null ? player.getPosition().dst(helmetGoon.getPosition()) : dst) : dst;
                drawOutlined(fonts.headerFont, String.format("DISTANCE: %.1f M", goonDst), cardX + 18f, cardY + 24f, goldAccent);
            } else {
                // Peaceful Campus Reconnaissance Completion (Level 1 Story Mode)
                drawOutlined(fonts.smallFont, "ALL ARCHIVES UNLOCKED: 05 / 05", cardX + 18f, cardY + cardH - 18f, Color.GREEN);
                drawOutlined(fonts.headerFont, "RECONNAISSANCE COMPLETE", cardX + 18f, cardY + cardH - 42f, goldAccent);
                drawOutlined(fonts.bodyFont, "All 5 historical archives secured. Proceed to Curzon Hall promenade.", cardX + 18f, cardY + cardH - 74f, Color.WHITE);
                drawOutlined(fonts.headerFont, "STATUS: MISSION ACCOMPLISHED", cardX + 18f, cardY + 24f, goldAccent);
            }
        } else if (allDone && goonKnockedOut) {
            drawOutlined(fonts.smallFont, "ALL ARCHIVES UNLOCKED: 05 / 05", cardX + 18f, cardY + cardH - 18f, Color.GREEN);
            drawOutlined(fonts.headerFont, "LEVEL 1 ACCOMPLISHED", cardX + 18f, cardY + cardH - 42f, goldAccent);
            drawOutlined(fonts.bodyFont, "All 5 archives secured & Aggressor neutralized. Campus liberated!", cardX + 18f, cardY + cardH - 74f, Color.WHITE);
            drawOutlined(fonts.headerFont, "STATUS: COMPLETED", cardX + 18f, cardY + 24f, Color.GREEN);
        } else {
            // Standard archive progression
            String missionTag = String.format("ACTIVE MISSION: %02d / %02d", Math.min(ins + 1, total), total);
            drawOutlined(fonts.smallFont, missionTag, cardX + 18f, cardY + cardH - 18f, new Color(1.0f, 0.75f, 0.30f, 1f));

            String missionTitle = (nearest != null ? nearest.title : "EXPLORE CAMPUS");
            drawOutlined(fonts.headerFont, missionTitle, cardX + 18f, cardY + cardH - 42f, goldAccent);

            String missionDesc = (nearest != null ? "Target: " + nearest.location : "Investigate Dhaka University archives");
            drawOutlined(fonts.bodyFont, missionDesc, cardX + 18f, cardY + cardH - 74f, Color.WHITE);

            String distStr = String.format("DISTANCE: %.1f M", dst);
            drawOutlined(fonts.headerFont, distStr, cardX + 18f, cardY + 24f, goldAccent);
        }
    }

    // =======================================================
    // 2. TOP-RIGHT ANTIQUE 8-POINT COMPASS ROSE (Matching Mockup)
    // =======================================================
    private void drawAntiqueCompassRoseFilled(float w, float h, float yaw) {
        float cx = currentLayout.compassCx;
        float cy = currentLayout.compassCy;
        float r = currentLayout.compassR;

        // Circular transparent glass background
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.22f);
        shapeRenderer.circle(cx, cy, r, 48);

        // Waypoint Ticker Pill to the left ("◆ CURZON HALL  W")
        float pillW = currentLayout.pillW;
        float pillH = currentLayout.pillH;
        float pillX = currentLayout.pillX;
        float pillY = currentLayout.pillY;
        fillGlassPanel(pillX, pillY, pillW, pillH);

        // Diamond marker on ticker
        shapeRenderer.setColor(goldAccent);
        float dX = pillX + 14f;
        float dY = cy;
        float dS = 5.5f;
        shapeRenderer.triangle(dX, dY + dS, dX + dS, dY, dX, dY - dS);
        shapeRenderer.triangle(dX, dY + dS, dX - dS, dY, dX, dY - dS);

        // 8-Point Antique Shaded Compass Star!
        // 4 Primary Points (N, E, S, W)
        float len1 = r - 10f;
        float baseW1 = 12f;

        for (int i = 0; i < 4; i++) {
            float angleDeg = 90f - (i * 90f) + yaw;
            float tipRad = angleDeg * MathUtils.degreesToRadians;
            float leftRad = (angleDeg - 25f) * MathUtils.degreesToRadians;
            float rightRad = (angleDeg + 25f) * MathUtils.degreesToRadians;

            float tx = cx + len1 * MathUtils.cos(tipRad);
            float ty = cy + len1 * MathUtils.sin(tipRad);
            float lx = cx + baseW1 * MathUtils.cos(leftRad);
            float ly = cy + baseW1 * MathUtils.sin(leftRad);
            float rx = cx + baseW1 * MathUtils.cos(rightRad);
            float ry = cy + baseW1 * MathUtils.sin(rightRad);

            // Shaded light gold facet
            shapeRenderer.setColor(goldAccent);
            shapeRenderer.triangle(cx, cy, tx, ty, lx, ly);

            // Shaded dark bronze facet
            shapeRenderer.setColor(goldDark);
            shapeRenderer.triangle(cx, cy, tx, ty, rx, ry);
        }

        // 4 Secondary Points (NE, SE, SW, NW)
        float len2 = r * 0.58f;
        float baseW2 = 8f;

        for (int i = 0; i < 4; i++) {
            float angleDeg = 90f - (i * 90f + 45f) + yaw;
            float tipRad = angleDeg * MathUtils.degreesToRadians;
            float leftRad = (angleDeg - 25f) * MathUtils.degreesToRadians;
            float rightRad = (angleDeg + 25f) * MathUtils.degreesToRadians;

            float tx = cx + len2 * MathUtils.cos(tipRad);
            float ty = cy + len2 * MathUtils.sin(tipRad);
            float lx = cx + baseW2 * MathUtils.cos(leftRad);
            float ly = cy + baseW2 * MathUtils.sin(leftRad);
            float rx = cx + baseW2 * MathUtils.cos(rightRad);
            float ry = cy + baseW2 * MathUtils.sin(rightRad);

            shapeRenderer.setColor(1.0f, 0.88f, 0.45f, 0.85f);
            shapeRenderer.triangle(cx, cy, tx, ty, lx, ly);

            shapeRenderer.setColor(0.48f, 0.35f, 0.15f, 0.85f);
            shapeRenderer.triangle(cx, cy, tx, ty, rx, ry);
        }

        // Center Gold Jewel Boss
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(cx, cy, 6f, 16);
    }

    private void drawAntiqueCompassRoseLines(float w, float h, float yaw) {
        float cx = currentLayout.compassCx;
        float cy = currentLayout.compassCy;
        float r = currentLayout.compassR;

        // Outer ornate gold rims
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.circle(cx, cy, r, 54);
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.circle(cx, cy, r - 6f, 48);

        // Degree tick marks
        for (int i = 0; i < 360; i += 15) {
            float tickRad = (90f - i + yaw) * MathUtils.degreesToRadians;
            float inner = (i % 90 == 0) ? r - 12f : ((i % 45 == 0) ? r - 9f : r - 5f);
            shapeRenderer.setColor(i == 0 ? healthRed : goldBorder);
            shapeRenderer.line(
                cx + inner * MathUtils.cos(tickRad),
                cy + inner * MathUtils.sin(tickRad),
                cx + r * MathUtils.cos(tickRad),
                cy + r * MathUtils.sin(tickRad)
            );
        }

        // Waypoint Ticker Border
        float pillW = currentLayout.pillW;
        float pillH = currentLayout.pillH;
        float pillX = currentLayout.pillX;
        float pillY = currentLayout.pillY;
        strokeGlassBorder(pillX, pillY, pillW, pillH);
    }

    private void drawAntiqueCompassRoseText(float w, float h, float yaw) {
        float cx = currentLayout.compassCx;
        float cy = currentLayout.compassCy;
        float r = currentLayout.compassR;

        // Cardinal Letters (N, E, S, W)
        String[] cardinals = {"N", "E", "S", "W"};
        Color[] cardColors = {healthRed, goldAccent, goldAccent, goldAccent};

        for (int i = 0; i < 4; i++) {
            float rad = (90f - i * 90f + yaw) * MathUtils.degreesToRadians;
            float lx = cx + (r - 18f) * MathUtils.cos(rad) - 5f;
            float ly = cy + (r - 18f) * MathUtils.sin(rad) + 5f;

            fonts.smallFont.setColor(cardColors[i]);
            fonts.smallFont.draw(spriteBatch, cardinals[i], lx, ly);
        }

        // Degree readout beneath
        int displayYaw = (int) ((yaw % 360 + 360) % 360);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, displayYaw + " deg", cx - 20f, cy - r - 8f);

        // Waypoint Ticker Text ("CURZON HALL   W")
        float pillX = currentLayout.pillX;
        drawOutlined(fonts.smallFont, "CURZON HALL   W", pillX + 28f, cy + 5f, goldAccent);
    }

    // ========================================================
    // 3. BOTTOM-LEFT LIVE MISSION PARAMETERS CHECKLIST & COUNTER
    // ========================================================
    private void drawMissionChecklistBg(PlayerController player, JulyMemorials memorials, float w, float h) {
        float bx = currentLayout.checkX;
        float by = currentLayout.checkY;
        float bw = currentLayout.checkW;
        float bh = currentLayout.checkH;

        // See-through smoked glass backing with soft gold halo
        fillGlassPanel(bx, by, bw, bh);

        if (activeLevel != null && activeLevel.getLevelNumber() > 1) {
            float barX = bx + 16f;
            float barY = by + 12f;
            float barW = bw - 32f;
            float barH = 5f;

            shapeRenderer.setColor(0.12f, 0.14f, 0.16f, 0.40f);
            shapeRenderer.rect(barX, barY, barW, barH);

            int ins = activeLevel.getCompletedObjectiveCount();
            int tot = activeLevel.getTotalObjectiveCount();
            float fillRatio = tot > 0 ? (float) ins / (float) tot : 0f;
            float fillW = barW * fillRatio;

            if (fillW > 0f) {
                Color fillC = ins >= tot ? Color.GREEN : staminaGreen;
                shapeRenderer.setColor(fillC.r, fillC.g, fillC.b, 0.16f);
                shapeRenderer.rect(barX, barY - 2f, fillW, barH + 4f);
                shapeRenderer.setColor(fillC);
                shapeRenderer.rect(barX, barY, fillW, barH);
            }
            return;
        }

        // Active objective row highlight: gold glow bar fading to the right + solid left accent bar
        Array<MemorialEntry> entries = (memorials != null) ? memorials.getEntries() : new Array<MemorialEntry>();
        MemorialEntry nextObj = (memorials != null) ? memorials.getNextObjective(player.getPosition()) : null;
        float startY = by + bh - 38f;
        float itemSpacing = 18f;
        float pulse = easedPulse(2.4f);
        for (int i = 0; i < entries.size && i < CHECKLIST_NAMES.length; i++) {
            MemorialEntry entry = entries.get(i);
            if (entry.inspected || entry != nextObj) continue;
            float rowTop = startY - i * itemSpacing + 3f;
            float rowBot = rowTop - itemSpacing;
            float rowX = bx + 8f;
            float rowW = bw - 16f;
            // Soft translucent gold glow (max alpha 0.22) fading to fully clear on the right
            tmpColor.set(1.0f, 0.80f, 0.32f, 0.12f + 0.10f * pulse);
            Color clear = panelSheenClear;
            shapeRenderer.rect(rowX, rowBot, rowW, itemSpacing, tmpColor, clear, clear, tmpColor);
            // Thin soft gold underline, fading right
            tmpColor.set(1.0f, 0.86f, 0.38f, 0.45f + 0.15f * pulse);
            shapeRenderer.rect(rowX, rowBot, rowW * 0.8f, 1f, tmpColor, clear, clear, tmpColor);
            // Left accent bar (+ faint halo), left of the text
            shapeRenderer.setColor(1.0f, 0.86f, 0.40f, 0.10f + 0.08f * pulse);
            shapeRenderer.rect(rowX - 1f, rowBot + 1f, 4f, itemSpacing - 2f);
            shapeRenderer.setColor(activeRowGold);
            shapeRenderer.rect(rowX, rowBot + 2f, 2f, itemSpacing - 4f);
        }

        // Progress bar track at bottom of checklist
        float barX = bx + 16f;
        float barY = by + 12f;
        float barW = bw - 32f;
        float barH = 5f;

        shapeRenderer.setColor(0.12f, 0.14f, 0.16f, 0.40f);
        shapeRenderer.rect(barX, barY, barW, barH);

        int ins = (memorials != null) ? memorials.getInspectedCount() : 0;
        int tot = (memorials != null) ? memorials.getTotalCount() : 5;
        float fillRatio = tot > 0 ? (float) ins / (float) tot : 0f;
        float fillW = barW * fillRatio;

        if (fillW > 0f) {
            Color fillC = ins >= tot ? Color.GREEN : staminaGreen;
            shapeRenderer.setColor(fillC.r, fillC.g, fillC.b, 0.16f);
            shapeRenderer.rect(barX, barY - 2f, fillW, barH + 4f);
            shapeRenderer.setColor(fillC);
            shapeRenderer.rect(barX, barY, fillW, barH);
        }
    }

    private void drawMissionChecklistBorders(PlayerController player, JulyMemorials memorials, float w, float h) {
        float bx = currentLayout.checkX;
        float by = currentLayout.checkY;
        float bw = currentLayout.checkW;
        float bh = currentLayout.checkH;

        // Crisp thin gold border with gentle breathing glow
        strokeGlassBorder(bx, by, bw, bh);

        // Ornate Corner Brackets
        shapeRenderer.setColor(goldAccent);
        float cLen = 14f;
        shapeRenderer.line(bx - 2, by + bh + 2, bx + cLen, by + bh + 2);
        shapeRenderer.line(bx - 2, by + bh + 2, bx - 2, by + bh - cLen);
        shapeRenderer.line(bx + bw + 2, by + bh + 2, bx + bw - cLen, by + bh + 2);
        shapeRenderer.line(bx + bw + 2, by + bh + 2, bx + bw + 2, by + bh - cLen);
        shapeRenderer.line(bx - 2, by - 2, bx + cLen, by - 2);
        shapeRenderer.line(bx - 2, by - 2, bx - 2, by + cLen);
        shapeRenderer.line(bx + bw + 2, by - 2, bx + bw - cLen, by - 2);
        shapeRenderer.line(bx + bw + 2, by - 2, bx + bw + 2, by + cLen);

        // Separator below title
        shapeRenderer.setColor(goldMuted);
        shapeRenderer.line(bx + 14f, by + bh - 28f, bx + bw - 14f, by + bh - 28f);

        if (activeLevel != null && activeLevel.getLevelNumber() > 1) {
            String[] items = activeLevel.getChecklistItems();
            float startY = by + bh - 38f;
            float itemSpacing = 18f;
            float strikeOffset = fonts.smallFont.getCapHeight() * 0.5f;
            shapeRenderer.setColor(doneRowColor.r, doneRowColor.g, doneRowColor.b, 0.75f);
            for (int i = 0; i < items.length; i++) {
                if (!activeLevel.isChecklistItemDone(i)) continue;
                glyphLayout.setText(fonts.smallFont, "[OK] " + items[i]);
                float sy = startY - i * itemSpacing - strikeOffset;
                shapeRenderer.line(bx + 16f, sy, bx + 16f + glyphLayout.width, sy);
            }
            return;
        }

        // Strike-through lines on completed objectives
        Array<MemorialEntry> entries = (memorials != null) ? memorials.getEntries() : new Array<MemorialEntry>();
        float startY = by + bh - 38f;
        float itemSpacing = 18f;
        float strikeOffset = fonts.smallFont.getCapHeight() * 0.5f;
        shapeRenderer.setColor(doneRowColor.r, doneRowColor.g, doneRowColor.b, 0.75f);
        for (int i = 0; i < entries.size && i < CHECKLIST_NAMES.length; i++) {
            if (!entries.get(i).inspected) continue;
            glyphLayout.setText(fonts.smallFont, "[OK] " + CHECKLIST_NAMES[i]);
            float sy = startY - i * itemSpacing - strikeOffset;
            shapeRenderer.line(bx + 16f, sy, bx + 16f + glyphLayout.width, sy);
        }
    }

    private void drawMissionChecklistText(PlayerController player, JulyMemorials memorials, float w, float h) {
        float bx = currentLayout.checkX;
        float by = currentLayout.checkY;
        float bw = currentLayout.checkW;
        float bh = currentLayout.checkH;

        if (activeLevel != null && activeLevel.getLevelNumber() > 1) {
            int ins = activeLevel.getCompletedObjectiveCount();
            int tot = activeLevel.getTotalObjectiveCount();

            String headerTitle = String.format("* MISSION PARAMETERS: %02d / %02d", ins, tot);
            drawOutlined(fonts.keyFont, headerTitle, bx + 16f, by + bh - 10f, ins >= tot ? Color.GREEN : goldAccent);
            drawOutlined(fonts.smallFont, "[P] HIDE", bx + bw - 75f, by + bh - 10f, 0.95f, 0.84f, 0.52f, 1f);

            String[] items = activeLevel.getChecklistItems();
            float startY = by + bh - 38f;
            float itemSpacing = 18f;

            for (int i = 0; i < items.length; i++) {
                float y = startY - i * itemSpacing;
                boolean isDone = activeLevel.isChecklistItemDone(i);
                if (isDone) {
                    drawOutlined(fonts.smallFont, "[OK] " + items[i], bx + 16f, y, doneRowColor);
                } else {
                    drawOutlined(fonts.smallFont, "[  ] " + items[i], bx + 16f, y, upcomingRowColor);
                }
            }

            int pct = tot > 0 ? (int) ((ins / (float) tot) * 100f) : 0;
            String pctStr = String.format("PROGRESS: %d%% (%d/%d COMPLETE)", pct, ins, tot);
            drawOutlined(fonts.smallFont, pctStr, bx + bw - 195f, by + 30f, ins >= tot ? Color.GREEN : goldAccent);
            return;
        }

        int ins = (memorials != null) ? memorials.getInspectedCount() : 0;
        int tot = (memorials != null) ? memorials.getTotalCount() : 5;

        // 1. Header: "* MISSION PARAMETERS: 01 / 05" + [P] HIDE hint
        String headerTitle = String.format("* MISSION PARAMETERS: %02d / %02d", ins, tot);
        drawOutlined(fonts.keyFont, headerTitle, bx + 16f, by + bh - 10f, ins >= tot ? Color.GREEN : goldAccent);
        drawOutlined(fonts.smallFont, "[P] HIDE", bx + bw - 75f, by + bh - 10f, 0.95f, 0.84f, 0.52f, 1f);

        // 2. Checklist of the 5 Memorial checkpoints
        String[] shortNames = CHECKLIST_NAMES;

        Array<MemorialEntry> entries = (memorials != null) ? memorials.getEntries() : new Array<MemorialEntry>();
        MemorialEntry nextObj = (memorials != null) ? memorials.getNextObjective(player.getPosition()) : null;
        float startY = by + bh - 38f;
        float itemSpacing = 18f;

        for (int i = 0; i < entries.size && i < shortNames.length; i++) {
            MemorialEntry entry = entries.get(i);
            float y = startY - i * itemSpacing;
            if (entry.inspected) {
                // Completed: dimmed muted green [OK] (struck through in the border pass)
                drawOutlined(fonts.smallFont, "[OK] " + shortNames[i], bx + 16f, y, doneRowColor);
            } else if (entry == nextObj) {
                // Active objective: bright gold with gentle eased brightness breathing
                float pulse = easedPulse(2.4f);
                float dst = player.getPosition().dst(entry.position);
                String line = String.format("[>]  %s (%.0fm)", shortNames[i], dst);
                drawOutlined(fonts.smallFont, line, bx + 16f, y,
                    1f, 0.80f + 0.08f * pulse, 0.26f + 0.10f * pulse, 1f);
            } else {
                // Upcoming
                drawOutlined(fonts.smallFont, "[  ] " + shortNames[i], bx + 16f, y, upcomingRowColor);
            }
        }

        // 3. Progress percentage
        int pct = tot > 0 ? (ins * 100 / tot) : 0;
        String pctStr = String.format("PROGRESS: %d%% (%d/5 COMPLETE)", pct, ins);
        drawOutlined(fonts.smallFont, pctStr, bx + bw - 195f, by + 30f, ins >= tot ? Color.GREEN : goldAccent);
    }

    // In-world 3D Waypoint Pin floating above active objective
    public void draw3DObjectivePinDirect(Vector3 targetPos, String title, float dst, com.badlogic.gdx.graphics.Camera camera, float w, float h) {
        if (targetPos == null || camera == null) return;

        // Elevated pin height (7.5m) so the waypoint floats clearly above portals and player character from third-person perspective
        Vector3 pinPos = new Vector3(targetPos.x, 7.5f, targetPos.z);
        Vector3 scr = camera.project(pinPos);

        if (scr.z > 0f && scr.z < 1.0f) {
            float px = scr.x * (w / Gdx.graphics.getBackBufferWidth());
            float py = scr.y * (h / Gdx.graphics.getBackBufferHeight());

            float bob = MathUtils.sin(animTime * 4f) * 4f;
            py += bob;

            beginShapes(ShapeType.Filled);
            shapeRenderer.setColor(goldAccent);
            float ds = 7f;
            shapeRenderer.triangle(px, py + ds, px + ds, py, px, py - ds);
            shapeRenderer.triangle(px, py + ds, px - ds, py, px, py - ds);
            shapeRenderer.end();

            beginShapes(ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(px, py, ds + 3f, 16);
            shapeRenderer.end();

            spriteBatch.begin();
            String pinText = String.format("> %s (%.0fm)", (title != null ? title : "OBJECTIVE"), dst);
            GlyphLayout layout = new GlyphLayout(fonts.keyFont, pinText);
            float textX = px - layout.width * 0.5f;
            fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.keyFont.draw(spriteBatch, pinText, textX + 1f, py + 24f);
            fonts.keyFont.setColor(goldAccent);
            fonts.keyFont.draw(spriteBatch, pinText, textX, py + 25f);
            spriteBatch.end();
        }
    }

    private void draw3DObjectivePin(MemorialEntry target, PlayerController player, com.badlogic.gdx.graphics.Camera camera, float w, float h) {
        if (target == null || camera == null) return;
        draw3DObjectivePinDirect(target.position, target.title, player.getPosition().dst(target.position), camera, w, h);
    }

    // ========================================================
    // 4. CENTER SPEECH-BUBBLE INTERACTION PROMPT (Matching Mockup)
    // ========================================================
    private void drawSpeechBubblePromptBg(float w, float h) {
        // Zero solid background: completely transparent campus view!
    }

    private void drawSpeechBubblePromptBorders(float w, float h) {
        float promptW = 460f;
        float promptH = 58f;
        float px = (w - promptW) / 2f + 140f;
        float py = h * 0.44f;

        // Gold border with subtle pulse
        float pulse = 0.7f + 0.3f * easedPulse(3.0f);
        shapeRenderer.setColor(1.0f, 0.84f * pulse, 0.38f, 1f);

        shapeRenderer.rect(px, py, promptW, promptH);

        // Pointer Beak lines
        shapeRenderer.line(px, py + 34f, px - 16f, py + 26f);
        shapeRenderer.line(px - 16f, py + 26f, px, py + 18f);
    }

    private void drawSpeechBubblePromptText(PlayerController player, MemorialEntry nearby, float w, float h) {
        float promptW = 460f;
        float px = (w - promptW) / 2f + 140f;
        float py = h * 0.44f;

        if (activeLevel != null) {
            String prompt = activeLevel.getContextPrompt(player.getPosition());
            if (prompt == null) return;
            fonts.headerFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.headerFont.draw(spriteBatch, prompt, px + 18f, py + 41f);
            fonts.headerFont.setColor(goldAccent);
            fonts.headerFont.draw(spriteBatch, prompt, px + 17f, py + 42f);

            String sub = "-- " + activeLevel.getTitle();
            fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.smallFont.draw(spriteBatch, sub, px + 48f, py + 19f);
            fonts.smallFont.setColor(new Color(0.95f, 0.92f, 0.80f, 1f));
            fonts.smallFont.draw(spriteBatch, sub, px + 47f, py + 20f);
            return;
        }

        if (nearby != null && nearby.id == 99) {
            // Boundary Stone Prompt (Matching reference image [RT] Boundary Stone)
            fonts.headerFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.headerFont.draw(spriteBatch, "[E]  INSPECT BOUNDARY STONE (1921)", px + 18f, py + 41f);
            fonts.headerFont.setColor(goldAccent);
            fonts.headerFont.draw(spriteBatch, "[E]  INSPECT BOUNDARY STONE (1921)", px + 17f, py + 42f);

            fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.smallFont.draw(spriteBatch, "-- HISTORIC DHAKA UNIVERSITY HERITAGE", px + 48f, py + 19f);
            fonts.smallFont.setColor(new Color(0.95f, 0.92f, 0.80f, 1f));
            fonts.smallFont.draw(spriteBatch, "-- HISTORIC DHAKA UNIVERSITY HERITAGE", px + 47f, py + 20f);
        } else {
            // Text with drop shadows
            fonts.headerFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.headerFont.draw(spriteBatch, "[E]  INSPECT HISTORICAL ARCHIVE", px + 23f, py + 41f);
            fonts.headerFont.setColor(goldAccent);
            fonts.headerFont.draw(spriteBatch, "[E]  INSPECT HISTORICAL ARCHIVE", px + 22f, py + 42f);

            fonts.smallFont.setColor(0f, 0f, 0f, 0.90f);
            fonts.smallFont.draw(spriteBatch, "-- 36 JULY STUDENT MASS MOVEMENT", px + 63f, py + 19f);
            fonts.smallFont.setColor(new Color(0.95f, 0.92f, 0.80f, 1f));
            fonts.smallFont.draw(spriteBatch, "-- 36 JULY STUDENT MASS MOVEMENT", px + 62f, py + 20f);
        }
    }

    // ========================================================
    // 5. BOTTOM-RIGHT SLEEK KEYCAPS (Matching Mockup)
    // ========================================================
    private void drawKeycapsBg(float w, float h) {
        float kBaseX = currentLayout.keyBaseX;
        float kBaseY = currentLayout.keyBaseY;

        // Keycap button boxes (see-through smoked glass with gold halo)
        // [W]
        fillGlassPanel(kBaseX + 68f, kBaseY + 84f, 28f, 26f);
        // [A] [S] [D]
        fillGlassPanel(kBaseX + 36f, kBaseY + 54f, 28f, 26f);
        fillGlassPanel(kBaseX + 68f, kBaseY + 54f, 28f, 26f);
        fillGlassPanel(kBaseX + 100f, kBaseY + 54f, 28f, 26f);

        // [SHIFT]
        fillGlassPanel(kBaseX + 138f, kBaseY + 54f, 48f, 26f);

        // [J] (Jump / Double Jump)
        fillGlassPanel(kBaseX + 16f, kBaseY + 26f, 26f, 22f);

        // [E]
        fillGlassPanel(kBaseX + 138f, kBaseY + 26f, 26f, 22f);

        // [M]
        fillGlassPanel(kBaseX + 44f, kBaseY, 26f, 20f);

        // [ESC]
        fillGlassPanel(kBaseX + 114f, kBaseY, 36f, 20f);
    }

    private void drawKeycapsBorders(float w, float h) {
        float kBaseX = currentLayout.keyBaseX;
        float kBaseY = currentLayout.keyBaseY;

        float breathe = easedPulse(1.6f);
        shapeRenderer.setColor(panelBorder.r, panelBorder.g, panelBorder.b, 0.78f + 0.17f * breathe);

        // [W]
        shapeRenderer.rect(kBaseX + 68f, kBaseY + 84f, 28f, 26f);
        // [A] [S] [D]
        shapeRenderer.rect(kBaseX + 36f, kBaseY + 54f, 28f, 26f);
        shapeRenderer.rect(kBaseX + 68f, kBaseY + 54f, 28f, 26f);
        shapeRenderer.rect(kBaseX + 100f, kBaseY + 54f, 28f, 26f);

        // [SHIFT]
        shapeRenderer.rect(kBaseX + 138f, kBaseY + 54f, 48f, 26f);

        // [J] (Jump / Double Jump)
        shapeRenderer.rect(kBaseX + 16f, kBaseY + 26f, 26f, 22f);

        // [E]
        shapeRenderer.rect(kBaseX + 138f, kBaseY + 26f, 26f, 22f);

        // [M]
        shapeRenderer.rect(kBaseX + 44f, kBaseY, 26f, 20f);

        // [ESC]
        shapeRenderer.rect(kBaseX + 114f, kBaseY, 36f, 20f);
    }

    private void drawKeycapsText(float w, float h) {
        float kBaseX = currentLayout.keyBaseX;
        float kBaseY = currentLayout.keyBaseY;

        // Button letter labels
        drawOutlined(fonts.keyFont, "W", kBaseX + 76f, kBaseY + 102f, goldAccent);
        drawOutlined(fonts.keyFont, "A", kBaseX + 44f, kBaseY + 72f, goldAccent);
        drawOutlined(fonts.keyFont, "S", kBaseX + 77f, kBaseY + 72f, goldAccent);
        drawOutlined(fonts.keyFont, "D", kBaseX + 108f, kBaseY + 72f, goldAccent);

        drawOutlined(fonts.keyFont, "SHIFT", kBaseX + 144f, kBaseY + 72f, goldAccent);
        drawOutlined(fonts.keyFont, "J", kBaseX + 25f, kBaseY + 42f, goldAccent);
        drawOutlined(fonts.keyFont, "E", kBaseX + 147f, kBaseY + 42f, goldAccent);
        drawOutlined(fonts.keyFont, "M", kBaseX + 51f, kBaseY + 15f, goldAccent);
        drawOutlined(fonts.keyFont, "ESC", kBaseX + 118f, kBaseY + 15f, goldAccent);

        // Action sub-labels matching updated keybinds:
        drawOutlined(fonts.smallFont, "MOVE", kBaseX - 6f, kBaseY + 72f, Color.WHITE);
        drawOutlined(fonts.smallFont, "SPRINT", kBaseX + 192f, kBaseY + 72f, Color.WHITE);
        drawOutlined(fonts.smallFont, "JUMP (2X)", kBaseX + 48f, kBaseY + 42f, Color.WHITE);
        drawOutlined(fonts.smallFont, "INTERACT", kBaseX + 170f, kBaseY + 42f, Color.WHITE);
        drawOutlined(fonts.smallFont, "MAP", kBaseX + 75f, kBaseY + 15f, Color.WHITE);
        drawOutlined(fonts.smallFont, "PAUSE", kBaseX + 156f, kBaseY + 15f, Color.WHITE);

        // Action Hints: Crouch, Attack (Arena mode only), Gender, and Hide Parameters (anchored within safe margins)
        String hint;
        if (bd.spark36.level.LevelConfig.isCombatEnabled()) {
            hint = "HOLD [J] AT WALL: CLIMB  |  [C] CROUCH  |  [F] ATTACK  |  [G] GENDER (M/F)  |  [P] PARAMETERS";
        } else {
            hint = "HOLD [J] AT WALL: CLIMB  |  [C] CROUCH / SIT  |  [G] GENDER (M/F)  |  [P] PARAMETERS";
        }
        glyphLayout.setText(fonts.smallFont, hint);
        float hintX = w - currentLayout.safeMarginX - glyphLayout.width;
        drawOutlined(fonts.smallFont, hint, hintX, currentLayout.hintLineY, goldAccent);
    }

    // ==========================================
    // HISTORICAL MEMORIAL FLOATING OVERLAY (100% Transparent, No Black Box)
    // ==========================================
    private void renderMemorialModal(MemorialEntry entry, float w, float h) {
        // Frosted transparent glassmorphic panel with gold filigree and generous margins
        float mw = 880f;
        float mh = 250f;
        float mx = (w - mw) / 2f;
        float my = 42f; // Floating in lower area of screen

        // Smoked glass panel backing with soft glow and top sheen
        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0.02f, 0.04f, 0.06f, 0.85f);
        shapeRenderer.rect(mx, my, mw, mh);
        fillGlassPanel(mx, my, mw, mh);

        // Thin golden top line
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(mx, my + mh - 2f, mw, 2f);
        shapeRenderer.end();

        // Golden Corner brackets
        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        float cLen = 16f;
        shapeRenderer.line(mx, my + mh, mx + cLen, my + mh);
        shapeRenderer.line(mx, my + mh, mx, my + mh - cLen);
        shapeRenderer.line(mx + mw, my + mh, mx + mw - cLen, my + mh);
        shapeRenderer.line(mx + mw, my + mh, mx + mw, my + mh - cLen);
        shapeRenderer.line(mx, my, mx + cLen, my);
        shapeRenderer.line(mx, my, mx, my + cLen);
        shapeRenderer.line(mx + mw, my, mx + mw - cLen, my);
        shapeRenderer.line(mx + mw, my, mx + mw, my + cLen);
        shapeRenderer.end();

        // Essential Text with drop shadow for 100% readability directly against 3D world
        spriteBatch.begin();
        float textX = mx + 32f;

        // Header Title: strictly positioned with generous padding inside top border
        fonts.titleFont.setColor(0f, 0f, 0f, 0.98f);
        fonts.titleFont.draw(spriteBatch, entry.title, textX + 2f, my + mh - 34f);
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, entry.title, textX, my + mh - 32f);

        // Subtitle: Date & Location
        fonts.smallFont.setColor(0f, 0f, 0f, 0.98f);
        fonts.smallFont.draw(spriteBatch, entry.date + "   |   " + entry.location, textX + 1.5f, my + mh - 67.5f);
        fonts.smallFont.setColor(new Color(0.95f, 0.90f, 0.70f, 1f));
        fonts.smallFont.draw(spriteBatch, entry.date + "   |   " + entry.location, textX, my + mh - 66f);

        // Description narrative
        fonts.bodyFont.setColor(0f, 0f, 0f, 0.98f);
        fonts.bodyFont.draw(spriteBatch, entry.description, textX + 1.5f, my + mh - 97.5f, mw - 64f, 10, true);
        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch, entry.description, textX, my + mh - 96f, mw - 64f, 10, true);

        // Close prompt
        fonts.promptFont.setColor(0f, 0f, 0f, 0.98f);
        fonts.promptFont.draw(spriteBatch, "[E] / [ESC] Close Archive", mx + mw - 238f, my + 27f);
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[E] / [ESC] Close Archive", mx + mw - 240f, my + 28f);

        spriteBatch.end();
    }

    // ========================================================
    // TACTICAL MAP HUD BUTTON (Top-Right under Compass)
    // ========================================================
    private void drawTacticalMapButtonBg(float w, float h) {
        fillGlassPanel(currentLayout.mapBtnX, currentLayout.mapBtnY, currentLayout.mapBtnW, currentLayout.mapBtnH);
    }

    private void drawTacticalMapButtonBorder(float w, float h) {
        float mbX = currentLayout.mapBtnX;
        float mbY = currentLayout.mapBtnY;
        float mbW = currentLayout.mapBtnW;
        float mbH = currentLayout.mapBtnH;
        strokeGlassBorder(mbX, mbY, mbW, mbH);

        float cLen = 7f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(mbX - 2, mbY + mbH + 2, mbX + cLen, mbY + mbH + 2);
        shapeRenderer.line(mbX - 2, mbY + mbH + 2, mbX - 2, mbY + mbH - cLen);
        shapeRenderer.line(mbX + mbW + 2, mbY + mbH + 2, mbX + mbW - cLen, mbY + mbH + 2);
        shapeRenderer.line(mbX + mbW + 2, mbY + mbH + 2, mbX + mbW + 2, mbY + mbH - cLen);
        shapeRenderer.line(mbX - 2, mbY - 2, mbX + cLen, mbY - 2);
        shapeRenderer.line(mbX - 2, mbY - 2, mbX - 2, mbY + cLen);
        shapeRenderer.line(mbX + mbW + 2, mbY - 2, mbX + mbW - cLen, mbY - 2);
        shapeRenderer.line(mbX + mbW + 2, mbY - 2, mbX + mbW + 2, mbY + cLen);
    }

    private void drawTacticalMapButtonText(float w, float h) {
        drawOutlined(fonts.keyFont, "[M]  CAMPUS MAP", currentLayout.mapBtnX + 18f, currentLayout.mapBtnY + 23f, goldAccent);
    }

    // ==========================================
    // CAMPUS TACTICAL MAP OVERLAY (MATCHING MEDIA_1790270627121)
    // ==========================================
    private void renderCampusMap(PlayerController player, JulyMemorials memorials, float cameraYaw, float w, float h) {
        if (campusMapTex == null && !campusMapLoadTried) loadCampusMapTexture();
        if (campusMapTex != null) {
            renderCampusMapImage(player, memorials, w, h);
            return;
        }

        // Fallback: shape-drawn map, used only if the map image is missing from the assets
        float mapW = 1520f;
        float mapH = h - 50f;
        float mx = (w - mapW) / 2f;
        float my = 25f;

        float centerMapX = mx + mapW / 2f;
        float centerMapY = my + mapH / 2f + 16f;
        float scale = 4.8f; // 4.8 pixels per meter
        // screenX = centerMapX + worldX * scale
        // screenY = centerMapY - (worldZ - 18) * scale  [north=negZ=higher Y]

        // ============================================================
        // FILLED PASS
        // ============================================================
        beginShapes(ShapeType.Filled);

        // Full dark backdrop
        shapeRenderer.setColor(0f, 0f, 0f, 0.92f);
        shapeRenderer.rect(0, 0, w, h);

        // Map canvas
        shapeRenderer.setColor(0.07f, 0.09f, 0.11f, 0.97f);
        shapeRenderer.rect(mx, my, mapW, mapH);

        // Campus lawn (reference: rich green base)
        shapeRenderer.setColor(0.13f, 0.20f, 0.12f, 0.95f);
        shapeRenderer.rect(centerMapX - 100f * scale, centerMapY - 62f * scale, 200f * scale, 124f * scale);

        // Fuller Road (north, top of map)
        shapeRenderer.setColor(0.16f, 0.16f, 0.18f, 0.96f);
        shapeRenderer.rect(centerMapX - 110f * scale, centerMapY + 62f * scale, 220f * scale, 10f * scale);

        // Doel Chattar Road (south, bottom of map)
        shapeRenderer.rect(centerMapX - 110f * scale, centerMapY - 72f * scale, 220f * scale, 14f * scale);

        // South boundary wall (red brick, gap at Main Gate)
        shapeRenderer.setColor(0.52f, 0.18f, 0.14f, 0.96f);
        shapeRenderer.rect(centerMapX - 110f * scale, centerMapY - 62f * scale, 105f * scale, 2.0f * scale);
        shapeRenderer.rect(centerMapX + 5.5f * scale, centerMapY - 62f * scale, 104.5f * scale, 2.0f * scale);

        // ============================================================
        // WALKWAY NETWORK
        // ============================================================
        shapeRenderer.setColor(0.70f, 0.63f, 0.50f, 0.92f);

        // Central Promenade: X=0, Z=+78 (gate) to Z=-40 (behind Curzon Hall)
        shapeRenderer.rect(centerMapX - 3.6f * scale, centerMapY - 62f * scale, 7.2f * scale, 118f * scale);

        // Pukur ring paths: X=+-14m, Z=-10.8 to +24
        float ringBottomY = centerMapY - (24f - 18f) * scale;
        shapeRenderer.rect(centerMapX - 16.4f * scale, ringBottomY, 4.8f * scale, 34.8f * scale);
        shapeRenderer.rect(centerMapX + 11.6f * scale, ringBottomY, 4.8f * scale, 34.8f * scale);

        // North link (Z=-10.8) and south link (Z=+24) joining the promenade to the ring paths
        shapeRenderer.rect(centerMapX - 14f * scale, centerMapY - (-10.8f - 18f) * scale - 2.4f * scale, 28f * scale, 4.8f * scale);
        shapeRenderer.rect(centerMapX - 14f * scale, centerMapY - (24f - 18f) * scale - 2.4f * scale, 28f * scale, 4.8f * scale);

        // West Cross-Avenue: Z=+26, X=0 to X=-74
        shapeRenderer.rect(centerMapX - 74f * scale, centerMapY - (26f - 18f) * scale - 2.4f * scale, 74f * scale, 4.8f * scale);

        // West North Branch to Madhur Canteen: X=-56, Z=+26 to Z=-20
        shapeRenderer.rect(centerMapX - 58.4f * scale, centerMapY - (26f - 18f) * scale, 4.8f * scale, 46f * scale);

        // West South Branch to Hakim Chattar: X=-58, Z=+26 to Z=+54
        shapeRenderer.rect(centerMapX - 60.4f * scale, centerMapY - (54f - 18f) * scale, 4.8f * scale, 28f * scale);

        // East Cross-Avenue: Z=+26, X=0 to X=+74
        shapeRenderer.rect(centerMapX, centerMapY - (26f - 18f) * scale - 2.4f * scale, 74f * scale, 4.8f * scale);

        // East North Branch to Swadhinata: X=+56, Z=+26 to Z=-18
        shapeRenderer.rect(centerMapX + 53.6f * scale, centerMapY - (26f - 18f) * scale, 4.8f * scale, 44f * scale);

        // East South Branch to Raju: X=+44, Z=+26 to Z=+42
        shapeRenderer.rect(centerMapX + 41.6f * scale, centerMapY - (42f - 18f) * scale, 4.8f * scale, 16f * scale);

        // Curzon Hall front verandah path: Z=-17, X=-48 to X=+48
        shapeRenderer.rect(centerMapX - 48f * scale, centerMapY - (-17f - 18f) * scale - 3f * scale, 96f * scale, 6f * scale);

        // ============================================================
        // CURZON HALL PUKUR
        // Pond sits in the centre of the spine; the promenade splits around it
        // ============================================================
        float pukurWorldX = 0f;
        float pukurWorldZ = 6f;
        float pukurWm     = 18f;
        float pukurHm     = 24f;

        float pX = centerMapX + pukurWorldX * scale;
        float pY = centerMapY - (pukurWorldZ - 18f) * scale;
        float pW = pukurWm * scale;
        float pH = pukurHm * scale;

        // Stone curb border
        shapeRenderer.setColor(0.82f, 0.79f, 0.74f, 1f);
        shapeRenderer.rect(pX - pW / 2f - 4f, pY - pH / 2f - 4f, pW + 8f, pH + 8f);
        // Water
        shapeRenderer.setColor(0.20f, 0.55f, 0.64f, 0.96f);
        shapeRenderer.rect(pX - pW / 2f, pY - pH / 2f, pW, pH);
        // Shimmer
        shapeRenderer.setColor(0.35f, 0.72f, 0.80f, 0.50f);
        shapeRenderer.rect(pX - pW / 2f + 4f, pY - pH / 2f + 3f, pW * 0.6f, pH * 0.25f);

        // ============================================================
        // BUILDINGS
        // ============================================================

        // 1. CURZON HALL (north/top-center)
        float chX = centerMapX;
        float chY = centerMapY - (-32f - 18f) * scale;
        shapeRenderer.setColor(0.66f, 0.21f, 0.15f, 0.97f);
        shapeRenderer.rect(chX - 19f * scale, chY - 8.5f * scale, 38f * scale, 17f * scale);
        shapeRenderer.rect(chX - 46f * scale, chY - 7.5f * scale, 27f * scale, 15f * scale);
        shapeRenderer.rect(chX + 19f * scale, chY - 7.5f * scale, 27f * scale, 15f * scale);
        shapeRenderer.rect(chX - 7.5f * scale, chY - 12.5f * scale, 15f * scale, 4f * scale);
        shapeRenderer.setColor(0.94f, 0.92f, 0.88f, 1f);
        shapeRenderer.circle(chX, chY, 5.2f * scale, 24);
        shapeRenderer.circle(chX - 18.5f * scale, chY - 2.5f * scale, 2.2f * scale, 16);
        shapeRenderer.circle(chX + 18.5f * scale, chY - 2.5f * scale, 2.2f * scale, 16);
        shapeRenderer.circle(chX - 44.5f * scale, chY - 2.5f * scale, 2.1f * scale, 16);
        shapeRenderer.circle(chX + 44.5f * scale, chY - 2.5f * scale, 2.1f * scale, 16);

        // 2. CENTRAL LIBRARY BUILDING (far west)
        float clMapX = centerMapX - 68f * scale;
        float clMapY = centerMapY - (22f - 18f) * scale;
        shapeRenderer.setColor(0.63f, 0.23f, 0.17f, 0.97f);
        shapeRenderer.rect(clMapX - 18f * scale, clMapY - 16f * scale, 36f * scale, 32f * scale);
        shapeRenderer.setColor(0.13f, 0.20f, 0.12f, 0.96f);
        shapeRenderer.rect(clMapX - 8f * scale, clMapY - 8f * scale, 16f * scale, 16f * scale);
        shapeRenderer.setColor(0.75f, 0.73f, 0.70f, 0.96f);
        shapeRenderer.rect(clMapX + 14f * scale, clMapY - 8f * scale, 4f * scale, 16f * scale);

        // 3. HAKIM CHATTAR (bottom-left, octagonal)
        float hkMapX = centerMapX - 58f * scale;
        float hkMapY = centerMapY - (54f - 18f) * scale;
        shapeRenderer.setColor(0.70f, 0.30f, 0.20f, 0.96f);
        shapeRenderer.circle(hkMapX, hkMapY, 5.2f * scale, 8);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.circle(hkMapX, hkMapY, 1.3f * scale, 8);

        // 4. MADHUR CANTEEN (top-left)
        float mcMapX = centerMapX - 56f * scale;
        float mcMapY = centerMapY - (-16f - 18f) * scale;
        shapeRenderer.setColor(0.56f, 0.22f, 0.18f, 0.97f);
        shapeRenderer.rect(mcMapX - 9f * scale, mcMapY - 7f * scale, 18f * scale, 14f * scale);
        shapeRenderer.setColor(0.82f, 0.76f, 0.68f, 0.96f);
        shapeRenderer.rect(mcMapX - 8f * scale, mcMapY - 7f * scale, 16f * scale, 3.5f * scale);

        // 5. BOOK STALLS (west of promenade, north of Arts Plaza)
        shapeRenderer.setColor(0.36f, 0.26f, 0.16f, 0.96f);
        for (float bsz : new float[]{6f, 12f, 18f}) {
            float bsMapY = centerMapY - (bsz - 18f) * scale;
            shapeRenderer.rect(centerMapX - 44f * scale, bsMapY - 1.4f * scale, 2.8f * scale, 2.8f * scale);
        }

        // 6. ARTS PLAZA + APARAJEYO BANGLA (west of promenade)
        float abMapX = centerMapX - 36f * scale;
        float abMapY = centerMapY - (26f - 18f) * scale;
        shapeRenderer.setColor(0.78f, 0.76f, 0.72f, 0.96f);
        shapeRenderer.rect(abMapX - 4f * scale, abMapY - 4f * scale, 8f * scale, 8f * scale);
        shapeRenderer.setColor(0.24f, 0.27f, 0.30f, 0.97f);
        shapeRenderer.circle(abMapX, abMapY, 2.0f * scale, 12);

        // 8. TEACHER-STUDENT CENTRE (far right, modernist)
        float tscMapX = centerMapX + 68f * scale;
        float tscMapY = centerMapY - (20f - 18f) * scale;
        shapeRenderer.setColor(0.74f, 0.73f, 0.70f, 0.97f);
        shapeRenderer.rect(tscMapX - 19f * scale, tscMapY - 15f * scale, 38f * scale, 30f * scale);
        shapeRenderer.setColor(0.60f, 0.26f, 0.20f, 0.97f);
        shapeRenderer.rect(tscMapX - 9f * scale, tscMapY - 9f * scale, 18f * scale, 18f * scale);
        shapeRenderer.setColor(0.82f, 0.80f, 0.76f, 0.70f);
        for (int f = -4; f <= 4; f++) {
            shapeRenderer.rect(tscMapX - 19.5f * scale, tscMapY + f * 1.8f * scale - 0.4f * scale, 1.5f * scale, 0.8f * scale);
        }

        // 9. RAJU MEMORIAL ROUNDABOUT (south-east)
        float rjMapX = centerMapX + 44f * scale;
        float rjMapY = centerMapY - (42f - 18f) * scale;
        shapeRenderer.setColor(0.16f, 0.16f, 0.18f, 0.96f);
        shapeRenderer.circle(rjMapX, rjMapY, 9.5f * scale, 28);
        shapeRenderer.setColor(0.17f, 0.30f, 0.15f, 0.96f);
        shapeRenderer.circle(rjMapX, rjMapY, 7.5f * scale, 28);
        shapeRenderer.setColor(0.90f, 0.88f, 0.84f, 0.97f);
        shapeRenderer.rect(rjMapX - 2.4f * scale, rjMapY - 2.4f * scale, 4.8f * scale, 4.8f * scale);

        // 10. SWADHINATA SANGRAM SCULPTURE GARDEN (top-right)
        float ssMapX = centerMapX + 56f * scale;
        float ssMapY = centerMapY - (-16f - 18f) * scale;
        shapeRenderer.setColor(0.68f, 0.66f, 0.62f, 0.96f);
        shapeRenderer.rect(ssMapX - 11f * scale, ssMapY - 9f * scale, 22f * scale, 18f * scale);
        shapeRenderer.setColor(0.24f, 0.27f, 0.30f, 0.96f);
        shapeRenderer.circle(ssMapX - 4.5f * scale, ssMapY + 3f * scale, 1.2f * scale, 8);
        shapeRenderer.circle(ssMapX + 4.5f * scale, ssMapY + 3f * scale, 1.2f * scale, 8);
        shapeRenderer.circle(ssMapX, ssMapY - 3f * scale, 1.2f * scale, 8);

        // 11. MAIN GATE (south/bottom-center)
        float mgMapY = centerMapY - (78f - 18f) * scale;
        shapeRenderer.setColor(0.70f, 0.22f, 0.16f, 0.97f);
        shapeRenderer.rect(centerMapX - 4.8f * scale, mgMapY - 1.5f * scale, 9.6f * scale, 3.5f * scale);
        shapeRenderer.setColor(0.82f, 0.76f, 0.68f, 1f);
        shapeRenderer.rect(centerMapX - 5.2f * scale, mgMapY - 2.5f * scale, 2.0f * scale, 5.5f * scale);
        shapeRenderer.rect(centerMapX + 3.2f * scale, mgMapY - 2.5f * scale, 2.0f * scale, 5.5f * scale);
        shapeRenderer.setColor(0.16f, 0.84f, 0.38f, 1f);
        shapeRenderer.triangle(centerMapX, mgMapY + 5f * scale,
            centerMapX - 2.2f * scale, mgMapY + 1.5f * scale,
            centerMapX + 2.2f * scale, mgMapY + 1.5f * scale);

        // 12. TREE CANOPIES
        float[][] treeMapLocs = {
            {-19.5f, -2f}, {19.5f, -2f},
            {-50f, -28f}, {-62f, -28f}, {50f, -28f}, {62f, -28f},
            {-19.5f, 14f}, {19.5f, 14f},
            {-28f, 14f}, {-48f, 4f}, {-62f, 28f},
            {28f, 14f}, {48f, 4f}, {62f, 28f},
            {-14f, 48f}, {14f, 48f},
            {-22f, 64f}, {22f, 64f},
            {-28f, 70f}, {28f, 70f},
            {-85f, -36f}, {85f, -36f}, {-85f, 60f}, {85f, 60f},
        };
        shapeRenderer.setColor(0.16f, 0.38f, 0.14f, 0.86f);
        for (float[] t : treeMapLocs) {
            shapeRenderer.circle(centerMapX + t[0] * scale, centerMapY - (t[1] - 18f) * scale, 4.0f * scale, 14);
        }
        shapeRenderer.setColor(0.11f, 0.28f, 0.10f, 0.70f);
        for (float[] t : treeMapLocs) {
            shapeRenderer.circle(centerMapX + t[0] * scale, centerMapY - (t[1] - 18f) * scale, 2.0f * scale, 10);
        }

        // 13. OBJECTIVE & MEMORIAL PINS
        MemorialEntry nextObj = memorials.getNextObjective(player.getPosition());
        for (MemorialEntry entry : memorials.getEntries()) {
            float pointX = centerMapX + entry.position.x * scale;
            float pointY = centerMapY - (entry.position.z - 18f) * scale;
            if (entry.inspected) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.circle(pointX, pointY, 8f, 16);
            } else if (entry == nextObj) {
                float pulse = 0.82f + 0.28f * MathUtils.sin(animTime * 6f);
                shapeRenderer.setColor(1.0f, 0.82f * pulse, 0.25f, 1f);
                float ds = 10f * pulse;
                shapeRenderer.triangle(pointX, pointY + ds, pointX + ds, pointY, pointX, pointY - ds);
                shapeRenderer.triangle(pointX, pointY + ds, pointX - ds, pointY, pointX, pointY - ds);
            } else {
                shapeRenderer.setColor(goldAccent);
                shapeRenderer.circle(pointX, pointY, 7f, 16);
            }
        }

        // 14. LIVE PLAYER MARKER
        Vector3 ppos = player.getPosition();
        float pMapX = centerMapX + ppos.x * scale;
        float pMapY = centerMapY - (ppos.z - 18f) * scale;
        shapeRenderer.setColor(0.2f, 0.9f, 1.0f, 0.30f + 0.18f * MathUtils.sin(animTime * 4f));
        shapeRenderer.circle(pMapX, pMapY, 13f, 20);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(pMapX, pMapY, 5.5f, 16);
        float pHeadRad = (player.getHeadingDegrees() - 90f) * MathUtils.degreesToRadians;
        float tipX = pMapX - 16f * MathUtils.cos(pHeadRad);
        float tipY = pMapY + 16f * MathUtils.sin(pHeadRad);
        float baseLeftX = pMapX - 6f * MathUtils.cos(pHeadRad + 2.2f);
        float baseLeftY = pMapY + 6f * MathUtils.sin(pHeadRad + 2.2f);
        float baseRightX = pMapX - 6f * MathUtils.cos(pHeadRad - 2.2f);
        float baseRightY = pMapY + 6f * MathUtils.sin(pHeadRad - 2.2f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.triangle(tipX, tipY, baseLeftX, baseLeftY, baseRightX, baseRightY);

        // 15. UI PANEL BACKGROUNDS
        float vtX = mx + 20f, vtY = my + 20f, vtW = 270f, vtH = 185f;
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.93f);
        shapeRenderer.rect(vtX, vtY, vtW, vtH);

        float legX = mx + mapW - 260f, legY = my + 20f, legW = 240f, legH = 230f;
        shapeRenderer.setColor(0.04f, 0.06f, 0.08f, 0.93f);
        shapeRenderer.rect(legX, legY, legW, legH);

        float cbX = mx + mapW - 175f, cbY = my + mapH - 48f, cbW = 155f, cbH = 34f;
        shapeRenderer.setColor(0.10f, 0.14f, 0.20f, 0.93f);
        shapeRenderer.rect(cbX, cbY, cbW, cbH);

        // LEGEND ICONS
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(legX + 16f, legY + legH - 48f, 11f, 11f);
        shapeRenderer.setColor(1.0f, 0.85f, 0.25f, 1f);
        float ldy = legY + legH - 72f;
        shapeRenderer.triangle(legX + 21f, ldy + 7f, legX + 28f, ldy, legX + 14f, ldy);
        shapeRenderer.triangle(legX + 21f, ldy - 7f, legX + 28f, ldy, legX + 14f, ldy);
        shapeRenderer.setColor(0.35f, 0.82f, 1.0f, 1f);
        float lpy = legY + legH - 96f;
        shapeRenderer.triangle(legX + 21f, lpy + 6f, legX + 27f, lpy, legX + 15f, lpy);
        shapeRenderer.triangle(legX + 21f, lpy - 6f, legX + 27f, lpy, legX + 15f, lpy);
        shapeRenderer.setColor(1.0f, 0.55f, 0.20f, 1f);
        shapeRenderer.circle(legX + 21f, legY + legH - 118f, 5.5f, 12);
        shapeRenderer.triangle(legX + 21f, legY + legH - 128f, legX + 18f, legY + legH - 118f, legX + 24f, legY + legH - 118f);
        shapeRenderer.setColor(0.30f, 0.55f, 1.0f, 1f);
        shapeRenderer.circle(legX + 21f, legY + legH - 148f, 5.5f, 12);
        shapeRenderer.setColor(0.90f, 0.20f, 0.20f, 1f);
        shapeRenderer.circle(legX + 21f, legY + legH - 168f, 5.5f, 12);
        shapeRenderer.setColor(0.2f, 0.9f, 1.0f, 0.50f);
        shapeRenderer.circle(legX + 21f, legY + legH - 190f, 7f, 12);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(legX + 21f, legY + legH - 190f, 3.5f, 12);

        shapeRenderer.end();

        // ============================================================
        // LINE BORDERS PASS
        // ============================================================
        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(mx, my, mapW, mapH);
        shapeRenderer.rect(mx + 3f, my + 3f, mapW - 6f, mapH - 6f);
        float cLen = 30f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(mx, my + mapH - cLen, mx, my + mapH);
        shapeRenderer.line(mx, my + mapH, mx + cLen, my + mapH);
        shapeRenderer.line(mx + mapW - cLen, my + mapH, mx + mapW, my + mapH);
        shapeRenderer.line(mx + mapW, my + mapH, mx + mapW, my + mapH - cLen);
        shapeRenderer.line(mx, my, mx + cLen, my);
        shapeRenderer.line(mx, my, mx, my + cLen);
        shapeRenderer.line(mx + mapW - cLen, my, mx + mapW, my);
        shapeRenderer.line(mx + mapW, my, mx + mapW, my + cLen);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(vtX, vtY, vtW, vtH);
        shapeRenderer.rect(legX, legY, legW, legH);
        shapeRenderer.setColor(new Color(0.30f, 0.42f, 0.56f, 0.90f));
        shapeRenderer.rect(cbX, cbY, cbW, cbH);

        // Compass Rose
        float crX = mx + 90f, crY = my + mapH - 90f, crR = 52f;
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.circle(crX, crY, crR, 36);
        shapeRenderer.circle(crX, crY, crR - 5f, 32);
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(crX, crY - crR, crX, crY + crR);
        shapeRenderer.line(crX - crR, crY, crX + crR, crY);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.line(crX - crR * 0.7f, crY - crR * 0.7f, crX + crR * 0.7f, crY + crR * 0.7f);
        shapeRenderer.line(crX + crR * 0.7f, crY - crR * 0.7f, crX - crR * 0.7f, crY + crR * 0.7f);
        shapeRenderer.end();

        // ============================================================
        // TEXT PASS
        // ============================================================
        spriteBatch.begin();

        // Title
        fonts.titleFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.titleFont.draw(spriteBatch, "LEVEL 1: DHAKA UNIVERSITY", centerMapX - 218f + 2f, my + mapH - 18f);
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "LEVEL 1: DHAKA UNIVERSITY", centerMapX - 218f, my + mapH - 16f);
        fonts.smallFont.setColor(new Color(0.88f, 0.84f, 0.68f, 1f));
        fonts.smallFont.draw(spriteBatch, "Mall Chattar Corridor  \u2022  Curzon Hall Precinct  \u2022  Est. 1921", centerMapX - 240f, my + mapH - 42f);

        // Compass
        fonts.headerFont.setColor(healthRed);
        fonts.headerFont.draw(spriteBatch, "N", crX - 6f, crY + crR + 22f);
        fonts.smallFont.setColor(goldAccent);
        fonts.smallFont.draw(spriteBatch, "S", crX - 4f, crY - crR - 5f);
        fonts.smallFont.draw(spriteBatch, "W", crX - crR - 18f, crY + 5f);
        fonts.smallFont.draw(spriteBatch, "E", crX + crR + 6f, crY + 5f);

        // Close button
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[M] / [ESC]  Close  X", cbX + 12f, cbY + 23f);

        // Landmark labels
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Fuller Road Corridor", centerMapX - 60f, centerMapY + 64f * scale);
        fonts.smallFont.draw(spriteBatch, "Curzon Hall", chX - 32f, chY + 2.5f * scale);
        fonts.smallFont.setColor(new Color(0.88f, 0.96f, 1.0f, 1f));
        fonts.smallFont.draw(spriteBatch, "Curzon Hall Pukur", pX - 46f, pY + 4f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Central Library Building", clMapX - 56f, clMapY + 20f * scale);
        fonts.smallFont.draw(spriteBatch, "Hakim Chattar", hkMapX - 42f, hkMapY - 9f * scale);
        fonts.smallFont.draw(spriteBatch, "Madhur Canteen", mcMapX - 44f, mcMapY + 11f * scale);
        fonts.smallFont.draw(spriteBatch, "Book Stalls", centerMapX - 50f * scale, centerMapY - (12f - 18f) * scale + 6f);
        fonts.smallFont.draw(spriteBatch, "Arts Plaza", abMapX - 28f, abMapY + 5f * scale);
        fonts.smallFont.draw(spriteBatch, "Aparajeyo Bangla", abMapX - 48f, abMapY - 7f * scale);
        fonts.smallFont.draw(spriteBatch, "Teacher-Student Centre (TSC)", tscMapX - 72f, tscMapY + 18f * scale);
        fonts.smallFont.draw(spriteBatch, "Raju Memorial Sculpture", rjMapX - 60f, rjMapY - 12f * scale);
        fonts.smallFont.draw(spriteBatch, "Swadhinata Sangram", ssMapX - 56f, ssMapY + 12f * scale);
        fonts.smallFont.draw(spriteBatch, "Sculpture Garden", ssMapX - 46f, ssMapY + 10f * scale - 14f);
        fonts.smallFont.draw(spriteBatch, "Main Gate", centerMapX - 28f, mgMapY - 10f);

        // Visual Target Card
        if (textures != null && textures.mapVisualTarget != null) {
            spriteBatch.draw(textures.mapVisualTarget, vtX + 12f, vtY + 38f, vtW - 24f, 110f);
        }
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "LEVEL 1 VISUAL TARGET", vtX + 14f, vtY + vtH - 12f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Curzon Hall (Science Faculty)", vtX + 14f, vtY + 24f);

        // Map Legend Card
        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "MAP LEGEND", legX + 16f, legY + legH - 14f);
        fonts.smallFont.setColor(Color.WHITE);
        fonts.smallFont.draw(spriteBatch, "Buildings", legX + 36f, legY + legH - 39f);
        fonts.smallFont.draw(spriteBatch, "POI objective", legX + 36f, legY + legH - 63f);
        fonts.smallFont.draw(spriteBatch, "Teacher-Student Centre", legX + 36f, legY + legH - 87f);
        fonts.smallFont.draw(spriteBatch, "Arts Plaza", legX + 36f, legY + legH - 109f);
        fonts.smallFont.draw(spriteBatch, "Quest Pin", legX + 36f, legY + legH - 139f);
        fonts.smallFont.draw(spriteBatch, "Quest Pins", legX + 36f, legY + legH - 159f);
        fonts.smallFont.draw(spriteBatch, "High objective Markers", legX + 36f, legY + legH - 181f);
        fonts.smallFont.setColor(new Color(0.70f, 0.82f, 0.90f, 1f));
        fonts.smallFont.draw(spriteBatch, String.format("%.1fE  %.1fN", ppos.x, -ppos.z), legX + 16f, legY + 24f);

        spriteBatch.end();
    }

    // ==========================================
    // CAMPUS MAP (illustrated map image + live markers)
    // ==========================================
    // The map art was cleaned of its baked-in UI (title bar, menu button, legend, photo panels,
    // quest pins). The top 48px title bar was cropped off, so anchors below stay in the original
    // 843px-tall picture's coordinates and MAP_TOP_CROP is subtracted when converting.
    private static final float MAP_IMG_W = 1264f;
    private static final float MAP_IMG_H = 795f;
    private static final float MAP_TOP_CROP = 48f;

    /**
     * Landmarks whose in-game position is pinned to a spot on the map illustration:
     * {worldX, worldZ, imageX, imageY} with the image origin at the top-left.
     * The illustration is hand-drawn, not to scale, so a plain linear mapping drifts; these
     * anchors pull the markers onto the right buildings (see {@link #worldToMapImage}).
     */
    private static final float[][] MAP_ANCHORS = {
        {0f, 78f, 632f, 745f},      // Main Gate
        {0f, -32f, 630f, 170f},     // Curzon Hall
        {0f, -16f, 630f, 262f},     // Curzon Hall central arcade
        {0f, 6f, 630f, 405f},       // Curzon Hall Pukur
        {-68f, 22f, 150f, 385f},    // Central Library
        {68f, 20f, 1105f, 400f},    // Teacher-Student Centre
        {66f, 20f, 1085f, 410f},    // TSC memorial
        {-56f, -16f, 255f, 130f},   // Madhur Canteen
        {-58f, 54f, 190f, 640f},    // Hakim Chattar
        {-58f, 36f, 200f, 545f},    // Library steps / Hakim memorial
        {-36f, 26f, 440f, 430f},    // Aparajeyo Bangla statue / Arts Plaza
        {-42f, 38f, 345f, 375f},    // Book stalls
        {44f, 42f, 930f, 410f},     // Raju memorial
        {56f, -16f, 1030f, 150f},   // Swadhinata Sangram garden
        {-26f, 52f, 400f, 590f},    // West academic building
        {26f, 52f, 870f, 590f},     // East academic building
    };

    private final com.badlogic.gdx.graphics.g2d.GlyphLayout glyphs = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    private void loadCampusMapTexture() {
        campusMapLoadTried = true;
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("maps/campus_map.png");
            if (file.exists()) {
                campusMapTex = new com.badlogic.gdx.graphics.Texture(file, true);
                campusMapTex.setFilter(
                    com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapLinearLinear,
                    com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            }
        } catch (Exception e) {
            Gdx.app.error("WhereWindsMeetHUD", "Could not load campus map image, using fallback map", e);
        }
    }

    /**
     * Converts a world position to a pixel on the map image. Starts from a linear estimate
     * (gate at the bottom, north up) and bends it toward the landmark anchors: each anchor's
     * error is blended in by inverse-distance weight, so a landmark lands exactly on its
     * building and positions in between are smoothly interpolated.
     */
    private static void worldToMapImage(float wx, float wz, float[] out) {
        float baseX = 632f + 7.0f * wx;
        float baseY = 745f - 4.85f * (78f - wz);

        float corrX = 0f, corrY = 0f, weightSum = 0f;
        for (float[] a : MAP_ANCHORS) {
            float dx = wx - a[0], dz = wz - a[1];
            float d2 = dx * dx + dz * dz;
            if (d2 < 0.01f) {
                out[0] = a[2];
                out[1] = a[3] - MAP_TOP_CROP;
                return;
            }
            float weight = 1f / (d2 * (float) Math.sqrt(d2)); // 1 / d^3: landmarks dominate nearby
            corrX += weight * (a[2] - (632f + 7.0f * a[0]));
            corrY += weight * (a[3] - (745f - 4.85f * (78f - a[1])));
            weightSum += weight;
        }
        out[0] = MathUtils.clamp(baseX + corrX / weightSum, 0f, MAP_IMG_W);
        out[1] = MathUtils.clamp(baseY + corrY / weightSum - MAP_TOP_CROP, 0f, MAP_IMG_H);
    }

    private void renderCampusMapImage(PlayerController player, JulyMemorials memorials, float w, float h) {
        float margin = 34f;
        float fit = Math.min((w - 2f * margin) / MAP_IMG_W, (h - 2f * margin - 16f) / MAP_IMG_H);
        float dw = MAP_IMG_W * fit;
        float dh = MAP_IMG_H * fit;
        float dx = (w - dw) / 2f;
        float dy = (h - dh) / 2f + 8f;

        // Dim the game behind the map
        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.94f);
        shapeRenderer.rect(0, 0, w, h);
        shapeRenderer.end();

        spriteBatch.begin();
        spriteBatch.setColor(1f, 1f, 1f, 1f);
        spriteBatch.draw(campusMapTex, dx, dy, dw, dh);
        spriteBatch.end();

        float[] pt = new float[2];
        MemorialEntry nextObj = memorials.getNextObjective(player.getPosition());

        beginShapes(ShapeType.Filled);

        // Memorial pins: secured = green, current objective = pulsing gold diamond, others = gold
        for (MemorialEntry entry : memorials.getEntries()) {
            worldToMapImage(entry.position.x, entry.position.z, pt);
            float px = dx + pt[0] * fit;
            float py = dy + dh - pt[1] * fit;
            shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
            shapeRenderer.circle(px, py, 11f, 20);
            if (entry == nextObj) {
                // Backing plate so the objective label stays readable over light paths
                glyphs.setText(fonts.smallFont, "OBJECTIVE: " + entry.title);
                shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.82f);
                shapeRenderer.rect(px - 86f, py + 17f, glyphs.width + 12f, 24f);
            }
            if (entry.inspected) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.circle(px, py, 8f, 16);
            } else if (entry == nextObj) {
                float pulse = 0.85f + 0.25f * MathUtils.sin(animTime * 6f);
                shapeRenderer.setColor(1.0f, 0.82f * pulse, 0.25f, 1f);
                float ds = 13f * pulse;
                shapeRenderer.triangle(px, py + ds, px + ds, py, px, py - ds);
                shapeRenderer.triangle(px, py + ds, px - ds, py, px, py - ds);
            } else {
                shapeRenderer.setColor(goldAccent);
                shapeRenderer.circle(px, py, 7f, 16);
            }
        }

        // Live player marker
        Vector3 ppos = player.getPosition();
        worldToMapImage(ppos.x, ppos.z, pt);
        float pMapX = dx + pt[0] * fit;
        float pMapY = dy + dh - pt[1] * fit;
        shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.82f);
        shapeRenderer.rect(pMapX + 15f, pMapY - 9f, 42f, 22f);
        shapeRenderer.setColor(0.2f, 0.9f, 1.0f, 0.30f + 0.18f * MathUtils.sin(animTime * 4f));
        shapeRenderer.circle(pMapX, pMapY, 15f, 24);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(pMapX, pMapY, 7.5f, 18);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(pMapX, pMapY, 5.5f, 18);
        float headRad = (player.getHeadingDegrees() - 90f) * MathUtils.degreesToRadians;
        float tipX = pMapX - 22f * MathUtils.cos(headRad);
        float tipY = pMapY + 22f * MathUtils.sin(headRad);
        float leftX = pMapX - 8f * MathUtils.cos(headRad + 2.2f);
        float leftY = pMapY + 8f * MathUtils.sin(headRad + 2.2f);
        float rightX = pMapX - 8f * MathUtils.cos(headRad - 2.2f);
        float rightY = pMapY + 8f * MathUtils.sin(headRad - 2.2f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(dx, dy, dw, dh);
        shapeRenderer.end();

        // Labels: objective name above its pin, "YOU" beside the player, close hint underneath
        spriteBatch.begin();
        if (nextObj != null) {
            worldToMapImage(nextObj.position.x, nextObj.position.z, pt);
            float px = dx + pt[0] * fit;
            float py = dy + dh - pt[1] * fit;
            String label = "OBJECTIVE: " + nextObj.title;
            fonts.smallFont.setColor(1f, 0.88f, 0.40f, 1f);
            fonts.smallFont.draw(spriteBatch, label, px - 80f, py + 35f);
        }
        fonts.smallFont.setColor(0.55f, 1f, 1f, 1f);
        fonts.smallFont.draw(spriteBatch, "YOU", pMapX + 24f, pMapY + 7f);

        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "[M] / [ESC]  Close map", dx + dw - 190f, dy - 6f);
        fonts.smallFont.setColor(new Color(0.70f, 0.82f, 0.90f, 1f));
        fonts.smallFont.draw(spriteBatch, String.format("%.1fE  %.1fN", ppos.x, -ppos.z), dx, dy - 8f);
        spriteBatch.end();
    }

    // ==========================================
    // PAUSE MENU
    // ==========================================
    // ==========================================
    // PAUSE MENU & EXIT CONFIRMATION
    // ==========================================
    private void renderPauseMenu(float w, float h) {
        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.82f);
        shapeRenderer.rect(0, 0, w, h);

        float pw = 980f;
        float ph = 520f;
        float px = (w - pw) / 2f;
        float py = (h - ph) / 2f;

        // Smoked obsidian glass backing
        fillGlassPanel(px, py, pw, ph);

        // Radiant Gold Top Accent
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(px, py + ph - 4f, pw, 4f);

        // Vertical Divider between Menu options and Codex
        float dividerX = px + 390f;
        shapeRenderer.setColor(goldDark.r, goldDark.g, goldDark.b, 0.40f);
        shapeRenderer.rect(dividerX, py + 24f, 1.5f, ph - 48f);

        // Interactive button fills in Left Column
        float btnX = px + 28f;
        float btnW = dividerX - px - 46f;
        float btnH = 38f;
        float startY = py + ph - 105f;
        float spacingY = 46f;

        for (int i = 0; i < 7; i++) {
            float by = startY - i * spacingY;
            if (pauseMenuSelection == i && !isExitConfirmOpen) {
                if (i == 6) {
                    shapeRenderer.setColor(0.92f, 0.35f, 0.28f, 0.25f);
                } else {
                    shapeRenderer.setColor(goldAccent.r, goldAccent.g, goldAccent.b, 0.22f);
                }
                shapeRenderer.rect(btnX, by, btnW, btnH);
            } else {
                shapeRenderer.setColor(0.06f, 0.08f, 0.10f, 0.60f);
                shapeRenderer.rect(btnX, by, btnW, btnH);
            }
        }

        // Rule of July Highlight Box at bottom of right column
        float ruleX = dividerX + 24f;
        float ruleY = py + 24f;
        float ruleW = pw - (dividerX - px) - 48f;
        float ruleH = 58f;
        shapeRenderer.setColor(0.18f, 0.08f, 0.04f, 0.70f);
        shapeRenderer.rect(ruleX, ruleY, ruleW, ruleH);
        shapeRenderer.setColor(goldAccent.r, goldAccent.g, goldAccent.b, 0.12f);
        shapeRenderer.rect(ruleX, ruleY + ruleH - 2f, ruleW, 2f);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        strokeGlassBorder(px, py, pw, ph);

        // Button borders and corner brackets
        for (int i = 0; i < 7; i++) {
            float by = startY - i * spacingY;
            if (pauseMenuSelection == i && !isExitConfirmOpen) {
                if (i == 6) {
                    shapeRenderer.setColor(0.95f, 0.40f, 0.35f, 1f);
                } else {
                    shapeRenderer.setColor(goldAccent);
                }
                shapeRenderer.rect(btnX, by, btnW, btnH);
                // Corner brackets
                float cb = 8f;
                shapeRenderer.line(btnX - 2f, by + btnH + 2f, btnX - 2f + cb, by + btnH + 2f);
                shapeRenderer.line(btnX - 2f, by + btnH + 2f, btnX - 2f, by + btnH + 2f - cb);
                shapeRenderer.line(btnX + btnW + 2f, by + btnH + 2f, btnX + btnW + 2f - cb, by + btnH + 2f);
                shapeRenderer.line(btnX + btnW + 2f, by + btnH + 2f, btnX + btnW + 2f, by + btnH + 2f - cb);
                shapeRenderer.line(btnX - 2f, by - 2f, btnX - 2f + cb, by - 2f);
                shapeRenderer.line(btnX - 2f, by - 2f, btnX - 2f, by - 2f + cb);
                shapeRenderer.line(btnX + btnW + 2f, by - 2f, btnX + btnW + 2f - cb, by - 2f);
                shapeRenderer.line(btnX + btnW + 2f, by - 2f, btnX + btnW + 2f, by - 2f + cb);
            } else {
                shapeRenderer.setColor(goldDark.r, goldDark.g, goldDark.b, 0.45f);
                shapeRenderer.rect(btnX, by, btnW, btnH);
            }
        }

        // Border around Rule box
        shapeRenderer.setColor(0.92f, 0.55f, 0.22f, 0.80f);
        shapeRenderer.rect(ruleX, ruleY, ruleW, ruleH);

        // Decorative corner brackets on main pause panel
        float cb = 16f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(px - 4f, py + ph + 4f, px - 4f + cb, py + ph + 4f);
        shapeRenderer.line(px - 4f, py + ph + 4f, px - 4f, py + ph + 4f - cb);
        shapeRenderer.line(px + pw + 4f, py + ph + 4f, px + pw + 4f - cb, py + ph + 4f);
        shapeRenderer.line(px + pw + 4f, py + ph + 4f, px + pw + 4f, py + ph + 4f - cb);
        shapeRenderer.line(px - 4f, py - 4f, px - 4f + cb, py - 4f);
        shapeRenderer.line(px - 4f, py - 4f, px - 4f, py - 4f + cb);
        shapeRenderer.line(px + pw + 4f, py - 4f, px + pw + 4f - cb, py - 4f);
        shapeRenderer.line(px + pw + 4f, py - 4f, px + pw + 4f, py - 4f + cb);
        shapeRenderer.end();

        spriteBatch.begin();
        // Left Column: Menu Header
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "GAME PAUSED", px + 36f, py + ph - 22f);

        fonts.smallFont.setColor(goldMuted);
        fonts.smallFont.draw(spriteBatch, "2x12: Spark • Birth of 36 July", px + 38f, py + ph - 50f);

        // Menu items labels
        String tmName;
        int tmMode = bd.spark36.render.RenderPipelineConfig.toneMappingMode;
        if (tmMode == 1) tmName = "ACES FILMIC";
        else if (tmMode == 2) tmName = "REINHARD";
        else tmName = "LINEAR";

        String tierName = bd.spark36.render.RenderPipelineConfig.activeGraphicsTier.name();

        String[] itemLabels = {
            "RESUME GAME",
            "CONTROLS & CODEX",
            "GRAPHICS: " + tierName,
            "TONE MAP: " + tmName,
            "RESTART LEVEL",
            "MAIN MENU",
            "EXIT GAME"
        };
        String[] itemShortcuts = {"[ESC]", "[H]", "[G]", "[T]", "[R]", "[Q]", "[X]"};

        for (int i = 0; i < 7; i++) {
            float by = startY - i * spacingY;
            boolean sel = (pauseMenuSelection == i && !isExitConfirmOpen);

            if (sel) {
                fonts.headerFont.setColor((i == 6) ? new Color(1.0f, 0.45f, 0.40f, 1f) : goldAccent);
            } else {
                fonts.headerFont.setColor((i == 6) ? new Color(0.92f, 0.45f, 0.40f, 0.9f) : ((i == 5) ? new Color(1f, 0.85f, 0.45f, 0.9f) : new Color(0.90f, 0.88f, 0.85f, 1f)));
            }

            fonts.headerFont.draw(spriteBatch, itemShortcuts[i] + "  " + itemLabels[i], btnX + 16f, by + btnH / 2f + 8f);
        }

        // Hints underneath buttons
        fonts.smallFont.setColor(new Color(0.70f, 0.68f, 0.64f, 0.85f));
        fonts.smallFont.draw(spriteBatch, "[M] Campus Map  •  [P] Parameters  •  [G] Switch Gender", btnX, py + 56f);

        fonts.smallFont.setColor(new Color(0.55f, 0.52f, 0.50f, 0.80f));
        fonts.smallFont.draw(spriteBatch, "Dhaka University Campus • July 2024", btnX, py + 34f);

        // Right Column: Martial Arts & Controls Codex
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "MARTIAL ARTS & CONTROLS CODEX", dividerX + 26f, py + ph - 24f);

        fonts.smallFont.setColor(new Color(0.85f, 0.82f, 0.75f, 0.95f));
        fonts.smallFont.draw(spriteBatch, "NAVIGATION & ATHLETICS", dividerX + 26f, py + ph - 56f);

        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch, "• WASD / Arrows : Movement (Camera-Relative Direction)", dividerX + 32f, py + ph - 80f);
        fonts.bodyFont.draw(spriteBatch, "• Shift / R : Free Sprint (Unlimited Exploration Stamina)", dividerX + 32f, py + ph - 104f);
        fonts.bodyFont.draw(spriteBatch, "• J / Space : Athletic Jump (Double-Tap for Mid-Air Double Jump!)", dividerX + 32f, py + ph - 128f);
        fonts.bodyFont.draw(spriteBatch, "• Hold J at Wall : Scale Vertical Walls & Vault onto Roofs", dividerX + 32f, py + ph - 152f);
        fonts.bodyFont.draw(spriteBatch, "• C : Crouch / Duck (Bosepora) — Evades High Lathi Swings", dividerX + 32f, py + ph - 176f);
        fonts.bodyFont.draw(spriteBatch, "• Z / ALT : Tactical Combat Slip & Evasion (i-frames)", dividerX + 32f, py + ph - 200f);

        fonts.smallFont.setColor(new Color(0.85f, 0.82f, 0.75f, 0.95f));
        fonts.smallFont.draw(spriteBatch, "TECHNICAL MARTIAL ARTS COMBAT", dividerX + 26f, py + ph - 228f);

        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch, "• Left-Click / F : Bare-Handed Punches (Jab -> Cross Combo)", dividerX + 32f, py + ph - 250f);
        fonts.bodyFont.draw(spriteBatch, "• Q / K : High Martial Arts Kick (Roundhouse Strike)", dividerX + 32f, py + ph - 272f);
        fonts.bodyFont.draw(spriteBatch, "• C + Attack : Low Dragon Leg Sweep (Tripping Counter Strike)", dividerX + 32f, py + ph - 294f);
        fonts.bodyFont.draw(spriteBatch, "• B (Hold) : Defensive Guard (Reduces Damage by 80%)", dividerX + 32f, py + ph - 316f);
        fonts.bodyFont.draw(spriteBatch, "• B (Tap / Timed) : Flash Parry (Stuns Goon & breaks posture!)", dividerX + 32f, py + ph - 338f);
        fonts.bodyFont.draw(spriteBatch, "• Counter-Hit : Strike during Goon wind-up for 1.5x interrupt damage!", dividerX + 32f, py + ph - 360f);

        // Rule of July Text
        fonts.smallFont.setColor(new Color(1.0f, 0.82f, 0.35f, 1f));
        fonts.smallFont.draw(spriteBatch, "CAMPUS RULE OF JULY:", ruleX + 16f, ruleY + ruleH - 14f);
        fonts.smallFont.setColor(new Color(0.95f, 0.92f, 0.88f, 0.90f));
        fonts.smallFont.draw(spriteBatch, "Striking unarmed, innocent students results in instant disqualification.", ruleX + 16f, ruleY + ruleH - 34f);
        spriteBatch.end();

        // Exit Confirmation Dialog Overlay (if active)
        if (isExitConfirmOpen) {
            renderExitConfirmationDialog(w, h);
        }
    }

    /**
     * Renders the modal Exit Confirmation Dialog on top of the pause menu.
     */
    private void renderExitConfirmationDialog(float w, float h) {
        float cw = 640f;
        float ch = 280f;
        float cx = (w - cw) / 2f;
        float cy = (h - ch) / 2f;

        // Dark dimming backdrop
        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.85f);
        shapeRenderer.rect(0, 0, w, h);

        fillGlassPanel(cx, cy, cw, ch);

        // Top accent bar (Crimson alert)
        shapeRenderer.setColor(0.92f, 0.35f, 0.28f, 1f);
        shapeRenderer.rect(cx, cy + ch - 4f, cw, 4f);

        // Buttons filled background
        float btnY = cy + 34f;
        float btnH = 48f;
        float b0X = cx + 45f, b0W = 250f;
        float b1X = cx + cw - 295f, b1W = 250f;

        if (exitConfirmSelection == 0) {
            shapeRenderer.setColor(goldAccent.r, goldAccent.g, goldAccent.b, 0.24f);
            shapeRenderer.rect(b0X, btnY, b0W, btnH);
            shapeRenderer.setColor(0.06f, 0.08f, 0.10f, 0.60f);
            shapeRenderer.rect(b1X, btnY, b1W, btnH);
        } else {
            shapeRenderer.setColor(0.06f, 0.08f, 0.10f, 0.60f);
            shapeRenderer.rect(b0X, btnY, b0W, btnH);
            shapeRenderer.setColor(0.92f, 0.35f, 0.28f, 0.28f);
            shapeRenderer.rect(b1X, btnY, b1W, btnH);
        }
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        strokeGlassBorder(cx, cy, cw, ch);

        // Button borders & corner brackets
        shapeRenderer.setColor(exitConfirmSelection == 0 ? goldAccent : new Color(goldDark.r, goldDark.g, goldDark.b, 0.5f));
        shapeRenderer.rect(b0X, btnY, b0W, btnH);

        shapeRenderer.setColor(exitConfirmSelection == 1 ? new Color(0.98f, 0.42f, 0.36f, 1f) : new Color(0.70f, 0.30f, 0.25f, 0.5f));
        shapeRenderer.rect(b1X, btnY, b1W, btnH);

        // Corner brackets on selected button
        float cb = 10f;
        float selX = (exitConfirmSelection == 0) ? b0X : b1X;
        float selW = (exitConfirmSelection == 0) ? b0W : b1W;
        Color selCol = (exitConfirmSelection == 0) ? goldAccent : new Color(0.98f, 0.42f, 0.36f, 1f);
        shapeRenderer.setColor(selCol);
        shapeRenderer.line(selX - 2f, btnY + btnH + 2f, selX - 2f + cb, btnY + btnH + 2f);
        shapeRenderer.line(selX - 2f, btnY + btnH + 2f, selX - 2f, btnY + btnH + 2f - cb);
        shapeRenderer.line(selX + selW + 2f, btnY + btnH + 2f, selX + selW + 2f - cb, btnY + btnH + 2f);
        shapeRenderer.line(selX + selW + 2f, btnY + btnH + 2f, selX + selW + 2f, btnY + btnH + 2f - cb);
        shapeRenderer.line(selX - 2f, btnY - 2f, selX - 2f + cb, btnY - 2f);
        shapeRenderer.line(selX - 2f, btnY - 2f, selX - 2f, btnY - 2f + cb);
        shapeRenderer.line(selX + selW + 2f, btnY - 2f, selX + selW + 2f - cb, btnY - 2f);
        shapeRenderer.line(selX + selW + 2f, btnY - 2f, selX + selW + 2f, btnY - 2f + cb);

        // Dialog outer corner brackets
        float dcb = 14f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(cx - 3f, cy + ch + 3f, cx - 3f + dcb, cy + ch + 3f);
        shapeRenderer.line(cx - 3f, cy + ch + 3f, cx - 3f, cy + ch + 3f - dcb);
        shapeRenderer.line(cx + cw + 3f, cy + ch + 3f, cx + cw + 3f - dcb, cy + ch + 3f);
        shapeRenderer.line(cx + cw + 3f, cy + ch + 3f, cx + cw + 3f, cy + ch + 3f - dcb);
        shapeRenderer.line(cx - 3f, cy - 3f, cx - 3f + dcb, cy - 3f);
        shapeRenderer.line(cx - 3f, cy - 3f, cx - 3f, cy - 3f + dcb);
        shapeRenderer.line(cx + cw + 3f, cy - 3f, cx + cw + 3f - dcb, cy - 3f);
        shapeRenderer.line(cx + cw + 3f, cy - 3f, cx + cw + 3f, cy - 3f + dcb);
        shapeRenderer.end();

        spriteBatch.begin();
        // Title: EXIT GAME?
        fonts.titleFont.setColor(new Color(0.98f, 0.42f, 0.36f, 1f));
        glyphLayout.setText(fonts.titleFont, "EXIT GAME?");
        fonts.titleFont.draw(spriteBatch, "EXIT GAME?", cx + (cw - glyphLayout.width) / 2f, cy + ch - 26f);

        // Warning text
        fonts.bodyFont.setColor(new Color(0.90f, 0.88f, 0.84f, 1f));
        String msg1 = "Are you sure you want to quit to desktop?";
        glyphLayout.setText(fonts.bodyFont, msg1);
        fonts.bodyFont.draw(spriteBatch, msg1, cx + (cw - glyphLayout.width) / 2f, cy + ch - 78f);

        fonts.smallFont.setColor(new Color(0.70f, 0.66f, 0.60f, 0.9f));
        String msg2 = "Any unsaved progress in the current chapter will be lost.";
        glyphLayout.setText(fonts.smallFont, msg2);
        fonts.smallFont.draw(spriteBatch, msg2, cx + (cw - glyphLayout.width) / 2f, cy + ch - 110f);

        // Button 0: Cancel
        fonts.headerFont.setColor(exitConfirmSelection == 0 ? goldAccent : new Color(0.85f, 0.82f, 0.78f, 0.95f));
        String cancelLabel = "[ESC] CANCEL";
        glyphLayout.setText(fonts.headerFont, cancelLabel);
        fonts.headerFont.draw(spriteBatch, cancelLabel, b0X + (b0W - glyphLayout.width) / 2f, btnY + btnH / 2f + 8f);

        // Button 1: Quit
        fonts.headerFont.setColor(exitConfirmSelection == 1 ? new Color(1.0f, 0.45f, 0.40f, 1f) : new Color(0.90f, 0.45f, 0.40f, 0.85f));
        String quitLabel = "[ENTER] QUIT GAME";
        glyphLayout.setText(fonts.headerFont, quitLabel);
        fonts.headerFont.draw(spriteBatch, quitLabel, b1X + (b1W - glyphLayout.width) / 2f, btnY + btnH / 2f + 8f);

        spriteBatch.end();
    }

    /**
     * Renders full on-screen Controls & Martial Arts Codex Overlay.
     */
    private void renderHelpControlsScreen(float w, float h) {
        float hw = Math.min(1080f, w - 80f);
        float hh = Math.min(620f, h - 60f);
        float hx = (w - hw) / 2f;
        float hy = (h - hh) / 2f;

        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.88f);
        shapeRenderer.rect(0, 0, w, h);

        fillGlassPanel(hx, hy, hw, hh);

        // Radiant Gold Top Accent
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(hx, hy + hh - 4f, hw, 4f);

        // Vertical divider between Locomotion and Combat
        float midX = hx + hw * 0.50f;
        shapeRenderer.setColor(goldDark.r, goldDark.g, goldDark.b, 0.35f);
        shapeRenderer.rect(midX, hy + 70f, 1.5f, hh - 140f);

        // Close button background
        float closeBtnX = hx + hw - 170f;
        float closeBtnY = hy + 20f;
        float closeBtnW = 140f;
        float closeBtnH = 38f;
        shapeRenderer.setColor(goldAccent.r, goldAccent.g, goldAccent.b, 0.20f);
        shapeRenderer.rect(closeBtnX, closeBtnY, closeBtnW, closeBtnH);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        strokeGlassBorder(hx, hy, hw, hh);

        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(closeBtnX, closeBtnY, closeBtnW, closeBtnH);

        // Corner brackets on outer panel
        float cb = 16f;
        shapeRenderer.line(hx - 4f, hy + hh + 4f, hx - 4f + cb, hy + hh + 4f);
        shapeRenderer.line(hx - 4f, hy + hh + 4f, hx - 4f, hy + hh + 4f - cb);
        shapeRenderer.line(hx + hw + 4f, hy + hh + 4f, hx + hw + 4f - cb, hy + hh + 4f);
        shapeRenderer.line(hx + hw + 4f, hy + hh + 4f, hx + hw + 4f, hy + hh + 4f - cb);
        shapeRenderer.line(hx - 4f, hy - 4f, hx - 4f + cb, hy - 4f);
        shapeRenderer.line(hx - 4f, hy - 4f, hx - 4f, hy - 4f + cb);
        shapeRenderer.line(hx + hw + 4f, hy - 4f, hx + hw + 4f - cb, hy - 4f);
        shapeRenderer.line(hx + hw + 4f, hy - 4f, hx + hw + 4f, hy - 4f + cb);
        shapeRenderer.end();

        spriteBatch.begin();
        // Title & Subtitle
        fonts.titleFont.setColor(goldAccent);
        fonts.titleFont.draw(spriteBatch, "CONTROLS & HISTORICAL FIELD CODEX", hx + 36f, hy + hh - 24f);

        fonts.smallFont.setColor(goldMuted);
        fonts.smallFont.draw(spriteBatch, "Spark: Birth of 36 July • University of Dhaka Campus", hx + 38f, hy + hh - 52f);

        // Left Column: Movement & Campus Exploration
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "LOCOMOTION & ATHLETICS", hx + 36f, hy + hh - 86f);

        float leftX = hx + 40f;
        float lineY = hy + hh - 114f;
        float stepY = 24f;

        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch, "• W / A / S / D or Arrows : 8-Way Movement (Camera Relative)", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Left Shift / R : Free Sprint (Unlimited Exploration Stamina)", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Space / J : Athletic Jump (Double-Tap Mid-Air for Double Jump)", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Hold Space at Wall : Vertical Wall Clamber & Ledge Vault", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• C / Left Ctrl : Crouch / Duck (Bosepora) & Sneak", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Z / Left Alt : Tactical Evasion Slip & Dodge (i-frames)", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• E / Enter : Interact (Inspect Archives, Talk to Students, Sit by Pond)", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• M / Tab : Dhaka University Campus Map & Landmark Directory", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• P : Toggle Objective Checklist & Parameters Tracker", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• G : Switch Gender (Toggle Male / Female Student)", leftX, lineY); lineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• H / F1 : Toggle this Controls Codex anytime", leftX, lineY);

        // Right Column: Martial Arts Defense & Combat
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "MARTIAL ARTS COMBAT & DEFENSE", midX + 24f, hy + hh - 86f);

        float rightX = midX + 28f;
        float rLineY = hy + hh - 114f;

        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch, "• Left-Click / F : Bare-Handed Punch (Jab -> Cross -> Heavy Combo)", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Q / K : High Martial Arts Kick (Roundhouse Strike)", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• C + Attack : Low Sweep Counter (Tripping leg sweep)", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• B (Hold) / Right-Click : Defensive Guard (Reduces damage by 80%)", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• B (Timed Tap) : Flash Parry (Stuns aggressor & breaks posture)", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Counter-Hit : Strike during enemy wind-up for 1.5x interrupt damage", rightX, rLineY); rLineY -= stepY;

        fonts.smallFont.setColor(new Color(1.0f, 0.82f, 0.35f, 1f));
        fonts.smallFont.draw(spriteBatch, "HISTORICAL COMBAT RULES:", rightX, rLineY - 8f); rLineY -= (stepY + 6f);
        fonts.bodyFont.setColor(new Color(0.95f, 0.90f, 0.85f, 0.95f));
        fonts.bodyFont.draw(spriteBatch, "• Chapter 1: Students maintain peaceful solidarity. Attacks are disabled.", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Combat unlocks in Chapter 2 ('The Day the Streets Changed') & Arena.", rightX, rLineY); rLineY -= stepY;
        fonts.bodyFont.draw(spriteBatch, "• Striking unarmed innocent students causes instant disqualification.", rightX, rLineY);

        // Bottom Bar info and Close Button
        fonts.smallFont.setColor(goldMuted);
        fonts.smallFont.draw(spriteBatch, "Press [ESC] or [H] to return to game", hx + 40f, hy + 42f);

        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "[ESC] CLOSE", closeBtnX + 16f, closeBtnY + closeBtnH / 2f + 8f);

        spriteBatch.end();
    }

    // ==========================================
    // TOAST NOTIFICATION: MISSION COMPLETE BANNER
    // ==========================================
    // TOAST NOTIFICATION: MISSION COMPLETE BANNER
    // ==========================================
    private void renderMissionCompleteBanner(float w, float h) {
        if (missionBannerTime <= 0f) return;

        // Smooth alpha fade: 0.8s fade in, 0.8s fade out
        float alpha = 1f;
        if (missionBannerTime > 5.2f) {
            alpha = (6.0f - missionBannerTime) / 0.8f;
        } else if (missionBannerTime < 0.8f) {
            alpha = missionBannerTime / 0.8f;
        }
        alpha = MathUtils.clamp(alpha, 0f, 1f);

        float bw = 640f;
        float bh = 76f;
        float bx = (w - bw) / 2f;
        float by = h - 105f;

        // Transparent glass background (zero dark block!)
        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0.01f, 0.02f, 0.04f, 0.20f * alpha);
        shapeRenderer.rect(bx, by, bw, bh);

        // Gold top accent
        shapeRenderer.setColor(1.0f, 0.85f, 0.40f, 0.95f * alpha);
        shapeRenderer.rect(bx, by + bh - 3f, bw, 3f);

        // Gold diamond icon on left
        float dx = bx + 28f;
        float dy = by + bh / 2f;
        float ds = 8f;
        shapeRenderer.triangle(dx, dy + ds, dx + ds, dy, dx, dy - ds);
        shapeRenderer.triangle(dx, dy + ds, dx - ds, dy, dx, dy - ds);
        shapeRenderer.end();

        // Border
        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(0.92f, 0.76f, 0.32f, 0.90f * alpha);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();

        // Text with drop shadows
        spriteBatch.begin();
        fonts.titleFont.setColor(0f, 0f, 0f, 0.85f * alpha);
        fonts.titleFont.draw(spriteBatch, "* MISSION COMPLETE! *", bx + 50f + 2f, by + bh - 16f);
        fonts.titleFont.setColor(1.0f, 0.88f, 0.40f, alpha);
        fonts.titleFont.draw(spriteBatch, "* MISSION COMPLETE! *", bx + 50f, by + bh - 14f);

        fonts.headerFont.setColor(0f, 0f, 0f, 0.85f * alpha);
        String sub = String.format("%s  |  [ %d / 5 Archives Secured ]", missionBannerTitle, missionBannerCount);
        fonts.headerFont.draw(spriteBatch, sub, bx + 50f + 1.5f, by + 26f);
        fonts.headerFont.setColor(1f, 1f, 1f, 0.95f * alpha);
        fonts.headerFont.draw(spriteBatch, sub, bx + 50f, by + 28f);
        spriteBatch.end();
    }

    // ==========================================
    // CELEBRATORY TOAST: CONGRATULATIONS POPUP
    // ==========================================
    private void renderCongratulationsPopup(float w, float h) {
        if (congratulationsPopupTimer <= 0f) return;

        // Fade in (first 0.35s) and fade out (last 0.45s)
        float alpha = 1f;
        float elapsed = 3.2f - congratulationsPopupTimer;
        if (elapsed < 0.35f) {
            alpha = elapsed / 0.35f;
        } else if (congratulationsPopupTimer < 0.45f) {
            alpha = congratulationsPopupTimer / 0.45f;
        }
        alpha = MathUtils.clamp(alpha, 0f, 1f);

        float pw = 840f;
        float ph = 96f;
        float px = (w - pw) / 2f;
        float py = h - 114f;

        // Frosted translucent dark backing with soft gold tint
        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0.02f, 0.04f, 0.08f, 0.92f * alpha);
        shapeRenderer.rect(px, py, pw, ph);

        // Warm gold header banner stripe
        shapeRenderer.setColor(0.24f, 0.18f, 0.06f, 0.92f * alpha);
        shapeRenderer.rect(px, py + ph - 28f, pw, 28f);

        // Gold top accent line
        shapeRenderer.setColor(1.0f, 0.86f, 0.38f, 0.98f * alpha);
        shapeRenderer.rect(px, py + ph - 3f, pw, 3f);

        // Side diamond accents
        float leftDiaX = px + 28f;
        float rightDiaX = px + pw - 28f;
        float diaY = py + ph / 2f;
        float ds = 10f;
        shapeRenderer.triangle(leftDiaX, diaY + ds, leftDiaX + ds, diaY, leftDiaX, diaY - ds);
        shapeRenderer.triangle(leftDiaX, diaY + ds, leftDiaX - ds, diaY, leftDiaX, diaY - ds);
        shapeRenderer.triangle(rightDiaX, diaY + ds, rightDiaX + ds, diaY, rightDiaX, diaY - ds);
        shapeRenderer.triangle(rightDiaX, diaY + ds, rightDiaX - ds, diaY, rightDiaX, diaY - ds);
        shapeRenderer.end();

        // Elegant double borders
        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(0.92f, 0.78f, 0.34f, 0.92f * alpha);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(0.60f, 0.50f, 0.20f, 0.50f * alpha);
        shapeRenderer.rect(px + 4f, py + 4f, pw - 8f, ph - 8f);

        // Corner filigree brackets
        float cLen = 14f;
        shapeRenderer.setColor(1.0f, 0.88f, 0.40f, alpha);
        shapeRenderer.line(px - 2f, py + ph + 2f, px + cLen, py + ph + 2f);
        shapeRenderer.line(px - 2f, py + ph + 2f, px - 2f, py + ph - cLen);
        shapeRenderer.line(px + pw + 2f, py + ph + 2f, px + pw - cLen, py + ph + 2f);
        shapeRenderer.line(px + pw + 2f, py + ph + 2f, px + pw + 2f, py + ph - cLen);
        shapeRenderer.line(px - 2f, py - 2f, px + cLen, py - 2f);
        shapeRenderer.line(px - 2f, py - 2f, px - 2f, py + cLen);
        shapeRenderer.line(px + pw + 2f, py - 2f, px + pw - cLen, py - 2f);
        shapeRenderer.line(px + pw + 2f, py - 2f, px + pw + 2f, py + cLen);
        shapeRenderer.end();

        // Typography
        spriteBatch.begin();
        // Title
        String titleStr = "* CONGRATULATIONS! LEVEL 1 COMPLETE *";
        glyphLayout.setText(fonts.titleFont, titleStr);
        float titleX = px + (pw - glyphLayout.width) / 2f;
        fonts.titleFont.setColor(0f, 0f, 0f, 0.90f * alpha);
        fonts.titleFont.draw(spriteBatch, titleStr, titleX + 2f, py + ph - 7f);
        fonts.titleFont.setColor(1.0f, 0.88f, 0.38f, alpha);
        fonts.titleFont.draw(spriteBatch, titleStr, titleX, py + ph - 5f);

        // Subtitle
        String subStr = "Campus Liberated: The Intimidation Enforcer Has Been Neutralized!";
        glyphLayout.setText(fonts.headerFont, subStr);
        float subX = px + (pw - glyphLayout.width) / 2f;
        fonts.headerFont.setColor(0f, 0f, 0f, 0.90f * alpha);
        fonts.headerFont.draw(spriteBatch, subStr, subX + 1.5f, py + 41f);
        fonts.headerFont.setColor(1f, 1f, 1f, 0.95f * alpha);
        fonts.headerFont.draw(spriteBatch, subStr, subX, py + 42f);

        // Status Prompt
        String promptStr = "Preparing Final Mission Debrief... [ENTER / CLICK to Proceed]";
        glyphLayout.setText(fonts.smallFont, promptStr);
        float promptX = px + (pw - glyphLayout.width) / 2f;
        float pulse = 0.75f + 0.25f * MathUtils.sin(animTime * 6f);
        fonts.smallFont.setColor(0.95f, 0.85f, 0.45f, alpha * pulse);
        fonts.smallFont.draw(spriteBatch, promptStr, promptX, py + 18f);
        spriteBatch.end();
    }

    // ==========================================
    // 4b. BOSS HEALTH BAR & COMBAT HUD
    // ==========================================
    private void renderBossHealthBar(bd.spark36.character.HelmetGoon goon, float w, float h) {
        float bw = currentLayout.bossW;
        float bh = 16f;
        float bx = currentLayout.bossX;
        float by = currentLayout.bossY + 18f;

        beginShapes(ShapeType.Filled);
        // Smoked glass panel behind boss bar (accommodates health and posture poise bars)
        fillGlassPanel(bx - 14f, by - 34f, bw + 28f, bh + 54f);

        // Health Bar Track
        shapeRenderer.setColor(0.18f, 0.08f, 0.08f, 0.88f);
        shapeRenderer.rect(bx, by, bw, bh);

        // Boss Health Fill
        float pct = MathUtils.clamp(goon.getHealth() / goon.getMaxHealth(), 0f, 1f);
        float fillW = bw * pct;
        if (fillW > 0f) {
            Color barCol = goon.isKnockedOut() ? Color.GREEN : new Color(0.92f, 0.20f, 0.16f, 0.95f);
            shapeRenderer.setColor(barCol);
            shapeRenderer.rect(bx, by, fillW, bh);

            // Glowing highlight line at top of fill
            shapeRenderer.setColor(1.0f, 0.65f, 0.35f, 0.40f);
            shapeRenderer.rect(bx, by + bh - 3f, fillW, 3f);
        }

        // Boss Posture / Poise Meter (Technical Martial Arts Poise Bar)
        float py = by - 8f;
        float ph = 6f;
        shapeRenderer.setColor(0.14f, 0.10f, 0.06f, 0.90f);
        shapeRenderer.rect(bx, py, bw, ph);

        float poisePct = MathUtils.clamp(goon.getPoise() / goon.getMaxPoise(), 0f, 1f);
        float poiseW = bw * poisePct;
        if (poiseW > 0f) {
            Color poiseCol = goon.isGuardBroken() ?
                (animTime % 0.3f < 0.15f ? Color.RED : Color.YELLOW) :
                new Color(1.0f, 0.68f, 0.18f, 0.95f);
            shapeRenderer.setColor(poiseCol);
            shapeRenderer.rect(bx, py, poiseW, ph);
        }

        // Diamond crest at top center
        shapeRenderer.setColor(goldAccent);
        float cdX = bx + bw / 2f;
        float cdY = by + bh + 14f;
        float cdS = 5f;
        shapeRenderer.triangle(cdX, cdY + cdS, cdX + cdS, cdY, cdX, cdY - cdS);
        shapeRenderer.triangle(cdX, cdY + cdS, cdX - cdS, cdY, cdX, cdY - cdS);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        strokeGlassBorder(bx, by, bw, bh);
        strokeGlassBorder(bx, py, bw, ph);

        // Corner teeth
        shapeRenderer.setColor(goldAccent);
        float cL = 8f;
        shapeRenderer.line(bx - 3f, by + bh + 3f, bx + cL, by + bh + 3f);
        shapeRenderer.line(bx - 3f, by + bh + 3f, bx - 3f, by + bh - cL);
        shapeRenderer.line(bx + bw + 3f, by + bh + 3f, bx + bw - cL, by + bh + 3f);
        shapeRenderer.line(bx + bw + 3f, by + bh + 3f, bx + bw + 3f, by + bh - cL);
        shapeRenderer.line(bx - 3f, py - 3f, bx + cL, py - 3f);
        shapeRenderer.line(bx - 3f, py - 3f, bx - 3f, py + cL);
        shapeRenderer.line(bx + bw + 3f, py - 3f, bx + bw - cL, py - 3f);
        shapeRenderer.line(bx + bw + 3f, py - 3f, bx + bw + 3f, py + cL);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.headerFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.headerFont.draw(spriteBatch, "HELMETED AGGRESSOR ENFORCER", bx + 10f + 1f, by + bh + 19f);
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "HELMETED AGGRESSOR ENFORCER", bx + 10f, by + bh + 20f);

        String subTitle = goon.isGuardBroken() ?
            "[!] GUARD BROKEN! CRITICAL OPENING! [!]" :
            "HELMETED AGGRESSOR • LATHI WIELDER  |  POSTURE: " + (int)goon.getPoise() + "%";
        fonts.smallFont.setColor(goon.isGuardBroken() ? Color.RED : new Color(0.85f, 0.82f, 0.78f, 0.90f));
        fonts.smallFont.draw(spriteBatch, subTitle, bx + 10f, by - 16f);

        String hpText = goon.isKnockedOut() ? "DEFEATED / KNOCKED OUT" : String.format("HP: %d / %d", (int)goon.getHealth(), (int)goon.getMaxHealth());
        fonts.headerFont.setColor(goon.isKnockedOut() ? Color.GREEN : new Color(1.0f, 0.92f, 0.80f, 1f));
        glyphLayout.setText(fonts.headerFont, hpText);
        fonts.headerFont.draw(spriteBatch, hpText, bx + bw - glyphLayout.width - 8f, by + bh + 20f);
        spriteBatch.end();
    }

    private void renderBossChallengeDialogue(bd.spark36.character.HelmetGoon goon, float w, float h) {
        float dw = currentLayout.dialW;
        float dh = currentLayout.dialH;
        float dx = currentLayout.dialX;
        float dy = currentLayout.dialY;

        beginShapes(ShapeType.Filled);
        fillGlassPanel(dx, dy, dw, dh);
        shapeRenderer.setColor(1.0f, 0.55f, 0.20f, 0.90f);
        shapeRenderer.rect(dx, dy + dh - 3f, dw, 3f);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        strokeGlassBorder(dx, dy, dw, dh);
        shapeRenderer.end();

        spriteBatch.begin();
        bd.spark36.level.DialogueRepository.DialogueLine diag = bd.spark36.level.DialogueRepository.AGGRESSOR_CHALLENGE;
        fonts.headerFont.setColor(new Color(1.0f, 0.65f, 0.30f, 1f));
        fonts.headerFont.draw(spriteBatch, diag.getSpeaker() + " :", dx + 18f, dy + dh - 16f);

        // Authentic Bengali threat subtitle
        com.badlogic.gdx.graphics.g2d.BitmapFont bFont = (fonts.bengaliFont != null) ? fonts.bengaliFont : fonts.headerFont;
        bFont.setColor(Color.WHITE);
        bFont.draw(spriteBatch, "\"" + diag.getTextBengali() + "\"", dx + 265f, dy + dh - 16f);

        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "\"" + diag.getTextEnglish() + "\"  |  [F] ACCEPT CHALLENGE   [LMB/F] PUNCH   [Q/K] KICK   [C] DUCK", dx + 18f, dy + 22f);
        spriteBatch.end();
    }

    private void renderStudentNpcDialogue(bd.spark36.level.DialogueRepository.DialogueLine diag, float w, float h) {
        float dw = currentLayout.dialW;
        float dh = currentLayout.dialH;
        float dx = currentLayout.dialX;
        float dy = currentLayout.dialY;

        beginShapes(ShapeType.Filled);
        fillGlassPanel(dx, dy, dw, dh);
        shapeRenderer.setColor(0.32f, 0.85f, 0.52f, 0.90f); // Emerald top stripe for student dialogue
        shapeRenderer.rect(dx, dy + dh - 3f, dw, 3f);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        strokeGlassBorder(dx, dy, dw, dh);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.headerFont.setColor(new Color(0.42f, 0.95f, 0.65f, 1f));
        fonts.headerFont.draw(spriteBatch, diag.getSpeaker() + " :", dx + 18f, dy + dh - 16f);

        // Authentic Bengali student voice
        com.badlogic.gdx.graphics.g2d.BitmapFont bFont = (fonts.bengaliFont != null) ? fonts.bengaliFont : fonts.headerFont;
        bFont.setColor(Color.WHITE);
        bFont.draw(spriteBatch, "\"" + diag.getTextBengali() + "\"", dx + 285f, dy + dh - 16f);

        fonts.promptFont.setColor(goldAccent);
        fonts.promptFont.draw(spriteBatch, "\"" + diag.getTextEnglish() + "\"", dx + 18f, dy + 22f);

        // Advance prompt if sequence is active (placed in bottom-right corner as a distinct key badge)
        if (dialogueManager.isDialogueActive()) {
            int cur = dialogueManager.getCurrentLineIndex() + 1;
            int tot = dialogueManager.getTotalLines();
            String promptText = dialogueManager.isLastLine() ? "[SPACE] FINISH" : "[SPACE] NEXT (" + cur + "/" + tot + ")";
            glyphLayout.setText(fonts.keyFont, promptText);
            float badgeW = glyphLayout.width + 16f;
            float badgeH = 22f;
            float badgeX = dx + dw - badgeW - 14f;
            float badgeY = dy + 12f;

            spriteBatch.end();
            beginShapes(ShapeType.Filled);
            shapeRenderer.setColor(0.08f, 0.12f, 0.10f, 0.95f);
            shapeRenderer.rect(badgeX, badgeY, badgeW, badgeH);
            shapeRenderer.setColor(goldAccent);
            shapeRenderer.rect(badgeX, badgeY, 2f, badgeH);
            shapeRenderer.end();
            spriteBatch.begin();

            fonts.keyFont.setColor(goldAccent);
            fonts.keyFont.draw(spriteBatch, promptText, badgeX + 9f, badgeY + 16f);
        }
        spriteBatch.end();
    }

    private void renderCombatFloatingPopup(String text, Color color, float w, float h) {
        spriteBatch.begin();
        glyphLayout.setText(fonts.titleFont, text);
        float tx = (w - glyphLayout.width) / 2f;
        float ty = h / 2f + 115f;

        fonts.titleFont.setColor(0f, 0f, 0f, 0.92f);
        fonts.titleFont.draw(spriteBatch, text, tx + 2f, ty - 2f);
        fonts.titleFont.setColor(color != null ? color : Color.WHITE);
        fonts.titleFont.draw(spriteBatch, text, tx, ty);
        spriteBatch.end();
    }

    // ==========================================
    // DISQUALIFICATION OVERLAY (Campus Peace Rule)
    // ==========================================
    private void renderDisqualificationScreen(float w, float h) {
        float[] vMouse = GameInput.getVirtualMousePos(w, h);
        float mouseX = vMouse[0];
        float mouseY = vMouse[1];

        beginShapes(ShapeType.Filled);
        // Dim screen background with dark crimson tint
        shapeRenderer.setColor(0.12f, 0.02f, 0.02f, 0.82f);
        shapeRenderer.rect(0, 0, w, h);

        float dw = 800f;
        float dh = 440f;
        float dx = (w - dw) / 2f;
        float dy = (h - dh) / 2f;

        // Smoked glass panel
        shapeRenderer.setColor(0.05f, 0.02f, 0.03f, 0.92f);
        shapeRenderer.rect(dx, dy, dw, dh);

        // Header band in danger red
        shapeRenderer.setColor(0.35f, 0.06f, 0.08f, 0.92f);
        shapeRenderer.rect(dx, dy + dh - 70f, dw, 70f);

        // Top crimson accent line
        shapeRenderer.setColor(1.0f, 0.25f, 0.20f, 1f);
        shapeRenderer.rect(dx, dy + dh - 4f, dw, 4f);

        // Sub-box for rule explanation
        float sx = dx + 35f;
        float sy = dy + 110f;
        float sw = dw - 70f;
        float sh = 88f;
        shapeRenderer.setColor(0.10f, 0.04f, 0.05f, 0.85f);
        shapeRenderer.rect(sx, sy, sw, sh);

        // Buttons
        float btnY = dy + 35f;
        float btnH = 46f;
        // Button 1: Retry
        float b1X = dx + 85f, b1W = 260f;
        boolean b1Hover = (mouseX >= b1X && mouseX <= b1X + b1W && mouseY >= btnY && mouseY <= btnY + btnH);
        shapeRenderer.setColor(b1Hover ? new Color(0.45f, 0.15f, 0.15f, 0.90f) : new Color(0.20f, 0.06f, 0.08f, 0.85f));
        shapeRenderer.rect(b1X, btnY, b1W, btnH);

        // Button 2: Main Menu
        float b2X = dx + 455f, b2W = 260f;
        boolean b2Hover = (mouseX >= b2X && mouseX <= b2X + b2W && mouseY >= btnY && mouseY <= btnY + btnH);
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            if (b1Hover) exitAction = 3; // Retry
            else if (b2Hover) exitAction = 1; // Main Menu
        }
        shapeRenderer.setColor(b2Hover ? new Color(0.35f, 0.25f, 0.10f, 0.90f) : new Color(0.15f, 0.10f, 0.06f, 0.85f));
        shapeRenderer.rect(b2X, btnY, b2W, btnH);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(new Color(0.95f, 0.30f, 0.25f, 0.90f));
        shapeRenderer.rect(dx, dy, dw, dh);
        shapeRenderer.line(dx, dy + dh - 70f, dx + dw, dy + dh - 70f);
        shapeRenderer.rect(sx, sy, sw, sh);
        shapeRenderer.rect(b1X, btnY, b1W, btnH);
        shapeRenderer.rect(b2X, btnY, b2W, btnH);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.titleFont.setColor(new Color(1.0f, 0.35f, 0.30f, 1f));
        fonts.titleFont.draw(spriteBatch, "DISQUALIFIED -- CAMPUS PEACE VIOLATION", dx + 35f, dy + dh - 20f);

        fonts.headerFont.setColor(new Color(1.0f, 0.85f, 0.80f, 1f));
        fonts.headerFont.draw(spriteBatch, "CAMPUS PEACE RULE: UNPROVOKED ATTACK ON INNOCENT STUDENT", dx + 35f, dy + dh - 46f);

        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch,
            "You struck an innocent campus student! The Student Movement for meritocracy and justice is founded upon peaceful solidarity, disciplined restraint, and moral courage.",
            dx + 35f, dy + dh - 95f, dw - 70f, 10, true);

        fonts.bodyFont.setColor(new Color(0.90f, 0.85f, 0.85f, 1f));
        fonts.bodyFont.draw(spriteBatch,
            "Unprovoked violence against unarmed campus peers violates the core code of 36 July and results in immediate disqualification.",
            dx + 35f, dy + dh - 140f, dw - 70f, 10, true);

        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "RULE OF ENGAGEMENT", sx + 18f, sy + sh - 14f);

        fonts.smallFont.setColor(new Color(1.0f, 0.90f, 0.85f, 1f));
        fonts.smallFont.draw(spriteBatch,
            "Self-defense is permitted only against armed aggressors (e.g. Helmeted Attackers). Defend unarmed peers, never harm them.",
            sx + 18f, sy + 32f);

        fonts.headerFont.setColor(b1Hover ? Color.WHITE : new Color(1f, 0.80f, 0.80f, 1f));
        fonts.headerFont.draw(spriteBatch, "[R] RETRY MISSION", b1X + 35f, btnY + 30f);

        fonts.headerFont.setColor(b2Hover ? Color.WHITE : goldAccent);
        fonts.headerFont.draw(spriteBatch, "[ESC] MAIN MENU", b2X + 45f, btnY + 30f);
        spriteBatch.end();
    }

    // ==========================================
    // DEFEAT OVERLAY (Player Knocked Out)
    // ==========================================
    private void renderDefeatScreen(float w, float h) {
        float[] vMouse = GameInput.getVirtualMousePos(w, h);
        float mouseX = vMouse[0];
        float mouseY = vMouse[1];

        beginShapes(ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.02f, 0.02f, 0.80f);
        shapeRenderer.rect(0, 0, w, h);

        float dw = 800f;
        float dh = 440f;
        float dx = (w - dw) / 2f;
        float dy = (h - dh) / 2f;

        shapeRenderer.setColor(0.04f, 0.03f, 0.05f, 0.92f);
        shapeRenderer.rect(dx, dy, dw, dh);

        shapeRenderer.setColor(0.24f, 0.08f, 0.08f, 0.92f);
        shapeRenderer.rect(dx, dy + dh - 70f, dw, 70f);

        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(dx, dy + dh - 4f, dw, 4f);

        float sx = dx + 35f;
        float sy = dy + 110f;
        float sw = dw - 70f;
        float sh = 88f;
        shapeRenderer.setColor(0.08f, 0.08f, 0.10f, 0.85f);
        shapeRenderer.rect(sx, sy, sw, sh);

        float btnY = dy + 35f;
        float btnH = 46f;
        float b1X = dx + 85f, b1W = 260f;
        boolean b1Hover = (mouseX >= b1X && mouseX <= b1X + b1W && mouseY >= btnY && mouseY <= btnY + btnH);
        shapeRenderer.setColor(b1Hover ? new Color(0.35f, 0.15f, 0.10f, 0.90f) : new Color(0.18f, 0.08f, 0.06f, 0.85f));
        shapeRenderer.rect(b1X, btnY, b1W, btnH);

        float b2X = dx + 455f, b2W = 260f;
        boolean b2Hover = (mouseX >= b2X && mouseX <= b2X + b2W && mouseY >= btnY && mouseY <= btnY + btnH);
        shapeRenderer.setColor(b2Hover ? new Color(0.30f, 0.25f, 0.10f, 0.90f) : new Color(0.15f, 0.10f, 0.06f, 0.85f));
        shapeRenderer.rect(b2X, btnY, b2W, btnH);
        shapeRenderer.end();

        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(dx, dy, dw, dh);
        shapeRenderer.line(dx, dy + dh - 70f, dx + dw, dy + dh - 70f);
        shapeRenderer.rect(sx, sy, sw, sh);
        shapeRenderer.rect(b1X, btnY, b1W, btnH);
        shapeRenderer.rect(b2X, btnY, b2W, btnH);
        shapeRenderer.end();

        spriteBatch.begin();
        fonts.titleFont.setColor(new Color(1.0f, 0.45f, 0.35f, 1f));
        fonts.titleFont.draw(spriteBatch, "DEFEATED IN COMBAT -- STRUCK DOWN", dx + 35f, dy + dh - 20f);

        fonts.headerFont.setColor(new Color(0.95f, 0.90f, 0.80f, 1f));
        fonts.headerFont.draw(spriteBatch, "KNOCKED OUT BY HELMET ENFORCER'S LATHI", dx + 35f, dy + dh - 46f);

        fonts.bodyFont.setColor(Color.WHITE);
        fonts.bodyFont.draw(spriteBatch,
            "You were struck down by the Helmet Enforcer. But the courage of 36 July cannot be silenced. Stand back up and liberate the campus!",
            dx + 35f, dy + dh - 95f, dw - 70f, 10, true);

        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "MARTIAL ARTS COMBAT TIPS", sx + 18f, sy + sh - 14f);

        fonts.smallFont.setColor(new Color(0.95f, 0.95f, 0.95f, 1f));
        fonts.smallFont.draw(spriteBatch,
            "  - Crouch / Duck [C] to evade high horizontal lathi swings!\n  - Guard / Block [B] to deflect 80% heavy overhead strikes!\n  - Retaliate with Punches [Left-Click] and Martial Kicks [Q / K]!",
            sx + 18f, sy + 52f);

        fonts.headerFont.setColor(b1Hover ? Color.WHITE : goldAccent);
        fonts.headerFont.draw(spriteBatch, "[R] RETRY COMBAT", b1X + 35f, btnY + 30f);

        fonts.headerFont.setColor(b2Hover ? Color.WHITE : new Color(0.90f, 0.85f, 0.80f, 1f));
        fonts.headerFont.draw(spriteBatch, "[ESC] MAIN MENU", b2X + 45f, btnY + 30f);
        spriteBatch.end();
    }

    // ==========================================
    // GRAND LEVEL 1 VICTORY & CINEMATIC FINISH SCREEN
    // ==========================================
    private void renderVictoryScreen(float w, float h) {
        float[] vMouse = GameInput.getVirtualMousePos(w, h);
        float mouseX = vMouse[0];
        float mouseY = vMouse[1];

        // 1. Cinematic Background Artwork / Wallpaper (matching user reference image)
        com.badlogic.gdx.graphics.Texture bgTex = getCinematicFinishTexture();
        if (bgTex != null) {
            beginShapes(ShapeType.Filled);
            shapeRenderer.setColor(0.04f, 0.05f, 0.08f, 1f);
            shapeRenderer.rect(0, 0, w, h);
            shapeRenderer.end();

            spriteBatch.begin();
            spriteBatch.setColor(1f, 1f, 1f, 0.96f);
            spriteBatch.draw(bgTex, 0, 0, w, h);
            spriteBatch.end();

            beginShapes(ShapeType.Filled);
            shapeRenderer.setColor(0.02f, 0.03f, 0.06f, 0.22f);
            shapeRenderer.rect(0, 0, w, h);
        } else {
            beginShapes(ShapeType.Filled);
            shapeRenderer.setColor(0.02f, 0.04f, 0.07f, 0.75f);
            shapeRenderer.rect(0, 0, w, h);
        }

        float vw = 860f;
        float vh = 580f;
        float vx = (w - vw) / 2f;
        float vy = (h - vh) / 2f;

        // Elegant glassmorphic panel (translucent frosted dark slate)
        shapeRenderer.setColor(0.04f, 0.07f, 0.12f, 0.82f);
        shapeRenderer.rect(vx, vy, vw, vh);

        // Header band
        shapeRenderer.setColor(0.06f, 0.09f, 0.14f, 0.88f);
        shapeRenderer.rect(vx, vy + vh - 75f, vw, 75f);

        // Gold top accent
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.rect(vx, vy + vh - 4f, vw, 4f);

        // Narrative box
        float nx = vx + 35f;
        float ny = vy + 265f;
        float nw = vw - 70f;
        float nh = 120f;
        shapeRenderer.setColor(0.05f, 0.07f, 0.10f, 0.75f);
        shapeRenderer.rect(nx, ny, nw, nh);

        // Statistics sub-card
        float sx = vx + 35f;
        float sy = vy + 124f;
        float sw = vw - 70f;
        float sh = 125f;
        shapeRenderer.setColor(0.06f, 0.09f, 0.12f, 0.85f);
        shapeRenderer.rect(sx, sy, sw, sh);

        // Interactive Button Backgrounds (Hoverable)
        float btnY = vy + 38f;
        float btnH = 46f;
        // Button 1: Menu
        float b1X = vx + 45f, b1W = 230f;
        boolean b1Hover = (mouseX >= b1X && mouseX <= b1X + b1W && mouseY >= btnY && mouseY <= btnY + btnH);
        shapeRenderer.setColor(b1Hover ? new Color(0.35f, 0.28f, 0.12f, 0.95f) : new Color(0.12f, 0.14f, 0.18f, 0.85f));
        shapeRenderer.rect(b1X, btnY, b1W, btnH);

        // Button 2: Replay
        float b2X = vx + 315f, b2W = 230f;
        boolean b2Hover = (mouseX >= b2X && mouseX <= b2X + b2W && mouseY >= btnY && mouseY <= btnY + btnH);
        shapeRenderer.setColor(b2Hover ? new Color(0.20f, 0.35f, 0.25f, 0.95f) : new Color(0.12f, 0.14f, 0.18f, 0.85f));
        shapeRenderer.rect(b2X, btnY, b2W, btnH);

        // Button 3: Exit
        float b3X = vx + 585f, b3W = 230f;
        boolean b3Hover = (mouseX >= b3X && mouseX <= b3X + b3W && mouseY >= btnY && mouseY <= btnY + btnH);
        shapeRenderer.setColor(b3Hover ? new Color(0.38f, 0.15f, 0.15f, 0.95f) : new Color(0.12f, 0.14f, 0.18f, 0.85f));
        shapeRenderer.rect(b3X, btnY, b3W, btnH);
        shapeRenderer.end();

        // Lines and Borders
        beginShapes(ShapeType.Line);
        shapeRenderer.setColor(goldBorder);
        shapeRenderer.rect(vx, vy, vw, vh);
        shapeRenderer.line(vx, vy + vh - 75f, vx + vw, vy + vh - 75f);
        shapeRenderer.rect(nx, ny, nw, nh);
        shapeRenderer.rect(sx, sy, sw, sh);

        shapeRenderer.setColor(b1Hover ? goldAccent : goldBorder);
        shapeRenderer.rect(b1X, btnY, b1W, btnH);

        shapeRenderer.setColor(b2Hover ? Color.GREEN : goldBorder);
        shapeRenderer.rect(b2X, btnY, b2W, btnH);

        shapeRenderer.setColor(b3Hover ? Color.RED : goldBorder);
        shapeRenderer.rect(b3X, btnY, b3W, btnH);

        // Ornate Corner Brackets (⌜ ⌝ ⌞ ⌟)
        float cLen = 22f;
        shapeRenderer.setColor(goldAccent);
        shapeRenderer.line(vx - 4f, vy + vh + 4f, vx + cLen, vy + vh + 4f);
        shapeRenderer.line(vx - 4f, vy + vh + 4f, vx - 4f, vy + vh - cLen);
        shapeRenderer.line(vx + vw + 4f, vy + vh + 4f, vx + vw - cLen, vy + vh + 4f);
        shapeRenderer.line(vx + vw + 4f, vy + vh + 4f, vx + vw + 4f, vy + vh - cLen);
        shapeRenderer.line(vx - 4f, vy - 4f, vx + cLen, vy - 4f);
        shapeRenderer.line(vx - 4f, vy - 4f, vx - 4f, vy + cLen);
        shapeRenderer.line(vx + vw + 4f, vy - 4f, vx + vw - cLen, vy - 4f);
        shapeRenderer.line(vx + vw + 4f, vy - 4f, vx + vw + 4f, vy + cLen);
        shapeRenderer.end();

        // Typography
        spriteBatch.begin();
        int lvlNum = (activeLevel != null) ? activeLevel.getLevelNumber() : 1;
        String levelTitle = (activeLevel != null) ? activeLevel.getTitle() : "THE SPARK OF SOLIDARITY";

        fonts.titleFont.setColor(goldAccent);
        String victoryTitle = (lvlNum == 6) ?
            "CAMPAIGN VICTORY! 36 JULY ACHIEVED" :
            "CONGRATULATIONS! CHAPTER " + lvlNum + " VICTORY ACHIEVED";
        glyphLayout.setText(fonts.titleFont, victoryTitle);
        float titleX = vx + (vw - glyphLayout.width) / 2f;
        fonts.titleFont.draw(spriteBatch, victoryTitle, titleX, vy + vh - 20f);

        fonts.headerFont.setColor(new Color(0.95f, 0.90f, 0.80f, 1f));
        String subHeader = (lvlNum == 6) ?
            "BIRTH OF 36 JULY -- THE FALL & REBIRTH OF BANGLADESH" :
            "CHAPTER " + lvlNum + ": " + levelTitle.toUpperCase() + " -- BANGLADESH 2.0";
        glyphLayout.setText(fonts.headerFont, subHeader);
        float subHeaderX = vx + (vw - glyphLayout.width) / 2f;
        fonts.headerFont.draw(spriteBatch, subHeader, subHeaderX, vy + vh - 48f);

        // Historical Tribute Narrative
        fonts.bodyFont.setColor(Color.WHITE);
        String victoryNarrative;
        String narrativeSecondPara;
        switch (lvlNum) {
            case 2:
                victoryNarrative = "You stood firm with fellow students on July 15, documenting police intimidation, securing medical aid for the wounded, and maintaining peaceful resolve despite violent provocation.";
                narrativeSecondPara = "When armed enforcers attacked unarmed students, moral courage and united solidarity transformed a campus protest into an unstoppable national awakening.";
                break;
            case 3:
                victoryNarrative = "Across the streets of Rangpur and Dhaka, Abu Sayed stood with open arms for justice. You delivered critical eye wash and first aid under heavy tear gas, documenting historic unyielding courage.";
                narrativeSecondPara = "'Stand firm, do not leave the fallen.' The sacrifice of brave youths ignited sparks in millions of hearts across Bangladesh.";
                break;
            case 4:
                victoryNarrative = "In total internet blackout and strict curfew, you navigated searchlight patrols, secured clandestine medical supplies, and safely delivered historical eyewitness documentation to community couriers.";
                narrativeSecondPara = "When digital wires were severed, human couriers, medical volunteers, and unshakeable courage kept the truth alive across every neighborhood.";
                break;
            case 5:
                victoryNarrative = "Central Shaheed Minar roared with hundreds of thousands of voices. From students to rickshaw-pullers, the nation united behind the 1-Point Demand for systemic freedom and accountable governance.";
                narrativeSecondPara = "Fear had completely dissolved. The entire populace stood shoulder to shoulder as the historic countdown to 36 July began.";
                break;
            case 6:
                victoryNarrative = "Millions marched into Shahbagh and the National Parliament. The authoritarian regime collapsed. Bangladesh 2.0 was born through the sacrifice, grit, and unity of its youth and people.";
                narrativeSecondPara = "'This freedom belongs to the martyrs and every ordinary citizen who refused to bow.' The spark became an eternal beacon of democratic renewal.";
                break;
            case 1:
            default:
                victoryNarrative = bd.spark36.level.LevelConfig.isCombatEnabled() ?
                    "You have documented all 5 historical archives of the July 2024 Student Mass Uprising and successfully repelled the campus intimidation enforcer with bare-handed martial arts courage!" :
                    "You have documented all 5 historical archives of the July 2024 Student Movement across Dhaka University campus through peaceful student solidarity and moral courage!";
                narrativeSecondPara = "From the initial solidarity at Curzon Hall to the climax of 36 July (August 5), students and citizens stood united for meritocracy, equal dignity, and truth. The campus remains free and proud.";
                break;
        }

        fonts.bodyFont.draw(spriteBatch, victoryNarrative, nx + 18f, ny + nh - 18f, nw - 36f, 10, true);
        fonts.bodyFont.setColor(new Color(0.88f, 0.88f, 0.92f, 1f));
        fonts.bodyFont.draw(spriteBatch, narrativeSecondPara, nx + 18f, ny + nh - 65f, nw - 36f, 10, true);

        // Statistics Card
        fonts.headerFont.setColor(goldAccent);
        fonts.headerFont.draw(spriteBatch, "MISSION PERFORMANCE & STATISTICS", sx + 20f, sy + sh - 15f);

        fonts.smallFont.setColor(Color.WHITE);
        int ins = (activeLevel != null) ? activeLevel.getCompletedObjectiveCount() : 5;
        int tot = (activeLevel != null) ? activeLevel.getTotalObjectiveCount() : 5;
        int pct = (tot > 0) ? (int) Math.min(100f, ((float) ins / tot) * 100f) : 100;
        fonts.smallFont.draw(spriteBatch, "Historical Objectives Accomplished: " + ins + " / " + tot + " (" + pct + "% Complete)", sx + 20f, sy + 76f);

        String defenseStat = (activeLevel != null && activeLevel.isCombatEnabled()) ?
            "Campus Defense: Aggressors Neutralized with Disciplined Martial Arts" :
            "Campus Peace Rule: Upheld (Zero Violations, Non-Violent Solidarity)";
        fonts.smallFont.draw(spriteBatch, defenseStat, sx + 20f, sy + 52f);
        fonts.smallFont.draw(spriteBatch, "Civic Ethics: Upheld (0 Innocent Students Harmed)", sx + 20f, sy + 28f);

        fonts.headerFont.setColor(Color.GREEN);
        String rankStr = (lvlNum == 6) ? "RANK S+ -- LIBERATOR OF 36 JULY" : "RANK S -- HERO OF 36 JULY";
        fonts.headerFont.draw(spriteBatch, rankStr, sx + sw - 290f, sy + 58f);

        // Chapter unlock & auto-advance notice
        if (lvlNum < 6) {
            fonts.promptFont.setColor(new Color(0.35f, 1.0f, 0.45f, 1f));
            String unlockBanner = "*** CHAPTER " + (lvlNum + 1) + " UNLOCKED! ***";
            glyphLayout.setText(fonts.promptFont, unlockBanner);
            fonts.promptFont.draw(spriteBatch, unlockBanner, vx + (vw - glyphLayout.width) / 2f, vy + 114f);

            fonts.smallFont.setColor(new Color(1f, 0.88f, 0.50f, 0.95f));
            String advanceNotice = String.format("Auto-advancing in %.1fs... [SPACE / ENTER to skip]", Math.max(0f, victoryAutoAdvanceTimer));
            glyphLayout.setText(fonts.smallFont, advanceNotice);
            fonts.smallFont.draw(spriteBatch, advanceNotice, vx + (vw - glyphLayout.width) / 2f, vy + 97f);
        } else {
            fonts.promptFont.setColor(new Color(1f, 0.85f, 0.25f, 1f));
            String unlockBanner = "*** ALL 6 CHAPTERS COMPLETED! 36 JULY LIBERATION ***";
            glyphLayout.setText(fonts.promptFont, unlockBanner);
            fonts.promptFont.draw(spriteBatch, unlockBanner, vx + (vw - glyphLayout.width) / 2f, vy + 114f);

            fonts.smallFont.setColor(new Color(1f, 0.88f, 0.50f, 0.95f));
            String advanceNotice = String.format("Auto-returning to Menu in %.1fs... [SPACE / ENTER to skip]", Math.max(0f, victoryAutoAdvanceTimer));
            glyphLayout.setText(fonts.smallFont, advanceNotice);
            fonts.smallFont.draw(spriteBatch, advanceNotice, vx + (vw - glyphLayout.width) / 2f, vy + 97f);
        }

        // Interactive action prompts
        fonts.promptFont.setColor(b1Hover ? Color.WHITE : goldAccent);
        fonts.promptFont.draw(spriteBatch, "[M] Main Menu", b1X + 45f, btnY + 30f);

        fonts.promptFont.setColor(b2Hover ? Color.WHITE : new Color(1f, 0.85f, 0.45f, 1f));
        String b2Label = (lvlNum < 6) ? "[SPACE] Next Chapter (N)" : "[R] Replay Campaign";
        fonts.promptFont.draw(spriteBatch, b2Label, b2X + 15f, btnY + 30f);

        fonts.promptFont.setColor(b3Hover ? Color.WHITE : new Color(0.95f, 0.45f, 0.40f, 1f));
        fonts.promptFont.draw(spriteBatch, "[X] Exit Game", b3X + 50f, btnY + 30f);

        spriteBatch.end();
    }

    /** Trigger celebratory toast directly (used for automated visual verification) */
    public void triggerCongratulationsToastForTest() {
        congratulationsPopupActive = true;
        congratulationsPopupTimer = 3.0f;
    }

    /** Trigger victory screen directly (used for automated visual verification) */
    public void triggerVictoryForTest() {
        isVictoryOpen = true;
        victoryShown = true;
        victoryAutoAdvanceTimer = 5.0f;
        int currentLvl = (activeLevel != null) ? activeLevel.getLevelNumber() : 1;
        bd.spark36.level.LevelConfig.unlockNextLevel(currentLvl);
    }

    /** Open memorial modal directly (used for automated visual verification) */
    public void openMemorialModal(MemorialEntry entry) {
        this.activeModalEntry = entry;
    }

    /** Open pause menu directly (used for automated visual verification) */
    public void openPauseMenuForTest() {
        this.isPauseMenuOpen = true;
        this.isExitConfirmOpen = false;
        this.pauseMenuSelection = 0;
    }

    /** Open exit confirmation dialog directly (used for automated visual verification) */
    public void openExitConfirmForTest() {
        this.isPauseMenuOpen = true;
        this.isExitConfirmOpen = true;
        this.exitConfirmSelection = 0;
    }

    public boolean isPauseMenuOpen() {
        return isPauseMenuOpen;
    }

    public boolean isExitConfirmOpen() {
        return isExitConfirmOpen;
    }

    public int getPauseMenuSelection() {
        return pauseMenuSelection;
    }

    public int getExitConfirmSelection() {
        return exitConfirmSelection;
    }

    public boolean isHelpControlsOpen() {
        return isHelpControlsOpen;
    }

    public void setHelpControlsOpen(boolean open) {
        this.isHelpControlsOpen = open;
    }

    public void setPauseMenuSelection(int index) {
        this.pauseMenuSelection = Math.max(0, Math.min(6, index));
    }

    public void setExitConfirmSelection(int index) {
        this.exitConfirmSelection = Math.max(0, Math.min(1, index));
    }

    public boolean isModalOpen() {
        return activeModalEntry != null || isMapOpen || isPauseMenuOpen || isExitConfirmOpen || isVictoryOpen || isDisqualified || isDefeated || isHelpControlsOpen;
    }

    /** Reset HUD state (used when returning from main menu or replaying) */
    public void reset() {
        activeModalEntry = null;
        isMapOpen = false;
        isPauseMenuOpen = false;
        isHelpControlsOpen = false;
        isExitConfirmOpen = false;
        pauseMenuSelection = 0;
        exitConfirmSelection = 0;
        isVictoryOpen = false;
        isDisqualified = false;
        isDefeated = false;
        victoryShown = false;
        congratulationsPopupActive = false;
        congratulationsPopupTimer = 0f;
        victoryTransitionTimer = 0f;
        victoryAutoAdvanceTimer = 0f;
        missionBannerTime = 0f;
        exitAction = 0;
        clearNpcDialogue();
        dialogueManager.cancelDialogue();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        if (campusMapTex != null) campusMapTex.dispose();
        if (cinematicFinishTex != null) cinematicFinishTex.dispose();
    }

    private void drawParametersHiddenPrompt(float w, float h) {
        float bx = currentLayout.checkX;
        float by = currentLayout.checkY;
        fonts.keyFont.setColor(0f, 0f, 0f, 0.90f);
        fonts.keyFont.draw(spriteBatch, "[P] SHOW MISSION PARAMETERS", bx + 1f, by + 19f);
        fonts.keyFont.setColor(goldAccent);
        fonts.keyFont.draw(spriteBatch, "[P] SHOW MISSION PARAMETERS", bx, by + 20f);
    }

    /**
     * Diagnostic debug overlay (toggle via F3 or Grave `):
     * Draws safe margins in cyan, panel bounding boxes in green (or red on collision),
     * and performs real-time collision detection across all active HUD panels.
     */
    private void drawDebugOverlay(HUDLayout l, float w, float h) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        shapeRenderer.begin(ShapeType.Line);

        // 1. Safe Margin Boundary (Cyan)
        shapeRenderer.setColor(0f, 0.9f, 1f, 0.85f);
        shapeRenderer.rect(l.safeMarginX, l.safeMarginY, l.virtW - 2f * l.safeMarginX, l.virtH - 2f * l.safeMarginY);

        // 2. Active Panel Rectangles
        com.badlogic.gdx.math.Rectangle rCard = new com.badlogic.gdx.math.Rectangle(l.cardX, l.cardY, l.cardW, l.cardH);
        com.badlogic.gdx.math.Rectangle rCheck = parametersVisible ? new com.badlogic.gdx.math.Rectangle(l.checkX, l.checkY, l.checkW, l.checkH) : null;
        com.badlogic.gdx.math.Rectangle rCompass = new com.badlogic.gdx.math.Rectangle(l.pillX, l.compassCy - l.compassR - 10f, l.virtW - l.safeMarginX - l.pillX, 2f * l.compassR + 20f);
        com.badlogic.gdx.math.Rectangle rMapBtn = (!isMapOpen && !isPauseMenuOpen && activeModalEntry == null) ?
            new com.badlogic.gdx.math.Rectangle(l.mapBtnX, l.mapBtnY, l.mapBtnW, l.mapBtnH) : null;
        com.badlogic.gdx.math.Rectangle rKeycaps = new com.badlogic.gdx.math.Rectangle(l.keyBaseX - 10f, l.hintLineY - 8f, l.virtW - l.safeMarginX - (l.keyBaseX - 10f), l.keyClusterH + 40f);
        com.badlogic.gdx.math.Rectangle rBoss = (bd.spark36.level.LevelConfig.isCombatEnabled() && helmetGoon != null && helmetGoon.getState() != bd.spark36.character.HelmetGoon.State.STANDOFF && !helmetGoon.isKnockedOut()) ?
            new com.badlogic.gdx.math.Rectangle(l.bossX - 14f, l.bossY - 34f, l.bossW + 28f, l.bossH + 54f) : null;
        com.badlogic.gdx.math.Rectangle rDial = (bd.spark36.level.LevelConfig.isCombatEnabled() && helmetGoon != null && helmetGoon.isChallenging()) ?
            new com.badlogic.gdx.math.Rectangle(l.dialX, l.dialY, l.dialW, l.dialH) : null;

        com.badlogic.gdx.math.Rectangle[] panels = {rCard, rCheck, rCompass, rMapBtn, rKeycaps, rBoss, rDial};

        int overlaps = 0;
        for (int i = 0; i < panels.length; i++) {
            if (panels[i] == null) continue;
            boolean hasOverlap = false;
            for (int j = i + 1; j < panels.length; j++) {
                if (panels[j] == null) continue;
                if (panels[i].overlaps(panels[j])) {
                    hasOverlap = true;
                    overlaps++;
                }
            }
            shapeRenderer.setColor(hasOverlap ? Color.RED : Color.GREEN);
            shapeRenderer.rect(panels[i].x, panels[i].y, panels[i].width, panels[i].height);
        }
        shapeRenderer.end();

        // 3. Status readout
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        spriteBatch.begin();
        fonts.smallFont.setColor(overlaps == 0 ? Color.GREEN : Color.RED);
        String status = String.format("HUD DEBUG OVERLAY | Res: %dx%d (Virtual: %.0fx%.0f, Scale: %.2f) | SafeMargin: 3.5%% | Overlaps: %d (%s) | [F3] Toggle",
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), l.virtW, l.virtH, uiScale, overlaps, overlaps == 0 ? "PASSED" : "COLLISION DETECTED");
        fonts.smallFont.draw(spriteBatch, status, l.safeMarginX, l.virtH - 12f);
        spriteBatch.end();
    }

}
