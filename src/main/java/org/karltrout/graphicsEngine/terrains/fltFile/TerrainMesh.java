package org.karltrout.graphicsEngine.terrains.fltFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.models.Mesh;

import java.util.ArrayList;

/**
 * new awesome code.
 * Created by karltrout on 6/2/17.
 */
public class TerrainMesh {


    //public static final int RESOLUTION_FACTOR = 1;
    public static final float GEO_ARC_SECOND_METER_DISTANCE = 30.87f;
    public static final int GRID_SIZE_METERS = 901;

    // Want to use this instead of hard coding the divisor
    // static final float GEO_ARC_SECOND_RESOLUTION = 1 / 3;
    private static final int MIN_RESOLUTION = 12 ;
    private final FltFileReader.FltHeader hdr;
    private int resolution = 12;

    private OBJLoader objLoader;

    private Logger logger = LogManager.getLogger(this.getClass());

    public TerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile, int resolution){

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

        float latSize = getLatitudePointLength();
        float lonSize = getLongitudePointLength();

      //  logger.debug("Size of tex Coords: "+ getTexCoords().size());

        int colLength = GRID_SIZE_METERS;
        int rowlength = GRID_SIZE_METERS;
        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (int x = 0; x < rowlength; x++ ) {
            // read in the first x rows and first x cols(for now)
            // Points are fltFile height by hdr.cellsize width and length ( a square )
            for ( int z = 0 ; z < colLength; z++ ){
                int xResolution = x * resolution;
                int zResolution = z * resolution;
                Vector3f vector3f = new Vector3f(z*latSize, fltFile.data[xResolution][zResolution], x*lonSize);
                vertices.add(vector3f);
            }
        }

        objLoader.setVertices(vertices);

        logger.debug("Number of mesh Points: "+ vertices.size());
        // faces
        //The Original loader takes data from a text file...
        ArrayList<String[][]>faceList = new ArrayList<>();

        for (int x = 0; x < rowlength - 1 ; x++) {
            for (int z = 1; z < colLength  ; z++) {
                int tl = x * rowlength + z; // top-left
                int bl = x * rowlength + z + 1; // bottom-left
                int tr = (x + 1) * rowlength + z; // top-right
                int br = (x + 1) * rowlength + z + 1; // bottom-right
                //getFaces().addAll(bl, 0, tl, 0, tr, 0); original I think javaFx odes counterclockwise triangles?
                //getFaces().addAll(tr, 0, br, 0, bl, 0);
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

    public TerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile) {
        this(hdr, fltFile, MIN_RESOLUTION);
    }

    public Mesh buildMesh(){
        return objLoader.build();
    }
    private float getLatitudePointLength(){
        return  (GEO_ARC_SECOND_METER_DISTANCE/3.0f) * resolution;
    }

   /* public float getLatitudeLength(){
        return getLatitudePointLength() * GRID_SIZE_METERS * (MIN_RESOLUTION/resolution);
    }
    */

    private float getLongitudePointLength(){
        Number lonMultiplyer = Math.cos(Math.toRadians(hdr.yllcorner));
        return ((GEO_ARC_SECOND_METER_DISTANCE * lonMultiplyer.floatValue()) / 3.0f) * resolution;
    }

    /*public float getLongitudeLength(){
        return getLongitudePointLength() * GRID_SIZE_METERS;
    }*/
}