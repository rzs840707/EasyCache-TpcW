package com.hazelcast.sqlclient;

public class TempExample {
	private String[] sqlList = {
		"INSERT INTO User(Name, Address, Age) VALUES ('shuping', 'beijing', 24)",
		"SELECT name, address, age FROM User WHERE age = 24",						//1
		"DELETE FROM User WHERE age = 24",							//2
		"SELECT c_fname,c_lname FROM customer WHERE c_uname = OG",	//3
		"SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id < 10", //4
		"INSERT INTO shopping_cart (sc_time) VALUES (CURRENT TIMESTAMP )",	//5
		"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?",
		"SELECT i_id, i_title, a_fname, a_lname FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_pub_date DESC,item.i_title FETCH FIRST 50 ROWS ONLY",
		"SELECT i_id, i_title, a_fname, a_lname FROM item, author, order_line WHERE item.i_id = order_line.ol_i_id AND item.i_a_id = author.a_id AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders) AND item.i_subject = ? GROUP BY i_id, i_title, a_fname, a_lname ORDER BY SUM(ol_qty) DESC FETCH FIRST 50 ROWS ONLY",
		"SELECT * FROM mytable0 as aliastable0, mytable1 as alias_tab1, mytable2 as alias_tab2, (SELECT * FROM mytable3) AS aliastable3 WHERE mytable.col = 9",

		"SELECT  C_FNAME,C_LNAME,C_EMAIL,C_PHONE, O_ID,O_DATE,O_SUBTOTAL,O_TAX,O_TOTAL,O_SHIP_TYPE,O_SHIP_DATE, " +
				"O_BILL_ADDR,O_SHIP_ADDR,O_CC_TYPE,O_STATUS,ADDR_STREET1,ADDR_STREET2,ADDR_CITY,ADDR_STATE,ADDR_ZIP,CO_NAME " +
				"FROM CUSTOMER,ADDRESS,COUNTRY,ORDERS " +
				"where C_ID=O_C_ID AND C_ADDR_ID=ADDR_ID AND ADDR_CO_ID=CO_ID AND O_C_ID=C_ID AND O_ID=2",
				
		"SELECT * FROM CUSTOMER,ADDRESS,COUNTRY,ORDERS " +
			"where O_C_ID=C_ID AND C_ADDR_ID=ADDR_ID AND ADDR_CO_ID=CO_ID AND O_ID=2"
	};
	
	public String getSQL(int index){
		return sqlList[index];
	}
}
