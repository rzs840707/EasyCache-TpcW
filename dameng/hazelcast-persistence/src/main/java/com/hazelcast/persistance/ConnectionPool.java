package com.hazelcast.persistance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionPool {
 
	public static String datasourceName; 
	private static InitialContext ctx;

	private static void initial() {
		datasourceName = DefaultMapStore.getDataSource();
		try {
			ctx = new InitialContext();
		} catch (javax.naming.NamingException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		Connection conn = null;

		if (ctx == null)
			initial();
		
		//jiang yong 2014-11-20
		conn = getTomcatConnection();
//		if(datasourceName.contains("env") || datasourceName.contains("ENV")){
//			conn = getTomcatConnection();
//		}else{
//			conn = getJDBCConnection();
//		}
		
		return conn;
	}
	public static Connection getTomcatConnection() {
		Connection conn = null;
		try {
			DataSource ds = (DataSource) ctx.lookup(datasourceName);
			conn = ds.getConnection();
			// conn.setAutoCommit(false);
		} catch (NamingException ne) {
			ne.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static Connection getJDBCConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(datasourceName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

}
