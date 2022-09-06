package rvmm.files;

import djf.components.AppDataComponent;
import djf.components.AppFileComponent;
import djf.modules.AppGUIModule;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import org.apache.commons.io.FileUtils;
import static rvmm.data.MapDataKeys.*;
import static rvmm.data.RVMM_Constants.DEFAULT_MAP_HEIGHT;
import static rvmm.data.RVMM_Constants.DEFAULT_MAP_WIDTH;
import static rvmm.data.RVMM_Constants.WORK_FILE_EXT;
import static rvmm.data.RVMM_Constants.WORK_PATH;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;
import rvmm.files.shp.SHPData;
import rvmm.files.shp.SHPToJSONConverter;
import rvmm.workspace.DebugDisplay;

public class RegioVincoFiles implements AppFileComponent {

    public void downloadFlagImages(RegioVincoMapMakerData data) {
        Iterator<SubregionPrototype> subregionsIt = data.subregionsIterator();
        String origImagesPath = data.getOrigImagesPath();

        // IF THIS REGION'S ORIG IMAGES DIRECTORY DOESN'T EXIST, CREATE IT
        File testDir = new File(origImagesPath);
        if (!testDir.exists()) {
            boolean success = testDir.mkdir();
            System.out.println(success);
        }

        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            subregion.setValidFlag(false);
            String subregionName = subregion.getName();
            String flagLink = subregion.getFlagLink();
            int extIndex = flagLink.lastIndexOf(".");
            if (extIndex < 0) {
                System.out.println("No Image File Extension for " + flagLink);
            } else {
                // NOTE WE ONLY DEAL WITH PNGs
                downloadFlag(subregion, flagLink, data.getOrigFlagPath(subregion));
            }
        }
    }
    public void retrieveBrochureImage(RegioVincoMapMakerData data) {
        String brochurePath = data.getOrigBrochurePath();
        String brochureSourceURL = data.getBrochureImageURL();
        this.downloadBrochure(brochureSourceURL, brochurePath);
    }
    public boolean isValidFlagURL(String flagLink) {
        try {
            URL url = new URL(flagLink);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            if (HttpURLConnection.HTTP_NOT_FOUND == responseCode) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
    public void downloadFlag(SubregionPrototype subregion, String flagLink, String outputPath) {
        try {
            if (isValidFlagURL(flagLink)) {
                URL flagURL = new URL(flagLink);
                boolean success = downloadImageFile(flagURL, outputPath);
                subregion.setValidFlag(success);
            }
        } catch (MalformedURLException murle) {
            subregion.setValidFlag(false);
            // HANDLE BAD URL HERE
            murle.printStackTrace();
        }
    }
    public void downloadBrochure(String brochureSourceURL, String outputPath) {
        try {
            if (isValidFlagURL(brochureSourceURL)) {
                URL brochureURL = new URL(brochureSourceURL);
                boolean success = downloadImageFile(brochureURL, outputPath);
            }
        } catch (MalformedURLException murle) {
            // HANDLE BAD URL HERE
            murle.printStackTrace();
        }
    }
    public boolean downloadImageFile(URL fileURL, String outputFileName) {
        try {
            System.out.println("Downloading " + fileURL);
            InputStream in = fileURL.openStream();
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(outputFileName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            
            File imageFile = new File(outputFileName);
            BufferedImage sourceImage = ImageIO.read(imageFile);
            BufferedImage newImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            newImage.getGraphics().drawImage(sourceImage, 0, 0, null);
            
            // NOW SAVE THE IMAGE
            ImageIO.write(newImage, "png", imageFile);
            return true;
        } catch (IOException ioe) {
            // HANDLE FILE READING ERROR HERE
            System.out.println("Error downloading " + fileURL);
            return false;
        }
    }
    public BufferedImage getImageFromURL(String url) throws IOException {
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        BufferedImage image = ImageIO.read(connection.getInputStream());
        connection.disconnect();
        return image;
    }
    public BufferedImage getScaledImageFromURL(String url, double percentage) throws IOException {
        BufferedImage image = getImageFromURL(url);
        BufferedImage scaledImage = getScaledImage(image, percentage);
        return scaledImage;
    }
    public BufferedImage getScaledImage(BufferedImage sourceImage, double percentage) {
        int sourceImageWidth = sourceImage.getWidth();
        int sourceImageHeight = sourceImage.getHeight();
        int scaledImageWidth = (int) (Math.round(sourceImageWidth * percentage));
        int scaledImageHeight = (int) (Math.round(sourceImageHeight * percentage));
        if ((scaledImageWidth <= 0) || (scaledImageHeight <= 0)) {
            System.out.println("scaled image width or height < 0");
        }
        // RESIZE THE IMAGE
        BufferedImage scaledImage = new BufferedImage(
                scaledImageWidth, scaledImageHeight, sourceImage.getType());

        // COPY THE OLD IMAGE DATA OVER        
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(sourceImage, 0, 0, scaledImageWidth, scaledImageHeight, null);
        g2.dispose();

        return scaledImage;
    }
    
    public void saveData(AppDataComponent data, String filePath) throws IOException {
        // GET THE DATA
        RegioVincoMapMakerData rvmmData = (RegioVincoMapMakerData) data;

        // GET THE REGION NAME
        String regionName = rvmmData.getRegionName();
        String brochureImageURL = rvmmData.getBrochureImageURL();
        String brochureLink = rvmmData.getBrochureLink();
        String landmarksDescription = rvmmData.getLandmarksDescription();
        String leadersType = rvmmData.getLeadersType();
        String leadersWikiPageType = rvmmData.getLeadersWikiPageType();
        String leadersWikiPageURL = rvmmData.getLeadersWikiPageURL();
        String parentRegionPath = rvmmData.getParentRegionPath();
        String subregionType = rvmmData.getSubregionType();
        boolean subregionsHaveNames = rvmmData.doSubregionsAllHaveNames();
        boolean subregionsHaveCapitals = rvmmData.doSubregionsAllHaveCapitals();
        boolean subregionsHaveLeaders = rvmmData.doSubregionsAllHaveLeaders();
        boolean subregionsHaveFlags = rvmmData.doSubregionsAllHaveFlags();
        boolean subregionsHaveLandmarks = rvmmData.doSubregionsHaveLandmarks();
        double mapWidth = rvmmData.getMapWidth();
        double mapHeight = rvmmData.getMapHeight();
        double mapScale = rvmmData.getMapNavigator().getScale();
        double mapTranslateX = rvmmData.getMapNavigator().getMapTranslateX();
        double mapTranslateY = rvmmData.getMapNavigator().getMapTranslateY();
        JsonArray subregionsJSA = makeSubregionsJSA(rvmmData);
        JsonObject dataJSO = Json.createObjectBuilder()
                .add(RVM_REGION_NAME, regionName)
                .add(RVM_BROCHURE_IMAGE_URL, brochureImageURL)
                .add(RVM_BROCHURE_LINK, brochureLink)
                .add(RVM_LANDMARKS_DESCRIPTION, landmarksDescription)
                .add(RVM_LEADERS_TYPE, leadersType)
                .add(RVM_LEADERS_WIKI_PAGE_TYPE, leadersWikiPageType)
                .add(RVM_LEADERS_WIKI_PAGE_URL, leadersWikiPageURL)
                .add(RVM_PARENT_REGION_PATH, parentRegionPath)
                .add(RVM_SUBREGION_TYPE, subregionType)
                .add(RVM_SUBREGIONS_HAVE_NAMES, subregionsHaveNames)
                .add(RVM_SUBREGIONS_HAVE_CAPITALS, subregionsHaveCapitals)
                .add(RVM_SUBREGIONS_HAVE_LEADERS, subregionsHaveLeaders)
                .add(RVM_SUBREGIONS_HAVE_FLAGS, subregionsHaveFlags)
                .add(RVM_SUBREGIONS_HAVE_LANDMARKS, subregionsHaveLandmarks)
                .add(RVM_WIDTH, mapWidth)
                .add(RVM_HEIGHT, mapHeight)
                .add(RVM_SCALE, mapScale)
                .add(RVM_TRANSLATE_X, mapTranslateX)
                .add(RVM_TRANSLATE_Y, mapTranslateY)
                .add(RVM_SUBREGIONS, subregionsJSA)
                .build();
        saveJsonFile(dataJSO, filePath, true);
    }
    public void saveJsonFile(JsonObject dataJSO, String filePath, boolean compressed) throws IOException {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, !compressed);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(sw);
        jsonWriter.writeObject(dataJSO);
        jsonWriter.close();

        OutputStream os = new FileOutputStream(filePath);
        JsonWriter jsonFileWriter = Json.createWriter(os);
        jsonFileWriter.writeObject(dataJSO);
        String jsonText = sw.toString();
        PrintWriter pw = new PrintWriter(filePath);
        pw.write(jsonText);
        pw.close();
        os.close();
    }
    public JsonArray makeLandmarksJSA(SubregionPrototype subregion) {
        JsonArrayBuilder landmarksArrayBuilder = Json.createArrayBuilder();
        Iterator<String> landmarksIt = subregion.landmarksIterator();
        while (landmarksIt.hasNext()) {
            String landmark = landmarksIt.next();
            landmarksArrayBuilder.add(landmark);
        }
        return landmarksArrayBuilder.build();
    }
    public JsonArray makePolygonJSA(Polygon polygon) {
        JsonArrayBuilder arrayCoordBuilder = Json.createArrayBuilder();
        ObservableList<Double> pointsToSave = polygon.getPoints();
        for (int i = 0; i < pointsToSave.size(); i += 2) {
            double pointX = pointsToSave.get(i);
            arrayCoordBuilder.add(pointX);
            double pointY = pointsToSave.get(i + 1);
            arrayCoordBuilder.add(pointY);
        }
        return arrayCoordBuilder.build();
    }
    public JsonArray makePolygonsJSA(SubregionPrototype subregion) {
        JsonArrayBuilder polygonsArrayBuilder = Json.createArrayBuilder();
        Iterator<Polygon> polygonsIt = subregion.polygonsIterator();
        while (polygonsIt.hasNext()) {
            Polygon polygon = polygonsIt.next();
            JsonArray polygonJSA = makePolygonJSA(polygon);
            polygonsArrayBuilder.add(polygonJSA);
        }
        return polygonsArrayBuilder.build();
    }
    public JsonObject makeGreyscaleColorJSO(SubregionPrototype subregion) {
        JsonObjectBuilder jsoBuilder = Json.createObjectBuilder();
        Color greyscaleColor = subregion.getGreyscaleColor();
        double red = greyscaleColor.getRed();
        double green = greyscaleColor.getGreen();
        double blue = greyscaleColor.getBlue();
        return jsoBuilder
                .add(RVM_RED, red)
                .add(RVM_GREEN, green)
                .add(RVM_BLUE, blue)
                .build();
    }
    public JsonObject makeSubregionJSO(SubregionPrototype subregion) {
        String subregionName = subregion.getName();
        String capitalName = subregion.getCapital();
        String leaderName = subregion.getLeader();
        String flagLink = subregion.getFlagLink();
        boolean isTerritory = subregion.getIsTerritory();
        JsonArray landmarksJSA = makeLandmarksJSA(subregion);
        JsonObject greyscaleColorJSO = makeGreyscaleColorJSO(subregion);
        JsonArray polygonsJSA = makePolygonsJSA(subregion);
        JsonObjectBuilder jso = Json.createObjectBuilder();
        return jso
                .add(RVM_REGION_NAME, subregionName)
                .add(RVM_CAPITAL, capitalName)
                .add(RVM_LEADER, leaderName)
                .add(RVM_FLAG_LINK, flagLink)
                .add(RVM_IS_TERRITORY, isTerritory)
                .add(RVM_LANDMARKS, landmarksJSA)
                .add(RVM_GREYSCALE_COLOR, greyscaleColorJSO)
                .add(RVM_POLYGON_ARRAY, polygonsJSA)
                .build();
    }
    public JsonArray makeSubregionsJSA(RegioVincoMapMakerData data) {
        Iterator<SubregionPrototype> subregionsIt = data.subregionsIterator();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            JsonObject subregionJSO = makeSubregionJSO(subregion);
            arrayBuilder.add(subregionJSO);
        }
        return arrayBuilder.build();
    }

    public void loadData(AppDataComponent data, String filePath) throws IOException {
        // LOAD THE JSON FILE WITH ALL THE DATA
        JsonObject json = loadJSONFile(filePath);

        // PREPARE TO LOAD DATA
        RegioVincoMapMakerData rvmmData = (RegioVincoMapMakerData) data;
        data.reset();
        rvmmData.startLoading();

        // THESE TWO HAVE TO LOAD FIRST DUE TO DEPENDENCIES
        loadRegionName(rvmmData, json);
        loadParentRegionPath(rvmmData, json);
        
        loadBrochureImageURL(rvmmData, json);
        loadBrochureLink(rvmmData, json);
        loadLandmarksDescription(rvmmData, json);
        loadLeadersType(rvmmData, json);
        loadLeadersWikiPageType(rvmmData, json);
        loadLeadersWikiPageURL(rvmmData, json);
        loadSubregionType(rvmmData, json);

        // AND THEN MAP VIEWING VALUES
        loadMapDimensions(rvmmData, json);
        loadViewport(rvmmData, json);

        // THE LOAD THE SUBREGIONS        
        loadSubregions(rvmmData, json);

        // WE'RE DONE LOADING DATA
        rvmmData.endLoading();
    }
    public JsonObject loadJSONFile(String jsonFilePath) throws IOException {
        InputStream is = new FileInputStream(jsonFilePath);
        JsonReader jsonReader = Json.createReader(is);
        JsonObject json = jsonReader.readObject();
        jsonReader.close();
        is.close();
        return json;
    }
    public void loadBrochureImageURL(RegioVincoMapMakerData data, JsonObject jso) {
        String brochureImageURL = loadString(jso, RVM_BROCHURE_IMAGE_URL);
        data.setBrochureImageURL(brochureImageURL);
    }
    public void loadBrochureLink(RegioVincoMapMakerData data, JsonObject jso) {
        String brochureLink = loadString(jso, RVM_BROCHURE_LINK);
        data.setBrochureLink(brochureLink);
    }
    public void loadLandmarksDescription(RegioVincoMapMakerData data, JsonObject jso) {
        String landmarksDescription = loadString(jso, RVM_LANDMARKS_DESCRIPTION);
        data.setLandmarksDescription(landmarksDescription);
    }
    public void loadLeadersType(RegioVincoMapMakerData data, JsonObject jso) {
        String leadersType = loadString(jso, RVM_LEADERS_TYPE);
        data.setLeadersType(leadersType);
    }
    public void loadLeadersWikiPageType(RegioVincoMapMakerData data, JsonObject jso) {
        String leadersWikiPageType = loadString(jso, RVM_LEADERS_WIKI_PAGE_TYPE);
        data.setLeadersWikiPageType(leadersWikiPageType);
    }
    public void loadLeadersWikiPageURL(RegioVincoMapMakerData data, JsonObject jso) {
        String leadersWikiPageURL = loadString(jso, RVM_LEADERS_WIKI_PAGE_URL);
        data.setLeadersWikiPageURL(leadersWikiPageURL);
    }
    public void loadParentRegionPath(RegioVincoMapMakerData data, JsonObject jso) {
        String parentRegionPath = loadString(jso, RVM_PARENT_REGION_PATH);
        data.setParentRegionPath(parentRegionPath);
    }
    public void loadRegionName(RegioVincoMapMakerData data, JsonObject jso) {
        String name = loadString(jso, RVM_REGION_NAME);
        data.setRegionName(name);
    }
    public void loadSubregionType(RegioVincoMapMakerData data, JsonObject jso) {
        String subregionType = loadString(jso, RVM_SUBREGION_TYPE);
        data.setSubregionType(subregionType);
    }
    public boolean loadBoolean(JsonObject jso, String id) {
        boolean value = false;
        try {
            value = jso.getBoolean(id);
        } catch (Exception e) {
            System.out.println(id + " field not found, setting false value");
        }
        return value;
    }
    public String loadString(JsonObject jso, String id) {
        String value = "";
        try {
            value = jso.getString(id);
        } catch (Exception e) {
            System.out.println(id + " field not found, setting empty value");
        }
        return value;
    }
    public void loadMapDimensions(RegioVincoMapMakerData data, JsonObject jso) {
        double mapWidth = jso.getJsonNumber(RVM_WIDTH).doubleValue();
        double mapHeight = jso.getJsonNumber(RVM_HEIGHT).doubleValue();
        if (mapWidth == 0.0)
            DebugDisplay.appendDebugText("mapWidth is 0?");
        data.setMapDimensions(mapWidth, mapHeight);
    }
    public void loadViewport(RegioVincoMapMakerData data, JsonObject jso) {
        double mapScale = jso.getJsonNumber(RVM_SCALE).doubleValue();
        double mapTranslateX = jso.getJsonNumber(RVM_TRANSLATE_X).doubleValue();
        double mapTranslateY = jso.getJsonNumber(RVM_TRANSLATE_Y).doubleValue();
        System.out.println("mapScale: " + mapScale);
        System.out.println("mapTranslateX: " + mapTranslateX);
        System.out.println("mapTranslateY: " + mapTranslateY);
        data.getMapNavigator().setMapTranslate(mapTranslateX, mapTranslateY);
        data.getMapNavigator().setMapScale(mapScale);
    }
    public void loadSubregions(RegioVincoMapMakerData data, JsonObject jso) {
        JsonArray subregionsJSA = jso.getJsonArray(RVM_SUBREGIONS);
        for (int i = 0; i < subregionsJSA.size(); i++) {
            JsonObject subregionJSO = subregionsJSA.getJsonObject(i);
            SubregionPrototype subregionToAdd = loadSubregion(subregionJSO);
            subregionToAdd.loadBounds();
            data.addSubregion(subregionToAdd);
        }
    }
    public SubregionPrototype loadSubregion(JsonObject subregionJSO) {
        SubregionPrototype subregion = new SubregionPrototype();
        String name = loadString(subregionJSO, RVM_REGION_NAME);
        subregion.setName(name);
        String capital = loadString(subregionJSO, RVM_CAPITAL);
        subregion.setCapital(capital);
        String leader = loadString(subregionJSO, RVM_LEADER);
        subregion.setLeader(leader);
        String flagLink = loadString(subregionJSO, RVM_FLAG_LINK);
        subregion.setFlagLink(flagLink);
        boolean isTerritory = loadBoolean(subregionJSO, RVM_IS_TERRITORY);
        subregion.setIsTerritory(isTerritory);
        try {
            JsonArray landmarksJSA = subregionJSO.getJsonArray(RVM_LANDMARKS);
            for (int i = 0; i < landmarksJSA.size(); i++) {
                String landmark = landmarksJSA.getString(i);
                subregion.addLandmark(landmark);
            }
        } catch (Exception e) {
            System.out.println("No Landmarks Found Yet");
        }
        JsonArray polygonsJSA = subregionJSO.getJsonArray(RVM_POLYGON_ARRAY);
        for (int i = 0; i < polygonsJSA.size(); i++) {
            JsonArray pointsJSA = polygonsJSA.getJsonArray(i);
            Polygon polygonToAdd = new Polygon();
            ObservableList<Double> points = polygonToAdd.getPoints();
            for (int j = 0; j < pointsJSA.size(); j += 2) {
                points.add(pointsJSA.getJsonNumber(j).doubleValue());
                points.add(pointsJSA.getJsonNumber(j + 1).doubleValue());
            }
            subregion.addPolygon(polygonToAdd);
        }
        JsonObject greyscaleColorJSO = subregionJSO.getJsonObject(RVM_GREYSCALE_COLOR);
        Color greyscaleColor = Color.color(
                greyscaleColorJSO.getJsonNumber(RVM_RED).doubleValue(),
                greyscaleColorJSO.getJsonNumber(RVM_GREEN).doubleValue(),
                greyscaleColorJSO.getJsonNumber(RVM_BLUE).doubleValue());
        subregion.setGreyscaleColor(greyscaleColor);
        return subregion;
    }

    public void importData(AppDataComponent data, String filePath) throws IOException {
        RegioVincoMapMakerData mapData = (RegioVincoMapMakerData)data;

        // NOTE THAT WE ARE USING THE SIZE OF THE MAP
        mapData.setMapDimensions(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
        mapData.reset();
        
        this.importShapefile(mapData, filePath);
    }
    public int getDataAsInt(JsonObject json, String dataName) {
        JsonValue value = json.get(dataName);
        JsonNumber number = (JsonNumber) value;
        return number.bigIntegerValue().intValue();
    }
    
    GeoJsonCompressor compressor;
    public void exportData(AppDataComponent dataManager, String savedFileName) throws IOException {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) dataManager;
        String regionName = data.getRegionName();

        // EXPORT THE IMAGES FIRST
        exportSubregionFlags(data);
        exportBrochure(data);

        // EXPORT THE DATA
        this.exportDataToGeoJson(data);
    }
    public void exportDataToGeoJson(RegioVincoMapMakerData data) {
        String name = data.getRegionName();
        AppGUIModule gui = data.getApp().getGUIModule();

        // WE'LL USE THIS TO COMPRESS THE DATA TO THE DESIRED LEVEL DURING EXPORTING
        compressor = new GeoJsonCompressor();

        // COMPRESS THE DATA FIRST
        compressor.compressData(data);

        // WE CAN THEN USE THAT COMPRESSED DATA TO GENERATE THE JSON FILE
        JsonObject dataJSO = makeGeoJsonJSO(data);

        // IF WE'VE REACHED HERE THEN WE HAVE COMPRESSED
        // THE COOREDINATES DATA ENOUGH AND WE CAN NOW SAVE IT,
        // NOTE WE MAY EXPORT IT TWICE IF THOSE OPTIONS ARE SELECTED
        if (data.getExportToGameApp())
            exportToFile(data.getExportGameAppDataFilePath(), dataJSO);
        if (data.getExportToRVMMApp())
            exportToFile(data.getExportRVMMAppDataFilePath(), dataJSO);
    }
    
    public void exportToFile(String exportPath, JsonObject jso) {
        try {
            System.out.println("Saving " + exportPath);
            
            // MAKE SURE THE EXPORT PATH EXISTS
            File dataFile = new File(exportPath);
            File parentDir = dataFile.getParentFile();
            if (!parentDir.exists()) {
                Path dir = Paths.get(parentDir.toURI());
                Files.createDirectories(dir);
            }
            
            saveJsonFile(jso, exportPath, true);
            System.out.println(exportPath + " saved");
//            RegioVincoMapMakerApp app = data.getApp();
//            ExportViewerDialog dialog = ExportViewerDialog.getSubregionDialog(app);
//            dialog.showDialog();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void exportSubregionFlags(RegioVincoMapMakerData data) {
        ObservableList<SubregionPrototype> subregions = data.getSubregions();
        for (SubregionPrototype subregion : subregions) {
            String flagSourcePath = data.getOrigFlagPath(subregion);
            String gameAppFlagDestPath = data.getExportGameAppFlagPath(subregion);
            String rvmmAppFlagDestPath = data.getExportRVMMAppFlagPath(subregion);
            if (data.getExportToGameApp())
                exportFlagImage(data, flagSourcePath, gameAppFlagDestPath);
            if (data.getExportToRVMMApp())
                exportFlagImage(data, flagSourcePath, rvmmAppFlagDestPath);
        }
    }
    
    private void exportFlagImage(RegioVincoMapMakerData data,
                                    String sourcePath,
                                    String destPath) {
        try {
            // LOAD THE SOURCE IMAGE FROM THE SOURCE
            File sourceFile = new File(sourcePath);
            BufferedImage sourceImage = ImageIO.read(sourceFile);
            int desiredFlagWidth = (int) (Math.round(data.getFlagsWidth()));
            int sourceImageWidth = sourceImage.getWidth();
            double percentage = (double) desiredFlagWidth / (double) sourceImageWidth;
            
            // GET THE SCALED IMAGE
            BufferedImage destImage = getScaledImage(sourceImage, percentage);
            
            // MAKE SURE THE EXPORT PATH EXISTS
            File flagFile = new File(destPath);
            File parentDir = flagFile.getParentFile();
            if (!parentDir.exists()) {
                Path dir = Paths.get(parentDir.toURI());
                Files.createDirectories(dir);
            }
            
            // SAVE THE IMAGE TO THE DESTINATION
            ImageIO.write(destImage, "png", flagFile);
        } catch (Exception e) {
            System.out.println("Unable to export " + sourcePath);
        }
    }

    public void exportBrochure(RegioVincoMapMakerData data) {
        String brochureSource = data.getOrigBrochurePath();
        String gameBrochureDest = data.getExportGameAppBrochurePath();
        String rvmmBrochureDest = data.getExportRVMMAppBrochurePath();
        if (data.getExportToGameApp())
            exportFlagImage(data, brochureSource, gameBrochureDest);
        if (data.getExportToRVMMApp())
            exportFlagImage(data, brochureSource, rvmmBrochureDest);
    }
    
    private void exportBrochureImage(RegioVincoMapMakerData data,
                        String brochureSource, String brochureDest) {
        try {
            File sourceFile = new File(brochureSource);
            BufferedImage sourceImage = ImageIO.read(sourceFile);

            double destHeight = data.getBrochuresHeight();
            double sourceHeight = sourceImage.getHeight();
            double percentage = destHeight / sourceHeight;

            // GET THE SCALED IMAGE
            BufferedImage destImage = getScaledImage(sourceImage, percentage);

            // MAKE SURE THE EXPORT PATH EXISTS
            File brochureFile = new File(brochureDest);
            File parentDir = brochureFile.getParentFile();
            if (!parentDir.exists()) {
                Path dir = Paths.get(parentDir.toURI());
                Files.createDirectories(dir);
            }
            
            // SAVE THE IMAGE TO THE DESTINATION
            ImageIO.write(destImage, "png", new File(brochureDest));
        } catch (Exception e) {
            System.out.println("Error exporting brochure for " + data.getRegionName());
        }
    }
    public JsonObject makeGeoJsonJSO(RegioVincoMapMakerData data) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        return jsonBuilder
                .add(RVM_GEOJSON_TYPE, "FeatureCollection")
                .add(RVM_GEOJSON_FEATURES, makeFeaturesJSA(data))
                .add(RVM_GEOJSON_PROPERTIES, makeMapPropertiesJSO(data, false))
                .build();
    }
    public JsonArray makeLandmarksJSA(ObservableList<String> landmarks) {
        JsonArrayBuilder jsonBuilder = Json.createArrayBuilder();
        for (String s : landmarks) {
            jsonBuilder.add(s);
        }
        return jsonBuilder.build();
    }
    public JsonObject makeSubregionPropertiesJSO(SubregionPrototype subregion) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        return jsonBuilder
                .add(RVM_GEOJSON_NAME, subregion.getName())
                .add(RVM_GEOJSON_CAPITAL, subregion.getCapital())
                .add(RVM_GEOJSON_LEADER, subregion.getLeader())
                .add(RVM_GEOJSON_LANDMARKS, makeLandmarksJSA(subregion.cloneLandmarks()))
                .build();
    }
    public JsonObject makeGeometryJSO(SubregionPrototype subregion) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        return jsonBuilder
                .add(RVM_GEOJSON_TYPE, "Polygon")
                .add(RVM_GEOJSON_COORDINATES, makeCoordinatesJSA(subregion))
                .build();
    }
    public JsonArray makeGeoJsonPolygonJSA(ArrayList<double[]> polygon) {
        JsonArrayBuilder polygonBuilder = Json.createArrayBuilder();
        for (double[] point : polygon) {
            JsonArrayBuilder pointBuilder = Json.createArrayBuilder();
            pointBuilder.add(point[0]);
            pointBuilder.add(point[1]);
            JsonArray pointJSA = pointBuilder.build();
            polygonBuilder.add(pointJSA);
        }
        return polygonBuilder.build();
    }
    public JsonArray makeCoordinatesJSA(SubregionPrototype subregion) {
        JsonArrayBuilder coordinatesBuilder = Json.createArrayBuilder();
        // @todo - change this to go through the compressor data
        String subregionName = subregion.getName();
        ArrayList<ArrayList<double[]>> polygonsData = compressor.features.get(subregionName);
        for (ArrayList<double[]> polygon : polygonsData) {
            coordinatesBuilder.add(makeGeoJsonPolygonJSA(polygon));
        }
        return coordinatesBuilder.build();
    }
    public JsonObject makeFeatureJSO(SubregionPrototype subregion) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        return jsonBuilder
                .add(RVM_GEOJSON_TYPE, "Feature")
                .add(RVM_GEOJSON_GEOMETRY, makeGeometryJSO(subregion))
                .add(RVM_GEOJSON_PROPERTIES, makeSubregionPropertiesJSO(subregion))
                .build();
    }
    public JsonArray makeFeaturesJSA(RegioVincoMapMakerData data) {
        JsonArrayBuilder jsaBuilder = Json.createArrayBuilder();
        for (SubregionPrototype subregion : data.getSubregions()) {
            jsaBuilder.add(makeFeatureJSO(subregion));
        }
        return jsaBuilder.build();
    }
    public JsonObject makeMapPropertiesJSO(RegioVincoMapMakerData data, boolean includeAllSavedData) {
        JsonObjectBuilder jsoBuilder = Json.createObjectBuilder();
        boolean subregionsHaveNames = data.doSubregionsAllHaveNames();
        boolean subregionsHaveCapitals = data.doSubregionsAllHaveCapitals();
        boolean subregionsHaveLeaders = data.doSubregionsAllHaveLeaders();
        boolean subregionsHaveFlags = data.doSubregionsAllHaveFlags();
        boolean subregionsHaveLandmarks = data.doSubregionsHaveLandmarks();
        String subregionType = data.getSubregionType();
        String landmarksDescription = data.getLandmarksDescription();
        String brochureImageURL = data.getBrochureImageURL();
        String brochureLink = data.getBrochureLink();
        String leadersWikiPageType = data.getLeadersWikiPageType();
        String leadersWikiPageURL = data.getLeadersWikiPageURL();
        if (includeAllSavedData) {
            return jsoBuilder
                .add(RVM_SUBREGIONS_HAVE_NAMES, subregionsHaveNames)
                .add(RVM_SUBREGIONS_HAVE_CAPITALS, subregionsHaveCapitals)
                .add(RVM_SUBREGIONS_HAVE_LEADERS, subregionsHaveLeaders)
                .add(RVM_SUBREGIONS_HAVE_FLAGS, subregionsHaveFlags)
                .add(RVM_SUBREGIONS_HAVE_LANDMARKS, subregionsHaveLandmarks)
                .add(RVM_SUBREGION_TYPE, subregionType)
                .add(RVM_LANDMARKS_DESCRIPTION, landmarksDescription)
                .add(RVM_BROCHURE_IMAGE_URL, brochureImageURL)
                .add(RVM_BROCHURE_LINK, brochureLink)
                .add(RVM_LEADERS_WIKI_PAGE_TYPE, leadersWikiPageType)
                .add(RVM_LEADERS_WIKI_PAGE_URL, leadersWikiPageURL)
                    .build();
        }
        else {
            // THIS ONLY INCLUDES THE DATA NEEDED BY THE GAME
            return jsoBuilder
                .add(RVM_SUBREGIONS_HAVE_NAMES, subregionsHaveNames)
                .add(RVM_SUBREGIONS_HAVE_CAPITALS, subregionsHaveCapitals)
                .add(RVM_SUBREGIONS_HAVE_LEADERS, subregionsHaveLeaders)
                .add(RVM_SUBREGIONS_HAVE_FLAGS, subregionsHaveFlags)
                .add(RVM_SUBREGIONS_HAVE_LANDMARKS, subregionsHaveLandmarks)
                .add(RVM_SUBREGION_TYPE, subregionType)
                .add(RVM_LANDMARKS_DESCRIPTION, landmarksDescription)
                .add(RVM_BROCHURE_LINK, brochureLink)
                    .build();
        }
    }

    public HashMap<String, String> loadMapProperties(String workFilePath) {
        HashMap<String, String> mapProperties = new HashMap();

        try {
            JsonObject json = loadJSONFile(workFilePath);
            boolean subregionsHaveNames = this.loadBoolean(json, RVM_SUBREGIONS_HAVE_NAMES);
            boolean subregionsHaveCapitals = this.loadBoolean(json, RVM_SUBREGIONS_HAVE_CAPITALS);
            boolean subregionsHaveLeaders = this.loadBoolean(json, RVM_SUBREGIONS_HAVE_LEADERS);
            boolean subregionsHaveFlags = this.loadBoolean(json, RVM_SUBREGIONS_HAVE_FLAGS);
            boolean subregionsHaveLandmarks = this.loadBoolean(json, RVM_SUBREGIONS_HAVE_LANDMARKS);
            mapProperties.put(RVM_SUBREGIONS_HAVE_NAMES, "" + subregionsHaveNames);
            mapProperties.put(RVM_SUBREGIONS_HAVE_CAPITALS, "" + subregionsHaveCapitals);
            mapProperties.put(RVM_SUBREGIONS_HAVE_LEADERS, "" + subregionsHaveLeaders);
            mapProperties.put(RVM_SUBREGIONS_HAVE_FLAGS, "" + subregionsHaveFlags);
            mapProperties.put(RVM_SUBREGIONS_HAVE_LANDMARKS, "" + subregionsHaveLandmarks);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return mapProperties;
    }


    class GeoJsonCompressor {
        // THIS ONLY GETS INITIALIZED ONCE, WHEN THE COMPRESSOR IS CONSTRUCTED,
        // IT WILL BE UPDATED SLIGHLY LARGER WITH EACH ROUND OF COMPRESSION
        double minDist;

        // THESE GET RESET WITH EACH TIME WE TRY TO COMPRESS THE DATA
        double[] firstCoord;
        double[] avg;
        int numCoords;
        int POINTS_THRESHOLD = 4;

        // THIS GETS RESET ONCE PER COMPRESSION EVENT, IT STORES ALL
        // THE COMPRESSED GEOMETRY
        public HashMap<String, ArrayList<ArrayList<double[]>>> features;

        // THIS GETS RESET AND THEN BUILT FOR EACH SUBREGION (i.e. FEATURE)
        public ArrayList<ArrayList<double[]>> geometry;

        // THIS GETS RESET AND THEN BUILT FOR EACH POLYGON
        // AND THEN GETS ADDED TO coordinates
        public ArrayList<double[]> coordinates;

        public GeoJsonCompressor() {
            // A minDist OF 0 WOULD MEAN NO COMPRESSION
            minDist = 0;

            // RESET TO START
            reset();
        }

        public void addFirstPointToLast() {
            if (coordinates.size() > 0) {
                coordinates.add(coordinates.get(0));
            }
        }

        public void addCoordinatesToGeometry() {
            if (coordinates.size() > 0) {
                geometry.add(coordinates);
            }
        }

        public void addFeature(String feature) {
            features.put(feature, geometry);
        }

        public void initFeatures() {
            features = new HashMap();
        }

        public void initGeometry() {
            geometry = new ArrayList();
        }

        public void initCoordinates() {
            coordinates = new ArrayList();
        }

        public void addPointToCoordinates(double[] point) {
            coordinates.add(point);
        }

        public boolean lastCoordinateIsNotAvg() {
            double[] testPoint = null;
            if (coordinates.size() > 0) {
                return (coordinates.get(coordinates.size() - 1) == avg);
            } else {
                return false;
            }
        }

        public double[] addRoundedPoint() {
            double ROUNDING_PLACES = 100000.0;
            double x = Math.round(avg[0] * ROUNDING_PLACES) / ROUNDING_PLACES;
            double y = Math.round(avg[1] * ROUNDING_PLACES) / ROUNDING_PLACES;
            double[] roundedPoint = {x, y};
            addPointToCoordinates(roundedPoint);
            return roundedPoint;
        }

        public void reset() {
            firstCoord = null;
            avg = null;
            numCoords = 0;
        }

        public boolean hasFewPoints() {
            return (coordinates.size() < POINTS_THRESHOLD);
        }

        public void incCompressionLevel() {
            // MAKING THIS VALUE SMALLER MIGHT SLOW DOWN COMPRESSION
            // MAKING IT BIGGER MIGHT ELIMINATE TOO MANY POINTS
            double COMPRESSION_INCREMENT = 0.001;
            minDist += COMPRESSION_INCREMENT;
        }

        public double distance(double[] coordA, double[] coordB) {
            return Math.sqrt(Math.pow(coordB[0] - coordA[0], 2)
                    + Math.pow(coordB[1] - coordA[1], 2));
        }

        public double convertLong(double x) {
            return (x / 4.0) - 180.0;
        }

        public double convertLat(double y) {
            return ((860.0 - y) / 4.0) - 90.0;
        }

        public boolean testPointToAdd(double[] point) {
            if (numCoords == 0) {
                firstCoord = point;
                numCoords = 1;
                avg = point;
                return true;
            }
            if (distance(avg, point) > minDist) {
                return false;
            }
            double[] newAvg = new double[2];
            newAvg[0] = (numCoords * avg[0] + point[0]) / (numCoords + 1);
            newAvg[1] = (numCoords * avg[1] + point[1]) / (numCoords + 1);
            if (distance(firstCoord, newAvg) > minDist) {
                return false;
            }
            avg = newAvg;
            numCoords += 1;
            return true;
        }

        public double[] getAvg() {
            return avg;
        }

        double VALUES_PER_COORD = 2;

        public double totalBytes() {
            int totalNumCoords = 0;
            for (String s : this.features.keySet()) {
                ArrayList<ArrayList<double[]>> polygons = features.get(s);
                for (ArrayList<double[]> polygon : polygons) {
                    totalNumCoords += polygon.size();
                }
            }
            // 2, ONE FOR x, ONE FOR y
            // 8 IS THE NUMBER OF BYTES PER DOUBLE
            return (totalNumCoords * VALUES_PER_COORD) * Double.SIZE;
        }

        public void compressData(RegioVincoMapMakerData data) {
            boolean smallEnough = false;
            do {
                initFeatures();
                reset();
                for (SubregionPrototype subregion : data.getSubregions()) {
                    initGeometry();
                    Iterator<Polygon> polygonsIt = subregion.polygonsIterator();
                    while (polygonsIt.hasNext()) {
                        initCoordinates();
                        Polygon polygon = polygonsIt.next();
                        ObservableList<Double> points = polygon.getPoints();
                        Iterator<Double> pointIt = points.iterator();
                        while (pointIt.hasNext()) {
                            double x = pointIt.next();
                            double y = pointIt.next();
                            x = convertLong(x);
                            y = convertLat(y);
                            double[] testPoint = {x, y};
                            if (!testPointToAdd(testPoint)) {
                                // REGISTER IT WITH THE COMPRESSOR AND LET
                                // THE COMPRESSOR DO SOME AVERAGING
                                double[] addedPoint = addRoundedPoint();
                                reset();
                                testPointToAdd(testPoint);
                            }
                        }
                        if (hasFewPoints()) {
                            reset();
                        } else {
                            if (lastCoordinateIsNotAvg()) {
                                addRoundedPoint();
                            }
                            addFirstPointToLast();
                            addCoordinatesToGeometry();
                            reset();
                        }
                    }
                    addFeature(subregion.getName());
                }
                System.out.println("totalBytes: " + totalBytes());
                System.out.println("exportMapSizeInBytes: " + data.getExportMapSizeInBytes());
                if (totalBytes() > data.getExportMapSizeInBytes()) {
                    incCompressionLevel();
                } else {
                    smallEnough = true;
                }
            } while (!smallEnough);
        }
    }
    
    public void importShapefile(RegioVincoMapMakerData mapData, String filePath) throws IOException {
        // IF THE IMPORT FILE IS AN SHP FILE WE HAVE TO FIRST
        // CONVERT IT INTO A JSON FILE
        if (filePath.endsWith(".shp")) {
            // SWITCH THE PATH TO THE JSON FILE AND THEN LOAD THAT FILE
            String jsonPath = filePath.substring(0, filePath.indexOf(".shp")) + ".json";
            System.out.println("Importing " + filePath);
            SHPToJSONConverter shpToJSONConverter = new SHPToJSONConverter();
            SHPData shpData = shpToJSONConverter.loadData(filePath);
            System.out.println("Saving " + filePath);
            shpToJSONConverter.saveData(shpData, jsonPath);
            System.out.println("Saved " + filePath);

            // WE'LL GO ON LOADING THE JSON FILE NOW
            filePath = jsonPath;
        }

        // LOAD THE JSON FILE WITH ALL THE DATA
        JsonObject json = loadJSONFile(filePath);

        // THIS IS THE TOTAL NUMBER OF SUBREGIONS, EACH WITH
        // SOME NUMBER OF POLYGONS
        int numSubregions = getDataAsInt(json, RAW_MAP_NUMBER_OF_SUBREGIONS);
        JsonArray jsonSubregionsArray = json.getJsonArray(RAW_MAP_SUBREGIONS);

        // GO THROUGH ALL THE SUBREGIONS
        for (int subregionIndex = 0; subregionIndex < numSubregions; subregionIndex++) {
            // MAKE A POLYGON LIST FOR THIS SUBREGION
            JsonObject jsonSubregion = jsonSubregionsArray.getJsonObject(subregionIndex);
            int numSubregionPolygons = getDataAsInt(jsonSubregion, RAW_MAP_NUMBER_OF_SUBREGION_POLYGONS);
            ArrayList<ArrayList<Double>> subregionPolygonPoints = new ArrayList();
            // GO THROUGH ALL OF THIS SUBREGION'S POLYGONS
            for (int polygonIndex = 0; polygonIndex < numSubregionPolygons; polygonIndex++) {
                // GET EACH POLYGON (IN LONG/LAT GEOGRAPHIC COORDINATES)
                JsonArray jsonPolygon = jsonSubregion.getJsonArray(RAW_MAP_SUBREGION_POLYGONS);
                JsonArray pointsArray = jsonPolygon.getJsonArray(polygonIndex);
                ArrayList<Double> polygonPointsList = new ArrayList();
                for (int pointIndex = 0; pointIndex < pointsArray.size(); pointIndex++) {
                    JsonObject point = pointsArray.getJsonObject(pointIndex);
                    double pointX = point.getJsonNumber(RAW_MAP_POLYGON_POINT_X).doubleValue();
                    double pointY = point.getJsonNumber(RAW_MAP_POLYGON_POINT_Y).doubleValue();
                    polygonPointsList.add(pointX);
                    polygonPointsList.add(pointY);
                }
                subregionPolygonPoints.add(polygonPointsList);
            }
            mapData.addSubregion(subregionPolygonPoints);
        }
        mapData.randomizeSubregionColors();
        mapData.getMapNavigator().reset();
    }

    private String removeFileExt(String workPath) {
        return workPath.substring(0, workPath.indexOf("."));
    }
    private void promoteWork(RegioVincoMapMakerData data, File workDir) throws IOException {
        String pathPieceToRemove = data.getRegionName() + "/";
        File[] files = workDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                promoteWork(data, file);
            }
            else {
                if (file.getName().endsWith(WORK_FILE_EXT)) {
                    // LOAD THE DATA FILE
                    JsonObject jso = loadJSONFile(file.getAbsolutePath());

                    // CHANGE THE PARENT REGION PATH
                    String newParentPath = jso.getString(RVM_PARENT_REGION_PATH);
                    newParentPath = newParentPath.replace(pathPieceToRemove, "");
                    JsonObjectBuilder jsoBuilder = Json.createObjectBuilder();
                    jso.forEach(jsoBuilder::add);
                    jsoBuilder.add(RVM_PARENT_REGION_PATH, newParentPath);
                    jso = jsoBuilder.build();
                    
                    // SAVE THE UPDATED FILE
                    saveJsonFile(jso, file.getAbsolutePath(), true);
                }
            }
        }
    }
    private void promoteDescendantFiles(RegioVincoMapMakerData data) {
        String workFilePath = data.getWorkFilePath();
        workFilePath = this.removeFileExt(workFilePath);
        
        File sourceDir = new File(workFilePath);
        File[] files = sourceDir.listFiles();
        String destPath = WORK_PATH + data.getParentRegionPath();
        File destDir = new File(destPath);
        for (File sourceFile : files) {
            try {
                if (sourceFile.isDirectory()) {
                    FileUtils.moveDirectoryToDirectory(sourceFile, destDir, true);
                }
                else {
                    FileUtils.moveFileToDirectory(sourceFile, destDir, false);
                }
            }catch(Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Promoting Children Failed");
                alert.setContentText("An error occured moving the child work files");
                alert.showAndWait();
            }
        }       
    }
    public void promoteAllDescendants(RegioVincoMapMakerData data) {
        try {             
            // AND THEN PROMOTE ALL THE PARENT PATHS IN THE JSON FILES
            promoteDescendantWork(data);    
            
            // FIRST MOVE ALL THE FILES IN THE CHILD DIRECTORY. NOTE, WE 
            // DON'T HAVE TO DO THIS FOR GRANDCHILD DIRECTORIES AS THEY WILL
            // MOVE WITH THE PARENT DIRECTORY
            promoteDescendantFiles(data);   
        } catch(IOException ioe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("An Error Occurred");
            alert.setContentText("Note that an error occured exporting");
            alert.showAndWait();
            DebugDisplay.appendDebugText(ioe.getStackTrace().toString());
        }
    }
    private void promoteDescendantWork(RegioVincoMapMakerData data) throws IOException {
        String workFilePath = data.getWorkFilePath();
        workFilePath = this.removeFileExt(workFilePath);
        File workDir = new File(workFilePath);
        promoteWork(data, workDir);
    }
    public void deleteCurrentRegionWorkFile(RegioVincoMapMakerData data) {
        String workFilePath = data.getWorkFilePath();
        File workFile = new File(workFilePath);
        workFile.delete();
    }
    public void deleteCurrentRegionWorkDir(RegioVincoMapMakerData data) {
        String workFilePath = data.getWorkFilePath();
        workFilePath = this.removeFileExt(workFilePath);
        File workDirFile = new File(workFilePath);
        if (workDirFile.exists() && workDirFile.isDirectory())
            deleteDir(workDirFile);
    }
    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDir(file);
            }
            file.delete();
        }
        boolean deleteSuccess = dir.delete();
        DebugDisplay.appendDebugText("deleteSuccess for " + dir + ": " + deleteSuccess);
    }
    public void updatePathInFile(File file, String oldPathPiece, String newPathPiece)
                        throws IOException {
        if (file.getName().endsWith(WORK_FILE_EXT)) {
            // LOAD THE DATA FILE
            JsonObject jso = loadJSONFile(file.getAbsolutePath());
            
            // CHANGE THE PARENT REGION PATH
            String newParentPath = jso.getString(RVM_PARENT_REGION_PATH);
            newParentPath = newParentPath.replace(oldPathPiece, newPathPiece);
            JsonObjectBuilder jsoBuilder = Json.createObjectBuilder();
            jso.forEach(jsoBuilder::add);
            jsoBuilder.add(RVM_PARENT_REGION_PATH, newParentPath);
            jso = jsoBuilder.build();
            
            // SAVE THE UPDATED FILE
            saveJsonFile(jso, file.getAbsolutePath(), true);
        }        
    }
    public void updateAllDescendantPaths(RegioVincoMapMakerData data, 
            File workDir, String oldPathStart, String newPathStart) throws IOException {
        File[] files = workDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                updateAllDescendantPaths(data, file, oldPathStart, newPathStart);
            }
            else {
                updatePathInFile(file, oldPathStart, newPathStart);
            }
        }        
    }    
    public void updateAllDescendants(RegioVincoMapMakerData data, 
            File workDir, String oldRegionName, String newRegionName) throws IOException {
        String pathPieceToRemove = "/" + oldRegionName + "/";
        String pathPieceToInsert = "/" + newRegionName + "/";
        File[] files = workDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                updateAllDescendants(data, file, oldRegionName, newRegionName);
            }
            else {
                updatePathInFile(file, pathPieceToRemove, pathPieceToInsert);
            }
        }        
    }
}
