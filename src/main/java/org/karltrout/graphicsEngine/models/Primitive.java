package org.karltrout.graphicsEngine.models;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Generate, create build wtf ever.
 * Created by karltrout on 7/22/17.
 */
public class Primitive implements Renderable {

    private int vaoId;
    private int vboId;
    private int vertexCount;
    private float[] vertices;
    private float[] colorIndices;
    private int mode;
    private int depth;

    private Material material;

    public Primitive(float[] vertices, float[] colorData, int mode, int depth, Material material ) {
        this.mode = mode;
        this.depth = depth;
        this.vertexCount = vertices.length/depth;
        this.vertices = vertices;
        this.colorIndices = colorData;
        this.material = material;
        build();

    }

    private void build(){

        FloatBuffer verticesBuffer = null;
        try {

            verticesBuffer = memAllocFloat(vertices.length+1);
            verticesBuffer.put(vertices).flip();
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);
            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, depth, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            if (verticesBuffer != null) {
                memFree(verticesBuffer);
            }
        }
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render() {
// Draw the mesh
       // glDisable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glDrawArrays(mode, 0, vertexCount);
// Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

      //  glEnable(GL_DEPTH_TEST);
    }

    @Override
    public boolean hasTexture() {
        return false;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
