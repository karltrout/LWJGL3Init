package org.karltrout.graphicsEngine.renderers;

import org.joml.*;
import org.karltrout.graphicsEngine.Camera;
import org.karltrout.graphicsEngine.models.Entity;

import java.lang.Math;

/**
 * Created by karltrout on 7/22/17.
 * blah
 */
public class Transformation {
    private final Matrix4f projectionMatrix;
    private final Matrix4f worldMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f orthoMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f orthoModelMatrix;
    private final Matrix4f transformationMatrix;

    public Transformation() {

        worldMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        orthoModelMatrix = new Matrix4f();
        transformationMatrix = new Matrix4f();
    }

    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();
        viewMatrix.identity();
        // First do the rotation so camera rotates over its position
        //viewMatrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
        //        .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0))
        //        .rotate((float)Math.toRadians(rotation.z), new Vector3f(0,0,1));
        // Then do the translation, remember the worl comes to you so its a negtive movement
        viewMatrix.mul(camera.getCameraAngle()).translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public final Matrix4f getProjectionMatrix(float fov, float width, float height,
                                              float zNear, float zFar) {

        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);

        return projectionMatrix;

    }

    public Matrix4f getModelViewMatrix(Entity entity, Matrix4f viewMatrix) {
        Matrix4f modelMatrix = new Matrix4f(entity.getModelMatrix()).scale(entity.getScale());
        modelViewMatrix.identity().translate(entity.getPosition());
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix).mul(modelMatrix);
    }

    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom,
                                                   float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    public Matrix4f buildOrtoProjModelMatrix(Entity entity, Matrix4f orthoMatrix) {
        Vector3f rotation = entity.getRotation();
        modelMatrix.identity().translate(entity.getPosition()).
                rotateX((float) Math.toRadians(-rotation.x)).
                rotateY((float) Math.toRadians(-rotation.y)).
                rotateZ((float) Math.toRadians(-rotation.z)).
                scale(entity.getScale());
        orthoModelMatrix.set(orthoMatrix);
        orthoModelMatrix.mul(modelMatrix);
        return orthoModelMatrix;
    }

    public Matrix4f getTransformationMatrix(Entity entity) {

        Matrix4f matrix = new Matrix4f();
        Vector3f translation = entity.getPosition();
        float rx =  entity.getRotation().x;
        float ry =  entity.getRotation().y;
        float rz =  entity.getRotation().z;
        float scale = entity.getScale();

        matrix.translate(translation);

        matrix.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0));
        matrix.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0));
        matrix.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1));

        matrix.scale(new Vector3f(scale,scale,scale));

        return matrix;
    }
}
