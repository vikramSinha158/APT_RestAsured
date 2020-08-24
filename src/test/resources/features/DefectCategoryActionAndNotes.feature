@DefectCategory 
Feature: Verify Defect Category Action and Notes funtionality 

Background: Get Access Token 
	Given Call accretivehealth Api to get secret key 
	And Call auth Api toget access token
	And Store Access Token
	
@26158 @439370 @APIGatewayBVT
Scenario Outline: To validate that defect category should be assigned for unclassified accounts
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory and DefectSubCateogory and ResultText from DB "<DefectCateogoryQuery>" and "<DefectSubCategoryQuery>" and "<ResultTextQuery>"
When Call Post Method for defect category with param value and Verify Status code "200"
Then Verify A record Defect Sub Category should be inserted in DefectAccount table "<DefectAccountDetailsQuery>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultText|
		

@26160 @439371 @APIGatewayBVT
Scenario Outline: To validate the defect category for classified accounts
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory and DefectSubCateogory and ResultText from DB "<DefectCateogoryQuery>" and "<DefectSubCategoryQuery>" and "<ResultTextQuery>"
And Call Post Method for defect category with param value and Verify Status code "200"
And Verify A record Defect Sub Category should be inserted in DefectAccount table "<DefectAccountDetailsQuery>"
And Find New DefectCateogory and DefectSubCateogory and ResultText from DB "<NewDefectCateogoryQuery>" and "<NewDefectSubCategoryQuery>" and "<NewResultTextQuery>"
When Call Post Method for defect category with param value and Verify Status code "200"
Then Verify A record Defect Sub Category should be inserted in DefectAccount table "<DefectAccountDetailsQuery>"
And Verify The previous record from DefectAccount table should be inserted in DefectAccountHistory table "<DefectAccountHistoryDetailQuery>"

Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQuery|NewDefectCateogoryQuery|NewDefectSubCategoryQuery|NewResultTextQuery|DefectAccountHistoryDetailQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultText|ToGetNewDefectCateogory|ToGetNewDefectSubCategory|ToGetResultText   |ToGetDefectAccountHistoryDetail|


@26204
Scenario Outline: To validate that Steps and action should be saved successfully (If follow up date are future dates)
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find Visit Num from DB "<VisitNumberQuery>"
And Find Registration Id from DB "<RegistrationCodeQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Find Current follow up Action from DB "<ResultTextQueryForCurrent>"
And Find Future follow up Action from DB "<ResultTextQueryForFuture>"
When Call Post Method with two Actions and Verify Status code "200"
Then Verify Record should be inserted in ProcessLogs table "<ProcessLogsDetailsQuery>"
And Verify Record should be inserted in WorkFlowStatus table "<WorkFlowStatusDetailsQuery>"
And Verify Record should be inserted in CrossSiteAction table "<CrossSiteActionDetailsQuery>"
And Verify Record should be inserted in DefectAccountAttribute table "<DefectAccountAttributeDetailsQuery>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQueryForCurrent|ResultTextQueryForFuture|ProcessLogsDetailsQuery|WorkFlowStatusDetailsQuery|CrossSiteActionDetailsQuery|DefectAccountAttributeDetailsQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultTextForCurrent|ToGetResultTextForFuture|ToGetProcessLogsDetails|ToGetWorkFlowStatusDetails|ToGetCrossSiteActionDetails|ToGetDefectAccountAttributeDetails|

