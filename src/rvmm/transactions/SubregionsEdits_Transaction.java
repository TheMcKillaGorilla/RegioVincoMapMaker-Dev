package rvmm.transactions;

import java.util.HashMap;
import java.util.Iterator;
import jtps.jTPS_Transaction;
import rvmm.data.MapPropertyType;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;

/**
 *
 * @author McKillaGorilla
 */
public class SubregionsEdits_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    HashMap<SubregionPrototype, SubregionPrototype> oldValues;
    HashMap<SubregionPrototype, SubregionPrototype> newValues;
    HashMap<MapPropertyType, String> oldMapProperties;
    HashMap<MapPropertyType, String> newMapProperties;

    public SubregionsEdits_Transaction( RegioVincoMapMakerData initData,
                                        HashMap<SubregionPrototype, SubregionPrototype> initOldValues, 
                                        HashMap<SubregionPrototype, SubregionPrototype> initNewValues,
                                        HashMap<MapPropertyType, String> initOldMapProperties,
                                        HashMap<MapPropertyType, String> initNewMapProperties) {
        data = initData;
        oldValues = initOldValues;
        newValues = initNewValues;
        oldMapProperties = initOldMapProperties;
        newMapProperties = initNewMapProperties;
    }

    @Override
    public void doTransaction() {
        Iterator<SubregionPrototype> subsToUpdate = newValues.keySet().iterator();
        while (subsToUpdate.hasNext()) {
            SubregionPrototype subToUpdate = subsToUpdate.next();
            SubregionPrototype editedSub = newValues.get(subToUpdate);
            subToUpdate.setName(editedSub.getName());
            subToUpdate.setCapital(editedSub.getCapital());
            subToUpdate.setLeader(editedSub.getLeader());
            subToUpdate.setFlagLink(editedSub.getFlagLink());
            subToUpdate.setIsTerritory(editedSub.getIsTerritory());
            subToUpdate.loadLandmarks(editedSub.cloneLandmarks());
        }
        loadMapProperties(newMapProperties);
        data.refreshSubregions();
    }
    
    private void loadMapProperties(HashMap<MapPropertyType,String> props) {
        Iterator<MapPropertyType> it = props.keySet().iterator();
        while (it.hasNext()) {
            MapPropertyType key = it.next();
            String prop = props.get(key);
            data.setMapProperty(key, prop);
        }        
    }

    @Override
    public void undoTransaction() {
        Iterator<SubregionPrototype> subsToUpdate = oldValues.keySet().iterator();
        while (subsToUpdate.hasNext()) {
            SubregionPrototype subToUpdate = subsToUpdate.next();
            SubregionPrototype editedSub = oldValues.get(subToUpdate);
            subToUpdate.setName(editedSub.getName());
            subToUpdate.setCapital(editedSub.getCapital());
            subToUpdate.setLeader(editedSub.getLeader());
            subToUpdate.setFlagLink(editedSub.getFlagLink());
            subToUpdate.setIsTerritory(editedSub.getIsTerritory());
            subToUpdate.loadLandmarks(editedSub.cloneLandmarks());
        }
        loadMapProperties(oldMapProperties);
        data.refreshSubregions();
    }
}
