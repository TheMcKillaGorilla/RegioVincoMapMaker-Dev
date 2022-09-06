package rvmm.data;

import javafx.scene.paint.Color;

public class RVMM_Constants {
    public static final String RVMM_FEEDBACK_FORM_URL = "https://docs.google.com/forms/d/1F_unQaXyu84PF_Zum9OB_844l13McpzXBy0MOiLyZBI";
    public static final String RVMM_APP_PROPERTIES_FILEPATH = "settings/rvmm_app_settings.txt";

    // EXPORT FILE PATH
    public static final String RVMM_TEST_EXPORT_FILEPATH = "export/ExportData.json";
    public static final String RVMM_ORIG_IMAGES_PATH = "orig_images/";
    public static final String TEMP_BROCHURE_PATH = "images/TempBrochure.png";
    
    // DEFAULT VALUES FOR OUR APP AND MAPS
    public static final double DEFAULT_SCALE = 1;
    public static final double DEFAULT_TRANSLATE = 0;
    public static final int DEFAULT_MAP_WIDTH = 1024;
    public static final int DEFAULT_MAP_HEIGHT = 860;
    
    // DEFAULT APP PROPERTIES
    public static final int DEFAULT_BROCHURES_HEIGHT = 500;
    public static final double DEFAULT_EXPORT_MAP_SIZE_IN_MB = 3.0;
    public static final String DEFAULT_ROOT_RVMM_APP_PATH = "app/";
    public static final int DEFAULT_FLAGS_WIDTH = 200;
    
    // FOR HELPING TO CALCULATE PATHS
    public static final String EXPORT_DATA_PATH = "data/";
    public static final String EXPORT_IMAGES_PATH = "public/img/";
    public static final String ORIG_IMAGES_ROOT_PATH = "orig_images/";
    public static final String WORK_PATH = "work/";
    public static final String WORK_FILE_EXT = ".rvm";

    // DEFAULT MAP PROPERTIES
    public static final String DEFAULT_BROCHURE_IMAGE_URL = "";
    public static final String DEFAULT_BROCHURE_LINK = "";
    public static final String DEFAULT_LANDMARKS_DESCRIPTION = "Find the landmarks of note";
    public static final String DEFAULT_LEADERS_TYPE = "-";
    public static final String DEFAULT_LEADERS_WIKI_PAGE_TYPE = "-";
    public static final String DEFAULT_LEADERS_WIKI_PAGE_URL = "";
    public static final String DEFAULT_PARENT_DIRECTORY = "The World";
    public static final String DEFAULT_REGION_NAME = "?";
    public static final String DEFAULT_SUBREGION_TYPE = "Region";
    
    public static final String DEFAULT_NAME = "-";
    public static final String DEFAULT_CAPITAL = "-";
    public static final String DEFAULT_LEADER = "-";
    public static final String DEFAULT_FLAG_LINK = "-";
    public static final Color DEFAULT_SUBREGION_FILL_COLOR = Color.BLUEVIOLET;
    
    // USED FOR NAVIGATION
    public static final double SCALE_FACTOR = 1.05;
    
    // BORDER COLOR
    public static final Color FILL_COLOR_SELECTED_SUBREGION = Color.LIMEGREEN;
    public static final Color FILL_COLOR_HIGHLIGHTED_SUBREGION = Color.LIGHTYELLOW;
    
    // MAP FRAME FILL COLOR
    public static final Color MAP_FRAME_FILL_COLOR = Color.web("#5b92e5");
        
    // MAP FRAME OFFSETS
    public static final int MAP_FRAME_THICKNESS = 10;
    public static final int MAP_FRAME_IMAGE_LENGTH = 300;
    
    // MAP SETTINGS
    public static final int MAP_SETTINGS_PANE_HGAP = 20;
    public static final int MAP_SETTINGS_PANE_VGAP = 10;
    public static final int BROCHURE_THUMBNAIL_HEIGHT = 50;
}
