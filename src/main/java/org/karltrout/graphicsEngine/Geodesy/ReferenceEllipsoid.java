package org.karltrout.graphicsEngine.Geodesy;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.OpenGLLoader;
import org.karltrout.graphicsEngine.textures.TextureData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * Package to contain Reference Ellipsoid methods and Static calculations based on the Ellipsoid.
 * Created by karl trout on 8/1/17.
 */

public class ReferenceEllipsoid {

    private static final Number EQUATORIAL_RADIUS=63781370f; //meters
    private static final Number POLAR_RADIUS=63567523f; //meters

    private static final int numberOfLatitude = 178;
    private static final int numberOfLongitude = 361;

    private static OBJLoader objLoader;
    private Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Calculate the Distance for the Center of the Earths Reference Ellipsoid.
     * See: https://en.wikipedia.org/wiki/Earth_radius
     * @param latitude in decimal degrees of latitude
     * @return Number holding distance in Kilometers.
     */

    public static Number distanceFromCenterAtLatitude(double latitude){

        double radianLatitude = Math.toRadians(latitude);
        return Math.sqrt(
                (Math.pow(Math.pow(EQUATORIAL_RADIUS.doubleValue(), 2) * Math.cos(radianLatitude),2)
                        + Math.pow(Math.pow(POLAR_RADIUS.doubleValue(), 2) * Math.sin(radianLatitude),2))
                        /
                        (Math.pow(EQUATORIAL_RADIUS.doubleValue() * Math.cos(radianLatitude),2)
                        + Math.pow((POLAR_RADIUS.doubleValue() * Math.sin(radianLatitude)),2))
        ) / 1000;

    }

    public static Vector3f geocentricCoordinates(double x, double y , double z){

        //System.out.println("X: "+x+" Y: "+y+" Z: "+z);
        Vector3f location = new Vector3f();

        double radius = Math.sqrt((Math.pow(y,2) + Math.pow(z, 2)));

        double e2 = 0.00669437999014; //(eccentricitySquared)
        double θ = 90 - Math.toDegrees(Math.acos( z / POLAR_RADIUS.doubleValue() ));
        // β(Φ)=atan(sqrt (1 - e^2) * tan(Φ))
        double ϕ = Math.toDegrees( Math.atan(( Math.tan(Math.toRadians(θ)) / Math.sqrt(1 - e2) )) );

        double altitude = radiusOfCurvature(ϕ).doubleValue();


        double λ = Math.toDegrees(Math.atan2( y , x ));

        location.x = (float) ϕ; //latitude
        location.y = (float) λ; //longitude
        location.z = (float) z; //altitude; //height


       // System.out.println("ϕ : "+  ϕ + " λ: "+λ+ " R: "+radius+" H: "+z+" A: "+altitude );

        return location;
    }


    public static Vector3f cartesianCoordinates(double latitude, double longitude, double height){
        Vector3f coordinates = new Vector3f();

        double latitudeRadians = Math.toRadians(latitude);
        double longitudeRadians = Math.toRadians(longitude);

        Number normal = radiusOfCurvature(latitude);
        coordinates.x = (float) ((normal.doubleValue() + height) * Math.cos(latitudeRadians) * Math.cos(longitudeRadians));
        coordinates.y = (float) ((normal.doubleValue() + height) * Math.cos(latitudeRadians) * Math.sin(longitudeRadians));
        coordinates.z = (float) (((Math.pow(POLAR_RADIUS.doubleValue(), 2) / Math.pow(EQUATORIAL_RADIUS.doubleValue(), 2))
                                * normal.doubleValue() + height)
                        * Math.sin(latitudeRadians));

        return coordinates;
    }
    /**
     *
     * See: https://en.wikipedia.org/wiki/Reference_ellipsoid
     *
     * @param latitude
     * @return
     */
    private static Number radiusOfCurvature(double latitude) {

        double radianLatitude = Math.toRadians(latitude);
        return Math.pow(EQUATORIAL_RADIUS.doubleValue(), 2)
                / Math.sqrt(
                        Math.pow(EQUATORIAL_RADIUS.doubleValue() * Math.cos(radianLatitude),2)
                                + Math.pow(POLAR_RADIUS.doubleValue() * Math.sin(radianLatitude),2)
                );

    }

