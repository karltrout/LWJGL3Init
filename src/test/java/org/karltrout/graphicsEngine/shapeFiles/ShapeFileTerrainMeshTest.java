package org.karltrout.graphicsEngine.shapeFiles;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.resources.geometry.XRectangle2D;
import org.junit.Before;
import org.junit.Test;
import org.karltrout.graphicsEngine.Geodesy.GeoSpacialTerrainMesh;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFile;
import org.karltrout.graphicsEngine.terrains.fltFile.FltFileReader;
import org.karltrout.graphicsEngine.terrains.fltFile.TerrainMesh;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.GL_TRUE;

/**
 * Created by karltrout on 9/8/17.
 * yeah Testing.
 */
public class ShapeFileTerrainMeshTest {
    XRectangle2D aerodromeBounds = null;
    FltFile fltFile;
    FltFileReader.FltHeader hdr;
    private FltFileReader[] fltFiles;
    private int height = 460;
    private int width = 600;
    private long id;

    private String title = "testing";
    private boolean resized = false;
    private boolean vSync = true;

    @Before
    public void setUp() throws Exception {

        Path statesShape = Paths.get("resources/shapeFiles/KPHX/aerodrome.shp");
        File file = statesShape.toFile();
        ShapeFileReader reader = new ShapeFileReader(file);

        aerodromeBounds = reader.getBoundingBox();

        Path pathToFltHdr112 = Paths.get("resources/models/terrainModels/n34w112.hdr");
        Path pathToFltFile112 = Paths.get("resources/models/terrainModels/n34w112.flt");
        FltFileReader fltFileReader112 = FltFileReader.loadFltFile(pathToFltFile112, pathToFltHdr112);


        Path pathToFltHdr113 = Paths.get("resources/models/terrainModels/n34w113.hdr");
        Path pathToFltFile113 = Paths.get("resources/models/terrainModels/n34w113.flt");
        FltFileReader fltFileReader113 = FltFileReader.loadFltFile(pathToFltFile113, pathToFltHdr113);

        fltFiles = new FltFileReader[2];
        fltFiles[0] = fltFileReader113;
        fltFiles[1] = fltFileReader112;
        if (!glfwInit()) {
            throw new IllegalStateException(" Could Not init GLFW system. exiting.");
        }
        /**
         * Bare minimum for etting up OpenGL GLFW Windows
         * this here is needed for testing Textures.
         */

/*
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        }
 */
        //size up the initial window
       id = glfwCreateWindow(width, height, title, 0, 0);
        if (id == 0) {
            throw new RuntimeException(" Could Not init GLFW window. exiting.");
        }
        glfwMakeContextCurrent(id);
        glfwShowWindow(id);
        GL.createCapabilities();
        System.out.println("Completed Window Initialization.");
    }

    @Test
    public void ShapeFileTerrain() throws Exception {

        assertNotNull(aerodromeBounds);
        assertNotNull(fltFiles);
        assertThat(fltFiles.length, is(greaterThan(0)));



        GeoSpacialTerrainMesh terrainMesh = new GeoSpacialTerrainMesh(fltFiles, aerodromeBounds, "kphxAeroDromeMasked.png", 1);

        System.out.println(aerodromeBounds);
        System.out.println("W: "+aerodromeBounds.getWidth()+",H: "+aerodromeBounds.getHeight());

        terrainMesh.buildMesh();

    }
}