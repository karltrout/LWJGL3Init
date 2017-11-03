package org.karltrout.graphicsEngine.Geodesy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.resources.geometry.XRectangle2D;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.OpenGLLoader;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFile;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;
import org.karltrout.graphicsEngine.textures.TextureData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * new awesome code.
 * Created by karl trout on 6/2/17.
 */
public class GeoSpacialTerrainMesh extends TerrainMesh {

    private static final float GEO_ARC_SECOND_METER_DISTANCE = 30.87f;
    private static final int GRID_SIZE = 301;

    private static final int MIN_RESOLUTION = 12 ;
    private FltFileReader.FltHeader hdr = null;
    private final int resolution;
    private OBJLoader objLoader;
    private final Logger logger = LogManager.getLogger(this.getClass());
    private float textureWidth = 1.0f;
    private float textureHeight = 1.0f;
    private FltFile fltFile;
    private int GRID_SIZE_COL = 301;
    private int GRID_SIZE_ROW = 301;
    private int startingRow = 0;
    private int startingColumn = 0;
    private boolean COMPLETE_COVERAGE = true;

    private int gridArraySize = 0;
    private FltFileReader topRightFltFile;
    private FltFileReader bottomLeftFltFile;
    private FltFileReader bottomRightFltFile;
    private FltFileReader topLeftFltFile = null;

    public void setzOffset(int zOffset) {
        this.zOffset = zOffset;
    }

    private int zOffset = 0;


    public GeoSpacialTerrainMesh(String fltFileName, int resolution) throws IOException {

        Path pathToFltHdr = Paths.get("resources/models/terrainModels/"+fltFileName+".hdr");
        Path pathToFltFile = Paths.get("resources/models/terrainModels/"+fltFileName+".flt");
        String terrainImg = "resources/models/terrainModels/"+fltFileName+".png";

        if(!(pathToFltFile.toFile().exists()||
                pathToFltHdr.toFile().exists()||
                Paths.get(terrainImg).toFile().exists())){
            throw new FileNotFoundException();
        }

        //TextureData textureData = OpenGLLoader.decodeTextureFile(terrainImg);
        FltFileReader fltFileReader = FltFileReader.loadFltFile(pathToFltFile, pathToFltHdr);
        hdr = fltFileReader.hdr;
        fltFile = fltFileReader.fltFile;
        this.resolution = resolution;

    }

    public GeoSpacialTerrainMesh(FltFileReader.FltHeader hdr, FltFile fltFile, String textureFile, int resolution){

        /* TODO this needs to be refactored into better init functions.
         * TODO function javadoc... what the hell */
        this.objLoader = new OBJLoader();
        this.hdr = hdr;
        this.resolution = (resolution > MIN_RESOLUTION) ? MIN_RESOLUTION : resolution;
        this.fltFile = fltFile;
        /* Texture information */
        /*TODO this needs to be set for each different terrain mesh*/
        String planetImg = "resources/models/terrainModels/"+textureFile;
        TextureData textureData = OpenGLLoader.decodeTextureFile(planetImg);

        /*TODO get this from the image or somewhere*/
        // this.textureWidth = (300f/385f);

        ArrayList<Vector2f> textureIndices = createTextureIndicesList();
        logger.debug("Number of Texture Points: "+textureIndices.size());
        objLoader.setTextureArray(textureIndices);
        objLoader.setTexture(textureData);

        /* Done with Texture */

        ArrayList<Vector3f> vertices = createVerticesList();
        logger.debug("Number of Mesh Points: "+ vertices.size());
        objLoader.setVertices(vertices);

        ArrayList<String[][]> faceList = createFacesList();
        logger.debug("Number of Faces: "+faceList.size());
        objLoader.setFaces(faceList);

        objLoader.calculateNormals();
    }


