// TestDemo: LoadData.LoadDataTest.java

package com.hazelcast.sqlclient.load;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;   
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.hazelcast.config.Config;
import com.hazelcast.config.TypeSerializerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;

import com.hazelcast.query.SqlPredicate;
import com.hazelcast.serializer.entity.HazelcastObject;
import com.hazelcast.sqlclient.BeanGeneratorFactory;
import com.hazelcast.sqlclient.HazelcastDatabaseMetaData;
import com.hazelcast.sqlclient.TableMetaData;
//import com.onceimdg.serializer.KryoSerializer;
import com.hazelcast.sqlclient.jdbc.IMDGResult;
import com.hazelcast.sqlclient.jdbc.IMDGResultSet;
import com.hazelcast.sqlclient.serializer.KryoSerializer;
//import com.hazelcast.sqlclient.serializer.KryoSerializerForImdgResultSet;

public class Loader {
	private String relDriver;
	private String relUrl;
	private String relUser;
	private String relPassword;
	
	private BeanGeneratorFactory beanGeneratorFactory = new BeanGeneratorFactory();
	private HazelcastDatabaseMetaData hazelcastDatabaseMetaData = new HazelcastDatabaseMetaData();
	private HazelcastInstance hazelcast = null;
	private InitialContext ctx = null;
	
	private boolean hasLoad = false;
	private String dbType = null;
	private String dsName = null;
	private static String loadConcurrently;
	private static int pageSize;
    private static int maxThreadPoolSize;
    private static Object lock_beanGenerator=new Object();
   
    public static void setThreadParam(String loadConcurrently, int pageSize, int maxThreadPoolSize){
   
    	Loader.loadConcurrently = loadConcurrently;
    	Loader.pageSize = pageSize;
    	Loader.maxThreadPoolSize = maxThreadPoolSize;
    }
    
    //jiang yong 2014-10-10  load one table concurrently
	class LoadTableSubThread implements Runnable{
		private Connection con;
		private long offset;
		private IMap<String, Object> map;
		private TableInfo tableInfo;
		private BeanGenerator beanGenerator;
		
		public LoadTableSubThread(TableInfo tableInfo, Connection con, long offset, BeanGenerator beanGenerator, IMap<String, Object> map){
			this.tableInfo = tableInfo;
			this.con = con;
			this.offset = offset;
			this.map = map;
			this.beanGenerator = beanGenerator;
			
		}
		public void run(){
			try{
				String tableName = tableInfo.getTableName();
				TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
				@SuppressWarnings("rawtypes")
				Class hazelcastObjectClass;
				HazelcastObject hazelcastObject;
				synchronized(lock_beanGenerator){
					hazelcastObjectClass = (Class) beanGenerator.createClass();
	                hazelcastObject = (HazelcastObject) beanGenerator.create();
				}
                BeanMap beanMap = BeanMap.create(hazelcastObject);
                String strid = "";
                Statement statement = con.createStatement();
                String sql = null;
            	if(dbType.equals("oracle")){
            		sql = "select * from " + tableName + " WHERE rownum < " + (offset+pageSize+1) 
            		+ " minus select * from " + tableName + " WHERE rownum < " + (offset + 1) ;
                }
                else if(dbType.equals("mysql")){
                	sql = "select * from " + tableName + " limit " + offset + "," + pageSize; 
                }
                else if(dbType.equals("shentong") || dbType.equals("dameng")){
                	sql = "select * from ( select *, rownum rnum from " + tableName + " where rownum < "
                	+ (offset+pageSize+1) + " ) where rnum > " + offset;
                }
                else{
                	throw new Exception("Warning: dbType error: " + dbType);
                }
            	
//            	System.out.println("sql:" + sql);
                ResultSet rsSet = statement.executeQuery(sql);
				while(rsSet.next()){
	        		for(int i = 0; i < tableMetaData.getColumnSize(); i++) {
	        			String attributeName = tableMetaData.getColumnName(i);
	        			Object attributeValue = getAttributeValue(tableMetaData.getColumnClassByAttributeIndex(i).getName(), rsSet, i+1);
//	        			Object attributeValue = rsSet.getObject(i+1);
//	        			if(tableMetaData.getColumnClassByAttributeIndex(i) == String.class) {
//	        				if(attributeValue != null) attributeValue = ((String)attributeValue).toLowerCase();
//	        			}
	        			try {
	        				beanMap.put(attributeName, attributeValue);
	        			} catch (Exception e) {
							e.printStackTrace();
							System.out.println(tableName);
							System.out.println(attributeName);
						}
	        		}	        		
	        		if(tableMetaData.getPrimaryKeyListSize() == 1){
	        			strid = tableMetaData.getPrimaryKeyList().get(0);
	        			int key = rsSet.getInt(strid);
	        			if(key > tableInfo.getMaxKey()){
	        				tableInfo.setMaxKey(key);
	        			}
	        			strid += "=" + String.valueOf(key);
//	        			while((id = idGenerator.newId()) < key);
//		        		strid += "=" + String.valueOf(id);
	        		}
	        		else{
	        			strid = "";
	        			for(String primaryAttributeName : tableMetaData.getPrimaryKeyList()){
	        				strid += primaryAttributeName + "=" + beanMap.get(primaryAttributeName)+"$#@";
	        			}
	        			strid = strid.toLowerCase();
	        		}
	        		
					hazelcastObject.setId(strid);
	        		map.put(strid, hazelcastObject);
	        		hazelcastObject = (HazelcastObject) hazelcastObjectClass.newInstance();
	        		beanMap.setBean(hazelcastObject);
                }
				rsSet.close();
			}catch (Exception e) {
	            System.err.println("SQLException :" + e.getMessage());
	            e.printStackTrace();
	        }
		}
	}
	
