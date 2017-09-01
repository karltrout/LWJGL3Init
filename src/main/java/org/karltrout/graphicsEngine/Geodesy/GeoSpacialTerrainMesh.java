package org.karltrout.graphicsEngine.Geodesy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.OpenGLLoader;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFile;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;
import org.karltrout.graphicsEngine.textures.TextureData;

import java.util.ArrayList;

/**
 * new awesome code.
 * Created by karltrout on 6/2/17.
 */
public class GeoSpacialTerrainMesh extends TerrainMesh {


    //public static final int RESOLUTION_FACTOR = 1;
    public static final float GEO_ARC_SECOND_METER_DISTANCE = 30.87f;
    public static final int GRID_SIZE = 901;

    // Want to use this instead of hard coding the divisor
    // static final float GEO_ARC_SECOND_RESOLUTION = 1 / 3;
    private static final int MIN_RESOLUTION = 12 ;
    private final FltFileReader.FltHeader hdr;
    private int resolution = 1;

    private OBJLoader objLoader;

    private Logger logger = LogManager.getLogger(this.getClass());

    public GeoSpacialTerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile, int resolution){

        this.objLoader = new OBJLoader();
        this.hdr = hdr;
        this.resolution = (resolution > MIN_RESOLUTION ) ? MIN_RESOLUTION: resolution;

        /* Texture information */
        String planetImg = "src/resources/34w112_tan.png";
        TextureData textureData = OpenGLLoader.decodeTextureFile(planetImg);

        ArrayList<Vector2f> textureIndices = new ArrayList<>();
        for (int i = 0; i <= GRID_SIZE; i++) {

            float y = (i == 0)? i : ((i * resolution) / hdr.nrows);
            for (int j = 0; j <=  GRID_SIZE; j++) {
                float x = (j == 0) ? j : (j * resolution / hdr.ncols);
                textureIndices.add(new Vector2f(x, y));
            }
        }
        logger.debug(" 34w112 texture point array Size: "+textureIndices.size());
        objLoader.setTextureArray(textureIndices);
        objLoader.setTexture(textureData);
        /* Done with Texture */

       /*
         At 49 degrees north latitude,
         along the northern boundary of the Concrete sheet,
         an arc-second of longitude equals 30.87 meters * 0.6561 (cos 49°)
         30.87 m = arc second at equator lat, file is at 1/3 arc second resolution ie 1/3 * 30.87
         30.87 m * cos lon degrees
       */

        int colLength = GRID_SIZE;
        int rowLength = GRID_SIZE;
        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (int z = 0; z < colLength ; z++ ) {
            // read in the first x rows and first x cols(for now)
            // Points are fltFile height by hdr.cellsize width and length ( a square )
            int xResolution = 0;
            int zResolution = 0;
            Vector3f vector3f = null;
            double latitudeDegrees = ( z > 0 ) ? (hdr.getLatitude() - (double)(z * resolution) / hdr.nrows ): hdr.getLatitude();
            double longitudeDegrees = 0;
            for ( int x = 0 ; x < rowLength; x++ ){
                 longitudeDegrees = (x > 0) ? hdr.getLongitude() + ((double)( x * resolution) / hdr.ncols ) : hdr.getLongitude();
                 xResolution = x * resolution;
                 zResolution = z * resolution;
                 vector3f = ReferenceEllipsoid.cartesianCoordinates(latitudeDegrees,longitudeDegrees, fltFile.data[zResolution][xResolution] * 10);

                 vertices.add(vector3f);
            }
        }

        objLoader.setVertices(vertices);

        logger.debug("Number of mesh Points: "+ vertices.size());
        // faces
        //The Original loader takes data from a text file...
        ArrayList<String[][]>faceList = new ArrayList<>();

        for (int x = 0; x < rowLength -1 ; x++) {
            for (int z = 1; z < colLength  ; z++) {

                int tl = x * rowLength + z; // top-left
                int bl = x * rowLength + z + 1; // bottom-left
                int tr = (x + 1) * rowLength + z; // top-right
                int br = (x + 1) * rowLength + z + 1; // bottom-right

                String[][] faceVector = new String[3][3];
                faceVector[0][0] = String.valueOf(tl);
                faceVector[0][1] = String.valueOf(tl);
                faceVector[0][2] = String.valueOf(tl);

                faceVector[1][0] = String.valueOf(bl);
                faceVector[1][1] = String.valueOf(bl);
                faceVector[1][2] = String.valueOf(bl);

                faceVector[2][0] = String.valueOf(tr);
                faceVector[2][1] = String.valueOf(tr);
                faceVector[2][2] = String.valueOf(tr);

                faceList.add(faceVector);

                String[][] faceVector2 = new String[3][3];
                faceVector2[0][0] = String.valueOf(tr);
                faceVector2[0][1] = String.valueOf(tr);
                faceVector2[0][2] = String.valueOf(tr);

                faceVector2[1][0] = String.valueOf(bl);
                faceVector2[1][1] = String.valueOf(bl);
                faceVector2[1][2] = String.valueOf(bl);

                faceVector2[2][0] = String.valueOf(br);
                faceVector2[2][1] = String.valueOf(br);
                faceVector2[2][2] = String.valueOf(br);

                faceList.add(faceVector2);
            }
        }

        objLoader.setFaces(faceList);
        logger.debug("Number of Faces: "+faceList.size());
        logger.debug("Calculating Normals");
        objLoader.calculateNormals();
    }

    public GeoSpacialTerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile) {
        this(hdr, fltFile, MIN_RESOLUTION);
    }

    public Mesh buildMesh(){
        return objLoader.build();
    }


    public Vector3f getWorldPosition(Vector2f latlong) {
        Vector3f results = new Vector3f(0,0,0);
        float latDelta = hdr.getLatitude() - latlong.x;
        float longDelta = hdr.getLongitude() - latlong.y;
        results.set(longDelta * getLongitudeMetersPerDegree(latlong.x).floatValue(), 200, latDelta * getLatitudeMetersPerDegree(latlong.x).floatValue());
        return results;
    }

    private Number getLatitudeMetersPerDegree(Number latitude){
        return GEO_ARC_SECOND_METER_DISTANCE * 3600 * Math.cos(Math.toRadians(latitude.doubleValue()));
    }

    private Number getLongitudeMetersPerDegree(Number latitude){
        return getLongitudeLength(latitude);
    }

    private double getLongitudeLength(Number latitude) {
        Number lonMultiplier = Math.cos(Math.toRadians(latitude.doubleValue()));
        return ((GEO_ARC_SECOND_METER_DISTANCE  / 3.0f)* lonMultiplier.floatValue());
    }

}