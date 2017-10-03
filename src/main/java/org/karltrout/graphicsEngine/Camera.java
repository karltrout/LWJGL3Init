package org.karltrout.graphicsEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * blah rewrite for position
 * Created by karltrout on 7/27/17.
 */
public class Camera implements ICamera {

    private final Vector3f position;
    private final Vector3f rotation;
    private Vector3f location = new Vector3f();
    private Logger logger = LogManager.getLogger();

    public Camera() {
        this.position = new Vector3f(0,0,0);
        this.rotation = new Vector3f(0,0,0);
    }

    public void setLocation(Vector3f latitudeLongitudeAltitude){
        this.location = latitudeLongitudeAltitude;
    }

    @Override
    public Vector3f getLocation() {
        return this.location;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void moveToLocation (float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    @Override
    public void moveTo(float offsetX, float offsetY, float offsetZ) {

        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;

    }

    public Vector3f getRotation() {
        return rotation;
    }

    @Override
    public void updatePosition() {
        throw new NotImplementedException();
    }

    @Override
    public void updateLocation() {
        throw new NotImplementedException();
    }

    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }

}
