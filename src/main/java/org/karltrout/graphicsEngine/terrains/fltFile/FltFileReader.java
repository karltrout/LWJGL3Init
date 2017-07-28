package org.karltrout.graphicsEngine.terrains.fltFile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * blah blah blah
 *
 * Created by karltrout on 5/28/17.
 */
public class FltFileReader {

    public static final float GEO_ARC_SECOND_METER_DISTANCE = 30.87f;
    public static final int GRID_SIZE_METERS = 901;

    private static Logger logger;
    public FltFile fltFile;
    public FltHeader hdr;
    private byte[] buffer;

    FltFileReader(File file, FltHeader hdr) throws FileNotFoundException {

        this.hdr = hdr;

        logger = LogManager.getLogger(this.getClass());

        if (file != null && file.exists() && file.canRead()) {
            this.fltFile = new FltFile(hdr.nrows, hdr.ncols);
            buffer = new byte[hdr.ncols * 4]; // read in one row at a time
            int row = 0;
            try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {

                while (input.read(buffer) != -1) {
                    int col = 0;
                    for (int i = 0; i < buffer.length; i = i + 4) {
                        byte[] fltBytes = Arrays.copyOfRange(buffer, i, i + 4);
                        //Data comes in little endian and java fx is y down coordenance so data must be
                        //inverted
                        fltFile.data[row][col] = (ByteBuffer.wrap(fltBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat());
                        col++;
                    }
                    row++;
                }
                logger.debug("Number of Rows: " + row);
            } catch (IOException e) {
                logger.error("Error reading file: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        } else {
            logger.error("Flt File " + file.getAbsolutePath() + " can not be read.");
        }
    }

    public static FltFileReader loadFltFile(Path pathToFltFile, Path pathToFltHdrFile) throws IOException{

        File fltFile = pathToFltFile.toFile();
        File hdrFile = pathToFltHdrFile.toFile();
        FltFileReader reader = null;
        FltHeader header = null;
        logger = LogManager.getLogger();

        if (hdrFile.exists() && hdrFile.canRead()){

            logger.info("Importing .hdr file : "+ hdrFile.getName());

            FltHeader.Builder fltBuilder = new FltHeader.Builder();

            try(Stream<String> hdrLines = Files.lines(pathToFltHdrFile)){
                hdrLines.forEach( line -> { fltBuilder.add(line); } );
            } catch (IOException e) {
                logger.error("HDR File is not readable or exists at: "+hdrFile.getAbsolutePath());
                throw e;
            }

            header = fltBuilder.build();
            logger.info("hdr loaded: "+header.toString());

        }

        if (fltFile.exists() && fltFile.canRead()){

            logger.info("Importing .FLT file : "+ fltFile.getName());

            try {

                reader = new FltFileReader(fltFile, header);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        return reader;

    }



    public final static class FltHeader {

        /*
            private static Class FLtHeader
            Example:
                ncols         10812
                nrows         10812
                xllcorner     -112.0005555556
                yllcorner     32.99944444444
                cellsize      9.2592592593e-005
                NODATA_value  -9999
                byteorder     LSBFIRST
         */

        final int ncols;
        final int nrows;
        final float xllcorner;
        final float yllcorner;
        final Double cellsize;
        final int noDataVal;
        final String byteOrder;

        public FltHeader(int cols, int rows, float xllcorner, float yllcorner, Double cellsize, int noDataVal, String byteOrder) {

            this.ncols = cols;
            this.nrows = rows;
            this.xllcorner = xllcorner;
            this.yllcorner = yllcorner;
            this.byteOrder = byteOrder;
            this.cellsize = cellsize;
            this.noDataVal = noDataVal;

        }
        @Override
        public String toString(){
            return "FltHeader -> Cols:"+ncols+" Rows:"+nrows+" X:"+xllcorner+
                    " Y:"+yllcorner+" Cell Size:"+cellsize+" NoData: "+noDataVal+" Byte Order:"+byteOrder;
        }

        static class Builder {

            private int ncols;
            private int nrows;
            private float xllcorner;
            private float yllcorner;
            private Double cellsize;
            private int noDataVal;
            private String byteOrder;
            FltHeader header;

            Builder(){
            }

            Builder add(String line) {

                String[] input = line.split("\\s+");

                if (input.length == 2) {
                    switch (input[0]) {
                        case "nrows":
                            this.nrows = Integer.valueOf(input[1]);
                            break;
                        case "ncols":
                            this.ncols = Integer.valueOf(input[1]);
                            break;
                        case "xllcorner":
                            this.xllcorner = Float.valueOf(input[1]);
                            break;
                        case "yllcorner":
                            this.yllcorner = Float.valueOf(input[1]);
                            break;
                        case "cellsize":
                            this.cellsize = Double.valueOf(input[1]);
                            break;
                        case "byteorder":
                            this.byteOrder = input[1];
                            break;
                        case "NODATA_value":
                            this.noDataVal = Integer.valueOf(input[1]);
                            break;
                    }
                }

                return this;
            }

            FltHeader build(){
                this.header = new FltHeader(nrows, ncols, xllcorner, yllcorner, cellsize, noDataVal, byteOrder);
                return this.header;
            }

        }
    }
}
