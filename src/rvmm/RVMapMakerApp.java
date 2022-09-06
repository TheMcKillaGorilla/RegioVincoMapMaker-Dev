package rvmm;

import java.util.Locale;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;

public class RVMapMakerApp extends Application {
    
    RegioVincoMapMakerApp app = new RegioVincoMapMakerApp();

    public static void main(String[] args) {
	Locale.setDefault(Locale.US);
	launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        app.start(primaryStage);
    }    
}
