package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.hazelcast.core.Hazelcast;

public class SelectPreparedStatementTest {
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {
		String[] sqls1 = {
				"SELECT * FROM author, item WHERE author.a_lname LIKE ? AND item.i_a_id = author.a_id ORDER BY item.i_title LIMIT 50",
				"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = ?",
				"SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_title LIMIT 50",
				"SELECT c_passwd FROM customer WHERE c_uname = ?",
				"SELECT co_id FROM country WHERE co_name = ?",
				"SELECT i_id, i_title, a_fname, a_lname FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_pub_date DESC,item.i_title LIMIT 50",
				"SELECT i_id, i_title, a_fname, a_lname FROM item, author, order_line WHERE item.i_id = order_line.ol_i_id AND item.i_a_id = author.a_id AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders)AND item.i_subject = ? GROUP BY i_id, i_title, a_fname, a_lname ORDER BY SUM(ol_qty) DESC LIMIT 50",
				"SELECT o_id FROM customer, orders WHERE customer.c_id = orders.o_c_id AND c_uname = ? ORDER BY o_date, orders.o_id DESC LIMIT 1", };
		String[] sqls2 = {
				"SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = ?",
				"SELECT * FROM shopping_cart_line, item WHERE scl_i_id = item.i_id AND scl_sc_id = ?",
				"SELECT * from shopping_cart_line where scl_sc_id = ?",
				"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?",
				"SELECT c_addr_id FROM customer WHERE customer.c_id = ?",
				"SELECT c_discount FROM customer WHERE customer.c_id = ?",
				"SELECT c_fname,c_lname FROM customer WHERE c_id = ?",
				"SELECT c_uname FROM customer WHERE c_id = ?",
				"SELECT co_id FROM address, country WHERE addr_id = ? AND addr_co_id = co_id",
				"SELECT i_related1 FROM item where i_id = ?",
				"SELECT i_stock FROM item WHERE i_id = ?",
				"SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?",

		// "SELECT addr_id FROM address WHERE addr_street1 = ? AND addr_street2 = ? AND addr_city = ? AND addr_state = ? AND addr_zip = ? AND addr_co_id = ?"
		};

		ArrayList<String[]> arguments1 = new ArrayList<String[]>();
		String[] argu1_1 = { "c" };
		String[] argu1_2 = { "OG" };
		String[] argu1_3 = { "HUMOR" };
		String[] argu1_4 = { "OG" };
		String[] argu1_5 = { "China" };
		String[] argu1_6 = { "HUMOR" };
		String[] argu1_7 = { "HUMOR" };
		String[] argu1_8 = { "OG" };
		arguments1.add(argu1_1);
		arguments1.add(argu1_2);
		arguments1.add(argu1_3);
		arguments1.add(argu1_4);
		arguments1.add(argu1_5);
		arguments1.add(argu1_6);
		arguments1.add(argu1_7);
		arguments1.add(argu1_8);

		ArrayList<Integer[]> arguments2 = new ArrayList<Integer[]>();
		Integer[] argu2_1 = {100};
		Integer[] argu2_2 = {2};
		Integer[] argu2_3 = {1};
		Integer[] argu2_4 = {1};
		Integer[] argu2_5 = {78};
		Integer[] argu2_6 = {56};
		Integer[] argu2_7 = {12};
		Integer[] argu2_8 = {15};
		Integer[] argu2_9 = {17};
		Integer[] argu2_10 = {997};
		Integer[] argu2_11 = {249};
		Integer[] argu2_12 = {29, 6834};
		
		
		
		arguments2.add(argu2_1);
		arguments2.add(argu2_2);
		arguments2.add(argu2_3);
		arguments2.add(argu2_4);
		arguments2.add(argu2_5);
		arguments2.add(argu2_6);
		arguments2.add(argu2_7);
		arguments2.add(argu2_8);
		arguments2.add(argu2_9);
		arguments2.add(argu2_10);
		arguments2.add(argu2_11);
		arguments2.add(argu2_12);
		
		Connection conn = SQLConnector.getConnection();
		for (int i = 0; i < sqls1.length; i++) {
			PreparedStatement pst = conn.prepareStatement(sqls1[i]);
			System.out.println(pst.getClass().getName());
			String[] argu = arguments1.get(i);
			for (int j = 0; j < argu.length; j++) {
				pst.setString(j+1, argu[j]);
			}
			ResultSet rs = pst.executeQuery();
			int num = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				for (int j = 1; j <= num; j++) {
					System.out.println(j + ":" + rs.getObject(j));
				}
			}
			System.out.println("========================================================");
			rs.close();
			pst.close();
		}
		
		for (int i = 0; i < sqls2.length; i++) {
			PreparedStatement pst = conn.prepareStatement(sqls2[i]);
			Integer[] argu = arguments2.get(i);
			for (int j = 0; j < argu.length; j++) {
				pst.setInt(j+1, argu[j]);
			}
			ResultSet rs = pst.executeQuery();
			int num = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				for (int j = 1; j <= num; j++) {
					System.out.println(j + ":" + rs.getObject(j));
				}
			}
			System.out.println("========================================================");
			rs.close();
			pst.close();
		}
		conn.close();
		System.out.println("done");
		Hazelcast.shutdownAll();
	}
}
