package com.hazelcast.sqlclient.load;

import java.lang.reflect.Field;

public class PersisterExample {
	public String getInsertSQLFromObject(Object object){	
		String insertSQL = "insert into " + object.getClass().getName() + " values (";	
		try {
			for(Field field : object.getClass().getDeclaredFields()){
				field.setAccessible(true);
				insertSQL += this.getStringFromObject(field.get(object)) + ",";				
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}		
		insertSQL = insertSQL.substring(0, insertSQL.length()-1) + ")";
		return insertSQL;
	}	
	private String getStringFromObject(Object object){
		String classTypeName = object.getClass().getName();
		if(classTypeName.equals("java.lang.String")){
			return "'" + object.toString() + "'";
		}
		if(classTypeName.equals("java.sql.Date")){
			return "'" + object.toString() + "'";
		}
		if(classTypeName.equals("java.sql.Timestamp")){
			return "'" + object.toString() + "'";
		}
		return object.toString();
	}

	public static void main(String[] args) {
		TestClass testClassObject = new TestClass(0, "shuping", 24, new java.sql.Date(10000));
		PersisterExample persisterExample = new PersisterExample();
		String insertSQL = persisterExample.getInsertSQLFromObject(testClassObject);
		System.out.println(insertSQL);
	}
}

class TestClass{
	int id;
	String name;
	int age;
	java.sql.Date date;
	public TestClass(int id, String name, int age, java.sql.Date date){
		this.id = id;
		this.name = name;
		this.age = age;
		this.date = date;
	}
}