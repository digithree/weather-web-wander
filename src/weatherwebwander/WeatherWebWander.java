/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author simonkenny
 */
public class WeatherWebWander extends Application {
    
    private Scene scene;
    
    private GraphCanvas graphCanvas;
    private boolean graphCanvasToggle;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        SplitPane pane = new SplitPane();
        //pane.setMaxHeight(Double.MAX_VALUE);
        graphCanvas = new GraphCanvas();
        AnchorPane graphCanvasContainer = new AnchorPane();
        AnchorPane.setTopAnchor(graphCanvas, 0.0);
        AnchorPane.setBottomAnchor(graphCanvas, 0.0);
        AnchorPane.setLeftAnchor(graphCanvas, 0.0);
        AnchorPane.setRightAnchor(graphCanvas, 0.0);
        graphCanvasContainer.getChildren().add(graphCanvas);
        SplitPane.setResizableWithParent(graphCanvasContainer, true);
        pane.getItems().addAll(graphCanvasContainer, new WebBrowser());
        graphCanvas.widthProperty().bind(
                       graphCanvasContainer.widthProperty());
        graphCanvas.heightProperty().bind(
                       graphCanvasContainer.heightProperty());
        pane.setDividerPositions(0.5f,0.5f);
        /*
        graphCanvas.toBack();
        Button button = new Button("Toggle Graph");
        graphCanvasToggle = false;
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(graphCanvasToggle) {
                    graphCanvas.toBack();
                } else {
                    graphCanvas.toFront();
                }
                graphCanvasToggle = !graphCanvasToggle;
            }
        });
        */
        /*
        AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setTopAnchor(pane,0.0);
        AnchorPane.setBottomAnchor(pane,0.0);
        AnchorPane.setLeftAnchor(pane,0.0);
        AnchorPane.setRightAnchor(pane,0.0);
        anchorPane.getChildren().add(pane);
        */
        /*
        VBox vBox = new VBox();
        vBox.setMaxHeight(Double.MAX_VALUE);
        //vBox.getChildren().addAll(button,anchorPane);
        vBox.getChildren().addAll(button,pane);
        */
        // create the scene
        primaryStage.setTitle("Web View");
        scene = new Scene(pane,750,700, Color.web("#666970"));
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
