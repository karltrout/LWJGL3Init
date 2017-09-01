package org.karltrout.graphicsEngine.Geodesy;

import org.joml.Vector3f;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.karltrout.graphicsEngine.OBJLoader;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by karltrout on 8/1/17.
 */
@RunWith(Theories.class)
public class ReferenceEllipsoidTest {
    @Test
    public void geocentricRectangularCoordinates() throws Exception {

        OBJLoader loader = ReferenceEllipsoid.referenceElipsoidMesh();

        assertNotNull(loader);


    }

    @Test
    public void pointCloud() throws Exception {
        ReferenceEllipsoid ref = new ReferenceEllipsoid();
        Vector3f[][] cloud = ref.pointCloud();

        // 90.0 degrees latitude, 0 degrees longitude
        assertEquals( -1116881.875, cloud[0][0].x, 0.000005d);
        assertEquals(  -9.597852413891335E-25d, cloud[0][0].y, 0.000005d);
        assertEquals(  -6.355778E7, cloud[0][0].z, 0d);

        // 0.0 degrees latitude, 90 degrees longitude
        assertEquals(-1113138.375, cloud[89][89].x, .000005d);
        assertEquals(-6.3771656E7, cloud[89][89].y, 0.00005d);
        assertEquals(0.0, cloud[89][89].z, 0.00005d);

        // 0.0 degrees latitude, 180 degrees longitude
        assertEquals(6.3781368E7, cloud[89][180].x, .000005d);
        assertEquals(7.810964816643943E-9d, cloud[89][180].y, 0.000005d);
        assertEquals(0.0, cloud[89][180].z, 0.000005d);

        // 0.0 degrees latitude, 270 degrees longitude
        assertEquals(1112970.0d, cloud[90][269].x, .000005d);
        assertEquals(6.3771656E7d, cloud[89][269].y, 0.00005d);
        assertEquals(0.0, cloud[89][269].z, 0.00005d);


        // -90.0 degrees latitude, 360 degrees longitude
        assertEquals(-2233076.5d, cloud[177][359].x, 0.00005d);
        assertEquals(38978.49609375d, cloud[177][359].y, 0.00005d);
        assertEquals(6.352854E7d, cloud[177][359].z, 0.00005d);

    }

    @Test
    public void distanceFromCenterAtLatitude() throws Exception {

        Number result = ReferenceEllipsoid.distanceFromCenterAtLatitude(90d);
        assertEquals(63567.523d, result.doubleValue(), 0.001);

    }

    @DataPoints public static float[] latitudes = {90.0f, 75.0f, 62.5f, 51.25f, 40.0f, 33.33f, 21.0f, 9.2f, 5.45f, 3.145f, 0f
                                            -5.0f,  -10f, -22.f, -35.0f, -45.f, -55.0f, -66.6f, -74f, -81.f, -90.0f
                                        };
    @Theory
    public void convertGeoDesicToCartisianAndBack(float latitude){

        Random rand = new Random();
        float longitude = ( rand.nextFloat() * 360.0f ) - 180.0f;

        System.out.println("lat: "+latitude+" lon: "+longitude);
        Vector3f vector3f = ReferenceEllipsoid.cartesianCoordinates(latitude, longitude,0);
        Vector3f location = ReferenceEllipsoid.geocentricCoordinates(vector3f.x, vector3f.y, vector3f.z);
        assertEquals(latitude, location.x, .001f);
        assertEquals(longitude, location.y, .001f);
    }

}