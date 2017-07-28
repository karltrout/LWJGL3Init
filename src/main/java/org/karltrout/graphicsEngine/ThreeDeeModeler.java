package org.karltrout.graphicsEngine;

import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

/**
 * The Beginning of 3D Modeler.
 * Created by karltrout on 6/29/17.
 */
public class ThreeDeeModeler implements Runnable {

    private static Window window;
    private AppRenderer renderer;
    private Camera camera;

    ArrayList<Entity> entities = new ArrayList<>();

    //private final Thread loopThread;

    public ThreeDeeModeler() {
       // loopThread = new Thread(this, "3D_ENGINE_LOOP_THREAD");
        //window = new Window(windowTitle, width, height, vsSync);

        window = new Window("Three DEE Modeler", 640, 480, true);

       // this.gameLogic = gameLogic;
    }

    public void start() {
        //loopThread.run();
    }

    public void run() {

        try {

            init();
            loop();

            glfwFreeCallbacks(window.id);
            glfwDestroyWindow(window.id);

        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        finally {

            if(renderer != null) renderer.cleanUp();

            glfwTerminate();
            glfwSetErrorCallback(null).free();

        }

    }

    private void init() throws Exception {

        if (!glfwInit()) {
            throw new IllegalStateException(" Could Not init GLFW system. exiting.");
        }

        window = new Window("Three DEE Modeler", 640, 480, true);
        window.init();

        camera = new Camera();

        renderer = new AppRenderer(window, camera);

        float[] vertices = new float[]{
                0.0f, 0.5f, -2.0f,
                -0.5f, -0.5f, -2.0f,
                0.5f, -0.5f, -2.0f
        };

        float[] triColors = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
        };

        int[] triAngleIndices = new int[]{
                0, 1, 2,
        };

        Mesh triangle = new Mesh(vertices, triColors, triAngleIndices);

        Entity triangleEnt = new Entity(triangle);
        triangleEnt.setScale(.5f);
       // entities.add(triangleEnt);

        float[] positions = new float[]{
                -0.5f, 0.5f, -1.f,
                -0.5f, -0.5f, -1.f,
                0.5f, -0.5f, -1.f,
                0.5f, 0.5f, -1.f,
        };

        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2,
        };
        float[] colors = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        Mesh square = new Mesh(positions,colors, indices);
        //entities.add(new Entity(square));

        OBJLoader objLoader = new OBJLoader();
        Mesh bunny = objLoader.loadObjModel("bunny");
        Entity bunnyEntity = new Entity(bunny);
        bunnyEntity.setScale(1.5f);
        bunnyEntity.setPosition(0.05f,-0.15f,-0.25f);
        bunnyEntity.makeWireFrame(true);
        entities.add(bunnyEntity);

        Path pathToFltHdr = Paths.get("resources/models/terrainModels/floatn34w112_13.hdr");
        Path pathToFltFile = Paths.get("resources/models/terrainModels/floatn34w112_13.flt");

        try {
        FltFileReader fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);

            TerrainMesh mesh_12 = new TerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
            Mesh terrainMesh12 = mesh_12.buildMesh();
            Entity terrainEntity = new Entity(terrainMesh12);
            //terrainEntity.setScale(.5f);
            terrainEntity.setPosition(-10000, -3000, -15000);
            terrainEntity.makeWireFrame(true);
            entities.add(terrainEntity);
        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private void loop(){

        while (!glfwWindowShouldClose(window.id)){
            //renderer.render(triangle);
            renderer.render(entities.toArray(new Entity[entities.size()]));
            glfwSwapInterval(1);
            glfwSwapBuffers(window.id);
            glfwPollEvents();
        }
    }

    public static void main(String[] args){
        try {
            boolean vSync = true;
            ThreeDeeModeler engine = new ThreeDeeModeler();
           // engine.start();
            engine.run();


        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }

    }
}
