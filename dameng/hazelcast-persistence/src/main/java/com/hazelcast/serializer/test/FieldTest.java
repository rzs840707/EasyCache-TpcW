package com.hazelcast.serializer.test;

import com.hazelcast.serializer.entity.Person;

public class FieldTest {
	public void test() throws NoSuchFieldException {
		Person person = new Person();
		person.setId("1");
		person.setName("Source");
		
		Person copy = (Person) person.copy();
		copy.setId("2");
		System.out.println(person.getId());
		System.out.println(copy.getName());
	}
	public static void main(String[] args) throws NoSuchFieldException {
		new FieldTest().test();
	}
}
