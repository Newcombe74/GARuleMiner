/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

import static garuleminer.Rule.DATA_TYPE_BINARY;
import static garuleminer.Rule.DATA_TYPE_FLOAT;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import geneticalgorithm.*;
import java.io.PrintWriter;

/**
 *
 * @author c2-newcombe
 */
public class GARuleMiner {

    //Hyperparameters
    private static final int POP_SIZE_MIN = 100,
            POP_SIZE_MAX = 1000,
            POP_SIZE_RES_STEP = 10,
            N_GENS_MIN = 1,
            N_GENS_MAX = 200,
            N_GENS_RES_STEP = 1,
            N_RUNS = 5,
            N_RULES_MIN = 1,
            N_RULES_MAX = 100,
            N_RULES_RES_STEP = 1,
            MUT_RES = 50;
    //Population
    private static int[] popSizeVariations;
    private static int popSizeIdx = 0;
    private static int popSize = 500;
    //Generations
    private static int[] nGensVariations;
    private static int nGensIdx = 0;
    private static int nGens = 200;
    //Mutation
    private static double[] mutationRateVariations;
    private static int mutationRateIdx = 0;
    private static double mutationRate = (double) 1 / popSize;
    private static double mRateMod = 1.5;
    //Rules
    private static int[] nRulesVariations;
    private static int nRulesIdx = 0;
    private static int chromSize = 0;
    private static int nRules = 40;

    //Test Option Indexes
    private static final int TEST_MUT = 1,
            TEST_POP = 2,
            TEST_GENS = 3,
            TEST_RULES = 4;

    //Imported Data
    private static Rule[] data;
    private static int dataType;

    //User inputs
    private static int selectedDataOption, selectedTestOption;

    //Results
    private static double[][] runResults = new double[4][N_RUNS];
    private static PrintWriter pw;
    private static int percComplete = 1;

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {

        //Init hyperparam variations
        initPopSizes();
        initNGenerations();
        initNRules();

        //GET users task selection
        Scanner scanner = new Scanner(System.in);
        boolean inputValid = false;
        while (!inputValid) {
            System.out.println("Please enter the number of the data file you wish to mine:");
            System.out.println(1 + ". Data1");
            System.out.println(2 + ". Data2");
            System.out.println(3 + ". Data3");

            selectedDataOption = scanner.nextInt();

            switch (selectedDataOption) {
                case 1:
                    data = readDataFile(1, DATA_TYPE_BINARY);
                    dataType = Rule.DATA_TYPE_BINARY;
                    inputValid = true;
                    break;
                case 2:
                    data = readDataFile(2, DATA_TYPE_BINARY);
                    dataType = DATA_TYPE_BINARY;
                    inputValid = true;
                    break;
                case 3:
                    data = readDataFile(3, DATA_TYPE_FLOAT);
                    dataType = DATA_TYPE_FLOAT;
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }

        inputValid = false;
        while (!inputValid) {
            System.out.println("Please enter the number to either:");
            System.out.println(1 + ". Mine Data For Rules");
            System.out.println(2 + ". Test Hyperparameter Variances");

            selectedDataOption = scanner.nextInt();

            switch (selectedDataOption) {
                case 1:
                    System.out.println("Starting rule mining");
                    if (dataType == DATA_TYPE_FLOAT) {
                        runRuleMining(new FloatRuleMiner(popSize, nGens, data, nRules));
                    } else {
                        runRuleMining(new RuleMiner(popSize, nGens, data, nRules));
                    }
                    inputValid = true;
                    break;
                case 2:
                    getUserTestHyperparamsChoice();
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }

        ringBell();
    }

    private static void getUserTestHyperparamsChoice() throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);

        boolean inputValid = false;
        while (!inputValid) {
            System.out.println("Please enter the number of the test you wish to run:");
            System.out.println(TEST_MUT + ". Mutation rate variance test");
            System.out.println(TEST_POP + ". Population size variance test");
            System.out.println(TEST_GENS + ". Number of generations variance test");
            System.out.println(TEST_RULES + ". Number of rules (chromosomes) variance test");

            selectedTestOption = scanner.nextInt();

            switch (selectedTestOption) {
                case TEST_MUT:
                    System.out.println("Starting mutation rate variance test");
                    runMutationVarianceTest();
                    inputValid = true;
                    break;
                case TEST_POP:
                    System.out.println("Starting population size variance test");
                    runPopSizeVarianceTest();
                    inputValid = true;
                    break;
                case TEST_GENS:
                    System.out.println("Starting number of generations variance test");
                    runNGensVarianceTest();
                    inputValid = true;
                    break;
                case TEST_RULES:
                    System.out.println("Starting number of rules variance test");
                    runNRulesVarianceTest();
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }
    }

    private static Rule[] readDataFile(int num, int dataType) throws FileNotFoundException {
        ArrayList<Rule> rules = new ArrayList<>();
        String[] line;

        File file = new File("./src/garuleminer/data/data" + num + ".txt");
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            line = sc.nextLine().split(" ");
            int result = Integer.parseInt(line[line.length - 1]);

            String input = "";

            for (int i = 0; i < line.length - 1; i++) {
                input += line[i];

                if ((i + 1) < (line.length - 1)) {
                    input += " ";
                }
            }
            rules.add(new Rule(input, result, dataType));
        }

        Rule[] ret = new Rule[rules.size()];
        for (int r = 0; r < ret.length; r++) {
            ret[r] = rules.get(r);
        }
        return ret;
    }

