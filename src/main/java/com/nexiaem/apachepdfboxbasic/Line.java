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
public class Line {
    private final ArrayList<Column> columns;    
    Line(ArrayList<StringBlock> line){
        columns = Column.blocksToColumns(line);
    }
    
    public int columnCount(){
        return columns.size();
    }
    
    /**
     * @param line -> assumed to have been sorted by x
     * @return 
     */
    public boolean assimilateLine(ArrayList<StringBlock> line){
        ArrayList<potentialMerger> potentialMergers = new ArrayList<>();
        ArrayList<Column> potentialAdditions = new ArrayList<>();
        ArrayList<Column> matchedColumns = new ArrayList<>();
        ArrayList<Column> lineToBeAssimilated = Column.blocksToColumns(line);
        for(int i = 0; i < line.size(); i++){            
            Column columnToBeAssimilated = lineToBeAssimilated.get(i);
            boolean overlapFound = false;
            //check to see if this Column overlaps other within the same line - thats not acceptable
            for(int j = 0; j < line.size(); j++){
                if(j!=i){
                    StringBlock comparedBlock = line.get(j);
                    Column c = Column.blockToColumn(comparedBlock);            
                    if(c.overlaps(columnToBeAssimilated)){
                        return false;
                    }
                }
            }
            for(Column thisLinesColumn: columns){                
                if(thisLinesColumn.overlaps(columnToBeAssimilated)){
                    overlapFound = true;
                    //If the new Column fits this one its a match so move to the next string block
                    if(thisLinesColumn.fits(columnToBeAssimilated)){
                        if(thisLinesColumn.getTOrN()!=columnToBeAssimilated.getTOrN()) return false;
                        if(matchedColumns.contains(thisLinesColumn)) return false;
                        matchedColumns.add(thisLinesColumn);
                        break;
                    }
                    //check if this strays across neighbouring boundaries
                    if(thisLinesColumn.straysAcrossMyBoundaries(columnToBeAssimilated)) return false;
                    if(matchedColumns.contains(thisLinesColumn)) return false;                    
                    //consider merging the two columns
                    //first check to see if there are any mergers in the offing for this Column - if there are then this is an issue as there are multiple columns that overlap this one 
                    for(potentialMerger this_pm: potentialMergers){
                        if(this_pm.thisLinesColumn==thisLinesColumn) return false;
                    }
                    if(thisLinesColumn.getTOrN()!=columnToBeAssimilated.getTOrN()) return false;
                    if(matchedColumns.contains(thisLinesColumn)) return false;                    
                    potentialMerger pm = new potentialMerger(thisLinesColumn, columnToBeAssimilated);
                    potentialMergers.add(pm);                    
                    matchedColumns.add(thisLinesColumn);
                    break;
                }
            }
            if(overlapFound == false){
                //The Column did not overlap any of the current columns - add it to the potential additional columns list if it is within the existing 
                //bounds of this line
                Column last = columns.get(columns.size()-1);
                if(last.isToTheLeftOf(columnToBeAssimilated)) return false;
                potentialAdditions.add(columnToBeAssimilated);
            }
        }
        //if we got to here then there were no clashes b/w columns in the new line and columns in the next line and the new line has no overlapping columns
        //check for any additions to make
        for(Column potentialAddition: potentialAdditions){
            columns.add(potentialAddition);
        }
        for(potentialMerger merger: potentialMergers){
            merger.thisLinesColumn.merge(merger.columnToBeAssimilated);
        }
        //sort the columns in this line template and then relink them to their neighbours
        Column.sortAndRelink(columns);
        return true;
    }
    
    
    /**
     * A line can be assimilated by another if all the overlapping columns within the line have a common beginning or end 
     * wih a single column in the other line.
     * Assimilation can only happen within the beginning and end of the assimilating line - ie the new line cant have additional 
     * columns outside the columns of the assimilating lines
     * @param line
     * @return 
     */
    public boolean assimilateLineV2(ArrayList<StringBlock> line){
        ArrayList<Column> new_lines_columns = Column.blocksToColumns(line);
        ArrayList<Column> matchedColumns = new ArrayList<>();
        ArrayList<Column> fitsWithinWithoutAMatch = new ArrayList<>();
        ArrayList<potentialMerger> potentialMergers = new ArrayList<>();
        float leftMost = columns.get(0).getA();
        float rightMost = columns.get(columns.size()-1).getB();
        
        //Move through each of the new lines columns
        for(Column c2: new_lines_columns){
            if(c2.getB()<leftMost) return false;
            if(c2.getA()<rightMost) return false;
            boolean matchFound = false;
            for(Column c: columns){
                //If the columns overlap then they need a common beginning or end
                if(c.overlaps(c2)){
                    //should only match a single column
                    if(matchFound) return false;
                    //need to have a common start or end position
                    if(Math.abs(c.getA()-c2.getA())>5 | Math.abs(c.getB()-c2.getB())>5) return false;
                    //might need to widen this column to fit c2, so add as a potential merger
                    potentialMerger pm = new potentialMerger(c, c2);
                    potentialMergers.add(pm);
                    matchFound = true;
                }
            }
            //If no match found then see if they fit within the line
            
            if(matchFound==false){
                //Column(s) within the new line fits the current line b/w gaps in existing columns
                fitsWithinWithoutAMatch.add(c2);
            }
        }
        
        //If it got to here then then these columns will fit nicely within the new line, and we can merge any potential mergers
        //
        
        //if we got to here then there were no clashes b/w columns in the new line and columns in the next line and the new line has no overlapping columns
        //check for any additions to make
        for(Column newColumn: fitsWithinWithoutAMatch){
            columns.add(newColumn);
        }
        
        for(potentialMerger merger: potentialMergers){
            merger.thisLinesColumn.merge(merger.columnToBeAssimilated);
        }

        //sort the columns in this line template and then relink them to their neighbours
        Column.sortAndRelink(columns);
        return true;        
   
    }    
    
