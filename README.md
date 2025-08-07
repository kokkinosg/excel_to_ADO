# excel_to_ADO

This is a tool which takes an excel file containing requirements and uploads them to DevOps. It saves enormous amount of physical and mental effort from doing it manually.  

## How to use

1) All requirements should be written on an xlsx with the following header (order must be exactly this):
    •	A – Parent ADO ID
	•	B – Parent Title
	•	C – Child / Predecessor ADO ID
	•	D – Child Work-item Type
	•	E – Child / Predecessor Title
	•	F – Acceptance Criteria
	•	I – Potential Failure Mode
	•	J – Potential Failure Effects
	•	K – Probable Cause
	•	L – Pre-Mitigation Severity
	•	N – Pre-Mitigation Occurrence
	•	P – Pre-Mitigation Detection
	•	T – Mitigation Type
	•	V – Evidence
	•	X – Post-Mitigation Severity
	•	Z – Post-Mitigation Occurrence
	•	AB – Post-Mitigation Detection

2) Specify the Organisation Name, Project Name, Api Version, Personal access token obtained from User Settings on ADO , xlsx file path and name of the sheet with the data on config.properties.
3) Run Main and choose whether to upload User Stories or FMEA Risks
4) Note down all IDs for the created items from the console. 

Note:
- There must be no blank lines in the sheet between rows
- Parent requirements are better created on devops and their details noted on the spreadsheet before running the code if a relationship is going to be needed.
- The defualt relationship is that the work items to be added are children to a feature. 
    // PARENT("System.LinkTypes.Hierarchy-Forward"),
    // CHILD ("System.LinkTypes.Hierarchy-Reverse"),
    // RELATED("System.LinkTypes.Related");  
    // SUCCESSOR ("System.LinkTypes.Dependency-Reverse"),
    // PREDECCESSOR ("System.LinkTypes.Dependency-Forward");   
- Currently, it can only create user stories and FMEA Risks  
- It does not check if the items already exist so it will keep on creating them. Do it only once per sheet. 


## Things to add 
- A function which removes trailing whitespace in strings because it may mess up picklist fields on DevOps. For example "Elimination by Design " is different to "Elimination by Design". The former will be rejected if i try to upload it. 

## Project Structure
TBD 

# Disclaimer
The PAT has been revoked. I just let it as an example of how it looks like. 

