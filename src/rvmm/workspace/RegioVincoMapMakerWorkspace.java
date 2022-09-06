package rvmm.workspace;

import static djf.AppPropertyType.APP_FILE_FOOLPROOF_SETTINGS;
import static djf.AppPropertyType.EXPORT_BUTTON;
import djf.components.AppWorkspaceComponent;
import djf.modules.AppFoolproofModule;
import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import djf.ui.foolproof.FoolproofDesign;
import static djf.ui.style.DJFStyle.*;
import java.awt.image.BufferedImage;
import java.beans.EventHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javafx.beans.value.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.imageio.ImageIO;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import static rvmm.data.RVMM_Constants.*;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;
import rvmm.files.RegioVincoFiles;
import rvmm.workspace.controllers.*;
import rvmm.workspace.dialogs.ExportMapDialog;
import rvmm.workspace.dialogs.BrochureDialog;
import rvmm.workspace.dialogs.LeadersDialog;
import rvmm.workspace.dialogs.ParentRegionDialog;
import rvmm.workspace.dialogs.NameDialog;
import rvmm.workspace.dialogs.NewMapDialog;
import rvmm.workspace.dialogs.SubregionDialog;
import static rvmm.workspace.style.RVMMStyle.*;

public class RegioVincoMapMakerWorkspace extends AppWorkspaceComponent {
    BrochureDialog brochureDialog;
    ExportMapDialog exportMapDialog;
    NameDialog nameDialog;
    LeadersDialog leadersDialog;
    ParentRegionDialog mapSettingsDialog;
    NewMapDialog newMapDialog;
    SubregionDialog subregionDialog;

    MapEditController mapEditController;
    int NUM_SUBREGION_COLUMNS = 5;
    HashMap<String, EventHandler> eventHandlers = new HashMap();

    public RegioVincoMapMakerWorkspace(RegioVincoMapMakerApp app) {
        super(app);

        // LAYOUT THE APP
        initLayout();

        // SETUP ALL EVENT HANDLING
        initControllers();

        // SETUP CONTROL ENABLING/DISABLING
        initFoolproofDesign();

        // WE'LL NEED THESE DIALOGS
        brochureDialog = BrochureDialog.getBrochureDialog(app);
        exportMapDialog = ExportMapDialog.getExportMapDialog(app);
        leadersDialog = LeadersDialog.getLeadersDialog(app);
        mapSettingsDialog = ParentRegionDialog.getMapSettingsDialog(app);
        nameDialog = NameDialog.getNameDialog(app);
        newMapDialog = NewMapDialog.getNewMapDialog(app);
        subregionDialog = SubregionDialog.getSubregionDialog(app);
    }

    public MapEditController getMapEditController() {
        return mapEditController;
    }

