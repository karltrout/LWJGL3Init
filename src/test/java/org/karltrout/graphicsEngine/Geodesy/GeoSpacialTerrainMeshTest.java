package org.karltrout.graphicsEngine.Geodesy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by karltrout on 8/29/17.
 */
public class GeoSpacialTerrainMeshTest {

    FltFileReader fltFileReader;

    @Before public void before()

    {
        Path pathToFltHdr = Paths.get("resources/models/terrainModels/floatn34w112_13.hdr");
        Path pathToFltFile = Paths.get("resources/models/terrainModels/floatn34w112_13.flt");
        try {
             fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void buildMesh() throws Exception {

        Assert.assertNotNull(fltFileReader);
        GeoSpacialTerrainMesh geoTerrainMesh = new GeoSpacialTerrainMesh(fltFileReader.hdr, fltFileReader.fltFile, 12);
        //Mesh geoMesh = geoTerrainMesh.buildMesh();
        Assert.assertNotNull(geoTerrainMesh);
    }

}