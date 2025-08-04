package com.example.excel_to_ADO;

// Immutable holder for single row of excel.
// The constructor sets the fields and accessors are automatically generated at compile time. 
public record SysReqData(
        Integer parentId,
        String  parentTitle,
        Integer childId,
        String  childType,
        String  childTitle,
        String  acceptanceCriteria) {}

        
