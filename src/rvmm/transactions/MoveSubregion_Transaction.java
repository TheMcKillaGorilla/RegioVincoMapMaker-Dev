package rvmm.transactions;

import jtps.jTPS_Transaction;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;

public class MoveSubregion_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    SubregionPrototype subregion;
    boolean moveUp;
    
    public MoveSubregion_Transaction( RegioVincoMapMakerData initData, 
            SubregionPrototype initSubregion,
            boolean initMoveUp) {
        data = initData;
        subregion = initSubregion;
        moveUp = initMoveUp;
    }

    @Override
    public void doTransaction() {
        if (moveUp)
            data.moveSubregionUp(subregion);
        else
            data.moveSubregionDown(subregion);
    }

    @Override
    public void undoTransaction() {
        if (moveUp)
            data.moveSubregionDown(subregion);
        else
            data.moveSubregionUp(subregion);
    }    
}