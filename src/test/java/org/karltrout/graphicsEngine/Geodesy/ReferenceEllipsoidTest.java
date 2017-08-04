package org.karltrout.graphicsEngine.Geodesy;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.junit.Test;
import org.karltrout.graphicsEngine.OBJLoader;
import org.karltrout.graphicsEngine.models.Mesh;

import static org.junit.Assert.*;

/**
 * Created by karltrout on 8/1/17.
 */
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
        assertEquals( 3.9186206261975835E-9, cloud[0][0].x, 0.000005d);
        assertEquals(  -9.597852413891335E-25d, cloud[0][0].y, 0.000005d);
        assertEquals(  6.3567524E7, cloud[0][0].z, 0d);

        // 0.0 degrees latitude, 90 degrees longitude
        assertEquals(1112970.0d, cloud[89][89].x, .000005d);
        assertEquals(6.3762004E7d, cloud[89][89].y, 0.00005d);
        assertEquals(1105687.875, cloud[89][89].z, 0.00005d);

        // 0.0 degrees latitude, 180 degrees longitude
        assertEquals(-6.3771716E7, cloud[89][180].x, .000005d);
        assertEquals(7.810964816643943E-9d, cloud[89][180].y, 0.000005d);
        assertEquals(1105687.875, cloud[89][180].z, 0.000005d);

        // 0.0 degrees latitude, 270 degrees longitude
        assertEquals(-1113138.375d, cloud[90][269].x, .000005d);
        assertEquals(-6.3762004E7d, cloud[89][269].y, 0.00005d);
        assertEquals(1105687.875, cloud[89][269].z, 0.00005d);


        // -90.0 degrees latitude, 360 degrees longitude
        assertEquals(1116711.75d, cloud[179][359].x, 0.00005d);
        assertEquals(-19492.275390625d, cloud[179][359].y, 0.00005d);
        assertEquals(-6.3557776E7d, cloud[179][359].z, 0.00005d);

    }

    @Test
    public void distanceFromCenterAtLatitude() throws Exception {

        Number result = ReferenceEllipsoid.distanceFromCenterAtLatitude(90d);
        assertEquals(63567.523d, result.doubleValue(), 0.001);

    }

}