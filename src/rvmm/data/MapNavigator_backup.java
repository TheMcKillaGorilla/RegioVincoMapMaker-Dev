package rvmm.data;

import djf.modules.AppGUIModule;
import java.util.Iterator;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_DEBUG_TEXT_AREA;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_MAP_PANE;
import static rvmm.data.MapNavigator_backup.MapState.MAP_DRAG;
import static rvmm.data.MapNavigator_backup.MapState.MAP_SELECT;
import static rvmm.data.RVMM_Constants.FILL_COLOR_SELECTED_SUBREGION;
import static rvmm.data.RVMM_Constants.DEFAULT_MAP_HEIGHT;
import static rvmm.data.RVMM_Constants.DEFAULT_SCALE;
import static rvmm.data.RVMM_Constants.DEFAULT_TRANSLATE;
import static rvmm.data.RVMM_Constants.SCALE_FACTOR;
import rvmm.workspace.DebugDisplay;

public class MapNavigator_backup {

    RegioVincoMapMakerApp app;
    MapState currentState;
    final double DEFAULT_LINE_THICKNESS = 1.0;

    // ZOOM LEVEL
    double scale = 1.0f;
    
    // USED DURING MOUSE DRAGGING
    double startX, startY;
    double cumulativeDragX, cumulativeDragY;

    // VIEWPORT
    double viewportWidth, viewportHeight;
    double viewportTranslateX, viewportTranslateY;
    double viewportMousePercentX, viewportMousePercentY;

    // THESE ARE VALUES USING WORLD COORDINATES
    double worldWidth, worldHeight;
    double worldMouseX, worldMouseY;
    double worldViewportWidth, worldViewportHeight;
    double worldViewportPaddingLeft, worldViewportPaddingTop;
    double worldViewportX, worldViewportY;
    
    SubregionPrototype mousedOverSubregion;

    enum MapState {
        MAP_SELECT,
        MAP_DRAG
    }

    public MapNavigator_backup(RegioVincoMapMakerApp initApp) {
        app = initApp;
        currentState = MapState.MAP_SELECT;
    }
    
    public void displayStats() {
        AppGUIModule gui = app.getGUIModule();
        TextArea debugTextArea = (TextArea)gui.getGUINode(RVMM_DEBUG_TEXT_AREA);
        debugTextArea.appendText("currentState: " + currentState + "\n");
        debugTextArea.appendText("scale: " + scale + "\n");
        debugTextArea.appendText("startX/startY: " + startX + "/" + startY + " (at start of mouse dragging)\n");
        debugTextArea.appendText("viewportWidth/viewportHeight: " + viewportWidth + "/" + viewportHeight + "\n");
        debugTextArea.appendText("viewportTranslateX/viewportTranslateY: " + viewportTranslateX + "/" + viewportTranslateY + "\n");
        debugTextArea.appendText("viewportMousePercentX/viewportMousePercentY: " + viewportMousePercentX + "/" + viewportMousePercentY + "\n");
        debugTextArea.appendText("worldWidth/worldHeight: " + worldWidth + "/" + worldHeight + "\n");
        debugTextArea.appendText("worldMouseX/worldMouseY: " + worldMouseX + "/" + worldMouseY + "\n");
        debugTextArea.appendText("worldViewportWidth/worldViewportHeight: " + worldViewportWidth + "/" + worldViewportHeight + "\n");
        debugTextArea.appendText("worldViewportPaddingLeft/worldViewportPaddingTop: " + worldViewportPaddingLeft + "/" + worldViewportPaddingTop + "\n");
        debugTextArea.appendText("worldViewportX/worldViewportY: " + worldViewportX + "/" + worldViewportY + "\n");
        debugTextArea.appendText("\n");
    }

    
    public double getStartX() {
        return startX;
    }
    
    public double getStartY() {
        return startY;
    }

    public double getScale() {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        return map.scaleXProperty().doubleValue();
    }

    public double getMapTranslateX() {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        return map.translateXProperty().doubleValue();
    }

    public double getMapTranslateY() {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        return map.translateYProperty().doubleValue();
    }
    
    public double getCumulativeDragX() {
        return cumulativeDragX;
    }
    
    public double getCumulativeDragY() {
        return cumulativeDragY;
    }

