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

public class UploadDocumentInR1DSteps extends BaseSetup{
	public ResponseOptions<Response> postResponce;
	public String requestorCode;
	public String requestorId;
	public String visitNumber;
	public String codeId;
	public String decodeText;
	String registrationId;
	
	
	@Given("^Find User ID and User Name for Upload Documents \"([^\"]*)\"$")
	public void find_User_ID_and_User_Name_for_Upload_Documents(String requestorCodeQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("DNNDataBaseName"),
				BaseSetup.returnQueryPropertyValue(requestorCodeQuery));
		DatabaseConn.resultSet.next();
		requestorCode = DatabaseConn.resultSet.getString("username");
		requestorId = DatabaseConn.resultSet.getString("userid");
	}
	
	@Given("^Find Visit Num from DB for Upload Documents \"([^\"]*)\"$")
	public void find_Visit_Num_from_DB_for_Upload_Documents(String visitNumberQuery) throws Throwable {
		QueryExecutor.runQueryTran(visitNumberQuery);
		DatabaseConn.resultSet.next();
		visitNumber = DatabaseConn.resultSet.getString("encounterid");
	}
	
	@Given("^Find Registration Id from DB for Upload Documents \"([^\"]*)\"$")
	public void find_Registration_Id_from_DB_for_Upload_Documents(String registrationCodeQuery) throws Throwable {
		QueryExecutor.runQueryTranParam(registrationCodeQuery, visitNumber);
		DatabaseConn.resultSet.next();
		registrationId = DatabaseConn.resultSet.getString("id");
	}
	
	@When("^Call Post Method With Excel File for Upload Documents and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_Excel_File_for_Upload_Documents_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(Excel), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}

	@Then("^Verify Responce body for Upload Documents$")
	public void verify_Responce_body_for_Upload_Documents() throws Throwable {
	    Assert.assertTrue("For Upload Documents, Expected Document Number should not be blank or null but found:- "+postResponce.getBody().jsonPath().get("documentNumber").toString(), !postResponce.getBody().jsonPath().get("documentNumber").toString().equals("") || !postResponce.getBody().jsonPath().get("documentNumber").toString().equals(null));
	    Assert.assertTrue("For Upload Documents, Expected document type is:- "+decodeText+" But found:- "+postResponce.getBody().jsonPath().get("type").toString(), postResponce.getBody().jsonPath().get("type").toString().trim().equalsIgnoreCase(decodeText.trim()));
	    Assert.assertTrue("For Upload Documents, Expected Document title is:- "+BaseSetup.returnPropertyValue("UploadDocumentsTitle")+" But found:- "+postResponce.getBody().jsonPath().get("title").toString(), postResponce.getBody().jsonPath().get("title").toString().trim().equalsIgnoreCase(BaseSetup.returnPropertyValue("UploadDocumentsTitle").trim()));
	}
	
	@Then("^Verify Record should be inserted into the document table in tran database \"([^\"]*)\" and \"([^\"]*)\"$")
	public void Verify_Record_should_be_inserted_into_the_document_table_in_tran_database_and(String documentQuery, String docFormat) throws Throwable {
		int recordCount = 0;
		QueryExecutor.runQueryTranParam(documentQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if ((!DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("null") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals(null))
						&& (DatabaseConn.resultSet.getString("CreatedDateTime").contains(CommonMethods.getCurrentDate()) || DatabaseConn.resultSet.getString("UpdatedDateTime").contains(CommonMethods.getCurrentDate()))
						&& (DatabaseConn.resultSet.getString("DocumentTitle").trim().equalsIgnoreCase(BaseSetup.returnPropertyValue("UploadDocumentsTitle")))
						&& (DatabaseConn.resultSet.getString("DocumentName").trim().contains(docFormat.trim()))
						&& (DatabaseConn.resultSet.getString("DocumentFormat").trim().contains(docFormat.trim()))
						&& (!DatabaseConn.resultSet.getString("DocImage").trim().equals("") || !DatabaseConn.resultSet.getString("DocImage").trim().equals("null") || !DatabaseConn.resultSet.getString("DocImage").trim().equals(null))
						&& DatabaseConn.resultSet.getString("IsAssociatedToMRN").trim().equals("1")) {
					recordCount = recordCount + 1;
					break;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("For Upload Documents, Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 1) {
			Assert.assertTrue("For Upload Documents, Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}
	
	@When("^Call Post Method With Pdf File for Upload Documents and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_Pdf_File_for_Upload_Documents_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validPdf.pdf";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(Pdf), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@When("^Call Post Method With docx File for Upload Documents and verify status code \"([^\"]*)\"$")
	public void call_Post_Method_With_docx_File_for_Upload_Documents_and_verify_status_code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validDoc.docx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(docx), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@When("^Call Post Method With File for Upload Documents and verify status code \"([^\"]*)\" and \"([^\"]*)\"$")
	public void Call_Post_Method_With_File_for_Upload_Documents_and_verify_status_code_and(String statusCode, String fileName) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/"+fileName;
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents("+fileName+"), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@Then("^Verify Record should be inserted in to the document table in tran database \"([^\"]*)\" and \"([^\"]*)\"$")
	public void Verify_Record_should_be_inserted_in_to_the_document_table_in_tran_database_and(String documentQuery, String fileName) throws Throwable {
		int recordCount = 0;
		
		String[] exten=fileName.split("\\.");
		QueryExecutor.runQueryTranParam(documentQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if ((!DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("null") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals(null))
						&& (DatabaseConn.resultSet.getString("CreatedDateTime").contains(CommonMethods.getCurrentDate()) || DatabaseConn.resultSet.getString("UpdatedDateTime").contains(CommonMethods.getCurrentDate()))
						&& (DatabaseConn.resultSet.getString("DocumentTitle").trim().equalsIgnoreCase(BaseSetup.returnPropertyValue("UploadDocumentsTitle")))
						&& (DatabaseConn.resultSet.getString("DocumentName").trim().contains(fileName.trim()))
						&& (DatabaseConn.resultSet.getString("DocumentFormat").trim().contains(exten[1].trim()))
						&& (!DatabaseConn.resultSet.getString("DocImage").trim().equals("") || !DatabaseConn.resultSet.getString("DocImage").trim().equals("null") || !DatabaseConn.resultSet.getString("DocImage").trim().equals(null))
						&& DatabaseConn.resultSet.getString("IsAssociatedToMRN").trim().equals("1")) {
					recordCount = recordCount + 1;
					break;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("For Upload Documents("+fileName+"), Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 1) {
			Assert.assertTrue("For Upload Documents("+fileName+"), Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}

	@Then("^Verify status code from responce body \"([^\"]*)\"$")
	public void Verify_status_code_from_responce_body(String statusCode) throws Throwable {
		Assert.assertTrue(
				"In Responce Body Expected code is:- " + statusCode + " But Found :- "
						+ postResponce.getBody().jsonPath().get("statusCode"),
						postResponce.getBody().jsonPath().get("statusCode").toString().trim()
						.equals(statusCode.trim()));
	}
	
	@Then("^Verify Error Message from responce body \"([^\"]*)\"$")
	public void Verify_Error_Message_from_responce_body(String errorMessage) throws Throwable {
		String resMessage = "";

		try {
			resMessage = postResponce.getBody().jsonPath().get("Message").toString();
		} catch (Exception e) {
			try {
				resMessage = postResponce.getBody().jsonPath().get("message").toString();
			} catch (Exception ex) {
			}
		}

		Assert.assertTrue("Expected Message is :- " + errorMessage + "But Found :- " + resMessage,
				resMessage.contains(errorMessage));
	}
	
	@When("^Call Post Method Without Type For Upload Documents and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_Without_Type_For_Upload_Documents_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", "");
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(Without Upload Document Type), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@When("^Call Post Method Without Title For Upload Documents and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_Without_Title_For_Upload_Documents_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", "");
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(Without Upload Document Title), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@When("^Call Post Method Without Visit Number For Upload Documents and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_Without_Visit_Number_For_Upload_Documents_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", "");
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(Without Visit Number), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@When("^Call Post Method With Invalid Type For Upload Documents and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_With_Invalid_Type_For_Upload_Documents_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", BaseSetup.returnPropertyValue("UploadDocumentsInvalidTypes"));
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(With Invalid Type), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	
	@When("^Call Post Method With Invalid Visit Number For Upload Documents and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_With_Invalid_Visit_Number_For_Upload_Documents_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", BaseSetup.returnPropertyValue("UploadDocumentsInvalidVisitNumber"));
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(With Invalid Visit Number), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	

	@When("^Call Post Method With Invalid Performer For Upload Documents and Verify Status Code \"([^\"]*)\"$")
	public void Call_Post_Method_With_Invalid_Performer_For_Upload_Documents_and_Verify_Status_Code(String statusCode) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", BaseSetup.returnPropertyValue("UploadDocumentsInvalidPerformer"));
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/validExcel.xlsx";
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(With Invalid Performer), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@When("^Call Post Method With Two Files for Upload Documents and verify status code \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
	public void call_Post_Method_With_Two_Files_for_Upload_Documents_and_verify_status_code_and_and(String statusCode, String firstFileName, String secoundFileName) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocument.json", replaceMap);
		String fFile="src/test/resources/TestData/TestFiles/"+firstFileName;
		String sFile="src/test/resources/TestData/TestFiles/"+secoundFileName;
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, fFile, sFile);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents(Multiple Files), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@Then("^Verify Record should not inserted in to the document table in tran database \"([^\"]*)\" and \"([^\"]*)\"$")
	public void verify_Record_should_not_inserted_in_to_the_document_table_in_tran_database_and(String documentQuery, String fileName) throws Throwable {
		int recordCount = 0;
		
		String[] exten=fileName.split("\\.");
		QueryExecutor.runQueryTranParam(documentQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if ((!DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("null") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals(null))
						&& (DatabaseConn.resultSet.getString("CreatedDateTime").contains(CommonMethods.getCurrentDate()) || DatabaseConn.resultSet.getString("UpdatedDateTime").contains(CommonMethods.getCurrentDate()))
						&& (DatabaseConn.resultSet.getString("DocumentTitle").trim().equalsIgnoreCase(BaseSetup.returnPropertyValue("UploadDocumentsTitle")))
						&& (DatabaseConn.resultSet.getString("DocumentName").trim().contains(fileName.trim()))
						&& (DatabaseConn.resultSet.getString("DocumentFormat").trim().contains(exten[1].trim()))
						&& (!DatabaseConn.resultSet.getString("DocImage").trim().equals("") || !DatabaseConn.resultSet.getString("DocImage").trim().equals("null") || !DatabaseConn.resultSet.getString("DocImage").trim().equals(null))
						&& DatabaseConn.resultSet.getString("IsAssociatedToMRN").trim().equals("1")) {
					recordCount = recordCount + 1;
					break;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("For Upload Documents(Multiple Files), Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 0) {
			Assert.assertTrue("For Upload Documents(Multiple Files), "+fileName+" Records has been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}
	
	@Given("^Find Document Type from DB for Upload Documents \"([^\"]*)\"$")
	public void find_Document_Type_from_DB_for_Upload_Documents(String getDocumentTypeQuery) throws Throwable {
		DatabaseConn.serverConn(BaseSetup.returnPropertyValue("DNNServerName"),
				BaseSetup.returnPropertyValue("AccretiveDataBaseName"),
				BaseSetup.returnQueryPropertyValue(getDocumentTypeQuery));
		DatabaseConn.resultSet.next();
		codeId = DatabaseConn.resultSet.getString("CodeId");
		decodeText = DatabaseConn.resultSet.getString("DecodeText");
	}
	
	@When("^Call Post Method With File and MRN for Upload Documents and verify status code \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
	public void call_Post_Method_With_File_and_MRN_for_Upload_Documents_and_verify_status_code_and_and(String statusCode, String fileName, String mrnValue) throws Throwable {
		String pdsString;
		String apiUri = BaseSetup.returnPropertyValue("UploadDocumentsPostUri");
		String mrnValueForUpload=BaseSetup.returnPropertyValue(mrnValue);
		String resMessage = "";
		
		int statusInt = Integer.parseInt(statusCode);
		int countWait = Integer.parseInt(BaseSetup.returnPropertyValue("MaxWaitCount"));
		int count = Integer.parseInt(BaseSetup.returnPropertyValue("MaxRunCount"));
		
		Map<String, String> mapHeaders = new HashMap<String, String>();
		Map<String, String> replaceMap = new HashMap<String, String>();
		
		
		mapHeaders.put("Content-Type", "multipart/form-data");
		mapHeaders.put("facilityCode", BaseSetup.returnPropertyValue("Facility"));
		mapHeaders.put("Authorization", "Bearer " + BaseSetup.returnPropertyValue("AccessTocken"));
		
		replaceMap.put("!!!!!", visitNumber);
		replaceMap.put("@@@@@", requestorCode);
		replaceMap.put("#####", BaseSetup.returnPropertyValue("UploadDocumentsNoteText"));
		replaceMap.put("%%%%%", decodeText);
		replaceMap.put("&&&&&", BaseSetup.returnPropertyValue("UploadDocumentsTitle"));
		replaceMap.put("$$$$$", requestorCode);
		replaceMap.put("^^^^^", mrnValueForUpload);
		
		pdsString=CommonMethods.getResourceFromFile("src/test/resources/TestData/JsonFiles/UploadDocumentMRN.json", replaceMap);
		String file="src/test/resources/TestData/TestFiles/"+fileName;
		
		for(int i=0;i<count;i++) {
			postResponce=BaseSetup.postMethodRequestForUploadDocument(apiUri, mapHeaders, pdsString, file);
			
			if(postResponce.getStatusCode() == statusInt) {
				break;
			}else {
				try {
					resMessage=postResponce.getBody().jsonPath().get("Message").toString();
				}catch(Exception e) {
					try {
						resMessage=postResponce.getBody().jsonPath().get("message").toString();
					}catch(Exception ex) {}
				}
				Thread.sleep(countWait);
			}
		}
		
		Assert.assertTrue("For Upload Documents("+fileName+"), Expected Code is:- "+statusInt + " but found:- "+postResponce.getStatusCode()+" And showing message:- "+resMessage, postResponce.getStatusCode() == statusInt);
		
	}
	
	@Then("^Verify MRN should be inserted in to the document table in tran database \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
	public void verify_MRN_should_be_inserted_in_to_the_document_table_in_tran_database_and_and(String documentQuery, String fileName, String mrnValue) throws Throwable {
		int recordCount = 0;
		
		String mrnValueUpload;
		
		if(BaseSetup.returnPropertyValue(mrnValue).equals("") || BaseSetup.returnPropertyValue(mrnValue).equalsIgnoreCase("null") || BaseSetup.returnPropertyValue(mrnValue).equals(null)) {
			mrnValueUpload="0";
		}else {
			mrnValueUpload="1";
		}
		
		String[] exten=fileName.split("\\.");
		QueryExecutor.runQueryTranParam(documentQuery, registrationId, requestorId);

		try {
			while (DatabaseConn.resultSet.next()) {
				if ((!DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals("null") || !DatabaseConn.resultSet.getString("DocumentTypeId").trim().equals(null))
						&& (DatabaseConn.resultSet.getString("CreatedDateTime").contains(CommonMethods.getCurrentDate()) || DatabaseConn.resultSet.getString("UpdatedDateTime").contains(CommonMethods.getCurrentDate()))
						&& (DatabaseConn.resultSet.getString("DocumentTitle").trim().equalsIgnoreCase(BaseSetup.returnPropertyValue("UploadDocumentsTitle")))
						&& (DatabaseConn.resultSet.getString("DocumentName").trim().contains(fileName.trim()))
						&& (DatabaseConn.resultSet.getString("DocumentFormat").trim().contains(exten[1].trim()))
						&& (!DatabaseConn.resultSet.getString("DocImage").trim().equals("") || !DatabaseConn.resultSet.getString("DocImage").trim().equals("null") || !DatabaseConn.resultSet.getString("DocImage").trim().equals(null))
						&& DatabaseConn.resultSet.getString("IsAssociatedToMRN").trim().equals(mrnValueUpload)) {
					recordCount = recordCount + 1;
					break;
				}
			}
		} catch (Exception e) {
			Assert.assertTrue("For Upload Documents("+fileName+"), Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

		if (recordCount != 1) {
			Assert.assertTrue("For Upload Documents("+fileName+"), Records has not been inserted in document table for " + registrationId
					+ " Registration Id and " + visitNumber + " Visit Number", false);
		}

	}

	



	

}
