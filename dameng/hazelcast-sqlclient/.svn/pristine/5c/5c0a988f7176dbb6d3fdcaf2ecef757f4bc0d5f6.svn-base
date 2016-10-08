package com.hazelcast.sqlclient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hazelcast.sqlclient.executor.Executor;
import com.hazelcast.sqlclient.executor.HazelcastExecutor;
import com.hazelcast.sqlclient.jdbc.IMDGResultSet;
import com.hazelcast.sqlclient.load.Loader;

public class MyTest {
	public static void main(String[] args) throws Exception {
		MyTest.testHazelcast();
		//MyTest.testMysql();
	}
	public static void testHazelcast() throws SQLException{
		Loader loader = new Loader();
		loader.configRelationalDatabase("com.mysql.jdbc.Driver","jdbc:mysql://localhost/test","root","shuping");
		loader.loadData();		
		Executor myExecutor = new HazelcastExecutor();		
		TempSimpleSelect tempSimpleSQL = new TempSimpleSelect();
		//TempSimpleUpdate tempSimpleSQL = new TempSimpleUpdate();
		//TempComplexSQL tempComplexSQL = new TempComplexSQL();
		//TempExample tempExample = new TempExample();
		long interval = System.currentTimeMillis();
		System.out.println();
		for(int j = 0; j <= 0; j++){
			for(int i = 4; i <= 4; i ++){
				IMDGResultSet resultSet = new IMDGResultSet();
				System.out.println("execute the " + i + " SQL: " + tempSimpleSQL.getSQL(i));
				switch(myExecutor.judgeSQLKind(tempSimpleSQL.getSQL(i))){
				case INSERT: myExecutor.executeInsert(tempSimpleSQL.getSQL(i), resultSet); break;
				case DELETE: myExecutor.executeDelete(tempSimpleSQL.getSQL(i)); break;
				case UPDATE: myExecutor.executeUpdate(tempSimpleSQL.getSQL(i)); break;
				case SELECT: myExecutor.executeSelect(tempSimpleSQL.getSQL(i), resultSet); break;
				default:
					break;
				}
			}
		}
		interval = System.currentTimeMillis() - interval;
		System.out.println(" the interval is : " + interval);
	}
	public static void testMysql(){
		long interval = System.currentTimeMillis();
		System.out.println();
		String str = "";
		for(int i = 0 ;i < 1000; i ++){
			List<String> list = new ArrayList<String>();
			list.add(new String("a"));
			str+=list.get(0);
		}
		System.out.println(str);
		interval = System.currentTimeMillis() - interval;
		System.out.println(" the interval is : " + interval);
	}
}