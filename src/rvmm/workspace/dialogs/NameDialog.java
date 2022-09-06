package rvmm.workspace.dialogs;

import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import java.io.File;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import properties_manager.PropertiesManager;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.transactions.ChangeRegionName_Transaction;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import static rvmm.workspace.style.RVMMStyle.*;

public class NameDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // UI COMPONENTS FOR THIS DIALOG
    BorderPane dialogPane;
    GridPane centerPane;
    Label regionNamePromptLabel;
    TextField regionNameTextField;
    Label warningLabel;
    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;

    public static NameDialog singleton = null;

    public static NameDialog getNameDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getNameDialog");
        if (singleton == null) {
            singleton = new NameDialog(initApp);
        }
        return singleton;
    }

    private NameDialog(RegioVincoMapMakerApp initApp) {
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
    
        centerPane = rvmmBuilder.buildGridPane(RVMM_NAME_DIALOG_CENTER_PANE, null, CLASS_RVMM_DIALOG_GRID, ENABLED);
        regionNamePromptLabel  = rvmmBuilder.buildLabel(RVMM_NAME_DIALOG_REGION_NAME_PROMPT_LABEL, centerPane, 0, 1, 1, 1, CLASS_RVMM_PROMPT, ENABLED);;
        regionNameTextField = rvmmBuilder.buildTextField(RVMM_NAME_DIALOG_REGION_NAME_TEXT_FIELD, centerPane, 1, 1, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        warningLabel = rvmmBuilder.buildLabel(RVMM_NAME_DIALOG_WARNING_LABEL, centerPane, 0, 2, 2, 1, CLASS_RVMM_DIALOG_WARNING_LABEL, ENABLED);

        bottomPane = rvmmBuilder.buildHBox(RVMM_NAME_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton = rvmmBuilder.buildTextButton(RVMM_NAME_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_NAME_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);

        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);
        
        // SETUP THE HANDLERS
        AppGUIModule gui = app.getGUIModule();

        confirmButton.setOnAction(e -> {
            processChangeRegionName();
        });
        cancelButton.setOnAction(e -> {
            hide();
        });
    }

    public void processChangeRegionName() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        String oldRegionName = data.getRegionName();
        String newRegionName = this.regionNameTextField.getText();
        
        // FIRST VERIFY ONE MORE TIME
        Alert verifyAlert = new Alert(AlertType.WARNING);
        verifyAlert.setTitle("Are you sure?");
        verifyAlert.setContentText("Warning, changing the region name will change corresponding directory names. Should we proceed?");
        verifyAlert.getButtonTypes().clear();
        verifyAlert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> selection = verifyAlert.showAndWait();
        if (selection.get() == ButtonType.YES) {
            ChangeRegionName_Transaction transaction = new ChangeRegionName_Transaction(data, oldRegionName, newRegionName);
            app.processTransaction(transaction);
        }
        hide();
    }

    public void updateControls() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        String regionName = data.getRegionName();
        String testRegionName = this.regionNameTextField.getText();
        
        // CASES TO WORRY ABOUT:        
        //   1) SAME NAME REGION
        boolean nameIsSame = testRegionName.equals(regionName);
        //   2) EMPTY STRING
        boolean nameIsEmpty = testRegionName.trim().length() == 0;
        //   3) DUPLICATE TO EXISTING REGION WITH SAME PARENT
        String testWorkFile = data.getWorkFilePath(testRegionName, data.getParentRegionPath());
        boolean namedRegionAlreadyExists = new File(testWorkFile).exists();

        if (nameIsSame) {
            // DISABLE THE CONFIRM BUTTON, NO WARNING NECESSARY
            this.confirmButton.setDisable(true);
            this.warningLabel.visibleProperty().set(false);
        }
        else if (nameIsEmpty) {
            // DISABLE THE CONFIRM BUTTON AND SHOW WARNING MESSAGE
            this.confirmButton.setDisable(true);
            this.warningLabel.visibleProperty().set(true);
            this.warningLabel.setText("Invalid value - name cannot be empty");
        }
        else if (namedRegionAlreadyExists) {
            // DISABLE THE CONFIRM BUTTON AND SHOW WARNING MESSAGE
            this.confirmButton.setDisable(true);
            this.warningLabel.visibleProperty().set(true);
            this.warningLabel.setText("Invalid value - " + testRegionName + " already exists");
        }
        else {
            this.confirmButton.setDisable(false);
            this.warningLabel.visibleProperty().set(false);
        }
    }
    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String dialogTitleText = props.getProperty(RVMM_NAME_DIALOG_TITLE);
        this.setTitle(dialogTitleText);
        
        // LOAD THE REGION NAME INTO THE TEXT FIELD
        String regionName = data.getRegionName();
        this.regionNameTextField.setText(regionName);
        
        updateControls();
        
        this.regionNameTextField.textProperty().addListener(e -> {
            updateControls();
        });

        // AND OPEN THE DIALOG
        showAndWait();
    }
}
