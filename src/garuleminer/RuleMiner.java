/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

import geneticalgorithm.*;

/**
 *
 * @author c2-newcombe
 */
public class RuleMiner extends GeneticAlgorithm {
    
    public RuleMiner(int populationSize, int numberOfGenerations, int chromosomeSize) {
        super.populationSize = populationSize;
        super.numberOfGenerations = numberOfGenerations;
        super.chromosomeSize = chromosomeSize;
        super.probabilityOfMutation = (float) 1 / super.populationSize;
        super.results = new float[super.numberOfGenerations][3];
    }

    public RuleMiner(int populationSize, int numberOfGenerations, int chromosomeSize, double probabilityOfMutation) {
        super.populationSize = populationSize;
        super.numberOfGenerations = numberOfGenerations;
        super.chromosomeSize = chromosomeSize;
        super.probabilityOfMutation = probabilityOfMutation;
        super.results = new float[super.numberOfGenerations][3];
    }

    @Override
    public Individual[] calcFitness(Individual[] pop) {
        Individual[] newPop = new Individual[pop.length];

        for (int i = 0; i < pop.length; i++) {
            int fitness = 0;
            Object[] genes = pop[i].getChromosome();
            for (int j = 0; j < genes.length; j++) {
                if ((int) genes[j] == 1) {
                    fitness++;
                }
            }
            newPop[i] = new Individual(genes, fitness);
        }
        return newPop;
    }
}
