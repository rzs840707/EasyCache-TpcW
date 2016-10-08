package com.hazelcast.sqlclient.executor;

public class UpdateParserResult {
	private String tableName;
	private QueryPredicate queryPredicate;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public QueryPredicate getQueryPredicate() {
		return queryPredicate;
	}
	public void setQueryPredicate(QueryPredicate queryPredicate) {
		this.queryPredicate = queryPredicate;
	}
}
