package com.hazelcast.persistance;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SQLParser {

	private static final String SPLIT_LABEL = "\\$#@";
	private static final String PREFIX = "$cglib_prop_";
	private static final int PREFIX_LEN = PREFIX.length();
	
	public static String getInsertSQLFromObject(Object object, String tableName){	
		String insertSQL = "insert into " + tableName + "(";	
		String cols = new String();
		String vals = new String();
		try { 
			for(Field field : object.getClass().getDeclaredFields()){
				field.setAccessible(true);
				String name = field.getName();
				if(name.startsWith(PREFIX))
					name = name.substring(PREFIX_LEN);
				if(field.get(object) != null)
					vals += getStringFromObject(field.get(object)) + ",";	
				else 
					vals += "null,";
				cols += name + ",";
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}		
		vals = vals.substring(0, vals.length()-1) + ")";
		cols = cols.substring(0, cols.length()-1) + ")";
		insertSQL += cols + " values(" + vals;
		return insertSQL;
	}
	
	public static String getDeleteSQLFromObject(String key, Object object, String tableName){	
		String [] keys = key.split(SPLIT_LABEL);
		if(keys == null || keys.length == 1){
			return "delete from " + tableName + " where " + key;
		} else {
			String ret = "delete from " + tableName + " where " + keys[0];
			for(int i = 1; i != keys.length; ++i){
				ret += " and " + keys[i];
			}
			return ret;
		}
	}
	
	public static String getDeleteSQLFromObject(String key, String table){	
		String [] keys = key.split(SPLIT_LABEL);
		if(keys == null || keys.length == 1){
			return "delete from " + table + " where " + key;
		} else {
			String ret = "delete from " + table + " where " + keys[0];
			for(int i = 1; i != keys.length; ++i){
				ret += " and " + keys[i];
			}
			return ret;
		}
	}
	
	public static String getQuerySQLFromObject(String key, Object object, String tableName){	
		String [] keys = key.split(SPLIT_LABEL);
		if(keys == null || keys.length == 1){
			return "select * from " + tableName + " where " + key;
		} else {
			String ret = "select * from " + tableName + " where " + keys[0];
			for(int i = 1; i != keys.length; ++i){
				ret += " and " + keys[i];
			}
			return ret;
		}
	}
	
	public static String getUpdateSQLFromObject(String key, Object object, String tableName){	
		String [] keys = key.split(SPLIT_LABEL);
		ArrayList<String> cols = new ArrayList<String>(2);
		String suffix = null;
		if(keys == null || keys.length == 1){
			suffix = key;
			cols.add(key.substring(0, key.indexOf("=")));
		} else {
			suffix = keys[0];
			cols.add(keys[0].substring(0, keys[0].indexOf("=")));
			for(int i = 1; i != keys.length; ++i){
				suffix += " and " + keys[i];
				cols.add(keys[i].substring(0, keys[i].indexOf("=")));
			}
		}
		
//		System.out.println(suffix);
//		for(int i = 0; i != cols.size(); ++i){
//			System.out.println(cols.get(i));
//		}
		String ret = "update " + tableName + " set ";
		
		try {
			for(Field field : object.getClass().getDeclaredFields()){
				field.setAccessible(true);
				String name = field.getName();
				if(name.startsWith(PREFIX))
					name = name.substring(PREFIX_LEN);
				if(!cols.contains(name) && field.get(object)!=null){
					ret += name + "=" + getStringFromObject(field.get(object)) + ",";	
				}
//				if(!cols.contains(field.getName())){
//					if(field.getName().startsWith(PREFIX))
//						ret += field.getName().substring(PREFIX_LEN) + "=" + getStringFromObject(field.get(object)) + ",";		
//					else
//						ret += field.getName() + "=" + getStringFromObject(field.get(object)) + ",";	
//				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}		
		ret = ret.substring(0, ret.length()-1);
		ret += " where " + suffix;
		
		return ret;
	}
	
	private static String getStringFromObject(Object object){
		String classTypeName = object.getClass().getName();
		if(classTypeName.equals("java.lang.String")){
			return "'" + object.toString() + "'";
		} else if(classTypeName.equals("java.sql.Date")){
			return "str_to_date('"+object.toString()+"','%Y-%m-%d')";
		} else if(classTypeName.equals("java.sql.Timestamp")){
			java.util.Date date = (java.sql.Timestamp)object;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return "str_to_date('"+format.format(date)+"','%Y-%m-%d %T')";
		}
		
		return object.toString();
	}
	
}
