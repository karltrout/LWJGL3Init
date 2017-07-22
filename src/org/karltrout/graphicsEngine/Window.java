package org.karltrout.graphicsEngine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

/**
 * Manages the window code.
 * Created by karltrout on 7/21/17.
 */
public class Window {

    private String title;

    private int height;
    private int width;
    public long id;
    private boolean resized = false;

    Window(String title, int width, int height){

        this.height = height;
        this.width = width;
        this.title = title;

        init();
    }

    private void init() {


        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        //size up the initial window
        id = glfwCreateWindow(width, height, title, 0, 0);

        if (id == 0) {

            throw new RuntimeException(" Could Not init GLFW window. exiting.");
        }

        //Error handling from the GLFW native calls *Error messaging from C code...
        GLFWErrorCallback.createPrint(System.err).set();

        glfwSetKeyCallback(id, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        // Setup resize callback
        glfwSetFramebufferSizeCallback(id, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        //Process the current primary monitor hardware size and position the widow in the middle
        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(id, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(id, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);

        }

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1);
        glfwShowWindow(id);

    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(id, keyCode) == GLFW_PRESS;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isResized() {
        return resized;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
