package org.karltrout.graphicsEngine.shaders;

/**
 * Created by karltrout on 9/24/17.
 */
public class HudShader extends ShaderProgram {

    private static final String VERTEX_FILE = "resources/shaderFiles/vertex/hudShader.vert";
    private static final String FRAGMENT_FILE = "resources/shaderFiles/fragment/hudShader.frag";

    public HudShader() throws Exception {
        super();
        loadVertexShader(VERTEX_FILE);
        loadFragmentShader(FRAGMENT_FILE);
        link();

// Create uniforms for Ortographic-model projection matrix and base colour
        createUniform("projModelMatrix");
        createUniform("colour");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }
}
