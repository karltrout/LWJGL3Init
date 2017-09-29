package org.karltrout.graphicsEngine.shaders;

public class DefaultShader extends ShaderProgram{

	private static final String VERTEX_FILE = "resources/shaderFiles/vertex/simpleShader.vert";
	private static final String FRAGMENT_FILE = "resources/shaderFiles/fragment/simpleShader.frag";

	public DefaultShader() throws Exception {
		super();
		loadVertexShader(VERTEX_FILE);
		loadFragmentShader(FRAGMENT_FILE);
		link();
	}

}
