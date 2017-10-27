package org.karltrout.graphicsEngine.shapeFiles;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.Before;
import org.junit.Test;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFile;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by karltrout on 9/8/17.
 * yeah Testing.
 */
public class ShapeFileTerrainMeshTest {
    BoundingBox aerodromeBounds = null;
    FltFile fltFile;
    FltFileReader.FltHeader hdr;

    @Before
    public void setUp() throws Exception {
        try {
            Path statesShape = Paths.get("resources/shapeFiles/KPHX/aerodrome.shp");
            File file = statesShape.toFile();
            Map<String, Object> map = new HashMap<>();
            map.put("url", file.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                    .getFeatureSource(typeName);
            Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    System.out.print(feature.getID());
                    System.out.print(": ");
                    System.out.println(feature.getDefaultGeometryProperty().getValue());
                    if(feature.getID().contains("aerodrome")){
                        aerodromeBounds = feature.getBounds();
                    }
                }

            }
            if(aerodromeBounds != null){
                System.out.println("Aerodrome Bounds :"+aerodromeBounds);
            }

        }catch(IOException e){
            e.printStackTrace();
        }


        Path pathToFltHdr112 = Paths.get("resources/models/terrainModels/n34w112.hdr");
        Path pathToFltFile112 = Paths.get("resources/models/terrainModels/n34w112.flt");
        FltFileReader fltFileReader112 = FltFileReader.loadFltFile(pathToFltFile112, pathToFltHdr112);
        fltFile = fltFileReader112.fltFile;
        hdr = fltFileReader112.hdr;




    }

    @Test
    public void ShapeFileTerrain() throws Exception {

        assertNotNull(aerodromeBounds);
        assertNotNull(hdr);
        assertNotNull(fltFile);

        TerrainMesh terrainMesh = new TerrainMesh(hdr, fltFile, aerodromeBounds);


    }
}