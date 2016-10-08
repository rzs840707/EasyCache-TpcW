package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hazelcast.core.Hazelcast;

public class InsertPreparedStatementTest {
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		@SuppressWarnings("unused")
		String[] sqls = {
			"INSERT INTO shopping_cart (sc_time) VALUES (?)",
			"INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)",
			"INSERT into customer (c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			"INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			"INSERT into address (addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES (?, ?, ?, ?, ?, ?)",
			"INSERT into orders (o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			"INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES (?, ?, ?, ?, ?, ?)"
		};
		
		Connection conn = SQLConnector.getConnection();
		String sql = "INSERT into address (addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES (?, ?, ?, ?, ?, ?)";
		for (int i = 0; i < 1; i++) {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, null);
			pst.setString(2, null);
			pst.setString(3, "00000000000000000000");
			pst.setString(4, "000000000000000");
			pst.setString(5, "000000");
			pst.setInt(6, 85);
			int num = pst.executeUpdate();
			System.out.println("update : " + num);
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				System.out.println(rs.getInt(1));
			}
			pst.close();
		}
		conn.close();
		System.out.println("done");
		Hazelcast.shutdownAll();
	}
}
