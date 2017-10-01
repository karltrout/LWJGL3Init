package org.karltrout.graphicsEngine.models;

import org.joml.Vector3f;
import org.karltrout.graphicsEngine.Timer;

/**
 * Created by karltrout on 9/24/17.
 */
public class Hud implements IHud {

    private final RenderedText hudText;
    Entity[] entities;

    public Hud(String text){
        hudText = new RenderedText(text, 36);
        Entity entity = new Entity(hudText);
        entity.setPosition(10, 46, 0);
        entity.setScale(1f);
        this.entities = new Entity[]{entity};
    }

    @Override
    public Entity[] getEntities() {
        return entities;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void init() {
        for (Entity entity: getEntities()) {
            RenderedText renderedText = (RenderedText) entity.getRenderable();
            renderedText.init();
            renderedText.updateText("This is a Test");
        }
    }

    public void updateWithPosition(float interval, Vector3f position) {

        String positionStr = "X: "+position.x+"\n"
                +"Y: "+position.y+"\n"
                +"Z: "+position.z;

        hudText.updateText(positionStr);

    }
}
