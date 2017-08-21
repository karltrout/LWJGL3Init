package org.karltrout.graphicsEngine.renderers;

import org.joml.Matrix4f;
import org.karltrout.graphicsEngine.Camera;
import org.karltrout.graphicsEngine.Window;
import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.shaders.DefaultShader;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPolygonMode;

/**
 * Application Renderer
 * Created by karltrout on 7/5/17.
 */
public class AppRenderer {

    private DefaultShader appShader;
    private Camera camera;

    private static final float FOV = (float) Math.toRadians(60);
    private static final float NEAR_PLANE = .5f;
    private static final float FAR_PLANE = 1200000.0f;

    //SKY COLOR  = 83.1, 94.5, 97.3
    private static float RED = 0.0f;
    private static float GREEN = 0.0f;
    private static float BLUE = 0.0f;

    private Transformation transformation;

    public AppRenderer( Camera camera ) throws Exception {
        this.camera = camera;
    }

    public void render(Entity[] entities, Window window) {

        prepare();

        appShader.start();
        // Update projection Matrix
        Matrix4f projectionMatrix =
                transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), NEAR_PLANE, FAR_PLANE);
        appShader.setUniform("projectionMatrix", projectionMatrix);
        appShader.setUniform("texture_sampler", 0);

        // Update view Matrix
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Render each gameItem
        for(Entity entity : entities) {

            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            //glDisable(GL_DEPTH_TEST);
            int polyMode = glGetInteger(GL_POLYGON_MODE);
            if (entity.isWireMesh()){
                glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
                //glEnable(GL_CULL_FACE);
                //glCullFace(GL_FRONT);
            }

            //Set the modelView Matrix for this entity
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(entity, viewMatrix);
            appShader.setUniform("modelViewMatrix", modelViewMatrix);

            // Render the mesh for this game item
            entity.getMesh().render();
            glPolygonMode(GL_FRONT_AND_BACK, polyMode);
        }

        appShader.stop();

    }

    private void prepare() {

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(RED, GREEN, BLUE, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    }

    public void cleanUp() {

        appShader.cleanUp();

    }

    public void init() throws Exception{

        appShader = new DefaultShader();

        transformation = new Transformation();

        //appShader.createUniform("worldMatrix");
        appShader.createUniform("projectionMatrix");
        appShader.createUniform("modelViewMatrix");
        appShader.createUniform("texture_sampler");

    }
}
