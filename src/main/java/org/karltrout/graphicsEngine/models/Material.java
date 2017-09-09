package org.karltrout.graphicsEngine.models;

import org.joml.Vector4f;
import org.karltrout.graphicsEngine.textures.TextureData;

/**
 * Keep information on Materials.
 * Created by karltrout on 8/24/17.
 */
public class Material {
    private static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColour;

    private Vector4f diffuseColour;

    private Vector4f specularColour;

    private float reflectance;

    private boolean texture;

    public Material() {
        this.ambientColour = DEFAULT_COLOUR;
        this.diffuseColour = DEFAULT_COLOUR;
        this.specularColour = DEFAULT_COLOUR;
        this.texture = false;
        this.reflectance = 0;
    }

    public Material(Vector4f colour, float reflectance) {
        this(colour, colour, colour, false, reflectance);
    }

    public Material(TextureData texture) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, false, 0);
    }

    public Material(TextureData texture, float reflectance) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, false, reflectance);
    }

    private Material(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, boolean isTexture, float reflectance) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.texture = isTexture;
        this.reflectance = reflectance;
    }

    public Vector4f getAmbientColour() {
        return ambientColour;
    }

    public void setAmbientColour(Vector4f ambientColour) {
        this.ambientColour = ambientColour;
    }

    public Vector4f getDiffuseColour() {
        return diffuseColour;
    }

    public void setDiffuseColour(Vector4f diffuseColour) {
        this.diffuseColour = diffuseColour;
    }

    public Vector4f getSpecularColour() {
        return specularColour;
    }

    public void setSpecularColour(Vector4f specularColour) {
        this.specularColour = specularColour;
    }

    public float getReflectance() {
        return reflectance;
    }

    public void setReflectance(float reflectance) {
        this.reflectance = reflectance;
    }

    public boolean isTextured() {
        return this.texture;
    }

    public void setTexture(boolean texture) {
        this.texture = texture;
    }
}
