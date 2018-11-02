/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garuleminer;

/**
 *
 * @author c2-newcombe
 */
public class Data {
    public final int DATA_TYPE_BINARY = 1,
            DATA_TYPE_FLOAT = 2;
    private int[] binaryArr;
    private float realNumber;
    private final int result, dataType;
    
    public Data(String input, int result, int dataType){
        this.result = result;
        this.dataType = dataType;
        resolveInput(input);
    }
    
    private void resolveInput(String input){
        switch(this.dataType){
            case DATA_TYPE_FLOAT:
                realNumber = Float.parseFloat(input);
                break;
            case DATA_TYPE_BINARY:
                binaryArr = transformStringToIntArr(input);
                break;
            default:
                System.err.println("Data Type Not Found");
        }
    }
    
    private int[] transformStringToIntArr(String str){
        char[] strArr = str.toCharArray();
        int [] ret = new int[strArr.length];
        
        for(int i = 0; i < strArr.length; i++){
            try{
                ret[i] = Character.getNumericValue(strArr[i]);
            }catch (Exception e){
                System.err.println(e);
                System.err.println("c = " + strArr[i]);
            }
        }
        
        return ret;
    }
}
