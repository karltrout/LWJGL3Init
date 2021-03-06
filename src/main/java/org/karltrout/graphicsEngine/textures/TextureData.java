package org.karltrout.graphicsEngine.textures;

import org.joml.Vector2f;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class TextureData {

    private int width;
    private int height;
    private ByteBuffer buffer;
    private int textureId;
    private ArrayList<Vector2f> texCoords;
    private float[] texCoordArray;

    public TextureData(int textureId){
        this.textureId = textureId;
    }

    public TextureData(ByteBuffer buffer, int width, int height, int channels, int channel){

        this.buffer = buffer;
        this.height = height;
        this.width = width;

        // Create a new OpenGL texture
        this.textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //Send texel data to OpenGL

        glTexImage2D(GL_TEXTURE_2D, 0, channels, width,
                height, 0, channel, GL_UNSIGNED_BYTE, buffer);

        //buffer = null;
        System.out.println("Texture Binding Complete. id:"+textureId);

    }

    public TextureData(ByteBuffer buffer, int width, int height, int channels) {
        this(buffer, width, height, channels, channels);
    }

    public TextureData(ByteBuffer buffer, int width, int height) {
        this(buffer, width, height, GL_RGBA, GL_RGBA);
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