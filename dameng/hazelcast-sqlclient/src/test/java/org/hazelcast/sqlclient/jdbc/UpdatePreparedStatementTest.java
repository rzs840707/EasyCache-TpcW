package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.hazelcast.core.Hazelcast;

public class UpdatePreparedStatementTest {
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		@SuppressWarnings("unused")
		String[] sqls = {
			"UPDATE item SET i_cost = ?, i_image = ?, i_thumbnail = ?, i_pub_date = ? WHERE i_id = ?",
			"UPDATE item SET i_related1 = ?, i_related2 = ?, i_related3 = ?, i_related4 = ?, i_related5 = ? WHERE i_id = ?",
			"UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?",
			"UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?",
			"UPDATE shopping_cart SET sc_time = ? WHERE sc_id = ?",
			"UPDATE customer SET c_login = ?, c_expiration = ? WHERE c_id = ?",
			"UPDATE customer SET c_uname = ?, c_passwd = ? WHERE c_id = ?",
			"UPDATE item SET i_stock = ? WHERE i_id = ?",
		};
		
		Connection conn = SQLConnector.getConnection();
		String sql = "INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)";
		PreparedStatement pst = conn.prepareStatement(sql);
		pst.setInt(1, 1);
		pst.setInt(2, 1);
		pst.setInt(3, 1);
		pst.executeUpdate();
		sql = "UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?";
		for (int i = 0; i < 50; i++) {
			pst = conn.prepareStatement(sql);
			pst.setInt(1, i);
			pst.setInt(2, 1);
			pst.setInt(3, 1);
			int num = pst.executeUpdate();
			System.out.println("update : " + num);
			pst.close();
		}
		conn.close();
		System.out.println("done");
		Hazelcast.shutdownAll();
	}
}
