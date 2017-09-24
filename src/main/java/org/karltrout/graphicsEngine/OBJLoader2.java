package org.karltrout.graphicsEngine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.karltrout.graphicsEngine.models.RawModel;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karltrout on 6/9/17.
 */
public class OBJLoader2 {

    public static RawModel loadObjModel(String fileName, OpenGLLoader loader) throws FileNotFoundException {

        FileReader reader = null;
        Path path = Paths.get("resources/models/"+fileName+".obj");
        reader = new FileReader(path.toFile());

        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = null;
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer>  indices = new ArrayList<>();
        List<String[][]> faces = new ArrayList<>();
        float[] verticiesArray = null;
        float[] normalsArray = null;
        float[] textureArray = null;
        int[] indiciesArray = null;

        try {

            while(true){

                line = bufferedReader.readLine();

                if(line == null) break;

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


                    //faceVector[0] = new Vector3f(Float.parseFloat(vertex1[0]),Float.parseFloat(vertex1[1]),Float.parseFloat(vertex1[2]));
                    //faceVector[1] = new Vector3f(Float.parseFloat(vertex2[0]),Float.parseFloat(vertex2[1]),Float.parseFloat(vertex2[2]));
                    //faceVector[2] = new Vector3f(Float.parseFloat(vertex3[0]),Float.parseFloat(vertex3[1]),Float.parseFloat(vertex3[2]));

                    faces.add(faceVector);

                }

            }

            textureArray = new float[vertices.size() * 2];
            normalsArray = new float[vertices.size() * 3];

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        for ( String[][] face : faces )
        {
            processVertex(face[0], indices, textures, normals, textureArray, normalsArray);
            processVertex(face[1], indices, textures, normals, textureArray, normalsArray);
            processVertex(face[2], indices, textures, normals, textureArray, normalsArray);

        }

        verticiesArray = new float[ vertices.size() * 3];
        indiciesArray = new int[indices.size()];

        int vertexPonter = 0;
        for (Vector3f vertex:vertices){

            verticiesArray[vertexPonter++] = vertex.x;
            verticiesArray[vertexPonter++] = vertex.y;
            verticiesArray[vertexPonter++] = vertex.z;

        }

        for(int i=0; i< indices.size(); i++){

            indiciesArray[i] = indices.get(i);

        }

        return loader.loadToVAO(verticiesArray, textureArray, normalsArray, indiciesArray);

    }

    private static void processVertex(
            String[] vertexData, List<Integer>indecies,
            List<Vector2f> textures, List<Vector3f> normals,
            float[] textureArray, float[] normalsArray) {

        int currentVertexPointer = Integer.parseInt(vertexData[0]) -1;
        indecies.add(currentVertexPointer);
        if (textures.size() > 0) {
            Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
            textureArray[currentVertexPointer * 2] = currentTex.x;
            textureArray[currentVertexPointer * 2 + 1] = 1 - currentTex.y;
        }
        else{
            textureArray[currentVertexPointer * 2] =1;
            textureArray[currentVertexPointer * 2 + 1] = 1;

        }
        if (normals.size() > 0 ) {
            Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
            normalsArray[currentVertexPointer * 3] = currentNorm.x;
            normalsArray[currentVertexPointer * 3 + 1] = currentNorm.y;
            normalsArray[currentVertexPointer * 3 + 2] = currentNorm.z;
        }
        else{
            normalsArray[currentVertexPointer * 3] =0 ;
            normalsArray[currentVertexPointer * 3 + 1] = 0 ;
            normalsArray[currentVertexPointer * 3 + 2] =0;

        }
    }
}