    // THIS HELPER METHOD INITIALIZES ALL THE CONTROLS IN THE WORKSPACE
    private void initLayout() {
        // THESE WILL STORE AND BUILD ALL OF OUR JavaFX COMPONENTS FOR US
        AppGUIModule gui = app.getGUIModule();
        AppNodesBuilder rvmmBuilder = gui.getNodesBuilder();
    
        // FIRST ADD THE DELETE MAP BUTTON TO THE FILE TOOLBAR
        HBox fileToolbar = gui.getFileToolbar();
        rvmmBuilder.buildIconButton(RVMM_DELETE_MAP_BUTTON, fileToolbar, CLASS_DJF_ICON_BUTTON, !ENABLED);       

        // PUT ALL THESE TOOLBARS IN THE TOP
        HBox topToolbarPane = gui.getTopToolbarPane();

        // NAVIGATION PANE
        HBox editingPane = rvmmBuilder.buildHBox(RVMM_EDITING_PANE, topToolbarPane, CLASS_DJF_TOOLBAR_PANE, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_RESET_NAVIGATION_BUTTON, editingPane, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_FIT_TO_SUBREGION_BUTTON, editingPane, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_SHUFFLE_MAP_COLORS_BUTTON, editingPane, CLASS_DJF_ICON_BUTTON, ENABLED);

        // OPTIONS PANE
        HBox optionsPane = rvmmBuilder.buildHBox(RVMM_OPTIONS_PANE, topToolbarPane, CLASS_DJF_TOOLBAR_PANE, ENABLED);
        rvmmBuilder.buildLabel(RVMM_TOGGLE_DEBUG_LABEL, optionsPane, CLASS_RVMM_PROMPT, ENABLED);
        rvmmBuilder.buildCheckBox(RVMM_TOGGLE_DEBUG_CHECKBOX, optionsPane, CLASS_RVMM_CHECKBOX, ENABLED);

        // MAP SETTINGS PANE
        HBox settingsPane = rvmmBuilder.buildHBox(RVMM_SETTINGS_PANE, topToolbarPane, CLASS_DJF_TOOLBAR_PANE, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_EDIT_LEADERS_BUTTON, settingsPane, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_EDIT_PARENT_REGION_BUTTON, settingsPane, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_EDIT_BROCHURE_BUTTON, settingsPane, CLASS_DJF_ICON_BUTTON, ENABLED);
                
        // ZOOM IN AND OUT
        HBox zoomPane = rvmmBuilder.buildHBox(RVMM_ZOOM_PANE, topToolbarPane, CLASS_DJF_TOOLBAR_PANE, ENABLED);
        rvmmBuilder.buildLabel(RVMM_ZOOM_LABEL, zoomPane, CLASS_RVMM_PROMPT, ENABLED);
        rvmmBuilder.buildTextField(RVMM_ZOOM_TEXT_FIELD, zoomPane, CLASS_RVMM_TEXT_FIELD, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_ZOOM_IN_BUTTON, zoomPane, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_ZOOM_OUT_BUTTON, zoomPane, CLASS_DJF_ICON_BUTTON, ENABLED);
        
        // FIRST SETUP THE PANES WE'LL USE FOR THE MAP
        Pane leftPane = new Pane();
        Pane clippedMapPane = new Pane();
        Pane mapPane = new Pane();
        Pane frameLayerPane = new Pane();
        gui.addGUINode(RVMM_LEFT_PANE, leftPane);
        gui.addGUINode(RVMM_CLIPPED_MAP_PANE, clippedMapPane);
        gui.addGUINode(RVMM_MAP_PANE, mapPane);
        gui.addGUINode(RVMM_FRAME_LAYER_PANE, frameLayerPane);

        Rectangle leftClipper = new Rectangle(0, 0, DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
        leftPane.setClip(leftClipper);
        gui.addGUINode(RVMM_MAP_CLIPPING_RECTANGLE, leftClipper);
        leftPane.getChildren().add(clippedMapPane);
        Rectangle mapClipper = new Rectangle(0, 0, DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
        clippedMapPane.setClip(mapClipper);
        clippedMapPane.getChildren().add(mapPane);
        mapPane.translateXProperty().set(0);
        mapPane.translateYProperty().set(0);

        BackgroundFill bgFill = new BackgroundFill(Color.PALETURQUOISE, CornerRadii.EMPTY, Insets.EMPTY);
        Background bg = new Background(bgFill);
        leftPane.setBackground(bg);

        // THIS WILL GO IN THE RIGHT HALF
        BorderPane rightPane = new BorderPane();

        // THIS IS THE TOP PANE
        HBox editSubregionsBox = rvmmBuilder.buildHBox(RVMM_EDIT_SUBREIONS_BOX, null, CLASS_RVMM_PANE, ENABLED);
        rvmmBuilder.buildImageView(RVMM_BROCHURE_THUMBNAIL_IMAGE_VIEW, editSubregionsBox, CLASS_RVMM_THUMBNAIL_IMAGE_VIEW, ENABLED);
        VBox regionNameBox = rvmmBuilder.buildVBox(RVMM_REGION_NAME_BOX, editSubregionsBox, CLASS_RVMM_PANE, ENABLED);
        rvmmBuilder.buildLabel(RVMM_REGION_NAME_LABEL, regionNameBox, CLASS_RVMM_SMALL_HEADER, ENABLED);
        rvmmBuilder.buildComboBox(RVMM_SUBREGION_TYPE_COMBO_BOX, RVMM_SUBREGION_TYPES, RVMM_DEFAULT_SUBREGION_TYPE, regionNameBox, CLASS_RVMM_COMBO_BOX, ENABLED);
        GridPane editSubregionsGridPane = rvmmBuilder.buildGridPane(RVMM_EDIT_SUBREGIONS_GRID_PANE, editSubregionsBox, CLASS_RVMM_PANE, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_ADD_SUBREGIONS_BUTTON, editSubregionsGridPane, 0, 0, 1, 1, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_REMOVE_SUBREGION_BUTTON, editSubregionsGridPane, 0, 1, 1, 1, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_MOVE_UP_BUTTON, editSubregionsGridPane, 1, 0, 1, 1, CLASS_DJF_ICON_BUTTON, ENABLED);
        rvmmBuilder.buildIconButton(RVMM_MOVE_DOWN_BUTTON, editSubregionsGridPane, 1, 1, 1, 1, CLASS_DJF_ICON_BUTTON, ENABLED);
        editSubregionsGridPane.heightProperty().addListener(e->{ loadBrochureThumbnail(); });
        rvmmBuilder.buildLabel(RVMM_SUBREGION_TYPE_PROMPT_LABEL, regionNameBox, CLASS_RVMM_PROMPT, ENABLED);
        TextArea debugTextArea = rvmmBuilder.buildTextArea(RVMM_DEBUG_TEXT_AREA, null, CLASS_RVMM_TEXT_AREA, ENABLED);
        DebugDisplay.setDebugTextArea(debugTextArea);

        TableView<SubregionPrototype> subregionsTableView = rvmmBuilder.buildTableView(RVMM_SUBREGIONS_TABLE_VIEW, null, CLASS_RVMM_TABLE_VIEW, ENABLED);
        TableColumn<String, String> nameColumn = rvmmBuilder.buildTableColumn(RVMM_NAME_TABLE_COLUMN, subregionsTableView, CLASS_RVMM_TABLE_COLUMN);
        nameColumn.setCellValueFactory(new PropertyValueFactory<String, String>(SubregionPrototype.NAME));
        TableColumn<String, String> capitalColumn = rvmmBuilder.buildTableColumn(RVMM_CAPITAL_TABLE_COLUMN, subregionsTableView, CLASS_RVMM_TABLE_COLUMN);
        capitalColumn.setCellValueFactory(new PropertyValueFactory<String, String>(SubregionPrototype.CAPITAL));
        TableColumn<String, String> leaderColumn = rvmmBuilder.buildTableColumn(RVMM_LEADER_TABLE_COLUMN, subregionsTableView, CLASS_RVMM_TABLE_COLUMN);
        leaderColumn.setCellValueFactory(new PropertyValueFactory<String, String>(SubregionPrototype.LEADER));
        TableColumn<String, String> landmarksColumn = rvmmBuilder.buildTableColumn(RVMM_LANDMARKS_TABLE_COLUMN, subregionsTableView, CLASS_RVMM_TABLE_COLUMN);
        landmarksColumn.setCellValueFactory(new PropertyValueFactory<String, String>(SubregionPrototype.LANDMARKS));
        TableColumn<SubregionPrototype, SubregionPrototype> flagLinkColumn = rvmmBuilder.buildTableColumn(RVMM_FLAG_LINK_TABLE_COLUMN, subregionsTableView, CLASS_RVMM_TABLE_COLUMN);
        flagLinkColumn.setCellValueFactory(new PropertyValueFactory<SubregionPrototype, SubregionPrototype>(SubregionPrototype.SELF));
        flagLinkColumn.setCellFactory(col -> new TableCell<SubregionPrototype, SubregionPrototype>() {
            @Override
            protected void updateItem(SubregionPrototype subregion, boolean empty) {
                super.updateItem(subregion, empty);
                if (subregion == null)
                    return;
                RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
                RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
                String flagPath = data.getOrigFlagPath(subregion);
                File flagFile = new File(flagPath);
                if (!flagFile.exists()) {
                    setText("Flag not fouind");
                    this.setGraphic(null);
                }
                else {
                    try {
                        URL flagURL = flagFile.toURI().toURL();
                        BufferedImage flagImage = ImageIO.read(flagURL);
                        double origHeight = flagImage.getHeight();
                        TableView subregionsTable = (TableView)app.getGUIModule().getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
                        double rowHeight = this.getTableRow().getHeight();
                        System.out.println("rowHeight: " + rowHeight);
                        double scaledHeight = 20.0;
                        double percentage = scaledHeight/origHeight;
                        BufferedImage scaledImage = files.getScaledImage(flagImage, percentage);
                        Image scaledImageFX = SwingFXUtils.toFXImage(scaledImage, null);
                        ImageView flagImageView = new ImageView();
                        flagImageView.setImage(scaledImageFX);
                        System.out.println("showing flag for " + subregion.getName());
                        DebugDisplay.appendDebugText("showing flag for " + subregion.getName());
                        this.setGraphic(flagImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        // MOVE THE HELP TOOLBAR TO THE FAR RIGHT
        HBox helpToolbar = gui.getHelpToolbar();
        rvmmBuilder.buildIconButton(RVMM_REPORT_ERROR_BUTTON, helpToolbar, CLASS_DJF_ICON_BUTTON, ENABLED);
        HBox topToolbar = gui.getTopToolbarPane();
        topToolbar.getChildren().remove(helpToolbar);
        topToolbar.getChildren().add(helpToolbar);
        

        // ARRANGE THE PANES IN THE RIGHT
        gui.addGUINode(RVMM_RIGHT_PANE, rightPane);
        rightPane.setTop(editSubregionsBox);
        rightPane.setCenter(subregionsTableView);

        // MAP ON LEFT, TABLE ON RIGHT
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().add(leftPane);
        splitPane.getItems().add(rightPane);
        splitPane.widthProperty().addListener(e -> {
            splitPane.setDividerPositions(.55);
        });

        workspace = new BorderPane();
        ((BorderPane) workspace).setCenter(splitPane);
    }

    public void loadTempBrochure() {
        File tempBrochureFile = new File(TEMP_BROCHURE_PATH);
        try {
            URL tempBrochureURL = tempBrochureFile.toURI().toURL();
            BufferedImage tempBrochureImage = ImageIO.read(tempBrochureURL);
            int origHeight = tempBrochureImage.getHeight();
            AppGUIModule gui = app.getGUIModule();
            double desiredHeight = ((Label) gui.getGUINode(RVMM_REGION_NAME_LABEL)).getHeight();
            double percentage = desiredHeight / origHeight;
            if (percentage <= 0.0) {
                // UI HAS NOT LOADED YET
                return;
            }
            RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
            BufferedImage scaledBrochureImage = files.getScaledImage(tempBrochureImage, percentage);
            Image scaledImageFX = SwingFXUtils.toFXImage(scaledBrochureImage, null);
            ((ImageView) gui.getGUINode(RVMM_BROCHURE_THUMBNAIL_IMAGE_VIEW)).setImage(scaledImageFX);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadBrochureThumbnail() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        if (data == null)
            return;
        if (!data.getRegionHasBrochure()) {
            loadTempBrochure();
        } else {
            String brochurePath = data.getOrigBrochurePath();
            File brochureFile = new File(brochurePath);
            try {
                URL brochureURL = brochureFile.toURI().toURL();
                BufferedImage origBrochureImage = ImageIO.read(brochureURL);
                int origHeight = origBrochureImage.getHeight();
                AppGUIModule gui = app.getGUIModule();
                double desiredHeight = ((Button) gui.getGUINode(RVMM_MOVE_UP_BUTTON)).getHeight();
                double percentage = desiredHeight / origHeight;
                RegioVincoFiles files = (RegioVincoFiles) app.getFileComponent();
                BufferedImage scaledBrochureImage = files.getScaledImage(origBrochureImage, percentage);
                Image scaledImageFX = SwingFXUtils.toFXImage(scaledBrochureImage, null);
                ((ImageView) gui.getGUINode(RVMM_BROCHURE_THUMBNAIL_IMAGE_VIEW)).setImage(scaledImageFX);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initControllers() {
        // WE'LL USE THIS TO GET CONTROLS IN ORDER TO ATTACH HANDLERS TO THEM
        AppGUIModule gui = app.getGUIModule();

        // NOW INIT THE HANDLERS, FIRST ALL NAVIGATION INTERACTIONS
        MapNavigationController mapNavigationController = new MapNavigationController((RegioVincoMapMakerApp) app);
        ((Button) gui.getGUINode(RVMM_RESET_NAVIGATION_BUTTON)).setOnAction(e -> {
            mapNavigationController.processResetNavigation();
        });
        ((Button) gui.getGUINode(RVMM_FIT_TO_SUBREGION_BUTTON)).setOnAction(e -> {
            mapNavigationController.processFitToPolygons();
        });
        ((CheckBox) gui.getGUINode(RVMM_TOGGLE_DEBUG_CHECKBOX)).setOnAction(e -> {
            CheckBox cB = (CheckBox) gui.getGUINode(RVMM_TOGGLE_DEBUG_CHECKBOX);
            if (cB.isSelected()) {
                // REMOVE THE TABLE
                BorderPane rightPane = (BorderPane) gui.getGUINode(RVMM_RIGHT_PANE);
                rightPane.setCenter(null);
                TextArea debugTextArea = (TextArea) gui.getGUINode(RVMM_DEBUG_TEXT_AREA);
                rightPane.setCenter(debugTextArea);
            } else {
                // REMOVE THE TEXT AREA
                BorderPane rightPane = (BorderPane) gui.getGUINode(RVMM_RIGHT_PANE);
                rightPane.setCenter(null);
                TableView subregionsTableView = (TableView) gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
                rightPane.setCenter(subregionsTableView);
            }
        });
        ((Button) gui.getGUINode(RVMM_SHUFFLE_MAP_COLORS_BUTTON)).setOnAction(e -> {
            mapEditController.processRandomizeGreyscaleColors();
        });
        ((Button) gui.getGUINode(EXPORT_BUTTON)).setOnAction(e -> {
            mapEditController.processExportMap();
        });
        ((Button) gui.getGUINode(RVMM_EDIT_LEADERS_BUTTON)).setOnAction(e -> {
            mapEditController.processEditLeaders();
        });
        ((Button) gui.getGUINode(RVMM_EDIT_PARENT_REGION_BUTTON)).setOnAction(e -> {
            mapEditController.processEditMapSettings();
        });
        ((Button) gui.getGUINode(RVMM_EDIT_BROCHURE_BUTTON)).setOnAction(e -> {
            mapEditController.processEditBrochure();
        });
        ((Button) gui.getGUINode(RVMM_REPORT_ERROR_BUTTON)).setOnAction(e -> {
            mapEditController.processReportError();
        });
        ((Pane) gui.getGUINode(RVMM_MAP_PANE)).setOnMousePressed(e -> {
            boolean isLeftButton = e.getButton() == MouseButton.PRIMARY;
            if (isLeftButton)
                mapNavigationController.processMapMousePress(e.getX(), e.getY());
        });
        ((Pane) gui.getGUINode(RVMM_MAP_PANE)).setOnMouseDragged(e -> {
            boolean isLeftButton = e.getButton() == MouseButton.PRIMARY;
            if (isLeftButton)
                mapNavigationController.processMapMouseDragged(e.getX(), e.getY());
        });
        ((Pane) gui.getGUINode(RVMM_MAP_PANE)).setOnMouseReleased(e -> {
            boolean isLeftButton = e.getButton() == MouseButton.PRIMARY;
            if (isLeftButton)
                mapNavigationController.processMapMouseRelease(e.getX(), e.getY());
        });
        ((Pane) gui.getGUINode(RVMM_MAP_PANE)).setOnScroll(e -> {
            mapNavigationController.processMapMouseScroll(e.getDeltaY() > 0, e.getX(), e.getY());
        });
        ((Pane) gui.getGUINode(RVMM_FRAME_LAYER_PANE)).setFocusTraversable(false);
        ((Pane) gui.getGUINode(RVMM_FRAME_LAYER_PANE)).setPickOnBounds(false);

        // SETUP HANDLERS FOR ALL OTHER WORKSPACE CONTROLS
        mapEditController = new MapEditController((RegioVincoMapMakerApp) app);
        ((Button) gui.getGUINode(RVMM_DELETE_MAP_BUTTON)).setOnAction(e -> {
            mapEditController.processDeleteMap();
        });        
        ((Button) gui.getGUINode(RVMM_MOVE_UP_BUTTON)).setOnAction(e -> {
            mapEditController.processMoveSubregionUp();
        });
        ((Button) gui.getGUINode(RVMM_MOVE_DOWN_BUTTON)).setOnAction(e -> {
            mapEditController.processMoveSubregionDown();
        });
        ((Label) gui.getGUINode(RVMM_REGION_NAME_LABEL)).setOnMouseClicked(e->{
            nameDialog.showDialog();
        });
        ((ComboBox) gui.getGUINode(RVMM_SUBREGION_TYPE_COMBO_BOX)).setOnAction(e -> {
            mapEditController.processChangeSubregionType();
        });
        ((Button) gui.getGUINode(RVMM_ADD_SUBREGIONS_BUTTON)).setOnAction(e -> {
            mapEditController.processImportShapefile();
        });
        ((Button) gui.getGUINode(RVMM_REMOVE_SUBREGION_BUTTON)).setOnAction(e -> {
            mapEditController.processRemoveSubregion();
        });
        ((TableView) gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW)).widthProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TableView<SubregionPrototype> subregionsTableView = ((TableView) gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW));
                for (int i = 0; i < subregionsTableView.getColumns().size(); i++) {
                    TableColumn tc = subregionsTableView.getColumns().get(i);
                    tc.setPrefWidth(subregionsTableView.getWidth() / NUM_SUBREGION_COLUMNS);
                }
            }
        });
        ((TableView) gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW)).setOnMouseClicked(e -> {
            RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
            if (!data.isLoading()) {
                TableView<SubregionPrototype> subregionsTableView = ((TableView) gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW));
                if (e.getButton() == MouseButton.PRIMARY) {
                    SubregionPrototype subregion = subregionsTableView.getSelectionModel().getSelectedItem();
                    mapEditController.processSelectSubregion(subregion);
                    app.getFoolproofModule().updateAll();
                    if (e.getClickCount() == 2) {
                        mapEditController.processEditSubregion(subregion);
                    }
                }
            }
        });
        ((ImageView) gui.getGUINode(RVMM_BROCHURE_THUMBNAIL_IMAGE_VIEW)).setOnMouseClicked(e -> {
            mapEditController.processEditBrochure();
        });
/*        ((Pane) gui.getGUINode(RVMM_MAP_PANE)).setOnMouseMoved(e -> {
            RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
            MapNavigator mapNavigator = data.getMapNavigator();
        });
*/
    }

    public void initFoolproofDesign() {
        AppGUIModule gui = app.getGUIModule();
        AppFoolproofModule foolproofSettings = app.getFoolproofModule();
        foolproofSettings.registerModeSettings(RVMM_FOOLPROOF_SETTINGS, 
                new RVMMFoolproofDesign((RegioVincoMapMakerApp)app)
        );
        FoolproofDesign fileFoolproofDesign = app.getFoolproofModule().getFoolproofDesign(APP_FILE_FOOLPROOF_SETTINGS);
        FoolproofDesign rvmmFoolproofDesign = app.getFoolproofModule().getFoolproofDesign(RVMM_FOOLPROOF_SETTINGS);
        app.getFoolproofModule().registerModeSettings(APP_FILE_FOOLPROOF_SETTINGS, new FoolproofDesign() {
            @Override
            public void updateControls() {
                fileFoolproofDesign.updateControls();
                rvmmFoolproofDesign.updateControls();
            }
        });
        System.out.println("What is registered?");
    }

    @Override
    public void showNewDialog() {
        newMapDialog.showDialog();
        if (newMapDialog.hasGoodData()) {
            String path = newMapDialog.getParentRegionPath();
            String regionName = newMapDialog.getRegionName();
            String fileExtension = WORK_FILE_EXT;
            
            // RESET THE APP
            app.getFileModule().newWork(path, regionName, fileExtension);

            // INIT IT WITH NEW DATA
            RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
            data.setRegionName(regionName);

            try {
                app.getFileComponent().importData(app.getDataComponent(), newMapDialog.getShapefilePath());
            } catch (IOException ioe) {
                System.out.println("ERROR IMPORTING MAP");
            }
        }
    }
    public void loadRegionsTreeView(TreeView treeView) {
        // The World IS ALWAYS THE ROOT NODE AND SO MUST BE THERE
        File theWorld = new File(WORK_PATH + "The World" + WORK_FILE_EXT);
        if (!theWorld.exists()) {
            // WHERE IS THE WORLD?
            DebugDisplay.appendDebugText("The World" + WORK_FILE_EXT + " is missing");
            return;
        }
        TreeItem<String> root = new TreeItem("The World");
        treeView.setRoot(root);
        
        // NOW BEGIN THE RECURSIVE TASK OF GOING THROUGH ALL THE SUBDIRECTORIES
        // TO FIND ALL THE OTHER REGION WORK FILES AND POPULATE THE TREE
        File rootDir = new File(WORK_PATH + "The World");
        populateTree(rootDir, root);
    }    
    private void populateTree(File dir, TreeItem<String> parent) {
        // GET ALL THE WORK FILES AND ADD THEM TO THE TREE
        File[] files = dir.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(WORK_FILE_EXT)) {
                String regionName = fileName.substring(0, fileName.indexOf(WORK_FILE_EXT));
                TreeItem<String> node = new TreeItem(regionName);
                parent.getChildren().add(node);
            }
        }

        // NOW PROPOGATE populateTree THROUGH THE SUBDIRECTORIES
        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                TreeItem<String> node = null;
                for (TreeItem<String> treeItem : parent.getChildren()) {
                    String nodeValue = treeItem.getValue();
                    if (nodeValue.equals(dirName)) {
                        node = treeItem;
                    }
                }
                if (node != null)
                    populateTree(file, node);
            }
        }        
    } 
}