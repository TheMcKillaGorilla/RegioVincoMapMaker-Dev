package rvmm.files.shp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author McKillaGorilla
 */
public class SHPToJSONConverter {

    // THESE ARE ALL THE DIFFERENT TYPES OF SHAPES THAT MAY BE FOUND
    // INSIDE A SHAPEFILE, NOTE THAT WE ONLY CARE ABOUT, AND HAVE ONLY
    // IMPLEMENTED, POLYGON TYPES. THESE ARE THE ONLY ONES USED BY
    // OUR MAPS.
    public static final int NULL_SHAPE = 0;
    public static final int POINT = 1;
    public static final int POLYLINE = 3;
    public static final int POLYGON = 5;
    public static final int MULTIPOINT = 8;
    public static final int POINTZ = 11;
    public static final int POLYLINEZ = 13;
    public static final int POLYGONZ = 15;
    public static final int MULTIPOINTZ = 18;
    public static final int POINTM = 21;
    public static final int POLYLINEM = 23;
    public static final int POLYGONM = 25;
    public static final int MULTIPOINTM = 28;
    public static final int MULTIPATCH = 31;

    // THIS IS A SILLY UTILITY VARIABLE USED FOR MASKING
    private long[] longMasks;

    /**
     * This constructor only initializes the mask. This loader class is used
     * more for functionality than for storing data.
     */
    public SHPToJSONConverter() {
        longMasks = initLongMasks();
    }

    /**
     * * UTILITY FUNCTIONS FOR LOADING DATA AND SWITCHING ENDIANNESS **
     */
    /**
     * This method generates a mask for each bit to help switch endianness.
     */
    private long[] initLongMasks() {
        long[] masks = new long[8];
        long initNum = 255;
        for (int i = 0; i < 8; i++) {
            masks[i] = initNum << (i * 8);
        }
        return masks;
    }

    public SHPData loadData(String filePath) throws IOException {
        File shapeFile = new File(filePath);
        SHPData shpData = loadShapefileData(shapeFile);
        return shpData;
    }

