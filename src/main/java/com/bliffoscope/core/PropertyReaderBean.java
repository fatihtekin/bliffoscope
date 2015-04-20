package com.bliffoscope.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service(value="propertyReaderBean")
public class PropertyReaderBean {

    @Value( "${bliffoscope.file.path}" )
    private String bliffoscopeFilePath;
 
    @Value("${bliffoscope.file.targets}")
    private String bliffoscopeFileTargets;
    
    @Value("${bliffoscope.file.data}")
    private String bliffoscopeFileData;

    public String getBliffoscopeFileTargets() {
        return bliffoscopeFileTargets;
    }

    public String getBliffoscopeFileData() {
        return bliffoscopeFileData;
    }

    public String getBliffoscopeFilePath() {
        return bliffoscopeFilePath;
    }
}
