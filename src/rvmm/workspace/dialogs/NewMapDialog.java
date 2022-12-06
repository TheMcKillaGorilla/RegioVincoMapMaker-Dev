package rvmm.workspace.dialogs;

import static djf.modules.AppGUIModule.ENABLED;
import djf.ui.AppNodesBuilder;
import djf.ui.dialogs.AppDialogsFacade;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import properties_manager.PropertiesManager;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import static rvmm.data.MapDataKeys.DBF_FILE_TYPE;
import static rvmm.data.MapDataKeys.SHP_FILE_TYPE;
import static rvmm.data.RVMM_Constants.WORK_FILE_EXT;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.files.dbf.DBFException;
import rvmm.files.dbf.DBFField;
import rvmm.files.dbf.DBFReader;
import rvmm.files.dbf.DBFUtils;
import rvmm.workspace.DebugDisplay;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import rvmm.workspace.RegioVincoMapMakerWorkspace;
import static rvmm.workspace.style.RVMMStyle.*;

public class NewMapDialog extends Stage {
    RegioVincoMapMakerApp app;
    
    // EVERYTHING GOES IN HERE
    BorderPane dialogPane;
    
    // AT THE TOP OF THE DIALOG
    HBox topPane;
    Label createMapHeadingLabel;
    
    // THIS IS THE GRID IN THE CENTER
    GridPane centerPane;
    Label regionNamePromptLabel;
    TextField regionNameTextField;
    Label shpPromptLabel;
    Label shpLabel;
    Button shpEditButton;
    Label dbfPromptLabel;
    Label dbfLabel;
    Button dbfEditButton;
    Label dbfFieldPromptLabel;
    ComboBox dbfFieldComboBox;
    Label parentRegionPromptLabel;
    TreeView parentRegionTreeView;
    Label warningLabel;
 
    // AT THE BOTTOM OF THE DIALOG
    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;
    
    private void enableControls(boolean enable) {
        confirmButton.setDisable(!enable);
        confirmButton.disableProperty().set(!enable);
        warningLabel.visibleProperty().set(!enable);
        DebugDisplay.appendDebugText("enableControls: " + enable);
    }

    // THE DATA WE'LL BE COLLECTING TO MAKE A NEW MAP
    String regionName = "";
    String shpPath = "";
    String dbfPath = "";
    String dbfField = "";
    String parentRegionPath = "";
    boolean shpPathSelected = false;
    boolean dbfPathSelected = false;
    boolean parentRegionPathSelected = false;
    boolean goodData;
    
    public static NewMapDialog singleton = null;
    