    /**
     * The passed line matches this line if the columns in the passed in line fall within the columns in this line perfectly, ie are all contained within the boundaries of existing columns in this line
     * @param line
     * @return 
     */
    public boolean matches(ArrayList<StringBlock> line){
        ArrayList<Column> new_lines_columns = Column.blocksToColumns(line);
        ArrayList<Column> matchedColumns = new ArrayList<>();
        for(Column new_lines_column: new_lines_columns){
            boolean matchFound = false;
            for(Column this_lines_column: columns){
                if(matchedColumns.contains(this_lines_column)) continue;
                if(this_lines_column.fits(new_lines_column)){
                    if(this_lines_column.getTOrN()==new_lines_column.getTOrN()){
                        matchFound = true;
                        matchedColumns.add(this_lines_column);
                        break;
                    }
                }
            }
            if(matchFound==false) return false;
        }
        return true;
    }

    public static String linesToString(Line line){
        StringBuilder sbTOrN = new StringBuilder();
        StringBuilder sbAB = new StringBuilder();
        for(Column column: line.columns){
            sbTOrN.append(column.getTOrN());
            if(sbAB.length()==0){
                sbAB.append("[");
            }else{
                sbAB.append(" ");
            }
            sbAB.append(column.getA() + "," + column.getB());
        }
        sbAB.append("]");
        return sbTOrN.toString() + sbAB.toString();
    }

    public String gettOrNs() {
        StringBuilder sbTOrN = new StringBuilder();
        for(Column column: columns){
            sbTOrN.append(column.getTOrN());
        }        
        return sbTOrN.toString();
    }    
    
    /**
     * One line matches another if each of the columns within that line that overlap have 
     * If line 1 matches line 2, line 2 may not match line 1
     * @param line
     * @return 
     */
    public boolean matches(Line line){
        for(Column c2: line.columns){
            boolean matchFound = false;
            for(Column c: columns){
                if(c.overlap(c2) > 0.9){
                    //should only match a single column
                    if(matchFound) return false;
                    matchFound = true;
                }
            }
        }
        return true;
    }    
    
    /**
     * One line matches another if all overlapping columns contain a common beginning or end.
     * @param line
     * @return 
     */
    public boolean matchesV2(Line line){
        for(Column c2: line.columns){
            boolean matchFound = false;
            for(Column c: columns){
                //If the columns overlap then they need a common beginning or end
                if(c.overlaps(c2)){
                    //should only match a single column
                    if(matchFound) return false;
                    //need to have a common start or end position
                    if(Math.abs(c.getA()-c2.getA())>5 | Math.abs(c.getB()-c2.getB())>5) return false;
                    matchFound = true;
                }
            }
        }
        return true;
    }       
    
