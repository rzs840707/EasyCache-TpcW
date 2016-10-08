package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.hazelcast.sqlclient.jdbc.IMDGConnection;
import com.hazelcast.sqlclient.load.ConfigParser;
import com.hazelcast.sqlclient.load.Loader;

public class SQLConnector {
	private static String dbType;
	private static String driver;
	private static String url;
	private static String user;
	private static String password ;

	static {
		try{
			dbType = ConfigParser.getInstance().configParseGetDbType();
			if(dbType.equals("mysql")){
				driver = "com.mysql.jdbc.Driver";
				url = "jdbc:mysql://127.0.0.1:3306/bench4q";
				user = "root";
				password = "root";
			}
			else if(dbType.equals("oracle")){
				driver = "oracle.jdbc.driver.OracleDriver";
				url = "jdbc:oracle:thin:@133.133.134.135:1521:ORCL";
				user = "imdg";
				password = "imdg";
			}
			else if (dbType.equals("shentong")){
				driver = "com.oscar.Driver";
				url = "jdbc:oscar://localhost:2003/IMDG";
				user = "SYSDBA";
				password = "szoscar55";
			}
			else if (dbType.equals("dameng")){
				driver = "dm.jdbc.driver.DmDriver";
				url = "jdbc:dm://192.168.1.87:5236";
				user = "SYSDBA";
				password = "SYSDBA";
			}
			else{
				throw new Exception("dbType error:" + dbType);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Loader loader = new Loader(driver, url, user, password);
		long start = System.currentTimeMillis();
		loader.loadData();
		long end = System.currentTimeMillis();
		System.out.println( "loadData Time : " + (end - start));
	}
	
//	public static void load(){
//		Loader loader = new Loader(driver, url, user, password);
//		long start = System.currentTimeMillis();
//		loader.loadData();
//		long end = System.currentTimeMillis();
//		System.out.println( "loadData Time : " + (end - start));
//	}
//	
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
//		load();
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, user, password);
		if (conn.isClosed()) {
			throw new SQLException("Connection Failure");
		}
		return new IMDGConnection(conn);
	}
	
	public static Connection getConnection2() throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, user, password);
		if (conn.isClosed()) {
			throw new SQLException("Connection Failure");
		}
		return conn;
	}
	
}
