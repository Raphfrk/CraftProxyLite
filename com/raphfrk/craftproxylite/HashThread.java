package com.raphfrk.craftproxylite;

public class HashThread extends KillableThread {

	final static int[] startPoint;
	final static int[] step1;

	static {

		startPoint = new int[40];
		step1 = new int[40];

		int cnt;
		for(cnt=0;cnt<16;cnt++) {
			startPoint[cnt] = cnt<<3;
			step1[cnt] = 120;
		}

		int nextStart = 32768;
		for(cnt=16;cnt<40;cnt++) {
			if((cnt & 0x0007) == 0) {
				startPoint[cnt] = nextStart;
				nextStart += 16384;
			} else {
				startPoint[cnt] = startPoint[cnt-1] + 8;
			}
			step1[cnt] = 56;
		}

	}

	static byte[] transferArray(byte[] buffer, int start, int x, byte[] hashArray, int hashStart, boolean hashToBuffer) {

		if(buffer == null) {
			return null;
		}

		int bufferPos = startPoint[x] + start;
		int thisStep1 = step1[x];

		int hashPos = hashStart;
		for(int outer=0;outer<16;outer++) {
			for(int mid=0;mid<16;mid++) {
				for(int inner=0;inner<8;inner++) {

					if(!hashToBuffer) {
						hashArray[hashPos] = buffer[bufferPos];
					} else {
						buffer[bufferPos] = hashArray[hashPos];
					}
					hashPos++;
					bufferPos++;
				}
				bufferPos+=thisStep1;
			}
		}
		return buffer;
	}

	private int x;
	private int xEnd;
	private int thisStart;
	private int thisStep1;
	private byte[] buffer;
	private long[] hashes;
	private final Object syncObject = new Object();
	private boolean running = false;
	private boolean done = false;
	private final PassthroughConnection ptc;
	private boolean[] wipeBuffer;

	public HashThread(PassthroughConnection ptc) {
		this.ptc = ptc;
	}

	public boolean init(byte[] buffer, int start, int x, int xEnd, long[] hashes, boolean[] wipeBuffer) {

		synchronized(syncObject) {
			if(isRunning()) {
				System.out.println("Attempt made to init thread that was already running");
				return false;
			}

			this.wipeBuffer = wipeBuffer;
			thisStart = start;
			this.x = x;
			this.xEnd = xEnd;
			this.buffer = buffer;
			this.hashes = hashes;
			running = true;
			done = false;
			syncObject.notifyAll();
			return true;
		}

	}

	public boolean isRunning() {
		synchronized(syncObject) {
			return running;
		}
	}

	public boolean isDone() {
		synchronized(syncObject) {
			return done;
		}
	}

	public Object getSyncObject() {
		return syncObject;
	}

	public void doneJoin() {
		synchronized(syncObject) {
			while(!done && !killed()) {
				try {
					syncObject.wait();
				} catch (InterruptedException e) {
					done = true;
					running = false;
					kill();
				}
			}
		}
	}

	public void run() {

		while(!killed() && !ptc.killed()) {

			synchronized(syncObject) {
				while(!running && !ptc.killed()) {
					try {
						syncObject.wait();
					} catch (InterruptedException e) {
						synchronized(syncObject) {
							running = false;
							done = true;
							syncObject.notifyAll();
							kill();
							return;
						}
					}
				}
			}

			int count = 0;

			for(int xCurrent = x; xCurrent < xEnd; xCurrent++) {
				int pos = startPoint[xCurrent] + thisStart;
				thisStep1 = step1[xCurrent];

				long h = 1;

				for(int outer=0;outer<16;outer++) {
					for(int mid=0;mid<16;mid++) {
						for(int inner=0;inner<8;inner++) {

							if(!(wipeBuffer[xCurrent])) {
								h += (h<<5) + (long)buffer[pos];
							} else {
								buffer[pos] = 1;
							}
							count++;
							pos++;
						}
						pos+=thisStep1;
					}
				}
				if(!wipeBuffer[xCurrent]) {
					hashes[xCurrent] = h;
				}
			}

			synchronized(syncObject) {
				done = true;
				running = false;
				syncObject.notifyAll();
			}
		}
	}

}
