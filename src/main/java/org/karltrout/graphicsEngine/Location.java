package org.karltrout.graphicsEngine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.geotools.measure.*;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.Point2D;

/**
 * Created by karltrout on 7/28/17.
 *
 * ncols         10812
 * nrows         10812
 * xllcorner     -112.0005555556
 * yllcorner     32.99944444444
 * cellsize      9.2592592593e-005
 */
public class Location {

    GeodeticCalculator calc = new GeodeticCalculator();
    private final Vector2f center = new Vector2f(0,0);
    Vector3f worldPosition = new Vector3f(0,0,0);
    Vector2f latlong = new Vector2f(0,0);
    float pointToMeterRatio = 1.0f;
    double altitude =0.0d;
    private Vector2f baseLatitudeLongitude;

    /*
    world position = stating position
    starting latlon - starting latitude and longitude
     */
    public Location(Vector3f worldPosition, Vector2f startingLatlong){
        this.worldPosition = worldPosition;
        this.latlong = startingLatlong;
        this.altitude = worldPosition.y;
        try {
            this.baseLatitudeLongitude = getZeroLatitudeLongitude();
        } catch (TransformException e) {
            e.printStackTrace();
        }
        update();
    }



    private Vector2f getZeroLatitudeLongitude() throws TransformException {

        if (this.worldPosition.x == 0 && this.worldPosition.z == 0){
            return this.latlong;
        }
        try {

            calc.setStartingGeographicPoint(this.latlong.y, this.latlong.x);

            calc.setDirection(0.0f, this.worldPosition.distance(0, 0, 0));
            Point2D dp = calc.getDestinationGeographicPoint();
            Number x = dp.getX();
            Number y = dp.getY();
            return new Vector2f(x.floatValue(), y.floatValue());

        }catch (IllegalArgumentException iae){
            System.out.println(iae.getLocalizedMessage());
            iae.printStackTrace();
        }
        return new Vector2f(0, 0);
    }

    public void updatePosition(float x, float z){
        this.latlong = getLatitudeLongitude(x, z);
    }

    private Vector2f getLatitudeLongitude(float x, float z) {

            testBasePosition();
            calc.setStartingGeographicPoint(baseLatitudeLongitude.y, baseLatitudeLongitude.x);
            double direction = getAngle(center, new Vector2f(x, z));
            calc.setDirection(direction, center.distance(x, (-1 * z)) );
            Point2D dp = calc.getDestinationGeographicPoint();
            Number dpX = dp.getX();
            Number dpY = dp.getY();
            return new Vector2f(dpY.floatValue(), dpX.floatValue());

    }

    private boolean testBasePosition() {
        float bx = this.baseLatitudeLongitude.x();
        float by = this.baseLatitudeLongitude.y();

        try{

            Latitude  lx = new Latitude(bx);
            if (lx.degrees() < -90 || lx.degrees() > 90)
                throw new IllegalArgumentException("Latitude "+lx.degrees()+" is invalid!");
            Longitude ly = new Longitude(by);
            if (ly.degrees() > 180 || ly.degrees() < -180)
                throw new IllegalArgumentException("Longitude "+ly.degrees()+" is invalid!");

        }
        catch (IllegalArgumentException e){
            System.out.println("there was a problem trying to get the Latitude or Logitude of the position.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void update(){

        this.altitude  = this.worldPosition.y * pointToMeterRatio;
        double xMeters  = this.worldPosition.x * pointToMeterRatio;
        double yMeters = this.worldPosition.z * pointToMeterRatio;

    }

    public float getAngle(Vector2f start, Vector2f end){
        float delta_x = end.x - start.x;
        float delta_y = end.y - start.y;
        Number theta = Math.toDegrees(Math.atan2(delta_y, delta_x));
        return theta.floatValue() + 90.0f;
    }

    @Override
    public String toString(){
        return "Location: "+this.latlong.x+", "+this.latlong.y;
    }

}
