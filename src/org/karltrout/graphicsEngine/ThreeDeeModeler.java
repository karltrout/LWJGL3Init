package org.karltrout.graphicsEngine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The Beginning of 3D Modeler.
 * Created by karltrout on 6/29/17.
 */
public class ThreeDeeModeler {


    private static long window;

    public ThreeDeeModeler() {
    }

    public static void main(String[] args){
        if(!glfwInit()){
            throw new IllegalStateException(" Could NOt init GLFW system. exiting.");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(680, 470, "Three DEE Modeler",0,0);

        if (window == 0){

            throw new RuntimeException(" Could Not init GLFW window. exiting.");
        }

        GLFWErrorCallback.createPrint(System.err).set();

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
               glfwSetWindowShouldClose(window, true);
           }
        });

        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height()-pHeight.get(0))/2);

        }


        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        while (!glfwWindowShouldClose(window)){

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glfwSwapBuffers(window);

            glfwPollEvents();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);


        glfwTerminate();

        glfwSetErrorCallback(null).free();
    }
}
