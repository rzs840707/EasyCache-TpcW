UPDATE item SET i_cost = ?, i_image = ?, i_thumbnail = ?, i_pub_date = CURRENT_DATE WHERE i_id = ?
INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, (SELECT co_id FROM address, country WHERE addr_id = ? AND addr_co_id = co_id))
INSERT into orders (o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) VALUES ( ?, CURRENT_DATE, ?, 8.25, ?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), ?, ?, 'Pending')
SELECT o_id FROM customer, orders WHERE customer.c_id = orders.o_c_id AND c_uname = ? LIMIT 1
SELECT i_id, i_title, a_fname, a_lname FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? LIMIT 50
SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_title LIKE ? LIMIT 50
SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? LIMIT 50
SELECT * FROM author, item WHERE author.a_lname LIKE ? AND item.i_a_id = author.a_id LIMIT 50
SELECT COUNT(*) from shopping_cart_line where scl_sc_id = ?
UPDATE shopping_cart SET sc_time = CURRENT_TIMESTAMP WHERE sc_id = ?
UPDATE customer SET c_login = CURRENT_TIMESTAMP, c_expiration = ADDDATE(CURRENT_TIMESTAMP, INTERVAL 2 HOUR) WHERE c_id = ?
INSERT INTO shopping_cart (sc_time) VALUES (CURRENT_TIMESTAMP)



DELETE FROM shopping_cart_line WHERE scl_sc_id = ?
DELETE FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?
INSERT into address (addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES (?, ?, ?, ?, ?, ?)
INSERT into customer (c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES (?, ?, ?, ?, ?, ?)
INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)
SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = ?
SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = ?
SELECT * FROM order_line, item WHERE ol_o_id = ? AND ol_i_id = i_id
SELECT * FROM shopping_cart_line, item WHERE scl_i_id = item.i_id AND scl_sc_id = ?
SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?
SELECT addr_id FROM address
SELECT addr_id FROM address WHERE addr_street1 = ? AND addr_street2 = ? AND addr_city = ? AND addr_state = ? AND addr_zip = ? AND addr_co_id = ?
SELECT c_addr_id FROM customer WHERE customer.c_id = ?
SELECT c_discount FROM customer WHERE customer.c_id = ?
SELECT c_fname,c_lname FROM customer WHERE c_id = ?
SELECT c_id FROM customer
SELECT c_passwd FROM customer WHERE c_uname = ?
SELECT c_uname FROM customer WHERE c_id = ?
SELECT co_id FROM country WHERE co_name = ?
SELECT i_id FROM item
SELECT i_related1 FROM item where i_id = ?
SELECT i_stock FROM item WHERE i_id = ?
SELECT orders.*, customer.*, cc_xacts.cx_type, ship.addr_street1 AS ship_addr_street1, ship.addr_street2 AS ship_addr_street2, ship.addr_state AS ship_addr_state, ship.addr_zip AS ship_addr_zip, ship_co.co_name AS ship_co_name, bill.addr_street1 AS bill_addr_street1, bill.addr_street2 AS bill_addr_street2, bill.addr_state AS bill_addr_state, bill.addr_zip AS bill_addr_zip, bill_co.co_name AS bill_co_name FROM customer, orders, cc_xacts, address AS ship, country AS ship_co, address AS bill, country AS bill_co WHERE orders.o_id = ? AND cx_o_id = orders.o_id AND customer.c_id = orders.o_c_id AND orders.o_bill_addr_id = bill.addr_id AND bill.addr_co_id = bill_co.co_id AND orders.o_ship_addr_id = ship.addr_id AND ship.addr_co_id = ship_co.co_id AND orders.o_c_id = customer.c_id
SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?
UPDATE customer SET c_uname = ?, c_passwd = ? WHERE c_id = ?
UPDATE item SET i_stock = ? WHERE i_id = ?
UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?
