package org.bench4Q.servlet;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ShoppingCartLineRecord {
	public static Object lock = new Object();
	public static ConcurrentHashMap<Integer, HashSet<Integer>> shoppingcartlineHashmap = new ConcurrentHashMap<Integer, HashSet<Integer>>();
}
