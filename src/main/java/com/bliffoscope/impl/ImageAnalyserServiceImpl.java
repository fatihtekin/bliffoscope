package com.bliffoscope.impl;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.bliffoscope.ImageAnalyserService;
import com.bliffoscope.log.Log;

@Service
public class ImageAnalyserServiceImpl implements ImageAnalyserService{

    @Log 
    private Logger logger;
    
    /**
     * Calculates the count of matched points in the of the @param data 2D array we are scanning against @param targetImage
     * 
     * @param targetImage image of the target object like torpido or starship
     * @param data the whole dataset that we will search the object in 
     * @param lineStartIndex from which line to start to look for the target object in the @param data 2D array
     * @param columnStartIndex from which column to start to for the target object in the @param data 2D array
     * @return point that are matched in targetImage and the frame of the @param data 2D array we are scanning
     */
    @Async
    public Future<Integer> getMatchedPoints(boolean [][] targetImage,boolean [][] data, int lineStartIndex,int columnStartIndex){
        
        int matchedPoints = 0;
        int frameLine = 0;
        for (int line = lineStartIndex; (line < lineStartIndex+targetImage.length && line < data.length) ; line++) {
            int frameColumn = 0;
            for(int column = columnStartIndex; column < columnStartIndex+targetImage[0].length && column < data[0].length; column++){
                if(data[line][column] == targetImage[frameLine][frameColumn]){
                    matchedPoints++;
                }
                frameColumn++;
            } 
            frameLine++;
        }
        return new AsyncResult<Integer>(matchedPoints);
    }

    /**
     * Trims the outer of the target object image so that if the first or the last lines
     * in the image are fully empty then we can discard these lines
     * the same logic applies to the columns as well 
     * @param targetImage image of the target object like torpido or starship
     * @return trimmed targetImage 2D array
     */
    public boolean [][] trimTargetImage(boolean [][] targetImage){
        int startLineIndex = 0;    
        loop:
            for (int line = 0; line<targetImage.length; line++){
                for (int column = 0; column<targetImage[line].length; column++){
                    if(targetImage[line][column]){                    
                        break loop;
                    }
                }
                startLineIndex++;
            }

        int startColumnIndex = 0;
        loop:
            while(true){            
                for (int line = 0; line<targetImage.length; line++){
                    if(targetImage[line].length > startColumnIndex && targetImage[line][startColumnIndex]){
                        break loop; 
                    }
                }
                startColumnIndex++;
            }

        int endLineIndex=0;
        loop:
            for (int line = targetImage.length-1; line >= 0; line--){
                for (int column = 0; column<targetImage[line].length; column++){
                    if(targetImage[line][column]){                    
                        break loop;
                    }
                }
                endLineIndex++;
            }

        int endColumnIndex=0;
        loop:
            while(true){            
                for (int line = 0; line<targetImage.length; line++){
                    if(targetImage[line].length > startColumnIndex && targetImage[line][targetImage[0].length-1-endColumnIndex]){
                        break loop; 
                    }
                }
                endColumnIndex++;
            }    
            
        boolean[][] trimedTargetImage = new boolean [targetImage.length-startLineIndex-endLineIndex][targetImage[0].length - startColumnIndex-endLineIndex];
        int trimmedImageLine = 0;
            
        for (int line = startLineIndex; line<targetImage.length-endLineIndex; line++){
            int trimmedImageColumn = 0;
            for (int column = startColumnIndex; column<targetImage[line].length-endColumnIndex; column++){
                trimedTargetImage[trimmedImageLine][trimmedImageColumn] = targetImage[line][column];
                trimmedImageColumn++;
            }
            trimmedImageLine++;
        }        

        return trimedTargetImage;
    }
    
    /**
     * Below function is implemented however not used in the final solution 
     * @param targetImage image of the target object like torpido or starship
     * @param data the whole dataset that we will search the object in 
     * @param lineStartIndex from which line to start to look for the target object in the @param data 2D array
     * @param columnStartIndex from which column to start to for the target object in the @param data 2D array
     * @param validPoints the valid points that can be calculated in the function {@link #getValidPoints(boolean[][])}getValidPoints in the the target frames 
     * @return
     */
    public int getMatchedPoints(boolean [][] targetImage,boolean [][] data, int lineStartIndex,int columnStartIndex,boolean [][] validPoints){

        int matchedPoints = 0;
        int frameLine = 0;
        for (int line = lineStartIndex; (line < lineStartIndex+targetImage.length && line < data.length) ; line++) {
            int frameColumn = 0;
            for(int column = columnStartIndex; column < columnStartIndex+targetImage[0].length && column < data[0].length; column++){
                if(validPoints[frameLine][frameColumn] && data[line][column] == targetImage[frameLine][frameColumn]){
                    matchedPoints++;
                }
                frameColumn++;
            } 
            frameLine++;
        }
        return matchedPoints;
    }
    
    /**
     * That is initially thought to discard the outer of the targetObject however being empty for most of the cases is also valid. 
     * To take this into account below function is not used in the solution
     * */
    public boolean[][] getValidPoints(boolean[][] targetImage) {

        boolean[][] startEndPoints = new boolean[targetImage.length][targetImage[0].length];
        for (int line = 0; line<targetImage.length; line++){
            for (int column = 0; column<targetImage[line].length; column++){
                if(targetImage[line][column]){                    
                    startEndPoints[line][column] = true;
                    break;
                }
            }
            boolean isEverythingTrueTillTheNextTrue = false;
            for (int column = targetImage[line].length-1; column>=0; column--){
                if(isEverythingTrueTillTheNextTrue || targetImage[line][column]){
                    if(!isEverythingTrueTillTheNextTrue){
                        if(startEndPoints[line][column]){                            
                            break;
                        }else{
                            isEverythingTrueTillTheNextTrue = true;
                            startEndPoints[line][column] = true;
                        }
                    }else if(isEverythingTrueTillTheNextTrue){
                        if(startEndPoints[line][column]){
                            break;                            
                        }else{
                            startEndPoints[line][column] = true;
                        }
                    }
                }
            }
        }
        return startEndPoints;
    }

}
