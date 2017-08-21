package org.karltrout.graphicsEngine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.karltrout.graphicsEngine.Geodesy.GeoSpacialTerrainMesh;
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
    private Vector3f cameraLoc;
    private final Camera camera;
    private ArrayList<Entity> entities = new ArrayList<>();
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 200.0f;
    private static final float scaleFactor = .01f;
    private int MIN_HEIGHT = 1500;
    private int ind = 1;
    private double KILOMETERS_PER_LATITUDE_DEGREE =110.5742727d;
    private float SPEED_KPH = 100000 /60 /60f; // ~.27 km  per seconds...

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
       /* Mesh bunny = objLoader.loadObjModel("bunny");
        Entity bunnyEntity = new Entity(bunny);
        bunnyEntity.setScale(100.5f);
        bunnyEntity.setPosition(0.0f,00.00f,1.50f);
        bunnyEntity.makeWireFrame(true);
        entities.add(bunnyEntity);*/

        //Add Terrain data to OPEN GL
        Path pathToFltHdr = Paths.get("resources/models/terrainModels/floatn34w112_13.hdr");
        Path pathToFltFile = Paths.get("resources/models/terrainModels/floatn34w112_13.flt");

        try {
            FltFileReader fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);

            Mesh elipsoid =  ReferenceEllipsoid.referenceElipsoidMesh().build();

            Entity planetEarth = new Entity(elipsoid);
            planetEarth.setScale(scaleFactor);
            planetEarth.makeWireFrame(false);
            entities.add(planetEarth);

          /*  GeoSpacialTerrainMesh geoTerrainMesh = new GeoSpacialTerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
            Mesh geoMesh = geoTerrainMesh.buildMesh();
            Entity terrainEntity = new Entity(geoMesh);
            terrainEntity.setScale(scaleFactor);
            terrainEntity.makeWireFrame(true);
            //entities.add(terrainEntity);
*/

           // bunnyEntity.setTerrain(geoTerrainMesh);
          //  bunnyEntity.setPosition(0,0,0);

            cameraLoc = new Vector3f(fltFileReader.hdr.getLatitude(), fltFileReader.hdr.getLongitude(), 500000.000f);
            //cameraLoc = new Vector3f(0,0, 0.000f);
            Vector3f cameraPos = ReferenceEllipsoid.cartesianCoordinates( cameraLoc.x, cameraLoc.y, cameraLoc.z);
            camera.moveTo(cameraPos.x * scaleFactor ,cameraPos.y * scaleFactor ,cameraPos.z * scaleFactor);
            //camera.moveTo(0 ,0,0);

            camera.moveRotation(  15 + (-1 * cameraLoc.x), 0,   90 + (-1 * cameraLoc.y) );

            System.out.println("camera Position: "+cameraPos);
            System.out.println("camera location: "+ ReferenceEllipsoid.geocentricCoordinates(cameraPos.x, cameraPos.y, cameraPos.z));

        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void input(Window window, Mouse mouse) {

        float travel = (float) (SPEED_KPH /KILOMETERS_PER_LATITUDE_DEGREE);
        if(cameraLoc.x <= -90 || cameraLoc.x >= 90 ){
            cameraLoc.x =  cameraLoc.x < 0 ? -90 : 90;
            ind = ind * -1;
            cameraLoc.y = (cameraLoc.y < 0 ) ? 180 + cameraLoc.y: cameraLoc.y - 180;
        }

        if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.x -=  travel;

            if ( cameraLoc.x < 90 && ind > 0){
                cameraLoc.x +=  travel;
            }
            else{
                cameraLoc.x -= travel;
            }
        }

        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.x += travel;

            if ( cameraLoc.x <= 90 && ind > 0){
                cameraLoc.x -= travel;
            }
            else{
               if (cameraLoc.x < 90 ) cameraLoc.x += travel;
            }
        }

        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.y += travel;
            if (cameraLoc.y > 180 ){
                cameraLoc.y *= -1;
            }
            cameraLoc.y += travel;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.y -= travel;
            if (cameraLoc.y < -180 ){
                cameraLoc.y *= -1;
            }
            cameraLoc.y -= travel;
        }

        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.z -= 100000;
            cameraLoc.z -= 100000;
            if (cameraLoc.z < MIN_HEIGHT) cameraLoc.z = MIN_HEIGHT;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.z += 100000;
            cameraLoc.z += 100000;
        }
    }

    @Override
    public void update(float interval, Mouse mouse) {
        // Update camera position
        if (cameraInc.length() > 0) {

            Vector3f p = ReferenceEllipsoid.cartesianCoordinates(cameraLoc.x, cameraLoc.y,  cameraLoc.z);
            camera.moveToLocation(p.x * scaleFactor , p.y  * scaleFactor , p.z  * scaleFactor );
            camera.moveRotation(cameraInc.x  , 0, cameraInc.y * -1);

            cameraInc.set(0, 0, 0);
        }
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

        for (Entity entity :
                entities) {
            entity.getMesh().cleanUp();

        }
    }
}
