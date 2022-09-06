package rvmm.workspace;

import djf.modules.AppGUIModule;
import djf.ui.foolproof.FoolproofDesign;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_DELETE_MAP_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_EDIT_BROCHURE_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_EDIT_LEADERS_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_EDIT_PARENT_REGION_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_FIT_TO_SUBREGION_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_MOVE_DOWN_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_MOVE_UP_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_REMOVE_SUBREGION_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_RESET_NAVIGATION_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_SHUFFLE_MAP_COLORS_BUTTON;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_TOGGLE_DEBUG_CHECKBOX;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_TOGGLE_DEBUG_LABEL;
import rvmm.data.RegioVincoMapMakerData;

public class RVMMFoolproofDesign implements FoolproofDesign {
    RegioVincoMapMakerApp app;
    
    public RVMMFoolproofDesign(RegioVincoMapMakerApp initApp) {
        app = initApp;
    }

    @Override
    public void updateControls() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        boolean isMapLoaded = data.isMapLoaded();
        ((Button)gui.getGUINode(RVMM_DELETE_MAP_BUTTON)).setDisable(!isMapLoaded);
        ((Button)gui.getGUINode(RVMM_RESET_NAVIGATION_BUTTON)).setDisable(!isMapLoaded);
        ((Button)gui.getGUINode(RVMM_FIT_TO_SUBREGION_BUTTON)).setDisable(!isMapLoaded);
        ((Button)gui.getGUINode(RVMM_SHUFFLE_MAP_COLORS_BUTTON)).setDisable(!isMapLoaded);
        ((Button)gui.getGUINode(RVMM_EDIT_LEADERS_BUTTON)).setDisable(!isMapLoaded);
        ((Button)gui.getGUINode(RVMM_EDIT_PARENT_REGION_BUTTON)).setDisable(!isMapLoaded);
        ((Button)gui.getGUINode(RVMM_EDIT_BROCHURE_BUTTON)).setDisable(!isMapLoaded);
        ((Label)gui.getGUINode(RVMM_TOGGLE_DEBUG_LABEL)).setDisable(!isMapLoaded);
        ((CheckBox)gui.getGUINode(RVMM_TOGGLE_DEBUG_CHECKBOX)).setDisable(!isMapLoaded);
        
        if (isMapLoaded) {
            boolean disableRemove = !data.isSubregionSelected();
            ((Button)gui.getGUINode(RVMM_REMOVE_SUBREGION_BUTTON)).setDisable(disableRemove);
            boolean isFirstSubregionSelected = data.isFirstSubregionSelected();
            boolean disableMoveUp = disableRemove || isFirstSubregionSelected;
            ((Button)gui.getGUINode(RVMM_MOVE_UP_BUTTON)).setDisable(disableMoveUp);
            boolean isLastSubregionSelected = data.isLastSubregionSelected();
            boolean disableMoveDown = disableRemove || isLastSubregionSelected;
            ((Button)gui.getGUINode(RVMM_MOVE_DOWN_BUTTON)).setDisable(disableMoveDown);
        }
        System.out.println("RVMMFoolproofDesign updateControls");
        DebugDisplay.appendDebugText("RVMMFoolproofDesign updateControls");
    }    
}