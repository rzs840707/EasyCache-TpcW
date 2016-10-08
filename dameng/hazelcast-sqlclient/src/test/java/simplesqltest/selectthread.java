package simplesqltest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class selectthread extends Thread {
	
	private PreparedStatement pst = null;
	@Override
	public void run() {
		String name = Thread.currentThread().getName();
		try {
			pst.executeQuery();
			ResultSet rs = pst.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				System.out.print(name + "#\t");
				for (int k = 1; k <= rsmd.getColumnCount(); k++) {
					System.out.print("[" + rsmd.getColumnName(k) + ": " + rs.getObject(k) + "]\t");
			 	}
				System.out.println();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Finally: " + name);
		}
	}
	public selectthread(PreparedStatement pst) {
		super();
		this.pst = pst;
	}
	
}
