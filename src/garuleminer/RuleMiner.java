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

    protected Rule[] dataRules;

    protected int nRules, conditionSize;

    public RuleMiner(Rule[] ruleBase) {
        this.dataRules = ruleBase;
    }

    public RuleMiner(int populationSize, int numberOfGenerations,
            Rule[] ruleBase, int nRules) {
        super.populationSize = populationSize;
        super.numberOfGenerations = numberOfGenerations;
        super.probabilityOfMutation = (float) 1 / super.populationSize;
        super.results = new float[super.numberOfGenerations][N_RESULT_SETS];
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
        super.results = new float[super.numberOfGenerations][N_RESULT_SETS];
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
                return this.dataRules[0].getRealNumArr().length * 2;
            }
        }
        return 0;
    }

    private int calcChromSize() {
        if (this.dataRules != null && this.dataRules[0] != null) {
            if (this.dataRules[0].getCharArr() != null) {
                return (this.conditionSize + 1) * this.nRules;
            } else if (this.dataRules[0].getRealNumArr() != null) {
                return (this.conditionSize + 1) * this.nRules;
            }
        }
        return 0;
        
    }

    @Override
    protected void initChromosomes() {
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
        boolean outputGene;

        for (int i = 0; i < chrom.length; i++) {
            double m = Math.random();
            if (this.conditionSize == condBound) {
                outputGene = true;
                condBound = 0;
            } else {
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
                    default:
                        intArray = new int[]{'0', '1', '#'};
                        idx = new Random().nextInt(intArray.length);
                        mutatedGenes[i] = (char) intArray[idx];
                        break;
                }
            }
        }

        return mutatedGenes;
    }

    @Override
    protected ArrayList<Individual> singlePointCrossover(Object[] parent1, Object[] parent2) {
        ArrayList<Individual> children = new ArrayList<>();
        Object[][] crossoverGenes = new Object[2][super.chromosomeSize];
        final int child1 = 0, child2 = 1;
        int geneCounter = 0, ruleCounter = 0, ruleSize = this.conditionSize + 1;
        int crossoverPoint = new Random().nextInt(
                (super.chromosomeSize / this.nRules) - 1) + 1;

        for (int i = 0; i < super.chromosomeSize; i++) {
            if (geneCounter == ruleSize) {
                ruleCounter++;
                geneCounter = 0;
            }

            if (ruleCounter < crossoverPoint) {
                crossoverGenes[child1][i] = parent1[i];
                crossoverGenes[child2][i] = parent2[i];
            } else {
                crossoverGenes[child1][i] = parent2[i];
                crossoverGenes[child2][i] = parent1[i];
            }

            geneCounter++;
        }

        children.add(new Individual(crossoverGenes[child1]));
        children.add(new Individual(crossoverGenes[child2]));

        return children;
    }

    @Override
    protected Individual[] calcFitness(Individual[] pop) {
        Individual[] ret = new Individual[pop.length];

        for (int i = 0; i < pop.length; i++) {
            int fitness = 0;
            ArrayList<Rule> indivRuleBase = chromosomeToRules(pop[i].getChromosome());

            for (Rule dataRule : dataRules) {
                for (Rule indivRule : indivRuleBase) {
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
            ret[i] = new Individual(pop[i].getChromosome(), fitness);
        }
        return ret;
    }

    public int[] calcRuleFitness(ArrayList<Rule> indivRuleBase) {
        int ruleIdx;
        
        int[] ruleFitnesses = new int[indivRuleBase.size()];
        for (int i = 0; i < ruleFitnesses.length; i++) {
            ruleFitnesses[i] = 0;
        }

        for (Rule dataRule : dataRules) {
            ruleIdx = 0;
            for (Rule indivRule : indivRuleBase) {
                //IF condition matches
                if (evaluateConditionMatch(indivRule, dataRule)) {
                    //IF output matches
                    if (indivRule.getOutput() == dataRule.getOutput()) {
                        ruleFitnesses[ruleIdx]++;
                    }
                    break;
                }
                ruleIdx++;
            }
        }
        return ruleFitnesses;
    }

    public int countFitRules(ArrayList<Rule> indivRuleBase) {
        int nFitRules = 0;
        
        int[] ruleFitnesses = calcRuleFitness(indivRuleBase);
        
        for(int fitness : ruleFitnesses){
            if(fitness > 0){
                nFitRules++;
            }
        }
        return nFitRules;
    }
    
    public ArrayList<Rule> chromosomeToRules(Object[] oGenes) {
        ArrayList<Rule> ret = new ArrayList<>();
        int k = 0;
        Character[] genes = new Character[oGenes.length];

        for (int g = 0; g < genes.length; g++) {
            genes[g] = (Character) oGenes[g];
        }

        for (int r = 0; r < this.nRules; r++) {
            char[] cond = new char[this.conditionSize];

            for (int c = 0; c < this.conditionSize; c++) {
                cond[c] = genes[k++];
            }
            ret.add(new Rule((char[]) cond,
                    Character.getNumericValue(genes[k++]), Rule.DATA_TYPE_BINARY));
        }

        return ret;
    }

    protected boolean evaluateConditionMatch(Rule b, Rule d) {
        char[] cond1 = b.getCharArr(), cond2 = d.getCharArr();

        for (int c = 0; c < cond1.length; c++) {
            if (cond1[c] == '#' || cond2[c] == '#') {
                continue;
            }
            if (!(cond1[c] == cond2[c])) {
                return false;
            }
        }
        return true;
    }

    public void setNRules(int nRules) {
        this.nRules = nRules;
        this.conditionSize = calcConditionSize();
        super.chromosomeSize = calcChromSize();
    }

    public int getConditionSize() {
        return conditionSize;
    }

}
