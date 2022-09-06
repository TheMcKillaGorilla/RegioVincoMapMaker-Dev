package rvmm.workspace.dialogs;

import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import rvmm.data.MapPropertyType;
import static rvmm.data.RVMM_Constants.TEMP_BROCHURE_PATH;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.files.RegioVincoFiles;
import rvmm.transactions.ChangeMapProperties_Transaction;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import rvmm.workspace.RegioVincoMapMakerWorkspace;
import static rvmm.workspace.style.RVMMStyle.*;

public class BrochureDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // UI COMPONENTS FOR THIS DIALOG
    BorderPane dialogPane;
    HBox topPane;
    Label brochureHeadingLabel;
    VBox centerPane;
    GridPane controlsPane;
    Label brochureImagePromptLabel;
    TextField brochureImageTextField;
    Button brochureImageRetrieveButton;
    Label brochureLinkPromptLabel;
    TextField brochureLinkTextField;
    ImageView brochureImageView;
    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;

    boolean editing;

    public static BrochureDialog singleton = null;

    public static BrochureDialog getBrochureDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getBrochureDialog");
        if (singleton == null) {
            singleton = new BrochureDialog(initApp);
        }
        return singleton;
    }

    private BrochureDialog(RegioVincoMapMakerApp initApp) {
        app = initApp;

        // KEEP THIS FOR WHEN THE WORK IS ENTERED
        app = initApp;

        initDialogUI();

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);

        // NOW PUT THE GRID IN THE SCENE AND THE SCENE IN THE DIALOG
        Scene scene = new Scene(dialogPane, 1000, 800);
        this.setScene(scene);

        // SETUP THE STYLESHEET
        app.getGUIModule().initStylesheet(this);

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);
    }

    private void initDialogUI() {
        AppNodesBuilder rvmmBuilder = app.getGUIModule().getNodesBuilder();

        topPane = rvmmBuilder.buildHBox(RVMM_BROCHURE_DIALOG_TOP_PANE, null, CLASS_RVMM_DIALOG_TOP_PANE, ENABLED);
        topPane.setAlignment(Pos.CENTER);
        brochureHeadingLabel = rvmmBuilder.buildLabel(RVMM_BROCHURE_DIALOG_HEADING, topPane, CLASS_RVMM_BIG_HEADER, ENABLED);

        centerPane = rvmmBuilder.buildVBox(RVMM_BROCHURE_DIALOG_CONTROLS_PANE, null, CLASS_RVMM_PANE, ENABLED);
        controlsPane = rvmmBuilder.buildGridPane(RVMM_BROCHURE_DIALOG_CENTER_PANE, centerPane, CLASS_RVMM_PANE, ENABLED);
        brochureImagePromptLabel = rvmmBuilder.buildLabel(RVMM_BROCHURE_DIALOG_IMAGE_PROMPT_LABEL, controlsPane, 0, 1, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        brochureImageTextField = rvmmBuilder.buildTextField(RVMM_BROCHURE_DIALOG_IMAGE_TEXT_FIELD, controlsPane, 1, 1, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        brochureImageRetrieveButton = rvmmBuilder.buildTextButton(RVMM_BROCHURE_DIALOG_IMAGE_RETRIEVE_BUTTON, controlsPane, 2, 1, 1, 1, CLASS_RVMM_BUTTON, ENABLED);
        brochureLinkPromptLabel = rvmmBuilder.buildLabel(RVMM_BROCHURE_DIALOG_LINK_PROMPT_LABEL, controlsPane, 0, 2, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        brochureLinkTextField = rvmmBuilder.buildTextField(RVMM_BROCHURE_DIALOG_LINK_TEXT_FIELD, controlsPane, 1, 2, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        brochureImageView = rvmmBuilder.buildImageView(RVMM_BROCHURE_DIALOG_IMAGE_VIEW, controlsPane, 3, 0, 1, 3, CLASS_RVMM_DIALOG_IMAGE_VIEW, ENABLED);
        
        // THE BOTTOM PANE
        bottomPane = rvmmBuilder.buildHBox(RVMM_BROCHURE_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton = rvmmBuilder.buildTextButton(RVMM_BROCHURE_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_BROCHURE_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);

        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        dialogPane.setTop(topPane);
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);

        this.brochureImageTextField.textProperty().addListener(e->{
            updateControls();
        });
        this.brochureLinkTextField.textProperty().addListener(e->{
            updateControls();
        });
        this.brochureImageRetrieveButton.setOnAction(e -> {
            retrieveBrochureImage();
        });
        this.brochureImageView.setOnMouseClicked(e->{
            processGoToBrochureLink();
        });

        // SETUP THE HANDLERS
        confirmButton.setOnAction(e -> {
            confirmEdits();
        });
        cancelButton.setOnAction(e -> {
            cancelEdits();
        });
    }
    
    public void processGoToBrochureLink() {
        String url = this.brochureLinkTextField.getText();
        app.getHostServices().showDocument(url);
    }
    
    private void retrieveBrochureImage() {
        String url = brochureImageTextField.getText();
        try {            
            // FIRST SAVE THE IMAGE
            RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
            RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
            files.downloadImageFile(new URL(url), data.getOrigBrochurePath());
            
            // NOW GET IT
            File origBrochureFile = new File(data.getOrigBrochurePath());
            BufferedImage origImage = ImageIO.read(origBrochureFile);// files.getImageFromURL(url);
            
            // THEN SAVE IT
            //File origBrochureFile = new File(data.getOrigBrochurePath());
            //ImageIO.write(origImage, "png", origBrochureFile);
            
            // UPDATE IT IN THE DIALOG
            double origHeight = origImage.getHeight();
            double scaledHeight = data.getBrochuresHeight();
            double percentage = scaledHeight / origHeight;
            BufferedImage scaledImage = files.getScaledImage(origImage, percentage);
            Image scaledImageFX = SwingFXUtils.toFXImage(scaledImage, null);
            this.brochureImageView.setImage(scaledImageFX);
            
            // UPDATE THE WORKSPACE'S THUMBNAIL
            files.retrieveBrochureImage(data);
            RegioVincoMapMakerWorkspace workspace = (RegioVincoMapMakerWorkspace)app.getWorkspaceComponent();
            workspace.loadBrochureThumbnail();
        } catch (Exception e) {
            System.out.println("Could not load image at " + url);
        }        
    }
    
    public void updateControls() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        String oldBrochureImageURL = data.getMapProperty(MapPropertyType.BROCHURE_IMAGE_URL);
        String oldBrochureLink = data.getMapProperty(MapPropertyType.BROCHURE_LINK);

        // SET THE TWO VALUES IN THE DATA MODEL
        String newBrochureImageURL = this.brochureImageTextField.getText();
        String newBrochureLink = this.brochureLinkTextField.getText();
        
        // IF NOTHING HAS CHANGED, DISABLE CONFIRM
        if (oldBrochureImageURL.equals(newBrochureImageURL)
                && oldBrochureLink.equals(newBrochureLink)) {
            this.confirmButton.setDisable(true);
        }
        else {
            // VALID EDIT
            this.confirmButton.setDisable(false);
        }
        
    }

    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        String titleText = "RVMM - " + data.getRegionName() + " Brochure";
        setTitle(titleText);

        // HAVE THE CARET START IN THE NAME TEXT FIELD
        String brochureImageURL = data.getBrochureImageURL();
        this.brochureImageTextField.setText(brochureImageURL);
        String brochureLink = data.getBrochureLink();
        this.brochureLinkTextField.setText(brochureLink);

        // LOAD THE CORRECT BROCHURE IMAGE
        loadLocalBrochureImage();
        
        // FOOLPROOF DESIGN
        updateControls();

        // AND OPEN THE DIALOG
        showAndWait();
    }

    private void loadLocalBrochureImage() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        String path = data.getOrigBrochurePath();
        File brochureFile = new File(path);
        try {
            if (brochureFile.exists()) {
                URL brochureURL = brochureFile.toURI().toURL();
                BufferedImage tempBrochureImage = ImageIO.read(brochureURL);
                // SCALE THE IMAGE TO FIT THE UI
                double percentage = 300.0/tempBrochureImage.getHeight();
                RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
                BufferedImage scaledBrochureImage = files.getScaledImage(tempBrochureImage, percentage);
                Image scaledImageFX = SwingFXUtils.toFXImage(scaledBrochureImage, null);
                this.brochureImageView.setImage(scaledImageFX);
            }
            else {
                RegioVincoMapMakerWorkspace workspace = (RegioVincoMapMakerWorkspace)app.getWorkspaceComponent();
                loadTempBrochure();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadTempBrochure() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        File tempBrochureFile = new File(TEMP_BROCHURE_PATH);
        try {
            URL tempBrochureURL = tempBrochureFile.toURI().toURL();
            BufferedImage tempBrochureImage = ImageIO.read(tempBrochureURL);
            int origHeight = tempBrochureImage.getHeight();
            AppGUIModule gui = app.getGUIModule();
            double desiredHeight = data.getBrochuresHeight();
            double percentage = desiredHeight / origHeight;
            if (percentage <= 0.0) {
                // UI HAS NOT LOADED YET
                return;
            }
            RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
            BufferedImage scaledBrochureImage = files.getScaledImage(tempBrochureImage, percentage);
            Image scaledImageFX = SwingFXUtils.toFXImage(scaledBrochureImage, null);
            this.brochureImageView.setImage(scaledImageFX);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void confirmEdits() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();

        HashMap<MapPropertyType, String> oldProperties = new HashMap();
        oldProperties.put(MapPropertyType.BROCHURE_IMAGE_URL, data.getMapProperty(MapPropertyType.BROCHURE_IMAGE_URL));
        oldProperties.put(MapPropertyType.BROCHURE_LINK, data.getMapProperty(MapPropertyType.BROCHURE_LINK));

        // SET THE TWO VALUES IN THE DATA MODEL
        String brochureImageURL = this.brochureImageTextField.getText();
        String brochureLink = this.brochureLinkTextField.getText();
        HashMap<MapPropertyType, String> newProperties = new HashMap();
        newProperties.put(MapPropertyType.BROCHURE_IMAGE_URL, brochureImageURL);
        newProperties.put(MapPropertyType.BROCHURE_LINK, brochureLink); 

        ChangeMapProperties_Transaction transaction = new ChangeMapProperties_Transaction(data, oldProperties, newProperties);
        app.processTransaction(transaction);

        // CLOSE THE DIALOG
        this.hide();
    }

    public void cancelEdits() {
        // AND CLOSE THE DIALOG
        hide();
    }
}
