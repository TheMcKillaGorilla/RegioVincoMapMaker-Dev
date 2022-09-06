package rvmm.transactions;

import jtps.jTPS_Transaction;
import rvmm.data.MapNavigator;

/**
 *
 * @author McKillaGorilla
 */
public class MoveMap_Transaction implements jTPS_Transaction {
    MapNavigator mapNavigator;
    double incX, incY;
    double xDiff, yDiff;
    boolean firstMove;

    public MoveMap_Transaction(MapNavigator initMapNavigator,
                 double initIncX, double initIncY, double initXDiff, double initYDiff) {
        mapNavigator = initMapNavigator;
        incX = initIncX;
        incY = initIncY;
        xDiff = initXDiff;
        yDiff = initYDiff;
        firstMove = true;
    }

    @Override
    public void doTransaction() {
        if (firstMove) {
            mapNavigator.updateMapDrag(incX, incY);
            firstMove = false;
        }
        else {
            mapNavigator.move(xDiff, yDiff);
        }        
    }

    @Override
    public void undoTransaction() {
        mapNavigator.move(-xDiff, -yDiff);
    }
}