package org.karltrout.graphicsEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.Math;

/**
 * blah rewrite for position
 * Created by karltrout on 7/27/17.
 */
public class Camera implements ICamera {

    private final Vector3f position;
    private final Vector3f rotation;
    private Vector3f location = new Vector3f();
    private Logger logger = LogManager.getLogger();
    private final Matrix4f cameraAngle;

    public Matrix4f getCameraAngle() {
        return cameraAngle;
    }

    public Camera( ) {
        this.position = new Vector3f(0,0,0);
        this.rotation = new Vector3f(0,0,0);
        cameraAngle = new Matrix4f();
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

    public void moveToLocation (Vector3f target) {
        position.x = target.x;
        position.y = target.y;
        position.z = target.z;
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
        rotation.y += offsetZ;
        rotation.z += offsetY;

        cameraAngle.rotateLocalX((float) Math.toRadians(offsetX));
        cameraAngle.rotateLocalY((float) Math.toRadians(offsetY));
        cameraAngle.rotateLocalZ((float) Math.toRadians(offsetZ));
    }

    public Vector3f calculateRayPicker(Window window, Vector2d mousePosition, Matrix4f projectionMatrix, Matrix4f viewMatrix)

        {
            Vector2f normalizedCoords = getNormalisedDeviceCoordinates((float)mousePosition.x, (float)mousePosition.y, window);
          // logger.info("Mouse Position :"+mousePosition );
            Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
            Vector4f eyeCoords = toEyeCoords(clipCoords, projectionMatrix);
            Vector3f worldRay = toWorldCoords(eyeCoords, viewMatrix);
            return worldRay;
        }

        private Vector3f toWorldCoords(Vector4f eyeCoords,Matrix4f viewMatrix) {
            Matrix4f invertedView = new Matrix4f(viewMatrix).invert();
            Vector4f rayWorld = invertedView.transform(eyeCoords);
            Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
            mouseRay.normalize();
           // logger.info("Mouse Ray: "+mouseRay);
            return mouseRay;
        }

        private Vector4f toEyeCoords(Vector4f clipCoords, Matrix4f projectionMatrix) {
            Matrix4f invertedProjection = new Matrix4f(projectionMatrix).invert();
            Vector4f eyeCoords = invertedProjection.transform(clipCoords);
            return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
        }

        private Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY, Window window) {
            float x = (2.0f * mouseX) / window.getWidth() - 1f;
            float y = (2.0f * mouseY) / window.getHeight() - 1f;
            return new Vector2f(x, -y);
        }
        /*
        //Normalize mouse position
        float x = (float)(2.0f * mousePosition.x) / window.getWidth() - 1f;
        float y = (float)(2.0f * mousePosition.y) / window.getHeight() - 1f;
        Vector4f clipCoords = new Vector4f(x, y, -1.0f, 1.0f);
        //copy the matrixes
        //convert to eye Coordinates. This is where the camera is looking at using the projection matrix
        Vector4f eyeCoords = new Matrix4f(projectionMatrix).invert().transform(clipCoords);
        eyeCoords = new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
        //Convert this to the World Coordinates. This is where in the work the ray is pointing to.
        Vector4f rayWorld = new Matrix4f(viewMatrix).invert().transform(eyeCoords);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalize();

        //logger.info("Mouse ray X: "+mouseRay.x+", Y: "+mouseRay.y+", Z: "+mouseRay.z);

        return mouseRay;
        */


}
