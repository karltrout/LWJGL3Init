package org.karltrout.graphicsEngine;

import org.karltrout.graphicsEngine.renderers.AppRenderer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

/**
 * The Beginning of 3D Modeler.
 * Created by karltrout on 6/29/17.
 */
public class ThreeDeeModeler implements Runnable {

    private static Window window;
    private final ILogic envLogic;

    private final Thread loopThread;

    public ThreeDeeModeler(String windowTitle, int width, int height, boolean vsSync, ILogic logic) {

        loopThread = new Thread(this, "3D_ENGINE_LOOP_THREAD");
        window = new Window(windowTitle, width, height, vsSync);
        this.envLogic = logic;
    }

    public void start() {
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
        envLogic.init();

    }

    private void loop(){

        while (!glfwWindowShouldClose(window.getWindowHandle())){

            envLogic.render(window);
            glfwSwapInterval(1);
            glfwSwapBuffers(window.getWindowHandle());
            glfwPollEvents();

        }
    }

    public static void main(String[] args){
        try {
            boolean vSync = true;
            ILogic envLogic = new Logic();
            ThreeDeeModeler engine = new ThreeDeeModeler("3D Modeler", 640, 480, vSync, envLogic);
            engine.start();

        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }

    }
}
