package org.karltrout.graphicsEngine;

import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * The Beginning of 3D Modeler.
 * Created by karltrout on 6/29/17.
 */
public class ThreeDeeModeler implements Runnable {


    private static Window window;
    private static int vaoID;
    private AppRenderer renderer;

    public void run() {

        try {

            init();
            loop();

            glfwFreeCallbacks(window.id);
            glfwDestroyWindow(window.id);

        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        finally {

            if(renderer != null) renderer.cleanUp();

            glfwTerminate();
            glfwSetErrorCallback(null).free();

        }

    }

    private void init() {

        if (!glfwInit()) {
            throw new IllegalStateException(" Could Not init GLFW system. exiting.");
        }

        window = new Window("Three DEE Modeler", 640, 480);

        GL.createCapabilities();

        renderer = new AppRenderer(window);

        float[] vertices = new float[]{
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };

        // USE JOML Here...
        FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(vertices.length);
        floatBuffer.put(vertices).flip();

        /* Generate a Vertex Array Object VAO. this
            object will be what is current until unbound
        */
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);
        /*Generate a Vertex Buffer Object VBO. these are used for all kinds of things.
          We are gonna put our float buffer array in this one.
         */
        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, floatBuffer, GL_STATIC_DRAW);
        memFree(floatBuffer);

        // Define Vertex Buffer Data to the shaders as an attribute.
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        //unbind the VBo and the VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        memFree(floatBuffer);

    }

    private void loop(){

        while (!glfwWindowShouldClose(window.id)){
            renderer.render(vaoID);
            glfwSwapInterval(1);
            glfwSwapBuffers(window.id);
            glfwPollEvents();
        }
    }

    public static void main(String[] args){

        ThreeDeeModeler modeler = new ThreeDeeModeler();
        modeler.run();

    }
}
