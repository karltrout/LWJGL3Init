package org.karltrout.graphicsEngine.models;

/**
 * Created by karltrout on 6/6/17.
 */
public class RawModel {

    private int vaoID;
    private int vertexCount;

    public RawModel(int vaoID, int vertexCount){

        this.vaoID = vaoID;
        this.vertexCount = vertexCount;

    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
