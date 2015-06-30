/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author simonkenny
 */
public class WebpageNode {
    
    public final static String HEAD_NODE_STRING = "[none]";
    
    private ArrayList<WebpageNode> parents = new ArrayList<WebpageNode>();
    private ArrayList<WebpageNode> children = new ArrayList<WebpageNode>();
    private ArrayList<WebpageNode> allConnections = new ArrayList<WebpageNode>();
    
    private String URL;
    private boolean metricsSet = false;
    private int relevancy;
    private int emotion;
    private int size = 1;
    private int color = 0;
    private boolean visited = false;
    
    // gfx
    private final float FORCE_FACTOR = 0.005f;
    private final float ENTROPY_FACTOR = 0.0f;
    private final float EQUILIBRIUM_DIST = 5.f;
    private final float MAX_VEL = 20.f;
    
    private final int NODE_VIZ_SIZE = 5;
    private final int MAX_SIZE = 8;
    private final int SIZE_ADJUST_FACTOR = 4;
    private final int MAX_EMOTION = 50;
    
    private final float POPULATE_CLOSE_RADIUS = 30;
    private final float POPULATE_CLOSE_ANGLE = (float)Math.PI / 4.f;
    
    private PVector pos = new PVector();
    private PVector vector = new PVector();
    private boolean unmoveable = true;
    private float mass = 0.1f;
    
    
    public WebpageNode(String URL) {
        this.URL = URL;
    }
    
    // --- family
    public void addChild( WebpageNode child ) {
        if( !children.contains(child) ) {
            System.out.println("["+getHashcode()+"] Added child ("+child.getHashcode()+")");
            children.add(child);
            if( !allConnections.contains(child) ) {
                allConnections.add(child);
            }
            child.addParent(this);
        }
    }

    public void addParent( WebpageNode parent ) {
        if( !parents.contains(parent) ) {
            System.out.println("["+getHashcode()+"] Added parent ("+parent.getHashcode()+")");
            parents.add(parent);
            // move node pos close to parent
            float angle = (float)Math.random()*((float)Math.PI*2.f);
            pos = new PVector(parent.getPos().x+((float)Math.cos(angle)*POPULATE_CLOSE_RADIUS),
                        parent.getPos().y+((float)Math.sin(angle)*POPULATE_CLOSE_RADIUS));
            unmoveable = false; // anything with a parent is released from unmovability
            if( !allConnections.contains(parent) ) {
                allConnections.add(parent);
            }
        }
    }

    public ArrayList<WebpageNode> getChildren() {
        return children;
    }
    
    public ArrayList<WebpageNode> getParents() {
        return parents;
    }
    
    public void setMetrics(int relevancy, int emotion) {
        this.relevancy = relevancy;
        this.size = relevancy + 1;
        //mass = (1.f / (float)size) * 0.1f;
        mass = 1.f;
        
        this.emotion = emotion;
        color = emotion;
        
        metricsSet = true;
    }
    
    // --- misc accessors
    
    public int getHashcode() {
        return URL.hashCode();
    }
    
    public String getURL() {
        return URL;
    }
    
    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setUnmoveable(boolean unmoveable) {
        this.unmoveable = unmoveable;
    }
    
    public boolean areMetricsSet() {
        return metricsSet;
    }
    
    public int getEmotion() {
        return emotion;
    }
    
    public int getRelevancy() {
        return relevancy;
    }
    
    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    
    public PVector getPos() {
        return pos;
    }
    
    public void setPos(PVector pos) {
        this.pos = pos;
    }
    
    // --- draw
    public void applyMovement( float deltaTime ) {
        vector = new PVector();
        for( WebpageNode node : allConnections ) {
            applyAttraction( node, deltaTime ,FORCE_FACTOR, EQUILIBRIUM_DIST);
        }
        pos.add(vector);
    }

    public void applyUniversalWeakReplusion( ArrayList<WebpageNode> allNodes, float deltaTime ) {
        for( WebpageNode node : allNodes ) {
            if( node != this ) {
                //applyAttraction( node, deltaTime, FORCE_FACTOR * 0.01, EQUILIBRIUM_DIST * 3 );
                applyReplusion( node, deltaTime, FORCE_FACTOR * 1000000 );
            }
        }
        pos.add(vector);	
    }

    void applyAttraction( WebpageNode other, float deltaTime, float force, float distance ) {
        if( unmoveable ) {
            return;
        }
        PVector attract = new PVector( other.pos.x, other.pos.y );
        attract.sub( pos );
        PVector entropy = new PVector();
        entropy.set(attract);

        float actualForce = force * (attract.mag() - distance);
        attract.normalize();
        attract.mult(actualForce);

        entropy.mult(ENTROPY_FACTOR);

        attract.sub(entropy);
        vector.add(attract);
        //F = -k(|x|-d)(x/|x|) - bv
  }

    void applyReplusion( WebpageNode other, float deltaTime, float force ) {
        if( unmoveable ) {
            return;
        }
        PVector attract = new PVector( other.pos.x, other.pos.y );
        attract.sub( pos );
        float actualForce = -force * (mass*other.mass)/Utils.sq(attract.mag());
        actualForce /= mass;
        if( actualForce > MAX_VEL ) {
            actualForce = MAX_VEL;
        }
        //println( "force = " + force );
        attract.normalize();
        attract.mult(actualForce);
        //attract.mult(mass*other.mass);
        attract.mult(deltaTime);
        vector.add(attract);
    }
    
    public void draw(GraphicsContext context) {
        if( pos != null ) {
            Color col = Color.color(0,0,1.f,0.7f);
            if( !unmoveable ) {
                if( color < 0 ) {
                    float val = (float)-(color<=MAX_EMOTION?color:MAX_EMOTION) / (float)MAX_EMOTION;
                    if( val > 1.f ) {
                        val = 1.f;
                    }
                    col = Color.color(val,0,1.f-val,0.7f);
                } else if( color > 0 ) {
                    float val = (float)(color<=MAX_EMOTION?color:MAX_EMOTION) / (float)MAX_EMOTION;
                    if( val > 1.f ) {
                        val = 1.f;
                    }
                    col = Color.color(0,val,1.f-val,0.7f);
                }
            } else {
                col = Color.BLACK;
            }
            context.setFill(col);
            context.setStroke(Color.WHITE);
            context.setLineWidth(1);
            int adjustedSize = (size/SIZE_ADJUST_FACTOR)>=1?(size/SIZE_ADJUST_FACTOR):1;
            float ovalSize = NODE_VIZ_SIZE*(adjustedSize<=MAX_SIZE?adjustedSize:MAX_SIZE);
            if( visited || unmoveable ) {
                context.fillOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
                context.strokeOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
            } else {
                context.setStroke(Color.BLACK);
                context.strokeOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
            }
            // draw connections
            for( WebpageNode child : children ) {
                if( child.pos != null ) {
                    context.setStroke(Color.color(0, 0, 0, 0.2));
                    context.setLineWidth(1);
                    context.strokeLine(pos.x,pos.y,child.pos.x,child.pos.y);
                }
            }
        }
    }
}
