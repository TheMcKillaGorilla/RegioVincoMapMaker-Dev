package rvmm;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public enum RegioVincoMapMakerPropertyType {
    PARENT_ERROR_TITLE,
    PARENT_ERROR_CONTENT,
    RVMM_FOOLPROOF_SETTINGS,
    
    // SOME PATHS
    RVMM_PATH_RAW_MAP_DATA_FILES,
    RVMM_PATH_EXPORT_DEFAULT,
    
    // ADDED TO THE FILE TOOLBAR
    RVMM_DELETE_MAP_BUTTON,
   
    // EDITING TOOLBAR
    RVMM_EDITING_PANE,
    RVMM_RESET_NAVIGATION_BUTTON, 
    RVMM_FIT_TO_SUBREGION_BUTTON,
    RVMM_SHUFFLE_MAP_COLORS_BUTTON,
    
    // OPTIONS TOOLBAR
    RVMM_OPTIONS_PANE,
    RVMM_TOGGLE_DEBUG_LABEL,
    RVMM_TOGGLE_DEBUG_CHECKBOX,
    
    // HELP TOOLBAR
    RVMM_REPORT_ERROR_BUTTON,
    
    // SETTINGS TOOLBAR
    RVMM_SETTINGS_PANE,
    RVMM_EDIT_EXPORT_MAP_BUTTON,
    RVMM_EDIT_LEADERS_BUTTON,
    RVMM_EDIT_VISA_BUTTON,
    RVMM_EDIT_PARENT_REGION_BUTTON,
    RVMM_EDIT_BROCHURE_BUTTON,
    
    // ZOOM TOOLBAR
    RVMM_ZOOM_PANE, 
    RVMM_ZOOM_LABEL, 
    RVMM_ZOOM_TEXT_FIELD, 
    RVMM_ZOOM_IN_BUTTON, 
    RVMM_ZOOM_OUT_BUTTON,

    // EDIT SUBREGIONS BOX
    RVMM_EDIT_SUBREIONS_BOX,
    RVMM_BROCHURE_THUMBNAIL_IMAGE_VIEW,
    RVMM_REGION_NAME_BOX,  
    RVMM_REGION_NAME_LABEL,
    RVMM_SUBREGION_TYPE_PROMPT_LABEL,
    RVMM_SUBREGION_TYPE_COMBO_BOX,
    RVMM_SUBREGION_TYPES,
    RVMM_DEFAULT_SUBREGION_TYPE,
    RVMM_EDIT_SUBREGIONS_GRID_PANE,
    RVMM_MOVE_DOWN_BUTTON, 
    RVMM_MOVE_UP_BUTTON,
    RVMM_ADD_SUBREGIONS_BUTTON,
    RVMM_REMOVE_SUBREGION_BUTTON,

    // FOR CHANGING THE REGION NAME
    RVMM_CHANGE_REGION_NAME_DIALOG_TITLE,
    RVMM_CHANGE_REGION_NAME_DIALOG_CONTENT,

    RVMM_LEFT_PANE,                 // THIS WILL HAVE THE RADIAL GRADIENT AS ITS BACKGROUND
    RVMM_CLIPPED_MAP_PANE,          // THIS WILL NOT HAVE THE GRADIENT
    RVMM_MAP_PANE,                  // THIS WILL HAVE THE POLYGONS AND WILL GO IN THE LEFT PANE
    RVMM_FRAME_LAYER_PANE,          // THIS WILL HAVE THE OUTLINE FRAME
    RVMM_MAP_CLIPPING_RECTANGLE,    // THIS WILL CLIP BOTH THE LEFT PANE AND THE MAP PANE
    
    RVMM_RIGHT_PANE,
    RVMM_DEBUG_TEXT_AREA,
    
    // THE TABLE
    RVMM_SUBREGIONS_TABLE_VIEW,
    RVMM_NAME_TABLE_COLUMN,
    RVMM_CAPITAL_TABLE_COLUMN,
    RVMM_LEADER_TABLE_COLUMN,
    RVMM_LANDMARKS_TABLE_COLUMN,
    RVMM_FLAG_LINK_TABLE_COLUMN,

    // THE DIALOGS
        // BROCHURE
        // EXPORT MAP
        // LEADERS
        // MAP SETTINGS
        // NAME
        // NEW MAP
        // SUBREGION
    
    // BROCHURE DIALOG
    RVMM_BROCHURE_DIALOG_PANE,
    RVMM_BROCHURE_DIALOG_TOP_PANE,
    RVMM_BROCHURE_DIALOG_HEADING,
    RVMM_BROCHURE_DIALOG_CENTER_PANE,
    RVMM_BROCHURE_DIALOG_CONTROLS_PANE,
    RVMM_BROCHURE_DIALOG_IMAGE_PROMPT_LABEL,
    RVMM_BROCHURE_DIALOG_IMAGE_TEXT_FIELD,
    RVMM_BROCHURE_DIALOG_IMAGE_RETRIEVE_BUTTON,
    RVMM_BROCHURE_DIALOG_LINK_PROMPT_LABEL,
    RVMM_BROCHURE_DIALOG_LINK_TEXT_FIELD,
    RVMM_BROCHURE_DIALOG_IMAGE_VIEW,
    RVMM_BROCHURE_DIALOG_BOTTOM_PANE,
    RVMM_BROCHURE_DIALOG_CONFIRM_BUTTON,
    RVMM_BROCHURE_DIALOG_CANCEL_BUTTON,    

    // EXPORT_MAP DIALOG
    RVMM_EXPORT_MAP_DIALOG_TITLE,
    RVMM_EXPORT_MAP_DIALOG_PANE,
    RVMM_EXPORT_MAP_DIALOG_CENTER_PANE,
    RVMM_EXPORT_MAP_DIALOG_EXPORT_SELECTION_PANE,
    RVMM_EXPORT_MAP_DIALOG_EXPORT_HEADING_LABEL,
    RVMM_EXPORT_MAP_DIALOG_EXPORT_TO_RVMM_APP_CHECKBOX,
    RVMM_EXPORT_MAP_DIALOG_RVMM_APP_EXPORT_PATH_PROMPT_LABEL,
    RVMM_EXPORT_MAP_DIALOG_RVMM_APP_EXPORT_PATH_LABEL,
    RVMM_EXPORT_MAP_DIALOG_EXPORT_TO_GAME_APP_CHECKBOX,
    RVMM_EXPORT_MAP_DIALOG_GAME_APP_EXPORT_PATH_PROMPT_LABEL,
    RVMM_EXPORT_MAP_DIALOG_GAME_APP_EXPORT_PATH_LABEL,
    RVMM_EXPORT_MAP_DIALOG_GAME_APP_EXPORT_PATH_EDIT_BUTTON,
    RVMM_EXPORT_MAP_DIALOG_BROCHURE_HEIGHT_LABEL,
    RVMM_EXPORT_MAP_DIALOG_BROCHURE_HEIGHT_SLIDER,
    RVMM_EXPORT_MAP_DIALOG_FLAG_WIDTH_LABEL,    
    RVMM_EXPORT_MAP_DIALOG_FLAG_WIDTH_SLIDER,
    RVMM_EXPORT_MAP_DIALOG_BOTTOM_PANE,
    RVMM_EXPORT_MAP_DIALOG_CONFIRM_BUTTON,
    RVMM_EXPORT_MAP_DIALOG_CANCEL_BUTTON,
    RVMM_EXPORT_MAP_DIALOG_CHANGE_ROOT_GAME_APP_PATH_TITLE,
    ROOT_GAME_APP_PATH_ERROR_TITLE, 
    ROOT_GAME_APP_PATH_ERROR_CONTENT,
    
    // LEADERS DIALOG
    RVMM_LEADERS_DIALOG_PANE,
    RVMM_LEADERS_DIALOG_TOP_PANE,
    RVMM_LEADERS_DIALOG_HEADING_LABEL,
    RVMM_LEADERS_DIALOG_CENTER_PANE,
    RVMM_LEADERS_DIALOG_CONTROLS_PANE,
    RVMM_LEADERS_DIALOG_LEADERS_TYPE_PANE,
    RVMM_LEADERS_DIALOG_LEADERS_TYPE_PROMPT_LABEL,
    RVMM_LEADERS_DIALOG_LEADERS_TYPE_COMBO_BOX,
    RVMM_LEADER_TYPES, 
    RVMM_DEFAULT_LEADER_TYPE,
    RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_TYPE_PANE,
    RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_TYPE_PROMPT_LABEL,
    RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_TYPE_COMBO_BOX,
    RVMM_LEADERS_WIKI_PAGE_TYPES, 
    RVMM_DEFAULT_LEADERS_WIKI_PAGE_TYPE,
    RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_URL_PANE,
    RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_URL_PROMPT_LABEL,
    RVMM_LEADERS_DIALOG_LEADERS_WIKI_PAGE_URL_TEXT_FIELD,
    RVMM_LEADERS_DIALOG_FOUND_LEADER_TABLE_COLUMN,
    RVMM_LEADERS_DIALOG_UPDATE_LEADER_TABLE_COLUMN,
    RVMM_LEADERS_DIALOG_LEADERS_TABLE_VIEW,
    RVMM_LEADERS_DIALOG_SUBREGION_NAME_TABLE_COLUMN,
    RVMM_LEADERS_DIALOG_CURRENT_LEADER_TABLE_COLUMN,
    RVMM_LEADERS_DIALOG_FIND_LEADERS_PANE,
    RVMM_LEADERS_DIALOG_FIND_LEADERS_BUTTON,
    RVMM_LEADERS_DIALOG_BOTTOM_PANE,
    RVMM_LEADERS_DIALOG_CONFIRM_BUTTON,
    RVMM_LEADERS_DIALOG_CANCEL_BUTTON, 
    
    // VISA DIALOG
    RVMM_VISA_DIALOG_TOP_PANE,
    RVMM_VISA_DIALOG_HEADING_LABEL,
    RVMM_VISA_DIALOG_CENTER_PANE,
    RVMM_VISA_DIALOG_GRID_PANE,
    RVMM_VISA_DIALOG_STAMP_WEB_VIEW,
    RVMM_VISA_DIALOG_STAMP_TYPE_LABEL,
    RVMM_VISA_DIALOG_STAMP_TYPE_COMBO_BOX,
    RVMM_VISA_DIALOG_STAMP_TYPE_OPTIONS,
    RVMM_VISA_DIALOG_STAMP_TYPE_DEFAULT,
    RVMM_VISA_DIALOG_DATE_FORMAT_LABEL,
    RVMM_VISA_DIALOG_DATE_FORMAT_COMBO_BOX,
    RVMM_VISA_DIALOG_DATE_FORMAT_OPTIONS,
    RVMM_VISA_DIALOG_DATE_FORMAT_DEFAULT,
    RVMM_VISA_DIALOG_STAMP_COLOR_LABEL,
    RVMM_VISA_DIALOG_STAMP_COLOR_PICKER,
    RVMM_VISA_DIALOG_STAMP_LENGTH_LABEL,
    RVMM_VISA_DIALOG_STAMP_LENGTH_SLIDER,
    RVMM_VISA_DIALOG_STAMP_LENGTH_VALUE_LABEL,
    RVMM_VISA_DIALOG_NAME_Y_LABEL,
    RVMM_VISA_DIALOG_NAME_Y_SLIDER,
    RVMM_VISA_DIALOG_NAME_Y_VALUE_LABEL,
    RVMM_VISA_DIALOG_DATE_Y_LABEL,
    RVMM_VISA_DIALOG_DATE_Y_SLIDER,
    RVMM_VISA_DIALOG_DATE_Y_VALUE_LABEL,
    RVMM_VISA_DIALOG_FONT_FAMILY_LABEL,
    RVMM_VISA_DIALOG_FONT_FAMILY_COMBO_BOX,
    RVMM_VISA_DIALOG_FONT_FAMILY_OPTIONS,
    RVMM_VISA_DIALOG_FONT_FAMILY_DEFAULT,
    RVMM_VISA_DIALOG_NAME_FONT_SIZE_LABEL,
    RVMM_VISA_DIALOG_NAME_FONT_SIZE_SLIDER,
    RVMM_VISA_DIALOG_NAME_FONT_SIZE_VALUE_LABEL,
    RVMM_VISA_DIALOG_DATE_FONT_SIZE_LABEL,
    RVMM_VISA_DIALOG_DATE_FONT_SIZE_SLIDER,
    RVMM_VISA_DIALOG_DATE_FONT_SIZE_VALUE_LABEL,
    RVMM_VISA_DIALOG_BORDER_THICKNESS_LABEL,
    RVMM_VISA_DIALOG_BORDER_THICKNESS_SLIDER,
    RVMM_VISA_DIALOG_BORDER_THICKNESS_VALUE_LABEL,
    RVMM_VISA_DIALOG_BOTTOM_PANE,
    RVMM_VISA_DIALOG_CONFIRM_BUTTON,
    RVMM_VISA_DIALOG_CANCEL_BUTTON,
    
    // NAME DIALOG
    RVMM_NAME_DIALOG_TITLE,
    RVMM_NAME_DIALOG_PANE,
    RVMM_NAME_DIALOG_CENTER_PANE,
    RVMM_NAME_DIALOG_REGION_NAME_PROMPT_LABEL,
    RVMM_NAME_DIALOG_REGION_NAME_TEXT_FIELD,
    RVMM_NAME_DIALOG_WARNING_LABEL,
    RVMM_NAME_DIALOG_BOTTOM_PANE,
    RVMM_NAME_DIALOG_CONFIRM_BUTTON,
    RVMM_NAME_DIALOG_CANCEL_BUTTON,
    
    // NEW MAP DIALOG
    RVMM_NEW_MAP_DIALOG_TITLE,
    RVMM_NEW_MAP_SELECT_PARENT_REGION_DIALOG_TITLE,
    RVMM_NEW_MAP_SELECT_SHP_DIALOG_TITLE,
    RVMM_NEW_MAP_SELECT_DBF_DIALOG_TITLE,
    RVMM_NEW_MAP_DIALOG_TOP_PANE,
    RVMM_NEW_MAP_DIALOG_CENTER_PANE,
    RVMM_NEW_MAP_DIALOG_BOTTOM_PANE,
    RVMM_NEW_MAP_DIALOG_HEADER_LABEL,
    RVMM_NEW_MAP_DIALOG_NAME_PROMPT_LABEL,
    RVMM_NEW_MAP_DIALOG_NAME_TEXT_FIELD,
    RVMM_NEW_MAP_DIALOG_SHP_PROMPT_LABEL,
    RVMM_NEW_MAP_DIALOG_SHP_LABEL,
    RVMM_NEW_MAP_DIALOG_SHP_EDIT_BUTTON,
    RVMM_NEW_MAP_DIALOG_DBF_PROMPT_LABEL,
    RVMM_NEW_MAP_DIALOG_DBF_LABEL,
    RVMM_NEW_MAP_DIALOG_DBF_EDIT_BUTTON,
    RVMM_NEW_MAP_DIALOG_DBF_FIELD_PROMPT_LABEL,
    RVMM_NEW_MAP_DIALOG_DBF_FIELD_COMBO_BOX,
    RVMM_NEW_MAP_DIALOG_PARENT_REGION_PROMPT_LABEL,
    RVMM_NEW_MAP_DIALOG_PARENT_REGION_TREE_VIEW,
    RVMM_NEW_MAP_DIALOG_WARNING_LABEL,
    RVMM_NEW_MAP_DIALOG_CONFIRM_BUTTON,
    RVMM_NEW_MAP_DIALOG_CANCEL_BUTTON,
    
    // PARENT REGION DIALOG
    RVMM_PARENT_REGION_DIALOG_PANE,
    RVMM_PARENT_REGION_DIALOG_TOP_PANE,
    RVMM_PARENT_REGION_DIALOG_HEADING_LABEL,
    RVMM_PARENT_REGION_DIALOG_CENTER_PANE,
    RVMM_PARENT_REGION_DIALOG_BOTTOM_PANE,
    RVMM_PARENT_REGION_DIALOG_REGIONS_TREE_VIEW,
    RVMM_PARENT_REGION_REGION_SUMMARY_WEB_VIEW,
    RVMM_PARENT_REGION_DIALOG_CONFIRM_BUTTON,
    RVMM_PARENT_REGION_DIALOG_CANCEL_BUTTON, 
    
    // EDIT SUBREGION DIALOG
    RVMM_SUBREGION_DIALOG_TITLE,
    RVMM_SUBREGION_DIALOG_TOP_PANE,
    RVMM_SUBREGION_DIALOG_CENTER_LEFT_PANE,
    RVMM_SUBREGION_DIALOG_BOTTOM_PANE,
    RVMM_SUBREGION_DIALOG_HEADER_LABEL,
    RVMM_SUBREGION_DIALOG_PREVIOUS_BUTTON,
    RVMM_SUBREGION_DIALOG_NEXT_BUTTON,
    RVMM_SUBREGION_DIALOG_NAME_PROMPT_LABEL,
    RVMM_SUBREGION_DIALOG_NAME_TEXT_FIELD,
    RVMM_SUBREGION_DIALOG_CAPITAL_PROMPT_LABEL,
    RVMM_SUBREGION_DIALOG_CAPITAL_TEXT_FIELD,
    RVMM_SUBREGION_DIALOG_LEADER_PROMPT_LABEL,
    RVMM_SUBREGION_DIALOG_LEADER_TEXT_FIELD,
    RVMM_SUBREGION_DIALOG_FLAG_LINK_PROMPT_LABEL,
    RVMM_SUBREGION_DIALOG_FLAG_LINK_TEXT_FIELD,
    RVMM_SUBREGION_DIALOG_TERRITORY_PROMPT_LABEL,
    RVMM_SUBREGION_DIALOG_TERRITORY_CHECKBOX,
    RVMM_SUBREGION_DIALOG_FLAG_IMAGE_VIEW,
    RVMM_SUBREIONG_DIALOG_DOWNLOAD_FLAG_PANE,
    RVMM_SUBREGION_DIALOG_DOWNLOAD_FLAG_IMAGE_BUTTON,
    RVMM_SUBREGION_DIALOG_DOWNLOAD_ALL_FLAG_IMAGES_BUTTON,
    RVMM_SUBREGION_DIALOG_CENTER_RIGHT_PANE,
    RVMM_SUBREGION_DIALOG_TOP_LANDMARKS_PANE,
    RVMM_SUBREGION_DIALOG_LANDMARKS_LABEL,
    RVMM_SUBREGION_DIALOG_ADD_LANDMARK_PANE,
    RVMM_SUBREGION_DIALOG_ADD_LANDMARK_PROMPT_LABEL,
    RVMM_SUBREGION_DIALOG_ADD_LANDMARK_TEXT_FIELD,
    RVMM_SUBREGION_DIALOG_ADD_LANDMARK_BUTTON,
    RVMM_SUBREGION_DIALOG_LANDMARKS_LIST_VIEW,
    RVMM_SUBREGION_DIALOG_LANDMARKS_DESCRIPTION_PANE,
    RVMM_SUBREGION_DIALOG_LANDMARKS_DESCRIPTION_LABEL,
    RVMM_SUBREGION_DIALOG_LANDMARKS_DESCRIPTION_TEXT_FIELD,
    RVMM_SUBREGION_DIALOG_CENTER_PANE,
    RVMM_SUBREGION_DIALOG_OK_BUTTON,
    RVMM_SUBREGION_DIALOG_CANCEL_BUTTON,
   
    // FRAME SHAPES
    RVMM_TOP_FRAME,
    RVMM_BOTTOM_FRAME,
    RVMM_LEFT_FRAME,
    RVMM_RIGHT_FRAME,
    RVMM_FLAG_FRAME,
    RVMM_SEAL_FRAME,
    
    // FOR THE EXPORT DIALOG
    RVMM_EXPORT_DIALOG_PANE,
    RVMM_EXPORT_DIALOG_TOP_PANE,
    RVMM_EXPORT_DIALOG_REGION_NAME_LABEL,
    RVMM_EXPORT_DIALOG_NAME_LABEL,
    RVMM_EXPORT_DIALOG_CAPITAL_LABEL,
    RVMM_EXPORT_DIALOG_LEADER_LABEL,
    RVMM_EXPORT_DIALOG_FLAGS_LABEL,
    RVMM_EXPORT_DIALOG_MAP_IMAGE_VIEW,
    RVMM_EXPORT_DIALOG_BOTTOM_PANE,
    RVMM_EXPORT_DIALOG_STATS_LABEL
}