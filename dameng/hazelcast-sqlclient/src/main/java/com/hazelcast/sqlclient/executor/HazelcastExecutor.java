package com.hazelcast.sqlclient.executor;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import com.hazelcast.config.Config;
import com.hazelcast.config.TypeSerializerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.serializer.entity.HazelcastObject;
import com.hazelcast.sqlclient.BeanGeneratorFactory;
import com.hazelcast.sqlclient.HazelcastDatabaseMetaData;
import com.hazelcast.sqlclient.TableMetaData;
import com.hazelcast.sqlclient.jdbc.IMDGResultSet;
import com.hazelcast.sqlclient.serializer.KryoSerializer;
//import com.hazelcast.sqlclient.serializer.KryoSerializerForImdgResultSet;
import com.hazelcast.sqlclient.type.SqlKind;

public class HazelcastExecutor extends Executor{
	private static CCJSqlParserManager parserManager = new CCJSqlParserManager();
	private static BeanGeneratorFactory beanGeneratorFactory = new BeanGeneratorFactory();
//	public static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
	private HazelcastDatabaseMetaData hazelcastDatabaseMetaData = new HazelcastDatabaseMetaData();
	private static HazelcastInstance hazelcast = null;
	
	//jiang yong 2014-10-14 SqlFilter
	private static String[] selectFilterArray;
	private static String[] insertFilterArray;
	private static String[] deleteFilterArray;
	private static String[] updateFilterArray;
	private static Set<String> sqlToCacheSet = new HashSet<String>();
	private static String cacheSwitch = null;
	
	public static void setSqlFilter(String[] selectFilterArray, String[] insertFilterArray, String[] deleteFilterArray, String[] updateFilterArray){
		HazelcastExecutor.selectFilterArray = selectFilterArray;
		HazelcastExecutor.insertFilterArray = insertFilterArray;
		HazelcastExecutor.deleteFilterArray = deleteFilterArray;
		HazelcastExecutor.updateFilterArray = updateFilterArray;
	}
	
	//jiang yong 2014-10-31 query result cache
	public static void setSqlToCache(String[] sqlToCacheArray){
		for(String sql : sqlToCacheArray){
			sqlToCacheSet.add(sql);
		}
//		for(String sql : sqlToCacheSet){
//			System.out.println(sql);
//		}
	}
	
	public static void setCacheSwitch(String cacheSwitch) {
		HazelcastExecutor.cacheSwitch = cacheSwitch;
	}
	
	public String getCacheSwitch(){
		return HazelcastExecutor.cacheSwitch;
	}
	// jiang yong done
	
