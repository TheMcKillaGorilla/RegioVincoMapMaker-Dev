package rvmm.workspace.controllers;

import rvmm.RegioVincoMapMakerApp;
import rvmm.data.MapNavigator;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;
import rvmm.transactions.MoveMap_Transaction;
import rvmm.transactions.ZoomOnPoint_Transaction;

public class MapNavigationController {

    RegioVincoMapMakerApp app;

    public MapNavigationController(RegioVincoMapMakerApp initApp) {
        app = initApp;
    }

    public void processResetNavigation() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        mapNavigator.reset();
    }

    public void processFitToPolygons() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        mapNavigator.fitToMap();
    }

    public void processMapMousePress(double x, double y) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        mapNavigator.startMapDrag(x, y);
    }

    public void processMapMouseDragged(double x, double y) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        mapNavigator.updateMapDrag(x, y);
    }

    public void processMapMouseRelease(double x, double y) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        mapNavigator.endMapDrag();
        //mapNavigator.move(xDiff, yDiff);
        MoveMap_Transaction moveMapTransaction = new MoveMap_Transaction(mapNavigator, x, y, mapNavigator.getCumulativeDragX(), mapNavigator.getCumulativeDragY());
        app.processTransaction(moveMapTransaction);
    }

    public void processMapMouseClicked(boolean leftButton, double x, double y) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        if (leftButton) {
            SubregionPrototype clickedSubregion = mapNavigator.getSubregionAt(x, y);
            if (clickedSubregion != null) {
                data.deselectSubregion();
            }
        }
    }

    public void processMapMouseScroll(boolean zoomIn, double x, double y) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        MapNavigator mapNavigator = data.getMapNavigator();
        ZoomOnPoint_Transaction zoomTransaction = new ZoomOnPoint_Transaction(mapNavigator, zoomIn, x, y);
        app.processTransaction(zoomTransaction);
    }

    /**
     * Respond to mouse dragging on the rendering surface, which we call canvas,
     * but is actually a Pane.
     */
    public void processMapMouseMoved(int x, int y) {
//        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
//        MapNavigator mapNavigator = data.getMapNavigator();
//        mapNavigator.update(x, y);
    }
}