    /**
     * This method loads all the shapefile data found in shapeFile into an
     * SHPData object that is then returned. Note again, that we are only using
     * polygon types.
     */
    private SHPData loadShapefileData(File shapeFile) throws IOException {
        // THIS IS WHERE WE'LL LOAD THE DATA INTO
        SHPData shapefileData = new SHPData();

        // THIS IS THE FILE WE'LL READ FROM
        FileInputStream fis = new FileInputStream(shapeFile);

        // LET'S QUICKLY LOAD ALL THE BYTES TO
        // SPEED EVERYTHING UP
        byte[] bytes = new byte[Long.valueOf(shapeFile.length()).intValue()];
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
System.out.println("Reading bytes from " + shapeFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(bytes);
        bis.close();

        DataInputStream dis;
        if (shapeFile.length() <= Integer.MAX_VALUE) {
            dis = new DataInputStream(bais);
        } else {
            dis = new DataInputStream(fis);
        }

        // GET THE FILE CODE, IT SHOULD BE 0x0000270a
        int fileCode = dis.readInt();
        String hex = Integer.toHexString(fileCode);
        shapefileData.setFileCode(fileCode);

        // NOW 5 UNUSED INTS (20 BYTES)
        int[] unusedBytes = new int[5];
        for (int i = 0; i < 5; i++) {
            unusedBytes[i] = dis.readInt();
        }
        shapefileData.setUnusedBytes(unusedBytes);

        // FILE LENGTH
        int fileLength = dis.readInt();
        shapefileData.setFileLength(fileLength);

        // VERSION
        int version = readLittleEndianInt(dis);
        shapefileData.setVersion(version);

        // SHAPE TYPE
        int shapeType = readLittleEndianInt(dis);
        shapefileData.setShapeType(shapeType);

        // MBR - Minimum Bounding Rectangle
        double[] mbr = extract2DBoundingBox(dis);
        shapefileData.setMBR(mbr);

        // Z BOUNDS
        double[] zBounds = new double[2];
        zBounds[0] = readLittleEndianDouble(dis);
        zBounds[1] = readLittleEndianDouble(dis);
        shapefileData.setZBounds(zBounds);

        // M BOUNDS
        double[] mBounds = new double[2];
        mBounds[0] = readLittleEndianDouble(dis);
        mBounds[1] = readLittleEndianDouble(dis);
        shapefileData.setMBounds(mBounds);

        // FILE LENGTH IS IN 16-BIT WORDS, WE'LL CONVERT
        // IT TO BYTES TO FIGURE OUT WHEN WE'VE READ ALL DATA
        fileLength = fileLength * 2;
        int byteCount = 100;
        while (byteCount < fileLength) {
            int recordNumber = dis.readInt();
            int recordLength = dis.readInt();
            byteCount += 8;

            System.out.println("Loading Polygon bytes (" + byteCount + " out of " + fileLength + ")");
            SHPPolygon poly = extractPolygon(dis);
            shapefileData.addShape(poly);
            poly.setRecordNumber(recordNumber);
            poly.setRecordLength(recordLength);
            byteCount += poly.getNumBytes();
        }
        return shapefileData;
    }

    /**
     * This helper method gets the bounding box data from the shapefile via the
     * input stream argument and uses it to build and return a double array that
     * represents it.
     */
    public double[] extract2DBoundingBox(DataInputStream dis) throws IOException {
        double minX = readLittleEndianDouble(dis);
        double minY = readLittleEndianDouble(dis);
        double maxX = readLittleEndianDouble(dis);
        double maxY = readLittleEndianDouble(dis);
        double[] boundingBox = new double[4];
        boundingBox[0] = minX;
        boundingBox[1] = minY;
        boundingBox[2] = maxX;
        boundingBox[3] = maxY;
        return boundingBox;
    }

    /**
     * This method loads a poly type, which could be a polygon or a polyline,
     * like for roads in a map. We will only be using polygons.
     */
    public SHPPolygon extractPolygon(DataInputStream dis) throws IOException {
        // THE FIRST 44 BYTES
        int verifyShapeType = readLittleEndianInt(dis);
        double[] boundingBox = extract2DBoundingBox(dis);
        int numParts = readLittleEndianInt(dis);
        int numPoints = readLittleEndianInt(dis);
        int numBytes = 44;

        // EXTRACT ALL PARTS
        int[] parts = new int[numParts];
        double[] pointsX = new double[numPoints];
        double[] pointsY = new double[numPoints];

        // PARTS MAY BE COUNTRIES OR STATES OR COUNTIES
        for (int i = 0; i < numParts; i++) {
            // STARTING INDEX OF POINT FOR THIS PART
            parts[i] = readLittleEndianInt(dis);
            numBytes += 4;
        }

        // GET ALL THE POINTS FOR THIS PART (WHICH IS REALLY ITS OWN POLYGON)
        for (int j = 0; j < (numPoints); j++) {
            pointsX[j] = readLittleEndianDouble(dis);
            pointsY[j] = readLittleEndianDouble(dis);
            numBytes += 16;
        }

        return new SHPPolygon(boundingBox, numBytes, numParts, numPoints, parts, pointsX, pointsY);
    }

    /**
     * This helps us read a char stored in a byte, ensuring we don't end up with
     * negatively values characters.
     */
    public char readCharByte(DataInputStream dis) throws IOException {
        byte b = dis.readByte();
        short s = b;
        if (s < 0) {
            s += 256;
        }
        return (char) s;
    }

    /**
     * This method is for reading a byte from a data stream and treating it as
     * an int, accounting for the fact that it should never be a negative
     * number.
     */
    public int readIntByte(DataInputStream dis) throws IOException {
        byte b = dis.readByte();
        int i = b;
        if (i < 0) {
            i += 256;
        }
        return i;
    }

    /**
     * Helper method for reading an int as little endian and building a big
     * endian int to use.
     */
    public int readLittleEndianInt(DataInputStream dis) throws IOException {
        int num = dis.readInt();
        int n0 = num & 0xff000000;
        n0 = (n0 >> 24) & 0x000000ff;
        int n1 = num & 0x00ff0000;
        n1 = n1 >> 8;
        int n2 = num & 0x0000ff00;
        n2 = n2 << 8;
        int n3 = num & 0x000000ff;
        n3 = n3 << 24;
        return n0 | n1 | n2 | n3;
    }

    /**
     * Helper method for reading a short as little endian and building a big
     * endian short to use.
     */
    public short readLittleEndianShort(DataInputStream dis) throws IOException {
        short num = dis.readShort();
        return Short.reverseBytes(num);
    }

    /**
     * Helper method for reading a double as little endian and building a big
     * endian double to use.
     */
    public double readLittleEndianDouble(DataInputStream dis) throws IOException {
        long num = dis.readLong();
        num = switchLongEndian(num);
        return Double.longBitsToDouble(num);
    }

    /**
     * This helper method switches the endianness of a long.
     */
    public long switchLongEndian(long bel) {
        long fullNum = 0;
        for (int i = 0; i < 8; i++) {
            long piece = bel & longMasks[i];
            if (i < 4) {
                piece = piece << (56 - (i * 8 * 2));
            } else {
                piece = piece >> (56 - ((7 - i) * 8 * 2));
            }
            if (i == 7) {
                piece = piece & 0x00000000000000ffL;
            }
            fullNum |= piece;
        }
        return fullNum;
    }
    static final String JSON_NUMBER_OF_SUBREGIONS = "NUMBER_OF_SUBREGIONS";
    static final String JSON_SUBREGIONS = "SUBREGIONS";
    static final String JSON_SUBREGION_INDEX = "SUBREGION_INDEX";
    static final String JSON_NUMBER_OF_SUBREGION_POLYGONS = "NUMBER_OF_SUBREGION_POLYGONS";
    static final String JSON_SUBREGION_POLYGONS = "SUBREGION_POLYGONS";
    static final String JSON_SUBREGION_POLYGON = "SUBREGION_POLYGON";
    static final String JSON_POLYGON_POINTS = "VERTICES";
    static final String JSON_POLYGON_POINT_X = "X";
    static final String JSON_POLYGON_POINT_Y = "Y";

    public void saveData(SHPData data, String filePath) throws IOException {

        // HERE ARE THE POLYGONS
        ObservableList<SHPPolygon> subRegions = data.getPolygons();

        // NUMBER OF REGIONS
        int numberOfSubRegions = subRegions.size();
int counter = 0;
        JsonArrayBuilder subRegionsBuilder = Json.createArrayBuilder();
        for (SHPPolygon subRegion : subRegions) {
            counter++;
            System.out.println("Saving subregion # " + counter + " out of " + numberOfSubRegions);
            // NUMBER OF PARTS
            int numberOfSubRegionPolygons = subRegion.getNumParts();
            JsonArrayBuilder polygonsBuilder = Json.createArrayBuilder();
            int polygonCounter = 0;
            for (int partIndex = 0; partIndex < numberOfSubRegionPolygons; partIndex++) {
                polygonCounter++;
                //System.out.println("Subregion #" + counter  + " out of " + numberOfSubRegions + " - Polygon #" + polygonCounter + " out of " + numberOfSubRegionPolygons);
                JsonArrayBuilder pointsBuilder = Json.createArrayBuilder();
                int startIndex = subRegion.parts[partIndex];
                int endIndex;
                if ((partIndex + 1) == numberOfSubRegionPolygons) {
                    endIndex = subRegion.numPoints - 1;
                } else {
                    endIndex = subRegion.parts[partIndex + 1] - 1;
                }

                for (int pointIndex = startIndex; pointIndex <= endIndex; pointIndex++) {
                    double pointX = subRegion.xPointsData[pointIndex];
                    double pointY = subRegion.yPointsData[pointIndex];
                    JsonObject pointObject = Json.createObjectBuilder()
                            .add(JSON_POLYGON_POINT_X, pointX)
                            .add(JSON_POLYGON_POINT_Y, pointY).build();
                    pointsBuilder.add(pointObject);
                }
                JsonArray pointsArray = pointsBuilder.build();
                polygonsBuilder.add(pointsArray);
            }
            JsonArray polygonsArray = polygonsBuilder.build();
            JsonObject subRegionObject = Json.createObjectBuilder()
                    .add(JSON_NUMBER_OF_SUBREGION_POLYGONS, numberOfSubRegionPolygons)
                    .add(JSON_SUBREGION_POLYGONS, polygonsArray).build();
            subRegionsBuilder.add(subRegionObject);
        }
        JsonArray subRegionsArray = subRegionsBuilder.build();

        JsonObject mapJson = Json.createObjectBuilder()
                .add(JSON_NUMBER_OF_SUBREGIONS, numberOfSubRegions)
                .add(JSON_SUBREGIONS, subRegionsArray).build();

        // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(sw);
        jsonWriter.writeObject(mapJson);
        jsonWriter.close();

        // INIT THE WRITER
        OutputStream os = new FileOutputStream(filePath);
        JsonWriter jsonFileWriter = Json.createWriter(os);
        jsonFileWriter.writeObject(mapJson);
        String prettyPrinted = sw.toString();
        PrintWriter pw = new PrintWriter(filePath);
        pw.write(prettyPrinted);
        pw.close();
    }
}
