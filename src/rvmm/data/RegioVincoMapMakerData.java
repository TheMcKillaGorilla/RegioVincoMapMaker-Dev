package rvmm.data;

import djf.components.AppDataComponent;
import djf.modules.AppGUIModule;
import djf.modules.AppRecentWorkModule;
import djf.modules.AppWork;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.apache.commons.io.FileUtils;
import rvmm.RegioVincoMapMakerApp;
import static rvmm.RegioVincoMapMakerPropertyType.*;
import static rvmm.data.MapDataKeys.*;
import static rvmm.data.RVMM_Constants.*;
import rvmm.files.RegioVincoFiles;
import static rvmm.workspace.DebugDisplay.appendDebugText;
import rvmm.workspace.RegioVincoMapMakerWorkspace;
import rvmm.workspace.controllers.MapEditController;

public class RegioVincoMapMakerData implements AppDataComponent {

    RegioVincoMapMakerApp app;
    MapNavigator mapNavigator;
    SubregionPrototype selectedSubregion;
    SubregionPrototype mousedOverSubregion;
    TableViewSelectionModel subregionsSelectionModel;
    boolean loading;
    boolean mapLoaded;

    HashMap<RVPropertyType, String> appProperties = new HashMap();
    HashMap<MapPropertyType, String> mapProperties = new HashMap();
    ObservableList<SubregionPrototype> subregions;

    public RegioVincoMapMakerData(RegioVincoMapMakerApp initApp) {
        app = initApp;
        selectedSubregion = null;

        // GET ALL THE THINGS WE'LL NEED TO MANIUPLATE THE TABLE
        TableView tableView = (TableView) app.getGUIModule().getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
        subregions = tableView.getItems();
        subregionsSelectionModel = tableView.getSelectionModel();
        subregionsSelectionModel.setSelectionMode(SelectionMode.SINGLE);

        mapNavigator = new MapNavigator(app);
        loading = false;
        mapLoaded = false;

        // FIRST LOAD ALL THE DEFAULT VALUES
        this.loadDefaultAppProperties();
        this.loadDefaultMapProperties();

        // THEN LOAD SAVED SETTINGS
        this.loadAppProperties();
    }
    
    public boolean isSubregionSelected() {
        return selectedSubregion != null;
    }
    
    public boolean isFirstSubregionSelected() {
        return isSubregion(0);
    }
    public boolean isLastSubregionSelected() {
        return isSubregion(subregions.size()-1);
    }
    public boolean isSubregion(int index) {
        if (selectedSubregion == null)
            return false;
        else if (subregions.size() == 0) {
            return false;
        }
        else {
            SubregionPrototype firstSubregion = this.subregions.get(index);
            return firstSubregion == selectedSubregion;
        }
    }

    public boolean isValidNewRegion(String regionName, String parentRegionPath) {
        // WE NEED TO LOOK INSIDE THE work DIRECTORY TO SEE IF 
        // SUCH A FILE ALREADY EXISTS
        String testPath = this.getWorkFilePath(regionName, parentRegionPath);
        File testFile = new File(testPath);
        return !testFile.exists();
    }
    public boolean isMapLoaded() {
        return mapLoaded;
    }
    
