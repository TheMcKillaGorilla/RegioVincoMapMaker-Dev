package rvmm.data;

import java.util.Iterator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import static rvmm.data.RVMM_Constants.DEFAULT_SUBREGION_FILL_COLOR;

public class SubregionPrototype {
    public static final String NAME = "name";
    public static final String CAPITAL = "capital";
    public static final String LEADER = "leader";
    public static final String FLAG_LINK = "flagLink";
    public static final String VALID_FLAG = "validFlag";
    public static final String LANDMARKS = "landmarksList";
    public static final String SELF = "self";
    private StringProperty name;
    private StringProperty capital;
    private StringProperty leader;
    private StringProperty flagLink;
    private BooleanProperty isTerritory;
    private BooleanProperty validFlag;
    private ObjectProperty<SubregionPrototype> self;
    ObservableList<String> landmarks;
    private StringProperty landmarksList;
    ObservableList<Polygon> polygons;
    Color greyscaleColor;
    double minX;
    double maxX;
    double minY;
    double maxY;

    public SubregionPrototype(){
        name = new SimpleStringProperty("");
        capital = new SimpleStringProperty("");
        leader = new SimpleStringProperty("");
        flagLink = new SimpleStringProperty("");
        isTerritory = new SimpleBooleanProperty(false);
        validFlag = new SimpleBooleanProperty(false);
        self = new SimpleObjectProperty(this);
        landmarks = FXCollections.observableArrayList();
        landmarksList = new SimpleStringProperty("");
        polygons = FXCollections.observableArrayList();
        greyscaleColor = DEFAULT_SUBREGION_FILL_COLOR;
    }

    public SubregionPrototype(String initName, 
            String initCapital, 
            String initLeader, 
            String initFlagLink,
            Boolean initIsTerritory,
            ObservableList initLandmarks){
        this();
        setName(initName);
        setCapital(initCapital);
        setLeader(initLeader);
        setFlagLink(initFlagLink);
        setIsTerritory(initIsTerritory);
        loadLandmarks(initLandmarks);
    }
    
    public boolean contains(double x, double y) {
        if ((x >= minX)
                && (x <= maxX)
                && (y >= minY)
                && (y <= maxY)) {
            Iterator<Polygon> it = polygons.iterator();
            while (it.hasNext()) {
                Polygon poly = it.next();
                if (poly.contains(x, y))
                    return true;
            } 
        }
        return false;
    }
    
    public double getMinX() {
        return minX;
    }
    
    public double getMaxX() {
        return maxX;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public void loadBounds() {
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        Iterator<Polygon> it = polygons.iterator();
        while (it.hasNext()) {
            Polygon poly = it.next();
            ObservableList<Double> points = poly.getPoints();
            Iterator<Double> pointIt = points.iterator();
            while (pointIt.hasNext()) {
                double x = pointIt.next();
                double y = pointIt.next();
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
    }
    
    public SubregionPrototype getSelf() {
        return this;
    }
    
    public void setSelf(SubregionPrototype update) {
        self.set(update);
    }
    
    public ObjectProperty selfProperty() {
        return self;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public StringProperty nameProperty() {
        return name;
    }
    
    public String getLandmarksList() {
        return landmarksList.get();
    }
    
    public void setLandmarksList(String value) {
        landmarksList.set(value);
    }
    
    public StringProperty landmarksListProperty() {
        return landmarksList;
    }
    
    public String getCapital() {
        return capital.get();
    }

    public void setCapital(String value) {
        capital.set(value);
    }

    public StringProperty capitalProperty() {
        return capital;
    }

    public String getLeader() {
        return leader.get();
    }

    public void setLeader(String value) {
        leader.set(value);
    }

    public StringProperty leaderProperty() {
        return leader;
    }
    
    public String getFlagLink() {
        return flagLink.get();
    }
    
    public void setFlagLink(String value) {
        flagLink.set(value);
    }
    
    public StringProperty flagLinkProperty() {
        return flagLink;
    }
    
    public boolean getIsTerritory() {
        return isTerritory.get();
    }
    
    public void setIsTerritory(boolean value) {
        isTerritory.set(value);
    }
    
    public BooleanProperty isTerritoryProperty() {
        return isTerritory;
    }
    
    public boolean getValidFlag() {
        return validFlag.get();
    }
    
    public void setValidFlag(boolean value) {
        validFlag.set(value);
    }
    
    public BooleanProperty validFlagProperty() {
        return validFlag;
    }
    
    public void clearLandmarks() {
        landmarks.clear();
    }

    public void addLandmark(String landmarkToAdd) {
        landmarks.add(landmarkToAdd);
        setLandmarksList(landmarks.toString());
    }
    
    public void removeLandmark(String landmarkToRemove) {
        landmarks.remove(landmarkToRemove);
        setLandmarksList(landmarks.toString());
    }

    public Iterator<String> landmarksIterator() {
        return landmarks.iterator();
    }
    
    public ObservableList<String> cloneLandmarks() {
        ObservableList<String> clonedLandmarks = FXCollections.observableArrayList();
        for (String s : landmarks) {
            clonedLandmarks.add(s);
        }        
        return clonedLandmarks;
    }
    
    public void loadLandmarks(ObservableList<String> initLandmarks) {
        clearLandmarks();
        for (String s : initLandmarks) {
            addLandmark(s);
        }        
    }
    
    public Color getGreyscaleColor() {
        return greyscaleColor;
    }
    
    public void setGreyscaleColor(Color initGreyscaleColor) {
        greyscaleColor = initGreyscaleColor;
        setFill(greyscaleColor);
    }
    
    public Color getFill() {        
        return (Color) polygons.get(0).getFill();
    }
    
    public void setFill(Color initFill) {
        for (Polygon p : polygons) {
            p.setFill(initFill);
        }
    }
    
    public Color getBorderColor() {
        return (Color)polygons.get(0).getStroke();
    }
    
    public void setBorderColor(Color borderColor) {
        for (Polygon p : polygons) {
            p.setStroke(borderColor);
        }
    }
    
    public double getBorderThickness() {
        return polygons.get(0).getStrokeWidth();
    }
    
    public void setBorderThickness(double orderThickness) {
        for (Polygon p : polygons) {
            p.setStrokeWidth(orderThickness);
        }
    }
    
    public void loadEditableFields(SubregionPrototype sub) {
        setName(sub.getName());
        setCapital(sub.getCapital());
        setLeader(sub.getLeader());
        setFlagLink(sub.getFlagLink());
        loadLandmarks(sub.cloneLandmarks());
    }
    
    public SubregionPrototype partialClone() {
        SubregionPrototype partialClone = new SubregionPrototype(name.get(), capital.get(), leader.get(), flagLink.get(), isTerritory.get(), landmarks);
        return partialClone;
    }

    public void addPolygon(Polygon polygonToAdd) {
        polygons.add(polygonToAdd);
    }

    public Iterator<Polygon> polygonsIterator() {
        return polygons.iterator();
    }
    
    public String toString() {
        return name + ", (capital: " + capital + ", leader: " + leader + ", flagLink: " + flagLink + ")";
    }
}