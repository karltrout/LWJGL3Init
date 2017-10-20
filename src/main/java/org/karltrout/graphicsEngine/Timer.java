package org.karltrout.graphicsEngine;

/**
 * Created by karltrout on 7/28/17.
 */
public class Timer
{

    private double lastLoopTime;
    private double startTime;

    public void init() {

        lastLoopTime = getTime();
        startTime = lastLoopTime;
    }

    public double getTime() {
        return System.nanoTime() / 1000_000_000.0;
    }

    public float getElapsedTime() {
        double time = getTime();
        float elapsedTime = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return elapsedTime;
    }

    public double getTimeFromStart(){
        return getTime() - startTime;
    }

    public double getLastLoopTime() {
        return lastLoopTime;
    }
}
