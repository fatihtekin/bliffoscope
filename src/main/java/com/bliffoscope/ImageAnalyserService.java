package com.bliffoscope;

import java.util.concurrent.Future;

public interface ImageAnalyserService {

    Future<Integer> getMatchedPoints(boolean[][] targetImage, boolean[][] data, int lineStartIndex, int columnStartIndex);

    boolean[][] trimTargetImage(boolean[][] targetImage);

}
