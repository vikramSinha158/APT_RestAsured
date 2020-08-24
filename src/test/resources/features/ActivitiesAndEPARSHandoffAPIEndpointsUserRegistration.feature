@BillEdit 
Feature: Verify Activities and EPARS Handoff API Endpoints funtionality For User Registration

@25533 @439365 @APIGatewayBVT 
Scenario: Verify that user registration API is working properly
When Call accretivehealth Api to get secret key
Then Verify status for accretivehealth Api "200" or "403"
And Verify response body should get generated and user should receive ClientSecret and ClientId

@25534
Scenario: Verify user is able to generate Token from the clientid and client secret that had been provided
Given Call accretivehealth Api to get secret key
When Call auth Api toget access token
Then Verify Get Method Status Code "200"
And Verify an authorization token should get generated

@25538
Scenario Outline: Verify that user is not able to use the token after expiry time ( 15 mnts )
Given Call accretivehealth Api to get secret key
And Call auth Api toget access token
And Verify Get Method Status Code "200"
And Store Access Token
And Get visit num from DB "<VisitNumberQuery>"
When Wait for fifteen minutes
Then Call Activities Get Method and verify Status Code as "401"
Examples:
|VisitNumberQuery    |
|DNNSelectEncounterID|



