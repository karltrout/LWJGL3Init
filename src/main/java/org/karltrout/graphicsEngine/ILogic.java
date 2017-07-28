package org.karltrout.graphicsEngine;

/**
 * Created by karltrout on 7/27/17.
 */
public interface ILogic {
    void init() throws Exception;
    void input(Window window);
    void update(float interval);
    void render(Window window);
}