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

    //Validation
    public static final int VALID_NONE = 1, VALID_HOLD = 2, VALID_K_FOLD = 3;
    private int holdTrainDist = 80, kFolds = 5, kFoldTrainDist = 67;
    //Crossover
    public final int CROSS_REG = 1, CROSS_BLEND_GENE = 2,
            CROSS_BLEND_RULE = 3, CROSS_BLEND_CANON = 4;
    //Mutation
    public final int MUT_CREEP = 1, MUT_NORM_DIST = 2;
    private float mutCreepTol = (float) 0.1;
    //Gene Types
    private final int GENE_COND = 1, GENE_OUT = 2, GENE_TOL = 3;

    //Method Variations
    private int validationMethod = VALID_NONE,
            mutationMethod = MUT_CREEP,
            crossoverMethod = CROSS_REG;

    //Holdout an k-fold
    protected Rule[] tDataRuleSet, vHoldDataRuleSet, vKFoldDataRuleSet;
    protected Rule[][] kDataRuleSets;

    public FloatRuleMiner(Rule[] ruleBase) {
        super(ruleBase);
        initHoldoutRules();
        initKRules();
    }

    public FloatRuleMiner(int populationSize, int numberOfGenerations,
            Rule[] ruleBase, int nRules) {
        super(populationSize, numberOfGenerations, ruleBase, nRules);
        initHoldoutRules();
        initKRules();
    }

    public FloatRuleMiner(int populationSize, int numberOfGenerations,
            double probabilityOfMutation,
            Rule[] ruleBase, int nRules) {
        super(populationSize, numberOfGenerations, probabilityOfMutation,
                ruleBase, nRules);
        initHoldoutRules();
        initKRules();
    }
    
    public void runHoldout(int selectionType){
        //INIT populations
        this.population = new Individual[populationSize];
        this.offspring = new Individual[populationSize];
        
        //SET each individuals genes to be 1 or 0 at random
        initChromosomes();

        for (int g = 0; g < this.numberOfGenerations; g++) {
            this.population = calcFitness(this.population, this.tDataRuleSet);

            recordResults(g);

            this.offspring = crossover();

            this.offspring = mutate();

            this.offspring = calcFitness(this.offspring, this.tDataRuleSet);

            this.population = selection(selectionType);
        }
        
        this.population = calcFitness(this.population, this.vHoldDataRuleSet);
    }

    @Override
    protected void initChromosomes() {
        for (int i = 0; i < super.population.length; i++) {
            Object[] chrom = new Object[this.chromosomeSize];
            int idx, chromPos = 0;
            float[] intArray;

            for (int r = 0; r < super.nRules; r++) {

                //Condition genes
                for (int c = 0; c < this.conditionSize; c++) {
                    chrom[chromPos] = (float) round(new Random().nextFloat(), 6);
                    chromPos++;
                }

                //Output gene
                intArray = new float[]{(float) 1.0, (float) 0.0};
                idx = new Random().nextInt(intArray.length);
                chrom[chromPos] = (float) intArray[idx];
                chromPos++;
            }
            super.population[i] = new Individual(chrom);
        }
    }

    private void initHoldoutRules() {
        int idx = 0;
        int len = super.dataRules.length;
        int tlen = (len / 100) * holdTrainDist;
        int vlen = len - tlen;

        //SET Training set
        tDataRuleSet = new Rule[tlen];
        for (int r = 0; r < tlen; r++) {
            tDataRuleSet[r] = super.dataRules[idx++];
        }

        //SET Validation set
        vHoldDataRuleSet = new Rule[vlen];
        for (int r = 0; r < vlen; r++) {
            vHoldDataRuleSet[r] = super.dataRules[idx++];
        }
    }

    private void initKRules() {
        int len = super.dataRules.length;
        int tlen = (len / 100) * kFoldTrainDist;
        int vlen = len - tlen;
        double f = (double) tlen % kFolds;

        if (f == 0) {
            int foldSize = tlen / kFolds;
            int idx = 0, bound = foldSize, kIdx = 0, fIdx = 0;
            
            //SET Training/Testing sets
            kDataRuleSets = new Rule[kFolds][foldSize];
            for (int r = 0; r < tlen; r++) {
                if (r == bound) {
                    kIdx++;
                    fIdx = 0;
                    bound += foldSize;
                }
                kDataRuleSets[kIdx][fIdx++] = super.dataRules[idx++];
            }

            //SET Validation set
            vKFoldDataRuleSet = new Rule[vlen];
            for (int r = 0; r < vlen; r++) {
                vKFoldDataRuleSet[r] = super.dataRules[idx++];
            }
        } else {
            throw new IllegalArgumentException(
                    "Rule dataset of size "
                    + len
                    + " does not divide by k-fold value "
                    + this.kFolds);
        }
    }

    //START_CROSSOVER
    @Override
    protected Individual[] crossover() {
        switch (this.crossoverMethod) {
            case CROSS_REG:
                //Crossover canonically
                return crossoverRules();
            case CROSS_BLEND_GENE:
                //Blending single gene
                return crossoverBlendSingle();
            case CROSS_BLEND_RULE:
                //Blending single rule
                return crossoverBlendRules();
            case CROSS_BLEND_CANON:
                //Blending canonical cross
                return crossoverBlendCanon();
            default:
                System.err.println("Crossover method: "
                        + this.crossoverMethod + " not found");
                return null;
        }
    }

    private Individual[] crossoverRules() {
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

    private Individual[] crossoverBlendSingle() {
        ArrayList<Individual> children = new ArrayList<>();
        Object[][] crossoverGenes = new Object[2][super.chromosomeSize];
        final int child1 = 0, child2 = 1;
        int geneCounter = 0, ruleCounter = 0,
                ruleSize = this.conditionSize + 1, crossoverPoint;
        Object[] parent1, parent2;
        float blend;

        for (int i = 0; i < populationSize - 1; i++) {
            parent1 = population[i].getChromosome();
            parent2 = population[(i + 1)].getChromosome();

            crossoverPoint = new Random().nextInt(
                    (super.chromosomeSize / this.nRules) - 1) + 1;

            for (int g = 0; g < super.chromosomeSize; g++) {
                if (geneCounter == ruleSize) {
                    ruleCounter++;
                    geneCounter = 0;
                }

                //TODO change a single gene that is not an output
                //Perhaps blend more a variation 
                if (ruleCounter == crossoverPoint) {
                    blend = (float) ((Float) parent1[g] + (Float) parent2[g]) / 2;
                    crossoverGenes[child1][g] = blend;
                    crossoverGenes[child2][g] = blend;
                } else {
                    crossoverGenes[child1][g] = parent1[g];
                    crossoverGenes[child2][g] = parent2[g];
                }

                geneCounter++;
            }

            children.add(new Individual(crossoverGenes[child1]));
            children.add(new Individual(crossoverGenes[child2]));
        }

        Individual[] ret = new Individual[children.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = children.get(i);
        }
        return ret;
    }

    private Individual[] crossoverBlendRules() {
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

    private Individual[] crossoverBlendCanon() {
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
    //END_CROSSOVER

    //START_MUTATION
    @Override
    protected Object[] mutateChromosome(Object[] chrom) {
        switch (this.mutationMethod) {
            case MUT_CREEP:
                //Creep
                return mutateCreep(chrom);
            case MUT_NORM_DIST:
                //TODO Normal Distribution
                return null;
            default:
                System.err.println("Mutation method: "
                        + this.mutationMethod + " not found");
                return null;
        }
    }

    private Object[] mutateCreep(Object[] chrom) {
        Object[] mutatedGenes = chrom;
        int condBound = 0;
        int geneType;

        for (int i = 0; i < chrom.length; i++) {
            double m = Math.random();
            if (this.conditionSize == condBound) {
                geneType = GENE_OUT;
                condBound = 0;
            } else if ((i % 2) != 0) {
                geneType = GENE_TOL;
                condBound++;
            } else {
                geneType = GENE_COND;
                condBound++;
            }

            if (super.probabilityOfMutation >= m) {
                float mutChange;
                float upOrDown;

                if (geneType == GENE_COND || geneType == GENE_TOL) {
                    mutChange = new Random().nextFloat() * this.mutCreepTol;
                    upOrDown = new Random().nextFloat();

                    if (upOrDown >= 0.5) {
                        //Add mutChange
                        mutatedGenes[i] = round((float) mutatedGenes[i] + mutChange, 6);
                    } else {
                        //Minus mutChange
                        mutatedGenes[i] = round((float) mutatedGenes[i] - mutChange, 6);
                    }
                } else {
                    mutatedGenes[i] = flipBinaryFloat(mutatedGenes[i]);
                }
            }
        }

        return mutatedGenes;
    }
    //END_MUTATION

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
    
    protected Individual[] calcFitness(Individual[] pop, Rule[] ruleset) {
        Individual[] ret = new Individual[pop.length];

        for (int i = 0; i < pop.length; i++) {
            int fitness = 0;
            ArrayList<Rule> indivRuleBase = chromosomeToRules(pop[i].getChromosome());

            for (Rule rule : ruleset) {
                for (Rule indivRule : indivRuleBase) {
                    //IF condition matched
                    if (evaluateConditionMatch(indivRule, rule)) {
                        //IF output matches
                        if (indivRule.getOutput() == rule.getOutput()) {
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
    
    public int[] calcRuleFitness(ArrayList<Rule> indivRuleBase, Rule[] ruleset) {
        int ruleIdx;

        int[] ruleFitnesses = new int[indivRuleBase.size()];
        for (int i = 0; i < ruleFitnesses.length; i++) {
            ruleFitnesses[i] = 0;
        }

        for (Rule dataRule : ruleset) {
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

    @Override
    public ArrayList<Rule> chromosomeToRules(Object[] oGenes) {
        ArrayList<Rule> ret = new ArrayList<>();
        int k = 0;
        Float[] genes = new Float[oGenes.length];

        for (int g = 0; g < genes.length; g++) {
            genes[g] = (Float) oGenes[g];
        }

        for (int r = 0; r < this.nRules; r++) {
            float[] cond = new float[this.conditionSize];

            for (int c = 0; c < this.conditionSize; c++) {
                cond[c] = genes[k++];
            }
            ret.add(new Rule((float[]) cond, (int) genes[k++].floatValue(),
                    Rule.DATA_TYPE_FLOAT));
        }

        return ret;
    }

    @Override
    protected boolean evaluateConditionMatch(Rule indivRule, Rule datasetRule) {
        float[] indivCond = indivRule.getRealNumArr(),
                datasetCond = datasetRule.getRealNumArr();
        float value, tolerance;
        float min, max;
        int condIdx = 0;

        for (int c = 0; c < indivCond.length; c++) {
            value = indivCond[c];
            tolerance = indivCond[c + 1];
            min = value - tolerance;
            max = value + tolerance;

            if (!((datasetCond[condIdx] >= min)
                    && (datasetCond[condIdx] <= max))) {
                return false;
            }
            condIdx++;
            c++;
        }
        return true;
    }

    //START UTILS
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private float flipBinaryFloat(Object gene) {
        if ((Float) gene == (float) 1.0) {
            return (float) 0.0;
        } else {
            return (float) 1.0;
        }
    }
    //END_UTILS

    public int getHoldTrainDist() {
        return holdTrainDist;
    }

    public void setHoldTrainDist(int holdTrainDist) {
        this.holdTrainDist = holdTrainDist;
    }

    public int getkFolds() {
        return kFolds;
    }

    public void setkFolds(int kFolds) {
        this.kFolds = kFolds;
    }

    public float getMutTolerence() {
        return mutCreepTol;
    }

    public void setMutTolerence(float mutCreepTol) {
        this.mutCreepTol = mutCreepTol;
    }

    public int getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(int validationMethod) {
        this.validationMethod = validationMethod;
    }

    public int getMutationMethod() {
        return mutationMethod;
    }

    public void setMutationMethod(int mutationMethod) {
        this.mutationMethod = mutationMethod;
    }

    public int getCrossoverMethod() {
        return crossoverMethod;
    }

    public void setCrossoverMethod(int crossoverMethod) {
        this.crossoverMethod = crossoverMethod;
    }

    public Rule[] gettDataRuleSet() {
        return tDataRuleSet;
    }

    public void settDataRuleSet(Rule[] tDataRuleSet) {
        this.tDataRuleSet = tDataRuleSet;
    }

    public int getkFoldTrainDist() {
        return kFoldTrainDist;
    }

    public void setkFoldTrainDist(int kFoldTrainDist) {
        this.kFoldTrainDist = kFoldTrainDist;
    }

    public float getMutCreepTol() {
        return mutCreepTol;
    }

    public void setMutCreepTol(float mutCreepTol) {
        this.mutCreepTol = mutCreepTol;
    }

    public Rule[] getvHoldDataRuleSet() {
        return vHoldDataRuleSet;
    }

    public void setvHoldDataRuleSet(Rule[] vHoldDataRuleSet) {
        this.vHoldDataRuleSet = vHoldDataRuleSet;
    }

    public Rule[] getvKFoldDataRuleSet() {
        return vKFoldDataRuleSet;
    }

    public void setvKFoldDataRuleSet(Rule[] vKFoldDataRuleSet) {
        this.vKFoldDataRuleSet = vKFoldDataRuleSet;
    }

    public Rule[][] getkDataRuleSets() {
        return kDataRuleSets;
    }

    public void setkDataRuleSets(Rule[][] kDataRuleSets) {
        this.kDataRuleSets = kDataRuleSets;
    }
}
