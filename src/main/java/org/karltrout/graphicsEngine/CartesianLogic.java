package org.karltrout.graphicsEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.karltrout.graphicsEngine.models.*;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.textures.TextureData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Blah blah blah
 * Created by karltrout on 10/12/17.
 */
public class CartesianLogic implements ILogic {

    private final Hud hud;
    private Logger logger = LogManager.getLogger(CartesianLogic.class);

    private final AppRenderer renderer;
    private final Vector3f cameraInc = new Vector3f();
    private Vector3f cameraLoc = new Vector3f();
    private final Camera camera;
    private ArrayList<Entity> entities = new ArrayList<>();
    private static final float MOUSE_SENSITIVITY = 0.2f;

    private Vector3f ambientLight;
    private PointLight pointLight;
    private DirectionalLight directionalLight;

    private static final float scaleFactor = .01f;
    private static final int MIN_HEIGHT = 1500;
    private int ind = 1;
    private static final double KILOMETERS_PER_LATITUDE_DEGREE = 110.5742727d;
    private float zoomSpd = 100;
    private float spdMultiplyer = 4.0f;

    private Entity movableEntity;
    private Mouse mouse;

    public CartesianLogic() throws Exception {
        camera = new Camera();
        hud = new Hud("Cartesian Logic Window");
        renderer = new AppRenderer(camera, hud);
    }

