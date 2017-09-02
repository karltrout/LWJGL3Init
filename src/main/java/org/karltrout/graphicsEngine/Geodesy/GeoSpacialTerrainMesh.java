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
 * Created by karl trout on 6/2/17.
 */
public class GeoSpacialTerrainMesh extends TerrainMesh {

    private static final float GEO_ARC_SECOND_METER_DISTANCE = 30.87f;
    private static final int GRID_SIZE = 901;

    private static final int MIN_RESOLUTION = 12 ;
    private final FltFileReader.FltHeader hdr;
    private OBJLoader objLoader;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public GeoSpacialTerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile, int resolution){

        /* TODO this needs to be refactored into better init functions.
         * TODO function javadoc... what the hell */
        this.objLoader = new OBJLoader();
        this.hdr = hdr;
        int resolution1 = (resolution > MIN_RESOLUTION) ? MIN_RESOLUTION : resolution;

        /* Texture information */
        /*TODO this needs to be set for each different terrain mesh*/
        String planetImg = "src/resources/34w112.png";
        TextureData textureData = OpenGLLoader.decodeTextureFile(planetImg);

        /*TODO get this from the image or somewhere*/
        float width = 300f/385f;

        ArrayList<Vector2f> textureIndices = new ArrayList<>();
        for (float i = 0; i < GRID_SIZE; i++) {

            float y = (i == 0)? i : (i / GRID_SIZE);
            for (float j = 0; j <  GRID_SIZE; j++) {
                float x = (j == 0) ? j : ( j / GRID_SIZE) * width;
                textureIndices.add(new Vector2f(x, y));
            }
        }
        logger.debug("Texture point array size: "+textureIndices.size());
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
            // TODO is this still right?
            // read in the first x rows and first x cols(for now)
            // Points are fltFile height by hdr.cellsize width and length ( a square )
            int xResolution;
            int zResolution;
            Vector3f vector3f;
            double latitudeDegrees = ( z > 0 ) ? (hdr.getLatitude() - (double)(z * resolution1) / hdr.nrows ): hdr.getLatitude();
            double longitudeDegrees;
            for ( int x = 0 ; x < rowLength; x++ ){
                 longitudeDegrees = (x > 0) ? hdr.getLongitude() + ((double)( x * resolution1) / hdr.ncols ) : hdr.getLongitude();
                 xResolution = x * resolution;
                 zResolution = z * resolution;
                 vector3f = ReferenceEllipsoid.cartesianCoordinates(latitudeDegrees,longitudeDegrees, fltFile.data[zResolution][xResolution] * 10);

                 vertices.add(vector3f);
            }
        }

        objLoader.setVertices(vertices);

        logger.debug("Number of mesh Points: "+ vertices.size());
        // Faces, The Original loader takes data from a text file...
        ArrayList<String[][]>faceList = new ArrayList<>();

        for (int x = 0; x < rowLength -1 ; x++) {
            for (int z = 1; z < colLength  ; z++) {

                int tl = x * rowLength + z; // top-left
                int tr = x * rowLength + z + 1; // top-right
                int bl = (x + 1) * rowLength + z; // bottom-left
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

        objLoader.calculateNormals();
    }

    public GeoSpacialTerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile) {
        this(hdr, fltFile, MIN_RESOLUTION);
    }

    public Mesh buildMesh(){
        return objLoader.build();
    }

    public Vector3f getWorldPosition(Vector2f latlong) {
        logger.debug("getting World position.");
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