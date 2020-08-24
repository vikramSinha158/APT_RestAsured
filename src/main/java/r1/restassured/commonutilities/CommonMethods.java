package r1.restassured.commonutilities;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
public class CommonMethods {
	
	public static String getCurrentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
		 LocalDateTime now = LocalDateTime.now();  
		 return dtf.format(now).toString(); 
	}
	
	public static String getRandomString(int length) {
		return RandomStringUtils.randomAlphabetic(length);
	}
	
	public static JSONObject convertSubStringToJson(String respnceStr, String splitFirstStr, String splitLastStr) throws JSONException {
		respnceStr=respnceStr.substring(respnceStr.indexOf(splitFirstStr), respnceStr.indexOf(splitLastStr));
		JSONObject jsonObject = new JSONObject(respnceStr);
		return jsonObject;
	}
	
	
	public static String generateResourceFromFiles(String path) throws Throwable {
		try {
			return new String(Files.readAllBytes(Paths.get(path)));
		} catch (Throwable t) {
			throw t;
		}
	}
	
	public static String getResourceFromFile(String path, Map<String, String> replaceMap) throws Throwable {
		String pdsString=CommonMethods.generateResourceFromFiles(path);
		
		if(replaceMap.size()>0) {
			for (Map.Entry<String,String> entry : replaceMap.entrySet()) {
				pdsString=pdsString.replace(entry.getKey(), entry.getValue());
			}
		}
		
		return pdsString;
		
	}
	
	
	

}
