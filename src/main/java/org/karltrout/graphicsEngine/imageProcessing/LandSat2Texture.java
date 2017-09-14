package org.karltrout.graphicsEngine.imageProcessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.data.WorldFileReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.geotools.resources.geometry.XRectangle2D;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.geotools.metadata.iso.citation.ContactImpl.EPSG;

/**
 * Created by karltrout on 9/9/17.
 *
 */
public class LandSat2Texture {

    private final String outFile = "n34w112ls";
    private static Logger logger = LogManager.getLogger(LandSat2Texture.class);

    public LandSat2Texture() {

        logger.info("Starting LandSat8 to Teture processing.");

        String kphxLocalImg = "resources/models/terrainModels/LC08_L1TP_037037_20170901_20170901_01_RT.png";
        File landSatImg = new File(kphxLocalImg);

        BufferedImage lsImg = null;
        try {
            lsImg = ImageIO.read(landSatImg);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lsImg == null){
            logger.error("Could not load the image located at "+kphxLocalImg);
            System.exit(0);
        }

        int lsHeight = lsImg.getHeight();
        int lsWidth  = lsImg.getWidth();

        logger.info("LandSat 8 image data...");
        logger.info("Width: "+lsWidth);
        logger.info("Height: "+lsHeight);

        Vector2f[] LatLongVectors = new Vector2f[4];
        LatLongVectors[0] = new Vector2f(34.21825f,-113.79947f );
        LatLongVectors[1] = new Vector2f(34.24991f,-111.27586f );
        LatLongVectors[2] = new Vector2f(32.08567f,-113.73248f );
        LatLongVectors[3] = new Vector2f(32.11487f,-111.26925f );

        XRectangle2D lsBB = calculateBoundingBox(LatLongVectors);

        LatLongVectors[0] = new Vector2f(34.0f,-112.0f );
        LatLongVectors[1] = new Vector2f(34.0f,-111.0f );
        LatLongVectors[2] = new Vector2f(33.0f,-112.0f );
        LatLongVectors[3] = new Vector2f(33.0f,-111.0f );

        XRectangle2D llBB = calculateBoundingBox(LatLongVectors);

        if (lsBB.intersects(llBB)){
            logger.info("LandSat Bounding Box intersects LatitudeLongitude BoundingBox");
        }
        /* LandSat data
         */
        int pixelsPerLatitude = (int)(Math.round(lsHeight/lsBB.getHeight()));
        int pixelsPerLongitude = (int)(Math.round(lsWidth/lsBB.getWidth()));

        int x = (int) (Math.abs(  Math.abs(llBB.getMinX()) -  Math.abs(lsBB.getMinX()) ) * pixelsPerLongitude);
        int y = (int) (Math.abs(  Math.abs(lsBB.getMaxY()) -  Math.abs(llBB.getMaxY()) ) * pixelsPerLatitude);

        logger.info("LandSat 8 latitude size: "+ lsBB.getHeight());
        logger.info("LandSat 8 Longitude size: "+ lsBB.getWidth());
        logger.info("LandSat 8 pixels per Latitude: "+pixelsPerLatitude);
        logger.info("LandSat 8 pixels per Longitude: "+pixelsPerLongitude);
        logger.info("X: "+x);
        logger.info("Y: "+y);

        int availablePixelsPerLatitude = (y+pixelsPerLatitude > lsImg.getHeight())? lsImg.getHeight() - y : pixelsPerLatitude;
        int availablePixelsPerLongitude = (x+pixelsPerLongitude > lsImg.getWidth())? lsImg.getWidth() - x : pixelsPerLongitude;

        BufferedImage llImg = lsImg.getSubimage(x,y,availablePixelsPerLongitude,availablePixelsPerLatitude);

        int imgWidth = llImg.getWidth();
        int imgHeight = llImg.getHeight();
        if(imgWidth < pixelsPerLongitude || imgHeight < pixelsPerLatitude){

            logger.info("LandSat 8 Image is smaller then the Latitude or Longitude space.");
            BufferedImage newImage = new BufferedImage(pixelsPerLongitude, pixelsPerLatitude,llImg.getType());
            Graphics2D g2 = newImage.createGraphics();
            Color oldColor = g2.getColor();
            //fill background
            g2.setPaint(Color.BLACK);
            g2.fillRect(0, 0, pixelsPerLongitude, pixelsPerLatitude);
            //draw image
            g2.setColor(oldColor);
            g2.drawImage(llImg, null, 0, 0);

            g2.dispose();
            llImg = newImage;
        }

        logger.info("results: img height: "+llImg.getHeight());
        logger.info("results: img Width: "+llImg.getWidth());
        File outputfile = new File("resources/models/terrainModels/"+outFile+".png");
        try {
            ImageIO.write(llImg, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

       // LandSat2Texture landSat2Texture = new LandSat2Texture();
       File tifFile = new File("resources/models/terrainModels/LC08_L1TP_037037_20170901_20170901_01_RT.tif");

        try {
            LandSat2Texture.readGeoTifFile(tifFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            e.printStackTrace();
        }

    }

    private XRectangle2D calculateBoundingBox(Vector2f[] latlong) {

        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;

        for (Vector2f v : latlong) {
            maxY = (maxY > v.x )? maxY : v.x;
            maxX = (maxX > v.y)? maxX : v.y;
            minY = (minY < v.x)? minY : v.x;
            minX = (minX < v.y)? minX: v.y;
        }

        return XRectangle2D.createFromExtremums( minX, minY, maxX, maxY);
    }

    private static void readGeoTifFile(File geoTiff) throws IOException, FactoryException, TransformException {

       /* String parentPath = geoTiff.getParent();
        String filename = geoTiff.getName();
        final int i = filename.lastIndexOf('.');
        filename = (i == -1) ? filename : filename.substring(0, i);

        // getting name and extension
        final String base = (parentPath != null) ? new StringBuilder(
                parentPath).append(File.separator).append(filename)
                .toString() : filename;

        // We can now construct the baseURL from this string.
        File geoTiffWorldFile = new File(new StringBuilder(base).append(".tfw")
                .toString());
        */

        AbstractGridFormat format = GridFormatFinder.findFormat( geoTiff );
        GridCoverage2DReader geoTiffReader = format.getReader( geoTiff );
        /*AbstractGridCoverage2DReader geoTiffReader;
        if (geoTiffWorldFile.exists()) {
            geoTiffReader = new WorldImageReader(geoTiff);
        } else {
            geoTiffReader = new GeoTiffReader(geoTiff);
        }
        */
        GridCoverage2D coverage = (GridCoverage2D) geoTiffReader.read(null);
        CoordinateReferenceSystem imgCrs = coverage.getCoordinateReferenceSystem2D();
        Envelope env = coverage.getEnvelope();
        PlanarImage image = (PlanarImage) coverage.getRenderedImage();
        logger.info("Image Size : w:"+image.getWidth()+" h:"+image.getHeight());

        CoordinateReferenceSystem epsgCrs = CRS.decode("EPSG:4326");

        double[] utmUpperCorner = env.getUpperCorner().getCoordinate();
        double[] utmLowerCorner = env.getLowerCorner().getCoordinate();
        double[] latlonUpperCorner = new double[2];
        double[] latlonLowerCorner = new double[2];

        MathTransform transform = CRS.findMathTransform(imgCrs, epsgCrs);

        transform.transform(utmUpperCorner,0,latlonUpperCorner,0,1);
        logger.info("Upper Corner -> latitude: "+latlonUpperCorner[0]+" longitude: "+latlonUpperCorner[1]);
        transform.transform(utmLowerCorner,0,latlonLowerCorner,0,1);
        logger.info("Lower Corner -> latitude: "+latlonLowerCorner[0]+" longitude: "+latlonLowerCorner[1]);

        /* note: upper corner is top right, lower corner is bottom left*/
        ArrayList<Vector4f> latLongGrids = generateLatLongGrids(latlonUpperCorner, latlonLowerCorner);

        for (Vector4f grid : latLongGrids) {
            generateImage(grid, latlonUpperCorner, latlonLowerCorner, image);
        }

    }

    private static void generateImage(Vector4f grid, double[] latlonUpperCorner, double[] latlonLowerCorner, PlanarImage image) {

        double imgLatHeight = Math.abs(latlonUpperCorner[0] - latlonLowerCorner[0]);
        double imgLongWidth = Math.abs(latlonUpperCorner[1] - latlonLowerCorner[1]);
        int pxPerLat  = (int)Math.round(image.getHeight() / imgLatHeight);
        int pxPerLong = (int)Math.round(image.getWidth()  / imgLongWidth);
        /*
        logger.info("image grids height: "+pxPerLat+" width: "+pxPerLong);
        logger.info("preparing image for latitude: "+grid.x+" to: "+grid.z+", longitude: "+grid.y+" to: "+grid.w);
        logger.info("upper lat: "+latlonUpperCorner[0]+" long: "+latlonUpperCorner[1]);
        logger.info("lower lat: "+latlonLowerCorner[0]+" long: "+latlonLowerCorner[1]);
        */
        int x, y, w, h;
        int ySign = 1;
        int xSign = 1;

        // latitude differences
        if( grid.x >= latlonUpperCorner[0] ){
            y = 0;
            h = (int)Math.round(( latlonUpperCorner[0] - grid.z ) * pxPerLat);
        }
        else {
            y = (int)Math.round(( latlonUpperCorner[0] - grid.x ) * pxPerLat);
            if( grid.z >= latlonLowerCorner[0] ){
                h = pxPerLat;
            }else {
                h = (int)Math.round(( grid.x - latlonLowerCorner[0]  ) * pxPerLat);
                ySign = -1;
            }
        }

        //longitude differences
        if (  latlonUpperCorner[1] <= grid.y ){
            x = (int)Math.round(Math.abs( latlonLowerCorner[1] - grid.w ) * pxPerLong);
            w = (int)Math.round(Math.abs( grid.w  - latlonUpperCorner[1] ) * pxPerLong);
            xSign = 0;
        }
        else {
            if ( grid.w <= latlonLowerCorner[1] ){
                x = 0;
                w = (int)Math.round(Math.abs( latlonLowerCorner[1] - grid.y ) *pxPerLong);
            } else {
                x = (int)Math.round(Math.abs( latlonLowerCorner[1] - grid.w ) * pxPerLong);
                w = pxPerLong;
            }


        }

        StringBuffer imgFileName = new StringBuffer();
          imgFileName.append((grid.x > 0)?"n":"s").append(Math.abs((int)grid.x));
          imgFileName.append((grid.y < 0)?"w":"e").append(Math.abs((int)grid.w));
          imgFileName.append("_ls8");

        if ( y+h > image.getHeight() )
            h = image.getHeight() - y;
        if ( x+w > image.getWidth() )
            w = image.getWidth() - x;

        logger.info("Image coordinates for img: "+imgFileName+" are:");
          logger.info("x: "+x+" y: "+y+" w: "+w+" h: "+h);

        BufferedImage buffImg = image.getAsBufferedImage();

        BufferedImage llimg =buffImg.getSubimage(x,y,w,h);

        writeImage(imgFileName.toString(), llimg, pxPerLat, pxPerLong, (pxPerLong - w)*xSign, (pxPerLat - h)*ySign);

    }

    private static ArrayList<Vector4f> generateLatLongGrids(double[] latlonUpperCorner, double[] latlonLowerCorner) {

        float upperLatitude  = (latlonUpperCorner[0] > 0) ?
                (float)Math.ceil(latlonUpperCorner[0]) : (float)Math.floor(latlonUpperCorner[0]);
        logger.info("upper Latitude: "+upperLatitude);

        float upperLongitude = (latlonUpperCorner[1] > 0) ?
                (float)Math.floor(latlonUpperCorner[1]) : (float)Math.ceil(latlonUpperCorner[1]);
        logger.info("upper Longitude: "+upperLongitude);

        float lowerLatitude =  (latlonLowerCorner[0] > 0) ?
                (float)Math.floor(latlonLowerCorner[0]): (float)Math.ceil(latlonLowerCorner[0]);
        logger.info("Lower Latitude: "+lowerLatitude);

        float lowerLongitude = (latlonLowerCorner[1] > 0) ?
                (float)Math.floor(latlonLowerCorner[1]): (float)Math.floor(latlonLowerCorner[1]);
        logger.info("Lower Longitude: "+lowerLongitude);


        float numLatitudes = Math.abs(lowerLatitude - upperLatitude);
        float numLongitudes = Math.abs(Math.abs(upperLongitude) - Math.abs(lowerLongitude));

        logger.info("Number of Latitudes: "+numLatitudes+" Longitudes: "+numLongitudes);

        ArrayList<Vector4f> grid = new ArrayList<>();
        for (int i = 0; i < numLatitudes; i++) {
            for (int j = 0; j < numLongitudes; j++) {
                grid.add(new Vector4f(upperLatitude-i, upperLongitude-j, upperLatitude-i-1, upperLongitude-j-1));
            }
        }
        return grid;
    }

    private static void writeImage(
            String outFile, BufferedImage llImg,
            int pixelsPerLatitude, int pixelsPerLongitude, int x, int y){

        int imgWidth = llImg.getWidth();
        int imgHeight = llImg.getHeight();
        if(imgWidth < pixelsPerLongitude || imgHeight < pixelsPerLatitude){

            logger.info("LandSat 8 Image is smaller then the Latitude or Longitude space.");
            BufferedImage newImage = new BufferedImage(pixelsPerLongitude, pixelsPerLatitude,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = newImage.createGraphics();
            Color oldColor = g2.getColor();
            //fill background
            g2.setPaint(Color.BLACK);
            g2.fillRect(0, 0, pixelsPerLongitude, pixelsPerLatitude);
            //draw image
            g2.setColor(oldColor);
            logger.info("filling h:"+pixelsPerLatitude+" w: "+pixelsPerLongitude+" at x: "+x+" y:"+y);
            g2.drawImage(llImg, null, x, y);

            g2.dispose();
            llImg = newImage;
        }

        logger.info("results: img height: "+llImg.getHeight());
        logger.info("results: img Width: "+llImg.getWidth());
        File outputfile = new File("resources/models/terrainModels/"+outFile+".png");
        try {
            ImageIO.write(llImg, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MathTransform  readWorldFile(File tiffFile){
        MathTransform raster2Model = null;
        String parentPath = tiffFile.getParent();
        String filename = tiffFile.getName();
        final int i = filename.lastIndexOf('.');
        filename = (i == -1) ? filename : filename.substring(0, i);

        // getting name and extension
        final String base = (parentPath != null) ? new StringBuilder(
                parentPath).append(File.separator).append(filename)
                .toString() : filename;

        // We can now construct the baseURL from this string.
        File file2Parse = new File(new StringBuilder(base).append(".tfw")
                .toString());

        if (file2Parse.exists()) {
            // parse world file
            final WorldFileReader reader;
            try {

                reader = new WorldFileReader(file2Parse);
                raster2Model = reader.getTransform();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return raster2Model;
    }
}
