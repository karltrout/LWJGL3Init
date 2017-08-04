package org.karltrout.graphicsEngine.models;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.Location;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;

/**
 * An Object in the World.
 * Holds the objects mesh, position, scale and rotation values.
 * <p>
 * Created by karltrout on 7/22/17.
 */
public class Entity {

    private final Mesh mesh;
    private final Vector3f position;
    private float scale;
    private final Vector3f rotation;
    private Boolean wireMesh = false;
    private TerrainMesh currentTerrain;
    private Location location;

    public Entity(Mesh mesh) {
        this.mesh = mesh;
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

    public Mesh getMesh() {
        return mesh;
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
}
