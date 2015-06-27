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
public class GraphNode {
    
    public final float FORCE_FACTOR = 0.005f;
    public final float ENTROPY_FACTOR = 0.0f;
    public final float EQUILIBRIUM_DIST = 5.f;
    public final float MAX_VEL = 200.f;
    
    public final int NODE_VIZ_SIZE = 5;
    
    ArrayList<GraphNode> children = new ArrayList<GraphNode>();
    ArrayList<GraphNode> parents = new ArrayList<GraphNode>();
    ArrayList<GraphNode> allConnections = new ArrayList<GraphNode>();
    PVector pos = new PVector();
    PVector vector = new PVector();
    boolean unmoveable = false;
    float mass = 0.1f;
    
    int size = 1;
    public int hashCode = 0;
    
    public GraphNode(int size) {
        this.size = size;
        mass = (1.f / (float)size) * 0.1f;
    }
    
    public void addChild( GraphNode child ) {
        if( !children.contains(child) ) {
            System.out.println("["+hashCode+"] Added child ("+child.hashCode+")");
            children.add(child);
            if( !allConnections.contains(child) ) {
                allConnections.add(child);
            }
            child.addParent(this);
        }
    }

    public void addParent( GraphNode parent ) {
        if( !parents.contains(parent) ) {
            System.out.println("["+hashCode+"] Added parent ("+parent.hashCode+")");
            parents.add(parent);
            if( !allConnections.contains(parent) ) {
                allConnections.add(parent);
            }
        }
    }

    public GraphNode[] getChildren() {
        GraphNode []dummyList = new GraphNode[children.size()];
        return children.toArray(dummyList);
    }

    public void applyMovement( float deltaTime ) {
        vector = new PVector();
        for( GraphNode node : allConnections ) {
            applyAttraction( node, deltaTime ,FORCE_FACTOR, EQUILIBRIUM_DIST);
        }
        pos.add(vector);
    }

    public void applyUniversalWeakReplusion( ArrayList<GraphNode> allNodes, float deltaTime ) {
        for( GraphNode node : allNodes ) {
            if( node != this ) {
                //applyAttraction( node, deltaTime, FORCE_FACTOR * 0.01, EQUILIBRIUM_DIST * 3 );
                applyReplusion( node, deltaTime, FORCE_FACTOR * 1000000 );
            }
        }
        pos.add(vector);	
    }

    void applyAttraction( GraphNode other, float deltaTime, float force, float distance ) {
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

    void applyReplusion( GraphNode other, float deltaTime, float force ) {
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
            context.setFill(Color.color(0.8, 0.8, 0.8));
            context.setStroke(Color.BLACK);
            context.setLineWidth(1);
            //ellipse(pos.x,pos.y,NODE_VIZ_SIZE*freq,NODE_VIZ_SIZE*freq);
            float ovalSize = NODE_VIZ_SIZE*size;
            context.strokeOval(pos.x-(ovalSize/2.f),pos.y-(ovalSize/2.f),ovalSize,ovalSize);
            // draw connections
            for( GraphNode child : children ) {
                if( child.pos != null ) {
                    context.setStroke(Color.color(0, 0, 0, 0.2));
                    context.setLineWidth(1);
                    context.strokeLine(pos.x,pos.y,child.pos.x,child.pos.y);
                }
            }
        }
    }
}
