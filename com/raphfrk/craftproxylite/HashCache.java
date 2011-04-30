package com.raphfrk.craftproxylite;

import java.util.concurrent.ConcurrentHashMap;

public class HashCache {
	
	final static byte[] empty = new byte[0];
	
	ConcurrentHashMap<Long,byte[]> cache = new ConcurrentHashMap<Long,byte[]>();
	
	public byte[] addArray(long hash, byte[] a) {

		cache.put(hash, a);
		
		return a;
	}
	
	public byte[] getArray(long hash) {
		return cache.get(hash);
	}
	
	public boolean contains(long hash) {
		return cache.containsKey(hash);
	}
}
