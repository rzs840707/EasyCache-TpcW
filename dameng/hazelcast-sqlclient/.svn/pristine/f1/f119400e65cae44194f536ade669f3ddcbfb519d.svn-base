package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hazelcast.core.Hazelcast;

public class SQLCustomerTest {
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {

		// Connection conn = new IMDGConnection();
		Connection conn = SQLConnector.getConnection();
		String sql = null;
		PreparedStatement pst = null;
		int times = 10000;

		System.out.println("=============Start==============");
		long start = System.currentTimeMillis();
		sql = "UPDATE customer SET c_login = '2012-01-17 10:00:00', c_expiration = '2012-01-17 12:00:00' WHERE c_id = ?";
		pst = conn.prepareStatement(sql);
		for (int g = 1; g <= times; ++g) {
			pst.setInt(1, g);
			pst.executeUpdate();
		}
		pst.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Update Done==============");

		start = System.currentTimeMillis();
		sql = "SELECT c_discount FROM customer WHERE customer.c_id = ?";
		pst = conn.prepareStatement(sql);
		for (int g = 1; g <= times; g++) {
			pst.setInt(1, g);
			ResultSet rs = pst.executeQuery();
			// while (rs.next()) {
			// System.out.println(rs.getInt(1) + " : " + rs.getTimestamp(2));
			// }
			rs.close();
		}
		pst.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Select Done==============");

		conn.close();
		Hazelcast.shutdownAll();
	}
}
