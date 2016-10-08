package LoadData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.hazelcast.sqlclient.HazelcastDatabaseMetaData;
import com.hazelcast.sqlclient.TableMetaData;
import com.hazelcast.sqlclient.load.Loader;

import org.hazelcast.sqlclient.jdbc.SQLConnector;

public class LoadDataTest {

	private HazelcastDatabaseMetaData hazelcastDatabaseMetaData = new HazelcastDatabaseMetaData();
	private Set<String> rsHashSet = new HashSet<String>();

	public void loadDataTest() {
		// SQLConnector.load();

	}

	public void sqlExecuteQuery() {
		try {
			// SQLConnector.load();
			String sql = "SELECT o_id FROM  customer, orders WHERE customer.c_id = orders.o_c_id AND c_uname = ? ORDER BY o_date, orders.o_id DESC LIMIT 1";
			Connection connImdg = SQLConnector.getConnection();
			PreparedStatement pstImdg = connImdg.prepareStatement(sql);
			pstImdg.setInt(1, 45);
			ResultSet rsSetImdg = pstImdg.executeQuery();
			while (rsSetImdg.next()) {
				System.out.println(rsSetImdg.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sqlExecuteQuerySupport() {
		try {
			// SQLConnector.load();
			String sql = "select * from item,author where item.i_a_id = author.a_id and i_id = ?";
			Connection connImdg = SQLConnector.getConnection();
			PreparedStatement pstImdg = connImdg.prepareStatement(sql);
			pstImdg.setInt(1, 45);
			ResultSet rsSetImdg = pstImdg.executeQuery();
			while (rsSetImdg.next()) {
				System.out.println(rsSetImdg.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sqlExecuteQueryNotSupport() {
		try {
			// SQLConnector.load();
			String sql ="SELECT i_id, i_title, a_fname, a_lname FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_pub_date DESC,item.i_title LIMIT 3";
//			String sql= "SELECT i_id, i_title, a_fname, a_lname "
//				+ "FROM item, author, order_line "
//				+ "WHERE item.i_id = order_line.ol_i_id "
//				+ "AND item.i_a_id = author.a_id "
//				+ "AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders)"
//				+ "AND item.i_subject = ? "
//				+ "GROUP BY i_id, i_title, a_fname, a_lname "
//				+ "ORDER BY SUM(ol_qty) DESC " + "LIMIT 50";
//			String sql = "SELECT addr_id FROM address "
//				+ "WHERE addr_street1 = ? " + "AND addr_street2 = ? "
//				+ "AND addr_city = ? " + "AND addr_state = ? "
//				+ "AND addr_zip = ? " + "AND addr_co_id = ?";
			Connection connImdg = SQLConnector.getConnection();
			PreparedStatement pstImdg = connImdg.prepareStatement(sql);
			pstImdg.setString(1, "arts");
			ResultSet rsSetImdg = pstImdg.executeQuery();
			while (rsSetImdg.next()) {
				System.out.println(rsSetImdg.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sqlExecuteUpdate() {
		try {
			String sql = "update shopping_cart set sc_time = ? where sc_id = ?";
			Connection connImdg = SQLConnector.getConnection();
			PreparedStatement pstImdg = connImdg.prepareStatement(sql);
			Timestamp now = new Timestamp(
					new GregorianCalendar().getTimeInMillis());
			pstImdg.setTimestamp(1, now);
			pstImdg.setInt(2,1);
			pstImdg.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sqlExecuteQueryOracleDate() {
		try {
			// SQLConnector.load();
			String sql = "select * from CUSTOMER where c_uname = ?";
			Connection conn = SQLConnector.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, "OG");
			ResultSet rsSet = pst.executeQuery();
			while (rsSet.next()) {
				System.out.println(rsSet.getInt(1));
				System.out.println(rsSet.getDate(9));
				System.out.println(rsSet.getDate(10));
				System.out.println(rsSet.getTimestamp(11));
				System.out.println(rsSet.getTimestamp(12));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sqlExecuteQuery3() throws ClassNotFoundException, SQLException {
		// SQLConnector.load();
		// String sql =
		// "SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?";
		String sql = "select j.i_id,j.i_thumbnail from item i, item j where (i.i_related1 = j.i_id or i.i_related2 = j.i_id or i.i_related3 = j.i_id or i.i_related4 = j.i_id or i.i_related5 = j.i_id) and i.i_id = 914";
		Connection connMysql = SQLConnector.getConnection2();
		PreparedStatement pstMysql = connMysql.prepareStatement(sql);
		ResultSet rsSetMysql = pstMysql.executeQuery();
	}

	public void loadDataValidation() throws Exception {
		// SQLConnector.load();
		Connection conn1 = SQLConnector.getConnection();
		Connection conn2 = SQLConnector.getConnection2();
		for (String tableName : hazelcastDatabaseMetaData.getTableNames()) {
			int num = 0;
			String sql = "select * from " + tableName;
			PreparedStatement pst1 = conn1.prepareStatement(sql);
			ResultSet rsSet1 = pst1.executeQuery();
			TableMetaData tableMetaData = hazelcastDatabaseMetaData
					.getTableMetaData(tableName);
			while (rsSet1.next()) {
				String temp = "";
				for (int i = 0; i < tableMetaData.getColumnSize(); i++) {
					Object attributeValue = Loader.getAttributeValue(
							tableMetaData.getColumnClassByAttributeIndex(i)
									.getName(), rsSet1, i + 1);
					if (attributeValue == null) {
						System.out.println("value null, tableName: "
								+ tableName
								+ " index: "
								+ i
								+ " columnclassName: "
								+ tableMetaData.getColumnClassByAttributeIndex(
										i).getName());
						break;
					}
					temp = temp + " $#@ " + attributeValue.toString();
				}
				num++;
				rsHashSet.add(temp);
			}
			System.out.println("tableName: " + tableName + " rsHashSet size: "
					+ rsHashSet.size() + " num: " + num);
			PreparedStatement pst2 = conn2.prepareStatement(sql);
			ResultSet rsSet2 = pst2.executeQuery();
			while (rsSet2.next()) {
				String temp = "";
				for (int i = 0; i < tableMetaData.getColumnSize(); i++) {
					// String attributeName = tableMetaData.getColumnName(i);
					Object attributeValue = Loader.getAttributeValue(
							tableMetaData.getColumnClassByAttributeIndex(i)
									.getName(), rsSet2, i + 1);
					if (attributeValue == null) {
						System.out.println("value null, tableName: "
								+ tableName
								+ " index: "
								+ i
								+ " columnclassName: "
								+ tableMetaData.getColumnClassByAttributeIndex(
										i).getName());
						break;
					}
					temp = temp + " $#@ " + attributeValue.toString();
				}
				if (!rsHashSet.remove(temp)) {
					System.out.println(tableName + " loaded incorrectly:"
							+ temp);
					break;
				}
			}
			if (rsHashSet.isEmpty()) {
				System.out.println(tableName + " loaded correctly.");
			} else {
				System.out.println(tableName + " loaded incorrectly.");
			}
			rsSet1.close();
			pst1.close();
			rsSet2.close();
			pst2.close();
		}
		conn1.close();
		conn2.close();
	}
	public static void main(String[] args) throws Exception {
		LoadDataTest test = new LoadDataTest();
//		for(int i = 0; i < 3; i++){
//			test.sqlExecuteQueryNotSupport();
//			test.sqlExecuteQuerySupport();
//		}
		// test.loadDataTest();
//		test.sqlExecuteUpdate();
		// test.sqlExecuteQueryOracleDate();
		 test.loadDataValidation();
		System.out.println("Exit?");
	}
	
}
