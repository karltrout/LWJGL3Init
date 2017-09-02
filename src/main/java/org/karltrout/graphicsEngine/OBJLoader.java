package org.karltrout.graphicsEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.models.Material;
import org.karltrout.graphicsEngine.models.Mesh;
import org.karltrout.graphicsEngine.textures.TextureData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * yeah this needs a lot of work...
 * getting better
 * Created by karltrout on 6/9/17.
 */
public class OBJLoader {

    private float[] normalsArray = null;
    private float[] textureArray = null;
    private List<Vector3f> vertices = new ArrayList<>();
    private List<Vector2f> textures = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    private List<String[][]> faces = new ArrayList<>();
    private TextureData texture;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public OBJLoader(){
    }

    public Mesh loadObjModel(String fileName) throws FileNotFoundException {

        logger.debug("Loading Model :"+fileName);
        FileReader reader = new FileReader(new File("resources/models/" + fileName + ".obj"));
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) break;
                String[] currentLine = line.split(" ");
                if (line.startsWith("v ")) {
                    Vector3f vertex = new Vector3f(
                            Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    vertices.add(vertex);

                } else if (line.startsWith("vt ")) {

                    Vector2f texture = new Vector2f(
                            Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2])
                    );
                    textures.add(texture);

                } else if (line.startsWith("vn ")) {

                    Vector3f normal = new Vector3f(Float.parseFloat(
                            currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    normals.add(normal);

                } else if (line.startsWith("f ")) {

                    String[][] faceVector = new String[3][3];
                    faceVector[0] = currentLine[1].split("/");
                    faceVector[1] = currentLine[2].split("/");
                    faceVector[2] = currentLine[3].split("/");

                    faces.add(faceVector);

                }

            }

            textureArray = new float[vertices.size() * 2];
            normalsArray = new float[vertices.size() * 3];

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return this.build();
    }
    public void setVertices(List<Vector3f> vertices){
        this.vertices = vertices;
        textureArray = new float[ this.vertices.size() * 2 ];
        normalsArray = new float[ this.vertices.size() * 3 ];
    }

    public void setFaces(List<String[][]> facesList){
        this.faces = facesList;
    }

    public Mesh build() {

        for (String[][] face : faces) {
            processVertex(face[0], indices, textures, normals, textureArray, normalsArray);
            processVertex(face[1], indices, textures, normals, textureArray, normalsArray);
            processVertex(face[2], indices, textures, normals, textureArray, normalsArray);

        }

        float[] verticesArray = new float[vertices.size() * 3];
        int[] indicesArray = new int[indices.size()];

        int vertexPointer = 0;
        for (Vector3f vertex : vertices) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        Material material = new Material();
        Mesh mesh;

        if (this.textures.size() > 0) {
            material.setTexture(texture);
            mesh = new Mesh(verticesArray, normalsArray, textureArray, indicesArray, material);
            mesh.setTexture(this.texture);
        }
        else {
            mesh = new Mesh(verticesArray, normalsArray, null, indicesArray, material);
        }

        return mesh;

    }

    private void processVertex(
            String[] vertexData,
            List<Integer>indices,
            List<Vector2f> textures,
            List<Vector3f> normals,
            float[] textureArray,
            float[] normalsArray) {

        int currentVertexPointer = Integer.parseInt(vertexData[0]) -1;
        indices.add(currentVertexPointer);

        if (textures.size() > 0) {
            Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) -1);
            textureArray[currentVertexPointer * 2] = currentTex.x;
            textureArray[currentVertexPointer * 2 + 1] =  currentTex.y;
        }
        else{
            textureArray[(currentVertexPointer) * 2] = 1;
            textureArray[(currentVertexPointer) * 2 + 1] = 1;
        }

        if (normals.size() > 0 ) {
            Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
            normalsArray[currentVertexPointer * 3] = currentNorm.x;
            normalsArray[currentVertexPointer * 3 + 1] = currentNorm.y;
            normalsArray[currentVertexPointer * 3 + 2] = currentNorm.z;
        }
        else{
            normalsArray[(currentVertexPointer) * 3] = 1 ;
            normalsArray[(currentVertexPointer) * 3 + 1] = 1 ;
            normalsArray[(currentVertexPointer) * 3 + 2] = 1;
        }

    }

    public void setTextureArray(List<Vector2f> textureList){
        this.textures = textureList;
    }

    public void setTexture(TextureData texture) {
        this.texture = texture;
    }

    public void setNormals(List<Vector3f> normals) {
        this.normals = normals;
    }

    public void calculateNormals(){
        this.normals = createNormals(this.vertices, this.faces);
    }

    /**
     *  see: https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
     * @param pointsList ArrayList of vertices.
     * @param faceList ArrayList of faces in string arrays
     * @return ArrayList of average normals of each of the faces sharing a common vertex.
     */
    private static ArrayList<Vector3f> createNormals(List<Vector3f> pointsList, List<String[][]> faceList) {

        SortedMap<Integer, ArrayList<Vector3f>> vectorNormals = new TreeMap<>();

        for (String[][] face :
                faceList) {
            int i0 = Integer.valueOf(face[0][0]) -1;
            int i1 = Integer.valueOf(face[1][0]) -1;
            int i2 = Integer.valueOf(face[2][0]) -1;

            Vector3f v0 = new Vector3f(pointsList.get(i0));
            Vector3f v1 = new Vector3f(pointsList.get(i1));
            Vector3f v2 = new Vector3f(pointsList.get(i2));

            Vector3f u = v1.sub(v0);
            Vector3f v = v2.sub(v0);


            Vector3f normal = new Vector3f();
            /* TODO: Think this can be done by JOML cross function.*/
            normal.x = (u.y * v.z) - (u.z * v.y);
            normal.y = (u.z * v.x) - (u.x * v.z);
            normal.z = (u.x * v.y) - (u.y * v.x);

            if (!vectorNormals.containsKey(i0)){
                vectorNormals.put(i0, new ArrayList<>());
            }
            vectorNormals.get(i0).add(normal.normalize());

            if (!vectorNormals.containsKey(i1)){
                vectorNormals.put(i1, new ArrayList<>());
            }
            vectorNormals.get(i1).add(normal.normalize());

            if (!vectorNormals.containsKey(i2)){
                vectorNormals.put(i2, new ArrayList<>());
            }
            vectorNormals.get(i2).add(normal.normalize());

        }
        ArrayList<Vector3f> results = new ArrayList<>();

        for (int v3i: vectorNormals.keySet()) {
            Vector3f aveVector = new Vector3f(0,0,0);
            for ( Vector3f v3x: vectorNormals.get(v3i) ) {
                aveVector = aveVector.add(v3x);
            }
            results.add(aveVector.div(vectorNormals.get(v3i).size()) );
        }
        return results;
    }
}