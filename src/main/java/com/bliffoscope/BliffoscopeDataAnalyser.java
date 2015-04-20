package com.bliffoscope;

import java.util.Map;

public interface BliffoscopeDataAnalyser {

    Map<String, Double> searchForTargetObject(String dataLocation, String targetImageLocation, double alpha);
    
}
