/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander.genalgo;

/**
 *
 * @author simonkenny
 */
public interface GenAlgoEntity {
    
    // TODO : create genes storage
    
    public void randomize();
    public GenAlgoEntity mate(GenAlgoEntity partner);
    public void mutate(double mutationRate);
    public double calcFitness();
}