    // VIEWPORT ACCESSORS
    public RegioVincoMapMakerApp getApp() {
        return app;
    }
    public MapNavigator getMapNavigator() {
        return mapNavigator;
    }
    public double getMapWidth() {
        return ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE)).getWidth();
    }
    public double getMapHeight() {
        return ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE)).getHeight();
    }    
    public void setMapDimensions(double mapWidth, double mapHeight) {
        Pane mapPane = ((Pane) app.getGUIModule().getGUINode(RVMM_MAP_PANE));
        mapPane.setMinWidth(mapWidth);
        mapPane.setMaxWidth(mapWidth);
        mapPane.setMinHeight(mapHeight);
        mapPane.setMaxHeight(mapHeight);
    }

    // App PROPERTIES
    public String getAppProperty(RVPropertyType type) {
        return appProperties.get(type);
    }
    public void setAppProperty(RVPropertyType type, String value) {
        this.appProperties.put(type, value);
        if (!loading)
            saveAppProperties();
    }
    public void loadAppProperties() {
        // LOAD THE SETTINGS IF THEY EXIST
        File settingsFile = new File(RVMM_APP_PROPERTIES_FILEPATH);
        if (settingsFile.exists()) {
            // LOAD THEM
            try {
                FileReader isr = new FileReader(settingsFile);
                BufferedReader br = new BufferedReader(isr);

                // FIRST LINE IS THE EXPORT FLAGS WIDTH
                setAppProperty(RVPropertyType.BROCHURES_HEIGHT, readAppProperty(br));
                setAppProperty(RVPropertyType.EXPORT_MAP_SIZE_IN_MB, readAppProperty(br));
                setAppProperty(RVPropertyType.EXPORT_TO_GAME_APP, readAppProperty(br));
                setAppProperty(RVPropertyType.EXPORT_TO_RVMM_APP, readAppProperty(br));
                setAppProperty(RVPropertyType.ROOT_GAME_APP_PATH, readAppProperty(br));
                setAppProperty(RVPropertyType.FLAGS_WIDTH, readAppProperty(br));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    public String readAppProperty(BufferedReader br) throws IOException {
        String line = br.readLine();
        String data = line.substring(line.indexOf(":") + 1);
        return data;
    }
    public void saveAppProperties() {
        try {
            PrintWriter pw = new PrintWriter(RVMM_APP_PROPERTIES_FILEPATH);
            pw.println(APP_SETTINGS_BROCHURES_HEIGHT + getBrochuresHeight());
            pw.println(APP_SETTINGS_EXPORT_MAP_SIZE + getExportMapSizeInMB());
            pw.println(APP_SETTINGS_EXPORT_TO_GAME_APP + getExportToGameApp());
            pw.println(APP_SETTINGS_EXPORT_TO_RVMM_APP + getExportToRVMMApp());
            pw.println(APP_SETTINGS_ROOT_GAME_APP_PATH + getRootGameAppPath());
            pw.println(APP_SETTINGS_FLAGS_WIDTH + getFlagsWidth());
            pw.flush();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void loadDefaultAppProperties() {
        loading = true;
        setAppProperty(RVPropertyType.BROCHURES_HEIGHT, "" + DEFAULT_BROCHURES_HEIGHT);
        setAppProperty(RVPropertyType.EXPORT_MAP_SIZE_IN_MB, "" + DEFAULT_EXPORT_MAP_SIZE_IN_MB);
        setAppProperty(RVPropertyType.EXPORT_TO_GAME_APP, "" + false);
        setAppProperty(RVPropertyType.EXPORT_TO_RVMM_APP, "" + true);
        setAppProperty(RVPropertyType.ROOT_GAME_APP_PATH, DEFAULT_ROOT_RVMM_APP_PATH);
        setAppProperty(RVPropertyType.FLAGS_WIDTH, "" + DEFAULT_FLAGS_WIDTH);
        loading = false;
    }
    public int getBrochuresHeight() {
        String heightPropertyValue = appProperties.get(RVPropertyType.BROCHURES_HEIGHT);
        return Integer.parseInt(heightPropertyValue);
    }
    public void setBrochuresHeight(int value) {
        appProperties.put(RVPropertyType.BROCHURES_HEIGHT, "" + value);
        saveAppProperties();
    }
    public double getExportMapSizeInMB() {
        String exportMapSizeInMB = appProperties.get(RVPropertyType.EXPORT_MAP_SIZE_IN_MB);
        return Double.parseDouble(exportMapSizeInMB);
    }
    public double getExportMapSizeInBytes() {
        return getExportMapSizeInMB() * 1000000;
    }
    public void setExportMapSizeInMB(double value) {
        appProperties.put(RVPropertyType.EXPORT_MAP_SIZE_IN_MB, "" + value);
        saveAppProperties();
    }
    public boolean getExportToGameApp() {
        return Boolean.parseBoolean(appProperties.get(RVPropertyType.EXPORT_TO_GAME_APP));
    }
    public void setExportToGameApp(boolean value) {
        appProperties.put(RVPropertyType.EXPORT_TO_GAME_APP, "" + value);
        saveAppProperties();
    }
    public boolean getExportToRVMMApp() {
        return Boolean.parseBoolean(appProperties.get(RVPropertyType.EXPORT_TO_RVMM_APP));
    }
    public void setExportToRVMMApp(boolean value) {
        appProperties.put(RVPropertyType.EXPORT_TO_RVMM_APP, "" + value);
        saveAppProperties();
    }
    public String getRootRVMMAppPath() {
        return DEFAULT_ROOT_RVMM_APP_PATH;
    }
    public String getRootGameAppPath() {
        return appProperties.get(RVPropertyType.ROOT_GAME_APP_PATH);
    }
    public void setRootGameAppPath(String value) {
        appProperties.put(RVPropertyType.ROOT_GAME_APP_PATH, "" + value);
        saveAppProperties();
    }
    public int getFlagsWidth() {
        String widthPropertyValue = appProperties.get(RVPropertyType.FLAGS_WIDTH);
        return Integer.parseInt(widthPropertyValue);
    }
    public void setFlagsWidth(int initFlagsWidth) {
        appProperties.put(RVPropertyType.FLAGS_WIDTH, "" + initFlagsWidth);
        saveAppProperties();
    }

    // Map Properties
    public String getMapProperty(MapPropertyType type) {
        return mapProperties.get(type);
    }
    public void setMapProperty(MapPropertyType type, String value) {
        mapProperties.put(type, value);
        AppGUIModule gui = app.getGUIModule();

        switch (type) {
            case BROCHURE_IMAGE_URL: {
                RegioVincoMapMakerWorkspace workspace = (RegioVincoMapMakerWorkspace) app.getWorkspaceComponent();
                workspace.loadBrochureThumbnail();
                appendDebugText("Setting Brochure Image URL to " + value);
                break;
            }
            case REGION_NAME: {
                // CHANGE IT IN THE REGION NAME LABEL
                Label headerLabel = (Label) gui.getGUINode(RVMM_REGION_NAME_LABEL);
                headerLabel.setText(value);
                // MAKE SURE THE RECENT HISTORY IS UPDATED
                appendDebugText("Setting Region Name to " + value);
                break;
            }
            case SUBREGION_TYPE: {
                this.startLoading();
                ComboBox subregionTypeComboBox = (ComboBox) gui.getGUINode(RVMM_SUBREGION_TYPE_COMBO_BOX);
                subregionTypeComboBox.getSelectionModel().select(value);
                appendDebugText("Setting Subregion Type to " + value);
                this.endLoading();
                break;
            }
        }
    }
    public void updateParentPath(String newParentPath) {
        String oldParentPath = this.getParentRegionPath();
        this.setParentRegionPath(newParentPath);
        
        // CHANGE IT IN ALL THE DESCENDANT FILES
        RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
        AppWork oldWork = app.getFileModule().getWork();
        try {
            String dirPath = oldWork.getDirectoryPath() + oldWork.getName() + "/";
            File workDir = new File(dirPath);
            if (workDir.exists()) {
                // CHANGE THE PARENT PATHS IN ALL CHILDREN
                files.updateAllDescendantPaths(this, workDir, oldParentPath, newParentPath);
                
                // AND THEN CHANGE THE PATH OF THIS REGION'S SUBDIRECTORY
                String newDirPath = "work/" + newParentPath + getRegionName() + "/";
                File newDir = new File(newDirPath);
                FileUtils.moveDirectory(workDir, newDir);
                
                // WE ALSO HAVE TO CHANGE THIS WORK FILE
                File regionFile = new File(oldWork.getFilePath());
                files.updatePathInFile(regionFile, oldParentPath, newParentPath);

                // AND THEN MOVE IT
                String currentWorkFilePath = oldWork.getFilePath();
                File currentWorkFile = new File(currentWorkFilePath);
                String newWorkFilePath = "work/" + newParentPath + getRegionName() + WORK_FILE_EXT;
                File newWorkFile = new File(newWorkFilePath);                
                FileUtils.moveFile(currentWorkFile, newWorkFile);
            }
        } catch(IOException ioe) { ioe.printStackTrace(); }

        // CHANGE IT IN THE RECENT WORK
        AppRecentWorkModule recentWork = app.getRecentWorkModule();
        AppWork newWork = recentWork.updateWork(oldWork, 
                        "work/" + newParentPath, this.getRegionName(), WORK_FILE_EXT);
        app.getFileModule().setWork(newWork);

        // AND THEN CHANGE ALL THE OTHER RECENT WORK THAT
        // MUST NOW CHANGE ITS PATH
        Iterator<AppWork> it = recentWork.getWorkIterator();
        while (it.hasNext()) {
            AppWork work = it.next();
            String path = work.getDirectoryPath();
            String testPath = oldWork.getDirectoryPath() + oldWork.getName() + "/";
            String newStartPath = newParentPath + oldWork.getName() + "/";
            if (path.startsWith(testPath)) {
                // IT HAS TO CHANGE
                String newPath = path.replace(testPath, newStartPath);
                recentWork.updateWork(work, "work/" + newPath, work.getName(), WORK_FILE_EXT);
            }
        }             
    }    
    public void changeRegionName(String newRegionName) {
        this.setRegionName(newRegionName);
        
        // CHANGE IT IN ALL THE DESCENDANT FILES
        RegioVincoFiles files = (RegioVincoFiles)app.getFileComponent();
        AppWork oldWork = app.getFileModule().getWork();
        try {
            String dirPath = oldWork.getDirectoryPath() + oldWork.getName() + "/";
            File workDir = new File(dirPath);
            if (workDir.exists()) {
                // CHANGE THE PARENT PATHS IN ALL CHILDREN
                files.updateAllDescendants(this, workDir, oldWork.getName(), newRegionName);
                
                // AND THEN CHANGE THE NAME OF THIS REGION'S SUBDIRECTORY
                String newDirPath = oldWork.getDirectoryPath() + newRegionName + "/";
                File newDir = new File(newDirPath);
                FileUtils.moveDirectory(workDir, newDir);
                
                // WE ALSO HAVE TO CHANGE THIS WORK FILE
                String currentWorkFilePath = oldWork.getFilePath();
                File currentWorkFile = new File(currentWorkFilePath);
                String newWorkFilePath = oldWork.getDirectoryPath() + newRegionName + WORK_FILE_EXT;
                File newWorkFile = new File(newWorkFilePath);
                FileUtils.moveFile(currentWorkFile, newWorkFile);
            }
        } catch(IOException ioe) { ioe.printStackTrace(); }
        
        // CHANGE IT IN THE RECENT WORK
        AppRecentWorkModule recentWork = app.getRecentWorkModule();
        AppWork newWork = recentWork.updateWork(oldWork, 
                        "work/" + this.getParentRegionPath(), newRegionName, WORK_FILE_EXT);
        app.getFileModule().setWork(newWork);
        
        // AND THEN CHANGE ALL THE OTHER RECENT WORK THAT
        // MUST NOW CHANGE ITS PATH
        Iterator<AppWork> it = recentWork.getWorkIterator();
        while (it.hasNext()) {
            AppWork work = it.next();
            String path = work.getDirectoryPath();
            String testPath = oldWork.getDirectoryPath() + oldWork.getName() + "/";
            String newStartPath = oldWork.getDirectoryPath() + newRegionName + "/";
            if (path.startsWith(testPath)) {
                // IT HAS TO CHANGE
                String newPath = path.replace(testPath, newStartPath);
                recentWork.updateWork(work, newPath, work.getName(), WORK_FILE_EXT);
            }
        }   
        
        // FINALLY, SAVE THIS FILE
        try {
            app.getFileModule().saveWork();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public boolean isValidNewName(String testName) {
        boolean nameIsSame = testName.equals(getRegionName());
        boolean nameIsEmpty = testName.trim().length() == 0;
        String testWorkFile = getWorkFilePath(testName, getParentRegionPath());
        boolean namedRegionAlreadyExists = new File(testWorkFile).exists();
        if (nameIsSame || nameIsEmpty || namedRegionAlreadyExists) {
            return false;
        }
        else {
            return true;
        }
    }
    public boolean rename(String newRegionName) {
        if (isValidNewName(newRegionName)) {
            // RENAME THE work FILE
            // @todo
            // IF THERE IS A work SUBDIRECTORY, RENAME THAT
            
            // IF THERE IS AN  orig_images DIRECTORY RENAME THAT
            
            // RENAME THE BROCHURE FILE
            
            return true;
        }
        else {
            return false;
        }
    }
    public String getBrochureImageURL() {
        return getMapProperty(MapPropertyType.BROCHURE_IMAGE_URL);
    }
    public void setBrochureImageURL(String value) {
        setMapProperty(MapPropertyType.BROCHURE_IMAGE_URL, value);
    }
    public String getBrochureLink() {
        return getMapProperty(MapPropertyType.BROCHURE_LINK);
    }
    public void setBrochureLink(String value) {
        setMapProperty(MapPropertyType.BROCHURE_LINK, value);
    }
    public String getLandmarksDescription() {
        return getMapProperty(MapPropertyType.LANDMARKS_DESCRIPTION);
    }  
    public void setLandmarksDescription(String value) {
        setMapProperty(MapPropertyType.LANDMARKS_DESCRIPTION, value);
    }
    public String getLeadersType() {
        return getMapProperty(MapPropertyType.LEADERS_TYPE);
    }
    public void setLeadersType(String value) {
        setMapProperty(MapPropertyType.LEADERS_TYPE, value);
    }
    public String getLeadersWikiPageType() {
        return getMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_TYPE);
    }
    public void setLeadersWikiPageType(String value) {
        setMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_TYPE, value);
    }
    public String getLeadersWikiPageURL() {
        return getMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_URL);
    }
    public void setLeadersWikiPageURL(String value) {
        setMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_URL, value);
    }
    public String getParentRegionPath() {
        return getMapProperty(MapPropertyType.PARENT_REGION_PATH);
    }
    public void setParentRegionPath(String value) {
        setMapProperty(MapPropertyType.PARENT_REGION_PATH, value);
    }
    public String getRegionName() {
        return mapProperties.get(MapPropertyType.REGION_NAME);
    }
    public void setRegionName(String value) {
        setMapProperty(MapPropertyType.REGION_NAME, value);
    }
    public String getSubregionType() {
        return getMapProperty(MapPropertyType.SUBREGION_TYPE);
    }
    public void setSubregionType(String value) {
        setMapProperty(MapPropertyType.SUBREGION_TYPE, value);
    }

    // NOW LET'S PROVIDE SOME DYNAMICALLY GENERATED PATHS
    public String getExportGameAppDataPath() {
        return getRootGameAppPath()
               + EXPORT_DATA_PATH
               + getParentRegionPath() + "/"
               + getRegionName() + "/";        
    }
    public String getExportRVMMAppDataPath() {
        return DEFAULT_ROOT_RVMM_APP_PATH
               + EXPORT_DATA_PATH
               + getParentRegionPath() + "/"
               + getRegionName() + "/";
    }
    public String getExportGameAppDataFilePath() {
        return getExportGameAppDataPath()
                + getRegionName() + ".json";
    }
    public String getExportRVMMAppDataFilePath() {
        return getExportRVMMAppDataPath()
                + getRegionName() + ".json";
    }
    public String getExportGameAppImagesPath() {
        return getRootGameAppPath()
                + EXPORT_IMAGES_PATH
                + getParentRegionPath() + "/"
                + getRegionName() + "/";
    }
    public String getExportRVMMAppImagesPath() {
        return DEFAULT_ROOT_RVMM_APP_PATH
                + EXPORT_IMAGES_PATH
                + getParentRegionPath() + "/"
                + getRegionName() + "/";                
    }
    public String getExportGameAppBrochurePath() {
        return getExportGameAppImagesPath()
                + getRegionName() + " Brochure.png";
    }
    public String getExportRVMMAppBrochurePath() {
        return getExportRVMMAppImagesPath()
                + getRegionName() + " Brochure.png";
    }
    public String getExportGameAppFlagPath(SubregionPrototype subregion) {
        return getExportGameAppImagesPath()
                + subregion.getName() + " Flag.png";
    }
    public String getExportRVMMAppFlagPath(SubregionPrototype subregion) {
        return getExportRVMMAppImagesPath()
                + subregion.getName() + " Flag.png";
    }
    public String getRootParentWorkPath() {
        return "work/The World";
    }
    public String getOrigImagesPath() {
        String orig = ORIG_IMAGES_ROOT_PATH;
        String parentRegionPath = mapProperties.get(MapPropertyType.PARENT_REGION_PATH) + "/";
        String regionPath = mapProperties.get(MapPropertyType.REGION_NAME) + "/";
        String origImagesPath = orig + parentRegionPath + regionPath;
        return origImagesPath;
    } 
    public String getOrigBrochurePath() {
        return getOrigImagesPath()
                + mapProperties.get(MapPropertyType.REGION_NAME)
                + " Brochure.png";
    } 
    public String getOrigFlagPath(SubregionPrototype subregion) {
        String origImagesPath = getOrigImagesPath();
        return origImagesPath
                + subregion.getName() + " Flag.png";
    }
    public String getWorkFilePath(String regionName, String parentRegionPath) {
        String path = WORK_PATH + parentRegionPath + regionName + WORK_FILE_EXT;
        return path;
    }
    public String getWorkFilePath() {
        String regionName = mapProperties.get(MapPropertyType.REGION_NAME);
        String parentRegionPath = mapProperties.get(MapPropertyType.PARENT_REGION_PATH);
        return getWorkFilePath(regionName, parentRegionPath);
    }
    public boolean getFileExists(String path) {
        File testFile = new File(path);
        return testFile.exists();
    }
    public boolean getSubregionHasFlag(SubregionPrototype subregion) {
        String subregionPath = getOrigFlagPath(subregion);
        return getFileExists(subregionPath);
    }
    public boolean getRegionHasBrochure() {
        String brochurePath = getOrigBrochurePath();
        return getFileExists(brochurePath);
    }

    private void loadDefaultMapProperties() {
        loading = true;

        // FIRST RESET MAP NAVIGATION
        this.mapNavigator.reset();
 
        // THEN THE MAP PROPERTIES
        this.setMapProperty(MapPropertyType.PARENT_REGION_PATH, DEFAULT_PARENT_DIRECTORY);
        this.setMapProperty(MapPropertyType.BROCHURE_IMAGE_URL, DEFAULT_BROCHURE_IMAGE_URL);
        this.setMapProperty(MapPropertyType.BROCHURE_LINK, DEFAULT_BROCHURE_LINK);
        this.setMapProperty(MapPropertyType.LANDMARKS_DESCRIPTION, DEFAULT_LANDMARKS_DESCRIPTION);
        this.setMapProperty(MapPropertyType.LEADERS_TYPE, DEFAULT_LEADERS_TYPE);
        this.setMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_TYPE, DEFAULT_LEADERS_WIKI_PAGE_TYPE);
        this.setMapProperty(MapPropertyType.LEADERS_WIKI_PAGE_URL, DEFAULT_LEADERS_WIKI_PAGE_URL);
        this.setMapProperty(MapPropertyType.REGION_NAME, DEFAULT_REGION_NAME);
        this.setMapProperty(MapPropertyType.SUBREGION_TYPE, DEFAULT_SUBREGION_TYPE);

        loading = false;
    }

    public void reset() {
        AppGUIModule gui = app.getGUIModule();
        mapLoaded = true;

        // CLEAR OUT THE SUBREGIONS DATA
        subregions.clear();
        
        // CLEAR OUT THE TABLE
        TableView subregionsTableView = (TableView)gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
        subregionsTableView.getItems().clear();
        subregionsTableView.refresh();
        
        // CLEAR THE MAP
        Pane mapPane = (Pane) gui.getGUINode(RVMM_MAP_PANE);
        mapPane.getChildren().clear();

        // AND RESET ALL THE CONTROLS TO DEFAULT VALUES
        this.loadDefaultMapProperties();

        RegioVincoMapMakerWorkspace workspace = (RegioVincoMapMakerWorkspace) app.getWorkspaceComponent();
        workspace.loadBrochureThumbnail();
    }
    public void unload() {
        mapLoaded = false;
    }
    public ObservableList<SubregionPrototype> getSubregions() {
        return subregions;
    }
    public Iterator<SubregionPrototype> subregionsIterator() {
        return this.subregions.iterator();
    }
    public int getNumSubregions() {
        return subregions.size();
    }
    public void deselectSubregion() {
        if (selectedSubregion != null) {
            Iterator<Polygon> polyIt = selectedSubregion.polygonsIterator();
            while (polyIt.hasNext()) {
                Polygon poly = polyIt.next();
                poly.setFill(selectedSubregion.getGreyscaleColor());
            }
            AppGUIModule gui = app.getGUIModule();
            TableView<SubregionPrototype> subregionTable = (TableView) gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
            subregionTable.getSelectionModel().clearSelection();
            selectedSubregion = null;
        }
    }
    public void selectSubregion(SubregionPrototype subregion) {
        startLoading();
        deselectSubregion();
        selectedSubregion = subregion;
        if (selectedSubregion == null) {
            return;
        }
        subregionsSelectionModel.select(selectedSubregion);
        Iterator<Polygon> polyIt = selectedSubregion.polygonsIterator();
        while (polyIt.hasNext()) {
            Polygon poly = polyIt.next();
            poly.setFill(FILL_COLOR_SELECTED_SUBREGION);
        }
        endLoading();
    }
    public void addSubregion(SubregionPrototype subregionToAdd) {
        addSubregionPolygons(subregionToAdd);
        subregions.add(subregionToAdd);
    }
    public void addSubregionPolygons(SubregionPrototype subregionToAdd) {
        Iterator<Polygon> polys = subregionToAdd.polygonsIterator();
        AppGUIModule gui = app.getGUIModule();
        Pane map = (Pane) gui.getGUINode(RVMM_MAP_PANE);
        while (polys.hasNext()) {
            Polygon poly = polys.next();
            poly.setUserData(subregionToAdd);
            registerPolygonHandlers(poly);
            map.getChildren().add(poly);
        }
    }
    public void removeSubregionPolygons(SubregionPrototype subregionToAdd) {
        Iterator<Polygon> polys = subregionToAdd.polygonsIterator();
        AppGUIModule gui = app.getGUIModule();
        Pane map = (Pane) gui.getGUINode(RVMM_MAP_PANE);
        while (polys.hasNext()) {
            Polygon poly = polys.next();
            poly.setUserData(subregionToAdd);
            map.getChildren().remove(poly);
        }
    }
    public void addSubregion(ArrayList<ArrayList<Double>> rawPolygons) {
        SubregionPrototype subregionToAdd = new SubregionPrototype();
        AppGUIModule gui = app.getGUIModule();
        Pane map = (Pane) gui.getGUINode(RVMM_MAP_PANE);
        for (int i = 0; i < rawPolygons.size(); i++) {
            ArrayList<Double> rawPolygonPoints = rawPolygons.get(i);
            Polygon polygonToAdd = new Polygon();
            ObservableList<Double> transformedPolygonPoints = polygonToAdd.getPoints();
            for (int j = 0; j < rawPolygonPoints.size(); j += 2) {
                double longX = rawPolygonPoints.get(j);
                double latY = rawPolygonPoints.get(j + 1);
                double x = mapNavigator.longToX(longX);
                double y = mapNavigator.latToY(latY);
                transformedPolygonPoints.addAll(x, y);
            }
            subregionToAdd.addPolygon(polygonToAdd);
            polygonToAdd.setStrokeWidth(0);
            polygonToAdd.setUserData(subregionToAdd);
            map.getChildren().add(polygonToAdd);
            registerPolygonHandlers(polygonToAdd);
        }
        subregionToAdd.loadBounds();
        subregions.add(subregionToAdd);
    }
    public void moveSubregionUp(SubregionPrototype subregion) {
        int index = subregions.indexOf(subregion);
        if (index > 0) {
            SubregionPrototype temp = subregions.get(index - 1);
            subregions.set(index - 1, subregion);
            subregions.set(index, temp);
        }
    }
    public void moveSubregionDown(SubregionPrototype subregion) {
        int index = subregions.indexOf(subregion);
        if (index < (subregions.size() - 1)) {
            SubregionPrototype temp = subregions.get(index + 1);
            subregions.set(index + 1, subregion);
            subregions.set(index, temp);
        }
    }

    public void registerPolygonHandlers(Polygon poly) {
        SubregionPrototype subregion = (SubregionPrototype) poly.getUserData();
        poly.setOnMouseEntered(e -> {
            mapNavigator.highlightSubregion(subregion);
        });
        poly.setOnMouseExited(e -> {
            mapNavigator.unhighlightSubregion(subregion);
        });
        poly.setOnMousePressed(e -> {
            if (subregion == selectedSubregion) {
                deselectSubregion();
            } else {
                selectSubregion(subregion);
            }
        });
        poly.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                // OPEN EDIT DIALOG
                MapEditController controller = ((RegioVincoMapMakerWorkspace) app.getWorkspaceComponent()).getMapEditController();
                controller.processEditSubregion(subregion);
            }
        });
    }

    public void startLoading() {
        loading = true;
    }
    public void endLoading() {
        loading = false;
        mapLoaded = true;
    }
    public boolean isLoading() {
        return loading;
    }

    public void randomizeSubregionColors() {
        HashMap<SubregionPrototype, Color> newColors = generateNewGreyscaleColors();
        Iterator<SubregionPrototype> it = newColors.keySet().iterator();
        while (it.hasNext()) {
            SubregionPrototype subregion = it.next();
            Color greyscaleColor = newColors.get(subregion);
            subregion.setGreyscaleColor(greyscaleColor);
        }
    }
    public HashMap<SubregionPrototype, Color> getCurrentGreyscaleColors() {
        HashMap<SubregionPrototype, Color> subregionToGreyscaleMappings = new HashMap();
        Iterator<SubregionPrototype> subregionsIt = subregions.iterator();
        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            Color greyscaleColor = subregion.getGreyscaleColor();
            subregionToGreyscaleMappings.put(subregion, greyscaleColor);
        }
        return subregionToGreyscaleMappings;
    }
    public HashMap<SubregionPrototype, Color> generateNewGreyscaleColors() {
        // FIRST GENERATE A RANDOM ORDER FOR ALL THE SUBREGIONS
        Iterator<SubregionPrototype> subregionsIt = subregions.iterator();
        ArrayList<SubregionPrototype> subregionsToRandomize = new ArrayList();
        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            subregionsToRandomize.add(subregion);
        }
        Collections.shuffle(subregionsToRandomize);

        // THEN CALCULATE THE SUBREGIONS INTERVAL
        double subregionsInterval = 1.0 / subregions.size();
        subregionsIt = subregionsToRandomize.iterator();
        double rgb = 0.0;
        HashMap<SubregionPrototype, Color> subregionToGreyscaleMappings = new HashMap();
        while (subregionsIt.hasNext()) {
            SubregionPrototype subregion = subregionsIt.next();
            Color fillColor = Color.color(rgb, rgb, rgb);
            subregionToGreyscaleMappings.put(subregion, fillColor);
            rgb += subregionsInterval;
        }
        return subregionToGreyscaleMappings;
    }

    public boolean doSubregionsAllHaveNames() {
        for (SubregionPrototype subregion : subregions) {
            if ((subregion.getName().trim().length() == 0)
                    || (subregion.getName().equals(DEFAULT_NAME))) {
                return false;
            }
        }
        return true;
    }
    public boolean doSubregionsAllHaveCapitals() {
        for (SubregionPrototype subregion : subregions) {
            if ((subregion.getCapital().trim().length() == 0)
                    || (subregion.getCapital().equals(DEFAULT_CAPITAL))) {
                return false;
            }
        }
        return true;
    }
    public boolean doSubregionsAllHaveLeaders() {
        for (SubregionPrototype subregion : subregions) {
            if ((subregion.getLeader().trim().length() == 0)
                    || (subregion.getLeader().equals(DEFAULT_LEADER))) {
                return false;
            }
        }
        return true;
    }
    public boolean doSubregionsAllHaveFlags() {
        for (SubregionPrototype subregion : subregions) {
            String subregionFlagPath = getOrigFlagPath(subregion);
            File flagFile = new File(subregionFlagPath);
            if (!flagFile.exists()) {
                return false;
            }
        }
        return true;
    }
    public boolean doSubregionsHaveLandmarks() {
        for (SubregionPrototype subregion : subregions) {
            // WE ONLY NEED ONE
            if (subregion.cloneLandmarks().size() > 0) {
                return true;
            }
        }
        return false;
    }    
    public int getSubregionIndex(SubregionPrototype subregion) {
        return this.subregions.indexOf(subregion);
    }

    public void removeSubregion(SubregionPrototype selectedSubregion) {
        // REMOVE THE SUBREGION FROM THE TABLE
        this.subregions.remove(selectedSubregion);
        
        // AND THE MAP
        this.removeSubregionPolygons(selectedSubregion);
    }

    public void restoreSubregion(int subregionIndex, SubregionPrototype subregion) {
        addSubregionPolygons(subregion);
        
        // RESTORE THE SUBREGION TO THE TABLE
        this.subregions.add(subregionIndex, subregion);
    }

    public void refreshSubregions() {
        AppGUIModule gui = app.getGUIModule();
        TableView subregionsTableView = (TableView)gui.getGUINode(RVMM_SUBREGIONS_TABLE_VIEW);
        subregionsTableView.refresh();
    }
    
    private HashMap<VisaProperty, String> visaProperties = new HashMap();
    public String getVisaProperty(VisaProperty prop) {
        return visaProperties.get(prop);
    }
    public void setVisaProperty(VisaProperty prop, String value) {
        visaProperties.put(prop, value);
    }
    public HashMap<VisaProperty, String> cloneVisaProperties() {
        return (HashMap<VisaProperty, String>) visaProperties.clone();
    }
}