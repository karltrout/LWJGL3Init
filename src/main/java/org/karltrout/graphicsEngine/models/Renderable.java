package org.karltrout.graphicsEngine.models;

/**
 * Created by karltrout on 8/21/17.
 */
public interface Renderable {
    void cleanUp();

    void render();

    boolean hasTexture();
}
