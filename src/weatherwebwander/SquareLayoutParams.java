/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

/**
 *
 * @author simonkenny
 */
public class SquareLayoutParams {
    
    public double USED_SPACE_FITNESS_WEIGHT = 0.1;
    public double OVERLAP_FITNESS_WEIGHT = 0.6;
    public double OUTSIDE_OVERLAP_FITNESS_WEIGHT = 0.3;
    
    public float containerSize;
    public int numSquares;
    public int []squareSizes;
    public int []squareSizesRoot;
    public float []squareSizesRootHalf;
    public float []squareSizesRootNorm;
}
