/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geneticalgorithm;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author c2-newcombe
 */
public class GeneticAlgorithm {

    //Selection Type
    public final static int SELECTION_TOURNEMENT = 1,
            SELECTION_ROULETTE = 2,
            SELECTION_TRUNCATION = 3;

    //Hyperparameters
    protected int populationSize,
            numberOfGenerations,
            chromosomeSize;
    protected double probabilityOfMutation;

    //Population
    protected Individual population[];
    protected Individual offspring[];

    //Results
    public final static int RESULT_BEST = 0,
            RESULT_WORST = 1,
            RESULT_RANGE = 2,
            RESULT_AVERAGE = 3,
            RESULT_SUM = 4,
            N_RESULT_SETS = 5;
    protected float[][] results;

    //START_CONSTRUCTORS
    public GeneticAlgorithm() {
    }

    public GeneticAlgorithm(int populationSize, int numberOfGenerations, int chromosomeSize) {
        this.populationSize = populationSize;
        this.numberOfGenerations = numberOfGenerations;
        this.chromosomeSize = chromosomeSize;
        this.probabilityOfMutation = (float) 1 / this.populationSize;
        this.results = new float[this.numberOfGenerations][N_RESULT_SETS];
    }

    public GeneticAlgorithm(int populationSize, int numberOfGenerations, int chromosomeSize, double probabilityOfMutation) {
        this.populationSize = populationSize;
        this.numberOfGenerations = numberOfGenerations;
        this.chromosomeSize = chromosomeSize;
        this.probabilityOfMutation = probabilityOfMutation;
        this.results = new float[this.numberOfGenerations][N_RESULT_SETS];
    }
    //END_CONSTRUCTORS

    public void run(int selectionType) {
        //INIT populations
        this.population = new Individual[populationSize];
        this.offspring = new Individual[populationSize];

        //SET each individuals genes to be 1 or 0 at random
        initChromosomes();

        for (int g = 0; g < this.numberOfGenerations; g++) {
            this.population = calcFitness(this.population);

            recordResults(g);

            this.offspring = crossover();

            this.offspring = mutate();

            this.offspring = calcFitness(this.offspring);

            this.population = selection(selectionType);
        }

        this.population = calcFitness(this.population);
    }

    protected void recordResults(int g) {
        this.results[g][RESULT_BEST] = bestFitness(this.population);
        this.results[g][RESULT_WORST] = worstFitness(this.population);
        this.results[g][RESULT_RANGE] = this.results[g][RESULT_BEST]
                - this.results[g][RESULT_WORST];
        this.results[g][RESULT_AVERAGE] = avgFitness(this.population);
        this.results[g][RESULT_SUM] = sumFitness(this.population);
    }

    protected void initChromosomes() {
        //SET to random 1s and 0s by default
        for (int i = 0; i < this.population.length; i++) {
            Object[] genes = new Object[this.chromosomeSize];

            for (int j = 0; j < genes.length; j++) {
                genes[j] = (int) ((Math.random() * 2) % 2);
            }
            this.population[i] = new Individual(genes);
        }
    }

    //START_Selection
    protected Individual[] selection(int selectionType) {
        switch (selectionType) {
            case SELECTION_TOURNEMENT:
                return tournementSelection();
            case SELECTION_ROULETTE:
                return rouletteWheelSelection();
            case SELECTION_TRUNCATION:
                return truncationSelection();
            default:
                System.err.println("Selection type not found: " + selectionType);
                return null;
        }
    }

    protected Individual[] truncationSelection() {
        Individual[] nextGen = new Individual[populationSize];

        //Put parents and children into a single population
        ArrayList<Individual> currentGen = new ArrayList<>();
        currentGen.addAll(Arrays.asList(this.population));
        currentGen.addAll(Arrays.asList(this.offspring));

        Individual bestIndiv;
        for (int i = 0; i < nextGen.length; i++) {
            bestIndiv = bestIndividual(currentGen);
            nextGen[i] = bestIndiv;
            currentGen.remove(bestIndiv);
        }

        return nextGen;
    }

    protected Individual[] tournementSelection() {
        if (offspring.length > 0) {
            Individual[] nextGen = new Individual[populationSize];

            //Put parents and children into a single population
            ArrayList<Individual> currentGen = new ArrayList<>();
            currentGen.addAll(Arrays.asList(this.population));
            currentGen.addAll(Arrays.asList(this.offspring));
            
            //Chosen Individuals
            int indiv1Idx, indiv2Idx;
            Individual indiv1, indiv2;
            for (int i = 0; i < populationSize; i++) {
                indiv1Idx = (int) ((Math.random() * currentGen.size())
                        % currentGen.size());
                indiv2Idx = (int) ((Math.random() * currentGen.size())
                        % currentGen.size());
                
                indiv1 = currentGen.get(indiv1Idx);
                indiv2 = currentGen.get(indiv2Idx);  
                if (indiv1.getFitness() >= indiv2.getFitness()) {
                    nextGen[i] = indiv1;
                    currentGen.remove(indiv1Idx);
                } else {
                    nextGen[i] = indiv2;
                    currentGen.remove(indiv2Idx);
                }
            }

            return nextGen;
        } else {
            return population;
        }
    }

