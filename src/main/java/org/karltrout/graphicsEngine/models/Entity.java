package org.karltrout.graphicsEngine.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.*;
import org.karltrout.graphicsEngine.Geodesy.ReferenceEllipsoid;
import org.lwjgl.opengl.GL11;

import java.lang.Math;

/**
 * An Object in the World.
 * Holds the objects renderable, position, scale and rotation values.
 * <p>
 * Created by karltrout on 7/22/17.
 */
public class Entity {

    private final Renderable renderable;
    private final Vector3f position;
    private float scale;
    private final Vector3f rotation;
    private Boolean wireMesh = false;
    private Vector3d location;
    private int cullFace = GL11.GL_BACK;
    private int frontFace = GL11.GL_CCW;
    private Matrix4f modelMatrix = new Matrix4f().identity();

    //private int minAltitude = 0;
    private int maxAltitude = 120000000;
    private Logger logger = LogManager.getLogger();

    private boolean selectable = false;
    private boolean selected = false;
    private float scaleFactor;

    public Entity(Renderable renderable) {
        this.renderable = renderable;
        position = new Vector3f(0, 0, 0);
        scale = 1;
        rotation = new Vector3f(0, 0, 0);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
        //calculateRotation();
    }

    public void setPosition(Vector3f target) {
        this.position.x = target.x;
        this.position.y = target.y;
        this.position.z = target.z;
        calculateRotation();
    }

    /**
     * Call this if you want your entity to do cartwheels ;-)
     */
    private void calculateRotation() {
       float z = (float)Math.atan((double)position.x/(double)position.y);
       float y = (float)Math.atan((double)position.x/(double)position.z);
       float x = (float)(3.142 +  Math.atan((double)position.y /(double)position.z));
       //this.moveRotation(x, y, z);
       //logger.info("Entity X Rot: "+Math.toDegrees(x)+" Y Rot: "+Math.toDegrees(y)+" Z Rot: "+Math.toDegrees(z));
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

   /* public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    } */

    public void  moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
        this.modelMatrix.rotateX((float)Math.toRadians(offsetX));
        this.modelMatrix.rotateY((float)Math.toRadians(offsetY));
        this.modelMatrix.rotateZ((float)Math.toRadians(offsetZ));
    }

    public Renderable getRenderable() {
        return renderable;
    }

    public boolean isWireMesh() {
        return wireMesh;
    }

    public void makeWireFrame(Boolean wireFrame){
        wireMesh = wireFrame;
    }

    /* Not Using this form of setLocation.
    public void setLocation(Vector2f latlong) {

        if (this.currentTerrain != null){
            Vector3f pos = currentTerrain.getWorldPosition(latlong);
            setPosition(pos.x, pos.y, pos.z);
        }

        this.location = new Location(this.position, latlong);

        System.out.println("Entity "+location);
        System.out.println("Entity Position  X: "+position.x + " Y: " + position.y + " Z: "+position.z);
    }*/

    /*public void setCullFace(int cullFace){
        this.cullFace = cullFace;
    }*/

    public int getCullFace() {
        return cullFace;
    }

    /*public int getMinAltitude() {
        return minAltitude;
    }

    public void setMinAltitude(int minAltitude) {
        this.minAltitude = minAltitude;
    }*/
    public int getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(int maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public int getFrontFace() {
        return frontFace;
    }

    /*public void setFrontFace(int frontFace) {
        this.frontFace = frontFace;
    }*/

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public boolean intersectedByRay(Vector3f origin, Vector3f ray) {
        Vector2f result = new Vector2f();
        float radius = 3f;
        Intersectionf.intersectRaySphere(origin, ray, position, radius * radius, result);
        return Intersectionf.intersectRaySphere(origin, ray, position, radius * radius, result);
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void updatePosition(float speed, float interval){
        //float speed = speed; //30.0 m/s
        double distance = (speed * interval)/111111.0;
        this.location.add(0,distance, 0);
        Vector3f newPosition = ReferenceEllipsoid.cartesianCoordinates(location.x, location.y, location.z).mul(scaleFactor);
        //logger.info(String.format("Current Location: %f , %f, %f", newPosition.x, newPosition.y, newPosition.z));
        setPosition(newPosition);

    }

    public void setLocation(Vector3d locationVector, float scaleFactor) {
        this.location = locationVector;
        this.scaleFactor = scaleFactor;
    }
}
