package org.karltrout.graphicsEngine.models;

import org.karltrout.graphicsEngine.textures.TextureData;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengles.GLES20.GL_TEXTURE0;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Generate, create build wtf ever.
 * Created by karltrout on 7/22/17.
 */
public class Mesh {

    private TextureData texture;
    private int vaoId;
    private int vertexVboId;


    private int vertexCount;
    private float[] vertices;
    private int[] indices;
    private float[] texCoords;
    private int idxVboId;
    private int colorVboId;


    public Mesh(float[] vertices, float[] textureData, int[] indices) {
        this.vertexCount = indices.length;
        this.indices = indices;
        this.vertices = vertices;
        this.texCoords = textureData;
        build();

    }

    public Mesh() {

    }

    private void build(){
        // USE JOML Here...
        FloatBuffer floatBuffer = memAllocFloat(vertices.length);
        floatBuffer.put(vertices).flip();

        /* Generate a Vertex Array Object VAO. this
            object will be what is current until unbound
        */
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        /*Generate a Vertex Buffer Object VBO. these are used for all kinds of things.
          We are gonna put our float buffer array in this one.
         */
        vertexVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
        glBufferData(GL_ARRAY_BUFFER, floatBuffer, GL_STATIC_DRAW);
        memFree(floatBuffer);

        // Define Vertex Buffer Data to the shaders as an attribute.
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        //unbind the vertice VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //create the vertice index vbo
        idxVboId = glGenBuffers();
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();

        //load the indexArray vbo
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        //free the non jvm memory
        memFree(indicesBuffer);

        // Colour VBO
        colorVboId = glGenBuffers();
        FloatBuffer colourBuffer = memAllocFloat(texCoords.length);
        colourBuffer.put(texCoords).flip();
        glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
        glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_STATIC_DRAW);
        memFree(colourBuffer);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        //unbind the index VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVaoId() {
        return vaoId;
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vertexVboId);
        glDeleteBuffers(idxVboId);
        //glDeleteBuffers(texture.getId());
        glDeleteBuffers(colorVboId);
        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render() {
        // Bind to the VAO
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        if (texture != null) {
            // Activate first texture unit
            glActiveTexture(GL_TEXTURE0);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        // Draw the vertices by thier index
        glDrawElements(GL_TRIANGLES,vertexCount, GL_UNSIGNED_INT, 0);
        // Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    public void setTexture(TextureData texture) {
        this.texture = texture;
    }
}
