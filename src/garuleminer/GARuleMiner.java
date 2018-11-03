/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

import static garuleminer.Data.DATA_TYPE_BINARY;
import static garuleminer.Data.DATA_TYPE_FLOAT;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

import geneticalgorithm.GeneticAlgorithm;

/**
 *
 * @author c2-newcombe
 */
public class GARuleMiner {

    //Imported Data
    private static ArrayList<Data> data1, data2, data3;

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        //Read and store data
        data1 = readDataFile(1, DATA_TYPE_BINARY);
        data2 = readDataFile(2, DATA_TYPE_BINARY);
        data3 = readDataFile(3, DATA_TYPE_FLOAT);

        RuleMiner ga = new RuleMiner(100, 50, 50);
        ga.run(GeneticAlgorithm.SELECTION_TOURNEMENT);
        
        System.out.println("Best Result = " 
                + ga.getResult(50, GeneticAlgorithm.RESULT_BEST));
    }

    private static ArrayList<Data> readDataFile(int num, int dataType) throws FileNotFoundException {
        ArrayList<Data> ret = new ArrayList<>();
        String[] line;

        File file = new File("./src/garuleminer/data/data" + num + ".txt");
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            line = sc.nextLine().split(" ");
            int result = Integer.parseInt(line[line.length - 1]);
            
            String input = "";
            
            for(int i = 0; i < line.length - 1; i++){
                input += line[i];
                
                if((i + 1) < (line.length - 1)){
                    input += " ";
                }
            }
            ret.add(new Data(input, result, dataType));
        }

        return ret;
    }
}
