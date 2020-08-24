@BillEdit 
Feature: Verify Activities and EPARS Handoff API Endpoints funtionality 

Background: Get Access Token 
	Given Call accretivehealth Api to get secret key 
	And Call auth Api toget access token 
	
@25537 
Scenario Outline: Verify validation message for mandatory fields in Bill Edit POST API 
	Given Store Access Token 
	And Get VisitNumber RequestorCode from DB "<VisitNumberQuery>" and "<RequestorCodeQuery>" 
	When Call POST Method for Bill Edit without encounterid and verify Status Code as "400"
	Then Verify Bill Edit Validation Message as "<StatusCode>" and "<MessageForEncId>"
	When Call POST Method for Bill Edit without why and verify Status Code as "400"
	Then Verify Bill Edit Validation Message as "<StatusCode>" and "<MessageForWhy>"
	When Call POST Method for Bill Edit without what and verify Status Code as "400"
	Then Verify Bill Edit Validation Message as "<StatusCode>" and "<MessageForWhat>" 
	Examples: 
		|StatusCode|MessageForWhat            |MessageForWhy |MessageForEncId		 |VisitNumberQuery    |RequestorCodeQuery|
		|400       |Invalid Action Disposition|Invalid Action|Invalid Registration id|DNNSelectEncounterID|DNNQuery          |
		
@25536 @439368 @APIGatewayBVT
Scenario Outline: Verify Bill Edit POST API 
	Given Store Access Token 
	And Get VisitNumber RequestorCode from DB "<VisitNumberQuery>" and "<RequestorCodeQuery>" 
	When Call POST Method for Bill Edit with all mandatory fields and verify Status Code as "200" 
	Then Verify visit number user code hand off type from responce body 
	And Verify entries should get inserted in the respective tables "<WorkFlowType>" and "<HandOffType>" and "<RegistrationsDetails>" and "<DetailFromDefectAccount>" and "<DetailsFromePARsActive>" and "<DetailsFromDefectHandoff>" and "<DetailsFromWorkflowStatus>" and "<DetailsFromAHCrossSite_CrossSiteRegistry>" and "<DetailsFromAHCrossSite_CrossSiteRegistryDetail>"
	Examples: 
		|VisitNumberQuery    |RequestorCodeQuery|WorkFlowType     |HandOffType|RegistrationsDetails|DetailFromDefectAccount     |DetailsFromePARsActive     |DetailsFromDefectHandoff     |DetailsFromWorkflowStatus     |DetailsFromAHCrossSite_CrossSiteRegistry     |DetailsFromAHCrossSite_CrossSiteRegistryDetail     |
		|DNNSelectEncounterID|DNNQuery          |ToGetWorkFlowType|HandOffType|ToGetRegistrationsID|ToGetDetailFromDefectAccount|ToGetDetailsFromePARsActive|ToGetDetailsFromDefectHandoff|ToGetDetailsFromWorkflowStatus|ToGetDetailsFromAHCrossSite_CrossSiteRegistry|ToGetDetailsFromAHCrossSite_CrossSiteRegistryDetail|

@25535 @439367 @APIGatewayBVT
Scenario Outline: Verify user is able to GET data from Activities endpoint 
	Given Store Access Token 
	And Get VisitNumber RequestorCode from DB "<VisitNumberQuery>" and "<RequestorCodeQuery>" 
	And Call POST Method for Bill Edit with all mandatory fields and verify Status Code as "200"
	When Call Activities Get Method and verify Status Code as "200"
	Then Verify Type from responce body
	And Verify timestamp from responce body
	And Verify why from responce body
	And Verify what from responce body
	And Verify note from responce body
	And Verify responce body from data base "<ProcesslogsQuery>"
	Examples:
			|VisitNumberQuery    |RequestorCodeQuery|ProcesslogsQuery|
			|DNNSelectEncounterID|DNNQuery          |ToGetProcesslogs|