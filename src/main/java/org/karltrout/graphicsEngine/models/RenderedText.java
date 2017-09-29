package org.karltrout.graphicsEngine.models;

import org.joml.Vector4f;
import org.karltrout.graphicsEngine.OpenGLLoader;
import org.karltrout.graphicsEngine.textures.TextureData;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.karltrout.graphicsEngine.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengles.GLES20.GL_TEXTURE0;
import static org.lwjgl.opengles.GLES20.glGenerateMipmap;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Created by karltrout on 9/24/17.
 * Attempting to put up truetype text in the display.
 */
public class RenderedText implements Renderable {

    private String text;
    private final Material material;

    private final ByteBuffer ttf;

    private final STBTTFontinfo info;

    private final int ascent;
    private final int descent;
    private final int lineGap;

    private final int lineCount;

    private int fontHeight;

    private int   scale;
    private int   lineOffset;
    private float lineHeight;

    private boolean kerningEnabled = true;
    private boolean lineBBEnabled = false;
    private STBTTBakedChar.Buffer cdata;

    int BITMAP_W = 512;
    int BITMAP_H = 512;
    private int textureID;

    private TextMesh mesh;
    private int cnt;


    public RenderedText(String text, int fontHeight) {

        this.text = text;
        this.material = new Material(new Vector4f(1.0f,1.0f,1.0f,1.0f), 1.0f);

        try {
            ttf = ioResourceToByteBuffer("resources/fonts/FiraSans.ttf", 160 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info, ttf)) {
            throw new IllegalStateException("Failed to initialize font information.");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent  = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }

        this.fontHeight = fontHeight;
        this.lineHeight = fontHeight;

        int lc = 0;

            Matcher m = Pattern.compile("^.*$", Pattern.MULTILINE).matcher(text);
            while (m.find()) {
                lc++;
            }

        lineCount = lc;


        mesh = new TextMesh(material);

    }

    @Override
    public void cleanUp() {



    }

    @Override
    public void render() {

        cnt++;
        updateText("This Is a Test: " + cnt);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);

        mesh.render();


    }

    @Override
    public boolean hasTexture() {
        return false;
    }

    @Override
    public Material getMaterial() {
        return this.material;
    }

    public TextMesh getMesh (){
        return mesh;
    }

    protected void init() {

        glActiveTexture(GL_TEXTURE0);
        textureID = glGenTextures();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);


        glBindTexture(GL_TEXTURE_2D, textureID);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

       // TTF FONT LOAD - Not Working
        cdata = STBTTBakedChar.malloc(96);

        System.out.println("Text Texture ID: "+textureID);

        ByteBuffer buffer = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        int i = stbtt_BakeFontBitmap(ttf, getFontHeight(), buffer, BITMAP_W, BITMAP_H, 32, cdata);

        TextureData t1 = new TextureData(buffer, BITMAP_W, BITMAP_H, GL_RED);
        this.textureID = t1.getId();
        System.out.println("The Texture binding T1 for PNG font is :"+t1.getId());

       TextureData textureData;
        Path textTexture = Paths.get("resources/fonts/lucidagrande.png");
        try {
             textureData = OpenGLLoader.decodeTextureFile(textTexture);
             //   this.textureID = textureData.getId();
            System.out.println("The Texture binding for PNG font is :"+textureData.getId());
           // glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, textureData.getWidth(), textureData.getHeight(), 0, GL_RED, GL_UNSIGNED_BYTE, textureData.getCoords());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createImageFile(byte[] bitmap) {

        DataBuffer buffer = new DataBufferByte(bitmap, bitmap.length);

//3 bytes per pixel: red, green, blue
        WritableRaster raster = Raster.createInterleavedRaster(buffer, 512, 512, 512, 1, new int[] {0, 1, 2}, (Point)null);
        ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage image = new BufferedImage(cm, raster, true, null);

        try {
            ImageIO.write(image, "png", new File("image.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateText(String text) {
        this.text = text;

        float scale = stbtt_ScaleForPixelHeight(info, getFontHeight());

        try (MemoryStack stack = stackPush()) {
             IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            int lineStart = 0;

            int i  = 0;
            int to = text.length();

            ArrayList<Float> verticeList = new ArrayList();
            ArrayList<Float> textureCoordList = new ArrayList();
            while (i < to) {
                i += getCP(text, to, i, pCodePoint);

                int cp = pCodePoint.get(0);
                if (cp == '\n') {
                    if (isLineBBEnabled()) {
                        glEnd();
                        renderLineBB(lineStart, i - 1, y.get(0), scale);
                        glBegin(GL_QUADS);
                    }

                    y.put(0, y.get(0) + (ascent - descent + lineGap) * scale);
                    x.put(0, 0.0f);

                    lineStart = i;
                    continue;
                } else if (cp < 32 || 128 <= cp) {
                    continue;
                }

                stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, cp - 32, x, y, q, true);
                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0)) * scale);
                }

                verticeList.add(q.x0());
                verticeList.add(q.y0());
                textureCoordList.add(q.s0());
                textureCoordList.add(q.t0());

                verticeList.add(q.x0());
                verticeList.add(q.y1());
                textureCoordList.add(q.s0());
                textureCoordList.add(q.t1());

                verticeList.add(q.x1());
                verticeList.add(q.y0());
                textureCoordList.add(q.s1());
                textureCoordList.add(q.t0());

                verticeList.add(q.x1());
                verticeList.add(q.y0());
                textureCoordList.add(q.s1());
                textureCoordList.add(q.t0());

                verticeList.add(q.x0());
                verticeList.add(q.y1());
                textureCoordList.add(q.s0());
                textureCoordList.add(q.t1());

                verticeList.add(q.x1());
                verticeList.add(q.y1());
                textureCoordList.add(q.s1());
                textureCoordList.add(q.t1());

                float[] verticeArray = new float[verticeList.size()];

                for (int j = 0; j < verticeList.size(); j++) {
                    verticeArray[j] = verticeList.get(j);
                }

                float[] textureArray = new float[textureCoordList.size()];
                for (int j = 0; j < textureCoordList.size() ; j++) {
                    textureArray[j] = textureCoordList.get(j);
                }
                mesh.updateData(verticeArray, textureArray);


            }
            
            if (isLineBBEnabled()) {
                renderLineBB(lineStart, text.length(), y.get(0), scale);
            }

        }
    }

    private void renderLineBB(int from, int to, float y, float scale) {
        glDisable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_LINE);
        glColor3f(1.0f, 1.0f, 0.0f);

        float width = getStringWidth(info, text, from, to, getFontHeight());
        y -= descent * scale;

        glBegin(GL_QUADS);
        glVertex2f(0.0f, y);
        glVertex2f(width, y);
        glVertex2f(width, y - getFontHeight());
        glVertex2f(0.0f, y - getFontHeight());
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_FILL);
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color
    }

    private float getStringWidth(STBTTFontinfo info, String text, int from, int to, int fontHeight) {
        int width = 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            int i = from;
            while (i < to) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(info, fontHeight);
    }

    private static int getCP(String text, int to, int i, IntBuffer cpOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        cpOut.put(0, c1);
        return 1;
    }

    public String getText() {
        return text;
    }

    private int getFontHeight() {
        return fontHeight;
    }

    public int getScale() {
        return scale;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    private boolean isKerningEnabled() {
        return kerningEnabled;
    }

    private boolean isLineBBEnabled() {
        return lineBBEnabled;
    }

    private int getLineCount(){ return lineCount; }

    public void setLineOffset(int lineOffset) {
        this.lineOffset = lineOffset;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public float getLineHeight() {
        return lineHeight;
    }
}
