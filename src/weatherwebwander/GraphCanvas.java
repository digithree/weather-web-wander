/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author simonkenny
 */
class GraphCanvas extends Canvas {
 
    public GraphCanvas() {
        super(200,200);
        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> doResize());
        heightProperty().addListener(evt -> doResize());
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
