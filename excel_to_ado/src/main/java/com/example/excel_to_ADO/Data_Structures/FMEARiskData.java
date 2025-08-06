package com.example.excel_to_ADO.Data_Structures;

// Immutable holder for single row of excel.
// The constructor sets the fields and accessors are automatically generated at compile time.
public record FMEARiskData (
        Integer riskID, // This one should be empty when trying to upload 
        Integer predecessorID,
        String predeccessorTitle,
        String title, 
        String failureEffects, 
        String cause,
        String preSeverity, 
        String preOccurrence, 
        String preDetection, 
        String mitigationType, 
        String implementation,
        String evidence, 
        String resSeverity, 
        String resOccurrence, 
        String resDetection)
        {}

    

