@TokenValidation
Feature: Verify API Gateway Token Funtionality

@25966 @439366 @APIGatewayBVT
Scenario Outline: To validate that token should be valid only for that IP for which it was registered
Given Get unique clientName
When Call Auth Post Api with unique clientName and environment
Then Verify A response body should get generated and user should receive ClientSecret and ClientId
When Call Token Get Api with ClientSecret and ClientId
Then Verify An authorization token should get generated
When Get VisitNumber RequestorCode from DB for Token Validation "<VisitNumberQuery>" and "<RequestorCodeQuery>" 
And Call POST Method for Bill Edit with all mandatory fields for Token Validation and verify Status Code as "200"
And Call Activities Get Method for Token Validation and verify Status Code as "200"
Examples:
|VisitNumberQuery    |RequestorCodeQuery|
|DNNSelectEncounterID|DNNQuery          |