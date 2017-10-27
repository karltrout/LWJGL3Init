package org.karltrout.physics;

import org.junit.Test;
import org.karltrout.graphicsEngine.Timer;

import static com.google.common.collect.Range.greaterThan;
import static org.junit.Assert.*;

/**
 * Created by karltrout on 10/24/17.
 */
public class DynamicsTest {

    @Test
    public void aeronauticalDrag() throws Exception {
        double drag = Dynamics.aeronauticalDrag(10.13f, .018130f, 909.22f, 1);

        assertEquals(83.4922646, drag, 0.0000005);
    }

    @Test
    public void currentVelocity() throws Exception {
    }

    @Test
    public void acceleration() throws Exception {
        assertEquals(Dynamics.acceleration(5, 10), 10, 0.0);
    }



    @Test
    public void takeOffAcceleration() throws Exception {
        Timer timer = new Timer();
        timer.init();
        double time, drag;
         double velocity = 0.0d;
        float distance = 0f;
        while (velocity < 77.17) { //150 Knots

            try { // take a break for a sec.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            time = timer.getElapsedTime();
            drag = Dynamics.aeronauticalDrag(10.13f, .018130f, 909.22f, velocity);
            velocity = Dynamics.currentVelocity(velocity, (4 * 332300.0d * .7) - drag, 596000, time);  //596000 kg full weight

            distance += velocity * time;
        }
        Float t = 2790.0f;
        assertTrue("distance" +distance+" is not greater then "+t, distance > t);

    }
}