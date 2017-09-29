package org.karltrout.graphicsEngine.models;

/**
 * Created by karltrout on 9/24/17.
 */
public interface IHud {
    Entity[] getEntities();
    default void cleanup() {
        Entity[] entities = getEntities();
        for (Entity entity : entities) {
            entity.getRenderable().cleanUp();
        }
    }

    void init();
}
