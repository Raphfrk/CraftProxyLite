package com.raphfrk.craftproxylite;

public class ByteCircleBuffer {

	private final byte[] buffer;
	private final int size;
	private int cnt;
	private boolean full;
	
	ByteCircleBuffer(int size) {
		this.size = size;
		buffer = new byte[size];
		cnt = 0;
		full = false;
	}

	public void write(byte x) {
		buffer[cnt] = x;
		cnt++;
		if(cnt>=size) {
			full = true;
			cnt = 0;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		if(!full) {
			for(int cnt2=0;cnt2<cnt;cnt2++) {
				if(first) {
					first = false;
					sb.append(buffer[cnt2]);
				} else {
					sb.append(", " + buffer[cnt2]);
				}
			}
		} else {
			int cntHigh = cnt + size;
			for(int cnt2=cnt;(cnt2!=cnt && cnt2 != cntHigh) || first;cnt2++) {
				if(cnt2>=size) {
					cnt2=0;
				}
				System.out.println(cnt + " " + cnt2);
				if(first) {
					first = false;
					sb.append(buffer[cnt2]);
				} else {
					sb.append(", " + buffer[cnt2]);
				}
			}
		}
		return sb.toString();
	}
	
}
