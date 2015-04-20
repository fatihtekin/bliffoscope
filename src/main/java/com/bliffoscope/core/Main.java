package com.bliffoscope.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import com.bliffoscope.BliffoscopeDataAnalyser;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bliffoscope-config.xml");

        while (!ctx.containsBean("bliffoscopeDataAnalyser")) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignore) {
                logger.debug("Ignoring: ", ignore);
            }
        }
        ctx.registerShutdownHook();
        BliffoscopeDataAnalyser bliffoscopeDataAnalyser = ctx.getBean("bliffoscopeDataAnalyser", BliffoscopeDataAnalyser.class);
        PropertyReaderBean propertyReaderBean = ctx.getBean("propertyReaderBean",PropertyReaderBean.class);
        String[] targetObjectImageFileNames = null;
        try {            
            targetObjectImageFileNames = propertyReaderBean.getBliffoscopeFileTargets().split(",");
        } catch (Exception e) {
            logger.error("Could not parse target objects from bliffoscope.file.targets:{} property in bliffoscope.properties file",System.getProperty("bliffoscope.file.targets"));
            System.exit(-1);
        }
        
        String dataFileName = propertyReaderBean.getBliffoscopeFileData();
        if(!StringUtils.hasText(dataFileName)){
            logger.error("Data file name to search for the target objects is not valid {}",dataFileName);
        }
        
        String filePath = propertyReaderBean.getBliffoscopeFilePath();
        List<Double> alphaConfidenceLevels = new ArrayList<Double>();
        Double alphaConfidenceLevel = null;
        try {
            alphaConfidenceLevel = Double.valueOf(System.getProperty("alpha"));
            if(alphaConfidenceLevel >= 90.0D){                
                alphaConfidenceLevels.add(alphaConfidenceLevel);
            }else{
                logger.info("Please do not enter lower than 90.0 as alpha for binomial confidence");
            }
        } catch (Exception e) {
            logger.error("Could get alpha confidence interval from jvm arguments will set default 99.5D");
        }
        if(alphaConfidenceLevels.size() == 0 ){
            alphaConfidenceLevels.add(99.5D);
            alphaConfidenceLevels.add(99.9D);
        }
        
        for (Double alpha : alphaConfidenceLevels) {
            
            logger.info("========For-Alpha-Binimial-Confidence=={}=========",alpha);
            
            for (String targetObjectImageFileName : targetObjectImageFileNames) {
                logger.info("=========={}S=========",targetObjectImageFileName.toUpperCase().
                        substring(0, targetObjectImageFileName.lastIndexOf('.')));            
                Map<String, Double> foundTargetObjectsMap = bliffoscopeDataAnalyser.searchForTargetObject(
                        (new StringBuilder(filePath)).append(File.separator).append(dataFileName).toString(),
                        (new StringBuilder(filePath)).append(File.separator).append(targetObjectImageFileName).toString(),
                        alpha);
                
                logger.info("{} {} found.",foundTargetObjectsMap.size(),
                        targetObjectImageFileName.substring(0, targetObjectImageFileName.lastIndexOf('.')));
                
                for (String coordinates: foundTargetObjectsMap.keySet()) {
                    logger.info(coordinates+" "+foundTargetObjectsMap.get(coordinates));
                }
            }
            
            logger.info("================================================",alpha);
            logger.info("\n\n");
        }
        
        ctx.close();
        
    }

}
