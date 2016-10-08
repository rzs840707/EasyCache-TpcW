package com.hazelcast.persistance;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.serializer.entity.Human;

/**
 * @author zhaohui liu
 *
 */
public class MyPortableFactory implements PortableFactory {
 
	@Override
	public Portable create(int classId) {
        switch (classId) {
        case SerializationConfig.persistanceConfigClassId: 
        	return new PersistanceConfig();
        case SerializationConfig.peopleConfigClassId:
        	return new People();
        case SerializationConfig.humanConfigClassId:
        	return new Human();
    }
    return null;
	}

}
