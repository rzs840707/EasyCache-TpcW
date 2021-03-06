package com.hazelcast.serializer;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.serializer.entity.PortableCase;

public class PortableCaseFactory implements PortableFactory {

	@Override
	public Portable create(int classId) {
		if(classId == GlobalClassID.PortableCaseID) return new PortableCase();
		return null;
	}

}
