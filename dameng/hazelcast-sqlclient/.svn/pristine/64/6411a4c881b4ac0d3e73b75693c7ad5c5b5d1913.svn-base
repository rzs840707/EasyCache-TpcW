package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ResultSetMetaDataTest {
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		Connection conn = SQLConnector.getConnection();
		Statement st = conn.createStatement();
		String sql = "select * from item";
		ResultSet rs = st.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			System.out.println(
					"CatalogName = " + rsmd.getCatalogName(i)
					+ " : TableName = " + rsmd.getTableName(i)
					+ " : ColumnCount = " + rsmd.getColumnCount()
					+ " : ColumnType = " + rsmd.getColumnType(i)
					+ " : ColumnTypeName  = " + rsmd.getColumnTypeName(i)
					+ " : ColumnLabel = " + rsmd.getColumnLabel(i));
		}
		rs.close();
		st.close();
		conn.close();
	}
}
