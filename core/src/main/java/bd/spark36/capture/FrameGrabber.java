// ei file ta opengl frame buffer theke pixel capture kore image banay
package bd.spark36.capture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.File;
import java.nio.ByteBuffer;

public final class FrameGrabber {

    private FrameGrabber() {}

    public static Pixmap grab() {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);

        for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 0xFF;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        ByteBuffer dst = pixmap.getPixels();
        BufferUtils.copy(pixels, 0, dst, pixels.length);
        return pixmap;
    }

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
