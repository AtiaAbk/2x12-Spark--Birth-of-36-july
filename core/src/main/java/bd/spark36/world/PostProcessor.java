package bd.spark36.world;

import bd.spark36.render.HdrPostProcessor;

/**
 * Backward-compatible wrapper delegating to {@link bd.spark36.render.HdrPostProcessor}.
 */
public class PostProcessor extends HdrPostProcessor {

    public PostProcessor(int screenW, int screenH) {
        super(screenW, screenH);
    }
}
