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
public class Rule {
    public static final int DATA_TYPE_BINARY = 1,
            DATA_TYPE_FLOAT = 2;
    private char[] charArr;
    private float[] realNumArr;
    private final int output, dataType;
    
    public Rule(String input, int output, int dataType){
        this.output = output;
        this.dataType = dataType;
        resolveInput(input);
    }
    
    public Rule(char[] input, int output, int dataType){
        this.output = output;
        this.dataType = dataType;
        charArr = input;
    }
    
    private void resolveInput(String input){
        switch(this.dataType){
            case DATA_TYPE_FLOAT:
                realNumArr = transformStringToFloatArr(input);
                break;
            case DATA_TYPE_BINARY:
                charArr = input.toCharArray();
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
    
    private float[] transformStringToFloatArr(String str){
        String[] strArr = str.split(" ");
        float [] ret = new float[strArr.length];
        
        for(int i = 0; i < strArr.length; i++){
            try{
                ret[i] = Float.parseFloat(strArr[i]);
            }catch (NumberFormatException e){
                System.err.println(e);
                System.err.println("c = " + strArr[i]);
            }
        }
        
        return ret;
    }

    public char[] getCharArr() {
        return charArr;
    }

    public void setCharArr(char[] charArr) {
        this.charArr = charArr;
    }

    public float[] getRealNumArr() {
        return realNumArr;
    }

    public void setRealNumArr(float[] realNumArr) {
        this.realNumArr = realNumArr;
    }

    public int getOutput() {
        return output;
    }

    public int getDataType() {
        return dataType;
    }
}
