package r1.restassured.stepdefination;

import java.util.HashMap;
import java.util.Map;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import junit.framework.Assert;
import r1.restassured.basesetup.BaseSetup;
import r1.restassured.commonutilities.CommonMethods;
import r1.restassured.commonutilities.DatabaseConn;
import r1.restassured.commonutilities.QueryExecutor;

public class ApiGatewayTokenValidationsSteps extends BaseSetup {

	private String clientName;
	private String secretKeyTokenValidation;
	private String clientIdKeyTokenValidation;
	private String accessTokenTokenValidation;
	private String requestorCodeTokenValidation;
	private String requestorIdTokenValidation;
	private String visitNumberTokenValidation;

	ResponseOptions<Response> postResponceTokenValidation;
	ResponseOptions<Response> restResponceTokenValidation;
	ResponseOptions<Response> billEditResponceTokenValidation;
	ResponseOptions<Response> resBodyGetMethodTockenValidation;

	@Given("^Get unique clientName$")
	public void get_unique_clientName() throws Throwable {
		clientName = CommonMethods.getRandomString(10);
	}

	@When("^Call Auth Post Api with unique clientName and environment$")
	public void call_Auth_Post_Api_with_unique_clientName_and_environment() throws Throwable {
		String pdsString;

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");

		for (int i = 0; i <= 10; i++) {
			replaceMap.put("@@@@@", clientName);
			replaceMap.put("#####", BaseSetup.returnPropertyValue("Environment"));

			pdsString = CommonMethods
					.getResourceFromFile("src/test/resources/TestData/JsonFiles/AuthenticateReplace.json", replaceMap);

			postResponceTokenValidation = BaseSetup
					.postMethodRequest(BaseSetup.returnPropertyValue("AuthServicePostUri"), mapHeaders, pdsString);

			try {
				postResponceTokenValidation.getBody().jsonPath().get("secret").toString();
				postResponceTokenValidation.getBody().jsonPath().get("clientId").toString();
				break;
			} catch (Exception e) {
				Thread.sleep(30000);
				clientName = CommonMethods.getRandomString(10);
			}
		}

	}

	@Then("^Verify A response body should get generated and user should receive ClientSecret and ClientId$")
	public void verify_A_response_body_should_get_generated_and_user_should_receive_ClientSecret_and_ClientId()
			throws Throwable {
		try {
			secretKeyTokenValidation = postResponceTokenValidation.getBody().jsonPath().get("secret").toString();
			clientIdKeyTokenValidation = postResponceTokenValidation.getBody().jsonPath().get("clientId").toString();
		} catch (Exception e) {
			Assert.assertTrue("ClientSecret and ClientId has not been generated and showing message:- "+postResponceTokenValidation.getBody().jsonPath().get("message").toString() , false);
		}

		Assert.assertTrue("ClientSecret has not been generated", !secretKeyTokenValidation.equals("")
				|| !secretKeyTokenValidation.equalsIgnoreCase("null") || !secretKeyTokenValidation.equals(null));
		Assert.assertTrue("ClientId has not been generated", !clientIdKeyTokenValidation.equals("")
				|| !clientIdKeyTokenValidation.equalsIgnoreCase("null") || !clientIdKeyTokenValidation.equals(null));
	}

	@When("^Call Token Get Api with ClientSecret and ClientId$")
	public void call_Token_Get_Api_with_ClientSecret_and_ClientId() throws Throwable {
		Map<String, String> mapHea = new HashMap<String, String>();

		mapHea.put("ClientSecret", secretKeyTokenValidation);
		mapHea.put("Clientid", clientIdKeyTokenValidation);

		restResponceTokenValidation = BaseSetup.getMethodRequest(BaseSetup.returnPropertyValue("AuthServiceGetUri"),
				mapHea);
	}

	@Then("^Verify An authorization token should get generated$")
	public void verify_An_authorization_token_should_get_generated() throws Throwable {
		try {
			accessTokenTokenValidation = restResponceTokenValidation.getBody().jsonPath().get("token").toString();
		} catch (Exception e) {
			Assert.assertTrue("Authorization Token has not been generated",
					!accessTokenTokenValidation.equals("") || !accessTokenTokenValidation.equalsIgnoreCase("null")
							|| !accessTokenTokenValidation.equals(null));
		}

		Assert.assertTrue("Authorization Token has not been generated", !accessTokenTokenValidation.equals("")
				|| !accessTokenTokenValidation.equalsIgnoreCase("null") || !accessTokenTokenValidation.equals(null));
	}

	@When("^Get VisitNumber RequestorCode from DB for Token Validation \"([^\"]*)\" and \"([^\"]*)\"$")
	public void get_VisitNumber_RequestorCode_from_DB_for_Token_Validation_and(String visitNumberQuery,
			String requestorCodeQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("DNNDataBaseName"),
				BaseSetup.returnQueryPropertyValue(requestorCodeQuery));
		DatabaseConn.resultSet.next();
		requestorCodeTokenValidation = DatabaseConn.resultSet.getString("username");
		requestorIdTokenValidation = DatabaseConn.resultSet.getString("userid");

		QueryExecutor.runQueryTran(visitNumberQuery);
		DatabaseConn.resultSet.next();
		visitNumberTokenValidation = DatabaseConn.resultSet.getString("encounterid");
	}

	@When("^Call POST Method for Bill Edit with all mandatory fields for Token Validation and verify Status Code as \"([^\"]*)\"$")
	public void call_POST_Method_for_Bill_Edit_with_all_mandatory_fields_for_Token_Validation_and_verify_Status_Code_as(
			String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("BillEditPostUri");

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> replaceMap = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + accessTokenTokenValidation);

		replaceMap.put("!!!!!", visitNumberTokenValidation);
		replaceMap.put("@@@@@", requestorCodeTokenValidation);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("Why"));
		replaceMap.put("$$$$$", BaseSetup.returnPropertyValue("What"));

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/BillEdit.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			billEditResponceTokenValidation = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (billEditResponceTokenValidation.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue(
				"Expected Code is:- " + statusInt + " but found:- " + billEditResponceTokenValidation.getStatusCode(),
				billEditResponceTokenValidation.getStatusCode() == statusInt);

	}

	@When("^Call Activities Get Method for Token Validation and verify Status Code as \"([^\"]*)\"$")
	public void call_Activities_Get_Method_for_Token_Validation_and_verify_Status_Code_as(String statusCode)
			throws Throwable {
		String apiUri = BaseSetup.returnPropertyValue("BillEditGetUri");

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaa = new HashMap<String, String>();

		mapHeaa.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaa.put("Authorization", "Bearer " + accessTokenTokenValidation);

		mapParam.put("visit", visitNumberTokenValidation);

		for (int i = 0; i < count; i++) {
			resBodyGetMethodTockenValidation = BaseSetup.getMethodRequestWithParam(apiUri, mapHeaa, mapParam);

			if (resBodyGetMethodTockenValidation.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue(
				"Expected Code is:- " + statusInt + " but found:- " + resBodyGetMethodTockenValidation.getStatusCode(),
				resBodyGetMethodTockenValidation.getStatusCode() == statusInt);

	}

}
