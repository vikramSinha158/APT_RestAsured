package r1.restassured.stepdefination;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import junit.framework.Assert;
import r1.restassured.basesetup.BaseSetup;
import r1.restassured.commonutilities.CommonMethods;
import r1.restassured.commonutilities.DatabaseConn;
import r1.restassured.commonutilities.QueryExecutor;

public class ActivitiesAndEPARSHandoffAPIEndpointsSteps extends BaseSetup {

	ResponseOptions<Response> resBody;
	ResponseOptions<Response> resBodyGetMethod;
	ResponseOptions<Response> resBodyGetMethodTocken;
	ResponseOptions<Response> billEditResponce;

	String messageKey = "";
	String secretKey;
	String clientIdKey;
	String accessTocken;
	String requestorCode;
	String requestorId;
	String visitNumber;
	String apiUri;
	String whyResponceStr;
	String whatResponceStr;
	String statusResponceStr;

	JSONObject jsonObject;
	JSONArray jsonObjectArray;

	@Given("^Call accretivehealth Api to get secret key$")
	public void call_accretivehealth_Api_to_get_secret_key() throws Throwable {
		String pdsString;

		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		
		replaceMap.put("#####", BaseSetup.returnPropertyValue("ClientNameRegistration"));
		replaceMap.put("@@@@@", BaseSetup.returnPropertyValue("Environment"));

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/Authenticate.json",
				replaceMap);

