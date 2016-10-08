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

public class updatetest {
	public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
		File file = new File("D:\\update_test.txt");
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
		PrintStream ps = new PrintStream(bos, false);
		
		String sqls[] = {
		"UPDATE customer SET c_uname = 'imdg', c_passwd = 'imdg' WHERE c_id = 1",
		"UPDATE item SET i_related1 = 3, i_related2 = 3, i_related3 = 3, i_related4 = 3, i_related5 = 3 WHERE i_id = 1",
		"UPDATE item SET i_stock = 10 WHERE i_id = 1",
		"UPDATE item SET i_pub_date = '2012-12-12'  WHERE i_id = 2"
		};
		String searchsqls[] = {
				"select * from customer where c_uname = 'imdg'",
				"select * from item where i_id = 1",
				"select * from item where i_id = 1",
				"select * from item where i_id = 2"
		};
		for(int loop = 0; loop < 3; ++loop) {
			Connection conn = SQLConnector.getConnection();
			Statement st = conn.createStatement();
			for (int k = 0; k < 4; ++k) {
				ps.println("======================================================================");
				ps.println("SQL[" + k + "]: " + sqls[k]);
				
				int cnt = st.executeUpdate(sqls[k]);
				ps.println("update line number: " + cnt);
				ResultSet rs = st.executeQuery(searchsqls[k]);
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					ps.print("[" + rsmd.getColumnName(i) + ": "
							+ rsmd.getColumnTypeName(i) + ":"
							+ rsmd.getTableName(i) + "]\t");
				}
				ps.println();
				while (rs.next()) {
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						ps.print("[" + rsmd.getColumnName(i) + ": "
								+ rs.getObject(i) + "]\t");
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
