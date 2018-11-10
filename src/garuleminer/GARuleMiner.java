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
            N_GENS_MIN = 10,
            N_GENS_MAX = 100,
            N_GENS_RES_STEP = 1,
            N_RUNS = 10,
            N_RULES_MIN = 10,
            N_RULES_MAX = 100,
            N_RULES_RES_STEP = 1,
            MUT_RES = 100;
    //Population
    private static int[] popSizeVariations;
    private static int popSizeIdx = 0;
    private static int popSize = POP_SIZE_MIN;
    //Generations
    private static int[] nGensVariations;
    private static int nGensIdx = 0;
    private static int nGens = popSize / 2;
    //Mutation
    private static double[] mutationRateVariations;
    private static int mutationRateIdx = 0;
    private static double mutationRate = (double) 1 / popSize;
    //Rules
    private static int[] nRulesVariations;
    private static int nRulesIdx = 0;
    private static int chromSize = 0;
    private static int nRules = N_RULES_MIN;

    //Test Option Indexes
    private static final int TEST_MUT = 1,
            TEST_POP = 2,
            TEST_GENS = 3,
            TEST_RULES = 4;

    //Imported Data
    private static Rule[] data;

    //User inputs
    private static int selectedDataOption, selectedTestOption;

    //Results
    private static double[][] runResults = new double[4][N_RUNS];
    private static PrintWriter pw;
    private static int percComplete = 10;

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
                    inputValid = true;
                    break;
                case 2:
                    data = readDataFile(2, DATA_TYPE_BINARY);
                    inputValid = true;
                    break;
                case 3:
                    data = readDataFile(3, DATA_TYPE_FLOAT);
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
            System.out.println(2 + ". Test Hyperparameter Variences");

            selectedDataOption = scanner.nextInt();

            switch (selectedDataOption) {
                case 1:
                    runBinaryRuleMining();
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
    }
    
    private static void getUserTestHyperparamsChoice() throws FileNotFoundException{
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
    private static void runBinaryRuleMining() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(popSize, nGens, data, nRules);
        ArrayList<Rule> rules;
        int conditionSize = ga.getConditionSize();
        
        initFittestCSV("FittestIndividualsResults.csv");

        for (int r = 0; r < N_RUNS; r++) {
            ga.run(RuleMiner.SELECTION_ROULETTE);

            Individual i = ga.getBestIndividual();
            rules = chromosomeToRules(i.getChromosome(), conditionSize);
            writeIndividualsResults(r + 1, rules, i.getFitness());

            if (calcPerc(r, N_RUNS) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }
    //END_MINING

    //START_TESTS
    private static void runMutationVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(POP_SIZE_MIN, N_GENS_MIN, data, N_RULES_MIN);
        chromSize = ga.getChromosomeSize();
        popSize = chromSize * 2;
        nGens = popSize / 2;
        ga.setPopulationSize(popSize);
        ga.setNumberOfGenerations(nGens);
        initMutationRates(popSize, chromSize);

        initMutationsCSV("MutationRateVarianceResults.csv");

        for (int m = 0; m < mutationRateVariations.length; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setProbabilityOfMutation(mutationRateVariations[m]);

                ga.run(RuleMiner.SELECTION_ROULETTE);

                recordResults(ga, r);
            }
            writeResults(m + 1, mutationRateVariations[m]);

            if (calcPerc(m, MUT_RES) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runPopSizeVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(POP_SIZE_MIN, N_GENS_MIN, data, N_RULES_MIN);
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

            if (calcPerc(p, popSizeVariations.length) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runNGensVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(popSize, N_GENS_MIN, data, N_RULES_MIN);
        chromSize = ga.getChromosomeSize();
        initGenerationsCSV("NoOfGenerationsVarianceResults.csv");

        for (int g = 0; g < nGensVariations.length; g++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setNumberOfGenerations(nGensVariations[g]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(g + 1, nGensVariations[g]);

            if (calcPerc(g, nGensVariations.length) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runNRulesVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(popSize, nGens, data, N_RULES_MIN);
        initRulesCSV("NoOfRulesVarianceResults.csv");

        for (int n = 0; n < nRulesVariations.length; n++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setNRules(nRulesVariations[n]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(n + 1, nRulesVariations[n]);

            if (calcPerc(n, nRulesVariations.length) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void recordResults(RuleMiner ga, int r) {
        runResults[RuleMiner.RESULT_BEST][r] = ga.getResult(N_GENS_MIN, RuleMiner.RESULT_BEST);
        runResults[RuleMiner.RESULT_WORST][r] = ga.getResult(N_GENS_MIN, RuleMiner.RESULT_WORST);
        runResults[RuleMiner.RESULT_AVERAGE][r] = ga.getResult(N_GENS_MIN, RuleMiner.RESULT_AVERAGE);
        runResults[RuleMiner.RESULT_SUM][r] = ga.getResult(N_GENS_MIN, RuleMiner.RESULT_SUM);
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
        sb.append(String.valueOf((double) 1 / popSize));
        sb.append('\n');
        sb.append('\n');
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
        sb.append("Mutation Rate");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Worst Fitness");
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
        sb.append(String.valueOf(POP_SIZE_MIN));
        sb.append('\n');
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append("No of Rules = ");
        sb.append(String.valueOf(N_RULES_MIN));
        sb.append('\n');
        sb.append("Probability of Mutation = ");
        sb.append(String.valueOf((double) 1 / popSize));
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
        sb.append(String.valueOf(N_GENS_MIN));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append("Probability of Mutation = ");
        sb.append(String.valueOf(1 / popSize));
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
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void writeResults(int id, double rate) {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(',');
        sb.append(rate);
        sb.append(',');
        sb.append(calcAvg(runResults[RuleMiner.RESULT_BEST]));
        sb.append(',');
        sb.append(calcAvg(runResults[RuleMiner.RESULT_WORST]));
        sb.append(',');
        sb.append(calcAvg(runResults[RuleMiner.RESULT_AVERAGE]));
        sb.append(',');
        sb.append(calcAvg(runResults[RuleMiner.RESULT_SUM]));
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void writeIndividualsResults(int id, ArrayList<Rule> rules, int fitness) {
        Rule rule;
        RuleMiner rm = new RuleMiner(data);
        int[] ruleFitnesses = rm.calcRuleFitness(rules);
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(id));
        sb.append(',');
        for (int r = 0; r < rules.size(); r++) {
            rule = rules.get(r);
            sb.append(String.valueOf(rule.getCharArr()));
            sb.append(' ');
            sb.append(String.valueOf(rule.getOutput()));
            sb.append(',');
            sb.append(String.valueOf(ruleFitnesses[r]));
            sb.append(',');
        }
        sb.append(String.valueOf(fitness));
        sb.append('\n');
        pw.write(sb.toString());
    }
    //END_CSV

    //START_Utils
    
    private static ArrayList<Rule> chromosomeToRules(Object[] oGenes, int conditionSize) {
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
    //END_Utils
}
