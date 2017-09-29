package org.karltrout.graphicsEngine.models;

import org.karltrout.graphicsEngine.textures.TextureData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Created by karltrout on 9/26/17.
 */
public class TextMesh implements Renderable {

    private TextureData texture;
    private int vaoId;
    private float[] vertices;
    private float[] texCoords;
    private int vertexVboId = -1;
    private int texVboId = -1;
    private Material material;
    private int vertexCount = 0;

    public TextMesh(Material material){
        this.material = material;
    }

    public void build(){

        FloatBuffer verticesBuffer = memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();

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
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        memFree(verticesBuffer);
        //vertices = null;

        // Define Vertex Buffer Data to the shaders as an attribute.
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        // texture VBO
        texVboId = glGenBuffers();
        FloatBuffer textCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        textCoordsBuffer.put(texCoords).flip();
        glBindBuffer(GL_ARRAY_BUFFER, texVboId);
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
        memFree(textCoordsBuffer);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        //texCoords = null;

        //unbind the VAO

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        System.out.println("--->>> Text texture VBO id :"+texVboId);
    }

    public void updateData(float[] vertices, float[] texCoords){

        this.vertices = vertices;
        //this.vertices = new float[]{0.0f, -512.0f, 0.0f, 0.0f, 512.0f, -512.0f, 512.0f, -512.0f, 0.0f, 0.0f, 512.0f, 0.0f};
        this.texCoords = texCoords;
        //this.texCoords = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        this.vertexCount = vertices.length;

        if ( vertexVboId == -1 || texVboId == -1 ) build();
        else {
            glBindVertexArray(vaoId);

            FloatBuffer verticesBuffer = memAllocFloat(this.vertices.length);
            verticesBuffer.put(this.vertices).flip();

            glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            memFree(verticesBuffer);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

            FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(this.texCoords.length);
            texCoordsBuffer.put(this.texCoords).flip();

            glBindBuffer(GL_ARRAY_BUFFER, texVboId);
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            memFree(texCoordsBuffer);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

    }

    @Override
    public void cleanUp() {

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vertexVboId);

        if(texture != null)
            glDeleteBuffers(texture.getId());

        glDeleteBuffers(texVboId);
        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);

    }

    @Override
    public void render() {


        //GL11.glFrontFace(GL11.GL_CW);
        //GL11.glCullFace(GL_BACK);
        // Bind to the VAO
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_TRIANGLES,0, vertexCount);
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        //GL11.glFrontFace(GL11.GL_CCW);
    }

    @Override
    public boolean hasTexture() {
        return texture != null;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
