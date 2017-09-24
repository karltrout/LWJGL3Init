package org.karltrout.graphicsEngine;

import org.karltrout.graphicsEngine.models.RawModel;
import org.karltrout.graphicsEngine.textures.TextureData;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.newdawn.slick.opengl.PNGDecoder;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryStack.stackPush;

public class OpenGLLoader {
	
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
    private List<Integer> textures = new ArrayList<Integer>();


    public int createEmptyVBO(int floatCount){
        int vbo = GL15.glGenBuffers();
        vbos.add(vbo);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount * 4, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    public void updateVbo(int vbo, float[] data, FloatBuffer buffer){

        buffer.clear();
        buffer.put(data);
        buffer.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);



    }

    public void addInstanceAttribute(int vao, int vbo, int attribute, int dataSize, int     instanceDataLength, int offset){
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL30.glBindVertexArray(vao);
        GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false, instanceDataLength * 4, offset * 4);

        GL33.glVertexAttribDivisor(attribute, 1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

    }

	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices){

	    int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0,3,positions);
        storeDataInAttributeList(1,2,textureCoords);
        storeDataInAttributeList(2,3,normals);
		unbindVAO();

		return new RawModel(vaoID,indices.length);

	}


    public RawModel loadToVAO(float[] positions,int dimentions){

        int vaoID = createVAO();
        storeDataInAttributeList(0,dimentions,positions);
        unbindVAO();

        return new RawModel(vaoID, positions.length/dimentions);

    }

	public int loadTexture(String fileName){

        Texture texture = null;

        try {

            texture = TextureLoader.getTexture("PNG", new FileInputStream("resources/models/"+fileName+".png"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        int textureID = texture.getTextureID();
        textures.add(textureID);

        return textureID;

    }
	
	public void cleanUp(){

		for(int vao:vaos){
			GL30.glDeleteVertexArrays(vao);
		}

		for(int vbo:vbos){
			GL15.glDeleteBuffers(vbo);
		}

		for(int texture: textures){
		    GL11.glDeleteTextures(texture);
        }
	}
	
	private int createVAO(){

		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;

	}
	
	private void storeDataInAttributeList(int attributeNumber,int coodianteSize, float[] data){

		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber,coodianteSize, GL11.GL_FLOAT,false,0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

	}
	
	private void unbindVAO(){
		GL30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices){

	    int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

	}
	
	private IntBuffer storeDataInIntBuffer(int[] data){

	    IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;

	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data){

	    FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	public int loadCubeMap(String[] textureFiles){
	    int texID = GL11.glGenTextures();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);

        for (int i =0; i< textureFiles.length; i++){
            TextureData data = decodeTextureFile("src/main/resources/images/"+textureFiles[i]+".png");

            //GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());

        }

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        textures.add(texID);

        return texID;

    }
    public static TextureData decodeTextureFile(Path path) throws IOException {
        int width;
        int height;
        System.out.println("--->>>>> BEFORE  <<<<<<----");
        try ( MemoryStack stack = stackPush() ) {

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            if (path != null) {
                ByteBuffer image = stbi_load(path.toString(), w, h, comp, 4);
                if (image == null) {
                    throw new RuntimeException("Failed to load a texture file!"
                            + System.lineSeparator() + stbi_failure_reason());
                }

               // System.out.println("Size of Image Buffer data -> width:"+w.get()+" height: "+h.get());
                System.out.println("--->>>>> AFTER  <<<<<<----");
                return new TextureData(image, w.get(), h.get());

            }

        }
       System.out.println("Size of Image Buffer data: ");
        return null;
    }


    public static BufferedImage getBufferedImage(Image img){
        if( img == null ) return null;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        // draw original image to thumbnail image object and
        // scale it to the new size on-the-fly
        BufferedImage bufimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, w, h, null);
        g2.dispose();
        return bufimg;
    }

	public static TextureData decodeTextureFile(String fileName){
	    int width = 0;
	    int height = 0;
        ByteBuffer buffer = null;
        try {


            FileInputStream in = new FileInputStream(fileName);
            PNGDecoder decoder = new PNGDecoder(in);
            width = decoder.getWidth();
            height = decoder.getHeight();

            buffer = ByteBuffer.allocateDirect(4*width*height);
            decoder.decode(buffer, width * 4, PNGDecoder.RGBA);
            buffer.flip();
            in.close();

        }  catch (IOException e) {
            System.err.println("failed to load textures For " + fileName);
            e.printStackTrace();
        }

        return new TextureData(buffer, width, height);
    }

}
