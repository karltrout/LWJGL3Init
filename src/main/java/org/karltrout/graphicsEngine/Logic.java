package org.karltrout.graphicsEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.karltrout.graphicsEngine.Geodesy.GeoSpacialTerrainMesh;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.karltrout.graphicsEngine.models.*;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.textures.TextureData;
import org.lwjgl.opengl.GL11;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Blah blah blah
 * Created by karltrout on 7/27/17.
 */
public class Logic implements ILogic {

    private final Hud hud;
    private Logger logger = LogManager.getLogger(Logic.class);

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

    public Logic() throws Exception {
        camera = new Camera();
        hud = new Hud("Hud Window");
        renderer = new AppRenderer(camera, hud);
    }

    @Override
    public void init() throws Exception {

        renderer.init();

        //Add Terrain data to OPEN GL
        try {

            Mesh ellipsoid =  ReferenceEllipsoid.referenceElipsoidMesh().build();
            Entity planetEarth = new Entity(ellipsoid);
            planetEarth.setScale(scaleFactor);
            planetEarth.makeWireFrame(true);

            entities.add(planetEarth);

            Path pathToFltHdr = Paths.get("resources/models/terrainModels/n34w113.hdr");
            Path pathToFltFile = Paths.get("resources/models/terrainModels/n34w113.flt");
            FltFileReader fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);
            GeoSpacialTerrainMesh geoTerrainMesh = new GeoSpacialTerrainMesh(fltFileReader.hdr, fltFileReader.fltFile,"n34w113_ls8.png", 12);
            Mesh geoMesh = geoTerrainMesh.buildMesh();
            Entity terrainEntity = new Entity(geoMesh);
            terrainEntity.setMaxAltitude(15000);
            terrainEntity.setScale(scaleFactor);
            terrainEntity.makeWireFrame(false);
            entities.add(terrainEntity);
            fltFileReader = null;
            geoTerrainMesh = null;


            Path pathToFltHdr112 = Paths.get("resources/models/terrainModels/n34w112.hdr");
            Path pathToFltFile112 = Paths.get("resources/models/terrainModels/n34w112.flt");
            FltFileReader fltFileReader112 = FltFileReader.loadFltFile(pathToFltFile112, pathToFltHdr112);
            GeoSpacialTerrainMesh geoTerrainMesh112 = new GeoSpacialTerrainMesh(fltFileReader112.hdr, fltFileReader112.fltFile,"n34w112_ls8.png", 12);
            Mesh geoMesh112 = geoTerrainMesh112.buildMesh();
            Entity terrainEntity112 = new Entity(geoMesh112);
            terrainEntity112.setMaxAltitude(15000);
            terrainEntity112.setScale(scaleFactor );
            terrainEntity112.makeWireFrame(false);
            entities.add(terrainEntity112);
            fltFileReader112 = null;
            geoTerrainMesh112 = null;

            OBJLoader objLoader = new OBJLoader();

            Path bunnyTexture = Paths.get("resources/models/a380_AIRBUS.png");
            TextureData textureData = OpenGLLoader.decodeTextureFile(bunnyTexture);
            Mesh model = objLoader.loadObjModel("A380",textureData );
            Entity bunny = new Entity(model);
            bunny.makeWireFrame(false);
            bunny.setScale(.10f);
            /*
                Latitude: 	33-25.863480N
                Longitude: 	112-01.626082W
                Elevation: 	1110.1 ft.
                runway: 7L  33.431077, -112.026144
                terminal gate: 33.428817, -112.005486
             */
            float altitude = 3600.0f;
            Vector3f bunnySpot = ReferenceEllipsoid.cartesianCoordinates(33.431077, -112.026144, altitude).mul(scaleFactor);
            logger.info("Bunny Position: "+bunnySpot);

            bunny.setPosition(bunnySpot.x , bunnySpot.y, bunnySpot.z );
            bunny.moveRotation(54f,-18f,80f);
            entities.add(bunny);

            objLoader = new OBJLoader();
            Mesh terminal = objLoader.loadObjModel("kphx",textureData );
            terminal.setMaterial(new Material(new Vector4f(1f,.5f,1f,1f), 1.0f));
            Entity terminalEntity = new Entity(terminal);
            terminalEntity.makeWireFrame(false);
            terminalEntity.setScale(.1f);
            /*
                Latitude: 	33-25.863480N
                Longitude: 	112-01.626082W
                Elevation: 	1110.1 ft.
             */
            float terminalAltitude = 3400.0f;
            Vector3f terminalPosition = ReferenceEllipsoid.cartesianCoordinates(33.425817, -112.003486, terminalAltitude).mul(scaleFactor);
            logger.info("Terminal Position: "+terminalPosition);

            terminalEntity.setPosition(terminalPosition.x , terminalPosition.y, terminalPosition.z );
            terminalEntity.moveRotation(149f,-12f,18.5f);
            entities.add(terminalEntity);

            Path hiResTexture = Paths.get("resources/models/kphx_hiRes.png");
            TextureData hiResData = OpenGLLoader.decodeTextureFile(hiResTexture);
            objLoader = new OBJLoader();
            Mesh kphxHiRes = objLoader.loadObjModel("kphx_hiRes",hiResData );
            Entity kphxHiResEntity = new Entity(kphxHiRes);
            kphxHiResEntity.makeWireFrame(false);
            kphxHiResEntity.setScale(800f);
            /*
                Latitude: 	33-25.863480N
                Longitude: 	112-01.626082W
                Elevation: 	1110.1 ft.
             */
            Vector3f hiresPosition = ReferenceEllipsoid.cartesianCoordinates(33.426817, -112.006486, terminalAltitude+1100).mul(scaleFactor);
            logger.info("hiRes Position: "+terminalPosition);

            kphxHiResEntity.setPosition(hiresPosition.x , hiresPosition.y, hiresPosition.z );
            kphxHiResEntity.moveRotation(148.75f,-12f,18.75f);
            entities.add(kphxHiResEntity);

            movableEntity = kphxHiResEntity;

            //ShapeFileTerrainMesh sftm = new ShapeFileTerrainMesh();
            //sftm.addFltFiles(fltFileReader);
           // sftm.addFltFiles(fltFileReader112);
           // sftm.createHeightMap(scaleFactor);

            //Entity kphx = new Entity(sftm.buildMesh());
           // kphx.setScale(scaleFactor);
            //kphx.makeWireFrame(false);
            //entities.add(kphx);

            // Add 15 degree latitude and longitude  lines
            for (int i = 15; i < 180; i+=15) {
                int latitude = i - 90;
                entities.add(new Entity(makeLongitudeLineAt(latitude)));
            }
            for (int i = 0; i < 360; i+=15) {
                int longitude = i - 180;
                entities.add(new Entity(makeLatitudeLineAt(longitude)));
            }

            //set Camera initial start
            cameraLoc = new Vector3f(33.428817f, -112.026486f, altitude);
            //Vector3f cameraPos = ReferenceEllipsoid.cartesianCoordinates( cameraLoc.x, cameraLoc.y, cameraLoc.z).mul(scaleFactor);
            camera.setLocation(cameraLoc);
            Vector3f cameraPos = ReferenceEllipsoid.cartesianCoordinates(33.427817, -112.026486, altitude+500).mul(scaleFactor);

            camera.moveTo(cameraPos.x  ,cameraPos.y ,cameraPos.z );
            camera.moveRotation(  -90 + (-1 * cameraLoc.x),    -90 + (-1 * cameraLoc.y), 0 );

            logger.debug("camera Position: "+camera.getPosition());
            logger.debug("camera location: "+ camera.getLocation());

            ambientLight = new Vector3f(0.2f, 0.2f, 0.2f);
            Vector3f lightColour = new Vector3f(1, 1, 1);
            Vector3f lightPosition = new Vector3f(0, 0, 1);
            float lightIntensity = .5f;
            pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
            PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
            pointLight.setAttenuation(att);

            lightPosition = new Vector3f(-1, 0, 0);
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
    public void input(Window window, Mouse mouse) {

        float travel = (float) (zoomSpd /60 /60 /KILOMETERS_PER_LATITUDE_DEGREE);
        float localZoomSpd = zoomSpd;


        if( window.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {travel /= 10;
            localZoomSpd = zoomSpd / 10;}

        if(!window.isKeyPressed(GLFW_KEY_RIGHT_ALT)) {

            if (cameraLoc.x <= -90 || cameraLoc.x >= 90) {
                cameraLoc.x = cameraLoc.x < 0 ? -90 : 90;
                ind = ind * -1;
                cameraLoc.y = (cameraLoc.y < 0) ? 180 + cameraLoc.y : cameraLoc.y - 180;
            }

            if (window.isKeyPressed(GLFW_KEY_W)) {
                cameraInc.x -= travel;

                if (cameraLoc.x < 90 && ind > 0) {
                    cameraLoc.x += travel;
                } else {
                    cameraLoc.x -= travel;
                }
            }

            if (window.isKeyPressed(GLFW_KEY_S)) {
                cameraInc.x += travel;

                if (cameraLoc.x <= 90 && ind > 0) {
                    cameraLoc.x -= travel;
                } else {
                    if (cameraLoc.x < 90) cameraLoc.x += travel;
                }
            }

            if (window.isKeyPressed(GLFW_KEY_A)) {
                cameraInc.y -= travel;
                cameraLoc.y -= travel;
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                cameraInc.y += travel;
                cameraLoc.y += travel;
            }

            if (window.isKeyPressed(GLFW_KEY_Z)) {
                cameraInc.z -= localZoomSpd;
                cameraLoc.z -= localZoomSpd;
                if (cameraLoc.z < MIN_HEIGHT) cameraLoc.z = MIN_HEIGHT;
            } else if (window.isKeyPressed(GLFW_KEY_X)) {
                cameraInc.z += localZoomSpd;
                cameraLoc.z += localZoomSpd;
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
    public void update(float interval, Mouse mouse) {
        // Update camera position
        if (cameraInc.length() > 0) {
            Vector3f p = ReferenceEllipsoid.cartesianCoordinates(cameraLoc.x, cameraLoc.y,  cameraLoc.z);
            camera.moveToLocation(p.x * scaleFactor , p.y  * scaleFactor , p.z  * scaleFactor );
            camera.setLocation(cameraLoc);
            //camera.moveRotation(cameraInc.x * -1 , 0, cameraInc.y * -1);
            cameraInc.set(0, 0, 0);

         //   hud.updateWithPosition(interval, movableEntity.getRotation());
        }
        // Update camera based on mouse
        if (mouse.isRightButtonPressed()) {

            Vector2f rotVec = mouse.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY
                    , 0);

        }
        else if (mouse.isLeftButtonPressed()){
            Vector2f rotVec = mouse.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, 0, rotVec.y * MOUSE_SENSITIVITY);
        }

        hud.updateWithPosition(interval, movableEntity.getRotation());

        // Update directional light direction, intensity and colour
        directionalLight.getDirection().x =  -1;
        directionalLight.getDirection().y =  -1;

        /* Time Based Sunlight may not implement this, this way..
          lightAngle += 1.1f;
        if (lightAngle > 360 ) lightAngle = 0.0f;
        if (lightAngle > 90) {
        directionalLight.setIntensity(0);
        if (lightAngle >= 360) {
        lightAngle = -90;
        }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
        float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
        directionalLight.setIntensity(factor);
        directionalLight.getColor().y = Math.max(factor, 0.9f);
        directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {

        directionalLight.setIntensity(1);
        directionalLight.getColor().x = 1;
        directionalLight.getColor().y = 1;
        directionalLight.getColor().z = 1;

        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
*/
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

    private Primitive makeLongitudeLineAt(int latitude){

        float[] vertices = new float[360*3];
        float[] verticeColors = new float[360*4];

        int vertexPointer = 0;
        int vertexColor = 0;

        for (int i = 0; i < 360 ; i++) {

             Vector3f vertex =  ReferenceEllipsoid.cartesianCoordinates(latitude, i - 180, 250000.0d);

                vertices[vertexPointer++] = vertex.x * scaleFactor;
                vertices[vertexPointer++] = vertex.y * scaleFactor;
                vertices[vertexPointer++] = vertex.z * scaleFactor;
                if (vertex.x < 1 && vertex.x > -1 && vertex.y < 1 && vertex.y > -1)
                    logger.error("Point: "+i+" caused zero x");
                verticeColors[vertexColor++] = 1;
                verticeColors[vertexColor++] = 1;
                verticeColors[vertexColor++] = 1;
                verticeColors[vertexColor++] = .25f;

        }

        Material material = new Material();
       return new Primitive(vertices, verticeColors, GL_LINE_LOOP, 3, material);

    }

    private Primitive makeLatitudeLineAt(int longitude){

        float[] vertices = new float[181*3];
        float[] verticeColors = new float[181*4];

        int vertexPointer = 0;
        int vertexColor = 0;

        for (int i = -90; i <= 90 ; i++) {

            Vector3f vertex =  ReferenceEllipsoid.cartesianCoordinates(i , longitude, 250000.0d);

            vertices[vertexPointer++] = vertex.x * scaleFactor;
            vertices[vertexPointer++] = vertex.y * scaleFactor;
            vertices[vertexPointer++] = vertex.z * scaleFactor;

            verticeColors[vertexColor++] = 1;
            verticeColors[vertexColor++] = 1;
            verticeColors[vertexColor++] = 1;
            verticeColors[vertexColor++] = .25f;

        }

        Material material = new Material();
        return new Primitive(vertices, verticeColors, GL_LINE_STRIP, 3, material);

    }

}