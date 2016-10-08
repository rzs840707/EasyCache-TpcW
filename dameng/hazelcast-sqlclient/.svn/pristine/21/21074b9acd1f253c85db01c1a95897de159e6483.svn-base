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

public class deleteTest {
	public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
		File file = new File("D:\\delete_test.txt");
		FileOutputStream fos = new  FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		PrintStream ps = new PrintStream(bos, false);
		String sqls[] = {
				"DELETE FROM shopping_cart_line WHERE scl_sc_id = 1 AND scl_i_id = 1",
				"DELETE FROM shopping_cart_line WHERE scl_sc_id = 10"
		};
		String searchsqls[] = {
				"select * from shopping_cart_line where scl_sc_id = 1 AND scl_i_id = 1",
				"select * from shopping_cart_line where scl_sc_id = 10"
		};
		Connection conn = SQLConnector.getConnection();
		Statement st = conn.createStatement();
		for (int k = 0; k < 1; ++k) {
			ps.println("======================================================================");
			ps.println("SQL[" + k + "]: " + sqls[k]);
			int cnt = st.executeUpdate(sqls[k]);
			ps.println("delete line number: " + cnt);
			ResultSet rs = st.executeQuery(searchsqls[k]);
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				ps.print("[" + rsmd.getColumnName(i) + ": " + rsmd.getColumnTypeName(i) + "]\t");
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
		ps.close();
		Hazelcast.shutdownAll();
	}
}