	//jiang yong 2014-10-10 load tables concurrently
	class LoadDataSubThread implements Runnable{
		
		private String tableName = null;
		private Connection con = null;
		public LoadDataSubThread(String tableName, Connection con){
			this.tableName = tableName;
			this.con = con;
		}
		public void run(){
			try {	
				Statement statementCount = con.createStatement();
				String sqlCount = "select count(*) as tableSize from " + tableName;
                ResultSet rsSetCount = statementCount.executeQuery(sqlCount);
                rsSetCount.next();
                long tableSize = rsSetCount.getLong("tableSize");
                rsSetCount.close();
//                System.out.println(tableName + ":" + tableSize);
                TableInfo tableInfo = new TableInfo(tableName);
        		tableInfo.setMaxKey(0);
        		
        		IdGenerator idGenerator = hazelcast.getIdGenerator(tableName);
        		BeanGenerator beanGenerator = beanGeneratorFactory.getBeanGenerator(tableName);
                if(beanGenerator == null){
                	throw new SQLException("no such table");
                }
                // IMap<String, Object> map = hazelcastClient.getMap(tableName);
				IMap<String, Object> map = hazelcast.getMap(tableName);
				map.setEnabled(false);
				//dynamic generate class by yanshi
                TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
                @SuppressWarnings("rawtypes")
				Class hazelcastObjectClass = (Class) beanGenerator.createClass();
                HazelcastObject hazelcastObject = (HazelcastObject) beanGenerator.create();
                BeanMap beanMap = BeanMap.create(hazelcastObject);
                String strid = "";
//        		if(hasLoad){continue;}
                if(!hasLoad){
                	if(tableSize > pageSize){
//                		System.out.println("start to load " + tableName + " table concurrently...");
                		long pageNum;
                		if (tableSize%pageSize == 0){
                			pageNum = tableSize/pageSize;
                		}
                		else{
                			pageNum = tableSize/pageSize  + 1;
                		}
                		long offset = 0;
                		ExecutorService executor = Executors.newFixedThreadPool(maxThreadPoolSize);
                        for(int i = 0; i < pageNum; i++){
                        	offset = i * pageSize;
                            Runnable loadTableSubThread = new LoadTableSubThread(tableInfo, con, offset, beanGenerator, map);
                            executor.execute(loadTableSubThread);
                        }
                        executor.shutdown();
                        while(!executor.isTerminated()){
                			try {
                	            Thread.sleep(10);
                	        } catch (InterruptedException e) {
                	            e.printStackTrace(); 
                	        }
                		}
                	}
                  else{
                  		Statement statement = con.createStatement();
        				String sql = "select * from " + tableName;
        				ResultSet rsSet = statement.executeQuery(sql);
                    	while(rsSet.next()){
        	        		for(int i = 0; i < tableMetaData.getColumnSize(); i++) {
        	        			String attributeName = tableMetaData.getColumnName(i);
        	        			Object attributeValue = getAttributeValue(tableMetaData.getColumnClassByAttributeIndex(i).getName(), rsSet, i+1);
//        	        			Object attributeValue = rsSet.getObject(i+1);
//        	        			if(tableMetaData.getColumnClassByAttributeIndex(i) == String.class) {
//        	        				if(attributeValue != null) attributeValue = ((String)attributeValue).toLowerCase();
//        	        			}
        	        			try {
        	        				beanMap.put(attributeName, attributeValue);
        	        			} catch (Exception e) {
        							e.printStackTrace();
        							System.out.println(tableName);
        							System.out.println(attributeName);
        						}
        	        		}	        		
        	        		if(tableMetaData.getPrimaryKeyListSize() == 1){
        	        			strid = tableMetaData.getPrimaryKeyList().get(0);
        	        			int key = rsSet.getInt(strid);
        	        			if(key > tableInfo.getMaxKey()){
        	        				tableInfo.setMaxKey(key);
        	        			}
        	        			strid += "=" + String.valueOf(key);
//        	        			while((id = idGenerator.newId()) < key);
//        		        		strid += "=" + String.valueOf(id);
        	        		}
        	        		else{
        	        			strid = "";
        	        			for(String primaryAttributeName : tableMetaData.getPrimaryKeyList()){
        	        				strid += primaryAttributeName + "=" + beanMap.get(primaryAttributeName)+"$#@";
        	        			}
        	        			strid = strid.toLowerCase();
        	        		}
        	        		hazelcastObject.setId(strid);
        	        		map.put(strid, hazelcastObject);
        	        		hazelcastObject = (HazelcastObject) hazelcastObjectClass.newInstance();
        	        		beanMap.setBean(hazelcastObject);
                        }
                    	rsSet.close();
                  }
                  idGenerator.init(tableInfo.getMaxKey());
                  map.setEnabled(true);
                  System.out.println("load data from " + tableName + " done!");
                }  
	        } catch (Exception e) {
	            System.err.println("SQLException :" + e.getMessage());
	            e.printStackTrace();
	        }
		}
	}
	
