package org.karltrout.graphicsEngine.models;

/**
 * Created by karltrout on 9/24/17.
 */
public class Hud implements IHud {

    Entity[] entities;
    String text;

    public Hud(String text){
        RenderedText hudText = new RenderedText(text, 36);
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
}
