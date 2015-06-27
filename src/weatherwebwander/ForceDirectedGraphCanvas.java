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
    
    ArrayList<GraphNode> graphNodes = new ArrayList<GraphNode>();
    GraphNode headNode;
    
    PVector center;
    float radius = 30;
    //float radius = 700;
    float angleInset = (float)Math.PI / 4.f;
    
    long lastTime = -1;
    long MINIMUM_ELAPSED_TIME = 30000;
    
    public ForceDirectedGraphCanvas() {
        super();
        headNode = new GraphNode(1);
        headNode.unmoveable = true;
        graphNodes.add(headNode);
        
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
    
    final public void addNode(int parentHashCode, int newHashCode, int score) {
        GraphNode newNode = null;
        for( GraphNode node : graphNodes ) {
            if( node.hashCode == parentHashCode ) {
                newNode = new GraphNode(score+1);
                newNode.hashCode = newHashCode;
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
    
    @Override
    protected void extraResize() {
        headNode.pos = new PVector((float)getWidth()/2.f, (float)getHeight()/2.f);
    }
    
    @Override
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        for( GraphNode node : graphNodes ) {
            node.draw(gc);
        }
        
    }
    
    public void update(float deltaTime) {
        for( GraphNode node : graphNodes ) {
            node.applyMovement(deltaTime*GLOBAL_TIME_FACTOR);
            node.applyUniversalWeakReplusion(graphNodes, deltaTime*GLOBAL_TIME_FACTOR);
        }
        draw();
    }
}