	public HazelcastExecutor() {
		super();
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
		}
	}
	
	public void configHazelcast(String hazelcastUrl){
	}	
	
	@Override
	public SqlKind judgeSQLKind(String sql){
		if(sql.startsWith("insert")||sql.startsWith("INSERT")){
			return SqlKind.INSERT;
		}
		if(sql.startsWith("delete")||sql.startsWith("DELETE")){
			return SqlKind.DELETE;
		}
		if(sql.startsWith("update")||sql.startsWith("UPDATE")){
			return SqlKind.UPDATE;
		}
		if(sql.startsWith("select")||sql.startsWith("SELECT")){
			return SqlKind.SELECT;
		}
		return SqlKind.NULL;
	}	
	
	@Override
	public int executeInsert(String insertSentence, IMDGResultSet hrs) throws SQLException {
		Insert insert = null;
		try {
			insert = (Insert) parserManager.parse(new StringReader(insertSentence));
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		String tableName = insert.getTable().getName().toLowerCase();
		List<String> attributeNameList = new ArrayList<String>();
		List<String> attributeValueList = new ArrayList<String>();

		int columnSize = insert.getColumns().size();
		for(int i = 0; i < columnSize; i ++){
			String attributeName = ((Column) insert.getColumns().get(i)).getColumnName().toLowerCase();
			AttributeExpressionVisitor expressionVisitor = new AttributeExpressionVisitor();
			((Expression)((ExpressionList) insert.getItemsList()).getExpressions().get(i)).accept(expressionVisitor);
			String attributeValue = null;
			if(expressionVisitor.getAttributeValue() != null){
				attributeValue = expressionVisitor.getAttributeValue().toString();
			}			
			attributeNameList.add(attributeName);
			attributeValueList.add(attributeValue);
		}
		return this.executeInsertImpl(tableName, attributeNameList, attributeValueList, hrs);
	}	
	
	@Override
	public boolean whetherInsertCached(String key) throws SQLException {
		InsertParserResult insertParserResult = SQLParserResultFactory.getInsertParserResult(key);
		if(insertParserResult != null){
			return true;
		}		
		return false;
	}

	@Override
	public int executeInsertPre(String key, String insertSentence, IMDGResultSet hrs) throws SQLException {
		Insert insert = null;
		try {
			insert = (Insert) parserManager.parse(new StringReader(insertSentence));
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		String tableName = insert.getTable().getName().toLowerCase();
		InsertParserResult insertParserResult = new InsertParserResult();
		insertParserResult.setTableName(tableName);
		SQLParserResultFactory.setInsertParserResult(key, insertParserResult);
		
		return this.executeInsert(insertSentence, hrs);
	}

	@Override
	public int executeInsertCache(String key, List<String> attributeNameList, List<String> attributeValueList, IMDGResultSet hrs) throws SQLException {
		InsertParserResult insertParserResult = SQLParserResultFactory.getInsertParserResult(key);
		if(insertParserResult == null){
			throw new SQLException("this insert sentence is not cached!");
		}
		String tableName = insertParserResult.getTableName();
		return executeInsertImpl(tableName, attributeNameList, attributeValueList, hrs);
	}
	
	private int executeInsertImpl(String tableName, List<String> attributeNameList, List<String> attributeValueList, IMDGResultSet hrs) throws SQLException {
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
		if(tableMetaData == null){
			throw new SQLException("table not exist!");
		}		
//      ArrayList<String> columnNameList = tableMetaData.getColumnNameList();
        @SuppressWarnings("rawtypes")
		List<Class> columnClassList = tableMetaData.getColumnClassList();
        List<String> columnTypeList = new ArrayList<String>();
        for(int i = 0; i < columnClassList.size(); i++){
        	columnTypeList.add(columnClassList.get(i).getName());
        }
//      hrs.addMetaData(columnNameList,columnTypeList, null, null);
        
		BeanGenerator beanGenerator = beanGeneratorFactory.getBeanGenerator(tableName);
		if(beanGenerator == null){
			/*
			beanGenerator = new BeanGenerator();
			beanGenerator.setSuperclass(HazelcastObject.class);
			int columnSize = insert.getColumns().size();
			for(int i = 0; i < columnSize; i ++){
				String attributeName = ((Column) insert.getColumns().get(i)).getColumnName();
				Class myClass = ((ExpressionList) insert.getItemsList()).getExpressions().get(i).getClass();
				beanGenerator.addProperty(attributeName,
						((Column) insert.getColumns().get(i)).getClass());
			}
			beanGeneratorFactory.setBeanGenerator(tableName, beanGenerator);
			*/
			throw new SQLException("there is no generator!");
		}

		HazelcastObject hazelcastObject = (HazelcastObject) beanGenerator.create();
		BeanMap beanMap = BeanMap.create(hazelcastObject);
		int columnSize = attributeNameList.size();
		String attributeName;
		String attributeValueStr;
		Object attributeValueObj = null;
		for(int i = 0; i < columnSize; i ++){
			attributeName = attributeNameList.get(i);
			attributeValueStr = attributeValueList.get(i);	
			if(attributeValueStr == null){
				attributeValueObj = null;
			}
			else{
				if(attributeValueStr.startsWith("'")) {
					attributeValueStr = attributeValueStr.substring(1,attributeValueStr.length() - 1);
				}
				
				@SuppressWarnings("rawtypes")
				Class columnClass = tableMetaData.getColumnClassByAttributeName(attributeName);
				if(columnClass.equals(Integer.class)){
					attributeValueObj = Integer.parseInt(attributeValueStr);
				} else if(columnClass.equals(String.class)){
					attributeValueObj = String.valueOf(attributeValueStr);
				} else if(columnClass.equals(java.sql.Date.class)){
					attributeValueObj = java.sql.Date.valueOf(attributeValueStr);
				} else if(columnClass.equals(java.sql.Timestamp.class)){
//					System.out.println("str: " + attributeValueStr);
					attributeValueObj = java.sql.Timestamp.valueOf(attributeValueStr);
				} else if(columnClass.equals(java.math.BigDecimal.class)){
					attributeValueObj = new BigDecimal(attributeValueStr);
				} else if(columnClass.equals(Double.class)) {
					attributeValueObj = Double.valueOf(attributeValueStr);
				}
				else{
					System.out.println("Warning: unsupported columnClass:" + columnClass);
				}
			}
			beanMap.put(attributeName, attributeValueObj);
		}
		
		IdGenerator idGenerator = hazelcast.getIdGenerator(tableName);
		long id = 0;
		String strid;
		if(tableMetaData.getPrimaryKeyListSize() == 1){
			String primaryAttributeName = tableMetaData.getPrimaryKeyList().get(0);
			id = idGenerator.newId();
			strid =  primaryAttributeName + "=";    		
    		strid += String.valueOf(id); 
    		hrs.addPrimaryKey(id, Long.class);
    		
    		@SuppressWarnings("rawtypes")
			Class attributeClass = tableMetaData.getColumnClassByAttributeName(primaryAttributeName);
    		Object primaryIdObj = null;
    		if(attributeClass.equals(Integer.class)){
				primaryIdObj = Integer.valueOf((int)id);
			}else if(attributeClass.equals(java.math.BigDecimal.class)){
				primaryIdObj = new BigDecimal(id);
			}else{
				throw new SQLException("not supported data type for primary key!");
			}
    		beanMap.put(primaryAttributeName, primaryIdObj);
		}
		else{
			strid = "";
			for(String primaryAttributeName : tableMetaData.getPrimaryKeyList()){
				strid += primaryAttributeName + "=" + beanMap.get(primaryAttributeName)+"$#@";				
//				@SuppressWarnings("rawtypes")
//				Class primaryKeyClass = tableMetaData.getColumnClassByAttributeName(primaryAttributeName);
//				hrs.addPrimaryKey(beanMap.get(primaryAttributeName), primaryKeyClass);
			}
		}
		hazelcastObject.setId(strid);		
		Map<String, HazelcastObject> myMap = hazelcast.getMap(tableName);
		myMap.put(strid, hazelcastObject);
		return 1;
	}
	
	
	
	@Override
	public int executeDelete(String deleteSentence) throws SQLException {
		QueryPredicate queryPredicate;
		Delete delete = null;
		try {
			delete = (Delete) parserManager.parse(new StringReader(deleteSentence));
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		String tableName = delete.getTable().getName();
		Expression whereExpression = delete.getWhere();
		if(whereExpression != null){
			WhereExpressionVisitor expressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(expressionVisitor);
			List<Predicate> predicateList = expressionVisitor.getPredicateList();
			if(!(predicateList.size() == 1)){
				throw new SQLException("where of delte sentence translated into several queries!");
			}
			if(!(predicateList.get(0) instanceof QueryPredicate) ){
				throw new SQLException("where of delte sentence translated into join query!");
			}
			queryPredicate = (QueryPredicate)predicateList.get(0);
		}
		else{
			throw new SQLException("delete without where!");
		}		
		return this.executeDeleteImpl(tableName, queryPredicate);
	}
	
	public boolean whetherDeleteCached(String key) throws SQLException{
		DeleteParserResult deleteParserResult = SQLParserResultFactory.getDeleteParserResult(key);
		if(deleteParserResult != null){
			return true;
		}		
		return false;
	}
	
	public int executeDeletePre(String key, String deleteSentence) throws SQLException{
		DeleteParserResult deleteParserResult = new DeleteParserResult();
		QueryPredicate queryPredicate;
		Delete delete = null;
		try {
			delete = (Delete) parserManager.parse(new StringReader(deleteSentence));
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		String tableName = delete.getTable().getName();
		Expression whereExpression = delete.getWhere();
		if(whereExpression != null){
			WhereExpressionVisitor expressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(expressionVisitor);
			List<Predicate> predicateList = expressionVisitor.getPredicateList();
			if(!(predicateList.size() == 1)){
				throw new SQLException("where of delete sentence translated into several queries!");
			}
			if(!(predicateList.get(0) instanceof QueryPredicate) ){
				throw new SQLException("where of delete sentence translated into join query!");
			}
			queryPredicate = (QueryPredicate)predicateList.get(0);
		}
		else{
			throw new SQLException("delete without where!");
		}
		deleteParserResult.setTableName(tableName);
		deleteParserResult.setQueryPredicate(queryPredicate);
		SQLParserResultFactory.setDeleteParserResult(key, deleteParserResult);		
		return this.executeDeleteImpl(tableName, queryPredicate);
	}
	
	public int executeDeleteCache(String key, List<String> attributeNameList, List<String> attributeValueList) throws SQLException{
		DeleteParserResult deleteParserResult = SQLParserResultFactory.getDeleteParserResult(key);
		if(deleteParserResult == null){
			throw new SQLException("this delete sentence is not cached!");
		}
		String tableName = deleteParserResult.getTableName();
		QueryPredicate queryPredicate = deleteParserResult.getQueryPredicate().duplicate();	
		queryPredicate.setAttributeValues(attributeNameList, attributeValueList);
		queryPredicate.setActive();
		return this.executeDeleteImpl(tableName, queryPredicate);
	}	
	
	private int executeDeleteImpl(String tableName, QueryPredicate queryPredicate)throws SQLException{
		IMap<String, HazelcastObject> myMap = hazelcast.getMap(tableName);
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
		if(tableMetaData == null){
			throw new SQLException("table not exist!");
		}
		String requiredKey = queryPredicate.getRequiredKey(tableMetaData.getPrimaryKeyList());			
		Set<String> toDeleteKeySet = null;
		if(requiredKey != null){
			myMap.remove(requiredKey);
			return 1;
		}
		else{
			toDeleteKeySet = myMap.keySet(new SqlPredicate(queryPredicate.getPredicate()));
			for(String toDeleteKey : toDeleteKeySet){
				myMap.remove(toDeleteKey);
			}
			return toDeleteKeySet.size();
		}
	}
	
	
	@Override
	public int executeUpdate(String updateSentence) throws SQLException {
		QueryPredicate queryPredicate;
		Update update = null;
		try {
			update = (Update) parserManager.parse(new StringReader(updateSentence));
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
		String tableName = update.getTable().getName();
		Expression whereExpression = update.getWhere();
		if(whereExpression != null){
			WhereExpressionVisitor expressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(expressionVisitor);
			List<Predicate> predicateList = expressionVisitor.getPredicateList();
			if(!(predicateList.size() == 1)){
				throw new SQLException("where of delte sentence translated into several queries!");
			}
			if(!(predicateList.get(0) instanceof QueryPredicate) ){
				throw new SQLException("where of delte sentence translated into join query!");
			}
			queryPredicate = (QueryPredicate)predicateList.get(0);
		}
		else{
			throw new SQLException("delete without where!");
		}
		
		List<String> setAttributeNameList = new ArrayList<String>();
		List<String> setAttributeValueList = new ArrayList<String>();
		
		int columnSize = update.getColumns().size();
		for(int i = 0; i < columnSize; i ++){
			String attributeName = ((Column) update.getColumns().get(i)).getColumnName();
			AttributeExpressionVisitor expressionVisitor = new AttributeExpressionVisitor();			
			((Expression)(update.getExpressions().get(i))).accept(expressionVisitor);
			String attributeValue = expressionVisitor.getAttributeValue().toString();
			setAttributeNameList.add(attributeName);
			setAttributeValueList.add(attributeValue);
		}
		return this.executeUpdateImpl(tableName, queryPredicate, setAttributeNameList, setAttributeValueList);
	}

	public boolean whetherUpdateCached(String key) throws SQLException{
		UpdateParserResult updateParserResult = SQLParserResultFactory.getUpdateParserResult(key);
		if(updateParserResult != null){
			return true;
		}		
		return false;
	}
	
	public int executeUpdatePre(String key, String updateSentence) throws SQLException{
		UpdateParserResult updateParserResult = new UpdateParserResult();
		
		QueryPredicate queryPredicate;
		Update update = null;
		try {
			update = (Update) parserManager.parse(new StringReader(updateSentence));
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
		String tableName = update.getTable().getName();
		Expression whereExpression = update.getWhere();
		if(whereExpression != null){
			WhereExpressionVisitor expressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(expressionVisitor);
			List<Predicate> predicateList = expressionVisitor.getPredicateList();
			if(!(predicateList.size() == 1)){
				throw new SQLException("where of delte sentence translated into several queries!");
			}
			if(!(predicateList.get(0) instanceof QueryPredicate) ){
				throw new SQLException("where of delte sentence translated into join query!");
			}
			queryPredicate = (QueryPredicate)predicateList.get(0);
		}
		else{
			throw new SQLException("delete without where!");
		}
		
		updateParserResult.setTableName(tableName);
		updateParserResult.setQueryPredicate(queryPredicate);
		SQLParserResultFactory.setUpdateParserResult(key, updateParserResult);
		
		List<String> setAttributeNameList = new ArrayList<String>();
		List<String> setAttributeValueList = new ArrayList<String>();
		
		int columnSize = update.getColumns().size();
		for(int i = 0; i < columnSize; i ++){
			String attributeName = ((Column) update.getColumns().get(i)).getColumnName();
			AttributeExpressionVisitor expressionVisitor = new AttributeExpressionVisitor();			
			((Expression)(update.getExpressions().get(i))).accept(expressionVisitor);
			String attributeValue = expressionVisitor.getAttributeValue().toString();
			setAttributeNameList.add(attributeName);
			setAttributeValueList.add(attributeValue);
		}
		return this.executeUpdateImpl(tableName, queryPredicate, setAttributeNameList, setAttributeValueList);
	}
	
	public int executeUpdateCache(String key, List<String> setAttributeNameList, List<String> setAttributeValueList, List<String> whereAttributeNameList, List<String> whereAttributeValueList) throws SQLException{
		UpdateParserResult updateParserResult = SQLParserResultFactory.getUpdateParserResult(key);
		if(updateParserResult == null){
			throw new SQLException("this update sentence is not cached!");
		}
		String tableName = updateParserResult.getTableName();
		QueryPredicate queryPredicate = updateParserResult.getQueryPredicate().duplicate();	
		queryPredicate.setAttributeValues(whereAttributeNameList, whereAttributeValueList);
		queryPredicate.setActive();
		return this.executeUpdateImpl(tableName, queryPredicate, setAttributeNameList, setAttributeValueList);
	}
	
	private int executeUpdateImpl(String tableName, QueryPredicate queryPredicate, List<String> setAttributeNameList, List<String> setAttributeValueList) throws SQLException{
		IMap<String, HazelcastObject> myMap = hazelcast.getMap(tableName);
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
		if(tableMetaData == null){
			throw new SQLException("table not exist!");
		}
		String requiredKey = queryPredicate.getRequiredKey(tableMetaData.getPrimaryKeyList());			
		Collection<HazelcastObject> toUpdateOnes = null;
		if(requiredKey != null){
			// it is not reasonable!!!!!!!!!!!!
			toUpdateOnes = new HashSet<HazelcastObject>();
			Object object= myMap.get(requiredKey);
			if(object instanceof HazelcastObject){
				toUpdateOnes.add((HazelcastObject) object);
			}
		}
		else{
			toUpdateOnes = myMap.values(new SqlPredicate(queryPredicate.getPredicate()));
		}

        @SuppressWarnings("rawtypes")
		List<Class> columnClassList = tableMetaData.getColumnClassList();
        List<String> columnTypeList = new ArrayList<String>();
        for(int i = 0; i < columnClassList.size(); i++){
        	columnTypeList.add(columnClassList.get(i).getName());
        }

		for(HazelcastObject hazelcastObject : toUpdateOnes){
			BeanMap beanMap = BeanMap.create(hazelcastObject);
			String attributeName;
			String attributeValueStr;
			Object attributeValueObj = null;
			int columnSize = setAttributeNameList.size();
			for(int i = 0; i < columnSize; i ++){
				attributeName = setAttributeNameList.get(i);
				attributeValueStr = setAttributeValueList.get(i);
				@SuppressWarnings("rawtypes")
				Class columnClass = tableMetaData.getColumnClassByAttributeName(attributeName);
				if(attributeValueStr.startsWith("'")) {
					attributeValueStr = attributeValueStr.substring(1,attributeValueStr.length() - 1);
				}
				if(columnClass.equals(Integer.class)){
					attributeValueObj = Integer.parseInt(attributeValueStr);
				} else if(columnClass.equals(String.class)){
					attributeValueObj = String.valueOf(attributeValueStr);
				} else if(columnClass.equals(java.sql.Date.class)){
					attributeValueObj = java.sql.Date.valueOf(attributeValueStr);
				} else if(columnClass.equals(java.sql.Timestamp.class)){
					attributeValueObj = java.sql.Timestamp.valueOf(attributeValueStr);
				} else if(columnClass.equals(java.math.BigDecimal.class)){
					attributeValueObj = new BigDecimal(attributeValueStr);
				} else if(columnClass.equals(Double.class) ){ 
					// zhaohui, jiangyong, 2014-8-22
					attributeValueObj = new Double(attributeValueStr);
				}
				else{
					System.out.println("Warning: unsupported columnClass:" + columnClass);
				}
				beanMap.put(attributeName.toLowerCase(), attributeValueObj);
			}
//			System.out.println("hazelcastObject.getId():" + hazelcastObject.getId());
			myMap.put(hazelcastObject.getId(), hazelcastObject);
		}
		return toUpdateOnes.size();
	}
	
	
	
	@Override
	public int executeSelect(String selectSentence, IMDGResultSet hrs) throws SQLException {
		String key;
		String temp = selectSentence.toLowerCase();
		int index = temp.indexOf(" where ");
		if(index == -1){ 
			key = temp;
		}
		else{
			key = temp.substring(0, index)+" where";
		}	
		Map<String, String> tableNameAliasMap;
		List<Item> itemList;
		List<Predicate> predicateList;
		
		SelectParserResult selectParserResult = SQLParserResultFactory.getSelectParserResult(key);
		if(selectParserResult != null){
			tableNameAliasMap = selectParserResult.getTableNameAliasMap();
			itemList = selectParserResult.getItemList();
			predicateList = selectParserResult.duplicatePredicateList(selectSentence);
			executeSelectImpl(itemList, tableNameAliasMap, predicateList, hrs);
			return 0;
		}
		
		
		selectParserResult = new SelectParserResult();
		PlainSelect plainSelect = null;
		try {
			plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(selectSentence))).getSelectBody();
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
		
		tableNameAliasMap = new HashMap<String, String>();
		String tableName =  ((Table) plainSelect.getFromItem()).getName();
		tableName = (tableName == null ? tableName : tableName.toLowerCase());
		String tableNameAlias = ((Table) plainSelect.getFromItem()).getAlias();
		tableNameAlias = tableNameAlias == null ? tableNameAlias : tableNameAlias.toLowerCase();
		if(tableNameAlias==null){
			tableNameAliasMap.put(tableName, tableName);
		}
		else{
			tableNameAliasMap.put(tableNameAlias, tableName);
		}		
		if(plainSelect.getJoins() != null){
			int joinNumber = plainSelect.getJoins().size();
			for(int i = 0; i < joinNumber; i++){
				tableName = ((Table) ((Join) plainSelect.getJoins().get(i)).getRightItem()).getName();
				tableName = tableName == null ? tableName : tableName.toLowerCase();
				tableNameAlias = ((Join) plainSelect.getJoins().get(i)).getRightItem().getAlias();
				tableNameAlias = tableNameAlias == null ? tableNameAlias : tableNameAlias.toLowerCase();
				if(tableNameAlias==null){
					tableNameAliasMap.put(tableName, tableName);
				}
				else{
					tableNameAliasMap.put(tableNameAlias, tableName);
				}
			}
		}
		selectParserResult.setTableNameAliasMap(tableNameAliasMap);

		itemList = new ArrayList<Item>();
		@SuppressWarnings("rawtypes")
		List selectItems = plainSelect.getSelectItems();
		if(selectItems.size() == 1 && selectItems.get(0).toString().equals("*")){				
			for(String interTableName : tableNameAliasMap.keySet()){
				TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(interTableName);
				for(int j = 0; j < tableMetaData.getColumnSize(); j++){
					String attributeName = tableMetaData.getColumnName(j);
					Item item = new Item(interTableName+"."+attributeName);
					itemList.add(item);
				}
			}
		}
		else{
			for(int i = 0; i < selectItems.size(); i++){
				String tableAtrribute = hazelcastDatabaseMetaData.getTableAttribute(selectItems.get(i).toString());
				if(tableAtrribute != null){
					itemList.add(new Item(tableAtrribute.toLowerCase()));
				}
			}
		}
		selectParserResult.setItemList(itemList);

		Expression whereExpression = plainSelect.getWhere();
		if(whereExpression != null){
			WhereExpressionVisitor expressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(expressionVisitor);
			predicateList = expressionVisitor.getPredicateList();
		}
		else{
			predicateList = null;
		}
		selectParserResult.setPredicateList(predicateList);
		SQLParserResultFactory.setSelectParserResult(key, selectParserResult);
		
		executeSelectImpl(itemList, tableNameAliasMap, predicateList, hrs);
		return 0;
	}

	@Override
	public boolean whetherSelectCached(String key) throws SQLException {		
		SelectParserResult selectParserResult = SQLParserResultFactory.getSelectParserResult(key);
		if(selectParserResult != null){
			return true;
		}		
		return false;
	}	

	@Override
	public int executeSelectPre(String key, String selectSentence, IMDGResultSet hrs) throws SQLException {
		Map<String, String> tableNameAliasMap;
		List<Item> itemList;
		List<Predicate> predicateList;
		
		SelectParserResult selectParserResult = new SelectParserResult();
		PlainSelect plainSelect = null;
		try {
			plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(selectSentence))).getSelectBody();
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
		
		tableNameAliasMap = new HashMap<String, String>();
		String tableName =  ((Table) plainSelect.getFromItem()).getName();
		tableName = (tableName == null ? tableName : tableName.toLowerCase());
		String tableNameAlias = ((Table) plainSelect.getFromItem()).getAlias();
		tableNameAlias = tableNameAlias == null ? tableNameAlias : tableNameAlias.toLowerCase();
		if(tableNameAlias==null){
			tableNameAliasMap.put(tableName, tableName);
		}
		else{
			tableNameAliasMap.put(tableNameAlias, tableName);
		}		
		if(plainSelect.getJoins() != null){
			int joinNumber = plainSelect.getJoins().size();
			for(int i = 0; i < joinNumber; i++){
				tableName = ((Table) ((Join) plainSelect.getJoins().get(i)).getRightItem()).getName();
				tableName = tableName == null ? tableName : tableName.toLowerCase();
				tableNameAlias = ((Join) plainSelect.getJoins().get(i)).getRightItem().getAlias();
				tableNameAlias = tableNameAlias == null ? tableNameAlias : tableNameAlias.toLowerCase();
				if(tableNameAlias==null){
					tableNameAliasMap.put(tableName, tableName);
				}
				else{
					tableNameAliasMap.put(tableNameAlias, tableName);
				}
			}
		}
		selectParserResult.setTableNameAliasMap(tableNameAliasMap);

		itemList = new ArrayList<Item>();
		@SuppressWarnings("rawtypes")
		List selectItems = plainSelect.getSelectItems();
		if(selectItems.size() == 1 && selectItems.get(0).toString().equals("*")){				
			for(String interTableName : tableNameAliasMap.keySet()){
				TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(interTableName);
				for(int j = 0; j < tableMetaData.getColumnSize(); j++){
					String attributeName = tableMetaData.getColumnName(j);
					Item item = new Item(interTableName+"."+attributeName);
					itemList.add(item);
				}
			}
		}
		else{
			for(int i = 0; i < selectItems.size(); i++){
				String tableAtrribute = hazelcastDatabaseMetaData.getTableAttribute(selectItems.get(i).toString());
				if(tableAtrribute != null){
					itemList.add(new Item(tableAtrribute.toLowerCase()));
				}
			}
		}
		selectParserResult.setItemList(itemList);

		Expression whereExpression = plainSelect.getWhere();
		if(whereExpression != null){
			WhereExpressionVisitor expressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(expressionVisitor);
			predicateList = expressionVisitor.getPredicateList();
		}
		else{
			predicateList = null;
		}
		selectParserResult.setPredicateList(predicateList);
		SQLParserResultFactory.setSelectParserResult(key, selectParserResult);
		
		executeSelectImpl(itemList, tableNameAliasMap, predicateList, hrs);
		return 0;
	}
	
	@Override
	public int executeSelectCache(String key, List<String> attributeNameList, List<String> attributeValueList, IMDGResultSet hrs) throws SQLException {
		Map<String, String> tableNameAliasMap;
		List<Item> itemList;
		List<Predicate> predicateList;
		
		SelectParserResult selectParserResult = SQLParserResultFactory.getSelectParserResult(key);
		if(selectParserResult != null){
			tableNameAliasMap = selectParserResult.getTableNameAliasMap();
			itemList = selectParserResult.getItemList();
			predicateList = selectParserResult.duplicatePredicateList(attributeNameList, attributeValueList);
			executeSelectImpl(itemList, tableNameAliasMap, predicateList, hrs);
			return 0;
		}
		else{
			throw new SQLException("this sentence is not cached!");
		}
	}	
	
	private int executeSelectImpl(List<Item> itemList, Map<String, String> tableNameAliasMap, List<Predicate> predicateList,  IMDGResultSet hrs) throws SQLException {		
		if(predicateList == null && tableNameAliasMap.size() == 1){
			String tableNameString = tableNameAliasMap.keySet().toArray()[0].toString();
			this.executeQueryPredicate(itemList, tableNameString, hrs);
			return 0;
		}
		if(predicateList == null && tableNameAliasMap.size() > 1){
			throw new SQLException("not supported now");
		}		
		
		
		QueryPredicate queryPredicate = null;		
		for(int i = 0; i < predicateList.size(); i++){
			if((predicateList.get(i) instanceof QueryPredicate) && predicateList.get(i).isActive()){
				queryPredicate = (QueryPredicate) predicateList.get(i);
				predicateList.get(i).setInactive();
				break;
			}
		}
		if(queryPredicate == null){
			throw new SQLException("there is no query predicate, not supported now");
		}
		Collection<HazelcastObject> currentResultSet = this.executeQueryPredicate(itemList, queryPredicate, tableNameAliasMap, hrs);
		if(currentResultSet == null || currentResultSet.size() == 0){
			hrs.clear();
			return 0;
		}
		String currentTableName = queryPredicate.getTableName();
		int activePredicateNumber = 0; 
		for(int i = 0; i < predicateList.size(); i++){
			if(predicateList.get(i).isActive()){
				activePredicateNumber++;
			}
		}
		for(int i = 0; i < activePredicateNumber; i++){
			JoinPredicate joinPredicate = getNextJoinPredicate(predicateList, currentTableName);
			currentResultSet = this.executeJoinPredicate(itemList, joinPredicate, currentResultSet, currentTableName, tableNameAliasMap, hrs);
			if(currentResultSet == null || currentResultSet.size() == 0){
				hrs.clear();
				return 0;
			}
			currentTableName = (joinPredicate.getTableNameOne().equals(currentTableName) ? joinPredicate.getTableNameTwo() : joinPredicate.getTableNameOne());
		}
		return 0;
	}
	
	
	private void executeQueryPredicate(List<Item> itemList, String tableName,  IMDGResultSet hrs){		
		//System.out.println("the query predicate is : no where predicate");
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(tableName);
		IMap<String, HazelcastObject> myMap = hazelcast.getMap(tableName);
		Collection<HazelcastObject> resultSet = myMap.values();
		
		List<String> requiredAttributeNameList = new ArrayList<String>();
		for(Item item : itemList){
			if(item.getTableName().equals(tableName)){
				String attributeName = item.getAttributeName();
				@SuppressWarnings("rawtypes")
				Class attributeClass = tableMetaData.getColumnClassByAttributeName(attributeName);
				requiredAttributeNameList.add(attributeName);
				hrs.addMetaDataCol(attributeName, attributeClass, tableName, null);
			}
		}
		
		List<List<Object>> partialResultList = new ArrayList<List<Object>>();
		for(int i = 0; i < requiredAttributeNameList.size(); i++){
			partialResultList.add(new ArrayList<Object>());
		}
		for(HazelcastObject hazelcastObject : resultSet){
			for(int i = 0; i < requiredAttributeNameList.size(); i++){
				String requiredAttributeName = requiredAttributeNameList.get(i);
				List<Object> columnList = partialResultList.get(i);
				Field field = null;				
				try {
					field = hazelcastObject.getClass().getDeclaredField("$cglib_prop_"+requiredAttributeName);
					field.setAccessible(true);
					columnList.add(field.get(hazelcastObject));
					//System.out.println(requiredAttributeName + "\t" + field.get(hazelcastObject));
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}		
		
		for(int i = 0; i < partialResultList.size(); i++){
			try {
				hrs.addValueDataColumn(partialResultList.get(i));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
		
		//System.out.println();
		//System.out.println();
		//System.out.println();
	}	
	
	private Collection<HazelcastObject> executeQueryPredicate(List<Item> itemList, QueryPredicate queryPredicate, Map<String, String> tableNameAliasMap, IMDGResultSet hrs) throws SQLException{		
		//System.out.println("the query predicate is : " + queryPredicate);
		String tableName = queryPredicate.getTableName();
		String realTableName = tableNameAliasMap.get(tableName);
		IMap<String, HazelcastObject> myMap = hazelcast.getMap(realTableName);
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(realTableName);
		String requiredKey = queryPredicate.getRequiredKey(tableMetaData.getPrimaryKeyList());
		ArrayList<String> requiredKeyList = requiredKey == null ? queryPredicate.getRequiredKeyList(tableMetaData.getPrimaryKeyList()):null;

		Collection<HazelcastObject> resultSet = new HashSet<HazelcastObject>();
		if(requiredKey != null){
			// it is not reasonable!!!!!!!!!!!!
			resultSet = new HashSet<HazelcastObject>();
			Object object= myMap.get(requiredKey);
			if(object instanceof HazelcastObject){
				resultSet.add((HazelcastObject) object);
			}
		}
		else if(requiredKeyList != null){
			// it is not reasonable!!!!!!!!!!!!
			resultSet = new HashSet<HazelcastObject>();
			for(String key : requiredKeyList){
				Object object= myMap.get(key);
				if(object instanceof HazelcastObject){
					resultSet.add((HazelcastObject) object);
				}
			}
		}
		else{
			SqlPredicate sqlPredicate = new SqlPredicate(queryPredicate.getPredicate());
			Set<HazelcastObject> collection = (Set<HazelcastObject>) myMap.values(sqlPredicate);
			Iterator<HazelcastObject> it = (Iterator<HazelcastObject>) collection.iterator();
			while(it.hasNext()) {
				HazelcastObject object = it.next();
				resultSet.add(object);
			}
			//interval = System.currentTimeMillis() - interval;
			//System.out.println(" the interval of the map operation is : " + interval);
		}
		
		List<String> requiredAttributeNameList = new ArrayList<String>();
		for(Item item : itemList){
			if(item.getTableName().equals(tableName)){
				String attributeName = item.getAttributeName();
				@SuppressWarnings("rawtypes")
				Class attributeClass = tableMetaData.getColumnClassByAttributeName(attributeName);
				requiredAttributeNameList.add(attributeName);
				hrs.addMetaDataCol(attributeName, attributeClass, realTableName, null);
			}
		}
		
		List<List<Object>> partialResultList = new ArrayList<List<Object>>();
		for(int i = 0; i < requiredAttributeNameList.size(); i++){
			partialResultList.add(new ArrayList<Object>());
		}		

		for(HazelcastObject hazelcastObject : resultSet){
			for(int i = 0; i < requiredAttributeNameList.size(); i++){
				String requiredAttributeName = requiredAttributeNameList.get(i);
				List<Object> columnList = partialResultList.get(i);
				Field field = null;				
				try {
					field = hazelcastObject.getClass().getDeclaredField("$cglib_prop_"+requiredAttributeName);
					field.setAccessible(true);					
					columnList.add(field.get(hazelcastObject));	
					//System.out.println(requiredAttributeName + "\t" + field.get(hazelcastObject));
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(int i = 0; i < partialResultList.size(); i++){
			hrs.addValueDataColumn(partialResultList.get(i));
		}		

		//System.out.println();
		//System.out.println();
		//System.out.println();
		return resultSet;
	}
	
	private Collection<HazelcastObject> executeQueryPredicate(QueryPredicate queryPredicate, Map<String, String> tableNameAliasMap) throws SQLException{		
		//System.out.println("the query predicate is : " + queryPredicate);
		String tableName = queryPredicate.getTableName();
		String realTableName = tableNameAliasMap.get(tableName);
		IMap<String, HazelcastObject> myMap = hazelcast.getMap(realTableName);
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(realTableName);
		String requiredKey = queryPredicate.getRequiredKey(tableMetaData.getPrimaryKeyList());			
		Collection<HazelcastObject> resultSet = null;
		if(requiredKey != null){
			// it is not reasonable!!!!!!!!!!!!
			resultSet = new HashSet<HazelcastObject>();
			Object object= myMap.get(requiredKey);
			if(object instanceof HazelcastObject){
				resultSet.add((HazelcastObject) object);
			}
		}
		else{
			resultSet = myMap.values(new SqlPredicate(queryPredicate.getPredicate()));
		}
		return resultSet;
	}
	
	
	private JoinPredicate getNextJoinPredicate(List<Predicate> predicateList, String currentTableName) throws SQLException{
		for(int i = 0; i < predicateList.size(); i++){
			if((predicateList.get(i) instanceof QueryPredicate) && predicateList.get(i).isActive()){
				throw new SQLException("doesn't support now");
			}
			else{
				if(!predicateList.get(i).isActive()){
					continue;
				}
				if(((JoinPredicate) predicateList.get(i)).getTableNameOne().equals(currentTableName) ||
						((JoinPredicate) predicateList.get(i)).getTableNameTwo().equals(currentTableName) ){
					JoinPredicate joinPredicate = (JoinPredicate) predicateList.get(i);
					predicateList.get(i).setInactive();
					return joinPredicate;
				}
			}
		}
		throw new SQLException("doesn't support now");
	}
	
	private Collection<HazelcastObject> executeJoinPredicate(List<Item> itemList, JoinPredicate joinPredicate, Collection<HazelcastObject> currentResultSet, 
			String currentTableName, Map<String, String> tableNameAliasMap, IMDGResultSet hrs) throws SQLException{		
		//System.out.println("the join predicate is : " + joinPredicate);
		Collection<HazelcastObject> resultSet;
		List<HazelcastObject> resultSetList = new ArrayList<HazelcastObject>();
		
		String tableName = "";
		for(HazelcastObject hazelcastObject : currentResultSet){
			QueryPredicate queryPredicate = this.generateQueryPredicate(joinPredicate, currentTableName, hazelcastObject);
			if(currentResultSet.size()<=1){
				resultSet = this.executeQueryPredicate(itemList, queryPredicate, tableNameAliasMap, hrs);
				return resultSet;
			}
			tableName = queryPredicate.getTableName();
			Collection<HazelcastObject> tempResultSet = this.executeQueryPredicate(queryPredicate, tableNameAliasMap);
			resultSetList.addAll(tempResultSet);
		}
		
		String realTableName = tableNameAliasMap.get(tableName);
		TableMetaData tableMetaData = hazelcastDatabaseMetaData.getTableMetaData(realTableName);
		List<String> requiredAttributeNameList = new ArrayList<String>();
		for(Item item : itemList){
			if(item.getTableName().equals(tableName)){
				String attributeName = item.getAttributeName();
				@SuppressWarnings("rawtypes")
				Class attributeClass = tableMetaData.getColumnClassByAttributeName(attributeName);
				requiredAttributeNameList.add(attributeName);
				hrs.addMetaDataCol(attributeName, attributeClass, realTableName, null);
			}
		}
		
		List<List<Object>> partialResultList = new ArrayList<List<Object>>();
		for(int i = 0; i < requiredAttributeNameList.size(); i++){
			partialResultList.add(new ArrayList<Object>());
		}		

		for(HazelcastObject hazelcastObject : resultSetList){
			for(int i = 0; i < requiredAttributeNameList.size(); i++){
				String requiredAttributeName = requiredAttributeNameList.get(i);
				List<Object> columnList = partialResultList.get(i);
				Field field = null;				
				try {
					field = hazelcastObject.getClass().getDeclaredField("$cglib_prop_"+requiredAttributeName);
					field.setAccessible(true);					
					columnList.add(field.get(hazelcastObject));	
					//System.out.println(requiredAttributeName + "\t" + field.get(hazelcastObject));
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(int i = 0; i < partialResultList.size(); i++){
			hrs.addValueDataColumn(partialResultList.get(i));
		}

		return resultSetList;
	}
	
	
	private QueryPredicate generateQueryPredicate(JoinPredicate joinPredicate, String currentTableName, HazelcastObject hazelcastObject){
		QueryPredicate queryPredicate = new QueryPredicate();
		int priority = joinPredicate.getPriority();
		String tableName = null;
		String attributeName = null;
		String relationship = null;
		String tempAttributeName = null;
		String attributeValue = null;
		for(int i = 0; i < joinPredicate.getSubQueryNumber(); i++){
			if(joinPredicate.getTableNameOne().equals(currentTableName)){
				tableName = joinPredicate.getTableNameTwo();
				attributeName = joinPredicate.getAttributeTwo(i);
				relationship = joinPredicate.getChangedAlgebraicRelationship(i);
				tempAttributeName = joinPredicate.getAttributeOne(i);
			}
			else{
				tableName = joinPredicate.getTableNameOne();
				attributeName = joinPredicate.getAttributeOne(i);
				relationship = joinPredicate.getAlgebraicRelationship(i);
				tempAttributeName = joinPredicate.getAttributeOne(i);
			}
			
			Field field = null;				
			try {
				field = hazelcastObject.getClass().getDeclaredField("$cglib_prop_"+tempAttributeName);
				field.setAccessible(true);
				attributeValue = field.get(hazelcastObject).toString();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			queryPredicate.addSubQueryPredicate(null, priority, tableName, attributeName, relationship, attributeValue);
		}
		queryPredicate.setLogicalRelationshipList(joinPredicate.getLogicalRelationshipList());
		return queryPredicate;
	}

	//jiang yong 2014-10-14 SqlFilter
	@Override
	public boolean supportedInsert(String insertSentence) throws SQLException {
//		if (insertSentence.indexOf(" from ") != -1) {
//			return false;
//		}
		if(insertFilterArray != null){
			for(String insertSql : insertFilterArray){
				if(insertSentence.indexOf(insertSql) != -1){
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean supportedDelete(String deleteSentence) throws SQLException {
//		if (insertSentence.indexOf(" from ") != -1) {
//			return false;
//		}
		if(deleteFilterArray != null){
			for(String deleteSql : deleteFilterArray){
				if(deleteSentence.indexOf(deleteSql) != -1){
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean supportedUpdate(String updateSentence) throws SQLException {
		if(updateFilterArray != null){
			for(String updateSql : updateFilterArray){
				if(updateSentence.indexOf(updateSql) != -1){
					return false;
				}
			}
		}
		return true;
	}	
	
	@Override
	public boolean supportedSelect(String selectSentence){		
		
		if(selectFilterArray != null){
			for(String selectSql : selectFilterArray){
				if(selectSentence.indexOf(selectSql) != -1){
					return false;
				}
			}
		}
		if(selectSentence.indexOf("order by")!=-1){
			return false;
		}
		if(selectSentence.indexOf("group by")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" count(")!=-1 || selectSentence.indexOf(" count ")!=-1 ){
			return false;
		}
		if(selectSentence.indexOf(" top ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" limit ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" as ")!=-1){
			return false;
		}
		if(selectSentence.indexOf("rownum")!=-1){
			return false;
		}
		/*
		if(selectSentence.indexOf(" max( ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" min( ")!=-1){
			return false;
		}		
		if(selectSentence.indexOf(" distinct ")!=-1){
			return false;
		}			
		if(selectSentence.indexOf(" case ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" union ")!=-1){
			return false;
		}
		*/
		return true;
	}

	//jiang yong 2014-10-31 query result cache
	public boolean supportedQueryResultCache(String sql) throws SQLException {
		if(sqlToCacheSet != null){
			return sqlToCacheSet.contains(sql);
		}
		return false;
	}	
	//jiang yong done
	
	public boolean supportedSelect_old(String selectSentence){		
		/// wys: delete some supported sql
//		for index of c_uname
		if(selectSentence.indexOf("select c_passwd from customer where") != -1) {
			return false;
		}
		
//     for index of co_name
		if(selectSentence.indexOf("select co_id from country where") != -1) {
			return false;
		}
		
//		for index of c_uname
//		if(tempSelectSentence.indexOf("select * from customer, address, country where") != -1) {
//			return false;
//		}
		if(selectSentence.indexOf("select j.i_id,j.i_thumbnail from item i, item j where") != -1) {
			return false;
		}
		
//     for index of scl_sc_id		
//		if(selectSentence.indexOf("select * from shopping_cart_line, item where") != -1) {
//			return false;
//		}
		///
		if(selectSentence.indexOf("order by")!=-1){
			return false;
		}
		if(selectSentence.indexOf("group by")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" count(")!=-1 || selectSentence.indexOf(" count ")!=-1 ){
			return false;
		}
		if(selectSentence.indexOf(" top ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" limit ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" as ")!=-1){
			return false;
		}
		/*
		if(selectSentence.indexOf(" max( ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" min( ")!=-1){
			return false;
		}		
		if(selectSentence.indexOf(" distinct ")!=-1){
			return false;
		}			
		if(selectSentence.indexOf(" case ")!=-1){
			return false;
		}
		if(selectSentence.indexOf(" union ")!=-1){
			return false;
		}
		*/
		return true;
	}

}

