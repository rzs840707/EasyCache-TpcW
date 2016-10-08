package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class PreStmResultSetMetaDataTest {
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		Connection conn = SQLConnector.getConnection();
		PreparedStatement pst = conn.prepareStatement("select i_title, i_pub_date, i_publisher from item where i_id = ?");
		ResultSetMetaData rsmd = pst.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			System.out.println(
					"CatalogName = " + rsmd.getCatalogName(i)
					+ " : TableName = " + rsmd.getTableName(i)
					+ " : ColumnCount = " + rsmd.getColumnCount()
					+ " : ColumnType = " + rsmd.getColumnType(i)
					+ " : ColumnTypeName  = " + rsmd.getColumnTypeName(i)
					+ " : ColumnLabel = " + rsmd.getColumnLabel(i));
		}
		pst.setInt(1, 1);
		if(pst.execute()) {
			ResultSet rs = pst.getResultSet();
			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					System.out.println(rs.getString(1) + "  ||  " + rs.getDate(2) + "  ||  " + rs.getString(3));
					System.out.println(rs.getString("i_title") + "  ||  " + rs.getDate("i_pub_date") + "  ||  " + rs.getString("i_publisher"));
				}
			}
		}
		pst.close();
		conn.close();
	}
}
