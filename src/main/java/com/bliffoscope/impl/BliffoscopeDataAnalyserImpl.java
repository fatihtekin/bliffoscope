package com.bliffoscope.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import com.bliffoscope.BliffoscopeDataAnalyser;
import com.bliffoscope.ImageAnalyserService;
import com.bliffoscope.log.Log;

@Service(value="bliffoscopeDataAnalyser")
public class BliffoscopeDataAnalyserImpl implements BliffoscopeDataAnalyser{

    @Autowired
    private ImageAnalyserService imageAnalyserService;
    
    @Log 
    private Logger logger;
    
    /**
     * Searches for the target object and lists the coordinates with the probability
     * 
     * We have used Binomial distribution instead of Poisson since An exact probability of
     * an event happening is given, or implied, in the question, 
     * and we are asked to calculate the probability of this event happening k times out of n,
     * then the Binomial Distribution must be used.
     *
     * On the other hand If a mean or average probability of an event happening per unit time/per page/per mile cycled etc., was given, and 
     * we were asked to calculate a probability of n events happening in a given time/number of pages/number of miles cycled,
     * then the Poisson Distribution would have been used.
     *
     * reference @link(http://personal.maths.surrey.ac.uk/st/J.Deane/Teach/se202/poiss_bin.html)
     * 
     * @param dataLocation the whole dataset file location that we search the object in 
     * @param targetImageLocation image file location of the target object like torpido or starship
     * @param alpha is the confidence level in to calculate binomial confidence interval 
     * for images this is 99.9D however we can also use 99.5D to be able to find more images
     * we are also using the upper limit of the confedence level
     * @throws IOException 
     * @throws  
     */
    public Map<String, Double> searchForTargetObject(String dataLocation, String targetImageLocation,double alpha){
        
        boolean[][] data = getImageAsBoolean2DArray(dataLocation);
        boolean[][] targetImage = imageAnalyserService.trimTargetImage(getImageAsBoolean2DArray(targetImageLocation));
        
        double totalPoints = targetImage.length*targetImage[0].length;
        Map<String,Integer> coordinateMatchedPointCountMap =  Collections.synchronizedSortedMap(new TreeMap<String,Integer>());
        double binomialConfidencePointCountMax = BinomialConfidenceCalc.calcBin(totalPoints/2, totalPoints,alpha)[1]*totalPoints;
        logger.info("Binomial Confidence Upper Limit is {}",binomialConfidencePointCountMax);
        Map<String,Future<Integer>> callablesMap = new HashMap<String, Future<Integer>>();
        int columnCount = 0;
        while(columnCount < data[0].length)
        {   
            int lineCount = 0;
            while (lineCount<data.length) {
                try{                    
                    Future<Integer> matchedPoints = imageAnalyserService.getMatchedPoints(targetImage, data,lineCount,columnCount);
                    callablesMap.put("["+lineCount+","+columnCount+"]",matchedPoints);
                    lineCount++;
                }catch(TaskRejectedException tre){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.error("Execution has interrupted.",e);
                        throw new RuntimeException(e);
                    }
                    logger.debug("Task is rejected will take a break of 10 milisecond");
                }
                
            }
            columnCount++;
        }

        for (String coordinates : callablesMap.keySet()) {
            Integer matchedPoints = null;
            try {
                matchedPoints = callablesMap.get(coordinates).get();
            } catch (Exception e) {
                throw new RuntimeException("Could process one of the frames:"+coordinates,e);
            } 
            if(((double)matchedPoints) >= binomialConfidencePointCountMax){                    
                coordinateMatchedPointCountMap.put(coordinates,(matchedPoints));
            }
        }
        
        List<Entry<String, Integer>> coordinateMatchedPointCountList = entriesSortedByValues(coordinateMatchedPointCountMap);
        
        Map<String,Double> foundTargetObjectsMap =  new HashMap<String, Double>();

        for (Entry<String, Integer> entry : coordinateMatchedPointCountList) {
            foundTargetObjectsMap.put(entry.getKey(), round((entry.getValue()/totalPoints),3));
        }  
        
        return foundTargetObjectsMap;
    }
    
    private static boolean[][] getImageAsBoolean2DArray(String location){
        
        List<boolean[]> matrixList = new ArrayList<boolean[]>();        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(location));
            String line = null;
            int maxColumnLength = 0;
            while ((line = br.readLine()) != null) {
                char[] lineChars = line.toCharArray();
                boolean[] lineArray = new boolean[lineChars.length];
                maxColumnLength = maxColumnLength < lineChars.length ? lineChars.length: maxColumnLength;
                for (int i = 0; i < lineChars.length; i++) {
                    if(lineChars[i] == ' '){
                        lineArray[i] = false;
                    }else{
                        lineArray[i] = true;                        
                    }                    
                }
                matrixList.add(lineArray);
            }
            boolean[][] data = new boolean[matrixList.size()][maxColumnLength];
            int counter = 0;
            for(int j=0;j<matrixList.size();j++){
                for (int i = 0; i < matrixList.get(j).length; i++) {
                    data[counter][i] = matrixList.get(j)[i];
                }
                counter++;
            }
            return data;

        }catch(Exception e){
            throw new RuntimeException("Could read file from location: "+location);
        }
        finally {
            if(br!=null){
                try {
                    br.close();                    
                } catch (Exception e) {
                    System.out.println("Could not close the file in location:"+location);                    
                }
            }
        }
    }
    
    /**
     * to compare a map by the values descending
     * @param map source data
     * @return sorted list by value descending
     */
    static <K,V extends Comparable<? super V>> 
    List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
        
        Collections.sort(sortedEntries, 
                new Comparator<Entry<K,V>>() {
            
            public int compare(Entry<K,V> e1, Entry<K,V> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        }
                );

        return sortedEntries;
    }
    
    /**
     * http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
//  private static void searchForTargetObject(boolean[][] testData, boolean[][] targetData) {
//
//      boolean[][] validPoints = getValidPoints(targetData);
//      
//      int columnCount = 0;
//      Integer maxmatchedpoints= 0;
//      String maxmatchedcoor="";
//      while(columnCount < testData[0].length)
//      {   
//          int lineCount = 0;
//          while (lineCount<testData.length) {
//              boolean[][] frame = new boolean[targetData.length][targetData[0].length];
//              int frameLine = 0;
//              for (int line = lineCount; (line < lineCount+targetData.length && line < testData.length) ; line++) {
//                  int frameColumn = 0;
//                  for(int column = columnCount; column < columnCount+targetData[0].length && column < testData[0].length; column++){
//                      frame[frameLine][frameColumn] = testData[line][column];
//                      frameColumn++;
//                  }
//                  frameLine++;
//              }
//              int matchedPoints = ImageAnalyser.getMatchedPoints(targetData, frame,validPoints);
//              //System.out.println("["+lineCount+","+columnCount+"], "+matchedPoints);
//              if(maxmatchedpoints < matchedPoints){
//                  maxmatchedpoints = matchedPoints;
//                  maxmatchedcoor = "["+(lineCount+1)+","+columnCount+"], ";
//              }
//              lineCount++;
//          }
//          columnCount++;
//      }
//      
//      System.out.println(maxmatchedcoor+" "+maxmatchedpoints);
//  }

}
