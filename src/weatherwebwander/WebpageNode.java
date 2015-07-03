/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
    private final float CENTRAL_FORCE_FACTOR = 0.001f;
    private final float CENTRAL_EQUILIBRIUM_DIST = 15.f;
    
    private final float FORCE_FACTOR = 0.005f;
    private final float ENTROPY_FACTOR = 0.0f;
    private final float EQUILIBRIUM_DIST = 5.f;
    private final float MAX_VEL = 2.f;
    private final float MAX_VECTOR_MAG = 4.f;
    //private final float MIN_DIST_FOR_FORCE = 2.f;
    
    private final int NODE_VIZ_SIZE = 5;
    private final int MAX_SIZE = 15;
    private final int SIZE_ADJUST_FACTOR = 2;
    private final int MAX_EMOTION = 30;
    
    private final float POPULATE_CLOSE_RADIUS = 30;
    
    private PVector pos = new PVector();
    private PVector vector = new PVector();
    private boolean unmoveable = true;
    private float mass = 0.1f;
    
    private final int MIN_RELEVANCY = 3;
    
    private int level = 0;
    
    private Image favIconImage;
    
    
    
    public int getLevel() {
        return level;
    }
    
    public WebpageNode(String URL) {
        this.URL = URL;
    }
    
    // --- family
    public void addChild( WebpageNode child ) {
        if( this != child && !children.contains(child) ) {
            if( this.getHashcode() != child.getHashcode() ) {
                System.out.println("["+getHashcode()+"] Added child ("+child.getHashcode()+")");
                children.add(child);
                if( !allConnections.contains(child) ) {
                    allConnections.add(child);
                }
                child.addParent(this);
            }
        }
    }

    public void addParent( WebpageNode parent ) {
        if( !parents.contains(parent) ) {
            System.out.println("["+getHashcode()+"] Added parent ("+parent.getHashcode()+")");
            parents.add(parent);
            // move node pos close to parent if not first parent
            if( parents.size() == 1 ) {
                level = parent.getLevel() + 1;
                float angle = (float)Math.random()*((float)Math.PI*2.f);
                pos = new PVector(parent.getPos().x+((float)Math.cos(angle)*POPULATE_CLOSE_RADIUS),
                            parent.getPos().y+((float)Math.sin(angle)*POPULATE_CLOSE_RADIUS));
                unmoveable = false; // anything with a parent is released from unmovability
            } else {
                if( parent.getLevel() + 1 < level ) {
                    level = parent.getLevel() + 1;
                }
            }
            if( !allConnections.contains(parent) ) {
                allConnections.add(parent);
            }
        }
    }
    
    public void removeNodeReferences( WebpageNode child ) {
        children.remove(child);
        parents.remove(child);
        allConnections.remove(child);
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
        //mass = 1.f;
        
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
    
    public void setFavIconImage(Image favIconImage) {
        this.favIconImage = favIconImage;
    }
    
    // --- draw
    public void applyForces(ArrayList<WebpageNode> allNodes, float deltaTime) {
        vector = new PVector();
        applyMovement(deltaTime);
        applyUniversalWeakReplusion(allNodes, deltaTime);
        applyUniversalCenterAttraction(allNodes.get(0), deltaTime);
        float force = vector.mag();
        if( force > MAX_VECTOR_MAG ) {
            vector.normalize();
            vector.mult(MAX_VECTOR_MAG);
        }
        pos.add(vector);
    }
    
    
    // primary force functions
    
    public void applyMovement( float deltaTime ) {
        for( WebpageNode node : allConnections ) {
            applyAttraction( node, deltaTime ,FORCE_FACTOR, EQUILIBRIUM_DIST, false);
        }
        //pos.add(vector);
    }

    public void applyUniversalWeakReplusion( ArrayList<WebpageNode> allNodes, float deltaTime ) {
        for( WebpageNode node : allNodes ) {
            if( node != this ) {
                //applyAttraction( node, deltaTime, FORCE_FACTOR * 0.01, EQUILIBRIUM_DIST * 3 );
                applyReplusion( node, deltaTime, FORCE_FACTOR * 2000000 );
            }
        }
        //pos.add(vector);	
    }
    
    public void applyUniversalCenterAttraction( WebpageNode centerNode, float deltaTime ) {
        if( this != centerNode ) {
            applyAttraction( centerNode, deltaTime ,CENTRAL_FORCE_FACTOR, CENTRAL_EQUILIBRIUM_DIST, true);
        }
    }
    
    
    // secondary force functions
    
    void applyAttraction( WebpageNode other, float deltaTime, float force, float distance, boolean invert ) {
        if( unmoveable  ) { //|| distance < MIN_DIST_FOR_FORCE
            return;
        }
        PVector attract = new PVector( other.pos.x, other.pos.y );
        attract.sub( pos );
        PVector entropy = new PVector();
        entropy.set(attract);

        float actualForce = force * (attract.mag() - ((invert?1:-1)*distance));
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
    
    public void drawConnections(GraphicsContext context) {
        if( pos != null ) {
            // draw connections
            for( WebpageNode child : children ) {
                if( child.pos != null ) {
                    context.setStroke(Color.color(0.1, 0.1, 0.1, 0.1));
                    context.setLineWidth(1);
                    context.strokeLine(pos.x,pos.y,child.pos.x,child.pos.y);
                }
            }
        }
    }
    
    public void drawNode(GraphicsContext context) {
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
                if( !unmoveable && this.relevancy < MIN_RELEVANCY ) {
                    context.setFill(Color.GREY);
                }
                context.fillOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
                context.strokeOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
            } else {
                context.setStroke(Color.GREY);
                context.strokeOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
            }
            // level text
            /*
            if( visited ) {
                context.setFill(Color.GREY);
                context.fillText(""+level, pos.x + 5, pos.y);
            }
                    */
            // favicon
            if( favIconImage != null ) {
                //context.setFill(Color.WHITE);
                //context.fillRect(pos.x+(ovalSize/2)+2, pos.y-9, 18, 18);
                context.drawImage(favIconImage, pos.x+(ovalSize/2)+2, pos.y-8, 16, 16);
            }
        }
    }
}
