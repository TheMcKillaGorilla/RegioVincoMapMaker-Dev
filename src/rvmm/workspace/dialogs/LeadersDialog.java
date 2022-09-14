package rvmm.workspace.dialogs;

import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import rvleaders.RVLeaders;
import rvleaders.RVLeadersWikiPageType;
import rvleaders.SubregionData;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import rvmm.data.LeaderPrototype;
import rvmm.data.MapPropertyType;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.SubregionPrototype;
import rvmm.transactions.ChangeLeaders_Transaction;
import rvmm.transactions.ChangeMapProperties_Transaction;
import rvmm.transactions.Complex_Transaction;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import static rvmm.workspace.style.RVMMStyle.*;

public class LeadersDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // UI COMPONENTS FOR THIS DIALOG
    BorderPane dialogPane;
    HBox topPane;
    Label headingLabel;
    BorderPane centerPane;
    HBox controlsPane;
    VBox leadersTypePane;
    Label leadersTypePromptLabel;
    ComboBox leadersTypeComboBox;
    VBox leadersWikiPageTypePane;
    Label leadersWikiPageTypePromptLabel;
    ComboBox leadersWikiPageTypeComboBox;
    VBox leadersWikiPageURLPane;
    Label leadersWikiPageURLPromptLabel;
    TextField leadersWikiPageURLTextField;
    VBox findLeadersPane;
    Button findLeadersButton;
    TableView<LeaderPrototype> leadersTableView;
    TableColumn<LeaderPrototype, String> subregionNameColumn;
    TableColumn<LeaderPrototype, String> currentLeaderColumn;
    TableColumn<LeaderPrototype, String> foundLeaderColumn;
    TableColumn<LeaderPrototype, Boolean> updateLeaderColumn;
    final int NUM_LEADER_COLUMNS = 4;
    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;

    boolean editing;

    public static LeadersDialog singleton = null;

    public static LeadersDialog getLeadersDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getLeadersDialog");
        if (singleton == null) {
            singleton = new LeadersDialog(initApp);
        }
        return singleton;
    }

    private LeadersDialog(RegioVincoMapMakerApp initApp) {
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
        topPane = rvmmBuilder.buildHBox(RVMM_LEADERS_DIALOG_TOP_PANE, null, CLASS_RVMM_DIALOG_TOP_PANE, ENABLED);
        topPane.setAlignment(Pos.CENTER);
        headingLabel = rvmmBuilder.buildLabel(RVMM_LEADERS_DIALOG_HEADING_LABEL, topPane, CLASS_RVMM_BIG_HEADER, ENABLED);
        centerPane = rvmmBuilder.buildBorderPane(RVMM_LEADERS_DIALOG_CENTER_PANE, null, CLASS_RVMM_PANE, ENABLED);
        controlsPane = rvmmBuilder.buildHBox(RVMM_LEADERS_DIALOG_CONTROLS_PANE, null, CLASS_RVMM_PANE, ENABLED);
        leadersTypePane = rvmmBuilder.buildVBox(RVMM_LEADERS_DIALOG_LEADERS_TYPE_PANE, controlsPane, CLASS_RVMM_PANE, ENABLED);
        leadersTypePromptLabel = rvmmBuilder.buildLabel(RVMM_LEADERS_DIALOG_LEADERS_TYPE_PROMPT_LABEL, leadersTypePane, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        leadersTypeComboBox = rvmmBuilder.buildComboBox(RVMM_LEADERS_DIALOG_LEADERS_TYPE_COMBO_BOX, RVMM_LEADER_TYPES, RVMM_DEFAULT_LEADER_TYPE, leadersTypePane, CLASS_RVMM_COMBO_BOX, ENABLED);
        leadersWikiPageTypePane = rvmmBuilder.buildVBox(RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_TYPE_PANE, controlsPane, CLASS_RVMM_PANE, ENABLED);
        leadersWikiPageTypePromptLabel = rvmmBuilder.buildLabel(RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_TYPE_PROMPT_LABEL, leadersWikiPageTypePane, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        leadersWikiPageTypeComboBox = rvmmBuilder.buildComboBox(RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_TYPE_COMBO_BOX, RVMM_LEADERS_WIKI_PAGE_TYPES, RVMM_DEFAULT_LEADERS_WIKI_PAGE_TYPE, leadersWikiPageTypePane, CLASS_RVMM_COMBO_BOX, ENABLED);
        leadersWikiPageURLPane = rvmmBuilder.buildVBox(RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_URL_PANE, controlsPane, CLASS_RVMM_PANE, ENABLED);
        leadersWikiPageURLPromptLabel = rvmmBuilder.buildLabel(RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_URL_PROMPT_LABEL, leadersWikiPageURLPane, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        leadersWikiPageURLTextField = rvmmBuilder.buildTextField(RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_URL_TEXT_FIELD, leadersWikiPageURLPane, CLASS_RVMM_TEXT_FIELD, ENABLED);
        leadersWikiPageURLTextField.setPrefColumnCount(50);
        findLeadersPane = rvmmBuilder.buildVBox(RVMM_LEADERS_DIALOG_FIND_LEADERS_PANE, controlsPane, CLASS_RVMM_PANE, ENABLED);
        findLeadersPane.setAlignment(Pos.CENTER_RIGHT);
        findLeadersButton = rvmmBuilder.buildTextButton(RVMM_LEADERS_DIALOG_FIND_LEADERS_BUTTON, findLeadersPane, CLASS_RVMM_BUTTON, ENABLED);
        leadersTableView = rvmmBuilder.buildTableView(RVMM_LEADERS_DIALOG_LEADERS_TABLE_VIEW, null, CLASS_RVMM_TABLE_VIEW, ENABLED);
        subregionNameColumn = rvmmBuilder.buildTableColumn(RVMM_LEADERS_DIALOG_SUBREGION_NAME_TABLE_COLUMN, leadersTableView, CLASS_RVMM_TABLE_COLUMN);
        subregionNameColumn.setCellValueFactory(new PropertyValueFactory<LeaderPrototype, String>(LeaderPrototype.SUBREGION_NAME));
        currentLeaderColumn = rvmmBuilder.buildTableColumn(RVMM_LEADERS_DIALOG_CURRENT_LEADER_TABLE_COLUMN, leadersTableView, CLASS_RVMM_TABLE_COLUMN);
        currentLeaderColumn.setCellValueFactory(new PropertyValueFactory<LeaderPrototype, String>(LeaderPrototype.CURRENT_LEADER));
        foundLeaderColumn = rvmmBuilder.buildTableColumn(RVMM_LEADERS_DIALOG_FOUND_LEADER_TABLE_COLUMN, leadersTableView, CLASS_RVMM_TABLE_COLUMN);
        foundLeaderColumn.setCellValueFactory(new PropertyValueFactory<LeaderPrototype, String>(LeaderPrototype.FOUND_LEADER));
        foundLeaderColumn.setCellFactory(new Callback<TableColumn<LeaderPrototype, String>, TableCell<LeaderPrototype, String>>() {
            public TableCell<LeaderPrototype, String> call(TableColumn param) {
                return new TableCell<LeaderPrototype, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        TableRow row = getTableRow();
                        if (row != null) {
                            LeaderPrototype leader = (LeaderPrototype) getTableRow().getItem();
                            if (leader != null) {
                                if (item.equals(leader.getCurrentLeader())) {
                                    this.setTextFill(Color.BLACK);
                                }
                                else {
                                    this.setTextFill(Color.RED);
                                }
                                setText(item);
                            }
                        }
                    }
                };
            }
        });
        
        // THIS LETS US USE THE CHECKBOX
        this.leadersTableView.setEditable(true);
        updateLeaderColumn = rvmmBuilder.buildTableColumn(RVMM_LEADERS_DIALOG_UPDATE_LEADER_TABLE_COLUMN, leadersTableView, CLASS_RVMM_TABLE_COLUMN);
        updateLeaderColumn.setCellFactory(column -> {
            CheckBoxTableCell cell = new CheckBoxTableCell<>();
            return cell;
        });
        updateLeaderColumn.setCellValueFactory(cellData -> {
            LeaderPrototype leader = cellData.getValue();
            BooleanProperty property = leader.updateLeaderProperty();

            property.addListener((observable, oldValue, newValue) -> {
                leader.setUpdateLeader(newValue);
                updateControls();
            });

            return property;
        });
        centerPane.setTop(controlsPane);
        centerPane.setCenter(this.leadersTableView);
        
        AppGUIModule gui = app.getGUIModule();
        ((TableView) gui.getGUINode(RVMM_LEADERS_DIALOG_LEADERS_TABLE_VIEW)).widthProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TableView<LeaderPrototype> leaderTableView = ((TableView) gui.getGUINode(RVMM_LEADERS_DIALOG_LEADERS_TABLE_VIEW));
                for (int i = 0; i < leaderTableView.getColumns().size(); i++) {
                    TableColumn tc = leaderTableView.getColumns().get(i);
                    tc.setPrefWidth(leaderTableView.getWidth() / NUM_LEADER_COLUMNS);
                }
            }
        });
        bottomPane = rvmmBuilder.buildHBox(RVMM_LEADERS_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton = rvmmBuilder.buildTextButton(RVMM_LEADERS_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_LEADERS_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        
        // NOW PUT EVERYTHING IN THE DIALOG
        dialogPane = new BorderPane();
        dialogPane.setTop(topPane);
        dialogPane.setCenter(centerPane);
        dialogPane.setBottom(bottomPane);

        // SETUP THE HANDLERS
        this.leadersTypeComboBox.setOnAction(e->{
            updateControls();
        });
        this.leadersWikiPageTypeComboBox.setOnAction(e->{
            updateControls();
        });
        this.leadersWikiPageURLTextField.textProperty().addListener(e->{
            updateControls();
        });
        findLeadersButton.setOnAction(e -> {
            processFindLeaders();
        });
        confirmButton.setOnAction(e -> {
            confirmEdits();
        });
        cancelButton.setOnAction(e -> {
            cancelEdits();
        });
    }
    private void updateControls() {
        // THIS IS THE TABLE DATA
        ObservableList<LeaderPrototype> leaders = this.leadersTableView.getItems();

        // IF NO FIND SETTINGS ARE PROVIDED THEN DISABLE THE FIND BUTTON
        String leaderType = (String)this.leadersTypeComboBox.getSelectionModel().getSelectedItem();
        String wikiPageType = (String)this.leadersWikiPageTypeComboBox.getSelectionModel().getSelectedItem();
        String wikiPageURL = this.leadersWikiPageURLTextField.getText();
        
        // WE MUST HAVE NON-EMPTY VALUES FOR ALL 3 TO EVEN ATTEMPT A FIND
        boolean enableFindLeadersButton = 
                    isValidSelection(leaderType)
                &&  isValidSelection(wikiPageType)
                && (wikiPageURL.trim().length() > 0)
                && (leaders.size() > 0);
        this.findLeadersButton.setDisable(!enableFindLeadersButton);
    }
    private boolean isValidSelection(String selection) {
        return (selection != null) && (!selection.equals("")) && (!selection.equals("-"));
    }
    private void processFindLeaders() {
        String wikiPageURL = this.leadersWikiPageURLTextField.getText();
        ArrayList<SubregionData> subregionData = makeSubregionData();
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)this.app.getDataComponent();
        String regionName = data.getRegionName();
        String wikiPageTypeText = this.leadersWikiPageTypeComboBox.getSelectionModel().getSelectedItem().toString();
        RVLeadersWikiPageType type = RVLeadersWikiPageType.valueOf(wikiPageTypeText);
        HashMap<String, String> newLeaders = RVLeaders.getAllLeaders(
                type, wikiPageURL, subregionData, regionName);
        ObservableList<LeaderPrototype> leaders = this.leadersTableView.getItems();
        for (String subregionName : newLeaders.keySet()) {
            String newLeader = newLeaders.get(subregionName);
            for (LeaderPrototype leader : leaders) {
                if (leader.getSubregionName().equals(subregionName)) {
                    leader.setFoundLeader(newLeader);
                }
            }
        }
        for (LeaderPrototype leader : leaders) {
            if (leader.getCurrentLeader().equals(leader.getFoundLeader())) {
                leader.setUpdateLeader(false);
            }
            else if (leader.getFoundLeader().trim().length() == 0) {
                leader.setUpdateLeader(false);
            }
            else {
                leader.setUpdateLeader(true);
            }
        }
        updateControls();
    }
    public ArrayList<SubregionData> makeSubregionData() {
        ArrayList<SubregionData> subregionData = new ArrayList();
        ObservableList<LeaderPrototype> leaders = this.leadersTableView.getItems();
        for (LeaderPrototype leader : leaders) {
            SubregionData subregionToAdd = new SubregionData(leader.getSubregionName(), false);
            subregionData.add(subregionToAdd);
        }
        return subregionData;
    }
    private void processUpdateLeaders() {
        ObservableList<LeaderPrototype> leaders = this.leadersTableView.getItems();
        HashMap<String, String> newLeaders = new HashMap();
        for (LeaderPrototype leader : leaders) {
            if (leader.getUpdateLeader() && (!leader.getCurrentLeader().equals(leader.getFoundLeader()))) {
                newLeaders.put(leader.getSubregionName(), leader.getFoundLeader());
                leader.setCurrentLeader(leader.getFoundLeader());
            }
        }
        updateControls();
    }
    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        String titleText = "RVMM - " + data.getRegionName() + " Leaders";
        setTitle(titleText);

        // HAVE THE CARET START IN THE NAME TEXT FIELD
        String leadersType = data.getLeadersType();
        this.leadersTypeComboBox.getSelectionModel().select(leadersType);
        String leadersWikiPageType = data.getLeadersWikiPageType();
        this.leadersWikiPageTypeComboBox.getSelectionModel().select(leadersWikiPageType);
        String leadersWikiPageURL = data.getLeadersWikiPageURL();
        this.leadersWikiPageURLTextField.setText(leadersWikiPageURL);

        loadCurrentLeaders();
        
        updateControls();
        
        // AND OPEN THE DIALOG
        showAndWait();
    }
    
    private void loadCurrentLeaders() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();
        ObservableList<LeaderPrototype> leaders = this.leadersTableView.getItems();
        leaders.clear();
        ObservableList<SubregionPrototype> subregions = data.getSubregions();
        for (SubregionPrototype subregion : subregions) {
            String subregionName = subregion.getName();
            String currentLeader = subregion.getLeader();
            LeaderPrototype leader = new LeaderPrototype(
                                            subregionName,
                                            currentLeader,
                                            "",
                                            false);
            leaders.add(leader);
        }
    }
    
    public void confirmEdits() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData)app.getDataComponent();

        HashMap<MapPropertyType, String> oldProperties = new HashMap();
        oldProperties.put(MapPropertyType.LEADERS_TYPE, data.getMapProperty(MapPropertyType.LEADERS_TYPE));
        oldProperties.put(MapPropertyType.LEADERS_WIKI_PAGE_TYPE, data.getMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_TYPE));
        oldProperties.put(MapPropertyType.LEADERS_WIKI_PAGE_URL, data.getMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_URL));

        // THE FIRST TRANSACTION
        String leadersType = this.leadersTypeComboBox.getValue().toString();
        String leadersWikiPageType = this.leadersWikiPageTypeComboBox.getValue().toString();
        String leadersWikiPageURL = this.leadersWikiPageURLTextField.getText();
        HashMap<MapPropertyType, String> newProperties = new HashMap();
        newProperties.put(MapPropertyType.LEADERS_TYPE, leadersType);
        newProperties.put(MapPropertyType.LEADERS_WIKI_PAGE_TYPE, leadersWikiPageType);
        newProperties.put(MapPropertyType.LEADERS_WIKI_PAGE_URL, leadersWikiPageURL);
        ChangeMapProperties_Transaction transaction1 = new ChangeMapProperties_Transaction(data, oldProperties, newProperties);

        // AND THE SECOND TRANSACTION
        HashMap<String, String> oldLeaders = new HashMap();
        HashMap<String, String> newLeaders = new HashMap();
        // GO THROUGH THE CHECKED ITEMS IN THE TABLE
        ObservableList<LeaderPrototype> leaders = this.leadersTableView.getItems();
        for (LeaderPrototype leader : leaders) {
            if (leader.getUpdateLeader()) {
                oldLeaders.put(leader.getSubregionName(), leader.getCurrentLeader());
                newLeaders.put(leader.getSubregionName(), leader.getFoundLeader());
            }
        }
        ChangeLeaders_Transaction transaction2 = new ChangeLeaders_Transaction(data, oldLeaders, newLeaders);

        // AND NOW WE'LL COMBINE THEM INTO ONE
        Complex_Transaction transaction = new Complex_Transaction(transaction1, transaction2);
        app.processTransaction(transaction);
        
        // CLOSE THE DIALOG
        this.hide();
    }

    public void cancelEdits() {
        // AND CLOSE THE DIALOG
        hide();
    }
}
