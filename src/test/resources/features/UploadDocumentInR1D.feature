@UploadDocument
Feature: Verify API Gateway - Upload document in R1D funtionality 

Background: Get Access Token 
	Given Call accretivehealth Api to get secret key 
	And Call auth Api toget access token
	And Store Access Token

@25979 @439369 @APIGatewayBVT
Scenario Outline: To validate that upload document API is able to upload the document in R1D
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With Excel File for Upload Documents and verify status code "200"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted into the document table in tran database "<DocumentQuery>" and "<DocumentFormatXlsx>"
When Call Post Method With Pdf File for Upload Documents and verify status code "200"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted into the document table in tran database "<DocumentQuery>" and "<DocumentFormatPdf>"
When Call Post Method With docx File for Upload Documents and verify status code "200"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted into the document table in tran database "<DocumentQuery>" and "<DocumentFormatDoc>"

Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |DocumentFormatXlsx|DocumentFormatPdf|DocumentFormatDoc|GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|xlsx              |pdf              |docx             |ToGetTypesForUploadDocuments|

@25982
Scenario Outline: To verify the valid file extension
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With File for Upload Documents and verify status code "200" and "<FileName>"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted in to the document table in tran database "<DocumentQuery>" and "<FileName>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |FileName	   |GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validDocc.doc  |ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validDoc.docx  |ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validXls.xls   |ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validExcel.xlsx|ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validJpg.jpg   |ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validPng.png   |ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validGif.gif   |ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validPdf.pdf   |ToGetTypesForUploadDocuments|

@25983
Scenario Outline: To verify the invalid file extension
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With File for Upload Documents and verify status code "400" and "<FileName>"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessage>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|FileName	          |StatusCode|ErrorMessage																										|GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |Database.accdb     |400       |File uploaded is not supported, Supported file extensions are .doc, .docx, .xls, .xlsx, .jpg, .png, .gif and .pdf!|ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |Test.txt           |400       |File uploaded is not supported, Supported file extensions are .doc, .docx, .xls, .xlsx, .jpg, .png, .gif and .pdf!|ToGetTypesForUploadDocuments|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |TestPowerPoint.pptx|400       |File uploaded is not supported, Supported file extensions are .doc, .docx, .xls, .xlsx, .jpg, .png, .gif and .pdf!|ToGetTypesForUploadDocuments|


@25980
Scenario Outline: To verify the mandatory fields
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method Without Type For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForType>"
When Call Post Method Without Title For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForTitle>"
When Call Post Method Without Visit Number For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForVisitNum>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|StatusCode|ErrorMessageForType  |ErrorMessageForTitle	  |ErrorMessageForVisitNum                              |GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |400       |Invalid Document Type|Document Title is required|Invalid Activity request model, Visit Number is empty|ToGetTypesForUploadDocuments|

@25981
Scenario Outline: To verify the data validation
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With Invalid Type For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForType>"
When Call Post Method With Invalid Visit Number For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForVisitNum>"
When Call Post Method With Invalid Performer For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForPerformer>"

Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|StatusCode|ErrorMessageForType  |ErrorMessageForVisitNum|ErrorMessageForPerformer|GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |400       |Invalid Document Type|Invalid Visit Number   |Invalid performer code  |ToGetTypesForUploadDocuments|

@25984
Scenario Outline: To verify the valid file size
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With File for Upload Documents and verify status code "200" and "<FileName>"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted in to the document table in tran database "<DocumentQuery>" and "<FileName>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |FileName	   	 |GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|validSizeWord.doc|ToGetTypesForUploadDocuments|


@25985
Scenario Outline: To verify the invalid file size
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With File for Upload Documents and verify status code "400" and "<FileName>"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessage>"

Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |FileName	   	   |StatusCode|ErrorMessage                         |GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|InValidSizeWord.doc|400       |Uploaded File size is more than 20 MB|ToGetTypesForUploadDocuments|

@25986
Scenario Outline: To verify the multiple file uploads
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With Two Files for Upload Documents and verify status code "200" and "<FirstFileName>" and "<SecoundFileName>"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted in to the document table in tran database "<DocumentQuery>" and "<FirstFileName>"
And Verify Record should not inserted in to the document table in tran database "<DocumentQuery>" and "<SecoundFileName>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |FirstFileName |SecoundFileName|GetDocumentTypeQuery        |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|FileNumOne.pdf|FileNumTwo.docx|ToGetTypesForUploadDocuments|

@25987
Scenario Outline: To verify that document should get uploaded only with defined document Type codes
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With File for Upload Documents and verify status code "200" and "<FileName>"
Then Verify Responce body for Upload Documents
And Verify Record should be inserted in to the document table in tran database "<DocumentQuery>" and "<FileName>"
When Call Post Method With Invalid Type For Upload Documents and Verify Status Code "400"
Then Verify status code from responce body "<StatusCode>"
And Verify Error Message from responce body "<ErrorMessageForType>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |FileName	   	|GetDocumentTypeQuery        |StatusCode|ErrorMessageForType  |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|DocTypeCode.docx|ToGetTypesForUploadDocuments|400       |Invalid Document Type|


@26288
Scenario Outline: To validate the ApplicableToMRN field
Given Find User ID and User Name for Upload Documents "<RequestorCodeQuery>"
And Find Visit Num from DB for Upload Documents "<VisitNumberQuery>"
And Find Registration Id from DB for Upload Documents "<RegistrationCodeQuery>"
And Find Document Type from DB for Upload Documents "<GetDocumentTypeQuery>"
When Call Post Method With File and MRN for Upload Documents and verify status code "200" and "<FileName>" and "<MRNValue>"
Then Verify Responce body for Upload Documents
And Verify MRN should be inserted in to the document table in tran database "<DocumentQuery>" and "<FileName>" and "<MRNValue>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DocumentQuery       |FileName     |GetDocumentTypeQuery        |MRNValue                          |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|MrnDoc.docx  |ToGetTypesForUploadDocuments|UploadDocumentMrnValueWithBlank   |
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDocumentDetails|MrnExcel.xlsx|ToGetTypesForUploadDocuments|UploadDocumentMrnValueWithNotBlank|











