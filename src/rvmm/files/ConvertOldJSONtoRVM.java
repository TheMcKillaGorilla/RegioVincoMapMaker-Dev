package rvmm.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import static rvmm.data.MapDataKeys.RVM_BLUE;
import static rvmm.data.MapDataKeys.RVM_CAPITAL;
import static rvmm.data.MapDataKeys.RVM_GREEN;
import static rvmm.data.MapDataKeys.RVM_GREYSCALE_COLOR;
import static rvmm.data.MapDataKeys.RVM_HEIGHT;
import static rvmm.data.MapDataKeys.RVM_LEADER;
import static rvmm.data.MapDataKeys.RVM_PARENT_REGION_PATH;
import static rvmm.data.MapDataKeys.RVM_POLYGON_ARRAY;
import static rvmm.data.MapDataKeys.RVM_RED;
import static rvmm.data.MapDataKeys.RVM_REGION_NAME;
import static rvmm.data.MapDataKeys.RVM_SCALE;
import static rvmm.data.MapDataKeys.RVM_SUBREGIONS;
import static rvmm.data.MapDataKeys.RVM_TRANSLATE_X;
import static rvmm.data.MapDataKeys.RVM_TRANSLATE_Y;
import static rvmm.data.MapDataKeys.RVM_WIDTH;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;

/**
 *
 * @author McKillaGorilla
 */
