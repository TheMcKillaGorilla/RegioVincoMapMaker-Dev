/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rvmm.transactions;

import java.util.HashMap;
import jtps.jTPS_Transaction;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.VisaProperty;

/**
 *
 * @author rtmck
 */
public class UpdateVisa_Transaction implements jTPS_Transaction {
    RegioVincoMapMakerData data;
    HashMap<VisaProperty, String> oldProps;
    HashMap<VisaProperty, String> newProps;
    
    public UpdateVisa_Transaction(  RegioVincoMapMakerData initData,
                                    HashMap<VisaProperty, String> initOldProps,
                                    HashMap<VisaProperty, String> initNewProps) {
        data = initData;
        oldProps = initOldProps;
        newProps = initNewProps;
    }
    
    private void setVisaProperties(HashMap<VisaProperty, String> props) {
        for (VisaProperty key : props.keySet()) {
            String value = props.get(key);
            data.setVisaProperty(key, value);
        }                
    }

    @Override
    public void doTransaction() {
        setVisaProperties(newProps);
    }

    @Override
    public void undoTransaction() {
        setVisaProperties(oldProps);
    }    
}
