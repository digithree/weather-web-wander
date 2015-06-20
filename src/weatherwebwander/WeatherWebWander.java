/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author simonkenny
 */
public class WeatherWebWander extends Application {
    
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) {
        // create the scene
        primaryStage.setTitle("Web View");
        scene = new Scene(new WebBrowser(),750,500, Color.web("#666970"));
        primaryStage.setScene(scene);
        //scene.getStylesheets().add("webviewsample/BrowserToolbar.css");        
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
