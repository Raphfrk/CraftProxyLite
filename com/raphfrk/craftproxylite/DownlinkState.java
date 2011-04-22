package com.raphfrk.craftproxylite;

import java.util.LinkedHashSet;

public class DownlinkState {
	
	public LinkedHashSet<Integer> entityIds = new LinkedHashSet<Integer>();
	public LinkedHashSet<Long> activeChunks = new LinkedHashSet<Long>();
	
	static long convertToKey(int x, int z) {
		return (((long)x)<<32) | z;
	}
	
	public void addChunk(int x, int z) {
		activeChunks.add((((long)x)<<32) | ((long)z));
	}
	
	public void removeChunk(int x, int z) {
		activeChunks.remove((((long)x)<<32) | ((long)z));
	}
	
	
}
