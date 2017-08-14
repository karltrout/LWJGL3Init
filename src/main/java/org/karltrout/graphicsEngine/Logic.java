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
    private Vector3f cameraLoc;
    private final Camera camera;
    private ArrayList<Entity> entities = new ArrayList<>();
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 200.0f;
    private static final float scaleFactor = .01f;
    private int MIN_HEIGHT = 1500;

    public Logic() throws Exception {
        camera = new Camera();
        renderer = new AppRenderer(camera);
        cameraInc = new Vector3f(0, 0, 0);
        cameraLoc = new Vector3f(0, 0, 0);;
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
            bunnyEntity.setTerrain(geoTerrainMesh);
            bunnyEntity.setPosition(0,-100,0);

            Vector3f cameraPos = ReferenceEllipsoid.cartesianCoordinates( fltFileReader.hdr.getLatitude(), fltFileReader.hdr.getLongitude(), 2100.000f);
            cameraLoc = new Vector3f(fltFileReader.hdr.getLatitude(), fltFileReader.hdr.getLongitude(), 2100.000f);

            camera.moveTo(cameraPos.x * scaleFactor ,cameraPos.y * scaleFactor ,cameraPos.z * scaleFactor);

            //camera.moveRotation(  34, 180, 90 + 112);

            System.out.println("camera Position: "+cameraPos);
            System.out.println("camera location: "+ ReferenceEllipsoid.geocentricCoordinates(cameraPos.x, cameraPos.y, cameraPos.z));


            /*float dirX = (float) Math.toDegrees(camera.getPosition().angle(new Vector3f( 1,0,0)));
            float dirY = (float) Math.toDegrees(camera.getPosition().angle(new Vector3f( 0,1,0)));
            float dirZ = (float) Math.toDegrees(camera.getPosition().angle(new Vector3f( 0,0,1)));
            System.out.println("DIR X: "+dirX+" DIR Y: "+dirY+" DIR Z: "+dirZ);
            */


        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void input(Window window, Mouse mouse) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.y = 1;
            if (cameraLoc.y > 180 ){
                cameraLoc.y = cameraLoc.y * -1;
            }
            cameraLoc.y += 1;// (30/3600);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.y = 1;
            if (cameraLoc.y < -180 ){
                cameraLoc.y = cameraLoc.y * -1;
            }
            cameraLoc.y -= 1; // (30/3600);
        }
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.x = -1;
            //longitude 0->-180
            if(cameraLoc.x < -90 ){
                cameraLoc.x = cameraLoc.x * -1;
                cameraLoc.y = cameraLoc.y * -1;
            }
            cameraLoc.x -= 1; // (30/3600);

        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.x = 1;
            if ( cameraLoc.x > 90){
                cameraLoc.x = cameraLoc.x * -1;
                cameraLoc.y = cameraLoc.y * -1;
            }
            cameraLoc.x += 1; //(30/3600);
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.z = -1;
            cameraLoc.z -= 100; //(30/3600);
            if (cameraLoc.z < MIN_HEIGHT) cameraLoc.z = MIN_HEIGHT;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.z = 1;
            cameraLoc.z += 100;//(30/3600);
        }
    }

    @Override
    public void update(float interval, Mouse mouse) {
        // Update camera position
        if (cameraInc.length() > 0) {


            System.out.println("location: lat:"+cameraLoc.x+" lon:"+cameraLoc.y+" alt:"+ cameraLoc.z);
            Vector3f p = ReferenceEllipsoid.cartesianCoordinates(cameraLoc.x, cameraLoc.y,  cameraLoc.z);
            camera.moveToLocation(p.x * scaleFactor , p.y * scaleFactor , p.z * scaleFactor );
            cameraInc.set(0, 0, 0);
        }
        /*camera.moveTo(cameraInc.x * CAMERA_POS_STEP,
                cameraInc.y * CAMERA_POS_STEP ,
                cameraInc.z * CAMERA_POS_STEP);
                */
        // Update camera based on mouse
        if (mouse.isRightButtonPressed()) {
            Vector2f rotVec = mouse.getDisplVec();
            //System.out.println("X: "+rotVec.x+" Y: "+rotVec.y);

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