    public static NewMapDialog getNewMapDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getNewMapDialog");
        if (singleton == null)
            singleton = new NewMapDialog(initApp);
        return singleton;
    }

    private NewMapDialog(RegioVincoMapMakerApp initApp) {
        // KEEP THE APP FOR LATER
        app = initApp;

        // LAYOUT EVERTHING AND SETUP HANDLERS
        initDialog();

        // NOW PUT THE GRID IN THE SCENE AND THE SCENE IN THE DIALOG
        Scene scene = new Scene(dialogPane, 1200, 700);
        this.setScene(scene);

        // SETUP THE STYLESHEET
        app.getGUIModule().initStylesheet(this);

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);
    }
    
    // ACCESSOR METHODS
    public boolean hasGoodData() {
        return goodData;
    }
    
    public String getRegionName() {
        return regionName;
    }
    
    public String getShpPath() {
        return shpPath;
    }
    
    public String getDbfPath() {
        return dbfPath;
    }

    public String getParentRegionPath() {
        return parentRegionPath;
    }
        
    private void initDialog(){
        AppNodesBuilder rvmmBuilder = app.getGUIModule().getNodesBuilder();

        // THE TOP PANE        
        topPane = rvmmBuilder.buildHBox(RVMM_NEW_MAP_DIALOG_TOP_PANE, null, CLASS_RVMM_DIALOG_TOP_PANE, ENABLED);
        topPane.setAlignment(Pos.CENTER);
        createMapHeadingLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_HEADER_LABEL, topPane, CLASS_RVMM_DIALOG_HEADER, ENABLED);

        // THE NODES ABOVE GO DIRECTLY INSIDE THE GRID
        centerPane = rvmmBuilder.buildGridPane(RVMM_NEW_MAP_DIALOG_CENTER_PANE, null, CLASS_RVMM_DIALOG_GRID, ENABLED);
        centerPane.setAlignment(Pos.CENTER);
        regionNamePromptLabel   = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_NAME_PROMPT_LABEL,     centerPane, 0, 0, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        regionNameTextField     = rvmmBuilder.buildTextField(RVMM_NEW_MAP_DIALOG_NAME_TEXT_FIELD,   centerPane, 1, 0, 2, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        shpPromptLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_SHP_PROMPT_LABEL, centerPane, 0, 1, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        shpLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_SHP_LABEL, centerPane, 1, 1, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        shpEditButton = rvmmBuilder.buildTextButton(RVMM_NEW_MAP_DIALOG_SHP_EDIT_BUTTON, centerPane, 2, 1, 1, 1, CLASS_RVMM_BUTTON, ENABLED);
        dbfPromptLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_DBF_PROMPT_LABEL, centerPane, 0, 2, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        dbfLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_DBF_LABEL, centerPane, 1, 2, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        dbfEditButton = rvmmBuilder.buildTextButton(RVMM_NEW_MAP_DIALOG_DBF_EDIT_BUTTON, centerPane, 2, 2, 1, 1, CLASS_RVMM_BUTTON, ENABLED);
        dbfFieldPromptLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_DBF_FIELD_PROMPT_LABEL, centerPane, 0, 3, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        dbfFieldComboBox = rvmmBuilder.buildComboBox(RVMM_NEW_MAP_DIALOG_DBF_FIELD_COMBO_BOX, centerPane, 1, 3, 2, 1, CLASS_RVMM_COMBO_BOX, ENABLED);
        parentRegionPromptLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_PARENT_REGION_PROMPT_LABEL, centerPane, 0, 4, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        parentRegionTreeView = rvmmBuilder.buildTreeView(RVMM_NEW_MAP_DIALOG_PARENT_REGION_TREE_VIEW, centerPane, 1, 4, 2, 1, CLASS_RVMM_PARENT_TREE_VIEW, ENABLED);
        warningLabel = rvmmBuilder.buildLabel(RVMM_NEW_MAP_DIALOG_WARNING_LABEL, centerPane, 0, 5, 3, 1, CLASS_RVMM_DIALOG_WARNING_LABEL, ENABLED);
        GridPane.setValignment(parentRegionPromptLabel, VPos.TOP);
        parentRegionTreeView.setMinWidth(1000);

        ColumnConstraints neverGrow = new ColumnConstraints();
        neverGrow.setHgrow(Priority.NEVER);

        ColumnConstraints alwaysGrow = new ColumnConstraints();
        alwaysGrow.setHgrow(Priority.ALWAYS);
        centerPane.getColumnConstraints().addAll(
            neverGrow,
            alwaysGrow,
            neverGrow);
        
        // THE BOTTOM PANE
        bottomPane = rvmmBuilder.buildHBox(RVMM_NEW_MAP_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton                = rvmmBuilder.buildTextButton(RVMM_NEW_MAP_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton            = rvmmBuilder.buildTextButton(RVMM_NEW_MAP_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        
        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        dialogPane.setTop(topPane);
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);
        
        // SETUP THE HANDLERS
        regionNameTextField.textProperty().addListener(e->{
            regionName = regionNameTextField.getText();
            this.updateControls();
        });
        shpEditButton.setOnAction(e->{
            this.selectShp();
            this.updateControls();
        });
        dbfEditButton.setOnAction(e->{
            this.selectDbf();
            this.updateControls();
        });
        registerTreeHandlers();
        confirmButton.setOnAction(e->{
            createNewMap();}
        );
        cancelButton.setOnAction(e->{
            cancelNewMap();
        });
    }    
    private void registerTreeHandlers() {
        this.parentRegionTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.parentRegionTreeView.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<TreeItem<String>>() {
                public void changed(
                            ObservableValue<? extends TreeItem<String>> changed, 
                            TreeItem<String> oldVal,
                            TreeItem<String> newVal) {
                    newVal.setExpanded(true);
                    parentRegionPath = pathToNode(newVal);
                    NewMapDialog.this.parentRegionPathSelected = true;
                    updateControls();
                }
            }
        );        
    }
    private String pathToNode(TreeItem<String> node) {
        String path = "";
        do {
            path = node.getValue() + "/" + path;
            node = node.getParent();
        } while (node != null);
        return path;
    }
    private void updateControls() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        if (!this.hasValidInput()) {
            // WE DON'T EVEN HAVE TO TEST WHEN THE USER
            // HAS YET TO ENTER VALUES
            enableControls(false);
        }
        // NOW WE ASSUME THE USER HAS ENTERED VALUES
        else if (data.isValidNewRegion(regionName, parentRegionPath)) {
            // ENABLE CONTROLS AND DISPLAY WARNING MESSAGES
            enableControls(true);
        }
        else {
            // DISABLE CONTROLS AND REMOVE WARNING MESSAGES
            enableControls(false);
        }
    }
    
    private void selectShp() {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        FileChooser fileChooser = new FileChooser();
        File dataFileDir = new File(props.getProperty(RVMM_PATH_RAW_MAP_DATA_FILES));
        fileChooser.setInitialDirectory(dataFileDir);
        fileChooser.setTitle(props.getProperty(RVMM_NEW_MAP_SELECT_SHP_DIALOG_TITLE));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Shapefiles (*.shp)", "*.shp");
        fileChooser.getExtensionFilters().add(extFilter);
        File dataFile = fileChooser.showOpenDialog(this);
        if(dataFile != null) {
            shpPath = new File(".").toURI().relativize(dataFile.toURI()).getPath();
            shpLabel.setText(shpPath); 
            shpPathSelected = true;
        }        
    }
    
    private void selectDbf() {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        FileChooser fileChooser = new FileChooser();
        File dataFileDir = new File(props.getProperty(RVMM_PATH_RAW_MAP_DATA_FILES));
        fileChooser.setInitialDirectory(dataFileDir);
        fileChooser.setTitle(props.getProperty(RVMM_NEW_MAP_SELECT_DBF_DIALOG_TITLE));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("dBase (*.dbf)", "*.dbf");
        fileChooser.getExtensionFilters().add(extFilter);
        File dataFile = fileChooser.showOpenDialog(this);
        if(dataFile != null) {
            dbfPath = new File(".").toURI().relativize(dataFile.toURI()).getPath();
            dbfLabel.setText(dbfPath); 
            dbfPathSelected = true;
            
            DBFReader reader = null;
            try {
                reader = new DBFReader(new FileInputStream(dbfPath));
                int numberOfFields = reader.getFieldCount();
                ObservableList<String> fieldNames = this.dbfFieldComboBox.getItems();
                fieldNames.clear();
                fieldNames.add("-");
                for (int i = 0; i < numberOfFields; i++) {
                    DBFField field = reader.getField(i);
                    String fieldName = field.getName();
                    fieldNames.add(fieldName);
                }
            } catch (DBFException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                DBFUtils.close(reader);
            }
        }        
    }
    
    private void createNewMap() {
        // GET THE USER INPUT
        regionName = regionNameTextField.getText();
        TreeItem<String> selectedItem = (TreeItem<String>)parentRegionTreeView.getSelectionModel().getSelectedItem();
        parentRegionPath = pathToNode(selectedItem);
        shpPath = shpLabel.getText();
        dbfPath = dbfLabel.getText();
        goodData = hasValidInput();
        String dataPaths = shpPath;
        if (this.dbfPathSelected && (dbfField != null) && (!dbfField.equals("!"))) {
            dbfField = dbfFieldComboBox.getSelectionModel().getSelectedItem().toString();
            dataPaths += "-" + dbfPath + "-" + dbfField;
        }
        if (goodData) {
            try {
                RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
                data.startLoading();
                app.getFileModule().newWork("work/" + parentRegionPath, regionName, WORK_FILE_EXT);
                app.getFileComponent().importData(app.getDataComponent(), dataPaths);
                               
                data.setRegionName(regionName);
                data.setParentRegionPath(parentRegionPath);

                // NOW SAVE THE FILE TO START
                String workFilePath = data.getWorkFilePath();
                File workFile = new File(workFilePath);

                // FIRST MAKE SURE THE PARENT DIRECTORY EXISTS
                workFile.getParentFile().mkdirs();
                
                // AND THEN SAVE THE WORK FILE
                app.getFileModule().saveWork(workFile);
                
                // AND CREATE A DIRECTORY FOR THE ORIG IMAGES
                String origPath = data.getOrigImagesPath();
                File origPathFile = new File(origPath);
                origPathFile.mkdirs();
                data.endLoading();
                this.hide();
            }
            catch(IOException ioe) {
                AppDialogsFacade.showMessageDialog(app.getGUIModule().getWindow(), "ERROR @todo", "Error @todo");
            }
        }
    }

    private void cancelNewMap() {
        goodData = false;
        hide();
    }
    
    
    private boolean hasValidInput() {
        return (!regionName.trim().isEmpty())
                && (parentRegionPathSelected)
                && (shpPathSelected);
    }
    
    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();

        // RESET THE REGION NAME
        regionName = EMPTY_TEXT;
        regionNameTextField.setText(EMPTY_TEXT);
        regionNameTextField.requestFocus();

        // RESET THE SHAPEFILE
        shpPath = EMPTY_TEXT;   
        shpLabel.setText(EMPTY_TEXT);
        dbfPath = EMPTY_TEXT;
        dbfLabel.setText(EMPTY_TEXT);
        shpPathSelected = false;
        dbfPathSelected = false;
        
        // AND THE PARENT PATH
        parentRegionPathSelected = false;

        // LOAD ALL THE DATA INTO THE TREE VIEW
        RegioVincoMapMakerWorkspace workspace = (RegioVincoMapMakerWorkspace)app.getWorkspaceComponent();
        workspace.loadRegionsTreeView(this.parentRegionTreeView);
        
        this.enableControls(false);
        show();
    }
}