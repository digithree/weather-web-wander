/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

import weatherwebwander.genalgo.GenAlgoEntity;
import weatherwebwander.genalgo.GenAlgoEntityFactory;

/**
 *
 * @author simonkenny
 */
public class SquareLayoutEntityFactory extends GenAlgoEntityFactory {
    
    private final SquareLayoutParams params;
    
    public SquareLayoutEntityFactory(SquareLayoutParams params) {
        this.params = params;
    }
    
    @Override
    public GenAlgoEntity createEntity() {
        return (GenAlgoEntity)new SquareLayoutEntity(params);
    }
}
