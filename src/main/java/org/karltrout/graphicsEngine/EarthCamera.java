package org.karltrout.graphicsEngine;

import org.joml.Vector3f;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;

/**
 * Created by karltrout on 8/5/17.
 */
public class EarthCamera implements ICamera {

    Vector3f position = null;
    Vector3f rotation = null;
    Vector3f location = null;

    public EarthCamera(){
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        updateLocation();
    }

    public EarthCamera(float latitude, float longitude, float altitude){
        this.location = new Vector3f(latitude, longitude, altitude);
        updatePosition();
    }

    private void updateBasedOnLocation() {
        //this.position = ReferenceEllipsoid.cartesianCoordinates(location.x, location.y, location.z);
        updateRotation();
    }

    private void updateRotation() {
        return;
    }

    @Override
    public Vector3f getLocation() {
        return location;
    }

    @Override
    public Vector3f getPosition() {
         return position;
    }

    @Override
    public Vector3f getRotation() {
         return rotation;
    }

    @Override
    public void moveTo(float xOffset, float yOffset, float zOffset) {

        if ( zOffset != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * zOffset;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * zOffset;
        }
        if ( xOffset != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * xOffset;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * xOffset;
        }
        position.y += yOffset;

        updateLocation();

    }

    @Override
    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    @Override
    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }

    @Override
    public void updatePosition() {
        this.position = ReferenceEllipsoid.cartesianCoordinates(location.x, location.y, location.z);
        updateRotation();
    }

    @Override
    public void updateLocation() {
        // Update Location here based on current position.
        this.location = ReferenceEllipsoid.geocentricCoordinates(position.x, position.y, position.z);
        updateRotation();
    }
}
