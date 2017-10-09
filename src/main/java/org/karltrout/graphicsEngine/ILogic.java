package org.karltrout.graphicsEngine;

/**
 * Aesome Interfaces
 * Created by karltrout on 7/27/17.
 */
public interface ILogic {
    void init(Mouse mouse) throws Exception;
    void input(Window window);
    void update(float interval);
    void render(Window window);
    void cleanUp();

    void setMouse(Mouse mouse);
}
