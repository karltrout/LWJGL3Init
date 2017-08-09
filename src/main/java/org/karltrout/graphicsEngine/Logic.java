package org.karltrout.graphicsEngine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.Geodesy.GeoSpacialTerrainMesh;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;

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
    private static final float CAMERA_POS_STEP = 200.0f;
    private static final float scaleFactor = 1f;

    public Logic() throws Exception {
        camera = new Camera();
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

            /*TerrainMesh mesh_12 = new TerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
            Mesh terrainMesh12 = mesh_12.buildMesh();
            Entity terrainEntity12 = new Entity(terrainMesh12);
            terrainEntity12.setPosition(0, 0, 0);
            terrainEntity12.makeWireFrame(true);
            entities.add(terrainEntity12);
            */
            Mesh elipsoid =  ReferenceEllipsoid.referenceElipsoidMesh().build();

            Entity planetEarth = new Entity(elipsoid);
            planetEarth.setScale(scaleFactor);
            planetEarth.makeWireFrame(true);
            entities.add(planetEarth);

            GeoSpacialTerrainMesh geoTerrainMesh = new GeoSpacialTerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
            Mesh geoMesh = geoTerrainMesh.buildMesh();
            Entity terrainEntity = new Entity(geoMesh);
            terrainEntity.setScale(scaleFactor);
            terrainEntity.makeWireFrame(true);
            entities.add(terrainEntity);


            //Vector3f cameraPos = ReferenceEllipsoid.cartesianCoordinates( fltFileReader.hdr.getLatitude(), fltFileReader.hdr.getLongitude(), 2100.000f);
            Vector3f cameraPos = ReferenceEllipsoid.cartesianCoordinates( 34, 360 - 112, 1500.000f);

            camera.moveTo(cameraPos.x * scaleFactor ,cameraPos.y * scaleFactor ,cameraPos.z * scaleFactor);

            System.out.println("camera Position: "+cameraPos);
            System.out.println("camera location: "+ ReferenceEllipsoid.geocentricCoordinates(cameraPos.x, cameraPos.y, cameraPos.z));

            //camera.moveRotation(270-112f, 180+34f, 40f);
            camera.moveRotation( 34 - 90f, 0f, 112 - 270f);

            bunnyEntity.setTerrain(geoTerrainMesh);
            bunnyEntity.setPosition(0,-100,0);

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
        camera.moveTo(cameraInc.x * CAMERA_POS_STEP,
                cameraInc.y * CAMERA_POS_STEP ,
                cameraInc.z * CAMERA_POS_STEP);
        // Update camera based on mouse
        if (mouse.isRightButtonPressed()) {
            Vector2f rotVec = mouse.getDisplVec();
            System.out.println("X: "+rotVec.x+" Y: "+rotVec.y);

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
