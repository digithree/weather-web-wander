/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander.genalgo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simonkenny
 * @param <T>
 */
public class GenAlgoRunner<T extends GenAlgoEntity> {
    
    // settings, with default values
    private int populationSize = 100;
    private double mutationRate = 0.01;
    private double targetFitness = 0.95;
    private int maxIterations = 200;
    
    // operating variables
    private List<T> population;
    private T overallMostFitEntity;
    private double overallBestFitness;
    private int iterations;
    private boolean finished;
    
    private final GenAlgoEntityFactory factory;
    
    public GenAlgoRunner(GenAlgoEntityFactory factory) {
        this.factory = factory;
        populationSize = 100;
        mutationRate = 0.01;
        targetFitness = 0.95;
        maxIterations = 200;
        prepare();
    }
    
    public GenAlgoRunner(GenAlgoEntityFactory factory, int populationSize, double mutationRate,
            double targetFitness, int maxIterations) {
        this.factory = factory;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.targetFitness = targetFitness;
        this.maxIterations = maxIterations;
        prepare();
    }
    
    public void reset() {
        prepare();
    }
    
    
    // setup
    
    private void prepare() {
        createNewPopulation();
        overallBestFitness = 0.f;
        overallMostFitEntity = null;
        iterations = 0;
        finished = false;
    }
    
    private void createNewPopulation() {
        population = new ArrayList<>();
        for( int i = 0 ; i < populationSize ; i++ ) {
            T t = (T)factory.createEntity();
            t.randomize();
            population.add(t);
        }
    }
    
    
    // processing
    
    public T processFrame() {
        if( finished ) {
            return overallMostFitEntity;
        }
        double bestFitness = 0.f;
        double []fitness = new double[populationSize];
        int idx = 0;
        for( T entity : population ) {
            double entityFitness = entity.calcFitness();
            fitness[idx++] = entityFitness;
            if( entityFitness > bestFitness ) {
                bestFitness = entityFitness;
                if( bestFitness > overallBestFitness ) {
                    overallBestFitness = bestFitness;
                    overallMostFitEntity = entity;
                }
            }
            if( entityFitness >= targetFitness
                    || iterations >= maxIterations ) {
                finished = true;
                return overallMostFitEntity;
            }
        }
        List<T> matingPool = new ArrayList<>();
        for( int i = 0 ; i < populationSize ; i++ ) {
            int numToSpawn = (int)(fitness[i] * 100);
            for( int j = 0 ; j < numToSpawn ; j++ ) {
                matingPool.add( population.get(i) );
            }
        }
        population = new ArrayList<>();
        if( !matingPool.isEmpty() ) {
            double matingPoolSize = matingPool.size();
            for( int i = 0 ; i < populationSize ; i++ ) {
                int idx1 = (int)(Math.random() * matingPoolSize);
                int idx2 = (int)(Math.random() * matingPoolSize);
                T parent1 = matingPool.get(idx1);
                T parent2 = matingPool.get(idx2);
                T child = (T)parent1.mate(parent2);
                child.mutate(mutationRate);
                population.add(child);
            }
        } else {
            createNewPopulation();
    	}
        iterations++;
        return overallMostFitEntity;
    }
    
    
    // access
    
    public int getNumIterations() {
        return iterations;
    }
    
    public T getOverallBestMostFitEntity() {
        return overallMostFitEntity;
    }
    
    public boolean isFinished() {
        return finished;
    }
}
