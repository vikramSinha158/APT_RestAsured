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

public class DefectCategoryActionAndNotesSteps extends BaseSetup {

	public ResponseOptions<Response> defectCatPostResponce;
	public String requestorCode;
	public String requestorId;
	public String visitNumber;
	String registrationId;
	String defectCategory;
	String defectCategoryId;
	String defectSubCategory;
	String defectSubCategoryId;
	String resultText;
	String resultTextId;
	String previousDefectCategory;
	String previousDefectSubCategory;
	String previousDefectSubCategoryId;
	String previousResultText;
	String previousResultTextId;
	String currentResultText;
	String futureResultText;
	String currentResultTextId;
	String futureResultTextId;
	String notRequiredResultTextId;
	String notRequiredResultText;
	String requiredResultTextId;
	String requiredResultText;

	@Given("^Find User ID and User Name \"([^\"]*)\"$")
	public void find_User_ID_and_User_Name(String requestorCodeQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("DNNDataBaseName"),
				BaseSetup.returnQueryPropertyValue(requestorCodeQuery));
		DatabaseConn.resultSet.next();
		requestorCode = DatabaseConn.resultSet.getString("username");
		requestorId = DatabaseConn.resultSet.getString("userid");
	}

	@Given("^Find unclassifiedAccount from DB \"([^\"]*)\" and \"([^\"]*)\" and \\\"([^\\\"]*)\\\"$")
	public void find_unclassifiedAccount_from_DB_and_and(String visitNumberQuery, String registrationCodeQuery,
			String defectAccountDetailsQuery) throws Throwable {
		for (int i = 0; i <= 100; i++) {
			QueryExecutor.runQueryTran(visitNumberQuery);
			DatabaseConn.resultSet.next();
			visitNumber = DatabaseConn.resultSet.getString("encounterid");

			QueryExecutor.runQueryTranParam(registrationCodeQuery, visitNumber);
			DatabaseConn.resultSet.next();
			registrationId = DatabaseConn.resultSet.getString("id");

			QueryExecutor.runQueryTranParam(defectAccountDetailsQuery, registrationId);
			if (DatabaseConn.resultSet.next() == false) {
				break;
			}

		}

	}

	@Given("^Find DefectCateogory and DefectSubCateogory and ResultText from DB \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
	public void find_DefectCateogory_and_DefectSubCateogory_and_ResultText_from_DB_and_and(String defectCateogoryQuery,
			String defectSubCategoryQuery, String resultTextQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(defectCateogoryQuery));
		DatabaseConn.resultSet.next();
		defectCategory = DatabaseConn.resultSet.getString("DefectTypeDesc");

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(defectSubCategoryQuery));
		DatabaseConn.resultSet.next();
		defectSubCategory = DatabaseConn.resultSet.getString("DefectSubCategoryDesc");
		defectSubCategoryId = DatabaseConn.resultSet.getString("DefectSubCategoryID");

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(resultTextQuery), defectSubCategoryId));
		DatabaseConn.resultSet.next();
		resultText = DatabaseConn.resultSet.getString("Name");
		resultTextId=DatabaseConn.resultSet.getString("ID");

	}

	@When("^Call Post Method for defect category with param value and Verify Status code \"([^\"]*)\"$")
	public void call_Post_Method_for_defect_category_with_param_value_and_Verify_Status_code(String statusCode)
			throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", resultText);

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + defectCatPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify A record Defect Sub Category should be inserted in DefectAccount table \"([^\"]*)\"$")
	public void Verify_A_record_Defect_Sub_Category_should_be_inserted_in_DefectAccount_table(
			String defectAccountDetailsQuery) throws Throwable {

		String defectSubCatId = null;
		QueryExecutor.runQueryTranParam(defectAccountDetailsQuery, registrationId);
		DatabaseConn.resultSet.next();
		try {
			defectSubCatId = DatabaseConn.resultSet.getString("DefectSubCategoryID");
		} catch (Exception e) {
			Assert.assertTrue("Data has not been inserted in to DefectAccount table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		Assert.assertTrue("Expected Defect Sub Category id for DefectAccount table is:- " + defectSubCategoryId
				+ "but it is showing:- " + defectSubCatId, defectSubCategoryId.equals(defectSubCatId));
	}

	@Given("^Find New DefectCateogory and DefectSubCateogory and ResultText from DB \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
	public void find_New_DefectCateogory_and_DefectSubCateogory_and_ResultText_from_DB_and_and(
			String newDefectCateogoryQuery, String newDefectSubCategoryQuery, String newResultTextQuery)
			throws Throwable {
		previousDefectCategory = defectCategory;
		previousDefectSubCategory = defectSubCategory;
		previousDefectSubCategoryId = defectSubCategoryId;
		previousResultText = resultText;
		previousResultTextId=resultTextId;

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(newDefectCateogoryQuery));
		DatabaseConn.resultSet.next();
		defectCategory = DatabaseConn.resultSet.getString("DefectTypeDesc");

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"), String.format(
						BaseSetup.returnQueryPropertyValue(newDefectSubCategoryQuery), previousDefectSubCategoryId));
		DatabaseConn.resultSet.next();
		defectSubCategory = DatabaseConn.resultSet.getString("DefectSubCategoryDesc");
		defectSubCategoryId = DatabaseConn.resultSet.getString("DefectSubCategoryID");

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(newResultTextQuery), defectSubCategoryId));
		DatabaseConn.resultSet.next();
		resultText = DatabaseConn.resultSet.getString("Name");
		resultTextId=DatabaseConn.resultSet.getString("ID");

	}

	@Given("^Verify The previous record from DefectAccount table should be inserted in DefectAccountHistory table \"([^\"]*)\"$")
	public void verify_The_previous_record_from_DefectAccount_table_should_be_inserted_in_DefectAccountHistory_table(
			String defectAccountHistoryDetailQuery) throws Throwable {
		String defectSubCatId = null;
		QueryExecutor.runQueryTranParam(defectAccountHistoryDetailQuery, registrationId, previousDefectSubCategoryId);
		DatabaseConn.resultSet.next();
		try {
			defectSubCatId = DatabaseConn.resultSet.getString("DefectSubCategoryID");
		} catch (Exception e) {
			Assert.assertTrue(
					"The previous record from Payer.DefectAccount table has not been inserted in Payer.DefectAccountHistory table for "
							+ registrationId + " Registration Id and " + visitNumber + " Visit Number",
					false);
		}
	}

	@Given("^Find Visit Num from DB \"([^\"]*)\"$")
	public void find_Visit_Num_from_DB(String visitNumberQuery) throws Throwable {
		QueryExecutor.runQueryTran(visitNumberQuery);
		DatabaseConn.resultSet.next();
		visitNumber = DatabaseConn.resultSet.getString("encounterid");
	}

	@Given("^Find Registration Id from DB \"([^\"]*)\"$")
	public void find_Registration_Id_from_DB(String registrationCodeQuery) throws Throwable {
		QueryExecutor.runQueryTranParam(registrationCodeQuery, visitNumber);
		DatabaseConn.resultSet.next();
		registrationId = DatabaseConn.resultSet.getString("id");
	}

	@Given("^Find DefectCateogory from DB \"([^\"]*)\"$")
	public void find_DefectCateogory_from_DB(String defectCateogoryQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(defectCateogoryQuery));
		DatabaseConn.resultSet.next();
		defectCategory = DatabaseConn.resultSet.getString("DefectTypeDesc");
	}
	
	@Given("^Get DefectCateogory and DefectCateogoryID from DB \"([^\"]*)\"$")
	public void Get_DefectCateogory_and_DefectCateogoryID_from_DB(String defectCateogoryQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(defectCateogoryQuery));
		DatabaseConn.resultSet.next();
		defectCategory = DatabaseConn.resultSet.getString("DefectTypeDesc");
		defectCategoryId=DatabaseConn.resultSet.getString("DefectTypeID");
	}

	@Given("^Find DefectSubCateogory from DB \"([^\"]*)\"$")
	public void find_DefectSubCateogory_from_DB(String defectSubCategoryQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(defectSubCategoryQuery));
		DatabaseConn.resultSet.next();
		defectSubCategory = DatabaseConn.resultSet.getString("DefectSubCategoryDesc");
		defectSubCategoryId = DatabaseConn.resultSet.getString("DefectSubCategoryID");
	}
	
	@Given("^Get DefectSubCateogory and DefectSubCateogoryID from DB \"([^\"]*)\"$")
	public void Get_DefectSubCateogory_and_DefectSubCateogoryID_from_DB(String defectSubCategoryQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(defectSubCategoryQuery), defectCategoryId));
		DatabaseConn.resultSet.next();
		defectSubCategory = DatabaseConn.resultSet.getString("DefectSubCategoryDesc");
		defectSubCategoryId = DatabaseConn.resultSet.getString("DefectSubCategoryID");
	}

	@Given("^Find Current follow up Action from DB \"([^\"]*)\"$")
	public void Find_Current_follow_up_Action_from_DB(String resultTextQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(resultTextQuery), defectSubCategoryId));
		DatabaseConn.resultSet.next();
		currentResultText = DatabaseConn.resultSet.getString("Name");
		currentResultTextId = DatabaseConn.resultSet.getString("ActionID");

	}

	@Given("^Find Future follow up Action from DB \"([^\"]*)\"$")
	public void Find_Future_follow_up_Action_from_DB(String resultTextQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(resultTextQuery), defectSubCategoryId));
		DatabaseConn.resultSet.next();
		futureResultText = DatabaseConn.resultSet.getString("Name");
		futureResultTextId = DatabaseConn.resultSet.getString("ActionID");

	}

	@When("^Call Post Method with two Actions and Verify Status code \"([^\"]*)\"$")
	public void call_Post_Method_with_two_Actions_and_Verify_Status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", currentResultText);
		replaceMap.put("~~~~~", futureResultText);

		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/DefectCategoryWithTwoActions.json", replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + defectCatPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Record should be inserted in ProcessLogs table \"([^\"]*)\"$")
	public void verify_Record_should_be_inserted_in_ProcessLogs_table(String processLogsDetailsQuery) throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(processLogsDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextAction").trim().equals(currentResultTextId.trim())
						|| DatabaseConn.resultSet.getString("NextAction").trim().equals(futureResultTextId.trim())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 2) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}

	@Then("^Verify Record should be inserted in WorkFlowStatus table \"([^\"]*)\"$")
	public void verify_Record_should_be_inserted_in_WorkFlowStatus_table(String workFlowStatusDetailsQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(workFlowStatusDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextWFTActionID").trim().equals(futureResultTextId.trim())
						&& DatabaseConn.resultSet.getString("Note").trim()
								.equals(BaseSetup.returnPropertyValue("NoteText").trim())
						&& DatabaseConn.resultSet.getString("CreatedDate").contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
				+ " Registration Id and " + visitNumber + " Visit Number", recordCount == 1);

		recordCount = 0;
		QueryExecutor.runQueryTranParam(workFlowStatusDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextWFTActionID").trim().equals(currentResultTextId.trim())
						&& DatabaseConn.resultSet.getString("Note").trim()
								.equals(BaseSetup.returnPropertyValue("NoteText").trim())
						&& DatabaseConn.resultSet.getString("CreatedDate").contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		Assert.assertTrue("Other then most later FollowUp date record has been inserted in WorkFlowStatus table for "
				+ registrationId + " Registration Id and " + visitNumber + " Visit Number", recordCount == 0);

	}

	@Then("^Verify Record should be inserted in CrossSiteAction table \"([^\"]*)\"$")
	public void verify_Record_should_be_inserted_in_CrossSiteAction_table(String crossSiteActionDetailsQuery)
			throws Throwable {
		boolean flag = false;
		QueryExecutor.runQueryTranParam(crossSiteActionDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("FacilityCode").trim()
						.equals(BaseSetup.returnPropertyValue("Facility").trim())
						&& DatabaseConn.resultSet.getString("ActionDate").contains(CommonMethods.getCurrentDate())) {
					flag = true;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in CrossSiteAction table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		Assert.assertTrue("Records has not been inserted in CrossSiteAction table for " + registrationId
				+ " Registration Id and " + visitNumber + " Visit Number", flag);
	}

	@Then("^Verify Record should be inserted in DefectAccountAttribute table \"([^\"]*)\"$")
	public void verify_Record_should_be_inserted_in_DefectAccountAttribute_table(
			String defectAccountAttributeDetailsQuery) throws Throwable {
		boolean flag = false;
		QueryExecutor.runQueryTran(defectAccountAttributeDetailsQuery);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("createduserid").trim().equals(requestorId.trim())) {
					flag = true;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue(
					"Records has not been inserted in DefectAccountAttribute table for " + registrationId
							+ " Registration Id and " + visitNumber + " Visit Number and " + requestorId + "Creator ID",
					false);
		}

		if (!flag) {
			Assert.assertTrue(
					"Records has not been inserted in DefectAccountAttribute table for " + registrationId
							+ " Registration Id and " + visitNumber + " Visit Number and " + requestorId + "Creator ID",
					false);
		}
	}

	@Given("^Store Current follow up Action and Find Current follow up Action from DB \"([^\"]*)\"$")
	public void store_Current_follow_up_Action_and_Find_Current_follow_up_Action_from_DB(
			String resultTextQueryForCurrentActionsQuery) throws Throwable {
		previousResultText = currentResultText;
		previousResultTextId = currentResultTextId;

		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(resultTextQueryForCurrentActionsQuery),
						defectSubCategoryId, previousResultTextId));
		DatabaseConn.resultSet.next();
		currentResultText = DatabaseConn.resultSet.getString("Name");
		currentResultTextId = DatabaseConn.resultSet.getString("ActionID");

	}

	@When("^Call Post Method for two current Actions and Verify Status code \"([^\"]*)\"$")
	public void call_Post_Method_for_two_current_Actions_and_Verify_Status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", currentResultText);
		replaceMap.put("~~~~~", previousResultText);

		pdsString = CommonMethods.getResourceFromFile(
				"src/test/resources/TestData/JsonFiles/DefectCategoryWithTwoActions.json", replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + defectCatPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Record for two current Actions should be inserted in ProcessLogs table \"([^\"]*)\"$")
	public void Verify_Record_for_two_current_Actions_should_be_inserted_in_ProcessLogs_table(
			String processLogsDetailsQuery) throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(processLogsDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextAction").trim().equals(currentResultTextId.trim())
						|| DatabaseConn.resultSet.getString("NextAction").trim().equals(previousResultTextId.trim())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 2) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}

	@Then("^Verify Last Action Record should be inserted in WorkFlowStatus table \"([^\"]*)\"$")
	public void verify_Last_Action_Record_should_be_inserted_in_WorkFlowStatus_table(String workFlowStatusDetailsQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(workFlowStatusDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextWFTActionID").trim().equals(currentResultTextId.trim())
						|| DatabaseConn.resultSet.getString("NextWFTActionID").trim()
								.equals(previousResultTextId.trim())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 1) {
			Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}

	@Then("^Verify Record should be inserted in CrossSiteRegistry table \"([^\"]*)\"$")
	public void verify_Record_should_be_inserted_in_CrossSiteRegistry_table(String crossSiteRegistryQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(crossSiteRegistryQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("facilitycode").trim()
						.equalsIgnoreCase(BaseSetup.returnPropertyValue("Facility").trim())
						&& DatabaseConn.resultSet.getString("createddatetime").trim()
								.contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in CrossSiteRegistry table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount < 1) {
			Assert.assertTrue("Records has not been inserted in CrossSiteRegistry table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}

	@Then("^Verify Record should be inserted in CrossSiteRegistryDetail table \"([^\"]*)\"$")
	public void verify_Record_should_be_inserted_in_CrossSiteRegistryDetail_table(String crossSiteRegistryDetailQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(crossSiteRegistryDetailQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("facilitycode").trim()
						.equalsIgnoreCase(BaseSetup.returnPropertyValue("Facility").trim())
						&& DatabaseConn.resultSet.getString("updateddatetime").trim()
								.contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in CrossSiteRegistryDetail table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount < 1) {
			Assert.assertTrue("Records has not been inserted in CrossSiteRegistryDetail table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}

	@When("^Call Post Method without sub category and Verify Status Code \"([^\"]*)\"$")
	public void call_Post_Method_without_sub_category_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", "");
		replaceMap.put("*****", currentResultText);

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + defectCatPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Error Message for Sub Category \"([^\"]*)\"$")
	public void verify_Error_Message_for_Sub_Category(String errorMessageForSubCategory) throws Throwable {
		String resMessage = "";

		try {
			resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
		} catch (Exception e) {
			try {
				resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
			} catch (Exception ex) {
			}
		}

		Assert.assertTrue("Expected Message is :- " + errorMessageForSubCategory + "But Found :- " + resMessage,
				errorMessageForSubCategory.equalsIgnoreCase(resMessage));
	}

	@When("^Call Post Method without Account Number and Verify Status Code \"([^\"]*)\"$")
	public void call_Post_Method_without_Account_Number_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", currentResultText);
		replaceMap.put("^^^^^", "NULL");

		pdsString = CommonMethods
				.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategoryValidation.json", replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue("Expected Code is:- " + statusInt + " but found:- " + defectCatPostResponce.getStatusCode()
				+ " And showing message:- " + resMessage, defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Status Code from Responce Body \"([^\"]*)\"$")
	public void verify_Status_Code_from_Responce_Body(String statusCode) throws Throwable {
		Assert.assertTrue(
				"In Responce Body Expected code is:- " + statusCode + " But Found :- "
						+ defectCatPostResponce.getBody().jsonPath().get("statusCode"),
				defectCatPostResponce.getBody().jsonPath().get("statusCode").toString().trim()
						.equals(statusCode.trim()));
	}

	@Then("^Verify Error Message for Account Number \"([^\"]*)\"$")
	public void verify_Error_Message_for_Account_Number(String errorMessageForAccountNumber) throws Throwable {
		String resMessage = "";

		try {
			resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
		} catch (Exception e) {
			try {
				resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
			} catch (Exception ex) {
			}
		}

		Assert.assertTrue("Expected Message is :- " + errorMessageForAccountNumber + "But Found :- " + resMessage,
				errorMessageForAccountNumber.equalsIgnoreCase(resMessage));
	}

	@Given("^Find Not Required Action From DB \"([^\"]*)\"$")
	public void find_Not_Required_Action_From_DB(String resultTextQueryForNotRequired) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(resultTextQueryForNotRequired), defectSubCategoryId));
		DatabaseConn.resultSet.next();
		notRequiredResultText = DatabaseConn.resultSet.getString("Name");
		notRequiredResultTextId = DatabaseConn.resultSet.getString("ActionID");

	}

	@Given("^Find Required Action From DB \"([^\"]*)\"$")
	public void find_Required_Action_From_DB(String resultTextQueryForCurrent) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				String.format(BaseSetup.returnQueryPropertyValue(resultTextQueryForCurrent), defectSubCategoryId));
		DatabaseConn.resultSet.next();
		requiredResultText = DatabaseConn.resultSet.getString("Name");
		requiredResultTextId = DatabaseConn.resultSet.getString("ActionID");

	}

	@When("^Call Post Method with blank Action and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_with_blank_Action_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", "");

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue(
				"For Blank Action Expected Code is:- " + statusInt + " but found:- "
						+ defectCatPostResponce.getStatusCode() + " And showing message:- " + resMessage,
				defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Error Message for blank Action \"([^\"]*)\"$")
	public void verify_Error_Message_for_blank_Action(String errorMessageForBlankAction) throws Throwable {
		String resMessage = "";

		try {
			resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
		} catch (Exception e) {
			try {
				resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
			} catch (Exception ex) {
			}
		}

		Assert.assertTrue(
				"For Blank Action Expected Message is :- " + errorMessageForBlankAction + "But Found :- " + resMessage,
				resMessage.contains(errorMessageForBlankAction));
	}

	@When("^Call Post Method with Not Required Action and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_with_Not_Required_Action_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", notRequiredResultText);

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue(
				"For Not Required Action Expected Code is:- " + statusInt + " but found:- "
						+ defectCatPostResponce.getStatusCode() + " And showing message:- " + resMessage,
				defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Error Message for Not Required Action \"([^\"]*)\"$")
	public void verify_Error_Message_for_Not_Required_Action(String errorMessageForNotRequiredAction) throws Throwable {
		String resMessage = "";

		try {
			resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
		} catch (Exception e) {
			try {
				resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
			} catch (Exception ex) {
			}
		}

		Assert.assertTrue("For Not Required Action Expected Message is :- " + errorMessageForNotRequiredAction
				+ " But Found :- " + resMessage, resMessage.equalsIgnoreCase(errorMessageForNotRequiredAction));
	}

	@When("^Call Post Method with Required Action and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_with_Required_Action_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";

		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));

		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();

		mapParam.put("account", visitNumber);

		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));

		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", requiredResultText);

		pdsString = CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json",
				replaceMap);

		for (int i = 0; i < count; i++) {
			defectCatPostResponce = BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);

			if (defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			} else {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				} catch (Exception e) {
					try {
						resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
					} catch (Exception ex) {
					}
				}
				Thread.sleep(countWait);
			}
		}

		Assert.assertTrue(
				"For Required Action Expected Code is:- " + statusInt + " but found:- "
						+ defectCatPostResponce.getStatusCode() + " And showing message:- " + resMessage,
				defectCatPostResponce.getStatusCode() == statusInt);

	}

	@Then("^Verify Action should be inserted in WorkFlowStatus table \"([^\"]*)\"$")
	public void verify_Action_should_be_inserted_in_WorkFlowStatus_table(String workFlowStatusDetailsQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(workFlowStatusDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextWFTActionID").trim().equals(requiredResultTextId.trim())
						&& DatabaseConn.resultSet.getString("CreatedDate").contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount < 1) {
			Assert.assertTrue("Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}
	
	@Then("^Verify A Record should be inserted in ProcessLogs table \"([^\"]*)\"$")
	public void Verify_A_Record_should_be_inserted_in_ProcessLogs_table(String processLogsDetailsQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(processLogsDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextAction").trim().equals(resultTextId.trim())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue(resultTextId+" Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount < 1) {
			Assert.assertTrue(resultTextId+" Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}
	
	@Then("^Verify A Action should be inserted in WorkFlowStatus table \"([^\"]*)\"$")
	public void verify_A_Action_should_be_inserted_in_WorkFlowStatus_table(String workFlowStatusDetailsQuery)
			throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(workFlowStatusDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("NextWFTActionID").trim().equals(resultTextId.trim())
						&& DatabaseConn.resultSet.getString("CreatedDate").contains(CommonMethods.getCurrentDate())) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue(resultTextId+" Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount < 1) {
			Assert.assertTrue(resultTextId+" Records has not been inserted in WorkFlowStatus table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}
	}
	
	@When("^Find Actions from DB \"([^\"]*)\" and \"([^\"]*)\"$")
	public void find_Actions_from_DB_and(String actionsQuery, String statusCode) throws Throwable {
			DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
					BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
					String.format(BaseSetup.returnQueryPropertyValue(actionsQuery), defectSubCategoryId));
			DatabaseConn.resultSet.next();
			currentResultText = DatabaseConn.resultSet.getString("Name");
			currentResultTextId = DatabaseConn.resultSet.getString("ActionID");
	}
	
	@When("^Get Actions From DB \"([^\"]*)\"$")
	public void Get_Actions_From_DB(String actionsQuery) throws Throwable {
			DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
					BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
					String.format(BaseSetup.returnQueryPropertyValue(actionsQuery), defectSubCategoryId));
			DatabaseConn.resultSet.next();
			currentResultText = DatabaseConn.resultSet.getString("Name");
			currentResultTextId = DatabaseConn.resultSet.getString("ActionID");
	}

	@Then("^Call Post Method and Verify Status Code \"([^\"]*)\" and \"([^\"]*)\"$")
	public void call_Post_Method_and_Verify_Status_Code_and(String statusCode, String actionsText) throws Throwable {
	    if(statusCode.equals("400")){
	    	currentResultText=actionsText;
	    }
	    
	    String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		mapParam.put("account", visitNumber);
		
		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", currentResultText);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json", replaceMap);
		
		for(int i=0;i<count;i++) {
			defectCatPostResponce=BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);
			
			if(defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=defectCatPostResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For "+currentResultText+" Actions Expected Code is:- "+statusInt + " but found:- "+defectCatPostResponce.getStatusCode()+" And showing message:- "+resMessage, defectCatPostResponce.getStatusCode() == statusInt);
		
	}
	
	@Then("^Call Post Method and Verify Status Code for Defect Sub Category \"([^\"]*)\" and \"([^\"]*)\"$")
	public void Call_Post_Method_and_Verify_Status_Code_for_Defect_Sub_Category_and(String statusCode, String subCategoryText) throws Throwable {
	    if(statusCode.equals("400")){
	    	defectSubCategory=subCategoryText;
	    }
	    
	    String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		mapParam.put("account", visitNumber);
		
		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", currentResultText);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json", replaceMap);
		
		for(int i=0;i<count;i++) {
			defectCatPostResponce=BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);
			
			if(defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=defectCatPostResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For "+currentResultText+" Actions Expected Code is:- "+statusInt + " but found:- "+defectCatPostResponce.getStatusCode()+" And showing message:- "+resMessage, defectCatPostResponce.getStatusCode() == statusInt);
		
	}
	
	@Then("^Verify Error Message \"([^\"]*)\" and \"([^\"]*)\"$")
	public void Verify_Error_Message_and(String statusCode, String errorMessage) throws Throwable {
		if(statusCode.equals("400")) {
			String resMessage = "";
	
			try {
				resMessage = defectCatPostResponce.getBody().jsonPath().get("Message").toString();
			} catch (Exception e) {
				try {
					resMessage = defectCatPostResponce.getBody().jsonPath().get("message").toString();
				} catch (Exception ex) {
				}
			}
	
			Assert.assertTrue(
					"Expected Message should contains :- " + errorMessage + "But Found :- " + resMessage,
					resMessage.contains(errorMessage));
		}
	}
	
	@Then("^Verify Notes should be inserted in ProcessLogs table \"([^\"]*)\"$")
	public void verify_Notes_should_be_inserted_in_ProcessLogs_table(String processLogsDetailsQuery) throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(processLogsDetailsQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if (DatabaseConn.resultSet.getString("Note").trim().equals(BaseSetup.returnPropertyValue("NoteText").trim())
						&& (DatabaseConn.resultSet.getString("NextAction").trim().equals(futureResultTextId.trim()) || DatabaseConn.resultSet.getString("NextAction").trim().equals(currentResultTextId.trim()))) {
					recordCount = recordCount + 1;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("Records has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 2) {
			Assert.assertTrue("Expected Notes :- "+BaseSetup.returnPropertyValue("NoteText").trim()+" has not been inserted in ProcessLogs table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}
	
	@Then("^Call Post Method and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_and_Verify_Status_Code(String statusCode) throws Throwable {
	    String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("DefectCategoryActionUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapParam = new HashMap<String, String>();
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		mapParam.put("account", visitNumber);
		
		mapHeaders.put("Content-Type", "application/json");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("NoteText"));
		replaceMap.put("$$$$$", defectCategory);
		replaceMap.put("%%%%%", defectSubCategory);
		replaceMap.put("*****", currentResultText);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/DefectCategory.json", replaceMap);
		
		for(int i=0;i<count;i++) {
			defectCatPostResponce=BaseSetup.postMethodRequestWithParam(apiUri, mapHeaders, pdsString, mapParam);
			
			if(defectCatPostResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=defectCatPostResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=defectCatPostResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For "+defectSubCategory+" SubCategory Expected Code is:- "+statusInt + " but found:- "+defectCatPostResponce.getStatusCode()+" And showing message:- "+resMessage, defectCatPostResponce.getStatusCode() == statusInt);
		
	}
	
	@Given("^Find inactive DefectSubCateogory from DB \"([^\"]*)\"$")
	public void find_inactive_DefectSubCateogory_from_DB(String defectSubCategoryQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(defectSubCategoryQuery));
		DatabaseConn.resultSet.next();
		defectSubCategory = DatabaseConn.resultSet.getString("DefectSubCategoryDesc");
		defectSubCategoryId = DatabaseConn.resultSet.getString("DefectSubCategoryID");
	}



}
