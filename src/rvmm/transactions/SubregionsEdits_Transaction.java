package rvmm.transactions;

import java.util.HashMap;
import java.util.Iterator;
import jtps.jTPS_Transaction;
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
    String oldLandmarksDescription;
    String newLandmarksDescription;

    public SubregionsEdits_Transaction( RegioVincoMapMakerData initData,
                                        HashMap<SubregionPrototype, SubregionPrototype> initOldValues, 
                                        HashMap<SubregionPrototype, SubregionPrototype> initNewValues,
                                        String initOldLandmarksDescription,
                                        String initNewLandmarksDescription) {
        data = initData;
        oldValues = initOldValues;
        newValues = initNewValues;
        oldLandmarksDescription = initOldLandmarksDescription;
        newLandmarksDescription = initNewLandmarksDescription;
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
        data.setLandmarksDescription(newLandmarksDescription);
        data.refreshSubregions();
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
        data.setLandmarksDescription(oldLandmarksDescription);
        data.refreshSubregions();
    }
}
