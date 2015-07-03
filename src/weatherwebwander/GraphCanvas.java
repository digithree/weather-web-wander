/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;

/**
 *
 * @author simonkenny
 */
class GraphCanvas extends Canvas {
    
    private static int SAVED_FILE_COUNT = 0;
 
    public GraphCanvas() {
        super(200,200);
        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> doResize());
        heightProperty().addListener(evt -> doResize());
    }
    
    public void saveFile(String filePath){
        if( filePath.charAt(filePath.length()-1) != '/' ) {
            filePath = filePath.concat("/");
        }
        filePath = filePath.concat(String.format("SCR_SHOT_%03d.png", SAVED_FILE_COUNT++));
        System.out.println("GraphCanvas: trying to save scrshot: "+filePath);
        File file = new File(filePath);
        WritableImage wi = new WritableImage((int)getWidth(),(int)getHeight());
        try {
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.color(0.9, 0.9, 0.9));
            ImageIO.write(SwingFXUtils.fromFXImage(this.snapshot(sp,wi),null),"png",file);
            System.out.println("GraphCanvas: file save success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void doResize() {
        double width = getWidth();
        double height = getHeight();
        
        extraResize();
        
        draw();
    }
    
    protected void extraResize() {
        
    }

    public void draw() {
        // debug
        double width = getWidth();
        double height = getHeight();
        
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setStroke(Color.RED);
        gc.strokeLine(0, 0, width, height);
        gc.strokeLine(0, height, width, 0);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