public class ConvertOldJSONtoRVM {
    public static void main(String[] args) {
        String DIR = "./work/The World/";
        String mergedRVMFilePath = DIR + "Oceania.rvm";
        String mergedJSONFilePath = DIR + "Oceania.json";
        RegioVincoFiles rvFiles = new RegioVincoFiles();

        try {
            // LOAD THE JSON FILE WITH ALL THE DATA
            JsonObject json = rvFiles.loadJSONFile(mergedRVMFilePath);

            // GET THE REGION DATA
            String name = json.getString(RVM_REGION_NAME);
            String parentRegionPath = json.getString(RVM_PARENT_REGION_PATH);
            double mapWidth = json.getJsonNumber(RVM_WIDTH).doubleValue();
            double mapHeight = json.getJsonNumber(RVM_HEIGHT).doubleValue();
            double mapScale = json.getJsonNumber(RVM_SCALE).doubleValue();
            double mapTranslateX = json.getJsonNumber(RVM_TRANSLATE_X).doubleValue();
            double mapTranslateY = json.getJsonNumber(RVM_TRANSLATE_Y).doubleValue();
            ArrayList<SubregionPrototype> subregions = loadSubregions(json);
            Iterator<SubregionPrototype> subregionsIt = subregions.iterator();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (subregionsIt.hasNext()) {
                SubregionPrototype subregion = subregionsIt.next();
                JsonObject subregionJSO = makeSubregionJSO(subregion);
                arrayBuilder.add(subregionJSO);
            }
                
            JsonArray subregionsJSA = arrayBuilder.build();
            JsonObject dataJSO = Json.createObjectBuilder()
                .add(RVM_REGION_NAME, name)
                .add(RVM_PARENT_REGION_PATH, parentRegionPath)
                .add(RVM_WIDTH, mapWidth)
                .add(RVM_HEIGHT, mapHeight)
                .add(RVM_SCALE, mapScale)
                .add(RVM_TRANSLATE_X, mapTranslateX)
                .add(RVM_TRANSLATE_Y, mapTranslateY)
                .add(RVM_SUBREGIONS, subregionsJSA)
                .build();
            rvFiles.saveJsonFile(dataJSO, mergedJSONFilePath, false);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static Color loadColor(JsonObject colorJSO) {
        double red = colorJSO.getJsonNumber(RVM_RED).doubleValue();
        double green = colorJSO.getJsonNumber(RVM_GREEN).doubleValue();
        double blue = colorJSO.getJsonNumber(RVM_BLUE).doubleValue();
        Color color = Color.color(red, green, blue);
        return color;
    }

    public static ArrayList<SubregionPrototype> loadSubregions(JsonObject jso) {
        ArrayList<SubregionPrototype> subregions = new ArrayList();
        JsonArray subregionsJSA = jso.getJsonArray(RVM_SUBREGIONS);
        for (int i = 0; i < subregionsJSA.size(); i++) {
            JsonObject subregionJSO = subregionsJSA.getJsonObject(i);
            SubregionPrototype subregionToAdd = loadSubregion(subregionJSO);
            subregionToAdd.loadBounds();
            subregions.add(subregionToAdd);
        }
        return subregions;
    }    

    public static SubregionPrototype loadSubregion(JsonObject subregionJSO) {
        SubregionPrototype subregion = new SubregionPrototype();
        String name = subregionJSO.getString(RVM_REGION_NAME);
        subregion.setName(name);
        String capital = subregionJSO.getString(RVM_CAPITAL);
        subregion.setCapital(capital);
        String leader = subregionJSO.getString(RVM_LEADER);
        subregion.setLeader(leader);
        JsonArray polygonsJSA = subregionJSO.getJsonArray(RVM_POLYGON_ARRAY);
        for (int i = 0; i < polygonsJSA.size(); i++) {
            JsonArray pointsJSA = polygonsJSA.getJsonArray(i);
            Polygon polygonToAdd = new Polygon();
            ObservableList<Double> points = polygonToAdd.getPoints();
            for (int j = 0; j < pointsJSA.size(); j+=2) {
                double x = pointsJSA.getJsonObject(j).getJsonNumber("point").doubleValue();
                double y = pointsJSA.getJsonObject(j+1).getJsonNumber("point").doubleValue();
                points.add(x);
                points.add(y);
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
    
    public static double getDataAsDouble(JsonObject json, String dataName) {
	JsonValue value = json.get(dataName);
	JsonNumber number = (JsonNumber)value;
	return number.bigDecimalValue().doubleValue();	
    }
       
    public static JsonArray makePolygonJSA(Polygon polygon) {
        JsonArrayBuilder arrayCoordBuilder = Json.createArrayBuilder();
        ObservableList<Double> pointsToSave = polygon.getPoints();
        for(int i = 0; i < pointsToSave.size(); i+=2){
            double pointX = pointsToSave.get(i);
            arrayCoordBuilder.add(pointX);
            double pointY = pointsToSave.get(i+1);
            arrayCoordBuilder.add(pointY);
        }
        return arrayCoordBuilder.build(); 
    }    
    public static JsonArray makePolygonsJSA(SubregionPrototype subregion) {
        JsonArrayBuilder polygonsArrayBuilder = Json.createArrayBuilder();
        Iterator<Polygon> polygonsIt = subregion.polygonsIterator();
        while (polygonsIt.hasNext()) {
            Polygon polygon = polygonsIt.next();
            JsonArray polygonJSA = makePolygonJSA(polygon);
            polygonsArrayBuilder.add(polygonJSA);
        }
        return polygonsArrayBuilder.build();
    }    
    public static JsonObject makeGreyscaleColorJSO(SubregionPrototype subregion) {
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
    public static JsonObject makeSubregionJSO(SubregionPrototype subregion) {
        String subregionName = subregion.getName();
        String capitalName = subregion.getCapital();
        String leaderName = subregion.getLeader();
        JsonObject greyscaleColorJSO = makeGreyscaleColorJSO(subregion);
        JsonArray polygonsJSA = makePolygonsJSA(subregion);
        JsonObjectBuilder jso = Json.createObjectBuilder();
        return jso
                .add(RVM_REGION_NAME,subregionName)
                .add(RVM_CAPITAL,capitalName)
                .add(RVM_LEADER,leaderName)
                .add(RVM_GREYSCALE_COLOR, greyscaleColorJSO)
                .add(RVM_POLYGON_ARRAY, polygonsJSA)
                .build();        
    }
    public static JsonArray makeSubregionsJSA(RegioVincoMapMakerData data) {
        Iterator<SubregionPrototype> subregionsIt = data.subregionsIterator();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            JsonObject subregionJSO = makeSubregionJSO(subregion);
            arrayBuilder.add(subregionJSO);
        }
        return arrayBuilder.build();         
    }
}
