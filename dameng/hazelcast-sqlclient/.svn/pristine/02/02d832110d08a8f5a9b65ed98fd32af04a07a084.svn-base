package simplesqltest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.hazelcast.sqlclient.jdbc.SQLConnector;

import com.hazelcast.core.Hazelcast;

public class selecttest {
	public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException {
		File file = new File("D:\\select_test.txt");
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
		PrintStream ps = new PrintStream(bos, false);
		String sqls[] = {
				"SELECT i_related1 FROM item where i_id = 1",
				
				"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'NGRERE'",
				"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'ATREBA'",
				"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'NGININ'",
				"SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = 8",
				"SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = 391",
				
				"SELECT * FROM order_line, item WHERE ol_o_id = 1 AND ol_i_id = i_id",
				"SELECT * FROM shopping_cart_line, item WHERE scl_i_id = item.i_id AND scl_sc_id = 0",
				"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 989",
				
//				"SELECT addr_id FROM address",
				"SELECT addr_id FROM address WHERE addr_street1 = 'bU_y =+!Qpkz&+:/c~#' AND addr_street2 = 'c#d@p]qp~B)gwrwG]t(&$vz' AND addr_city = 'QkoG#|rcL;BrV;MDoI|%P$|![:kF' AND addr_state = 'u}|~l' AND addr_zip = 'Xj%jA@e?' AND addr_co_id = 12",
				"SELECT addr_id FROM address WHERE addr_street1 = '.ycoxNGDR LY+T&n' AND addr_street2 = 'p~cj;v/AK^T^#|sxzcR-' AND addr_city = 'X(M;' AND addr_state = 'q.Inzdv@kmRu' AND addr_zip = 'FN-P[uf!/' AND addr_co_id = 82",
				"SELECT c_addr_id FROM customer WHERE customer.c_id = 1",
				"SELECT c_discount FROM customer WHERE customer.c_id = 1",
				"SELECT c_fname,c_lname FROM customer WHERE c_id = 1",
				
//				"SELECT c_id FROM customer",
				"SELECT c_passwd FROM customer WHERE c_uname = 'OG'",
				"SELECT c_uname FROM customer WHERE c_id = 1",
				"SELECT co_id FROM country WHERE co_name = 'United States'",
//				"SELECT i_id FROM item",
				
				"SELECT i_related1 FROM item where i_id = 1",
				"SELECT i_stock FROM item WHERE i_id = 1",
				"SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = 0 AND scl_i_id = 2",
				"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 906",
				"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 531"};
		
		for(int loop = 0; loop < 3; ++loop) {
			Connection conn = SQLConnector.getConnection();
			Statement st = conn.createStatement();
			for (int k = 0; k < 22; ++k) {
				ps.println("======================================================================");
				ps.println("SQL[" + k + "]: " + sqls[k]);
				System.out.println(sqls[k]);
				ResultSet rs = st.executeQuery(sqls[k].toLowerCase());
				ResultSetMetaData rsmd = rs.getMetaData();
				System.out.println(rsmd.getColumnCount());
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					ps.print("[" + rsmd.getColumnName(i) + ": "
							+ rsmd.getColumnTypeName(i) + ":"
							+ rsmd.getTableName(i) + "]\t");
					System.out.println("[" + rsmd.getColumnName(i) + ": "
							+ rsmd.getColumnTypeName(i) + ":"
							+ rsmd.getTableName(i) + "]\t");
				}
				ps.println();
				while (rs.next()) {
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						ps.print("[" + rsmd.getColumnName(i) + ": "
								+ rs.getObject(i) + "]\t");
						System.out.println("[" + rsmd.getColumnName(i) + ": "
								+ rs.getObject(i) + "]\t");
					}
					ps.println();
				}
				rs.close();
				ps.println("======================================================================");
			}
			System.out.println("done");
			st.close();
			conn.close();
		}
		ps.close();
		Hazelcast.shutdownAll();
	}
}
