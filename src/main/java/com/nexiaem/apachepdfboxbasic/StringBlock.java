/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nexiaem.apachepdfboxbasic;

import java.util.ArrayList;

/**
 *
 * @author bjames
 */
public class StringBlock implements Comparable<StringBlock>{

    private final float x;
    private float xend;
    private String text;
    private final boolean sortByXend;   
    private StringBlock left;
    private StringBlock right;

    StringBlock(float x, float xend, String text){
        this.x = x;
        this.xend = xend;
        this.text = text;
        this.sortByXend = false;
    }
    float getX(){
        return x;
    }
    float getXEnd(){
        return xend;
    }
    String getText(){
        return text;
    }

    public void setLeft(StringBlock left) {
        this.left = left;
    }

    public void setRight(StringBlock right) {
        this.right = right;
    }

    public StringBlock getLeft() {
        return left;
    }

    public StringBlock getRight() {
        return right;
    }    
    
    @Override
    public int compareTo(StringBlock a)
    {
        if(sortByXend){
            return (int)((this.xend - a.xend)*1000);
        }else{
            return (int)((this.x - a.x)*1000);
        }
    }        

    @Override
    public String toString(){
        return text;
    }

    public static String arrayToStringTabDelimited(ArrayList<StringBlock> blocks){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < blocks.size(); i++){
            StringBlock block = blocks.get(i);
            if(i>0) builder.append("\t");
            builder.append(block.text);
        }            
        return builder.toString();
    }     
    
    static char tOrN(StringBlock block){
        String rawText = block.getText().trim();
        String thisTextOnlyNumbersEtcRemaining = rawText.replaceAll("[^\\d\\.,−\\-\\(\\)%]", "");
        if(thisTextOnlyNumbersEtcRemaining.length() < rawText.length()){
            return 'T';
        }else{
            //should have at least one 0-9 or be a single minus sign or ()
            if(rawText.equals("-")||rawText.equals("()")) return 'N';
            if(rawText.replaceAll("[0-9]", "").equals(rawText)) return 'T';
            return 'N';
        }
    }
    
    public void integrate(StringBlock block){
        if(block.x < this.x) throw new IllegalArgumentException("The integrated block needs to be to the right of the existing block");
        this.text = this.text + " " + block.text;
        if(this.xend < block.xend) this.xend = block.xend;
    }
    
}
