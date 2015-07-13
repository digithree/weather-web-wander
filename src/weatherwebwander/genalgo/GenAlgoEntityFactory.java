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
public class GenAlgoEntityFactory {
    public GenAlgoEntity createEntity() {
        return new GenAlgoEntity() {

            @Override
            public void randomize() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public GenAlgoEntity mate(GenAlgoEntity partner) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mutate(double mutationRate) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public double calcFitness() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
