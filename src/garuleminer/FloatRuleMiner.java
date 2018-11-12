/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

import geneticalgorithm.Individual;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Conor
 */
public class FloatRuleMiner extends RuleMiner {
    
    //Holdout
    protected Rule[] tDataRuleSet, vDataRuleSet;
    //k-fold
    protected Rule[][] kDataRuleSets;
    
    public FloatRuleMiner(Rule[] ruleBase) {
        super(ruleBase);
    }

    public FloatRuleMiner(int populationSize, int numberOfGenerations,
            Rule[] ruleBase, int nRules) {
        super(populationSize, numberOfGenerations, ruleBase, nRules);
    }

    public FloatRuleMiner(int populationSize, int numberOfGenerations,
            double probabilityOfMutation,
            Rule[] ruleBase, int nRules) {
        super(populationSize, numberOfGenerations, probabilityOfMutation,
                ruleBase, nRules);
    }

    @Override
    protected void initChromosomes() {
        for (int i = 0; i < super.population.length; i++) {
            Object[] chrom = new Object[this.chromosomeSize];
            int condBound = 0, idx;
            float[] intArray;

            for (int j = 0; j < chrom.length; j++) {
                if (this.conditionSize == condBound++) {
                    intArray = new float[]{(float) 1.0, (float) 0.0};
                    condBound = 0;
                    idx = new Random().nextInt(intArray.length);
                    chrom[j] = (float) intArray[idx];
                } else {
                    chrom[j] = (float) round(new Random().nextFloat(), 6);
                }
            }
            super.population[i] = new Individual(chrom);
        }
    }

    @Override
    protected Individual[] crossover() {
        //TODO Blending
        
        ArrayList<Individual> children = new ArrayList<>();

        for (int i = 0; i < populationSize - 1; i++) {
            children.addAll(singlePointCrossover(
                    population[i].getChromosome(),
                    population[(i + 1)].getChromosome()));
        }

        Individual[] ret = new Individual[children.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = children.get(i);
        }
        return ret;
    }
    
    @Override
    protected Object[] mutateChromosome(Object[] chrom) {
        Object[] mutatedGenes = chrom;
        int condBound = 0;
        boolean outputGene;

        //TODO Creep
        //TODO Normal Distribution
        
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

    @Override
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

    @Override
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

    //START UTILS
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    //END_UTILS
}