@26205
Scenario Outline: To validate that Steps and action should be saved successfully (If follow up date is Current date)
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find Visit Num from DB "<VisitNumberQuery>"
And Find Registration Id from DB "<RegistrationCodeQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Find Current follow up Action from DB "<ResultTextQueryForCurrent>"
And Store Current follow up Action and Find Current follow up Action from DB "<ResultTextQueryForCurrentActionsQuery>"
When Call Post Method for two current Actions and Verify Status code "200"
Then Verify Record for two current Actions should be inserted in ProcessLogs table "<ProcessLogsDetailsQuery>"
And Verify Last Action Record should be inserted in WorkFlowStatus table "<WorkFlowStatusDetailsQuery>"
And Verify Record should be inserted in CrossSiteAction table "<CrossSiteActionDetailsQuery>"
And Verify Record should be inserted in CrossSiteRegistry table "<CrossSiteRegistryQuery>"
And Verify Record should be inserted in CrossSiteRegistryDetail table "<CrossSiteRegistryDetailQuery>"
And Verify Record should be inserted in DefectAccountAttribute table "<DefectAccountAttributeDetailsQuery>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQueryForCurrent|ProcessLogsDetailsQuery|WorkFlowStatusDetailsQuery|CrossSiteActionDetailsQuery|DefectAccountAttributeDetailsQuery|ResultTextQueryForCurrentActionsQuery|CrossSiteRegistryQuery|CrossSiteRegistryDetailQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultTextForCurrent|ToGetProcessLogsDetails|ToGetWorkFlowStatusDetails|ToGetCrossSiteActionDetails|ToGetDefectAccountAttributeDetails|ToGetResultTextQueryForCurrentActions|ToGetCrossSiteRegistry|ToGetCrossSiteRegistryDetail|

@26223
Scenario Outline: To validate the mandatory fields for Defect category API
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find Visit Num from DB "<VisitNumberQuery>"
And Find Registration Id from DB "<RegistrationCodeQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Find Current follow up Action from DB "<ResultTextQueryForCurrent>"
When Call Post Method without sub category and Verify Status Code "400"
Then Verify Error Message for Sub Category "<ErrorMessageForSubCategory>"
When Call Post Method without Account Number and Verify Status Code "400"
Then Verify Status Code from Responce Body "<StatusCode>"
And Verify Error Message for Account Number "<ErrorMessageForAccountNumber>"

Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQueryForCurrent|ErrorMessageForSubCategory              |StatusCode|ErrorMessageForAccountNumber                              |
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultTextForCurrent|DefectSubCategory can't be null or empty|400       |AccountNumber must be same in Activity and Account details|

@26234
Scenario Outline: To validate the mandatory fields in Action steps taken API
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find Visit Num from DB "<VisitNumberQuery>"
And Find Registration Id from DB "<RegistrationCodeQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Find Not Required Action From DB "<ResultTextQueryForNotRequired>"
And Find Required Action From DB "<ResultTextQueryForCurrent>"
When Call Post Method with blank Action and verify status code "400"
Then Verify Error Message for blank Action "<ErrorMessageForBlankAction>"
When Call Post Method with Not Required Action and verify status code "400"
Then Verify Error Message for Not Required Action "<ErrorMessageForNotRequiredAction>"
When Call Post Method with Required Action and verify status code "200"
Then Verify Action should be inserted in WorkFlowStatus table "<WorkFlowStatusDetailsQuery>"
And Verify Record should be inserted in CrossSiteAction table "<CrossSiteActionDetailsQuery>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQueryForCurrent|ResultTextQueryForNotRequired     |ErrorMessageForBlankAction                              |ErrorMessageForNotRequiredAction       |WorkFlowStatusDetailsQuery|CrossSiteActionDetailsQuery|
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultTextForCurrent|ToGetResultTextQueryForNotRequired|Associated ActionIds are Invalid for DefectSubcategoryId|AtLeast one Mandatory ActionId required|ToGetWorkFlowStatusDetails|ToGetCrossSiteActionDetails|