    public void setMapScale(double mapScale) {
        ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE)).setScaleX(mapScale);
        ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE)).setScaleY(mapScale);
    }

    public void setMapTranslate(double mapTranslateX, double mapTranslateY) {
        ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE)).setTranslateX(mapTranslateX);
        ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE)).setTranslateY(mapTranslateY);
    }

    public double calcXPerc(double x) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        double translateX = map.translateXProperty().doubleValue();
        double xMax = (scale - 1.0) * (map.getWidth() / 2.0);
        double leftX = (xMax - translateX) / scale;
        double percentX = (x - leftX) / (map.getWidth() / scale);
        return percentX;
    }

    public double calcYPerc(double y) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        double translateY = map.translateYProperty().doubleValue();
        double yMax = (scale - 1.0) * (map.getWidth() / 2.0);
        double leftY = (yMax - translateY) / scale;
        double percentY = (y - leftY) / (map.getHeight() / scale);
        return percentY;
    }

    /**
     * This calculates and returns the x pixel value that corresponds to the
     * xCoord longitude argument.
     */
    public double longToX(double longCoord) {
        double unitDegree = DEFAULT_MAP_HEIGHT / 180;
        double newLongCoord = (longCoord + 180) * unitDegree;
        return newLongCoord;
    }

    /**
     * This calculates and returns the y pixel value that corresponds to the
     * yCoord latitude argument.
     */
    public double latToY(double latCoord) {
        // WE ONLY WANT POSITIVE COORDINATES, SO SHIFT BY 90
        double unitDegree = DEFAULT_MAP_HEIGHT / 180;
        double newLatCoord = (latCoord + 90) * unitDegree;
        return DEFAULT_MAP_HEIGHT - newLatCoord;
    }

    public void reset() {
        resetLocation();
        resetScale();
        DebugDisplay.appendDebugText("map scale: " + this.scale);
    }

    public void resetLocation() {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        map.translateXProperty().set(DEFAULT_TRANSLATE);
        map.translateYProperty().set(DEFAULT_TRANSLATE);
    }

    public void resetScale() {
        scale = DEFAULT_SCALE;
        scaleMap(scale);
    }

    private void scaleMap(double zoomScale) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        map.scaleXProperty().setValue(zoomScale);
        map.scaleYProperty().setValue(zoomScale);
    }

    public void moveMap(double x, double y) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        map.translateXProperty().set(x);
        map.translateYProperty().set(y);
    }

    public void scale(double scaleMultiplier) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        double translateX = map.translateXProperty().doubleValue();
        double translateY = map.translateYProperty().doubleValue();
        if ((scale * scaleMultiplier) >= 1.0) {
            scale *= scaleMultiplier;
            moveMap(0, 0);
            scaleMap(scale);
            translateX *= scaleMultiplier;
            translateY *= scaleMultiplier;
            moveMap(translateX, translateY);
            clamp();
            //adjustLineThickness();
        }
    }

    public void clamp() {
/*        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);

        // FIRST CLAMP X
        double xMax = (scale - 1.0) * (map.getWidth() / 2.0);
        double xTranslate = map.translateXProperty().doubleValue();
        if (xTranslate > xMax) {
            xTranslate = xMax;
        }
        //else if (xTranslate < adjustedX) xTranslate = adjustedX;
        map.translateXProperty().setValue(xTranslate);

        // THEN Y
        double yMax = (scale - 1.0) * (map.getHeight() / 2.0);
        double yTranslate = map.translateYProperty().doubleValue();
        if (yTranslate > yMax) {
            yTranslate = yMax;
        }
        map.translateYProperty().setValue(yTranslate);
*/
    }

    public void move(double xInc, double yInc) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);

        // FIRST X, WITH CLAMPING AT THE EDGES
        double xTranslate = map.translateXProperty().doubleValue() + xInc;
        map.translateXProperty().setValue(xTranslate);

        double yTranslate = map.translateYProperty().doubleValue() + yInc;
        map.translateYProperty().setValue(yTranslate);
        
        // MAKE SURE WE'RE NOT OUT OF BOUNDS
        //clamp();
    }

    public double xToScaledX(double x) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        double translateX = map.translateXProperty().doubleValue();
        double diffX = x - translateX;
        return diffX / scale;
    }

    public double yToScaledY(double y) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        double translateY = map.translateYProperty().doubleValue();
        double diffY = y - translateY;
        return diffY / scale;
    }

    public void startMapDrag(double x, double y) {
        startX = x;
        startY = y;
        cumulativeDragX = 0;
        cumulativeDragY = 0;
        System.out.println("(startX, startY) set to: (" + startX + ", " + startY + ")");
        currentState = MAP_DRAG;
        app.getGUIModule().getWindow().getScene().setCursor(Cursor.MOVE);
    }

    public void updateMapDrag(double x, double y) {
        if (currentState == MAP_DRAG) {
            double diffX = x - startX;
            double diffY = y - startY;
            cumulativeDragX += diffX;
            cumulativeDragY += diffY;
            this.move(diffX, diffY);
            AppGUIModule gui = app.getGUIModule();
            TextArea debugTextArea = (TextArea)gui.getGUINode(RVMM_DEBUG_TEXT_AREA);
            debugTextArea.appendText("dragging map by (" + diffX + ", " + diffY + ") from (" + startX + ", " + startY + ") to (" + x + ", " + y + ")\n");
        }
    }

    public void endMapDrag() {
        currentState = MAP_SELECT;
        app.getGUIModule().getWindow().getScene().setCursor(Cursor.DEFAULT);
    }

    public void zoomInOnPoint(double x, double y) {
        zoomOnPoint(SCALE_FACTOR, x, y);
    }

    public void zoomOutOnPoint(double x, double y) {
        zoomOnPoint(1/SCALE_FACTOR, x, y);
    }

    private void zoomOnPoint(double factor, double mouseX, double mouseY) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        double newScale = factor * scale;
        if (newScale >= 1.0) {
            updateStats(newScale, mouseX, mouseY);
            double diffX = (viewportMousePercentX * (viewportWidth / newScale));
            double diffY = (viewportMousePercentY * (viewportHeight / newScale));
            double newWorldViewportX = worldMouseX - diffX;
            double newWorldViewportY = worldMouseY - diffY;
            double newWorldViewportPaddingLeft = (newScale - 1.0) * (viewportWidth / 2.0);
            double newWorldViewportPaddingTop = (newScale - 1.0) * (viewportHeight / 2.0);
            viewportTranslateX = newWorldViewportPaddingLeft - (newWorldViewportX * newScale);
            viewportTranslateY = newWorldViewportPaddingTop - (newWorldViewportY * newScale);
            map.translateXProperty().setValue(viewportTranslateX);
            map.translateYProperty().setValue(viewportTranslateY);
            
            scale(factor);
            this.update(mouseX, mouseY);
            displayStats();
        }
    }
    
    public SubregionPrototype getSubregionAt(double mouseX, double mouseY) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        ObservableList<SubregionPrototype> subregions = data.getSubregions();
        Iterator<SubregionPrototype> it = subregions.iterator();
        while (it.hasNext()) {
            SubregionPrototype subregion = it.next();
            if (subregion.contains(mouseX, mouseY)) {
                return subregion;
            }
        }
        return null;
    }

    public void highlightSubregion(double mouseX, double mouseY) {
        SubregionPrototype subregion = this.getSubregionAt(mouseX, mouseY);
        if (subregion != mousedOverSubregion) {
            unhighlightMousedOverSubregion();
            mousedOverSubregion = subregion;
            if (subregion != null) {
                Iterator<Polygon> polyIt = mousedOverSubregion.polygonsIterator();
                while (polyIt.hasNext()) {
                    Polygon poly = polyIt.next();
                    poly.setFill(FILL_COLOR_SELECTED_SUBREGION);
                }
            }
        }
    }

    public void highlightSubregion(SubregionPrototype subregion) {
        if (subregion != null) {
            Iterator<Polygon> polyIt = subregion.polygonsIterator();
            while (polyIt.hasNext()) {
                Polygon poly = polyIt.next();
                poly.setFill(FILL_COLOR_SELECTED_SUBREGION);
            }
        }
    }

    public void unhighlightSubregion(SubregionPrototype subregion) {
        if (subregion != null) {
            Iterator<Polygon> polyIt = subregion.polygonsIterator();
            while (polyIt.hasNext()) {
                Polygon poly = polyIt.next();
                poly.setFill(subregion.getGreyscaleColor());
            }
        }
    }

    public void unhighlightMousedOverSubregion() {
        if (mousedOverSubregion != null) {
            Iterator<Polygon> polyIt = mousedOverSubregion.polygonsIterator();
            while (polyIt.hasNext()) {
                Polygon poly = polyIt.next();
                poly.setFill(mousedOverSubregion.getGreyscaleColor());
            }
        }
        mousedOverSubregion = null;
    }

    public void update(double mouseX, double mouseY) {
        updateStats(scale, mouseX, mouseY);
        highlightSubregion(mouseX, mouseY);
    }

    private void updateStats(double newScale, double mouseX, double mouseY) {
        Pane map = (Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE);
        scale = map.scaleXProperty().doubleValue();
        viewportWidth = map.widthProperty().doubleValue();
        viewportHeight = map.heightProperty().doubleValue();
        viewportTranslateX = map.translateXProperty().doubleValue();
        viewportTranslateY = map.translateYProperty().doubleValue();

        worldWidth = map.heightProperty().doubleValue() * 2.0;
        worldHeight = map.heightProperty().doubleValue();
        worldMouseX = mouseX;
        worldMouseY = mouseY;
        worldViewportWidth = viewportWidth / newScale;
        worldViewportHeight = viewportHeight / newScale;
        worldViewportPaddingLeft = (newScale - 1.0) * (map.getWidth() / 2.0);
        worldViewportPaddingTop = (newScale - 1.0) * (map.getHeight() / 2.0);
        worldViewportX = (worldViewportPaddingLeft - viewportTranslateX) / newScale;
        worldViewportY = (worldViewportPaddingTop - viewportTranslateY) / newScale;

        viewportMousePercentX = (worldMouseX - worldViewportX) / worldViewportWidth;
        viewportMousePercentY = (worldMouseY - worldViewportY) / worldViewportHeight;
    }

    public Polygon getPolygonAt(double x, double y) {
        Polygon poly = getMousedOverPolygon(x, y);
        if (poly == null) {
            return null;
        } else {
            return poly;
        }
    }

    public Polygon getMousedOverPolygon(double x, double y) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        Iterator<SubregionPrototype> subregionsIt = data.subregionsIterator();
        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            Iterator<Polygon> polygonsIt = subregion.polygonsIterator();
            while (polygonsIt.hasNext()) {
                Polygon poly = polygonsIt.next();
                double localX = x - poly.getLayoutX();
                double localY = y - poly.getLayoutY();
                if (poly.contains(x, y)) {
                    return poly;
                }
            }
        }
        return null;
    }
}
