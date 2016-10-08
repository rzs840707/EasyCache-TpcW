package com.hazelcast.sqlclient;

public class TempSimpleUpdate {
	private String[] sqlList = {
		"select * from shopping_cart_line where scl_sc_id = 1",
		"DELETE FROM shopping_cart_line WHERE scl_sc_id = 1",
		"select * from shopping_cart_line where scl_sc_id = 1",
		
		"select * from shopping_cart_line where scl_sc_id = 1 and scl_i_id = 10",
		"DELETE FROM shopping_cart_line WHERE scl_sc_id = 1 AND scl_i_id = 10",
		"select * from shopping_cart_line where scl_sc_id = 1 and scl_i_id = 10",
		
		"select * from address where addr_street1 = bacdfad",
		"INSERT into address (addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES (null, 'haidianqun', 'Beijing', 'Beijing', '100190', 12324)",
		"select * from address where addr_street1 = bacdfad",		

"INSERT into customer (c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES ( 'imdg', 'imdg', 'imdg', 'imdg', 4635, '9876543210', 'jdbc@test.com', '2011-12-06', '2011-12-18', '2012-12-21 10:53:59', '2012-12-21 12:53:59', 2, 0, 473, '1978-10-10', 'imdg')",
"INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES (?, ?, ?, ?, ?, ?)",
"INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)",

		"select * from customer where c_id=1",
		"UPDATE customer SET c_uname = 'ol', c_passwd = 'ol' WHERE c_id = 1",
		"select * from customer where c_id=1",
		
		"select * from item where i_id=1",
		"UPDATE item SET i_related1 = 2, i_related2 = 2, i_related3 = 2, i_related4 = 2, i_related5 = 2 WHERE i_id = 1",
		"select * from item where i_id=1",
		
		"select * from item where i_id=1",
		"UPDATE item SET i_stock = 10 WHERE i_id = 1",
		"select * from item where i_id=1",

		"select * from shopping_cart_line where scl_sc_id = 1 AND scl_i_id = 10",
		"UPDATE shopping_cart_line SET scl_qty = 222 WHERE scl_sc_id = 1 AND scl_i_id = 10",
		"select * from shopping_cart_line where scl_sc_id = 1 AND scl_i_id = 10",
		
"UPDATE customer SET c_login = CURRENT_TIMESTAMP, c_expiration = ADDDATE(CURRENT_TIMESTAMP, INTERVAL 2 HOUR) WHERE c_id = 1",
"INSERT INTO shopping_cart (sc_time) VALUES (CURRENT_TIMESTAMP)",
"UPDATE item SET i_cost = ?, i_image = ?, i_thumbnail = ?, i_pub_date = CURRENT_DATE WHERE i_id = 1",
"INSERT into orders (o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) VALUES ( ?, CURRENT_DATE, ?, 8.25, ?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), ?, ?, 'Pending')",
"UPDATE shopping_cart SET sc_time = CURRENT_TIMESTAMP WHERE sc_id = 1",

	};
	public String getSQL(int index){
		return sqlList[index];
	}
}

