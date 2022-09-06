package rvmm.workspace.controllers;

import djf.modules.AppGUIModule;
import djf.modules.AppRecentWorkModule;
import djf.modules.AppWork;
import djf.ui.dialogs.AppDialogsFacade;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.commons.io.FileUtils;
import properties_manager.PropertiesManager;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_NEW_MAP_SELECT_SHAPEFILE_DIALOG_TITLE;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_PATH_RAW_MAP_DATA_FILES;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_SUBREGIONS_TABLE_VIEW;
import static rvmm.RegioVincoMapMakerPropertyType.RVMM_SUBREGION_TYPE_COMBO_BOX;
import static rvmm.data.MapDataKeys.RVM_PARENT_REGION_PATH;
import rvmm.data.MapPropertyType;
import static rvmm.data.RVMM_Constants.RVMM_FEEDBACK_FORM_URL;
import static rvmm.data.RVMM_Constants.WORK_FILE_EXT;
import static rvmm.data.RVMM_Constants.WORK_PATH;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;
import rvmm.files.RegioVincoFiles;
import rvmm.transactions.ChangeMapProperties_Transaction;
import rvmm.transactions.MoveSubregion_Transaction;
import rvmm.transactions.RemoveSubregion_Transaction;
import rvmm.workspace.DebugDisplay;
import rvmm.workspace.dialogs.ExportMapDialog;
import rvmm.workspace.dialogs.BrochureDialog;
import rvmm.workspace.dialogs.LeadersDialog;
import rvmm.workspace.dialogs.ParentRegionDialog;
import rvmm.workspace.dialogs.SubregionDialog;

public class MapEditController {
    RegioVincoMapMakerApp app;
    
