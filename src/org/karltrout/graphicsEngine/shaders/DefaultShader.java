package org.karltrout.graphicsEngine.shaders;


import org.joml.Matrix4f;

public class DefaultShader extends ShaderProgram{
	private static final String VERTEX_FILE = "resources/shaderFiles/vertex/simpleShader.vert";
	private static final String FRAGMENT_FILE = "resources/shaderFiles/fragment/simpleShader.frag";

	private int location_transformationMatrix;
	private int location_projectionMatrix;


	public DefaultShader() throws Exception {
		super();
		loadVertexShader(VERTEX_FILE);
		loadFragmentShader(FRAGMENT_FILE);
		link();
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");

	}

	@Override
	public void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
	}

	//public void  loadTransformationMatrix(Matrix4f matrix){
	//	super.loadMatrix(location_transformationMatrix, matrix);
	//}

	//public void loadProjectionMatrix(Matrix4f matrix){
	 //   super.loadMatrix(location_projectionMatrix, matrix);
    //}


}
