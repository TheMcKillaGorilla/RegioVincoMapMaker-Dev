package rvmm.workspace.dialogs;

import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import java.util.HashMap;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import static rvmm.data.MapDataKeys.RVM_SUBREGIONS_HAVE_CAPITALS;
import static rvmm.data.MapDataKeys.RVM_SUBREGIONS_HAVE_FLAGS;
import static rvmm.data.MapDataKeys.RVM_SUBREGIONS_HAVE_LANDMARKS;
import static rvmm.data.MapDataKeys.RVM_SUBREGIONS_HAVE_LEADERS;
import static rvmm.data.MapDataKeys.RVM_SUBREGIONS_HAVE_NAMES;
import static rvmm.data.RVMM_Constants.WORK_FILE_EXT;
import static rvmm.data.RVMM_Constants.WORK_PATH;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.files.RegioVincoFiles;
import rvmm.workspace.DebugDisplay;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import rvmm.workspace.RegioVincoMapMakerWorkspace;
import static rvmm.workspace.style.RVMMStyle.*;

public class ParentRegionDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // UI COMPONENTS FOR THIS DIALOG
    BorderPane dialogPane;
    HBox topPane;
    Label parentRegionHeadingLabel;
    SplitPane centerPane;
    TreeView<String> regionsTreeView;
    WebView regionSummaryWebView;
    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;

    boolean editing;

    public static ParentRegionDialog singleton = null;

    public static ParentRegionDialog getMapSettingsDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getMapSettingsDialog");
        if (singleton == null) {
            singleton = new ParentRegionDialog(initApp);
        }
        return singleton;
    }

    private ParentRegionDialog(RegioVincoMapMakerApp initApp) {
        app = initApp;

        // KEEP THIS FOR WHEN THE WORK IS ENTERED
        app = initApp;

        initDialogUI();

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);

        // NOW PUT THE GRID IN THE SCENE AND THE SCENE IN THE DIALOG
        Scene scene = new Scene(dialogPane, 700, 500);
        this.setScene(scene);

        // SETUP THE STYLESHEET
        app.getGUIModule().initStylesheet(this);

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);
    }

    private void initDialogUI() {
        AppNodesBuilder rvmmBuilder = app.getGUIModule().getNodesBuilder();

        // TOP PANE
        topPane = rvmmBuilder.buildHBox(RVMM_PARENT_REGION_DIALOG_TOP_PANE, null, CLASS_RVMM_DIALOG_TOP_PANE, ENABLED);
        topPane.setAlignment(Pos.CENTER);
        parentRegionHeadingLabel = rvmmBuilder.buildLabel(RVMM_PARENT_REGION_DIALOG_HEADING_LABEL, topPane, CLASS_RVMM_BIG_HEADER, ENABLED);

        // CENTER PANE
        centerPane = rvmmBuilder.buildSplitPane(RVMM_PARENT_REGION_DIALOG_CENTER_PANE, null, CLASS_RVMM_DIALOG_CENTER_PANE, ENABLED);        
        regionsTreeView = rvmmBuilder.buildTreeView(RVMM_PARENT_REGION_DIALOG_REGIONS_TREE_VIEW, null, CLASS_RVMM_DIALOG_TREE_VIEW, ENABLED);
        regionSummaryWebView = rvmmBuilder.buildWebView(RVMM_PARENT_REGION_REGION_SUMMARY_WEB_VIEW, null, CLASS_RVMM_DIALOG_WEB_VIEW, ENABLED);
        centerPane.getItems().add(regionsTreeView);
        centerPane.getItems().add(regionSummaryWebView);

        // BOTTOM PANE
        bottomPane = rvmmBuilder.buildHBox(RVMM_PARENT_REGION_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton = rvmmBuilder.buildTextButton(RVMM_PARENT_REGION_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_PARENT_REGION_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);

        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        dialogPane.setTop(topPane);
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);

        // SETUP THE HANDLERS
        confirmButton.setOnAction(e -> {
            confirmEdits();
        });
        cancelButton.setOnAction(e -> {
            cancelEdits();
        });
    }

    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        String titleText = "RVMM - " + data.getRegionName() + " Parent Region Selection";
        setTitle(titleText);

        // HAVE THE CARET START IN THE NAME TEXT FIELD
        String regionName = data.getRegionName();
        
        // LOAD ALL THE DATA INTO THE TREE VIEW
        RegioVincoMapMakerWorkspace workspace = (RegioVincoMapMakerWorkspace)app.getWorkspaceComponent();
        workspace.loadRegionsTreeView(regionsTreeView);
        
        // REGISTER HANDLERS
        registerTreeHandlers();
        
        // SELECT THE PARENT
        String parentRegionPath = data.getParentRegionPath();
        selectParentRegion(parentRegionPath);
        
        // START OUT OUR CONFIRM BUTTON AS DISABLED
        updateControls();

        // AND OPEN THE DIALOG
        showAndWait();
    }
    
    private void updateControls() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        String currentParentPath = data.getParentRegionPath();
        String selectedParentPath = getSelectedParentPath();
        String selectedRegion = this.regionsTreeView.getSelectionModel().getSelectedItem().getValue();
        DebugDisplay.appendDebugText("currentParentPath: " + currentParentPath);
        DebugDisplay.appendDebugText("selectedParentPath: " + selectedParentPath);
        if (currentParentPath.equals(selectedParentPath)) {
            this.confirmButton.setDisable(true);
        }
        else if (selectedParentPath.startsWith(currentParentPath + data.getRegionName() + "/")) {
            this.confirmButton.setDisable(true);
        }
        else if (selectedRegion.equals(data.getRegionName())) {
            this.confirmButton.setDisable(true);
        }
        else {
            this.confirmButton.setDisable(false);
        }
    }
    
    private String getSelectedParentPath() {
        TreeItem<String> selectedItem = this.regionsTreeView.getSelectionModel().getSelectedItem();
        String parentPath = "";
        while (selectedItem != null) {
            parentPath = selectedItem.getValue() + "/" + parentPath;
            selectedItem = selectedItem.getParent();
        }
        return parentPath;
    }
    
    private void selectParentRegion(String path) {
        DebugDisplay.appendDebugText(path);
        String[] pathNodeNames = path.split("/");
        TreeItem<String> root = regionsTreeView.getRoot();
        findAndSelectParent(root, pathNodeNames, 0);
    }
    
    private void findAndSelectParent(TreeItem<String> node, String[] pathNodeNames, int currentNodeIndex) {
        // STOPPING CASE
        if (currentNodeIndex == (pathNodeNames.length-1)) {
            // node IS THE NODE TO SELECT
            this.regionsTreeView.getSelectionModel().select(node);
        }
        else {
            // FIND THE CHILD
            ObservableList<TreeItem<String>> children = node.getChildren();
            for (TreeItem<String> item : children) {
                if (item.getValue().equals(pathNodeNames[currentNodeIndex+1])) {
                    findAndSelectParent(item, pathNodeNames, currentNodeIndex+1);
                }
            }
            // NOT FOUND
            DebugDisplay.appendDebugText("Parent Not Found");
        }
    }
    
    private void registerTreeHandlers() {
        this.regionsTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.regionsTreeView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TreeItem<String>>() {
                    public void changed(
                            ObservableValue<? extends TreeItem<String>> changed, 
                            TreeItem<String> oldVal,
                            TreeItem<String> newVal) {
                        if (newVal != null) {
                            // MAKE SURE IT'S OPENED ALL THE WAY
                            newVal.setExpanded(true);
                        
                            // UPDATE THE CONFIRM BUTTON IF NECESSARY
                            updateControls();

                            // SHOW INFO ABOUT THE SELECTED REGION
                            displayRegionStats(newVal);
                        }
                    }
        });        
    }
    
    private String getWorkDataFilePath(TreeItem<String> node) {
        String regionName = node.getValue();
        String path = "";
        node = node.getParent();
        while (node != null) {
            path = node.getValue() + "/" + path;
            node = node.getParent();
        }
        path = WORK_PATH + path + regionName + WORK_FILE_EXT;
        return path;
    }
    
    private void displayRegionStats(TreeItem<String> node) {
        String workFilePath = getWorkDataFilePath(node);
        RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
        HashMap<String, String> mapProperties = files.loadMapProperties(workFilePath);
        String regionName = node.getValue();
        boolean subregionsHaveNames = Boolean.parseBoolean(mapProperties.get(RVM_SUBREGIONS_HAVE_NAMES));
        boolean subregionsHaveCapitals = Boolean.parseBoolean(mapProperties.get(RVM_SUBREGIONS_HAVE_CAPITALS));
        boolean subregionsHaveLeaders = Boolean.parseBoolean(mapProperties.get(RVM_SUBREGIONS_HAVE_LEADERS));
        boolean subregionsHaveFlags = Boolean.parseBoolean(mapProperties.get(RVM_SUBREGIONS_HAVE_FLAGS));
        boolean subregionsHaveLandmarks = Boolean.parseBoolean(mapProperties.get(RVM_SUBREGIONS_HAVE_LANDMARKS));
        String html = "<h2 style='font-size:28pt'>" + regionName + " Info</h2>"
                + "<table style='font-size:24pt'>"
                + " <tr><td style='font-weight:bold'>Games Available:</td></tr>"
                + " <tr><td>-Names Game:</td>" + makeTableCell(subregionsHaveNames) + "</tr>"
                + " <tr><td>-Capitals Game:</td>" + makeTableCell(subregionsHaveCapitals) + "</tr>"
                + " <tr><td>-Leaders Game:</td>" + makeTableCell(subregionsHaveLeaders) + "</tr>"
                + " <tr><td>-Flags Game:</td>" + makeTableCell(subregionsHaveFlags) + "</tr>"
                + " <tr><td>-Landmarks Game:</td>" + makeTableCell(subregionsHaveLandmarks) + "</tr>"
                + "</table>";
        this.regionSummaryWebView.getEngine().loadContent(html);
    }
    
    public String makeTableCell(boolean valid) {
        if (valid)
            return "<td style='color:green'>true</td>";
        else
            return "<td style='color:red'>false</td>";
    }

    public void confirmEdits() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();

        ButtonType result = confirmDialog();
        
        if (result == ButtonType.YES) {
            // CHANGE THE PARENT PATH
            String newParentPath = this.getSelectedParentPath();
            data.updateParentPath(newParentPath);
        }
        // CANCEL BUT CONTINUE WITH THE PARENT REGION DIALOG
        else if (result == ButtonType.CANCEL) {
            return;
        }

        // CLOSE THE DIALOG, THE USER MUST HAVE EITHER CHANGED THE
        // PARENT PATH OR SAID NO
        this.hide();
    }
    
    private ButtonType confirmDialog() {
        String newParentPath = this.getSelectedParentPath();
        String title = "Change Parent Path?";
        String contentText = "Are you sure you wish to change the parent region to "
                                + newParentPath + "? Note that all affected paths for "
                                + " this region and its child regions will be updated.";
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.initOwner(this);
        confirmationDialog.initModality(Modality.APPLICATION_MODAL);
        confirmationDialog.getButtonTypes().clear();
        confirmationDialog.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        confirmationDialog.setTitle(title);
        confirmationDialog.setContentText(contentText);
        Optional<ButtonType> result = confirmationDialog.showAndWait();
        return result.get();
    }

    public void cancelEdits() {
        // AND CLOSE THE DIALOG
        hide();
    }
}
