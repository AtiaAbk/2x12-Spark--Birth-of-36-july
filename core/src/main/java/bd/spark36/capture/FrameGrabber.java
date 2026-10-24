package bd.spark36.capture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Reads the current back buffer into a PNG. Call it at the point in the frame whose pixels you
 * want, e.g. after the 3D scene and before the HUD for a HUD-free capture.
 */
public final class FrameGrabber {

    private FrameGrabber() {}

    /** Snapshot of the back buffer as an opaque, top-row-first RGBA8888 pixmap. Caller disposes. */
    public static Pixmap grab() {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        // The default framebuffer's alpha is whatever blending left behind; a PNG must be opaque.
        for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 0xFF;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        ByteBuffer dst = pixmap.getPixels();
        BufferUtils.copy(pixels, 0, dst, pixels.length);
        return pixmap;
    }

    /**
     * PNG sink that reads the back buffer when the service writes. The service writes
     * synchronously, so the pixels are those of the moment the capture was requested; nothing is
     * read (or allocated) when the service refuses the capture, e.g. on low disk space.
     */
    public static ScreenshotService.PngSink currentFrameSink() {
        return new ScreenshotService.PngSink() {
            @Override
            public void write(File target) {
                Pixmap pixmap = grab();
                try {
                    PixmapIO.writePNG(new FileHandle(target), pixmap);
                } finally {
                    pixmap.dispose();
                }
            }
        };
    }
}
