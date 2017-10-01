package org.karltrout.graphicsEngine;

import javafx.application.Application;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

/**
 * The Beginning of 3D Modeler.
 * Created by karltrout on 6/29/17.
 */
public class ThreeDeeModeler implements Runnable {

    private static Window window;
    private final ILogic envLogic;
    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;

    private final Thread loopThread;
    private Mouse mouse;

    private final Timer timer;


    public ThreeDeeModeler (String windowTitle, int width, int height, boolean vsSync, ILogic logic) {

        loopThread = new Thread(this, "3D_ENGINE_LOOP_THREAD");
        window = new Window(windowTitle, width, height, vsSync);
        mouse = new Mouse();
        this.envLogic = logic;

        timer = new Timer();
    }

    private void start() {
        String osName = System.getProperty("os.name");
        if ( osName.contains("Mac") ) {
            loopThread.run();
        } else {
            loopThread.start();
        }
    }

    public void run() {

        try {

            init();
            loop();

            glfwFreeCallbacks(window.getWindowHandle());
            glfwDestroyWindow(window.getWindowHandle());

        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        finally {

            envLogic.cleanUp();
            glfwTerminate();
            glfwSetErrorCallback(null).free();

        }

    }

    private void init() throws Exception {

        window.init();
        mouse.init(window);
        envLogic.init();

    }

    protected void input() {
        mouse.input(window);
        envLogic.input(window, mouse);
    }

    private void loop(){

        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        while (!glfwWindowShouldClose(window.getWindowHandle())){

            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            // Use elapse time here in input for timed events. multiply it by milliseconds against speed
            input();

          //  while (accumulator >= interval) {
                update(interval);
           //     accumulator -= interval;
           // }

            render();

            if ( !window.isvSync() ) {
                sync();
            }

        }

    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }


    protected void update(float interval) {
        envLogic.update(interval, mouse);
    }


    protected void render() {
        envLogic.render(window);
        window.update();
    }

    public static void main(String[] args){
        try {

            ILogic envLogic = new Logic();
            ThreeDeeModeler engine = new ThreeDeeModeler("3D Modeler", 640, 480, true, envLogic);
            engine.start();

        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }

    }
}
