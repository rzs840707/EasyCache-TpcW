package com.hazelcast.persistance;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

/**
 * @author zhaoxin
 * 
 */ 
public class PersistanceConfig implements Portable {

	private boolean enabled = true;

	private String driverName = null;

	private String url = null;

	private int writeDelay = 0;
	
	private long persistencyKeyPeriod = 0;

	private Set<String> obmitMap = new HashSet<String>(5);

	public PersistanceConfig() {
	}

	public PersistanceConfig(PersistanceConfig config) {
		enabled = config.isEnabled();
		driverName = config.getDriverName();
		url = config.getUrl();
		writeDelay = config.getWriteDelay();
		persistencyKeyPeriod = config.getPersistencyKeyPeriod();
		for (String mapName : config.getObmitMap()) {
			obmitMap.add(mapName);
		}
	}

	@Override
	public int getFactoryId() {
		return SerializationConfig.persistanceConfigFactoryId;
	}

	@Override
	public int getClassId() {
		return SerializationConfig.persistanceConfigClassId;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeBoolean("enabled", enabled);
		writer.writeUTF("driverName", driverName);
		writer.writeUTF("url", url);
		writer.writeInt("writeDelay", writeDelay);
		writer.writeLong("persistencyKeyPeriod", persistencyKeyPeriod);
		writer.writeInt("obmitMapSize", obmitMap.size());

		int i = 0;
		for (String name : obmitMap) {
			writer.writeUTF("obmitMap" + i++, name);
		}
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		enabled = reader.readBoolean("enabled");
		driverName = reader.readUTF("driverName");
		url = reader.readUTF("url");
		writeDelay = reader.readInt("writeDelay");
		persistencyKeyPeriod = reader.readLong("persistencyKeyPeriod");
		int num = reader.readInt("obmitMapSize");
		for (int i = 0; i != num; ++i) {
			obmitMap.add(reader.readUTF("obmitMap" + i));
		}
	}

//	public void writeData(DataOutput out) throws IOException {
//		out.writeBoolean(enabled);
//		out.writeUTF(driverName);
//		out.writeUTF(url);
//		out.writeInt(writeDelay);
//		out.writeInt(obmitMap.size());
//		for (String name : obmitMap) {
//			out.writeUTF(name);
//		}
//	}
//
//	public void readData(DataInput in) throws IOException {
//		enabled = in.readBoolean();
//		driverName = in.readUTF();
//		url = in.readUTF();
//		writeDelay = in.readInt();
//		int num = in.readInt();
//		for (int i = 0; i != num; ++i) {
//			obmitMap.add(in.readUTF());
//		}
//	}

	public void addObmitMap(String name) {
		obmitMap.add(name);
	}

	public boolean isObmitMap(String name) {
		return obmitMap.contains(name);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getWriteDelay() {
		return writeDelay;
	}

	public void setWriteDelay(int writeDelay) {
		this.writeDelay = writeDelay;
	}

	public long getPersistencyKeyPeriod() {
		return persistencyKeyPeriod;
	}

	public void setPersistencyKeyPeriod(long persistencyKeyPeriod) {
		this.persistencyKeyPeriod = persistencyKeyPeriod;
	}
	
	public Set<String> getObmitMap() {
		return obmitMap;
	}

	public void setObmitMap(Set<String> obmitMap) {
		this.obmitMap = obmitMap;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("PersistanceConfig [enabled="
				+ enabled + ",driverName=" + driverName + ", url=" + url
				+ ", writeDelay=" + writeDelay + ",persistencyKeyPeriod=" + persistencyKeyPeriod + ", obmitMap=(");
		for (String mapName : obmitMap) {
			sb.append(mapName);
		}
		sb.append(")]");
		return sb.toString();
	}

}
