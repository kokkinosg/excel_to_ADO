# excel_to_ADO
This is a tool which takes an excel file containing requirements and uploads them to DevOps. It saves enormous amount of physical and mental effort from doing it manually.  

## How to use

1) All requirements should be written on an xlsx with the following header (order must be exactly this):
    - Parent ADO ID /	Parent Title/ Child Workitem ADO ID	/ Child Workitem Type / Workitem Title / Acceptance criteria / NOT UPLOADABLE / NOT UPLOADABLE
2) Specify the Organisation Name, Project Name, Api Version, Personal access token obtained from User Settings on ADO , xlsx file path and name of the sheet with the data
3) Run Main 
4) Note down all IDs for the created items from the console. 

Note:
- There must be no blank lines in the sheet between rows
- Pare requirements are better created on devops and their details noted on hte spreadsheet before running the code if a relationship is going to be needed.
- The defualt relationship is that the work items to be added are children to a feature. 
    PARENT("System.LinkTypes.Hierarchy-Forward"),
    CHILD ("System.LinkTypes.Hierarchy-Reverse"),
    RELATED("System.LinkTypes.Related");
- Currently, it only creates User-Stories. But it can be modified in AdoRESTClient to include oter types such as functional requirements.    

        
## Project Structure

### AdoRESTCLient 

It includes all HTTP calls to Azure DevOps. It includes methods to create work items, retrieve the work item id, test the connection 

AdoRESTClient – method summary

1) public	AdoRESTClient(String org, String project, String apiVersion, String pat)	
    Builds a reusable client: stores organisation, project and PAT; creates a single OkHttpClient (10-second call-timeout, one automatic retry).
2) public	boolean testConnection()	
    GET /_apis/projects  → verifies that the PAT is valid for the given org; prints number of visible projects and returns true on HTTP 2xx.
3) public	Integer getWorkItemID(String exactTitle)	
    POST /_apis/wit/wiql with a WIQL query that matches the given title; returns the first work-item ID or null if none is found.
4) public	boolean createWorkItem(String type, String title, String ac, Integer relatedId, String linkType)	
    Creates a new work-item of type using a JSON-Patch body; optionally links it to relatedId (Hierarchy-Reverse, Hierarchy-Forward, Related, …). Prints the new ID and returns true on success.
5) private	JsonArray createJsonArray(String title, String ac, Integer relatedId, String linkType)	
    Helper that assembles the JSON-Patch array ([{op, path, value}, …]) used by createWorkItem — adds Title, Acceptance Criteria, and one optional relation.

*All other protocol details (URL building, headers, response parsing) are encapsulated inside these methods, keeping callers limited to three public operations: testConnection, getWorkItemID, and createWorkItem.

### ExcelReader

Loads a .xlsx workbook with Apache POI, selects one sheet, converts every data row (after the header) into a strongly-typed RowData record, and exposes the resulting List<RowData> to the caller.

ExcelReader - method summary

1) public ExcelReader(String filePath, String sheetName)
    Opens the workbook (try-with-resources to auto-close the stream), selects the requested sheet, then calls retrieveRowData() to populate the internal list.
2) public List<RowData> getSheetData()
    Returns the cached list of row objects so other classes can iterate over the backlog.
3) private boolean openXlsx(String filePath)
    Creates XSSFWorkbook from the file; prints and returns false if the file is missing or unreadable.
4) private boolean openSheet(String sheetName)
    Retrieves the named sheet from the workbook; returns false on error.
5) private void retrieveRowData()
    Iterates the sheet: skips the header row, maps each remaining row to a new RowData, and adds it to sheetData.
6) private Integer getInt(Row r, int col)
    Safely converts a numeric or numeric-as-text cell to Integer, returning null for blanks.
7) private String getStr(Row r, int col)
    Returns trimmed cell text or an empty string for blanks.

### RowData
RowData is a Java record that captures one logical line from the Excel backlog. ExcelReader instantiates one RowData per non-header row and returns List<RowData>; Main then iterates that list to drive Azure DevOps imports with AdoRESTClient.

Because records are implicitly immutable and auto-generate constructor, getters, equals, hashCode, and toString, RowData is a light, safe value object we can pass around without risk of later mutation.

### Main
The entry point to the software. It acts as the controler and executor defining the logic and execution. Specifically it:

	1.	Define configuration constants
        •	Azure DevOps organisation, project, and API version
        •	Personal-Access-Token (PAT) with Work-Items scope
        •	Excel file path and sheet name
	2.	Instantiate AdoRESTClient with the above values.
	3.	Test the Azure DevOps connection via client.testConnection(); exit early if it fails.
	4.	Load spreadsheet data
        •	Create ExcelReader with the file path and sheet name.
        •	Retrieve List<RowData> representing each data row (header skipped).
	5.	Loop through the RowData list
        •	For each row call
    createWorkItem(childType, childTitle, acceptanceCriteria, parentId, "Hierarchy-Reverse")
        •	This creates the work item and links it to its parent when parentId is not null.
	6.	Output progress — AdoRESTClient prints a success line with the new work-item ID for every row processed.