@26235
Scenario Outline: To validate that Steps action taken API shoud update the defect category and actions both at same time
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory and DefectSubCateogory and ResultText from DB "<DefectCateogoryQuery>" and "<DefectSubCategoryQuery>" and "<ResultTextQuery>"
And Call Post Method for defect category with param value and Verify Status code "200"
And Verify A record Defect Sub Category should be inserted in DefectAccount table "<DefectAccountDetailsQuery>"
And Find New DefectCateogory and DefectSubCateogory and ResultText from DB "<NewDefectCateogoryQuery>" and "<NewDefectSubCategoryQuery>" and "<NewResultTextQuery>"
When Call Post Method for defect category with param value and Verify Status code "200"
Then Verify A record Defect Sub Category should be inserted in DefectAccount table "<DefectAccountDetailsQuery>"
And Verify The previous record from DefectAccount table should be inserted in DefectAccountHistory table "<DefectAccountHistoryDetailQuery>"
And Verify A Record should be inserted in ProcessLogs table "<ProcessLogsDetailsQuery>"
And Verify A Action should be inserted in WorkFlowStatus table "<WorkFlowStatusDetailsQuery>"
And Verify Record should be inserted in CrossSiteRegistry table "<CrossSiteRegistryQuery>"
And Verify Record should be inserted in CrossSiteRegistryDetail table "<CrossSiteRegistryDetailQuery>"
And Verify Record should be inserted in CrossSiteAction table "<CrossSiteActionDetailsQuery>"
And Verify Record should be inserted in DefectAccountAttribute table "<DefectAccountAttributeDetailsQuery>"

Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQuery       |NewDefectCateogoryQuery|NewDefectSubCategoryQuery|NewResultTextQuery       |DefectAccountHistoryDetailQuery|ProcessLogsDetailsQuery|WorkFlowStatusDetailsQuery|CrossSiteRegistryQuery|CrossSiteRegistryDetailQuery|CrossSiteActionDetailsQuery|DefectAccountAttributeDetailsQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultTextDetails|ToGetNewDefectCateogory|ToGetNewDefectSubCategory|ToGetResultTextDetails   |ToGetDefectAccountHistoryDetail|ToGetProcessLogsDetails|ToGetWorkFlowStatusDetails|ToGetCrossSiteRegistry|ToGetCrossSiteRegistryDetail|ToGetCrossSiteActionDetails|ToGetDefectAccountAttributeDetails|

@26236
Scenario Outline: To validate that only valid Actions should be passed in the API
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find Visit Num from DB "<VisitNumberQuery>"
And Find Registration Id from DB "<RegistrationCodeQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
When Find Actions from DB "<ActionsQuery>" and "<StatusCode>"
Then Call Post Method and Verify Status Code "<StatusCode>" and "<ActionsText>"
And Verify Error Message "<StatusCode>" and "<ErrorMessage>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ActionsQuery		 |StatusCode|ActionsText					  |ErrorMessage					   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetRandomActions|200       |								  |								   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetRandomActions|200       |								  |								   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetRandomActions|200       |								  |								   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetRandomActions|400       |AutomationTestingForInvalidAction|Associated ActionIds are Invalid|


@26249
Scenario Outline: To validate DefectSubCategory Name Parameter (field or key) for unclassified account in API
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
When Find Actions from DB "<ActionsQuery>" and "<StatusCode>"
Then Call Post Method and Verify Status Code for Defect Sub Category "<StatusCode>" and "<SubCategoryText>"
And Verify Error Message "<StatusCode>" and "<ErrorMessage>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ActionsQuery	   |StatusCode|SubCategoryText					  	 |ErrorMessage			   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetRandomSubCategory|ToGetRandomActions|200       |								  	  	 |						   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetRandomSubCategory|ToGetRandomActions|200       |								  		 |						   |	
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetRandomSubCategory|ToGetRandomActions|200       |								  		 |						   |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetRandomSubCategory|ToGetRandomActions|400       |AutomationTestingForInvalidSubCategory|Invalid DefectSubCategory|


@26248
Scenario Outline: To validate that Action steps API should be able to save the notes
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find Visit Num from DB "<VisitNumberQuery>"
And Find Registration Id from DB "<RegistrationCodeQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Find Current follow up Action from DB "<ResultTextQueryForCurrent>"
And Find Future follow up Action from DB "<ResultTextQueryForFuture>"
When Call Post Method with two Actions and Verify Status code "200"
Then Verify Notes should be inserted in ProcessLogs table "<ProcessLogsDetailsQuery>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQueryForCurrent|ResultTextQueryForFuture|ProcessLogsDetailsQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultTextForCurrent|ToGetResultTextForFuture|ToGetProcessLogsDetails|


