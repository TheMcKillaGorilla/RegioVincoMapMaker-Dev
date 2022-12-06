package rvmm.workspace.dialogs;

import com.sun.javafx.webkit.WebConsoleListener;
import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.*;
import djf.ui.AppNodesBuilder;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import static rvmm.data.RVMM_Constants.STAMP_FILE_PATH;
import static rvmm.data.RVMM_Constants.STAMP_WEB_PAGE;
import rvmm.data.RegioVincoMapMakerData;
import rvmm.data.VisaProperty;
import rvmm.files.RegioVincoFiles;
import rvmm.transactions.UpdateVisa_Transaction;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import static rvmm.workspace.style.RVMMStyle.*;
import netscape.javascript.JSObject;

class JavaBridge {

    public void log(String text) {
        System.out.println(text);
    }
}

public class VisaDialog extends Stage {

    // THE MAP EDITOR APPLICATION
    RegioVincoMapMakerApp app;

    // UI COMPONENTS FOR THIS DIALOG
    BorderPane dialogPane;

    HBox topPane;
    Label headingLabel;

    SplitPane centerPane;
    WebView visaWebView;
    GridPane visaGridPane;
    Label stampTypeLabel;
    ComboBox<String> stampTypeComboBox;
    Label dateFormatLabel;
    ComboBox<String> dateFormatComboBox;
    Label stampColorLabel;
    ColorPicker stampColorPicker;
    Label stampLengthLabel;
    Slider stampLengthSlider;
    Label stampLengthValueLabel;
    Label nameYLabel;
    Slider nameYSlider;
    Label nameYValueLabel;
    Label dateYLabel;
    Slider dateYSlider;
    Label dateYValueLabel;
    Label fontFamilyLabel;
    ComboBox<String> fontFamilyComboBox;
    Label nameFontSizeLabel;
    Slider nameFontSizeSlider;
    Label nameFontSizeValueLabel;
    Label dateFontSizeLabel;
    Slider dateFontSizeSlider;
    Label dateFontSizeValueLabel;
    Label borderThicknessLabel;
    Slider borderThicknessSlider;
    Label borderThicknessValueLabel;

    HBox bottomPane;
    Button confirmButton;
    Button cancelButton;

    boolean editing;

    public static VisaDialog singleton = null;

    public static VisaDialog getVisaDialog(RegioVincoMapMakerApp initApp) {
        appendDebugText("getLeadersDialog");
        if (singleton == null) {
            singleton = new VisaDialog(initApp);
        }
        return singleton;
    }

    private VisaDialog(RegioVincoMapMakerApp initApp) {
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

    private void drawStamp() {
        String svgPaneId = "visa-svg-pane";
        String stampType = stampTypeComboBox.getValue();
        String regionName = ((RegioVincoMapMakerData) app.getDataComponent()).getRegionName();
        int nameFontSize = (int) Math.round(nameFontSizeSlider.getValue());
        int nameY = (int) Math.round(nameYSlider.getValue());
        String dateFormat = dateFormatComboBox.getValue();
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
        String date = fmt.format(new GregorianCalendar().getTime());
        int dateFontSize = (int) Math.round(dateFontSizeSlider.getValue());
        int dateY = (int) Math.round(dateYSlider.getValue());
        String stampColor = "#" + Integer.toHexString(stampColorPicker.getValue().hashCode());
        int borderThickness = (int) Math.round(borderThicknessSlider.getValue());
        int stampLength = (int) Math.round(stampLengthSlider.getValue());
        String fontFamily = fontFamilyComboBox.getValue();

        String script = "generateStamp("
                + "'" + svgPaneId + "', "
                + "'" + stampType + "', "
                + "'" + regionName + "', "
                + "'" + stampLength + "', "
                + "'" + nameFontSize + "', "
                + "'" + nameY + "', "
                + "'" + date + "', "
                + "'" + dateFormat + "', "
                + "'" + dateFontSize + "', "
                + "'" + dateY + "', "
                + "'" + fontFamily + "', "
                + "'" + stampColor + "', "
                + "'" + borderThickness + "', "
                + "'0', '0', '0')"; // TRANSLATION AND ROTATION
        WebEngine webEngine = visaWebView.getEngine();
        webEngine.executeScript(script);
        
        fillValueLabels();
    }

    class JavaBridge {
        public void log(String text) {
            System.out.println(text);
        }
    }

    private final JavaBridge bridge = new JavaBridge();

    private void initDialogUI() {
        AppNodesBuilder rvmmBuilder = app.getGUIModule().getNodesBuilder();
        topPane = rvmmBuilder.buildHBox(RVMM_VISA_DIALOG_TOP_PANE, null, CLASS_RVMM_DIALOG_TOP_PANE, ENABLED);
        topPane.setAlignment(Pos.CENTER);
        headingLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_HEADING_LABEL, topPane, CLASS_RVMM_BIG_HEADER, ENABLED);

        centerPane = rvmmBuilder.buildSplitPane(RVMM_VISA_DIALOG_CENTER_PANE, null, CLASS_RVMM_PANE, ENABLED);
        visaWebView = rvmmBuilder.buildWebView(RVMM_VISA_DIALOG_STAMP_WEB_VIEW, null, CLASS_RVMM_PANE, ENABLED);
        visaGridPane = rvmmBuilder.buildGridPane(RVMM_VISA_DIALOG_GRID_PANE, null, CLASS_RVMM_PANE, ENABLED);
        centerPane.getItems().add(visaWebView);
        centerPane.getItems().add(visaGridPane);
        centerPane.setDividerPositions(.7);

        // REDIRECT THE CONSOLE
        WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> {
            System.out.println(message + "[at " + lineNumber + "]");
        });

