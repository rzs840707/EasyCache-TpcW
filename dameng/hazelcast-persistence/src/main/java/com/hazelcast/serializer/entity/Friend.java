package com.hazelcast.serializer.entity;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

@SuppressWarnings("serial")
public class Friend implements DataSerializable {
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
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("id : " + id + "\n");
		sb.append("name: " + name + "\n");
		return sb.toString();
	}
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(id);
		out.writeUTF(name);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		id = in.readUTF();
		name = in.readUTF();
	}
	
}
