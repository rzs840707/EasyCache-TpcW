package com.hazelcast.serializer.entity;


public class Person extends HazelcastObject {
	private String id;
	private String name;
	
	public Person() {
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
