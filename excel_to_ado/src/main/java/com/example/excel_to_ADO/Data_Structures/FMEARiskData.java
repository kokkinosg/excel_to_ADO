package com.example.excel_to_ADO.Data_Structures;

// Immutable holder for single row of excel.
// The constructor sets the fields and accessors are automatically generated at compile time.
public record FMEARiskData (
        Integer predecessorID,
        String predeccessorTitle,
        String title, 
        String failureEffects, 
        String cause,
        String mitigation, 
        String evidence, 
        String preSeverity, 
        String preOccurance, 
        String preDetection, 
        String resSeverity, 
        String resOccurance, 
        String resDetection)
        {}

    

