package rvmm.transactions;

import jtps.jTPS_Transaction;
import rvmm.data.MapNavigator;

/**
 *
 * @author McKillaGorilla
 */
public class ZoomOnPoint_Transaction implements jTPS_Transaction {
    MapNavigator mapNavigator;
    boolean zoomIn;
    double mouseX;
    double mouseY;
    
    public ZoomOnPoint_Transaction(MapNavigator initMapNavigator,
                                    boolean initZoomIn, double initMouseX, double initMouseY) {
        mapNavigator = initMapNavigator;
        zoomIn = initZoomIn;
        mouseX = initMouseX;
        mouseY = initMouseY;
    }

    @Override
    public void doTransaction() {
        if (zoomIn)
            mapNavigator.zoomInOnPoint(mouseX, mouseY);
        else
            mapNavigator.zoomOutOnPoint(mouseX, mouseY);
    }

    @Override
    public void undoTransaction() {
        if (!zoomIn)
            mapNavigator.zoomInOnPoint(mouseX, mouseY);
        else
            mapNavigator.zoomOutOnPoint(mouseX, mouseY);
    }
}