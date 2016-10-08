package com.hazelcast.config;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.Portable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author zhaohuiliu
 *
 */
public class IdGeneratorConfig implements DataSerializable {

	public final static long DEFAULT_ID_SEED = 0;
	public final static String DEFAULT_P_NAME = "default";

	private long idSeed = DEFAULT_ID_SEED;//the value of the first id
	private String name = DEFAULT_P_NAME;//the name of "persistence" id-seed belongs to

	public IdGeneratorConfig() {
	}
	
	public IdGeneratorConfig(String name) {
		this.name = name;
	}

	public IdGeneratorConfig(IdGeneratorConfig config) {
		this.idSeed = config.idSeed;
		this.name = config.name;
	}

	public long getIdSeed() {
		return idSeed;
	}

	public void setIdSeed(long idSeed) {
		this.idSeed = idSeed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(idSeed);
        out.writeUTF(name);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		name = in.readUTF();
		idSeed = in.readLong();
	}

}