        // LOAD THE STAMP RENDERING PAGE        
        WebEngine webEngine = visaWebView.getEngine();
        try {
            URL pageURL = new File(STAMP_WEB_PAGE).toURI().toURL();
            String pagePath = pageURL.toExternalForm();
            webEngine.load(pagePath);
            //webEngine.executeScript("testTextExample()");
        } catch (MalformedURLException murle) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Error Loading " + STAMP_WEB_PAGE);
            alert.showAndWait();
        }
        stampTypeLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_STAMP_TYPE_LABEL, visaGridPane, 0, 0, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        stampTypeComboBox = rvmmBuilder.buildComboBox(RVMM_VISA_DIALOG_STAMP_TYPE_COMBO_BOX, visaGridPane, 1, 0, 1, 1, CLASS_RVMM_COMBO_BOX, ENABLED, RVMM_VISA_DIALOG_STAMP_TYPE_OPTIONS, RVMM_VISA_DIALOG_STAMP_TYPE_DEFAULT);
        dateFormatLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_DATE_FORMAT_LABEL, visaGridPane, 0, 1, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        dateFormatComboBox = rvmmBuilder.buildComboBox(RVMM_VISA_DIALOG_DATE_FORMAT_COMBO_BOX, visaGridPane, 1, 1, 1, 1, CLASS_RVMM_COMBO_BOX, ENABLED, RVMM_VISA_DIALOG_DATE_FORMAT_OPTIONS, RVMM_VISA_DIALOG_DATE_FORMAT_DEFAULT);
        stampColorLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_STAMP_COLOR_LABEL, visaGridPane, 0, 2, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        stampColorPicker = rvmmBuilder.buildColorPicker(RVMM_VISA_DIALOG_STAMP_COLOR_PICKER, visaGridPane, 1, 2, 1, 1, EMPTY_TEXT, ENABLED);
        stampLengthLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_STAMP_LENGTH_LABEL, visaGridPane, 0, 3, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        stampLengthSlider = rvmmBuilder.buildSlider(RVMM_VISA_DIALOG_STAMP_LENGTH_SLIDER, visaGridPane, 1, 3, 1, 1, CLASS_RVMM_SLIDER, ENABLED, 100, 1000);
        stampLengthValueLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_STAMP_LENGTH_VALUE_LABEL, visaGridPane, 2, 3, 1, 1, CLASS_RVMM_DIALOG_VALUE_LABEL, ENABLED);
        nameYLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_NAME_Y_LABEL, visaGridPane, 0, 4, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        nameYSlider = rvmmBuilder.buildSlider(RVMM_VISA_DIALOG_NAME_Y_SLIDER, visaGridPane, 1, 4, 1, 1, CLASS_RVMM_SLIDER, ENABLED, -250, 250);
        nameYValueLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_NAME_Y_VALUE_LABEL, visaGridPane, 2, 4, 1, 1, CLASS_RVMM_DIALOG_VALUE_LABEL, ENABLED);
        dateYLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_DATE_Y_LABEL, visaGridPane, 0, 5, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        dateYSlider = rvmmBuilder.buildSlider(RVMM_VISA_DIALOG_DATE_Y_SLIDER, visaGridPane, 1, 5, 1, 1, CLASS_RVMM_SLIDER, ENABLED, -250, 250);
        dateYValueLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_DATE_Y_VALUE_LABEL, visaGridPane, 2, 5, 1, 1, CLASS_RVMM_DIALOG_VALUE_LABEL, ENABLED);
        fontFamilyLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_FONT_FAMILY_LABEL, visaGridPane, 0, 6, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        fontFamilyComboBox = rvmmBuilder.buildComboBox(RVMM_VISA_DIALOG_FONT_FAMILY_COMBO_BOX, visaGridPane, 1, 6, 1, 1, CLASS_RVMM_COMBO_BOX, ENABLED, RVMM_VISA_DIALOG_FONT_FAMILY_OPTIONS, RVMM_VISA_DIALOG_FONT_FAMILY_DEFAULT);
        nameFontSizeLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_NAME_FONT_SIZE_LABEL, visaGridPane, 0, 7, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        nameFontSizeSlider = rvmmBuilder.buildSlider(RVMM_VISA_DIALOG_NAME_FONT_SIZE_SLIDER, visaGridPane, 1, 7, 1, 1, CLASS_RVMM_FONT_SLIDER, ENABLED, 5, 40);
        nameFontSizeValueLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_NAME_FONT_SIZE_VALUE_LABEL, visaGridPane, 2, 7, 1, 1, CLASS_RVMM_DIALOG_VALUE_LABEL, ENABLED);
        dateFontSizeLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_DATE_FONT_SIZE_LABEL, visaGridPane, 0, 8, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        dateFontSizeSlider = rvmmBuilder.buildSlider(RVMM_VISA_DIALOG_DATE_FONT_SIZE_SLIDER, visaGridPane, 1, 8, 1, 1, CLASS_RVMM_FONT_SLIDER, ENABLED, 5, 40);
        dateFontSizeValueLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_DATE_FONT_SIZE_VALUE_LABEL, visaGridPane, 2, 8, 1, 1, CLASS_RVMM_DIALOG_VALUE_LABEL, ENABLED);
        borderThicknessLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_BORDER_THICKNESS_LABEL, visaGridPane, 0, 9, 1, 1, CLASS_RVMM_DIALOG_PROMPT, ENABLED);
        borderThicknessSlider = rvmmBuilder.buildSlider(RVMM_VISA_DIALOG_BORDER_THICKNESS_SLIDER, visaGridPane, 1, 9, 1, 1, CLASS_RVMM_FONT_SLIDER, ENABLED, 1, 5);
        borderThicknessValueLabel = rvmmBuilder.buildLabel(RVMM_VISA_DIALOG_BORDER_THICKNESS_VALUE_LABEL, visaGridPane, 2, 9, 1, 1, CLASS_RVMM_DIALOG_VALUE_LABEL, ENABLED);
        bottomPane = rvmmBuilder.buildHBox(RVMM_VISA_DIALOG_BOTTOM_PANE, null, CLASS_RVMM_DIALOG_BOTTOM_PANE, ENABLED);
        bottomPane.setAlignment(Pos.CENTER);
        confirmButton = rvmmBuilder.buildTextButton(RVMM_VISA_DIALOG_CONFIRM_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);
        cancelButton = rvmmBuilder.buildTextButton(RVMM_VISA_DIALOG_CANCEL_BUTTON, bottomPane, CLASS_RVMM_BUTTON, ENABLED);

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

        registerComboBoxListener(stampTypeComboBox);
        registerComboBoxListener(dateFormatComboBox);
        registerColorPicker(stampColorPicker);
        registerSliderListener(stampLengthSlider);
        registerSliderListener(nameYSlider);
        registerSliderListener(dateYSlider);
        registerComboBoxListener(fontFamilyComboBox);
        registerSliderListener(nameFontSizeSlider);
        registerSliderListener(dateFontSizeSlider);
        registerSliderListener(borderThicknessSlider);

        // MAKE ALL THE ROWS THE SAME HEIGHT
        int rowCount = 12;
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(100d / rowCount);
        for (int i = 0; i < rowCount; i++) {
            visaGridPane.getRowConstraints().add(rc);
        }
        int colCount = 3;
        ColumnConstraints cc = new ColumnConstraints();
        cc.setFillWidth(true);
        for (int i = 0; i < colCount; i++) {
            visaGridPane.getColumnConstraints().add(cc);
        }
    }
    private void fillValueLabels() {
        fillValueLabel(stampLengthSlider, stampLengthValueLabel);
        fillValueLabel(nameYSlider, nameYValueLabel);
        fillValueLabel(dateYSlider, dateYValueLabel);
        fillValueLabel(nameFontSizeSlider, nameFontSizeValueLabel);
        fillValueLabel(dateFontSizeSlider, dateFontSizeValueLabel);
        fillValueLabel(borderThicknessSlider, borderThicknessValueLabel);        
    }
    private void fillValueLabel(Slider slider, Label label) {
        double value = slider.getValue();
        int valueAsInt = (int)value;
        label.setText("" + valueAsInt);
    }

    private void registerComboBoxListener(ComboBox comboBox) {
        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                drawStamp();
            }
        });
    }

    private void registerColorPicker(ColorPicker colorPicker) {
        colorPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                drawStamp();
            }
        });
    }

    private void registerSliderListener(Slider slider) {
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                drawStamp();
            }
        });
    }

    private void updateControls() {
        // THIS IS THE TABLE DATA

    }

    private boolean isValidSelection(String selection) {
        return (selection != null) && (!selection.equals("")) && (!selection.equals("-"));
    }

    private void processUpdateVisa() {

        updateControls();
    }

    public void showDialog() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        AppGUIModule gui = app.getGUIModule();
        String titleText = "RVMM - " + data.getRegionName() + " Visa";
        setTitle(titleText);

        loadCurrentVisa();

        updateControls();

        // AND OPEN THE DIALOG
        showAndWait();
    }

    private void loadCurrentVisa() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        stampTypeComboBox.setValue(data.getVisaProperty(VisaProperty.STAMP_TYPE));
        dateFormatComboBox.setValue(data.getVisaProperty(VisaProperty.DATE_FORMAT));
        stampColorPicker.setValue(Color.web(data.getVisaProperty(VisaProperty.STAMP_COLOR)));
        stampLengthSlider.setValue(Integer.parseInt(data.getVisaProperty(VisaProperty.STAMP_LENGTH)));
        nameYSlider.setValue(Integer.parseInt(data.getVisaProperty(VisaProperty.NAME_Y)));
        dateYSlider.setValue(Integer.parseInt(data.getVisaProperty(VisaProperty.DATE_Y)));
        fontFamilyComboBox.setValue(data.getVisaProperty(VisaProperty.FONT_FAMILY));
        nameFontSizeSlider.setValue(Integer.parseInt(data.getVisaProperty(VisaProperty.NAME_FONT_SIZE)));
        dateFontSizeSlider.setValue(Integer.parseInt(data.getVisaProperty(VisaProperty.DATE_FONT_SIZE)));
        borderThicknessSlider.setValue(Integer.parseInt(data.getVisaProperty(VisaProperty.BORDER_THICKNESS)));
    }

    public HashMap<VisaProperty, String> getVisaProperties() {
        // GET THE VALUES FROM THE CONTROLS
        String stampType = stampTypeComboBox.getValue();
        String dateFormat = dateFormatComboBox.getValue();
        Color stampColor = stampColorPicker.getValue();
        int stampLength = (int) Math.round(stampLengthSlider.getValue());
        int nameY = (int) Math.round(nameYSlider.getValue());
        int dateY = (int) Math.round(dateYSlider.getValue());
        String fontFamily = fontFamilyComboBox.getValue();
        int nameFontSize = (int) Math.round(nameFontSizeSlider.getValue());
        int dateFontSize = (int) Math.round(dateFontSizeSlider.getValue());
        int borderThickness = (int) Math.round(borderThicknessSlider.getValue());

        HashMap<VisaProperty, String> newProps = new HashMap();
        newProps.put(VisaProperty.STAMP_TYPE, stampType);
        newProps.put(VisaProperty.DATE_FORMAT, dateFormat);
        newProps.put(VisaProperty.STAMP_COLOR, stampColor.toString());
        newProps.put(VisaProperty.STAMP_LENGTH, "" + stampLength);
        newProps.put(VisaProperty.NAME_Y, "" + nameY);
        newProps.put(VisaProperty.DATE_Y, "" + dateY);
        newProps.put(VisaProperty.FONT_FAMILY, fontFamily);
        newProps.put(VisaProperty.NAME_FONT_SIZE, "" + nameFontSize);
        newProps.put(VisaProperty.DATE_FONT_SIZE, "" + dateFontSize);
        newProps.put(VisaProperty.BORDER_THICKNESS, "" + borderThickness);

        return newProps;
    }

    public void confirmEdits() {
        RegioVincoMapMakerData data = (RegioVincoMapMakerData) app.getDataComponent();
        HashMap<VisaProperty, String> newProps = getVisaProperties();
        HashMap<VisaProperty, String> oldProps = data.cloneVisaProperties();
        UpdateVisa_Transaction transaction = new UpdateVisa_Transaction(data, oldProps, newProps);
        app.processTransaction(transaction);

        // CLOSE THE DIALOG
        this.hide();
    }

    public void cancelEdits() {
        // AND CLOSE THE DIALOG
        hide();
    }
}
