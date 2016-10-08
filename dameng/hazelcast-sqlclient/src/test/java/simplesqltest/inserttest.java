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

public class inserttest {
	public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
		File file = new File("D:\\insert_test.txt");
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
		PrintStream ps = new PrintStream(bos, false);
		
		String sqls[] = {
				"INSERT INTO shopping_cart (sc_time) VALUES (CURRENT_TIMESTAMP)",
				"INSERT into address (addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES ('jdbctest', 'jdbctest', 'jdbctest', 'jdbctest', 'jdbctest', 65)",
				"INSERT into customer (c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES ('null', 'null', 'muye', 'muye', 5761, '1078717514', 'NG@D&j+XB.com', '2012-12-27', '2012-12-27', '2012-12-27 18:54:32', '2012-12-27 20:54:34', 13.0, 0.0, 0.0, '2012-12-12', '')",
				"INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES (0, 2599, 3, 2, 2, 'imdg')",
				"INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (11,1,9)"
		};
		for(int loop = 0; loop < 3; ++loop) {
			Connection conn = SQLConnector.getConnection();
			Statement st = conn.createStatement();
			for (int k = 0; k < 5; ++k) {
				ps.println("======================================================================");
				ps.println("Insert SQL[" + k + "]: " + sqls[k]);
				int updatenum = st.executeUpdate(sqls[k],
						Statement.RETURN_GENERATED_KEYS);
				ps.println("insert number: " + updatenum);
				ResultSet rs = st.getGeneratedKeys();
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					ps.print("[" + rsmd.getColumnName(i) + ": "
							+ rsmd.getColumnTypeName(i) + "]\t");
				}
				ps.println();
				while (rs.next()) {
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						ps.print("[" + rs.getObject(i) + "]\t");
					}
					ps.println();
				}
				rs.close();
				ps.println("======================================================================");
			}
			System.err.println("done");
			st.close();
			conn.close();
		}
		ps.close();
		Hazelcast.shutdownAll();
	}
}
