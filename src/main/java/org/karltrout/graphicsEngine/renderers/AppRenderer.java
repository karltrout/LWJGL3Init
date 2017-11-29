package org.karltrout.graphicsEngine.renderers;

import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.karltrout.graphicsEngine.Camera;
import org.karltrout.graphicsEngine.Mouse;
import org.karltrout.graphicsEngine.Timer;
import org.karltrout.graphicsEngine.Window;
import org.karltrout.graphicsEngine.models.*;
import org.karltrout.graphicsEngine.shaders.DefaultShader;
import org.karltrout.graphicsEngine.shaders.HudShader;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
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
    private Mouse mouse;

    private static final float FOV = (float) Math.toRadians(60);
    private static final float NEAR_PLANE = .5f;
    private static final float FAR_PLANE = 1200000.0f;

    //SKY COLOR  = 83.1, 94.5, 97.3
    private static float RED   = .831f; //.273f; //.831f;
    private static float GREEN = .945f; //.594f; //.945f;
    private static float BLUE  = .973f; //.887f; //.973f;

    private Transformation transformation;

    private IHud hud;
    private final float specularPower;
    private final Timer timer;

    public AppRenderer( Camera camera , IHud hud) throws Exception {
        this.camera = camera;
        specularPower = 10f;
        this.hud = hud;
        this.timer = new Timer();
    }

    public void render(Entity[] entities, Window window, Vector3f ambientLight,
                       PointLight pointLight, DirectionalLight directionalLight ) {

        renderScene(entities, window, ambientLight, pointLight, directionalLight);

        renderHud(window, hud);

    }

    private void renderHud(Window window, IHud hud) {
        hudShader.start();
        double frameRate = timer.getElapsedTime();
        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (Entity hudEntity : hud.getEntities()) {
            RenderedText text = (RenderedText) hudEntity.getRenderable();

            text.updateRate(frameRate);

            // Set orthographic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildOrtoProjectionModelMatrix(hudEntity, ortho);
            hudShader.setUniform("projModelMatrix", projModelMatrix);
            hudShader.setUniform("color", text.getMaterial().getAmbientColour());

            // Render the mesh for this HUD item
            text.render();
        }
        hudShader.stop();
    }

    private void renderScene(Entity[] entities, Window window, Vector3f ambientLight,
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


        Vector3f ray = camera.calculateRayPicker(window, mouse.getDisplayPosition(), projectionMatrix, viewMatrix);

        // Get a copy of the point light object and transform its position to view coordinates
        /*
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        */
        //appShader.setUniform("pointLight", currPointLight);

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        appShader.setUniform("directionalLight", currDirLight);

        Vector4f selectedColor = null;
        // Render each gameItem
        for(Entity entity : entities) {

            currDirLight.setColor(new Vector3f(1f,1f,1f));
            if(entity.isSelected() ||(entity.isSelectable() && entity.intersectedByRay(camera.getPosition(), ray))) {
                currDirLight.setColor(new Vector3f(.75f,0f,0f));
                if(window.isKeyPressed(GLFW_KEY_K)){
                    entity.setSelected(true);
                    Arrays.stream(entities).filter( entity1 -> !entity.equals( entity1 )).forEach(entity1 ->  entity1.setSelected(false));
                }
            }
            appShader.setUniform("skyColor", new Vector3f(RED,GREEN,BLUE));

            appShader.setUniform("directionalLight", currDirLight);

            //TODO this is wrong... need to calculate altitude for camera not z...
            if ( camera.getLocation().z * entity.getScale()  > entity.getMaxAltitude()) {
                continue;
            }

            glEnable(GL_CULL_FACE);
            glCullFace(entity.getCullFace());
            glFrontFace(entity.getFrontFace());
            int polyMode = glGetInteger(GL_POLYGON_MODE);
            if (entity.isWireMesh()){
                glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
            }

            //set the transformationMatrix for this entity
            Matrix4f transformationMatrix = transformation.getTransformationMatrix(entity);
            appShader.setUniform("transformationMatrix", transformationMatrix);

            //Set the modelView Matrix for this entity
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(entity, viewMatrix);
            appShader.setUniform("modelViewMatrix", modelViewMatrix);
            appShader.setUniform("viewMatrix", viewMatrix);
            // Render the mesh for this game item
            appShader.setUniform("material", entity.getRenderable().getMaterial());
            // Render the mesh for this game item
            entity.getRenderable().render();
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

    public void init(Mouse mouse) throws Exception{

        this.mouse = mouse;
        appShader = new DefaultShader();

        transformation = new Transformation();

        appShader.createUniform("projectionMatrix");
        appShader.createUniform("modelViewMatrix");
        appShader.createUniform("viewMatrix");
        appShader.createUniform("transformationMatrix");
        appShader.createUniform("texture_sampler");
        appShader.createMaterialUniform("material");
        // Create lighting related uniforms
        appShader.createUniform("specularPower");
        appShader.createUniform("ambientLight");
        //appShader.createPointLightUniform("pointLight");
        //appShader.createSpotLightUniform("spotLight");
        appShader.createDirectionalLightUniform("directionalLight");
        appShader.createUniform("skyColor");

        hudShader = new HudShader();

        //hudShader.start();
        hud.init();
    }

}
