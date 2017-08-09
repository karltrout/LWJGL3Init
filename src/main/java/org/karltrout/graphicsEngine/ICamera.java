package org.karltrout.graphicsEngine;

import org.joml.Vector3f;

/**
 * Created by karltrout on 8/5/17.
 */
public interface ICamera {

    /**
     * Location is the geoSpatial positioning of Latitude, Longitude and Height.
     * -90  <= latitude  <= 90
     * -180 <= longitude <= 180
     * height > 0 +
     *
     * @return Vector3f containing Latitude, Longitude and Height as floats
     */
    Vector3f getLocation();

    Vector3f getPosition();

    Vector3f getRotation();

    void moveTo(float xOffset, float yOffset, float zOffset);

    void updatePosition();

    void updateLocation();

    void setRotation(float x, float y, float z);

    void moveRotation(float offsetX, float offsetY, float offsetZ);

}
