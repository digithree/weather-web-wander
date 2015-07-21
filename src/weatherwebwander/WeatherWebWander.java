/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
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
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author simonkenny
 */
public class WeatherWebWander extends Application {
    
    private final String PREF_PATH = "WWW_SCRSHOT_PATH";
    
    private Scene scene;
    
    private GraphCanvas forceDirectedGraphCanvas;
    
    private WebpageManager webpageManager;
    
    private KeywordMatching keywordMatching;
    private KeywordHistogramChart keywordHistogramChart;
    
    private DomainData domainData;
    private DomainIconsGraph domainIconsGraph;
    
    private BestURLLearner bestURLLearner;
    private URLWordHistogramChart wordHistogramChart;
    
    
    private String path;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        Preferences prefs = Preferences.userNodeForPackage(WeatherWebWander.class);
        // UNCOMMENT THIS IF YOU WANT TO RESET THE VIDEOS
        //prefs.remove(PREF_PATH);
        path = prefs.get(PREF_PATH, null);
        if( path == null ) {
            // do first time stuff
            Optional<String> response = Dialogs.create()
                    .owner(primaryStage)
                    .title("First time setup")
                    .masthead("Please choose where you would like to save screenshots")
                    .message("Path:")
                    .showTextInput();
            // One way to get the response value.
            if (response.isPresent()) {
                path = response.get();
            }
            if( path == null ) {
                // error
                Dialogs.create()
                        .owner(null)
                        .title("Error")
                        .masthead("Invalid path")
                        .message("Couldn't add path, please restart and re-enter")
                        .showError();
                Platform.exit();
                return;
            }
            prefs.put(PREF_PATH, path);
        }
        
        
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
        
        Pane pane1 = new Pane();
        keywordMatching = new KeywordMatching();
        keywordHistogramChart = new KeywordHistogramChart(pane1, keywordMatching);
        
        Pane pane2 = new Pane();
        bestURLLearner = new BestURLLearner();
        wordHistogramChart = new URLWordHistogramChart(pane2, bestURLLearner);
        
        domainData = new DomainData();
        domainIconsGraph = new DomainIconsGraph(domainData);
        
        AnchorPane domainIconsGraphCanvasContainer = new AnchorPane();
        domainIconsGraphCanvasContainer.setMaxHeight(200);
        AnchorPane.setTopAnchor(domainIconsGraph, 0.0);
        AnchorPane.setBottomAnchor(domainIconsGraph, 0.0);
        AnchorPane.setLeftAnchor(domainIconsGraph, 0.0);
        AnchorPane.setRightAnchor(domainIconsGraph, 0.0);
        domainIconsGraphCanvasContainer.getChildren().add(domainIconsGraph);
        SplitPane.setResizableWithParent(domainIconsGraphCanvasContainer, true);
        domainIconsGraph.widthProperty().bind(
                       domainIconsGraphCanvasContainer.widthProperty());
        domainIconsGraph.heightProperty().bind(
                       domainIconsGraphCanvasContainer.heightProperty());
        
        SplitPane analysisSplitPane = new SplitPane();
        analysisSplitPane.setOrientation(Orientation.VERTICAL);
        analysisSplitPane.getItems().addAll(pane1, pane2, domainIconsGraphCanvasContainer);
        //analysisSplitPane.getItems().addAll(pane1, pane2, domainIconsGraph);
        analysisSplitPane.setDividerPositions(0.37f,0.37f,0.26f);
        
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
        
        webpageManager = new WebpageManager(keywordMatching, (ForceDirectedGraphCanvas)forceDirectedGraphCanvas,
                text, domainData, bestURLLearner, path);

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
                    wordHistogramChart.prepareForExit();
                    domainIconsGraph.prepareForExit();
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
