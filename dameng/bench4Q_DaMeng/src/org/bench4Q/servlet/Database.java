package org.bench4Q.servlet;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Vector;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Database
{
  static java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
  public static String dsName;
  private static InitialContext ctx;

  private static void initial()
  {
    DBHelper dbhelper = DBHelper.getInstance();
    dsName = dbhelper.getProperty("dsName");
    try {
      ctx = new InitialContext();
    } catch (NamingException e) {
      e.printStackTrace();
    }
  }

  public static Connection getConnection() throws SQLException, ClassNotFoundException {
    Connection conn = null;
    try {
      if (ctx == null)
        initial();
      DataSource ds = null;
      ds = (DataSource)ctx.lookup(dsName);
      conn = ds.getConnection();
    } catch (NamingException ne) {
      ne.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return conn;
  }

  public static void closeConnection(Connection con) {
    try {
      if (con != null)
        con.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void closeStmt(PreparedStatement stmt) {
    try {
      if (stmt != null)
        stmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void closeStmt(Statement stmt) {
    try {
      if (stmt != null)
        stmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void closeResultSet(ResultSet rs) {
    try {
      if (rs != null)
        rs.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static String[] getName(int c_id) {
    String[] name = new String[2];
    Connection con = null;
    PreparedStatement get_name = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();

      get_name = con.prepareStatement("SELECT c_fname,c_lname FROM customer WHERE c_id = ?");

      get_name.setInt(1, c_id);

      rs = get_name.executeQuery();

      rs.next();
      name[0] = rs.getString("c_fname");
      name[1] = rs.getString("c_lname");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(get_name);
      closeConnection(con);
    }
    return name;
  }

  public static Book getBook(int i_id) {
    Book book = null;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      statement = con
        .prepareStatement("SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = ?");

      statement.setInt(1, i_id);
      rs = statement.executeQuery();

      rs.next();
      book = new Book(rs);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    return book;
  }

  public static Customer getCustomer(String UNAME) {
    Customer cust = null;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      statement = con
        .prepareStatement("SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = ?");

      statement.setString(1, UNAME);
      rs = statement.executeQuery();

      if (rs.next())
        cust = new Customer(rs);
      else
        return null;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    closeResultSet(rs);
    closeStmt(statement);
    closeConnection(con);

    return cust;
  }

  public static Vector doSubjectSearch(String search_key)
  {
    Vector vec = new Vector();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();

      statement = con
        .prepareStatement("SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ?  AND rownum < 51 ORDER BY item.i_title");

      statement.setString(1, search_key);
      rs = statement.executeQuery();

      while (rs.next()) {
        vec.addElement(new Book(rs));
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    return vec;
  }

  public static Vector doTitleSearch(String search_key)
  {
    Vector vec = new Vector();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();

      statement = con
        .prepareStatement("SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_title LIKE ?  AND rownum < 51 ORDER BY item.i_title");

      statement.setString(1, search_key + "%");
      rs = statement.executeQuery();

      while (rs.next()) {
        vec.addElement(new Book(rs));
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    return vec;
  }

  public static Vector doAuthorSearch(String search_key)
  {
    Vector vec = new Vector();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      statement = con
        .prepareStatement("SELECT * FROM author, item WHERE author.a_lname LIKE ? AND item.i_a_id = author.a_id AND rownum < 51 ORDER BY item.i_title");

      statement.setString(1, search_key + "%");
      rs = statement.executeQuery();

      while (rs.next()) {
        vec.addElement(new Book(rs));
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    return vec;
  }

  public static Vector getNewProducts(String subject)
  {
    Vector vec = new Vector();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      statement = con
        .prepareStatement("SELECT i_id, i_title, a_fname, a_lname FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? AND rownum < 51 ORDER BY item.i_pub_date DESC,item.i_title ");

      statement.setString(1, subject);
      rs = statement.executeQuery();

      while (rs.next()) {
        vec.addElement(new ShortBook(rs));
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    return vec;
  }

  public static Vector getBestSellers(String subject)
  {
    Vector vec = new Vector();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();

      statement = con
        .prepareStatement("SELECT i_id, i_title, a_fname, a_lname FROM item, author, order_line WHERE item.i_id = order_line.ol_i_id AND item.i_a_id = author.a_id AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders)AND item.i_subject = ? AND rownum < 51 GROUP BY i_id, i_title, a_fname, a_lname ORDER BY SUM(ol_qty) DESC ");

      statement.setString(1, subject);
      rs = statement.executeQuery();

      while (rs.next()) {
        vec.addElement(new ShortBook(rs));
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
    return vec;
  }

  public static void getRelated(int i_id, Vector i_id_vec, Vector i_thumbnail_vec) {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      statement = con
        .prepareStatement("SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?");

      statement.setInt(1, i_id);
      rs = statement.executeQuery();

      i_id_vec.removeAllElements();
      i_thumbnail_vec.removeAllElements();

      while (rs.next()) {
        i_id_vec.addElement(new Integer(rs.getInt(1)));
        i_thumbnail_vec.addElement(rs.getString(2));
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeConnection(con);
    }
  }

  public static void adminUpdate(int i_id, double cost, String image, String thumbnail)
  {
    Connection con = null;
    PreparedStatement statement = null;
    PreparedStatement related = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      statement = con
        .prepareStatement("UPDATE item SET i_cost = ?, i_image = ?, i_thumbnail = ?, i_pub_date = CURRENT_DATE WHERE i_id = ?");

      statement.setDouble(1, cost);
      statement.setString(2, image);
      statement.setString(3, thumbnail);
      statement.setInt(4, i_id);
      statement.executeUpdate();
      statement.close();
      related = con
        .prepareStatement("SELECT ol_i_id FROM orders, order_line WHERE orders.o_id = order_line.ol_o_id AND NOT (order_line.ol_i_id = ?) AND orders.o_c_id IN (SELECT o_c_id                       FROM orders, order_line                       WHERE orders.o_id = order_line.ol_o_id                       AND orders.o_id > (SELECT MAX(o_id)-10000 FROM orders)                      AND order_line.ol_i_id = ?)                       AND rownum < 6 GROUP BY ol_i_id ORDER BY SUM(ol_qty) DESC ");

      related.setInt(1, i_id);
      related.setInt(2, i_id);
      rs = related.executeQuery();

      int[] related_items = new int[5];

      int counter = 0;
      int last = 0;
      while (rs.next()) {
        last = rs.getInt(1);
        related_items[counter] = last;
        counter++;
      }

      for (int i = counter; i < 5; i++) {
        last++;
        related_items[i] = last;
      }

      statement = con
        .prepareStatement("UPDATE item SET i_related1 = ?, i_related2 = ?, i_related3 = ?, i_related4 = ?, i_related5 = ? WHERE i_id = ?");

      statement.setInt(1, related_items[0]);
      statement.setInt(2, related_items[1]);
      statement.setInt(3, related_items[2]);
      statement.setInt(4, related_items[3]);
      statement.setInt(5, related_items[4]);
      statement.setInt(6, i_id);
      statement.executeUpdate();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
      closeStmt(related);
      closeConnection(con);
    }
  }

  public static String GetUserName(int C_ID) {
    String u_name = null;
    Connection con = null;
    PreparedStatement get_user_name = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();
      get_user_name = con.prepareStatement("SELECT c_uname FROM customer WHERE c_id = ?");

      get_user_name.setInt(1, C_ID);
      rs = get_user_name.executeQuery();

      if (rs.next()) u_name = rs.getString("c_uname"); 
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(get_user_name);
      closeConnection(con);
    }
    return u_name;
  }

  public static String GetPassword(String C_UNAME) {
    String passwd = null;
    Connection con = null;
    PreparedStatement get_passwd = null;
    ResultSet rs = null;
    try {
      con = getConnection();
      get_passwd = con.prepareStatement("SELECT c_passwd FROM customer WHERE c_uname = ?");

      get_passwd.setString(1, C_UNAME);
      rs = get_passwd.executeQuery();

      rs.next();
      passwd = rs.getString("c_passwd");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(get_passwd);
      closeConnection(con);
    }
    return passwd;
  }

  private static int getRelated1(int I_ID, Connection con)
  {
    int related1 = -1;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      statement = con.prepareStatement("SELECT i_related1 FROM item where i_id = ?");
      statement.setInt(1, I_ID);
      rs = statement.executeQuery();
      if (rs.next())
        related1 = rs.getInt(1);
      else
        related1 = 10;
    } catch (Exception ex) {
      System.out.println("I_ID is " + I_ID);
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
    }
    return related1;
  }

  public static Order GetMostRecentOrder(String c_uname, Vector order_lines)
  {
    Connection con = null;
    PreparedStatement get_most_recent_order_id = null;
    PreparedStatement get_order = null;
    PreparedStatement get_order_lines = null;
    ResultSet rs = null;
    ResultSet rs2 = null;
    ResultSet rs3 = null;
    try {
      order_lines.removeAllElements();

      con = getConnection();

      get_most_recent_order_id = con.prepareStatement("SELECT o_id FROM customer, orders WHERE customer.c_id = orders.o_c_id AND c_uname = ? AND rownum < 2 ORDER BY o_date, orders.o_id DESC ");

      get_most_recent_order_id.setString(1, c_uname);
      rs = get_most_recent_order_id.executeQuery();
      int order_id;
      if (rs.next())
        order_id = rs.getInt("o_id");
      else
        return null;
      get_order = con.prepareStatement("SELECT orders.*, customer.*,   cc_xacts.cx_type,   ship.addr_street1 AS ship_addr_street1,   ship.addr_street2 AS ship_addr_street2,   ship.addr_state AS ship_addr_state,   ship.addr_zip AS ship_addr_zip,   ship_co.co_name AS ship_co_name,   bill.addr_street1 AS bill_addr_street1,   bill.addr_street2 AS bill_addr_street2,   bill.addr_state AS bill_addr_state,   bill.addr_zip AS bill_addr_zip,   bill_co.co_name AS bill_co_name FROM customer, orders, cc_xacts,  address  ship,   country  ship_co,   address  bill,    country  bill_co WHERE orders.o_id = ?   AND cc_xacts.cx_o_id = orders.o_id   AND customer.c_id = orders.o_c_id   AND orders.o_bill_addr_id = bill.addr_id   AND bill.addr_co_id = bill_co.co_id   AND orders.o_ship_addr_id = ship.addr_id   AND ship.addr_co_id = ship_co.co_id   AND orders.o_c_id = customer.c_id");

      get_order.setInt(1, order_id);
      rs2 = get_order.executeQuery();

      if (!rs2.next())
      {
        return null;
      }
      Order order = new Order(rs2);

      get_order_lines = con.prepareStatement("SELECT * FROM order_line, item WHERE ol_o_id = ? AND ol_i_id = i_id");

      get_order_lines.setInt(1, order_id);
      rs3 = get_order_lines.executeQuery();

      while (rs3.next()) {
        order_lines.addElement(new OrderLine(rs3));
      }

      return order;
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeResultSet(rs2);
      closeResultSet(rs3);
      closeStmt(get_most_recent_order_id);
      closeStmt(get_order);
      closeStmt(get_order_lines);
      closeConnection(con);
    }
    return null;
  }

  public static int createEmptyCart()
  {
    Connection con = null;
    PreparedStatement insert_cart = null;
    PreparedStatement get_id = null;
    ResultSet rs = null;
    int SHOPPING_ID = 0;
    try {
      con = getConnection();
      insert_cart = null;
      rs = null;
      insert_cart = con.prepareStatement(
        "INSERT INTO shopping_cart (sc_id, sc_time) VALUES (SHOPPING_CART_SEQ.nextval, CURRENT_TIMESTAMP)", 
        1);
      insert_cart.executeUpdate();

      get_id = con.prepareStatement("SELECT SHOPPING_CART_SEQ.currval FROM SHOPPING_CART");
      rs = get_id.executeQuery();

      if (rs.next())
        SHOPPING_ID = rs.getInt(1);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(insert_cart);
      closeConnection(con);
    }
    return SHOPPING_ID;
  }

  public static Cart doCart(int SHOPPING_ID, Integer I_ID, Vector ids, Vector quantities) {
    Cart cart = null;
    Connection con = null;
    try
    {
      con = getConnection();

      if (I_ID != null) {
        addItem(con, SHOPPING_ID, I_ID.intValue());
      }
      refreshCart(con, SHOPPING_ID, ids, quantities);
      addRandomItemToCartIfNecessary(con, SHOPPING_ID);
      resetCartTime(con, SHOPPING_ID);
      cart = getCart(con, SHOPPING_ID, 0.0D);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeConnection(con);
    }
    return cart;
  }

  private static void addItem(Connection con, int SHOPPING_ID, int I_ID)
  {
    PreparedStatement find_entry = null;
    ResultSet rs = null;
    try
    {
      find_entry = con
        .prepareStatement("SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?");

      find_entry.setInt(1, SHOPPING_ID);
      find_entry.setInt(2, I_ID);
      rs = find_entry.executeQuery();

      if (rs.next())
      {
        int currqty = rs.getInt("scl_qty");
        currqty++;
        PreparedStatement update_qty = con
          .prepareStatement("UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?");
        update_qty.setInt(1, currqty);
        update_qty.setInt(2, SHOPPING_ID);
        update_qty.setInt(3, I_ID);
        update_qty.executeUpdate();
        update_qty.close();
      }
      else
      {
        PreparedStatement put_line = con
          .prepareStatement("INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)");
        put_line.setInt(1, SHOPPING_ID);
        put_line.setInt(2, 1);
        put_line.setInt(3, I_ID);
        put_line.executeUpdate();
        put_line.close();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(find_entry);
    }
  }

  private static void refreshCart(Connection con, int SHOPPING_ID, Vector ids, Vector quantities) {
    PreparedStatement statement = null;
    try
    {
      for (int i = 0; i < ids.size(); i++) {
        String I_IDstr = (String)ids.elementAt(i);
        String QTYstr = (String)quantities.elementAt(i);
        int I_ID = Integer.parseInt(I_IDstr);
        int QTY = Integer.parseInt(QTYstr);

        if (QTY == 0) {
          statement = con
            .prepareStatement("DELETE FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?");
          statement.setInt(1, SHOPPING_ID);
          statement.setInt(2, I_ID);
          statement.executeUpdate();
        }
        else {
          statement = con
            .prepareStatement("UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?");
          statement.setInt(1, QTY);
          statement.setInt(2, SHOPPING_ID);
          statement.setInt(3, I_ID);
          statement.executeUpdate();
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeStmt(statement);
    }
  }

  private static void addRandomItemToCartIfNecessary(Connection con, int SHOPPING_ID)
  {
    int related_item = 0;
    PreparedStatement get_cart = null;
    ResultSet rs = null;
    try
    {
      get_cart = con
        .prepareStatement("SELECT COUNT(*) from shopping_cart_line where scl_sc_id = ?");
      get_cart.setInt(1, SHOPPING_ID);
      rs = get_cart.executeQuery();
      rs.next();
      if (rs.getInt(1) == 0)
      {
        int rand_id = Util.getRandomI_ID();
        related_item = getRelated1(rand_id, con);
        addItem(con, SHOPPING_ID, related_item);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Adding entry to shopping cart failed: shopping id = " + SHOPPING_ID + 
        " related_item = " + related_item);
    } finally {
      closeResultSet(rs);
      closeStmt(get_cart);
    }
  }

  private static void resetCartTime(Connection con, int SHOPPING_ID)
  {
    PreparedStatement statement = null;
    try {
      statement = con
        .prepareStatement("UPDATE shopping_cart SET sc_time = current_timestamp WHERE sc_id = ?");

      statement.setInt(1, SHOPPING_ID);
      statement.executeUpdate();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeStmt(statement);
    }
  }

  public static Cart getCart(int SHOPPING_ID, double c_discount) {
    Cart mycart = null;
    Connection con = null;
    try {
      con = getConnection();
      mycart = getCart(con, SHOPPING_ID, c_discount);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeConnection(con);
    }
    return mycart;
  }

  private static Cart getCart(Connection con, int SHOPPING_ID, double c_discount)
  {
    Cart mycart = null;
    PreparedStatement get_cart = null;
    ResultSet rs = null;
    try {
      get_cart = con.prepareStatement("SELECT * FROM shopping_cart_line, item WHERE scl_i_id = item.i_id AND scl_sc_id = ?");

      get_cart.setInt(1, SHOPPING_ID);
      rs = get_cart.executeQuery();
      mycart = new Cart(rs, c_discount);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(get_cart);
    }
    return mycart;
  }

  public static void refreshSession(int C_ID)
  {
    Connection con = null;
    PreparedStatement updateLogin = null;
    try
    {
      con = getConnection();
      updateLogin = con
        .prepareStatement("UPDATE customer SET c_login = CURRENT_TIMESTAMP, c_expiration = CURRENT_TIMESTAMP WHERE c_id = ?");

      updateLogin.setInt(1, C_ID);
      updateLogin.executeUpdate();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeStmt(updateLogin);
      closeConnection(con);
    }
  }

  public static Customer createNewCustomer(Customer cust)
  {
    Connection con = null;
    PreparedStatement insert_customer_row = null;
    PreparedStatement get_id = null;
    ResultSet rs = null;
    try
    {
      con = getConnection();

      cust.c_discount = ((int)(Math.random() * 51.0D));
      cust.c_balance = 0.0D;
      cust.c_ytd_pmt = 0.0D;

      cust.c_last_visit = new java.sql.Date(System.currentTimeMillis());
      cust.c_since = new java.sql.Date(System.currentTimeMillis());
      cust.c_login = new java.sql.Date(System.currentTimeMillis());
      cust.c_expiration = new java.sql.Date(System.currentTimeMillis() + 7200000L);

      insert_customer_row = con
        .prepareStatement(
        "INSERT into customer (c_id, c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES ( CUSTOMER_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
        1);
      insert_customer_row.setString(3, cust.c_fname);
      insert_customer_row.setString(4, cust.c_lname);
      insert_customer_row.setString(6, cust.c_phone);
      insert_customer_row.setString(7, cust.c_email);
      insert_customer_row.setDate(8, 
        new java.sql.Date(cust.c_since.getTime()));
      insert_customer_row.setDate(9, 
        new java.sql.Date(cust.c_last_visit.getTime()));
      insert_customer_row.setDate(10, new java.sql.Date(cust.c_login.getTime()));
      insert_customer_row.setDate(11, new java.sql.Date(cust.c_expiration.getTime()));
      insert_customer_row.setDouble(12, cust.c_discount);
      insert_customer_row.setDouble(13, cust.c_balance);
      insert_customer_row.setDouble(14, cust.c_ytd_pmt);
      insert_customer_row.setDate(15, 
        new java.sql.Date(cust.c_birthdate.getTime()));
      insert_customer_row.setString(16, cust.c_data);

      cust.addr_id = enterAddress(con, cust.addr_street1, 
        cust.addr_street2, cust.addr_city, cust.addr_state, 
        cust.addr_zip, cust.co_name);

      insert_customer_row.setString(1, cust.c_uname);
      insert_customer_row.setString(2, cust.c_passwd);
      insert_customer_row.setInt(5, cust.addr_id);
      insert_customer_row.executeUpdate();

      get_id = con.prepareStatement("SELECT CUSTOMER_SEQ.currval FROM customer");
      rs = get_id.executeQuery();

      if (rs.next()) {
        cust.c_id = rs.getInt(1);
      }
      cust.c_uname = Util.DigSyl(cust.c_id, 0);
      cust.c_passwd = cust.c_uname.toLowerCase();
      PreparedStatement updateUnameANDPasswd = con
        .prepareStatement("UPDATE customer SET c_uname = ?, c_passwd = ? WHERE c_id = ?");
      updateUnameANDPasswd.setString(1, cust.c_uname);
      updateUnameANDPasswd.setString(2, cust.c_passwd);
      updateUnameANDPasswd.setLong(3, cust.c_id);
      updateUnameANDPasswd.executeUpdate();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(insert_customer_row);
      closeConnection(con);
    }
    return cust;
  }

  public static BuyConfirmResult doBuyConfirm(int shopping_id, int customer_id, String cc_type, long cc_number, String cc_name, java.sql.Date cc_expiry, String shipping)
  {
    BuyConfirmResult result = new BuyConfirmResult();
    Connection con = null;
    try
    {
      con = getConnection();
      double c_discount = getCDiscount(con, customer_id);
      result.cart = getCart(con, shopping_id, c_discount);
      int ship_addr_id = getCAddr(con, customer_id);
      result.order_id = enterOrder(con, customer_id, result.cart, ship_addr_id, shipping, 
        c_discount);
      enterCCXact(con, result.order_id, cc_type, cc_number, cc_name, cc_expiry, 
        result.cart.SC_TOTAL, ship_addr_id);
      clearCart(con, shopping_id);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeConnection(con);
    }
    return result;
  }

  public static BuyConfirmResult doBuyConfirm(int shopping_id, int customer_id, String cc_type, long cc_number, String cc_name, java.sql.Date cc_expiry, String shipping, String street_1, String street_2, String city, String state, String zip, String country)
  {
    BuyConfirmResult result = new BuyConfirmResult();
    Connection con = null;
    try {
      con = getConnection();
      double c_discount = getCDiscount(con, customer_id);
      result.cart = getCart(con, shopping_id, c_discount);
      int ship_addr_id = enterAddress(con, street_1, street_2, city, state, zip, country);
      result.order_id = enterOrder(con, customer_id, result.cart, ship_addr_id, shipping, 
        c_discount);
      enterCCXact(con, result.order_id, cc_type, cc_number, cc_name, cc_expiry, 
        result.cart.SC_TOTAL, ship_addr_id);
      clearCart(con, shopping_id);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeConnection(con);
    }
    return result;
  }

  public static double getCDiscount(Connection con, int c_id)
  {
    double c_discount = 0.0D;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      statement = con
        .prepareStatement("SELECT c_discount FROM customer WHERE customer.c_id = ?");

      statement.setInt(1, c_id);
      rs = statement.executeQuery();

      if (rs.next()) c_discount = rs.getDouble(1); 
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
    }
    return c_discount;
  }

  public static int getCAddrID(Connection con, int c_id)
  {
    int c_addr_id = 0;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      statement = con
        .prepareStatement("SELECT c_addr_id FROM customer WHERE customer.c_id = ?");

      statement.setInt(1, c_id);
      rs = statement.executeQuery();

      if (rs.next()) c_addr_id = rs.getInt(1); 
    }
    catch (Exception ex) { ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
    }
    return c_addr_id;
  }

  public static int getCAddr(Connection con, int c_id) {
    int c_addr_id = 0;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      statement = con.prepareStatement("SELECT c_addr_id FROM customer WHERE customer.c_id = ?");

      statement.setInt(1, c_id);
      rs = statement.executeQuery();

      if (rs.next()) c_addr_id = rs.getInt(1); 
    }
    catch (Exception ex) { ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(statement);
    }
    return c_addr_id;
  }

  public static void enterCCXact(Connection con, int o_id, String cc_type, long cc_number, String cc_name, java.sql.Date cc_expiry, double total, int ship_addr_id)
  {
    PreparedStatement statement = null;

    if (cc_type.length() > 10)
      cc_type = cc_type.substring(0, 10);
    if (cc_name.length() > 30) {
      cc_name = cc_name.substring(0, 30);
    }

    try
    {
      int co_id = 0;

      String sql = "SELECT co_id FROM address, country WHERE addr_id = ? AND addr_co_id = co_id";
      PreparedStatement statement2 = con.prepareStatement(sql);
      statement2.setInt(1, ship_addr_id);
      ResultSet rs = statement2.executeQuery();
      if (rs.next()) {
        co_id = rs.getInt(1);
      }

      rs.close();

      statement = con
        .prepareStatement("INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)");

      statement.setInt(1, o_id);
      statement.setString(2, cc_type);
      statement.setLong(3, cc_number);
      statement.setString(4, cc_name);
      statement.setDate(5, cc_expiry);
      statement.setDouble(6, total);
      statement.setInt(7, ship_addr_id);
      statement.executeUpdate();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeStmt(statement);
    }
  }

  public static void clearCart(Connection con, int shopping_id)
  {
    PreparedStatement statement = null;
    try
    {
      statement = con.prepareStatement("DELETE FROM shopping_cart_line WHERE scl_sc_id = ?");

      statement.setInt(1, shopping_id);
      statement.executeUpdate();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeStmt(statement);
    }
  }

  public static int enterAddress(Connection con, String street1, String street2, String city, String state, String zip, String country)
    throws Exception
  {
    int addr_id = 0;
    PreparedStatement get_co_id = null;
    PreparedStatement match_address = null;
    PreparedStatement insert_address_row = null;
    ResultSet rs = null;
    ResultSet rs2 = null;

    label372: 
    try { get_co_id = con
        .prepareStatement("SELECT co_id FROM country WHERE co_name = ?");
      get_co_id.setString(1, country);
      rs = get_co_id.executeQuery();
      int addr_co_id = 0;
      if (rs.next())
        addr_co_id = rs.getInt("co_id");
      rs.close();

      match_address = con.prepareStatement("SELECT addr_id FROM address WHERE addr_street1 = ? AND addr_street2 = ? AND addr_city = ? AND addr_state = ? AND addr_zip = ? AND addr_co_id = ?");

      match_address.setString(1, street1);
      match_address.setString(2, street2);
      match_address.setString(3, city);
      match_address.setString(4, state);
      match_address.setString(5, zip);
      match_address.setInt(6, addr_co_id);
      rs = match_address.executeQuery();
      if (!rs.next()) {
        PreparedStatement get_id = null;
        insert_address_row = con
          .prepareStatement(
          "INSERT into address (addr_id, addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES (ADDRESS_SEQ.nextval, ?, ?, ?, ?, ?, ?)", 
          1);
        insert_address_row.setString(1, street1);
        insert_address_row.setString(2, street2);
        insert_address_row.setString(3, city);
        insert_address_row.setString(4, state);
        insert_address_row.setString(5, zip);
        insert_address_row.setInt(6, addr_co_id);
        insert_address_row.executeUpdate();
        get_id = con.prepareStatement("SELECT ADDRESS_SEQ.currval FROM address");
        rs2 = get_id.executeQuery();

        if (rs2.next()) {
          addr_id = rs2.getInt(1); break label372;
        }
      } else {
        addr_id = rs.getInt("addr_id");
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeResultSet(rs2);
      closeStmt(get_co_id);
      closeStmt(match_address);
      closeStmt(insert_address_row);
    }
    return addr_id;
  }

  public static int enterOrder(Connection con, int customer_id, Cart cart, int ship_addr_id, String shipping, double c_discount)
  {
    int o_id = 0;
    PreparedStatement get_id = null;
    PreparedStatement insert_row = null;
    ResultSet rs = null;
    try
    {
      GregorianCalendar calendar = new GregorianCalendar();
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      String date1 = formatter.format(calendar.getTime());

      insert_row = con
        .prepareStatement(
        "INSERT into orders (o_id, o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) VALUES ( ORDERS_SEQ.nextval, ?,  CURRENT_DATE, ?, 8.25, ?, ?,  CURRENT_DATE+?, ?, ?, 'Pending')", 
        1);
      insert_row.setInt(1, customer_id);
      insert_row.setDouble(2, cart.SC_SUB_TOTAL);
      insert_row.setDouble(3, cart.SC_TOTAL);
      insert_row.setString(4, shipping);
      insert_row.setInt(5, Util.getRandom(7));
      insert_row.setInt(6, getCAddrID(con, customer_id));
      insert_row.setInt(7, ship_addr_id);
      insert_row.executeUpdate();
      get_id = con.prepareStatement("SELECT ORDERS_SEQ.currval FROM orders");
      rs = get_id.executeQuery();

      rs = insert_row.getGeneratedKeys();
      if (rs.next())
        o_id = rs.getInt(1);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(insert_row);
    }

    Enumeration e = cart.lines
      .elements();
    int counter = 0;
    while (e.hasMoreElements())
    {
      CartLine cart_line = (CartLine)e.nextElement();
      addOrderLine(con, counter, o_id, cart_line.scl_i_id, 
        cart_line.scl_qty, c_discount, 
        Util.getRandomString(20, 100));
      counter++;

      int stock = getStock(con, cart_line.scl_i_id);
      if (stock - cart_line.scl_qty < 10)
        setStock(con, cart_line.scl_i_id, stock - cart_line.scl_qty + 
          21);
      else {
        setStock(con, cart_line.scl_i_id, stock - cart_line.scl_qty);
      }
    }
    return o_id;
  }

  public static void addOrderLine(Connection con, int ol_id, int ol_o_id, int ol_i_id, int ol_qty, double ol_discount, String ol_comment)
  {
    int success = 0;
    PreparedStatement insert_row = null;
    try {
      insert_row = con
        .prepareStatement("INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES (?, ?, ?, ?, ?, ?)");

      insert_row.setInt(1, ol_id);
      insert_row.setInt(2, ol_o_id);
      insert_row.setInt(3, ol_i_id);
      insert_row.setInt(4, ol_qty);
      insert_row.setDouble(5, ol_discount);
      insert_row.setString(6, ol_comment);
      insert_row.executeUpdate();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeStmt(insert_row);
    }
  }

  public static int getStock(Connection con, int i_id) {
    int stock = 0;
    PreparedStatement get_stock = null;
    ResultSet rs = null;
    try {
      get_stock = con.prepareStatement("SELECT i_stock FROM item WHERE i_id = ?");

      get_stock.setInt(1, i_id);
      rs = get_stock.executeQuery();

      rs.next();
      stock = rs.getInt("i_stock");
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(get_stock);
    }
    return stock;
  }

  public static void setStock(Connection con, int i_id, int new_stock) {
    PreparedStatement update_row = null;
    try {
      update_row = con.prepareStatement("UPDATE item SET i_stock = ? WHERE i_id = ?");
      update_row.setInt(1, new_stock);
      update_row.setInt(2, i_id);
      update_row.executeUpdate();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeStmt(update_row);
    }
  }

  public static void verifyDBConsistency() {
    Connection con = null;
    PreparedStatement get_ids = null;
    ResultSet rs = null;
    try {
      con = getConnection();

      int id_expected = 1;

      get_ids = con.prepareStatement("SELECT c_id FROM customer");
      rs = get_ids.executeQuery();
      while (rs.next()) {
        int this_id = rs.getInt("c_id");
        while (this_id != id_expected) {
          System.out.println("Missing C_ID " + id_expected);
          id_expected++;
        }
        id_expected++;
      }

      id_expected = 1;

      get_ids = con.prepareStatement("SELECT i_id FROM item");
      rs = get_ids.executeQuery();
      while (rs.next()) {
        int this_id = rs.getInt("i_id");
        while (this_id != id_expected) {
          System.out.println("Missing I_ID " + id_expected);
          id_expected++;
        }
        id_expected++;
      }

      id_expected = 1;

      get_ids = con.prepareStatement("SELECT addr_id FROM address");
      rs = get_ids.executeQuery();
      while (rs.next()) {
        int this_id = rs.getInt("addr_id");

        while (this_id != id_expected) {
          System.out.println("Missing ADDR_ID " + id_expected);
          id_expected++;
        }
        id_expected++;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    } finally {
      closeResultSet(rs);
      closeStmt(get_ids);
      closeConnection(con);
    }
  }
}