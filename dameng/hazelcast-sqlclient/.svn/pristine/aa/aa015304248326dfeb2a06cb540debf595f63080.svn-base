package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hazelcast.core.Hazelcast;

public class DeletePreparedStatementTest {
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {
		@SuppressWarnings("unused")
		String[] sqls = {
				"DELETE FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?",
				"DELETE FROM shopping_cart_line WHERE scl_sc_id = ?" };

//		Connection conn = new IMDGConnection();
		Connection conn = SQLConnector.getConnection();
		String sql = null;
		PreparedStatement pst = null;
		int times = 400000;
		long start = 0;
		
		System.out.println("=============Start==============");	
		start = System.currentTimeMillis();
		sql = "INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)";
		pst = conn.prepareStatement(sql);
		for (int g = 0; g < times; ++g) {
			pst.setInt(1, g);
			pst.setInt(2, g);
			pst.setInt(3, g);
			pst.executeUpdate();
		}
		pst.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Insert Done==============");
		
		start = System.currentTimeMillis();
		sql = "UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?";
		pst = conn.prepareStatement(sql);
		for (int g = 0; g < times; ++g) {
			pst.setInt(1, g);
			pst.setInt(2, g+1);
			pst.setInt(3, g);
			pst.executeUpdate();
		}
		pst.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Update Done==============");
		
		start = System.currentTimeMillis();
		sql = "SELECT * FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?";
		pst = conn.prepareStatement(sql);
		for(int g = 0; g < times; g++) {
			pst.setInt(1, g);
			pst.setInt(2, g);
			ResultSet rs = pst.executeQuery();
			rs.close();
		}
		pst.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Select Done==============");
		
		start = System.currentTimeMillis();
		sql = "DELETE FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?";
		pst = conn.prepareStatement(sql);
		for(int g = 0; g < times; g++) {
			pst.setInt(1, g);
			pst.setInt(2, g);
			pst.executeUpdate();
		}
		pst.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Delete Done==============");
		
		conn.close();
		Hazelcast.shutdownAll();
	}
}
