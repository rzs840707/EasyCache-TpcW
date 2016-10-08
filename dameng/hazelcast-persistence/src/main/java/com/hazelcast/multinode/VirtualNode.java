package com.hazelcast.multinode;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

public class VirtualNode {
	public static void main(String[] args) {
		System.out.println("hello multinode");
		VirtualNode vn = new VirtualNode();
		vn.testMultiNode();
	}
	
	void testMultiNode(){
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(null);
	}
}
