package r1.restassured.commonutilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import r1.restassured.basesetup.BaseSetup;

public class QueryExecutor {
	
	
	public static void runQueryTran(String queryName) throws ClassNotFoundException, FileNotFoundException, SQLException, IOException {
		DatabaseConn.getServerDBName(BaseSetup.returnPropertyValue("Environment"), BaseSetup.returnPropertyValue("Facility"));
		DatabaseConn.serverConn(DatabaseConn.serverName,DatabaseConn.databaseName, BaseSetup.returnQueryPropertyValue(queryName));
	}
	
	public static void runQueryTran(String queryName, String facility) throws ClassNotFoundException, FileNotFoundException, SQLException, IOException {
		DatabaseConn.getServerDBName(BaseSetup.returnPropertyValue("Environment"), facility);
		DatabaseConn.serverConn(DatabaseConn.serverName,DatabaseConn.databaseName, BaseSetup.returnQueryPropertyValue(queryName));
	}
	
	public static void runQueryTranParam(String queryName, String parameter) throws ClassNotFoundException, FileNotFoundException, SQLException, IOException {
		DatabaseConn.getServerDBName(BaseSetup.returnPropertyValue("Environment"), BaseSetup.returnPropertyValue("Facility"));
		DatabaseConn.serverConn(DatabaseConn.serverName,DatabaseConn.databaseName, String.format(BaseSetup.returnQueryPropertyValue(queryName), parameter));
	}
	
	public static void runQueryTranParam(String queryName, String firstParameter, String secoundParameter) throws ClassNotFoundException, FileNotFoundException, SQLException, IOException {
		DatabaseConn.getServerDBName(BaseSetup.returnPropertyValue("Environment"), BaseSetup.returnPropertyValue("Facility"));
		DatabaseConn.serverConn(DatabaseConn.serverName,DatabaseConn.databaseName, String.format(BaseSetup.returnQueryPropertyValue(queryName), firstParameter, secoundParameter));
	}

}
