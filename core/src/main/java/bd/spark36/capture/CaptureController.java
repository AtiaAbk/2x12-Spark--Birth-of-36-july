package bd.spark36.capture;

import bd.spark36.camera.CinematicCamera;
import bd.spark36.hud.FontRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

/**
 * Single entry point SparkGame talks to for screenshots, photo mode and the gallery, so the game
 * loop only needs a handful of one-line hooks:
 * <ul>
 *   <li>F12: HUD-free capture. The request is remembered and taken in {@link #beforeHud()} of the
 *       next frame, after the 3D scene and post-processing but before any 2D overlay.</li>
 *   <li>F10: photo mode (simulation frozen, HUD hidden, orbit camera). Esc or F10 leaves it.</li>
 *   <li>F11 (gameplay) or G / Tab (photo mode): gallery.</li>
 * </ul>
 * Results are reported with a toast drawn after the HUD, so a toast never ends up in a capture.
 */
public final class CaptureController {

    private final FontRenderer fonts;
    private final PhotoMode photo = new PhotoMode();
    private final GalleryScreen gallery;
    private final CaptureToast toast = new CaptureToast();
    private final ScrollInput scroll = new ScrollInput();
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private final GlyphLayout layout = new GlyphLayout();

    private boolean pendingUserCapture;
    private boolean galleryReturnsToPhoto;

    // Automated verification (-Dbd.spark36.testPhotoMode / testGallery)
    private final boolean testPhotoMode = System.getProperty("bd.spark36.testPhotoMode") != null;
    private final boolean testGallery = System.getProperty("bd.spark36.testGallery") != null;
    private float testTime;
    private int testStep;
    private CinematicCamera testCamera;
    private Vector3 testPlayerPos;

    public CaptureController(FontRenderer fonts) {
        this.fonts = fonts;
        this.gallery = new GalleryScreen(fonts);
    }

    // ── State queries used by SparkGame ──────────────────────────────

    public boolean isPhotoModeActive() { return photo.isActive(); }

    public boolean isGalleryOpen() { return gallery.isOpen(); }

    /** True while the HUD must not be drawn (photo mode). */
    public boolean hidesHud() { return photo.isActive(); }

    // ── Harness captures (deterministic names, may overwrite) ────────

    public static void captureHarness(String name) {
        ScreenshotService.Result r = ScreenshotService.get().saveHarnessCapture(name, FrameGrabber.currentFrameSink());
        if (r.ok) Gdx.app.log("SparkGame", "Screenshot saved: " + r.file.getAbsolutePath());
        else Gdx.app.error("SparkGame", r.message);
    }

    // ── Frame hooks ──────────────────────────────────────────────────

