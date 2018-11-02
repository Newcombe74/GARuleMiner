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
        data1 = readDataFile(1, DATA_TYPE_BINARY);
        data2 = readDataFile(2, DATA_TYPE_BINARY);
        data3 = readDataFile(3, DATA_TYPE_FLOAT);

        int i = 0;
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
            for(int i = 0; i < line.length - 2; i++){
                input += line[i] + " ";
            }
            ret.add(new Data(input, result, dataType));
        }

        return ret;
    }
}
