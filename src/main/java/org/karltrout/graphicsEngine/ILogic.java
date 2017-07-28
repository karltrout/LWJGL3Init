package org.karltrout.graphicsEngine;

/**
 * Aesome Interfaces
 * Created by karltrout on 7/27/17.
 */
public interface ILogic {
    void init() throws Exception;
    void input(Window window, Mouse mouse);
    void update(float interval, Mouse mouse);
    void render(Window window);
    void cleanUp();
}