    @Override
    public void init(Mouse mouse) throws Exception {
        this.mouse = mouse;
        renderer.init(mouse);
        try {

            OBJLoader objLoader = new OBJLoader();

            Path bunnyTexture = Paths.get("resources/models/a380_AIRBUS.png");
            TextureData textureData = OpenGLLoader.decodeTextureFile(bunnyTexture);
            Mesh model = objLoader.loadObjModel("A380",textureData );
            Entity target = new Entity(model);
            target.makeWireFrame(false);

            target.setSelectable(true);

            Vector3f targetSpot = new Vector3f(0,0, 0);

            logger.info("Target Position: "+targetSpot);

            target.setPosition(targetSpot);
            target.moveRotation(-90f,0f,0f);
            entities.add(target);

            objLoader = new OBJLoader();
            Mesh terminal = objLoader.loadObjModel("kphx",textureData );
            terminal.setMaterial(new Material(new Vector4f(.5f,.5f,.5f,1f), 1.0f));
            Entity terminalEntity = new Entity(terminal);
            terminalEntity.makeWireFrame(false);

            Vector3f terminalPosition = new Vector3f(0,0, 0);

            logger.info("Terminal Position: "+terminalPosition);

            terminalEntity.setPosition(terminalPosition.x , terminalPosition.y, terminalPosition.z );
            terminalEntity.moveRotation(0f,0f,0f);
            entities.add(terminalEntity);

            Path hiResTexture = Paths.get("resources/models/kphx_hiRes.png");
            TextureData hiResData = OpenGLLoader.decodeTextureFile(hiResTexture);

            objLoader = new OBJLoader();
            Mesh kphxHiRes = objLoader.loadObjModel("kphx_hiRes",hiResData );
            Entity kphxHiResEntity = new Entity(kphxHiRes);
            kphxHiResEntity.makeWireFrame(false);
            kphxHiResEntity.setScale(8000f);
            Vector3f hiresPosition = new Vector3f(-200,92, -80);

            logger.info("hiRes Position: "+terminalPosition);

            kphxHiResEntity.setPosition(hiresPosition.x , hiresPosition.y, hiresPosition.z );
            kphxHiResEntity.moveRotation(0f,0f,0f);
            entities.add(kphxHiResEntity);

            movableEntity = target;

            //set Camera initial start
            cameraLoc = new Vector3f(0f, 0f, 100);
            camera.setLocation(cameraLoc);
            camera.moveTo(cameraLoc.x  ,cameraLoc.y ,cameraLoc.z );
            camera.moveRotation(  0, 0,0 );

            logger.debug("camera Position: "+camera.getPosition());
            logger.debug("camera location: "+ camera.getLocation());

            ambientLight = new Vector3f(0.2f, 0.2f, 0.2f);
            Vector3f lightColour = new Vector3f(1, 1, 1);
            Vector3f lightPosition = new Vector3f(0, 0, 1);
            float lightIntensity = .5f;
            pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
            PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
            pointLight.setAttenuation(att);

            lightPosition = new Vector3f(-1, 1, 0);
            lightColour = new Vector3f(1, 1, 1);
            directionalLight = new DirectionalLight(lightColour, lightPosition, lightIntensity);

            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;

        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void input(Window window) {

        float travel =  20;
        float localZoomSpd = 20;
        if( window.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {travel /= 10;
            localZoomSpd /= 10;}

        if(!window.isKeyPressed(GLFW_KEY_RIGHT_ALT)) {

            if (window.isKeyPressed(GLFW_KEY_W)) {
                cameraInc.z -= travel;
                cameraLoc.z -= travel;
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                cameraInc.z += travel;
                cameraLoc.z += travel;
            }

            if (window.isKeyPressed(GLFW_KEY_A)) {
                cameraInc.x -= travel;
                cameraLoc.x -= travel;
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                cameraInc.x += travel;
                cameraLoc.x += travel;
            }

            if (window.isKeyPressed(GLFW_KEY_Z)) {
                cameraInc.y -= localZoomSpd;
                cameraLoc.y -= localZoomSpd;
            } else if (window.isKeyPressed(GLFW_KEY_X)) {
                cameraInc.y += localZoomSpd;
                cameraLoc.y += localZoomSpd;
            }
            adjustCameraSpdBasedOnheight(cameraLoc.z);
        }
        else if(movableEntity != null)
        {
            travel = (float)Math.toRadians(1);  //temporary shit
            Vector3f rotation = movableEntity.getRotation();

            Matrix4f model = movableEntity.getModelMatrix();

            if(window.isKeyPressed(GLFW_KEY_RIGHT_ALT)){
                if (window.isKeyPressed(GLFW_KEY_W)) {
                    model.rotateX(-travel);
                    rotation.x -=  1;
                }
                 else if (window.isKeyPressed(GLFW_KEY_S)) {
                    model.rotateX(travel);
                    rotation.x += 1;
                }
                if (window.isKeyPressed(GLFW_KEY_D)) {
                    model.rotateY(-travel);
                    rotation.y -= 1;
                }
                else if (window.isKeyPressed(GLFW_KEY_A)) {
                   model.rotateY(travel);
                    rotation.y += 1;
                }
                if (window.isKeyPressed(GLFW_KEY_E)) {
                    model.rotateZ(-travel);
                    rotation.z -= 1;
                }
                else if (window.isKeyPressed(GLFW_KEY_Q)) {
                    model.rotateZ(travel);
                    rotation.z += 1;
                }
            }
        }
    }

    private void adjustCameraSpdBasedOnheight(float z) {

        if     ( z * scaleFactor > 15000) zoomSpd = 100000;
        else if( z * scaleFactor > 10000) zoomSpd = 10000;
        else if( z * scaleFactor > 6000 ) zoomSpd = 1000 * spdMultiplyer;
        else if( z * scaleFactor > 5000 ) zoomSpd = 500 * spdMultiplyer;
        else if( z * scaleFactor > 3000 ) zoomSpd = 400 * spdMultiplyer;
        else if( z * scaleFactor > 2000 ) zoomSpd = 300 * spdMultiplyer;
        else if( z * scaleFactor > 1000 ) zoomSpd = 200 * spdMultiplyer;
        else zoomSpd = 100 * spdMultiplyer ;

    }

    @Override
    public void update(float interval) {
        // Update camera position
        if (cameraInc.length() > 0) {
            camera.moveToLocation(new Vector3f(cameraLoc));
            cameraInc.set(0, 0, 0);
        }
        // Update camera based on mouse
        if (mouse.isRightButtonPressed()) {

            Vector2f rotVec = mouse.getDisplayVector();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY
                    , 0);

        }
        else if (mouse.isLeftButtonPressed()){
            Vector2f rotVec = mouse.getDisplayVector();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, 0, rotVec.y * MOUSE_SENSITIVITY);
        }

        hud.updateWithPosition(interval, movableEntity.getRotation());

    }

    @Override
    public void render(Window window) {

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

      renderer.render(entities.toArray(new Entity[entities.size()]), window, ambientLight, pointLight, directionalLight);

    }

    @Override
    public void cleanUp(){

        renderer.cleanUp();
        for (Entity entity : entities) {
            entity.getRenderable().cleanUp();
        }
    }

    @Override
    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

}