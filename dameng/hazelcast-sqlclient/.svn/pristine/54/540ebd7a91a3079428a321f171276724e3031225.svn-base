package com.hazelcast.sqlclient;

import java.util.HashMap;
import java.util.Set;

public class HazelcastDatabaseMetaData {
	private static HashMap<String, TableMetaData> tableMetaDataList = new HashMap<String, TableMetaData>();
	
	public TableMetaData getTableMetaData(String tableName){
		return tableMetaDataList.get(tableName);
	}
	
	public void setTableMetaData(String tableName, TableMetaData tableMetaData){
		tableMetaDataList.put(tableName, tableMetaData);
	}
	
	public Set<String> getTableNames(){
		return tableMetaDataList.keySet();
	}
	
	public boolean judgeTableAttribute(String tableAttribute){
		int index = tableAttribute.indexOf(".");
		if(index == -1){
			for(String tableName : tableMetaDataList.keySet()){
				if(tableMetaDataList.get(tableName).judgeTableAttribute(tableAttribute)){
					return true;
				}
			}
		}
		else{
			String tableName = tableAttribute.substring(0,index-1);
			String attributeName = tableAttribute.substring(index+1);
			return tableMetaDataList.get(tableName).judgeTableAttribute(attributeName);
		}
		return false;
	}
	
	public String getTableAttribute(String str){
		int index = str.indexOf(".");
		if(index == -1){
			for(String tableName : tableMetaDataList.keySet()){
				if(tableMetaDataList.get(tableName).judgeTableAttribute(str)){
					return tableName+"."+str.toLowerCase();
				}
			}
			return null;
		}
		else{
			String tableName = str.substring(0, index);
			return tableMetaDataList.get(tableName) == null ? null : str;
		}
	}
}
