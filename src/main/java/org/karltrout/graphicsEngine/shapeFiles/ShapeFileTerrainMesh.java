package org.karltrout.graphicsEngine.shapeFiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.resources.geometry.XRectangle2D;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.OpenGLLoader;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;
import org.karltrout.graphicsEngine.textures.TextureData;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Converts a ShapeFile to A 3D TerrainMesh...
 * Created by karltrout on 9/8/17.
 */
public class ShapeFileTerrainMesh extends TerrainMesh {

    private OBJLoader objLoader;
    private final Logger logger = LogManager.getLogger(this.getClass());
    private float textureWidth = 1.0f;
    private float textureHeight = 1.0f;
    private ArrayList<FltFileReader> fltFiles;
    private TextureData textureData;

    private XRectangle2D boundingBox;

    public ShapeFileTerrainMesh(){

        logger.debug("Started Shapefile TerrainMesh...");
        this.objLoader = new OBJLoader();

        //Texture
        String kphxLocalImg = "resources/models/terrainModels/LC08_L1TP_037037_20170901_20170901_01_RT.png";
        this.textureData = OpenGLLoader.decodeTextureFile(kphxLocalImg);

        fltFiles = new ArrayList<>();
        objLoader.setTexture(textureData);

        init();

        logger.debug("Completed ShapeFile TerrainMesh...");

    }

    private void init(){
        logger.debug("Started ShapeFile Init...");
        ArrayList<Vector3f> vertices    = createVerticesList();
        ArrayList<String[][]> faces     = createFacesList();
        ArrayList<Vector2f> textureInd  = createTextureIndicesList();

        objLoader.setTextureArray(textureInd);
        objLoader.setFaces(faces);
        objLoader.setVertices(vertices);
        objLoader.calculateNormals();

    }

    private ArrayList<Vector2f> createTextureIndicesList() {

        logger.debug("Started ShapeFile createTextureIndicesList...");
        ArrayList<Vector2f> textureIndices = new ArrayList<>();
        for (float i = 0; i < 2; i++) {

            float y = (i == 0)? i : (i / 1) * this.textureHeight;
            for (float j = 0; j <  2; j++) {
                float x = (j == 0) ? j : ( j / 1) * this.textureWidth;
                textureIndices.add(new Vector2f(x, y));
            }
        }
        return textureIndices;
    }

    private ArrayList<String[][]> createFacesList() {

        logger.debug("Started ShapeFile createFacesList...");
        int tl = 1; // top-left
        int tr = 2; // top-right
        int bl = 3; // bottom-left
        int br = 4; // bottom-right


        ArrayList<String[][]>faceList = new ArrayList<>();
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
        return faceList;
    }

    private ArrayList<Vector3f> createVerticesList() {

        logger.debug("Started ShapeFile createVerticesList...");
        /*
        CORNER_UL_LAT_PRODUCT = 34.21825
        CORNER_UL_LON_PRODUCT = -113.79947
        CORNER_UR_LAT_PRODUCT = 34.24991
        CORNER_UR_LON_PRODUCT = -111.27586
        CORNER_LL_LAT_PRODUCT = 32.08567
        CORNER_LL_LON_PRODUCT = -113.73248
        CORNER_LR_LAT_PRODUCT = 32.11487
        CORNER_LR_LON_PRODUCT = -111.26925
         */
        ArrayList<Vector3f> vertices = new ArrayList<>();
        Vector2f[] latlong = new Vector2f[4];
        latlong[0] = new Vector2f(34.21825f,-113.79947f );
        latlong[1] = new Vector2f(34.24991f,-111.27586f );
        latlong[2] = new Vector2f(32.08567f,-113.73248f );
        latlong[3] = new Vector2f(32.11487f,-111.26925f );

        calculateBoundingBox(latlong);

        for (Vector2f aLatlong : latlong) {

            float altitude = lookupAltitude(aLatlong);

            Vector3f vector3f = ReferenceEllipsoid.cartesianCoordinates(aLatlong.x, aLatlong.y, altitude );

            vertices.add(vector3f);
        }

        return vertices;
    }

