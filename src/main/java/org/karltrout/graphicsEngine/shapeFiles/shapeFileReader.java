package org.karltrout.graphicsEngine.shapeFiles;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
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

/**
 * blah
 * Created by karltrout on 9/4/17.
 */
public class shapeFileReader {

    File file = new File("example.shp");
    HashMap<String, Object> map = new HashMap<>();


    shapeFileReader(){

    }

    public static void main(String[] args) {

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
            BoundingBox aerodromeBounds = null;
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

    }

}
