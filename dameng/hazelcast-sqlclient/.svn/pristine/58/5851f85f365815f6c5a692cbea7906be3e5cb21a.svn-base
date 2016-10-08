package com.hazelcast.sqlclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.TypeSerializer;

public class HazelcastObjectSerializer implements TypeSerializer<HazelcastPortableObject> {
	public int getTypeId() {
		return 5;
	}

	public void destroy() {
	}

	public void write(ObjectDataOutput out, HazelcastPortableObject object)
			throws IOException {
		// TODO Auto-generated method stub
		Kryo kryo = new Kryo();
		Output output = new Output((OutputStream)out);
		kryo.writeObject(output, object);
		output.flush();
	}
	public HazelcastPortableObject read(ObjectDataInput in) throws IOException {
		// TODO Auto-generated method stub
		Kryo kryo = new Kryo();
		Input input = new Input((InputStream)in);
		return (HazelcastPortableObject) kryo.readClassAndObject(input);
	}
}
