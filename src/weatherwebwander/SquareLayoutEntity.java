/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

import java.util.Arrays;
import weatherwebwander.genalgo.GenAlgoEntity;

/**
 *
 * @author simonkenny
 */
public class SquareLayoutEntity implements GenAlgoEntity {
    
    private final float SCALE_MIN = 0.1f;
    private final float SCALE_MAX = 100;
    
    private final SquareLayoutParams params;
    
    // genes
    private PVector []squareCenterPos;
    private float scaleFactor;
    
    public SquareLayoutEntity(SquareLayoutParams params) {
        this.params = params;
        clear();
    }
    
    private void clear() {
        squareCenterPos = new PVector[params.numSquares];
        Arrays.fill(squareCenterPos, new PVector());
        scaleFactor = 0.f;
    }
    
    // accessors
    public PVector getSquareCenterPos(int idx) {
        if( idx >= 0 && idx < params.numSquares ) {
            return squareCenterPos[idx];
        }
        return null;
    }

    public void setSquareCenterPos(int idx, PVector pos) {
        if( idx >= 0 && idx < params.numSquares ) {
            squareCenterPos[idx] = new PVector(pos.x, pos.y);
        }
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }
    
   
    
    // GA

    @Override
    public void randomize() {
        scaleFactor = (float)(Math.random() * (SCALE_MAX-SCALE_MIN)) + SCALE_MIN;
        for( int i = 0 ; i < params.numSquares ; i++ ) {
            if( (params.squareSizesRoot[i]*scaleFactor) > params.containerSize ) {
                scaleFactor /= 4;
                break;
            }
        }
        for( int i = 0 ; i < params.numSquares ; i++ ) {
            squareCenterPos[i] = new PVector(
                    (float)(Math.random() 
                            * (params.containerSize - ((float)params.squareSizesRoot[i]*scaleFactor)))
                            + (params.squareSizesRootHalf[i]*scaleFactor),
                    (float)(Math.random() 
                            * (params.containerSize - ((float)params.squareSizesRoot[i]*scaleFactor))) 
                            + (params.squareSizesRootHalf[i]*scaleFactor)
            );
        }
    }

    @Override
    public GenAlgoEntity mate(GenAlgoEntity partner) {
        SquareLayoutEntity child = new SquareLayoutEntity(params);
        int midPoint = (int)(Math.random() * (float)params.numSquares);
        for( int i = 0 ; i < midPoint ; i++ ) {
                child.setSquareCenterPos(i, squareCenterPos[i]);
        }
        for( int i = midPoint ; i < params.numSquares ; i++ ) {
                child.setSquareCenterPos(i, ((SquareLayoutEntity)partner).getSquareCenterPos(i));
        }
        if( Math.random() < 0.5 ) {
                child.setScaleFactor(scaleFactor);
        } else {
                child.setScaleFactor(((SquareLayoutEntity)partner).getScaleFactor());
        }
        return child;
    }

    @Override
    public void mutate(double mutationRate) {
        if( Math.random() < mutationRate ) {
            scaleFactor = (float)(Math.random() * (SCALE_MAX-SCALE_MIN)) + SCALE_MIN;
            for( int i = 0 ; i < params.numSquares ; i++ ) {
                if( (params.squareSizesRoot[i]*scaleFactor) > params.containerSize ) {
                    scaleFactor /= 4;
                    break;
                }
            }
        }
        for( int i = 0 ; i < params.numSquares ; i++ ) {
            if( Math.random() < mutationRate ) {
                squareCenterPos[i] = new PVector(
                        (float)(Math.random() 
                                * (params.containerSize - ((float)params.squareSizesRoot[i]*scaleFactor)))
                                + (params.squareSizesRootHalf[i]*scaleFactor),
                        (float)(Math.random() 
                                * (params.containerSize - ((float)params.squareSizesRoot[i]*scaleFactor))) 
                                + (params.squareSizesRootHalf[i]*scaleFactor)
                );
            }
        }
    }

