/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 *
 * @author simonkenny
 */
public class WeatherWebWander extends Application {
    
    private Scene scene;
    
    private GraphCanvas forceDirectedGraphCanvas;
    
    private WebpageManager webpageManager;
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        //SplitPane graphsSplitPane = new SplitPane();
        //graphsSplitPane.setOrientation(Orientation.VERTICAL);
        
        forceDirectedGraphCanvas = new ForceDirectedGraphCanvas();
        AnchorPane graphCanvasContainer = new AnchorPane();
        AnchorPane.setTopAnchor(forceDirectedGraphCanvas, 0.0);
        AnchorPane.setBottomAnchor(forceDirectedGraphCanvas, 0.0);
        AnchorPane.setLeftAnchor(forceDirectedGraphCanvas, 0.0);
        AnchorPane.setRightAnchor(forceDirectedGraphCanvas, 0.0);
        graphCanvasContainer.getChildren().add(forceDirectedGraphCanvas);
        SplitPane.setResizableWithParent(graphCanvasContainer, true);
        forceDirectedGraphCanvas.widthProperty().bind(
                       graphCanvasContainer.widthProperty());
        forceDirectedGraphCanvas.heightProperty().bind(
                       graphCanvasContainer.heightProperty());
        
        //graphsSplitPane.getItems().addAll(graphCanvasContainer, textFlow);
        //graphsSplitPane.setDividerPositions(0.66f,0.34f);
        
        // master split pane
        SplitPane masterPane = new SplitPane();
        masterPane.setOrientation(Orientation.VERTICAL);
        Text text = new Text("[WEB PAGE TITLE]");
        text.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 18));
        masterPane.getItems().addAll(graphCanvasContainer, text);
        //masterPane.setDividerPositions(0.35f,0.65f);
        
        webpageManager = new WebpageManager((ForceDirectedGraphCanvas)forceDirectedGraphCanvas, text);

        // create the scene
        primaryStage.setTitle("Web View");
        scene = new Scene(masterPane,500,500, Color.web("#666970"));
        primaryStage.setScene(scene);
        //scene.getStylesheets().add("webviewsample/BrowserToolbar.css");        
        primaryStage.show();
        //primaryStage.setFullScreen(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
