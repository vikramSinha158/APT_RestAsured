@Annotate
Feature: Verify Annotate API : R1D Workflow for Claim Status - Notes funtionality 

Background: Get Access Token 
	Given Call accretivehealth Api to get secret key 
	And Call auth Api toget access token
	And Store Access Token

@29799
Scenario Outline: Validate the POST operation is working for Notes and Entry saved in Process log table
Given Find User ID and User Name for Notes "<RequestorCodeQuery>"
And Find Visit Num from DB for Notes "<VisitNumberQuery>"
And Find Registration Id from DB for Notes "<RegistrationCodeQuery>"
When Call Post Method with all mandatory fields for Notes and verify status code "200"
Then Verify Responce body for Notes
And Verify added Notes will display in Process log table for Notes "<ProcessLogsDetailsQuery>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|ProcessLogsDetailsQuery|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |ToGetProcessLogsDetails|

@29800
Scenario Outline: Validate the POST operation is not working for Notes and Error will return in case of incorrect Visit Number
Given Find User ID and User Name for Notes "<RequestorCodeQuery>"
And Find Visit Num from DB for Notes "<VisitNumberQuery>"
And Find Registration Id from DB for Notes "<RegistrationCodeQuery>"
When Call Post Method With Blank Visit Number for Notes and verify status code "400"
Then Verify Responce Message for Notes "<ResponceMessageWithBlankVisitNumber>"
When Call Post Method With Invalid Visit Number for Notes and verify status code "400"
Then Verify Responce Message for Notes "<ResponceMessageWithInvalidVisitNumber>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|ResponceMessageWithBlankVisitNumber      |ResponceMessageWithInvalidVisitNumber|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |Visit Number value can't be null or empty|Invalid Visit Number for Facility    |

@29801
Scenario Outline: Validate the POST operation is not working for Notes and Error will return in case of incorrect UserCode
Given Find User ID and User Name for Notes "<RequestorCodeQuery>"
And Find Visit Num from DB for Notes "<VisitNumberQuery>"
And Find Registration Id from DB for Notes "<RegistrationCodeQuery>"
When Call Post Method With Invalid User Code for Notes and verify status code "400"
Then Verify Responce Message for Notes "<ResponceMessageWithInvalidUserCode>"
Examples:
|RequestorCodeQuery|VisitNumberQuery    |RegistrationCodeQuery|ResponceMessageWithInvalidUserCode|
|DNNQuery		   |DNNSelectEncounterID|ToGetRegistrationsID |Invalid performer code!           |


