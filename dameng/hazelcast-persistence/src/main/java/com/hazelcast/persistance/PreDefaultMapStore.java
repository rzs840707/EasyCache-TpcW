package com.hazelcast.persistance;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PreDefaultMapStore extends DefaultMapStore{
	
	private HashMap<String, PreSQLParser> psps  = null;
	
	private Object lock = new Object();
	
	public PreDefaultMapStore(){
		psps = new HashMap<String, PreSQLParser>(15);
	}
	 
    public void store(Object key, Object value, String tableName) {
    	PreSQLParser psp = psps.get(tableName);
    	if(psp == null){
    		synchronized(lock){
    			psp = psps.get(tableName);
    			if(psp == null){
    				psp = new PreSQLParser();
            		psps.put(tableName, psp);
    			}
    		}
    	}
    	
    	if(value != null){
    		String sql = psp.getQuerySQLFromObject((String)key, value, tableName);
//        	System.out.println(sql);
        	Connection con = ConnectionPool.getConnection();
        	PreparedStatement stmt = null;
        	PreparedStatement stmt1 = null;
        	try {
        		stmt = con.prepareStatement(sql);
        		
        		String k = (String)key;
        		String [] keys = k.split(PreSQLParser.SPLIT_LABEL);
    			if(keys == null || keys.length == 1){
    				stmt.setInt(1, Integer.valueOf(k.substring(k.indexOf("=")+1)));
//    				stmt.setObject(1, k.substring(k.indexOf("=")+1));
//    				System.out.println("Insert Col Value:" + k.substring(k.indexOf("=")+1));
    			} else {
    				ArrayList<String> cols = psp.getQueryCols();
//    				stmt.setObject(cols.indexOf(keys[0].substring(0, keys[0].indexOf("=")))+1, 
//    						keys[0].substring(keys[0].indexOf("=")+1));
    				stmt.setInt(cols.indexOf(keys[0].substring(0, keys[0].indexOf("=")))+1, 
    						Integer.valueOf(keys[0].substring(keys[0].indexOf("=")+1)));
    				for(int i = 1; i != keys.length; ++i){
//    					stmt.setObject(cols.indexOf(keys[i].substring(0, keys[i].indexOf("=")))+1, 
//    							keys[i].substring(keys[i].indexOf("=")+1));
    					stmt.setInt(cols.indexOf(keys[i].substring(0, keys[i].indexOf("=")))+1, 
    							Integer.valueOf(keys[i].substring(keys[i].indexOf("=")+1)));
    				}
    			}
    			
    			ResultSet rs = stmt.executeQuery();
    			if(rs != null && !rs.next()){
    				sql = psp.getInsertSQLFromObject(value, tableName);
//    				System.out.println(sql);
    				stmt1 = con.prepareStatement(sql);
    				ArrayList<String> cols = psp.getInsertCols();
//    				int index;
//    				Object object;
    				for(Field field : value.getClass().getDeclaredFields()){
    					field.setAccessible(true);
    					String name = field.getName();
    					if(name.startsWith(PreSQLParser.PREFIX))
    						name = name.substring(PreSQLParser.PREFIX_LEN);
    					if(field.get(value) != null){
//    						index = cols.indexOf(name)+1;
//    						object = null;
    						stmt1.setObject(cols.indexOf(name)+1, field.get(value));	
    					} else {
//    						index = cols.indexOf(name)+1;
//    						object = null;
    						stmt1.setObject(cols.indexOf(name)+1, null);
    					}
//    					System.out.println(sql + "[" + index + ":" + object + "]");
    				}
    				
    				stmt1.executeUpdate();
//    				insert(key, value, tableName, con);
    			} else {
    				sql = psp.getUpdateSQLFromObject((String)key, value, tableName);
    				stmt1 = con.prepareStatement(sql);
    				
    				ArrayList<String> cols = psp.getUpdateCols();
//    				int index;
//    				Object object;
    				for(Field field : value.getClass().getDeclaredFields()){
    					field.setAccessible(true);
    					String name = field.getName();
    					if(name.startsWith(PreSQLParser.PREFIX))
    						name = name.substring(PreSQLParser.PREFIX_LEN);
    					if(field.get(value) != null){
//    						index = cols.indexOf(name)+1;
//    						object = field.get(value);
    						stmt1.setObject(cols.indexOf(name)+1, field.get(value));	
    					} else {
//    						index = cols.indexOf(name)+1;
//    						object = null;
    						stmt1.setObject(cols.indexOf(name)+1, null);
    					}
//    					System.out.println(sql + "[" + index + ":" + object + "]");
    				}
    				
    				stmt1.executeUpdate();
//    				update(key, value, tableName, con);
    			}
    		} catch (SQLException e) {
    			long threadId = Thread.currentThread().getId();
				System.out.println("==============================[" + threadId + "] Debug Info ==============================");
				System.out.println("[" + threadId +"] SQL:" + sql);
				for(Field field : value.getClass().getDeclaredFields()){
					field.setAccessible(true);
					String name = field.getName();
					try {
						System.out.println("[" + threadId + "] " + name + " : " + field.get(value));
					} catch (IllegalArgumentException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					}
				}
				System.out.println("================================" + threadId + "====================================");
    			e.printStackTrace();
    		} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} finally {
    			closeStmt(stmt);
    			closeStmt(stmt1);
    			closeConnection(con);
    		}
    	}
    }
    
    public void insert(Object key, Object value, String tableName, Connection con) throws SQLException, IllegalArgumentException, IllegalAccessException{
    	PreSQLParser psp = psps.get(tableName);
    	if(psp == null){
    		synchronized(lock){
    			psp = psps.get(tableName);
    			if(psp == null){
    				psp = new PreSQLParser();
            		psps.put(tableName, psp);
    			}
    		}
    	}
    	
    	String sql = psp.getInsertSQLFromObject(value, tableName);
    	PreparedStatement stmt = null;
//		System.out.println(sql);
		stmt = con.prepareStatement(sql);
		ArrayList<String> cols = psp.getInsertCols();
		for(Field field : value.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String name = field.getName();
			if(name.startsWith(PreSQLParser.PREFIX))
				name = name.substring(PreSQLParser.PREFIX_LEN);
			if(field.get(value) != null){
				stmt.setObject(cols.indexOf(name)+1, field.get(value));	
			} else {
				stmt.setObject(cols.indexOf(name)+1, null);
			}
		}
		
		stmt.executeUpdate();
		closeStmt(stmt);
    }
    
    public void update(Object key, Object value, String tableName, Connection con) throws SQLException, IllegalArgumentException, IllegalAccessException{
    	PreSQLParser psp = psps.get(tableName);
    	if(psp == null){
    		synchronized(lock){
    			psp = psps.get(tableName);
    			if(psp == null){
    				psp = new PreSQLParser();
            		psps.put(tableName, psp);
    			}
    		}
    	}
    	String sql = psp.getUpdateSQLFromObject((String)key, value, tableName);
//		System.out.println(sql);
		PreparedStatement stmt = con.prepareStatement(sql);
		
		ArrayList<String> cols = psp.getUpdateCols();
		for(Field field : value.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String name = field.getName();
			if(name.startsWith(PreSQLParser.PREFIX))
				name = name.substring(PreSQLParser.PREFIX_LEN);
			if(field.get(value) != null){
				stmt.setObject(cols.indexOf(name)+1, field.get(value));	
			} else {
				stmt.setObject(cols.indexOf(name)+1, null);
			}
		}
		
		stmt.executeUpdate();
		closeStmt(stmt);
    }

    public void storeAll(Map map, String tableName) {
    	// yanshi.wys
    	// System.out.println("storeALL()#\tthreadId: " + Thread.currentThread().getId() + "\tthreadName: " + Thread.currentThread().getName());
    	
        Set<Map.Entry<Object, Object>> entrys = map.entrySet();
        for(Map.Entry<Object, Object> entry : entrys){
        	// yanshi.wys
        	// System.out.println("\tkey: " + entry.getKey() + "\tvalue: " + ((People)entry.getValue()).getName());
        	store(entry.getKey(), entry.getValue(), tableName);
        }
    }

    public void delete(Object key, String table) {
    	PreSQLParser psp = psps.get(table);
    	if(psp == null){
    		synchronized(lock){
    			psp = psps.get(table);
    			if(psp == null){
    				psp = new PreSQLParser();
            		psps.put(table, psp);
    			}
    		}
    	}
    	
    	String sql = psp.getDeleteSQLFromObject((String)key, table);
//    	System.out.println(sql);
    	Connection con = ConnectionPool.getConnection();
    	PreparedStatement stmt = null;
    	try {
			// added over
    		stmt = con.prepareStatement(sql);
    		String k = (String)key;
    		String [] keys = k.split(PreSQLParser.SPLIT_LABEL);
			if(keys == null || keys.length == 1){
				//閸楁洑瀵岄柨锟�				
				stmt.setInt(1, Integer.valueOf(k.substring(k.indexOf("=")+1)));
//				stmt.setObject(1, k.substring(k.indexOf("=")+1));
//				System.out.println("Delete Col Value:" + k.substring(k.indexOf("=")+1));
			} else {
				//婢舵矮瀵岄柨锟�				
				ArrayList<String> cols = psp.getDeleteCols();
				stmt.setObject(cols.indexOf(keys[0].substring(0, keys[0].indexOf("=")))+1, 
						keys[0].substring(keys[0].indexOf("=")+1));
				for(int i = 1; i != keys.length; ++i){
					stmt.setInt(cols.indexOf(keys[i].substring(0, keys[i].indexOf("=")))+1, 
							Integer.valueOf(keys[i].substring(keys[i].indexOf("=")+1)));
//					stmt.setObject(cols.indexOf(keys[i].substring(0, keys[i].indexOf("=")))+1, 
//							keys[i].substring(keys[i].indexOf("=")+1));
				}
			}
    		stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStmt(stmt);
			closeConnection(con);
		}
    }

    public void deleteAll(Collection keys, String table) {
    	//闂囷拷顪呴崷鈥揗AP閻ㄥ墔unStoreUpdate閸戣姤鏆熸穱顔芥暭娑撳甯撮崣锝忕礉娴肩姴鍙唌ap閿涘苯顪唖toreAll娑擄拷鐗�
        for(Object key : keys){
        	delete((String)key, table);
        }
    }
    
    public Object load(Object key) {
//        System.out.println("Loader.load " + key);
        return null;
    }
    
    
    public Set loadAllKeys() {
//        System.out.println("Loader.loadAllKeys ");
//        Set keys = new HashSet();
//        keys.add("key");
//        return keys;
        return null;
    }

    public Map loadAll(Collection keys) {
//        System.out.println("Loader.loadAll keys " + keys);
        return null;
    }
}