    protected Individual[] rouletteWheelSelection() {
        Individual[] nextGen = new Individual[populationSize];

        //Put parents and children into a single population
        ArrayList<Individual> currentGen = new ArrayList<>();
        currentGen.addAll(Arrays.asList(this.population));
        currentGen.addAll(Arrays.asList(this.offspring));

        int totalFitness = sumFitness(this.population) + sumFitness(this.offspring);
        int runningTotal = 0, j = 0, currFitness = 0, selectionPoint;
        for (int i = 0; i < populationSize; i++) {

            selectionPoint = (int) ((Math.random() * totalFitness) % totalFitness) + 1;

            while (runningTotal < selectionPoint) {
                currFitness = currentGen.get(j).getFitness();
                runningTotal += currFitness;
                j++;
            }

            nextGen[i] = currentGen.get(j - 1);

            //To maintain diversity
            currentGen.remove(j - 1);
            totalFitness -= currFitness;

            j = 0;
            runningTotal = 0;
        }

        return nextGen;
    }
    //END_Selection

    //START_Crossover
    protected Individual[] crossover() {
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

    protected ArrayList<Individual> singlePointCrossover(Object[] parent1, Object[] parent2) {
        ArrayList<Individual> children = new ArrayList<>();
        Object[][] crossoverGenes = new Object[2][chromosomeSize];
        int child1 = 0, child2 = 1;
        int crossoverPoint = (int) (Math.random() * chromosomeSize) - 1;

        for (int i = 0; i < chromosomeSize; i++) {
            if (i < crossoverPoint) {
                crossoverGenes[child1][i] = parent1[i];
                crossoverGenes[child2][i] = parent2[i];
            } else {
                crossoverGenes[child1][i] = parent2[i];
                crossoverGenes[child2][i] = parent1[i];
            }
        }

        children.add(new Individual(crossoverGenes[child1]));
        children.add(new Individual(crossoverGenes[child2]));

        return children;
    }
    //END_Crossover

    //START_Mutation
    protected Individual[] mutate() {
        for (Individual child : offspring) {
            child.setChromosome(mutateChromosome(child.getChromosome()));
        }
        return offspring;
    }

    protected float calcRandMutationRate() {
        float min = (float) 1 / populationSize;
        float max = (float) 1 / chromosomeSize;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "Population size must be greater than chromosome size");
        }
        return (float) (Math.random() * ((max - min) + 1)) + min;
    }

    protected Object[] mutateChromosome(Object[] chrom) {
        Object[] mutatedGenes = chrom;

        for (int i = 0; i < chromosomeSize; i++) {
            double m = Math.random();
            if (probabilityOfMutation >= m) {
                mutatedGenes[i] = flipBinaryInt(chrom[i]);
            }
        }

        return mutatedGenes;
    }

    private int flipBinaryInt(Object gene) {
        if ((int) gene == 1) {
            return 0;
        } else {
            return 1;
        }
    }
    //END_Mutation

    //START_Fitness
    protected Individual[] calcFitness(Individual[] pop) {
        return null;
    }

    protected int avgFitness(Individual[] pop) {
        return sumFitness(pop) / pop.length;
    }

    protected int bestFitness(Individual[] pop) {
        int ret = 0;
        for (Individual i : pop) {
            if (i.getFitness() > ret) {
                ret = i.getFitness();
            }
        }
        return ret;
    }

    protected Individual bestIndividual(ArrayList<Individual> pop) {
        Individual ret = new Individual();
        for (Individual i : pop) {
            if (i.getFitness() > ret.getFitness()) {
                ret = i;
            }
        }
        return ret;
    }

    protected int worstFitness(Individual[] pop) {
        int ret = bestFitness(pop);
        for (Individual i : pop) {
            if (i.getFitness() < ret) {
                ret = i.getFitness();
            }
        }
        return ret;
    }

    protected int sumFitness(Individual[] pop) {
        int ret = 0;
        for (Individual i : pop) {
            ret += i.getFitness();
        }
        return ret;
    }
    //END_Fitness

    //START_Utils
    private double calcAvg(double[] arr) {
        if (arr.length == 0) {
            return 0;
        }

        double ret = 0;

        for (int i = 0; i < arr.length; i++) {
            ret += arr[i];
        }
        return ret / arr.length;
    }

    private double calcPerc(double a, double b) {
        return (100 / b) * a;
    }
    //END_Utils

    //START_GETTERS_AND_SETTERS
    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getNumberOfGenerations() {
        return numberOfGenerations;
    }

    public int getChromosomeSize() {
        return chromosomeSize;
    }

    public void setChromosomeSize(int chromosomeSize) {
        this.chromosomeSize = chromosomeSize;
    }

    public double getProbabilityOfMutation() {
        return probabilityOfMutation;
    }

    public void setProbabilityOfMutation(float probabilityOfMutation) {
        this.probabilityOfMutation = probabilityOfMutation;
    }

    public Individual[] getPopulation() {
        return population;
    }

    public Individual getBestIndividual() {
        Individual ret = new Individual();
        for (Individual i : population) {
            if (i.getFitness() > ret.getFitness()) {
                ret = i;
            }
        }
        return ret;
    }

    public Individual[] getOffspring() {
        return offspring;
    }

    public float[][] getResults() {
        return results;
    }

    public float getResult(int generation, int resultType) {
        return results[generation - 1][resultType];
    }

    public void setNumberOfGenerations(int numberOfGenerations) {
        this.numberOfGenerations = numberOfGenerations;
        this.results = new float[this.numberOfGenerations][N_RESULT_SETS];
    }

    public void setProbabilityOfMutation(double probabilityOfMutation) {
        this.probabilityOfMutation = probabilityOfMutation;
    }
    //END_GETTERS_AND_SETTERS

}
