package com.raphfrk.craftproxylite;

import java.util.LinkedHashSet;

public class DownlinkState {
	
	public LinkedHashSet<Integer> entityIds = new LinkedHashSet<Integer>();
	public LinkedHashSet<Long> activeChunks = new LinkedHashSet<Long>();
	
	static long convertToKey(int x, int z) {
		long temp1 = z & 0x00000000FFFFFFFFL;
		long temp2 = ((long)x) << 32L;
		long temp = temp1 | temp2;
		return temp;
	}
	
	static int[] convertFromKey(long key, int[] buffer) {
		int x = (int)((key & 0xFFFFFFFF00000000L) >> 32L);
		int z = (int)(key & 0x00000000FFFFFFFFL);
		buffer[0] = x;
		buffer[1] = z;
		return buffer;
	}
	
	public void addChunk(int x, int z) {
		activeChunks.add(convertToKey(x, z));
	}
	
	public void removeChunk(int x, int z) {
		activeChunks.remove(convertToKey(x, z));
	}
	
	public boolean contains(int x, int z) {
		return activeChunks.contains(convertToKey(x, z));
	}
	
}