    /**
     * One line matches another if all overlapping columns contain a common beginning or end and all columns overlap
     * @param line
     * @return 
     */        
    public boolean matchesV2(ArrayList<StringBlock> line){
        ArrayList<Column> new_lines_columns = Column.blocksToColumns(line);
        ArrayList<Column> matchedColumns = new ArrayList<>();
        for(Column c2: new_lines_columns){
            boolean matchFound = false;
            for(Column c: columns){
                //If the columns overlap then they need a common beginning or end
                if(c.overlaps(c2)){
                    //should only match a single column
                    if(matchFound) return false;
                    //need to have a common start or end position
                    if(Math.abs(c.getA()-c2.getA())>5 | Math.abs(c.getB()-c2.getB())>5) return false;
                    matchFound = true;
                }
            }
            if(matchFound==false) return false;
        }
        return true;
    }    
    
    public ArrayList<StringBlock> apply(ArrayList<StringBlock> line){
        ArrayList<StringBlock> newOne = new ArrayList<StringBlock>();        
        for(int i = 0; i < columns.size(); i++){
            //Is Column found in the blocks?
            Column c = columns.get(i);
            boolean matchFound = false;
            for(StringBlock block: line){
                if(newOne.contains(block)) continue;
                Column c2 = Column.blockToColumn(block);
                if(c.fitsWithinLine(c2)){
                    matchFound = true;
                    newOne.add(block);
                    break;
                }
            }
            if(matchFound==false){
                //add a blank StringBlock to represent this Column
                StringBlock newBlock = c.toBlock();
                newOne.add(newBlock);
            }
        }
        return newOne;
    }
    
    private class potentialMerger{      
        final Column thisLinesColumn;
        final Column columnToBeAssimilated;
        potentialMerger(Column thisLinesColumn, Column columnToBeAssimilated){
            this.thisLinesColumn = thisLinesColumn;
            this.columnToBeAssimilated = columnToBeAssimilated;
        }        
    }
    
    
    //This will seek to standardise the position of columns based on the 
    public static void standardiseEncounteredLines(ArrayList<Line> encounteredLines){
        for(int i = 0; i < encounteredLines.size(); i++){
            Line line = encounteredLines.get(i);
            for(int j = 0; j < encounteredLines.size(); j++){
                if(i!=j){
                    Line otherLine = encounteredLines.get(j);
                    if(line.matches(otherLine)){
                        int iii = 11;
                    }
                }
            }
        }            
    }
    
    private class columnLadder{
        ArrayList<columnLadderRung> rungs;
        private boolean successfulMerge = false;
        columnLadder(ArrayList<Column> columns1, ArrayList<Column> columns2){
            columnLadderRung clrPrior=null;
            for(Column c: columns1){
                columnLadderRung clg = new columnLadderRung();
                clg.addC1(c);
                if(clrPrior!=null){
                    clg.prior = clrPrior;
                    clrPrior.next = clg;
                }                
            }
            
        }
    }
    
    private class columnLadderRung{
        Column c1;
        Column c2;
        float a;
        float b;
        columnLadderRung prior;
        columnLadderRung next;
        columnLadderRung(Column c1, Column c2){
            if(c1!=null){
                this.c1 = c1;
                a=c1.getA();
                b=c1.getB();
                if(c2!=null){
                    this.c2 = c2;
                    if(a< c2.getA()) a = c2.getA();
                    if(b> c2.getB()) b = c2.getB();        
                }
            }
            if(c2!=null){
                this.c2 = c2;
                a = c2.getA();
                b = c2.getB();        
            }            
        }
        
        columnLadderRung(){            
        }
        
        void addC1(Column c1){
            if(c1==null) return;
            this.c1 = c1;
            a=c1.getA();
            b=c1.getB();   
            if(c2!=null){
                if(a< c2.getA()) a = c2.getA();
                if(b> c2.getB()) b = c2.getB();        
            }            
        }
        
        void addC2(Column c2){
            if(c2==null) return;
            this.c2 = c2;
            a=c2.getA();
            b=c2.getB();   
            if(c1!=null){
                if(a< c1.getA()) a = c1.getA();
                if(b> c1.getB()) b = c1.getB();        
            }            
        }        
        
    }
    
}
