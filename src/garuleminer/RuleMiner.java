/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

import geneticalgorithm.*;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author c2-newcombe
 */
public class RuleMiner extends GeneticAlgorithm {

    private final Rule[] dataRules;

    private final int nRules, conditionSize;

    public RuleMiner(int populationSize, int numberOfGenerations,
            Rule[] ruleBase, int nRules) {
        super.populationSize = populationSize;
        super.numberOfGenerations = numberOfGenerations;
        super.probabilityOfMutation = (float) 1 / super.populationSize;
        super.results = new float[super.numberOfGenerations][3];
        this.dataRules = ruleBase;
        this.nRules = nRules;
        this.conditionSize = calcConditionSize();
        super.chromosomeSize = calcChromSize();
    }

    public RuleMiner(int populationSize, int numberOfGenerations,
            double probabilityOfMutation,
            Rule[] ruleBase, int nRules) {
        super.populationSize = populationSize;
        super.numberOfGenerations = numberOfGenerations;
        super.chromosomeSize = chromosomeSize;
        super.probabilityOfMutation = probabilityOfMutation;
        super.results = new float[super.numberOfGenerations][3];
        this.dataRules = ruleBase;
        this.nRules = nRules;
        this.conditionSize = calcConditionSize();
        super.chromosomeSize = calcChromSize();
    }

    private int calcConditionSize() {
        if (this.dataRules != null && this.dataRules[0] != null) {
            if (this.dataRules[0].getCharArr() != null) {
                return this.dataRules[0].getCharArr().length;
            } else if (this.dataRules[0].getRealNumArr() != null) {
                return this.dataRules[0].getRealNumArr().length;
            }
        }
        return 0;
    }

    private int calcChromSize() {
        return (this.conditionSize + 1) * this.nRules;
    }

    @Override
    protected void initChromosomes() {
        //SET to random 1s and 0s by default
        for (int i = 0; i < super.population.length; i++) {
            Object[] chrom = new Object[this.chromosomeSize];
            int condBound = 0;
            int[] intArray;

            for (int j = 0; j < chrom.length; j++) {
                if (this.conditionSize == condBound++) {
                    intArray = new int[]{'1', '0'};
                    condBound = 0;
                } else {
                    intArray = new int[]{'1', '0', '#'};
                }

                int idx = new Random().nextInt(intArray.length);

                chrom[j] = (char) intArray[idx];
            }
            super.population[i] = new Individual(chrom);
        }
    }

    @Override
    protected Object[] mutateChromosome(Object[] chrom) {
        Object[] mutatedGenes = chrom;
        int condBound = 0;
        boolean outputGene = false;

        for (int i = 0; i < chrom.length; i++) {
            double m = Math.random();
            if (this.conditionSize == condBound) {
                outputGene = true;
                condBound = 0;
            }else{
                outputGene = false;
                condBound++;
            }
            if (super.probabilityOfMutation >= m) {
                int[] intArray;
                int idx;
                char gene = (Character) mutatedGenes[i];

                switch (gene) {
                    case '1':
                        if (outputGene) {
                            mutatedGenes[i] = '0';
                        } else {
                            intArray = new int[]{'0', '#'};
                            idx = new Random().nextInt(intArray.length);
                            mutatedGenes[i] = (char) intArray[idx];
                        }
                        break;
                    case '0':
                        if (outputGene) {
                            mutatedGenes[i] = '1';
                        } else {
                            intArray = new int[]{'1', '#'};
                            idx = new Random().nextInt(intArray.length);
                            mutatedGenes[i] = (char) intArray[idx];
                        }
                        break;
                    case '#':
                        intArray = new int[]{'0', '1'};
                        idx = new Random().nextInt(intArray.length);
                        mutatedGenes[i] = (char) intArray[idx];
                        break;
                }
            }
        }

        return mutatedGenes;
    }

    @Override
    protected Individual[] crossover() {
        return population;
    }
    
    @Override
    protected Individual[] calcFitness(Individual[] pop) {
        Individual[] ret = new Individual[pop.length];
        
        for (int i = 0; i < pop.length; i++) {
            ArrayList<Rule> indivRuleBase = new ArrayList<>();
            int fitness = 0, k = 0;

            Object[] oGenes = pop[i].getChromosome();
            Character[] genes = new Character[pop[i].getChromosome().length];
            for (int g = 0; g < genes.length; g++) {
                genes[g] = (Character) oGenes[g];
            }

            for (int r = 0; r < this.nRules; r++) {
                char[] cond = new char[this.conditionSize];

                for (int c = 0; c < this.conditionSize; c++) {
                    cond[c] = genes[k++];
                }
                indivRuleBase.add(new Rule((char[]) cond, 
                        Character.getNumericValue(genes[k++]), Rule.DATA_TYPE_BINARY));
            }

            for (Rule indivRule : indivRuleBase) {
                for (Rule dataRule : dataRules) {
                    //IF condition matched
                    if (evaluateConditionMatch(indivRule, dataRule)) {
                        //IF output matches
                        if (indivRule.getOutput() == dataRule.getOutput()) {
                            fitness++;
                        }
                        break;
                    }
                }
            }
            ret[i] = new Individual(genes, fitness);
        }
        return ret;
    }

    private boolean evaluateConditionMatch(Rule b, Rule d) {
        char[] cond1 = b.getCharArr(), cond2 = d.getCharArr();

        for (int c = 0; c < this.conditionSize; c++) {
            if (cond1[c] == '#' || cond2[c] == '#') {
                continue;
            }
            if (!(cond1[c] == cond2[c])) {
                return false;
            }
        }
        return true;
    }
}