    /**
     * Normal gameplay keys, called after the frame is drawn.
     * @param photoFrame whether photo mode was active when this frame began
     * @return true when the rest of the gameplay key handling must be skipped this frame
     */
    public boolean handleGameplayKeys(float delta, CinematicCamera camera, Vector3 playerPos,
                                      boolean modalOpen, boolean photoFrame) {
        tickTest(delta, camera, playerPos);
        if (photoFrame || photo.isActive() || gallery.isOpen()) return true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) pendingUserCapture = true;
        if (modalOpen) return false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            enterPhotoMode(camera, playerPos);
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            openGallery(false);
            return true;
        }
        return false;
    }

    /** Replaces CinematicCamera.update while photo mode is active; handles all photo-mode keys. */
    public void updatePhotoCamera(float delta) {
        if (!photo.isActive()) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            exitPhotoMode();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) pendingUserCapture = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.G) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)
            || Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            openGallery(true);
            return;
        }
        photo.update(delta, scroll.consumeScrollY(), !testPhotoMode);
    }

    /** Call after the 3D scene, post-processing and particles, before the HUD. */
    public void beforeHud() {
        if (!pendingUserCapture) return;
        pendingUserCapture = false;
        ScreenshotService.Result r = ScreenshotService.get().saveUserCapture(FrameGrabber.currentFrameSink());
        toast.show(r.ok ? r.message + "  (" + r.file.getParent() + ")" : r.message, !r.ok);
    }

    /** Call after the HUD: photo-mode hint line and capture toast. */
    public void afterHud(float delta) {
        toast.update(delta);
        if (batch == null) {
            batch = new SpriteBatch();
            shapes = new ShapeRenderer();
        }
        int w = Gdx.graphics.getBackBufferWidth(), h = Gdx.graphics.getBackBufferHeight();
        float s = Math.max(0.6f, h / 900f);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        if (photo.isActive() && photo.isHintVisible()) drawPhotoHint(w, h, s);
        toast.render(batch, shapes, fonts.bodyFont, w, h, s);
    }

    private void drawPhotoHint(float w, float h, float s) {
        String hint = "PHOTO MODE   ·   mouse / WASD  orbit   ·   wheel or + / -  zoom   ·   Q / E  roll   ·   R  level"
            + "   ·   F12  capture   ·   G  gallery   ·   H  hide   ·   Esc / F10  exit";
        float oldX = fonts.smallFont.getData().scaleX, oldY = fonts.smallFont.getData().scaleY;
        fonts.smallFont.getData().setScale(oldX * s, oldY * s);
        layout.setText(fonts.smallFont, hint);
        float bh = layout.height + 20f * s;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.03f, 0.04f, 0.06f, 0.72f);
        shapes.rect(0, 0, w, bh);
        shapes.end();
        batch.begin();
        fonts.smallFont.draw(batch, layout, (w - layout.width) / 2f, 10f * s + layout.height);
        batch.end();
        fonts.smallFont.getData().setScale(oldX, oldY);
    }

    /** Draws the gallery instead of the game while it is open. */
    public void renderGallery(float delta) {
        boolean close = gallery.render(delta, scroll.consumeScrollY());
        toast.update(delta);
        if (gallery.isOpen()) tickTest(delta, testCamera, testPlayerPos);
        if (close) closeGallery();
    }

    // ── Transitions ──────────────────────────────────────────────────

    private void enterPhotoMode(CinematicCamera camera, Vector3 playerPos) {
        photo.enter(camera, playerPos);
        scroll.install();
    }

    private void exitPhotoMode() {
        photo.exit();
        scroll.uninstall();
    }

    private void openGallery(boolean fromPhoto) {
        galleryReturnsToPhoto = fromPhoto;
        scroll.install();
        gallery.open(ScreenshotService.get().getDirectory());
    }

    private void closeGallery() {
        gallery.close();
        if (galleryReturnsToPhoto && photo.isActive()) {
            Gdx.input.setCursorCatched(true);
            Gdx.input.getDeltaX();
            Gdx.input.getDeltaY();
        } else {
            scroll.uninstall(); // gameplay re-captures the cursor itself
        }
    }

    public void dispose() {
        if (photo.isActive()) photo.exit();
        scroll.uninstall();
        gallery.dispose();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        batch = null;
        shapes = null;
    }

    // ── Automated verification ───────────────────────────────────────

    /**
     * {@code -Dbd.spark36.testPhotoMode=true}: enter photo mode, orbit with a little roll, capture
     * the hint overlay (harness name {@code t7/photo_mode_overlay}), then a real F12-path capture
     * (unique name), then leave photo mode and log the follow camera's state before/after.
     * {@code -Dbd.spark36.testGallery=true} then opens the gallery and captures the grid and viewer
     * as {@code t7/gallery_grid} and {@code t7/gallery_viewer}. {@code autoExit} quits at the end.
     */
    private void tickTest(float delta, CinematicCamera camera, Vector3 playerPos) {
        if (!testPhotoMode && !testGallery) return;
        if (camera != null) { testCamera = camera; testPlayerPos = playerPos; }
        testTime += delta;
        if (testPhotoMode) {
            if (testStep == 0 && testTime >= 2.0f && testCamera != null) {
                Gdx.app.log("PhotoModeTest", "before: yaw=" + testCamera.getYaw() + " pitch=" + testCamera.getPitch()
                    + " pos=" + testCamera.getCamera().position);
                enterPhotoMode(testCamera, testPlayerPos);
                photo.setOrbit(testCamera.getYaw() + 145f, 16f, 5.2f, 6f);
                testStep = 1;
            } else if (testStep == 1 && testTime >= 2.6f) {
                captureHarness("t7/photo_mode_overlay");
                testStep = 2;
            } else if (testStep == 2 && testTime >= 2.8f) {
                pendingUserCapture = true; // same path as F12, taken before the HUD next frame
                testStep = 3;
            } else if (testStep == 3 && testTime >= 3.2f) {
                exitPhotoMode();
                Gdx.app.log("PhotoModeTest", "after:  yaw=" + testCamera.getYaw() + " pitch=" + testCamera.getPitch()
                    + " pos=" + testCamera.getCamera().position);
                testStep = 4;
            }
        } else if (testStep == 0) {
            testStep = 4;
        }
        if (testStep == 4 && testTime >= 3.5f) {
            if (testGallery) {
                openGallery(false);
                testStep = 5;
            } else {
                finishTest();
            }
        } else if (testStep == 5 && testTime >= 5.5f) {
            captureHarness("t7/gallery_grid");
            gallery.openViewer();
            testStep = 6;
        } else if (testStep == 6 && testTime >= 6.8f) {
            captureHarness("t7/gallery_viewer");
            closeGallery();
            finishTest();
        }
    }

    private void finishTest() {
        testStep = 99;
        Gdx.app.log("PhotoModeTest", "screenshot folder: " + ScreenshotService.get().getDirectory().getAbsolutePath()
            + " (" + GalleryIndex.list(ScreenshotService.get().getDirectory()).size() + " PNGs)");
        if ("true".equalsIgnoreCase(System.getProperty("bd.spark36.autoExit"))) Gdx.app.exit();
    }
}
