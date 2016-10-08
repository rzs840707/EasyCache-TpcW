package com.hazelcast.sqlclient.type;

public class DataTypeCheck {
	public static DataType.Type check(String type) {
		if (type.equalsIgnoreCase("java.lang.Integer")) {
			return DataType.Type.INTEGER;
		} else if (type.equalsIgnoreCase("java.lang.Long")) {
			return DataType.Type.LONG;
		} else if (type.equalsIgnoreCase("java.lang.Float")) {
			return DataType.Type.FLOAT;
		} else if (type.equalsIgnoreCase("java.lang.Double")) {
			return DataType.Type.DOUBLE;
		} else if (type.equalsIgnoreCase("java.lang.String")) {
			return DataType.Type.STRING;
		} else if (type.equalsIgnoreCase("java.sql.Date")) {
			return DataType.Type.DATE;
		}else if (type.equalsIgnoreCase("java.sql.Time")) {
			return DataType.Type.TIME;
		} else if (type.equalsIgnoreCase("java.sql.Timestamp")) {
			return DataType.Type.TIMESTAMP;
		} else if (type.equalsIgnoreCase("java.lang.Boolean")) {
			return DataType.Type.BOOLEAN;
		} else if (type.equalsIgnoreCase("java.lang.Short")) {
			return DataType.Type.SHORT;
		} else if (type.equalsIgnoreCase("java.math.BigDecimal")) {
			return DataType.Type.DECIMAL;
		} else {
			return DataType.Type.UNKNOWN;
		}
	}
	public static String check(DataType.Type type) {
		switch (type) {
		case INTEGER:
			return "java.lang.Integer";
		case SHORT:
			return "java.lang.Short";
		case LONG:
			return "java.lang.Long";
		case BOOLEAN:
			return "java.lang.Boolean";
		case FLOAT:
			return "java.lang.Float";
		case DOUBLE:
			return "java.lang.Double";
		case DATE:
			return "java.sql.Date";
		case TIME:
			return "java.sql.Time";
		case TIMESTAMP:
			return "java.sql.TimeStamp";
		case STRING:
			return "java.lang.String";
		case DECIMAL:
			return "java.math.BigDecimal";
		default:
			return "UNKNOWN";
		}
	}
}
