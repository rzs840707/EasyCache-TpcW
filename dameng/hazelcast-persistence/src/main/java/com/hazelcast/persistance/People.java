package com.hazelcast.persistance;

import java.io.IOException;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

public class People implements Portable{
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
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
		return SerializationConfig.peopleFactoryId;
	}
	@Override
	public int getClassId() {
		return SerializationConfig.peopleConfigClassId;
	}
	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeInt("id", id);
		writer.writeUTF("name", name);
	}
	@Override
	public void readPortable(PortableReader reader) throws IOException {
		id = reader.readInt("id");
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
