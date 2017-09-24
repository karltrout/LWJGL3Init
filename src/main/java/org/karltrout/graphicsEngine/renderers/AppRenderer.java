package org.karltrout.graphicsEngine.renderers;

import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.karltrout.graphicsEngine.Camera;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.karltrout.graphicsEngine.Window;
import org.karltrout.graphicsEngine.models.DirectionalLight;
import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.models.PointLight;
import org.karltrout.graphicsEngine.shaders.DefaultShader;
import org.karltrout.graphicsEngine.shaders.HudShader;
import org.lwjgl.opengl.GL11;

import java.util.logging.LogManager;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPolygonMode;

/**
 * Application Renderer
 * Created by karltrout on 7/5/17.
 */
public class AppRenderer {

    Logger logger = org.apache.logging.log4j.LogManager.getLogger(AppRenderer.class);
    private DefaultShader appShader;
    private HudShader hudShader;
    private Camera camera;

    private static final float FOV = (float) Math.toRadians(60);
    private static final float NEAR_PLANE = .5f;
    private static final float FAR_PLANE = 1200000.0f;

    //SKY COLOR  = 83.1, 94.5, 97.3
    private static float RED = 0.0f;
    private static float GREEN = 0.0f;
    private static float BLUE = 0.0f;

    private Transformation transformation;

    private final float specularPower;

    public AppRenderer( Camera camera ) throws Exception {
        this.camera = camera;
        specularPower = 10f;
    }

    public void render(Entity[] entities, Window window, Vector3f ambientLight,
                       PointLight pointLight, DirectionalLight directionalLight ) {

        prepare();

        appShader.start();
        // Update projection Matrix
        Matrix4f projectionMatrix =
                transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), NEAR_PLANE, FAR_PLANE);
        appShader.setUniform("projectionMatrix", projectionMatrix);
        appShader.setUniform("texture_sampler", 0);

        // Update view Matrix
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);
        // Update Light Uniforms
        appShader.setUniform("ambientLight", ambientLight);
        appShader.setUniform("specularPower", specularPower);

        // Get a copy of the point light object and transform its position to view coordinates
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        //appShader.setUniform("pointLight", currPointLight);

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        appShader.setUniform("directionalLight", currDirLight);

        // Render each gameItem
        for(Entity entity : entities) {

            if ( camera.getLocation().z * entity.getScale()  > entity.getMaxAltitude()) {
                //logger.debug("camera Altitude: "+(camera.getLocation().z * entity.getScale() )+" entity Altitude: "+entity.getMaxAltitude());
                continue;
            }

            glEnable(GL_CULL_FACE);
            glCullFace(entity.getCullFace());
            glFrontFace(entity.getFrontFace());
            int polyMode = glGetInteger(GL_POLYGON_MODE);
            if (entity.isWireMesh()){
                glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
            }

            //Set the modelView Matrix for this entity
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(entity, viewMatrix);
            appShader.setUniform("modelViewMatrix", modelViewMatrix);
            // Render the mesh for this game item
            appShader.setUniform("material", entity.getMesh().getMaterial());
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
        //hudShader = new HudShader();

        transformation = new Transformation();

        //appShader.createUniform("worldMatrix");
        appShader.createUniform("projectionMatrix");
        appShader.createUniform("modelViewMatrix");
        appShader.createUniform("texture_sampler");
        //appShader.createUniform("hasTexture");
        // Create uniform for material
        appShader.createMaterialUniform("material");
        // Create lighting related uniforms
        appShader.createUniform("specularPower");
        appShader.createUniform("ambientLight");
        //appShader.createPointLightUniform("pointLight");
        //appShader.createSpotLightUniform("spotLight");
        appShader.createDirectionalLightUniform("directionalLight");


    }
}
