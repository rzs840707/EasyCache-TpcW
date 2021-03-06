package simplesqltest;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.serializer.entity.Human;

public class indexTest {
	void initial(){
		number = 100000;
		maxAge = 100;
		maxNameLen = 13;
	}
	
	void generate(IMap<Integer, Human> imap){
		java.util.Date dt = new Date();
		Random r = new Random(dt.getTime());
		System.out.println("Start generate..");
		
		for(int i = 0; i < number; i++){
			int id = i;
			int age = r.nextInt(maxAge);
			String name = null;
			for(int j = 0; j < r.nextInt(maxNameLen)+2; j++){
				char c = (char) ('a'+r.nextInt(CHARTABLE));
				name += c;
			}
			Human e = new Human();
			e.setId(age+"");
			e.setName(name);
			imap.put(id, e);
			if(i%1000==0){System.out.print(i+", ");}
		}
		System.out.println("End generate!");
	}
	
	void addIndex(IMap<Integer, Human> imap , boolean bAdd){
		if(bAdd){
			imap.addIndex("age", false);
			System.out.println("Index added!");
		}
	}
	
	void testIndex(IMap<Integer, Human> imap ){
		java.util.Date dt = new Date();
		Random r = new Random(dt.getTime());
		
		int[] statics = new int [maxAge];
		for(int i = 0;i < maxAge; i++){
			statics[i] = 0;
		}
		System.out.println("Start test..");
		
		int count = 100000;
		long start = System.currentTimeMillis();
		
		for(int i = 0;i < count; i++){
			int targetAge =  r.nextInt(maxAge);
			Set<Human> Humanes = (Set<Human>) imap.values(new SqlPredicate("active AND age = "+targetAge));
			statics[targetAge] += Humanes.size();
			if(i%1000==0){System.out.print(i+", ");}
		}
		
		long end = System.currentTimeMillis();
		System.out.println( "SELECT TIME : " + (end - start));

		System.out.println("End test!");
		
		for(int i = 0;i < maxAge; i++){
			System.out.println(statics[i]);
		}
	}
	
	public static void main(String[] args) {
		Config cfg = new Config();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
		IMap<Integer, Human> imap = instance.getMap("Humanes");
		
		indexTest it = new indexTest();
		it.initial();
		
		it.addIndex(imap, true);
		
		it.generate(imap);
		
		it.testIndex(imap);
	}
	
	private int number;
	private int maxAge;
	private int maxNameLen;
	private int CHARTABLE = 26;
}
