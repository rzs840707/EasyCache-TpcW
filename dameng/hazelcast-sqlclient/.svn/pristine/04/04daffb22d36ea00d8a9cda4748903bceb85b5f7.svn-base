package org.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hazelcast.core.Hazelcast;

public class ConnectionTest {
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		Connection conn = SQLConnector.getConnection();
		Statement st = conn.createStatement();
		String sql = "select * from item";
		ResultSet rs = st.executeQuery(sql);
		int cnt = 0;
		while (rs.next()) {
			assert(rs.getInt(1) == rs.getInt("i_id"));
			assert(rs.getString(2) == rs.getString("i_title"));
			assert(rs.getInt(3) == rs.getInt("i_a_id"));
			++cnt;
		}
		System.out.println("total " + cnt + " lines selected");
		rs.close();
		st.close();
		conn.close();
		Hazelcast.shutdownAll();
	}
}
