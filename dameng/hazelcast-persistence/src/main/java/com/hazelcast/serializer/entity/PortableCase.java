package com.hazelcast.serializer.entity;

import java.io.IOException;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.serializer.GlobalClassID;

public class PortableCase implements Portable {
	public byte version = 100;
	public byte count = 0;
	@Override
	public int getFactoryId() {
		return GlobalClassID.PortableCaseID;
	}
	@Override
	public int getClassId() {
		return GlobalClassID.PortableCaseID;
	}
	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByte("version", version);
		writer.writeByte("count", count);
	}
	@Override
	public void readPortable(PortableReader reader) throws IOException {
		version = reader.readByte("version");
		count = reader.readByte("count");
	}
}
