/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author simonkenny
 */
public class WeatherWebWander extends Application {
    
    private Scene scene;
    
    private GraphCanvas forceDirectedGraphCanvas;
    
    private WebpageManager webpageManager;
    
    private KeywordMatching keywordMatching;
    private KeywordHistogramChart keywordHistogramChart;
    
    //private DomainList domainList;
    
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        
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
        
        Pane pane = new Pane();
        keywordMatching = new KeywordMatching();
        keywordHistogramChart = new KeywordHistogramChart(pane, keywordMatching);
        
        Pane pane2 = new Pane();
        //domainList = new DomainList(pane2);
        
        SplitPane analysisSplitPane = new SplitPane();
        analysisSplitPane.setOrientation(Orientation.VERTICAL);
        analysisSplitPane.getItems().addAll(pane, pane2);
        analysisSplitPane.setDividerPositions(0.6f,0.4f);
        
        SplitPane graphsSplitPane = new SplitPane();
        graphsSplitPane.setOrientation(Orientation.HORIZONTAL);
        graphsSplitPane.getItems().addAll(analysisSplitPane, graphCanvasContainer);
        graphsSplitPane.setDividerPositions(0.25f,0.75f);
        
        // master split pane
        SplitPane masterPane = new SplitPane();
        masterPane.setOrientation(Orientation.VERTICAL);
        Text text = new Text("[WEB PAGE TITLE]");
        text.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 18));
        masterPane.getItems().addAll(graphsSplitPane, text);
        //masterPane.setDividerPositions(0.35f,0.65f);
        
        webpageManager = new WebpageManager(keywordMatching, (ForceDirectedGraphCanvas)forceDirectedGraphCanvas, text);

        // create the scene
        primaryStage.setTitle("Web View - Press escape to exit");
        scene = new Scene(masterPane,500,500, Color.web("#666970"));
        
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if( t.getCode()==KeyCode.ESCAPE ) {
                    //stage.setFullScreen(true); // do this if handling escape to keep large
                    System.out.println("EXITING");
                    webpageManager.prepareForExit();
                    keywordHistogramChart.prepareForExit();
                    //domainList.prepareForExit();
                    primaryStage.close();
                } else {
                    //handleKey(t.getCode());
                }
            }
        });
        
        primaryStage.setScene(scene);
        //scene.getStylesheets().add("webviewsample/BrowserToolbar.css");        
        primaryStage.show();
        primaryStage.setFullScreen(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