		resBody = BaseSetup.postMethodRequest(BaseSetup.returnPropertyValue("AuthServicePostUri"), mapHeaders,
				pdsString);

	}

	@Given("^Call auth Api toget access token$")
	public void call_auth_Api_toget_access_token() throws Throwable {
		Map<String, String> mapHea = new HashMap<String, String>();

		try {
			messageKey = resBody.getBody().jsonPath().get("message").toString();
		} catch (Exception e) {

		}

		if (!messageKey.contains("Client already registered")) {
			secretKey = resBody.getBody().jsonPath().get("secret").toString();
			clientIdKey = resBody.getBody().jsonPath().get("clientId").toString();
			BaseSetup.storeValueInPropertyFile("DefaultSecretKey", secretKey);
			BaseSetup.storeValueInPropertyFile("DefaultClientIdKey", clientIdKey);
		} else {
			secretKey = BaseSetup.returnPropertyValue("DefaultSecretKey");
			clientIdKey = BaseSetup.returnPropertyValue("DefaultClientIdKey");
		}

		mapHea.put("ClientSecret", secretKey);
		mapHea.put("Clientid", clientIdKey);

		resBodyGetMethod = BaseSetup.getMethodRequest(BaseSetup.returnPropertyValue("AuthServiceGetUri"), mapHea);
	}

	@When("^Store Access Token$")
	public void Store_Access_Token() throws Throwable {
		accessTocken = resBodyGetMethod.getBody().jsonPath().get("token").toString();
		BaseSetup.storeValueInPropertyFile("AccessTocken", accessTocken);
	}

	@Given("^Get VisitNumber RequestorCode from DB \"([^\"]*)\" and \"([^\"]*)\"$")
	public void get_VisitNumber_RequestorCode_from_DB_and(String visitNumberQuery, String requestorCodeQuery)
			throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("DNNDataBaseName"),
				BaseSetup.returnQueryPropertyValue(requestorCodeQuery));
		DatabaseConn.resultSet.next();
		requestorCode = DatabaseConn.resultSet.getString("username");
		requestorId = DatabaseConn.resultSet.getString("userid");

		QueryExecutor.runQueryTran(visitNumberQuery);
		DatabaseConn.resultSet.next();
		visitNumber = DatabaseConn.resultSet.getString("encounterid");
	}

	@When("^Call POST Method for Bill Edit without what and verify Status Code as \"([^\"]*)\"$")
	public void call_POST_Method_for_Bill_Edit_without_what_and_verify_Status_Code_as(String statusCode)
			throws Throwable {
		String pdsString;

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> replaceMap = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + accessTocken);

		apiUri = BaseSetup.returnPropertyValue("BillEditPostUri");

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("Why"));
		replaceMap.put("$$$$$", "");

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/BillEdit.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			billEditResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (billEditResponce.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + billEditResponce.getStatusCode(),
				billEditResponce.getStatusCode() == statusInt);

	}
	
	@When("^Call POST Method for Bill Edit without why and verify Status Code as \"([^\"]*)\"$")
	public void Call_POST_Method_for_Bill_Edit_without_why_and_verify_Status_Code_as(String statusCode)
			throws Throwable {
		String pdsString;

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> replaceMap = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + accessTocken);

		apiUri = BaseSetup.returnPropertyValue("BillEditPostUri");

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", "");
		replaceMap.put("$$$$$", BaseSetup.returnPropertyValue("What"));

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/BillEdit.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			billEditResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (billEditResponce.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + billEditResponce.getStatusCode(),
				billEditResponce.getStatusCode() == statusInt);

	}
	
	@When("^Call POST Method for Bill Edit without encounterid and verify Status Code as \"([^\"]*)\"$")
	public void Call_POST_Method_for_Bill_Edit_without_encounterid_and_verify_Status_Code_as(String statusCode)
			throws Throwable {
		String pdsString;

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> replaceMap = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + accessTocken);

		apiUri = BaseSetup.returnPropertyValue("BillEditPostUri");

		replaceMap.put("!!!!!", "");
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("Why"));
		replaceMap.put("$$$$$", BaseSetup.returnPropertyValue("What"));

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/BillEdit.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			billEditResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (billEditResponce.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + billEditResponce.getStatusCode(),
				billEditResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Bill Edit Status Code as \"([^\"]*)\"$")
	public void verify_Bill_Edit_Status_Code_as(String statusCode) throws Throwable {
		int statusInt = Integer.parseInt(statusCode);
		Assert.assertTrue(statusInt + " Status Code is not displaying", billEditResponce.getStatusCode() == statusInt);
	}

	@Then("^Verify Bill Edit Validation Message as \"([^\"]*)\" and \"([^\"]*)\"$")
	public void verify_Bill_Edit_Validation_Message_as_and(String statusCode, String message) throws Throwable {
		Assert.assertTrue(statusCode + " Status Code is not displaying in responce body",
				billEditResponce.getBody().jsonPath().get("statusCode").toString().equals(statusCode.trim()));
		Assert.assertTrue(message + "  is not displaying in responce body",
				message.contains(billEditResponce.getBody().jsonPath().get("message").toString().replace("|", "")));
	}

	@When("^Call POST Method for Bill Edit with all mandatory fields and verify Status Code as \"([^\"]*)\"$")
	public void call_POST_Method_for_Bill_Edit_with_all_mandatory_fields_and_verify_Status_Code_as(String statusCode)
			throws Throwable {
		String pdsString;

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> replaceMap = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + accessTocken);

		apiUri = BaseSetup.returnPropertyValue("BillEditPostUri");

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("Why"));
		replaceMap.put("$$$$$", BaseSetup.returnPropertyValue("What"));

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/BillEdit.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			billEditResponce = BaseSetup.postMethodRequest(apiUri, mapHeaders, pdsString);

			if (billEditResponce.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + billEditResponce.getStatusCode(),
				billEditResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify visit number user code hand off type from responce body$")
	public void Verify_visit_number_user_code_hand_off_type_from_responce_body() throws Throwable {
		Assert.assertTrue(visitNumber + " Visit Number is not displaying in Responce Body",
				visitNumber.equals(billEditResponce.getBody().jsonPath().get("body.item.focus.identifier.value")
						.toString().replace("[", "").replace("]", "")));
		Assert.assertTrue(requestorCode + " User Code is not displaying in Responce Body",
				requestorCode.equals(billEditResponce.getBody().jsonPath().get("body.item.task.requestor.code")
						.toString().replace("[", "").replace("]", "")));
		Assert.assertTrue(
				BaseSetup.returnPropertyValue("HandOffType") + " Hand Off Type is not displaying in Responce Body",
				BaseSetup.returnPropertyValue("HandOffType").equals(billEditResponce.getBody().jsonPath()
						.get("body.item.partOf.identifier.value").toString().replace("[", "").replace("]", "")));
	}

	@Then("^Verify entries should get inserted in the respective tables \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
	public void verify_entries_should_get_inserted_in_the_respective_tables_and_and_and_and_and_and_and_and(
			String workFlowType, String handOffType, String registrationsDetails, String detailFromDefectAccount,
			String detailsFromePARsActive, String detailsFromDefectHandoff, String detailsFromWorkflowStatus,
			String detailsFromAHCrossSite_CrossSiteRegistry, String detailsFromAHCrossSiteCrossSiteRegistryDetail)
			throws Throwable {
		String workflowtypesId;
		String registrationId;
		String defectCreatedDateTime = null;
		String createdUserId = null;
		String lastWorkFlowTypeId = null;
		String date;
		String createdDateTime = null;
		String createdBy = null;
		String updatedDateTime = null;
		String updatedBy = null;
		String workFlowTypeId = null;
		String createdDate = null;
		String facilityCode = null;
		String encounterId = null;
		String updatedUserId = null;
		String faciCode;

		boolean flag = false;

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"), String.format(
						BaseSetup.returnQueryPropertyValue(workFlowType), BaseSetup.returnPropertyValue(handOffType)));
		DatabaseConn.resultSet.next();
		workflowtypesId = DatabaseConn.resultSet.getString("id");

		QueryExecutor.runQueryTranParam(registrationsDetails, visitNumber);
		DatabaseConn.resultSet.next();
		registrationId = DatabaseConn.resultSet.getString("id");

		// Verify Details From Defect Account

		QueryExecutor.runQueryTranParam(detailFromDefectAccount, registrationId);
		date = CommonMethods.getCurrentDate();

		try {
			while (DatabaseConn.resultSet.next()) {
				defectCreatedDateTime = DatabaseConn.resultSet.getString("defectcreateddatetime");
				createdUserId = DatabaseConn.resultSet.getString("createduserid");
				lastWorkFlowTypeId = DatabaseConn.resultSet.getString("lastworkflowtypeid");
				updatedDateTime = DatabaseConn.resultSet.getString("updateddatetime");
				updatedUserId = DatabaseConn.resultSet.getString("updateduserid");
				if ((defectCreatedDateTime.contains(date) || updatedDateTime.contains(date))
						&& (createdUserId.equals(requestorId) || updatedUserId.equals(requestorId))
						&& lastWorkFlowTypeId.equals(workflowtypesId)) {
					flag = true;
				}
			}

		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to DefectAccount table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (!flag) {
			Assert.assertTrue("Data has not been inserted in to DefectAccount table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		// Verify Details From PARsActive
		flag = false;

		QueryExecutor.runQueryTranParam(detailsFromePARsActive, registrationId);

		try {
			while (DatabaseConn.resultSet.next()) {
				createdDateTime = DatabaseConn.resultSet.getString("createddatetime");
				createdBy = DatabaseConn.resultSet.getString("createdby");
				updatedDateTime = DatabaseConn.resultSet.getString("updateddatetime");
				updatedBy = DatabaseConn.resultSet.getString("updatedby");

				if ((createdBy.equalsIgnoreCase(requestorId) || updatedBy.equalsIgnoreCase(requestorId))
						&& (createdDateTime.contains(date) || updatedDateTime.contains(date))) {
					flag = true;
				}
			}

		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to PARsActive table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (!flag) {
			Assert.assertTrue("Data has not been inserted in to PARsActive table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		// Verify Details From DefectHandoff

		flag = false;
		QueryExecutor.runQueryTranParam(detailsFromDefectHandoff, registrationId);

		try {
			while (DatabaseConn.resultSet.next()) {
				createdUserId = DatabaseConn.resultSet.getString("createduserid");
				createdDateTime = DatabaseConn.resultSet.getString("createddatetime");
				workFlowTypeId = DatabaseConn.resultSet.getString("workflowtypeid");
				updatedUserId = DatabaseConn.resultSet.getString("updateduserid");
				updatedDateTime = DatabaseConn.resultSet.getString("updateddatetime");

				if ((createdUserId.equals(requestorId) || updatedUserId.equals(requestorId))
						&& (createdDateTime.contains(date) || updatedDateTime.contains(date))
						&& workFlowTypeId.equals(workflowtypesId)) {
					flag = true;
				}
			}

		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to DefectHandoff table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (!flag) {
			Assert.assertTrue("Data has not been inserted in to DefectHandoff table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		// Verify Details From WorkflowStatus

		flag = false;
		QueryExecutor.runQueryTranParam(detailsFromWorkflowStatus, registrationId);

		try {
			while (DatabaseConn.resultSet.next()) {
				workFlowTypeId = DatabaseConn.resultSet.getString("workflowtypeid");
				createdBy = DatabaseConn.resultSet.getString("CreatedBy");
				createdDate = DatabaseConn.resultSet.getString("CreatedDate");
				updatedBy = DatabaseConn.resultSet.getString("updatedby");
				updatedDateTime = DatabaseConn.resultSet.getString("updateddate");

				if (workFlowTypeId.equals(workflowtypesId)
						&& (createdBy.equals(requestorId) || updatedBy.equals(requestorId))
						&& (createdDate.contains(date) || updatedDateTime.contains(date))) {
					flag = true;
				}
			}

		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to WorkflowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (!flag) {
			Assert.assertTrue("Data has not been inserted in to WorkflowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
		// Verify Details From AHCrossSite_CrossSiteRegistry

		flag = false;
		QueryExecutor.runQueryTranParam(detailsFromAHCrossSite_CrossSiteRegistry, registrationId);
		faciCode = BaseSetup.returnPropertyValue("Facility");

		try {
			while (DatabaseConn.resultSet.next()) {
				facilityCode = DatabaseConn.resultSet.getString("facilitycode");
				createdUserId = DatabaseConn.resultSet.getString("createduserid");
				createdDateTime = DatabaseConn.resultSet.getString("createddatetime");
				encounterId = DatabaseConn.resultSet.getString("encounterid");
				updatedUserId = DatabaseConn.resultSet.getString("updateduserid");
				updatedDateTime = DatabaseConn.resultSet.getString("updateddatetime");

				if (facilityCode.trim().equals(faciCode.trim()) && encounterId.trim().equals(visitNumber.trim())
						&& (createdUserId.equals(requestorId) || updatedUserId.equals(requestorId))
						&& (createdDateTime.contains(date) || updatedDateTime.contains(date))) {
					flag = true;
				}
			}

		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to AHCrossSite_CrossSiteRegistry table for "
					+ registrationId + " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (!flag) {
			Assert.assertTrue("Data has not been inserted in to AHCrossSite_CrossSiteRegistry table for "
					+ registrationId + " Registration Id and " + visitNumber + " Visit Number", false);
		}

		// Verify Details From AHCrossSite_CrossSiteRegistryDetail
		flag = false;
		QueryExecutor.runQueryTranParam(detailsFromAHCrossSiteCrossSiteRegistryDetail, registrationId);

		try {
			while (DatabaseConn.resultSet.next()) {
				facilityCode = DatabaseConn.resultSet.getString("facilitycode");
				if (facilityCode.trim().equals(faciCode.trim())) {
					flag = true;
				}
			}

		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to AHCrossSite_CrossSiteRegistryDetail table for "
					+ registrationId + " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (!flag) {
			Assert.assertTrue("Data has not been inserted in to AHCrossSite_CrossSiteRegistryDetail table for "
					+ registrationId + " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}

	@When("^Call Activities Get Method and verify Status Code as \"([^\"]*)\"$")
	public void call_Activities_Get_Method_and_verify_Status_Code_as(String statusCode) throws Throwable {
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaa = new HashMap<String, String>();

		mapHeaa.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaa.put("Authorization", "Bearer " + accessTocken);

		mapParam.put("visit", visitNumber);

		apiUri = BaseSetup.returnPropertyValue("BillEditGetUri");

		for (int i = 0; i < count; i++) {
			resBodyGetMethodTocken = BaseSetup.getMethodRequestWithParam(apiUri, mapHeaa, mapParam);

			if (resBodyGetMethodTocken.getStatusCode() == statusInt) {
				break;
			} else {
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + resBodyGetMethodTocken.getStatusCode(),
				resBodyGetMethodTocken.getStatusCode() == statusInt);

	}

	@Then("^Verify Get Method Status Code as \"([^\"]*)\"$")
	public void verify_Get_Method_Status_Code_as(String statusCode) throws Throwable {
		int statusInt = Integer.parseInt(statusCode);
		Assert.assertTrue(statusInt + " Status Code is not displaying",
				resBodyGetMethodTocken.getStatusCode() == statusInt);
	}

	@Then("^Verify Type from responce body$")
	public void verify_Type_from_responce_body() throws Throwable {
		Assert.assertTrue("ActivityResponse is not displaying in Type of get responce", "ActivityResponse"
				.equalsIgnoreCase(resBodyGetMethodTocken.getBody().jsonPath().get("type").toString()));
	}

	@Then("^Verify timestamp from responce body$")
	public void verify_timestamp_from_responce_body() throws Throwable {
		String currentDate = CommonMethods.getCurrentDate();
		Assert.assertTrue(currentDate + " is not displaying in timestamp of get responce",
				resBodyGetMethodTocken.getBody().jsonPath().get("timestamp").toString().trim().contains(currentDate));
	}

	@Then("^Verify why from responce body$")
	public void Verify_why_from_responce_body() throws Throwable {
		String whyStr;

		jsonObject = CommonMethods.convertSubStringToJson(resBodyGetMethodTocken.getBody().asString(),
				BaseSetup.returnPropertyValue("AccretiveSplitString"),
				BaseSetup.returnPropertyValue("AccretiveSplitStringLast"));
		jsonObject = jsonObject.getJSONArray("task").getJSONObject(0);
		whyResponceStr = jsonObject.getString("name");
		statusResponceStr = jsonObject.getString("status");
		whyStr = BaseSetup.returnPropertyValue("Why");
		Assert.assertTrue(whyStr + " is not displaying responce body", whyStr.equalsIgnoreCase(whyResponceStr));
	}

	@Then("^Verify what from responce body$")
	public void Verify_what_from_responce_body() throws Throwable {
		boolean flag = false;
		String whatStr = BaseSetup.returnPropertyValue("What");
		jsonObjectArray = jsonObject.getJSONArray("note");

		for (int i = 0; i < jsonObjectArray.length(); i++) {
			JSONObject rec = jsonObjectArray.getJSONObject(i);
			if (rec.getString("text").equalsIgnoreCase(whatStr)) {
				flag = true;
				break;
			}
		}

		if (!flag) {
			Assert.assertFalse(whatStr + " is not displaying in responce body as what", true);
		}

	}

	@Then("^Verify note from responce body$")
	public void Verify_note_from_responce_body() throws Throwable {
		boolean flag = false;
		for (int i = 0; i < jsonObjectArray.length(); i++) {
			JSONObject rec = jsonObjectArray.getJSONObject(i);
			if (rec.getString("text").contains("Automation testing")) {
				flag = true;
				break;
			}
		}

		if (!flag) {
			Assert.assertFalse("Automation testing is not displaying in responce body as note", true);
		}
	}

	@Then("^Verify responce body from data base \"([^\"]*)\"$")
	public void Verify_responce_body_from_data_base(String query) throws Throwable {
		String status;
		String note;
		String date;
		String currentDate;

		QueryExecutor.runQueryTranParam(query, requestorId);
		DatabaseConn.resultSet.next();
		status = DatabaseConn.resultSet.getString("status");
		note = DatabaseConn.resultSet.getString("note");
		date = DatabaseConn.resultSet.getString("createddatetime");
		currentDate = CommonMethods.getCurrentDate();

		Assert.assertTrue(statusResponceStr + " Status is not displaying in processlogs table",
				status.trim().equals(statusResponceStr.trim()));
		Assert.assertTrue("Automation testing is not displaying in processlogs table",
				note.contains("Automation testing"));
		Assert.assertTrue(currentDate + " is not displaying in processlogs table", date.contains(currentDate));

	}

	@Then("^Verify status for accretivehealth Api \"([^\"]*)\" or \"([^\"]*)\"$")
	public void verify_status_for_accretivehealth_Api_or(String successCode, String forbiddenCode) throws Throwable {
		int statusInt = Integer.parseInt(successCode);
		int statusForInt = Integer.parseInt(forbiddenCode);
		Assert.assertTrue(
				"Expected status code is :-" + successCode + "OR" + forbiddenCode + " But it is showing "
						+ resBody.getStatusCode(),
				resBody.getStatusCode() == statusInt || resBody.getStatusCode() == statusForInt);
	}

	@Then("^Verify response body should get generated and user should receive ClientSecret and ClientId$")
	public void verify_response_body_should_get_generated_and_user_should_receive_ClientSecret_and_ClientId()
			throws Throwable {
		try {
			messageKey = resBody.getBody().jsonPath().get("message").toString();
		} catch (Exception e) {

		}

		if (!messageKey.contains("Client already registered")) {
			try {
				secretKey = resBody.getBody().jsonPath().get("secret").toString();
				clientIdKey = resBody.getBody().jsonPath().get("clientId").toString();
			} catch (Exception e) {
				Assert.assertTrue("secret key & client Id are not displaying in responce body", false);
			}
			Assert.assertTrue("Blank secret key is displaying", !secretKey.equalsIgnoreCase(""));
			Assert.assertTrue("Blank client Id Key is displaying", !clientIdKey.equalsIgnoreCase(""));
		} else {
			Assert.assertTrue("Client already registered message is not displaying in responce",
					messageKey.contains("Client already registered"));
		}
	}

	@Then("^Verify Get Method Status Code \"([^\"]*)\"$")
	public void verify_Get_Method_Status_Code(String statusCode) throws Throwable {
		int statusInt = Integer.parseInt(statusCode);
		Assert.assertTrue(statusInt + " Status Code is not displaying", resBodyGetMethod.getStatusCode() == statusInt);
	}

	@Then("^Verify an authorization token should get generated$")
	public void verify_an_authorization_token_should_get_generated() throws Throwable {
		accessTocken = resBodyGetMethod.getBody().jsonPath().get("token").toString();
		Assert.assertTrue("An authorization token should not get generated", !accessTocken.equals(""));
	}

	@Given("^Get visit num from DB \"([^\"]*)\"$")
	public void get_visit_num_from_DB(String visitNumberQuery) throws Throwable {
		QueryExecutor.runQueryTran(visitNumberQuery);
		DatabaseConn.resultSet.next();
		visitNumber = DatabaseConn.resultSet.getString("encounterid");
	}

	@When("^Wait for fifteen minutes$")
	public void wait_for_fifteen_minutes() throws Throwable {
		Thread.sleep(960000);
	}

}
