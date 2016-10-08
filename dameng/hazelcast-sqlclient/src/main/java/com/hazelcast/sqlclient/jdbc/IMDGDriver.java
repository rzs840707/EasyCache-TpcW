package com.hazelcast.sqlclient.jdbc;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.hazelcast.sqlclient.load.ConfigParser;
import com.hazelcast.sqlclient.load.Loader;

public class IMDGDriver implements java.sql.Driver {
	private static boolean loadflag = false;
	private static InitialContext ctx = null;
	private static String dsName = ConfigParser.getInstance().configParseGetDsName();
	
	static {
		try {
			java.sql.DriverManager.registerDriver(new IMDGDriver());
		} catch (SQLException E) {
			throw new RuntimeException("Can't register driver!");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public IMDGDriver() throws SQLException, NamingException {
		if(ctx == null) {
			ctx = new InitialContext();
		}
	} 
	
	public Connection connect(String url, Properties info) throws SQLException {
		if(!loadflag) {
			synchronized (IMDGDriver.class) {
				if(!loadflag) {
					Loader loader = new Loader();
					long start = System.currentTimeMillis();
					loader.loadData();
					long end = System.currentTimeMillis();
					DecimalFormat fnum = new DecimalFormat("##0.00");    
					String time=fnum.format((float)(end-start)/1000);       
					System.out.println("data loaded done in " + time + " s");
				}
				loadflag = true;
			}
		}
		DataSource ds = null;
		try {
			ds = (DataSource) ctx.lookup(dsName);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return new IMDGConnection(ds.getConnection());
	}

	public boolean acceptsURL(String url) throws SQLException {
		DataSource ds = null;
		try {
			ds = (DataSource) ctx.lookup(dsName);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		if(ds != null) {
			if(ds.getConnection() != null) return true;
			else  return false;
		} else {
			return false;
		}
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return null;
	}

	public int getMajorVersion() {
		return 1;
	}

	public int getMinorVersion() {
		return 1;
	}

	public boolean jdbcCompliant() {
		return true;
	}
	
}