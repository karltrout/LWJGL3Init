package org.karltrout.physics;

import javax.measure.quantity.Acceleration;

public class Dynamics {
    public Dynamics() {
    }


    /**
     *  acceleration(N/kg) = force(N)/mass(kg)
     */
    public static double acceleration(double newtons, double kilograms){
        return newtons/kilograms;
    }

    public static double acceleration(float metersDelta, float secondsDelta){
        return metersDelta / secondsDelta;
    }

    public static double velocity(double initialVelocity, double acceleration, float secondsDelta ){
        return initialVelocity + acceleration * secondsDelta;
    }


}
