package com.hazelcast.serializer.entity;

import java.io.IOException;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.persistance.SerializationConfig;

public class Human implements Portable {
	private String id;
	private String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	} 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public int getFactoryId() {
		return SerializationConfig.humanFactoryId;
	}
	@Override
	public int getClassId() {
		return SerializationConfig.humanConfigClassId;
	}
	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF("id", id);
		writer.writeUTF("name", name);
	}
	@Override
	public void readPortable(PortableReader reader) throws IOException {
		id = reader.readUTF("id");
		name = reader.readUTF("name");
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("id : " + id + "\n");
		sb.append("name: " + name + "\n");
		return sb.toString();
	}
	
}