    @Override
    public double calcFitness() {
        // setup
        double containerSizeSq = params.containerSize * params.containerSize;
        // overlap with outside
        double outsideOverlapArea = 0;
        for( int i = 0 ; i < params.numSquares ; i++ ) {
            outsideOverlapArea += outsideIntersection(i);
        }
        // used space area
        double totalUsedSpaceArea = 0;
        double []usedSpaceArea = new double[params.numSquares];
        for( int i = 0 ; i < params.numSquares ; i++ ) {
            usedSpaceArea[i] = (params.squareSizesRoot[i] * scaleFactor) 
                    * (params.squareSizesRoot[i] * scaleFactor);
            totalUsedSpaceArea += usedSpaceArea[i];
        }
        // overlap area
        double overlapArea = 0;
        for( int j = 0 ; j < params.numSquares ; j++ ) {
            for( int i = j+1 ; i < params.numSquares ; i++ ) {
                overlapArea += intersection(j, i);
            }
        }
        
        // FITNESS
        // outside overlap fitness
        double outsideOverlap = outsideOverlapArea / containerSizeSq;
        double outsideOverlapFitness = 1 - outsideOverlap;
        if( outsideOverlapFitness < 0 ) {
            outsideOverlapFitness = 0;
        } else if( outsideOverlapFitness > (containerSizeSq*2) ) {
            outsideOverlapFitness = (containerSizeSq*2);
        }
        // overlap fitness
        //if( overlapArea > containerSizeSq ) {
        //	overlapArea = containerSizeSq;
        //}
        double overlap = overlapArea / (totalUsedSpaceArea - overlapArea);
        double overlapFitness = 1 - overlap;
        // used space fitness
        totalUsedSpaceArea -= overlapArea;
        if( totalUsedSpaceArea > containerSizeSq ) {
            totalUsedSpaceArea = containerSizeSq;
        }
        double usedSpaceFitness = totalUsedSpaceArea / containerSizeSq;
        // combine fitness
        //fitness = usedSpaceFitness - overlap;
        //fitness = overlapFitness - (1 - usedSpaceFitness);
        double fitness = (usedSpaceFitness * params.USED_SPACE_FITNESS_WEIGHT)
                + (overlapFitness * params.OVERLAP_FITNESS_WEIGHT)
                + (outsideOverlapFitness * params.OUTSIDE_OVERLAP_FITNESS_WEIGHT)
                ;
        if( fitness < 0 ) {
            fitness = 0;
        }
        return fitness;
    }
    
    
    // physical space calculations
    
    private double intersection(int idx1, int idx2) {
        double newX = Math.max(
        	squareCenterPos[idx1].x - ((float)params.squareSizesRootHalf[idx1]*scaleFactor), 
        	squareCenterPos[idx2].x - ((float)params.squareSizesRootHalf[idx2]*scaleFactor)
        	);
        double newY = Math.max(
        	squareCenterPos[idx1].y - ((float)params.squareSizesRootHalf[idx1]*scaleFactor), 
        	squareCenterPos[idx2].y - ((float)params.squareSizesRootHalf[idx2]*scaleFactor)
        	);

        double newWidth = Math.min(
        	squareCenterPos[idx1].x + ((float)params.squareSizesRootHalf[idx1]*scaleFactor),
        	squareCenterPos[idx2].x + ((float)params.squareSizesRootHalf[idx2]*scaleFactor)
        	) - newX;
        double newHeight = Math.min(
        	squareCenterPos[idx1].y + ((float)params.squareSizesRootHalf[idx1]*scaleFactor),
        	squareCenterPos[idx2].y + ((float)params.squareSizesRootHalf[idx2]*scaleFactor)
        	) - newY;

        if (newWidth <= 0d || newHeight <= 0d) return 0d;

        return newWidth * newHeight;
    }

    // TODO : better way of doing with would be to use a large square over the whole container
    //          and then logical NOT intersect with the container itself
    private double outsideIntersection(int idx) {
    	double area = 0;
    	//top
    	{
            double newX = Math.max(
                    squareCenterPos[idx].x - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    0
                    );
            double newY = Math.max(
                    squareCenterPos[idx].y - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    -params.containerSize
                    );

            double newWidth = Math.min(
                    squareCenterPos[idx].x + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    params.containerSize
                    ) - newX;
            double newHeight = Math.min(
                    squareCenterPos[idx].y + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    0
                    ) - newY;
            if (!(newWidth <= 0d || newHeight <= 0d)) {
                area += newWidth * newHeight;
            }
    	}
    	// bottom
    	{
            double newX = Math.max(
                    squareCenterPos[idx].x - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    0
                    );
            double newY = Math.max(
                    squareCenterPos[idx].y - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    params.containerSize
                    );

            double newWidth = Math.min(
                    squareCenterPos[idx].x + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    params.containerSize
                    ) - newX;
            double newHeight = Math.min(
                    squareCenterPos[idx].y + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    params.containerSize*2
                    ) - newY;
            if (!(newWidth <= 0d || newHeight <= 0d)) {
                area += newWidth * newHeight;
            }
    	}
    	// left
    	{
            double newX = Math.max(
                    squareCenterPos[idx].x - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    -params.containerSize
                    );
            double newY = Math.max(
                    squareCenterPos[idx].y - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    0
                    );

            double newWidth = Math.min(
                    squareCenterPos[idx].x + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    0
                    ) - newX;
            double newHeight = Math.min(
                    squareCenterPos[idx].y + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    params.containerSize
                    ) - newY;
            if (!(newWidth <= 0d || newHeight <= 0d)) {
                area += newWidth * newHeight;
            }
    	}
    	// right
    	{
            double newX = Math.max(
                    squareCenterPos[idx].x - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    params.containerSize
                    );
            double newY = Math.max(
                    squareCenterPos[idx].y - ((float)params.squareSizesRootHalf[idx]*scaleFactor), 
                    0
                    );

            double newWidth = Math.min(
                    squareCenterPos[idx].x + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    params.containerSize*2
                    ) - newX;
            double newHeight = Math.min(
                    squareCenterPos[idx].y + ((float)params.squareSizesRootHalf[idx]*scaleFactor),
                    params.containerSize
                    ) - newY;
            if (!(newWidth <= 0d || newHeight <= 0d)) {
                area += newWidth * newHeight;
            }
    	}
    	return area;
    }
}
