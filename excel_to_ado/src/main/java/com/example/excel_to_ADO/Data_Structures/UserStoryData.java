package com.example.excel_to_ADO.Data_Structures;

// Immutable holder for single row of User Story from the excel.
// The constructor sets the fields and accessors are automatically generated at compile time. 
public record UserStoryData(
        Integer parentId,
        String  parentTitle,
        Integer userStoryId,
        String  childType,
        String  childTitle,
        String  acceptanceCriteria) {}

        
