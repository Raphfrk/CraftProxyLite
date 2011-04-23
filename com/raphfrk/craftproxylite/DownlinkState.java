package com.raphfrk.craftproxylite;

import java.util.LinkedHashSet;

public class DownlinkState {
	
	public LinkedHashSet<Integer> entityIds = new LinkedHashSet<Integer>();
	public LinkedHashSet<Long> activeChunks = new LinkedHashSet<Long>();
	
	static long convertToKey(int x, int z) {
		return (((long)x)<<32) | z;
	}
	
	static int[] convertFromKey(long key, int[] buffer) {
		int x = (int)((key & 0xFFFFFFFF00000000L) >> 32);
		int z = (int)(key & 0x00000000FFFFFFFFL);
		buffer[0] = x;
		buffer[1] = z;
		return buffer;
	}
	
	public void addChunk(int x, int z) {
		activeChunks.add((((long)x)<<32) | ((long)z));
	}
	
	public void removeChunk(int x, int z) {
		activeChunks.remove((((long)x)<<32) | ((long)z));
	}
	
	
}
