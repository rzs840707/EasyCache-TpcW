package Select;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

import org.hazelcast.sqlclient.jdbc.SQLConnector;

import com.hazelcast.core.Hazelcast;


public class ItemAuthorSelectTest {
	public void Select() throws ClassNotFoundException, SQLException {
		String sql = "SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND i_id = ?";
		
		Connection conn = SQLConnector.getConnection();
		PreparedStatement pst = conn.prepareStatement(sql);
		ResultSet rs = null;
		
		Runtime runtime = Runtime.getRuntime();
		System.gc();
		long memory = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Memory Cost:" + memory / (1024 * 1024 * 1.0));
		
		Random random = new Random(System.currentTimeMillis());
		int count = 1000;
		int range = 200000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			pst.setInt(1, random.nextInt(range) + 1);
			rs = pst.executeQuery();
		}
		long end = System.currentTimeMillis();
		System.out.println( "SELECT TIME : " + (end - start));
		rs.close();
		pst.close();
		conn.close();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		ItemAuthorSelectTest test = new ItemAuthorSelectTest();
		Scanner scanner = new Scanner(System.in);
		test.Select();
		test.Select();
		System.out.println("Exit?");
		scanner.next();
		Hazelcast.shutdownAll();
	}
}
