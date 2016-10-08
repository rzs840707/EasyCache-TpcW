package com.hazelcast.sqlclient.load;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.hazelcast.sqlclient.executor.HazelcastExecutor;
import com.hazelcast.sqlclient.utility.IMDGString;

public class ConfigParser {
	
	private static String dbType = null;
	private static boolean dbTypeIsParsed = false;
	private static String configFilePath = "EasyCacheConfig.properties";
	
	//singleton
	private static ConfigParser configParser = null;   
    private ConfigParser(){}   
    public static ConfigParser getInstance()   
    {   
        if (configParser== null)   
        {   
            configParser= new ConfigParser();
        }   
        return configParser;   
    }   
	
    public static String getConfigFilePath(){
    	return configFilePath;
    }
    
    public static void loaderConfigParse(){
    	try{
    		Properties prop = new Properties();  
//        	File file1 = new File(this.getClass().getResource("/").getPath()+ "../loadData.properties");
//        	System.out.println(file1.getAbsolutePath());
        	InputStream in = ConfigParser.class.getResourceAsStream(configFilePath);
        	if(in != null){
        		prop.load(in);
        		String loadConcurrently = prop.getProperty("loadConcurrently");
        		int pageSize = Integer.parseInt(prop.getProperty("pageSize"));
                int maxThreadPoolSize = Integer.parseInt(prop.getProperty("maxThreadPoolSize"));
                Loader.setThreadParam(loadConcurrently, pageSize, maxThreadPoolSize);
        	}
        	else{
        		throw new IOException("warning :can't find EasyCacheConfig.properties");
        	}
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void executorConfigParse(){
    	try{
    		Properties prop = new Properties();  
//        	File file1 = new File(Loader.class.getResource("/").getPath()+ "../config.properties");
//        	System.out.println(file1.getAbsolutePath());
        	InputStream in = ConfigParser.class.getResourceAsStream(configFilePath);
        	if(in != null){
        		prop.load(in);
        		String selectFilter = prop.getProperty("selectFilter");
        		if(selectFilter != null){
        			selectFilter = selectFilter.toLowerCase();
    			}
        		String insertFilter = prop.getProperty("insertFilter");
    			if (insertFilter != null) {
    				insertFilter = insertFilter.toLowerCase();
    			}
        		String deleteFilter = prop.getProperty("deleteFilter");
        		if (deleteFilter != null) {
        			deleteFilter = deleteFilter.toLowerCase();
    			}
        		String updateFilter = prop.getProperty("updateFilter");
        		if (updateFilter != null) {
        			updateFilter = updateFilter.toLowerCase();
    			}
                HazelcastExecutor.setSqlFilter(stringSplit(selectFilter), stringSplit(insertFilter), stringSplit(deleteFilter), stringSplit(updateFilter));
        	}
        	else{
        		throw new IOException("warning :can't find EasyCacheConfig.properties");
        	}
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String configParseGetDbType(){
    	if(!dbTypeIsParsed){
    		try{
    			Properties prop = new Properties();  
            	InputStream in = ConfigParser.class.getResourceAsStream(configFilePath);
            	if(in != null){
            		dbTypeIsParsed = true;
            		prop.load(in);
            		dbType = prop.getProperty("dbType");
            		if(dbType != null){
            			dbType = dbType.toLowerCase();
            		}
            		else{
            			throw new IOException("Warning: dbType is null.");
            		}
                    return dbType;
            	}
            	else{
            		throw new IOException("warning :can't find EasyCacheConfig.properties");
            	}
    		} catch (IOException e) {
    			e.printStackTrace();
    			return null;
    		}
    	}
    	else{
    		return dbType;
    	}
    }
    
    public String configParseGetDsName(){
    	String dsName;
    	String dsType = ConfigParser.getInstance().configParseGetDbType();
    	if(dsType.equals("oracle")){
    		dsName = "java:comp/env/jdbc/Oracle";
    	}
    	else if(dsType.equals("mysql")){
    		dsName = "java:comp/env/jdbc/MySQL";
    	}
    	else if(dsType.equals("shentong")){
    		dsName = "jdbc/ShenTong";
    	}
    	else if(dsType.equals("dameng")){
    		dsName = "jdbc/DaMeng";
    	}
    	else{
    		dsName = null;
    	}
    	return dsName;
    }
    
    public static void queryResultCacheConfigParse(){
    	try{
    		Properties prop = new Properties();  
//        	File file1 = new File(Loader.class.getResource("/").getPath()+ "../config.properties");
//        	System.out.println(file1.getAbsolutePath());
        	InputStream in = ConfigParser.class.getResourceAsStream(configFilePath);
        	if(in != null){
        		prop.load(in);
        		String cacheSwitch = prop.getProperty("cacheSwitch");
        		if(cacheSwitch != null){
        			cacheSwitch = cacheSwitch.toLowerCase();
    			}
        		else{
        			throw new IOException("Warning: cacheSwitch is null.");
        		}
        		String sqlToCache = prop.getProperty(IMDGString.SQLTOCACHE);
        		if(sqlToCache != null){
        			sqlToCache = sqlToCache.toLowerCase();
    			}
        		HazelcastExecutor.setCacheSwitch(cacheSwitch);
                HazelcastExecutor.setSqlToCache(stringSplit(sqlToCache));
        	}
        	else{
        		throw new IOException("warning :can't find EasyCacheConfig.properties");
        	}
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String[] stringSplit(String filter){
    	if(filter == null || filter.equals("")){
    		return null;
    	}
    	else{
    		return filter.trim().split("\\$#@");
    	}
    }
    
}
