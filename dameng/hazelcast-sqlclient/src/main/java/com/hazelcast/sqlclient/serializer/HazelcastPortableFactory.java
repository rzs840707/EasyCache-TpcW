package com.hazelcast.sqlclient.serializer;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.sqlclient.HazelcastPortableObject;

public class HazelcastPortableFactory implements PortableFactory {

	public Portable create(int classId) {
		if(classId == HazelcastPortableObject.CLASS_ID) return new HazelcastPortableObject();
		return null;
	}

}
