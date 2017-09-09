package org.karltrout.graphicsEngine.textures;

import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureData {

    private int width;
    private int height;
    private ByteBuffer buffer;
    private int textureId;
    private ArrayList<Vector2f> texCoords;
    private float[] texCoordArray;

    public TextureData(ByteBuffer buffer, int width, int height){

        this.buffer = buffer;
        this.height = height;
        this.width = width;

        // Create a new OpenGL texture
        this.textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width,
                height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        buffer = null;
        System.out.println("Texture Binding Complete.");

    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    //public ByteBuffer getBuffer(){
    //    return buffer;
    //}

    public int getId(){
        return this.textureId;
    }

    public float[] getCoords(){
        return this.texCoordArray;
    }

    public void setTexCoords(ArrayList<Vector2f> texCoords) {
        this.texCoords = texCoords;
        texCoordArray = new float[texCoords.size() * 2];
        int i = 0;
        for(Vector2f v2f : this.texCoords){
            texCoordArray[i++] = v2f.x;
            texCoordArray[i++] = v2f.y;
        }
    }
}