    public GeoSpacialTerrainMesh(FltFileReader[] fltFiles, XRectangle2D boundingBox, String textureFile, int zOffset)
    {
        this.zOffset = zOffset;
        this.objLoader = new OBJLoader();
        this.resolution = 1;
        this.gridArraySize = fltFiles.length;
        // 1, 2 or 4 files comprise all fltFiles surrounding an area.
        this.topLeftFltFile = fltFiles[0];
        if(gridArraySize > 1){
            this.topRightFltFile = fltFiles[1];
            if(fltFiles.length == 4){
                this.bottomLeftFltFile = fltFiles[2];
                this.bottomRightFltFile = fltFiles[3];
            }
        }

        setFltFileScope(boundingBox);

        double fltTopLeftLatitude = hdr.getLatitude() - (this.startingRow * hdr.cellsize);
        double fltTopLeftLongitude = hdr.getLongitude() + (this.startingColumn * hdr.cellsize);
        double fltBottomRightLatitude = fltTopLeftLatitude - (this.GRID_SIZE_ROW * hdr.cellsize);
        double fltBottomRightLongitude = fltTopLeftLongitude + (this.GRID_SIZE_COL * hdr.cellsize);

        logger.info("Texture size: Top Left ("+fltTopLeftLatitude+","+fltTopLeftLongitude+"), Bottom Right("+
                fltBottomRightLatitude+","+fltBottomRightLongitude+")");
        logger.info("Texture Diminsions: "+(fltTopLeftLatitude - fltBottomRightLatitude)+", "+(fltTopLeftLongitude- fltBottomRightLongitude));


        logger.info("Bounding Box of SHP file: "+boundingBox);
        logger.info(String.format("Flt HDR FILE Lat: %.2f, Long: %.2f ", hdr.getLatitude(), hdr.getLongitude()));
        logger.info(String.format("Flt HDR file Cell Size: %.10f", hdr.cellsize));
        logger.info("Grid Start Col: "+this.startingColumn);
        logger.info("Grid Start Row: "+this.startingRow);
        logger.info("Grid Number of Columns: "+this.GRID_SIZE_COL);
        logger.info("Grid Number of Rows: "+this.GRID_SIZE_ROW);

        if(!COMPLETE_COVERAGE){
            logger.warn("WARNING: THe FLT File does not FULLY cover the required Bounding Box!!");
        }

 /* Texture information */
        /*TODO this needs to be set for each different terrain mesh*/
        String planetImg = "resources/models/terrainModels/"+textureFile;
        TextureData textureData = OpenGLLoader.decodeTextureFile(planetImg);

        /*TODO get this from the image or somewhere*/
        // this.textureWidth = (300f/385f);

        ArrayList<Vector2f> textureIndices = createTextureIndicesList();
        logger.debug("Number of Texture Points: "+textureIndices.size());
        objLoader.setTextureArray(textureIndices);
        objLoader.setTexture(textureData);
        ArrayList<Vector3f> vertices = createVerticesList();
        logger.debug("Number of Mesh Points: "+ vertices.size());
        objLoader.setVertices(vertices);

        ArrayList<String[][]> faceList = createFacesList();
        logger.debug("Number of Faces: "+faceList.size());
        objLoader.setFaces(faceList);

        objLoader.calculateNormals();
    }


    private void setFltFileScope(XRectangle2D boundingBox) {

        this.hdr = topLeftFltFile.hdr; // all hdr files should match the first one
        if(gridArraySize == 1){
            this.fltFile = topLeftFltFile.fltFile;
        } else {
            if(gridArraySize == 2){
                this.fltFile = combineTwoFltFiles(topLeftFltFile, topRightFltFile);
            }
            //need to create a composite grid from all
            // this.fltFile = topRightFltFile.fltFile;
        }
        double minLongitude  = boundingBox.getMinX();
        double minLatitude   = boundingBox.getMinY();

        float fltLatitude    = hdr.getLatitude();
        float fltLongitude   = hdr.getLongitude();

        double bbWidth = boundingBox.getWidth() * 1.46 ;
        double bbHeight = boundingBox.getHeight() * 3.47;

        Number longitudeStart = 0 ;
        if (fltLongitude < 0 && minLongitude > fltLongitude ) {
            longitudeStart =
                    Math.floor((Math.abs(fltLongitude) - Math.abs(minLongitude)) / hdr.cellsize);
        } else {
            COMPLETE_COVERAGE = false;
        }
        Number longitudeEnd = 0;
        if (fltLongitude < 0 && minLongitude+bbWidth > fltLongitude ){
            longitudeEnd = Math.ceil(longitudeStart.doubleValue()  +(bbWidth/hdr.cellsize));

        }
        else {
            COMPLETE_COVERAGE = false;
        }

        Number latitudeStart = Math.ceil(Math.abs(Math.abs(fltLatitude)  - Math.abs(boundingBox.getMaxY()))/hdr.cellsize);
        Number latitudeEnd   = Math.floor(Math.abs(Math.abs(fltLatitude) - Math.abs(boundingBox.getMaxY()) - bbHeight)/hdr.cellsize);

        this.startingRow = latitudeStart.intValue();
        this.startingColumn = longitudeStart.intValue() -1;
        this.GRID_SIZE_COL = longitudeEnd.intValue() - longitudeStart.intValue() ;
        this.GRID_SIZE_ROW =  latitudeStart.intValue() - latitudeEnd.intValue() ;

    }