	public Loader() {
		try {
			if(ctx == null)
				ctx = new InitialContext();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		if((hazelcast = Hazelcast.getHazelcastInstanceByName("IMDG")) == null) {
			TypeSerializerConfig sc = new TypeSerializerConfig().
									setImplementation(new KryoSerializer()).
									setTypeClass(HazelcastObject.class);
//			TypeSerializerConfig sc2 = new TypeSerializerConfig().
//					setImplementation(new KryoSerializerForImdgResultSet()).
//					setTypeClass(IMDGResultSet.class);
			Config config = null;
			config = new XmlConfigBuilder().build();
			config.setInstanceName("IMDG");
			config.getSerializationConfig().addTypeSerializer(sc);
//			config.getSerializationConfig().addTypeSerializer(sc2);
			hazelcast = Hazelcast.newHazelcastInstance(config);
			//for distributed , zhaohui
//			IMap<String, Boolean> loadOnceMap = hazelcast.getMap("LoadOnce");
//			if(!loadOnceMap.containsKey("HasLoaded")) {
//				loadOnceMap.put("HasLoaded", true);
//			} else hasLoad = true;

//			IMap<String, String> loadOnceMap = hazelcast.getMap("LoadOnce");
//			loadOnceMap.setEnabled(false);
//			if(!loadOnceMap.containsKey("id=1")) {
//				loadOnceMap.put("id=1", "HasLoaded");
//			} else hasLoad = true;
//			loadOnceMap.setEnabled(false);
		}
	}
	
	public Loader(String driver, String url, String user, String password) {
		this.relDriver = driver;
		this.relUrl = url;
		this.relUser = user;
		this.relPassword = password;
		if((hazelcast = Hazelcast.getHazelcastInstanceByName("IMDG")) == null) {
			TypeSerializerConfig sc = new TypeSerializerConfig().
									setImplementation(new KryoSerializer()).
									setTypeClass(HazelcastObject.class);
			Config config = null;
			/*
			try {
//				config = new XmlConfigBuilder("hazelcast.xml").build();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			*/
			config = new XmlConfigBuilder().build();
			config.setInstanceName("IMDG");
			config.getSerializationConfig().addTypeSerializer(sc);
			hazelcast = Hazelcast.newHazelcastInstance(config);
			//for distributed , zhaohui
//			IMap<String, Boolean> loadOnceMap = hazelcast.getMap("LoadOnce");
//			if(!loadOnceMap.containsKey("HasLoaded")) {
//				loadOnceMap.put("HasLoaded", true);
//			} else hasLoad = true;

//			IMap<String, String> loadOnceMap = hazelcast.getMap("LoadOnce");
//			loadOnceMap.setEnabled(false);
//			if(!loadOnceMap.containsKey("id=1")) {
//				loadOnceMap.put("id=1", "HasLoaded");
//			} else hasLoad = true;
//			loadOnceMap.setEnabled(false);
		}   
	}
	
	public void configRelationalDatabase(String driver, String url, String user, String password){
		this.relDriver = driver;
		this.relUrl = url;
		this.relUser = user;
		this.relPassword = password;
	}
	
	public void loadMetaData(){
        try {
        	Connection con = null;
        	if(ctx != null) {
        		DataSource ds = (DataSource) ctx.lookup(dsName);
    			con = ds.getConnection();
        	} else {
        		Class.forName(relDriver);
              	con = DriverManager.getConnection(this.relUrl, this.relUser, this.relPassword);
        	}
            Statement statement = con.createStatement();
            DatabaseMetaData dbMetaData = con.getMetaData();
            // get MetaData about all tables
            ResultSet resultSet = null;
            if(dbType.equals("oracle")){
            	resultSet = dbMetaData.getTables(null, "IMDG", null, new String[]{"TABLE"});
            }
            else if(dbType.equals("mysql")){
            	resultSet = dbMetaData.getTables(null, null, null, new String[]{"TABLE"});
            }
            else if(dbType.equals("shentong") || dbType.equals("dameng")){
            	resultSet = dbMetaData.getTables(null, "SYSDBA", null, new String[]{"TABLE"});
            }
            else{
            	throw new Exception("Warning: dbType error: " + dbType);
            }
            
            //jiang yong 2014-10-10 get the number of tables
//            ResultSetMetaData rsmd = resultSet.getMetaData();
//            tableNum = rsmd.getColumnCount();
            
            while(resultSet.next()){
            	// int tableRow = resultSet.getRow();
            	//TODO: Oracle Table_Name UperCase
                String tableName = resultSet.getString("TABLE_NAME").toLowerCase();
                if(tableName.startsWith("##")){
                	continue;
                }
//                String tableName = resultSet.getString("TABLE_NAME").toLowerCase();
                // String tableType = resultSet.getString("TABLE_TYPE");
                // String tableCategory = resultSet.getString("TABLE_CAT");
                // String tableSchema = resultSet.getString("TABLE_SCHEM");
                // String tableRemarks = resultSet.getString("REMARKS");
               
            	String sql = null;
            	if(dbType.equals("oracle") || dbType.equals("shentong") || dbType.equals("dameng")){
            		sql = "select * from " + tableName + " WHERE rownum < 2";
                }
                else if(dbType.equals("mysql")){
                	sql = "select * from " + tableName + " limit 1";
                }
                else{
                	throw new Exception("Warning: dbType error: " + dbType);
                }
                ResultSet rsSet = statement.executeQuery(sql);
                ResultSetMetaData rsData = rsSet.getMetaData();
            	
                BeanGenerator beanGenerator = new BeanGenerator();
                beanGenerator.setNamingPolicy(new IMDGNamingPolicy(tableName));
            	beanGenerator.setSuperclass(HazelcastObject.class);
            	TableMetaData tableMetaData = new TableMetaData();
            	tableMetaData.setTableName(tableName);
//            	System.out.println("tableName: " + tableName);
                for (int i = 1; i <= rsData.getColumnCount(); i++) {
                	
                	//TODO: data to java.sql.timestamp
                	String columnName = rsData.getColumnName(i).toLowerCase();
                	String className = rsData.getColumnClassName(i);
//                	
//                	System.out.println("columnName: " + columnName);
//                	System.out.println("className: " + className);
                	/*
                	String label = rsData.getColumnLabel(i);
                	int type = rsData.getColumnType(i);
                	int precision = rsData.getPrecision(i);
                	boolean bl = rsData.isCaseSensitive(i);
                	boolean rdonly = rsData.isReadOnly(i);
                	*/
                	beanGenerator.addProperty(columnName, this.getClassFromString(className));
                	tableMetaData.addColumnName(columnName);
                	tableMetaData.addColumnClass(this.getClassFromString(className));
                }
                rsSet.close();
                
                //TODO : oracle schema
//                ResultSet rs = dbMetaData.getPrimaryKeys(null, null, tableName);
                ResultSet rs = null;
            	if(dbType.equals("oracle")){
            		rs = dbMetaData.getPrimaryKeys(null, "IMDG", tableName.toUpperCase());
                }
                else if(dbType.equals("mysql")){
                	rs = dbMetaData.getPrimaryKeys(null, null, tableName);
                }
                else if(dbType.equals("shentong") || dbType.equals("dameng")){
            		rs = dbMetaData.getPrimaryKeys(null, "SYSDBA", tableName.toUpperCase());
                }
                else{
                	throw new Exception("Warning: dbType error: " + dbType);
                }
                while(rs.next()){
                	/*
                	String name = rs.getString("table_name");
                	System.out.println("table name: " + name);
                	String columnName = rs.getString("column_name");
                	System.out.println("column name: " + columnName);
                	String keySeq = rs.getString("key_seq");
                	System.out.println("sequence in key: " + keySeq);                	
                	String pkName = rs.getString("pk_name");
                	*/
                	String columnName = rs.getString("column_name").toLowerCase();
                	tableMetaData.addPrimaryKey(columnName);
                }
                rs.close();
                beanGeneratorFactory.setBeanGenerator(tableName, beanGenerator);
                hazelcastDatabaseMetaData.setTableMetaData(tableName, tableMetaData);   
            }
            resultSet.close();
            con.close();
        } catch (Exception e) {
            System.err.println("SQLException :" + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("load metadata done!");
	}
	
	public void loadIndex(){
        try {
        	Connection con = null;
        	if(ctx != null) {
        		DataSource ds = (DataSource) ctx.lookup(dsName);
    			con = ds.getConnection();
        	} else {
        		Class.forName(relDriver);
              	con = DriverManager.getConnection(this.relUrl, this.relUser, this.relPassword);
        	}
            DatabaseMetaData dbMetaData = con.getMetaData();
            ResultSet resultSet = null; 
            if(dbType.equals("oracle")){
            	resultSet = dbMetaData.getTables(null, "IMDG", null, new String[]{"TABLE"});
            }
            else if(dbType.equals("mysql")){
            	resultSet = dbMetaData.getTables(null, null, null, new String[]{"TABLE"});
            }
            else if(dbType.equals("shentong") || dbType.equals("dameng")){
            	resultSet = dbMetaData.getTables(null, "SYSDBA", null, new String[]{"TABLE"});
            }
            else{
            	throw new Exception("Warning: dbType error: " + dbType);
            }
            System.out.println("creating index from backend database...");
            while(resultSet.next()){
                String tableName = resultSet.getString("TABLE_NAME").toLowerCase();
                IMap<String, Object> map = hazelcast.getMap(tableName);
                ResultSet indexInfo = null; 
                if(dbType.equals("oracle")){
                	indexInfo = dbMetaData.getIndexInfo(null, "IMDG", tableName.toUpperCase(), false, false);
                	while (indexInfo.next()) {
    					short type = indexInfo.getShort("TYPE");
    					if(type != DatabaseMetaData.tableIndexStatistic){
    						String dbIndexName = indexInfo.getString("INDEX_NAME");
    						String dbColumnName = indexInfo.getString("COLUMN_NAME");
//    						 System.out.println("tableName:"+tableName);
//    						 System.out.println("indexName:"+dbIndexName);
//    						 System.out.println("columnName:"+dbColumnName);
    						if (!dbIndexName.startsWith("SYS")) {
    							map.addIndex(dbColumnName.toLowerCase(), true);
    						}
    					}
    				}
                }
                else if(dbType.equals("mysql")){
                	indexInfo = dbMetaData.getIndexInfo(null, null, tableName, false, false);
                	while (indexInfo.next()) {
    					short type = indexInfo.getShort("TYPE");
    					if(type != DatabaseMetaData.tableIndexStatistic){
    						String dbIndexName = indexInfo.getString("INDEX_NAME");
    						String dbColumnName = indexInfo.getString("COLUMN_NAME");
    						if (!dbIndexName.equals("PRIMARY")) {
    							map.addIndex(dbColumnName.toLowerCase(), true);
    						}
    					}
    				}
                }
                else if(dbType.equals("shentong")){
                	indexInfo = dbMetaData.getIndexInfo(null, "SYSDBA", tableName.toUpperCase(), false, false);
                	while (indexInfo.next()) {
    					short type = indexInfo.getShort("TYPE");
    					if(type != DatabaseMetaData.tableIndexStatistic){
    						String dbIndexName = indexInfo.getString("INDEX_NAME");
    						String dbColumnName = indexInfo.getString("COLUMN_NAME");
    						 System.out.println("tableName:"+tableName);
    						 System.out.println("indexName:"+dbIndexName);
    						 System.out.println("columnName:"+dbColumnName);
    						if (!dbIndexName.endsWith("PKEY")) {
    							map.addIndex(dbColumnName.toLowerCase(), true);
    						}
    					}
    				}
                }
                else if(dbType.equals("dameng")){
                	indexInfo = dbMetaData.getIndexInfo(null, "SYSDBA", tableName.toUpperCase(), false, false);
                	while (indexInfo.next()) {
    					short type = indexInfo.getShort("TYPE");
    					if(type != DatabaseMetaData.tableIndexStatistic){
    						String dbIndexName = indexInfo.getString("INDEX_NAME");
    						String dbColumnName = indexInfo.getString("COLUMN_NAME");
//    						 System.out.println("tableName:"+tableName);
//    						 System.out.println("indexName:"+dbIndexName);
//    						 System.out.println("columnName:"+dbColumnName);
    						if (!dbIndexName.endsWith("INDEX")) {
    							map.addIndex(dbColumnName.toLowerCase(), true);
    						}
    					}
    				}
                }
                else{
                	throw new Exception("Warning: dbType error: " + dbType);
                }
//                ResultSetMetaData rsmd = indexInfo.getMetaData();
//                int cols = rsmd.getColumnCount();
//                while(indexInfo.next()) {
//                	short type = indexInfo.getShort("TYPE");
//                	System.out.println("tableName: " + tableName + " type: " + type);
//                	switch (type) {
//                    case DatabaseMetaData.tableIndexClustered:
//                      System.out.println("tableIndexClustered");
//                      break;
//                    case DatabaseMetaData.tableIndexHashed:
//                      System.out.println("tableIndexHashed");
//                      break;
//                    case DatabaseMetaData.tableIndexOther:
//                      System.out.println("tableIndexOther");
//                      break;
//                    case DatabaseMetaData.tableIndexStatistic:
//                      System.out.println("tableIndexStatistic");
//                      break;
//                    default:
//                      System.out.println("tableIndexOther44");
//                    }
//                   for (int i = 1; i <= cols; i++) {
//                      System.out.println("tableName: " + tableName + " index: " + i + " indexinfo: " + indexInfo.getString(i));
//                   }
//                }
			}
            System.out.println("index created.");
            resultSet.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	//TODO: create index error锟斤拷锟斤拷1锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟捷库不锟斤拷锟节的�?2锟斤拷锟斤拷锟斤拷锟斤拷确锟斤拷锟斤拷锟斤拷锟斤拷锟�
	//TODO: 锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟叫讹拷
    public void createIndex(){
    	try{
    		Properties prop = new Properties();  
        	InputStream in = Loader.class.getResourceAsStream(ConfigParser.getConfigFilePath());
        	if(in != null){
				prop.load(in);
				String indexSwitch = prop.getProperty("indexSwitch");
				if (indexSwitch.equals("0")) {
					this.loadIndex();
				} else if (indexSwitch.equals("1")) {
					System.out.println("creating index...");
					String indexTables = prop.getProperty("indexTables");
					for (String indexTable : ConfigParser.stringSplit(indexTables)) {
						IMap<String, Object> map = hazelcast.getMap(indexTable.toLowerCase());
						String indexTableColumns = prop.getProperty(indexTable);
						for (String indexTableColumn : ConfigParser.stringSplit(indexTableColumns)) {
							map.addIndex(indexTableColumn.toLowerCase(), true);
						}
					}
					System.out.println("index created!");
				} else if (indexSwitch.equals("2")) {
					System.out.println("no index created!");
				} else {
					throw new IOException("indexSwitch error, no index created!");
				}
			}else {
				throw new IOException("warning: can't find EasyCacheConfig.properties");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public void loadData(){
		ConfigParser.loaderConfigParse();
		ConfigParser.executorConfigParse();
		ConfigParser.queryResultCacheConfigParse();
		dbType = ConfigParser.getInstance().configParseGetDbType();
		dsName = ConfigParser.getInstance().configParseGetDsName();
		this.loadMetaData();
		this.createIndex();
		if(loadConcurrently.equals("true")){
			loadDataConcurrently();
		}
		else{
			loadDataSingly();
		}
	}
	
	public void loadDataConcurrently(){
		System.out.println("begin to load data concurrently......");
		// HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
		try {
			// changed by yanshi.wang
        	Connection con = null;
        	if(ctx != null) {
        		DataSource ds = (DataSource) ctx.lookup(dsName);
    			con = ds.getConnection();
        	} else {
        		Class.forName(relDriver);
              	con = DriverManager.getConnection(this.relUrl, this.relUser, this.relPassword);
        	}
        	//jiang yong 2014-10-10 get the number of tables
        	int threadNum = hazelcastDatabaseMetaData.getTableNames().size();
            ExecutorService executor = Executors.newFixedThreadPool(threadNum);
            for(String tableName : hazelcastDatabaseMetaData.getTableNames()){
                Runnable loadDataSubThread = new LoadDataSubThread(tableName, con);
                executor.execute(loadDataSubThread);
            }
            executor.shutdown();
            while(!executor.isTerminated()){
    			try {
    	            Thread.sleep(10);
    	        } catch (InterruptedException e) {
    	            e.printStackTrace(); 
    	        }
    		}
            con.close();
        } catch (Exception e) {
            System.err.println("SQLException :" + e.getMessage());
            e.printStackTrace();
        }
	}
	
	public void loadDataSingly(){
		System.out.println("begin to load data......");
		// HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
		try {
			// changed by yanshi.wang
        	Connection con = null;
        	if(ctx != null) {
        		DataSource ds = (DataSource) ctx.lookup(dsName);
    			con = ds.getConnection();
        	} else {
        		Class.forName(relDriver);
              	con = DriverManager.getConnection(this.relUrl, this.relUser, this.relPassword);
        	}
        	// changed over
            Statement statement = con.createStatement();

            for(String tableName : hazelcastDatabaseMetaData.getTableNames()){
                String sql = "select * from " + tableName;
                ResultSet rsSet = statement.executeQuery(sql);
                TableInfo tableInfo = new TableInfo(tableName);
        		tableInfo.setMaxKey(0);
                BeanGenerator beanGenerator = beanGeneratorFactory.getBeanGenerator(tableName);
                if(beanGenerator == null){
                	throw new SQLException("no such table");
                }
                // IMap<String, Object> map = hazelcastClient.getMap(tableName);
				IMap<String, Object> map = hazelcast.getMap(tableName);
				map.setEnabled(false);
				
				//dynamic generate class by yanshi
                TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
                @SuppressWarnings("rawtypes")
				Class hazelcastObjectClass = (Class) beanGenerator.createClass();
                HazelcastObject hazelcastObject = (HazelcastObject) beanGenerator.create();
                BeanMap beanMap = BeanMap.create(hazelcastObject);
				IdGenerator idGenerator = hazelcast.getIdGenerator(tableName);
//                long id = 0;
                String strid = "";
        		if(hasLoad){continue;}
                while(rsSet.next()){
	        		for(int i = 0; i < tableMetaData.getColumnSize(); i++) {
	        			String attributeName = tableMetaData.getColumnName(i);
	        			Object attributeValue = getAttributeValue(tableMetaData.getColumnClassByAttributeIndex(i).getName(), rsSet, i+1);
////	        			Object attributeValue = rsSet.getObject(i+1);
//	        			if(tableMetaData.getColumnClassByAttributeIndex(i) == String.class) {
//	        				if(attributeValue != null) attributeValue = ((String)attributeValue).toLowerCase();
//	        			}
//	        			if(attributeValue.getClass().equals(java.math.BigDecimal.class)){
//	        				BigDecimal temp = (BigDecimal)attributeValue;
//	        				attributeValue = new Integer(temp.intValue());
//	        			}
	        			try {
	        				beanMap.put(attributeName, attributeValue);
	        			} catch (Exception e) {
							e.printStackTrace();
							System.out.println(tableName);
							System.out.println(attributeName);
						}
	        		}	        		
	        		if(tableMetaData.getPrimaryKeyListSize() == 1){
	        			strid = tableMetaData.getPrimaryKeyList().get(0);
	        			int key = rsSet.getInt(strid);
	        			if(key > tableInfo.getMaxKey()){
	        				tableInfo.setMaxKey(key);
	        			}
	        			strid += "=" + String.valueOf(key);
//	        			while((id = idGenerator.newId()) < key);
//		        		strid += "=" + String.valueOf(id);
	        		}
	        		else{
	        			strid = "";
	        			for(String primaryAttributeName : tableMetaData.getPrimaryKeyList()){
	        				strid += primaryAttributeName + "=" + beanMap.get(primaryAttributeName)+"$#@";
	        			}
	        			strid = strid.toLowerCase();
	        		}
	        		hazelcastObject.setId(strid);
	        		map.put(strid, hazelcastObject);
	        		hazelcastObject = (HazelcastObject) hazelcastObjectClass.newInstance();
	        		beanMap.setBean(hazelcastObject);
                }
                idGenerator.init(tableInfo.getMaxKey());
                rsSet.close();
                map.setEnabled(true);
                System.out.println("load data from " + tableName + " done!");
            }
            con.close();
        } catch (Exception e) {
            System.err.println("SQLException :" + e.getMessage());
            e.printStackTrace();
        }
	}
	
	//zhaohui, jiangyong
	public void creatIndexForTPCW(){
		System.out.println("create index...");
		IMap<String, Object> map = hazelcast.getMap("order_line");
		map.addIndex("ol_o_id", true);
//		map = hazelcast.getMap("shopping_cart_line");
//		map.addIndex("scl_sc_id", true);
//		map = hazelcast.getMap("country");
//		map.addIndex("co_name", true);
		
		// added by yanshi.wang
		// map = Hazelcast.getMap("item");
//		map.addIndex("i_id", false);
//		map = hazelcast.getMap("address");
		// map.addIndex("$cglib_prop_addr_id", true); // ...
		// map.addIndex("$cglib_prop_addr_street1", true);
		// map.addIndex("$cglib_prop_addr_street2", true);
		// map.addIndex("$cglib_prop_addr_city", true);
		// map.addIndex("$cglib_prop_addr_state", true);
		// map.addIndex("$cglib_prop_addr_zip", true);
//		map.addIndex("addr_co_id", true);
		// added over
		System.out.println("create index done!");
	}
	
	public void creatIndexForTPCW_old_yanshi(){
		System.out.println("create index...");
		IMap<String, Object> map = hazelcast.getMap("customer");
		map.addIndex("$cglib_prop_c_uname", true);
		map = hazelcast.getMap("order_line");
		map.addIndex("$cglib_prop_ol_o_id", true);
		map = hazelcast.getMap("shopping_cart_line");
		map.addIndex("$cglib_prop_scl_sc_id", true);
		map = hazelcast.getMap("country");
		map.addIndex("$cglib_prop_co_name", true);
		
		// added by yanshi.wang
		// map = Hazelcast.getMap("item");
		map.addIndex("$cglib_prop_i_id", false);
		map = hazelcast.getMap("address");
		// map.addIndex("$cglib_prop_addr_id", true); // ...
		// map.addIndex("$cglib_prop_addr_street1", true);
		// map.addIndex("$cglib_prop_addr_street2", true);
		// map.addIndex("$cglib_prop_addr_city", true);
		// map.addIndex("$cglib_prop_addr_state", true);
		// map.addIndex("$cglib_prop_addr_zip", true);
		map.addIndex("$cglib_prop_addr_co_id", true);
		// added over
	}
	

	public void printMetaData(){
		try{
	        // Class.forName(relDriver);
	        // Connection con = DriverManager.getConnection(this.relUrl, this.relUser, this.relPassword);
        	Connection con = null;
        	if(ctx != null) {
        		DataSource ds = (DataSource) ctx.lookup(dsName);
    			con = ds.getConnection();
        	} else {
        		Class.forName(relDriver);
              	con = DriverManager.getConnection(this.relUrl, this.relUser, this.relPassword);
        	}
        	// changed over
	        DatabaseMetaData dbMetaData = con.getMetaData();
	        
	        
            System.out.println("URL:" + dbMetaData.getURL() + ";");
            System.out.println("UserName:" + dbMetaData.getUserName() + ";");
            System.out.println("isReadOnly:" + dbMetaData.isReadOnly() + ";");
            System.out.println("DatabaseProductName:"+ dbMetaData.getDatabaseProductName() + ";");
            System.out.println("DatabaseProductVersion:" + dbMetaData.getDatabaseProductVersion() + ";");
            System.out.println("DriverName:" + dbMetaData.getDriverName() + ";");
            System.out.println("DriverVersion:" + dbMetaData.getDriverVersion());
            
	        System.out.println("database product name: " + dbMetaData.getDatabaseProductName());
	        System.out.println("whether support transaction: " + dbMetaData.supportsTransactions());
	        System.out.println("version number of database: "+dbMetaData.getDatabaseProductVersion());
	        System.out.println("isolation level of transaction: "+dbMetaData.getDefaultTransactionIsolation());
	        System.out.println("whether support batch updates: "+dbMetaData.supportsBatchUpdates());
	        System.out.println("database url: "+dbMetaData.getURL());
	        System.out.println("user name of database: "+dbMetaData.getUserName());
	        System.out.println("whether read only model: "+dbMetaData.isReadOnly());
	        System.out.println("whether support alias for column: "+dbMetaData.supportsColumnAliasing());
	        System.out.println("whether support like: "+dbMetaData.supportsLikeEscapeClause());
	        System.out.println("whether support limited outerjoins: "+dbMetaData.supportsLimitedOuterJoins());
	        System.out.println("whether support multiple transactions: "+dbMetaData.supportsMultipleTransactions());
	        System.out.println("whether support subsqueries in exists:"+dbMetaData.supportsSubqueriesInExists());
	        System.out.println("whether support subqueries in in sentence: "+dbMetaData.supportsSubqueriesInIns());
	        System.out.println("whether support given isolation level: "+dbMetaData.supportsTransactionIsolationLevel(1));
	        System.out.println("whetehr support transaction: "+dbMetaData.supportsTransactions());
	        System.out.println("whether support SQL UNION:"+dbMetaData.supportsUnion());
	        System.out.println("whether support SQL UNION ALL:"+dbMetaData.supportsUnionAll());
	        System.out.println("use local file for each table? "+dbMetaData.usesLocalFilePerTable());
	        System.out.println("whether store table in local file:"+dbMetaData.usesLocalFiles());
	        System.out.println("major version of database: "+dbMetaData.getDatabaseMajorVersion());
	        System.out.println("minor version of database: "+dbMetaData.getDatabaseMinorVersion());
	        System.out.println("JDBC majoir version: "+dbMetaData.getJDBCMajorVersion());
	        System.out.println("JDBC minor version: "+dbMetaData.getJDBCMinorVersion());
	        System.out.println("JDBC driver name: "+dbMetaData.getDriverName());
	        System.out.println("JDBC driver version:"+dbMetaData.getDriverVersion());
	        System.out.println("extral characters: "+dbMetaData.getExtraNameCharacters());
	        System.out.println("string to invoke sql: "+dbMetaData.getIdentifierQuoteString());
	        System.out.println("getMaxCatalogNameLength:"+dbMetaData.getMaxCatalogNameLength());
	        System.out.println("getMaxColumnNameLength:"+dbMetaData.getMaxColumnNameLength());
	        System.out.println("getMaxColumnsInGroupBy:"+dbMetaData.getMaxColumnsInGroupBy());
	        System.out.println("getMaxColumnsInSelect:"+dbMetaData.getMaxColumnsInSelect());
	        System.out.println("getMaxColumnsInTable:"+dbMetaData.getMaxColumnsInTable());
	        System.out.println("getMaxConnections:"+dbMetaData.getMaxConnections());
	        System.out.println("getMaxCursorNameLength:"+dbMetaData.getMaxCursorNameLength());
	        System.out.println("getMaxStatements: "+dbMetaData.getMaxStatements());
		}
    	catch (Exception e) {
    		System.err.println("SQLException :" + e.getMessage());
    		e.printStackTrace();
    	}
	}
	
	//TODO: more type, like short...
	public static Object getAttributeValue(String classTypeName, ResultSet rsSet, int i){
		try{
			if(classTypeName.equals("java.lang.Integer")){
				return new Integer(rsSet.getInt(i));
			}
			if(classTypeName.equals("java.lang.Long")){
				return new Long(rsSet.getLong(i));
			}
			if(classTypeName.equals("java.lang.Double")){
				return new Double(rsSet.getDouble(i));
			}
			//lowercase?
			if(classTypeName.equals("java.lang.String")){
				return rsSet.getString(i);
			}
			if(classTypeName.equals("java.sql.Date")){
				return rsSet.getDate(i);
			}
			if(classTypeName.equals("java.sql.Timestamp")){
				return rsSet.getTimestamp(i);
			}
			//TODO cglib not support bigDecimal
			if(classTypeName.equals("java.math.BigDecimal")){
				return new Double(rsSet.getBigDecimal(i).doubleValue());
			}
			if(classTypeName.equals("oracle.sql.TIMESTAMP")){
				return rsSet.getTimestamp(i);
			}
			return rsSet.getObject(i);
		}catch(Exception e) {
    		System.err.println("SQLException :" + e.getMessage());
    		e.printStackTrace();
    		return null;
    	}
		
	}
	//TODO: more type, like short...
	@SuppressWarnings("rawtypes")
	private  Class getClassFromString(String classTypeName){
		if(classTypeName.equals("java.lang.Integer")){
			return Integer.class;
		}
		if(classTypeName.equals("java.lang.Long")){
			return Long.class;
		}
		if(classTypeName.equals("java.lang.Double")){
			return Double.class;
		}
		if(classTypeName.equals("java.lang.String")){
			return String.class;
		}
		if(classTypeName.equals("java.sql.Date")){
			if(dbType.equals("dameng")){
				return java.sql.Timestamp.class;
			}
			return java.sql.Date.class;
		}
		if(classTypeName.equals("java.sql.Timestamp")){
			if(dbType.equals("oracle")){
				return java.sql.Date.class;
			}
			return java.sql.Timestamp.class;
		}
//		if(classTypeName.equals("java.math.BigDecimal")){
//			return java.math.BigDecimal.class;
//		}
		//TODO cglib not support bigDecimal
		if(classTypeName.equals("java.math.BigDecimal")){
			if(dbType.equals("oracle") || dbType.equals("shentong") || dbType.equals("dameng") ){
				return java.lang.Integer.class;
			}
			return java.math.BigDecimal.class;
		}
		if(classTypeName.equals("oracle.sql.TIMESTAMP")){
			return java.sql.Timestamp.class;
		}
		System.out.println("warning: unsupported classTypeName" + classTypeName);
		return String.class;
	}
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Loader loader = new Loader("com.mysql.jdbc.Driver","jdbc:mysql://localhost:3306/bench4q","muye","muye");
		loader.loadData();
		System.out.println("Load Data Done");
		IMap<String, HazelcastObject> imap = Hazelcast.getHazelcastInstanceByName("IMDG").getMap("shopping_cart_line");
		System.out.println(imap.size());
		HazelcastObject object = null;
		String sql = "scl_sc_id=0";
		Collection<HazelcastObject> collection = imap.values(new SqlPredicate(sql));
		if(collection.size() == 0) {
			System.out.println("NO found items for sql: " + sql);
		} else {
			System.out.println("item size: " + collection.size());
			for (HazelcastObject hazelcastObject : collection) {
				object = hazelcastObject;
				@SuppressWarnings("rawtypes")
				Class cls = object.getClass();
				Method[] methods = cls.getDeclaredMethods();
				java.lang.reflect.Field[] fields = cls.getDeclaredFields();
				
				if(fields == null) {
					System.out.println("fields is null");
				}
				
				for(int i = 0; i < fields.length; ++i) {
					System.out.println("fields[" + i + "].getName() : " + fields[i].getName());
				}
				
				for (int i = 0; i < methods.length; i++) {
					System.out.println("methods[" + i + "].getName() : " + methods[i].getName());
					if(methods[i].getName().indexOf("get") != -1) 
						System.out.println("methods[" + i + "].invoke(object) : " + methods[i].invoke(object));
				}		
			}
		}

		Hazelcast.shutdownAll();
    }
}