/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nexiaem.apachepdfboxbasic;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * @author bjames
 */
public class PDFToText {
    
    public static float defaultRowHeight = 8;           
    
    /**
     * Provides row data written directly to file using default row height
     * @param pdfFile
     * @param pathForParsedOutput
     * @throws IOException 
     */
    
    public static void pdfToText(File pdfFile, BufferedWriter writer) throws IOException{
        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        writer.write(text);
        document.close();
    }    
    
    public static void pdfToTextByArea(File pdfFile, String pathForParsedOutput) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathForParsedOutput)));              
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();                                 
            ArrayList<String> rowText = new ArrayList<>();
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();     
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                writer.write("<NEW PAGE>\r\n");                
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;     
                float numberOfRows = height / defaultRowHeight;   
                int rowCount = 0;
                for(int i = 0; i<numberOfRows;i++){
                    Rectangle2D.Double rect = new Rectangle2D.Double(0, i*defaultRowHeight, width*2, defaultRowHeight);
                    pdfStripper.addRegion( "r" + i, rect);
                    rowCount = i;
                }                
                pdfStripper.extractRegions(page);                
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                for(int i = 0; i<=rowCount;i++){
                    ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                    String text = splitTextPositionListToString(ss.get(0));
                    if(ss.size()>1){
                        System.out.println("more than one list in text position arraylist");
                    }
                    if(text.trim().length()>0){
                        writer.write(text + "\r\n");
                    }
                }            
            }
            document.close();
            writer.close();        
    }         
    
    /**
     * This was developed for the UWCB audit file where word wrapping in a column made it impossible to assign a value to a given row if this was lost by using the row height filter
     * @param pdfFile
     * @param writer
     * @param columnPositions
     * @param prefix - needs to have a tab for spacing
     * @throws IOException 
     */
    public static void pdfToTextByAreaFullPage(File pdfFile, BufferedWriter writer, ArrayList<Float> columnPositions, String prefix) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);             
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();                                 
            ArrayList<String> rowText = new ArrayList<>();               
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();  
                writer.write("<NEW PAGE>\r\n");                
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;     
                Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, width*2, height);
                pdfStripper.addRegion("r", rect);
                pdfStripper.extractRegions(page);                
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                ArrayList<List<TextPosition>> ss = textPositions.get("r");
                if(ss.size()>1){
                    System.out.println("more than one list in text position arraylist");
                }                                       
                ArrayList<ArrayList<StringBlock>> lines = splitFullPageTextPositionListIgnoreSpaces(ss.get(0), columnPositions);
                if(lines!=null){
                    for(ArrayList<StringBlock> line: lines){
                        String text = StringBlock.arrayToStringTabDelimited(line);
                        if(text.length()>0) writer.write(prefix + text + "\r\n");
                    }
                }
            }
            document.close();   
            
    }          
    
    public static void pdfToTextByAreaFullPageIncludeSpaces(File pdfFile, BufferedWriter writer, ArrayList<Float> columnPositions, String prefix) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);             
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();                                 
            ArrayList<String> rowText = new ArrayList<>();               
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();  
                writer.write("<NEW PAGE>\r\n");                
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;     
                Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, width*2, height);
                pdfStripper.addRegion("r", rect);
                pdfStripper.extractRegions(page);                
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                ArrayList<List<TextPosition>> ss = textPositions.get("r");
                if(ss.size()>1){
                    System.out.println("more than one list in text position arraylist");
                }                                       
                ArrayList<ArrayList<StringBlock>> lines = splitFullPageTextPositionList(ss.get(0), columnPositions);
                if(lines!=null){
                    for(ArrayList<StringBlock> line: lines){
                        String text = StringBlock.arrayToStringTabDelimited(line);
                        if(text.length()>0) writer.write(prefix + text + "\r\n");
                    }
                }
            }
            document.close();   
            
    }             
    
    /**
     * Provides row data written directly to file using specified row height.
     * @param pdfFile
     * @param pathForParsedOutput
     * @param rowHeight
     * @throws IOException 
     */
    public static void pdfToTextByArea(File pdfFile, String pathForParsedOutput, float rowHeight) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathForParsedOutput)));              
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();                                 
            ArrayList<String> rowText = new ArrayList<>();
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();     
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                writer.write("<NEW PAGE>\r\n");
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;     
                float numberOfRows = height / rowHeight;   
                int rowCount = 0;
                for(int i = 0; i<numberOfRows;i++){
                    Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                    pdfStripper.addRegion( "r" + i, rect);
                    rowCount = i;
                }                
                pdfStripper.extractRegions(page);                
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                for(int i = 0; i<=rowCount;i++){
                    ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                    String text = splitTextPositionListToString(ss.get(0));
                    if(ss.size()>1){
                        System.out.println("more than one list in text position arraylist");
                    }
                    if(text.trim().length()>0){
                        writer.write(text + "\r\n");
                    }
                }            
            }
            document.close();
            writer.close();        
    }     

    
    public static void pdfToTextByAreaIgnoreSpacesSortedBlocksStandardisedColumns(File pdfFile, String pathForParsedOutput, float rowHeight) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);
            ArrayList<ArrayList<StringBlock>> lines = new ArrayList<>();
            ArrayList<Line> encounteredLines = new ArrayList<>();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathForParsedOutput)));              
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                StringBlock newLineBlock = new StringBlock(0, 1, "<NEW PAGE>");
                ArrayList<StringBlock> newPageLine = new ArrayList<>();
                newPageLine.add(newLineBlock);
                lines.add(newPageLine);
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;
                float numberOfRows = height / rowHeight;
                int rowCount = 0;
                for(int i = 0; i<numberOfRows;i++){
                    Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                    pdfStripper.addRegion( "r" + i, rect);
                    rowCount = i;
                }                
                pdfStripper.extractRegions(page);                
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                for(int i = 0; i<=rowCount;i++){
                    ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                    ArrayList<StringBlock> asb = splitTextPositionListIgnoreSpaces(ss.get(0));
                    if(asb!=null){
                        Collections.sort(asb);
                        String text = StringBlock.arrayToStringTabDelimited(asb);
                        if(ss.size()>1){
                            System.out.println("more than one list in text position arraylist");
                        }
                        if(text.trim().length()>0){
                            lines.add(asb);
                            boolean lineMatched = false;
                            for(Line line: encounteredLines){
                                if(line.assimilateLine(asb)){
                                    lineMatched = true;
                                    break;
                                }                 
                            }
                            if(!lineMatched){
                                Line newLine = new Line(asb);
                                encounteredLines.add(newLine);
                            }                        
                        }
                    }
                }            
            }
            
            //Now have a list of String block lines.  Each of these has a x and an xend that represent the position of the string block on the page
            //the next code block seeks to see if there are any common x/xend positions for these blocks - where x would be left and xend right justified
            standardiseJustifiedColumns(lines, encounteredLines);
            for(ArrayList<StringBlock> asb: lines){
                String text =  StringBlock.arrayToStringTabDelimited(asb);
                writer.write(text + "\r\n");
            }
            document.close();
            writer.close();        
    }       
    
    public static void setPatternsForPDF(File pdfFile, float rowHeight, ArrayList<Line> encounteredLines) throws IOException{
        
        PDDocument document = PDDocument.load(pdfFile);      
        int rowNumber = 0;
        //Create the parsed PDF File
        int numberOfPages = document.getNumberOfPages();
        PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
        for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
            PDPage page = document.getPage(pageNumber);
            float height = page.getMediaBox().getHeight();
            float width = page.getMediaBox().getWidth()*2;
            float numberOfRows = height / rowHeight;
            int rowCount = 0;
            for(int i = 0; i<numberOfRows;i++){
                Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                pdfStripper.addRegion( "r" + i, rect);
                rowCount = i;
            }                
            pdfStripper.extractRegions(page);                
            Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
            for(int i = 0; i<=rowCount;i++){
                ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                if(ss.size()>1){
                    System.out.println("more than one list in text position arraylist - this is unexpected and may lead to errors parsing the file");
                }                    
                List<TextPosition> ss0 = ss.get(0);
                if(ss0.size()==0) continue;
                ArrayList<StringBlock> asb = splitTextPositionListIgnoreSpaces(ss.get(0));
                if(asb!=null){                                 
                    rowNumber++;
                    Collections.sort(asb);
                    boolean lineMatched = false;
                    for(int j = 0; j < encounteredLines.size(); j++){
                        Line line = encounteredLines.get(j);
                        if(line.matches(asb)){
                            lineMatched = true;
                            break;
                        }                         
                    }
                    if(!lineMatched){
                        for(int j = 0; j < encounteredLines.size();j++){
                            Line line = encounteredLines.get(j);
                            if(line.assimilateLine(asb)){
                                if(!line.matches(asb)){
                                    System.out.println("Issue - pattern post assimilation does not match the assimilated line ");
                                }                           
                                lineMatched = true;
                                break;
                            }                                             
                        }
                    }
                    if(!lineMatched){
                        Line newLine = new Line(asb);
                        encounteredLines.add(newLine);
                    }                                            
                }
            } 
        }            
        document.close();   
    }      
    
    public static int setPatternsForPDFConsolidateColumns(File pdfFile, float rowHeight, ArrayList<Line> encounteredLines, HashMap<Integer, Integer> abcounts) throws IOException{
        
        ArrayList<Integer[]> countsForSorting = new ArrayList<>();
        int totalCount = 0;
        int maxCount = 0;
        for(int aOrB: abcounts.keySet()){
            int aOrBCount = abcounts.get(aOrB);
            Integer[] thisOne = new Integer[2];
            thisOne[0] = aOrB;
            thisOne[1] = aOrBCount; 
            totalCount = totalCount + aOrBCount;
            if(aOrBCount > maxCount) maxCount = aOrBCount;
            countsForSorting.add(thisOne);
        }

        //Sort by the count - use p2 compared to p1 to sort in reverse 
        Collections.sort(countsForSorting, (Integer[] p1, Integer[] p2) -> p2[1].compareTo(p1[1]));         
        
        int topTenPCValue = 0;
        int topTenPCCount = 0;
        
        //Get the values for the top 10%
        for(int i = 0; i < (countsForSorting.size() / 10);i++){
            Integer[] thisN = countsForSorting.get(i);
            topTenPCValue = topTenPCValue + thisN[1];
            topTenPCCount++;
        }

        int countThreshold = (topTenPCValue/topTenPCCount) / 5;
        
        PDDocument document = PDDocument.load(pdfFile);      
        int rowNumber = 0;
        //Create the parsed PDF File
        int numberOfPages = document.getNumberOfPages();
        PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
        int integrationCount = 0; 
        for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
            PDPage page = document.getPage(pageNumber);
            float height = page.getMediaBox().getHeight();
            float width = page.getMediaBox().getWidth()*2;
            float numberOfRows = height / rowHeight;
            int rowCount = 0;
            for(int i = 0; i<numberOfRows;i++){
                Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                pdfStripper.addRegion( "r" + i, rect);
                rowCount = i;
            }                
            pdfStripper.extractRegions(page);                
            Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();            
           
            for(int i = 0; i<=rowCount;i++){
                ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                if(ss.size()>1){
                    System.out.println("more than one list in text position arraylist - this is unexpected and may lead to errors parsing the file");
                }                    
                List<TextPosition> ss0 = ss.get(0);
                if(ss0.size()==0) continue;
                ArrayList<StringBlock> asb = splitTextPositionListIgnoreSpaces(ss.get(0));
                if(asb!=null){                                      
                    rowNumber++;
                    Collections.sort(asb);
                    
                    //Now we consolidate the columns 
                    StringBlock priorBlock = null;
                    ArrayList<StringBlock> newASB = new ArrayList<>();                    
                    for(StringBlock sb: asb){
                        if(priorBlock==null){
                            priorBlock = sb;
                        }else{
                            //Get the a and b for this block
                            int a = (int)sb.getX();
                            int b = (int)sb.getXEnd();
                            //See which is the most common
                            int countA = abcounts.get(a);
                            int countB = abcounts.get(b);
                            if(countB > countA) countA = countB;
                            //Compare to the threshold
                            if(countA < countThreshold){
                                //merge the blocks
                                priorBlock.integrate(sb);
                                integrationCount++;
                            }else{           
                                newASB.add(priorBlock);
                                priorBlock = sb;
                            }
                        }                        
                    }
                    newASB.add(priorBlock);
                    
                    boolean lineMatched = false;
                    for(int j = 0; j < encounteredLines.size(); j++){
                        Line line = encounteredLines.get(j);
                        if(line.matchesV2(newASB)){
                            lineMatched = true;
                            break;
                        }                         
                    }
                    if(!lineMatched){
                        for(int j = 0; j < encounteredLines.size();j++){
                            Line line = encounteredLines.get(j);
                            if(line.assimilateLine(newASB)){
                                if(!line.matches(newASB)){
                                    System.out.println("Issue - pattern post assimilation does not match the assimilated line ");
                                }                           
                                lineMatched = true;
                                break;
                            }                                             
                        }
                    }
                    if(!lineMatched){
                        Line newLine = new Line(newASB);
                        encounteredLines.add(newLine);
                    }                                            
                }
            } 
        }            
        document.close();   
        return countThreshold;
    }      
    
    public static HashMap<Integer, Integer> getABCounts(File pdfFile, float rowHeight) throws IOException{
        HashMap<Integer, Integer> abcounts = new HashMap<>();
        
        PDDocument document = PDDocument.load(pdfFile);      
        int rowNumber = 0;
        //Create the parsed PDF File
        int numberOfPages = document.getNumberOfPages();
        PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
        for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
            PDPage page = document.getPage(pageNumber);
            float height = page.getMediaBox().getHeight();
            float width = page.getMediaBox().getWidth()*2;
            float numberOfRows = height / rowHeight;
            int rowCount = 0;
            for(int i = 0; i<numberOfRows;i++){
                Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                pdfStripper.addRegion( "r" + i, rect);
                rowCount = i;
            }                
            pdfStripper.extractRegions(page);                
            Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
            for(int i = 0; i<=rowCount;i++){
                ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                if(ss.size()>1){
                    System.out.println("more than one list in text position arraylist - this is unexpected and may lead to errors parsing the file");
                }                    
                List<TextPosition> ss0 = ss.get(0);
                if(ss0.size()==0) continue;
                ArrayList<StringBlock> asb = splitTextPositionListIgnoreSpaces(ss.get(0));
                if(asb!=null){
                    for(StringBlock block: asb){
                        int a = (int)block.getX();
                        int b = (int)block.getXEnd();
                        Integer aCount = abcounts.get(a);
                        if(aCount==null) aCount = 0;
                        aCount++;
                        abcounts.put(a, aCount);
                        Integer bCount = abcounts.get(b);
                        if(bCount==null) bCount = 0;
                        bCount++;
                        abcounts.put(b, bCount);                    
                    }                                        
                    rowNumber++;                                     
                }
            } 
        }            
        document.close();   
        return abcounts;
    }          
    
    public static void pdfLinePatternToText(File pdfFile, BufferedWriter writer, float rowHeight, ArrayList<Line> patternsToApply) throws IOException{
            PDDocument document = PDDocument.load(pdfFile);
            writer.write("<FileName:" + pdfFile.getName()+">\r\n");
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                writer.write("<NEW PAGE>\r\n");
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;
                float numberOfRows = height / rowHeight;
                int rowCount = 0;
                for(int i = 0; i<numberOfRows;i++){
                    Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                    pdfStripper.addRegion( "r" + i, rect);
                    rowCount = i;
                }                
                pdfStripper.extractRegions(page);
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                for(int i = 0; i<=rowCount;i++){
                    ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                    ArrayList<StringBlock> asb = splitTextPositionListIgnoreSpaces(ss.get(0));
                    if(asb!=null){
                        Collections.sort(asb);
                        Line mostGranularMatchedLine = null;
                        int matchedIndex = 0;
                        int mostColumnsMatched = 0;
                        for(int j = 0; j < patternsToApply.size(); j++){
                            Line line = patternsToApply.get(j);
                            if(line.columnCount()>mostColumnsMatched){
                                if(line.matches(asb)){
                                    matchedIndex = j;
                                    mostColumnsMatched = line.columnCount();
                                    mostGranularMatchedLine = line;
                                    break;
                                }
                            }
                        }
                        if(mostColumnsMatched> 0){
                            ArrayList<StringBlock> newAsb = mostGranularMatchedLine.apply(asb);
                            String text =  StringBlock.arrayToStringTabDelimited(newAsb);     
                            writer.write("P" + matchedIndex + "\t" + mostGranularMatchedLine.gettOrNs() + "\t" + text + "\r\n");
                        }else{
                            String text =  StringBlock.arrayToStringTabDelimited(asb);
                            writer.write("Px\t" + text + "\r\n");                            
                            System.out.println("In standardiseJustifiedColumns, no lines matched, which shouldnt be possible so something is wrong with the algorithm");
                        }                                                
                    }
                }
            }
            document.close();
    }     
    
    public static void pdfLinePatternToTextStandardiseColumns(File pdfFile, BufferedWriter writer, float rowHeight, ArrayList<Line> patternsToApply, HashMap<Integer, Integer> abcounts, int countThreshold) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);
            writer.write("<FileName:" + pdfFile.getName()+">\r\n");
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                writer.write("<NEW PAGE>\r\n");
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;
                float numberOfRows = height / rowHeight;
                int rowCount = 0;
                for(int i = 0; i<numberOfRows;i++){
                    Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                    pdfStripper.addRegion( "r" + i, rect);
                    rowCount = i;
                }                
                pdfStripper.extractRegions(page);
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                for(int i = 0; i<=rowCount;i++){
                    ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                    ArrayList<StringBlock> asb = splitTextPositionListIgnoreSpaces(ss.get(0));
                    if(asb!=null){
                        Collections.sort(asb);
                        
                        //Now we consolidate the columns 
                        StringBlock priorBlock = null;
                        int integrationCount = 0;
                        ArrayList<StringBlock> newASB = new ArrayList<>();                    
                        for(StringBlock sb: asb){
                            if(priorBlock==null){
                                priorBlock = sb;
                            }else{
                                //Get the a and b for this block
                                int a = (int)sb.getX();
                                int b = (int)sb.getXEnd();
                                //See which is the most common
                                int countA = abcounts.get(a);
                                int countB = abcounts.get(b);
                                if(countB > countA) countA = countB;
                                //Compare to the threshold
                                if(countA < countThreshold){
                                    //merge the blocks
                                    priorBlock.integrate(sb);
                                    integrationCount++;                                            
                                }else{           
                                    newASB.add(priorBlock);
                                    priorBlock = sb;
                                }
                            }                        
                        }
                        newASB.add(priorBlock);                        
                        
                        Line mostGranularMatchedLine = null;
                        int matchedIndex = 0;
                        int mostColumnsMatched = 0;
                        for(int j = 0; j < patternsToApply.size(); j++){
                            Line line = patternsToApply.get(j);
                            if(line.columnCount()>mostColumnsMatched){
                                if(line.matches(newASB)){
                                    matchedIndex = j;
                                    mostColumnsMatched = line.columnCount();
                                    mostGranularMatchedLine = line;
                                    break;
                                }
                            }
                        }
                        if(mostColumnsMatched> 0){
                            ArrayList<StringBlock> newAsb = mostGranularMatchedLine.apply(newASB);
                            String text =  StringBlock.arrayToStringTabDelimited(newAsb);     
                            writer.write("P" + matchedIndex + "\t" + mostGranularMatchedLine.gettOrNs() + "\t" + text + "\r\n");
                        }else{
                            String text =  StringBlock.arrayToStringTabDelimited(newASB);
                            writer.write("Px\t" + text + "\r\n");                            
                            System.out.println("In standardiseJustifiedColumns, no lines matched, which shouldnt be possible so something is wrong with the algorithm");
                        }                                                
                    }
                }
            }
            document.close();
    }

    private static int checkIfMatchExists(ArrayList<Line> patternsToApply, ArrayList<StringBlock> asb){
        for(int j = 0; j < patternsToApply.size(); j++){
            Line line = patternsToApply.get(j);
            if(line.matches(asb)){
                return j;
            }            
        }    
        return -1;
    }
    
    
    private static void standardiseJustifiedColumns(ArrayList<ArrayList<StringBlock>> lines, ArrayList<Line> encounteredLines){
        try{
            //Conform each line to a line template
            for(int i = 0; i < lines.size(); i++){
                ArrayList<StringBlock> asb = lines.get(i);
                if(asb.size()==1&&asb.get(0).getText().equals("<NEW PAGE>")) continue;
                boolean applied = false;
                Line mostGranularMatchedLine = null;
                int matchedIndex = 0;
                int mostColumnsMatched = 0;
                for(int j = 0; j < encounteredLines.size(); j++){
                    Line line = encounteredLines.get(j);
                    if(line.columnCount()>mostColumnsMatched){
                        if(line.matches(asb)){
                            matchedIndex = j;
                            mostColumnsMatched = line.columnCount();
                            mostGranularMatchedLine = line;
                        }
                    }
                }
                if(mostColumnsMatched> 0){
                    ArrayList<StringBlock> newASB = mostGranularMatchedLine.apply(asb);
                    newASB.add(0, new StringBlock(0, 0, "Line pattern" + matchedIndex));
                    lines.set(i, newASB);
                    applied = true;                  
                }else{
                    System.out.println("In standardiseJustifiedColumns, no lines matched, which shouldnt be possible so something is wrong with the algorithm");
                }
            }                
            
        }catch(Exception e){
            throw e;
        }   
    }
    
    
    public static void pdfToTextByAreaIgnoreSpaces(File pdfFile, BufferedWriter writer, float rowHeight) throws IOException{

            PDDocument document = PDDocument.load(pdfFile);
            
            //Create the parsed PDF File
            int numberOfPages = document.getNumberOfPages();                                 
            ArrayList<String> rowText = new ArrayList<>();
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();     
            for(int pageNumber = 0; pageNumber < numberOfPages; pageNumber++){
                writer.write("<NEW PAGE>\r\n");
                PDPage page = document.getPage(pageNumber);
                float height = page.getMediaBox().getHeight();
                float width = page.getMediaBox().getWidth()*2;     
                float numberOfRows = height / rowHeight;   
                int rowCount = 0;
                for(int i = 0; i<numberOfRows;i++){
                    Rectangle2D.Double rect = new Rectangle2D.Double(0, i*rowHeight, width*2, rowHeight);
                    pdfStripper.addRegion( "r" + i, rect);
                    rowCount = i;
                }                
                pdfStripper.extractRegions(page);                
                Map<String, ArrayList<List<TextPosition>>> textPositions = pdfStripper.getRegionCharacterList();                
                for(int i = 0; i<=rowCount;i++){
                    ArrayList<List<TextPosition>> ss = textPositions.get("r" + i);
                    String text = splitTextPositionListToStringIgnoreSpaces(ss.get(0));
                    if(ss.size()>1){
                        System.out.println("more than one list in text position arraylist");
                    }
                    if(text.trim().length()>0){
                        writer.write(text + "\r\n");
                    }
                }            
            }
            document.close();
    }         

    private static String splitTextPositionListToString(List<TextPosition> texts){
        StringBuilder sb = new StringBuilder();
        float prevXPlusWidth = -1;
        for(TextPosition text: texts){            
            String chars = text.toString();            
            float x = text.getX();
            float width = text.getWidth();
            if(prevXPlusWidth == -1) prevXPlusWidth = x;
            float widthBetweenThisAndPriorCharacter = x - prevXPlusWidth;
            float spaceWidth = text.getWidthOfSpace();
            if(widthBetweenThisAndPriorCharacter > 0.8 * spaceWidth){   
                //Either a space or a tab
                if(widthBetweenThisAndPriorCharacter > 1.5 * spaceWidth){
                    //tab
                    sb.append("\t" + chars);
                }else{
                    //space
                    sb.append(" " + chars);
                }
            }else{
                if(widthBetweenThisAndPriorCharacter< -0.8*spaceWidth ){
                    //tab
                    sb.append("\t" + chars);
                }else{
                    sb.append(chars);
                }
            }
            prevXPlusWidth = x + text.getWidth();
        }
        return sb.toString();
    }
    
    private static class compareTextPositionByX implements Comparator<TextPosition>{

        @Override
        public int compare(TextPosition o1, TextPosition o2) {
            float x1 = o1.getX();
            float x2 = o2.getX();
            if(x1>x2){
                return 1;
            }else if(x2>x1){
                return -1;
            }
            return 0;
        }
        
    }
    
    private static String splitSortedTextPositionListToString(List<TextPosition> texts){
        Collections.sort(texts, new compareTextPositionByX());
        StringBuilder sb = new StringBuilder();
        float prevXPlusWidth = -1;
        for(TextPosition text: texts){            
            String chars = text.toString();
            float x = text.getX();
            float width = text.getWidth();
            if(prevXPlusWidth == -1) prevXPlusWidth = x;
            float widthBetweenThisAndPriorCharacter = x - prevXPlusWidth;
            float spaceWidth = text.getWidthOfSpace();
            if(widthBetweenThisAndPriorCharacter > 0.8 * spaceWidth){   
                //Either a space or a tab
                if(widthBetweenThisAndPriorCharacter > 1.5 * spaceWidth){
                    //tab
                    sb.append("\t" + chars);
                }else{
                    //space
                    sb.append(" " + chars);
                }
            }else{
                if(widthBetweenThisAndPriorCharacter< -0.8*spaceWidth ){
                    //tab
                    sb.append("\t" + chars);
                }else{
                    sb.append(chars);
                }
            }
            prevXPlusWidth = x + text.getWidth();
        }
        String retval = sb.toString();
        return sb.toString();
    }    
    
    private static String splitTextPositionListToStringIgnoreSpaces(List<TextPosition> texts){
        StringBuilder sb = new StringBuilder();
        float prevXPlusWidth = -1;
        for(TextPosition text: texts){            
            String chars = text.toString();  
            if(!chars.equals(" ")){
                float x = text.getX();
                float width = text.getWidth();
                if(prevXPlusWidth == -1) prevXPlusWidth = x;
                float widthBetweenThisAndPriorCharacter = x - prevXPlusWidth;
                float spaceWidth = text.getWidthOfSpace();
                if(widthBetweenThisAndPriorCharacter > 0.8 * spaceWidth){   
                    //Either a space or a tab
                    if(widthBetweenThisAndPriorCharacter > 1.5 * spaceWidth){
                        //tab
                        sb.append("\t" + chars);
                    }else{
                        //space
                        sb.append(" " + chars);
                    }
                }else{
                    if(widthBetweenThisAndPriorCharacter< -0.8*spaceWidth ){
                        //tab
                        sb.append("\t" + chars);
                    }else{
                        sb.append(chars);
                    }
                }
                prevXPlusWidth = x + text.getWidth();
            }
        }
        return sb.toString();
    }    
    
    private static ArrayList<StringBlock> splitTextPositionListIgnoreSpaces(List<TextPosition> texts){
        StringBuilder sb = new StringBuilder();
        float prevXPlusWidth = -1;
        float leftX = -1;   
        float rightX = -1;
        int charCount = 0;
        ArrayList<StringBlock> blocks = new ArrayList<>();
        for(TextPosition text: texts){  
            String chars = text.toString();
            if(!chars.equals(" ")){        
                charCount = charCount + chars.length();
                float x = text.getX();
                rightX = x + text.getWidth();
                if(leftX == -1){
                    leftX = x;
                }
                float width = text.getWidth();
                if(prevXPlusWidth == -1) prevXPlusWidth = x;
                float widthBetweenThisAndPriorCharacter = x - prevXPlusWidth;
                float spaceWidth = text.getWidthOfSpace();

                if(widthBetweenThisAndPriorCharacter > 0.3 * spaceWidth){   
                    //Either a space or a tab
                    if(widthBetweenThisAndPriorCharacter > 1.5 * spaceWidth){
                        //tab
                        if(sb.length() > 0){
                            StringBlock thisBlock = new StringBlock(leftX, prevXPlusWidth, sb.toString());
                            blocks.add(thisBlock);
                        }                    
                        sb = new StringBuilder(chars);
                        leftX = x;
                    }else{
                        //space
                        sb.append(" " + chars);
                    }
                }else{
                    if(widthBetweenThisAndPriorCharacter< -0.8*spaceWidth ){
                        //tab
                        if(sb.length() > 0){
                            StringBlock thisBlock = new StringBlock(leftX, prevXPlusWidth, sb.toString());
                            blocks.add(thisBlock);
                        }
                        sb = new StringBuilder(chars);
                        leftX = x;
                    }else{
                        sb.append(chars);
                    }
                }
                prevXPlusWidth = x + text.getWidth();
            }
        }
        if(sb.length() > 0){
            StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
            blocks.add(thisBlock);
        }
        if(charCount > 0) return blocks;
        return null;
    }        
    
    /**
     * This assumes that lines will come in with columns in sequence, with a character within a space of the leftmost column triggering a new line
     * and a character that cross over a column position triggering a new stringblock.
     * IT assumes that the first and final columns are always on each line and within their column position boundaries
     * @param texts
     * @param columnPositions
     * @return 
     */
    private static ArrayList<ArrayList<StringBlock>> splitFullPageTextPositionListIgnoreSpaces(List<TextPosition> texts, ArrayList<Float> columnPositions){
        StringBuilder sb = new StringBuilder();
        float prevXPlusWidth = -1;
        float leftX = -1;   
        float rightX = -1;
        int charCount = 0;
        int currentColumn = 1;
        ArrayList<ArrayList<StringBlock>> lines = new ArrayList<>();
        ArrayList<StringBlock> line = new ArrayList<>();
        for(TextPosition text: texts){  
            String chars = text.toString();
            if(!chars.equals(" ")){        
                charCount = charCount + chars.length();
                float x = text.getX();

                //If in the last column and x crosses back to the first column then create a new string block and a new line
                if(currentColumn==columnPositions.size()){
                    if(x < columnPositions.get(1)){
                        StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
                        sb = new StringBuilder();                        
                        line.add(thisBlock);
                        lines.add(line);
                        line = new ArrayList<>();
                        currentColumn = 1;
                    }
                }else{
                    //If x cross from one column to the next then create a new string block                
                    if(x > columnPositions.get(currentColumn)){
                        StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
                        sb = new StringBuilder();
                        line.add(thisBlock);
                        currentColumn = currentColumn + 1;
                    }
                }                
                
                rightX = x + text.getWidth();
                if(leftX == -1){
                    leftX = x;
                }
                float width = text.getWidth();
                if(prevXPlusWidth == -1) prevXPlusWidth = x;
                float widthBetweenThisAndPriorCharacter = x - prevXPlusWidth;
                float spaceWidth = text.getWidthOfSpace();
                if(widthBetweenThisAndPriorCharacter > 0.3 * spaceWidth){   
                    //add a space
                    sb.append(" " + chars);
                }else{
                    if(widthBetweenThisAndPriorCharacter< -0.8*spaceWidth ){
                        //add a space
                        sb.append(" " + chars);                        
                        if(leftX > x) leftX = x;
                    }else{
                        sb.append(chars);
                    }
                }
                prevXPlusWidth = x + text.getWidth();
            }
        }
        if(sb.length() > 0){
            StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
            line.add(thisBlock);
            lines.add(line);
        }
        if(charCount > 0) return lines;
        return null;
    }        

    private static ArrayList<ArrayList<StringBlock>> splitFullPageTextPositionList(List<TextPosition> texts, ArrayList<Float> columnPositions){
        StringBuilder sb = new StringBuilder();
        float prevXPlusWidth = -1;
        float leftX = -1;   
        float rightX = -1;
        int charCount = 0;
        int currentColumn = 1;
        ArrayList<ArrayList<StringBlock>> lines = new ArrayList<>();
        ArrayList<StringBlock> line = new ArrayList<>();
        for(TextPosition text: texts){  
            String chars = text.toString();
       
                charCount = charCount + chars.length();
                float x = text.getX();

                //If in the last column and x crosses back to the first column then create a new string block and a new line
                if(currentColumn==columnPositions.size()){
                    if(x < columnPositions.get(1)){
                        StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
                        sb = new StringBuilder();                        
                        line.add(thisBlock);
                        lines.add(line);
                        line = new ArrayList<>();
                        currentColumn = 1;
                    }
                }else{
                    //If x cross from one column to the next then create a new string block                
                    if(x > columnPositions.get(currentColumn)){
                        StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
                        sb = new StringBuilder();
                        line.add(thisBlock);
                        currentColumn = currentColumn + 1;
                    }
                }                
                
                rightX = x + text.getWidth();
                if(leftX == -1){
                    leftX = x;
                }
                float width = text.getWidth();
                if(prevXPlusWidth == -1) prevXPlusWidth = x;
                float widthBetweenThisAndPriorCharacter = x - prevXPlusWidth;
                float spaceWidth = text.getWidthOfSpace();
                if(widthBetweenThisAndPriorCharacter > 0.3 * spaceWidth){   
                    //add a space
                    sb.append(" " + chars);
                }else{
                    if(widthBetweenThisAndPriorCharacter< -2*spaceWidth ){
                        //add a space
                        sb.append("\t" + chars);                        
                        if(leftX > x) leftX = x;
                    }else{
                        sb.append(chars);
                    }
                }
                prevXPlusWidth = x + text.getWidth();
            
        }
        if(sb.length() > 0){
            StringBlock thisBlock = new StringBlock(leftX, rightX, sb.toString());
            line.add(thisBlock);
            lines.add(line);
        }
        if(charCount > 0) return lines;
        return null;
    }               
   
}
