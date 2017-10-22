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

    /**
     * Aeronautical Drag:
     * R = 1/2 * ρ * Cd * A * v^2
     * ρ (rho) = density of Fluid (air)
     * Cd = Coefficient of drag
     * A = Surface Area
     * v^2 = velocity squared
     */
    public double aeronauticalDrag(float rho, float Cd, float area, double velocity ){
        return .5 * rho * Cd * area * (velocity * velocity);
    }


}
