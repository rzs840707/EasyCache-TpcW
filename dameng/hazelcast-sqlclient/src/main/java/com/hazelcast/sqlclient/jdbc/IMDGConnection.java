package com.hazelcast.sqlclient.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import com.hazelcast.sqlclient.executor.Executor;
import com.hazelcast.sqlclient.executor.HazelcastExecutor;
import com.hazelcast.sqlclient.type.SqlKind;

public class IMDGConnection implements Connection {
	private Connection connection = null;
// for store statement reference
//	private ArrayList<IMDGStatement> array_st = new ArrayList<IMDGStatement>();
//	private ArrayList<IMDGPreparedStatement> array_pst = new ArrayList<IMDGPreparedStatement>();
	private DatabaseMetaData dbmd = null;
	private boolean closed = false;
	@SuppressWarnings("unused")
	private boolean autocommit = true;
	private boolean isReadOnly = false;
	// to check cached sql 
	private Executor hzExecutor = new HazelcastExecutor();

	private void CheckClosed() throws SQLException {
		if (closed) {
			throw new SQLException("Connection closed");
		}
	}
	
	
	private boolean supportedCheck(String sql) throws SQLException {
		SqlKind kind = hzExecutor.judgeSQLKind(sql);
		switch (kind) {
		case SELECT:
			return hzExecutor.supportedSelect(sql);
		case INSERT:
			return hzExecutor.supportedInsert(sql);
		case DELETE:
			return hzExecutor.supportedDelete(sql);
		case UPDATE:
			return hzExecutor.supportedUpdate(sql);
		default:
			throw new SQLException("Unknow SQL Type");
		}
	}
	
	//wang wei, jiang yong 2014-10-14
	private boolean supportedCheck(String sql, SqlKind kind) throws SQLException {
		switch (kind) {
		case SELECT:
			return hzExecutor.supportedSelect(sql);
		case INSERT:
			return hzExecutor.supportedInsert(sql);
		case DELETE:
			return hzExecutor.supportedDelete(sql);
		case UPDATE:
			return hzExecutor.supportedUpdate(sql);
		default:
			throw new SQLException("Unknow SQL Type");
		}
	}
	
	public IMDGConnection(Connection c) {
		connection = c;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		CheckClosed();
		return connection.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		CheckClosed();
		return connection.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		CheckClosed();
		return new IMDGStatement(this, connection.createStatement());
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		CheckClosed();
		sql = sql.toLowerCase();
		SqlKind kind = hzExecutor.judgeSQLKind(sql);
		if(supportedCheck(sql, kind)) {
			return new IMDGPreparedStatement(sql, this, null, kind, Statement.NO_GENERATED_KEYS);
		} else {
			//TODO: preparestatement?
			return new IMDGPreparedStatement(sql, this, connection.createStatement(), kind, Statement.NO_GENERATED_KEYS);
		}
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return null;
	}

	public String nativeSQL(String sql) throws SQLException {
		// turn sql statements to the natives
		return connection.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// set whether auto commit or not. true default
		// for autocommit false situation:
		// sql statements before commit() are executed in one transaction
		// need to be rollbacked together
		autocommit = autoCommit;
		connection.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		// return autocommit;
		return connection.getAutoCommit();
	}

	public void commit() throws SQLException {
		// invoke this method under the circumstance of autocommit is false
		// connection.commit();
	}

	public void rollback() throws SQLException {
		// connection.rollback();
	}

	public void close() throws SQLException {
		closed = true;
//		for (IMDGStatement st : array_st) {
//			if (!st.closed) {
//				st.close();
//			}
//		}
//		for (IMDGPreparedStatement pst : array_pst) {
//			pst.close();
//		}
//		array_st.clear();
//		array_pst.clear();
		connection.close();
		connection = null;
	}

	public boolean isClosed() throws SQLException {
		return closed;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		if (dbmd == null)
			return dbmd = connection.getMetaData();
		else
			return dbmd;
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
//		isReadOnly = readOnly;
//		for (IMDGPreparedStatement pst : array_pst) {
//			pst.setReadOnly(readOnly);
//		}
//		for (IMDGStatement st : array_st) {
//			st.setReadOnly(readOnly);
//		}
//		connection.setReadOnly(readOnly);
		throw new SQLException("No Supported");
	}

	public boolean isReadOnly() throws SQLException {
		return isReadOnly;
		// return connection.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		// transaction isolate level
		connection.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		IMDGStatement st = new IMDGStatement(this, connection.createStatement());
		st.setResultSetType(resultSetType);
		st.setResultSetConcurrency(resultSetConcurrency);
//		array_st.add(st);
		return st;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		//toLowerCase?
		//sql = sql.toLowerCase();
		IMDGPreparedStatement pst = (IMDGPreparedStatement)this.prepareStatement(sql);
		pst.setResultSetType(resultSetType);
		pst.setResultSetConcurrency(resultSetConcurrency);
//		array_pst.add(pst);
		return pst;
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		// an empty map default
		return connection.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		// no support yet
		connection.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		// no support yet
		return connection.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		// no support yet
		return connection.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// these arguments are useless
		return createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// these arguments are useless
		return prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return null;
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		CheckClosed();
		String temp = sql.toLowerCase();
		SqlKind kind = hzExecutor.judgeSQLKind(temp);
		if(supportedCheck(temp, kind)) {
			return new IMDGPreparedStatement(sql, this, null, kind, autoGeneratedKeys);
		} else {
//			return new IMDGPreparedStatement(sql, this, connection.prepareStatement(sql), kind, autoGeneratedKeys);
			return new IMDGPreparedStatement(sql, this, connection.createStatement(), kind, autoGeneratedKeys);
		}
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		throw new SQLException("No Implement");
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		throw new SQLException("No Implement");
	}

	public Clob createClob() throws SQLException {
		throw new SQLException("No Implement");
	}

	public Blob createBlob() throws SQLException {
		throw new SQLException("No Implement");
	}

	public NClob createNClob() throws SQLException {
		throw new SQLException("No Implement");
	}

	public SQLXML createSQLXML() throws SQLException {
		throw new SQLException("No Implement");
	}

	public boolean isValid(int timeout) throws SQLException {
		return !closed && connection.isValid(timeout);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		try {
			throw new SQLException("No Implement");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		try {
			throw new SQLException("No Implement");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getClientInfo(String name) throws SQLException {
		throw new SQLException("No Implement");
	}

	public Properties getClientInfo() throws SQLException {
		throw new SQLException("No Implement");
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		throw new SQLException("No Implement");
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		throw new SQLException("No Implement");
	}
}
