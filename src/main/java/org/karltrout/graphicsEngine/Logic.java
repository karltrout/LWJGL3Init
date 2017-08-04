package org.karltrout.graphicsEngine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Blah blah blah
 * Created by karltrout on 7/27/17.
 */
public class Logic implements ILogic {

    private final AppRenderer renderer;
    private final Vector3f cameraInc;
    private final Camera camera;
    private ArrayList<Entity> entities = new ArrayList<>();
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 100.0f;

    public Logic() throws Exception {
        camera = new Camera();
        camera.setPosition(0, 2000, 0);
        camera.setRotation(0f,-180, 0);
        renderer = new AppRenderer(camera);

        cameraInc = new Vector3f(0, 0, 0);
    }

    @Override
    public void init() throws Exception {

        renderer.init();

        //Add Our Entities to OpenGL Memory
        OBJLoader objLoader = new OBJLoader();
        Mesh bunny = objLoader.loadObjModel("bunny");
        Entity bunnyEntity = new Entity(bunny);
        bunnyEntity.setScale(1000.5f);
        bunnyEntity.setPosition(0.0f,2000.20f,-.50f);
        bunnyEntity.makeWireFrame(true);
        entities.add(bunnyEntity);

        //Add Terrain data to OPEN GL
        Path pathToFltHdr = Paths.get("resources/models/terrainModels/floatn34w112_13.hdr");
        Path pathToFltFile = Paths.get("resources/models/terrainModels/floatn34w112_13.flt");

        try {
            FltFileReader fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);

            TerrainMesh mesh_12 = new TerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
            Mesh terrainMesh12 = mesh_12.buildMesh();
            Entity terrainEntity12 = new Entity(terrainMesh12);
            terrainEntity12.setPosition(0, 0, 0);
            terrainEntity12.makeWireFrame(true);
            entities.add(terrainEntity12);

            Mesh elipsoid =  ReferenceEllipsoid.referenceElipsoidMesh().build();

            Entity planetEarth = new Entity(elipsoid);
            planetEarth.setScale(.1f);
            planetEarth.setRotation(-90,0,0);
            planetEarth.makeWireFrame(true);
            entities.add(planetEarth);
            /*
            TerrainMesh terrainMesh = new TerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 1);
            Mesh terrainMesh1 = terrainMesh.buildMesh();
            Entity terrainEntity = new Entity(terrainMesh1);
            terrainEntity.setPosition(0, 0, 0);
            terrainEntity.makeWireFrame(false);
            entities.add(terrainEntity);
            */

            camera.setLocation(new Vector2f(fltFileReader.hdr.getLatitude(), fltFileReader.hdr.getLongitude()), terrainEntity12.getPosition().y);
            //camera.movePosition(-54530.16f ,71099.5f ,-41461.668f);
            //camera.movePosition(0,0,-62626); //-62626.0f
            camera.moveRotation(90f, 0, 0);

            bunnyEntity.setTerrain(mesh_12);
            bunnyEntity.setLocation(new Vector2f (  33.437644f, -112.008944f));

        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void input(Window window, Mouse mouse) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
    }

    @Override
    public void update(float interval, Mouse mouse) {
        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP,
                cameraInc.y * CAMERA_POS_STEP * 5 ,
                cameraInc.z * CAMERA_POS_STEP);
        // Update camera based on mouse
        if (mouse.isRightButtonPressed()) {
            Vector2f rotVec = mouse.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY
                    , 0);
        }
    }

    @Override
    public void render(Window window) {

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

      renderer.render(entities.toArray(new Entity[entities.size()]), window);

    }
    @Override
    public void cleanUp(){
        renderer.cleanUp();
    }
}
