package org.karltrout.graphicsEngine.renderers;

import org.joml.Matrix4f;
import org.karltrout.graphicsEngine.Window;
import org.karltrout.graphicsEngine.shaders.DefaultShader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Application Renderer
 * Created by karltrout on 7/5/17.
 */
public class AppRenderer {

    private DefaultShader appShader;

    private Matrix4f projectionMatrix;

    private Window window;


    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 10000.0f;

    //SKY COLOR  = 83.1, 94.5, 97.3
    private static float RED   = 1.0f;
    private static float GREEN = 1.0f;
    private static float BLUE  = 1.0f;

    public AppRenderer(Window window) {

        this.window = window;
        createProjectionMatrix();

        try {

            appShader = new DefaultShader();

        }
        catch(Exception exception){

            exception.printStackTrace();

        }
    }

    public void render(int vaoId){

        prepare();
       if(window.isResized()) {
           glViewport(0, 0, window.getWidth(), window.getHeight());
           window.setResized(false);
       }

        appShader.start();
        // Bind to the VAO
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);

        // Draw the vertices
        glDrawArrays(GL_TRIANGLES, 0, 3);
        // Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        appShader.stop();


     /*   prepare();
        appShader.start();
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        //render things here :-)
        appShader.stop();
*/
    }

    private void prepare(){

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(RED,GREEN ,BLUE,1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    }


    private  void createProjectionMatrix(){

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window.id, w, h);
        int width = w.get(0);
        int height = h.get(0);

        float aspectRatio = (float) width / (float) height;
        float y_scale = (float) ( 1f / Math.tan(Math.toRadians(FOV/2f)) );
        float x_scale = y_scale/aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00( x_scale );
        projectionMatrix.m11( y_scale );
        projectionMatrix.m22( -((FAR_PLANE + NEAR_PLANE) / frustum_length) );
        projectionMatrix.m23( -1 );
        projectionMatrix.m32( -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length) );
        projectionMatrix.m33( 0 );

    }


    public void cleanUp(){

        appShader.cleanUp();

    }

}
