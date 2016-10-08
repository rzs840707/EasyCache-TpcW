package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DriverTest {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		String url = "jdbc:mysql://127.0.0.1:3306/test";
		String user = "muye";
		String password = "muye";
		Class.forName("com.hazelcast.sqlclient.jdbc.IMDGDriver");
		Connection conn = DriverManager.getConnection(url, user, password);
		if (conn.isClosed()) {
			throw new SQLException("Connection Failure");
		}
		PreparedStatement pst = conn.prepareStatement("select * from test1 where id = ?");
		pst.setInt(1, 1);
		ResultSet rs = pst.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int num = rsmd.getColumnCount();
		while (rs.next()) {
			for (int i = 1; i <= num; i++) {
				System.out.println(rsmd.getColumnName(i) + " : " + rs.getObject(i));
			}
		}
		rs.close();
		pst.close();
		conn.close();
	}
}
