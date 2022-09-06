package rvmm.transactions;

import java.util.HashMap;
import javafx.collections.ObservableList;
import jtps.jTPS_Transaction;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;

public class ChangeLeaders_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    HashMap<String, String> oldLeaders;
    HashMap<String, String> newLeaders;
    
    public ChangeLeaders_Transaction(RegioVincoMapMakerData initData,
                                        HashMap<String, String> initOldLeaders,
                                        HashMap<String, String> initNewLeaders) {
        data = initData;
        oldLeaders = initOldLeaders;
        newLeaders = initNewLeaders;
    }

    public void doTransaction() {
        ObservableList<SubregionPrototype> subregions = data.getSubregions();
        for (SubregionPrototype subregion : subregions) {
            String subregionName = subregion.getName();
            if (newLeaders.containsKey(subregionName)) {
                String newLeader = newLeaders.get(subregionName);
                subregion.setLeader(newLeader);
            }
        }
    }

    public void undoTransaction() {
        ObservableList<SubregionPrototype> subregions = data.getSubregions();
        for (SubregionPrototype subregion : subregions) {
            String subregionName = subregion.getName();
            if (oldLeaders.containsKey(subregionName)) {
                String newLeader = oldLeaders.get(subregionName);
                subregion.setLeader(newLeader);
            }
        }
    }
}