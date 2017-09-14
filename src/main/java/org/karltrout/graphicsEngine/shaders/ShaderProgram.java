package org.karltrout.graphicsEngine.shaders;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.karltrout.graphicsEngine.models.DirectionalLight;
import org.karltrout.graphicsEngine.models.Material;
import org.karltrout.graphicsEngine.models.PointLight;
import org.lwjgl.opengl.GL11;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;


public abstract class ShaderProgram {
	
	private static int programID;
	private int vertexShaderID;
	private int fragmentShaderID;

    private final Map<String, Integer> uniforms;

	public ShaderProgram() throws Exception {

	    programID = glCreateProgram();

	    if (programID == 0){
	        throw new Exception("could not create Shader Program.");
        }

        uniforms = new HashMap<String,Integer>();
    }

	/**
	 * Used to bind the program based on the ProgramId
	 */
	public void start(){
		glUseProgram(programID);
	}

	/**
	 * UnBinds the program
	 */
	public void stop(){
		glUseProgram(0);
	}

	public void cleanUp(){

		stop();
		glDetachShader(programID, vertexShaderID);
		glDetachShader(programID, fragmentShaderID);
		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);
		glDeleteProgram(programID);

	}

	protected abstract void bindAttributes();

	protected void bindAttribute(int attribute, String variableName){
		glBindAttribLocation(programID, attribute, variableName);
	}


    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(programID,
                uniformName);
        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform:" +
                    uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

	public void setUniform(String uniformName, float value) {
		glUniform1f(uniforms.get(uniformName), value);
	}

	public void setUniform(String uniformName, Vector3f value) {
		glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
	}

	public void setUniform(String uniformName, Vector4f value) {
		glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
	}



	protected void loadVector(int location, Vector3f vector){
	    glUniform3f(location, vector.x, vector.y,vector.z);
    }

    protected void loadBoolean(int location, boolean value){
	    float toLoad = 0;
	    if(value){
	        toLoad = 1;
        }
        glUniform1f(location, toLoad);
    }

    protected void loadInt(int location, int value){

        glUniform1i(location, value);

    }

	public void setUniform(String uniformName, int value) {
		glUniform1i(uniforms.get(uniformName), value);
	}

	protected void loadFloat(int location, float value){
	    glUniform1f(location, value);
    }

    protected void load2DVector(int location, Vector2f value){
        glUniform2f(location, value.x, value.y);
    }

    protected void loadVertexShader(String filePath) throws Exception {
        this.vertexShaderID = loadShader(filePath, GL_VERTEX_SHADER);
    }

    protected  void loadFragmentShader(String filePath) throws Exception {
        this.fragmentShaderID = loadShader(filePath, GL_FRAGMENT_SHADER);
    }

    protected void loadTessellationControlShader(String filePath) throws Exception {
    	loadShader(filePath, GL40.GL_TESS_CONTROL_SHADER);
	}

	protected void loadTessellationEvalShader(String filePath) throws Exception {
		loadShader(filePath, GL40.GL_TESS_EVALUATION_SHADER);
	}

	private static int loadShader(String filePath, int type) throws Exception {

		StringBuilder shaderSource = new StringBuilder();

		try{

			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			while((line = reader.readLine())!=null){
				shaderSource.append(line).append("//\n");
			}
			reader.close();

		}catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}

		int shaderID = glCreateShader(type);

		if(shaderID == 0){
		    throw new Exception("Failed to load the Shader file: "+filePath);
        }

		glShaderSource(shaderID, shaderSource);
		glCompileShader(shaderID);

		if(glGetShaderi(shaderID, GL_COMPILE_STATUS )== GL11.GL_FALSE){

			System.out.println(glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader!");
			System.exit(-1);

		}

		glAttachShader(programID, shaderID);

		return shaderID;

	}

	public void link() throws Exception {
        glLinkProgram(programID);

        if(glGetProgrami(programID, GL_LINK_STATUS) == 0 ){
            throw new Exception("Could not Link Program. Info: "+glGetProgramInfoLog(programID, 1024));
        }

        if (vertexShaderID != 0) {
            glDetachShader(programID, vertexShaderID);
        }

        if(fragmentShaderID != 0){
            glDetachShader(programID, fragmentShaderID);
        }

        glValidateProgram(programID);

        if(glGetProgrami(programID, GL_LINK_STATUS) == 0 ){
            System.err.println("Warning validating program Shader code: "+glGetProgramInfoLog(programID, 1024));
        }

    }

    protected int getUniformLocation(String uniformName){
        return glGetUniformLocation(programID, uniformName);
    }

    public void loadMatrix(int location, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }

	public void createPointLightUniform(String uniformName) throws Exception {
		createUniform(uniformName + ".color");
		createUniform(uniformName + ".position");
		createUniform(uniformName + ".intensity");
		createUniform(uniformName + ".att.constant");
		createUniform(uniformName + ".att.linear");
		createUniform(uniformName + ".att.exponent");
	}

	public void createMaterialUniform(String uniformName) throws Exception {
		createUniform(uniformName + ".ambient");
		createUniform(uniformName + ".diffuse");
		createUniform(uniformName + ".specular");
		createUniform(uniformName + ".hasTexture");
		createUniform(uniformName + ".reflectance");
	}

    public void createDirectionalLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }


    public void setUniform(String uniformName, PointLight pointLight) {
		setUniform(uniformName + ".color", pointLight.getColor() );
		setUniform(uniformName + ".position", pointLight.getPosition());
		setUniform(uniformName + ".intensity", pointLight.getIntensity());
		PointLight.Attenuation att = pointLight.getAttenuation();
		setUniform(uniformName + ".att.constant", att.getConstant());
		setUniform(uniformName + ".att.linear", att.getLinear());
		setUniform(uniformName + ".att.exponent", att.getExponent());
	}


    public void setUniform(String uniformName, DirectionalLight dirLight) {
        setUniform(uniformName + ".color", dirLight.getColor());
        setUniform(uniformName + ".direction", dirLight.getDirection());
        setUniform(uniformName + ".intensity", dirLight.getIntensity());
    }

	public void setUniform(String uniformName, Material material) {
		setUniform(uniformName + ".ambient", material.getAmbientColour());
		setUniform(uniformName + ".diffuse", material.getDiffuseColour());
		setUniform(uniformName + ".specular", material.getSpecularColour());
		setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
		setUniform(uniformName + ".reflectance", material.getReflectance());
	}
}