    public static Vector3f[][] pointCloud(){

        Vector3f[][] latitudeLongitudeCloud = new Vector3f[numberOfLatitude][numberOfLongitude];


        for (int la = 0; la < numberOfLatitude ; la++) { //la = latitude
            for (int lo = 0; lo < numberOfLongitude; lo++) { //lo = longitude
                try {

                        latitudeLongitudeCloud[la][lo] =
                                cartesianCoordinates( (la + 1) - 90, lo - 180 , 1);

                    } catch (ArrayIndexOutOfBoundsException a){
                    a.printStackTrace();
                }
            }
        }

        return latitudeLongitudeCloud;
    }

    public static OBJLoader referenceElipsoidMesh(){

        objLoader = new OBJLoader();
        Vector3f[][] latitudes = pointCloud();

        TextureData textureData = OpenGLLoader.decodeTextureFile("src/resources/worldtopobathy2004013x2.png"); //"src/resources/world.topo.bathy.200412.3x21600x10800.png");

        //float[] textures = new float[180*360];
        ArrayList<Vector2f> textureIndices = new ArrayList<>();
        for (int i = 0; i < 180; i++) {

            float y = (i == 0 )? (float)i : (i / 360f);
            for (int j = 360; j > -1; j--) {
                float x = j / 360f;
                textureIndices.add(new Vector2f(x, y));
            }
        }

        System.out.println("Tex Height: "+textureData.getHeight()+" Width: "+textureData.getWidth()+" Id: "+textureData.getId());

        objLoader.setTextureArray(textureIndices);
        objLoader.setTexture(textureData);

        ArrayList<Vector3f> pointsList = new ArrayList<>();

        for (int la = 0; la < latitudes.length ; la++) {
            Vector3f[] longitudes = latitudes[la];
            for (int lo = 0; lo < longitudes.length ; lo++) {
                pointsList.add(latitudes[la][lo]);
            }
        }

        objLoader.setVertices(pointsList);

        // faces
        //The Original loader takes data from a text file...
        ArrayList<String[][]>faceList = new ArrayList<>();


        for (int x = 0; x < numberOfLatitude -1 ; x++) {
            for (int z = 1; z < numberOfLongitude  ; z++) {

                int tl = x * numberOfLongitude + z; // top-left
                int bl = x * numberOfLongitude + z + 1; // bottom-left
                int tr = (x + 1) * numberOfLongitude + z; // top-right
                int br = (x + 1) * numberOfLongitude + z + 1; // bottom-right

                String[][] faceVector = new String[3][3];
                faceVector[0][0] = String.valueOf(tl);
                faceVector[0][1] = String.valueOf(tl);
                faceVector[1][0] = String.valueOf(bl);
                faceVector[1][1] = String.valueOf(bl);
                faceVector[2][0] = String.valueOf(tr);
                faceVector[2][1] = String.valueOf(tr);
                faceList.add(faceVector);
                //System.out.println("Face cnt: "+ faceList.size()+" face 1 : "+faceVector[0][0]+", "+faceVector[1][0]+", "+faceVector[2][0]);

                String[][] faceVector2 = new String[3][3];
                faceVector2[0][0] = String.valueOf(tr);
                faceVector2[0][1] = String.valueOf(tr);
                faceVector2[1][0] = String.valueOf(bl);
                faceVector2[1][1] = String.valueOf(bl);
                faceVector2[2][0] = String.valueOf(br);
                faceVector2[2][1] = String.valueOf(br);
                faceList.add(faceVector2);
                //System.out.println("Face cnt: "+ faceList.size()+" face 2 : "+faceVector2[0][0]+", "+faceVector2[1][0]+", "+faceVector2[2][0]);

            }
        }
        //System.out.println("Number 0f Earth Faces "+faceList.size());
        objLoader.setFaces(faceList);

        return objLoader;
    }

}