    //START_INITIALISERS
    private static void initMutationRates(int popSize, int chromSize) {
        mutationRateVariations = new double[MUT_RES];

        double min = (double) 1 / popSize;
        double max = (double) 1 / chromSize;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "Population size must be greater than Chromosome size");
        }

        double step = (max - min) / MUT_RES;

        mutationRateVariations[0] = min;
        for (int i = 1; i < MUT_RES; i++) {
            mutationRateVariations[i] = mutationRateVariations[i - 1] + step;
        }
    }

    private static void initPopSizes() {
        if (POP_SIZE_MIN >= POP_SIZE_MAX) {
            throw new IllegalArgumentException(
                    "Max population size must be greater than minimum population size");
        }

        int n = (int) ((POP_SIZE_MAX - POP_SIZE_MIN) / POP_SIZE_RES_STEP);
        popSizeVariations = new int[n + 1];

        popSizeVariations[0] = POP_SIZE_MIN;
        for (int i = 1; i <= n; i++) {
            popSizeVariations[i] = popSizeVariations[i - 1] + POP_SIZE_RES_STEP;
        }
    }

    private static void initNGenerations() {
        if (N_GENS_MIN >= N_GENS_MAX) {
            throw new IllegalArgumentException(
                    "Max number of generations must be greater than the minimum number of generations");
        }

        int n = (int) ((N_GENS_MAX - N_GENS_MIN) / N_GENS_RES_STEP);
        nGensVariations = new int[n + 1];

        nGensVariations[0] = N_GENS_MIN;
        for (int i = 1; i <= n; i++) {
            nGensVariations[i] = nGensVariations[i - 1] + N_GENS_RES_STEP;
        }
    }

    private static void initNRules() {
        if (N_RULES_MIN >= N_RULES_MAX) {
            throw new IllegalArgumentException(
                    "Max number of rules must be greater than the minimum number of rules");
        }

        int n = (int) ((N_RULES_MAX - N_RULES_MIN) / N_RULES_RES_STEP);
        nRulesVariations = new int[n + 1];

        nRulesVariations[0] = N_RULES_MIN;
        for (int i = 1; i <= n; i++) {
            nRulesVariations[i] = nRulesVariations[i - 1] + N_RULES_RES_STEP;
        }
    }
    //END_INITIALISERS

    //START_MINING
    private static void runRuleMining(RuleMiner ga) throws FileNotFoundException {

        //RuleMiner ga = new RuleMiner(popSize, nGens, data, nRules);
        Individual bestIndiv = new Individual();
        ArrayList<Rule> rules;
        int conditionSize = ga.getConditionSize(),
                bestFitness = 0, bestNFitRules = 0, nFitRules, bestIndivID = 0;

        //Calculate Mutation Rate
        chromSize = ga.getChromosomeSize();
        mutationRate = (double) (((double) 1 / popSize) + ((double) 1 / chromSize) / mRateMod);
        ga.setProbabilityOfMutation(mutationRate);

        initFittestCSV("FittestIndividualsResults.csv");

        for (int r = 0; r < N_RUNS; r++) {
            ga.run(RuleMiner.SELECTION_TOURNEMENT);

            //Write fittest individual of the current run
            Individual i = ga.getBestIndividual();
            if (dataType == DATA_TYPE_FLOAT) {
                rules = chromosomeToFloatRules(i.getChromosome(), conditionSize);
                writeIndividualsResultsVertical(r + 1, rules, i.getFitness());
            } else {
                rules = chromosomeToCharRules(i.getChromosome(), conditionSize);
                writeIndividualsResultsHorizotal(r + 1, rules, i.getFitness());
            }

            //Check for fittest individual
            nFitRules = ga.countFitRules(rules);
            if (i.getFitness() > bestFitness
                    || (i.getFitness() == bestFitness
                    && nFitRules < bestNFitRules)) {
                bestIndiv = i;
                bestIndivID = r + 1;
                bestNFitRules = nFitRules;
                bestFitness = i.getFitness();
            }

            outputPercComplete(r, N_RUNS);
        }

        //Write fittest individual out of all runs
        if (dataType != DATA_TYPE_FLOAT) {
            rules = chromosomeToCharRules(bestIndiv.getChromosome(), conditionSize);
            writeIndividualsResultsVertical(bestIndivID, rules, bestIndiv.getFitness());
        }

        System.out.println("Test complete");
        pw.close();
    }
    //END_MINING

    //START_TESTS
    private static void runMutationVarianceTest() throws FileNotFoundException {

        RuleMiner ga;
        if (dataType == DATA_TYPE_FLOAT) {
            ga = new FloatRuleMiner(popSize, nGens, data, nRules);
        } else {
            ga = new RuleMiner(popSize, nGens, data, nRules);
        }

        //RuleMiner ga = new RuleMiner(popSize, nGens, data, nRules);
        chromSize = ga.getChromosomeSize();
        initMutationRates(popSize, chromSize);

        initMutationsCSV("MutationRateVarianceResults.csv");

        for (int m = 0; m < mutationRateVariations.length; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setProbabilityOfMutation(mutationRateVariations[m]);

                ga.run(RuleMiner.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(m + 1, mutationRateVariations[m]);

            outputPercComplete(m, mutationRateVariations.length);
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runPopSizeVarianceTest() throws FileNotFoundException {

        RuleMiner ga;
        if (dataType == DATA_TYPE_FLOAT) {
            ga = new FloatRuleMiner(popSize, nGens, data, nRules);
        } else {
            ga = new RuleMiner(popSize, nGens, data, nRules);
        }

        chromSize = ga.getChromosomeSize();
        initPopulationsCSV("PopulationSizeVarianceResults.csv");

        for (int p = 0; p < popSizeVariations.length; p++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setPopulationSize(popSizeVariations[p]);

                ga.setProbabilityOfMutation(1 / popSizeVariations[p]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(p + 1, popSizeVariations[p]);

            outputPercComplete(p, popSizeVariations.length);
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runNGensVarianceTest() throws FileNotFoundException {
        RuleMiner ga;
        if (dataType == DATA_TYPE_FLOAT) {
            ga = new FloatRuleMiner(popSize, nGens, data, nRules);
        } else {
            ga = new RuleMiner(popSize, nGens, data, nRules);
        }

        chromSize = ga.getChromosomeSize();
        initGenerationsCSV("NoOfGenerationsVarianceResults.csv");

        for (int g = 0; g < nGensVariations.length; g++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setNumberOfGenerations(nGensVariations[g]);
                nGens = nGensVariations[g];

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(g + 1, nGensVariations[g]);

            outputPercComplete(g, nGensVariations.length);
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runNRulesVarianceTest() throws FileNotFoundException {

        RuleMiner ga;
        if (dataType == DATA_TYPE_FLOAT) {
            ga = new FloatRuleMiner(popSize, nGens, data, nRules);
        } else {
            ga = new RuleMiner(popSize, nGens, data, nRules);
        }

        initRulesCSV("NoOfRulesVarianceResults.csv");

        for (int n = 0; n < nRulesVariations.length; n++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setNRules(nRulesVariations[n]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(n + 1, nRulesVariations[n]);

            outputPercComplete(n, nRulesVariations.length);
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void recordResults(RuleMiner ga, int r) {
        runResults[RuleMiner.RESULT_BEST][r] = ga.getResult(nGens, RuleMiner.RESULT_BEST);
        runResults[RuleMiner.RESULT_WORST][r] = ga.getResult(nGens, RuleMiner.RESULT_WORST);
        runResults[RuleMiner.RESULT_AVERAGE][r] = ga.getResult(nGens, RuleMiner.RESULT_AVERAGE);
        runResults[RuleMiner.RESULT_SUM][r] = ga.getResult(nGens, RuleMiner.RESULT_SUM);
    }
    //END_TESTS

    //START_CSV
    private static void initFittestCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(popSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(nGens));
        sb.append('\n');
        sb.append("Probability of Mutation = ");
        sb.append(String.valueOf(mutationRate));
        sb.append('\n');
        sb.append('\n');
        if (dataType != DATA_TYPE_FLOAT) {
            sb.append("Id");
            sb.append(',');
            for (int i = 0; i < nRules; i++) {
                sb.append("Rule ");
                sb.append(String.valueOf(i + 1));
                sb.append(',');
                sb.append("Fitness Awarded ");
                sb.append(String.valueOf(i + 1));
                sb.append(',');
            }
            sb.append("Fitness");
            sb.append('\n');
        }
        pw.write(sb.toString());
    }

    private static void initMutationsCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(popSize));
        sb.append('\n');
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(nGens));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append("No of Rules = ");
        sb.append(String.valueOf(nRules));
        sb.append('\n');
        sb.append('\n');
        sb.append("Id");
        sb.append(',');
        sb.append("Mutation Rate");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Worst Fitness");
        sb.append(',');
        sb.append("Avg Fitness Range");
        sb.append(',');
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void initPopulationsCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(N_GENS_MIN));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append("No of Rules = ");
        sb.append(String.valueOf(N_RULES_MIN));
        sb.append('\n');
        sb.append('\n');
        sb.append("Id");
        sb.append(',');
        sb.append("Population Size");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Worst Fitness");
        sb.append(',');
        sb.append("Avg Fitness Range");
        sb.append(',');
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void initGenerationsCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(popSize));
        sb.append('\n');
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append("No of Rules = ");
        sb.append(String.valueOf(nRules));
        sb.append('\n');
        sb.append("Probability of Mutation = ");
        sb.append(String.valueOf(mutationRate));
        sb.append('\n');
        sb.append('\n');
        sb.append("Id");
        sb.append(',');
        sb.append("No of Generations");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Worst Fitness");
        sb.append(',');
        sb.append("Avg Fitness Range");
        sb.append(',');
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void initRulesCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(popSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(nGens));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append("Probability of Mutation = ");
        sb.append(String.valueOf(mutationRate));
        sb.append('\n');
        sb.append('\n');
        sb.append("Id");
        sb.append(',');
        sb.append("No of Rules");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Worst Fitness");
        sb.append(',');
        sb.append("Avg Fitness Range");
        sb.append(',');
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void writeResults(int id, double rate) {
        StringBuilder sb = new StringBuilder();
        double best = calcAvg(runResults[RuleMiner.RESULT_BEST]),
                worst = calcAvg(runResults[RuleMiner.RESULT_WORST]);
        sb.append(id);
        sb.append(',');
        sb.append(String.valueOf(rate));
        sb.append(',');
        sb.append(String.valueOf(best));
        sb.append(',');
        sb.append(String.valueOf(worst));
        sb.append(',');
        sb.append(String.valueOf((best - worst)));
        sb.append(',');
        sb.append(String.valueOf(calcAvg(runResults[RuleMiner.RESULT_AVERAGE])));
        sb.append(',');
        sb.append(String.valueOf(calcAvg(runResults[RuleMiner.RESULT_SUM])));
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void writeIndividualsResultsHorizotal(int id, ArrayList<Rule> rules, int fitness) {
        Rule rule;
        RuleMiner rm;
        if (dataType == DATA_TYPE_FLOAT) {
            rm = new FloatRuleMiner(data);
        } else {
            rm = new RuleMiner(data);
        }
        int[] ruleFitnesses = rm.calcRuleFitness(rules);

        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(id));
        sb.append(',');
        for (int r = 0; r < rules.size(); r++) {
            rule = rules.get(r);
            if (dataType == DATA_TYPE_FLOAT) {
                float[] realNumArr = rule.getRealNumArr();
                for (float realNum : realNumArr) {
                    sb.append(String.valueOf(realNum));
                    sb.append(' ');
                }
                sb.append(String.valueOf(rule.getOutput()));
            } else {
                sb.append(String.valueOf(rule.getCharArr()));
                sb.append(' ');
                sb.append(String.valueOf(rule.getOutput()));
            }
            sb.append(',');
            sb.append(String.valueOf(ruleFitnesses[r]));
            sb.append(',');
        }
        sb.append(String.valueOf(fitness));
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void writeIndividualsResultsVertical(int id, ArrayList<Rule> rules, int fitness) {
        Rule rule;
        RuleMiner rm;
        if (dataType == DATA_TYPE_FLOAT) {
            rm = new FloatRuleMiner(data);
        } else {
            rm = new RuleMiner(data);
        }
        int[] ruleFitnesses = rm.calcRuleFitness(rules);
        int ruleFitness, nFitRules = 0;

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("ID");
        sb.append(',');
        sb.append(String.valueOf(id));
        sb.append('\n');
        sb.append("Rule");
        sb.append(',');
        sb.append("Fitness Awarded");
        sb.append('\n');
        for (int r = 0; r < rules.size(); r++) {
            rule = rules.get(r);
            ruleFitness = ruleFitnesses[r];

            if (ruleFitness > 0) {
                if (dataType == DATA_TYPE_FLOAT) {
                    float[] realNumArr = rule.getRealNumArr();
                    for (float realNum : realNumArr) {
                        sb.append(String.valueOf(realNum));
                        sb.append(' ');
                    }
                    sb.append(String.valueOf(rule.getOutput()));
                } else {
                    sb.append(String.valueOf(rule.getCharArr()));
                    sb.append(' ');
                    sb.append(String.valueOf(rule.getOutput()));
                }
                sb.append(',');
                sb.append(String.valueOf(ruleFitness));
                
                if ((r + 1) != rules.size()) {
                    sb.append('\n');
                }

                nFitRules++;
            }
        }
        sb.append(String.valueOf(nFitRules));
        sb.append(',');
        sb.append(String.valueOf(fitness));
        sb.append('\n');
        sb.append('\n');
        pw.write(sb.toString());
    }
    //END_CSV

    //START_Utils
    private static void outputPercComplete(double a, double b) {
        System.out.println("Test " + calcPerc(a, b) + "% complete");
    }

    private static ArrayList<Rule> chromosomeToCharRules(Object[] oGenes, int conditionSize) {
        ArrayList<Rule> ret = new ArrayList<>();
        int k = 0;
        Character[] genes = new Character[oGenes.length];

        for (int g = 0; g < genes.length; g++) {
            genes[g] = (Character) oGenes[g];
        }

        for (int r = 0; r < nRules; r++) {
            char[] cond = new char[conditionSize];

            for (int c = 0; c < conditionSize; c++) {
                cond[c] = genes[k++];
            }
            ret.add(new Rule((char[]) cond,
                    Character.getNumericValue(genes[k++]), Rule.DATA_TYPE_BINARY));
        }

        return ret;
    }

    private static ArrayList<Rule> chromosomeToFloatRules(Object[] oGenes, int conditionSize) {
        ArrayList<Rule> ret = new ArrayList<>();
        int k = 0;
        Float[] genes = new Float[oGenes.length];

        for (int g = 0; g < genes.length; g++) {
            genes[g] = (Float) oGenes[g];
        }

        for (int r = 0; r < nRules; r++) {
            float[] cond = new float[conditionSize];

            for (int c = 0; c < conditionSize; c++) {
                cond[c] = genes[k++];
            }
            ret.add(new Rule((float[]) cond, (int) genes[k++].floatValue(),
                    Rule.DATA_TYPE_FLOAT));
        }

        return ret;
    }

    private static double calcAvg(double[] arr) {
        if (arr.length == 0) {
            return 0;
        }

        double ret = 0;

        for (int i = 0; i < arr.length; i++) {
            ret += arr[i];
        }
        return ret / arr.length;
    }

    private static double calcPerc(double a, double b) {
        return (100 / b) * a;
    }

    private static void ringBell() {
        try {
            AudioInputStream audioInputStream
                    = AudioSystem.getAudioInputStream(
                            new File("./src/garuleminer/BELL.WAV"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //END_Utils
}
