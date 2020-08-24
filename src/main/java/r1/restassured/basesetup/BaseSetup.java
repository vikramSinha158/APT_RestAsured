package r1.restassured.basesetup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.omg.CORBA.Request;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class BaseSetup {

	public static String returnPropertyValue(String strVariable) {
		String expValue = null;
		try {
			FileReader reader = new FileReader(System.getProperty("user.dir") + "//restAssured.properties");
			Properties priperty = new Properties();
			priperty.load(reader);
			expValue = priperty.getProperty(strVariable);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return expValue;

	}
	
	public static void storeValueInPropertyFile(String keyString, String keyValue) throws IOException, ConfigurationException {
		
		PropertiesConfiguration config = new PropertiesConfiguration(System.getProperty("user.dir") + "//restAssured.properties");
		config.setProperty(keyString, keyValue);
		config.save();
	}
	
	public static String returnQueryPropertyValue(String strVariable) {
		String expValue = null;
		try {
			FileReader reader = new FileReader("src/test/resources/TestData/Query.properties");
			Properties priperty = new Properties();
			priperty.load(reader);
			expValue = priperty.getProperty(strVariable);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return expValue;

	}

	public static ResponseOptions<Response> postMethodRequest(String uri, Map<String, String> headers, String pds) {
		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri(uri);
		rsb.setContentType(ContentType.JSON);
		rsb.addHeaders(headers);
		RequestSpecification requestSpec = rsb.build().relaxedHTTPSValidation();
		RequestSpecification request = RestAssured.given().spec(requestSpec);
		request.body(pds);
		ResponseOptions<Response> res = (ResponseOptions<Response>) request.post().body();
		return res;
	}
	
	public static ResponseOptions<Response> postMethodRequestWithParam(String uri, Map<String, String> headers, String pds, Map<String, String> params) {
		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri(uri);
		rsb.setContentType(ContentType.JSON);
		rsb.addQueryParams(params);
		rsb.addHeaders(headers);
		RequestSpecification requestSpec = rsb.build().relaxedHTTPSValidation();
		RequestSpecification request = RestAssured.given().spec(requestSpec);
		request.body(pds);
		ResponseOptions<Response> res = (ResponseOptions<Response>) request.post().body();
		return res;
	}

	public static ResponseOptions<Response> getMethodRequest(String uri, Map<String, String> headers) {
		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri(uri);
		rsb.setContentType(ContentType.JSON);
		rsb.addHeaders(headers);
		RequestSpecification requestSpec = rsb.build().relaxedHTTPSValidation();
		RequestSpecification request = RestAssured.given().spec(requestSpec);
		ResponseOptions<Response> res = (ResponseOptions<Response>) request.get().body();
		return res;
	}
	
	
	public static ResponseOptions<Response> getMethodRequestWithParam(String uri, Map<String, String> headers, Map<String, String> params) {
		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri(uri);
		rsb.setContentType(ContentType.JSON);
		rsb.addQueryParams(params);
		rsb.addHeaders(headers);
		RequestSpecification requestSpec = rsb.build().relaxedHTTPSValidation();
		RequestSpecification request = RestAssured.given().spec(requestSpec);
		ResponseOptions<Response> res = (ResponseOptions<Response>) request.get().body();
		return res;
	}
	
	public static ResponseOptions<Response> postMethodRequestForUploadDocument(String uri, Map<String, String> headers, String pds, String fileName) {
		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri(uri);
		rsb.setContentType(ContentType.JSON);
		rsb.addHeaders(headers);
		rsb.addMultiPart("file", new File(fileName));
		rsb.addMultiPart("requestJson", pds);
		RequestSpecification requestSpec = rsb.build().relaxedHTTPSValidation();
		RequestSpecification request = RestAssured.given().spec(requestSpec);
		ResponseOptions<Response> res = (ResponseOptions<Response>) request.post().body();
		return res;
	}
	
	public static ResponseOptions<Response> postMethodRequestForUploadDocument(String uri, Map<String, String> headers, String pds, String firstFileName, String secoundFileName) {
		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri(uri);
		rsb.setContentType(ContentType.JSON);
		rsb.addHeaders(headers);
		rsb.addMultiPart("file", new File(firstFileName));
		rsb.addMultiPart("file", new File(secoundFileName));
		rsb.addMultiPart("requestJson", pds);
		RequestSpecification requestSpec = rsb.build().relaxedHTTPSValidation();
		RequestSpecification request = RestAssured.given().spec(requestSpec);
		ResponseOptions<Response> res = (ResponseOptions<Response>) request.post().body();
		return res;
	}

	
}
