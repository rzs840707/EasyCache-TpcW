package com.hazelcast.sqlclient.serializer;

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
import com.hazelcast.sqlclient.serializer.KryoDateSerializer;
import com.hazelcast.sqlclient.serializer.KryoTimeStampSerializer;

public class KryoSerializer implements TypeSerializer<HazelcastObject> {
//	private Kryo kryo = null;
//	public KryoSerializer() {
//		kryo = new Kryo();
//		kryo.addDefaultSerializer(java.sql.Date.class, KryoDateSerializer.class);
//		kryo.addDefaultSerializer(java.sql.Timestamp.class, KryoTimeStampSerializer.class);
//	}
	public int getTypeId() {
		return 5;// 5 means no serializer
//		return 13;
	}

	public void destroy() {
	}

	public void write(ObjectDataOutput out, HazelcastObject object) throws IOException {
		Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(java.sql.Date.class, KryoDateSerializer.class);
		kryo.addDefaultSerializer(java.sql.Timestamp.class, KryoTimeStampSerializer.class);
		
		Output output = new Output((OutputStream)out);
		kryo.writeClassAndObject(output, object);
		output.flush();
	}

	public HazelcastObject read(ObjectDataInput in) throws IOException {
		Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(java.sql.Date.class, KryoDateSerializer.class);
		kryo.addDefaultSerializer(java.sql.Timestamp.class, KryoTimeStampSerializer.class);
		
		Input input = new Input((InputStream)in);
		return (HazelcastObject) kryo.readClassAndObject(input);
	}
}
