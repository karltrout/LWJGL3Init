package org.karltrout.graphicsEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Created by karltrout on 7/27/17.
 */
public class Camera {
    private final Vector3f position;
    private final Vector3f rotation;
    private Location location = null;
    private Logger logger = LogManager.getLogger();
    private int timer = 0;

    public Camera() {
        this.position = new Vector3f(0,0,0);
        this.rotation = new Vector3f(0,0,0);
    }

    public void setLocation(Vector2f latitudeLongitude, float altitude){
        movePosition(0, altitude, 0);
        this.location = new Location(this.position, latitudeLongitude);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;

        if(offsetX != 0 || offsetZ != 0){
            this.location.updatePosition(position.x, position.z);
            System.out.println(this.location);
            System.out.println("X: "+this.position.x+" Y: "+position.y+" Z: "+position.z);
        }

    }

    public Vector3f getRotation() {
        return rotation;
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