    private void calculateBoundingBox(Vector2f[] latlong) {

        this.boundingBox = new XRectangle2D();

        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;

        for (Vector2f v : latlong) {
            maxX = (maxX > v.x )? maxX : v.x;
            maxY = (maxY > v.y)? maxY : v.y;
            minX = (minX < v.x)? minX : v.x;
            minY = (minY < v.y)? minY : v.y;
        }

        this.boundingBox = XRectangle2D.createFromExtremums( minX, minY, maxX, maxY);
    }

    private float lookupAltitude(Vector2f vector2f) {
        //TODO retrieve actual value from a flt height map....
        return ReferenceEllipsoid.distanceFromCenterAtLatitude(vector2f.x).floatValue();
    }

    public void addFltFiles(FltFileReader fltFileReader){
        this.fltFiles.add(fltFileReader);
    }

    public void createHeightMap(float scale){
        if(this.fltFiles.size() > 0){

            for (FltFileReader reader :
                    fltFiles) {
                if (this.boundingBox.contains(reader.hdr.getBoundingBox())) {
                    addAllHeightData(reader);
                }
                else if (reader.hdr.getBoundingBox().intersects(this.boundingBox))
                    addHeightData(reader);
                else {
                    logger.debug("There is no data from this fltFile that is usable.");
                }
            }


            double pixelHeight = (this.boundingBox.getMaxY() - this.boundingBox.getMinY()) / this.textureHeight;
            double pixelWidth  = (this.boundingBox.getMaxX() - this.boundingBox.getMinX()) / this.textureWidth;

            Vector3f[][] positionMap = new Vector3f[this.textureData.getHeight()][this.textureData.getWidth()];

            for (int y = 0; y < this.textureData.getHeight(); y++ ){

                float latitude = (float)(this.boundingBox.getMinY() + (y * pixelHeight));

                for (int x = 0; x < this.textureData.getWidth(); x++) {
                    float longitude = (float)(this.boundingBox.getMinX() + (x*pixelWidth));
                    float altitude = 0;
                    for (FltFileReader reader : fltFiles) {
                        if (this.boundingBox.contains(latitude, longitude)) {
                            altitude = reader.getHeightAt(latitude, longitude);
                            break;
                        }
                    }
                    Vector3f pixel = ReferenceEllipsoid.cartesianCoordinates(latitude, longitude, altitude * 10);
                    positionMap[y][x] = pixel.mul(scale);
                }

            }
            logger.debug("Size of Position Map : "+positionMap.length);
            //generateImage(positionMap);

        }
    }

    private void generateImage(Vector3f[][] positionMap){

        int w = this.textureData.getWidth();
        int h = this.textureData.getHeight();

        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream("resources/imageData.dat"));


        for (int y = 0; y < h; y++) {

            float[] data = new float[w * 4];
            int i = 0;
            for (int x = 0; x < w; x++) {
                data[i++] = positionMap[y][x].x;
                data[i++] = positionMap[y][x].y;
                data[i++] = positionMap[y][x].z;
                data[i++] = 1f;
            }
            byte buf[] = new byte[4*data.length];
            for (int ii=0; ii<data.length; ++ii)
            {
                int val = Float.floatToRawIntBits(data[ii]);
                buf[4 * ii] = (byte) (val >> 24);
                buf[4 * ii + 1] = (byte) (val >> 16) ;
                buf[4 * ii + 2] = (byte) (val >> 8);
                buf[4 * ii + 3] = (byte) (val);
            }

            out.write(buf);

        }
        out.flush();

        logger.debug("Finished Writting file: imageData.dat.");

        out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void addHeightData(FltFileReader reader) {

        logger.debug("THe FltFile contains Some of this bounding box.");

    }

    private void addAllHeightData(FltFileReader reader) {
        logger.debug("THe FltFile contains All this bounding box.");

    }

    public Mesh buildMesh(){
        return objLoader.build();
    }

}
