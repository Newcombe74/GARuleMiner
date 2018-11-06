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

import geneticalgorithm.GeneticAlgorithm;
import java.io.PrintWriter;

/**
 *
 * @author c2-newcombe
 */
public class GARuleMiner {

    //Hyperparameters
    private static final int POP_SIZE_MIN = 180,
            POP_SIZE_MAX = 1000,
            POP_SIZE_RES_STEP = 10,
            N_GENS_MIN = 50,
            N_GENS_MAX = 100,
            N_GENS_RES_STEP = 1,
            N_RUNS = 10,
            N_RULES_MIN = 10,
            N_RULES_MAX = 20,
            N_RULES_RES_STEP = 1,
            MUT_RES = 100;
    //Population
    private static int popSizes[];
    private static int currPopSizeIdx = 0;
    //Generations
    private static int nGenerations[];
    private static int currNGensIdx = 0;
    //Mutation
    private static double mutationRates[];
    private static int currPmIdx = 0;
    //Rules
    private static int nRules[];
    private static int currNRulesIdx = 0;
    private static int chromSize = 0;

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
            System.out.println("Please enter the number of the test you wish to run:");
            System.out.println(TEST_MUT + ". Mutation rate variance test");
            System.out.println(TEST_POP + ". Population size variance test");
            System.out.println(TEST_GENS + ". Number of generations variance test");
            System.out.println(TEST_RULES + ". Number of rules variance test");

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
        mutationRates = new double[MUT_RES];

        double min = (double) 1 / popSize;
        double max = (double) 1 / chromSize;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "Population size must be greater than Chromosome size");
        }

        double step = (max - min) / MUT_RES;

        mutationRates[0] = min;
        for (int i = 1; i < MUT_RES; i++) {
            mutationRates[i] = mutationRates[i - 1] + step;
        }
    }

    private static void initPopSizes() {
        if (POP_SIZE_MIN >= POP_SIZE_MAX) {
            throw new IllegalArgumentException(
                    "Max population size must be greater than minimum population size");
        }

        int n = (int) ((POP_SIZE_MAX - POP_SIZE_MIN) / POP_SIZE_RES_STEP);
        popSizes = new int[n];

        popSizes[0] = POP_SIZE_MIN;
        for (int i = 1; i < n; i++) {
            popSizes[i] = popSizes[i - 1] + POP_SIZE_RES_STEP;
        }
    }

    private static void initNGenerations() {
        if (N_GENS_MIN >= N_GENS_MAX) {
            throw new IllegalArgumentException(
                    "Max number of generations must be greater than the minimum number of generations");
        }

        int n = (int) ((N_GENS_MAX - N_GENS_MIN) / N_GENS_RES_STEP);
        nGenerations = new int[n];

        nGenerations[0] = N_GENS_MIN;
        for (int i = 1; i < n; i++) {
            nGenerations[i] = nGenerations[i - 1] + N_GENS_RES_STEP;
        }
    }

    private static void initNRules() {
        if (N_RULES_MIN >= N_RULES_MAX) {
            throw new IllegalArgumentException(
                    "Max number of rules must be greater than the minimum number of rules");
        }

        int n = (int) ((N_RULES_MAX - N_RULES_MIN) / N_RULES_RES_STEP);
        nRules = new int[n];

        nRules[0] = N_RULES_MIN;
        for (int i = 1; i < n; i++) {
            nRules[i] = nRules[i - 1] + N_RULES_RES_STEP;
        }
    }
    //END_INITIALISERS

    //START_TESTS
    private static void runMutationVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(POP_SIZE_MIN, N_GENS_MIN, data, N_RULES_MIN);
        chromSize = ga.getChromosomeSize();
        initMutationRates(POP_SIZE_MIN, chromSize);

        initMutationsCSV("MutationRateVarianceResults.csv");

        for (int m = 0; m < mutationRates.length; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setProbabilityOfMutation(mutationRates[m]);

                ga.run(RuleMiner.SELECTION_ROULETTE);

                recordResults(ga, r);
            }
            writeResults(m + 1, mutationRates[m]);

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
        initMutationRates(POP_SIZE_MIN, chromSize);

        initMutationsCSV("MutationRateVarianceResults.csv");

        for (int m = 0; m < mutationRates.length; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setProbabilityOfMutation(mutationRates[m]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(m + 1, mutationRates[m]);

            if (calcPerc(m, MUT_RES) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runNGensVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(POP_SIZE_MIN, N_GENS_MIN, data, N_RULES_MIN);
        chromSize = ga.getChromosomeSize();
        initMutationRates(POP_SIZE_MIN, chromSize);

        initMutationsCSV("MutationRateVarianceResults.csv");

        for (int m = 0; m < mutationRates.length; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setProbabilityOfMutation(mutationRates[m]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(m + 1, mutationRates[m]);

            if (calcPerc(m, MUT_RES) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runNRulesVarianceTest() throws FileNotFoundException {

        RuleMiner ga = new RuleMiner(POP_SIZE_MIN, N_GENS_MIN, data, N_RULES_MIN);
        chromSize = ga.getChromosomeSize();
        initMutationRates(POP_SIZE_MIN, chromSize);

        initMutationsCSV("MutationRateVarianceResults.csv");

        for (int m = 0; m < mutationRates.length; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                ga.setProbabilityOfMutation(mutationRates[m]);

                ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);

                recordResults(ga, r);
            }
            writeResults(m + 1, mutationRates[m]);

            if (calcPerc(m, MUT_RES) > percComplete) {
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
    private static void initMutationsCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(POP_SIZE_MIN));
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
        sb.append("MutationRate");
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
    //END_CSV

    //START_Utils
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
