package rvmm.transactions;

import java.util.HashMap;
import jtps.jTPS_Transaction;
import rvmm.data.MapPropertyType;
import rvmm.data.RegioVincoMapMakerData;

public class ChangeMapProperties_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    HashMap<MapPropertyType, String> oldValues;
    HashMap<MapPropertyType, String> newValues;
    
    public ChangeMapProperties_Transaction( 
            RegioVincoMapMakerData initData, 
            HashMap<MapPropertyType, String> initOldValues,
            HashMap<MapPropertyType, String> initNewValues) {
        data = initData;
        oldValues = initOldValues;
        newValues = initNewValues;
    }

    @Override
    public void doTransaction() {
        for (MapPropertyType type : newValues.keySet()) {
            String value = newValues.get(type);
            data.setMapProperty(type, value);
        }
    }

    @Override
    public void undoTransaction() {
        for (MapPropertyType type : oldValues.keySet()) {
            String value = oldValues.get(type);
            data.setMapProperty(type, value);
        }
    }    
}