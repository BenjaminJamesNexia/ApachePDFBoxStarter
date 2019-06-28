/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nexiaem.apachepdfboxbasic;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author bjames
 */
class Column implements Comparable<Column>{
    private float a;
    private float b;
    private char tOrN;
    private Column lc;
    private Column rc;

    @Override      
    public int compareTo(Column c)
    {
        return (int)((this.a - c.a)*1000);
    }                
    
    //Measures if the column passed fits within this columns boundaries
    boolean fits(Column c){
        if(a<=c.a && b>=c.b) return true;
        return false;
    }   
    
    boolean overlapsAndIsWithinNeighbouringBoundaries(Column c){
        if(!overlaps(c)) return false;
        if(c.b > b){
            if(rc!=null){
                if(c.b >= rc.a) return false;
            }
        }        
        if(c.a < a){
            if(lc!=null){
                if(c.a <= lc.b) return false;
            }
        }
        return true;
    }
    
    /**
     * Returns true if the passed column fits within the line for this column irrespective of whether the column overlaps this column
     * @param c
     * @return 
     */
    boolean fitsWithinLine(Column c){
        if(a<=c.a && b>=c.b) return true;
        //Even if the column doesnt fit, test if it fits between this columns neighbours boundaries
        float iba = a;
        if(lc!=null&&lc.b<a) iba = lc.b;
        float ibb = b;
        if(rc!=null&&rc.a>b){
            ibb = rc.a;
        }else{
            //This is the last column in both sets so add a margin of error in
            if(c.rc==null){
                ibb = ibb + 5;
            }
        }
        if(iba<=c.a && ibb>=c.b) return true;
        return false;
    }
    
    boolean overlaps(Column c){
        if(c.a > b) return false;
        if(c.b < a) return false;
        return true;
    }
    
    /**
     * returns true if the passed column is to the right of this column.  If there is a column to the right of this it also checks that it fits b/w this and the next column without any overlap.
     * @param c
     * @return 
     */
    boolean fitsBetweenRC(Column c){          
        if(c.a > b){
            if(rc==null) return true;
            if(c.b < rc.a) return true;
        }
        return false;            
    }
    boolean fitsBetweenLC(Column c){
        if(c.b<a){
            if(lc==null) return true;
            if(c.a > lc.b) return true;
        }
        return false;
    }
        
    boolean isToTheLeftOf(Column c){
        if(c.a > b) return true;
        return false;
    }
    
    void merge(Column c){
        if(c.a < a) a = c.a;
        if(c.b > b) b = c.b;
    }    

    float getA() {
        return a;
    }

    float getB() {
        return b;
    }

    void setA(float a) {
        this.a = a;
    }

    void setB(float b) {
        this.b = b;
    }
    
    /**
     * calculates the percent of this column that is overlapped by the passed in column from its a value to the neighbouring columns a value
     * @param c 
     */
    float overlap(Column c){
        float length = b - a;
        float aStart = a;
        if(c.a>a) aStart = c.a;
        float bEnd = b;
        float c_b = c.b;
        if(c.rc!=null) c_b = c.rc.a;
        if(c_b<b) bEnd = c_b;
        float overlapLength = bEnd - aStart;
        return (overlapLength / length);
    }
    
    static ArrayList<Column> blocksToColumns(ArrayList<StringBlock> line){
        ArrayList<Column> columns = new ArrayList<>();
        Column prior = null;
        for(StringBlock block: line){            
            Column c = new Column();
            c.a = block.getX();
            c.b = block.getXEnd();    
            c.tOrN = StringBlock.tOrN(block);
            if(prior!=null){
                c.lc = prior;
                prior.rc = c;
            }
            columns.add(c);
            prior = c;
        }    
        return columns;
    }
    
    static Column blockToColumn(StringBlock block){
        Column c = new Column();
        c.a = block.getX();
        c.b = block.getXEnd();     
        c.tOrN = StringBlock.tOrN(block);
        return c;
    }
    
    static void sortAndRelink(ArrayList<Column> columns){
        Collections.sort(columns);
        //Now relink
        Column prior = null;
        for(Column c: columns){
            if(prior!=null){
                c.lc = prior;
                prior.rc = c;
            }
            prior = c;            
        }        
    }
    
    StringBlock toBlock(){
        StringBlock newBlock = new StringBlock(a, b, "");
        return newBlock;
    }
    
    /**
     * Checks is the column passed would, if added to the line containing this column, stray across any boundaries and reduce the granularity of the column if added to this columns line
     * @param c2
     * @return true if the column passed overlaps multiple columns within this columns lines
     */
    boolean straysAcrossMyBoundaries(Column c2){
        if(c2.getA() < a){
            if(lc!=null){
                if(c2.a < lc.b) return true;
            }
        }else{
            if(rc!=null){
                if(c2.b > rc.a) return true;
            }                        
        }   
        if(c2.getB() > b){
            if(rc!=null){
                if(c2.b > rc.a) return true;
            }
        }
        return false;
    }
    
    char getTOrN(){
        return tOrN;
    }
    
    static ArrayList<Column> cloneColumns(ArrayList<Column> toBeCloned){
        ArrayList<StringBlock> blocks = new ArrayList<>();
        for(Column c: toBeCloned){
            blocks.add(c.toBlock());
        }
        return blocksToColumns(blocks);
    }
}
