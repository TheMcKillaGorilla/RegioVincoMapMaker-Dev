package rvmm.workspace;

import javafx.scene.control.TextArea;

public class DebugDisplay {
    static TextArea debugTextArea;
    
    public static void setDebugTextArea(TextArea initDebugTextArea) {
        debugTextArea = initDebugTextArea;
    }
    
    public static void appendDebugText(String text) {
        debugTextArea.appendText(text + "\n");        
    }
}
