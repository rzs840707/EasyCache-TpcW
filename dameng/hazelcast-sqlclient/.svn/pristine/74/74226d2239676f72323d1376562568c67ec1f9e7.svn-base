package simplesqltest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hazelcast.sqlclient.jdbc.SQLConnector;

import com.hazelcast.core.Hazelcast;

public class multithreadselecttest {
	static String sqls[] = {
			"SELECT * from shopping_cart_line where scl_sc_id = ?",
		
			"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'NGRERE'",
			"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'ATREBA'",
			"SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = 'NGININ'",
			"SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = 8",
			"SELECT * FROM order_line, item WHERE ol_o_id = 1 AND ol_i_id = i_id",
			"SELECT * FROM shopping_cart_line, item WHERE scl_i_id = item.i_id AND scl_sc_id = 0",
			//"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 989",
			
			//"SELECT addr_id FROM address",
			"SELECT addr_id FROM address WHERE addr_street1 = 'bU_y =+!Qpkz&+:/c~#' AND addr_street2 = 'c#d@p]qp~B)gwrwG]t(&$vz' AND addr_city = 'QkoG#|rcL;BrV;MDoI|%P$|![:kF' AND addr_state = 'u}|~l' AND addr_zip = 'Xj%jA@e?' AND addr_co_id = 12",
			"SELECT c_addr_id FROM customer WHERE customer.c_id = 1",
			"SELECT c_discount FROM customer WHERE customer.c_id = 1",
			"SELECT c_fname,c_lname FROM customer WHERE c_id = 1",
			
			//"SELECT c_id FROM customer",
			"SELECT c_passwd FROM customer WHERE c_uname = 'OG'",
			"SELECT c_uname FROM customer WHERE c_id = 1",
			"SELECT co_id FROM country WHERE co_name = 'United States'",
			//"SELECT i_id FROM item",
			
			"SELECT i_related1 FROM item where i_id = 1",
			"SELECT i_stock FROM item WHERE i_id = 1",
			"SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = 0 AND scl_i_id = 2",
//			"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 906",
//			"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = 531"
			};
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
		Connection conn = SQLConnector.getConnection();
		int nthreads = 2;
		PreparedStatement[] sts = new PreparedStatement[nthreads];
		
		for (int i = 0; i < sts.length; i++) {
			sts[i] = conn.prepareStatement(sqls[0]);
			sts[i].setInt(1, 3);
		}
		ExecutorService exec = Executors.newFixedThreadPool(nthreads);
		for (int i = 0; i < sts.length; i++) {
			exec.submit(new selectthread(sts[i]));
		}
		exec.shutdown();
		exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		System.out.println("done");
		Hazelcast.shutdownAll();
	}
}
