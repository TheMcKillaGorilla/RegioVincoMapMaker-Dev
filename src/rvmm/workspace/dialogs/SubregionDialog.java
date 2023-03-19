package rvmm.workspace.dialogs;

import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import rvmm.data.MapPropertyType;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;
import rvmm.files.RegioVincoFiles;
import rvmm.transactions.SubregionsEdits_Transaction;
import rvmm.workspace.DebugDisplay;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import static rvmm.workspace.style.RVMMStyle.*;

public class SubregionDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // EVERYTHING GOES IN HERE
    BorderPane dialogPane;

    // AT THE TOP OF THE DIALOG
    HBox topPane;
    Label editSubregionLabel;
    Button previousButton;
    Button nextButton;

    // CONTROLS FOR EDITING DETAILS ABOUT SUBREGIONS
    GridPane centerLeftPane;
    Label namePromptLabel;
    TextField nameTextField;
    Label capitalPromptLabel;
    TextField capitalTextField;
    Label leaderPromptLabel;
    TextField leaderTextField;
    Label flagLinkPromptLabel;
    TextField flagLinkTextField;
    Label territoryPromptLabel;
    CheckBox territoryCheckBox;
    ImageView flagImageView;
    HBox downloadFlagsPane;
    Button downloadFlagImageButton;
    Button downloadAllFlagImagesButton;

    // FOR EDITING LANDMARKS
    VBox centerRightPane;
    HBox topLandmarksPane;
    Label landmarksLabel;
    HBox addLandmarkPane;
    Label addLandmarkPromptLabel;
    TextField addLandmarkTextField;
    Button addLandmarkButton;
    ListView<String> landmarksListView;
    GridPane landmarksDescriptionPane;
    Label landmarksDescriptionLabel;
    TextField landmarksDescriptionTextField;
    Label landmarksSourceURLLabel;
    TextField landmarksSourceURLTextField;
    
    // WE PUT THE SUBREGION AND LANDMARK EDITING CONTROLS HERE
    SplitPane centerPane;

    // SOUTH CONTROLS
    HBox bottomPane;
    Button okButton;
    Button cancelButton;

    // THE SUBREGION CURRENTLY LOADED AND BEING EDITED BY THIS DIALOG    
    SubregionPrototype currentSubregion;
    String originalLandmarksDescription;
    String originalLandmarksSourceURL;

    // THIS STORES THE EDITS THAT HAVE HAPPENED BUT HAVE
    // NOT YET BEEN CONFIRMED
    HashMap<SubregionPrototype, SubregionPrototype> clonedSubregions;

    // KEEPS TRACK OF WHETHER WE HAVE MADE EDITS OR NOT
    boolean editing;
    boolean handlersEnabled;

    public static SubregionDialog singleton = null;

    public static SubregionDialog getSubregionDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getSubregionDialog");
        if (singleton == null) {
            singleton = new SubregionDialog(initApp);
        }
        return singleton;
    }

    private SubregionDialog(RegioVincoMapMakerApp initApp) {
        app = initApp;
        handlersEnabled = false;
        clonedSubregions = new HashMap();

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

        // THE TOP PANE        
        topPane = rvmmBuilder.buildHBox(RVMM_SUBREGION_DIALOG_TOP_PANE, null, CLASS_RVMM_DIALOG_TOP_PANE, ENABLED);
        topPane.setAlignment(Pos.CENTER);
        previousButton = rvmmBuilder.buildIconButton(RVMM_SUBREGION_DIALOG_PREVIOUS_BUTTON, topPane, CLASS_RVMM_CIRCLE_BUTTON, ENABLED);
        editSubregionLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_HEADER_LABEL, topPane, CLASS_RVMM_DIALOG_HEADER, ENABLED);
        nextButton = rvmmBuilder.buildIconButton(RVMM_SUBREGION_DIALOG_NEXT_BUTTON, topPane, CLASS_RVMM_CIRCLE_BUTTON, ENABLED);

        // CONTROLS ON THE LEFT
        centerLeftPane = rvmmBuilder.buildGridPane(RVMM_SUBREGION_DIALOG_CENTER_LEFT_PANE, null, CLASS_RVMM_DIALOG_GRID, ENABLED);
        centerLeftPane.setAlignment(Pos.TOP_CENTER);
        namePromptLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_NAME_PROMPT_LABEL, centerLeftPane, 0, 0, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        nameTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_NAME_TEXT_FIELD, centerLeftPane, 1, 0, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        capitalPromptLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_CAPITAL_PROMPT_LABEL, centerLeftPane, 0, 1, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        capitalTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_CAPITAL_TEXT_FIELD, centerLeftPane, 1, 1, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        leaderPromptLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_LEADER_PROMPT_LABEL, centerLeftPane, 0, 2, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        leaderTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_LEADER_TEXT_FIELD, centerLeftPane, 1, 2, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        flagLinkPromptLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_FLAG_LINK_PROMPT_LABEL, centerLeftPane, 0, 3, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        flagLinkTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_FLAG_LINK_TEXT_FIELD, centerLeftPane, 1, 3, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        territoryPromptLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_TERRITORY_PROMPT_LABEL, centerLeftPane, 0, 4, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        territoryCheckBox = rvmmBuilder.buildCheckBox(RVMM_SUBREGION_DIALOG_TERRITORY_CHECKBOX, centerLeftPane, 1, 4, 1, 1, CLASS_RVMM_CHECKBOX, ENABLED);
        flagImageView = rvmmBuilder.buildImageView(RVMM_SUBREGION_DIALOG_FLAG_IMAGE_VIEW, centerLeftPane, 0, 5, 1, 2, CLASS_RVMM_DIALOG_IMAGE_VIEW, ENABLED);
        downloadFlagsPane = rvmmBuilder.buildHBox(RVMM_SUBREIONG_DIALOG_DOWNLOAD_FLAG_PANE, centerLeftPane, 1, 5, 2, 2, CLASS_RVMM_PANE, ENABLED);
        downloadFlagsPane.setAlignment(Pos.CENTER);
        downloadFlagImageButton = rvmmBuilder.buildTextButton(RVMM_SUBREGION_DIALOG_DOWNLOAD_FLAG_IMAGE_BUTTON, downloadFlagsPane, CLASS_RVMM_CIRCLE_BUTTON, ENABLED);
        downloadAllFlagImagesButton = rvmmBuilder.buildTextButton(RVMM_SUBREGION_DIALOG_DOWNLOAD_ALL_FLAG_IMAGES_BUTTON, downloadFlagsPane, CLASS_RVMM_CIRCLE_BUTTON, ENABLED);

        // LET'S HAVE THESE BUTTONS FILL THEIR CELL
        for (int colIndex = 0; colIndex < 2; colIndex++) {
            ColumnConstraints cc = new ColumnConstraints();
            if (colIndex == 1) {
                cc.setHgrow(Priority.ALWAYS);
                cc.setFillWidth(true);
            }
            centerLeftPane.getColumnConstraints().add(cc);
        }
        
        // CONTROLS ON THE RIGHT ARE FOR LANDMARKS
        centerRightPane = rvmmBuilder.buildVBox(RVMM_SUBREGION_DIALOG_CENTER_RIGHT_PANE, null, CLASS_RVMM_DIALOG_GRID, ENABLED);
        topLandmarksPane = rvmmBuilder.buildHBox(RVMM_SUBREGION_DIALOG_TOP_LANDMARKS_PANE, centerRightPane, CLASS_RVMM_PANE, ENABLED);
        landmarksLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_LANDMARKS_LABEL, topLandmarksPane, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        topLandmarksPane.setAlignment(Pos.CENTER);
        addLandmarkPane = rvmmBuilder.buildHBox(RVMM_SUBREGION_DIALOG_ADD_LANDMARK_PANE, centerRightPane, CLASS_RVMM_PANE, ENABLED);
        addLandmarkPane.setAlignment(Pos.CENTER_LEFT);
        addLandmarkPromptLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_ADD_LANDMARK_PROMPT_LABEL, addLandmarkPane, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        addLandmarkTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_ADD_LANDMARK_TEXT_FIELD, addLandmarkPane, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        addLandmarkButton = rvmmBuilder.buildTextButton(RVMM_SUBREGION_DIALOG_ADD_LANDMARK_BUTTON, addLandmarkPane, CLASS_RVMM_BUTTON, ENABLED);
        landmarksListView = rvmmBuilder.buildListView(RVMM_SUBREGION_DIALOG_LANDMARKS_LIST_VIEW, centerRightPane, CLASS_RVMM_LANDMARKS_LIST_VIEW, ENABLED);
        landmarksDescriptionPane = rvmmBuilder.buildGridPane(RVMM_SUBREGION_DIALOG_LANDMARKS_DESCRIPTION_PANE, centerRightPane, CLASS_RVMM_PANE, ENABLED);
        landmarksDescriptionLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_LANDMARKS_DESCRIPTION_LABEL, landmarksDescriptionPane, 0, 0, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        landmarksDescriptionTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_LANDMARKS_DESCRIPTION_TEXT_FIELD, landmarksDescriptionPane, 1, 0, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);
        landmarksSourceURLLabel = rvmmBuilder.buildLabel(RVMM_SUBREGION_DIALOG_LANDMARKS_SOURCE_URL_LABEL, landmarksDescriptionPane, 0, 1, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        landmarksSourceURLTextField = rvmmBuilder.buildTextField(RVMM_SUBREGION_DIALOG_LANDMARKS_SOURCE_URL_TEXT_FIELD, landmarksDescriptionPane, 1, 1, 1, 1, CLASS_RVMM_DIALOG_TEXT_FIELD, ENABLED);

        centerPane = rvmmBuilder.buildSplitPane(RVMM_SUBREGION_DIALOG_CENTER_PANE, dialogPane, EMPTY_TEXT, ENABLED);
        centerPane.getItems().add(centerLeftPane);
        centerPane.getItems().add(centerRightPane);

        // THE BOTTOM PANE
        bottomPane = rvmmBuilder.buildHBox(RVMM_SUBREGION_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        okButton = rvmmBuilder.buildTextButton(RVMM_SUBREGION_DIALOG_OK_BUTTON, bottomPane, CLASS_RVMM_CIRCLE_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_SUBREGION_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_CIRCLE_BUTTON, ENABLED);

        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        dialogPane.setTop(topPane);
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);

        // SETUP THE HANDLERS
        nameTextField.textProperty().addListener(e -> {
            updateClonedCurrentSubregion();
        });
        capitalTextField.textProperty().addListener(e -> {
            updateClonedCurrentSubregion();
        });
        leaderTextField.textProperty().addListener(e -> {
            updateClonedCurrentSubregion();
        });
        flagLinkTextField.textProperty().addListener(e -> {
            updateClonedCurrentSubregion();
        });
        territoryCheckBox.setOnAction(e -> {
            updateClonedCurrentSubregion();
        });
        downloadFlagImageButton.setOnAction(e->{
            processDownloadFlagImage();
        });
        downloadAllFlagImagesButton.setOnAction(e->{
            processDownloadAllFlagImages();
        });
        previousButton.setOnAction(e -> {
            goToPreviousSubregion();
        });
        nextButton.setOnAction(e -> {
            goToNextSubregion();
        });
        nameTextField.setOnAction(e -> {
            processCompleteWork();
        });
        capitalTextField.setOnAction(e -> {
            processCompleteWork();
        });
        leaderTextField.setOnAction(e -> {
            processCompleteWork();
        });
        flagLinkTextField.setOnAction(e -> {
            processCompleteWork();
        });
        addLandmarkButton.setOnAction(e -> {
            processAddLandmark();
        });
        addLandmarkTextField.setOnAction(e -> {
            processAddLandmark();
        });
        okButton.setOnAction(e -> {
            processCompleteWork();
        });
        cancelButton.setOnAction(e -> {
            cancelEdits();
        });
        this.flagImageView.setOnMouseClicked(e->{
            processGoToFlagImage();
        });
        
        // WE NEED TO CUSTOMLY DRAW THE LIST ITEMS SO THAT
        // EACH ONE HAS A DELETE BUTTON
        this.landmarksListView.setCellFactory(new Callback<ListView<String>, ListCell<String>> () {
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    public void updateItem(String landmark, boolean empty) {
                        super.updateItem(landmark, empty);
                        if (empty || (landmark == null)) {
                            setGraphic(null);
                            setText(null);
                        }
                        if (!empty) {
                            DebugDisplay.appendDebugText("List View Item: " + landmark);
                            Button removeButton = new Button("❌");
                            removeButton.getStyleClass().add(CLASS_RVMM_DELETE_BUTTON);
                            removeButton.setOnAction(e -> {
                                processRemoveLandmark(landmark);
                            });
                            setGraphic(removeButton);
                            setText(landmark);
                        }
                    }
                };
            }
        });
    }
    
    public void processDownloadFlagImage() {
        String flagImageURL = this.flagLinkTextField.getText();
        RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        String outputPath = data.getOrigFlagPath(currentSubregion);
        files.downloadFlag(currentSubregion, flagImageURL, outputPath);
        loadFlag(currentSubregion);
        
        // @todo - FORCE THE FLAG TO LOAD IN THE WORKSPACE
    }

    public void processDownloadAllFlagImages() {
        // NOW GET ALL THE FLAG IMAGES IF THEY ARE THERE        
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
        files.downloadFlagImages(data);    
        loadFlag(currentSubregion);
    }
    
    public void processGoToFlagImage() {
        String url = this.flagLinkTextField.getText();
        if (url.length() > 0)
            app.getHostServices().showDocument(url);        
    }

    public void processAddLandmark() {
        String landmarkToAdd = addLandmarkTextField.getText();
        landmarkToAdd = landmarkToAdd.trim();
        ObservableList<String> listItems = landmarksListView.getItems();

        // DON'T ADD IT IF IT ALREADY EXISTS IN THE LIST
        if (listItems.contains(landmarkToAdd))
            return;

        // ADD IT TO THE DISPLAYED LIST
        if (landmarkToAdd.length() > 0) {
            listItems.add(landmarkToAdd);
        }
        
        // AND SORT THE LIST
        Collections.sort(listItems);
        addLandmarkTextField.setText(EMPTY_TEXT);
        updateClonedCurrentSubregion();
    }

    public void processRemoveLandmark(String landmark) {
        ObservableList<String> listItems = landmarksListView.getItems();
        listItems.remove(landmark);
        updateClonedCurrentSubregion();
    }

    private void processCompleteWork() {
        // SINCE OK WAS PRESSED WE NEED TO TAKE ALL THE CLONES
        // ALONG WITH
        confirmEdits();

        // CLOSE THE DIALOG
        this.hide();
    }

    public void showDialog(SubregionPrototype initCurrentSubregion) {
        this.disableHandlers();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        Label headingLabel = (Label) gui.getGUINode(RVMM_SUBREGION_DIALOG_HEADER_LABEL);
        String subregionType = data.getSubregionType();
        if (subregionType.endsWith("y")) {
            subregionType = subregionType.substring(0, subregionType.length()-1);
            subregionType += "ie";
        }
        subregionType += "s";
        headingLabel.setText(data.getRegionName() + " " + subregionType);

        // FIRST CLEAR OUT THE OLD CLONES
        clonedSubregions.clear();
        
        // CLEAR OUT THE FLAG
        this.flagImageView.setImage(null);

        // AND CLEAR OUT THE OLD UI VALUES
        nameTextField.setText(EMPTY_TEXT);
        capitalTextField.setText(EMPTY_TEXT);
        leaderTextField.setText(EMPTY_TEXT);
        flagLinkTextField.setText(EMPTY_TEXT);
        territoryCheckBox.setSelected(false);
        landmarksListView.getItems().clear();

        String landmarksDescPrompt = data.getRegionName() + " Landmarks Description: ";
        this.landmarksDescriptionLabel.setText(landmarksDescPrompt);
        String landmarksDescription = data.getMapProperty(MapPropertyType.LANDMARKS_DESCRIPTION);
        this.landmarksDescriptionTextField.setText(landmarksDescription);
        this.originalLandmarksDescription = landmarksDescription;
        String landmarksSourceURLPrompt = data.getRegionName() + " Landmarks Source URL: ";
        this.landmarksSourceURLLabel.setText(landmarksSourceURLPrompt);
        String landmarksSourceURL = data.getMapProperty(MapPropertyType.LANDMARKS_SOURCE_URL);
        this.landmarksSourceURLTextField.setText(landmarksSourceURL);
        this.originalLandmarksSourceURL = landmarksSourceURL;

        // NOW CLONE THE ONE WE ARE EDITING FIRST
        cloneAndLoadSubregion(initCurrentSubregion);

        // HAVE THE CARET START IN THE NAME TEXT FIELD
        nameTextField.requestFocus();

        this.enableHandlers();

        // AND OPEN THE DIALOG
        showAndWait();
    }

    public void loadLandmarks(ObservableList<String> landmarks) {
        ObservableList<String> items = landmarksListView.getItems();
        items.clear();
        for (String s : landmarks) {
            items.add(s);
        }
    }

    public void disableHandlers() {
        handlersEnabled = false;
    }

    public void enableHandlers() {
        handlersEnabled = true;
    }

    public void cloneAndLoadSubregion(SubregionPrototype subregion) {
        currentSubregion = subregion;
        SubregionPrototype clonedSubregion = clonedSubregions.get(subregion);
        if (clonedSubregion == null) {
            clonedSubregion = subregion.partialClone();
            clonedSubregions.put(subregion, clonedSubregion);
        }

        String clonedName = clonedSubregion.getName();
        String clonedCapital = clonedSubregion.getCapital();
        String clonedLeader = clonedSubregion.getLeader();
        String clonedFlagLink = clonedSubregion.getFlagLink();
        boolean clonedIsTerritory = clonedSubregion.getIsTerritory();
        ObservableList<String> clonedLandmarks = clonedSubregion.cloneLandmarks();

        // MAKE SURE WE DON'T CARRY OVER OLD LANDMARKS
        nameTextField.setText(clonedName);
        capitalTextField.setText(clonedCapital);
        leaderTextField.setText(clonedLeader);
        flagLinkTextField.setText(clonedFlagLink);
        territoryCheckBox.setSelected(clonedIsTerritory);
        loadLandmarks(clonedLandmarks);
        loadFlag(clonedSubregion);
    }

    public void loadFlag(SubregionPrototype subregion) {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
        if (data.getSubregionHasFlag(subregion)) {
            String flagPath = data.getOrigFlagPath(subregion);
            File flagFile = new File(flagPath);
            try {
                URL flagURL = flagFile.toURI().toURL();
                BufferedImage flagImage = ImageIO.read(flagURL);
                double origWidth = flagImage.getWidth();
                double scaledWidth = data.getFlagsWidth();
                double percentage = scaledWidth/origWidth;
                BufferedImage scaledImage = files.getScaledImage(flagImage, percentage);
                Image scaledImageFX = SwingFXUtils.toFXImage(scaledImage, null);
                flagImageView.setImage(scaledImageFX);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateClonedCurrentSubregion() {
        if (handlersEnabled) {
            SubregionPrototype clonedCurrentSubregion = clonedSubregions.get(currentSubregion);
            clonedCurrentSubregion.setName(nameTextField.getText());
            clonedCurrentSubregion.setCapital(capitalTextField.getText());
            clonedCurrentSubregion.setLeader(leaderTextField.getText());
            clonedCurrentSubregion.setFlagLink(flagLinkTextField.getText());
            clonedCurrentSubregion.setIsTerritory(territoryCheckBox.isSelected());
            clonedCurrentSubregion.loadLandmarks(landmarksListView.getItems());
        }
    }

    public HashMap<SubregionPrototype, SubregionPrototype> getClonedSubregions() {
        return clonedSubregions;
    }

    public void goToPreviousSubregion() {
        this.disableHandlers();
        // CLEAR OUT THE FLAG
        this.flagImageView.setImage(null);
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        ObservableList<SubregionPrototype> subregions = data.getSubregions();
        // FIRST MOVE THE CURRENT SUBREGION
        int currentIndex = subregions.indexOf(currentSubregion);
        int previousIndex = currentIndex - 1;
        if (previousIndex < 0) {
            previousIndex = subregions.size() - 1;
        }

        // MOVE ON TO THE PREVIOUS SUBREGION
        SubregionPrototype previousSubregion = subregions.get(previousIndex);
        TableView subregionsTable = (TableView) (app.getGUIModule().getGUINode(RVMM_SUBREGIONS_TABLE_VIEW));
        subregionsTable.getSelectionModel().select(previousSubregion);
        data.selectSubregion(previousSubregion);
        cloneAndLoadSubregion(previousSubregion);
        this.enableHandlers();
    }

    public void goToNextSubregion() {
        this.disableHandlers();
        // CLEAR OUT THE FLAG
        this.flagImageView.setImage(null);
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        ObservableList<SubregionPrototype> subregions = data.getSubregions();

        // FIRST MOVE THE CURRENT SUBREGION
        int currentIndex = subregions.indexOf(currentSubregion);
        int nextIndex = currentIndex + 1;
        if (nextIndex == subregions.size()) {
            nextIndex = 0;
        }

        // MOVE ON THE THE PREVIOUS SUBREGION
        SubregionPrototype nextSubregion = subregions.get(nextIndex);
        TableView subregionsTable = (TableView) (app.getGUIModule().getGUINode(RVMM_SUBREGIONS_TABLE_VIEW));
        subregionsTable.getSelectionModel().select(nextSubregion);
        data.selectSubregion(nextSubregion);
        cloneAndLoadSubregion(nextSubregion);
        this.enableHandlers();
    }

    public void confirmEdits() {
        // FIRST GET THE ONES WE EDITED
        HashMap<SubregionPrototype, SubregionPrototype> oldValues = new HashMap();
        HashMap<SubregionPrototype, SubregionPrototype> newValues = new HashMap();
        Iterator<SubregionPrototype> editedSubregionsIt = clonedSubregions.keySet().iterator();
        while (editedSubregionsIt.hasNext()) {
            SubregionPrototype originalSubregion = editedSubregionsIt.next();
            SubregionPrototype editedSubregion = clonedSubregions.get(originalSubregion);

            // SEND CLONES OF THE ORIGINALS
            SubregionPrototype oldClone = originalSubregion.partialClone();
            oldValues.put(originalSubregion, oldClone);

            // AND SEND THE EDITED ONES
            newValues.put(originalSubregion, editedSubregion);
        }
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        HashMap<MapPropertyType, String> oldProps = new HashMap();
        oldProps.put(MapPropertyType.LANDMARKS_DESCRIPTION, data.getMapProperty(MapPropertyType.LANDMARKS_DESCRIPTION));
        oldProps.put(MapPropertyType.LANDMARKS_SOURCE_URL, data.getMapProperty(MapPropertyType.LANDMARKS_SOURCE_URL));
        HashMap<MapPropertyType, String> newProps = new HashMap();
        newProps.put(MapPropertyType.LANDMARKS_DESCRIPTION, this.landmarksDescriptionTextField.getText());
        newProps.put(MapPropertyType.LANDMARKS_SOURCE_URL, this.landmarksSourceURLTextField.getText());
        SubregionsEdits_Transaction transaction = new SubregionsEdits_Transaction(data, oldValues, newValues, oldProps, newProps);
        app.processTransaction(transaction);
    }

    public void cancelEdits() {
        // GET RID OF ALL EDITS
        clonedSubregions.clear();

        // AND CLOSE THE DIALOG
        hide();
    }
}
