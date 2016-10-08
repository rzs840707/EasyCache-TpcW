package com.hazelcast.serializer.test;

import java.util.Random;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.serializer.entity.Friend;
import com.hazelcast.serializer.entity.Human;
import com.hazelcast.serializer.entity.Person;

public class LocalStorageTest {
	
	public void ObjectPutTest(IMap<Integer, Person> map, Integer count) {
		Person person = new Person();
		person.setId("1");
		person.setName("Object_Test");
		
		Runtime runtime = Runtime.getRuntime();
		System.gc();
		long before_memory = runtime.totalMemory() - runtime.freeMemory();
		
		long start = System.currentTimeMillis();
		for(Integer i = 0; i < count; ++i) {
			map.put(i, person);
		}
		long end = System.currentTimeMillis();
		System.gc();
		long after_memory = runtime.totalMemory() - runtime.freeMemory();
		LocalMapStats status = map.getLocalMapStats();

		System.out.println("Object Memory Cost:" + (after_memory - before_memory));
		System.out.println("Object Put Time Cost:" + (end - start));
		System.out.println("Object Map Memory Cost:" + status.getOwnedEntryMemoryCost());
		System.out.println("Object Backup Memory Cost:" + status.getBackupEntryMemoryCost());
	}
	
	public void ObjectGetTest(IMap<Integer, Person> map, Integer count, Integer times) {
		Person person = null;
		Random random = new Random(System.currentTimeMillis());
		
		long start = System.currentTimeMillis();
		for(Integer i = 0; i < times; ++i) {
			person = map.get((Integer)(random.nextInt() % count));
			assert(person != null && person.getName() != null);
		}
		long end = System.currentTimeMillis();
		System.out.println("Object Get Time Cost:" + (end - start));
	}
	
	public void PortablePutTest(IMap<Integer, Human> map, Integer count) {
		Human human = new Human();
		human.setId("1");
		human.setName("Object_Test");
		
		Runtime runtime = Runtime.getRuntime();
		System.gc();
		long before_memory = runtime.totalMemory() - runtime.freeMemory();
		
		long start = System.currentTimeMillis();
		for(Integer i = 0; i < count; ++i) {
			map.put(i, human);
		}
		long end = System.currentTimeMillis();
		System.gc();
		long after_memory = runtime.totalMemory() - runtime.freeMemory();
		LocalMapStats status = map.getLocalMapStats();

		System.out.println("Portable Memory Cost:" + (after_memory - before_memory));
		System.out.println("Portable Put Time Cost:" + (end - start));
		System.out.println("Portable Map Memory Cost:" + status.getOwnedEntryMemoryCost());
		System.out.println("Portable Backup Memory Cost:" + status.getBackupEntryMemoryCost());
	}
	
	public void PortableGetTest(IMap<Integer, Human> map, Integer count, Integer times) {
		Human human = null;
		Random random = new Random(System.currentTimeMillis());
		
		long start = System.currentTimeMillis();
		for(Integer i = 0; i < times; ++i) {
			human = map.get((Integer)(random.nextInt() % count));
			assert(human != null && human.getName() != null);
		}
		long end = System.currentTimeMillis();
		System.out.println("Serialization Get Time Cost:" + (end - start));
	}
	
	public void DataSerializationPutTest(IMap<Integer, Friend> map, Integer count) {
		Friend friend = new Friend();
		friend.setId("1");
		friend.setName("Object_Test");
		
		Runtime runtime = Runtime.getRuntime();
		System.gc();
		long before_memory = runtime.totalMemory() - runtime.freeMemory();
		
		long start = System.currentTimeMillis();
		for(Integer i = 0; i < count; ++i) {
			map.put(i, friend);
		}
		long end = System.currentTimeMillis();
		System.gc();
		long after_memory = runtime.totalMemory() - runtime.freeMemory();
		LocalMapStats status = map.getLocalMapStats();

		System.out.println("Data Memory Cost:" + (after_memory - before_memory));
		System.out.println("Data Put Time Cost:" + (end - start));
		System.out.println("Data Map Memory Cost:" + status.getOwnedEntryMemoryCost());
		System.out.println("Data Backup Memory Cost:" + status.getBackupEntryMemoryCost());
	}
	
	public void DataSerializationGetTest(IMap<Integer, Friend> map, Integer count, Integer times) {
		Friend friend = null;
		Random random = new Random(System.currentTimeMillis());
		
		long start = System.currentTimeMillis();
		for(Integer i = 0; i < times; ++i) {
			friend = map.get((Integer)(random.nextInt() % count));
			assert(friend != null && friend.getName() != null);
		}
		long end = System.currentTimeMillis();
		System.out.println("Data Serialization Get Time Cost:" + (end - start));
	}
	
	public void ObjectTest(Integer putcount, Integer querytimes) {
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		IMap<Integer, Person> map = hz.getMap("People");
		map.setEnabled(false);
		
		this.ObjectPutTest(map, putcount);
		this.ObjectGetTest(map, putcount, querytimes);
		
		Hazelcast.shutdownAll();
	}
	
	public void PortableSerializationTest(Integer putcount, Integer querytimes) {
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		IMap<Integer, Human> map = hz.getMap("Human");
		map.setEnabled(false);
		
		this.PortablePutTest(map, putcount);
		this.PortableGetTest(map, putcount, querytimes);
		
		Hazelcast.shutdownAll();
	}
	
	public void DataSerializationTest(Integer putcount, Integer querytimes) {
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		IMap<Integer, Friend> map = hz.getMap("Friend");
		map.setEnabled(false);
		
		this.DataSerializationPutTest(map, putcount);
		this.DataSerializationGetTest(map, putcount, querytimes);
		
		Hazelcast.shutdownAll();
	}
	
	public static void main(String[] args) {
		LocalStorageTest test = new LocalStorageTest();
		
		Integer putcount = 500000;
		Integer querytimes = 500000;
		
		test.ObjectTest(putcount, querytimes);
		//test.PortableSerializationTest(putcount, querytimes);
		// test.DataSerializationTest(putcount, querytimes);
	}
}
