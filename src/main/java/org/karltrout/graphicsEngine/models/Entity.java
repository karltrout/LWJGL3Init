package org.karltrout.graphicsEngine.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.*;
import org.karltrout.graphicsEngine.Location;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;
import org.lwjgl.opengl.GL11;

import java.lang.Math;
import java.text.NumberFormat;

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
    private TerrainMesh currentTerrain;
    private Location location;
    private int cullFace = GL11.GL_BACK;
    private int frontFace = GL11.GL_CCW;
    private Matrix4f modelMatrix = new Matrix4f().identity();

    private int minAltitude = 0;
    private int maxAltitude = 120000000;
    private Logger logger = LogManager.getLogger();
    private float radius = 50f;

    private boolean selectable = false;

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
        //calculateRotation();
    }

    private void calculateRotation() {
       double z = Math.atan((double)position.x/(double)position.y);
       double y = Math.atan((double)position.x/(double)position.z);
       double x = 3.142 +  Math.atan((double)position.y /(double)position.z);
       logger.info("Entity X Rot: "+Math.toDegrees(x)+" Y Rot: "+Math.toDegrees(y)+" Z Rot: "+Math.toDegrees(z));
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

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;

    }

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

    public void setLocation(Vector2f latlong) {

        if (this.currentTerrain != null){
            Vector3f pos = currentTerrain.getWorldPosition(latlong);
            setPosition(pos.x, pos.y, pos.z);
        }

        this.location = new Location(this.position, latlong);

        System.out.println("Entity "+location);
        System.out.println("Entity Position  X: "+position.x + " Y: " + position.y + " Z: "+position.z);
    }

    public void setTerrain(TerrainMesh terrainMesh) {
        this.currentTerrain = terrainMesh;
    }

    public void setCullFace(int cullFace){
        this.cullFace = cullFace;
    }

    public int getCullFace() {
        return cullFace;
    }

    public int getMinAltitude() {
        return minAltitude;
    }

    public void setMinAltitude(int minAltitude) {
        this.minAltitude = minAltitude;
    }

    public int getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(int maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public int getFrontFace() {
        return frontFace;
    }

    public void setFrontFace(int frontFace) {
        this.frontFace = frontFace;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void setWorldPosition(){
        double tanX = Math.atan(position.y/position.z);
        double tanY = Math.atan(position.x/position.z);
        System.out.println("X: "+Math.toDegrees(tanX)+" Y: "+Math.toDegrees(tanY));
//        modelMatrix.lookAt(this.position, new Vector3f(), new Vector3f(0,1,0) );
    }


    public boolean intersectedByRay(Vector3f origin, Vector3f ray) {

        Vector2f result = new Vector2f();
        //logger.info("O: "+origin+" R:"+ray+" P:"+position+" r:"+radius);
        Intersectionf.intersectRaySphere(origin, ray, position,radius*radius, result);

        //logger.info("intersection: "+result);
        return Intersectionf.intersectRaySphere(origin, ray, position,radius*radius, result);
        /*Vector3f u = new Vector3f(origin).sub(position);
        float puv = new Vector3f(ray).dot(u);
logger.info("U: "+u+" puv: "+puv);
        // is the point in front of the ray?
        if ( puv > 0 ){
            //compute distance from center of entity to the Ray
            Vector3f pointOnRay = new Vector3f(ray).mul(new Vector3f(ray).dot(position));

            Vector3f distOnRay = new Vector3f(origin).add(pointOnRay);
            float rayToSphereLength = position.distance(distOnRay);
            logger.info("Distance to point: "+ rayToSphereLength);
            if(radius > rayToSphereLength ) return true;
        }
        //float c = (float) (distance.dot(distance) - Math.pow(this.radius, 2));
*/
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }
}
