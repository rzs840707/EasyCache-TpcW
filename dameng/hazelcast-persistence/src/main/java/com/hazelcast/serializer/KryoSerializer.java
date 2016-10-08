package com.hazelcast.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.TypeSerializer;
import com.hazelcast.serializer.entity.HazelcastObject;

public class KryoSerializer implements TypeSerializer<HazelcastObject> {
	@Override
	public int getTypeId() {
		return 5;//no serializer
//		return 7;
	}

	@Override
	public void destroy() {
	}

	@Override
	public void write(ObjectDataOutput out, HazelcastObject object) throws IOException {
		Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(java.sql.Date.class, KryoDateSerializer.class);
		kryo.addDefaultSerializer(java.sql.Timestamp.class, KryoTimeStampSerializer.class);
		
		Output output = new Output((OutputStream)out);
		kryo.writeClassAndObject(output, object);
		output.flush();
	}

	@Override
	public HazelcastObject read(ObjectDataInput in) throws IOException {
		Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(java.sql.Date.class, KryoDateSerializer.class);
		kryo.addDefaultSerializer(java.sql.Timestamp.class, KryoTimeStampSerializer.class);
		
		Input input = new Input((InputStream)in);
		return (HazelcastObject) kryo.readClassAndObject(input);
	}
}
