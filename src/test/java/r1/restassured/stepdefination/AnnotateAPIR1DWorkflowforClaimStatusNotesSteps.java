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

public class AnnotateAPIR1DWorkflowforClaimStatusNotesSteps extends BaseSetup {
	public ResponseOptions<Response> annotateNotesPostResponce;
	
	public String requestorCode;
	public String requestorId;
	public String visitNumber;
	String registrationId;
	
	@Given("^Find User ID and User Name for Notes \"([^\"]*)\"$")
	public void find_User_ID_and_User_Name_for_Notes(String requestorCodeQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("DNNDataBaseName"),
				BaseSetup.returnQueryPropertyValue(requestorCodeQuery));
		DatabaseConn.resultSet.next();
		requestorCode = DatabaseConn.resultSet.getString("username");
		requestorId = DatabaseConn.resultSet.getString("userid");
	}
	
	@Given("^Find Visit Num from DB for Notes \"([^\"]*)\"$")
	public void find_Visit_Num_from_DB_for_Notes(String visitNumberQuery) throws Throwable {
		QueryExecutor.runQueryTran(visitNumberQuery);
		DatabaseConn.resultSet.next();
		visitNumber = DatabaseConn.resultSet.getString("encounterid");
	}
	
	@Given("^Find Registration Id from DB for Notes \"([^\"]*)\"$")
	public void find_Registration_Id_from_DB_for_Notes(String registrationCodeQuery) throws Throwable {
		QueryExecutor.runQueryTranParam(registrationCodeQuery, visitNumber);
		DatabaseConn.resultSet.next();
		registrationId = DatabaseConn.resultSet.getString("id");
	}
	
	@When("^Call Post Method with all mandatory fields for Notes and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_with_all_mandatory_fields_for_Notes_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("AnnotateNotesPostUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("AnnotateNotesPostNote"));
		
		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/AnnotateNotes.json", replaceMap);

		for (int i = 0; i < count; i++) {
			annotateNotesPostResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (annotateNotesPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = annotateNotesPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = annotateNotesPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + annotateNotesPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, annotateNotesPostResponce.getStatusCode() == statusInt);


	}

	@Then("^Verify Responce body for Notes$")
	public void verify_Responce_body_for_Notes() throws Throwable {
		Assert.assertTrue("In Responce body, Expected Type is:- User but found:- "+annotateNotesPostResponce.getBody().jsonPath().get("user.type").toString(), annotateNotesPostResponce.getBody().jsonPath().get("user.type").toString().equalsIgnoreCase("User"));
		Assert.assertTrue("In Responce body, Expected User Code is:- "+requestorCode+" But found:-  "+annotateNotesPostResponce.getBody().jsonPath().get("user.code").toString() , annotateNotesPostResponce.getBody().jsonPath().get("user.code").toString().equalsIgnoreCase(requestorCode));
	}

	@Then("^Verify added Notes will display in Process log table for Notes \"([^\"]*)\"$")
	public void verify_added_Notes_will_display_in_Process_log_table_for_Notes(String processLogsDetailsQuery) throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(processLogsDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("Note").trim().equals(BaseSetup.returnPropertyValue("AnnotateNotesPostNote").trim())
						&& DatabaseConn.resultSet.getString("CreatedDateTime").contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
					break;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 1) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}
	
	@When("^Call Post Method With Blank Visit Number for Notes and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_Blank_Visit_Number_for_Notes_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("AnnotateNotesPostUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", "");
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("AnnotateNotesPostNote"));
		
		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/AnnotateNotes.json", replaceMap);

		for (int i = 0; i < count; i++) {
			annotateNotesPostResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (annotateNotesPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = annotateNotesPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = annotateNotesPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + annotateNotesPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, annotateNotesPostResponce.getStatusCode() == statusInt);


	}

	@Then("^Verify Responce Message for Notes \"([^\"]*)\"$")
	public void verify_Responce_Message_for_Notes(String errorMessage) throws Throwable {
		String resMessage = "";
		
		try {
			resMessage = annotateNotesPostResponce.getBody().jsonPath().get("Message").toString();
		} catch (Exception e) {
			try {
				resMessage = annotateNotesPostResponce.getBody().jsonPath().get("message").toString();
			} catch (Exception ex) {
			}
		}

		Assert.assertTrue(
				"Expected Message should contains :- " + errorMessage + "But Found :- " + resMessage,
				resMessage.contains(errorMessage));

	}

	@When("^Call Post Method With Invalid Visit Number for Notes and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_Invalid_Visit_Number_for_Notes_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("AnnotateNotesPostUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", BaseSetup.returnPropertyValue("AnnotateNotesInvalidVisitNum"));
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("AnnotateNotesPostNote"));
		
		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/AnnotateNotes.json", replaceMap);

		for (int i = 0; i < count; i++) {
			annotateNotesPostResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (annotateNotesPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = annotateNotesPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = annotateNotesPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + annotateNotesPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, annotateNotesPostResponce.getStatusCode() == statusInt);

	}
	
	@When("^Call Post Method With Blank User Code for Notes and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_Blank_User_Code_for_Notes_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("AnnotateNotesPostUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", "");
		replaceMap.put("#####", BaseSetup.returnPropertyValue("AnnotateNotesPostNote"));
		
		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/AnnotateNotes.json", replaceMap);

		for (int i = 0; i < count; i++) {
			annotateNotesPostResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (annotateNotesPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = annotateNotesPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = annotateNotesPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + annotateNotesPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, annotateNotesPostResponce.getStatusCode() == statusInt);



	}

	@When("^Call Post Method With Invalid User Code for Notes and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_Invalid_User_Code_for_Notes_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("AnnotateNotesPostUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", BaseSetup.returnPropertyValue("AnnotateNotesInvalidUserCode"));
		replaceMap.put("#####", BaseSetup.returnPropertyValue("AnnotateNotesPostNote"));
		
		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/AnnotateNotes.json", replaceMap);

		for (int i = 0; i < count; i++) {
			annotateNotesPostResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (annotateNotesPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = annotateNotesPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = annotateNotesPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + annotateNotesPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, annotateNotesPostResponce.getStatusCode() == statusInt);




	}

}
