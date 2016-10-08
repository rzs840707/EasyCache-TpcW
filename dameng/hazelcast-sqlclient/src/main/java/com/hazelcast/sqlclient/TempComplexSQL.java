package com.hazelcast.sqlclient;

public class TempComplexSQL {
	private String[] sqlList = {
		"SELECT o_id FROM customer, orders WHERE customer.c_id = orders.o_c_id AND c_uname = ? ORDER BY o_date, orders.o_id DESC LIMIT 1",
		"SELECT i_id, i_title, a_fname, a_lname FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_pub_date DESC, item.i_title LIMIT 50",
		"SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_title LIMIT 50",
		"SELECT * FROM author, item WHERE author.a_lname LIKE ? AND item.i_a_id = author.a_id ORDER BY item.i_title LIMIT 50",
		"SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_title LIKE ? ORDER BY item.i_title LIMIT 50",
		"SELECT ol_i_id FROM orders, order_line WHERE orders.o_id = order_line.ol_o_id AND NOT (order_line.ol_i_id = ?) AND orders.o_c_id IN (SELECT o_c_id FROM orders, order_line WHERE orders.o_id = order_line.ol_o_id AND orders.o_id > (SELECT MAX(o_id)-10000 FROM orders) AND order_line.ol_i_id = ?) GROUP BY ol_i_id ORDER BY SUM(ol_qty) DESC LIMIT 5",
		"SELECT i_id, i_title, a_fname, a_lname FROM item, author, order_line WHERE item.i_id = order_line.ol_i_id AND item.i_a_id = author.a_id AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders) AND item.i_subject = ? GROUP BY i_id, i_title, a_fname, a_lname ORDER BY SUM(ol_qty) DESC LIMIT 50",
		"SELECT COUNT(*) from shopping_cart_line where scl_sc_id = ?",
		//complex and as operation
		"SELECT orders.*, customer.*, cc_xacts.cx_type, ship.addr_street1 AS ship_addr_street1, ship.addr_street2 AS ship_addr_street2, ship.addr_state AS ship_addr_state, ship.addr_zip AS ship_addr_zip, ship_co.co_name AS ship_co_name, bill.addr_street1 AS bill_addr_street1, bill.addr_street2 AS bill_addr_street2, bill.addr_state AS bill_addr_state, bill.addr_zip AS bill_addr_zip, bill_co.co_name AS bill_co_name FROM customer, orders, cc_xacts, address AS ship, country AS ship_co, address AS bill, country AS bill_co WHERE orders.o_id = ? AND cx_o_id = orders.o_id AND customer.c_id = orders.o_c_id AND orders.o_bill_addr_id = bill.addr_id AND bill.addr_co_id = bill_co.co_id AND orders.o_ship_addr_id = ship.addr_id AND ship.addr_co_id = ship_co.co_id AND orders.o_c_id = customer.c_id",
		"INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, (SELECT co_id FROM address, country WHERE addr_id = ? AND addr_co_id = co_id))",
	};
	
	public String getSQL(int index){
		return sqlList[index];
	}
}
