package com.hazelcast.persistance;

import java.util.Collection;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

public class PredicateTest {
	public void testPredicate() {
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		IMap<String, People> map = hz.getMap("People");
		
		People people = new People();
		people.setId(1);
		people.setName("test");
		
		map.put("id=1", people);
		
		Collection<People> collection = map.values(new SqlPredicate("name = test"));
		for (People p : collection) {
			System.out.println(p.getId());
		}
		Hazelcast.shutdownAll();
	}
	public static void main(String[] args) {
		new PredicateTest().testPredicate();
	}
}
