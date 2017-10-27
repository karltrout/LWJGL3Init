package org.karltrout.physics;

import org.karltrout.graphicsEngine.Timer;

public class Dynamics {
    public Dynamics() {
    }


    /**
     *  acceleration(N/kg) = force(N)/mass(kg)
     */
    public static double acceleration(double newtons, float kilograms){
        return newtons/kilograms;
    }

    /**
     * Aeronautical Drag:
     * R = 1/2 * ρ * Cd * A * v^2
     * ρ (rho) = density of Fluid (air)
     * Cd = Coefficient of drag
     * A = Surface Area
     * v^2 = velocity squared
     */
    public static double aeronauticalDrag(float rho, float Cd, float area, double velocity ){
        return .5 * rho * Cd * area * (velocity * velocity);
    }


    /**
     *  Return the velocity in meters per second given the Force applied to the Mass for a given amount of time
     *  @param force kilo Newton force
     *  @param mass Mass in kilo grams
     *  @param time length of time in seconds.
     *  @return  distance in kilometers
     */
    private static double velocity(double force, float mass, double time){
        // acc = F/m
        // acc = (dist/time)/time
        // (dist/time)/time = acc;
        // dist/time = acc * time;
        // dist/time = F/m * time;
        return acceleration(force,mass) * time;

    }


    public static void main (String[] args){

        Timer timer = new Timer();
        timer.init();
        double time, drag;
        double velocity = 0.0d;
        float distance = 0f;
        while (velocity < 77.17){ //150 Knots

            try { // take a break for a sec.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            time = timer.getElapsedTime();
            drag = Dynamics.aeronauticalDrag(10.13f, .018130f, 909.22f, velocity);
            velocity = Dynamics.currentVelocity(velocity, (4*332300.0d * .7 ) - drag, 596000, time);  //596000 kg full weight

            distance += velocity * time;

            System.out.println("Sleeping... "+timer.getTimeFromStart()+" t: "+ time+ " v: "+velocity+" d: "+(velocity * time));

        }

        System.out.println("Total Time :"+ timer.getTimeFromStart()+" Total Distance: "+distance+" Last Velocity: "+velocity);
        
    }

    public static double currentVelocity(double velocity, double force, float mass, double time) {
        return velocity + velocity(force, mass, time);
    }
}
