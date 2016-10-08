package com.hazelcast.sqlclient.executor;

import java.sql.SQLException;
import java.util.List;

import com.hazelcast.sqlclient.jdbc.IMDGResultSet;
import com.hazelcast.sqlclient.type.SqlKind;

public abstract class Executor {
	public Executor() {}
	public abstract SqlKind judgeSQLKind(String sql);
	
	public abstract int executeInsert(String insertSentence, IMDGResultSet hrs) throws SQLException;
	public abstract boolean whetherInsertCached(String key) throws SQLException;
	public abstract int executeInsertPre(String key, String insertSentence, IMDGResultSet hrs) throws SQLException;
	public abstract int executeInsertCache(String key, List<String> attributeNameList, List<String> attributeValueList, IMDGResultSet hrs) throws SQLException;
	
	public abstract int executeDelete(String deleteSentence) throws SQLException;
	public abstract boolean whetherDeleteCached(String key) throws SQLException;
	public abstract int executeDeletePre(String key, String deleteSentence) throws SQLException;
	public abstract int executeDeleteCache(String key, List<String> attributeNameList, List<String> attributeValueList) throws SQLException;
	
	public abstract int executeUpdate(String updateSentence) throws SQLException;
	public abstract boolean whetherUpdateCached(String key) throws SQLException;
	public abstract int executeUpdatePre(String key, String updateSentence) throws SQLException;
	public abstract int executeUpdateCache(String key, List<String> setAttributeNameList, List<String> setAttributeValueList, List<String> whereAttributeNameList, List<String> whereAttributeValueList) throws SQLException;
	
	public abstract int executeSelect(String selectSentence, IMDGResultSet hrs) throws SQLException;
	public abstract boolean whetherSelectCached(String key) throws SQLException;
	public abstract int executeSelectPre(String key, String selectSentence, IMDGResultSet hrs) throws SQLException;
	public abstract int executeSelectCache(String key, List<String> attributeNameList, List<String> attributeValueList, IMDGResultSet hrs) throws SQLException;
	
	public abstract boolean supportedSelect(String selectSentence) throws SQLException;
	public abstract boolean supportedInsert(String updateSentence) throws SQLException;
	public abstract boolean supportedUpdate(String updateSentence) throws SQLException;
	public abstract boolean supportedDelete(String deleteSentence) throws SQLException;
	public abstract boolean supportedQueryResultCache(String sql) throws SQLException;
	public abstract String getCacheSwitch();
	
}