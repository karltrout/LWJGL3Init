package org.karltrout.graphicsEngine;

import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Blah blah blah
 * Created by karltrout on 7/27/17.
 */
public class Logic implements ILogic {

    private final AppRenderer renderer;
    private ArrayList<Entity> entities = new ArrayList<>();

    public Logic() throws Exception {
        Camera camera = new Camera();
        renderer = new AppRenderer(camera);
    }

    @Override
    public void init() throws Exception {

        renderer.init();

        //Add Our Entities to OpenGL Memory
        OBJLoader objLoader = new OBJLoader();
        Mesh bunny = objLoader.loadObjModel("bunny");
        Entity bunnyEntity = new Entity(bunny);
        bunnyEntity.setScale(1.5f);
        bunnyEntity.setPosition(0.05f,-0.15f,-0.25f);
        bunnyEntity.makeWireFrame(true);
        entities.add(bunnyEntity);

        //Add Terrain data to OPEN GL
        Path pathToFltHdr = Paths.get("resources/models/terrainModels/floatn34w112_13.hdr");
        Path pathToFltFile = Paths.get("resources/models/terrainModels/floatn34w112_13.flt");

        try {
            FltFileReader fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);

            TerrainMesh mesh_12 = new TerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
            Mesh terrainMesh12 = mesh_12.buildMesh();
            Entity terrainEntity = new Entity(terrainMesh12);
            terrainEntity.setPosition(-10000, -3000, -15000);
            terrainEntity.makeWireFrame(true);
            entities.add(terrainEntity);
        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void input(Window window) {

    }

    @Override
    public void update(float interval) {

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
