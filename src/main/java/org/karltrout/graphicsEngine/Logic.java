package org.karltrout.graphicsEngine;

import org.karltrout.graphicsEngine.models.Entity;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.renderers.AppRenderer;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by karltrout on 7/27/17.
 */
public class Logic implements ILogic {

    private final AppRenderer renderer;
    private final Window window;
    private final Camera camera;


    ArrayList<Entity> entities = new ArrayList<>();


    public Logic(Window window) throws Exception {
        this.window = window;
        camera = new Camera();
        renderer = new AppRenderer(window, camera);
    }

    @Override
    public void init() throws Exception {

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

        Path pathToFltHdr = Paths.get("/Users/karltrout/Downloads/n34w112/floatn34w112_13.hdr");
        Path pathToFltFile = Paths.get("/Users/karltrout/Downloads/n34w112/floatn34w112_13.flt");

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

    @Override
    public void input(Window window) {

    }

    @Override
    public void update(float interval) {

    }

    @Override
    public void render(Window window) {

      renderer.render(entities.toArray(new Entity[entities.size()]));

    }
}
