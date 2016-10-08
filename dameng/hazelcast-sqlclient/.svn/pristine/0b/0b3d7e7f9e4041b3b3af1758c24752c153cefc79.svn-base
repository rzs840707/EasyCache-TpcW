package com.hazelcast.sqlclient;

public class TempSimpleSelect {
	private String[] sqlList = {
		"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'ALBAATIN'",
		"SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = 1",
		"SELECT * FROM order_line, item WHERE ol_o_id = 1 AND ol_i_id = i_id",
		"SELECT * FROM shopping_cart_line, item WHERE scl_i_id = item.i_id AND scl_sc_id = 1",
		"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 1",

		//"SELECT addr_id FROM address",
		"SELECT addr_id FROM address WHERE addr_street1 = 'abc' AND addr_street2 = 'abc' AND addr_city = 'abc' AND addr_state = 'abc' AND addr_zip = 123 AND addr_co_id = 1",
		"SELECT c_addr_id FROM customer WHERE customer.c_id = 1",
		"SELECT c_discount FROM customer WHERE customer.c_id = 1",
		"SELECT c_fname,c_lname FROM customer WHERE c_id = 1",
		
		//"SELECT c_id FROM customer",
		"SELECT c_passwd FROM customer WHERE c_uname = 'OG'",
		"SELECT c_uname FROM customer WHERE c_id = 1",
		"SELECT co_id FROM country WHERE co_name = 'Zambia'",
		//"SELECT i_id FROM item",
		
		"SELECT i_related1 FROM item where i_id = 1",
		"SELECT i_stock FROM item WHERE i_id = 1",
		"SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = 1 AND scl_i_id = 10",
	};
	public String getSQL(int index){
		return sqlList[index];
	}
}

