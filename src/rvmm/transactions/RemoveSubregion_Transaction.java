package rvmm.transactions;

import jtps.jTPS_Transaction;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;

public class RemoveSubregion_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    int subregionIndex;
    SubregionPrototype subregion;
    
    public RemoveSubregion_Transaction(RegioVincoMapMakerData initData,
                                        int initSubregionIndex,
                                        SubregionPrototype initSubregion) {
        data = initData;
        subregionIndex = initSubregionIndex;
        subregion = initSubregion;
    }
    
    public void doTransaction() {
        data.removeSubregion(subregion);
    }
    
    public void undoTransaction() {
        data.restoreSubregion(subregionIndex, subregion);
    }
}