/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author simonkenny
 */
public class ForceDirectedGraphCanvas extends GraphCanvas {
    
    private float GLOBAL_TIME_FACTOR = 50.f;
    
    private ArrayList<WebpageNode> allNodes;
    private WebpageNode headNode;
    
    private PVector center;
    
    private long lastTime = -1;
    private long MINIMUM_ELAPSED_TIME = 30000;
    
    private String title;
    
    private WebpageNode currentNode;
    private String currentPageTitle;
    private PVector crossHairsPos = new PVector();
    
    public ForceDirectedGraphCanvas() {
        super();
        
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if( lastTime == -1 ) {
                    lastTime = now;
                }
                /*
                long elapsedTimeNano = now - lastTime;
                if( elapsedTimeNano >= MINIMUM_ELAPSED_TIME ) {
                    float deltaTime = (float)(elapsedTimeNano/1000000)/1000.f;
                    update(deltaTime);
                    lastTime = now;
                }
                */
                update(0.005f);
            }
        }.start();
    }
    
    
    public void setCurrentNode(WebpageNode currentNode) {
        this.currentNode = currentNode;
    }
    
    public void setCurrentPageTitle(String currentPageTitle) {
        this.currentPageTitle = currentPageTitle;
    }
    
    public WebpageNode getCurrentNode() {
        return currentNode;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public ArrayList<WebpageNode> getAllNodes() {
        return allNodes;
    }
    
    public void setNodesList(ArrayList<WebpageNode> allNodes, WebpageNode headNode) {
        this.allNodes = allNodes;
        this.headNode = headNode;
    }
    
    public WebpageNode getNode(int hashcode) {
        if( allNodes != null ) {
            for( WebpageNode node : allNodes ) {
                if( node.getHashcode() == hashcode ) {
                    return node;
                }
            }
        }
        return null;
    }
    
    @Override
    protected void extraResize() {
        if( headNode != null ) {
            System.out.println("ForceDirectedGraphCanvas:: extraResize");
            headNode.setPos( new PVector((float)getWidth()/2.f, (float)getHeight()/2.f) );
        }
    }
    
    @Override
    public void draw() {
        GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        drawCrossHairs(context);
        if( allNodes != null ) {
            for( WebpageNode node : allNodes ) {
                node.drawConnections(context);
            }
            for( WebpageNode node : allNodes ) {
                node.drawNode(context);
            }
        } else {
            System.out.println("ForceDirectedGraphCanvas:: allNodes is null!");
        }
        if( title != null ) {
            context.setFill(Color.BLACK);
            context.fillText(title, 20, 20);
        }
    }
    
    public void update(float deltaTime) {
        if( allNodes != null ) {
            for( WebpageNode node : allNodes ) {
                node.applyForces(allNodes, deltaTime*GLOBAL_TIME_FACTOR);
            }
        }
        updateCrossHairs();
        draw();
    }
    
    private void updateCrossHairs() {
        if( currentNode != null ) {
            PVector attract = new PVector( currentNode.getPos().x, currentNode.getPos().y );
            attract.sub( crossHairsPos );

            float actualForce = attract.mag() * 0.1f;
            attract.normalize();
            attract.mult(actualForce);

            crossHairsPos.add(attract);
        }
    }
    
    private void drawCrossHairs(GraphicsContext context) {
        if( crossHairsPos != null ) {
            context.setStroke(Color.ORANGE);
            context.strokeLine(0, crossHairsPos.y, getWidth(), crossHairsPos.y);
            context.strokeLine(crossHairsPos.x, 0, crossHairsPos.x, getHeight());
            if( currentPageTitle != null ) {
                //context.setFill(Color.DARKGRAY);
                context.setFill(Color.BLACK);
                context.fillText(currentPageTitle, 20, crossHairsPos.y - 10);
            }
        }
    }
}
