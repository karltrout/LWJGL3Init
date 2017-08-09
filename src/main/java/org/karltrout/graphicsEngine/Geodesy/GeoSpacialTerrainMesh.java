package org.karltrout.graphicsEngine.Geodesy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFile;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;

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
        super(hdr, fltFile, resolution);

        this.objLoader = new OBJLoader();
        this.hdr = hdr;
        this.resolution = (resolution > MIN_RESOLUTION ) ? MIN_RESOLUTION: resolution;

        float[] root = {0,0};
       //            this.getTexCoords().addAll(root);

       /*
         At 49 degrees north latitude,
         along the northern boundary of the Concrete sheet,
         an arc-second of longitude equals 30.87 meters * 0.6561 (cos 49°)
         30.87 m = arc second at equator lat, file is at 1/3 arc second resolution ie 1/3 * 30.87
         30.87 m * cos lon degrees
       */

        //float latSize = getLatitudePointLength();
        //float lonSize = getLongitudePointLength();

      //  logger.debug("Size of tex Coords: "+ getTexCoords().size());

        int colLength = GRID_SIZE;
        int rowlength = GRID_SIZE;
        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (int z = 0; z < colLength ; z++ ) {
            // read in the first x rows and first x cols(for now)
            // Points are fltFile height by hdr.cellsize width and length ( a square )
            int xResolution = 0;
            int zResolution = 0;
            Vector3f vector3f = null;
            double latitudeDegrees = ( z > 0 ) ? (hdr.getLatitude() - (double)(z * resolution) / hdr.nrows ): hdr.getLatitude();

            double longitudeDegrees = 0;
            for ( int x = 0 ; x < rowlength; x++ ){
                 longitudeDegrees = (x > 0) ? hdr.getLongitude() + ((double)( x * resolution) / hdr.ncols ) : hdr.getLongitude();
                 xResolution = x * resolution;
                 zResolution = z * resolution;
                 vector3f = ReferenceEllipsoid.cartesianCoordinates(latitudeDegrees,longitudeDegrees, fltFile.data[zResolution][xResolution]);
                 vertices.add(vector3f);
            }
            //System.out.println("Z: "+zResolution+",X: "+xResolution+" La: "+ latitudeDegrees+"="+vector3f.x+" Lo: "+longitudeDegrees+"="+vector3f.y+" h: "+fltFile.data[zResolution][xResolution]+"="+vector3f.z);
            System.out.println(vector3f);
        }

        objLoader.setVertices(vertices);

        logger.debug("Number of mesh Points: "+ vertices.size());
        // faces
        //The Original loader takes data from a text file...
        ArrayList<String[][]>faceList = new ArrayList<>();

        for (int x = 0; x < rowlength -1 ; x++) {
            for (int z = 1; z < colLength  ; z++) {

                    int tl = x * rowlength + z; // top-left
                    int bl = x * rowlength + z + 1; // bottom-left
                    int tr = (x + 1) * rowlength + z; // top-right
                    int br = (x + 1) * rowlength + z + 1; // bottom-right

                    String[][] faceVector = new String[3][3];
                    faceVector[0] = String.valueOf(tl).split("/");
                    faceVector[1] = String.valueOf(bl).split("/");
                    faceVector[2] = String.valueOf(tr).split("/");
                    faceList.add(faceVector);

                    String[][] faceVector2 = new String[3][3];
                    faceVector2[0] = String.valueOf(tr).split("/");
                    faceVector2[1] = String.valueOf(bl).split("/");
                    faceVector2[2] = String.valueOf(br).split("/");
                    faceList.add(faceVector2);

            }
        }

        objLoader.setFaces(faceList);
        logger.debug("Number of Faces: "+faceList.size());
    }

    public GeoSpacialTerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile) {
        this(hdr, fltFile, MIN_RESOLUTION);
    }

    public Mesh buildMesh(){
        return objLoader.build();
    }

    private float getLatitudePointLength(Number latitude){
        Number length = getLatitudeLength(latitude);
        return  (length.floatValue() / hdr.nrows) * resolution;
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

    private double getLatitudeLength(Number latitude){
        double results =  (111132.954d - (559.822d * Math.cos( 2d * Math.toRadians(latitude.doubleValue()))) + (1.175d * Math.cos( 4d * Math.toRadians(latitude.doubleValue()) )));
        return results;
    }

}