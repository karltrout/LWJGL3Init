package org.karltrout.graphicsEngine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by karltrout on 7/29/17.
 */
public class PositionTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
        Vector3f worldPosition = new Vector3f(0,0,0);
        Vector2f location = new Vector2f( -112.0005555556f, 32.99944444444f);
        double altitude = 0d;
        Location position = new Location(worldPosition, location);

        @Test
        public void movePosition(){

            //position.updatePosition(10,10);
            //Vector2f newPosition = position.latlong;
           // assertEquals(newPosition.x,-112.00045, 0.00005 );
           // assertEquals(newPosition.y, 32.999535, 0.00005 );
        }

        @Test
        public void angle(){
           Vector2f start = new Vector2f(0,0);
           Vector2f end = new Vector2f(10,10);
           float angle = position.getAngle(start, end);
           assertEquals(angle, 135d, 0);
        }

        @Test
    public void lengthofLatitude(){
            float latitude = 33.00f;

           Number y = (111132.954d - (559.822d * Math.cos( 2d * Math.toRadians(30d))) + (1.175d * Math.cos( 4d * Math.toRadians(latitude) )));

           assertEquals(110852.25677153754d, y.doubleValue(),  .000000005d);
        }

}