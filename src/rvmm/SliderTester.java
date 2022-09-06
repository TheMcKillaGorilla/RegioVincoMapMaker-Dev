/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rvmm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author McKillaGorilla
 */
public class SliderTester extends Application {
    public void start(Stage primaryStage) {
        Slider slider = new Slider(-1.0, 1.0, 0.0);
        Pane pane = new Pane();
        pane.getChildren().add(slider);
        Scene scene = new Scene(pane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