    private FltFile combineTwoFltFiles(FltFileReader topLeftFltFile, FltFileReader topRightFltFile) {
        int cols = topLeftFltFile.fltFile.data[0].length + topRightFltFile.fltFile.data[0].length;
        int rows = topLeftFltFile.fltFile.data.length;
        FltFile combined = new FltFile(rows, cols);
        FltFile topLeft = topLeftFltFile.fltFile;
        FltFile topRight = topRightFltFile.fltFile;
        for (int i = 0; i < rows ; i++) {
            for (int j = 0; j < cols; j++) {
                if (j > topLeft.data.length - 1 ){
                    combined.data[i][j] = topRight.data[i][j-topLeft.data.length];
                } else
                {
                    combined.data[i][j] = topLeft.data[i][j];
                }
            }
        }
        return combined;
    }


    private ArrayList<String[][]> createFacesList() {
        // Faces, The Original loader takes data from a text file...
        ArrayList<String[][]>faceList = new ArrayList<>();

        for (int x = 0; x < GRID_SIZE_ROW -1 ; x++) {
            for (int z = 1; z < GRID_SIZE_COL  ; z++) {

                int tl = x * GRID_SIZE_COL + z; // top-left
                int tr = x * GRID_SIZE_COL + z + 1; // top-right
                int bl = (x + 1) * GRID_SIZE_COL + z; // bottom-left
                int br = (x + 1) * GRID_SIZE_COL + z + 1; // bottom-right

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
        return faceList;
    }

    private ArrayList<Vector3f> createVerticesList() {

    /*
      At 49 degrees north latitude,
      along the northern boundary of the Concrete sheet,
      an arc-second of longitude equals 30.87 meters * 0.6561 (cos 49°)
      30.87 m = arc second at equator lat, file is at 1/3 arc second resolution ie 1/3 * 30.87
      30.87 m * cos lon degrees
    */

        int colLength = GRID_SIZE_COL;
        int rowLength = GRID_SIZE_ROW;

        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (int z = startingRow; z < startingRow+rowLength ; z++ ) {
            // read in the first x rows and first x cols(for now)
            // Points are fltFile height by hdr.cellsize width and length ( a square )
            int xResolution;
            int zResolution;
            Vector3f vector3f;
            double latitudeDegrees = ( z > 0 ) ? hdr.getLatitude() - z * (resolution * hdr.cellsize ): hdr.getLatitude();
            double longitudeDegrees;
            for ( int x = startingColumn ; x < startingColumn+colLength; x++ ){
                 longitudeDegrees = (x > 0) ? hdr.getLongitude() + x * (resolution * hdr.cellsize ) : hdr.getLongitude();
                 xResolution = x * resolution;
                 zResolution = z * resolution;
                 vector3f = ReferenceEllipsoid.cartesianCoordinates(latitudeDegrees,longitudeDegrees, (fltFile.data[zResolution][xResolution] + zOffset) * 12);

                 vertices.add(vector3f);
            }
        }
        return vertices;
    }

    private ArrayList<Vector2f> createTextureIndicesList() {
        ArrayList<Vector2f> textureIndices = new ArrayList<>();
        float texResolution = (resolution==1)?resolution  : resolution/MIN_RESOLUTION;
        for (float i = 0; i < GRID_SIZE_ROW; i++) {

            float y = (i == 0)? i : (i / GRID_SIZE_ROW ) * (float)(hdr.cellsize*hdr.ncols) * texResolution ;
            for (float j = 0; j <  GRID_SIZE_COL; j++) {
                float x = (j == 0) ? j : ( j / GRID_SIZE_COL) * (float)(hdr.cellsize*hdr.ncols)  * texResolution;
                textureIndices.add(new Vector2f(x, y));
            }
        }
        logger.info("texture indice size: "+textureIndices.size());
        return textureIndices;
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