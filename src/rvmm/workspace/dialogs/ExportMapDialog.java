package rvmm.workspace.dialogs;

import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import djf.ui.dialogs.AppDialogsFacade;
import java.io.File;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import properties_manager.PropertiesManager;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import rvmm.data.RVPropertyType;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.files.RegioVincoFiles;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import static rvmm.workspace.style.RVMMStyle.*;

public class ExportMapDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // UI COMPONENTS FOR THIS DIALOG
    BorderPane dialogPane;
    GridPane centerPane;
    GridPane selectionPane;
    Label headingLabel;
    CheckBox exportToRVMMAppCheckBox;
    Label rvmmAppExportPathPromptLabel;
    Label rvmmAppExportPathLabel;
    CheckBox exportToGameAppCheckBox;
    Label gameAppExportPathPromptLabel;
    Label gameAppExportPathLabel;
    Button editGameAppPathButton;
    Label brochuresHeightPromptLabel;
    Slider brochuresHeightSlider;
    Label flagsHeightPromptLabel;
    Slider flagsWidthSlider;
    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;
    
    public static ExportMapDialog singleton = null;

    public static ExportMapDialog getExportMapDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getExportMapDialog");
        if (singleton == null) {
            singleton = new ExportMapDialog(initApp);
        }
        return singleton;
    }

    private ExportMapDialog(RegioVincoMapMakerApp initApp) {
        app = initApp;

        // KEEP THIS FOR WHEN THE WORK IS ENTERED
        app = initApp;

        initDialogUI();

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);

        // NOW PUT THE GRID IN THE SCENE AND THE SCENE IN THE DIALOG
        Scene scene = new Scene(dialogPane, 1000, 500);
        this.setScene(scene);

        // SETUP THE STYLESHEET
        app.getGUIModule().initStylesheet(this);

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);
    }

    private void initDialogUI() {
        AppNodesBuilder rvmmBuilder = app.getGUIModule().getNodesBuilder();
        centerPane = rvmmBuilder.buildGridPane(RVMM_EXPORT_MAP_DIALOG_CENTER_PANE, null, CLASS_RVMM_DIALOG_GRID, ENABLED);

        selectionPane = rvmmBuilder.buildGridPane(RVMM_EXPORT_MAP_DIALOG_EXPORT_SELECTION_PANE, centerPane, 0, 0, 2, 1, CLASS_RVMM_DIALOG_GRID, ENABLED);
        headingLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_EXPORT_HEADING_LABEL, selectionPane, 0, 0, 4, 1, CLASS_RVMM_DIALOG_HEADER, ENABLED);
        exportToRVMMAppCheckBox = rvmmBuilder.buildCheckBox(RVMM_EXPORT_MAP_DIALOG_EXPORT_TO_RVMM_APP_CHECKBOX, selectionPane, 0, 1, 1, 1, CLASS_RVMM_CHECKBOX, ENABLED);
        rvmmAppExportPathPromptLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_RVMM_APP_EXPORT_PATH_PROMPT_LABEL, selectionPane, 1, 1, 1, 1, CLASS_RVMM_PROMPT, ENABLED);
        rvmmAppExportPathLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_RVMM_APP_EXPORT_PATH_LABEL, selectionPane, 2, 1, 1, 1, CLASS_RVMM_PROMPT, ENABLED);;
        exportToGameAppCheckBox = rvmmBuilder.buildCheckBox(RVMM_EXPORT_MAP_DIALOG_EXPORT_TO_GAME_APP_CHECKBOX, selectionPane, 0, 2, 1, 1, CLASS_RVMM_CHECKBOX, ENABLED);
        gameAppExportPathPromptLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_GAME_APP_EXPORT_PATH_PROMPT_LABEL, selectionPane, 1, 2, 1, 1, CLASS_RVMM_PROMPT, ENABLED);
        gameAppExportPathLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_GAME_APP_EXPORT_PATH_LABEL, selectionPane, 2, 2, 1, 1, CLASS_RVMM_PROMPT, ENABLED);;
        editGameAppPathButton = rvmmBuilder.buildTextButton(RVMM_EXPORT_MAP_DIALOG_GAME_APP_EXPORT_PATH_EDIT_BUTTON, selectionPane, 3, 2, 1, 1, EMPTY_TEXT, ENABLED);

        brochuresHeightPromptLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_BROCHURE_HEIGHT_LABEL, centerPane, 0, 2, 1, 1, CLASS_RVMM_PROMPT, ENABLED);
        brochuresHeightSlider = rvmmBuilder.buildSlider(RVMM_EXPORT_MAP_DIALOG_BROCHURE_HEIGHT_SLIDER, centerPane, 1, 2, 1, 1, CLASS_RVMM_SLIDER, ENABLED, 300, 1000);
        brochuresHeightSlider.setSnapToTicks(true);
        flagsHeightPromptLabel = rvmmBuilder.buildLabel(RVMM_EXPORT_MAP_DIALOG_FLAG_WIDTH_LABEL, centerPane, 0, 3, 1, 1, CLASS_RVMM_PROMPT, ENABLED);
        flagsWidthSlider = rvmmBuilder.buildSlider(RVMM_EXPORT_MAP_DIALOG_FLAG_WIDTH_SLIDER, centerPane, 1, 3, 1, 1, CLASS_RVMM_SLIDER, ENABLED, 100, 500);
        flagsWidthSlider.setSnapToTicks(true);
        bottomPane = rvmmBuilder.buildHBox(RVMM_EXPORT_MAP_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton = rvmmBuilder.buildTextButton(RVMM_EXPORT_MAP_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_EXPORT_MAP_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);

        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        AppGUIModule gui = app.getGUIModule();
        gui.addGUINode(RVMM_EXPORT_MAP_DIALOG_PANE, dialogPane); 
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);
        
        // SETUP THE HANDLERS        
        exportToGameAppCheckBox.setOnAction(e -> {
            RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
            boolean exportToGame = exportToGameAppCheckBox.isSelected();
            data.setExportToGameApp(exportToGame);
            updateControls();
        });
        exportToRVMMAppCheckBox.setOnAction(e -> {
            RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
            boolean exportToRVMM = exportToRVMMAppCheckBox.isSelected();
            data.setExportToRVMMApp(exportToRVMM);
            updateControls();
        });
        editGameAppPathButton.setOnAction(e -> {
            processChangeRootGameAppPath();
        });
        brochuresHeightSlider.setOnMouseReleased(e -> {
            processChangeBrochuresHeight();
        });
        flagsWidthSlider.setOnMouseReleased(e -> {
            processChangeFlagsWidth();
        }); 
        confirmButton.setOnAction(e -> {
            processConfirmExport();
        });
        cancelButton.setOnAction(e -> {
            processCancelExport();
        });
    }
    
    public void processConfirmExport() {
        // EXPORT THE MAP
        RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        
        // WE MAY NEED TO EXPORT MULTIPLE TIMES
        if (data.getExportToRVMMApp()) {
            String rvmmExportFilePath = data.getExportRVMMAppDataFilePath();
            try {
                files.exportData(data, rvmmExportFilePath);
            } catch (IOException ioe) {
                displayError(rvmmExportFilePath);
            }
        }
        if (data.getExportToGameApp()) {
            String gameExportFilePath = data.getExportGameAppDataFilePath();
            try {
                files.exportData(data, gameExportFilePath);
            } catch (IOException ioe) {
                displayError(gameExportFilePath);
            }
        }
        
        // HIDE THE DIALOG
        hide();
    }
    
    public void displayError(String exportFilePath) {
        Alert errorDialog = new Alert(AlertType.ERROR);
        errorDialog.setTitle("Export Error");
        errorDialog.setContentText("An error occured exporting to " + exportFilePath);
        errorDialog.show();        
    }
    
    public void processCancelExport() {
        hide();
    }
    
    public void updateControls() {
        // IF BOTH EXPORT CHECKBOXES ARE OFF DISABLE THE CONFIRM
        // BUTTON AND DISPLAY A WARNING MESSAGE
        if(this.exportToGameAppCheckBox.isSelected()
                || this.exportToRVMMAppCheckBox.isSelected()) {
            this.confirmButton.setDisable(false);
        }
        else {
            this.confirmButton.setDisable(true);
        }
    }

    public void processChangeRootGameAppPath() {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        String rootGameAppPath = data.getRootGameAppPath();
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(rootGameAppPath));
        dirChooser.setTitle(props.getProperty(RVMM_EXPORT_MAP_DIALOG_CHANGE_ROOT_GAME_APP_PATH_TITLE));
        File selectedDir = dirChooser.showDialog(this);
        if(selectedDir != null){
            // FIRST WE HAVE TO MAKE SURE IT CONTAINS "The World"            
            String newRootGameAppPath = new File(".").toURI().relativize(selectedDir.toURI()).getPath();
            File testDataDir = new File(newRootGameAppPath + "/data/The World/");
            File testImagesDir = new File(newRootGameAppPath + "/public/img/The World/");
            boolean dataDirExists = testDataDir.exists();
            boolean imagesDirExists = testImagesDir.exists();
            if (!dataDirExists || !imagesDirExists) {
                // NOT A VALID DIRECTORY
                AppDialogsFacade.showMessageDialog(app.getGUIModule().getWindow(), ROOT_GAME_APP_PATH_ERROR_TITLE, ROOT_GAME_APP_PATH_ERROR_CONTENT);
            }
            else {
                data.setAppProperty(RVPropertyType.ROOT_GAME_APP_PATH, newRootGameAppPath);
                this.gameAppExportPathLabel.setText(newRootGameAppPath);
            }
        }
    }
    public void processChangeBrochuresHeight() {
        int newHeight = (int)((Slider)app.getGUIModule().getGUINode(RVMM_EXPORT_MAP_DIALOG_BROCHURE_HEIGHT_SLIDER)).getValue();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        data.setBrochuresHeight(newHeight);
    }
    public void processChangeFlagsWidth() {
        int newWidth = (int)((Slider)app.getGUIModule().getGUINode(RVMM_EXPORT_MAP_DIALOG_FLAG_WIDTH_SLIDER)).getValue();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        data.setFlagsWidth(newWidth);
    }
    public void loadUISettings(RegioVincoMapMakerData data) {
        AppGUIModule gui = app.getGUIModule();
        Slider brochuresSlider = (Slider)gui.getGUINode(RVMM_EXPORT_MAP_DIALOG_BROCHURE_HEIGHT_SLIDER);
        brochuresSlider.setValue(data.getBrochuresHeight());
        Slider flagsSlider = (Slider)gui.getGUINode(RVMM_EXPORT_MAP_DIALOG_FLAG_WIDTH_SLIDER);
        flagsSlider.setValue(data.getFlagsWidth());
    }
    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String dialogTitleText = props.getProperty(RVMM_EXPORT_MAP_DIALOG_TITLE);
        this.setTitle(dialogTitleText);
        String rvmmAppExportPath = data.getRootRVMMAppPath();
        this.rvmmAppExportPathLabel.setText(rvmmAppExportPath);
        String gameAppExportPath = data.getRootGameAppPath();
        this.gameAppExportPathLabel.setText(gameAppExportPath);
        int brochuresHeight = data.getBrochuresHeight();
        this.brochuresHeightSlider.setValue(brochuresHeight);
        int flagsWidth = data.getFlagsWidth();
        this.flagsWidthSlider.setValue(flagsWidth);
        
        // MAKE SURE PROPER BUTTONS ARE ENABLED/DISABLED TO START
        updateControls();

        // AND OPEN THE DIALOG
        showAndWait();
    }
}