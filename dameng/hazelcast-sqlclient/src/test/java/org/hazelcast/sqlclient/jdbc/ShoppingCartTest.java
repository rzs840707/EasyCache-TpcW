package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import com.hazelcast.core.Hazelcast;

public class ShoppingCartTest {
	public static void UpdateSelect(Connection conn, int times, Random random) throws SQLException {
		String sql1 = "UPDATE shopping_cart SET sc_time = ? WHERE sc_id = ?";
		String sql2 = "SELECT * FROM shopping_cart WHERE sc_id = ?";
		PreparedStatement pst1 = conn.prepareStatement(sql1);
		PreparedStatement pst2 = conn.prepareStatement(sql2);
		
		long start = System.currentTimeMillis();
		for (int g = 0; g < times; ++g) {
			pst1.setString(1, "2012-01-17 10:00:00");
			pst1.setInt(2, random.nextInt(times) + 1);
			pst1.executeUpdate();
			
			pst2.setInt(1, random.nextInt(times) + 1);
			ResultSet rs = pst2.executeQuery();
			rs.close();
		}
		pst1.close();
		pst2.close();
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============UpdateSelect Done==============");		
	}
	
	public static void ALLOperationWithThinkTime(Connection conn, int times, Random random, int thinktime) throws SQLException, InterruptedException {
		System.out.println("=============Start==============");
		long start = System.currentTimeMillis();
		String sql = "INSERT INTO shopping_cart (sc_time) VALUES (?)";
		PreparedStatement pst = null;
		for (int g = 0; g < times; ++g) {
			pst = conn.prepareStatement(sql);
			pst.setString(1, "2012-01-17 12:00:00");
			pst.executeUpdate();
			Thread.sleep(0, thinktime);
			pst.close();
		}
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Insert Done==============");
		
		start = System.currentTimeMillis();
		sql = "UPDATE shopping_cart SET sc_time = ? WHERE sc_id = ?";
		for (int g = 0; g < times; ++g) {
			pst = conn.prepareStatement(sql);
			pst.setString(1, "2012-01-17 10:00:00");
			pst.setInt(2, random.nextInt(times) + 1);
			pst.executeUpdate();
			Thread.sleep(0, thinktime);
			pst.close();
		}
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Update Done==============");
		
		start = System.currentTimeMillis();
		sql = "SELECT * FROM shopping_cart WHERE sc_id = ?";
		for(int g = 0; g < times; g++) {
			pst = conn.prepareStatement(sql);
			pst.setInt(1, random.nextInt(times) + 1);
			ResultSet rs = pst.executeQuery();
			rs.close();
			Thread.sleep(0, thinktime);
			pst.close();
		}
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Select Done==============");
		
		start = System.currentTimeMillis();
		sql = "DELETE FROM shopping_cart WHERE sc_id = ?";
		for(int g = 1; g <= times; g++) {
			pst = conn.prepareStatement(sql);
			pst.setInt(1, g);
			pst.executeUpdate();
			Thread.sleep(0, thinktime);
			pst.close();
		}
		System.out.println((System.currentTimeMillis() - start) / 1000);
		System.out.println("=============Delete Done==============");
	}
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException, InterruptedException {
		
		Connection conn = SQLConnector.getConnection();
		int times = 100;
		int thinktime = 10;
		Random random = new Random(System.currentTimeMillis());
		
		ShoppingCartTest.ALLOperationWithThinkTime(conn, times, random, thinktime);
		conn.close();
		Hazelcast.shutdownAll();
	}
}