    public MapEditController(RegioVincoMapMakerApp initApp) {
        app = initApp;
    }
    public void processExportMap() {
        ExportMapDialog exportMapDialog = ExportMapDialog.getExportMapDialog(app);
        exportMapDialog.showDialog();
    }
    public void processEditBrochure() {
        BrochureDialog brochureDialog = BrochureDialog.getBrochureDialog(app);
        brochureDialog.showDialog();
    }
    public void processEditLeaders() {
        LeadersDialog leadersDialog = LeadersDialog.getLeadersDialog(app);
        leadersDialog.showDialog();
    }
    public void processEditMapSettings() {
        ParentRegionDialog mapSettingsDialog = ParentRegionDialog.getMapSettingsDialog(app);
        mapSettingsDialog.showDialog();
    } 
    public void processMoveSubregionUp() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        TableView subregionsTable = (TableView)app.getGUIModule().getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
        SubregionPrototype subregion = (SubregionPrototype)subregionsTable.getSelectionModel().getSelectedItem();
        if (subregion != null) {
            MoveSubregion_Transaction transaction = new MoveSubregion_Transaction(data, subregion, true);
            app.processTransaction(transaction);
        }
    }  
    public void processMoveSubregionDown() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        TableView subregionsTable = (TableView)app.getGUIModule().getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
        SubregionPrototype subregion = (SubregionPrototype)subregionsTable.getSelectionModel().getSelectedItem();
        if (subregion != null) {
            MoveSubregion_Transaction transaction = new MoveSubregion_Transaction(data, subregion, false);
            app.processTransaction(transaction);
        }
    }
    public void processRandomizeGreyscaleColors() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        data.randomizeSubregionColors();
    }
    public void processSelectSubregion(SubregionPrototype subregion) {
        ((RegioVincoMapMakerData)app.getDataComponent()).selectSubregion(subregion);
    }
    public void processEditSubregion(SubregionPrototype subregion) {
        ((RegioVincoMapMakerData)app.getDataComponent()).selectSubregion(subregion);
        SubregionDialog subregionDialog = SubregionDialog.getSubregionDialog(app);
        subregionDialog.showDialog(subregion);
    }
    public boolean isNotLoading() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        return ((data != null) && (!data.isLoading()));
    }
    public void processChangeSubregionType() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        if (!data.isLoading()){
            ComboBox subregionTypeComboBox = (ComboBox)app.getGUIModule().getGUINode(RVMM_SUBREGION_TYPE_COMBO_BOX);
            String oldSubregionType = data.getSubregionType();
            HashMap<MapPropertyType, String> oldProperties = new HashMap();
            oldProperties.put(MapPropertyType.SUBREGION_TYPE, oldSubregionType);
            String newSubregionType = (String)subregionTypeComboBox.getSelectionModel().getSelectedItem();
            HashMap<MapPropertyType, String> newProperties = new HashMap();
            newProperties.put(MapPropertyType.SUBREGION_TYPE, newSubregionType);
            ChangeMapProperties_Transaction transaction = new ChangeMapProperties_Transaction(data, oldProperties, newProperties);
            app.processTransaction(transaction);
        }
    }
    public void processRemoveSubregion() {
        AppGUIModule gui = app.getGUIModule();
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Selected Subregion?");
        alert.setContentText("Are you sure you wish to remove the selected subregion from the map?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        ButtonType result = alert.showAndWait().get();
        if (result == ButtonType.YES) {
            RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
            TableView<SubregionPrototype> subregionsTable = (TableView)gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
            SubregionPrototype selectedSubregion = subregionsTable.getSelectionModel().getSelectedItem();
            int subregionIndex = data.getSubregionIndex(selectedSubregion);
            RemoveSubregion_Transaction transaction = new RemoveSubregion_Transaction(data, subregionIndex, selectedSubregion);
            app.processTransaction(transaction);
        }
    }
    public void processImportShapefile() {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        FileChooser fileChooser = new FileChooser();
        File dataFileDir = new File(props.getProperty(RVMM_PATH_RAW_MAP_DATA_FILES));
        fileChooser.setInitialDirectory(dataFileDir);
        fileChooser.setTitle(props.getProperty(RVMM_NEW_MAP_SELECT_SHAPEFILE_DIALOG_TITLE));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Shapefiles (*.shp)", "*.shp");
        fileChooser.getExtensionFilters().add(extFilter);
        File dataFile = fileChooser.showOpenDialog(app.getGUIModule().getWindow());
        if(dataFile != null) {
            String shapefilePath = new File(".").toURI().relativize(dataFile.toURI()).getPath();
            RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
            RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
            try {
                files.importShapefile(data, shapefilePath);
            } catch (IOException ioe) {
                AppDialogsFacade.showMessageDialog(app.getGUIModule().getWindow(), "Error", "Error");
            }
        }        
    }
    private void loadAndSetWarningImage(TextInputDialog dialog) {
        File warningImageFile = new File("images/icons/Warning.png");
        ImageView warningImageView = new ImageView();
        if (warningImageFile.exists()) {
            try {
                BufferedImage warningBufferedImage = ImageIO.read(warningImageFile);
                Image warningImage = SwingFXUtils.toFXImage(warningBufferedImage, null);
                warningImageView.setImage(warningImage);
            } catch(IOException ioe) {
                DebugDisplay.appendDebugText("Problems loading " + warningImageFile.getAbsolutePath());
            }
        }
        dialog.setGraphic(warningImageView);        
    }
    private boolean verifyDelete(String headerText, String title, String contentText) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        TextInputDialog deleteAllDialog = new TextInputDialog();
        deleteAllDialog.setHeaderText(headerText);
        deleteAllDialog.setTitle(title);
        deleteAllDialog.setContentText(contentText);
        this.loadAndSetWarningImage(deleteAllDialog);
        String deleteAllInput = deleteAllDialog.showAndWait().get();
        boolean continueWithDelete = deleteAllInput.equals(data.getRegionName());
        return continueWithDelete;
    }
    public void processDeleteMap() {
        // DON'T DELETE THE WORLD, NO MATTER WHAT
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        if (data.getRegionName().equals("The World")) {
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle(("Error - Deletion Failed"));
            errorAlert.setContentText("The World cannot be deleted");
            errorAlert.showAndWait();
            return;
        }
        
        
        // FIRST WE HAVE TO GIVE THE USER A CHOICE:
            // DELETE MAP AND ALL CHILDREN
            // DELETE MAP BUT PROMOTE CHILDREN
            // CANCEL
        Alert verifyDeleteAlert = new Alert(AlertType.NONE);
        verifyDeleteAlert.setTitle("Delete this Map?");
        verifyDeleteAlert.setContentText("Do you want to delete this map and its child maps or promote its children?");
        verifyDeleteAlert.getButtonTypes().clear();
        ButtonType deleteAllButton = new ButtonType("Delete All");
        ButtonType deleteKeepChildrenButton = new ButtonType("Promote Children");
        ButtonType cancelButton = new ButtonType("Cancel");
        verifyDeleteAlert.getButtonTypes().addAll(  deleteAllButton,
                                                    deleteKeepChildrenButton,
                                                    cancelButton);
        ButtonType choice = verifyDeleteAlert.showAndWait().get();
        System.out.println("choice: " + choice);

        if (choice == deleteAllButton) {
            // THE USER HAS VERIFIED TO DELETE MAP AND ALL CHILDREN
            // NOW VERIFY AGAIN BY ASKING THE USER TO TYPE IN THE NAME OF THE REGION
            this.deleteMapAndAllDescendants();
        }  
        else if (choice == deleteKeepChildrenButton) {
            // THE USER HAS VERIFIED TO DELETE THIS MAP BUT PROMOTE CHILDREN
            // NOW VERIFY AGAIN BY ASKING THE USER TO TYPE IN THE NAME OF THE REGION
            this.deleteMapAndPromoteDescendants();
        }
        else {
            // THE USER HAS OPTIONED TO CANCEL, SO WE DON'T NEED TO DO ANYTHING
            DebugDisplay.appendDebugText("Delete Map Cancelled");
            System.out.println("Delete Map Cancelled");
        }
    }
    private void deleteMapAndAllDescendants() {
        String regionName = ((RegioVincoMapMakerData)app.getDataComponent()).getRegionName();
        boolean continueWithDelete = verifyDelete(
                "Warning: You are about to delete this map and all its descendants.",
                "Warning - Delete All?",
                "Enter the name of this map region:"
        );
        if (continueWithDelete) {
            deleteMap(false);
        }
        else {
            // USER DID NOT ENTER A MATCH AND SO THE MAP WAS NOT DELETED
            DebugDisplay.appendDebugText("Map not deleted - Text does not match");
            System.out.println("Map not deleted - Text does not match");
        }        
    }
    private void deleteMapAndPromoteDescendants() {
        String regionName = ((RegioVincoMapMakerData)app.getDataComponent()).getRegionName();
        boolean continueWithDelete = verifyDelete(
                "Warning: You are about to delete this map and promote its descendants.",
                "Warning - Delete " + regionName + " Map?",
                "Enter the name of this map region:"
        );
        if (continueWithDelete) {
            deleteMap(true);
        }
        else {
            // USER DID NOT ENTER A MATCH AND SO THE MAP WAS NOT DELETED
            DebugDisplay.appendDebugText("Map not deleted - Text does not match");
            System.out.println("Map not deleted - Text does not match");
        }      
        
    }
    private void deleteMap(boolean keepDescendants) {
        RegioVincoMapMakerData data = ((RegioVincoMapMakerData)app.getDataComponent());
        String regionName = data.getRegionName();
        String parentDirectoryPath = data.getParentRegionPath();
        DebugDisplay.appendDebugText("Deleting " + regionName + " and promoting descendants");
        System.out.println("Deleting " + regionName + " and promoting descendants");
        
        // UPDATE RECENT HISTORY
        this.removeMapFromRecentHistory(keepDescendants);

        // CLOSE THE CURRENT MAP
        app.getGUIModule().getFileController().processCloseRequest();
           
        RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
        if (keepDescendants) {
            // MOVE ALL DESCENDANTS
            files.promoteAllDescendants(data);
        }
            
        // DELETING THE work FILE
        files.deleteCurrentRegionWorkFile(data);
            
        // AND DELETE THE NOW EMPTY work DIRECTORY IF IT EXISTS
        files.deleteCurrentRegionWorkDir(data);
            
        // WARN THE USER THAT THEY WILL NEED TO CLEAN UP EXPORTED CONTENT
        // AS WELL AS THE ORIGINAL IMAGES
        this.showPostDeleteDialog();                
    }
    private void removeMapFromRecentHistory(boolean keepDescendants) {
        AppWork currentWork = app.getFileModule().getWork();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        AppRecentWorkModule recentWorkModule = app.getRecentWorkModule();

        // REMOVE THIS MAP
        recentWorkModule.deleteWork(currentWork);
        String affectedDir = currentWork.getDirectoryPath() + currentWork.getName() + "/";

        if (keepDescendants) {
            recentWorkModule.removeDirFromAllPaths(affectedDir);
        }
        else {
            recentWorkModule.deleteAllIn(affectedDir);
        }
    }
    private void showPostDeleteDialog() {
        Alert alert = new Alert(AlertType.NONE);
        alert.getButtonTypes().add(ButtonType.OK);
        alert.setTitle("Map Deletion Complete");
        alert.setContentText("The Map has been deleted. Note that the map's images and exported directories have been left intact.");
        alert.showAndWait();
    }
    public void processReportError() {
        // OPEN A WEB BROWSER TO THE GOOGLE FORM        
        String url = RVMM_FEEDBACK_FORM_URL;
        app.getHostServices().showDocument(url);
    }
}