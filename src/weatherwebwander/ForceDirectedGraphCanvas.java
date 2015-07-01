/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author simonkenny
 */
public class ForceDirectedGraphCanvas extends GraphCanvas {
    
    float GLOBAL_TIME_FACTOR = 50.f;
    
    ArrayList<WebpageNode> allNodes;
    WebpageNode headNode;
    
    PVector center;
    
    long lastTime = -1;
    long MINIMUM_ELAPSED_TIME = 30000;
    
    public ForceDirectedGraphCanvas() {
        super();
        /*
        headNode = new WebpageNode(WebpageNode.HEAD_NODE_STRING);
        headNode.setUnmoveable(true);
        graphNodes.add(headNode);
        */
        
        // add test nodes
        /*
        addNode(0, 234562);
        addNode(0, 238383);
        addNode(0, 958372);
        addNode(958372, 937422);
        */
        
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
        
        /*
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                beeperHandle.cancel(true);
            }
        }, 60 * 60, TimeUnit.SECONDS);
                */
        // NOTE: only runs for an hour
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
    
    /*
    final public void addNode(int parentHashCode, int newHashCode, int score, int emotion) {
        WebpageNode newNode = null;
        for( WebpageNode node : graphNodes ) {
            if( node.getHashcode() == parentHashCode ) {
                newNode = new WebpageNode(score+1);
                newNode.hashCode = newHashCode;
                newNode.type = emotion;
                float angle = (float)Math.random()*((float)Math.PI*2.f);
                newNode.pos = new PVector(node.pos.x+((float)Math.cos(angle)*radius),
                        node.pos.y+((float)Math.sin(angle)*radius));
                node.addChild(newNode);
                
            }
        }
        if( newNode != null ) {
            graphNodes.add(newNode);
        }
    }
    */
    
    /*
    final public void addNode( WebpageNode webpageNode ) {
        int parentHashCode = 0;
        if( webpageNode.getParent() != null ) {
            parentHashCode = webpageNode.getParent().getHashcode();
        }
        WebpageNode newNode = null;
        for( WebpageNode node : graphNodes ) {
            if( node.hashCode == parentHashCode ) {
                newNode = new WebpageNode(webpageNode.getRelevancy()+1);
                newNode.hashCode = webpageNode.getHashcode();
                newNode.type = webpageNode.getEmotion();
                float angle = (float)Math.random()*((float)Math.PI*2.f);
                newNode.pos = new PVector(node.pos.x+((float)Math.cos(angle)*radius),
                        node.pos.y+((float)Math.sin(angle)*radius));
                node.addChild(newNode);
                
            }
        }
        if( newNode != null ) {
            graphNodes.add(newNode);
        }
    }
    */
    
    @Override
    protected void extraResize() {
        if( headNode != null ) {
            System.out.println("ForceDirectedGraphCanvas:: extraResize");
            headNode.setPos( new PVector((float)getWidth()/2.f, (float)getHeight()/2.f) );
        }
    }
    
    @Override
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        if( allNodes != null ) {
            for( WebpageNode node : allNodes ) {
                node.drawConnections(gc);
            }
            for( WebpageNode node : allNodes ) {
                node.drawNode(gc);
            }
        } else {
            System.out.println("ForceDirectedGraphCanvas:: allNodes is null!");
        }
        
    }
    
    public void update(float deltaTime) {
        if( allNodes != null ) {
            for( WebpageNode node : allNodes ) {
                node.applyForces(allNodes, deltaTime*GLOBAL_TIME_FACTOR);
            }
        }
        draw();
    }
}
