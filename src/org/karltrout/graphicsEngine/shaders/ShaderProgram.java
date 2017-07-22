package org.karltrout.graphicsEngine.shaders;

import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;


public abstract class ShaderProgram {
	
	private static int programID;
	private int vertexShaderID;
	private int fragmentShaderID;



	public ShaderProgram() throws Exception {

	    programID = glCreateProgram();

	    if (programID == 0){
	        throw new Exception("could not create Shader Program.");
        }

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

    protected  abstract void getAllUniformLocations();

    protected int getUniformLocation(String uniformName){
        return glGetUniformLocation(programID, uniformName);
    }

}
