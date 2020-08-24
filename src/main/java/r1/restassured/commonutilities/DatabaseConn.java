package r1.restassured.commonutilities;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConn {

	public static ResultSet resultSet;
	public static String serverName;
	public static String databaseName;

	public static void getServerDBName(String env, String facility) {

		try {
			String query = "SELECT Replace(Replace(serverpath, '[', ''), ']', '')  AS servername,databasename from locations where code='"
					+ facility + "'";

			/********************** IQA ***************************/

			if (env.contains("IQA")) {
				serverConn("ALPiQAR1ATRN01.accretivehealth.local", "Accretive", query);
				while (resultSet.next()) {
					serverName = resultSet.getString("servername");
					databaseName = resultSet.getString("databasename");
				}
			}
			/********************** UAT ***************************/
			if (env.contains("UAT")) {
				serverConn("UATRHUBWTRN20", "Accretive", query);
				while (resultSet.next()) {
					serverName = resultSet.getString("servername");
					databaseName = resultSet.getString("databasename");
				}
			}

			
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void serverConn(String serverHost, String dbName, String query)
			throws ClassNotFoundException, SQLException {
		String path = System.getProperty("java.library.path");
		path = "src/test/resources/drivers" + ";" + path;
		System.setProperty("java.library.path", path);

		try {
			final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		try {
			String dbUrl = "jdbc:sqlserver://" + serverHost + ";databaseName=" + dbName + ";integratedSecurity=true";
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

			Connection conn = DriverManager.getConnection(dbUrl);
			Statement stmt = conn.createStatement();
			resultSet = stmt.executeQuery(query);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
