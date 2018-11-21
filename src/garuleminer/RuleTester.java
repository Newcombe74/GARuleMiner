/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

import static garuleminer.Rule.DATA_TYPE_BINARY;
import static garuleminer.Rule.DATA_TYPE_FLOAT;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Conor
 */
public class RuleTester {

    public static void main(String[] args) throws FileNotFoundException {

        int selectedDataOption;

        //GET users task selection
        Scanner scanner = new Scanner(System.in);
        boolean inputValid = false;
        while (!inputValid) {
            System.out.println("Please enter the number of the data file you wish to test assetion:");
            System.out.println(1 + ". Data1");
            System.out.println(2 + ". Data2");
            System.out.println(3 + ". Data3");

            selectedDataOption = scanner.nextInt();

            switch (selectedDataOption) {
                case 1:
                    testData1Rule();
                    inputValid = true;
                    break;
                case 2:
                    testData2Rule();
                    inputValid = true;
                    break;
                case 3:
                    testData3Rule();
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }
    }

    private static void testData1Rule() throws FileNotFoundException {
        Rule[] data = readDataFile(1, DATA_TYPE_BINARY);
        char[] cond;
        int zeroCount, nCorrectAssertions = 0;
        boolean assertion, output;

        for (Rule rule : data) {
            zeroCount = 0;
            cond = rule.getCharArr();
            output = rule.getOutput() == 1;

            for (int c = 0; c < cond.length; c++) {
                if (cond[c] == '0') {
                    zeroCount++;
                }
            }

            assertion = zeroCount == 1 || zeroCount == 3;

            if (assertion == output) {
                nCorrectAssertions++;
            }
        }

        System.out.println(nCorrectAssertions + " / " + data.length);
    }

    private static void testData2Rule() throws FileNotFoundException {
        Rule[] data = readDataFile(2, DATA_TYPE_BINARY);
        char[] cond;
        int zeroCount, nCorrectAssertions = 0;
        boolean assertion, output;

        for (Rule rule : data) {
            zeroCount = 0;
            cond = rule.getCharArr();
            output = rule.getOutput() == 1;

            for (int c = 0; c < cond.length; c++) {
                if (cond[c] == '0') {
                    zeroCount++;
                }
            }

            assertion = zeroCount == 1 || zeroCount == 3;

            if (assertion == output) {
                nCorrectAssertions++;
            }
        }

        System.out.println(nCorrectAssertions + " / " + data.length);
    }

    private static void testData3Rule() throws FileNotFoundException {
        Rule[] data = readDataFile(3, DATA_TYPE_FLOAT);
        float[] cond;
        int n0s = 0, n1s = 0, nCorrect0s = 0, nCorrect1s = 0, nLessThanAvg;
        float total, avg;
        boolean assertion, output;
        String UpOrDown;

        for (Rule rule : data) {
            cond = rule.getRealNumArr();
            output = rule.getOutput() == 1;

            UpOrDown = "";
            for (int c = 0; c < cond.length; c++) {
                if (cond[c] > 0.5) {
                    UpOrDown += '1';
                } else {
                    UpOrDown += '0';
                }
            }
            UpOrDown += ' ';
            if (output) {
                    UpOrDown += '1';
                } else {
                    UpOrDown += '0';
                }
            System.out.println(UpOrDown);


            /*
            assertion = (cond[4] > 0.5 && cond[2] > 0.5 && cond[1] < 0.5) 
                    || (cond[3] > 0.5 && cond[6] > 0.5);

            if (output) {
                n1s++;
            } else {
                n0s++;
            }

            if (assertion == output) {
                if (output) {
                    nCorrect1s++;
                } else {
                    nCorrect0s++;
                }
            }
             */
        }

        System.out.println(nCorrect0s + " / " + n0s);
        System.out.println(nCorrect1s + " / " + n1s);
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
}
