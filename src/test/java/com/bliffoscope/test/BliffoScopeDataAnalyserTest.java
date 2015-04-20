package com.bliffoscope.test;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bliffoscope.BliffoscopeDataAnalyser;
import com.bliffoscope.log.Log;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:bliffoscope-config.xml"})
public class BliffoScopeDataAnalyserTest extends TestCase{

    @Autowired
    private BliffoscopeDataAnalyser bliffoscopeDataAnalyser;

    @Log Logger logger;

    @Test
    public void testTorpidosLowConfidenceLevel(){

        Map<String, Double> foundTargetObjectsMap = bliffoscopeDataAnalyser.searchForTargetObject(
                getTestFilePath()+"TestData.blf",
                getTestFilePath()+"SlimeTorpedo.blf",99.5D);
        
        logger.info("{} SlimeTorpedos found with confidence level: {}.",foundTargetObjectsMap.size(),99.5D);

        for (String coordinates: foundTargetObjectsMap.keySet()) {
            logger.info(coordinates+" "+foundTargetObjectsMap.get(coordinates));
        } 
        Assert.assertEquals("Expected 15 but found "+foundTargetObjectsMap.size(),foundTargetObjectsMap.size(), 15);
    }
    
    @Test
    public void testTorpidos(){

        Map<String, Double> foundTargetObjectsMap = bliffoscopeDataAnalyser.searchForTargetObject(
                getTestFilePath()+"TestData.blf",
                getTestFilePath()+"SlimeTorpedo.blf",99.9D);
        
        logger.info("{} SlimeTorpedos found with confidence level: {}.",foundTargetObjectsMap.size(),99.9D);

        for (String coordinates: foundTargetObjectsMap.keySet()) {
            logger.info(coordinates+" "+foundTargetObjectsMap.get(coordinates));
        }  
        Assert.assertEquals("Expected 5 but found "+foundTargetObjectsMap.size(),foundTargetObjectsMap.size(), 5);
    }
    
    @Test
    public void testStarShipLowConfidenceLevel(){

        Map<String, Double> foundTargetObjectsMap = bliffoscopeDataAnalyser.searchForTargetObject(
                getTestFilePath()+"TestData.blf",
                getTestFilePath()+"Starship.blf",99.5D);
        
        logger.info("{} Starship found with confidence level: {}.",foundTargetObjectsMap.size(),99.5D);

        for (String coordinates: foundTargetObjectsMap.keySet()) {
            logger.info(coordinates+" "+foundTargetObjectsMap.get(coordinates));
        }  
        Assert.assertEquals("Expected 15 but found "+foundTargetObjectsMap.size(),foundTargetObjectsMap.size(), 15);

    }
    
    @Test
    public void testStarShip(){

        Map<String, Double> foundTargetObjectsMap = bliffoscopeDataAnalyser.searchForTargetObject(
                getTestFilePath()+"TestData.blf",
                getTestFilePath()+"Starship.blf",99.9D);
        
        logger.info("{} Starship found with confidence level: {}.",foundTargetObjectsMap.size(),99.9D);

        for (String coordinates: foundTargetObjectsMap.keySet()) {
            logger.info(coordinates+" "+foundTargetObjectsMap.get(coordinates));
        } 
        Assert.assertEquals("Expected 6 but found "+foundTargetObjectsMap.size(),foundTargetObjectsMap.size(), 6);

    }

    public String getTestFilePath() {
        return System.getProperty("user.dir") + File.separator +"src"+File.separator+"test"+File.separator+"resources"
        +File.separator;
    }

}
