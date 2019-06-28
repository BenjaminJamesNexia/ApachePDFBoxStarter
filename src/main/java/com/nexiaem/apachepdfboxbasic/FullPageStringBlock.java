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
public class FullPageStringBlock implements Comparable<FullPageStringBlock>{

    private final float x;
    private final float xend;
    private final float y;
    private final float yend;
    private final String text;
    private boolean sortByXend;   
    private FullPageStringBlock previous;
    private FullPageStringBlock next;
    
    FullPageStringBlock(float x, float xend, float y, float yend, String text){
        this.x = x;
        this.xend = xend;
        this.text = text;
        this.y = y;
        this.yend = yend;
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

    @Override
    public int compareTo(FullPageStringBlock a)
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

    public static String arrayToStringTabDelimited(ArrayList<FullPageStringBlock> blocks){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < blocks.size(); i++){
            FullPageStringBlock block = blocks.get(i);
            if(i>0) builder.append("\t");
            builder.append(block.text);
        }            
        return builder.toString();
    }    

    public FullPageStringBlock getPrevious() {
        return previous;
    }

    public void setPrevious(FullPageStringBlock previous) {
        this.previous = previous;
    }

    public FullPageStringBlock getNext() {
        return next;
    }

    public void setNext(FullPageStringBlock next) {
        this.next = next;
    }
    
    
    
}

