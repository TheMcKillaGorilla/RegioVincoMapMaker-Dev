package rvmm.transactions;

import jtps.jTPS_Transaction;
import rvmm.data.RegioVincoMapMakerData;

public class ChangeRegionName_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    String oldRegionName;
    String newRegionName;
    
    public ChangeRegionName_Transaction(RegioVincoMapMakerData initData,
                                        String initOldRegionName, String initNewRegionName) {
        data = initData;
        oldRegionName = initOldRegionName;
        newRegionName = initNewRegionName;
    }
    
    public void doTransaction() {
        data.changeRegionName(newRegionName);
    }
    
    public void undoTransaction() {
        data.changeRegionName(oldRegionName);        
    }
}