@26250 @439372 @APIGatewayBVT
Scenario Outline: To validate only few specific DefectSubCategory Name should be assigned for classified accounts
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Get Actions From DB "<ActionsQuery>"
When Call Post Method and Verify Status Code "<SuccessStatusCode>"
And Get DefectCateogory and DefectCateogoryID from DB "<DetailsDefectCateogory>"
And Get DefectSubCateogory and DefectSubCateogoryID from DB "<DetailsDefectSubCateogory>"
And Get Actions From DB "<ActionsQueries>"
When Call Post Method and Verify Status Code "<SuccessStatusCode>"
And Get DefectCateogory and DefectCateogoryID from DB "<DetailsOfDefectCateogory>"
And Get DefectSubCateogory and DefectSubCateogoryID from DB "<DetailsDefectSubCateogory>"
And Get Actions From DB "<ActionsQueries>"
Then Call Post Method and Verify Status Code "<ErrorStatusCode>"
And Verify Error Message "<ErrorStatusCode>" and "<ErrorMessage>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery |DefectSubCategoryQuery |ActionsQuery      |ActionsQueries|DetailsOfDefectCateogory      |DetailsDefectCateogory      |DetailsDefectSubCateogory      |SuccessStatusCode|ErrorStatusCode|ErrorMessage                                                      |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToFindDefectCateogory|ToFindDefectSubCategory|ToGetRandomActions|ToFindActions |ToFindDetailsOfDefectCateogory|ToFindDetailsDefectCateogory|ToFindDetailsDefectSubCateogory|200              |400            |Not a valid request due to Inactive or Invalid DefectSubCategoryId|


@26949
Scenario Outline: To validate that API should not be submitted if passing an action that belongs to another Defect sub category Name
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Get Actions From DB "<ActionsQuery>"
When Call Post Method and Verify Status Code "<StatusCode>"
Then Verify Error Message "<StatusCode>" and "<ErrorMessage>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery |DefectSubCategoryQuery |ActionsQuery		|StatusCode|ErrorMessage                                            |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToFindDefectCateogory|ToFindDefectSubCategory|ToFindActionQuery|400       |Associated ActionIds are Invalid for DefectSubcategoryId|


@26950 @439373 @APIGatewayBVT
Scenario Outline: To validate API should be posted if passing the valid action belongs to that defect sub category name
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory and DefectSubCateogory and ResultText from DB "<DefectCateogoryQuery>" and "<DefectSubCategoryQuery>" and "<ResultTextQuery>"
When Call Post Method for defect category with param value and Verify Status code "200"
Then Verify A record Defect Sub Category should be inserted in DefectAccount table "<DefectAccountDetailsQuery>"
And Verify A Action should be inserted in WorkFlowStatus table "<WorkFlowStatusDetailsQuery>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery|DefectSubCategoryQuery|ResultTextQuery|WorkFlowStatusDetailsQuery|
		|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToGetDefectCateogory|ToGetDefectSubCategory|ToGetResultText|ToGetWorkFlowStatusDetails|

@27209
Scenario Outline: To validate that API should not get posted with inactive Defect SubCategory
Given Find User ID and User Name "<RequestorCodeQuery>"
And Find unclassifiedAccount from DB "<VisitNumberQuery>" and "<RegistrationCodeQuery>" and "<DefectAccountDetailsQuery>"
And Find DefectCateogory from DB "<DefectCateogoryQuery>"
And Find inactive DefectSubCateogory from DB "<DefectSubCategoryQuery>"
And Get Actions From DB "<ActionsQuery>"
When Call Post Method and Verify Status Code "<StatusCode>"
Then Verify Error Message "<StatusCode>" and "<ErrorMessage>"
Examples:
		|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|DefectAccountDetailsQuery|DefectCateogoryQuery |DefectSubCategoryQuery 		  |ActionsQuery		 |StatusCode|ErrorMessage                                                      |
		|DNNQuery          |DNNSelectEncounterID|ToGetRegistrationsID |ToGetDefectAccountDetails|ToFindDefectCateogory|ToFindInactiveDefectSubCategory|ToFindActionsQuery|400       |Not a valid request due to Inactive or Invalid DefectSubCategoryId|









