/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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
    private GraphCanvas vennDiagramGraphCanvas;
    private TextFlow textFlow;
    
    private WebpageManager webpageManager;
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        SplitPane graphsSplitPane = new SplitPane();
        graphsSplitPane.setOrientation(Orientation.VERTICAL);
        
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
        
        /*
        vennDiagramGraphCanvas = new GraphCanvas();
        AnchorPane vennGraphCanvasContainer = new AnchorPane();
        AnchorPane.setTopAnchor(vennDiagramGraphCanvas, 0.0);
        AnchorPane.setBottomAnchor(vennDiagramGraphCanvas, 0.0);
        AnchorPane.setLeftAnchor(vennDiagramGraphCanvas, 0.0);
        AnchorPane.setRightAnchor(vennDiagramGraphCanvas, 0.0);
        vennGraphCanvasContainer.getChildren().add(vennDiagramGraphCanvas);
        SplitPane.setResizableWithParent(vennGraphCanvasContainer, true);
        vennDiagramGraphCanvas.widthProperty().bind(
                       vennGraphCanvasContainer.widthProperty());
        vennDiagramGraphCanvas.heightProperty().bind(
                       vennGraphCanvasContainer.heightProperty());
        */
        
        textFlow = new TextFlow();
        //textFlow.setMinWidth(Float.MAX_VALUE);
        textFlow.setMaxHeight(200);
        textFlow.getChildren().add(new Text("Some kind of text."));
        textFlow.getChildren().add(new Text("Some other kind of text."));
        
        /*
        graphsSplitPane.getItems().addAll(graphCanvasContainer, vennGraphCanvasContainer, textFlow);
        graphsSplitPane.setDividerPositions(0.33f,0.33f,0.34f);
        */
        
        graphsSplitPane.getItems().addAll(graphCanvasContainer, textFlow);
        graphsSplitPane.setDividerPositions(0.66f,0.34f);
        
        // master split pane
        SplitPane masterPane = new SplitPane();
        masterPane.setOrientation(Orientation.HORIZONTAL);
        WebBrowser webBrowser = new WebBrowser();
        masterPane.getItems().addAll(graphsSplitPane, webBrowser);
        masterPane.setDividerPositions(0.35f,0.65f);
        
        webpageManager = new WebpageManager(webBrowser, (ForceDirectedGraphCanvas)forceDirectedGraphCanvas);

        // create the scene
        primaryStage.setTitle("Web View");
        scene = new Scene(masterPane,750,700, Color.web("#666970"));
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
